package org.schema.game.common.controller.elements.rail.speed;

import java.util.List;

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
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.util.FastCopyLongOpenHashSet;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

public class RailSpeedElementManager extends UsableControllableElementManager<RailSpeedUnit, RailSpeedCollectionManager, RailSpeedElementManager> {

	public final static String TAG_ID = "RSP";

	public static boolean debug = false;

	private final SegmentPiece tmp = new SegmentPiece();

	private Vector3i controlledFromOrig = new Vector3i();

	private Vector3i controlledFrom = new Vector3i();

	public RailSpeedElementManager(final SegmentController segmentController) {
		super(ElementKeyMap.RAIL_RAIL_SPEED_CONTROLLER, Element.TYPE_ALL, segmentController);
	}

	@Override
	public void addConnectionIfNecessary(Vector3i controller, short fromType, Vector3i controlled, short controlledType) {
		assert (ElementKeyMap.getInfo(controlledType).isRailDockable()) : ElementKeyMap.toString(fromType) + " " + controller + " -> " + controlled + "; " + ElementKeyMap.toString(controlledType);
		super.addConnectionIfNecessary(controller, fromType, controlled, controlledType);
	}

	public float getRailSpeedForTrack(long blockIndex4) {
		int size = getCollectionManagers().size();
		long normalAbsPosIndex = ElementCollection.getPosIndexFrom4(blockIndex4);
		for (int i = 0; i < size; i++) {
			RailSpeedCollectionManager railSpeedCollectionManager = getCollectionManagers().get(i);
			if (!railSpeedCollectionManager.checkAllConnections()) {
				// System.err.println("NOT CHECK ALL");
				return 0.0f;
			}
			List<RailSpeedUnit> elementCollections = railSpeedCollectionManager.getElementCollections();
			railSpeedCollectionManager.rawCollection.size();
			int checkSize = 0;
			for (int j = 0; j < elementCollections.size(); j++) {
				RailSpeedUnit railSpeedUnit = elementCollections.get(j);
				checkSize += railSpeedUnit.size();
				if (railSpeedUnit.contains(normalAbsPosIndex)) {
					Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> mmap = getSegmentController().getControlElementMap().getControllingMap().get(railSpeedCollectionManager.getControllerElement().getAbsoluteIndex());
					FastCopyLongOpenHashSet acts;
					// System.err.println("FOUND UNIT!!!!!! "+mmap);
					if (mmap != null && (acts = mmap.get(ElementKeyMap.ACTIVAION_BLOCK_ID)) != null && !acts.isEmpty()) {
						int activeBlocks = 0;
						for (long l : acts) {
							SegmentPiece pointUnsave = getSegmentController().getSegmentBuffer().getPointUnsave(l, tmp);
							if (pointUnsave == null) {
								return 0.0f;
							}
							if (pointUnsave.isActive()) {
								activeBlocks++;
							}
						}
						// System.err.println("ACT: "+activeBlocks +" / "+acts.size());
						return (float) activeBlocks / (float) acts.size();
					} else {
						return 0.5f;
					}
				}
			}
		}
		return 0.5f;
	}

	@Override
	public ControllerManagerGUI getGUIUnitValues(RailSpeedUnit firingUnit, RailSpeedCollectionManager col, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return ControllerManagerGUI.create((GameClientState) getState(), "Activation Unit", firingUnit);
	}

	@Override
	protected String getTag() {
		return "railspeed";
	}

	@Override
	public RailSpeedCollectionManager getNewCollectionManager(SegmentPiece position, Class<RailSpeedCollectionManager> clazz) {
		return new RailSpeedCollectionManager(position, getSegmentController(), this);
	}

	@Override
	public String getManagerName() {
		return Lng.str("Rail System Collective");
	}

	@Override
	public GUIKeyValueEntry[] getGUIElementCollectionValues() {
		return new GUIKeyValueEntry[] { new ActivateValueEntry(Lng.str("Reset all Turrets")) {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(908);
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
								for (int i = 0; i < getCollectionManagers().size(); i++) {
									getSegmentController().railController.undockAllClient();
								}
							}
						};
						check.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(909);
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
					AudioController.fireAudioEventID(910);
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
					AudioController.fireAudioEventID(911);
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
					AudioController.fireAudioEventID(912);
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
					AudioController.fireAudioEventID(913);
					getSegmentController().railController.getRoot().railController.activateAllAIClient(false, false, true);
				}
			}
		} };
	}

	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {
	}
}
