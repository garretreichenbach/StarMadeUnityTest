package org.schema.game.common.controller.elements.explosive;

import javax.vecmath.Vector3f;

import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.EditableSendableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.HitType;
import org.schema.game.common.controller.elements.BlockKillInterface;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.ManagerUpdatableInterface;
import org.schema.game.common.controller.elements.UsableControllableSingleElementManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.explosion.AfterExplosionCallback;
import org.schema.game.common.data.explosion.ExplosionData;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.Universe;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.sound.controller.AudioController;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;

public class ExplosiveElementManager extends UsableControllableSingleElementManager<ExplosiveUnit, ExplosiveCollectionManager, ExplosiveElementManager> implements ManagerUpdatableInterface, BlockKillInterface {

	@ConfigurationElement(name = "SpontaniousExplodeCheckFrequency")
	public static float SPONTANIOUS_EXPLODE_CHECK_FREQUENCY_SEC = 10;

	@ConfigurationElement(name = "BlockDamage")
	public static float INITIAL_DAMAGE = 10000;

	// public static double BONUS_PER_UNIT = 1.25;
	@ConfigurationElement(name = "PlayerDamage")
	public static float PLAYER_DAMAGE = 200;

	@ConfigurationElement(name = "Radius")
	public static float INITIAL_RADIUS = 8;

	private final ObjectArrayFIFOQueue<ExplosionType> explosions = new ObjectArrayFIFOQueue<ExplosionType>();

	private LongOpenHashSet explodingBlocks = new LongOpenHashSet();

	public ExplosiveElementManager(SegmentController segmentController) {
		super(segmentController, ExplosiveCollectionManager.class);
	}

	public void addExplosion(Vector3i fromBlock) {
		addExplosion(fromBlock, null);
	}

	public float getDamage() {
		return getSegmentController().getConfigManager().apply(StatusEffectType.WARHEAD_DAMAGE, INITIAL_DAMAGE);
	}

	public float getDamagePlayer() {
		return getSegmentController().getConfigManager().apply(StatusEffectType.WARHEAD_DAMAGE, PLAYER_DAMAGE);
	}

	public float getRadius() {
		return getSegmentController().getConfigManager().apply(StatusEffectType.WARHEAD_RADIUS, INITIAL_RADIUS);
	}

	public void addExplosion(Vector3i fromBlock, Vector3f explosionRelPointPoint) {
		long index = ElementCollection.getIndex(fromBlock);
		if (explodingBlocks.contains(index)) {
			return;
		}
		explodingBlocks.add(index);
		ExplosiveUnit explosiveUnit = null;
		if (getSegmentController().isOnServer()) {
			GameServerState state = (GameServerState) getSegmentController().getState();
			Sector sector = state.getUniverse().getSector(getSegmentController().getSectorId());
			if (sector != null && sector.isProtected()) {
				// System.err.println("[EXPLOSION] not adding explosion because sector is protected!");
				return;
			}
		}
		for (ExplosiveUnit u : getCollection().getElementCollections()) {
			if (u.getNeighboringCollection().contains(ElementCollection.getIndex(fromBlock))) {
				explosiveUnit = u;
				break;
			}
		}
		if (explosiveUnit != null) {
			explodingBlocks.addAll(explosiveUnit.getNeighboringCollection());
			synchronized (explosions) {
				if (explosionRelPointPoint == null) {
					explosionRelPointPoint = new Vector3f(fromBlock.x - SegmentData.SEG_HALF, fromBlock.y - SegmentData.SEG_HALF, fromBlock.z - SegmentData.SEG_HALF);
				// getSegmentController().getWorldTransform().transform(eplostionRelPointPoint);
				}
				ExplosionType expl = new ExplosionType(new Vector3i(fromBlock), new Vector3f(explosionRelPointPoint), (EditableSendableSegmentController) getSegmentController(), ExplosionData.INNER);
				explosions.enqueue(expl);
			}
		} else {
			System.err.println("EXPLOSION POINT NOT FOUND " + fromBlock + " on " + getSegmentController());
		}
	}

	@Override
	public void onControllerChange() {
	// this.setChanged();
	// this.notifyObservers(SegNotifyType.SHIP_ELEMENT_CHANGED);
	}

