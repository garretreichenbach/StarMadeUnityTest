package org.schema.game.common.data.element;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.SimplePosElement;

import it.unimi.dsi.fastutil.ints.IntCollection;

public abstract class Element {

	//	public final byte[] occlusion = new byte[]{99,99,99,99,99,99};

	public static final float BLOCK_SIZE = 1;
	public static final short TYPE_BLENDED_START = -1;
	public static final short TYPE_NONE = 0;
	public static final short TYPE_ALL = Short.MAX_VALUE;
	public static final short TYPE_SIGNAL = 30000;
	public static final short TYPE_RAIL_TRACK = 29999;
	public static final short TYPE_RAIL_INV = 29998;

	//	orientationMapping[Element.OLDFRONT] = 0;
	//	orientationMapping[Element.OLDBACK] = 1;
	//	orientationMapping[Element.OLDTOP] = 2;
	//	orientationMapping[Element.OLDBOTTOM] = 3;
	//	orientationMapping[Element.OLDRIGHT] = 4;
	//	orientationMapping[Element.OLDLEFT] = 5;
	//	orientationMapping[Element.OLDBACK_BACK] = 6;
	//	orientationMapping[Element.OLDBACK_LEFT] = 7;
	public static final int FRONT = 0;
	public static final int BACK = 1;
	public static final int TOP = 2;
	public static final int BOTTOM = 3;
	public static final int RIGHT = 4;
	public static final int LEFT = 5;
	public static final int[] OPPOSITE_SIDE = {
			BACK,
			FRONT,
			BOTTOM,
			TOP,
			LEFT,
			RIGHT

	};
	public static final int[][] dirs26 = {
			{-1, -1, -1},
			{1, -1, -1},
			{-1, -1, 1},
			{1, -1, 1},
			{-1, 1, -1},
			{1, 1, -1},
			{-1, 1, 1},
			{1, 1, 1},
	};
	public static final int FLAG_FRONT = 1;
	public static final int FLAG_BACK = 2;
	public static final int FLAG_TOP = 4;
	public static final int FLAG_BOTTOM = 8;
	public static final int FLAG_RIGHT = 16;
	public static final int FLAG_LEFT = 32;
	public static final int VIS_ALL = FLAG_FRONT + FLAG_BACK + FLAG_TOP + FLAG_BOTTOM + FLAG_RIGHT + FLAG_LEFT;
	public static final int[] SIDE_FLAG = {FLAG_FRONT, FLAG_BACK, FLAG_TOP, FLAG_BOTTOM, FLAG_RIGHT, FLAG_LEFT};
	public static final Vector3b[] DIRECTIONSb = {
			new Vector3b(0, 0, 1), new Vector3b(0, 0, -1),
			new Vector3b(0, 1, 0), new Vector3b(0, -1, 0),
			new Vector3b(1, 0, 0), new Vector3b(-1, 0, 0),

	};
	public static final Vector3i[] DIRECTIONSi = {
			new Vector3i(0, 0, 1), new Vector3i(0, 0, -1),
			new Vector3i(0, 1, 0), new Vector3i(0, -1, 0),
			new Vector3i(1, 0, 0), new Vector3i(-1, 0, 0),

	};
	public static final int[] SIGNIFICANT_COORD = {
			2, 2, 1, 1, 0, 0
	};
	public static final float[] COORD_DIR = {
			1, -1, 1, -1, 1, -1
	};
	public static final Vector4f[] SIDE_COLORS = {
			new Vector4f(1, 0, 0, 1),
			new Vector4f(0, 1, 0, 1),
			new Vector4f(0, 0, 1, 1),
			new Vector4f(1, 0, 1, 1),
			new Vector4f(0, 1, 1, 1),
			new Vector4f(1, 1, 0, 1),
	};
	public static final Vector3f[] DIRECTIONSf = GlUtil.DIRECTIONSf;

//	public static int permC = 0;
//	public static int permN = 0;
//	private static List<List<Vector3f>> binormalPerms;
//	private static List<Combo> combos = new ObjectArrayList<Combo>();
//	private static List<ComboS> comboSs = new ObjectArrayList<ComboS>();
//	private static List<List<Vector3f>> tangentPerms;
//	
//	static{
//		createTangents();
//		createBingents();
//		createCombiBingents();
//		createCombiSimpl();
//	}
//	0: T/B: (-1.0, 0.0, 0.0); (0.0, -1.0, 0.0)//ok one of them
//	1: T/B: (1.0, 0.0, 0.0); (0.0, 1.0, 0.0)//ok one of them
//	2: T/B: (0.0, 0.0, -1.0); (-1.0, 0.0, 0.0)
//	3: T/B: (0.0, 0.0, 1.0); (1.0, 0.0, 0.0)
//	4: T/B: (0.0, -1.0, 0.0); (0.0, 0.0, -1.0) 
//	5: T/B: (0.0, 1.0, 0.0); (0.0, 0.0, 1.0) 

//	
//	public static void createCombiSimpl(){
//		for(int i = 0; i < 6; i++){
//			for(int j = 0; j < 6; j++){
//				ComboS b = new ComboS(DIRECTIONSf[i], DIRECTIONSf[j]);
//				comboSs.add(b);
//			}			
//		}
//	}
//	public static void nextCombo(boolean d){
//		ComboS l = comboSs.get(permC);
//		
//		System.err.println("USING COMBO: "+permC+" / "+(comboSs.size()-1));
//		if(d){
//			permC = (permC+1)%comboSs.size();
//		}else{
//			permC = permC-1;
//			if(permC < 0){
//				permC = comboSs.size()-1;
//			}
//		}
//		
////		for(int i = 0; i < 2; i++){
//			TANGENTSf[3] = l.tan;
//			BIDIRECTIONSf[3] = l.bi;
////		}
////		TANGENTSf[0] = new Vector3f(-1.0f, 0.0f, 0.0f);
////		TANGENTSf[1] = new Vector3f(1.0f, 0.0f, 0.0f);
////		
////		BIDIRECTIONSf[0] = new Vector3f(0.0f, -1.0f, 0.0f);
////		BIDIRECTIONSf[1] = new Vector3f(0.0f, 1.0f, 0.0f);
//		
//		for(int i = 0; i < 6; i++){
//			System.err.println(i+": T/B: "+TANGENTSf[i]+"; "+BIDIRECTIONSf[i]);
//		}
//	}
//	public static void createTangents(){
//		List<Vector3f> l = new ObjectArrayList<Vector3f>();
//		for(Vector3f v : TANGENTSf){
//			l.add(v);
//		}
//		PermutationIterator<Vector3f> s = new PermutationIterator<Vector3f>(l);
//		
//		List<List<Vector3f>> pl = new ObjectArrayList<List<Vector3f>>();
//		while(s.hasNext()){
//			pl.add(s.next());
//		}
//		
//		
//		Iterator<List<Vector3f>> iterator = pl.iterator();
//		while(iterator.hasNext()){
//			List<Vector3f> mm = iterator.next();
//			for(int i = 0; i < DIRECTIONSf.length; i++){
//				Vector3f v = mm.get(i);
//				Vector3f o = DIRECTIONSf[i];
//				if(Math.abs(v.x) == Math.abs(o.x) && Math.abs(v.y) == Math.abs(o.y) && Math.abs(v.z) == Math.abs(o.z)){
//					iterator.remove();
//					break;
//				}
//			}
//		}
//		Element.tangentPerms = pl;
//		System.err.println("PERMS LOADED: ::: "+pl.size());
//	}
//	
//	
//	public static void nextTangents(){
//		List<Vector3f> l = tangentPerms.get(permN);
//		
//		System.err.println("USING TANGENTS: "+permN+" / "+(tangentPerms.size()-1));
//		permN = (permN+1)%tangentPerms.size();
//		
//		for(int i = 0; i < TANGENTSf.length; i++){
//			TANGENTSf[i] = l.get(i);
//			System.err.println(i+": "+TANGENTSf[i]);
//		}
//		
//		
//	}
//	private static class ComboS{
//		Vector3f bi;
//		Vector3f tan;
//		public ComboS(Vector3f bi, Vector3f tan) {
//			super();
//			this.bi = bi;
//			this.tan = tan;
//		}
//		
//		
//	}
//	private static class Combo{
//		Vector3f[] binormals = new Vector3f[6] ;
//		Vector3f[] tangents = new Vector3f[6] ;
//		public Combo(List<Vector3f> bi, List<Vector3f> tan){
//			for(int i = 0; i < 6; i++){
//				binormals[i] = bi.get(i);
//				tangents[i] = tan.get(i);
//			}
//		}
//		public boolean isValid(){
//			for(int i = 0; i < 6; i++){
//				Vector3f v = binormals[i];
//				Vector3f o = tangents[i];
//				if(Math.abs(v.x) == Math.abs(o.x) && Math.abs(v.y) == Math.abs(o.y) && Math.abs(v.z) == Math.abs(o.z)){
//					return false;
//				}
////				if(i > 1 && Math.abs(v.y) == 1){
////					return false;
////				}
////				if(i > 1 && Math.abs(o.x) == 1){
////					return false;
////				}
//			}
//			return true;
//		}
//	}
//	public static void createCombiBingents(){
//		for(List<Vector3f> bi : binormalPerms){
//			for(List<Vector3f> tan : tangentPerms){
//				Combo co = new Combo(bi, tan);
//				if(co.isValid()){
//					Element.combos.add(co);
//				}
//			}
//		}
//	}
//	public static void createBingents(){
//		List<Vector3f> l = new ObjectArrayList<Vector3f>();
//		for(Vector3f v : BIDIRECTIONSf){
//			l.add(v);
//		}
//		PermutationIterator<Vector3f> s = new PermutationIterator<Vector3f>(l);
//		
//		List<List<Vector3f>> pl = new ObjectArrayList<List<Vector3f>>();
//		while(s.hasNext()){
//			pl.add(s.next());
//		}
//		
//		
//		Iterator<List<Vector3f>> iterator = pl.iterator();
//		while(iterator.hasNext()){
//			List<Vector3f> mm = iterator.next();
//			for(int i = 0; i < DIRECTIONSf.length; i++){
//				Vector3f v = mm.get(i);
//				Vector3f o = DIRECTIONSf[i];
//				if(Math.abs(v.x) == Math.abs(o.x) && Math.abs(v.y) == Math.abs(o.y) && Math.abs(v.z) == Math.abs(o.z)){
//					iterator.remove();
//					break;
//				}
//				
//			}
//		}
//		Element.binormalPerms = pl;
//		System.err.println("PERMS LOADED: ::: "+pl.size());
//	}
//	public static int permT = 0;
//	public static void nextBingents(){
//		List<Vector3f> l = binormalPerms.get(permT);
//		
//		System.err.println("USING BINORMALS: "+permT+" / "+(binormalPerms.size()-1));
//		permT = (permT+1)%binormalPerms.size();
//		
//		for(int i = 0; i < BIDIRECTIONSf.length; i++){
//			BIDIRECTIONSf[i] = l.get(i);
//			System.err.println(i+": "+BIDIRECTIONSf[i]);
//		}
//		
//		
//	}

