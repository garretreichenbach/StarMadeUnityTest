package org.schema.game.client.view.character;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.creature.AICreature;
import org.schema.game.common.data.creature.CreaturePartNode;
import org.schema.schine.graphicsengine.animation.AnimationChannel;
import org.schema.schine.graphicsengine.animation.AnimationController;
import org.schema.schine.graphicsengine.animation.AnimationEventListener;
import org.schema.schine.graphicsengine.animation.LoopMode;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationIndex;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationIndexElement;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.resource.CreatureStructure.PartType;
import org.schema.schine.resource.ResourceLoadEntryMesh;

import java.util.Random;

public class CreaturePart extends BoneAttachable {

	public static final Random r = new Random();
	private PartType type;

	public CreaturePart(GameClientState state, Mesh mesh, BoneLocationInterface object) {
		super(mesh, state, object);
	}

	public void createHirachy(AICreature<?> creature) {
		CreaturePartNode creatureNode = creature.getCreatureNode();

		createChildren(creatureNode);

		setAnim(AnimationIndex.IDLING_FLOATING, 0, 0.0f);
		this.setSpeed(creature.getSpeed());
	}

	public void setPartAnimForced(PartType type, CreaturePartNode creatureNode,
	                              AnimationIndexElement selectedItem, boolean fullBody) throws AnimationNotSetException {
		if (this.type == type) {
			if (fullBody) {
				setAnimationState(selectedItem);
			} else {
				setAnimationStateTorso(selectedItem);
			}
		} else {
			for (BoneAttachable s : getAttachments().values()) {
				if (s instanceof CreaturePart) {
					((CreaturePart) s).setPartAnimForced(type, creatureNode, selectedItem, fullBody);

				}
			}
		}
	}

	public void setForcedLoopMode(PartType type,
	                              CreaturePartNode creatureNode, LoopMode mode, boolean fullBody) {
		if (this.type == type) {
			if (fullBody) {
				channel.setLoopMode(mode);
			}
			if (channelTorso != null) {
				channelTorso.setLoopMode(mode);
			}
		} else {
			for (BoneAttachable s : getAttachments().values()) {
				if (s instanceof CreaturePart) {
					((CreaturePart) s).setForcedLoopMode(type, creatureNode, mode, fullBody);

				}
			}
		}

	}

	public void setAnimSpeedForced(PartType type,
	                               CreaturePartNode creatureNode, float speed, boolean fullBody) {
		if (this.type == type) {
			setSpeed(speed);
			if (fullBody) {
				channel.setSpeed(speed);
			}
			if (channelTorso != null) {
				channelTorso.setSpeed(speed);
			}
		} else {
			for (BoneAttachable s : getAttachments().values()) {
				if (s instanceof CreaturePart) {
					((CreaturePart) s).setAnimSpeedForced(type, creatureNode, speed, fullBody);

				}
			}
		}

	}

	//	public void updateState(AnimationIndexElement animationState) {
	//		AnimationIndexElement oldState = this.animationState;
	//		this.animationState = animationState;
	//		if (animationState != oldState) {
	//
	//			setAnim(animationState, 0, 0.5f);
	//
	//			if (animationState.isType(Moving.class)) {
	//				channel.setSpeed(getSpeed());
	//				channelTorso.setSpeed(getSpeed());
	//			} else {
	//				channel.setSpeed(1);
	//				channelTorso.setSpeed(1);
	//			}
	//		}
	//	}
	//
	private void createChildren(CreaturePartNode creatureNode) {
		type = (creatureNode.type);
		if (mesh.getMaterial().getTexture() == null) {
			ResourceLoadEntryMesh r = state.getResourceMap().getMesh(mesh.getName());
			int indexOf = r.texture.getDiffuseMapNames().indexOf(creatureNode.texture);
			this.setTexture(r.texture.getDiffuseMaps().get(indexOf));
		} else {
			if (mesh.getSkin() != null) {
				this.setTexture(mesh.getMaterial().getTexture());
			}
		}
		getAttachments().clear();
		if (mesh.getSkin() != null) {
			for (CreaturePartNode n : creatureNode.getChields()) {
				Mesh m = (Mesh) Controller.getResLoader().getMesh(n.meshName).getChilds().get(0);
				CreaturePart chield = new CreaturePart(state, m, n);
				ResourceLoadEntryMesh meshResource = this.state.getResourceMap().getMesh(mesh.getParent().getName());
				String bone = switch(CreaturePartNode.AttachmentType.values()[n.attachedTo]) {
					case MAIN -> meshResource.creature.mainBone;
					case WEAPON -> meshResource.creature.rightHand;
				};
				System.err.println("[CREATUREPART] " + mesh.getName() + " WILL ATTACH " + chield.mesh);
				assert (mesh.getSkin().getSkeleton().getBone(bone) != null) : "BONE NOT FOUND: " + bone + " in " + mesh.getParent().getName();
				getAttachments().put(mesh.getSkin().getSkeleton().getBone(bone), chield);
				chield.createChildren(n);
			}
		} else {
			System.err.println("[CREATUREPART] WILL NOT ATTACH ON " + mesh.getName());
		}
	}

