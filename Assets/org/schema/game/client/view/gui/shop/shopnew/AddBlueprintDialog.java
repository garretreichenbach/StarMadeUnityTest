package org.schema.game.client.view.gui.shop.shopnew;

import api.common.GameClient;
import org.schema.game.client.controller.PlayerGameDropDownInput;
import org.schema.game.client.controller.PlayerInput;
import org.schema.game.client.controller.manager.ingame.catalog.CatalogControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.common.controller.bpmarket.BlueprintMarketData;
import org.schema.game.common.controller.bpmarket.BlueprintMarketManager;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import java.util.List;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class AddBlueprintDialog extends PlayerInput {

	public static final int PLAYER_MODE = 0;
	public static final int ADMIN_MODE = 1;

	private final AddBlueprintPanel panel;
	private final int mode;

	public AddBlueprintDialog(GameClientState state, int mode) {
		super(state);
		this.mode = mode;
		(panel = new AddBlueprintPanel(getState(), this, mode)).onInit();
	}
	
	private static BlueprintMarketManager getBlueprintMarketManager() {
		return GameClient.getClientPlayerState().getBlueprintMarketManager();
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if(callingGuiElement.getUserPointer() != null && !callingGuiElement.wasInside() && callingGuiElement.isInside()) AudioController.fireAudioEventID(235);
		if(event.pressedLeftMouse() && callingGuiElement.getUserPointer() instanceof String) {
			switch((String) callingGuiElement.getUserPointer()) {
				case "OK" -> {
					BlueprintMarketData data = panel.getBlueprintData();
					if(data != null) {
						if(getBlueprintMarketManager().getDataByName(data.getName()) != null) {
							AudioController.fireAudioEventID(237);
							GameClient.showPopupMessage(Lng.str("A Blueprint by that name already exists!"), 0);
							deactivate();
							return;
						}
						data.setAdmin(mode == ADMIN_MODE);
						data.setFilled(mode == ADMIN_MODE);
						AudioController.fireAudioEventID(238);
						GameClient.getClientPlayerState().getBlueprintMarketManager().addData(data);
						deactivate();
					}
				}
				case "CANCEL", "X" -> {
					AudioController.fireAudioEventID(236);
					deactivate();
				}
			}
		}
	}

	@Override
	public void onDeactivate() {

	}

	@Override
	public AddBlueprintPanel getInputPanel() {
		return panel;
	}

	public static class AddBlueprintPanel extends GUIInputPanel {

		private final int mode;
		private BlueprintMarketData blueprintData;
		private GUIActivatableTextBar nameInput;
		private GUIActivatableTextBar descriptionInput;
		private GUIActivatableTextBar priceInput;
		private GUIActivatableTextBar imageInput;

		public AddBlueprintPanel(InputState state, GUICallback guiCallback, int mode) {
			super("Add_Blueprint_Panel", state, 500, 500, guiCallback, Lng.str("Add Blueprint"), "");
			this.mode = mode;
		}

		@Override
		public void onInit() {
			super.onInit();
			GUIContentPane contentPane = ((GUIDialogWindow) background).getMainContentPane();
			blueprintData = new BlueprintMarketData(((GameClientState) getState()).getPlayer(), mode);
			GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 1, 1, contentPane.getContent(0));
			buttonPane.onInit();
			buttonPane.addButton(0, 0, Lng.str("SELECT ENTRY"), GUIHorizontalArea.HButtonColor.GREEN, new GUICallback() {
				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse()) {
						List<CatalogPermission> catalogEntries = getCatalogEntries();
						GUIAnchor[] entries = new GUIAnchor[catalogEntries.size()];
						for(int i = 0; i < entries.length; i++) {
							entries[i] = new GUIAnchor(getState(), UIScale.getUIScale().scale(300), UIScale.getUIScale().h);
							entries[i].setUserPointer(catalogEntries.get(i));
							GUITextOverlay t = new GUITextOverlay(FontLibrary.FontSize.TINY_12, getState());
							t.setTextSimple(catalogEntries.get(i).getUid());
							t.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
							t.setUserPointer(catalogEntries.get(i));
							entries[i].attach(t);
						}

						(new PlayerGameDropDownInput("Add_Blueprint_Dialog_Select_Entry", (GameClientState) getState(), Lng.str("Select Blueprint"), UIScale.getUIScale().h, Lng.str("Select Blueprint"), entries) {

							public CatalogControlManager getPlayerCatalogControlManager() {
								return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getCatalogControlManager();
							}

							@Override
							public void onDeactivate() {
								getPlayerCatalogControlManager().suspend(false);
							}

							@Override
							public boolean isOccluded() {
								return false;
							}

							@Override
							public void pressedOK(GUIListElement current) {
								if(current != null && current.getContent().getUserPointer() != null) {
									if(current.getContent().getUserPointer() instanceof CatalogPermission permission) {
										blueprintData.setFromCatalogEntry(permission);
										nameInput.setText(permission.getUid());
										descriptionInput.setText(permission.description);
										priceInput.setText(String.valueOf(permission.price));
										blueprintData.setType(permission.getClassification());
									}
								}
								deactivate();
							}
						}).activate();
					}
				}

				private List<CatalogPermission> getCatalogEntries() {
					return GameClient.getClientPlayerState().getCatalog().getPersonalCatalog();
				}

				@Override
				public boolean isOccluded() {
					return false;
				}
			}, new GUIActivationCallback() {
				@Override
				public boolean isVisible(InputState state) {
					return true;
				}

				@Override
				public boolean isActive(InputState state) {
					return true;
				}
			});
			contentPane.getContent(0).attach(buttonPane);

			nameInput = new GUIActivatableTextBar(getState(), FontLibrary.FontSize.SMALL_14, 32, 1, "Name", contentPane.getContent(0), new TextCallback() {
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
			}, contentPane.getTextboxes().get(0), s -> {
				blueprintData.setName(s);
				return blueprintData.getName();
			});
			nameInput.drawStats = false;
			nameInput.setPos(0, buttonPane.getPos().y + buttonPane.getHeight() + UIScale.getUIScale().inset, 0);
			nameInput.setText(blueprintData.getName());
			contentPane.getContent(0).attach(nameInput);

			priceInput = new GUIActivatableTextBar(getState(), FontLibrary.FontSize.SMALL_14, 10, 1, "Price", contentPane.getContent(0), new TextCallback() {
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
			}, contentPane.getTextboxes().get(0), s -> {
				try {
					blueprintData.setPrice(Long.parseLong(s.trim()));
				} catch(NumberFormatException ignored) {
					blueprintData.setPrice(0);
				}
				return String.valueOf(blueprintData.getPrice());
			});
			priceInput.drawStats = false;
			priceInput.setPos(0, nameInput.getPos().y + nameInput.getHeight() + UIScale.getUIScale().inset, 0);
			contentPane.getContent(0).attach(priceInput);

			descriptionInput = new GUIActivatableTextBar(getState(), FontLibrary.FontSize.SMALL_14, 256, 3, "Description", contentPane.getContent(0), new TextCallback() {
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
			}, contentPane.getTextboxes().get(0), s -> {
				blueprintData.setDescription(s);
				return blueprintData.getDescription();
			});
			descriptionInput.drawStats = false;
			descriptionInput.setPos(0, priceInput.getPos().y + priceInput.getHeight() + UIScale.getUIScale().inset, 0);
			descriptionInput.setText(blueprintData.getDescription());
			contentPane.getContent(0).attach(descriptionInput);

