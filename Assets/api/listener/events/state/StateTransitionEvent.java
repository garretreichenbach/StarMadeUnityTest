package api.listener.events.state;

import api.listener.events.Event;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;

public class StateTransitionEvent extends Event {
    private final State statef;
    private final Transition transitionf;
    private final int subtransitionf;

    public StateTransitionEvent(State state, Transition transition, int subtransition){

        statef = state;
        transitionf = transition;
        subtransitionf = subtransition;
    }

    public State getState() {
        return statef;
    }

    public Transition getTransition() {
        return transitionf;
    }

    public int getSubtransition() {
        return subtransitionf;
    }
}
