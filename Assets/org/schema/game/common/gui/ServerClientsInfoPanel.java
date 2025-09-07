package org.schema.game.common.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerController;
import org.schema.schine.network.server.ServerMessage;

public class ServerClientsInfoPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private JTable clientTable;
	private ClientTableModel clientTableModel;
	private ServerController serverController;

	/**
	 * Create the panel.
	 */
	public ServerClientsInfoPanel(ServerController c) {
		this.serverController = c;
		clientTableModel = new ClientTableModel();
		setLayout(new BorderLayout(0, 0));
		clientTable = new JTable(clientTableModel);
		clientTable.setFillsViewportHeight(true);
		clientTable.setAutoCreateRowSorter(true);
		clientTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				int r = clientTable.rowAtPoint(e.getPoint());
				if (r >= 0 && r < clientTable.getRowCount()) {
					clientTable.setRowSelectionInterval(r, r);
				} else {
					clientTable.clearSelection();
				}

				int rowindex = clientTable.getSelectedRow();
				if (rowindex < 0)
					return;
				if (e.isPopupTrigger() && e.getComponent() instanceof JTable) {
					JPopupMenu popup = popUp(rowindex);
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}

		});

		add(clientTable);
		add(clientTable, BorderLayout.CENTER);
		add(clientTable.getTableHeader(), BorderLayout.NORTH);

	}

	private JPopupMenu popUp(final int rowindex) {
		JPopupMenu menu = new JPopupMenu();
		JMenuItem kickItem = new JMenuItem("KICK");
		kickItem.addActionListener(es -> {
			try {
				Object valueAt = clientTableModel.getValueAt(rowindex, 1);
				String name = clientTableModel.getValueAt(rowindex, 2).toString();
				System.err.println("[SERVER] GUI KICK PLAYER: " + valueAt);
				serverController.sendLogout(Integer.parseInt(valueAt.toString()), Lng.str("You have been manually kicked!"));
				serverController.unregister(Integer.parseInt(valueAt.toString()));
				serverController.broadcastMessage(Lng.astr("Player %s has been kicked!",  name), ServerMessage.MESSAGE_TYPE_SIMPLE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		menu.add(kickItem);
		return menu;
	}

	public void update(ServerController server) {
		clientTableModel.update(server);

	}

}
