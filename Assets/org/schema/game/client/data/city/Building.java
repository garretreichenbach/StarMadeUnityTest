package org.schema.game.client.data.city;

import org.lwjgl.opengl.GL11;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.Mesh;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.ArrayList;
import java.util.Random;

public class Building {
	/*-----------------------------------------------------------------------------

	  Building.cpp

	  2009 Shamus Young

	-------------------------------------------------------------------------------

	  This module contains the class to construct the buildings.

	-----------------------------------------------------------------------------*/

	public final static int MAX_VBUFFER = 256;

	//This is used by the recursive roof builder to decide what items may be added.

	public static final int ADDON_NONE = 0,
			ADDON_LOGO = 0,
			ADDON_TRIM = 0,
			ADDON_LIGHTS = 0,
			ADDON_COUNT = 5;

	public static final int BUILDING_SIMPLE = 0;

	public static final int BUILDING_MODERN = 1;

	public static final int BUILDING_TOWER = 2;

	public static final int BUILDING_BLOCKY = 3;
	public static final int NORTH = 0;
	/*-----------------------------------------------------------------------------

	  This will take the given area and populate it with rooftop stuff like
	  air conditioners or light towers.

	-----------------------------------------------------------------------------*/
	public static final int SOUTH = 1;
	public static final int EAST = 2;
	public static final int WEST = 3;
	private static final Vector4f RANDOM_COLOR = new Vector4f(1, 0, 1, 1);
	public static BuildingTexture textures;
	static Vector3f[] vector_buffer = new Vector3f[MAX_VBUFFER];
	public ArrayList<CDeco> decos = new ArrayList<CDeco>();
	Random random = new Random();
	private ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
	private ArrayList<Vector2f> texCoords = new ArrayList<Vector2f>();
	@SuppressWarnings("unused")
	private ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
	private int x;
	private int y;

	private int width;

	private int depth;

	private int height;

	private Vector3f center;

	private int seed;
	private int texture_type;
	private Vector4f color;
	private boolean have_lights;
	private boolean have_logo;
	private int roof_tiers;
	private Vector4f trim_color;
	@SuppressWarnings("unused")
	private Mesh mesh;
	@SuppressWarnings("unused")
	private Mesh mesh_flat;
	public Building(int type, int x, int y, int height, int width, int depth, int seed, Vector4f color) {

		this.x = x;
		this.y = y;
		this.width = width;
		this.depth = depth;
		this.height = height;
		this.center = new Vector3f((this.x + width / 2), 0.0f, (this.y + depth / 2));
		this.seed = seed;
		this.texture_type = random.nextInt();
		this.color = color;
		this.color.w = 0.1f;
		this.have_lights = false;
		this.have_logo = false;
		this.roof_tiers = 0;
		//Pick a color for logos & roof lights
		this.trim_color = new Vector4f();//WorldLightColor (seed);

		this.mesh = new Mesh(); //The main textured cubeMeshes for the building
		this.mesh_flat = new Mesh(); //Flat-color cubeMeshes for untextured detail items.
		switch(type) {
			case BUILDING_SIMPLE -> CreateSimple();
			case BUILDING_MODERN -> CreateModern();
			case BUILDING_TOWER -> CreateTower();
			case BUILDING_BLOCKY -> CreateBlocky();
		}

	}


	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	public static boolean coinflip() {
		return Math.random() > 0.5f;
	}


	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	//	int PolyCount ()
	//	{
	//
	//	  return this.mesh->PolyCount () + this.mesh_flat->PolyCount ();
	//
	//	}

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	//	void Render ()
	//	{
	//
	//	  glColor3fv (&_color.red);
	//	  this.mesh->Render ();
	//
	//	}


	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	//	void RenderFlat (bool colored)
	//	{
	//
	//	  if (colored)
	//	    glColor3fv (&_color.red);
	//	  this.mesh_flat->Render ();
	//
	//	}

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	//	void ConstructCube (int left, int right, int front, int back, int bottom, int top)
	//	{

	//	  GLvertex    multiTexturePathPattern[10];
	//	  float       x1, x2, z1, z2, y1, y2;
	//	  int         i;
	//	  cube        c;
	//	  float       u, v1, v2;
	//	  float       mapping;
	//	  int         base_index;
	//	  int         height;
	//
	//	  height = top - bottom;
	//	  x1 = (float)left;
	//	  x2 = (float)right;
	//	  y1 = (float)bottom;
	//	  y2 = (float)top;
	//	  z1 = (float)front;
	//	  z2 = (float)back;
	//	  base_index = this.mesh->VertexCount ();
	//
	//	  mapping = (float)SEGMENTS_PER_TEXTURE;
	//	  u = (float)(RandomVal () % SEGMENTS_PER_TEXTURE) / (float)SEGMENTS_PER_TEXTURE;
	//	  v1 = (float)bottom / (float)mapping;
	//	  v2 = (float)top / (float)mapping;
	//
	//	  multiTexturePathPattern[0].position = glVector (x1, y1, z1);  multiTexturePathPattern[0].uv = glVector (u, v1);
	//	  multiTexturePathPattern[1].position = glVector (x1, y2, z1);  multiTexturePathPattern[1].uv = glVector (u, v2);
	//	  u += (float)_width / mapping;
	//	  multiTexturePathPattern[2].position = glVector (x2, y1, z1);  multiTexturePathPattern[2].uv = glVector (u, v1);
	//	  multiTexturePathPattern[3].position = glVector (x2, y2, z1);  multiTexturePathPattern[3].uv = glVector (u, v2);
	//	  u += (float)_depth / mapping;
	//	  multiTexturePathPattern[4].position = glVector (x2, y1, z2);  multiTexturePathPattern[4].uv = glVector (u, v1);
	//	  multiTexturePathPattern[5].position = glVector (x2, y2, z2);  multiTexturePathPattern[5].uv = glVector (u, v2);
	//	  u += (float)_width / mapping;
	//	  multiTexturePathPattern[6].position = glVector (x1, y1, z2);  multiTexturePathPattern[6].uv = glVector (u, v1);
	//	  multiTexturePathPattern[7].position = glVector (x1, y2, z2);  multiTexturePathPattern[7].uv = glVector (u, v2);
	//	  u += (float)_width / mapping;
	//	  multiTexturePathPattern[8].position = glVector (x1, y1, z1);  multiTexturePathPattern[8].uv = glVector (u, v1);
	//	  multiTexturePathPattern[9].position = glVector (x1, y2, z1);  multiTexturePathPattern[9].uv = glVector (u, v2);
	//	  for (i = 0; i < 10; i++) {
	//	    multiTexturePathPattern[i].uv.x = (multiTexturePathPattern[i].position.x + multiTexturePathPattern[i].position.z) / (float)SEGMENTS_PER_TEXTURE;
	//	    this.mesh->VertexAdd (multiTexturePathPattern[i]);
	//	    c.index_list.push_back(base_index + i);
	//	  }
	//	  this.mesh->CubeAdd (c);

