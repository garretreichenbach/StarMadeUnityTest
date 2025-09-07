package org.schema.game.client.view.character;

import java.util.Collection;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.client.view.effects.ConstantIndication;
import org.schema.game.client.view.gui.shiphud.HudIndicatorOverlay;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.MetaObjectManager.MetaObjectType;
import org.schema.game.common.data.element.meta.weapon.Weapon.WeaponSubType;
import org.schema.game.common.data.player.AbstractCharacterInterface;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.schine.graphicsengine.animation.LoopMode;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationIndex;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationIndexElement;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationStructEndPoint;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationStructure;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.forms.Skin;
import org.schema.schine.resource.CreatureStructure;
import org.schema.schine.resource.CreatureStructure.PartType;
import org.schema.schine.resource.ResourceLoadEntryMesh;

public abstract class AbstractDrawableSimplifiedHumanCharacter<E extends AbstractCharacterInterface> extends AbstractAnimatedObject<E> implements DrawableCharacterInterface {

	private final Vector3f minAABB = new Vector3f();
	private final Vector3f maxAABB = new Vector3f();
	protected boolean lastState;
	protected boolean lastBack;
	private Vector3f localTranslation = new Vector3f();
	private E ch;

	public AbstractDrawableSimplifiedHumanCharacter(E playerCharacter, Timer timer, GameClientState state) {
		super(timer, playerCharacter, state);
		this.ch = playerCharacter;
		this.indication = new ConstantIndication(playerCharacter.getWorldTransform(), "UNAMED THINGY");

		getBoneAttachable().setSpeed(2);
	}

	@Override
	public void setForcedAnimation(PartType type, AnimationIndexElement selectedItem, boolean fullBody) throws AnimationNotSetException {
		if (fullBody) {
			setAnim(selectedItem);
		} else {
			setAnimTorso(selectedItem);
		}
	}

	@Override
	public void setAnimSpeedForced(PartType type, float speed, boolean fullBody) {
		if (fullBody) {
			setAnimSpeed(speed);
		} else {
			setAnimTorsoSpeed(speed);
		}
	}
	@Override
	public boolean isInClientRange() {
		return ch.isInClientRange();
	}
	@Override
	public void setForcedLoopMode(PartType type, LoopMode mode, boolean fullBody) {
		if (fullBody) {
			setLoopMode(mode);
		} else {
			setLoopModeTorso(mode);
		}
	}

	@Override
	public void initManualAttachments() {
		attach(getRightHolster(), "Pistol");
		//		attach(getLeftHand(), "Pistol");
	}

	@Override
	protected Matrix3f getYawRotation() {
		if (ch.isSitting()) {

			Matrix3f aa = new Matrix3f();
			aa.setIdentity();

			Vector3i dir = new Vector3i();
			dir.sub(ch.getSittingPosTo(), ch.getSittingPos());

			Vector3i axis = new Vector3i();
			axis.sub(ch.getSittingPosTo(), ch.getSittingPosLegs());
			Vector3f up = new Vector3f(0, 1, 0);
			GlUtil.setUpVector(up, aa);

			Vector3f forward = new Vector3f();
			Element.getRelativeForward(Element.getSide(axis), Element.getSide(dir), forward);

			GlUtil.setForwardVector(forward, aa);

			Vector3f right = new Vector3f();
			right.cross(up, forward);

			GlUtil.setRightVector(right, aa);

			return aa;
		} else {
			return super.getYawRotation();
		}

	}

	@Override
	protected boolean skipYawRotation() {
		return ch.isSitting();
	}

	@Override
	public Vector3f getOrientationUp() {
		return ch.getOrientationUp();
	}

	@Override
	public Vector3f getOrientationRight() {
		return ch.getOrientationRight();
	}

	@Override
	public Vector3f getOrientationForward() {
		return ch.getOrientationForward();
	}

	@Override
	public void draw() {

		if (timer == null) {
			return;
		}

		super.draw();

	}

