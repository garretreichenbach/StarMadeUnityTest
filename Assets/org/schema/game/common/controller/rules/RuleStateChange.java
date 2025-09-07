package org.schema.game.common.controller.rules;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.SerializationInterface;
import org.schema.game.common.controller.rules.rules.Rule;

import it.unimi.dsi.fastutil.shorts.ShortArrayList;

public class RuleStateChange implements SerializationInterface{
	public enum RuleTriggerState{
		UNCHANGED,
		TRIGGERED,
		UNTRIGGERED
	}
	public int ruleIdNT = Integer.MIN_VALUE;
	public RuleStateChange() {
	}
	public RuleStateChange(Rule r, RuleStateChange c) {
		this.ruleIdNT = r.getRuleId();
		this.changeLogCond.addAll(c.changeLogCond);
		this.triggerState = c.triggerState;
	}
	
	public final ShortArrayList changeLogCond = new ShortArrayList(); 
	public RuleTriggerState triggerState = RuleTriggerState.UNCHANGED;
	public boolean lastSatisfied; //used for duration condition since it depends on last state
	public void clear() {
		changeLogCond.clear();
		triggerState = RuleTriggerState.UNCHANGED;
	}
	public boolean changed() {
		return !changeLogCond.isEmpty();
	}
	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		assert(ruleIdNT != Integer.MIN_VALUE);
		b.writeInt(ruleIdNT);
		b.writeShort(changeLogCond.size());
		for(short s : changeLogCond) {
			b.writeShort(s);
		}
		b.writeByte((byte)triggerState.ordinal());
	}
	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		ruleIdNT = b.readInt();
		int size = b.readShort();
		changeLogCond.ensureCapacity(size);
		for(int i = 0; i < size; i++) {
			changeLogCond.add(b.readShort());
		}
		triggerState = RuleTriggerState.values()[b.readByte()];
	}
}
