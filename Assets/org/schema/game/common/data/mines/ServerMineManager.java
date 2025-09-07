package org.schema.game.common.data.mines;

import java.sql.SQLException;
import java.util.List;

import javax.vecmath.Vector3f;

import org.schema.game.common.data.mines.MineActivityLevelContainer.ActiveLevel;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.Timer;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class ServerMineManager {
	
	private final Int2ObjectOpenHashMap<Mine> mines = new Int2ObjectOpenHashMap<Mine>();
	
	private final GameServerState state;
	private final Sector sector;
	

	private final MineActivityLevelContainer mineActiveLvl;
	public ServerMineManager(GameServerState state, Sector sector) {
		this.state = state;
		this.sector = sector;
		mineActiveLvl = new MineActivityLevelContainer();
	}

	public Int2ObjectOpenHashMap<Mine> getMines() {
		return mines;
	}

	public void updateActiveLevel(Mine mine, ActiveLevel oldLevel, ActiveLevel newLevel) {
		mineActiveLvl.updateActiveLevel(mine, oldLevel, newLevel);
	}

	public void updateLocal(Timer timer) {
		mineActiveLvl.updateLocal(timer, state.getController().getMineController());
	}

	public void onSectorEntitesChanged() {
		mineActiveLvl.onSectorChange(sector.getEntities(), mines.values());
	}

	public boolean contains(Mine m) {
		return mines.containsKey(m.getId());
	}

	public void loadServer() {
		try {
			state.getDatabaseIndex().getTableManager().getMinesTable().loadMines(state, sector, mines);
			for(Mine mine : mines.values()) {
				if(sector.getEntities().isEmpty()) {
					mine.setActiveLevel(ActiveLevel.INACTIVE);
				}else {
					mine.setActiveLevel(ActiveLevel.ACTIVE);
				}
				mineActiveLvl.add(mine);
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void unloadServer() {
		mineActiveLvl.clearActivity();
		
		for(Mine mine : mines.values()) {
			try {
				//will be skipped if mine unchanged
				state.getDatabaseIndex().getTableManager().getMinesTable().updateOrInsertMine(mine);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		mines.clear();
	}

	public int getSectorId() {
		return sector.getId();
	}

	public void addMine(Mine mine) {
		mine.setChanged(true);
		if(sector.getEntities().isEmpty()) {
			mine.setActiveLevel(ActiveLevel.INACTIVE);
		}else {
			mine.setActiveLevel(ActiveLevel.ACTIVE);
		}
		mines.put(mine.getId(), mine);
		mineActiveLvl.add(mine);
		try {
			state.getDatabaseIndex().getTableManager().getMinesTable().updateOrInsertMine(mine);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	public void removeMine(Mine mine) {
		mines.remove(mine.getId());
		mineActiveLvl.remove(mine);
		
		try {
			state.getDatabaseIndex().getTableManager().getMinesTable().deleteMine(mine.getId());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void clearMines() {
		mines.clear();
		mineActiveLvl.clear();
	}

	public Mine removeMine(int mineId) {
		Mine mine = mines.get(mineId);
		if(mine != null) {
			removeMine(mine);
		}else {
			System.err.println("[SERVER][MINE][WARNING] MINE ID TO REMOVE NOT FOUND "+mineId);
		}
		return mine;
	}

	public void getMinesInRange(SimpleTransformableSendableObject<?> from, float distance, List<Mine> out) {
		for(Mine m : mines.values()) {
			if(m.getDistanceTo(from) <= distance) {
				out.add(m);
			}
		}
	}

	public void getMinesInRange(int sectorFrom, Vector3f from, float distance, List<Mine> out) {
		for(Mine m : mines.values()) {
			if(m.getDistanceTo(sectorFrom, from) <= distance) {
				out.add(m);
			}
		}
	}
}
