package org.schema.game.common.controller.rules.rules;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.schema.common.SerializationInterface;
import org.schema.common.XMLSerializationInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleSet;
import org.schema.game.common.controller.rules.RuleSetManager;
import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.RuleStateChange.RuleTriggerState;
import org.schema.game.common.controller.rules.rules.actions.Action;
import org.schema.game.common.controller.rules.rules.actions.ActionTypes;
import org.schema.game.common.controller.rules.rules.conditions.Condition;
import org.schema.game.common.controller.rules.rules.conditions.ConditionList;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.controller.rules.rules.conditions.faction.FactionCondition;
import org.schema.game.common.controller.rules.rules.conditions.player.PlayerCondition;
import org.schema.game.common.controller.rules.rules.conditions.sector.SectorCondition;
import org.schema.game.common.controller.rules.rules.conditions.seg.SegmentControllerCondition;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.game.common.data.world.RuleEntityContainer;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.TopLevelType;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.network.server.ServerMessage;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class Rule implements SerializationInterface, XMLSerializationInterface {
	
	private int ruleId;
	private static byte VERSION = 0;
	private String uniqueIdentifier;
	
	protected final ConditionList conditions = new ConditionList();
	protected final List<Action<?>> actions = new ObjectArrayList<Action<?>>();
	public boolean allTrue;
	private boolean triggered;
	private static int ruleIdGen;
	
	
	public TopLevelType ruleType = TopLevelType.SEGMENT_CONTROLLER;

	public Rule(boolean createId) {
		if(createId) {
			ruleId = ++ruleIdGen;
		}
	}
	public Rule duplicate(String uid, boolean createId) throws IOException {
		
		Rule c = new Rule(createId);
		
		int id = c.ruleId;
		FastByteArrayOutputStream fbo = new FastByteArrayOutputStream(10*1024);
		DataOutputStream sb = new DataOutputStream(fbo);
		serialize(sb, true);
		
		DataInputStream in = new DataInputStream(new FastByteArrayInputStream(fbo.array, 0, (int)fbo.position()));
		c.deserialize(in, 0, true);
		
		c.uniqueIdentifier = uid;
		c.ruleId = id;
		return c;
	}

	@Override
	public void parseXML(Node node) {
		
		if(node.getAttributes().getNamedItem("version") == null) {
			throw new RuleParserException("missing version attribute on rule");
		}
		if(node.getAttributes().getNamedItem("id") == null) {
			throw new RuleParserException("missing id attribute on rule");
		}
		if(node.getAttributes().getNamedItem("alltrue") == null) {
			throw new RuleParserException("missing alltrue attribute on rule");
		}
		if(node.getAttributes().getNamedItem("type") != null) {
			ruleType = TopLevelType.values()[Integer.parseInt(node.getAttributes().getNamedItem("type").getNodeValue())];
		}

		final byte version = Byte.parseByte(node.getAttributes().getNamedItem("version").getNodeValue());

		uniqueIdentifier = node.getAttributes().getNamedItem("id").getNodeValue();
		
		allTrue = Boolean.parseBoolean(node.getAttributes().getNamedItem("alltrue").getNodeValue());
		
		
		conditions.clear();
		actions.clear();
		NodeList childNodes = node.getChildNodes();
		for(int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			
			if(item.getNodeType() == Node.ELEMENT_NODE) {
				if(item.getNodeName().toLowerCase(Locale.ENGLISH).equals("conditions")) {
					NodeList cl = item.getChildNodes();
					for(int c = 0; c < cl.getLength(); c++) {
						Node m = cl.item(c);
						if(m.getNodeType() == Node.ELEMENT_NODE) {
							Node typeN = m.getAttributes().getNamedItem("type");
							if(typeN != null) {
								int type = Integer.parseInt(typeN.getNodeValue());
								Condition<?> con = ConditionTypes.getByUID(type).fac.instantiateCondition();
								con.parseXML(m);
								conditions.add(con);
								if(ruleType == null) {
									ruleType = con.getType().getType() ;
								}
							}else {
								throw new RuleParserException("No type on condition node ");
							}
						}
					}
				}
				if(item.getNodeName().toLowerCase(Locale.ENGLISH).equals("actions")) {
					NodeList cl = item.getChildNodes();
					for(int c = 0; c < cl.getLength(); c++) {
						Node m = cl.item(c);
						if(m.getNodeType() == Node.ELEMENT_NODE) {
							Node typeN = m.getAttributes().getNamedItem("type");
							if(typeN != null) {
								int type = Integer.parseInt(typeN.getNodeValue());
								Action<?> con = ActionTypes.getByUID(type).fac.instantiateAction();
								con.parseXML(m);
								actions.add(con);
								if(ruleType == null) {
									ruleType = con.getType().getType() ;
								}
							}else {
								throw new RuleParserException("No type on action node ");
							}
						}
					}
				}
			}
		}
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uniqueIdentifier == null) ? 0 : uniqueIdentifier.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Rule)) {
			return false;
		}
		Rule other = (Rule) obj;
		if (uniqueIdentifier == null) {
			if (other.uniqueIdentifier != null) {
				return false;
			}
		} else if (!uniqueIdentifier.equals(other.uniqueIdentifier)) {
			return false;
		}
		return true;
	}
	@Override
	public Node writeXML(Document doc, Node parent) {
		Node root = doc.createElement("Rule");
		
		Attr vAtt = doc.createAttribute("version");
		vAtt.setValue(String.valueOf(VERSION));
		root.getAttributes().setNamedItem(vAtt);
		
		Attr idAtt = doc.createAttribute("id");
		idAtt.setValue(uniqueIdentifier);
		root.getAttributes().setNamedItem(idAtt);
		
		Attr atAtt = doc.createAttribute("alltrue");
		atAtt.setValue(String.valueOf(allTrue));
		root.getAttributes().setNamedItem(atAtt);
		
		Attr tpy = doc.createAttribute("type");
		tpy.setValue(String.valueOf(ruleType.ordinal()));
		root.getAttributes().setNamedItem(tpy);


		
		Element condNode = doc.createElement("Conditions");
		root.appendChild(condNode);
		for(Condition<?> c : conditions) {
			Node n = c.writeXML(doc, root);
			condNode.appendChild(n);
		}
		
		Element actNode = doc.createElement("Actions");
		root.appendChild(actNode);
		for(Action<?> a : actions) {
			Node n = a.writeXML(doc, root);
			actNode.appendChild(n);
		}
		
		
		
		return root;
	}


	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeByte(VERSION);
		b.writeInt(ruleId);
		b.writeUTF(uniqueIdentifier);
		
		b.writeByte((byte)ruleType.ordinal());

		b.writeBoolean(allTrue);
		b.writeInt(conditions.size());
		for(Condition<?> c : conditions) {
			c.serialize(b, isOnServer);
		}
		
		b.writeInt(actions.size());
		for(Action<?> a : actions) {
			a.serialize(b, isOnServer);
		}
	}


	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		//type already read at this point
		final byte version = b.readByte();
		ruleId = b.readInt();
		uniqueIdentifier = b.readUTF();
		
		ruleType = TopLevelType.values()[b.readByte()];

		allTrue = b.readBoolean();
		final int condSize = b.readInt();
		conditions.clear();
		for(int i = 0; i < condSize; i++) {
			final int conditionType = b.readByte();
			Condition<?> c = (Condition<?>)ConditionTypes.getByUID(conditionType).fac.instantiateCondition();
			c.deserialize(b, updateSenderStateId, isOnServer);
			conditions.add(c);
		}
		
		
		final int actSize = b.readInt();
		actions.clear();
		for(int i = 0; i < actSize; i++) {
			final int actionType = b.readByte();
			Action<?> a = (Action<?>)ActionTypes.getByUID(actionType).fac.instantiateAction();
			a.deserialize(b, updateSenderStateId, isOnServer);
			actions.add(a);
		}
		
	}


	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}


	public int getRuleId() {
		return ruleId;
	}


	public void checkUID(RuleSetManager man) {
		String uid = uniqueIdentifier;
		int i = 0;
		while(man.ruleUIDlkMap.containsKey(uid.toLowerCase(Locale.ENGLISH))){
			uid = uniqueIdentifier +"_"+i;
			i++;
		}
		uniqueIdentifier = uid;
	}


	public void assignNewId() {
		ruleId = ruleIdGen++;
	}


	public void removeCondition(Condition<?> selectedCondidion) {
		conditions.remove(selectedCondidion);
	}


	public void addCondition(Condition<?> c) {
		if((actions.isEmpty() && conditions.isEmpty()) || getEntityType() == c.getEntityType()) {
			conditions.add(c);
		}else {
			throw new RuntimeException("Incompatible type "+c.getEntityType().name()+"; Rule: "+getEntityType().name());
		}
	}


	public void removeAction(Action<?> a) {
		actions.remove(a);
	}
	public void addAction(Action<?> a) {
		if((actions.isEmpty() && conditions.isEmpty()) || getEntityType() == a.getEntityType()) {
			actions.add(a);
		}else {
			throw new RuntimeException("Incompatible type "+a.getEntityType().name()+"; Rule: "+getEntityType().name());
		}
	}


	public int getConditionCount() {
		return conditions.size();
	}


	public int getActionCount() {
		return actions.size();
	}


	public List<Action<?>> getActions() {
		return actions;
	}
	public ConditionList getConditions() {
		return conditions;
	}


	public void setUniqueIdentifier(String uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
	}
	public TopLevelType getEntityType() {
		TopLevelType t = null;
		for(Condition<?> c : conditions) {
			return c.getType().getType();
		}
		for(Action<?> c : actions) {
			return c.getType().getType();
		}
		return TopLevelType.GENERAL;
	}
	private final RuleStateChange stateChange = new RuleStateChange();
	public boolean ignoreRule; 
	public boolean checkIgnored;
	public String ruleSetUID = "undefined"; 
	
	
	private void sendReset(RuleEntityContainer s, TopLevelType topLevelType) {
		stateChange.clear();
		resetConditions(false);
		if(triggered) {
			onTrigger(s, topLevelType, false);
			triggered = false;
			
			stateChange.triggerState = RuleTriggerState.UNTRIGGERED;
		}
		createCompleteConditionState(stateChange);
		s.getRuleEntityManager().sendRuleStateChange(this, stateChange);
		stateChange.clear();
	}
	public void process(RuleEntityContainer s, TopLevelType topLevelType, long trigger) {
		if(topLevelType != ruleType) {
			return;
		}
		if(ignoreRule) {
			System.err.println("NOT CHECKING RULE "+uniqueIdentifier+"; Ignored!");
			if(!checkIgnored) {
				//send over a full reset
				sendReset(s, topLevelType);
				checkIgnored = true;
			}
			return;
		}
		checkIgnored = false;
		boolean conditionChanged = false;
		for(short i = 0; i < conditions.size(); i++) {
			Condition<?> c = conditions.get(i);
		switch(topLevelType) {
			case FACTION:
			{
				Faction seg = (Faction)s;
				FactionCondition sc = ((FactionCondition)c);
				boolean triggeredOn = sc.isTriggeredOn(trigger);
				if(triggeredOn) {
					boolean lastCheck = c.isSatisfied();
					stateChange.lastSatisfied = lastCheck;
					boolean nowCheck = sc.checkSatisfied(i, stateChange, seg, trigger, false);
					if(lastCheck != nowCheck) {
						conditionChanged = true;
					}
				}
			}
				break;
			case SECTOR:
			{
				RemoteSector seg = (RemoteSector)s;
				SectorCondition sc = ((SectorCondition)c);
				boolean triggeredOn = sc.isTriggeredOn(trigger);
				if(triggeredOn) {
					boolean lastCheck = c.isSatisfied();
					stateChange.lastSatisfied = lastCheck;
					boolean nowCheck = sc.checkSatisfied(i, stateChange, seg, trigger, false);
					if(lastCheck != nowCheck) {
						conditionChanged = true;
					}
				}
			}
				break;
			case PLAYER:
			{
				PlayerState seg = (PlayerState)s;
				PlayerCondition sc = ((PlayerCondition)c);
				boolean triggeredOn = sc.isTriggeredOn(trigger);
				if(triggeredOn) {
					boolean lastCheck = c.isSatisfied();
					stateChange.lastSatisfied = lastCheck;
					boolean nowCheck = sc.checkSatisfied(i, stateChange, seg, trigger, false);
					if(lastCheck != nowCheck) {
						conditionChanged = true;
					}
				}
			}
				break;
			case SEGMENT_CONTROLLER:
			{
					SegmentController seg = (SegmentController)s;
					SegmentControllerCondition sc = ((SegmentControllerCondition)c);
					boolean triggeredOn = sc.isTriggeredOn(trigger);
					if(triggeredOn) {
						boolean lastCheck = c.isSatisfied();
						stateChange.lastSatisfied = lastCheck;
						boolean nowCheck = sc.checkSatisfied(i, stateChange, seg, trigger, false);
						if(lastCheck != nowCheck) {
							conditionChanged = true;
						}
					}
			}
				break;
			default:
				assert(false);
				break;

			}
		}
		
		if(conditionChanged) {
			try {
				boolean wasTriggered = triggered;

				triggered = checkRuleTriggered();
				stateChange.triggerState = triggered ? RuleTriggerState.TRIGGERED : RuleTriggerState.UNTRIGGERED;
				if(wasTriggered != triggered) {
					onTrigger(s, topLevelType, triggered);
				}

				s.getRuleEntityManager().triggerRuleStateChanged();
			}catch(RuntimeException e) {
				e.printStackTrace();
				((GameServerState)s.getRuleEntityManager().entity.getState()).getController().broadcastMessageAdmin(Lng.astr("An Critical Error Happened executing actions of rule %s. Check the logs for the error message", uniqueIdentifier), ServerMessage.MESSAGE_TYPE_ERROR);
			}
		}
		
		if(stateChange.changed()) {
			s.getRuleEntityManager().sendRuleStateChange(this, stateChange);
		}
		stateChange.clear();
	}
	public void createCompleteConditionState(RuleStateChange stateChange) {
		for(short i = 0; i < conditions.size(); i++) {
			Condition<?> c = conditions.get(i);
			c.createStateChange(i, stateChange);
		}
		
	}
	private void resetConditions(boolean b) {
		for(Condition<?> c : conditions) {
			c.resetCondition(b);
		}
	}
	private void onTrigger(RuleEntityContainer s, TopLevelType topLevelType, boolean triggered) {
		if(triggered) {
			for(Action<?> a : actions) {
				
				a.onTrigger(s, topLevelType);
			}
		}else {
			for(Action<?> a : actions) {
				a.onUntrigger(s, topLevelType);
			}
		}
	}
	private boolean checkRuleTriggered() {
		
		if(allTrue) {
			boolean ok = true;
			for(Condition<?> c : conditions) {
				if(!c.isSatisfied()) {
					return false;
				}
			}
			return true;
		}else {
			for(Condition<?> c : conditions) {
				if(c.isSatisfied()) {
					return true;
				}
			}
			return false;
		}
	}
	public boolean receiveState(RuleEntityContainer s, TopLevelType topLevelType, RuleStateChange r) {
		boolean changed = false;
		try {
			for(int i = 0; i < r.changeLogCond.size(); i++) {

				short index = r.changeLogCond.get(i);

				//indices are increased by 1 to avoid having 0 since they also carry their activation state in being pos/neg
				Condition<?> condition = conditions.get(Math.abs(index)-1);
				boolean satisfiedBef = condition.isSatisfied();
				i = condition.processReceivedState(i, r.changeLogCond, index);
				if(condition.isSatisfied() != satisfiedBef) {
					changed = true;
				}
			}

			if(r.triggerState != RuleTriggerState.UNCHANGED) {
				triggered = r.triggerState == RuleTriggerState.TRIGGERED;
				onTrigger(s, topLevelType, triggered);
			}
		}catch(Exception e) {
			e.printStackTrace();
			System.err.println("ERROR WHEN RECEIVING INCREMENTAL RULE STATE: Requesting full update for this rule");
			requestRuleState(s);
		}
		return changed;
	}
	public void sendCompleteRuleState(RuleEntityContainer s) {
		stateChange.clear();
		createCompleteConditionState(stateChange);
		if(triggered) {
			stateChange.triggerState = RuleTriggerState.TRIGGERED;
		}
		s.getRuleEntityManager().sendRuleStateChange(this, stateChange);
		stateChange.clear();
	}
	public void requestRuleState(RuleEntityContainer s) {
		assert(!s.isOnServer()):"Can't request on server (We are Authority)";
		s.getNetworkObject().getRuleStateRequestBuffer().add(ruleId);
	}
	public void getAllConditions(ConditionList all) {
		for(Condition<?> c : conditions) {
			c.addToList(all);
		}
	}
	public String getRuleSetUID() {
		return ruleSetUID;
	}
	public void setRuleSet(RuleSet s) {
		this.ruleSetUID = s.uniqueIdentifier;
	}

}
