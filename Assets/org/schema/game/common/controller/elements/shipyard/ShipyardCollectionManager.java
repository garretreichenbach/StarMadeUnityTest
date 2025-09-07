package org.schema.game.common.controller.elements.shipyard;

import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.bytes.Byte2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import org.reflections.Reflections;
import org.schema.common.FastMath;
import org.schema.common.LogUtil;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.PlayerButtonTilesInput;
import org.schema.game.client.controller.PlayerGameDropDownInput;
import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.PlayerTextInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.view.effects.RaisingIndication;
import org.schema.game.client.view.gui.shiphud.HudIndicatorOverlay;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.ElementCountMap;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.database.DatabaseEntry;
import org.schema.game.common.controller.elements.BlockMetaDataDummy;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.power.PowerAddOn;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;
import org.schema.game.common.controller.elements.power.reactor.PowerConsumer;
import org.schema.game.common.controller.elements.shipyard.orders.ShipyardCommandData;
import org.schema.game.common.controller.elements.shipyard.orders.ShipyardEntityState;
import org.schema.game.common.controller.elements.shipyard.orders.ShipyardMachine;
import org.schema.game.common.controller.elements.shipyard.orders.ShipyardProgram;
import org.schema.game.common.controller.elements.shipyard.orders.states.*;
import org.schema.game.common.controller.observer.DrawerObserver;
import org.schema.game.common.controller.rails.RailRelation;
import org.schema.game.common.controller.rails.RailRequest;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.MetaObjectState;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.VoidUniqueSegmentPiece;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.BlueprintMetaItem;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.element.meta.VirtualBlueprintMetaItem;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.game.common.data.player.BlueprintPlayerHandleRequest;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.world.*;
import org.schema.game.common.util.FastCopyLongOpenHashSet;
import org.schema.game.network.commands.gamerequests.EntityRequest;
import org.schema.game.network.objects.remote.RemoteValueUpdate;
import org.schema.game.network.objects.remote.ShipyardCommand;
import org.schema.game.network.objects.valueUpdate.*;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.blueprintnw.BlueprintClassification;
import org.schema.game.server.data.blueprintnw.BlueprintType;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.common.InputChecker;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.language.Translatable;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.input.InputState;
import org.schema.schine.network.client.ClientState;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.sound.controller.AudioController;

import javax.vecmath.Vector3f;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

public class ShipyardCollectionManager extends ControlBlockElementCollectionManager<ShipyardUnit, ShipyardCollectionManager, ShipyardElementManager> implements Comparator<ShipyardUnit>, PowerConsumer, Serializable {

	private static final InputChecker shipInputChecker = (entry, callback) -> {
		if(EntityRequest.isShipNameValid(entry)) {
			return true;
		} else {
			callback.onFailedTextCheck(Lng.str("Must only contain letters or numbers or (_-)!"));
			return false;
		}
	};
	private static final Object2ByteOpenHashMap<Class<? extends ShipyardState>> stateClass2Ids;
	private static final Byte2ObjectOpenHashMap<Class<? extends ShipyardState>> ids2StateClass;
	private static final Byte2ObjectOpenHashMap<String> ids2StateDescription;
	private static final Byte2BooleanOpenHashMap idsCanCancel;
	private static final Byte2BooleanOpenHashMap idsHasBlockGoal;
	private static final Byte2BooleanOpenHashMap idsCanEdit;
	private static final Byte2BooleanOpenHashMap idsCanUndock;
	public static boolean DEBUG_MODE = EngineSettings.SECRET.getString().contains("shipyard");

	static {
		stateClass2Ids = new Object2ByteOpenHashMap<>();
		ids2StateClass = new Byte2ObjectOpenHashMap<>();
		idsCanCancel = new Byte2BooleanOpenHashMap();
		idsHasBlockGoal = new Byte2BooleanOpenHashMap();
		idsCanEdit = new Byte2BooleanOpenHashMap();
		idsCanUndock = new Byte2BooleanOpenHashMap();
		ids2StateDescription = new Byte2ObjectOpenHashMap<>();
		stateClass2Ids.defaultReturnValue((byte) -1);
		readBuildHelperClasses();
	}

	public final byte VERSION = 1;
	protected final ConcurrentLinkedQueue<ShipyardCommandData> commandQueue = new ConcurrentLinkedQueue<>();
	private final ShipyardEntityState serverEntityState;
	private final ShortArrayList distances = new ShortArrayList();
	private final IntOpenHashSet requestedMeta = new IntOpenHashSet();
	public byte currentClientState = -1;
	public ElementCountMap clientGoalFrom = new ElementCountMap();
	public ElementCountMap clientGoalTo = new ElementCountMap();
	public DrawerObserver drawObserver;
	Vector3f minbb = new Vector3f();
	Vector3f maxbb = new Vector3f();
	Vector3f minbbOut = new Vector3f();
	Vector3f maxbbOut = new Vector3f();
	private long lastPopup;
	private boolean unitsValid;
	private int biggest = -1;
	private long lastFSMUpdate;
	private double completionOrderPercent;
	private byte lastState = -1;
	private int lastIntPercent;
	private int currentDesign = -1;
	private String waitForExistsUntilFSMUpdate;
	private long lastStateRequest;
	private boolean wasValid;

	private int maxX;

	private int maxY;

	private int maxZ;

	private int minX;

	private int minY;

	private int minZ;

	private boolean dockingValid;

	private long lastDockValidTest;

	private boolean isPowered = true;

	private float powered;

	private ShipyardState lastServerState;

	private boolean wasPowered;

	private boolean wasValidLogCheck = true;

	private boolean wasDockingValid = true;
	private ShipyardCommandData command;
	private boolean instantBuild;

	public ShipyardCollectionManager(SegmentPiece element, SegmentController segController, ShipyardElementManager em) {
		super(element, ElementKeyMap.SHIPYARD_MODULE, segController, em);
		if(segController.isOnServer()) {
			serverEntityState = new ShipyardEntityState("SY", this, segController.getState());
			serverEntityState.setCurrentProgram(new ShipyardProgram(serverEntityState, false));
		} else {
			serverEntityState = null;
		}
	}

	private static void addClass(Class<? extends ShipyardState> c, byte id) {
		assert (!ids2StateClass.containsKey(id));
		assert (!stateClass2Ids.containsKey(c));
		stateClass2Ids.put(c, id);
		ids2StateClass.put(id, c);
	}

	private static void readBuildHelperClasses() {
		try {
			/*
			 * the reason why thses are added manually and not
			 * by reflection is that the state id gets saved
			 * to disk. To load it again the classes need a
			 * unique id that doesn't change when states are added
			 * or state names are changed.
			 *
			 * However it can be assurred by reflection that each id is
			 * unique and valid.
			 */
			addClass(BlueprintSpawned.class, (byte) 0);
			addClass(CollectingBlocksFromInventory.class, (byte) 1);
			addClass(ConvertingRealToVirtual.class, (byte) 2);
			addClass(ConvertingVirtualToReal.class, (byte) 3);
			addClass(CreatingDesign.class, (byte) 4);
			addClass(Deconstructing.class, (byte) 5);
			addClass(DesignLoaded.class, (byte) 6);
			addClass(NormalShipLoaded.class, (byte) 7);
			addClass(PuttingBlocksInInventory.class, (byte) 8);
			addClass(RemovingDesign.class, (byte) 9);
			addClass(RemovingPhysical.class, (byte) 10);
			addClass(RevertPullingBlocksFromInventory.class, (byte) 11);
			addClass(RevertPuttingBlocksInInventory.class, (byte) 12);
			addClass(SpawningBlueprint.class, (byte) 13);
			addClass(Constructing.class, (byte) 14);
			addClass(UnloadingDesign.class, (byte) 15);
			addClass(WaitingForShipyardOrder.class, (byte) 16);
			addClass(LoadingDesign.class, (byte) 17);
			addClass(CreateBlueprintFromDesign.class, (byte) 18);
			addClass(CreateDesignFromBlueprint.class, (byte) 19);
			addClass(MovingToTestSite.class, (byte) 20);
			Reflections reflections = new Reflections("org.schema.game.common.controller.elements.shipyard.orders.state");
			Set<Class<? extends ShipyardState>> classes = reflections.getSubTypesOf(ShipyardState.class);
			for(Class<?> c : classes) {
				assert (c != null) : c;
				assert (stateClass2Ids != null);
				assert (ids2StateClass != null);
				assert (stateClass2Ids.getByte(c) != -1) : "missing id for class: " + c.getName();
				assert (ids2StateClass.get(stateClass2Ids.getByte(c)) == c) : "invalid id for class: " + c.getName();
				Constructor<ShipyardState> constructor = (Constructor<ShipyardState>) c.getConstructor(ShipyardEntityState.class);
				ShipyardState newInstance = constructor.newInstance((ShipyardEntityState) null);
				idsCanCancel.put(stateClass2Ids.getByte(c), newInstance.canCancel());
				idsHasBlockGoal.put(stateClass2Ids.getByte(c), newInstance.hasBlockGoal());
				idsCanUndock.put(stateClass2Ids.getByte(c), newInstance.canUndock());
				idsCanEdit.put(stateClass2Ids.getByte(c), newInstance.canEdit());
				ids2StateDescription.put(stateClass2Ids.getByte(c), newInstance.getClientShortDescription());
			}
		} catch(Exception e) {
			e.printStackTrace();
			assert (false);
		}
	}

	@Override
	public float getSensorValue(SegmentPiece connected) {
		if(getCurrentDocked() != null && !getCurrentDocked().isVirtualBlueprint()) {
			return (float) getCurrentDocked().getHpController().getHpPercent();
		}
		return (float) completionOrderPercent;
	}

	public SegmentPiece getCore() {
		return getSegmentController().getSegmentBuffer().getPointUnsave(getConnectedCorePositionBlock4());
	}

	public boolean isCurrentStateCancelClient() {
		return idsCanCancel.containsKey(currentClientState) && idsCanCancel.get(currentClientState);
	}

	public boolean isCurrentStateWithGoalListClient() {
		return idsHasBlockGoal.containsKey(currentClientState) && idsHasBlockGoal.get(currentClientState);
	}

	public boolean isCurrentStateUndockable() {
		return idsCanUndock.containsKey(currentClientState) && idsCanUndock.get(currentClientState);
	}

	public boolean isPublicException(int forFactionId) {
		return SegmentController.isBlockPublicException(getSegmentController(), getControllerPos(), forFactionId);
	}

