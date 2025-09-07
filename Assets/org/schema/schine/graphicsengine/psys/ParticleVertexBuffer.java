package org.schema.schine.graphicsengine.psys;

import java.nio.FloatBuffer;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Quat4Util;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.psys.modules.RendererModule;

public class ParticleVertexBuffer {
	//(3floatPos, 2FloatTex, 3FloatNormal, 4FloatColor)
	public static final int vertexDataSize = (3 + 2 + 3 + 4);
	private static final Vector2f[] squareVertices = new Vector2f[]{
			new Vector2f(-0.5f, -0.5f),
			new Vector2f(0.5f, -0.5f),
			new Vector2f(0.5f, 0.5f),
			new Vector2f(-0.5f, 0.5f)
	};
	private static final Vector2f[] squareTexCoords = new Vector2f[]{
			new Vector2f(0f, 0f),
			new Vector2f(1f, 0f),
			new Vector2f(1f, 1f),
			new Vector2f(0f, 1f)
	};
	static int maxParticleCount = 1024;
	/*
	 * (count * vertexDataSize) * 4verticesInQuad
	 */
	protected static FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat((maxParticleCount * vertexDataSize) * 4);
	static int currentVBOId;
	private static boolean initialized;
	Vector3f cameraRight_worldspace = new Vector3f();
	Vector3f cameraBack_worldspace = new Vector3f();
	Vector3f cameraUp_worldspace = new Vector3f();
	Vector2f BillboardSize = new Vector2f(1, 1);
	ParticleContainer tmpContainer = new ParticleContainer();
	private Vector4f colorTmp = new Vector4f();
	private Vector3f posTmp = new Vector3f();

	protected int addQuadNormalBillboard(ParticleContainer particles, ParticleSystem sys, RendererModule.FrustumCullingMethod frustumCulling) {

		float[] verts = new float[squareVertices.length * 3];

		for (int i = 0; i < squareVertices.length; i++) {

			float sizeX = squareVertices[i].x * BillboardSize.x * particles.size.x;
			float sizeY = squareVertices[i].y * BillboardSize.y * particles.size.y;
			Vector3f result = Quat4Util.mult(particles.rotation, new Vector3f(sizeX, sizeY, 0.0F), new Vector3f());

			float vertX =
					particles.position.x
							+ cameraRight_worldspace.x * result.x
							+ cameraUp_worldspace.x * result.y;
			float vertY =
					particles.position.y
							+ cameraRight_worldspace.y * result.x
							+ cameraUp_worldspace.y * result.y;
			float vertZ =
					particles.position.z
							+ cameraRight_worldspace.z * result.x
							+ cameraUp_worldspace.z * result.y;

			verts[i * 3] = vertX;
			verts[i * 3 + 1] = vertY;
			verts[i * 3 + 2] = vertZ;
		}

		switch (frustumCulling) {
			case NONE:
				break;
			case SINGLE:
				if (!GlUtil.isPointInView(particles.position, Controller.vis.getVisLen())) {
					return 0;
				}
				break;
			case ACCURATE:
				boolean anyVisible = false;
				for (int i = 0; i < squareVertices.length; i++) {
					posTmp.set(verts[i * 3], verts[i * 3 + 1], verts[i * 3 + 2]);
					if (GlUtil.isPointInView(posTmp, Controller.vis.getVisLen())) {
						anyVisible = true;
						break;
					}
				}
				if (!anyVisible) {
					return 0;
				}
				break;
		}

		for (int i = 0; i < squareVertices.length; i++) {

			vertexBuffer.put(verts[i * 3]);
			vertexBuffer.put(verts[i * 3 + 1]);
			vertexBuffer.put(verts[i * 3 + 2]);

			vertexBuffer.put(squareTexCoords[i].x);
			vertexBuffer.put(squareTexCoords[i].y);

			vertexBuffer.put(cameraBack_worldspace.x);
			vertexBuffer.put(cameraBack_worldspace.y);
			vertexBuffer.put(cameraBack_worldspace.z);

			colorTmp.set(particles.color);

			sys.getColor(colorTmp, particles);

			vertexBuffer.put(colorTmp.x);
			vertexBuffer.put(colorTmp.y);
			vertexBuffer.put(colorTmp.z);
			vertexBuffer.put(colorTmp.w);

		}

		return squareVertices.length; //4
	}

	public void draw(ParticleSystem sys, RendererModule.FrustumCullingMethod frustumCulling) {
		if (!initialized) {
			init();
		}

		int vertexCount = updateBillboards(sys, frustumCulling);

		drawVBO(vertexCount);
	}

