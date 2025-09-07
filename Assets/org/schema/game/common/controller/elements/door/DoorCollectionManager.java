package org.schema.game.common.controller.elements.door;

import org.schema.game.client.view.gui.structurecontrol.ActivateValueEntry;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.BlockActivationListenerInterface;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;

public class DoorCollectionManager extends ElementCollectionManager<DoorUnit, DoorCollectionManager, VoidElementManager<DoorUnit, DoorCollectionManager>> implements BlockActivationListenerInterface {

	public DoorCollectionManager(SegmentController segController, VoidElementManager<DoorUnit, DoorCollectionManager> em) {
		super(ElementKeyMap.DOOR_ELEMENT, segController, em);
	}

	@Override
	public int getMargin() {
		return 0;
	}

	@Override
	protected Class<DoorUnit> getType() {
		return DoorUnit.class;
	}

	@Override
	public boolean needsUpdate() {
		return false;
	}

	@Override
	public DoorUnit getInstance() {
		return new DoorUnit();
	}

	@Override
	public boolean isUsingIntegrity() {
		return false;
	}

	@Override
	protected void onChangedCollection() {
	// setChanged();
	// notifyObservers(getCollection());
	}

	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {
		return new GUIKeyValueEntry[] { new ActivateValueEntry(Lng.str("Close all")) {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(886);
					for (DoorUnit u : getElementCollections()) {
						u.activateClient(false);
					}
				}
			}
		}, new ActivateValueEntry(Lng.str("Open all")) {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(887);
					for (DoorUnit u : getElementCollections()) {
						u.activateClient(true);
					}
				}
			}
		} };
	}

	@Override
	public String getModuleName() {
		return Lng.str("Door System");
	}

	@Override
	public int onActivate(SegmentPiece piece, boolean oldActive, boolean active) {
		if (getSegmentController().isOnServer()) {
			long absIndex = piece.getAbsoluteIndex();
			for (DoorUnit d : getElementCollections()) {
				if (d.contains(absIndex)) {
					d.activate(active);
					return active ? 1 : 0;
				}
			}
		}
		return active ? 1 : 0;
	}

	@Override
	public void updateActivationTypes(ShortOpenHashSet typesThatNeedActivation) {
		if (getSegmentController().isOnServer()) {
			typesThatNeedActivation.addAll(ElementKeyMap.doorTypes);
		}
	}

	@Override
	public float getSensorValue(SegmentPiece connected) {
		return connected.isActive() ? 1.0f : 0.0f;
	}

	@Override
	public boolean isHandlingActivationForType(short type) {
		return ElementKeyMap.isDoor(type);
	}
}
