package org.schema.game.common.controller.rules.rules.actions.seg;

import java.util.Locale;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.controller.rules.rules.actions.ActionTypes;
import org.schema.game.common.data.blockeffects.config.ConfigGroup;
import org.schema.game.common.data.blockeffects.config.EffectConfigNetworkObjectInterface;
import org.schema.schine.common.language.Lng;

public class SegmentControllerApplyEffectAction extends SegmentControllerAction {
	
	
	@RuleValue(tag = "EffectUID")
	public String effectUID = "";


	public SegmentControllerApplyEffectAction() {
		super();
	}

	@Override
	public ActionTypes getType() {
		return ActionTypes.SEG_APPLY_EFFECT;
	}


	@Override
	public String getDescriptionShort() {
		return Lng.str("Prints Message (leave empty to skip)");
	}

	@Override
	public void onTrigger(SegmentController s) {
		if(effectUID.trim().length() > 0) {
			if(s.isOnServer()) {
				ConfigGroup configGroup = s.getConfigManager().getConfigPool().poolMapLowerCase.get(effectUID.toLowerCase(Locale.ENGLISH));
				if(configGroup != null) {
					s.getConfigManager().addEffectAndSend(configGroup, true, (EffectConfigNetworkObjectInterface) s.getNetworkObject());
				}else {
					System.err.println("[SERVER][RULE][ACTION][APPLYEFFECT][ERROR] Can't add effect to "+s+". Effect UID not found: "+effectUID);
				}
			}
		}
	}

	@Override
	public void onUntrigger(SegmentController s) {
		if(effectUID.trim().length() > 0) {
			if(s.isOnServer()) {
				ConfigGroup configGroup = s.getConfigManager().getConfigPool().poolMapLowerCase.get(effectUID.toLowerCase(Locale.ENGLISH));
				if(configGroup != null) {
					s.getConfigManager().removeEffectAndSend(configGroup, true, (EffectConfigNetworkObjectInterface) s.getNetworkObject());
				}else {
					System.err.println("[SERVER][RULE][ACTION][APPLYEFFECT][ERROR] Can't remove effect from "+s+". UID not found: "+effectUID);
				}
			}
		}
	}

	
	
}
