package org.schema.schine.graphicsengine.psys.modules;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.LineBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.schema.schine.graphicsengine.psys.ParticleSystemConfiguration;
import org.schema.schine.graphicsengine.psys.modules.variable.BooleanInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.DropDownInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.PSCurveVariable;
import org.schema.schine.graphicsengine.psys.modules.variable.PSGradientVariable;
import org.schema.schine.graphicsengine.psys.modules.variable.PSVariable;
import org.schema.schine.graphicsengine.psys.modules.variable.StringPair;
import org.schema.schine.graphicsengine.psys.modules.variable.VarInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.XMLSerializable;
import org.schema.schine.tools.curve.SplineDisplay;
import org.schema.schine.tools.gradient.LinearGradientChooser;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class ParticleSystemModule {
	public static final int SPACE_WORLD = 0;
	public static final int SPACE_LOCAL = 1;
	public static int QUALITY_LOW = 0;
	public static int QUALITY_MID = 1;
	public static int QUALITY_HIGH = 2;
	protected final ParticleSystemConfiguration sys;
	private boolean enabled;

	public ParticleSystemModule(ParticleSystemConfiguration sys) {
		this.sys = sys;
	}

	/**
	 * @return the enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	public boolean canDisable() {
		return true;
	}

	/**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		if (enabled != this.enabled) {
			sys.setModuleEnabledChanged(true);
		}
		this.enabled = enabled;
	}

	public Element serialize(Document doc) throws IllegalArgumentException, IllegalAccessException {
		System.out.println("[XML] Writing module: [" + getName() + "] as [" + getTagName() + "]");
		Element elementRoot = doc.createElement(getTagName());
		elementRoot.setAttribute("enabled", String.valueOf(enabled));
		Field[] fields = getClass().getDeclaredFields();
		for (int j = 0; j < fields.length; j++) {
			fields[j].setAccessible(true);
			Annotation[] annotations = fields[j].getAnnotations();

			for (int k = 0; k < annotations.length; k++) {
				if (annotations[k] instanceof XMLSerializable) {
					XMLSerializable annotation = (XMLSerializable) annotations[k];
					if (annotation.type().toLowerCase(Locale.ENGLISH).equals("int")) {
						append(annotation.name(), fields[j].getInt(this), elementRoot);
					} else if (annotation.type().toLowerCase(Locale.ENGLISH).equals("float")) {
						append(annotation.name(), fields[j].getFloat(this), elementRoot);
					} else if (annotation.type().toLowerCase(Locale.ENGLISH).equals("boolean")) {
						append(annotation.name(), fields[j].getBoolean(this), elementRoot);
					} else if (annotation.type().toLowerCase(Locale.ENGLISH).equals("gradient")) {
						append(annotation.name(), fields[j].get(this), elementRoot);
					} else if (annotation.type().toLowerCase(Locale.ENGLISH).equals("curve")) {
						append(annotation.name(), fields[j].get(this), elementRoot);
					} else if (annotation.type().toLowerCase(Locale.ENGLISH).equals("string")) {
						append(annotation.name(), fields[j].get(this), elementRoot);
					} else if (annotation.type().toLowerCase(Locale.ENGLISH).equals("long")) {
						append(annotation.name(), fields[j].get(this), elementRoot);
					} else {
						throw new IllegalArgumentException(annotation.type());
					}

				}
			}
		}

		return elementRoot;
	}

	public String getTagName() {
		return getName().replaceAll(" ", "");
	}

	public void deserialize(Node node) throws NumberFormatException, IllegalArgumentException, IllegalAccessException, DOMException {

		enabled = Boolean.parseBoolean(node.getAttributes().getNamedItem("enabled").getNodeValue());

		NodeList childNodes = node.getChildNodes();

		Field[] fields = getClass().getDeclaredFields();
//		System.err.println("NOW PARSING NODE: "+node.getNodeName());

		for (int i = 0; i < childNodes.getLength(); i++) {

			Node item = childNodes.item(i);

			if (item.getNodeType() == Node.ELEMENT_NODE) {

				for (int j = 0; j < fields.length; j++) {
					fields[j].setAccessible(true);
					Annotation[] annotations = fields[j].getAnnotations();

//					System.err.println("NOW PARSING: "+item.getNodeName()+"->"+item.getTextContent()+"; -> "+fields[j].getName()+"; "+Arrays.toString(annotations));
					for (int k = 0; k < annotations.length; k++) {
						if (annotations[k] instanceof XMLSerializable) {
							try {
								XMLSerializable annotation = (XMLSerializable) annotations[k];
								if (item.getNodeName().toLowerCase(Locale.ENGLISH).equals(annotation.name().toLowerCase(Locale.ENGLISH))) {
									if (annotation.type().toLowerCase(Locale.ENGLISH).equals("int")) {
										fields[j].setInt(this, Integer.parseInt(item.getTextContent()));
									} else if (annotation.type().toLowerCase(Locale.ENGLISH).equals("float")) {
										fields[j].setFloat(this, Float.parseFloat(item.getTextContent()));
									} else if (annotation.type().toLowerCase(Locale.ENGLISH).equals("boolean")) {
										fields[j].setBoolean(this, Boolean.parseBoolean(item.getTextContent()));
									} else if (annotation.type().toLowerCase(Locale.ENGLISH).equals("gradient")) {
										((PSVariable<?>) fields[j].get(this)).parse(item);
									} else if (annotation.type().toLowerCase(Locale.ENGLISH).equals("curve")) {
										((PSVariable<?>) fields[j].get(this)).parse(item);
									} else if (annotation.type().toLowerCase(Locale.ENGLISH).equals("string")) {
										fields[j].set(this, item.getTextContent());
									} else if (annotation.type().toLowerCase(Locale.ENGLISH).equals("long")) {
										fields[j].setLong(this, Long.parseLong(item.getTextContent()));
									} else {
										throw new IllegalArgumentException(annotation.type());
									}
								}
							} catch (RuntimeException e) {
								e.printStackTrace();
								System.err.println("Exception happened on node: " + item.getNodeName());
								throw e;
							}
						}
					}
				}
			}
		}

	}

	protected void append(String name, Object m, Element element) {
		Element createElement = element.getOwnerDocument().createElement(name);
		if (m instanceof PSVariable<?>) {
			PSVariable<?> ps = (PSVariable<?>) m;
			ps.appendXML(m, createElement);
		} else {
			createElement.setTextContent(m.toString());
		}
		element.appendChild(createElement);
	}

	protected void addRow(JPanel p, int row, String title, JComponent component) {
		{
			GridBagConstraints gbc_equationDisplay = new GridBagConstraints();
			gbc_equationDisplay.weighty = 0.0;
			gbc_equationDisplay.weightx = 1.0;
			gbc_equationDisplay.anchor = GridBagConstraints.WEST;
			gbc_equationDisplay.fill = GridBagConstraints.NONE;
			gbc_equationDisplay.gridx = 0;
			gbc_equationDisplay.gridy = row;
			p.add(new JLabel(title), gbc_equationDisplay);
		}
		{
			GridBagConstraints gbc_equationDisplay = new GridBagConstraints();
			gbc_equationDisplay.weighty = 0.0;
			gbc_equationDisplay.weightx = 1.0;
			gbc_equationDisplay.anchor = GridBagConstraints.EAST;
			gbc_equationDisplay.fill = GridBagConstraints.NONE;
			gbc_equationDisplay.gridx = 1;
			gbc_equationDisplay.gridy = row;
			p.add(component, gbc_equationDisplay);
		}
	}

	protected void addRowTitledBorder(JPanel p, int row, String[] title, Color[] color, JComponent component) {
		JPanel pp = new JPanel();
		pp.setLayout(new GridBagLayout());
		{
			// #RM1958 pp.setBorder(new Border(new BasicStroke()));
			pp.setBorder(LineBorder.createBlackLineBorder());

			JPanel namePanel = new JPanel();
			namePanel.setLayout(new GridBagLayout());

			for (int i = 0; i < title.length; i++) {
				JLabel nameLabel = new JLabel(title[i]);
				nameLabel.setForeground(color[i]);

				GridBagConstraints gbc = new GridBagConstraints();
				gbc.weighty = 0.0;
				gbc.weightx = 0.0;
				gbc.insets = new Insets(0, 0, 3, 10);
				gbc.anchor = GridBagConstraints.NORTHWEST;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.gridx = i;
				gbc.gridy = 0;
				namePanel.add(nameLabel, gbc);
			}

			{
				GridBagConstraints gbc_equationDisplay = new GridBagConstraints();
				gbc_equationDisplay.weighty = 0.0;
				gbc_equationDisplay.weightx = 0.0;
				gbc_equationDisplay.anchor = GridBagConstraints.SOUTHWEST;
				gbc_equationDisplay.fill = GridBagConstraints.NONE;
				gbc_equationDisplay.gridx = 0;
				gbc_equationDisplay.gridy = 0;
				pp.add(namePanel, gbc_equationDisplay);
			}
			{
				GridBagConstraints gbc_equationDisplay = new GridBagConstraints();
				gbc_equationDisplay.weighty = 0.0;
				gbc_equationDisplay.weightx = 0.0;
				gbc_equationDisplay.anchor = GridBagConstraints.EAST;
				gbc_equationDisplay.fill = GridBagConstraints.BOTH;
				gbc_equationDisplay.gridx = 0;
				gbc_equationDisplay.gridy = 1;
				pp.add(component, gbc_equationDisplay);
			}
		}
		{
			GridBagConstraints gbc_equationDisplay = new GridBagConstraints();
			gbc_equationDisplay.weighty = 0.0;
			gbc_equationDisplay.weightx = 1.0;
			gbc_equationDisplay.anchor = GridBagConstraints.EAST;
			gbc_equationDisplay.fill = GridBagConstraints.BOTH;
			gbc_equationDisplay.gridx = 1;
			gbc_equationDisplay.gridwidth = 1;
			gbc_equationDisplay.gridy = row;
			p.add(pp, gbc_equationDisplay);
		}
	}

	public void addRow(JPanel p, int row, final DropDownInterface f) {
		{
			// #RM1958 removed java generics on JComboBox
			final JComboBox input = new JComboBox(f);
			input.setPreferredSize(new Dimension(150, 24));
			input.setSelectedIndex(f.getCurrentIndex());
			input.addActionListener(e -> {
				if (input.getSelectedItem() != null) {
					f.set((StringPair) input.getSelectedItem());
				}
			});
			addRow(p, row, f.getName(), input);
		}
	}

	public void addRow(JPanel p, int row, final PSGradientVariable gradient) {
		LinearGradientChooser grad = new LinearGradientChooser(gradient);
		addRowTitledBorder(p, row, new String[]{gradient.getName()}, new Color[]{Color.BLACK}, grad);
	}

	public void addRow(JPanel p, int row, final PSCurveVariable... curve) {
		final SplineDisplay splineDisplay = new SplineDisplay(curve);

		String[] s = new String[curve.length];
		Color[] c = new Color[curve.length];

		for (int i = 0; i < curve.length; i++) {
			s[i] = curve[i].getName();
			c[i] = curve[i].getColor();
		}

		JLabel options = new JLabel();
		options.setLayout(new GridBagLayout());
		for (int i = 0; i < curve.length; i++) {
			final PSCurveVariable psCurveVariable = curve[i];
			final JCheckBox m = new JCheckBox("IN");
			final JTextField t = new JTextField(String.valueOf(psCurveVariable.getBase()) + "     ");
			final JButton b = new JButton("X");
			m.setForeground(psCurveVariable.getColor());
			b.setForeground(psCurveVariable.getColor());
			t.addCaretListener(e -> {
				try {
					psCurveVariable.setBase(Float.parseFloat(t.getText()));
				} catch (NumberFormatException ex) {
					t.setText("1.0");
				}
			});
			m.addActionListener(e -> {
				psCurveVariable.setUseIntegral(m.isSelected());
				splineDisplay.repaint();
			});
			b.addActionListener(e -> {
				psCurveVariable.reset();
				splineDisplay.repaint();
			});

			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.weighty = 0.0;
				gbc.weightx = 0.0;
				gbc.insets = new Insets(0, 0, 3, 3);
				gbc.anchor = GridBagConstraints.NORTHWEST;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.gridx = 0;
				gbc.gridy = i;
				options.add(m, gbc);
			}
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.weighty = 0.0;
				gbc.weightx = 0.0;
				gbc.insets = new Insets(0, 0, 3, 3);
				gbc.anchor = GridBagConstraints.NORTHWEST;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.gridx = 1;
				gbc.gridy = i;
				options.add(t, gbc);
			}
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.weighty = 0.0;
				gbc.weightx = 0.0;
				gbc.insets = new Insets(0, 0, 3, 3);
				gbc.anchor = GridBagConstraints.NORTHWEST;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.gridx = 2;
				gbc.gridy = i;
				options.add(b, gbc);
			}

		}
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weighty = 0.0;
		gbc.weightx = 0.0;
//		gbc.insets = new Insets(0, 0, 3, 10);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = row;
		p.add(options, gbc);
		addRowTitledBorder(p, row, s, c, splineDisplay);
	}

	public void addRow(JPanel p, int row, final BooleanInterface f) {
		{
			final JCheckBox input = new JCheckBox();
			input.setSelected(f.get());
			input.addActionListener(e -> f.set(input.isSelected()));
			addRow(p, row, f.getName(), input);
		}
	}

	public void addRow(JPanel p, int row, final VarInterface<?> f) {
		{
			final JTextField input = new JTextField();
			input.setText(String.valueOf(f.get()));
			input.setColumns(18);
			input.addCaretListener(e -> {
				try {
					f.set(input.getText());
					System.err.println("input set to " + f.get());
				} catch (Exception ex) {
					ex.printStackTrace();
					input.setText(String.valueOf(f.getDefault()));
				}

			});
			addRow(p, row, f.getName(), input);
		}
	}

	protected abstract JPanel getConfigPanel();

	private JPanel getTabPanel() {
		final JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		// #RM1958 p.setBorder(new StrokeBorder(new BasicStroke()));
		p.setBorder(LineBorder.createBlackLineBorder());
		{
			GridBagConstraints gbc_equationDisplay = new GridBagConstraints();
			gbc_equationDisplay.weighty = 0.0;
			gbc_equationDisplay.weightx = 0.0;
			gbc_equationDisplay.anchor = GridBagConstraints.WEST;
			gbc_equationDisplay.fill = GridBagConstraints.NONE;
			gbc_equationDisplay.gridx = 0;
			gbc_equationDisplay.gridy = 0;
			final JCheckBox jCheckBox = new JCheckBox();
			p.add(jCheckBox, gbc_equationDisplay);
			jCheckBox.setSelected(isEnabled());
			if (!canDisable())
			{
				setEnabled(true);
				jCheckBox.setSelected(true);
				jCheckBox.setEnabled(false);
			}

			jCheckBox.addActionListener(e -> {
				if (canDisable())
				{
					setEnabled(jCheckBox.isSelected());
					jCheckBox.setSelected(isEnabled());
				}
			});
		}
		{
			GridBagConstraints gbc_equationDisplay = new GridBagConstraints();
			gbc_equationDisplay.weighty = 0.0;
			gbc_equationDisplay.weightx = 1.0;
			gbc_equationDisplay.anchor = GridBagConstraints.WEST;
			gbc_equationDisplay.fill = GridBagConstraints.NONE;
			gbc_equationDisplay.gridx = 1;
			gbc_equationDisplay.gridy = 0;
			p.add(new JLabel(getName()), gbc_equationDisplay);
		}
		{
			GridBagConstraints gbc_equationDisplay = new GridBagConstraints();
			gbc_equationDisplay.weighty = 0.0;
			gbc_equationDisplay.weightx = 1.0;
			gbc_equationDisplay.anchor = GridBagConstraints.EAST;
			gbc_equationDisplay.fill = GridBagConstraints.NONE;
			gbc_equationDisplay.gridx = 2;
			gbc_equationDisplay.gridy = 0;
			final JToggleButton jToggleButton = new JToggleButton("Show");
			jToggleButton.addActionListener(e -> {
				if (jToggleButton.isSelected()) {
					jToggleButton.setText("Hide");
					p.firePropertyChange("hidden", false, true);
				} else {
					jToggleButton.setText("Show");
					p.firePropertyChange("hidden", true, false);
				}
			});
			p.add(jToggleButton, gbc_equationDisplay);
		}
		return p;
	}

	public String getName() {
		return "undefined(" + getClass().getSimpleName() + ")";
	}

	public JPanel getPanel() {
		final JPanel p = new JPanel();

		// #RM1958 p.setBorder(new StrokeBorder(new BasicStroke()));
		p.setBorder(LineBorder.createBlackLineBorder());
		p.setLayout(new GridBagLayout());
		JPanel tabPanel = getTabPanel();
		tabPanel.setBackground(Color.LIGHT_GRAY);
		{
			GridBagConstraints gbc_equationDisplay = new GridBagConstraints();
			gbc_equationDisplay.weighty = 0.0;
			gbc_equationDisplay.weightx = 1.0;
			gbc_equationDisplay.anchor = GridBagConstraints.WEST;
			gbc_equationDisplay.fill = GridBagConstraints.HORIZONTAL;
			gbc_equationDisplay.gridx = 0;
			gbc_equationDisplay.gridy = 0;
			p.add(tabPanel, gbc_equationDisplay);
		}
		final JPanel configPanel = getConfigPanel();
		{

		}
		tabPanel.addPropertyChangeListener("hidden", evt -> {
			if (!(Boolean) evt.getNewValue()) {
				p.remove(configPanel);
			} else {
				GridBagConstraints gbc_equationDisplay = new GridBagConstraints();
				gbc_equationDisplay.weighty = 0.0;
				gbc_equationDisplay.weightx = 1.0;
				gbc_equationDisplay.fill = GridBagConstraints.BOTH;
				gbc_equationDisplay.gridx = 0;
				gbc_equationDisplay.gridy = 1;
				p.add(configPanel, gbc_equationDisplay);
			}
		});
		return p;
	}
}
