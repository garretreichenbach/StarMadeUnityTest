package org.schema.game.server.controller.world.factory.regions.city;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.server.controller.world.factory.regions.Region;
import org.schema.schine.graphicsengine.forms.BoundingBox;

public class BuildingWorld {
	//Bitflags used to track how world space is being used.
	public static final int CLAIM_ROAD = 1;
	public static final int CLAIM_WALK = 2;
	public static final int CLAIM_BUILDING = 4;
	public static final int MAP_ROAD_NORTH = 8;
	public static final int MAP_ROAD_SOUTH = 16;
	public static final int MAP_ROAD_EAST = 32;
	public static final int MAP_ROAD_WEST = 64;
	public final Collection<Region> regions;
	private final int[][] world;
	private final Random random;
	public int WORLD_EDGE = 100;
	public int WORLD_SIZE = 330;
	public int WORLD_HALF = (WORLD_SIZE / 2);
	public ArrayList<Building> buildings = new ArrayList<Building>();
	public ArrayList<CDeco> decos = new ArrayList<CDeco>();
	private int modern_count;
	private int skyscrapers;
	;
	private int tower_count;
	private int blocky_count;
	private int logo_index;

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/
	private int scene_begin;

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/
	private BoundingBox hot_zone;

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	public BuildingWorld(Random random, Collection<Region> regions) {
		this.regions = regions;
		this.random = random;

		WORLD_SIZE = 100 + random.nextInt(220);
		WORLD_EDGE = WORLD_SIZE / 3;
		WORLD_HALF = (WORLD_SIZE / 2);
		world = new int[WORLD_SIZE][WORLD_SIZE];
	}

	public static int CLAMP(int a, int b, int c) {
		return (a < b ? b : (a > c ? c : a));
	}

	public static boolean coinflip() {
		return Math.random() > 0.5;
	}

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/
	static Plot make_plot(int x, int z, int width, int depth) {

		Plot p = new Plot(x, z, width, depth);
		return p;

	}

	int build_light_strip(Collection<Region> regions, int x1, int z1, int direction) {

		CDeco d;
		Vector4f color;
		int x2, z2;
		int length;
		int width, depth;
		int dir_x = 0, dir_z = 0;
		float size_adjust;

		//We adjust the size of the lights with this.
		size_adjust = 2.5f;
		//		Color c = Color.getHSBColor(0.09f,  0.99f,  0.85f);
		//		color = new Vector4f(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
		switch(direction) {
			case Building.NORTH -> {
				dir_z = 1;
				dir_x = 0;
			}
			case Building.SOUTH -> {
				dir_z = 1;
				dir_x = 0;
			}
			case Building.EAST -> {
				dir_z = 0;
				dir_x = 1;
			}
			case Building.WEST -> {
				dir_z = 0;
				dir_x = 1;
			}
		}
		//So we know we're on the corner of an intersection
		//look in the given  until we reach the end of the sidewalk
		x2 = x1;
		z2 = z1;
		length = 0;
		while (x2 > 0 && x2 < WORLD_SIZE && z2 > 0 && z2 < WORLD_SIZE) {
			if ((world[x2][z2] & CLAIM_ROAD) != 0) {
				break;
			}
			length++;
			x2 += dir_x;
			z2 += dir_z;
		}
		if (length < 10) {
			return length;
		}
		width = Math.max(Math.abs(x2 - x1), 1);
		depth = Math.max(Math.abs(z2 - z1), 1);
		d = new CDeco(this);
		if (direction == Building.EAST) {
			d.CreateLightStrip(regions, x1, z1 - size_adjust, width, depth + size_adjust, 2, null);
		} else if (direction == Building.WEST) {
			d.CreateLightStrip(regions, x1, z1, width, depth + size_adjust, 2, null);
		} else if (direction == Building.NORTH) {
			d.CreateLightStrip(regions, x1, z1, width + size_adjust, depth, 2, null);
		} else {
			d.CreateLightStrip(regions, x1 - size_adjust, z1, width + size_adjust, depth, 2, null);
		}
		return length;

	}

