package api.listener.events.calculate;

import api.listener.events.Event;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.power.reactor.PowerImplementation;

public class CurrentPowerCalculateEvent extends Event {
    private final PowerImplementation impl;
    private double power;
    private SegmentController segmentController;

    public CurrentPowerCalculateEvent(PowerImplementation impl, double power){
        this.segmentController = impl.getSegmentController();
        this.impl = impl;
        this.power = power;
    }

    public void setPower(double power){
        this.power = power;
    }

    public void addPower(double power){
        this.power += power;
    }

    public PowerImplementation getImpl() {
        return impl;
    }

    public double getPower() {
        return power;
    }

    public SegmentController getSegmentController() {
        return segmentController;
    }
}
