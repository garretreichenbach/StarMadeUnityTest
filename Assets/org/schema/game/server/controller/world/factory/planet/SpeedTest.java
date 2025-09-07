package org.schema.game.server.controller.world.factory.planet;

import java.util.Random;

import javax.vecmath.Vector3f;

import org.schema.game.client.view.cubes.noise.Simplex;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.server.controller.RequestDataIcoPlanet;
import org.schema.game.server.controller.world.factory.planet.FastNoiseSIMD.FractalType;
import org.schema.game.server.controller.world.factory.planet.FastNoiseSIMD.NoiseType;
import org.schema.game.server.controller.world.factory.planet.terrain.TerrainGenerator;
import org.schema.game.server.controller.world.factory.planet.terrain.TerrainGeneratorEarth;
//import sun.misc.Unsafe;

public class SpeedTest {

	private static int[] permutations;


	public static void main(String[] args) {
		
		generatorTest();
	}
	
	
	static void generatorTest(){
		RequestDataIcoPlanet rq = new RequestDataIcoPlanet();
		
		TerrainGenerator tg = new TerrainGeneratorEarth(1337, 16f);
		
		//tg.generateSegment(null, rq);
	}
	
//	static void arraysTest(){
//		final int size = 32*32*32;
//		
//		long start,end;
//		int total = 0;
//		
//		Unsafe unsafe = null;
//		
//		try {
//			Constructor<Unsafe> unsafeConstructor = null;
//			unsafeConstructor = Unsafe.class.getDeclaredConstructor();
//			unsafeConstructor.setAccessible(true);
//			unsafe = unsafeConstructor.newInstance();
//			
//		} catch (InstantiationException e) {
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			e.printStackTrace();
//		} catch (NoSuchMethodException e) {
//			e.printStackTrace();
//		}
//		
//		total = 0;
//		int[] intArray = new int[size];
//		
//		start = System.nanoTime();
//		for (int i = 0; i < size; i++){
//			intArray[i] = i;
//		}
//		end = System.nanoTime();
//		
//		System.out.println((end - start));
//		
//		start = System.nanoTime();
//		for (int i = 0; i < size; i++){
//			total += intArray[i];
//		}
//		end = System.nanoTime();
//		
//		System.out.println((end - start) + " - " + total);
//		
//		total = 0;
//		byte b = 0;
//		byte[] byteArray = new byte[size*3];
//		
//		start = System.nanoTime();
//		for (int i = 0; i < size*3; i++){
//			byteArray[i++] = b++;
//			byteArray[i++] = b++;
//			byteArray[i] = b++;
//		}
//		end = System.nanoTime();
//		
//		System.out.println((end - start));
//		
//		start = System.nanoTime();
//		for (int i = 0; i < size*3; i++){
//			total += byteArray[i++];
//			total += byteArray[i++];
//			total += byteArray[i];
//		}
//		end = System.nanoTime();
//		
//		System.out.println((end - start) + " - " + total);
//		
//		long p = unsafe.allocateMemory(size*Unsafe.ARRAY_INT_INDEX_SCALE);
//		
//		start = System.nanoTime();
//		for (int i = 0; i < size; i++){
//			unsafe.putInt(p+i*Unsafe.ARRAY_INT_INDEX_SCALE, i);
//		}
//		end = System.nanoTime();
//		
//		System.out.println((end - start));
//		
//		start = System.nanoTime();
//		for (int i = 0; i < size; i++){
//			total += unsafe.getInt(p+i*Unsafe.ARRAY_INT_INDEX_SCALE);
//		}
//		end = System.nanoTime();
//		
//		System.out.println((end - start) + " - " + total);
//		
//		unsafe.freeMemory(p);
//	}

	static void noiseTest(){
		System.loadLibrary("./native/windows/x64/StarMadeNative64");
		
		long start,end;
		Random rand = new Random(1337);
		permutations = Simplex.randomize(rand);
		
		//int test = rand.nextInt(0);
		
		FastNoise fn = new FastNoise();
		
		FastNoiseSIMD baseNoise = new FastNoiseSIMD(1337);
		baseNoise.SetNoiseType(NoiseType.SimplexFractal);
		baseNoise.SetFrequency(0.008f);
		baseNoise.SetFractalOctaves(4);
		baseNoise.SetFractalGain(0.5f);
		baseNoise.SetFractalLacunarity(2f);
		baseNoise.SetFractalType(FractalType.RigidMulti);
		
		Vector3f sizeMult = new Vector3f();
		sizeMult.set(1f/32, 1f/32, 1f/32);
		
		//long start,end;
		
		start = System.nanoTime();
		float added = 0;
		for (int i = 0; i < 10000000; i++){
			float density = fn.GetPerlin(0.5f, 0.5f, 0.5f);
			added+=density;
		}
		end = System.nanoTime();
		
		System.out.println((end - start));
		
		float[] noiseSet = FastNoiseSIMD.GetEmptyNoiseSet(SegmentData.SEG, SegmentData.SEG, SegmentData.SEG);
		
		start = System.nanoTime();
		for (int i = 0; i < 1000; i++){
			baseNoise.FillNoiseSet(noiseSet, 0, 0, 0, SegmentData.SEG, SegmentData.SEG, SegmentData.SEG);
		}
		end = System.nanoTime();
		
		System.out.println((end - start));
        
        /*start = System.nanoTime();
		for (int i = 0; i < 1000; i++){
			//baseNoise.FillSampledNoiseSet(noiseSet, 0, 0, 0, SegmentData.SEG, SegmentData.SEG, SegmentData.SEG, 2);
		}
        end = System.nanoTime();
        
        System.out.println((end - start));
        
		start = System.nanoTime();
		for (int i = 0; i < 1000; i++){
			//int simdL = FastNoiseSIMD.GetSIMDLevel();
		}
        end = System.nanoTime();
        
        System.out.println((end - start));*/
		//System.out.println(added);
	}
	
}
