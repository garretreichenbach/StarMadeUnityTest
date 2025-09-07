package org.schema.game.common.data.world;

import java.io.IOException;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.physics.PhysicsExt;
import org.schema.game.common.data.physics.RigidBodySegmentController;
import org.schema.game.common.data.world.SectorInformation.SectorType;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.forms.BoundingBox;

import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;

public class VirtualEntityAttachment {

	public static Universe universe;
	private static Vector3i tmpPos = new Vector3i();
	public final SimpleTransformableSendableObject obj;
	private final Transform tmpTrans = new Transform();
	BoundingBox bb = new BoundingBox();
	BoundingBox bbOwn = new BoundingBox();
	private Transform t = new Transform();
	private Vector3f lc = new Vector3f();
	private VirtualEntityAttachmentItem[] items = new VirtualEntityAttachmentItem[27];
	private boolean allDespawned = false;

	public VirtualEntityAttachment(SimpleTransformableSendableObject obj) {
		super();
		this.obj = obj;
	}

	private boolean isMainPhysicsSpawned(PhysicsExt ownObjPhysics) {
		if (obj.getPhysicsDataContainer().getObject() == null) {
			return false;
		}
		return ownObjPhysics.containsObject(obj.getPhysicsDataContainer().getObject());
	}

	public void update() {

		Sector entitySector = universe.getSector(obj.getSectorId());
		if (entitySector != null) {
			PhysicsExt physics = obj.getPhysics();

			boolean mainPhysicsSpawned = isMainPhysicsSpawned(physics);

			boolean nowOverlapping = isOverlappingWithOtherSectors();

			if (nowOverlapping) {
				allDespawned = false;
				updateAll(entitySector, mainPhysicsSpawned);
			} else {
				desawnAll(entitySector, mainPhysicsSpawned);
				//everything is despawned
			}
		}
	}

	private boolean isOverlappingWithOtherSectors() {
		if (obj.getPhysicsDataContainer().getObject() == null) {
			return false;
		}
		float sectorSizeWithoutMarginHalf = ((GameServerState) obj.getState()).getSectorSizeWithoutMargin() * 0.5f;
		bb.min.set(-sectorSizeWithoutMarginHalf, -sectorSizeWithoutMarginHalf, -sectorSizeWithoutMarginHalf);
		bb.max.set(sectorSizeWithoutMarginHalf, sectorSizeWithoutMarginHalf, sectorSizeWithoutMarginHalf);

		obj.getPhysicsDataContainer().getShape().getAabb(obj.getPhysicsDataContainer().getObject().getWorldTransform(tmpTrans), bbOwn.min, bbOwn.max);

		return !bb.isInside(bbOwn);
	}

	private void updateAll(Sector entitySector, boolean mainPhysicsSpawned) {
		int i = 0;
		for (int z = -1; z <= 1; z++) {
			for (int y = -1; y <= 1; y++) {
				for (int x = -1; x <= 1; x++) {
					tmpPos.set(entitySector.pos);
					if (x != 0 || y != 0 || z != 0) {
						tmpPos.add(x, y, z);

						if (items[i] == null) {
							items[i] = new VirtualEntityAttachmentItem();
						}
						//							System.err.println("UPDAINT::: "+i+"; "+items[i].sectorPosFrom+" -> "+items[i].sectorPosTo+"; "+items[i].isSpawnedVirtual());
						items[i].update(entitySector, mainPhysicsSpawned, tmpPos);

						assert (!items[i].isSpawnedVirtual() || items[i].sectorTo != obj.getSectorId()) : obj + "; " + items[i].sectorFrom + " " + items[i].sectorTo;

						//entity physics MAY NOT contain any virtual object of itself
						assert (items[i].isSpawnedVirtual() || !entitySector.getPhysics().containsObject(items[i].virtualBody)) : i + "#; " + obj + "; " + items[i].sectorFrom + " " + items[i].sectorTo;
					}

					i++;
				}

			}
		}
	}

	private void desawnAll(Sector entitySector, boolean mainPhysicsSpawned) {
		int i = 0;
		if (!allDespawned) {
			for (int z = -1; z <= 1; z++) {
				for (int y = -1; y <= 1; y++) {
					for (int x = -1; x <= 1; x++) {
						tmpPos.set(entitySector.pos);
						if (x != 0 || y != 0 || z != 0) {
							tmpPos.add(x, y, z);

							if (items[i] != null) {
								items[i].despawn();
								items[i] = null;
							}
						}

						i++;
					}

				}
			}
		}
		allDespawned = true;
	}

	public int getVicinityIndex(Vector3i pos, Vector3i otherPos) {
		int x = (otherPos.x - pos.x) + 1;
		int y = (otherPos.x - pos.x) + 1;
		int z = (otherPos.x - pos.x) + 1;

		return z * 9 + y * 3 + x;
	}

	public void clear() {
		for (int i = 0; i < items.length; i++) {
			if (items[i] != null) {
				items[i].despawn();
			}
		}
	}

