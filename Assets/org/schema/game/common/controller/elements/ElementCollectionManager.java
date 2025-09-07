package org.schema.game.common.controller.elements;

import api.listener.events.systems.ElementCollectionManagerInstantiateEvent;
import api.mod.StarLoader;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.elements.cargo.CargoCollectionManager;
import org.schema.game.common.controller.elements.power.reactor.PowerConsumer;
import org.schema.game.common.controller.elements.power.reactor.PowerInterface;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.config.ConfigEntityManager;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.util.FastCopyLongOpenHashSet;
import org.schema.game.server.ai.AIFireState;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.network.StateInterface;

import java.util.ArrayList;
import java.util.List;

public abstract class ElementCollectionManager<E extends ElementCollection<E, EC, EM>, EC extends ElementCollectionManager<E, EC, EM>, EM extends UsableElementManager<E, EC, EM>> {

	
	public enum CollectionShape{
		ANY_NEIGHBOR,
		LOOP,
		RIP, 
		SEPERATED, 
		ALL_IN_ONE, 
		PROXIMITY, 
	}
	public short drawnUpdateNumber;
	protected static final long UPDATE_FREQUENCY_MS = 50;
	private static final long INTEGRITY_CHECK_MS = 3000L;
	private static int debugIdGen;
//	public long initializationStart;
	private final SegmentController segmentController;
	private final short enhancerClazz;
	private final EM elementManager;
	public long nextShot;
	public int expected = 2;
	public FastCopyLongOpenHashSet rawCollection;
	protected long lastUpdate;
	protected long lastUpdateLocal;
	//	private final LongArrayFIFOQueue modsA = new LongArrayFIFOQueue();
	boolean modSwitch = false;
	private int debugID;
	private List<E> elementCollections;
	private Object updateLock = new Object();
	private ElementCollectionCalculationThreadExecution<E, EC, EM> finishedThread;
	private FastCopyLongOpenHashSet scheduledListToUpdate;
	private long flagDirty = -1;
	private long updateStatus = -1;
	private boolean stopped;
	//	private LongArrayFIFOQueue modsA = new LongArrayFIFOQueue();
	private boolean addException;
	private boolean delException;
	private int totalSize;
	private ArrayList<E> failedFinishedChanges = new ArrayList<E>();
	private long lastFailUpdateLocal;
	long updateInProgress = Long.MIN_VALUE;
	public long cancelUpdateStatus = Long.MIN_VALUE;
	public long lastReloading;
	public double reloadingNeeded;
	private double lowestIntegrity = Double.POSITIVE_INFINITY;
	private double lowestIntegrityActual = Double.POSITIVE_INFINITY;
	private long lastRemoved = Long.MIN_VALUE;
	private long lastIntegrityCheck;
	
