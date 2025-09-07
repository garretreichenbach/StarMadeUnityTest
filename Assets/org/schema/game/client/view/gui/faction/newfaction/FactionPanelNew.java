package org.schema.game.client.view.gui.faction.newfaction;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.npc.GUINPCDiplomacyScrollableList;
import org.schema.game.client.view.gui.npc.GUINPCFactionNewsScrollableList;
import org.schema.game.client.view.gui.npc.GUINPCFactionsScrollableList;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.game.server.data.simulation.npc.NPCFaction;
import org.schema.game.server.data.simulation.npc.diplomacy.NPCDiplomacyEntity;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIMainWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class FactionPanelNew extends GUIElement implements GUIActiveInterface {

	public GUIMainWindow factionPanel;

	private GUIContentPane newsTab;

	private GUIContentPane membersTab;

	private GUIContentPane diplomacyTab;

	private GUIContentPane optionTab;

	private FactionScrollableListNew fList;

	private boolean init;

	private int fid;

	private boolean flagFactionTabRecreate;

	private FactionMemberScrollableListNew mList;

	private FactionNewsScrollableListNew nList;

	private GUIAnchor topList;

	private GUIAnchor bottomList;

	private GUINPCFactionsScrollableList npcFactionList;

	private GUINPCDiplomacyScrollableList gPlayer;

	private GUINPCDiplomacyScrollableList gFaction;

	private GUIAnchor bottomHead;

	private GUIAnchor topHead;

	private GUIAnchor infoPanel;

	private GUIContentPane listTab;

	public FactionPanelNew(InputState state) {
		super(state);
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public void draw() {
		if (!init) {
			onInit();
		}
		if (flagFactionTabRecreate) {
			recreateTabs();
			flagFactionTabRecreate = false;
		}
		factionPanel.draw();
	}

	@Override
	public void onInit() {
		factionPanel = new GUIMainWindow(getState(), UIScale.getUIScale().scale(850), UIScale.getUIScale().scale(550), "FactionPanelNew");
		factionPanel.onInit();
		factionPanel.setCloseCallback(new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(478);
					getState().getWorldDrawer().getGuiDrawer().getPlayerPanel().deactivateAll();
				}
			}

			@Override
			public boolean isOccluded() {
				return !getState().getController().getPlayerInputs().isEmpty();
			}
		});
		factionPanel.orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_VERTICAL_MIDDLE);
		recreateTabs();
		this.fid = getOwnPlayer().getFactionId();
		init = true;
	}

	public void recreateTabs() {
		Object beforeTab = null;
		if (factionPanel.getSelectedTab() < factionPanel.getTabs().size()) {
			beforeTab = factionPanel.getTabs().get(factionPanel.getSelectedTab()).getTabName();
		}
		factionPanel.clearTabs();
		newsTab = factionPanel.addTab(Lng.str("NEWS"));
		diplomacyTab = factionPanel.addTab(Lng.str("NPC DIPLOMACY"));
		createNPCNewsPane();
		createNPCDiplomacyPane();
		if (getOwnFaction() != null) {
			newsTab = factionPanel.addTab(Lng.str("FACTION NEWS"));
			membersTab = factionPanel.addTab(Lng.str("MEMBERS"));
			createNewsPane();
			createMembersPane();
		}
		listTab = factionPanel.addTab(Lng.str("LIST"));
		optionTab = factionPanel.addTab(Lng.str("OPTIONS"));
		createFactionListPane();
		createOptionPane();
		factionPanel.activeInterface = this;
		if (beforeTab != null) {
			for (int i = 0; i < factionPanel.getTabs().size(); i++) {
				if (factionPanel.getTabs().get(i).getTabName().equals(beforeTab)) {
					factionPanel.setSelectedTab(i);
					break;
				}
			}
		}
	}

	private void createNPCNewsPane() {
		newsTab.setTextBoxHeightLast(UIScale.getUIScale().scale(28));
		GUIAnchor content = newsTab.getContent(0);
		GUINPCFactionNewsScrollableList c = new GUINPCFactionNewsScrollableList(getState(), content, this);
		c.onInit();
		content.attach(c);
	}

	public void createNPCDiplomacyPane() {
		diplomacyTab.setTextBoxHeightLast(UIScale.getUIScale().scale(270));
		diplomacyTab.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		infoPanel = diplomacyTab.getContent(0, 1);
		npcFactionList = new GUINPCFactionsScrollableList(getState(), diplomacyTab.getContent(0, 0), this);
		npcFactionList.onInit();
		diplomacyTab.getContent(0, 0).attach(npcFactionList);
		diplomacyTab.addDivider(290);
		diplomacyTab.setTextBoxHeightLast(1, UIScale.getUIScale().scale(48));
		diplomacyTab.addNewTextBox(1, UIScale.getUIScale().scale(190));
		diplomacyTab.addNewTextBox(1, UIScale.getUIScale().scale(48));
		diplomacyTab.addNewTextBox(1, UIScale.getUIScale().scale(200));
		topHead = diplomacyTab.getContent(1, 0);
		topList = diplomacyTab.getContent(1, 1);
		bottomHead = diplomacyTab.getContent(1, 2);
		bottomList = diplomacyTab.getContent(1, 3);
	}

	@Override
	public void update(Timer timer) {
		if (init) {
			if (this.fid != getOwnPlayer().getFactionId()) {
				if (getOwnPlayer().getFactionId() > 0 && getOwnFaction() == null) {
				} else {
					flagFactionTabRecreate = true;
					this.fid = getOwnPlayer().getFactionId();
				}
			}
		}
	}

	public void createMembersPane() {
		if (mList != null) {
			mList.cleanUp();
		}
		mList = new FactionMemberScrollableListNew(getState(), membersTab.getContent(0), getOwnFaction());
		mList.onInit();
		membersTab.getContent(0).attach(mList);
	}

	public void createNewsPane() {
		GUITextOverlay name = new GUITextOverlay(FontSize.BIG_20, getState());
		name.setTextSimple(getOwnFaction().getName() + ", ");
		GUITextOverlay homebase = new GUITextOverlay(FontSize.SMALL_14, getState());
		homebase.setTextSimple(new Object() {

			@Override
			public String toString() {
				Faction f = getOwnFaction();
				String homebase = f.getHomebaseUID().length() > 0 ? (Lng.str("Homebase: ") + f.getHomeSector().toStringPure()) : Lng.str("No home");
				return homebase;
			}
		});
		GUITextOverlay description = new GUITextOverlay(FontSize.SMALL_14, getState());
		description.setTextSimple(Lng.str("Description: "));
		GUITextOverlay descriptionText = new GUITextOverlay(FontSize.SMALL_14, getState());
		descriptionText.setTextSimple(new Object() {

			@Override
			public String toString() {
				return getOwnFaction().getDescription();
			}
		});
		descriptionText.setColor(0.76f, 0.76f, 0.76f, 1f);
		GUITextOverlay members = new GUITextOverlay(FontSize.SMALL_14, getState());
		members.setTextSimple(new Object() {

			@Override
			public String toString() {
				return Lng.str("Members: ") + getOwnFaction().getMembersUID().size();
			}
		});
		newsTab.setTextBoxHeightLast(UIScale.getUIScale().scale(110));
		newsTab.addNewTextBox(UIScale.getUIScale().scale(10));
		int nameWidth = name.getFont().getWidth(getOwnFaction().getName());
		name.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
		homebase.setPos(4 + nameWidth + 10, 9, 0);
		description.setPos(4, 30, 0);
		descriptionText.setPos(80, 30, 0);
		members.setPos(4, 90, 0);
		newsTab.getContent(0).attach(name);
		newsTab.getContent(0).attach(homebase);
		newsTab.getContent(0).attach(description);
		newsTab.getContent(0).attach(descriptionText);
		newsTab.getContent(0).attach(members);
		if (nList != null) {
			nList.cleanUp();
		}
		nList = new FactionNewsScrollableListNew(getState(), newsTab.getContent(1), getOwnFaction());
		nList.onInit();
		newsTab.getContent(1).attach(nList);
	}

	private void createFactionListPane() {
		if (fList != null) {
			fList.cleanUp();
		}
		fList = new FactionScrollableListNew(getState(), listTab.getContent(0));
		fList.onInit();
		listTab.getContent(0).attach(fList);
	}

	private void createOptionPane() {
		FactionOptionPersonalContent c0 = new FactionOptionPersonalContent(getState(), this);
		c0.onInit();
		optionTab.setContent(0, c0);
		optionTab.setTextBoxHeightLast(UIScale.getUIScale().scale(86));
		optionTab.addNewTextBox(UIScale.getUIScale().scale(10));
		FactionOptionFactionContent c1 = new FactionOptionFactionContent(getState(), this);
		c1.onInit();
		optionTab.setContent(1, c1);
	}

	public PlayerState getOwnPlayer() {
		return FactionPanelNew.this.getState().getPlayer();
	}

	public Faction getOwnFaction() {
		return FactionPanelNew.this.getState().getFactionManager().getFaction(getOwnPlayer().getFactionId());
	}

	@Override
	public float getHeight() {
		return factionPanel.getHeight();
	}

	@Override
	public GameClientState getState() {
		return ((GameClientState) super.getState());
	}

	@Override
	public float getWidth() {
		return factionPanel.getWidth();
	}

	@Override
	public boolean isActive() {
		return getState().getController().getPlayerInputs().isEmpty();
	}

	public void reset() {
		if (factionPanel != null) {
			factionPanel.reset();
		}
	}

	public RType getOwnRelationTo(NPCFaction f) {
		return getState().getFactionManager().getRelation(getState().getPlayerName(), getState().getPlayer().getFactionId(), f.getIdFaction());
	}

	public void putInNPCInfoPanel(final NPCFaction f) {
		GUIScrollablePanel sc = new GUIScrollablePanel(10, 10, infoPanel, getState());
		GUITextOverlayTable lBottom = new GUITextOverlayTable(getState());
		lBottom.setTextSimple(new Object() {

			@Override
			public String toString() {
				return f.getName() + "\n" + f.getDescription();
			}
		});
		lBottom.onInit();
		sc.setContent(lBottom);
		sc.onInit();
		infoPanel.getChilds().clear();
		infoPanel.attach(sc);
	}

	public void onSelectFaction(final NPCFaction f) {
		if (gPlayer != null) {
			gPlayer.cleanUp();
		}
		if (gFaction != null) {
			gFaction.cleanUp();
		}
		bottomList.getChilds().clear();
		topList.getChilds().clear();
		gPlayer = new GUINPCDiplomacyScrollableList(getState(), getState().getPlayer().getDbId(), f, topList);
		gPlayer.onInit();
		topList.attach(gPlayer);
		gFaction = new GUINPCDiplomacyScrollableList(getState(), getState().getPlayer().getFactionId(), f, bottomList);
		gFaction.onInit();
		bottomList.attach(gFaction);
		GUIScrollablePanel topScroll = new GUIScrollablePanel(10, 10, topHead, getState());
		GUITextOverlayTable lTop = new GUITextOverlayTable(getState());
		lTop.autoWrapOn = topHead;
		lTop.autoHeight = true;
		lTop.setTextSimple(new Object() {

			@Override
			public String toString() {
				RType ownRelationTo = getOwnRelationTo(f);
				String rel = switch(ownRelationTo) {
					case ENEMY -> Lng.str("They consider you an enemy");
					case FRIEND -> Lng.str("They consider you an ally");
					case NEUTRAL -> Lng.str("Neutral relation");
				};
				NPCDiplomacyEntity ent = f.getDiplomacy().entities.get(getState().getPlayer().getDbId());
				int points = 0;
				int raw = 0;
				if (ent != null) {
					points = ent.getPoints();
					raw = ent.getRawPoints();
				}
				return Lng.str("Personal Relation Status: %s (%s w/o status)", points, raw) + "\n" + rel;
			}
		});
		lTop.onInit();
		GUIScrollablePanel bottomScroll = new GUIScrollablePanel(10, 10, bottomHead, getState());
		GUITextOverlayTable lBottom = new GUITextOverlayTable(getState());
		lBottom.autoHeight = true;
		lBottom.autoWrapOn = bottomHead;
		lBottom.setTextSimple(new Object() {

			@Override
			public String toString() {
				RType ownRelationTo = getOwnRelationTo(f);
				String rel = switch(ownRelationTo) {
					case ENEMY -> Lng.str("They consider your faction enemy");
					case FRIEND -> Lng.str("They consider your faction an ally");
					case NEUTRAL -> Lng.str("Neutral relation to your faction");
				};
				NPCDiplomacyEntity ent = f.getDiplomacy().entities.get(getState().getPlayer().getFactionId());
				int points = 0;
				int raw = 0;
				if (ent != null) {
					points = ent.getPoints();
					raw = ent.getRawPoints();
				}
				return Lng.str("Faction Relation Status: %s (%s w/o status)", points, raw) + "\n" + rel;
			}
		});
		lBottom.onInit();
		topScroll.setContent(lTop);
		bottomScroll.setContent(lBottom);
		topScroll.onInit();
		bottomScroll.onInit();
		topHead.getChilds().clear();
		bottomHead.getChilds().clear();
		topHead.attach(topScroll);
		bottomHead.attach(bottomScroll);
		putInNPCInfoPanel(f);
	}
}