	public static final int SIDE_INDEX_COUNT = 4;
	public static final int INDEX_BOTTOM = 0;
	public static final int OLDRIGHT = 0;
	public static final int OLDLEFT = 1;
	public static final int OLDTOP = 2;

	//	public static final int INDEX_TOP 				= SIDE_INDEX_COUNT;
	//
	//	public static final int INDEX_FRONT 			= SIDE_INDEX_COUNT*2;
	//	public static final int INDEX_BACK 				= SIDE_INDEX_COUNT*4;
	//	public static final int INDEX_LEFT 				= SIDE_INDEX_COUNT*3;
	//	public static final int INDEX_RIGHT 			= SIDE_INDEX_COUNT*5;
	public static final int OLDBOTTOM = 3;
	public static final int OLDFRONT = 4;
	public static final int OLDBACK = 5;
	public static final int OLDBACK_BACK = 6;
	public static final int OLDBACK_LEFT = 7;
	private static final float margin = BLOCK_SIZE * 0.001f;

	public static byte[] orientationMapping;
	public static byte[] orientationBackMapping;
	/**
	 * value between 0 and 63 to determine which sides are visible
	 */
	public static byte FULLVIS = 63;

	private static final Vector3f sumTemp = new Vector3f(0.0F, 0.0F, 0.0F);

