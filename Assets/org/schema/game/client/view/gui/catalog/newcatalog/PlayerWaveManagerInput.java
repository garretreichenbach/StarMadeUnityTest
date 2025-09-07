package org.schema.game.client.view.gui.catalog.newcatalog;

import java.util.List;

import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.data.player.catalog.CatalogWavePermission;
import org.schema.schine.common.OnInputChangedCallback;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.font.FontStyle;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActivatableTextBar;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.sound.controller.AudioController;

public class PlayerWaveManagerInput extends PlayerGameOkCancelInput {

	private int factionValue;

	private int diff;

	private CatalogBattleScrollableListNew scrl;

	private WaveScrollableListNew wfl;

	public PlayerWaveManagerInput(String windowid, GameClientState state, GUIElement panel) {
		super(windowid, state, UIScale.getUIScale().scale(500), UIScale.getUIScale().scale(500), Lng.str("AI Wave manager"), "");
		getInputPanel().setCancelButton(false);
		getInputPanel().onInit();
		((GUIDialogWindow) getInputPanel().getBackground()).getMainContentPane().setTextBoxHeightLast(UIScale.getUIScale().scale(100));
		((GUIDialogWindow) getInputPanel().getBackground()).getMainContentPane().addNewTextBox(UIScale.getUIScale().scale(100));
		wfl = new WaveScrollableListNew(getState(), ((GUIDialogWindow) getInputPanel().getBackground()).getMainContentPane().getContent(1));
		wfl.onInit();
		((GUIDialogWindow) getInputPanel().getBackground()).getMainContentPane().getContent(1).attach(wfl);
		GUITextButton addEdit = new GUITextButton(getState(), UIScale.getUIScale().scale(60), UIScale.getUIScale().h, ColorPalette.OK, Lng.str("ADD"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse() && getState().getPlayerInputs().get(getState().getPlayerInputs().size() - 1) == PlayerWaveManagerInput.this) {
					openBattleDialog();
				}
			}
		});
		addEdit.setPos(5, 5, 0);
		((GUIDialogWindow) getInputPanel().getBackground()).getMainContentPane().getContent(0).attach(addEdit);
	}

	@Override
	public void onDeactivate() {
	}

	private void openBattleDialog() {
		final PlayerGameOkCancelInput main = new PlayerGameOkCancelInput("ADMIN_BATTLE_POPUP", getState(), UIScale.getUIScale().scale(640), UIScale.getUIScale().scale(400), Lng.str("Battle"), Lng.str("Choose catalog entries and faction to fight each other. (Default Faction ID's: 0 is neutral, -1 is pirate, -2 is Trading Guild)"), FontStyle.small) {

			@Override
			public boolean isOccluded() {
				return getState().getPlayerInputs().get(getState().getPlayerInputs().size() - 1) != this;
			}

			@Override
			public void pressedOK() {
				List<CatalogBattleRowObject> a = scrl.list;
				for (CatalogBattleRowObject c : a) {
					for (CatalogPermission s : getState().getCatalogManager().getCatalog()) {
						if (s.getUid().equals(c.catId)) {
							CatalogWavePermission pp = new CatalogWavePermission();
							pp.amount = c.amount;
							pp.factionId = factionValue;
							pp.difficulty = (short) diff;
							s.wavePermissions.remove(pp);
							s.wavePermissions.add(pp);
							s.changeFlagForced = true;
							getState().getCatalogManager().clientRequestCatalogEdit(s);
							System.err.println("ADDED WAVE PERMISSION: " + pp + " TO " + s);
						}
					}
				}
				wfl.flagDirty();
				deactivate();
			}

			@Override
			public void onDeactivate() {
			}
		};
		main.getInputPanel().setCancelButton(false);
		main.getInputPanel().onInit();
		((GUIDialogWindow) main.getInputPanel().getBackground()).getMainContentPane().setTextBoxHeightLast(UIScale.getUIScale().scale(120));
		((GUIDialogWindow) main.getInputPanel().getBackground()).getMainContentPane().addNewTextBox(UIScale.getUIScale().scale(100));
		scrl = new CatalogBattleScrollableListNew(getState(), ((GUIDialogWindow) main.getInputPanel().getBackground()).getMainContentPane().getContent(1), false);
		scrl.onInit();
		((GUIDialogWindow) main.getInputPanel().getBackground()).getMainContentPane().getContent(1).attach(scrl);
		GUITextButton addEdit = new GUITextButton(getState(), 60, 24, ColorPalette.OK, Lng.str("ADD"), new GUICallback() {

			private int numberValue = 1;

			private CatalogScrollableListNew select;

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse() && getState().getPlayerInputs().get(getState().getPlayerInputs().size() - 1) == main) {
					final PlayerGameOkCancelInput c = new PlayerGameOkCancelInput("CHSOE_CAT", getState(), 400, 300, Lng.str("Choose Blueprint"), "") {

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
					GUIActivatableTextBar numberInputBar = new GUIActivatableTextBar(getState(), FontSize.MEDIUM_15, Lng.str("AMOUNT"), ((GUIDialogWindow) c.getInputPanel().getBackground()).getMainContentPane().getContent(0), new TextCallback() {

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
						} catch (NumberFormatException e) {
						}
						;
						return t;
					});
					numberInputBar.setPos(0, 0, 0);
					((GUIDialogWindow) c.getInputPanel().getBackground()).getMainContentPane().getContent(0).attach(numberInputBar);
					select = new CatalogScrollableListNew(getState(), ((GUIDialogWindow) c.getInputPanel().background).getMainContentPane().getContent(1), CatalogScrollableListNew.ADMIN, false, true);
					select.onInit();
					((GUIDialogWindow) c.getInputPanel().background).getMainContentPane().getContent(1).attach(select);
					c.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(407);
				}
			}
		});
		GUITextButton clearAll = new GUITextButton(getState(), 120, 24, ColorPalette.CANCEL, Lng.str("CLEAR ALL"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse() && getState().getPlayerInputs().size() == 1) {
					scrl.currentList.clear();
					scrl.flagDirty();
				}
			}
		});
		GUIActivatableTextBar faction = new GUIActivatableTextBar(getState(), FontSize.MEDIUM_15, Lng.str("FACTION"), main.getInputPanel().getContent(), new TextCallback() {

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
				factionValue = Integer.parseInt(t.trim());
			} catch (NumberFormatException e) {
			}
			;
			return t;
		});
		GUIActivatableTextBar dInputBar = new GUIActivatableTextBar(getState(), FontSize.MEDIUM_15, Lng.str("DIFFICULTY"), main.getInputPanel().getContent(), new TextCallback() {

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
				diff = Integer.parseInt(t.trim());
			} catch (NumberFormatException e) {
			}
			;
			return t;
		});
		faction.setPos(0, 60, 0);
		dInputBar.setPos(0, 60 + 26, 0);
		main.getInputPanel().getContent().attach(faction);
		main.getInputPanel().getContent().attach(dInputBar);
		addEdit.setPos(2, 30, 0);
		clearAll.setPos(240, 30, 0);
		main.getInputPanel().getContent().attach(addEdit);
		main.getInputPanel().getContent().attach(clearAll);
		main.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(408);
	}

	@Override
	public void pressedOK() {
		deactivate();
	}
}