	//	}

	public void ConstructCube(float left, float right, float front, float back, float bottom, float top) {

		//	  GLvertex    multiTexturePathPattern[10];
		float x1, x2, z1, z2, y1, y2;
		int i;
		//	  cube        c;
		int base_index;

		x1 = left;
		x2 = right;
		y1 = bottom;
		y2 = top;
		z1 = front;
		z2 = back;
		//	  base_index = this.mesh_flat->VertexCount ();
		float mapping = BuildingTexture.SEGMENTS_PER_TEXTURE;
		float u = (float) (random.nextInt() % BuildingTexture.SEGMENTS_PER_TEXTURE) / (float) BuildingTexture.SEGMENTS_PER_TEXTURE;
		float v1 = bottom / mapping;
		float v2 = top / mapping;


		/* Top Side of the Cube with different shades of colors */

		vertices.add(new Vector3f(x1, y1, z2)); // Top Right
		texCoords.add(new Vector2f(v1, v1));
		vertices.add(new Vector3f(x2, y1, z1)); // Bottom Left
		texCoords.add(new Vector2f(v2, v2));
		vertices.add(new Vector3f(x2, y1, z2)); // Top Left
		texCoords.add(new Vector2f(v2, v1));

		vertices.add(new Vector3f(x2, y1, z1)); // Bottom Left
		texCoords.add(new Vector2f(v2, v1));
		vertices.add(new Vector3f(x1, y1, z2)); // Top Right
		texCoords.add(new Vector2f(v2, v2));
		vertices.add(new Vector3f(x1, y1, z1)); // Bottom Right
		texCoords.add(new Vector2f(v1, v2));

		/* Bottom Side of the Cube with different shades of colors */

		vertices.add(new Vector3f(x1, y2, z1)); // Top Right
		texCoords.add(new Vector2f(v1, v1));
		vertices.add(new Vector3f(x2, y2, z2)); // Bottom Left
		texCoords.add(new Vector2f(v2, v2));
		vertices.add(new Vector3f(x2, y2, z1)); // Top Left
		texCoords.add(new Vector2f(v2, v1));

		vertices.add(new Vector3f(x2, y2, z2)); // Bottom Left
		texCoords.add(new Vector2f(v2, v1));
		vertices.add(new Vector3f(x1, y2, z1)); // Top Right
		texCoords.add(new Vector2f(v2, v2));
		vertices.add(new Vector3f(x1, y2, z2)); // Bottom Right
		texCoords.add(new Vector2f(v1, v2));

		/* Front Side of the Cube with different shades of colors */

		vertices.add(new Vector3f(x1, y1, z1)); // Top Right
		texCoords.add(new Vector2f(u, v1));
		vertices.add(new Vector3f(x2, y2, z1)); // Bottom Left
		texCoords.add(new Vector2f(u, v1));
		vertices.add(new Vector3f(x2, y1, z1)); // Top Left
		texCoords.add(new Vector2f(u, v1));

		vertices.add(new Vector3f(x2, y2, z1)); // Bottom Left
		texCoords.add(new Vector2f(u, v1));
		vertices.add(new Vector3f(x1, y1, z1)); // Top Right
		texCoords.add(new Vector2f(u, v1));
		vertices.add(new Vector3f(x1, y2, z1)); // Bottom Right
		texCoords.add(new Vector2f(u, v1));

		/* BackSide of the Cube with different shades of colors */

		vertices.add(new Vector3f(x1, y2, z2)); // Top Right
		texCoords.add(new Vector2f(u, v1));
		vertices.add(new Vector3f(x2, y1, z2)); // Bottom Left
		texCoords.add(new Vector2f(u, v1));
		vertices.add(new Vector3f(x2, y2, z2)); // Top Left
		texCoords.add(new Vector2f(u, v1));

		vertices.add(new Vector3f(x2, y1, z2)); // Bottom Left
		texCoords.add(new Vector2f(u, v1));
		vertices.add(new Vector3f(x1, y2, z2)); // Top Right
		texCoords.add(new Vector2f(u, v1));
		vertices.add(new Vector3f(x1, y1, z2)); // Bottom Right
		texCoords.add(new Vector2f(u, v1));

		/* LeftSide of the Cube with different shades of colors */

		vertices.add(new Vector3f(x2, y1, z1)); // Top Right
		texCoords.add(new Vector2f(u, v1));
		vertices.add(new Vector3f(x2, y2, z2)); // Bottom Left
		texCoords.add(new Vector2f(u, v1));
		vertices.add(new Vector3f(x2, y1, z2)); // Top Left
		texCoords.add(new Vector2f(u, v1));

		vertices.add(new Vector3f(x2, y2, z2)); // Bottom Left
		texCoords.add(new Vector2f(u, v1));
		vertices.add(new Vector3f(x2, y1, z1)); // Top Right
		texCoords.add(new Vector2f(u, v1));
		vertices.add(new Vector3f(x2, y2, z1)); // Bottom Right
		texCoords.add(new Vector2f(u, v1));

		/* Right Side of the Cube with different shades of colors */

		vertices.add(new Vector3f(x1, y1, z2)); // Top Right
		texCoords.add(new Vector2f(u, v1));
		vertices.add(new Vector3f(x1, y2, z1)); // Bottom Left
		texCoords.add(new Vector2f(u, v1));
		vertices.add(new Vector3f(x1, y1, z1)); // Top Left
		texCoords.add(new Vector2f(u, v1));

		vertices.add(new Vector3f(x1, y2, z1)); // Bottom Left
		texCoords.add(new Vector2f(u, v1));
		vertices.add(new Vector3f(x1, y1, z2)); // Top Right
		texCoords.add(new Vector2f(u, v1));
		vertices.add(new Vector3f(x1, y2, z2)); // Bottom Right
		texCoords.add(new Vector2f(u, v1));

		//	  for(int j = 0; j < vertices.size(); j++){
		//		  System.out.println("vert "+j+": "+vertices.get(j));
		//		  if((j+1)%3 == 0){
		//			  System.out.println("---------");
		//		  }
		//	  }

		//	  for (i = 0; i < 10; i++) {
		//	    multiTexturePathPattern[i].uv.x = (multiTexturePathPattern[i].position.x + multiTexturePathPattern[i].position.z) / (float)SEGMENTS_PER_TEXTURE;
		//	    this.mesh_flat->VertexAdd (multiTexturePathPattern[i]);
		//	    c.index_list.push_back(base_index + i);
		//	  }
		//	  this.mesh_flat->CubeAdd (c);

	}