	//	public static byte[] orientationSBackMapping;
	static {
		createOrientationMapping();
	}

	public static int countBits(int x) {
		// collapsing partial parallel sums method
		// collapse 32x1 bit counts to 16x2 bit counts, mask 01010101
		x = (x >>> 1 & 0x55555555) + (x & 0x55555555);
		// collapse 16x2 bit counts to 8x4 bit counts, mask 00110011
		x = (x >>> 2 & 0x33333333) + (x & 0x33333333);
		// collapse 8x4 bit counts to 4x8 bit counts, mask 00001111
		x = (x >>> 4 & 0x0f0f0f0f) + (x & 0x0f0f0f0f);
		// collapse 4x8 bit counts to 2x16 bit counts
		x = (x >>> 8 & 0x00ff00ff) + (x & 0x00ff00ff);
		// collapse 2x16 bit counts to 1x32 bit count
		return (x >>> 16) + (x & 0x0000ffff);
	}

	private static void createOrientationMapping() {
		orientationMapping = new byte[24];
		orientationMapping[OLDFRONT] = 0;
		orientationMapping[OLDBACK] = 1;
		orientationMapping[OLDTOP] = 2;
		orientationMapping[OLDBOTTOM] = 3;
		orientationMapping[OLDRIGHT] = 4;
		orientationMapping[OLDLEFT] = 5;

		orientationMapping[OLDBACK_BACK] = 6;
		orientationMapping[OLDBACK_LEFT] = 7;
		orientationMapping[OLDFRONT + 8] = 8;
		orientationMapping[OLDBACK + 8] = 9;
		orientationMapping[OLDTOP + 8] = 10;
		orientationMapping[OLDBOTTOM + 8] = 11;
		orientationMapping[OLDRIGHT + 8] = 12;
		orientationMapping[OLDLEFT + 8] = 13;
		orientationMapping[OLDBACK_BACK + 8] = 14;
		orientationMapping[OLDBACK_LEFT + 8] = 15;

		orientationMapping[OLDFRONT + 16] = 16;
		orientationMapping[OLDBACK + 16] = 17;
		orientationMapping[OLDTOP + 16] = 18;
		orientationMapping[OLDBOTTOM + 16] = 19;
		orientationMapping[OLDRIGHT + 16] = 20;
		orientationMapping[OLDLEFT + 16] = 21;
		orientationMapping[OLDBACK_BACK + 16] = 22;
		orientationMapping[OLDBACK_LEFT + 16] = 23;

		orientationBackMapping = new byte[24];
		orientationBackMapping[0] = OLDFRONT;
		orientationBackMapping[1] = OLDBACK;
		orientationBackMapping[2] = OLDTOP;
		orientationBackMapping[3] = OLDBOTTOM;
		orientationBackMapping[4] = OLDRIGHT;
		orientationBackMapping[5] = OLDLEFT;
		orientationBackMapping[6] = OLDBACK_BACK;
		orientationBackMapping[7] = OLDBACK_LEFT;

		orientationBackMapping[8] = OLDFRONT + 8;
		orientationBackMapping[9] = OLDBACK + 8;
		orientationBackMapping[10] = OLDTOP + 8;
		orientationBackMapping[11] = OLDBOTTOM + 8;
		orientationBackMapping[12] = OLDRIGHT + 8;
		orientationBackMapping[13] = OLDLEFT + 8;
		orientationBackMapping[14] = OLDBACK_BACK + 8;
		orientationBackMapping[15] = OLDBACK_LEFT + 8;

		orientationBackMapping[16] = OLDFRONT + 16;
		orientationBackMapping[17] = OLDBACK + 16;
		orientationBackMapping[18] = OLDTOP + 16;
		orientationBackMapping[19] = OLDBOTTOM + 16;
		orientationBackMapping[20] = OLDRIGHT + 16;
		orientationBackMapping[21] = OLDLEFT + 16;
		orientationBackMapping[22] = OLDBACK_BACK + 16;
		orientationBackMapping[23] = OLDBACK_LEFT + 16;
		//
		//		orientationSBackMapping = new byte[8];
		//		orientationSBackMapping[0] = Element.TOP_FRONT;
		//		orientationSBackMapping[1] = Element.TOP_RIGHT;
		//		orientationSBackMapping[2] = Element.TOP_BACK;
		//		orientationSBackMapping[3] = Element.TOP_LEFT;
		//		orientationSBackMapping[4] = Element.BACK_FRONT;
		//		orientationSBackMapping[5] = Element.BACK_RIGHT;
		//		orientationSBackMapping[6] = Element.BACK_BACK;
		//		orientationSBackMapping[7] = Element.BACK_LEFT;

	}

