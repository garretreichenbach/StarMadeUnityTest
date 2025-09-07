package org.schema.game.client.view.mainmenu.gui.ruleconfig;

import java.util.Collection;
import java.util.List;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.rules.RuleSet;
import org.schema.game.common.controller.rules.rules.Rule;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GUIEntityRuleStatIndividual extends GUIEnttityRuleStat{
	public GUIEntityRuleStatIndividual(GameClientState state, GUIEntitySelectedRuleStat selectedEntityRuleStat) {
		super(state, selectedEntityRuleStat, false);
	}

	@Override
	public Collection<Rule> getRules() {
		List<Rule> rls = new ObjectArrayList<Rule>();
		if(getEntityContainer() == null) {
			return rls;
		}
		for(RuleSet r : getEntityContainer().getRuleEntityManager().individualRules) {
			rls.addAll(r);
		}
		return rls;
	}
}
