package org.schema.game.common.controller.elements.explosive;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Universe;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.server.ServerMessage;

public class ExplosiveCollectionManager extends ElementCollectionManager<ExplosiveUnit, ExplosiveCollectionManager, ExplosiveElementManager> {

	private long lastSpontaniousCheck;

	public ExplosiveCollectionManager(SegmentController segController, ExplosiveElementManager em) {
		super(ElementKeyMap.EXPLOSIVE_ID, segController, em);
	}

	@Override
	public int getMargin() {
		return 0;
	}

	//	/**
	//	 * @return the totalExplosive
	//	 */
	//	public float getTotalExplosive() {
	//		return totalExplosive;
	//	}

	@Override
	protected Class<ExplosiveUnit> getType() {
		return ExplosiveUnit.class;
	}

	@Override
	public boolean needsUpdate() {
		return true;
	}

	@Override
	public void update(Timer timer) {
		super.update(timer);
		
		if(getSegmentController().isOnServer() && getElementCollections().size() > 0){
			float chance = getConfigManager().apply(StatusEffectType.WARHEAD_CHANCE_FOR_SPONTANIOUS_EXPLODE, 0.0f);
			if(chance > 0 ){
				long delay = (long)(ExplosiveElementManager.SPONTANIOUS_EXPLODE_CHECK_FREQUENCY_SEC * 1000f);
				if(timer.currentTime - lastSpontaniousCheck > delay){
					float ran = Universe.getRandom().nextFloat();
					long explode = rawCollection.iterator().nextLong();
					getSegmentController().sendControllingPlayersServerMessage(Lng.astr("---CRITICAL WARNING---\nEffects on your ship is causing your warheads to spontaniously explode!"), ServerMessage.MESSAGE_TYPE_ERROR);
					getElementManager().addExplosion(ElementCollection.getPosFromIndex(explode, new Vector3i()));
					lastSpontaniousCheck = timer.currentTime;
				}
			}
		}
	}

	@Override
	public ExplosiveUnit getInstance() {
		return new ExplosiveUnit();
	}

	@Override
	protected void onChangedCollection() {
		//		refreshMaxExplosive();
	}

	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {
		return new GUIKeyValueEntry[]{new ModuleValueEntry(Lng.str("explosive Power"), getElementManager().getDamage())};
	}

	@Override
	public String getModuleName() {
		return Lng.str("Explosives System");
	}
}
