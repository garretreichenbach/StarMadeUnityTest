package org.schema.schine.graphicsengine.forms.particle;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;
import org.schema.common.FastMath;
import org.schema.common.util.ByteUtil;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.GlUtil;

import com.bulletphysics.linearmath.Transform;

public class ParticleLaserDrawerVBO implements Drawable {

	public static final int MODE_POINTS = 1;
	public static final int MODE_QUADS = 0;
	public static final float maxDamage = 1000;
	public static final float minDamage = 0.1f * maxDamage;
	public static final float maxDamageM = 1f / maxDamage;
	private static final int MAX_PARTICLES_DRAWN = 1024*2;
	public static boolean DEBUG = false;
	protected static float[] tCoords = new float[]{0, 0.50f, 0.75f, 0.25f};
	private static FloatBuffer vertexBuffer;
	private static FloatBuffer attribBuffer;
	private static FloatBuffer colorBuffer;
	private ParticleController particleController;
	private int mode;
	private int vertexVBO;
	private int parametersVBO ;
	private int colorVBO;
	private float spriteSize = 1;
	private float beamLength = 5;
	private Vector3f up = new Vector3f();
	private Vector3f right = new Vector3f();
	private Vector3f forward = new Vector3f();
	private Vector3f beamStartPosHelper = new Vector3f();
	private Vector3f startHelper = new Vector3f();
	private Vector3f velocityHelper = new Vector3f();
	private Transform t = new Transform();
	private Vector3f axis = new Vector3f();
	private boolean drawInverse;
	private Vector3f beamEndPosHelper = new Vector3f();
	private Vector4f attribHelper = new Vector4f();
	private Vector3f[] cStart = new Vector3f[4];
	private Vector3f[] cMiddle = new Vector3f[4];
	private Vector3f[] cEnd = new Vector3f[4];
	private boolean blended = true;
	private int floatsDrawn;
	private boolean updateBuffers;
	private Vector4f colorHelper = new Vector4f();
	public ParticleLaserDrawerVBO(ParticleController controller) {
		particleController = controller;

		t.setIdentity();
		//		t.basis.rotZ(FastMath.PI);
	}

	public ParticleLaserDrawerVBO(ParticleController controller, float spriteSize) {
		this(controller);
		this.spriteSize = spriteSize;

		for (int i = 0; i < cStart.length; i++) {
			cStart[i] = new Vector3f();
			cMiddle[i] = new Vector3f();
			cEnd[i] = new Vector3f();
		}
	}

	@Override
	public void cleanUp() {

	}

	@Override
	public void draw() {

		//		AbstractScene.infoList.add("# DParticles: "+particleController.getParticleCount());
		if (particleController.getParticleCount() > 0) {

			if (DEBUG) {
				drawDebugPoint(particleController.getParticles());
			} else {
				if (updateBuffers) {
					GlUtil.glPushMatrix();
					GlUtil.glDisable(GL11.GL_CULL_FACE);
					GlUtil.glDisable(GL11.GL_LIGHTING);
					if (blended) {
						GlUtil.glEnable(GL11.GL_BLEND);
						GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
					}
					drawVBO();
					GlUtil.glDisable(GL11.GL_BLEND);
					GlUtil.glPopMatrix();
					GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
					GlUtil.glEnable(GL11.GL_CULL_FACE);
					GlUtil.glEnable(GL11.GL_LIGHTING);
				}
				updateBuffers = updateBuffers();
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

	private void drawDebugPoint(int i, ParticleContainer particles) {
		particles.getPos(i, beamStartPosHelper);
		GL11.glVertex3f(beamStartPosHelper.x, beamStartPosHelper.y, beamStartPosHelper.z);
	}

	private void drawDebugPoint(ParticleContainer particles) {

		GlUtil.glPushMatrix();
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);

		GlUtil.glColor4f(1, 1, 1, 1);
		GL11.glPointSize(10);

		GL11.glBegin(GL11.GL_POINTS);
		int max = Math.min(MAX_PARTICLES_DRAWN, particleController.getParticleCount());

		System.err.println("DRAWING " + max + " PARTICLES ");
		if (drawInverse) {

			for (int i = max - 1; i >= 0; i--) {
				drawDebugPoint(i, particles);
			}
		} else {
			for (int i = 0; i < max; i++) {
				drawDebugPoint(i, particles);
			}
		}
		GL11.glEnd();

		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);

		GlUtil.glColor4f(1, 1, 1, 1);
		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);

		GlUtil.glPopMatrix();
	}