	public static int getOpposite(int side) {

		switch(side) {
			case (LEFT):
				return RIGHT;
			case (RIGHT):
				return LEFT;
			case (TOP):
				return BOTTOM;
			case (BOTTOM):
				return TOP;
			case (FRONT):
				return BACK;
			case (BACK):
				return FRONT;
		}

		throw new RuntimeException("SIDE NOT FOUND: " + side);
	}

	public static int getClockWiseZ(int side) {

		switch(side) {
			case (LEFT):
				return TOP;
			case (RIGHT):
				return BOTTOM;
			case (TOP):
				return RIGHT;
			case (BOTTOM):
				return LEFT;
			case (FRONT):
				return FRONT;
			case (BACK):
				return BACK;
		}

		throw new RuntimeException("SIDE NOT FOUND: " + side);
	}

	public static int getCounterClockWiseZ(int side) {

		switch(side) {
			case (LEFT):
				return BOTTOM;
			case (RIGHT):
				return TOP;
			case (TOP):
				return LEFT;
			case (BOTTOM):
				return RIGHT;
			case (FRONT):
				return FRONT;
			case (BACK):
				return BACK;
		}

		throw new RuntimeException("SIDE NOT FOUND: " + side);
	}

	public static int getClockWiseX(int side) {

		switch(side) {
			case (LEFT):
				return LEFT;
			case (RIGHT):
				return RIGHT;
			case (TOP):
				return FRONT;
			case (BOTTOM):
				return BACK;
			case (FRONT):
				return BOTTOM;
			case (BACK):
				return TOP;
		}

		throw new RuntimeException("SIDE NOT FOUND: " + side);
	}

