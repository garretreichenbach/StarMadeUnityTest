package api.listener.events.state;

import api.listener.events.Event;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;
import org.schema.schine.ai.stateMachines.State;

public class FSMStateEnterEvent extends Event {
    private final FiniteStateMachine<?> machinef;
    public final State statef;

    public FSMStateEnterEvent(FiniteStateMachine<?> stateMachine, State state){
        machinef = stateMachine;
        statef = state;
    }

    public FiniteStateMachine<?> getMachine() {
        return machinef;
    }

    public State getState() {
        return statef;
    }
}
