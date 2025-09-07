package org.schema.game.client.view.gui;

import com.bulletphysics.linearmath.Transform;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;
import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.tools.SingleBlockDrawer;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.AbstractSceneNode;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.util.timer.LinearTimerUtil;
import org.schema.schine.input.InputState;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import java.nio.FloatBuffer;

public class GUI3DBlockElement extends GUIElement {

	public static LinearTimerUtil linearTimer = new LinearTimerUtil();
	private static Transform mView = new Transform();
	private static FloatBuffer fb = MemoryUtil.memAllocFloat(16);
	private static float[] ff = new float[16];
	private static Transform mView2 = new Transform();
	private static FloatBuffer fb2 = MemoryUtil.memAllocFloat(16);
	private static float[] ff2 = new float[16];
	private final SingleBlockDrawer drawer = new SingleBlockDrawer();
	private Transform orientation = new Transform();
	private Transform orientationTmp = new Transform();
	private Matrix3f rot = new Matrix3f();
	private short blockType = 1;
	private int shapeOrienation = 0;
	private int sidedOrienation = 0;
	private boolean init;

	public GUI3DBlockElement(InputState state) {
		super(state);
		orientation.setIdentity();
		orientationTmp.setIdentity();
	}

	public static void setMatrix() {
		Matrix4f modelviewMatrix = Controller.modelviewMatrix;
		fb.rewind();
		Matrix4fTools.store(modelviewMatrix, fb);
		fb.rewind();
		fb.get(ff);
		mView.setFromOpenGLMatrix(ff);
		mView.origin.set(0, 0, 0);

	}

	@Override
	public void cleanUp() {

	}

	@Override
	public void draw() {
		if (isInvisible()) {
			return;
		}
		if (!init) {
			onInit();
		}

		GlUtil.glPushMatrix();

		setInside(false);

		transform();

		if (isMouseUpdateEnabled()) {
			checkMouseInside();
		}
		drawBlock(blockType);
		for (AbstractSceneNode f : getChilds()) {
			f.draw();
		}
		GlUtil.glPopMatrix();

	}

	@Override
	public void onInit() {
		init = true;
	}

	public void drawBlock(short type) {
		if (type == Element.TYPE_NONE) {
			return;
		}
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glDepthMask(false);
		Matrix4f modelviewMatrix = Controller.modelviewMatrix;
		fb2.rewind();
		Matrix4fTools.store(modelviewMatrix, fb2);
		fb2.rewind();
		fb2.get(ff2);
		mView2.setFromOpenGLMatrix(ff2);

//		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

		GUIElement.enableOrthogonal3d();
		
		
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		GlUtil.glMultMatrix(mView2);

		//draw on top of screen

		GlUtil.glPushMatrix();
		int x = 0;
		int y = 0;
		//			int y = (buildIconNum) / 16;

		GlUtil.translateModelview(x, y, 100);

		//y axis has to be flipped (cause: orthographic projection used)
		GlUtil.scaleModelview(32f, -32f, 32f);
		//		if(ElementKeyMap.getInfo(type).getBlockStyle() == BlockStyle.SPRITE){
		//			orientationTmp.basis.set(mView.basis);
		//			mView.basis.setIdentity();
		//		}else{
		rot.set(orientation.basis);
		mView.basis.mul(rot);
		//		}

		GlUtil.glMultMatrix(mView);
		Transform m = new Transform();
		m.setIdentity();
		if(((GameClientState) getState()).getCurrentPlayerObject() != null){
			m.basis.set(((GameClientState) getState()).getCurrentPlayerObject().getWorldTransform().basis);
		}
		GlUtil.glMultMatrix(m);

		//		if(ElementKeyMap.getInfo(type).getBlockStyle() == BlockStyle.SPRITE){
		//			mView.basis.set(orientationTmp.basis);
		//		}

		drawer.useSpriteIcons = false;
		drawer.activateBlinkingOrientation(ElementKeyMap.getInfo(type).isOrientatable());

		drawer.setShapeOrientation24((byte) shapeOrienation);
		drawer.setSidedOrientation((byte) sidedOrienation);
		if (EngineSettings.G_DRAW_ADV_BUILDMODE_BLOCK_PREVIEW.isOn()) {
			drawer.drawType(type);
		}

		GlUtil.glPopMatrix();

		GUIElement.disableOrthogonal();

		//		if(write){
		//			System.err.println("WRITING SCREEN TO DISK");
		//			GlUtil.writeScreenToDisk("testTexture", "png", 1024, 1024, 4);
		//		}

		//		time += 0.15f;
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_NORMALIZE);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
//		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		GlUtil.glDepthMask(true);
		drawer.useSpriteIcons = true;
		
	}



	/**
	 * @return the blockType
	 */
	public short getBlockType() {
		return blockType;
	}

	/**
	 * @param blockType the blockType to set
	 */
	public void setBlockType(short blockType) {
		this.blockType = blockType;
	}

	@Override
	public float getHeight() {
		return 64;
	}

	@Override
	public float getWidth() {
		return 64;
	}

	public void setShapeOrientation(int blockOrientation) {
		this.shapeOrienation = blockOrientation;
	}

	public void setSidedOrientation(int blockOrientation) {
		this.sidedOrienation = blockOrientation;
	}

}
