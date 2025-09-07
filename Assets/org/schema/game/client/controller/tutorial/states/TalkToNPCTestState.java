package org.schema.game.client.controller.tutorial.states;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.creature.AICreature;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.common.language.Lng;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.objects.Sendable;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class TalkToNPCTestState extends SatisfyingCondition {

	/**
	 *
	 */
	
	private final String npcName;
	public TalkToNPCTestState(AiEntityStateInterface gObj, String txt, GameClientState state, String npcName) {
		super(gObj, txt, state);
		skipIfSatisfiedAtEnter = true;

		this.npcName = npcName;

		setMarkers(new ObjectArrayList<TutorialMarker>(1));
		getMarkers().add(new TutorialMarker(new Vector3i(), Lng.str("Press %s here to talk to '%s'", KeyboardMappings.ACTIVATE.getKeyChar(), npcName)));

	}

	@Override
	protected boolean checkSatisfyingCondition() throws FSMException {
		synchronized (getGameState().getLocalAndRemoteObjectContainer().getLocalObjects()) {
			for (Sendable s : getGameState().getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
				if (s instanceof AICreature<?>) {
					AICreature<?> p = (AICreature<?>) s;
					if (p.getRealName().equals(npcName)) {
						getMarkers().get(0).absolute = p.getWorldTransform().origin;
//							System.err.println("NPC RELATIVE POS "+markers.get(0).where);
						return p.getOwnerState().getConversationPartner() == getGameState().getPlayer();
					}
				}
			}
		}
		System.err.println("[TUTORIAL] ERROR: NPC " + npcName + " does NOT exist");
		getEntityState().getMachine().getFsm().stateTransition(Transition.TUTORIAL_FAILED);
		return false;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.tutorial.states.SatisfyingCondition#onUpdate()
	 */
	@Override
	public boolean onUpdate() throws FSMException {

		return super.onUpdate();
	}

	/**
	 * @return the npcName
	 */
	public String getNpcName() {
		return npcName;
	}

}
