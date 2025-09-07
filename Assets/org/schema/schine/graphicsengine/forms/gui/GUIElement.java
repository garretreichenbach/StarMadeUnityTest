package org.schema.schine.graphicsengine.forms.gui;

import api.listener.events.gui.GUIElementInstansiateEvent;
import api.mod.StarLoader;
import com.bulletphysics.linearmath.Transform;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.system.MemoryUtil;
import org.schema.common.util.linAlg.TransformTools;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.schine.graphicsengine.OculusVrHelper;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.AbstractSceneNode;
import org.schema.schine.graphicsengine.forms.Positionable;
import org.schema.schine.graphicsengine.forms.Scalable;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIAbstractNewScrollBar;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActivatableTextBar;
import org.schema.schine.input.InputState;
import org.schema.schine.input.Mouse;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

public abstract class GUIElement extends AbstractSceneNode implements Positionable, Scalable, Drawable {

	public static final int RENDER_MODE_NORMAL = 1;
	public static final int RENDER_MODE_SHADOW = 2;
	public static final int ORIENTATION_NONE = 0;
	public static final int ORIENTATION_LEFT = 1;
	public static final int ORIENTATION_RIGHT = 2;
	public static final int ORIENTATION_TOP = 4;
	public static final int ORIENTATION_BOTTOM = 8;
	public static final int ORIENTATION_VERTICAL_MIDDLE = 16;
	public static final int ORIENTATION_HORIZONTAL_MIDDLE = 32;
	private static final long CALLBACK_DELAY_MS = 200;
	public static int renderModeSet = RENDER_MODE_NORMAL;
	public static FloatBuffer coord = MemoryUtil.memAllocFloat(3);
	public static GUITextOverlay textOverlay;
	public static boolean deactivateCallbacks;
	private static IntBuffer viewportTemp = MemoryUtil.memAllocInt(16);
	private static DoubleBuffer leftClipPlane = MemoryUtil.memAllocDouble(4);
	private static DoubleBuffer rightClipPlane = MemoryUtil.memAllocDouble(4);
	private static DoubleBuffer topClipPlane = MemoryUtil.memAllocDouble(4);
	private static DoubleBuffer bottomClipPlane = MemoryUtil.memAllocDouble(4);
	public static boolean translateOnlyMode;
	/**
	 * The rel mouse pos.
	 */
	private final Vector3f relMousePos = new Vector3f();
	public int renderMode = RENDER_MODE_NORMAL;
	/**
	 * The inside.
	 */
	private boolean inside;
	protected GUICallback callback;
	private int[] viewportMonitor = new int[4];
	private boolean mouseUpdateEnabled;
	private boolean changed = true;
	private final InputState state;
	private Object userPointer;
	private boolean wasInside;
	private static Vector4f clip = new Vector4f();
	protected static boolean debug;
	private int insideUpdate;
	public static final float x32 = 0.03125f;
	public static final float x16 = 0.0625f;

	private final GUIObservable obs = new GUIObservable();




	public void addObserver(GUIChangeListener s) {
		obs.addObserver(s);
	}

	public void deleteObserver(GUIChangeListener s) {
		obs.deleteObserver(s);
	}

	public void deleteObservers() {
		obs.deleteObservers();
	}

	public void notifyObservers(boolean listDimUpdate) {
		obs.notifyObservers(listDimUpdate);
	}

	public void notifyObservers() {
		obs.notifyObservers();
	}

	public GUIElement(InputState state) {
		this.state = state;
		//INSERTED CODE
		GUIElementInstansiateEvent event = new GUIElementInstansiateEvent(this, state);
		StarLoader.fireEvent(event, false);
		///
	}

	public static void disableOrthogonal() {
		GlUtil.glMatrixMode(GL11.GL_PROJECTION);    // Select The Projection Matrix
		GlUtil.glPopMatrix();    // Restore The Old Projection Matrix
		GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
		GlUtil.glPopMatrix();

		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glEnable(GL11.GL_LIGHTING);
	}
	public String generateToolTip() {
		for(AbstractSceneNode e : getChilds()){
			if(e instanceof GUIElement){
				String generateToolTip = ((GUIElement)e).generateToolTip();
				if(generateToolTip != null){
					return generateToolTip;
				}
			}
		}
		return null;
	}
	public static boolean isNewHud() {
		return true;
//		return !EngineSettings.SECRET.getCurrentState().equals("newgui");
	}
	public static void startStandardDraw(){
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		GlUtil.glDisable(GL11.GL_LIGHTING);
	}
	public static void endStandardDraw(){
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glColor4f(1, 1, 1, 1);
		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
	}
	public static void disableScreenProjection() {
		GlUtil.glMatrixMode(GL11.GL_PROJECTION);    // Select The Projection Matrix
		GlUtil.glPopMatrix();    // Restore The Old Projection Matrix
		GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
		GlUtil.glPopMatrix();
	}

