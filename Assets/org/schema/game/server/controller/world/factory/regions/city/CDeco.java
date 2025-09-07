package org.schema.game.server.controller.world.factory.regions.city;

import java.util.ArrayList;
import java.util.Collection;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.server.controller.world.factory.regions.Region;

public class CDeco {

	private final BuildingWorld world;
	@SuppressWarnings("unused")
	private Vector3f center;

	@SuppressWarnings("unused")

	public CDeco(BuildingWorld world) {
		this.world = world;
	}

	public void CreateLightStrip(Collection<Region> regions, float x, float z, float width, float depth, float height, Vector4f color) {

		Vector3f p;
		//		  quad_strip qs1;
		float u, v;

		//		  qs1.index_list.push_back(0);
		//		  qs1.index_list.push_back(1);
		//		  qs1.index_list.push_back(3);
		//		  qs1.index_list.push_back(2);
		this.center = new Vector3f(x + width / 2, height, z + depth / 2);

		regions.add(new RoadRegion(world, null,
				new Vector3i(x, height, z),
				new Vector3i(x + width, height + 1, z + depth),
				8, 0));

	}

	public void CreateLightTrim(Collection<Region> regions, Vector3f[] chain, int count, float height,
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

		//		regions.add(new BuildingRegion(null,
		//				new Vector3i(x, height, z),
		//				new Vector3i(x + width, height, z + depth),
		//				8, 0));
		//
		//		this.color = color;
		//		this.center = new Vector3f(0.0f, 0.0f, 0.0f);
		//		//		  qs.index_list.reserve(count * 2 + 2);
		//
		//		for (i = 0; i < count; i++) {
		//			this.center.add(chain[i]);
		//		}
		//		this.center.scale(1.0f/count);
		//		row = (seed % TRIM_ROWS);
		//		v1 = row * TRIM_SIZE;
		//		v2 = (row + 1.0f) * TRIM_SIZE;
		//		index = 0;
		//		u = 0.0f;
		//		for (i = 0; i < count + 1; i++) {
		//			if (i != 0){
		//				Vector3f dist = new Vector3f(chain[i % count]);
		//				dist.sub(p);
		//
		//				u += dist.length() * 0.1f;
		//			}
		//			//Add the bottom point
		//			prev = i - 1;
		//			if (prev < 0) {
		//				prev = count + prev;
		//			}
		//			next = (i + 1) % count;
		//			to = new Vector3f(chain[next]);
		//			to.sub(chain[prev]);
		//			to.normalize();
		//			Vector3f normal_y = new Vector3f(0.0f, 1.0f, 0.0f);
		//			out = new Vector3f();
		//			out.cross(normal_y, to);
		//			out.scale(LOGO_OFFSET);
		//
		//			p = new Vector3f(chain[i % count]);
		//			p.add(out);
		//			uv = new Vector2f (u, v2);
		//
		//			buffer.add(new Vector3f(p));
		//
		//			//		    qs.index_list.push_back(index++);
		//			//Top point
		//			p.y += height;
		//			uv = new Vector2f (u, v1);
		//			buffer.add(new Vector3f(p));
		//			//		    qs.index_list.push_back(index++);
		//		}
		//		//		  _mesh->QuadStripAdd (qs);
		//		texture = BuildingTexture.TextureId(BuildingTexture.TEXTURE_TRIM);
		//		//		  _mesh->Compile ();
		//		if(buffer.size() >= 2){
		//			float fac = 1f/buffer.size();
		//			for(int j = 0; j < buffer.size()-2; j+=2){
		//				//quadstrip
		//				vertices.add(new Vector3f(buffer.get(j)));
		//				texCoords.add(new Vector2f((j)*fac, 0));
		//
		//				vertices.add(new Vector3f(buffer.get(j+1)));
		//				texCoords.add(new Vector2f((j)*fac, 1));
		//
		//				vertices.add(new Vector3f(buffer.get(j+2)));
		//				texCoords.add(new Vector2f((j+1)*fac, 0));
		//
		//
		//
		//				vertices.add(new Vector3f(buffer.get(j+2)));
		//				texCoords.add(new Vector2f((j+1)*fac, 0));
		//
		//				vertices.add(new Vector3f(buffer.get(j+1)));
		//				texCoords.add(new Vector2f((j)*fac, 1));
		//
		//				vertices.add(new Vector3f(buffer.get(j+3)));
		//				texCoords.add(new Vector2f((j+1)*fac, 1));
		//
		//			}
		//		}

	}

	public void CreateLogo(Vector2f start, Vector2f end, float bottom,
	                       Object worldLogoIndex, Vector4f trimColor) {

	}

	void CreateRadioTower(Collection<Region> regions, Vector3f pos, float height) {

		CLight l;
		float offset;
		Vector3f v;
		//	  fan       f;

		//	  for(int i=0; i<6; i++)
		//	    f.index_list.push_back(i);

		//		offset = height / 15.0f;
		offset = 1;
		this.center = pos;

		regions.add(new RadioTowerRegion(world, null,
				new Vector3i(center.x, center.y, center.z),
				new Vector3i(center.x + offset, center.y + height, center.z + offset),
				8, 0));

		//	  _mesh->VertexAdd (clazz);
		//	  _mesh->FanAdd (f);
		l = new CLight(new Vector3f(center.x, center.y + height + 1.0f, center.z), new Vector4f(255, 192, 160, 1), 1);
		//	  l->Blink ();

	}

}
