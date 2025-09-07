package org.schema.game.client.view.gui.catalog.newcatalog;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.controller.manager.ingame.PlayerGameControlManager;
import org.schema.game.client.controller.manager.ingame.ship.InShipControlManager;
import org.schema.game.client.controller.manager.ingame.ship.WeaponAssignControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.catalog.CatalogManager;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.resource.FileExt;
import org.schema.schine.sound.controller.AudioController;

import javax.vecmath.Vector3f;
import java.io.*;
import java.util.*;

public class CatalogBattleScrollableListNew extends ScrollableTableList<CatalogBattleRowObject> {

	public final ObjectOpenHashSet<CatalogBattleRowObject> currentList = new ObjectOpenHashSet<CatalogBattleRowObject>();

	private static final Vector3f dir = new Vector3f();

	private static final Vector3f dir1 = new Vector3f();

	private static final Vector3f dir2 = new Vector3f();

	public final List<CatalogBattleRowObject> list = new ObjectArrayList<CatalogBattleRowObject>();

	private boolean factionOption;

	public CatalogBattleScrollableListNew(InputState state, GUIElement dependend, boolean factionOption) {
		super(state, 100, 100, dependend);
		setColumnsHeight(UIScale.getUIScale().scale(32));
		this.factionOption = factionOption;
	}

	public CatalogBattleScrollableListNew(GameClientState state, GUIAnchor content) {
		this(state, content, true);
	}

	public static void load(String path, CatalogBattleScrollableListNew l) {
		File f = new FileExt(path);
		DataInputStream dIn = null;
		try {
			dIn = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
			int version = dIn.readInt();
			int size = dIn.readInt();
			l.currentList.clear();
			for (int i = 0; i < size; i++) {
				CatalogBattleRowObject v = new CatalogBattleRowObject(dIn.readUTF(), dIn.readInt(), dIn.readInt());
				l.currentList.add(v);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			try {
				if (dIn != null) {
					dIn.close();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	public static boolean write(String path, CatalogBattleScrollableListNew l) {
		File f = new FileExt(path);
		if (f.isDirectory()) {
			f.delete();
		}
		if (f.getParentFile() != null) {
			f.getParentFile().mkdirs();
		}
		DataOutputStream dOut = null;
		int version = 0;
		try {
			dOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
			dOut.writeInt(version);
			dOut.writeInt(l.currentList.size());
			for (CatalogBattleRowObject e : l.currentList) {
				dOut.writeUTF(e.catId);
				dOut.writeInt(e.faction);
				dOut.writeInt(e.amount);
			}
			return true;
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			try {
				if (dOut != null) {
					dOut.close();
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
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
		addColumn(Lng.str("Name"), 6, (o1, o2) -> o1.catId.compareToIgnoreCase(o2.catId), true);
		addColumn(Lng.str("Faction ID"), 1, (o1, o2) -> o1.faction - o2.faction);
		addColumn(Lng.str("Amount"), 1, (o1, o2) -> o1.amount > o2.amount ? 1 : (o1.amount < o2.amount ? -1 : 0));
		addFixedWidthColumnScaledUI(Lng.str("Options"), 240, (o1, o2) -> 0);
		addTextFilter(new GUIListFilterText<CatalogBattleRowObject>() {

			@Override
			public boolean isOk(String input, CatalogBattleRowObject listElement) {
				return listElement.catId.toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH BY NAME"), FilterRowStyle.FULL);
	}

	@Override
	protected Collection<CatalogBattleRowObject> getElementList() {
		updateList();
		return list;
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<CatalogBattleRowObject> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final FactionManager factionManager = getState().getGameState().getFactionManager();
		final CatalogManager catalogManager = getState().getGameState().getCatalogManager();
		final PlayerState player = getState().getPlayer();
		int i = 0;
		for (final CatalogBattleRowObject f : collection) {
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
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
					return String.valueOf(f.faction);
				}
			});
			GUIClippedRow nameAnchorP = new GUIClippedRow(getState());
			nameAnchorP.attach(nameText);
			nameText.setTextSimple(f.catId);
			int heightInset = 5;
			nameText.getPos().y = heightInset;
			amount.getPos().y = heightInset;
			fidText.getPos().y = heightInset;
			assert (!nameText.getText().isEmpty());
			assert (!amount.getText().isEmpty());
			GUIAnchor optionsAnchorP = new GUIAnchor(getState(), 120, getDefaultColumnsHeight());
			GUITextButton changeAmountButton = new GUITextButton(getState(), 80, 22, ColorPalette.OK, Lng.str("AMOUNT"), new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						openAmountDialog(f);
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
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.DELETE)*/
						AudioController.fireAudioEventID(373);
						apply(f, 0, f.faction);
					}
				}
			});
			changeAmountButton.setPos(UIScale.getUIScale().smallinset, UIScale.getUIScale().smallinset, 0);
			changeFactionButton.setPos(2 + changeAmountButton.getWidth() + 6, 2, 0);
			delButton.setPos(2 + changeFactionButton.getWidth() + 6 + changeAmountButton.getWidth() + 6, 2, 0);
			optionsAnchorP.attach(changeAmountButton);
			if (factionOption) {
				optionsAnchorP.attach(changeFactionButton);
			}
			optionsAnchorP.attach(delButton);
			final InventoryStashRow r = new InventoryStashRow(getState(), f, nameAnchorP, fidText, amount, optionsAnchorP);
			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
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
		for (CatalogBattleRowObject e : currentList) {
			list.add(e);
		}
	}

	public void apply(CatalogBattleRowObject f, int dirtyCount, int factionId) {
		if (f != null) {
			if (dirtyCount == 0) {
				currentList.remove(f);
			} else {
				currentList.remove(f);
				f.amount = dirtyCount;
				f.faction = factionId;
				currentList.add(f);
			}
			dirtyCount = 0;
			flagDirty();
		}
	}

	private void openAmountDialog(final CatalogBattleRowObject f) {
		PlayerGameTextInput p = new PlayerGameTextInput("sstionScrollableListNew_AMOUNT", getState(), 8, Lng.str("Amount of Ships"), Lng.str("How many ships?"), String.valueOf(f.amount)) {

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
					int parseInt = Integer.parseInt(entry);
					if (parseInt >= 0) {
						CatalogBattleScrollableListNew.this.apply(f, parseInt, f.faction);
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
		AudioController.fireAudioEventID(374);
	}

	private void openFactionDialog(final CatalogBattleRowObject f) {
		PlayerGameTextInput p = new PlayerGameTextInput("sstionScrollableListNew_AMOUNT", getState(), 16, Lng.str("Faction ID"), Lng.str("Faction ID"), String.valueOf(f.faction)) {

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
					CatalogBattleScrollableListNew.this.apply(f, f.amount, parseInt);
					return true;
				} catch (NumberFormatException e) {
					setErrorMessage(Lng.str("Must be a number!"));
				}
				return false;
			}

			@Override
			public void onDeactivate() {
			}
		};
		p.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(375);
	}

	private class InventoryStashRow extends Row {

		public InventoryStashRow(InputState state, CatalogBattleRowObject f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}
	}
}
