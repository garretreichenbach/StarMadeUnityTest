package org.schema.game.common.controller.elements.dockingBlock;

import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ActivateValueEntry;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.BlockActivationListenerInterface;
import org.schema.game.common.controller.elements.BlockMetaDataDummy;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.ElementChangeListenerInterface;
import org.schema.game.common.controller.elements.TagModuleUsableInterface;
import org.schema.game.common.controller.elements.UsableControllableElementManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementDocking;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.sound.controller.AudioController;

public abstract class DockingBlockElementManager<E extends DockingBlockUnit<E, CM, EM>, CM extends DockingBlockCollectionManager<E, CM, EM>, EM extends DockingBlockElementManager<E, CM, EM>> extends UsableControllableElementManager<E, CM, EM> implements ElementChangeListenerInterface, TagModuleUsableInterface, DockingElementManagerInterface, BlockActivationListenerInterface {

	public final static String TAG_ID = "A";

	public DockingBlockElementManager(SegmentController segmentController, short dockingId, short dockEnhancerId) {
		super(dockingId, dockEnhancerId, segmentController);
	}

	@Override
	public String getTagId() {
		return TAG_ID;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.UsableElementManager#getGUIUnitValues(org.schema.game.common.data.element.ElementCollection, org.schema.game.common.controller.elements.ElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager)
	 */
	@Override
	public ControllerManagerGUI getGUIUnitValues(E firingUnit, CM col, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		// not used
		return null;
	}

	// 
	// /* (non-Javadoc)
	// * @see org.schema.game.common.controller.elements.UsableElementManager#handleSingleActivation(org.schema.game.common.data.SegmentPiece)
	// */
	// @Override
	// public void handleSingleActivation(SegmentPiece controller) {
	// if(getSegmentController().isOnServer()){
	// for(int i = 0; i < getCollectionManagers().size(); i++){
	// DockingBlockCollectionManager dockingBlockCollectionManager = getCollectionManagers().get(i);
	// if(controller.equalsPos(dockingBlockCollectionManager.getControllerPos())){
	// for(ElementDocking d : controller.getSegment().getSegmentController().getDockingController().getDockedOnThis()){
	// if(d.to.equals(controller)){
	// d.from.getSegment().getSegmentController().getDockingController().requestDelayedUndock(true);
	// }
	// }
	// }
	// }
	// }
	// }
	@Override
	public GUIKeyValueEntry[] getGUIElementCollectionValues() {
		return new GUIKeyValueEntry[] { new ActivateValueEntry("undock all") {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					PlayerGameOkCancelInput check = new PlayerGameOkCancelInput("CONFIRM", (GameClientState) getState(), "Confirm", Lng.str("Do you really want to do this?")) {

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
								DockingBlockCollectionManager dockingBlockCollectionManager = getCollectionManagers().get(i);
								dockingBlockCollectionManager.clientUndock();
							}
						}
					};
					check.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(883);
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		}, new ActivateValueEntry("activate all AI") {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(884);
					for (int i = 0; i < getCollectionManagers().size(); i++) {
						DockingBlockCollectionManager dockingBlockCollectionManager = getCollectionManagers().get(i);
						dockingBlockCollectionManager.clientActivateAI(true);
					}
				}
			}
		}, new ActivateValueEntry("deactivate all AI") {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(885);
					for (int i = 0; i < getCollectionManagers().size(); i++) {
						DockingBlockCollectionManager dockingBlockCollectionManager = getCollectionManagers().get(i);
						dockingBlockCollectionManager.clientActivateAI(false);
					}
				}
			}
		} };
	}

	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {
	}

	@Override
	public void onAddedAnyElement() {
		for (int i = 0; i < getCollectionManagers().size(); i++) {
			getCollectionManagers().get(i).refreshActive();
		}
	}

	@Override
	public void onRemovedAnyElement() {
		for (int i = 0; i < getCollectionManagers().size(); i++) {
			getCollectionManagers().get(i).refreshActive();
		}
	}

	@Override
	public int onActivate(SegmentPiece piece, boolean oldActive, boolean active) {
		if (getSegmentController().isOnServer() && piece.getType() == ElementKeyMap.TURRET_DOCK_ID || piece.getType() == ElementKeyMap.FIXED_DOCK_ID) {
			long index = piece.getAbsoluteIndex();
			CM cm = getCollectionManagersMap().get(index);
			// System.err.println(getSegmentController()+" "+getSegmentController().getState()+" DOCK ON ACTIVATE SENDING "+active+"; "+cm+"; "+getSegmentController().getDockingController().getDockedOnThis()+"; "+index+"; "+piece+" "+getCollectionManagersMap());
			// try{
			// throw new IllegalArgumentException(getSegmentController()+" "+getSegmentController().getState()+" DOCK ON ACTIVATE SENDING "+active+"; "+cm+"; "+getSegmentController().getDockingController().getDockedOnThis()+"; "+index+"; "+piece+" "+getCollectionManagersMap());
			// }catch (Exception e) {
			// e.printStackTrace();
			// }
			if (cm != null && active) {
				for (ElementDocking d : getSegmentController().getDockingController().getDockedOnThis()) {
					if (d.to.getAbsoluteIndex() == index) {
						d.from.getSegment().getSegmentController().getDockingController().requestDelayedUndock(true);
					}
				}
			}
		}
		return 1;
	}

	@Override
	public BlockMetaDataDummy getDummyInstance() {
		return new DockingMetaDataDummy();
	}
}
