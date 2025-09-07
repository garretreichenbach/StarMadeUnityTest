package org.schema.game.client.view.character;

import java.util.Map.Entry;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.glfw.GLFW;
import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerSkin;
import org.schema.schine.graphicsengine.animation.AnimationChannel;
import org.schema.schine.graphicsengine.animation.AnimationController;
import org.schema.schine.graphicsengine.animation.LoopMode;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationIndexElement;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationStructure;
import org.schema.schine.graphicsengine.animation.structure.classes.Moving;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Bone;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.texture.Texture;
import org.schema.schine.input.Keyboard;
import org.schema.schine.resource.ResourceLoadEntryMesh;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

public class BoneAttachable {
	protected final GameClientState state;
	final Mesh mesh;
	private final Object2ObjectArrayMap<Bone, BoneAttachable> attachments = new Object2ObjectArrayMap<Bone, BoneAttachable>();
	protected AnimationIndexElement animationState;
	protected AnimationIndexElement animationTorsoState;
	protected AnimationController controller;
	protected AnimationChannel channel;
	protected AnimationChannel channelTorso;
	protected long animationStarted;
	private Texture texture;
	private float speed = 4;
	//	protected final AbstractAnimatedObject object;
	private Texture emissiveTexture;

	public BoneAttachable(Mesh mesh, GameClientState state, final BoneLocationInterface object) {
		super();
		this.state = state;
		assert (state != null);
		this.mesh = mesh;
		this.texture = mesh.getMaterial().getTexture();
		this.emissiveTexture = mesh.getMaterial().getEmissiveTexture();

		if (mesh.getSkin() != null) {
			controller = new AnimationController(mesh.getSkin());
			channel = controller.createChannel();
			channelTorso = controller.createChannel();
			if (object.getRootBoneName() == null) {
				object.loadClientBones(state);
			}
			assert (object.getRootBoneName() != null) : object + "; " + mesh;
			channel.addFromRootBone(object.getRootBoneName());
			channelTorso.addFromRootBone(object.getRootTorsoBoneName());
			if (object.getHeldBoneName() != null && !object.getHeldBoneName().equals("none")) {
				//torso needs this bone for teh helmet animation for example
				channelTorso.addBone(object.getHeldBoneName());
			}
			channelTorso.setOverwritePreviousAnimation(true);

			object.initializeListeners(controller, channel, channelTorso);

		}
	}

	public void setAnim(AnimationIndexElement e, int animationIndex, float blendTime) {
		assert (e.get(getAnimations()) != null) : e + "; " + getAnimations();
		AnimationState anim = new AnimationState(e.get(getAnimations()), animationIndex);
		try {
			//			try{
			//				throw new NullPointerException("ANIMATION CHANGED TO "+anim.getAnimation());
			//			}catch(Exception se){
			//				se.printStackTrace();
			//			}
			channel.setAnim(anim.getAnimation(), blendTime);
			animationStarted = System.currentTimeMillis();
			animationState = e;

			if (animationState.isType(Moving.class)) {
				channel.setSpeed(speed);
				channelTorso.setSpeed(speed);
			} else {
				channel.setSpeed(1);
				channelTorso.setSpeed(1);
			}
		} catch (RuntimeException ex) {
			ex.printStackTrace();
			System.err.println("EXCEPTION ON ANIM");
			assert (mesh != null);
			assert (anim.state != null);
			System.err.println("EXCEPTION WHEN SETTING: " + anim.state.getClass().getSimpleName() + ": " + anim.state.animations[anim.anim] + " in " + mesh.getName());
			throw ex;
		}
	}

	public void setAnim(AnimationIndexElement e, int animationIndex, float blendTime, LoopMode loopMode) {
		AnimationState anim = new AnimationState(e.get(getAnimations()), animationIndex);
		channel.setAnim(anim.getAnimation(), blendTime);
		animationStarted = System.currentTimeMillis();
		channel.setLoopMode(loopMode);
		animationState = e;
	}

