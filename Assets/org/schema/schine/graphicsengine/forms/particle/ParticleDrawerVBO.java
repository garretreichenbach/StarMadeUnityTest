package org.schema.schine.graphicsengine.forms.particle;

import java.nio.FloatBuffer;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;
import org.schema.common.FastMath;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.GlUtil;

import com.bulletphysics.linearmath.Transform;

public class ParticleDrawerVBO implements Drawable {

	public static final int MODE_QUADS = 0;
	private static final int MAX_PARTICLES_DRAWN = 512;
	protected static FloatBuffer vertexBuffer;
	protected static FloatBuffer normalBuffer;
	protected static FloatBuffer attribBuffer;
	protected static float[] tCoords = new float[]{0, 0.50f, 0.75f, 0.25f};
	public Transform currentSystemTransform;
	protected Vector3f up = new Vector3f();
	protected Vector3f right = new Vector3f();
	protected Vector3f forward = new Vector3f();
	protected Vector3f upTmp = new Vector3f();
	protected Vector3f rightTmp = new Vector3f();
	protected Vector3f posHelper = new Vector3f();
	protected Vector4f posHelperAndTime = new Vector4f();
	protected Vector4f colorHelper = new Vector4f();
	protected Vector3f startHelper = new Vector3f();
	protected Vector3f velocityHelper = new Vector3f();
	protected Transform t = new Transform();
	protected AxisAngle4f axis = new AxisAngle4f(forward, FastMath.PI);
	protected Vector4f attribHelper = new Vector4f();
	float test = 0;
	private ParticleController particleController;
	private int vertexVBOName;
	private int attribVBOName;
	private float spriteSize = 1;
	private int vertexCount;
	private boolean drawInverse;

	public ParticleDrawerVBO(ParticleController controller) {
		setParticleController(controller);

		t.setIdentity();
		//		t.basis.rotZ(FastMath.PI);
	}

	public ParticleDrawerVBO(ParticleController controller, float spriteSize) {
		this(controller);
		this.setSpriteSize(spriteSize);
	}

	@Override
	public void cleanUp() {

	}
	public boolean depthMask;
	private int normalVBOName;
	@Override
	public void draw() {

		if (getParticleController().getParticleCount() > 0) {
			//			System.err.println("DRAW");
			boolean updateBuffers = updateBuffers();
			if (updateBuffers) {

				GlUtil.glPushMatrix();

                GlUtil.glDisable(GL11.GL_CULL_FACE);
                GlUtil.glDisable(GL11.GL_LIGHTING);
                GlUtil.glEnable(GL11.GL_BLEND);
				GlUtil.glDepthMask(depthMask);
                GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

				drawVBO();

                GlUtil.glDisable(GL11.GL_BLEND);

                GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
                GlUtil.glEnable(GL11.GL_CULL_FACE);
                GlUtil.glEnable(GL11.GL_LIGHTING);

				GlUtil.glPopMatrix();

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

		GlUtil.glEnableClientState(GL11.GL_NORMAL_ARRAY);

		// Bind Buffer To the Vertex Array
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVBOName);

		// Set The Vertex Pointer To The Vertex Buffer
		GL11.glVertexPointer(4, GL11.GL_FLOAT, 0, 0);

		// Bind Buffer to the Tex Coord Array
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, attribVBOName);

		// Set The TexCoord Pointer To The TexCoord Buffer
		GL11.glTexCoordPointer(4, GL11.GL_FLOAT, 0, 0);
		
		// Bind Buffer to the normal Coord Array
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, normalVBOName);
		
		// Set The TexCoord Pointer To The TexCoord Buffer
		GL11.glNormalPointer(GL11.GL_FLOAT, 0, 0);
		// Render

		// Draw All Of The Quads At Once
		GL11.glDrawArrays(GL11.GL_QUADS, 0, vertexCount);

		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		// Disable Pointers
		// Disable Vertex Arrays
		GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		// Disable TextureNew Coord Arrays
		GlUtil.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

		GlUtil.glDisableClientState(GL11.GL_NORMAL_ARRAY);

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
	public float getSpriteSize(int i, ParticleContainer particles) {
		return spriteSize;
	}

	public float getSpriteCode(int i, ParticleContainer particles) {
		return particles.getSpriteCode(i);
	}
	
