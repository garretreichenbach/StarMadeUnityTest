package org.schema.game.client.data.city;

import java.util.ArrayList;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.schema.schine.graphicsengine.core.GlUtil;

public class CDeco {

	private static final int TRIM_ROWS = 3;
	private static final float TRIM_SIZE = 3;
	private static final float LOGO_OFFSET = 1;
	private Vector3f center;
	private ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
	private ArrayList<Vector2f> texCoords = new ArrayList<Vector2f>();
	@SuppressWarnings("unused")
	private ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
	private boolean use_alpha;
	private int texture;

	public void CreateLightStrip(float x, float z, float width, float depth, float height, Vector4f color) {
		Vector3f p;
		//		  quad_strip qs1;
		float u, v;

		this.use_alpha = true;
		this.center = new Vector3f(x + width / 2, height, z + depth / 2);
		if (width < depth) {
			u = 1.0f;
			v = ((int) (depth / width));
		} else {
			v = 1.0f;
			u = ((int) (width / depth));
		}
		texture = BuildingTexture.TextureId(BuildingTexture.TEXTURE_LIGHT);
		vertices.add(new Vector3f(x, height, z));
		texCoords.add(new Vector2f(0.0f, 0.0f));

		vertices.add(new Vector3f(x, height, z + depth));
		texCoords.add(new Vector2f(0.0f, v));

		vertices.add(new Vector3f(x + width, height, z + depth));
		texCoords.add(new Vector2f(u, v));

		vertices.add(new Vector3f(x + width, height, z + depth));
		texCoords.add(new Vector2f(u, v));

		vertices.add(new Vector3f(x + width, height, z));
		texCoords.add(new Vector2f(u, 0.0f));

		vertices.add(new Vector3f(x, height, z + depth));
		texCoords.add(new Vector2f(0.0f, v));

	}

	public void CreateLightTrim(Vector3f[] chain, int count, float height,
	                            int seed, Vector4f color) {
		ArrayList<Vector3f> buffer = new ArrayList<Vector3f>();
		Vector3f p = new Vector3f();
		Vector2f uv;
		Vector3f to;
		Vector3f out;
		int i;
		int index;
		int prev, next;
		float u, v1, v2;
		float row;
		//		  quad_strip qs;

		this.center = new Vector3f(0.0f, 0.0f, 0.0f);
		//		  qs.index_list.reserve(count * 2 + 2);

		for (i = 0; i < count; i++) {
			this.center.add(chain[i]);
		}
		this.center.scale(1.0f / count);
		row = (seed % TRIM_ROWS);
		v1 = row * TRIM_SIZE;
		v2 = (row + 1.0f) * TRIM_SIZE;
		index = 0;
		u = 0.0f;
		for (i = 0; i < count + 1; i++) {
			if (i != 0) {
				Vector3f dist = new Vector3f(chain[i % count]);
				dist.sub(p);

				u += dist.length() * 0.1f;
			}
			//Add the bottom point
			prev = i - 1;
			if (prev < 0) {
				prev = count + prev;
			}
			next = (i + 1) % count;
			to = new Vector3f(chain[next]);
			to.sub(chain[prev]);
			to.normalize();
			Vector3f normal_y = new Vector3f(0.0f, 1.0f, 0.0f);
			out = new Vector3f();
			out.cross(normal_y, to);
			out.scale(LOGO_OFFSET);

			p = new Vector3f(chain[i % count]);
			p.add(out);
			uv = new Vector2f(u, v2);

			buffer.add(new Vector3f(p));

			//		    qs.index_list.push_back(index++);
			//Top point
			p.y += height;
			uv = new Vector2f(u, v1);
			buffer.add(new Vector3f(p));
			//		    qs.index_list.push_back(index++);
		}
		//		  _mesh->QuadStripAdd (qs);
		texture = BuildingTexture.TextureId(BuildingTexture.TEXTURE_TRIM);
		//		  _mesh->Compile ();
		if (buffer.size() >= 2) {
			float fac = 1f / buffer.size();
			for (int j = 0; j < buffer.size() - 2; j += 2) {
				//quadstrip
				vertices.add(new Vector3f(buffer.get(j)));
				texCoords.add(new Vector2f((j) * fac, 0));

				vertices.add(new Vector3f(buffer.get(j + 1)));
				texCoords.add(new Vector2f((j) * fac, 1));

				vertices.add(new Vector3f(buffer.get(j + 2)));
				texCoords.add(new Vector2f((j + 1) * fac, 0));

				vertices.add(new Vector3f(buffer.get(j + 2)));
				texCoords.add(new Vector2f((j + 1) * fac, 0));

				vertices.add(new Vector3f(buffer.get(j + 1)));
				texCoords.add(new Vector2f((j) * fac, 1));

				vertices.add(new Vector3f(buffer.get(j + 3)));
				texCoords.add(new Vector2f((j + 1) * fac, 1));

			}
		}

	}