	public static void enableOrthogonal() {
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		GlUtil.glPushMatrix();
		GlUtil.glMatrixMode(GL11.GL_PROJECTION);    // Select The Projection Matrix
		GlUtil.glPushMatrix();            // Store The Projection Matrix
		GlUtil.glLoadIdentity();
		if (Controller.ocMode == OculusVrHelper.OCCULUS_LEFT) {
			int width = GLFrame.getWidth();
			int height = GLFrame.getHeight();
			width *= OculusVrHelper.getScaleFactor();
			height *= OculusVrHelper.getScaleFactor();

//			GlUtil.gluOrtho2D(0, width/2, height, 0);
			GlUtil.gluOrtho2D(0, GLFrame.getWidth() / 2 * OculusVrHelper.getScaleFactor(), GLFrame.getHeight(), 0);
		} else if (Controller.ocMode == OculusVrHelper.OCCULUS_RIGHT) {
			int width = GLFrame.getWidth();
			int height = GLFrame.getHeight();
			width *= OculusVrHelper.getScaleFactor();
			height *= OculusVrHelper.getScaleFactor();
//			GlUtil.gluOrtho2D(width/2,  width, height, 0);
			GlUtil.gluOrtho2D(GLFrame.getWidth() / 2 - (GLFrame.getWidth() / 2 * OculusVrHelper.getScaleFactor() - GLFrame.getWidth() / 2), GLFrame.getWidth(), GLFrame.getHeight(), 0);
		} else {
			GlUtil.gluOrtho2D(0, GLFrame.getWidth(), GLFrame.getHeight(), 0);
		}

		GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
		GlUtil.glLoadIdentity();

		if (Controller.ocMode != 0) {
//			Matrix4f pMat = GlUtil.createPerspectiveProjectionMatrix(OculusVrHelper.getyFov(), OculusVrHelper.getAspectRatio(), getNearPlane(), getFarPlane());

//			Transform projectionMatrix = new Transform();
//			Controller.getMat(pMat, projectionMatrix);

			Transform projTranslationLeftEye = new Transform();
			Transform projTranslationRightEye = new Transform();

//	        projTranslationLeftEye.setIdentity();
//	        projTranslationLeftEye.origin.set(OculusVrHelper.getProjectionCenterOffset(), 0.0f, 0.0f);
//
//	        Matrix4f leftProjTranlate = new Matrix4f();
//	        Controller.getMat(projTranslationLeftEye, leftProjTranlate);

//	        projTranslationRightEye.setIdentity();
//	        projTranslationRightEye.origin.set(-OculusVrHelper.getProjectionCenterOffset(), 0.0f, 0.0f);

//	        Matrix4f rightProjTranlate = new Matrix4f();
//	        Controller.getMat(projTranslationRightEye, rightProjTranlate);

//	        Transform projectionMatrixLeftEye = new Transform();
//	        Transform projectionMatrixRightEye = new Transform();

//	        projectionMatrixLeftEye.mul(projTranslationLeftEye, projectionMatrix);
//	        projectionMatrixRightEye.mul(projTranslationRightEye, projectionMatrix);
//

			Transform viewTranslationLeftEye = new Transform();
			Transform viewTranslationRightEye = new Transform();

			final float halfIPD = OculusVrHelper.getInterpupillaryDistance() * 0.5f;

			viewTranslationLeftEye.setIdentity();
			viewTranslationLeftEye.origin.set(-halfIPD * GLFrame.getWidth() * 4f * (OculusVrHelper.getScaleFactor() + 0.3f), 0.0f, 0.0f);

			viewTranslationRightEye.setIdentity();
			viewTranslationRightEye.origin.set(halfIPD * GLFrame.getWidth() * 4f * (OculusVrHelper.getScaleFactor() + 0.3f), 0.0f, 0.0f);

			if (Controller.ocMode == OculusVrHelper.OCCULUS_LEFT) {
				GlUtil.translateModelview(viewTranslationLeftEye.origin);
			} else if (Controller.ocMode == OculusVrHelper.OCCULUS_RIGHT) {
				GlUtil.translateModelview(viewTranslationRightEye.origin);
			}

		}
	}

