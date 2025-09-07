package org.schema.game.server.ai.program.common;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.SimpleGameObject;
import org.schema.game.common.data.world.Universe;
import org.schema.schine.ai.MachineProgram;
import org.schema.schine.ai.stateMachines.AiEntityState;

public abstract class TargetProgram<E extends AiEntityState> extends
		MachineProgram<E> {

	private Vector3i sectorTarget;

	private SimpleGameObject target;

	private int specificTargetId = -1;

	private long targetAquiredTime;

	private int targetHoldTime;

	public TargetProgram(E entityState, boolean startSuspended) {
		super(entityState, startSuspended);
	}

	/**
	 * @return the sectorTarget
	 */
	public Vector3i getSectorTarget() {
		return sectorTarget;
	}

	/**
	 * @param sectorTarget the sectorTarget to set
	 */
	public void setSectorTarget(Vector3i sectorTarget) {
		//		try{
		//		throw new NullPointerException("SET SECTOR TO "+sectorTarget);
		//		}catch (Exception e) {
		//			e.printStackTrace();
		//		}
		this.sectorTarget = sectorTarget;
	}

	/**
	 * @return the specificTargetId
	 */
	public int getSpecificTargetId() {
		return specificTargetId;
	}

	/**
	 * @param specificTargetId the specificTargetId to set
	 */
	public void setSpecificTargetId(int specificTargetId) {
		this.specificTargetId = specificTargetId;
	}

	/**
	 * @return the target
	 */
	public SimpleGameObject getTarget() {
		return target;
	}

	/**
	 * @param target the target to set
	 */
	public void setTarget(SimpleGameObject target) {
		if (this.target != target) {
			targetAquiredTime = System.currentTimeMillis();
			targetHoldTime = Universe.getRandom().nextInt(3000);
		}
		this.target = target;
	}

	/**
	 * @return the targetAquiredTime
	 */
	public long getTargetAquiredTime() {
		return targetAquiredTime;
	}

	/**
	 * @return the targetHoldTime
	 */
	public int getTargetHoldTime() {
		return targetHoldTime;
	}

}
