/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>ViewerCamera</H2>
 * <H3>org.schema.schine.graphicsengine.camera</H3>
 * ViewerCamera.java
 * <HR>
 * Description goes here. If you see this message, please contact me and the
 * description will be filled.<BR>
 * <BR>
 *
 * @author Robin Promesberger (schema)
 * @mail <A HREF="mailto:schemaxx@gmail.com">schemaxx@gmail.com</A>
 * @site <A
 * HREF="http://www.the-schema.com/">http://www.the-schema.com/</A>
 * @project JnJ / VIR / Project R
 * @homepage <A
 * HREF="http://www.the-schema.com/JnJ">
 * http://www.the-schema.com/JnJ</A>
 * @copyright Copyright � 2004-2010 Robin Promesberger (schema)
 * @licence Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.schema.game.client.view.camera;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.lwjgl.glfw.GLFW;
import org.schema.common.FastMath;
import org.schema.game.client.controller.manager.ingame.EditSegmentInterface;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.SlotAssignment;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.common.JoystickAxisMapping;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.camera.CameraMouseState;
import org.schema.schine.graphicsengine.camera.TransitionCamera;
import org.schema.schine.graphicsengine.camera.viewer.AbstractViewer;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.input.JoystickMappingFile;
import org.schema.schine.input.Keyboard;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.container.TransformTimed;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class InShipCamera extends Camera implements SegmentControllerCamera {
	private final SegmentController controller;
	public boolean docked;
	private boolean align;
	private Matrix3f mRot;
	private HelperCamera helperCamera;
	private Camera freeViewLockedMoveCamera;
	private BuildShipCamera freeCamera;
	private Vector3f shipForward = new Vector3f(0, 0, 1);
	private Vector3f shipRight = new Vector3f(1, 0, 0);
	private Vector3f shipUp = new Vector3f(0, 1, 0);
	private boolean firstInit = false;
	private Matrix3f mRot2;
	private float rotMap[];
	private int orientation;
	private Camera toReset;
	private TransitionCamera transitionCamera;
	private boolean cancel;
	private boolean adjustMode;
	private EditSegmentInterface c;
	private Transform adjustedTransformRelativeToBlock;
	
	private static class ShipViewable extends ShipOffsetCameraViewable{
		public ShipViewable(EditSegmentInterface edit) {
			super(edit);
		}
		public InShipCamera fm;
		@Override
		public Vector3f getPos() {
			if(fm.adjustMode) {
				return fm.freeCamera.getViewable().getPos();
			}else {
				return super.getPos();
			}
		}
		
		
	}
	private static class ShipAdjustViewable extends ShipMovableCameraViewable{
		
		public ShipAdjustViewable(EditSegmentInterface edit) {
			super(edit, new Vector3f());
		}

		@Override
		public boolean canMove(PlayerInteractionControlManager playerIntercationManager) {
			if (!playerIntercationManager.isTreeActive() || playerIntercationManager.isSuspended()) {
				return false;
			}
			if (((GameClientState) controller.getState()).getGlobalGameControlManager().getIngameControlManager().getChatControlManager().isActive()) {
				return false;
			}
			if (((GameClientState) controller.getState()).getPlayer() != null && !((GameClientState) controller.getState()).getPlayer().getControllerState().canClientPressKey()) {
				return false;
			}
			return true;
		}
	}
	
	public InShipCamera(EditSegmentInterface c, Camera old, SegmentPiece entered) {
		super(c.getSegmentController().getState(), new ShipViewable(c));
		((ShipViewable)getViewable()).fm = this;
		this.c = c;
		this.controller = c.getSegmentController();
		rotMap = new float[6];
		rotMap[Element.FRONT] = 0;
		rotMap[Element.BACK] = (float) Math.PI;

		rotMap[Element.TOP] = (float) -Math.PI / 2f;
		rotMap[Element.BOTTOM] = (float) Math.PI / 2f;

		rotMap[Element.RIGHT] = (float) -Math.PI / 2f;
		rotMap[Element.LEFT] = (float) Math.PI / 2f;
		;

		this.toReset = old;

		this.firstInit = true;
		byte origOrientation = entered.getOrientation();
		//		if(origOrientation == Element.LEFT || origOrientation == Element.RIGHT){
		//			origOrientation = (byte) Element.getOpposite(origOrientation);
		//		}
		//		System.err.println("ORIG ORIENTATION: "+origOrientation+": "+Element.getSideString(origOrientation)+" -> "+Element.getSideString(origOrientation));
		origOrientation = (byte) Math.max(0, Math.min(5, origOrientation));
		orientation = origOrientation;

		if (orientation == Element.TOP || orientation == Element.BOTTOM) {

			//BOTTOM / TOP
			Matrix3f r = new Matrix3f();
			r.setIdentity();
			r.rotY((float) (Math.PI / 2));

			mRot = new Matrix3f();
			mRot.setIdentity();
			//			System.err.println("ROTATION_X: "+rotMap[getOrientation()]+" oo "+Element.getSideString(getOrientation())+" orig: "+origOrientation);
			mRot.rotX(rotMap[orientation]);
			mRot.mul(r);
			mRot2 = new Matrix3f();
			mRot2.setIdentity();
			mRot2.rotX(-rotMap[orientation]);
			mRot2.mul(r);
		} else {
			//LEFT / RIGHT / FRONT / BACK
			mRot = new Matrix3f();
			mRot.setIdentity();

			//			System.err.println("ROTATION_Y: "+rotMap[getOrientation()]+" oo "+Element.getSideString(getOrientation())+" orig: "+origOrientation);
			mRot.rotY(rotMap[orientation]);

			mRot2 = new Matrix3f();
			mRot2.setIdentity();
			mRot2.rotY(-rotMap[orientation]);

		}

		helperCamera = new HelperCamera(c.getSegmentController().getState(), new ShipOffsetCameraViewable(c));

		freeViewLockedMoveCamera = new Camera(c.getSegmentController().getState(), new ShipOffsetCameraViewable(c));
		freeCamera = new BuildShipCamera((GameClientState) state, null, c, new ShipAdjustViewable(c), null);
		
		
		reset();
		helperCamera.reset();
		freeViewLockedMoveCamera.reset();
		freeCamera.reset();

	}

	public void align() {
		align = true;
	}

	public void cancel() {
		cancel = true;
	}

	public void forceOrientation(Transform t, Timer timer) {
		getWorldTransform().basis.set(t.basis);
		setForward(GlUtil.getForwardVector(new Vector3f(), t));
		setUp(GlUtil.getUpVector(new Vector3f(), t));
		setRight(GlUtil.getRightVector(new Vector3f(), t));
		getViewable().setLeft(GlUtil.getLeftVector(new Vector3f(), t));
		getViewable().setForward(GlUtil.getForwardVector(new Vector3f(), t));
		getViewable().setUp(GlUtil.getUpVector(new Vector3f(), t));
		getLookAlgorithm().force(t);
		//		cam.forceOrientation();

		//
		helperCamera.getWorldTransform().basis.set(t.basis);
		helperCamera.setForward(GlUtil.getForwardVector(new Vector3f(), t));
		helperCamera.setUp(GlUtil.getUpVector(new Vector3f(), t));
		helperCamera.setRight(GlUtil.getRightVector(new Vector3f(), t));
		helperCamera.getViewable().setLeft(GlUtil.getLeftVector(new Vector3f(), t));
		helperCamera.getViewable().setForward(GlUtil.getForwardVector(new Vector3f(), t));
		helperCamera.getViewable().setUp(GlUtil.getUpVector(new Vector3f(), t));
		helperCamera.getLookAlgorithm().force(t);
		////		cam.forceOrientation();
		//
		//				helperCamera.resetAlgo();
		//
		//
		this.shipRight.set(GlUtil.getRightVector(new Vector3f(), t));
		this.shipUp.set(GlUtil.getUpVector(new Vector3f(), t));
		this.shipForward.set(GlUtil.getForwardVector(new Vector3f(), t));
		//
		//		getController().getWorldTransform().set(t);
		update(timer, controller.isOnServer());
	}

	/**
	 * @return the controller
	 */
	@Override
	public final SegmentController getSegmentController() {
		return controller;
	}

	public HelperCamera getHelperCamera() {
		return helperCamera;
	}

	public void setHelperCamera(HelperCamera helperCamera) {
		this.helperCamera = helperCamera;
	}

	public Vector3f getHelperForward() {

		Transform t = new Transform(helperCamera.getWorldTransform());
		//		t.basis.mul(mRot);
		return GlUtil.getForwardVector(new Vector3f(), t);
	}

	/**
	 * @return the orientation
	 */
	public int getOrientation() {
		return orientation;
	}

	/**
	 * @param orientation the orientation to set
	 */
	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}

	public void resetTransition(Camera camera) {
		setStable(false);
		this.transitionCamera = new TransitionCamera(camera, this, 0.2f);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.camera.Camera#getWorldTransform()
	 */
	@Override
	public TransformTimed getWorldTransform() {
		return super.getWorldTransform();
	}

	@Override
	public void reset() {
		super.reset();
		if (firstInit) {
			getWorldTransform().basis.set(controller.getWorldTransform().basis);
		}

	}

	/**
	 *
	 * @return true if the entered ship position is NOT the core (but a weapons computer e.g.)
	 */
	private boolean addShipRotationFromEnteredNonCore() {
		SegmentPiece entered = ((GameClientState) state).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager()
				.getPlayerIntercationManager().getInShipControlManager().getEntered();
		return entered != null && !entered.equalsPos(Ship.core);
	}	
	public void resetAdjustedMatrix() {
		((ShipViewable)getViewable()).offset.set(0,0,0);
		this.adjustedTransformRelativeToBlock = null;
	}
	
	public void resetAdjustMode() {
		if(adjustMode) {
			//reset camera view
			freeCamera = new BuildShipCamera((GameClientState) state, null, this.c, new ShipAdjustViewable(this.c), null);
			freeCamera.getWorldTransform().set(helperCamera.getWorldTransform());
			
			((ShipMovableCameraViewable)freeCamera.getViewable()).jumpToInstantlyWithoutOffset(((ShipOffsetCameraViewable) getViewable()).getPosMod());
			
			resetAdjustedMatrix();
		}else {
			//reset view and send
			this.adjustedTransformRelativeToBlock = new Transform();
			this.adjustedTransformRelativeToBlock.setIdentity();
			((Ship) controller).getManagerContainer().getCockpitManager().setTransformFor(((GameClientState)state).getPlayer().getCockpit(), this.adjustedTransformRelativeToBlock);
			((ShipViewable)getViewable()).offset.set(this.adjustedTransformRelativeToBlock.origin);
			((Ship) controller).getManagerContainer().getCockpitManager().send(((GameClientState)state).getPlayer().getCockpit().block, adjustedTransformRelativeToBlock);
			resetAdjustedMatrix();
		}
	}
	public void setAdjustMode(boolean on) {
		
		if (((GameClientState) controller.getState()).getPlayer() == null || ((GameClientState) controller.getState()).getPlayer().getCockpit().isCore()) {
			resetAdjustedMatrix();
			return;
		}
		
		if(!adjustMode && on) {
			
			Transform ct = ((Ship)c.getSegmentController()).getManagerContainer().getCockpitManager().getTransform((((GameClientState) state).getPlayer().getCockpit().block));
			Transform rotOnly = new Transform();
			rotOnly.setIdentity();
			rotOnly.basis.set(ct.basis);
			
			freeCamera = new BuildShipCamera((GameClientState) state, null, this.c, new ShipAdjustViewable(this.c), rotOnly);
			freeCamera.getWorldTransform().set(helperCamera.getWorldTransform());
			
			freeCamera.getLookAlgorithm().force(rotOnly);
			
			((ShipMovableCameraViewable)freeCamera.getViewable()).jumpToInstantlyWithoutOffset(((ShipOffsetCameraViewable) getViewable()).getPosMod());
			
			((ShipMovableCameraViewable)freeCamera.getViewable()).getPosRaw().add(ct.origin);
			
			
			resetAdjustedMatrix();
		}
		
		if(adjustMode && !on) {
			
			this.adjustedTransformRelativeToBlock = new Transform();
			this.adjustedTransformRelativeToBlock.set(this.controller.getWorldTransformInverse());
			this.adjustedTransformRelativeToBlock.mul(freeCamera.getWorldTransform());
			this.adjustedTransformRelativeToBlock.origin.sub(((ShipMovableCameraViewable)freeCamera.getViewable()).blockPos);
			
			
			((Ship) controller).getManagerContainer().getCockpitManager().setTransformFor(((GameClientState)state).getPlayer().getCockpit(), this.adjustedTransformRelativeToBlock);
			
			((ShipViewable)getViewable()).offset.set(this.adjustedTransformRelativeToBlock.origin);
			
			((Ship) controller).getManagerContainer().getCockpitManager().send(((GameClientState)state).getPlayer().getCockpit().block, adjustedTransformRelativeToBlock);
			
			
			System.err.println("ADJUSTED MAT: \n"+this.adjustedTransformRelativeToBlock.getMatrix(new Matrix4f()));
		}
		
		adjustMode = on;
		System.err.println("[CLIENT][INSHIPCAMERA] ADJUST MODE "+adjustMode);
	}
	public void switchAdjustMode() {
		setAdjustMode(!adjustMode);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.camera.AbstractViewerCamera#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public synchronized void update(Timer timer, boolean server) {

		if (firstInit) {
			GlUtil.getRightVector(shipRight, controller.getWorldTransform());
			GlUtil.getUpVector(shipUp, controller.getWorldTransform());
			GlUtil.getForwardVector(shipForward, controller.getWorldTransform());

			setRight(shipRight);
			setUp(shipUp);
			setForward(shipForward);
			firstInit = false;
		}
		super.mouseRotationActive = false;
		super.update(timer, server);

		GlUtil.getRightVector(shipRight, controller.getWorldTransform());
		GlUtil.getUpVector(shipUp, controller.getWorldTransform());
		GlUtil.getForwardVector(shipForward, controller.getWorldTransform());

		setRight(shipRight);
		setUp(shipUp);
		setForward(shipForward);

		if (CameraMouseState.isInMouseControl()) {
			((ShipOffsetCameraViewable) helperCamera.getViewable()).getPosMod().set(((ShipOffsetCameraViewable) getViewable()).getPosMod());
			((ShipOffsetCameraViewable) freeViewLockedMoveCamera.getViewable()).getPosMod().set(((ShipOffsetCameraViewable) getViewable()).getPosMod());
//			((ShipMovableCameraViewable) freeCamera.getViewable()).getPosMod().set(((ShipOffsetCameraViewable) getViewable()).getPosMod());

			helperCamera.update(timer, server);
			freeViewLockedMoveCamera.update(timer, server);
			freeCamera.update(timer, server);

			if (controller != null) {
				
				if (adjustMode) {
					setRight(freeCamera.getRight());
					setUp(freeCamera.getUp());
					setForward(freeCamera.getForward());
					
//					System.err.println("CAMMMMA: ; "+freeCamera.getPos()+"; "+((ShipAdjustViewable)freeCamera.getViewable()).getPosRaw());
					
				}else if (KeyboardMappings.FREE_CAM.isDownOrSticky(state)) {
					// show free camera

					setRight(freeViewLockedMoveCamera.getRight());
					setUp(freeViewLockedMoveCamera.getUp());
					setForward(freeViewLockedMoveCamera.getForward());

					GameClientState s = (GameClientState)state;
					
					PlayerState player = s.getPlayer();
					SlotAssignment shipConfiguration = controller.getSlotAssignment();
					
					long pos = shipConfiguration.getAsIndex(player.getCurrentShipControllerSlot());
					if(pos == PlayerUsableInterface.USABLE_ID_SHOOT_TURRETS){
						//used the actual look-at from free cam. comment out if pure free mode is needed
						helperCamera.getWorldTransform().set(freeViewLockedMoveCamera.getWorldTransform());
					}
				} else if (EngineSettings.DEBUG_SHIP_CAM_ON_RCONTROL.isOn() && Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL)) {
					// show helper camera

					setRight(helperCamera.getRight());
					setUp(helperCamera.getUp());
					setForward(helperCamera.getForward());
					
					
				} else {

					if (addShipRotationFromEnteredNonCore()) {
						Vector3f shipRight = new Vector3f();
						Vector3f shipUp = new Vector3f();
						Vector3f shipForward = new Vector3f();
						GlUtil.getRightVector(shipRight, controller.getWorldTransform());
						GlUtil.getUpVector(shipUp, controller.getWorldTransform());
						GlUtil.getForwardVector(shipForward, controller.getWorldTransform());

						Transform start = new Transform();
						GlUtil.setForwardVector(this.shipForward, start);
						GlUtil.setUpVector(this.shipUp, start);
						GlUtil.setRightVector(this.shipRight, start);

						Transform end = new Transform();
						GlUtil.setForwardVector(shipForward, end);
						GlUtil.setUpVector(shipUp, end);
						GlUtil.setRightVector(shipRight, end);

						Matrix3f m = new Matrix3f();
						m.sub(end.basis, start.basis);

						helperCamera.getWorldTransform().basis.add(m);

						this.shipRight.set(shipRight);
						this.shipUp.set(shipUp);
						this.shipForward.set(shipForward);
					}
				}
				
				
				
			}

		}
		if (controller != null && !controller.railController.isDockedOrDirty() &&
				!Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL) 
				&& !KeyboardMappings.FREE_CAM.isDownOrSticky(state) && !adjustMode) {
			getWorldTransform().basis.set(controller.getWorldTransform().basis);
			Matrix3f asd = new Matrix3f(mRot2);
			asd.invert();
			getWorldTransform().basis.mul(asd);
			
			
			if(adjustedTransformRelativeToBlock != null) {
				//adjust angle
				getWorldTransform().basis.mul(adjustedTransformRelativeToBlock.basis);
			}else {
				((ShipViewable)getViewable()).offset.set(0,0,0);
			}
		}
		if (transitionCamera != null && transitionCamera.isActive()) {
			setStable(false);
			transitionCamera.update(timer, server);
			this.getWorldTransform().set(transitionCamera.getWorldTransform());
		} else {
			if (!isStable()) {
				setStable(true);
				helperCamera.reset();
			}
		}
		if (toReset != null) {
			resetTransition(toReset);
			toReset = null;
		}
