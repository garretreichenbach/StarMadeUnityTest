package api.utils.game;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.*;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorElement;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorTree;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.network.objects.Sendable;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.vecmath.Vector3f;
import java.util.ArrayList;

public class SegmentControllerUtils {
	/**
	 * Gets a Vector3f of the entity's current direction.
	 */
	@NotNull
	public static Vector3f getDirection(SegmentController controller) {
		return GlUtil.getForwardVector(new Vector3f(), controller.getWorldTransform());
	}

	@Nullable
	public static ReactorElement getChamberFromElement(@NotNull ManagedSegmentController<?> controller, @NotNull ElementInformation blockId) {
		for(ReactorElement child : getAllChambers(controller)) {
			if(child.type == blockId.id) return child;
		}
		return null;
	}

	@Nullable //for backwards compatibility; avoid NoSuchMethodError with old mod builds
	public static ReactorElement getChamberFromElement(@NotNull ManagedUsableSegmentController<?> controller, @NotNull ElementInformation blockId) {
		return getChamberFromElement((ManagedSegmentController<?>) controller, blockId);
	}

	@NotNull
	public static ArrayList<ReactorElement> getAllChambers(ManagedSegmentController<?> controller) {
		ArrayList<ReactorElement> r = new ArrayList<>();
		try {
			if(controller == null) return new ArrayList<>();
			ReactorTree mainReactor = controller.getManagerContainer().getPowerInterface().getActiveReactor();
			if(mainReactor == null) return new ArrayList<>();
			for(ReactorElement child : mainReactor.children) getChildrenChambers(child, r);
		} catch(Exception ignored) {}
		return r;
	}

	@NotNull
	private static void getChildrenChambers(ReactorElement element, ArrayList<ReactorElement> arr) {
		//Add children to array
		if(!element.children.isEmpty()) {
			for(ReactorElement child : element.children) {
				getChildrenChambers(child, arr);
			}
		}
		//Add self
		arr.add(element);
	}

	@NotNull //for backwards compatibility; avoid NoSuchMethodError with old mod builds
	public static ObjectArrayList<ManagerModule<?, ?, ?>> getManagerModules(ManagedUsableSegmentController<?> ent) {
		return getManagerModules((ManagedSegmentController<?>) ent);
	}

	@NotNull
	public static ObjectArrayList<ManagerModule<?, ?, ?>> getManagerModules(ManagedSegmentController<?> ent) {
		return ent.getManagerContainer().getModules();
	}

	@Nullable //for backwards compatibility; avoid NoSuchMethodError with old mod builds
	public static <EM extends UsableElementManager<?, ?, ?>> EM getElementManager(ManagedUsableSegmentController<?> ent, Class<EM> classType) {
		return getElementManager((ManagedSegmentController<?>) ent, classType);
	}

	@Nullable
	public static <EM extends UsableElementManager<?, ?, ?>> EM getElementManager(ManagedSegmentController<?> ent, Class<EM> classType) {
		for(ManagerModule<?, ?, ?> managerModule : getManagerModules(ent)) {
			UsableElementManager<?, ?, ?> elementManager = managerModule.getElementManager();
			if(elementManager.getClass().equals(classType)) {
				return (EM) elementManager;
			}
		}
		return null;
	}

	/**
	 * Gets an arraylist of players currently attached to the entity.
	 * Returns an empty list if none are attached
	 */
	public static ArrayList<PlayerState> getAttachedPlayers(SegmentController controller) {
		if(controller instanceof PlayerControllable) {
			return new ArrayList<PlayerState>(((PlayerControllable) controller).getAttachedPlayers());
		} else {
			return new ArrayList<PlayerState>();
		}
	}

	//for backwards compatibility; avoid NoSuchMethodError with old mod builds
	public static <CM extends ElementCollectionManager<?, ?, ?>> ElementCollectionManager<?, ?, ?> getPieceCollectionManager(ManagedUsableSegmentController<?> entity, SegmentPiece segmentPiece, Class<CM> classType) {
		return getPieceCollectionManager((ManagedSegmentController<?>) entity, segmentPiece, classType);
	}

	public static <CM extends ElementCollectionManager<?, ?, ?>> ElementCollectionManager<?, ?, ?> getPieceCollectionManager(ManagedSegmentController<?> entity, SegmentPiece segmentPiece, Class<CM> classType) {
		long absIndex = ElementCollection.getIndex4(segmentPiece.getAbsoluteIndex(), segmentPiece.getOrientation());
		for(ElementCollectionManager<?, ?, ?> collectionManager : getCollectionManagers(entity, classType)) {
			if(collectionManager.rawCollection.contains(absIndex) || collectionManager.rawCollection.contains(segmentPiece.getAbsoluteIndex())) return collectionManager;
		}
		return null;
	}

	public static <CM extends ElementCollectionManager<?, ?, ?>> ArrayList<ElementCollectionManager<?, ?, ?>> getCollectionManagers(ManagedSegmentController<?> ent, Class<CM> classType) {
		ArrayList<ElementCollectionManager<?, ?, ?>> ecms = new ArrayList<ElementCollectionManager<?, ?, ?>>();
		for(ManagerModule<?, ?, ?> module : ent.getManagerContainer().getModules()) {
			if(module instanceof ManagerModuleCollection) {
				for(Object cm : ((ManagerModuleCollection<?, ?, ?>) module).getCollectionManagers()) {
					if(cm.getClass().equals(classType)) {
						ecms.add((ElementCollectionManager<?, ?, ?>) cm);
					}
				}
			} else if(module instanceof ManagerModuleSingle) {
				ElementCollectionManager<?, ?, ?> cm = ((ManagerModuleSingle<?, ?, ?>) module).getCollectionManager();
				if(cm.getClass().equals(classType)) {
					ecms.add(cm);
				}
			}//else{ something broke }
		}
		return ecms;
	}

