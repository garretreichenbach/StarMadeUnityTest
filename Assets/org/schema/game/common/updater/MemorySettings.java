package org.schema.game.common.updater;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.schema.common.util.security.OperatingSystem;
import org.schema.game.common.updater.Updater.VersionFile;
import org.schema.schine.resource.FileExt;

public class MemorySettings extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private final JPanel contentPanel = new JPanel();
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField textField_3;
	private JTextField textField_4;
	private JTextField textField_5;

	/**
	 * Create the dialog.
	 */
	public MemorySettings(JFrame f) {
		super(f);
		setTitle("Memory Settings");
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

						if (is64Bit()) {
							UpdatePanel.maxMemory = Integer.parseInt(textField.getText());
							UpdatePanel.minMemory = Integer.parseInt(textField_1.getText());
							UpdatePanel.earlyGenMemory = Integer.parseInt(textField_2.getText());
						} else {
							UpdatePanel.maxMemory32 = Integer.parseInt(textField.getText());
							UpdatePanel.minMemory32 = Integer.parseInt(textField_1.getText());
							UpdatePanel.earlyGenMemory32 = Integer.parseInt(textField_2.getText());
						}

						UpdatePanel.serverMaxMemory = Integer.parseInt(textField_3.getText());
						UpdatePanel.serverMinMemory = Integer.parseInt(textField_4.getText());
						UpdatePanel.serverEarlyGenMemory = Integer.parseInt(textField_5.getText());

						try {
							saveSettings();
						} catch (Exception ex) {
							ex.printStackTrace();
							JOptionPane.showOptionDialog(new JPanel(), "Settings applied but failed to save for next session", "ERROR",
									JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE,
									null, null, null);
						}

						dispose();
					} catch (Exception ex) {
						ex.printStackTrace();
						JOptionPane.showOptionDialog(new JPanel(), "Please only use numbers", "ERROR",
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
			tabbedPane.addTab("Client & SinglePlayer", null, contentPanel, null);
			contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			GridBagLayout gbl_contentPanel = new GridBagLayout();
			gbl_contentPanel.columnWidths = new int[]{0, 0};
			gbl_contentPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
			gbl_contentPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gbl_contentPanel.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			contentPanel.setLayout(gbl_contentPanel);
			{
				JLabel lblClientSingle = new JLabel("Client & Single Player Memory Settings");
				GridBagConstraints gbc_lblClientSingle = new GridBagConstraints();
				gbc_lblClientSingle.insets = new Insets(0, 0, 5, 0);
				gbc_lblClientSingle.gridx = 0;
				gbc_lblClientSingle.gridy = 0;
				contentPanel.add(lblClientSingle, gbc_lblClientSingle);
			}
			{
				JTextPane txtpnPleaseKeepIn = new JTextPane();
				txtpnPleaseKeepIn.setText("Please keep in mind that 32 bit OS have a limit of allocating memory. Should 1024 throw out of memory exceptions, please try less then 1024");
				GridBagConstraints gbc_txtpnPleaseKeepIn = new GridBagConstraints();
				gbc_txtpnPleaseKeepIn.insets = new Insets(0, 0, 5, 0);
				gbc_txtpnPleaseKeepIn.fill = GridBagConstraints.BOTH;
				gbc_txtpnPleaseKeepIn.gridx = 0;
				gbc_txtpnPleaseKeepIn.gridy = 1;
				contentPanel.add(txtpnPleaseKeepIn, gbc_txtpnPleaseKeepIn);
			}
			{
				JPanel panel = new JPanel();
				panel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Maximal Memory (MB)", TitledBorder.LEADING, TitledBorder.TOP, null, null));
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
					textField = new JTextField();
					GridBagConstraints gbc_textField = new GridBagConstraints();
					gbc_textField.fill = GridBagConstraints.HORIZONTAL;
					gbc_textField.gridx = 0;
					gbc_textField.gridy = 0;
					panel.add(textField, gbc_textField);
					if (is64Bit()) {
						textField.setText(String.valueOf(UpdatePanel.maxMemory));
					} else {
						textField.setText(String.valueOf(UpdatePanel.maxMemory32));
					}
					textField.setColumns(10);
				}
			}
			{
				JPanel panel = new JPanel();
				panel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Initial Memory (MB)", TitledBorder.LEADING, TitledBorder.TOP, null, null));
				GridBagConstraints gbc_panel = new GridBagConstraints();
				gbc_panel.insets = new Insets(0, 0, 5, 0);
				gbc_panel.fill = GridBagConstraints.BOTH;
				gbc_panel.gridx = 0;
				gbc_panel.gridy = 3;
				contentPanel.add(panel, gbc_panel);
				GridBagLayout gbl_panel = new GridBagLayout();
				gbl_panel.columnWidths = new int[]{0, 0};
				gbl_panel.rowHeights = new int[]{0, 0};
				gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
				gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
				panel.setLayout(gbl_panel);
				{
					textField_1 = new JTextField();
					GridBagConstraints gbc_textField_1 = new GridBagConstraints();
					gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
					gbc_textField_1.gridx = 0;
					gbc_textField_1.gridy = 0;
					panel.add(textField_1, gbc_textField_1);

					if (is64Bit()) {
						textField_1.setText(String.valueOf(UpdatePanel.minMemory));
					} else {
						textField_1.setText(String.valueOf(UpdatePanel.minMemory32));
					}
					textField_1.setColumns(10);
				}
			}
			{
				JPanel panel = new JPanel();
				panel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Early Generation Memory", TitledBorder.LEADING, TitledBorder.TOP, null, null));
				GridBagConstraints gbc_panel = new GridBagConstraints();
				gbc_panel.fill = GridBagConstraints.BOTH;
				gbc_panel.gridx = 0;
				gbc_panel.gridy = 4;
				contentPanel.add(panel, gbc_panel);
				GridBagLayout gbl_panel = new GridBagLayout();
				gbl_panel.columnWidths = new int[]{0, 0};
				gbl_panel.rowHeights = new int[]{0, 0};
				gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
				gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
				panel.setLayout(gbl_panel);
				{
					textField_2 = new JTextField();
					GridBagConstraints gbc_textField_2 = new GridBagConstraints();
					gbc_textField_2.fill = GridBagConstraints.HORIZONTAL;
					gbc_textField_2.gridx = 0;
					gbc_textField_2.gridy = 0;
					panel.add(textField_2, gbc_textField_2);
					if (is64Bit()) {
						textField_2.setText(String.valueOf(UpdatePanel.earlyGenMemory));
					} else {
						textField_2.setText(String.valueOf(UpdatePanel.earlyGenMemory32));
					}

					textField_2.setColumns(10);
				}
			}
			{
				JPanel panel = new JPanel();
				panel.setBorder(new EmptyBorder(5, 5, 5, 5));
				tabbedPane.addTab("Dedicated Server", null, panel, null);
				GridBagLayout gbl_panel = new GridBagLayout();
				gbl_panel.columnWidths = new int[]{0, 0};
				gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
				gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
				gbl_panel.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
				panel.setLayout(gbl_panel);
				{
					JLabel lblDedicatedServerMemory = new JLabel("Dedicated Server Memory Settings");
					GridBagConstraints gbc_lblDedicatedServerMemory = new GridBagConstraints();
					gbc_lblDedicatedServerMemory.insets = new Insets(0, 0, 5, 0);
					gbc_lblDedicatedServerMemory.gridx = 0;
					gbc_lblDedicatedServerMemory.gridy = 0;
					panel.add(lblDedicatedServerMemory, gbc_lblDedicatedServerMemory);
				}
				{
					JTextPane textPane = new JTextPane();
					textPane.setText("Please keep in mind that 32 bit OS have a limit of allocating memory. Should 1024 throw out of memory exceptions, please try less then 1024");
					GridBagConstraints gbc_textPane = new GridBagConstraints();
					gbc_textPane.fill = GridBagConstraints.BOTH;
					gbc_textPane.insets = new Insets(0, 0, 5, 0);
					gbc_textPane.gridx = 0;
					gbc_textPane.gridy = 1;
					panel.add(textPane, gbc_textPane);
				}
				{
					JPanel panel_1 = new JPanel();
					panel_1.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Maximal Memory (MB)", TitledBorder.LEADING, TitledBorder.TOP, null, null));
					GridBagConstraints gbc_panel_1 = new GridBagConstraints();
					gbc_panel_1.fill = GridBagConstraints.BOTH;
					gbc_panel_1.insets = new Insets(0, 0, 5, 0);
					gbc_panel_1.gridx = 0;
					gbc_panel_1.gridy = 2;
					panel.add(panel_1, gbc_panel_1);
					GridBagLayout gbl_panel_1 = new GridBagLayout();
					gbl_panel_1.columnWidths = new int[]{0, 0};
					gbl_panel_1.rowHeights = new int[]{0, 0};
					gbl_panel_1.columnWeights = new double[]{1.0, Double.MIN_VALUE};
					gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
					panel_1.setLayout(gbl_panel_1);
					{
						textField_3 = new JTextField();
						textField_3.setText(String.valueOf(UpdatePanel.serverMaxMemory));
						textField_3.setColumns(10);
						GridBagConstraints gbc_textField_3 = new GridBagConstraints();
						gbc_textField_3.fill = GridBagConstraints.HORIZONTAL;
						gbc_textField_3.gridx = 0;
						gbc_textField_3.gridy = 0;
						panel_1.add(textField_3, gbc_textField_3);
					}
				}
				{
					JPanel panel_1 = new JPanel();
					panel_1.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Initial Memory (MB)", TitledBorder.LEADING, TitledBorder.TOP, null, null));
					GridBagConstraints gbc_panel_1 = new GridBagConstraints();
					gbc_panel_1.fill = GridBagConstraints.BOTH;
					gbc_panel_1.insets = new Insets(0, 0, 5, 0);
					gbc_panel_1.gridx = 0;
					gbc_panel_1.gridy = 3;
					panel.add(panel_1, gbc_panel_1);
					GridBagLayout gbl_panel_1 = new GridBagLayout();
					gbl_panel_1.columnWidths = new int[]{0, 0};
					gbl_panel_1.rowHeights = new int[]{0, 0};
					gbl_panel_1.columnWeights = new double[]{1.0, Double.MIN_VALUE};
					gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
					panel_1.setLayout(gbl_panel_1);
					{
						textField_4 = new JTextField();
						textField_4.setText(String.valueOf(UpdatePanel.serverMinMemory));
						textField_4.setColumns(10);
						GridBagConstraints gbc_textField_4 = new GridBagConstraints();
						gbc_textField_4.fill = GridBagConstraints.HORIZONTAL;
						gbc_textField_4.gridx = 0;
						gbc_textField_4.gridy = 0;
						panel_1.add(textField_4, gbc_textField_4);
					}
				}
				{
					JPanel panel_1 = new JPanel();
					panel_1.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Early Generation Memory", TitledBorder.LEADING, TitledBorder.TOP, null, null));
					GridBagConstraints gbc_panel_1 = new GridBagConstraints();
					gbc_panel_1.fill = GridBagConstraints.BOTH;
					gbc_panel_1.gridx = 0;
					gbc_panel_1.gridy = 4;
					panel.add(panel_1, gbc_panel_1);
					GridBagLayout gbl_panel_1 = new GridBagLayout();
					gbl_panel_1.columnWidths = new int[]{0, 0};
					gbl_panel_1.rowHeights = new int[]{0, 0};
					gbl_panel_1.columnWeights = new double[]{1.0, Double.MIN_VALUE};
					gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
					panel_1.setLayout(gbl_panel_1);
					{
						textField_5 = new JTextField();
						textField_5.setText(String.valueOf(UpdatePanel.serverEarlyGenMemory));
						textField_5.setColumns(10);
						GridBagConstraints gbc_textField_5 = new GridBagConstraints();
						gbc_textField_5.fill = GridBagConstraints.HORIZONTAL;
						gbc_textField_5.gridx = 0;
						gbc_textField_5.gridy = 0;
						panel_1.add(textField_5, gbc_textField_5);
					}
				}
			}
		}
	}

	public static boolean is64Bit() {
		return System.getProperty("os.arch").contains("64");
	}

	public static void loadSettings() throws IOException {

		File file = new FileExt(OperatingSystem.getAppDir(), "settings.properties");
		Properties p = new Properties();

		if (file.exists()) {
			FileInputStream fs = new FileInputStream(file);
			p.load(fs);
			fs.close();
		} else {
			System.err.println("ERROR, FILE DOES NOT EXIST: " + file.getAbsolutePath());
			return;
		}

		if (p.get("maxMemory") != null)
			UpdatePanel.maxMemory = Integer.parseInt(p.get("maxMemory").toString());

		if (p.get("minMemory") != null)
			UpdatePanel.minMemory = Integer.parseInt(p.get("minMemory").toString());

		if (p.get("earlyGenMemory") != null)
			UpdatePanel.earlyGenMemory = Integer.parseInt(p.get("earlyGenMemory").toString());

		if (p.get("maxMemory32") != null)
			UpdatePanel.maxMemory32 = Integer.parseInt(p.get("maxMemory32").toString());

		if (p.get("minMemory32") != null)
			UpdatePanel.minMemory32 = Integer.parseInt(p.get("minMemory32").toString());

		if (p.get("earlyGenMemory32") != null)
			UpdatePanel.earlyGenMemory32 = Integer.parseInt(p.get("earlyGenMemory32").toString());

		if (p.get("serverMaxMemory") != null)
			UpdatePanel.serverMaxMemory = Integer.parseInt(p.get("serverMaxMemory").toString());

		if (p.get("serverMinMemory") != null)
			UpdatePanel.serverMinMemory = Integer.parseInt(p.get("serverMinMemory").toString());

		if (p.get("serverEarlyGenMemory") != null)
			UpdatePanel.serverEarlyGenMemory = Integer.parseInt(p.get("serverEarlyGenMemory").toString());

		if (p.get("port") != null)
			UpdatePanel.port = Integer.parseInt(p.get("port").toString());

		if (p.get("installDir") != null)
			UpdatePanel.installDir = p.get("installDir").toString();

		if (p.get("buildBranch") != null)
			UpdatePanel.buildBranch = VersionFile.valueOf(p.get("buildBranch").toString());

		file.createNewFile();
		FileOutputStream fo = new FileOutputStream(file);
		p.store(fo, "Properties for the StarMade Starter");
		fo.flush();
		fo.close();

	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			MemorySettings dialog = new MemorySettings(new JFrame());
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void saveSettings() throws IOException {

		File file = new FileExt(OperatingSystem.getAppDir(), "settings.properties");
		Properties p = new Properties();

		p.put("maxMemory", String.valueOf(UpdatePanel.maxMemory));
		p.put("minMemory", String.valueOf(UpdatePanel.minMemory));
		p.put("earlyGenMemory", String.valueOf(UpdatePanel.earlyGenMemory));

		p.put("maxMemory32", String.valueOf(UpdatePanel.maxMemory32));
		p.put("minMemory32", String.valueOf(UpdatePanel.minMemory32));
		p.put("earlyGenMemory32", String.valueOf(UpdatePanel.earlyGenMemory32));

		p.put("serverMaxMemory", String.valueOf(UpdatePanel.serverMaxMemory));
		p.put("serverMinMemory", String.valueOf(UpdatePanel.serverMinMemory));
		p.put("serverEarlyGenMemory", String.valueOf(UpdatePanel.serverEarlyGenMemory));

		p.put("port", String.valueOf(UpdatePanel.port));

		p.put("installDir", UpdatePanel.installDir);
		p.put("buildBranch", UpdatePanel.buildBranch.name());

		file.createNewFile();
		FileOutputStream fo = new FileOutputStream(file);
		p.store(fo, "Properties for the StarMade Starter");
		fo.flush();
		fo.close();
		System.out.println("Memory Settings saved to: " + file.getAbsolutePath());

	}

}
