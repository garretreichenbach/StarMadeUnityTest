package org.schema.common.util;


/**
 * some jre dont support Integer.compare and Long.compare
 * 
 * TODO replace with default when java version updated to 8
 * @author schema
 *
 */
public class CompareTools {

	
	public static int compare(int a, int b){
//		return Integer.compare(a, b);
		return a > b ? 1 : (a < b ? -1 : 0);
	}
	public static int compare(long a, long b){
//		return Long.compare(a, b);
		return a > b ? 1 : (a < b ? -1 : 0);
	}
	public static int compare(float a, float b){
//		return Float.compare(a, b);
		return a > b ? 1 : (a < b ? -1 : 0);
	}
	public static int compare(double a, double b){
//		return Double.compare(a, b);
		return a > b ? 1 : (a < b ? -1 : 0);
	}
	public static int compare(short a, short b){
//		return Short.compare(a, b);
		return a - b;
	}
	public static int compare(byte a, byte b){
//		return Byte.compare(a, b);
		return a - b;
	}
	public static int compare(boolean a, boolean b) {
//		return Boolean.compare(a, b);
		return a == b ? 0 : (a ? 1 : -1);
	}
}
