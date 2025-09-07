package org.schema.game.client.view.gui.catalog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.vecmath.Vector4f;

import org.schema.game.client.controller.PlayerGameDropDownInput;
import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.controller.manager.ingame.catalog.CatalogControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.UploadInProgressException;
import org.schema.game.common.data.player.BlueprintPlayerHandleRequest;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.network.commands.gamerequests.EntityRequest;
import org.schema.game.network.objects.remote.RemoteBlueprintPlayerRequest;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.data.blueprintnw.BlueprintEntry;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class CatalogToolsPanel extends GUIAnchor implements GUICallback {

	private GUITextButton createButton;

	private GUITextButton createLocalButton;

	private GUITextButton uploadButton;

	private GUITextOverlay hint;

	public CatalogToolsPanel(InputState state) {
		super(state, 510, 60);
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (event.pressedLeftMouse()) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
			AudioController.fireAudioEventID(360);
			SimpleTransformableSendableObject selectedEntity = ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getSelectedEntity();
			boolean active = ((GameClientState) getState()).getShip() != null || (selectedEntity != null && selectedEntity instanceof Ship);
			if ("save".equals(callingGuiElement.getUserPointer())) {
				if (active) {
					save();
				} else {
					((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("You must be in a\nship or have one\nselected to save it."), 0);
				}
			} else if ("save_local".equals(callingGuiElement.getUserPointer())) {
				if (active) {
					saveLocal();
				} else {
					((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("You must be in a\nship or have one\nselected to save it."), 0);
				}
			} else if ("upload".equals(callingGuiElement.getUserPointer())) {
				upload();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#draw()
	 */
	@Override
	public void draw() {
		SimpleTransformableSendableObject selectedEntity = ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getSelectedEntity();
		// boolean active = ((GameClientState)getState()).getShip() != null || (selectedEntity != null && selectedEntity instanceof Ship);
		// createButton.setActive(active);
		super.draw();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();
		GameClientState state = (GameClientState) getState();
		createButton = new GUITextButton(state, 142, 25, new Vector4f(0.3f, 0.3f, 0.7f, 1f), new Vector4f(1, 1, 1, 1), FontSize.MEDIUM_15, "Create new entry", this, getPlayerCatalogControlManager());
		createButton.setUserPointer("save");
		createButton.setTextPos(4, 1);
		createLocalButton = new GUITextButton(state, 140, 20, new Vector4f(0.3f, 0.7f, 0.5f, 1f), new Vector4f(1, 1, 1, 1), FontSize.SMALL_15, "Save in local catalog", this, getPlayerCatalogControlManager());
		createLocalButton.setUserPointer("save_local");
		uploadButton = new GUITextButton(state, 160, 20, new Vector4f(0.5f, 0.7f, 0.3f, 1f), new Vector4f(1, 1, 1, 1), FontSize.SMALL_15, "Upload entry from local", this, getPlayerCatalogControlManager());
		uploadButton.setUserPointer("upload");
		createLocalButton.getPos().x = 220;
		uploadButton.getPos().x = 370;
		GUITextOverlay usedSlots = new GUITextOverlay(state);
		int slots = state.getGameState().getNetworkObject().saveSlotsAllowed.get();
		if (slots < 0) {
			usedSlots.setTextSimple("Used: " + state.getPlayer().getCatalog().getPersonalCatalog().size());
		} else {
			usedSlots.setTextSimple("Used: " + state.getPlayer().getCatalog().getPersonalCatalog().size() + "/" + slots);
		}
		usedSlots.getPos().x = UIScale.getUIScale().scale(150);
		usedSlots.getPos().y = UIScale.getUIScale().smallinset;
		hint = new GUITextOverlay(state);
		hint.setText(new ArrayList());
		hint.getText().add("\"Create new Entry\" will save the ship you are currently in into this catalog. You can also save ");
		hint.getText().add("a ship in your singleplayer (local) catalog, or upload an entry from it.");
		hint.getPos().y = createButton.getPos().y + createButton.getHeight() + 4;
		attach(hint);
		attach(usedSlots);
		attach(createButton);
		attach(createLocalButton);
		attach(uploadButton);
	}

	public CatalogControlManager getPlayerCatalogControlManager() {
		return ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getCatalogControlManager();
	}

	private void save() {
		getPlayerCatalogControlManager().suspend(true);
		String description = "Please enter in a name for your blue print!";
		PlayerGameTextInput pp = new PlayerGameTextInput("CatalogToolsPanel_SAVE", (GameClientState) getState(), 50, "BluePrint", description, "BLUEPRINT" + "_" + System.currentTimeMillis()) {

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
				getPlayerCatalogControlManager().suspend(false);
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
				BlueprintPlayerHandleRequest req = new BlueprintPlayerHandleRequest();
				req.catalogName = entry;
				req.entitySpawnName = entry;
				req.save = true;
				req.toSaveShip = currentPlayerObject.getId();
				req.directBuy = false;
				getState().getPlayer().getNetworkObject().catalogPlayerHandleBuffer.add(new RemoteBlueprintPlayerRequest(req, false));
				// RemoteStringArray sa = new RemoteStringArray(2, getState().getPlayer().getNetworkObject());
				// sa.set(0, "#save;"+currentPlayerObject.getId());
				// sa.set(1, entry);
				// getState().getPlayer().getNetworkObject().catalogPlayerHandleBuffer.add(sa);
				return true;
			}
		};
		pp.setInputChecker((entry, callback) -> {
			if (EntityRequest.isShipNameValid(entry)) {
				return true;
			} else {
				callback.onFailedTextCheck("Must only contain Letters or numbers or (_-)!");
				return false;
			}
		});
		pp.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(361);
	}

	private void saveLocal() {
		getPlayerCatalogControlManager().suspend(true);
		String description = "Please enter in a name for your blue print!";
		PlayerGameTextInput pp = new PlayerGameTextInput("CatalogToolsPanel_SAVE_LOCAL", (GameClientState) getState(), 50, "BluePrint", description, "BLUEPRINT" + "_" + System.currentTimeMillis()) {

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
				getPlayerCatalogControlManager().suspend(false);
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
				List<BlueprintEntry> readBluePrints = BluePrintController.active.readBluePrints();
				boolean exists = false;
				for (int i = 0; i < readBluePrints.size(); i++) {
					// System.err.println("CHECKING: " + readBluePrints.get(i).getName());
					if (readBluePrints.get(i).getName().toLowerCase(Locale.ENGLISH).equals(entry.toLowerCase(Locale.ENGLISH))) {
						exists = true;
						break;
					}
				}
				if (!exists) {
					getState().getPlayer().enqueueClientBlueprintToWrite((SegmentController) currentPlayerObject, entry, null);
					throw new NullPointerException("DEPRECATED");
				} else {
					getState().getController().popupAlertTextMessage(Lng.str("File already exists\nin your local database!"), 0);
				}
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
		pp.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(362);
	}

	private void upload() {
		getPlayerCatalogControlManager().suspend(true);
		List<BlueprintEntry> readBluePrints = BluePrintController.active.readBluePrints();
		Collections.sort(readBluePrints, (o1, o2) -> {
			String str1 = o1.getName().trim();
			String str2 = o2.getName().trim();
			if (str1.equals(str2)) {
				return 0;
			}
			return String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
		});
		String description = Lng.str("Please select your blueprint!");
		// PlayerGameTextInput pp = new PlayerGameTextInput((GameClientState)getState(), 50, "BluePrint", description
		// ,readBluePrints.isEmpty() ? "" : readBluePrints.get(0).toString()) {
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
		// return getState().getController().getPlayerInputs().indexOf(this) != getState().getController().getPlayerInputs().size()-1;
		// }
		// 
		// @Override
		// public void onDeactivate() {
		// getPlayerCatalogControlManager().suspend(false);
		// }
		// 
		// @Override
		// public void onFailedTextCheck(String msg) {
		// setErrorMessage("SHIPNAME INVALID: "+msg);
		// 
		// }
		// @Override
		// public boolean onInput(String entry) {
		// try {
		// getState().getPlayer().getShipUploadController().upload(entry);
		// 
		// return true;
		// } catch (IOException e) {
		// e.printStackTrace();
		// GLFrame.processErrorDialogException(e);
		// }  catch (UploadInProgressException e) {
		// getState().getController().popupAlertTextMessage("Cannot Upload!\nThere is already\nan Upload in progress", 0);
		// }
		// 
		// return false;
		// }
		// 
		// };
		// 
		// pp.setInputChecker(new InputChecker() {
		// @Override
		// public boolean check(String entry, TextCallback callback) {
		// if(EntityRequest.isShipNameValid(entry)){
		// return true;
		// }else{
		// callback.onFailedTextCheck("Must only contain Letters or numbers or (_-)!");
		// return false;
		// }
		// }
		// });
		// pp.activate(); AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE);
		GUIAnchor[] bb = new GUIAnchor[readBluePrints.size()];
		for (int i = 0; i < bb.length; i++) {
			bb[i] = new GUIAnchor(getState(), UIScale.getUIScale().scale(300), UIScale.getUIScale().h);
			bb[i].setUserPointer(readBluePrints.get(i));
			GUITextOverlay t = new GUITextOverlay(FontSize.TINY_12, getState());
			t.setTextSimple(readBluePrints.get(i).getName());
			t.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
			bb[i].attach(t);
		}
		System.err.println("UPLOADING DIALOG");
		PlayerGameDropDownInput pp = new PlayerGameDropDownInput("CatalogToolsPanel_UPLOAD", (GameClientState) getState(), Lng.str("Upload blueprint"), UIScale.getUIScale().h, description, bb) {

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
				if (current != null) {
					try {
						getState().getPlayer().getShipUploadController().upload(((BlueprintEntry) current.getContent().getUserPointer()).getName());
					} catch (IOException e) {
						e.printStackTrace();
						GLFrame.processErrorDialogException(e, getState());
					} catch (UploadInProgressException e) {
						getState().getController().popupAlertTextMessage(Lng.str("Cannot Upload!\nThere is already\nan upload in progress!"), 0);
					}
				} else {
					System.err.println("[UPLOAD] dropdown null selected");
				}
				deactivate();
			}
		};
		pp.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(363);
	}
}
