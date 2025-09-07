package org.schema.game.common.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import org.schema.game.common.data.player.PlayerSkin;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.resource.FileExt;

public class CustomSkinCreateDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private final JPanel contentPanel = new JPanel();
	private JTextField textFiledMain;
	private JFileChooser fc;
	private JTextField textFieldHelmetEmission;
	private JTextField textFieldHelmet;
	private JTextField textFieldMainEmission;

	/**
	 * Create the dialog.
	 */
	public CustomSkinCreateDialog(final JFrame jFrame) {
		super(jFrame, true);
		this.setAlwaysOnTop(true);
		setBounds(100, 100, 444, 459);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 0};
		gbl_contentPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JPanel panel = new JPanel();
			panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Main Skin Texture", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			GridBagConstraints gbc_panel = new GridBagConstraints();
			gbc_panel.fill = GridBagConstraints.BOTH;
			gbc_panel.insets = new Insets(0, 0, 5, 0);
			gbc_panel.gridx = 0;
			gbc_panel.gridy = 0;
			contentPanel.add(panel, gbc_panel);
			GridBagLayout gbl_panel = new GridBagLayout();
			gbl_panel.columnWidths = new int[]{0, 0};
			gbl_panel.rowHeights = new int[]{0, 0, 0};
			gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gbl_panel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
			panel.setLayout(gbl_panel);
			textFiledMain = new JTextField();
			GridBagConstraints gbc_textFiledMain = new GridBagConstraints();
			gbc_textFiledMain.insets = new Insets(0, 0, 5, 0);
			gbc_textFiledMain.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFiledMain.gridx = 0;
			gbc_textFiledMain.gridy = 0;
			panel.add(textFiledMain, gbc_textFiledMain);
			textFiledMain.setColumns(10);
			{
				JButton btnBrowse = new JButton("browse");
				GridBagConstraints gbc_btnBrowse = new GridBagConstraints();
				gbc_btnBrowse.anchor = GridBagConstraints.EAST;
				gbc_btnBrowse.gridx = 0;
				gbc_btnBrowse.gridy = 1;
				panel.add(btnBrowse, gbc_btnBrowse);
				{
					JPanel panel_1 = new JPanel();
					panel_1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Main Skin Emission Textue", TitledBorder.LEADING, TitledBorder.TOP, null, null));
					GridBagConstraints gbc_panel_1 = new GridBagConstraints();
					gbc_panel_1.insets = new Insets(0, 0, 5, 0);
					gbc_panel_1.fill = GridBagConstraints.BOTH;
					gbc_panel_1.gridx = 0;
					gbc_panel_1.gridy = 1;
					contentPanel.add(panel_1, gbc_panel_1);
					GridBagLayout gbl_panel_1 = new GridBagLayout();
					gbl_panel_1.columnWidths = new int[]{0, 0};
					gbl_panel_1.rowHeights = new int[]{0, 0, 0};
					gbl_panel_1.columnWeights = new double[]{1.0, Double.MIN_VALUE};
					gbl_panel_1.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
					panel_1.setLayout(gbl_panel_1);
					{
						textFieldMainEmission = new JTextField();
						textFieldMainEmission.setText("");
						textFieldMainEmission.setColumns(10);
						GridBagConstraints gbc_textFieldMainEmission = new GridBagConstraints();
						gbc_textFieldMainEmission.fill = GridBagConstraints.HORIZONTAL;
						gbc_textFieldMainEmission.insets = new Insets(0, 0, 5, 0);
						gbc_textFieldMainEmission.gridx = 0;
						gbc_textFieldMainEmission.gridy = 0;
						panel_1.add(textFieldMainEmission, gbc_textFieldMainEmission);
					}
					{
						JButton button = new JButton("browse");
						button.addActionListener(e -> importFile(jFrame, textFieldMainEmission));
						GridBagConstraints gbc_button = new GridBagConstraints();
						gbc_button.anchor = GridBagConstraints.EAST;
						gbc_button.gridx = 0;
						gbc_button.gridy = 1;
						panel_1.add(button, gbc_button);
					}
				}
				{
					JPanel panel_1 = new JPanel();
					panel_1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Helmet Skin Texture", TitledBorder.LEADING, TitledBorder.TOP, null, null));
					GridBagConstraints gbc_panel_1 = new GridBagConstraints();
					gbc_panel_1.insets = new Insets(0, 0, 5, 0);
					gbc_panel_1.fill = GridBagConstraints.BOTH;
					gbc_panel_1.gridx = 0;
					gbc_panel_1.gridy = 2;
					contentPanel.add(panel_1, gbc_panel_1);
					GridBagLayout gbl_panel_1 = new GridBagLayout();
					gbl_panel_1.columnWidths = new int[]{0, 0};
					gbl_panel_1.rowHeights = new int[]{0, 0, 0};
					gbl_panel_1.columnWeights = new double[]{1.0, Double.MIN_VALUE};
					gbl_panel_1.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
					panel_1.setLayout(gbl_panel_1);
					{
						textFieldHelmet = new JTextField();
						textFieldHelmet.setText("");
						textFieldHelmet.setColumns(10);
						GridBagConstraints gbc_textFieldHelmet = new GridBagConstraints();
						gbc_textFieldHelmet.fill = GridBagConstraints.HORIZONTAL;
						gbc_textFieldHelmet.insets = new Insets(0, 0, 5, 0);
						gbc_textFieldHelmet.gridx = 0;
						gbc_textFieldHelmet.gridy = 0;
						panel_1.add(textFieldHelmet, gbc_textFieldHelmet);
					}
					{
						JButton button = new JButton("browse");
						button.addActionListener(e -> importFile(jFrame, textFieldHelmet));
						GridBagConstraints gbc_button = new GridBagConstraints();
						gbc_button.anchor = GridBagConstraints.EAST;
						gbc_button.gridx = 0;
						gbc_button.gridy = 1;
						panel_1.add(button, gbc_button);
					}
				}
				{
					JPanel panel_1 = new JPanel();
					panel_1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Helmet Skin Emission Texture", TitledBorder.LEADING, TitledBorder.TOP, null, null));
					GridBagConstraints gbc_panel_1 = new GridBagConstraints();
					gbc_panel_1.fill = GridBagConstraints.BOTH;
					gbc_panel_1.gridx = 0;
					gbc_panel_1.gridy = 3;
					contentPanel.add(panel_1, gbc_panel_1);
					GridBagLayout gbl_panel_1 = new GridBagLayout();
					gbl_panel_1.columnWidths = new int[]{0, 0};
					gbl_panel_1.rowHeights = new int[]{0, 0, 0};
					gbl_panel_1.columnWeights = new double[]{1.0, Double.MIN_VALUE};
					gbl_panel_1.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
					panel_1.setLayout(gbl_panel_1);
					{
						textFieldHelmetEmission = new JTextField();
						textFieldHelmetEmission.setText("");
						textFieldHelmetEmission.setColumns(10);
						GridBagConstraints gbc_textFieldHelmetEmission = new GridBagConstraints();
						gbc_textFieldHelmetEmission.fill = GridBagConstraints.HORIZONTAL;
						gbc_textFieldHelmetEmission.insets = new Insets(0, 0, 5, 0);
						gbc_textFieldHelmetEmission.gridx = 0;
						gbc_textFieldHelmetEmission.gridy = 0;
						panel_1.add(textFieldHelmetEmission, gbc_textFieldHelmetEmission);
					}
					{
						JButton button = new JButton("browse");
						button.addActionListener(e -> importFile(jFrame, textFieldHelmetEmission));
						GridBagConstraints gbc_button = new GridBagConstraints();
						gbc_button.anchor = GridBagConstraints.EAST;
						gbc_button.gridx = 0;
						gbc_button.gridy = 1;
						panel_1.add(button, gbc_button);
					}
				}
				btnBrowse.addActionListener(e -> importFile(jFrame, textFiledMain));
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(e -> {
					if (textFiledMain.getText().length() > 0 && textFieldMainEmission.getText().length() > 0 &&
							textFieldHelmet.getText().length() > 0 && textFieldHelmetEmission.getText().length() > 0) {
						File file;
						try {
							File f = chooseFile(jFrame, "Save As...");
							if (f != null && f.getName().endsWith(PlayerSkin.EXTENSION)) {

								file = PlayerSkin.createSkinFile(f, textFiledMain.getText(), textFieldMainEmission.getText(), textFieldHelmet.getText(), textFieldHelmetEmission.getText());

								Object[] options = {"Yes", "No"};
								String title = "Sucessfully created skin";
								JFrame jFrame1 = new JFrame(title);
								jFrame1.setUndecorated(true); // set frame undecorated, so the frame
								// itself is invisible
								jFrame1.setVisible(true); // the frame must be set to visible, so it
								// appears in the task bar
								jFrame1.setAlwaysOnTop(true);
								Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

								jFrame1.setLocation(screenSize.width / 2, screenSize.height / 2);

								int n = JOptionPane.showOptionDialog(jFrame1, "Do you want to set that skin file as your skin now?\n", title,
										JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE,
										null, options, options[0]);
								switch (n) {
									case 0:
										EngineSettings.PLAYER_SKIN.setString(file.getAbsolutePath());
										EngineSettings.write();
										break;

									case 1:

										break;

								}

							} else {
								throw new IOException();
							}

						} catch (IOException e1) {
							e1.printStackTrace();
							onError();
						}

					} else {
						onError();
					}
					dispose();
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);

			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(e -> dispose());
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	private void onError() {
		Object[] options = {"OK"};
		String title = "Error creating skin";
		JFrame jFrame = new JFrame(title);
		jFrame.setUndecorated(true); // set frame undecorated, so the frame
		// itself is invisible
		jFrame.setVisible(true); // the frame must be set to visible, so it
		// appears in the task bar
		jFrame.setAlwaysOnTop(true);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		jFrame.setLocation(screenSize.width / 2, screenSize.height / 2);

		int n = JOptionPane.showOptionDialog(jFrame, "Could not create skin file.\nPlease make sure all files are provided and\nthat they are in PNG format\n\ncontinuing will reset the skin paths", title,
				JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE,
				null, options, options[0]);
		switch (n) {
			case 0:

				break;

		}
	}

	private File chooseFile(JFrame frame, String title) {
		JFileChooser fc = new JFileChooser(new FileExt("./"));
		FileFilter fileFilter = new FileFilter() {

			@Override
			public boolean accept(File arg0) {
				if (arg0.isDirectory()) {
					return true;
				}
				if (arg0.getName().endsWith(PlayerSkin.EXTENSION)) {
					return true;
				}
				return false;
			}

			@Override
			public String getDescription() {
				return "StarMade Skin (PlayerSkin.EXTENSION)";
			}
		};
		fc.addChoosableFileFilter(fileFilter);
		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileFilter(fileFilter);

		//Show it.
		int returnVal = fc.showDialog(frame, title);

		//Process the results.
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File fileToBeSaved = fc.getSelectedFile();
			String suffix = PlayerSkin.EXTENSION;
			if (!fileToBeSaved.getAbsolutePath().endsWith(suffix)) {
				fileToBeSaved = new FileExt(fileToBeSaved.getAbsolutePath() + suffix);
			}
			return fileToBeSaved;
		} else {
			return null;
		}
	}

	private void importFile(JFrame jFrame, JTextField field) {
		if (fc == null) {
			fc = new JFileChooser(new FileExt("./"));
			FileFilter fileFilter = new FileFilter() {

				@Override
				public boolean accept(File arg0) {
					if (arg0.isDirectory()) {
						return true;
					}
					if (arg0.getName().endsWith(".png")) {
						return true;
					}
					return false;
				}

				@Override
				public String getDescription() {
					return "PNG";
				}
			};
			fc.addChoosableFileFilter(fileFilter);
			fc.setFileFilter(fileFilter);
			fc.setAcceptAllFileFilterUsed(false);
		}
		//Show it.
		int returnVal = fc.showDialog(jFrame, "Import");

		//Process the results.
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			field.setText(file.getAbsolutePath());
		} else {
		}

	}
}
