package org.schema.schine.tools.gradient;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.schema.schine.graphicsengine.psys.modules.variable.PSGradientVariable;

public class ColorAdd extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JSlider slider;
	private float lastColorValue;

	/**
	 * Create the panel.
	 */
	public ColorAdd(final LinearGradientChooser chooser, final PSGradientVariable var, float val) {
		setLayout(new GridBagLayout());

		final JButton btnColor = new JButton("Pick");
		btnColor.addActionListener(e -> {

			final JColorChooser tcc = new JColorChooser(Color.RED);
			JDialog d = new JDialog();
			d.setContentPane(tcc);
			d.setAlwaysOnTop(true);
			d.setLocationRelativeTo(null);
			d.setVisible(true);
			d.pack();
			tcc.getSelectionModel().addChangeListener(e1 -> {
				Color newColor = tcc.getColor();
				btnColor.setBackground(newColor);
				btnColor.setContentAreaFilled(false);
				btnColor.setOpaque(true);
				chooser.update(lastColorValue, lastColorValue, btnColor.getBackground());
				//				        btnColor.setForeground(newColor);
			});

		});
		GridBagConstraints gbc_btnColor = new GridBagConstraints();
		gbc_btnColor.anchor = GridBagConstraints.WEST;
		gbc_btnColor.insets = new Insets(0, 0, 0, 5);
		gbc_btnColor.gridx = 0;
		gbc_btnColor.gridy = 0;
		add(btnColor, gbc_btnColor);

		slider = new JSlider();
		slider.setValue((int) (val * 100f));
		slider.addChangeListener(e -> {
			chooser.update(lastColorValue, getColorValue(), btnColor.getBackground());

			lastColorValue = getColorValue();

		});
		GridBagConstraints gbc_slider = new GridBagConstraints();
		gbc_slider.fill = GridBagConstraints.BOTH;
		gbc_slider.weightx = 1.0;
		gbc_slider.insets = new Insets(0, 0, 0, 5);
		gbc_slider.gridx = 1;
		gbc_slider.gridy = 0;
		add(slider, gbc_slider);

		lastColorValue = getColorValue();

		JButton btnRemove = new JButton("del");
		btnRemove.addActionListener(e -> chooser.removeColor(ColorAdd.this, var));
		GridBagConstraints gbc_btnRemove = new GridBagConstraints();
		gbc_btnRemove.anchor = GridBagConstraints.EAST;
		gbc_btnRemove.gridx = 2;
		gbc_btnRemove.gridy = 0;
		add(btnRemove, gbc_btnRemove);
	}

	public float getColorValue() {
		return slider.getValue() / 100f;
	}

}