	public void init() {

		currentVBOId = GL15.glGenBuffers();

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, currentVBOId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STREAM_DRAW);
		initialized = true;
	}

	private int updateBillboards(ParticleSystem sys, RendererModule.FrustumCullingMethod frustumCulling) {

		int particleCount = ParticleProperty.getParticleCount(sys.getRawParticles());
		if (particleCount > maxParticleCount) {
			maxParticleCount = particleCount;
			vertexBuffer = MemoryUtil.memAllocFloat((maxParticleCount * vertexDataSize) * 4);
			init();
		}

		// particles * quad(4) * vector(3) * float(4)

		vertexBuffer.rewind();
		vertexBuffer.limit(vertexBuffer.capacity());

		//ViewMatrix[0][0], ViewMatrix[1][0], ViewMatrix[2][0]
		cameraRight_worldspace.set(Controller.modelviewMatrix.m00, Controller.modelviewMatrix.m10, Controller.modelviewMatrix.m20);
		//ViewMatrix[0][1], ViewMatrix[1][1], ViewMatrix[2][1]
		cameraUp_worldspace.set(Controller.modelviewMatrix.m01, Controller.modelviewMatrix.m11, Controller.modelviewMatrix.m21);
		//ViewMatrix[0][2], ViewMatrix[1][2], ViewMatrix[2][2]
		cameraBack_worldspace.set(-Controller.modelviewMatrix.m02, -Controller.modelviewMatrix.m12, -Controller.modelviewMatrix.m22);

		int vertexCount = 0;

//		for(int particleIndex = 0; particleIndex < sys.getParticleCount(); particleIndex++){
//			Particle.updateDistance(particleIndex, sys.getRawParticles(), camPos);
//		}

//		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)){
		for (int particleIndex = sys.getParticleCount() - 1; particleIndex >= 0; particleIndex--) {

			Particle.getParticle(particleIndex, sys.getRawParticles(), tmpContainer);

			vertexCount += addQuadNormalBillboard(tmpContainer, sys, frustumCulling);
		}
//		}else{
//			for(int particleIndex = 0; particleIndex < sys.getParticleCount(); particleIndex++){
//				
//				Particle.getParticle(particleIndex, sys.getRawParticles(), tmpContainer);
//				
//				vertexCount += addQuadNormalBillboard(particleIndex, tmpContainer);
//			}
//		}
		if (vertexCount == 0) {
			//nothing to draw
			return 0;
		}
		vertexBuffer.flip();

		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, currentVBOId);
		GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, vertexBuffer);

		return vertexCount;
	}

	private void drawVBO(int vertexCount) {
//		GlUtil.printGlErrorCritical();
		// Enable Vertex Arrays
		GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		// Enable TextureNew Coord Arrays
		GlUtil.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

		GlUtil.glEnableClientState(GL11.GL_NORMAL_ARRAY);

		GlUtil.glEnableClientState(GL11.GL_COLOR_ARRAY);

//		 Bind Buffer To the Vertex Array
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, currentVBOId);

//		GlUtil.printGlErrorCritical();

		// Set The Vertex Pointer To The Vertex Buffer
		GL11.glVertexPointer(3, GL11.GL_FLOAT, vertexDataSize * ByteUtil.SIZEOF_FLOAT, 0);
		GL11.glTexCoordPointer(2, GL11.GL_FLOAT, vertexDataSize * ByteUtil.SIZEOF_FLOAT, 3 * ByteUtil.SIZEOF_FLOAT);
		GL11.glNormalPointer(GL11.GL_FLOAT, vertexDataSize * ByteUtil.SIZEOF_FLOAT, (3 + 2) * ByteUtil.SIZEOF_FLOAT);
		GL11.glColorPointer(4, GL11.GL_FLOAT, vertexDataSize * ByteUtil.SIZEOF_FLOAT, (3 + 2 + 3) * ByteUtil.SIZEOF_FLOAT);

//		GlUtil.printGlErrorCritical();

		// Bind Buffer to the Tex Coord Array

		// Draw All Of The Quads At Once
		GL11.glDrawArrays(GL11.GL_QUADS, 0, vertexCount);

		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		// Disable Pointers
		// Disable Vertex Arrays
		GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		// Disable TextureNew Coord Arrays
		GlUtil.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

		GlUtil.glDisableClientState(GL11.GL_NORMAL_ARRAY);

		GlUtil.glDisableClientState(GL11.GL_COLOR_ARRAY);

//		GlUtil.printGlErrorCritical();

	}
}
