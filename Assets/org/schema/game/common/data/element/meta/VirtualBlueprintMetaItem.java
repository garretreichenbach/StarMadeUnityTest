package org.schema.game.common.data.element.meta;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.FastMath;
import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ManagerModuleCollection;
import org.schema.game.common.controller.elements.ShipyardManagerContainerInterface;
import org.schema.game.common.controller.elements.shipyard.ShipyardCollectionManager;
import org.schema.game.common.controller.elements.shipyard.ShipyardCollectionManager.ShipyardCommandType;
import org.schema.game.common.controller.elements.shipyard.ShipyardElementManager;
import org.schema.game.common.controller.elements.shipyard.ShipyardUnit;
import org.schema.game.common.controller.io.SegmentDataFileUtils;
import org.schema.game.common.data.element.meta.MetaObjectManager.MetaObjectType;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButton;
import org.schema.schine.input.InputState;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.sound.controller.AudioController;

public class VirtualBlueprintMetaItem extends MetaObject {

	public static final int MAX_LENGTH = 50;

	public String virtualName = "no name";

	public String UID = "ERROR_undef";

	long lastLoadAction;

	public VirtualBlueprintMetaItem(int id) {
		super(id);
	}

	@Override
	public void deserialize(DataInputStream stream) throws IOException {
		UID = stream.readUTF();
		virtualName = stream.readUTF();
	}

	@Override
	public void fromTag(Tag tag) {
		Tag[] t = (Tag[]) tag.getValue();
		UID = (String) t[0].getValue();
		virtualName = (String) t[1].getValue();
	}

	@Override
	public Tag getBytesTag() {
		return new Tag(Type.STRUCT, null, new Tag[] { new Tag(Type.STRING, null, UID), new Tag(Type.STRING, null, virtualName), FinishTag.INST });
	}

	@Override
	public DialogInput getInfoDialog(GameClientState state, AbstractControlManager parent, Inventory openedFrom) {
		PlayerGameOkCancelInput p = new PlayerGameOkCancelInput("METAINFO_" + getObjectBlockID(), state, Lng.str("Info"), Lng.str("This is a ship design. Designs are used in shipyards to load a virtual projection on a ship. The projection can be edited in creative mode.\nDesigns can then be made into a real ship by providing the resources.")) {

			@Override
			public void onDeactivate() {
			}

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void pressedOK() {
				deactivate();
			}
		};
		p.getInputPanel().setCancelButton(false);
		return p;
	}