	void build_road(int x1, int y1, int width, int depth) {

		int lanes;
		int divider;
		int sidewalk;

		//the given rectangle defines a street and its sidewalk. See which way it goes.
		if (width > depth) {
			lanes = depth;
		} else {
			lanes = width;
		}
		//if we dont have room for both lanes and sidewalk, abort
		if (lanes < 4) {
			return;
		}
		//if we have an odd number of lanes, give the extra to a divider.
		if (lanes % 2 != 0) {
			lanes--;
			divider = 1;
		} else {
			divider = 0;
		}
		//no more than 10 traffic lanes, give the rest to sidewalks
		sidewalk = Math.max(2, (lanes - 10));
		lanes -= sidewalk;
		sidewalk /= 2;
		//take the remaining space and give half to each direction
		lanes /= 2;
		//Mark the entire rectangle as used
		claim(x1, y1, width, depth, CLAIM_WALK);
		//now place the directional roads
		if (width > depth) {
			claim(x1, y1 + sidewalk, width, lanes, CLAIM_ROAD | MAP_ROAD_WEST);
			claim(x1, y1 + sidewalk + lanes + divider, width, lanes, CLAIM_ROAD | MAP_ROAD_EAST);
		} else {
			claim(x1 + sidewalk, y1, lanes, depth, CLAIM_ROAD | MAP_ROAD_SOUTH);
			claim(x1 + sidewalk + lanes + divider, y1, lanes, depth, CLAIM_ROAD | MAP_ROAD_NORTH);
		}

	}

	void claim(int x, int y, int width, int depth, int val) {

		int xx, yy;

		for (xx = x; xx < (x + width); xx++) {
			for (yy = y; yy < (y + depth); yy++) {
				world[CLAMP(xx, 0, WORLD_SIZE - 1)][CLAMP(yy, 0, WORLD_SIZE - 1)] |= val;
			}
		}

	}

	boolean claimed(int x, int y, int width, int depth) {

		int xx, yy;

		for (xx = x; xx < x + width; xx++) {
			for (yy = y; yy < y + depth; yy++) {
				if (world[CLAMP(xx, 0, WORLD_SIZE - 1)][CLAMP(yy, 0, WORLD_SIZE - 1)] != 0) {
					return true;
				}
			}
		}
		return false;

	}

