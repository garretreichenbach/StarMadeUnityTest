package org.schema.game.server.data.simulation.groups;

import java.sql.SQLException;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.controller.EntityNotFountException;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class AttackSingleEntitySimulationGroup extends TargetSectorSimulationGroup {

	private String targetUID;
	private boolean startupTransferred;

	public AttackSingleEntitySimulationGroup(GameServerState state) {
		super(state);
	}

	public AttackSingleEntitySimulationGroup(GameServerState state, Vector3i targetSector, String targetUID) {
		super(state, targetSector);
		this.targetUID = targetUID;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.server.data.simulation.SimulationGroup#getMetaData()
	 */
	@Override
	protected Tag getMetaData() {
		return new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.VECTOR3i, null, targetSector),
				new Tag(Type.STRING, null, targetUID),
				FinishTag.INST,
		});
	}

	/* (non-Javadoc)
	 * @see org.schema.game.server.data.simulation.SimulationGroup#getType()
	 */
	@Override
	public GroupType getType() {
		return GroupType.ATTACK_SINGLE;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.server.data.simulation.SimulationGroup#handleMetaData(org.schema.schine.resource.Tag)
	 */
	@Override
	protected void handleMetaData(Tag metadata) {
		Tag[] tags = (Tag[]) metadata.getValue();
		targetSector = (Vector3i) tags[0].getValue();
		targetUID = (String) tags[1].getValue();

		if (getCurrentProgram() != null) {
			((TargetProgram<?>) getCurrentProgram()).setSectorTarget(targetSector);
			if (getState().getSegmentControllersByName().containsKey(targetUID)) {
				((TargetProgram<?>) getCurrentProgram()).setSpecificTargetId(
						getState().getSegmentControllersByName().get(targetUID).getId());
			}
		}
	}

	@Override
	public void returnHomeMessage(PlayerState s) {
		s.sendServerMessage(new ServerMessage(
				Lng.astr("#### Transmission Start\nTarget has fled...\nReturn to base...\n#### Transmission End\n"), ServerMessage.MESSAGE_TYPE_WARNING, s.getId()));
	}

	/* (non-Javadoc)
	 * @see org.schema.game.server.data.simulation.SimulationGroup#sendInvestigationMessage(org.schema.game.common.data.player.PlayerState)
	 */
	@Override
	public void sendInvestigationMessage(PlayerState s) {
		s.sendServerMessage(new ServerMessage(
				Lng.astr("#### Transmission Start\nHostile identified...\nExterminate...\n#### Transmission End\n"), ServerMessage.MESSAGE_TYPE_WARNING, s.getId()));
	}

	/* (non-Javadoc)
	 * @see org.schema.game.server.data.simulation.SimulationGroup#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void updateOnActive(Timer timer) throws FSMException {
		super.updateOnActive(timer);
		if (!startupTransferred && getCurrentProgram() != null) {
			if (getState().getLocalAndRemoteObjectContainer().getUidObjectMap().containsKey(targetUID)
					&& (getState().getLocalAndRemoteObjectContainer().getUidObjectMap().get(targetUID) instanceof SimpleTransformableSendableObject)) {
				SimpleTransformableSendableObject g = (SimpleTransformableSendableObject) getState().getLocalAndRemoteObjectContainer().getUidObjectMap().get(targetUID);
				System.err.println("[SIM] Specific Target found: " + targetUID + ": " + g);
				((TargetProgram<?>) getCurrentProgram()).setSpecificTargetId(g.getId());
				startupTransferred = true;
			} else {
				System.err.println("[SIM] AttackSingleEntitySimulationGroup: Target not found " + targetUID + ":::: ");
			}
		} else {

		}
		if (!getMembers().isEmpty()) {
			try {
				System.err.println("[SIM][SIMULATIONGROUP] current pos " + getSector(getMembers().get(0), new Vector3i()));
			} catch (EntityNotFountException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}
