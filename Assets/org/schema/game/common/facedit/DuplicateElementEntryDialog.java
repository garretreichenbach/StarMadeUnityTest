package org.schema.game.common.facedit;

import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.util.GuiErrorHandler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class DuplicateElementEntryDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private String name = "";
	private short type = -1;

	public DuplicateElementEntryDialog(final JFrame frame, final ElementChoseInterface elementChoseInterface, final ElementInformation currentInfo) {
		super(frame, true);
		setSize(400, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] {0};
		gbl_contentPanel.rowHeights = new int[] {0, 0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[] {1.0};
		gbl_contentPanel.rowWeights = new double[] {1.0, 0.0, 0.0, 0.0, 1.0};
		contentPanel.setLayout(gbl_contentPanel);
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Block Id", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel.insets = new Insets(10, 0, 10, 0);
		gbc_panel.weightx = 1.0;
		gbc_panel.anchor = GridBagConstraints.NORTHWEST;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		contentPanel.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] {0, 0, 0};
		gbl_panel.rowHeights = new int[] {0, 0};
		gbl_panel.columnWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[] {0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		final JLabel label = new JLabel("----");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.insets = new Insets(5, 5, 5, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = 0;
		panel.add(label, gbc_label);

		JButton pickButton = new JButton("Create & Pick");
		pickButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					ElementKeyMap.reparseProperties();
				} catch(IOException e1) {
					e1.printStackTrace();
					GuiErrorHandler.processErrorDialogException(e1);
				}
				String name = JOptionPane.showInputDialog(frame, "Enter ID name");
				if(!Pattern.matches("[A-Z0-9_]+", name)) {
					Object[] op = {"cancel", "retry"};
					int n = JOptionPane.showOptionDialog(frame, "Error: must only contain capital letters, numbers and underscores", "Error", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, op, op[0]);
					switch(n) {
						case 0:
							break;
						case 1:
							actionPerformed(event);
							break;
					}
				} else {
					for(int i = 1; i < Short.MAX_VALUE; i++) {
						boolean ok = true;
						for(Map.Entry<Object, Object> e : ElementKeyMap.properties.entrySet()) {
							if(Integer.parseInt(e.getValue().toString()) == i) {
								ok = false;
								break;
							}
						}
						if(ok) {
							type = (short) i;
							break;
						}
					}
					ElementKeyMap.properties.setProperty(name, String.valueOf(type));
					ElementKeyMap.writePropertiesOrdered();
					label.setText(name);
				}
			}
		});

		JButton btnPick = new JButton("Pick");
		btnPick.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent asd) {
				try {
					ElementKeyMap.reparseProperties();
				} catch(IOException e1) {
					e1.printStackTrace();
					GuiErrorHandler.processErrorDialogException(e1);
				}

				HashSet<String> m = new HashSet<>();
				for(Map.Entry<Object, Object> e : ElementKeyMap.properties.entrySet()) {
					if(!ElementKeyMap.keySet.contains(Short.parseShort(e.getValue().toString()))) {
						m.add(e.getKey().toString());
					}
				}
				if(m.isEmpty()) {
					Object[] op = {"cancel", "retry"};
					int n = JOptionPane.showOptionDialog(frame, "Error: No Free ID's available in BlockTypes.properties\n" + "Please add a new entry and try again!", "No ID available", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, op, op[0]);
					switch(n) {
						case 0:
							break;
						case 1:
							actionPerformed(asd);
							break;
					}
				} else {

					Object[] possibilities = new Object[m.size()];
					int in = 0;
					for(String s : m) {
						possibilities[in] = s;
						in++;
					}
					Arrays.sort(possibilities);
					String s = (String) JOptionPane.showInputDialog(frame, "Pick an ID from the list", "Pick ID", JOptionPane.PLAIN_MESSAGE, null, possibilities, possibilities[0]);

					if((s != null) && (!s.isEmpty())) {
						type = Short.parseShort(ElementKeyMap.properties.getProperty(s));
						label.setText(s);
					}
				}

			}
		});
		GridBagConstraints gbc_btnPick = new GridBagConstraints();
		gbc_btnPick.weightx = 1.0;
		gbc_btnPick.anchor = GridBagConstraints.EAST;
		gbc_btnPick.gridx = 1;
		gbc_btnPick.gridy = 0;
		panel.add(btnPick, gbc_btnPick);
		GridBagConstraints gbc_btnCPick = new GridBagConstraints();
		gbc_btnCPick.weightx = 1.0;
		gbc_btnCPick.anchor = GridBagConstraints.EAST;
		gbc_btnCPick.gridx = 2;
		gbc_btnCPick.gridy = 0;
		panel.add(btnPick, gbc_btnCPick);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "Name", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel_1.anchor = GridBagConstraints.NORTHWEST;
		gbc_panel_1.weightx = 1.0;
		gbc_panel_1.insets = new Insets(10, 0, 10, 0);
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 3;
		contentPanel.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] {0, 0, 0};
		gbl_panel_1.rowHeights = new int[] {0, 0};
		gbl_panel_1.columnWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[] {0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		JLabel label_ = new JLabel("----");
		GridBagConstraints gbc_label_ = new GridBagConstraints();
		gbc_label_.insets = new Insets(5, 5, 0, 5);
		gbc_label_.gridx = 0;
		gbc_label_.gridy = 0;
		panel_1.add(label_, gbc_label);

		JButton button = new JButton("Pick");
		GridBagConstraints gbc_button = new GridBagConstraints();
		gbc_button.weightx = 1.0;
		gbc_button.anchor = GridBagConstraints.EAST;
		gbc_button.gridx = 1;
		gbc_button.gridy = 0;
		panel_1.add(button, gbc_button);

		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String s = (String) JOptionPane.showInputDialog(frame, "Pick a Name", "Pick Name", JOptionPane.PLAIN_MESSAGE, null, null, null);
				if(s != null && !s.isEmpty()) {
					name = s.trim();
					label.setText(name);
				}
			}
		});

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		JButton okButton = new JButton("OK");
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);
		okButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if(type < 0 || name.isEmpty()) JOptionPane.showMessageDialog(frame, "Every field in this dialog\n" + "has to be filled.", "Error", JOptionPane.ERROR_MESSAGE);
				else {
					ElementInformation info = new ElementInformation(currentInfo, type, name);
					try {
						info.styleIds = new short[0];
						info.slabIds = new short[0];
						info.price = currentInfo.price;
						info.producedInFactory = currentInfo.producedInFactory;
						info.mass = currentInfo.mass;
						info.volume = currentInfo.volume;
						info.maxHitPointsFull = currentInfo.maxHitPointsFull;
						info.inRecipe = currentInfo.inRecipe;
						info.setProducedInFactory(currentInfo.getProducedInFactory());
						info.consistence.addAll(currentInfo.consistence);
						info.description = currentInfo.description;
						info.type = currentInfo.type;
						ElementKeyMap.addInformationToExisting(info);
						dispose();
						elementChoseInterface.onEnter(info);
					} catch(ParserConfigurationException e1) {
						e1.printStackTrace();
						GuiErrorHandler.processErrorDialogException(e1);
					}
				}
			}
		});
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
	}
}