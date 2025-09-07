package org.schema.game.client.view.mainmenu.gui.ruleconfig;

import java.util.Collection;
import java.util.List;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.rules.RuleSet;
import org.schema.game.common.controller.rules.rules.Rule;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GUIEntityRuleStatGlobal extends GUIEnttityRuleStat{
	public GUIEntityRuleStatGlobal(GameClientState state, GUIEntitySelectedRuleStat selectedEntityRuleStat) {
		super(state, selectedEntityRuleStat, true);
	}

	@Override
	public Collection<Rule> getRules() {
		List<Rule> rls = new ObjectArrayList<Rule>();
		if(getEntityContainer() == null) {
			return rls;
		}
		for(RuleSet r : getEntityContainer().getRuleEntityManager().globalRules) {
			rls.addAll(r);
		}
		return rls;
	}

	
}
