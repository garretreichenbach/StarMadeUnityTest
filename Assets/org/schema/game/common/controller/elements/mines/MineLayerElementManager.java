package org.schema.game.common.controller.elements.mines;

import java.util.Arrays;
import java.util.List;

import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.MineInterface;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.ManagerReloadInterface;
import org.schema.game.common.controller.elements.ShootingRespose;
import org.schema.game.common.controller.elements.UsableControllableElementManager;
import org.schema.game.common.controller.elements.UsableControllableFiringElementManager;
import org.schema.game.common.controller.elements.UsableControllableFiringElementManager.DrawReloadListener;
import org.schema.game.common.controller.elements.UsableControllableFiringElementManager.PrintReloadListener;
import org.schema.game.common.controller.elements.UsableControllableFiringElementManager.ReloadListener;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.ShootContainer;
import org.schema.game.common.data.mines.Mine;
import org.schema.game.common.data.mines.Mine.MineDataException;
import org.schema.game.common.data.mines.Mine.MineSettings;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.util.FastCopyLongOpenHashSet;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.InputState;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.Short2IntMap.Entry;
import it.unimi.dsi.fastutil.shorts.Short2IntMap.FastEntrySet;
import it.unimi.dsi.fastutil.shorts.Short2IntOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

public class MineLayerElementManager extends UsableControllableElementManager<MineLayerUnit, MineLayerCollectionManager, MineLayerElementManager> implements ManagerReloadInterface {

	@ConfigurationElement(name = "ReactorPowerNeededPerBlock")
	public static double REACTOR_POWER_CONST_NEEDED_PER_BLOCK = 1;

	@ConfigurationElement(name = "ReactorPowerNeededPow")
	public static double REACTOR_POWER_POW = 1;

	@ConfigurationElement(name = "LayableMineDistance")
	public static float LAYABLE_MINE_DISTANCE = 100;

	@ConfigurationElement(name = "CannonDamage")
	public static float CANNON_DAMAGE = 100;

	@ConfigurationElement(name = "MissileDamage")
	public static float MISSILE_DAMAGE = 100;

	@ConfigurationElement(name = "ProximityDamage")
	public static float PROXMITY_DAMAGE = 100;

	@ConfigurationElement(name = "CannonDamage1")
	public static float CANNON_DAMAGE_1 = 100;

	@ConfigurationElement(name = "CannonDamage2")
	public static float CANNON_DAMAGE_2 = 100;

	@ConfigurationElement(name = "CannonDamage3")
	public static float CANNON_DAMAGE_3 = 100;

	@ConfigurationElement(name = "CannonDamage4")
	public static float CANNON_DAMAGE_4 = 100;

	@ConfigurationElement(name = "CannonDamage5")
	public static float CANNON_DAMAGE_5 = 100;

	@ConfigurationElement(name = "CannonDamage6")
	public static float CANNON_DAMAGE_6 = 100;

	@ConfigurationElement(name = "MissileDamage1")
	public static float MISSILE_DAMAGE_1 = 100;

	@ConfigurationElement(name = "MissileDamage2")
	public static float MISSILE_DAMAGE_2 = 100;

	@ConfigurationElement(name = "MissileDamage3")
	public static float MISSILE_DAMAGE_3 = 100;

	@ConfigurationElement(name = "MissileDamage4")
	public static float MISSILE_DAMAGE_4 = 100;

	@ConfigurationElement(name = "MissileDamage5")
	public static float MISSILE_DAMAGE_5 = 100;

	@ConfigurationElement(name = "MissileDamage6")
	public static float MISSILE_DAMAGE_6 = 100;

	@ConfigurationElement(name = "ProximityDamage1")
	public static float PROXIMITY_DAMAGE_1 = 100;

	@ConfigurationElement(name = "ProximityDamage2")
	public static float PROXIMITY_DAMAGE_2 = 100;

	@ConfigurationElement(name = "ProximityDamage3")
	public static float PROXIMITY_DAMAGE_3 = 100;

	@ConfigurationElement(name = "ProximityDamage4")
	public static float PROXIMITY_DAMAGE_4 = 100;

	@ConfigurationElement(name = "ProximityDamage5")
	public static float PROXIMITY_DAMAGE_5 = 100;