	void do_building(Plot p) {

		int height;
		int seed;
		int area;
		int type;
		Vector4f color;
		boolean square;

		//now we know how big the rectangle Plot is.
		area = p.width * p.depth;
		color = new Vector4f(0.9f, 0.9f, 0.9f, 1);//WorldLightColor (random.nextInt ());
		seed = random.nextInt();
		//Make sure the Plot is big enough for a building
		if (p.width < 10 || p.depth < 10) {
			return;
			//If the area is too big for one building, sub-divide it.
		}

		if (area > 800) {
			if (coinflip()) {
				p.width /= 2;
				if (coinflip()) {
					do_building(make_plot(p.x, p.z, p.width, p.depth));
				} else {
					do_building(make_plot(p.x + p.width, p.z, p.width, p.depth));
				}
				return;
			} else {
				p.depth /= 2;
				if (coinflip()) {
					do_building(make_plot(p.x, p.z, p.width, p.depth));
				} else {
					do_building(make_plot(p.x, p.z + p.depth, p.width, p.depth));
				}
				return;
			}
		}
		if (area < 100) {
			return;
		}
		//The Plot is "square" if width & depth are close
		square = Math.abs(p.width - p.depth) < 10;
		//mark the land as used so other buildings don'transformationArray appear here, even if we don'transformationArray use it all.
		claim(p.x, p.z, p.width, p.depth, CLAIM_BUILDING);

		//The roundy mod buildings look best on square plots.
		if (square && p.width > 20) {
			height = 45 + random.nextInt(10);
			modern_count++;
			skyscrapers++;
			buildings.add(new Building(this, random, regions, Building.BUILDING_MODERN, p.x, p.z, height, p.width, p.depth, seed, color));
			return;
		}
		/*
	  //Rectangular plots are a good place for Blocky style buildsing to sprawl blockily.
	  if (multiTexturePathPattern.width > multiTexturePathPattern.depth * 2 || multiTexturePathPattern.depth > multiTexturePathPattern.width * 2 && area > 800) {
	    height = 20 + random.nextInt (10);
	    blocky_count++;
	    skyscrapers++;
	    new CBuilding (BUILDING_BLOCKY, multiTexturePathPattern.x, multiTexturePathPattern.z, height, multiTexturePathPattern.width, multiTexturePathPattern.depth, seed, color);
	    return;
	  }
		 */
		//tower_count = -1;
		//This spot isn'transformationArray ideal for any particular building, but try to keep a good mix
		if (tower_count < modern_count && tower_count < blocky_count) {
			type = Building.BUILDING_TOWER;
			tower_count++;
		} else if (blocky_count < modern_count) {
			type = Building.BUILDING_BLOCKY;
			blocky_count++;
		} else {
			type = Building.BUILDING_MODERN;
			modern_count++;
		}
		height = 45 + random.nextInt(10);
		buildings.add(new Building(this, random, regions, type, p.x, p.z, height, p.width, p.depth, seed, color));
		skyscrapers++;

	}

