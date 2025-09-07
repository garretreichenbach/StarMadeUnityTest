package org.schema.game.client.view.effects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.Mesh;

public class ShieldDrawerManager implements Drawable, Observer {

	public static float time;
	static Mesh mesh;
	private static ArrayList<ShieldDrawer> shieldDrawerPool = new ArrayList<ShieldDrawer>();
	private final Map<SegmentController, ShieldDrawer> shieldDrawers = new HashMap<SegmentController, ShieldDrawer>();
	private final ArrayList<ShieldDrawer> toAdd = new ArrayList<ShieldDrawer>();
	private final GameClientState state;
	private int shieldPointer;
	private ShieldDrawer toDraw[] = new ShieldDrawer[128];
	public ShieldDrawerManager(GameClientState state) {
		this.state = state;
	}

	private static ShieldDrawer get(ManagedSegmentController<?> s) {
		if (shieldDrawerPool.isEmpty()) {
			return new ShieldDrawer(s);
		} else {
			ShieldDrawer remove = shieldDrawerPool.remove(0);
			remove.set(s);
			return remove;
		}
	}

	private static void releaseDrawer(ShieldDrawer sd) {
		sd.reset();
		shieldDrawerPool.add(sd);
	}

	public void add(ManagedSegmentController<?> s) {

		toAdd.add(get(s));
	}

	@Override
	public void cleanUp() {
		
	}

	@Override
	public void draw() {
		if (!EngineSettings.G_DRAW_SHIELDS.isOn()) {
			return;
		}
		//		if(firstDraw){
		//			onInit();
		//		}
		//		if(!drawNeeded){
		//			return;
		//		}
		//		GlUtil.glEnable(GL11.GL_BLEND);
		//		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		//		if(ShaderLibrary.shieldShader == null){
		//			try {
		//				ShaderLibrary.shieldShader = new Shader(DataUtil.dataPath
		//						+ "/shader/shieldhit/shieldhit.vert.glsl", DataUtil.dataPath
		//						+ "/shader/shieldhit/shieldhit.frag.glsl");
		//			} catch (ResourceException e) {
		//				e.printStackTrace();
		//				EngineSettings.G_DRAW_SHIELDS.setOn(false);
		//				state.getController().popupAlertTextMessage(
		//						"ERROR: something went wrong\n" +
		//								"activating the shield drawer.\n" +
		//								"please send an error log\n" +
		//								"Shield Drawer has been deactivated", 0);
		//			}
		//		}
		//		ShaderLibrary.shieldShader.loadWithoutUpdate();
		//
		//		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		//		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, Controller.getResLoader().getSprite("shield_tex").getMaterial().getTexture().getTextureId());
		//		GlUtil.updateShaderInt(ShaderLibrary.shieldShader, "m_ShieldTex", 0);
		//
		//		GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
		//		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, GameResourceLoader.effectTextures[0].getTextureId());
		//		GlUtil.updateShaderInt(ShaderLibrary.shieldShader, "m_Distortion", 1);
		//
		//		GlUtil.glActiveTexture(GL13.GL_TEXTURE2);
		//		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, GameResourceLoader.effectTextures[1].getTextureId());
		//		GlUtil.updateShaderInt(ShaderLibrary.shieldShader, "m_Noise", 2);
		//
		////		mesh.loadVBO();
		//
		//		for(int i = 0; i < shieldPointer; i++){
		//			toDraw[i].draw();
		//		}
		//
		////		mesh.unloadVBO();
		//		ShaderLibrary.shieldShader.unloadWithoutExit();
		//		GlUtil.glDisable(GL11.GL_BLEND);
		//
		//
		//
		//
		//		GlUtil.glActiveTexture(GL13.GL_TEXTURE2);
		//		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		//		GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
		//		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		//		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		//		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

	}

	@Override
	public boolean isInvisible() {
				return false;
	}

	@Override
	public void onInit() {
		mesh = (Mesh) (Controller.getResLoader().getMesh("Sphere").getChilds().get(0));
	}

	public void clear() {
		for (ShieldDrawer sd : shieldDrawers.values()) {
			sd.deleteObserver(this);
			releaseDrawer(sd);
		}
		shieldPointer = 0;
		shieldDrawers.clear();
	}

	public ShieldDrawer get(SegmentController ship) {
		return shieldDrawers.get(ship);
	}

	/**
	 * @return the state
	 */
	public GameClientState getState() {
		return state;
	}

	@Override
	public void update(Observable o, Object arg) {
		if (((Boolean) arg) == true) {
			boolean needsAdd = true;
			for (int i = 0; i < toDraw.length && i < shieldPointer; i++) {
				if (toDraw[i] == o) {
					needsAdd = false;
					break;
				}
			}
			//			System.err.println("NOTIFIED OBSERVER OF HIT!");
			if (needsAdd && shieldPointer < toDraw.length) {
				toDraw[shieldPointer] = (ShieldDrawer) o;
				shieldPointer++;
			}

		} else {
			//			System.err.println("NOTIFIED OBSERVER OF HIT END!");
			if (shieldPointer < toDraw.length) {
				for (int i = 0; i < toDraw.length; i++) {
					if (toDraw[i] == (ShieldDrawer) o) {
						toDraw[i] = toDraw[shieldPointer - 1];
						shieldPointer--;
						break;
					}
				}
			}
		}

	}

	public void update(Timer timer) {
		time += timer.getDelta();
		if (time > 1) {
			time -= (int) time;
		}
		while (!toAdd.isEmpty()) {
			ShieldDrawer n = toAdd.remove(0);
			n.addObserver(this);
			shieldDrawers.put(n.controller, n);
		}
		for (int i = 0; i < shieldPointer; i++) {
			toDraw[i].update(timer);
		}
	}

}