	@Override
	public DialogInput getEditDialog(GameClientState state, final AbstractControlManager parent, Inventory openedFrom) {
		PlayerGameTextInput p = new PlayerGameTextInput("ENTER_NAME", state, MAX_LENGTH, Lng.str("Virtual Design Name"), Lng.str("Enter a name for this design!")) {

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
				return null;
			}

			@Override
			public void onFailedTextCheck(String msg) {
			}

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void onDeactivate() {
				if (parent != null) {
					parent.suspend(false);
				}
			}

			@Override
			public boolean onInput(String entry) {
				VirtualBlueprintMetaItem l = new VirtualBlueprintMetaItem(getId());
				l.virtualName = entry != null ? entry : "";
				l.UID = UID;
				try {
					getState().getMetaObjectManager().modifyRequest(getState().getController().getClientChannel().getNetworkObject(), l);
				} catch (MetaItemModifyPermissionException e) {
					getState().getController().popupAlertTextMessage(Lng.str("Operation not permitted:\nAccess denied!"), 0);
				}
				return true;
			}
		};
		return p;
	}

	@Override
	public void drawPossibleOverlay(GUIOverlay reload, Inventory inventory) {
		if (isInventoryLocked(inventory)) {
			int base = 2;
			float max = 8;
			float percent = 0;
			percent = 0.0f;
			int sprite = base;
			sprite = (int) FastMath.floor(FastMath.clamp(base + percent * max, base, base + max));
			reload.setSpriteSubIndex(sprite);
			reload.draw();
		}
	}

	@Override
	public String getName() {
		return Lng.str("Design");
	}

	@Override
	public boolean drawUsingReloadIcon() {
		return true;
	}

	@Override
	public MetaObjectType getObjectBlockType() {
		return MetaObjectType.VIRTUAL_BLUEPRINT;
	}

	@Override
	public int getPermission() {
		return MODIFIABLE_CLIENT;
	}

	@Override
	public boolean isValidObject() {
		return true;
	}

	@Override
	public void serialize(DataOutputStream stream) throws IOException {
		stream.writeUTF(UID);
		stream.writeUTF(virtualName);
	}

	protected GUIHorizontalButton getLoadDesignButton(final GameClientState state, final Inventory inventory) {
		return new GUIHorizontalButton(state, HButtonType.BUTTON_BLUE_MEDIUM, Lng.str("LOAD"), new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					ShipyardCollectionManager y = getShipyardFromInventory(inventory);
					if (y != null) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(946);
						lastLoadAction = System.currentTimeMillis();
						y.sendShipyardCommandToServer(state.getPlayer().getFactionId(), ShipyardCommandType.LOAD_DESIGN, getId());
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
				ShipyardCollectionManager y = getShipyardFromInventory(inventory);
				return y != null && y.getCurrentDocked() == null && y.getCurrentDesignObject() == null && y.isCommandUsable(ShipyardCommandType.LOAD_DESIGN) && System.currentTimeMillis() - lastLoadAction > 3000;
			}
		});
	}

	protected GUIHorizontalButton getUnloadDesignButton(final GameClientState state, final Inventory inventory) {
		return new GUIHorizontalButton(state, HButtonType.BUTTON_BLUE_MEDIUM, Lng.str("UNLOAD"), new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(947);
					ShipyardCollectionManager y = getShipyardFromInventory(inventory);
					if (y != null) {
						y.sendShipyardCommandToServer(state.getPlayer().getFactionId(), ShipyardCommandType.UNLOAD_DESIGN);
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
				ShipyardCollectionManager y = getShipyardFromInventory(inventory);
				return y != null && y.getCurrentDocked() != null && y.getCurrentDocked().isVirtualBlueprint() && y.getCurrentDesignObject() == VirtualBlueprintMetaItem.this && y.isCommandUsable(ShipyardCommandType.UNLOAD_DESIGN);
			}
		});
	}

	@Override
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
								inventory.removeMetaItem(VirtualBlueprintMetaItem.this);
							}
							deactivate();
						}
					};
					confirm.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(948);
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

	@Override
	protected void onDelete(boolean server, StateInterface state) {
		if (server) {
			Sendable s = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(UID);
			if (s != null && s instanceof SegmentController) {
				s.markForPermanentDelete(true);
				s.setMarkedForDeleteVolatile(true);
			} else {
				try {
					SegmentDataFileUtils.deleteEntitiyFileAndAllData(UID);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			System.err.println("[SERVER][DESIGN][REMOVE] deleting Design UID '" + UID + "'");
		}
	}

	@Override
	protected GUIHorizontalButton[] getButtons(GameClientState state, Inventory inventory) {
		return new GUIHorizontalButton[] { getInfoButton(state, inventory), getEditButton(state, inventory), getDeleteButton(state, inventory), getLoadDesignButton(state, inventory), getUnloadDesignButton(state, inventory) };
	}

	public ShipyardCollectionManager getShipyardFromInventory(Inventory inventory) {
		if (inventory.getInventoryHolder() != null && inventory.getParameter() != Long.MIN_VALUE && inventory.getInventoryHolder() instanceof ShipyardManagerContainerInterface) {
			ManagerModuleCollection<ShipyardUnit, ShipyardCollectionManager, ShipyardElementManager> shipyard = ((ShipyardManagerContainerInterface) inventory.getInventoryHolder()).getShipyard();
			ShipyardCollectionManager shipyardCollectionManager = shipyard.getCollectionManagersMap().get(inventory.getParameter());
			return shipyardCollectionManager;
		}
		return null;
	}

	@Override
	public boolean isInventoryLocked(Inventory inventory) {
		ShipyardCollectionManager shipyardCollectionManager = getShipyardFromInventory(inventory);
		if (shipyardCollectionManager != null) {
			VirtualBlueprintMetaItem currentDesign = shipyardCollectionManager.getCurrentDesignObject();
			return currentDesign == this;
		}
		return System.currentTimeMillis() - lastLoadAction < 3000;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Lng.str("Ship Design (%s)\n(right click to view)", virtualName);
	}

	@Override
	public boolean isDrawnOverlayInHotbar() {
		return true;
	}

	@Override
	public boolean isDrawnOverlayInInventory() {
		return true;
	}

	@Override
	public boolean equalsObject(MetaObject other) {
		return super.equalsTypeAndSubId(other) && UID.equals(((VirtualBlueprintMetaItem) other).UID) && virtualName.equals(((VirtualBlueprintMetaItem) other).virtualName);
	}
}
