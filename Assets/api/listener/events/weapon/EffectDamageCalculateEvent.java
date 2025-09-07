package api.listener.events.weapon;

import api.listener.events.Event;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.HitReceiverType;
import org.schema.game.common.controller.damage.HitType;
import org.schema.game.common.controller.damage.effects.InterEffectHandler;
import org.schema.game.common.controller.damage.effects.InterEffectSet;

/**
This event fires every time damage for a given damage type is calculated.
 Due to some idiosyncracies in the damage calculations, the result of your calculations must be multiplied by the number of damage types that exist. It gets divided by the same later.
 */
public class EffectDamageCalculateEvent extends Event {

    private final float inputDamage;
    private float resultDamage;
    private final InterEffectHandler.InterEffectType damageType;
    private final InterEffectSet attack;
    private final InterEffectSet defense;
    private final HitType hitType;
    private final DamageDealerType sourceType;
    private final HitReceiverType recieverType;
    private final short typeParam;

    public EffectDamageCalculateEvent(
            float inputDamage,
            float resultDamage,
            InterEffectHandler.InterEffectType type,
            InterEffectSet attack,
            InterEffectSet defense,
            HitType hitType,
            DamageDealerType damageType,
            HitReceiverType recieverType,
            short typeParam
    ) {
        this.inputDamage = inputDamage;
        this.resultDamage = resultDamage;
        this.damageType = type;
        this.attack = attack;
        this.defense = defense;
        this.hitType = hitType;
        this.sourceType = damageType;
        this.recieverType = recieverType;
        this.typeParam = typeParam;
    }

    /**
     * @return The resulting damage in this damage type, multiplied by the total amount of default damage types (3 as of the time of writing).
     */
    public float getResultDamage() {
        return resultDamage;
    }

    /**
     * @param resultDamage the actual damage **FOR THIS DAMAGE TYPE** that results.
     */
    public void setResultDamage(float resultDamage) {
        this.resultDamage = resultDamage;
    }

    /**
     * @return the original damage
     */
    public float getInputDamage() {
        return inputDamage;
    }

    /**
     * @return the damage type being calculated at this event
     */
    public InterEffectHandler.InterEffectType getDamageType() {
        return damageType;
    }

    /**
     * @return the damage profile of the attack. For the value of any given type, call getAttack().getStrength(EFFECTS.SomeType).
     */
    public InterEffectSet getAttack() {
        return attack;
    }

    /**
     * Provides the defensive profile of the target. For the value as any given type, call getDefense().getStrength(EFFECTS.SomeType).
     * @return the defenses of the target.
     */
    public InterEffectSet getDefense() {
        return defense;
    }

    /**
     * @return the type of hit that resulted in this damage
     */
    public HitType getHitType() {
        return hitType;
    }

    public DamageDealerType getSourceType() {
        return sourceType;
    }

    public HitReceiverType getRecieverType() {
        return recieverType;
    }

    public short getTypeParam() {
        return typeParam;
    }
}
