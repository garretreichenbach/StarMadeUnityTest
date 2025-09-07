package org.schema.game.client.data.terrain.fractal;

import java.util.Arrays;
import java.util.Random;

import javax.vecmath.Vector3f;

public class FractalHeightMap {
	private static final int RAND_MAX = 32767;
	public static float[] heightValues;
	private static float[] source;
	public int size;
	public float scale;
	public int vertexSpacing;
	public int width;
	public int height;
	public int depth;
	Random random = new Random();

	public FractalHeightMap(int width, int height, int depth, int heightmapSpacing, float heightmapScale) {
		this.width = width;
		this.height = height;
		this.depth = depth;
		scale = heightmapScale;
		size = width;
		vertexSpacing = heightmapSpacing;
		if (heightValues == null) {
			heightValues = new float[size * size];
		}
		//	      heightValues.resize(size * size);
		//	    memset(heightValues[0), 0, heightValues.size());
	}

	/*********************************************
	 * /* This method is based on an article found on gamedev
	 * /* http://www.gamedev.net/reference/articles/article2164.asp
	 * /* It is a simple box filter algorithm that smooths out the
	 * /* heightmaps values
	 * /
	 *********************************************/
	public void boxFilter() {
		// initialise the vector to the current heightmap values
		if (source == null) {
			source = Arrays.copyOf(heightValues, heightValues.length);
		} else {
			for (int i = 0; i < heightValues.length; i++) {
				source[i] = heightValues[i];
			}
		}

		// this variable is changed to access the various values
		// as the heightmap is traversed
		int i = 0;

		// calculate bounds ahead of percentage
		int bounds = size * size;

		for (int y = 0; y < size; ++y) {
			for (int x = 0; x < size; ++x) {
				// value is the running total
				float value = 0.0f;

				// the cell average is then calculated
				float cellAverage = 0.0f;

				// top row

				i = (y - 1) * size + (x - 1);
				if (i >= 0 && i < bounds) {
					value += source[i];
					cellAverage += 1.0f;
				}

				i = y * size + (x - 1);
				if (i >= 0 && i < bounds) {
					value += source[i];
					cellAverage += 1.0f;
				}

				i = (y + 1) * size + (x - 1);
				if (i >= 0 && i < bounds) {
					value += source[i];
					cellAverage += 1.0f;
				}

				// middle row

				i = (y - 1) * size + x;
				if (i >= 0 && i < bounds) {
					value += source[i];
					cellAverage += 1.0f;
				}

				i = y * size + x;
				if (i >= 0 && i < bounds) {
					value += source[i];
					cellAverage += 1.0f;
				}

				i = (y + 1) * size + x;
				if (i >= 0 && i < bounds) {
					value += source[i];

					cellAverage += 1.0f;
				}

				// bottom row

				i = (y - 1) * size + (x + 1);
				if (i >= 0 && i < bounds) {
					value += source[i];
					cellAverage += 1.0f;
				}

				i = y * size + (x + 1);
				if (i >= 0 && i < bounds) {
					value += source[i];
					cellAverage += 1.0f;
				}

				i = (y + 1) * size + (x + 1);
				if (i >= 0 && i < bounds) {
					value += source[i];
					cellAverage += 1.0f;
				}

				heightValues[y * size + x] = value / cellAverage;
			}
		}
	}

	float getHeight(int x, int z) {
		return heightValues[z * size + x];
	}

	/******************************************************
	 * Henry Keith Prescott  Computer Games Engineering MSc  061263286
	 * <p/>
	 * 88
	 * <p/>
	 * /* This method is used to calculate the normal for lighting
	 * /* Similar to the phong model the normal at each point is calculated
	 * /* rather than working out the value at each vertex and averaging it
	 * /* across the face of the polygon (taken from tutorial)
	 * /*
	 *****************************************************/
	Vector3f getNormal(int x, int z) {
		Vector3f normalRequired = new Vector3f();
		if (x > 0 && x < size - 1) {
			normalRequired.x = getHeight(x - 1, z) - getHeight(x + 1, z);
		} else if (x > 0) {
			normalRequired.x = 2.0f * (getHeight(x - 1, z) - getHeight(x, z));
		} else {
			normalRequired.x = 2.0f * (getHeight(x, z) - getHeight(x + 1, z));
		}

		if (z > 0 && z < size - 1) {
			normalRequired.z = getHeight(x, z - 1) - getHeight(x, z + 1);
		} else if (z > 0) {
			normalRequired.z = 2.0f * (getHeight(x, z - 1) - getHeight(x, z));
		} else {
			normalRequired.z = 2.0f * (getHeight(x, z) - getHeight(x, z + 1));
		}

		normalRequired.y = 2.0f * vertexSpacing;
		normalRequired.normalize();
		return normalRequired;
	}

