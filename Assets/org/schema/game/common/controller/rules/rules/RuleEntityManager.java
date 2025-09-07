package org.schema.game.common.controller.rules.rules;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.rules.RuleSet;
import org.schema.game.common.controller.rules.RuleSetManager;
import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.actions.ActionUpdate;
import org.schema.game.common.controller.rules.rules.actions.RecurringAction;
import org.schema.game.common.controller.rules.rules.actions.player.PlayerRecurringAction;
import org.schema.game.common.controller.rules.rules.conditions.Condition;
import org.schema.game.common.controller.rules.rules.conditions.TimedCondition;
import org.schema.game.common.controller.rules.rules.conditions.seg.SegmentControllerCondition;
import org.schema.game.common.data.world.RuleEntityContainer;
import org.schema.game.network.objects.NTRuleInterface;
import org.schema.game.network.objects.remote.RemoteRuleStateChange;
import org.schema.game.network.objects.remote.RemoteRuleStateChangeBuffer;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIObservable;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.remote.RemoteBuffer;
import org.schema.schine.network.objects.remote.RemoteIntBuffer;
import org.schema.schine.network.objects.remote.RemoteString;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

public abstract class RuleEntityManager<E extends RuleEntityContainer> extends GUIObservable{

	
	private static final String ADD_INDIVIDUAL = "ADDIN_";
	private static final String REMOVE_INDIVIDUAL = "DELIN_";
	private static final String ADD_IGNORE = "ADDIG_";
	private static final String REMOVE_IGNORE = "DELIG_";

	public final E entity;

	private final List<RecurringAction<E>> recurringActions = new ObjectArrayList<RecurringAction<E>>();

	private final List<ActionUpdate> updates = new ObjectArrayList<ActionUpdate>();

	public final Int2ObjectOpenHashMap<Rule> ruleIdMap = new Int2ObjectOpenHashMap<Rule>();

	/**
	 * contains rules that are not for this entity type.
	 * They are kept for now to make sure network updates are correct (in the case the rule id isn't found in ruleIdMap)
	 */
	public final Int2ObjectOpenHashMap<Rule> ruleIdDifferentTypeDebugMap = new Int2ObjectOpenHashMap<Rule>();
	public final ObjectArrayList<Rule> rulesActiveTotal = new ObjectArrayList<Rule>();
	public final ObjectArrayList<Rule> rulesTotal = new ObjectArrayList<Rule>();
	public final ObjectArrayList<RuleSet> globalRules = new ObjectArrayList<RuleSet>();
	public final ObjectArrayList<RuleSet> individualRules = new ObjectArrayList<RuleSet>();

	private final ObjectArrayFIFOQueue<RuleStateChange> receivedRuleStates = new ObjectArrayFIFOQueue<RuleStateChange>();
	private final ObjectArrayFIFOQueue<String> receivedRuleIndividualRequests = new ObjectArrayFIFOQueue<String>();
	private final IntArrayFIFOQueue receivedRuleStateRequests = new IntArrayFIFOQueue();
	private final Object2ObjectOpenHashMap<String, String> individualRulesUIDMap = new Object2ObjectOpenHashMap<String, String>();
	private final Object2ObjectOpenHashMap<String, String> individualGlobalRuleExceptionUIDMap = new Object2ObjectOpenHashMap<String, String>();
	private final ObjectArrayList<TimedCondition> timedConditions = new ObjectArrayList<TimedCondition>();
	private long trigger = 0;
	private boolean flagRuleChanged = true;
	private boolean ruleChangePrepare;