	public void CreateLogo(Vector2f start, Vector2f end, float bottom,
	                       Object worldLogoIndex, Vector4f trimColor) {

	}

	void CreateRadioTower(Vector3f pos, float height) {

		CLight l;
		float offset;
		Vector3f v;
		//	  fan       f;

		//	  for(int i=0; i<6; i++)
		//	    f.index_list.push_back(i);

		offset = height / 15.0f;
		this.center = pos;
		this.use_alpha = true;
		//Radio tower
		vertices.add(new Vector3f(center.x, center.y + height, center.z));
		texCoords.add(new Vector2f(0, 1));

		vertices.add(new Vector3f(center.x - offset, center.y, center.z - offset));
		texCoords.add(new Vector2f(1, 0));

		vertices.add(new Vector3f(center.x + offset, center.y, center.z - offset));
		texCoords.add(new Vector2f(0, 0));

		vertices.add(new Vector3f(center.x, center.y + height, center.z));
		texCoords.add(new Vector2f(0, 1));

		vertices.add(new Vector3f(center.x + offset, center.y, center.z - offset));
		texCoords.add(new Vector2f(1, 0));

		vertices.add(new Vector3f(center.x + offset, center.y, center.z + offset));
		texCoords.add(new Vector2f(0, 0));

		vertices.add(new Vector3f(center.x, center.y + height, center.z));
		texCoords.add(new Vector2f(0, 1));

		vertices.add(new Vector3f(center.x + offset, center.y, center.z + offset));
		texCoords.add(new Vector2f(1, 0));

		vertices.add(new Vector3f(center.x - offset, center.y, center.z + offset));
		texCoords.add(new Vector2f(0, 0));

		vertices.add(new Vector3f(center.x, center.y + height, center.z));
		texCoords.add(new Vector2f(0, 1));

		vertices.add(new Vector3f(center.x - offset, center.y, center.z + offset));
		texCoords.add(new Vector2f(1, 0));

		vertices.add(new Vector3f(center.x - offset, center.y, center.z - offset));
		texCoords.add(new Vector2f(0, 0));

		//	  _mesh->VertexAdd (clazz);
		//	  _mesh->FanAdd (f);
		l = new CLight(new Vector3f(center.x, center.y + height + 1.0f, center.z), new Vector4f(255, 192, 160, 1), 1);
		//	  l->Blink ();
		texture = BuildingTexture.TextureId(BuildingTexture.TEXTURE_LATTICE);
		System.err.println("bunding texture to tower " + texture);

	}

	public void testDraw() {
		//		GL11.glShadeModel(GL11.GL_FLAT);
		//		GL11.glCullFace(GL11.GL_BACK);
		//		GlUtil.glEnable(GL11.GL_CULL_FACE);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		//		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		if (use_alpha) {
			//			GlUtil.glDepthMask (false);
			//			GlUtil.glDisable(GL11.GL_DEPTH_TEST);
			//			GlUtil.glBlendFunc (GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_DST_ALPHA);
			GlUtil.glEnable(GL11.GL_BLEND);
			GlUtil.glDisable(GL11.GL_CULL_FACE);
		}

		GL11.glBegin(GL11.GL_TRIANGLES);

		//		GL11.glColor4f(0.3f, 1, 0.7f, 1);
		for (int i = 0; i < vertices.size(); i++) {
			Vector3f v = vertices.get(i);
			Vector2f t = texCoords.get(i);
			GL11.glTexCoord2f(t.x, t.y);
			GL11.glVertex3f(v.x, v.y, v.z);

		}
		GL11.glEnd();
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		//		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		if (use_alpha) {
			GlUtil.glDepthMask(true);
			GlUtil.glDisable(GL11.GL_BLEND);
			//			GlUtil.glEnable (GL11.GL_CULL_FACE);
			GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		}
	}

}
