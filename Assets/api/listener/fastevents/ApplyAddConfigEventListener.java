package api.listener.fastevents;

import org.schema.game.common.controller.damage.effects.InterEffectHandler;
import org.schema.game.common.data.blockeffects.config.ConfigEntityManager;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;

public interface ApplyAddConfigEventListener {
    float onApplyEffectDefense(ConfigEntityManager c, StatusEffectType t, InterEffectHandler.InterEffectType em, float result, float[] strength);
}
