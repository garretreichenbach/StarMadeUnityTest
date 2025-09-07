package org.schema.game.server.controller.world.factory.planet;

public class FastNoiseSIMD {
	
	public enum NoiseType { Value, ValueFractal, Gradient, GradientFractal, Simplex, SimplexFractal, WhiteNoise, Cellular }
	public enum FractalType { FBM, Billow, RigidMulti }
	public enum CellularDistanceFunction { Euclidean, Manhattan, Natural }
	public enum CellularReturnType { CellValue, Distance, Distance2, Distance2Add, Distance2Sub, Distance2Mul, Distance2Div, NoiseLookup, Distance2Cave }
	public enum PerturbType { None, Gradient, GradientFractal, Normalise }
	
	private long nativeID;
	
	private static native long NewFastNoiseSIMD(int seed);
	public static native int GetSIMDLevel();
	public static native void SetSIMDLevel(int level);

	private static native void NativeFree(long id);
	
	private static native void NativeSetSeed(long id, int seed);
	private static native int NativeGetSeed(long id);
	private static native void NativeSetFrequency(long id, float frequency);
	private static native void NativeSetNoiseType(long id, int noiseType);
	private static native void NativeSetAxisScales(long id, float xScale, float yScale, float zScale);

	private static native void NativeSetFractalOctaves(long id, int octaves);
	private static native void NativeSetFractalLacunarity(long id, float lacunarity);
	private static native void NativeSetFractalGain(long id, float gain);
	private static native void NativeSetFractalType(long id, int fractalType);
	
	private static native void NativeSetCellularReturnType(long id, int cellularReturnType);
	private static native void NativeSetCellularDistanceFunction(long id, int cellularDistanceFunction);
	private static native void NativeSetCellularNoiseLookupType(long id, int cellularNoiseLookupType);
	private static native void NativeSetCellularNoiseLookupFrequency(long id, float cellularNoiseLookupFrequency);
	private static native void NativeSetCellularDistance2Indicies(long id, int cellularDistanceIndex0, int cellularDistanceIndex1);
	private static native void NativeSetCellularJitter(long id, float cellularJitter);

	private static native void NativeSetPerturbType(long id, int perturbType);
	private static native void NativeSetPerturbAmp(long id, float perturbAmp);
	private static native void NativeSetPerturbFrequency(long id, float perturbFrequency);
	private static native void NativeSetPerturbFractalOctaves(long id, int perturbOctaves);
	private static native void NativeSetPerturbFractalLacunarity(long id, float perturbLacunarity);
	private static native void NativeSetPerturbFractalGain(long id, float perturbGain);
	private static native void NativeSetPerturbNormaliseLength(long id, float perturbNormaliseLength);

	private static native void NativeFillNoiseSet(long id, float[] noiseSet, int xStart, int yStart, int zStart, int xSize, int ySize, int zSize);
	private static native void NativeFillSampledNoiseSet(long id, float[] noiseSet, int xStart, int yStart, int zStart, int xSize, int ySize, int zSize, int sampleScale);
	
	private static native void NativeFillNoiseSetVector(long id, float[] noiseSet, long vectorSet, float xOffset, float yOffset, float zOffset);
	private static native void NativeFillSampledNoiseSetVector(long id, float[] noiseSet, long vectorSet, float xOffset, float yOffset, float zOffset);
	
	public static native long NewVectorSet(int samplingScale, float[] vectorSet, int xSize, int ySize, int zSize);
	public static native void FreeVectorSet(long id);
	
	public FastNoiseSIMD(){
		nativeID = NewFastNoiseSIMD(1337);
	}	
	public FastNoiseSIMD(int seed){
		nativeID = NewFastNoiseSIMD(seed);
	}
	@Override
	protected void finalize() throws Throwable{		
		Free();
	}
	
