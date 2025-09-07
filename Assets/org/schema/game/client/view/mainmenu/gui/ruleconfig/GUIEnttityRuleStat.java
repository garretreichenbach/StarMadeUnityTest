package org.schema.game.client.view.mainmenu.gui.ruleconfig;

import org.schema.game.client.controller.EntitySelectionChangeChangeListener;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.rules.rules.Rule;
import org.schema.game.common.controller.rules.rules.actions.Action;
import org.schema.game.common.controller.rules.rules.conditions.Condition;
import org.schema.game.common.data.world.RuleEntityContainer;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.forms.gui.GUIChangeListener;

public abstract class GUIEnttityRuleStat extends GUIRuleCollection implements EntitySelectionChangeChangeListener, GUIChangeListener{

	public final GameClientState state;
	public final GUIEntitySelectedRuleStat selectedEntityRuleStat;
	public Rule selectedRule;
	public final boolean canRulesBeIgnored;
	
	public GUIEnttityRuleStat(GameClientState state, GUIEntitySelectedRuleStat selectedEntityRuleStat, boolean canRulesBeIgnored) {
		this.state = state;
		this.selectedEntityRuleStat = selectedEntityRuleStat;
		this.canRulesBeIgnored = canRulesBeIgnored;
		
		
		RuleEntityContainer entityContainer = getEntityContainer();
		if(entityContainer != null) {
			entityContainer.getRuleEntityManager().addObserver(this);
		}
	}
	
	@Override
	public void addObserver(GUIChangeListener o) {
		super.addObserver(o);
		state.getController().addEntitySelectionChangeListener(this);
	}
	

	@Override
	public void deleteObserver(GUIChangeListener o) {
		super.deleteObserver(o);
		state.getController().removeEntitySelectionChangeListener(this);
	}

	@Override
	public void deleteObservers() {
		super.deleteObservers();
		state.getController().removeEntitySelectionChangeListener(this);
	}
	public boolean canRulesBeIgnored() {
		return canRulesBeIgnored;
	}
	

	@Override
	public void setSelectedRule(Rule f) {
		this.selectedRule = f; //keep second ref to know which list was clicked on
		this.selectedEntityRuleStat.selectRule(f);
	}

	@Override
	public void setSelectedAction(Action<?> object) {
	}

	@Override
	public void setSelectedCondition(Condition<?> object) {
	}

	@Override
	public void change() {
		notifyObservers();
	}

	public RuleEntityContainer getEntityContainer() {
		if(state.getSelectedEntity() instanceof RuleEntityContainer) {
			return (RuleEntityContainer)state.getSelectedEntity();
		}else {
			return null;
		}
		
	}

	@Override
	public void onEntityChanged(SimpleTransformableSendableObject<?> old,
			SimpleTransformableSendableObject<?> selected) {
		if(old instanceof RuleEntityContainer) {
			((RuleEntityContainer)old).getRuleEntityManager().deleteObserver(this);
		}
		if(selected instanceof RuleEntityContainer) {
			((RuleEntityContainer)selected).getRuleEntityManager().addObserver(this);
		}
		//flag to update
		change();
	}


	@Override
	public void onChange(boolean updateListDim) {
		System.err.println(getEntityContainer()+" RULE ENTITY CHANGED OBSERVERS CALLED");
		//called from a rule entity manager (if the entity's rules parameters changed)
		change();		
	}

	public Rule getSelectedRule() {
		return selectedRule;
	}

}
