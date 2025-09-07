package org.schema.game.client.view.gui.messagelog.messagelognew;

import org.schema.game.client.controller.PlayerMessageLogPlayerInput;
import org.schema.game.client.data.ClientMessageLog;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIMainWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

public class MessageLogPanelNew extends GUIMainWindow implements GUIActiveInterface {
	private ClientMessageLog messageLog;

	//	private GUIContentPane personalTab;
	private boolean init;
	private GUIContentPane generalTab;
	public MessageLogPanelNew(InputState state, PlayerMessageLogPlayerInput mainMenu) {
		super(state, 800, 500, "MessageLogPanelNew");
		this.messageLog = ((GameClientState) state).getMessageLog();
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public void draw() {
		if (!init) {
			onInit();

		}
		super.draw();
	}

	@Override
	public void onInit() {
		super.onInit();

		recreateTabs();
		orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_VERTICAL_MIDDLE);

		init = true;
	}

	@Override
	public GameClientState getState() {
		return ((GameClientState) super.getState());
	}

	public void recreateTabs() {
		Object beforeTab = null;
		if (getSelectedTab() < getTabs().size()) {
			beforeTab = getTabs().get(getSelectedTab()).getTabName();
		}
		clearTabs();

		generalTab = addTab(Lng.str("GENERAL"));

		createGeneralPane();

		if (beforeTab != null) {
			for (int i = 0; i < getTabs().size(); i++) {
				if (getTabs().get(i).getTabName().equals(beforeTab)) {
					setSelectedTab(i);
					break;
				}
			}
		}
	}

	private void createGeneralPane() {
		generalTab.setTextBoxHeightLast(UIScale.getUIScale().scale(28));

		MessageLogScrollableListNew m = new MessageLogScrollableListNew(getState(), generalTab.getContent(0), messageLog);
		m.onInit();
		generalTab.getContent(0).attach(m);
	}

	public PlayerState getOwnPlayer() {
		return MessageLogPanelNew.this.getState().getPlayer();
	}

	public Faction getOwnFaction() {
		return MessageLogPanelNew.this.getState().getFactionManager().getFaction(getOwnPlayer().getFactionId());
	}

}
