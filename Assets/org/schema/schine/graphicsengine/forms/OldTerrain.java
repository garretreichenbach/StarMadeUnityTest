/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>OldTerrain</H2>
 * <H3>org.schema.schine.graphicsengine.forms</H3>
 * OldTerrain.java
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
package org.schema.schine.graphicsengine.forms;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL21;
import org.schema.common.util.data.DataUtil;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.schine.graphicsengine.core.AbstractScene;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GLU;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.ResourceException;
import org.schema.schine.graphicsengine.shader.HoffmannSky;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.texture.Texture;
import org.schema.schine.graphicsengine.texture.TextureLoader;
import org.schema.schine.resource.ResourceLoader;

/**
 * The Class OldTerrain.
 */
public class OldTerrain extends Mesh {

	public static final int FIELD_SIZE = 100;
	/**
	 * The Constant MAX_TEXTURE_SIZE.
	 */
	public static final int MAX_TEXTURE_SIZE = 512;
	/**
	 * The Constant terrainSubZero.
	 */
	public final static float terrainSubZero = 240;
	/**
	 * The Constant pixelOccupiedThreshold.
	 */
	public static final float pixelOccupiedThreshold = 20;
	/**
	 * The Constant TERRAIN_CLIFF_SIZE.
	 */
	public static final float TERRAIN_CLIFF_SIZE = 50;
	/**
	 * The Constant MAX_TERRAIN_DIMENSION.
	 */
	public static final int MAX_TERRAIN_DIMENSION = 128;
	/**
	 * The Constant TERRAIN_REGION_DIRT.
	 */
	private static final int TERRAIN_REGION_DIRT = 0;
	/**
	 * The Constant TERRAIN_REGION_GRASS.
	 */
	private static final int TERRAIN_REGION_GRASS = 1;
	/**
	 * The Constant TERRAIN_REGION_ROCK.
	 */
	private static final int TERRAIN_REGION_ROCK = 2;
	/**
	 * The Constant TERRAIN_REGION_SNOW.
	 */
	private static final int TERRAIN_REGION_SNOW = 3;
	/**
	 * The Constant rimY.
	 */
	private static final int rimY = 0;
	/**
	 * The Constant rimX.
	 */
	private static final int rimX = 0;
	public static int drawnTerrains;
	/**
	 * The width scale.
	 */
	public static float widthScale = 1.0f;
	/**
	 * The stepsize.
	 */
	public static int stepsize = MAX_TEXTURE_SIZE / 2;// stepsize
	/**
	 * The pixel occupied distance.
	 */
	public static int pixelOccupiedDistance = 5;
	/**
	 * The scalefactor.
	 */
	static int scalefactor = 1;// scale of normalized color values
	/**
	 * The regions.
	 */
	private static TerrainRegion[] regions;
	/**********************************************************************/
	// it looks like shit... but these values are static because they are needed temporarily when using at(x,y)
	private static int m_width = 1024;
	private static int m_height = 1024;
	private static int m_halfwidth;
	private static int m_widthm1;
	private static int m_heightm1;
	protected int heightMapPointer;
	private float heightFields[][];
	/**
	 * The scale.
	 */
	private float scale = scalefactor;
	/**
	 * The tiling_factor.
	 */
	private float tiling_factor = 3;
	/**
	 * The height map.
	 */
	private BufferedImage heightMap;
	/**
	 * The optimizer.
	 */
	private TerrainMeshOptimizer optimizer;
	/**
	 * The orig verts.
	 */
	private Vector3f[] origVerts;
	/**
	 * The normal map pointer.
	 */
	private int normalMapPointer;
	/**
	 * The specular map pointer.
	 */
	private int specularMapPointer;
	private TerrainShaderable terrainShaderable;
	private boolean asVertexBufferObject;

	/**
	 * Instantiates a new heightField.
	 */
	public OldTerrain() {
		super();
		this.terrainShaderable = new TerrainShaderable();
		//		multipassShader = new MultipassShader();

	}

	/**
	 * @param x
	 * @param y
	 * @return
	 */
	protected static int at(int x, int y) {
		while (x < 0) {
			x += m_width;
		}

		y = y & ((m_height << 1) - 1);

		if (y > m_heightm1) {
			y = (m_heightm1 << 1) - y;
			x += m_halfwidth;
		}

		if (y < 0) {
			y = -y;
			x += m_width >> 1;
		}

		x = x & m_widthm1;

		return (y * m_width) + x;

	}

