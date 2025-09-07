package org.schema.game.common.controller.rules;

import java.util.List;

import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.rules.rules.Rule;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.TagSerializable;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class RuleContainer<A extends SimpleTransformableSendableObject<?>> implements TagSerializable{

	private final A obj; 
	private static byte VERSION = 0; 
	
	private final List<Rule> globalRules = new ObjectArrayList<Rule>();
	private final List<Rule> individualRules = new ObjectArrayList<Rule>();


	private final RuleSetManager ruleManager;
	
	public RuleContainer(A a) {
		this.obj = a;
		this.ruleManager = ((GameStateInterface)a.getState()).getGameState().getRuleManager();
	}
	public A getObj() {
		return obj;
	}
	
	
	public void onRuleChange() {
		
	}
	public RuleSetManager getRuleManager() {
		return ruleManager;
	}
	@Override
	public void fromTagStructure(Tag tag) {
				
	}
	@Override
	public Tag toTagStructure() {
		return null;
	}
}