	public static int getCounterClockWiseX(int side) {

		switch(side) {
			case (LEFT):
				return LEFT;
			case (RIGHT):
				return FRONT;
			case (TOP):
				return BACK;
			case (BOTTOM):
				return FRONT;
			case (FRONT):
				return TOP;
			case (BACK):
				return BOTTOM;
		}

		throw new RuntimeException("SIDE NOT FOUND: " + side);
	}

	public static int getCounterClockWiseY(int side) {

		switch(side) {
			case (LEFT):
				return FRONT;
			case (RIGHT):
				return BACK;
			case (TOP):
				return TOP;
			case (BOTTOM):
				return BOTTOM;
			case (FRONT):
				return RIGHT;
			case (BACK):
				return LEFT;
		}

		throw new RuntimeException("SIDE NOT FOUND: " + side);
	}

	public static int getClockWiseY(int side) {

		switch(side) {
			case (LEFT):
				return BACK;
			case (RIGHT):
				return FRONT;
			case (TOP):
				return TOP;
			case (BOTTOM):
				return BOTTOM;
			case (FRONT):
				return LEFT;
			case (BACK):
				return RIGHT;
		}

		throw new RuntimeException("SIDE NOT FOUND: " + side);
	}

	public static Matrix3f getRotationPerSide(int side, int orientationBase) {
		return switch(orientationBase) {
			case FRONT -> getRotationPerSideFrontBase(side);
			case BACK -> getRotationPerSideBackBase(side);
			case TOP -> getRotationPerSideTopBase(side);
			case BOTTOM -> getRotationPerSideBottomBase(side);
			case RIGHT -> getRotationPerSideRightBase(side);
			case LEFT -> getRotationPerSideLeftBase(side);
			default -> throw new RuntimeException("orientation not found: " + orientationBase);
		};
	}

	public static Matrix3f getRotationPerSideFrontBase(int side) {
		Matrix3f r = new Matrix3f();
		r.setIdentity();
		switch(side) {
			case (FRONT) -> r.setIdentity();
			case (BACK) -> r.rotX(FastMath.PI);
			case (TOP) -> r.rotZ(-FastMath.HALF_PI);
			case (BOTTOM) -> r.rotZ(FastMath.HALF_PI);
			case (RIGHT) -> r.rotY(-FastMath.HALF_PI);
			case (LEFT) -> r.rotY(FastMath.HALF_PI);
		}
		return r;
	}

	public static Matrix3f getRotationPerSideBackBase(int side) {
		Matrix3f r = new Matrix3f();
		r.setIdentity();
		switch(side) {
			case (FRONT) -> r.rotX(FastMath.PI);
			case (BACK) -> r.setIdentity();
			case (TOP) -> r.rotZ(FastMath.HALF_PI);
			case (BOTTOM) -> r.rotZ(-FastMath.HALF_PI);
			case (RIGHT) -> r.rotY(FastMath.HALF_PI);
			case (LEFT) -> r.rotY(-FastMath.HALF_PI);
		}
		return r;
	}

	public static Matrix3f getRotationPerSideTopBase(int side) {
		Matrix3f r = new Matrix3f();
		r.setIdentity();
		switch(side) {
			case (FRONT) -> r.rotX(FastMath.HALF_PI);
			case (BACK) -> r.rotX(-FastMath.HALF_PI);
			case (TOP) -> r.setIdentity();
			case (BOTTOM) -> r.rotZ(-FastMath.PI);
			case (RIGHT) -> r.rotZ(FastMath.HALF_PI);
			case (LEFT) -> r.rotZ(-FastMath.HALF_PI);
		}
		return r;
	}