	public void reset() {

		int x, y;
		int width, depth, height;
		int attempts;
		boolean broadway_done;
		boolean road_left, road_right;
		Vector4f light_color;
		Vector4f building_color;
		float west_street = 0, north_street = 0, east_street = 0, south_street = 0;

		regions.add(new CityBaseRegion(this, null, new Vector3i(), new Vector3i(WORLD_SIZE, 64, WORLD_SIZE), 7, 0));
		broadway_done = false;
		skyscrapers = 0;
		logo_index = 0;
		scene_begin = 0;
		tower_count = blocky_count = modern_count = 0;
		//	  hot_zone = glBboxClear ();
		//	  EntityClear ();
		//	  LightClear ();
		//	  CarClear ();
		//	  TextureReset ();
		//Pick a tint for the bloom
		//	  bloom_color = get_light_color(0.5f + (float)random.nextInt (10) / 20.0f, 0.75f);
		//	  light_color = glRgbaFromHsl (0.11f, 1.0f, 0.65f);

		//	  ZeroMemory (world, WORLD_SIZE * WORLD_SIZE);
		for (y = WORLD_EDGE; y < WORLD_SIZE - WORLD_EDGE; y += random.nextInt(25) + 25) {
			if (!broadway_done && y > WORLD_HALF - 20) {
				build_road(0, y, WORLD_SIZE, 19);
				y += 20;
				broadway_done = true;
			} else {
				depth = 6 + random.nextInt(6);
				if (y < WORLD_HALF / 2) {
					north_street = (y + depth / 2);
				}
				if (y < (WORLD_SIZE - WORLD_HALF / 2)) {
					south_street = (y + depth / 2);
				}
				build_road(0, y, WORLD_SIZE, depth);
			}
		}

		broadway_done = false;
		for (x = WORLD_EDGE; x < WORLD_SIZE - WORLD_EDGE; x += random.nextInt(25) + 25) {
			if (!broadway_done && x > WORLD_HALF - 20) {
				build_road(x, 0, 19, WORLD_SIZE);
				x += 20;
				broadway_done = true;
			} else {
				width = 6 + random.nextInt(6);
				if (x <= WORLD_HALF / 2) {
					west_street = (x + width / 2);
				}
				if (x <= WORLD_HALF + WORLD_HALF / 2) {
					east_street = (x + width / 2);
				}
				build_road(x, 0, width, WORLD_SIZE);
			}
		}
		//We kept track of the positionsToAdd of streets that will outline the high-detail hot zone
		//in the middle of the world.  Save this in a bounding box so that later we can
		//have the camera fly around without clipping through buildings.
		hot_zone = new BoundingBox(new Vector3f(west_street, 0.0f, north_street), new Vector3f(east_street, 0.0f, south_street));

		//Scan for places to put runs of streetlights on the east & west side of the road
		for (x = 1; x < WORLD_SIZE - 1; x++) {
			for (y = 0; y < WORLD_SIZE; y++) {
				//if this isn'transformationArray a bit of sidewalk, then keep looking
				if ((world[x][y] & CLAIM_WALK) == 0) {
					continue;
				}
				//If it's used as a road, skip it.
				if ((world[x][y] & CLAIM_ROAD) != 0) {
					continue;
				}
				road_left = (world[x + 1][y] & CLAIM_ROAD) != 0;
				road_right = (world[x - 1][y] & CLAIM_ROAD) != 0;
				//if the cells to our east and west are not road, then we're not on a corner.
				if (!road_left && !road_right) {
					continue;
				}
				//if the cell to our east AND west is road, then we're on a median. skip it
				if (road_left && road_right) {
					continue;
				}
				y += build_light_strip(regions, x, y, road_right ? Building.SOUTH : Building.NORTH);
			}
		}
		//Scan for places to put runs of streetlights on the north & south side of the road
		for (y = 1; y < WORLD_SIZE - 1; y++) {
			for (x = 1; x < WORLD_SIZE - 1; x++) {
				//if this isn'transformationArray a bit of sidewalk, then keep looking
				if ((world[x][y] & CLAIM_WALK) == 0) {
					continue;
				}
				//If it's used as a road, skip it.
				if ((world[x][y] & CLAIM_ROAD) != 0) {
					continue;
				}
				road_left = (world[x][y + 1] & CLAIM_ROAD) != 0;
				road_right = (world[x][y - 1] & CLAIM_ROAD) != 0;
				//if the cell to our east AND west is road, then we're on a median. skip it
				if (road_left && road_right) {
					continue;
				}
				//if the cells to our north and south are not road, then we're not on a corner.
				if (!road_left && !road_right) {
					continue;
				}
				x += build_light_strip(regions, x, y, road_right ? Building.EAST : Building.WEST);
			}
		}

		//Scan over the center area of the map and place the big buildings
		attempts = 0;
		while (skyscrapers < 50 && attempts < 350) {
			x = (WORLD_HALF / 2) + (random.nextInt() % WORLD_HALF);
			y = (WORLD_HALF / 2) + (random.nextInt() % WORLD_HALF);
			if (!claimed(x, y, 1, 1)) {
				do_building(find_plot(x, y));
				skyscrapers++;
			}
			attempts++;
		}

		//now blanket the rest of the world with lesser buildings
		for (x = 0; x < WORLD_SIZE; x++) {
			for (y = 0; y < WORLD_SIZE; y++) {
				if (world[CLAMP(x, 0, WORLD_SIZE)][CLAMP(y, 0, WORLD_SIZE)] != 0) {
					continue;
				}
				width = 12 + random.nextInt(20);
				depth = 12 + random.nextInt(20);
				height = Math.min(width, depth);
				if (x < 30 || y < 30 || x > WORLD_SIZE - 30 || y > WORLD_SIZE - 30) {
					height = random.nextInt(15) + 20;
				} else if (x < WORLD_HALF / 2) {
					height /= 2;
				}
				while (width > 8 && depth > 8) {
					if (!claimed(x, y, width, depth)) {
						claim(x, y, width, depth, CLAIM_BUILDING);
						building_color = new Vector4f(0.5f, 0.5f, 0.5f, 1);//WorldLightColor (random.nextInt ());
						//if we're out of the hot zone, use simple buildings
						if (x < hot_zone.min.x || x > hot_zone.max.x || y < hot_zone.min.z || y > hot_zone.max.z) {
							height = 5 + random.nextInt(height) + random.nextInt(height);
							buildings.add(new Building(this, random, regions, Building.BUILDING_SIMPLE, x + 1, y + 1, height, width - 2, depth - 2, random.nextInt(), building_color));
						} else { //use fancy buildings.
							height = 15 + random.nextInt(15);
							width -= 2;
							depth -= 2;
							if (coinflip()) {
								buildings.add(new Building(this, random, regions, Building.BUILDING_TOWER, x + 1, y + 1, height, width, depth, random.nextInt(), building_color));
							} else {
								buildings.add(new Building(this, random, regions, Building.BUILDING_BLOCKY, x + 1, y + 1, height, width, depth, random.nextInt(), building_color));
							}
						}
						break;
					}
					width--;
					depth--;
				}
				//leave big gaps near the edge of the map, no need to pack detail there.
				if (y < WORLD_EDGE || y > WORLD_SIZE - WORLD_EDGE) {
					y += 32;
				}
			}
			//leave big gaps near the edge of the map
			if (x < WORLD_EDGE || x > WORLD_SIZE - WORLD_EDGE) {
				x += 28;
			}
		}

	}

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	Plot find_plot(int x, int z) {

		Plot p = new Plot(0, 0, 0, 0);
		int x1, x2, z1, z2;

		//We've been given the location of an open bit of land, but we have no
		//idea how big it is. Find the boundary.
		x1 = x2 = x;
		while (!claimed(x1 - 1, z, 1, 1) && x1 > 0) {
			x1--;
		}
		while (!claimed(x2 + 1, z, 1, 1) && x2 < WORLD_SIZE) {
			x2++;
		}
		z1 = z2 = z;
		while (!claimed(x, z1 - 1, 1, 1) && z1 > 0) {
			z1--;
		}
		while (!claimed(x, z2 + 1, 1, 1) && z2 < WORLD_SIZE) {
			z2++;
		}
		p.width = (x2 - x1);
		p.depth = (z2 - z1);
		p.x = x1;
		p.z = z1;
		return p;

	}

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	BoundingBox WorldHotZone() {

		return hot_zone;

	}

