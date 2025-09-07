package api.listener.events.weapon;

import api.listener.events.Event;
import org.schema.game.common.controller.elements.BeamState;
import org.schema.game.common.data.element.beam.AbstractBeamHandler;

public class BeamPostAddEvent extends Event {
    private final AbstractBeamHandler<?> handlerf;
    private final BeamState statef;
    private boolean updatedExistingf;

    public BeamPostAddEvent(AbstractBeamHandler<?> handler, BeamState beamState, boolean updatedExisting){
        handlerf = handler;
        statef = beamState;
        updatedExistingf = updatedExisting;
    }

    public AbstractBeamHandler<?> getHandler() {
        return handlerf;
    }

    public BeamState getBeamState() {
        return statef;
    }

    public boolean updatedExisting() {
        return updatedExistingf;
    }
}
