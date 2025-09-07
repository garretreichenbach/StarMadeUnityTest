package org.schema.game.common.data.element.beam;

import api.listener.events.weapon.BeamPostAddEvent;
import api.listener.events.weapon.BeamPreAddEvent;
import api.mod.StarLoader;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.schema.common.FastMath;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.TransformTools;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.GameClientController;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.view.beam.BeamColors;
import org.schema.game.client.view.beam.BeamDrawer;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.HitType;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;
import org.schema.game.common.controller.elements.BeamState;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ShootingRespose;
import org.schema.game.common.controller.elements.ammo.damagebeam.DamageBeamCapacityElementManager;
import org.schema.game.common.controller.elements.beam.BeamCommand;
import org.schema.game.common.controller.elements.mines.MineController;
import org.schema.game.common.controller.elements.power.reactor.StabilizerPath;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ColorBeamInterface;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementNotFoundException;
import org.schema.game.common.data.physics.*;
import org.schema.game.common.data.world.*;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;
import org.schema.schine.graphicsengine.forms.debug.DebugPoint;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.Identifiable;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.client.ClientController;
import org.schema.schine.network.client.ClientState;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.physics.IgnoreBlockRayTestInterface;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import static java.lang.Math.max;
import static org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager.WeaponType.BEAM;
import static org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager.WeaponType.REPAIR;

public abstract class AbstractBeamHandler<E extends SimpleTransformableSendableObject> implements IgnoreBlockRayTestInterface, NonBlockHitCallback {

	private static final Vector4f color1Blue = new Vector4f(0.0f, 0.0f, 1.0f, 1.0f);
	private static final Vector4f color1Green = new Vector4f(0.0f, 1.0f, 0.0f, 1.0f);
	private static final Vector4f color1Red = new Vector4f(0.7f, 0.0f, 0.0f, 1.0f);
	private static final Vector4f color1Yellow = new Vector4f(1.0f, 1.0f, 0.0f, 1.0f);
	private static final Vector4f color1White = new Vector4f(0.7f, 0.7f, 0.7f, 1.0f);
	private static final Vector4f color1Purple = new Vector4f(1.0f, 0.3f, 1.0f, 1.0f);
	private final Long2ObjectOpenHashMap<BeamState> beamStates = new Long2ObjectOpenHashMap<BeamState>();
	private final Set<Segment> updatedSegments = new ObjectOpenHashSet<Segment>();
	private final Vector3i tmp = new Vector3i();
	Vector3f start = new Vector3f();
	Vector3f end = new Vector3f();
	String[] cannotHitReason = new String[1];
	Vector3f tmpVal = new Vector3f();
	BoundingBox secTmpBB = new BoundingBox();

	private final BeamHandlerContainer<E> owner;
	private boolean lastActive;
	private final Vector3f dirTmp = new Vector3f();
	private BeamDrawer drawer;
	private final E fromObj;
	private final CubeRayCastResult rayCallback;

	protected AbstractBeamHandler(BeamHandlerContainer<E> owner, E fromObj) {
		this.owner = owner;
		this.fromObj = fromObj;
		rayCallback = new CubeRayCastResult(new Vector3f(), new Vector3f(), fromObj);
	}

	public boolean isOnServer() {
		return fromObj.isOnServer();
	}

	public StateInterface getState() {
		return fromObj.getState();
	}

	public abstract InterEffectSet getAttackEffectSet(long weaponId, DamageDealerType damageDealerType);

	public abstract MetaWeaponEffectInterface getMetaWeaponEffect(long weaponId, DamageDealerType damageDealerType);

	public static Vector4f getColorRange(BeamColors color) {
		return switch(color) {
			case BLUE -> (color1Blue);
			case GREEN -> (color1Green);
			case RED -> (color1Red);
			case PURPLE -> (color1Purple);
			case YELLOW -> (color1Yellow);
			case WHITE -> (color1White);
			default -> (color1Green);
		};
	}

	public SimpleTransformableSendableObject<?> getShootingEntity() {
		return fromObj;
	}

	public E getBeamShooter() {
		return fromObj;
	}

