package org.schema.game.server.controller;

import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.AabbUtil2;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.StringUtils;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.database.DatabaseEntry;
import org.schema.game.common.controller.rails.RailRelation;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.PullEffect;
import org.schema.game.common.data.element.ElementDocking;
import org.schema.game.common.data.physics.CubeShape;
import org.schema.game.common.data.physics.PairCachingGhostObjectAlignable;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.*;
import org.schema.game.common.data.world.SectorInformation.SectorType;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.blueprint.ChildStats;
import org.schema.game.server.data.blueprint.SegmentControllerOutline;
import org.schema.game.server.data.blueprintnw.BlueprintEntry;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.LocalSectorTransition;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.physics.Physical;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

public class SectorSwitch {
	public static final int TRANS_LOCAL = 0;
	public static final int TRANS_JUMP = 1;
	public static final int TRANS_REMOTE = 2;
	private static Vector3f minRemote = new Vector3f();
	private static Transform switchTrans = new Transform();
	private static Transform wo = new Transform();
	private static Vector3f absCenterPos = new Vector3f();
	private static Matrix4f a = new Matrix4f();
	private static Matrix4f b = new Matrix4f();
	private static Vector3i relSysPos = new Vector3i();
	private static Vector3i fromOldToNew = new Vector3i();
	private static Transform transTmp = new Transform();
	public boolean makeCopy;
	public byte executionGraphicsEffect = 0;
	public List<SimpleTransformableSendableObject> avoidOverlapping;
	public Vector3f jumpSpawnPos;
	public boolean keepJumpBasisWithJumpPos = false;
	public long delay;
	public Transform sectorSpaceTransform = null;
	private SimpleTransformableSendableObject o;
	private Vector3i belogingVector;
	private int jump;
	public boolean forceEvenOnDocking;
	public boolean eliminateGravity;

	public SectorSwitch(
			SimpleTransformableSendableObject o, Vector3i belogingVector, int jump) {
		super();
		this.o = o;
		this.belogingVector = belogingVector;
		this.jump = jump;
	}

	public static Transform sunPos(long dayInMillis, long startTime, long t, Transform worldTransfromFrom, Vector3i referencePoint, boolean rotating, StateInterface state) {
		float pc = 0;
		if (dayInMillis > 0) {
			long diff = (t - (startTime)) % dayInMillis;
			pc = (float) diff / (float) dayInMillis;
		}
		Vector3i sysPos = StellarSystem.getPosFromSector(referencePoint, new Vector3i());

		sysPos.scale(VoidSystem.SYSTEM_SIZE);
		sysPos.add(VoidSystem.SYSTEM_SIZE / 2, VoidSystem.SYSTEM_SIZE / 2, VoidSystem.SYSTEM_SIZE / 2);
		//		System.err.println("SYS POSB: "+sysPos);
		sysPos.sub(referencePoint);
		//		System.err.println("SYS POS: "+sysPos);

		Vector3f absCenterPos = new Vector3f(
				(sysPos.x) * ((GameStateInterface) state).getSectorSize(),
				(sysPos.y) * ((GameStateInterface) state).getSectorSize(),
				(sysPos.z) * ((GameStateInterface) state).getSectorSize());

		Transform tmp = new Transform();
		tmp.setIdentity();
		//		tmp.basis.mul(worldTransfromFrom.basis);
		if (rotating) {
			Matrix3f r = new Matrix3f();
			r.rotX((FastMath.PI * 2) * pc);
			tmp.basis.mul(r);
		}

		//		tmp.origin.set(absCenterPos);
		//		tmp.origin.sub(absSectorPos);
		tmp.transform(absCenterPos);

		System.err.println(referencePoint + " -> CENTER: " + absCenterPos);
		Vector3f dis = new Vector3f();

		dis.sub(absCenterPos, worldTransfromFrom.origin);

		Vector3f forward = new Vector3f(dis);
		forward.normalize();
		Vector3f up = new Vector3f(0, 1, 0);
		Vector3f right = new Vector3f();

		right.cross(up, forward);
		right.normalize();
		up.cross(forward, right);
		up.normalize();

		Transform out = new Transform();
		out.setIdentity();
		GlUtil.setForwardVector(forward, out);
		GlUtil.setUpVector(up, out);
		GlUtil.setRightVector(right, out);

		return out;
	}

