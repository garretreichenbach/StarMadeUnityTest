package org.schema.game.client.view.gui.catalog.newcatalog;

import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.admin.AdminCommands;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.font.FontStyle;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActivatableTextBar;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class CatalogAdminOptionsButtonPanel extends GUIAnchor {

	private CatalogPanelNew panel;

	private CatalogBattleScrollableListNew scrl;

	public CatalogAdminOptionsButtonPanel(InputState state, CatalogPanelNew panel) {
		super(state);
		this.panel = panel;
	}

	public static boolean areMultiplayerButtonVisible() {
		return !GameServerState.isCreated();
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
		GUIHorizontalButtonTablePane p = new GUIHorizontalButtonTablePane(getState(), 1, 4, Lng.str("Blueprint Options"), this);
		p.onInit();
		p.activeInterface = panel;
		p.addButton(0, 0, Lng.str("Mass Spawn Ships"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					openBattleDialog();
				}
			}

			@Override
			public boolean isOccluded() {
				return !isActive();
			}
		}, null);
		p.addButton(0, 1, Lng.str("Clean up Mob/Admin-Spawned ships in range"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					PlayerGameOkCancelInput confirm = new PlayerGameOkCancelInput("CONFIRM", getState(), Lng.str("Confirm"), Lng.str("This will delete all ships in the area that have\nnot been spawned by a player.\n\nThis action cannot be undone.")) {

						@Override
						public boolean isOccluded() {
							return false;
						}

						@Override
						public void onDeactivate() {
						}

						@Override
						public void pressedOK() {
							getState().getController().sendAdminCommand(AdminCommands.CLEAR_SYSTEM_SHIP_SPAWNS);
							deactivate();
						}
					};
					confirm.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(364);
				}
			}
		}, null);
		p.addButton(0, 2, Lng.str("Manage AI Spawn Groups"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					PlayerWaveManagerInput playerWaveManagerInput = new PlayerWaveManagerInput("AD:MMSAM", getState(), panel);
					playerWaveManagerInput.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(365);
				}
			}
		}, null);
		p.addButton(0, 3, Lng.str("Remove all enemy usable permissions"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					if (getState().isAdmin()) {
						(new PlayerOkCancelInput("CONFIRM", getState(), 300, 200, Lng.str("Confirm"), Lng.str("Do you really want to remove the 'enemy usable' for all entries?")) {

							@Override
							public void pressedOK() {
								for (CatalogPermission p : CatalogAdminOptionsButtonPanel.this.getState().getCatalogManager().getCatalog()) {
									p.setPermission(false, CatalogPermission.P_ENEMY_USABLE);
									CatalogAdminOptionsButtonPanel.this.getState().getCatalogManager().clientRequestCatalogEdit(p);
								}
								CatalogAdminOptionsButtonPanel.this.getState().getController().popupInfoTextMessage(Lng.str("All entries are no longer enemy usable!"), 0);
								deactivate();
							}

							@Override
							public void onDeactivate() {
							}
						}).activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(366);
					} else {
						getState().getController().popupInfoTextMessage(Lng.str("Only admins can do that!"), 0);
					}
				}
			}
		}, null);
		setPos(1, 0, 0);
		attach(p);
	}

	private void openBattleDialog() {
		PlayerGameOkCancelInput main = new PlayerGameOkCancelInput("ADMIN_BATTLE_POPUP", getState(), 740, 400, Lng.str("Battle"), Lng.str("Choose catalog entries and faction to fight each other. (Default Faction ID's: 0 is neutral, -1 is pirate, -2 is Trading Guild)"), FontStyle.small) {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void pressedOK() {
				if (getState().getPlayerInputs().get(getState().getPlayerInputs().size() - 1) == this) {
					for (CatalogBattleRowObject e : scrl.currentList) {
						System.err.println("[ADMIN] SPAWNING: " + e.catId + ", " + e.faction + ", " + e.amount);
						getState().getController().sendAdminCommand(AdminCommands.SPAWN_MOBS, e.catId, e.faction, e.amount);
					}
					deactivate();
				}
			}

			@Override
			public void onDeactivate() {
			}
		};
		main.getInputPanel().setCancelButton(false);
		main.getInputPanel().onInit();
		((GUIDialogWindow) main.getInputPanel().getBackground()).getMainContentPane().setTextBoxHeightLast(UIScale.getUIScale().scale(80));
		((GUIDialogWindow) main.getInputPanel().getBackground()).getMainContentPane().addNewTextBox(UIScale.getUIScale().scale(100));
		scrl = new CatalogBattleScrollableListNew(getState(), ((GUIDialogWindow) main.getInputPanel().getBackground()).getMainContentPane().getContent(1));
		scrl.onInit();
		((GUIDialogWindow) main.getInputPanel().getBackground()).getMainContentPane().getContent(1).attach(scrl);
		GUITextButton addEdit = new GUITextButton(getState(), UIScale.getUIScale().scale(60), UIScale.getUIScale().h, ColorPalette.OK, Lng.str("ADD"), new GUICallback() {

			private int numberValue;

			private CatalogScrollableListNew select;

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse() && getState().getPlayerInputs().size() == 1) {
					final PlayerGameOkCancelInput c = new PlayerGameOkCancelInput("CHSOE_CAT", getState(), UIScale.getUIScale().scale(400), UIScale.getUIScale().scale(300), Lng.str("Choose Blueprint"), "") {

						@Override
						public void onDeactivate() {
						}

						@Override
						public void pressedOK() {
							if (select.selectedSingle != null) {
								CatalogPermission selectedSingle = select.selectedSingle;
								scrl.currentList.add(new CatalogBattleRowObject(selectedSingle.getUid(), 0, numberValue));
								scrl.flagDirty();
								deactivate();
							}
						}
					};
					c.getInputPanel().onInit();
					((GUIDialogWindow) c.getInputPanel().getBackground()).getMainContentPane().setTextBoxHeightLast(UIScale.getUIScale().scale(30));
					((GUIDialogWindow) c.getInputPanel().getBackground()).getMainContentPane().addNewTextBox(UIScale.getUIScale().scale(30));
					GUIActivatableTextBar numberInputBar;
					numberInputBar = new GUIActivatableTextBar(getState(), FontSize.MEDIUM_15, Lng.str("AMOUNT"), ((GUIDialogWindow) c.getInputPanel().getBackground()).getMainContentPane().getContent(0), new TextCallback() {

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
						public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {
						}

						@Override
						public void newLine() {
						}
					}, t -> {
						try {
							numberValue = Integer.parseInt(t.trim());
							return String.valueOf(numberValue);
						} catch (NumberFormatException e) {
						}
						;
						return t;
					});
					numberInputBar.appendText(String.valueOf(1));
					numberInputBar.setPos(0, 0, 0);
					((GUIDialogWindow) c.getInputPanel().getBackground()).getMainContentPane().getContent(0).attach(numberInputBar);
					select = new CatalogScrollableListNew(getState(), ((GUIDialogWindow) c.getInputPanel().background).getMainContentPane().getContent(1), CatalogScrollableListNew.ADMIN, false, true);
					select.onInit();
					((GUIDialogWindow) c.getInputPanel().background).getMainContentPane().getContent(1).attach(select);
					c.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(367);
				}
			}
		});
		GUITextButton clearAll = new GUITextButton(getState(), UIScale.getUIScale().scale(120), UIScale.getUIScale().h, ColorPalette.CANCEL, Lng.str("CLEAR ALL"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse() && getState().getPlayerInputs().size() == 1) {
					scrl.currentList.clear();
					scrl.flagDirty();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(368);
				}
			}
		});
		GUITextButton save = new GUITextButton(getState(), UIScale.getUIScale().scale(80), UIScale.getUIScale().h, ColorPalette.OK, Lng.str("SAVE"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse() && getState().getPlayerInputs().size() == 1) {
					boolean write = CatalogBattleScrollableListNew.write("b0.btl", scrl);
					if (write) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(370);
						getState().getController().popupInfoTextMessage(Lng.str("Saved Mass Spawn"), 0);
					} else {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.ERROR)*/
						AudioController.fireAudioEventID(369);
						getState().getController().popupAlertTextMessage(Lng.str("Error Saving Mass Spawn!\nSee logs!"), 0);
					}
				}
			}
		});
		GUITextButton load = new GUITextButton(getState(), 80, 24, ColorPalette.OK, Lng.str("LOAD"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse() && getState().getPlayerInputs().size() == 1) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(371);
					CatalogBattleScrollableListNew.load("b0.btl", scrl);
					scrl.flagDirty();
				}
			}
		});
		addEdit.setPos(UIScale.getUIScale().smallinset, UIScale.getUIScale().scale(40), 0);
		save.setPos(UIScale.getUIScale().smallinset + addEdit.getWidth() + UIScale.getUIScale().scale(5), UIScale.getUIScale().scale(40), 0);
		load.setPos(UIScale.getUIScale().smallinset + addEdit.getWidth() + UIScale.getUIScale().scale(5) + save.getWidth() + UIScale.getUIScale().scale(5), UIScale.getUIScale().scale(40), 0);
		clearAll.setPos(UIScale.getUIScale().scale(240), UIScale.getUIScale().scale(40), 0);
		main.getInputPanel().getContent().attach(addEdit);
		main.getInputPanel().getContent().attach(clearAll);
		main.getInputPanel().getContent().attach(save);
		main.getInputPanel().getContent().attach(load);
		main.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(372);
	}
}
