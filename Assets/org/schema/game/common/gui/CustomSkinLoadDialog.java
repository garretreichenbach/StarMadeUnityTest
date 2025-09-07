package org.schema.game.common.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.resource.FileExt;

public class CustomSkinLoadDialog extends JDialog {

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

	/**
	 * Create the dialog.
	 */
	public CustomSkinLoadDialog(final JFrame jFrame) {
		super(jFrame, true);

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
			panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Skin Package Path", TitledBorder.LEADING, TitledBorder.TOP, null, null));
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
			textFiledMain.setText(EngineSettings.PLAYER_SKIN.getString());
			{
				JButton btnBrowse = new JButton("browse");
				GridBagConstraints gbc_btnBrowse = new GridBagConstraints();
				gbc_btnBrowse.anchor = GridBagConstraints.EAST;
				gbc_btnBrowse.gridx = 0;
				gbc_btnBrowse.gridy = 1;
				panel.add(btnBrowse, gbc_btnBrowse);
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
					if (textFiledMain.getText().length() > 0) {
						EngineSettings.PLAYER_SKIN.setString(textFiledMain.getText());
						try {
							EngineSettings.write();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						File file;

					} else {
						EngineSettings.PLAYER_SKIN.setString("");
						try {
							EngineSettings.write();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
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

	private void importFile(JFrame jFrame, JTextField field) {
		if (fc == null) {
			fc = new JFileChooser(new FileExt("./"));
			FileFilter fileFilter = new FileFilter() {

				@Override
				public boolean accept(File arg0) {
					if (arg0.isDirectory()) {
						return true;
					}
					if (arg0.getName().endsWith(".smskin")) {
						return true;
					}
					return false;
				}

				@Override
				public String getDescription() {
					return ".smskin (StarMade Skin)";
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
