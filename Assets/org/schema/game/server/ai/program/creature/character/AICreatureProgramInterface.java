package org.schema.game.server.ai.program.creature.character;

import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.ai.stateMachines.FSMException;

public interface AICreatureProgramInterface {

	public void underFire(SimpleTransformableSendableObject from) throws FSMException;

	public void changedOrder(SimpleTransformableSendableObject from) throws FSMException;

	public void enemyProximity(SimpleTransformableSendableObject from) throws FSMException;

	public void stopCurrent(SimpleTransformableSendableObject from) throws FSMException;

	public void onNoPath() throws FSMException;
}
