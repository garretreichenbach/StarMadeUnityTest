package org.schema.schine.graphicsengine.forms.particle.trail;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.particle.ParticleContainer;
import org.schema.schine.graphicsengine.forms.particle.ParticleController;

import com.bulletphysics.linearmath.Transform;

public class ParticleTrailDrawerVBO implements Drawable {

	public static final float DIST_BETWEEN_SECTIONS = 0.5f;
	private static final int MAX_PARTICLES_DRAWN = 512;
	protected static float[] tCoords = new float[]{0, 0.50f, 0.75f, 0.25f};
	private static FloatBuffer vertexBuffer;
	private static FloatBuffer attribBuffer;
	Vector3f d1 = new Vector3f();
	Vector3f d2 = new Vector3f();
	Vector3f h1 = new Vector3f();
	Vector3f h2 = new Vector3f();
	private ParticleController particleController;
	private IntBuffer vertexVBOName = MemoryUtil.memAllocInt(1);
	private IntBuffer parametersVBOName = MemoryUtil.memAllocInt(1);
	private float spriteSize = 1;
	private Vector3f up = new Vector3f();
	private Vector3f right = new Vector3f();
	private Vector3f forward = new Vector3f();
	private Vector3f posHelper = new Vector3f();
	private Vector3f posHelper2 = new Vector3f();
	private Vector3f axis = new Vector3f();
	private Vector3f lastAxis = new Vector3f();
	private Vector3f correction = new Vector3f();
	private Vector4f posHelperAndTime = new Vector4f();
	private Vector3f[] c = new Vector3f[4];
	private Vector3f[] cor = new Vector3f[4];
	private Vector3f[] cOld = new Vector3f[4];
	private Transform t = new Transform();
	private int vertexCount;


	private Vector4f attribHelper = new Vector4f();

	public ParticleTrailDrawerVBO(ParticleController controller) {
		particleController = controller;

		t.setIdentity();

		for (int i = 0; i < c.length; i++) {
			c[i] = new Vector3f();
			cOld[i] = new Vector3f();
			cor[i] = new Vector3f();
		}
		// t.basis.rotZ(FastMath.PI);
	}

	public ParticleTrailDrawerVBO(ParticleController controller,
	                              float spriteSize) {
		this(controller);
		this.spriteSize = spriteSize;
	}

	@Override
	public void cleanUp() {

	}

	@Override
	public void draw() {

		if (particleController.getParticleCount() > 1) {
			boolean updateBuffers = updateBuffers();

			if (updateBuffers) {
				GlUtil.glPushMatrix();

				GlUtil.glDisable(GL11.GL_CULL_FACE);

				GlUtil.glDisable(GL11.GL_LIGHTING);
				GlUtil.glEnable(GL11.GL_BLEND);
				GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

				drawVBO();

				GlUtil.glDisable(GL11.GL_BLEND);
				GlUtil.glPopMatrix();

				GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
				GlUtil.glEnable(GL11.GL_CULL_FACE);
				GlUtil.glEnable(GL11.GL_LIGHTING);
			}
		}

	}

	@Override
	public boolean isInvisible() {
		return false;
	}

	@Override
	public void onInit() {
		loadVBO();
	}

	private void drawVBO() {
		// Enable Vertex Arrays
		GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		// Enable TextureNew Coord Arrays
		GlUtil.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

		// Bind Buffer To the Vertex Array
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVBOName.get(0));
		// Set The Vertex Pointer To The Vertex Buffer
		GL11.glVertexPointer(4, GL11.GL_FLOAT, 0, 0);

