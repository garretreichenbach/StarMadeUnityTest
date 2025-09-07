package org.schema.game.common.controller.elements;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.vecmath.Vector3f;

import api.listener.events.systems.UsableElementManagerInstantiateEvent;
import api.mod.StarLoader;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.schema.common.CallInterace;
import org.schema.common.LogUtil;
import org.schema.common.config.*;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateControllerInterface;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.common.LibLoader;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.projectile.ProjectileController;
import org.schema.game.common.controller.elements.combination.Combinable;
import org.schema.game.common.controller.elements.config.ReactorDualConfigElement;
import org.schema.game.common.controller.elements.effectblock.EffectElementManager;
import org.schema.game.common.controller.elements.effectblock.EffectElementManager.OffensiveEffects;
import org.schema.game.common.controller.elements.power.PowerAddOn;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;
import org.schema.game.common.controller.observer.DrawerObservable;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.BlockEffectTypes;
import org.schema.game.common.data.element.ControlElementMap;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.ShipConfigurationNotFoundException;
import org.schema.game.server.controller.GameServerController;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.container.PhysicsDataContainer;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.vecmath.Vector3f;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public abstract class UsableElementManager<E extends ElementCollection<E, CM, EM>, CM extends ElementCollectionManager<E, CM, EM>, EM extends UsableElementManager<E, CM, EM>> extends DrawerObservable {
	public static ObjectOpenHashSet<Class<?>> initializedServer = new ObjectOpenHashSet<Class<?>>();
	public static ObjectOpenHashSet<Class<?>> initializedClient = new ObjectOpenHashSet<Class<?>>();
	private final SegmentController segmentController;
	public long nextShot;
	public int totalSize;
	
	long lastEnqueue;
	public double lowestIntegrity = Double.POSITIVE_INFINITY;
	
	private boolean updatable;
	private boolean explosiveStructure;

	public UsableElementManager(SegmentController segmentController) {
		this.segmentController = segmentController;
		//INSERTED CODE @???
		UsableElementManagerInstantiateEvent event = new UsableElementManagerInstantiateEvent(this, segmentController);
		StarLoader.fireEvent(event, isOnServer());
		///

	}
	public boolean isOnServer() {
		return segmentController.isOnServer();
	}
	public abstract void onElementCollectionsChanged();
	public boolean isAddToPlayerUsable() {
		return true;
	}
	public boolean isCombinable() {
		return this instanceof Combinable && ((Combinable<?,?,?,?>)this).getAddOn() != null;
	}
	public void init(ManagerContainer container){
	}
	
	public void onPlayerDetachedFromThisOrADock(ManagedUsableSegmentController<?> originalCaller, PlayerState pState,
			PlayerControllable newAttached){
		
	}
	public double calculateReload( E u) {
		return 0d;
	}
	public abstract void flagCheckUpdatable();
	public boolean canConsumePower(float powerConsumed) {
		if (powerConsumed <= 0) {
			return true;
		}
		if (getPowerManager().canConsumePowerInstantly(powerConsumed)) {
			return true;
		}
		if (segmentController.getDockingController().getDockedOn() != null) {
			SegmentController segmentController = this.segmentController.getDockingController().getDockedOn().to.getSegment().getSegmentController();
			if (segmentController instanceof ManagedSegmentController && ((ManagedSegmentController<?>) segmentController).getManagerContainer() instanceof PowerManagerInterface) {
				PowerManagerInterface p = (PowerManagerInterface) ((ManagedSegmentController<?>) segmentController).getManagerContainer();
				return p.getPowerAddOn().canConsumePowerInstantly(powerConsumed);
			}
		}
		return false;
	}

	public double getPower() {
		if (segmentController.getDockingController().getDockedOn() != null) {
			SegmentController segmentController = this.segmentController.getDockingController().getDockedOn().to.getSegment().getSegmentController();
			if (segmentController instanceof ManagedSegmentController && ((ManagedSegmentController<?>) segmentController).getManagerContainer() instanceof PowerManagerInterface) {
				PowerManagerInterface p = (PowerManagerInterface) ((ManagedSegmentController<?>) segmentController).getManagerContainer();
				return p.getPowerAddOn().getPower();
			}
		}
		return 0;
	}
	public boolean isUsingPowerReactors(){
		return getManagerContainer().getPowerInterface().isUsingPowerReactors();
	}
	public boolean consumePower(float powerConsumed) {
		if (powerConsumed <= 0 || getPowerManager().consumePowerInstantly(powerConsumed)) {
			return true;
		}
		if (segmentController.getDockingController().isDocked()) {
			SegmentController segmentController = this.segmentController.getDockingController().getDockedOn().to.getSegment().getSegmentController();
			if (segmentController instanceof ManagedSegmentController && ((ManagedSegmentController<?>) segmentController).getManagerContainer() instanceof PowerManagerInterface) {
				PowerManagerInterface p = (PowerManagerInterface) ((ManagedSegmentController<?>) segmentController).getManagerContainer();
				return p.getPowerAddOn().consumePowerInstantly(powerConsumed);
			}
		}
		return false;
	}

	public abstract ControllerManagerGUI getGUIUnitValues(E firingUnit, CM col, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol); //,

	public void printTags() {
		Field[] fields = getClass().getDeclaredFields();
		for (Field f : fields) {
			ConfigurationElement annotation = f.getAnnotation(ConfigurationElement.class);
			if (annotation != null) {
				try {
					System.out.println("<" + annotation.name() + ">" + f.get(this).toString() + "</" + annotation.name() + ">");
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public EffectElementManager<?, ?, ?> getEffect(short effectType) {
		return ((EffectManagerContainer) getManagerContainer()).getEffect(effectType);
	}

	public boolean canHandle(ControllerStateInterface unit) {
		return !segmentController.isVirtualBlueprint() && (unit.isDown(KeyboardMappings.SHIP_PRIMARY_FIRE) || unit.isDown(KeyboardMappings.SHIP_ZOOM));
	}

	public SlotAssignment checkShipConfig(ControllerStateInterface unit) throws ShipConfigurationNotFoundException {
		if (unit.getPlayerState() == null) {
			return null;
		}

//		getSegmentController().getSlotAssignment().reassignIfNotExists(unit.getParameter());

		return segmentController.getSlotAssignment();
	}

	protected abstract String getTag();

	public void parse(Document config) throws IllegalArgumentException, IllegalAccessException, ConfigParserException {
		parse(config, segmentController.isOnServer(), this, this.getClass());
	}
	public static void parseTest(){
		try {
			LogUtil.setUp(20, () -> {
			});
			ElementKeyMap.initializeData(null);
			LibLoader.loadNativeLibs(true, -1, false);
			BlockShapeAlgorithm.initialize();
			GameServerState state = new GameServerState();
			state.setController(new GameServerController(state));
			state.udpateTime = System.currentTimeMillis(); 
			((GameStateControllerInterface) state.getController()).parseBlockBehavior("./data/config/blockBehaviorConfig.xml");
			Ship p = new Ship(state);
			
			p.getManagerContainer().reparseBlockBehavior(true);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public static void parse(Document config, boolean onServer, UsableElementManager<?,?,?> obj, Class<? extends UsableElementManager> clazz) throws IllegalArgumentException, IllegalAccessException, ConfigParserException {
		assert(config != null);
		synchronized(initializedServer){
		if ((onServer && !initializedServer.contains(clazz)) ||
				(!onServer && !initializedClient.contains(clazz))) {
			org.w3c.dom.Element root = config.getDocumentElement();
			NodeList childNodesTop = root.getChildNodes();

			Field[] fields = clazz.getDeclaredFields();

			Object2ObjectOpenHashMap<String, Field> annoMap = new Object2ObjectOpenHashMap<String, Field>();
			for (Field f : fields) {
				ConfigurationElement annotation = f.getAnnotation(ConfigurationElement.class);
				if(annotation != null) {
					annoMap.put(annotation.name().toLowerCase(Locale.ENGLISH), f);
				}
			}
			
			boolean foundTop = false;
			ObjectOpenHashSet<Field> loaded = new ObjectOpenHashSet<Field>();
			for (int j = 0; j < childNodesTop.getLength(); j++) {
				Node itemTop = childNodesTop.item(j);

				if (itemTop.getNodeType() == Node.ELEMENT_NODE && itemTop.getNodeName().toLowerCase(Locale.ENGLISH).equals(obj.getTag())) {
					NodeList childNodesIn = itemTop.getChildNodes();
					foundTop = true;
					for (int k = 0; k < childNodesIn.getLength(); k++) {
						Node itemIn = childNodesIn.item(k);
						if (itemIn.getNodeType() == Node.ELEMENT_NODE) {

							if (itemIn.getNodeName().toLowerCase(Locale.ENGLISH).equals("basicvalues")) {
								NodeList childNodes = itemIn.getChildNodes();
								for (int i = 0; i < childNodes.getLength(); i++) {
									Node item = childNodes.item(i);
									if (item.getNodeType() == Node.ELEMENT_NODE) {
										Node versionNode = item.getAttributes().getNamedItem("version");
										boolean hasVersion = versionNode != null;
										
//										System.err.println("LOKKING "+item.getNodeName().toLowerCase(Locale.ENGLISH));
										Field f = annoMap.get(item.getNodeName().toLowerCase(Locale.ENGLISH));
										if (f != null) {
											f.setAccessible(true);
											Object toSet = obj;
											
//											System.err.println("DOUNF "+f.getName());
											try {
												if (f.getType() == Boolean.TYPE) {
													assert(!hasVersion):f.getName()+" of "+clazz+" is versioned and can't be primitive ("+clazz.getSimpleName()+".java:0)";
													f.setBoolean(toSet, Boolean.parseBoolean(item.getTextContent()));
												} else if (f.getType() == Integer.TYPE) {
													assert(!hasVersion):f.getName()+" of "+clazz+" is versioned and can't be primitive ("+clazz.getSimpleName()+".java:0)";
													f.setInt(toSet, Integer.parseInt(item.getTextContent()));
												} else if (f.getType() == Short.TYPE) {
													assert(!hasVersion):f.getName()+" of "+clazz+" is versioned and can't be primitive ("+clazz.getSimpleName()+".java:0)";
													f.setShort(toSet, Short.parseShort(item.getTextContent()));
												} else if (f.getType() == Byte.TYPE) {
													assert(!hasVersion):f.getName()+" of "+clazz+" is versioned and can't be primitive ("+clazz.getSimpleName()+".java:0)";
													f.setByte(toSet, Byte.parseByte(item.getTextContent()));
												} else if (f.getType() == Float.TYPE) {
													assert(!hasVersion):f.getName()+" of "+clazz+" is versioned and can't be primitive ("+clazz.getSimpleName()+".java:0)";
													f.setFloat(toSet, Float.parseFloat(item.getTextContent()));
												} else if (f.getType() == Double.TYPE) {
													assert(!hasVersion):f.getName()+" of "+clazz+" is versioned and can't be primitive ("+clazz.getSimpleName()+".java:0)";
													f.setDouble(toSet, Double.parseDouble(item.getTextContent()));
												} else if (f.getType() == Long.TYPE) {
													assert(!hasVersion):f.getName()+" of "+clazz+" is versioned and can't be primitive ("+clazz.getSimpleName()+".java:0)";
													f.setLong(toSet, Long.parseLong(item.getTextContent()));
												} else {
													if(InterEffectSet.class.equals(f.getType())) {
														((InterEffectSet)f.get(toSet)).parseXML(item);
													}else if(ReactorDualConfigElement.class.isAssignableFrom(f.getType())){
														boolean oldpower = hasVersion && versionNode.getNodeValue().toLowerCase(Locale.ENGLISH).equals("noreactor");
														int index = ReactorDualConfigElement.getIndex(!oldpower);
														if(IntMultiConfigField.class.isAssignableFrom(f.getType())){
															((IntMultiConfigField)f.get(toSet)).set(index, Integer.parseInt(item.getTextContent()));
														}else if(FloatMultiConfigField.class.isAssignableFrom(f.getType())){
															((FloatMultiConfigField)f.get(toSet)).set(index, Float.parseFloat(item.getTextContent()));
														}else if(DoubleMultiConfigField.class.isAssignableFrom(f.getType())){
															((DoubleMultiConfigField)f.get(toSet)).set(index, Double.parseDouble(item.getTextContent()));
														}else{
															throw new ConfigParserException("Unknown type: "+f.getType()+" Cannot parse field: " + f.getName() + "; " + f.getType() + "; with " + item.getTextContent());
														}
													}else if (f.getType().equals(HpConditionList.class)) {
														((HpConditionList)f.get(toSet)).parse(item);

													} else if (f.getType().equals(BlockEffectTypes.class)) {
														BlockEffectTypes valueOf = BlockEffectTypes.valueOf(item.getTextContent());
														if (valueOf == null) {
															throw new ConfigParserException("Cannot parse enum field: " + itemTop.getNodeName() + "-> " + itemIn.getNodeName() + " -> " + item.getNodeName() + ": " + f.getName() + "; " + f.getType() + "; enum unkown (possible: " + Arrays.toString(BlockEffectTypes.values()) + ")");
																} else {
																	f.set(toSet, valueOf);
																}

															} else if (f.getType().isEnum()) {
//														System.err.println("NM: "+f.getType().getSimpleName());
														assert(!hasVersion);
														try {
															Method method = f.getType().getMethod("valueOf", String.class);
															
															Enum e = (Enum) method.invoke(null, item.getTextContent());
															if(e != null) {
																f.set(toSet, e);
															}else {
																Enum[] possile = (Enum[]) f.getType().getMethod("values").invoke(null);
																throw new ConfigParserException("Cannot parse enum field: " + itemTop.getNodeName() + "-> " + itemIn.getNodeName() + " -> " + item.getNodeName() + ": " + f.getName() + "; " + f.getType() + "; enum unkown (possible: " + Arrays.toString(possile) + ")");
															}
//																	UnitCalcStyle valueOf = UnitCalcStyle.valueOf(item.getTextContent());
//																	if (valueOf == null) {
//																		throw new ConfigParserException("Cannot parse enum field: " + itemTop.getNodeName() + "-> " + itemIn.getNodeName() + " -> " + item.getNodeName() + ": " + f.getName() + "; " + f.getType() + "; enum unkown (possible: " + Arrays.toString(UnitCalcStyle.values()) + ")");
//																	} else {
//																		f.set(toSet, valueOf);
//																		found = true;
//																	}
															
														} catch (NoSuchMethodException e) {
															e.printStackTrace();
														} catch (SecurityException e) {
															e.printStackTrace();
														} catch (InvocationTargetException e) {
															e.printStackTrace();
														} catch (DOMException e) {
															e.printStackTrace();
														}
														
														

													} else if (f.getType().equals(String.class)) {
														assert(!hasVersion):f.getName()+" of "+clazz+" is versioned and can't be primitive";
														String desc = item.getTextContent();
														desc = desc.replaceAll("\\r\\n|\\r|\\n", "");
														desc = desc.replaceAll("\\\\n", "\n");
														desc = desc.replaceAll("\\\\r", "\r");
														desc = desc.replaceAll("\\\\t", "\t");
														f.set(toSet, desc);
													} else {
														throw new ConfigParserException("Cannot parse field: " + f.getName() + "; " + f.getType());
													}
												}
											} catch (NumberFormatException e) {
												throw new ConfigParserException("Cannot parse field: " + f.getName() + "; " + f.getType() + "; with '" + item.getTextContent()+"'", e);
											}
											loaded.add(f);
										}else {
											throw new ConfigParserException(itemTop.getNodeName() + "-> " + itemIn.getNodeName() + " -> " + item.getNodeName() + ": No appropriate field found for tag: " + item.getNodeName());
										}
									}
								}
							} else if (itemIn.getNodeName().toLowerCase(Locale.ENGLISH).equals("combination")) {
								if (obj instanceof Combinable) {
									((Combinable<?, ?, ?, ?>) obj).getAddOn().parse(itemIn);
								} else {
									throw new ConfigParserException(itemTop.getNodeName() + " -> " + itemIn.getNodeName() + " class is not combinable " + obj + ", but has combinable tag");
								}
							} else {
								throw new ConfigParserException("tag \"" + itemTop.getNodeName() + " -> " + itemIn.getNodeName() + "\" unknown in this context (has to be either \"BasicValues\" or \"Combinable\")");
							}
						}
					}

				}
			}

			if (!foundTop) {
				throw new ConfigParserException("Block module Tag \"" + obj.getTag() + "\" not found in block behavior configuation. Please create it (case insensitive)");
			}
			Annotation[] annotations = clazz.getAnnotations();
			for (Field f : fields) {
				f.setAccessible(true);
				ConfigurationElement annotation = f.getAnnotation(ConfigurationElement.class);

				if (annotation != null && !loaded.contains(f)) {
					throw new ConfigParserException("virtual field " + f.getName() + " (" + annotation.name() + ") not found. Please define a tag \"" + annotation.name() + "\" inside the <BasicValues> of \"" + obj.getTag() + "\"");
				}
			}

			if (onServer) {
				initializedServer.add(clazz);
			} else {
				initializedClient.add(clazz);
			}
		} else {
			if (onServer) {
				assert (initializedServer.size() > 0);
			} else {
				assert (initializedClient.size() > 0);
			}
		}
		}

	}

	public boolean hasAnyBlock() {
		return totalSize > 0;
	}

	protected boolean clientIsOwnShip() {
		return ((PlayerControllable) segmentController).isClientOwnObject();
	}

	protected List<PlayerState> getAttachedPlayers() {
		return ((ManagedUsableSegmentController<?>) segmentController).getAttachedPlayers();
	}

	public ControlElementMap getControlElementMap() {
		return segmentController.getControlElementMap();
	}

	/**
	 * @return the managerContainer
	 */
	public ManagerContainer<? extends SegmentController> getManagerContainer() {
		return ((ManagedSegmentController<?>) segmentController).getManagerContainer();
	}

	public abstract CM getNewCollectionManager(SegmentPiece position, Class<CM> clazz);

	public abstract String getManagerName();

	public GUIKeyValueEntry[] getGUIElementCollectionValues() {
		return new GUIKeyValueEntry[0];
	}

	protected PhysicsDataContainer getPhysicsDataContainer() {
		return segmentController.getPhysicsDataContainer();
	}

	@Deprecated //old power
	protected PowerAddOn getPowerManager() {
		return ((PowerManagerInterface) getManagerContainer()).getPowerAddOn();
	}

	public ProjectileController getParticleController() {
		return ((EditableSendableSegmentController) segmentController).getParticleController();
	}

	public PulseController getPulseController() {
		return ((PulseHandler) segmentController).getPulseController();
	}

	protected SegmentBufferInterface getSegmentBuffer() {
		return segmentController.getSegmentBuffer();
	}

	public void notifyShooting(E c) {
		this.notifyObservers(c, "s");
	}


	public void handleResponse(ShootingRespose handled, E fireingUnit, Vector3f outputWorldPos) {
		switch (handled) {
			case FIRED:
				if (!segmentController.isOnServer()) {
					Transform t = new Transform();
					t.setIdentity();
					t.origin.set(outputWorldPos);
				}
				notifyShooting(fireingUnit);
				getManagerContainer().onAction();
				break;
			case RELOADING:
				break;
			case CHARGING:
				break;
			case INITIALIZING:
				if (segmentController.isClientOwnObject()) {
					((GameClientState) segmentController.getState()).getController().popupAlertTextMessage(Lng.str("Cannot shoot!\nInitializing weapons..."), 0);
				}
				break;
			case INVALID_COMBI:
				if (segmentController.isClientOwnObject()) {
					((GameClientState) segmentController.getState()).getController().popupAlertTextMessage(Lng.str("Cannot shoot\nError in combination\nreason:\nInvalid combination"), 0);
				}
				break;
			case NO_COMBINATION:
				if (segmentController.isClientOwnObject()) {
					((GameClientState) segmentController.getState()).getController().popupAlertTextMessage(Lng.str("Cannot shoot\nError in combination\nreason:\nNo combination"), 0);
				}
				break;
			case NO_POWER:
				if (segmentController.isClientOwnObject()) {
					if (((GameClientState) getState()).getWorldDrawer() != null) {
						((GameClientState) getState()).getWorldDrawer().getGuiDrawer().notifyEffectHit(this.segmentController, OffensiveEffects.NO_POWER);
					}
				}
				break;
			default:
				break;
		}

	}

	/**
	 * @return the segmentController
	 */
	public SegmentController getSegmentController() {
		return segmentController;
	}

	public StateInterface getState() {
		return segmentController.getState();
	}

	protected Transform getWorldTransform() {
		return segmentController.getWorldTransform();
	}

	public abstract void handle(ControllerStateInterface unit, Timer timer);

	public int updatePrio(){
		return 0;
	}
	
	public void setUpdatable(boolean updatable) {
		if(!this.updatable && updatable) {
			getManagerContainer().flagUpdatableCheckFor(this);
		}else if(this.updatable && !updatable) {
			getManagerContainer().flagUpdatableCheckFor(this);
		}
		this.updatable = updatable;
	}
	public boolean isUpdatable() {
		return updatable;
	}
	public void setExplosiveStructure(boolean b) {
		explosiveStructure = b;
	}
	public boolean isExplosiveStructure() {
		return explosiveStructure;
	}
}


