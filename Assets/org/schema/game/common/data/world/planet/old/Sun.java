///*
// 	This file is part of jME Planet Demo.
//
//    jME Planet Demo is free software: you can redistribute it and/or modify
//    it under the terms of the GNU Lesser General Public License as published by
//    the Free Software Foundation.
//
//    jME Planet Demo is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU Lesser General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with jME Planet Demo.  If not, see <http://www.gnu.org/licenses/>.
//*/
//package org.schema.game.common.data.world.planet;
//
//import java.io.ByteArrayInputStream;
//import java.math.BigDecimal;
//import java.nio.ByteBuffer;
//import java.sql.Blob;
//import java.sql.PreparedStatement;
//import java.sql.SQLException;
//import org.lwjgl.opengl.GL11;
//import org.lwjgl.opengl.GL13;
//import org.schema.game.client.data.GameClientState;
//import org.schema.game.common.data.world.galaxy.CelestialBody;
//import org.schema.game.common.data.world.galaxy.StarSystem;
//import org.schema.game.common.data.world.planet.texgen.ContinentalGenerator;
//import org.schema.game.common.data.world.planet.texgen.PlanetGenerator;
//import org.schema.game.common.data.world.planet.texgen.palette.BlueSunPalette;
//import org.schema.game.common.data.world.planet.texgen.palette.TerrainPalette;
//import org.schema.schine.graphicsengine.core.Controller;
//import org.schema.schine.graphicsengine.core.DrawableScene;
//import org.schema.schine.graphicsengine.core.GlUtil;
//import org.schema.schine.graphicsengine.forms.Mesh;
//import org.schema.schine.graphicsengine.shader.ErrorDialogException;
//import org.schema.schine.graphicsengine.shader.Shader;
//import org.schema.schine.graphicsengine.shader.ShaderLibrary;
//import org.schema.schine.graphicsengine.shader.Shaderable;
//import org.schema.schine.graphicsengine.texture.Material;
//import org.schema.schine.graphicsengine.texture.Texture;
//
//
////
/////**
//// * Telluric planet object
//// * @author Yacine Petitprez (anykeyh)
//// */
//public class Sun extends CelestialBody{
//
//	private PlanetGenerator generator;
//	private PlanetInformations infos;
//	private int textureHeight;
//	private int textureWidth;
//	private int baseMapId;
//	private int specularMapId;
//	private int normMapId;
//	private Mesh sphere;
//	private SunShaderable sunShaderable;
//	private int rotation;
//	private Material sunMaterial;
//	private int[] tempColor;
//	private int[] tempSpec;
//	private int[] tempNormal;
//	private int[] heightMap;
//	public Sun(GameClientState state, PlanetInformations infos, StarSystem system){
//		super(state, TYPE_SUN, system);
//		this.infos = infos;
//		initialize();
//	}
//@Override
//	public void initialize(){
//
//
//
//
////		palette = new MarsPalette(infos);
////		infos.setHasCloud(false);
////		infos.setWaterInPercent(0);
////		infos.setAtmosphereDensity(0.5f);
////		infos.setHeightFactor(0.2f);
//
//
//
//		sunShaderable = new SunShaderable();
//		sunMaterial = new Material();
//	}
//
//
//	@Override
//	public void generate(){
//		TerrainPalette palette = null;
//		palette = new BlueSunPalette(infos);
//		PlanetGenerator generator = new ContinentalGenerator(1024, 1024, infos, palette );
//
//		this.generator = generator;
//		textureHeight = generator.getHeight();
//		textureWidth = generator.getWidth();
//		baseMapId = getBaseMapData();
//		specularMapId = getSpecularMapData();
//		normMapId = getNormalMapData();
//		this.setMaterial(new Material());
//		System.err.println("baseMap of sun "+baseMapId);
//		sphere = (Mesh) Controller.getResLoader().getMesh("Planet").getChilds().iterator().next();
//
//
//		heightMap = generator.getHeightMap();
//		generator = null;
//
////		TextureNew cloudMap = TextureIO.newTexture(Controller.getResLoader().get, arg1, arg2)TextureManager.loadTexture(getClass().getResource("/org/ankh/unfall/media/textures/clouds.dds"), false);
//	}
//
////	public static CelestialBody loadFromResult(EmbedResultSet40 result, GameClientState state, StarSystem system) throws SQLException{
////		//TODO add planetinformation in db
////		PlanetInformations infos = new PlanetInformations();
////		Sun p = new Sun(state, infos, system);
////		p.setPos(result.getBigDecimal("X").floatValue(),
////				result.getBigDecimal("Y").floatValue(),
////				result.getBigDecimal("Z").floatValue());
////		p.name = result.getString("ENTITY_NAME");
////		p.textureWidth = result.getInt("MAP_WIDTH");
////		p.textureHeight = result.getInt("MAP_HEIGHT");
////		p.infos.setRadius(result.getFloat("RADIUS"));
////
////
////
////		Blob blob = result.getBlob("DIFFUSE");
////		byte[] diffuseBytes = blob.getBytes(1L, (int)blob.length());
////		p.tempColor = generateIntArray(diffuseBytes);
////
////		blob = result.getBlob("NORMAL");
////		byte[] normalBytes = blob.getBytes(1L, (int)blob.length());
////		p.tempNormal = generateIntArray(normalBytes);
////
////		blob = result.getBlob("SPECULAR");
////		byte[] specularBytes = blob.getBytes(1L, (int)blob.length());
////		p.tempSpec = generateIntArray(specularBytes);
////
////
////		blob = result.getBlob("HEIGHTMAP");
////		byte[] heightBytes = blob.getBytes(1L, (int)blob.length());
////		p.heightMap = generateIntArray(heightBytes);
////		p.setConsistent(true);
////		System.err.println("loaded sun "+p.name);
////		return p;
////	}
//	@Override
//	public void onInit(){
//		if(generator != null){
//		}else{
//			baseMapId = getMapData( tempColor, textureWidth, textureHeight);
//			specularMapId = getMapData( tempSpec, textureWidth, textureHeight);
//			normMapId = getMapData( tempNormal, textureWidth, textureHeight);
//		}
//		this.setMaterial(new Material());
//		sphere = (Mesh) Controller.getResLoader().getMesh("Planet").getChilds().iterator().next();
//		sphere.setMaterial(this.getMaterial());
//	}
//	@Override
//	public void insertIntoDB(){
////		String query = "INSERT INTO GALAXY_CELESTIAL_"+getStarSystem().getGalaxy().getName()+" ( "+
////				"SYSTEM_ID,"+
////				"X, "+
////				"Y, "+
////				"Z, "+
////				"RADIUS, "+
////				"MAP_WIDTH, "+
////				"MAP_HEIGHT, "+
////				"HEIGHTMAP, "+
////				"DIFFUSE, "+
////				"NORMAL, "+
////				"SPECULAR, "+
////				"ENTITY_NAME, "+
////				"ENTITY_TYPE "+
////				") VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
////		try {
////			PreparedStatement pstd = getState().getSqlConnection().getConnection().prepareStatement(query);
////			pstd.setInt(1, getStarSystem().getId());
////			pstd.setBigDecimal(2, BigDecimal.valueOf(getPos().x));
////			pstd.setBigDecimal(3, BigDecimal.valueOf(getPos().y));
////			pstd.setBigDecimal(4, BigDecimal.valueOf(getPos().z));
////			pstd.setFloat(5, getRadius());
////			pstd.setInt(6, textureWidth);
////			pstd.setInt(7, textureHeight);
////			pstd.setBinaryStream(8,  new ByteArrayInputStream(getFromIntArray(generator.getHeightMap())));
////			pstd.setBinaryStream(9,  new ByteArrayInputStream(getFromIntArray(generator.getColorMap())));
////			pstd.setBinaryStream(10,  new ByteArrayInputStream(getFromIntArray(generator.getNormalMap())));
////			pstd.setBinaryStream(11,  new ByteArrayInputStream(getFromIntArray(generator.getSpecularMap())));
////			pstd.setString(12, getName());
////			pstd.setInt(13, getType());
////
////			pstd.executeUpdate();
////			pstd.close();
////			getState().getSqlConnection().getConnection().commit();
////			this.setConsistent(true);
////			System.err.println("WROTE SUN IN DATABASE! For system: "+getStarSystem().getId());
////		} catch (SQLException e) {
////
////			e.printStackTrace();
////		}
//	}
//	@Override
//	public void draw() {
//		GlUtil.glPushMatrix();
//		sphere.setMaterial(sunMaterial);
//		ShaderLibrary.sunShader.setShaderInterface(sunShaderable);
//		ShaderLibrary.sunShader.load( );
//		GL11.glTranslatef(getPos().x, getPos().y, getPos().z);
//		GL11.glRotatef((rotation++)/100f, 0, 1, 0);
//		GL11.glScalef(getRadius()/1000, getRadius()/1000, getRadius()/1000);
//		sphere.draw( );
//		ShaderLibrary.sunShader.unload( );
//
//
//		GlUtil.glPopMatrix();
//	}
//	private int getNormalMapData() {
//		ByteBuffer baseMapData = MemoryUtil.memAlloc(textureWidth*textureHeight*4);
//		int[] colorMap = generator.getNormalMap();
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
////			TextureIO.write(transformationArray, new FileExt("./test/test_normal.png"));
////		} catch (GLException e) {
////			e.printStackTrace();
////		} catch (IOException e) {
////
////			e.printStackTrace();
////		}
//
//		return baseMap;
//	}
//
//	private int getSpecularMapData() {
//		ByteBuffer baseMapData = MemoryUtil.memAlloc(textureWidth*textureHeight*4);
//		int[] colorMap = generator.getSpecularMap();
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
////			TextureIO.write(transformationArray, new FileExt("./test/test_spec.png"));
////		} catch (GLException e) {
////			e.printStackTrace();
////		} catch (IOException e) {
////
////			e.printStackTrace();
////		}
//
//		return baseMap;
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
//
//
//		return baseMap;
//	}
//
//
//	/**
//	 * @param infos the infos to set
//	 */
//	public void setInfos(PlanetInformations infos) {
//		this.infos = infos;
//	}
//	/**
//	 * @return the infos
//	 */
//	public PlanetInformations getInfos() {
//		return infos;
//	}
//
//
//	private class SunShaderable implements Shaderable{
//
//
//		@Override
//		public void updateShaderParameters( Shader shader) {
//
//
//			//GlUtil.updateShaderVector3f( shader, "fvViewPosition", Controller.camera.getPosition().getVector3f());
//			//GlUtil.updateShaderVector3f( shader, "fvLightPosition", Controller.camera.getPosition().getVector3f());
//
//
//
//			GlUtil.updateShaderVector4f( shader, "fvSpecular", 1,1,1,1);
//			GlUtil.updateShaderVector4f( shader, "fvDiffuse", 1,1,1,1);
//
//			GlUtil.updateShaderFloat( shader, "fSpecularPower", 30.0f);
//
//			GlUtil.glEnable(GL11.GL_TEXTURE_2D);
//
//
//			GlUtil.updateShaderTexture2D(shader, "baseMap", baseMapId, 0);
//			GlUtil.updateShaderTexture2D(shader, "normalMap", normMapId, 1);
//			GlUtil.updateShaderTexture2D(shader, "specMap", specularMapId, 2);
//
//
////			Controller.getResLoader().getSprite("clouds").getMaterial().getTexture().bindOnShader(  GL13.GL_TEXTURE0, 0, "cloudsMap", shaderprogram);
////			baseTex.bindOnShader(  GL13.GL_TEXTURE1, 1, "baseMap", shaderprogram);
////			normTex.bindOnShader(  GL13.GL_TEXTURE2, 2, "normalMap", shaderprogram);
////			specTex.bindOnShader(  GL13.GL_TEXTURE3, 3, "specMap", shaderprogram);
//
//
////			if(infos.hasCloud()) {
////
////			} else {
////				//pointer to noTextures
////				Controller.getResLoader().getSprite("clouds").getMaterial().getTexture().bindOnShader(  GL11.GL_TEXTURE4, 4, "cloudsMap", shaderprogram);
////			}
//
//
//
//
//		}
//		@Override
//		public void onExit() {
//
//			GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
//			GlUtil.glDisable(GL11.GL_TEXTURE_2D);
//			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
//
//			GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
//			GlUtil.glDisable(GL11.GL_TEXTURE_2D);
//			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
//
//			GlUtil.glActiveTexture(GL13.GL_TEXTURE2);
//			GlUtil.glDisable(GL11.GL_TEXTURE_2D);
//			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
//
//
//			GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
//
//		}
//
//		@Override
//		public void updateShader(DrawableScene scene) {
//
//
//		}
//	}
//
//
//	@Override
//	public float getRadius() {
//		return infos.getRadius();
//	}
//	@Override
//	public void debugWriteToDisk() {
//
//
//	}
//
//}
