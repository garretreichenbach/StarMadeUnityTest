package org.schema.schine.common.language.editor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class StatsAndToolsPanel extends JPanel implements Observer{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private LanguageEditor l;
	private JLabel lblTotal;
	private JLabel lblTranslated;
	private JLabel lblMissing;

	/**
	 * Create the panel.
	 * @param l 
	 * @param f 
	 */
	public StatsAndToolsPanel(JFrame f, LanguageEditor l) {
		l.addObserver(this);
		setLayout(new GridBagLayout());
		this.l = l;
		lblTotal = new JLabel("Total:");
		GridBagConstraints gbc_lblTotal = new GridBagConstraints();
		gbc_lblTotal.weightx = 1.0;
		gbc_lblTotal.anchor = GridBagConstraints.WEST;
		gbc_lblTotal.insets = new Insets(0, 0, 0, 5);
		gbc_lblTotal.gridx = 0;
		gbc_lblTotal.gridy = 0;
		add(lblTotal, gbc_lblTotal);
		
		lblTranslated = new JLabel("Translated:");
		GridBagConstraints gbc_lblTranslated = new GridBagConstraints();
		gbc_lblTranslated.weightx = 1.0;
		gbc_lblTranslated.insets = new Insets(0, 0, 0, 5);
		gbc_lblTranslated.gridx = 1;
		gbc_lblTranslated.gridy = 0;
		add(lblTranslated, gbc_lblTranslated);
		
		lblMissing = new JLabel("Missing:");
		GridBagConstraints gbc_lblMissing = new GridBagConstraints();
		gbc_lblMissing.weightx = 1.0;
		gbc_lblMissing.anchor = GridBagConstraints.EAST;
		gbc_lblMissing.gridx = 2;
		gbc_lblMissing.gridy = 0;
		add(lblMissing, gbc_lblMissing);
	}

	@Override
	public void update(Observable o, Object arg) {
		if(arg == null || arg == getClass()){
			lblTotal.setText("Total: "+l.total);
			lblTranslated.setText("Translated: "+l.translated);
			lblMissing.setText("Missing: "+l.missing);
		}
	}

}
