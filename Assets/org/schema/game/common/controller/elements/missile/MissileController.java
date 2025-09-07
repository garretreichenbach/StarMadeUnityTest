package org.schema.game.common.controller.elements.missile;

import api.listener.events.weapon.MissilePostAddEvent;
import api.mod.StarLoader;
import com.bulletphysics.linearmath.Transform;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ammo.missile.MissileCapacityElementManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.missile.*;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.network.objects.NetworkClientChannel;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.network.server.ServerState;

import javax.vecmath.Vector3f;

import static org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager.WeaponType.MISSILE;

public class MissileController {

	private static short missileIdCreator;
	private final boolean onServer;
	private final ServerMissileManager missileManager;
	private GameServerState state;
	private long lastsentMissile;

	public MissileController(GameServerState state) {
		this.state = state;
		onServer = state instanceof ServerState;
		missileManager = new ServerMissileManager(state);
	}

	public Missile addDumbMissile(Damager owner, Transform start, Vector3f dir, float speed, float damage, float distance, long weaponId, short lightType) {
		DumbMissile dumbMissile = new DumbMissile(state);
		
		

		dumbMissile.setSpeed(speed);
		dumbMissile.setDistance(distance);
		dumbMissile.setDamage((int) damage);
		return addMissile(dumbMissile, start, dir, owner, weaponId, lightType);
	}
	public Missile addBombMissile(Damager owner, Transform start, float activationTimer, Vector3f dir, float speed, float damage, float distance, long weaponId, int missileCapacityUsed, short lightType) {
		BombMissile m = new BombMissile(state);
		
		m.setCapacityConsumption(missileCapacityUsed);
		m.setActivationTimer(activationTimer);
		m.setSpeed(speed);
		m.setDistance(distance);
		m.setDamage((int) damage);
		return addMissile(m, start, dir, owner, weaponId, lightType);
	}

	public Missile addFafoMissile(Damager owner, Transform start, Vector3f dir, float speed, float damage, float distance, long weaponId, SimpleTransformableSendableObject aquiredTarget, short lightType) {
		//		System.err.println("[MISSILE] ADDING FAFO MISSILE!!!!!!!!!!!!!!! Dir: "+dir+" onServer: "+isOnServer());
		FafoMissile fafoMissile = new FafoMissile(state);
		fafoMissile.setSpeed(speed);
		fafoMissile.setDistance(distance);
		fafoMissile.setDamage((int) damage);
		fafoMissile.setDesignatedTarget(aquiredTarget);
		
		
		return addMissile(fafoMissile, start, dir, owner, weaponId, lightType);

	}

	public Missile addHeatMissile(Damager owner, Transform start, Vector3f dir, float speed, float damage, float distance, long weaponId, short lightType) {
		//		System.err.println("ADDING HEAT MISSILE!!!!!!!!!!!!!!!");
		HeatMissile heatMissile = new HeatMissile(state);
		heatMissile.setSpeed(speed);
		heatMissile.setDistance(distance);
		heatMissile.setDamage((int) damage);

		return addMissile(heatMissile, start, dir, owner, weaponId, lightType);

	}

	private Missile addMissile(Missile missile, Transform start, Vector3f dir, Damager owner, long weaponId, short lightType) {
		assert (onServer);
		if (owner == null) {
			throw new NullPointerException("OWNER NULL");
		}
		float missileDamage = missile.getDamage();
//		if (effectType != 0 && owner != null && owner instanceof EditableSendableSegmentController) {
//			EffectElementManager<?, ?, ?> effect = ((EditableSendableSegmentController)owner).getEffect(owner, effectType);
//			if (effect != null) {
////				System.err.println("[MISSILE] missile has additional radius from effect: "+(effectRatio * effect.getMissileExplosiveRadius()));
//				missile.setBlastRadius(missile.getBlastRadius() + Math.max(0, effectRatio * effect.getMissileExplosiveRadius()));
//				
//				missileDamage = effect.modifyTotalDamage(missileDamage, DamageDealerType.MISSILE, effectRatio);
//			}
//		}
		missile.setupHp(missileDamage);
		//System.out.println("[MISSILE SPAWNED] Damage: " + missileDamage + " and HP " + missile.getHp());
		
		
//		System.err.println("[SERVER][MISSILE] NEW MISSILE: "+start.origin+"; "+missile+": Speed: "+missile.getSpeed()+", Damage: "+missile.getDamage()+", Radius: "+missile.getBlastRadius()+", Distance: "+missile.getDistance());
		missile.setOwner(owner);
		missile.setSectorId((owner).getSectorId(), false);
		missile.getInitialTransform().set(start);
		missile.getWorldTransform().set(start);
		assert(dir.lengthSquared() != 0);
		missile.setDirection(dir);
		missile.setWeaponId(weaponId);
		missile.setId((short) ((missileIdCreator++)%Short.MAX_VALUE));
		missile.setColorType(lightType);
		//INSERTED CODE
		//Fire event after data is set but before sent to the client
		MissilePostAddEvent event = new MissilePostAddEvent(this, missile);
		StarLoader.fireEvent(event, true);
		///
		missileManager.addMissile(missile);
		assert(!Float.isNaN(missile.getDirection(new Vector3f()).x) );
		
		if(owner instanceof SegmentController) {
			SegmentController root = ((SegmentController)owner).railController.getRoot();
			
			if(root instanceof ManagedSegmentController<?>) {
				ManagerContainer<?> mc = ((ManagedSegmentController<?>)root).getManagerContainer();
				MissileCapacityElementManager missiles = (MissileCapacityElementManager) mc.getAmmoSystem(MISSILE).getElementManager();
				if(missiles.getCapacityFilled() >= missile.getCapacityConsumption() ) {
					
					float newTimer;
					if((missiles.ammoReloadResetsOnManualFire() && root.isConrolledByActivePlayer()) ||
							missiles.ammoReloadResetsOnAIFire() && root.isAIControlled()) {
						newTimer = missiles.getAmmoCapacityReloadTime();
					}else {
						//no reset of timer
						newTimer = missiles.getAmmoTimer();
					}
					mc.setAmmoCapacity(MISSILE, missiles.getCapacityFilled() - missile.getCapacityConsumption(), newTimer, true);
				}else {
					if(missile.getCapacityConsumption() > 1 && missiles.getCapacityFilled() < 2) {
						if(owner != null) {
							if(state.getUpdateTime() - lastsentMissile > 1000) {
								owner.sendServerMessage(Lng.astr("Can't fire missile. It takes %s missile capacity to fire!",missile.getCapacityConsumption()), ServerMessage.MESSAGE_TYPE_ERROR);
								lastsentMissile = state.getUpdateTime();
								mc.setAmmoCapacity(MISSILE, missiles.getCapacityFilled(), missiles.getAmmoTimer(), true);
							}
						}
					}
				}
			}
		}
		
		return missile;

	}
	
	public void fromNetwork(NetworkClientChannel networkClientChannel) {
		missileManager.fromNetwork(networkClientChannel);
	}

	/**
	 * @return the onServer
	 */
	public boolean isOnServer() {
		return onServer;
	}

	public void updateServer(Timer timer) {
		missileManager.updateServer(timer);
	}

	public Missile hasHit(short missileID, int projectileIdFilter, Vector3f posBeforeUpdate, Vector3f posAfterUpdate) {
		return missileManager.hasHit(missileID, projectileIdFilter, posBeforeUpdate, posAfterUpdate);
	}

	/**
	 * @return the missileManager
	 */
	public ServerMissileManager getMissileManager() {
		return missileManager;
	}

	

}