	@ConfigurationElement(name = "ProximityDamage6")
	public static float PROXIMITY_DAMAGE_6 = 100;

	@ConfigurationElement(name = "BlockConsumption1")
	public static int BLOCK_CONSUMPTION_1 = 100;

	@ConfigurationElement(name = "BlockConsumption2")
	public static int BLOCK_CONSUMPTION_2 = 100;

	@ConfigurationElement(name = "BlockConsumption3")
	public static int BLOCK_CONSUMPTION_3 = 100;

	@ConfigurationElement(name = "BlockConsumption4")
	public static int BLOCK_CONSUMPTION_4 = 100;

	@ConfigurationElement(name = "BlockConsumption5")
	public static int BLOCK_CONSUMPTION_5 = 100;

	@ConfigurationElement(name = "BlockConsumption6")
	public static int BLOCK_CONSUMPTION_6 = 100;

	@ConfigurationElement(name = "CannonReloadInSec")
	public static float CANNON_RELOAD_SEC = 100;

	@ConfigurationElement(name = "CannonSpeed")
	public static float CANNON_SPEED = 100;

	@ConfigurationElement(name = "CannonShotAtTargetCount")
	public static int CANNON_SHOOT_AT_TARGET_COUNT = 1;

	@ConfigurationElement(name = "MissileSpeed")
	public static float MISSILE_SPEED = 100;

	@ConfigurationElement(name = "MissileReloadInSec")
	public static float MISSILE_RELOAD_SEC = 100;

	@ConfigurationElement(name = "ProximitySpeed")
	public static float PROXIMITY_SPEED = 100;

	@ConfigurationElement(name = "CannonAmmo")
	public static int CANNON_AMMO = 100;

	@ConfigurationElement(name = "MissileAmmo")
	public static int MISSILE_AMMO = 100;

	public static int getBlockConsumption(int strength) {
		strength = Math.min(6, Math.max(1, strength));
		switch(strength) {
			case 1:
				return BLOCK_CONSUMPTION_1;
			case 2:
				return BLOCK_CONSUMPTION_2;
			case 3:
				return BLOCK_CONSUMPTION_3;
			case 4:
				return BLOCK_CONSUMPTION_4;
			case 5:
				return BLOCK_CONSUMPTION_5;
			case 6:
				return BLOCK_CONSUMPTION_6;
		}
		throw new RuntimeException("Illegal mine strength: " + strength);
	}

	public static float getMissileDamageMult(int strength) {
		strength = Math.min(6, Math.max(1, strength));
		switch(strength) {
			case 1:
				return MISSILE_DAMAGE_1;
			case 2:
				return MISSILE_DAMAGE_2;
			case 3:
				return MISSILE_DAMAGE_3;
			case 4:
				return MISSILE_DAMAGE_4;
			case 5:
				return MISSILE_DAMAGE_5;
			case 6:
				return MISSILE_DAMAGE_6;
		}
		throw new RuntimeException("Illegal mine strength: " + strength);
	}

	public static float getCannonDamageMult(int strength) {
		strength = Math.min(6, Math.max(1, strength));
		switch(strength) {
			case 1:
				return CANNON_DAMAGE_1;
			case 2:
				return CANNON_DAMAGE_2;
			case 3:
				return CANNON_DAMAGE_3;
			case 4:
				return CANNON_DAMAGE_4;
			case 5:
				return CANNON_DAMAGE_5;
			case 6:
				return CANNON_DAMAGE_6;
		}
		throw new RuntimeException("Illegal mine strength: " + strength);
	}

	public static float getProximityDamageMult(int strength) {
		strength = Math.min(6, Math.max(1, strength));
		switch(strength) {
			case 1:
				return PROXIMITY_DAMAGE_1;
			case 2:
				return PROXIMITY_DAMAGE_2;
			case 3:
				return PROXIMITY_DAMAGE_3;
			case 4:
				return PROXIMITY_DAMAGE_4;
			case 5:
				return PROXIMITY_DAMAGE_5;
			case 6:
				return PROXIMITY_DAMAGE_6;
		}
		throw new RuntimeException("Illegal mine strength: " + strength);
	}

	public static boolean DEBUG_MODE;

