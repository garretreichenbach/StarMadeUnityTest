package org.schema.game.common.data.player;

import api.listener.events.block.BlockPublicPermissionEvent;
import api.mod.StarLoader;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.GameClientController;
import org.schema.game.client.controller.manager.ingame.navigation.NavigationControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.Starter;
import org.schema.game.common.controller.EditableSendableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.network.objects.ControllerUnitRequest;
import org.schema.game.network.objects.NetworkPlayer;
import org.schema.game.network.objects.remote.RemoteControllerUnitRequest;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyboardEvent;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;

import javax.vecmath.Vector3f;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ControllerState {

	public static final long DELAY_ORIENTATION_MS = 500;

	public final ObjectArrayFIFOQueue<ControllerUnitRequest> requests = new ObjectArrayFIFOQueue<ControllerUnitRequest>();

	private final Set<ControllerStateUnit> units = new HashSet<ControllerStateUnit>();
	private final Set<ControllerStateUnit> unitsToRemoveTmp = new HashSet<ControllerStateUnit>();
	private final PlayerState owner;
	ControllerStateUnit controllerStateUnitTmp = new ControllerStateUnit();
	private short lastCanKeyPressCheck;
	private boolean lastCanKeyPressCheckResult;

	private boolean suspended;

	private Transform lastTransform;

	private long lastDeferralLog;

	private int lastDeferralsAttempts;

	private final List<KeyboardEvent> events = new ObjectArrayList<>();

	private final ObjectOpenHashSet<KeyboardMappings> triggeredPressed = new ObjectOpenHashSet<>();

	public ControllerState(PlayerState owner) {
		this.owner = owner;

	}

	private void addUnit(PlayerState pState, PlayerControllable controllable, Vector3i toParameter, Sendable fromDetached, Vector3i fromParameter) {
		ControllerStateUnit controllerStateUnit = new ControllerStateUnit();
		controllerStateUnit.playerControllable = controllable;
		controllerStateUnit.parameter = toParameter;
		controllerStateUnit.playerState = pState;
		boolean add = false;
		add = units.add(controllerStateUnit);
		if (!controllable.getAttachedPlayers().contains(pState)) {
			controllable.getAttachedPlayers().add(pState);
			if(fromDetached != null && fromDetached instanceof PlayerControllable){
				((PlayerControllable)fromDetached).onPlayerDetachedFromThis(pState, controllable);
			}
			controllable.onAttachPlayer(pState, fromDetached, fromParameter, toParameter);
		}
			
		if (add) {
			if (controllable instanceof Ship) {
				pState.setLastEnteredShip((Ship) controllable);
			}

			System.err.println("[CONTROLLER][ADD-UNIT] (" + pState.getState() + "): " + pState + " Added to controllers: " + controllable);
			if (owner.isClientOwnPlayer()) {
				((GameClientState) owner.getState()).getController().updateCurrentControlledEntity(this);
			}

			Starter.modManager.onPlayerChangedContol(pState, controllable, toParameter, fromDetached, fromParameter);
		}
	}

	public void checkPlayerControllers() {
		assert (owner.isOnServer());
		if (ServerConfig.USE_STRUCTURE_HP.isOn()) {
			return;
		}

		for (ControllerStateUnit u : units) {

			if (u.playerControllable instanceof SegmentController) {
				SegmentController s = (SegmentController) u.playerControllable;

				Vector3i pos = (Vector3i) u.parameter;

				SegmentPiece pointUnsave = s.getSegmentBuffer().getPointUnsave(pos);//autorequest true previously

				System.err.println("PARAMETER " + u.parameter + ": POINT " + pointUnsave);

				if (pointUnsave != null && (!pointUnsave.isValid() || pointUnsave.isDead())) {
					PlayerCharacter p = owner.getAssingedPlayerCharacter();
					System.err.println("FORCING PLAYER OUT OF SHIP " + owner + ": " + this + " from " + u.playerControllable + ": " + p);
					assert (p != null);
					requestControl(u.playerControllable, p, pos, new Vector3i(), false);

				}

			}

		}

	}

	public boolean controls(ControllerStateUnit unit) {
		return unit.playerState == owner && isControlling(unit);
	}

	public ControllerStateUnit getControllerByClass(Class<Ship> clazz, PlayerState state) {
		for (ControllerStateUnit u : units) {
			if (u.playerState == state && u.playerControllable.equals(clazz)) {
				return u;
			}
		}
		return null;

	}

	/**
	 * @return the owner
	 */
	public PlayerState getOwner() {
		return owner;
	}

	/**
	 * @return the units
	 */
	public Set<ControllerStateUnit> getUnits() {
		return units;
	}

	public void handleKeyPress(Timer timer, PlayerState pstate) {
		for (ControllerStateUnit u : units) {
			if (u.playerState == pstate) {
				if (!pstate.isOnServer() && u.playerControllable instanceof SimpleTransformableSendableObject) {
					if (u.playerState == ((GameClientState) pstate.getState()).getPlayer()) {
						((GameClientState) pstate.getState()).setCurrentPlayerObject((SimpleTransformableSendableObject) u.playerControllable);
					}
				}
				assert(u.getPlayerState() != null);

				//character etc using regular method
				if(u.playerControllable.isControlHandled(u)) {
					u.playerControllable.handleKeyPress(timer, u);
				}
			}
		}
	}
//	public void handleMouseEvent(MouseEvent e) {
////		if(isOnServer()) {
////			System.err.println("MOUSE EVENT "+e);
////		}
//		PlayerState pstate = owner;
//		for (ControllerStateUnit u : units) {
//			if (u.playerState == pstate) {
//				if (!pstate.isOnServer() && u.playerControllable instanceof SimpleTransformableSendableObject) {
//					if (u.playerState == ((GameClientState) pstate.getState()).getPlayer()) {
//						((GameClientState) pstate.getState()).setCurrentPlayerObject((SimpleTransformableSendableObject) u.playerControllable);
//					}
//				}
//
//				if (u.isFlightControllerActive()
//						&& u.isUnitInPlayerSector()) {
//					u.playerControllable.handleMouseEvent(u, e);
//				}
//
//			}
//		}
//	}
//
	public boolean isOnServer() {
		return owner.isOnServer();
	}

	public void handleControllerStateFromNT(NetworkPlayer p) {

		for (int i = 0; i < p.controlRequestParameterBuffer.getReceiveBuffer()
				.size(); i++) {

			ControllerUnitRequest param = p.controlRequestParameterBuffer
					.getReceiveBuffer().get(i).get();


			System.err.println("[CONTROLLERSTATE] INCOMING REQUEST (NT) " + owner.getState() + "; " + this.owner + " CONTROLLER REQUEST RECEIVED  " + param);

			
			assert(param.fromParam != null);
			assert(param.toParam != null);
			
			requests.enqueue(param);
		}

	}

	public boolean isAdminException() {
		boolean b = ((GameServerState) owner.getState()).isAdmin(owner.getName()) && ServerConfig.ADMINS_CIRCUMVENT_STRUCTURE_CONTROL.isOn();

		if (b) {
			owner.sendServerMessage(new ServerMessage(Lng.astr("Admin priviledges used\nto enter.\n(would otherwise be denied)"), ServerMessage.MESSAGE_TYPE_INFO, owner.getId()));
		}

		return b;
	}

	public boolean isFactionException(SegmentController to, Vector3i toPos, int forFactionId) {
		boolean result = false;
		if (toPos != null && to != null) {
			if (to instanceof Ship && toPos.equals(Ship.core)) {
				result = false;
			} else {
				Vector3i posDir = new Vector3i();
				for (int i = 0; i < 6; i++) {
					posDir.add(toPos, Element.DIRECTIONSi[i]);
					SegmentPiece pointUnsave;
					pointUnsave = to.getSegmentBuffer().getPointUnsave(posDir);

					if (pointUnsave != null && pointUnsave.getType() == ElementKeyMap.FACTION_PUBLIC_EXCEPTION_ID ||
							(pointUnsave.getType() == ElementKeyMap.FACTION_FACTION_EXCEPTION_ID && pointUnsave.getSegmentController().getFactionId() == forFactionId)) {
						result = true;
					}
				}
			}
			BlockPublicPermissionEvent ev = new BlockPublicPermissionEvent(to, toPos, forFactionId, result);
			StarLoader.fireEvent(ev, to.isOnServer());
			result = ev.getPermission();
		}

		return result;
	}
	/**
	 * make sure that the point to enter is loaded
	 * @param request
	 * @return
	 */
	private boolean canHandleRequest(ControllerUnitRequest request) {
		if(owner.isOnServer()) {
			return true;
		}
		
		final Vector3i fromPos = request.fromParam;
		final Vector3i toPos = request.toParam;
		boolean hideExitedObject = request.hideExitedObject;

		final Sendable from = owner.getState()
				.getLocalAndRemoteObjectContainer()
				.getLocalObjects().get(request.fromId);
		Sendable to = owner.getState()
				.getLocalAndRemoteObjectContainer()
				.getLocalObjects().get(request.toId);
		
		if(request.toParam != null && to instanceof SegmentController) {
			SegmentPiece pointUnsave = ((SegmentController)to).getSegmentBuffer().getPointUnsave(request.toParam);
			//make sure that the entry point is loaded
			return pointUnsave != null;
		}
		
		return true;
	}
	private void handleRequest(ControllerUnitRequest request) {

		owner.inControlTransition = System.currentTimeMillis();

		assert(request.toParam != null);

		final Vector3i fromPos = request.fromParam;
		final Vector3i toPos = request.toParam;
		boolean hideExitedObject = request.hideExitedObject;

		final Sendable from = owner.getState()
				.getLocalAndRemoteObjectContainer()
				.getLocalObjects().get(request.fromId);
		Sendable to = owner.getState()
				.getLocalAndRemoteObjectContainer()
				.getLocalObjects().get(request.toId);

		if (owner.isOnServer() && to != null && to instanceof PlayerControllable) {

			if (to instanceof SegmentController) {
				SegmentController seg = ((SegmentController) to);
				if (seg.getFactionId() != 0 && ((FactionState) owner.getState()).getFactionManager().existsFaction(seg.getFactionId()) && seg.getFactionId() != owner.getFactionId()) {
					if (!isFactionException(seg, toPos, owner.getFactionId())) {
						if (!isAdminException()) {
							//							detach(from, fromPos, hideExitedObject);
							((GameServerState) owner.getState()).getController().broadcastMessageAdmin(Lng.astr("SERVER: %s\nattempted to take control of\n%s\n(Faction access denied)",  owner.getName(),  to), ServerMessage.MESSAGE_TYPE_ERROR);
							//							return;

							to = owner.getAssingedPlayerCharacter();
						}
					}
				}
			}
			if (to instanceof PlayerControllable && ((PlayerControllable) to).getAttachedPlayers().size() > 0) {

				for (PlayerState alreadyAttached : ((PlayerControllable) to).getAttachedPlayers()) {

					for (ControllerStateUnit u : alreadyAttached.getControllerState().units) {
						if (u.parameter != null && u.parameter.equals(toPos)) {
							if (alreadyAttached != owner) {
								if (!isAdminException()) {
									//									detach(from, fromPos, hideExitedObject);
									((GameServerState) owner.getState()).getController().broadcastMessageAdmin(Lng.astr("SERVER: %s\nattempted to take control of\n%s\n(Already controlled by player %s)",  owner.getName(),  to,  u.playerState), ServerMessage.MESSAGE_TYPE_ERROR);
									//									return;

									to = owner.getAssingedPlayerCharacter();
									if (from == to) {
										return;
									}
								}
							} else {
								System.err.println("[SERVER] WARNING Duplicate entry: " + owner + " - " + units + "; request: " + from + "(" + fromPos + ") -> " + to + "(" + toPos + ")");
								assert(false);
							}
						}
					}
				}
			}

		}

		detach(from, fromPos, hideExitedObject);

		if (to != null) {

			if (to instanceof SimpleTransformableSendableObject) {
				owner.setLastOrientation(((SimpleTransformableSendableObject) to).getWorldTransform());
			}

			if (to instanceof PlayerControllable) {
				PlayerControllable toPl = (PlayerControllable) to;
				addUnit(owner, toPl, toPos, from, fromPos);
			}
		} else {
			//request release to nothing -> clear all
			units.clear();
		}

		if (owner.isOnServer()) {
			//deligate request to clients
			sendControlRequest((PlayerControllable) from, (PlayerControllable) to, fromPos, toPos, hideExitedObject);
		}

	}

	private void detach(Sendable from, Vector3i fromPos,
	                    boolean hideExitedObject) {
		if (from != null) {

			if (from instanceof PlayerControllable) {
				PlayerControllable fromPl = (PlayerControllable) from;
				
				removeUnit(fromPl, fromPos, owner, hideExitedObject);

				//get transformation of object that was detached
				
				
			}
			if (from instanceof EditableSendableSegmentController) {
				System.err.println("[CONTROLLERSTATE] CHECKING EE: " + owner+", "+owner.getState());
				assert (!NavigationControllerManager.isPlayerInCore(owner.getAssingedPlayerCharacter())) : "DETACH FROM: "+from+"; "+ units;
			}
		} else {
		}
	}

	private void handleRequests() {
		if (!requests.isEmpty()) {
			while (!requests.isEmpty()) {
				
				//check if the entry point of segmentcontrollers is loaded is the request is entering one of those
				if(!canHandleRequest(requests.first())) {
					lastDeferralsAttempts++;
					if(System.currentTimeMillis() - lastDeferralLog > 2000) {
						lastDeferralLog = System.currentTimeMillis();
						System.err.println((isOnServer() ? "[SERVER]" : "[CLIENT]") +" DEFERRING CONTROLLER REQUEST SINCE BLOCK IS NOT LOADED: TOTAL ATTEMPTS: "+lastDeferralsAttempts+"; "+requests.first());
					}
					return;
				}
				lastDeferralsAttempts = 0;
				
				handleRequest(requests.dequeue());
			}
		}

	}

	

	public boolean isControlling(ControllerStateUnit unit) {
		return units.contains(unit);
	}

	public boolean isControlling(PlayerState state, PlayerControllable controllable, Vector3i parameter) {

		controllerStateUnitTmp.playerState = state;
		controllerStateUnitTmp.playerControllable = controllable;
		controllerStateUnitTmp.parameter = parameter;
		return units.contains(controllerStateUnitTmp);
	}

	public boolean isOwnerControlling(PlayerControllable controllable) {
		for (ControllerStateUnit u : units) {
			if (u.playerControllable == controllable) {
				return true;
			}
		}
		return false;
	}

	public boolean isOwnerControlling(PlayerControllable controllable, Vector3i parameter) {
		return isControlling(owner, controllable, parameter);
	}

	/**
	 * @return the suspended
	 */
	public boolean isSuspended() {
		return suspended;
	}

	public void setSuspended(boolean suspend) {
		this.suspended = suspend;
	}

	public void onDestroyedElement(SegmentPiece segmentPiece) {
		if (segmentPiece.getSegmentController() instanceof PlayerControllable) {
			ControllerStateUnit csu = new ControllerStateUnit();
			csu.playerControllable = (PlayerControllable) segmentPiece
					.getSegment().getSegmentController();
			csu.playerState = owner;
			csu.parameter = segmentPiece.getAbsolutePos(new Vector3i());
			if (controls(csu)) {
				System.err.println(segmentPiece.getSegmentController().getState() + " onDestroyedElement: Forcing " + this
						+ " to leave a controllable element of " + segmentPiece.getSegment().getSegmentController() + ": " + segmentPiece);
				if(isOnServer()) {
					requestControlServerAndSend(csu.playerControllable, owner.getAssingedPlayerCharacter(), (Vector3i) csu.parameter, null, false);
				}else {
					requestControl(csu.playerControllable, owner.getAssingedPlayerCharacter(), (Vector3i) csu.parameter, null, false);
				}

//				assert(!NavigationControllerManager.isPlayerInCore((SimpleTransformableSendableObject)csu.playerControllable));
			}

		}

	}

	public void forcePlayerOutOfSegmentControllers() {
		for (ControllerStateUnit csu : units) {
			if (csu.playerControllable instanceof SegmentController) {

				System.err.println("[SERVER] forceOutOfSegmentController: Forcing " + this
						+ " to leave a controllable element of " + csu.playerControllable);

				csu.playerState.sendServerMessage(new ServerMessage(Lng.astr("You are forced out of this structure!"), ServerMessage.MESSAGE_TYPE_ERROR, csu.playerState.getId()));

				requestControlServerAndSend(csu.playerControllable, owner.getAssingedPlayerCharacter(), (Vector3i) csu.parameter, null, false);

			}
		}
	}

	public void forcePlayerOutOfShips() {
		for (ControllerStateUnit csu : units) {
			if (csu.playerControllable instanceof Ship) {

				System.err.println("[SERVER] forceOutOfShip: Forcing " + this
						+ " to leave a controllable element of " + csu.playerControllable);

				csu.playerState.sendServerMessage(new ServerMessage(Lng.astr("You are forced out of this structure!"), ServerMessage.MESSAGE_TYPE_ERROR, csu.playerState.getId()));

					requestControlServerAndSend(csu.playerControllable, owner.getAssingedPlayerCharacter(), (Vector3i) csu.parameter, null, false);

			}
		}
	}

	public void removeAllUnitsFromPlayer(PlayerState playerState, boolean hide) {

		Iterator<ControllerStateUnit> it = units.iterator();
		while (it.hasNext()) {
			ControllerStateUnit u = it.next();
			if (u.playerState == playerState) {
				it.remove();

				u.playerControllable.getAttachedPlayers().remove(playerState);
				u.playerControllable.onDetachPlayer(playerState, hide, (Vector3i) u.parameter);
				if (u.playerControllable instanceof Ship) {
					((Ship) u.playerControllable).setFlagNameChange(true);
				}
			}
		}
		lastTransform = null;
		System.err.println("[ControllerState] REMOVED ALL UNITS OF " + owner + " ON " + owner.getState() + "! LEFT " + units);

		System.err.println("[NOTIFIED] REMOVED ALL UNITS OF " + owner + " ON " + owner.getState() + "! LEFT " + units);
	}

	private Transform removeUnit(PlayerControllable controllable, Vector3i parameter, PlayerState owner, boolean hide) {

		controllerStateUnitTmp.playerControllable = controllable;
		controllerStateUnitTmp.parameter = parameter;
		controllerStateUnitTmp.playerState = owner;
		boolean remove = false;
		Transform detachTransform = new Transform();

		detachTransform.set(((SimpleTransformableSendableObject) controllable).getWorldTransform());
		if (parameter != null) {
			Vector3f blockPos = new Vector3f(parameter.x - SegmentData.SEG_HALF, parameter.y - SegmentData.SEG_HALF, parameter.z - SegmentData.SEG_HALF);
			detachTransform.transform(blockPos);
			detachTransform.origin.set(blockPos);
		}
		System.err.println("[CONTROLLERSTATE][REMOVE-UNIT] " + owner.getState() + "; REMOVING CONTROLLER UNIT FROM " + owner + ": " + controllable + "; detach pos: " + detachTransform.origin);

		controllable.getAttachedPlayers().remove(owner);
		controllable.onDetachPlayer(owner, hide, parameter);
		remove = units.remove(controllerStateUnitTmp);
			
		if (remove) {
			if (owner.isClientOwnPlayer()) {
				((GameClientState) owner.getState()).getController().updateCurrentControlledEntity(this);
			}
		}
		return detachTransform;
	}

	public void requestControlServerAndSend(PlayerControllable from, PlayerControllable to,
			Vector3i fromParam, Vector3i toParam, boolean hideExitedObject) {
		assert(owner.isOnServer());
			System.err.println("[SERVER]: " + owner + " request control: " + from + " -> " + to+" [[[FORCE TO CLIENT]]]");
			ControllerUnitRequest r = new ControllerUnitRequest();
			
			r.setFrom(from);
			r.setTo(to);
			r.setFromParam(fromParam);
			r.setToParam(toParam == null ? new Vector3i() : toParam);
			r.setHideExitedObject(hideExitedObject);
			requests.enqueue(r);
			sendControlRequest(from, to, fromParam, toParam, hideExitedObject);
	}
	public void requestControl(PlayerControllable from, PlayerControllable to,
	                           Vector3i fromParam, Vector3i toParam, boolean hideExitedObject) {

		if (owner.isOnServer()) {
			System.err.println("[SERVER]: " + owner + " request control: " + from + " -> " + to);
			ControllerUnitRequest r = new ControllerUnitRequest();
			
			r.setFrom(from);
			r.setTo(to);
			r.setFromParam(fromParam);
			r.setToParam(toParam == null ? new Vector3i() : toParam);
			r.setHideExitedObject(hideExitedObject);
			requests.enqueue(r);
		} else {
			owner.inControlTransition = System.currentTimeMillis();

			if (to instanceof SimpleTransformableSendableObject) {
				owner.setLastOrientation(((SimpleTransformableSendableObject) to).getWorldTransform());
			}
			SegmentPiece entered = ((GameClientState)owner.getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getEntered();
//			assert(!(from instanceof Ship) || (entered != null && from == entered.getSegmentController())):"FROM "+from+"; TO "+to+"; ENTERED: "+(entered != null ? entered.getSegmentController()+"; "+entered : "null");
			System.err.println("[CLIENT]: " + owner + " request control: " + from + " -> " + to+";  Entered: "+(entered != null ? entered.getSegmentController()+"; "+entered : "null"));
			//send to handle on server
			sendControlRequest(from, to, fromParam, toParam, hideExitedObject);
		}

	}

	public void sendAll() {
		for (ControllerStateUnit u : units) {
			
			ControllerUnitRequest r = new ControllerUnitRequest();
			
			r.setFrom(null);
			
			
			r.setFromParam(null);
			
			r.setTo(u.playerControllable);
			r.setToParam((Vector3i) u.parameter);
			
			r.setHideExitedObject(u.playerControllable.isHidden());
			
			
			sendRequest(r);
			
		}

	}
	public StateInterface getState(){
		return owner.getState();
	}
	public void sendRequest(ControllerUnitRequest r){
//		try{
//			throw new NullPointerException("IIIIIIIIIII "+r);
//		}catch(Exception s){
//			s.printStackTrace();
//		}
		owner.getNetworkObject().controlRequestParameterBuffer.add(new RemoteControllerUnitRequest(r, owner.getNetworkObject()));
	}
	private void sendControlRequest(PlayerControllable from, PlayerControllable to,
	                                Vector3i fromParam, Vector3i toParam, boolean hideExitedObject) {
		
		
		ControllerUnitRequest r = new ControllerUnitRequest();
		
		r.setFrom(from);
		r.setTo(to);
		
		r.setFromParam(fromParam);
		r.setToParam(toParam);
		
		r.setHideExitedObject(hideExitedObject);
		
		
		sendRequest(r);
		
//		RemoteIntegerArray a = new RemoteIntegerArray(9, owner.getNetworkObject());
//		if (from != null) {
//			a.set(0, from.getId());
//		} else {
//			a.set(0, -1);
//		}
//		if (fromParam != null) {
//			a.set(1, fromParam.x);
//			a.set(2, fromParam.y);
//			a.set(3, fromParam.z);
//		} else {
//			a.set(1, 0);
//			a.set(2, 0);
//			a.set(3, 0);
//		}
//		if (to != null) {
//			a.set(4, to.getId());
//		} else {
//			a.set(4, -1);
//		}
//		if (toParam != null) {
//			a.set(5, toParam.x);
//			a.set(6, toParam.y);
//			a.set(7, toParam.z);
//		} else {
//			a.set(5, 0);
//			a.set(6, 0);
//			a.set(7, 0);
//		}
//		a.set(8, hideExitedObject ? 1 : -1);
//
//		owner.getNetworkObject().controlRequestParameterBuffer.add(a);
	}
	/**
	 * To check events during the main update (e.g. single click on a mouse button instead of having to manually track isDown())
	 *
	 * @param mapping
	 * @return true if there was a event for this mapping during this update
	 */
	public boolean isTriggered(KeyboardMappings mapping) {
		return triggeredPressed.contains(mapping);
	}
	public void update(Timer timer) {
		Iterator<ControllerStateUnit> it = units.iterator();

		//last transform is created here so the first attach doesnt use identity as reference
		if (units.size() > 0 && lastTransform == null) {
			lastTransform = new Transform();
			lastTransform.setIdentity();
		}

		while (it.hasNext()) {
			ControllerStateUnit next = it.next();

			if (next.playerControllable instanceof SimpleTransformableSendableObject) {
				lastTransform.set(((SimpleTransformableSendableObject) next.playerControllable).getWorldTransform());
				if (next.parameter != null && next.parameter instanceof Vector3i) {
					Vector3f blockPos = new Vector3f(((Vector3i) next.parameter).x - SegmentData.SEG_HALF, ((Vector3i) next.parameter).y - SegmentData.SEG_HALF, ((Vector3i) next.parameter).z - SegmentData.SEG_HALF);
					lastTransform.transform(blockPos);
					lastTransform.origin.set(blockPos);
				}
			}
			if (!owner.getState().getLocalAndRemoteObjectContainer().getLocalObjects().containsKey(next.playerControllable.getId())) {
				it.remove();
			}
		}

		handleRequests();

		if(isOnServer()) {
			//handles the key events on server (received over network)
			handleServerKeyEvents(timer);
		}
		if (!suspended) {
			//handles key presses for both server and client
			handleKeyPress(timer, owner);
		}
		triggeredPressed.clear();
	}

	public Transform getLastTransform() {
		return lastTransform;
	}

	public void setLastTransform(Transform t) {
		lastTransform = t;
	}

	public boolean isDown(KeyboardMappings m) {
		if(isOnServer()) {
			return receivedPressedMap >= 0 && (receivedPressedMap & m.ntKey) == m.ntKey;
		}else {
			return m.isDown();
		}
	}
	private final ByteList receivedEvents = new ByteArrayList();
	private int receivedPressedMap = -1;

	public void handleKeyEvent(KeyboardMappings mapping, Timer timer) {
		triggeredPressed.add(mapping); //add mapping to pressed set, so that it's possible to check events in the regular update
		PlayerState pstate = owner;
		for (ControllerStateUnit u : units) {
			if (u.playerState == pstate) {
				if (!pstate.isOnServer() && u.playerControllable instanceof SimpleTransformableSendableObject) {
					if (u.playerState == ((GameClientState) pstate.getState()).getPlayer()) {
						((GameClientState) pstate.getState()).setCurrentPlayerObject((SimpleTransformableSendableObject) u.playerControllable);
					}
				}

				if (u.isFlightControllerActive()
						&& u.isUnitInPlayerSector()) {
					u.playerControllable.handleKeyEvent(u, mapping, timer);
				}
			}
		}
	}
	public void handleServerKeyEvents(Timer timer) {
		for(KeyboardMappings b : triggeredPressed) {
			handleKeyEvent(b, timer);
		}
	}
	public boolean canClientPressKey() {
		assert (!isOnServer());
		if(lastCanKeyPressCheck != getState().getNumberOfUpdate()){
			lastCanKeyPressCheckResult = !((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getChatControlManager().isActive()
				&& ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().isActive()
				&& !((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager()
				.isAnyMenuActive()
				&& ((GameClientState) getState()).getPlayerInputs().isEmpty();
			lastCanKeyPressCheck = getState().getNumberOfUpdate();
		}
		return lastCanKeyPressCheckResult;
	}
	public void sendInputToServer() {
		if (owner.isClientOwnPlayer() && ((GameClientController)getState().getController()).isWindowActive()) {
			if (canClientPressKey()) {
				ByteArrayList events = ((GameClientController)getState().getController()).getInputController().getEventKeyNTQueue();
				int map = ((GameClientController)getState().getController()).getInputController().getPressedKeyNTMap();
				owner.getNetworkObject().keyboardPressedMap.set(map,true);
				owner.getNetworkObject().keyboardEventQueue.addAll(events);
			}
		}
	}
	public void receiveInput(NetworkPlayer p) {
		triggeredPressed.clear();
		for (int i = 0; i < p.keyboardEventQueue
				.getReceiveBuffer().size(); i++) {
			if(owner != null) {
				owner.incInputBasedSeed();
			}
			final byte b = p.keyboardEventQueue.getReceiveBuffer().getByte(i);
			receivedEvents.add(b);
			boolean pressed = b > 0;
			int ordinal = Math.abs(b)-1;
			KeyboardMappings m = KeyboardMappings.remoteMappings[ordinal];
			triggeredPressed.add(m);
		}
		this.receivedPressedMap = p.keyboardPressedMap.getInt();
	}

	public void resetNetworkInputClient() {

	}

	public void handleReceivedInput() {
		// TODO Auto-generated method stub

	}

}
