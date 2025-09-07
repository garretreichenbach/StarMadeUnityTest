package org.schema.game.common.controller.database;

public interface DatabaseInsertable {
	boolean hasChangedForDb();

	void setChangedForDb(boolean changed);
}
