package org.schema.game.client.view.gui.structurecontrol.structurenew;

import org.schema.common.util.StringTools;
import org.schema.game.client.data.CollectionManagerChangeListener;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class StructurePanelNew extends GUIElement implements GUIActiveInterface, CollectionManagerChangeListener {

	public GUIMainWindow structurePanel;
	private GUIContentPane currentEntityTab;
	private boolean init;
	private boolean flagFactionTabRecreate;
	private StructureScrollableListNew structureList;
	private SegmentController lastControl;
	private long lastControllerCheck;

	public StructurePanelNew(InputState state) {
		super(state);
	}

	@Override
	public void cleanUp() {
		getState().getController().removeCollectionManagerChangeListener(this);
	}

	@Override
	public void draw() {
		if(!init) {
			onInit();
		}
		if(System.currentTimeMillis() - lastControllerCheck > 1000) {
			lastControllerCheck = System.currentTimeMillis();
			if(getState().getCurrentPlayerObject() instanceof SegmentController controller) {
				if(controller != lastControl) {
					lastControl = controller;
					flagFactionTabRecreate = true;
				}
			}
		}
		if(flagFactionTabRecreate) {
			recreateTabs();
			flagFactionTabRecreate = false;
		}
		structurePanel.draw();
	}

	@Override
	public void onInit() {
		if(structurePanel != null) {
			structurePanel.cleanUp();
		}
		getState().getController().addCollectionManagerChangeListener(this);
		structurePanel = new GUIMainWindow(getState(), 750, 550, "StructurePanelNew");
		structurePanel.onInit();
		structurePanel.setCloseCallback(new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !getState().getController().getPlayerInputs().isEmpty();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					AudioController.fireAudioEventID(696);
					getState().getWorldDrawer().getGuiDrawer().getPlayerPanel().deactivateAll();
				}
			}
		});
		structurePanel.orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_VERTICAL_MIDDLE);
		recreateTabs();
		init = true;
	}

	public void recreateTabs() {
		Object beforeTab = null;
		if(structurePanel.getSelectedTab() < structurePanel.getTabs().size()) {
			beforeTab = structurePanel.getTabs().get(structurePanel.getSelectedTab()).getTabName();
		}
		structurePanel.clearTabs();
		currentEntityTab = structurePanel.addTab(Lng.str("STRUCTURE"));
		createCurrentEntityPane();
		structurePanel.activeInterface = this;
		if(beforeTab != null) {
			for(int i = 0; i < structurePanel.getTabs().size(); i++) {
				if(structurePanel.getTabs().get(i).getTabName().equals(beforeTab)) {
					structurePanel.setSelectedTab(i);
					break;
				}
			}
		}
	}

	@Override
	public void update(Timer timer) {
	}

	public void createCurrentEntityPane() {
		if(structureList != null) structureList.cleanUp();
		currentEntityTab.setTextBoxHeightLast(UIScale.getUIScale().scale(28));
		GUIHorizontalButtonTablePane p = new GUIHorizontalButtonTablePane(getState(), 1, 1, null, currentEntityTab.getContent(0));
		p.onInit();
		p.activeInterface = this;
		p.addButton(0, 0, new Object() {

			@Override
			public String toString() {
				SimpleTransformableSendableObject currentPlayerObject = getState().getCurrentPlayerObject();
				if(currentPlayerObject instanceof SegmentController) {
					if(((SegmentController) currentPlayerObject).getHpController().isRebooting()) {
						long rebootTimeLeftMS = ((SegmentController) currentPlayerObject).getHpController().getRebootTimeLeftMS();
						return Lng.str("REBOOTING (%s)", StringTools.formatTimeFromMS(rebootTimeLeftMS));
					} else {
						if(currentPlayerObject instanceof SpaceStation) {
							if(((SegmentController) currentPlayerObject).getHpController().getHpPercent() < 1.0d) {
								return Lng.str("REBOOT SYSTEMS");
							} else {
								// if (((SegmentController) currentPlayerObject).getHpController().getMaxArmorHp() > 0 && ((SegmentController) currentPlayerObject).getHpController().getArmorHpPercent() < 1d) {
								// return Lng.str("REPAIR ARMOR");
								// }
								return Lng.str("ALL SYSTEMS WORKING FINE");
							}
						} else {
							if(((SegmentController) currentPlayerObject).getHpController().getHpPercent() < 1.0d) {
								return Lng.str("REBOOT SYSTEMS");
							} else {
								return Lng.str("ALL SYSTEMS WORKING FINE");
							}
						}
					}
				} else {
					return Lng.str("invalid object");
				}
			}
		}, GUIHorizontalArea.HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					SimpleTransformableSendableObject<?> currentPlayerObject = getState().getCurrentPlayerObject();
					if(currentPlayerObject instanceof SegmentController) {
						getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().popupShipRebootDialog((SegmentController) currentPlayerObject);
					}
				}
			}

			@Override
			public boolean isOccluded() {
				return !isActive();
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				SimpleTransformableSendableObject<?> currentPlayerObject = getState().getCurrentPlayerObject();
				if(currentPlayerObject instanceof SegmentController) {
					return !((SegmentController) currentPlayerObject).getHpController().isRebooting() && (((SegmentController) currentPlayerObject).getHpController().getHpPercent() < 1.0d);
				} else {
					return false;
				}
			}
		});
		currentEntityTab.getContent(0).attach(p);
		SimpleTransformableSendableObject<?> currentPlayerObject = getState().getCurrentPlayerObject();
		if(currentPlayerObject instanceof ManagedSegmentController<?> managedSegmentController) {
			currentEntityTab.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
			structureList = new StructureScrollableListNew(getState(), 10, 10, currentEntityTab.getContent(1), managedSegmentController.getManagerContainer());
			structureList.onInit();
			currentEntityTab.getContent(1).attach(structureList);
		}
	}

	public PlayerState getOwnPlayer() {
		return getState().getPlayer();
	}

	public Faction getOwnFaction() {
		return getState().getFactionManager().getFaction(getOwnPlayer().getFactionId());
	}

	@Override
	public float getHeight() {
		return structurePanel.getHeight();
	}

	@Override
	public GameClientState getState() {
		return ((GameClientState) super.getState());
	}

	@Override
	public float getWidth() {
		return structurePanel.getWidth();
	}

	@Override
	public boolean isActive() {
		return getState().getController().getPlayerInputs().isEmpty();
	}

	public void reset() {
		structurePanel.reset();
	}

	@Override
	public void onChange(ElementCollectionManager<?, ?, ?> col) {
		if(structureList != null) structureList.notifyFinishedChangingCollection(col);
	}
}
