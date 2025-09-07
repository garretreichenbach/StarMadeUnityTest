package org.schema.game.common.gui;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerController;
import org.schema.schine.network.server.ServerState;

public class ServerGUI extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private JPanel contentPane;
	private ServerController serverController;

	/**
	 * Create the frame.
	 */
	public ServerGUI(ServerController c) {
		setTitle("StarMade Server Manager");
		this.serverController = c;
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				System.out.println("[CLIENT] Intercepted window closing. Doing soft shutdown");
				ServerState.setFlagShutdown(true);
			}
		});

		setBounds(100, 100, 668, 363);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane, BorderLayout.CENTER);

		ServerMainPanel serverMainPanel = new ServerMainPanel(serverController);
		tabbedPane.addTab(Lng.str("Main"), null, serverMainPanel, null);

	}

}