	/*-----------------------------------------------------------------------------

	  This will return a random color which is suitible for light sources, taken
	  from a narrow group of hues. (Yellows, oranges, blues.)

	-----------------------------------------------------------------------------*/

	//	GLrgba WorldLightColor (unsigned index)
	//	{
	//
	//	  index %= LIGHT_COLOR_COUNT;
	//	  return glRgbaFromHsl (light_colors[index].hue, light_colors[index].sat, light_colors[index].lum);
	//
	//	}

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	//	char WorldCell (int x, int y)
	//	{
	//
	//	  return world[CLAMP (x, 0,WORLD_SIZE - 1)][CLAMP (y, 0, WORLD_SIZE - 1)];
	//
	//	}

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	//	GLrgba WorldBloomColor ()
	//	{
	//
	//	  return bloom_color;
	//
	//	}

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	int WorldLogoIndex() {

		return logo_index++;

	}

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	int WorldSceneBegin() {

		return scene_begin;

	}

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	//	void WorldTerm (void)
	//	{
	//
	//
	//	}

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	//	void WorldReset ()
	//	{
	//
	//	  //If we're already fading out, then this is the developer hammering on the
	//	  //"rebuild" button.  Let's hurry things up for the nice man...
	//	  if (fade_state == FADE_OUT)
	//	    do_reset ();
	//	  //If reset is called but the world isn'transformationArray ready, then don'transformationArray bother fading out.
	//	  //The program probably just timeRunning.
	//	  fade_state = FADE_OUT;
	//	  fade_start = GetTickCount ();
	//
	//	}

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	//	void WorldRender ()
	//	{
	//
	//	  if (!SHOW_DEBUG_GROUND)
	//	    return;
	//	  //Render a single texture over the city that shows traffic lanes
	//	  glDepthMask (false);
	//	  glDisable (GL_CULL_FACE);
	//	  glDisable (GL_BLEND);
	//	  glEnable (GL_TEXTURE_2D);
	//	  glColor3f (1,1,1);
	//	  glBindTexture (GL_TEXTURE_2D, 0);
	//	  glBegin (GL_QUADS);
	//	  glTexCoord2f (0, 0);   glVertex3f ( 0., 0, 0);
	//	  glTexCoord2f (0, 1);   glVertex3f ( 0, 0,  1024);
	//	  glTexCoord2f (1, 1);   glVertex3f ( 1024, 0, 1024);
	//	  glTexCoord2f (1, 0);   glVertex3f ( 1024, 0, 0);
	//	  glEnd ();
	//	  glDepthMask (true);
	//
	//
	//	}


	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	//	float WorldFade ()
	//	{
	//
	//	  return fade_current;
	//
	//	}

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	/*-----------------------------------------------------------------------------

	  How long since this current iteration of the city went on display,

	-----------------------------------------------------------------------------*/

