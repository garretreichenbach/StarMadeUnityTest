package org.schema.game.server.data;

import java.io.IOException;
import java.sql.SQLException;

import org.schema.game.server.controller.SectorUtil;
import org.schema.schine.network.RegisteredClientInterface;

public class SectorBulkRequest implements ServerExecutionJob {
	public RegisteredClientInterface client;
	private String name;
	private boolean export;

	public SectorBulkRequest(
			RegisteredClientInterface clien, String name, boolean export) {
		super();
		this.client = clien;
		this.name = name;
		this.export = export;
	}

	;

	@Override
	public boolean execute(GameServerState state) {
		try {
			SectorUtil.bulk(state, name, export);
			if (client != null) {
				client.serverMessage("bulk " + (export ? "export" : "import") + " successful to " + name);
			}
			return true;
		} catch (SQLException e) {
			try {
				if (client != null) {
					client.serverMessage("sector exporting failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		} catch (IOException e) {
			try {
				if (client != null) {
					client.serverMessage("sector exporting failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		return false;
	}
}