	@Override
	public void updateAnimation(Timer timer) {
		super.updateAnimation(timer);
		if (animationState != null) {

			updateRandomAnimation(timer, animationState);
			//			System.err.println("CURRENT FOR "+mesh.getName()+" "+getAnimName());
		}
	}

	public void updateRandomAnimation(Timer timer,
	                                  final AnimationIndexElement animationSt) {

		AnimationState animationState = new AnimationState(animationSt.get(getAnimations()), 0);

		String animationName = channel.getAnimationName();

		if (System.currentTimeMillis() - animationStarted > 5000) {
			if (r.nextInt(3) == 0) {

				if (animationState.state.animations.length > 0) {

					final int gravityIndex = r.nextInt(animationState.state.animations.length);
					final AnimationState newState = new AnimationState(animationState.state, gravityIndex);

					if (!this.animationState.get(getAnimations()).get().equals(newState.state.get())) {
						System.err.println("[ANIMATION] SETTING ANIMATION OF "
								+ mesh + " to " + newState.getAnimation() + "; index " + gravityIndex + " (tot: " + (animationState.state.animations.length) + ")");
						System.err.println("[ANIMATION] CURENT VS NEW: " + this.animationState.get(getAnimations()).get() + " -> " + newState.state.get());
						if (gravityIndex > 1) {
							controller.addListener(new AnimationEventListener() {

								@Override
								public void onAnimChange(
										AnimationController animationController,
										AnimationChannel channel, String name) {

								}

								@Override
								public void onAnimCycleDone(
										AnimationController animationController,
										AnimationChannel channel, String name) {
									// only execute if we are still idleing
									if (channel.getAnimationName()
											.equals(newState.getAnimation())) {
										setAnim(newState.state.getIndex(), gravityIndex, 0.5f);
									}
								}

								@Override
								public boolean removeOnFinished() {
									return true;
								}
							});
						}
						setAnim(newState.state.getIndex(), gravityIndex, 0.5f);
					}
				} else {
					System.err.println("no gravity animation");
				}
			}

			animationStarted = System.currentTimeMillis();
		}

	}

	//	public String[] getAnimations(AnimationState state){
	//		ResourceLoadEntry meshResource = this.state.getResourceMap().get(mesh.getParent().getName());
	//		AnimationStructure animationStructure = meshResource.animation;
	//		if(animationStructure != null){
	//		switch(state){
	//		case IDLELING_STANDING: return animationStructure.idling.gravity.animations;
	//		case ATTACKING_MEELEE: return animationStructure.upperBody.bareHand.meelee.animations;
	//		case CRAWLING: return animationStructure.getMoving().getByFoot().getCrawling();
	//		case FALLING:return animationStructure.getMoving().getFalling();
	//		case FLOATING:return animationStructure.getMoving().getNoGravity().getFloating();
	//		case IDLELING_OUTGRAVITY:return animationStructure.getIdle().getFloating();
	//		case JUMPING_DOWN:return animationStructure.getMoving().getJumping().getJumpDown();
	//		case JUMPING_UP:return animationStructure.getMoving().getJumping().getJumpUp();
	//		case LANDING_LONG:return animationStructure.getMoving().getLanding().getLongFall();
	//		case LANDING_MIDDLE:return animationStructure.getMoving().getLanding().getMiddleFall();
	//		case LANDING_SHORT:return animationStructure.getMoving().getLanding().getShortFall();
	//		case RUNNING:return animationStructure.getMoving().getByFoot().getRunning();
	//		case WALKING:return animationStructure.getMoving().getByFoot().getWalking();
	//		case WALKING_SLOW:return animationStructure.getMoving().getByFoot().getSlowWalking();
	//
	//			default:System.err.println("STATE NOT FOUND IN SWITCH "+state.name());return null;
	//		}
	//
	//		}else{
	//			System.err.println("NO ANIMATION FOR "+mesh);
	////			throw new NullPointerException();
	//			return null;
	//		}
	//	}

	/**
	 * @return the state
	 */
	public AnimationIndexElement getAnimationState() {
		return animationState;
	}

	/**
	 * @param state the state to set
	 * @throws AnimationNotSetException
	 */
	public void setAnimationState(AnimationIndexElement state) throws AnimationNotSetException {

		if (state == null || getAnimations() == null || state.get(getAnimations()) == null || state.get(getAnimations()).animations == null) {
			throw new AnimationNotSetException(state);
		}
		setAnim(state, 0, 0.5f);
		this.animationState = state;

	}

	public void setAnimationStateTorso(AnimationIndexElement state) throws AnimationNotSetException {

		if (state == null || getAnimations() == null || state.get(getAnimations()) == null || state.get(getAnimations()).animations == null) {
			throw new AnimationNotSetException(state);
		}
		setAnimTorso(state, 0, 0.5f);
		this.animationTorsoState = state;

	}

}