		// Bind Buffer to the Tex Coord Array
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, parametersVBOName.get(0));
		// Set The TexCoord Pointer To The TexCoord Buffer
		GL11.glTexCoordPointer(4, GL11.GL_FLOAT, 0, 0);

		// Render

		// Draw All Of The Quads At Once
		GL11.glDrawArrays(GL11.GL_QUAD_STRIP, 0, vertexCount);

		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		// Disable Pointers
		// Disable Vertex Arrays
		GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		// Disable TextureNew Coord Arrays
		GlUtil.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

	}

	public ParticleController getParticleController() {
		return particleController;
	}

	public void setParticleController(ParticleController particleController) {
		this.particleController = particleController;
	}

	/**
	 * @return the spriteSize
	 */
	public float getSpriteSize() {
		return spriteSize;
	}

	/**
	 * @param spriteSize the spriteSize to set
	 */
	public void setSpriteSize(float spriteSize) {
		this.spriteSize = spriteSize;
	}

	private void handleQuad(int i, int next, ParticleContainer particles, float percent, float absIndex, int size) {
		// don't check for frustum here since missing verts will fuck up the
		// trail
		// TODO add Frustum culling on higher level
		try {
			boolean correct = absIndex > 0;

			particles.getPos(i, posHelper);
			if (absIndex >= size - 2) {
				axis.set(lastAxis);
			} else {
				particles.getPos(next, posHelper2);
				axis.sub(posHelper2, posHelper);
			}

			if (correct) {
				correction.cross(axis, lastAxis);
			}

			GlUtil.billboardAbitraryAxis(posHelper, axis, Controller.getCamera().getPos(), t);


			posHelperAndTime.set(posHelper.x, posHelper.y, posHelper.z,
					particles.getLifetime(i));

			GlUtil.getRibbon(t, DIST_BETWEEN_SECTIONS, c);

			//		DebugDrawer.points.add(new DebugPoint(new Vector3f(cOld[2]), new Vector4f(1,0,0,1), 0.1f));
			//		DebugDrawer.points.add(new DebugPoint(new Vector3f(cOld[3]), new Vector4f(0,0,1,1), 0.1f));
			//		DebugDrawer.points.add(new DebugPoint(new Vector3f(c[0]), new Vector4f(1,1,0,1), 0.1f));
			//		DebugDrawer.points.add(new DebugPoint(new Vector3f(c[1]), new Vector4f(0,1,1,1), 0.1f));

			d1.sub(cOld[3], c[0]);
			d2.sub(cOld[2], c[1]);
			d1.scale(0.5f);
			d2.scale(0.5f);

			h1.add(c[0], d1);
			h2.add(c[1], d2);

			if (absIndex > 0) {
				cor[0].set(cOld[0]);
				cor[1].set(cOld[1]);
				cor[2].set(h2);
				cor[3].set(h1);
				GlUtil.putRibbon2(vertexBuffer, posHelperAndTime.w, cor, vertexBuffer.position() - 8);
			}
			//		DebugDrawer.points.add(new DebugPoint(new Vector3f(h1), new Vector4f(1,1,1,1), 0.3f));
			//		DebugDrawer.points.add(new DebugPoint(new Vector3f(h2), new Vector4f(1,0,1,1), 0.3f));

			if (absIndex > 0) {
				cor[0].set(h1);
				cor[1].set(h2);
				cor[2].set(c[2]);
				cor[3].set(c[3]);
				GlUtil.putRibbon2(vertexBuffer, posHelperAndTime.w, cor, -1);
				vertexCount += 2;
			} else {
				GlUtil.putRibbon2(vertexBuffer, posHelperAndTime.w, c, -1);
				vertexCount += 2;
			}

			for (int g = 0; g < 2; g++) {
				attribHelper.set(0, absIndex * 0.05f, percent, tCoords[g]);
				GlUtil.putPoint4(attribBuffer, attribHelper);

			}
			for (int g = 0; g < 4; g++) {
				if (absIndex > 0) {
					cOld[g].set(cor[g]);
				} else {
					cOld[g].set(c[g]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("[ERROR][TRAILDRAWER] " + e.getClass().getSimpleName() + " " + e.getMessage());
			//			e.printStackTrace();
		}
		lastAxis.set(axis);
	}

	private void initVertexBuffer(int maxParticleCount) {
		if (vertexBuffer == null) {
			// 2 vertices per strip part; for the fist, add 2 more
			vertexBuffer = MemoryUtil.memAllocFloat(maxParticleCount * 4 * 2 + 8);
		} else {
			vertexBuffer.rewind();
		}

		// Generate And Bind The Vertex Buffer
		GL15.glGenBuffers(vertexVBOName); // Get A Valid Name
		Controller.loadedVBOBuffers.add(vertexVBOName.get(0));
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVBOName.get(0)); // Bind
		// Load The Data
//		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, (ByteBuffer)null,
//				GL15.GL_DYNAMIC_DRAW);
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Bind

		if (attribBuffer == null) {
			// quads need 4 attribs
			attribBuffer = MemoryUtil.memAllocFloat(maxParticleCount * 4 * 2 + 8);
		} else {
			attribBuffer.rewind();
		}

		// Generate And Bind The Vertex Buffer
		GL15.glGenBuffers(parametersVBOName); // Get A Valid Name
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, parametersVBOName.get(0)); // Bind
		Controller.loadedVBOBuffers.add(parametersVBOName.get(0));
		// Load The Data
//		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, (ByteBuffer)null,
//				GL15.GL_DYNAMIC_DRAW);
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Bind
	}

	private void loadVBO() {
		initVertexBuffer(MAX_PARTICLES_DRAWN);
	}

	private boolean updateBufferQuads(ParticleContainer particles) {

		// particles * quad(4) * vector(3) * float(4)

		vertexBuffer.rewind();
		attribBuffer.rewind();
		vertexBuffer.limit(vertexBuffer.capacity());
		attribBuffer.limit(attribBuffer.capacity());

		up = GlUtil.getUpVector(up);
		right = GlUtil.getRightVector(right);
		forward = GlUtil.getForwardVector(forward);

		int end = Math.min(MAX_PARTICLES_DRAWN, particleController.getParticleCount());
		int start = particleController.getOffset()
				% particleController.getParticles().getCapacity();
		float p = 0;
		float fac = 1f / end;
		vertexCount = 0;
		{
			int i = start;
			int t = 0;
			while (t < end && i < particleController.getParticles().getCapacity() - 2) {
				int c = i;
				int n = (i + 1);
				handleQuad(c, n, particles, p, t, end);
				p += fac;
				i++;
				t++;
			}
		}

		if (vertexBuffer.position() == 0) {
			// everything is out of frustum
			return false;
		}
		vertexBuffer.flip();
		attribBuffer.flip();

		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVBOName.get(0));
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer,
				GL15.GL_STREAM_DRAW);

		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, parametersVBOName.get(0));
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, attribBuffer,
				GL15.GL_STREAM_DRAW);
		return true;

	}

	private boolean updateBuffers() {

		ParticleContainer particles = particleController.getParticles();

		return updateBufferQuads(particles);
	}

}
