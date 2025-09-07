package org.schema.game.common.facedit;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.common.controller.damage.effects.InterEffectHandler;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.data.blockeffects.config.ConfigPool;
import org.schema.game.common.data.element.*;
import org.schema.game.common.data.element.annotation.ElemType;
import org.schema.game.common.data.element.annotation.Element;
import org.schema.game.common.data.element.annotation.NodeDependency;
import org.schema.game.common.util.GuiErrorHandler;
import org.schema.schine.common.language.Lng;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ElementInformationOption extends JPanel implements Comparable<ElementInformationOption> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public Component editComponent;
	public ElementInformationEditPanel editPanel;
	public boolean isSourceBlock = true; //true if not referenced by any wildcard
	/**
	 *
	 */

	ElementInformation info;
	private ApplyInterface applyInterface;
	private final Field field;
	private final int order;
	private int localOrder;
	private final JPanel mainPanel;

	/**
	 * Create the panel.
	 */
	public ElementInformationOption(JFrame frame, Field field, short type, int index, ElementInformationEditPanel ePanel) {
		setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		editPanel = ePanel;
		info = ElementKeyMap.getInfo(type);
		this.field = field;

		Element annotation = field.getAnnotation(org.schema.game.common.data.element.annotation.Element.class);
		order = annotation.order();

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		JLabel lblName = new JLabel(field.getAnnotation(Element.class).parser().tag);
		lblName.setFont(new Font("Tahoma", Font.PLAIN, 15));
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.insets = new Insets(0, 0, 0, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		add(lblName, gbc_lblName);

		mainPanel = new JPanel();
		GridBagLayout gg = new GridBagLayout();
		gg.columnWidths = new int[]{300};
		gg.rowHeights = new int[]{0};
		gg.columnWeights = new double[]{1.0};
		gg.rowWeights = new double[]{1.0};
		mainPanel.setLayout(gg);

		GridBagConstraints gbc_panel = new GridBagConstraints();
		//		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel.gridx = 1;
		gbc_panel.gridy = 0;
		gbc_panel.weightx = 2;
		gbc_panel.anchor = GridBagConstraints.EAST;
		add(mainPanel, gbc_panel);

		try {
			GridBagConstraints ggb = new GridBagConstraints();
			ggb.gridx = 0;
			ggb.gridy = 0;
			ggb.weightx = 1.0f;
			ggb.fill = GridBagConstraints.BOTH;
			editComponent = addContent(frame, field, ggb, ePanel);
			if(applyInterface != null) {

				Component c = editComponent;
				if(c instanceof JScrollPane) {
					c = ((JScrollPane) c).getViewport().getComponent(0);
				}
				addApplyListener(c, false);
			}
			mainPanel.add(editComponent, ggb);
		} catch(IllegalArgumentException e) {
			e.printStackTrace();
			GuiErrorHandler.processErrorDialogException(e);
		} catch(IllegalAccessException e) {
			e.printStackTrace();
			GuiErrorHandler.processErrorDialogException(e);
		}

	}

	private void addApplyListener(Component c, boolean onAnyKeyPressed) {
		c.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent arg0) {

			}

			@Override
			public void keyPressed(KeyEvent arg0) {
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				if(arg0.getKeyCode() == KeyEvent.VK_ENTER || onAnyKeyPressed) {
					apply();
				}
			}
		});
		c.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent arg0) {
				apply();

			}

			@Override
			public void focusLost(FocusEvent arg0) {
				apply();
			}
		});
	}

	@SuppressWarnings("unchecked")
	private Component addContent(JFrame frame, Field field, GridBagConstraints ggb, ElementInformationEditPanel panel) throws IllegalArgumentException, IllegalAccessException {
		Class<?> clazz = field.getType();

		Element ann = field.getAnnotation(Element.class);
		if(field.getAnnotation(Element.class) == null) {
			return null;//new JLabel(field.getName()+" (field not accessible)");
		}

		ggb.insets = new Insets(0, 50, 0, 0);

		if(ann.parser() == ElemType.NAME) {
			try {
				JPanel p = new JPanel();
				JTextPane text = new JTextPane();
				text.setEditable(false);
				String o = (String) field.get(info);
				text.setText(info.getName());

				JButton box = new JButton("Edit");

				box.addActionListener(e -> {
					Object[] possibilities = null;
					String s = (String) JOptionPane.showInputDialog(frame, "Choose a name:", "Name Block", JOptionPane.PLAIN_MESSAGE, null, possibilities, info.getName());

					//If a string was returned, say so.
					if((s != null) && (s.length() > 0)) {
						info.name = s;
						text.setText(info.getName());
						frame.repaint();
					}

				});
				p.add(text);
				p.add(box);
				return p;
			} catch(Exception e) {
				e.printStackTrace();
				assert (false);
			}
		} else if(ann != null && ann.configGroupSet()) {
			JPanel p = new JPanel();

			JScrollPane s = new JScrollPane();
			List<String> o = (List<String>) field.get(info);
			StringListModel m = new StringListModel(o);
			JList items = new JList(m);
			s.setViewportView(items);

			JButton add = new JButton("Add");

			add.addActionListener(e -> {
				ConfigPool p1 = new ConfigPool();
				try {
					p1.readConfigFromFile(p1.getPath(true));

					String[] choices = new String[p1.pool.size()];

					for(int i = 0; i < choices.length; i++) {
						choices[i] = p1.pool.get(i).id;
					}
					Arrays.sort(choices);
					String input = (String) JOptionPane.showInputDialog(null, "Config Group", "Select a group to add", JOptionPane.QUESTION_MESSAGE, null, choices, // Array of choices
							choices[0]); // Initial choice

					if(input != null) {
						o.add(input.toLowerCase(Locale.ENGLISH));
						m.setChanged();
					}

				} catch(Exception ex) {
					ex.printStackTrace();
					GuiErrorHandler.processErrorDialogException(ex);
				}

			});
			JButton remove = new JButton("Remove");

			remove.addActionListener(e -> {
				try {
					Object selectedValue = items.getSelectedValue();
					if(selectedValue != null) {
						o.remove(selectedValue);
					}
					m.setChanged();
				} catch(Exception e1) {
					e1.printStackTrace();
					GuiErrorHandler.processErrorDialogException(e1);
				}
			});
			p.add(s);
			p.add(add);
			p.add(remove);
			return p;

		} else if("buildIconNum".equals(field.getName())) {
			JPanel p = new JPanel();
			JTextPane text = new JTextPane();
			text.setEditable(false);
			int o = field.getInt(info);
			text.setText(String.valueOf(info.getBuildIconNum()));

			JButton box = new JButton("SetToFree");
			JButton manualSet = new JButton("Set Manually");

			manualSet.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					//Create new text field
					JTextField field = new JTextField();
					field.setText(String.valueOf(info.getBuildIconNum()));
					//Show dialog
					int result = JOptionPane.showConfirmDialog(null, field, "Enter a new Icon ID", JOptionPane.OK_CANCEL_OPTION);
					info.buildIconNum = Short.parseShort(field.getText());
					text.setText(String.valueOf(info.getBuildIconNum()));
					AddElementEntryDialog.addedBuildIcons.add((short) info.getBuildIconNum());
				}
			});

			box.addActionListener(e -> {
				AddElementEntryDialog.addedBuildIcons.remove((short) info.getBuildIconNum());
				info.setBuildIconToFree();
				text.setText(String.valueOf(info.getBuildIconNum()));
				AddElementEntryDialog.addedBuildIcons.add((short) info.getBuildIconNum());

			});
			p.add(text);
			p.add(box);
			p.add(manualSet);
			return p;
		} else if(field.getType().equals(Vector4f.class)) {
			JColorChooser colorChooser = new JColorChooser();
			Vector4f v = (Vector4f) field.get(info);
			colorChooser.setColor(new Color(v.x, v.y, v.z, v.w));
			colorChooser.getSelectionModel().addChangeListener(e -> {
				Color color = colorChooser.getColor();
				v.x = color.getRed() / 255.0f;
				v.y = color.getGreen() / 255.0f;
				v.z = color.getBlue() / 255.0f;
				v.w = color.getAlpha() / 255.0f;
			});
			applyInterface = new ApplyInterface() {

				@Override
				public void afterApply(Field field, ElementInformation info) throws IllegalArgumentException, IllegalAccessException {
					colorChooser.setColor(new Color(v.x, v.y, v.z, v.w));
				}

				@Override
				public void apply(Field field, ElementInformation info) throws IllegalArgumentException, IllegalAccessException {
					field.set(info, v);
				}
			};
			return colorChooser;
		} else if(field.getType().equals(ElementInformation.LodCollision.class)) {
			ElementInformation.LodCollision set = (ElementInformation.LodCollision) field.get(info);
			JPanel p = new JPanel();

			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{0, 0};
			gridBagLayout.rowHeights = new int[4];

			gridBagLayout.columnWeights = new double[]{0.0, 1.0};
			gridBagLayout.rowWeights = new double[4];

			p.setLayout(gridBagLayout);
			JTextField boxModel = new JTextField();
			JComboBox boxStyle = new JComboBox();
			JComboBox boxSlab = new JComboBox();
			{
				JComboBox box = new JComboBox();
				for(int i = 0; i < ElementInformation.LodCollision.LodCollisionType.values().length; i++) {
					ElementInformation.LodCollision.LodCollisionType t = ElementInformation.LodCollision.LodCollisionType.values()[i];
					box.addItem(t);
					if(set.type == t) {
						box.setSelectedIndex(i);
					}
				}
				box.setEditable(ann.editable());
				box.addActionListener(e -> {
					try {
						ElementInformation.LodCollision.LodCollisionType paa = (ElementInformation.LodCollision.LodCollisionType) box.getSelectedItem();
						set.type = paa;
						switch(set.type) {
							case BLOCK_TYPE -> {
								boxModel.setEditable(false);
								boxStyle.setEnabled(true);
								boxSlab.setEnabled(true);
								set.modelId = null;
								boxModel.setText(set.modelId != null ? set.modelId : "");
							}
							case CONVEX_HULL -> {
								boxModel.setEditable(true);
								boxStyle.setEnabled(false);
								boxSlab.setEnabled(false);
							}
							default -> throw new RuntimeException("UNKNOWN TYPE: " + set.type.name());
						}
					} catch(IllegalArgumentException e1) {
						e1.printStackTrace();
						GuiErrorHandler.processErrorDialogException(e1);
					}
				});

				GridBagConstraints cBox = new GridBagConstraints();
				cBox.weightx = 1;
				cBox.gridx = 0;
				cBox.gridy = 0;
				p.add(box, cBox);
			}
			{

				for(int i = 0; i < BlockStyle.values().length; i++) {
					BlockStyle t = BlockStyle.values()[i];
					boxStyle.addItem(t);
					if(set.blockTypeToEmulate == t) {
						boxStyle.setSelectedIndex(i);
					}
				}
				boxStyle.setEditable(ann.editable());
				boxStyle.addActionListener(e -> {
					try {
						BlockStyle paa = (BlockStyle) boxStyle.getSelectedItem();
						set.blockTypeToEmulate = paa;

					} catch(IllegalArgumentException e1) {
						e1.printStackTrace();
						GuiErrorHandler.processErrorDialogException(e1);
					}
				});

				GridBagConstraints cBox = new GridBagConstraints();
				cBox.weightx = 1;
				cBox.gridx = 0;
				cBox.gridy = 1;
				p.add(boxStyle, cBox);
			}
			{

				for(int i = 0; i < ElementInformation.slabStrings.length; i++) {
					String t = ElementInformation.slabStrings[i];
					boxSlab.addItem(t);
					if(set.colslab == i) {
						boxSlab.setSelectedIndex(i);
					}
				}
				boxSlab.setEditable(ann.editable());
				boxSlab.addActionListener(e -> {
					try {
						String paa = (String) boxSlab.getSelectedItem();
						for(int i = 0; i < ElementInformation.slabStrings.length; i++) {
							String t = ElementInformation.slabStrings[i];
							if(t.equals(paa)) {
								set.colslab = i;
								break;
							}
						}

					} catch(IllegalArgumentException e1) {
						e1.printStackTrace();
						GuiErrorHandler.processErrorDialogException(e1);
					}
				});

				GridBagConstraints cBox = new GridBagConstraints();
				cBox.weightx = 1;
				cBox.gridx = 0;
				cBox.gridy = 2;
				p.add(boxSlab, cBox);
			}

			{

				boxModel.setText(set.modelId != null ? set.modelId : "");
				boxModel.setEditable(set.type == ElementInformation.LodCollision.LodCollisionType.CONVEX_HULL);
				applyInterface = new ApplyInterface() {

					@Override
					public void afterApply(Field field, ElementInformation info) throws IllegalArgumentException, IllegalAccessException {
					}

					@Override
					public void apply(Field field, ElementInformation info) throws IllegalArgumentException, IllegalAccessException {
						set.modelId = boxModel.getText().trim();
						if(set.modelId.length() == 0) {
							set.modelId = null;
						}
						System.err.println("APPLY TEXT");
					}
				};
				GridBagConstraints cBox = new GridBagConstraints();
				cBox.fill = GridBagConstraints.HORIZONTAL;
				cBox.weightx = 1;
				cBox.gridx = 0;
				cBox.gridy = 3;
				p.add(boxModel, cBox);
				addApplyListener(boxModel, true);
			}
			switch(set.type) {
				case BLOCK_TYPE -> {
					boxModel.setEditable(false);
					boxStyle.setEnabled(true);
					boxSlab.setEnabled(true);
					set.modelId = null;
					boxModel.setText(set.modelId != null ? set.modelId : "");
				}
				case CONVEX_HULL -> {
					boxModel.setEditable(true);
					boxStyle.setEnabled(false);
					boxSlab.setEnabled(false);
				}
				default -> throw new RuntimeException("UNKNOWN TYPE: " + set.type.name());
			}

			return p;
		} else if(field.getType().equals(InterEffectSet.class)) {

			InterEffectSet set = (InterEffectSet) field.get(info);
			JPanel p = new JPanel();

			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{0, 0};
			gridBagLayout.rowHeights = new int[InterEffectHandler.InterEffectType.values().length];

			gridBagLayout.columnWeights = new double[]{0.0, 1.0};
			gridBagLayout.rowWeights = new double[InterEffectHandler.InterEffectType.values().length];

			p.setLayout(gridBagLayout);

			List<ApplyInterface> ap = new ObjectArrayList<ApplyInterface>();
			for(int i = 0; i < InterEffectHandler.InterEffectType.values().length; i++) {

				InterEffectHandler.InterEffectType t = InterEffectHandler.InterEffectType.values()[i];

				GridBagConstraints nmk = new GridBagConstraints();
				nmk.insets = new Insets(0, 0, 0, 5);
				nmk.gridx = 0;
				nmk.gridy = i;
				JLabel n = new JLabel(t.id);
				p.add(n, nmk);

				GridBagConstraints gbc_lblSetting = new GridBagConstraints();
				gbc_lblSetting.insets = new Insets(0, 0, 0, 5);
				gbc_lblSetting.gridx = 1;
				gbc_lblSetting.gridy = i;
				gbc_lblSetting.fill = GridBagConstraints.HORIZONTAL;


				JTextField box = new JTextField();
				box.setText(String.valueOf(set.getStrength(t)));
				ApplyInterface applyInterface = new ApplyInterface() {

					@Override
					public void afterApply(Field field, ElementInformation info) throws IllegalArgumentException, IllegalAccessException {
						box.setText(String.valueOf(set.getStrength(t)));
					}

					@Override
					public void apply(Field field, ElementInformation info) throws IllegalArgumentException, IllegalAccessException {
						float parseFloat = Float.parseFloat(box.getText());
						set.setStrength(t, parseFloat);

						System.err.println("APPLIED SET: " + set);
					}
				};
				ap.add(applyInterface);
				p.add(box, gbc_lblSetting);

				addApplyListener(box, false);

			}

			applyInterface = new ApplyInterface() {

				@Override
				public void afterApply(Field field, ElementInformation info) throws IllegalArgumentException, IllegalAccessException {
					for(ApplyInterface a : ap) {
						a.afterApply(field, info);
					}
				}

				@Override
				public void apply(Field field, ElementInformation info) throws IllegalArgumentException, IllegalAccessException {
					for(ApplyInterface a : ap) {
						a.apply(field, info);
					}
				}
			};

			return p;
		} else if("textureId".equals(field.getName())) {
			JPanel p = new JPanel();
			JTextPane text = new JTextPane();

			short[] o = (short[]) field.get(info);
			text.setText(Arrays.toString(o));

			JButton box = new JButton("Edit");

			box.addActionListener(e -> {
				try {
					TextureChoserDialog diag = new TextureChoserDialog(frame, info, () -> {
						short[] o111;
						try {
							o111 = (short[]) field.get(info);
							text.setText(Arrays.toString(o111));
						} catch(IllegalArgumentException e15) {
							e15.printStackTrace();
						} catch(IllegalAccessException e15) {
							e15.printStackTrace();
						}
						panel.updateTextures();
					});
					diag.setVisible(true);
				} catch(IllegalArgumentException e1) {
					e1.printStackTrace();
					GuiErrorHandler.processErrorDialogException(e1);
				}
			});
			p.add(text);
			p.add(box);
			return p;
		} else if(clazz.equals(Class.class)) {
			JTextField box = new JTextField();
			box.setText(((ElementCategory) field.get(info)).getCategory());
			box.setEditable(false);
			return box;
		} else if(ann.elementSet()) {

			JButton box = new JButton("Edit");
			box.addActionListener(e -> {
				try {
					if(field.get(info).getClass().equals(ShortArrayList.class)) {
						ElementEditSetDialog diag = new ElementEditSetDialog(frame, (ShortArrayList) field.get(info));
						diag.setVisible(true);
					} else if(field.get(info).getClass().equals(ShortOpenHashSet.class)) {
						ElementEditSetDialog diag = new ElementEditSetDialog(frame, (ShortOpenHashSet) field.get(info));
						diag.setVisible(true);
					} else throw new IllegalArgumentException("Unknown type: " + field.get(info).getClass());
				} catch(IllegalArgumentException | IllegalAccessException e1) {
					e1.printStackTrace();
					GuiErrorHandler.processErrorDialogException(e1);
				}
			});
			return box;
		} else if(ann.consistence()) {
			FactoryResourceEditPanel factoryResourceEditPanel = new FactoryResourceEditPanel(frame, "Consistence Resource List", info.getConsistence());
			return factoryResourceEditPanel;
		} else if(ann.selectBlock()) {
			JPanel p = new JPanel();
			JTextPane text = new JTextPane();
			ElementInformation o;

			if(this.field.equals(Short.TYPE)) {
				o = ElementKeyMap.getInfoFast(field.getShort(info));
			} else {
				o = ElementKeyMap.getInfoFast(field.getInt(info));
			}
			if(o != null) {
				text.setText(o.getName() + "(" + o.id + ")");
			} else {
				text.setText("   -   ");
			}
			JButton create = new JButton("Set");

			create.addActionListener(e -> {
				try {
					ElementChoserDialog diag = new ElementChoserDialog(frame, oth -> {
						try {
							if(this.field.equals(Short.TYPE)) {
								this.field.setShort(info, oth.id);
							} else {
								this.field.setInt(info, oth.id);
							}
						} catch(IllegalArgumentException e14) {
							e14.printStackTrace();
						} catch(IllegalAccessException e14) {
							e14.printStackTrace();
						}
					});
					diag.setVisible(true);
				} catch(Exception e1) {
					e1.printStackTrace();
					GuiErrorHandler.processErrorDialogException(e1);
				}
				ElementInformation o110;
				try {
					if(this.field.equals(Short.TYPE)) {

						o110 = ElementKeyMap.getInfoFast(field.getShort(info));

					} else {
						o110 = ElementKeyMap.getInfoFast(field.getInt(info));
					}
					if(o110 != null) {
						text.setText(o110.getName() + "(" + o110.id + ")");
					} else {
						text.setText("   -   ");
					}
				} catch(IllegalArgumentException e1) {
					e1.printStackTrace();
				} catch(IllegalAccessException e1) {
					e1.printStackTrace();
				}
			});
			JButton delete = new JButton("Clear");

			delete.addActionListener(e -> {
				try {
					if(this.field.equals(Short.TYPE)) {
						this.field.setShort(info, (short) 0);
					} else {
						this.field.setInt(info, 0);
					}
					text.setText("   -   ");
				} catch(IllegalArgumentException eyy) {
					eyy.printStackTrace();
				} catch(IllegalAccessException ye) {
					ye.printStackTrace();
				}

			});
			p.add(text);
			if(ann.editable()) {
				p.add(create);
				p.add(delete);
			}
			return p;
		} else if(ann != null && ann.cubatomConsistence()) {
			FactoryResourceEditPanel factoryResourceEditPanel = new FactoryResourceEditPanel(frame, "Cubatom Consistence Resource List", info.cubatomConsistence);
			return factoryResourceEditPanel;
		} else if(ann != null && ann.stringSet()) {
			JPanel p = new JPanel();
			JTextPane text = new JTextPane();

			List<String> o = (List<String>) field.get(info);
			text.setText(o.toString());
			JButton create = new JButton("Add");

			create.addActionListener(e -> {
				try {
					String name = JOptionPane.showInputDialog(frame, "Add Value");
					if(name != null) {
						o.add(name.toLowerCase(Locale.ENGLISH));
						field.set(info, o);
						text.setText(o.toString());
					}
				} catch(Exception e1) {
					e1.printStackTrace();
					GuiErrorHandler.processErrorDialogException(e1);
				}
			});
			JButton delete = new JButton("Clear");

			delete.addActionListener(e -> {
				try {
					if(o != null) {
						List<String> o19 = (List<String>) field.get(info);
						o19.clear();
						text.setText(o19.toString());
						//							((ElementEditorFrame)frame).reinitializeElements();
					}
				} catch(Exception e1) {
					e1.printStackTrace();
					GuiErrorHandler.processErrorDialogException(e1);
				}
			});
			p.add(text);
			p.add(create);
			p.add(delete);
			return p;
		} else if(clazz.equals(ElementReactorChange.class)) {

			JButton box = new JButton("Edit");
			box.addActionListener(e -> {
				try {
					field.set(info, new ElementReactorChange(info));
					ElementReactorChange c = (ElementReactorChange) field.get(info);
					c.openDialog(frame);
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			});
			return box;
		} else if(clazz.equals(List.class)) {

			JButton box = new JButton("Edit");
			box.addActionListener(e -> {

				try {
					ElementEditSetDialog diag = new ElementEditSetDialog(frame, (List<Short>) field.get(info));
					diag.setVisible(true);
				} catch(IllegalArgumentException e1) {
					e1.printStackTrace();
					GuiErrorHandler.processErrorDialogException(e1);
				} catch(IllegalAccessException e1) {
					e1.printStackTrace();
					GuiErrorHandler.processErrorDialogException(e1);
				}
			});
			return box;
		} else if(clazz.equals(BlockFactory.class)) {

			JPanel p = new JPanel();
			JTextPane text = new JTextPane();

			Object o = field.get(info);
			if(o != null) {
				BlockFactory bl = (BlockFactory) o;
				text.setText(bl.toString());
			} else {
				text.setText("   -   ");
			}
			JButton box = new JButton("Edit");

			box.addActionListener(e -> {
				try {
					ElementEditFactoryProductsDialog diag = new ElementEditFactoryProductsDialog(frame, info, () -> {
						Object o18;
						try {
							o18 = field.get(info);
							if(o18 != null) {
								BlockFactory bl = (BlockFactory) o18;
								text.setText(bl.toString());
							} else {
								text.setText("   -   ");
							}
						} catch(IllegalArgumentException e13) {
							// TODO Auto-generated catch block
							e13.printStackTrace();
							GuiErrorHandler.processErrorDialogException(e13);
						} catch(IllegalAccessException e13) {
							// TODO Auto-generated catch block
							e13.printStackTrace();
							GuiErrorHandler.processErrorDialogException(e13);
						}
					});
					diag.setVisible(true);
				} catch(IllegalArgumentException e1) {
					e1.printStackTrace();
					GuiErrorHandler.processErrorDialogException(e1);
				}
			});
			p.add(text);
			p.add(box);
			return p;
		} else if("slabids".equals(field.getName().toLowerCase(Locale.ENGLISH))) {

			JPanel p = new JPanel();
			JTextPane text = new JTextPane();

			short[] o = (short[]) field.get(info);
			if(o != null) {
				text.setText(Arrays.toString(o));
			} else {
				text.setText("   -   ");
			}
			JButton create = new JButton("Create");

			create.addActionListener(e -> {
				try {
					ElementKeyMap.deleteBlockSlabs(info);
					ElementKeyMap.createBlockSlabs(info);

					short[] o17 = (short[]) field.get(info);
					if(o17 != null) {
						text.setText(Arrays.toString(o17));
					} else {
						text.setText("   -   ");
					}
					((ElementEditorFrame) frame).reinitializeElements();
				} catch(Exception e1) {
					e1.printStackTrace();
					GuiErrorHandler.processErrorDialogException(e1);
				}
			});
			JButton delete = new JButton("Delete");

			delete.addActionListener(e -> {
				try {
					if(info.slabIds != null) {
						ElementKeyMap.deleteBlockSlabs(info);

						short[] o16 = (short[]) field.get(info);
						if(o16 != null) {
							text.setText(Arrays.toString(o16));
						} else {
							text.setText("   -   ");
						}
						((ElementEditorFrame) frame).reinitializeElements();
					}
				} catch(Exception e1) {
					e1.printStackTrace();
					GuiErrorHandler.processErrorDialogException(e1);
				}
			});
			p.add(text);
			if(ann.editable()) {
				p.add(create);
				p.add(delete);
			}
			return p;
		} else if("styleids".equals(field.getName().toLowerCase(Locale.ENGLISH))) {

			JPanel p = new JPanel();
			JTextPane text = new JTextPane();

			short[] o = (short[]) field.get(info);
			if(o != null) {
				text.setText(Arrays.toString(o));
			} else {
				text.setText("   -   ");
			}
			JButton create = new JButton("Generate by Name");

			JButton createButton = new JButton("Auto Generate Styles");
			createButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ElementKeyMap.generateBlockStyles(info);
					((ElementEditorFrame) frame).reinitializeElements();
				}
			});

			create.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						ElementKeyMap.createBlockStyleReferencesFromName(info);

						short[] o15 = (short[]) field.get(info);
						if(o15 != null) {
							text.setText(Arrays.toString(o15));
						} else {
							text.setText("   -   ");
						}
						((ElementEditorFrame) frame).reinitializeElements();
					} catch(Exception e1) {
						e1.printStackTrace();
						GuiErrorHandler.processErrorDialogException(e1);
					}
				}
			});
			JButton delete = new JButton("Delete");

			delete.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						if(info.styleIds != null) {
							ElementKeyMap.deleteBlockStyleReferences(info);

							short[] o14 = (short[]) field.get(info);
							if(o14 != null) {
								text.setText(Arrays.toString(o14));
							} else {
								text.setText("   -   ");
							}
							((ElementEditorFrame) frame).reinitializeElements();
						}
					} catch(Exception e1) {
						e1.printStackTrace();
						GuiErrorHandler.processErrorDialogException(e1);
					}
				}
			});
			p.add(text);
			if(ann.editable()) {
//				p.add(create);
				p.add(createButton);
				p.add(delete);
			}
			return p;
		} else if("wildcardids".equals(field.getName().toLowerCase(Locale.ENGLISH))) {

			JPanel p = new JPanel();
			JTextPane text = new JTextPane();

			short[] o = (short[]) field.get(info);
			if(o != null) {
				text.setText(Arrays.toString(o));
			} else {
				text.setText("   -   ");
			}
			JButton create = new JButton("Add");

			create.addActionListener(e -> {
				try {
					ElementChoserDialog diag = new ElementChoserDialog(frame, oth -> {
						ShortArrayList l = new ShortArrayList();
						if(info.wildcardIds != null) {
							for(short s : info.wildcardIds) {
								l.add(s);
							}
						}
						oth.setSourceReference(info.id);
						l.add(oth.id);
						info.wildcardIds = new short[l.size()];
						for(int i = 0; i < l.size(); i++) {
							info.wildcardIds[i] = l.getShort(i);
						}
						short[] o13;
						try {
							o13 = (short[]) field.get(info);
							if(o13 != null) {
								text.setText(Arrays.toString(o13));
							} else {
								text.setText("   -   ");
							}
						} catch(IllegalArgumentException e12) {
							e12.printStackTrace();
						} catch(IllegalAccessException e12) {
							e12.printStackTrace();
						}
						((ElementEditorFrame) frame).reinitializeElements();
					});
					diag.setVisible(true);
				} catch(Exception e1) {
					e1.printStackTrace();
					GuiErrorHandler.processErrorDialogException(e1);
				}
			});
			JButton createAd = new JButton("Create from inventory group");
			createAd.addActionListener(e -> {
				try {
					ElementKeyMap.createBlockStyleReferencesFromInvGroup(info);

					short[] o12 = (short[]) field.get(info);
					if(o12 != null) {
						text.setText(Arrays.toString(o12));
					} else {
						text.setText("   -   ");
					}
					((ElementEditorFrame) frame).reinitializeElements();
				} catch(Exception e1) {
					e1.printStackTrace();
					GuiErrorHandler.processErrorDialogException(e1);
				}
			});
			JButton delete = new JButton("Clear");

			delete.addActionListener(e -> {
				try {
					if(info.wildcardIds != null) {
						ElementKeyMap.deleteWildCardReferences(info);

						short[] o1 = (short[]) field.get(info);
						if(o1 != null) {
							text.setText(Arrays.toString(o1));
						} else {
							text.setText("   -   ");
						}
						((ElementEditorFrame) frame).reinitializeElements();
					}
				} catch(Exception e1) {
					e1.printStackTrace();
					GuiErrorHandler.processErrorDialogException(e1);
				}
			});
			p.add(text);
			if(ann.editable()) {
				p.add(create);
				p.add(createAd);
				p.add(delete);
			}
			return p;
		} else if(ann != null && ann.shortSet()) {

			JPanel p = new JPanel();
			JTextPane text = new JTextPane();

			ShortSet o = (ShortSet) field.get(info);
			text.setText(o.toString());
			JButton create = new JButton("Add");
			create.addActionListener(e -> {
				try {
					ElementChoserDialog diag = new ElementChoserDialog(frame, oth -> {
						o.add(oth.id);
						text.setText(o.toString());
					});
					diag.setVisible(true);
				} catch(Exception e1) {
					e1.printStackTrace();
					GuiErrorHandler.processErrorDialogException(e1);
				}
			});
			JButton remove = new JButton("Remove");

			remove.addActionListener(e -> {
				try {
					ElementChoserDialog diag = new ElementChoserDialog(frame, oth -> {
						o.remove(oth.id);
						text.setText(o.toString());
					});
					diag.setVisible(true);
				} catch(Exception e1) {
					e1.printStackTrace();
					GuiErrorHandler.processErrorDialogException(e1);
				}
			});
			JButton clear = new JButton("Clear");

			clear.addActionListener(e -> {
				try {
					o.clear();
					text.setText(o.toString());
				} catch(Exception e1) {
					e1.printStackTrace();
					GuiErrorHandler.processErrorDialogException(e1);
				}
			});
			p.add(text);
			if(ann.editable()) {
				p.add(create);
				p.add(remove);
				p.add(clear);
			}
			return p;
		} else if(clazz.equals(String.class)) {
			if(ann != null) {
				if(ann.textArea()) {
					JTextArea box = new JTextArea();
					box.setEditable(ann.editable());
					box.setText(field.get(info).toString().replace("\t", ""));
					ggb.insets = new Insets(0, 50, 0, 0);
					applyInterface = new ApplyInterface() {
						@Override
						public void afterApply(Field field, ElementInformation info) throws IllegalArgumentException, IllegalAccessException {
							//nothing to do
						}

						@Override
						public void apply(Field field, ElementInformation info) throws IllegalArgumentException, IllegalAccessException {
							field.set(info, box.getText());
						}
					};
					JScrollPane jScrollPane = new JScrollPane(box);
					jScrollPane.setPreferredSize(new Dimension(640, 200));
					return jScrollPane;
				} else {

					JTextField box = new JTextField();
					box.setEditable(ann.editable());
					box.setText(field.get(info).toString());
					applyInterface = new ApplyInterface() {
						@Override
						public void afterApply(Field field, ElementInformation info) throws IllegalArgumentException, IllegalAccessException {
							box.setText(field.get(info).toString());
						}

						@Override
						public void apply(Field field, ElementInformation info) throws IllegalArgumentException, IllegalAccessException {
							field.set(info, box.getText());
						}
					};

					if(ann.modelSelect()) {
						JButton selectModel = new JButton("select");
						selectModel.addActionListener(e -> {
							String openSelectModelDialog = ElementEditorFrame.openSelectModelDialog(frame, ann.modelSelectFilter());
							if(openSelectModelDialog != null) {
								box.setText(openSelectModelDialog.trim());
								try {
									applyInterface.apply(field, info);
								} catch(IllegalArgumentException e1) {
									e1.printStackTrace();
								} catch(IllegalAccessException e1) {
									e1.printStackTrace();
								}
							}
						});
						JPanel bPanel = new JPanel();
						GridBagConstraints cText = new GridBagConstraints();
						cText.gridx = 0;
						cText.gridy = 0;
						cText.fill = GridBagConstraints.HORIZONTAL;
						GridBagConstraints cButton = new GridBagConstraints();
						cButton.gridx = 1;
						cButton.gridy = 0;
						GridBagLayout l = new GridBagLayout();
						l.columnWidths = new int[]{0, 0};
						l.rowHeights = new int[]{0};
						l.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
						l.rowWeights = new double[]{1.0, Double.MIN_VALUE};
						bPanel.setLayout(l);

						bPanel.add(box, cText);
						bPanel.add(selectModel, cButton);
						return bPanel;
					} else {
						return box;
					}

				}
			}

		} else if(clazz.equals(Vector3f.class)) {
			JPanel p = new JPanel();

			Vector3f v = (Vector3f) field.get(info);

			JColorChooser tcc = new JColorChooser(new Color(v.x, v.y, v.z, 1));
			p.add(tcc);

			float[] components = new float[4];
			tcc.getSelectionModel().addChangeListener(arg0 -> {
				Color c = tcc.getColor();
				c.getComponents(components);
				Vector3f n = new Vector3f();
				n.x = components[0];
				n.y = components[1];
				n.z = components[2];
				//					System.err.println("Color Changed: "+n);
				v.set(n);
			});
			return p;
		} else if(clazz.equals(Vector4f.class)) {
			JPanel p = new JPanel();

			Vector4f v = (Vector4f) field.get(info);

			JColorChooser tcc = new JColorChooser(new Color(v.x, v.y, v.z, v.w));
			p.add(tcc);

			float[] components = new float[4];
			tcc.getSelectionModel().addChangeListener(arg0 -> {
				Color c = tcc.getColor();
				c.getComponents(components);
				Vector4f n = new Vector4f();
				n.x = components[0];
				n.y = components[1];
				n.z = components[2];
				n.w = components[3];
				//					System.err.println("Color Changed: "+n);
				v.set(n);
			});
			return p;
		} else if(clazz.equals(boolean.class)) {
			//#RM1958 remove JComboBox generic argument
			JComboBox box = new JComboBox();
			box.addItem(false);
			box.addItem(true);
			box.setEditable(ann.editable());
			box.setSelectedItem(field.getBoolean(info));
			box.addActionListener(e -> {
				try {

					Element annotation = field.getAnnotation(Element.class);

					if("orientation".equals(annotation.parser().tag.toLowerCase(Locale.ENGLISH)) && info.getBlockStyle() != BlockStyle.NORMAL && (Boolean) box.getSelectedItem()) {
						field.setBoolean(info, false);
						int n = JOptionPane.showOptionDialog(frame, Lng.str("Option only available for normal BlockStyle"), "Option not available", JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{"Ok"}, "Ok");
						box.setSelectedItem(false);
					} else {
						field.setBoolean(info, (Boolean) box.getSelectedItem());
					}
				} catch(IllegalArgumentException e1) {
					e1.printStackTrace();
					GuiErrorHandler.processErrorDialogException(e1);
				} catch(IllegalAccessException e1) {
					e1.printStackTrace();
					GuiErrorHandler.processErrorDialogException(e1);
				}
			});
			return box;
		} else if(clazz == BlockStyle.class) {
			JComboBox box = new JComboBox();
			for(int i = 0; i < BlockStyle.values().length; i++) {
				box.addItem(new ValueClass(BlockStyle.values()[i], BlockStyle.values()[i].realName));
				if(BlockStyle.values()[i] == field.get(info)) {
					box.setSelectedIndex(i);
				}
			}
			box.setEditable(ann.editable());
			box.addActionListener(e -> {
				try {
					BlockStyle val = ((ValueClass<BlockStyle>) box.getSelectedItem()).value;

					field.set(info, val);

				} catch(IllegalArgumentException e1) {
					e1.printStackTrace();
					GuiErrorHandler.processErrorDialogException(e1);
				} catch(IllegalAccessException e1) {
					e1.printStackTrace();
					GuiErrorHandler.processErrorDialogException(e1);
				}
			});
			box.setToolTipText(BlockStyle.getDescs());
			return box;
		} else if(clazz == int.class) {

			Element annotation = field.getAnnotation(org.schema.game.common.data.element.annotation.Element.class);

			if(annotation != null) {

				if(annotation.states().length > 0) {
					//#RM1958 remove JComboBox generic argument
					JComboBox box = new JComboBox();
					for(int i = 0; i < annotation.states().length; i++) {
						box.addItem(new ValueClass(Integer.parseInt(annotation.states()[i]), (annotation.stateDescs().length == annotation.states().length) ? annotation.stateDescs()[i] : annotation.states()[i]));
						if(Integer.parseInt(annotation.states()[i]) == field.getInt(info)) {
							box.setSelectedIndex(i);
						}
					}
					box.setEditable(ann.editable());
					box.addActionListener(e -> {
						try {
							int parseInt = ((ValueClass<Integer>) box.getSelectedItem()).value;

							field.setInt(info, parseInt);

							if(annotation.updateTextures()) {
								panel.updateTextures();
							}
							if(annotation.parser().fac instanceof NodeDependency) {
								((NodeDependency) annotation.parser().fac).onSwitch(this, info, annotation);
							}
						} catch(IllegalArgumentException e1) {
							e1.printStackTrace();
							GuiErrorHandler.processErrorDialogException(e1);
						} catch(IllegalAccessException e1) {
							e1.printStackTrace();
							GuiErrorHandler.processErrorDialogException(e1);
						}
					});

					return box;
				} else {

					JTextField box = new JTextField();
					box.setEditable(ann.editable());
					box.setText(String.valueOf(field.getInt(info)));
					applyInterface = new ApplyInterface() {

						@Override
						public void afterApply(Field field, ElementInformation info) throws IllegalArgumentException, IllegalAccessException {
							box.setText(String.valueOf(field.getInt(info)));
						}

						@Override
						public void apply(Field field, ElementInformation info) throws IllegalArgumentException, IllegalAccessException {
							int parseInteger = Integer.parseInt(box.getText());
							if(annotation.from() != -1 && annotation.to() != -1) {
								parseInteger = Math.max(annotation.from(), Math.min(annotation.to(), parseInteger));
							}
							field.setInt(info, parseInteger);
						}
					};
					JPanel p = new JPanel();
					p.add(box);
					return box;
				}
			} else {
				JTextField box = new JTextField();
				box.setText(String.valueOf(field.getInt(info)));
				box.setEditable(false);
				return box;
			}
		} else if(clazz == long.class) {
			Element annotation = field.getAnnotation(org.schema.game.common.data.element.annotation.Element.class);

			if(annotation != null) {

				if(annotation.states().length > 0) {
					//#RM1958 remove JComboBox generic argument
					JComboBox box = new JComboBox();
					for(int i = 0; i < annotation.states().length; i++) {
						box.addItem(new ValueClass(Long.parseLong(annotation.states()[i]), (annotation.stateDescs().length == annotation.states().length) ? annotation.stateDescs()[i] : annotation.states()[i]));
						if(Long.parseLong(annotation.states()[i]) == field.getLong(info)) {
							box.setSelectedIndex(i);
						}
					}
					//					box.setSelectedItem(field.getLong(info));
					box.addActionListener(e -> {
						try {
							long parseLong = ((ValueClass<Long>) box.getSelectedItem()).value;

							field.setLong(info, parseLong);

						} catch(IllegalArgumentException e1) {
							e1.printStackTrace();
							GuiErrorHandler.processErrorDialogException(e1);
						} catch(IllegalAccessException e1) {
							e1.printStackTrace();
							GuiErrorHandler.processErrorDialogException(e1);
						}
					});
					return box;
				} else {

					JTextField box = new JTextField();
					box.setText(String.valueOf(field.getLong(info)));
					applyInterface = new ApplyInterface() {

						@Override
						public void afterApply(Field field, ElementInformation info) throws IllegalArgumentException, IllegalAccessException {
							box.setText(String.valueOf(field.getLong(info)));
						}

						@Override
						public void apply(Field field, ElementInformation info) throws IllegalArgumentException, IllegalAccessException {
							long parseLong = Long.parseLong(box.getText());
							if(annotation.from() != -1 && annotation.to() != -1) {
								parseLong = Math.max(annotation.from(), Math.min(annotation.to(), parseLong));
							}
							field.setLong(info, parseLong);

						}
					};

					return box;
				}
			} else {
				JTextField box = new JTextField();
				box.setText(String.valueOf(field.getLong(info)));
				box.setEditable(false);
				return box;
			}
		} else if(clazz == short.class) {
			Element annotation = field.getAnnotation(org.schema.game.common.data.element.annotation.Element.class);

			if(annotation != null) {

				if(annotation.states().length > 0) {
					//#RM1958 remove JComboBox generic argument
					JComboBox box = new JComboBox();
					for(int i = 0; i < annotation.states().length; i++) {
						box.addItem(new ValueClass(Short.parseShort(annotation.states()[i]), (annotation.stateDescs().length == annotation.states().length) ? annotation.stateDescs()[i] : annotation.states()[i]));

						if(Short.parseShort(annotation.states()[i]) == field.getShort(info)) {
							box.setSelectedIndex(i);
						}
					}
					//					box.setSelectedItem(field.getShort(info));
					box.addActionListener(e -> {
						try {
							short parseShort = ((ValueClass<Short>) box.getSelectedItem()).value;

							field.setShort(info, parseShort);

						} catch(IllegalArgumentException e1) {
							e1.printStackTrace();
							GuiErrorHandler.processErrorDialogException(e1);
						} catch(IllegalAccessException e1) {
							e1.printStackTrace();
							GuiErrorHandler.processErrorDialogException(e1);
						}
					});

					return box;
				} else {

					JTextField box = new JTextField();
					box.setText(String.valueOf(field.getShort(info)));
					applyInterface = new ApplyInterface() {

						@Override
						public void afterApply(Field field, ElementInformation info) throws IllegalArgumentException, IllegalAccessException {
							box.setText(String.valueOf(field.getShort(info)));
						}

						@Override
						public void apply(Field field, ElementInformation info) throws IllegalArgumentException, IllegalAccessException {
							short parseShort = Short.parseShort(box.getText());
							if(annotation.from() != -1 && annotation.to() != -1) {
								parseShort = (short) Math.max(annotation.from(), Math.min(annotation.to(), parseShort));
							}
							field.setShort(info, parseShort);

						}
					};
					return box;
				}
			} else {

				JTextField box = new JTextField();
				box.setText(String.valueOf(field.getShort(info)));
				box.setEditable(false);
				return box;
			}
		} else if(clazz == float.class) {
			Element annotation = field.getAnnotation(org.schema.game.common.data.element.annotation.Element.class);

			if(annotation != null) {

				if(annotation.states().length > 0) {
					//#RM1958 remove JComboBox generic argument
					JComboBox box = new JComboBox();
					for(int i = 0; i < annotation.states().length; i++) {
						box.addItem(new ValueClass(Float.parseFloat(annotation.states()[i]), (annotation.stateDescs().length == annotation.states().length) ? annotation.stateDescs()[i] : annotation.states()[i]));

						if(Float.parseFloat(annotation.states()[i]) == field.getFloat(info)) {
							box.setSelectedIndex(i);
						}
					}
					//					box.setSelectedItem(field.getFloat(info));
					box.addActionListener(e -> {
						try {
							float parseFloat = ((ValueClass<Float>) box.getSelectedItem()).value;

							field.setFloat(info, parseFloat);

						} catch(IllegalArgumentException e1) {
							e1.printStackTrace();
							GuiErrorHandler.processErrorDialogException(e1);
						} catch(IllegalAccessException e1) {
							e1.printStackTrace();
							GuiErrorHandler.processErrorDialogException(e1);
						}
					});

					return box;
				} else {

					JTextField box = new JTextField();
					box.setText(String.valueOf(field.getFloat(info)));
					applyInterface = new ApplyInterface() {

						@Override
						public void afterApply(Field field, ElementInformation info) throws IllegalArgumentException, IllegalAccessException {
							box.setText(String.valueOf(field.getFloat(info)));
						}

						@Override
						public void apply(Field field, ElementInformation info) throws IllegalArgumentException, IllegalAccessException {
							float parseFloat = Float.parseFloat(box.getText());
							if(annotation.from() != -1 && annotation.to() != -1) {
								parseFloat = Math.max(annotation.from(), Math.min(annotation.to(), parseFloat));
							}
							field.setFloat(info, parseFloat);

						}
					};
					return box;
				}
			} else {

				JTextField box = new JTextField();
				box.setText(String.valueOf(field.getShort(info)));
				box.setEditable(false);
				return box;
			}
		}
		return new JLabel("Cannot parse " + field.getName() + ": " + clazz.getSimpleName());
	}

	public void apply() {
		try {
			applyInterface.apply(field, info);

		} catch(IllegalArgumentException e) {
			e.printStackTrace();
			GuiErrorHandler.processErrorDialogException(e);
		} catch(IllegalAccessException e) {
			e.printStackTrace();
			GuiErrorHandler.processErrorDialogException(e);
		}

		try {
			applyInterface.afterApply(field, info);
		} catch(IllegalArgumentException e) {
			e.printStackTrace();
			GuiErrorHandler.processErrorDialogException(e);
		} catch(IllegalAccessException e) {
			e.printStackTrace();
			GuiErrorHandler.processErrorDialogException(e);
		}
	}

	@Override
	public int compareTo(ElementInformationOption o) {
		return order - o.order;
	}

	public void setLocalOrder(int i) {
		localOrder = i;
		if(localOrder % 2 == 0) {
			setBackground(Color.gray.brighter());
			mainPanel.setBackground(Color.gray.brighter());
		}
	}

	private class StringListModel extends AbstractListModel<String> {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private final List<String> o;

		public StringListModel(List<String> o) {
			this.o = o;
		}

		@Override
		public int getSize() {
			return o.size();
		}

		@Override
		public String getElementAt(int index) {
			return o.get(index);
		}

		public void setChanged() {
			fireContentsChanged(this, 0, o.size());
		}
	}

	private class ValueClass<E> {
		public E value;
		public String desc;

		public ValueClass(E value, String desc) {
			this.value = value;
			this.desc = desc;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return value.hashCode();
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof ValueClass) {
				return value.equals(((ValueClass<E>) obj).value);
			} else {
				return value.equals(obj);
			}
		}

		@Override
		public String toString() {
			return desc;
		}

	}

}
