package org.schema.game.common.controller.elements.power.reactor.chamber;

import java.util.List;
import java.util.Set;

import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.controller.elements.power.reactor.PowerConsumer;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;

public class ConduitCollectionManager extends ElementCollectionManager<ConduitUnit, ConduitCollectionManager, ConduitElementManager> implements PowerConsumer {

	private boolean allConduitsReady;

	public ConduitCollectionManager(
			SegmentController segController, ConduitElementManager  em) {
		super(ElementKeyMap.REACTOR_CONDUIT, segController, em);
	}

	@Override
	public int getMargin() {
		return 0;
	}

	@Override
	protected Class<ConduitUnit> getType() {
		return ConduitUnit.class;
	}
	

	@Override
	public boolean needsUpdate() {
		return true;
	}

	@Override
	public void update(Timer timer) {
		boolean allFinished = true;
		for(ConduitUnit a : getElementCollections()){
			a.updateCalcConnections();
			if(!a.isFinishedCalcConnections()){
				allFinished = false;
			}
		}
		if(allFinished && !allConduitsReady){
			getPowerInterface().onAnyReactorModulesChanged();
		}
		this.allConduitsReady = allFinished;
	}
	public void checkRemovedChamber(List<ReactorChamberUnit> currentChambers) {
		for(ConduitUnit a : getElementCollections()){
			for(ReactorChamberUnit c : a.getConnectedChambers()){
				if(!currentChambers.contains(c)){
					a.flagCalcConnections();
				}
			}
		}
	}
	@Override
	public ConduitUnit getInstance() {
		return new ConduitUnit();
	}
	@Override
	protected void onChangedCollection() {
	}

	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {
		return new GUIKeyValueEntry[]{new ModuleValueEntry(Lng.str("Test"), "")};
	}

	@Override
	public String getModuleName() {
		return Lng.str("Reactor Conduit");
	}

	@Override
	public float getSensorValue(SegmentPiece connected){
		return  0;
	}

	public Set<ReactorChamberUnit> getConnected(long index) {
		for(ConduitUnit a : getElementCollections()){
			if(a.getNeighboringCollection().contains(index)){
				return a.getConnectedChambers();
			}
		}
		return null;
	}

	public boolean isAllConduitsReady() {
		return allConduitsReady;
	}
	public void flagConduitsDirty(){
		allConduitsReady = false;
	}

	@Override
	public double getPowerConsumedPerSecondResting() {
		float powCons = VoidElementManager.POWER_REACTOR_CONDUIT_POWER_CONSUMPTION_PER_SEC;
		getSegmentController().getConfigManager().apply(StatusEffectType.POWER_CONDUIT_POWER_USAGE, powCons);
		
		return powCons;
	}

	@Override
	public double getPowerConsumedPerSecondCharging() {
		return getPowerConsumedPerSecondResting();
	}

	@Override
	public boolean isPowerCharging(long curTime) {
		return false;
	}

	@Override
	public void setPowered(float powered) {
	}

	@Override
	public float getPowered() {
		return 1;
	}
	@Override
	public boolean isPowerConsumerActive() {
		return false;
	}
	@Override
	public void reloadFromReactor(double secTime, Timer timer, float tickTime, boolean powerCharging, float poweredResting) {
	}
	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.OTHERS;
	}
	@Override
	public void dischargeFully() {
	}
	
}
