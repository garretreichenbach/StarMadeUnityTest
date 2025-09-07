package org.schema.game.client.view.buildhelper;

import java.nio.FloatBuffer;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL15;
import org.schema.common.FastMath;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.Transformable;
import org.schema.schine.graphicsengine.forms.simple.Box;

import com.bulletphysics.linearmath.MatrixUtil;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

/**
 * Classes with the annotation BuildHelperClass are automatically
 * listed as helper algorithms
 *
 * @author schema
 */
@BuildHelperClass(name = "Circle")
public class CircleBuildHelper extends BuildHelper {
	
	public static LongOpenHashSet poses = new LongOpenHashSet();
	private static Vector3f[][] verts = Box.init();
	@BuildHelperVar(type = "float", name = BuildHelperVarName.CIRCLE_RADIUS, min = 0, max = 5000)
	public float R;
	@BuildHelperVar(type = "float", name = BuildHelperVarName.CIRCLE_X_ROT, min = 0, max = 360)
	public float xRot;
	@BuildHelperVar(type = "float", name = BuildHelperVarName.CIRCLE_Y_ROT, min = 0, max = 360)
	public float yRot;
	@BuildHelperVar(type = "float", name = BuildHelperVarName.CIRCLE_Z_ROT, min = 0, max = 360)
	public float zRot;
	private int vertCount;
	private FloatBuffer fBuffer;
	public CircleBuildHelper(Transformable transformable) {
		super(transformable);
	}

//	@BuildHelperVar(type = "float", name="N", min=0, max = 256)
//	public float N;
//	@BuildHelperVar(type = "float", name="n", min=0, max = 256)
//	public float n;

	void createCircle(float r, int steps, Matrix3f rot) {
		poses.clear();

		int d = (5 - FastMath.round(r) * 4) / 4;
		int x = 0;
		int y = FastMath.round(r);

		Vector3f center = new Vector3f(0.0F, 0.0F, 0.0F);
		do {
			Vector3f p0 = new Vector3f(center.x + x, center.y + y, center.z);
			Vector3f p1 = new Vector3f(center.x + x, center.y - y, center.z);
			Vector3f p2 = new Vector3f(center.x - x, center.y + y, center.z);
			Vector3f p3 = new Vector3f(center.x - x, center.y - y, center.z);
			Vector3f p4 = new Vector3f(center.x + y, center.y + x, center.z);
			Vector3f p5 = new Vector3f(center.x + y, center.y - x, center.z);
			Vector3f p6 = new Vector3f(center.x - y, center.y + x, center.z);
			Vector3f p7 = new Vector3f(center.x - y, center.y - x, center.z);
			rot.transform(p0);
			rot.transform(p1);
			rot.transform(p2);
			rot.transform(p3);
			rot.transform(p4);
			rot.transform(p5);
			rot.transform(p6);
			rot.transform(p7);
			poses.add(ElementCollection.getIndex(FastMath.round(p0.x), FastMath.round(p0.y), FastMath.round(p0.z)));
			poses.add(ElementCollection.getIndex(FastMath.round(p1.x), FastMath.round(p1.y), FastMath.round(p1.z)));
			poses.add(ElementCollection.getIndex(FastMath.round(p2.x), FastMath.round(p2.y), FastMath.round(p2.z)));
			poses.add(ElementCollection.getIndex(FastMath.round(p3.x), FastMath.round(p3.y), FastMath.round(p3.z)));
			poses.add(ElementCollection.getIndex(FastMath.round(p4.x), FastMath.round(p4.y), FastMath.round(p4.z)));
			poses.add(ElementCollection.getIndex(FastMath.round(p5.x), FastMath.round(p5.y), FastMath.round(p5.z)));
			poses.add(ElementCollection.getIndex(FastMath.round(p6.x), FastMath.round(p6.y), FastMath.round(p6.z)));
			poses.add(ElementCollection.getIndex(FastMath.round(p7.x), FastMath.round(p7.y), FastMath.round(p7.z)));
			if (d < 0) {
				d += 2 * x + 1;
			} else {
				d += 2 * (x - y) + 1;
				y--;
			}
			x++;
		} while (x <= y);

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
//		createTorus(radius, numc, numt);

		Matrix3f rot = new Matrix3f();

		MatrixUtil.setEulerZYX(rot, FastMath.DEG_TO_RAD * xRot, FastMath.DEG_TO_RAD * yRot, FastMath.DEG_TO_RAD * zRot);

		createCircle(R, (int) (180 + R * 300), rot);
		setInitialized(true);
	}

	@Override
	public void drawLocal() {
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
		assert(this.buffer != 0);
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
		System.err.println("[CLIENT] Circle clean called");
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

		System.err.println("[CLIENT] Circle: Blocks: " + poses.size() + "; ByteBufferNeeded: " + ((vertCount * 3 * 4) / 1024f) / 1024f + "MB");

		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, fBuffer, GL15.GL_STATIC_DRAW);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}

	@Override
	public BuildHelperFactory getType() {
		return BuildHelpers.CIRCLE;
	}

}
