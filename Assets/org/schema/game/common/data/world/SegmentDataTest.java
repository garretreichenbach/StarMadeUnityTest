package org.schema.game.common.data.world;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

public class SegmentDataTest {

	public static final int SEG = Segment.DIM;
	
	public static final int TOTAL = SEG * SEG * SEG;
	
	public static final int BLOCK = 3;
	
	private final byte[] data = new byte[TOTAL*BLOCK];
	private final int[] intData = new int[TOTAL];
	
	public static final int typeIndexStart = 0; //11 bits -> 2048
	public static final int hitpointsIndexStart = 11; //7 bits -> 128
	public static final int activeIndexStart = 18; //1 bit -> 1
	public static final int orientationStart = 19; //5 bit -> 16
	

	public static final int typeMask 	= Integer.parseInt("000000000000011111111111", 2);
	public static final int typeMaskNot 	= ~typeMask;
	public static final int hpMask 		= Integer.parseInt("000000111111100000000000", 2);
	public static final int hpMaskNot 		= ~hpMask;
	public static final int activeMask 	= Integer.parseInt("000001000000000000000000", 2);
	public static final int activeMaskNot 	= ~activeMask;
	public static final int orientMask 	= Integer.parseInt("111110000000000000000000", 2);
	public static final int orientMaskNot 	= ~orientMask;
	
	public int getValue(int index){
		int i = index * BLOCK;
		int val = (data[i] & 0xFF) + ((data[i+1] & 0xFF) << 8) + ((data[i+2] & 0xFF) << 16); 
		return val;
	}
	public int getType(int index){
		return (typeMask & getValue(index));
	}
	public void putType(int index, short value){
		put(index, (getValue(index) & typeMaskNot) | value);
	}
	public int getHitpoints(int index){
		return (hpMask & getValue(index)) >> hitpointsIndexStart;
	}
	public void putHitpoints(int index, byte value){
		put(index, (getValue(index) & hpMaskNot) | (value << hitpointsIndexStart));
	}
	public boolean getActivation(int index){
		return (activeMask & getValue(index)) > 0;
	}
	public void putActivation(int index, boolean value){
		put(index, value ? (getValue(index) | activeMask) : (getValue(index) & activeMaskNot) );
	}
	public int getOrientation(int index){
		return (orientMask & getValue(index)) >> orientationStart;
	}
	public void putOrientation(int index, byte value){
		put(index, (getValue(index) & orientMaskNot) | (value << orientationStart));
	}
	public void put(int index, int value){
		int i = index * BLOCK;
		data[i] = (byte) (value & 0xFF);
		data[i+1] = (byte) ((value >> 8) & 0xFF);
		data[i+2] = (byte) ((value >> 16) & 0xFF);
	}
	
	
	
	public int getValueInt(int index){
		return intData[index];
	}
	public int getTypeInt(int index){
		return (typeMask & intData[index]);
	}
	public void putIntTypeInt(int index, short value){
		intData[index] = (intData[index] & typeMaskNot) | value;
	}
	public int getHitpointsInt(int index){
		return (hpMask & intData[index]) >> hitpointsIndexStart;
	}
	public void putIntHitpointsInt(int index, byte value){
		intData[index] = (intData[index] & hpMaskNot) | (value << hitpointsIndexStart);
	}
	public boolean getActivationInt(int index){
		return (activeMask & intData[index]) > 0;
	}
	public void putIntActivationInt(int index, boolean value){
		intData[index] = value ? (intData[index] | activeMask) : (intData[index] & activeMaskNot) ;
	}
	public int getOrientationInt(int index){
		return (orientMask & intData[index]) >> orientationStart;
	}
	public void putIntOrientationInt(int index, byte value){
		intData[index] = (intData[index] & orientMaskNot) | (value << orientationStart);
	}
	
	@Test
	public void test() {
		
		final int testSize = 1000;
		
		short[] typeArray = new short[TOTAL];
		byte[] hpArray = new byte[TOTAL];
		boolean[] actArray = new boolean[TOTAL];
		byte[] orientArray = new byte[TOTAL];
		
		long time = System.currentTimeMillis();
		for(int n = 0; n < testSize; n++){
			Random r = new Random(n);
			for(int i = 0; i < TOTAL; i++){
				short type = (short)r.nextInt(2048);
				byte hp = (byte)(r.nextInt(128));
				boolean act = r.nextBoolean();
				byte orientation = (byte)r.nextInt(32);
				putType(i, type);
				putHitpoints(i, hp);
				putActivation(i, act);
				putOrientation(i, orientation);
				typeArray[i] = type;
				hpArray[i] = hp;
				actArray[i] = act;
				orientArray[i] = orientation;
			}
			for(int i = 0; i < TOTAL; i++){
				assertEquals(getType(i), typeArray[i]);
				assertEquals(getHitpoints(i), hpArray[i]);
				assertEquals(getActivation(i), actArray[i]);
				assertEquals(getOrientation(i), orientArray[i]);
			
			}
		}
		long taken3Byte = System.currentTimeMillis() - time;
		time = System.currentTimeMillis();
		for(int n = 0; n < testSize; n++){
			Random r = new Random(n);
			for(int i = 0; i < TOTAL; i++){
				short type = (short)r.nextInt(2048);
				byte hp = (byte)(r.nextInt(128));
				boolean act = r.nextBoolean();
				byte orientation = (byte)r.nextInt(32);
				putIntTypeInt(i, type);
				putIntHitpointsInt(i, hp);
				putIntActivationInt(i, act);
				putIntOrientationInt(i, orientation);
				typeArray[i] = type;
				hpArray[i] = hp;
				actArray[i] = act;
				orientArray[i] = orientation;
			}
			for(int i = 0; i < TOTAL; i++){
				assertEquals(getTypeInt(i), typeArray[i]);
				assertEquals(getHitpointsInt(i), hpArray[i]);
				assertEquals(getActivationInt(i), actArray[i]);
				assertEquals(getOrientationInt(i), orientArray[i]);
				
			}
		}
		
		long takenInt = System.currentTimeMillis() - time;
		
		
		time = System.currentTimeMillis();
		for(int n = 0; n < testSize; n++){
			for(int i = 0; i < TOTAL; i++){
				assertEquals(getType(i), typeArray[i]);
				assertEquals(getHitpoints(i), hpArray[i]);
				assertEquals(getActivation(i), actArray[i]);
				assertEquals(getOrientation(i), orientArray[i]);
			
			}
		}
		long taken3ByteRead = System.currentTimeMillis() - time;
		time = System.currentTimeMillis();
		for(int n = 0; n < testSize; n++){
			for(int i = 0; i < TOTAL; i++){
				assertEquals(getTypeInt(i), typeArray[i]);
				assertEquals(getHitpointsInt(i), hpArray[i]);
				assertEquals(getActivationInt(i), actArray[i]);
				assertEquals(getOrientationInt(i), orientArray[i]);
				
			}
		}
		
		long takenIntRead = System.currentTimeMillis() - time;
		
		
		
		System.err.println("3Byte: "+taken3Byte+";");
		System.err.println("Int  : "+takenInt+";");
		System.err.println("% "+(float)takenInt / (float)taken3Byte);
		System.err.println("3ByteRead: "+taken3ByteRead+";");
		System.err.println("IntRead  : "+takenIntRead+";");
		System.err.println("% "+(float)takenIntRead / (float)taken3ByteRead);
		
		
	}
	public static void main(String arg[]){
		SegmentDataTest t = new SegmentDataTest();
		t.test();
	}

	
	
}
