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
@BuildHelperClass(name = "Torus")
public class TorusBuildHelper extends BuildHelper {

	public static LongOpenHashSet poses = new LongOpenHashSet();
	private static Vector3f[][] verts = Box.init();
	@BuildHelperVar(type = "float", name = BuildHelperVarName.TORUS_RADIUS, min = 0, max = 256)
	public float R;
	@BuildHelperVar(type = "float", name = BuildHelperVarName.TORUS_TUBE_RADIUS, min = 0, max = 256)
	public float r;
	@BuildHelperVar(type = "float", name = BuildHelperVarName.TORUS_X_ROT, min = 0, max = 360)
	public float xRot;
	@BuildHelperVar(type = "float", name = BuildHelperVarName.TORUS_Y_ROT, min = 0, max = 360)
	public float yRot;
	@BuildHelperVar(type = "float", name = BuildHelperVarName.TORUS_Z_ROT, min = 0, max = 360)
	public float zRot;
	private int vertCount;
	private FloatBuffer fBuffer;
	public TorusBuildHelper(Transformable transformable) {
		super(transformable);
	}
//	@BuildHelperVar(type = "float", name="N", min=0, max = 256)
//	public float N;
//	@BuildHelperVar(type = "float", name="n", min=0, max = 256)
//	public float n;

	public static void drawTorus(int uiStacks, int uiSlices, float fA, float fB, float fC) {

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

	public void createTorus(float R, float r, int N, int n, Matrix3f rot) {
		poses.clear();
		int maxn = 1000; // max precision
		n = Math.min(n, maxn - 1);
		N = Math.min(N, maxn - 1);
		float rr = 1.5f * r;
		float dv = 2 * FastMath.PI / n;
		float dw = 2 * FastMath.PI / N;
		float v = 0.0f;
		float w = 0.0f;
		// outer loop

		float totalStep = (float) (((2 * Math.PI + dw) / dw) * ((2 * FastMath.PI + dv) / dv));
		float step = 0;
		Vector3f a = new Vector3f();
		Vector3f b = new Vector3f();
		while (w < 2 * Math.PI + dw) {
			v = 0.0f;
//			gl.glBegin(GL.GL_TRIANGLE_STRIP);
			// inner loop
			while (v < 2 * FastMath.PI + dv) {
//				gl.glNormal3d(
//						(R + rr * Math.cos(v)) * Math.cos(w)
//								- (R + r * Math.cos(v)) * Math.cos(w),
//						(R + rr * Math.cos(v)) * Math.sin(w)
//								- (R + r * Math.cos(v)) * Math.sin(w), (rr
//								* Math.sin(v) - r * Math.sin(v)));
//				gl.glVertex3d((R + r * Math.cos(v)) * Math.cos(w), (R + r
//						* Math.cos(v))
//						* Math.sin(w), r * Math.sin(v));

				float x0 = (R + r * FastMath.cos(v)) * FastMath.cos(w);
				float y0 = (R + r * FastMath.cos(v)) * FastMath.sin(w);
				float z0 = r * FastMath.sin(v);

//				GL11.glVertex3f(x0, y0, z0);

				float x1 = (R + r * FastMath.cos(v + dv)) * FastMath.cos(w + dw);
				float y1 = (R + r * FastMath.cos(v + dv)) * FastMath.sin(w + dw);
				float z1 = r * FastMath.sin(v + dv);

				a.set(x0, y0, z0);
				b.set(x1, y1, z1);

				rot.transform(a);
				rot.transform(b);

				poses.add(ElementCollection.getIndex(FastMath.round(a.x), FastMath.round(a.y), FastMath.round(a.z)));
				poses.add(ElementCollection.getIndex(FastMath.round(b.x), FastMath.round(b.y), FastMath.round(b.z)));
//				GL11.glVertex3f(x1, y1, z1);

//				gl.glNormal3d(
//						(R + rr * Math.cos(v + dv)) * Math.cos(w + dw)
//								- (R + r * Math.cos(v + dv)) * Math.cos(w + dw),
//						(R + rr * Math.cos(v + dv)) * Math.sin(w + dw)
//								- (R + r * Math.cos(v + dv)) * Math.sin(w + dw),
//						rr * Math.sin(v + dv) - r * Math.sin(v + dv));
//				gl.glVertex3d((R + r * Math.cos(v + dv)) * Math.cos(w + dw),
//						(R + r * Math.cos(v + dv)) * Math.sin(w + dw),
//						r * Math.sin(v + dv));
				v += dv;
				step++;
				percent = step / totalStep;
			} // inner loop
//			gl.glEnd();
			w += dw;
		} // outer loop
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

		createTorus(R, r, (int) R * 8, (int) R * 8, rot);
		setInitialized(true);
	}

	//	public void createTorus(float radius, float numc, float numt){
//		poses.clear();
//
//
//
//		   float s, t, x, y, z;
//
//		   for (int i = 0; i < numc; i++) {
//		      for (int j = 0; j <= numt; j++) {
//		         for (int k = 1; k >= 0; k--) {
//		            s = (i + k) % numc + 0.5f;
//		            t = j % numt;
//
//		            x = (1f+.1f*FastMath.cos(s*FastMath.TWO_PI/numc))*FastMath.cos(t*FastMath.TWO_PI/numt);
//		            y = (1f+.1f*FastMath.cos(s*FastMath.TWO_PI/numc))*FastMath.sin(t*FastMath.TWO_PI/numt);
//		            z = .1f * FastMath.sin(s * FastMath.TWO_PI / numc);
//		            poses.add(ElementCollection.getIndex(FastMath.round(radius*x), FastMath.round(radius*y), FastMath.round(radius*z)));
//		         }
//		      }
//		   }
//
//
//
//		vertCount = poses.size() * 24;
//		if(this.buffer != 0){
//			GL15.glDeleteBuffers(this.buffer);
//		}
//		this.buffer = GL15.glGenBuffers();
//
//		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.buffer);
//
//		System.err.println("[CLIENT] elipsoid: Blocks: "+poses.size()+"; ByteBufferNeeded: "+((vertCount*3*4)/1024f)/1024f+"MB");
//
//		FloatBuffer fBuffer = GlUtil.getDynamicByteBuffer(vertCount*3*4, 0).asFloatBuffer();
//
//
//
//
//		Vector3f[][] box = Box.getVertices(new Vector3f(-0.5f,-0.5f,-0.5f), new Vector3f(0.5f,0.5f,0.5f), verts);
//
//
//
//		Vector3f pos = new Vector3f();
//		for(long p : poses){
//
//			ElementCollection.getPosFromIndex(p, pos);
//			for(int i = 0; i < box.length; i++){
//				for(int k = 0; k < box[i].length; k++){
//					fBuffer.put(pos.x+box[i][k].x);
//					fBuffer.put(pos.y+box[i][k].y);
//					fBuffer.put(pos.z+box[i][k].z);
//				}
//			}
//		}
//		fBuffer.flip();
//
//		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, fBuffer, GL15.GL_STATIC_DRAW);
//
//		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
//	}
//
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
		return BuildHelpers.TORUS;
	}
}
