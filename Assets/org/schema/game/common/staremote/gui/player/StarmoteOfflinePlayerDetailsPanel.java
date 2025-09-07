package org.schema.game.common.staremote.gui.player;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.schema.game.network.ReceivedPlayer;

public class StarmoteOfflinePlayerDetailsPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	

	public StarmoteOfflinePlayerDetailsPanel(final ReceivedPlayer stats) {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		JLabel lblName = new JLabel("Name: ");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.WEST;
		gbc_lblName.insets = new Insets(0, 5, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		add(lblName, gbc_lblName);

		JLabel lblValname = new JLabel(stats.name);
		GridBagConstraints gbc_lblValname = new GridBagConstraints();
		gbc_lblValname.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblValname.insets = new Insets(0, 0, 5, 0);
		gbc_lblValname.gridx = 1;
		gbc_lblValname.gridy = 0;
		add(lblValname, gbc_lblValname);

		JLabel lblLastLogin = new JLabel("Last Login: ");
		GridBagConstraints gbc_lblLastLogin = new GridBagConstraints();
		gbc_lblLastLogin.anchor = GridBagConstraints.WEST;
		gbc_lblLastLogin.insets = new Insets(0, 5, 5, 5);
		gbc_lblLastLogin.gridx = 0;
		gbc_lblLastLogin.gridy = 1;
		add(lblLastLogin, gbc_lblLastLogin);

		JLabel lblVallastlogin = new JLabel((new Date(stats.lastLogin)).toString());
		GridBagConstraints gbc_lblVallastlogin = new GridBagConstraints();
		gbc_lblVallastlogin.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblVallastlogin.insets = new Insets(0, 0, 5, 0);
		gbc_lblVallastlogin.gridx = 1;
		gbc_lblVallastlogin.gridy = 1;
		add(lblVallastlogin, gbc_lblVallastlogin);

		JLabel lblLastLogout = new JLabel("Last Logout: ");
		GridBagConstraints gbc_lblLastLogout = new GridBagConstraints();
		gbc_lblLastLogout.anchor = GridBagConstraints.WEST;
		gbc_lblLastLogout.insets = new Insets(0, 5, 5, 5);
		gbc_lblLastLogout.gridx = 0;
		gbc_lblLastLogout.gridy = 2;
		add(lblLastLogout, gbc_lblLastLogout);

		JLabel lblVallastlogout = new JLabel((new Date(stats.lastLogout)).toString());
		GridBagConstraints gbc_lblVallastlogout = new GridBagConstraints();
		gbc_lblVallastlogout.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblVallastlogout.insets = new Insets(0, 0, 5, 0);
		gbc_lblVallastlogout.gridx = 1;
		gbc_lblVallastlogout.gridy = 2;
		add(lblVallastlogout, gbc_lblVallastlogout);

		JLabel lblUsedIp = new JLabel("Used IP:");
		GridBagConstraints gbc_lblUsedIp = new GridBagConstraints();
		gbc_lblUsedIp.anchor = GridBagConstraints.WEST;
		gbc_lblUsedIp.insets = new Insets(0, 5, 5, 5);
		gbc_lblUsedIp.gridx = 0;
		gbc_lblUsedIp.gridy = 3;
		add(lblUsedIp, gbc_lblUsedIp);

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 1;
		gbc_scrollPane.gridy = 3;
		add(scrollPane, gbc_scrollPane);

		JList list = new JList(stats.ips);
		scrollPane.setViewportView(list);

		JLabel lblOptions = new JLabel("Options:");
		GridBagConstraints gbc_lblOptions = new GridBagConstraints();
		gbc_lblOptions.insets = new Insets(0, 0, 0, 5);
		gbc_lblOptions.gridx = 0;
		gbc_lblOptions.gridy = 4;
		add(lblOptions, gbc_lblOptions);

		JButton btnNewButton = new JButton("Coming Soon");
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.anchor = GridBagConstraints.WEST;
		gbc_btnNewButton.gridx = 1;
		gbc_btnNewButton.gridy = 4;
		add(btnNewButton, gbc_btnNewButton);
	}

}
