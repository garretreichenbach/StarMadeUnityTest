package org.schema.game.client.view.character;

import java.util.Collection;

import javax.vecmath.Vector3f;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.creature.AICreature;
import org.schema.game.common.data.creature.AIPlayer;
import org.schema.game.common.data.creature.CreaturePartNode;
import org.schema.game.common.data.player.PlayerSkin;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.animation.LoopMode;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationIndex;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationIndexElement;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationStructEndPoint;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.forms.Skin;
import org.schema.schine.network.StateInterface;
import org.schema.schine.resource.CreatureStructure;
import org.schema.schine.resource.CreatureStructure.PartType;

import com.bulletphysics.linearmath.Transform;

public class AbstractDrawableCreature<E extends AIPlayer> extends AbstractAnimatedObject<AICreature<?>> implements DrawableCharacterInterface, BoneLocationInterface {

	private final Vector3f minAABB = new Vector3f();
	private final Vector3f maxAABB = new Vector3f();
	private CreaturePart creaturePart;
	private Vector3f localTranslation = new Vector3f();

	public AbstractDrawableCreature(AICreature<E> creature, Timer timer, GameClientState state) {
		super(timer, creature, state);
		assert (creature != null);
	}

	@Override
	public void cleanUp() {
		
	}

	@Override
	public boolean isInvisible() {
				return false;
	}

	@Override
	public void onRemove() {
		
	}



	@Override
	public boolean isInClientRange() {
		return getEntity().isInClientRange();
	}

	@Override
	public void update(Timer timer) {
		if (getBoneAttachable() != null) {
			handleState();
			for (AnimatedObjectHoldAnimation a : holders) {
				a.apply();
			}

		} else {
		}
	}

	@Override
	public int getId() {
				return 0;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.character.DrawableCharacterInterface#isInFrustum()
	 */
	@Override
	public boolean isInFrustum() {
		if (timer == null) {
			return false;
		}
		if (!isShadowMode()) {
			creature.getPhysicsDataContainer().getShape().getAabb(creature.getWorldTransformOnClient(), minAABB, maxAABB);
			return Controller.getCamera().isAABBInFrustum(minAABB, maxAABB);
		} else {
			return true;
		}

	}

	@Override
	public Vector3f getForward() {
		return creature.getOwnerState().getForward(new Vector3f());
	}

	@Override
	public Vector3f getUp() {
		return creature.getOwnerState().getUp(new Vector3f());
	}

	@Override
	public Vector3f getRight() {
		return creature.getOwnerState().getRight(new Vector3f());
	}

	@Override
	public void setForcedAnimation(PartType type, AnimationIndexElement selectedItem, boolean fullBody) throws AnimationNotSetException {
		CreaturePartNode creatureNode = creature.getCreatureNode();
		creaturePart.setPartAnimForced(type, creatureNode, selectedItem, fullBody);
	}

	@Override
	public void setAnimSpeedForced(PartType type, float speed, boolean fullBody) {
		CreaturePartNode creatureNode = creature.getCreatureNode();
		creaturePart.setAnimSpeedForced(type, creatureNode, speed, fullBody);
	}

	@Override
	public void setForcedLoopMode(PartType type, LoopMode mode, boolean fullBody) {
		CreaturePartNode creatureNode = creature.getCreatureNode();
		creaturePart.setForcedLoopMode(type, creatureNode, mode, fullBody);
	}

	@Override
	public Vector3f getOrientationUp() {
		return creature.getCharacterController().upAxisDirection[1];
	}

	@Override
	public Vector3f getOrientationRight() {
		return creature.getCharacterController().upAxisDirection[0];
	}

	@Override
	public Vector3f getOrientationForward() {
		return creature.getCharacterController().upAxisDirection[2];
	}

	@Override
	public Transform getWorldTransform() {
		return creature.getWorldTransformOnClient();
	}

	@Override
	protected float getScale() {
		return creature.getScale();
	}

	@Override
	protected AnimationIndexElement getAnimationState() {
		return creature.getAnimationState();
	}

	@Override
	public SimpleTransformableSendableObject getGravitySource() {
		return getEntity().getGravity().source;
	}

	@Override
	protected PlayerSkin getPlayerSkin() {
				return null;
	}

	@Override
	protected Vector3f getLocalTranslation() {
		return localTranslation;
	}

	@Override
	public Skin getSkin() {
		assert (creature.getCreatureNode() != null) : creature;
		assert (Controller.getResLoader().getMesh(creature.getCreatureNode().meshName) != null) : "not found " + creature.getCreatureNode().meshName;
		Mesh mesh = (Mesh) Controller.getResLoader().getMesh(creature.getCreatureNode().meshName).getChilds().get(0);

		assert (mesh.getSkin() != null) : "skin not found " + creature.getCreatureNode().meshName;
		return mesh.getSkin();
	}

	@Override
	public BoneAttachable initBoneAttachable(AICreature<?> creature) {
		assert (creature != null);
		CreaturePartNode creatureNode = creature.getCreatureNode();

		assert (creatureNode != null);
		Mesh mesh = Controller.getResLoader().getMesh(creatureNode.meshName);
		assert (mesh != null) : creatureNode.meshName;
		creaturePart = new CreaturePart((GameClientState) creature.getState(),
				(Mesh) (mesh.getChilds().get(0)), this);
		creaturePart.createHirachy(creature);
		return creaturePart;
	}

	@Override
	public AnimationStructEndPoint getAnimation(AnimationIndexElement ai) {
		return ai.get(creaturePart.getAnimations());
	}

	@Override
	public void inititalizeHolders(
			Collection<AnimatedObjectHoldAnimation> holders) {
		
	}

	@Override
	protected void handleState() {
		if (getEntity().isAttacking()) {
			if (!isAnim(AnimationIndex.UPPERBODY_BAREHAND_MEELEE)) {
				setAnim(AnimationIndex.UPPERBODY_BAREHAND_MEELEE, LoopMode.CYCLE);
			}
		} else {
			if (isAnim(AnimationIndex.UPPERBODY_BAREHAND_MEELEE)) {
				setAnim(AnimationIndex.IDLING_FLOATING);
			}
		}
	}

	@Override
	public CreatureStructure getCreatureStructure() {
		return state.getResourceMap().getMesh(creature.getCreatureNode().meshName).creature;
	}

	@Override
	public String getRootBoneName() {
		assert (getSkin() != null) : creature;
		assert (getSkin().getSkeleton() != null) : creature;
		assert (getSkin().getSkeleton().getRootBone() != null) : creature;
		return getSkin().getSkeleton().getRootBone().name;
	}

	@Override
	public String getRootTorsoBoneName() {
		return getSkin().getSkeleton().getRootBone().name;
	}

	public void setForcedAnimation(PartType type, AnimationIndexElement selectedItem) throws AnimationNotSetException {
		setForcedAnimation(type, selectedItem, true);
	}

	public void setAnimSpeedForced(PartType type, float speed) {
		setAnimSpeedForced(type, speed, true);
	}

	public void setForcedLoopMode(PartType type, LoopMode mode) {
		setForcedLoopMode(type, mode, true);
	}

	@Override
	public void loadClientBones(StateInterface state) {
		
	}

	//	@Override
	//	public String getAnimation(AnimationIndexElement ai) {
	//		return creaturePart.getAnimation(ai);
	//	}

}

	