	public static void enableOrthogonal3d() {
		enableOrthogonal3d(GLFrame.getWidth(), GLFrame.getHeight());
	}

	public static void enableOrthogonal3d(int width, int height) {
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);

		GlUtil.glPushMatrix();

		GlUtil.glMatrixMode(GL11.GL_PROJECTION);    // Select The Projection Matrix

		GlUtil.glPushMatrix();            // Store The Projection Matrix
		GlUtil.glLoadIdentity();
		GlUtil.glOrtho(0, width, height, 0, -1000, 1000);

		GlUtil.glMatrixMode(GL11.GL_MODELVIEW);

		GlUtil.glLoadIdentity();
	}

	public static void enableScreenProjection() {
		GlUtil.glPushMatrix();
		GlUtil.glMatrixMode(GL11.GL_PROJECTION);    // Select The Projection Matrix
		GlUtil.glPushMatrix();            // Store The Projection Matrix
		GlUtil.glLoadIdentity();
		//near plane must not be zero. nearPlane = 0 produces culling and depth bugs
		GlUtil.gluPerspective(45, (float) GLFrame.getWidth() / (float) GLFrame.getHeight(), 0.01f, 100.0f);

		GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
		GlUtil.glLoadIdentity();
	}

	public boolean isRenderable() {
		return GlUtil.isColorMask() && ((renderModeSet & renderMode) == renderMode);
	}

	public void attach(GUIElement o, int index) {
		assert (o != this) : "Attaching to self " + o;
		o.setParent(this);
		assert(checkDuplicates(o)):"DUPLICATE: "+o+"; "+getChilds();
		getChilds().add(index, o);
	}
	public void attach(GUIElement o) {
		assert (o != this) : "Attaching to self " + o;
		o.setParent(this);
		assert(checkDuplicates(o)):"DUPLICATE: "+o+"; "+getChilds();
		getChilds().add(o);
		

	}

	private boolean checkDuplicates(GUIElement o) {
		for(AbstractSceneNode e : getChilds()){
			if(e == o){
				return false;
			}
		}
		return true;
	}

	public void attach(int i, GUIActivatableTextBar o) {
		assert (o != this) : "Attaching to self " + o;
		o.setParent(this);
		getChilds().add(i, o);
	}



	public Vector3f getCurrentAbsolutePos(Vector3f out) {
		Matrix4f modelviewMatrix = Controller.modelviewMatrix;

		out.set(modelviewMatrix.m30, modelviewMatrix.m32, modelviewMatrix.m33);
		out.add(getPos());
		return out;
	}
	protected boolean isCoordsInside(Vector3f relMousePos, float scaleX, float scaleY){
		boolean xIn = relMousePos.x < getWidth() * scaleX * scaleX
				&& relMousePos.x > 0;
		boolean yIn = relMousePos.y < getHeight() * scaleY * scaleY
				&& relMousePos.y > 0;
		return (xIn && yIn);
	}
	public void checkMouseInside() {

		if (deactivateCallbacks || Mouse.isGrabbed()  || state.getController().getInputController().getCurrentContextPane() != state.getController().getInputController().getCurrentContextPaneDrawing()) {
			setInside(false);
			return;
		}

		float scaleX = Vector3fTools.length(Controller.modelviewMatrix.m00, Controller.modelviewMatrix.m01, Controller.modelviewMatrix.m02);
		float x = (Mouse.getX() - Controller.modelviewMatrix.m30) * scaleX;
		
		
		float scaleY = Vector3fTools.length(Controller.modelviewMatrix.m10, Controller.modelviewMatrix.m11, Controller.modelviewMatrix.m12);
		float y = (Mouse.getY() - Controller.modelviewMatrix.m31) * scaleY;
		
		
		Matrix4f modelviewMatrix = Controller.modelviewMatrix;
		GlUtil.getRelMouseX();
		if(debug){
			System.err.println("MousePos ;; "+x+", "+y+"; "+GLFrame.getWidth()+"x"+GLFrame.getHeight()+"; "+Mouse.getX()+"; "+Mouse.getY());
		}
		relMousePos.set(x, y, 0);
		
		
		if(!getTransform().basis.equals(TransformTools.ident.basis)){
			//rotate if necessary
			rotTmp.invert(getTransform().basis);
			rotTmp.transform(relMousePos);
		}
		
		
		if (getState().getUpdateTime() - getState().getController().getInputController().getLastDeactivatedMenu() > CALLBACK_DELAY_MS) {
			
			setInside(isCoordsInside(relMousePos, scaleX, scaleY));

		} else {
			setInside(false);
		}
		if(isInside() && GlUtil.getClip() != null){
			javax.vecmath.Vector4f clip = new javax.vecmath.Vector4f(GlUtil.getClip());
			GlUtil.addClipModelViewStack(clip);


			
			int mx = Mouse.getX();
			int my = Mouse.getY();
			boolean clipped = 
					mx >= clip.x && mx <= clip.y && //for left right clipping. might exclude scroll bar
					my >= clip.z && my <= clip.w;
			
//			if(this instanceof GUIListElement && ((GUIListElement)this).getContent().getUserPointer() != null){
//				System.err.println("HH "+mx+"; "+my+"; "+clip+"; "+clipped+"; ("+((GUIListElement)this).getContent().getUserPointer()+")"+"; ");
//			}
			if(!clipped && !(this instanceof GUIAbstractNewScrollBar) && !(this instanceof GUIScrollablePanel)){
				setInside(false);
			}
		}
		
		
		
		if (isInside() && GlUtil.isColorMask()) {
			if (callback != null) {
				if (!callback.isOccluded()) {
					if(this instanceof TooltipProviderCallback && ((TooltipProviderCallback)this).getToolTip() != null ){
						state.getController().getInputController().getGuiCallbackController().addToolTip(((TooltipProviderCallback)this).getToolTip());
					}
					state.getController().getInputController().getGuiCallbackController().addCallback(callback, this);
					
				}
				if(callback instanceof GUICallbackBlocking && ((GUICallbackBlocking)callback).isBlocking()){
					state.getController().getInputController().getGuiCallbackController().addBlocking((GUICallbackBlocking) callback, this);
				}
			}else{
				if(this instanceof TooltipProviderCallback && ((TooltipProviderCallback)this).getToolTip() != null ){
					state.getController().getInputController().getGuiCallbackController().addToolTip(((TooltipProviderCallback)this).getToolTip());
				}
			}
			//			state.getController().getGuiCallbackController().addInsideGUIElement(this);
		}
		
			
		wasInside = isInside();
		//		System.err.println(this+" inside: "+isInside()+" "+getRelMousePos());

	}
	
	public void checkBlockingOnly(){
		
		if (deactivateCallbacks || Mouse.isGrabbed()  || state.getController().getInputController().getCurrentContextPane() != state.getController().getInputController().getCurrentContextPaneDrawing()) {
			return;
		}

		Matrix4f modelviewMatrix = Controller.modelviewMatrix;

		float scaleX = new Vector3f(modelviewMatrix.m00, modelviewMatrix.m01, modelviewMatrix.m02).length();
		float scaleY = new Vector3f(modelviewMatrix.m10, modelviewMatrix.m11, modelviewMatrix.m12).length();

		float x = (Mouse.getX() - modelviewMatrix.m30) * scaleX;
		float y = (Mouse.getY() - modelviewMatrix.m31) * scaleY;

			boolean xIn = x < getWidth() * scaleX * scaleX
					&& x > 0;
			boolean yIn = y < getHeight() * scaleY * scaleY
					&& y > 0;

		if (xIn && yIn && GlUtil.isColorMask()) {
			if (callback != null) {
				if(callback instanceof GUICallbackBlocking && ((GUICallbackBlocking)callback).isBlocking()){
					state.getController().getInputController().getGuiCallbackController().addBlocking((GUICallbackBlocking) callback, this);
				}
			}
			//			state.getController().getGuiCallbackController().addInsideGUIElement(this);
		}
	}

	public void checkMouseInsideWithTransform() {
		if(translateOnlyMode){
			translate();
			checkMouseInside();
			translateBack();
		}else{
			GlUtil.glPushMatrix();
			transform();
			checkMouseInside();
			GlUtil.glPopMatrix();
		}
	}

	@Override
	public AbstractSceneNode clone() {
		return null;
	}

	@Override
	public void transform() {
		
		transformTranslation();
		GlUtil.scaleModelview(getScale().x, getScale().y, getScale().z);
	}
	
	public void detach(GUIElement o) {
		o.setParent(null);
		boolean f = getChilds().remove(o);
	}

	public void detachAll() {
		for (int i = 0; i < getChilds().size(); i++) {
			AbstractSceneNode a = getChilds().get(i);
			if (a instanceof GUIElement) {
				detach((GUIElement) a);
			} else {
				detach(a);
			}
			i--;
		}
	}

	protected void doOrientation() {
	}

	public void drawAttached() {
		if (isInvisible()) {
			return;
		}
		if(translateOnlyMode){
			translate();
		}else{
			GlUtil.glPushMatrix();
			transform();
		}
		setInside(false);
		if (mouseUpdateEnabled) {
			checkMouseInside();
		}
		
		final int size = childs.size();
		for (int i = 0; i < size; i++) {
			assert(childs.get(i) != this):this;
			childs.get(i).draw();
		}
		if(translateOnlyMode){
			translateBack();
		}else{
			GlUtil.glPopMatrix();
		}
		

	}

	public void drawClipped(float wVal, float wMax, float hVal, float hMax) {

		float wPercent = wVal / wMax;
		float hPercent = hVal / hMax;

		clip.set(
				getPos().x,
				getPos().x + wPercent * getWidth(),

				getPos().y,
				getPos().y + hPercent * getHeight());

		drawClipped(clip);
	}

	public void drawClipped(Vector4f clip) {

		leftClipPlane.put(new double[]{1, 0, 0, -clip.x}).rewind();
		GL11.glClipPlane(GL11.GL_CLIP_PLANE0, leftClipPlane);
		GlUtil.glEnable(GL11.GL_CLIP_PLANE0);

		rightClipPlane.put(new double[]{-1, 0, 0, clip.y}).rewind();
		GL11.glClipPlane(GL11.GL_CLIP_PLANE1, rightClipPlane);
		GlUtil.glEnable(GL11.GL_CLIP_PLANE1);

		topClipPlane.put(new double[]{0, 1, 0, -clip.z}).rewind();
		GL11.glClipPlane(GL11.GL_CLIP_PLANE2, topClipPlane);
		GlUtil.glEnable(GL11.GL_CLIP_PLANE2);

		bottomClipPlane.put(new double[]{0, -1, 0, clip.w}).rewind();
		GL11.glClipPlane(GL11.GL_CLIP_PLANE3, bottomClipPlane);
		GlUtil.glEnable(GL11.GL_CLIP_PLANE3);

		draw();

		GlUtil.glDisable(GL11.GL_CLIP_PLANE0);
		GlUtil.glDisable(GL11.GL_CLIP_PLANE1);
		GlUtil.glDisable(GL11.GL_CLIP_PLANE2);
		GlUtil.glDisable(GL11.GL_CLIP_PLANE3);
	}

	/**
	 * Draw info.
	 *
	 * @param gl  the gl
	 * @param glu the glu
	 * @
	 */
	public void drawInfo() {
		if (!isInside()) {
			return;
		}
		enableOrthogonal();
		if (textOverlay == null) {
			textOverlay = new GUITextOverlay(state);
			textOverlay.setText(new ArrayList());
			textOverlay.getText().add("NONE");
		}
		textOverlay.getPos().set(Mouse.getX() + 10, Mouse.getY(), 0);
		String fps = getName() + hashCode() + " " + (isInside() ? "(+) " : "(-) ")
				+ (int) relMousePos.x + ", " + (int) relMousePos.y;

		textOverlay.getText().set(0, fps);
		textOverlay.drawText();
		disableOrthogonal();
	}

	/**
	 * @return the callback
	 */
	public GUICallback getCallback() {
		return callback;
	}

	/**
	 * @param callback the callback to set
	 */
	public void setCallback(GUICallback callback) {
		//		assert(callback != null);
		this.callback = callback;
	}

	public abstract float getWidth();

	public abstract float getHeight();

	/**
	 * @return the relMousePos
	 */
	public Vector3f getRelMousePos() {
		return relMousePos;
	}

	/**
	 * @return the state
	 */
	public InputState getState() {
		return state;
	}

