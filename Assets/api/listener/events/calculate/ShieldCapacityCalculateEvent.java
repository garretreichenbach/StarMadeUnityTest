package api.listener.events.calculate;

import api.listener.events.Event;
import org.schema.game.common.controller.elements.ShieldLocal;
import org.schema.game.common.controller.elements.shield.capacity.ShieldCapacityUnit;

public class ShieldCapacityCalculateEvent extends Event {
    private final ShieldCapacityUnit capacityUnit;
    private double capacity;

    public ShieldCapacityCalculateEvent(ShieldCapacityUnit unit, ShieldLocal local, double capacity) {
        this.capacityUnit = unit;
        this.capacity = capacity;
    }

    public void setShields(long shields) {
        capacity = shields;
    }

    public void addShields(long shields) {
        capacity += shields;
    }

    public void subtractShields(long shields) {
        capacity -= shields;
    }


    public ShieldCapacityUnit getCapacityUnit() {
        return capacityUnit;
    }

    public double getCapacity() {
        return capacity;
    }
}
