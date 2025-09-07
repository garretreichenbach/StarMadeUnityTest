package org.schema.game.client.view.gui.catalog.newcatalog;

import api.common.GameClient;
import org.schema.game.client.controller.PlayerGameDropDownInput;
import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.game.client.controller.PlayerTextInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.Segment2ObjWriter;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.data.UploadInProgressException;
import org.schema.game.common.data.player.BlueprintPlayerHandleRequest;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.SimplePlayerCommands;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.network.commands.gamerequests.EntityRequest;
import org.schema.game.network.objects.remote.RemoteBlueprintPlayerRequest;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.controller.EntityNotFountException;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.blueprintnw.BlueprintClassification;
import org.schema.game.server.data.blueprintnw.BlueprintEntry;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.resource.FileExt;
import org.schema.schine.sound.controller.AudioController;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Locale;

public class CatalogOptionsButtonPanel extends GUIAnchor {

	private CatalogPanelNew panel;

	public CatalogOptionsButtonPanel(InputState state, CatalogPanelNew panel) {
		super(state);
		this.panel = panel;
	}

	public static boolean areMultiplayerButtonVisible() {
		return !GameServerState.isCreated() || EngineSettings.A_FORCE_LOCAL_SAVE_ENABLED_IN_SINGLE_PLAYER.isOn();
	}

	public PlayerState getOwnPlayer() {
		return this.getState().getPlayer();
	}

	public Faction getOwnFaction() {
		return this.getState().getFaction();
	}

	@Override
	public GameClientState getState() {
		return ((GameClientState) super.getState());
	}