	@Override
	protected Vector3f getLocalTranslation() {
		localTranslation.y = ch.getCharacterHeightOffset();
		return localTranslation;
	}

	@Override
	public Skin getSkin() {
		return ((Mesh) Controller.getResLoader().getMesh("PlayerMdl").getChilds().get(0)).getSkin();
	}

	@Override
	public BoneAttachable initBoneAttachable(E creature) {
		return new BoneAttachable((Mesh) Controller.getResLoader().getMesh("PlayerMdl").getChilds().get(0), state, this);
	}

	@Override
	public AnimationStructEndPoint getAnimation(AnimationIndexElement ai) {
		Mesh mesh = (Mesh) Controller.getResLoader().getMesh("PlayerMdl").getChilds().get(0);
		ResourceLoadEntryMesh meshResource = this.state.getResourceMap().getMesh(mesh.getParent().getName());
		AnimationStructure animationStructure = meshResource.animation;
		assert (!ai.get(animationStructure).animations[0].equals("default")) : ai.get(animationStructure).getClass().getSimpleName();
		return ai.get(animationStructure);
	}

	@Override
	public void inititalizeHolders(
			Collection<AnimatedObjectHoldAnimation> holders) {
		holders.add(new AnimatedObjectHoldAnimation(
				this,
				AnimationIndex.UPPERBODY_FABRICATOR_DRAW,
				AnimationIndex.UPPERBODY_FABRICATOR_IDLE,
				AnimationIndex.UPPERBODY_FABRICATOR_AWAY,
				"Fabricator",
				getRightForeArm(),
				null
		) {

			@Override
			public boolean checkCondition() {
				
				return ch.isAnyBlockSelected();
			}
		});

		holders.add(new AnimatedObjectHoldAnimation(
				this,
				AnimationIndex.UPPERBODY_GUN_DRAW,
				AnimationIndex.UPPERBODY_GUN_IDLE,
				AnimationIndex.UPPERBODY_GUN_AWAY,
				"Pistol",
				getRightHand(),
				getRightHolster()
		) {

			@Override
			public boolean checkCondition() {

				return ch.isAnyMetaObjectSelected() && 
						ch.isMetaObjectSelected(MetaObjectType.WEAPON, -1) && 
						!ch.isMetaObjectSelected(MetaObjectType.WEAPON, WeaponSubType.TORCH.type);
			}
		});
		
		holders.add(new AnimatedObjectHoldAnimation(
				this,
				AnimationIndex.UPPERBODY_GUN_DRAW,
				AnimationIndex.UPPERBODY_GUN_IDLE,
				AnimationIndex.UPPERBODY_GUN_AWAY,
				"Torch",
				getRightHand(),
				getRightHolster()
		) {

			@Override
			public boolean checkCondition() {

				return ch.isAnyMetaObjectSelected() && 
						ch.isMetaObjectSelected(MetaObjectType.WEAPON, WeaponSubType.TORCH.type);
			}
		});
		holders.add(new AnimatedObjectHoldAnimation(
				this,
				AnimationIndex.UPPERBODY_GUN_DRAW,
				AnimationIndex.UPPERBODY_GUN_IDLE,
				AnimationIndex.UPPERBODY_GUN_AWAY,
				"HealingBeam",
				getRightHand(),
				getRightHolster()
				) {
			
			@Override
			public boolean checkCondition() {
				
				return ch.isAnyMetaObjectSelected() && 
						ch.isMetaObjectSelected(MetaObjectType.WEAPON, WeaponSubType.HEAL.type);
			}
		});
		holders.add(new AnimatedObjectHoldAnimation(
				this,
				AnimationIndex.UPPERBODY_GUN_DRAW,
				AnimationIndex.UPPERBODY_GUN_IDLE,
				AnimationIndex.UPPERBODY_GUN_AWAY,
				"MarkerBeam",
				getRightHand(),
				getRightHolster()
				) {
			
			@Override
			public boolean checkCondition() {
				
				return ch.isAnyMetaObjectSelected() && 
						ch.isMetaObjectSelected(MetaObjectType.WEAPON, WeaponSubType.MARKER.type);
			}
		});
		holders.add(new AnimatedObjectHoldAnimation(
				this,
				AnimationIndex.UPPERBODY_GUN_DRAW,
				AnimationIndex.UPPERBODY_GUN_IDLE,
				AnimationIndex.UPPERBODY_GUN_AWAY,
				"GrappleBeam",
				getRightHand(),
				getRightHolster()
				) {
			
			@Override
			public boolean checkCondition() {
				
				return ch.isAnyMetaObjectSelected() && 
						ch.isMetaObjectSelected(MetaObjectType.WEAPON, WeaponSubType.GRAPPLE.type);
			}
		});
	}

