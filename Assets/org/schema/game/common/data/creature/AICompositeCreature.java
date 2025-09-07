package org.schema.game.common.data.creature;

import java.io.File;

import javax.vecmath.Vector3f;

import org.schema.game.common.controller.ai.AIRandomCreatureConfiguration;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.network.objects.TargetableAICreatureNetworkObject;
import org.schema.game.server.ai.CreatureAIEntity;
import org.schema.game.server.ai.CreatureDefaultAIEntity;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.resource.FileExt;

import com.bulletphysics.linearmath.Transform;

public abstract class AICompositeCreature extends AICreature<AICompositeCreaturePlayer> {

	private final AIRandomCreatureConfiguration aiRandomCreatureConfiguration;
	private float characterMargin = 0.1f;
	private TargetableAICreatureNetworkObject networkPlayerCharacterObject;

	public AICompositeCreature(StateInterface state) {
		super(state);
		aiRandomCreatureConfiguration = new AIRandomCreatureConfiguration(state, this);
	}



	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#cleanUpOnEntityDelete()
	 */
	@Override
	public void cleanUpOnEntityDelete() {
		super.cleanUpOnEntityDelete();
		System.err.println("[DELETE] Cleaning up playerCharacter for " + getOwnerState() + " on " + getState());
	}

	@Override
	public void destroyPersistent() {
		assert (isOnServer());

		File f = new FileExt(GameServerState.ENTITY_DATABASE_PATH + getUniqueIdentifier() + ".ent");
		f.delete();
	}

	@Override
	public Transform getShoulderWorldTransform() {
		Transform worldTransform = new Transform(super.getWorldTransform());
		Vector3f up = GlUtil.getUpVector(new Vector3f(), worldTransform);
		up.scale(shoulderUpScale);
		worldTransform.origin.add(up);
		return worldTransform;
	}

	@Override
	protected float getCharacterMargin() {
		return characterMargin;
	}

	@Override
	protected AICompositeCreaturePlayer instantiateOwnerState() {
		return new AICompositeCreaturePlayer(this);
	}

	@Override
	public String toNiceString() {
		Faction faction = ((FactionState) getState()).getFactionManager().getFaction(getFactionId());
		return super.toNiceString() +
				(faction != null && faction.isShowInHub() ? ("[" + faction.getName() + "]") : "")
				+ (getGravity().isGravityOn() ? "(" + getGravity().source + ")" + getGravity().accelToString() + "[" + getMass() + "]" : "");
	}

	@Override
	public CreatureAIEntity<AICompositeCreaturePlayer, ? extends AICreature<AICompositeCreaturePlayer>> instantiateAIEntity() {
		return new CreatureDefaultAIEntity("CRE_AI", this);
	}

	@Override
	public TargetableAICreatureNetworkObject getNetworkObject() {
		return networkPlayerCharacterObject;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.player.AbstractCharacter#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void updateLocal(Timer timer) {
		super.updateLocal(timer);
		getOwnerState().updateLocal(timer);

		//		if(isOnServer()){
		//			System.err.println("AIIIII: "+toString()+" "+toNiceString());
		//		}
	}

	@Override
	public Transform getHeadWorldTransform() {
		Transform worldTransform = new Transform(super.getWorldTransform());
		Vector3f up = GlUtil.getUpVector(new Vector3f(), worldTransform);
		up.scale(headUpScale);
		worldTransform.origin.add(up);
		return worldTransform;
	}

	@Override
	public String toString() {
		return "AIComCreature[(" + getUniqueIdentifier() + ")" + "(" + getId() + ")]";
	}

	@Override
	public AIRandomCreatureConfiguration getAiConfiguration() {
		return aiRandomCreatureConfiguration;
	}

	//	@Override
	//	public CreaturePartNode getCreatureNode() {
	////		return new CreaturePartNode("PlayerMdl", null);
	//		CreaturePartNode legs = new CreaturePartNode("LegsArag", null, "QuadLegsTex01");
	//		CreaturePartNode body = new CreaturePartNode("TorsoShell", "PelvisRoot", "QuadTorsoTex01");
	//		legs.attach(body);
	//		return legs;
	//	}

	@Override
	public void newNetworkObject() {
		ntInit(new TargetableAICreatureNetworkObject(getState(), getOwnerState()));
	}

	protected void ntInit(
			TargetableAICreatureNetworkObject nt) {
		networkPlayerCharacterObject = nt;
	}

}
