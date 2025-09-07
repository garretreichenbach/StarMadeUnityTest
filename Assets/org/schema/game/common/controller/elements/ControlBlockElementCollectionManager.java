package org.schema.game.common.controller.elements;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import org.schema.common.util.CompareTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.client.view.gui.weapon.WeaponRowElement;
import org.schema.game.client.view.gui.weapon.WeaponRowElementInterface;
import org.schema.game.client.view.tools.ColorTools;
import org.schema.game.common.controller.HandleControlInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.elements.FocusableUsableModule.FireMode;
import org.schema.game.common.controller.elements.combination.CombinationAddOn;
import org.schema.game.common.controller.elements.effectblock.EffectCollectionManager;
import org.schema.game.common.controller.elements.power.reactor.PowerConsumer;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.*;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.util.FastCopyLongOpenHashSet;
import org.schema.game.network.objects.remote.RemoteValueUpdate;
import org.schema.game.network.objects.valueUpdate.NTValueUpdateInterface;
import org.schema.game.network.objects.valueUpdate.ValueUpdate.ValTypes;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import javax.vecmath.Vector4f;
import java.util.Collections;
import java.util.Comparator;

public abstract class ControlBlockElementCollectionManager<E extends ElementCollection<E, CM, EM>, CM extends ControlBlockElementCollectionManager<E, CM, EM>, EM extends UsableControllableElementManager<E, CM, EM>> extends ElementCollectionManager<E, CM, EM> 
		implements ColorBeamInterface, HandleControlInterface {

	//	/* (non-Javadoc)
	//	 * @see org.schema.game.common.controller.elements.ElementCollectionManager#createGUI()
	//	 */
	//	@Override
	//	public ControllerManagerGUI createGUI(GameClientState state) {
	//		return null;
	//	}
	private static final Vector4f defaultColor = new Vector4f(1, 1, 1, 1);
	private final SegmentPiece controllerElement;
	private Vector3i tmp = new Vector3i();
	private int effectTotal;
	private long slaveConnectedElement = Long.MIN_VALUE;
	private long effectConnectedElement = Long.MIN_VALUE;
	private long lightConnectedElement = Long.MIN_VALUE;
	private boolean checkedForInitialMetaData = false;
	private Vector4f color = new Vector4f();
	private long lastColorElem = Long.MIN_VALUE;
	private long lastSendLimitWarning;
	private long controllerIndex;
	private long controllerIndex4;
	private static int accumulatedAbuses;
	private static long lastAbuseMsg;
	public SimpleTransformableSendableObject<?> getShootingEntity(){
		return getSegmentController();
	}
	private final SegmentPiece tmpP = new SegmentPiece();
	private boolean flagVolleyOrderDirty;
	private int volleyIndex;
	private float lastVolleyTime;
	
	public class ConnectedLogicCon{
		public int connected;
		public int active;
		public void reset() {
			connected = 0;
			active = 0;
		}
	}
	public void onLogicActivate(SegmentPiece selfBlock, boolean oldActive, Timer timer) {
	}
	public ConnectedLogicCon getActiveConnectedLogic(ConnectedLogicCon con) {
		con.reset();
		Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> map = getElementManager().getControlElementMap().getControllingMap().get(controllerIndex);

		if(map != null){
			for(short s : ElementKeyMap.signalArray){
				assert(ElementInformation.canBeControlled(getElementManager().controllerId, s)):"Tried to pull active logic blocks from block that can't be connected to logic: "+this+"; "+ElementKeyMap.toString(getElementManager().controllerId)+" <- "+ElementKeyMap.toString(s);
				FastCopyLongOpenHashSet toMap = map.get(s);
				
				if(toMap != null){
					for(long l : toMap){
						SegmentPiece pointUnsave = getSegmentController().getSegmentBuffer().getPointUnsave(l, tmpP);
						if(pointUnsave != null && pointUnsave.isActive()) {
							con.active++;
						}
						con.connected++;
					}
				}
			}
		}
		return con;
	}
	public ControlBlockElementCollectionManager(SegmentPiece controllerElement, short clazz,
	                                            SegmentController segController, EM em) {
		super(clazz, segController, em);
		this.controllerElement = controllerElement;
		this.controllerIndex = controllerElement.getAbsoluteIndex();
		this.controllerIndex4 = controllerElement.getAbsoluteIndexWithType4();
		pieceRefresh();
		controllerElement.getAbsolutePos(tmp);

	}
	public long getControllerIndex(){
		return controllerIndex;
	}
	public long getControllerIndex4() {
		return controllerIndex4;
	}

	@Override
	public int getMargin() {
		return 0;
	}

	protected void updateInterEffects(InterEffectSet basis, InterEffectSet out) {
		assert(!basis.isZero()):"no basis set";
		
		
		final EffectCollectionManager<?, ?, ?> e = getEffectCollectionManager();
		if(e != null) {
			addInterEffect(e, basis, out);
		}else {
			out.setEffect(basis);
		}
	}
	private void addInterEffect(EffectCollectionManager<?, ?, ?> e, InterEffectSet basis, InterEffectSet out) {
		final boolean capped = true;
		float ratio = CombinationAddOn.getRatio(this, e, capped);
		
		InterEffectSet s = e.getElementManager().getInterEffect();
		assert(!s.isZero()):e+"; "+s;
		out.setEffect(basis);
		out.scaleAdd(s, ratio);
	}

	@Override
	public boolean needsUpdate() {
		return false;
	}
	@Override
	protected void onChangedCollection() {
		flagVolleyOrderDirty = true;
		volleyIndex = 0;
		lastVolleyTime = volleyReload;
	}

	public boolean allowedOnServerLimit() {
		if(getSegmentController().isOnServer() && ((GameServerState)getState()).getGameConfig().hasGroupLimit(getElementManager().controllerId, getElementCollections().size()) ){
			if(System.currentTimeMillis() - lastSendLimitWarning > 5000){
				getSegmentController().sendControllingPlayersServerMessage(Lng.astr("WARNING!\nWeapon/Tool will not have any effect!\nServer doesn't allow more than %s groups\nper computer.", 
						((GameServerState)getState()).getGameConfig().getGroupLimit(getElementManager().controllerId)), ServerMessage.MESSAGE_TYPE_ERROR);	
				lastSendLimitWarning = System.currentTimeMillis();
			}
			return false;
		}
		return true;
	}
	public String getTagId() {
		return null;
	}

	public int getFactionId() {
		return getSegmentController().getFactionId();
	}

	public boolean hasTag() {
		return getElementManager().hasMetaData();
	}

	public void sendHitConfirm(byte damageType) {
		getSegmentController().sendHitConfirm(damageType);
	}

	@Override
	public String getName() {
		return getSegmentController().getName();
	}
	public WeaponRowElementInterface getWeaponRow() {
		WeaponRowElementInterface row = new WeaponRowElement(controllerElement);
		return row;
	}

	public boolean isControllerConnectedTo(long index, short type) {
		Long2ObjectOpenHashMap<FastCopyLongOpenHashSet> all = getSegmentController().getControlElementMap().getControllingMap().getAll();
		if(all == null){
			return false;
		}
		FastCopyLongOpenHashSet cmap = all.get(index);
		return cmap != null && cmap.contains(controllerElement.getAbsoluteIndexWithType4());
	}
	/* (non-Javadoc)
	 * @see org.schema.game.common.data.Damager#getPlayerState()
	 */
	public AbstractOwnerState getOwnerState() {
		return getSegmentController().getOwnerState();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.Damager#isSegmentController()
	 */
	public boolean isSegmentController() {
		return true;
	}

	public StateInterface getState() {
		return getSegmentController().getState();
	}
	public void onEffectChanged() {
//		System.err.println(getState()+"; ON EFFECT CHANGED "+this+"; "+getSegmentController());
		onChangedCollection();
	}
	protected void applyMetaData(BlockMetaDataDummy dummy) {
	}

	public boolean equalsControllerPos(Vector3i controller) {
		return controller != null && controllerElement.equalsPos(controller);
	}

	/**
	 * @return the controllerElement
	 */
	public SegmentPiece getControllerElement() {
		return controllerElement;
	}

	public Vector3i getControllerPos() {
		assert (controllerElement.equalsPos(tmp)) : tmp + ": " + controllerElement.getAbsolutePos(new Vector3i());
		return tmp;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return controllerElement.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return obj instanceof ControlBlockElementCollectionManager<?, ?, ?> && ((ControlBlockElementCollectionManager<?, ?, ?>) obj).controllerElement.equals(controllerElement);
	}

	@Override
	protected void pieceRefresh() {
		controllerElement.refresh();
		controllerElement.getAbsolutePos(tmp);
		//		System.err.println(this+" REFRESHED PIECE: "+tmp+": "+controllerElement.getAbsolutePos(new Vector3i()));
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.ElementCollectionManager#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + "(controllerPos: " + controllerElement + ")";
	}

	@Override
	public void updateStructure(long time) {

		if (getSegmentController().isOnServer() && !checkedForInitialMetaData && getElementManager().hasMetaData()) {
			BlockMetaDataDummy dummy = getContainer().getInitialBlockMetaData().remove(this.controllerIndex);
			if (dummy != null) {
				applyMetaData(dummy);
			}
		}
		
		
		super.updateStructure(time);
		
		if(!checkedForInitialMetaData) {
			getElementManager().flagCheckUpdatable();
		}
		checkedForInitialMetaData = true;
	}
	@Override
	public boolean isStructureUpdateNeeded() {
		return (getSegmentController().isOnServer() && !checkedForInitialMetaData && getElementManager().hasMetaData()) || super.isStructureUpdateNeeded();
	}
	@Override
	public ControllerManagerGUI createGUI(GameClientState state) {
		GUIElementList list = new GUIElementList(state);
		int totalBlocks = 0;
		for (int i = 0; i < getElementCollections().size(); i++) {
			totalBlocks += getElementCollections().get(i).size();
		}

		ModuleValueEntry e = new ModuleValueEntry(
				Lng.str("Total blocks"), Lng.str("%s in %s", totalBlocks, getElementCollections().size()) + (getElementCollections().size() > 1 ? Lng.str(" groups") : Lng.str(" group")));
		GUIAnchor blocksOverview = e.get(state);
		list.add(new GUIListElement(blocksOverview, blocksOverview, state));

		GUIKeyValueEntry[] t = getGUICollectionStats();
		for (int i = 0; i < t.length; i++) {
			GUIAnchor guiAnchor = t[i].get(state);
			list.add(new GUIListElement(guiAnchor, guiAnchor, state));
		}
		ControlBlockElementCollectionManager<?, ?, ?> supportCol = null;
		ControlBlockElementCollectionManager<?, ?, ?> effectCol = null;
		float effectRatio = 0;
		short effectType = 0;
		if (getSlaveConnectedElement() != Long.MIN_VALUE) {
			ManagerModuleCollection<?, ?, ?> managerModuleCollection = ((ManagedSegmentController<?>) getSegmentController())
					.getManagerContainer().getModulesControllerMap().get((short) ElementCollection.getType(getSlaveConnectedElement()));
			ControlBlockElementCollectionManager<?, ?, ?> cb;
			if (managerModuleCollection != null && (cb = managerModuleCollection.getCollectionManagersMap().get(ElementCollection.getPosIndexFrom4(getSlaveConnectedElement()))) != null) {
				float ratio = CombinationAddOn.getRatio(this, cb);
				supportCol = cb;
			}
		}
		if (getEffectConnectedElement() != Long.MIN_VALUE) {
			short eT = (short) ElementCollection.getType(getEffectConnectedElement());
			ManagerModuleCollection<?, ?, ?> managerModuleCollection = ((ManagedSegmentController<?>) getSegmentController())
					.getManagerContainer().getModulesControllerMap().get(eT);
			ControlBlockElementCollectionManager<?, ?, ?> cb;
			if (managerModuleCollection != null && (cb = managerModuleCollection.getCollectionManagersMap().get(ElementCollection.getPosIndexFrom4(getEffectConnectedElement()))) != null) {
				effectRatio = CombinationAddOn.getRatio(this, cb);
				effectType = eT;
				effectCol = cb;
			}
		}

		for (int i = 0; i < getElementCollections().size(); i++) {
			ControllerManagerGUI m = getElementCollections().get(i).createUnitGUI(state, supportCol, effectCol);
			assert (m != null) : "GUI FAILED: " + getElementCollections().get(i).getClass().getSimpleName() + "::: " + this + ": " + getElementCollections().get(i);
			if (m == null) {
				throw new NullPointerException("GUI FAILED: " + getElementCollections().get(i).getClass().getSimpleName() + "::: " + this + ": " + getElementCollections().get(i));
			}
			list.add(m.getListEntry(state, list));
		}
		ControllerManagerGUI m = new ControllerManagerGUI();
		m.createFromElementCollection(state, this, list);
		assert (m.check()) : m;
		return m;
	}

	public void refreshControlled(
			ControlElementMap controlElementMap, short controllerType) {
		/*
		 * Add all elements this controller block controls
		 */
		FastCopyLongOpenHashSet controlled = controlElementMap.getControllingMap().getAll().get(controllerIndex);

		ElementInformation controllerInfo = ElementKeyMap.getInfo(controllerType);

		if (rawCollection == null) {
			if (controlled != null) {
				rawCollection = new FastCopyLongOpenHashSet(controlled.size());
			} else {
				rawCollection = new FastCopyLongOpenHashSet(16);
			}
		}
		final boolean controllsAll = controllerInfo.controlsAll();
		final boolean rail = controllerInfo.getId() == ElementKeyMap.RAIL_RAIL_SPEED_CONTROLLER;
		final boolean checkUnique = getSegmentController().isOnServer() && getElementManager().isCheckForUniqueConnections();
		if(checkUnique){
			if(getElementManager().uniqueConnections == null){
				getElementManager().uniqueConnections = new LongOpenHashSet(128);
			}
			
		}
		if (controlled != null) {

			//use this iterator for native long values
			LongIterator iterator = controlled.iterator();
			while (iterator.hasNext()) {
				long v = iterator.nextLong();
				short type = (short) ElementCollection.getType(v);
				if (type == getEnhancerClazz() || controllsAll || (rail && ElementKeyMap.isValidType(type) && ElementKeyMap.getInfoFast(type).isRailTrack())) {
					if(checkUnique){
						boolean added = getElementManager().uniqueConnections.add(v);
						if(!added){
							accumulatedAbuses++;
							if(getState().getUpdateTime() - lastAbuseMsg > 10000){
								lastAbuseMsg = getState().getUpdateTime();
								Object[] message = Lng.astr("One or more connections has been added twice to the same block,\nbut this type of link is restricted to unique connections\n-> possible abuse by linking one block to multiple controllers for entity:\n%s in %s", getSegmentController().toNiceString(), getSegmentController().getSector(new Vector3i()));
								try {
									throw new Exception(this+" "+String.format("%s Connections have been added twice to the same block, but this type of link is restricted to unique connections\n-> possible abuse by linking one block to multiple controllers: %s in %s", accumulatedAbuses, getSegmentController().toNiceString(), getSegmentController().getSector(new Vector3i())));
								} catch (Exception e) {
									e.printStackTrace();
									((GameServerState)getState()).getController().broadcastMessageAdmin(message, ServerMessage.MESSAGE_TYPE_ERROR);
								}
								System.err.println("[ABUSE][CATCH] REMOVING "+controlled.size()+" BLOCKS FROM ALL CONTROLLERS ("+this.getSegmentController()+")");
								accumulatedAbuses = 0;
							}
							getSegmentController().getControlElementMap().removeControlledFromAll(v, type, true);
						}
					}
					
					rawCollection.add(ElementCollection.getPosIndexFrom4(v));
				} else {
					if (ElementKeyMap.isValidType(type)) {
						ElementInformation info = ElementKeyMap.getInfo(controllerType);
						if (info.isCombiConnectSupport(type)) {
							setSlaveConnectedElement(v);
						} else if (info.isCombiConnectEffect(type)) {
							setEffectConnectedElement(v);
						} else if (info.isLightConnect(type)) {
							setLightConnectedElement(v);
						}
					}
				}
			}
			flagDirty();
		}

	}

	public Tag toTagStructure() {
		return new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.LONG, null, controllerIndex),
				toTagStructurePriv(), 
				FinishTag.INST
				});
	}

	protected Tag toTagStructurePriv() {
		return new Tag(Type.BYTE, null, (byte) 0);
	}
	public long getSlaveConnectedElementRaw() {
		return slaveConnectedElement;
	}
	public long getEffectConnectedElementRaw() {
		return effectConnectedElement;
	}
	public long getLightConnectedElementRaw() {
		return lightConnectedElement;
	}
	/**
	 * @return the extraConnectedElement
	 */
	public long getSlaveConnectedElement() {

		if (slaveConnectedElement != Long.MIN_VALUE) {
			LongOpenHashSet controlled = getSegmentController().getControlElementMap().getControllingMap().getAll().get(ElementCollection.getIndex(getControllerPos()));
			if (controlled == null || !controlled.contains(slaveConnectedElement)) {

				if (getSegmentController() instanceof Ship) {
					synchronized(getState()){
					boolean needsSynch = !getState().isSynched();
					if(needsSynch){
						//called from several drawers
						getState().setSynched();
					}
					System.err.println("[SLAVE][RESET] ADDING SUPPORT BACK TO DEFAULT (CORE): " + ElementKeyMap.toString((short) ElementCollection.getType(slaveConnectedElement)));
					getSegmentController().getControlElementMap().addControllerForElement(ElementCollection.getIndex(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF),
							ElementCollection.getPosIndexFrom4(slaveConnectedElement), (short) ElementCollection.getType(slaveConnectedElement));

					if (getSegmentController().getState() instanceof GameClientState) {
						((GameClientState) getSegmentController().getState()).getWorldDrawer().getGuiDrawer().getPlayerPanel().getWeaponManagerPanel().setReconstructionRequested(true);
					}
					if(needsSynch){
						getState().setUnsynched();
					}
					}
				}
				getElementManager().getManagerContainer().modifySlavesAndEffects(this.slaveConnectedElement, Long.MIN_VALUE);
				slaveConnectedElement = Long.MIN_VALUE;
				getElementManager().getManagerContainer().flagElementChanged();
				flagVolleyOrderDirty = true;
				System.err.println(this + "; " + this.getSegmentController().getState() + " [SLAVE][RESET] controller no longer connected");
			}

		}
		if (slaveConnectedElement != Long.MIN_VALUE) {
			LongOpenHashSet oCore = getSegmentController().getControlElementMap().getControllingMap().getAll()
					.get(ElementCollection.getIndex(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF));
			if (oCore != null && oCore.contains(slaveConnectedElement)) {
				synchronized(getState()){
				boolean needsSynch = !getState().isSynched();
				if(needsSynch){
					getState().setSynched();
				}
				getSegmentController().getControlElementMap().removeControllerForElement(ElementCollection.getIndex(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF),
						ElementCollection.getPosIndexFrom4(slaveConnectedElement), (short) ElementCollection.getType(slaveConnectedElement));
				
				if(needsSynch){
					getState().setUnsynched();
				}
				}
			}
		}
		return slaveConnectedElement;
	}

	/**
	 * @param extraConnectedElement the extraConnectedElement to set
	 */
	public void setSlaveConnectedElement(long extraConnectedElement) {
		getElementManager().getManagerContainer().modifySlavesAndEffects(this.slaveConnectedElement, extraConnectedElement);
		this.slaveConnectedElement = extraConnectedElement;
		getElementManager().getManagerContainer().flagElementChanged();
		flagVolleyOrderDirty = true;
		
	}

	/**
	 * @return the effectConnectedElement
	 */
	public long getEffectConnectedElement() {
		if (effectConnectedElement != Long.MIN_VALUE) {
			LongOpenHashSet controlled = getSegmentController().getControlElementMap().getControllingMap().getAll().get(ElementCollection.getIndex(getControllerPos()));
			if (controlled == null || !controlled.contains(effectConnectedElement)) {
				synchronized(getState()){
					boolean needsSynch = !getState().isSynched();
					if(needsSynch){
						getState().setSynched();
					}
					if (getSegmentController() instanceof Ship) {
						System.err.println("[EFFECT][RESET] ADDING EFFECT BACK TO DEFAULT (CORE): " + ElementKeyMap.toString((short) ElementCollection.getType(effectConnectedElement)));
						
						getSegmentController().getControlElementMap()
						.addControllerForElement(ElementCollection.getIndex(
								SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF),
								ElementCollection.getPosIndexFrom4(effectConnectedElement), 
								(short) ElementCollection.getType(effectConnectedElement));
	
						if (getSegmentController().getState() instanceof GameClientState) {
							((GameClientState) getSegmentController().getState()).getWorldDrawer().getGuiDrawer().getPlayerPanel().getWeaponManagerPanel().setReconstructionRequested(true);
						}
					}
					
					if(needsSynch){
						getState().setUnsynched();
					}
					getElementManager().getManagerContainer().modifySlavesAndEffects(this.effectConnectedElement, Long.MIN_VALUE);
					effectConnectedElement = Long.MIN_VALUE;
					flagVolleyOrderDirty = true;
					this.flagEffectChanged();
					getElementManager().getManagerContainer().flagElementChanged();
					System.err.println(this + "; " + this.getSegmentController().getState() + " [EFFECT][RESET] controller no longer connected");
				}
			}
		}
		if (effectConnectedElement != Long.MIN_VALUE) {
			short connectedType = 0;
			connectedType = (short) ElementCollection.getType(effectConnectedElement);
			ManagerModuleCollection<?, ?, ?> effectModuleCollection = getContainer().getModulesControllerMap().get(connectedType);

			ControlBlockElementCollectionManager<?, ?, ?> effect = CombinationAddOn.getEffect(effectConnectedElement, effectModuleCollection, getSegmentController());
			if (effect != null) {
				effectTotal = effect.getTotalSize();
			}
		} else {
			effectTotal = 0;
		}

		if (effectConnectedElement != Long.MIN_VALUE) {
			synchronized(getState()){
				boolean needsSynch = !getState().isSynched();
				if(needsSynch){
					getState().setSynched();
				}
				LongOpenHashSet oCore = getSegmentController().getControlElementMap().getControllingMap().getAll()
						.get(ElementCollection.getIndex(Ship.core));
				if (oCore != null && oCore.contains(effectConnectedElement)) {
					getSegmentController().getControlElementMap().removeControllerForElement(
							ElementCollection.getIndex(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF),
							ElementCollection.getPosIndexFrom4(effectConnectedElement), (short) ElementCollection.getType(effectConnectedElement));
				}
				if(needsSynch){
					getState().setUnsynched();
				}
			}
		}
		return effectConnectedElement;
	}

	/**
	 * @param effectConnectedElement the effectConnectedElement to set
	 */
	public void setEffectConnectedElement(long effectConnectedElement) {
		getElementManager().getManagerContainer().modifySlavesAndEffects(this.effectConnectedElement, effectConnectedElement);
		this.effectConnectedElement = effectConnectedElement;
		getElementManager().getManagerContainer().flagElementChanged();
		flagVolleyOrderDirty = true;
		this.flagEffectChanged();
		
	}

	public void flagEffectChanged() {
		if(this instanceof EffectChangeHanlder) {
			getElementManager().getManagerContainer().queueOnEffectChange((EffectChangeHanlder)this);
		}
	}

	/**
	 * @return the lightConnectedElement
	 */
	public long getLightConnectedElement() {
		if (lightConnectedElement != Long.MIN_VALUE) {
			LongOpenHashSet controlled = getSegmentController().getControlElementMap().getControllingMap().getAll().get(ElementCollection.getIndex(getControllerPos()));
			if (controlled == null || !controlled.contains(lightConnectedElement)) {

				if (getSegmentController() instanceof Ship) {

					if (getSegmentController().getState() instanceof GameClientState) {
						((GameClientState) getSegmentController().getState()).getWorldDrawer().getGuiDrawer().getPlayerPanel().getWeaponManagerPanel().setReconstructionRequested(true);
					}
				}

				lightConnectedElement = Long.MIN_VALUE;
				getElementManager().getManagerContainer().flagElementChanged();
				System.err.println(this.getSegmentController().getState() + " [LIGHT][RESET] controller no longer connected " + this);
			}
		}

		if (lightConnectedElement != Long.MIN_VALUE) {
			synchronized(getState()){
			boolean needsSynch = !getState().isSynched();
			if(needsSynch){
				getState().setSynched();
			}
			LongOpenHashSet oCore = getSegmentController().getControlElementMap().getControllingMap().getAll()
					.get(ElementCollection.getIndex(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF));
			if (oCore != null && oCore.contains(lightConnectedElement)) {
				getSegmentController().getControlElementMap().removeControllerForElement(ElementCollection
						.getIndex(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF),
						ElementCollection.getPosIndexFrom4(lightConnectedElement), (short) ElementCollection.getType(lightConnectedElement));
			}
			if(needsSynch){
				getState().setUnsynched();
			}
			}
		}
		return lightConnectedElement;
	}

	/**
	 * @param lightConnectedElement the lightConnectedElement to set
	 */
	public void setLightConnectedElement(long lightConnectedElement) {
		this.lightConnectedElement = lightConnectedElement;
		getElementManager().getManagerContainer().flagElementChanged();
	}

	@Override
	public Vector4f getColor() {
		long l = getLightConnectedElement();
		if (l != Long.MIN_VALUE) {
			if (l == lastColorElem) {
				return color; //cached
			} else {
				short type = (short) ElementCollection.getType(l);
				ElementInformation info;
				if (ElementKeyMap.isValidType(type) && (info = ElementKeyMap.getInfo(type)).isLightSource()) {
					color.set(info.getLightSourceColor());
					ColorTools.brighten(color);
					lastColorElem = l;
					return color;
				}
			}
		}
		return getDefaultColor();
	}

	@Override
	public Vector4f getDefaultColor() {
		return defaultColor;
	}

	@Override
	public boolean hasCustomColor() {
		return getLightConnectedElement() != Long.MIN_VALUE;
	}

	/**
	 * @return the effectTotal
	 */
	public int getEffectTotal() {
		return effectTotal;
	}

	/**
	 * @param effectTotal the effectTotal to set
	 */
	public void setEffectTotal(int effectTotal) {
		this.effectTotal = effectTotal;
	}

	public ControlBlockElementCollectionManager<?, ?, ?> getSupportCollectionManager() {
		final ControlBlockElementCollectionManager<?, ?, ?> supportCol;
		if (getSlaveConnectedElement() != Long.MIN_VALUE) {

			ManagerModuleCollection<?, ?, ?> managerModuleCollection = ((ManagedSegmentController<?>) getSegmentController()).getManagerContainer().getModulesControllerMap().get((short) ElementCollection.getType(getSlaveConnectedElement()));
			ControlBlockElementCollectionManager<?, ?, ?> cb;
			if (managerModuleCollection != null && (cb = managerModuleCollection.getCollectionManagersMap().get(ElementCollection.getPosIndexFrom4(getSlaveConnectedElement()))) != null) {
				float ratio = CombinationAddOn.getRatio(this, cb);
//				b.append("support: \n  "+cb.getModuleName()+" ("+ratio*100f+"%)\n");
				supportCol = cb;
			} else {
				supportCol = null;
			}
		} else {
			supportCol = null;
		}
		return supportCol;
	}

	public EffectCollectionManager<?, ?, ?> getEffectCollectionManager() {
		final EffectCollectionManager<?, ?, ?> effectCol;
		if (getEffectConnectedElement() != Long.MIN_VALUE) {
			ManagerModuleCollection<?, ?, ?> managerModuleCollection = ((ManagedSegmentController<?>) getSegmentController()).getManagerContainer().getModulesControllerMap().get((short) ElementCollection.getType(getEffectConnectedElement()));
			ControlBlockElementCollectionManager<?, ?, ?> cb;
			if (managerModuleCollection != null && (cb = managerModuleCollection.getCollectionManagersMap().get(ElementCollection.getPosIndexFrom4(getEffectConnectedElement()))) != null) {
//				float ratio = CombinationAddOn.getRatio(this, cb, false);
//				b.append("effect: \n  "+cb.getModuleName()+" ("+ratio*100f+"%)\n");
				effectCol = (EffectCollectionManager<?, ?, ?>) cb;
			} else {
				effectCol = null;
			}
		} else {
			effectCol = null;
		}
		return effectCol;
	}
	public boolean isPlayerUsable() {
		return true;
	}
	
	public long getUsableId(){
		return controllerIndex;
	}
	public void handleKeyPress(ControllerStateInterface unit, Timer timer){
		
		if(allowedOnServerLimit()){
			if(unit.isDown(KeyboardMappings.SHIP_PRIMARY_FIRE) && unit.isFlightControllerActive() && getElementManager().canHandle(unit)){
				handleControlShot(unit, timer);
			}else {
				
				onNotShootingButtonDown(unit, timer);
			}
		}else{
			if (getSegmentController().isClientOwnObject()) {
				((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("WARNING!\nServer doesn't allow the amount of blocks in this module!"), 0);
			}
		}
	}
	
	protected void onNotShootingButtonDown(ControllerStateInterface unit, Timer timer) {
		volleyIndex = 0;
		lastVolleyTime = volleyReload;
	}
	
	private class VolleyComp implements Comparator<E>{
		@Override
		public int compare(E o1, E o2) {
			return CompareTools.compare(o1.getSignificator(), o2.getSignificator());
		}
	}
	
	private final VolleyComp volleyComp = new VolleyComp();
	private float volleyReload; 
	public void handleControlShot(ControllerStateInterface unit, Timer timer){
		if(canUseCollection(unit, timer)) {
			if(isVolleyShot()) {
				if(flagVolleyOrderDirty) {
					//sort when collections have changed to always have the same order on client/server
					Collections.sort(getElementCollections(), volleyComp); 
					
					flagVolleyOrderDirty = false;
					this.volleyIndex = 0;
					this.volleyReload = 0f;
					// no need to do it with any more units, since all have the same reload
					if (getElementCollections().size() > 0) {
						E e = getElementCollections().get(0);
						float rl = (float) getElementManager().calculateReload(e);
						this.volleyReload = ( rl / (float)getElementCollections().size())/1000f;
					}
					
				}
				lastVolleyTime += timer.getDelta();
				int volleys = (int)(lastVolleyTime / Math.max(0.05f, volleyReload));
				lastVolleyTime -= (float)volleys * volleyReload;
				
				int sz = Math.min(volleys, getElementCollections().size());
				for(int i = 0; i < sz; i++) {
					volleyIndex = volleyIndex % getElementCollections().size();
					E e = getElementCollections().get(volleyIndex);
					
					volleyIndex++;
					if(e instanceof FiringUnit<?, ?, ?> && ((FiringUnit<?, ?, ?>)e).isReloading(timer.currentTime)) {
						break;
					}
					e.fire(unit, timer);
				}
				
			}else {
				for (int u = 0; u < getElementCollections().size(); u++) {
					E e = getElementCollections().get(u);
					e.fire(unit, timer);
				}
			}
		}
		if (getElementCollections().isEmpty() && getSegmentController().isClientOwnObject()) {
			((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("WARNING!\n \nNo Weapons connected \nto entry point"), 0);
		}
	}
	public boolean isVolleyShot() {
		return false;
	}
	public float getVolleyShotTimeSec() {
		return this.volleyReload;
	}
	public boolean canUseCollection(ControllerStateInterface unit, Timer timer) {
		return true;
	}

	@Override
	public ManagerReloadInterface getReloadInterface(){
		if(getElementManager() instanceof ManagerReloadInterface){
			return (ManagerReloadInterface)getElementManager();
		}
		if(this instanceof ManagerReloadInterface){
			return (ManagerReloadInterface)this;
		}
		return null;
	}
	@Override
	public ManagerActivityInterface getActivityInterface(){
		if(getElementManager() instanceof ManagerActivityInterface){
			return (ManagerActivityInterface)getElementManager();
		}
		if(this instanceof ManagerActivityInterface){
			return (ManagerActivityInterface)this;
		}
		return null;
	}
	public boolean hasCoreConnection() {
		FastCopyLongOpenHashSet coreCon = getSegmentController().getControlElementMap().getControllingMap().getAll().get(ElementCollection.getIndex(Ship.core));
		
		return coreCon != null && coreCon.contains(controllerElement.getAbsoluteIndexWithType4());
	}
	public void handleMouseEvent(ControllerStateUnit unit, MouseEvent e) {
		
	}
	public void handleKeyEvent(ControllerStateUnit unit, KeyboardMappings mapping, Timer timer) {
		if(isOnServer()) {
			if(this instanceof FocusableUsableModule && mapping == KeyboardMappings.SWITCH_FIRE_MODE) {
				FocusableUsableModule f = ((FocusableUsableModule)this);
				
				do {
					f.setFireMode(FireMode.values()[(f.getFireMode().ordinal()+1)%FireMode.values().length]);
				}while(!isAllowedFireMode(f.getFireMode()));
				
				
				f.sendFireMode();
			}
		}
	}
	protected boolean isAllowedFireMode(FireMode fireMode) {
		if(!isAllowedVolley() && fireMode == FireMode.VOLLEY) {
			return false;
		}
		return true;
	}
	public boolean isAllowedVolley() {
		return false;
	}
	public void sendFireMode() {
		assert(this instanceof FocusableUsableModule);
		FireModeValueUpdate v = new FireModeValueUpdate();
		v.setServer(
		((ManagedSegmentController<?>) getSegmentController()).getManagerContainer(), controllerIndex);
		assert(v.getType() == ValTypes.FIRE_MODE);
		((NTValueUpdateInterface) getSegmentController().getNetworkObject())
		.getValueUpdateBuffer().add(new RemoteValueUpdate(v, getSegmentController().isOnServer()));
	}
	
	public GUIKeyValueEntry[] getGUICollectionStats() {
		double powerResting = 0;
		double powerCharging = 0;
		double dps = 0;
		for(E e : getElementCollections()) {
			if(e instanceof PowerConsumer) {
				powerResting += ((PowerConsumer) e).getPowerConsumedPerSecondResting();
				powerCharging += ((PowerConsumer) e).getPowerConsumedPerSecondCharging();
			}
			if(e instanceof FiringUnit<?, ?, ?>) {
				float g = 1000f / ((FiringUnit<?, ?, ?>)e).getReloadTimeMs();
				dps += g * ((FiringUnit<?, ?, ?>)e).getDamage();
			}
		}
		return new GUIKeyValueEntry[]{
				new ModuleValueEntry(Lng.str("DPS"), dps),
				new ModuleValueEntry(Lng.str("Resting Power Usage"), powerResting),
				new ModuleValueEntry(Lng.str("Charging Power Usage"), powerCharging),
				};
	}
}
