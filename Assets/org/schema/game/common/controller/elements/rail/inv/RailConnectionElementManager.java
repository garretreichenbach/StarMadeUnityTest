package org.schema.game.common.controller.elements.rail.inv;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ActivateValueEntry;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.UsableControllableElementManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.sound.controller.AudioController;

public class RailConnectionElementManager extends UsableControllableElementManager<RailConnectionUnit, RailConnectionCollectionManager, RailConnectionElementManager> {

	public static boolean debug = false;

	private final SegmentPiece tmp = new SegmentPiece();

	private Vector3i controlledFromOrig = new Vector3i();

	private Vector3i controlledFrom = new Vector3i();

	public RailConnectionElementManager(final SegmentController segmentController) {
		super(Element.TYPE_RAIL_INV, Element.TYPE_ALL, segmentController);
	}

	@Override
	public void addConnectionIfNecessary(Vector3i controller, short fromType, Vector3i controlled, short controlledType) {
		assert (ElementKeyMap.getInfo(controlledType).isRailDockable()) : ElementKeyMap.toString(fromType) + " " + controller + " -> " + controlled + "; " + ElementKeyMap.toString(controlledType);
		super.addConnectionIfNecessary(controller, fromType, controlled, controlledType);
	}

	@Override
	public ControllerManagerGUI getGUIUnitValues(RailConnectionUnit firingUnit, RailConnectionCollectionManager col, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return ControllerManagerGUI.create((GameClientState) getState(), "Rail Unit", firingUnit);
	}

	@Override
	protected String getTag() {
		return "railinventory";
	}

	@Override
	public RailConnectionCollectionManager getNewCollectionManager(SegmentPiece position, Class<RailConnectionCollectionManager> clazz) {
		return new RailConnectionCollectionManager(position, getSegmentController(), this);
	}

	@Override
	public String getManagerName() {
		return Lng.str("Rail Collective");
	}

	@Override
	public GUIKeyValueEntry[] getGUIElementCollectionValues() {
		return new GUIKeyValueEntry[] { new ActivateValueEntry(Lng.str("Reset all Turrets")) {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(902);
					getSegmentController().railController.sendClientTurretResetRequest();
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		}, new ActivateValueEntry(Lng.str("Undock all")) {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					if (!getSegmentController().isVirtualBlueprint()) {
						PlayerGameOkCancelInput check = new PlayerGameOkCancelInput("CONFIRM", (GameClientState) getState(), Lng.str("Confirm"), Lng.str("Do you really want to do this?")) {

							@Override
							public boolean isOccluded() {
								return false;
							}

							@Override
							public void onDeactivate() {
							}

							@Override
							public void pressedOK() {
								deactivate();
								getSegmentController().railController.undockAllClient();
							}
						};
						check.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(903);
					} else {
						((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Can't undock ships from a design"));
					}
				}
			}
		}, new ActivateValueEntry(Lng.str("Activate all AI Turrets")) {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(904);
					getSegmentController().railController.getRoot().railController.activateAllAIClient(true, true, false);
				}
			}
		}, new ActivateValueEntry(Lng.str("Deactivate all AI Turrets")) {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(905);
					getSegmentController().railController.getRoot().railController.activateAllAIClient(false, true, false);
				}
			}
		}, new ActivateValueEntry(Lng.str("Activate all docked AI")) {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(906);
					getSegmentController().railController.getRoot().railController.activateAllAIClient(true, false, true);
				}
			}
		}, new ActivateValueEntry(Lng.str("Deactivate all docked AI")) {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(907);
					getSegmentController().railController.getRoot().railController.activateAllAIClient(false, false, true);
				}
			}
		} };
	}

	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {
	}
}
