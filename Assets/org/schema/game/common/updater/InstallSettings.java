package org.schema.game.common.updater;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.schema.game.common.updater.Updater.VersionFile;
import org.schema.schine.resource.FileExt;

public class InstallSettings extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private final JPanel contentPanel = new JPanel();
	private JTextField pathField;
	private JComboBox buildBranchBox;

	/**
	 * Create the dialog.
	 *
	 * @param updatePanel
	 */
	public InstallSettings(final JFrame f, final UpdatePanel updatePanel) {
		super(f);
		setTitle("Install Settings");
		setBounds(100, 100, 387, 354);
		getContentPane().setLayout(new BorderLayout());
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(e -> {
					try {

						UpdatePanel.installDir = pathField.getText();

						UpdatePanel.buildBranch = (VersionFile) buildBranchBox.getSelectedItem();

						try {
							MemorySettings.saveSettings();
						} catch (Exception ex) {
							ex.printStackTrace();
							JOptionPane.showOptionDialog(new JPanel(), "Settings applied but failed to save for next session", "ERROR",
									JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE,
									null, null, null);
						}
						//refresh
						updatePanel.update(null, "reload Versions");
						dispose();
					} catch (Exception ex) {
						ex.printStackTrace();
						JOptionPane.showOptionDialog(new JPanel(), "Exception: " + ex.getClass().getSimpleName() + ": " + ex.getMessage(), "ERROR",
								JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE,
								null, null, null);
					}

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
		{
			JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			getContentPane().add(tabbedPane, BorderLayout.NORTH);
			tabbedPane.addTab("Path", null, contentPanel, null);
			contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			GridBagLayout gbl_contentPanel = new GridBagLayout();
			gbl_contentPanel.columnWidths = new int[]{0, 0};
			gbl_contentPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
			gbl_contentPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gbl_contentPanel.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			contentPanel.setLayout(gbl_contentPanel);
			{
				JLabel lblClientSingle = new JLabel("Installation Settings");
				GridBagConstraints gbc_lblClientSingle = new GridBagConstraints();
				gbc_lblClientSingle.insets = new Insets(0, 0, 5, 0);
				gbc_lblClientSingle.gridx = 0;
				gbc_lblClientSingle.gridy = 0;
				contentPanel.add(lblClientSingle, gbc_lblClientSingle);
			}
			{
				JPanel panel = new JPanel();
				panel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Path", TitledBorder.LEADING, TitledBorder.TOP, null, null));
				GridBagConstraints gbc_panel = new GridBagConstraints();
				gbc_panel.insets = new Insets(0, 0, 5, 0);
				gbc_panel.fill = GridBagConstraints.BOTH;
				gbc_panel.gridx = 0;
				gbc_panel.gridy = 2;
				contentPanel.add(panel, gbc_panel);
				GridBagLayout gbl_panel = new GridBagLayout();
				gbl_panel.columnWidths = new int[]{0, 0};
				gbl_panel.rowHeights = new int[]{0, 0};
				gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
				gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
				panel.setLayout(gbl_panel);
				{
					pathField = new JTextField();
					GridBagConstraints gbc_pathField = new GridBagConstraints();
					gbc_pathField.fill = GridBagConstraints.HORIZONTAL;
					gbc_pathField.gridx = 0;
					gbc_pathField.gridy = 0;
					panel.add(pathField, gbc_pathField);

					pathField.setText(UpdatePanel.installDir);

					pathField.setColumns(10);
				}
			}
			{
				JButton btnBrowse = new JButton("Browse");
				btnBrowse.addActionListener(e -> {

					File dest = new FileExt(pathField.getText());

					if (!dest.exists()) {
						dest = new FileExt("." + File.separator);
					}

					JFileChooser j = new JFileChooser(dest);
					j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int opt = j.showSaveDialog(f);

					if (opt == JFileChooser.APPROVE_OPTION) {
						File selectedFile = j.getSelectedFile();

						if (selectedFile.isDirectory()) {
							pathField.setText(selectedFile.getAbsolutePath());
						}
					}

				});
				GridBagConstraints gbc_btnBrowse = new GridBagConstraints();
				gbc_btnBrowse.anchor = GridBagConstraints.EAST;
				gbc_btnBrowse.insets = new Insets(0, 0, 5, 0);
				gbc_btnBrowse.gridx = 0;
				gbc_btnBrowse.gridy = 3;
				contentPanel.add(btnBrowse, gbc_btnBrowse);
			}
			{
				JLabel lblBuildBranch = new JLabel("Build Branch");
				GridBagConstraints gbc_lblBuildBranch = new GridBagConstraints();
				gbc_lblBuildBranch.insets = new Insets(0, 0, 5, 0);
				gbc_lblBuildBranch.gridx = 0;
				gbc_lblBuildBranch.gridy = 4;
				contentPanel.add(lblBuildBranch, gbc_lblBuildBranch);
			}
			{
				buildBranchBox = new JComboBox(VersionFile.values());
				GridBagConstraints gbc_buildBranchBox = new GridBagConstraints();
				gbc_buildBranchBox.fill = GridBagConstraints.HORIZONTAL;
				gbc_buildBranchBox.gridx = 0;
				gbc_buildBranchBox.gridy = 5;
				contentPanel.add(buildBranchBox, gbc_buildBranchBox);
				buildBranchBox.setSelectedItem(UpdatePanel.buildBranch);
			}

		}
	}

}
