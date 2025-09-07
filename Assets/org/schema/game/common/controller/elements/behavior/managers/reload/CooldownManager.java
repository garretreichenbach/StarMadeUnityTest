package org.schema.game.common.controller.elements.behavior.managers.reload;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.elements.behavior.interfaces.ManagedCooldownInterface;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector4f;

import static org.schema.game.common.controller.elements.UsableControllableElementManager.drawReload;

public class CooldownManager {
    public static final Vector4f cooldownColor = new Vector4f(0.7F, 0.71F, 0.73F, 0.4F);
    protected final ManagedCooldownInterface ifc;
    private long lastStartedReload;
    protected boolean sendUpdates;

    public CooldownManager(ManagedCooldownInterface i, boolean isUpdateSender) {
        ifc = i;
        sendUpdates = isUpdateSender;
    }

    public long getLastStartedCooldown(){
        return lastStartedReload;
    }

    public void startCooldown(Timer t){
        startCooldown(t.currentTime);
    }

    public void startCooldown(long time){
        lastStartedReload = time;
    }

    public boolean isCoolingDown(long currentTime){
        return ifc.getCooldownDurationMs() > currentTime - lastStartedReload;
    }

    public boolean isCoolingDown(Timer t){
        return isCoolingDown(t.currentTime);
    }

    /**
     * Draw a "reload" visual based on the remaining cooldown time, if any.
     */
    public void drawReloads(Vector3i iconPos, Vector3i iconSize, InputState state, long currentTime){
        if(isCoolingDown(currentTime)) {
            float fraction = (float) (currentTime - getLastStartedCooldown()) / ifc.getCooldownDurationMs();
            drawReload(state, iconPos, iconSize, cooldownColor, true, fraction);
        }
    }
}
