package api.listener.events.systems;

import api.listener.events.Event;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.elements.ShieldHitCallback;
import org.schema.game.common.controller.elements.ShieldLocal;

import javax.vecmath.Vector3f;

public class ShieldHitEvent extends Event {
    private ShieldLocal shield;
    ShieldHitCallback shieldHit;
    private boolean isLowDamage;
    private boolean isHighDamage;
    private double damage;
    private SegmentController hitControllerf;

    public ShieldHitEvent(ShieldLocal local, ShieldHitCallback shieldHit, boolean isLowDamage, boolean isHighDamage, SegmentController hitController){
        this.shield = local;
        this.shieldHit = shieldHit;
        this.isLowDamage = isLowDamage;
        this.isHighDamage = isHighDamage;
        this.damage = damage;
        hitControllerf = hitController;
    }
    public Vector3f getWorldHit(){
        return new Vector3f(shieldHit.xWorld,shieldHit.yWorld,shieldHit.zWorld);
    }

    public Vector3f getLocalHit(){
        return new Vector3f(shieldHit.xLocalBlock, shieldHit.yLocalBlock, shieldHit.zLocalBlock);
    }
    public DamageDealerType getDamageType(){
        return shieldHit.damageType;
    }

    public SegmentController getHitController() {
        return hitControllerf;
    }

    public void setDamage(double damage){
        this.damage = damage;
    }
    public void addDamage(double damage){
        this.damage += damage;
    }


    public ShieldLocal getShield() {
        return shield;
    }

    public ShieldHitCallback getShieldHit() {
        return shieldHit;
    }

    public boolean isHighDamage() {
        return isHighDamage;
    }

    public boolean isLowDamage() {
        return isLowDamage;
    }

    public void setLowDamage(boolean lowDamage) {
        isLowDamage = lowDamage;
    }

    public void setHighDamage(boolean highDamage) {
        isHighDamage = highDamage;
    }
}