	void ConstructRoof(float left, float right, float front, float back, float bottom) {

		int air_conditioners;
		int i;
		int width, depth, height;
		int face;
		int addon = 0;
		int max_tiers;
		float ac_x;
		float ac_y;
		float ac_base;
		float ac_size;
		float ac_height;
		float tower_height;
		float logo_offset;
		Vector2f start, end;

		this.roof_tiers++;
		max_tiers = this.height / 13;
		width = (int) (right - left);
		depth = (int) (back - front);
		height = this.height / 13 - this.roof_tiers;
		logo_offset = 0.2f;
		//See if this building is special and worthy of fancy roof decorations.
		if (bottom > 35.0f) {
			addon = random.nextInt(ADDON_COUNT);
		}
		//Build the roof slab
		System.err.println("cube height " + height);
		ConstructCube(left, right, front, back, bottom, bottom + height);
		//Consider putting a logo on the roof, if it's tall enough
		if (addon == ADDON_LOGO && !this.have_logo) {
			CDeco d = new CDeco();
			if (width > depth) {
				face = coinflip() ? NORTH : SOUTH;
			} else {
				face = coinflip() ? EAST : WEST;
			}
			switch(face) {
				case NORTH -> {
					start = new Vector2f(left, back + logo_offset);
					end = new Vector2f(right, back + logo_offset);
				}
				case SOUTH -> {
					start = new Vector2f(right, front - logo_offset);
					end = new Vector2f(left, front - logo_offset);
				}
				case EAST -> {
					start = new Vector2f(right + logo_offset, back);
					end = new Vector2f(right + logo_offset, front);
				}
				default -> {
					start = new Vector2f(left - logo_offset, front);
					end = new Vector2f(left - logo_offset, back);
				}
			}
			d.CreateLogo(start, end, bottom, null, this.trim_color);
			this.have_logo = true;
			decos.add(d);
		} else if (addon == ADDON_TRIM) {
			CDeco d = new CDeco();
			vector_buffer[0] = new Vector3f(left, bottom, back);
			vector_buffer[1] = new Vector3f(left, bottom, front);
			vector_buffer[2] = new Vector3f(right, bottom, front);
			vector_buffer[3] = new Vector3f(right, bottom, back);
			d.CreateLightTrim(vector_buffer, 4, random.nextInt(2) + 1.0f, this.seed, this.trim_color);
			decos.add(d);
		} else if (addon == ADDON_LIGHTS && !this.have_lights) {
			new CLight(new Vector3f(left, (bottom + 2), front), this.trim_color, 2);
			new CLight(new Vector3f(right, (bottom + 2), front), this.trim_color, 2);
			new CLight(new Vector3f(right, (bottom + 2), back), this.trim_color, 2);
			new CLight(new Vector3f(left, (bottom + 2), back), this.trim_color, 2);
			this.have_lights = true;
		}
		bottom += height;
		//If the roof is big enough, consider making another layer
		if (width > this.width / 5 && depth > this.depth - 5 && this.roof_tiers < max_tiers) {
			System.err.println("current width depth: " + width + " x " + depth + " --- orig " + this.width + " x " + this.depth);
			ConstructRoof(left + 1, right - 1, front + 1, back - 1, bottom);
			return;
		}
		//1 air conditioner block for every 15 floors sounds reasonble
		air_conditioners = this.height / 15;
		for (i = 0; i < air_conditioners; i++) {
			ac_size = (float) (10 + random.nextInt(30)) / 10;
			ac_height = (float) random.nextInt(20) / 10 + 1.0f;
			if (width <= 0) {
				System.out.println("width was " + width);
				if (width == 0) {
					width = 1;
				}
			}
			ac_x = left + random.nextInt(Math.abs(width)) * Math.signum(width);
			if (depth <= 0) {
				System.out.println("depth was " + depth);
				if (depth == 0) {
					depth = 1;
				}
			}
			ac_y = front + random.nextInt(Math.abs(depth)) * Math.signum(depth);
			//make sure the unit doesn'transformationArray hang off the right edge of the building
			if (ac_x + ac_size > right) {
				ac_x = right - ac_size;
			}
			//make sure the unit doesn'transformationArray hang off the back edge of the building
			if (ac_y + ac_size > back) {
				ac_y = back - ac_size;
			}
			ac_base = bottom;
			//make sure it doesn'transformationArray hang off the edge
			ConstructCube(ac_x, ac_x + ac_size, ac_y, ac_y + ac_size, ac_base, ac_base + ac_height);
		}

		if (this.height > 45) {
			CDeco d = new CDeco();
			tower_height = (12 + random.nextInt(8));
			d.CreateRadioTower(new Vector3f((left + right) / 2.0f, bottom, (front + back) / 2.0f), 15.0f);
			decos.add(d);
		}

	}

