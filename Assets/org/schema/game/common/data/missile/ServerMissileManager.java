package org.schema.game.common.data.missile;

import java.util.Comparator;

import javax.vecmath.Vector3f;

import org.schema.common.util.CompareTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.ClientChannel;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.data.missile.updates.MissileSpawnUpdate;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SectorNotFoundRuntimeException;
import org.schema.game.network.objects.NetworkClientChannel;
import org.schema.game.network.objects.remote.RemoteMissileUpdate;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.RegisteredClientOnServer;
import org.schema.schine.network.objects.Sendable;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;

public class ServerMissileManager implements MissileManagerInterface{
	private final Short2ObjectOpenHashMap<Missile> missiles = new Short2ObjectOpenHashMap<Missile>();
	
	
	private ObjectRBTreeSet<Missile> sortedByDamage = new ObjectRBTreeSet<Missile>((o1, o2) -> {
		int c = CompareTools.compare(o1.getDamage(), o2.getDamage());
		return c == 0 ? CompareTools.compare(o1.getId(), o2.getId()) : c;
	});
	private final GameServerState state;

	private int ticks;
	long lastUpdate = -1;
	
	public final MissileTargetManager targetManager = new MissileTargetManager();
	
	private long accumulated;
	private int lastTickRecorded = -1;
	private int missilesSpawned;
	private long recordMissileSpawed;
	public ShortSet stalling = new ShortOpenHashSet();

	private long lastStallingMsg;


	private Transform tmp = new Transform();


	private Vector3f dist = new Vector3f(); 
	public ServerMissileManager(GameServerState state) {
		this.state = state;
	}

	public void addMissile(Missile missile) {
		missile.initPhysics();
		missiles.put(missile.getId(), missile);
		sortedByDamage.add(missile);
		missile.onSpawn();
		missile.pendingBroadcastUpdates.add(getSpawnUpdate(missile));
	}
	
	public void fromNetwork(NetworkClientChannel networkClientChannel) {
		for (int i = 0; i < networkClientChannel.missileMissingRequestBuffer
				.getReceiveBuffer().size(); i++) {
			short id = networkClientChannel.missileMissingRequestBuffer
					.getReceiveBuffer().get(i).get();
			Missile missile = missiles.get(id);
			if (missile != null) {
				System.err.println("[SERVER] Adding Requested Spawn " + missile);
				networkClientChannel.missileUpdateBuffer
						.add(new RemoteMissileUpdate(getSpawnUpdate(missile),
								networkClientChannel));
			}
		}
	}
	public Missile getLowestDamage(Damager damager, Vector3f from, float shootingDist) {
		if(!sortedByDamage.isEmpty()) {
			ObjectBidirectionalIterator<Missile> it = sortedByDamage.iterator();
			while(it.hasNext()) {
				Missile m = it.next();
				if(checkMissileRange(m, damager, from, shootingDist) ) {
					return m;
				}
			}
		}
		return null;
	}
	public Missile getHighestDamage(Damager damager, Vector3f from, float shootingDist) {
		if(!sortedByDamage.isEmpty()) {
			ObjectBidirectionalIterator<Missile> it = sortedByDamage.iterator(sortedByDamage.last());
			
			while(it.hasPrevious()) {
				Missile m = it.previous();
				if(checkMissileRange(m, damager, from, shootingDist) ) {
					return m;
				}
			}
		}
		return null;
	}
	private boolean checkMissileRange(Missile m, Damager damager, Vector3f from, float shootingDist) {
		
		Sector s = state.getUniverse().getSector(damager.getSectorId());
		if(s == null || !m.isInNeighborSecorToServer(s.pos)) {
			return false;
		}
		
		boolean ownMissile = m.getOwner().isSegmentController() && damager instanceof SegmentController && ((SegmentController) m.getOwner()).railController.isInAnyRailRelationWith((SegmentController)damager);
		if (!ownMissile && (damager.getFactionId() == 0 || m.getOwner().getFactionId() != damager.getFactionId())) {
		
			Transform missileWorldTransform = m.getWorldTransformRelativeToSector(damager.getSectorId(), tmp);
			
			dist.sub(missileWorldTransform.origin, from);
	
			return dist.length() < shootingDist;
		}else {
			return false;
		}
	}
	