	public boolean isUsingPowerReactors(){
		return segmentController.isUsingPowerReactors();
	}
	public ElementCollectionManager(final short clazz, final SegmentController segController, final EM elementManager) {
		debugID = debugIdGen++;
		this.enhancerClazz = clazz;
		this.segmentController = segController;
		this.elementManager = elementManager;
		//INSERTED CODE @???
		ElementCollectionManagerInstantiateEvent event = new ElementCollectionManagerInstantiateEvent(this, enhancerClazz, segmentController, elementManager);
		StarLoader.fireEvent(event, isOnServer());
		///
	}
	public void checkIntegrityForced(Damager from){
		if(!elementManager.isExplosiveStructure()) {
			return;
		}
		if(getLowestIntegrity() < VoidElementManager.INTEGRITY_MARGIN){
			List<E> eCol = getElementCollections();
			final int colSize = eCol.size();
			for(int j = 0; j < colSize; j++){
				E e = eCol.get(j);
				e.onIntegrityHitForced(from);
			}
		}
	}
	/**
	 * player switched to this module
	 * @param on
	 */
	public void onSwitched(boolean on) {
	}
	public boolean isOnServer() {
		return segmentController.isOnServer();
	}
	public void sendServerMessage(Object[] astr, byte msgType) {
		segmentController.sendServerMessage(astr, msgType);
	}
	public void checkIntegrity(long pos, short type, Damager from){
		if(!segmentController.isOnServer() || !elementManager.isExplosiveStructure()){
			return;
		}
//		assert(false):(lowestIntegrity < VoidElementManager.INTEGRITY_MARGIN)+"; "+(lastRemoved == index4)+"; "+(lastRemoved == pos)+"; "+lastRemoved+"; "+pos+"; "+index4; 
		long uTime = segmentController.getUpdateTime();
		if(getLowestIntegrity() < VoidElementManager.INTEGRITY_MARGIN && uTime > lastIntegrityCheck + INTEGRITY_CHECK_MS && lastRemoved == pos){
			lastIntegrityCheck = uTime;
			List<E> eCol = getElementCollections();
			final int colSize = eCol.size();
			for(int j = 0; j < colSize; j++){
				E e = eCol.get(j);
				e.onIntegrityHit(pos, type, from);
			}
		}
	}
	public boolean isAddToPlayerUsable() {
		return true;
	}
	public void onPlayerDetachedFromThisOrADock(ManagedUsableSegmentController<?> originalCaller, PlayerState pState,
			PlayerControllable newAttached){
		
	}
	public CollectionShape requiredNeigborsPerBlock() {
		return CollectionShape.ANY_NEIGHBOR;
	}
	protected long getDirtyState(){
		return flagDirty;
	}
	//	public void addModded(long index){
	//		doAdd(index);
	////		synchronized(modsA){
	////			modsA.enqueue(index);
	////		}
	//	}
	public void addModded(Vector3i absPos, short toType) {
		long index = ElementCollection.getIndex(absPos);
		doAdd(index, toType);
	}
	public String getName(){
		return getClass().getSimpleName();
	}
	public void clear() {
		stopUpdate();
		collectionCleanUp();
		flagDirty();
		totalSize = 0;
		updateInProgress = Long.MIN_VALUE;
	}
	public boolean isDetailedElementCollections(){
		return true;
	}
	private void collectionCleanUp() {
		long time = System.currentTimeMillis();
		synchronized (updateLock) {
			if (rawCollection != null) {
//				if(this instanceof CargoCollectionManager){
//					System.err.println("CLEAR --> SCHDULED UPDATE. RAW COLLECTION: "+rawCollection.size());
//				}
				rawCollection.clear();
			}
		}
		for (ElementCollection<E, EC, EM> e : getElementCollections()) {
			e.cleanUp();
		}
		getElementCollections().clear();

		long took = (System.currentTimeMillis() - time);
		if (took > 10) {
			System.err.println("[ELEMENTCOLLECTIONMANAGER][CLEAR] WARNING COLLECTION CLEANUP OF " + segmentController + " ON " + segmentController.getState() + " TOOK " + took);
		}
	}

	public PowerInterface getPowerInterface(){
		return (((ManagedSegmentController<?>) segmentController).getManagerContainer()).getPowerInterface();
	}
	public ConfigEntityManager getConfigManager(){
		return segmentController.getConfigManager();
	}
	public void doAdd(long index, short toType) {
		if (rawCollection == null) {
			rawCollection = new FastCopyLongOpenHashSet(expected);
		}
		if(this instanceof CargoCollectionManager){
			System.err.println("ADD --> SCHDULED UPDATE. RAW COLLECTION: "+rawCollection.size());
		}
		boolean add = rawCollection.add(index);
		if (add) {
			flagDirty();
			
		} else {
			//			System.err.println(getSegmentController().getState()+"; "+getSegmentController()+" WARNING: duplicate add ");
			if (!addException) {
				addException = true;
				//INFO: can happen when all connected blocks are added with adding of controller,
				//while some are already added from another source
				System.err.println(segmentController.getState() + ": multiple add in " + this + "; " + segmentController + "; onePos: " + ElementCollection.getPosFromIndex(index, new Vector3i()));
			}

		}
	}

