package org.schema.game.client.controller.tutorial;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.schema.game.client.controller.tutorial.states.SatisfyingCondition;
import org.schema.game.client.controller.tutorial.states.TextState;
import org.schema.game.client.controller.tutorial.states.TutorialEnded;
import org.schema.game.client.controller.tutorial.states.TutorialEndedTextState;
import org.schema.game.client.controller.tutorial.states.TutorialMarker;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.EditableSendableSegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.schine.ai.MachineProgram;
import org.schema.schine.ai.stateMachines.AIConfiguationElementsInterface;
import org.schema.schine.ai.stateMachines.AiEntityState;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.resource.FileExt;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class TutorialMode extends MachineProgram<AiEntityState> {

	public static final String BASIC_TUTORIAL = "Basics Tutorial";
	public static final Object2ObjectOpenHashMap<String, Object2ObjectOpenHashMap<String, String>> translation = new Object2ObjectOpenHashMap<String, Object2ObjectOpenHashMap<String, String>>();
	public EditableSendableSegmentController currentContext;
	public Ship lastSpawnedShip;
	public List<TutorialMarker> markers;

	public short highlightType;
	private boolean backgroundMode = false;

	public TutorialMode(AiEntityState entityState) {
		super(entityState, true);

		suspend(false);
	}

	@Override
	public void onAISettingChanged(AIConfiguationElementsInterface setting) {

	}

	@Override
	protected String getStartMachine() {
		return BASIC_TUTORIAL;
	}

	/**
	 * @return the state
	 */
	@Override
	public GameClientState getState() {
		return (GameClientState) super.getState();
	}

	@Override
	protected void initializeMachines(
			HashMap<String, FiniteStateMachine<?>> machines) {
		machines.put(BASIC_TUTORIAL, new DynamicTutorialStateMachine(getEntityState(), this, (new FileExt("./data/tutorial/TutorialStartUp/TutorialStartUp.xml")).getAbsolutePath(), new FileExt("./data/tutorial/TutorialStartUp/")));

		File dir = new FileExt("./data/tutorial/");

		for (File subDir : dir.listFiles()) {
			if (subDir.isDirectory()) {
				for (File f : subDir.listFiles()) {
					if (f.getName().endsWith(".xml")) {
						try {
							machines.put(subDir.getName(), new DynamicTutorialStateMachine(getEntityState(), this, f.getAbsolutePath(), subDir));
						} catch (TutorialException e) {
							System.err.println("[TUTORIAL] TUTORIAL EXCEPTION IN " + subDir.getName());
							e.printStackTrace();
							throw new RuntimeException(e);
						}
					}
				}
			}
		}

	}

	public boolean hasBack() {

		if (getMachine().getFsm().getCurrentState().containsTransition(Transition.BACK)) {
			try {
				State output = getMachine().getFsm().getCurrentState().getStateData().getOutput(Transition.BACK);
				return output != getMachine().getFsm().getCurrentState() && output instanceof TextState;
			} catch (FSMException e) {
				e.printStackTrace();
			}
		}
		return false;

	}

	public void back() throws FSMException {
		getMachine().getFsm().stateTransition(Transition.BACK);

	}

	public void end() {
		try {
			getMachine().getFsm().stateTransition(Transition.TUTORIAL_END);
		} catch (FSMException e) {
			e.printStackTrace();
		}

	}

    public void endNow() {
        try {
            getMachine().getFsm().stateTransition(Transition.TUTORIAL_END);
            getMachine().getFsm().stateTransition(Transition.CONDITION_SATISFIED);
        } catch (FSMException e) {
            e.printStackTrace();
        }

    }

	public void endStep() {
		try {
			getMachine().getFsm().stateTransition(Transition.TUTORIAL_SKIP_PART);
		} catch (FSMException e) {
			e.printStackTrace();
		}

	}

	public boolean isBackgroundMode() {
		return backgroundMode;
	}

	/**
	 * @param backgroundMode the backgroundMode to set
	 */
	public void setBackgroundMode(boolean backgroundMode) {
		this.backgroundMode = backgroundMode;
	}

	public void repeat() {
		try {
			getMachine().getFsm().stateTransition(Transition.TUTORIAL_RESTART);
		} catch (FSMException e) {
			e.printStackTrace();
		}

	}

	public void repeatStep() {
		try {
			getMachine().getFsm().stateTransition(Transition.TUTORIAL_RESET_PART);
		} catch (FSMException e) {
			e.printStackTrace();
		}

	}

	public void shopDistanceChanged(boolean inShopDistance) {
		if (!(getMachine().getFsm().getCurrentState() instanceof TutorialEnded)) {
			if (!inShopDistance) {
				try {
					getMachine().getFsm().stateTransition(Transition.TUTORIAL_SHOP_DISTANCE_LOST);
				} catch (FSMException e) {
					//Expected, when not in shop tutorial mode
				}
			}
		}
	}

	public void skip() {
		if (getMachine().getFsm().getCurrentState() instanceof SatisfyingCondition) {
			((SatisfyingCondition) getMachine().getFsm().getCurrentState()).forceSatisfied();
		}
	}

	public boolean isEndState() {
		return getMachine().getFsm().getCurrentState() instanceof TutorialEnded || getMachine().getFsm().getCurrentState() instanceof TutorialEndedTextState;
	}

}
