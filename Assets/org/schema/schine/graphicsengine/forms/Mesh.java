/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>Mesh</H2>
 * <H3>org.schema.schine.graphicsengine.forms</H3>
 * Mesh.java
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

import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;
import org.schema.common.util.data.DataUtil;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.texture.Material;
import org.schema.schine.input.Keyboard;
import org.schema.schine.physics.Physical;
import org.schema.schine.resource.FileExt;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * The Class Mesh.
 */
public class Mesh extends Geometry {
	private final static int MAX_SIMUL_VBO_BUILDING = 1;
	/**
	 * The Constant TYPE_SOFTWARE.
	 */
	public static final int TYPE_SOFTWARE = 0;
	/**
	 * The Constant TYPE_DISPLAY_LIST.
	 */
	public static final int TYPE_DISPLAY_LIST = 1;
	/**
	 * The Constant TYPE_VERTEX_ARRAY.
	 */
	public static final int TYPE_VERTEX_ARRAY = 2;
	/**
	 * The Constant TYPE_VERTEX_BUFFER_OBJ.
	 */
	public static final int TYPE_VERTEX_BUFFER_OBJ = 3;
	public static final int BUFFERTYPE_Index = 1;
	public static final int BUFFERTYPE_Position = 2;
	public static final int BUFFERTYPE_Normal = 3;
	public static final int BUFFERTYPE_Tangent = 4;
	public static final int BUFFERTYPE_Binormal = 5;
	public static final int BUFFERTYPE_TexCoord = 6;
	private static final int BUFFERTYPE_IndexNormals = 7;
	private static final int BUFFERTYPE_IndexTexcords = 8;
	private static int buildingVBOCount;
	private static int cycles;
	public ConvexShape shape;
	/**
	 * The faces.
	 */
	public Face faces[];
	// public static int mode = 0;
	/**
	 * The tex coords.
	 */
	public Vector3f texCoords[];
	/**
	 * The normals.
	 */
	public Vector3f normals[];
	public IntBuffer VBOindex = MemoryUtil.memAllocInt(1);
	public IntBuffer normalIndexBuffer;
	public IntBuffer texCoordIndexBuffer;
	public FloatBuffer texCoordsBuffer; // TextureNew Coordinates
	public FloatBuffer normalsBuffer; // TextureNew Coordinates
	public int texCoordSetCount;
	public int currentTexCoordSet;
	/**
	 * The face count.
	 */
	protected int faceCount;
	/**
	 * The indiced normals.
	 */
	private boolean indicedNormals = false;
	/**
	 * The VBO vertices.
	 */
	private IntBuffer VBOVertices = MemoryUtil.memAllocInt(1); // Vertex VBO id
	/**
	 * The VBO tex coords.
	 */
	private IntBuffer VBOTexCoords = MemoryUtil.memAllocInt(1);// TextureNew Coordinate VBO id
	// public static final int MODE_WITH_VERTEX_BUFFER= 0;
	// public static final int MODE_WITHOUT_VERTEX_BUFFER= 1;
	private IntBuffer VBOTexCoordsindex = MemoryUtil.memAllocInt(1);
	private IntBuffer VBONormalindex = MemoryUtil.memAllocInt(1);
	/**
	 * The VBO normals.
	 */
	private IntBuffer VBONormals = MemoryUtil.memAllocInt(1);
	private IntBuffer VBOTangents = MemoryUtil.memAllocInt(1);
	private IntBuffer VBOBinormals = MemoryUtil.memAllocInt(1);
	private int currentBufferIndex = 0;
	/**
	 * The type.
	 */
	private int type;
	/**
	 * The tex coord count.
	 */
	private int texCoordCount;
	/**
	 * The collision object.
	 */
	private boolean collisionObject;
	/**
	 * The normal count.
	 */
	private int normalCount;
	/**
	 * The first draw.
	 */
	private boolean firstDraw;
	/**
	 * The vertex bone assignments.
	 */
	private VertexBoneWeight[] vertexBoneAssignments;
	/**
	 * The bone draw mode.
	 */
	private boolean boneDrawMode;
	/**
	 * The collision vec.
	 */
	private Vector3f collisionVec;
	/**
	 * The collision face normal.
	 */
	private Vector3f collisionFaceNormal;
	/**
	 * The updated.
	 */
	private boolean updated;
	private Skin skin;
	/**
	 * The drawing wireframe.
	 */
	private boolean drawingWireframe;
	/**
	 * The static mesh.
	 */
	private boolean staticMesh;
	private int phase = 0;
	private int drawMode = GL11.GL_TRIANGLES;
	private boolean meshPointersLoaded;
	/**
	 * Checks if is pivot centered.
	 *
	 * @return true, if is pivot centered
	 */
	private Vector3f cTmp = new Vector3f();
	private boolean vboLoaded;
	public Vector3f[] tangents;
	public Vector3f[] binormals;
	public IntBuffer tangentIndexBuffer;
	public IntBuffer binormalIndexBuffer;
	public FloatBuffer tangentsBuffer;
	public FloatBuffer binormalsBuffer;
	public boolean hasTangents;
	public boolean hasBinormals;
	public IntArrayList recordedIndices = new IntArrayList();
	public it.unimi.dsi.fastutil.objects.ObjectArrayList<Vector3f> recordedVectices = new it.unimi.dsi.fastutil.objects.ObjectArrayList<Vector3f>();
	public it.unimi.dsi.fastutil.objects.ObjectArrayList<Vector3f> recordedNormals = new it.unimi.dsi.fastutil.objects.ObjectArrayList<Vector3f>();
	public it.unimi.dsi.fastutil.objects.ObjectArrayList<Vector2f> recordedTextcoords = new it.unimi.dsi.fastutil.objects.ObjectArrayList<Vector2f>();

	/**
	 * Instantiates a new mesh.
	 */
	public Mesh() {
		firstDraw = true;
		material = new Material();
	}

	/**
	 * Adds the triangles recursive.
	 *
	 * @param current       the current
	 * @param w             the w
	 * @param lvlOfDetail   the lvl of detail
	 * @param sizeThreshold the size threshold
	 */
	private static void addTrianglesRecursive(Mesh current, MeshGroup w, int lvlOfDetail, float sizeThreshold) {
		for(AbstractSceneNode f : current.getChilds()) {
			if(f instanceof Mesh) {
				Mesh child = (Mesh) f;
				Mesh[] triangles = getTriangles(child, lvlOfDetail, sizeThreshold);
				for(int i = 0; i < triangles.length; i += lvlOfDetail) {
					Mesh m = triangles[i];
					if(m != null) {
						w.attach(m);
					}
				}
				addTrianglesRecursive(child, w, lvlOfDetail, sizeThreshold);
			}
		}
	}

	/**
	 * Builds the vb os.
	 *
	 * @param gl   the gl
	 * @param mesh the mesh
	 *
	 * @throws Exception the exception
	 */
	public static void buildVBOs(Mesh mesh) throws Exception {
		// Logger.println("Generating Vertex Buffer Object of "+mesh.name);
		if(mesh.phase == 0 && buildingVBOCount >= MAX_SIMUL_VBO_BUILDING) {
			return;
		} else if(mesh.phase == 0) {
			buildingVBOCount++;
		}
		//		System.err.println(cycles);
		cycles++;
		if(mesh.phase != 0 && cycles % 5 != 0) {
			return;
		}
		int pPhase = 0;
		int sSizeVerts = mesh.faceCount * 3 * 3;
		int sSizeTex = mesh.faceCount * 2 * 3;
		int sSizeNorm = mesh.faceCount * 3 * 3;
		long allocTime = System.currentTimeMillis();
		//		System.err.println("VBO building phase "+mesh.phase);
		if(mesh.phase == pPhase++) {
			mesh.phase++;
			return;
		}
		if(mesh.phase == pPhase++) {
			mesh.phase++;
			return;
		}
		if(mesh.phase == pPhase++) {
			mesh.phase++;
			return;
		}
		allocTime = System.currentTimeMillis() - allocTime;
		int cordCount = 0;
		int c = 0;
		long fillTime = System.currentTimeMillis();
		if(mesh.phase == pPhase++) {
			mesh.phase++;
			return;
		}
		fillTime = System.currentTimeMillis() - fillTime;
		long genBufferTime = System.currentTimeMillis();
		// mesh.vertices = hVert;
		genBufferTime = System.currentTimeMillis() - genBufferTime;
		long bufferTime = System.currentTimeMillis();
		if(mesh.phase == pPhase++) {
			assert (mesh.verticesBuffer.position() == 0);
			createVertexBuffers(BUFFERTYPE_Position, mesh, sSizeVerts);
			mesh.phase++;
			return;
		}
		if(mesh.phase == pPhase++) {
			assert (mesh.texCoordsBuffer.position() == 0);
			createVertexBuffers(BUFFERTYPE_TexCoord, mesh, sSizeTex);
			mesh.phase++;
			return;
		}
		if(mesh.phase == pPhase++) {
			assert (mesh.normalsBuffer.position() == 0);
			createVertexBuffers(BUFFERTYPE_Normal, mesh, sSizeNorm);
			mesh.phase++;
			return;
		}
		if(mesh.phase == pPhase++) {
			if(mesh.hasTangents) {
				assert (mesh.tangentsBuffer.position() == 0);
				createVertexBuffers(BUFFERTYPE_Tangent, mesh, sSizeNorm);
			}
			mesh.phase++;
			return;
		}
		if(mesh.phase == pPhase++) {
			if(mesh.hasBinormals) {
				assert (mesh.binormalsBuffer.position() == 0);
				createVertexBuffers(BUFFERTYPE_Binormal, mesh, sSizeNorm);
			}
			mesh.phase++;
			return;
		}
		if(mesh.phase == pPhase++) {
			assert (mesh.getIndexBuffer().position() == 0);
			createVertexBuffers(BUFFERTYPE_Index, mesh, mesh.getIndexBuffer().capacity());
			mesh.phase++;
			return;
		}
		bufferTime = System.currentTimeMillis() - bufferTime;
		// Our Copy Of The Data Is No Longer Necessary, It Is Safe In The
		// Graphics Card
		if(mesh.phase == pPhase++) {
			//			System.err.println("cleaning buffers");
			// mesh.faces = null;
			// mesh.vertices = null; //needed for manipulation
			//		mesh.texCoords = null;
			//		mesh.normals = null;
			mesh.type = Mesh.TYPE_VERTEX_BUFFER_OBJ;
			//		 System.err.println("FINISHED BUILDING VERTEX BUFFER OBJECT FOR "
			//		 + mesh.name+"; it is now type "+mesh.getType());
			buildingVBOCount--;
			//			System.err.println("BOUND "+mesh.getName()+" to VBO("+mesh.getVBOVertices().get(0)+"): faces: "+mesh.getFaceCount()+", indices: "+mesh.getIndexBuffer().capacity()+" type: "+mesh.type+", alloc: "+allocTime+", prep: "+fillTime+", gen: "+genBufferTime+", bufferList: "+bufferTime+", total: "+(allocTime+fillTime+genBufferTime+bufferTime)+", currently building: "+buildingVBOCount);
			mesh.setLoaded(true);
		}
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}

