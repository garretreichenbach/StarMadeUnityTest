package org.schema.game.client.view.gui.faction;

import javax.vecmath.Vector4f;

import org.schema.game.client.controller.manager.ingame.faction.FactionControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.PlayerFactionController;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangle;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

public class PersonalFactionPanel extends GUIElement {

	private GUITextButton createFactionButton;

	private GUITextButton leaveFactionButton;

	private GUITextButton invitesInButton;

	private GUIColoredRectangle currentFactionBackground;
	private GUITextOverlay currentFactionText;

	private ScrollingFactionNewsPosts scrollingFactionNewsPosts;

	private FactionDescriptionPanel factionDescriptionPanel;

	private GUITextButton invitesOutButton;

	public PersonalFactionPanel(InputState state) {
		super(state);
	}

	@Override
	public void cleanUp() {
		
	}

	@Override
	public void draw() {
		currentFactionText.getText().set(0, "Current Faction: " + getFactionController().getFactionName());

		drawAttached();
	}

	@Override
	public void onInit() {

		FactionControlManager factionControlManager = ((GameClientState) getState()).getGlobalGameControlManager()
				.getIngameControlManager().getPlayerGameControlManager().getFactionControlManager();
		currentFactionBackground = new GUIColoredRectangle(getState(), UIScale.getUIScale().scale(540), UIScale.getUIScale().scale(30), new Vector4f(0, 0.3f, 0, 0.5f));
		currentFactionBackground.rounded = UIScale.getUIScale().scale(3);
		currentFactionText = new GUITextOverlay(FontSize.MEDIUM_18, getState());
		currentFactionText.setTextSimple("Current Faction: ");
		currentFactionBackground.attach(currentFactionText);

		createFactionButton = new GUITextButton(getState(), UIScale.getUIScale().scale(130), UIScale.getUIScale().scale(20), "Create new faction",
				factionControlManager, factionControlManager);
		createFactionButton.setUserPointer(FactionControlManager.CREATE_FACTION);
		createFactionButton.setPos(0, UIScale.getUIScale().scale(35));

		leaveFactionButton = new GUITextButton(getState(), UIScale.getUIScale().scale(100), UIScale.getUIScale().scale(20), "Leave Faction",
				factionControlManager, factionControlManager);
		leaveFactionButton.setUserPointer(FactionControlManager.LEAVE_FACTION);
		leaveFactionButton.setPos(UIScale.getUIScale().scale(400), UIScale.getUIScale().scale(35));

		Object invitesText = new Object() {
			@Override
			public String toString() {
				return "Invites(" + ((GameClientState) getState()).getPlayer().getFactionController().getInvitesIncoming().size() + ")";
			}
		};

		invitesInButton = new GUITextButton(getState(), UIScale.getUIScale().scale(100), UIScale.getUIScale().scale(20), invitesText, factionControlManager, factionControlManager);
		invitesInButton.setUserPointer(FactionControlManager.VIEW_INCOMING_INVITE_FACTION);
		invitesInButton.setPos(UIScale.getUIScale().scale(150), UIScale.getUIScale().scale(35));

		invitesOutButton = new GUITextButton(getState(), UIScale.getUIScale().scale(120), UIScale.getUIScale().scale(20), "Pending Invites", factionControlManager, factionControlManager);
		invitesOutButton.setUserPointer(FactionControlManager.VIEW_OUTGOING_INVITE_FACTION);
		invitesOutButton.setPos(UIScale.getUIScale().scale(260), UIScale.getUIScale().scale(35));

		factionDescriptionPanel = new FactionDescriptionPanel(getState(), UIScale.getUIScale().scale(500), UIScale.getUIScale().scale(80)) {

			@Override
			public String getCurrentDesc() {
				int id = ((GameClientState) getState()).getPlayer().getFactionId();
				Faction f = ((GameClientState) getState()).getFactionManager().getFaction(id);
				return f != null ? f.getDescription() : "(no faction)";
			}

		};
		factionDescriptionPanel.onInit();
		factionDescriptionPanel.setPos(0, UIScale.getUIScale().scale(60));

		scrollingFactionNewsPosts = new ScrollingFactionNewsPosts(getState(), UIScale.getUIScale().scale(540), UIScale.getUIScale().scale(205));
		scrollingFactionNewsPosts.setPos(0, UIScale.getUIScale().scale(140));
		scrollingFactionNewsPosts.onInit();
		this.setMouseUpdateEnabled(true);

		this.attach(createFactionButton);
		this.attach(currentFactionBackground);
		this.attach(leaveFactionButton);
		this.attach(invitesInButton);
		this.attach(invitesOutButton);
		this.attach(factionDescriptionPanel);
		this.attach(scrollingFactionNewsPosts);
	}

	private PlayerFactionController getFactionController() {
		return ((GameClientState) getState()).getPlayer().getFactionController();
	}

	@Override
	public float getHeight() {
				return 0;
	}

	@Override
	public float getWidth() {
				return 0;
	}

	@Override
	public boolean isPositionCenter() {
				return false;
	}

}
