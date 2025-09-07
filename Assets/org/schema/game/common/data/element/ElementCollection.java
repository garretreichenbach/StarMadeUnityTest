package org.schema.game.common.data.element;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.elements.*;
import org.schema.game.common.controller.elements.ModuleExplosion.ExplosionCause;
import org.schema.game.common.controller.elements.power.reactor.PowerInterface;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.config.ConfigEntityManager;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.Universe;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.sound.controller.AudioEmitter;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public abstract class ElementCollection<E extends ElementCollection<E, CM, EM>, CM extends ElementCollectionManager<E, CM, EM>, EM extends UsableElementManager<E, CM, EM>> implements Cancelable{
	private static final ObjectArrayList<LongArrayList> volatileCollectionPool = new ObjectArrayList<LongArrayList>();
	public static final long l = 0xFFFFFFFFFFFFL;
	public static final long SHORT_MAX2 = Short.MAX_VALUE * 2;
	public static final long SHORT_MAX2x2 = SHORT_MAX2 * SHORT_MAX2;
	public static final short INACTIVE = 1;
	public static final short INACTIVE_NO_DELEGATE = 2;
	public static final short ACTIVE = 3;
	public static final short ACTIVE_NO_DELEGATE = 4;
	public static final short FROM_DELIGATE_ECODE = 10;
	public static final short SENT_FROM_WIRELESS = 100;
	static final long yDelim = (Short.MAX_VALUE + 1) * 2;
	static final long zDelim = yDelim * yDelim;
	private static final long LONG_MIN = getIndex(Short.MIN_VALUE + 1, Short.MIN_VALUE + 1, Short.MIN_VALUE + 1);
	private LongArrayList neighboringCollection;
	public boolean valid = true;
	public boolean prevalid = true;
	public int indexDebug = 0;
	public long idPos = LONG_MIN;
	public final int[] touching = new int[7];
	public String getName(){
		return getClass().getSimpleName()+"["+idPos+"]";
	}
	//	public static long[] vals = new long[]{4294967296L, 281470681743360L, 65536L, 4294901760L, 1L, 65535L};

	//	public static <T extends ElementCollection<E, EC, EM>, EC extends ElementCollectionManager<T,EC, EM>, EM extends UsableElementManager<T, EC, EM>> T getInstanceOfT(Class<T> aClass,
	//			short clazz, ElementCollectionManager<T, EC, EM> col,
	//			final SegmentController controller)
	//					throws InstantiationException, IllegalAccessException,
	//					SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	//
	//		T newInstance = aClass.newInstance();
	//		newInstance.initialize(clazz, col, controller);
	//
	//		return newInstance;
	//	}
	//	private final Vector3i min = new Vector3i(Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE);
	//	private final Vector3i max = new Vector3i(Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE);
	//	public long getMax() {
	//		return max;
	//	}
	//	public long getMin() {
	//		return min;
	//	}
	public CM elementCollectionManager;
	protected long significator = LONG_MIN;
	private int minX = Integer.MAX_VALUE;
	private int minY = Integer.MAX_VALUE;
	private int minZ = Integer.MAX_VALUE;
	private int maxX = Integer.MIN_VALUE;
	private int maxY = Integer.MIN_VALUE;
	private int maxZ = Integer.MIN_VALUE;
	private SegmentController controller;
	private short clazzId;
	private SegmentPiece id = null;
	private boolean idChanged = true;
	private long randomlySelectedFromLastThreadUpdate;
	private int size;
	private ElementCollectionMesh mesh;
	private double integrity;
	private long[] nonDetailedCollectionRandoms;

	public static long getIndex(byte x, byte y, byte z, Segment segment) {
		return getIndex(segment.pos.x + x, segment.pos.y + y, segment.pos.z + z);
	}
	public PowerInterface getPowerInterface(){
		return (((ManagedSegmentController<?>) controller).getManagerContainer()).getPowerInterface();
	}
	
	public boolean isIntegrityUsed(){
		return true;
	}
	//	private long min = LONG_MIN;
	//	private long max = LONG_MIN;

	public static final long getIndex4(long posIndex, short type) {
		long index = ((long) (type & 0xFFFF) << 48) | posIndex & 0xFFFFFFFFFFFFL;
		return index;
	}
	public ConfigEntityManager getConfigManager() {
		return controller.getConfigManager();
	}
	public static final long getIndex4(Vector3i p, short type) {
		return getIndex4((short) p.x, (short) p.y, (short) p.z, type);
	}

	public static final long getIndex4(short vx, short vy, short vz, short type) {

		long index = ((long) (type & 0xFFFF) << 48) + ((long) (vz & 0xFFFF) << 32) + ((long) (vy & 0xFFFF) << 16) + (vx & 0xFFFF);

		return index;

	}
	
	@Override
	public boolean isCancelled(long updateSignture) {
		return elementCollectionManager.cancelUpdateStatus == updateSignture;
	}
	public boolean isUsingPowerReactors(){
		return controller.isUsingPowerReactors();
	}
	public static final long getIndex(int vx, int vy, int vz) {
		long index = ((long) (vz & 0xFFFF) << 32) + ((long) (vy & 0xFFFF) << 16) + (vx & 0xFFFF);
		return index;
	}

	public static final long getIndex(Vector3i v) {
		return getIndex(v.x, v.y, v.z);
	}

	public static long getPosIndexFrom4(long index) {
		long l = index & 0xFFFFFFFFFFFFL;
		return l;
	}

	public static long getSide(long index, int i) {
		return getIndex(
				Math.min(Short.MAX_VALUE, Math.max(Short.MIN_VALUE, getPosX(index) + Element.DIRECTIONSi[i].x)), 
				Math.min(Short.MAX_VALUE, Math.max(Short.MIN_VALUE, getPosY(index) + Element.DIRECTIONSi[i].y)), 
				Math.min(Short.MAX_VALUE, Math.max(Short.MIN_VALUE, getPosZ(index) + Element.DIRECTIONSi[i].z)));
	}
	public void fire(ControllerStateInterface unit, Timer timer){
	}
	public static long getSide(int x, int y, int z, int i) {
		return getIndex(
				Math.min(Short.MAX_VALUE, Math.max(Short.MIN_VALUE, x + Element.DIRECTIONSi[i].x)), 
				Math.min(Short.MAX_VALUE, Math.max(Short.MIN_VALUE, y + Element.DIRECTIONSi[i].y)), 
				Math.min(Short.MAX_VALUE, Math.max(Short.MIN_VALUE, z + Element.DIRECTIONSi[i].z)));
	}

	public static Vector3i getPosFromIndex(long index, Vector3i out) {
		out.set(getPosX(index), getPosY(index), getPosZ(index));
		assert (getIndex(out) == getPosIndexFrom4(index)) : getPosIndexFrom4(index) + " != " + getIndex(out) + "; " + out;
		return out;
	}

	public static Vector3f getPosFromIndex(long index, Vector3f out) {
		out.set(getPosX(index), getPosY(index), getPosZ(index));
		return out;
	}

	public static int getPosX(long index) {
		int x = (short) (index & 0xFFFF);
		return x;
	}
	public static String getPosString(long index) {
		return getPosX(index)+", "+getPosY(index)+", "+getPosZ(index);
	}

	//	private final Vector3i idPos = new Vector3i(Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE);

	public static int getPosY(long index) {
		int y = (short) ((index >> 16) & 0xFFFF);
		return y;
	}

	public static int getPosZ(long index) {
		int z = (short) ((index >> 32) & 0xFFFF);
		return z;
	}

	public static int getType(long index) {
		int type = (short) ((index >> 48) & 0xFFFF);
		return type;
	}

	public static void main(String[] asd) {

		System.err.println((long) (-16 & 0xFFFF));
		System.err.println((long) (-15 & 0xFFFF));
		System.err.println((long) (-14 & 0xFFFF));
		System.err.println("LL " + ((long) (1 & 0xFFFF) << 16));
		System.err.println("MAX 2 " + ((Short.MAX_VALUE + 1) * 2));
		System.err.println((long) (-14 & 0xFFFF) << 16);
		System.err.println((long) (-13 & 0xFFFF) << 16);
		System.err.println((long) (0 & 0xFFFF) << 32);
		System.err.println(getIndex(0, 0, 0));
		System.err.println(getIndex(Short.MIN_VALUE, Short.MIN_VALUE, Short.MIN_VALUE));
		System.err.println(getIndex(Short.MAX_VALUE, Short.MAX_VALUE, Short.MAX_VALUE));

		for (int zz = -100 * 16; zz < 100 * 16; zz += 16) {
			for (int yy = -100 * 16; yy < 100 * 16; yy += 16) {
				for (int xx = -100 * 16; xx < 100 * 16; xx += 16) {

					//		System.err.println("HANLING: "+xx+", "+yy+", "+zz);

					int segPosX = xx;
					int segPosY = yy;
					int segPosZ = zz;

					long xAbsStart = (segPosX & 0xFFFF);
					long yAbsStart = ((long) (segPosY & 0xFFFF) << 16);
					long zAbsStart = ((long) (segPosZ & 0xFFFF) << 32);
					long xAbs = xAbsStart;
					long yAbs = yAbsStart;
					long zAbs = zAbsStart;

					for (byte z = 0; z < SegmentData.SEG; z++) {
						for (byte y = 0; y < SegmentData.SEG; y++) {
							for (byte x = 0; x < SegmentData.SEG; x++) {

								long absIndex = xAbs + yAbs + zAbs;

								long normIndex = getIndex(segPosX + x, segPosY + y, segPosZ + z);
								assert (normIndex == absIndex) : "\n" + segPosX + "; " + segPosY + ", " + segPosZ + ";" +
										"\n" + x + "; " + y + ", " + z + ";\n"
										+ xAbs + ", " + yAbs + " " + zAbs + ";\n"
										+ ((long) ((segPosX + x) & 0xFFFF)) + ", " + ((long) ((segPosY + y) & 0xFFFF) << 16) + " " + ((long) ((segPosZ + z) & 0xFFFF) << 32) + ";\n"
										+ normIndex + "; " + absIndex;
								xAbs++;
							}
							xAbs = xAbsStart;
							yAbs += yDelim;
						}
						xAbs = xAbsStart;
						yAbs = yAbsStart;
						zAbs += zDelim;
					}
				}
			}
		}
		//		Vector3i t = new Vector3i();
		//		Vector3i t2 = new Vector3i();
		//		int min = 0;
		//		int max = 1;
		//		for(int z = min; z < max; z++){
		//			System.err.println("Z "+z);
		//			for(int y = min; y < max; y++){
		//
		//				for(int x = min; x < max; x++){
		//
		//
		//					for(int s = 0; s < Element.DIRECTIONSi.length; s++){
		//						t.set(x,y,z);
		//						t.add(Element.DIRECTIONSi[s]);
		//						System.err.println(t+": "+s+": "+getIndex(t));
		//					}
		//					t.set(x,y,z);
		//					long i = getIndex4((short)x,(short)y,(short)z,(short)5);
		//
		//					i = getIndex4(i, (short)5);
		//
		//					getPosFromIndex(i, t2);
		//
		//					if(!t.equals(t2)){
		//						throw new IllegalArgumentException(t+"; "+t2+"; index "+i);
		//					}
		//				}
		//			}
		//		}
	}

	public static void writeIndexAsShortPos(long index, DataOutput outputStream) throws IOException {
		outputStream.writeShort((short) (getPosX(index)));
		outputStream.writeShort((short) (getPosY(index)));
		outputStream.writeShort((short) (getPosZ(index)));
	}
	public float getExtraConsume() {
		return 1;
	}
	public int getEffectBonus() {
		return 1;
	}
	public static long getEncodeActivation(SegmentPiece p, boolean delegate, boolean active, boolean fromDelgate) {
		long absoluteIndex = p.getAbsoluteIndex();
		return ElementCollection.getIndex4(absoluteIndex, (short) ((active ? (delegate ? ACTIVE : ACTIVE_NO_DELEGATE) : (delegate ? INACTIVE : INACTIVE_NO_DELEGATE)) + (fromDelgate ? FROM_DELIGATE_ECODE : 0)));
	}

	public static long getDeactivation(int x, int y, int z, boolean delegate, boolean fromDelgate) {
		return getIndex4((short) x, (short) y, (short) z, (short) ((delegate ? INACTIVE : INACTIVE_NO_DELEGATE) + (fromDelgate ? FROM_DELIGATE_ECODE : 0)));
	}
	//	public Vector3f getCenter(Vector3f out){
	//		out.set(max.x - Math.abs(max.x - min.x) / 2f, max.y - Math.abs(max.y - min.y) / 2f, max.z - Math.abs(max.z - min.z) / 2f);
	//		return out;
	//	}

	public static long getActivation(int x, int y, int z, boolean delegate, boolean fromDelgate) {
		return getIndex4((short) x, (short) y, (short) z, (short) ((delegate ? ACTIVE : ACTIVE_NO_DELEGATE) + (fromDelgate ? FROM_DELIGATE_ECODE : 0)));
	}

	public static long getDeactivation(long index, boolean delegate, boolean fromDelgate) {
		return getIndex4(index, (short) ((delegate ? INACTIVE : INACTIVE_NO_DELEGATE) + (fromDelgate ? FROM_DELIGATE_ECODE : 0)));
	}

	public static long getActivation(long index, boolean delegate, boolean fromDelgate) {
		return getIndex4(index, (short) ((delegate ? ACTIVE : ACTIVE_NO_DELEGATE) + (fromDelgate ? FROM_DELIGATE_ECODE : 0)));
	}

	public static long getDeactivationWireless(long index, boolean delegate, boolean fromDelgate) {
		return getIndex4(index, (short) ((delegate ? INACTIVE : INACTIVE_NO_DELEGATE) + (fromDelgate ? FROM_DELIGATE_ECODE : 0) + ElementCollection.SENT_FROM_WIRELESS));
	}

	public static long getActivationWireless(long index, boolean delegate, boolean fromDelgate) {
		return getIndex4(index, (short) ((delegate ? ACTIVE : ACTIVE_NO_DELEGATE) + (fromDelgate ? FROM_DELIGATE_ECODE : 0) + ElementCollection.SENT_FROM_WIRELESS));
	}

	public static boolean isActiveFromActivationIndex(long index) {
		long activeRec = getType(index);
		return (activeRec == ElementCollection.ACTIVE || activeRec == ElementCollection.ACTIVE_NO_DELEGATE);
	}


	public static boolean isActiveFromType(long activeRec) {
		return (activeRec == ElementCollection.ACTIVE || activeRec == ElementCollection.ACTIVE_NO_DELEGATE);
	}

	public static boolean isDeligateFromActivationIndex(long index) {
		long activeRec = getType(index);
		return (activeRec == ElementCollection.INACTIVE || activeRec == ElementCollection.ACTIVE);

	}

	/**
	 * If there is a required neighbor count
	 * this gets set on the block calculations
	 *
	 * @return the valid
	 */
	public boolean isValid() {
		return valid;
	}
	/**
	 * used to calculate some extra data (like orientation for ship yards)
	 * to make calculating validity easier later
	 * @param updateSignture TODO
	 * @param totalCollectionSet TODO
	 * @param totalSet 
	 * @param preInitMesh 
	 */
	public void calculateExtraDataAfterCreationThreaded(long updateSignture, LongOpenHashSet totalCollectionSet){
	}
	public void onClear(){
		if(mesh != null){
			ElementCollectionMesh m = this.mesh;
			mesh = null;
			freeMesh(m);
		}
		if(this instanceof AudioEmitter) {
			((AudioEmitter)this).stopAudio();
		}
	}
	public boolean hasMesh(){
		return true;
	}
	public static final List<ElementCollectionMesh> meshPool = new ObjectArrayList<ElementCollectionMesh>();
	private static final int MAX_MESH_POOL = 512;
	public void calculateMesh(long updateSignture, boolean preInitMesh){
		assert(!controller.isOnServer());
		
		if(elementCollectionManager.cancelUpdateStatus == updateSignture){
//			System.err.println(this+" MESH CALC CANCELED");
			return;
		}
		ElementCollectionMesh m = getMeshInstance();
		m.calculate(this, updateSignture, neighboringCollection);
		
		this.mesh = m;
		
		if(preInitMesh && this.mesh != null){
//			System.err.println("[CLIENT] preinitializing mesh");
			long t = System.currentTimeMillis();
			this.mesh.initializeMesh();
			this.mesh.markDraw();
			
			long took = System.currentTimeMillis() - t;
			if(t > 30) {
				System.err.println("[MESH] time to pre-initialize mesh took long "+t+" ms");
			}
		}
		
	}
	public static void freeMesh(ElementCollectionMesh m){
		m.clear();
		
		synchronized(meshPool){
			ElementCollectionMesh.meshesInUse--;
			if(meshPool.size() < MAX_MESH_POOL){
				meshPool.add(m);
				return;
			}
		}
		long t = System.currentTimeMillis();
		//clean up mesh that will not be placed into the pool
		m.destroyBuffer();
		
		long taken = System.currentTimeMillis() - t;
		if(taken > 10) {
			System.err.println("[ElementCollection] WARNING: Mesh buffer data destruction took long: "+taken+" ms");
		}
	}
	public static ElementCollectionMesh getMeshInstance(){
		synchronized(meshPool){
			ElementCollectionMesh.meshesInUse++;
			if(meshPool.isEmpty()){
				return new ElementCollectionMesh();
			}else{
				return meshPool.remove(meshPool.size()-1);
			}
		}
	}
	/**
	 * If there is a required neighbor count
	 * this gets set on the block calculations
	 *
	 * @param valid the valid to set
	 */
	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public void decode4(long index, Vector4i out) {
		int typeE = (short) ((index >> 48) & 0xFFFF);

		int zE = (short) ((index >> 32) & 0xFFFF);

		int yE = (short) ((index >> 16) & 0xFFFF);

		int xE = (short) (index & 0xFFFF);

		out.set(xE, yE, zE, typeE);
	}

	public void addElement(long index, int x, int y, int z) {
		neighboringCollection.add(index);
		updateBB(x, y, z, index);
	}

	public void cleanUp() {
		onClear();
		clear();
	}

	public void clear() {
		assert(isDetailedNeighboringCollection() || neighboringCollection == null):this.getClass().getSimpleName()+"; "+isDetailedNeighboringCollection()+"; "+(neighboringCollection != null ? "EXISTS COL" : "NULL");
		if(isDetailedNeighboringCollection()){
			neighboringCollection.clear();
		}
		Arrays.fill(touching, 0);
		size = 0;
		integrity = 0;
		nonDetailedCollectionRandoms = null;
	}

	/**
	 * @param absPos
	 * @return true, if this element position is part of this collection
	 */
	public boolean contains(long index) {
		if(neighboringCollection == null){
			throw new NullPointerException("Class: "+this.getClass().getSimpleName()+" Overwrite isDetailedElementCollections() to enable this collection");
		}
		return neighboringCollection.contains(index);
	}

	/**
	 * @param absPos
	 * @return true, if this element position is part of this collection
	 */
	public boolean contains(Vector3i absPos) {
		if(neighboringCollection == null){
			throw new NullPointerException("Class: "+this.getClass().getSimpleName()+" Overwrite isDetailedElementCollections() to enable this collection");
		}
		return neighboringCollection.contains(getIndex(absPos));
	}

	/**
	 * @return the clazzId
	 */
	public short getClazzId() {
		return clazzId;
	}

	/**
	 * @return the controller
	 */
	public SegmentController getSegmentController() {
		return controller;
	}

	public SegmentPiece getElementCollectionId() {
		if (idChanged) {
			SegmentPiece pointUnsave = controller.getSegmentBuffer().getPointUnsave(getPosIndexFrom4(idPos));
			if(pointUnsave != null){
				id = pointUnsave;//autorequest true previously
				idChanged = false;
			}else{
				idChanged = true;
			}
		}
		return id;
	}

	public Vector3i getMax(Vector3i out) {
		out.set(maxX, maxY, maxZ);
		return out;
	}

	public Vector3i getMin(Vector3i out) {
		out.set(minX, minY, minZ);
		return out;
	}
	public Vector3f getMax(Vector3f out) {
		out.set(maxX, maxY, maxZ);
		return out;
	}
	
	public Vector3f getMin(Vector3f out) {
		out.set(minX, minY, minZ);
		return out;
	}

	public LongArrayList getNeighboringCollectionUnsave() {
		return neighboringCollection;
	}
	public LongArrayList getNeighboringCollection() {
		if(!elementCollectionManager.isDetailedElementCollections()){
			throw new IllegalArgumentException("Can't access details for this elementCollection. For memory reason it only saves size and bounding box ("+this.getClass().getName()+")");
		}
		return neighboringCollection;
	}

	/**
	 * @return the significator
	 */
	public long getSignificator() {
		return significator;
	}
	/**
	 * @return the significator
	 */
	public Vector3i getSignificator(Vector3i out) {
		if (significator == LONG_MIN) {
			out.set(Short.MIN_VALUE + 1, Short.MIN_VALUE + 1, Short.MIN_VALUE + 1);
		}
		getPosFromIndex(significator, out);
		return out;
	}

	public void initialize(short clazz, CM col, final SegmentController controller) {
		this.elementCollectionManager = col;
		this.controller = controller;
		this.clazzId = clazz;
		if(elementCollectionManager.isDetailedElementCollections()){
			this.neighboringCollection = new LongArrayList();
		}else{
			//this is a temorary collection just for calculation. it will be free after the calc thrad is done
			this.neighboringCollection = null;
		}
	}

	
	public static int takenCollections = 0;
	public void takeVolatileCollection(){
		this.neighboringCollection = getVolatileCollection();
	}
	private LongArrayList getVolatileCollection(){
		synchronized(volatileCollectionPool){
			takenCollections++;
			if(volatileCollectionPool.isEmpty()){
				return new LongArrayList();
			}else{
				return volatileCollectionPool.remove(volatileCollectionPool.size()-1);
			}
		}
	}
	public int getStoreRandomBlocksForNonDetailedAmount(){
		return VoidElementManager.COLLECTION_INTEGRITY_EXPLOSION_AMOUNT;
	}
	public void storeRandomBlocksForNonDetailed(){
		
		final int amount = Math.min(neighboringCollection.size(), getStoreRandomBlocksForNonDetailedAmount());
		nonDetailedCollectionRandoms = new long[amount];
		Random r = new Random();
		for(int i = 0; i < amount; i++){
			long randomElement = neighboringCollection.getLong(r.nextInt(neighboringCollection.size()));
			nonDetailedCollectionRandoms[i] = randomElement;
		}
	}
	public void freeVolatileCollection(){
		assert(!isDetailedNeighboringCollection()):this;
		
		int size = neighboringCollection.size();
		neighboringCollection.clear();
		synchronized (volatileCollectionPool) {
			takenCollections--;
			if(volatileCollectionPool.size() < 16){
				volatileCollectionPool.add(neighboringCollection);
			}
		}
		neighboringCollection = null;
	}
	
	public boolean onChangeFinished() {
		idChanged = true;
		
		
		
		
		if(elementCollectionManager.isUsingIntegrity()) {
			double integrity = VoidElementManager.COLLECTION_INTEGRITY_START_VALUE;
			integrity += (touching[0] * VoidElementManager.COLLECTION_INTEGRITY_BASE_TOUCHING_0);
			integrity += (touching[1] * VoidElementManager.COLLECTION_INTEGRITY_BASE_TOUCHING_1);
			integrity += (touching[2] * VoidElementManager.COLLECTION_INTEGRITY_BASE_TOUCHING_2);
			integrity += (touching[3] * VoidElementManager.COLLECTION_INTEGRITY_BASE_TOUCHING_3);
			integrity += (touching[4] * VoidElementManager.COLLECTION_INTEGRITY_BASE_TOUCHING_4);
			integrity += (touching[5] * VoidElementManager.COLLECTION_INTEGRITY_BASE_TOUCHING_5);
			integrity += (touching[6] * VoidElementManager.COLLECTION_INTEGRITY_BASE_TOUCHING_6);
			this.integrity = integrity;	
		}else {
			this.integrity = Float.POSITIVE_INFINITY;
		}
		
		elementCollectionManager.getElementManager().getManagerContainer().flagElementChanged();
		
		if(this instanceof AudioEmitter) {
			if(getElementCollectionId() == null) {
				System.err.println("Attempted to start element collection audio with no source block!!! This would cause a crash.");
				for (StackTraceElement entry : Thread.currentThread().getStackTrace()) System.err.println(entry);
			}
			else ((AudioEmitter)this).startAudio();
		}
		
		return true;
	}

	public void resetAABB() {
		significator = LONG_MIN;
		minX = Integer.MAX_VALUE;
		minY = Integer.MAX_VALUE;
		minZ = Integer.MAX_VALUE;
		maxX = Integer.MIN_VALUE;
		maxY = Integer.MIN_VALUE;
		maxZ = Integer.MIN_VALUE;
		id = null;
		idPos = LONG_MIN;
		idChanged = true;
		return;
	}
	public void onIntegrityHitForced(Damager from){
		assert(controller.isOnServer());
		long rd = randomlySelectedFromLastThreadUpdate;
		long pos = ElementCollection.getPosIndexFrom4(rd);
		short type = (short) ElementCollection.getType(rd);
		onIntegrityHit(pos, type, from);
	}
	public void onIntegrityHit(long pos, short type, Damager from){
		assert(controller.isOnServer());
		if(!controller.isOnServer() || !elementCollectionManager.isExplosiveStructure()){
			return;
		}
		long damageLong = (long) (VoidElementManager.COLLECTION_INTEGRITY_DAMAGE_PER_BLOCKS * this.size);
		int damage = (int) Math.min(VoidElementManager.COLLECTION_INTEGRITY_DAMAGE_MAX,
				Math.min(Integer.MAX_VALUE, Math.max(0, damageLong)));
		

		explodeOnServer(
				VoidElementManager.COLLECTION_INTEGRITY_EXPLOSION_AMOUNT, 
				pos, type, 
				VoidElementManager.COLLECTION_INTEGRITY_EXPLOSION_RATE,
				VoidElementManager.COLLECTION_INTEGRITY_EXPLOSION_RADIUS, 
				damage, 
				true, ExplosionCause.INTEGRITY, from);
	}
	protected void significatorUpdate(int x, int y, int z, int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, long index) {
		significator = getIndex(xMax - (xMax - xMin) / 2, yMax - (yMax - yMin) / 2, zMax - (zMax - zMin) / 2);
	}

	protected void significatorUpdateMin(int x, int y, int z, int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, long index) {
		long sig = ElementCollection.getIndex(x, y, z);
		if (sig > significator || significator == LONG_MIN) {
			significator = sig;
		}
	}

	protected void significatorUpdateZ(int x, int y, int z, int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, long index) {

		if (significator == LONG_MIN || z > getPosZ(significator)) {
			//			System.err.println(x+","+y+","+z+" SIG UPDATE: "+z+"; "+getPosZ(significator));
			assert (z == getPosZ(index));
			significator = index;
		}
	}

	public int size() {
		return size;
	}
	public void setSize(int size){
		this.size = size;
	}
	public int getBBTotalSize() {
		return (this.maxX - this.minX) + (this.maxY - this.minY) + (this.maxZ - this.minZ);
	}

	public int getAbsBBMult() {
		return Math.abs((this.maxX - this.minX)) * Math.abs(this.maxY - this.minY) * Math.abs((this.maxZ - this.minZ));
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + hashCode() + "[" + getMin(new Vector3i()) + "/" + getMax(new Vector3i()) + "](" + (neighboringCollection != null ? neighboringCollection.size() : "volatile")+ "; "+size+"; "+super.toString()+")";
	}

	protected void updateBB(int x, int y, int z, long index) {

		long xId = getPosX(idPos);
		long yId = getPosY(idPos);
		long zId = getPosZ(idPos);

		if (this.idPos == LONG_MIN || (x < xId || (x == xId && y < yId) || (x == xId && y == yId && z < zId))) {
			this.idPos = index;
			idChanged = true;
		}

		minX = Math.min(x, minX);
		minY = Math.min(y, minY);
		minZ = Math.min(z, minZ);

		maxX = Math.max(x + 1, maxX);
		maxY = Math.max(y + 1, maxY);
		maxZ = Math.max(z + 1, maxZ);

		significatorUpdate(x, y, z, minX, minY, minZ, maxX, maxY, maxZ, index);
	}

	public abstract ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol);

	/**
	 * @return the idPos
	 */
	public Vector3i getIdPos(Vector3i out) {
		if (idPos == LONG_MIN) {
			out.set(Short.MIN_VALUE + 1, Short.MIN_VALUE + 1, Short.MIN_VALUE + 1);
		}
		getPosFromIndex(idPos, out);
		return out;
	}

	//	/**
	//	 * @return the texCoords
	//	 */
	//	public Vector2f[] getTexCoords() {
	//		return texCoords;
	//	}
	
	/**
	 * only used by overwriting to save space
	 * @return a message on why this unit isn't valid
	 */
	public String getInvalidReason() {
		return null;
	}
	/**
	 * only used by overwriting to save space
	 */
	public void setInvalidReason(String invalidReason) {
	}

	public final boolean isDetailedNeighboringCollection() {
		return elementCollectionManager.isDetailedElementCollections();
	}

	public long getRandomlySelectedFromLastThreadUpdate() {
		return randomlySelectedFromLastThreadUpdate;
	}

	public void setRandomlySelectedFromLastThreadUpdate(
			long randomlySelectedFromLastThreadUpdate) {
		this.randomlySelectedFromLastThreadUpdate = randomlySelectedFromLastThreadUpdate;
	}

	public static long shiftIndex(long index3, int x, int y, int z) {
		return getIndex(getPosX(index3)+x, getPosY(index3)+y, getPosZ(index3)+z);
	}
	public static long shiftIndex4(long index4, int x, int y, int z) {
		return getIndex4((short)(getPosX(index4)+x), (short)(getPosY(index4)+y), (short)(getPosZ(index4)+z), (short)getType(index4));
	}
	
	public void explodeOnServer(int amount, long pos, short type, long explosionDelay, int radius, int damage, boolean chain, ExplosionCause cause, Damager from) {
		
		assert(controller.isOnServer());
		if(!controller.isOnServer()){
			try {
				throw new Exception("Invalid Explosion call on client (not a crash)");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}
		LongArrayList l = new LongArrayList(amount);
		
		int bRate = Math.min(size(), Math.max(1, size() / amount));
		
		if(isDetailedNeighboringCollection()){
			for(int i = 0; i < amount; i++){
				l.add(getNeighboringCollection().get(Universe.getRandom().nextInt(getNeighboringCollection().size())));
			}
		}else{
			if(nonDetailedCollectionRandoms != null){
				final int needed = Math.min(VoidElementManager.COLLECTION_INTEGRITY_EXPLOSION_AMOUNT, nonDetailedCollectionRandoms.length);
				for(int i = 0; i < needed; i++){
					l.add(nonDetailedCollectionRandoms[i]);
				}
			}
			
		}
		
		System.err.println("[SERVER] Module Explosion "+ controller +": amount "+amount+" (actual "+l.size()+", rate "+bRate+") of "+size()+", delay "+explosionDelay+", radius "+radius+", damage "+damage);
		
		ModuleExplosion expl = 
				new ModuleExplosion(l,
						explosionDelay, 
						radius, 
						damage, 
						idPos, 
						cause,
						new BoundingBox(new Vector3i(minX, minY, minZ), new Vector3i(maxX, maxY, maxZ)));
		
		expl.setChain(chain);
		
		((ManagedSegmentController<?>) controller).getManagerContainer().addModuleExplosions(expl);
	}
	public static boolean overlaps(ElementCollection<?,?,?> a, ElementCollection<?,?,?> b, int tolA, int tolB) {
		int aMaxX = a.maxX+tolA;
		int aMaxY = a.maxY+tolA;
		int aMaxZ = a.maxZ+tolA;

		int aMinX = a.minX-tolA;
		int aMinY = a.minY-tolA;
		int aMinZ = a.minZ-tolA;
		
		int bMbxX = b.maxX+tolB;
		int bMbxY = b.maxY+tolB;
		int bMbxZ = b.maxZ+tolB;
		
		int bMinX = b.minX-tolB;
		int bMinY = b.minY-tolB;
		int bMinZ = b.minZ-tolB;
		
		boolean overlap = true;
		overlap = 				!(aMinX > bMbxX || aMaxX < bMinX);
		overlap = overlap && 	!(aMinY > bMbxY || aMaxY < bMinY);
		overlap = overlap && 	!(aMinZ > bMbxZ || aMaxZ < bMinZ);
		return overlap;
		
		
	}
	public ElementCollectionMesh getMesh() {
		return mesh;
	}
	public void markDraw() {
		if(mesh != null){
			mesh.markDraw();
		}
	}
	public void setDrawColor(Vector4f c) {
		if(mesh != null){
			mesh.setColor(c);
		}
	}
	public void setDrawColor(float r, float g, float b, float a){
		if(mesh != null){
			mesh.setColor(r, g, b, a);
		}
	}
	public double getIntegrity() {
		return integrity;
	}
	public boolean containsAABB(long index) {
		int x = getPosX(index);
		int y = getPosY(index);
		int z = getPosZ(index);
		
		
		return x >= minX && x < maxX &&
				y >= minY && y < maxY &&
				z >= minZ && z < maxZ;
	}
}
