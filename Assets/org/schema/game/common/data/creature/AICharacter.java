package org.schema.game.common.data.creature;

import java.io.File;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.gui.shiphud.newhud.ColorPalette;
import org.schema.game.common.controller.ai.AICharacterConfiguration;
import org.schema.game.common.data.SendableTypes;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.element.meta.MetaObjectManager;
import org.schema.game.common.data.element.meta.MetaObjectManager.MetaObjectType;
import org.schema.game.common.data.element.meta.weapon.LaserWeapon;
import org.schema.game.common.data.element.meta.weapon.Weapon.WeaponSubType;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.game.network.objects.TargetableAICreatureNetworkObject;
import org.schema.game.server.ai.AIControllerStateUnit;
import org.schema.game.server.ai.CharacterAIEntity;
import org.schema.game.server.ai.CreatureAIEntity;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.SendableType;
import org.schema.schine.network.StateInterface;
import org.schema.schine.resource.CreatureStructure.PartType;
import org.schema.schine.resource.FileExt;

import com.bulletphysics.linearmath.Transform;

public class AICharacter extends AICreature<AICharacterPlayer> {

	public static final float headUpScale = 0.485f;
	public static final float shoulderUpScale = 0.385f;
	private final AICharacterConfiguration aiCharacterConfiguration;
	private float characterWidth = 0.2f;
	private float characterHeight = 1.13f;
	private float characterMargin = 0.1f;
	private float characterHeightOffset = 0.2f;
	private TargetableAICreatureNetworkObject networkPlayerCharacterObject;
	private float speed = 4;
	private Vector3i blockDim = new Vector3i(1, 2, 1);
	private boolean meelee = false;

	public AICharacter(StateInterface state) {
		super(state);

		aiCharacterConfiguration = new AICharacterConfiguration(state, this);
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
	public float getCharacterHeightOffset() {
		return characterHeightOffset;
	}

	@Override
	public float getCharacterHeight() {
		return characterHeight;
	}

	@Override
	public float getCharacterWidth() {
		return characterWidth;
	}

	@Override
	public Vector3i getBlockDim() {
		return blockDim;
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
	public float getSpeed() {
		return speed;
	}

	@Override
	public void setSpeed(float speed) {
		this.speed = speed;
	}

	@Override
	protected AICharacterPlayer instantiateOwnerState() {
		return new AICharacterPlayer(this);
	}

	@Override
	public boolean isMeleeAttacker() {
		return meelee;
	}

	
	@Override
	public void getRelationColor(RType relation, boolean sameFaction, Vector4f out, float select, float pulse) {
		switch(relation) {
			case ENEMY -> out.set(ColorPalette.enemyLifeform);
			case FRIEND -> out.set(ColorPalette.allyLifeform);
			case NEUTRAL -> out.set(ColorPalette.neutralLifeform);
		}
		if(sameFaction) {
			out.set(ColorPalette.factionLifeform);
		}
		out.x += select;
		out.y += select;
		out.z += select;
	}
	@Override
	public String toNiceString() {
		//			return "NPC"+(getGravity().isGravityOn() ? "("+getGravity().source+")"+getGravity().acceleration+"["+getMass()+"]" : (getGravity().isAligedOnly() ? "(aligned: "+getGravity().source+")" : ""))+super.toNiceString();
		return "NPC[" + super.toNiceString() + "]";
	}

	@Override
	public void initialFillInventory() {
		MetaObject weapon = MetaObjectManager.instantiate(MetaObjectType.WEAPON.type, WeaponSubType.LASER.type, true);
		((LaserWeapon) weapon).getColor().set(0, 1, 0, 1);
		getOwnerState().getInventory().put(1, weapon);
	}

	@Override
	public CreatureAIEntity<AICharacterPlayer, ? extends AICreature<AICharacterPlayer>> instantiateAIEntity() {
		return new CharacterAIEntity("NPC_AI", this);
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

		//		if(isOnServer() && ((PairCachingGhostObjectAlignable)getPhysicsDataContainer().getObject()).getAttached() != null){
		//			System.err.println("[AICHAR]"+getState()+"; "+getWorldTransform().origin+"; "+
		//			((PairCachingGhostObjectAlignable)getPhysicsDataContainer().getObject()).localWorldTransform.origin);
		//
		//			if(!getPhysics().containsObject(((PairCachingGhostObjectAlignable)getPhysicsDataContainer().getObject()).getAttached().getPhysicsDataContainer().getObject())){
		//				throw new NullPointerException();
		//			}
		//		}
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
		return "AICharacter[(" + getUniqueIdentifier() + ")(" + getName()+")(" + getId() + ")]";
	}

	@Override
	public CreaturePartNode getCreatureNode() {
		//		return new CreaturePartNode("PlayerMdl", null);
		CreaturePartNode legs = new CreaturePartNode(PartType.BOTTOM, getState(), "PlayerMdl", null);
		//		CreaturePartNode body = new CreaturePartNode("TorsoShell", "PelvisRoot", "QuadTorsoTex01");
		//		legs.attach(body);
		return legs;
	}

	@Override
	public AICharacterConfiguration getAiConfiguration() {
		return aiCharacterConfiguration;
	}

	@Override
	public void newNetworkObject() {
		networkPlayerCharacterObject = new TargetableAICreatureNetworkObject(getState(), getOwnerState());
	}



	@Override
	public void handleControl(Timer timer,
			AIControllerStateUnit<AICreature<AICharacterPlayer>> unit) {
		
	}



	@Override
	public SendableType getSendableType() {
		return SendableTypes.AI_CHARACTER;
	}



	



	






	

}
