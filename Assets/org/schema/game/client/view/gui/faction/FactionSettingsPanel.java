package org.schema.game.client.view.gui.faction;

import org.schema.game.client.controller.manager.ingame.faction.FactionControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionPermission;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUICheckBox;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;

public class FactionSettingsPanel extends GUIAnchor {
	private GUIScrollablePanel scrollPanel;
	private GUICallback settingsCallback;
	private GUITextButton factionPersonalEnemyButton;

	public FactionSettingsPanel(InputState state, int width, int height, GUICallback settingsCallback) {
		super(state, width, height);
		this.settingsCallback = settingsCallback;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#draw()
	 */
	@Override
	public void draw() {
		GameClientState s = (GameClientState) getState();
		if (s.getPlayer().getFactionId() != 0) {
			Faction faction = s.getFactionManager().getFaction(s.getPlayer().getFactionId());
			if (faction != null) {
				FactionPermission factionPermission = faction.getMembersUID().get(s.getPlayer().getName());
				if (factionPermission != null && factionPermission.hasRelationshipPermission(faction)) {
					super.draw();
				} else {
					GlUtil.glPushMatrix();
					transform();
					factionPersonalEnemyButton.draw();
					GlUtil.glPopMatrix();
				}
			}
		}

	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#onInit()
	 */
	@Override
	public void onInit() {

		scrollPanel = new GUIScrollablePanel(width, height, getState());

		GUIAnchor bg = new GUIAnchor(getState(), 100, 40);

		GUITextOverlay textOpenToJoin = new GUITextOverlay(getState());
		textOpenToJoin.setTextSimple(Lng.str("Public Faction"));
		GUICheckBox checkBoxOpenToJoin = new GUICheckBox(getState()) {
			@Override
			protected void activate()
					throws StateParameterNotFoundException {
				int facId = ((GameClientState) getState()).getPlayer().getFactionId();
				Faction faction = ((GameClientState) getState()).getFactionManager().getFaction(facId);
				if (faction != null) {
					faction.clientRequestOpenFaction(((GameClientState) getState()).getPlayer(), true);
				} else {
					((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Cannot change setting!\nYou are not in a faction!"), 0);
					System.err.println("[CLIENT][FactionSetting] faction not found: " + facId);
				}
			}

			@Override
			protected void deactivate()
					throws StateParameterNotFoundException {
				int facId = ((GameClientState) getState()).getPlayer().getFactionId();
				Faction faction = ((GameClientState) getState()).getFactionManager().getFaction(facId);
				if (faction != null) {
					faction.clientRequestOpenFaction(((GameClientState) getState()).getPlayer(), false);
				} else {
					((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Cannot change setting!\nYou are not in a faction!"), 0);
					System.err.println("[CLIENT][FactionSetting] faction not found: " + facId);
				}

			}

			@Override
			protected boolean isActivated() {
				int facId = ((GameClientState) getState()).getPlayer().getFactionId();
				Faction faction = ((GameClientState) getState()).getFactionManager().getFaction(facId);
				if (faction != null) {
					return faction.isOpenToJoin();
				} else {
					return false;
				}
			}

		};

		GUITextOverlay textAttackNeutrals = new GUITextOverlay(getState());
		textAttackNeutrals.setTextSimple("Consider neutral enemy");
		GUICheckBox checkBoxAttackNeutrals = new GUICheckBox(getState()) {
			@Override
			protected void activate()
					throws StateParameterNotFoundException {
				int facId = ((GameClientState) getState()).getPlayer().getFactionId();
				Faction faction = ((GameClientState) getState()).getFactionManager().getFaction(facId);
				if (faction != null) {
					faction.clientRequestAttackNeutral(((GameClientState) getState()).getPlayer(), true);
				} else {
					((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Cannot change setting!\nYou are not in a faction!"), 0);
					System.err.println("[CLIENT][FactionSetting] faction not found: " + facId);
				}
			}

			@Override
			protected void deactivate()
					throws StateParameterNotFoundException {
				int facId = ((GameClientState) getState()).getPlayer().getFactionId();
				Faction faction = ((GameClientState) getState()).getFactionManager().getFaction(facId);
				if (faction != null) {
					faction.clientRequestAttackNeutral(((GameClientState) getState()).getPlayer(), false);
				} else {
					((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Cannot change setting!\nYou are not in a faction!"), 0);
					System.err.println("[CLIENT][FactionSetting] faction not found: " + facId);
				}

			}

			@Override
			protected boolean isActivated() {
				int facId = ((GameClientState) getState()).getPlayer().getFactionId();
				Faction faction = ((GameClientState) getState()).getFactionManager().getFaction(facId);
				if (faction != null) {
					return faction.isAttackNeutral();
				} else {
					return false;
				}
			}

		};

		GUITextOverlay textAutoDeclareWar = new GUITextOverlay(getState());
		textAutoDeclareWar.setTextSimple("Declare war on hostile action");
		GUICheckBox checkBoxAutoDeclareWar = new GUICheckBox(getState()) {
			@Override
			protected void activate()
					throws StateParameterNotFoundException {
				int facId = ((GameClientState) getState()).getPlayer().getFactionId();
				Faction faction = ((GameClientState) getState()).getFactionManager().getFaction(facId);
				if (faction != null) {
					faction.clientRequestAutoDeclareWar(((GameClientState) getState()).getPlayer(), true);
				} else {
					((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Cannot change setting!\nYou are not in a faction!"), 0);
					System.err.println("[CLIENT][FactionSetting] faction not found: " + facId);
				}
			}

			@Override
			protected void deactivate()
					throws StateParameterNotFoundException {
				int facId = ((GameClientState) getState()).getPlayer().getFactionId();
				Faction faction = ((GameClientState) getState()).getFactionManager().getFaction(facId);
				if (faction != null) {
					faction.clientRequestAutoDeclareWar(((GameClientState) getState()).getPlayer(), false);
				} else {
					((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Cannot change setting!\nYou are not in a faction!"), 0);
					System.err.println("[CLIENT][FactionSetting] faction not found: " + facId);
				}

			}

			@Override
			protected boolean isActivated() {
				int facId = ((GameClientState) getState()).getPlayer().getFactionId();
				Faction faction = ((GameClientState) getState()).getFactionManager().getFaction(facId);
				if (faction != null) {
					return faction.isAutoDeclareWar();
				} else {
					return false;
				}
			}

		};

		GUITextButton postNewsButton = new GUITextButton(getState(), 80, 20, Lng.str("Post News"), settingsCallback, ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager()
				.getFactionControlManager());
		postNewsButton.setUserPointer(FactionControlManager.POST_NEWS);
		postNewsButton.getPos().x = 280;

		GUITextButton factionRolesButton = new GUITextButton(getState(), 45, 20, Lng.str("Roles"), settingsCallback, ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager()
				.getFactionControlManager()) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
			 */
			@Override
			public void draw() {
				int facId = ((GameClientState) getState()).getPlayer().getFactionId();
				Faction faction = ((GameClientState) getState()).getFactionManager().getFaction(facId);
				if (faction != null) {
					FactionPermission factionPermission = faction.getMembersUID().get(((GameClientState) getState()).getPlayer().getName());
					if (factionPermission != null && factionPermission.hasPermissionEditPermission(faction)) {
						super.draw();
					}
				}
			}

		};
		factionRolesButton.setUserPointer(FactionControlManager.FACTION_ROLES);
		factionRolesButton.getPos().x = 450;

		factionPersonalEnemyButton = new GUITextButton(getState(), 125, 20, Lng.str("Personal Enemies"), settingsCallback, ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager());
		factionPersonalEnemyButton.setUserPointer(FactionControlManager.PERSONAL_ENEMIES);
		factionPersonalEnemyButton.getPos().x = 5;

		GUITextButton editDescriptionButton = new GUITextButton(getState(), 110, 20, Lng.str("Edit Description"), settingsCallback, ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager()
				.getFactionControlManager());
		editDescriptionButton.setUserPointer(FactionControlManager.EDIT_DESCRIPTION);
		editDescriptionButton.getPos().x = 150;

		GUITextButton offerButton = new GUITextButton(getState(), 60, 20, Lng.str("Offers"), new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager()
							.getFactionControlManager().openOpenOffers();
				}

			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		}, ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager()
				.getFactionControlManager());
		offerButton.getPos().x = 375;

		int cbPosY = 23;
		int tPosY = 32;

		checkBoxOpenToJoin.getPos().y = cbPosY;
		textOpenToJoin.getPos().x = 35;
		textOpenToJoin.getPos().y = tPosY;

		checkBoxAutoDeclareWar.getPos().x = 145;
		checkBoxAutoDeclareWar.getPos().y = cbPosY;
		textAutoDeclareWar.getPos().x = 180;
		textAutoDeclareWar.getPos().y = tPosY;

		checkBoxAttackNeutrals.getPos().x = 350;
		checkBoxAttackNeutrals.getPos().y = cbPosY;
		textAttackNeutrals.getPos().x = 385;
		textAttackNeutrals.getPos().y = tPosY;

		bg.attach(checkBoxOpenToJoin);
		bg.attach(textOpenToJoin);
		bg.attach(textAttackNeutrals);
		bg.attach(checkBoxAttackNeutrals);
		bg.attach(textAutoDeclareWar);
		bg.attach(checkBoxAutoDeclareWar);
		bg.attach(postNewsButton);
		bg.attach(editDescriptionButton);
		bg.attach(offerButton);
		bg.attach(factionRolesButton);
		bg.attach(factionPersonalEnemyButton);

		scrollPanel.setContent(bg);
		this.attach(scrollPanel);
		super.onInit();
	}

}
