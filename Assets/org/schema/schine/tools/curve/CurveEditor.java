package org.schema.schine.tools.curve;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.schema.schine.graphicsengine.psys.modules.variable.PSCurveVariable;

public class CurveEditor extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Create the panel.
	 */
	public CurveEditor(JFrame frame, PSCurveVariable var) {
		setLayout(new GridBagLayout());

		EquationDisplay equationDisplay = new SplineDisplay(new PSCurveVariable[]{var});
		GridBagConstraints gbc_equationDisplay = new GridBagConstraints();
		gbc_equationDisplay.weighty = 1.0;
		gbc_equationDisplay.weightx = 1.0;
		gbc_equationDisplay.fill = GridBagConstraints.BOTH;
		gbc_equationDisplay.gridx = 0;
		gbc_equationDisplay.gridy = 0;
		add(equationDisplay, gbc_equationDisplay);

	}

	public static void main(String[] saf) {
		SwingUtilities.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (ClassNotFoundException e) {
			} catch (InstantiationException e) {
			} catch (IllegalAccessException e) {
			} catch (UnsupportedLookAndFeelException e) {
			}
			JFrame jFrame = new JFrame("CurveEditor");
			jFrame.setContentPane(new CurveEditor(jFrame, new PSCurveVariable() {

				@Override
				public String getName() {
					return "curve";
				}

				@Override
				public Color getColor() {
					return Color.BLUE;
				}
			}));

			jFrame.pack();
			jFrame.setLocationRelativeTo(null);
			jFrame.setResizable(true);

			jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			jFrame.setVisible(true);
//             new CurveEditor()).setVisible(true);
		});
	}
}
