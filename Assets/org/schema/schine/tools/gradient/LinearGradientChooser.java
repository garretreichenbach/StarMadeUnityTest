package org.schema.schine.tools.gradient;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.schema.schine.graphicsengine.psys.modules.variable.PSGradientVariable;

import it.unimi.dsi.fastutil.floats.Float2ObjectMap.Entry;
import it.unimi.dsi.fastutil.floats.Float2ObjectRBTreeMap;

public class LinearGradientChooser extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private LinearGradient linearGradient;
	private JPanel panel;

	/**
	 * Create the panel.
	 */
	public LinearGradientChooser(final PSGradientVariable var) {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, 1.0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0};
		setLayout(gridBagLayout);

		final JButton btnPick = new JButton("Pick");
		btnPick.addActionListener(e -> {

			final JColorChooser tcc = new JColorChooser(Color.RED);
			JDialog d = new JDialog();
			d.setContentPane(tcc);
			d.setLocationRelativeTo(null);

			d.setAlwaysOnTop(true);
			d.setVisible(true);
			d.pack();
			tcc.getSelectionModel().addChangeListener(e12 -> {
				Color newColor = tcc.getColor();
				btnPick.setBackground(newColor);
				btnPick.setContentAreaFilled(false);
				btnPick.setOpaque(true);
				var.color.put(0, newColor);
				linearGradient.repaint();
			});

		});
		GridBagConstraints gbc_btnPick = new GridBagConstraints();
		gbc_btnPick.fill = GridBagConstraints.BOTH;
		gbc_btnPick.insets = new Insets(0, 0, 5, 5);
		gbc_btnPick.gridx = 0;
		gbc_btnPick.gridy = 0;
		add(btnPick, gbc_btnPick);

		linearGradient = new LinearGradient(var);
		GridBagConstraints gbc_linearGradient = new GridBagConstraints();
		gbc_linearGradient.insets = new Insets(0, 0, 5, 5);
		gbc_linearGradient.fill = GridBagConstraints.BOTH;
		gbc_linearGradient.gridx = 1;
		gbc_linearGradient.gridy = 0;
		add(linearGradient, gbc_linearGradient);
		GridBagLayout gbl_linearGradient = new GridBagLayout();
		gbl_linearGradient.columnWidths = new int[]{0};
		gbl_linearGradient.rowHeights = new int[]{0};
		gbl_linearGradient.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_linearGradient.rowWeights = new double[]{Double.MIN_VALUE};
		linearGradient.setLayout(gbl_linearGradient);

		final JButton btnPick_1 = new JButton("Pick");
		btnPick_1.addActionListener(e -> {
		});
		GridBagConstraints gbc_btnPick_1 = new GridBagConstraints();
		gbc_btnPick_1.insets = new Insets(0, 0, 5, 0);
		gbc_btnPick_1.fill = GridBagConstraints.BOTH;
		gbc_btnPick_1.gridx = 2;
		gbc_btnPick_1.gridy = 0;
		btnPick_1.addActionListener(e -> {

			final JColorChooser tcc = new JColorChooser(Color.RED);
			JDialog d = new JDialog();
			d.setContentPane(tcc);
			d.setAlwaysOnTop(true);
			d.setLocationRelativeTo(null);
			d.setVisible(true);
			d.pack();
			tcc.getSelectionModel().addChangeListener(e1 -> {
				Color newColor = tcc.getColor();
				btnPick_1.setBackground(newColor);
				btnPick_1.setContentAreaFilled(false);
				btnPick_1.setOpaque(true);
				var.color.put(1, newColor);
				linearGradient.repaint();
			});

		});
		add(btnPick_1, gbc_btnPick_1);

		JButton btnAdd = new JButton("Add");

		GridBagConstraints gbc_btnAdd = new GridBagConstraints();
		gbc_btnAdd.fill = GridBagConstraints.VERTICAL;
		gbc_btnAdd.insets = new Insets(0, 0, 5, 5);
		gbc_btnAdd.gridx = 1;
		gbc_btnAdd.gridy = 1;
		add(btnAdd, gbc_btnAdd);

		btnAdd.addActionListener(e -> {
			panel.add(new ColorAdd(LinearGradientChooser.this, var, 0.5f));
			panel.revalidate();
		});

		panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.weightx = 1.0;
		gbc_panel.gridwidth = 3;
		gbc_panel.insets = new Insets(0, 0, 0, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 2;
		add(panel, gbc_panel);

//		GridBagLayout gbl_panel = new GridBagLayout();
//		gbl_panel.columnWidths = new int[]{0};
//		gbl_panel.rowHeights = new int[]{0};
//		gbl_panel.columnWeights = new double[]{Double.MIN_VALUE};
//		gbl_panel.rowWeights = new double[]{Double.MIN_VALUE};
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		for (Entry<Color> a : var.color.float2ObjectEntrySet()) {
			if (a.getFloatKey() > 0f && a.getFloatKey() < 1.0f) {
				ColorAdd colorAdd = new ColorAdd(LinearGradientChooser.this, var, a.getFloatKey());
				panel.add(colorAdd);
			}

		}
	}

	public void removeColor(ColorAdd colorAdd, PSGradientVariable var) {
		if (colorAdd.getColorValue() > 0 && colorAdd.getColorValue() < 1) {
			Float2ObjectRBTreeMap<Color> color = new Float2ObjectRBTreeMap<Color>(var.color);
			color.remove(colorAdd.getColorValue());
			var.color = color;
		}
		panel.remove(colorAdd);
		panel.revalidate();
		panel.repaint();
	}

	public void update(float lastColorValue, float newValue,
	                   Color background) {
		linearGradient.update(lastColorValue, newValue, background);
	}

}
