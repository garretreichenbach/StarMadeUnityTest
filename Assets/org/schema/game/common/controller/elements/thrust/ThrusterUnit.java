package org.schema.game.common.controller.elements.thrust;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.StructureAudioEmitter;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

public class ThrusterUnit extends ElementCollection<ThrusterUnit, ThrusterCollectionManager, ThrusterElementManager> implements StructureAudioEmitter {

	private final Long2LongOpenHashMap lastElements = new Long2LongOpenHashMap();

	float thrust;

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.element.ElementCollection#addElement(long)
	 */
	@Override
	public void addElement(long index, int x, int y, int z) {
		super.addElement(index, x, y, z);
		onAdd(index, x, y, z);
	}

	/**
	 * updates the significator so it is the smallest
	 * (by order x,y,z)
	 *
	 * @param v
	 */
	@Override
	protected void significatorUpdate(int x, int y, int z, int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, long index) {
		significatorUpdateMin(x, y, z, xMin, yMin, zMin, xMax, yMax, zMax, index);
	}

	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return elementCollectionManager.getElementManager().getGUIUnitValues(this, elementCollectionManager, supportCol, effectCol);
	// return ControllerManagerGUI.create(state, "Thruster Module", this, new ModuleValueEntry(Lng.str("Thrust", StringTools.formatPointZero(thrust)));
	}

	/**
	 * @return the lastElements
	 */
	public Long2LongOpenHashMap getLastElements() {
		return lastElements;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.element.ElementCollection#onAdd(org.schema.common.util.linAlg.Vector3i)
	 */
	protected void onAdd(long index, int x, int y, int z) {
		long zPlaneIndex = ElementCollection.getIndex(x, y, 0);
		if (!lastElements.containsKey(zPlaneIndex) || z < ElementCollection.getPosZ(lastElements.get(zPlaneIndex))) {
			lastElements.put(zPlaneIndex, index);
		}
	}

	public void refreshThrusterCapabilities() {
		switch(ThrusterElementManager.UNIT_CALC_STYLE) {
			case BOX_DIM_ADD -> {
				thrust = getBBTotalSize();
				float tot = (float) (Math.pow(size(), ThrusterElementManager.THRUSTER_BONUS_POW_PER_UNIT));
				tot *= ThrusterElementManager.UNIT_CALC_MULT.get(isUsingPowerReactors());
				thrust += tot;
			}
			case BOX_DIM_MULT -> {
				thrust = getAbsBBMult();
				float tot2 = (float) (Math.pow(size(), ThrusterElementManager.THRUSTER_BONUS_POW_PER_UNIT));
				tot2 *= ThrusterElementManager.UNIT_CALC_MULT.get(isUsingPowerReactors());
				thrust += tot2;
			}
			case LINEAR ->
				// System.err.println("REFRESH LINEAR :::: "+ThrusterElementManager.UNIT_CALC_MULT.get(isUsingPowerReactors()));
				thrust = (float) (Math.pow(size(), ThrusterElementManager.THRUSTER_BONUS_POW_PER_UNIT) * ThrusterElementManager.UNIT_CALC_MULT.get(isUsingPowerReactors()));
			default -> throw new IllegalArgumentException();
		}
		thrust = Math.max(1f, thrust);
	}

	public float getPowerConsumption() {
		float powerConsumed;
		if (ThrusterElementManager.POWER_CONSUMPTION_PER_BLOCK <= 0) {
			powerConsumed = thrust;
		} else {
			powerConsumed = (float) (ThrusterElementManager.POWER_CONSUMPTION_PER_BLOCK * size());
		}
		return powerConsumed;
	}

	@Override
	public void startAudio() {
		/*AudioController.fireAudioEvent("THRUSTER", new AudioTag[] { AudioTags.GAME, AudioTags.AMBIENCE, AudioTags.SHIP, AudioTags.BLOCK, AudioTags.THRUSTER }, AudioParam.START, AudioController.ent(getSegmentController(), getElementCollectionId(), getSignificator(), this))*/
		AudioController.fireAudioEventID(929, AudioController.ent(getSegmentController(), getElementCollectionId(), this));
	}

	@Override
	public void stopAudio() {
		/*AudioController.fireAudioEvent("THRUSTER", new AudioTag[] { AudioTags.GAME, AudioTags.AMBIENCE, AudioTags.SHIP, AudioTags.BLOCK, AudioTags.THRUSTER }, AudioParam.STOP, AudioController.ent(getSegmentController(), getElementCollectionId(), getSignificator(), this))*/
		AudioController.fireAudioEventID(930, AudioController.ent(getSegmentController(), getElementCollectionId(), this));
	}
}