	@Override
	protected void handleState() {

//		handleState(timer, playerState);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.character.AbstractAnimatedObject#getCreatureStructure()
	 */
	@Override
	public CreatureStructure getCreatureStructure() {
		return state.getResourceMap().getMesh("PlayerMdl").creature;
	}

	@Override
	public void cleanUp() {
		
	}

	@Override
	public boolean isInvisible() {

		return ch.isHidden();
	}

	protected abstract void handleTextureUpdate(Mesh mesh, E oState);


	@Override
	public void onRemove() {
		HudIndicatorOverlay.toDrawTexts.remove(indication);
	}

	@Override
	public void update(Timer timer) {
		try {


			if (getBoneAttachable() != null ) {
				for (AnimatedObjectHoldAnimation a : holders) {
					a.apply();
				}

			} else {
			}

		} catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
		}

	}

	@Override
	public int getId() {
		return ch.getId();
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
			ch.getClientAABB(minAABB, maxAABB);
			return Controller.getCamera().isAABBInFrustum(minAABB, maxAABB);
		} else {
			return true;
		}
	}

	protected abstract void handleState(Timer timer, AbstractOwnerState state);



	
	public boolean handleSitting() {
		if (getEntity().isSitting()) {
			boolean onFloor = false;
			boolean wedge = false;
			if (getEntity().getGravity().source != null && getEntity().getGravity().source instanceof SegmentController) {
				SegmentPiece pointLegs = ((SegmentController) getEntity().getGravity().source).getSegmentBuffer().getPointUnsave(ch.getSittingPosLegs());
				onFloor = pointLegs != null && pointLegs.getType() != Element.TYPE_NONE;

				SegmentPiece pointSit = ((SegmentController) getEntity().getGravity().source).getSegmentBuffer().getPointUnsave(ch.getSittingPos());
				wedge = pointSit != null && pointSit.getType() != Element.TYPE_NONE && ElementKeyMap.getInfo(pointSit.getType()).getBlockStyle() == BlockStyle.WEDGE;

			}

			if (wedge) {
				if (onFloor) {
					if (!isAnim(AnimationIndex.SITTING_WEDGE_FLOOR_IDLE)) {
						setAnim(AnimationIndex.SITTING_WEDGE_FLOOR_IDLE, 0.5f);
					}
				} else {
					if (!isAnim(AnimationIndex.SITTING_WEDGE_NOFLOOR_IDLE)) {
						setAnim(AnimationIndex.SITTING_WEDGE_NOFLOOR_IDLE, 0.5f);
					}
				}
			} else {
				if (onFloor) {
					if (!isAnim(AnimationIndex.SITTING_BLOCK_FLOOR_IDLE)) {
						setAnim(AnimationIndex.SITTING_BLOCK_FLOOR_IDLE, 0.5f);
					}
				} else {
					if (!isAnim(AnimationIndex.SITTING_BLOCK_NOFLOOR_IDLE)) {
						setAnim(AnimationIndex.SITTING_BLOCK_NOFLOOR_IDLE, 0.5f);
					}
				}
			}
			return true;
		}
		return false;
	}
}
