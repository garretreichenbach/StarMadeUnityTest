package org.schema.game.client.view.effects;

import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.schema.game.client.view.GameResourceLoader;
import org.schema.game.client.view.MainGameGraphics;
import org.schema.game.common.controller.Ship;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.SilhouetteShaderAlpha;
import org.schema.schine.graphicsengine.texture.textureImp.Texture3D;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;

public class PlumeAndMuzzleDrawer implements Drawable, BloomEffectInterface {

	static Mesh plumeMesh;
	static Texture3D tex;
	private final ObjectArrayFIFOQueue<ExhaustPlumes> toAddPlumes = new ObjectArrayFIFOQueue<ExhaustPlumes>();
	private final ObjectArrayFIFOQueue<MuzzleFlash> toAddMuzzle = new ObjectArrayFIFOQueue<MuzzleFlash>();
	private final Map<Ship, ExhaustPlumes> plumeDrawers = new Object2ObjectOpenHashMap<Ship, ExhaustPlumes>();
	private final Map<Ship, MuzzleFlash> muzzleDrawers = new Object2ObjectOpenHashMap<Ship, MuzzleFlash>();
	private SilhouetteShaderAlpha silhouetteShaderAlpha;
	private boolean firstDraw = true;

	public void addMuzzle(MuzzleFlash p) {
		toAddMuzzle.enqueue(p);
	}

	public void addPlume(ExhaustPlumes p) {
		toAddPlumes.enqueue(p);
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public void draw() {
		if (firstDraw) {
			onInit();
		}
		if(MainGameGraphics.drawBloomedEffects()){
			return;
		}
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glEnable(GL12.GL_TEXTURE_3D);

		Shader shader = ShaderLibrary.exaustShader;

		shader.loadWithoutUpdate();
		GlUtil.glBindTexture(GL12.GL_TEXTURE_3D, PlumeAndMuzzleDrawer.tex.getId());
		GlUtil.updateShaderInt(shader, "noiseTex", 0);

		((Mesh) plumeMesh.getChilds().get(0)).loadMeshPointers();

		for (ExhaustPlumes plumDrawer : plumeDrawers.values()) {
			plumDrawer.raw = false;
			plumDrawer.draw();
		}

		GlUtil.updateShaderInt(shader, "noiseTex", 0);
		for (MuzzleFlash muzzleDrawer : muzzleDrawers.values()) {
			muzzleDrawer.raw = false;
			muzzleDrawer.draw();
		}

		((Mesh) plumeMesh.getChilds().get(0)).unloadMeshPointers();

		GlUtil.glDisable(GL12.GL_TEXTURE_3D);
		GlUtil.glDisable(GL11.GL_BLEND);

		shader.unloadWithoutExit();
	}
	@Override
	public void drawRaw(){
		if (firstDraw) {
			onInit();
		}
		silhouetteShaderAlpha.color.set(1,1,1,1);
		ShaderLibrary.silhouetteAlpha.setShaderInterface(silhouetteShaderAlpha);
		ShaderLibrary.silhouetteAlpha.load();
		
		((Mesh) plumeMesh.getChilds().get(0)).loadMeshPointers();

		for (ExhaustPlumes plumDrawer : plumeDrawers.values()) {
			plumDrawer.raw = true;
			plumDrawer.draw();
			plumDrawer.raw = false;
		}

		for (MuzzleFlash muzzleDrawer : muzzleDrawers.values()) {
			muzzleDrawer.raw = true;
			muzzleDrawer.draw();
			muzzleDrawer.raw = false;
		}

		((Mesh) plumeMesh.getChilds().get(0)).unloadMeshPointers();
		ShaderLibrary.silhouetteAlpha.unload();
	}
	@Override
	public boolean isInvisible() {
		return false;
	}
	
	@Override
	public void onInit() {
		if (plumeMesh == null) {
			plumeMesh = Controller.getResLoader().getMesh("ExhaustPlum");

		}
		if(silhouetteShaderAlpha == null){
			silhouetteShaderAlpha = new SilhouetteShaderAlpha();
		}
		if (tex == null) {
			tex = GameResourceLoader.noiseVolume;
		}
		firstDraw = false;
	}

	public void clear() {
		for (MuzzleFlash f : muzzleDrawers.values()) {
			f.cleanUp();

		}
		muzzleDrawers.clear();

		for (ExhaustPlumes f : plumeDrawers.values()) {
			f.cleanUp();

		}
		plumeDrawers.clear();

	}

	public void scheduleUpdatePlums() {
		//		System.err.println("[PLUM AND MUZZLE] UPDATING PLUMS");
		for (ExhaustPlumes p : plumeDrawers.values()) {
			p.scheduleUpdate();
		}
	}

	public void update(Timer timer) {
		while (!toAddPlumes.isEmpty()) {
			ExhaustPlumes n = toAddPlumes.dequeue();
			plumeDrawers.put(n.getShip(), n);
		}
		while (!toAddMuzzle.isEmpty()) {
			MuzzleFlash n = toAddMuzzle.dequeue();
			muzzleDrawers.put(n.getShip(), n);
		}
		for (ExhaustPlumes plumDrawer : plumeDrawers.values()) {
			plumDrawer.update(timer);
		}
		for (MuzzleFlash muzzleDrawer : muzzleDrawers.values()) {
			muzzleDrawer.update(timer);
		}
	}

}
