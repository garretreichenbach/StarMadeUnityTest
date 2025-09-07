package org.schema.game.common.controller.elements.factory;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.StructureAudioEmitter;
import org.schema.schine.common.language.Lng;
import org.schema.schine.sound.controller.AudioController;

public class FactoryUnit extends ElementCollection<FactoryUnit, FactoryCollectionManager, FactoryElementManager> implements StructureAudioEmitter {

	private int capability;

	public void refreshFactoryCapabilities(FactoryCollectionManager factoryCollectionManager) {
		this.capability = getNeighboringCollection().size();
		factoryCollectionManager.addCapability(this.capability);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FactoryUnit";
	}

	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return ControllerManagerGUI.create(state, Lng.str("Factory Module"), this, new ModuleValueEntry(Lng.str("Efficiency"), capability));
	}

	@Override
	public void startAudio() {
		/*AudioController.fireAudioEvent("FACTORY", new AudioTag[] { AudioTags.GAME, AudioTags.AMBIENCE, AudioTags.SHIP, AudioTags.BLOCK, AudioTags.FACTORY }, AudioParam.START, AudioController.ent(getSegmentController(), getElementCollectionId(), getSignificator(), this))*/
		AudioController.fireAudioEventID(891, AudioController.ent(getSegmentController(), getElementCollectionId(), this));
	}

	@Override
	public void stopAudio() {
		/*AudioController.fireAudioEvent("FACTORY", new AudioTag[] { AudioTags.GAME, AudioTags.AMBIENCE, AudioTags.SHIP, AudioTags.BLOCK, AudioTags.FACTORY }, AudioParam.STOP, AudioController.ent(getSegmentController(), getElementCollectionId(), getSignificator(), this))*/
		AudioController.fireAudioEventID(892, AudioController.ent(getSegmentController(), getElementCollectionId(), this));
	}
}
