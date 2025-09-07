package org.schema.game.mod;

import java.util.ArrayList;
import java.util.List;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.data.player.PlayerCharacter;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.mod.listeners.BlockInitializationListener;
import org.schema.game.mod.listeners.ModListener;
import org.schema.game.mod.listeners.NetworkInitializationListener;
import org.schema.game.mod.listeners.PlayerStateListener;
import org.schema.game.mod.listeners.SegmentControllerListener;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.objects.Sendable;

public abstract class Mod implements BlockInitializationListener, NetworkInitializationListener, PlayerStateListener, SegmentControllerListener {

	private final List<ModListener> listeners = new ArrayList<ModListener>();
	private final List<BlockInitializationListener> blockInitializationListeners = new ArrayList<BlockInitializationListener>();
	private final List<NetworkInitializationListener> networkInitializationListeners = new ArrayList<NetworkInitializationListener>();
	private final List<PlayerStateListener> playerStateListeners = new ArrayList<PlayerStateListener>();
	private final List<SegmentControllerListener> segmentControllerListeners = new ArrayList<SegmentControllerListener>();

	public synchronized void addListener(BlockInitializationListener l) {
		listeners.add(l);
		blockInitializationListeners.add(l);
	}

	public synchronized void addListener(NetworkInitializationListener l) {
		listeners.add(l);
		networkInitializationListeners.add(l);
	}

	public synchronized void addListener(PlayerStateListener l) {
		listeners.add(l);
		playerStateListeners.add(l);
	}

	public synchronized void addListener(SegmentControllerListener l) {
		listeners.add(l);
		segmentControllerListeners.add(l);
	}

	public synchronized void removeListener(ModListener l) {
		listeners.remove(l);
		blockInitializationListeners.remove(l);
		networkInitializationListeners.remove(l);
		playerStateListeners.remove(l);
		segmentControllerListeners.remove(l);
	}

	@Override
	public final void onSegmentControllerUpdate(SegmentController c) {
		for (int i = 0; i < segmentControllerListeners.size(); i++) {
			segmentControllerListeners.get(i).onSegmentControllerUpdate(c);
		}
	}

	@Override
	public final void onSegmentControllerSpawn(SegmentController c) {
		for (int i = 0; i < segmentControllerListeners.size(); i++) {
			segmentControllerListeners.get(i).onSegmentControllerSpawn(c);
		}
	}

	@Override
	public final void onSegmentControllerDelete(SegmentController c) {
		for (int i = 0; i < segmentControllerListeners.size(); i++) {
			segmentControllerListeners.get(i).onSegmentControllerDelete(c);
		}
	}

	@Override
	public final void onSegmentControllerDamageTaken(SegmentController c) {
		for (int i = 0; i < segmentControllerListeners.size(); i++) {
			segmentControllerListeners.get(i).onSegmentControllerDamageTaken(c);
		}
	}

	@Override
	public final void onSegmentControllerHitByLaser(SegmentController c) {
		for (int i = 0; i < segmentControllerListeners.size(); i++) {
			segmentControllerListeners.get(i).onSegmentControllerHitByLaser(c);
		}
	}

	@Override
	public final void onSegmentControllerHitByBeam(SegmentController c) {
		for (int i = 0; i < segmentControllerListeners.size(); i++) {
			segmentControllerListeners.get(i).onSegmentControllerHitByBeam(c);
		}
	}

	@Override
	public final void onSegmentControllerHitByPulse(SegmentController c) {
		for (int i = 0; i < segmentControllerListeners.size(); i++) {
			segmentControllerListeners.get(i).onSegmentControllerHitByPulse(c);
		}
	}

	@Override
	public final void onSegmentControllerHitByMissile(SegmentController c) {
		for (int i = 0; i < segmentControllerListeners.size(); i++) {
			segmentControllerListeners.get(i).onSegmentControllerHitByMissile(c);
		}
	}

