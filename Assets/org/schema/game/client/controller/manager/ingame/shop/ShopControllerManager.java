package org.schema.game.client.controller.manager.ingame.shop;

import java.io.IOException;
import java.util.List;

import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.ShopInterface;
import org.schema.game.common.controller.ShoppingAddOn;
import org.schema.game.common.data.UploadInProgressException;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.BlueprintPlayerHandleRequest;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.network.commands.gamerequests.EntityRequest;
import org.schema.game.network.objects.remote.RemoteBlueprintPlayerRequest;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.data.blueprintnw.BlueprintEntry;
import org.schema.schine.common.InputChecker;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.camera.CameraMouseState;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.network.objects.remote.RemoteStringArray;
import org.schema.schine.sound.controller.AudioController;

public class ShopControllerManager extends AbstractControlManager implements GUICallback {

	private short selectedElementClass = -1;

	private GUIListElement currentlySelectedCatalogElement;

	private long lastClick;

	private GUIListElement currentlySelectedListElement;

	public ShopControllerManager(GameClientState state) {
		super(state);
	}

	@Override
	public void callback(GUIElement callingGui, MouseEvent event) {
		if (event.pressedLeftMouse()) {
			if (callingGui instanceof GUIListElement) {
				GUIListElement en = (GUIListElement) callingGui;
				if ("CATEGORY".equals(en.getContent().getUserPointer())) {
				} else {
					if (currentlySelectedListElement != null) {
						currentlySelectedListElement.getList().deselectAll();
						currentlySelectedListElement.setSelected(false);
					}
					en.setSelected(true);
					selectedElementClass = (Short) en.getContent().getUserPointer();
					currentlySelectedListElement = en;
				}
			}
			if ("upload".equals(callingGui.getUserPointer())) {
				if (System.currentTimeMillis() - this.lastClick < 5000) {
					getState().getController().popupAlertTextMessage(Lng.str("Cannot upload now!\nPlease wait %s seconds.", (System.currentTimeMillis() - this.lastClick) / 1000), 0);
					return;
				}
				ShopControllerManager.this.lastClick = System.currentTimeMillis();
				List<BlueprintEntry> readBluePrints = BluePrintController.active.readBluePrints();
				String description = Lng.str("Please enter in a name for your blueprint!\n\nAvailable:\n%s", readBluePrints);
				PlayerGameTextInput pp = new PlayerGameTextInput("ENTER_NAME", getState(), 50, Lng.str("Blueprint"), description, readBluePrints.isEmpty() ? "" : readBluePrints.get(0).toString()) {

					@Override
					public String[] getCommandPrefixes() {
						return null;
					}

					@Override
					public boolean isOccluded() {
						return getState().getController().getPlayerInputs().indexOf(this) != getState().getController().getPlayerInputs().size() - 1;
					}

					@Override
					public String handleAutoComplete(String s, TextCallback callback, String prefix) {
						return s;
					}

					@Override
					public void onDeactivate() {
						suspend(false);
					}

					@Override
					public void onFailedTextCheck(String msg) {
						setErrorMessage(Lng.str("SHIPNAME INVALID: %s", msg));
					}

					@Override
					public boolean onInput(String entry) {
						try {
							getState().getPlayer().getShipUploadController().upload(entry);
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
							AudioController.fireAudioEventID(190);
							return true;
						} catch (IOException e) {
							e.printStackTrace();
							GLFrame.processErrorDialogException(e, getState());
						} catch (UploadInProgressException e) {
							getState().getController().popupAlertTextMessage(Lng.str("Cannot Upload!\nThere is already\nan upload in progress!"), 0);
						}
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.ERROR)*/
						AudioController.fireAudioEventID(191);
						return false;
					}
				};
				pp.setInputChecker((entry, callback) -> {
					if (EntityRequest.isShipNameValid(entry)) {
						return true;
					} else {
						callback.onFailedTextCheck(Lng.str("Must only contain letters or numbers or (_-)!"));
						return false;
					}
				});
				getState().getController().getPlayerInputs().add(pp);
				suspend(true);
			}
			if ("save_local".equals(callingGui.getUserPointer())) {
			// if (System.currentTimeMillis() - this.lastClick < 5000) {
			// getState().getController().popupAlertTextMessage(Lng.str("Cannot save now!\nPlease wait %s seconds.", (System.currentTimeMillis() - this.lastClick) / 1000), 0);
			// return;
			// }
			// ShopControllerManager.this.lastClick = System.currentTimeMillis();
			// 
			// String description = Lng.str("Please enter in a name for your blueprint!");
			// PlayerGameTextInput pp = new PlayerGameTextInput("ENTER_NAME", getState(), 50, Lng.str("Blueprint"), description
			// , "BLUEPRINT" + "_" + System.currentTimeMillis()) {
			// @Override
			// public String[] getCommandPrefixes() {
			// return null;
			// }
			// 
			// @Override
			// public String handleAutoComplete(String s,
			// TextCallback callback, String prefix) {
			// return s;
			// }
			// 
			// @Override
			// public boolean isOccluded() {
			// return getState().getController().getPlayerInputs().indexOf(this) != getState().getController().getPlayerInputs().size() - 1;
			// }
			// 
			// @Override
			// public void onDeactivate() {
			// suspend(false);
			// }
			// 
			// @Override
			// public void onFailedTextCheck(String msg) {
			// setErrorMessage("SHIPNAME INVALID: " + msg);
			// 
			// }
			// 
			// @Override
			// public boolean onInput(String entry) {
			// SimpleTransformableSendableObject currentPlayerObject = getState().getCurrentPlayerObject();
			// if (currentPlayerObject == null || !(currentPlayerObject instanceof Ship)) {
			// System.err.println("[ERROR] Player not int a ship");
			// return false;
			// }
			// List<BlueprintEntry> readBluePrints = BluePrintController.active.readBluePrints();
			// boolean exists = false;
			// for (int i = 0; i < readBluePrints.size(); i++) {
			// //							System.err.println("CHECKING: " + readBluePrints.get(i).getName());
			// if (readBluePrints.get(i).getName().toLowerCase(Locale.ENGLISH).equals(entry.toLowerCase(Locale.ENGLISH))) {
			// exists = true;
			// break;
			// }
			// }
			// if (!exists) {
			// getState().getPlayer().enqueueClientBlueprintToWrite((SegmentController) currentPlayerObject, entry);
			// } else {
			// getState().getController().popupAlertTextMessage(Lng.str("File already exists\nin your local database!"), 0);
			// }
			// 
			// return true;
			// }
			// };
			// 
			// pp.setInputChecker(new InputChecker() {
			// @Override
			// public boolean check(String entry, TextCallback callback) {
			// if (EntityRequest.isShipNameValid(entry)) {
			// return true;
			// } else {
			// callback.onFailedTextCheck("Must only contain Letters or numbers or (_-)!");
			// return false;
			// }
			// }
			// });
			// getState().getController().getPlayerInputs().add(pp);
			// suspend(true);
			}
			if ("save_catalog".equals(callingGui.getUserPointer())) {
				if (System.currentTimeMillis() - this.lastClick < 5000) {
					getState().getController().popupAlertTextMessage(Lng.str("Cannot save now!\nPlease wait %s seconds.", (System.currentTimeMillis() - this.lastClick) / 1000), 0);
					return;
				}
				ShopControllerManager.this.lastClick = System.currentTimeMillis();
				String description = Lng.str("Please enter in a name for your blueprint!");
				PlayerGameTextInput pp = new PlayerGameTextInput("ENTER_NAME", getState(), 50, Lng.str("Blueprint"), description, "BLUEPRINT" + "_" + System.currentTimeMillis()) {

					@Override
					public String[] getCommandPrefixes() {
						return null;
					}

					@Override
					public String handleAutoComplete(String s, TextCallback callback, String prefix) {
						return s;
					}

					@Override
					public boolean isOccluded() {
						return getState().getController().getPlayerInputs().indexOf(this) != getState().getController().getPlayerInputs().size() - 1;
					}

					@Override
					public void onDeactivate() {
						suspend(false);
					}

					@Override
					public void onFailedTextCheck(String msg) {
						setErrorMessage("SHIPNAME INVALID: " + msg);
					}

					@Override
					public boolean onInput(String entry) {
						SimpleTransformableSendableObject currentPlayerObject = getState().getCurrentPlayerObject();
						if (currentPlayerObject == null || !(currentPlayerObject instanceof Ship)) {
							System.err.println("[ERROR] Player not int a ship");
							return false;
						}
						// RemoteStringArray sa = new RemoteStringArray(2, getState().getPlayer().getNetworkObject());
						// sa.set(0, "#save;"+currentPlayerObject.getId());
						// sa.set(1, entry);
						BlueprintPlayerHandleRequest req = new BlueprintPlayerHandleRequest();
						req.catalogName = entry;
						req.entitySpawnName = entry;
						req.save = true;
						req.toSaveShip = currentPlayerObject.getId();
						req.directBuy = false;
						getState().getPlayer().getNetworkObject().catalogPlayerHandleBuffer.add(new RemoteBlueprintPlayerRequest(req, false));
						// getState().getPlayer().getNetworkObject().catalogPlayerHandleBuffer.add(sa);
						return true;
					}
				};
				pp.setInputChecker((entry, callback) -> {
					if (EntityRequest.isShipNameValid(entry)) {
						return true;
					} else {
						callback.onFailedTextCheck(Lng.str("Must only contain letters or numbers or (_-)!"));
						return false;
					}
				});
				getState().getController().getPlayerInputs().add(pp);
				suspend(true);
			}
			if ("buy_catalog".equals(callingGui.getUserPointer()) && getSelectedBluePrint() != null) {
				if (System.currentTimeMillis() - this.lastClick < 5000) {
					getState().getController().popupAlertTextMessage(Lng.str("Cannot buy now!\nPlease wait %s seconds.", (System.currentTimeMillis() - this.lastClick) / 1000), 0);
					return;
				}
				ShopControllerManager.this.lastClick = System.currentTimeMillis();
				String description = Lng.str("Please type in a name for your new Ship!");
				PlayerGameTextInput pp = new PlayerGameTextInput("ENTER_NAME", getState(), 50, Lng.str("New Ship"), description, getSelectedBluePrint().getUid() + "_" + System.currentTimeMillis()) {

					@Override
					public String[] getCommandPrefixes() {
						return null;
					}

					@Override
					public String handleAutoComplete(String s, TextCallback callback, String prefix) {
						return s;
					}

					@Override
					public boolean isOccluded() {
						return getState().getController().getPlayerInputs().indexOf(this) != getState().getController().getPlayerInputs().size() - 1;
					}

					@Override
					public void onDeactivate() {
						suspend(false);
					}

					@Override
					public void onFailedTextCheck(String msg) {
						setErrorMessage(Lng.str("SHIPNAME INVALID: %s", msg));
					}

					@Override
					public boolean onInput(String entry) {
						if (getState().getCharacter() == null || getState().getCharacter().getPhysicsDataContainer() == null || !getState().getCharacter().getPhysicsDataContainer().isInitialized()) {
							System.err.println("[CLIENT][ERROR] Character might not have been initialized");
							return false;
						}
						RemoteStringArray sa = new RemoteStringArray(2, getState().getPlayer().getNetworkObject());
						System.err.println("[CLIENT] BUYING CATALOG ENTRY: " + getSelectedBluePrint().getUid() + " FOR " + getState().getPlayer().getNetworkObject());
						BlueprintPlayerHandleRequest req = new BlueprintPlayerHandleRequest();
						req.catalogName = getSelectedBluePrint().getUid();
						req.entitySpawnName = entry;
						req.save = false;
						req.toSaveShip = -1;
						req.directBuy = true;
						getState().getPlayer().getNetworkObject().catalogPlayerHandleBuffer.add(new RemoteBlueprintPlayerRequest(req, false));
						// sa.set(0, getSelectedBluePrint().catUID);
						// sa.set(1, entry);
						// getState().getPlayer().getNetworkObject().catalogPlayerHandleBuffer.add(sa);
						return true;
					}
				};
				pp.setInputChecker((entry, callback) -> {
					if (EntityRequest.isShipNameValid(entry)) {
						return true;
					} else {
						callback.onFailedTextCheck(Lng.str("Must only contain letters or numbers or (_-)!"));
						return false;
					}
				});
				getState().getController().getPlayerInputs().add(pp);
				suspend(true);
			}
			if (callingGui instanceof GUIOverlay) {
				if (selectedElementClass >= 0) {
					if ("buy".equals(callingGui.getUserPointer())) {
						assert (false);
					}
					if ("sell".equals(callingGui.getUserPointer())) {
						assert (false);
					}
					if ("sell_more".equals(callingGui.getUserPointer())) {
						assert (false);
					}
					if ("buy_more".equals(callingGui.getUserPointer())) {
						assert (false);
					}
				}
			}
		}
	}

	@Override
	public boolean isOccluded() {
		return !getState().getController().getPlayerInputs().isEmpty();
	}

	public boolean canBuy(short selectedElementClass) {
		if (getState().getPlayer().getInventory().isLockedInventory()) {
			getState().getController().popupAlertTextMessage(Lng.str("Cannot use make shop transfers in shipyard creative mode!"), 0);
			return false;
		}
		if (!ElementKeyMap.isValidType(selectedElementClass) || !ElementKeyMap.getInfo(selectedElementClass).isShoppable()) {
			return false;
		}
		if (ElementKeyMap.getInfo(selectedElementClass).isDeprecated()) {
			getState().getController().popupAlertTextMessage(Lng.str("Sorry, this item cannot be sold!\nIt is old technology\nand we are no longer\ntrading it..."), 0);
			return false;
		}
		if (getState().getPlayer().getInventory().getFirstSlot(selectedElementClass, false) == Inventory.FULL_RET) {
			getState().getController().popupAlertTextMessage(Lng.str("Inventory is full!"), 0);
			return false;
		}
		int count = 0;
		ShopInterface currentClosestShop = getState().getCurrentClosestShop();
		if (currentClosestShop == null) {
			getState().getController().popupAlertTextMessage(Lng.str("ERROR: No shop available!"), 0);
			return false;
		}
		if (!currentClosestShop.getShoppingAddOn().hasPermission(getState().getPlayer())) {
			getState().getController().popupAlertTextMessage(Lng.str("You are not allowed\nto buy at this shop!\n(no permission)"), 0);
			return false;
		}
		if (currentClosestShop.getShoppingAddOn().isInfiniteSupply()) {
			getState().getController().popupInfoTextMessage(Lng.str("Shop has infinite\nsupply."), 0);
			return true;
		}
		int firstSlot = currentClosestShop.getShopInventory().getFirstSlot(selectedElementClass, true);
		count = currentClosestShop.getShopInventory().getCount(firstSlot, selectedElementClass);
		if (firstSlot < 0 || count < 1) {
			getState().getController().popupAlertTextMessage(Lng.str("ERROR: Item out of stock!"), 0);
			return false;
		}
		if (currentClosestShop.getShoppingAddOn().canAfford(getState().getPlayer(), selectedElementClass, 1) < 1) {
			getState().getController().popupAlertTextMessage(Lng.str("ERROR: Not enough credits!"), 0);
			return false;
		}
		return true;
	}

	public boolean canSell(short selectedElementClass) {
		if (getState().getPlayer().getInventory().isLockedInventory()) {
			getState().getController().popupAlertTextMessage(Lng.str("Cannot use make shop transfers in shipyard creative mode!"), 0);
			return false;
		}
		if (!ElementKeyMap.isValidType(selectedElementClass) || !ElementKeyMap.getInfo(selectedElementClass).isShoppable()) {
			getState().getController().popupAlertTextMessage(Lng.str("ERROR: Item is forbidden\nfrom being traded:\n%s", ElementKeyMap.getInfo(selectedElementClass).getName()), 0);
			return false;
		}
		if (getState().getPlayer().getInventory(null).getFirstSlot(selectedElementClass, true) >= 0) {
			ShopInterface currentClosestShop = getState().getCurrentClosestShop();
			if (currentClosestShop != null) {
				if (!currentClosestShop.getShoppingAddOn().hasPermission(getState().getPlayer())) {
					getState().getController().popupAlertTextMessage(Lng.str("You are not allowed\nto buy at this shop!\n(no permission)"), 0);
					return false;
				}
				if (currentClosestShop.getShopInventory().canPutIn(selectedElementClass, 1)) {
					return true;
				} else {
					getState().getController().popupAlertTextMessage(Lng.str("ERROR: Shop has reached the max\nstock for the item:\n%s", ElementKeyMap.getInfo(selectedElementClass).getName()), 0);
				}
			} else {
				getState().getController().popupAlertTextMessage(Lng.str("ERROR: No shop in range!"), 0);
			}
		} else {
			getState().getController().popupAlertTextMessage(Lng.str("ERROR: You don't own that item!\n%s", ElementKeyMap.getInfo(selectedElementClass).getName()), 0);
		}
		return false;
	}

	/**
	 * @return the currentlySelectedCatalogElement
	 */
	public GUIListElement getCurrentlySelectedCatalogElement() {
		return currentlySelectedCatalogElement;
	}

	/**
	 * @param currentlySelectedCatalogElement the currentlySelectedCatalogElement to set
	 */
	public void setCurrentlySelectedCatalogElement(GUIListElement currentlySelectedCatalogElement) {
		this.currentlySelectedCatalogElement = currentlySelectedCatalogElement;
	}

	public PlayerInteractionControlManager getInteractionManager() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
	}

	/**
	 * @return the selectedBluePrint
	 */
	public CatalogPermission getSelectedBluePrint() {
		if (currentlySelectedCatalogElement != null) {
			return (CatalogPermission) currentlySelectedCatalogElement.getUserPointer();
		} else {
			return null;
		}
	}

	/**
	 * @return the selectedElementClass
	 */
	public short getSelectedElementClass() {
		return selectedElementClass;
	}

	/**
	 * @param selectedElementClass the selectedElementClass to set
	 */
	public void setSelectedElementClass(short selectedElementClass) {
		this.selectedElementClass = selectedElementClass;
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.manager.AbstractControlManager#onSwitch(boolean)
	 */
	@Override
	public void onSwitch(boolean active) {
		CameraMouseState.setGrabbed(!active);
		if (active) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(193);
		} else {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DEACTIVATE)*/
			AudioController.fireAudioEventID(192);
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
		if (!getState().isInShopDistance()) {
			getState().getController().popupAlertTextMessage(Lng.str("Lost all shops in range!"), 0);
			setDelayedActive(false);
			getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getInventoryControlManager().setDelayedActive(true);
		}
		getInteractionManager().suspend(true);
	}

	public void openSellDialog(short selectedClass, int startQuantity, ShopInterface currentClosestShop) {
		if (getState().getPlayer().getInventory().isLockedInventory()) {
			getState().getController().popupAlertTextMessage(Lng.str("Cannot use make shop transfers in shipyard creative mode!"), 0);
			return;
		}
		if (ElementKeyMap.isValidType(selectedClass) && ElementKeyMap.getInfo(selectedClass).isShoppable()) {
			String title = ShoppingAddOn.isSelfOwnedShop(getState(), currentClosestShop) ? Lng.str("Put Quantity") : Lng.str("Sell Quantity");
			getState().getController().getPlayerInputs().add(new SellQuantityDialog(getState(), title, selectedClass, startQuantity, currentClosestShop));
			suspend(true);
		} else {
			getState().getController().popupAlertTextMessage(Lng.str("Cannot sell this item.\nIt is not allowed in shops."), 0);
		}
	}

	/**
	 * @return the currentlySelectedListElement
	 */
	public GUIListElement getCurrentlySelectedListElement() {
		return currentlySelectedListElement;
	}

	/**
	 * @param currentlySelectedListElement the currentlySelectedListElement to set
	 */
	public void setCurrentlySelectedListElement(GUIListElement currentlySelectedListElement) {
		this.currentlySelectedListElement = currentlySelectedListElement;
	}

	public void openDeleteDialog(int slot, short type, int count, Inventory draggingInventory) {
		if (draggingInventory != null && draggingInventory.isInfinite()) {
			if (slot < 10) {
				draggingInventory.setSlot(slot, (short) 0, 0, 0);
				draggingInventory.sendInventoryModification(slot);
			}
		} else if (ElementKeyMap.isValidType(type)) {
			DeleteQuantityDialog deleteQuantityDialog = new DeleteQuantityDialog(getState(), slot, "Trash Item", type, count);
			deleteQuantityDialog.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(194);
		} else {
			getState().getController().popupAlertTextMessage(Lng.str("Cannot trash this item.\nIt isn't valid on this server."), 0);
		}
	}
}