	public ShootingRespose addBeam(BeamCommand b) {
		//INSERTED CODE @???
		BeamPreAddEvent event = new BeamPreAddEvent(this, b);
		StarLoader.fireEvent(event, isOnServer());
		if(event.isCanceled()) return ShootingRespose.NO_POWER;
		///

		BeamState beamState = beamStates.get(b.identifier);
		if(beamState == null) {
			if(!b.reloadCallback.isInitializing(b.currentTime)) {
				if(b.reloadCallback.canUse(b.currentTime, false)) {
					if(b.reloadCallback.isUsingPowerReactors() || b.reloadCallback.canConsumePower(b.powerConsumedByTick)) {

						if(b.capacityPerTick > 0 && owner.getShootingEntity() instanceof ManagedUsableSegmentController<?>) {
							ManagedUsableSegmentController<?> entity = (ManagedUsableSegmentController<?>) ((SegmentController) owner.getShootingEntity()).railController.getRoot();
							if(!handleAmmo(b, entity)) return ShootingRespose.RELOADING;
						}

						if(b.lastShot) {
							b.reloadCallback.setShotReloading(((long) (b.cooldownSec * 1000.0f)));
						}
						beamState = new BeamState(
								b.identifier, b.relativePos, b.from, b.to, b.playerState,
								b.tickRate, b.beamPower, b.weaponId, b.beamType, b.originMetaObject,
								b.controllerPos, b.handheld, b.latchOn, b.checkLatchConnection, b.capacityPerTick, b.hitType, this);
						if(b.beamTimeout < 0) {
							beamState.timeOutInSecs = getBeamTimeoutInSecs();
						} else {
							beamState.timeOutInSecs = b.beamTimeout;
						}
						beamState.reloadCallback = b.reloadCallback;
						beamState.powerConsumptionPerTick = b.powerConsumedByTick;
						beamState.powerConsumptionExtraPerTick = b.powerConsumedExtraByTick;

						checkBeamButtons(beamState, b); //[REFACTOR]: Moved to separate method

						beamState.timeRunningSinceLastUpdate = 0;
						beamState.timeRunning = 0;
						beamState.dontFade = b.dontFade;
						beamState.size = b.beamPower;
						beamState.burstTime = b.bursttime;
						beamState.initialTicks = b.initialTicks;
						beamState.railParent = b.railParent;
						beamState.railChild = b.railChild;
						beamState.ticksToDo = FastMath.round(b.bursttime > 0 ? (b.bursttime / b.tickRate) : 0);
						beamState.fireStart = b.currentTime;
						beamState.latchOn = b.latchOn;
						beamState.checkLatchConnection = b.checkLatchConnection;
						beamState.firstLatch = Long.MIN_VALUE;
						beamState.hitType = b.hitType;

						beamState.minEffectiveRange = b.minEffectiveRange;
						beamState.minEffectiveValue = b.minEffectiveValue;
						beamState.maxEffectiveRange = b.maxEffectiveRange;
						beamState.maxEffectiveValue = b.maxEffectiveValue;
						beamState.ignoreShield = b.ignoreShields;
						beamState.ignoreArmor = b.ignoreArmor;
						beamState.friendlyFire = b.firendlyFire;
						beamState.penetrating = b.penetrating;
						beamState.acidDamagePercent = b.acidDamagePercent;

						beamState.beamLength = Vector3fTools.diffLength(beamState.from, beamState.to);
						if(b.beamTimeout < 0) {
							b.reloadCallback.flagBeamFiredWithoutTimeout();
						}
						//INSERTED CODE @???
						BeamPostAddEvent postEvent = new BeamPostAddEvent(this, beamState, false);
						StarLoader.fireEvent(postEvent, isOnServer());
						if(postEvent.isCanceled()) return ShootingRespose.NO_POWER;
						///
						beamStates.put(b.identifier, beamState);

						return ShootingRespose.FIRED;
					} else {
						return ShootingRespose.NO_POWER;
					}
				} else {
					return ShootingRespose.RELOADING;
				}
			} else {
				return ShootingRespose.INITIALIZING;
			}
		} else {

			checkBeamButtons(beamState, b);
			if(b.reloadCallback.isUsingPowerReactors() || b.reloadCallback.canConsumePower(b.powerConsumedByTick)) {
				if(b.beamTimeout < 0) {
					beamState.timeOutInSecs = getBeamTimeoutInSecs();
				} else {
					beamState.timeOutInSecs = b.beamTimeout;
				}
//				beamState.ticksToDo = Math.round (b.bursttime > 0 ? (b.bursttime/b.tickRate) : 0);
				beamState.from.set(b.from);

				if(beamState.latchOn && beamState.currentHit != null) {
					//dont update beam end point as its currently latched on
				} else {
					beamState.to.set(b.to);
				}
				if(b.beamTimeout < 0) {
					b.reloadCallback.flagBeamFiredWithoutTimeout();
				}
				beamState.timeRunningSinceLastUpdate = 0;
				//INSERTED CODE @???
				BeamPostAddEvent postEvent = new BeamPostAddEvent(this, beamState, true);
				StarLoader.fireEvent(postEvent, isOnServer());
				//Todo: Fix merge conflict
				if(postEvent.isCanceled()) {
					return ShootingRespose.NO_POWER;
				}
				///
				//Causes weird sound stacking when holding the mouse button, it's seen as firing during the whole beam burst time/ticks so it also triggers the sound every time it checks
				return ShootingRespose.FIRED;
			} else {

				//				//stop beam because power ran out
				beamState.timeOutInSecs = 0;
				return ShootingRespose.NO_POWER;
			}
		}
	}

	/**
	 * @param out The incoming BeamCommand. It will be modified if there is only enough ammo for a partial firing, and so the BeamCommand must not be used until after this method is executed, if applicable.
	 * @param entity The root entity of the firing entity.
	 * @return
	 */
	private boolean handleAmmo(BeamCommand out, ManagedUsableSegmentController<?> entity) {
		ManagerContainer<?> m = entity.getManagerContainer();
		float currCap = (out.hitType == HitType.SUPPORT && out.capacityPerTick > 0) ? m.getAmmoCapacity(REPAIR) : m.getAmmoCapacity(BEAM);
		float ticks = FastMath.floor((1.0f / out.tickRate) / out.bursttime) + out.initialTicks;
		if(currCap < out.capacityPerTick) { //not enough beam juice left to execute even one tick
			entity.sendServerMessage(Lng.astr("Beam shutdown: Particle reserves depleted.\nIt needs %1$s beam capacity to initiate its firing cycle,\nand a total of %2$s beam capacity for a complete cycle.", out.capacityPerTick, out.capacityPerTick * ticks), ServerMessage.MESSAGE_TYPE_ERROR);
			return false;
		} else {
			if(ticks * out.capacityPerTick > currCap) {
				entity.sendServerMessage(Lng.astr("Warning: Beam system shutting down early!\nYour Beam weapon needs a total of %s beam capacity for a complete cycle.", out.capacityPerTick * ticks), ServerMessage.MESSAGE_TYPE_ERROR);
				float initial = out.initialTicks * out.capacityPerTick;
				if(initial >= currCap) {
					out.bursttime = out.tickRate + 0.001f; //with most values, this means only enough time for one tick
					out.initialTicks *= initial / currCap; //literally only do initial ticks
				} else {
					out.bursttime *= out.bursttime * (out.capacityPerTick / (currCap - initial)); //if not enough available ammo to do all allotted ticks, truncate firing duration to just the possible ticks
				}
			}
			float ammoTimer;
			DamageBeamCapacityElementManager c = (DamageBeamCapacityElementManager) m.getAmmoSystem(BEAM).getElementManager();
			if(c.ammoReloadResetsOnAIFire() && entity.isAIControlled() ||
					c.ammoReloadResetsOnManualFire() && entity.isConrolledByActivePlayer()) {
				ammoTimer = c.getAmmoCapacityReloadTime();
			} else ammoTimer = m.getAmmoCapacityTimer(BEAM);
			m.setAmmoCapacity(BEAM, max(0.0f, currCap - (out.capacityPerTick * ticks)), ammoTimer, true); //consume ammo
			return true;
		}
	}

	private void checkBeamButton(BeamState beamState, BeamCommand b, KeyboardMappings m) {
		if(b.playerState != null && b.playerState.isDown(m)) {
			beamState.beamButton.add(m);
		}
	}

	private void checkBeamButtons(BeamState beamState, BeamCommand b) {
		beamState.beamButton.clear();
		checkBeamButton(beamState, b, KeyboardMappings.SHIP_PRIMARY_FIRE);
		checkBeamButton(beamState, b, KeyboardMappings.SHIP_ZOOM);
		checkBeamButton(beamState, b, KeyboardMappings.USE_SLOT_ITEM_CHARACTER);
		checkBeamButton(beamState, b, KeyboardMappings.REMOVE_BLOCK_CHARACTER);

	}

	public int beamHitNonCube(BeamState beam) {

		beam.hitOneSegment += beam.timeSpent;
		int hits = 0;



		/*
		 * if there was a lag an Timer.getDelta() gets big enough, more that one
		 * hit has to be registered
		 */
		float bhps = getBeamToHitInSecs(beam);
		hits = FastMath.fastFloor(beam.hitOneSegment / bhps);
		beam.hitOneSegment -= hits * bhps;
		cannotHitReason[0] = "";

		if(beam.initialTicks > 0) {
			hits += (int) beam.initialTicks;
			beam.initialTicks = 0;
		}
		return hits;
	}

