package org.schema.game.common.staremote.gui.player;

import java.util.Date;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.staremote.gui.entity.StarmoteEntitySettingsPanel;
import org.schema.game.common.staremote.gui.settings.StarmoteSettingButtonTrigger;
import org.schema.game.common.staremote.gui.settings.StarmoteSettingTextLabel;
import org.schema.game.server.data.admin.AdminCommands;

public class StarmotePlayerSettingPanel extends StarmoteEntitySettingsPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	

	public StarmotePlayerSettingPanel(PlayerState s) {
		super(s);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.staremote.gui.entity.StaremoteEntitySettingsPanel#initializeSettings()
	 */
	@Override
	public void initializeSettings() {
		super.initializeSettings();
		final PlayerState player = (PlayerState) getSendable();
		addSetting(new StarmoteSettingTextLabel("NAME") {
			

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public String getValue() {
				return "NAME: " + player.getName();
			}
		});
		addSetting(new StarmoteSettingTextLabel("CREDITS") {
			

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Object getValue() {
				return player.getCredits();
			}
		});
		addSetting(new StarmoteSettingTextLabel("HEALTH") {
			

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Object getValue() {
				return player.getHealth();
			}
		});
		addSetting(new StarmoteSettingTextLabel("SECTOR") {
			

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Object getValue() {
				return player.getCurrentSector().toString();
			}
		});
		addSetting(new StarmoteSettingTextLabel("CREATED") {
			

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Object getValue() {
				return new Date(player.getCreationTime());
			}
		});

		addSetting(new StarmoteSettingButtonTrigger("KICK") {
			

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Object getValue() {
				return "";
			}

			@Override
			public void trigger() {
				((GameClientState) player.getState()).getController().sendAdminCommand(AdminCommands.KICK, player.getName());
			}
		});
		addSetting(new StarmoteSettingButtonTrigger("BAN BY NAME") {
			

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Object getValue() {
				return "";
			}

			@Override
			public void trigger() {
				((GameClientState) player.getState()).getController().sendAdminCommand(AdminCommands.BAN, player.getName(), false);
			}
		});
	}

}
