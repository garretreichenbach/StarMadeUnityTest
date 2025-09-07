package org.schema.game.client.view.gui.shop.shopnew;

import api.common.GameClient;
import api.common.GameCommon;
import org.json.JSONObject;
import org.schema.common.util.StringTools;
import org.schema.game.client.controller.ClientChannel;
import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.bpmarket.BlueprintMarketData;
import org.schema.game.common.controller.bpmarket.BlueprintMarketManager;
import org.schema.game.common.data.player.BlueprintPlayerHandleRequest;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.playermessage.PlayerMessage;
import org.schema.game.common.data.player.playermessage.PlayerMessageController;
import org.schema.game.network.objects.remote.RemoteBlueprintPlayerRequest;
import org.schema.game.network.objects.remote.RemotePlayerMessage;
import org.schema.game.server.data.blueprintnw.BlueprintClassification;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.settingsnew.GUICheckBoxTextPairNew;
import org.schema.schine.input.InputState;
import org.schema.schine.network.objects.remote.RemoteString;

import java.util.*;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class BlueprintMarketDataScrollableList extends ScrollableTableList<BlueprintMarketData> implements GUIActiveInterface {

	private final GUIElement panel;
	private final int mode;
	private long lastUpdateCheck;

	public BlueprintMarketDataScrollableList(InputState state, GUIElement panel, int mode) {
		super(state, 10, 10, panel);
		this.panel = panel;
		this.mode = mode;
	}

	private BlueprintMarketManager getManager() {
		return GameClient.getClientPlayerState().getBlueprintMarketManager();
	}

	@Override
	protected Collection<BlueprintMarketData> getElementList() {
		if(mode == AddBlueprintDialog.ADMIN_MODE) return getManager().getAdminItems();
		else return getManager().getMarketItems();
	}

	@Override
	public void draw() {
		super.draw();
		if(getManager().isUpdated() && System.currentTimeMillis() - lastUpdateCheck > 1000) {
			lastUpdateCheck = System.currentTimeMillis();
			getManager().changed = false;
			flagDirty();
			handleDirty();
		}
	}

	@Override
	public void initColumns() {
		addColumn(Lng.str("Name"), 15.0F, Comparator.comparing(BlueprintMarketData::getName));
		addColumn(Lng.str("Manufacturer"), 10.0F, Comparator.comparing(BlueprintMarketData::getFactionName));
		addColumn(Lng.str("Price"), 7.0f, Comparator.comparingLong(BlueprintMarketData::getPrice));
		addColumn(Lng.str("Type"), 5.0f, Comparator.comparing(BlueprintMarketData::getType));
		addColumn(Lng.str("Mass"), 7.0f, Comparator.comparing(BlueprintMarketData::getMass));

		addTextFilter(new GUIListFilterText<>() {

			@Override
			public boolean isOk(String s, BlueprintMarketData item) {
				return item.getName().toLowerCase(Locale.ENGLISH).contains(s.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH BY NAME"), ControllerElement.FilterRowStyle.FULL);

		addTextFilter(new GUIListFilterText<>() {

			@Override
			public boolean isOk(String s, BlueprintMarketData item) {
				return item.getFactionName().toLowerCase(Locale.ENGLISH).contains(s.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH BY MANUFACTURER"), ControllerElement.FilterRowStyle.LEFT);

		addDropdownFilter(new GUIListFilterDropdown<>(getClassifications()) {

			@Override
			public boolean isOk(String classification, BlueprintMarketData item) {
				return item.getType().getName().equals(classification) || classification.equals("ANY");
			}

		}, new CreateGUIElementInterface<>() {
			@Override
			public GUIElement create(String classification) {
				GUIAnchor anchor = new GUIAnchor(getState(), 10.0F, 24.0F);
				GUITextOverlayTableDropDown dropDown;
				(dropDown = new GUITextOverlayTableDropDown(10, 10, getState())).setTextSimple(classification.toUpperCase(Locale.ENGLISH));
				dropDown.setPos(4.0F, 4.0F, 0.0F);
				anchor.setUserPointer(classification);
				anchor.attach(dropDown);
				return anchor;
			}

			@Override
			public GUIElement createNeutral() {
				GUIAnchor anchor = new GUIAnchor(getState(), 10.0F, 24.0F);
				GUITextOverlayTableDropDown dropDown;
				(dropDown = new GUITextOverlayTableDropDown(10, 10, getState())).setTextSimple(Lng.str("ANY"));
				dropDown.setPos(4.0F, 4.0F, 0.0F);
				anchor.setUserPointer("ANY");
				anchor.attach(dropDown);
				return anchor;
			}
		}, ControllerElement.FilterRowStyle.RIGHT);
		activeSortColumnIndex = 0;
	}

	private String[] getClassifications() {
		List<String> classifications = new ArrayList<>();
		for(BlueprintClassification classification : BlueprintClassification.values()) {
			if(!classification.name().contains("NONE") && classification != BlueprintClassification.ALL_SHIPS && classification != BlueprintClassification.SHOPPING_STATION) classifications.add(classification.getName());
		}
		return classifications.toArray(new String[0]);
	}

	@Override
	public void updateListEntries(GUIElementList guiElementList, Set<BlueprintMarketData> set) {
		guiElementList.deleteObservers();
		guiElementList.addObserver(this);
		for(BlueprintMarketData data : set) {
			GUIClippedRow nameRow = getSimpleRow(data.getName(), this);
			GUIClippedRow producerRow = getSimpleRow(data.getFactionName(), this);
			GUIClippedRow priceRow = getSimpleRow(data.getPrice() + "", this);
			GUIClippedRow typeRow = getSimpleRow(data.getType().getName(), this);
			GUIClippedRow massRow = getSimpleRow(StringTools.massFormat(data.getMass()), this);
			BlueprintMarketDataScrollableListRow entryListRow = new BlueprintMarketDataScrollableListRow(getState(), data, nameRow, producerRow, priceRow, typeRow, massRow);
			GUIAnchor anchor = new GUIAnchor(getState(), panel.getWidth() - 28.0f, 24.0f) {
				@Override
				public void draw() {
					setWidth(panel.getWidth() - 28.0f);
					super.draw();
				}
			};
			GUIHorizontalButtonTablePane buttonTablePane = redrawButtonPane(data, anchor);
			anchor.attach(buttonTablePane);
			entryListRow.expanded = new GUIElementList(getState());
			GUITextOverlayTableInnerDescription description = new GUITextOverlayTableInnerDescription(10, 10, getState());
			description.onInit();
			description.setTextSimple("(Posted by " + data.getSellerName() + ")\n" + data.getDescription());
			entryListRow.expanded.add(new GUIListElement(description, getState()));
//			if(ImageUtils.isValidImageURL(data.getImage())) {
//				try {
//					if(StarLoaderTexture.isCached(data.getImage())) {
//						Sprite sprite = StarLoaderTexture.fetchCachedSprite(data.getImage());
//						if(sprite != null) {
//							GUIOverlay overlay = new GUIOverlay(sprite, getState());
//							entryListRow.expanded.add(new GUIListElement(overlay, getState()));
//						}
//					} else {
//						Todo: Loading sprite
//						ImageUtils.getImageFromURL(data.getImage(), downloaded -> {
//							Sprite sprite = StarLoaderTexture.newSprite(downloaded, data.getImage());
//							sprite.setPositionCenter(true);
//							GUIOverlay overlay = new GUIOverlay(sprite, getState());
//							entryListRow.expanded.add(new GUIListElement(overlay, getState()));
//						});
//					}
//				} catch(Exception exception) {
//					exception.printStackTrace();
//				}
//			}
			entryListRow.expanded.add(new GUIListElement(anchor, getState()));
			entryListRow.onInit();
			guiElementList.addWithoutUpdate(entryListRow);
		}
		guiElementList.updateDim();
	}

	private GUIHorizontalButtonTablePane redrawButtonPane(BlueprintMarketData data, GUIAnchor anchor) {
		boolean isOwner = GameClient.getClientPlayerState().getName().equals(data.getSellerName());
		boolean isAdmin = GameClient.getClientPlayerState().isAdmin();
		int columns = isAdmin ? 2 : 1;
		GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), columns, 1, anchor);
		buttonPane.onInit();
		int x = 0;
		if(!isOwner || isAdmin) {
			buttonPane.addButton(x, 0, Lng.str("BUY"), GUIHorizontalArea.HButtonColor.GREEN, new GUICallback() {
				@Override
				public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
					if(mouseEvent.pressedLeftMouse()) {
						(new PlayerOkCancelInput("Confirm", getState(), Lng.str("Confirm"), Lng.str("Do you want to buy this Blueprint?")) {
							@Override
							public void onDeactivate() {

							}

							@Override
							public void pressedOK() {
								String error = canBuy(data);
								if(error != null) ((GameClientState) getState()).getPlayer().sendServerMessagePlayerError(new Object[]{error});
								else buyBlueprint(data);
								deactivate();
							}
						}).activate();
					}
				}

				@Override
				public boolean isOccluded() {
					return false;
				}
			}, new GUIActivationCallback() {
				@Override
				public boolean isVisible(InputState inputState) {
					return true;
				}

				@Override
				public boolean isActive(InputState inputState) {
					return true;
				}
			});
			x++;
		}
		if(isOwner || isAdmin) {
			String text = Lng.str("REMOVE");
			if(isOwner && !isAdmin) text += " " + Lng.str("(ADMIN ACTION)");
			buttonPane.addButton(x, 0, text, GUIHorizontalArea.HButtonColor.RED, new GUICallback() {
				@Override
				public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
					if(mouseEvent.pressedLeftMouse()) {
						if(isAdmin && !isOwner) {
							boolean[] notifyOwner = {true};
							String[] removalReason = {"Inappropriate listing"};
							PlayerOkCancelInput okCancelInput = new PlayerOkCancelInput("Confirm", getState(), Lng.str("Confirm Admin Action"), Lng.str("Do you want to remove this Blueprint?")) {
								@Override
								public void onDeactivate() {

								}

								@Override
								public void pressedOK() {
									if(notifyOwner[0]) {
										PlayerMessage m = PlayerMessageController.getNew("Server Admin", data.getSellerName(), "Admin Action", "Your blueprint listing " + data.getName() + " was removed by an admin due to:\n" + removalReason[0]);
										ClientChannel channel = GameClient.getClientState().getController().getClientChannel();
										channel.getNetworkObject().playerMessageBuffer.add(new RemotePlayerMessage(m, channel.getNetworkObject()));
										((GameClientState) channel.getState()).getController().popupInfoTextMessage(Lng.str("Message sent to player\n%s", data.getSellerName()), 0);
									}
									getManager().removeData(data);
									deactivate();
								}
							};

							GUICheckBoxTextPairNew checkBox = new GUICheckBoxTextPairNew(getState(), Lng.str("Notify Owner"), FontLibrary.FontSize.SMALL_14) {
								@Override
								public void activate() {
									notifyOwner[0] = true;
								}

								@Override
								public void deactivate() {
									notifyOwner[0] = false;
								}

								@Override
								public boolean isChecked() {
									return notifyOwner[0];
								}
							};
							okCancelInput.getInputPanel().getContent().attach(checkBox);
							checkBox.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);

							GUIActivatableTextBar reasonText = new GUIActivatableTextBar(getState(), FontLibrary.FontSize.SMALL_14, 256, 5, "Removal Reason", okCancelInput.getInputPanel().getContent(), new TextCallback() {
								@Override
								public String[] getCommandPrefixes() {
									return new String[0];
								}

								@Override
								public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
									return "";
								}

								@Override
								public void onFailedTextCheck(String msg) {

								}

								@Override
								public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {

								}

								@Override
								public void newLine() {

								}
							}, t -> {
								removalReason[0] = t;
								return removalReason[0];
							}) {
								@Override
								public void draw() {
									if(notifyOwner[0]) super.draw();
									else cleanUp();
								}
							};
							okCancelInput.getInputPanel().getContent().attach(reasonText);
							reasonText.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset + checkBox.getHeight() + UIScale.getUIScale().inset, 0);
							okCancelInput.activate();
						} else {
							(new PlayerOkCancelInput("Confirm", getState(), Lng.str("Confirm"), Lng.str("Do you want to remove this Blueprint?")) {
								@Override
								public void onDeactivate() {
								}

								@Override
								public void pressedOK() {
									getManager().removeData(data);
									deactivate();
								}
							}).activate();
						}
					}
				}

				@Override
				public boolean isOccluded() {
					return false;
				}
			}, new GUIActivationCallback() {
				@Override
				public boolean isVisible(InputState inputState) {
					return true;
				}

				@Override
				public boolean isActive(InputState inputState) {
					return true;
				}
			});
		}
		return buttonPane;
	}

	public String canBuy(BlueprintMarketData data) {
		GameClientState state = (GameClientState) getState();
		if(!hasPermission(data)) return "Selected blueprint is not available or you don't have access to it!";
		else return state.getPlayer().getCredits() < data.getPrice() ? "You don't have enough credits to buy this blueprint!" : null;
	}

	private boolean hasPermission(BlueprintMarketData data) {
		PlayerState player = ((GameClientState) getState()).getPlayer();
		if(player.isAdmin()) return true;
		else {
			if(data.getSellerName().equals(GameClient.getClientPlayerState().getName())) return false;
			if(player.getFactionId() != 0 && GameClient.getClientPlayerState().getFactionId() != 0) {
				int factionId = player.getFactionId();
				int sellerFactionId = GameClient.getClientPlayerState().getFactionId();
				return !GameCommon.getGameState().getFactionManager().isEnemy(factionId, sellerFactionId);
			}
		}
		return true;
	}

	private void buyBlueprint(BlueprintMarketData data) {
		BlueprintPlayerHandleRequest req = new BlueprintPlayerHandleRequest();
		req.catalogName = data.getCatalogEntryUID();
		req.entitySpawnName = "";
		req.save = false;
		req.toSaveShip = -1;
		req.directBuy = false;
		req.fill = data.isFilled();
		((GameClientState) getState()).getPlayer().getNetworkObject().catalogPlayerHandleBuffer.add(new RemoteBlueprintPlayerRequest(req, true));
		if(!data.isFilled()) {
			JSONObject json = new JSONObject();
			json.put("dataUUID", data.getDataUUID());
			json.put("buyerName", ((GameClientState) getState()).getPlayer().getName());
			json.put("sellerName", ((GameClientState) getState()).getPlayer().getName());
			json.put("price", data.getPrice());
			((GameClientState) getState()).getPlayer().getNetworkObject().creditTransactionBuffer.add(new RemoteString(json.toString(), ((GameClientState) getState()).getPlayer().getNetworkObject()));
		}
	}

	public class BlueprintMarketDataScrollableListRow extends ScrollableTableList<BlueprintMarketData>.Row {

		public BlueprintMarketDataScrollableListRow(InputState state, BlueprintMarketData data, GUIElement... elements) {
			super(state, data, elements);
			highlightSelect = true;
			highlightSelectSimple = true;
			setAllwaysOneSelected(true);
		}
	}
}