	public List<Rule> getActiveRules(){
		return rulesActiveTotal;
	}
	public void triggerRuleStateChanged() {
		trigger(Condition.TRIGGER_ON_RULE_STATE_CHANGE);
	}
	public void triggerTimedCondition() {
		trigger(Condition.TRIGGER_ON_TIMED_CONDITION);
	}
	public void triggerOnAIActivityChange() {
		trigger(Condition.TRIGGER_ON_AI_ACTIVE_CHANGE);
	}
	public void triggerOnFactionChange() {
		trigger(Condition.TRIGGER_ON_FACTION_CHANGE);
	}
	public void triggerOnAttack() {
		trigger(Condition.TRIGGER_ON_ATTACK);
	}
	public void trigger(long trigger) {
		assert(isOnServer());
		this.trigger |= trigger;
	}
	public void addDurationCheck(TimedCondition timedCondition) {
		timedConditions.add(timedCondition);
	}

	public void removeDurationCheck(TimedCondition timedCondition) {
		timedConditions.remove(timedCondition);
	}
	public void onRulesChanged() throws IOException {
		updates.clear();
		rulesActiveTotal.clear();
		rulesTotal.clear();
		globalRules.clear();
		individualRules.clear();
		receivedRuleStates.clear();
		ruleIdMap.clear();
		ruleIdDifferentTypeDebugMap.clear();

		List<RuleSet> tmp = ((GameStateInterface)entity.getState()).getGameState().getRuleManager().getGlobalRules(getEntitySubType(), new ObjectArrayList<RuleSet>());
//		System.err.println(getState()+"; "+entity+" ON RULE CHANGED: "+tmp);
		for(RuleSet rll : tmp) {
			//duplicate global rules into entity
			RuleSet g = new RuleSet(rll, rll.getUniqueIdentifier());
			globalRules.add(g);
			
			if(individualGlobalRuleExceptionUIDMap.containsKey(rll.getUniqueIdentifier().toLowerCase(Locale.ENGLISH))) {
				for(Rule r : g) {
					r.ignoreRule = true;
				}
			}else {
				for(Rule r : g) {
					r.ignoreRule = false;
				}
			}
		}
		ObjectIterator<String> iterator = individualRulesUIDMap.values().iterator();
//		System.err.println(getState()+" "+entity+" [ONRULECHANGE] INDIVIDUAL RULES "+individualRulesUIDMap.size()+"; MAP: "+individualRulesUIDMap);
		while(iterator.hasNext() ) {
			String ruleSetUID = iterator.next();
			RuleSet rll = ((GameStateInterface)entity.getState()).getGameState().getRuleManager().getRuleSetByUID(ruleSetUID);
			
			if(rll != null) {
				RuleSet g = new RuleSet(rll, rll.getUniqueIdentifier());
				individualRules.add(g);
				
//				if(individualGlobalRuleExceptionUIDMap.containsKey(rll.getUniqueIdentifier().toLowerCase(Locale.ENGLISH))) {
//					for(Rule r : g) {
//						r.ignoreRule = true;
//					}
//				}else {
//					for(Rule r : g) {
//						r.ignoreRule = false;
//					}
//				}
			}else {
				try {
					throw new Exception("WARNING. no ruleset found for "+ruleSetUID);
				} catch (Exception e) {
					e.printStackTrace();
				}
				iterator.remove();
			}
		}
		for(RuleSet rs : globalRules) {
			for(Rule r : rs) {
				if(entity.getTopLevelType() == r.ruleType) {
					if(!r.ignoreRule) {
						rulesActiveTotal.add(r);
					}
					rulesTotal.add(r);
				}else {
					ruleIdDifferentTypeDebugMap.put(r.getRuleId(), r);
				}
			}
		}
		for(RuleSet rs : individualRules) {
			for(Rule r : rs) {
				if(entity.getTopLevelType() == r.ruleType) {
					if(!r.ignoreRule) {
						rulesActiveTotal.add(r);
					}
					rulesTotal.add(r);
				}else {
					ruleIdDifferentTypeDebugMap.put(r.getRuleId(), r);
				}
			}
		}
//		System.err.println(getState()+" "+entity+" [ONRULECHANGE] RESULT: TOTAL: "+rulesTotal.size()+"; globalSets: "+globalRules.size()+"; individualSets: "+individualRules.size()+";");
		for(Rule r : rulesTotal) {
			ruleIdMap.put(r.getRuleId(), r);
		}
		if(!entity.isOnServer()) {
			requestRuleStatesFromServer();
		}else {
//			System.err.println("CHEKCING ALL RULES FOR SERVER");
			checkAllRules();
		}
		notifyObservers();
		flagRuleChanged = false;
	}
	
