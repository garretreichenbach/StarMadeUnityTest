package org.schema.game.server.ai.program.simpirates.states;

import java.sql.SQLException;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.world.Universe;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.controller.EntityNotFountException;
import org.schema.game.server.data.simulation.groups.SimulationGroup;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

public class MovingToSector extends SimulationGroupState<SimulationGroup> {

	/**
	 *
	 */
	
	private long lastMove;

	public MovingToSector(AiEntityStateInterface gObj) {
		super(gObj);
	}

	@Override
	public boolean onEnter() {
				return false;
	}

	@Override
	public boolean onExit() {
				return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {
		TargetProgram<?> p = ((TargetProgram<?>) getEntityState().getCurrentProgram());
		if (p.getSectorTarget() == null) {
			stateTransition(Transition.RESTART);
			return true;
		}

		Vector3i pos = new Vector3i();
		for (int i = 0; i < getSimGroup().getMembers().size(); i++) {
			try {
				getSimGroup().getSector(getSimGroup().getMembers().get(i), pos);

				if (pos.equals(p.getSectorTarget())) {
					stateTransition(Transition.TARGET_SECTOR_REACHED);
					return true;
				}

			} catch (EntityNotFountException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		if (System.currentTimeMillis() - lastMove > SimulationGroup.SECTOR_SPEED_MS) {
			boolean existsGroupInSector = getSimGroup().getState().getSimulationManager().existsGroupInSector(p.getSectorTarget());
			boolean occ = false;
			for (int i = 0; i < getSimGroup().getMembers().size(); i++) {
				boolean noError;
				if (getSimGroup().getState().getUniverse().isSectorLoaded(p.getSectorTarget())) {
					//always go in loaded sector
					noError = getSimGroup().moveToTarget(getSimGroup().getMembers().get(i), p.getSectorTarget());
				} else {
					if (existsGroupInSector) {
						//wait
						occ = true;
						noError = true;
						Vector3i rnd = new Vector3i();
						Vector3i dir = Element.DIRECTIONSi[Universe.getRandom().nextInt(Element.DIRECTIONSi.length)];
						try {
							Vector3i secPos = getSimGroup().getSector(getSimGroup().getMembers().get(i), new Vector3i());
							secPos.add(dir);
							//
							getSimGroup().moveToTarget(getSimGroup().getMembers().get(i), secPos);
						} catch (EntityNotFountException e) {
							e.printStackTrace();
						} catch (SQLException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						noError = getSimGroup().moveToTarget(getSimGroup().getMembers().get(i), p.getSectorTarget());
					}
				}
				if (!noError) {
					System.err.println("[SIMULATION] Exception while moving entity: REMOVING FROM MEMBERS: " + getSimGroup().getMembers().get(i));
					getSimGroup().getMembers().remove(i);
					i--;
				}
				lastMove = System.currentTimeMillis();
			}
			if (occ) {
				System.err.println("[MOVING TO SECTOR] Position " + p.getSectorTarget() + " occupied for " + getSimGroup().getMembers());
			}
		}

		return false;
	}

}