	@Override
	public final void onSegmentControllerPlayerAttached(SegmentController c) {
		for (int i = 0; i < segmentControllerListeners.size(); i++) {
			segmentControllerListeners.get(i).onSegmentControllerPlayerAttached(c);
		}
	}

	@Override
	public final void onSegmentControllerPlayerDetached(SegmentController c) {
		for (int i = 0; i < segmentControllerListeners.size(); i++) {
			segmentControllerListeners.get(i).onSegmentControllerPlayerDetached(c);
		}
	}

	@Override
	public final void onSegmentControllerDocking(SegmentController c) {
		for (int i = 0; i < segmentControllerListeners.size(); i++) {
			segmentControllerListeners.get(i).onSegmentControllerDocking(c);
		}
	}

	@Override
	public final void onSegmentControllerUndocking(SegmentController c) {
		for (int i = 0; i < segmentControllerListeners.size(); i++) {
			segmentControllerListeners.get(i).onSegmentControllerUndocking(c);
		}
	}

	@Override
	public void onSegmentControllerDestroyedPermanently(SegmentController s) {
		for (int i = 0; i < segmentControllerListeners.size(); i++) {
			segmentControllerListeners.get(i).onSegmentControllerDestroyedPermanently(s);
		}
	}

	@Override
	public final void onPlayerKilled(PlayerState ps, Damager from) {
		for (int i = 0; i < playerStateListeners.size(); i++) {
			playerStateListeners.get(i).onPlayerKilled(ps, from);
		}
	}

	@Override
	public final void onPlayerCreated(PlayerState ps) {
		for (int i = 0; i < playerStateListeners.size(); i++) {
			playerStateListeners.get(i).onPlayerCreated(ps);
		}
	}

	@Override
	public final void onPlayerSpawned(PlayerState ps, PlayerCharacter spawnedObject) {
		for (int i = 0; i < playerStateListeners.size(); i++) {
			playerStateListeners.get(i).onPlayerSpawned(ps, spawnedObject);
		}
	}

	@Override
	public final void onPlayerCreditsChanged(PlayerState ps) {
		for (int i = 0; i < playerStateListeners.size(); i++) {
			playerStateListeners.get(i).onPlayerCreditsChanged(ps);
		}
	}

	@Override
	public final void onPlayerUpdate(PlayerState ps, Timer t) {
		for (int i = 0; i < playerStateListeners.size(); i++) {
			playerStateListeners.get(i).onPlayerUpdate(ps, t);
		}
	}

	@Override
	public final void onPlayerSectorChanged(PlayerState ps) {
		for (int i = 0; i < playerStateListeners.size(); i++) {
			playerStateListeners.get(i).onPlayerSectorChanged(ps);
		}
	}

	@Override
	public void onPlayerRemoved(PlayerState s) {
		for (int i = 0; i < playerStateListeners.size(); i++) {
			playerStateListeners.get(i).onPlayerRemoved(s);
		}
	}

	@Override
	public final void onPlayerChangedContol(PlayerState ps, PlayerControllable to,
	                                        Vector3i toParameter, Sendable from, Vector3i fromParameter) {
		for (int i = 0; i < playerStateListeners.size(); i++) {
			playerStateListeners.get(i).onPlayerChangedContol(ps, to, toParameter, from, fromParameter);
		}
	}

	@Override
	public final void registerNetworkClasses() {
		for (int i = 0; i < networkInitializationListeners.size(); i++) {
			networkInitializationListeners.get(i).registerNetworkClasses();
		}
	}

	@Override
	public final void registerRemoteClasses() {
		for (int i = 0; i < networkInitializationListeners.size(); i++) {
			networkInitializationListeners.get(i).registerRemoteClasses();
		}
	}

	@Override
	public final void onInitializeBlockData() {
		for (int i = 0; i < blockInitializationListeners.size(); i++) {
			blockInitializationListeners.get(i).onInitializeBlockData();
		}
	}

}
