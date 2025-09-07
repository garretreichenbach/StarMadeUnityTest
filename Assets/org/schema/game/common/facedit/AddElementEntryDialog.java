package org.schema.game.common.facedit;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.xml.parsers.ParserConfigurationException;

import org.schema.game.common.data.element.ElementCategory;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.util.GuiErrorHandler;

import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;

public class AddElementEntryDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	public static ShortOpenHashSet addedBuildIcons = new ShortOpenHashSet();
	private final JPanel contentPanel = new JPanel();
	private short type = -1;
	private String name = "";
	private short textureId[] = new short[]{-1, -1, -1, -1, -1, -1};
	private int sides = 1;
	private short icon = -1;
	private ElementCategory category = null;

	/**
	 * Create the dialog.
	 */
	public AddElementEntryDialog(final JFrame frame, final ElementChoseInterface callback) {
		super(frame, true);
		setBounds(100, 100, 491, 453);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0};
		gbl_contentPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{1.0};
		gbl_contentPanel.rowWeights = new double[]{1.0, 0.0, 0.0, 0.0, 1.0};
		contentPanel.setLayout(gbl_contentPanel);
		{
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
			gbl_panel.columnWidths = new int[]{0, 0, 0};
			gbl_panel.rowHeights = new int[]{0, 0};
			gbl_panel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
			gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
			panel.setLayout(gbl_panel);

			final JLabel label = new JLabel("----");
			GridBagConstraints gbc_label = new GridBagConstraints();
			gbc_label.insets = new Insets(5, 5, 5, 5);
			gbc_label.gridx = 0;
			gbc_label.gridy = 0;
			panel.add(label, gbc_label);

			JButton btnCPick = new JButton("Create & Pick");
			btnCPick.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent asd) {
					try {
						ElementKeyMap.reparseProperties();
					} catch (IOException e1) {
						e1.printStackTrace();
						GuiErrorHandler.processErrorDialogException(e1);
					}

					String name = JOptionPane.showInputDialog(frame, "Enter ID name");
					
					if(!Pattern.matches("[A-Z0-9_]+", name)){
						Object[] op = new Object[]{"cancel", "retry"};
						int n = JOptionPane.showOptionDialog(frame, "Error: must only contain capital letters, numbers and underscores","Error",
								JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, op, op[0]);
						switch (n) {
							case 0:
								break;
							case 1:
								actionPerformed(asd);
								break;
						}
					}else{
						for(int i = 1; i < Short.MAX_VALUE; i++){
							boolean ok = true;
							for(Entry<Object, Object> e : ElementKeyMap.properties.entrySet()){
								if(Integer.parseInt(e.getValue().toString())== i){
									ok = false;
									break;
								}
							}
							if(ok){
								type = (short) i;
								break;
							}
						}
						ElementKeyMap.properties.put(name, String.valueOf(type));
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
					} catch (IOException e1) {
						e1.printStackTrace();
						GuiErrorHandler.processErrorDialogException(e1);
					}

					;
					HashSet<String> m = new HashSet<String>();
					for (Entry<Object, Object> e : ElementKeyMap.properties.entrySet()) {
						if (!ElementKeyMap.keySet.contains(Short.parseShort(e.getValue().toString()))) {
							m.add(e.getKey().toString());
						}
					}
					if (m.isEmpty()) {
						Object[] op = new Object[]{"cancel", "retry"};
						int n = JOptionPane.showOptionDialog(frame, "Error: No Free ID's available in BlockTypes.properties\n" +
										"Please add a new entry and try again!", "No ID available",
								JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, op, op[0]);
						switch (n) {
							case 0:
								break;
							case 1:
								actionPerformed(asd);
								break;
						}
					} else {

						Object[] possibilities = new Object[m.size()];
						int in = 0;
						for (String s : m) {
							possibilities[in] = s;
							in++;
						}
						Arrays.sort(possibilities);
						String s = (String) JOptionPane.showInputDialog(
								frame,
								"Pick an ID from the list",
								"Pick ID",
								JOptionPane.PLAIN_MESSAGE,
								null,
								possibilities,
								possibilities[0]);

						if ((s != null) && (s.length() > 0)) {
							type = Short.parseShort(ElementKeyMap.properties.get(s).toString());
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
			panel.add(btnCPick, gbc_btnCPick);

		}
		{
			JPanel panel = new JPanel();
			panel.setBorder(new TitledBorder(null, "Texture ID", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			GridBagConstraints gbc_panel = new GridBagConstraints();
			gbc_panel.anchor = GridBagConstraints.WEST;
			gbc_panel.insets = new Insets(10, 0, 10, 0);
			gbc_panel.fill = GridBagConstraints.BOTH;
			gbc_panel.gridx = 0;
			gbc_panel.gridy = 1;
			contentPanel.add(panel, gbc_panel);
			GridBagLayout gbl_panel = new GridBagLayout();
			gbl_panel.columnWidths = new int[]{0, 0, 0};
			gbl_panel.rowHeights = new int[]{0, 0};
			gbl_panel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
			gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
			panel.setLayout(gbl_panel);
			{
				final JLabel label = new JLabel("----");
				GridBagConstraints gbc_label = new GridBagConstraints();
				gbc_label.insets = new Insets(5, 5, 0, 5);
				gbc_label.gridx = 0;
				gbc_label.gridy = 0;
				panel.add(label, gbc_label);

				JButton button = new JButton("Pick");
				GridBagConstraints gbc_button = new GridBagConstraints();
				gbc_button.weightx = 1.0;
				gbc_button.anchor = GridBagConstraints.EAST;
				gbc_button.gridx = 1;
				gbc_button.gridy = 0;
				panel.add(button, gbc_button);

				button.addActionListener(e -> {
					final ElementInformation dummy = new ElementInformation((short) 0, "dummy", ElementKeyMap.getCategoryHirarchy().find("Ship"), new short[6]);
					TextureChoserDialog diag = new TextureChoserDialog(frame, dummy, () -> {
						textureId = dummy.getTextureIds();
						label.setText(Arrays.toString(textureId));
						sides = dummy.getIndividualSides();
					});

					diag.setVisible(true);
				});
			}
		}
		{
			JPanel panel_1 = new JPanel();
			panel_1.setBorder(new TitledBorder(null, "Icon ID", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			GridBagConstraints gbc_panel_1 = new GridBagConstraints();
			gbc_panel_1.fill = GridBagConstraints.BOTH;
			gbc_panel_1.anchor = GridBagConstraints.NORTHWEST;
			gbc_panel_1.insets = new Insets(10, 0, 10, 0);
			gbc_panel_1.gridx = 0;
			gbc_panel_1.gridy = 2;
			contentPanel.add(panel_1, gbc_panel_1);
			GridBagLayout gbl_panel_1 = new GridBagLayout();
			gbl_panel_1.columnWidths = new int[]{0, 0, 0};
			gbl_panel_1.rowHeights = new int[]{0, 0};
			gbl_panel_1.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
			gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
			panel_1.setLayout(gbl_panel_1);

			final JLabel label = new JLabel("----");
			GridBagConstraints gbc_label = new GridBagConstraints();
			gbc_label.insets = new Insets(5, 5, 0, 5);
			gbc_label.gridx = 0;
			gbc_label.gridy = 0;
			panel_1.add(label, gbc_label);

			JButton button = new JButton("Pick");
			GridBagConstraints gbc_button = new GridBagConstraints();
			gbc_button.weightx = 1.0;
			gbc_button.anchor = GridBagConstraints.EAST;
			gbc_button.gridx = 1;
			gbc_button.gridy = 0;

			button.addActionListener(e -> {
				String s = (String) JOptionPane.showInputDialog(
						frame,
						"Enter an icon ID",
						"Pick ID",
						JOptionPane.PLAIN_MESSAGE,
						null,
						null,
						null);

				if (s != null && s.length() > 0) {
					try {
						short iId = Short.parseShort(s.trim());
						icon = iId;
						label.setText(String.valueOf(icon));
					} catch (NumberFormatException e1) {
						e1.printStackTrace();
					}
				}
			});
			panel_1.add(button, gbc_button);

			JButton button1 = new JButton("Generate");
			GridBagConstraints gbc_button1 = new GridBagConstraints();
			gbc_button1.weightx = 1.0;
			gbc_button1.anchor = GridBagConstraints.EAST;
			gbc_button1.gridx = 2;
			gbc_button1.gridy = 0;
			button1.addActionListener(arg0 -> {
				short buildIconNum = 0;
				for (Short s : ElementKeyMap.typeList()) {
					buildIconNum = (short) Math.max(buildIconNum, ElementKeyMap.getInfo(s).getBuildIconNum());
				}
				for (Short s : addedBuildIcons) {
					buildIconNum = (short) Math.max(buildIconNum, s);
				}
				buildIconNum++;

				addedBuildIcons.add(buildIconNum);
				icon = buildIconNum;
				label.setText(String.valueOf(icon));
			});
			panel_1.add(button1, gbc_button1);

		}
		{
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
			gbl_panel_1.columnWidths = new int[]{0, 0, 0};
			gbl_panel_1.rowHeights = new int[]{0, 0};
			gbl_panel_1.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
			gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
			panel_1.setLayout(gbl_panel_1);
			{
				final JLabel label = new JLabel("----");
				GridBagConstraints gbc_label = new GridBagConstraints();
				gbc_label.insets = new Insets(5, 5, 0, 5);
				gbc_label.gridx = 0;
				gbc_label.gridy = 0;
				panel_1.add(label, gbc_label);

				JButton button = new JButton("Pick");
				GridBagConstraints gbc_button = new GridBagConstraints();
				gbc_button.weightx = 1.0;
				gbc_button.anchor = GridBagConstraints.EAST;
				gbc_button.gridx = 1;
				gbc_button.gridy = 0;
				panel_1.add(button, gbc_button);

				button.addActionListener(e -> {

					String s = (String) JOptionPane.showInputDialog(
							frame,
							"Pick a Name",
							"Pick Name",
							JOptionPane.PLAIN_MESSAGE,
							null,
							null,
							null);

					if (s != null && s.length() > 0) {
						name = s.trim();
						label.setText(name);
					}

				});

				JPanel panel = new JPanel();
				panel.setBorder(new TitledBorder(null, "Category", TitledBorder.LEADING, TitledBorder.TOP, null, null));
				GridBagConstraints gbc_panel = new GridBagConstraints();
				gbc_panel.fill = GridBagConstraints.BOTH;
				gbc_panel.gridx = 0;
				gbc_panel.gridy = 4;
				contentPanel.add(panel, gbc_panel);
				GridBagLayout gbl_panel = new GridBagLayout();
				gbl_panel.columnWidths = new int[]{0, 0, 0};
				gbl_panel.rowHeights = new int[]{0, 0};
				gbl_panel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
				gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
				panel.setLayout(gbl_panel);

				final JLabel label_1 = new JLabel("----");
				GridBagConstraints gbc_label_1 = new GridBagConstraints();
				gbc_label_1.insets = new Insets(5, 5, 0, 5);
				gbc_label_1.gridx = 0;
				gbc_label_1.gridy = 0;
				panel.add(label_1, gbc_label_1);

				JButton button_1 = new JButton("Pick");
				GridBagConstraints gbc_button_1 = new GridBagConstraints();
				gbc_button_1.weightx = 1.0;
				gbc_button_1.anchor = GridBagConstraints.EAST;
				gbc_button_1.gridx = 1;
				gbc_button_1.gridy = 0;
				panel.add(button_1, gbc_button_1);

				button_1.addActionListener(e -> {

					String[] typeStringArray = ElementKeyMap.getCategoryNames(ElementKeyMap.getCategoryHirarchy());
					Arrays.sort(typeStringArray);
					String s = (String) JOptionPane.showInputDialog(
							frame,
							"Pick a category",
							"Pick Category",
							JOptionPane.PLAIN_MESSAGE,
							null,
							typeStringArray,
							typeStringArray[0]);

					if (s != null && s.length() > 0) {
						category = ElementKeyMap.getCategoryHirarchy().find(s);

						System.err.println("CATEGORY SET TO " + category);
						if (category != null) {
							label_1.setText(s);
						}
					}
				});
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
				okButton.addActionListener(e -> {

					if (type < 0 || name.length() == 0 ||
							textureId[0] < 0 ||
							textureId[1] < 0 ||
							textureId[2] < 0 ||
							textureId[3] < 0 ||
							textureId[4] < 0 ||
							textureId[5] < 0 ||
							icon < 0 || category == null) {

						System.err.println("CAT: " + category);
						//custom title, error icon
						JOptionPane.showMessageDialog(frame,
								"Every field in this dialog\n" +
										"has to be filled.",
								"Error",
								JOptionPane.ERROR_MESSAGE);
					} else {
						ElementInformation info = new ElementInformation(type, name, category, textureId);
						info.setBuildIconNum(icon);
						info.setIndividualSides(sides);
						try {
							ElementKeyMap.addInformationToExisting(info);
							dispose();
							callback.onEnter(info);
						} catch (ParserConfigurationException e1) {
							e1.printStackTrace();
							GuiErrorHandler.processErrorDialogException(e1);
						}
					}
				});
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
				cancelButton.addActionListener(e -> dispose());
			}
		}
	}

}
