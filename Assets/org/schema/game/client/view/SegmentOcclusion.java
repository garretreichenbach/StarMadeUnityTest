package org.schema.game.client.view;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.DrawableRemoteSegment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.Cube;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;
import org.schema.schine.input.Keyboard;

import javax.vecmath.Vector3f;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class SegmentOcclusion {

	public static int total;
	public static int occluded;
	public static int failed;
	private static int aabbId;
	public int[] result = new int[EngineSettings.G_MAX_SEGMENTSDRAWN.getInt()];
	int[] resultPoint = new int[EngineSettings.G_MAX_SEGMENTSDRAWN.getInt()];
	int resultPointer = 0;
	int fCount = 0;
	int queryPointer;
	int queryPointerStart;
	private IntBuffer queryId;
	private boolean initialized;
	private int countDrawFunc;
	private short updateNum = -100;

	public void reinitialize(int max) {
		if (EngineSettings.G_USE_OCCLUSION_CULLING.isOn()) {
			System.err.println("[OCLUSION] Reinitializing queries: " + max);
			result = new int[EngineSettings.G_MAX_SEGMENTSDRAWN.getInt()];
			resultPoint = new int[EngineSettings.G_MAX_SEGMENTSDRAWN.getInt()];
			if (queryId != null) {
				GL15.glDeleteQueries(queryId);
					GlUtil.destroyDirectByteBuffer(queryId);
			}

			for (int i = 0; i < result.length; i++) {
				result[i] = 100;
			}

			queryId = MemoryUtil.memAllocInt(max);
			GL15.glGenQueries(queryId);

			for (int i = 0; i < queryId.limit(); i++) {
				int qid = queryId.get(i);
				GL15.glBeginQuery(GL15.GL_SAMPLES_PASSED, qid);
				GL15.glEndQuery(GL15.GL_SAMPLES_PASSED);

				//			GlUtil.printGlErrorCritical("GETT: "+i+" -> "+qid);
				int gg = GL15.glGetQueryObjecti(qid, GL15.GL_QUERY_RESULT_AVAILABLE);
				//			GlUtil.printGlErrorCritical("GETT: "+i+" -> "+queryId.get(i));
				int samples = GL15.glGetQueryObjecti(qid, GL15.GL_QUERY_RESULT);
				GlUtil.printGlErrorCritical();
			}
			initialized = true;
		}
	}

	public void initializeAABB() {
		if (aabbId == 0) {
			FloatBuffer buffer = MemoryUtil.memAllocFloat(24 * 3);

			Cube.cube(buffer, SegmentData.SEGf, new Vector3f(-0.5f, -0.5f, -0.5f));

			buffer.flip();

			assert (buffer.limit() == 24 * 3) : buffer.limit();

			aabbId = GL15.glGenBuffers();

			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, aabbId);

			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

			GlUtil.printGlErrorCritical();
		}
	}

	public void updateSamples(boolean requireFullDraw) {
		if (!initialized) {
			reinitialize((EngineSettings.G_MAX_SEGMENTSDRAWN.getInt()) * 2);
		}
		if (queryPointer - queryPointerStart > result.length) {
			queryPointerStart = 0;
			queryPointer = 0;
			assert (queryPointer - queryPointerStart < EngineSettings.G_MAX_SEGMENTSDRAWN.getInt()) : queryPointer + " :: " + ((Integer) EngineSettings.G_MAX_SEGMENTSDRAWN.getInt()).intValue();
			result = new int[(EngineSettings.G_MAX_SEGMENTSDRAWN.getInt())];
			resultPoint = new int[(EngineSettings.G_MAX_SEGMENTSDRAWN.getInt())];
		}
//		if(fCount % 3 == 0){
		occluded = 0;
		failed = 0;
		total = queryPointer - queryPointerStart;
		resultPointer = 0;
		for (int i = queryPointerStart; i < queryPointer; i++) {

			if (requireFullDraw) {
				result[resultPointer] = 100;
			} else {
				if (resultPoint[resultPointer] != 0) {
					if (GL15.glGetQueryObjecti(queryId.get(i), GL15.GL_QUERY_RESULT_AVAILABLE) == 1) {
						result[resultPointer] = GL15.glGetQueryObjecti(queryId.get(i), GL15.GL_QUERY_RESULT);
					} else {
						failed++;
						//no result available
						result[resultPointer] = 100;
					}

					if (result[resultPointer] == 0) {
						occluded++;
					} else {
						//				System.err.println("PASSED: "+result[resultPointer]);
					}
				} else {
					// this result was not queried (frustum culled)
					result[resultPointer] = 0;
					occluded++;

				}
			}
			resultPointer++;
		}

//		System.err.println("OCCLUDED BATCHES: "+occluded);
//		}
		fCount++;
	}

	public void prepareAABB() {
		assert (aabbId != 0);
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, aabbId);
		GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);//24*3*ByteUtil.SIZEOF_FLOAT
	}

	public void endAABB() {
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}

	public void processOcclusionQueries(SegmentDrawer draw, boolean requireFullDraw, int stepsPerQuery, int maxSegs, int miniSteps, int elementCountToDraw, DrawableRemoteSegment[] drawnSegments, int minStepsBeforeQuery, Shaderable cubeShaderInterface) {
		if (!initialized) {
			reinitialize((EngineSettings.G_MAX_SEGMENTSDRAWN.getInt()) * 2);
		}
		if (draw.updateNum == updateNum) {
			return;
		}
		if (!(draw.getState().isDebugKeyDown() && Keyboard.isKeyDown(GLFW.GLFW_KEY_COMMA))) {
			GlUtil.glDepthMask(false);
			GL11.glColorMask(false, false, false, false);
		}
		GlUtil.glDisable(GL11.GL_CULL_FACE);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);

		updateNum = draw.updateNum;

		queryPointer = 0;

		int mod = countDrawFunc % 2 == 0 ? maxSegs : 0;
		int omod = (countDrawFunc + 1) % 2 == 0 ? maxSegs : 0;
		queryPointerStart = mod;
		queryPointer = mod;
		countDrawFunc++;

		ShaderLibrary.cubeShader13Simple.setShaderInterface(cubeShaderInterface);
		ShaderLibrary.cubeShader13Simple.load();
		int resultIndi = 0;

		SegmentController currentTrans = null;

		GlUtil.glPushMatrix();
		Vector3i lastPos = new Vector3i();
		for (int step = 0; step < stepsPerQuery; step++) {
			int stt = step * miniSteps;
			int nEnd = stt + miniSteps;
//			System.err.println("SS:: "+((step+mod)-1));
			int qid = queryId.get((step + mod));

			if (step >= minStepsBeforeQuery) {

				for (int i = stt; i < elementCountToDraw && i < nEnd; i++) {
					DrawableRemoteSegment segmentToDraw = drawnSegments[i];
					if (draw.inViewFrustum(segmentToDraw)) {

						if (currentTrans != null || currentTrans != segmentToDraw.getSegmentController()) {
							GlUtil.glPopMatrix(); //pop the current general modelview
							GlUtil.glPushMatrix();//and back to the stack for it for the next context change
							GlUtil.glMultMatrix(segmentToDraw.getSegmentController().getWorldTransformOnClient());
							lastPos.set(0, 0, 0);
						}
						GL11.glTranslatef(segmentToDraw.pos.x - lastPos.x, segmentToDraw.pos.y - lastPos.y, segmentToDraw.pos.z - lastPos.z);

						GL15.glBeginQuery(GL15.GL_SAMPLES_PASSED, qid);
						//go from last pos to the current segment
						GL11.glDrawArrays(GL11.GL_QUADS, 0, 24);

						GL15.glEndQuery(GL15.GL_SAMPLES_PASSED);

						lastPos.set(segmentToDraw.pos);
						resultPoint[resultIndi] = 1;
					} else {
						resultPoint[resultIndi] = 0;
					}
				}

			} else {
				resultPoint[resultIndi] = 0;
			}
			resultIndi++;
			queryPointer++;

		}

		GlUtil.glPopMatrix();

		GL11.glColorMask(true, true, true, true);
		GlUtil.glDepthMask(true);

		GlUtil.glEnable(GL11.GL_CULL_FACE);
	}

	/**
	 * @return the initialized
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * @param initialized the initialized to set
	 */
	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	public void cleanUp() {
				
	}
}