	public void ConstructSpike(int left, int right, int front, int back, int bottom, int top) {

		Vector3f p1;
		Vector3f p2;
		Vector3f p3;
		Vector2f uv;
		//	  fan         f;
		int i;
		Vector3f center = new Vector3f();

		//	  for (i = 0; i < 5; i++)
		//	    f.index_list.push_back(_mesh_flat->VertexCount () + i);
		//	  f.index_list.push_back(f.index_list[1]);

		uv = new Vector2f(0.0f, 0.0f);
		center.x = (left + -(float) right) / 2.0f;
		center.z = (front + -(float) back) / 2.0f;

		vertices.add(new Vector3f(center.x, top, center.z));
		vertices.add(new Vector3f(left, -(float) bottom, -(float) back));
		vertices.add(new Vector3f(-right, -(float) bottom, -(float) back));
		//	  this.mesh_flat->VertexAdd (multiTexturePathPattern);

		vertices.add(new Vector3f(center.x, top, center.z));
		vertices.add(new Vector3f(-right, -(float) bottom, -(float) back));
		vertices.add(new Vector3f(-right, -(float) bottom, front));
		//	  this.mesh_flat->VertexAdd (multiTexturePathPattern);

		vertices.add(new Vector3f(center.x, top, center.z));
		vertices.add(new Vector3f(-right, -(float) bottom, front));
		vertices.add(new Vector3f(left, -(float) bottom, front));
		//	  this.mesh_flat->VertexAdd (multiTexturePathPattern);

		vertices.add(new Vector3f(center.x, top, center.z));
		vertices.add(new Vector3f(left, -(float) bottom, front));
		vertices.add(new Vector3f(left, -(float) bottom, -(float) back));
		//	  this.mesh_flat->VertexAdd (multiTexturePathPattern);

		//	  this.mesh_flat->FanAdd (f);
		for (int j = 0; j < vertices.size(); j++) {
			System.out.println("vert " + j + ": " + vertices.get(j));
			if ((j + 1) % 3 == 0) {
				System.out.println("---------");
			}
		}
		System.out.println(center);
	}