//			imageInput = new GUIActivatableTextBar(getState(), FontLibrary.FontSize.SMALL_14, 140, 1, "Image URL (Must end in .png)", contentPane.getContent(0), new TextCallback() {
//				@Override
//				public String[] getCommandPrefixes() {
//					return new String[0];
//				}
//
//				@Override
//				public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
//					return "";
//				}
//
//				@Override
//				public void onFailedTextCheck(String msg) {
//
//				}
//
//				@Override
//				public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {
//
//				}
//
//				@Override
//				public void newLine() {
//
//				}
//			}, contentPane.getTextboxes().get(0), s -> {
//				blueprintData.setImage(s);
//				return blueprintData.getImage();
//			});
//			imageInput.drawStats = false;
//			imageInput.setPos(0, priceInput.getPos().y + priceInput.getHeight() + UIScale.getUIScale().inset, 0);
//			contentPane.getContent(0).attach(imageInput);
		}

		private boolean isValid() {
			return !blueprintData.getCatalogEntryUID().isEmpty() && !blueprintData.getName().isEmpty() && !blueprintData.getDescription().isEmpty() && blueprintData.getPrice() > 0 && (mode == ADMIN_MODE || blueprintData.getFactionId() > 0);
		}

		public BlueprintMarketData getBlueprintData() {
			if(isValid()) return blueprintData;
			else return null;
		}
	}
}
