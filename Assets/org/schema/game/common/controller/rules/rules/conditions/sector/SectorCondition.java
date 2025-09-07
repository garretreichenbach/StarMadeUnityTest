package org.schema.game.common.controller.rules.rules.conditions.sector;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleSet;
import org.schema.game.common.controller.rules.RuleSetManager;
import org.schema.game.common.controller.rules.rules.Rule;

import org.schema.game.common.controller.rules.rules.conditions.Condition;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.schine.network.TopLevelType;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class SectorCondition extends Condition<RemoteSector> {
	
	
	
	public static final long TRIGGER_ON_SECTOR_CHMOD = 2048L;
//	public static final long TRIGGER_ON_REMOVE_BLOCK = 4096L;
//	public static final long TRIGGER_ON_COLLECTION_UPDATE = 8192L;
//	public static final long TRIGGER_ON_MASS_UPDATE = 16384L;
//	public static final long TRIGGER_ON_BB_UPDATE = 32768L;
//	public static final long TRIGGER_ON_REACTOR_ACTIVITY_CHANGE = 65536L;
//	public static final long TRIGGER_ON_DOCKING_CHANGED = 131072L;
//	public static final long TRIGGER_ON_ADMIN_FLAG_CHANGED = 262144L;
//	public static final long TRIGGER_ON_SECTOR_SWITCHED = 262144L;
//	public static final long TRIGGER_ON_ANY_SECTOR_SWITCHED = 1048576L;
//	public static final long TRIGGER_ON_FLEET_CHANGE = 2097152L;
//	public static final long TRIGGER_ON_HOMEBASE_CHANGE = 4194304L;
	
	public SectorCondition() {
		super();
	}
	
	
	
	
	
	public boolean isTriggeredOnSectorChmod() {
		return isTriggeredOn(TRIGGER_ON_SECTOR_CHMOD);
	}

}