//		System.err.println("INSHIP:::\n "+getWorldTransform().basis);
	}

	private static class Recoil{
		public float speedOut = 5.0f;
		public float speedOutAdd;
		public float speedOutPowMult;
		
		public float speedBack = 1.0f;
		public float speedBackAdd;
		public float speedBackPowMult;
		
		public Vector2f force = new Vector2f();
		public Vector2f forceNorm = new Vector2f();
		public Vector2f totalCurrent = new Vector2f();
		public boolean goingBack = false;
		
		public float timeAcc;
		
		
		public static final float T_STEP = 1f/60f; //60 fps
		
		public boolean apply(boolean server, Timer timer, HelperCamera c) {
			
			timeAcc += timer.getDelta();
			
			//fix rate for recoil to avoid inaccuricies from slowdown
			while(timeAcc > T_STEP) {
				timeAcc -= T_STEP;
				
				float lenTo = force.length();
				float lenCurrent = totalCurrent.lengthSquared() > 0 ? totalCurrent.length(): 0;
				if(!goingBack && totalCurrent.length() < force.length()) {
					
					float lenToGo = lenTo - lenCurrent;
					
					forceNorm.set(force);
					forceNorm.normalize();
	
					float speed = speedOut;
					float percentDone = totalCurrent.length() / force.length();
					
					//start fast and get slower
					if(speedOutAdd > 0) {
						speed *= FastMath.pow((1.0f-percentDone)*speedOutPowMult + speedOutAdd, 2);
					}
//					System.err.println("OUT: % "+percentDone+"; speed "+speed+"; Add "+speedOutAdd);
					float scale = speed * T_STEP;
					
					if(scale > lenToGo) {
						forceNorm.scale(lenToGo);
						totalCurrent.set(force);
						goingBack = true;
					}else {
						forceNorm.scale(scale);
						totalCurrent.add(forceNorm);
					}
					
					if(forceNorm.length() < 0.002f && lenToGo < 0.1f) {
						//margin to not end up in an endless loop
						totalCurrent.set(force);
						goingBack = true;
					}
				}else if(totalCurrent.lengthSquared() > 0) {
					forceNorm.set(force);
					forceNorm.normalize();
					forceNorm.negate();
					
					
					float speed = speedBack;
					//start slow and get faster
					float percentDone = (totalCurrent.length() / force.length());
					
					if(speedBackAdd > 0) {
						speed *= FastMath.pow((1.0f-percentDone)*speedBackPowMult + speedBackAdd, 2);
					}
//					System.err.println("IN: % "+percentDone+"; speed "+speed+"; Add "+speedBackAdd);
					float scale = speedBack * T_STEP;
					
					if(lenCurrent - scale < 0) {
						forceNorm.scale(lenCurrent);
						totalCurrent.set(0,0);
						//end of recoil
					}else {
						forceNorm.scale(scale);
						totalCurrent.add(forceNorm);
					}
					if(forceNorm.length() < 0.002f && lenCurrent < 0.1f) {
						//margin to not end up in an endless loop
						totalCurrent.set(0,0);
					}
				}else {
					
					return false;
				}
				c.getLookAlgorithm().mouseRotate(server, 
						forceNorm.x,
						forceNorm.y,
						0,
						1.0f,
						1.0f,
						0);
			}
			return true;
		}
		
		
	}
	public void addRecoil(float forceX, float forceY, float dirX, float dirY, float in, float inAdd, float inPowMult, float out, float outAdd, float outPowMult) {
		Recoil recoil = new Recoil();
		float x;
		float y;
		
		if(dirX != 0) {
			x = (float) ((Math.random() * FastMath.sign(dirX)));
		}else {
			x = (float) ((Math.random() - 0.5) * 2.0);
		}
		
		if(dirY != 0) {
			y = (float) ((Math.random() * FastMath.sign(dirY)));
		}else {
			y = (float) ((Math.random() - 0.5) * 2.0);
		}
		
		recoil.force.set(x, y);
		if(x == 0 && y == 0) {
			recoil.force.set(1, 0);
		}
		recoil.force.normalize();
		
		recoil.force.x *= forceX;
		recoil.force.y *= forceY;
		if(recoil.force.lengthSquared() > 0) {
			
			
			
			recoil.speedOut = out * recoil.force.length();
			recoil.speedOutAdd = outAdd;
			recoil.speedOutPowMult = outPowMult;
			
			recoil.speedBack = in * recoil.force.length();
			recoil.speedBackAdd = inAdd;
			recoil.speedBackPowMult = inPowMult;
			
			for(Recoil r : helperCamera.recoils) {
				r.speedOut *= 2;
				r.speedBack *= 2;
			}
			if(helperCamera.recoils.isEmpty()) {
				helperCamera.lastInitialRecoil = controller.getUpdateTime();
				helperCamera.lastRecoilState.set(helperCamera.getWorldTransform().basis);
			}
			helperCamera.recoils.add(recoil);
			
		}
	}
	public class HelperCamera extends Camera {

		public long lastInitialRecoil;
		public Matrix3f lastRecoilState = new Matrix3f();
		private TransformableOldRestrictedAxisCameraLook dockingOldAlgo;
		private TransformableRestrictedAxisCameraLook dockingAlgo;
		private TransformableRestrictedCameraLook defaultAlgo;
		private boolean wasDocked;
		public final ObjectArrayList<Recoil> recoils = new ObjectArrayList<Recoil>();
		public HelperCamera(StateInterface state, AbstractViewer viewer) {
			super(state, viewer);
			defaultAlgo = new TransformableRestrictedCameraLook(this, getSegmentController());
			setLookAlgorithm(defaultAlgo);
			setMouseSensibilityX(0.2f);
			setMouseSensibilityY(0.2f);

			this.dockingOldAlgo = new TransformableOldRestrictedAxisCameraLook(this, getSegmentController());
			this.dockingAlgo = new TransformableRestrictedAxisCameraLook(this, getSegmentController());

		}

		private void setDockingAlgo(boolean force) {
			if (getSegmentController().getDockingController().isDocked()) {
				if (force || (dockingOldAlgo.getOrientation() !=
						getSegmentController().getDockingController().getDockedOn().to.getOrientation()
						|| dockingOldAlgo.correcting2Transformable !=
						getSegmentController().getDockingController().getDockedOn().to.getSegment().getSegmentController())) {
					//				System.err.println("NEW LOOKING ALGO "+dockingAlgo.getOrientation()+" / "+getSegmentController().getDockingController().getDockedOn().to.getOrientation()+"; "
					//						+dockingAlgo.correcting2Transformable+" / "+getSegmentController().getDockingController().getDockedOn().to.getSegment().getSegmentController());

					this.dockingOldAlgo = new TransformableOldRestrictedAxisCameraLook(this, getSegmentController());
				}
				dockingOldAlgo.setCorrectingForNonCoreEntry(addShipRotationFromEnteredNonCore());
				dockingOldAlgo.getFollowing().set(getSegmentController().getWorldTransform());

				dockingOldAlgo.setCorrecting2Transformable(getSegmentController().getDockingController().getDockedOn().to.getSegment().getSegmentController());

				byte orientation = getSegmentController().getDockingController().getDockedOn().to.getOrientation();

				dockingOldAlgo.setOrientation(orientation);

				setLookAlgorithm(dockingOldAlgo);
			} else if (getSegmentController().railController.isDockedAndExecuted()) {
				boolean init = !wasDocked || force || (dockingAlgo.getOrientation() !=
						getSegmentController().railController.previous.rail.getOrientation()
						|| dockingAlgo.correcting2Transformable !=
						getSegmentController().railController.previous.rail.getSegmentController());
				if (init) {
					assert (dockingAlgo != null);
					assert (getSegmentController().railController.previous != null);
					System.err.println("NEW LOOKING ALGO " + dockingAlgo.getOrientation() + " / " + getSegmentController().railController.previous.rail.getOrientation() + "; "
							+ dockingAlgo.correcting2Transformable + " / " + getSegmentController().railController.previous.rail.getSegmentController());

					this.dockingAlgo = new TransformableRestrictedAxisCameraLook(this, getSegmentController());

				}

				dockingAlgo.setCorrecting2Transformable(getSegmentController().railController.previous.rail.getSegmentController());

				byte orientation = getSegmentController().railController.previous.rail.getOrientation();

				dockingAlgo.setOrientation(orientation);

				setLookAlgorithm(dockingAlgo);

				if (init) {
//					System.err.println("MOVING::::::: \n"+getSegmentController().railController.getRailMovingLocalTransform().basis);
					//update one time so the last orientation can be set to what the
					//moving rail transform was (and the first update doesn't take the last recorded one)
					this.dockingAlgo.mouseRotate(getSegmentController().isOnServer(), 0, 0, 0, 0, 0, 0);
				}
			}

			wasDocked = getSegmentController().railController.isDockedAndExecuted();
		}		@Override
		public void reset() {
			super.reset();
			getWorldTransform().basis.set(getSegmentController().getWorldTransform().basis);
		}

		/**
		 * this function is needed so
		 * we don't have a movement update without the camera
		 * being able to be re-initialized from a new rail dock
		 * @return true if the entity from this camera was docked on last update
		 */
		public boolean wasDockedOnUpdate() {
			return wasDocked;
		}



		@Override
		public void update(Timer timer, boolean server) {

			if (getSegmentController().getDockingController().isDocked() || getSegmentController().railController.isDockedOrDirty()) {

				setDockingAlgo(false);

			} else {
				setLookAlgorithm(defaultAlgo);
				//				System.err.println("addShipRotationFromEnteredNonCore "+addShipRotationFromEnteredNonCore());

				((TransformableRestrictedCameraLook) getLookAlgorithm()).setCorrecting(addShipRotationFromEnteredNonCore());
				((TransformableRestrictedCameraLook) getLookAlgorithm()).getFollowing().set(getSegmentController().getWorldTransform());
				((TransformableRestrictedCameraLook) getLookAlgorithm()).setOrientation(getOrientation());
			}

			if (CameraMouseState.isInMouseControl()) {

				updateMouseWheel();

				for(int i = 0; i < recoils.size(); i++) {
					Recoil recoil = recoils.get(i);
					boolean alive = recoil.apply(server, timer, this);
					if(!alive) {
						recoils.remove(i--);
					}
					
				}
				
				if (JoystickMappingFile.ok()) {
					GameClientState c = (GameClientState) getSegmentController().getState();

					double pitch = c.getController().getJoystickAxis(JoystickAxisMapping.PITCH);
					double yaw = c.getController().getJoystickAxis(JoystickAxisMapping.YAW);
					double roll = c.getController().getJoystickAxis(JoystickAxisMapping.ROLL);

					getLookAlgorithm().mouseRotate(getSegmentController().isOnServer(),
							(float) yaw * timer.getDelta() * 10f,
							(float) pitch * timer.getDelta() * 10f,
							(float) roll * timer.getDelta() * 10f,
							getMouseSensibilityX(),
							getMouseSensibilityY(),
							getMouseSensibilityX());

				}

				if (KeyboardMappings.ROTATE_LEFT_SHIP.isDown()) {
					getLookAlgorithm().mouseRotate(server, 
							0,
							0.0f,
							-30f * timer.getDelta(),
							0,
							getMouseSensibilityY(),
							getMouseSensibilityX());

				}
				if (KeyboardMappings.ROTATE_RIGHT_SHIP.isDown()) {
					getLookAlgorithm().mouseRotate(server, 
							0,
							0.0f,
							30f * timer.getDelta(),
							0,
							getMouseSensibilityY(),
							getMouseSensibilityX());

				}

				if (KeyboardMappings.ROLL.isDown()) {

					getLookAlgorithm().mouseRotate(server, 
							0,
							((EngineSettings.S_MOUSE_SHIP_INVERT.isOn() ||
									EngineSettings.S_MOUSE_ALL_INVERT.isOn()) ? (float) mouseState.dy : (float) -mouseState.dy) / (GLFrame.getHeight() / 2),
							(float) mouseState.dx / (float) (GLFrame.getWidth() / 2),
							0,
							getMouseSensibilityY(),
							getMouseSensibilityX());
				} else {

					getLookAlgorithm().mouseRotate(server, 
							(float) mouseState.dx / (float) (GLFrame.getWidth() / 2),
							((EngineSettings.S_MOUSE_SHIP_INVERT.isOn() ||
									EngineSettings.S_MOUSE_ALL_INVERT.isOn()) ? (float) mouseState.dy : (float) -mouseState.dy) / (GLFrame.getHeight() / 2),
							0,
							getMouseSensibilityX(),
							getMouseSensibilityY(),
							0);
				}

				if (align) {
					System.err.println("[SHIP-CAMERA] ALIGN VIEW");
					if (getSegmentController().railController.isDockedOrDirty()) {
						System.err.println("[SHIP-CAMERA] TODO: ALIGN VIEW FOR RAIL");
					} else if (getSegmentController().getDockingController().isDocked()) {
						Matrix3f m = new Matrix3f(getSegmentController().getDockingController().getDockedOn().to.getSegment().getSegmentController().getWorldTransformOnClient().basis);
						helperCamera.getWorldTransform().basis.set(m);
						m.set(getSegmentController().getDockingController().targetStartQuaternion);
						helperCamera.getWorldTransform().basis.mul(m);
						setDockingAlgo(true);
						getLookAlgorithm().fix();
					} else {

						helperCamera.getWorldTransform().basis.setIdentity();
						getLookAlgorithm().force(helperCamera.getWorldTransform());
						getSegmentController().getPhysicsDataContainer().getObject().activate(true);

					}
					align = false;
				}
				if (cancel) {
					if (!getSegmentController().getDockingController().isDocked() && !getSegmentController().railController.isDockedOrDirty()) {
						helperCamera.getWorldTransform().basis.set(getSegmentController().getWorldTransform().basis);
						getLookAlgorithm().force(helperCamera.getWorldTransform());
						getSegmentController().getPhysicsDataContainer().getObject().activate(true);
//						getLookAlgorithm().fix();
					}
					cancel = false;
				}
				//				System.err.println("WT: "+getWorldTransform().basis);

				updateViewer(timer);
			}
		}
	}



	@Override
	public void setAllowZoom(boolean allowZoom) {
		if (adjustMode) {
			freeCamera.setAllowZoom(allowZoom);
		} else if (KeyboardMappings.FREE_CAM.isDownOrSticky(state)) {
			freeViewLockedMoveCamera.setAllowZoom(allowZoom);
		} else {
			helperCamera.setAllowZoom(allowZoom);
		}
		super.setAllowZoom(allowZoom);
	}

	public boolean isInAdjustMode() {
		return adjustMode;
	}

	public void setAdjustMatrix(Transform t) {
		this.adjustedTransformRelativeToBlock = new Transform(t);
		((ShipViewable)getViewable()).offset.set(t.origin);
	}

	

	
}
