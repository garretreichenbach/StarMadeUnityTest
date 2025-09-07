package org.schema.game.client.view.buildhelper;

import java.nio.FloatBuffer;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL15;
import org.schema.common.FastMath;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.Transformable;
import org.schema.schine.graphicsengine.forms.simple.Box;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

/**
 * Classes with the annotation BuildHelperClass are automatically
 * listed as helper algorithms
 *
 * @author schema
 */
@BuildHelperClass(name = "Ellipsoid")
public class EllipsoidBuildHelper extends BuildHelper {

	public static LongOpenHashSet poses = new LongOpenHashSet();
	private static Vector3f[][] verts = Box.init();
	@BuildHelperVar(type = "float", name = BuildHelperVarName.ELIPSOID_RADIUS_X, min = 0, max = 256)
	public float xRadius;
	@BuildHelperVar(type = "float", name = BuildHelperVarName.ELIPSOID_RADIUS_Y, min = 0, max = 256)
	public float yRadius;
	@BuildHelperVar(type = "float", name = BuildHelperVarName.ELIPSOID_RADIUS_Z, min = 0, max = 256)
	public float zRadius;
	private int vertCount;
	private FloatBuffer fBuffer;
	public EllipsoidBuildHelper(Transformable transformable) {
		super(transformable);
	}

	public static void drawEllipsoid(int uiStacks, int uiSlices, float fA, float fB, float fC) {

		poses.clear();

		float tStep = (FastMath.PI) / uiSlices;
		float sStep = (FastMath.PI) / uiStacks;
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		GlUtil.glDisable(GL11.GL_CULL_FACE);
		for (float t = -FastMath.HALF_PI; t <= (FastMath.HALF_PI) + .0001f; t += tStep) {
			GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			for (float s = -FastMath.PI; s <= FastMath.PI + .0001f; s += sStep) {
				float x0 = fA * FastMath.cos(t) * FastMath.cos(s);
				float y0 = fB * FastMath.cos(t) * FastMath.sin(s);
				float z0 = fC * FastMath.sin(t);

				GL11.glVertex3f(x0, y0, z0);

				float x1 = fA * FastMath.cos(t + tStep) * FastMath.cos(s);
				float y1 = fB * FastMath.cos(t + tStep) * FastMath.sin(s);
				float z1 = fC * FastMath.sin(t + tStep);

				GL11.glVertex3f(x1, y1, z1);

				poses.add(ElementCollection.getIndex(FastMath.round(x0), FastMath.round(y0), FastMath.round(z0)));
				poses.add(ElementCollection.getIndex(FastMath.round(x1), FastMath.round(y1), FastMath.round(z1)));
			}
			GL11.glEnd();
		}
		GlUtil.glEnable(GL11.GL_CULL_FACE);
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);

