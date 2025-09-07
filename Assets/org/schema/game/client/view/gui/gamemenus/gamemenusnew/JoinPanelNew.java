package org.schema.game.client.view.gui.gamemenus.gamemenusnew;

import org.schema.game.client.controller.JoinMenu;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIMainWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

public class JoinPanelNew extends GUIMainWindow implements GUIActiveInterface {

	private JoinMenu joinMenu;
	//	private GUIContentPane personalTab;
	private boolean init;
	private GUIContentPane generalTab;
	public JoinPanelNew(InputState state, JoinMenu joinMenu) {
		super(state, 400, 170, "JoinPanelNew");
		this.joinMenu = joinMenu;
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
		GUIHorizontalButtonTablePane joinButtons = new GUIHorizontalButtonTablePane(getState(), 1, 1, generalTab.getContent(0));
		joinButtons.onInit();
		joinButtons.addButton(0, 0, Lng.str("SPAWN"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					joinMenu.pressedJoin();
				}
			}			@Override
			public boolean isOccluded() {
				return !isActive();
			}


		}, null);
		generalTab.getContent(0).attach(joinButtons);

		generalTab.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);

		GUIHorizontalButtonTablePane exitButtons = new GUIHorizontalButtonTablePane(getState(), 1, 1, generalTab.getContent(1));
		exitButtons.onInit();
		exitButtons.addButton(0, 0, Lng.str("EXIT"), HButtonType.BUTTON_RED_MEDIUM, new GUICallback() {
			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					joinMenu.pressedExit();
				}
			}

		}, null);
		generalTab.getContent(1).attach(exitButtons);

		int added = 2;
		if (getState().getGameState() != null && !getState().getGameState().getState().isPassive()) {
			String msg = getState().getGameState().getNetworkObject().serverMessage.get();
			if (msg.length() > 0) {
				generalTab.addNewTextBox(UIScale.getUIScale().scale(300));
				GUIScrollablePanel p = new GUIScrollablePanel(10, 10, generalTab.getContent(added), getState());
				p.setScrollable(GUIScrollablePanel.SCROLLABLE_HORIZONTAL | GUIScrollablePanel.SCROLLABLE_VERTICAL);

				final GUITextOverlay l = new GUITextOverlay(FontSize.SMALL_14, getState());
				l.setTextSimple((getOwnPlayer().getLastDiedMessage().length() > 0 ? getOwnPlayer().getLastDiedMessage() + "\n\n" : "") + msg);
				l.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
				l.onInit();
				l.updateTextSize();
				final GUIAnchor anc = new GUIAnchor(getState(), 10, 10) {

					/* (non-Javadoc)
					 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#draw()
					 */
					@Override
					public void draw() {
						setWidth(l.getMaxLineWidth());
						setHeight(l.getTextHeight());
						super.draw();
					}

				};

				anc.attach(l);
				p.setContent(anc);
				generalTab.getContent(added).attach(p);
				added++;
			}
			setHeight(getHeight() + 60 + 16);
		}

//		if( > 0){
//			generalTab.addNewTextBox(UIScale.getUIScale().scale(100));
//			GUIScrollablePanel p = new GUIScrollablePanel(10, 10, generalTab.getContent(added), getState());
//			GUIAncor anc = new GUIAncor(getState(), 10, 10);
//
//			GUITextOverlay l = new GUITextOverlay(10, 10, FontSize.SMALL, getState());
//			l.setTextSimple(getOwnPlayer().getLastDiedMessage());
//			l.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
//			l.onInit();
//			l.updateTextSize();
//			anc.setWidth(l.getMaxLineWidth());
//			anc.setHeight(l.getTextHeight());
//			anc.attach(l);
//
//			generalTab.getContent(added).attach(anc);
//			added++;
//			setHeight(getHeight()+100+16);
//		}

		if (added == 2) {
			generalTab.addNewTextBox(UIScale.getUIScale().scale(100));
			GUIScrollablePanel p = new GUIScrollablePanel(10, 10, generalTab.getContent(added), getState());
			GUIAnchor anc = new GUIAnchor(getState(), 10, 10);

			GUITextOverlay l = new GUITextOverlay(FontSize.SMALL_14, getState());
			l.setTextSimple(Lng.str("Hello %s!\nWelcome to StarMade!",  getOwnPlayer().getName()));
			l.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
			l.onInit();
			l.updateTextSize();
			anc.setWidth(l.getMaxLineWidth());
			anc.setHeight(l.getTextHeight());
			anc.attach(l);

			generalTab.getContent(added).attach(anc);
			added++;

		}
	}

	public PlayerState getOwnPlayer() {
		return JoinPanelNew.this.getState().getPlayer();
	}

	public Faction getOwnFaction() {
		return JoinPanelNew.this.getState().getFactionManager().getFaction(getOwnPlayer().getFactionId());
	}

}
