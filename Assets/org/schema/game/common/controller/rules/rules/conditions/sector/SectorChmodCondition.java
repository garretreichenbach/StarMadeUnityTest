package org.schema.game.common.controller.rules.rules.conditions.sector;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Set;

import org.schema.common.util.StringTools;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.RuleValue;

import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.Sector.SectorMode;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.schine.common.language.Lng;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class SectorChmodCondition extends SectorCondition{

	@RuleValue(tag = "NoAttack")
	public boolean noAttack;
	
	@RuleValue(tag = "NoSpawn")
	public boolean noSpawn;
	
	@RuleValue(tag = "NoIndicators")
	public boolean noIndicators;
	
	@RuleValue(tag = "NoEntry")
	public boolean noEntry;
	
	@RuleValue(tag = "NoExit")
	public boolean noExit;
	
	
	@RuleValue(tag = "NoFPLoss")
	public boolean noFPLoss;
	
	
	
	public SectorChmodCondition() {
		super();
	}

	@Override
	public long getTrigger() {
		return TRIGGER_ON_SECTOR_CHMOD;
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SECTOR_CHMOD;
	}

	@Override
	protected boolean processCondition(short conditionIndex, RuleStateChange stateChange, RemoteSector a, long trigger, boolean forceTrue) {
		if(forceTrue) {
			return true;
		}
		int mode = 0;
		if(a.isOnServer()) {
			mode = a.getServerSector().getProtectionMode();
		}else {
			mode = a.getSectorModeClient();
		}
		
		int mask = getMask();
		return (mode & mask) == mask;
	}
	public int getMask() {
		int mask = 0;
		mask |= noAttack ? SectorMode.PROT_NO_ATTACK.code : 0;
		mask |= noSpawn ? SectorMode.PROT_NO_SPAWN.code : 0;
		mask |= noIndicators ? SectorMode.NO_INDICATIONS.code : 0;
		mask |= noEntry ? SectorMode.LOCK_NO_ENTER.code : 0;
		mask |= noExit ? SectorMode.LOCK_NO_EXIT.code : 0;
		mask |= noFPLoss ? SectorMode.NO_FP_LOSS.code : 0;
		return mask;
	}
	
	@Override
	public String getDescriptionShort() {
		int mask = getMask();
		return Sector.getPermissionString(mask);
	}
}