	public float ConstructWall(int start_x, int start_y, int start_z, int direction, int length, int height, int window_groups, float uv_start, boolean blank_corners) {
		ArrayList<Vector3f> buffer = new ArrayList<Vector3f>();
		int x, z;
		int step_x = 1, step_z = 0;
		int i;
		//	  quad_strip  qs;
		int column;
		int mid;
		int odd;
		Vector3f v;
		Vector3f uv = new Vector3f();
		boolean blank;
		boolean last_blank;

		//	  qs.index_list.reserve(100);
		switch(direction) {
			case NORTH -> {
				step_z = 1;
				step_x = 0;
			}
			case WEST -> {
				step_z = 0;
				step_x = -1;
			}
			case SOUTH -> {
				step_z = -1;
				step_x = 0;
			}
			case EAST -> {
				step_z = 0;
				step_x = 1;
			}
		}
		x = start_x;
		;
		z = start_z;
		mid = (length / 2) - 1;
		odd = 1 - (length % 2);
		if (length % 2 == 0) {
			mid++;
		}
		//mid = (length / 2);
		uv.x = (float) (x + z) / BuildingTexture.SEGMENTS_PER_TEXTURE;
		uv.x = uv_start;
		blank = false;
		for (i = 0; i <= length; i++) {
			//column counts up to the mid point, then back down, to make it symetrical
			if (i <= mid) {
				column = i - odd;
			} else {
				column = (mid) - (i - (mid));
			}
			last_blank = blank;
			blank = (column % window_groups) > window_groups / 2;
			if (blank_corners && i == 0) {
				blank = true;
			}
			if (blank_corners && i == (length - 1)) {
				blank = true;
			}
			if (last_blank != blank || i == 0 || i == length) {
				buffer.add(new Vector3f((float) x - step_x, start_y, (float) z - step_z));
				uv.y = (float) start_y / BuildingTexture.SEGMENTS_PER_TEXTURE;
				buffer.add(new Vector3f((float) x - step_x, (float) start_y + height, (float) z - step_z));
				uv.y = (float) (start_y + height) / BuildingTexture.SEGMENTS_PER_TEXTURE;

				//	      this.mesh->VertexAdd (clazz);TODO
				//	      qs.index_list.push_back(_mesh->VertexCount () - 1);
			}
			//if (!blank && i != 0 && i != (length - 1))
			if (!blank && i != length) {
				uv.x += 1.0f / BuildingTexture.SEGMENTS_PER_TEXTURE;
			}
			x += step_x;
			z += step_z;
		}
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
		//	  this.mesh->QuadStripAdd (qs);
		return uv.x;

	}

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	void CreateBlocky() {

		int min_height;
		int left, right, front, back;
		int max_left, max_right, max_front, max_back;
		int height;
		int mid_x, mid_z;
		int half_depth, half_width;
		int tiers;
		int max_tiers;
		int grouping;
		float lid_height;
		float uv_start;
		boolean skip;
		boolean blank_corners;

		//Choose if the corners of the building are to be windowless.
		blank_corners = coinflip();
		//Choose a random column on our texture;
		uv_start = (float) random.nextInt(BuildingTexture.SEGMENTS_PER_TEXTURE) / BuildingTexture.SEGMENTS_PER_TEXTURE;
		//Choose how the windows are grouped
		grouping = 2 + random.nextInt(4);
		//Choose how tall the lid should be on top of each section
		lid_height = (random.nextInt(3) + 1);
		//find the center of the building.
		mid_x = this.x + this.width / 2;
		mid_z = this.y + this.depth / 2;
		max_left = max_right = max_front = max_back = 1;
		height = this.height;
		min_height = this.height / 2;
		min_height = 3;
		half_depth = this.depth / 2;
		half_width = this.width / 2;
		tiers = 0;
		if (this.height > 40) {
			max_tiers = 15;
		} else if (this.height > 30) {
			max_tiers = 10;
		} else if (this.height > 20) {
			max_tiers = 5;
		} else if (this.height > 10) {
			max_tiers = 2;
		} else {
			max_tiers = 1;
		}
		//We begin at the top of the building, and work our way down.
		//Viewed from above, the sections of the building are randomly sized
		//rectangles that ALWAYS include the center of the building somewhere within
		//their area.
		while (true) {
			if (height < min_height) {
				break;
			}
			if (tiers >= max_tiers) {
				break;
			}
			//pick new locationsfor our four outer walls
			left = (random.nextInt() % half_width) + 1;
			right = (random.nextInt() % half_width) + 1;
			front = (random.nextInt() % half_depth) + 1;
			back = (random.nextInt() % half_depth) + 1;
			skip = false;
			//At least ONE of the walls must reach out beyond a previous maximum.
			//Otherwise, this tier would be completely hidden within a previous one.
			if (left <= max_left && right <= max_right && front <= max_front && back <= max_back) {
				skip = true;
			}
			//If any of the four walls is in the same position as the previous maxThis,then
			//skip this tier, or else the two walls will end up z-fightng.
			if (left == max_left || right == max_right || front == max_front || back == max_back) {
				skip = true;
			}
			if (!skip) {
				//if this is the top, then put some lights up here
				max_left = Math.max(left, max_left);
				max_right = Math.max(right, max_right);
				max_front = Math.max(front, max_front);
				max_back = Math.max(back, max_back);
				//Now build the four walls of this part
				uv_start = ConstructWall(mid_x - left, 0, mid_z + back, SOUTH, front + back, height, grouping, uv_start, blank_corners) - BuildingTexture.ONE_SEGMENT;
				uv_start = ConstructWall(mid_x - left, 0, mid_z - front, EAST, right + left, height, grouping, uv_start, blank_corners) - BuildingTexture.ONE_SEGMENT;
				uv_start = ConstructWall(mid_x + right, 0, mid_z - front, NORTH, front + back, height, grouping, uv_start, blank_corners) - BuildingTexture.ONE_SEGMENT;
				uv_start = ConstructWall(mid_x + right, 0, mid_z + back, WEST, right + left, height, grouping, uv_start, blank_corners) - BuildingTexture.ONE_SEGMENT;
				if (tiers == 0) {//original if(!tiers) (0 is false) -> !(tiers != 0) -> tiers == 0
					ConstructRoof((mid_x - left), (mid_x + right), (mid_z - front), (mid_z + back), height);
				} else {//add a flat-color lid onto this section
					ConstructCube((mid_x - left), (mid_x + right), (mid_z - front), (mid_z + back), height, height + lid_height);
				}
				height -= (random.nextInt() % 10) + 1;
				tiers++;
			}
			height--;
		}
		ConstructCube(mid_x - half_width, mid_x + half_width, mid_z - half_depth, mid_z + half_depth, 0, 2);
		//	  this.mesh->Compile ();
		//	  this.mesh_flat->Compile ();

	}

	/*-----------------------------------------------------------------------------

	  This builds an outer wall of a building, with blank (windowless) areas
	  deliberately left.  It creates a chain of segments that alternate
	  between windowed and windowless, and it always makes sure the wall
	  is symetrical.  window_groups tells it how many windows to place in a row.

	-----------------------------------------------------------------------------*/