	private void drawVBO() {
		// Enable Vertex Arrays
		GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		// Enable TextureNew Coord Arrays
		GlUtil.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

		GlUtil.glEnableClientState(GL11.GL_COLOR_ARRAY);

		// Bind Buffer To the Vertex Array
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVBO);

		// Set The Vertex Pointer To The Vertex Buffer
		GL11.glVertexPointer(4, GL11.GL_FLOAT, 0, 0);

		// Bind Buffer to the Tex Coord Array
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, parametersVBO);

		// Set The TexCoord Pointer To The TexCoord Buffer
		GL11.glTexCoordPointer(4, GL11.GL_FLOAT, 0, 0);

		if (mode == MODE_QUADS) {
			// Bind Buffer to the Tex Coord Array
			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorVBO);

			// Set The TexCoord Pointer To The TexCoord Buffer
			GL11.glColorPointer(4, GL11.GL_FLOAT, 0, 0);
		}

		// Render

		if (mode == MODE_QUADS) {
			// Draw All Of The Quads At Once (one vert is 4 floats)
			GL11.glDrawArrays(GL11.GL_QUADS, 0, floatsDrawn / 4);
		} else if (mode == MODE_POINTS) {
			// Draw All Of The Points At Once
			GL11.glDrawArrays(GL11.GL_POINTS, 0, floatsDrawn);
		}

		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		// Disable Pointers
		// Disable Vertex Arrays
		GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		// Disable TextureNew Coord Arrays
		GlUtil.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

		GlUtil.glDisableClientState(GL11.GL_COLOR_ARRAY);

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

	private void handleAtribb(Vector3f start, float startPercent, float endPercent, float len, FloatBuffer attribBuffer) {
		attribHelper.set(start.x, startPercent, len, tCoords[0]);
		GlUtil.putPoint4(attribBuffer, attribHelper);

		attribHelper.set(start.x, startPercent, len, tCoords[1]);
		GlUtil.putPoint4(attribBuffer, attribHelper);

		attribHelper.set(start.x, endPercent, len, tCoords[2]);
		GlUtil.putPoint4(attribBuffer, attribHelper);

		attribHelper.set(start.x, endPercent, len, tCoords[3]);
		GlUtil.putPoint4(attribBuffer, attribHelper);
	}
	public int sections = 2; 
	private void handleBeamQuads(int i, ParticleContainer particles) {
		particles.getVelocity(i, velocityHelper);
		particles.getPos(i, beamStartPosHelper);

		float lifetime = particles.getLifetime(i);
		float speed = velocityHelper.length();
		
		float distanceFlown = lifetime * speed;
		
		float beamLength = (this.beamLength*(speed*0.03f)) * Math.min(1f, lifetime/(this.beamLength*(speed*0.03f)));
		

		beamEndPosHelper.set(beamStartPosHelper);
		
		FastMath.normalizeCarmack(velocityHelper);
		
		beamStartPosHelper.x -= velocityHelper.x * beamLength;
		beamStartPosHelper.y -= velocityHelper.y * beamLength;
		beamStartPosHelper.z -= velocityHelper.z * beamLength;

		if (!GlUtil.isPointInCamRange(beamStartPosHelper, Controller.vis.getVisLen()) &&
				!GlUtil.isPointInCamRange(beamEndPosHelper, Controller.vis.getVisLen())) {
			return;
		}
		if (!Controller.getCamera().isPointInFrustrum(beamStartPosHelper.x, beamStartPosHelper.y, beamStartPosHelper.z) &&
				!Controller.getCamera().isPointInFrustrum(beamEndPosHelper.x, beamEndPosHelper.y, beamEndPosHelper.z)) {
			return;
		}

		
		((StartContainerInterface)particles).getStart(i, startHelper);

		axis.sub(beamEndPosHelper, beamStartPosHelper);
		axis.normalize();

		

		float damage = Math.max(minDamage, Math.min(maxDamage, ((DamageContainerInterface)particles).getDamage(i)));

		float dist = damage * maxDamageM;
		
		t.setIdentity();
		GlUtil.billboardAbitraryAxis(beamStartPosHelper, axis, Controller.getCamera().getPos(), t);
		GlUtil.getRibbon(t, dist, cStart);

		t.setIdentity();
		GlUtil.billboardAbitraryAxis(beamEndPosHelper, axis, Controller.getCamera().getPos(), t);
		GlUtil.getRibbon(t, dist, cEnd);

		cMiddle[0].set(cStart[2]);
		cMiddle[1].set(cStart[3]);
		cMiddle[2].set(cEnd[0]);
		cMiddle[3].set(cEnd[1]);

		if(sections >= 3) {
			GlUtil.putRibbon4(vertexBuffer, lifetime, cStart);
		}
		GlUtil.putRibbon4(vertexBuffer, lifetime, cMiddle);
		
		GlUtil.putRibbon4(vertexBuffer, lifetime, cEnd);

		if(sections >= 3) {
			handleAtribb(startHelper, 0f, 0.3333333f, beamLength, attribBuffer);
		}
		handleAtribb(startHelper, 0.3333333f, 0.6666666f, beamLength, attribBuffer);
		handleAtribb(startHelper, 0.6666666f, 1f, beamLength, attribBuffer);

		
		Vector4f color = particles.getColor(i, colorHelper);
		for (int j = 0; j < sections*4; j++) {
			GlUtil.putPoint4(colorBuffer, color);
		}

	}

	private void initVertexBuffer(int maxParticleCount) {

		// Generate And Bind The Vertex Buffer
		vertexVBO = GL15.glGenBuffers(); // Get A Valid Name
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVBO); // Bind
		Controller.loadedVBOBuffers.add(vertexVBO);
		// Load The Data
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer,
				GL15.GL_DYNAMIC_DRAW);
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Bind

		if (colorBuffer != null) {
			// Generate And Bind The Vertex Buffer
			colorVBO = GL15.glGenBuffers(); // Get A Valid Name
			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorVBO); // Bind
			Controller.loadedVBOBuffers.add(colorVBO);
			// Load The Data
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorBuffer,
					GL15.GL_DYNAMIC_DRAW);
			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Bind
		}

		// Generate And Bind The Vertex Buffer
		parametersVBO = GL15.glGenBuffers(); // Get A Valid Name
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, parametersVBO); // Bind
		Controller.loadedVBOBuffers.add(parametersVBO);
		// Load The Data
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, attribBuffer,
				GL15.GL_DYNAMIC_DRAW);
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Bind
	}

	/**
	 * @return the blended
	 */
	public boolean isBlended() {
		return blended;
	}

	/**
	 * @param blended the blended to set
	 */
	public void setBlended(boolean blended) {
		this.blended = blended;
	}

	public boolean isDrawInverse() {
		return drawInverse;
	}

	public void setDrawInverse(boolean drawInverse) {
		this.drawInverse = drawInverse;
	}

	private void loadVBO() {
		int maxParticleCount = MAX_PARTICLES_DRAWN;
		if (vertexBuffer == null) {
			if (mode == MODE_POINTS) {
				vertexBuffer = MemoryUtil.memAllocFloat(maxParticleCount * 4);
			} else if (mode == MODE_QUADS) {
				//quads need 4 verts
				vertexBuffer = MemoryUtil.memAllocFloat(maxParticleCount * 4 * (sections * 4));
			}
		} else {
			vertexBuffer.clear();
		}
		if (attribBuffer == null) {
			if (mode == MODE_POINTS) {
				attribBuffer = MemoryUtil.memAllocFloat(maxParticleCount * 4);
			} else if (mode == MODE_QUADS) {
				//quads need 4 attribs
				attribBuffer = MemoryUtil.memAllocFloat(maxParticleCount * 4 * (sections * 4));
			}
		} else {
			attribBuffer.clear();
		}
		if (colorBuffer == null) {
			if (mode == MODE_POINTS) {
				//no color needed on point
			} else if (mode == MODE_QUADS) {
				//quads need 4 attribs
				colorBuffer = MemoryUtil.memAllocFloat(maxParticleCount * 4 * (sections * 4));
			}
		} else {
			colorBuffer.clear();
		}
		initVertexBuffer(maxParticleCount);
	}
	@Deprecated
	private boolean updateBufferPoints(ParticleContainer particles) {

		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVBO);
		ByteBuffer bn = null;

		// particles * vector(3) * float(4)
		int byteCountToLast = particleController.getParticleCount() * 4 * ByteUtil.SIZEOF_FLOAT;

		bn = GL30.glMapBufferRange(GL15.GL_ARRAY_BUFFER,
				0,
				byteCountToLast,
				GL30.GL_MAP_WRITE_BIT | GL30.GL_MAP_FLUSH_EXPLICIT_BIT | GL30.GL_MAP_UNSYNCHRONIZED_BIT | GL30.GL_MAP_INVALIDATE_RANGE_BIT,
				bn);

		assert (bn != null) : "byte buffer null. currently bound to " + vertexVBO;

		vertexBuffer = bn.order(ByteOrder.nativeOrder()).asFloatBuffer();
		vertexBuffer.flip();
		floatsDrawn = vertexBuffer.limit();

		for (int i = 0; i < particleController.getParticleCount(); i++) {
			particles.getPos(i, beamStartPosHelper);
			GlUtil.putPoint3(vertexBuffer, beamStartPosHelper);
			particles.getColor(i, colorHelper);
			GlUtil.putPoint4(attribBuffer, colorHelper);
		}
		GL30.glFlushMappedBufferRange(GL15.GL_ARRAY_BUFFER,
				0,
				byteCountToLast);

		boolean un = GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER);
		assert (un) : "unmapping of VBO failed";

		return true;
	}

	private boolean updateBufferQuads(ParticleContainer particles) {

		// particles * quad(4) * vector(3) * float(4)

		vertexBuffer.clear();
		attribBuffer.clear();
		colorBuffer.clear();


		up = GlUtil.getUpVector(up);
		right = GlUtil.getRightVector(right);
		forward = GlUtil.getForwardVector(forward);

		int max = Math.min(MAX_PARTICLES_DRAWN, particleController.getParticleCount());

		if (drawInverse) {

			for (int i = max - 1; i >= 0; i--) {
				handleBeamQuads(i, particles);
			}
		} else {
			for (int i = 0; i < max; i++) {
				handleBeamQuads(i, particles);
			}
		}
		if (vertexBuffer.position() == 0) {
			floatsDrawn = 0;
			//everything is out of frustum
			return false;
		}

		vertexBuffer.flip();
		attribBuffer.flip();
		colorBuffer.flip();

		floatsDrawn = vertexBuffer.limit();
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVBO);
		GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, vertexBuffer);
		
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, parametersVBO);
		GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, attribBuffer);
		
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorVBO);
		GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, colorBuffer);

		return true;

	}

	private boolean updateBuffers() {

		ParticleContainer particles = particleController.getParticles();

		if (mode == MODE_QUADS) {
			return updateBufferQuads(particles);
		} else if (mode == MODE_POINTS) {
			return updateBufferPoints(particles);
		}
		throw new IllegalStateException("mode not known: " + mode);
	}

}
