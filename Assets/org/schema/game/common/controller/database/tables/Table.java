package org.schema.game.common.controller.database.tables;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.common.controller.database.DatabaseEntry;
import org.schema.game.common.controller.database.SimDatabaseEntry;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public abstract class Table {

	public final String table;
	protected final Connection c;
	protected final TableManager m;
	private final List<Column> columns = new ObjectArrayList<Column>();
	private final List<Index> indices = new ObjectArrayList<Index>();
	private String[] primaryKeyCustom;
	private String fieldsString;

	protected Table(String table, TableManager m, Connection c) {
		this.table = table;
		this.c = c;
		this.m = m;

	}

	public static List<SimDatabaseEntry> resultToListSim(ResultSet r) throws SQLException {
		long t = System.currentTimeMillis();
		ObjectArrayList<SimDatabaseEntry> l = new ObjectArrayList<SimDatabaseEntry>();
		if(r.next()) {
			do {
				// Logic to retrieve the data from the resultset.
				l.add(new SimDatabaseEntry(r));
			} while(r.next());
		} else {

			// No data
		}

		if(!r.isClosed()) {
			r.close();
		}
		if((System.currentTimeMillis() - t) > 10) {
			System.err.println("[SQL] SECTOR QUERY LIST CONVERSION TOOK " + (System.currentTimeMillis() - t) + "ms; list size: " + l.size() + ";");
		}
		return l;
	}

	public static List<DatabaseEntry> resultToList(ResultSet r) throws SQLException {
		long t = System.currentTimeMillis();
		ObjectArrayList<DatabaseEntry> l = new ObjectArrayList<DatabaseEntry>();
		if(r.next()) {
			do {
				// Logic to retrieve the data from the resultset.
				l.add(new DatabaseEntry(r));
			} while(r.next());
		} else {

			// No data
		}

		if(!r.isClosed()) {
			r.close();
		}
		if((System.currentTimeMillis() - t) > 10) {
			System.err.println("[SQL] SECTOR QUERY LIST CONVERSION TOOK " + (System.currentTimeMillis() - t) + "ms; list size: " + l.size() + ";");
		}
		return l;
	}

	public void addColumn(String name, String type) {
		addColumn(name, type, false);
	}

	public void addColumn(String name, String type, boolean prim) {
		columns.add(new Column(name, type, prim));
	}

	public String createFieldsString() {
		StringBuffer b = new StringBuffer();
		for(int i = 0; i < columns.size(); i++) {
			b.append(columns.get(i).name);
			if(i < columns.size() - 1) {
				b.append(", ");
			}
		}
		return b.toString();

	}

	public void addIndex(String name, String... col) {
		addIndex(name, false, col);
	}

	public void addIndex(String name, boolean unique, String... col) {
		Index m = new Index(name, unique, col);
		indices.add(m);
	}

	public abstract void define();

	public abstract void afterCreation(Statement s) throws SQLException;

	public void migrate(Statement s) throws SQLException {
		createTable();
	}

	protected void setPrimaryKey(String... keys) {
		primaryKeyCustom = keys;
	}

	public boolean existsTable() throws SQLException {
		Statement s = c.createStatement();
		boolean exists = existsTable(s);
		s.close();
		return exists;
	}

	public boolean existsTable(Statement s) throws SQLException {

		ResultSet rx = s.executeQuery("SELECT * " +
				"FROM information_schema.COLUMNS " +
				"WHERE " +
				"TABLE_NAME = '" + table + "';");

		return rx.next();
	}

	public String getFieldsString() {
		assert (fieldsString != null) : table;
		return fieldsString;
	}

	public void createTable() throws SQLException {

		define();

		fieldsString = createFieldsString();

		Statement s = c.createStatement();

		if(!existsTable(s)) {
			try {
				createTable(s);
				createIndices(s);
			} catch(SQLException e) {
				throw new SQLException("Error in table '" + table + "'", e);
			}
		} else {
			for(int i = 0; i < columns.size(); i++) {

				//check nonexisting columns

				Column c = columns.get(i);

				ResultSet r = s.executeQuery("SELECT * " + //character_maximum_length
						"FROM information_schema.COLUMNS " +
						"WHERE " +
						"TABLE_NAME = '" + table + "' " +
						"AND COLUMN_NAME = '" + c.name.trim() + "';");
				if(!r.next()) {
					s.executeUpdate("ALTER TABLE '" + table + "' ADD COLUMN " + c.name.trim() + " " + c.type.trim() + ";");
				}
			}
			//update indices if necessary
			createIndices(s);
		}

		afterCreation(s);
		s.close();
	}

	private void createTable(Statement s) throws SQLException {

		StringBuffer a = new StringBuffer();
		Column prim = null;
		for(Column c : columns) {
			if(c.primaryKey) {
				if(prim == null) {
					prim = c;
				} else {
					throw new SQLException("Duplicate primary key: " + prim + "; " + c);
				}
			}
		}
		if(primaryKeyCustom == null && prim == null) {
			throw new SQLException("No primary key!");
		}
		for(int i = 0; i < columns.size(); i++) {
			Column c = columns.get(i);
			a.append(c.name.trim() + " " + c.type.trim());
			a.append(", ");
		}
		if(primaryKeyCustom != null) {
			StringBuffer b = new StringBuffer();
			for(int i = 0; i < primaryKeyCustom.length; i++) {
				b.append(primaryKeyCustom[i]);
				if(i < primaryKeyCustom.length - 1) {
					b.append(", ");
				}
			}
			a.append("primary key (" + b + ")");
		} else {
			a.append("primary key (" + prim.name + ")");
		}
		String create = "CREATE CACHED TABLE " + table + "(" + a + ");";
		try {
			s.execute(create);
		} catch(SQLException e) {
			throw new SQLException("On executing: " + create + " in " + table + "; ", e);
		}

	}

	//	private void createIndices(Statement s) throws SQLException{
//		for(int i = 0; i < indices.size(); i++){
//			Index x = indices.get(i);
//			String cIndex = "create "+(x.unique ? "unique " : "")+"index "+x.name+" on "+table+"("+x.getString()+");";
//			try{
//			s.execute(cIndex);
//			}catch(SQLException e){
//				throw new SQLException("Error on index: '"+cIndex+"' in "+table, e);
//			}
//		}
//	}
	private void createIndices(Statement s) throws SQLException {
		for(int i = 0; i < indices.size(); i++) {
			Index x = indices.get(i);
//			s.execute("DROP INDEX IF EXISTS "+x.name+";");

//			ResultSet r = s.executeQuery("select " +
//					"    t.relname as table_name, " +
//					"    i.relname as index_name, " +
//					"    a.attname as column_name " +
//					"from " +
//					"    pg_class t, " +
//					"    pg_class i, " +
//					"    pg_index ix, " +
//					"    pg_attribute a " +
//					"where " +
//					"    t.oid = ix.indrelid " +
//					"    and i.oid = ix.indexrelid " +
//					"    and a.attrelid = t.oid " +
//					"    and a.attnum = ANY(ix.indkey) " +
//					"    and t.relkind = 'r' " +
//					"    and i.relname like '"+x.name+"' " +
//					"order by " +
//					"    t.relname, " +
//					"    i.relname;");
//			if (!r.next()) {
			System.err.println("[DATABASE] creating nonexitent index in table: " + x.name);
			String cIndex = "create " + (x.unique ? "unique " : "") + "index " + x.name + " on " + table + "(" + x.getString() + ");";
			try {
				s.execute(cIndex);
			} catch(SQLException e) {
				if(e.getMessage() == null || !e.getMessage().contains("object name already exists")) {
					e.printStackTrace();
				}
//					throw new SQLException("Error on index: '"+cIndex+"' in "+table, e);
			}
//			}else {
////				System.err.println("[DATABASE] INDEX ON "+getName()+": "+x.name);
//			}

		}
	}

	public class Index {
		public String[] col;
		public boolean unique;
		private final String name;

		public Index(String name, boolean unique, String... col) {
			this.name = name;
			this.col = col;
			this.unique = unique;
		}

		public String getString() {
			StringBuffer a = new StringBuffer();

			for(int i = 0; i < col.length; i++) {
				a.append(col[i].trim());
				if(i < col.length - 1) {
					a.append(", ");
				}
			}
			return a.toString();
		}
	}

	public class Column {
		String name;
		String type;
		boolean primaryKey;

		public Column(String name, String type, boolean primaryKey) {
			this.name = name;
			this.type = type;
			this.primaryKey = primaryKey;
		}
	}
}
