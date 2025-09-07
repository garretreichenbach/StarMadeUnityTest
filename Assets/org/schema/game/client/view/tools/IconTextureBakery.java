package org.schema.game.client.view.tools;

import com.bulletphysics.linearmath.Transform;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.Keyboard;
import org.schema.schine.resource.FileExt;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import java.io.File;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * Utility class for generating block icon textures.
 * <br/>It does this by rendering each block in the game onto the screen in a grid, then saving that output to a file.
 * <br/>It can also render each block individually to a file.
 */
public class IconTextureBakery {

	public static boolean normalWrite = !EngineSettings.ICON_BAKERY_SINGLE_ICONS.isOn();
	private final Transform orientation = new Transform();
	private final Transform orientationTmp = new Transform();
	private final Matrix3f rot = new Matrix3f();
	private final Transform mView = new Transform();
	private final FloatBuffer fb = MemoryUtil.memAllocFloat(16);
	private final float[] ff = new float[16];
	private final ArrayList<IconRotationData> iconRotationList = new ArrayList<>() {
		{ //...Just don't question it
			add(new IconRotationData(ElementKeyMap.THRUSTER_ID, () -> {
				GlUtil.rotateModelview(-90, 0, 1, 0);
				GlUtil.rotateModelview(25, 0, 0, 1);
				GlUtil.rotateModelview(135, 0, 1, 0);
				GlUtil.rotateModelview(-90, 0, 1, 0);
			}));
			add(new IconRotationData(ElementKeyMap.RAIL_BLOCK_BASIC, () -> {
				GlUtil.rotateModelview(-90, 0, 1, 0);
				GlUtil.rotateModelview(-25, 0, 0, 1);
				GlUtil.rotateModelview(135, 0, 1, 0);
				GlUtil.rotateModelview(-90, 0, 1, 0);
			}));
			add(new IconRotationData(ElementKeyMap.RAIL_BLOCK_DOCKER, () -> {
				GlUtil.rotateModelview(-90, 0, 1, 0);
				GlUtil.rotateModelview(-25, 0, 0, 1);
				GlUtil.rotateModelview(135, 0, 1, 0);
				GlUtil.rotateModelview(-90, 0, 1, 0);
			}));
			add(new IconRotationData(ElementKeyMap.RAIL_BLOCK_CCW, () -> {
				GlUtil.rotateModelview(-90, 0, 1, 0);
				GlUtil.rotateModelview(-25, 0, 0, 1);
				GlUtil.rotateModelview(135, 0, 1, 0);
				GlUtil.rotateModelview(-90, 0, 1, 0);
			}));
			add(new IconRotationData(ElementKeyMap.RAIL_BLOCK_CW, () -> {
				GlUtil.rotateModelview(-90, 0, 1, 0);
				GlUtil.rotateModelview(-25, 0, 0, 1);
				GlUtil.rotateModelview(135, 0, 1, 0);
				GlUtil.rotateModelview(-90, 0, 1, 0);
			}));
			add(new IconRotationData(ElementKeyMap.RAIL_LOAD, () -> {
				GlUtil.rotateModelview(-90, 0, 1, 0);
				GlUtil.rotateModelview(-25, 0, 0, 1);
				GlUtil.rotateModelview(135, 0, 1, 0);
				GlUtil.rotateModelview(-90, 0, 1, 0);
			}));
			add(new IconRotationData(ElementKeyMap.RAIL_UNLOAD, () -> {
				GlUtil.rotateModelview(-90, 0, 1, 0);
				GlUtil.rotateModelview(-25, 0, 0, 1);
				GlUtil.rotateModelview(135, 0, 1, 0);
				GlUtil.rotateModelview(-90, 0, 1, 0);
			}));
			add(new IconRotationData(ElementKeyMap.RAIL_BLOCK_TURRET_Y_AXIS, () -> {
				GlUtil.rotateModelview(-90, 0, 1, 0);
				GlUtil.rotateModelview(-25, 0, 0, 1);
				GlUtil.rotateModelview(135, 0, 1, 0);
				GlUtil.rotateModelview(-90, 0, 1, 0);
			}));
			add(new IconRotationData(ElementKeyMap.PICKUP_AREA, () -> {
				GlUtil.rotateModelview(-90, 0, 1, 0);
				GlUtil.rotateModelview(-25, 0, 0, 1);
				GlUtil.rotateModelview(135, 0, 1, 0);
				GlUtil.rotateModelview(-90, 0, 1, 0);
			}));
			add(new IconRotationData(ElementKeyMap.PICKUP_RAIL, () -> {
				GlUtil.rotateModelview(-90, 0, 1, 0);
				GlUtil.rotateModelview(-25, 0, 0, 1);
				GlUtil.rotateModelview(135, 0, 1, 0);
				GlUtil.rotateModelview(-90, 0, 1, 0);
			}));
			add(new IconRotationData(ElementKeyMap.EXIT_SHOOT_RAIL, () -> {
				GlUtil.rotateModelview(-90, 0, 1, 0);
				GlUtil.rotateModelview(-25, 0, 0, 1);
				GlUtil.rotateModelview(135, 0, 1, 0);
				GlUtil.rotateModelview(-90, 0, 1, 0);
			}));
			add(new IconRotationData(ElementKeyMap.TRACTOR_BEAM_COMPUTER, () -> {
				GlUtil.rotateModelview(-90, 0, 1, 0);
				GlUtil.rotateModelview(25, 0, 0, 1);
				GlUtil.rotateModelview(135, 0, 1, 0);
				GlUtil.rotateModelview(-90, 0, 1, 0);
			}));
			add(new IconRotationData(ElementKeyMap.EFFECT_HEAT_COMPUTER, () -> {
				GlUtil.rotateModelview(-90, 0, 1, 0);
				GlUtil.rotateModelview(-25, 0, 0, 1);
				GlUtil.rotateModelview(135, 0, 1, 0);
				GlUtil.rotateModelview(-90, 0, 1, 0);
			}));
			add(new IconRotationData(ElementKeyMap.EFFECT_EM_COMPUTER, () -> {
				GlUtil.rotateModelview(-90, 0, 1, 0);
				GlUtil.rotateModelview(-25, 0, 0, 1);
				GlUtil.rotateModelview(135, 0, 1, 0);
				GlUtil.rotateModelview(-90, 0, 1, 0);
			}));
			add(new IconRotationData(ElementKeyMap.EFFECT_KINETIC_COMPUTER, () -> {
				GlUtil.rotateModelview(-90, 0, 1, 0);
				GlUtil.rotateModelview(-25, 0, 0, 1);
				GlUtil.rotateModelview(135, 0, 1, 0);
				GlUtil.rotateModelview(-90, 0, 1, 0);
			}));
		}
	};
	public int sheetNumber;
	int xMod, yMod;
	int xM = 200, yM = 200;
	private boolean write;
	private boolean orderedIcons;
	private SingleBlockDrawer drawer;