	public static Matrix3f getRotationPerSideBottomBase(int side) {
		Matrix3f r = new Matrix3f();
		r.setIdentity();
		switch(side) {
			case (FRONT) -> r.rotX(-FastMath.HALF_PI);
			case (BACK) -> r.rotX(FastMath.HALF_PI);
			case (TOP) -> r.rotZ(FastMath.PI);
			case (BOTTOM) -> r.setIdentity();
			case (RIGHT) -> r.rotZ(-FastMath.HALF_PI);
			case (LEFT) -> r.rotZ(FastMath.HALF_PI);
		}
		return r;
	}
	
	public static Matrix3f getRotationPerSideLeftBase(int side) {
		Matrix3f r = new Matrix3f();
		r.setIdentity();
		switch(side) {
			case (FRONT) -> r.rotY(-FastMath.HALF_PI);
			case (BACK) -> r.rotY(FastMath.HALF_PI);
			case (TOP) -> r.rotX(-FastMath.HALF_PI);
			case (BOTTOM) -> r.rotX(FastMath.HALF_PI);
			case (RIGHT) -> r.setIdentity();
			case (LEFT) -> r.rotZ(-FastMath.PI);
		}
		return r;
	}
	
	public static Matrix3f getRotationPerSideRightBase(int side) {
		Matrix3f r = new Matrix3f();
		r.setIdentity();
		switch(side) {
			case (FRONT) -> r.rotY(FastMath.HALF_PI);
			case (BACK) -> r.rotY(-FastMath.HALF_PI);
			case (TOP) -> r.rotX(FastMath.HALF_PI);
			case (BOTTOM) -> r.rotX(-FastMath.HALF_PI);
			case (RIGHT) -> r.rotZ(-FastMath.PI);
			case (LEFT) -> r.setIdentity();
		}
		return r;
	}

	public static int getRelativeOrientation(Vector3f forw) {
		int relativeOrientation = 0;
		if(Math.abs(forw.x) >= Math.abs(forw.y) && Math.abs(forw.x) >= Math.abs(forw.z)) {
			//x is biggest
			if(forw.x >= 0) {
				relativeOrientation = RIGHT;
			} else {
				relativeOrientation = LEFT;
			}
		} else if(Math.abs(forw.y) >= Math.abs(forw.x) && Math.abs(forw.y) >= Math.abs(forw.z)) {
			//y is biggest
			if(forw.y >= 0) {
				relativeOrientation = TOP;
			} else {
				relativeOrientation = BOTTOM;
			}

		} else if(Math.abs(forw.z) >= Math.abs(forw.y) && Math.abs(forw.z) >= Math.abs(forw.x)) {
			//z is biggest

			if(forw.z >= 0) {
				relativeOrientation = FRONT;
			} else {
				relativeOrientation = BACK;
			}

		}
		return relativeOrientation;
	}

	public static int getSide(Vector3f hitPoint, BlockShapeAlgorithm hitAlgorithm, Vector3i pos, short type, int orientation) {
		return getSide(hitPoint, hitAlgorithm, pos, type, orientation, null);
	}

	public static int getSide(Vector3f hitPoint, BlockShapeAlgorithm hitAlgorithm, Vector3i pos, short type, int orientation, IntCollection disabledSides) {
		return getSide(hitPoint, hitAlgorithm, pos, margin, type, orientation, disabledSides);
	}