	@Override
	public void onInit() {
		GUIHorizontalButtonTablePane p = new GUIHorizontalButtonTablePane(getState(), 2, 2, Lng.str("Blueprint Options"), this);
		p.onInit();
		p.activeInterface = panel;
		p.addButton(0, 0, Lng.str("Save Blueprint of entered structure"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(376);
					save();
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
				return isValidPlayerObjectToSave();
			}
		});
		p.addButton(0, 1, Lng.str("Save from multiplayer server as local"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(377);
					saveLocal();
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return areMultiplayerButtonVisible();
			}

			@Override
			public boolean isActive(InputState state) {
				return isValidPlayerObjectToSave();
			}
		});
		p.addButton(1, 1, Lng.str("Upload Blueprint to multiplayer server"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(378);
					upload();
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return areMultiplayerButtonVisible();
			}

			@Override
			public boolean isActive(InputState state) {
				return true;
			}
		});
		p.addButton(1, 0, Lng.str("Export as 3D model"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					SimpleTransformableSendableObject currentPlayerObject = getState().getCurrentPlayerObject();
					if (currentPlayerObject != null && currentPlayerObject instanceof SegmentController) {
						final SegmentController c = (SegmentController) currentPlayerObject;
						(new PlayerTextInput("PPLExport", getState(), 64, Lng.str("Export"), Lng.str("Enter export name")) {

							@Override
							public void onFailedTextCheck(String msg) {
							}

							@Override
							public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
								return null;
							}

							@Override
							public String[] getCommandPrefixes() {
								return null;
							}

							@Override
							public boolean onInput(final String entry) {
								if (entry.length() == 0) {
									/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.ERROR)*/
									AudioController.fireAudioEventID(379);
									return false;
								}
								final File f = new FileExt(Segment2ObjWriter.DEFAULT_PATH + "/" + entry + "/");
								if (f.exists()) {
									(new PlayerOkCancelInput("AAEXPO", getState(), Lng.str("Export"), Lng.str("File already exists. Overwrite?")) {

										@Override
										public void pressedOK() {
											FileUtil.deleteDir(f);
											CatalogOptionsButtonPanel.this.getState().getController().popupInfoTextMessage(Lng.str("Exporting Structure to \n%s", f.getAbsolutePath()), 0);
											Segment2ObjWriter w = new Segment2ObjWriter(c, Segment2ObjWriter.DEFAULT_PATH, entry);
											(new Thread(w)).start();
											CatalogOptionsButtonPanel.this.getState().exportingShip = w;
											deactivate();
										}

										@Override
										public void onDeactivate() {
										}
									}).activate();
									/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
									AudioController.fireAudioEventID(380);
								} else {
									if (!f.mkdirs()) {
										CatalogOptionsButtonPanel.this.getState().getController().popupAlertTextMessage(Lng.str("Invalid file Name"), 0);
										return false;
									} else {
										CatalogOptionsButtonPanel.this.getState().getController().popupInfoTextMessage(Lng.str("Exporting Structure to \n%s", f.getAbsolutePath()), 0);
										Segment2ObjWriter w = new Segment2ObjWriter(c, Segment2ObjWriter.DEFAULT_PATH, entry);
										(new Thread(w)).start();
										CatalogOptionsButtonPanel.this.getState().exportingShip = w;
									}
								}
								/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
								AudioController.fireAudioEventID(381);
								return true;
							}

							@Override
							public void onDeactivate() {
							}
						}).activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(382);
					} else {
						CatalogOptionsButtonPanel.this.getState().getController().popupAlertTextMessage(Lng.str("No valid structure"), 0);
					}
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return isValidPlayerObjectToSave() && !Segment2ObjWriter.isRunning();
			}
		});
		setPos(1, 0, 0);
		attach(p);
	}

	private static final ByteBuffer buffer = ByteBuffer.allocate(10 * 1024);

	private void save() {
		String description = Lng.str("Please choose a name and classification for your blueprint!");
		final SimpleTransformableSendableObject<?> currentPlayerObject = getState().getCurrentPlayerObject();
		final BlueprintPlayerHandleRequest req = new BlueprintPlayerHandleRequest();
		PlayerGameTextInput pp = new PlayerGameTextInput("CatalogOptionsButtonPanel_SAVE", getState(), 50, Lng.str("Blueprint"), description, "BLUEPRINT" + "_" + System.currentTimeMillis()) {

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
			}

			@Override
			public void onFailedTextCheck(String msg) {
				setErrorMessage("SHIPNAME INVALID: " + msg);
			}

			@Override
			public boolean onInput(String entry) {
				if (!isValidPlayerObjectToSave()) {
					System.err.println("[ERROR] Player not int a ship");
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.ERROR)*/
					AudioController.fireAudioEventID(383);
					return false;
				}
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
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(384);
				return true;
			}
		};
		pp.getInputPanel().onInit();
		GUIElement[] guiElements = BlueprintClassification.getGUIElements(getState(), currentPlayerObject.getType());
		req.classification = (BlueprintClassification) guiElements[0].getUserPointer();
		GUIDropDownList catList = new GUIDropDownList(getState(), UIScale.getUIScale().scale(300), UIScale.getUIScale().h, UIScale.getUIScale().scale(200), element -> req.classification = (BlueprintClassification) element.getContent().getUserPointer(), guiElements);
		catList.setPos(4, 30, 0);
		((GUIDialogWindow) pp.getInputPanel().getBackground()).getMainContentPane().getContent(0).attach(catList);
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
		AudioController.fireAudioEventID(385);
	}

	private boolean isValidPlayerObjectToSave() {
		SimpleTransformableSendableObject currentPlayerObject = getState().getCurrentPlayerObject();
		return (((currentPlayerObject instanceof Ship) || (currentPlayerObject instanceof SpaceStation)));
	}

	private void saveLocal() {
		final SimpleTransformableSendableObject currentPlayerObject = getState().getCurrentPlayerObject();
		final BlueprintPlayerHandleRequest req = new BlueprintPlayerHandleRequest();
		String description = Lng.str("Please enter in a name for your blueprint!");
		PlayerGameTextInput pp = new PlayerGameTextInput("CatalogOptionsButtonPanel_SAVE_LOCAL", getState(), 50, Lng.str("Blueprint"), description, "BLUEPRINT" + "_" + System.currentTimeMillis()) {

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
			}

			@Override
			public void onFailedTextCheck(String msg) {
				setErrorMessage(Lng.str("SHIPNAME INVALID: %s", msg));
			}

			@Override
			public boolean onInput(String entry) {
				if (!isValidPlayerObjectToSave()) {
					System.err.println("[ERROR] Player not int a ship/station");
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
					getState().getPlayer().sendSimpleCommand(SimplePlayerCommands.CLIENT_TO_SERVER_LOG, "[LOCALSAVE] " + getState().getPlayer().getName() + " saved structure local: UID: " + currentPlayerObject.getUniqueIdentifier() + "; Real Name: " + currentPlayerObject.getRealName());
					getState().getPlayer().enqueueClientBlueprintToWrite((SegmentController) currentPlayerObject, entry, req.classification);
				} else {
					(new PlayerOkCancelInput("OverwriteBPDialog", getState(), "Overwrite Blueprint", "A blueprint with the name \"" + entry + "\" already exists in your local database.\nWould you like to overwrite it?") {
						@Override
						public void onDeactivate() {
						}

						@Override
						public void pressedOK() {
							try {
								BlueprintEntry bp = BluePrintController.active.getBlueprint(entry);
								BluePrintController.active.removeBluePrint(bp);
							} catch(IOException | EntityNotFountException exception) {
								exception.printStackTrace();
							}
							GameClient.getClientPlayerState().sendSimpleCommand(SimplePlayerCommands.CLIENT_TO_SERVER_LOG, "[LOCALSAVE] " + GameClient.getClientPlayerState().getName() + " saved structure local: UID: " + currentPlayerObject.getUniqueIdentifier() + "; Real Name: " + currentPlayerObject.getRealName());
							GameClient.getClientPlayerState().enqueueClientBlueprintToWrite((SegmentController) currentPlayerObject, entry, req.classification);
							deactivate();
						}
					}).activate();
//					getState().getController().popupAlertTextMessage(Lng.str("File already exists\nin your local database!"), 0);
				}
				return true;
			}
		};
		pp.getInputPanel().onInit();
		GUIElement[] guiElements = BlueprintClassification.getGUIElements(getState(), currentPlayerObject.getType());
		req.classification = (BlueprintClassification) guiElements[0].getUserPointer();
		GUIDropDownList catList = new GUIDropDownList(getState(), UIScale.getUIScale().scale(300), UIScale.getUIScale().h, UIScale.getUIScale().scale(200), element -> req.classification = (BlueprintClassification) element.getContent().getUserPointer(), guiElements);
		catList.setPos(4, 30, 0);
		((GUIDialogWindow) pp.getInputPanel().getBackground()).getMainContentPane().getContent(0).attach(catList);
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
		AudioController.fireAudioEventID(386);
	}

	private void upload() {
		List<BlueprintEntry> readBluePrints = BluePrintController.active.readBluePrints();
		readBluePrints.sort((o1, o2) -> {
			String str1 = o1.getName().trim();
			String str2 = o2.getName().trim();
			if(str1.equals(str2)) {
				return 0;
			}
			return String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
		});
		String description = Lng.str("Please select your blueprint!");
		GUIAnchor[] bb = new GUIAnchor[readBluePrints.size()];
		for (int i = 0; i < bb.length; i++) {
			bb[i] = new GUIAnchor(getState(), UIScale.getUIScale().scale(300), UIScale.getUIScale().h);
			bb[i].setUserPointer(readBluePrints.get(i));
			GUITextOverlay t = new GUITextOverlay(FontLibrary.FontSize.MEDIUM_15, getState());
			t.setTextSimple(readBluePrints.get(i).getName());
			t.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
			bb[i].attach(t);
		}
		System.err.println("UPLOADING DIALOG");
		PlayerGameDropDownInput pp = new PlayerGameDropDownInput("CatalogOptionsButtonPanel_UPLOAD", getState(), Lng.str("Blueprint"), UIScale.getUIScale().h, description, bb) {

			@Override
			public void onDeactivate() {
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
		AudioController.fireAudioEventID(387);
	}
}
