package org.schema.game.server.data.simulation.groups;

import java.io.IOException;
import java.sql.SQLException;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.controller.EntityAlreadyExistsException;
import org.schema.game.server.controller.EntityNotFountException;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.blueprint.ChildStats;
import org.schema.game.server.data.blueprint.SegmentControllerOutline;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;

import com.bulletphysics.linearmath.Transform;

public abstract class ShipSimulationGroup extends SimulationGroup {

	/**
	 *
	 */
	

	public ShipSimulationGroup(GameServerState state) {
		super(state);
	}

	public void createFromBlueprints(Vector3i startSector, long uid, int factionId, CatalogPermission... catalogEntries) {

		this.setStartSector(startSector);
		int line = 0;
		int i = 0;
		for (CatalogPermission catalogName : catalogEntries) {

			try {
				Transform where = new Transform();
				where.setIdentity();

				line += 16 * 10;
				int row = (16 * 10) * (line / (16 * 10 * 10));
				int col = (line % (16 * 10 * 10));
				where.origin.set(0,
						row - getState().getSectorSize() / 2,
						col - getState().getSectorSize() / 2);

				int creatorThreadId = -1;
				String name = "MOB_SIM_" + catalogName.getUid() + "_" + uid + "_" + i;

				//				String name = "MOB_"+catalogName.catUID+"_"+System.currentTimeMillis()+"_"+i;
				int len = catalogName.getUid().length() - 1;
				while (name.length() > 64) {
					System.err.println("[SERVER] WARNING: SIM MOB NAME LENGTH TOO LONG: " + name + " -> " + name.length() + "/64");
					name = "MOB_SIM_" + catalogName.getUid().substring(0, len) + "_" + uid + "_" + i;
					len--;
				}
				getState().getController().despawn(name);
				SegmentPiece toDockOn = null; //this is for spawning turrets
				SegmentControllerOutline loadBluePrint = BluePrintController.active.loadBluePrint(
						getState(),
						catalogName.getUid(),
						name,
						where,
						-1,
						factionId,
						new Vector3i(), //no starting sector it will spawn into a sector later
						"<simulation>",
						PlayerState.buffer, toDockOn, true, new ChildStats(true));
				loadBluePrint.realName = catalogName.getUid() + " " + uid + "-" + i;
				if (!getState().getUniverse().isSectorLoaded(startSector)) {
					System.err.println("Spawning in database: " + name + " of blueprint: " + catalogName.getUid());
					loadBluePrint.spawnInDatabase(startSector, getState(), creatorThreadId, getMembers(), new ChildStats(true), false);
					//					getMembers().add(EntityType.SHIP.dbPrefix+uid);
				}
				System.err.println("[SIM] Spawning UID: "+name+"; "+loadBluePrint.uniqueIdentifier);
			} catch (EntityNotFountException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (EntityAlreadyExistsException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (StateParameterNotFoundException e) {
				e.printStackTrace();
			}

			i++;

		}

	}

}
