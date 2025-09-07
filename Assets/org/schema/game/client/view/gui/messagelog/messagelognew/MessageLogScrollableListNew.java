package org.schema.game.client.view.gui.messagelog.messagelognew;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import javax.vecmath.Vector4f;

import org.schema.game.client.controller.manager.ingame.PlayerGameControlManager;
import org.schema.game.client.controller.manager.ingame.ship.InShipControlManager;
import org.schema.game.client.controller.manager.ingame.ship.WeaponAssignControllerManager;
import org.schema.game.client.data.ClientMessageLog;
import org.schema.game.client.data.ClientMessageLogEntry;
import org.schema.game.client.data.ClientMessageLogType;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.catalog.CatalogManager;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.CreateGUIElementInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterDropdown;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTableDropDown;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.graphicsengine.forms.gui.newgui.config.GuiDateFormats;
import org.schema.schine.input.InputState;

public class MessageLogScrollableListNew extends ScrollableTableList<ClientMessageLogEntry> {

	private ClientMessageLog messageLog;

	public MessageLogScrollableListNew(InputState state, GUIElement p, ClientMessageLog messageLog) {
		super(state, 100, 100, p);
		//		columnsHeight = 32;
		this.messageLog = messageLog;
		this.messageLog.addObserver(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		super.cleanUp();

		this.messageLog.deleteObserver(this);
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


		addFixedWidthColumnScaledUI(Lng.str("Type"), 80, (o1, o2) -> o1.getType().name().compareToIgnoreCase(o2.getType().name()));
		addFixedWidthColumnScaledUI(Lng.str("Date"), 140, (o1, o2) -> o1.getTimestamp() > o2.getTimestamp() ? 1 : (o1.getTimestamp() < o2.getTimestamp() ? -1 : 0), true);
		addColumn(Lng.str("From"), 1, (o1, o2) -> o1.getFrom().compareToIgnoreCase(o2.getFrom()));

		addColumn(Lng.str("Message"), 8, (o1, o2) -> o1.getMessage().compareToIgnoreCase(o2.getMessage()));

		addDropdownFilter(new GUIListFilterDropdown<ClientMessageLogEntry, ClientMessageLogType>(ClientMessageLogType.values()) {
			@Override
			public boolean isOk(ClientMessageLogType input, ClientMessageLogEntry f) {
				return f.getType() == input;
			}
		}, new CreateGUIElementInterface<ClientMessageLogType>() {
			@Override
			public GUIElement create(ClientMessageLogType o) {
				GUIAnchor c = new GUIAnchor(getState(), 10, UIScale.getUIScale().h);
				GUITextOverlayTableDropDown a = new GUITextOverlayTableDropDown(10, 10, getState());
				a.setTextSimple(o.getName());
				a.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
				c.setUserPointer(o);
				c.attach(a);
				return c;
			}

			@Override
			public GUIElement createNeutral() {
				GUIAnchor c = new GUIAnchor(getState(), 10, UIScale.getUIScale().h);
				GUITextOverlayTableDropDown a = new GUITextOverlayTableDropDown(10, 10, getState());
				a.setTextSimple(Lng.str("Filter By Type (off)"));
				a.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
				c.attach(a);
				return c;
			}
		}, FilterRowStyle.FULL);

		addTextFilter(new GUIListFilterText<ClientMessageLogEntry>() {

			@Override
			public boolean isOk(String input,
			                    ClientMessageLogEntry listElement) {
				return listElement.getFrom().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH BY FROM"), FilterRowStyle.LEFT);
		addTextFilter(new GUIListFilterText<ClientMessageLogEntry>() {

			@Override
			public boolean isOk(String input,
			                    ClientMessageLogEntry listElement) {
				return listElement.getMessage().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH BY MESSAGE"), FilterRowStyle.RIGHT);

	}

	@Override
	protected Collection<ClientMessageLogEntry> getElementList() {
		return messageLog.getLog();
	}

	@Override
	public void updateListEntries(GUIElementList mainList,
	                              Set<ClientMessageLogEntry> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);

		final FactionManager factionManager = getState().getGameState().getFactionManager();
		final CatalogManager catalogManager = getState().getGameState().getCatalogManager();
		final PlayerState player = getState().getPlayer();
		int i = 0;

		for (final ClientMessageLogEntry f : collection) {

			GUITextOverlayTable typeText = new GUITextOverlayTable(getState());
			GUITextOverlayTable dateText = new GUITextOverlayTable(getState());
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			GUITextOverlayTable messageText = new GUITextOverlayTable(getState());

			GUIClippedRow nameAnchorP = new GUIClippedRow(getState());
			nameAnchorP.attach(nameText);

			GUIClippedRow messageAnchorP = new GUIClippedRow(getState());
			messageAnchorP.attach(messageText);

			final Vector4f tint = new Vector4f(1, 1, 1, 1);
			typeText.setColor(f.getType().color);

			messageText.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
			nameText.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
			typeText.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
			dateText.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);

			messageText.setTextSimple(f.getMessage());

			nameText.setTextSimple(f.getFrom());

			typeText.setTextSimple(f.getType().name().toLowerCase(Locale.ENGLISH));

			dateText.setTextSimple(GuiDateFormats.messageLogTime.format(f.getTimestamp()));

			final MessageRow r = new MessageRow(getState(), f, typeText, dateText, nameAnchorP, messageAnchorP);

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

	private class MessageRow extends Row {


		public MessageRow(InputState state, ClientMessageLogEntry f, GUIElement... elements) {
			super(state, f, elements);

			this.highlightSelect = true;
		}


	}

}