	public MineLayerElementManager(final SegmentController segmentController) {
		super(ElementKeyMap.MINE_LAYER, ElementKeyMap.MINE_CORE, segmentController);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.missile.MissileElementManager#getGUIUnitValues(org.schema.game.common.controller.elements.missile.MissileUnit, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager)
	 */
	@Override
	public ControllerManagerGUI getGUIUnitValues(MineLayerUnit firingUnit, MineLayerCollectionManager col, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return ControllerManagerGUI.create((GameClientState) getState(), Lng.str("Mine Layer Unit"), firingUnit);
	}

	private Object2ObjectOpenHashMap<Inventory, IntOpenHashSet> connectedInventories = new Object2ObjectOpenHashMap<Inventory, IntOpenHashSet>();

	private Short2IntOpenHashMap consTmp = new Short2IntOpenHashMap();

	private Short2IntOpenHashMap haveTmp = new Short2IntOpenHashMap();

	public void layMine(MineLayerUnit mineLayerUnit, MineLayerCollectionManager elementCollectionManager, int armIn, ShootContainer shootContainer) {
		if (mineLayerUnit.canUse(getState().getUpdateTime(), false)) {
			if (!((MineInterface) getState().getController()).getMineController().getMinesInRange(getSegmentController().getSectorId(), shootContainer.weapontOutputWorldPos, MineLayerElementManager.LAYABLE_MINE_DISTANCE, new ObjectArrayList<Mine>()).isEmpty()) {
				getSegmentController().popupOwnClientMessage(Lng.str("Mines have to be placed at least %s meters apart to not interfere with each other.", String.valueOf((int) MineLayerElementManager.LAYABLE_MINE_DISTANCE)), ServerMessage.MESSAGE_TYPE_ERROR);
				return;
			}
			short[] composition = mineLayerUnit.calcComposition();
			boolean hasType = false;
			for (short s : composition) {
				if (ElementKeyMap.isValidType(s) && ElementKeyMap.getInfoFast(s).isMineType()) {
					hasType = true;
					break;
				}
			}
			if (!hasType) {
				getSegmentController().popupOwnClientMessage(Lng.str("Can't construct mine without type.\nPlace a mine type block next to the mine core to set its type."), ServerMessage.MESSAGE_TYPE_ERROR);
				return;
			}
			System.err.println("[MINE] " + getSegmentController().getState() + " " + getSegmentController() + " MINE COMPOSITION: " + Arrays.toString(composition) + "; " + ElementCollection.getPosFromIndex(mineLayerUnit.getNeighboringCollection().getLong(0), new Vector3i()));
			boolean consumed = consumeMineResources(composition, mineLayerUnit, elementCollectionManager, shootContainer);
			if (!consumed) {
				return;
			}
			if (getSegmentController().isOnServer()) {
				try {
					Mine m = ((GameServerState) getState()).getController().getMineController().createMineServer(((GameServerState) getState()), getSegmentController().getSectorId(), shootContainer.weapontOutputWorldPos, getSegmentController().getOwnerState(), getSegmentController().getFactionId(), armIn, composition);
					((GameServerState) getState()).getController().getMineController().addMineServer(m);
					System.err.println("[SERVER] ADDED MINE");
				} catch (MineDataException e) {
					e.printStackTrace();
				}
			} else {
				/*AudioController.fireAudioEventRemote("MINE_LAYER_FIRE", getSegmentController().getId(), new AudioTag[] { AudioTags.GAME, AudioTags.SHIP, AudioTags.WEAPON, AudioTags.MINE_LAYER, AudioTags.FIRE }, AudioParam.ONE_TIME, AudioController.ent(getSegmentController(), mineLayerUnit.getElementCollectionId(), mineLayerUnit.getSignificator(), mineLayerUnit))*/
				AudioController.fireAudioEventRemoteID(895, getSegmentController().getId(), AudioController.ent(getSegmentController(), mineLayerUnit.getElementCollectionId(), mineLayerUnit));
			}
			mineLayerUnit.setStandardShotReloading();
			handleResponse(ShootingRespose.FIRED, mineLayerUnit, shootContainer.weapontOutputWorldPos);
		} else {
			handleResponse(ShootingRespose.RELOADING, mineLayerUnit, shootContainer.weapontOutputWorldPos);
		}
	}

	private boolean consumeMineResources(short[] composition, MineLayerUnit mineLayerUnit, MineLayerCollectionManager elementCollectionManager, ShootContainer shootContainer) {
		Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> cm = getSegmentController().getControlElementMap().getControllingMap().get(mineLayerUnit.elementCollectionManager.getControllerIndex());
		if (cm == null) {
			handleResponse(ShootingRespose.INITIALIZING, mineLayerUnit, shootContainer.weapontOutputWorldPos);
			return false;
		}
		try {
			for (short t : cm.keySet()) {
				if (ElementKeyMap.isValidType(t) && ElementKeyMap.getInfoFast(t).isInventory()) {
					FastCopyLongOpenHashSet invSet = cm.get(t);
					for (long indexInv : invSet) {
						Inventory inv = getManagerContainer().getInventory(ElementCollection.getPosIndexFrom4(indexInv));
						if (inv != null) {
							connectedInventories.put(inv, new IntOpenHashSet());
						} else {
							System.err.println("[MINELAYER] " + getState() + "; " + getSegmentController() + ": WARNING: no inventory connected: " + ElementCollection.getPosFromIndex(indexInv, new Vector3i()));
						}
					}
				}
			}
			if (connectedInventories.isEmpty()) {
				if (getSegmentController().getOwnerState() != null) {
					connectedInventories.put(getSegmentController().getOwnerState().getInventory(), new IntOpenHashSet());
				}
			}
			if (connectedInventories.isEmpty()) {
				getSegmentController().popupOwnClientMessage(Lng.str("No storage connected to minelayer"), ServerMessage.MESSAGE_TYPE_ERROR);
				handleResponse(ShootingRespose.RELOADING, mineLayerUnit, shootContainer.weapontOutputWorldPos);
				return false;
			}
			getConsumptionForMine(composition, consTmp);
			FastEntrySet es = consTmp.short2IntEntrySet();
			boolean hasEnough = true;
			for (Entry e : es) {
				final short type = ElementKeyMap.convertSourceReference(e.getShortKey());
				final int count = e.getIntValue();
				int quantity = 0;
				for (Inventory inv : connectedInventories.keySet()) {
					quantity += inv.getOverallQuantity(type);
				}
				haveTmp.put(type, quantity);
				if (quantity < count) {
					hasEnough = false;
				}
			}
			if (!hasEnough) {
				StringBuffer b = new StringBuffer();
				for (Entry e : es) {
					final short type = ElementKeyMap.convertSourceReference(e.getShortKey());
					final int count = e.getIntValue();
					final int have = haveTmp.get(type);
					ElementInformation info = ElementKeyMap.getInfo(type);
					b.append("\n" + info.getName() + ": " + have + "/" + count);
				}
				getSegmentController().popupOwnClientMessage(Lng.str("Not enough resources in connected storage to lay mine. Needed: %s", b.toString()), ServerMessage.MESSAGE_TYPE_ERROR);
				handleResponse(ShootingRespose.RELOADING, mineLayerUnit, shootContainer.weapontOutputWorldPos);
				return false;
			}
			if (getSegmentController().isOnServer()) {
				for (Entry e : es) {
					final short type = ElementKeyMap.convertSourceReference(e.getShortKey());
					int countToConsume = e.getIntValue();
					for (java.util.Map.Entry<Inventory, IntOpenHashSet> inv : connectedInventories.entrySet()) {
						int quantity = inv.getKey().getOverallQuantity(type);
						int consumed = Math.min(countToConsume, quantity);
						inv.getKey().decreaseBatch(type, consumed, inv.getValue());
						countToConsume -= consumed;
						assert (countToConsume >= 0);
						if (countToConsume == 0) {
							break;
						}
					}
				}
				for (java.util.Map.Entry<Inventory, IntOpenHashSet> inv : connectedInventories.entrySet()) {
					inv.getKey().sendInventoryModification(inv.getValue());
				}
			}
		} finally {
			haveTmp.clear();
			consTmp.clear();
			connectedInventories.clear();
		}
		return true;
	}

	private void getConsumptionForMine(short[] composition, Short2IntOpenHashMap consTmp) {
		int strength = MineSettings.calculateStrength(composition);
		int blockMult = getBlockConsumption(strength);
		// 
		// int blockMult = 1;
		// for(int i = 1; i < strength; i++) {
		// blockMult *= 10;
		// }
		// 
		for (final short type : composition) {
			if (ElementKeyMap.isValidType(type) && ElementKeyMap.getInfo(type).isMineAddOn()) {
				((Short2IntOpenHashMap) consTmp).addTo(type, blockMult);
			}
		}
		((Short2IntOpenHashMap) consTmp).addTo(ElementKeyMap.MINE_CORE, 1);
	}

	@Override
	public void addConnectionIfNecessary(Vector3i controller, short fromType, Vector3i controlled, short controlledType) {
		super.addConnectionIfNecessary(controller, fromType, controlled, controlledType);
		if (getSegmentController().isOnServer() && controlledType == controllingId) {
			System.err.println("[MINELAYER] REMOVING OTHERS " + controller + " -> " + controlled);
			// only one mine core can be connected to this
			long cIndex = ElementCollection.getIndex(controller);
			Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> mp = getSegmentController().getControlElementMap().getControllingMap().get(cIndex);
			long index4 = ElementCollection.getIndex4(controlled, controlledType);
			if (mp != null) {
				FastCopyLongOpenHashSet other = mp.get(controlledType);
				if (other != null) {
					for (long s : other) {
						if (s != index4) {
							assert (controlledType == ElementCollection.getType(s));
							getSegmentController().getControlElementMap().removeControllerForElement(cIndex, ElementCollection.getPosIndexFrom4(s), controlledType);
						}
					}
				}
			}
		}
	}

	@Override
	protected String getTag() {
		return "minelayer";
	}

	@Override
	public MineLayerCollectionManager getNewCollectionManager(SegmentPiece position, Class<MineLayerCollectionManager> clazz) {
		return new MineLayerCollectionManager(position, getSegmentController(), this);
	}

	@Override
	public String getManagerName() {
		return Lng.str("Warp Gate System Collective");
	}

	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {
	}

	@Override
	public void drawReloads(Vector3i iconPos, Vector3i iconSize, long controllerPos) {
		handleReload(iconPos, iconSize, controllerPos, drawReloadListener);
	}

	private static final DrawReloadListener drawReloadListener = new DrawReloadListener();

	private static final PrintReloadListener printReloadListener = new PrintReloadListener();

	@Override
	public int getCharges() {
		return 0;
	}

	@Override
	public int getMaxCharges() {
		return 0;
	}

	@Override
	public String getReloadStatus(long id) {
		return handleReload(null, null, id, drawReloadListener);
	}

	public String handleReload(Vector3i iconPos, Vector3i iconSize, long controllerPos, ReloadListener r) {
		boolean backwards = false;
		long time = System.currentTimeMillis();
		MineLayerCollectionManager ec = getCollectionManagersMap().get(controllerPos);
		List<MineLayerUnit> elementCollections = ec.getElementCollections();
		final int size = elementCollections.size();
		String s = null;
		for (int i = 0; i < size; i++) {
			MineLayerUnit d = elementCollections.get(i);
			if (!d.canUse(time, false)) {
				if (d.isUsingPowerReactors()) {
					if (d.getReactorReloadNeededFull() > 0) {
						float percent = 1.0f - (float) (d.getReactorReloadNeeded() / d.getReactorReloadNeededFull());
						if (percent <= 0.000001) {
							s = r.onDischarged((InputState) getState(), iconPos, iconSize, UsableControllableFiringElementManager.disabledColor, false, 1);
						} else {
							s = r.onReload((InputState) getState(), iconPos, iconSize, UsableControllableFiringElementManager.reloadColor, backwards, percent);
						}
					}
				} else {
					if (d.getCurrentReloadTime() > 0) {
						int duration = (int) (d.getNextShoot() - time);
						float percent = 1.0f - ((float) duration / (float) d.getCurrentReloadTime());
						s = r.onReload((InputState) getState(), iconPos, iconSize, UsableControllableFiringElementManager.reloadColor, backwards, percent);
					}
				}
			}
		}
		if (s != null) {
			return s;
		} else {
			return r.onFull((InputState) getState(), iconPos, iconSize, UsableControllableFiringElementManager.reloadColor, backwards, 1f, controllerPos);
		}
	}
}