	// public float getActualExplosive() {
	// float explosive = explosiveManager.getTotalExplosive();
	// if(explosive == 0){
	// return 0;
	// }
	// return explosive;
	// }
	// 
	@Override
	public void update(Timer timer) {
		if (!explosions.isEmpty()) {
			long time = System.currentTimeMillis();
			synchronized (explosions) {
				int size = explosions.size();
				int i = 0;
				while (i < size && !explosions.isEmpty()) {
					final ExplosionType explosion = explosions.first();
					getSegmentController().getWorldTransform().transform(explosion.where);
					// System.err.println("Executing explosion for "+explosion.to);
					Transform t = new Transform();
					t.setIdentity();
					t.origin.set(explosion.where);
					// no nead since those kind of explosions don't have a push/pull effect
					Vector3f fromPos = new Vector3f();
					Vector3f toPos = new Vector3f();
					if (getSegmentController().isOnServer()) {
						AfterExplosionCallback afterExplosionCallback = () -> {
							if (explodingBlocks.remove(ElementCollection.getIndex(explosion.id)))
								;
						};
						((EditableSendableSegmentController) getSegmentController()).addExplosion(getSegmentController(), DamageDealerType.EXPLOSIVE, HitType.INTERNAL, ElementCollection.getIndex(explosion.id), t, getRadius(), getDamage(), true, afterExplosionCallback, ExplosionData.INNER | ExplosionData.IGNORESHIELDS_GLOBAL);
						/*AudioController.fireAudioEventRemote("BEAM_FIRE", getSegmentController().getId(), new AudioTag[] { AudioTags.GAME, AudioTags.SHIP, AudioTags.EXPLOSION }, AudioParam.ONE_TIME, AudioController.ent(getSegmentController(), explosion.where, 0L, 10))*/
						AudioController.fireAudioEventRemoteID(890, getSegmentController().getId(), AudioController.ent(getSegmentController(), explosion.where, 0L, 10));
					}
					explosions.dequeue();
					size--;
					i++;
				}
			}
		} else if(!explodingBlocks.isEmpty()){
			//FIXME no explosions to do, but "exploding blocks" still here. Will basically block them from blowing up in the future (bug)
			// It seems like the code was left halfway between a group explosion and singular explosion implementation.
			// I'm not entirely sure what the intended implementation is, so I am just doing this for now to prevent inexplicably unexplodable warheads. -Ithirahad
			System.err.println("[WARNING] Unexploded warheads present, but no explosions queued. Manually clearing exploding block list.");
			explodingBlocks.clear();
		}
	}

	@Override
	public ControllerManagerGUI getGUIUnitValues(ExplosiveUnit firingUnit, ExplosiveCollectionManager col, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return null;
	}

	@Override
	protected String getTag() {
		return "explosive";
	}

	@Override
	public ExplosiveCollectionManager getNewCollectionManager(SegmentPiece position, Class<ExplosiveCollectionManager> clazz) {
		return new ExplosiveCollectionManager(getSegmentController(), this);
	}

	private final Vector3i paramTmp = new Vector3i();

	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {
		if (!unit.isFlightControllerActive()) {
			return;
		}
		if (!(Ship.core.equals(unit.getParameter(paramTmp)))) {
			// can only control explosive at core
			return;
		}
	// float explosive = getActualExplosive() ;
	}

	public class ExplosionType {

		private final Vector3i id;

		private final Vector3f where;

		private long started;

		public ExplosionType(Vector3i id, Vector3f where, EditableSendableSegmentController from, byte type) {
			super();
			this.started = System.currentTimeMillis();
			this.id = id;
			this.where = where;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			return ((ExplosionType) obj).id.equals(id);
		}
	}

	@Override
	public void onKilledBlock(long pos, short type, Damager from) {
		if (getSegmentController().isOnServer() && type == ElementKeyMap.EXPLOSIVE_ID) {
			float chance = getSegmentController().getConfigManager().apply(StatusEffectType.WARHEAD_CHANCE_FOR_EXPLOSION_ON_HIT, 1.0f);
			if (chance >= 1f || Universe.getRandom().nextFloat() <= chance) {
				addExplosion(ElementCollection.getPosFromIndex(pos, new Vector3i()));
			}
		}
	}

	@Override
	public boolean canUpdate() {
		return true;
	}

	@Override
	public void onNoUpdate(Timer timer) {
	}
}
