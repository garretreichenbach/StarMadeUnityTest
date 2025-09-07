package org.schema.game.common.data.element.meta;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.common.data.element.meta.MetaObjectManager.MetaObjectType;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContextPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButton;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.StateInterface;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.sound.controller.AudioController;

public abstract class MetaObject {

	public static final int NO_EDIT_PERMISSION = 0;

	public static final int MODIFIABLE_CLIENT = 1;

	public static final long SALVAGE_TOOL_ID = -100100;

	private int id;

	public MetaObject(int id) {
		this.id = id;
	}

	public boolean clientMayModify() {
		return (getPermission() & MODIFIABLE_CLIENT) == MODIFIABLE_CLIENT;
	}

	public abstract void deserialize(DataInputStream stream) throws IOException;

	public abstract void fromTag(Tag tag);

	public abstract Tag getBytesTag();

	public abstract DialogInput getEditDialog(GameClientState state, final AbstractControlManager parent, Inventory openedFrom);

	public DialogInput getInfoDialog(GameClientState state, final AbstractControlManager parent, Inventory openedFrom) {
		return getEditDialog(state, parent, openedFrom);
	}

	public short[] getSubTypes() {
		throw new NullPointerException("No subtypes for " + this);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	public abstract MetaObjectType getObjectBlockType();

	public short getObjectBlockID() {
		return getObjectBlockType().type;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		return id == ((MetaObject) obj).id;
	}

	public abstract int getPermission();

	public short getSubObjectId() {
		return -1;
	}

	public abstract boolean isValidObject();

	public abstract void serialize(DataOutputStream stream) throws IOException;

	public int getExtraBuildIconIndex() {
		return 0;
	}

	public void drawPossibleOverlay(GUIOverlay reload, Inventory inventory) {
	}

	public float hasZoomFunction() {
		return -1;
	}

	public void handleKeyPress(AbstractCharacter<?> playerCharacter, ControllerStateUnit unit, Timer timer) {
	}

	public void handleKeyEvent(AbstractCharacter<?> playerCharacter, ControllerStateUnit unit, KeyboardMappings mapping) {
	}

	public boolean drawUsingReloadIcon() {
		return false;
	}

	protected GUIHorizontalButton getDeleteButton(final GameClientState state, final Inventory inventory) {
		return new GUIHorizontalButton(state, HButtonType.BUTTON_RED_MEDIUM, "DISPOSE", new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					PlayerGameOkCancelInput confirm = new PlayerGameOkCancelInput("CONFIRM", state, Lng.str("Confirm"), Lng.str("Do you really want to dispose this item?\nThis cannot be undone!")) {

						@Override
						public boolean isOccluded() {
							return false;
						}

						@Override
						public void onDeactivate() {
						}

						@Override
						public void pressedOK() {
							int slot = inventory.getSlotFromMetaId(getId());
							if (slot >= 0) {
								inventory.removeMetaItem(MetaObject.this);
							}
							deactivate();
						}
					};
					confirm.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(943);
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		}, null, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return canDisposeItem(inventory);
			}
		});
	}

	protected boolean canDisposeItem(Inventory inventory) {
		return !isInventoryLocked(inventory);
	}

	public boolean isInventoryLocked(Inventory inventory) {
		return false;
	}

	protected GUIHorizontalButton getInfoButton(final GameClientState state, final Inventory inventory) {
		return new GUIHorizontalButton(state, HButtonType.BUTTON_BLUE_MEDIUM, Lng.str("INFO"), new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					DialogInput editDialog = getInfoDialog(state, (state).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager(), inventory);
					if (editDialog == null) {
						(state.getController()).popupAlertTextMessage(Lng.str("Object has no info (yet)..."), 0);
					} else {
						editDialog.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(944);
					}
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		}, null, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return true;
			}
		});
	}

	protected GUIHorizontalButton getEditButton(final GameClientState state, final Inventory inventory) {
		return getCustomEditButton(state, inventory, Lng.str("EDIT"));
	}

	protected GUIHorizontalButton getCustomEditButton(final GameClientState state, final Inventory inventory, String name) {
		return new GUIHorizontalButton(state, HButtonType.BUTTON_BLUE_MEDIUM, name, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					(state).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().suspend(true);
					DialogInput editDialog = getEditDialog(state, (state).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager(), inventory);
					if (editDialog == null) {
						(state.getController()).popupAlertTextMessage(Lng.str("Object has no editing option (yet)..."), 0);
					} else {
						editDialog.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(945);
					}
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		}, null, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return clientMayModify() && !isInventoryLocked(inventory);
			}
		});
	}

	protected GUIHorizontalButton[] getButtons(GameClientState state, Inventory inventory) {
		return new GUIHorizontalButton[] { getInfoButton(state, inventory), getEditButton(state, inventory), getDeleteButton(state, inventory) };
	}

	protected void onDelete(boolean server, StateInterface state) {
	}

	public abstract String getName();

	public GUIContextPane createContextPane(GameClientState state, Inventory inventory, GUIElement parent) {
		GUIHorizontalButton[] buttons = getButtons(state, inventory);
		GUIContextPane p = new GUIContextPane(state, 100, (buttons.length) * 25);
		p.onInit();
		GUIHorizontalButtonTablePane buttonTable = new GUIHorizontalButtonTablePane(state, 1, buttons.length, p);
		buttonTable.onInit();
		for (int i = 0; i < buttons.length; i++) {
			buttons[i].activeInterface = buttonTable.activeInterface;
			buttonTable.addButton(buttons[i], 0, i);
		}
		p.attach(buttonTable);
		System.err.println("[CLIENT][GUI] contect pane for meta item " + this);
		return p;
	}

	public boolean canClientRemove() {
		return true;
	}

	public boolean isDrawnOverlayInHotbar() {
		return true;
	}

	public boolean isDrawnOverlayInInventory() {
		return false;
	}

	public boolean update(Timer timer) {
		return false;
	}

	public double getVolume() {
		return 1;
	}

	public boolean equalsTypeAndSubId(MetaObject other) {
		return getObjectBlockType() == other.getObjectBlockType() && getSubObjectId() == other.getSubObjectId();
	}

	public abstract boolean equalsObject(MetaObject other);
}