	public static int getSide(Vector3f hitPoint, BlockShapeAlgorithm hitAlgorithm, Vector3i pos, float margin, short type, int orientation, IntCollection disabledSides) {

		int side = -1;

		if(hitAlgorithm != null && hitAlgorithm.hasValidShape()) {
			hitAlgorithm.getShapeCenter(sumTemp);
			sumTemp.add(new Vector3f(0.5F, 0.5F, 0.5F));
			Vector3f hitPointI = new Vector3f(hitPoint.x - 0.5f, hitPoint.y - 0.5F, hitPoint.z - 0.5F);
			Vector3f hitPointF = new Vector3f(hitPointI.x - (float) Math.floor(hitPointI.x),
					hitPointI.y - (float) Math.floor(hitPointI.y),
					hitPointI.z - (float) Math.floor(hitPointI.z));
			if(Math.abs(hitPointF.x - 0.5F) <= 0.45F &&
					Math.abs(hitPointF.y - 0.5F) <= 0.45F &&
					Math.abs(hitPointF.z - 0.5F) <= 0.45F) {
				hitPointF.sub(sumTemp);
				hitPointF.normalize();
				hitPointF.negate();

				float minDot = Float.MAX_VALUE;
				for(int sideI = 0; sideI < 6; ++sideI) {
					Vector3f dirF = DIRECTIONSf[sideI];
					float dot = dirF.dot(hitPointF);
					if(dot < minDot && (disabledSides == null || !disabledSides.contains(sideI))) {
						minDot = dot;
						side = sideI;
					}
				}
			}
		}

		if(side == -1) {
			float nMinX;
			float nMinY;
			float nMinZ;

			float nMaxX;
			float nMaxY;
			float nMaxZ;

			int slab;
			nMinX = pos.x - 0.5f;
			nMinY = pos.y - 0.5f;
			nMinZ = pos.z - 0.5f;

			nMaxX = pos.x + 0.5f;
			nMaxY = pos.y + 0.5f;
			nMaxZ = pos.z + 0.5f;
			if(ElementKeyMap.isValidType(type) && (slab = ElementKeyMap.getInfoFast(type).getSlab()) > 0) {
				float slabP = 0.5f - (slab * 0.25f);
				switch(switchLeftRight(orientation % 6)) {
					case FRONT -> {
						nMinX = pos.x - 0.5f;
						nMinY = pos.y - 0.5f;
						nMinZ = pos.z - 0.5f;
						nMaxX = pos.x + 0.5f;
						nMaxY = pos.y + 0.5f;
						nMaxZ = pos.z + slabP;
					}
					case BACK -> {
						nMinX = pos.x - 0.5f;
						nMinY = pos.y - 0.5f;
						nMinZ = pos.z - slabP;
						nMaxX = pos.x + 0.5f;
						nMaxY = pos.y + 0.5f;
						nMaxZ = pos.z + 0.5f;
					}
					case TOP -> {
						nMinX = pos.x - 0.5f;
						nMinY = pos.y - 0.5f;
						nMinZ = pos.z - 0.5f;
						nMaxX = pos.x + 0.5f;
						nMaxY = pos.y + slabP;
						nMaxZ = pos.z + 0.5f;
					}
					case BOTTOM -> {
						nMinX = pos.x - 0.5f;
						nMinY = pos.y - slabP;
						nMinZ = pos.z - 0.5f;
						nMaxX = pos.x + 0.5f;
						nMaxY = pos.y + 0.5f;
						nMaxZ = pos.z + 0.5f;
					}
					case RIGHT -> {
						nMinX = pos.x - 0.5f;
						nMinY = pos.y - 0.5f;
						nMinZ = pos.z - 0.5f;
						nMaxX = pos.x + slabP;
						nMaxY = pos.y + 0.5f;
						nMaxZ = pos.z + 0.5f;
					}
					case LEFT -> {
						nMinX = pos.x - slabP;
						nMinY = pos.y - 0.5f;
						nMinZ = pos.z - 0.5f;
						nMaxX = pos.x + 0.5f;
						nMaxY = pos.y + 0.5f;
						nMaxZ = pos.z + 0.5f;
					}
				}
			}

			if(hitPoint.x >= nMaxX - margin && (disabledSides == null || !disabledSides.contains(RIGHT))) {
				//			System.err.println(hitPoint.x+"     ---> "+nMaxX+" - "+margin+" = "+(nMaxX - margin));
				return SimplePosElement.RIGHT;

			} else if(hitPoint.y >= nMaxY - margin && (disabledSides == null || !disabledSides.contains(TOP))) {

				return SimplePosElement.TOP;

			} else if(hitPoint.z >= nMaxZ - margin && (disabledSides == null || !disabledSides.contains(FRONT))) {

				return SimplePosElement.FRONT;

			} else if(hitPoint.x <= nMinX + margin && (disabledSides == null || !disabledSides.contains(LEFT))) {
				return SimplePosElement.LEFT;
			} else if(hitPoint.y <= nMinY + margin && (disabledSides == null || !disabledSides.contains(BOTTOM))) {
				return SimplePosElement.BOTTOM;
			} else if(hitPoint.z <= nMinZ + margin && (disabledSides == null || !disabledSides.contains(BACK))) {
				return SimplePosElement.BACK;
			}
			if(-1 < 0 && margin < 0.5f) {
				//substepping
				margin *= 2;
				// Passing null, because if the algorithm made it this far the BlockShapeAlgorithm is unneeded.
				return getSide(hitPoint, null, pos, margin, type, orientation, disabledSides);
			}
		}
		return side;
	}

