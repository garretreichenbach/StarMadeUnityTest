package org.schema.game.client.view.gui.faction.newfaction;

import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;

import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.player.faction.FactionPermission;
import org.schema.game.common.data.player.faction.FactionPermission.PermType;
import org.schema.game.common.data.player.faction.FactionRole;
import org.schema.game.common.data.player.faction.FactionRoles;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUICheckBoxTextPair;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class FactionRolesScrollableListNew extends ScrollableTableList<FactionRole> {

	private final ObjectArrayList<FactionRole> r = new ObjectArrayList<FactionRole>();

	private Faction faction;

	private FactionRoles rolesInstance;

	public FactionRolesScrollableListNew(InputState state, GUIElement p, Faction f) {
		super(state, 10, 10, p);
		this.faction = f;
		rolesInstance = new FactionRoles();
		rolesInstance.factionId = faction.getIdFaction();
		rolesInstance.apply(faction.getRoles());
		((GameClientState) getState()).getFactionManager().obs.addObserver(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		((GameClientState) getState()).getFactionManager().obs.deleteObserver(this);
		super.cleanUp();
	}

	@Override
	public void initColumns() {
		addColumn(Lng.str("Name"), 7, (o1, o2) -> o1.name.compareToIgnoreCase(o2.name));
	}

	@Override
	protected Collection<FactionRole> getElementList() {
		r.clear();
		for (int i = 0; i < rolesInstance.getRoles().length; i++) {
			r.add(rolesInstance.getRoles()[i]);
		}
		return r;
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<FactionRole> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final FactionManager factionManager = ((GameClientState) getState()).getGameState().getFactionManager();
		final PlayerState player = ((GameClientState) getState()).getPlayer();
		int i = 0;
		for (final FactionRole f : collection) {
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			nameText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f.name;
				}
			});
			nameText.getPos().y = 4;
			FactionRow r = new FactionRow(getState(), f, nameText);
			r.expanded = new GUIElementList(getState());
			GUITextButton nameEditButton = new GUITextButton(getState(), 100, 24, ColorPalette.OK, Lng.str("CHANGE NAME"), new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						PlayerGameTextInput in = new PlayerGameTextInput("FactionRolesScrollableListNew_CHANGE_NAME", (GameClientState) getState(), 16, Lng.str("Edit Role Name"), Lng.str("Enter a new name for this Role"), f.name) {

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
							}

							@Override
							public boolean onInput(String entry) {
								f.name = entry;
								/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
								AudioController.fireAudioEventID(479);
								return true;
							}
						};
						in.setInputChecker((entry, callback) -> {
							if (entry.length() >= 3 && entry.length() <= 16) {
								if (Pattern.matches("[a-zA-Z0-9 _-]+", entry)) {
									return true;
								} else {
									System.err.println("MATCH FOUND ^ALPHANUMERIC");
								}
							}
							callback.onFailedTextCheck(Lng.str("Please only alphanumeric (and space, _, -) values \nand between 3 and 16 long!"));
							return false;
						});
						in.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(480);
					}
				}

				@Override
				public boolean isOccluded() {
					return !isActive();
				}
			});
			GUIElementList permissionList = new GUIElementList(getState());
			for (int g = 0; g < FactionPermission.PermType.values().length; g++) {
				final PermType permType = FactionPermission.PermType.values()[g];
				if (permType.active) {
					GUICheckBoxTextPair p0 = new GUICheckBoxTextPair(getState(), permType.getName(), 200, 24) {

						@Override
						public void activate() {
							if (f.index == rolesInstance.getRoles().length - 1) {
								((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Cannot change permission\nof admin rank!"), 0);
							} else {
								f.setPermission(permType, true);
							}
						}

						@Override
						public void deactivate() {
							if (f.index == rolesInstance.getRoles().length - 1) {
								((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Cannot change permission\nof admin rank!"), 0);
							} else {
								f.setPermission(permType, false);
							}
						}

						@Override
						public boolean isActivated() {
							return f.hasPermission(permType);
						}
					};
					p0.setPos(4, 0, 0);
					permissionList.addWithoutUpdate(new GUIListElement(p0, p0, getState()));
				}
			}
			permissionList.updateDim();
			GUIAnchor c = new GUIAnchor(getState(), 100, permissionList.height);
			c.attach(permissionList);
			c.attach(nameEditButton);
			nameEditButton.setPos(0, c.getHeight(), 0);
			r.expanded.add(new GUIListElement(c, c, getState()));
			r.onInit();
			mainList.addWithoutUpdate(r);
		}
		mainList.updateDim();
	}

	public FactionRoles getRoles() {
		return rolesInstance;
	}

	private class FactionRow extends Row {

		public FactionRow(InputState state, FactionRole f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}
	}
}