	private class VirtualEntityAttachmentItem {
		int sectorFrom = -1;
		int sectorTo = -1;
		Vector3i sectorPosFrom = new Vector3i(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
		Vector3i sectorPosTo = new Vector3i(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
		private boolean spawnedVirtual = false;
		private Sector spawnedSector;
		private RigidBody virtualBody;

		public void update(Sector entitySector, boolean mainPhysicsSpawned, Vector3i otherPos) {

			if (spawnedVirtual && (!obj.hasVirtual() || !mainPhysicsSpawned)) {
//								System.err.println("DESPAWNING BECAUSE: not virt: "+!obj.hasVirtual()+"; not main spawned: "+!mainPhysicsSpawned);
				despawn();
			}

			if (obj.hasVirtual() && mainPhysicsSpawned) {
				Sector otherSector = universe.getSectorWithoutLoading(otherPos);
				if (otherSector == null) {
					//sector does not exist, reset spawning
					if (spawnedVirtual) {
						if (spawnedSector != null && virtualBody != null && spawnedSector.getPhysics() != null) {
							spawnedSector.getPhysics().removeObject(virtualBody);
						}
						spawnedSector = null;
						spawnedVirtual = false;

					}
				} else {
					try {
						if (otherSector.getSectorType() != SectorType.PLANET && entitySector.getSectorType() != SectorType.PLANET) {
							if (sectorFrom != entitySector.getId() || sectorTo != otherSector.getId() || sectorTo == obj.getSectorId()) {
//																System.err.println("DESPAWNING BECAUSE SECTOR CHANGED ( "+obj+" spawned: "+isSpawnedVirtual()+"): "+sectorFrom+" -> "+entitySector.getId()+"; "+sectorTo+" -> "+otherSector.getId());
								despawn();
							}
							assert (entitySector != otherSector);
							assert (!entitySector.pos.equals(otherSector.pos));
							if (!spawnedVirtual) {
								spawn(entitySector, otherSector);
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			if (spawnedVirtual) {
				assert (virtualBody != null);

				Sector sector = universe.getSector(sectorTo);
				if (sector != null) {
					obj.calculateRelToThis(sector, sectorPosTo);
//					if(sectorPosTo.equals(1, 2, 2)){
//						System.err.println("UPDATING POSITION OF VIRTUAL: "+obj.getClientTransform().origin);
//					}
//					System.err.println("UPDATING: "+virtualBody);

					t.set(obj.getClientTransform());
					lc.set(obj.getPhysicsDataContainer().lastCenter);
					t.basis.transform(lc);
					t.origin.add(lc);

					virtualBody.setWorldTransform(t);
					virtualBody.getMotionState().setWorldTransform(t);
					virtualBody.setInterpolationWorldTransform(t);
//					assert(sector.getPhysics().containsObject(virtualBody));
				} else {
					System.err.println("[VIRTUALENT] WARNING: sector not loaded: " + sectorTo + " -> " + sectorPosTo);
					//the sector is not loaded, therefore the physics object has already been cleaned up by the sector clear
					if (spawnedSector != null && virtualBody != null) {
						spawnedSector.getPhysics().removeObject(virtualBody);
						spawnedSector = null;
					}

					spawnedVirtual = false;
				}

			}

		}

		private void spawn(Sector entitySector, Sector otherSector) {

			try {
				if (otherSector.getSectorType() != SectorType.PLANET && entitySector.getSectorType() != SectorType.PLANET) {

					//position from the neighbors sector's view
					obj.calculateRelToThis(otherSector, otherSector.pos);

					virtualBody = otherSector.getPhysics().getBodyFromShape(obj.getPhysicsDataContainer().getShape(), 0, new Transform(obj.getClientTransform()));
					if (virtualBody instanceof RigidBodySegmentController) {
						((RigidBodySegmentController) virtualBody).virtualString = "virtS(ori)" + entitySector.pos + "mapTo(vi)" + otherSector.pos + "{" + obj.getPhysicsDataContainer().getObject().toString() + "}";
						((RigidBodySegmentController) virtualBody).virtualSec = new Vector3i(otherSector.pos);
					}
					virtualBody.setUserPointer(obj.getId());
					virtualBody.setCollisionFlags(CollisionFlags.KINEMATIC_OBJECT);
					virtualBody.setActivationState(CollisionObject.DISABLE_DEACTIVATION);
					otherSector.getPhysics().addObject(virtualBody, obj.getPhysicsDataContainer().collisionGroup, obj.getPhysicsDataContainer().collisionMask);

					spawnedSector = otherSector;

					assert (otherSector.getPhysics().containsObject(virtualBody));
					virtualBody.activate(true);
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}

			if (virtualBody != null) {
				sectorFrom = entitySector.getId();
				sectorPosFrom.set(entitySector.pos);

				sectorTo = otherSector.getId();
				sectorPosTo.set(otherSector.pos);

				assert (sectorFrom >= 0);
				assert (sectorTo >= 0);

				spawnedVirtual = true;

//				System.err.println("ORIG SEC: "+obj.getSectorId()+" -> "+sectorFrom+"; "+entitySector+" ADDED VIRTUAL BODY: "+virtualBody+": virt in "+sectorTo+" -> "+sectorPosTo);
			} else {
				try {

					assert (otherSector.getSectorType() == SectorType.PLANET || entitySector.getSectorType() == SectorType.PLANET);
					//					System.err.println("DID NOT SPAWN VIRTUAL BODY: "+sectorTo+" -> "+sectorPosTo+" (should be planet) "+entitySector.getSectorType()+" -> "+otherSector.getSectorType());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			assert (entitySector == null || sectorTo != entitySector.getId());
		}

		private void despawn() {
			if (spawnedVirtual) {
				assert (virtualBody != null);

				spawnedVirtual = false;
				if (spawnedSector != null && spawnedSector.getPhysics() != null) {
					spawnedSector.getPhysics().removeObject(virtualBody);
				}
				spawnedSector = null;

				sectorFrom = -1;
				sectorTo = -1;
				sectorPosFrom.set(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
				sectorPosTo.set(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
			}
		}

		/**
		 * @return the spawnedVirtual
		 */
		public boolean isSpawnedVirtual() {
			return spawnedVirtual;
		}

		/**
		 * @param spawnedVirtual the spawnedVirtual to set
		 */
		public void setSpawnedVirtual(boolean spawnedVirtual) {

			this.spawnedVirtual = spawnedVirtual;
		}
	}
}
