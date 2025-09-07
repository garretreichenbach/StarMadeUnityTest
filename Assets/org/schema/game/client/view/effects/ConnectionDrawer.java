package org.schema.game.client.view.effects;

import java.nio.FloatBuffer;
import java.util.Map;
import java.util.Map.Entry;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.schema.common.FastMath;
import org.schema.common.util.ByteUtil;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.util.FastCopyLongOpenHashSet;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.texture.Material;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

public class ConnectionDrawer implements Drawable {

	static int maxVerts = 1024;
	private static Vector3f[] tubeVerticesA = new Vector3f[8];
	private static Vector3f[] tubeVerticesNormal = new Vector3f[8];
	private static Vector3f[] tubeVerticesB = new Vector3f[8];
	private static Vector3f[] tubeTexCoord = new Vector3f[8];
	private static Vector3f[] tubeVerticesAPre = new Vector3f[8];
	private static Vector3f[] tubeVerticesNormalPre = new Vector3f[8];
	private static Vector3f[] tubeVerticesBPre = new Vector3f[8];
	private static Vector3f[] tubeTexCoordPre = new Vector3f[8];

	static {
		for (int i = 0; i < 8; i++) {
			tubeVerticesA[i] = new Vector3f();
			tubeVerticesB[i] = new Vector3f();
			tubeVerticesNormal[i] = new Vector3f();
			tubeTexCoord[i] = new Vector3f();

			tubeVerticesAPre[i] = new Vector3f();
			tubeVerticesBPre[i] = new Vector3f();
			tubeVerticesNormalPre[i] = new Vector3f();
			tubeTexCoordPre[i] = new Vector3f();
		}
	}

	static {
		Vector3f forward = new Vector3f();
		Vector3f right = new Vector3f();
		Vector3f up = new Vector3f();
		AxisAngle4f aa = new AxisAngle4f();
		Matrix3f m = new Matrix3f();
		forward.set(0, 0, 1);
		right.set(1, 0, 0);
		up.set(0, 1, 0);

		right.scale(0.1f);

		float sec = FastMath.PI / 4;
		float rot = 0;
		float texCoordY = 0;
		float p = 1.0f / 8.0f;

		for (int i = 0; i < 8; i++) {

			aa.set(forward, rot);
			m.set(aa);
			tubeVerticesAPre[i].set(0, 0.1f, 0);

			m.transform(tubeVerticesAPre[i]);

			tubeVerticesNormalPre[i].set(tubeVerticesAPre[i]);

			tubeVerticesBPre[i].set(tubeVerticesAPre[i]);

			tubeTexCoordPre[i].set(0, texCoordY, 0);
			texCoordY += p;
			rot += sec;
		}
	}

	private final SegmentController c;
	private final Long2ObjectOpenHashMap<LongOpenHashSet> connections = new Long2ObjectOpenHashMap<LongOpenHashSet>();
	private int VBOIndex;
	private Vector3f start = new Vector3f();
	private Vector3f end = new Vector3f();
	private Vector3f dist = new Vector3f();
	private Vector3f forward = new Vector3f();
	private Vector3f up = new Vector3f(0, 1, 0);
	private Vector3f right = new Vector3f();
	private Matrix3f m = new Matrix3f();
	private AxisAngle4f aa = new AxisAngle4f();
	private int vertices;
	private boolean flagUpdate = true;
	private boolean noDraw;
	private int VBONormalIndex;
	private int VBOTexCoordsIndex;
	private Material material;
	private long failed = -1;
	private boolean conOn;
	private boolean conLohOn;
	private final Vector4f color = new Vector4f(1,1,1,1);

	public ConnectionDrawer(SegmentController c) {
		super();
		this.c = c;
		
		conOn = EngineSettings.G_DRAW_ANY_CONNECTIONS.isOn();
	}