	public StateInterface getState() {
		return entity.getState();
	}

	public abstract byte getEntitySubType();

	private void checkRules(long trigger) {
		for(Rule r : getActiveRules()) {
			r.process((RuleEntityContainer) entity, entity.getTopLevelType(), trigger);
		}
	}
	private void checkAllRules() {
		checkRules(SegmentControllerCondition.TRIGGER_ON_ALL);
	}


	private void requestRuleStatesFromServer() {
		for(Rule r : getActiveRules()) {
			r.requestRuleState((RuleEntityContainer)entity);
		}
	}


	public RuleEntityManager(E entity) {
		this.entity = entity;
	}
	
	public void update(Timer timer) {
		if(!isOnServer() && ruleChangePrepare) {
			System.err.println("[CLIENT] RuleChange Received but not applied in game state yet. Not executing rule updates");
			return;
		}
		if(!isOnServer() && !getRuleSetManager().receivedInitialOnClient) {
			//dont update until we received rules on client
			System.err.println("Not Updating "+isOnServer());
			return;
		}
		if(flagRuleChanged) {
			try {
				onRulesChanged();
			} catch (IOException e) {
				e.printStackTrace();
				flagRuleChanged = false;
			}
		}
		
		while(!receivedRuleStates.isEmpty()) {
			processReceivedState(receivedRuleStates.dequeue());
		}
		
		for(ActionUpdate a : updates) {
			a.update(timer);
		}
		if(this.trigger != 0) {
			assert(isOnServer());
			checkRules(this.trigger);
			this.trigger = 0;
		}
		

		for(int i = 0; i < recurringActions.size(); i++) {
			RecurringAction<E> ra = recurringActions.get(i);
			if(timer.currentTime - ra.getLastActivate() > ra.getCheckInterval()) {
				ra.onActive(entity);
				ra.setLastActivate(timer.currentTime);
			}
		}

		while(!receivedRuleIndividualRequests.isEmpty()) {
			String in = receivedRuleIndividualRequests.dequeue();
			processIndividualProcess(in, isOnServer());
		}
		while(!receivedRuleStateRequests.isEmpty()) {
			assert(isOnServer()):"Rule State Request must have been on client and may only be sent from server "+entity;
			int ruleId = receivedRuleStateRequests.dequeueInt();
			Rule r = ruleIdMap.get(ruleId);
			if(r != null) {
				r.sendCompleteRuleState((RuleEntityContainer) entity);
			}else {
				Rule differentTypeRule = ruleIdDifferentTypeDebugMap.get(ruleId);
				if(differentTypeRule == null) {
					try {
						throw new Exception("Rule state request for nonexiting rule: "+entity+"; ruleId: "+ruleId+"; "+ruleIdMap);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		if(isOnServer()) {
			for(int i = 0; i < timedConditions.size(); i++) {
				TimedCondition c = timedConditions.get(i);
				long serverTime = entity.getState().getUpdateTime();
				boolean fire = c.isTimeToFire(serverTime);
				if(fire && !c.isTriggeredTimedCondition()) {
					c.flagTriggeredTimedCondition();
					triggerTimedCondition();
				}else if(!fire && c.isTriggeredTimedCondition() && !c.isTriggeredTimedEndCondition()) {
					//another trigger when the condition was triggered but not anymore (duration ended)
					c.flagTriggeredTimedEndCondition();
					triggerTimedCondition();
				}

				if(c.isRemoveOnTriggered() && c.isTriggeredTimedCondition()) {
					timedConditions.remove(i);
					i--;
				}
			}
		}
	}
	private void processIndividualProcess(String in, boolean send) {
		if(in.startsWith(ADD_INDIVIDUAL)) {
			String s = in.substring(ADD_INDIVIDUAL.length());
			assert(getRuleSetManager().getRuleSetByUID(s) != null):s;
			individualRulesUIDMap.put(s.toLowerCase(Locale.ENGLISH), s);
			System.err.println(getState()+" "+entity+" [RULEENTITYMANAGER] RECEIVED ADD INDIVIDUAL ADD "+s+" deligate: "+send);
		}else if(in.startsWith(REMOVE_INDIVIDUAL)) {
			String s = in.substring(REMOVE_INDIVIDUAL.length());
			String remove = individualRulesUIDMap.remove(s.toLowerCase(Locale.ENGLISH));
			assert(remove != null):s;
			System.err.println(getState()+" "+entity+" [RULEENTITYMANAGER] RECEIVED REMOVE INDIVIDUAL ADD "+s+" deligate: "+send);
		}else if(in.startsWith(ADD_IGNORE)) {
			String s = in.substring(ADD_IGNORE.length());
			assert(getRuleSetManager().getRuleSetByUID(s) != null):s;
			individualGlobalRuleExceptionUIDMap.put(s.toLowerCase(Locale.ENGLISH), s);
			System.err.println(getState()+" "+entity+" [RULEENTITYMANAGER] RECEIVED IGNORE ADD "+s+" deligate: "+send);
		}else if(in.startsWith(REMOVE_IGNORE)) {
			String s = in.substring(REMOVE_IGNORE.length());
			String remove = individualGlobalRuleExceptionUIDMap.remove(s.toLowerCase(Locale.ENGLISH));
			assert(remove != null):s;
			System.err.println(getState()+" "+entity+" [RULEENTITYMANAGER] RECEIVED IGNORE REMOVE "+s+" deligate: "+send);
		}else {
			throw new RuntimeException("Unknown individual Rule command: "+in);
		}
		if(send) {
			entity.getNetworkObject().getRuleIndividualAddRemoveBuffer().add(new RemoteString(in, isOnServer()));
		}
		flagRuleChanged(); 
	}

	public void flagRuleChanged() {
		this.ruleChangePrepare = false;
		this.flagRuleChanged = true;
	}

	private boolean isOnServer() {
		return entity.isOnServer();
	}


	public boolean existsUpdatableAction(ActionUpdate a) {
		for(int i = 0; i < updates.size(); i++) {
			if(updates.get(i).getAction() == a.getAction()) {
				return true;
			}
		}
		return false;
	}
	public void addUpdatableAction(ActionUpdate a) {
		if(!existsUpdatableAction(a)) {
			a.onAdd();
			updates.add(a);
		}
	}

	public void removeUpdatableAction(ActionUpdate a) {
		for(int i = 0; i < updates.size(); i++) {
			if(updates.get(i).getAction() == a.getAction()) {
				updates.get(i).onRemove();
				updates.remove(i);
				return;
			}
		}
	}

	public void sendRuleStateChange(Rule rule, RuleStateChange stateChange) {
		assert(isOnServer());
		RuleStateChange c = new RuleStateChange(rule, stateChange);
		assert(entity.getNetworkObject() instanceof NTRuleInterface);
		((NTRuleInterface)entity.getNetworkObject()).getRuleStateChangeBuffer().add(new RemoteRuleStateChange(c, entity.isOnServer()));
	}

	public void receive(NTRuleInterface n) {
		
		
		{
			RemoteRuleStateChangeBuffer o = n.getRuleStateChangeBuffer();
			for(int i = 0; i < o.getReceiveBuffer().size(); i++) {
				RuleStateChange r = o.getReceiveBuffer().get(i).get();
				receivedRuleStates.enqueue(r);
				
			}
		}
		{
			RemoteIntBuffer o = n.getRuleStateRequestBuffer();
			for(int i = 0; i < o.getReceiveBuffer().size(); i++) {
				int ruleRequested = o.getReceiveBuffer().getInt(i);
				receivedRuleStateRequests.enqueue(ruleRequested);
				
			}
		}
		{
			RemoteBuffer<RemoteString> o = n.getRuleIndividualAddRemoveBuffer();
			for(int i = 0; i < o.getReceiveBuffer().size(); i++) {
				String r = o.getReceiveBuffer().get(i).get();
				receivedRuleIndividualRequests.enqueue(r);
			
			}
		}
	}
	public RuleSetManager getRuleSetManager() {
		return ((GameStateInterface)entity.getState()).getGameState().getRuleManager();
	}
	private void processReceivedState(RuleStateChange r) {
		Rule rule = ruleIdMap.get(r.ruleIdNT);
		
		if(rule != null) {
			boolean changed = rule.receiveState((RuleEntityContainer) entity, entity.getTopLevelType(),  r);
		}else {
			Rule differentTypeRule = ruleIdDifferentTypeDebugMap.get(r.ruleIdNT);
			if(differentTypeRule == null) {
				try {
					throw new Exception("Rule ID not found for "+entity+"; ID: "+r.ruleIdNT+"; ID MAP: "+ruleIdMap);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	public void addIndividualRuleSet(RuleSet rs) {
		entity.getNetworkObject().getRuleIndividualAddRemoveBuffer().add(new RemoteString(ADD_INDIVIDUAL+rs.uniqueIdentifier, isOnServer()));
		System.err.println("SENDING ADD INDIVIDUAL RULE "+rs.uniqueIdentifier);
	}
	public void removeIndividualRuleSet(RuleSet rs) {
		entity.getNetworkObject().getRuleIndividualAddRemoveBuffer().add(new RemoteString(REMOVE_INDIVIDUAL+rs.uniqueIdentifier, isOnServer()));
		System.err.println("SENDING REMOVE INDIVIDUAL RULE "+rs.uniqueIdentifier);
	}
	public void addIgnoreRuleSet(RuleSet rs) {
		entity.getNetworkObject().getRuleIndividualAddRemoveBuffer().add(new RemoteString(ADD_IGNORE+rs.uniqueIdentifier, isOnServer()));
		System.err.println("SENDING ADD IGNORE RULE "+rs.uniqueIdentifier);
	}
	public void removeIgnoreRuleSet(RuleSet rs) {
		entity.getNetworkObject().getRuleIndividualAddRemoveBuffer().add(new RemoteString(REMOVE_IGNORE+rs.uniqueIdentifier, isOnServer()));
		System.err.println("SENDING REMOVE IGNORE RULE "+rs.uniqueIdentifier);
	}

	public void removeIndividualRuleSetByRule(Rule r) {
		for(RuleSet rs : individualRules) {
			if(rs.contains(r)) {
				removeIndividualRuleSet(rs);
			}
		}
	}
	public void addIgnorelRuleSetByRule(Rule r) {
		for(RuleSet rs : globalRules) {
			if(rs.contains(r)) {
				addIgnoreRuleSet(rs);
			}
		}
	}
	public void removeIgnorelRuleSetByRule(Rule r) {
		for(RuleSet rs : globalRules) {
			if(rs.contains(r) && r.ruleType == entity.getTopLevelType()) {
				removeIgnoreRuleSet(rs);
			}
		}
	}

	public void flagRuleChangePrepare() {
		ruleChangePrepare = true;
	}

	public void addRecurringAction(RecurringAction<E> ra) {
		recurringActions.add(ra);
	}

	public void removeRecurringAction(RecurringAction<E> ra) {
		boolean removed = recurringActions.remove(ra);
		if(!removed) {
			System.err.println(getState()+" [WARNING] RECURRING ACTION NOT REMOVED: "+recurringActions+"; "+ra);
		}
	}
}
