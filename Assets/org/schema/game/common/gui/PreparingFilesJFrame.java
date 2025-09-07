package org.schema.game.common.gui;

import java.awt.BorderLayout;
//#RM1958 remove import java.awt.Window.Type;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class PreparingFilesJFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	/**
	 * Create the frame.
	 */
	public PreparingFilesJFrame() {
//		setType(Type.UTILITY);
		setResizable(false);
		setAlwaysOnTop(true);
		setTitle("Preparing Files");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(400, 400, 447, 91);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JLabel lblPreparingFilesone = new JLabel("Preparing Files (one time operation)...");
		contentPane.add(lblPreparingFilesone, BorderLayout.CENTER);
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				PreparingFilesJFrame frame = new PreparingFilesJFrame();
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

}