	//for backwards compatibility; avoid NoSuchMethodError with old mod builds
	public static <CM extends ElementCollectionManager<?, ?, ?>> ArrayList<ElementCollectionManager<?, ?, ?>> getCollectionManagers(ManagedUsableSegmentController<?> ent, Class<CM> classType) {
		return getCollectionManagers((ManagedSegmentController<?>) ent, classType);
	}

	/**
	 * Gets the SERVER and CLIENT versions of a sendable.
	 * Everything is loaded twice on singleplayer.
	 * <p>
	 * If SINGLEPLAYER:
	 * Returns {Server sendable, Client sendable}
	 * If DEDICATED SERVER:
	 * Returns {Server sendable}
	 * If DEDICATED CLIENT:
	 * Returns {Client sendable}
	 */
	public static Sendable[] getDualSidedSendable(Sendable sendable) {
		int id = sendable.getId();
		boolean serverExists = GameServerState.instance != null;
		boolean clientExists = GameClientState.instance != null;
		if(clientExists && serverExists) {
			return new Sendable[] {
				GameServerState.instance.getLocalAndRemoteObjectContainer().getLocalObjects().get(id),
				GameClientState.instance.getLocalAndRemoteObjectContainer().getLocalObjects().get(id)
			};
		} else if(clientExists) {
			return new Sendable[] {
				GameClientState.instance.getLocalAndRemoteObjectContainer().getLocalObjects().get(id)
			};
		} else if(serverExists) {
			return new Sendable[] {
				GameServerState.instance.getLocalAndRemoteObjectContainer().getLocalObjects().get(id)
			};
		}
		throw new IllegalStateException("No server OR client sendable found.");
	}

	/**
	 * Gets the CLIENT sendable of any sendable.
	 */
	@Nullable
	public static Sendable getClientSendable(Sendable sendable) {
		int id = sendable.getId();
		boolean clientExists = GameClientState.instance != null;
		if(clientExists) {
			return GameClientState.instance.getLocalAndRemoteObjectContainer().getLocalObjects().get(id);
		}
		return null;
	}

	@Nullable
	public static Sendable getServerSendable(Sendable sendable) {
		int id = sendable.getId();
		boolean clientExists = GameClientState.instance != null;
		if(clientExists) {
			return GameServerState.instance.getLocalAndRemoteObjectContainer().getLocalObjects().get(id);
		}
		return null;
	}

	@NotNull //for backwards compatibility; avoid NoSuchMethodError with old mod builds
	public static ArrayList<ElementCollectionManager> getAllCollectionManagers(ManagedUsableSegmentController<?> ent) {
		return getAllCollectionManagers((ManagedSegmentController<?>) ent);
	}

	@NotNull
	public static ArrayList<ElementCollectionManager> getAllCollectionManagers(ManagedSegmentController<?> ent) {
		ArrayList<ElementCollectionManager> ecms = new ArrayList<ElementCollectionManager>();
		for(ManagerModule<?, ?, ?> module : ent.getManagerContainer().getModules()) {
			if(module instanceof ManagerModuleCollection) {
				for(Object cm : ((ManagerModuleCollection) module).getCollectionManagers()) {
					ecms.add((ElementCollectionManager) cm);
				}
			} else if(module instanceof ManagerModuleSingle) {
				ElementCollectionManager cm = ((ManagerModuleSingle) module).getCollectionManager();
				ecms.add(cm);
			}
		}
		return ecms;
	}

	@Nullable //for backwards compatibility; avoid NoSuchMethodError with old mod builds
	public static PlayerUsableInterface getAddon(ManagedUsableSegmentController<?> ent, long playerUsableId) {
		return getAddon((ManagedSegmentController) ent, playerUsableId);
	}

	/**
	 * Gets an addon based on its PlayerUsableId, which is faster than the iterative method
	 */
	@Nullable
	public static PlayerUsableInterface getAddon(ManagedSegmentController<?> ent, long playerUsableId) {
		return ent.getManagerContainer().getPlayerUsable(playerUsableId);
	}

	@Nullable //for backwards compatibility; avoid NoSuchMethodError with old mod builds
	public static <P extends PlayerUsableInterface> P getAddon(ManagedUsableSegmentController<?> ent, Class<P> classType) {
		return getAddon((ManagedSegmentController) ent, classType);
	}

	/**
	 * Get a player usable (addon) based on its class type. will be slow.
	 */
	@Nullable
	public static <P extends PlayerUsableInterface> P getAddon(ManagedSegmentController<?> ent, Class<P> classType) {
		ObjectCollection<PlayerUsableInterface> usable = ent.getManagerContainer().getPlayerUsable();
		for(PlayerUsableInterface i : usable) {
			if(i.getClass().equals(classType)) {
				return (P) i;
			}
		}
		return null;
	}

	public static Object2ObjectOpenHashMap<Vector3i, SegmentController> getLoadedSegmentControllersInRange(Vector3i currentSector, float range) {
		Object2ObjectOpenHashMap<Vector3i, SegmentController> map = new Object2ObjectOpenHashMap<>();
		for(SegmentController controller : GameServerState.instance.getSegmentControllersByName().values()) {
			Vector3i controllerSector = new Vector3i();
            controller.getSector(controllerSector);
            if(controller.isFullyLoadedWithDock() && controllerSector.getDistance(currentSector) <= range) map.put(controllerSector, controller);
		}
		return map;
	}
}