		Vector3f[][] box = Box.getVertices(new Vector3f(-0.5f, -0.5f, -0.5f), new Vector3f(0.5f, 0.5f, 0.5f), verts);

		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_CULL_FACE);
		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glDisable(GL12.GL_TEXTURE_3D);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glDisable(GL11.GL_TEXTURE_1D);
		//			GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glColor4f(1, 1, 1, 1);

		GL11.glBegin(GL11.GL_QUADS);
		Vector3f pos = new Vector3f();
		for (long p : poses) {

			ElementCollection.getPosFromIndex(p, pos);
			for (int i = 0; i < box.length; i++) {
				for (int k = 0; k < box[i].length; k++) {
					GL11.glVertex3f(pos.x + box[i][k].x, pos.y + box[i][k].y, pos.z + box[i][k].z);
				}
			}
		}
		GL11.glEnd();
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glEnable(GL11.GL_CULL_FACE);
		GlUtil.glDisable(GL11.GL_BLEND);
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
	}

	public void createEllipsoid(int uiStacks, int uiSlices, float fA, float fB, float fC) {
		poses.clear();

		float tStep = (FastMath.PI) / uiSlices;
		float sStep = (FastMath.PI) / uiStacks;

		float totalSteps = ((((FastMath.HALF_PI * 2 + FastMath.HALF_PI) + .0001f) / tStep) + 1) * ((((FastMath.HALF_PI * 2 + FastMath.HALF_PI) + .0001f) / sStep) + 1);

		float step = 0;
		for (float t = -FastMath.HALF_PI; t <= (FastMath.HALF_PI) + .0001f; t += tStep) {
			for (float s = -FastMath.PI; s <= FastMath.PI + .0001f; s += sStep) {
				float x0 = fA * FastMath.cos(t) * FastMath.cos(s);
				float y0 = fB * FastMath.cos(t) * FastMath.sin(s);
				float z0 = fC * FastMath.sin(t);

				float x1 = fA * FastMath.cos(t + tStep) * FastMath.cos(s);
				float y1 = fB * FastMath.cos(t + tStep) * FastMath.sin(s);
				float z1 = fC * FastMath.sin(t + tStep);

				poses.add(ElementCollection.getIndex(FastMath.round(x0), FastMath.round(y0), FastMath.round(z0)));
				poses.add(ElementCollection.getIndex(FastMath.round(x1), FastMath.round(y1), FastMath.round(z1)));

				step++;
				percent = step / totalSteps;
			}
		}
		vertCount = poses.size() * 24;
		fBuffer = GlUtil.getDynamicByteBuffer(vertCount * 3 * 4, 8).asFloatBuffer();
		Vector3f[][] box = Box.getVertices(new Vector3f(-0.5f, -0.5f, -0.5f), new Vector3f(0.5f, 0.5f, 0.5f), verts);

		Vector3f pos = new Vector3f();
		for (long p : poses) {

			ElementCollection.getPosFromIndex(p, pos);
			for (int i = 0; i < box.length; i++) {
				for (int k = 0; k < box[i].length; k++) {
					fBuffer.put(pos.x + box[i][k].x);
					fBuffer.put(pos.y + box[i][k].y);
					fBuffer.put(pos.z + box[i][k].z);
				}
			}
		}
		fBuffer.flip();
		percent = 1;
		setFinished(true);

	}

	@Override
	public void create() {
		int max = (int) Math.max(xRadius, Math.max(yRadius, Math.max(1, zRadius)));
		createEllipsoid(max * 4, max * 4, xRadius, yRadius, zRadius);
		setInitialized(true);
	}

	@Override
	public void drawLocal() {
		if(!isFinished()){
			return;
		}
		GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GlUtil.glEnable(GL11.GL_CULL_FACE);
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_CULL_FACE);
		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glDisable(GL12.GL_TEXTURE_3D);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glDisable(GL11.GL_TEXTURE_1D);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glColor4f(1, 1, 1, 1);

		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.buffer);
		GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);
		GL11.glDrawArrays(GL11.GL_QUADS, 0, vertCount);

		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glEnable(GL11.GL_CULL_FACE);
		GlUtil.glDisable(GL11.GL_BLEND);
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
	}

	@Override
	public void clean() {
		if (this.buffer != 0) {
			GL15.glDeleteBuffers(this.buffer);
			buffer = 0;
		}
		setFinished(false);
	}

	@Override
	public LongOpenHashSet getPoses() {
		return poses;
	}

	@Override
	public void onFinished() {
		if (this.buffer != 0) {
			GL15.glDeleteBuffers(this.buffer);
		}
		this.buffer = GL15.glGenBuffers();

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.buffer);

		System.err.println("[CLIENT] elipsoid: Blocks: " + poses.size() + "; ByteBufferNeeded: " + ((vertCount * 3 * 4) / 1024f) / 1024f + "MB");

		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, fBuffer, GL15.GL_STATIC_DRAW);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}

	@Override
	public BuildHelperFactory getType() {
		return BuildHelpers.ELLIPSOID;
	}
}