	public static String getSideString(int side) {
		return switch(side) {
			case (LEFT) -> "LEFT";
			case (RIGHT) -> "RIGHT";
			case (TOP) -> "TOP";
			case (BOTTOM) -> "BOTTOM";
			case (FRONT) -> "FRONT";
			case (BACK) -> "BACK";
			default -> "[WARNING] UNKNOWN SIDE " + side;
		};
	}

	public static String getSideStringLng(int side) {
		return switch(side) {
			case (LEFT) -> Lng.str("left");
			case (RIGHT) -> Lng.str("right");
			case (TOP) -> Lng.str("top");
			case (BOTTOM) -> Lng.str("bottom");
			case (FRONT) -> Lng.str("front");
			case (BACK) -> Lng.str("back");
			default -> "[WARNING] UNKNOWN SIDE " + side;
		};
	}

	public static int getSide(Vector3i dir) {
		assert (Math.abs(dir.x) + Math.abs(dir.y) + Math.abs(dir.z)) == 1;

		if(dir.x == 1) {
			return RIGHT;
		}
		if(dir.x == -1) {
			return LEFT;
		}
		if(dir.y == 1) {
			return TOP;
		}
		if(dir.y == -1) {
			return BOTTOM;
		}
		if(dir.z == 1) {
			return FRONT;
		}
		if(dir.z == -1) {
			return BACK;
		}
		throw new RuntimeException("ERROR ON DIR: " + dir);
	}

	public static void getRelativeForward(int axis, int dirSide, Vector3f out) {
		if(axis == TOP) {
			out.set(DIRECTIONSf[dirSide]);
		} else if(axis == BOTTOM) {

			if(dirSide == LEFT || dirSide == RIGHT) {
				out.set(DIRECTIONSf[dirSide]);
			} else {
				out.set(DIRECTIONSf[getOpposite(dirSide)]);
			}
		} else if(axis == FRONT || axis == BACK) {
			if(axis == BACK) {
				dirSide = getOpposite(dirSide);
			}
			switch(dirSide) {
				case BOTTOM -> {
					out.set(DIRECTIONSf[FRONT]);
				}
				case RIGHT -> {
					out.set(DIRECTIONSf[axis == FRONT ? RIGHT : LEFT]);
				}
				case TOP -> {
					out.set(DIRECTIONSf[BACK]);
				}
				case LEFT -> {
					out.set(DIRECTIONSf[axis == FRONT ? LEFT : RIGHT]);
				}
			}

		} else if(axis == RIGHT || axis == LEFT) {
			if(axis == LEFT) {
				dirSide = getOpposite(dirSide);
			}
			switch(dirSide) {
				case FRONT -> {
					out.set(DIRECTIONSf[axis == RIGHT ? FRONT : BACK]);
				}
				case TOP -> {
					out.set(DIRECTIONSf[LEFT]);
				}
				case BACK -> {
					out.set(DIRECTIONSf[axis == RIGHT ? BACK : FRONT]);
				}
				case BOTTOM -> {
					out.set(DIRECTIONSf[RIGHT]);
				}
			}
		}
	}

	@Override
	public String toString() {
		return "ELEMENT";
	}

	public static byte switchLeftRight(int x) {
		if(x == LEFT) {
			return RIGHT;
		} else if(x == RIGHT) {
			return LEFT;
		}
		return (byte) x;
	}

	private static final int[] s = new int[3 * 3 * 3];

	static {

		for(int i = 0; i < DIRECTIONSi.length; i++) {
			Vector3i e = DIRECTIONSi[i];
			assert (s[(e.x + 1) + (e.y + 1) * 3 + (e.z + 1) * 9] == 0);
			s[(e.x + 1) + (e.y + 1) * 3 + (e.z + 1) * 9] = i;
		}

		for(int i = 0; i < DIRECTIONSi.length; i++) {
			Vector3i v = DIRECTIONSi[i];
			assert (getDirectionFromCoords(v.x, v.y, v.z) == i);
		}
	}

	public static int getDirectionFromCoords(int x, int y, int z) {
		assert (Math.abs(x) < 2);
		assert (Math.abs(y) < 2);
		assert (Math.abs(z) < 2);
		assert (Math.abs(x) + Math.abs(y) + Math.abs(z) == 1) : x + ", " + y + ", " + z;
		return s[(x + 1) + (y + 1) * 3 + (z + 1) * 9];
	}

}