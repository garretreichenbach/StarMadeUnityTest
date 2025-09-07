/*
 	This file is part of jME Planet Demo.

    jME Planet Demo is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation.

    jME Planet Demo is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with jME Planet Demo.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.schema.game.common.data.world.planet.old;

//
///**
// * Telluric planet object
// * @author Yacine Petitprez (anykeyh)
// */
@Deprecated
public class Planet {

	//	private PlanetGenerator generator;
	//	private PlanetInformations infos;
	//	private int textureHeight;
	//	private int textureWidth;
	//	private int baseMapId;
	//	private int specularMapId;
	//	private int normMapId;
	//	private Mesh sphere;
	//	private Material planetMaterial;
	//	private AtmoShaderable atmoShaderable;
	//	private PlanetShaderable planetShaderable;
	//	private int cloudsMapId;
	//	private int rotation;
	//	private EditableSendableSegmentController surface;
	//	private int[] tempColor;
	//	private int[] tempSpec;
	//	private int[] tempNormal;
	//	private int[] heightMap;
	//	private Terrain terrain;
	//	private HoffmannSky hoffmannSky;
	//	private boolean surfaceMode;
	//	private TerrainShaderable terrainShaderable;
	//	private Ocean ocean;

	//	public Planet(GameClientState state, PlanetInformations infos){
	//		super(state);
	//		this.infos = infos;
	//		initialize();
	//	}
	//
	//	public void insertIntoDB(){
	////		String query = "INSERT INTO GALAXY_CELESTIAL_"+getStarSystem().getGalaxy().getName()+" ( "+
	////		"SYSTEM_ID,"+
	////		"X, "+
	////		"Y, "+
	////		"Z, "+
	////		"RADIUS, "+
	////		"MAP_WIDTH, "+
	////		"MAP_HEIGHT, "+
	////		"HEIGHTMAP, "+
	////		"DIFFUSE, "+
	////		"NORMAL, "+
	////		"SPECULAR, "+
	////		"ENTITY_NAME, "+
	////		"ENTITY_TYPE "+
	////		") VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
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
	////
	////			System.err.println("WROTE PLANET IN DATABASE! for system "+getStarSystem().getId());
	////		} catch (SQLException e) {
	////
	////			e.printStackTrace();
	////		}
	//	}
	////	public static CelestialBody loadFromResult(EmbedResultSet40 result, GameClientState state, StarSystem system) throws SQLException{
	////		//TODO add planetinformation in db
	////
	////		PlanetInformations infos = new PlanetInformations();
	////		Planet p = new Planet(state, infos, system);
	////		p.setPos(result.getBigDecimal("X").floatValue(),
	////				result.getBigDecimal("Y").floatValue(),
	////				result.getBigDecimal("Z").floatValue());
	////		p.name = result.getString("ENTITY_NAME");
	////		p.textureWidth = result.getInt("MAP_WIDTH");
	////		p.textureHeight = result.getInt("MAP_HEIGHT");
	////
	////		p.infos.setRadius(result.getFloat("RADIUS"));
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
	////		p.setHeightMap(generateIntArray(heightBytes));
	////
	////		p.setConsistent(true);
	////		System.err.println("loaded planet "+p.name+" on pos "+p.getPos());
	////		return p;
	////	}
	//
	//
	//	@Override
	//	public void initialize(){
	//
	//
	////		palette = new MarsPalette(infos);
	////		infos.setHasCloud(false);
	////		infos.setWaterInPercent(0);
	////		infos.setAtmosphereDensity(0.5f);
	////		infos.setHeightFactor(0.2f);
	//
	//
	//		atmoShaderable = new AtmoShaderable();
	//		planetShaderable = new PlanetShaderable();
	//
	//	}
	//	public static int getHeightTerrainMapData(int[] data, int textureWidth, int textureHeight) {
	//		ByteBuffer baseMapData = MemoryUtil.memAlloc(textureWidth*textureHeight*4);
	//
	//		for(int c: data) {
	//			baseMapData.putInt(c);
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
	//	public void addSurface(){
	//
	//		try {
	//			System.err.println("adding surface cubeMeshes");
	//			//TODO: adjust planet atmosphere for hoffmann sky
	//			hoffmannSky = new HoffmannSky();
	//			System.err.println("generating heightMapTexture");
	//			int heightMapTex = getHeightTerrainMapData( heightMap, textureWidth, textureHeight);
	//			System.err.println("generating surface");
	//			Vector3f stepSize = new Vector3f(100,100,100);
	//			terrain = new Terrain(getState(), heightMapTex, getNormMapId(), getSpecularMapId(), heightMap, textureWidth+1, 512, stepSize);
	//			terrain.onInit( );
	//			terrain.setPos(new Vector3f(getPos()));
	//
	//			ocean = new Ocean();
	//			Vector3f oceanPos = new Vector3f(getPos().x, getPos().y + 30 * stepSize.y, getPos().z) ;
	//			ocean.setPos(oceanPos); //
	//			ocean.onInit( );
	//		} catch (Exception e) {
	//			e.printStackTrace();
	//		}
	//
	////		setSurface(new PlanetSurface((PlanetSurface.VISIBILITY+1)*2, 1,(PlanetSurface.VISIBILITY+1)*2, 32, 32, 32, getState(), this));
	////		WorldCreatorFactory factory = new WorldCreatorSimpleFactory(10, 1, 10);
	////		getSurface().initialize();
	//	}
	//
	//	@Override
	//	public void generate(){
	//		TerrainPalette palette = null;
	//		palette = new EarthPalette(infos);
	//		PlanetGenerator generator = new ContinentalGenerator(1024, 1024, infos, palette );
	//		this.generator = generator;
	//		textureHeight = generator.getHeight();
	//		textureWidth = generator.getWidth();
	//		setBaseMapId(getMapData( generator.getColorMap(), textureWidth, textureHeight));
	//		setSpecularMapId(getMapData( generator.getNormalMap(), textureWidth, textureHeight));
	//		setNormMapId(getMapData( generator.getSpecularMap(), textureWidth, textureHeight));
	////		for(int i = 0; i < 16*16; i++){
	////			System.err.println("HEIGHTMAP: "+generator.getHeightMap()[i]);
	////		}
	//
	//		setHeightMap(generator.getHeightMap());
	//		generator = null;
	//
	//	}
	//	@Override
	//	public void onInit(){
	//		if(generator != null){
	//		}else{
	//			setBaseMapId(getMapData( tempColor, textureWidth, textureHeight));
	//			setSpecularMapId(getMapData( tempSpec, textureWidth, textureHeight));
	//			setNormMapId(getMapData( tempNormal, textureWidth, textureHeight));
	//			tempColor = null;
	//			tempSpec = null;
	//			tempNormal = null;
	//		}
	//		cloudsMapId = Controller.getResLoader().getSprite("clouds").getMaterial().getTexture().getTextureId();
	//		this.setMaterial(new Material());
	//		sphere = (Mesh) Controller.getResLoader().getMesh("Planet").getChilds().iterator().next();
	//		sphere.setMaterial(this.getMaterial());
	//	}
	//
	//	/* (non-Javadoc)
	//	 * @see org.schema.schine.graphicsengine.forms.AbstractSceneNode#drawInFbo(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	//	 */
	//	public void drawInFbo()  {
	//		GlUtil.glPushMatrix();
	////		ShaderLibrary.silhouetteShader.load( );
	//		transform( );
	//		sphere.draw( );
	////		ShaderLibrary.silhouetteShader.unload( );
	//		GlUtil.glPopMatrix();
	//	}
	//	@Override
	//	public void transform(){
	//		GL11.glTranslatef(getPos().x, getPos().y, getPos().z);
	//		GL11.glRotatef((rotation++)/100f, 0, 1, 0);
	//		GL11.glScalef(getRadius()/1000, getRadius()/1000, getRadius()/1000);
	//	}
	//
	//	public void drawSurface() {
	//		if(terrain != null && !terrain.isLoaded()){
	//			terrain.load( );
	//		}
	//		if(ocean != null && !ocean.isLoaded()){
	//			terrain.load( );
	//		}
	//		terrain.draw( );
	//		ocean.draw( );
	//
	////		ShaderLibrary.hoffmanTerrainShader.unload( );
	//
	//	}
	//	@Override
	//	public void draw() {
	//
	//		Vector3f viewerPos = new Vector3f(Controller.getCamera().getViewable().getPos());
	//		Vector3f camPos = new Vector3f(Controller.getCamera().getPos());
	//
	//		viewerPos.sub(this.getPos());
	//		if(!surfaceMode && viewerPos.length() < 1.1f*getRadius()){
	//			if(terrain == null){
	//				addSurface( );
	//			}
	//			surfaceMode = true;
	//			Vector3f pos = new Vector3f(getPos());
	//			pos.add(new Vector3f(0,50000,0));
	//			Controller.getCamera().getViewable().getPos().set(pos);
	//			getState().getScene().setFarPlane(50000);
	//			return;
	//		}
	//		int heightDiff = (int) (viewerPos.y-getPos().y);
	//		if(surfaceMode && heightDiff > 50100){
	//			surfaceMode = false;
	//			System.err.println("set cam position "+heightDiff);
	//			Controller.getCamera().getViewable().getPos().set(new Vector3f(getPos().x, getPos().y+1.11f*getRadius(), getPos().z));
	//			getState().getScene().setFarPlane(50000);
	//		}
	//
	//		if(surfaceMode){
	//				ShaderLibrary.hoffmanSkyShader.setShaderInterface(hoffmannSky);
	//				ShaderLibrary.hoffmanSkyShader.load( );
	//				GlUtil.glPushMatrix();
	//				GL11.glTranslatef(camPos.x, camPos.y, camPos.z);
	//				Controller.getResLoader().getMesh("Sky").getChilds().iterator().next().draw( );
	//				GlUtil.glPopMatrix();
	//				ShaderLibrary.hoffmanSkyShader.unload( );
	//
	//				drawSurface( );
	//
	//		}else{
	//			drawPlanet( );
	//		}
	//
	//	}
	//	public void drawPlanet() {
	//		if(getState().getScene().getFbo() != null &&  getState().getScene().getFbo().isEnabled()){
	//			drawInFbo( );
	//			return;
	//		}
	//		if(isVisibleInFrustum()){
	//			return;
	//		}
	//		GlUtil.glPushMatrix();
	//
	//		ShaderLibrary.planetShader.setShaderInterface(planetShaderable);
	//		ShaderLibrary.planetShader.load( );
	//		transform( );
	//		sphere.draw( );
	//		ShaderLibrary.planetShader.unload( );
	//
	//		ShaderLibrary.atmosphereShader.setShaderInterface(atmoShaderable);
	//		ShaderLibrary.atmosphereShader.load( );
	////		GlUtil.glDisable( GL11.GL_DEPTH_TEST );
	////		GlUtil.glDepthMask( false );
	//		GlUtil.glEnable(GL11.GL_BLEND);
	//		GlUtil.glBlendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	//
	//		GlUtil.glEnable (GL11.GL_CULL_FACE);
	//		GL11.glCullFace(GL11.GL_BACK);
	////		GL11.glScalef(1.1f, 1.1f, 1.1f);
	////		GL11.glTranslatef(300.0f, 0f, 0f);
	//
	//		sphere.draw( );
	//		GL11.glCullFace(GL11.GL_FRONT);
	//		sphere.draw( );
	//		GL11.glCullFace(GL11.GL_BACK);
	//		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
	//		GlUtil.glDisable(GL11.GL_BLEND);
	//		GlUtil.glDepthMask( true );
	//		ShaderLibrary.atmosphereShader.unload( );
	//		GlUtil.glPopMatrix();
	//	}
	//
	//
	//
	//
	//	private class PlanetShaderable implements Shaderable{
	//
	//
	//		private float counter;
	//
	//		@Override
	//		public void updateShaderParameters( Shader shader) {
	//
	//
	//			//GlUtil.updateShaderVector3f( shader, "fvViewPosition", Controller.camera.getPosition().getVector3f());
	//			//GlUtil.updateShaderVector3f( shader, "fvLightPosition", AbstractScene.mainLight.getPos());
	//
	//
	//			GlUtil.updateShaderFloat( shader, "fCloudRotation", counter * 0.000005f);
	//			counter++;
	//
	//
	//
	//
	//
	//
	//			GlUtil.updateShaderVector4f( shader, "fvSpecular", 1,1,1,1);
	//			GlUtil.updateShaderVector4f( shader, "fvDiffuse", 1,1,1,1);
	//
	//			GlUtil.updateShaderFloat( shader, "fSpecularPower", 20.0f);
	//			GlUtil.updateShaderFloat( shader, "fCloudHeight", infos.getCloudHeight());
	//
	//			GlUtil.glEnable(GL11.GL_TEXTURE_2D);
	//
	//			GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
	////			System.err.println("base "+baseMapId);
	//			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, getBaseMapId());
	//
	//			GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
	////			System.err.println("norm "+normMapId);
	//			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, getNormMapId());
	//
	//			GlUtil.glActiveTexture(GL13.GL_TEXTURE2);
	////			System.err.println("spec "+specularMapId);
	//			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, getSpecularMapId());
	//
	//			GlUtil.glActiveTexture(GL13.GL_TEXTURE3);
	////			System.err.println("clou "+cloudsMapId);
	//			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, cloudsMapId);
	//
	////			GlUtil.glActiveTexture(GL11.GL_TEXTURE4);
	////			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, baseMapId);
	//			GlUtil.glActiveTexture(GL13.GL_TEXTURE4);
	////			GlUtil.glDisable(GL11.GL_TEXTURE_2D);
	//
	//			GlUtil.updateShaderTexture2D(shader, "baseMap", getBaseMapId(), 0);
	//			GlUtil.updateShaderTexture2D(shader, "normalMap", getNormMapId(), 1);
	//			GlUtil.updateShaderTexture2D(shader, "specMap", getSpecularMapId(), 2);
	//			GlUtil.updateShaderTexture2D(shader, "cloudsMap", cloudsMapId, 3);
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
	////			baseTex.unbindFromIndex( );
	////			normTex.unbindFromIndex( );
	////			specTex.unbindFromIndex( );
	////
	////			if(infos.hasCloud()) {
	////				Controller.getResLoader().getSprite("clouds").getMaterial().getTexture().unbindFromIndex( );
	////			} else {
	////				//pointer to noTextures
	////				Controller.getResLoader().getSprite("clouds").getMaterial().getTexture().unbindFromIndex( );
	////			}
	//
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
	//			GlUtil.glActiveTexture(GL13.GL_TEXTURE3);
	//			GlUtil.glDisable(GL11.GL_TEXTURE_2D);
	//			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	//
	//			GlUtil.glActiveTexture(GL13.GL_TEXTURE4);
	//			GlUtil.glDisable(GL11.GL_TEXTURE_2D);
	//			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
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
	//
	//	private class AtmoShaderable implements Shaderable{
	//		@Override
	//		public void onExit() {
	//
	//		}
	//
	//		@Override
	//		public void updateShader(DrawableScene scene) {
	//
	//
	//		}
	//
	//		@Override
	//		public void updateShaderParameters( Shader shader) {
	//
	//			//GlUtil.updateShaderVector3f( shader, "fvLightPosition", AbstractScene.mainLight.getPos());
	////			GlUtil.updateShaderVector3f( shader, "fvViewPosition", Controller.camera.getPosition().getVector3f());
	//
	//			GlUtil.updateShaderVector4f( shader, "fvDiffuse", 1,1,1,1f);
	//			//The light which comes on the atmosphere
	//			GlUtil.updateShaderColor4f( shader, "fvAtmoColor", infos.getAtmosphereColor());
	//
	//			GlUtil.updateShaderFloat( shader, "fCloudHeight", infos.getCloudHeight());
	//			GlUtil.updateShaderFloat( shader, "fAbsPower", infos.getAtmosphereAbsorptionPower());
	//			GlUtil.updateShaderFloat( shader, "fAtmoDensity", infos.getAtmosphereDensity());
	//			GlUtil.updateShaderFloat( shader, "fGlowPower", infos.getAtmosphereGlowPower());
	//
	//		}
	//	}
	//
	//
	//
	//	@Override
	//	public float getRadius() {
	//		return infos.getRadius();
	//	}
	//
	//	/**
	//	 * @param surface the surface to set
	//	 */
	//	public void setSurface(EditableSendableSegmentController surface) {
	//		this.surface = surface;
	//	}
	//
	//	/**
	//	 * @return the surface
	//	 */
	//	public EditableSendableSegmentController getSurface() {
	//		return surface;
	//	}
	//
	//	/**
	//	 * @param heightMap the heightMap to set
	//	 */
	//	public void setHeightMap(int[] heightMap) {
	//		this.heightMap = heightMap;
	//	}
	//
	//	/**
	//	 * @return the heightMap
	//	 */
	//	public int[] getHeightMap() {
	//		return heightMap;
	//	}
	//
	////	/**
	////	 * @param terrain the terrain to set
	////	 */
	////	public void setSurfaceMesh(Terrain terrain) {
	////		this.surfaceMesh = terrain;
	////	}
	////
	////	/**
	////	 * @return the terrain
	////	 */
	////	public Terrain getSurfaceMesh() {
	////		return terrain;
	////	}
	//
	//	@Override
	//	public void debugWriteToDisk() {
	//
	//
	//	}
	//
	//	/**
	//	 * @param baseMapId the baseMapId to set
	//	 */
	//	public void setBaseMapId(int baseMapId) {
	//		this.baseMapId = baseMapId;
	//	}
	//
	//	/**
	//	 * @return the baseMapId
	//	 */
	//	public int getBaseMapId() {
	//		return baseMapId;
	//	}
	//
	//	/**
	//	 * @param specularMapId the specularMapId to set
	//	 */
	//	public void setSpecularMapId(int specularMapId) {
	//		this.specularMapId = specularMapId;
	//	}
	//
	//	/**
	//	 * @return the specularMapId
	//	 */
	//	public int getSpecularMapId() {
	//		return specularMapId;
	//	}
	//
	//	/**
	//	 * @param normMapId the normMapId to set
	//	 */
	//	public void setNormMapId(int normMapId) {
	//		this.normMapId = normMapId;
	//	}
	//
	//	/**
	//	 * @return the normMapId
	//	 */
	//	public int getNormMapId() {
	//		return normMapId;
	//	}
	//
	//	public static void main(String args[]) {
	//
	//		java.awt.EventQueue.invokeLater(new Runnable() {
	//			@Override
	//			public void run() {
	//				PlanetInformations infos = new PlanetInformations();
	//				infos.setDaytime(360);
	//				infos.setEquatorTemperature(45);
	//				infos.setPoleTemperature(20);
	//				infos.setRadius(10);
	//				infos.setWaterInPercent(0.6f);
	//				infos.setHeightFactor(0.2f);
	//				infos.setSeed((int) System.currentTimeMillis());
	//				infos.setHumidity(1.0f);
	//				infos.setCloudHeight(0.0035f);
	//				infos.setHasCloud(true);
	//				infos.setAtmosphereDensity(1.0f);
	//				PlanetGenerator generator = new ContinentalGenerator(1024,
	//						1024, infos, new EarthPalette(infos));
	//				BufferedImage i = new BufferedImage(1024, 1024,
	//						BufferedImage.TYPE_INT_ARGB);
	//				Graphics2D g = ((Graphics2D) i.getGraphics());
	//				int[] colorMap = generator.getColorMap();
	//				for (int y = 0; y < 1024; y++) {
	//					for (int x = 0; x < 1024; x++) {
	//						g.setColor(new Color(colorMap[y * 1024 + x]));
	//						g.drawRect(x, y, 1, 1);
	//					}
	//				}
	//				g.dispose();
	//				JFrame f = new JFrame();
	//				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	//				f.setSize(1224, 792);
	//
	//				JLabel l = new JLabel();
	//				l.setIcon(new ImageIcon(i));
	//				l.setSize(1024, 1024);
	//
	//				JScrollPane p = new JScrollPane(l,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
	//				p.setSize(792, 792);
	//				p.setAutoscrolls(true);
	//
	//
	//
	//				f.setContentPane(p);
	//				f.setVisible(true);
	//
	//			}
	//		});
	//
	//	}

}
