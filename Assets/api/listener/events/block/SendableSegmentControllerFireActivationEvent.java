package api.listener.events.block;

import api.listener.events.Event;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.data.element.ActivationTrigger;

/**
 * Created by Jake on 2/24/2021.
 * <insert description here>
 */
public class SendableSegmentControllerFireActivationEvent extends Event {
    private SendableSegmentController controller;
    private ActivationTrigger trigger;

    public SendableSegmentControllerFireActivationEvent(SendableSegmentController controller, ActivationTrigger trigger) {
        this.controller = controller;
        this.trigger = trigger;
    }

    public SendableSegmentController getController() {
        return controller;
    }

    public ActivationTrigger getTrigger() {
        return trigger;
    }
}
