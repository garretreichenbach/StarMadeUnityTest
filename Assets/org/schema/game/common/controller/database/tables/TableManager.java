package org.schema.game.common.controller.database.tables;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

public class TableManager {
	private final Connection c;
	private final SystemTable systemTable;
	private final EntityTable entityTable;
	private final FTLTable FTLTable;
	private final Map<String, Table> tables = new Object2ObjectOpenHashMap<String, Table>();
	private final List<Table> tableList = new ObjectArrayList<Table>();

	private final SectorItemTable sectorItemTable;
	private final SectorTable sectorTable;
	private final TradeHistoryTable tradeHistoryTable;
	private final TradeNodeTable tradeNodeTable;
	private final VisibilityTable visibilityTable;
	private final PlayerTable playerTable;
	private final FleetTable fleetTable;

	private final FleetMemberTable fleetMemberTable;
	private final EntityEffectTable entityEffectTable;
	private final NPCStatTable npcStatTable;
	private final PlayerMessagesTable playerMessagesTable;
	private final MinesTable minesTable;
	private final IdGenTable idGenTable;

	public TableManager(Connection c) {
		this.c = c;
		addTable(idGenTable = new IdGenTable(this, c));
		addTable(playerTable = new PlayerTable(this, c));
		addTable(systemTable = new SystemTable(this, c));
		addTable(sectorTable = new SectorTable(this, c));
		addTable(sectorItemTable = new SectorItemTable(this, c));
		addTable(entityTable = new EntityTable(this, c));
		addTable(entityEffectTable = new EntityEffectTable(this, c));
		addTable(FTLTable = new FTLTable(this, c));
		addTable(visibilityTable = new VisibilityTable(this, c));
		addTable(fleetTable = new FleetTable(this, c));
		addTable(fleetMemberTable = new FleetMemberTable(this, c));
		addTable(tradeNodeTable = new TradeNodeTable(this, c));
		addTable(tradeHistoryTable = new TradeHistoryTable(this, c));
		addTable(npcStatTable = new NPCStatTable(this, c));
		addTable(playerMessagesTable = new PlayerMessagesTable(this, c));
		addTable(minesTable = new MinesTable(this, c));

		((ObjectArrayList<?>) tableList).trim();

	}

	private void addTable(Table t) {
		tables.put(t.table, t);
		tableList.add(t);
	}

	public void create() throws SQLException {
		for(Table t : tableList) {
			System.err.println("[DATABASE] Creating table: " + t.table);
			t.createTable();
			assert (t.existsTable()) : t.table;
		}
	}

	public SystemTable getSystemTable() {
		return systemTable;
	}

	public EntityTable getEntityTable() {
		return entityTable;
	}

	public FTLTable getFTLTable() {
		return FTLTable;
	}

	public SectorItemTable getSectorItemTable() {
		return sectorItemTable;
	}

	public SectorTable getSectorTable() {
		return sectorTable;
	}

	public TradeHistoryTable getTradeHistoryTable() {
		return tradeHistoryTable;
	}

	public TradeNodeTable getTradeNodeTable() {
		return tradeNodeTable;
	}

	public VisibilityTable getVisibilityTable() {
		return visibilityTable;
	}

	public PlayerTable getPlayerTable() {
		return playerTable;
	}

	public FleetTable getFleetTable() {
		return fleetTable;
	}

	public FleetMemberTable getFleetMemberTable() {
		return fleetMemberTable;
	}

	public EntityEffectTable getEntityEffectTable() {
		return entityEffectTable;
	}

	public NPCStatTable getNpcStatTable() {
		return npcStatTable;
	}

	public PlayerMessagesTable getPlayerMessagesTable() {
		return playerMessagesTable;
	}

	public void migrate() throws SQLException {
		for(Table t : tableList) {
			Statement s = c.createStatement();
			if(!t.existsTable()) {
				t.createTable();
			}
			t.migrate(s);
			s.close();
		}
	}

	public MinesTable getMinesTable() {
		return minesTable;
	}

	public IdGenTable getIdGenTable() {
		return idGenTable;
	}

}