	public Missile getLowestDamage(Vector3i sec) {
		ObjectBidirectionalIterator<Missile> it = sortedByDamage.iterator();
		while(it.hasNext()) {
			Missile m = it.next();
			
			if(m.isInNeighborSecorToServer(sec)) {
				return m;
			}
		}
		return null;
	}
	public Missile getHighestDamage(Vector3i sec) {
		ObjectBidirectionalIterator<Missile> it = sortedByDamage.iterator(sortedByDamage.last());
		while(it.hasPrevious()) {
			Missile m = it.previous();
			
			if(m.isInNeighborSecorToServer(sec)) {
				return m;
			}
		}
		return null;
	}
	public MissileSpawnUpdate getSpawnUpdate(Missile missile) {
		int targetId = -1;
		
//		System.err.println("ADD MISSILE AT : "+missile.getWorldTransform().origin+"; "+missile.getInitialTransform().origin);
		
		missilesSpawned++;
		if ((missile instanceof TargetChasingMissile && ((TargetChasingMissile) missile)
				.getTarget() != null)) {
			targetId = ((TargetChasingMissile) missile).getTarget().getId();
			
			
		}
		Transform initialTargetTrans = new Transform();
		initialTargetTrans.setIdentity();
		Vector3f initialTargetPos = new Vector3f();
		Vector3f relativePos = new Vector3f();
		if (missile.getType().targetChasing){
			((TargetChasingMissile) missile).startTicks = ticks;
			relativePos = ((TargetChasingMissile) missile).getTargetRelativePosition(relativePos);
			
			((TargetChasingMissile) missile).relativeTargetPos = relativePos;
			initialTargetTrans = ((TargetChasingMissile) missile).getTargetPosition(initialTargetTrans);
			initialTargetPos.set(relativePos);
			initialTargetTrans.transform(initialTargetPos);
			
			MissileTargetPosition p = new MissileTargetPosition();
			p.targetPosition = new Vector3f(initialTargetPos);
			p.time = missile.spawnTime + TargetChasingMissile.UPDATE_LEN;
			((TargetChasingMissile) missile).targetPositions.add(p);
			
			
		}
		float activationTimer = 0;
		if (missile.getType().activationTime){
			activationTimer = ((ActivationMissileInterface)missile).getActivationTimer();
		}
		
		//		System.err.println("[MISSILE] "+state+" sending spawn update "+missile+";");
		MissileSpawnUpdate missileSpawnUpdate = new MissileSpawnUpdate(
				missile.getId(), missile.getDamage(), missile.getInitialTransform().origin,
				missile.getDirection(new Vector3f()), missile.getSpeed(), targetId, missile.getType(),
				missile.getSectorId(), missile.getWeaponId(), 
				missile.getColorType(), 
				missile.spawnTime, initialTargetPos, ticks, relativePos, activationTimer);
		assert (missileSpawnUpdate.missileType == missile.getType());

		return missileSpawnUpdate;
	}

	/**
	 * @return the state
	 */
	public GameServerState getState() {
		return state;
	}

	public void sendMissileUpdates() {
		
		targetManager.sendPending(state);
		
		
		int broadcasts = 0;
		int local = 0;
		for (Missile m : missiles.values()) {
			//broadcast first (add/remove)
			if (m.hasPendingBroadcastUpdates()) {
				broadcasts++;
				for (PlayerState p : state.getPlayerStatesByName().values()) {
					RegisteredClientOnServer c = state.getClients().get(
							p.getClientId());
					if (c != null) {

						Sendable sendable = c.getLocalAndRemoteObjectContainer()
								.getLocalObjects().get(0);
						if (sendable != null && sendable instanceof ClientChannel) {
							ClientChannel cc = (ClientChannel) sendable;
							m.sendPendingBroadcastUpdates(cc);
						} else {
							System.err
									.println("[SERVER] BROADCAST MISSILE UPDATE FAILED FOR "
											+ p + ": NO CLIENT CHANNEL");
						}
					} else {
						System.err.println("[SEVRER][MISSILEMAN] client for player not found: " + p);
					}
				}
			}

			if (m.hasPendingUpdates() && m.isAlive()) {
				local++;
				for (PlayerState p : m.nearPlayers()) {
					RegisteredClientOnServer c = state.getClients().get(
							p.getClientId());
					if (c != null) {
						Vector3i playerPos = p.getCurrentSector();

						Sendable sendable = c.getLocalAndRemoteObjectContainer()
								.getLocalObjects().get(0);
						if (sendable != null && sendable instanceof ClientChannel) {
							ClientChannel cc = (ClientChannel) sendable;
							m.sendPendingUpdates(cc);
						}
					} else {
						System.err.println("[SEVRER][MISSILEMAN] broadcast client for player not found: " + p);
					}
				}
			}

			m.clearAllUpdates();
		}
//		if (broadcasts > 0) {//report only on broadcasts
//			System.err.println("[SERVER] MISSILE UPDATE; Missile Count: " + missiles.size() + "; Broadcasts: " + broadcasts + "; local: " + local);
//		}
	}