	/**
	 * Creates the heightField.
	 *
	 * @param width     the width
	 * @param height    the height
	 * @param heights   the heights
	 * @param fieldSize the field size
	 * @param offy
	 * @param offx
	 * @param maxHeight
	 * @param maxWidth
	 * @return the heightField
	 */
	public static OldTerrain createTerrain(int width, int height, float[][] heights, int fieldSize, int offx, int offy, int maxWidth, int maxHeight) {

		OldTerrain m = new OldTerrain();
		m.scale = scalefactor;
		System.out.println("[OldTerrain] creating from clientState " + width + ", " + height);
		initializeMesh(m, width, height);
		m.heightFields = heights;

		int i = 0;
		float maxX = Integer.MIN_VALUE;
		float minX = Integer.MAX_VALUE;
		float maxY = Integer.MIN_VALUE;
		float minY = Integer.MAX_VALUE;
		float maxZ = Integer.MIN_VALUE;
		float minZ = Integer.MAX_VALUE;
		//		System.err.println("Terraindata: "+w+" x "+h+". size: "+w*h);
		float texFakX = 1f / (width);
		float texFakY = 1f / (height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float color_y = 0;
				try {
					color_y = heights[y][x];
				} catch (IndexOutOfBoundsException e) {

				}
				color_y -= terrainSubZero;
				m.vertices[i].x = (x * fieldSize * widthScale);
				m.vertices[i].y = (color_y * fieldSize);
				m.vertices[i].z = (y * fieldSize * widthScale);

				maxX = (m.vertices[i].x > maxX) ? m.vertices[i].x
						: maxX;
				minX = (m.vertices[i].x < minX) ? m.vertices[i].x
						: minX;
				maxY = (m.vertices[i].y > maxY) ? m.vertices[i].y
						: maxY;
				minY = (m.vertices[i].y < minY) ? m.vertices[i].y
						: minY;
				maxZ = (m.vertices[i].z > maxZ) ? m.vertices[i].z
						: maxZ;
				minZ = (m.vertices[i].z < minZ) ? m.vertices[i].z
						: minZ;

				//				m.texCoords[i].x = (float) x * texFakX;
				//				m.texCoords[i].y = (float) y * texFakY;

				i++;
			}
		}
		m.setBoundingBox(new BoundingBox(new Vector3f(minX, minY, minZ), new Vector3f(maxX, maxY, maxZ)));
		m.origVerts = Vector3fTools.clone(m.vertices);
		//		triangulate(m, width, height);
		m.optimizer = new TerrainMeshOptimizer(m, width, height);
		m.optimizer.optimize(offx, offy, maxWidth, maxHeight);
		m.setIndicedNormals(true);

		m.origVerts = null;
		m.heightFields = null;
		//		triangulate(m, w+(2*rimX), h+(2*rimY));
		makeNormals(m);
		m.optimizer = null;