	/**
	 * Calculate bounding box.
	 *
	 * @param m the m
	 *
	 * @return the bounding box
	 */
	public static BoundingBox calculateBoundingBox(Mesh m) {
		Vector3f min = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
		for(int i = 0; i < m.vertCount; i++) {
			min.x = (Math.min(min.x, m.vertices[i].x));
			max.x = (Math.max(max.x, m.vertices[i].x));
			min.y = (Math.min(min.y, m.vertices[i].y));
			max.y = (Math.max(max.y, m.vertices[i].y));
			min.z = (Math.min(min.z, m.vertices[i].z));
			max.z = (Math.max(max.z, m.vertices[i].z));
		}
		BoundingBox aabb = new BoundingBox(min, max);
		//		System.err.println("AABB: "+aabb);
		return aabb;
	}

	private static void createVertexBuffers(int buffertypeIndex, Mesh mesh, int size) {
		switch(buffertypeIndex) {
			case BUFFERTYPE_Index -> {
				if(mesh.VBOindex.get(0) == 0) {
					GL15.glGenBuffers(mesh.VBOindex); // Get A Valid Name
					Controller.loadedVBOBuffers.add(mesh.VBOindex.get(0));
				}
				GlUtil.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, mesh.VBOindex.get(0)); // Bind
				// Load The Data
				GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, mesh.getIndexBuffer(), GL15.GL_STREAM_DRAW);
				GlUtil.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0); // Bind
			}
			case BUFFERTYPE_IndexNormals -> {
				if(mesh.VBOindex.get(0) == 0) {
					GL15.glGenBuffers(mesh.VBOindex); // Get A Valid Name
					Controller.loadedVBOBuffers.add(mesh.VBOindex.get(0));
				}
				GlUtil.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, mesh.VBONormalindex.get(0)); // Bind
				// Load The Data
				GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, mesh.normalIndexBuffer, GL15.GL_STREAM_DRAW);
				GlUtil.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0); // Bind
			}
			case BUFFERTYPE_IndexTexcords -> {
				if(mesh.VBOindex.get(0) == 0) {
					GL15.glGenBuffers(mesh.VBOindex); // Get A Valid Name
					Controller.loadedVBOBuffers.add(mesh.VBOindex.get(0));
				}
				GlUtil.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, mesh.VBOTexCoordsindex.get(0)); // Bind
				// Load The Data
				GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, mesh.texCoordIndexBuffer, GL15.GL_STREAM_DRAW);
				GlUtil.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0); // Bind
			}
			case BUFFERTYPE_Position -> {
				//			System.err.println("buffering vertex bufferList "+size+", "+mesh.verticesBuffer);
				// Generate And Bind The Vertex Buffer
				GL15.glGenBuffers(mesh.VBOVertices); // Get A Valid Name
				Controller.loadedVBOBuffers.add(mesh.VBOVertices.get(0));
				//			System.err.println("vertex bufferList generated ");
				GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, mesh.VBOVertices.get(0)); // Bind
				//			System.err.println("vertex bufferList bound ");
				// Load The Data
				GL15.glBufferData(GL15.GL_ARRAY_BUFFER, mesh.verticesBuffer, GL15.GL_STATIC_DRAW);
				//			System.err.println("vertex bufferList buffered");
				GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Bind
			}
			case BUFFERTYPE_TexCoord -> {
				//			System.err.println("buffering tex bufferList");
				// Generate And Bind The TextureNew Coordinate Buffer
				GL15.glGenBuffers(mesh.VBOTexCoords); // Get A Valid Name
				Controller.loadedVBOBuffers.add(mesh.VBOTexCoords.get(0));
				GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, mesh.VBOTexCoords.get(0)); // Bind
				// Load The Data
				GL15.glBufferData(GL15.GL_ARRAY_BUFFER, mesh.texCoordsBuffer, GL15.GL_STATIC_DRAW);
				GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Bind
			}
			case BUFFERTYPE_Normal -> {
				//			System.err.println("buffering normal bufferLiast");
				// Generate And Bind The Normal Buffer
				GL15.glGenBuffers(mesh.VBONormals); // Get A Valid Name
				Controller.loadedVBOBuffers.add(mesh.VBONormals.get(0));
				GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, mesh.VBONormals.get(0)); // Bind
				// Load The Data
				GL15.glBufferData(GL15.GL_ARRAY_BUFFER, mesh.normalsBuffer, GL15.GL_STATIC_DRAW);
				GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Bind
			}
			case BUFFERTYPE_Tangent -> {
				//			System.err.println("buffering normal bufferLiast");
				// Generate And Bind The Normal Buffer
				GL15.glGenBuffers(mesh.VBOTangents); // Get A Valid Name
				Controller.loadedVBOBuffers.add(mesh.VBOTangents.get(0));
				GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, mesh.VBOTangents.get(0)); // Bind
				// Load The Data
				GL15.glBufferData(GL15.GL_ARRAY_BUFFER, mesh.tangentsBuffer, GL15.GL_STATIC_DRAW);
				GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Bind
			}
			case BUFFERTYPE_Binormal -> {
				//			System.err.println("buffering normal bufferLiast");
				// Generate And Bind The Normal Buffer
				GL15.glGenBuffers(mesh.VBOBinormals); // Get A Valid Name
				Controller.loadedVBOBuffers.add(mesh.VBOBinormals.get(0));
				GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, mesh.VBOBinormals.get(0)); // Bind
				// Load The Data
				GL15.glBufferData(GL15.GL_ARRAY_BUFFER, mesh.binormalsBuffer, GL15.GL_STATIC_DRAW);
				GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Bind
			}
		}
		GlUtil.printGlErrorCritical();
	}

	/**
	 * Gets the triangles.
	 *
	 * @param m             the m
	 * @param lvlOfDetail   the lvl of detail
	 * @param sizeThreshold the size threshold
	 *
	 * @return the triangles
	 */
	private static Mesh[] getTriangles(Mesh m, int lvlOfDetail, float sizeThreshold) {
		Mesh[] triangles = new Mesh[m.faceCount];
		try {
			String userDataString = "<PhysicsObject><ShapeType>triangle</ShapeType><CylinderOrientation>x</CylinderOrientation></PhysicsObject>";
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(userDataString));
			Document doc = db.parse(is);
			float extrudesize = 3;
			for(int i = 0; i < m.faceCount; i += lvlOfDetail) {
				Face f = m.faces[i];
				Vector3f centerOfFace = m.faces[i].getCentroid(m);
				Vector3f ab = Vector3fTools.sub(m.vertices[f.m_vertsIndex[1]], m.vertices[f.m_vertsIndex[0]]);
				Vector3f ac = Vector3fTools.sub(m.vertices[f.m_vertsIndex[2]], m.vertices[f.m_vertsIndex[0]]);
				Vector3f bc = Vector3fTools.sub(m.vertices[f.m_vertsIndex[2]], m.vertices[f.m_vertsIndex[1]]);
				if(ab.length() + ac.length() + bc.length() < sizeThreshold) {
					continue;
				}
				triangles[i] = new Mesh();
				triangles[i].indicedNormals = true;
				triangles[i].setUserData(doc);
				triangles[i].vertCount = 6;
				triangles[i].vertices = new Vector3f[triangles[i].vertCount];
				triangles[i].normalCount = 6;
				triangles[i].normals = new Vector3f[triangles[i].normalCount];
				triangles[i].texCoordCount = 6;
				triangles[i].texCoords = new Vector3f[triangles[i].texCoordCount];
				triangles[i].faceCount = 8;
				triangles[i].faces = new Face[triangles[i].faceCount];
				Vector3f dPivot = Vector3fTools.sub(m.getInitionPos(), centerOfFace);
				triangles[i].vertices[0] = Vector3fTools.add(dPivot, m.vertices[f.m_vertsIndex[0]]);
				triangles[i].normals[0] = new Vector3f(m.normals[f.m_normalIndex[0]]);
				triangles[i].texCoords[0] = new Vector3f(m.texCoords[f.m_texCoordsIndex[0]]);
				triangles[i].vertices[1] = Vector3fTools.add(dPivot, m.vertices[f.m_vertsIndex[1]]);
				triangles[i].normals[1] = new Vector3f(m.normals[f.m_normalIndex[1]]);
				triangles[i].texCoords[1] = new Vector3f(m.texCoords[f.m_texCoordsIndex[1]]);
				triangles[i].vertices[2] = Vector3fTools.add(dPivot, m.vertices[f.m_vertsIndex[2]]);
				triangles[i].normals[2] = new Vector3f(m.normals[f.m_normalIndex[2]]);
				triangles[i].texCoords[2] = new Vector3f(m.texCoords[f.m_texCoordsIndex[2]]);
				triangles[i].vertices[3] = new Vector3f(triangles[i].vertices[0]);
				triangles[i].normals[3] = new Vector3f(triangles[i].normals[0]);
				triangles[i].texCoords[3] = new Vector3f(triangles[i].texCoords[0]);
				triangles[i].vertices[4] = new Vector3f(triangles[i].vertices[1]);
				triangles[i].normals[4] = new Vector3f(triangles[i].normals[1]);
				triangles[i].texCoords[4] = new Vector3f(triangles[i].texCoords[1]);
				triangles[i].vertices[5] = new Vector3f(triangles[i].vertices[2]);
				triangles[i].normals[5] = new Vector3f(triangles[i].normals[2]);
				triangles[i].texCoords[5] = new Vector3f(triangles[i].texCoords[2]);
				Vector3f dCenterA = Vector3fTools.sub(centerOfFace, triangles[i].vertices[0]);
				Vector3f dCenterB = Vector3fTools.sub(centerOfFace, triangles[i].vertices[1]);
				Vector3f dCenterC = Vector3fTools.sub(centerOfFace, triangles[i].vertices[2]);
				dCenterA.normalize();
				dCenterB.normalize();
				dCenterC.normalize();
				dCenterA.scale(extrudesize / 2);
				dCenterB.scale(extrudesize / 2);
				dCenterC.scale(extrudesize / 2);
				triangles[i].normals[0].scale(-1);
				triangles[i].normals[1].scale(-1);
				triangles[i].normals[2].scale(-1);
				Vector3f n0 = new Vector3f(triangles[i].normals[0]);
				Vector3f n1 = new Vector3f(triangles[i].normals[1]);
				Vector3f n2 = new Vector3f(triangles[i].normals[2]);
				n0.scale(-extrudesize / 2);
				n1.scale(-extrudesize / 2);
				n2.scale(-extrudesize / 2);
				triangles[i].vertices[0].add(n0);
				triangles[i].vertices[1].add(n1);
				triangles[i].vertices[2].add(n2);
				triangles[i].vertices[0].add(dCenterA);
				triangles[i].vertices[1].add(dCenterB);
				triangles[i].vertices[2].add(dCenterC);
				triangles[i].vertices[3].add(n0);
				triangles[i].vertices[4].add(n1);
				triangles[i].vertices[5].add(n2);
				triangles[i].vertices[3].add(dCenterA);
				triangles[i].vertices[4].add(dCenterB);
				triangles[i].vertices[5].add(dCenterC);
				triangles[i].setScale(1, 1, 1);
				triangles[i].setInitionPos(centerOfFace);
				triangles[i].setInitialScale(new Vector3f(1, 1, 1));
				triangles[i].setInitialQuadRot(new Vector4f(0, 0, 0, 1));
				for(int j = 0; j < triangles[i].faceCount; j++) {
					triangles[i].faces[j] = new Face();
				}
				int[] a = new int[8];
				int[] b = new int[8];
				int[] c = new int[8];
				a[0] = 0;
				b[0] = 1;
				c[0] = 2; //top
				a[1] = 3;
				b[1] = 5;
				c[1] = 4; //bottom
				a[2] = 5;
				b[2] = 0;
				c[2] = 2; //sideA1
				a[3] = 5;
				b[3] = 3;
				c[3] = 0; //sideA2
				a[4] = 3;
				b[4] = 4;
				c[4] = 0; //sideB1
				a[5] = 0;
				b[5] = 4;
				c[5] = 1; //sideB2
				a[6] = 2;
				b[6] = 4;
				c[6] = 5; //sideC1
				a[7] = 2;
				b[7] = 1;
				c[7] = 4; //sideC2
				for(int j = 0; j < triangles[i].faceCount; j++) {
					triangles[i].faces[j].setVertIndex(c[j], b[j], a[j]);
					triangles[i].faces[j].setNormalIndex(c[j], b[j], a[j]);
					triangles[i].faces[j].setTexCoordIndex(c[j], b[j], a[j]);
				}
				triangles[i].setBoundingBox(calculateBoundingBox(triangles[i]));
				triangles[i].setPivot(centerOfFace);
				//				for(int n = 0; n < triangles[i].faces.length; n++){
				//					System.err.println(triangles[i].faces[n].getVertString(triangles[i]));
				//				}
			}
		} catch(SAXException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		} catch(ParserConfigurationException e) {
			e.printStackTrace();
		}
		return triangles;
	}

	/**
	 * Load obj.
	 *
	 * @param path the path
	 *
	 * @return the mesh
	 * @throws FileNotFoundException the file not found exception
	 */
	public static Mesh loadObj(String path) throws FileNotFoundException {
		Mesh mesh = new Mesh();
		mesh.setName(path);
		int vc = 0;
		int nc = 0;
		int tc = 0;
		int fc = 0;
		File f = new FileExt(path);
		if(!f.exists()) {
			throw new FileNotFoundException(f.getPath());
		}
		// System.err.println("starting to read: " + f.getAbsolutePath());
		BufferedReader br = new BufferedReader(new FileReader(f));
		try {
			int lineCount = 0;
			while(br.ready()) {
				String line = br.readLine();
				if(line.contains("vn ")) {
					++mesh.normalCount;
				}
				if(line.contains("vt ")) {
					mesh.texCoordCount = mesh.texCoordCount + 1;
				}
				if(line.contains("clazz ")) {
					mesh.setVertCount(mesh.getVertCount() + 1);
				}
				if(line.contains("f ")) {
					mesh.faceCount = mesh.faceCount + 1;
				}
				lineCount++;
			}
			// System.err.println("vertices : " + mesh.vertCount);
			// System.err.println("normals  : " + mesh.normalCount);
			// System.err.println("texCoords: " + mesh.texCoordCount);
			// Logger.println("faces    : " + mesh.getFaceCount());
			// System.err.println("in " + lineCount + " lines");
			mesh.normals = new Vector3f[mesh.normalCount];
			mesh.texCoords = new Vector3f[mesh.texCoordCount];
			mesh.vertices = new Vector3f[mesh.getVertCount()];
			mesh.faces = new Face[mesh.faceCount];
			for(int i = 0; i < mesh.faces.length; i++) {
				mesh.faces[i] = new Face();
			}
			if(br != null) {
				br.close();
			}
			br = new BufferedReader(new FileReader(f));
			while(br.ready()) {
				String line = br.readLine();
				if(line.contains("vn ")) {
					String parts[] = line.split("[\\s]+"); // split at
					// whitespaces
					mesh.normals[nc] = new Vector3f(Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3]));
					nc++;
				}
				if(line.contains("vt ")) {
					String parts[] = line.split("[\\s]+");// split at
					// whitespaces
					mesh.texCoords[tc] = new Vector3f(Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3]));
					tc++;
				}
				if(line.contains("clazz ")) {
					String parts[] = line.split("[\\s]+");// split at
					// whitespaces
					mesh.vertices[vc] = new Vector3f(Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3]));
					vc++;
				}
				if(line.contains("f ")) {
					String parts[] = line.split("[\\s]+");
					// System.err.println("reading " + parts.length + " faces");
					int vertI = 0;
					int texI = 1;
					int normI = 2;
					// v0/t0/n0 v1/t1/n1 v2/t2/n2
					// start at 1 because first split elem is 'f'
					mesh.indicedNormals = true;
					for(int faceCoord = 1; faceCoord < parts.length; faceCoord++) {
						// System.err.println(parts[vertNormTex]);
						String xyz[] = parts[faceCoord].split("/");
						mesh.faces[fc].m_vertsIndex[faceCoord - 1] = Integer.parseInt(xyz[vertI]) - 1;
						// ------------------
						mesh.faces[fc].m_texCoordsIndex[faceCoord - 1] = Integer.parseInt(xyz[texI]) - 1;
						// ------------------
						mesh.faces[fc].m_normalIndex[faceCoord - 1] = Integer.parseInt(xyz[normI]) - 1;
					}
					fc++;
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		return mesh;
	}

	/**
	 * The main method.
	 *
	 * @param a the arguments
	 */
	public static void main(String[] a) {
		try {
			loadObj(DataUtil.dataPath + "test.obj");
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void setMaterialToAllChildrenRecusivly(AbstractSceneNode n, Material mat) {
		n.setMaterial(mat);
		for(AbstractSceneNode c : n.getChilds()) {
			setMaterialToAllChildrenRecusivly(c, mat);
		}
	}

	/**
	 * Split triangles.
	 *
	 * @param orig the orig
	 *
	 * @return the world
	 */
	public static MeshGroup splitTriangles(Mesh orig) {
		MeshGroup w = new MeshGroup();
		int lvlOfDetail = 1;
		float sizeThreshold = 30;
		if(orig instanceof Mesh) {
			addTrianglesRecursive(orig, w, lvlOfDetail, sizeThreshold);
		}
		w.setBoundingBox(w.makeBiggestBoundingBox(new Vector3f(), new Vector3f()));
		return w;
	}

	//	/**
	//	 * Apply physics.
	//	 *
	//	 * @param map the map
	//	 * @param n the n
	//	 */
	//	public void applyPhysics(HashMap<Mesh , float[]> map, NetworkEntity n){
	//		if(map.containsKey(this)){
	//			setCollisionObject(true);
	//			setPhysicsMat(map.get(this));
	//		}
	//		for(int i = 0; i < getChilds().size(); i++){
	//			AbstractSceneNode form = getChilds().get(i);
	//			if(form instanceof Mesh){
	//				((Mesh)form).applyPhysics(map, n);
	//			}
	//		}
	//	}
	//
	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.AbstractSceneNode#transform(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	//	@Override
	//	public void transform() {
	//		if (isCollisionObject()) {
	////			if(getPhysicsMat()[15] == 0){
	////				System.err.println("[MESH] physics warning. invalid matrix: "+getName()+": "+Arrays.toString(getPhysicsMat()));
	////			}
	//			GL11.glMultMatrix((float[])getPhysicsMat(), 0);
	//		}
	//		else {
	//			super.transform();
	//		}
	//
	//	}
	public static void arrayStaticVBODraw(Collection<? extends SimplePosElement> parts, Mesh mesh, boolean culling) {
		if(mesh.firstDraw) {
			mesh.onInit();
			mesh.firstDraw = false;
		}
		//		if (EngineSettings.G_WIREFRAMED.isOn() && !mesh.drawingWireframe) {
		//			mesh.drawPolygonWireframe();
		//			return;
		//		}
		//
		;
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GlUtil.glColor4f(0.9f, 0.9f, 0.9f, 1);
		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		mesh.material.attach(0);
		//		if(EngineSettings.G_CULLING_ACTIVE.isOn()){
		//			mesh.setCullingActivated(culling);
		////			System.err.println("activated "+mesh.isCullingActivated()+" "+mesh.getParent()+" "+mesh.getParent().getClass().getSimpleName());
		//			mesh.activateCulling();
		//		}
		switch(mesh.type) {
			case (Mesh.TYPE_VERTEX_BUFFER_OBJ) -> {
				// Enable Pointers
				GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY); // Enable
				// Vertex
				// Arrays
				// Enable TextureNew Coord Arrays
				GlUtil.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
				GlUtil.glEnableClientState(GL11.GL_NORMAL_ARRAY);
				// Set Pointers To Our Data
				GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, mesh.VBOVertices.get(0));
				// Set The Vertex Pointer To The Vertex Buffer
				GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);
				GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, mesh.VBOTexCoords.get(0));
				// Set The TexCoord Pointer To The TexCoord Buffer
				GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);
				if(mesh.material.isMaterialBumpMapped()) {
					//in case of normal maps
					GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
					// Bind Buffer to the Tex Coord Array
					GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, mesh.VBOTexCoords.get(0));
					// Set The TexCoord Pointer To The TexCoord Buffer
					GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);
				}
				if(mesh.material.isSpecularMapped()) {
					//in case of specular maps
					GlUtil.glActiveTexture(GL13.GL_TEXTURE2);
					// Bind Buffer to the Tex Coord Array
					GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, mesh.VBOTexCoords.get(0));
					// Set The TexCoord Pointer To The TexCoord Buffer
					GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);
				}
				GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, mesh.VBONormals.get(0));
				// Set The Normal Pointer To The TexCoord Buffer
				GL11.glNormalPointer(GL11.GL_FLOAT, 0, 0);
				// Render
				// Draw All Of The Triangles At Once
				int vertCount = mesh.faceCount * 3;
				mesh.collisionObject = false;
				for(SimplePosElement nge : parts) {
					//				if(nge != null && Controller.camera.isFormBoundingBoxFrustum(
					//						nge.t[12],
					//						nge.t[13],
					//						nge.t[14],
					//						100)){
					//				System.err.println("drawing transformationArray "+Arrays.toString(nge.t));
					GlUtil.glPushMatrix();
					//					TODO: possible optimization: put all transformations in one array and call by offset (all in memory next to each other)
					//					GL11.glMultMatrix(nge.t, 0);
					GL11.glTranslatef(nge.x, nge.y, nge.z);
					GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertCount);
					GlUtil.glPopMatrix();
					//				}
				}
				// Disable Pointers
				// Disable Vertex Arrays
				GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
				// Disable TextureNew Coord Arrays
				GlUtil.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
				mesh.deactivateCulling();
				GlUtil.glDisableClientState(GL11.GL_NORMAL_ARRAY);
			}
			default -> throw new IllegalArgumentException("this Mesh is no Vertex Buffer Object: " + mesh.toString() + " is " + mesh.type);
		}
		for(AbstractSceneNode child : mesh.getChilds()) {
			child.draw();
			// System.err.println("drawing child: "+child.name);
		}
		//		GL11.glPopAttrib();
		;
		mesh.deactivateCulling();
		mesh.material.detach();
		// if(isBoned()){
		// skeleton.getRootBone().draw();
		// }
	}

	/**
	 * Static vbo multidraw.
	 *
	 * @param staticMeshes the static meshes
	 * @param mesh         the mesh
	 * @param gl           the gl
	 * @param glu          the glu
	 * @param withPhysics
	 *
	 * @ the error diolog exception
	 */
	public static void drawFastVBOInstancedPositionOnly(Collection<? extends Positionable> staticMeshes, Mesh mesh) {
		;
		if(mesh instanceof MeshGroup) {
			mesh = (Mesh) ((MeshGroup) mesh).getChilds().iterator().next();
		}
		if(mesh.isInvisible() || staticMeshes.isEmpty()) {
			return;
		}
		//		;
		//		if (isBoned() && !isMirrorMode()) {
		//			applySkeleton();
		//		}
		if(mesh.firstDraw) {
			mesh.onInit();
			mesh.firstDraw = false;
		}
		//		if (EngineSettings.G_WIREFRAMED.isOn() && !mesh.drawingWireframe) {
		//			mesh.drawPolygonWireframe();
		//			return;
		//		}
		;
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GlUtil.glColor4f(1f, 1f, 1f, 1);
		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		// GL11.glFrontFace(GL11.GL_);
		// System.err.println(material.getName());
		// if(drawingWireframe){
		// GlUtil.glDisable(GL11.GL_BLEND);
		// GlUtil.glDisable(GL11.GL_LIGHTING);
		// GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		// GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		// GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		// GlUtil.glColor4f(1,1,1,1);
		// }
		// else{
		mesh.material.attach(0);
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		// }
		switch(mesh.type) {
			case (Mesh.TYPE_VERTEX_BUFFER_OBJ) -> {
				// Enable Pointers
				GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY); // Enable
				// Vertex
				// Arrays
				// Enable TextureNew Coord Arrays
				GlUtil.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
				GlUtil.glEnableClientState(GL11.GL_NORMAL_ARRAY);
				// Set Pointers To Our Data
				GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, mesh.VBOVertices.get(0));
				// Set The Vertex Pointer To The Vertex Buffer
				GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);
				GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, mesh.VBOTexCoords.get(0));
				// Set The TexCoord Pointer To The TexCoord Buffer
				GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);
				if(mesh.material.isMaterialBumpMapped()) {
					//in case of normal maps
					GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
					// Bind Buffer to the Tex Coord Array
					GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, mesh.VBOTexCoords.get(0));
					// Set The TexCoord Pointer To The TexCoord Buffer
					GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);
				}
				if(mesh.material.isSpecularMapped()) {
					//in case of specular maps
					GlUtil.glActiveTexture(GL13.GL_TEXTURE2);
					// Bind Buffer to the Tex Coord Array
					GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, mesh.VBOTexCoords.get(0));
					// Set The TexCoord Pointer To The TexCoord Buffer
					GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);
				}
				GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, mesh.VBONormals.get(0));
				// Set The Normal Pointer To The TexCoord Buffer
				GL11.glNormalPointer(GL11.GL_FLOAT, 0, 0);
				// Render
				// Draw All Of The Triangles At Once
				mesh.activateCulling();
				GlUtil.glPushMatrix();
				Vector3f old = new Vector3f();
				Vector3f current = new Vector3f();
				for(Positionable nge : staticMeshes) {
					current.set(nge.getPos());
					current.sub(old);
					GL11.glTranslatef(current.x, current.y, current.z);
					GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, mesh.faceCount * 3);
					old.set(nge.getPos());
				}
				GlUtil.glPopMatrix();
				// Disable Pointers
				// Disable Vertex Arrays
				GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
				// Disable TextureNew Coord Arrays
				GlUtil.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
				GlUtil.glDisableClientState(GL11.GL_NORMAL_ARRAY);
			}
			default -> throw new IllegalArgumentException("this Mesh is no Vertex Buffer Object " + mesh.toString() + ": " + mesh.type);
		}
		for(AbstractSceneNode child : mesh.getChilds()) {
			child.draw();
			// System.err.println("drawing child: "+child.name);
		}
		//		GL11.glPopAttrib();
		;
		mesh.deactivateCulling();
		mesh.material.detach();
		// if(isBoned()){
		// skeleton.getRootBone().draw();
		// }
	}

	/**
	 * Static vbo multidraw.
	 *
	 * @param staticMeshes the static meshes
	 * @param mesh         the mesh
	 * @param gl           the gl
	 * @param glu          the glu
	 *
	 * @ the error diolog exception
	 */
	public static void staticVBOMultidraw(HashSet<Physical> staticMeshes, Mesh mesh) {
		;
		if(mesh.isInvisible() || staticMeshes.isEmpty()) {
			return;
		}
		//		;
		//		if (isBoned() && !isMirrorMode()) {
		//			applySkeleton();
		//		}
		if(mesh.firstDraw) {
			mesh.onInit();
			mesh.firstDraw = false;
		}
		//		if (EngineSettings.G_WIREFRAMED.isOn() && !mesh.drawingWireframe) {
		//			mesh.drawPolygonWireframe();
		//			return;
		//		}
		;
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GlUtil.glColor4f(1f, 1f, 1f, 1);
		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		// GL11.glFrontFace(GL11.GL_);
		// System.err.println(material.getName());
		// if(drawingWireframe){
		// GlUtil.glDisable(GL11.GL_BLEND);
		// GlUtil.glDisable(GL11.GL_LIGHTING);
		// GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		// GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		// GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		// GlUtil.glColor4f(1,1,1,1);
		// }
		// else{
		mesh.material.attach(0);
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		// }
		switch(mesh.type) {
			case (Mesh.TYPE_VERTEX_BUFFER_OBJ) -> {
				// Enable Pointers
				GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY); // Enable
				// Vertex
				// Arrays
				// Enable TextureNew Coord Arrays
				GlUtil.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
				GlUtil.glEnableClientState(GL11.GL_NORMAL_ARRAY);
				// Set Pointers To Our Data
				GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, mesh.VBOVertices.get(0));
				// Set The Vertex Pointer To The Vertex Buffer
				GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);
				GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, mesh.VBOTexCoords.get(0));
				// Set The TexCoord Pointer To The TexCoord Buffer
				GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);
				if(mesh.material.isMaterialBumpMapped()) {
					//in case of normal maps
					GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
					// Bind Buffer to the Tex Coord Array
					GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, mesh.VBOTexCoords.get(0));
					// Set The TexCoord Pointer To The TexCoord Buffer
					GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);
				}
				if(mesh.material.isSpecularMapped()) {
					//in case of specular maps
					GlUtil.glActiveTexture(GL13.GL_TEXTURE2);
					// Bind Buffer to the Tex Coord Array
					GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, mesh.VBOTexCoords.get(0));
					// Set The TexCoord Pointer To The TexCoord Buffer
					GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);
				}
				GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, mesh.VBONormals.get(0));
				// Set The Normal Pointer To The TexCoord Buffer
				GL11.glNormalPointer(GL11.GL_FLOAT, 0, 0);
				// Render
				// Draw All Of The Triangles At Once
				for(Physical nge : staticMeshes) {
					GlUtil.glPushMatrix();
					//				mesh.setPos(nge.x, nge.y, nge.z);
					//				mesh.setRot(nge.rx, nge.ry, nge.rz);
					//				mesh.setScale(nge.sx, nge.sy, nge.sz);
					mesh.activateCulling();
					//FIXME draw possible siblings of this mesh
					if(nge.getPhysicsDataContainer().isInitialized()) {
						//					System.err.println("size of mats here is "+nge.physicsMat.size());
						mesh.collisionObject = true;
						//					mesh.applyPhysics(nge.getPhysicsDataContainer().physicsMatMap, nge);
						//					nge.transform.setFromOpenGLMatrix(matTmp);
						//					nge.rw = FastMath.sqrt(1.0 + m1.m00 + m1.m11 + m1.m22) / 2.0;
						//					double w4 = (4.0 * w);
						//					x = (m1.m21 - m1.m12) / w4 ;
						//					y = (m1.m02 - m1.m20) / w4 ;
						//					z = (m1.m10 - m1.m01) / w4 ;
						//					nge.rw = (float) (FastMath.sqrt(1.0 + matTmp[0] + matTmp[5] + matTmp[10]) / 2.0);
						//					float w4 = (4.0f * nge.rw);
						//					x = (matTmp[9] - matTmp[6]) / w4 ;
						//					y = (matTmp[2] - matTmp[8]) / w4 ;
						//					z = (matTmp[4] - matTmp[1]) / w4 ;
						//						mesh.setRot(nge.rx, nge.ry, nge.rz);
						//						mesh.setScale(nge.sx, nge.sy, nge.sz);
					} else {
						mesh.collisionObject = false;
					}
					mesh.transform();
					//				AbstractSceneNode.transform(mesh);
					GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, mesh.faceCount * 3);
					GlUtil.glPopMatrix();
				}
				// Disable Pointers
				// Disable Vertex Arrays
				GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
				// Disable TextureNew Coord Arrays
				GlUtil.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
				GlUtil.glDisableClientState(GL11.GL_NORMAL_ARRAY);
			}
			default -> throw new IllegalArgumentException("this Mesh is no Vertex Buffer Oject " + mesh.toString());
		}
		for(AbstractSceneNode child : mesh.getChilds()) {
			child.draw();
			// System.err.println("drawing child: "+child.name);
		}
		//		GL11.glPopAttrib();
		;
		mesh.deactivateCulling();
		mesh.material.detach();
		// if(isBoned()){
		// skeleton.getRootBone().draw();
		// }
	}

	public void loadPhysicsMeeshConvexHull() {
		//		System.err.println("[MESH] loading physics hull for "+this.getName());
		verticesBuffer.rewind();
		int size = verticesBuffer.limit() / 3;
		ObjectArrayList<Vector3f> points = new ObjectArrayList<Vector3f>(size);
		float scale = 0.75f;//0.83f;
		for(int i = 0; i < size; i++) {
			points.add(new Vector3f(verticesBuffer.get() * scale, verticesBuffer.get() * scale, verticesBuffer.get() * scale));
		}
		ConvexHullShape cShape = new ConvexHullShape(points);
		cShape.setMargin(0);
		this.shape = cShape;
		verticesBuffer.rewind();
	}

	public void retainVertices() {
		ObjectArrayList<Vector3f> verticesListInstance = getVerticesListInstanceFromBuffer();
		vertices = verticesListInstance.toArray(new Vector3f[verticesListInstance.size()]);
		vertCount = vertices.length;
	}

	/**
	 * WARNING. this is only available directly after load, since the buffer is otherwise going to be reused
	 *
	 * @return
	 */
	public ObjectArrayList<Vector3f> getVerticesListInstanceFromBuffer() {
		verticesBuffer.rewind();
		int size = verticesBuffer.limit() / 3;
		ObjectArrayList<Vector3f> points = new ObjectArrayList<Vector3f>(size);
		for(int i = 0; i < size; i++) {
			points.add(new Vector3f(verticesBuffer.get(), verticesBuffer.get(), verticesBuffer.get()));
		}
		verticesBuffer.rewind();
		return points;
	}

	public ObjectArrayList<Vector3f> getVerticesListInstance() {
		if(vertices == null) {
			throw new RuntimeException("No Vertices retained. used 'dedicated' physics mesh flag in mainConfig.xml to prevent vertices from being disarded in non graphics memory");
		}
		ObjectArrayList<Vector3f> points = new ObjectArrayList<Vector3f>(vertCount);
		for(int i = 0; i < vertCount; i++) {
			points.add(vertices[i]);
		}
		return points;
	}

	public List<Vector3f> getVertices(List<Vector3f> out) {
		verticesBuffer.rewind();
		int size = verticesBuffer.limit() / 3;
		for(int i = 0; i < size; i++) {
			out.add(new Vector3f(verticesBuffer.get(), verticesBuffer.get(), verticesBuffer.get()));
		}
		verticesBuffer.rewind();
		return out;
	}

	@Override
	public void cleanUp() {
		if(getMaterial() != null) {
			getMaterial().cleanUp();
		}
		if(type == TYPE_VERTEX_BUFFER_OBJ) {
			//			System.out.println("[CLEANUP] [mesh] cleaning up vertex bufferList object for "+this.getName());
			VBOVertices.rewind();
			VBOTexCoords.rewind();
			VBONormals.rewind();
			GL15.glDeleteBuffers(VBOVertices);
			GL15.glDeleteBuffers(VBOTexCoords);
			GL15.glDeleteBuffers(VBONormals);
		}
		for(AbstractSceneNode n : getChilds()) {
			n.cleanUp();
		}
	}

	@Override
	public void draw() {
		if(isInvisible()) {
			return;
		}
		if(EngineSettings.G_WIREFRAMED.isOn()) {
			drawingWireframe = true;
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		}
		//		TimeStatistics.reset("prepare "+description);
		preparedraw();
		//		TimeStatistics.set("prepare "+description);
		switch(type) {
			case (TYPE_VERTEX_BUFFER_OBJ) -> {
				//			TimeStatistics.reset("vbo "+description);
				assert (VBOindex.get(0) != 0);
				drawVBO(true);
			}
			//			TimeStatistics.set("vbo "+description);
			case (TYPE_VERTEX_ARRAY) -> throw new UnsupportedOperationException();
			default -> {
				System.err.println("SOFTWARE " + this);
				softwaredraw();
			}
		}
		for(AbstractSceneNode child : getChilds()) {
			child.draw();
			// System.err.println("drawing child: "+child.name);
		}
		//		TimeStatistics.reset("finish "+description);
		finishdraw();
		//		TimeStatistics.set("finish "+description);
		if(EngineSettings.G_WIREFRAMED.isOn()) {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
			drawingWireframe = false;
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.SceneNode#onInit(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void onInit() {
	}

	public void clearBuffer(int buffertypeIndex) {
		switch(buffertypeIndex) {
			case BUFFERTYPE_Index -> getIndexBuffer().clear();
			case BUFFERTYPE_Position -> verticesBuffer.clear();
			case BUFFERTYPE_TexCoord -> texCoordsBuffer.clear();
			case BUFFERTYPE_Normal -> normalsBuffer.clear();
		}
	}

	@Override
	public AbstractSceneNode clone() {
		return null;
	}

	public void drawVBO() {
		drawVBO(true);
	}

	public void drawVBO(boolean enableClientState) {
		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
			GlUtil.printGlErrorCritical();
		}
		loadVBO(enableClientState);
		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
			GlUtil.printGlErrorCritical();
		}
		renderVBO();
		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
			GlUtil.printGlErrorCritical("DRAW MODE: " + drawMode + "; " + getName() + "; " + (getParent() != null ? getParent().getName() : "No Parent"));
		}
		unloadVBO(enableClientState);
		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
			GlUtil.printGlErrorCritical();
		}
	}

	public void drawVBOAttributed() {
		assert (this.meshPointersLoaded);
		GL11.glDrawElements(drawMode, faceCount * 3, GL11.GL_UNSIGNED_INT, 0);
	}

	public void drawVBOInterleaved() {
		//		System.err.println("draw vertexbuffer of "+this.getName());
		// Enable Pointers
		GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY); // Enable
		// Vertex
		// Arrays
		// Enable TextureNew Coord Arrays
		GlUtil.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GlUtil.glEnableClientState(GL11.GL_NORMAL_ARRAY);
		// Bind Buffer To the Vertex Array
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBOVertices.get(currentBufferIndex));
		int stride = 32;//(3verts * 3normals * 2texCoords)
		// Set The Vertex Pointer To The Vertex Buffer
		GL11.glVertexPointer(3, GL11.GL_FLOAT, stride, 0);
		//		GL11.glNormalPointer(3, GL11.GL_FLOAT, 24, 0);
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		// Bind Buffer to the Tex Coord Array
		// Set The TexCoord Pointer To The TexCoord Buffer
		GL11.glTexCoordPointer(2, GL11.GL_FLOAT, stride, 12);//12 to 20
		if(material.isMaterialBumpMapped()) {
			GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
			GL11.glTexCoordPointer(2, GL11.GL_FLOAT, stride, 12);//12 to 20
		}
		if(material.isSpecularMapped()) {
			GlUtil.glActiveTexture(GL13.GL_TEXTURE2);
			GL11.glTexCoordPointer(2, GL11.GL_FLOAT, stride, 12); //12 to 20
		}
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		// Bind Buffer to the Normal Array
		GL11.glNormalPointer(GL11.GL_FLOAT, stride, 20); //20 to 32
		// Render
		// Draw All Of The Triangles At Once
		//		System.err.println("drawing "+getName()+ ": "+getFaceCount() * 3+",   "+getVBOVertices()[currentBufferIndex]);
		/*
		 * To get the maximum throughput out of VBOs you need to partition your
		 * data into chunks that contain only up to 2^16 vertices and then use
		 * 16 bit indices. The indices themselves need to be stored in VBOs,
		 * too, so that you can render a whole batch with one draw-call without
		 * the need to transfer "anything" (almost) over the bus.
		 *
		 * For optimal speed you should also not draw the WHOLE bufferList with one
		 * drawcall. Instead partition them, so that you draw more than 300
		 * triangles per drawcall, but less than, say, 20000 triangles per call
		 * (more won'transformationArray be possible with only up to 2^16 vertices anyway). That
		 * allows even hardware that seems to ignore the range-parameter in
		 * glDrawRangeElements (hint: ATI), to swap out buffers, that are not
		 * currently in use and thus it can much more efficiently streamBuffer buffers
		 * from RAM.
		 *
		 * Implementing such a renderer is still quite a lot of work and you
		 * need to care for many details. However, it will pay off, since it
		 * will work very well on all hardware and it is THE way to do it, so
		 * you can reuse it in future projects. Also in OpenGL 3.0 there will be
		 * no more display lists, as you know them, and what remains will focus
		 * on small pieces of geometry.
		 *
		 * Try using unsigned byte for colors, instead of floats. If your
		 * slowdown is bandwidth-related, it will improve things.
		 */
		GlUtil.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, VBOindex.get(0));
		GL11.glDrawElements(drawMode, faceCount * 3, GL11.GL_UNSIGNED_INT, 0);
		//		GL11.glDrawElements(GL11.GL_TRIANGLES, getFaceCount() * 3, GL11.GL_UNSIGNED_INT, indexBuffer);
		;
		GlUtil.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		// Disable Pointers
		// Disable Vertex Arrays
		GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		// Disable TextureNew Coord Arrays
		GlUtil.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GlUtil.glDisableClientState(GL11.GL_NORMAL_ARRAY);
	}

	private void finishdraw() {
		GlUtil.glPopMatrix();
		;
		deactivateCulling();
		collisionObject = false; //reset Collision parameter
		material.detach();
	}

	/**
	 * Gets the collision face normal.
	 *
	 * @return the collision face normal
	 */
	public Vector3f getCollisionFaceNormal() {
		return collisionFaceNormal;
	}

	/**
	 * Gets the collision vec.
	 *
	 * @return the collisionVec
	 */
	public Vector3f getCollisionVec() {
		return collisionVec;
	}

	/**
	 * Sets the collision vec.
	 *
	 * @param collisionVec the collisionVec to set
	 */
	public void setCollisionVec(Vector3f collisionVec) {
		this.collisionVec = collisionVec;
	}

	/**
	 * @return the currentBufferIndex
	 */
	public int getCurrentBufferIndex() {
		return currentBufferIndex;
	}

	/**
	 * @param currentBufferIndex the currentBufferIndex to set
	 */
	public void setCurrentBufferIndex(int currentBufferIndex) {
		this.currentBufferIndex = currentBufferIndex;
	}

	/**
	 * @return the drawMode
	 */
	public int getDrawMode() {
		return drawMode;
	}

	/**
	 * @param drawMode the drawMode to set
	 */
	public void setDrawMode(int drawMode) {
		this.drawMode = drawMode;
	}

	/**
	 * Gets the face count.
	 *
	 * @return the face count
	 */
	public int getFaceCount() {
		return faceCount;
	}

	/**
	 * Sets the face count.
	 *
	 * @param faceCount the new face count
	 */
	public void setFaceCount(int faceCount) {
		this.faceCount = faceCount;
	}

	/**
	 * @return the skin
	 */
	public Skin getSkin() {
		return skin;
	}

	/**
	 * @param skin the skin to set
	 */
	public void setSkin(Skin skin) {
		this.skin = skin;
	}

	/**
	 * Gets the tex coord count.
	 *
	 * @return the tex coord count
	 */
	public int getTexCoordCount() {
		return texCoordCount;
	}

	/**
	 * Sets the tex coord count.
	 *
	 * @param texCoordCount the new tex coord count
	 */
	public void setTexCoordCount(int texCoordCount) {
		this.texCoordCount = texCoordCount;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @return the vBONormals
	 */
	public IntBuffer getVBONormals() {
		return VBONormals;
	}

	/**
	 * @param vBONormals the vBONormals to set
	 */
	public void setVBONormals(IntBuffer vBONormals) {
		VBONormals = vBONormals;
	}

	/**
	 * @return the vBOTexCoords
	 */
	public IntBuffer getVBOTexCoords() {
		return VBOTexCoords;
	}

	/**
	 * @param vBOTexCoords the vBOTexCoords to set
	 */
	public void setVBOTexCoords(IntBuffer vBOTexCoords) {
		VBOTexCoords = vBOTexCoords;
	}

	/**
	 * @return the vBOVertices
	 */
	public IntBuffer getVBOVertices() {
		return VBOVertices;
	}
	//	public void loadVBOAndIndex(){
	//		loadVBO();
	//
	//	}

	/**
	 * @param vBOVertices the vBOVertices to set
	 */
	public void setVBOVertices(IntBuffer vBOVertices) {
		VBOVertices = vBOVertices;
	}

	/**
	 * Gets the vertex bone assignments.
	 *
	 * @return the vertex bone assignments
	 */
	public VertexBoneWeight[] getVertexBoneAssignments() {
		return vertexBoneAssignments;
	}

	/**
	 * Sets the vertex bone assignments.
	 *
	 * @param vertexBoneAssignments the new vertex bone assignments
	 */
	public void setVertexBoneAssignments(VertexBoneWeight[] vertexBoneAssignments) {
		this.vertexBoneAssignments = vertexBoneAssignments;
	}

	/**
	 * Checks if is collision object.
	 *
	 * @return true, if is collision object
	 */
	public boolean isCollisionObject() {
		return collisionObject;
	}

	/**
	 * Sets the collision object.
	 *
	 * @param collisionObject the new collision object
	 */
	public void setCollisionObject(boolean collisionObject) {
		this.collisionObject = collisionObject;
	}

	/**
	 * Checks if is first draw.
	 *
	 * @return the firstDraw
	 */
	public boolean isFirstDraw() {
		return firstDraw;
	}

	/**
	 * Sets the first draw.
	 *
	 * @param firstDraw the firstDraw to set
	 */
	public void setFirstDraw(boolean firstDraw) {
		this.firstDraw = firstDraw;
	}

	/**
	 * Checks if is indiced normals.
	 *
	 * @return true, if is indiced normals
	 */
	public boolean isIndicedNormals() {
		return indicedNormals;
	}

	/**
	 * Sets the indiced normals.
	 *
	 * @param indicedNormals the new indiced normals
	 */
	public void setIndicedNormals(boolean indicedNormals) {
		this.indicedNormals = indicedNormals;
	}

	public boolean isPivotCentered() {
		getBoundingBox().getCenter(cTmp);
		cTmp.sub(getPivot());
		return cTmp.length() < 2;
	}

	/**
	 * Checks if is static mesh.
	 *
	 * @return true, if is static mesh
	 */
	public boolean isStaticMesh() {
		return staticMesh;
	}

	/**
	 * Sets the static mesh.
	 *
	 * @param staticMesh the new static mesh
	 */
	public void setStaticMesh(boolean staticMesh) {
		this.staticMesh = staticMesh;
	}

	/**
	 * Checks if is updated.
	 *
	 * @return true, if is updated
	 */
	public boolean isUpdated() {
		return updated;
	}

	/**
	 * Sets the updated.
	 *
	 * @param updated the new updated
	 */
	public void setUpdated(boolean updated) {
		this.updated = updated;
	}

	public void loadMeshPointers() {
		assert (!meshPointersLoaded) : "Type is: " + type;
		assert (type == Mesh.TYPE_VERTEX_BUFFER_OBJ) : "Type is: " + type;
		assert (VBOVertices.get(currentBufferIndex) != 0);
		assert (VBOTexCoords.get(currentBufferIndex) != 0);
		assert (VBONormals.get(currentBufferIndex) != 0);
		assert (VBOindex.get(0) != 0);
		// Enable Pointers
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		// Bind Buffer To the Vertex Array
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBOVertices.get(currentBufferIndex));
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0); // Set The Vertex Pointer To The Vertex Buffer
		// Bind Buffer to the Normal Array
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBONormals.get(currentBufferIndex));
		GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 0, 0); // Set The Vertex Pointer To The Vertex Buffer
		// Bind Buffer to the Tex Coord Array
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBOTexCoords.get(currentBufferIndex));
		GL20.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, 0, 0); // Set The Vertex Pointer To The Vertex Buffer
		// Render
		// Draw All Of The Triangles At Once
		GlUtil.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, VBOindex.get(0));
		this.meshPointersLoaded = true;
	}

	public void loadVBO(boolean enableVertexArray) {
		// Enable Pointers
		// Enable Vertex Arrays
		if(enableVertexArray) {
			GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		}
		assert (VBOVertices.get(currentBufferIndex) != 0);
		// Bind Buffer To the Vertex Array
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBOVertices.get(currentBufferIndex));
		// Set The Vertex Pointer To The Vertex Buffer
		GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);
		if(enableVertexArray) {
			// Enable Normal Arrays
			GlUtil.glEnableClientState(GL11.GL_NORMAL_ARRAY);
		}
		assert (VBONormals.get(currentBufferIndex) != 0);
		// Bind Buffer to the Normal Array
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBONormals.get(currentBufferIndex));
		// Set The Normal Pointer To The TexCoord Buffer
		GL11.glNormalPointer(GL11.GL_FLOAT, 0, 0);
		if(enableVertexArray) {
			// Enable TextureNew Coord Arrays
			GlUtil.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		}
		assert (VBOTexCoords.get(currentBufferIndex) != 0);
		// Bind Buffer to the Tex Coord Array
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBOTexCoords.get(currentBufferIndex));
		// Set The TexCoord Pointer To The TexCoord Buffer
		if(texCoordSetCount > 1) {
			GL11.glTexCoordPointer(2, GL11.GL_FLOAT, texCoordSetCount * 8, currentTexCoordSet * 8);
		} else {
			GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);
		}
		if(hasTangents) {
			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBOTangents.get(currentBufferIndex));
			// Set The Normal Pointer To The TexCoord Buffer
			GL11.glColorPointer(3, GL11.GL_FLOAT, 0, 0);
			if(enableVertexArray) {
				// Enable TextureNew Coord Arrays
				GlUtil.glEnableClientState(GL11.GL_COLOR_ARRAY);
			}
		}
		if(skin != null) {
			skin.loadVBO();
		}
		GlUtil.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, VBOindex.get(0));
		this.vboLoaded = true;
	}

	private void preparedraw() {
		if(isInvisible()) {
			return;
		}
		//		if (isBoned() && !isMirrorMode()) {
		//			applySkeleton();
		//		}
		if(firstDraw) {
			onInit();
			firstDraw = false;
		}
		if(getParent() != null) {
			drawingWireframe = ((Mesh) getParent()).drawingWireframe;
		}
		//		for(AbstractSceneNode f : getChilds()){
		//			if(f instanceof Mesh){
		//				((Mesh)f).setCollisionObject(isCollisionObject());
		//			}
		//		}
		//		if(getParent() != null ){
		//			this.setCullingActivated(getParent().isCullingActivated());
		//		}
		//		this.activateCulling();
		GlUtil.glPushMatrix();
		;
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		//		TimeStatistics.reset("bind0 "+description);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		//		TimeStatistics.set("bind0 "+description);
		GlUtil.glColor4f(1f, 1f, 1f, 1);
		//		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		if(drawingWireframe) {
			//drawing white wireframe
			GlUtil.glDisable(GL11.GL_BLEND);
			GlUtil.glDisable(GL11.GL_LIGHTING);
			GlUtil.glDisable(GL11.GL_TEXTURE_2D);
			GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
			//			GlUtil.glDisable(GL11.GL_DEPTH_TEST);
			GlUtil.glColor4f(1, 1, 1, 1);
		} else {
			//			TimeStatistics.reset("material "+description);
			material.attach(0);
			//			TimeStatistics.set("material "+description);
			GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		}
		//		transform();
	}

	public void renderVBO() {
		// Render
		// Draw All Of The Triangles At Once
		GL11.glDrawElements(drawMode, faceCount * 3, GL11.GL_UNSIGNED_INT, 0);
	}

	public void setBuffer(int buffertypeIndex, int dim, Buffer buffer) {
		switch(buffertypeIndex) {
			case BUFFERTYPE_Index -> setIndexBuffer((IntBuffer) buffer);
			case BUFFERTYPE_Position -> {
				System.err.println("verticesBuffer set");
				verticesBuffer = (FloatBuffer) buffer;
			}
			case BUFFERTYPE_TexCoord -> texCoordsBuffer = (FloatBuffer) buffer;
			case BUFFERTYPE_Normal -> normalsBuffer = (FloatBuffer) buffer;
		}
		buffer.rewind();
		createVertexBuffers(buffertypeIndex, this, buffer.capacity());
	}

	private void softwaredraw() {
		if(skin != null && boneDrawMode) {
			GlUtil.glDisable(GL11.GL_TEXTURE_2D);
			GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		}
		GL11.glBegin(GL11.GL_TRIANGLES);
		{
			// System.err.println("draw mesh: "+this);
			if(faces == null || faces.length < 1) {
				throw new IllegalArgumentException("Mesh " + this.getName() + " has no faces");
			}
			int t1 = 0;
			int t2 = 1;
			int t3 = 2;
			for(int i = 0; i < faces.length; i++) {
				// System.err.println(texCoords[faces[i].m_texCoordsIndex[0]
				// ] +" "+vertices[faces[i].m_vertsIndex[0]]+" "+description);
				// System.err.println(texCoords[faces[i].m_texCoordsIndex[1]
				// ] +" "+vertices[faces[i].m_vertsIndex[1]]+" "+description);
				// System.err.println(texCoords[faces[i].m_texCoordsIndex[2]
				// ] +" "+vertices[faces[i].m_vertsIndex[2]]+" "+description);
				// TEXTURE COORDINATES X1
				if(texCoords != null && faces[i].m_texCoordsIndex != null) {
					// System.err.println( this.name +" has tex");
					GL11.glTexCoord2f(texCoords[faces[i].m_texCoordsIndex[t1]].x, texCoords[faces[i].m_texCoordsIndex[t1]].y);
				}
				if(indicedNormals) {
					GL11.glNormal3f(normals[faces[i].m_normalIndex[0]].x, normals[faces[i].m_normalIndex[0]].y, normals[faces[i].m_normalIndex[0]].z);
				} else {
					GL11.glNormal3f(faces[i].m_normals[0].x, faces[i].m_normals[0].y, faces[i].m_normals[0].z);
				}
				// System.err.println(vertices[faces[i].m_vertsIndex[0]] );
				Vector3f[] vert = vertices;
				//				if (isBoned() && boneDrawMode) {
				//					Vector<VertexBoneWeight> l = vertexWeightMap
				//							.get(faces[i].m_vertsIndex[0]);
				//					Bone b = skeleton.getBones().get(l.get(0).boneIndex);
				//					Vector4f color = b.color;
				//					GlUtil.glColor4f(color.x, color.y, color.z, 1);
				//				}
				GL11.glVertex3f(vert[faces[i].m_vertsIndex[0]].x, vert[faces[i].m_vertsIndex[0]].y, vert[faces[i].m_vertsIndex[0]].z);
				// TEXTURE COORDINATES X2
				if(texCoords != null && faces[i].m_texCoordsIndex != null) {
					GL11.glTexCoord2f(texCoords[faces[i].m_texCoordsIndex[t2]].x, texCoords[faces[i].m_texCoordsIndex[t2]].y);
				}
				if(indicedNormals) {
					GL11.glNormal3f(normals[faces[i].m_normalIndex[1]].x, normals[faces[i].m_normalIndex[1]].y, normals[faces[i].m_normalIndex[1]].z);
				} else {
					GL11.glNormal3f(faces[i].m_normals[1].x, faces[i].m_normals[1].y, faces[i].m_normals[1].z);
				}
				//				if (isBoned() && boneDrawMode) {
				//					Vector<VertexBoneWeight> l = vertexWeightMap
				//							.get(faces[i].m_vertsIndex[1]);
				//					Bone b = skeleton.getBones().get(l.get(0).boneIndex);
				//					Vector4f color = b.color;
				//					GlUtil.glColor4f(color.x, color.y, color.z, 1);
				//				}
				GL11.glVertex3f(vert[faces[i].m_vertsIndex[1]].x, vert[faces[i].m_vertsIndex[1]].y, vert[faces[i].m_vertsIndex[1]].z);
				// TEXTURE COORDINATES X3
				if(texCoords != null && faces[i].m_texCoordsIndex != null) {
					GL11.glTexCoord2f(texCoords[faces[i].m_texCoordsIndex[t3]].x, texCoords[faces[i].m_texCoordsIndex[t3]].y);
				}
				if(indicedNormals) {
					GL11.glNormal3f(normals[faces[i].m_normalIndex[2]].x, normals[faces[i].m_normalIndex[2]].y, normals[faces[i].m_normalIndex[2]].z);
				} else {
					GL11.glNormal3f(faces[i].m_normals[2].x, faces[i].m_normals[2].y, faces[i].m_normals[2].z);
				}
				//				if (isBoned() && boneDrawMode) {
				//					Vector<VertexBoneWeight> l = vertexWeightMap
				//							.get(faces[i].m_vertsIndex[2]);
				//					Bone b = skeleton.getBones().get(l.get(0).boneIndex);
				//					Vector4f color = b.color;
				//					GlUtil.glColor4f(color.x, color.y, color.z, 1);
				//				}
				GL11.glVertex3f(vert[faces[i].m_vertsIndex[2]].x, vert[faces[i].m_vertsIndex[2]].y, vert[faces[i].m_vertsIndex[2]].z);
			}
		}
		GL11.glEnd();
	}

	/**
	 * Exports the mesh back to an ogre file format xml
	 *
	 * @return the mesh XML-Document in Ogre File Format
	 * @throws ParserConfigurationException
	 */
	public Document toOrgeXML() throws ParserConfigurationException {
		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser = fact.newDocumentBuilder();
		Document doc = parser.newDocument();
		Element meshElement = doc.createElement("mesh");
		doc.appendChild(meshElement);
		Element submeshesElement = doc.createElement("submeshes");
		meshElement.appendChild(submeshesElement);
		Element submeshElement = doc.createElement("submesh");
		submeshElement.setAttribute("material", material.getName());
		submeshElement.setAttribute("usesharedvertices", "false");
		submeshElement.setAttribute("use32bitindexes", "false");
		submeshElement.setAttribute("operationtype", "triangle_list");
		submeshesElement.appendChild(submeshElement);
		Element facesElement = doc.createElement("faces");
		facesElement.setAttribute("count", String.valueOf(this.faceCount));
		submeshElement.appendChild(facesElement);
		for(int i = 0; i < this.faceCount; i++) {
			Element faceElement = doc.createElement("face");
			faceElement.setAttribute("v1", String.valueOf(this.faces[i].m_vertsIndex[0]));
			faceElement.setAttribute("v2", String.valueOf(this.faces[i].m_vertsIndex[1]));
			faceElement.setAttribute("v3", String.valueOf(this.faces[i].m_vertsIndex[2]));
			facesElement.appendChild(faceElement);
		}
		Element geometryElement = doc.createElement("geometry");
		geometryElement.setAttribute("vertexcount", String.valueOf(this.vertCount));
		submeshElement.appendChild(geometryElement);
		Element vertexbufferElement = doc.createElement("vertexbuffer");
		vertexbufferElement.setAttribute("positions", "true");
		vertexbufferElement.setAttribute("normals", "true");
		vertexbufferElement.setAttribute("texture_coord_dimensions_0", "2");
		vertexbufferElement.setAttribute("texture_coords", "1");
		//attach all vertices to the vertexbuffer Element
		for(int i = 0; i < this.vertCount; i++) {
			Element vertexElement = doc.createElement("vertex");
			Element positionElement = doc.createElement("position");
			Element normalElement = doc.createElement("normal");
			Element texcoordElement = doc.createElement("texcoord");
			positionElement.setAttribute("x", String.valueOf(this.vertices[i].x));
			positionElement.setAttribute("y", String.valueOf(this.vertices[i].y));
			positionElement.setAttribute("z", String.valueOf(this.vertices[i].z));
			normalElement.setAttribute("x", String.valueOf(this.normals[i].x));
			normalElement.setAttribute("y", String.valueOf(this.normals[i].y));
			normalElement.setAttribute("z", String.valueOf(this.normals[i].z));
			texcoordElement.setAttribute("u", String.valueOf(this.texCoords[i].x));
			texcoordElement.setAttribute("clazz", String.valueOf(this.texCoords[i].y));
			vertexElement.appendChild(positionElement);
			vertexElement.appendChild(normalElement);
			vertexElement.appendChild(texcoordElement);
			vertexbufferElement.appendChild(vertexElement);
		}
		//attach zje vertex bufferList element to the geometry element
		geometryElement.appendChild(vertexbufferElement);
		//append the submesh names to the root
		Element submeshnamesElement = doc.createElement("submeshnames");
		meshElement.appendChild(submeshnamesElement);
		Element submeshnameElement = doc.createElement("submeshname");
		submeshnameElement.setAttribute("description", "submesh0");
		submeshnameElement.setAttribute("index", "0");
		submeshnamesElement.appendChild(submeshnameElement);
		return doc;
	}

	public void unbindBuffers() {
		GlUtil.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}

	public void unloadMeshPointers() {
		//unbind buffers
		unbindBuffers();
		// Disable Pointers
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		this.meshPointersLoaded = false;
	}

	public void unloadVBO(boolean enableClientState) {
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		if(enableClientState) {
			// Disable Pointers
			// Disable Vertex Arrays
			GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
			// Disable TextureNew Coord Arrays
			GlUtil.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
			GlUtil.glDisableClientState(GL11.GL_NORMAL_ARRAY);
			GlUtil.glDisableClientState(GL11.GL_COLOR_ARRAY);
		}
		if(skin != null) {
			skin.unloadVBO();
		}
		GlUtil.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		this.vboLoaded = false;
	}

	public void updateBound() {
		//		System.err.println("bound update not implemented yet");
		float maxX = Integer.MIN_VALUE;
		float minX = Integer.MAX_VALUE;
		float maxY = Integer.MIN_VALUE;
		float minY = Integer.MAX_VALUE;
		float maxZ = Integer.MIN_VALUE;
		float minZ = Integer.MAX_VALUE;
		if(vertices != null) {
			for(int i = 0; i < vertices.length; i++) {
				maxX = (vertices[i].x > maxX) ? vertices[i].x : maxX;
				minX = (vertices[i].x < minX) ? vertices[i].x : minX;
				maxY = (vertices[i].y > maxY) ? vertices[i].y : maxY;
				minY = (vertices[i].y < minY) ? vertices[i].y : minY;
				maxZ = (vertices[i].z > maxZ) ? vertices[i].z : maxZ;
				minZ = (vertices[i].z < minZ) ? vertices[i].z : minZ;
			}
		}
		if(verticesBuffer != null) {
			while(verticesBuffer.hasRemaining()) {
				float x = verticesBuffer.get();
				float y = verticesBuffer.get();
				float z = verticesBuffer.get();
				maxX = (x > maxX) ? x : maxX;
				minX = (x < minX) ? x : minX;
				maxY = (y > maxY) ? y : maxY;
				minY = (y < minY) ? y : minY;
				maxZ = (z > maxZ) ? z : maxZ;
				minZ = (z < minZ) ? z : minZ;
			}
			verticesBuffer.rewind();
		}
		this.setBoundingBox(new BoundingBox(new Vector3f(minX, minY, minZ), new Vector3f(maxX, maxY, maxZ)));
	}

	/**
	 * @return the vboLoaded
	 */
	public boolean isVboLoaded() {
		return vboLoaded;
	}

	/**
	 * The Class Face.
	 */
	public static class Face {
		/**
		 * The m_verts index.
		 */
		public int m_vertsIndex[] = new int[3]; // array of indicies that
		// reference the vertex array in
		// the mesh
		/**
		 * array of indicies that reference the normal array in the mesh.
		 */
		public int m_normalIndex[] = new int[3];
		/**
		 * array of indicies that reference the normal if no indices are present.
		 */
		public Vector3f m_normals[];// = new Vector3f[3];
		/**
		 * The m_tex coords index.
		 */
		public int m_texCoordsIndex[] = new int[3];

		/**
		 * Gets the centroid.
		 *
		 * @param m the m
		 *
		 * @return the centroid
		 */
		public Vector3f getCentroid(Mesh m) {
			Vector3f a = m.vertices[m_vertsIndex[0]];
			Vector3f b = m.vertices[m_vertsIndex[1]];
			Vector3f c = m.vertices[m_vertsIndex[2]];
			Vector3f centroid = new Vector3f((1f / 3f) * (a.x + b.x + c.x), (1f / 3f) * (a.y + b.y + c.y), (1f / 3f) * (a.z + b.z + c.z));
			return centroid;
		}

		/**
		 * Gets the vert string.
		 *
		 * @param m the m
		 *
		 * @return the vert string
		 */
		public String getVertString(Mesh m) {
			return "[Face] " + m.vertices[m_vertsIndex[0]] + ", " + m.vertices[m_vertsIndex[1]] + ", " + m.vertices[m_vertsIndex[2]];
		}

		/**
		 * Sets the normal index.
		 *
		 * @param a the a
		 * @param b the b
		 * @param c the c
		 */
		public void setNormalIndex(int a, int b, int c) {
			m_normalIndex[0] = a;
			m_normalIndex[1] = b;
			m_normalIndex[2] = c;
		}

		/**
		 * Sets the tex coord index.
		 *
		 * @param a the a
		 * @param b the b
		 * @param c the c
		 */
		public void setTexCoordIndex(int a, int b, int c) {
			m_texCoordsIndex[0] = a;
			m_texCoordsIndex[1] = b;
			m_texCoordsIndex[2] = c;
		}

		/**
		 * Sets the vert index.
		 *
		 * @param a the a
		 * @param b the b
		 * @param c the c
		 */
		public void setVertIndex(int a, int b, int c) {
			m_vertsIndex[0] = a;
			m_vertsIndex[1] = b;
			m_vertsIndex[2] = c;
		}
	}

	public Transform getInitialTransformWithoutScale(Transform out) {
		out.setIdentity();
		out.basis.set(getInitialQuadRot());
		out.origin.set(getInitionPos());
		return out;
	}
}