	public IconTextureBakery() {
		orientation.setIdentity();
		orientationTmp.setIdentity();
	}

	public void bake() throws GLException {
		FrameBufferObjects fb = new FrameBufferObjects("IconBakery", 1024, 1024);
		fb.initialize();
		fb.enable();
		GL11.glClearColor(0, 0, 0, 0);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
		if(normalWrite) {
			xMod = 32;
			yMod = 32;//256+32;//512+

			GL11.glViewport(0, 0, 1024, 1024);
			write = true;
			draw();
			write = false;
			GlUtil.writeScreenToDisk("./data/image-resource/build-icons-" + StringTools.formatTwoZero(sheetNumber) + "-16x16-gui-", "png", 1024, 1024, 4, fb);
		} else {

			File f = new FileExt("./iconbakery/");
			if(!f.exists()) {
				f.mkdir();
			}

			int sizeX = EngineSettings.ICON_BAKERY_SINGLE_RESOLUTION.getInt();
			int sizeY = EngineSettings.ICON_BAKERY_SINGLE_RESOLUTION.getInt();

			File f2 = new FileExt("./iconbakery/" + sizeX + "x" + sizeY + "/");
			if(!f2.exists()) {
				f2.mkdir();
			}

			xMod = sizeX / 2;
			yMod = sizeY / 2;//256+32;//512+
			GL11.glViewport(0, 0, sizeX, sizeY);

			for(short e : ElementKeyMap.typeList()) {
				ElementInformation info = ElementKeyMap.getInfo(e);
//				if(info.slab == 0){
				write = true;
				GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
				drawSingle(e, sizeX / 2, sizeX, sizeY);
				write = false;

				String n = info.getName().replaceAll("/", "-").replaceAll(" ", "_");
				String path = "./iconbakery/" + sizeX + "x" + sizeY + "/" + n;
				System.err.println("SHEET: " + sheetNumber + " WRITING SCREEN TO DISK: " + path);
				GlUtil.writeScreenToDisk(path, "png", sizeX, sizeY, 4, fb);
//				}
			}
		}
		GL11.glViewport(0, 0, GLFrame.getWidth(), GLFrame.getHeight());
		fb.disable();
		fb.cleanUp();
	}

