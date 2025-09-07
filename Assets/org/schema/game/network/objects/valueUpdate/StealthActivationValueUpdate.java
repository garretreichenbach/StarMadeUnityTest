package org.schema.game.network.objects.valueUpdate;

import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.stealth.StealthElementManager;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class StealthActivationValueUpdate extends BooleanValueUpdate{
    long lastStart;
    int cooldown;

    @Override
    public boolean applyClient(ManagerContainer<?> o) {
        if(o.getStealth() != null && o.getStealth().getElementManager().hasCollection()) {
            StealthElementManager s = o.getStealth().getElementManager();
            s.setActivationCooldown(cooldown);
            s.setStealthActivation(val);
            if(!val) o.getStealth().getElementManager().getCollection().getCooldownManager().startCooldown(lastStart); //don't need to assign if not deactivating
            return true;
        }
        else{
            return false;
//			assert(false);
        }
    }

    @Override
    public void setServer(ManagerContainer<?> o, long parameter) {
        this.val = o.getStealth().getElementManager().isActive();
        this.cooldown = o.getStealth().getElementManager().getActivationCooldownMs();
        this.lastStart = o.getStealth().getElementManager().getCollection().getCooldownManager().getLastStartedCooldown();
    }

    @Override
    public ValTypes getType() {
        return ValTypes.STEALTH_ACTIVE;
    }

    @Override
    public void serialize(DataOutput buffer, boolean onServer) throws IOException {
        super.serialize(buffer, onServer);
        buffer.writeLong(lastStart);
        buffer.writeInt(cooldown);
    }

    @Override
    public void deserialize(DataInput stream, int updateSenderStateId, boolean onServer) throws IOException {
        super.deserialize(stream, updateSenderStateId, onServer);
        lastStart = stream.readLong();
        cooldown = stream.readInt();
    }
}
