package org.schema.game.common.gui;

import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.schema.game.server.data.GameServerState;

public class CatalogManagerEditorController extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private JPanel contentPane;

	//	/**
	//	 * Launch the application.
	//	 */
	//	public static void main(String[] args) {
	//		EventQueue.invokeLater(new Runnable() {
	//			public void run() {
	//				try {
	//					CatalogManager frame = new CatalogManager();
	//				} catch (Exception e) {
	//					e.printStackTrace();
	//				}
	//			}
	//		});
	//	}

	/**
	 * Create the frame.
	 */
	public CatalogManagerEditorController(JFrame parent) {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 688, 351);
		contentPane = new JPanel();
		setTitle("StarMade Ship Catalog Manager");
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		setAlwaysOnTop(true);
		CatalogPanel catalogPanel = new CatalogPanel( this);
		contentPane.add(catalogPanel, BorderLayout.CENTER);
	}

	public static void main(String[] args){
		JFrame f = new JFrame("TT");
		f.setDefaultCloseOperation(EXIT_ON_CLOSE);
		GameServerState.initPaths(false, 0);
		CatalogManagerEditorController s = new CatalogManagerEditorController(f);
		f.setVisible(true);
		s.setVisible(true);
	}
	
}
