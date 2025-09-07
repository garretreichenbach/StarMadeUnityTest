package org.schema.game.client.view.gui.catalog.newcatalog;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.vecmath.Vector3f;

import org.schema.game.client.controller.CatalogChangeListener;
import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.controller.manager.ingame.PlayerGameControlManager;
import org.schema.game.client.controller.manager.ingame.ship.InShipControlManager;
import org.schema.game.client.controller.manager.ingame.ship.WeaponAssignControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.catalog.CatalogManager;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.data.player.catalog.CatalogWavePermission;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.font.FontStyle;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIChangeListener;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActivatableTextBar;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class WaveScrollableListNew extends ScrollableTableList<WaveRow> implements GUIChangeListener, CatalogChangeListener {

	private static final Vector3f dir = new Vector3f();

	private static final Vector3f dir1 = new Vector3f();

	private static final Vector3f dir2 = new Vector3f();

	private final List<WaveRow> list = new ObjectArrayList<WaveRow>();

	private CatalogBattleScrollableListNew scrl;

	public WaveScrollableListNew(InputState state, GUIElement dependend) {
		super(state, 100, 100, dependend);
		setColumnsHeight(UIScale.getUIScale().scale(32));
		getState().getCatalogManager().listeners.add(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		getState().getCatalogManager().listeners.remove(this);
		super.cleanUp();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();
	}

	@Override
	public void initColumns() {
		addColumn(Lng.str("First"), 6, (o1, o2) -> o1.permissions.get(0).getUid().compareTo(o2.permissions.get(0).getUid()));
		addColumn(Lng.str("Difficulty"), 1, (o1, o2) -> o1.difficulty - o2.difficulty);
		addColumn(Lng.str("Faction ID"), 1, (o1, o2) -> o1.factionId - o2.factionId);
		addColumn(Lng.str("Ships"), 1, (o1, o2) -> o1.amount > o2.amount ? 1 : (o1.amount < o2.amount ? -1 : 0));
		addFixedWidthColumnScaledUI(Lng.str("Options"), 280, (o1, o2) -> 0);
		addTextFilter(new GUIListFilterText<WaveRow>() {

			@Override
			public boolean isOk(String input, WaveRow listElement) {
				try {
					int parseInt = Integer.parseInt(input);
					return listElement.factionId == parseInt;
				} catch (Exception e) {
					return true;
				}
			}
		}, Lng.str("SEARCH BY FACTION ID"), FilterRowStyle.FULL);
	}

	@Override
	protected Collection<WaveRow> getElementList() {
		updateList();
		return list;
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<WaveRow> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final FactionManager factionManager = getState().getGameState().getFactionManager();
		final CatalogManager catalogManager = getState().getGameState().getCatalogManager();
		final PlayerState player = getState().getPlayer();
		int i = 0;
		for (final WaveRow f : collection) {
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			GUITextOverlayTable difficultyText = new GUITextOverlayTable(getState());
			GUITextOverlayTable fidText = new GUITextOverlayTable(getState());
			GUITextOverlayTable amount = new GUITextOverlayTable(getState());
			amount.setTextSimple(new Object() {

				@Override
				public String toString() {
					return String.valueOf(f.amount);
				}
			});
			fidText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return String.valueOf(f.factionId);
				}
			});
			GUIClippedRow nameAnchorP = new GUIClippedRow(getState());
			nameAnchorP.attach(nameText);
			GUIClippedRow diffAnchorP = new GUIClippedRow(getState());
			diffAnchorP.attach(difficultyText);
			nameText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f.permissions.get(0).getUid();
				}
			});
			difficultyText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return String.valueOf(f.difficulty);
				}
			});
			int heightInset = 5;
			difficultyText.getPos().y = heightInset;
			amount.getPos().y = heightInset;
			fidText.getPos().y = heightInset;
			assert (!difficultyText.getText().isEmpty());
			assert (!amount.getText().isEmpty());
			GUIAnchor optionsAnchorP = new GUIAnchor(getState(), 120, getDefaultColumnsHeight());
			GUITextButton changeDiffButton = new GUITextButton(getState(), 80, 22, ColorPalette.OK, Lng.str("DIFFICULTY"), new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						openChangeDifficultyDialog(f);
					}
				}

				@Override
				public boolean isOccluded() {
					return !isActive();
				}
			});
			GUITextButton editButton = new GUITextButton(getState(), 60, 22, ColorPalette.OK, Lng.str("EDIT"), new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						openBattleDialog(f);
					}
				}

				@Override
				public boolean isOccluded() {
					return !isActive();
				}
			});
			GUITextButton changeFactionButton = new GUITextButton(getState(), 80, 22, ColorPalette.OK, Lng.str("FACTION"), new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						openFactionDialog(f);
					}
				}
			});
			GUITextButton delButton = new GUITextButton(getState(), 30, 22, ColorPalette.CANCEL, Lng.str("DEL"), new GUICallback() {

				@Override
				public boolean isOccluded() {
					return false;
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse() && getState().getPlayerInputs().size() == 1) {
						delete(f);
					}
				}
			});
			changeDiffButton.setPos(UIScale.getUIScale().smallinset, UIScale.getUIScale().smallinset, 0);
			editButton.setPos(2 + changeDiffButton.getWidth() + 6, 2, 0);
			changeFactionButton.setPos(2 + editButton.getWidth() + 2 + changeDiffButton.getWidth() + 6, 2, 0);
			delButton.setPos(2 + editButton.getWidth() + 2 + changeFactionButton.getWidth() + 6 + changeDiffButton.getWidth() + 6, 2, 0);
			optionsAnchorP.attach(changeDiffButton);
			optionsAnchorP.attach(editButton);
			optionsAnchorP.attach(changeFactionButton);
			optionsAnchorP.attach(delButton);
			final InventoryStashRow r = new InventoryStashRow(getState(), f, nameAnchorP, diffAnchorP, fidText, amount, optionsAnchorP);
			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}

	private void openBattleDialog(final WaveRow f) {
		f.refresh(getState());
		scrl = null;
		final PlayerGameOkCancelInput main = new PlayerGameOkCancelInput("ADMIN_BATTLE_POPUP", getState(), 640, 400, Lng.str("Battle"), Lng.str("Choose catalog entries and faction to fight each other. (Default Faction ID's: 0 is neutral, -1 is pirate, -2 is Trading Guild)"), FontStyle.small) {

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
							pp.factionId = f.factionId;
							pp.difficulty = f.difficulty;
							s.wavePermissions.remove(pp);
							s.wavePermissions.add(pp);
							s.changeFlagForced = true;
							getState().getCatalogManager().clientRequestCatalogEdit(s);
							System.err.println("ADDED WAVE PERMISSION: " + pp + " TO " + s);
						}
					}
				}
				flagDirty();
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
		for (CatalogPermission s : f.permissions) {
			for (CatalogWavePermission p : s.wavePermissions) {
				if (p.difficulty == f.difficulty && p.factionId == f.factionId) {
					CatalogBattleRowObject br = new CatalogBattleRowObject(s.getUid(), p.factionId, p.amount);
					scrl.currentList.add(br);
				}
			}
		}
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
					AudioController.fireAudioEventID(409);
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
		addEdit.setPos(2, 30, 0);
		clearAll.setPos(240, 30, 0);
		main.getInputPanel().getContent().attach(addEdit);
		main.getInputPanel().getContent().attach(clearAll);
		main.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(410);
	}

	public boolean isPlayerAdmin() {
		return getState().getPlayer().getNetworkObject().isAdminClient.get();
	}

	public boolean canEdit(CatalogPermission f) {
		return f.ownerUID.toLowerCase(Locale.ENGLISH).equals(getState().getPlayer().getName().toLowerCase(Locale.ENGLISH)) || isPlayerAdmin();
	}

	public WeaponAssignControllerManager getAssignWeaponControllerManager() {
		return getPlayerGameControlManager().getWeaponControlManager();
	}

	public InShipControlManager getInShipControlManager() {
		return getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager();
	}

	@Override
	public GameClientState getState() {
		return (GameClientState) super.getState();
	}

	private PlayerGameControlManager getPlayerGameControlManager() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager();
	}

	public void updateList() {
		list.clear();
		GameClientState state = getState();
		WaveRow.get(state, list);
	}

	public void apply(WaveRow f, short difficulty, int factionId) {
		if (f != null) {
			GameClientState state = getState();
			Collection<CatalogPermission> catalog = state.getCatalogManager().getCatalog();
			for (CatalogPermission p : f.permissions) {
				CatalogWavePermission current = new CatalogWavePermission();
				current.difficulty = f.difficulty;
				current.factionId = f.factionId;
				CatalogWavePermission exiting = new CatalogWavePermission();
				exiting.difficulty = difficulty;
				exiting.factionId = factionId;
				boolean chaged = p.wavePermissions.remove(exiting);
				for (CatalogWavePermission m : p.wavePermissions) {
					if (m.equals(current)) {
						m.difficulty = difficulty;
						m.factionId = factionId;
						chaged = true;
					}
				}
				if (chaged) {
					p.changeFlagForced = true;
					getState().getCatalogManager().clientRequestCatalogEdit(p);
				}
			}
			flagDirty();
		}
	}

	private void delete(WaveRow f) {
		if (f != null) {
			GameClientState state = getState();
			Collection<CatalogPermission> catalog = state.getCatalogManager().getCatalog();
			for (CatalogPermission p : f.permissions) {
				CatalogWavePermission current = new CatalogWavePermission();
				current.difficulty = f.difficulty;
				current.factionId = f.factionId;
				p.wavePermissions.remove(current);
				p.changeFlagForced = true;
				getState().getCatalogManager().clientRequestCatalogEdit(p);
			}
			flagDirty();
		}
	}

	private void openChangeDifficultyDialog(final WaveRow f) {
		PlayerGameTextInput p = new PlayerGameTextInput("sstionScrollableListNew_AMOUNT", getState(), 8, Lng.str("Difficulty"), Lng.str("How many ships?"), String.valueOf(f.amount)) {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}

			@Override
			public void onFailedTextCheck(String msg) {
			}

			@Override
			public void onDeactivate() {
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
				return null;
			}

			@Override
			public boolean onInput(String entry) {
				try {
					short parseInt = Short.parseShort(entry);
					if (parseInt >= 0) {
						WaveScrollableListNew.this.apply(f, parseInt, f.factionId);
						return true;
					} else {
						setErrorMessage(Lng.str("Must be positive!"));
					}
				} catch (NumberFormatException e) {
					setErrorMessage(Lng.str("Must be a number!"));
				}
				return false;
			}
		};
		p.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(411);
	}

	private void openFactionDialog(final WaveRow f) {
		PlayerGameTextInput p = new PlayerGameTextInput("sstionScrollableListNew_AMOUNT", getState(), 8, Lng.str("Faction ID"), Lng.str("Faction ID"), String.valueOf(f.factionId)) {

			@Override
			public boolean isOccluded() {
				return false;
			}

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
			public boolean onInput(String entry) {
				try {
					int parseInt = Integer.parseInt(entry);
					WaveScrollableListNew.this.apply(f, f.difficulty, parseInt);
					return true;
				} catch (NumberFormatException e) {
					setErrorMessage("Must be number!");
				}
				return false;
			}

			@Override
			public void onDeactivate() {
			}
		};
		p.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(412);
	}

	private class InventoryStashRow extends Row {

		public InventoryStashRow(InputState state, WaveRow f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}
	}

	@Override
	public void onCatalogChanged() {
		onChange(false);
	}
}
