package org.schema.game.common.controller.elements.door;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ActivateValueEntry;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.sound.controller.AudioController;
import org.schema.schine.sound.controller.AudioEmitter;

import java.util.Locale;

public class DoorUnit extends ElementCollection<DoorUnit, DoorCollectionManager, VoidElementManager<DoorUnit, DoorCollectionManager>> implements AudioEmitter {

	public void activate(boolean active) {
		// only on server
		// this is still in synchronize(getBlockActivationBuffer())
		// since its called from within
		for(long index : getNeighboringCollection()) {
			long d;
			if(active) {
				d = ElementCollection.getActivation(index, false, false);
				assert (ElementCollection.getType(d) < 10);
			} else {
				d = ElementCollection.getDeactivation(index, false, false);
				assert (ElementCollection.getType(d) < 10);
			}
			((SendableSegmentController) getSegmentController()).getBlockActivationBuffer().enqueue(d);
		}
		/*AudioController.fireAudioEventRemote("DOOR", getSegmentController().getId(), new AudioTag[] { AudioTags.GAME, AudioTags.ACTIVATE, AudioTags.SHIP, AudioTags.BLOCK, AudioTags.DOOR }, AudioParam.ONE_TIME, AudioController.ent(getSegmentController(), getElementCollectionId(), getSignificator(), this))*/
		//AudioController.fireAudioEventRemoteID(888, getSegmentController().getId(), AudioController.ent(getSegmentController(), getElementCollectionId(), this));
		if(active) startAudio();
		else stopAudio();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.element.ElementCollection#cleanUp()
	 */
	@Override
	public void cleanUp() {
		super.cleanUp();
	}

	@Override
	public boolean hasMesh() {
		return false;
	}

	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return ControllerManagerGUI.create(state, Lng.str("Door Module"), this, new ActivateValueEntry(new Object() {

			@Override
			public String toString() {
				return (isActive() ? Lng.str("Open") : Lng.str("Close"));
			}
		}) {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(889);
					activateClient(isActive());
				}
			}
		});
	}

	public boolean isActive() {
		if(getElementCollectionId() != null) {
			getElementCollectionId().refresh();
			return getElementCollectionId().isActive();
		}
		return false;
	}

	public void activateClient(boolean open) {
		getSegmentController().sendBlockActivation(ElementCollection.getEncodeActivation(getElementCollectionId(), true, !open, false));
	}

	@Override
	public void startAudio() {
		/*
		ElementInformation info = ElementKeyMap.getInfo(getElementCollectionId().getType());
		if(info.getName().toLowerCase(Locale.ENGLISH).contains("forcefield")) {
//			AudioController.fireAudioEvent("0022_item - forcefield activate", AudioController.ent(getSegmentController(), getElementCollectionId(), this));
//			AudioController.firedAudioLoopStart("0022_item - forcefield loop", AudioController.ent(getSegmentController(), getElementCollectionId(), this));
		} //else AudioController.fireAudioEvent("0022_ambience sfx - air release steam valve", AudioController.ent(getSegmentController(), getElementCollectionId(), this));
		 */
	}

	@Override
	public void stopAudio() {
		/*
		ElementInformation info = ElementKeyMap.getInfo(getElementCollectionId().getType());
		if(info.getName().toLowerCase(Locale.ENGLISH).contains("forcefield")) {
//			AudioController.fireAudioEvent("0022_item - forcefield powerdown", AudioController.ent(getSegmentController(), getElementCollectionId(), this));
//			AudioController.firedAudioLoopStop("0022_item - forcefield loop");
		} //else AudioController.fireAudioEvent("0022_ambience sfx - air release steam valve", AudioController.ent(getSegmentController(), getElementCollectionId(), this));
		 */
	}
}
