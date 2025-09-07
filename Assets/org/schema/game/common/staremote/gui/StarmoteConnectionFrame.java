package org.schema.game.common.staremote.gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.schema.game.common.staremote.Staremote;
import org.schema.game.common.staremote.gui.connection.StarmoteServerPanel;

public class StarmoteConnectionFrame extends JFrame {

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
	public StarmoteConnectionFrame(Staremote starmote) {
		setTitle("Starmote Connection");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 468, 359);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		StarmoteServerPanel starmoteServerPanel = new StarmoteServerPanel(this, starmote);
		contentPane.add(starmoteServerPanel, BorderLayout.CENTER);
	}

}