	void CreateModern() {
		ArrayList<Vector3f> buffer = new ArrayList<Vector3f>();
		Vector3f p = new Vector3f();
		Vector3f uv = new Vector3f();
		Vector3f center = new Vector3f();
		Vector3f pos = new Vector3f();
		Vector2f radius;
		Vector2f start, end;
		int angle;
		int windows;
		int cap_height;
		int half_depth, half_width;
		float dist;
		float length;
		//	  quad_strip  qs;
		//	  fan         f;
		int points;
		int skip_interval;
		int skip_counter;
		int skip_delta;
		int i;
		boolean logo_done;
		boolean do_trim;

		logo_done = false;
		//How tall the windowless section on top will be.
		cap_height = 1 + random.nextInt(5);
		//How many 10-degree segments to build before the next skip.
		skip_interval = 1 + random.nextInt(8);
		//When a skip happens, how many degrees should be skipped
		skip_delta = (1 + random.nextInt(2)) * 30; //30 60 or 90
		//See if this is eligible for fancy lighting trim on top
		if (this.height > 48 && random.nextInt(3) == 0) {
			do_trim = true;
		} else {
			do_trim = false;
		}
		//Get the center and radius of the circle
		half_depth = this.depth / 2;
		half_width = this.width / 2;
		center = new Vector3f((this.x + half_width), 0.0f, (this.y + half_depth));
		radius = new Vector2f(half_width, half_depth);
		dist = 0;
		windows = 0;
		uv.x = 0.0f;
		points = 0;
		skip_counter = 0;
		for (angle = 0; angle <= 360; angle += 10) {
			if (skip_counter >= skip_interval && (angle + skip_delta < 360)) {
				angle += skip_delta;
				skip_counter = 0;
			}
			pos.x = (float) (center.x - Math.sin(Math.toRadians(angle)) * radius.x);
			pos.z = (float) (center.z + Math.cos(Math.toRadians(angle)) * radius.y);
			if (angle > 0 && skip_counter == 0) {
				length = MathDistance(p.x, p.z, pos.x, pos.z);
				windows += (int) length;
				if (length > 10 && !logo_done) {
					logo_done = true;
					start = new Vector2f(pos.x, pos.z);
					end = new Vector2f(p.x, p.z);
					CDeco d = new CDeco();
					d.CreateLogo(start, end, this.height, null, RANDOM_COLOR);
					decos.add(d);
				}
			} else if (skip_counter != 1) {
				windows++;
			}
			p = pos;
			uv.x = (float) windows / (float) BuildingTexture.SEGMENTS_PER_TEXTURE;
			uv.y = 0.0f;
			p.y = 0.0f;
			//	    this.mesh->VertexAdd (multiTexturePathPattern);
			buffer.add(new Vector3f(p));

			p.y = this.height;
			uv.y = (float) this.height / (float) BuildingTexture.SEGMENTS_PER_TEXTURE;
			//	    this.mesh->VertexAdd (multiTexturePathPattern);
			//	    this.mesh_flat->VertexAdd (multiTexturePathPattern);
			buffer.add(new Vector3f(p));

			p.y += cap_height;
			//	    this.mesh_flat->VertexAdd (multiTexturePathPattern);

			vector_buffer[points / 2] = p;
			vector_buffer[points / 2].y = (float) this.height + cap_height / 4;
			points += 2;
			skip_counter++;
		}
		//if this is a big building and it didn'transformationArray get a logo, consider giving it a light strip
		if (!logo_done && do_trim) {
			CDeco d = new CDeco();
			d.CreateLightTrim(vector_buffer, (points / 2) - 2, (float) cap_height / 2, this.seed, RANDOM_COLOR);
			decos.add(d);
		}

		//	  qs.index_list.reserve(points);
		//Add the outer walls
		//	  for (i = 0; i < points; i++)
		//	    qs.index_list.push_back(i);

		//	  this.mesh->QuadStripAdd (qs);
		//	  this.mesh_flat->QuadStripAdd (qs);

		//add the fan to cap the top of the buildings
		//	  f.index_list.push_back(points);

		//	    f.index_list.push_back(points - (1 + i * 2));
		p.x = this.center.x;
		p.z = this.center.z;
		//	  this.mesh_flat->VertexAdd (multiTexturePathPattern);
		//	  this.mesh_flat->FanAdd (f);

		radius.scale(0.5f);
		//not my comment//ConstructRoof ((int)(_center.x - radius), (int)(_center.x + radius), (int)(_center.z - radius), (int)(_center.z + radius), this.height + cap_height);
		//	  this.mesh->Compile ();
		//	  this.mesh_flat->Compile ();
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

				//cap

				vertices.add(new Vector3f(buffer.get(j + 1)));
				texCoords.add(new Vector2f((j + 1) * fac, 0));

				vertices.add(new Vector3f(buffer.get(j + 3)));
				texCoords.add(new Vector2f((j + 3) * fac, 1));

				vertices.add(new Vector3f(p));//middle
				texCoords.add(new Vector2f((j) * fac, 0));
			}
		}
	}

	/*-----------------------------------------------------------------------------

	  This makes a big chunky building of intersecting cubes.

	-----------------------------------------------------------------------------*/

	void CreateSimple() {

		Vector3f p;
		Vector2f uv;
		float x1, x2, z1, z2, y1, y2;
		//	  quad_strip  qs;
		float u, v1, v2;
		float cap_height;
		float ledge;

		//	  for(int i=0; i<=10; i++)
		//	    qs.index_list.push_back(i);

		//How tall the flat-color roof is
		cap_height = (1 + random.nextInt(4));
		//how much the ledge sticks out
		ledge = random.nextInt(10) / 30.0f;

		x1 = this.x;
		x2 = (this.x + this.width);
		y1 = 0.0f;
		y2 = this.height;
		z2 = this.y;
		z1 = (this.y + this.depth);

		u = (float) (random.nextInt(BuildingTexture.SEGMENTS_PER_TEXTURE / 2)
				+ BuildingTexture.SEGMENTS_PER_TEXTURE / 2) / BuildingTexture.SEGMENTS_PER_TEXTURE;
		v1 = (float) (random.nextInt(BuildingTexture.SEGMENTS_PER_TEXTURE)) / BuildingTexture.SEGMENTS_PER_TEXTURE;
		v2 = v1 + 1;//(float)this.height * BuildingTexture.ONE_SEGMENT;


		/* Front Side of the Cube with different shades of colors */

		vertices.add(new Vector3f(x1, y1, z1)); // Top Right
		texCoords.add(new Vector2f(u, v1));
		vertices.add(new Vector3f(x2, y1, z1)); // Top Left
		texCoords.add(new Vector2f(2 * u, v1));
		vertices.add(new Vector3f(x2, y2, z1)); // Bottom Left
		texCoords.add(new Vector2f(2 * u, v2));

		vertices.add(new Vector3f(x2, y2, z1)); // Bottom Left
		texCoords.add(new Vector2f(2 * u, v2));
		vertices.add(new Vector3f(x1, y2, z1)); // Bottom Right
		texCoords.add(new Vector2f(u, v2));
		vertices.add(new Vector3f(x1, y1, z1)); // Top Right
		texCoords.add(new Vector2f(u, v1));



		/* BackSide of the Cube with different shades of colors */

		vertices.add(new Vector3f(x1, y2, z2)); // Top Right
		texCoords.add(new Vector2f(u, v2));
		vertices.add(new Vector3f(x2, y2, z2)); // Top Left
		texCoords.add(new Vector2f(u * 2, v2));
		vertices.add(new Vector3f(x2, y1, z2)); // Bottom Left
		texCoords.add(new Vector2f(u * 2, v1));

		vertices.add(new Vector3f(x2, y1, z2)); // Bottom Left
		texCoords.add(new Vector2f(u * 2, v1));
		vertices.add(new Vector3f(x1, y1, z2)); // Bottom Right
		texCoords.add(new Vector2f(u, v1));
		vertices.add(new Vector3f(x1, y2, z2)); // Top Right
		texCoords.add(new Vector2f(u, v2));
		float fac = 0;
		if (depth > width) {
			fac = depth / width;
		} else {
			fac = width / depth;
		}

		//	  u *= fac;
		//	  v1 *= fac;
		//	  v2 *= fac;

		/* LeftSide of the Cube with different shades of colors */

		vertices.add(new Vector3f(x2, y1, z1)); // Top Right
		texCoords.add(new Vector2f(1, v1));
		vertices.add(new Vector3f(x2, y1, z2)); // Top Left
		texCoords.add(new Vector2f(1, v2));
		vertices.add(new Vector3f(x2, y2, z2)); // Bottom Left
		texCoords.add(new Vector2f(1 * 2, v2));

		vertices.add(new Vector3f(x2, y2, z2)); // Bottom Left
		texCoords.add(new Vector2f(1 * 2, v2));
		vertices.add(new Vector3f(x2, y2, z1)); // Bottom Right
		texCoords.add(new Vector2f(1 * 2, v1));
		vertices.add(new Vector3f(x2, y1, z1)); // Top Right
		texCoords.add(new Vector2f(1, v1));

		/* Right Side of the Cube with different shades of colors */

		vertices.add(new Vector3f(x1, y1, z2)); // Top Right
		texCoords.add(new Vector2f(1, v2));
		vertices.add(new Vector3f(x1, y1, z1)); // Top Left
		texCoords.add(new Vector2f(1, v1));
		vertices.add(new Vector3f(x1, y2, z1)); // Bottom Left
		texCoords.add(new Vector2f(2, v1));

		vertices.add(new Vector3f(x1, y2, z1)); // Bottom Left
		texCoords.add(new Vector2f(2, v1));
		vertices.add(new Vector3f(x1, y2, z2)); // Bottom Right
		texCoords.add(new Vector2f(2, v2));
		vertices.add(new Vector3f(x1, y1, z2)); // Top Right
		texCoords.add(new Vector2f(1, v2));

		//	  multiTexturePathPattern = new Vector3f  (x1, y1, z1);  uv = new Vector2f  (u, v1);
		////	  this.mesh->VertexAdd (multiTexturePathPattern);
		//
		//	  multiTexturePathPattern = new Vector3f  (x1, y2, z1);  uv = new Vector2f  (u, v2);
		////	  this.mesh->VertexAdd (multiTexturePathPattern);
		//
		//	  u += (float)this.depth / SEGMENTS_PER_TEXTURE;
		//
		//	  multiTexturePathPattern = new Vector3f (x1, y1, z2);  uv = new Vector2f (u, v1);
		////	  this.mesh->VertexAdd (multiTexturePathPattern);
		//
		//	  multiTexturePathPattern = new Vector3f (x1, y2, z2);  uv = new Vector2f (u, v2);
		////	  this.mesh->VertexAdd (multiTexturePathPattern);
		//
		//	  u += (float)this.width / SEGMENTS_PER_TEXTURE;
		//
		//	  multiTexturePathPattern = new Vector3f (x2, y1, z2);  uv = new Vector2f (u, v1);
		////	  this.mesh->VertexAdd (multiTexturePathPattern);
		//
		//	  multiTexturePathPattern = new Vector3f (x2, y2, z2);  uv = new Vector2f (u, v2);
		////	  this.mesh->VertexAdd (multiTexturePathPattern);
		//
		//	  u += (float)this.depth / SEGMENTS_PER_TEXTURE;
		//
		//	  multiTexturePathPattern = new Vector3f (x2, y1, z1);  uv = new Vector2f (u, v1);
		////	  this.mesh->VertexAdd (multiTexturePathPattern);
		//
		//	  multiTexturePathPattern = new Vector3f (x2, y2, z1); uv = new Vector2f (u, v2);
		////	  this.mesh->VertexAdd (multiTexturePathPattern);
		//
		//	  u += (float)this.depth / SEGMENTS_PER_TEXTURE;
		//
		//	  multiTexturePathPattern = new Vector3f (x1, y1, z1);  uv = new Vector2f (u, v1);
		////	  this.mesh->VertexAdd (multiTexturePathPattern);
		//
		//	  multiTexturePathPattern = new Vector3f (x1, y2, z1);  uv = new Vector2f (u, v2);
		//	  this.mesh->VertexAdd (multiTexturePathPattern);

		//	  this.mesh->QuadStripAdd (qs);
		ConstructCube(x1 - ledge, x2 + ledge, z2 - ledge, z1 + ledge, this.height, this.height + cap_height);
		//	  this.mesh->Compile ();

	}

	/*-----------------------------------------------------------------------------

	  A single-cube building.  Good for low-rise buildings and stuff that will be
	  far from the camera;

	-----------------------------------------------------------------------------*/

	void CreateTower() {

		int left, right, front, back, bottom;
		int section_height, section_width, section_depth;
		int remaining_height;
		int ledge_height;
		int tier_fraction;
		int grouping;
		int foundation;
		int narrowing_interval;
		int tiers;
		float ledge;
		float uv_start;
		boolean blank_corners;
		boolean roof_spike;
		boolean tower;

		//How much ledges protrude from the building
		ledge = random.nextInt(3) * 0.25f;
		//How tall the ledges are, in stories
		ledge_height = random.nextInt(4) + 1;
		//How the windows are grouped
		grouping = random.nextInt(3) + 2;
		//if the corners of the building have no windows
		blank_corners = random.nextInt(4) > 0;
		//if the roof is pointed or has infrastructure on it
		roof_spike = random.nextInt(3) == 0;
		//What fraction of the remaining height should be given to each tier
		tier_fraction = 2 + random.nextInt(4);
		//How often (in tiers) does the building get narrorwer?
		narrowing_interval = 1 + random.nextInt(10);
		//The height of the windowsless slab at the bottom
		foundation = 2 + random.nextInt(3);
		//The odds that we'll have a big fancy spikey top
		tower = random.nextInt(5) != 0 && this.height > 40;
		//set our initial parameters
		left = this.x;
		right = this.x + this.width;
		front = this.y;
		back = this.y + this.depth;
		bottom = 0;
		tiers = 0;
		//build the foundations.
		ConstructCube(left - ledge, right + ledge, front - ledge, back + ledge, bottom, foundation);
		bottom += foundation;
		//now add tiers until we reach the top
		while (true) {
			remaining_height = this.height - bottom;
			section_depth = back - front;
			section_width = right - left;
			section_height = Math.max(remaining_height / tier_fraction, 2);
			if (remaining_height < 10) {
				section_height = remaining_height;
			}
			//Build the four walls
			uv_start = (float) random.nextInt(BuildingTexture.SEGMENTS_PER_TEXTURE) / BuildingTexture.SEGMENTS_PER_TEXTURE;
			uv_start = ConstructWall(left, bottom, back, SOUTH, section_depth, section_height, grouping, uv_start, blank_corners) - BuildingTexture.ONE_SEGMENT;
			uv_start = ConstructWall(left, bottom, front, EAST, section_width, section_height, grouping, uv_start, blank_corners) - BuildingTexture.ONE_SEGMENT;
			uv_start = ConstructWall(right, bottom, front, NORTH, section_depth, section_height, grouping, uv_start, blank_corners) - BuildingTexture.ONE_SEGMENT;
			uv_start = ConstructWall(right, bottom, back, WEST, section_width, section_height, grouping, uv_start, blank_corners) - BuildingTexture.ONE_SEGMENT;
			bottom += section_height;
			//Build the slab / ledges to cap this section.
			if (bottom + ledge_height > this.height) {
				break;
			}
			ConstructCube(left - ledge, right + ledge, front - ledge, back + ledge, bottom, (bottom + ledge_height));
			bottom += ledge_height;
			if (bottom > this.height) {
				break;
			}
			tiers++;
			if ((tiers % narrowing_interval) == 0) {
				if (section_width > 7) {
					left += 1;
					right -= 1;
				}
				if (section_depth > 7) {
					front += 1;
					back -= 1;
				}
			}
		}
		ConstructRoof(left, right, front, back, bottom);
		//	  this.mesh->Compile ();
		//	  this.mesh_flat->Compile ();

	}


	/*-----------------------------------------------------------------------------

	  This makes a deformed cylinder building.

	-----------------------------------------------------------------------------*/

	private float MathDistance(float x, float z, float x2, float z2) {

		Vector2f a = new Vector2f(x, z);
		Vector2f b = new Vector2f(x2, z2);

		b.sub(a);
		return b.length();
	}

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/
	public void testDraw() {
		//		GL11.glShadeModel(GL11.GL_FLAT);
		//		GL11.glCullFace(GL11.GL_BACK);
		//		GlUtil.glEnable(GL11.GL_CULL_FACE);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, Texture());
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		//		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
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
		for (CDeco d : decos) {
			GlUtil.glPushMatrix();
			d.testDraw();
			GlUtil.glPopMatrix();
		}

	}

	int Texture() {

		return BuildingTexture.head.TextureRandomBuilding(texture_type);

	}

}