//	/**
//	 * @param state the state to set
//	 */
//	public void setState(InputState state) {
//		this.state = state;
//	}

	/**
	 * @return the userPointer
	 */
	public Object getUserPointer() {
		return userPointer;
	}

	/**
	 * @param userPointer the userPointer to set
	 */
	public void setUserPointer(Object userPointer) {
		this.userPointer = userPointer;
	}

	/**
	 * @return the changed
	 */
	public boolean isChanged() {
		return changed;
	}

	/**
	 * @param changed the changed to set
	 */
	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	/**
	 * Checks if is inside.
	 *
	 * @return the inside
	 */
	public boolean isInside() {

		if (Math.abs(insideUpdate - state.getNumberOfUpdate()) > 1) {
			//no draw for 2 frames
			inside = false;
		}

		return inside;
	}

	/**
	 * Sets the inside.
	 *
	 * @param inside the inside to set
	 */
	public void setInside(boolean inside) {
		//		if(this.name.equals("afld")){
		//			System.err.println("setting inside from "+this.inside+" to "+inside);
		//		}
		this.inside = inside;
		insideUpdate = state.getNumberOfUpdate();
	}

	/**
	 * @return the mouseUpdateEnabled
	 */
	public boolean isMouseUpdateEnabled() {
		return mouseUpdateEnabled;
	}

	/**
	 * @param mouseUpdateEnabled the mouseUpdateEnabled to set
	 */
	public void setMouseUpdateEnabled(boolean mouseUpdateEnabled) {
		this.mouseUpdateEnabled = mouseUpdateEnabled;
	}

	public boolean isOnScreen() {
		if (getPos().x > GLFrame.getWidth()) {
			return false;
		}
		if (getPos().y > GLFrame.getHeight()) {
			return false;
		}
		if (getPos().x + getWidth() * getScale().x < 0) {
			return false;
		}
		if (getPos().y + getHeight() * getScale().y < 0) {
			return false;
		}
		return true;
	}

	public boolean isPositionCenter() {
		return false;
	}

	public boolean needsReOrientation() {
		for (int i = 0; i < 4; i++) {
			if (viewportMonitor[i] != Controller.viewport.get(i)) {
				for (int j = 0; j < 4; j++) {
					viewportMonitor[j] = Controller.viewport.get(j);
				}

				return true;
			}
		}
		return false;
	}

	public void orientate(int orientation) {
		orientate(orientation, Controller.viewport.get(0), Controller.viewport.get(1), Controller.viewport.get(2), Controller.viewport.get(3));
	}

	public void orientate(int orientation, int x, int y, int w, int h) {

		getPos().set(0, 0, 0);

		if ((orientation & ORIENTATION_VERTICAL_MIDDLE) == ORIENTATION_VERTICAL_MIDDLE) {
			getPos().y = (int) (h - (getHeight() * getScale().y) - y) / 2;
		}
		if ((orientation & ORIENTATION_HORIZONTAL_MIDDLE) == ORIENTATION_HORIZONTAL_MIDDLE) {
			getPos().x = (int) (w - (getWidth() * getScale().x) - x) / 2;
		}
		if ((orientation & ORIENTATION_LEFT) == ORIENTATION_LEFT) {
			getPos().x = x;
		}
		if ((orientation & ORIENTATION_RIGHT) == ORIENTATION_RIGHT) {
			getPos().x = (int) (w - (getWidth() * getScale().x));
		}
		if ((orientation & ORIENTATION_TOP) == ORIENTATION_TOP) {
			getPos().y = y;
		}
		if ((orientation & ORIENTATION_BOTTOM) == ORIENTATION_BOTTOM) {
			getPos().y = (int) (h - (getHeight() * getScale().y));
		}
	}

	public void orientateInsideFrame() {
		if (this.getPos().x < 0) {
			this.getPos().x = 0;
		}
		if (this.getPos().y < 0) {
			this.getPos().y = 0;
		}

		if (this.getPos().x + this.getWidth() > GLFrame.getWidth()) {
			this.getPos().x = GLFrame.getWidth() - this.getWidth();
		}
		if (this.getPos().y + this.getHeight() > GLFrame.getHeight()) {
			this.getPos().y = GLFrame.getHeight() - this.getHeight();
		}
	}

	protected void transformTranslation() {
//		System.err.println("TRANSFORM TO "+getTransform().origin.x+"; "+getTransform().origin.y+"; -> "+this.getClass());
//		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_U)) {
//			try {
//				throw new Exception("TR");
//			}catch(Exception e) {
//				e.printStackTrace();
//			}
//		}
		GlUtil.glMultMatrix(getTransform());
	}

	/**
	 * WARNING: This can only be used by a callBack() since outside of that scope
	 * this will not repesent anything
	 *
	 * @return the wasInside
	 */
	public boolean wasInside() {
		return wasInside;
	}
	//	public void resetMouseInside() {
	//		inside = false;
	//	};

	public boolean isActive() {
		return true;
	}

	public void resetToolTip() {
	}
	public boolean isOccluded() {
		return false;
	}
}
