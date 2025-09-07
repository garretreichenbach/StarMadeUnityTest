package org.schema.game.common.updater;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.schema.game.common.version.VersionContainer;

public class InstallOptions extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private final JPanel contentPanel = new JPanel();
	/**
	 * Create the dialog.
	 *
	 * @param updatePanel
	 */
	private JComboBox comboBox;

	public InstallOptions(final UpdatePanel updatePanel, JFrame f) {
		super(f);
		setBounds(100, 100, 887, 354);
		setTitle("Install Options");
		getContentPane().setLayout(new BorderLayout());
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(e -> {
					try {

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
			tabbedPane.addTab("Download Options", null, contentPanel, null);
			contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			GridBagLayout gbl_contentPanel = new GridBagLayout();
			gbl_contentPanel.columnWidths = new int[]{0, 0};
			gbl_contentPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
			gbl_contentPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gbl_contentPanel.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
			contentPanel.setLayout(gbl_contentPanel);
			{
				JPanel panel = new JPanel();
				panel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Build Switcher", TitledBorder.LEADING, TitledBorder.TOP, null, null));
				GridBagConstraints gbc_panel = new GridBagConstraints();
				gbc_panel.insets = new Insets(0, 0, 5, 0);
				gbc_panel.fill = GridBagConstraints.BOTH;
				gbc_panel.gridx = 0;
				gbc_panel.gridy = 2;
				contentPanel.add(panel, gbc_panel);
				GridBagLayout gbl_panel = new GridBagLayout();
				gbl_panel.columnWidths = new int[]{0, 0, 0};
				gbl_panel.rowHeights = new int[]{0, 0, 0};
				gbl_panel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
				gbl_panel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
				panel.setLayout(gbl_panel);
				{

					IndexFileEntry[] e = new IndexFileEntry[updatePanel.updater.versions.size()];

					for (int i = 0; i < e.length; i++) {
						e[i] = updatePanel.updater.versions.get(i);
					}
					comboBox = new JComboBox(e);
					GridBagConstraints gbc_comboBox = new GridBagConstraints();
					gbc_comboBox.gridwidth = 2;
					gbc_comboBox.insets = new Insets(0, 0, 5, 0);
					gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
					gbc_comboBox.gridx = 0;
					gbc_comboBox.gridy = 0;
					panel.add(comboBox, gbc_comboBox);

					if (!VersionContainer.build.equals("undefined")) {
						for (int i = 0; i < updatePanel.updater.versions.size(); i++) {
							IndexFileEntry in = updatePanel.updater.versions.get(i);
							if (in.equals(VersionContainer.build)) {
								comboBox.setSelectedItem(in);
							}
						}
					} else {
						if (updatePanel.updater.versions.size() > 0) {
							IndexFileEntry in = updatePanel.updater.versions.get(updatePanel.updater.versions.size() - 1);
							comboBox.setSelectedItem(in);
						}
					}
				}
				{
					JButton btnNewButton = new JButton("Search newest");
					btnNewButton.addActionListener(e -> {
						if (updatePanel.updater.versions.size() > 0) {
							IndexFileEntry in = updatePanel.updater.versions.get(updatePanel.updater.versions.size() - 1);
							comboBox.setSelectedItem(in);
						}
					});
					GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
					gbc_btnNewButton.anchor = GridBagConstraints.WEST;
					gbc_btnNewButton.insets = new Insets(0, 0, 0, 5);
					gbc_btnNewButton.gridx = 0;
					gbc_btnNewButton.gridy = 1;
					panel.add(btnNewButton, gbc_btnNewButton);
				}
				{
					JButton btnResetToCurrently = new JButton("Search currently installed");
					btnResetToCurrently.addActionListener(e -> {
						if (!VersionContainer.build.equals("undefined")) {
							for (int i = 0; i < updatePanel.updater.versions.size(); i++) {
								IndexFileEntry in = updatePanel.updater.versions.get(i);
								if (in.build.equals(VersionContainer.build)) {
									comboBox.setSelectedItem(in);
								}
							}
						}
					});
					GridBagConstraints gbc_btnResetToCurrently = new GridBagConstraints();
					gbc_btnResetToCurrently.anchor = GridBagConstraints.EAST;
					gbc_btnResetToCurrently.gridx = 1;
					gbc_btnResetToCurrently.gridy = 1;
					panel.add(btnResetToCurrently, gbc_btnResetToCurrently);
				}
			}

		}
		{
			JPanel panelOtherOptions = new JPanel();
			panelOtherOptions.setBorder(new TitledBorder(null, "Other Options", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			GridBagConstraints gbc_panelOtherOptions = new GridBagConstraints();
			gbc_panelOtherOptions.insets = new Insets(0, 0, 5, 0);
			gbc_panelOtherOptions.fill = GridBagConstraints.BOTH;
			gbc_panelOtherOptions.gridx = 0;
			gbc_panelOtherOptions.gridy = 3;
			contentPanel.add(panelOtherOptions, gbc_panelOtherOptions);
			GridBagLayout gbl_panelOtherOptions = new GridBagLayout();
			gbl_panelOtherOptions.columnWidths = new int[]{0, 0, 0};
			gbl_panelOtherOptions.rowHeights = new int[]{0, 0, 0, 0};
			gbl_panelOtherOptions.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
			gbl_panelOtherOptions.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
			panelOtherOptions.setLayout(gbl_panelOtherOptions);
			{
				JButton btnDownloadNormal = new JButton("Download Normal");
				btnDownloadNormal.addActionListener(e -> {

					updatePanel.updater.startUpdateNew(UpdatePanel.installDir, (IndexFileEntry) comboBox.getSelectedItem(), false, 0);
					dispose();
				});
				GridBagConstraints gbc_btnDownloadNormal = new GridBagConstraints();
				gbc_btnDownloadNormal.anchor = GridBagConstraints.WEST;
				gbc_btnDownloadNormal.insets = new Insets(0, 0, 5, 5);
				gbc_btnDownloadNormal.gridx = 0;
				gbc_btnDownloadNormal.gridy = 0;
				panelOtherOptions.add(btnDownloadNormal, gbc_btnDownloadNormal);
			}
			{
				JButton btnDownloadAll = new JButton("Force Download All & Overwrite");
				btnDownloadAll.addActionListener(e -> {
					updatePanel.updater.startUpdateNew(UpdatePanel.installDir, (IndexFileEntry) comboBox.getSelectedItem(), true, 0);
					dispose();
				});
				GridBagConstraints gbc_btnDownloadAll = new GridBagConstraints();
				gbc_btnDownloadAll.weightx = 1.0;
				gbc_btnDownloadAll.anchor = GridBagConstraints.EAST;
				gbc_btnDownloadAll.insets = new Insets(0, 0, 5, 0);
				gbc_btnDownloadAll.gridx = 1;
				gbc_btnDownloadAll.gridy = 0;
				panelOtherOptions.add(btnDownloadAll, gbc_btnDownloadAll);
			}
			{
				JLabel lblUseNormalDownload = new JLabel("Use normal download to repair your current installation or quickly switch to another version");
				GridBagConstraints gbc_lblUseNormalDownload = new GridBagConstraints();
				gbc_lblUseNormalDownload.anchor = GridBagConstraints.WEST;
				gbc_lblUseNormalDownload.insets = new Insets(0, 5, 5, 0);
				gbc_lblUseNormalDownload.gridwidth = 2;
				gbc_lblUseNormalDownload.gridx = 0;
				gbc_lblUseNormalDownload.gridy = 1;
				panelOtherOptions.add(lblUseNormalDownload, gbc_lblUseNormalDownload);
			}
			{
				JLabel lblForceDownload = new JLabel("Use \"Force Download All & Override\" to re-download and replace every file (takes longer)");
				GridBagConstraints gbc_lblForceDownload = new GridBagConstraints();
				gbc_lblForceDownload.anchor = GridBagConstraints.WEST;
				gbc_lblForceDownload.gridwidth = 2;
				gbc_lblForceDownload.insets = new Insets(0, 5, 5, 0);
				gbc_lblForceDownload.gridx = 0;
				gbc_lblForceDownload.gridy = 2;
				panelOtherOptions.add(lblForceDownload, gbc_lblForceDownload);
			}
		}
	}

}