	public void Free(){
		if (nativeID == 0)
			return;
		
		NativeFree(nativeID);
		nativeID = 0;
	}	
	public void SetSeed(int seed){
		assert(nativeID != 0);
		NativeSetSeed(nativeID, seed);
	}
	public int GetSeed(){
		assert(nativeID != 0);
		return NativeGetSeed(nativeID);
	}
	public void SetFrequency(float frequency){
		assert(nativeID != 0);
		NativeSetFrequency(nativeID, frequency);
	}
	public void SetNoiseType(NoiseType noiseType){
		assert(nativeID != 0);
		NativeSetNoiseType(nativeID, noiseType.ordinal());
	}
	public void SetAxisScales(float xScale, float yScale, float zScale){
		assert(nativeID != 0);
		NativeSetAxisScales(nativeID, xScale, yScale, zScale);
	}
	public void SetFractalOctaves(int octaves){
		assert(nativeID != 0);
		NativeSetFractalOctaves(nativeID, octaves);
	}
	public void SetFractalLacunarity(float lacunarity){
		assert(nativeID != 0);
		NativeSetFractalLacunarity(nativeID, lacunarity);
	}
	public void SetFractalGain(float gain){
		assert(nativeID != 0);
		NativeSetFractalGain(nativeID, gain);
	}
	public void SetFractalType(FractalType fractalType){
		assert(nativeID != 0);
		NativeSetFractalType(nativeID, fractalType.ordinal());
	}
	public void SetCellularDistanceFunction(CellularDistanceFunction cellularDistanceFunction){
		assert(nativeID != 0);
		NativeSetCellularDistanceFunction(nativeID, cellularDistanceFunction.ordinal());
	}
	public void SetCellularReturnType(CellularReturnType cellularReturnType){
		assert(nativeID != 0);
		NativeSetCellularReturnType(nativeID, cellularReturnType.ordinal());
	}
	public void SetCellularNoiseLookupType(NoiseType cellularLookupNoiseType){
		assert(nativeID != 0);
		NativeSetCellularNoiseLookupType(nativeID, cellularLookupNoiseType.ordinal());
	}
	public void SetCellularNoiseLookupFrequency(float cellularLookupNoiseFrequency){
		assert(nativeID != 0);
		NativeSetCellularNoiseLookupFrequency(nativeID, cellularLookupNoiseFrequency);
	}
	public void SetCellularDistance2Indicies(int cellularDistanceIndex0, int cellularDistanceIndex1){
		assert(nativeID != 0);
		NativeSetCellularDistance2Indicies(nativeID, cellularDistanceIndex0, cellularDistanceIndex1);
	}
	public void SetCellularJitter(float cellularJitter){
		assert(nativeID != 0);
		NativeSetCellularJitter(nativeID, cellularJitter);
	}
	public void SetPerturbType(PerturbType perturbType){
		assert(nativeID != 0);
		NativeSetPerturbType(nativeID, perturbType.ordinal());
	}
	public void SetPerturbAmp(float perturbAmp){
		assert(nativeID != 0);
		NativeSetPerturbAmp(nativeID, perturbAmp);
	}
	public void SetPerturbFrequency(float perturbFrequency){
		assert(nativeID != 0);
		NativeSetPerturbFrequency(nativeID, perturbFrequency);
	}
	public void SetPerturbFractalOctaves(int perturbOctaves){
		assert(nativeID != 0);
		NativeSetPerturbFractalOctaves(nativeID, perturbOctaves);
	}
	public void SetPerturbFractalLacunarity(float perturbLacunarity){
		assert(nativeID != 0);
		NativeSetPerturbFractalLacunarity(nativeID, perturbLacunarity);
	}
	public void SetPerturbFractalGain(float perturbGain){
		assert(nativeID != 0);
		NativeSetPerturbFractalGain(nativeID, perturbGain);
	}
	public void SetPerturbNormaliseLength(float perturbNormaliseLength){
		assert(nativeID != 0);
		NativeSetPerturbNormaliseLength(nativeID, perturbNormaliseLength);
	}
	public void FillNoiseSet(float[] noiseSet, int xStart, int yStart, int zStart, int xSize, int ySize, int zSize){
		assert(nativeID != 0);
		NativeFillNoiseSet(nativeID, noiseSet, xStart, yStart, zStart, xSize, ySize, zSize);
	}
	public float[] GetNoiseSet(int xStart, int yStart, int zStart, int xSize, int ySize, int zSize){
		assert(nativeID != 0);
		float[] noiseSet = GetEmptyNoiseSet(xSize, ySize, zSize);
		NativeFillNoiseSet(nativeID, noiseSet, xStart, yStart, zStart, xSize, ySize, zSize);
		return noiseSet;
	}
	public void FillSampledNoiseSet(float[] noiseSet, int xStart, int yStart, int zStart, int xSize, int ySize, int zSize, int sampleScale){
		assert(nativeID != 0);
		NativeFillSampledNoiseSet(nativeID, noiseSet, xStart, yStart, zStart, xSize, ySize, zSize, sampleScale);
	}
	public float[] GetSampledNoiseSet(int xStart, int yStart, int zStart, int xSize, int ySize, int zSize, int sampleScale){
		assert(nativeID != 0);
		float[] noiseSet = GetEmptyNoiseSet(xSize, ySize, zSize);
		NativeFillSampledNoiseSet(nativeID, noiseSet, xStart, yStart, zStart, xSize, ySize, zSize, sampleScale);
		return noiseSet;
	}
	public void FillNoiseSet(float[] noiseSet, long vectorSet, float xOffset, float yOffset, float zOffset){
		assert(nativeID != 0);
		NativeFillNoiseSetVector(nativeID, noiseSet, vectorSet, xOffset, yOffset, zOffset);
	}
	public void FillSampledNoiseSet(float[] noiseSet, long vectorSet, float xOffset, float yOffset, float zOffset){
		assert(nativeID != 0);
		NativeFillSampledNoiseSetVector(nativeID, noiseSet, vectorSet, xOffset, yOffset, zOffset);
	}
	
	public static float[] GetEmptyNoiseSet(int xSize,int ySize,int zSize){
		return new float[xSize*ySize*zSize];
	}	
}	
