package org.schema.schine.ai.stateMachines;

import java.util.Random;

import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.objects.Sendable;

public abstract class AIGameEntityState<E extends Sendable> extends AiEntityState {

	/**
	 *
	 */
	
	private final E sendable;
	public Random random;
	public long seed;
	private long seedTime;

	public AIGameEntityState(String name, E s) {
		super(name, s.getState());
		this.sendable = s;
		random = new Random();
		seed = random.nextLong();
	}

	public E getEntity() {
		return sendable;
	}

	public float getShootingRange() {
		return 64;
	}
	
	public float getSalvageRange() {
		return 64;
	}

	@Override
	public boolean isActive() {
		return super.isActive() || (!isOnServer() && ((AiInterface) sendable).getAiConfiguration().isAIActiveClient());
	}
	public AIConfigurationInterface getAIConfig() {
		return ((AiInterface) getEntity()).getAiConfiguration();
	}
	
	/* (non-Javadoc)
	 * @see org.schema.schine.ai.stateMachines.AiEntityState#update()
	 */
	@Override
	public void updateOnActive(Timer timer) throws FSMException {
		super.updateOnActive(timer);
		if (isActive()) {
			if (isOnServer()) {
				updateAIServer(timer);

				if (System.currentTimeMillis() > seedTime) {
					seedTime = System.currentTimeMillis() + 10000;
					seed = random.nextLong();
				}
			} else {
				updateAIClient(timer);
			}
		}
	}
	
	public abstract void updateAIClient(Timer timer);

	public abstract void updateAIServer(Timer timer) throws FSMException;

	public float getAntiMissileShootingSpeed() {
		return 1;
	}

	public boolean canSalvage() {
		return false;
	}
}