	/**
	 * @param i
	 * @param particles
	 * @return count of vertexCount placed in buffer
	 */
	protected int handleQuad(int i, ParticleContainer particles) {
		particles.getPos(i, posHelper);

		if (currentSystemTransform != null) {
			currentSystemTransform.transform(posHelper);
		}
		if (!GlUtil.isPointInView(posHelper, Controller.vis.getVisLen())) {
			return 0;
		}
		if (currentSystemTransform != null) {
			particles.getPos(i, posHelper);
		}
		
		upTmp.set(up);
		rightTmp.set(right);
		

		posHelperAndTime.set(posHelper.x, posHelper.y, posHelper.z, particles.getLifetime(i));

		
		
		GlUtil.putBillboardQuad(vertexBuffer, upTmp, rightTmp, getSpriteSize(i, particles), posHelperAndTime);
		particles.getColor(i, colorHelper);
		for (int g = 0; g < 4; g++) {
			attribHelper.set(colorHelper.x, colorHelper.y, colorHelper.z, tCoords[g]);
			GlUtil.putPoint4(attribBuffer, attribHelper);
		}
		
		
		
		GlUtil.putPoint3(normalBuffer, getSpriteCode(i, particles), 0, 0);
		return 4;
	}

	public boolean isDrawInverse() {
		return drawInverse;
	}

	public void setDrawInverse(boolean drawInverse) {
		this.drawInverse = drawInverse;
	}

	private void loadVBO() {
		if (vertexBuffer == null) {
			//quads need 4 verts
            //MemoryUtil.memAllocFloat (used in UU)
			vertexBuffer = BufferUtils.createFloatBuffer(MAX_PARTICLES_DRAWN * 4 * 4);
			attribBuffer = BufferUtils.createFloatBuffer(MAX_PARTICLES_DRAWN * 4 * 4);
			normalBuffer = BufferUtils.createFloatBuffer(MAX_PARTICLES_DRAWN * 4 * 3);

		} else {
			vertexBuffer.rewind();
			attribBuffer.rewind();
			normalBuffer.rewind();
		}

		// Generate And Bind The Vertex Buffer
		vertexVBOName = GL15.glGenBuffers(); // Get A Valid Name
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVBOName); // Bind
		Controller.loadedVBOBuffers.add(vertexVBOName);
		// Load The Data
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer,
				GL15.GL_DYNAMIC_DRAW);
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Bind
		
		
		
		normalVBOName = GL15.glGenBuffers(); // Get A Valid Name
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, normalVBOName); // Bind
		Controller.loadedVBOBuffers.add(normalVBOName);
		// Load The Data
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, normalBuffer,
				GL15.GL_DYNAMIC_DRAW);
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Bind

		// Generate And Bind The Vertex Buffer
		attribVBOName = GL15.glGenBuffers(); // Get A Valid Name
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, attribVBOName); // Bind
		Controller.loadedVBOBuffers.add(attribVBOName);
		// Load The Data
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, attribBuffer,
				GL15.GL_DYNAMIC_DRAW);
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Bind
	}

	/**
	 * @param spriteSize the spriteSize to set
	 */
	public void setSpriteSize(float spriteSize) {
		this.spriteSize = spriteSize;
	}

	private boolean updateBufferQuads(ParticleContainer particles) {

		// particles * quad(4) * vector(3) * float(4)

		vertexBuffer.rewind();
		normalBuffer.rewind();
		attribBuffer.rewind();
		vertexBuffer.limit(vertexBuffer.capacity());
		normalBuffer.limit(normalBuffer.capacity());
		attribBuffer.limit(attribBuffer.capacity());

		up = GlUtil.getUpVector(up);
		right = GlUtil.getRightVector(right);
		forward = GlUtil.getForwardVector(forward);

		int max = Math.min(MAX_PARTICLES_DRAWN, getParticleController().getParticleCount());

		vertexCount = 0;
		if (isDrawInverse()) {

			for (int i = max - 1; i >= 0; i--) {
				vertexCount += handleQuad(i, particles);
			}
		} else {
			for (int i = 0; i < max; i++) {
				vertexCount += handleQuad(i, particles);
			}
		}
		if (vertexBuffer.position() == 0) {
			//nothing to draw
			return false;
		}
		vertexBuffer.flip();
		attribBuffer.flip();
		normalBuffer.flip();

		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVBOName);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer,
				GL15.GL_DYNAMIC_DRAW);

		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, normalVBOName);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, normalBuffer,
				GL15.GL_DYNAMIC_DRAW);
		
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, attribVBOName);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, attribBuffer,
				GL15.GL_DYNAMIC_DRAW);
		return true;
	}

	private boolean updateBuffers() {

		ParticleContainer particles = getParticleController().getParticles();
		return updateBufferQuads(particles);

	}

}