	public List<GUIElement> getCatalog() {
		ObjectArrayList<GUIElement> l = new ObjectArrayList<GUIElement>();
		for(CatalogPermission cat : ((GameClientState) getState()).getPlayer().getCatalog().getAvailableCatalog()) {
			if(cat.type == BlueprintType.SHIP) {
				GUIAnchor a = new GUIAnchor((GameClientState) getState(), 300, 24);
				GUITextOverlay t = new GUITextOverlay((GameClientState) getState());
				t.setTextSimple(cat.getUid());
				t.setPos(5, 4, 0);
				a.attach(t);
				a.setUserPointer(cat.getUid());
				l.add(a);
			}
		}
		Collections.sort(l, (o1, o2) -> {
			GUITextOverlay a1 = (GUITextOverlay) o1.getChilds().get(0);
			GUITextOverlay a2 = (GUITextOverlay) o2.getChilds().get(0);
			return a1.getText().get(0).toString().toLowerCase(Locale.ENGLISH).compareTo(a2.getText().get(0).toString().toLowerCase(Locale.ENGLISH));
		});
		return l;
	}

	public VirtualBlueprintMetaItem getCurrentDesignObject() {
		if(currentDesign >= 0) {
			if(getSegmentController().isOnServer()) {
				MetaObject object = ((MetaObjectState) getSegmentController().getState()).getMetaObjectManager().getObject(currentDesign);
				if(object != null && object instanceof VirtualBlueprintMetaItem) {
					return (VirtualBlueprintMetaItem) object;
				}
			} else {
				MetaObject object = ((MetaObjectState) getSegmentController().getState()).getMetaObjectManager().getObject(currentDesign);
				if(object == null) {
					if(requestedMeta.contains(currentDesign)) {
						((MetaObjectState) getSegmentController().getState()).getMetaObjectManager().checkAvailable(currentDesign, ((MetaObjectState) getSegmentController().getState()));
						requestedMeta.add(currentDesign);
					}
				} else if(object instanceof VirtualBlueprintMetaItem) {
					return (VirtualBlueprintMetaItem) object;
				}
			}
		}
		return null;
	}

	public boolean isCommandUsable(ShipyardCommandType r) {
		boolean a = (DesignLoaded.class.equals(ids2StateClass.get(currentClientState)) && r == ShipyardCommandType.UNLOAD_DESIGN);
		return (getCurrentDocked() == null || dockingValid || a) && r.requiredState.equals(ids2StateClass.get(currentClientState));
	}

	@Override
	protected Tag toTagStructurePriv() {
		if(getSegmentController().isOnServer()) {
			Tag version = new Tag(Tag.Type.BYTE, null, VERSION);
			Tag stateTag = new Tag(Tag.Type.BYTE, null, getStateByteOnServer());
			System.err.println("[SERVER][SHIPYARD] " + getSegmentController() + "; writing current design: " + currentDesign);
			Tag designTag = new Tag(Tag.Type.INT, null, currentDesign);
			Tag entityStateTag = serverEntityState.toTagStructure();
			ShipyardState currentStateServer = getCurrentStateServer();
			if(currentStateServer != null) {
				System.err.println("[SERVER][SHIPYARD] saved ongoning state " + getSegmentController() + ": " + currentStateServer + ": Start " + currentStateServer.getStartTime() + "; ticks done " + currentStateServer.getTicksDone());
			}
			Tag startTime = new Tag(Tag.Type.LONG, null, currentStateServer != null ? currentStateServer.getStartTime() : -1L);
			Tag ticksDone = new Tag(Tag.Type.INT, null, currentStateServer != null ? currentStateServer.getTicksDone() : -1);
			return new Tag(Tag.Type.STRUCT, null, new Tag[]{version, stateTag, designTag, entityStateTag, startTime, ticksDone, FinishTag.INST});
		} else {
			return new Tag(Tag.Type.STRUCT, null, new Tag[]{new Tag(Tag.Type.BYTE, null, (byte) -2), FinishTag.INST});
		}
	}

	private void fromTagStructure(Tag tag) {
		// is executed synched
		Tag[] v = (Tag[]) tag.getValue();
		byte version = (Byte) v[0].getValue();
		// < 0 is client and doesn't need any loading as it's from blueprint
		if(version >= 0) {
			byte cState = (Byte) v[1].getValue();
			serverEntityState.fromTagStructure(v[3]);
			ShipyardState byId = (ShipyardState) ((ShipyardMachine) serverEntityState.getCurrentProgram().getMachine()).getById(ids2StateClass.get(cState), serverEntityState.isInRepair);
			if(byId != null) {
				byId.setLoadedFromTag(true);
				serverEntityState.getCurrentProgram().getMachine().setState(byId);
				if(version > 0) {
					long startTime = (Long) v[4].getValue();
					int ticksDone = (Integer) v[5].getValue();
					byId.loadedStartTime = startTime;
					byId.loadedTicksDone = ticksDone;
				}
			}
			currentDesign = (Integer) v[2].getValue();
			MetaObject object = ((MetaObjectState) getSegmentController().getState()).getMetaObjectManager().getObject(currentDesign);
			System.err.println("[SERVER][SHIPYARD] from tag loaded design: " + currentDesign + ": " + object);
			LogUtil.sy().fine(getSegmentController() + "; " + this + " from tag loaded design: " + currentDesign + ": " + object);
			if(object != null && object instanceof VirtualBlueprintMetaItem) {
				SegmentController loadDesign = loadDesign((VirtualBlueprintMetaItem) object);
				if(loadDesign != null) {
					waitForExistsUntilFSMUpdate = ((VirtualBlueprintMetaItem) object).UID;
					LogUtil.sy().fine(getSegmentController() + "; " + this + "  wait for exists UID: " + ((VirtualBlueprintMetaItem) object).UID);
				}
			}
		}
	}

	@Override
	protected void applyMetaData(BlockMetaDataDummy dummy) {
		fromTagStructure(((ShipyardMetaDataDummy) dummy).tag);
	}

	@Override
	public CollectionShape requiredNeigborsPerBlock() {
		return CollectionShape.RIP;
	}

	@Override
	public int getMargin() {
		return 0;
	}

	@Override
	protected Class<ShipyardUnit> getType() {
		return ShipyardUnit.class;
	}

	@Override
	public boolean needsUpdate() {
		return true;
	}

	@Override
	public ShipyardUnit getInstance() {
		return new ShipyardUnit();
	}

	public boolean alwaysValid() {
		return DEBUG_MODE || ((GameStateInterface) getState()).getGameState().isShipyardIgnoreStructure();
	}

	@Override
	protected void onChangedCollection() {
		if(!getSegmentController().isOnServer()) {
			((GameClientState) getSegmentController().getState()).getWorldDrawer().getGuiDrawer().managerChanged(this);
		}
		if(alwaysValid()) {
			unitsValid = true;
		} else {
			unitsValid = false;
			if(getElementCollections().size() > 0) {
				unitsValid = true;
				for(ShipyardUnit e : getElementCollections()) {
					if(!e.isValid()) {
						/*
						 * if a unit is valid that means that it is already assured
						 * that it is extending in only two dimensions (more than
						 * one so a simple line is excluded) and that the endpoints
						 * are on the same plane
						 */
						if(e.getInvalidReason() == null) {
							e.setInvalidReason(Lng.str("Unit group invalid for unknown reason"));
						}
						LogUtil.sy().fine(getSegmentController() + "; " + this + " One or more shipyard unit invalid: " + e);
						unitsValid = false;
						break;
					}
				}
			}
			if(unitsValid) {
				int xDims = 0;
				int yDims = 0;
				int zDims = 0;
				maxX = Integer.MIN_VALUE;
				maxY = Integer.MIN_VALUE;
				maxZ = Integer.MIN_VALUE;
				minX = Integer.MAX_VALUE;
				minY = Integer.MAX_VALUE;
				minZ = Integer.MAX_VALUE;
				// check that all C grous are in line on the same dimensions
				for(ShipyardUnit e : getElementCollections()) {
					e.getMax(e.max);
					e.getMin(e.min);
					maxX = Math.max(maxX, e.max.x);
					maxY = Math.max(maxY, e.max.y);
					maxZ = Math.max(maxZ, e.max.z);
					minX = Math.min(minX, e.min.x);
					minY = Math.min(minY, e.min.y);
					minZ = Math.min(minZ, e.min.z);
					if(e.xDim) {
						xDims++;
					} else if(e.yDim) {
						yDims++;
					} else {
						zDims++;
						assert (e.zDim);
					}
				}
				if(xDims > yDims) {
					if(xDims > zDims) {
						// x biggest
						biggest = 0;
					} else {
						// z biggest
						biggest = 2;
					}
				} else {
					if(yDims > zDims) {
						// y biggest
						biggest = 1;
					} else {
						// z biggest
						biggest = 2;
					}
				}
				distances.clear();
				distances.ensureCapacity(getElementCollections().size());
				for(ShipyardUnit e : getElementCollections()) {
					if(biggest == 0) {
						if(!e.xDim || e.yDim || e.zDim) {
							e.valid = false;
							e.setInvalidReason(Lng.str("Unit group must have same orientation as the others unit groups"));
							unitsValid = false;
						} else {
							e.normalPos = e.min.x;
						}
						if(e.valid && (e.max.y != maxY || e.max.z != maxZ || e.min.y != minY || e.min.z != minZ)) {
							e.valid = false;
							e.setInvalidReason(Lng.str("Unit group must have same size as the biggest:") + " [" + e.min + ", " + e.max + "] / [(" + minX + ", " + minY + ", " + minZ + "), (" + maxX + ", " + maxY + ", " + maxZ + ")]");
							unitsValid = false;
						}
					} else if(biggest == 1) {
						if(e.xDim || !e.yDim || e.zDim) {
							e.valid = false;
							e.setInvalidReason(Lng.str("Unit group must have same orientation as the others unit groups"));
							unitsValid = false;
						} else {
							e.normalPos = e.min.y;
						}
						if(e.valid && (e.max.x != maxX || e.max.z != maxZ || e.min.x != minX || e.min.z != minZ)) {
							e.valid = false;
							e.setInvalidReason(Lng.str("Unit group must have same size as the biggest:") + "[" + e.min + ", " + e.max + "] / [(" + minX + ", " + minY + ", " + minZ + "), (" + maxX + ", " + maxY + ", " + maxZ + ")]");
							unitsValid = false;
						}
					} else {
						assert (biggest == 2);
						if(e.xDim || e.yDim || !e.zDim) {
							e.valid = false;
							e.setInvalidReason(Lng.str("Unit group must have same orientation as the others unit groups"));
							unitsValid = false;
						} else {
							e.normalPos = e.min.z;
						}
						if(e.valid && (e.max.x != maxX || e.max.y != maxY || e.min.x != minX || e.min.y != minY)) {
							e.valid = false;
							e.setInvalidReason(Lng.str("Unit group must have same size as the biggest:") + "[" + e.min + ", " + e.max + "] / [(" + minX + ", " + minY + ", " + minZ + "), (" + maxX + ", " + maxY + ", " + maxZ + ")]");
							unitsValid = false;
						}
					}
				}
				if(!unitsValid) {
					LogUtil.sy().fine(getSegmentController() + "; " + this + " One or more shipyard unit invalid (PRECHECK)");
				}
				if(unitsValid) {
					/*
					 * check if all parts are within the correct distance
					 */
					Collections.sort(getElementCollections(), this);
					int size = getElementCollections().size();
					int aX = ElementCollection.getPosX(getElementCollections().get(0).endA);
					int aY = ElementCollection.getPosY(getElementCollections().get(0).endA);
					int aZ = ElementCollection.getPosZ(getElementCollections().get(0).endA);
					int bX = ElementCollection.getPosX(getElementCollections().get(0).endB);
					int bY = ElementCollection.getPosY(getElementCollections().get(0).endB);
					int bZ = ElementCollection.getPosZ(getElementCollections().get(0).endB);
					int normalDistance = getMaxArcDistance();
					for(int i = 0; i < size; i++) {
						ShipyardUnit u = getElementCollections().get(i);
						if(i == 0 && size > 1) {
							if(Math.abs(u.normalPos - getElementCollections().get(1).normalPos) > normalDistance) {
								u.setValid(false);
								u.setInvalidReason(Lng.str("Unit group too far from next. It's %s / %s blocks", Math.abs(u.normalPos - getElementCollections().get(1).normalPos), normalDistance));
								unitsValid = false;
							}
						} else if(i == size - 1 && size > 1) {
							if(Math.abs(u.normalPos - getElementCollections().get(i - 1).normalPos) > normalDistance) {
								u.setValid(false);
								u.setInvalidReason(Lng.str("Unit group too far from next. It's %s / %s blocks", Math.abs(u.normalPos - getElementCollections().get(i - 1).normalPos), normalDistance));
								unitsValid = false;
							}
						} else if(size > 1 && i < size - 1 && i > 0) {
							if(Math.abs(u.normalPos - getElementCollections().get(i + 1).normalPos) > normalDistance) {
								u.setValid(false);
								u.setInvalidReason(Lng.str("Unit group too far from next. It's %s / %s blocks", Math.abs(u.normalPos - getElementCollections().get(i + 1).normalPos), normalDistance));
								unitsValid = false;
							}
						} else if(size > 1 && i < size - 1 && i > 0) {
							if(Math.abs(u.normalPos - getElementCollections().get(i - 1).normalPos) > normalDistance) {
								u.setValid(false);
								u.setInvalidReason(Lng.str("Unit group too far from next. It's %s / %s blocks", Math.abs(u.normalPos - getElementCollections().get(i - 1).normalPos), normalDistance));
								unitsValid = false;
							}
						}
						int avX = ElementCollection.getPosX(u.endA);
						int avY = ElementCollection.getPosY(u.endA);
						int avZ = ElementCollection.getPosZ(u.endA);
						int bvX = ElementCollection.getPosX(u.endB);
						int bvY = ElementCollection.getPosY(u.endB);
						int bvZ = ElementCollection.getPosZ(u.endB);
						if(biggest == 0) {
							if(avY != aY || bvY != bY || avZ != aZ || bvZ != bZ) {
								u.setValid(false);
								u.setInvalidReason(Lng.str("End Points of this Unit group doesn't match up another unit group (YZ)"));
								unitsValid = false;
							}
						} else if(biggest == 1) {
							if(avX != aX || bvX != bX || avZ != aZ || bvZ != bZ) {
								u.setValid(false);
								u.setInvalidReason(Lng.str("End Points of this Unit group doesn't match up another unit group (XZ)"));
								unitsValid = false;
							}
						} else {
							assert (biggest == 2);
							if(avX != aX || bvX != bX || avY != aY || bvY != bY) {
								u.setValid(false);
								u.setInvalidReason(Lng.str("End Points of this Unit group doesn't match up another unit group (XY)"));
								unitsValid = false;
							}
						}
					}
					if(!unitsValid) {
						LogUtil.sy().fine(getSegmentController() + "; " + this + " One or more shipyard unit invalid (MIDCHECK)");
					}
				}
			}
		}
		// System.err.println("UNITS VALID: ::: ::: "+unitsValid);
	}

