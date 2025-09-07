package org.schema.game.common.data;

import com.bulletphysics.linearmath.Transform;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentBufferManager;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.server.controller.world.factory.planet.FastNoiseSIMD;
import org.schema.schine.graphicsengine.core.GlUtil;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class IcosahedronHelper {

	public static Vector3f[] sideNormals;
	public static Transform[] sideTransforms;
	public static long[] sideVectorSets;
	//private static long[] sideDMVectorSets;
	
	static{		
		System.err.println("[ICOSAHEDRON] Loading vectors - Building transforms/vector sets ");
		
		sideNormals = new Vector3f[3];
		sideTransforms = new Transform[20];
		sideVectorSets = new long[80];
		//sideDMVectorSets = new long[80];
		
		Vector3f up = new Vector3f(0f,1f,0f);
		
		try {
			DataInputStream in = new DataInputStream(new FileInputStream("./data/IcoVectors.bin"));
			
			sideNormals[0] = new Vector3f(
						in.readFloat(),
						in.readFloat(),
						in.readFloat());
				
			sideNormals[1] = new Vector3f(
						in.readFloat(),
						in.readFloat(),
						in.readFloat());
				
			sideNormals[2] = new Vector3f(
						in.readFloat(),
						in.readFloat(),
						in.readFloat());
				
			for (int i = 0; i < 20; i++){											
				sideTransforms[i] = new Transform();
				sideTransforms[i].setIdentity();
				
				Vector3f sideUp;
				
				//set Rotation
				GlUtil.setRightVector(new Vector3f(in.readFloat(), in.readFloat(), in.readFloat()), sideTransforms[i].basis);
				GlUtil.setUpVector(sideUp = new Vector3f(in.readFloat(), in.readFloat(), in.readFloat()), sideTransforms[i].basis);
				GlUtil.setForwardVector(new Vector3f(in.readFloat(), in.readFloat(), in.readFloat()), sideTransforms[i].basis);				
				
				sideVectorSets[i] = newVectorSet(sideTransforms[i].basis, 0);
				sideVectorSets[i+20] = newVectorSet(sideTransforms[i].basis, 1);
				sideVectorSets[i+40] = newVectorSet(sideTransforms[i].basis, 2);
				sideVectorSets[i+60] = newVectorSet(sideTransforms[i].basis, 3);
				
				/*sideDMVectorSets[i] = newDMVectorSet(sideTransforms[i].basis, 0);
				sideDMVectorSets[i+20] = newDMVectorSet(sideTransforms[i].basis, 1);
				sideDMVectorSets[i+40] = newDMVectorSet(sideTransforms[i].basis, 2);
				sideDMVectorSets[i+60] = newDMVectorSet(sideTransforms[i].basis, 3);*/
			}
			
			in.close();
			
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		 
	}
	
	public static long newVectorSet(Matrix3f transform, int sampleScale)
	{
		assert((SegmentData.SEG >> sampleScale) > 0);
		
		int xSize = SegmentData.SEG;
		int ySize = SegmentData.SEG;
		int zSize = SegmentData.SEG + 1;
		
		int sampleSize = 1;

		int xSizeSample = xSize;
		int ySizeSample = ySize;
		int zSizeSample = zSize;
		
		if (sampleScale > 0)
		{
			sampleSize = 1 << sampleScale;
			int sampleMask = sampleSize - 1;
	
			if ((xSizeSample & sampleMask) != 0)
				xSizeSample = (xSizeSample & ~sampleMask) + sampleSize;
	
			if ((ySizeSample & sampleMask) != 0)
				ySizeSample = (ySizeSample & ~sampleMask) + sampleSize;
	
			if ((zSizeSample & sampleMask) != 0)
				zSizeSample = (zSizeSample & ~sampleMask) + sampleSize;
	
			xSizeSample = (xSizeSample >> sampleScale) + 1;
			ySizeSample = (ySizeSample >> sampleScale) + 1;
			zSizeSample = (zSizeSample >> sampleScale) + 1;
		}
		
		int size = xSize*ySize*zSize;
		float[] vectorSet = new float[size*3];
		Vector3f v3 = new Vector3f();

		int index = 0;

		for (int ix = 0; ix < xSizeSample; ix++){
			for (int iy = 0; iy < ySizeSample; iy++){
				for (int iz = zSizeSample - 1; iz >= 0; iz--){
					v3.set((ix*sampleSize)-SegmentData.SEG_HALF, (iz*sampleSize)-SegmentData.SEG_HALF, (iy*sampleSize)-SegmentData.SEG_HALF);
					transform.transform(v3);
					
					vectorSet[index] = v3.x;
					vectorSet[index+size] = v3.y;
					vectorSet[index+(size * 2)] = v3.z;
					index++;
				}
			}
		}
		
		return FastNoiseSIMD.NewVectorSet(sampleScale, vectorSet, xSize, ySize, zSize);
	}
	
	public static long newDMVectorSet(Matrix3f transform, int sampleScale)
	{
		int dmSampleScale = sampleScale+SegmentBufferManager.DENSITY_MAP_BIT_SCALE;
		assert((SegmentBufferManager.DENSITY_MAP_SIZE >> dmSampleScale) > 0);
		int axisSize = SegmentBufferManager.DENSITY_MAP_SIZE;
		
		if (sampleScale > 0)
		{
			axisSize >>= sampleScale;
			axisSize++;
		}		
		
		int size = axisSize*axisSize*axisSize;
		float[] vectorSet = new float[size*3];
		Vector3f v3 = new Vector3f();
		
		int sampleSize = 1 << dmSampleScale;
		int index = 0;

		for (int ix = 0; ix < axisSize; ix++)
		{
			for (int iy = 0; iy < axisSize; iy++)
			{
				for (int iz = 0; iz < axisSize; iz++)
				{
					v3.set((ix*sampleSize)-SegmentData.SEG_HALF, (iy*sampleSize)-SegmentData.SEG_HALF, (iz*sampleSize)-SegmentData.SEG_HALF);
					transform.transform(v3);
					
					vectorSet[index] = v3.x;
					vectorSet[index+size] = v3.y;
					vectorSet[index+(size * 2)] = v3.z;
					index++;
				}
			}
		}
		
		return FastNoiseSIMD.NewVectorSet(sampleScale, vectorSet, SegmentBufferManager.DENSITY_MAP_SIZE, SegmentBufferManager.DENSITY_MAP_SIZE, SegmentBufferManager.DENSITY_MAP_SIZE);
	}
	
	public static Transform getSideTransform(byte sideID){
		return sideTransforms[sideID];
	}

	public static Vector3f[] calculatePoly(byte sideID) {
		Vector3f[] poly = new Vector3f[3];
		poly[0] = new Vector3f();
		poly[1] = new Vector3f();
		poly[2] = new Vector3f();

		Vector3f up = new Vector3f(0f,1f,0f);

		Transform t = getSideTransform(sideID);

		t.transform(up);

		poly[0].set(up);
		poly[1].set(up);
		poly[2].set(up);

		poly[0].scale(0.5f);
		poly[1].scale(0.5f);
		poly[2].scale(0.5f);

		poly[0].add(new Vector3f(-0.5f, 0.5f, 0.5f));
		poly[1].add(new Vector3f(0.5f, 0.5f, 0.5f));
		poly[2].add(new Vector3f(0.5f, 0.5f, -0.5f));

		return poly;
	}

	public static long getVectorSet(byte sideID, int sampleScale){
		return sideVectorSets[sideID + sampleScale*20];
	}
	
	/*public static long getDMVectorSet(byte sideID, int sampleScale){
		return sideDMVectorSets[sideID + sampleScale*20];
	}*/
	
	//Non transformed point
	public static boolean isPointInSide(Vector3f point){
		
		//float yMod = point.y - 0.5f;
		
		return point.x * sideNormals[0].x + point.y * sideNormals[0].y + point.z * sideNormals[0].z > 0.0f &&
				point.x * sideNormals[1].x + point.y * sideNormals[1].y + point.z * sideNormals[1].z > 0.0f &&
				point.x * sideNormals[2].x + point.y * sideNormals[2].y + point.z * sideNormals[2].z > 0.0f;
	}
	
	public static boolean isPointInSide(float x, float y, float z){
		
		//y -= 0.5f;
		
		return (sideNormals[0].x*x + sideNormals[0].y*y + sideNormals[0].z*z) > 0.0f &&
				(sideNormals[1].x*x + sideNormals[1].y*y + sideNormals[1].z*z) > 0.0f &&
				(sideNormals[2].x*x + sideNormals[2].y*y + sideNormals[2].z*z) > 0.0f;
	}
	
	public static boolean isSegmentInSide(Vector3i position){
		return isSegmentInSide(position.x, position.y, position.z);
	}
	public static boolean isSegmentInSide(int positionX, int positionY, int positionZ){
		
		//if (positionX*positionX+positionY*positionY+positionZ*positionZ < 340*340)
		//	return false;
		
		int posX = positionX - SegmentData.SEG_HALF;
		int posY = positionY + SegmentData.SEG_HALF - 1;
		int posZ = positionZ - SegmentData.SEG_HALF;
		
		if(posX < -SegmentData.SEG_MINUS_ONE)
			posX += SegmentData.SEG_MINUS_ONE;
		else if (posX < 0)
			posX = 0;
		
		for (int i = 0; i < SegmentData.SEG; i++)
		{			
			if (isPointInSide(posX, posY, posZ + i))
				return true;
		}	
		
		return false;
	}
	
	
	public static boolean isSegmentAllInSide(Vector3i position){		
						
		float x = position.x - SegmentData.SEG_HALF;
		float y = position.y - SegmentData.SEG_HALF;
		float z = position.z - SegmentData.SEG_HALF;		
		
		if (!isPointInSide(x,y,z)) return false;		
		
		x += SegmentData.SEG_MINUS_ONE;
		if (!isPointInSide(x,y,z)) return false;
		
		z += SegmentData.SEG_MINUS_ONE;
		if (!isPointInSide(x,y,z)) return false;
		
		x -= SegmentData.SEG_MINUS_ONE;
		if (!isPointInSide(x,y,z)) return false;
		
		return true;
	}
	
	static float xApproxRatio = (float) Math.tan(Math.toRadians(33.6));
	static float zPosApproxRatio = (float) Math.tan(Math.toRadians(37.5));
	static float zNegApproxRatio = (float) Math.tan(Math.toRadians(21.0));
	
/*	public static boolean isSegmentInSideApprox(Vector3i position){
		return isSegmentInSideApprox(position.x, position.y, position.z);
	}
	public static boolean isSegmentInSideApprox(int x, int y, int z){
		
		if (x*x+y*y+z*z < 350*350)
			return false;
		
		int posY = y + SegmentData.SEG_HALF - 1;
		
		int posX = x - SegmentData.SEG_HALF;
		
		if(posX < -(SegmentData.SEG - 1)){
			posX += SegmentData.SEG - 1;
			
			if (-posX > posY * xApproxRatio )
				return false;
		}
		else if (posX > posY * xApproxRatio )
			return false;		
		
		
		int posZ = z - SegmentData.SEG_HALF;
		
		if(posZ < -(SegmentData.SEG - 1)){
			posZ += SegmentData.SEG - 1;
			
			if (-posZ > posY * zNegApproxRatio )
				return false;
		}
		else if (posZ > posY * zPosApproxRatio )
			return false;

		return true;
	}*/
	
	public static int segmentProviderXMinMax(float planetRadius){
		return FastMath.fastRound(planetRadius * xApproxRatio / SegmentData.SEGf);
	}	
	public static int segmentProviderZMin(float planetRadius){
		return -FastMath.fastRound(planetRadius * zNegApproxRatio / SegmentData.SEGf);
	}
	public static int segmentProviderZMax(float planetRadius){
		return FastMath.fastRound(planetRadius * zPosApproxRatio / SegmentData.SEGf);
	}

	/**
	 * Furthest point from the center of the planet
	 * @param pos
	 * @param out
	 */
	public static void segmentHighPoint(Vector3i pos, Vector3i out){
		out.x = pos.x - SegmentData.SEG_HALF;
		out.y = pos.y + SegmentData.SEG_HALF - 1;
		out.z = pos.z - SegmentData.SEG_HALF;

		if (out.x > -SegmentData.SEG_HALF)
			out.x += SegmentData.SEG_MINUS_ONE;

		if (out.z > -SegmentData.SEG_HALF)
			out.z += SegmentData.SEG_MINUS_ONE;
	}

	/**
	 * Closest point to the center of the planet
	 * @param pos
	 * @param out
	 */
	public static void segmentLowPoint(Vector3i pos, Vector3i out){
		out.x = pos.x - SegmentData.SEG_HALF;
		out.y = pos.y - SegmentData.SEG_HALF;
		out.z = pos.z - SegmentData.SEG_HALF;

		if(out.x < -SegmentData.SEG_MINUS_ONE)
			out.x += SegmentData.SEG_MINUS_ONE;
		else if (out.x < 0)
			out.x = 0;

		if(out.z < -SegmentData.SEG_MINUS_ONE)
			out.z += SegmentData.SEG_MINUS_ONE;
		else if (out.z < 0)
			out.z = 0;
	}
	
	static int fastAbs(int i) { return (i > 0) ? i : -i; }
}
