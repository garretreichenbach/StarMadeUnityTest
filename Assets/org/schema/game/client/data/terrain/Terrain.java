package org.schema.game.client.data.terrain;

import java.io.IOException;
import java.util.Vector;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.schema.common.util.data.DataUtil;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.terrain.geimipmap.LODGeomap;
import org.schema.game.client.data.terrain.geimipmap.TerrainQuad;
import org.schema.schine.graphicsengine.core.AbstractScene;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.AbstractSceneNode;
import org.schema.schine.graphicsengine.shader.HoffmannSky;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.texture.Texture;

public class Terrain extends AbstractSceneNode implements LODLoadableInterface {
	public static int currently_drawn;
	private static TerrainRegion[] regions;
	private static TerrainShaderable terrainShaderable;
	private int specularMapPointer;
	private int normalMapPointer;
	private int heightMapPointer;
	private Vector<LODGeomap> geoMapsToLoad;
	private float tiling_factor = 20;
	private TerrainQuad[] root;
	private boolean firstDraw = true;
	private float[] heightMap;
	private int blocksize;
	private Vector3f stepScale;
	private int size;
	private Vector<Vector3f> locations;
	private GameClientState state;

	public Terrain(GameClientState state) {
		this.state = state;
		geoMapsToLoad = new Vector<LODGeomap>();
	}
	public Terrain(GameClientState state, int heightMapTexPoint, int normalMapTexPoint, int specularMapTexPoint, int[] heightMap, int size, int blocksize, Vector3f stepScale) {
		this(state);

		heightMapPointer = heightMapTexPoint;
		//		normalMapPointer = normalMapTexPoint;
		//		specularMapPointer = specularMapTexPoint;

		this.blocksize = blocksize;
		this.stepScale = stepScale;
		this.size = size;
		locations = new Vector<Vector3f>();
		locations.add(new Vector3f(Controller.getCamera().getPos()));
		float[] hm = new float[size * size];
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				int a = (y * (size - 1)) % (heightMap.length);
				int b = x % (size - 1);
				hm[y * size + x] = (heightMap[a + b]);
			}
		}
		this.heightMap = hm;

	}

	@Override
	public void cleanUp() {
		//TODO clean up terrain

	}

	@Override
	public void draw() {
		if (firstDraw) {
			onInit();
		}

		currently_drawn = 0;

		//		System.err.println("normal map "+normalMapPointer);
		//		System.err.println("spec map "+specularMapPointer);

		Vector3f camPos = new Vector3f(Controller.getCamera().getPos());
		locations.set(0, camPos);
		//		this.terrainShaderable = ((OldTerrain)getSurfaceMesh().getChilds().get(0)).getTerrainShaderable();
		int xPos = (Math.round(camPos.x / ((size - 1) * 100)));
		int zPos = (Math.round(camPos.z / ((size - 1) * 100)));

		Vector3f planetPos = new Vector3f(getPos());
		int sou = 1;
		int i = 0;

		ShaderLibrary.hoffmanTerrainShader.setShaderInterface(terrainShaderable);
		ShaderLibrary.hoffmanTerrainShader.load();
		GlUtil.glEnable(GL11.GL_CULL_FACE);

		GL11.glCullFace(GL11.GL_BACK);

		for (int x = -sou; x <= sou; x++) {
			for (int z = -sou; z <= sou; z++) {
				//				System.err.println("drawing transformationArray "+i);
				root[i].setPos(
						((x + xPos) * ((size - 1) * 100)),
						planetPos.y,
						((z + zPos) * ((size - 1) * 100)));

				root[i].update(locations);

				root[i].draw();
				i++;
			}

		}
		GlUtil.glDisable(GL11.GL_CULL_FACE);

		ShaderLibrary.hoffmanTerrainShader.unload();
		AbstractScene.infoList.add("currently drawn terrain patches: " + currently_drawn + ", surfPos: " + xPos + ", " + zPos + ";");
	}

	@Override
	public void onInit() {
		if (terrainShaderable == null) {
			terrainShaderable = new TerrainShaderable();
		}
		if (regions == null) {
			makeRegions();
		}
		normalMapPointer = Controller.getResLoader().getSprite("normalmap2").getMaterial().getTexture().getTextureId();
		specularMapPointer = Controller.getResLoader().getSprite("specmap2").getMaterial().getTexture().getTextureId();
		//specularMap
		root = new TerrainQuad[9];
		//		surfaceMesh[0] = new Terrain(getState(), heightMapTex, getNormMapId(), getSpecularMapId(), heightMap, textureWidth+1, 512, new Vector3f(100,100,100));
		//
		//		surfaceMesh[0].onInit( );
		//		surfaceMesh[0].setPos(getPos());

		for (int i = 0; i < 9; i++) {
			root[i] = new TerrainQuad(getName(), blocksize, size, stepScale, heightMap, this);
		}

		firstDraw = false;
	}

	@Override
	public Terrain clone() {
		Terrain t = new Terrain(state);
		t.specularMapPointer = specularMapPointer;
		t.normalMapPointer = normalMapPointer;
		t.heightMapPointer = heightMapPointer;
		t.root = root.clone();
		t.firstDraw = firstDraw;
		t.heightMap = heightMap;
		t.blocksize = blocksize;
		t.stepScale = stepScale;
		t.size = size;
		t.locations = new Vector<Vector3f>(locations);

		return t;
	}

	/**
	 * @return the geoMapsToLoad
	 */
	@Override
	public Vector<LODGeomap> getGeoMapsToLoad() {
		return geoMapsToLoad;
	}

	@Override
	public void load() {
		if (firstDraw) {
			onInit();
		}
		if (geoMapsToLoad.size() > 0) {
			geoMapsToLoad.get(0).doLoadStep();
			if (geoMapsToLoad.get(0).isMeshLoaded()) {
				geoMapsToLoad.remove(0);
				System.err.println("LOADING MAPS: " + geoMapsToLoad.size());
			}
			return;
		}

		setLoaded(true);
	}

	/**
	 * @param geoMapsToLoad the geoMapsToLoad to set
	 */
	public void setGeoMapsToLoad(Vector<LODGeomap> geoMapsToLoad) {
		this.geoMapsToLoad = geoMapsToLoad;
	}

	/**
	 * @return the firstDraw
	 */
	public boolean isFirstDraw() {
		return firstDraw;
	}

	/**
	 * @param firstDraw the firstDraw to set
	 */
	public void setFirstDraw(boolean firstDraw) {
		this.firstDraw = firstDraw;
	}

	/**
	 * Make regions.
	 *
	 * @param gl  the gl
	 * @param glu the glu
	 */
	private void makeRegions() {
		if (regions == null) {
			float scale = 1;
			regions = new TerrainRegion[4];
			try {
				regions[TerrainRegion.DIRT] = new TerrainRegion(Controller.getTexLoader().getTexture2D(
						DataUtil.dataPath + "/heightmaps/dirt.png", true), 0 * scale, 80 * scale);
				regions[TerrainRegion.GRASS] = new TerrainRegion(Controller.getTexLoader().getTexture2D(
						DataUtil.dataPath + "/heightmaps/grass.png", true), 80 * scale, 130 * scale);
				regions[TerrainRegion.ROCK] = new TerrainRegion(Controller.getTexLoader().getTexture2D(
						DataUtil.dataPath + "/heightmaps/rock.png", true), 130 * scale, 180 * scale);
				regions[TerrainRegion.SNOW] = new TerrainRegion(Controller.getTexLoader().getTexture2D(
						DataUtil.dataPath + "/heightmaps/snow.png", true), 180 * scale, 255 * scale);
			} catch (IOException e) {

				e.printStackTrace();
			}
		}

	}

	/**
	 * The Class TerrainRegion.
	 */

	private class TerrainRegion {
		/**
		 * The Constant TERRAIN_REGION_DIRT.
		 */
		private static final int DIRT = 0;

		/**
		 * The Constant TERRAIN_REGION_GRASS.
		 */
		private static final int GRASS = 1;

		/**
		 * The Constant TERRAIN_REGION_ROCK.
		 */
		private static final int ROCK = 2;

		/**
		 * The Constant TERRAIN_REGION_SNOW.
		 */
		private static final int SNOW = 3;
		/**
		 * The texture.
		 */
		private Texture texture;

		/**
		 * The maxThis.
		 */
		private float min, max;

		/**
		 * Instantiates a new heightField region.
		 *
		 * @param texture the texture
		 * @param minThis the minThis
		 * @param maxThis the maxThis
		 */
		public TerrainRegion(Texture texture, float min, float max) {
			super();
			this.texture = texture;
			this.min = min;
			this.max = max;
		}

		/**
		 * Gets the maxThis.
		 *
		 * @return the maxThis
		 */
		public float getMax() {
			return max;
		}

		/**
		 * Gets the minThis.
		 *
		 * @return the minThis
		 */
		public float getMin() {
			return min;
		}

		/**
		 * Gets the texture.
		 *
		 * @return the texture
		 */
		public Texture getTexture() {
			return texture;
		}

	}

	public class TerrainShaderable extends HoffmannSky {

		@Override
		public void onExit() {
			if (isFirstDraw()) {
				return;
			}

			regions[TerrainRegion.DIRT].getTexture().unbindFromIndex();
			regions[TerrainRegion.GRASS].getTexture().unbindFromIndex();
			regions[TerrainRegion.ROCK].getTexture().unbindFromIndex();
			regions[TerrainRegion.SNOW].getTexture().unbindFromIndex();

			GlUtil.glActiveTexture(GL13.GL_TEXTURE4);
			GlUtil.glDisable(GL11.GL_TEXTURE_2D);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

			GlUtil.glActiveTexture(GL13.GL_TEXTURE5);
			GlUtil.glDisable(GL11.GL_TEXTURE_2D);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

			GlUtil.glActiveTexture(GL13.GL_TEXTURE6);
			GlUtil.glDisable(GL11.GL_TEXTURE_2D);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

			GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		}

		@Override
		public void updateShaderParameters(Shader shader) {
			if (isFirstDraw()) {
				return;
			}
			super.updateShaderParameters(shader);

			// if(state != null){
			// GlUtil.updateShaderFloat( shader, "mapwidth", state.mapWidth *
			// FIELD_SIZE);
			// GlUtil.updateShaderFloat( shader, "mapheight", state.mapHeight *
			// FIELD_SIZE);
			// }

			// Update the heightField tiling factor.

			GlUtil.updateShaderFloat(shader, "terrainTilingFactor",
					tiling_factor);

			// Update height map scale value.

			GlUtil.updateShaderFloat(shader, "heightMapScale", 1);

			// Update dirt heightField region.

			GlUtil.updateShaderFloat(shader, "dirtRegion.max",
					regions[TerrainRegion.DIRT].getMax());

			GlUtil.updateShaderFloat(shader, "dirtRegion.min",
					regions[TerrainRegion.DIRT].getMin());

			// Update grass heightField region.

			GlUtil.updateShaderFloat(shader, "grassRegion.max",
					regions[TerrainRegion.GRASS].getMax());

			GlUtil.updateShaderFloat(shader, "grassRegion.min",
					regions[TerrainRegion.GRASS].getMin());

			// Update rock heightField region.

			GlUtil.updateShaderFloat(shader, "rockRegion.max",
					regions[TerrainRegion.ROCK].getMax());

			GlUtil.updateShaderFloat(shader, "rockRegion.min",
					regions[TerrainRegion.ROCK].getMin());

			// Update snow heightField region.

			GlUtil.updateShaderFloat(shader, "snowRegion.max",
					regions[TerrainRegion.SNOW].getMax());

			GlUtil.updateShaderFloat(shader, "snowRegion.min",
					regions[TerrainRegion.SNOW].getMin());

			GlUtil.updateShaderVector4f(shader, "light.ambient",
					AbstractScene.mainLight.getAmbience());
			GlUtil.updateShaderVector4f(shader, "light.diffuse",
					AbstractScene.mainLight.getDiffuse());
			GlUtil.updateShaderVector4f(shader, "light.specular",
					AbstractScene.mainLight.getSpecular());
			//			GlUtil.updateShaderVector4f( shader, "light.position",
			//					AbstractScene.mainLight.getPos().x, AbstractScene.mainLight
			//							.getPos().y, AbstractScene.mainLight.getPos().z, 1);

			GlUtil.updateShaderFloat(shader, "shininess",
					AbstractScene.mainLight.getShininess()[0]);
			int old = 0;

			// Bind textures.

			regions[TerrainRegion.DIRT].getTexture().bindOnShader(
					GL13.GL_TEXTURE1, 1, "dirtColorMap", shader);
			regions[TerrainRegion.GRASS].getTexture().bindOnShader(
					GL13.GL_TEXTURE2, 2, "grassColorMap", shader);
			regions[TerrainRegion.ROCK].getTexture().bindOnShader(
					GL13.GL_TEXTURE3, 3, "rockColorMap", shader);
			regions[TerrainRegion.SNOW].getTexture().bindOnShader(
					GL13.GL_TEXTURE4, 4, "snowColorMap", shader);

			GlUtil.glActiveTexture(GL13.GL_TEXTURE5);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, heightMapPointer);
			GlUtil.updateShaderInt(shader, "heightMap", 5);

			GlUtil.glActiveTexture(GL13.GL_TEXTURE6);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, normalMapPointer);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
			GlUtil.updateShaderInt(shader, "heightMap", 6);

			GlUtil.glActiveTexture(GL13.GL_TEXTURE7);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, specularMapPointer);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
			GlUtil.updateShaderInt(shader, "specularMap", 7);

			GlUtil.glActiveTexture(GL13.GL_TEXTURE8); // switch to unused texture unit
			// or shader wont take the
			// texture in the current slot
		}
	}

}
