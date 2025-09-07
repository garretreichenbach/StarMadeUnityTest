package org.schema.game.common.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.schema.game.common.ClientRunnable;
import org.schema.game.common.Starter;
import org.schema.game.common.api.SessionNewStyle;
import org.schema.game.common.util.GuiErrorHandler;
import org.schema.game.common.util.StarMadeCredentials;
import org.schema.schine.auth.exceptions.WrongUserNameOrPasswordException;
import org.schema.schine.common.language.Lng;

public class LoginDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private final JPanel contentPanel = new JPanel();
	private JTextField textField;
	private JPasswordField passwordField;
	private JCheckBox chckbxSaveCredentialsencrypted;
	private ClientRunnable callback;

	public LoginDialog(JFrame p) {
		this(p, null);
	}

	/**
	 * Create the dialog.
	 */
	public LoginDialog(JFrame p, ClientRunnable c) {
		super(p);
		this.callback = c;
		setTitle(Lng.str("Login to Star-Made.org"));
		setBounds(100, 100, 470, 275);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0};
		gbl_contentPanel.rowHeights = new int[]{0, 0, 20, 0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblNewLabel = new JLabel(Lng.str("Please enter your registry.star-made.org credentials"));
			GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
			gbc_lblNewLabel.insets = new Insets(0, 0, 5, 0);
			gbc_lblNewLabel.gridx = 0;
			gbc_lblNewLabel.gridy = 0;
			contentPanel.add(lblNewLabel, gbc_lblNewLabel);
		}
		{
			JLabel lblNewLabel_1 = new JLabel(Lng.str("If you don't have an Account yet, please go to www.star-made.org to create one"));
			GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
			gbc_lblNewLabel_1.insets = new Insets(0, 0, 20, 0);
			gbc_lblNewLabel_1.gridx = 0;
			gbc_lblNewLabel_1.gridy = 1;
			contentPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		}
		{
			JPanel panel = new JPanel();
			panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			GridBagConstraints gbc_panel = new GridBagConstraints();
			gbc_panel.weightx = 1.0;
			gbc_panel.fill = GridBagConstraints.BOTH;
			gbc_panel.insets = new Insets(0, 0, 5, 0);
			gbc_panel.gridx = 0;
			gbc_panel.gridy = 2;
			contentPanel.add(panel, gbc_panel);
			GridBagLayout gbl_panel = new GridBagLayout();
			gbl_panel.columnWidths = new int[]{0, 80, 0, 50, 0};
			gbl_panel.rowHeights = new int[]{20, 0};
			gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
			panel.setLayout(gbl_panel);
			{
				JLabel lblUserName = new JLabel(Lng.str("User Name"));
				GridBagConstraints gbc_lblUserName = new GridBagConstraints();
				gbc_lblUserName.anchor = GridBagConstraints.EAST;
				gbc_lblUserName.insets = new Insets(0, 0, 0, 5);
				gbc_lblUserName.gridx = 0;
				gbc_lblUserName.gridy = 0;
				panel.add(lblUserName, gbc_lblUserName);
			}
			{
				textField = new JTextField();
				GridBagConstraints gbc_textField = new GridBagConstraints();
				gbc_textField.weightx = 0.5;
				gbc_textField.anchor = GridBagConstraints.NORTH;
				gbc_textField.fill = GridBagConstraints.HORIZONTAL;
				gbc_textField.insets = new Insets(0, 0, 0, 5);
				gbc_textField.gridx = 1;
				gbc_textField.gridy = 0;
				panel.add(textField, gbc_textField);
				textField.setColumns(13);
			}
			{
				JLabel lblPassword = new JLabel(Lng.str("Password"));
				GridBagConstraints gbc_lblPassword = new GridBagConstraints();
				gbc_lblPassword.anchor = GridBagConstraints.EAST;
				gbc_lblPassword.insets = new Insets(0, 0, 0, 5);
				gbc_lblPassword.gridx = 2;
				gbc_lblPassword.gridy = 0;
				panel.add(lblPassword, gbc_lblPassword);
			}
			{
				passwordField = new JPasswordField();
				GridBagConstraints gbc_passwordField = new GridBagConstraints();
				gbc_passwordField.weightx = 1.0;
				gbc_passwordField.anchor = GridBagConstraints.NORTH;
				gbc_passwordField.fill = GridBagConstraints.HORIZONTAL;
				gbc_passwordField.gridx = 3;
				gbc_passwordField.gridy = 0;
				panel.add(passwordField, gbc_passwordField);
			}
		}
		{
			chckbxSaveCredentialsencrypted = new JCheckBox("Save Login (encrypted)");
			GridBagConstraints gbc_chckbxSaveCredentialsencrypted = new GridBagConstraints();
			gbc_chckbxSaveCredentialsencrypted.anchor = GridBagConstraints.EAST;
			gbc_chckbxSaveCredentialsencrypted.insets = new Insets(0, 0, 5, 0);
			gbc_chckbxSaveCredentialsencrypted.gridx = 0;
			gbc_chckbxSaveCredentialsencrypted.gridy = 3;
			contentPanel.add(chckbxSaveCredentialsencrypted, gbc_chckbxSaveCredentialsencrypted);
		}
		{
			JButton btnDeleteSavedLogin = new JButton(Lng.str("Delete Saved Login"));
			btnDeleteSavedLogin.addActionListener(arg0 -> {
				try {
					StarMadeCredentials.removeFile();
				} catch (Exception ex) {
					ex.printStackTrace();
					GuiErrorHandler.processErrorDialogException(ex);
				}
			});
			btnDeleteSavedLogin.setEnabled(StarMadeCredentials.exists());
			btnDeleteSavedLogin.setHorizontalAlignment(SwingConstants.RIGHT);
			GridBagConstraints gbc_btnDeleteSavedLogin = new GridBagConstraints();
			gbc_btnDeleteSavedLogin.anchor = GridBagConstraints.EAST;
			gbc_btnDeleteSavedLogin.insets = new Insets(0, 0, 5, 0);
			gbc_btnDeleteSavedLogin.gridx = 0;
			gbc_btnDeleteSavedLogin.gridy = 4;
			contentPanel.add(btnDeleteSavedLogin, gbc_btnDeleteSavedLogin);
		}
		{
			JLabel lblNoteThisIs = new JLabel(Lng.str("Note: this is optional!"));
			lblNoteThisIs.setForeground(new Color(139, 0, 0));
			lblNoteThisIs.setFont(new Font("Tahoma", Font.BOLD, 12));
			GridBagConstraints gbc_lblNoteThisIs = new GridBagConstraints();
			gbc_lblNoteThisIs.insets = new Insets(15, 0, 5, 0);
			gbc_lblNoteThisIs.anchor = GridBagConstraints.WEST;
			gbc_lblNoteThisIs.gridx = 0;
			gbc_lblNoteThisIs.gridy = 5;
			contentPanel.add(lblNoteThisIs, gbc_lblNoteThisIs);
		}
		{
			JLabel lblYouDontHave = new JLabel(Lng.str("You don't have to be logged on to play single, or multiplayer"));
			GridBagConstraints gbc_lblYouDontHave = new GridBagConstraints();
			gbc_lblYouDontHave.anchor = GridBagConstraints.WEST;
			gbc_lblYouDontHave.gridx = 0;
			gbc_lblYouDontHave.gridy = 6;
			contentPanel.add(lblYouDontHave, gbc_lblYouDontHave);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(e -> {
					String username = textField.getText();
					String passwd = new String(passwordField.getPassword());

					switch (Starter.getAuthStyle()) {

						case 0:
							throw new IllegalArgumentException("AuthStyle " + Starter.getAuthStyle() + " is no longer supported.");

						case 1:
							try {
								loginNewStyle(username, passwd);
							} catch (Exception ex) {
								ex.printStackTrace();
								GuiErrorHandler.processErrorDialogException(ex);
							}
							break;

						default:
							throw new IllegalArgumentException("Authstyle Unknown: " + Starter.getAuthStyle());
					}

				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton(callback != null ? Lng.str("Exit") : Lng.str("Cancel"));
				cancelButton.addActionListener(arg0 -> {
					setVisible(false);
					dispose();
					if (callback != null) {
						try{throw new Exception("System.exit() called");}catch(Exception ex){ex.printStackTrace();}System.exit(0);
					}
				});
				cancelButton.setActionCommand(callback != null ? Lng.str("Exit") : Lng.str("Cancel"));
				buttonPane.add(cancelButton);
			}
		}
	}

	public void loginNewStyle(String username, String passwd) throws IOException, WrongUserNameOrPasswordException {
		SessionNewStyle session = new SessionNewStyle("starMadeOrg");
		session.login(username, passwd);
		if (chckbxSaveCredentialsencrypted.isSelected()) {
			StarMadeCredentials credentials = new StarMadeCredentials(username, passwd);
			credentials.write();
		}
		dispose();
		if (callback != null) {
			callback.callback();
		}
	}
}
