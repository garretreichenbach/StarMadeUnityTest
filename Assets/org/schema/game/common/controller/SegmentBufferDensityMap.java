package org.schema.game.common.controller;

import java.util.Stack;

import org.schema.common.FastMath;
import org.schema.game.server.controller.world.factory.planet.FastNoiseSIMD;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public class SegmentBufferDensityMap {

	private static final int VOXEL_STEP_X = SegmentBufferManager.DENSITY_MAP_SIZE_P2;
	private static final int VOXEL_STEP_Y = SegmentBufferManager.DENSITY_MAP_SIZE;
	private static final int VOXEL_STEP_Z = 1;
	
	private static Stack<SegmentBufferDensityMap> objectPool;
	
	public float[] densityMap;
	public float[] densityMapTemp;
	private Int2IntOpenHashMap voxelIndexVertMap;
	private IntArrayList tris;
	private FloatArrayList verts;
	private FloatArrayList normals;
	
	static {
		objectPool = new Stack<SegmentBufferDensityMap>();
	}
	
	public static SegmentBufferDensityMap getSegmentBufferDensityMap()
	{
		synchronized (objectPool) {		
			if (objectPool.empty())
				return new SegmentBufferDensityMap();
		
			return objectPool.pop();
		}
	}
	
	public static void returnSegmentBufferDensityMap(SegmentBufferDensityMap o)
	{
		o.clean();	
		synchronized (objectPool) {		
			objectPool.push(o);
		}
	}
	
	public SegmentBufferDensityMap() {
		densityMap = FastNoiseSIMD.GetEmptyNoiseSet(SegmentBufferManager.DENSITY_MAP_SIZE, SegmentBufferManager.DENSITY_MAP_SIZE, SegmentBufferManager.DENSITY_MAP_SIZE);
		densityMapTemp = FastNoiseSIMD.GetEmptyNoiseSet(SegmentBufferManager.DENSITY_MAP_SIZE, SegmentBufferManager.DENSITY_MAP_SIZE, SegmentBufferManager.DENSITY_MAP_SIZE);
		voxelIndexVertMap = new Int2IntOpenHashMap();
		tris = new IntArrayList();
		verts = new FloatArrayList();
		normals = new FloatArrayList();
	}
	
	public void clean() {
		voxelIndexVertMap.clear();
		tris.clear();
		verts.clear();
		normals.clear();
	}
	
	public void generateMesh() {	
	
		for (int x = 0; x < SegmentBufferManager.DENSITY_MAP_SIZE - 1; x++)
		{
			for (int y = 0; y < SegmentBufferManager.DENSITY_MAP_SIZE - 1; y++)
			{
				for (int z = 0; z < SegmentBufferManager.DENSITY_MAP_SIZE - 1; z++)
				{
					int index = x * VOXEL_STEP_X + y * VOXEL_STEP_Y + z * VOXEL_STEP_Z;
					float voxel = densityMap[index];					
					float right = densityMap[index + VOXEL_STEP_X];
					float up = densityMap[index + VOXEL_STEP_Y];
					float forward = densityMap[index + VOXEL_STEP_Z];					
	
					if (voxel >= 0.0f)
					{
						// Right
						if (right < 0.0f)
						{	
							int vert0 = voxelVertIndex(index, x, y+1, z);
							int vert1 = voxelVertIndex(index, x, y+1, z+1);
							int vert2 = voxelVertIndex(index, x, y, z+1);
							int vert3 = voxelVertIndex(index, x, y, z);
							
							addQuadTris(vert0, vert1, vert2, vert3);
						}
	
						// Up
						if (up < 0.0f)
						{
							int vert0 = voxelVertIndex(index, x, y, z+1);
							int vert1 = voxelVertIndex(index, x+1, y, z+1);
							int vert2 = voxelVertIndex(index, x+1, y, z);
							int vert3 = voxelVertIndex(index, x, y, z);
							
							addQuadTris(vert0, vert1, vert2, vert3);
						}
	
						// Forward
						if (forward < 0.0f)
						{
							int vert0 = voxelVertIndex(index, x+1, y, z);
							int vert1 = voxelVertIndex(index, x+1, y+1, z);
							int vert2 = voxelVertIndex(index, x, y+1, z);
							int vert3 = voxelVertIndex(index, x, y, z);
							
							addQuadTris(vert0, vert1, vert2, vert3);
						}
					}
					else // Voxel not solid
					{
						// Right
						if (right >= 0.0f)
						{	
							int vert0 = voxelVertIndex(index, x, y, z);
							int vert1 = voxelVertIndex(index, x, y, z+1);
							int vert2 = voxelVertIndex(index, x, y+1, z+1);
							int vert3 = voxelVertIndex(index, x, y+1, z);
							
							addQuadTris(vert0, vert1, vert2, vert3);
						}
	
						// Up
						if (up >= 0.0f)
						{
							int vert0 = voxelVertIndex(index, x, y, z);
							int vert1 = voxelVertIndex(index, x+1, y, z);
							int vert2 = voxelVertIndex(index, x+1, y, z+1);
							int vert3 = voxelVertIndex(index, x, y, z+1);
							
							addQuadTris(vert0, vert1, vert2, vert3);
						}
	
						// Forward
						if (forward >= 0.0f)
						{
							int vert0 = voxelVertIndex(index, x, y, z);
							int vert1 = voxelVertIndex(index, x, y+1, z);
							int vert2 = voxelVertIndex(index, x+1, y+1, z);
							int vert3 = voxelVertIndex(index, x+1, y, z);
							
							addQuadTris(vert0, vert1, vert2, vert3);
						}
					}
				}
			}
		}
	
		if (verts.size() == 0){
			//No mesh
		}
	
		//Mesh is done
	}
	
	// Calculate difference vector from the isosurface
	private int voxelVertIndex(int voxelIndex, float vertX, float vertY, float vertZ)
	{
		if (voxelIndexVertMap.containsKey(voxelIndex)){	
			return voxelIndexVertMap.get(voxelIndex);
		}

		float a = densityMap[voxelIndex];
		float b = densityMap[voxelIndex + VOXEL_STEP_X];
		float c = densityMap[voxelIndex + VOXEL_STEP_Y];
		float d = densityMap[voxelIndex + VOXEL_STEP_X + VOXEL_STEP_Y];
		float e = densityMap[voxelIndex + VOXEL_STEP_Z];
		float f = densityMap[voxelIndex + VOXEL_STEP_X + VOXEL_STEP_Z];
		float g = densityMap[voxelIndex + VOXEL_STEP_Y + VOXEL_STEP_Z];
		float h = densityMap[voxelIndex + VOXEL_STEP_X + VOXEL_STEP_Y + VOXEL_STEP_Z];
		
		float v = (a + b + c + d + e + f + g + h) * -0.125f;
		
		float x = (-a + b - c + d - e + f - g + h)*0.25f;
		float y = (-a - b + c + d - e - f + g + h)*0.25f;
		float z = (-a - b - c - d + e + f + g + h)*0.25f;
	
		float sqrMag = x*x + y*y + z*z;
		
		v /= sqrMag;
		x *= v;
		y *= v;
		z *= v;
		
		int vertIndex = verts.size();
		voxelIndexVertMap.addTo(voxelIndex, vertIndex);
		verts.add(vertX + x);
		verts.add(vertY + y);
		verts.add(vertZ + z);
		
		float invSqrMag = FastMath.carmackInvSqrt(sqrMag);
		x *= invSqrMag;
		y *= invSqrMag;
		z *= invSqrMag;
		
		normals.add(x);
		normals.add(y);
		normals.add(z);
		
		return vertIndex;
	}
	
	private void addQuadTris(int vert0, int vert1, int vert2, int vert3){
		float diag0x = verts.get(vert0) - verts.get(vert2);
		float diag0y = verts.get(vert0+1) - verts.get(vert2+1);
		float diag0z = verts.get(vert0+2) - verts.get(vert2+2);
		float diag1x = verts.get(vert1) - verts.get(vert3);
		float diag1y = verts.get(vert1+1) - verts.get(vert3+1);
		float diag1z = verts.get(vert1+2) - verts.get(vert3+2);

		if (diag0x*diag0x + diag0y*diag0y + diag0z*diag0z <
			diag1x*diag1x + diag1y*diag1y + diag1z*diag1z){
			
			tris.add(vert0);
			tris.add(vert1);
			tris.add(vert2);
			tris.add(vert0);
			tris.add(vert2);
			tris.add(vert3);
		}else{
			tris.add(vert1);
			tris.add(vert2);
			tris.add(vert3);
			tris.add(vert1);
			tris.add(vert3);
			tris.add(vert0);								
		}

	}
}