	public abstract boolean canhit(BeamState con, SegmentController controller, String[] cannotHitReason, Vector3i position);

	/* (non-Javadoc)
	 * @see java.util.Observable#deleteObserver(java.util.Observer)
	 */
	public void clearStates() {
		beamStates.clear();
		if(drawer != null) {
			drawer.notifyDraw(this, false);
		}
		//		notifyObservers(false);
	}

	public BeamState getBeam(Vector3i elementPos) throws ElementNotFoundException {
		return getBeam(ElementCollection.getIndex(elementPos));
	}

	public synchronized BeamState getBeam(long index) throws ElementNotFoundException {
		BeamState beamState = beamStates.get(index);
		return beamState;
	}

	/**
	 * @return the beamStates
	 */
	public Long2ObjectOpenHashMap<BeamState> getBeamStates() {
		return beamStates;
	}

	public abstract float getBeamTimeoutInSecs();

	public abstract float getBeamToHitInSecs(BeamState beam);

	public boolean isAnyBeamActiveActive() {
		return !beamStates.isEmpty();
	}

	public abstract int onBeamHit(BeamState hittingBeam, int hits, BeamHandlerContainer<E> hittingContainer, SegmentPiece hitPiece, Vector3f from, Vector3f to, Timer timer, Collection<Segment> updatedSegments);

	protected abstract boolean onBeamHitNonCube(BeamState con, int hits,
	                                            BeamHandlerContainer<E> owner, Vector3f from,
	                                            Vector3f to, CubeRayCastResult cubeResult, Timer timer,
	                                            Collection<Segment> updatedSegments);

	public void setDrawer(BeamDrawer beamDrawer) {
		drawer = beamDrawer;
	}

	public abstract void transform(BeamState con);

	public void update(Timer timer) {

		if(beamStates.isEmpty()) {
			return;
		}

//		if(getBeamShooter().isOnServer()){
//			BeamState next = getBeamStates().values().iterator().next();
//			System.err.println("update beam handler: "+Vector3fTools.diffLength(next.from, next.to));
//		}
		updatedSegments.clear();
		Iterator<BeamState> iterator = beamStates.values().iterator();
		float delta = timer.getDelta();
		while(iterator.hasNext()) {
			BeamState con = iterator.next();
//			System.err.println(getBeamShooter().getState()+" BEAM "+con.from+"; "+con.to);
			if(!con.isAlive()) {
//				System.err.println(getBeamShooter().getState()+" BEAM DIED");
				iterator.remove();
				if(con.latchOn && fromObj instanceof SegmentController && fromObj.isOnServer()) {
					((SegmentController) fromObj).sendBeamLatchOn(con.identifyerSig, 0, Long.MIN_VALUE);
				}
			} else {

				con.from.set(con.relativePos);
				transform(con);
				start.set(con.from);
				end.set(con.to);
				updateBeam(start, end, con, timer, updatedSegments, fromObj.getState().getUpdateTime());
				if(!con.isAlive()) {

					iterator.remove();
					if(con.latchOn && fromObj instanceof SegmentController && fromObj.isOnServer()) {
						((SegmentController) fromObj).sendBeamLatchOn(con.identifyerSig, 0, Long.MIN_VALUE);
					}
				}
			}
			con.timeRunningSinceLastUpdate += delta;
			con.timeRunning += delta;
		}

		//update Counding box as a batch instead of doing it
		//with removeElement, which causes lots of overhead
		//when a lot of stuff is removed at once
		for(Segment s : updatedSegments) {
			if(s.getSegmentData() != null) {
				s.getSegmentData().restructBB(true);
			}
		}
		updatedSegments.clear();

		if(lastActive != isAnyBeamActiveActive()) {
//						System.err.println("[BEAM UDPATE] NOTIFYING: "+isAnyBeamActiveActive()+" "+drawer+" on "+this+"; "+getBeamShooter().getState());
			if(drawer != null) {

				drawer.notifyDraw(this, isAnyBeamActiveActive());
			}
			//			notifyObservers(isAnyBeamActiveActive());
		}
		lastActive = isAnyBeamActiveActive();
		//		System.err.println("beamupdate took "+t+"ms");
	}

	public void updateBeam(Vector3f from, Vector3f to, BeamState bState, Timer timer, Collection<Segment> updatedSegments, long time) {
		if(owner != null && owner.getState() instanceof ClientState && fromObj != null && fromObj instanceof Identifiable && !((GameClientState) owner.getState()).getCurrentSectorEntities().containsKey(((Identifiable) fromObj).getId())) {
			bState.markDeath = true;
			return;
		}
		E controller = fromObj;

		rayCallback.setDebug(false);

		if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()) {
			CollisionWorld.ClosestRayResultCallback result = controller.getPhysics().testRayCollisionPoint(from,
					to, false, controller, null, true, true, true);

			if(result.hasHit() && result.collisionObject != null) {

				DebugPoint n = new DebugPoint(result.hitPointWorld, fromObj.isOnServer() ? new Vector4f(1, 0, 1, 1) : new Vector4f(0, 1, 0, 0.1f));
				DebugDrawer.points.add(n);

			} else {
				DebugPoint n = new DebugPoint(to, fromObj.isOnServer() ? new Vector4f(0, 0, 1, 1) : new Vector4f(1, 0, 1, 0.1f));
				DebugDrawer.points.add(n);
			}

		}

		bState.timeSpent += timer.getDelta();
		bState.hitBlockTime += timer.getDelta();

		if(!controller.isOnServer() && ClientController.hasGraphics(controller.getState()) &&
				!((GameClientState) controller.getState()).getWorldDrawer().getShards().isEmpty()) {

			CollisionWorld.ClosestRayResultCallback result = controller.getPhysics().testRayCollisionPoint(from,
					to, false, controller, null, ignoreNonPhysical(bState), false, true);

			if(result.hasHit() && result.collisionObject != null && result.collisionObject instanceof RigidDebrisBody) {
				((RigidDebrisBody) result.collisionObject).shard.kill();
			}

		}
		if(isDamagingMines(bState) && controller.isOnServer()) {
			checkMines(from, to, bState, controller);
		}
		long diff = time - bState.fireStart;
		long tickrateMs = (long) (bState.getTickRate() * 1000);