	public void setAnimTorso(AnimationIndexElement e, int animationIndex, float blendTime) {
		AnimationState anim = new AnimationState(e.get(getAnimations()), animationIndex);
		channelTorso.setAnim(anim.getAnimation(), blendTime);
		animationTorsoState = e;
	}

	public void setAnimTorso(AnimationIndexElement e, int animationIndex, float blendTime, LoopMode loopMode) {
		AnimationState anim = new AnimationState(e.get(getAnimations()), animationIndex);
		channelTorso.setAnim(anim.getAnimation(), blendTime);
		channelTorso.setLoopMode(loopMode);
		animationTorsoState = e;
	}

	public boolean isAnimTorso(AnimationIndexElement e) {
		return channelTorso.isActive() && animationTorsoState == e;
	}

	public boolean isAnim(AnimationIndexElement e) {
		return channel.isActive() && animationState == e;
	}

	public void draw(Timer timer, PlayerSkin skin, Vector4f tint) {
		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
			GlUtil.printGlErrorCritical();
		}
		applyTexture(mesh, skin);
		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
			GlUtil.printGlErrorCritical();
		}
		updateController(timer);
		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
			GlUtil.printGlErrorCritical();
		}
		mesh.getSkin().spotActivated = state.getWorldDrawer().isSpotLightSupport() == 0;
		mesh.getSkin().color.set(tint);
		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
			GlUtil.printGlErrorCritical();
		}
		mesh.drawVBO(false);
		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
			GlUtil.printGlErrorCritical();
		}
		drawAttached(timer, skin, tint);
	}

	public void drawAttached(Timer timer, PlayerSkin skin, Vector4f tint) {
		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
			GlUtil.printGlErrorCritical();
		}
		for (Entry<Bone, BoneAttachable> en : attachments.entrySet()) {
			BoneAttachable bo = en.getValue();
			Mesh mesh = bo.mesh;
			Bone bone = en.getKey();
			GlUtil.glPushMatrix();
			Transform t = new Transform();
			if (mesh.getSkin() != null) {

				mesh.getSkin().getSkeleton().updateAttachment(timer, bone);

				t.origin.set(mesh.getSkin().getSkeleton().getRootBone().worldPos);
				t.basis.set(mesh.getSkin().getSkeleton().getRootBone().worldRot);

				Matrix4f m = new Matrix4f();
				Controller.getMat(t, m);
				Matrix4fTools.scale(m, new Vector3f(
						mesh.getSkin().getSkeleton().getRootBone().worldScale.x,
						mesh.getSkin().getSkeleton().getRootBone().worldScale.y,
						mesh.getSkin().getSkeleton().getRootBone().worldScale.z));

//				System.err.println("HHMMAMMM MMAMM MMAMM \n"+m);

				Controller.getMat(m, t);
				//				if(bone.worldScale.length() < 0.5f){
				//					System.err.println("ATTACHED SCALE: "+bone.worldScale);
				//				}

			} else {

//				System.err.println("##########HHMMAMMM MMAMM MMAMM \n"+mesh.getName());

				t.origin.set(bone.worldPos);
				t.basis.set(bone.worldRot);

				Matrix4f m = new Matrix4f();
				Controller.getMat(t, m);
				Matrix4fTools.scale(m, new Vector3f(
						bone.worldScale.x,
						bone.worldScale.y,
						bone.worldScale.z));

				Controller.getMat(m, t);
			}

			GlUtil.glMultMatrix(t);
			bo.applyTexture(mesh, skin);
			bo.updateController(timer);
			if (mesh.getSkin() != null) {
				mesh.getSkin().color.set(tint);
			}
			mesh.drawVBO(false);
			bo.drawAttached(timer, skin, tint);

			GlUtil.glPopMatrix();
		}

	}

	protected void updateController(Timer timer) {
		if (controller != null) {
			controller.update(timer);
		}
	}

	protected void applyTexture(Mesh mesh, PlayerSkin skin) {
		if (skin != null && skin.containsMesh(mesh)) {
			if (mesh.getSkin() != null) {
				//				System.err.println("DRAWING FROM SKIN");
				mesh.getSkin().setDiffuseTexId(skin.getDiffuseIdFor(mesh));
				mesh.getSkin().setEmissiveTexId(skin.getEmissiveIdFor(mesh));
			} else {
				mesh.getMaterial().setTexture(texture);
			}
		} else {
			if (this.texture != null) {
				if (mesh.getSkin() != null) {
					mesh.getSkin().setDiffuseTexId(texture.getTextureId());
					if (emissiveTexture != null) {
						mesh.getSkin().setEmissiveTexId(emissiveTexture.getTextureId());
					}
				} else {
					mesh.getMaterial().setTexture(texture);
				}
			} else {
				if (mesh.getSkin() == null) {
					mesh.getMaterial().attach(0);
				}
			}
		}
	}

	public void updateState(AnimationIndexElement animationState) {
	}

	public void updateAnimation(Timer timer) {
	}

	public AnimationStructure getAnimations() {
		assert (mesh != null);
		assert (mesh.getParent() != null) : mesh.getName();
		assert (mesh.getParent().getName() != null) : mesh.getName();
		assert (mesh.getParent().getName() != null) : mesh.getName();
		assert (this.state != null) : mesh.getName();
		assert (this.state.getResourceMap() != null) : mesh.getName();
		assert (this.state.getResourceMap().get(mesh.getParent().getName()) != null) : mesh.getParent().getName();
		;
		ResourceLoadEntryMesh meshResource = this.state.getResourceMap().getMesh(mesh.getParent().getName());
		AnimationStructure animationStructure = meshResource.animation;
		return animationStructure;
	}

	public String getAnimation(AnimationIndexElement ai) {
		return ai.get(getAnimations()).animations[0];
	}

	/**
	 * @return the speed
	 */
	public float getSpeed() {
		return speed;
	}

	/**
	 * @param speed the speed to set
	 */
	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public boolean isAnimActive() {
		return channel.isActive();
	}

	public float getAnimTime() {
		return channel.getTime();
	}

	public void setAnimTime(float time) {
		channel.setTime(time);
	}

	public boolean isAnimTorsoActive() {
		return channelTorso.isActive();
	}

	public float getAnimTorsoTime() {
		return channelTorso.getTime();
	}

	public void setAnimTorsoTime(float time) {
		channelTorso.setTime(time);
	}

	public float getAnimTorsoMaxTime() {
		return channelTorso.getAnimMaxTime();
	}

	public float getAnimMaxTime() {
		return channel.getAnimMaxTime();
	}

	/**
	 * @return the texture
	 */
	public Texture getTexture() {
		return texture;
	}

	/**
	 * @param texture the texture to set
	 */
	public void setTexture(Texture texture) {
		this.texture = texture;
	}

	public boolean isEqualMeshFromResourceName(String meshResource) {
		return mesh.getParent().getName().equals(meshResource);
	}

	/**
	 * @return the emissiveTexture
	 */
	public Texture getEmissiveTexture() {
		return emissiveTexture;
	}

	/**
	 * @param emissiveTexture the emissiveTexture to set
	 */
	public void setEmissiveTexture(Texture emissiveTexture) {
		this.emissiveTexture = emissiveTexture;
	}

	public void setAnimSpeed(float speed) {
		channel.setSpeed(speed);
	}

	public void setLoopModeTorso(LoopMode loopMode) {
		channelTorso.setLoopMode(loopMode);
	}

	public void setLoopMode(LoopMode loopMode) {
		channel.setLoopMode(loopMode);
	}

	public void setAnimTorsoSpeed(float speed) {
		channelTorso.setSpeed(speed);
	}

	public String getAnimTorsoName() {
		return channelTorso.getAnimationName();
	}

	public String getAnimName() {
		return channel.getAnimationName();
	}

	/**
	 * @return the attachments
	 */
	public Object2ObjectArrayMap<Bone, BoneAttachable> getAttachments() {
		return attachments;
	}

}
