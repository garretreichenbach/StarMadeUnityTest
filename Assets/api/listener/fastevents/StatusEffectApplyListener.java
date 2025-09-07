package api.listener.fastevents;

import org.schema.game.common.data.blockeffects.config.ConfigEntityManager;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;

import javax.vecmath.Vector3f;

/**
 * Created by Jake on 11/9/2020.
 * <insert description here>
 */
public interface StatusEffectApplyListener {
    int apply(ConfigEntityManager manager, StatusEffectType type, int value);
    double apply(ConfigEntityManager manager, StatusEffectType type, double value);
    float apply(ConfigEntityManager manager, StatusEffectType type, float value);
    boolean apply(ConfigEntityManager manager, StatusEffectType type, boolean value);
    Vector3f apply(ConfigEntityManager manager, StatusEffectType type, Vector3f value);
}
