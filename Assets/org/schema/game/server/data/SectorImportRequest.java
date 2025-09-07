package org.schema.game.server.data;

import java.io.IOException;
import java.sql.SQLException;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.server.controller.SectorUtil;
import org.schema.schine.network.RegisteredClientInterface;

public class SectorImportRequest implements ServerExecutionJob {
	public Vector3i pos;
	public RegisteredClientInterface client;
	private String name;

	public SectorImportRequest(Vector3i pos,
	                           RegisteredClientInterface clien, String name) {
		super();
		this.pos = pos;
		this.client = clien;
		this.name = name;
	}

	;

	@Override
	public boolean execute(GameServerState state) {
		try {
			SectorUtil.importSector(name, pos, state);
			if (client != null) {
				client.serverMessage("sector " + pos + " importing successful from " + name);
			}
			return true;
		} catch (SQLException e) {
			try {
				if (client != null) {
					client.serverMessage("sector importing failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		} catch (IOException e) {
			try {
				if (client != null) {
					client.serverMessage("sector importing failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		return false;
	}
}
