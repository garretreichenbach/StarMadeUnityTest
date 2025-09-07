package org.schema.game.client.controller.manager.ingame;

import java.util.Map.Entry;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.PlayerTextInputBar;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.controller.manager.ingame.shop.DropCreditsDialog;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.inventory.InventoryIconsNew;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.camera.CameraMouseState;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.DropDownCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIDropDownList;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputType;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.sound.controller.AudioController;

public class InventoryControllerManager extends AbstractControlManager implements GUICallback {

	public PlayerTextInputBar playerTextInputBar;

	private Inventory secondInventory;

	private boolean searchActive;

	private boolean inSearchbar;

	private SegmentController singleShip;

	private GUIDropDownList otherInventories;

	public InventoryControllerManager(GameClientState state) {
		super(state);
	}

	@Override
	public void callback(GUIElement callingGui, MouseEvent event) {
	}

	public PlayerInteractionControlManager getInteractionManager() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
	}

	public Inventory getSecondInventory() {
		return secondInventory;
	}

	public void setSecondInventory(Inventory inventory) {
		this.secondInventory = inventory;
		if (inventory == null) {
			getState().getController().getInputController().setDragging(null);
		}
	}

	@Override
	public GameClientState getState() {
		return super.getState();
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		int size = getState().getController().getPlayerInputs().size();
		if (size > 0) {
			// only the last in list is active
			getState().getController().getPlayerInputs().get(size - 1).handleKeyEvent(e);
		// return;
		}
		if (e.isInputType(InputType.MOUSE) && searchActive) {
			searchActive = false;
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.manager.AbstractControlManager#onSwitch(boolean)
	 */
	@Override
	public void onSwitch(boolean active) {
		CameraMouseState.setGrabbed(!active);
		if (active) {
			SimpleTransformableSendableObject po = getState().getCurrentPlayerObject();
			if (po != null && po instanceof SegmentController && po instanceof ManagedSegmentController<?>) {
				singleShip = (SegmentController) getState().getCurrentPlayerObject();
			} else {
				singleShip = null;
			}
			int width = 140;
			int height = 28;
			if (singleShip != null) {
				ManagerContainer<?> managerContainer = ((ManagedSegmentController<?>) singleShip).getManagerContainer();
				GUIElement[] elements = new GUIElement[managerContainer.getInventories().size() + 1];
				GUITextOverlay oP = new GUITextOverlay(FontSize.MEDIUM_15, getState());
				oP.setTextSimple(new Object() {

					@Override
					public String toString() {
						return "Personal " + getState().getPlayer().getInventory().getVolumeString();
					}
				});
				GUIAnchor aP = new GUIAnchor(getState(), width, height);
				oP.getPos().y += 3;
				aP.attach(oP);
				elements[0] = aP;
				aP.setUserPointer(getState().getPlayer().getInventory(null));
				int i = 1;
				for (final Entry<Long, Inventory> e : managerContainer.getInventories().entrySet()) {
					GUITextOverlay o = new GUITextOverlay(FontSize.MEDIUM_15, getState());
					final Vector3i pos = new Vector3i();
					ElementCollection.getPosFromIndex(e.getKey(), pos);
					o.setTextSimple(new Object() {

						@Override
						public String toString() {
							return "Stash " + pos.toStringPure() + "; " + e.getValue().getVolumeString();
						}
					});
					GUIAnchor a = new GUIAnchor(getState(), width, height);
					o.getPos().y += 3;
					a.attach(o);
					a.setUserPointer(e.getValue());
					elements[i] = a;
					i++;
				}
				otherInventories = new GUIDropDownList(getState(), width, height, 400, new DropDownCallback() {

					private boolean first = true;

					@Override
					public void onSelectionChanged(GUIListElement element) {
						if (first) {
							first = false;
							return;
						}
						Inventory inv = (Inventory) element.getContent().getUserPointer();
						if (getState().getPlayer().getInventory(null) != inv) {
							System.err.println("[CLIENT][INVDROPDOWN] CHANGING TO " + inv);
							getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().inventoryAction(inv, true, true);
						} else {
							// own inventory selected
							getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().inventoryAction(null, true, true);
						}
					}
				}, elements);
				if (secondInventory != null) {
					for (GUIListElement e : otherInventories.getList()) {
						if (e.getContent().getUserPointer() == secondInventory) {
							otherInventories.setSelectedElement(e);
							break;
						}
					}
				}
				otherInventories.setPos(377, -(height + 2), 0);
			}
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(142);
			notifyObservers();
		} else {
			inSearchbar = false;
			searchActive = false;
			if (playerTextInputBar != null) {
				playerTextInputBar.reset();
				playerTextInputBar.deactivate();
			}
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DEACTIVATE)*/
			AudioController.fireAudioEventID(141);
		}
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getShipExternalFlightController().suspend(active);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getSegmentBuildController().suspend(active);
		// getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager()
		// .getInShipControlManager().getShipControlManager().getShipControllerManagerManager().suspend(active);
		super.onSwitch(active);
	}

	@Override
	public void update(Timer timer) {
		CameraMouseState.setGrabbed(false);
		getInteractionManager().suspend(true);
		if (secondInventory != null && secondInventory.getLocalInventoryType() != Inventory.PLAYER_INVENTORY) {
			if (secondInventory.getParameter() != Long.MIN_VALUE && secondInventory.getInventoryHolder() instanceof ManagerContainer<?>) {
				ManagerContainer<?> m = (ManagerContainer<?>) secondInventory.getInventoryHolder();
				if (!m.getInventories().containsKey((secondInventory.getParameterIndex()))) {
					setDelayedActive(false);
					getState().getController().popupAlertTextMessage(Lng.str("Chest not found!\nIt has been removed."), 0);
				} else {
				// System.err.println("[CLIENT] still contains inventory");
				}
			}
		}
	}

	/**
	 * @return the inSearchbar
	 */
	public boolean isInSearchbar() {
		return inSearchbar;
	}

	/**
	 * @param inSearchbar the inSearchbar to set
	 */
	public void setInSearchbar(boolean inSearchbar) {
		this.inSearchbar = inSearchbar;
	}

	public boolean isSearchActive() {
		return searchActive;
	}

	/**
	 * @param searchActive the searchActive to set
	 */
	public void setSearchActive(boolean searchActive) {
		this.searchActive = searchActive;
	}

	public void drawDropDown(InventoryIconsNew upperInventoryIcons, InventoryIconsNew lowerInventoryIcons, GUIScrollablePanel upper, GUIScrollablePanel downer) {
		if (singleShip != null) {
			upper.setScrollingListener(() -> !otherInventories.isExpanded());
			downer.setScrollingListener(() -> !otherInventories.isExpanded());
			if (otherInventories.isExpanded()) {
				upperInventoryIcons.setInside(false);
				lowerInventoryIcons.setInside(false);
				upper.setInside(false);
				downer.setInside(false);
			}
			otherInventories.draw();
		}
	}

	public void openDropCreditsDialog(int startQuantity) {
		(new DropCreditsDialog(getState(), startQuantity)).activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(143);
		suspend(true);
	}

	public void switchSearchActive() {
		searchActive = !searchActive;
	}
}
