package org.schema.game.client.data.city;

import java.awt.Color;
import java.nio.IntBuffer;
import java.util.Random;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.texture.TextureLoader;

public class BuildingTexture {
	public static final int SEGMENTS_PER_TEXTURE = 32;
	public static final float ONE_SEGMENT = (1.0f / SEGMENTS_PER_TEXTURE);
	public static final int LANES_PER_TEXTURE = 8;
	public static final float LANE_SIZE = (1.0f / LANES_PER_TEXTURE);
	public static final int TRIM_RESOLUTION = 256;
	public static final int TRIM_ROWS = 4;
	public static final float TRIM_SIZE = (1.0f / TRIM_ROWS);
	public static final int TRIM_PIXELS = (TRIM_RESOLUTION / TRIM_ROWS);
	public static final int LOGO_RESOLUTION = 512;
	public static final int LOGO_ROWS = 16;
	public static final float LOGO_SIZE = (1.0f / LOGO_ROWS);
	public static final int LOGO_PIXELS = (LOGO_RESOLUTION / LOGO_ROWS);
	public static final int TEXTURE_LIGHT = 0,
			TEXTURE_SOFT_CIRCLE = 1,
			TEXTURE_SKY = 2,
			TEXTURE_LOGOS = 3,
			TEXTURE_TRIM = 4,
			TEXTURE_BLOOM = 5,
			TEXTURE_HEADLIGHT = 6,
			TEXTURE_LATTICE = 7,
			TEXTURE_BUILDING1 = 8,
			TEXTURE_BUILDING2 = 9,
			TEXTURE_BUILDING3 = 10,
			TEXTURE_BUILDING4 = 11,
			TEXTURE_BUILDING5 = 12,
			TEXTURE_BUILDING6 = 13,
			TEXTURE_BUILDING7 = 14,
			TEXTURE_BUILDING8 = 15,
			TEXTURE_BUILDING9 = 16,
			TEXTURE_COUNT = 17;
	public static final int BUILDING_COUNT = ((TEXTURE_BUILDING9 - TEXTURE_BUILDING1) + 1);
	public static float SKY_BANDS = 1; //(sky_pos. / sizeof (int));
	static String prefix[] =
			{
					"i",
					"Green ",
					"Mega",
					"Super ",
					"Omni",
					"e",
					"Hyper",
					"Global ",
					"Vital",
					"Next ",
					"Pacific ",
					"Metro",
					"Unity ",
					"G-",
					"Trans",
					"Infinity ",
					"Superior ",
					"Monolith ",
					"Best ",
					"Atlantic ",
					"First ",
					"Union ",
					"National ",
			};
	public static int PREFIX_COUNT = (prefix.length);
	static String name[] =
			{
					"Biotic",
					"Info",
					"Data",
					"Solar",
					"Aerospace",
					"Motors",
					"Nano",
					"Online",
					"Circuits",
					"Energy",
					"Med",
					"Robotic",
					"Exports",
					"Security",
					"Systems",
					"Financial",
					"Industrial",
					"Media",
					"Materials",
					"Foods",
					"Networks",
					"Shipping",
					"Tools",
					"Medical",
					"Publishing",
					"Enterprises",
					"Audio",
					"Health",
					"Bank",
					"Imports",
					"Apparel",
					"Petroleum",
					"Studios",
			};
	public static int NAME_COUNT = (name.length);
	static String suffix[] =
			{
					"Corp",
					" Inc.",
					"Co",
					"PlanetSurface",
					".Com",
					" USA",
					" Ltd.",
					"Net",
					" Tech",
					" Labs",
					" Mfg.",
					" UK",
					" Unlimited",
					" One",
					" LLC"
			};
	public static int SUFFIX_COUNT = (suffix.length);
	static BuildingTexture head;
	static boolean textures_done;
	//	public static final int  LANE_PIXELS           =(_size / LANES_PER_TEXTURE);
	static boolean[] prefix_used = new boolean[PREFIX_COUNT];
	static boolean[] name_used = new boolean[NAME_COUNT];
	static boolean[] suffix_used = new boolean[SUFFIX_COUNT];
	static int build_time;
	/*-----------------------------------------------------------------------------

	  Here is where ALL of the procedural textures are created.  It's filled with
	  obscure logic, magic numbers, and messy code. Part of this is because
	  there is a lot of "art" being done here, and lots of numbers that could be
	  endlessly tweaked.  Also because I'm lazy.

	-----------------------------------------------------------------------------*/
	static IntBuffer viewport = MemoryUtil.memAllocInt(16);
	/*-----------------------------------------------------------------------------

	  TextureNew.cpp

	  2009 Shamus Young

	-------------------------------------------------------------------------------

	  This procedurally builds all of the textures.

	  I apologize in advance for the apalling state of this module. It's the victim
	  of iterative and experimental development.  It has cruft, poorly named
	  functions, obscure code, poorly named variables, and is badly organized. Even
	  the formatting sucks in places. Its only saving grace is that it works.

	-----------------------------------------------------------------------------*/
	private static Random random = new Random();
	public static float RANDOM_COLOR_SHIFT = ((random.nextInt(10)) / 50.0f);
	public static float RANDOM_COLOR_VAL = ((random.nextInt(256)) / 256.0f);
	public static float RANDOM_COLOR_LIGHT = ((200 + random.nextInt(56)) / 256.0f);
	int _my_id;
	int _glid;
	int _desired_size;
	int _size;
	int _half;
	int _segment_size;
	boolean _ready;
	boolean _masked;
	boolean _mipmap;
	boolean _clamp;

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/
	private BuildingTexture _next;


	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	public BuildingTexture(int id, int size, boolean mipmap, boolean clamp, boolean masked) {

		//	  glGenTextures (1, &_glid);

		_my_id = id;
		_mipmap = mipmap;
		_clamp = clamp;
		_masked = masked;
		_desired_size = size;
		_size = size;
		_half = size / 2;
		_segment_size = (size / BuildingTexture.SEGMENTS_PER_TEXTURE);
		_ready = false;
		_next = head;
		head = this;

	}


	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	static void do_bloom(BuildingTexture t) {

		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL11.glViewport(0, 0, t._size, t._size);
		GL11.glCullFace(GL11.GL_BACK);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glDepthMask(true);
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		//	  GlUtil.glEnable (GL11.GL_FOG);
		//	  GL11.glFogf (GL11.GL_FOG_START, RenderFogDistance () / 2);
		//	  GL11.glFogf (GL11.GL_FOG_END, RenderFogDistance ());
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		//	  GL11.glClearColor (0.6f, 0.6f, 0.6f, 0.6f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		//	  EntityRender ();
		//	  CarRender ();
		//	  LightRender ();
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, t._glid);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glCopyTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, 0, 0, t._size, t._size, 0);

	}

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	static void drawrect(int left, int top, int right, int bottom, Vector4f color) {

		float average;
		float hue;
		int potential;
		int repeats;
		int height;
		int i, j;
		boolean bright;
		Vector4f color_noise;

		GlUtil.glDisable(GL11.GL_CULL_FACE);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		//	  GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		//	  GlUtil.glBlendFunc (GL11.GL_ONE, GL11.GL_ONE);
		GL11.glLineWidth(1.0f);
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		GL11.glColor4f(color.x, color.y, color.z, color.w); // glColor3fv (&color1.red);

		if (left == right) { //in low resolution, a "rect" might be 1 pixel wide
			GL11.glBegin(GL11.GL_LINES);
			GL11.glVertex2i(left, top);
			GL11.glVertex2i(left, bottom);
			GL11.glEnd();
		}
		if (top == bottom) { //in low resolution, a "rect" might be 1 pixel wide
			GL11.glBegin(GL11.GL_LINES);
			GL11.glVertex2i(left, top);
			GL11.glVertex2i(right, top);
			GL11.glEnd();
		} else { // draw one of those fancy 2-dimensional rectangles

			GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex2i(left, top);
			GL11.glVertex2i(right, top);
			GL11.glVertex2i(right, bottom);
			GL11.glVertex2i(left, bottom);
			GL11.glEnd();

			average = (color.x + color.y + color.z) / 3.0f;
			bright = average > 0.5f;
			potential = (int) (average * 255.0f);

			if (bright) {
				GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				//	      GlUtil.glBlendFunc (GL11.GL_ONE, GL11.GL_ONE);
				//	      GL11.glPointSize(10);
				GL11.glBegin(GL11.GL_POINTS);
				for (i = left + 1; i < right - 1; i++) {
					for (j = top + 1; j < bottom - 1; j++) {
						GL11.glColor4f(255, 0, random.nextInt(potential), 255);
						hue = 0.2f + random.nextInt(100) / 300.0f + random.nextInt(100) / 300.0f + random.nextInt(100) / 300.0f;
						Color hsBtoRGB = Color.getHSBColor(hue, 0.3f, 0.5f);
						color_noise = new Vector4f(hsBtoRGB.getRed(), hsBtoRGB.getGreen(), hsBtoRGB.getBlue(), hsBtoRGB.getAlpha());
						color_noise.w = random.nextInt(potential) / 144.0f;
						GL11.glColor4f(RANDOM_COLOR_VAL, RANDOM_COLOR_VAL, RANDOM_COLOR_VAL, random.nextInt(potential) / 144.0f);
						GL11.glColor4f(color_noise.x, color_noise.y, color_noise.z, color_noise.w);
						GL11.glVertex2i(i, j);
					}
				}
				GL11.glEnd();
			}
			repeats = random.nextInt(6) + 1;
			height = (bottom - top) + (random.nextInt(3) - 1) + (random.nextInt(3) - 1);
			for (i = left; i < right; i++) {
				if (random.nextInt(3) == 0) {
					repeats = random.nextInt(4) + 1;
				}
				if (random.nextInt(6) == 0) {
					height = bottom - top;
					height = (int) (random.nextInt(Math.abs(height + 1)) * Math.signum(height));
					height = (int) (random.nextInt(Math.abs(height + 1)) * Math.signum(height));
					height = (int) (random.nextInt(Math.abs(height + 1)) * Math.signum(height));
					height = ((bottom - top) + height) / 2;
				}
				for (j = 0; j < 1; j++) {
					GL11.glBegin(GL11.GL_LINES);
					GL11.glColor4f(0, 0, 0, random.nextInt(256) / 256.0f);
					GL11.glVertex2i(i, bottom - height);
					GL11.glColor4f(0, 0, 0, random.nextInt(256) / 256.0f);
					GL11.glVertex2i(i, bottom);
					GL11.glEnd();
				}
			}
		}
	}

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	static void drawrect_simple(int left, int top, int right, int bottom, Vector4f color) {

		GL11.glColor4f(color.x, color.y, color.z, color.w); // glColor3fv (&color1.red);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2i(left, top);
		GL11.glVertex2i(right, top);
		GL11.glVertex2i(right, bottom);
		GL11.glVertex2i(left, bottom);
		GL11.glEnd();

	}

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	static void drawrect_simple(int left, int top, int right, int bottom, Vector4f color1, Vector4f color2) {

		GL11.glColor4f(color1.x, color1.y, color1.z, color1.w); // glColor3fv (&color1.red);
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
		GL11.glVertex2i((left + right) / 2, (top + bottom) / 2);
		GL11.glColor4f(color2.x, color2.y, color2.z, color2.w); // glColor3fv (&color2.red);
		GL11.glVertex2i(left, top);
		GL11.glVertex2i(right, top);
		GL11.glVertex2i(right, bottom);
		GL11.glVertex2i(left, bottom);
		GL11.glVertex2i(left, top);
		GL11.glEnd();

	}

	/*-----------------------------------------------------------------------------

	  This draws all of the windows on a building texture. lit_density controls
	  how many lights are on. (1 in n chance that the light is on. Higher values
	  mean less lit windows. run_length controls how often it will consider
	  changing the lit / unlit status. 1 produces a complete scatter, higher
	  numbers make long strings of lights.

	-----------------------------------------------------------------------------*/

	private static int RenderBloom() {

		return 0;
	}


	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	public static int TextureId(int id) {

		for (BuildingTexture t = head; t != null; t = t._next) {
			if (t._my_id == id) {
				return t._glid;
			}
		}
		return 0;

	}

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	public static void TextureInit() {

		//	  new BuildingTexture (  TEXTURE_SKY,          512,  true,  false, false);
		new BuildingTexture(TEXTURE_LATTICE, 128, true, true, true);
		new BuildingTexture(TEXTURE_LIGHT, 128, false, false, true);
		new BuildingTexture(TEXTURE_SOFT_CIRCLE, 128, false, false, true);
		new BuildingTexture(TEXTURE_HEADLIGHT, 128, false, false, true);
		new BuildingTexture(TEXTURE_TRIM, TRIM_RESOLUTION, true, false, false);
		new BuildingTexture(TEXTURE_LOGOS, LOGO_RESOLUTION, true, false, true);
		for (int i = TEXTURE_BUILDING1; i <= TEXTURE_BUILDING9; i++) {
			new BuildingTexture(i, 512, true, false, false);
		}
		new BuildingTexture(TEXTURE_BLOOM, 512, true, false, false);
		int names = PREFIX_COUNT * NAME_COUNT + SUFFIX_COUNT * NAME_COUNT;
		System.out.println("BUILDING TEXTURES DONE");

	}

	public static void TextureUpdate() {

		if (textures_done) {
			if (RenderBloom() == 0) {
				return;
			}

			for (BuildingTexture t = head; t != null; t = t._next) {
				if (t._my_id != TEXTURE_BLOOM) {
					continue;
				}
				do_bloom(t);
				return;
			}
		}
		for (BuildingTexture t = head; t != null; t = t._next) {
			if (!t._ready) {
				t.Rebuild();
				GlUtil.printGlErrorCritical();
				//	      return;
			}

		}
		//	  textures_done = true;

	}

	static void window(int x, int y, int size, int id, Vector4f color) {

		int margin;
		int half;
		int i;

		margin = size / 3;
		half = size / 2;
		switch(id) {
			case TEXTURE_BUILDING1 -> //filled, 1-pixel frame
				drawrect(x + 1, y + 1, x + size - 1, y + size - 1, color);
			case TEXTURE_BUILDING2 -> //vertical
				drawrect(x + margin, y + 1, x + size - margin, y + size - 1, color);
			case TEXTURE_BUILDING3 -> { //side-by-side pair
				drawrect(x + 1, y + 1, x + half - 1, y + size - margin, color);
				drawrect(x + half + 1, y + 1, x + size - 1, y + size - margin, color);
			}
			case TEXTURE_BUILDING4 -> { //windows with blinds
				drawrect(x + 1, y + 1, x + size - 1, y + size - 1, color);
				i = random.nextInt(size - 2);
				color.scale(0.3f);
				drawrect(x + 1, y + 1, x + size - 1, y + i + 1, color);
			}
			case TEXTURE_BUILDING5 -> { //vert stripes
				drawrect(x + 1, y + 1, x + size - 1, y + size - 1, color);
				color.scale(0.7f);
				drawrect(x + margin, y + 1, x + margin, y + size - 1, color);
				color.scale(0.3f);
				drawrect(x + size - margin - 1, y + 1, x + size - margin - 1, y + size - 1, color);
			}
			case TEXTURE_BUILDING6 -> //wide horz line
				drawrect(x + 1, y + 1, x + size - 1, y + size - margin, color);
			case TEXTURE_BUILDING7 -> { //4-pane
				drawrect(x + 2, y + 1, x + size - 1, y + size - 1, color);
				color.scale(0.2f);
				drawrect(x + 2, y + half, x + size - 1, y + half, color);
				color.scale(0.2f);
				drawrect(x + half, y + 1, x + half, y + size - 1, color);
			}
			case TEXTURE_BUILDING8 -> // Single narrow window
				drawrect(x + half - 1, y + 1, x + half + 1, y + size - margin, color);
			case TEXTURE_BUILDING9 -> //horizontal
				drawrect(x + 1, y + margin, x + size - 1, y + size - margin - 1, color);
		}

	}

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	private void cleanUp() {
		System.err.println("cleanup not implemented");

	}

	void DrawHeadlight() {

		float radius;
		int i, x, y;
		Vector2f pos = new Vector2f();

		//Make a simple circle of light, bright in the center and fading out
		radius = ((float) _half) - 20;
		x = _half - 20;
		y = _half;
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
		GL11.glColor4f(0.8f, 0.8f, 0.8f, 0.6f);
		GL11.glVertex2i(_half - 5, y);
		GL11.glColor4f(0, 0, 0, 0);
		for (i = 0; i <= 360; i += 36) {
			float radians = (float) Math.toRadians(i % 360);
			pos.x = (float) Math.sin(radians * radius);
			pos.y = (float) Math.cos(radians * radius);
			GL11.glVertex2i(x + (int) pos.x, _half + (int) pos.y);
		}
		GL11.glEnd();
		x = _half + 20;
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
		GL11.glColor4f(0.8f, 0.8f, 0.8f, 0.6f);
		GL11.glVertex2i(_half + 5, y);
		GL11.glColor4f(0, 0, 0, 0);
		for (i = 0; i <= 360; i += 36) {
			float radians = (float) Math.toRadians(i % 360);
			pos.x = (float) Math.sin(radians * radius);
			pos.y = (float) Math.cos(radians * radius);
			GL11.glVertex2i(x + (int) pos.x, _half + (int) pos.y);
		}
		GL11.glEnd();
		x = _half - 6;
		Vector4f white = new Vector4f(1, 1, 1, 1);
		drawrect_simple(x - 3, y - 2, x + 2, y + 2, white);
		x = _half + 6;
		drawrect_simple(x - 2, y - 2, x + 3, y + 2, white);

	}

	void DrawSky() {

		//	  Vector4f          color;
		//	  float           grey;
		//	  float           scale, inv_scale;
		//	  int             i, x, y;
		//	  int             width, height;
		//	  int             offset;
		//	  int             width_adjust;
		//	  int             height_adjust;
		//
		//	  color = new Vector4f(0.9f,0.9f,0.9f,0.7f);
		//	  grey = (color.x + color.y + color.z) / 3.0f;
		//	  //desaturate, slightly dim
		//	  color = (color + glRgba (grey) * 2.0f) / 15.0f;
		//	  glDisable (GL_BLEND);
		//	  glBegin (GL_QUAD_STRIP);
		//	  glColor3f (0,0,0);
		//	  glVertex2i (0, _half);
		//	  glVertex2i (_size, _half);
		//	  glColor3fv (&color.red);
		//	  glVertex2i (0, _size - 2);
		//	  glVertex2i (_size, _size - 2);
		//	  glEnd ();
		//	  //Draw a bunch of little faux-buildings on the horizon.
		//	  for (i = 0; i < _size; i += 5)
		//	    drawrect (i, _size - RandomVal (8) - RandomVal (8) - RandomVal (8), i + RandomVal (9), _sizeRgba (0.0f));
		//	  //Draw the clouds
		//	  for (i = _size - 30; i > 5; i -= 2) {
		//
		//	    x = RandomVal (_size);
		//	    y = i;
		//
		//	    scale = 1.0f - ((float)y / (float)_size);
		//	    width = RandomVal (_half / 2) + (int)((float)_half * scale) / 2;
		//	    scale = 1.0f - (float)y / (float)_size;
		//	    height = (int)((float)(width) * scale);
		//	    height = MAX (height, 4);
		//
		//	    glEnable (GL_BLEND);
		//	    glBlendFunc (GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		//	    glDisable (GL_CULL_FACE);
		//	    glEnable (GL_TEXTURE_2D);
		//	    glBindTexture (GL_TEXTURE_2D, TextureId (TEXTURE_SOFT_CIRCLE));
		//	    glDepthMask (false);
		//	    glBegin (GL_QUADS);
		//	    for (offset = -_size; offset <= _size; offset += _size) {
		//	      for (scale = 1.0f; scale > 0.0f; scale -= 0.25f) {
		//
		//	        inv_scale = 1.0f - (scale);
		//	        if (scale < 0.4f)
		//	          color = WorldBloomColor () * 0.1f;
		//	        else
		//	          color = glRgba (0.0f);
		//	        color.alpha = 0.2f;
		//	        glColor4fv (&color.red);
		//	        width_adjust = (int)((float)width / 2.0f + (int)(inv_scale * ((float)width / 2.0f)));
		//	        height_adjust = height + (int)(scale * (float)height * 0.99f);
		//	        glTexCoord2f (0, 0);   glVertex2i (offset + x - width_adjust, y + height - height_adjust);
		//	        glTexCoord2f (0, 1);   glVertex2i (offset + x - width_adjust, y + height);
		//	        glTexCoord2f (1, 1);   glVertex2i (offset + x + width_adjust, y + height);
		//	        glTexCoord2f (1, 0);   glVertex2i (offset + x + width_adjust, y + height - height_adjust);
		//	      }
		//
		//	    }
		//	  }
		//	  glEnd ();

	}

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	void DrawWindows() {

		int x, y;
		int run = 0;
		int run_length = 0;
		int lit_density = 0;
		Vector4f color;
		boolean lit = false;

		//color = glRgbaUnique (_my_id);
		for (y = 0; y < BuildingTexture.SEGMENTS_PER_TEXTURE; y++) {
			//Every few floors we change the behavior
			if ((y % 8) == 0) { //if (!y % 8)
				run = 0;
				run_length = random.nextInt(9) + 2;
				lit_density = 2 + random.nextInt(2) + random.nextInt(2);
				lit = false;
			}
			for (x = 0; x < BuildingTexture.SEGMENTS_PER_TEXTURE; x++) {
				//if this run is over reroll lit and start a new one
				if (run < 1) {
					run = random.nextInt(run_length);
					lit = random.nextInt(lit_density) == 0;
					//if (lit)
					//color = glRgba (0.5f + (float)(RandomVal () % 128) / 256.0f) + glRgba (RANDOM_COLOR_SHIFT, RANDOM_COLOR_SHIFT, RANDOM_COLOR_SHIFT);
				}
				if (lit) {
					float c = (0.5f + (((float) random.nextInt() % 128) / 256.0f));
					//	    	System.err.println("lit color "+c);
					color = new Vector4f(c, c, c, 1);
					color.add(new Vector4f(((random.nextInt(10)) / 50.0f), ((random.nextInt(10)) / 50.0f), ((random.nextInt(10)) / 50.0f), 0));
				} else {
					float c = (((random.nextInt() % 40) / 256.0f));
					color = new Vector4f(c, c, c, 1);
				}
				window(x * _segment_size, y * _segment_size, _segment_size, _my_id, color);
				run--;

			}
		}

	}

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	void Rebuild() {
		viewport.rewind();
//		GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
		viewport.rewind();
		System.err.println("stated texture creation " + _size + " x " + _size);
		int i, j;
		int x, y;
		int name_num, prefix_num, suffix_num;
		int max_size;
		float radius;
		Vector2f pos = new Vector2f();
		boolean use_framebuffer;
		long start;
		long lapsed;

		start = System.currentTimeMillis();
		//Since we make textures by drawing into the viewport, we can'transformationArray make them bigger
		//than the current view.
		_size = _desired_size;
		max_size = RenderMaxTextureSize();
		while (_size > max_size) {
			_size /= 2;
		}

		//Set up the texture
		_glid = TextureLoader.getEmptyTexture(_size, _size).getTextureId();
		System.err.println("binding to " + _glid);
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, _glid);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.printGlErrorCritical();
		//	  GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		//	  GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		//	  if (_clamp) {
		//	    GL11.glTexParameteri (GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		//	    GL11.glTexParameteri (GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		//	  }
		//Set up our viewport so that drawing into our texture will be as easy
		//as possible.  We make the viewport and projection simply match the given
		//texture size.
		GL11.glViewport(0, 0, _size, _size);
		GlUtil.glMatrixMode(GL11.GL_PROJECTION);
		GlUtil.printGlErrorCritical();
		GL11.glLoadIdentity();
		System.err.println("ortho " + _size);

		GL11.glOrtho(0, _size, _size, 0, 0.1f, 2048);
		GlUtil.printGlErrorCritical();
		GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
		GlUtil.glPushMatrix();
		GlUtil.glLoadIdentity();
		GlUtil.printGlErrorCritical();
		GlUtil.glDisable(GL11.GL_CULL_FACE);
		GlUtil.glDisable(GL11.GL_FOG);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL11.glTranslatef(0, 0, -10.0f);
		GL11.glClearColor(0, 0, 0, _masked ? 0.0f : 1.0f);
		//	  GL11.glClearColor (1, 1, 1, _masked ? 0.0f : 1.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		//	  GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		use_framebuffer = true;
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		switch(_my_id) {
			case TEXTURE_LATTICE -> {
				System.err.println("LATTICE is " + _glid);
				//		  GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				//		  GlUtil.glEnable(GL11.GL_BLEND);
				GL11.glLineWidth(2.0f);
				GL11.glColor4f(1, 1, 1, 1);
				//	    GL11.glColor4f (1,1,1,1);
				GL11.glBegin(GL11.GL_LINES);
				GL11.glVertex2i(0, 0);
				GL11.glVertex2i(_size, _size);//diagonal
				GL11.glVertex2i(0, 0);
				GL11.glVertex2i(0, _size);//vertical
				GL11.glVertex2i(0, 0);
				GL11.glVertex2i(_size, 0);//vertical
				GL11.glEnd();
				GL11.glBegin(GL11.GL_LINE_STRIP);
				GL11.glVertex2i(0, 0);
				for(i = 0; i < _size; i += 9) {
					if(i % 2 != 0) {
						GL11.glVertex2i(0, i);
					} else {
						GL11.glVertex2i(i, i);
					}
				}
				for(i = 0; i < _size; i += 9) {
					if(i % 2 != 0) {
						GL11.glVertex2i(i, 0);
					} else {
						GL11.glVertex2i(i, i);
					}
				}
				GL11.glEnd();
			}
			//	    GlUtil.glDisable(GL11.GL_BLEND);
			case TEXTURE_SOFT_CIRCLE -> {
				//Make a simple circle of light, bright in the center and fading out
				GlUtil.glEnable(GL11.GL_BLEND);
				GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				radius = ((float) _half) - 3;
				GL11.glBegin(GL11.GL_TRIANGLE_FAN);
				GL11.glColor4f(1, 1, 1, 1);
				GL11.glVertex2i(_half, _half);
				GL11.glColor4f(0, 0, 0, 0);
				for(i = 0; i <= 360; i++) {
					float radians = (float) Math.toRadians(i);
					pos.x = (float) Math.sin(radians * radius);
					pos.y = (float) Math.cos(radians * radius);
					GL11.glVertex2i(_half + (int) pos.x, _half + (int) pos.y);
				}
				GL11.glEnd();
			}
			case TEXTURE_LIGHT -> {
				GlUtil.glEnable(GL11.GL_BLEND);
				GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				radius = ((float) _half) - 3;
				for(j = 0; j < 2; j++) {
					GL11.glBegin(GL11.GL_TRIANGLE_FAN);
					GL11.glColor4f(1, 1, 1, 1);
					GL11.glVertex2i(_half, _half);
					if(j == 0) {
						radius = ((float) _half / 2);
					} else {
						radius = 8;
					}
					GL11.glColor4f(1, 1, 1, 0);
					for(i = 0; i <= 360; i++) {
						float radians = (float) Math.toRadians(i);
						pos.x = (float) Math.sin(radians * radius);
						pos.y = (float) Math.cos(radians * radius);
						GL11.glVertex2i(_half + (int) pos.x, _half + (int) pos.y);
					}
					GL11.glEnd();
				}
			}
			case TEXTURE_HEADLIGHT -> DrawHeadlight();
			case TEXTURE_LOGOS -> {
				i = 0;
				GlUtil.glDepthMask(false);
				GlUtil.glDisable(GL11.GL_BLEND);
				name_num = random.nextInt(NAME_COUNT);
				prefix_num = random.nextInt(PREFIX_COUNT);
				suffix_num = random.nextInt(SUFFIX_COUNT);
				GL11.glColor3f(1, 1, 1);
				while(i < _size) {
					//randomly use a prefix OR suffix, but not both.  Too verbose.
					if(Math.random() > 0.5) {
						RenderPrint(2, _size - i - LOGO_PIXELS / 4, random.nextInt(), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), "%s%s", prefix[prefix_num], name[name_num]);
					} else {
						RenderPrint(2, _size - i - LOGO_PIXELS / 4, random.nextInt(), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), "%s%s", name[name_num], suffix[suffix_num]);
					}
					name_num = (name_num + 1) % NAME_COUNT;
					prefix_num = (prefix_num + 1) % PREFIX_COUNT;
					suffix_num = (suffix_num + 1) % SUFFIX_COUNT;
					i += LOGO_PIXELS;
				}
			}
			case TEXTURE_TRIM -> {
				int margin;
				y = 0;
				margin = Math.max(TRIM_PIXELS / 4, 1);
				Vector4f white = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
				Vector4f gray = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
				for(x = 0; x < _size; x += TRIM_PIXELS) {
					drawrect_simple(x + margin, y + margin, x + TRIM_PIXELS - margin, y + TRIM_PIXELS - margin, white, gray);
				}
				y += TRIM_PIXELS;
				for(x = 0; x < _size; x += TRIM_PIXELS * 2) {
					drawrect_simple(x + margin, y + margin, x + TRIM_PIXELS - margin, y + TRIM_PIXELS - margin, white, gray);
				}
				y += TRIM_PIXELS;
				for(x = 0; x < _size; x += TRIM_PIXELS * 3) {
					drawrect_simple(x + margin, y + margin, x + TRIM_PIXELS - margin, y + TRIM_PIXELS - margin, white, gray);
				}
				y += TRIM_PIXELS;
				for(x = 0; x < _size; x += TRIM_PIXELS) {
					drawrect_simple(x + margin, y + margin * 2, x + TRIM_PIXELS - margin, y + TRIM_PIXELS - margin, white, gray);
				}
			}
			case TEXTURE_SKY -> DrawSky();
			default -> { //building textures
				GlUtil.glDepthMask(false);
				DrawWindows();
			}
		}
		GL11.glPopMatrix();
		//	  com.jogamp.openGL11.util.texture.Texture t = TextureIO.newTexture(GL11.GL_TEXTURE_2D);
		//Now blit the finished image into our texture
		if (use_framebuffer) {
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, _glid);

			//	    System.err.println("orig: "+_glid+", jogl "+t.getTextureObject());
			GL11.glCopyTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, 0, 0, _size, _size, 0);

			//		t.bind();
			GL11.glCopyTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, 0, 0, _size, _size, 0);

			//		GL11.glCopyTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, 0, 0, texWidth, texHeight, 0);
		}
		if (_my_id == TEXTURE_LATTICE) {
			//			  TextureIO.write(t, new FileExt("./test/test"+_glid+".png"));
		}
		//	  if (_mipmap) {
		//	    bits = (unsigned char*)malloc (_size * _size * 4);
		//	    GL11.glGetTexImage (GL11.GL_TEXTURE_2D,	0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, bits);
		//	    GL11.gluBuild2DMipmaps(GL11.GL_TEXTURE_2D, GL11.GL_RGBA, _size, _size, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, bits);
		//	    free (bits);
		//		  GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_LINEAR_MIPMAP_LINEAR);
		//		  GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_LINEAR);
		//	  } else
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		//cleanup and restore the viewport
		//	  RenderResize ();
		GL11.glViewport(viewport.get(0), viewport.get(1), viewport.get(2), viewport.get(3));
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glDepthMask(true);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_FILL);
		GL11.glClearColor(0.5f, 0.5f, 0.5f, 1);
		_ready = true;
		lapsed = System.currentTimeMillis() - start;
		build_time += lapsed;
		GlUtil.printGlErrorCritical();

	}

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	private int RenderMaxTextureSize() {
		//		System.err.println("RenderMaxTextureSize not implemented");
		return 512;
	}

	private void RenderPrint(int i, int j, int nextInt, Vector4f vector4f,
	                         String string, String string2, String string3) {
		System.err.println("RenderPrint not implemented");

	}

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	public int TextureRandomBuilding(int index) {

		index = Math.abs(index) % BUILDING_COUNT;
		//	  System.err.println("textureing building "+(TEXTURE_BUILDING1 + index));
		return TextureId(TEXTURE_BUILDING1 + index);

	}

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	boolean TextureReady() {

		return textures_done;

	}

	void TextureReset() {

		textures_done = false;
		build_time = 0;
		for (BuildingTexture t = head; true; t = t._next) {
			t.cleanUp();
		}
		//	  ZeroMemory (prefix_used, sizeof (prefix_used));
		//	  ZeroMemory (name_used, sizeof (name_used));
		//	  ZeroMemory (suffix_used, sizeof (suffix_used));

	}

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	void TextureTerm() {

		BuildingTexture t;

		while (head != null) {
			t = head._next;
			//	    free (head);
			System.err.println("impement free");
			head = t;
		}

	}

}