	private void orderIcons() {
		for(short id : ElementKeyMap.typeList()) {
			if(ElementKeyMap.isValidType(id)) {
				IconRotationData rotationData = new IconRotationData(id, () -> {
					ElementInformation info = ElementKeyMap.getInfo(id);
					if(info.hasLod()) {
						GlUtil.rotateModelview(90, 0, 0, 1);
						GlUtil.rotateModelview(90, 0, 1, 0);
						GlUtil.rotateModelview(-20, 0, 1, 0);
						GlUtil.rotateModelview(-45, 0, 0, 1);
					} else {
						if(info.getBlockStyle() != BlockStyle.NORMAL) {
							GlUtil.rotateModelview(-90, 0, 1, 0);
							GlUtil.rotateModelview(-25, 0, 0, 1);
						} else if(info.sideTexturesPointToOrientation || info.isMainCombinationControllerB() || info.isSupportCombinationControllerB() || info.isEffectCombinationController()) {
							GlUtil.rotateModelview(25, 1, 0, 0);
						} else {
							GlUtil.rotateModelview(-25, 1, 0, 0);
						}
						GlUtil.rotateModelview(135, 0, 1, 0);
						if(info.slab != 0) GlUtil.rotateModelview(180, 0, 1, 0);
					}
				});
				if(!iconRotationList.contains(rotationData)) iconRotationList.add(rotationData);
			}
		}
		iconRotationList.sort(IconRotationData::compareTo);
//		resetBuildIcons();
		orderedIcons = true;
	}

	private void resetBuildIcons() {
		int iconCounter = 0;
		for(IconRotationData entry : iconRotationList) {
			entry.getInfo().setBuildIconNum(iconCounter);
			iconCounter++;
		}
		ElementKeyMap.writeDocument(new File("./data/config/BlockConfig.xml"), ElementKeyMap.getCategoryHirarchy(), ElementKeyMap.fixedRecipes);
	}

	public void draw() {
		if(!orderedIcons) orderIcons();
		Matrix4f modelviewMatrix = Controller.modelviewMatrix;
		fb.rewind();
		Matrix4fTools.store(modelviewMatrix, fb);
		fb.rewind();
		fb.get(ff);
		mView.setFromOpenGLMatrix(ff);
		mView.origin.set(0, 0, 0);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

		if(write) {
			GUIElement.enableOrthogonal3d(1024, 1024);
		} else {
			GUIElement.enableOrthogonal3d();
		}

		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

//		for(short e : ElementKeyMap.typeList()) {
		for(IconRotationData entry : iconRotationList) {
			//draw on top of screen
			ElementInformation info = entry.getInfo();
			if(info == null) continue;
			int buildIconNum = info.buildIconNum;
			if(buildIconNum / 256 != sheetNumber) continue;
			GlUtil.glPushMatrix();
			int x = buildIconNum % 16;
			int y = (buildIconNum - sheetNumber * 256) / 16;
			GlUtil.translateModelview(xMod + x * 64, yMod + y * 64, 0);
			//y axis has to be flipped (cause: orthographic projection used)
			GlUtil.scaleModelview(32.0f, -32.0f, 32.0f);
			if(info.getBlockStyle() == BlockStyle.SPRITE) {
				orientationTmp.basis.set(mView.basis);
				mView.basis.setIdentity();
			} else {
				rot.set(orientation.basis);
				mView.basis.mul(rot);
			}

			GlUtil.glMultMatrix(mView);
			if(info.getBlockStyle() == BlockStyle.SPRITE) {
				mView.basis.set(orientationTmp.basis);
			}

			if(info.getBlockStyle() == BlockStyle.NORMAL && !info.sideTexturesPointToOrientation && !(info.isMainCombinationControllerB() || info.isSupportCombinationControllerB() || info.isEffectCombinationController())) {
				GlUtil.rotateModelview(180, 0, 1, 0);
			}

			GlUtil.glDisable(GL11.GL_DEPTH_TEST);
			drawer = new SingleBlockDrawer();

			if(!Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) && info.blockStyle != BlockStyle.SPRITE) {
				entry.apply();
			}

			drawer.drawType(entry.typeID());
			GlUtil.glPopMatrix();
		}

