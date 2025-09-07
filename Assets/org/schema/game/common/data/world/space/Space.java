//package org.schema.game.common.data.world.space;
//
//import java.nio.ByteBuffer;
//
//import javax.vecmath.Vector3f;
//
//import org.lwjgl.opengl.GL11;
//import org.schema.game.client.data.GameClientState;
//import org.schema.game.common.data.world.galaxy.CelestialBody;
//import org.schema.game.common.data.world.galaxy.StarSystem;
//import org.schema.game.common.data.world.planet.PlanetInformations;
//import org.schema.game.common.data.world.space.textgen.SpaceGenerator;
//import org.schema.game.common.data.world.space.textgen.SpacePalette;
//import org.schema.game.common.data.world.space.textgen.StandardSpacePalette;
//import org.schema.game.common.data.world.space.textgen.StandartSpaceGenerator;
//import org.schema.schine.graphicsengine.core.Controller;
//import org.schema.schine.graphicsengine.core.GlUtil;
//import org.schema.schine.graphicsengine.forms.Mesh;
//import org.schema.schine.graphicsengine.shader.ErrorDialogException;
//import org.schema.schine.graphicsengine.texture.Material;
//import org.schema.schine.graphicsengine.texture.Texture;
//
//
//public class Space  extends CelestialBody{
//	private PlanetInformations infos;
//	private int textureHeight;
//	private int textureWidth;
//	private SpaceGenerator generator;
//	private int baseMapId;
//	private Texture baseTex;
//	private Mesh sphere;
//	private Material spaceMaterial;
//	public Space(GameClientState state, StarSystem system){
//		super(state, TYPE_SPACE, system);
//	}
//
//	@Override
//	public void generate(){
//		baseMapId = getBaseMapData();
//		this.setMaterial(new Material());
//		baseTex = new Texture(GL11.GL_TEXTURE_2D, baseMapId);
//
////		sphere = (Mesh) Controller.getResLoader().getMesh("Planet").getChilds().get(0);
////		sphere.setMaterial(this.getMaterial());
////		TextureNew cloudMap = TextureIO.newTexture(Controller.getResLoader().get, arg1, arg2)TextureManager.loadTexture(getClass().getResource("/org/ankh/unfall/media/textures/clouds.dds"), false);
//	}
//	@Override
//	public void initialize(){
//		infos = new PlanetInformations();
//		infos.setDaytime(360);
//		infos.setEquatorTemperature(100);
//		infos.setPoleTemperature(15);
//		infos.setRadius(10f);
//		infos.setWaterInPercent(0.995f);
//		infos.setHeightFactor(0.001f);
//		infos.setSeed((int)System.currentTimeMillis());
//		infos.setHumidity(1.0f);
//		infos.setCloudHeight(0.0035f);
//
//		SpacePalette palette = null;
//
//		palette = new StandardSpacePalette(infos);
//		infos.setHasCloud(false);
//		infos.setAtmosphereDensity(1.0f);
//
//
////		palette = new MarsPalette(infos);
////		infos.setHasCloud(false);
////		infos.setWaterInPercent(0);
////		infos.setAtmosphereDensity(0.5f);
////		infos.setHeightFactor(0.2f);
//
//
//		generator = new StandartSpaceGenerator(1024, 1024, infos, palette );
//
//
//		textureHeight = generator.getHeight();
//		textureWidth = generator.getWidth();
//		spaceMaterial = new Material();
//		spaceMaterial.setAmbient(new float[]{1,1,1,1});
//		sphere = (Mesh) Controller.getResLoader().getMesh("Planet").getChilds().iterator().next();
//		spaceMaterial.setTexture(baseTex);
//	}
//
//
//
//	/* (non-Javadoc)
//	 * @see org.schema.schine.graphicsengine.forms.SceneNode#draw(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
//	 */
//	@Override
//	public void draw()  {
//		GlUtil.glPushMatrix();
//		sphere.setMaterial(spaceMaterial);
//		Vector3f camPos = Controller.getCamera().getPos();
//		GL11.glTranslatef(camPos.x,camPos.y,camPos.z);
//		GL11.glScalef(1390, 1390, 1390);
//		sphere.draw( );
//		GlUtil.glPopMatrix();
//
//	}
//	private int getBaseMapData() {
//		ByteBuffer baseMapData = MemoryUtil.memAlloc(textureWidth*textureHeight*4);
//		int[] colorMap = generator.getColorMap();
//
//
//		for(int c: colorMap) {
//			int newc = ((c & 0x00FFFFFF) << 8) | 0xFF;
//			baseMapData.putInt(newc);
//		}
//		baseMapData.rewind();
//		int baseMap = Texture.getTextureFromBuffer( textureWidth, textureHeight, baseMapData);
////		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
////		com.jogamp.openGL11.util.texture.Texture transformationArray = TextureIO.newTexture(GL11.GL_TEXTURE_2D);
////		transformationArray.bind();
////		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, textureWidth, textureHeight, 0, GL11.GL_RGBA,
////				GL11.GL_UNSIGNED_BYTE, baseMapData);
////		try {
////			TextureIO.write(transformationArray, new FileExt("./test/test_base.png"));
////		} catch (GLException e) {
////			e.printStackTrace();
////		} catch (IOException e) {
////
////			e.printStackTrace();
////		}
////
//
//		return baseMap;
//	}
//
//	@Override
//	public float getRadius() {
//		return -1;
//	}
//	@Override
//	public void insertIntoDB() {
//
//
//	}
//
//	@Override
//	public void debugWriteToDisk() {
//
//
//	}
//}