	public void updateServer(Timer timer) {
		
		if(lastUpdate > 0){
			accumulated += (timer.currentTime - lastUpdate);
			ticks = (int) (accumulated /
					TargetChasingMissile.UPDATE_LEN);
		}
		lastUpdate = timer.currentTime;
		
		if(lastTickRecorded >= 0){
			//record multiple ticks
			//(needed when there was a huge lag spike)
			for(int t = lastTickRecorded+1; t <= ticks; t++){
				targetManager.record(state, t);
			}
		}else{
			targetManager.record(state, ticks);
		}
		lastTickRecorded = ticks;
		//better save than sorry. register one more tick right away, so that buffer doesnt run out
		targetManager.record(state, ticks+1);
		
		
		if(missilesSpawned > 0 && recordMissileSpawed == 0){
			recordMissileSpawed = System.currentTimeMillis();
		}else if(recordMissileSpawed > 0 && System.currentTimeMillis() - recordMissileSpawed > 20000){
			System.err.println("[SERVER] MISSILE REPORT: Spawned missiles in the last 20 seconds: "+missilesSpawned);
			missilesSpawned = 0;
			recordMissileSpawed = 0;
		}
		
		if(stalling.size() > 0 && System.currentTimeMillis() - lastStallingMsg > 5000){
			System.err.println("[SERVER] Stalling missiles in the last 5 sec: "+stalling.size());
			stalling.clear();
			lastStallingMsg = System.currentTimeMillis();
		}
		ObjectIterator<Missile> iterator = missiles.values().iterator();

		long t = System.currentTimeMillis();
		while (iterator.hasNext()) {
			Missile m = iterator.next();

			if (m.isAlive()) {
				m.updateServer(timer);

				m.nearPlayers().clear();
				for (PlayerState p : state.getPlayerStatesByName().values()) {
					if (p.getCurrentSectorId() == m.getSectorId()) {
						m.nearPlayers().add(p);
					} else {
						Sector sector = state.getUniverse().getSector(
								m.getSectorId());
						if (sector != null) {

							if (Math.abs(sector.pos.x - p.getCurrentSector().x) < 2
									&& Math.abs(sector.pos.y
									- p.getCurrentSector().y) < 2
									&& Math.abs(sector.pos.z
									- p.getCurrentSector().z) < 2) {
								m.nearPlayers().add(p);
							}

						} else {
							m.setAlive(false);
						}
					}
				}
			} else {
				try {
					m.removeFromSectorPhysicsForProjectileCheck(m.currentProjectileSector);
				} catch (SectorNotFoundRuntimeException e) {
					System.err.println("[SERVER][MISSILEMAN] WARNING: Physics for missile " + m + " has not been removed: Sector not loaded: " + e.getMessage());
				}
				iterator.remove();
				sortedByDamage.remove(m);

			}
		}
		long took = System.currentTimeMillis() - t;
		if (took > 50) {
			System.err.println("[SERVER][MISSILECONTROLLER] WARNING: update of " + missiles.size() + " took " + took + " ms");
		}

		sendMissileUpdates();
	}

	public Missile hasHit(short missileID, int projectileIdFilter, Vector3f posBeforeUpdate, Vector3f posAfterUpdate) {
		Missile missile = missiles.get(missileID);
		if (missile != null && !missile.hadHit(projectileIdFilter) && missile.hasHit(posBeforeUpdate, posAfterUpdate)) {
			return missile;
		}
		return null;
	}

	public Short2ObjectOpenHashMap<Missile> getMissiles() {
		return missiles;
	}

	public int getTicks() {
		return ticks;
	}

	



}
