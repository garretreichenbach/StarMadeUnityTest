package org.schema.game.common.gui;

import java.util.Collection;

import javax.swing.table.AbstractTableModel;

import org.schema.schine.network.RegisteredClientOnServer;
import org.schema.schine.network.server.ServerController;
import org.schema.schine.network.server.ServerProcessor;

public class ClientTableModel extends AbstractTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	

	private ServerController serverController;

	public ClientTableModel() {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
		if (column == 0) {
			return "#";
		}
		if (column == 1) {
			return "ID";
		}
		if (column == 2) {
			return "Name";
		}
		if (column == 3) {
			return "NetStatus";
		}
		if (column == 4) {
			return "Ping";
		}

		return "unknown";
	}

	@Override
	public Class<? extends Object> getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	@Override
	public int getRowCount() {
		try {
			return serverController.getServerState().getClients().size();
		} catch (Exception e) {
		}
		return 0;
	}

	@Override
	public int getColumnCount() {
		return 5;
	}

	@Override
	public Object getValueAt(int x, int y) {

		try {
			Collection<RegisteredClientOnServer> values = serverController.getServerState().getClients().values();
			RegisteredClientOnServer client = null;
			int i = 0;
			for (RegisteredClientOnServer c : values) {
				if (i >= x) {
					client = c;
					break;
				}
				i++;
			}
			ServerProcessor gameObject = (ServerProcessor) client.getProcessor();
			if (y == 0) {
				return String.valueOf(x);
			}
			if (gameObject.getClient() == null) {
				return "UNKNOWN -4242";
			}
			if (y == 1) {
				return client.getId();
			}
			if (y == 2) {
				return client.getClientName();
			}
			if (y == 3) {
				return gameObject.isConnected() ? "alive" : "dead";
			}
			if (y == 4) {
				return gameObject.getPing();
			}

		} catch (Exception e) {
		}
		return "-";

	}

	public void initData(ServerController arg0) {
		this.serverController = arg0;
	}

	public void update(ServerController server) {

		initData(server);
		fireTableDataChanged();

	}

}