	public void buildMesh() {
		long t0 = System.currentTimeMillis();
		long t = System.currentTimeMillis();
		int connectionCount = 0;
		for (it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<LongOpenHashSet> e : connections.long2ObjectEntrySet()) {
			for (long ep : e.getValue()) {
				int x = Math.abs(ElementCollection.getPosX(e.getLongKey()) - ElementCollection.getPosX(ep));
				int y = Math.abs(ElementCollection.getPosY(e.getLongKey()) - ElementCollection.getPosY(ep));
				int z = Math.abs(ElementCollection.getPosZ(e.getLongKey()) - ElementCollection.getPosZ(ep));
				short type = (short) ElementCollection.getType(ep);
				if (x + y + z > 1 && ElementKeyMap.isValidType(type) && ElementKeyMap.getInfo(type).drawConnection()) {
					connectionCount++;
				}
			}
		}
		long tookInitial = System.currentTimeMillis() - t;
		noDraw = connectionCount == 0;
		if (noDraw) {
			//			System.err.println("[CONNECTIONDRAWER] NOTHING TO DRAW");
			return;
		}
		//		System.err.println("[CONNECTIONDRAWER] UPDATING CONNECTIONS: "+connectionCount);
		vertices = connectionCount * 2 * 2 * 8;
		t = System.currentTimeMillis();
		int bytes = vertices * 3 * ByteUtil.SIZEOF_FLOAT;
		while (maxVerts < bytes) {
			maxVerts *= 2;
		}
		FloatBuffer buffer = GlUtil.getDynamicByteBuffer(maxVerts, 0).asFloatBuffer();
		FloatBuffer normalBuffer = GlUtil.getDynamicByteBuffer(maxVerts, 1).asFloatBuffer();
		FloatBuffer texCoordBuffer = GlUtil.getDynamicByteBuffer(maxVerts, 2).asFloatBuffer();
		long tookFetchBuffers = System.currentTimeMillis() - t;
		t = System.currentTimeMillis();
		int max = 1;
		int index = 1;
		float pMod = 0;
		int actualVerts = 0;
		for (it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<LongOpenHashSet> e : connections.long2ObjectEntrySet()) {
			start.set(pMod - SegmentData.SEG_HALF + ElementCollection.getPosX(e.getKey()), pMod - SegmentData.SEG_HALF + ElementCollection.getPosY(e.getKey()), pMod - SegmentData.SEG_HALF + ElementCollection.getPosZ(e.getKey()));
			//			c.getWorldTransformClient().transform(start);
			for (long ep : e.getValue()) {
				int x = Math.abs(ElementCollection.getPosX(e.getLongKey()) - ElementCollection.getPosX(ep));
				int y = Math.abs(ElementCollection.getPosY(e.getLongKey()) - ElementCollection.getPosY(ep));
				int z = Math.abs(ElementCollection.getPosZ(e.getLongKey()) - ElementCollection.getPosZ(ep));
				short type = (short) ElementCollection.getType(ep);
				if (x + y + z > 1 && ElementKeyMap.isValidType(type) && ElementKeyMap.getInfo(type).drawConnection()) {
					end.set(pMod - SegmentData.SEG_HALF + ElementCollection.getPosX(ep), pMod - SegmentData.SEG_HALF + ElementCollection.getPosY(ep), pMod - SegmentData.SEG_HALF + ElementCollection.getPosZ(ep));
					//					c.getWorldTransformClient().transform(end);
					actualVerts += putStraightSection(start, end, buffer, normalBuffer, texCoordBuffer, index, max);
				}
			}
		}
		long tookVerInser = System.currentTimeMillis() - t;
		t = System.currentTimeMillis();
		assert (vertices == actualVerts) : vertices + "/" + actualVerts;

		if (VBOIndex == 0) {
			VBOIndex = GL15.glGenBuffers();
			Controller.loadedVBOBuffers.add(VBOIndex);
			
			VBONormalIndex = GL15.glGenBuffers();
			Controller.loadedVBOBuffers.add(VBONormalIndex);

			VBOTexCoordsIndex = GL15.glGenBuffers();
			Controller.loadedVBOBuffers.add(VBOTexCoordsIndex);
		}
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBOIndex); // Bind
		//			System.err.println("vertex bufferList bound ");
		// Load The Data
		buffer.flip();
		//		System.err.println("BUFFER LIMIT "+buffer.limit()+" / "+vertices*3*4+" ("+vertices+")");
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
		//			System.err.println("vertex bufferList buffered");

		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBONormalIndex); // Bind
		normalBuffer.flip();
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, normalBuffer, GL15.GL_STATIC_DRAW);

		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBOTexCoordsIndex); // Bind
		texCoordBuffer.flip();
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, texCoordBuffer, GL15.GL_STATIC_DRAW);

		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Bind

		material = new Material();
		material.setShininess(64);
		material.setSpecular(new float[]{0.9f, 0.9f, 0.9f, 1});
		long tookVBO = System.currentTimeMillis() - t;

		long total = System.currentTimeMillis() - t0;

		if (total > 30) {
			System.err.println("CONNECTION BUILDING TOOK " + total + "ms : tookInitial " + tookInitial + "; tookFetchBuffers " + tookFetchBuffers + "; tookVerInser " + tookVerInser + "; tookVBO " + tookVBO);
		}
	}

	@Override
	public void cleanUp() {
		if (VBOIndex != 0) {
			GL15.glDeleteBuffers(VBOIndex);
			GL15.glDeleteBuffers(VBONormalIndex);
			GL15.glDeleteBuffers(VBOTexCoordsIndex);
		}
	}

	@Override
	public void draw() {

		if (failed > 0 && System.currentTimeMillis() - failed > 2500) {
			flagUpdate = true;
			failed = -1;
		}
		if(conOn != EngineSettings.G_DRAW_ANY_CONNECTIONS.isOn()){
			flagUpdate = true;
			conOn = EngineSettings.G_DRAW_ANY_CONNECTIONS.isOn();
		}
		if(conLohOn != EngineSettings.G_DRAW_ALL_CONNECTIONS.isOn()){
			flagUpdate = true;
			conLohOn = EngineSettings.G_DRAW_ALL_CONNECTIONS.isOn();
		}
		if (flagUpdate) {
			updateConnections();
			buildMesh();
			flagUpdate = false;
		}

		if (!noDraw && connections.size() > 0) {

			GlUtil.glPushMatrix();

			GlUtil.glMultMatrix(c.getWorldTransformOnClient());

			GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
			GlUtil.glEnable(GL11.GL_LIGHTING);
			GlUtil.glEnable(GL11.GL_DEPTH_TEST);
			material.attach(0);
			
			GlUtil.glColor4f(color);

			// Enable Pointers
			// Enable Vertex Arrays
			GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY);
			GlUtil.glEnableClientState(GL11.GL_NORMAL_ARRAY);
			GlUtil.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

			// Bind Buffer To the Vertex Array
			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBOTexCoordsIndex);
			// Set The Vertex Pointer To The Vertex Buffer
			GL11.glTexCoordPointer(3, GL11.GL_FLOAT, 0, 0);

			// Bind Buffer To the Vertex Array
			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBONormalIndex);
			// Set The Vertex Pointer To The Vertex Buffer
			GL11.glNormalPointer(GL11.GL_FLOAT, 0, 0);

			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBOIndex);

			GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);

			GL11.glDrawArrays(GL11.GL_QUADS, 0, vertices);

			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

			GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
			GlUtil.glDisableClientState(GL11.GL_NORMAL_ARRAY);
			GlUtil.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
			GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);

			GlUtil.glPopMatrix();
		}

	}

	@Override
	public boolean isInvisible() {
		return false;
	}

	@Override
	public void onInit() {

	}

	public void flagUpdate() {
		flagUpdate = true;
	}

	/**
	 * @return the c
	 */
	public SegmentController getSegmentController() {
		return c;
	}

	private int putStraightSection(Vector3f start, Vector3f end, FloatBuffer buffer, FloatBuffer normalBuffer, FloatBuffer texCoordBuffer, int index, int maxIndex) {
		
		right.set(1, 0, 0);
		up.set(0, 1, 0);
		forward.set(0, 0, 1);
		
		right.scale(0.1f);

		forward.sub(end, start);

		up.cross(right, forward);
		if (up.lengthSquared() == 0) {
			//special case
			up.set(0, 1, 0);
		}
		up.normalize();
		right.cross(up, forward);
		if (right.lengthSquared() == 0) {
			//special case
			right.set(1, 0, 0);
		}
		right.normalize();

		GlUtil.setRightVector(right, m);
		GlUtil.setUpVector(up, m);
		GlUtil.setForwardVector(forward, m);

		for (int i = 0; i < 8; i++) {
			tubeVerticesA[i].set(tubeVerticesAPre[i]);
			tubeVerticesB[i].set(tubeVerticesBPre[i]);

			m.transform(tubeVerticesA[i]);
			m.transform(tubeVerticesB[i]);

			tubeVerticesNormal[i].set(tubeVerticesA[i]);

			tubeTexCoord[i].set(tubeTexCoordPre[i]);

			tubeVerticesA[i].add(start);
			tubeVerticesB[i].add(end);
		}
		assert (maxIndex != 0);
		float percent = (float) index / (float) maxIndex;

		for (int i = 0; i < 8; i++) {
			GlUtil.putPoint3(buffer, tubeVerticesA[i]);
			GlUtil.putPoint3(normalBuffer, tubeVerticesNormal[i]);
			tubeTexCoord[i].x = 0;
			GlUtil.putPoint3(texCoordBuffer, tubeTexCoord[i]);

			GlUtil.putPoint3(buffer, tubeVerticesA[(i + 1) % 8]);
			GlUtil.putPoint3(normalBuffer, tubeVerticesNormal[(i + 1) % 8]);
			tubeTexCoord[(i + 1) % 8].x = 0;
			GlUtil.putPoint3(texCoordBuffer, tubeTexCoord[(i + 1) % 8]);

			GlUtil.putPoint3(buffer, tubeVerticesB[(i + 1) % 8]);
			GlUtil.putPoint3(normalBuffer, tubeVerticesNormal[(i + 1) % 8]);
			tubeTexCoord[(i + 1) % 8].x = 1;
			GlUtil.putPoint3(texCoordBuffer, tubeTexCoord[(i + 1) % 8]);

			GlUtil.putPoint3(buffer, tubeVerticesB[i]);
			GlUtil.putPoint3(normalBuffer, tubeVerticesNormal[i]);
			tubeTexCoord[i].x = 1;
			GlUtil.putPoint3(texCoordBuffer, tubeTexCoord[i]);

		}
		return 32;//8*4;

	}

	public void updateConnections() {
		long time = System.currentTimeMillis();
		connections.clear();
		failed = -1;
		int checks = 0;
		Map<Long, FastCopyLongOpenHashSet> controllingMap = c.getControlElementMap().getControllingMap().getAll();
		for (Entry<Long, FastCopyLongOpenHashSet> e : controllingMap.entrySet()) {
			SegmentPiece p = new SegmentPiece();
			if (e.getValue().size() > 0) {
				SegmentPiece controller = null;
				controller = c.getSegmentBuffer().getPointUnsave(e.getKey(), p);
				if (controller != null) {
					if (ElementKeyMap.isValidType(controller.getType()) && ElementKeyMap.getInfo(controller.getType()).drawConnection()) {
						connections.put(e.getKey(), e.getValue());
					}
				} else {
					failed = System.currentTimeMillis();
				}
			}
		}
		long took = System.currentTimeMillis() - time;
		if (took > 40) {
			System.err.println("[CLIENT] update connections took " + took + "ms; controllers: " + controllingMap.size() + "; checks: " + checks);
		}
	}

	public Vector4f getColor() {
		return color;
	}

}