		m.getMaterial().setAmbient(
				new float[]{0.6f, 0.6f, 0.6f, 1f});
		m.getMaterial().setDiffuse(
				new float[]{0.8f, 0.8f, 0.8f, 1f});
		m.getMaterial().setSpecular(
				new float[]{0.9f, 0.9f, 0.9f, 1f});
		return m;
	}

	/**
	 * Creates the heightField world.
	 *
	 * @param heightField          the height field
	 * @param gl                   the gl
	 * @param glu                  the glu
	 * @param asVertexBufferObject the as vertex bufferList object
	 * @return the world
	 * @throws Exception the exception
	 */
	public static MeshGroup createTerrainWorld(float[][] heightField, boolean asVertexBufferObject) throws Exception {
		MeshGroup world = new MeshGroup();
		int width = heightField[0].length;
		int height = heightField.length;

		int xParts = width / MAX_TERRAIN_DIMENSION;
		int yParts = height / MAX_TERRAIN_DIMENSION;
		for (int offy = 0; offy < yParts; offy++) {
			for (int offx = 0; offx < xParts; offx++) {

				float[][] heights = new float[MAX_TERRAIN_DIMENSION + 1][MAX_TERRAIN_DIMENSION + 1];
				int xFieldOffset = offx * (MAX_TERRAIN_DIMENSION);
				int yFieldOffset = offy * (MAX_TERRAIN_DIMENSION);
				xFieldOffset = Math.max(0, xFieldOffset - 1);
				yFieldOffset = Math.max(0, yFieldOffset - 1);
				for (int y = 0; y <= MAX_TERRAIN_DIMENSION; y++) {
					for (int x = 0; x <= MAX_TERRAIN_DIMENSION; x++) {
						heights[y][x] = heightField[yFieldOffset + y][xFieldOffset + x];
					}
				}
				OldTerrain t = createTerrain(
						MAX_TERRAIN_DIMENSION + 1,
						MAX_TERRAIN_DIMENSION + 1,
						heights, FIELD_SIZE, offx, offy, width, height);
				t.makeTerrainRegions("/maps/files/standartMapRegion.png");

				if (asVertexBufferObject) {
					Mesh.buildVBOs(t);
				}
				Vector3f pos = new Vector3f();
				pos.x = xFieldOffset * FIELD_SIZE - offx * FIELD_SIZE;
				pos.z = yFieldOffset * FIELD_SIZE - offy * FIELD_SIZE;
				t.setPos(pos);
				world.attach(t);
			}
		}

		//		for (int y = -rimY*2; y < height+rimY*2; y++) {
		//			for (int x = -rimX*2; x < width+rimX*2; x++) {
		//
		//		state.getMapFields()[y][x].cLvl * TERRAIN_CLIFF_SIZE;
		return world;
	}

	/**
	 * Creates the heightField world.
	 *
	 * @param heightField          the height field
	 * @param gl                   the gl
	 * @param glu                  the glu
	 * @param asVertexBufferObject the as vertex bufferList object
	 * @return the world
	 * @throws Exception the exception
	 */
	public static MeshGroup createTerrainWorld(final int[] heightField, final int width, final int height, final int heightMap, final int normalMap, final int specularMap, final boolean asVertexBufferObject) throws Exception {
		final MeshGroup world = new MeshGroup();
		m_width = width;
		m_height = height;
		//		m_halfheight = m_height >> 1;
		m_halfwidth = m_width >> 1;
		m_widthm1 = m_width - 1;
		m_heightm1 = m_height - 1;

		final int xParts = width / MAX_TERRAIN_DIMENSION;
		final int yParts = height / MAX_TERRAIN_DIMENSION;

		final float[][] heights = new float[MAX_TERRAIN_DIMENSION][MAX_TERRAIN_DIMENSION];
		new Thread(() -> {
			for (int toffy = 0; toffy < yParts; toffy++) {
				for (int toffx = 0; toffx < xParts; toffx++) {
					final int offy = toffy;
					final int offx = toffx;

					int xFieldOffset = offx * MAX_TERRAIN_DIMENSION;
					int yFieldOffset = offy * MAX_TERRAIN_DIMENSION;
					for (int y = 0; y < MAX_TERRAIN_DIMENSION; y++) {
						for (int x = 0; x < MAX_TERRAIN_DIMENSION; x++) {
							heights[y][x] = heightField[at((offx * MAX_TERRAIN_DIMENSION) + x, (offy * MAX_TERRAIN_DIMENSION) + y)];
						}
					}
					OldTerrain t = createTerrain(
							MAX_TERRAIN_DIMENSION,
							MAX_TERRAIN_DIMENSION,
							heights, 100, offx, offy, width, height);
					t.heightMapPointer = heightMap;
					t.asVertexBufferObject = asVertexBufferObject;
					t.normalMapPointer = normalMap;
					t.specularMapPointer = specularMap;
					Vector3f pos = new Vector3f();
					pos.x = xFieldOffset * FIELD_SIZE - offx * FIELD_SIZE;
					pos.z = yFieldOffset * FIELD_SIZE - offy * FIELD_SIZE;
					t.setPos(pos);
					world.attach(t);

				}

			}
			world.setLoaded(true);
		}).start();

		//		for (int y = -rimY*2; y < height+rimY*2; y++) {
		//			for (int x = -rimX*2; x < width+rimX*2; x++) {
		//
		//		state.getMapFields()[y][x].cLvl * TERRAIN_CLIFF_SIZE;
		return world;
	}

	/**
	 * Creates the height map.
	 *
	 * @param b                    the b
	 * @param gl                   the gl
	 * @param glu                  the glu
	 * @param asVertexBufferObject the as vertex bufferList object
	 * @return the heightField
	 * @throws Exception the exception
	 */
	public static MeshGroup createTerrainWorldFromImage(BufferedImage b, boolean asVertexBufferObject) throws Exception {
		float[][] heightField = new float[b.getHeight()][b.getWidth()];
		for (int y = 0; y < heightField.length; y++) {
			for (int x = 0; x < heightField[y].length; y++) {
				heightField[y][x] = getPixelValue(b, x, y);
			}
		}

		return createTerrainWorld(heightField, asVertexBufferObject);
	}

	/**********************************************************************/
	public static void createThreaded(int[] heightField, int width, int height, int heightMap, int normalMap, int specularMap, boolean asVertexBufferObject) {

	}

	/**
	 * Gets the pixel value.
	 *
	 * @param img the img
	 * @param x   the x
	 * @param y   the y
	 * @return the pixel value
	 */
	public static float getPixelValue(BufferedImage img, int x, int y) {
		if (x >= img.getWidth() || y >= img.getHeight() || x < 0 || y < 0) {
			x = Math.min(Math.max(0, x), MAX_TEXTURE_SIZE - 1);
			y = Math.min(Math.max(0, y), MAX_TEXTURE_SIZE - 1);
			// throw new
			// ArrayIndexOutOfBoundsException("Coordinate out of bounds: ["
			// +x+", "+y+"] of ["+img.getWidth()+", "+img.getHeight()+"]");
		}
		int c = 0;
		try {
			c = img.getRGB((int) (x / widthScale), (int) (y / widthScale)); // BufferedImage
			// method
			// called
			// getRGB
			// (
			// )
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("out of bounds " + (int) (x / widthScale) + ","
					+ (int) (y / widthScale) + "  maxSize = " + img.getWidth()
					+ "x" + img.getHeight());
		}
		int red = (c & 0x00ff0000) >> 16; // shifting values
		int green = (c & 0x0000ff00) >> 8; // with hex numbers
		int blue = c & 0x000000ff; // to return each color channel

		float value = red + green + blue; // Color to hold RBG values
		// if(value < 10){
		// System.err.println("r "+red+", g"+green+", b "+blue);
		// }
		return value;
	}

	/**
	 * Initialize mesh.
	 *
	 * @param m      the m
	 * @param width  the width
	 * @param height the height
	 */
	private static void initializeMesh(OldTerrain m, int width, int height) {
		m.vertCount = ((width + rimX * 2) * (height + rimY * 2)); // number of points in a rectangle
		m.faceCount = m.vertCount * 2; // number of triangle faces in a
		m.vertices = new Vector3f[m.getVertCount()];

		m.faces = new Face[m.getFaceCount()];

		m.normals = new Vector3f[m.getVertCount()];

		m.texCoords = new Vector3f[m.getVertCount()];

		// printf("--INFO: (Heightmap): Memory allocated\n");
		// initialize all vertices too 0
		for (int i = 0; i < m.vertCount; i++) {
			m.vertices[i] = new Vector3f();
			m.texCoords[i] = new Vector3f();
			m.normals[i] = new Vector3f(0, 1, 0);
		}
		System.err.println("heightField faces: " + m.faceCount);
		for (int i = 0; i < m.faceCount; i++) {
			m.faces[i] = new Face();
		}
	}

	@SuppressWarnings("null")
	public static MeshGroup loadTerrainFromXML(String saveName, String path, int partsX, int partsY) throws ResourceException {

		path += File.separator;
		saveName = saveName.replaceAll(".mesh", "");
		saveName = saveName.replaceAll(".xml", "");
		System.out.println("[TERRAIN] loading " + saveName);
		MeshGroup world = new MeshGroup();
		int i = 0;
		for (int x = 0; x < partsX; x++) {
			for (int y = 0; y < partsY; y++) {
				String saveNameXML = saveName + i + ".mesh"; //the .xml will be added in the ogreParser method
				//			    System.out.println("loading heightField part mesh: "+saveNameXML);
				OldTerrain terrain = null;//(OldTerrain) Mesh.loadFromXML(path, saveNameXML, XMLOgreParser.TYPE_TERRAIN);
				terrain.getMaterial().setName(saveName);
				terrain.setIndicedNormals(true);
				terrain.scale = scalefactor;

				//				System.err.println("placing heightField at "+x*MAX_TERRAIN_DIMENSION*FIELD_SIZE +", "+ y*MAX_TERRAIN_DIMENSION*FIELD_SIZE);
				terrain.setPos((x * MAX_TERRAIN_DIMENSION * FIELD_SIZE) - (x * FIELD_SIZE), 0, y * MAX_TERRAIN_DIMENSION * FIELD_SIZE - (y * FIELD_SIZE));
				world.attach(terrain);
				i++;
			}
		}

		i = 0;
		for (AbstractSceneNode f : world.getChilds()) {
			if (f instanceof OldTerrain) {
				OldTerrain t = (OldTerrain) f;
				t.makeTerrainRegions("./maps/files/" + t.getMaterial().getName() + i + ".png");
				//				System.err.println("heightField regions made");
				i++;
			}
		}

		return world;
	}

	/**
	 * Make normals.
	 *
	 * @param m the m
	 */
	private static void makeNormals(OldTerrain m) {

		// cycle through each row
		// for (int i = 0; i < _h; i++) {
		// // cycle through each column
		// for (int j = 0; j < _w; j++) {
		for (int j = 0; j < m.faceCount; j++) {
			Vector3f p1, p2, p3, v1, v2, tmp;
			// calculate the positions int the vertex bufferList of the adjacent
			// vertices

			p1 = m.vertices[m.faces[j].m_vertsIndex[0]];
			p2 = m.vertices[m.faces[j].m_vertsIndex[1]];
			p3 = m.vertices[m.faces[j].m_vertsIndex[2]];
			// store the positions of first triangle into 3 usable vertices
			// p1 = m.vertices[clazz[0]];
			// p2 = m.vertices[clazz[3]];
			// p3 = m.vertices[clazz[2]];

			// calculate the triangle sides

			v1 = Vector3fTools.sub(p2, p1);
			// v1.scalarMult(-1);
			v2 = Vector3fTools.sub(p3, p1);
			v2.scale(-1);
			// calculate normal
			tmp = Vector3fTools.crossProduct(v1, v2);
			// add normal to the normal of all the vertices in the triangle
			m.normals[m.faces[j].m_vertsIndex[0]].add(tmp);
			m.normals[m.faces[j].m_vertsIndex[1]].add(tmp);
			m.normals[m.faces[j].m_vertsIndex[2]].add(tmp);

		}
		// }

		// normalize all vertices
		for (int i = 0; i < m.vertCount; i++) {
			m.normals[i].normalize();
		}
	}

	/**
	 * high map face triangularisation will work like this:
	 * <p/>
	 * We have vertices in the form of: 4 8 12 3 7 11 2 6 10 1 5 9 ...
	 * <p/>
	 * The first loop will create triangles like (1,2,5) (2,3,6) (3,4,7) and
	 * so on The second loop will create triangles like (12,11,8) (11,10,7)
	 * (10,9,6) and so on
	 *
	 * @param m the m
	 * @param w the w
	 * @param h the h
	 */
	private static void triangulate(OldTerrain m, int w, int h) {

		int in = 0;
		for (int z = 0; z < h - 1; z++) {
			for (int x = 0; x < w - 1; x++) {

				m.faces[in].m_vertsIndex[2] = (z * w) + x;
				m.faces[in].m_vertsIndex[1] = (z * w) + x + 1;
				m.faces[in].m_vertsIndex[0] = (z * w) + x + w;

				m.faces[in].m_normalIndex[2] = (z * w) + x;
				m.faces[in].m_normalIndex[1] = (z * w) + x + 1;
				m.faces[in].m_normalIndex[0] = (z * w) + x + w;

				m.faces[in].m_texCoordsIndex[2] = (z * w) + x;
				m.faces[in].m_texCoordsIndex[1] = (z * w) + x + 1;
				m.faces[in].m_texCoordsIndex[0] = (z * w) + x + w;
				in++;
			}
			// System.err.println("--------------");
		}
		for (int z = h - 1; z > 0; z--) {
			for (int x = w - 1; x > 0; x--) {
				m.faces[in].m_vertsIndex[2] = (z * w) + x;
				m.faces[in].m_vertsIndex[1] = (z * w) + x - 1;
				m.faces[in].m_vertsIndex[0] = (z * w) + x - w;

				m.faces[in].m_normalIndex[2] = (z * w) + x;
				m.faces[in].m_normalIndex[1] = (z * w) + x - 1;
				m.faces[in].m_normalIndex[0] = (z * w) + x - w;

				m.faces[in].m_texCoordsIndex[2] = (z * w) + x;
				m.faces[in].m_texCoordsIndex[1] = (z * w) + x - 1;
				m.faces[in].m_texCoordsIndex[0] = (z * w) + x - w;
				in++;
			}
		}
		//		System.err.printf(
		//				"[TERRAIN] Heightmap: Triangulation finished. Total faces: %d\n",
		//				in);
	}

	@Override
	public void draw() {
		//		System.err.println(isLoaded()+" && "+asVertexBufferObject);
		if (!isLoaded() && asVertexBufferObject) {
			try {
				Mesh.buildVBOs(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}
		if (isFirstDraw()) {
			onInit();
			setFirstDraw(false);
			vertices = null;
			texCoords = null;
			normals = null;
			faces = null;
			//			System.gc();
		}
		//		System.err.println("drawing terrain");
		if (isVisibleBoundingBox()) {

			super.draw();
			drawnTerrains++;
		}
		//		GL11.glCullFace(GL11.GL_FRONT);
		//		GlUtil.glEnable(GL11.GL_CULL_FACE);
		//		if(!multipassShader.isDrawing()){
		//
		//			multipassShader.drawAllShaders();
		//
		//		}else{
		//
		//			super.draw();
		//		}
		//		GlUtil.glDisable(GL11.GL_CULL_FACE);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.Mesh#onInit(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void onInit() {
		super.onInit();

		makeRegions();
		material.setTexture(new Texture(GL11.GL_TEXTURE_2D, heightMapPointer, "terrain"));
		//				System.err.println("[TERRAIN] Height-TextureNew created");

		//		multipassShader.getShaders().add(ShaderLibrary.terrainShader);
		//		multipassShader.getShaders().add(ShaderLibrary.pointLightShader);
		//		multipassShader.setShaderable(this);
	}

	/**
	 * Draw on the fly. Draws the heightField mesh directly from a given height map.
	 * Warning: this method is very slow! use only when the height map is changed in every cycle!
	 * If the height map is not constantly changed, it's better to generate the height map once
	 * per every change by using {@link #updateHeightMapMesh(GL21, GLU, float[][])}
	 *
	 * @param gl  the gl
	 * @param glu the glu
	 * @param b   the b
	 * @ the error diolog exception
	 */
	public void drawOnTheFly(BufferedImage b) {
		ShaderLibrary.terrainShader.setShaderInterface(terrainShaderable);
		ShaderLibrary.terrainShader.load();
		int x_max = b.getWidth();
		int y_max = b.getHeight(); // Lanscape dimension

		// System.err.println("width "+x_max+", height "+y_max);
		int h = y_max / stepsize + 1;
		int w = x_max / stepsize + 1;

		scale = scalefactor;
		int i = 0;
		// create all vertices
		int size = stepsize;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {

				float color_y = getPixelValue(b, x * size, y * size);
				color_y -= terrainSubZero;

				vertices[i].x = (x * size * widthScale);
				vertices[i].y = (color_y);
				vertices[i].z = (y * size * widthScale);

				texCoords[i].x = ((float) x * size / (x_max) / 2);
				texCoords[i].y = ((float) y * size / (y_max) / 2);
				i++;
			}
		}

		this.setIndicedNormals(true);
		triangulate(this, w, h);
		makeNormals(this);


		/*
	     * now that we have everything in our mesh what we need, we can return
		 * it
		 */
		super.draw();

		ShaderLibrary.terrainShader.unload();
	}

	/**
	 * Gets the height.
	 *
	 * @param x the x
	 * @param y the y
	 * @return the height
	 */
	public float getHeight(float x, float y) {
		try {
			if (heightMap != null) {
				return getPixelValue((int) (x / FIELD_SIZE), (int) (y / FIELD_SIZE)) / 1.534f - terrainSubZero;
			} else {
				throw new NullPointerException("not implemented");
				//			return state.getMapFields()[(int) (y/FIELD_SIZE)][(int) (x/FIELD_SIZE)].cLvl * OldTerrain.TERRAIN_CLIFF_SIZE;
			}
		} catch (IndexOutOfBoundsException e) {
			//			System.err.println("out of bounds: "+x+", "+y);
			return 0;
		}
	}

	/**
	 * @return the heightFields
	 */
	public float[][] getHeightFields() {
		return heightFields;
	}

	/**
	 * @param heightFields the heightFields to set
	 */
	public void setHeightFields(float heightFields[][]) {
		this.heightFields = heightFields;
	}

	/**
	 * Gets the height map.
	 *
	 * @return the height map
	 */
	public BufferedImage getHeightMap() {
		return heightMap;
	}

	/**
	 * Sets the height map.
	 *
	 * @param heightMap the new height map
	 */
	public void setHeightMap(BufferedImage heightMap) {
		this.heightMap = heightMap;
	}

	/**
	 * @return the origVerts
	 */
	public Vector3f[] getOrigVerts() {
		return origVerts;
	}

	/**
	 * @param origVerts the origVerts to set
	 */
	public void setOrigVerts(Vector3f[] origVerts) {
		this.origVerts = origVerts;
	}

	/**
	 * Gets the pixel value.
	 *
	 * @param x the x
	 * @param y the y
	 * @return the pixel value
	 */
	public float getPixelValue(int x, int y) {
		if (x >= heightMap.getWidth() || y >= heightMap.getHeight() || x < 0
				|| y < 0) {
			x = Math.min(Math.max(0, x), MAX_TEXTURE_SIZE - 1);
			y = Math.min(Math.max(0, y), MAX_TEXTURE_SIZE - 1);
			// throw new
			// ArrayIndexOutOfBoundsException("Coordinate out of bounds: ["
			// +x+", "+y+"] of ["+img.getWidth()+", "+img.getHeight()+"]");
		}
		int c = heightMap
				.getRGB((int) (x / widthScale), (int) (y / widthScale)); // BufferedImage
		// method
		// called
		// getRGB
		// (
		// )
		int red = (c & 0x00ff0000) >> 16; // shifting values
		int green = (c & 0x0000ff00) >> 8; // with hex numbers
		int blue = c & 0x000000ff; // to return each color channel

		float value = red + green + blue; // Color to hold RBG values
		// if(value < 10){
		// System.err.println("r "+red+", g"+green+", b "+blue);
		// }
		return value;
	}

	/**
	 * @return the terrainShaderable
	 */
	public TerrainShaderable getTerrainShaderable() {
		return terrainShaderable;
	}

	/**
	 * @param terrainShaderable the terrainShaderable to set
	 */
	public void setTerrainShaderable(TerrainShaderable terrainShaderable) {
		this.terrainShaderable = terrainShaderable;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.Mesh#draw(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	public boolean isVisibleBoundingBox() {

		return true;

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
				regions[TERRAIN_REGION_DIRT] = new TerrainRegion(Controller.getTexLoader().getTexture2D(
						DataUtil.dataPath + "/heightmaps/dirt.png", true), 0 * scale, 80 * scale);
				regions[TERRAIN_REGION_GRASS] = new TerrainRegion(Controller.getTexLoader().getTexture2D(
						DataUtil.dataPath + "/heightmaps/grass.png", true), 80 * scale, 130 * scale);
				regions[TERRAIN_REGION_ROCK] = new TerrainRegion(Controller.getTexLoader().getTexture2D(
						DataUtil.dataPath + "/heightmaps/rock.png", true), 130 * scale, 180 * scale);
				regions[TERRAIN_REGION_SNOW] = new TerrainRegion(Controller.getTexLoader().getTexture2D(
						DataUtil.dataPath + "/heightmaps/snow.png", true), 180 * scale, 255 * scale);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Make heightField regions.
	 *
	 * @param gl        the gl
	 * @param glu       the glu
	 * @param heightMap the height map
	 */
	public void makeTerrainRegions(BufferedImage heightMap) {
		Controller.getTexLoader();
		this.material.setTexture(TextureLoader.getTexture(heightMap, "terainRegion",
				GL11.GL_TEXTURE_2D, // target
				GL11.GL_RGBA,     // dst pixel format
				GL11.GL_LINEAR, // min filter (unused)
				GL11.GL_LINEAR, true, true));
		//		System.err.println("[TERRAIN] Height-TextureNew created");
		this.heightMap = heightMap;
		makeRegions();
		try {
			normalMapPointer = Controller.getTexLoader().getTexture2D(DataUtil.dataPath + "heightmaps/ground-normalMap.png", true).getTextureId();
			specularMapPointer = Controller.getTexLoader().getTexture2D(DataUtil.dataPath + "heightmaps/ground-specular.png", true).getTextureId();
		} catch (IOException e) {

			e.printStackTrace();
		}
		///image-resource/specularMap.png
	}

	/**
	 * Make heightField regions.
	 *
	 * @param gl                   the gl
	 * @param glu                  the glu
	 * @param heightMapTexturePath the height map texture path
	 */
	public void makeTerrainRegions(String heightMapTexturePath) {
		try {
			heightMap = ImageIO.read(ResourceLoader.resourceUtil.getResourceURL(
					DataUtil.dataPath + heightMapTexturePath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		makeTerrainRegions(heightMap);
	}

	/**
	 * Make heightField regions flat.
	 *
	 * @param gl  the gl
	 * @param glu the glu
	 */
	public void makeTerrainRegionsFlat() {
		makeRegions();
	}

	/**
	 * Reload shader.
	 *
	 * @param gl  the gl
	 * @param glu the glu
	 * @ the error diolog exception
	 */
	public void reloadShader() {
		ShaderLibrary.loadShaders();
	}

	/**
	 * Update height map mesh.
	 *
	 * @param gl  the gl
	 * @param glu the glu
	 * @param map the map
	 */
	public void updateHeightMapMesh() {
		//		ShaderLibrary.terrainShader.load();
		int h = heightFields.length;
		int w = heightFields[0].length;
		int size = FIELD_SIZE;
		int i = 0;

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				this.getOrigVerts()[i].x = (x * size * widthScale);
				this.getOrigVerts()[i].y = (heightFields[y][x]);
				this.getOrigVerts()[i].z = (y * size * widthScale);
				i++;
			}
		}
		setIndicedNormals(true);
		//		triangulate(this, w+(2*rimX), h+(2*rimY));
		if (optimizer == null) {
			optimizer = new TerrainMeshOptimizer(this, w, h);
		}
		//warning: this is not tested. the texture coords could be fucked up
		optimizer.optimize(0, 0, w, h);
		makeNormals(this);

	}

	/**
	 * Update texture.
	 *
	 * @param gl  the gl
	 * @param glu the glu
	 * @param b   the b
	 */
	public void updateTexture(BufferedImage b) {

		this.material.getTexture().updateTexture(b);
	}

	/**
	 * The Class TerrainRegion.
	 */
	private class TerrainRegion {

		/**
		 * The texture.
		 */
		private Texture texture;

		/**
		 * The max.
		 */
		private float min, max;

		/**
		 * Instantiates a new heightField region.
		 *
		 * @param texture the texture
		 * @param min     the min
		 * @param max     the max
		 */
		public TerrainRegion(Texture texture, float min, float max) {
			super();
			this.texture = texture;
			this.min = min;
			this.max = max;
		}

		/**
		 * Gets the max.
		 *
		 * @return the max
		 */
		public float getMax() {
			return max;
		}

		/**
		 * Gets the min.
		 *
		 * @return the min
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
		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * org.schema.schine.graphicsengine.shader.Shaderable#onExit(javax.media
		 * .openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
		 */
		@Override
		public void onExit() {
			if (isFirstDraw()) {
				return;
			}
			getMaterial().getTexture().unbindFromIndex();
			regions[TERRAIN_REGION_DIRT].getTexture().unbindFromIndex();
			regions[TERRAIN_REGION_GRASS].getTexture().unbindFromIndex();
			regions[TERRAIN_REGION_ROCK].getTexture().unbindFromIndex();
			regions[TERRAIN_REGION_SNOW].getTexture().unbindFromIndex();
			GlUtil.glActiveTexture(GL13.GL_TEXTURE5);
			GlUtil.glDisable(GL11.GL_TEXTURE_2D);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

			GlUtil.glActiveTexture(GL13.GL_TEXTURE6);
			GlUtil.glDisable(GL11.GL_TEXTURE_2D);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

			GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		}

		@Override
		public void updateShader(DrawableScene scene) {

		}

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * org.schema.schine.graphicsengine.shader.Shaderable#updateShaderParameters
		 * (javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU, int)
		 */
		@Override
		public void updateShaderParameters(Shader shader) {
			if (isFirstDraw()) {
				return;
			}
			super.updateShaderParameters(shader);

			material.attach(0);

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

			GlUtil.updateShaderFloat(shader, "heightMapScale", scale);

			// Update dirt heightField region.

			GlUtil.updateShaderFloat(shader, "dirtRegion.max",
					regions[TERRAIN_REGION_DIRT].getMax());

			GlUtil.updateShaderFloat(shader, "dirtRegion.min",
					regions[TERRAIN_REGION_DIRT].getMin());

			// Update grass heightField region.

			GlUtil.updateShaderFloat(shader, "grassRegion.max",
					regions[TERRAIN_REGION_GRASS].getMax());

			GlUtil.updateShaderFloat(shader, "grassRegion.min",
					regions[TERRAIN_REGION_GRASS].getMin());

			// Update rock heightField region.

			GlUtil.updateShaderFloat(shader, "rockRegion.max",
					regions[TERRAIN_REGION_ROCK].getMax());

			GlUtil.updateShaderFloat(shader, "rockRegion.min",
					regions[TERRAIN_REGION_ROCK].getMin());

			// Update snow heightField region.

			GlUtil.updateShaderFloat(shader, "snowRegion.max",
					regions[TERRAIN_REGION_SNOW].getMax());

			GlUtil.updateShaderFloat(shader, "snowRegion.min",
					regions[TERRAIN_REGION_SNOW].getMin());
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

			regions[TERRAIN_REGION_DIRT].getTexture().bindOnShader(
					GL13.GL_TEXTURE0, 0, "dirtColorMap", shader);
			regions[TERRAIN_REGION_GRASS].getTexture().bindOnShader(
					GL13.GL_TEXTURE1, 1, "grassColorMap", shader);
			regions[TERRAIN_REGION_ROCK].getTexture().bindOnShader(
					GL13.GL_TEXTURE2, 2, "rockColorMap", shader);
			regions[TERRAIN_REGION_SNOW].getTexture().bindOnShader(
					GL13.GL_TEXTURE3, 3, "snowColorMap", shader);

			getMaterial().getTexture().bindOnShader(GL13.GL_TEXTURE4, 4,
					"heightMap", shader);

			GlUtil.glActiveTexture(GL13.GL_TEXTURE5);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, normalMapPointer);
			GlUtil.updateShaderInt(shader, "normalMap", 5);

			GlUtil.glActiveTexture(GL13.GL_TEXTURE6);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, specularMapPointer);
			GlUtil.updateShaderInt(shader, "specularMap", 6);

			GlUtil.glActiveTexture(GL13.GL_TEXTURE7); // switch to unused texture unit
			// or shader wont take the
			// texture in the current slot
		}
	}

}