	/**************************************************************************
	 * /*   This method emulates the midpoint displacement techique as implemented
	 * /*   in Game Programming Gems I &
	 * http://www.lighthouse3d.com/opengl/terrain/index.php?mpd2
	 * /
	 *************************************************************************/
	public void midpointDisplacement(float roughness) {
		// initialise all the values in the array to zero
		//	    std::fill(heightValues.begin(), heightValues.end(), 0.0f);

		int pointA;
		int pointB;
		int pointC;
		int pointD;
		int midPoint;

		// The displacement height is equal to the length of each heightmap
		float displacementHeight = height;// * 0.5f;

		// This variable is reduced after each iteration
		float displacementHeightFactor = (float) Math.pow(2.0, -roughness);

		// these variables are later used by the normalise values to rap around
		// any random values that are outside the texture height range
		float minimumHeight = 0.0f;
		float maximumHeight = 0.0f;

		for (int i = size; i > 0; displacementHeight *= displacementHeightFactor, i /= 2)

		{

			for (int z = 0; z < size; z += i) // Diamond Step
			{
				for (int x = 0; x < size; x += i) {
					// translate x z coordinates into index locations
					// modulus is used to wrap around (just in case)
					pointA = (((x + size) % size) + ((z + size) % size) * size);
					pointB = ((((x + i) + size) % size) + ((z + size) %
							size) * size);
					pointC = ((((x + i) + size) % size) + (((z + i) + size) %
							size) * size);
					pointD = (((x + size) % size) + (((z + i) + size) % size) *
							size);
					midPoint = ((((x + i / 2) + size) % size) + (((z + i / 2) +
							size) % size) * size);

					//	                        float newValue = (
					//	displacementHeight * (random.nextInt(RAND_MAX) * 1.0f) / (RAND_MAX * 1.0f));
					//	                        heightValues[midPoint] = newValue +
					//	(heightValues[pointA] + heightValues[pointB] + heightValues[pointC] +
					//	heightValues[pointD]) * 0.25f;
					//	                        if(newValue < 0 || newValue > 40){
					//	                        	System.err.println("ahhhh "+newValue);
					//	                        }
					float newValue = -displacementHeight + (2.0f *
							displacementHeight * (random.nextInt(RAND_MAX) * 1.0f) / (RAND_MAX * 1.0f));
					heightValues[midPoint] = newValue +
							(heightValues[pointA] + heightValues[pointB] + heightValues[pointC] +
									heightValues[pointD]) * 0.25f;

					if (minimumHeight > heightValues[midPoint]) {
						minimumHeight = heightValues[midPoint];
					}

					if (maximumHeight < heightValues[midPoint]) {
						maximumHeight = heightValues[midPoint];
					}
				}
			}

			for (int z = 0; z < size; z += i) // Square Step
			{
				for (int x = 0; x < size; x += i) {
					// translate x z coordinates into index locations
					// modulus is used to wrap around (just in case)
					pointA = (((x + size) % size) + ((z + size) % size) * size);
					pointB = ((((x + i) + size) % size) + ((z + size) %
							size) * size);
					pointC = ((((x + i / 2) + size) % size) + (((z - i / 2) +
							size) % size) * size);
					pointD = ((((x + i / 2) + size) % size) + (((z + i / 2) +
							size) % size) * size);
					midPoint = ((((x + i / 2) + size) % size) + ((z + size) %
							size) * size);

					//	                        float newValue = (
					//	displacementHeight * random.nextInt(RAND_MAX) * 1.0f) / (RAND_MAX * 1.0f);
					//	                        if(newValue < 0 || newValue > 40){
					//	                        	System.err.println("ahhhh "+newValue);
					//	                        }
					float newValue = -displacementHeight + (2.0f *
							displacementHeight * random.nextInt(RAND_MAX) * 1.0f) / (RAND_MAX * 1.0f);

					heightValues[midPoint] = newValue + (heightValues[pointA] +
							heightValues[pointB] + heightValues[pointC] + heightValues[pointD]) * 0.25f;

					if (minimumHeight > heightValues[midPoint]) {
						minimumHeight = heightValues[midPoint];
					}

					if (maximumHeight < heightValues[midPoint]) {
						maximumHeight = heightValues[midPoint];
					}

					// translate x z coordinates into index locations
					// modulus is used to wrap around (just in case)
					pointA = (((x + size) % size) + ((z + size) % size) * size);
					pointB = (((x + size) % size) + (((z + i) + size) %
							size) * size);
					pointC = ((((x + i / 2) + size) % size) + (((z + i / 2) +
							size) % size) * size);
					pointD = ((((x - i / 2) + size) % size) + (((z + i / 2) +
							size) % size) * size);
					midPoint = (((x + size) % size) + (((z + i / 2) + size) %
							size) * size);

					//	                        newValue = (
					//	displacementHeight * (random.nextInt(RAND_MAX) * 1.0f) / (RAND_MAX * 1.0f));
					//	                        if(newValue < 0 || newValue > 40){
					//	                        	System.err.println("ahhhh "+newValue);
					//	                        }

					newValue = -displacementHeight + (2.0f *
							displacementHeight * (random.nextInt(RAND_MAX) * 1.0f) / (RAND_MAX * 1.0f));

					heightValues[midPoint] = newValue + (heightValues[pointA] +
							heightValues[pointB] + heightValues[pointC] + heightValues[pointD]) * 0.25f;

					if (minimumHeight > heightValues[midPoint]) {
						minimumHeight = heightValues[midPoint];
					}

					if (maximumHeight < heightValues[midPoint]) {
						maximumHeight = heightValues[midPoint];
					}
				}
			}
		}
		//	 System.err.println("minMax "+minimumHeight+", "+maximumHeight);
		for (int i = 0; i < size * size; ++i) {
			heightValues[i] = normalise(heightValues[i], minimumHeight, maximumHeight);
		}
	}

	float normalise(float value, float minimumValue, float maximumValue) {
		return (value - minimumValue) / (maximumValue - minimumValue) * height; // * 300.0
	}

}
