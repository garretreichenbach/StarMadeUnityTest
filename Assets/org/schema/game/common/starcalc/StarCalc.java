package org.schema.game.common.starcalc;

import java.awt.EventQueue;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import org.schema.schine.common.language.Lng;

public class StarCalc extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private JPanel contentPane;

	/**
	 * Create the frame.
	 */
	public StarCalc() {
		setTitle("StarCalc");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 1142, 610);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		//		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		//		gbc_tabbedPane.weighty = 1.0;
		//		gbc_tabbedPane.weightx = 1.0;
		//		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		//		gbc_tabbedPane.gridx = 0;
		//		gbc_tabbedPane.gridy = 0;
		contentPane.add(tabbedPane, new BoxLayout(contentPane, BoxLayout.X_AXIS));

		WeaponFormulaCalculator weaponFormulaCalculator = new WeaponFormulaCalculator();
		tabbedPane.addTab(Lng.str("Formula Calculator"), null, weaponFormulaCalculator, null);
		weaponFormulaCalculator.setLayout(new BoxLayout(weaponFormulaCalculator, BoxLayout.X_AXIS));
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				StarCalc frame = new StarCalc();
				frame.setVisible(true);
				frame.requestFocus();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

}
