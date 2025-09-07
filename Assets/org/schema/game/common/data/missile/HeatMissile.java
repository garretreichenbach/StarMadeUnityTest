package org.schema.game.common.data.missile;

import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import javax.vecmath.Vector3f;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.data.missile.updates.MissileSpawnUpdate.MissileType;
import org.schema.game.common.data.missile.updates.MissileTargetUpdate;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class HeatMissile extends TargetChasingMissile {
	public HeatMissile(StateInterface state) {
		super(state);
	}

	private boolean checkTarget(
			SimpleTransformableSendableObject s) {

		if (getOwner() instanceof SegmentController && s instanceof SegmentController &&
				(((SegmentController) s).getDockingController().isInAnyDockingRelation((SegmentController) getOwner())
						||
						((SegmentController) s).railController.isInAnyRailRelationWith((SegmentController) getOwner()))
				) {
			//don't go for own turrets
			return false;
		}
		if(getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(s.getId()) == null){
			return false;
		}
		if (s.isInAdminInvisibility()) {
			return false;
		}
		if (!(s instanceof AbstractCharacter<?> || s instanceof SpaceStation || s instanceof Ship)) {
			return false;
		}
		if (s.getSectorId() == this.getSectorId()) {
			return true;
		} else {
			Sector sectorOwn = ((GameServerState) getState()).getUniverse().getSector(getSectorId());
			Sector sector = ((GameServerState) getState()).getUniverse().getSector(s.getSectorId());
			if (sector != null && sectorOwn != null) {
				if (Math.abs(sector.pos.x - sectorOwn.pos.x) < 2 &&
						Math.abs(sector.pos.y - sectorOwn.pos.y) < 2 &&
						Math.abs(sector.pos.z - sectorOwn.pos.z) < 2) {

					s.calcWorldTransformRelative(sectorOwn.getId(), sectorOwn.pos);
					return true;
				}
			}
		}

		return false;
	}

	private void findNewTarget() {
		if (((GameServerState) getState()).getUniverse().getSector(getSectorId()) == null) {
			System.err.println("[SERVER] " + this + " cant find new targte: it's sector is not loaded: " + getSectorId());
		}
		SimpleTransformableSendableObject nearest = null;
		final Vector3f d = new Vector3f();
		int max = 5;
		ObjectArrayList<SimpleTransformableSendableObject> potential = new ObjectArrayList<SimpleTransformableSendableObject>();
		for (Sendable s : getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
			if (s != getOwner() && s instanceof SimpleTransformableSendableObject &&
					checkTarget(((SimpleTransformableSendableObject) s))) {
				SimpleTransformableSendableObject tar = (SimpleTransformableSendableObject) s;

				if (((GameServerState) getState()).getUniverse().getSector(getSectorId()) != null) {
					tar.calcWorldTransformRelative(getSectorId(), ((GameServerState) getState()).getUniverse().getSector(getSectorId()).pos);
				}
//				target.calcWorldTransformRelative(getEntity().getSectorId(), ((GameServerState)getEntity().getState()).getUniverse().getSector(getEntity().getSectorId()).pos);

				if (!tar.isHidden()) {

					if (tar instanceof SegmentController) {
						SegmentController tt = (SegmentController) tar;

						if (getOwner() instanceof SegmentController) {
							if (((SegmentController) getOwner()).railController.isInAnyRailRelationWith(tt) || ((SegmentController) getOwner()).getDockingController().isInAnyDockingRelation(tt)) {
								//don't add any targets that have docking relation
								continue;
							}
						}

						if (!(tar instanceof Ship) || (!((Ship) tar).isCoreOverheating() && !((Ship) tar).isCloakedFor(null))) {
							potential.add(tt);
						}
					} else {
						potential.add(tar);
					}
				}
			}
		}

		Collections.sort(potential, (o1, o2) -> {
			d.sub(o1.getClientTransform().origin, HeatMissile.this.getWorldTransform().origin);
			float a = d.lengthSquared();
			d.sub(o2.getClientTransform().origin, HeatMissile.this.getWorldTransform().origin);
			float b = d.lengthSquared();
			return Float.compare(a, b);
		});

		if (potential.size() > 1) {
			d.sub(potential.get(0).getClientTransform().origin, HeatMissile.this.getWorldTransform().origin);
			float a = d.lengthSquared();
			d.sub(potential.get(1).getClientTransform().origin, HeatMissile.this.getWorldTransform().origin);
			float b = d.lengthSquared();
			assert (a <= b);
		}

		while (potential.size() > max) {
			potential.remove(potential.size() - 1);
		}
		if (!potential.isEmpty()) {
			SimpleTransformableSendableObject first = potential.get(0);
			d.sub(first.getClientTransform().origin, HeatMissile.this.getWorldTransform().origin);
			float n = d.length();
			for (int i = potential.size() - 1; i > 0; i--) {
				d.sub(first.getClientTransform().origin, HeatMissile.this.getWorldTransform().origin);
				float l = d.length();
				if (l < 8 * n || l < 300) {
					//its in range relatively
				} else {
					potential.remove(i);
				}
			}
		}
		if (!potential.isEmpty()) {
			Random r = new Random();
			nearest = potential.get(r.nextInt(potential.size()));
		}

		if (nearest != getTarget()) {

			this.setTarget((nearest));
			assert ((!(getTarget() instanceof Ship) || !((Ship) getTarget()).isCoreOverheating()));
			MissileTargetUpdate missileTargetUpdate = new MissileTargetUpdate(getId());
			missileTargetUpdate.target = getTarget() == null ? -1 : getTarget().getId();
//			System.err.println("[SERVER][MISSILE] adding target update " + getTarget() + " -> " + missileTargetUpdate.target);
			pendingBroadcastUpdates.add(missileTargetUpdate);
		}

	}

	private boolean needsTargetUpdate() {
		if (getTarget() == null) {
			return true;
		}
		Vector3f d = new Vector3f();
		//calculates client transform
		if (!checkTarget((getTarget()))) {
			return true;
		}
		//		d.sub(target.getClientTransform().origin, this.getWorldTransform().origin);

		return false;
	}

	@Override
	public MissileType getType() {
		return MissileType.HEAT;
	}

	@Override
	public void onSpawn() {
		super.onSpawn();
	}

	@Override
	public String toString() {
		return "Heat" + super.toString() + ")->" + getTarget();
	}

	@Override
	public void updateTarget(Timer timer) {
		if (isOnServer()) {
			if (needsTargetUpdate()) {
				findNewTarget();
			}
		}
	}

}