	public boolean doRemove(long index) {
//		assert (Thread.currentThread().getName().equals("ClientThread") || Thread.currentThread().getName().equals("ServerController")) : Thread.currentThread().getName();
		boolean remove = rawCollection.remove(index);
		if (remove) {
			lastRemoved = index;
			flagDirty();
		} else {
			if (!delException) {
				delException = true;
				try {
					throw new RuntimeException("multiple delete " + segmentController.getState() + "; " + segmentController);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return remove;
	}

	public void flagDirty() {
		flagDirty++;
		
		if(!elementManager.isUpdatable()) {
			//flag this super structure to update if we got flagged dirty
			elementManager.flagCheckUpdatable();
		}
	}

	public boolean flagPrepareUpdate(FastCopyLongOpenHashSet toUpdateRawCollection) {
		assert (toUpdateRawCollection != null);

		synchronized (updateLock) {
			flagDirty = updateStatus;
			lastUpdate = System.currentTimeMillis();
			scheduledListToUpdate = toUpdateRawCollection;
			//this method is called by the thread manager
			//wait now, until the list is filled
			try {
				assert (scheduledListToUpdate != null);
				if (!stopped) {
					updateLock.wait(1000);
				} else {
					stopped = false;
				}
				if (scheduledListToUpdate != null) {
					//no update has been taken place!
					scheduledListToUpdate = null;
					flagDirty();
					return false;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return true;

	}

	public void flagUpdateFinished(
			ElementCollectionCalculationThreadExecution<E, EC, EM> nextQueueElement) {
		synchronized (updateLock) {
			finishedThread = nextQueueElement;
		}
	}

	public List<E> getElementCollections() {
		if (elementCollections == null) {
			elementCollections = new ObjectArrayList<E>();
		}
		return elementCollections;
	}

	@SuppressWarnings("unchecked")
	public ManagerContainer<SegmentController> getContainer() {
		return ((ManagedSegmentController<SegmentController>) segmentController).getManagerContainer();
	}

	/**
	 * @return the enhancerClazz
	 */
	public short getEnhancerClazz() {
		return enhancerClazz;
	}

	public abstract int getMargin();

	/**
	 * @return the segmentController
	 */
	public final SegmentController getSegmentController() {
		return segmentController;
	}

	protected abstract Class<E> getType();

	public abstract boolean needsUpdate();

	//	public E newElementCollection(short clazzId,
	//			SegmentController controller) {
	//		return newElementCollection(clazzId, this, controller);
	//	}

	public abstract E getInstance();

	public E newElementCollection(short clazzId, EC col, SegmentController controller) {

		E instance = getInstance();

		instance.initialize(enhancerClazz, col, controller);

		instance.resetAABB();
		return instance;

	}

	protected abstract void onChangedCollection();
	protected void onRemovedCollection(long absPos, EC instance){
		if(instance instanceof PlayerUsableInterface){
			elementManager.getManagerContainer().removePlayerUsable((PlayerUsableInterface)instance);
		}
		if(instance instanceof PowerConsumer){
			elementManager.getManagerContainer().removeConsumer((PowerConsumer)instance);
		}
		for(E e : instance.getElementCollections()){
			if(e instanceof PlayerUsableInterface){
				assert(false);
				elementManager.getManagerContainer().removePlayerUsable((PlayerUsableInterface)e);
			}
			if(e instanceof PowerConsumer){
				elementManager.getManagerContainer().removeConsumer((PowerConsumer)e);
			}
		}
	}
	public StateInterface getState() {
		return segmentController.getState();
	}
	protected void onFinishedCollection() {
		
		elementManager.totalSize -= this.totalSize;
		this.totalSize = 0;
		double lowestIntegrity = Float.POSITIVE_INFINITY;
		
		for (E c : getElementCollections()) {
			if (!c.onChangeFinished()) {
				failedFinishedChanges.add(c);
			}
			this.totalSize += c.size();
			if(isUsingIntegrity()) {
				lowestIntegrity = Math.min(lowestIntegrity, c.getIntegrity());
			}
			
//			System.err.println(getState()+" FINISHED::: "+c+"; "+c.size());
		}
		
		
		if(this.lowestIntegrity != Float.POSITIVE_INFINITY && elementManager.getManagerContainer().getIntegrityUpdateDelay() > 0) {
			//save the actual value for later
			this.lowestIntegrityActual = lowestIntegrity;
		}else {
			//set normal if initial or no delay currently
			this.lowestIntegrity = lowestIntegrity;
			this.lowestIntegrityActual = lowestIntegrity;
		}
		
		if(ServerConfig.DISPLAY_GROUPING_DEBUG_INFORMATION.isOn()){
			System.err.println("######### GROUP INFO "+getState()+" for "+this+": Total size: "+this.totalSize+" on "+ segmentController);
		}
		elementManager.totalSize += this.totalSize;
		elementManager.onElementCollectionsChanged();
		pieceRefresh();
		
		if (!segmentController.isOnServer()) {
			GameClientState s = (GameClientState) getState();
			s.getController().notifyCollectionManagerChanged(this);
		}
	}
	public double getLowestIntegrity() {
		if(this.lowestIntegrityActual != this.lowestIntegrity && elementManager.getManagerContainer().getIntegrityUpdateDelay() <= 0) {
			//lowest integriy was delayed before. update to actual value since the delay expired
			this.lowestIntegrity = this.lowestIntegrityActual;
			//call the onChanged, so the element manager updates it's lowest value. Call frequency of this if case is low so it doesn't cost too much
			elementManager.onElementCollectionsChanged();
		}
		return lowestIntegrity;
	}
	public void setLowestIntegrity(double lowestIntegrity) {
		this.lowestIntegrity = lowestIntegrity;
	}
	public boolean isUsingIntegrity() {
		return true;
	}
	protected void pieceRefresh() {

	}

//	public boolean receiveDistribution(ReceivedDistribution d){
//		boolean found = false;
//		for(E e : getElementCollections()){
//			try{
//				if(e.idPos == d.idPos){
//					((PointDistributionUnit)e).receiveDistChange(d);
//					found = true;
//					assert((e.idPos == d.idPos) == (ElementCollection.getIndex(e.getElementCollectionId().getAbsolutePos(new Vector3i())) == d.idPos));
//					break;
//				}
//			}catch(CannotImmediateRequestOnClientException ex){
//				//could not be retrived
//				//-> return false to make dummy
//			}
//		}
//		if(!found){
//			System.err.println("[ElementCollectionManager] "+getSegmentController().getState()+": "+getSegmentController()+" Target for distributuion not found: "+d.idPos);
//		}
//		return found;
//	}

	public void remove(long index) {
		if (rawCollection == null) {
			rawCollection = new FastCopyLongOpenHashSet(expected);
		}
		int sizeBef = rawCollection.size();
		if (!rawCollection.contains(index)) {
			if (!delException) {
				delException = true;
				try {
					throw new RuntimeException("multiple delete " + segmentController.getState() + "; " + segmentController);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		doRemove(index);

		//		synchronized(modsA){
		//			modsA.enqueue(index);
		//		}
	}

	public void remove(Vector3i absPos) {
		remove(ElementCollection.getIndex(absPos));
	}

	public void stopUpdate() {
		//		System.err.println("####STOPPED "+this);
		synchronized (updateLock) {
			this.stopped = true;
			updateLock.notify();
		}
	}
	public boolean isStructureUpdateNeeded() {
		return updateInProgress != Long.MIN_VALUE || flagDirty != updateStatus;
	}
	@Override
	public String toString() {
		return segmentController + "->" + (enhancerClazz == Element.TYPE_ALL ? "TYPE_ALL" : ElementKeyMap.getInfo(enhancerClazz).getName()) + "(" + debugID + ")";
	}

	public void update(Timer timer) {
	}
	long to = Long.MIN_VALUE;
	private void updateCollections(long time) {

		if (rawCollection == null) {
			rawCollection = new FastCopyLongOpenHashSet(expected);
		}
		if(!segmentController.isFullyLoaded()){
			return;
		}
		if (finishedThread != null) {
			synchronized (updateLock) {
				finishedThread.apply();
				finishedThread = null;
				onChangedCollection();
				onFinishedCollection();
				to = time + 5000;
				updateInProgress = Long.MIN_VALUE;
				
				//notify element manager to check if we need to keep updating the super structure
				elementManager.flagCheckUpdatable();
			}
		}
		if(!segmentController.isOnServer()) {
			((GameClientState) segmentController.getState()).getWorldDrawer()
			.getSegmentDrawer().getElementCollectionDrawer().flagUpdate();
		}
		if(to > 0 && time > to) {
			to = Long.MIN_VALUE;
			//do something delayed
		}
		if (scheduledListToUpdate != null) {
			if(ServerConfig.DISPLAY_GROUPING_DEBUG_INFORMATION.isOn()){
				System.err.println("######### GROUP INFO "+ segmentController.getState()+" SHEDULING GROUP UPDATE FOR "+this+" on "+ segmentController);
			}
			synchronized (updateLock) {
				//check again just in case
				if (scheduledListToUpdate != null) {
					try {
						scheduledListToUpdate.deepApplianceCopy(rawCollection);
					} catch (RuntimeException e) {
						e.printStackTrace();
						assert (false) : (rawCollection != null) + ";\n\n" + (scheduledListToUpdate != null);
						throw e;
					}
					scheduledListToUpdate = null;
					updateLock.notify();
				}
			}
		}
		
		if (flagDirty != updateStatus && isOkToEnqueue(time)) {
			
			synchronized (updateLock) {
				//double check after synchronize
				if (flagDirty != updateStatus && isOkToEnqueue(time)) {
//					if(!segmentController.isOnServer()) {
//						System.err.println("TRING TO ENQ "+this);
//					}
					if(updateInProgress == Long.MIN_VALUE){
						
						updateInProgress = flagDirty;
						elementManager.lastEnqueue = time;
	
						if (segmentController.isOnServer()) {
							GameServerState state = (GameServerState) segmentController.getState();
							state.getController().enqueueElementCollectionUpdate(this);
						} else {
							GameClientState state = (GameClientState) segmentController.getState();
							state.getController().enqueueElementCollectionUpdate(this);
						}
					}else{
						if(flagDirty > updateInProgress){
							if(((GameStateInterface) segmentController.getState()).getGameState().isManCalcCancelOn()){
								cancelUpdateStatus = updateInProgress;
							}
						}
					}
				}
			}
		}
	}
	private boolean isOkToEnqueue(long time){
		
		boolean ok = segmentController.isFullyLoaded() ||
				((time - lastUpdate > 1000 && time - elementManager.lastEnqueue > 500) || (rawCollection.size() < 30 && (time - lastUpdate > 100 && time - elementManager.lastEnqueue > 100)));
		return ok;
	}
	protected void updateStructure(long time) {
		
		if (time - lastUpdateLocal < UPDATE_FREQUENCY_MS && scheduledListToUpdate == null) {
			return;
		}
		if (time - lastFailUpdateLocal > 1000) {
			/*
			 * recheck failed changed
			 * Fail can happen when control structure is provided
			 * by nt or from disk and a custom output unit
			 * cannot check all blocks
			 */
			if (!failedFinishedChanges.isEmpty()) {
				for (int i = 0; i < failedFinishedChanges.size(); i++) {
					if (failedFinishedChanges.get(i).onChangeFinished()) {
						failedFinishedChanges.remove(i);
						i--;
						updateInProgress = Long.MIN_VALUE;
					}
				}
			}
			lastFailUpdateLocal = System.currentTimeMillis();
		}

		lastUpdateLocal = time;

		updateCollections(time);
		
		
	}

	public void createLazyGUI(GameClientState state) {
		GUIElementList list = new GUIElementList(state);
		int totalBlocks = totalSize;

		ModuleValueEntry e = new ModuleValueEntry(Lng.str("Total Blocks"), Lng.str("%s in %s",  totalBlocks,  getElementCollections().size() ) + (getElementCollections().size() > 1 ? " groups" : " group"));
		GUIAnchor blocksOverview = e.get(state);
		list.add(new GUIListElement(blocksOverview, blocksOverview, state));

		GUIKeyValueEntry[] t = getGUICollectionStats();
		for (int i = 0; i < t.length; i++) {
			GUIAnchor guiAnchor = t[i].get(state);
			list.add(new GUIListElement(guiAnchor, guiAnchor, state));
		}

		for (int i = 0; i < getElementCollections().size(); i++) {
			E collection = getElementCollections().get(i);

//			list.add(new GUIListElement(guiAncor, guiAncor, state));
		}

	}

	public ControllerManagerGUI createGUI(GameClientState state) {
		GUIElementList list = new GUIElementList(state);
		int totalBlocks = totalSize;

		ModuleValueEntry e = new ModuleValueEntry(Lng.str("Total Blocks"), Lng.str("%s in %s",  totalBlocks,  getElementCollections().size() ) + (getElementCollections().size() > 1 ? " groups" : " group"));
		GUIAnchor blocksOverview = e.get(state);
		list.add(new GUIListElement(blocksOverview, blocksOverview, state));

		GUIKeyValueEntry[] t = getGUICollectionStats();
		for (int i = 0; i < t.length; i++) {
			GUIAnchor guiAnchor = t[i].get(state);
			list.add(new GUIListElement(guiAnchor, guiAnchor, state));
		}

		for (int i = 0; i < getElementCollections().size(); i++) {
			E collection = getElementCollections().get(i);
			ControllerManagerGUI m = getElementCollections().get(i).createUnitGUI(state, null, null);
			if (m == null) {

				GUITextOverlay err = new GUITextOverlay(state);
				String errString = Lng.str("StructureTab:\nUnable to load GUI-UNIT: \n%s:\n Please Report!",  collection.elementCollectionManager.getModuleName());
				try {
					throw new NullPointerException(errString);
				} catch (NullPointerException e1) {
					e1.printStackTrace();
				}
				if (state.getPlayer().getNetworkObject().isAdminClient.get()) {
					state.getController().popupAlertTextMessage(errString, 0);
				}
				err.setTextSimple(errString);
				GUIListElement guiListElement = new GUIListElement(err, err, state);
				list.add(guiListElement);
			} else {
				list.add(m.getListEntry(state, list));
			}
		}
		ControllerManagerGUI m = new ControllerManagerGUI();
		m.createFromElementCollection(state, this, list);
		assert (m.check()) : m;
		return m;
	}

	public abstract GUIKeyValueEntry[] getGUICollectionStats();

	public abstract String getModuleName();

	/**
	 * @return the totalSize
	 */
	public int getTotalSize() {
		return totalSize;
	}

	public void clearCollectionForApply() {
		for(E e : getElementCollections()){
			e.onClear();
			if(e instanceof PowerConsumer){
				elementManager.getManagerContainer().removeConsumer((PowerConsumer) e);
			}
			if(e instanceof PlayerUsableInterface){
				elementManager.getManagerContainer().removePlayerUsable((PlayerUsableInterface) e);
			}
		}
		getElementCollections().clear();
	}

	/**
	 * @return the elementManager
	 */
	public EM getElementManager() {
		return elementManager;
	}
	public float getSensorValue(SegmentPiece connected){
		return 0;
	}
//	public void startInitialization() {
//		this.initializationStart = System.currentTimeMillis();
//	}
	public void sendClientMessage(String str, byte type) {
		segmentController.sendClientMessage(str, type);
	}
	public float getDamageGivenMultiplier() {
		return segmentController.getDamageGivenMultiplier();
	}
	public ManagerReloadInterface getReloadInterface(){
		if(elementManager instanceof ManagerReloadInterface){
			return (ManagerReloadInterface) elementManager;
		}
		if(this instanceof ManagerReloadInterface){
			return (ManagerReloadInterface)this;
		}
		return null;
	}
	public ManagerActivityInterface getActivityInterface(){
		if(elementManager instanceof ManagerActivityInterface){
			return (ManagerActivityInterface) elementManager;
		}
		if(this instanceof ManagerActivityInterface){
			return (ManagerActivityInterface)this;
		}
		return null;
	}
	public int getSectorId() {
		return segmentController.getSectorId();
	}
	public float getWeaponSpeed() {
		return 0;
	}

	public float getWeaponDistance() {
		return 0;
	}
	/**
	 * FireStates are used by weapons that need more than one update to fire like beams and any charged weapons
	 * @param aiShipControllerStateUnit 
	 * @return null if the weapon can be executed in one update (like a cannon).
	 */
	public AIFireState getAiFireState(ControllerStateInterface aiShipControllerStateUnit) {
	
		return null;
	}
	public boolean isExplosiveStructure() {
		return false;
	}
}