	//	int WorldSceneElapsed ()
	//	{
	//
	//	  int     elapsed;
	//
	//	  if (!EntityReady () || !WorldSceneBegin ())
	//	    elapsed = 1;
	//	  else
	//	    elapsed = GetTickCount () - (WorldSceneBegin ());
	//	  elapsed = MAX (elapsed, 1);
	//	  return elapsed;
	//
	//	}

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/
	//
	//	void WorldUpdate ()
	//	{
	//
	//	  unsigned      fade_delta;
	//	  int           now;
	//
	//	  now = GetTickCount ();
	//	  if (reset_needed) {
	//	    do_reset (); //Now we've faded out the scene, rebuild it
	//	  }
	//	  if (fade_state != FADE_IDLE) {
	//	    if (fade_state == FADE_WAIT && TextureReady () && EntityReady ()) {
	//	        fade_state = FADE_IN;
	//	        fade_start = now;
	//	        fade_current = 1.0f;
	//	    }
	//	    fade_delta = now - fade_start;
	//	    //See if we're done fading in or out
	//	    if (fade_delta > FADE_TIME && fade_state != FADE_WAIT) {
	//	      if (fade_state == FADE_OUT) {
	//	        reset_needed = true;
	//	        fade_state = FADE_WAIT;
	//	        fade_current = 1.0f;
	//	      } else {
	//	        fade_state = FADE_IDLE;
	//	        fade_current = 0.0f;
	//	        start_time = percentage (NULL);
	//	        scene_begin = GetTickCount ();
	//	      }
	//	    } else {
	//	      fade_current = (float)fade_delta / FADE_TIME;
	//	      if (fade_state == FADE_IN)
	//	        fade_current = 1.0f - fade_current;
	//	      if (fade_state == FADE_WAIT)
	//	        fade_current = 1.0f;
	//	    }
	//	    if (!TextureReady ())
	//	      fade_current = 1.0f;
	//	  }
	//	  if (fade_state == FADE_IDLE && !TextureReady ()) {
	//	    fade_state = FADE_IN;
	//	    fade_start = now;
	//	  }
	//	  if (fade_state == FADE_IDLE && WorldSceneElapsed () > RESET_INTERVAL)
	//	    WorldReset ();
	//
	//	}

	/*-----------------------------------------------------------------------------

	-----------------------------------------------------------------------------*/

	//	void WorldInit ()
	//	{
	//
	//	  last_update = GetTickCount ();
	//	  for (int i = 0; i < CARS; i++)
	//	    new CCar ();
	//	  sky = new CSky ();
	//	  WorldReset ();
	//	  fade_state = FADE_OUT;
	//	  fade_start = 0;
	//
	//	}

}
