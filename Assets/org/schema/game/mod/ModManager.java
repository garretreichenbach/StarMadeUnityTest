package org.schema.game.mod;

import java.util.ArrayList;
import java.util.List;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.data.player.PlayerCharacter;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.objects.Sendable;

public class ModManager {

	private final List<Mod> mods = new ArrayList<Mod>();

	public synchronized void registerMod(Mod mod) {
		mods.add(mod);
	}

	public synchronized void unregisterMod(Mod mod) {
		mods.remove(mod);
	}

	public final void onSegmentControllerUpdate(SegmentController c) {
		for (int i = 0; i < mods.size(); i++) {
			mods.get(i).onSegmentControllerUpdate(c);
		}
	}

	public final void onSegmentControllerSpawn(SegmentController c) {
		for (int i = 0; i < mods.size(); i++) {
			mods.get(i).onSegmentControllerSpawn(c);
		}
	}

	public final void onSegmentControllerDelete(SegmentController c) {
		for (int i = 0; i < mods.size(); i++) {
			mods.get(i).onSegmentControllerDelete(c);
		}
	}

	public final void onSegmentControllerDamageTaken(SegmentController c) {
		for (int i = 0; i < mods.size(); i++) {
			mods.get(i).onSegmentControllerDamageTaken(c);
		}
	}

	public final void onSegmentControllerHitByLaser(SegmentController c) {
		for (int i = 0; i < mods.size(); i++) {
			mods.get(i).onSegmentControllerHitByLaser(c);
		}
	}

	public final void onSegmentControllerHitByBeam(SegmentController c) {
		for (int i = 0; i < mods.size(); i++) {
			mods.get(i).onSegmentControllerHitByBeam(c);
		}
	}

	public final void onSegmentControllerHitByPulse(SegmentController c) {
		for (int i = 0; i < mods.size(); i++) {
			mods.get(i).onSegmentControllerHitByPulse(c);
		}
	}

	public final void onSegmentControllerHitByMissile(SegmentController c) {
		for (int i = 0; i < mods.size(); i++) {
			mods.get(i).onSegmentControllerHitByMissile(c);
		}
	}

	public final void onSegmentControllerPlayerAttached(SegmentController c) {
		for (int i = 0; i < mods.size(); i++) {
			mods.get(i).onSegmentControllerPlayerAttached(c);
		}
	}

	public final void onSegmentControllerPlayerDetached(SegmentController c) {
		for (int i = 0; i < mods.size(); i++) {
			mods.get(i).onSegmentControllerPlayerDetached(c);
		}
	}

	public final void onSegmentControllerDocking(SegmentController c) {
		for (int i = 0; i < mods.size(); i++) {
			mods.get(i).onSegmentControllerDocking(c);
		}
	}

	public final void onSegmentControllerUndocking(SegmentController c) {
		for (int i = 0; i < mods.size(); i++) {
			mods.get(i).onSegmentControllerUndocking(c);
		}
	}

	public final void onPlayerKilled(PlayerState ps, Damager from) {
		for (int i = 0; i < mods.size(); i++) {
			mods.get(i).onPlayerKilled(ps, from);
		}
	}

	public final void onPlayerCreated(PlayerState ps) {
		for (int i = 0; i < mods.size(); i++) {
			mods.get(i).onPlayerCreated(ps);
		}
	}

	public final void onPlayerSpawned(PlayerState ps,
	                                  PlayerCharacter spawnedObject) {
		for (int i = 0; i < mods.size(); i++) {
			mods.get(i).onPlayerSpawned(ps, spawnedObject);
		}
	}

	public final void onPlayerChangedContol(PlayerState ps,
	                                        PlayerControllable to, Vector3i toParameter,
	                                        Sendable from, Vector3i fromParameter) {
		for (int i = 0; i < mods.size(); i++) {
			mods.get(i).onPlayerChangedContol(ps, to, toParameter, from, fromParameter);
		}
	}

	public final void onPlayerCreditsChanged(PlayerState ps) {
		for (int i = 0; i < mods.size(); i++) {
			mods.get(i).onPlayerCreditsChanged(ps);
		}
	}

	public final void onPlayerUpdate(PlayerState ps, Timer t) {
		for (int i = 0; i < mods.size(); i++) {
			mods.get(i).onPlayerUpdate(ps, t);
		}
	}

	public final void onPlayerSectorChanged(PlayerState ps) {
		for (int i = 0; i < mods.size(); i++) {
			mods.get(i).onPlayerSectorChanged(ps);
		}
	}

	public final void registerNetworkClasses() {
		for (int i = 0; i < mods.size(); i++) {
			mods.get(i).registerNetworkClasses();
		}
	}

	public final void registerRemoteClasses() {
		for (int i = 0; i < mods.size(); i++) {
			mods.get(i).registerRemoteClasses();
		}
	}

	public final void onInitializeBlockData() {
		for (int i = 0; i < mods.size(); i++) {
			mods.get(i).onInitializeBlockData();
		}
	}

	public void onPlayerRemoved(PlayerState s) {
		for (int i = 0; i < mods.size(); i++) {
			mods.get(i).onPlayerRemoved(s);
		}
	}

	public void onSegmentControllerDestroyedPermanently(
			SegmentController s) {
		for (int i = 0; i < mods.size(); i++) {
			mods.get(i).onSegmentControllerDestroyedPermanently(s);
		}
	}

}