		GUIElement.disableOrthogonal();

		AbstractScene.mainLight.draw();
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_NORMALIZE);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
	}

	public void drawSingle(short e, float size, int width, int height) {
		Matrix4f modelviewMatrix = Controller.modelviewMatrix;
		fb.rewind();
		Matrix4fTools.store(modelviewMatrix, fb);
		fb.rewind();
		fb.get(ff);
		mView.setFromOpenGLMatrix(ff);
		mView.origin.set(0, 0, 0);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

		if(write) {
			GUIElement.enableOrthogonal3d(width, height);
		} else {
			GUIElement.enableOrthogonal3d();
		}
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);

		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		//draw on top of screen
		int buildIconNum = ElementKeyMap.getInfo(e).getBuildIconNum();

		//		if(buildIconNum / 256 != sheetNumber){
		////				System.err.println("Skipped: "+buildIconNum+" for sheet: "+sheetNumber);
		//			return false;
		//		}
		GlUtil.glPushMatrix();
		int x = buildIconNum % 16;
		int y = (buildIconNum - sheetNumber * 256) / 16;
		//			int y = (buildIconNum) / 16;

		GlUtil.translateModelview(xMod, yMod, 0);

		//y axis has to be flipped (cause: orthographic projection used)
		GlUtil.scaleModelview(size, -size, size);
		if(ElementKeyMap.getInfo(e).getBlockStyle() == BlockStyle.SPRITE) {
			orientationTmp.basis.set(mView.basis);
			mView.basis.setIdentity();
		} else {
			rot.set(orientation.basis);
			mView.basis.mul(rot);
		}

		GlUtil.glMultMatrix(mView);
		if(ElementKeyMap.getInfo(e).getBlockStyle() == BlockStyle.SPRITE) {
			mView.basis.set(orientationTmp.basis);
		}

		SingleBlockDrawer drawer = new SingleBlockDrawer();
		drawer.setLightAll(false);
		GlUtil.glPushMatrix();
		if(ElementKeyMap.getInfo(e).getBlockStyle() != BlockStyle.NORMAL) {
			GlUtil.rotateModelview(EngineSettings.ICON_BAKERY_BLOCKSTYLE_ROTATE_DEG.getFloat(), 0, 1, 0);
		}
		drawer.drawType(e);

		GlUtil.glPopMatrix();
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

	}

	public void drawTest() {
		xMod = xM;
		yMod = yM;
		draw();
	}

	public void handleKeyEvent(KeyEventInterface e) {
		switch(e.getKey()) {
			case GLFW.GLFW_KEY_W -> yM += 8;
			case GLFW.GLFW_KEY_A -> xM -= 8;
			case GLFW.GLFW_KEY_S -> yM -= 8;
			case GLFW.GLFW_KEY_D -> xM += 8;
			case GLFW.GLFW_KEY_UP -> {
				if(drawer != null) drawer.useSpriteIcons = !drawer.useSpriteIcons;
			}
			case GLFW.GLFW_KEY_RIGHT -> {
				int maxLayers = ElementKeyMap.highestType / 256 + 1;
				sheetNumber = (sheetNumber + 1) % maxLayers;
				break;
			}
			case GLFW.GLFW_KEY_LEFT -> {
				int maxLayers = ElementKeyMap.highestType / 256 + 1;
				if(sheetNumber - 1 < 0) {
					sheetNumber = maxLayers - 1;
				} else {
					sheetNumber = (sheetNumber - 1);
				}
				break;
			}
		}
	}

}
