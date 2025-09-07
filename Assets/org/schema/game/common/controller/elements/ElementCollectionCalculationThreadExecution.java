package org.schema.game.common.controller.elements;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.elements.ElementCollectionManager.CollectionShape;
import org.schema.game.common.controller.elements.power.reactor.PowerConsumer;
import org.schema.game.common.controller.elements.shipyard.ShipyardUnit;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.util.FastCopyLongOpenHashSet;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class ElementCollectionCalculationThreadExecution<E extends ElementCollection<E, EC, EM>, EC extends ElementCollectionManager<E, EC, EM>, EM extends UsableElementManager<E, EC, EM>> {
	private final EC man;
	private final ObjectArrayList<E> collections = new ObjectArrayList<E>();
	private E lastElementCollection;
	private LongOpenHashSet closedCollection;
	private LongArrayList openCollection;
	private CollectionCalculationCallback callback;
	private int pointer;
	private LongArrayList rawCollection;
	private Random random = new Random();
	private final long updateInProgressSignature;
	private LongOpenHashSet totalSet;

	//	(-14, -77, -99) #0    (-13, -77, -99) --> 140301570375421 -> 140301570375422; -1
	//	(-14, -77, -99) #1    (-15, -77, -99) --> 140301570375421 -> 140301570375420; 1
	//	(-14, -77, -99) #2    (-14, -76, -99) --> 140301570375421 -> 140301570440955; -65534
	//	(-14, -77, -99) #3    (-14, -78, -99) --> 140301570375421 -> 140301570309887; 65534
	//	(-14, -77, -99) #4    (-14, -77, -98) --> 140301570375421 -> 140305865080577; -4294705156
	//	(-14, -77, -99) #5    (-14, -77, -100) --> 140301570375421 -> 140297275670265; 4294705156

	public ElementCollectionCalculationThreadExecution(
			EC man) {
		this.man = man;
		
		this.updateInProgressSignature = man.updateInProgress;

	}
	public int getRawSize(){
		FastCopyLongOpenHashSet r = man.rawCollection;
		return r != null ? r.size() : 0;
	}
	@Override
	public String toString() {
		LongArrayList rawCollection = this.rawCollection;
		return "EXEC("+man+"; raw: "+(rawCollection != null ? rawCollection.size() : -1)+")";
	}

	//	public static long[] vals = new long[]{-1L, 1L, -65534L, 65534L, -4294705156L, 4294705156L};
	public static void main(String[] at) {
		for (int z = -100; z < 100; z++) {
			for (int y = -100; y < 100; y++) {
				for (int x = -100; x < 100; x++) {

					Vector3i pos = new Vector3i(x, y, z);
					long index = ElementCollection.getIndex(pos);

					for (int i = 0; i < 6; i++) {
						Vector3i mod = new Vector3i(pos);
						mod.add(Element.DIRECTIONSi[i]);
						long indexMod = ElementCollection.getIndex(mod);

						//						assert((index - ElementCollection.vals[i]) == indexMod):index+"; "+indexMod+"; "+(index-indexMod)+"; "+ElementCollection.vals[i]+"; "+(index + ElementCollection.vals[i]);
						//												System.err.println(pos+" #"+i+"    "+mod+" --> "+index+" -> "+indexMod+"; "+(index-indexMod));

					}
				}
			}

		}
	}

	public void apply() {
		man.clearCollectionForApply();
		man.getElementCollections().addAll(collections);
		if(!collections.isEmpty() && collections.get(0) instanceof PowerConsumer){
			for(ElementCollection<?,?,?> s : collections){
				man.getElementManager().getManagerContainer().addConsumer(((PowerConsumer)s));
			}
		}
		if(!collections.isEmpty() && collections.get(0) instanceof PlayerUsableInterface){
			for(ElementCollection<?,?,?> s : collections){
				man.getElementManager().getManagerContainer().addPlayerUsable(((PlayerUsableInterface)s));
			}
		}
		if(!man.getSegmentController().isOnServer()){
			for(ElementCollection<?,?,?> s : collections){
				if(s.getMesh() != null && s.getMesh().isDraw()){
					//flag update so no flicker occurs 
					((GameClientState)man.getSegmentController().getState()).getWorldDrawer()
					.getSegmentDrawer().getElementCollectionDrawer().flagUpdate();
					break;
				}
			}
		}
		collections.clear();
	}

	public void flagUpdateFinished() {
		man.flagUpdateFinished(this);
	}

	public E getCollectionInstance() {
		E instance = man.newElementCollection(man.getEnhancerClazz(), man,
				man.getSegmentController());
		return instance;
	}

	/**
	 * @return the collections
	 */
	public ObjectArrayList<E> getCollections() {
		return collections;
	}

	/**
	 * @return the man
	 */
	public EC getMan() {
		return man;
	}

	@Override
	public int hashCode() {
		return man.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return man == ((ElementCollectionCalculationThreadExecution<?, ?, ?>) obj).man;
	}

	public void initialize(LongOpenHashSet closedCollection,
							LongOpenHashSet totalSet,
	                       LongArrayList rawCollection,
	                       LongArrayList openCollection, CollectionCalculationCallback callback) {
		this.totalSet = totalSet;
		this.closedCollection = closedCollection;
		this.openCollection = openCollection;
		this.rawCollection = rawCollection;
		this.callback = callback;
		this.pointer = 0;
	}

	private void onCalculationFinished() {
		onFinish();
		callback.callback(this);
	}

	public void onFinish() {
		lastElementCollection = null;
		closedCollection = null;
		openCollection = null;
	}
	private final List<CollectionSetContainer> collectionSetList = new ObjectArrayList<CollectionSetContainer>();
	private static List<LongOpenHashSet> setPool = new ObjectArrayList<LongOpenHashSet>();
	public static LongOpenHashSet allocateLongSet() {
		synchronized(setPool){
			if(setPool.isEmpty()){
				return new LongOpenHashSet(256);
			}else{
				return setPool.remove(setPool.size()-1);
			}
		}
	}
	public static void freeLongSet(LongOpenHashSet d) {
		synchronized(setPool){
			setPool.add(d);
		}
	}
	private int pidGen;
	private static class CollectionSetContainer{
		private float minX = Float.POSITIVE_INFINITY;
		private float minY = Float.POSITIVE_INFINITY;
		private float minZ = Float.POSITIVE_INFINITY;
		private float maxX = Float.NEGATIVE_INFINITY;
		private float maxY = Float.NEGATIVE_INFINITY;
		private float maxZ = Float.NEGATIVE_INFINITY;
		private int[] touching = new int[7];
		LongOpenHashSet s;
		private int id;
		public CollectionSetContainer(int id){
			this.id = id;;
			allocate();
		}
		public void allocate(){
			s = allocateLongSet();
		}
		public void free(){
			Arrays.fill(touching, 0);
			s.clear();
			freeLongSet(s);
		}
		public boolean contains(int x, int y, int z) {
			return x >= minX && x <= maxX &&
				y >= minY && y <= maxY &&
				z >= minZ && z <= maxZ ;
		}
		public void add(long n, int x, int y, int z, float radius) {
			s.add(n);
			
			minX = Math.min(minX, x-radius);
			minY = Math.min(minY, y-radius);
			minZ = Math.min(minZ, z-radius);
			
			maxX = Math.max(maxX, x+radius);
			maxY = Math.max(maxY, y+radius);
			maxZ = Math.max(maxZ, z+radius);
		}
		@Override
		public int hashCode() {
			return id;
		}
		@Override
		public boolean equals(Object obj) {
			return id == ((CollectionSetContainer)obj).id;
		}
		public void combine(CollectionSetContainer toCombi) {
			s.addAll(toCombi.s);
			minX = Math.min(minX, toCombi.minX);
			minY = Math.min(minY, toCombi.minY);
			minZ = Math.min(minZ, toCombi.minZ);
			
			maxX = Math.max(maxX, toCombi.maxX);
			maxY = Math.max(maxY, toCombi.maxY);
			maxZ = Math.max(maxZ, toCombi.maxZ);
			
			for(int i = 0; i < touching.length; i++){
				touching[i] += toCombi.touching[i];
			}
		}
		public void addTouching(int neighborsForThisBlock) {
			touching[neighborsForThisBlock]++;
		}
		
		
	}
	public void process() {
		assert (rawCollection.size() == closedCollection.size()) : rawCollection.size() + "; " + closedCollection.size();
	
		
		while (!closedCollection.isEmpty()) {
//			if(updateInProgressSignature == man.cancelUpdateStatus){
//				System.err.println(man+" Collection cancelled");
//			}
			if(man.requiredNeigborsPerBlock() == CollectionShape.PROXIMITY){
				
				final float proxyRadius = ((ProximityCollectionInterface)man).getGroupProximity();
				List<CollectionSetContainer> addedList = new ObjectArrayList<CollectionSetContainer>();
				//use different methodology to find proximity groups
				while (!closedCollection.isEmpty()) {
					long n;
					do {
						assert (!closedCollection.isEmpty());
						assert (pointer < rawCollection.size()) : "\n" + closedCollection + "; \n" + rawCollection;
						n = rawCollection.getLong(pointer);
	
						pointer++;
						assert (pointer < rawCollection.size() || closedCollection.contains(n)) : n + " " + pointer + "/" + rawCollection.size() + ";\n" + closedCollection + "; \n" + rawCollection;
					} while (!closedCollection.remove(n));
					//n is the next non-assigned position
					int x = ElementCollection.getPosX(n);
					int y = ElementCollection.getPosY(n);
					int z = ElementCollection.getPosZ(n);
					int neighborsForThisBlock = 0;
					for (int i = 0; i < 6; i++) {
						long index = ElementCollection.getSide(x, y, z, i);
						if(totalSet.contains(index)){
							neighborsForThisBlock++;
						}
					}
					
					
					for(int i = 0; i < collectionSetList.size(); i++){
						CollectionSetContainer setList = collectionSetList.get(i);
						if(setList.contains(x,y,z)){
							setList.add(n, x, y, z, proxyRadius);
							setList.addTouching(neighborsForThisBlock);
							addedList.add(setList);
						}
					}
				
					if(addedList.size() > 1){
						//need to combine sets
						CollectionSetContainer base = addedList.remove(addedList.size()-1);
						
						for(int i = 0; i < addedList.size(); i++){
							CollectionSetContainer toCombi = addedList.get(i);
							base.combine(toCombi);
							toCombi.free();
							collectionSetList.remove(toCombi);
						}
					}else if(addedList.isEmpty()){
						CollectionSetContainer s = new CollectionSetContainer(pidGen++);
						s.add(n, x, y, z, proxyRadius);
						s.addTouching(neighborsForThisBlock);
						collectionSetList.add(s);
					}
					
					addedList.clear();
				
				}
				//all positions grouped by proximity. create element collections 
				for(int i = 0; i < collectionSetList.size(); i++){
					CollectionSetContainer setElem = collectionSetList.get(i);
					
					E collectionInstance = getCollectionInstance();
					
					if(!collectionInstance.isDetailedNeighboringCollection()){
						collectionInstance.takeVolatileCollection();
					}
					collectionInstance.prevalid = true;
					
					//add all elements of set to actual element collection
					for(long l : setElem.s){
						int x = ElementCollection.getPosX(l);
						int y = ElementCollection.getPosY(l);
						int z = ElementCollection.getPosZ(l);
						
						collectionInstance.addElement(l, x, y, z);
					}
					for(int j = 0; j < setElem.touching.length; j++){
						//copy integrity parameters
						collectionInstance.touching[j] = setElem.touching[j]; 
					}
					
					collections.add(collectionInstance);
					lastElementCollection = collectionInstance;
					
					//clean up
					setElem.free();
				}
				collectionSetList.clear();
				
				
			}else{
				if (openCollection.isEmpty()) {
					//				if(lastElementCollection != null){
					//					System.err.println(lastElementCollection+" FOUND SIZE: "+lastElementCollection.size());
					//				}
					assert (!closedCollection.isEmpty());
					long n;
					do {
						assert (!closedCollection.isEmpty());
						assert (pointer < rawCollection.size()) : "\n" + closedCollection + "; \n" + rawCollection;
						n = rawCollection.getLong(pointer);
	
						pointer++;
						assert (pointer < rawCollection.size() || closedCollection.contains(n)) : n + " " + pointer + "/" + rawCollection.size() + ";\n" + closedCollection + "; \n" + rawCollection;
					} while (!closedCollection.remove(n));
					// find new start position
					long nextNotConsumedPoint = n;
					openCollection.add(nextNotConsumedPoint);
					E collectionInstance = getCollectionInstance();
	
					if(!collectionInstance.isDetailedNeighboringCollection()){
						collectionInstance.takeVolatileCollection();
					}
					collectionInstance.prevalid = true;
					int x = ElementCollection.getPosX(nextNotConsumedPoint);
					int y = ElementCollection.getPosY(nextNotConsumedPoint);
					int z = ElementCollection.getPosZ(nextNotConsumedPoint);
	
					collectionInstance.addElement(nextNotConsumedPoint, x, y, z);
	
					collections.add(collectionInstance);
	
					lastElementCollection = collectionInstance;
				}
				if(!openCollection.isEmpty()){
					if(man.requiredNeigborsPerBlock() == CollectionShape.ALL_IN_ONE){
						openCollection.clear(); //first block was already added
						//every block in one instance
						openCollection.addAll(closedCollection);
						
						
						while(!openCollection.isEmpty()){
							long lastPoint = openCollection.removeLong(openCollection.size() - 1);
							int x = ElementCollection.getPosX(lastPoint);
							int y = ElementCollection.getPosY(lastPoint);
							int z = ElementCollection.getPosZ(lastPoint);
							lastElementCollection.addElement(lastPoint, x, y, z );
							int neighborsForThisBlock = 0;
							for (int i = 0; i < 6; i++) {
								long index = ElementCollection.getSide(x, y, z, i);
								if(closedCollection.contains(index)){
									neighborsForThisBlock++;
								}
							}
							lastElementCollection.touching[neighborsForThisBlock]++;
						}
						assert(openCollection.isEmpty());
						
						closedCollection.clear();
					}else if(man.requiredNeigborsPerBlock() == CollectionShape.SEPERATED){
						//each block gets its own instance
						openCollection.clear();
					}else{
						
						while (!openCollection.isEmpty()) {
							long lastPoint = openCollection.removeLong(openCollection.size() - 1);
							int neighbor;
		
							int x = ElementCollection.getPosX(lastPoint);
							int y = ElementCollection.getPosY(lastPoint);
							int z = ElementCollection.getPosZ(lastPoint);
							int neighborsForThisBlock = 0;
							for (int i = 0; i < 6; i++) {
								long index = ElementCollection.getSide(x, y, z, i);
								if (closedCollection.remove(index)) {
									lastElementCollection.addElement(index, x + Element.DIRECTIONSi[i].x, y + Element.DIRECTIONSi[i].y, z + Element.DIRECTIONSi[i].z);
									openCollection.add(index);
									neighborsForThisBlock++;
								}
								else if(totalSet.contains(index)){
									neighborsForThisBlock++;
								}
								
								
							}
		
							lastElementCollection.touching[neighborsForThisBlock]++;
						}
						
					}
					
					lastElementCollection.setValid(true);
					//default is -1 so only some use it (like warpgate)
					if (man.requiredNeigborsPerBlock() != CollectionShape.ANY_NEIGHBOR) {
						
						if(man.requiredNeigborsPerBlock() == CollectionShape.LOOP){
							
							int requiredBlocksForLoop = 2;
							
							//if there are any blocks touching more or less than 2 other blocks
							for(int i = 0; i < 7; i++){
								if(i != requiredBlocksForLoop && lastElementCollection.touching[i] > 0){
									lastElementCollection.setValid(false);
									lastElementCollection.prevalid = false;
									break;
								}
							}
							
						}else if(man.requiredNeigborsPerBlock() == CollectionShape.RIP){
							int blocksWithOneNeighbor = 0;
							long endA = Long.MIN_VALUE;
							long endB = Long.MIN_VALUE;
							
							/*
							 * ensure that the unit is a C (rip) shape this is
							 * essentially the same method as the loop check, just
							 * that the minimum size must be 5, and all but exactly
							 * 2 blocks must have 2 neighbors. The 2 other blocks
							 * must have exactly one neighbor
							 */
							for (int in = 0; in < lastElementCollection.getNeighboringCollectionUnsave().size(); in++) {
								long index = lastElementCollection.getNeighboringCollectionUnsave().getLong(in);
								int x = ElementCollection.getPosX(index);
								int y = ElementCollection.getPosY(index);
								int z = ElementCollection.getPosZ(index);
								int neighborsForThisBlock = 0;
								for (int s = 0; s < 6; s++) {
									long sindex = ElementCollection.getSide(x, y, z, s);
		
									if (lastElementCollection.getNeighboringCollectionUnsave().contains(sindex)) {
										neighborsForThisBlock++;
									}
								}
								if (neighborsForThisBlock != 2) {
									if(neighborsForThisBlock == 1 && blocksWithOneNeighbor < 2){
										
										if(blocksWithOneNeighbor == 0){
											endA = index;
										}else{
											//make sure the end points have the
											//same order in a valid unit group
											if(index > endA){
												endB = endA;
												endA = index;
											}else{
												endB = index;
											}
										}
										
										blocksWithOneNeighbor++;
									}else{
										//so there is a block the error message can display on
										endA = endA == Long.MIN_VALUE ? index : endA; 
										endB = endB == Long.MIN_VALUE ? index : endB; 
										lastElementCollection.setValid(false);
										lastElementCollection.setInvalidReason("Unit Group Invalid! Must be in a C shape with exactly two ends");
										lastElementCollection.prevalid = false;
										break;
									}
								}
							}
							((ShipyardUnit)lastElementCollection).endA = endA;
							((ShipyardUnit)lastElementCollection).endB = endB;
							
							if(lastElementCollection.prevalid && blocksWithOneNeighbor != 2){
								//valid = false;
	//							lastElementCollection.setInvalidReason("Unit Group Invalid! Must be in a C shape with exactly two ends. (May not be complete loop)");
							}
							if(lastElementCollection.prevalid){
								assert(blocksWithOneNeighbor == 2); //condition to be valid anyways
								assert(endA != Long.MIN_VALUE);
								assert(endB != Long.MIN_VALUE);
								//check if both blocks have 2 coordinates in common (are in a line)
								
								int xA = ElementCollection.getPosX(endA);
								int yA = ElementCollection.getPosY(endA);
								int zA = ElementCollection.getPosZ(endA);
								
								int xB = ElementCollection.getPosX(endB);
								int yB = ElementCollection.getPosY(endB);
								int zB = ElementCollection.getPosZ(endB);
								
								boolean v = 
										(xA == xB && yA == yB) || 
										(xA == xB && zA == zB) || 
										(yA == yB && zA == zB);
								
								
								
								if(!v){
									lastElementCollection.setInvalidReason("Unit Group Invalid! The end blocks of the C must be in a straight line");
									lastElementCollection.setValid(false);
									lastElementCollection.prevalid  = false;
								}
								
							}
							
							
							
						}
	
					}
					
					
				}
			}
		}
		
	
		
		boolean preInitMesh = false;
		if(!man.getSegmentController().isOnServer() && Math.abs(man.drawnUpdateNumber - man.getSegmentController().getState().getNumberOfUpdate()) < 10){
			//this has been drawn within the second, so it is likely that it will continue to be draw
			//initialize the mesh before swapping to avoid flicker
			preInitMesh = true;
		}
		for(int i = 0; i < collections.size(); i++){
			E e = collections.get(i);
			
			e.setSize(e.getNeighboringCollectionUnsave().size());
			
			
			//deterministic on both server on client
			random.setSeed(man.getSegmentController().getId() + e.getSignificator() + e.size());
			
			//A random index is determined for missiles/AI to shoot at if it's targeting this collection
			int randomIndex = random.nextInt(e.size());
			e.setRandomlySelectedFromLastThreadUpdate(e.getNeighboringCollectionUnsave().getLong(randomIndex));
			
			if(e.prevalid){
				e.calculateExtraDataAfterCreationThreaded(updateInProgressSignature, totalSet);
			}
			if(!man.getSegmentController().isOnServer() && EngineSettings.CREATE_MANAGER_MESHES.isOn() && e.hasMesh()){
				e.calculateMesh(updateInProgressSignature, preInitMesh);
			}
			if(!man.isDetailedElementCollections()){
				e.storeRandomBlocksForNonDetailed();
				//to save on memeory, this option allows ElementCollectionsTo not store their data
				e.freeVolatileCollection();
				
				
			}
			
			
		}
		
		onCalculationFinished();
	}

}