	public StateInterface getState() {
		return o.getState();
	}
	public void execute(GameServerState state) throws IOException {
		//		System.err.println("[SERVER] Doing Sector Change for "+o+" TO "+belogingVector+" REMOTE = "+(jump == TRANS_REMOTE));
		
		
		
		if (o.getPhysicsDataContainer().getObject() == null) {
			if (o instanceof SegmentController &&
					(((SegmentController) o).getDockingController().isDocked() || (((SegmentController) o).railController.isDockedOrDirty()) || ((SegmentController) o).railController.hasInitialRailRequest())) {
				if(!(((SegmentController) o).railController.isDockedOrDirty()) && ((SegmentController) o).railController.hasInitialRailRequest()){
					assert(false):"got you "+o;
				}
				System.err.println("[SECTORSWITCH] Exception (catched): tried to change sector on dockedobject: " + o);
				return;
			} else {
				throw new NullPointerException("[SECTORSWITCH] no physics object for: " + o);
			}
		}
		if (o instanceof SegmentController && !forceEvenOnDocking && 
				((((SegmentController) o).railController.isDockedOrDirty()) || ((SegmentController) o).railController.hasInitialRailRequest())) {
			System.err.println("[SECTORSWITCH] Not changing sector for object with pending rail request: " + o);
			return;
		}
		boolean activate = true;
		if (o instanceof PlayerControllable &&
				((PlayerControllable) o).getAttachedPlayers().isEmpty()) {
			activate = false;
		}

		Sector oldSector = state.getUniverse().getSector(o.getSectorId());

		if (jump == TRANS_LOCAL && oldSector != null && o instanceof PlayerControllable && oldSector.isNoExit()) {
			for (PlayerState s : ((PlayerControllable) o).getAttachedPlayers()) {
				s.sendServerMessage(new ServerMessage(Lng.astr("Cannot exit sector\n(Sector locked by admin)"), ServerMessage.MESSAGE_TYPE_ERROR, s.getId()));
			}
			if (o instanceof SendableSegmentController) {

				Vector3f force = new Vector3f(o.getWorldTransform().origin);
				float pullForce = force.length();
				force.normalize();

				((SendableSegmentController) o).getBlockEffectManager().addEffect(new PullEffect(((SendableSegmentController) o), force, pullForce, false, 5));
			} else {
				o.warpTransformable(0, 0, 0, true, null);
			}
			return;
		}

		Sector newSector = null;

		newSector = state.getUniverse().getSector(belogingVector, activate);

		assert (state.getUniverse().getSector(newSector.getId()) == newSector) : newSector.getId() + ": " + state.getUniverse().getSector(newSector.getId());

		if (o instanceof PlayerControllable &&
				((PlayerControllable) o).getAttachedPlayers().isEmpty()) {

			newSector.pingShort();
		} else {
			newSector.ping();
		}

		if (jump == TRANS_LOCAL && o instanceof PlayerControllable && newSector.isNoEntry()) {
			if (o instanceof PlayerControllable &&
					!((PlayerControllable) o).getAttachedPlayers().isEmpty()) {
				for (PlayerState s : ((PlayerControllable) o).getAttachedPlayers()) {
					s.sendServerMessage(new ServerMessage(Lng.astr("Cannot enter sector\n(Sector locked by admin)"), ServerMessage.MESSAGE_TYPE_ERROR, s.getId()));
				}
				if (o instanceof SendableSegmentController) {

					Vector3f force = new Vector3f(o.getWorldTransform().origin);
					float pullForce = force.length();
					force.normalize();

					((SendableSegmentController) o).getBlockEffectManager().addEffect(new PullEffect(((SendableSegmentController) o), force, pullForce, false, 5));
				} else {
					o.warpTransformable(0, 0, 0, true, null);
				}
				return;
			}
		}

		o.onPhysicsRemove(); //remove from old physics
		o.setSectorId(newSector.getId());
		o.onSectorSwitchServer(newSector);
		HashSet<Sendable> alreadyMoved = new HashSet<Sendable>();
		HashSet<PlayerState> players = new HashSet<PlayerState>();
		if (o instanceof PlayerControllable) {
			PlayerControllable pc = (PlayerControllable) o;
			for (PlayerState ps : pc.getAttachedPlayers()) {
				System.err.println("[SERVER] " + o + " has players attached. Doing Sector Change for " + ps + ": " + oldSector + " -> " + newSector);
				ps.setCurrentSector(newSector.pos);
				ps.setCurrentSectorId(newSector.getId());

				AbstractCharacter<?> assingedPlayerCharacter = ps.getAssingedPlayerCharacter();
				alreadyMoved.add(assingedPlayerCharacter);
				if (assingedPlayerCharacter != null) {
					System.err.println("[SERVER] " + o + " has CHARACTER. Doing Sector Change for " + assingedPlayerCharacter + ": " + oldSector + " -> " + newSector + " ID " + newSector.getId());
					if (!(o instanceof AbstractCharacter<?>) && !assingedPlayerCharacter.isHidden()) {
						try {
							throw new IllegalArgumentException("Player " + ps + " was attached to object " + o + " BUT " + assingedPlayerCharacter + " WAS NOT HIDDEN ");
						} catch (Exception e) {
							e.printStackTrace();
						}
						assingedPlayerCharacter.setHidden(true);
						assingedPlayerCharacter.onPhysicsRemove();
					}
					if(eliminateGravity && o.getGravity().source != null && (o.getGravity().source instanceof Planet || o.getGravity().source instanceof PlanetIco)) {
						assingedPlayerCharacter.removeGravity();
					}
					assingedPlayerCharacter.setSectorId(newSector.getId());
				} else {
					System.err.println("[SERVER] WARNING NO PLAYER CHARACTER ATTACHED TO " + ps);
				}
			}

		}

		if (o instanceof SegmentController) {
			handleDockingList((SegmentController) o, newSector);

			handleRailsRecursively((SegmentController) o, newSector);
		}

		Vector3f linVelo = new Vector3f();
		Vector3f angVelo = new Vector3f();

		if (o.getPhysicsDataContainer().getObject() instanceof RigidBody) {
			RigidBody b = (RigidBody) o.getPhysicsDataContainer().getObject();
			b.getLinearVelocity(linVelo);
			b.getAngularVelocity(angVelo);
		}
		Transform oldTransform = new Transform(o.getWorldTransform());
		o.onPhysicsRemove(); //make sure remove from new

		if (jump != TRANS_LOCAL && makeCopy && o instanceof SegmentController) {
			System.err.println("[SECTORSWITCH] COPY JUMP: " + o + "; " + o.getUniqueIdentifier());
			SegmentController s = (SegmentController) o;
			s.writeAllBufferedSegmentsToDatabase(true, false, false);
			BlueprintEntry blueprintEntry = BluePrintController
					.temp.writeBluePrint(s, "temp_" + o.getUniqueIdentifier(), false, null);
			Transform t = new Transform(s.getWorldTransform());

			SegmentControllerOutline loadBluePrint;
			try {
				String newId = DatabaseEntry.removePrefix(o.getUniqueIdentifier());
				long i = 0;
				try {
					while (DatabaseEntry.removePrefix(o.getUniqueIdentifier()).equals(newId) || state.getDatabaseIndex().getTableManager().getEntityTable().getByUIDExact(newId, -1).size() > 0) {
						String rSub = DatabaseEntry.removePrefix(o.getUniqueIdentifier());
						int last = -1;
						if ((last = newId.lastIndexOf("_")) > 0 && last + 1 < newId.length() && StringUtils.isNumeric(newId.substring(last + 1, newId.length()))) {
							i = Long.parseLong(newId.substring(last + 1, newId.length()));
							String orig = newId;
							newId = rSub.substring(0, newId.length() - ("_" + i).length()) + "_" + (i + 1);

							System.err.println("[SECTORSWITCH] COPY SUBID " + newId + " (serial): " + orig + " -> " + newId);
						} else {
							System.err.println("[SECTORSWITCH] SUBID " + newId + " (Orig)");
							newId = rSub + "_" + i;
						}
						i++;
					}
					SegmentPiece toDockOn = null; //this is for spawning turrets manually by the player
					loadBluePrint = BluePrintController.temp.loadBluePrint(
							state,
							blueprintEntry.getName(),
							newId,
							t,
							-1,
							0,
							BluePrintController.temp.readBluePrints(),
							oldSector.pos,
							null,
							"<system>",
							Sector.buffer,
							true, toDockOn, new ChildStats(false));

					assert (!o.getUniqueIdentifier().equals(loadBluePrint.uniqueIdentifier)) : o.getUniqueIdentifier();
					loadBluePrint.realName = o.getRealName();
					loadBluePrint.spawnSectorId = new Vector3i(oldSector.pos);

					loadBluePrint.removeAfterSpawn = BluePrintController.temp;

					synchronized (state.getBluePrintsToSpawn()) {
						state.getBluePrintsToSpawn().add(loadBluePrint);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} catch (EntityNotFountException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (EntityAlreadyExistsException e) {
				e.printStackTrace();
			}
		}

		//		System.err.println("SECTOR SWITCH VELOS: : "+linVelo+"; "+angVelo);
		switchTrans.setIdentity();
		switchTrans.set(o.getWorldTransform());
		LocalSectorTransition trn =  null;
		if (jump == TRANS_LOCAL) {
			trn = new LocalSectorTransition();
			
			trn.oldPos.set(oldSector.pos);
			trn.newPos.set(newSector.pos);
			trn.planetRotation = ((GameStateInterface) state).getGameState().getRotationProgession();
			trn.oldPosPlanet = oldSector.getSectorType() == SectorType.PLANET;
			trn.newPosPlanet = newSector.getSectorType() == SectorType.PLANET;
			trn.sectorSize = ((GameStateInterface) getState()).getSectorSize();

			

			switchTrans.set(trn.getTransitionTransform(o.getWorldTransform()));
		} else if (jump == TRANS_JUMP) {
			System.err.println("[SERVER] sector change is a jump. not normalizing object position");

			if (jumpSpawnPos != null) {

				if (keepJumpBasisWithJumpPos) {

				} else {
					switchTrans.setIdentity();

					if (Math.abs(jumpSpawnPos.z) > Math.abs(jumpSpawnPos.x)) {
						if (jumpSpawnPos.z > 0) {
							switchTrans.basis.rotY(FastMath.PI);
						} else {

						}
					} else {
						if (jumpSpawnPos.x > 0) {
							switchTrans.basis.rotY(-FastMath.HALF_PI);
						} else {
							switchTrans.basis.rotY(FastMath.HALF_PI);
						}
					}
				}

				switchTrans.origin.set(jumpSpawnPos);

				System.err.println("[SERVER] SET DESIRED JUMP POS TO " + switchTrans.origin);

				ObjectArrayList<SimpleTransformableSendableObject> updateEntities = newSector.updateEntities();

				Vector3f minOut = new Vector3f();
				Vector3f maxOut = new Vector3f();
				Vector3f minOutOther = new Vector3f();
				Vector3f maxOutOther = new Vector3f();
				float evadeStep = 32;
				float evade = 0;
				float evadeDir = 1;
				Vector3f origin = new Vector3f(switchTrans.origin);
				while (checkSectorCollision(o, switchTrans, switchTrans.origin, minOut, maxOut, minOutOther, maxOutOther, state) != null) {
					switchTrans.origin.set(origin);
					evade += evadeStep;
					evadeDir *= -1;
					if (Math.abs(jumpSpawnPos.z) > Math.abs(jumpSpawnPos.x)) {
						switchTrans.origin.x += evadeDir * evade;
					} else {
						switchTrans.origin.z += evadeDir * evade;
					}
				}

			}
		} else {
			throw new IllegalArgumentException("JUMP SIGN WRONG: " + jump);
			//			//jumping from fixed to rotation

		}
		if (switchTrans.origin.length() > ((GameStateInterface) getState()).getSectorSize() * 4) {
			switchTrans.origin.normalize();
			switchTrans.origin.scale(((GameStateInterface) getState()).getSectorSize() / 3);
			System.err.println("[SERVER] Exception: Abnormal Sector change " + o + " from " + oldSector.pos + " to " + newSector.pos + "; CORRECTION ATTEMPT: pos: " + o.getWorldTransform().origin + " -> " + switchTrans.origin);
			//			((RigidBody)o.getPhysicsDataContainer().getObject()).setLinearVelocity(new Vector3f());
		}
		
		
		
		o.getWorldTransform().set(switchTrans);

		if (!o.isHidden()) {
//			System.err.println("ADDING OBJECT "+o+" TO ITS NEW SECTOR: "+newSector+"; current objects sectorid: "+state.getUniverse().getSector(o.getSectorId()));
			assert (o.getSectorId() == newSector.getId()) : "Sector switch failed: " + state.getUniverse().getSector(o.getSectorId()) + " : " + newSector + ";";
			if(o.getPhysicsDataContainer().getObject() == null || o.getPhysicsDataContainer().getObject().getCollisionShape() instanceof CubeShape){
				try {
					throw new Exception("[SERVER] WARNING: Switching sector for possibly docked object or object without physics "+o+"; "+o.getPhysicsDataContainer().getObject());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else{
				o.onPhysicsAdd();
				if (o.getPhysicsDataContainer().getObject() instanceof RigidBody) {
					RigidBody b = (RigidBody) o.getPhysicsDataContainer().getObject();
					//				System.err.println("SECTOR SWITCH VELOS: NEW : "+linVelo+"; "+angVelo);
					b.setLinearVelocity(linVelo);
					b.setAngularVelocity(angVelo);
				}
			}
		}
		synchronized (state.getLocalAndRemoteObjectContainer().getLocalObjects()) {
			for (Sendable s : state.getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
				if(!alreadyMoved.contains(s) && s instanceof AbstractCharacter<?> && !isAttachedToThisOrAnyDocked((AbstractCharacter<?>) s, o)){
//					System.err.println("[SECTOR SWITCH] CHARACTER "+s+" IS NOT ATTACHED TO "+o+"; "+((AbstractCharacter<?>)s).getGravity());
				}
				if (!alreadyMoved.contains(s) && s instanceof AbstractCharacter<?> && isAttachedToThisOrAnyDocked((AbstractCharacter<?>) s, o)) {

					System.err.println("[SERVER][SectorSwitch] " + o + " has ATTACHED gravity CHARACTER. Doing Sector Change for " + s + ": " + oldSector + " -> " + newSector + " ID " + newSector.getId());
					((AbstractCharacter<?>) s).onPhysicsRemove();
					((AbstractCharacter<?>) s).setSectorId(newSector.getId());
					if (!((AbstractCharacter<?>) s).isHidden()) {
						((AbstractCharacter<?>) s).onPhysicsAdd();
					} else {
						System.err.println("[SERVER][SectorSwitch]: not adding hidden character to physics of " + newSector);
					}
					((AbstractCharacter<?>) s).sectorChangedTimeOwnClient = System.currentTimeMillis();
					alreadyMoved.add(s);
					if (s instanceof PlayerControllable) {
						PlayerControllable pc = (PlayerControllable) s;
						for (PlayerState ps : pc.getAttachedPlayers()) {
							System.err.println("[SERVER][SectorSwitch] " + s + " has players attached. Doing Sector Change for " + ps + ": " + oldSector + " -> " + newSector);
							ps.setCurrentSector(newSector.pos);
							ps.setCurrentSectorId(newSector.getId());

							AbstractCharacter<?> assingedPlayerCharacter = ps.getAssingedPlayerCharacter();

							if (assingedPlayerCharacter != null) {
								if (!alreadyMoved.contains(assingedPlayerCharacter)) {
									System.err.println("[SERVER] " + o + " has CHARACTER. Doing Sector Change for " + assingedPlayerCharacter + ": " + oldSector + " -> " + newSector + " ID " + newSector.getId());
									assingedPlayerCharacter.setSectorId(newSector.getId());
								}
							} else {
								System.err.println("[SERVER] WARNING NO PLAYER CHARACTER ATTACHED TO " + ps);
							}
						}

					}
				}
			}
		}
		if(!o.isHidden()){
			o.setWarpToken(true);
		}
		o.warpTransformable(switchTrans, true, jumpSpawnPos != null, trn);

		if (sectorSpaceTransform != null) {
			o.warpTransformable(sectorSpaceTransform.origin.x, sectorSpaceTransform.origin.y, sectorSpaceTransform.origin.z, true, null);
		}

		if (executionGraphicsEffect > 0) {
			o.executeGraphicalEffectServer(executionGraphicsEffect);
		}
		Transform physicsT = new Transform();
		if(o.getPhysicsDataContainer().getObject() != null && !(o.getPhysicsDataContainer().getObject().getCollisionShape() instanceof CubeShape)){
			
			o.getPhysicsDataContainer().getObject().getWorldTransform(physicsT );
		}
		if(eliminateGravity){
			System.err.println("[SERVER][SECTORSWITCH] Requested gravity removal: "+o);
			o.removeGravity();
		}
		if(o instanceof SegmentController seg) {
			if(jump == TRANS_JUMP) seg.onFTLJump();
			seg.updateInverseTransform();
			seg.getRuleEntityManager().triggerSectorSwitched();
		}
//		System.err.println("[SERVER][SECTORSWITCH] ENTITY "+o+" changed sector from "+oldSector + " -> " + newSector+"; Trans: "+oldTransform.origin+" -> "+switchTrans.origin+"; Physical: "+physicsT.origin);
	}

	public boolean isAttachedToThisOrAnyDocked(AbstractCharacter<?> s, SimpleTransformableSendableObject<?> o) {
		PairCachingGhostObjectAlignable p = (PairCachingGhostObjectAlignable) ((AbstractCharacter<?>) s).getPhysicsDataContainer().getObject();

		if (p.getAttached() == o || s.getGravity().source == o) {
			return true;
		}

		if (o instanceof SegmentController) {
			for (ElementDocking a : ((SegmentController) o).getDockingController().getDockedOnThis()) {
				if (isAttachedToThisOrAnyDocked(s, a.from.getSegment().getSegmentController())) {
					return true;
				}
			}

			for (RailRelation a : ((SegmentController) o).railController.next) {
				if (isAttachedToThisOrAnyDocked(s, a.docked.getSegmentController())) {
					return true;
				}
			}
		}

		return false;
	}

	private Sendable checkSectorCollision(SimpleTransformableSendableObject so, Transform soTrans, Vector3f pos, Vector3f minOut, Vector3f maxOut, Vector3f minOutOther, Vector3f maxOutOther, GameServerState state) {
		long time = System.currentTimeMillis();
		Transform t = new Transform(soTrans);

		so.getRemoteTransformable().getPhysicsDataContainer().getShape().getAabb(t, minOut, maxOut);

		//		System.err.println("CHECKING NOW: "+t.origin+": ["+minOut+", "+maxOut+"]");

		synchronized (state.getLocalAndRemoteObjectContainer().getLocalObjects()) {
			for (Sendable s : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
				if (s instanceof Physical) {
					if (s instanceof SimpleTransformableSendableObject
							&& ((SimpleTransformableSendableObject) s)
							.getSectorId() != so.getSectorId()) {
						continue;
					}
					Physical p = ((Physical) s);

					if (s instanceof SegmentController) {
						SegmentController seg = (SegmentController) s;
						Vector3f min = new Vector3f(seg.getMinPos().x * SegmentData.SEG - SegmentData.SEG_HALF, seg.getMinPos().y * SegmentData.SEG - SegmentData.SEG_HALF, seg.getMinPos().z * SegmentData.SEG - SegmentData.SEG_HALF);
						Vector3f max = new Vector3f(seg.getMaxPos().x * SegmentData.SEG + SegmentData.SEG_HALF, seg.getMaxPos().y * SegmentData.SEG + SegmentData.SEG_HALF, seg.getMaxPos().z * SegmentData.SEG + SegmentData.SEG_HALF);
						AabbUtil2.transformAabb(min, max, 3, p.getPhysicsDataContainer()
										.getCurrentPhysicsTransform(),
								minOutOther, maxOutOther);
					} else {

						p.getPhysicsDataContainer().getShape().getAabb(
								p.getPhysicsDataContainer()
										.getCurrentPhysicsTransform(),
								minOutOther, maxOutOther);
					}

					if (AabbUtil2.testAabbAgainstAabb2(minOut, maxOut, minOutOther,
							maxOutOther)) {
						//
						long took = (System.currentTimeMillis() - time);
						if (took > 10) {
							System.err.println("[Sector] collision test at " + pos
									+ " is true: trying another pos " + minOut + ", " + maxOut + " ---> " + minOutOther + ", " + maxOutOther + ": " + took + "ms");
						}
						return s;
					}
				}
			}
		}
		long took = (System.currentTimeMillis() - time);
		if (took > 10) {
			System.err.println("[Sector] No Collission: " + took + "ms");
		}
		return null;
	}

	private void handleRail(SegmentController docked, Sector newSector) {
		docked.setSectorId(newSector.getId());
		if (docked instanceof PlayerControllable) {
			PlayerControllable pc = (PlayerControllable) docked;
			for (PlayerState ps : pc.getAttachedPlayers()) {
				System.err.println("[SERVER][SECTORSWITCH] RAIL ELEMENT " + docked + " has players attached. Doing Sector Change for " + ps);
				ps.setCurrentSector(newSector.pos);
				ps.setCurrentSectorId(newSector.getId());
				AbstractCharacter<?> assingedPlayerCharacter = ps.getAssingedPlayerCharacter();
				if (assingedPlayerCharacter != null) {
					assingedPlayerCharacter.setSectorId(newSector.getId());
				}
			}
		}
	}

	private void handleRailsRecursively(SegmentController docked, Sector newSector) {
		if (docked instanceof SegmentController) {
			SegmentController s = docked;
			if (!s.railController.next.isEmpty()) {
				for (RailRelation d : s.railController.next) {
					SegmentController dock = d.docked.getSegmentController();
					handleRail(dock, newSector);
					handleRailsRecursively(dock, newSector); //handle recursively
				}
			}
		}
	}

	private void handleDocking(SegmentController docked, Sector newSector) {
		docked.setSectorId(newSector.getId());
		if (docked instanceof PlayerControllable) {
			PlayerControllable pc = (PlayerControllable) docked;
			for (PlayerState ps : pc.getAttachedPlayers()) {
				System.err.println("[SERVER] " + docked + " has players attached. Doing Sector Change for " + ps);
				ps.setCurrentSector(newSector.pos);
				ps.setCurrentSectorId(newSector.getId());
				AbstractCharacter<?> assingedPlayerCharacter = ps.getAssingedPlayerCharacter();
				if (assingedPlayerCharacter != null) {
					assingedPlayerCharacter.setSectorId(newSector.getId());
				}
			}
		}
	}

	private void handleDockingList(SegmentController docked, Sector newSector) {
		if (docked instanceof SegmentController) {
			SegmentController s = docked;
			if (!s.getDockingController().getDockedOnThis().isEmpty()) {
				for (ElementDocking d : s.getDockingController().getDockedOnThis()) {
					SegmentController dock = d.from.getSegment().getSegmentController();
					handleDocking(dock, newSector);
					handleDockingList(dock, newSector); //handle recursively
				}
			}
		}
	}

	@Override
	public int hashCode() {
		return o.getId();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return ((SectorSwitch) obj).o == o;
	}

}