	private int getMaxArcDistance() {
		return ShipyardElementManager.ARC_MAX_SPACING;
	}

	public float getPowerConsumption() {
		return DEBUG_MODE ? 0 : getTotalSize() * ShipyardElementManager.POWER_COST_NEEDED_PER_BLOCK;
	}

	private double getReactorPowerUsage() {
		double p = (double) ShipyardElementManager.REACTOR_POWER_CONSUMPTION_RESTING * getTotalSize();
		return getConfigManager().apply(StatusEffectType.WARP_POWER_EFFICIENCY, p);
	}

	@Override
	public double getPowerConsumedPerSecondResting() {
		return getReactorPowerUsage();
	}

	@Override
	public double getPowerConsumedPerSecondCharging() {
		return getReactorPowerUsage();
	}

	@Override
	public boolean isPowerCharging(long curTime) {
		return true;
	}

	@Override
	public float getPowered() {
		return powered;
	}

	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.SHIPYARD;
	}

	@Override
	public void reloadFromReactor(double secTime, Timer timer, float tickTime, boolean powerCharging, float poweredResting) {
	}

	@Override
	public boolean isPowerConsumerActive() {
		return true;
	}

	@Override
	public boolean isUsingIntegrity() {
		return false;
	}

	@Override
	public String toString() {
		return "ShipColMan(" + getControllerPos() + ")";
	}

	@Override
	public void update(Timer timer) {
		boolean step = timer.currentTime - lastFSMUpdate > 100;
		boolean valid = isValid();
		if(getSegmentController().isUsingPowerReactors()) {
			isPowered = powered > 0.9999999f;
		} else {
			PowerAddOn powerAddOn = ((PowerManagerInterface) ((ManagedSegmentController<?>) getSegmentController()).getManagerContainer()).getPowerAddOn();
			float powerConsumed = getPowerConsumption() * 0.1f;
			if(!powerAddOn.canConsumePowerInstantly(powerConsumed)) {
				isPowered = false;
			} else {
				isPowered = true;
				if(step && valid) {
					powerAddOn.consumePowerInstantly(powerConsumed);
				}
			}
		}
		if(getSegmentController().isOnServer()) {
			if(wasValidLogCheck && !valid) {
				LogUtil.sy().fine(getSegmentController() + "; " + this + " " + getCurrentStateServer() + " no longer valid");
			} else if(!wasValidLogCheck && valid) {
				LogUtil.sy().fine(getSegmentController() + "; " + this + " " + getCurrentStateServer() + " is now valid again");
			}
		}
		wasValidLogCheck = valid;
		if(step && valid) {
			lastFSMUpdate = timer.currentTime;
		}
		SegmentController d;
		if(!getSegmentController().isOnServer()) {
			boolean docked = (d = getCurrentDocked()) != null;
			if(docked) {
				float speed = -1;
				if(d.getTotalElements() > 50000) {
					speed = 100;
				}
				if(d.isVirtualBlueprint() && currentClientState == stateClass2Ids.getByte(Constructing.class)) {
					if(speed > 0) {
						d.percentageDrawn = Math.max(0.01f, ((int) ((float) completionOrderPercent * speed)) / speed);
					} else {
						d.percentageDrawn = Math.max(0.01f, (float) completionOrderPercent);
					}
				} else if(!d.isVirtualBlueprint() && currentClientState == stateClass2Ids.getByte(Deconstructing.class)) {
					if(speed > 0) {
						d.percentageDrawn = 1.0f - Math.max(0.01f, ((int) ((float) completionOrderPercent * speed)) / speed);
					} else {
						d.percentageDrawn = 1.0f - (float) completionOrderPercent;
					}
				} else {
					d.percentageDrawn = 1.0f;
				}
			}
			if(getConnectedCorePositionValid()) {
				SegmentPiece pointUnsave = getSegmentController().getSegmentBuffer().getPointUnsave(getConnectedCorePositionBlock4());
				if(pointUnsave != null) {
					((DrawableRemoteSegment) pointUnsave.getSegment()).exceptDockingBlock = docked;
					if(((DrawableRemoteSegment) pointUnsave.getSegment()).exceptDockingBlock != ((DrawableRemoteSegment) pointUnsave.getSegment()).exceptDockingBlockMarked) {
						((DrawableRemoteSegment) pointUnsave.getSegment()).setNeedsMeshUpdate(true);
						((DrawableRemoteSegment) pointUnsave.getSegment()).exceptDockingBlockMarked = ((DrawableRemoteSegment) pointUnsave.getSegment()).exceptDockingBlock;
					}
				}
			}
		}
		if(timer.currentTime - lastDockValidTest > 500) {
			dockingValid = isCurrentDockedValid();
			lastDockValidTest = timer.currentTime;
			if(getSegmentController().isOnServer()) {
				if(wasDockingValid && !dockingValid) {
					LogUtil.sy().fine(getSegmentController() + "; " + this + " " + getCurrentStateServer() + ": docking no longer valid: " + getCurrentDocked());
				} else if(!wasDockingValid && dockingValid) {
					LogUtil.sy().fine(getSegmentController() + "; " + this + " " + getCurrentStateServer() + ": docking is now valid: " + getCurrentDocked());
				}
				wasDockingValid = dockingValid;
			}
		}
		if(!getSegmentController().isOnServer() && ((currentClientState < 0 && timer.currentTime - lastStateRequest > 500) || (((GameClientState) getState()).getCurrentSectorId() == getSegmentController().getSectorId() && timer.currentTime - lastStateRequest > 10000))) {
			sendStateRequestToServer(ShipyardRequestType.STATE);
			lastStateRequest = timer.currentTime;
		}
		if(valid) {
			wasValid = true;
			// getElementCollections().get(0).update(timer);
			// either initially request or request every 10 sec if client is in the same sector
			if(getSegmentController().isOnServer()) {
				if(!isPowered()) {
					if(wasPowered) {
						LogUtil.sy().fine(getSegmentController() + "; " + this + " " + getCurrentStateServer() + ": no longer powered");
					}
				} else {
					if(!wasPowered) {
						LogUtil.sy().fine(getSegmentController() + "; " + this + " " + getCurrentStateServer() + ": is now powered");
					}
				}
			}
			wasPowered = isPowered();
			if(getSegmentController().isOnServer() && isPowered() && step) {
				if(lastServerState != getCurrentStateServer()) {
					LogUtil.sy().fine(getSegmentController() + "; " + this + " updating now in state: " + getCurrentStateServer());
				}
				lastServerState = getCurrentStateServer();
				if(getCurrentStateServer() != null && getCurrentStateServer().isPullingResources()) {
					LogUtil.sy().fine(getSegmentController() + "; " + this + " " + getCurrentStateServer() + " pulling resources");
					getCurrentStateServer().pullResources();
				}
				Object2ObjectOpenHashMap<String, Sendable> uMap = getState().getLocalAndRemoteObjectContainer().getUidObjectMap();
				boolean waitingForTagDesign = (waitForExistsUntilFSMUpdate != null && !uMap.containsKey(waitForExistsUntilFSMUpdate));
				if(waitingForTagDesign) {
					if(lastServerState != getCurrentStateServer()) {
						LogUtil.sy().fine(getSegmentController() + "; " + this + " " + getCurrentStateServer() + " waiting for design UID: " + waitForExistsUntilFSMUpdate);
					}
				}
				SegmentPiece p;
				if(waitForExistsUntilFSMUpdate != null && uMap.containsKey(waitForExistsUntilFSMUpdate) && ((p = ((SegmentController) uMap.get(waitForExistsUntilFSMUpdate)).getSegmentBuffer().getPointUnsave(Ship.core)) == null || p.getType() != ElementKeyMap.CORE_ID)) {
					// autorequest true previously
					waitingForTagDesign = true;
					if(lastServerState != getCurrentStateServer()) {
						LogUtil.sy().fine(getSegmentController() + "; " + this + " " + getCurrentStateServer() + " waiting for design UID core loaded (UID exists): " + waitForExistsUntilFSMUpdate);
					}
				}
				if(waitingForTagDesign || (getCurrentDocked() != null && !getCurrentDocked().isFullyLoadedWithDock())) {
					if(lastState != (byte) -2) {
						sendShipyardStateToClient();
						lastState = -2;
					}
					if(lastServerState != getCurrentStateServer()) {
						LogUtil.sy().fine(getSegmentController() + "; " + this + " " + getCurrentStateServer() + " waiting for design fully loaded with dock (UID exists): " + waitForExistsUntilFSMUpdate + "; DOCK: " + getCurrentDocked());
					}
				} else {
					// when loaded from tag, wait until possible object is available until update continues
					waitForExistsUntilFSMUpdate = null;
					try {
						assert (serverEntityState.getCurrentProgram() != null);
						assert (!serverEntityState.getCurrentProgram().isSuspended());
						serverEntityState.updateOnActive(timer);
					} catch(FSMException e) {
						e.printStackTrace();
						LogUtil.sy().log(Level.WARNING, getSegmentController() + "; " + this + " " + getCurrentStateServer() + " FSM Exception: " + e.getClass() + "; " + e.getMessage(), e);
					}
					if(!commandQueue.isEmpty()) {
						if(command == null) command = commandQueue.poll();
						if(command != null) {
							if(!command.isStarted(this)) command.onStart(this);
							else if(command.isFinished(this)) {
								command.onFinish(this);
								command = null;
							}
						}
					}
					if(getCurrentDocked() != null && !dockingValid) {
						LogUtil.sy().fine(getSegmentController() + "; " + this + " " + getCurrentStateServer() + " current dock invalid: " + getCurrentDocked());
						if(lastState != (byte) -3) {
							sendShipyardStateToClient();
							lastState = -3;
						}
					} else if(serverEntityState.getCurrentProgram().getMachine() != null && serverEntityState.getCurrentProgram().getMachine().getFsm().getCurrentState() != null) {
						State currentState = serverEntityState.getCurrentProgram().getMachine().getFsm().getCurrentState();
						// System.err.println(" STATE::: :: :: :: SSS "+stateClass2Ids.get(currentState.getClass())+" ,, "+currentState.getClass().getSimpleName()+"; "+waitForExistsUntilFSMUpdate);
						if(stateClass2Ids.get(currentState.getClass()) != lastState) {
							LogUtil.sy().fine(getSegmentController() + "; " + this + " " + getCurrentStateServer() + " sending current state to client: " + currentState);
							sendShipyardStateToClient();
							lastState = stateClass2Ids.get(currentState.getClass());
						}
					} else {
						LogUtil.sy().severe(getSegmentController() + "; " + this + " " + getCurrentStateServer() + " SHIPYARD IN INVALID STATE! " + serverEntityState.getCurrentProgram().getMachine() + "; " + (serverEntityState.getCurrentProgram().getMachine() != null ? serverEntityState.getCurrentProgram().getMachine().getFsm().getCurrentState() : ""));
						assert (false);
						if(lastState != (byte) -1) {
							sendShipyardStateToClient();
							lastState = -1;
						}
					}
				}
			}
		} else {
			if(wasValid && getSegmentController().isOnServer()) {
				State startState = serverEntityState.getCurrentProgram().getMachine().getStartState();
				serverEntityState.getCurrentProgram().getMachine().setState(startState);
				startState.setNewState(true);
				wasValid = false;
				currentDesign = -1;
				sendShipyardStateToClient();
				LogUtil.sy().fine(getSegmentController() + "; " + this + " " + getCurrentStateServer() + " shipyard no longer valid: " + getCurrentDocked() + ": start state: " + startState);
			}
			if(!getSegmentController().isOnServer() && ((GameClientState) getSegmentController().getState()).getCurrentSectorId() == getSegmentController().getSectorId()) {
				if(getTotalSize() > 0 && System.currentTimeMillis() - lastPopup > 5000) {
					for(ShipyardUnit u : getElementCollections()) {
						if(!u.isValid() && u.getInvalidReason() != null) {
							Transform t = new Transform();
							t.setIdentity();
							Vector3i p = ElementCollection.getPosFromIndex(u.endA, new Vector3i());
							t.origin.set(p.x - SegmentData.SEG_HALF, p.y - SegmentData.SEG_HALF, p.z - SegmentData.SEG_HALF);
							getSegmentController().getWorldTransform().transform(t.origin);
							RaisingIndication raisingIndication = new RaisingIndication(t, u.getInvalidReason(), 1.0f, 0.3f, 0.3f, 1.0f);
							raisingIndication.speed = 0.1f;
							raisingIndication.lifetime = 4.6f;
							HudIndicatorOverlay.toDrawTexts.add(raisingIndication);
						}
					}
					Transform t = new Transform();
					t.setIdentity();
					Vector3i p = getControllerPos();
					t.origin.set(p.x - SegmentData.SEG_HALF, p.y - SegmentData.SEG_HALF, p.z - SegmentData.SEG_HALF);
					getSegmentController().getWorldTransform().transform(t.origin);
					String msg;
					if(!unitsValid) {
						msg = Lng.str("Shipyard structure invalid!");
					} else if(!getConnectedCorePositionValid()) {
						msg = Lng.str("Shipyard must have exactly one connected Shipyard Core Anchor \nin the shipyard shipyard arc area!");
					} else if(!isPowered()) {
						msg = Lng.str("Shipyard unpowered!");
					} else {
						msg = Lng.str("Shipyard structure invalid!");
					}
					RaisingIndication raisingIndication = new RaisingIndication(t, msg, 1.0f, 0.1f, 0.1f, 1.0f);
					raisingIndication.speed = 0.03f;
					raisingIndication.lifetime = 6.6f;
					HudIndicatorOverlay.toDrawTexts.add(raisingIndication);
					lastPopup = System.currentTimeMillis();
				}
			}
			// System.err.println("NOT UPDATING INVALID WARPGATE cols: "+getElementCollections().size()+"; ");
			// if(getElementCollections().size() == 1){
			// System.err.println("DEBUG VALID: "+getElementCollections().get(0).getValidInfo());
			// }
		}
	}

	public int getDesignId(String name) {
		for(VirtualBlueprintMetaItem design : getDesignList()) {
			if(design.UID.equals(name)) return design.getId();
		}
		return -1;
	}

	public boolean isFinished() {
		return completionOrderPercent >= 1 || completionOrderPercent < 0;
	}

	public ShipyardCommandData getCurrentCommand() {
		return command;
	}

	private void undockCurrent(FleetMember member) {
		undockRequestedFromShipyard();
		moveFromShipyard(member.getLoaded());
	}

	private void moveFromShipyard(SegmentController docked) {
		Vector3f dir = new Vector3f();
		Transform transform = docked.railController.getRelativeToRootLocalTransform();
		switch(getCore().getOrientation()) {
			case (Element.RIGHT) -> GlUtil.getLeftVector(dir, transform);
			case (Element.LEFT) -> GlUtil.getRightVector(dir, transform);
			case (Element.TOP) -> GlUtil.getUpVector(dir, transform);
			case (Element.BOTTOM) -> GlUtil.getBottomVector(dir, transform);
			case (Element.FRONT) -> GlUtil.getForwardVector(dir, transform);
			case (Element.BACK) -> GlUtil.getBackVector(dir, transform);
		}
		dir.scale(3);
		docked.getPhysicsObject().setLinearVelocity(dir);
	}

	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {
		return new GUIKeyValueEntry[]{new ModuleValueEntry(Lng.str("Power Consumption"), StringTools.formatPointZero(getPowerConsumption())), new ModuleValueEntry(Lng.str("Max Arc Distance"), getMaxArcDistance())};
	}

	@Override
	public String getModuleName() {
		return Lng.str("Shipyard System");
	}

	public boolean isValid() {
		return alwaysValid() | (unitsValid && getConnectedCorePositionValid());
	}

	private boolean getConnectedCorePositionValid() {
		long index4 = getConnectedCorePositionBlock4();
		if(index4 == Long.MIN_VALUE) {
			return false;
		} else {
			int x = ElementCollection.getPosX(index4);
			int y = ElementCollection.getPosY(index4);
			int z = ElementCollection.getPosZ(index4);
			return x >= minX && y >= minY && z >= minZ && x < maxX && y < maxY && z < maxZ;
		}
	}

	@Override
	public int compare(ShipyardUnit o1, ShipyardUnit o2) {
		return o2.normalPos - o1.normalPos;
	}

	public boolean isCurrentDockedValid() {
		SegmentController currentDocked = getCurrentDocked();
		if(currentDocked != null && currentDocked.getSegmentBuffer().getBoundingBox().isValid()) {
			if(DEBUG_MODE) {
				return true;
			}
			Transform transform = new Transform(currentDocked.getPhysicsDataContainer().getShapeChild().transform);
			// Transform transform = new Transform(getSegmentController().getWorldTransformInverse());
			// transform.mul(currentDocked.getWorldTransform());
			// transform.inverse();
			minbb.set(currentDocked.getSegmentBuffer().getBoundingBox().min);
			maxbb.set(currentDocked.getSegmentBuffer().getBoundingBox().max);
			minbb.x += Element.BLOCK_SIZE;
			minbb.y += Element.BLOCK_SIZE;
			minbb.z += Element.BLOCK_SIZE;
			maxbb.x -= Element.BLOCK_SIZE;
			maxbb.y -= Element.BLOCK_SIZE;
			maxbb.z -= Element.BLOCK_SIZE;
			// AabbUtil2.transformAabb(minbb, maxbb, 0.0f, transform, minbbOut, maxbbOut);
			transform.transform(minbb);
			transform.transform(maxbb);
			minbbOut.x = Math.min(minbb.x, maxbb.x);
			minbbOut.y = Math.min(minbb.y, maxbb.y);
			minbbOut.z = Math.min(minbb.z, maxbb.z);
			maxbbOut.x = Math.max(minbb.x, maxbb.x);
			maxbbOut.y = Math.max(minbb.y, maxbb.y);
			maxbbOut.z = Math.max(minbb.z, maxbb.z);
			// System.err.println("HH \n"+transform.basis);
			// System.err.println("HH "+minbb+"; "+maxbb+"; || "+transform.origin+" || "+minbbOut+"; "+maxbbOut);
			minbbOut.x += SegmentData.SEG_HALF + 1.0f;
			minbbOut.y += SegmentData.SEG_HALF + 1.0f;
			minbbOut.z += SegmentData.SEG_HALF + 1.0f;
			maxbbOut.x += SegmentData.SEG_HALF;
			maxbbOut.y += SegmentData.SEG_HALF;
			maxbbOut.z += SegmentData.SEG_HALF;
			minbbOut.x = FastMath.round(minbbOut.x);
			minbbOut.y = FastMath.round(minbbOut.y);
			minbbOut.z = FastMath.round(minbbOut.z);
			maxbbOut.x = FastMath.round(maxbbOut.x);
			maxbbOut.y = FastMath.round(maxbbOut.y);
			maxbbOut.z = FastMath.round(maxbbOut.z);
			// System.err.println("AABB ::: "+(minbbOut.x +" >= "+ minX )+", "+ (minbbOut.y +" >= "+ minY) +", "+ (minbbOut.z +" >= "+ minZ) +", "+ (maxbbOut.x +" <= "+ maxX) +", "+ (maxbbOut.y +" <= "+ maxY) +", "+ (maxbbOut.z +" <= "+ maxZ));
			// System.err.println("AABB ::: "+(minbbOut.x >= minX )+", "+ (minbbOut.y >= minY) +", "+ (minbbOut.z >= minZ) +", "+ (maxbbOut.x <= maxX) +", "+ (maxbbOut.y <= maxY) +", "+ (maxbbOut.z <= maxZ));
			if(biggest == 0) {
				return minbbOut.x >= minX && maxbbOut.x <= maxX;
			} else if(biggest == 1) {
				return minbbOut.y >= minY && maxbbOut.y <= maxY;
			} else if(biggest == 2) {
				return minbbOut.z >= minZ && maxbbOut.z <= maxZ;
			}
			return minbbOut.x >= minX && minbbOut.y >= minY && minbbOut.z >= minZ && maxbbOut.x <= maxX && maxbbOut.y <= maxY && maxbbOut.z <= maxZ;
		}
		return true;
	}

	public SegmentController getCurrentDocked() {
		long index4 = getConnectedCorePositionBlock4();
		if(index4 != Long.MIN_VALUE) {
			// long index3 = ElementCollection.getPosIndexFrom4(index4);
			for(RailRelation r : getSegmentController().railController.next) {
				if(r.rail.getAbsoluteIndexWithType4() == index4) {
					return r.docked.getSegmentController();
				}
			}
		}
		return null;
	}

	public LongOpenHashSet getConnectedCorePositionBlockMap() {
		Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> map = getSegmentController().getControlElementMap().getControllingMap().get(getControllerElement().getAbsoluteIndex());
		if(map != null) {
			return map.get(ElementKeyMap.SHIPYARD_CORE_POSITION);
		}
		return null;
	}

	public long getConnectedCorePositionBlock4() {
		LongOpenHashSet m = getConnectedCorePositionBlockMap();
		if(m != null && m.size() == 1) {
			return m.iterator().nextLong();
		}
		return Long.MIN_VALUE;
	}

	public Inventory getInventory() {
		return getContainer().getInventory(getControllerElement().getAbsoluteIndex());
	}

	public ShipyardEntityState getServerEntityState() {
		return serverEntityState;
	}

	public ShipyardState getCurrentStateServer() {
		if(serverEntityState.getCurrentProgram() != null && serverEntityState.getCurrentProgram().getMachine() != null && serverEntityState.getCurrentProgram().getMachine().getFsm() != null && serverEntityState.getCurrentProgram().getMachine().getFsm().getCurrentState() != null) {
			State currentState = serverEntityState.getCurrentProgram().getMachine().getFsm().getCurrentState();
			return (ShipyardState) currentState;
		}
		return null;
	}

	public void handleShipyardCommandOnServer(int factionId, ShipyardCommandType t, Object[] args) {
		if(serverEntityState.getCurrentProgram() != null && serverEntityState.getCurrentProgram().getMachine() != null && serverEntityState.getCurrentProgram().getMachine().getFsm() != null && serverEntityState.getCurrentProgram().getMachine().getFsm().getCurrentState() != null) {
			serverEntityState.lastOrderFactionId = factionId;
			State currentState = serverEntityState.getCurrentProgram().getMachine().getFsm().getCurrentState();
			if(t.requiredState.equals(currentState.getClass())) {
				try {
					serverEntityState.handle(t, args);
					serverEntityState.getCurrentProgram().getMachine().getFsm().stateTransition(t.transition);
				} catch(FSMException e) {
					e.printStackTrace();
					sendShipyardErrorToClient(Lng.str("Critical: Invalid Transition\n%s", StringTools.wrap(e.getMessage(), 100)));
				}
			} else {
				sendShipyardErrorToClient(Lng.str("Critical: Required state not met\n%s\n%s", t.requiredState.getSimpleName(), currentState.getClass().getSimpleName()));
				assert (false) : t.name() + "; CURRENT: " + currentState.getClass().getSimpleName() + "; REQUIRED: " + t.requiredState.getSimpleName();
			}
		} else {
			sendShipyardErrorToClient(Lng.str("Critical: FSM not setup"));
			assert (false);
		}

	}

	private boolean canExecute(ShipyardCommandType t) {
		// if(t == ShipyardCommandType.TEST_DESIGN){
		// if(getCurrentDocked() != null){
		// if(!getCurrentDocked().railController.next.isEmpty()){
		// sendShipyardErrorToClient("Sorry! Testing out designs with docks/rails isn't\nyet supported, but will be very soon.");
		// return false;
		// }
		// }
		// }
		return true;
	}

	public void sendShipyardStateToClient() {
		assert (getSegmentController().isOnServer());
		ShipyardCurrentStateValueUpdate v = new ShipyardCurrentStateValueUpdate();
		assert (v.getType() == ValueUpdate.ValTypes.SHIPYARD_STATE_UPDATE);
		v.setServer(((ManagedSegmentController<?>) getSegmentController()).getManagerContainer(), getControllerElement().getAbsoluteIndex());
		((NTValueUpdateInterface) getSegmentController().getNetworkObject()).getValueUpdateBuffer().add(new RemoteValueUpdate(v, getSegmentController().isOnServer()));
	}

	public void sendShipyardGoalToClient() {
		assert (getSegmentController().isOnServer());
		ShipyardBlockGoalValueUpdate v = new ShipyardBlockGoalValueUpdate();
		assert (v.getType() == ValueUpdate.ValTypes.SHIPYARD_BLOCK_GOAL);
		v.setServer(((ManagedSegmentController<?>) getSegmentController()).getManagerContainer(), getControllerElement().getAbsoluteIndex());
		((NTValueUpdateInterface) getSegmentController().getNetworkObject()).getValueUpdateBuffer().add(new RemoteValueUpdate(v, getSegmentController().isOnServer()));
	}

	public void sendShipyardErrorToClient(String error) {
		assert (getSegmentController().isOnServer());
		ShipyardErrorValueUpdate v = new ShipyardErrorValueUpdate(error);
		assert (v.getType() == ValueUpdate.ValTypes.SHIPYARD_ERROR_UPDATE);
		v.setServer(((ManagedSegmentController<?>) getSegmentController()).getManagerContainer(), getControllerElement().getAbsoluteIndex());
		((NTValueUpdateInterface) getSegmentController().getNetworkObject()).getValueUpdateBuffer().add(new RemoteValueUpdate(v, getSegmentController().isOnServer()));
	}

	public void sendShipyardCommandToServer(int factionId, ShipyardCommandType t, Object... args) {
		assert (!getSegmentController().isOnServer());
		ShipyardClientCommandValueUpdate v = new ShipyardClientCommandValueUpdate(new ShipyardCommand(factionId, t, args));
		assert (v.getType() == ValueUpdate.ValTypes.SHIPYARD_CLIENT_COMMAND);
		v.setServer(((ManagedSegmentController<?>) getSegmentController()).getManagerContainer(), getControllerElement().getAbsoluteIndex());
		((NTValueUpdateInterface) getSegmentController().getNetworkObject()).getValueUpdateBuffer().add(new RemoteValueUpdate(v, getSegmentController().isOnServer()));
	}

	public void sendStateRequestToServer(ShipyardRequestType request) {
		assert (!getSegmentController().isOnServer());
		ShipyardClientStateRequestValueUpdate v = new ShipyardClientStateRequestValueUpdate(request);
		assert (v.getType() == ValueUpdate.ValTypes.SHIPYARD_CLIENT_STATE_REQUEST);
		v.setServer(((ManagedSegmentController<?>) getSegmentController()).getManagerContainer(), getControllerElement().getAbsoluteIndex());
		((NTValueUpdateInterface) getSegmentController().getNetworkObject()).getValueUpdateBuffer().add(new RemoteValueUpdate(v, getSegmentController().isOnServer()));
	}

	public void orderCommand(ShipyardCommandData command) {
		commandQueue.add(command);
	}

	public boolean isWorkingOnOrder() {
		return completionOrderPercent >= 0;
	}

	public double getCompletionOrderPercent() {
		return completionOrderPercent;
	}

	public List<VirtualBlueprintMetaItem> getDesignList() {
		ObjectArrayList<VirtualBlueprintMetaItem> designs = new ObjectArrayList<>();
		for(int slot : getInventory().getSlots()) {
			int meta = getInventory().getMeta(slot);
			if(meta >= 0) {
				MetaObject object = ((MetaObjectState) getSegmentController().getState()).getMetaObjectManager().getObject(meta);
				if(object == null) {
					if(requestedMeta.contains(meta)) {
						((MetaObjectState) getSegmentController().getState()).getMetaObjectManager().checkAvailable(meta, ((MetaObjectState) getSegmentController().getState()));
						requestedMeta.add(meta);
					}

				} else if(object instanceof VirtualBlueprintMetaItem) designs.add((VirtualBlueprintMetaItem) object);
			}
		}
		return designs;
	}

	public List<GUIElement> getGUIInventoryDesignList() {
		ObjectArrayList<GUIElement> l = new ObjectArrayList<GUIElement>();
		for(int slot : getInventory().getSlots()) {
			int meta = getInventory().getMeta(slot);
			if(meta >= 0) {
				MetaObject object = ((MetaObjectState) getSegmentController().getState()).getMetaObjectManager().getObject(meta);
				if(object == null) {
					if(requestedMeta.contains(meta)) {
						((MetaObjectState) getSegmentController().getState()).getMetaObjectManager().checkAvailable(meta, ((MetaObjectState) getSegmentController().getState()));
						requestedMeta.add(meta);
					}
				} else if(object instanceof VirtualBlueprintMetaItem o) {
					GUIAnchor a = new GUIAnchor((GameClientState) getState(), 300, 24);
					GUITextOverlay t = new GUITextOverlay((GameClientState) getState());
					t.setTextSimple(o.virtualName);
					t.setPos(5, 4, 0);
					a.attach(t);
					a.setUserPointer(o.getId());
					l.add(a);
				}
			}
		}
		Collections.sort(l, (o1, o2) -> {
			GUITextOverlay a1 = (GUITextOverlay) o1.getChilds().get(0);
			GUITextOverlay a2 = (GUITextOverlay) o2.getChilds().get(0);
			return a1.getText().get(0).toString().toLowerCase(Locale.ENGLISH).compareTo(a2.getText().get(0).toString().toLowerCase(Locale.ENGLISH));
		});
		return l;
	}

	public List<GUIElement> getGUIInventoryBlueprintList() {
		ObjectArrayList<GUIElement> l = new ObjectArrayList<GUIElement>();
		for(int slot : getInventory().getSlots()) {
			int meta = getInventory().getMeta(slot);
			if(meta >= 0) {
				MetaObject object = ((MetaObjectState) getSegmentController().getState()).getMetaObjectManager().getObject(meta);
				if(object == null) {
					if(requestedMeta.contains(meta)) {
						((MetaObjectState) getSegmentController().getState()).getMetaObjectManager().checkAvailable(meta, ((MetaObjectState) getSegmentController().getState()));
						requestedMeta.add(meta);
					}
				} else if(object instanceof BlueprintMetaItem o) {
					GUIAnchor a = new GUIAnchor((GameClientState) getState(), 300, 24);
					GUITextOverlay t = new GUITextOverlay((GameClientState) getState());
					t.setTextSimple(o.blueprintName);
					t.setPos(5, 4, 0);
					a.attach(t);
					a.setUserPointer(o.blueprintName);
					l.add(a);
				}
			}
		}
		Collections.sort(l, (o1, o2) -> {
			GUITextOverlay a1 = (GUITextOverlay) o1.getChilds().get(0);
			GUITextOverlay a2 = (GUITextOverlay) o2.getChilds().get(0);
			return a1.getText().get(0).toString().toLowerCase(Locale.ENGLISH).compareTo(a2.getText().get(0).toString().toLowerCase(Locale.ENGLISH));
		});
		return l;
	}

	/**
	 * @param completionOrderPercent
	 * @return true, if full percent changed
	 */
	public boolean setCompletionOrderPercent(double completionOrderPercent) {
		this.completionOrderPercent = completionOrderPercent;
		if((int) (completionOrderPercent * 100.0) != lastIntPercent) {
			lastIntPercent = (int) (completionOrderPercent * 100.0);
			return true;
		}
		return false;
	}

	public boolean setCompletionOrderPercentAndSendIfChanged(double completionOrderPercent) {
		boolean c = setCompletionOrderPercent(completionOrderPercent);
		if(c) {
			sendShipyardStateToClient();
		}
		return c;
	}

	public byte getStateByteOnServer() {
		if(isValid() && serverEntityState.getCurrentProgram() != null && serverEntityState.getCurrentProgram().getMachine() != null && serverEntityState.getCurrentProgram().getMachine().getFsm() != null && serverEntityState.getCurrentProgram().getMachine().getFsm().getCurrentState() != null) {
			return stateClass2Ids.getByte(serverEntityState.getCurrentProgram().getMachine().getFsm().getCurrentState().getClass());
		} else {
			return (byte) -1;
		}
	}

	public String getStateDescription() {
		if(currentClientState == (byte) -2) {
			return Lng.str("Please wait for the entity to load...");
		} else if(currentClientState < 0) {
			return Lng.str("Out of service...") + (isValid() ? Lng.str("(server error)") : Lng.str("(shipyard invalid)"));
		}
		return ids2StateDescription.get(currentClientState);
	}

	public boolean isLoadedDesignValid() {
		SegmentController docked;
		return currentDesign >= 0 && isValid() && getInventory().conatainsMetaItem(currentDesign) && (docked = getCurrentDocked()) != null && getCurrentDesignObject() != null && getCurrentDesignObject().UID.equals(docked.getUniqueIdentifier()) && docked.isVirtualBlueprint();
	}

	public String isLoadedDesignValidToSring() {
		if(currentDesign >= 0 && !getInventory().conatainsMetaItem(currentDesign)) {
			System.err.println("META ITEM MISSING: " + currentDesign);
			System.err.println("INVETORY: " + getInventory().getParameter() + "; content: " + getInventory().toString());
		}
		SegmentController docked;
		return "CurrentDesign ID > 0? " + (currentDesign >= 0) + "; SY valid: " + isValid() + "; InventoryHadDesignMeta: " + getInventory().conatainsMetaItem(currentDesign) + "; currentDocked != null? " + ((docked = getCurrentDocked()) != null) + "; UID equals: " + (docked != null && getCurrentDesignObject() != null && getCurrentDesignObject().UID != null && getCurrentDesignObject().UID.equals(docked.getUniqueIdentifier())) + "; isVirtual: " + (docked != null && docked.isVirtualBlueprint());
	}

	/**
	 * turns currently docked virtual, write it, and removes it from current state
	 * <p>
	 * AFTER THAT THE ENTITY IS VIRTUAL AND WILL NOT BE LOADED ON SECTOR LOAD
	 * IT HAS TO BE MANUALLY LOADED OVER IT'S CORRESPONDING DESIGN
	 */
	public void unloadCurrentDockedVolatile() {
		SegmentController currentDocked = getCurrentDocked();
		if(currentDocked != null) {
			LogUtil.sy().fine(getSegmentController() + "; " + this + " write&unload starting for " + currentDocked);
			currentDocked.setVirtualBlueprintRecursive(true);
			try {
				((GameServerState) getState()).getController().writeSingleEntityWithDock(currentDocked);
			} catch(IOException e) {
				e.printStackTrace();
			} catch(SQLException e) {
				e.printStackTrace();
			}
			currentDocked.setMarkedForDeleteVolatileIncludingDocks(true);
			LogUtil.sy().fine(getSegmentController() + "; " + this + " write&unload successful: design marked for volatile remove: " + currentDocked);
		} else {
			LogUtil.sy().fine(getSegmentController() + "; " + this + " cannot unload design since nothing is loaded");
		}
	}

	public SegmentController loadDesign(VirtualBlueprintMetaItem o) {
		GameServerState state = (GameServerState) getState();
		Sector sector = state.getUniverse().getSector(getSegmentController().getSectorId());
		if(sector != null) {
			try {
				SegmentController loadEntitiy = sector.loadSingleEntitiyWithDock(state, new EntityUID(o.UID, DatabaseEntry.getEntityType(o.UID), -1), true);
				if(loadEntitiy != null && loadEntitiy instanceof SegmentController) {
					SegmentController s = loadEntitiy;
					LogUtil.sy().fine(getSegmentController() + "; " + this + " design successfully loaded " + o.UID + "; " + loadEntitiy + " -> " + s);
					return s;
				} else {
					LogUtil.sy().fine(getSegmentController() + "; " + this + " load error for design " + o.UID + "; " + loadEntitiy);
					System.err.println("[SERVER][SHIPYARD] Cannot load design Entity for design not valid in database: " + o.UID + "; " + loadEntitiy);
					sendShipyardErrorToClient(Lng.str("Cannot load design\nEntity for design not valid in database!"));
				}
			} catch(SQLException e) {
				e.printStackTrace();
				sendShipyardErrorToClient(Lng.str("Cannot load design\n%s\n%s", e.getClass().getSimpleName(), e.getMessage()));
			}
		} else {
			LogUtil.sy().fine(getSegmentController() + "; " + this + " sector to load design null " + o.UID);
			System.err.println("[SERVER][SHIPYARD] Cannot load design Entity for design not found in database: " + o.UID);
			sendShipyardErrorToClient(Lng.str("Cannot load design\nEntity for design not found in database!"));
		}
		return null;
	}

	public boolean createDockingRelation(SegmentController newShip, boolean turnVirtual) {
		newShip.setVirtualBlueprintRecursive(turnVirtual);
		VoidUniqueSegmentPiece d = new VoidUniqueSegmentPiece();
		d.setSegmentController(newShip);
		d.setType(ElementKeyMap.CORE_ID);
		d.voidPos.set(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF);
		long index4 = getConnectedCorePositionBlock4();
		if(index4 == Long.MIN_VALUE || ElementCollection.getType(index4) != ElementKeyMap.SHIPYARD_CORE_POSITION) {
			System.err.println("[SERVER][SHIPYARD] Cannot create design! No core position block connected to this shipyard!");
			sendShipyardErrorToClient(Lng.str("Cannot create design!\nNo Shipyard Core Anchor connected to this shipyard!"));
			return false;
		} else {
			// autorequest true previously
			SegmentPiece pointUnsave = getSegmentController().getSegmentBuffer().getPointUnsave(index4);
			if(pointUnsave != null && pointUnsave.getType() != ElementKeyMap.SHIPYARD_CORE_POSITION) {
				System.err.println("[SERVER][SHIPYARD] Cannot create design! No core position block connected to this shipyard");
				sendShipyardErrorToClient(Lng.str("Cannot create design!\nNo Shipyard Core Anchor connected to this shipyard!"));
				return false;
			} else {
				RailRequest railRequest = newShip.railController.getRailRequest(d, pointUnsave, pointUnsave.getAbsolutePos(new Vector3i()), null, RailRelation.DockingPermission.PUBLIC);
				railRequest.fromtag = true;
				railRequest.ignoreCollision = true;
				railRequest.sentFromServer = false;
				newShip.railController.railRequestCurrent = railRequest;
				return true;
			}
		}
	}

	public void cancelOrderOnServer() {
		assert (getSegmentController().isOnServer());
		if(serverEntityState.getCurrentProgram() != null && serverEntityState.getCurrentProgram().getMachine() != null && serverEntityState.getCurrentProgram().getMachine().getFsm() != null && serverEntityState.getCurrentProgram().getMachine().getFsm().getCurrentState() != null) {
			State currentState = serverEntityState.getCurrentProgram().getMachine().getFsm().getCurrentState();
			if(currentState instanceof ShipyardState && ((ShipyardState) currentState).canCancel()) {
				try {
					currentState.stateTransition(Transition.SY_CANCEL);
				} catch(FSMException e) {
					e.printStackTrace();
					sendShipyardErrorToClient(Lng.str("This operation cannot be canceled!"));
				}
			} else {
				sendShipyardErrorToClient(Lng.str("This operation cannot be canceled!"));
			}
		} else {
			sendShipyardErrorToClient(Lng.str("Cannot cancel current operation!\nSystems out of service!"));
		}
	}

	public Class<? extends ShipyardState> getCurrentClientStateClass() {
		assert (!getSegmentController().isOnServer());
		return ids2StateClass.get(currentClientState);
	}

	public boolean isDockedInEditableState() {
		return idsCanEdit.get(currentClientState);
	}

	public boolean isPowered() {
		if(getSegmentController().isUsingPowerReactors()) {
			return powered >= 0.999999999f || DEBUG_MODE;
		}
		return isPowered || DEBUG_MODE;
	}

	@Override
	public void setPowered(float powered) {
		this.powered = powered;
	}

	public void undockRequestedFromShipyard() {
		SegmentController currentDocked = getCurrentDocked();
		if(currentDocked != null) {
			System.err.println("[SERVER] Manually undocking " + currentDocked);
			serverEntityState.wasUndockedManually = true;
			currentDocked.railController.disconnect();
		}
	}

	public boolean isInstantBuild() {
		return instantBuild;
	}

	public boolean isDockingValid() {
		return dockingValid;
	}

	public void setDockingValid(boolean dockingValid) {
		this.dockingValid = dockingValid;
	}

	@Override
	protected void onRemovedCollection(long absPos, ShipyardCollectionManager instance) {
		super.onRemovedCollection(absPos, instance);
		if(getSegmentController().isOnServer() && serverEntityState.isStateSet()) {
			State currentState = serverEntityState.getCurrentProgram().getMachine().getFsm().getCurrentState();
			if(currentState instanceof ShipyardState) {
				((ShipyardState) currentState).onShipyardRemoved(getControllerPos());
			}
		}
	}

	public int getCurrentDesign() {
		return currentDesign;
	}

	public void setCurrentDesign(int currentDesign) {
		// try{
		// throw new Exception(getSegmentController()+"; "+getState()+" Debug Exception: setting new design ID: "+currentDesign);
		// }catch(Exception r){
		// r.printStackTrace();
		// }
		this.currentDesign = currentDesign;
	}

	@Override
	public void dischargeFully() {
	}

	public enum ShipyardRequestType {

		STATE,
		INFO,
		CANCEL,
		UNDOCK
	}

	public enum ShipyardCommandType {

		CREATE_NEW_DESIGN(en -> {
			return Lng.str("Create new Design");
		}, en -> {
			return Lng.str("Creates and loads a fresh design and places it here.\nIn design mode, you can build with any block type and amount.");
		}, new ShipyardCommandDialog() {

			@Override
			public void clientSend(ShipyardCollectionManager yard) {
				PlayerTextInput pp = new PlayerTextInput("ASK", (GameClientState) yard.getState(), VirtualBlueprintMetaItem.MAX_LENGTH, "Name", "Choose a name", "D" + "_" + System.currentTimeMillis()) {

					@Override
					public String[] getCommandPrefixes() {
						return null;
					}

					@Override
					public boolean isOccluded() {
						return getState().getController().getPlayerInputs().indexOf(this) != getState().getController().getPlayerInputs().size() - 1;
					}

					@Override
					public String handleAutoComplete(String s, TextCallback callback, String prefix) {
						return s;
					}

					@Override
					public void onDeactivate() {
					}

					@Override
					public void onFailedTextCheck(String msg) {
						setErrorMessage(Lng.str("SHIPNAME INVALID: %s", msg));
					}

					@Override
					public boolean onInput(String entry) {
						yard.sendShipyardCommandToServer(((GameClientState) getState()).getPlayer().getFactionId(), CREATE_NEW_DESIGN, entry);
						return true;
					}
				};
				pp.setInputChecker(shipInputChecker);
				pp.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(916);
			}
		}, WaitingForShipyardOrder.class, Transition.SY_CREATE_DESIGN, String.class),
		UNLOAD_DESIGN(en -> {
			return Lng.str("Unload Design");
		}, en -> {
			return Lng.str("Unloads the currently loaded design.\nThis can also be done by right-clicking on the design item.");
		}, new ShipyardCommandDialog() {

			@Override
			public void clientSend(ShipyardCollectionManager yard) {
				yard.sendShipyardCommandToServer(((GameClientState) yard.getState()).getPlayer().getFactionId(), UNLOAD_DESIGN);
			}
		}, DesignLoaded.class, Transition.SY_UNLOAD_DESIGN),
		LOAD_DESIGN(en -> {
			return Lng.str("Load Design");
		}, en -> {
			return Lng.str("Loads an existing design.\nThis can also be done by right-clicking on the design item.");
		}, new ShipyardCommandDialog() {

			@Override
			public void clientSend(ShipyardCollectionManager yard) {
				List<GUIElement> ll = yard.getGUIInventoryDesignList();
				if(ll.isEmpty()) {
					((GameClientState) yard.getState()).getController().popupAlertTextMessage(Lng.str("No designs in shipyard inventory!"), 0);
					return;
				}
				PlayerGameDropDownInput p = new PlayerGameDropDownInput("SELECT_DESIGN", (GameClientState) yard.getState(), Lng.str("Designs"), 24, Lng.str("Choose a Design from shipyard inventory."), ll) {

					@Override
					public void onDeactivate() {
					}

					@Override
					public boolean isOccluded() {
						return false;
					}

					@Override
					public void pressedOK(GUIListElement current) {
						yard.sendShipyardCommandToServer(((GameClientState) yard.getState()).getPlayer().getFactionId(), LOAD_DESIGN, current.getContent().getUserPointer());
						deactivate();
					}
				};
				p.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(917);
			}
		}, WaitingForShipyardOrder.class, Transition.SY_LOAD_DESIGN, Integer.class),
		DECONSTRUCT(en -> {
			return Lng.str("Deconstruct to Design");
		}, en -> {
			return Lng.str("Deconstructs currently docked Ship, and creates a design of it.\nAll resources of the ship will be placed here and in connected chests.\nIf there is no room, the rest will spawn at your position.");
		}, new ShipyardCommandDialog() {

			@Override
			public void clientSend(ShipyardCollectionManager yard) {
				PlayerTextInput pp = new PlayerTextInput("ASK", (GameClientState) yard.getState(), VirtualBlueprintMetaItem.MAX_LENGTH, Lng.str("Name"), Lng.str("Choose a name."), "D" + "_" + System.currentTimeMillis()) {

					@Override
					public String[] getCommandPrefixes() {
						return null;
					}

					@Override
					public boolean isOccluded() {
						return getState().getController().getPlayerInputs().indexOf(this) != getState().getController().getPlayerInputs().size() - 1;
					}

					@Override
					public String handleAutoComplete(String s, TextCallback callback, String prefix) {
						return s;
					}

					@Override
					public void onDeactivate() {
					}

					@Override
					public void onFailedTextCheck(String msg) {
						setErrorMessage(Lng.str("SHIPNAME INVALID: %s", msg));
					}

					@Override
					public boolean onInput(String entry) {
						yard.sendShipyardCommandToServer(((GameClientState) yard.getState()).getPlayer().getFactionId(), DECONSTRUCT, entry);
						return true;
					}
				};
				pp.setInputChecker(shipInputChecker);
				pp.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(918);
			}
		}, NormalShipLoaded.class, Transition.SY_DECONSTRUCT, String.class),
		DECONSTRUCT_RECYCLE(en -> {
			return Lng.str("Deconstruct without design");
		}, en -> {
			return Lng.str("Deconstructs currently docked Ship.\nAll resources of the ship will be placed here and in connected chests.\nIf there is no room, the rest will spawn at your position.\nWarning: this will not create a design!");
		}, new ShipyardCommandDialog() {

			@Override
			public void clientSend(ShipyardCollectionManager yard) {
				PlayerGameOkCancelInput p = new PlayerGameOkCancelInput("CONFIRM", (GameClientState) yard.getState(), Lng.str("Deconstruct without Design!"), Lng.str("Warning: this will disassemble your ship into its resources without creating a design!")) {

					@Override
					public boolean isOccluded() {
						return false;
					}

					@Override
					public void onDeactivate() {
					}

					@Override
					public void pressedOK() {
						yard.sendShipyardCommandToServer(((GameClientState) yard.getState()).getPlayer().getFactionId(), DECONSTRUCT_RECYCLE);
						deactivate();
					}
				};
				p.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(919);
			}
		}, NormalShipLoaded.class, Transition.SY_DECONSTRUCT_RECYCLE),
		SPAWN_DESIGN(en -> {
			return Lng.str("Construct current Design");
		}, en -> {
			return Lng.str("Collects the resources in here and in connected chests. When all resources for the design are provided, the ship will be constructed and will become yours to take.");
		}, new ShipyardCommandDialog() {

			@Override
			public void clientSend(ShipyardCollectionManager yard) {
				PlayerTextInput pp = new PlayerTextInput("ASK", (GameClientState) yard.getState(), VirtualBlueprintMetaItem.MAX_LENGTH, Lng.str("Name"), Lng.str("Choose a name."), "D" + "_" + System.currentTimeMillis()) {

					@Override
					public String[] getCommandPrefixes() {
						return null;
					}

					@Override
					public boolean isOccluded() {
						return getState().getController().getPlayerInputs().indexOf(this) != getState().getController().getPlayerInputs().size() - 1;
					}

					@Override
					public String handleAutoComplete(String s, TextCallback callback, String prefix) {
						return s;
					}

					@Override
					public void onDeactivate() {
					}

					@Override
					public void onFailedTextCheck(String msg) {
						setErrorMessage(Lng.str("SHIPNAME INVALID: %s", msg));
					}

					@Override
					public boolean onInput(String entry) {
						yard.sendShipyardCommandToServer(((GameClientState) yard.getState()).getPlayer().getFactionId(), SPAWN_DESIGN, entry);
						return true;
					}
				};
				pp.setInputChecker(shipInputChecker);
				pp.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(920);
			}
		}, DesignLoaded.class, Transition.SY_SPAWN_DESIGN, String.class),
		CATALOG_TO_DESIGN(en -> {
			return Lng.str("Create Design from Catalog");
		}, en -> {
			return Lng.str("Choose a design from the catalog.");
		}, new ShipyardCommandDialog() {

			@Override
			public void clientSend(ShipyardCollectionManager yard) {
				List<GUIElement> guiInventoryBlueprintList = yard.getCatalog();
				if(guiInventoryBlueprintList.isEmpty()) {
					((GameClientState) yard.getState()).getController().popupAlertTextMessage(Lng.str("No blueprints available in catalog!"), 0);
					return;
				}
				PlayerGameDropDownInput p = new PlayerGameDropDownInput("SELECT_BB", (GameClientState) yard.getState(), Lng.str("Blueprints"), 24, Lng.str("Choose a blueprint from the catalog."), guiInventoryBlueprintList) {

					@Override
					public void onDeactivate() {
					}

					@Override
					public boolean isOccluded() {
						return false;
					}

					@Override
					public void pressedOK(GUIListElement current) {
						PlayerTextInput pp = new PlayerTextInput("ASK", (GameClientState) yard.getState(), VirtualBlueprintMetaItem.MAX_LENGTH, Lng.str("Name"), Lng.str("Choose a name."), "D" + "_" + System.currentTimeMillis()) {

							@Override
							public String[] getCommandPrefixes() {
								return null;
							}

							@Override
							public boolean isOccluded() {
								return getState().getController().getPlayerInputs().indexOf(this) != getState().getController().getPlayerInputs().size() - 1;
							}

							@Override
							public String handleAutoComplete(String s, TextCallback callback, String prefix) {
								return s;
							}

							@Override
							public void onDeactivate() {
							}

							@Override
							public void onFailedTextCheck(String msg) {
								setErrorMessage(Lng.str("SHIPNAME INVALID: %s", msg));
							}

							@Override
							public boolean onInput(String entry) {
								yard.sendShipyardCommandToServer(((GameClientState) yard.getState()).getPlayer().getFactionId(), CATALOG_TO_DESIGN, entry, current.getContent().getUserPointer());
								return true;
							}
						};
						pp.setInputChecker(shipInputChecker);
						pp.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(921);
						deactivate();
					}
				};
				p.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(922);
			}
		}, WaitingForShipyardOrder.class, Transition.SY_CONVERT_BLUEPRINT_TO_DESIGN, String.class, String.class),
		BLUEPRINT_TO_DESIGN(en -> {
			return Lng.str("Create Design from Blueprint Item");
		}, en -> {
			return Lng.str("This will create and load a design of an existing Blueprint.");
		}, new ShipyardCommandDialog() {

			@Override
			public void clientSend(ShipyardCollectionManager yard) {
				List<GUIElement> guiInventoryBlueprintList = yard.getGUIInventoryBlueprintList();
				if(guiInventoryBlueprintList.isEmpty()) {
					((GameClientState) yard.getState()).getController().popupAlertTextMessage(Lng.str("No blueprints in shipyard inventory!"), 0);
					return;
				}
				PlayerGameDropDownInput p = new PlayerGameDropDownInput("SELECT_BB", (GameClientState) yard.getState(), Lng.str("Blueprints"), 24, Lng.str("Choose a blueprint from shipyard inventory."), guiInventoryBlueprintList) {

					@Override
					public void onDeactivate() {
					}

					@Override
					public boolean isOccluded() {
						return false;
					}

					@Override
					public void pressedOK(GUIListElement current) {
						PlayerTextInput pp = new PlayerTextInput("ASK", (GameClientState) yard.getState(), VirtualBlueprintMetaItem.MAX_LENGTH, Lng.str("Name"), Lng.str("Choose a name."), "D" + "_" + System.currentTimeMillis()) {

							@Override
							public String[] getCommandPrefixes() {
								return null;
							}

							@Override
							public boolean isOccluded() {
								return getState().getController().getPlayerInputs().indexOf(this) != getState().getController().getPlayerInputs().size() - 1;
							}

							@Override
							public String handleAutoComplete(String s, TextCallback callback, String prefix) {
								return s;
							}

							@Override
							public void onDeactivate() {
							}

							@Override
							public void onFailedTextCheck(String msg) {
								setErrorMessage(Lng.str("SHIPNAME INVALID: %s", msg));
							}

							@Override
							public boolean onInput(String entry) {
								yard.sendShipyardCommandToServer(((GameClientState) yard.getState()).getPlayer().getFactionId(), BLUEPRINT_TO_DESIGN, entry, current.getContent().getUserPointer());
								return true;
							}
						};
						pp.setInputChecker(shipInputChecker);
						pp.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(923);
						deactivate();
					}
				};
				p.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(924);
			}
		}, WaitingForShipyardOrder.class, Transition.SY_CONVERT_BLUEPRINT_TO_DESIGN, String.class, String.class),
		DESIGN_TO_BLUEPRINT(en -> {
			return Lng.str("Create Blueprint from Design");
		}, en -> {
			return Lng.str("This will create a Blueprint of the current loaded design");
		}, new ShipyardCommandDialog() {

			@Override
			public void clientSend(ShipyardCollectionManager yard) {
				BlueprintPlayerHandleRequest req = new BlueprintPlayerHandleRequest();
				PlayerTextInput pp = new PlayerTextInput("ASK", (GameClientState) yard.getState(), VirtualBlueprintMetaItem.MAX_LENGTH, Lng.str("Name"), Lng.str("Choose a name."), "D" + "_" + System.currentTimeMillis()) {

					@Override
					public String[] getCommandPrefixes() {
						return null;
					}

					@Override
					public boolean isOccluded() {
						return getState().getController().getPlayerInputs().indexOf(this) != getState().getController().getPlayerInputs().size() - 1;
					}

					@Override
					public String handleAutoComplete(String s, TextCallback callback, String prefix) {
						return s;
					}

					@Override
					public void onDeactivate() {
					}

					@Override
					public void onFailedTextCheck(String msg) {
						setErrorMessage(Lng.str("SHIPNAME INVALID: %s", msg));
					}

					@Override
					public boolean onInput(String entry) {
						yard.sendShipyardCommandToServer(((GameClientState) yard.getState()).getPlayer().getFactionId(), DESIGN_TO_BLUEPRINT, entry, ((GameClientState) getState()).getPlayer().getId(), req.classification.ordinal());
						return true;
					}
				};
				pp.getInputPanel().onInit();
				GUIElement[] guiElements = BlueprintClassification.getGUIElements((GameClientState) yard.getState(), SimpleTransformableSendableObject.EntityType.SHIP);
				req.classification = (BlueprintClassification) guiElements[0].getUserPointer();
				GUIDropDownList catList = new GUIDropDownList((GameClientState) yard.getState(), 300, 24, 200, element -> req.classification = (BlueprintClassification) element.getContent().getUserPointer(), guiElements);
				catList.setPos(4, 30, 0);
				((GUIDialogWindow) pp.getInputPanel().getBackground()).getMainContentPane().getContent(0).attach(catList);
				pp.setInputChecker(shipInputChecker);
				pp.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(925);
			}
		}, DesignLoaded.class, Transition.SY_CONVERT_TO_BLUEPRINT, String.class, Integer.class, Integer.class),
		TEST_DESIGN(en -> {
			return Lng.str("Test out Design");
		}, en -> {
			return Lng.str("Take your current design for a test flight in a virtual sector.\nSpawn in enemy waves or shoot at some static targets.");
		}, new ShipyardCommandDialog() {

			@Override
			public void clientSend(ShipyardCollectionManager yard) {
				yard.sendShipyardCommandToServer(((GameClientState) yard.getState()).getPlayer().getFactionId(), TEST_DESIGN, ((GameClientState) yard.getState()).getPlayer().getId());
			}
		}, DesignLoaded.class, Transition.SY_TEST_DESIGN, Integer.class),
		REPAIR_FROM_DESIGN(en -> {
			return Lng.str("Repair Docked from Design");
		}, en -> {
			return Lng.str("Restore the current docked ship to a design.");
		}, new ShipyardCommandDialog() {

			@Override
			public void clientSend(ShipyardCollectionManager yard) {
				List<GUIElement> ll = yard.getGUIInventoryDesignList();
				if(ll.isEmpty()) {
					((GameClientState) yard.getState()).getController().popupAlertTextMessage(Lng.str("No designs in shipyard inventory!"), 0);
					return;
				}
				PlayerGameDropDownInput p = new PlayerGameDropDownInput("SELECT_DESIGN", (GameClientState) yard.getState(), Lng.str("Designs"), 24, Lng.str("Choose a Design from shipyard inventory."), ll) {

					@Override
					public void onDeactivate() {
					}

					@Override
					public boolean isOccluded() {
						return false;
					}

					@Override
					public void pressedOK(GUIListElement current) {
						yard.sendShipyardCommandToServer(((GameClientState) yard.getState()).getPlayer().getFactionId(), REPAIR_FROM_DESIGN, current.getContent().getUserPointer());
						deactivate();
					}
				};
				p.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(926);
			}
		}, NormalShipLoaded.class, Transition.SY_REPAIR_TO_DESIGN, Integer.class);

		public final Class<?>[] args;
		public final Transition transition;
		private final Translatable name;
		private final Translatable description;
		private final ShipyardCommandDialog c;

		private final Class<? extends ShipyardState> requiredState;

		ShipyardCommandType(Translatable name, Translatable description, ShipyardCommandDialog c, Class<? extends ShipyardState> requiredState, Transition transition, Class<?>... args) {
			this.name = name;
			this.args = args;
			this.description = description;
			this.c = c;
			this.requiredState = requiredState;
			this.transition = transition;
		}

		public String getName() {
			return name.getName(this);
		}

		public String getDescription() {
			return description.getName(this);
		}


		public void checkMatches(Object[] to) {
			if(args.length != to.length) {
				throw new IllegalArgumentException("Invalid argument count: Provided: " + Arrays.toString(to) + ", but needs: " + Arrays.toString(args));
			}
			for(int i = 0; i < args.length; i++) {
				if(!to[i].getClass().equals(args[i])) {
					System.err.println("Not Equal: " + to[i] + " and " + args[i]);
					throw new IllegalArgumentException("Invalid argument on index " + i + ": Provided: " + Arrays.toString(to) + "; cannot take " + to[i] + ":" + to[i].getClass() + ", it has to be type: " + args[i].getClass());
				}
			}
		}

		public void addTile(PlayerButtonTilesInput a, ShipyardCollectionManager yard) {
			GUIActivationCallback ac = new GUIActivationCallback() {

				@Override
				public boolean isVisible(InputState state) {
					return true;
				}

				@Override
				public boolean isActive(InputState state) {
					return yard.isCommandUsable(ShipyardCommandType.this);
				}
			};
			a.addTile(getName(), getDescription(), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !(a.isActive() && (ac == null || ac.isActive((ClientState) yard.getState())));
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(927);
						c.clientSend(yard);
						a.deactivate();
						if(ShipyardCommandType.this == TEST_DESIGN) {
							((GameClientState) callingGuiElement.getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().inventoryAction(null);
						}
					}
				}
			}, ac);
		}
	}

	public interface ShipyardCommandDialog {

		void clientSend(ShipyardCollectionManager yard);
	}
}