		if(bState.initialTicks > 0 || diff > tickrateMs) {
			//actual tick update. Do what the beam does
			doTicks(bState, diff, tickrateMs, from, to, timer, time, controller);
		} else {
			//not a tick. update beam connection only
			if(!fromObj.isOnServer()) {
				doGraphicalUpdate(bState, diff, tickrateMs, from, to, timer, time, controller);
			}
		}
	}

	protected boolean isDamagingMines(BeamState bState) {
		return false;
	}

	private void updateFromNTLatch(BeamState bState, ManagerContainer.ReceivedBeamLatch d) {
		if(d.blockPos == Long.MIN_VALUE) {
			bState.currentHit = null;
			bState.hitPoint = null;
			bState.cachedLastSegment = null;
			bState.p1.reset();
			bState.p2.reset();
			bState.cachedLastPos.set((byte) -1, (byte) -1, (byte) -1);
			bState.firstLatch = Long.MIN_VALUE;
		} else {
			Sendable sen = fromObj.getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(d.objId);
			if(sen instanceof SegmentController c) {

				bState.currentHit = c.getSegmentBuffer().getPointUnsave(d.blockPos, new SegmentPiece());
				bState.currentHit.getWorldPos(bState.to, fromObj.getSectorId());
				if(bState.hitPoint == null) {
					bState.hitPoint = new Vector3f();
				}
				bState.hitPoint.set(bState.to);
				bState.p1.setByReference(bState.currentHit);
				bState.currentHit = bState.p1;
				bState.p2.setByReference(bState.currentHit);
				bState.cachedLastPos.set(bState.currentHit.x, bState.currentHit.y, bState.currentHit.z);
			}
		}

	}

	private void doTicks(BeamState bState, long diff, long tickrateMs, Vector3f from, Vector3f to, Timer timer, long time, E shooterController) {
		int ticks;

		//calculate ticks to do in this update.
		//all inital ticks plus ticks accumulated over time
		if(bState.initialTicks > 0) {
			ticks = (int) bState.initialTicks;
			bState.initialTicks = 0;
		} else {
			///INSERTED CODE
			//by Ithirahad
			//original: ticks = (int) (diff / tickrateMs);
			if(bState.hitType == HitType.WEAPON) {
				ticks = Math.min((int) (diff / tickrateMs), bState.ticksToDo);
			} else ticks = (int) (diff / tickrateMs);

			//originally this took the diff/tickrate no matter what - so a fast tickrate and short burst time could give large excesses of damage on the last tick in laggy scenarios
			//this is only a partial fix though - more damage in one tick is still better than several longer ticks under the exponential armor formula
			//also, not certain why but enacting this change for all beams broke salvage (and potentially other beams), so it's limited to damage beams for now.
			///
			bState.fireStart = time;
			bState.ticksToDo -= ticks;
		}

		bState.ticksDone += ticks;

		if(bState.oldPower) {
			//only consume power here for non reactor ships
			float poweConsumptionPerTick = (bState.powerConsumptionPerTick + (bState.powerConsumptionPerTick * bState.powerConsumptionExtraPerTick));
			float powerConsumption = ticks * poweConsumptionPerTick;

			if(powerConsumption > 0) {
				boolean powerForTicksDepleated = false;

				//a faster way would be to divide but we need a power
				//value representation that would include a possible mothership
				while(ticks > 0 && !bState.reloadCallback.canConsumePower(powerConsumption)) {
					ticks--; //try with less ticks
					powerConsumption = ticks * poweConsumptionPerTick;
					powerForTicksDepleated = true;
				}

				if(ticks <= 0) {
					bState.markDeath = true;
					return;
				}

				bState.totalConsumedPower += powerConsumption;
				if(!bState.reloadCallback.consumePower(powerConsumption)) {
					bState.markDeath = true;
					return;
				}
			}
		}
//		System.err.println("PPP;; "+bState.latchOn+"; "+bState.checkLatchConnection+"; "+bState.currentHit+"; ");
		boolean hit = false;
		if(bState.latchOn && bState.currentHit != null) {
			bState.currentHit.getWorldPos(bState.to, fromObj.getSectorId());
			to.set(bState.to);

			short oldType = bState.currentHit.getType();
			if(!bState.currentHit.isValid() || bState.currentHit.isDead()) {
//				System.err.println(getBeamShooter().getState()+" NEXT LATCH BECAUSE DED");
				checkNextLatch(oldType, bState, from, to);
			}
			if(bState.checkLatchConnection) {
				//check if we have visible connection to the latch
				testRayOnOvelappingSectors(from, to, bState, shooterController);

				if(bState.currentHit != null && rayCallback.getSegment() != null) {

					if(!bState.currentHit.equalsSegmentPos(rayCallback.getSegment(), rayCallback.getCubePos())) {
						//latch onto new
						bState.currentHit.setByReference(rayCallback.getSegment(), rayCallback.getCubePos());
						bState.firstLatch = bState.currentHit.getAbsoluteIndex();
						bState.currentHit.getWorldPos(bState.to, fromObj.getSectorId());
						bState.hitPoint.set(bState.to);
						bState.p1.setByReference(bState.currentHit);
						bState.currentHit = bState.p1;
						bState.p2.setByReference(bState.currentHit);
						bState.cachedLastPos.set(bState.currentHit.x, bState.currentHit.y, bState.currentHit.z);
						bState.hitPoint.set(bState.to);
						to.set(bState.to);
					}
					bState.hitPoint.set(bState.to);
					bState.hitSectorId = fromObj.getSectorId();
					bState.hitNormalWorld.set(rayCallback.hitNormalWorld);
					bState.hitNormalRelative.set(rayCallback.hitNormalWorld);
					rayCallback.getSegment().getSegmentController().getWorldTransformInverse().basis.transform(bState.hitNormalRelative);
					boolean nextLatch = checkNextLatch(oldType, bState, from, to);
					if(nextLatch) {
						//					System.err.println(getBeamShooter().getState()+" NEXT LATCH BECUASE PHYS TEST");
					}
				} else {

					//				assert(false);
					//				bState.currentHit = null;
					//				bState.hitPoint = null;
					//				bState.cachedLastSegment = null;
					//				bState.p1.reset();
					//				bState.p2.reset();
					//				bState.cachedLastPos.set((byte) -1, (byte) -1, (byte) -1);
					//				bState.firstLatch = Long.MIN_VALUE;
				}
			} else {
				if(bState.currentHit != null) {
					if(Vector3fTools.diffLength(bState.from, bState.to) > bState.beamLength) {
						//lost beam due to being too far from latched on target
						System.err.println("lost beam due to being too far from latched on target");
						bState.currentHit = null;
						bState.hitPoint = null;
						bState.hitSectorId = -1;
						bState.cachedLastSegment = null;
						bState.p1.reset();
						bState.p2.reset();
						bState.cachedLastPos.set((byte) -1, (byte) -1, (byte) -1);
						bState.firstLatch = Long.MIN_VALUE;
					}
				}
//				if(bState.currentHit != null) {
//					if(getBeamShooter().isOnServer()) {
//						System.err.println("MAINTAIN HIT ON "+bState.currentHit.getAbsolutePos(new Vector3i())+"; ");
//					}
//				}else {
//					if(getBeamShooter().isOnServer()) {
//						System.err.println("LOST HIT; ");
//					}
//				}
			}
//			System.err.println(getBeamShooter().getState()+" HITTTING "+bState.currentHit+"; "+bState.currentHit.getSegment());
			if(bState.currentHit != null) {
				onCubeStructureHit(bState, ticks, from, to, timer, time, shooterController, bState.currentHit);//2nd
				hit = true;
			} else {
				hit = false;
			}
		}
//		System.err.println(getBeamShooter().getState()+" HIT:::: "+hit+"; "+bState.currentHit);
		if(!hit) {

			//do a multisector ray cast. this traverses on all sectors overlapping with the ray
			testRayOnOvelappingSectors(from, to, bState, shooterController);

			if(rayCallback.hasHit()) {
				//we hit something
				bState.hitSectorId = fromObj.getSectorId();

//				if(isOnServer() && rayCallback.getSegment() != null) {
//
//					Sector s = ((GameServerState)getState()).getUniverse().getSector(getBeamShooter().getSectorId());
//					if(s != null) {
//						inTo.setIdentity();
//						inTo.origin.set(rayCallback.hitPointWorld);
//
//						SimpleTransformableSendableObject.calcWorldTransformRelative(
//								s.getId(), s.pos, rayCallback.getSegment().getSegmentController().getSectorId(),
//								inTo, getState(), true, outTo, v);
//
//						rayCallback.hitPointWorld.set(outTo.origin);
//					}
//				}

				bState.hitPointCache.set(rayCallback.hitPointWorld);
				bState.hitNormalWorld.set(rayCallback.hitNormalWorld);
				bState.hitNormalRelative.set(rayCallback.hitNormalWorld);
				if(rayCallback.getSegment() != null && rayCallback.getSegment().getSegmentController().getWorldTransformInverse() != null) {
					rayCallback.getSegment().getSegmentController().getWorldTransformInverse().basis.transform(bState.hitNormalRelative);
				}

				bState.hitPoint = bState.hitPointCache;
				if(rayCallback.getSegment() != null) {
					CubeRayCastResult cubeResult = rayCallback;
					SegmentController segmentController = cubeResult.getSegment().getSegmentController();

					if(fromObj.isOnServer()) {
						segmentController.calcWorldTransformRelative(fromObj.getSectorId(), ((GameServerState) segmentController.getState()).getUniverse().getSector(fromObj.getSectorId()).pos);
						bState.initalRelativeTranform.set(fromObj.getWorldTransformInverse());
						bState.initalRelativeTranform.mul(segmentController.getClientTransform());
					}

					bState.p1.setByReference(cubeResult.getSegment(), cubeResult.getCubePos());
					bState.currentHit = bState.p1;
					bState.p2.setByReference(cubeResult.getSegment(), cubeResult.getCubePos());
					bState.segmentHit = bState.p2;
					bState.cachedLastPos.set(cubeResult.getCubePos());

					if(bState.currentHit != null) {
						bState.currentHit.getWorldPos(bState.to, fromObj.getSectorId());
					}
					short oldType = bState.currentHit.getType();
					hit = onCubeStructureHit(bState, ticks, from, to, timer, time, shooterController, bState.currentHit);//1st
					bState.firstLatch = bState.currentHit.getAbsoluteIndex();

					if(bState.latchOn) {
						checkNextLatch(oldType, bState, from, to);
					}

				} else if(rayCallback instanceof CubeRayCastResult && rayCallback.getSegment() == null) {
					CubeRayCastResult cubeResult = rayCallback;
					//hit a non-block structure
					hit = onBeamHitNonCube(bState, ticks, owner, from, to, cubeResult, timer, updatedSegments);
					if(!fromObj.isOnServer() && bState.hitPoint != null) {
						bState.hitPoint.sub(from);
						dirTmp.set(to);
						dirTmp.sub(from);
						dirTmp.normalize();
						dirTmp.scale(bState.hitPoint.length());
						dirTmp.add(from);
						bState.hitPoint.set(dirTmp);
					}
				}
			} else {
				bState.reset();
			}
		}
		if(!hit) {
			bState.reset();

		}
	}

	private boolean checkNextLatch(short oldType, BeamState bState, Vector3f from, Vector3f to) {
		bState.currentHit.refresh();

//		System.err.println(getBeamShooter().getState()+" CURRENT: "+bState.currentHit);

		if(!bState.currentHit.isValid() || bState.currentHit.isDead()) {
			//latch onto next block
			bState.currentHit = getBeamLatchTransitionInterface().selectNextToLatch(bState, oldType, bState.firstLatch, bState.currentHit.getAbsoluteIndex(), from, to, this, bState.currentHit.getSegmentController());
//			System.err.println(getBeamShooter().getState()+" LATCH ON TO "+bState.currentHit);
			if(bState.currentHit != null) {
				bState.currentHit.getWorldPos(bState.to, fromObj.getSectorId());
				bState.hitSectorId = fromObj.getSectorId();
				bState.hitPoint.set(bState.to);
				bState.p1.setByReference(bState.currentHit);
				bState.currentHit = bState.p1;
				bState.p2.setByReference(bState.currentHit);
				bState.cachedLastPos.set(bState.currentHit.x, bState.currentHit.y, bState.currentHit.z);
				bState.hitPoint.set(bState.to);
				to.set(bState.to);

				if(fromObj instanceof SegmentController && fromObj.isOnServer()) {
					((SegmentController) fromObj).sendBeamLatchOn(bState.identifyerSig, bState.currentHit.getSegmentController().getId(), bState.currentHit.getAbsoluteIndex());
				}
				return true;
			} else {
				//no next block found. reset rest
				bState.reset();
			}
		}
		return false;
	}

	private void onCubeStructureHitSingle(BeamState bState, int ticks, Vector3f from, Vector3f to, Timer timer, long time, E controller, SegmentPiece currentHit) {
		SegmentController segmentController = currentHit.getSegmentController();
		if(owner != null && owner.getHandler() != null) {
			Damager damager = owner.getHandler().fromObj;

			if(damager != null && damager instanceof SegmentController shooter) {
				SegmentController target = segmentController;
				if(shooter.railController.isChildDock(target)) {
					if(bState.railChild < 1.0 && !shooter.isOnServer()) {
						shooter.popupOwnClientMessage(Lng.str("Hitting rail docks with reduced efficiency (%s%%)", StringTools.formatPointZero(bState.railChild * 100.0d)), ServerMessage.MESSAGE_TYPE_ERROR);
					}
					bState.setPower((float) (bState.getPower() * bState.railChild));
				} else if(shooter.railController.isParentDock(target)) {
					if(bState.railParent < 1.0 && !shooter.isOnServer()) {
						shooter.popupOwnClientMessage(Lng.str("Hitting rail docks with reduced efficiency (%s%%)", StringTools.formatPointZero(bState.railParent * 100.0d)), ServerMessage.MESSAGE_TYPE_ERROR);
					}
					bState.setPower((float) (bState.getPower() * bState.railParent));
				}
			}
		}

		int hits = onBeamHit(bState, ticks, owner, currentHit, from, to, timer, updatedSegments);
		if(!bState.latchOn && !fromObj.isOnServer() && bState.hitPoint != null) {
			bState.hitPoint.sub(from);
			dirTmp.set(to);
			dirTmp.sub(from);
			dirTmp.normalize();
			dirTmp.scale(bState.hitPoint.length());
			dirTmp.add(from);
			bState.hitPoint.set(dirTmp);

		}
	}

	private final SegmentPiece tmpHit = new SegmentPiece();
	private Int2ObjectOpenHashMap<BlockRecorder> blockRecorder;

	private boolean onCubeStructureHit(BeamState bState, int ticks, Vector3f from, Vector3f to, Timer timer, long time, E controller, SegmentPiece currentHit) {

		SegmentController segmentController = currentHit.getSegmentController();
		bState.cachedLastSegment = segmentController;

		cannotHitReason[0] = "";
		if(canhit(bState, segmentController, cannotHitReason,
				currentHit.getAbsolutePos(tmp))) {

			if(bState.penetrating) {
				//transform beam so it is relative to the object
				if(segmentController.getSectorId() != owner.getSectorId()) {

					Vector3i sector = segmentController.getSector(new Vector3i());
					if(sector != null) {
						inFrom.origin.set(from);
						inTo.origin.set(to);

						SimpleTransformableSendableObject.calcWorldTransformRelative(
								segmentController.getSectorId(), sector, controller.getSectorId(),
								inFrom, controller.getState(), controller.isOnServer(), outFrom, v);
						SimpleTransformableSendableObject.calcWorldTransformRelative(
								segmentController.getSectorId(), sector, controller.getSectorId(),
								inTo, controller.getState(), controller.isOnServer(), outTo, v);

					}
				} else {
					outFrom.origin.set(from);
					outTo.origin.set(to);
				}
				if(blockRecorder == null) {
					blockRecorder = new Int2ObjectOpenHashMap<BlockRecorder>();
				}
				testRay(from, to, bState, segmentController, blockRecorder, 100000, (ModifiedDynamicsWorld) segmentController.getPhysics().getDynamicsWorld());

				if(rayCallback.hasHit()) {

					BlockRecorder br = blockRecorder.get(segmentController.getId());
					if(br != null && br.size() > 0) {
						int size = br.size();
						float damageFull = bState.getPower();
						//divide damage up to blocks hit
						bState.setPower(damageFull / size);
						for(int i = 0; i < size; i++) {
							int localIndex = br.blockLocalIndices.getInt(i);

							tmpHit.setByReference(br.datas.get(i).getSegment(),
									SegmentData.getPosXFromIndex(localIndex),
									SegmentData.getPosYFromIndex(localIndex),
									SegmentData.getPosZFromIndex(localIndex));
//							System.err.println("Penetrating beam hit on "+tmpHit);
							if((int) bState.getPower() == 0) {
								break;
							}
							onCubeStructureHitSingle(bState, ticks, from, to, timer, time, controller, tmpHit);
						}
						//reset block damage
						bState.setPower(damageFull);
					} else {
						System.err.println("WARNING: No block Recorder when there should be one for id " + segmentController.getId() + ": size: " + blockRecorder.size() + "; " + blockRecorder);
					}

					tmpHit.reset();
				}
				for(BlockRecorder r : blockRecorder.values()) {
					r.free();
				}
				blockRecorder.clear();
			} else {
				onCubeStructureHitSingle(bState, ticks, from, to, timer, time, controller, currentHit);
			}

			return true;
		} else {
//			System.err.println("NOT HIT "+isOnServer()+"; "+Arrays.toString(cannotHitReason));
			if(!segmentController.isOnServer()) {

				if(((GameClientState) fromObj.getState()).getCurrentPlayerObject() == fromObj) {
					((GameClientController) (segmentController.getState().getController()))
							.popupInfoTextMessage(Lng.str("%s\ncannot be hit by this beam.\n%s", segmentController.toNiceString(), cannotHitReason[0]), "BHITTXT", 0);
				} else {
					System.err.println(((GameClientState) fromObj.getState()).getCurrentPlayerObject() + "; " + fromObj);
				}
			}
			return false;
		}

	}

	private void doGraphicalUpdate(BeamState bState, long diff, long tickrateMs, Vector3f from, Vector3f to, Timer timer, long time, E controller) {
		//to save performance, inbetween ticks, the raycast is simplified and interpolated
		if(bState.latchOn && bState.currentHit != null) {
			bState.currentHit.getWorldPos(bState.to, fromObj.getSectorId());
			bState.hitPoint.set(bState.to);
			bState.hitSectorId = fromObj.getSectorId();
		} else if(bState.penetrating && bState.hitPoint != null) {
			bState.hitPoint.set(to);
			bState.hitSectorId = fromObj.getSectorId();
		} else if(time - bState.lastCheck > 50) {

			rayCallback.setCubesOnly(isHitsCubesOnly());
			rayCallback.closestHitFraction = 1.0f;
			rayCallback.rayFromWorld.set(from);
			rayCallback.rayToWorld.set(to);

			rayCallback.collisionObject = null;
			rayCallback.setSegment(null);
			rayCallback.setDamageTest(false);
			rayCallback.setIgnoereNotPhysical(false);
			rayCallback.setIgnoreDebris(true);
			rayCallback.setZeroHpPhysical(true);
			rayCallback.setHasCollidingBlockFilter(false);
			rayCallback.setCollidingBlocks(null);
			rayCallback.setSimpleRayTest(false);
			controller.getPhysics().getDynamicsWorld().rayTest(from, to, rayCallback);

			if(rayCallback.hasHit()) {
				bState.hitPoint = new Vector3f(rayCallback.hitPointWorld);
				bState.hitSectorId = fromObj.getSectorId();
				if(rayCallback instanceof CubeRayCastResult && rayCallback.getSegment() != null) {
					CubeRayCastResult cubeResult = rayCallback;
					bState.currentHit = new SegmentPiece(cubeResult.getSegment(), cubeResult.getCubePos());
					bState.segmentHit = new SegmentPiece(cubeResult.getSegment(), cubeResult.getCubePos());
				}
			} else {
				bState.currentHit = null;
				bState.hitPoint = null;
				bState.hitSectorId = -1;
				bState.cachedLastSegment = null;
				bState.cachedLastPos.set((byte) -1, (byte) -1, (byte) -1);

				bState.segmentHit = null;
				bState.hitOneSegment = 0;
				bState.hitBlockTime = 0;
				bState.timeSpent = 0;
			}
			bState.lastCheck = timer.currentTime;
		} else if(bState.hitPoint != null) {
			//don't change bState.hitSectorId since its still relying on the hit of the last raycast
			tmpVal.sub(bState.hitPoint, from);
			dirTmp.set(to);
			dirTmp.sub(from);
			dirTmp.normalize();
			dirTmp.scale(tmpVal.length());
			dirTmp.add(from);

			bState.hitPoint.interpolate(dirTmp, timer.getDelta());
		}

	}

	private final TransformaleObjectTmpVars v = new TransformaleObjectTmpVars();
	private final Transform inFrom = new Transform();
	private final Transform inTo = new Transform();
	private final Transform inRes = new Transform();
	private final Transform outFrom = new Transform();
	private final Transform outTo = new Transform();
	private final Transform outRes = new Transform();

	private class BigTrav implements SegmentTraversalInterface<BigTrav> {

		public Vector3f from;
		public Vector3f to;

		public BeamState con;
		public E controller;
		private final Vector3i secPosTmp = new Vector3i();
		private final Vector3i ownSectorPos = new Vector3i();

		public BigTrav() {
			inFrom.setIdentity();
			inTo.setIdentity();
		}

		@Override
		public boolean handle(int x, int y, int z, RayTraceGridTraverser traverser) {
			assert (controller.isOnServer());
			if(!(Math.abs(x) < 2 && Math.abs(y) < 2 && Math.abs(z) < 2 && !rayCallback.hasHit())) {
				return false;
			} else if(x == 0 && y == 0 && z == 0) {
				return true;
			}
			secPosTmp.set(ownSectorPos.x + x, ownSectorPos.y + y, ownSectorPos.z + z);
			Sector s = ((GameServerState) controller.getState()).getUniverse().getSectorWithoutLoading(secPosTmp);
			if(s != null) {
				inFrom.origin.set(from);
				inTo.origin.set(to);

				SimpleTransformableSendableObject.calcWorldTransformRelative(
						s.getId(), s.pos, controller.getSectorId(),
						inFrom, controller.getState(), true, outFrom, v);

				SimpleTransformableSendableObject.calcWorldTransformRelative(
						s.getId(), s.pos, controller.getSectorId(),
						inTo, controller.getState(), true, outTo, v);

				testRay(outFrom.origin, outTo.origin, con, ((ModifiedDynamicsWorld) s.getPhysics().getDynamicsWorld()));

				if(rayCallback.hasHit()) {
					//result was taken in sector of target
					//transform result back to the space of the beam shooter
					Sector own = ((GameServerState) controller.getState()).getUniverse().getSectorWithoutLoading(ownSectorPos);

					inRes.setIdentity();
					inRes.origin.set(rayCallback.hitPointWorld);

					SimpleTransformableSendableObject.calcWorldTransformRelative(
							own.getId(), own.pos, s.getSectorId(),
							inRes, controller.getState(), true, outRes, v);
					rayCallback.hitPointWorld.set(outRes.origin);
//					System.err.println("NORMALIZED FOR SECTOR CAST");
					return false;
				}

			}

			return true;
		}

		@Override
		public BigTrav getContextObj() {
			return this;
		}
	}

	private class BigTravMines implements SegmentTraversalInterface<BigTravMines> {

		public Vector3f from;
		public Vector3f to;
		Transform inFrom = new Transform();
		Transform inTo = new Transform();
		Transform outFrom = new Transform();
		Transform outTo = new Transform();
		public BeamState con;
		public E controller;
		private final Vector3i ownSectorPos = new Vector3i();
		private final TransformaleObjectTmpVars v = new TransformaleObjectTmpVars();
		private MineController mineController;
		private GameServerState state;
		private final Vector3i secTmp = new Vector3i();

		public void init() {
			state = ((GameServerState) getBeamShooter().getState());
			mineController = state.getController().getMineController();
		}

		public BigTravMines() {
			inFrom.setIdentity();
			inTo.setIdentity();

		}

		@Override
		public boolean handle(int x, int y, int z, RayTraceGridTraverser traverser) {
			secTmp.set(x, y, z);
			secTmp.add(ownSectorPos);
			Sector sec = state.getUniverse().getSectorWithoutLoading(secTmp);
			if(sec != null) {
				mineController.handleHit(sec.getId(), from, to);
			}

			return true;
		}

		@Override
		public BigTravMines getContextObj() {
			return this;
		}
	}

	RayCubeGridSolver raySolver = new RayCubeGridSolver();
	BigTrav bt = new BigTrav();
	BigTravMines btMines = new BigTravMines();

	private void checkMines(Vector3f from, Vector3f to, BeamState con, E controller) {
		btMines.from = from;
		btMines.to = to;
		btMines.con = con;
		btMines.controller = controller;
		btMines.init();
		btMines.ownSectorPos.set(((GameServerState) controller.getState()).getUniverse().getSector(controller.getSectorId()).pos);
		//FIXME planet sectors aren't looked at

		float sSize = ((GameStateInterface) controller.getState()).getSectorSize();
		RayCubeGridSolver.sectorInv = 1.0f / sSize;
		RayCubeGridSolver.sectorHalf = sSize * 0.5f;
		raySolver.initializeSectorGranularity(from, to, TransformTools.ident);
		raySolver.traverseSegmentsOnRay(btMines);
	}

	CubeRayCastResult testRayOnOvelappingSectors(Vector3f from, Vector3f to,
	                                             BeamState con, E controller) {

		/**
		 * IMPORTANT:
		 * objects on the border of sectors are duplicated via virtual objects on server.
		 * therefore no check on other physics contexts is necessary.
		 * Objects further away need to be checked by casting the ray into the other sector.
		 *
		 * The result will be tranformed to the space of the shooter so all results are consistent
		 * in the shooter space.
		 *
		 *
		 */

		testRay(from, to, con, ((ModifiedDynamicsWorld) controller.getPhysics().getDynamicsWorld()));

		if(!rayCallback.hasHit() && controller.isOnServer()) {

			//test for other sectors on servers. since virtual objects only load if overlapping with sector border

			bt.from = from;
			bt.to = to;
			bt.con = con;
			bt.controller = controller;
			bt.ownSectorPos.set(((GameServerState) controller.getState()).getUniverse().getSector(controller.getSectorId()).pos);
			//FIXME planet sectors aren't looked at

			float sSize = ((GameStateInterface) controller.getState()).getSectorSize();
			RayCubeGridSolver.sectorInv = 1.0f / sSize;
			RayCubeGridSolver.sectorHalf = sSize * 0.5f;
			raySolver.initializeSectorGranularity(from, to, TransformTools.ident);
			raySolver.traverseSegmentsOnRay(bt);
		}
		return rayCallback;
	}

	private boolean testRay(Vector3f from, Vector3f to, BeamState con, ModifiedDynamicsWorld world) {
		return testRay(from, to, con, null, null, 0, world);
	}

	private boolean testRay(Vector3f from, Vector3f to, BeamState con, SegmentController filter, Int2ObjectOpenHashMap<BlockRecorder> blockRecorderMap, int blockDeepness, ModifiedDynamicsWorld world) {
		rayCallback.setCubesOnly(isHitsCubesOnly());
		rayCallback.closestHitFraction = 1.0f;
		rayCallback.rayFromWorld.set(from);
		rayCallback.rayToWorld.set(to);
		rayCallback.collisionObject = null;
		rayCallback.setSegment(null);
		rayCallback.setDamageTest(affectsTargetBlock());
		rayCallback.setIgnoereNotPhysical(ignoreNonPhysical(con));
		rayCallback.setIgnInterface(this);
		rayCallback.setIgnoreDebris(true);
		rayCallback.setZeroHpPhysical(isConsiderZeroHpPhysical());
		rayCallback.setHasCollidingBlockFilter(false);
		rayCallback.setCollidingBlocks(null);
		rayCallback.setSimpleRayTest(true);
		rayCallback.setHitNonblockCallback(this);
		rayCallback.power = con.getPower();
		if(filter != null) {
			rayCallback.setFilter(filter);
		} else {
			rayCallback.setFilter();
		}

		if(blockRecorderMap != null) {
			rayCallback.setRecordAllBlocks(true);
			rayCallback.setRecordedBlocks(blockRecorderMap, blockDeepness);
		} else {
			rayCallback.setRecordAllBlocks(false);
			rayCallback.setRecordedBlocks(null, 0);
		}
		world.rayTest(from, to, rayCallback);

		con.armorValue.reset();
		if(rayCallback.hasHit() && rayCallback.getSegment() != null && !con.ignoreArmor) {
			ArmorValue armorInfo = retrieveArmorInfo(rayCallback.getSegment().getSegmentController(), from, to);
			con.armorValue.set(armorInfo);
		}

		return rayCallback.hasHit();
	}

	private final ArmorCheckTraverseHandler pt = new ArmorCheckTraverseHandler();
	private final CubeRayCastResult rayCallbackTraverse = new CubeRayCastResult(new Vector3f(), new Vector3f(), null) {

		@Override
		public InnerSegmentIterator newInnerSegmentIterator() {

			return pt;
		}

	};
	private final ArmorValue armorValue = new ArmorValue();

	private ArmorValue retrieveArmorInfo(SegmentController c, Vector3f from, Vector3f to) {

		rayCallbackTraverse.closestHitFraction = 1.0f;
		rayCallbackTraverse.collisionObject = null;
		rayCallbackTraverse.setSegment(null);

		rayCallbackTraverse.rayFromWorld.set(from);
		rayCallbackTraverse.rayToWorld.set(to);

		rayCallbackTraverse.setFilter(c); //filter for performance since inital check already succeeded
		rayCallbackTraverse.setOwner(null);
		rayCallbackTraverse.setIgnoereNotPhysical(false);
		rayCallbackTraverse.setIgnoreDebris(false);
		rayCallbackTraverse.setRecordAllBlocks(false);
		rayCallbackTraverse.setZeroHpPhysical(false); //dont hit 0 hp blocks
		rayCallbackTraverse.setDamageTest(true);
		rayCallbackTraverse.setCheckStabilizerPaths(false); //hit stablizer paths
		rayCallbackTraverse.setSimpleRayTest(true);

		armorValue.reset();
		pt.armorValue = armorValue;
		c.getPhysics().getDynamicsWorld().rayTest(from, to, rayCallbackTraverse);

		if(armorValue.typesHit.size() > 0) {
			armorValue.calculate();
		}

		rayCallbackTraverse.collisionObject = null;
		rayCallbackTraverse.setSegment(null);
		rayCallbackTraverse.setFilter();

		return armorValue;

	}

	@Override
	public boolean onHit(CollisionObject obj, float damage) {
		if(canHit(obj) && obj.getUserPointer() instanceof StabilizerPath) {
			((StabilizerPath) obj.getUserPointer()).onHit(owner, damage);
			return false;
		}
		return true;
	}

	protected boolean canHit(CollisionObject obj) {
		return true;
	}

	public boolean ignoreBlockType(short type) {
		return false;
	}

	protected boolean isHitsCubesOnly() {
		return false;
	}

	public boolean affectsTargetBlock() {
		return true;
	}

	protected boolean isConsiderZeroHpPhysical() {
		return true;
	}

	protected boolean ignoreNonPhysical(BeamState con) {
		return true;
	}

	public boolean drawBlockSalvage() {
		return false;
	}

	public final Vector4f getColor(BeamState beamState) {
		if((owner) instanceof ColorBeamInterface && ((ColorBeamInterface) owner).hasCustomColor()) {
			return ((ColorBeamInterface) owner).getColor();
		} else {
			return getDefaultColor(beamState);
		}
	}

	protected abstract Vector4f getDefaultColor(BeamState beamState);

	@Override
	public boolean ignoreBlock(short type) {
		return false;
	}

	public void sendClientMessage(String str, byte type) {
		if(fromObj != null) {
			fromObj.sendClientMessage(str, type);
		}
	}

	public void sendServerMessage(Object[] astr, byte msgType) {
		if(fromObj != null) {
			fromObj.sendServerMessage(astr, msgType);
		}
	}

	public float getDamageGivenMultiplier() {
		if(fromObj != null) {
			return fromObj.getDamageGivenMultiplier();
		}
		return 1;
	}

	public abstract boolean isUsingOldPower();

	public abstract BeamLatchTransitionInterface getBeamLatchTransitionInterface();

	public boolean handleBeamLatch(ManagerContainer.ReceivedBeamLatch d) {
		BeamState beamState = beamStates.get(d.beamId);
		if(beamState != null) {
			updateFromNTLatch(beamState, d);
			return true;
		}
		return false;

	}

	public void onArmorBlockKilled(BeamState hittingBeam, float armorValue) {

	}

}
