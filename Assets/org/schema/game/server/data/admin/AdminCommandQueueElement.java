package org.schema.game.server.data.admin;

import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback;
import com.bulletphysics.linearmath.Transform;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.schema.common.LogUtil;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Quat4fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.ParticleUtil;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.ai.AIConfiguationElements;
import org.schema.game.common.controller.ai.AIGameConfiguration;
import org.schema.game.common.controller.ai.AIGameCreatureConfiguration;
import org.schema.game.common.controller.ai.Types;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.database.DatabaseEntry;
import org.schema.game.common.controller.database.DatabaseIndex;
import org.schema.game.common.controller.database.tables.EntityTable;
import org.schema.game.common.controller.elements.*;
import org.schema.game.common.controller.elements.power.PowerAddOn;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;
import org.schema.game.common.controller.elements.warpgate.WarpgateCollectionManager;
import org.schema.game.common.controller.elements.warpgate.WarpgateElementManager;
import org.schema.game.common.controller.elements.warpgate.WarpgateUnit;
import org.schema.game.common.controller.rails.RailRelation;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.creature.AICreature;
import org.schema.game.common.data.creature.AIPlayer;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementClassNotFoundException;
import org.schema.game.common.data.element.ElementDocking;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.element.meta.MetaObjectManager;
import org.schema.game.common.data.element.meta.MetaObjectManager.MetaObjectType;
import org.schema.game.common.data.element.meta.weapon.*;
import org.schema.game.common.data.element.meta.weapon.Weapon.WeaponSubType;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.game.common.data.missile.TargetChasingMissile;
import org.schema.game.common.data.mission.spawner.DefaultSpawner;
import org.schema.game.common.data.mission.spawner.SpawnMarker;
import org.schema.game.common.data.mission.spawner.component.SpawnComponentCreature;
import org.schema.game.common.data.mission.spawner.component.SpawnComponentDestroySpawnerAfterCount;
import org.schema.game.common.data.mission.spawner.condition.SpawnConditionCreatureCountOnAffinity;
import org.schema.game.common.data.mission.spawner.condition.SpawnConditionTime;
import org.schema.game.common.data.physics.CubeRayCastResult;
import org.schema.game.common.data.physics.PhysicsExt;
import org.schema.game.common.data.player.*;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.data.player.faction.*;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.player.inventory.InventorySlot;
import org.schema.game.common.data.player.inventory.NoSlotFreeException;
import org.schema.game.common.data.world.*;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.game.common.data.world.space.PlanetCore;
import org.schema.game.common.data.world.space.PlanetIcoCore;
import org.schema.game.network.commands.gamerequests.EntityRequest;
import org.schema.game.network.objects.remote.RemoteNPCSystem;
import org.schema.game.server.controller.*;
import org.schema.game.server.data.*;
import org.schema.game.server.data.blueprint.ChildStats;
import org.schema.game.server.data.blueprint.SegmentControllerOutline;
import org.schema.game.server.data.blueprintnw.BlueprintClassification;
import org.schema.game.server.data.blueprintnw.BlueprintEntry;
import org.schema.game.server.data.simulation.jobs.SpawnTradingPartyJob;
import org.schema.game.server.data.simulation.npc.NPCFaction;
import org.schema.game.server.data.simulation.npc.NPCFactionSpawn;
import org.schema.game.server.data.simulation.npc.NPCSpawnException;
import org.schema.game.server.data.simulation.npc.NPCStartupConfig;
import org.schema.game.server.data.simulation.npc.geo.NPCEntityContainer.NPCEntity;
import org.schema.game.server.data.simulation.npc.geo.NPCSystem;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;
import org.schema.schine.graphicsengine.forms.debug.DebugPoint;
import org.schema.schine.network.RegisteredClientInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.AdminLocalClient;
import org.schema.schine.network.server.AdminRemoteClient;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.network.server.ServerProcessor;
import org.schema.schine.resource.FileExt;
import org.schema.schine.resource.tag.Tag;

import javax.script.*;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.*;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.*;

import static org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager.WeaponType.*;

public class AdminCommandQueueElement {

	private Object[] commandParams;
	private AdminCommands command;
	private RegisteredClientInterface client;
	private int sqlT;

	public AdminCommandQueueElement(RegisteredClientInterface client, AdminCommands command, Object[] commandParams) {
		this.client = client;
		this.command = command;
		this.commandParams = commandParams;
	}

	public static SimpleTransformableSendableObject getControllerRoot(SimpleTransformableSendableObject s) {
		if(s instanceof SegmentController) {
			if(((SegmentController) s).getDockingController().isDocked()) {

				SegmentController segmentController = ((SegmentController) s).getDockingController().getDockedOn().to.getSegment().getSegmentController();

				if(segmentController.getDockingController().isDocked()) {
					SegmentController segmentController2 = segmentController.getDockingController().getDockedOn().to.getSegment().getSegmentController();

					return segmentController2;
				} else {
					return segmentController;
				}
			} else {
				return s;
			}
		} else {
			return s;
		}

	}

	private void activateWhiteList(GameServerState state) throws IOException {
		Boolean b = (Boolean) commandParams[0];
		ServerConfig.USE_WHITELIST.setOn(b);
		ServerConfig.write();
		client.serverMessage("[ADMIN COMMAND] Set WHITELIST activation to: " + b);

	}

	private void addAdmin(GameServerState state) throws IOException {
		String name = (String) commandParams[0];

		try {
			PlayerState playerFromName = state.getPlayerFromNameIgnoreCase(name);
			state.getController().addAdmin(client.getClientName(), playerFromName.getName());
		} catch (PlayerNotFountException e) {
			state.getController().addAdmin(client.getClientName(), name);
			client.serverMessage("[ADMIN COMMAND] [WARNING] '" + name + "' is NOT online. Please make sure you have the correct name. Name was still added to admin list");
		}

	}

	private void addAllItems(GameServerState state) throws IOException {
		try {
			String name = (String) commandParams[0];
			Integer count = (Integer) commandParams[1];
			PlayerState playerFromName = state.getPlayerFromName(name);

			Inventory inventory = playerFromName.getInventory(new Vector3i());
			IntOpenHashSet changed = new IntOpenHashSet();
			for(short type : ElementKeyMap.typeList()) {
				System.err.println("ADDING ITEM " + type + " " + count);
				int slot = inventory.incExistingOrNextFreeSlot(type, count);
				changed.add(slot);
			}
			playerFromName.sendInventoryModification(changed, Long.MIN_VALUE);
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] player not found for your client");
		}
	}

	private void addAllItemsCategory(GameServerState state) throws IOException {
		try {
			String name = (String) commandParams[0];
			Integer count = (Integer) commandParams[1];
			String cat = (String) commandParams[2];
			PlayerState playerFromName = state.getPlayerFromName(name);
			IntOpenHashSet changed = new IntOpenHashSet();
			Inventory inventory = playerFromName.getInventory(new Vector3i());
			for(short type : ElementKeyMap.typeList()) {

				if(belongsToCategory(cat, type)) {
					System.err.println("ADDING ITEM " + type + " " + count + " " + cat);
					int slot = inventory.incExistingOrNextFreeSlot(type, count);
					changed.add(slot);
//						playerFromName.sendInventoryModification(slot, Long.MIN_VALUE);
				}
			}
			playerFromName.sendInventoryModification(changed, Long.MIN_VALUE);
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] player not found for your client");
		} catch(UnknownCategoryException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] unknown category: " + e.getCat() + "");
		}
	}

	private void addFaction(final GameServerState state, boolean withFacId) throws IOException {
		String name;
		final int facId;
		final String leader;
		if(withFacId) {
			facId = ((Integer) commandParams[0]);
			name = ((String) commandParams[1]).trim();
			leader = ((String) commandParams[2]).trim();
		} else {
			facId = FactionManager.getNewId();
			name = ((String) commandParams[0]).trim();
			leader = ((String) commandParams[1]).trim();
		}
		if(name.length() > 0) {
			Faction faction = new Faction(state, facId, name, "description goes here");
			faction.setOpenToJoin(true);
			faction.addOrModifyMember("ADMIN", leader, FactionRoles.INDEX_ADMIN_ROLE, System.currentTimeMillis(), state.getGameState(), false);
			state.getGameState().getFactionManager().addFaction(faction);

			faction.addHook = new FactionAddCallback() {

				@Override
				public void callback() {
					try {
						PlayerState playerFromName = state
								.getPlayerFromName(leader);
						playerFromName.getFactionController().setFactionId(facId);
					} catch (PlayerNotFountException e) {
						try {
							File playerFile = new FileExt(GameServerState.ENTITY_DATABASE_PATH + "ENTITY_PLAYERSTATE_" + leader + ".ent");
							if (playerFile.exists()) {
								Tag readFrom = Tag.readFrom(new FileInputStream(playerFile), true, false);
								((Tag[]) ((Tag[]) readFrom.getValue())[6].getValue())[0].setValue(facId);
								readFrom.writeTo(new FileOutputStream(playerFile), true);
							} else {
								client.serverMessage("[ADMIN COMMAND] [ERROR] File not found: " + playerFile.getAbsolutePath());
							}
						} catch (Exception ex) {
							try {
								client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getClass().getSimpleName());
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							ex.printStackTrace();
						}
					}
				}
			};

			client.serverMessage("[ADMIN COMMAND] [SUCCESS] added new faction!");
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] name empty!");
		}
	}

	private void addFactionAmount(final GameServerState state, boolean withFacId) throws IOException {
		String name;
		int facId;
		String leader;
		int amount = (Integer) commandParams[1];
		for(int i = 0; i < amount; i++) {
			facId = FactionManager.getNewId();
			name = ((String) commandParams[0]).trim() + i;
			leader = "Random" + i;
			if(name.length() > 0) {
				Faction faction = new Faction(state, facId, name, "description goes here");
				faction.setOpenToJoin(true);
				faction.addOrModifyMember("ADMIN", leader, FactionRoles.INDEX_ADMIN_ROLE, System.currentTimeMillis(), state.getGameState(), false);
				state.getGameState().getFactionManager().addFaction(faction);

				client.serverMessage("[ADMIN COMMAND] [SUCCESS] added new faction!");
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] name empty!");
			}
		}
	}

	private void banIp(GameServerState state, boolean byPlayerName, boolean timed) throws IOException {
		long time = -1;
		if(timed) {
			time = System.currentTimeMillis() + ((Integer) commandParams[1]).longValue() * 60000L;
		}
		if(!byPlayerName) {
			String ip = (String) commandParams[0];
			for(PlayerState p : state.getPlayerStatesByName().values()) {
				if(p.getIp().replace("/", "").equals(ip)) {
					kick(state, false, timed ? 2 : 1, p.getName());
				}
			}

			try {
				state.getController().addBannedIp(client.getClientName(), ip, time);
				client.serverMessage("[ADMIN COMMAND] successfully banned: " + ip);
			} catch(NoIPException e) {
				e.printStackTrace();
				client.serverMessage("[ADMIN COMMAND] [ERROR] not an IP: " + ip);
			}
		} else {
			String name = (String) commandParams[0];
			try {
				kick(state, false, timed ? 2 : 1, name);
				PlayerState playerFromName = state.getPlayerFromName(name);
				String ip = playerFromName.getIp().replaceAll("/", "");
				try {
					state.getController().addBannedIp(client.getClientName(), ip, time);
					client.serverMessage("[ADMIN COMMAND] successfully banned IP: " + ip + " of " + name);
				} catch(NoIPException e) {
					e.printStackTrace();
					client.serverMessage("[ADMIN COMMAND] [ERROR] not an IP: " + ip + " of " + name);
				}

			} catch(PlayerNotFountException e) {
				client.serverMessage("[ADMIN COMMAND] [ERROR] ban failed: " + name + " does not exist");
			}
		}
	}

	private void unBanAccount(GameServerState state) throws IOException {
		String name = (String) commandParams[0];
		state.getController().removeBannedAccount(client.getClientName(), name);
		client.serverMessage("[ADMIN COMMAND] successfully unbanned account: " + name);
	}

	private void banAccount(GameServerState state, boolean byPlayerName, boolean timed) throws IOException {
		String name = (String) commandParams[0];

		long time = -1;
		String tmp = "";
		if(timed) {
			time = System.currentTimeMillis() + ((Integer) commandParams[1]).longValue() * 60000L;
		}
		if(!byPlayerName) {
			for(PlayerState p : state.getPlayerStatesByName().values()) {
				if(p.getStarmadeName() != null && p.getStarmadeName().equals(name)) {
					kick(state, false, timed ? 2 : 1, p.getName());
				}
			}
			state.getController().addBannedAccount(client.getClientName(), name, time);
			client.serverMessage("[ADMIN COMMAND] successfully banned account: " + name);
		} else {
			try {
				PlayerState playerFromName = state.getPlayerFromName(name);

				if(playerFromName.getStarmadeName() != null) {

					kick(state, false, timed ? 2 : 1, name);
					state.getController().addBannedAccount(client.getClientName(), playerFromName.getStarmadeName(), time);
					client.serverMessage("[ADMIN COMMAND] successfully banned account of: " + name + " -> " + playerFromName.getStarmadeName());

				} else {
					client.serverMessage("[ADMIN COMMAND] [ERROR] ban failed: " + name + " not uplinked");
				}
			} catch(PlayerNotFountException e) {
				client.serverMessage("[ADMIN COMMAND] [ERROR] ban failed: " + name + " does not exist");
			}
		}
	}

	private void banName(GameServerState state) throws IOException {
		String name = (String) commandParams[0];
		boolean kick = (Boolean) commandParams[1];
		String reason = commandParams.length >= 3 ? (String) commandParams[2] : "";
		long time = -1;
		String tmp = "";
		// If time parameter is present
		if(commandParams.length >= 4) {
			time = System.currentTimeMillis() + ((Integer) commandParams[3]).longValue() * 60000L;
			tmp = " (Temporary Ban for " + StringTools.formatTimeFromMS(time - System.currentTimeMillis()) + ")";

			// If reason parameter is present
			if(reason.length() > 0) {
				reason = reason + tmp;
			}
		}
		state.getController().addBannedName(client.getClientName(), name, time);
		if (kick) {
			if (reason.length() > 0) {
				kick(state, reason);
			} else {
				kick(state, "You have been banned by an admin for " + tmp);
			}
		}
		client.serverMessage("[ADMIN COMMAND] successfully banned: " + name);
	}

	private boolean belongsToCategory(String cat, short type) throws UnknownCategoryException {

		return ElementKeyMap.getInfo(type).getType().hasParent(cat);

//		Class<? extends Element> ty = ElementKeyMap.getInfo(type).getType();
//		if (cat.equals("terrain")) {
//			return TerrainElement.class.isAssignableFrom(ty);
//		} else if (cat.equals("ship")) {
//			return ShipElement.class.isAssignableFrom(ty);
//		} else if (cat.equals("station")) {
//			return SpaceStationElement.class.isAssignableFrom(ty);
//		} else if (cat.equals("logic")) {
//			return LogicElement.class.isAssignableFrom(ty);
//		}else if (cat.equals("hull")) {
//			return HullElement.class.isAssignableFrom(ty);
//		}else if (cat.equals("manufacturing")) {
//			return ManufacturingElement.class.isAssignableFrom(ty);
//		} else if (cat.equals("factory")) {
//			return FactoryElement.class.isAssignableFrom(ty);
//		}
//		throw new UnknownCategoryException(cat);

	}

	private ClosestRayResultCallback getCollision(Vector3f pos, Vector3f forw, GameServerState state, SimpleTransformableSendableObject firstControlledTransformable) throws NoCollisioinFountException, IOException {

		Vector3f camPos = new Vector3f(pos);
		Vector3f camTo = new Vector3f(pos);
		forw.scale(8000);
		camTo.add(forw);

		//		SubsimplexRayCubesCovexCast.debug = true;
		CubeRayCastResult rayCallback = new CubeRayCastResult(camPos, camTo, false);
		rayCallback.setIgnoereNotPhysical(true);
		rayCallback.setOnlyCubeMeshes(true);
		ClosestRayResultCallback testRayCollisionPoint = firstControlledTransformable.getPhysics().testRayCollisionPoint(camPos, camTo, rayCallback, false);
		//		SubsimplexRayCubesCovexCast.debug = false;
		return testRayCollisionPoint;

//		Vector3f to = new Vector3f(pos);
//		forw.scale(8000 );
//		to.add(forw);
//		Sector sector = state.getUniverse().getSector(
//				firstControlledTransformable.getSectorId());
//		ClosestRayResultCallback testRayCollisionPoint = ((PhysicsExt) sector
//				.getPhysics()).testRayCollisionPoint(pos, to, false, null,
//						null, false, true);
//
//		return testRayCollisionPoint;
	}

	private SegmentPiece getPiece(Vector3f pos, Vector3f forw, GameServerState state, SimpleTransformableSendableObject firstControlledTransformable, Vector3i p) throws NoCollisioinFountException, IOException {
		Vector3f to = new Vector3f(pos);
		forw.scale(8000);
		to.add(forw);
		Sector sector = state.getUniverse().getSector(firstControlledTransformable.getSectorId());
		ClosestRayResultCallback testRayCollisionPoint = ((PhysicsExt) sector.getPhysics()).testRayCollisionPoint(pos, to, false, null, null, false, true, false);
		if(testRayCollisionPoint.hasHit()) {
			CubeRayCastResult cr = (CubeRayCastResult) testRayCollisionPoint;
			if(cr.getSegment() != null) {
				SegmentPiece pr = new SegmentPiece(cr.getSegment(), cr.getCubePos());
				pr.getAbsolutePos(p);
				Vector3i relP = new Vector3i(p);
				relP.add(-SegmentData.SEG_HALF, -SegmentData.SEG_HALF, -SegmentData.SEG_HALF);

				cr.getSegment().getSegmentController().getWorldTransformInverse().transform(testRayCollisionPoint.hitPointWorld);

				SegmentPiece piece = pr.getSegment().getSegmentController().getSegmentBuffer().getPointUnsave(p, pr); //autorequest true previously
				int side = Element.getSide(cr.hitPointWorld, piece == null ? null : piece.getAlgorithm(), relP, piece != null ? piece.getType() : (short)0, piece != null ? piece.getOrientation() : 0);
				switch(side) {
					case (Element.RIGHT) -> p.x += 1f;
					case (Element.LEFT) -> p.x -= 1f;
					case (Element.TOP) -> p.y += 1f;
					case (Element.BOTTOM) -> p.y -= 1f;
					case (Element.FRONT) -> p.z += 1f;
					case (Element.BACK) -> p.z -= 1f;
					default -> System.err.println("[BUILDMODEDRAWER] WARNING: NO SIDE recognized!!!");
				}

				pr = pr.getSegment().getSegmentController().getSegmentBuffer().getPointUnsave(p, pr);
				return pr;
			}
		} else {
			System.err.println("[ADMIN] no collision point: " + pos + " + " + forw + " -> " + to);
		}

		throw new NoCollisioinFountException();
	}

	private void testPath(GameServerState state, CreatureCommand c) throws IOException {
		PlayerState playerFromName;
		try {
			playerFromName = state.getPlayerFromStateId(client.getId());

			SimpleTransformableSendableObject firstControlledTransformable = playerFromName.getFirstControlledTransformable();
			Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(playerFromName.getNetworkObject().selectedEntityId.get());

			if(sendable != null && sendable instanceof AICreature<?>) {
				AICreature<AIPlayer> creature = (AICreature<AIPlayer>) sendable;

				Vector3f pos;

				if(firstControlledTransformable instanceof AbstractCharacter<?>) {
					pos = new Vector3f(((AbstractCharacter<?>) firstControlledTransformable).getHeadWorldTransform().origin);
				} else {
					pos = new Vector3f(firstControlledTransformable.getWorldTransform().origin);

				}

				Vector3f forw = playerFromName.getForward(new Vector3f());
				Vector3i p = new Vector3i();
				SegmentPiece pr = getPiece(pos, forw, state, firstControlledTransformable, p);

				switch(c) {
					case IDLE:
						((AIGameConfiguration<?, ?>) creature.getAiConfiguration()).get(Types.ORDER).switchSetting(AIGameCreatureConfiguration.BEHAVIOR_IDLING, true);
						creature.getOwnerState().standUp();
						client.serverMessage("[ADMIN COMMAND] creature " + creature + " idling");
						break;
					case ROAM:
						Vector3i absolutePos = pr.getAbsolutePos(new Vector3i());
						((AIGameConfiguration<?, ?>) creature.getAiConfiguration()).get(Types.ORIGIN_X).switchSetting(String.valueOf(absolutePos.x), true);
						((AIGameConfiguration<?, ?>) creature.getAiConfiguration()).get(Types.ORIGIN_Y).switchSetting(String.valueOf(absolutePos.y), true);
						((AIGameConfiguration<?, ?>) creature.getAiConfiguration()).get(Types.ORIGIN_Z).switchSetting(String.valueOf(absolutePos.z), true);
						((AIGameConfiguration<?, ?>) creature.getAiConfiguration()).get(Types.ORDER).switchSetting(AIGameCreatureConfiguration.BEHAVIOR_ROAMING, true);
						creature.getOwnerState().standUp();
						client.serverMessage("[ADMIN COMMAND] creature " + creature + " roaming");
						break;

					case STAND_UP:
						creature.getOwnerState().standUp();
						client.serverMessage("[ADMIN COMMAND] creature " + creature + " standing up");
						break;
					case SIT:
						ClosestRayResultCallback collision = getCollision(pos, forw, state, firstControlledTransformable);
						if(!collision.hasHit()) {
							client.serverMessage("[ERROR] " + creature + " Cannot sit. Your line of sight has no collision");
						} else {
							creature.sitDown(collision, pos, forw, 8000);
							client.serverMessage("[ADMIN COMMAND] sit down: " + creature + ": " + creature.getOwnerState().isSitting());
						}
						break;
					case GOTO:
						creature.getOwnerState().plotPath(pr, true);
						client.serverMessage("[ADMIN COMMAND] testing path plotted");
						break;
					case GRAVITY:
						if(pr != null) {
							creature.enableGravityOnAI(pr.getSegment().getSegmentController(), new Vector3f(0, -9.81f, 0));
							;
							client.serverMessage("[ADMIN COMMAND] gravity engaged");
						} else {
							//resets gravity
							creature.enableGravityOnAI(null, new Vector3f(0, 0, 0));
							client.serverMessage("[ADMIN COMMAND] gravity disengaged");
						}

						break;
					default:
						break;
				}

				if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()) {
					Vector3f vv = new Vector3f(p.x - SegmentData.SEG_HALF, p.y - SegmentData.SEG_HALF, p.z - SegmentData.SEG_HALF);
					pr.getSegment().getSegmentController().getWorldTransform().transform(vv);
					DebugPoint debugPoint = new DebugPoint(vv, new Vector4f(1, 0, 0, 1));
					debugPoint.size = 0.3f;
					debugPoint.LIFETIME = 10000;
					DebugDrawer.points.add(debugPoint);
				}

				return;

			} else {
				client.serverMessage("[ERROR][ADMIN COMMAND] OBJECT TO DESTROY NOT FOUND");
			}

		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] player not found for your client");
		} catch(NoCollisioinFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] no collision found");
		} catch(Exception e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] server could not load sector");
		}

	}

	private void testBreaking(GameServerState state) throws IOException {
		PlayerState playerFromName;
		try {
			playerFromName = state.getPlayerFromStateId(client.getId());

			SimpleTransformableSendableObject firstControlledTransformable = playerFromName.getFirstControlledTransformable();
			Vector3f pos;
			if(firstControlledTransformable instanceof AbstractCharacter<?>) {
				pos = new Vector3f(((AbstractCharacter<?>) firstControlledTransformable).getHeadWorldTransform().origin);
			} else {
				pos = new Vector3f(firstControlledTransformable.getWorldTransform().origin);

			}
			Vector3f to = new Vector3f(pos);
			Vector3f forw = playerFromName.getForward(new Vector3f());
			forw.scale(5000 * Element.BLOCK_SIZE);
			to.add(forw);
			Sector sector = state.getUniverse().getSector(firstControlledTransformable.getSectorId());
			ClosestRayResultCallback testRayCollisionPoint = ((PhysicsExt) sector.getPhysics()).testRayCollisionPoint(pos, to, false, null, null, false, true, false);
			if(testRayCollisionPoint.hasHit()) {
				CubeRayCastResult cr = (CubeRayCastResult) testRayCollisionPoint;
				if(cr.getSegment() != null) {
					SegmentPiece p = new SegmentPiece(cr.getSegment(), cr.getCubePos());
					state.getController().queueSegmentControllerBreak(p);

				}

				client.serverMessage("[ADMIN COMMAND] testing break ");
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] no object in line of sight");
			}
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] player not found for your client");
		} catch(Exception e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] server could not load sector");
		}

	}

	private void changeSector(GameServerState state, boolean copy) throws IOException {
		PlayerState playerFromName;
		try {
			playerFromName = state.getPlayerFromStateId(client.getId());

			Vector3i sec = new Vector3i((Integer) commandParams[0], (Integer) commandParams[1], (Integer) commandParams[2]);
			Sector sector = state.getUniverse().getSector(sec);
			if(sector != null) {
				for(ControllerStateUnit u : playerFromName.getControllerState().getUnits()) {
					if(u.playerControllable instanceof SimpleTransformableSendableObject) {
						state.getController().queueSectorSwitch(getControllerRoot((SimpleTransformableSendableObject) u.playerControllable), sector.pos, SectorSwitch.TRANS_JUMP, copy, true, true);

					}
				}
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] sector not found: " + sec + ": " + state.getUniverse().getSectorSet());
			}
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] player not found for your client");
		} catch(Exception e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] server could not load sector");
		}

	}

	private void changeSectorPlayer(GameServerState state, boolean copy) throws IOException {
		PlayerState playerFromName;
		try {
			playerFromName = state.getPlayerFromName(((String) commandParams[0]).trim());

			Vector3i sec = new Vector3i((Integer) commandParams[1], (Integer) commandParams[2], (Integer) commandParams[3]);
			Sector sector = state.getUniverse().getSector(sec);
			if(sector != null) {
				boolean hasObj = false;
				for(ControllerStateUnit u : playerFromName.getControllerState().getUnits()) {
					if(u.playerControllable instanceof SimpleTransformableSendableObject) {
						state.getController().queueSectorSwitch(getControllerRoot((SimpleTransformableSendableObject) u.playerControllable), sector.pos, SectorSwitch.TRANS_JUMP, copy, true, true);
						hasObj = true;
					}
				}
				if(hasObj) {
					client.serverMessage("[ADMIN COMMAND] [SUCCESS] changed sector for " + playerFromName.getName() + " to " + sec);
				} else {
					client.serverMessage("[ADMIN COMMAND] [ERROR] not changed sector for " + playerFromName.getName() + " to " + sec + ": Player is not bound to any entity");
				}
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] sector not found: " + sec + ": " + state.getUniverse().getSectorSet());
			}
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] player not found for your client " + (String) commandParams[0]);
		} catch(Exception e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] server could not load sector");
		}

	}

	private void changeSectorUid(GameServerState state, boolean copy) throws IOException {
		try {
			String uid = (String) commandParams[0];
			Sendable s = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(uid);

			if(s != null && s instanceof SimpleTransformableSendableObject) {
				Vector3i sec = new Vector3i((Integer) commandParams[1], (Integer) commandParams[2], (Integer) commandParams[3]);
				Sector sector = state.getUniverse().getSector(sec);
				if(sector != null) {

					state.getController().queueSectorSwitch(getControllerRoot((SimpleTransformableSendableObject) s), sector.pos, SectorSwitch.TRANS_JUMP, copy, true, true);

					client.serverMessage("[ADMIN COMMAND] [SUCCESS] changed sector for " + uid + " to " + sec);

				} else {
					client.serverMessage("[ADMIN COMMAND] [ERROR] sector not found: " + sec + ": " + state.getUniverse().getSectorSet());
				}
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] sector not found: " + uid + " not found or loaded");
			}
		} catch(Exception e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] server could not load sector");
		}

	}

	private void changeSectorSelected(GameServerState state, boolean copy) throws IOException {
		try {
			PlayerState playerFromName = state.getPlayerFromStateId(client.getId());
			Sendable s = state.getLocalAndRemoteObjectContainer().getLocalObjects().get((int) playerFromName.getNetworkObject().selectedEntityId.get());
			if(s != null && s instanceof SimpleTransformableSendableObject) {
				Vector3i sec = new Vector3i((Integer) commandParams[0], (Integer) commandParams[1], (Integer) commandParams[2]);
				Sector sector = state.getUniverse().getSector(sec);
				if(sector != null) {

					state.getController().queueSectorSwitch(getControllerRoot((SimpleTransformableSendableObject) s), sector.pos, SectorSwitch.TRANS_JUMP, copy, true, true);

					client.serverMessage("[ADMIN COMMAND] [SUCCESS] changed sector for " + s + " to " + sec);

				} else {
					client.serverMessage("[ADMIN COMMAND] [ERROR] sector not found: " + s + ": " + state.getUniverse().getSectorSet());
				}
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] sector not found: " + s + " not found or loaded");
			}
		} catch(Exception e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] server could not load sector");
		}

	}

	private void creatureAnimation(GameServerState state, boolean start) throws IOException {

		PlayerState playerFromName;

		try {
			playerFromName = state.getPlayerFromStateId(client.getId());

			System.err.println("[ADMIN COMMAND] checking selected");
			Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(playerFromName.getNetworkObject().selectedEntityId.get());
			if(sendable != null && sendable instanceof AICreature<?>) {
				AICreature<?> c = (AICreature<?>) sendable;
				if(start) {
					String animation = (String) commandParams[0];
					String loopMode = (String) commandParams[1];
					float speed = (Float) commandParams[2];
					boolean fullBody = (Boolean) commandParams[3];
					c.forceAnimation(animation, loopMode, speed, fullBody);
					client.serverMessage("[ADMIN COMMAND] [SUCCESS] Started animation");
				} else {
					c.stopForcedAnimation();
					client.serverMessage("[ADMIN COMMAND] [SUCCESS] Stopped animation");
				}
			}

		} catch(Exception e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}
	}

	private void runScript() throws IOException {
		String name = (String) commandParams[0];
		String function = (String) commandParams[1];
		try {
			FileExt file = new FileExt("./data/script/" + name);
			if(file.exists()) {
				ScriptEngineManager manager = new ScriptEngineManager();
				ScriptEngine engine = manager.getEngineByName("luaj");
				BufferedReader reader = new BufferedReader(new FileReader(file));
				GameServerState.scriptThreads.add(new ScriptThread(engine, reader, function, file.getName().replace(".lua", "")));
				client.serverMessage("[ADMIN COMMAND] [SUCCESS] script executed: " + name);
			} else client.serverMessage("[ADMIN COMMAND] [ERROR] script not found: " + name);
		} catch(Exception exception) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] script not found: " + name);
		}
	}

	private void listScripts() throws IOException {
		StringBuilder builder = new StringBuilder();
		for(ScriptThread thread : GameServerState.scriptThreads) {
			builder.append(thread.getName());
			builder.append("[");
			builder.append(thread.getRunTime());
			builder.append("ms]");
		}
		client.serverMessage("[ADMIN COMMAND] [SUCCESS] Currently running scripts: " + builder);
	}

	private void creatureScript(GameServerState state) throws IOException {
		String name = (String) commandParams[0];
		PlayerState playerFromName;
		try {
			playerFromName = state.getPlayerFromStateId(client.getId());

			System.err.println("[ADMIN COMMAND]checking selected");
			Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(playerFromName.getNetworkObject().selectedEntityId.get());
			if(sendable != null && sendable instanceof SimpleTransformableSendableObject) {
				SimpleTransformableSendableObject s = (SimpleTransformableSendableObject) sendable;
				if(s instanceof AICreature<?>) {
					if((new FileExt("./data/script/" + name).exists())) {
						((AICreature<?>) s).getOwnerState().setConversationScript(name);

						client.serverMessage("[ADMIN COMMAND] [SUCCESS] script of " + s + " set to " + name);
					} else {
						client.serverMessage("[ADMIN COMMAND] [ERROR] script not found: " + (new FileExt("./data/script/" + name).getAbsolutePath()));
					}
				}
			}

		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}
	}

	private void creatureRename(GameServerState state) throws IOException {
		String name = (String) commandParams[0];
		PlayerState playerFromName;
		try {
			playerFromName = state.getPlayerFromStateId(client.getId());

			System.err.println("[ADMIN COMMAND]checking selected");
			Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(playerFromName.getNetworkObject().selectedEntityId.get());
			if(sendable != null && sendable instanceof SimpleTransformableSendableObject) {
				SimpleTransformableSendableObject s = (SimpleTransformableSendableObject) sendable;
				if(s instanceof AICreature<?>) {
					((AICreature<?>) s).setRealName(name);
					client.serverMessage("[ADMIN COMMAND] [SUCCESS] renamed object to" + name);
				}
			}

		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}
	}

	private void checkFactionMembers(GameServerState state) throws IOException {
		Integer code = (Integer) commandParams[0];
		if(code != null) {
			Faction faction = state.getGameState().getFactionManager().getFaction(code);
			if(faction != null) {
				client.serverMessage("[ADMIN COMMAND] [SUCCESS] " + faction.getName() + ": " + faction.getMembersUID());
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] Faction Not Found: " + code);
			}

		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] Faction Not Found (must be ID, list with /faction_list: " + code);
		}

	}

	private void checkFactions(GameServerState state) {
		state.getGameState().getFactionManager().flagCheckFactions();
	}

	private void chmodSector(GameServerState state) throws IOException {
		try {

			Vector3i sec = new Vector3i((Integer) commandParams[0], (Integer) commandParams[1], (Integer) commandParams[2]);
			Sector sector = state.getUniverse().getSector(sec);

			String op = (String) commandParams[3];

			if(!op.equals("+") && !op.equals("-")) {
				client.serverMessage("[ADMIN COMMAND] [ERROR] operator must be either + or -");
				return;
			}
			String mode = (String) commandParams[4];
			if(!mode.toLowerCase(Locale.ENGLISH).equals("peace") && !mode.toLowerCase(Locale.ENGLISH).equals("protected") && !mode.toLowerCase(Locale.ENGLISH).equals("noenter") && !mode.toLowerCase(Locale.ENGLISH).equals("noindications") && !mode.toLowerCase(Locale.ENGLISH).equals("noexit") && !mode.toLowerCase(Locale.ENGLISH).equals("nofploss")) {
				client.serverMessage("[ADMIN COMMAND] [ERROR] mode must be either 'peace' or 'protected' or 'noenter' or 'noexit' or 'noindications' or 'nofploss' (without quotes)");
				return;
			}
			if(mode.toLowerCase(Locale.ENGLISH).equals("protected")) {
				sector.protect(op.toLowerCase(Locale.ENGLISH).equals("+"));
			} else if(mode.toLowerCase(Locale.ENGLISH).equals("peace")) {
				sector.peace(op.toLowerCase(Locale.ENGLISH).equals("+"));
			} else if(mode.toLowerCase(Locale.ENGLISH).equals("noenter")) {
				sector.noEnter(op.toLowerCase(Locale.ENGLISH).equals("+"));
			} else if(mode.toLowerCase(Locale.ENGLISH).equals("noexit")) {
				sector.noExit(op.toLowerCase(Locale.ENGLISH).equals("+"));
			} else if(mode.toLowerCase(Locale.ENGLISH).equals("noindications")) {
				sector.noIndications(op.toLowerCase(Locale.ENGLISH).equals("+"));
			} else if(mode.toLowerCase(Locale.ENGLISH).equals("nofploss")) {
				sector.noFpLoss(op.toLowerCase(Locale.ENGLISH).equals("+"));
			}
			client.serverMessage("[ADMIN COMMAND] [SUCCESS] set " + mode + " on " + sector.pos + " to '" + op.toLowerCase(Locale.ENGLISH).equals("+") + "'");
		} catch(Exception e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] server could not load sector");
		}

	}

	private void countdown(GameServerState state) {
		int secs = (Integer) commandParams[0];
		StringBuffer message = new StringBuffer();
		for(int i = 1; i < commandParams.length; i++) {
			message.append(commandParams[i]);
			message.append(" ");
		}

		state.addCountdownMessage(secs, message.toString());

	}

	private void dayTime(GameServerState state) throws IOException {
		int timeInHours = (Integer) commandParams[0];
		if(timeInHours >= 0) {
			float t = timeInHours % 24 / 24f;
			state.setServerTimeMod(state.getController().getUniverseDayInMs() - (long) (t * state.getController().getUniverseDayInMs()));
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] daytime must be >= 0");
		}
	}

	private void debugFSMInfo() throws IOException {
		Boolean b = (Boolean) commandParams[0];

		ServerConfig.DEBUG_FSM_STATE.setOn(b);
		ServerConfig.write();
		client.serverMessage("[ADMIN COMMAND] Set FSM dbug to: " + b);

	}

	private void deleteFaction(GameServerState state) throws IOException {
		Integer code = (Integer) commandParams[0];
		try {
			state.getGameState().getFactionManager().removeFaction(code);

			client.serverMessage("[ADMIN COMMAND] [SUCCESS] deleted faction " + code);
		} catch(FactionNotFoundException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] Faction Not Found: " + code);
		}
	}

	private void destroyUID(GameServerState state, boolean dock, boolean dockOnly) throws IOException {
		String entity = ((String) commandParams[0]);
		PlayerState playerFromName;

		Sendable sendable = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(entity);

		if(sendable != null) {
			if(dock && sendable instanceof SegmentController) {
				SegmentController s = (SegmentController) sendable;
				s.railController.destroyDockedRecursive();
				for(ElementDocking d : s.getDockingController().getDockedOnThis()) {
					client.serverMessage("[ADMIN COMMAND] DESTROY DOCK: " + d.from.getSegment().getSegmentController());
					LogUtil.log().fine("[ADMIN COMMAND] destroying entity: " + d);
					d.from.getSegment().getSegmentController().markForPermanentDelete(true);
					d.from.getSegment().getSegmentController().setMarkedForDeleteVolatile(true);
				}
			}

			if(sendable instanceof PlanetIcoCore) {
				synchronized(state.getLocalAndRemoteObjectContainer().getLocalObjects()) {
					for(Sendable s : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
						if(s instanceof PlanetIco) {
							if(((PlanetIco) s).getCore() == sendable) {
								sendable.markForPermanentDelete(true);
								sendable.setMarkedForDeleteVolatile(true);
							}
						}
					}
				}
			}
			if(sendable instanceof SimpleTransformableSendableObject && sendable instanceof TransientSegmentController) {
				Sector sector = state.getUniverse().getSector(((SimpleTransformableSendableObject) sendable).getSectorId());
				if(sector != null) {
					sector.setTransientSector(false);
				}
			}

			if(!dockOnly) {
				client.serverMessage("[ADMIN COMMAND] DESTROY: " + sendable);
				LogUtil.log().fine("[ADMIN COMMAND] destroying entity: " + sendable);
				sendable.markForPermanentDelete(true);
				sendable.setMarkedForDeleteVolatile(true);
			}

		} else {
			client.serverMessage("[ERROR][ADMIN COMMAND] OBJECT TO DESTROY NOT FOUND");
		}

	}

	private void despawnAll(GameServerState state) throws IOException {
		String entity = ((String) commandParams[0]);
		String unused = ((String) commandParams[1]).trim().toLowerCase(Locale.ENGLISH);
		boolean shipOnly = ((Boolean) commandParams[2]);

		EntityTable.Despawn mode = null;

		if(unused.equals("all")) {
			mode = EntityTable.Despawn.ALL;
		}
		if(unused.equals("unused")) {
			mode = EntityTable.Despawn.UNUSED;
		}
		if(unused.equals("used")) {
			mode = EntityTable.Despawn.USED;
		}
		if(mode != null) {
			try {
				for(Sendable s : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
					if(s instanceof SegmentController && (!shipOnly || s instanceof Ship)) {
						if(DatabaseEntry.removePrefix(((SegmentController) s).getUniqueIdentifier()).startsWith(entity)) {
							s.markForPermanentDelete(true);
							s.setMarkedForDeleteVolatile(true);
							client.serverMessage("[ADMIN COMMAND] [SUCCESS] ACTIVE DESPAWNED " + s + " ");
						}
					}
				}
				String escSer = DatabaseIndex.escape(entity) + "%";
				System.err.println("[DESPAWN] using escaped matching string '" + escSer + "'");
				int count = state.getDatabaseIndex().getTableManager().getEntityTable().despawn(escSer, mode, null, shipOnly ? EntityType.SHIP : null);
				client.serverMessage("[ADMIN COMMAND] [SUCCESS] DESPAWNED " + count + " ENTITIES (MODE: " + mode.name() + ")");
			} catch(SQLException e) {
				e.printStackTrace();
				client.serverMessage("[ADMIN COMMAND] [ERROR] SQL EXCEPTION");
			}
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] mode '" + mode + "' unknown. mist be 'used', 'unused', or 'all'");
		}
	}

	private void despawnSector(GameServerState state) throws IOException {
		String entity = ((String) commandParams[0]);
		String unused = ((String) commandParams[1]).trim().toLowerCase(Locale.ENGLISH);
		boolean shipOnly = ((Boolean) commandParams[2]);

		//		/despawn_all "a_b c" all false

		int x = ((Integer) commandParams[3]);
		int y = ((Integer) commandParams[4]);
		int z = ((Integer) commandParams[5]);

		EntityTable.Despawn mode = null;

		if(unused.equals("all")) {
			mode = EntityTable.Despawn.ALL;
		}
		if(unused.equals("unused")) {
			mode = EntityTable.Despawn.UNUSED;
		}
		if(unused.equals("used")) {
			mode = EntityTable.Despawn.USED;
		}
		Vector3i sectorPos = new Vector3i(x, y, z);
		if(mode != null) {
			try {
				for(Sendable s : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
					if(s instanceof SegmentController && (!shipOnly || s instanceof Ship)) {
						if(mode == EntityTable.Despawn.UNUSED && ((SegmentController) s).getLastModifier().length() == 0) {
							continue;
						}
						if(mode == EntityTable.Despawn.USED && ((SegmentController) s).getLastModifier().length() > 0) {
							continue;
						}

						if(DatabaseEntry.removePrefix(((SegmentController) s).getUniqueIdentifier()).startsWith(entity) && ((SegmentController) s).getSectorId() == state.getUniverse().getSector(sectorPos).getId()) {
							s.markForPermanentDelete(true);
							s.setMarkedForDeleteVolatile(true);
							client.serverMessage("[ADMIN COMMAND] [SUCCESS] ACTIVE DESPAWNED " + s + "");
						}
					}
				}
				String escSer = DatabaseIndex.escape(entity) + "%";
				System.err.println("[DESPAWN] using escaped matching string '" + escSer + "'");
				int count = state.getDatabaseIndex().getTableManager().getEntityTable().despawn(escSer, mode, sectorPos, shipOnly ? EntityType.SHIP : null);
				client.serverMessage("[ADMIN COMMAND] [SUCCESS] DESPAWNED " + count + " ENTITIES IN SECTOR: " + sectorPos + " (MODE: " + mode.name() + ")");
			} catch(SQLException e) {
				e.printStackTrace();
				client.serverMessage("[ADMIN COMMAND] [ERROR] SQL EXCEPTION");
			} catch(Exception e) {
				e.printStackTrace();
				client.serverMessage("[ADMIN COMMAND] [ERROR] COULD NOT LOAD SECTOR: " + sectorPos);
			}
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] mode '" + mode + "' unknown. mist be 'used', 'unused', or 'all'");
		}
	}

	private void removeSpawners(GameServerState state) throws IOException {
		PlayerState playerFromName;
		try {
			playerFromName = state.getPlayerFromStateId(client.getId());

			SimpleTransformableSendableObject firstControlledTransformable = null;

			Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(playerFromName.getNetworkObject().selectedEntityId.get());

			if(sendable != null && sendable instanceof SimpleTransformableSendableObject) {
				((SimpleTransformableSendableObject) sendable).getSpawnController().removeAll();

			}
		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ERROR][ADMIN COMMAND] Player not found for client " + client);
		}
	}

	private void softDespawn(GameServerState state, Int2ObjectOpenHashMap<Sendable> localObjects, boolean dock, boolean dockOnly) throws IOException {
		PlayerState playerFromName;
		try {
			playerFromName = state.getPlayerFromStateId(client.getId());

			SimpleTransformableSendableObject firstControlledTransformable = null;

			Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(playerFromName.getNetworkObject().selectedEntityId.get());

			if(sendable != null) {

				if(dock && sendable instanceof SegmentController) {
					SegmentController s = (SegmentController) sendable;
					s.railController.destroyDockedRecursive();
					for(ElementDocking d : s.getDockingController().getDockedOnThis()) {
						client.serverMessage("[ADMIN COMMAND] DESTROY DOCK: " + d.from.getSegment().getSegmentController());
						LogUtil.log().fine("[ADMIN COMMAND] destroying entity: " + d);
						d.from.getSegment().getSegmentController().setMarkedForDeleteVolatile(true);
					}
				}
				if(sendable instanceof PlanetIcoCore) {
					synchronized(state.getLocalAndRemoteObjectContainer().getLocalObjects()) {
						for(Sendable s : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
							if(s instanceof PlanetIco) {
								if(((PlanetIco) s).getCore() == sendable) {
									s.setMarkedForDeleteVolatile(true);
								}
							}
						}
					}
				}
				if(sendable instanceof SimpleTransformableSendableObject && sendable instanceof TransientSegmentController) {
					Sector sector = state.getUniverse().getSector(((SimpleTransformableSendableObject) sendable).getSectorId());
					if(sector != null) {
						sector.setTransientSector(false);
					}
				}
				if(!dockOnly) {
					client.serverMessage("[ADMIN COMMAND] DESTROY: " + sendable);
					LogUtil.log().fine("[ADMIN COMMAND] destroying entity: " + sendable);
					sendable.setMarkedForDeleteVolatile(true);
					if(!dock && sendable instanceof SegmentController) {
						SegmentController s = (SegmentController) sendable;
						s.railController.undockAllServer();
						for(RailRelation r : s.railController.next) {
							r.docked.getSegmentController().railController.disconnect();
						}
					}
				}

			} else {
				client.serverMessage("[ERROR][ADMIN COMMAND] OBJECT TO DESTROY NOT FOUND");
			}
		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ERROR][ADMIN COMMAND] Player not found for client " + client);
		}
	}

	private void destroyEntity(GameServerState state, Int2ObjectOpenHashMap<Sendable> localObjects, boolean dock, boolean dockOnly) throws IOException {
		PlayerState playerFromName;
		try {
			playerFromName = state.getPlayerFromStateId(client.getId());

			SimpleTransformableSendableObject firstControlledTransformable = null;

			Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(playerFromName.getNetworkObject().selectedEntityId.get());

			if(sendable != null) {

				if(dock && sendable instanceof SegmentController) {
					SegmentController s = (SegmentController) sendable;
					s.railController.destroyDockedRecursive();
					for(ElementDocking d : s.getDockingController().getDockedOnThis()) {
						client.serverMessage("[ADMIN COMMAND] DESTROY DOCK: " + d.from.getSegment().getSegmentController());
						LogUtil.log().fine("[ADMIN COMMAND] destroying entity: " + d);
						d.from.getSegment().getSegmentController().markForPermanentDelete(true);
						d.from.getSegment().getSegmentController().setMarkedForDeleteVolatile(true);
					}
				}
				if(sendable instanceof PlanetIcoCore) {
					synchronized(state.getLocalAndRemoteObjectContainer().getLocalObjects()) {
						for(Sendable s : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
							if(s instanceof PlanetIco) {
								if(((PlanetIco) s).getCore() == sendable) {
									s.markForPermanentDelete(true);
									s.setMarkedForDeleteVolatile(true);
								}
							}
						}
					}
				}
				if(sendable instanceof SimpleTransformableSendableObject && sendable instanceof TransientSegmentController) {
					Sector sector = state.getUniverse().getSector(((SimpleTransformableSendableObject) sendable).getSectorId());
					if(sector != null) {
						sector.setTransientSector(false);
					}
				}
				if(!dockOnly) {
					if(!dock && sendable instanceof SegmentController) {
						SegmentController s = (SegmentController) sendable;
						s.railController.undockAllServer();
					}
					client.serverMessage("[ADMIN COMMAND] DESTROY: " + sendable);
					LogUtil.log().fine("[ADMIN COMMAND] destroying entity: " + sendable);
					sendable.markForPermanentDelete(true);
					sendable.setMarkedForDeleteVolatile(true);

				}

			} else {
				client.serverMessage("[ERROR][ADMIN COMMAND] OBJECT TO DESTROY NOT FOUND");
			}
		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ERROR][ADMIN COMMAND] Player not found for client " + client);
		}
	}

	private void enableDockingValidation(GameServerState state) throws IOException {
		Boolean b = (Boolean) commandParams[0];

		ServerConfig.write();

		client.serverMessage("[ADMIN COMMAND] Set DockingValidationIgnore to: " + b);

	}

	private void enableSimulation(GameServerState state) throws IOException {
		Boolean b = (Boolean) commandParams[0];

		ServerConfig.ENABLE_SIMULATION.setOn(b);
		ServerConfig.write();
		client.serverMessage("[ADMIN COMMAND] Set SIMULATION to: " + b);

	}

	public void execute(GameServerState state) throws IOException,
			AdminCommandNotFoundException {
		



		Int2ObjectOpenHashMap<Sendable> localObjects = state
				.getLocalAndRemoteObjectContainer().getLocalObjects();
		System.err.println("[ADMIN COMMAND] " + command.name() + " from "
				+ client + " params: " + Arrays.toString(commandParams));

		LogUtil.log().fine("[ADMINCOMMAND] " + client.getClientName() + " used: '" + command.name() + "' with args " + Arrays.toString(commandParams));

		try {
			switch(command) {
				case FACTION_LIST -> {
					listFactions(state);
					break;
				}
				case FACTION_CREATE_AS -> {
					addFaction(state, true);
					break;
				}
				case FACTION_CREATE_AMOUNT -> {
					addFactionAmount(state, true);
					break;
				}
				case FACTION_CREATE -> {
					addFaction(state, false);
					break;
				}
				case SECTOR_SIZE -> {
					sectorSize(state);
					break;
				}
				case GOD_MODE -> {
					godMode(state);
					break;
				}
				case CREATIVE_MODE -> {
					creativeMode(state);
					break;
				}
				case INVISIBILITY_MODE -> {
					invisibilityMode(state);
					break;
				}
				case LIST_CONTROL_UNITS -> {
					listControlUnits(state);
					break;
				}
				case TINT -> {
					tint(state, false);
					break;
				}
				case TINT_NAME -> {
					tint(state, true);
					break;
				}
				case FACTION_DELETE -> {
					deleteFaction(state);
					break;
				}
				case FACTION_DEL_MEMBER -> {
					factionRemoveMember(state);
					break;
				}
				case FACTION_MOD_RELATION -> {
					factionModRelation(state);
					break;
				}
				case FACTION_EDIT -> {
					factionEdit(state);
					break;
				}
				case LOAD_SYSTEM -> {
					loadSystem(state);
					break;
				}
				case IGNORE_DOCKING_AREA -> {
					enableDockingValidation(state);
					break;
				}
				case START_COUNTDOWN -> {
					countdown(state);
					break;
				}
				case LOAD_SECTOR_RANGE -> {
					loadRange(state);
					break;
				}
				case DESTROY_ENTITY -> {
					destroyEntity(state, localObjects, false, false);
					break;
				}
				case DESTROY_ENTITY_DOCK -> {
					destroyEntity(state, localObjects, true, false);
					break;
				}
				case DESTROY_ENTITY_ONLY_DOCK -> {
					destroyEntity(state, localObjects, true, true);
					break;
				}
				case DESTROY_UID -> {
					destroyUID(state, false, false);
					break;
				}
				case DESTROY_UID_DOCKED -> {
					destroyUID(state, true, false);
					break;
				}
				case DESTROY_UID_ONLY_DOCKED -> {
					destroyUID(state, true, true);
					break;
				}
				case SHUTDOWN -> {
					shutdown(state);
					break;
				}
				case DAYTIME -> {
					dayTime(state);
					break;
				}
				case SIMULATION_AI_ENABLE -> {
					enableSimulation(state);
					break;
				}
				case SIMULATION_SPAWN_DELAY -> {
					setSimulationDelay(state);
					break;
				}
				case SIMULATION_INVOKE -> {
					triggerSimulationPlanning(state);
					break;
				}
				case SIMULATION_SEND_RESPONSE_FLEET -> {
					triggerResponseFleet(state);
					break;
				}
				case SEARCH -> {
					search(state);
					break;
				}
				case DESPAWN_ALL -> {
					despawnAll(state);
					break;
				}
				case SOFT_DESPAWN -> {
					softDespawn(state, localObjects, false, false);
					break;
				}
				case SOFT_DESPAWN_DOCK -> {
					softDespawn(state, localObjects, false, false);
					break;
				}
				case DESPAWN_SECTOR -> {
					despawnSector(state);
					break;
				}
				case SHOP_RESTOCK_UID -> {
					restockUid(state, false);
					break;
				}
				case SHOP_RESTOCK_FULL_UID -> {
					restockUid(state, true);
					break;
				}
				case FACTION_SET_ENTITY -> {
					setFactionIdEntity(state, false);
					break;
				}
				case FACTION_SET_ENTITY_UID -> {
					setFactionIdEntity(state, true);
					break;
				}
				case FACTION_SET_ALL_RELATIONS -> {
					setAllRelations(state);
					break;
				}
				case POPULATE_SECTOR -> {
					populateSector(state);
					break;
				}
				case CREATE_TRADE_PARTY -> {
					createTradeParty(state);
					break;
				}
				case SET_DEBUG_MODE -> {
					setDebugMode(state);
					break;
				}
				case CREATE_SPAWNER_TEST -> {
					createCreatureSpawnerTest(state);
					break;
				}
				case REMOVE_SPAWNERS -> {
					removeSpawners(state);
					break;
				}
				case GIVEID -> {
					giveId(state);
					break;
				}
				case SCAN -> {
					scan(state);
					break;
				}
				case FOG_OF_WAR -> {
					fogOfWar(state);
					break;
				}
				case MANAGER_CALCULATIONS -> {
					manCalcs(state);
					break;
				}
				case TP -> {
					tp(state);
					break;
				}
				case TP_TO -> {
					tpTo(state);
					break;
				}
				case CREATURE_RENAME -> {
					creatureRename(state);
					break;
				}
				case CREATURE_SCRIPT -> {
					creatureScript(state);
					break;
				}
				case CREATURE_ANIMATION_START -> {
					creatureAnimation(state, true);
					break;
				}
				case CREATURE_ANIMATION_STOP -> {
					creatureAnimation(state, false);
					break;
				}
				case CREATURE_IDLE -> {
					testPath(state, CreatureCommand.IDLE);
					break;
				}
				case CREATURE_GOTO -> {
					testPath(state, CreatureCommand.GOTO);
					break;
				}
				case CREATURE_SIT -> {
					testPath(state, CreatureCommand.SIT);
					break;
				}
				case CREATURE_STAND_UP -> {
					testPath(state, CreatureCommand.STAND_UP);
					break;
				}
				case CREATURE_ENTER_GRAVITY -> {
					testPath(state, CreatureCommand.GRAVITY);
					break;
				}
				case INITIATE_WAVE -> {
					initiateWave(state, localObjects, 5);
					break;
				}
				case GIVE -> {
					give(state);
					break;
				}
				case GIVE_METAITEM -> {
					giveMetaItem(state);
					break;
				}
				case GIVE_LASER_WEAPON -> {
					giveLaserWeapon(state, false);
					break;
				}
				case GIVE_LASER_WEAPON_OP -> {
					giveLaserWeapon(state, true);
					break;
				}
				case GIVE_HEAL_WEAPON -> {
					giveHealWeapon(state);
					break;
				}
				case PLAYER_PUT_INTO_ENTITY_UID -> {
					putPlayerIntoEntity(state);
					break;
				}
				case GIVE_MARKER_WEAPON -> {
					giveMarkerWeapon(state);
					break;
				}
				case GIVE_TRANSPORTER_MARKER_WEAPON -> {
					giveTransporterMarkerWeapon(state);
					break;
				}
				case GIVE_SNIPER_WEAPON -> {
					giveSniperWeapon(state, false);
					break;
				}
				case GIVE_SNIPER_WEAPON_OP -> {
					giveSniperWeapon(state, true);
					break;
				}
				case GIVE_TORCH_WEAPON -> {
					giveTorchWeapon(state, false);
					break;
				}
				case GIVE_TORCH_WEAPON_OP -> {
					giveTorchWeapon(state, true);
					break;
				}
				case GIVE_GRAPPLE_ITEM -> {
					giveGrappleWeapon(state, false);
					break;
				}
				case GIVE_GRAPPLE_ITEM_OP -> {
					giveGrappleWeapon(state, true);
					break;
				}
				case GIVE_ROCKET_LAUNCHER_OP -> {
					giveRocketLauncherWeapon(state, 2);
					break;
				}
				case GIVE_ROCKET_LAUNCHER_TEST -> {
					giveRocketLauncherWeapon(state, 1);
					break;
				}
				case GIVE_ROCKET_LAUNCHER_WEAPON -> {
					giveRocketLauncherWeapon(state, 0);
					break;
				}
				case GIVE_POWER_SUPPLY_WEAPON -> {
					givePowerSupplyWeapon(state);
					break;
				}
				case SET_SPAWN -> {
					setSpawn(state);
					break;
				}
				case SET_SPAWN_PLAYER -> {
					setSpawnPlayer(state);
					break;
				}
				case FACTION_RESET_ACTIVITY -> {
					factionResetActivity(state);
					break;
				}
				case SHIELD_DAMAGE -> {
					shieldDamage(state);
					break;
				}
				case DECAY -> {
					decay(state);
					break;
				}
				case STRUCTURE_SET_MINABLE -> {
					minable(state);
					break;
				}
				case STRUCTURE_SET_VULNERABLE -> {
					vulnerable(state);
					break;
				}
				case STRUCTURE_SET_MINABLE_UID -> {
					minableUid(state);
					break;
				}
				case STRUCTURE_SET_VULNERABLE_UID -> {
					vulnerableUid(state);
					break;
				}
				case FACTION_POINT_TURN -> {
					factionPointTurn(state);
					break;
				}
				case FACTION_POINT_SET -> {
					factionPointSet(state);
					break;
				}
				case FACTION_POINT_ADD -> {
					factionPointAdd(state);
					break;
				}
				case FACTION_POINT_PROTECT_PLAYER -> {
					factionPointProtectPlayer(state);
					break;
				}
				case PLAYER_SET_SPAWN_TO -> {
					setPlayerSpawnTo(state);
					break;
				}
				case PLAYER_GET_SPAWN -> {
					getPlayerSpawn(state);
					break;
				}
				case FACTION_POINT_GET -> {
					factionPointGet(state);
					break;
				}
				case GIVE_CREDITS -> {
					giveCredits(state);
					break;
				}
				case SET_INFINITE_INVENTORY_VOLUME -> {
					giveInfiniteVolume(state);
					break;
				}
				case GATE_DEST -> {
					setJumpGateDest(state);
					break;
				}
				case START_SHIP_AI -> {
					startShipAI(state);
					break;
				}
				case STOP_SHIP_AI -> {
					stopShipAI(state);
					break;
				}
				case CREATURE_ROAM -> {
					testPath(state, CreatureCommand.ROAM);
					break;
				}
				case GIVE_ALL_ITEMS -> {
					addAllItems(state);
					break;
				}
				case SHOP_RESTOCK -> {
					restock(state, false);
					break;
				}
				case SHOP_INFINITE -> {
					shopSetInfinite(state);
					break;
				}
				case SHOP_RESTOCK_FULL -> {
					restock(state, true);
					break;
				}
//				case EXPLODE_PLANET_SECTOR -> {
//					explodePlanetSector(state, true);
//					break;
//				}
				case PLAYER_SUSPEND_FACTION -> {
					suspendFaction(state);
					break;
				}
				case PLAYER_UNSUSPEND_FACTION -> {
					unsuspendFaction(state);
					break;
				}
				case EXPLODE_PLANET_SECTOR_NOT_CORE -> {
					explodePlanetSector(state, false);
					break;
				}
				case UPDATE_SHOP_PRICES -> {
					updateAllShopPrices(state);
					break;
				}
				case GIVE_CATEGORY_ITEMS -> {
					addAllItemsCategory(state);
					break;
				}
				case NPC_TURN_ALL -> {
					npcTurn(state, 0);
					break;
				}
				case FLEET_DEBUG_MOVE -> {
					sendFleetDebug(state);
					break;
				}
				case FLEET_DEBUG_STOP -> {
					stopFleetDebug(state);
					break;
				}
				case FLEET_SPEED -> {
					fleetSpeed(state);
					break;
				}
				case NPC_DEBUG_MODE -> {
					npcDebugMode(state);
					break;
				}
				case NPC_FLEET_LOADED_SPEED -> {
					npcFleetLoadedSpeed(state);
					break;
				}
				case NPC_KILL_RANDOM_IN_SYSTEM -> {
					killRandomNPC(state);
					break;
				}
				case NPC_BRING_DOWN_SYSTEM_STATUS -> {
					killUntilStatusNPC(state);
					break;
				}
				case NPC_SPAWN_FACTION -> {
					npcSpawnFaction(state, false);
					break;
				}
				case NPC_ADD_SHOP_OWNER -> {
					npcModShopOwner(state, true);
					break;
				}
				case NPC_REMOVE_SHOP_OWNER -> {
					npcModShopOwner(state, false);
					break;
				}
				case NPC_SPAWN_FACTION_POS_FIXED -> {
					npcSpawnFaction(state, true);
					break;
				}
				case NPC_REMOVE_FACTION -> {
					npcRemoveFaction(state);
					break;
				}
				case LIST_ADMINS -> {
					listAdmins(state);
					break;
				}
				case LIST_BANNED_IP -> {
					listBannedIps(state);
					break;
				}
				case LIST_BANNED_NAME -> {
					listBannedNames(state);
					break;
				}
				case LIST_BANNED_ACCOUNTS -> {
					listBannedAccounts(state);
					break;
				}
				case LIST_WHITELIST_ACCOUNTS -> {
					listWhiteAccounts(state);
					break;
				}
				case LIST_WHITELIST_IP -> {
					listWhiteIps(state);
					break;
				}
				case KICK_PLAYERS_OUT_OF_ENTITY -> {
					forcePlayerExit(state);
					break;
				}
				case SHIELD_OUTAGE -> {
					shieldOutage(state);
					break;
				}
				//				case ENTITY_SET_ARMOR_HP_PERCENT: {
				//					setArmorHP(state);
				//					break;
				//				}
				case ENTITY_SET_STRUCTURE_HP_PERCENT -> {
					setStructureHP(state);
					break;
				}
				case ENTITY_REBOOT -> {
					resetStructureHP(state);
					break;
				}
				case ENTITY_TRACK -> {
					trackSelected(state);
					break;
				}
				case ENTITY_TRACK_UID -> {
					trackSelectedUID(state);
					break;
				}
				case ENTITY_SET_CHECK_FLAG -> {
					flagSelected(state);
					break;
				}
				case ENTITY_SET_CHECK_FLAG_UID -> {
					flagSelectedUID(state);
					break;
				}
				case ENTITY_IS_CHECK_FLAG -> {
					isFlagSelected(state);
					break;
				}
				case ENTITY_IS_CHECK_FLAG_UID -> {
					isFlagSelectedUID(state);
					break;
				}
				//				case ENTITY_REPAIR_ARMOR: {
				//					resetArmorHP(state);
				//					break;
				//				}
				case TEST_STATISTICS_SCRIPT -> {
					testStatisticsScript(state);
					break;
				}
				case POWER_OUTAGE -> {
					powerOutage(state);
					break;
				}
				case LIST_WHITELIST_NAME -> {
					listWhiteNames(state);
					break;
				}
				case ENTITY_INFO -> {
					getEntityInfo(state);
					break;
				}
				case SAVE -> {
					saveShip(state);
					break;
				}
				case SAVE_AS -> {
					saveShipAs(state);
					break;
				}
				case SAVE_UID -> {
					saveShipUid(state);
					break;
				}
				case SERVER_MESSAGE_BROADCAST -> {
					serverMessageBroadcast(state);
					break;
				}
				case SERVER_MESSAGE_TO -> {
					serverMessageTo(state);
					break;
				}
				case LIST_BLUEPRINTS -> {
					listBlueprints(state, false, false);
					break;
				}
				case LIST_BLUEPRINTS_VERBOSE -> {
					listBlueprints(state, false, true);
					break;
				}
				case LIST_BLUEPRINTS_BY_OWNER -> {
					listBlueprints(state, true, false);
					break;
				}
				case LIST_BLUEPRINTS_BY_OWNER_VERBOSE -> {
					listBlueprints(state, true, true);
					break;
				}
				case BREAK_SHIP -> {
					testBreaking(state);
					break;
				}
				case SPAWN_MOBS_LINE -> {
					spawnMobsLine(state);
					break;
				}
				case SPAWN_ITEM -> {
					spawnItem(state);
					break;
				}
				case SPAWN_MOBS -> {
					spawnMobs(state);
					break;
				}
				case SECTOR_CHMOD -> {
					chmodSector(state);
					break;
				}
				case TERRITORY_MAKE_UNCLAIMABLE -> {
					territoryUnclaimable(state);
					break;
				}
				case TERRITORY_RESET -> {
					territoryReset(state);
					break;
				}
				case DEBUG_FSM_INFO -> {
					debugFSMInfo();
					break;
				}
				case LOAD -> {
					loadShip(state, BluePrintController.active, false, false);
					break;
				}
				case LOAD_DOCKED -> {
					loadShip(state, BluePrintController.active, true, false);
					break;
				}
				case LOAD_AS_FACTION -> {
					loadShip(state, BluePrintController.active, false, true);
					break;
				}
				case LOAD_AS_FACTION_DOCKED -> {
					loadShip(state, BluePrintController.active, true, true);
					break;
				}
				case LOAD_STATION_NEUTRAL -> {
					loadShip(state, BluePrintController.stationsNeutral, false, false);
					break;
				}
				case LOAD_STATION_PIRATE -> {
					loadShip(state, BluePrintController.stationsPirate, false, false);
					break;
				}
				case LOAD_STATION_TRADING_GUILD -> {
					loadShip(state, BluePrintController.stationsPirate, false, false);
					break;
				}
				case JUMP -> {
					jump(state);
					break;
				}
				case FLEET_INFO -> fleetInfo(state);
				case SIMULATION_INFO -> simulationInfo(state);
				case SIMULATION_CLEAR_ALL -> simulationClearAll(state);
				case FORCE_SAVE -> {
					save(state);
					break;
				}
				case EXPORT_SECTOR -> {
					exportSectorNorm(state);
					break;
				}
				case DELAY_SAVE -> {
					delayAutosave(state);
					break;
				}
				case IMPORT_SECTOR -> {
					importSectorNorm(state);
					break;
				}
				case EXPORT_SECTOR_BULK -> {
					exportSectorBulk(state);
					break;
				}
				case IMPORT_SECTOR_BULK -> {
					importSectorBulk(state);
					break;
				}
				case ADD_ADMIN -> {
					addAdmin(state);
					break;
				}
				case LIST_ADMIN_DENIED_COMMANDS -> {
					listDeniedCommands(state);
					break;
				}
				case ADD_ADMIN_DENIED_COMAND -> {
					addOrRemoveAdminDeniedCommand(state, true);
					break;
				}
				case REMOVE_ADMIN_DENIED_COMAND -> {
					addOrRemoveAdminDeniedCommand(state, false);
					break;
				}
				case REMOVE_ADMIN -> {
					removeAdmin(state);
					break;
				}
				case STATUS -> {
					status(state);
					break;
				}
				case SQL_INSERT_RETURN_GENERATED_KEYS -> {
					sql(state, true, true);
					break;
				}
				case SQL_QUERY -> {
					sql(state, false, false);
					break;
				}
				case SQL_UPDATE -> {
					sql(state, true, false);
					break;
				}
				case GIVE_LOOK -> {
					giveLook(state);
					break;
				}
				case AI_WEAPON_SWITCH_DELAY -> {
					setAIWeaponSwitchDelay(state);
					break;
				}
				case SHIP_INFO_UID -> {
					shipInfoUid(state);
					break;
				}
				case SHIP_INFO_NAME -> {
					shipInfoName(state);
					break;
				}
				case SHIP_INFO_SELECTED -> {
					shipInfoSelected(state);
					break;
				}
				case SECTOR_INFO -> {
					sectorInfo(state);
					break;
				}
				case GIVE_SLOT -> {
					giveSlot(state);
					break;
				}
				case BAN -> {
					banName(state);
					break;
				}
				case WHITELIST_ACTIVATE -> {
					activateWhiteList(state);
					break;
				}
				case BAN_IP -> {
					banIp(state, false, false);
					break;
				}
				case FACTION_SET_ENTITY_RANK -> {
					setEntityPermission(state);
					break;
				}
				case FACTION_SET_ENTITY_RANK_UID -> {
					setEntityPermissionUID(state);
					break;
				}
				case PLAYER_GET_INVENTORY -> {
					getPlayerInventory(state);
					break;
				}
				case PLAYER_GET_BLOCK_AMOUNT -> {
					getPlayerBlockAmount(state);
					break;
				}
				case BAN_IP_BY_PLAYERNAME -> {
					banIp(state, true, false);
					break;
				}
				case BAN_ACCOUNT -> {
					banAccount(state, false, false);
					break;
				}
				case BAN_ACCOUNT_BY_PLAYERNAME -> {
					banAccount(state, true, false);
					break;
				}
				case BAN_IP_TEMP -> {
					banIp(state, false, true);
					break;
				}
				case BAN_IP_BY_PLAYERNAME_TEMP -> {
					banIp(state, true, true);
					break;
				}
				case BAN_ACCOUNT_TEMP -> {
					banAccount(state, false, true);
					break;
				}
				case BAN_ACCOUNT_BY_PLAYERNAME_TEMP -> {
					banAccount(state, true, true);
					break;
				}
				case CLEAR_MINES_HERE -> {
					clearMinesHere(state);
					break;
				}
				case CLEAR_MINES_SECTOR -> {
					clearMinesSector(state, true, true);
					break;
				}
				case UNBAN_ACCOUNT -> {
					unBanAccount(state);
					break;
				}
				case WHITELIST_ACCOUNT -> {
					whitelistAccount(state, false);
					break;
				}
				case WHITELIST_NAME -> {
					whitelistName(state, false);
					break;
				}
				case WHITELIST_IP -> {
					whitelistIp(state, false);
					break;
				}
				case SIM_FACTION_SPAWN_TEST -> {
					break;
				}
				case WHITELIST_ACCOUNT_TEMP -> {
					whitelistAccount(state, true);
					break;
				}
				case WHITELIST_NAME_TEMP -> {
					whitelistName(state, true);
					break;
				}
				case WHITELIST_IP_TEMP -> {
					whitelistIp(state, true);
					break;
				}
				case FACTION_SET_ID_MEMBER -> {
					setFactionId(state);
					break;
				}
				case PLAYER_INFO -> {
					playerInfo(state);
					break;
				}
				case PLAYER_LIST -> {
					playerList(state);
					break;
				}
				case PLAYER_PROTECT -> {
					playerProtect(state);
					break;
				}
				case PLAYER_UNPROTECT -> {
					playerUnprotect(state);
					break;
				}
				case REFRESH_SERVER_MSG -> {
					refreshServerMessage(state);
					break;
				}
				case SPAWN_CREATURE -> {
					spawnCreature(state);
					break;
				}
				case SPAWN_CREATURE_MASS -> {
					spawnMassCreature(state);
					break;
				}
				case UNBAN_NAME -> {
					unBanName(state);
					break;
				}
				case CLEAR_OVERHEATING -> {
					clearOverheating(state, false);
					break;
				}
				case CLEAR_OVERHEATING_SECTOR -> {
					clearOverheatingSector(state);
					break;
				}
				case CLEAR_SYSTEM_SHIP_SPAWNS -> {
					clearSystemShipSpawns(state, false);
					break;
				}
				case CLEAR_SYSTEM_SHIP_SPAWNS_ALL -> {
					clearSystemShipSpawns(state, true);
					break;
				}
				case CLEAR_OVERHEATING_ALL -> {
					clearOverheating(state, true);
					break;
				}
				case UNBAN_IP -> {
					unBanIp(state);
					break;
				}
				case KICK -> {
					kick(state, false, 0);
					break;
				}
				case KICK_REASON -> {
					kick(state, true, 1);
					break;
				}
				// case END_ROUND :
				// {
				// endRound(state);
				//
				// break;
				// }
				case RESTRUCT_AABB -> {
					resAABB(state);
					break;
				}
				case EXECUTE_ENTITY_EFFECT -> {
					executeGraphicsEffect(state);
					break;
				}
				case KILL_CHARACTER -> {
					killCharacter(state);
					break;
				}
				case TELEPORT_SELF_HOME -> {
					teleportSelfHome(state);
					break;
				}
				case TELEPORT_SELF_TO -> {
					teleportSelfTo(state);
					break;
				}
				case TELEPORT_UID_TO -> {
					teleportUidTo(state, localObjects);
					break;
				}
				case TELEPORT_TO -> {
					teleportTo(state, localObjects);
					break;
				}
				case TELEPORT_SELECTED_TO -> {
					teleportSelectedTo(state, localObjects);
					break;
				}
				case CHANGE_SECTOR -> {
					changeSector(state, false);
					break;
				}
				case CHANGE_SECTOR_COPY -> {
					changeSector(state, true);
					break;
				}
				case CHANGE_SECTOR_FOR -> {
					changeSectorPlayer(state, false);
					break;
				}
				case CHANGE_SECTOR_FOR_UID -> {
					changeSectorUid(state, false);
					break;
				}
				case CHANGE_SECTOR_SELECTED -> {
					changeSectorSelected(state, false);
					break;
				}
				case CHANGE_SECTOR_FOR_COPY -> {
					changeSectorPlayer(state, true);
					break;
				}
				case SET_GLOBAL_SPAWN -> {
					setGlobalSpawn(state);
					break;
				}
				case SPAWN_ENTITY -> {
					spawnEntity(state);
					break;
				}
				case SPAWN_ENTITY_POS -> {
					spawnEntityPos(state);
					break;
				}
				case REPAIR_SECTOR -> {
					repairSector(state);
					break;
				}
				case FACTION_CHECK -> {
					checkFactions(state);
					break;
				}
				case FACTION_REINSTITUTE -> {
					factionsReinstitude(state);
					break;
				}
				case FACTION_MOD_MEMBER -> {
					factionModMember(state);
					break;
				}
				case MISSILE_DEFENSE_FRIENDLY_FIRE -> {
					handleServerConfigBoolean(state, ServerConfig.MISSILE_DEFENSE_FRIENDLY_FIRE);
					break;
				}
				case FACTION_JOIN_ID -> {
					joinFaction(state);
					break;
				}
				case FACTION_LIST_MEMBERS -> {
					checkFactionMembers(state);
					break;
				}
				case LAST_CHANGED -> {
					showModifierAndSpawner(state);
					break;
				}
				case SHIELD_REGEN -> {
					entitySetShieldRegen(state);
					break;
				}
				case POWER_REGEN -> {
					entitySetPowerRegen(state);
					break;
				}
				case POWER_DRAIN -> {
					powerDrain(state);
					break;
				}
				case SPAWN_PARTICLE -> {
					spawnParticle(state);
					break;
				}
				case BLUEPRINT_DELETE -> {
					blueprintDelete(state);
					break;
				}
				case BLUEPRINT_INFO -> {
					blueprintInfo(state);
					break;
				}
				case BLUEPRINT_SET_OWNER -> {
					blueprintSetOwner(state);
					break;
				}
				case ENTITY_INFO_BY_PLAYER_UID -> {
					entityInfoByPlayerUID(state);
					break;
				}
				case ENTITY_INFO_NAME -> {
					shipInfoName(state);
					break;
				}
				case ENTITY_INFO_UID -> {
					shipInfoUid(state);
					break;
				}
				case KICK_PLAYERS_OUT_OF_ENTITY_UID -> {
					kickPlayersOutOfEntityByUID(state, false);
					break;
				}
				case KICK_PLAYERS_OUT_OF_ENTITY_UID_DOCK -> {
					kickPlayersOutOfEntityByUID(state, true);
					break;
				}
				case KICK_PLAYER_NAME_OUT_OF_ENTITY -> {
					kickPlayerUIDOutOfEntity(state);
					break;
				}
				case RAIL_RESET_ALL -> {
					resetDock(state, true);
					break;
				}
				case RAIL_RESET -> {
					resetDock(state, false);
					break;
				}
				case SET_WEAPON_RANGE_REFERENCE -> {
					setWeaponRangeReference(state);
					break;
				}
				case RESET_REPRAIR_DELAY -> {
					resetRepairTimeout(state);
					break;
				}
				case RESET_INTEGRITY_DELAY -> {
					resetIntegrityTimeout(state);
					break;
				}
				case MISSILE_TARGET_PREDICTION -> {
					setMissileTargetPrediction(state);
					break;
				}
				case GIVE_UID_STORAGE_ID -> {
					putIntoInventory(state);
					break;
				}
				case ENTITY_GET_INVENTORY -> {
					readFromInventory(state);
					break;
				}
				default -> throw new AdminCommandNotFoundException(command.name());
			}
		} catch (IndexOutOfBoundsException e) {
			String needed = "Needed: ";
			if (command.getRequiredParameterCount() != command.getTotalParameterCount()) {
				needed += "Minimum of " + command.getRequiredParameterCount();
			} else {
				needed += command.getTotalParameterCount();
			}
			client.serverMessage("[ADMIN COMMAND] [ERROR] Wrong amount of arguments. " + needed);
		} catch (AdminCommandNotFoundException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] Command known, but not executable: "
					+ e.getMessage() + ". Please send in report!");
		}
		client.executedAdminCommand();
	}


	private void setMissileTargetPrediction(GameServerState state) throws IOException {
		float delay = ((Float) commandParams[0]);

		ServerConfig.MISSILE_TARGET_PREDICTION_SEC.setFloat(delay);
		ServerConfig.write();
		
		client.serverMessage("[ADMIN COMMAND] [SUCCESS] Missile target prediction set to "+ServerConfig.MISSILE_TARGET_PREDICTION_SEC.getFloat()+" ticks ("+(int)(ServerConfig.MISSILE_TARGET_PREDICTION_SEC.getFloat() * TargetChasingMissile.UPDATE_MS)+"ms)");
	}

	private void setAIWeaponSwitchDelay(GameServerState state) throws IOException {
		int delay = ((Integer) commandParams[0]);

		state.getGameState().setAIWeaponSwitchDelayMS(delay);
		ServerConfig.AI_WEAPON_SWITCH_DELAY.setInt(delay);
		ServerConfig.write();

		client.serverMessage("[ADMIN COMMAND] [SUCCESS] AI Weapon switch delay set to " + delay + "ms");
	}

	private void putPlayerIntoEntity(GameServerState state) throws IOException {
		String name = ((String) commandParams[0]);
		String UID = ((String) commandParams[1]);

		Sector to = loadUID(state, UID);

		if(to == null) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] Either UID doesn't exist or UID sector is not spawned anywhere in the universe");
		}
		try {
			PlayerState p = state.getPlayerFromName(name);
			try {
				SimpleTransformableSendableObject cm = p.getFirstControlledTransformable();
				if(cm instanceof SegmentController) {
					kickPlayersOut(state, (SegmentController) cm, false, p);
				}
			} catch(PlayerControlledTransformableNotFound e) {
			}


			p.forcePlayerIntoEntity(to, UID);

		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] player " + name + " not online");
			return;
		}


	}

	private Sector loadUID(GameServerState state, String UID) {
		Sendable sendable = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(UID);
		if(sendable instanceof SimpleTransformableSendableObject<?>) {
			return state.getUniverse().getSector(((SimpleTransformableSendableObject<?>) sendable).getSectorId());
		}
		DatabaseEntry byUIDExact = state.getDatabaseIndex().getTableManager().getEntityTable().getEntryForFullUID(UID);
		if(byUIDExact != null) {
			try {
				return state.getUniverse().getSector(byUIDExact.sectorPos);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private void getPlayerBlockAmount(GameServerState state) throws IOException {
		String name = ((String) commandParams[0]);
		int type = ((Integer) commandParams[1]);
		PlayerState playerFromName;
		try {
			playerFromName = state.getPlayerFromName(name);


		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] player " + name + " not online");
			return;
		}
		Inventory personalInv = playerFromName.getInventory();
		if(ElementKeyMap.isValidType(type)) {
			int quant = personalInv.getOverallQuantity((short) type);

			client.serverMessage("[ADMIN COMMAND] [SUCCESS] " + name + ": " + quant + " of " + ElementKeyMap.toString(type));
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] invalid block type");
		}
	}

	private void getPlayerInventory(GameServerState state) throws IOException {
		String name = ((String) commandParams[0]);
		PlayerState playerFromName;
		try {
			playerFromName = state.getPlayerFromName(name);


		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] player " + name + " not online");
			return;
		}
		Inventory personalInv = playerFromName.getInventory();

		client.serverMessage("[ADMIN COMMAND] [SUCCESS] Listing player " + name + " personal inventory START");
		for(int e : personalInv.getSlots()) {
			InventorySlot slot = personalInv.getSlot(e);
			if(slot != null) {
				printSlot(name, 0, slot);
			}
		}
		client.serverMessage("[ADMIN COMMAND] [SUCCESS] Listing player " + name + " personal inventory END.");
	}

	private void printSlot(String name, int lvl, InventorySlot slot) throws IOException {
		if(slot != null) {
			StringBuffer b = new StringBuffer();
			for(int i = 0; i < lvl; i++) {
				b.append("-");
			}
			client.serverMessage("[INVENTORY] " + name + ": " + b + " SLOT: " + slot.slot + "; MULTI: " + slot.isMultiSlot() + "; TYPE: " + slot.getType() + "; META: " + slot.metaId + "; COUNT: " + slot.count());
			if(slot.isMultiSlot()) {
				for(InventorySlot sub : slot.getSubSlots()) {
					printSlot(name, lvl + 1, sub);
				}
			}
		}
	}

	private void setWeaponRangeReference(GameServerState state) throws IOException {
		float val = ((Float) commandParams[0]);
		state.getGameState().setWeaponRangeReference(val);
		client.serverMessage("[ADMIN COMMAND] [SUCCESS] Weapon Reference Range set to " + (int) val + " meters");
	}

	private void kickPlayerUIDOutOfEntity(GameServerState state) throws IOException {
		String name = ((String) commandParams[0]);
		PlayerState playerFromName;
		try {
			playerFromName = state.getPlayerFromName(name);


		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] player " + name + " not online");
			return;
		}

		try {
			SimpleTransformableSendableObject cm = playerFromName.getFirstControlledTransformable();
			if(cm instanceof SegmentController) {
				kickPlayersOut(state, (SegmentController) cm, false, playerFromName);
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] Player " + name + " is currently not controlling any entity");
			}
		} catch(PlayerControlledTransformableNotFound e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] Player " + name + " is currently not controlling any entity");
		}
	}

	private void kickPlayersOutOfEntityByUID(GameServerState state, boolean dock) throws IOException {
		String name = ((String) commandParams[0]);
		Sendable sendable = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(name);
		if(sendable instanceof SegmentController) {
			kickPlayersOut(state, (SegmentController) sendable, dock, null);

			if(dock) {
				client.serverMessage("[ADMIN COMMAND] [SUCCESS] Kicking players out of entity " + name);
			} else {
				client.serverMessage("[ADMIN COMMAND] [SUCCESS] Kicking players out of entity and all related docks and mothership of " + name);
			}
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] entity " + name + " not found in currently loaded objects");
		}
	}

	public void kickPlayersOut(GameServerState s, SegmentController c, boolean dock, PlayerState playerFilter) {
		if(playerFilter == null) {
			c.kickAllPlayersOutServer();
		} else {
			c.kickPlayerOutServer(playerFilter);
		}

		if(dock) {
			for(RailRelation r : c.railController.next) {
				kickPlayersOut(s, r.docked.getSegmentController(), dock, playerFilter);
			}
		}
	}

	private void entityInfoByPlayerUID(GameServerState state) throws IOException {
		String name = ((String) commandParams[0]);
		PlayerState playerFromName;
		try {

			try {
				playerFromName = state.getPlayerFromName(name);


			} catch(PlayerNotFountException e) {
				client.serverMessage("[ADMIN COMMAND] [ERROR] player " + name + " not online");
				return;
			}

			SimpleTransformableSendableObject firstControlledTransformable = playerFromName.getFirstControlledTransformable();

			if(firstControlledTransformable != null) {
				printInfo(state, firstControlledTransformable);
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] Player is not controlling any entity");
			}
		} catch(PlayerControlledTransformableNotFound e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}
	}

	private void clearMinesSector(GameServerState state, boolean b, boolean c) throws IOException {
		int x = ((Integer) commandParams[0]);
		int y = ((Integer) commandParams[1]);
		int z = ((Integer) commandParams[2]);

		clearMines(state, x, y, z);
	}

	private void clearMines(GameServerState state, int x, int y, int z) throws IOException {

		state.getController().getMineController().clearMinesInSectorServer(x, y, z);
		client.serverMessage("Mines cleared in " + x + ", " + y + ", " + z + "!");
	}

	private void clearMinesHere(GameServerState state) throws IOException {
		try {
			PlayerState playerFromName = state.getPlayerFromName(client.getClientName());
			clearMines(state, playerFromName.getCurrentSector().x, playerFromName.getCurrentSector().y, playerFromName.getCurrentSector().z);
		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("Player not found!");
		}

	}

	private void fleetInfo(GameServerState state) throws IOException {
		client.serverMessage("[ADMIN COMMAND] [FLEETINFO] Cached Fleets: " + state.getFleetManager().fleetCache.size());
	}

	private void triggerResponseFleet(GameServerState state) throws IOException {
		try {
			PlayerState player = state.getPlayerFromStateId(client.getId());

			state.getSimulationManager().sendToAttackSpecific(player.getFirstControlledTransformable(), FactionManager.TRAIDING_GUILD_ID, 1);
			client.serverMessage("[ADMIN COMMAND] [SUCCESS] SENT RESPONSE FLEET TO YOUR POSITION");
		} catch(PlayerControlledTransformableNotFound e) {
			e.printStackTrace();
		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] Player not found (client has no player attached)");
		}
	}

	private void sql(final GameServerState state, boolean update, boolean returnKeys) throws IOException {
		String[] nmArray = ServerConfig.SQL_PERMISSION.getString().split(",");
		ObjectOpenHashSet<String> names = new ObjectOpenHashSet<String>();
		for(String n : nmArray) {
			names.add(n.toLowerCase(Locale.ENGLISH).trim());
		}
		boolean perm = names.contains(client.getClientName().toLowerCase(Locale.ENGLISH)) || client instanceof AdminLocalClient || client instanceof AdminRemoteClient;
		if(!perm){
			client.serverMessage("ERROR: YOU DO NOT HAVE PERMISSIONS TO DO SQL QUERIES!");
			return;
		}
		String query = ((String) commandParams[0]);
		final StringBuffer sb = new StringBuffer();
		final int id;
		synchronized(state) {
			state.getDatabaseIndex().adminSql(query, update, returnKeys, state, client, sb);
			id = this.sqlT++;
		}
		client.blockFromLogout();
		System.err.println("NOW BLOCKED :::: ");
		(new Thread() {
			@Override
			public void run() {
				try {
					System.err.println("NOW BLOCKED :::: THREAD START");
					BufferedReader r = new BufferedReader(new StringReader(sb.toString()));
					String line;
					synchronized(state) {
						client.serverMessage("---------- SQL QUERY " + id + " BEGIN ----------");
					}
					while((line = r.readLine()) != null) {
						synchronized(state) {
							client.serverMessage("SQL#" + id + ": " + line);
						}
					}
					synchronized(state) {
						client.serverMessage("---------- SQL QUERY " + id + " END ----------");
					}
					r.close();
					System.err.println("NOW BLOCKED :::: THREAD END");
				} catch(Exception r) {
					r.printStackTrace();
				}finally{
					client.disconnect();
				}
			}

		}).start();


	}

	private void npcModShopOwner(GameServerState state, boolean add) throws IOException {
		String name = ((String) commandParams[0]);
		
		String nmStr = ServerConfig.NPC_DEBUG_SHOP_OWNERS.getString().trim();
		Set<String> names = new ObjectOpenHashSet<String>();
		if(nmStr.length() > 0) {
			String[] split = nmStr.split(",");
			for(int i = 0; i < split.length; i++) {
				names.add(split[i].trim().toLowerCase(Locale.ENGLISH));
			}
		}
		if(add) {
			names.add(name.toLowerCase(Locale.ENGLISH).trim());
		} else {
			names.remove(name.toLowerCase(Locale.ENGLISH).trim());
		}
		Iterator<String> iterator = names.iterator();
		StringBuffer b = new StringBuffer();

		while(iterator.hasNext()) {
			String next = iterator.next();
			b.append(next);
			if(iterator.hasNext()) {
				b.append(", ");
			}
		}
		ServerConfig.write();
		client.serverMessage("[ADMIN COMMAND] [SUCCESS] Debug Shop Owners Now: " + names);
	}

	private void manCalcs(GameServerState state) throws IOException {
		boolean on = ((Boolean) commandParams[0]);		
		ServerConfig.MANAGER_CALC_CANCEL_ON.setOn(on);
		ServerConfig.write();
		client.serverMessage("[ADMIN COMMAND] [SUCCESS] Manager Calculations set to: "+ServerConfig.MANAGER_CALC_CANCEL_ON.isOn()+" (reload sector to take effect)");
		
	}

	private void fogOfWar(GameServerState state) throws IOException {
		boolean on = ((Boolean) commandParams[0]);		
		ServerConfig.USE_FOW.setOn(on);
		ServerConfig.write();
		client.serverMessage("[ADMIN COMMAND] [SUCCESS] Fog of war set to: "+ServerConfig.USE_FOW.isOn());
		
	}

	private void scan(GameServerState state) throws IOException {
		int x = ((Integer) commandParams[0]);
		int y = ((Integer) commandParams[1]);
		int z = ((Integer) commandParams[2]);

		try {
			PlayerState playerFromName = state.getPlayerFromStateId(client.getId());
			Vector3i sys = new Vector3i(x, y, z);
			playerFromName.getFogOfWar().scan(sys);
			client.serverMessage("[ADMIN COMMAND] [SUCCESS] Scanned System: " + sys + " for " + playerFromName.getName());
		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] Player not found (client has no player attached)");
		}

	}

	private void npcDebugMode(GameServerState state) throws IOException {
		boolean on = ((Boolean) commandParams[0]);		
		ServerConfig.NPC_DEBUG_MODE.setOn(on);
		ServerConfig.write();
		client.serverMessage("[ADMIN COMMAND] [SUCCESS] NPC Debug Mode set to: "+ServerConfig.NPC_DEBUG_MODE.isOn());
	}

	private void npcFleetLoadedSpeed(GameServerState state) throws IOException {
		float on = ((Float) commandParams[0]);		
		ServerConfig.NPC_LOADED_SHIP_MAX_SPEED_MULT.setFloat(on);
		ServerConfig.write();
		client.serverMessage("[ADMIN COMMAND] [SUCCESS] NPC Loaded Ship Speed set to: "+ServerConfig.NPC_LOADED_SHIP_MAX_SPEED_MULT.getFloat());
	}

	private void fleetSpeed(GameServerState state) throws IOException {
		int on = ((Integer) commandParams[0]);		
		ServerConfig.FLEET_OUT_OF_SECTOR_MOVEMENT.setInt(on);
		ServerConfig.write();
		client.serverMessage("[ADMIN COMMAND] [SUCCESS] Fleet speed set to: "+ServerConfig.FLEET_OUT_OF_SECTOR_MOVEMENT.getInt()+" milliseconds / sector");
	}

	private void npcRemoveFaction(GameServerState state) throws IOException {
		int fid = ((Integer) commandParams[0]);

		Faction df = state.getFactionManager().getFaction(fid);

		if(df != null && df.isNPC()) {
			NPCFaction f = (NPCFaction) df;
			f.removeCompletely();
			try {
				state.getGameState().getFactionManager().removeFaction(f.getIdFaction());
			} catch(FactionNotFoundException e) {
				e.printStackTrace();
			}
			client.serverMessage("[ADMIN COMMAND] [SUCCESS] faction removed!");
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] faction not found!");
		}

	}

	private void npcSpawnFaction(GameServerState state, boolean fixedPosition) throws IOException {
		String name = ((String) commandParams[0]);
		String desc = ((String) commandParams[1]);
		String preset = ((String) commandParams[2]);
		int initialGrowth = ((Integer) commandParams[3]);
		Vector3i pos = null;

		if(fixedPosition) {
			int x = ((Integer) commandParams[4]);
			int y = ((Integer) commandParams[5]);
			int z = ((Integer) commandParams[6]);
			pos = new Vector3i(x, y, z);
		}
		NPCFactionSpawn spawn = NPCStartupConfig.createSpawn(name, desc, !fixedPosition, initialGrowth, pos, preset);
		Galaxy galaxy;
		if(fixedPosition) {
			galaxy = state.getUniverse().getGalaxyFromSystemPos(pos);
		} else {
			galaxy = state.getUniverse().getGalaxyFromSystemPos(new Vector3i(0, 0, 0));
		}
		try {
			galaxy.getNpcFactionManager().add(galaxy, spawn);
			client.serverMessage("[ADMIN COMMAND] [SUCCESS] Spawned new faction!");
		} catch(NPCSpawnException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] Exception: " + e.getMessage() + "! Check logs for exact error!");
		}
	}

	private void killUntilStatusNPC(GameServerState state) throws IOException {
		PlayerState playerFromName;
		try {
			float until = ((Float) commandParams[0]) / 100f;
			playerFromName = state.getPlayerFromStateId(client.getId());

			Vector3i currentSystem = playerFromName.getCurrentSystem();

			StellarSystem sys = state.getUniverse().getStellarSystemFromStellarPos(currentSystem);

			Faction f = state.getFactionManager().getFaction(sys.getOwnerFaction());

			assert (sys.getPos().equals(currentSystem));

			if(f != null && f instanceof NPCFaction) {

				NPCSystem system = ((NPCFaction) f).structure.getSystem(currentSystem);
				assert (system.system.equals(currentSystem));
				int i = 0;
				while(until < system.status) {
					NPCEntity killRandomSpawned = system.getContingent().killRandomSpawned();
					if(killRandomSpawned == null) {
						system.getContingent().killRandomNonSpanwed();
					}
					i++;
				}
				state.getGameState().getNetworkObject().npcSystemBuffer.add(new RemoteNPCSystem(system, true));
				client.serverMessage("[ADMIN COMMAND] [SUCCESS] killed: " + i + " in " + system.system + "; New Status: " + system.status * 100d + "% ");
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] System not owned by NPC faction");
			}


		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}
	}

	private void killRandomNPC(GameServerState state) throws IOException {
		PlayerState playerFromName;
		try {

			playerFromName = state.getPlayerFromStateId(client.getId());

			Vector3i currentSystem = playerFromName.getCurrentSystem();

			StellarSystem sys = state.getUniverse().getStellarSystemFromStellarPos(currentSystem);

			Faction f = state.getFactionManager().getFaction(sys.getOwnerFaction());

			if(f != null && f instanceof NPCFaction) {
				NPCSystem system = ((NPCFaction) f).structure.getSystem(currentSystem);
				NPCEntity killRandomSpawned = system.getContingent().killRandomSpawned();
				client.serverMessage("[ADMIN COMMAND] [SUCCESS] killed: " + killRandomSpawned);
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] System not owned by NPC faction");
			}


		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}
	}

	private void npcTurn(GameServerState state, int factionId) throws IOException {
		if(factionId == 0) {
			for(Faction f : state.getFactionManager().getFactionMap().values()) {
				if(f instanceof NPCFaction) {
					npcTurn(state, f.getIdFaction());
				}
			}
		} else {
			Faction faction = state.getFactionManager().getFaction(factionId);

			if(faction != null && faction instanceof NPCFaction) {
				NPCFaction npcFac = (NPCFaction) faction;
				state.getFactionManager().scheduleTurn(npcFac);
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] faction unknown or not an NPC " + factionId + " (" + faction + ")");
			}
		}
	}

//	private void setArmorHP(GameServerState state) throws IOException {
//		SimpleTransformableSendableObject selectedObject = getSelectedObject(state);
//		if (selectedObject != null && selectedObject instanceof SegmentController) {
//			SegmentController c = (SegmentController) selectedObject;
//
//			c.getHpController().setArmorHpPercent(((Float) commandParams[0]));
//			client.serverMessage("[ADMIN COMMAND] [SUCCESS] set armor hp to " + c.getHpController().getArmorHp() + "/" + c.getHpController().getMaxArmorHp());
//		}
//	}

	private void setStructureHP(GameServerState state) throws IOException {
		SimpleTransformableSendableObject selectedObject = getSelectedObject(state);
		if(selectedObject != null && selectedObject instanceof SegmentController) {
			SegmentController c = (SegmentController) selectedObject;

			c.getHpController().setHpPercent(((Float) commandParams[0]));
			client.serverMessage("[ADMIN COMMAND] [SUCCESS] set hp to " + c.getHpController().getHp() + "/" + c.getHpController().getMaxHp());
		}
	}

//	private void resetArmorHP(GameServerState state) throws IOException {
//		SimpleTransformableSendableObject selectedObject = getSelectedObject(state);
//		if (selectedObject != null && selectedObject instanceof SegmentController) {
//			SegmentController c = (SegmentController) selectedObject;
//
//			c.getHpController().repairArmor(true);
//			client.serverMessage("[ADMIN COMMAND] [SUCCESS] armor reset");
//		}
//	}

	private void testStatisticsScript(GameServerState state) throws IOException {
		SimpleTransformableSendableObject selectedObject = getSelectedObject(state);
		if(selectedObject != null && selectedObject instanceof Ship) {
			Ship c = (Ship) selectedObject;

			try {
				EntityIndexScore calculateIndex = c.getManagerContainer().getStatisticsManager().calculateIndex();
				client.serverMessage("[ADMIN COMMAND] [SUCCESS] script executed: index (long version): " + calculateIndex.toString());
				client.serverMessage("[ADMIN COMMAND] [SUCCESS] script executed: index: " + calculateIndex.toShortString());
			} catch(Exception e) {
				e.printStackTrace();
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				client.serverMessage("[ADMIN COMMAND] [ERROR] script failed: " + c.getManagerContainer().getStatisticsManager().getScript() + "\n" + sw.toString());
			}

		}
	}

	private void resetStructureHP(GameServerState state) throws IOException {
		SimpleTransformableSendableObject selectedObject = getSelectedObject(state);
		if(selectedObject != null && selectedObject instanceof SegmentController) {
			SegmentController c = (SegmentController) selectedObject;

			c.getHpController().reboot(true);
			client.serverMessage("[ADMIN COMMAND] [SUCCESS] rebooted");
		}
	}

	private void trackSelectedUID(GameServerState state) throws IOException {
		String name = ((String) commandParams[0]);
		boolean track = ((Boolean) commandParams[1]);
		Sendable sendable = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(name);
		if(sendable instanceof SimpleTransformableSendableObject<?>) {
			track(state, track, (SimpleTransformableSendableObject<?>) sendable);
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] entity " + name + " not found in currently loaded objects");
		}
	}

	private void trackSelected(GameServerState state) throws IOException {
		boolean track = ((Boolean) commandParams[0]);
		SimpleTransformableSendableObject sendable = getSelectedObject(state);
		if(sendable instanceof SegmentController) {
			if(((SegmentController) sendable).lastAdminCheckFlag != 0) {
				client.serverMessage("[ADMIN COMMAND] [SUCCESS] entity " + ((SegmentController) sendable).getName() + " was flagged: " + (new Date(((SegmentController) sendable).lastAdminCheckFlag)));
			} else {
				client.serverMessage("[ADMIN COMMAND] [SUCCESS] entity " + ((SegmentController) sendable).getName() + " not flagged");
			}
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] invalid selected object");
		}
	}

	private void isFlagSelectedUID(GameServerState state) throws IOException {
		String name = ((String) commandParams[0]);
		boolean track = ((Boolean) commandParams[1]);
		Sendable sendable = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(name);
		if(sendable instanceof SegmentController) {
			if(((SegmentController) sendable).lastAdminCheckFlag != 0) {
				client.serverMessage("[ADMIN COMMAND] [SUCCESS] entity " + name + " was flagged: " + (new Date(((SegmentController) sendable).lastAdminCheckFlag)));
			} else {
				client.serverMessage("[ADMIN COMMAND] [SUCCESS] entity " + name + " not flagged");
			}
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] entity " + name + " not found in currently loaded objects");
		}
	}

	private void isFlagSelected(GameServerState state) throws IOException {
		boolean track = ((Boolean) commandParams[0]);
		SimpleTransformableSendableObject selectedObject = getSelectedObject(state);
		if(selectedObject instanceof SegmentController) {
			flag(state, track, (SegmentController) selectedObject);
		}
	}

	private void flagSelectedUID(GameServerState state) throws IOException {
		String name = ((String) commandParams[0]);
		boolean track = ((Boolean) commandParams[1]);
		Sendable sendable = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(name);
		if(sendable instanceof SegmentController) {
			flag(state, track, (SegmentController) sendable);
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] entity " + name + " not found in currently loaded objects");
		}
	}

	private void flagSelected(GameServerState state) throws IOException {
		boolean track = ((Boolean) commandParams[0]);
		SimpleTransformableSendableObject selectedObject = getSelectedObject(state);
		if(selectedObject instanceof SegmentController) {
			flag(state, track, (SegmentController) selectedObject);
		}
	}

	private void flag(GameServerState state, boolean track, SegmentController selectedObject) throws IOException {
		if(selectedObject != null) {
			selectedObject.lastAdminCheckFlag = track ? System.currentTimeMillis() : 0;
			selectedObject.getRuleEntityManager().triggerAdminFlagChanged();
			client.serverMessage("[ADMIN COMMAND] [SUCCESS] " + (track ? "FLAGGED" : "NOT FLAGGED") + " ENTITY " + selectedObject);
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] entity not flaggable or not found in currently loaded objects");
		}
	}

	private void track(GameServerState state, boolean track, SimpleTransformableSendableObject selectedObject) throws IOException {
		if(selectedObject != null) {
			selectedObject.setTracked(track);
			client.serverMessage("[ADMIN COMMAND] [SUCCESS] " + (track ? "TRACKING" : "NOT TRACKING") + " ENTITY " + selectedObject);
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] entity not trackable or not found in currently loaded objects");
		}
	}

	private void forcePlayerExit(GameServerState state) throws IOException {
		SimpleTransformableSendableObject selectedObject = getSelectedObject(state);
		if(selectedObject != null && selectedObject instanceof EditableSendableSegmentController) {
			EditableSendableSegmentController c = (EditableSendableSegmentController) selectedObject;
			c.forceAllCharacterExit();
			client.serverMessage("[ADMIN COMMAND] [SUCCESS] forced all player out of " + c.toNiceString());
		}
	}

	private void createTradeParty(GameServerState state) throws IOException {
		PlayerState playerFromName;
		try {

			playerFromName = state.getPlayerFromStateId(client.getId());

			Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get((int) playerFromName.getNetworkObject().selectedEntityId.get());
			ShopInterface shop = null;
			if(sendable != null) {
				if(sendable instanceof ShopInterface) {
					shop = (ShopInterface) sendable;
				} else if(sendable instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) sendable).getManagerContainer() instanceof ShopInterface) {
					shop = (ShopInterface) ((ManagedSegmentController<?>) sendable).getManagerContainer();
				}
			}
			if(shop != null) {
				Vector3i unloadedSectorAround = state.getSimulationManager().getUnloadedSectorAround(playerFromName.getCurrentSector(), new Vector3i());
				state.getSimulationManager().addJob(new SpawnTradingPartyJob(unloadedSectorAround, new Vector3i(playerFromName.getCurrentSector()), 3));
				client.serverMessage("[ADMIN COMMAND] [SUCCESS] Trade party created from the nearest unloaded sector");
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] No Shop Selected: " + playerFromName.getNetworkObject().selectedEntityId.get() + "->(" + sendable + ")");
			}

		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}
	}

	private void territoryUnclaimable(GameServerState state) throws IOException {
		int x = ((Integer) commandParams[0]);
		int y = ((Integer) commandParams[1]);
		int z = ((Integer) commandParams[2]);

		VoidSystem s = new VoidSystem();
		state.getDatabaseIndex().getTableManager().getSystemTable().loadSystem(state, new Vector3i(x, y, z), s);
		FactionSystemOwnerChange fk = new FactionSystemOwnerChange("ADMIN", FactionManager.TRAIDING_GUILD_ID, VoidSystem.UNCLAIMABLE, new Vector3i(x * VoidSystem.SYSTEM_SIZE + VoidSystem.SYSTEM_SIZE_HALF, y * VoidSystem.SYSTEM_SIZE + VoidSystem.SYSTEM_SIZE_HALF, z * VoidSystem.SYSTEM_SIZE + VoidSystem.SYSTEM_SIZE_HALF), new Vector3i(x, y, z), s.getName());

		state.getFactionManager().addFactionSystemOwnerChangeServer(fk);
		client.serverMessage("[ADMIN COMMAND] [SUCCESS] made system unclaimable: " + x + ", " + y + ", " + z);
	}

	private void territoryReset(GameServerState state) throws IOException {
		int x = ((Integer) commandParams[0]);
		int y = ((Integer) commandParams[1]);
		int z = ((Integer) commandParams[2]);

		VoidSystem s = new VoidSystem();
		state.getDatabaseIndex().getTableManager().getSystemTable().loadSystem(state, new Vector3i(x, y, z), s);
		FactionSystemOwnerChange fk = new FactionSystemOwnerChange("ADMIN", 0, "", s.getOwnerPos(), new Vector3i(x, y, z), s.getName());

		state.getFactionManager().addFactionSystemOwnerChangeServer(fk);
		client.serverMessage("[ADMIN COMMAND] [SUCCESS] unclaimed system: " + x + ", " + y + ", " + z);
	}

	private void factionPointProtectPlayer(GameServerState state) throws IOException {
		try {
			PlayerState p = state.getPlayerFromName((String) commandParams[0]);
			p.setFactionPointProtected((Boolean) commandParams[1]);
			client.serverMessage("[ADMIN COMMAND] [SUCCESS] player " + p.getName() + " FP protected: " + p.isFactionPointProtected());
		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] Player not found or not online'" + commandParams[0] + "'");
		}

	}

	private void factionPointGet(GameServerState state) throws IOException {
		Integer code = (Integer) commandParams[0];
		Faction faction = state.getGameState().getFactionManager().getFaction(code);

		if(faction != null) {
			client.serverMessage("[ADMIN COMMAND] [SUCCESS] faction points of " + faction.getName() + " now: " + faction.factionPoints);
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] Faction Not Found: " + code + " (use /list_factions, or check the faction hub)");
		}
	}

	private void factionPointAdd(GameServerState state) throws IOException {
		Integer code = (Integer) commandParams[0];
		Integer val = (Integer) commandParams[1];
		Faction faction = state.getGameState().getFactionManager().getFaction(code);

		if(faction != null) {
			faction.factionPoints += val;
			faction.sendFactionPointUpdate(state.getGameState());
			client.serverMessage("[ADMIN COMMAND] [SUCCESS] faction points of " + faction.getName() + " now: " + faction.factionPoints);
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] Faction Not Found: " + code + " (use /list_factions, or check the faction hub)");
		}
	}

	private void factionPointSet(GameServerState state) throws IOException {
		Integer code = (Integer) commandParams[0];
		Integer val = (Integer) commandParams[1];
		Faction faction = state.getGameState().getFactionManager().getFaction(code);

		if(faction != null) {
			faction.factionPoints = val;
			faction.sendFactionPointUpdate(state.getGameState());
			client.serverMessage("[ADMIN COMMAND] [SUCCESS] faction points of " + faction.getName() + " now: " + faction.factionPoints);
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] Faction Not Found: " + code + " (use /list_factions, or check the faction hub)");
		}
	}

	private void factionResetActivity(GameServerState state) throws IOException {
		Integer code = (Integer) commandParams[0];
		try {
			state.getGameState().getFactionManager().resetAllActivity(code);

			client.serverMessage("[ADMIN COMMAND] [SUCCESS] reset faction activity of " + code);
		} catch(FactionNotFoundException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] Faction Not Found: " + code);
		}
	}

	private void listDeniedCommands(GameServerState state) throws IOException {
		String name = (String) commandParams[0];

		if(state.isAdmin(name)) {
			Admin admin = state.getAdmins().get(name.toLowerCase(Locale.ENGLISH).trim());
			if(admin.deniedCommands.size() == 0) {
				client.serverMessage("Player " + name + " has no denied commands");
			} else {
				client.serverMessage("Denied Commands for " + name + ":");
				for(AdminCommands c : admin.deniedCommands) {
					client.serverMessage(c.name().toLowerCase(Locale.ENGLISH));
				}
			}

		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] Player " + name + " is not an admin");
		}

	}

	private void addOrRemoveAdminDeniedCommand(GameServerState state, boolean add) throws IOException {
		String name = (String) commandParams[0];
		String command = (String) commandParams[1];

		try {
			AdminCommands c = AdminCommands.valueOf(command.toUpperCase(Locale.ENGLISH).trim());

			if (state.isAdmin(name)) {
				if (add) {
					state.getController().addAdminDeniedCommand(client.getClientName(), name, c);
					client.serverMessage("[ADMIN COMMAND] [SUCCESS] Player " + name + " is now forbidden to use " + c.name().toLowerCase(Locale.ENGLISH));
				} else {
					state.getController().removeAdminDeniedCommand(client.getClientName(), name, c);
					client.serverMessage("[ADMIN COMMAND] [SUCCESS] Player " + name + " is no longer forbidden to use " + c.name().toLowerCase(Locale.ENGLISH));
				}
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] Player " + name + " is not an admin");
			}
		} catch(Exception e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] Admin Command unknown '" + command + "'");
		}
	}

	private void handleServerConfigBoolean(GameServerState state, ServerConfig c) throws IOException {
		boolean b = ((Boolean) commandParams[0]);

		c.setOn(b);
		ServerConfig.write();

		client.serverMessage("[ADMIN COMMAND] [SUCCESS] " + c.name() + " set to '" + b + "'");
	}

	private void tint(GameServerState state, boolean byPlayerName) throws IOException {
		float r = ((Float) commandParams[0]);
		float g = ((Float) commandParams[1]);
		float b = ((Float) commandParams[2]);
		float a = ((Float) commandParams[3]);
		try {
			PlayerState p;
			if(byPlayerName) {
				p = state.getPlayerFromName((String) commandParams[4]);
			} else {
				PlayerState playerFromName = state.getPlayerFromStateId(client.getId());

				Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(playerFromName.getNetworkObject().selectedEntityId.get());

				if(sendable != null && sendable instanceof PlayerControllable && !((PlayerControllable) sendable).getAttachedPlayers().isEmpty()) {
					p = ((PlayerControllable) sendable).getAttachedPlayers().get(0);
				} else {
					client.serverMessage("[ADMIN COMMAND] [ERROR] Player not found");
					return;
				}
			}

			p.getTint().set(r, g, b, a);
		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] Player not found");
		}
	}

	private void sectorInfo(GameServerState state) throws IOException {
		int x = ((Integer) commandParams[0]);
		int y = ((Integer) commandParams[1]);
		int z = ((Integer) commandParams[2]);

		try {
			Sector sectorWithoutLoading = state.getUniverse().getSectorWithoutLoading(new Vector3i(x, y, z));
			if(sectorWithoutLoading != null) {
				for(Sendable a : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
					if(a instanceof SimpleTransformableSendableObject && ((SimpleTransformableSendableObject) a).getSectorId() == sectorWithoutLoading.getId()) {
						client.serverMessage(((SimpleTransformableSendableObject) a).getInfo());
					}

				}
				client.serverMessage("LOADED SECTOR INFO: " + sectorWithoutLoading.toDetailString());
			} else {
				Sector sector = new Sector(state);
				if(state.getDatabaseIndex().getTableManager().getSectorTable().loadSector(new Vector3i(x, y, z), sector)) {

					List<DatabaseEntry> bySector = state.getDatabaseIndex().getTableManager().getEntityTable().getBySector(new Vector3i(x, y, z), 0);
					if(bySector.size() == 0) {
						client.serverMessage("[ADMIN COMMAND] No entity record for sector");
					} else {
						System.err.println("[ADMIN COMMAND] [SUCCESS] Displaying " + bySector.size() + " entities for Sector " + new Vector3i(x, y, z));
					}
					for(DatabaseEntry d : bySector) {
						client.serverMessage(d.toString());
					}
					client.serverMessage("SECTOR INFO: " + sector.toDetailString());
				} else {
					client.serverMessage("[ADMIN COMMAND] [ERROR] Sector " + new Vector3i(x, y, z) + " not in database");
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] Command known, but not executable: " + e.getClass().getSimpleName() + ": " + e.getMessage() + ". Please send in report!");
		}

	}

	private void printInfo(GameServerState state, Ship ship) throws IOException {
		client.serverMessage("ReactorHP: " + ship.getReactorHp() + " / " + ship.getReactorHpMax());
		client.serverMessage("CannonAmmoCapacity: " + ship.getAmmoCapacity(CANNON) + " / " + ship.getAmmoCapacityMax(CANNON));
		client.serverMessage("BeamAmmoCapacity: " + ship.getAmmoCapacity(BEAM) + " / " + ship.getAmmoCapacityMax(BEAM));
		client.serverMessage("MissileCapacity: " + ship.getAmmoCapacity(MISSILE) + " / " + ship.getAmmoCapacityMax(MISSILE));
		client.serverMessage("Attached: " + ship.getAttachedPlayers());
		client.serverMessage("DockedUIDs: " + getDockedList(ship));
		client.serverMessage("Blocks: " + ship.getTotalElements());
		client.serverMessage("Mass: " + ship.getMass());
		client.serverMessage("LastModified: " + ship.getLastModifier());
		client.serverMessage("Creator: " + ship.getSpawner());
		client.serverMessage("Sector: " + ship.getSectorId() + " -> " + state.getUniverse().getSector(ship.getSectorId()));
		client.serverMessage("Name: " + ship.getRealName());
		client.serverMessage("UID: " + ship.getUniqueIdentifier());
		client.serverMessage("MinBB(chunks): " + ship.getMinPos());
		client.serverMessage("MaxBB(chunks): " + ship.getMaxPos());
		client.serverMessage("Local-Pos: " + ship.getWorldTransform().origin);
		client.serverMessage("Orientation: " + Quat4fTools.set(ship.getWorldTransform().basis, new Quat4f()));
		client.serverMessage("FactionID: " + ship.getFactionId());
		client.serverMessage("Ship");
	}

	private String getDockedList(SegmentController ship) {

		StringBuffer b = new StringBuffer();

		for(int i = 0; i < ship.getDockingController().getDockedOnThis().size(); i++) {
			ElementDocking a = ship.getDockingController().getDockedOnThis().get(i);
			b.append(a.from.getSegment().getSegmentController().getUniqueIdentifier());
			if(i < ship.getDockingController().getDockedOnThis().size()) {
				b.append(", ");
			}
		}
		return b.toString();

	}

	private void printInfo(GameServerState state, Planet ship) throws IOException {
		client.serverMessage("Attached: " + ship.getAttachedPlayers());
		client.serverMessage("DockedUIDs: " + getDockedList(ship));
		client.serverMessage("Blocks: " + ship.getTotalElements());
		client.serverMessage("Mass: " + ship.getMass());
		client.serverMessage("LastModified: " + ship.getLastModifier());
		client.serverMessage("Creator: " + ship.getSpawner());
		client.serverMessage("Sector: " + ship.getSectorId() + " -> " + state.getUniverse().getSector(ship.getSectorId()));
		client.serverMessage("Name: " + ship.getRealName());
		client.serverMessage("UID: " + ship.getUniqueIdentifier());
		client.serverMessage("MinBB(chunks): " + ship.getMinPos());
		client.serverMessage("MaxBB(chunks): " + ship.getMaxPos());
		client.serverMessage("Local-Pos: " + ship.getWorldTransform().origin);
		client.serverMessage("Orientation: " + Quat4fTools.set(ship.getWorldTransform().basis, new Quat4f()));
		client.serverMessage("FactionID: " + ship.getFactionId());
		client.serverMessage("Planet");
	}

	private void printInfo(GameServerState state, SpaceStation ship) throws IOException {
		client.serverMessage("Attached: " + ship.getAttachedPlayers());
		client.serverMessage("DockedUIDs: " + getDockedList(ship));
		client.serverMessage("Blocks: " + ship.getTotalElements());
		client.serverMessage("Mass: " + ship.getMass());
		client.serverMessage("LastModified: " + ship.getLastModifier());
		client.serverMessage("Creator: " + ship.getSpawner());
		client.serverMessage("Sector: " + ship.getSectorId() + " -> " + state.getUniverse().getSector(ship.getSectorId()));
		client.serverMessage("Name: " + ship.getRealName());
		client.serverMessage("UID: " + ship.getUniqueIdentifier());
		client.serverMessage("MinBB(chunks): " + ship.getMinPos());
		client.serverMessage("MaxBB(chunks): " + ship.getMaxPos());
		client.serverMessage("Local-Pos: " + ship.getWorldTransform().origin);
		client.serverMessage("Orientation: " + Quat4fTools.set(ship.getWorldTransform().basis, new Quat4f()));
		client.serverMessage("FactionID: " + ship.getFactionId());
		client.serverMessage("Station");
	}

	private void printInfo(GameServerState state, SimpleTransformableSendableObject firstControlledTransformable) throws IOException {
		if(firstControlledTransformable != null) {
			if(firstControlledTransformable instanceof Ship) {
				printInfo(state, (Ship) firstControlledTransformable);
			}
			if(firstControlledTransformable instanceof SpaceStation) {
				printInfo(state, (SpaceStation) firstControlledTransformable);
			}
			if(firstControlledTransformable instanceof Planet) {
				printInfo(state, (Planet) firstControlledTransformable);
			}
		}
	}

	private void shipInfoSelected(GameServerState state) throws IOException {
		PlayerState playerFromName;
		try {

			playerFromName = state.getPlayerFromStateId(client.getId());
			SimpleTransformableSendableObject firstControlledTransformable = playerFromName.getFirstControlledTransformable();

			if(firstControlledTransformable == null || !(firstControlledTransformable instanceof SegmentController)) {
				System.err.println("[ADMIN COMMAND]checking selected");
				Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(playerFromName.getNetworkObject().selectedEntityId.get());
				if(sendable != null && sendable instanceof SimpleTransformableSendableObject) {
					firstControlledTransformable = (SimpleTransformableSendableObject) sendable;
				}

			}
			printInfo(state, firstControlledTransformable);
		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(PlayerControlledTransformableNotFound e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}
	}

	private void shipInfoName(GameServerState state) throws IOException {
		String name = (String) commandParams[0];
		try {

			synchronized(state.getLocalAndRemoteObjectContainer().getLocalObjects()) {
				boolean found = false;
				for(Sendable cc : state.getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
					if(cc instanceof SegmentController && ((SimpleTransformableSendableObject) cc).getRealName().equals(name)) {
						client.serverMessage("[INFO] " + ((SimpleTransformableSendableObject) cc).getRealName() + " found in loaded objects");
						printInfo(state, (SegmentController) cc);
						found = true;
					}

				}

				if(found) {
					return;
				}
			}
			client.serverMessage("[INFO] " + name + " not found in loaded objects. Checking Database...");
			List<DatabaseEntry> byUIDExact = state.getDatabaseIndex().getTableManager().getEntityTable().getByNameExact(name, 1);
			if(byUIDExact.size() > 0) {
				for(DatabaseEntry d : byUIDExact) {
					client.serverMessage(d.toString());
				}
			} else {
				client.serverMessage("[INFO] " + name + " not found in database");
			}
		} catch(SQLException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}
	}

	private void shipInfoUid(GameServerState state) throws IOException {
		String name = (String) commandParams[0];
		try {

			List<DatabaseEntry> byUIDExact = state.getDatabaseIndex().getTableManager().getEntityTable().getByUIDExact(DatabaseEntry.removePrefix(name), 1);

			client.serverMessage("Loaded: " + state.getLocalAndRemoteObjectContainer().getUidObjectMap().containsKey(name));

			if(byUIDExact.isEmpty()) {
				client.serverMessage("UID Not Found in DB: " + name + "; checking unsaved objects");
				if(state.getLocalAndRemoteObjectContainer().getUidObjectMap().containsKey(name)) {
					Sendable sendable = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(name);
					if(sendable != null && sendable instanceof SimpleTransformableSendableObject) {
						printInfo(state, (SimpleTransformableSendableObject) sendable);
					}
				} else {
					client.serverMessage("UID also not found in unsaved objects");
				}

			} else {
				for(DatabaseEntry d : byUIDExact) {
					client.serverMessage(d.toString());
					if(state.getLocalAndRemoteObjectContainer().getUidObjectMap().containsKey(name)) {
						Sendable sendable = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(name);
						if(sendable != null && sendable instanceof SimpleTransformableSendableObject) {
							printInfo(state, (SimpleTransformableSendableObject) sendable);
						}
					}

				}
			}
		} catch(SQLException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(IllegalArgumentException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] you need to provide the full UID (e.g. ENTITY_SHIP_RESTOFUID)");
		}
	}

	private void factionPointTurn(GameServerState state) throws IOException {
		state.getFactionManager().forceFactionPointTurn();
		client.serverMessage("[ADMIN COMMAND] [SUCCESS] faction point turn forced");
	}

	private void setEntityPermissionUID(GameServerState state) throws IOException {
		String uid = (String) commandParams[0];
		Sendable sendable = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(uid);

		if(sendable != null && sendable instanceof SegmentController) {
			byte b = ((Integer) commandParams[1]).byteValue();
			b = (byte) Math.max(FactionRoles.NOT_SET_RANK, Math.min(FactionRoles.ROLE_COUNT - 1, b));
			SegmentController cont = (SegmentController) sendable;
			cont.setFactionRights(b);

			client.serverMessage("[ADMIN COMMAND] [SUCCESS] Faction Rank of " + cont + " set to " + b + " = " + FactionRoles.getRoleName(b));
		}
	}

	private void setEntityPermission(GameServerState state) throws IOException {

		SegmentController cont = getSelectedOrEnteredStructure(state);
		byte b = ((Integer) commandParams[0]).byteValue();
		b = (byte) Math.max(FactionRoles.NOT_SET_RANK, Math.min(FactionRoles.ROLE_COUNT - 1, b));


		cont.setFactionRights(b);

		client.serverMessage("[ADMIN COMMAND] [SUCCESS] Faction Rank of " + cont + " set to " + b + " = " + FactionRoles.getRoleName(b));
	}

	private void decay(GameServerState state) throws IOException {
		boolean dec = (Boolean) commandParams[0];
		PlayerState playerFromName;
		try {
			playerFromName = state.getPlayerFromStateId(client.getId());
			Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(playerFromName.getNetworkObject().selectedEntityId.get());
			if(sendable != null && sendable instanceof SegmentController) {
				SegmentController g = (SegmentController) sendable;
				g.setScrap(dec);
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] nothing valid selected");
			}
		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}
	}

	private void minable(GameServerState state) throws IOException {
		boolean dec = (Boolean) commandParams[0];
		PlayerState playerFromName;
		try {
			playerFromName = state.getPlayerFromStateId(client.getId());
			Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(playerFromName.getNetworkObject().selectedEntityId.get());
			if(sendable != null && sendable instanceof SegmentController) {
				SegmentController g = (SegmentController) sendable;
				g.setMinable(dec);
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] nothing valid selected");
			}
		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}
	}

	private void vulnerable(GameServerState state) throws IOException {
		boolean dec = (Boolean) commandParams[0];
		PlayerState playerFromName;
		try {
			playerFromName = state.getPlayerFromStateId(client.getId());
			Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(playerFromName.getNetworkObject().selectedEntityId.get());
			if(sendable != null && sendable instanceof SegmentController) {
				SegmentController g = (SegmentController) sendable;
				g.setVulnerable(dec);
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] nothing valid selected");
			}
		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}
	}

	private void minableUid(GameServerState state) throws IOException {
		boolean dec = (Boolean) commandParams[1];
		PlayerState playerFromName;
		String uid = (String) commandParams[0];
		Sendable sendable = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(uid);
		if(sendable != null && sendable instanceof SegmentController) {
			SegmentController g = (SegmentController) sendable;
			g.setMinable(dec);
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] nothing valid selected");
		}
	}

	private void vulnerableUid(GameServerState state) throws IOException {
		boolean dec = (Boolean) commandParams[1];
		PlayerState playerFromName;
		String uid = (String) commandParams[0];
		Sendable sendable = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(uid);

		if(sendable != null && sendable instanceof SegmentController) {
			SegmentController g = (SegmentController) sendable;
			g.setVulnerable(dec);
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] nothing valid selected");
		}
	}

	private void shieldDamage(GameServerState state) throws IOException {
		int damage = (Integer) commandParams[0];
		PlayerState playerFromName;
		try {

			playerFromName = state.getPlayerFromStateId(client.getId());
			SimpleTransformableSendableObject firstControlledTransformable = playerFromName.getFirstControlledTransformable();

			if(firstControlledTransformable == null || !(firstControlledTransformable instanceof SegmentController)) {
				System.err.println("[ADMIN COMMAND]checking selected");
				Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(playerFromName.getNetworkObject().selectedEntityId.get());
				if(sendable != null && sendable instanceof SimpleTransformableSendableObject) {
					firstControlledTransformable = (SimpleTransformableSendableObject) sendable;
				}

			}
			boolean ok = false;
			if(firstControlledTransformable != null && firstControlledTransformable instanceof ManagedSegmentController<?>) {
				if(((ManagedSegmentController<?>) firstControlledTransformable).getManagerContainer() instanceof ShieldContainerInterface) {
					ShieldAddOn shieldAddOn = ((ShieldContainerInterface) ((ManagedSegmentController<?>) firstControlledTransformable).getManagerContainer()).getShieldAddOn();
					ok = true;
					shieldAddOn.onHit(0L, (short) 0, damage, DamageDealerType.GENERAL);
					client.serverMessage("[ADMIN COMMAND] [SUCCESS] took out shields of " + firstControlledTransformable);

					shieldAddOn.getShieldLocalAddOn().hitAllShields(damage);
				}
			}

			if(!ok) {
				client.serverMessage("[ADMIN COMMAND] [ERROR] object " + firstControlledTransformable + " has no shield capability");
			}
		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(PlayerControlledTransformableNotFound e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}
	}

	private void shieldOutage(GameServerState state) throws IOException {
		PlayerState playerFromName;
		try {

			playerFromName = state.getPlayerFromStateId(client.getId());
			SimpleTransformableSendableObject firstControlledTransformable = playerFromName.getFirstControlledTransformable();

			if(firstControlledTransformable == null || !(firstControlledTransformable instanceof SegmentController)) {
				System.err.println("[ADMIN COMMAND]checking selected");
				Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(playerFromName.getNetworkObject().selectedEntityId.get());
				if(sendable != null && sendable instanceof SimpleTransformableSendableObject) {
					firstControlledTransformable = (SimpleTransformableSendableObject) sendable;
				}

			}
			boolean ok = false;
			if(firstControlledTransformable != null && firstControlledTransformable instanceof ManagedSegmentController<?>) {
				if(((ManagedSegmentController<?>) firstControlledTransformable).getManagerContainer() instanceof ShieldContainerInterface) {
					ShieldAddOn shieldAddOn = ((ShieldContainerInterface) ((ManagedSegmentController<?>) firstControlledTransformable).getManagerContainer()).getShieldAddOn();
					ok = true;
					shieldAddOn.onHit(0L, (short) 0, (long) Math.ceil(shieldAddOn.getShields()), DamageDealerType.GENERAL);

					shieldAddOn.getShieldLocalAddOn().dischargeAllShields();
					client.serverMessage("[ADMIN COMMAND] [SUCCESS] took out shields of " + firstControlledTransformable);
				}
			}

			if(!ok) {
				client.serverMessage("[ADMIN COMMAND] [ERROR] object " + firstControlledTransformable + " has no shield capability");
			}
		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(PlayerControlledTransformableNotFound e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}
	}

	private void powerOutage(GameServerState state) throws IOException {
		PlayerState playerFromName;
		try {

			playerFromName = state.getPlayerFromStateId(client.getId());
			SimpleTransformableSendableObject firstControlledTransformable = playerFromName.getFirstControlledTransformable();

			if(firstControlledTransformable == null || !(firstControlledTransformable instanceof SegmentController)) {
				System.err.println("[ADMIN COMMAND]checking selected");
				Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(playerFromName.getNetworkObject().selectedEntityId.get());
				if(sendable != null && sendable instanceof SimpleTransformableSendableObject) {
					firstControlledTransformable = (SimpleTransformableSendableObject) sendable;
				}

			}
			boolean ok = false;
			if(firstControlledTransformable != null && firstControlledTransformable instanceof ManagedSegmentController<?>) {
				if(((ManagedSegmentController<?>) firstControlledTransformable).getManagerContainer() instanceof PowerManagerInterface) {
					PowerAddOn powerAddOn = ((PowerManagerInterface) ((ManagedSegmentController<?>) firstControlledTransformable).getManagerContainer()).getPowerAddOn();
					ok = true;
					powerAddOn.consumePowerInstantly(powerAddOn.getPower());
					client.serverMessage("[ADMIN COMMAND] [SUCCESS] took out power of " + firstControlledTransformable);
				}
			}

			if(!ok) {
				client.serverMessage("[ADMIN COMMAND] [ERROR] object " + firstControlledTransformable + " has no shield capability");
			}
		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(PlayerControlledTransformableNotFound e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}
	}

	private void serverMessageTo(GameServerState state) throws IOException {

		String type = (String) commandParams[0];
		String name = (String) commandParams[1];
		String message = (String) commandParams[2];

		message = message.replaceAll("\\\\n", "\n");

		byte msgType;

		if(type.toLowerCase(Locale.ENGLISH).equals("plain")) {
			msgType = ServerMessage.MESSAGE_TYPE_SIMPLE;
		} else if(type.toLowerCase(Locale.ENGLISH).equals("info")) {
			msgType = ServerMessage.MESSAGE_TYPE_INFO;
		} else if(type.toLowerCase(Locale.ENGLISH).equals("warning")) {
			msgType = ServerMessage.MESSAGE_TYPE_WARNING;
		} else if(type.toLowerCase(Locale.ENGLISH).equals("error")) {
			msgType = ServerMessage.MESSAGE_TYPE_ERROR;
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] message type must be either plain/info/warning/error");
			return;
		}

		PlayerState player = null;
		try {
			player = state.getPlayerFromName(name);

			player.sendServerMessage(new ServerMessage(new Object[]{message}, msgType, player.getId()));

		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] player " + name + " not online");
		}

	}

	private void serverMessageBroadcast(GameServerState state) throws IOException {
		String type = (String) commandParams[0];
		String message = (String) commandParams[1];
		message = message.replaceAll("\\\\n", "\n");
		byte msgType;

		if(type.toLowerCase(Locale.ENGLISH).equals("plain")) {
			msgType = ServerMessage.MESSAGE_TYPE_SIMPLE;
		} else if(type.toLowerCase(Locale.ENGLISH).equals("info")) {
			msgType = ServerMessage.MESSAGE_TYPE_INFO;
		} else if(type.toLowerCase(Locale.ENGLISH).equals("warning")) {
			msgType = ServerMessage.MESSAGE_TYPE_WARNING;
		} else if(type.toLowerCase(Locale.ENGLISH).equals("error")) {
			msgType = ServerMessage.MESSAGE_TYPE_ERROR;
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] message type must be either plain/info/warning/error");
			return;
		}

		state.getController().broadcastMessage(new Object[]{message}, msgType);
	}

	private void explodePlanetSector(GameServerState state, boolean core) {
		PlayerState playerFromName;
		try {

			playerFromName = state.getPlayerFromStateId(client.getId());

			SimpleTransformableSendableObject s = playerFromName.getFirstControlledTransformable();

			synchronized(state.getLocalAndRemoteObjectContainer().getLocalObjects()) {
				for(Sendable cc : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
					if(!core && cc instanceof Planet) {
						if(((Planet) cc).getSectorId() == s.getSectorId()) {
							((Planet) cc).setPlanetCore(null);
							((Planet) cc).setPlanetCoreUID("none");
							((Planet) cc).setBlownOff(new Vector3f(0, 0, 0));
						}
					}
					if(core && cc instanceof PlanetCore) {
						if(((PlanetCore) cc).getSectorId() == s.getSectorId()) {
							((PlanetCore) cc).setDestroyed(true);
						}
					}
				}
			}

		} catch(PlayerNotFountException e) {
			e.printStackTrace();
		} catch(PlayerControlledTransformableNotFound e) {
			e.printStackTrace();
		}
	}

	private void clearOverheatingSector(GameServerState state) throws IOException {
		PlayerState playerFromName;
		try {
			int x = ((Integer) commandParams[0]);
			int y = ((Integer) commandParams[1]);
			int z = ((Integer) commandParams[2]);

			Sector sec = state.getUniverse().getSectorWithoutLoading(new Vector3i(x, y, z));
			if(sec == null) {

				client.serverMessage("[ADMIN COMMAND] [ERROR] sector " + x + ", " + y + ", " + z + " unloaded");
				return;
			}

			playerFromName = state.getPlayerFromStateId(client.getId());


			synchronized(state.getLocalAndRemoteObjectContainer().getLocalObjects()) {
				int count = 0;
				for(Sendable cc : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
					if(cc instanceof SegmentController) {
						if(((((SegmentController) cc).getSectorId() == sec.getSectorId())) && ((SegmentController) cc).isCoreOverheating()) {
							if(cc instanceof SegmentController) {
								SegmentController ss = (SegmentController) cc;
								ss.railController.undockAllServer();
							}
							cc.markForPermanentDelete(true);
							cc.setMarkedForDeleteVolatile(true);
							count++;
						}
					}
				}

				client.serverMessage("[ADMIN COMMAND] [SUCCESS] cleared overheating in sector " + x + ", " + y + ", " + z + ": " + count);
			}

		} catch(PlayerNotFountException e) {
			e.printStackTrace();
		}
	}

	private void clearOverheating(GameServerState state, boolean all) throws IOException {
		PlayerState playerFromName;
		try {
			playerFromName = state.getPlayerFromStateId(client.getId());

			SimpleTransformableSendableObject s = playerFromName.getFirstControlledTransformable();

			synchronized(state.getLocalAndRemoteObjectContainer().getLocalObjects()) {
				int count = 0;
				for(Sendable cc : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
					if(cc instanceof SegmentController) {
						if((all || (((SegmentController) cc).getSectorId() == s.getSectorId() || ((SegmentController) cc).isNeighbor(s.getSectorId(), ((SegmentController) cc).getSectorId()))) && ((SegmentController) cc).isCoreOverheating()) {
							if(cc instanceof SegmentController) {
								SegmentController ss = (SegmentController) cc;
								ss.railController.undockAllServer();
							}
							cc.markForPermanentDelete(true);
							cc.setMarkedForDeleteVolatile(true);
							count++;
						}
					}
					client.serverMessage("[ADMIN COMMAND] [SUCCESS] cleared overheating: " + count);
				}
			}

		} catch(PlayerNotFountException e) {
			e.printStackTrace();
		} catch(PlayerControlledTransformableNotFound e) {
			e.printStackTrace();
		}
	}

	private void clearSystemShipSpawns(GameServerState state, boolean all) {
		PlayerState playerFromName;
		try {
			playerFromName = state.getPlayerFromStateId(client.getId());

			SimpleTransformableSendableObject s = playerFromName.getFirstControlledTransformable();

			synchronized(state.getLocalAndRemoteObjectContainer().getLocalObjects()) {
				for(Sendable cc : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
					if(cc instanceof Ship) {
						if((all || (((Ship) cc).getSectorId() == s.getSectorId() || ((Ship) cc).isNeighbor(s.getSectorId(), ((Ship) cc).getSectorId()))) && ((Ship) cc).getSpawner().toLowerCase(Locale.ENGLISH).equals("<system>")) {
							cc.markForPermanentDelete(true);
							cc.setMarkedForDeleteVolatile(true);
						}
					}
				}
			}

		} catch(PlayerNotFountException e) {
			e.printStackTrace();
		} catch(PlayerControlledTransformableNotFound e) {
			e.printStackTrace();
		}
	}

	private void createCreatureSpawnerTest(GameServerState state) throws IOException {
		PlayerState playerFromName;
		try {

			playerFromName = state.getPlayerFromStateId(client.getId());
			SimpleTransformableSendableObject firstControlledTransformable = playerFromName.getFirstControlledTransformable();
			Vector3f pos;
			if(firstControlledTransformable instanceof AbstractCharacter<?>) {
				pos = new Vector3f(((AbstractCharacter<?>) firstControlledTransformable).getHeadWorldTransform().origin);
			} else {
				pos = new Vector3f(firstControlledTransformable.getWorldTransform().origin);

			}
			Vector3f to = new Vector3f(pos);
			Vector3f forw = playerFromName.getForward(new Vector3f());
			forw.scale(5000);
			to.add(forw);
			Sector sector = state.getUniverse().getSector(firstControlledTransformable.getSectorId());
			ClosestRayResultCallback testRayCollisionPoint = ((PhysicsExt) sector.getPhysics()).testRayCollisionPoint(pos, to, false, null, null, false, true, false);
			if(testRayCollisionPoint.hasHit() && testRayCollisionPoint instanceof CubeRayCastResult) {
				CubeRayCastResult c = (CubeRayCastResult) testRayCollisionPoint;
				if(c.getSegment() != null) {

					DefaultSpawner s = new DefaultSpawner();

					SpawnMarker marker = new SpawnMarker(c.getNextToAbsolutePosition(), c.getSegment().getSegmentController(), s);

					SpawnComponentCreature spawnComponentCreature = new SpawnComponentCreature();
					spawnComponentCreature.setBottom("LegsArag");
					spawnComponentCreature.setMiddle("TorsoShell");
					spawnComponentCreature.setName("Spider");
					spawnComponentCreature.setCreatureType(CreatureType.CREATURE_SPECIFIC);
					spawnComponentCreature.setFactionId(FactionManager.FAUNA_GROUP_ENEMY[0]);
					s.getComponents().add(spawnComponentCreature);
					s.getComponents().add(new SpawnComponentDestroySpawnerAfterCount(5));

					s.getConditions().add(new SpawnConditionTime(5000));
					s.getConditions().add(new SpawnConditionCreatureCountOnAffinity(1));

					c.getSegment().getSegmentController().getSpawnController().getSpawnMarker().add(marker);

					client.serverMessage("[ADMIN COMMAND] Spawner Spawned");
				} else {
					client.serverMessage("[ADMIN COMMAND] [ERROR] no object in line of sight");
				}
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] no object in line of sight");
			}

		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(PlayerControlledTransformableNotFound e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}
	}

	private void spawnCreature(GameServerState state) throws IOException {

		PlayerState playerFromName;
		try {
			playerFromName = state.getPlayerFromStateId(client.getId());

			CreatureSpawn s = new CreatureSpawn(new Vector3i(playerFromName.getCurrentSector()), new Transform(playerFromName.getFirstControlledTransformable().getWorldTransform()), "NoName", CreatureType.CHARACTER) {
				@Override
				public void initAI(AIGameCreatureConfiguration<?, ?> aiConfiguration) {
					try {
						assert (aiConfiguration != null);
						aiConfiguration.get(Types.ORIGIN_X).switchSetting(String.valueOf(Integer.MIN_VALUE), false);
						aiConfiguration.get(Types.ORIGIN_Y).switchSetting(String.valueOf(Integer.MIN_VALUE), false);
						aiConfiguration.get(Types.ORIGIN_Z).switchSetting(String.valueOf(Integer.MIN_VALUE), false);

						aiConfiguration.get(Types.ROAM_X).switchSetting("22", false);
						aiConfiguration.get(Types.ROAM_Y).switchSetting("3", false);
						aiConfiguration.get(Types.ROAM_Z).switchSetting("22", false);
					} catch(StateParameterNotFoundException e) {
						e.printStackTrace();
					}

				}
			};

			state.getController().queueCreatureSpawn(s);
			client.serverMessage("[ADMIN COMMAND] [SUCCESS] Spawning creature");
		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] No player");
		} catch(PlayerControlledTransformableNotFound e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] No player position found");
		}

	}

	private void spawnMassCreature(GameServerState state) throws IOException {
		int amount = (Integer) commandParams[0];

		PlayerState playerFromName;
		try {
			playerFromName = state.getPlayerFromStateId(client.getId());

			SimpleTransformableSendableObject tr = playerFromName.getFirstControlledTransformable();

			for(int i = 0; i < amount; i++) {
				Transform trans = new Transform(tr.getWorldTransform());
				Vector3f add = new Vector3f(Universe.getRandom().nextFloat() * 50 - 25, 20, Universe.getRandom().nextFloat() * 50 - 25);
				trans.basis.transform(add);
				trans.origin.add(add);
				Vector3i p = new Vector3i();
				try {
					SegmentPiece piece = getPiece(trans.origin, new Vector3f(0, -1, 0), state, tr, p);

					piece.getTransform(trans);
					//					trans.origin.y += 1;
					CreatureSpawn s = new CreatureSpawn(new Vector3i(playerFromName.getCurrentSector()), new Transform(trans), "NoName" + System.currentTimeMillis(), CreatureType.CHARACTER) {
						@Override
						public void initAI(AIGameCreatureConfiguration<?, ?> aiConfiguration) {
							try {
								assert (aiConfiguration != null);
								aiConfiguration.get(Types.ORIGIN_X).switchSetting(String.valueOf(Integer.MIN_VALUE), false);
								aiConfiguration.get(Types.ORIGIN_Y).switchSetting(String.valueOf(Integer.MIN_VALUE), false);
								aiConfiguration.get(Types.ORIGIN_Z).switchSetting(String.valueOf(Integer.MIN_VALUE), false);

								aiConfiguration.get(Types.ROAM_X).switchSetting("22", false);
								aiConfiguration.get(Types.ROAM_Y).switchSetting("3", false);
								aiConfiguration.get(Types.ROAM_Z).switchSetting("22", false);
							} catch(StateParameterNotFoundException e) {
								e.printStackTrace();
							}
						}
					};

					state.getController().queueCreatureSpawn(s);
					System.err.println("SPAWNING CREATURE AT: " + trans.origin);
				} catch(NoCollisioinFountException e) {
					e.printStackTrace();
				}
			}
			client.serverMessage("[ADMIN COMMAND] [SUCCESS] Spawning mass creature");
		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] No player");
		} catch(PlayerControlledTransformableNotFound e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] No player position found");
		}

	}

	private void playerUnprotect(GameServerState state) throws IOException {
		String plname = (String) commandParams[0];
		ProtectedUplinkName remove = state.getController().removeProtectedUser(plname.trim());
		if(remove != null) {
			client.serverMessage("[ADMIN COMMAND] [SUCCESS] removed account protection " + remove.uplinkname + " from playerName " + remove.playername);
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] no protection for player name " + plname + " existed");
		}
	}

	private void playerProtect(GameServerState state) throws IOException {
		String plname = (String) commandParams[0];
		String ulname = (String) commandParams[1];

		state.getController().addProtectedUser(ulname.trim(), plname.trim());

		client.serverMessage("[ADMIN COMMAND] [SUCCESS] added account protection '" + ulname + "' to playerName " + plname);
	}

	private void playerList(GameServerState state) throws IOException {
		for(PlayerState p : state.getPlayerStatesByName().values()) {
			outputPlayer(state, p);
		}
	}

	private void playerInfo(GameServerState state) throws IOException {
		String name = (String) commandParams[0];
		PlayerState player = null;
		try {
			player = state.getPlayerFromName(name);

		} catch(PlayerNotFountException e) {
			System.err.println("[ADMIN] Player not online: " + name + "; checking logged off");
			File playerFile = new FileExt(GameServerState.ENTITY_DATABASE_PATH + File.separator + "ENTITY_PLAYERSTATE_" + name.trim() + ".ent");

			Tag tag;
			try {
				tag = Tag.readFrom(new BufferedInputStream(new FileInputStream(playerFile)), true, false);
				player = new PlayerState(state);
				player.initialize();
				player.fromTagStructure(tag);
				String fName = playerFile.getName();
				player.setName(fName.substring("ENTITY_PLAYERSTATE_".length(), fName.lastIndexOf(".")));
			} catch(FileNotFoundException e1) {
				client.serverMessage("[ADMIN COMMAND] [ERROR] player " + name + " not online, and no offline save state found");
				e1.printStackTrace();
			} catch(IOException e1) {
				client.serverMessage("[ADMIN COMMAND] [ERROR] player " + name + " not online, and no offline save state reading failed");
				e1.printStackTrace();
			}
		}
		if(player != null) {
			//			StringBuilder sb = new StringBuilder();
			outputPlayer(state, player);
		}
	}

	private void outputPlayer(GameServerState state, PlayerState player) throws IOException {
		ArrayList<PlayerInfoHistory> h = new ArrayList<PlayerInfoHistory>();
		h.addAll(player.getHosts());
		for(int j = 0; j < h.size(); j++) {
			//			sb.append(h.get(j));
			//			if(j < player.getHosts().size()-1){
			//				sb.append(",");
			//			}
			client.serverMessage("[PL] LOGIN: " + h.get(j));
		}
		client.serverMessage("[PL] PERSONAL-TEST-SECTOR: " + player.testSector);
		client.serverMessage("[PL] PERSONAL-BATTLE_MODE-SECTOR: " + player.testSector);
		try {
			client.serverMessage("[PL] CONTROLLING-POS: " + player.getFirstControlledTransformable().getWorldTransform().origin);
			client.serverMessage("[PL] CONTROLLING: " + player.getFirstControlledTransformable());
		} catch(PlayerControlledTransformableNotFound e) {
//			e.printStackTrace();
			client.serverMessage("[PL] CONTROLLING-POS: <not spawned>");
			client.serverMessage("[PL] CONTROLLING: <not spawned>");
		}
		client.serverMessage("[PL] SECTOR: " + player.getCurrentSector());
		client.serverMessage("[PL] FACTION: " + state.getFactionManager().getFaction(player.getFactionId()));
		client.serverMessage("[PL] CREDITS: " + player.getCredits());
		client.serverMessage("[PL] UPGRADED: " + player.isUpgradedAccount());
		client.serverMessage("[PL] SM-NAME: " + player.getStarmadeName());

		client.serverMessage("[PL] IP: " + player.getIp());
		client.serverMessage("[PL] Name: " + player.getName());
	}

	private void exportSectorBulk(GameServerState state) {
		String name = (String) commandParams[0];
		state.scheduleSectorBulkExport(client, name);
	}

	private void exportSectorNorm(GameServerState state) {
		Vector3i sec = new Vector3i((Integer) commandParams[0], (Integer) commandParams[1], (Integer) commandParams[2]);
		String name = (String) commandParams[3];
		state.scheduleSectorExport(sec, client, name);
	}

	private void factionEdit(GameServerState state) throws IOException {
		Integer code = (Integer) commandParams[0];
		String name = (String) commandParams[1];
		String description = (String) commandParams[2];

		Faction faction = state.getGameState().getFactionManager().getFaction(code);
		if(faction != null) {
			faction.setDescription(description);
			faction.setName(name);
			faction.sendDescriptionMod("ADMIN", description, state.getGameState());
			faction.sendNameMod("ADMIN", name, state.getGameState());
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] Faction Not Found (must be ID, list with /faction_list: " + code);
		}

		client.serverMessage("[ADMIN COMMAND] [SUCCESS] added new faction!");
	}

	private void factionModMember(GameServerState state) throws IOException {
		String name = (String) commandParams[0];
		int roleid = (Integer) commandParams[1];

		try {
			PlayerState playerFromName = state.getPlayerFromName(name);

			Faction f = state.getFactionManager().getFaction(playerFromName.getFactionId());
			if(f != null) {
				FactionPermission factionPermission = f.getMembersUID().get(playerFromName.getName());

				if(factionPermission != null) {
					if(roleid > 0 && roleid < 6) {
						f.addOrModifyMember("ADMIN", playerFromName.getName(), (byte) (roleid - 1), System.currentTimeMillis(), state.getGameState(), true);
					} else {
						client.serverMessage("[ADMIN COMMAND] [ERROR] role id must be between 1 and 5 ");
					}
				} else {
					client.serverMessage("[ADMIN COMMAND] [ERROR] player is not part of the faction " + f.getName());
				}
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] player is not in a faction; fid: " + playerFromName.getFactionId());
			}

		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] player not found for your client");
		}

	}

	private void factionModRelation(GameServerState state) throws IOException {
		int facA = (Integer) commandParams[0];
		int facB = (Integer) commandParams[1];
		String rel = ((String) commandParams[2]).trim().toLowerCase(Locale.ENGLISH);

		if(!rel.equals("enemy") && !rel.equals("ally") && !rel.equals("neutral")) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] relation must either be enemy, ally, or neutral");
			return;
		}

		Faction fA = state.getFactionManager().getFaction(facA);
		Faction fB = state.getFactionManager().getFaction(facB);
		if(fA != null && fB != null) {
			state.getFactionManager().setRelationServer(facA, facB, (byte) (rel.equals("neutral") ? RType.NEUTRAL.ordinal() : (rel.equals("enemy") ? RType.ENEMY.ordinal() : RType.FRIEND.ordinal())));
		} else {
			if(fA == null) {
				client.serverMessage("[ADMIN COMMAND] [ERROR] faction ID not found: " + facA);
			}
			if(fB == null) {
				client.serverMessage("[ADMIN COMMAND] [ERROR] faction ID not found: " + facB);
			}
		}

	}

	private void factionRemoveMember(GameServerState state) throws IOException {
		String name = (String) commandParams[0];

		try {
			PlayerState playerFromName = state.getPlayerFromName(name);

			Faction f = state.getFactionManager().getFaction(playerFromName.getFactionId());
			if(f != null) {
				FactionPermission factionPermission = f.getMembersUID().get(playerFromName.getName());

				if(factionPermission != null) {
					f.removeMember(name, state.getGameState());
				} else {
					client.serverMessage("[ADMIN COMMAND] [ERROR] player is not part of the faction " + f.getName());
				}
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] player is not in a faction; fid: " + playerFromName.getFactionId());
			}

		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] player not found for your client");
		}

	}

	private void factionsReinstitude(GameServerState state) throws IOException {
		state.setFactionReinstitudeFlag(true);
	}

	private void giveSlot(GameServerState state) throws IOException {
		PlayerState playerFromName;
		try {
			Integer count = (Integer) commandParams[0];

			playerFromName = state.getPlayerFromStateId(client.getId());

			short type = playerFromName.getInventory().getType(playerFromName.getSelectedBuildSlot());
			if(ElementKeyMap.isValidType(type)) {
				int slot = playerFromName.getInventory().incExistingOrNextFreeSlot(type, count);
				playerFromName.sendInventoryModification(slot, Long.MIN_VALUE);
			} else if(type == InventorySlot.MULTI_SLOT) {
				InventorySlot slot = playerFromName.getInventory().getSlot(playerFromName.getSelectedBuildSlot());
				if(slot != null) {
					for(int i = 0; i < slot.getSubSlots().size(); i++) {
						InventorySlot s = slot.getSubSlots().get(i);
						int sl = playerFromName.getInventory().incExistingOrNextFreeSlot(s.getType(), count);

						playerFromName.sendInventoryModification(sl, Long.MIN_VALUE);
						if(i < slot.getSubSlots().size() && s != slot.getSubSlots().get(i)) {
							//check if slot has been removed
							i--;
						}
					}
				}
			}
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}
	}

	private void giveLook(GameServerState state) throws IOException {
		PlayerState playerFromName;
		try {
			Integer count = (Integer) commandParams[0];

			playerFromName = state.getPlayerFromStateId(client.getId());
			SimpleTransformableSendableObject firstControlledTransformable = playerFromName.getFirstControlledTransformable();
			Vector3f pos;
			if(firstControlledTransformable instanceof AbstractCharacter<?>) {
				pos = new Vector3f(((AbstractCharacter<?>) firstControlledTransformable).getHeadWorldTransform().origin);
			} else {
				pos = new Vector3f(firstControlledTransformable.getWorldTransform().origin);

			}
			Vector3f to = new Vector3f(pos);
			Vector3f forw = playerFromName.getForward(new Vector3f());
			forw.scale(5000 * Element.BLOCK_SIZE);
			to.add(forw);
			Sector sector = state.getUniverse().getSector(firstControlledTransformable.getSectorId());
			ClosestRayResultCallback testRayCollisionPoint = ((PhysicsExt) sector.getPhysics()).testRayCollisionPoint(pos, to, false, null, null, false, true, false);
			if(testRayCollisionPoint.hasHit() && testRayCollisionPoint instanceof CubeRayCastResult) {
				CubeRayCastResult c = (CubeRayCastResult) testRayCollisionPoint;

				if(c.getSegment() != null) {
					SegmentPiece p = new SegmentPiece(c.getSegment(), c.getCubePos());
					short type = p.getType();

					client.serverMessage("[ADMIN COMMAND] Given object " + ElementKeyMap.toString(type));
					int slot = playerFromName.getInventory().incExistingOrNextFreeSlot(type, count);
					playerFromName.sendInventoryModification(slot, Long.MIN_VALUE);
				} else {
					System.err.println("[ADMINCOMMAND] giveLook: segment null");
					client.serverMessage("[ADMIN COMMAND] ERROR: giveLook: segment null");
				}

			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] no object in line of sight");
			}

		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(PlayerControlledTransformableNotFound e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}
	}

	private void give(GameServerState state) throws IOException {

		String name = (String) commandParams[0];

		StringBuilder s = new StringBuilder();
		for(int i = 1; i < commandParams.length - 1; i++) {
			s.append((String) commandParams[i]);
			if(i < commandParams.length - 2) {
				s.append(" ");
			}
		}
		String type = s.toString();

		Integer count = (Integer) commandParams[commandParams.length - 1];

		try {

			ArrayList<Short> names = new ArrayList<Short>();
			for(Short c : ElementKeyMap.typeList()) {
				if(ElementKeyMap.getInfo(c).getName().toLowerCase(Locale.ENGLISH).contains(type.trim().toLowerCase(Locale.ENGLISH))) {
					if(ElementKeyMap.getInfo(c).getName().toLowerCase(Locale.ENGLISH).equals(type.trim().toLowerCase(Locale.ENGLISH))) {
						//exact match found
						names.clear();
						names.add(c);
						break;
					} else {
						names.add(c);
					}
				}

			}

			if(names.size() == 1) {

				PlayerState playerFromName = state.getPlayerFromName(name);

				int slot = playerFromName.getInventory().incExistingOrNextFreeSlot(names.get(0), count);
				playerFromName.sendInventoryModification(slot, Long.MIN_VALUE);
			} else if(names.isEmpty()) {
				client.serverMessage("[ADMIN COMMAND] [ERROR] no element starts with the string: \"" + type + "\"");
			} else {
				Iterator<Short> iterator = names.iterator();

				while(iterator.hasNext()) {
					short next = iterator.next();
					client.serverMessage("[ADMIN COMMAND] [ERROR] ambigous string: \"" + type + "\": " + "(" + ElementKeyMap.getInfo(next).getName() + " [" + next + "])" + (iterator.hasNext() ? ", " : ""));
				}
				client.serverMessage("[ADMIN COMMAND] [ERROR] use either the classified name or the one in the parenthesis");
			}

		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(IndexOutOfBoundsException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] Too many arguments");
		} catch(ElementClassNotFoundException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] Unknown Element" + type);
		}

	}

	private void giveCredits(GameServerState state) throws IOException {

		try {
			String name = (String) commandParams[0];
			Long count = (Long) commandParams[1];
			PlayerState playerFromName = state.getPlayerFromName(name);
			playerFromName.modCreditsServer(count);
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}

	}

	private void giveInfiniteVolume(GameServerState state) throws IOException {

		try {
			String name = (String) commandParams[0];
			boolean b = ((Boolean) commandParams[1]).booleanValue();
			PlayerState playerFromName = state.getPlayerFromName(name);
			playerFromName.setInfiniteInventoryVolume(b);
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}

	}

	private void giveId(GameServerState state) throws IOException {
		String name = (String) commandParams[0];
		short type = ((Integer) commandParams[1]).shortValue();
		Integer count = (Integer) commandParams[2];

		try {
			if(ElementKeyMap.isValidType(type)) {
				PlayerState playerFromName = state.getPlayerFromName(name);

				int slot = playerFromName.getInventory().incExistingOrNextFreeSlot(type, count);
				playerFromName.sendInventoryModification(slot, Long.MIN_VALUE);
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] invalid type " + type);
			}
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(IndexOutOfBoundsException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] Unknown Element" + type);
		} catch(ElementClassNotFoundException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] Unknown Element" + type);
		}

	}

	private void putIntoInventory(GameServerState state) throws IOException {
		String UID = (String) commandParams[0];
		int x = (Integer) commandParams[1];
		int y = (Integer) commandParams[2];
		int z = (Integer) commandParams[3];

		int id = (Integer) commandParams[4];
		int count = (Integer) commandParams[5];

		if(!ElementKeyMap.isValidType(id)) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] Block ID invalid");
			return;
		}

		Sendable sendable = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(UID);
		if(sendable instanceof ManagedSegmentController<?>) {
			ManagedSegmentController<?> s = (ManagedSegmentController<?>) sendable;
			Inventory inventory = s.getManagerContainer().getInventory(new Vector3i(x, y, z));
			if(inventory != null) {
				if(count < 0) {
					IntOpenHashSet mod = new IntOpenHashSet();
					inventory.decreaseBatch((short) id, count, mod);
					inventory.sendInventoryModification(mod);
					client.serverMessage("[ADMIN COMMAND] [SUCCESS] Removed " + Math.abs(count) + " of " + ElementKeyMap.toString(id) + " from " + s + "; inventory " + inventory + "; affected slots " + mod);
				} else {
					if(!inventory.canPutIn((short) id, count)) {
						client.serverMessage("[ADMIN COMMAND] [ERROR] Can't put amount of that ID into inventory");
					} else {
						int slot = inventory.incExistingOrNextFreeSlot((short) id, count, -1);
						inventory.sendInventoryModification(slot);
						client.serverMessage("[ADMIN COMMAND] [SUCCESS] Put " + count + " of " + ElementKeyMap.toString(id) + " into " + s + "; inventory " + inventory + "; slot " + slot);
					}
				}
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] No inventory found at " + (new Vector3i(x, y, z)) + ". (hold RShift to check block coordinates of looked at block)");
			}

		}else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] No Entity found for UID '"+UID+"'");
		}

	}

	private void readFromInventory(GameServerState state) throws IOException {
		String UID = (String) commandParams[0];
		int x = (Integer) commandParams[1];
		int y = (Integer) commandParams[2];
		int z = (Integer) commandParams[3];


		Sendable sendable = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(UID);
		if(sendable instanceof ManagedSegmentController<?>) {
			ManagedSegmentController<?> s = (ManagedSegmentController<?>) sendable;
			Inventory inventory = s.getManagerContainer().getInventory(new Vector3i(x, y, z));
			if(inventory != null) {
				String ts = s.toString() + "[" + new Vector3i(x, y, z) + "]";
				client.serverMessage("[ADMIN COMMAND] [SUCCESS] Listing entity " + ts + " inventory START");

				for(int e : inventory.getSlots()) {
					InventorySlot slot = inventory.getSlot(e);
					if(slot != null) {
						printSlot(ts, 0, slot);
					}
				}
				client.serverMessage("[ADMIN COMMAND] [SUCCESS] Listing entity " + ts + " inventory END.");
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] No inventory found at " + (new Vector3i(x, y, z)) + ". (hold RShift to check block coordinates of looked at block)");
			}

		}else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] No Entity found for UID '"+UID+"'");
		}

	}

	private void giveLaserWeapon(GameServerState state, boolean overpowered) throws IOException {
		String name = (String) commandParams[0];

		try {
			LaserWeapon laser = (LaserWeapon) MetaObjectManager.instantiate(MetaObjectType.WEAPON, WeaponSubType.LASER.type, true);

			if(overpowered) {
				laser.damage = 100000000;
			}
			PlayerState playerFromName = state.getPlayerFromName(name);

			int slot = playerFromName.getInventory().getFreeSlot();
			playerFromName.getInventory().put(slot, laser);

			playerFromName.sendInventoryModification(slot, Long.MIN_VALUE);
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(NoSlotFreeException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}

	}

	private void giveHealWeapon(GameServerState state) throws IOException {
		String name = (String) commandParams[0];

		try {
			MetaObject healer = MetaObjectManager.instantiate(MetaObjectType.WEAPON, WeaponSubType.HEAL.type, true);
			PlayerState playerFromName = state.getPlayerFromName(name);

			int slot = playerFromName.getInventory().getFreeSlot();
			playerFromName.getInventory().put(slot, healer);

			playerFromName.sendInventoryModification(slot, Long.MIN_VALUE);
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(NoSlotFreeException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}

	}

	private void giveMarkerWeapon(GameServerState state) throws IOException {
		String name = (String) commandParams[0];

		try {
			MetaObject marker = MetaObjectManager.instantiate(MetaObjectType.WEAPON, WeaponSubType.MARKER.type, true);
			PlayerState playerFromName = state.getPlayerFromName(name);

			int slot = playerFromName.getInventory().getFreeSlot();
			playerFromName.getInventory().put(slot, marker);

			playerFromName.sendInventoryModification(slot, Long.MIN_VALUE);
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(NoSlotFreeException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}

	}

	private void giveTransporterMarkerWeapon(GameServerState state) throws IOException {
		String name = (String) commandParams[0];

		try {
			MetaObject marker = MetaObjectManager.instantiate(MetaObjectType.WEAPON, WeaponSubType.TRANSPORTER_MARKER.type, true);
			PlayerState playerFromName = state.getPlayerFromName(name);

			int slot = playerFromName.getInventory().getFreeSlot();
			playerFromName.getInventory().put(slot, marker);

			playerFromName.sendInventoryModification(slot, Long.MIN_VALUE);
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(NoSlotFreeException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}

	}

	private void giveSniperWeapon(GameServerState state, boolean op) throws IOException {
		String name = (String) commandParams[0];

		try {
			SniperRifle sniper = (SniperRifle) MetaObjectManager.instantiate(MetaObjectType.WEAPON, WeaponSubType.SNIPER_RIFLE.type, true);
			PlayerState playerFromName = state.getPlayerFromName(name);
			if(op) {
				sniper.damage = 100000000;
				sniper.speed = 220;
				sniper.reload = 0.5f;
				sniper.distance = 2000;
			}
			int slot = playerFromName.getInventory().getFreeSlot();
			playerFromName.getInventory().put(slot, sniper);

			playerFromName.sendInventoryModification(slot, Long.MIN_VALUE);
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(NoSlotFreeException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}

	}

	private void giveGrappleWeapon(GameServerState state, boolean op) throws IOException {
		String name = (String) commandParams[0];

		try {
			GrappleBeam sniper = (GrappleBeam) MetaObjectManager.instantiate(MetaObjectType.WEAPON, WeaponSubType.GRAPPLE.type, true);
			PlayerState playerFromName = state.getPlayerFromName(name);
			if(op) {
				sniper.reload = 0.5f;
				sniper.distance = 2000;
			}
			int slot = playerFromName.getInventory().getFreeSlot();
			playerFromName.getInventory().put(slot, sniper);

			playerFromName.sendInventoryModification(slot, Long.MIN_VALUE);
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(NoSlotFreeException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}

	}

	private void giveTorchWeapon(GameServerState state, boolean op) throws IOException {
		String name = (String) commandParams[0];

		try {
			TorchBeam sniper = (TorchBeam) MetaObjectManager.instantiate(MetaObjectType.WEAPON, WeaponSubType.TORCH.type, true);
			PlayerState playerFromName = state.getPlayerFromName(name);
			if(op) {
				sniper.damage = 100000000;
				sniper.reload = 0.5f;
				sniper.distance = 2000;
			}
			int slot = playerFromName.getInventory().getFreeSlot();
			playerFromName.getInventory().put(slot, sniper);

			playerFromName.sendInventoryModification(slot, Long.MIN_VALUE);
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(NoSlotFreeException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}

	}

	private void giveRocketLauncherWeapon(GameServerState state, int overpowered) throws IOException {
		String name = (String) commandParams[0];

		try {
			RocketLauncherWeapon missileLauncher = (RocketLauncherWeapon) MetaObjectManager.instantiate(MetaObjectType.WEAPON, WeaponSubType.ROCKET_LAUNCHER.type, true);
			PlayerState playerFromName = state.getPlayerFromName(name);
			if(overpowered == 1) {
				missileLauncher.damage = 100000;
				missileLauncher.speed = 220;
				missileLauncher.reload = 1000;
				missileLauncher.distance = 2000;
			} else if(overpowered == 2) {
				missileLauncher.damage = 100000000;
				missileLauncher.speed = 220;
				missileLauncher.reload = 1000;
				missileLauncher.distance = 2000;
			}
			int slot = playerFromName.getInventory().getFreeSlot();
			playerFromName.getInventory().put(slot, missileLauncher);

			playerFromName.sendInventoryModification(slot, Long.MIN_VALUE);
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(NoSlotFreeException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}

	}

	private void givePowerSupplyWeapon(GameServerState state) throws IOException {
		String name = (String) commandParams[0];

		try {
			MetaObject logbook = MetaObjectManager.instantiate(MetaObjectType.WEAPON, WeaponSubType.POWER_SUPPLY.type, true);
			PlayerState playerFromName = state.getPlayerFromName(name);

			int slot = playerFromName.getInventory().getFreeSlot();
			playerFromName.getInventory().put(slot, logbook);

			playerFromName.sendInventoryModification(slot, Long.MIN_VALUE);
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(NoSlotFreeException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}

	}

	private void giveMetaItem(GameServerState state) throws IOException {
		String name = (String) commandParams[0];
		String type = (String) commandParams[1];

		try {
			MetaObject m = null;

			type = type.trim().toLowerCase(Locale.ENGLISH);

			for(MetaObjectType mt : MetaObjectType.values()) {
				if(mt.name().toLowerCase(Locale.ENGLISH).equals(type)) {

					m = MetaObjectManager.instantiate(mt, (short) -1, true);
				}
			}
			if(m == null) {
				MetaObjectType mt = MetaObjectType.WEAPON;

				for(WeaponSubType st : WeaponSubType.values()) {
					if(st.name().toLowerCase(Locale.ENGLISH).equals(type)) {

						m = MetaObjectManager.instantiate(mt, st.type, true);
					}
				}
			}


			if(m != null) {
				PlayerState playerFromName = state.getPlayerFromName(name);

				int slot = playerFromName.getInventory().getFreeSlot();
				playerFromName.getInventory().put(slot, m);

				playerFromName.sendInventoryModification(slot, Long.MIN_VALUE);
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] meta item type not known");
			}
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(NoSlotFreeException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}

	}


	private void godMode(GameServerState state) throws IOException {
		String name = (String) commandParams[0];
		Boolean b = (Boolean) commandParams[1];

		try {
			PlayerState p = state.getPlayerFromName(name);
			p.setGodMode(b);
			if(b) {
				client.serverMessage("[ADMIN COMMAND] activated godmode for " + name);
			} else {
				client.serverMessage("[ADMIN COMMAND] deactivated godmode for " + name);
			}
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}

	}

	private void creativeMode(GameServerState state) throws IOException {
		String name = (String) commandParams[0];
		Boolean b = (Boolean) commandParams[1];

		try {
			PlayerState p = state.getPlayerFromName(name);
			p.setHasCreativeMode(b);
			if(b) {
				client.serverMessage("[ADMIN COMMAND] activated creative mode for " + name);
			} else {
				client.serverMessage("[ADMIN COMMAND] deactivated creative mode for " + name);
			}
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}

	}

	private void importSectorBulk(GameServerState state) {
		String name = (String) commandParams[0];
		state.scheduleSectorBulkImport(client, name);
	}

	private void importSectorNorm(GameServerState state) {
		Vector3i sec = new Vector3i((Integer) commandParams[0], (Integer) commandParams[1], (Integer) commandParams[2]);
		String name = (String) commandParams[3];
		state.scheduleSectorImport(sec, client, name);
	}

	private void delayAutosave(GameServerState state) {
		int sec = (Integer) commandParams[0];
		state.delayAutosave = System.currentTimeMillis() + sec * 1000;
	}

	private void initiateWave(GameServerState state, Int2ObjectOpenHashMap<Sendable> localObjects, int amountOfShips) throws IOException {
		int level = (Integer) commandParams[0];
		int time = (Integer) commandParams[1];
		int factionId = (Integer) commandParams[2];
		PlayerState playerFromName;

		try {
			playerFromName = state.getPlayerFromStateId(client.getId());
			SimpleTransformableSendableObject firstControlledTransformable = playerFromName.getFirstControlledTransformable();

			state.getController().initiateWave(amountOfShips, factionId, level, time, BluePrintController.active, playerFromName.getCurrentSector());

		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(PlayerControlledTransformableNotFound e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(EntityNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(EntityAlreadyExistsException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}
	}

	private void invisibilityMode(GameServerState state) throws IOException {
		String name = (String) commandParams[0];
		Boolean b = (Boolean) commandParams[1];

		try {
			PlayerState p = state.getPlayerFromName(name);
			p.setInvisibilityMode((b));
			if(b) {
				client.serverMessage("[ADMIN COMMAND] activated invisibility for " + name);
			} else {
				client.serverMessage("[ADMIN COMMAND] deactivated invisibility for " + name);
			}
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}

	}

	private void sectorSize(GameServerState state) throws IOException {
		int size = (Integer) commandParams[0];
		ServerConfig.SECTOR_SIZE.setInt(size);
		state.getGameState().setSectorSize(size);
		client.serverMessage("[ADMIN COMMAND] set sector size to " + size);
		ServerConfig.write();
	}

	private void setJumpGateDest(GameServerState state) throws IOException {
		String uidDest = (String) commandParams[0];
		PlayerState playerFromName;
		try {
			playerFromName = state.getPlayerFromStateId(client.getId());

			Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get((int) playerFromName.getNetworkObject().selectedEntityId.get());

			if(sendable != null && sendable instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) sendable).getManagerContainer() instanceof StationaryManagerContainer<?>) {
				StationaryManagerContainer<?> c = (StationaryManagerContainer<?>) ((ManagedSegmentController<?>) sendable).getManagerContainer();
				ManagerModuleCollection<WarpgateUnit, WarpgateCollectionManager, WarpgateElementManager> warpgate = c.getWarpgate();
				for(int i = 0; i < warpgate.getCollectionManagers().size(); i++) {
					warpgate.getCollectionManagers().get(i).setDestination(uidDest, new Vector3i());
				}
			}

		} catch(PlayerNotFountException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void stopFleetDebug(GameServerState state) throws IOException {
		PlayerState playerFromName;
		try {
			playerFromName = state.getPlayerFromStateId(client.getId());

			Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get((int) playerFromName.getNetworkObject().selectedEntityId.get());

			if(sendable != null && sendable instanceof Ship) {
				Ship ship = (Ship) sendable;

				if(ship.getFleet() == null) {
					client.serverMessage("[ADMIN COMMAND] [ERROR] ship " + ship + " not in a fleet");
				} else {
					for(FleetMember s : ship.getFleet().getMembers()) {
						if(!s.isLoaded()) {
							client.serverMessage("[ADMIN COMMAND] [ERROR] All ships in fleet must be loaded. (found one not loaded: " + s.UID + ")");
							return;
						}
					}
					ship.getFleet().debugStop();
					client.serverMessage("[ADMIN COMMAND] Success: Debugging Stopped for fleet");
				}

			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] must select a fleet ship");
			}

		} catch(PlayerNotFountException e) {
			e.printStackTrace();
		}
	}

	private void sendFleetDebug(GameServerState state) throws IOException {
		int sX = (Integer) commandParams[0];
		int sY = (Integer) commandParams[1];
		int sZ = (Integer) commandParams[2];
		PlayerState playerFromName;
		try {
			playerFromName = state.getPlayerFromStateId(client.getId());

			Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get((int) playerFromName.getNetworkObject().selectedEntityId.get());

			if(sendable != null && sendable instanceof Ship) {
				Ship ship = (Ship) sendable;

				if(ship.getFleet() == null) {
					client.serverMessage("[ADMIN COMMAND] [ERROR] ship " + ship + " not in a fleet");
				} else {
					ship.getFleet().debugMoveBetween(playerFromName.getCurrentSector(), new Vector3i(sX, sY, sZ));
					client.serverMessage("[ADMIN COMMAND] Success: Debugging started for fleet");
				}

			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] must select a fleet ship");
			}

		} catch(PlayerNotFountException e) {
			e.printStackTrace();
		}
	}

	private void joinFaction(GameServerState state) throws IOException {
		String name = (String) commandParams[0];
		int fid = (Integer) commandParams[1];

		try {
			PlayerState playerFromName = state.getPlayerFromName(name);
			client.serverMessage("[ADMIN COMMAND] joining factionID of " + name + " to " + fid);
			playerFromName.getFactionController().forceJoinOnServer(fid);
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] player not found for your client");
		}
	}

	public ClosestRayResultCallback getPlayerBlock(GameServerState state) throws IOException, PlayerNotFountException, PlayerControlledTransformableNotFound {
		PlayerState playerFromName;
		Vector3f pos;
		Vector3f to;
		playerFromName = state.getPlayerFromStateId(client.getId());
		SimpleTransformableSendableObject firstControlledTransformable = playerFromName.getFirstControlledTransformable();
		if(playerFromName.getNetworkObject().isInBuildMode.getBoolean()) {
			pos = new Vector3f(playerFromName.getBuildModePosition().getWorldTransform().origin);
			Vector3f forw = GlUtil.getForwardVector(new Vector3f(), playerFromName.getBuildModePosition().getWorldTransform());
			forw.scale(5000);
			to = new Vector3f(pos);
			to.add(forw);
		} else {


			if(firstControlledTransformable instanceof AbstractCharacter<?>) {
				pos = new Vector3f(((AbstractCharacter<?>) firstControlledTransformable).getHeadWorldTransform().origin);
			} else {
				pos = new Vector3f(firstControlledTransformable.getWorldTransform().origin);

			}
			to = new Vector3f(pos);
			Vector3f forw = playerFromName.getForward(new Vector3f());
			forw.scale(5000);
			to.add(forw);

		}
		Sector sector = state.getUniverse().getSector(firstControlledTransformable.getSectorId());
		ClosestRayResultCallback testRayCollisionPoint = ((PhysicsExt) sector.getPhysics()).testRayCollisionPoint(pos, to, false, null, null, false, true, false);
		return testRayCollisionPoint;
	}

	private void jump(GameServerState state) throws IOException {
		try {
			PlayerState playerFromName;

			playerFromName = state.getPlayerFromStateId(client.getId());
			SimpleTransformableSendableObject firstControlledTransformable = playerFromName.getFirstControlledTransformable();
			ClosestRayResultCallback testRayCollisionPoint = getPlayerBlock(state);
			if(testRayCollisionPoint.hasHit()) {
				Vector3f h = new Vector3f(testRayCollisionPoint.hitPointWorld);
				h.sub(playerFromName.getForward(new Vector3f()));
				warpTransformable(firstControlledTransformable, h.x, h.y, h.z);
				client.serverMessage("[ADMIN COMMAND] Object successfully jumped to " + h);
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] no object in line of sight");
			}

		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(PlayerControlledTransformableNotFound e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}

	}

	private void kick(GameServerState state, boolean reason, int reasonIndex) throws IOException {
		kick(state, reason, reasonIndex, (String) commandParams[0]);
	}

	private void kick(GameServerState state, boolean reason, int reasonIndex, String player) throws IOException {
		if(reason) {
			kick(state, (String) commandParams[reasonIndex], player);
		} else {
			kick(state, "You have been kicked by an admin", player);
		}
	}

	private void kick(GameServerState state, String reason) throws IOException {
		kick(state, reason, (String) commandParams[0]);
	}

	private void kick(GameServerState state, String reason, String player) throws IOException {
		try {
			PlayerState playerFromName = state.getPlayerFromName(player);
			state.getController().sendLogout(playerFromName.getClientId(), reason);
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}

	}

	private void resetIntegrityTimeout(GameServerState state) throws IOException {
		SegmentController s = getSelectedOrEnteredStructure(state);
		if(s instanceof ManagedSegmentController<?>) {
			((ManagedSegmentController<?>) s).getManagerContainer().resetIntegrityDelay();
			client.serverMessage("[ADMIN COMMAND] [SUCCESS] reset integrity timer for " + s);
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] no selected or entered entity");
		}
	}

	private void resetRepairTimeout(GameServerState state) throws IOException {
		SegmentController s = getSelectedOrEnteredStructure(state);
		if(s instanceof ManagedSegmentController<?>) {
			((ManagedSegmentController<?>) s).getManagerContainer().resetRepairDelay();
			client.serverMessage("[ADMIN COMMAND] [SUCCESS] reset block repair timer for " + s);
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] no selected or entered entity");
		}
	}

	private void killCharacter(GameServerState state) throws IOException {
		String name = (String) commandParams[0];

		try {
			PlayerState playerFromName = state.getPlayerFromName(name);
			playerFromName.suicideOnServer();
			playerFromName.sendServerMessage(new ServerMessage(Lng.astr("YOU HAVE BEEN\nREMOTELY KILLED\nBY AN ADMIN!"), ServerMessage.MESSAGE_TYPE_ERROR, playerFromName.getId()));
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}

	}

	private void listAdmins(GameServerState state) throws IOException {
		String a = null;
		synchronized(state.getAdmins()) {
			a = state.getAdmins().toString();
		}
		client.serverMessage("Admins: " + a);

	}

	private void listBannedIps(GameServerState state) throws IOException {
		String a = null;
		synchronized(state.getBlackListedIps()) {
			a = state.getBlackListedIps().toString();
		}
		client.serverMessage("Banned: " + a);

	}

	private void listBannedNames(GameServerState state) throws IOException {
		String a = null;
		synchronized(state.getBlackListedNames()) {
			a = state.getBlackListedNames().toString();
		}
		client.serverMessage("Banned: " + a);

	}

	private void listBannedAccounts(GameServerState state) throws IOException {
		String a = null;
		synchronized(state.getBlackListedAccounts()) {
			a = state.getBlackListedAccounts().toString();
		}
		client.serverMessage("Banned: " + a);

	}

	private void listControlUnits(GameServerState state) throws IOException {

		try {
			synchronized(state.getLocalAndRemoteObjectContainer().getLocalObjects()) {

				for(Sendable s : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
					if(s instanceof PlayerControllable && ((PlayerControllable) s).getAttachedPlayers().size() > 0) {
						client.serverMessage(s + ": " + ((PlayerControllable) s).getAttachedPlayers());
					}
				}
				client.serverMessage("----BY CONTROLLABLE----");
				for(PlayerState p : state.getPlayerStatesByName().values()) {
					client.serverMessage(p.getControllerState().getUnits().toString());
				}
				client.serverMessage("----BY UNIT----");
			}
		} catch(Exception e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR]: " + e.getClass() + ": " + e.getMessage() + ". ");
		}
	}

	private void getPlayerSpawn(GameServerState state) throws IOException {
		String name = ((String) commandParams[0]);
		try {
			PlayerState player = state.getPlayerFromName(name);
			PlayerStateSpawnData spawn = player.getSpawn();

			if(!spawn.deathSpawn.UID.equals("")) {
				client.serverMessage("[ADMINCOMMAND][SPAWN][SUCCESS] " + player + " spawn currently relative; UID: " + spawn.deathSpawn.UID + "; local position: " + spawn.deathSpawn.localPos);
			} else {
				client.serverMessage("[ADMINCOMMAND][SPAWN][SUCCESS] " + player + " spawn currently absolute; sector: " + spawn.deathSpawn.absoluteSector + "; local position: " + spawn.deathSpawn.localPos);
			}
		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMINCOMMAND][SPAWN] Player not found");
		}
	}

	private void setPlayerSpawnTo(GameServerState state) throws IOException {
		String name = ((String) commandParams[0]);
		Vector3i sector = new Vector3i((Integer) commandParams[1], (Integer) commandParams[2], (Integer) commandParams[3]);
		Vector3f local = new Vector3f((Float) commandParams[4], (Float) commandParams[5], (Float) commandParams[6]);
		try {
			PlayerState player = state.getPlayerFromName(name);
			PlayerStateSpawnData spawn = player.getSpawn();
			spawn.deathSpawn.UID = "";
			spawn.deathSpawn.absoluteSector.set(sector);
			spawn.deathSpawn.localPos.set(local);
			spawn.deathSpawn.gravityAcceleration.set(0, 0, 0);

			client.serverMessage("[ADMINCOMMAND][SPAWN][SUCCESS] set spawn of player " + player + " to sector " + sector + "; local position: " + local);
		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMINCOMMAND][SPAWN] Player not found");
		}
	}

	private void listFactions(GameServerState state) throws IOException {
		Int2ObjectOpenHashMap<Faction> factionMap = state.getGameState().getFactionManager().getFactionMap();
		client.serverMessage("FACTION_LIST START");
		List<Vector3i> systems = new ObjectArrayList<Vector3i>();
		for(Faction f : factionMap.values()) {

			state.getDatabaseIndex().getTableManager().getSystemTable().getSystemsByFaction(f.getIdFaction(), systems);
			client.serverMessage("FACTION: " + f + "; HomeBaseName: " + f.getHomebaseRealName() + "; HomeBaseUID: " + f.getHomebaseUID() + "; HomeBaseLocation: " + f.getHomeSector() + "; Owned: " + systems);
			systems.clear();
		}
		client.serverMessage("FACTION_LIST END");
	}

	private void listBlueprints(GameServerState state, boolean filterByOwner, boolean vorbose) throws IOException {
		String owner = null;

		if(filterByOwner) {
			owner = ((String) commandParams[0]).toLowerCase(Locale.ENGLISH);
		}

		Collection<CatalogPermission> catalog = state.getCatalogManager().getCatalog();
		client.serverMessage("[CATALOG] START");
		int i = 0;
		for(CatalogPermission p : catalog) {
			if(filterByOwner && !owner.equals(p.ownerUID.toLowerCase(Locale.ENGLISH))) {
				continue;
			}
			if(vorbose) {
				BlueprintEntry blueprint;
				try {
					blueprint = BluePrintController.active.getBlueprint(p.getUid());
					BoundingBox total = new BoundingBox();
					blueprint.calculateTotalBb(total);
					client.serverMessage("[CATALOG] INDEX " + i + ": " + p.getUid() + "; Owner: " + p.ownerUID + "; Type: " + p.type.name() + "; Mass: " + blueprint.getMass() + "; DimensionInclChields: " + total + "; BlocksInclChilds: " + blueprint.getElementCountMapWithChilds().getTotalAmount());
				} catch(EntityNotFountException e) {
					e.printStackTrace();
					client.serverMessage("[CATALOG] INDEX " + i + ": " + p.getUid() + " ERROR. BLUEPRINT MISSING");
				}
			} else {
				client.serverMessage("[CATALOG] INDEX " + i + ": " + p.getUid());
			}
			i++;
		}
		client.serverMessage("[CATALOG] END");
	}

	private void listWhiteIps(GameServerState state) throws IOException {
		String a = null;
		synchronized(state.getWhiteListedIps()) {
			a = state.getWhiteListedIps().toString();
		}
		client.serverMessage("Whitelisted: " + a);

	}

	private void listWhiteAccounts(GameServerState state) throws IOException {
		String a = null;
		synchronized(state.getWhiteListedAccounts()) {
			a = state.getWhiteListedAccounts().toString();
		}
		client.serverMessage("Whitelisted: " + a);

	}

	private void listWhiteNames(GameServerState state) throws IOException {
		String a = null;
		synchronized(state.getWhiteListedNames()) {
			a = state.getWhiteListedNames().toString();
		}
		client.serverMessage("Whitelisted: " + a);

	}

	// private void endRound(GameServerState state) throws IOException {
	// String name = (String) commandParams[0];
	// state.getController().endRound(0, 0, null);
	// if(name != null && (name.toLowerCase(Locale.ENGLISH).equals("green") ||
	// name.toLowerCase(Locale.ENGLISH).equals("blue")) ){
	// if(name.toLowerCase(Locale.ENGLISH).equals("green")){
	//
	// }else{
	// state.getController().endRound(state.getBlueTeam(), state.getGreenTeam(),
	// null);
	// }
	// }else{
	// client.serverMessage("[ADMIN COMMAND] [ERROR] please choose a winning team (either green or blue)");
	// }

	// }

	private void loadRange(GameServerState state) throws IOException {
		Vector3i from = new Vector3i((Integer) commandParams[0], (Integer) commandParams[1], (Integer) commandParams[2]);
		Vector3i to = new Vector3i((Integer) commandParams[3], (Integer) commandParams[4], (Integer) commandParams[5]);
		try {

			ObjectArrayFIFOQueue<Vector3i> queue = new ObjectArrayFIFOQueue<Vector3i>(VoidSystem.SYSTEM_SIZE * VoidSystem.SYSTEM_SIZE * VoidSystem.SYSTEM_SIZE);
			for(int z = from.z; z <= to.z; z++) {
				for(int y = from.y; y <= to.y; y++) {
					for(int x = from.x; x <= to.x; x++) {
						Vector3i pos = new Vector3i(x, y, z);

						queue.enqueue(pos);

					}
				}
			}
			state.toLoadSectorsQueue = queue;
		} catch(Exception e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] critical: " + e.getClass().getSimpleName() + ": " + e.getMessage());
		}

	}

	private void loadShip(GameServerState state, BluePrintController bbC, boolean docked, boolean withFaction) throws IOException {
		String catalogname = (String) commandParams[0];
		String name = (String) commandParams[1];
		int fid = 0;

		if(withFaction) {
			fid = (Integer) commandParams[2];
		}

		if(!EntityRequest.isShipNameValid(name)) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] Invalid Ship name (Only Characters And numbers and -_ allowed)");
			return;
		}

		Transform t = new Transform();
		t.setIdentity();
		try {
			PlayerState playerFromName = state.getPlayerFromStateId(client.getId());
			SimpleTransformableSendableObject firstControlledTransformable = playerFromName.getFirstControlledTransformable();

			t.origin.set(firstControlledTransformable.getWorldTransform().origin);
			ByteBuffer buffer = ByteBuffer.allocate(10 * 1024);


			if(bbC == BluePrintController.stationsTradingGuild) {
				fid = FactionManager.TRAIDING_GUILD_ID;
			}
			if(bbC == BluePrintController.stationsPirate) {
				fid = FactionManager.PIRATES_ID;
			}
			SegmentPiece toDockOn = null;
			if(docked) {
				try {
					ClosestRayResultCallback testRayCollisionPoint = getPlayerBlock(state);
					if(testRayCollisionPoint.hasHit() && testRayCollisionPoint instanceof CubeRayCastResult && ((CubeRayCastResult) testRayCollisionPoint).getSegment() != null) {
						CubeRayCastResult c = (CubeRayCastResult) testRayCollisionPoint;

						SegmentPiece p = new SegmentPiece(c.getSegment(), c.getCubePos());
						if(p.isValid() && p.getInfo().isRailDockable()) {
							toDockOn = p;
						} else {
							client.serverMessage("[ADMIN COMMAND] [ERROR] selected block is not dockable");
							return;
						}
					} else {
						client.serverMessage("[ADMIN COMMAND] [ERROR] no object in line of sight");
						return;
					}

				} catch(PlayerNotFountException e) {
					client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
				} catch(PlayerControlledTransformableNotFound e) {
					client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
				}
			}
			SegmentControllerOutline<?> loadBluePrint = bbC.loadBluePrint(state, catalogname, name, t, -1, fid, playerFromName.getCurrentSector(), "<admin>", buffer, toDockOn, false, new ChildStats(false));

			loadBluePrint.scrap = (bbC == BluePrintController.stationsNeutral);
			loadBluePrint.shop = (bbC == BluePrintController.stationsTradingGuild);

			synchronized(state.getBluePrintsToSpawn()) {
				state.getBluePrintsToSpawn().add(loadBluePrint);
			}
			System.err.println("[ADMIN] LOADING " + loadBluePrint.getClass());
		} catch(EntityNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(EntityAlreadyExistsException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] Entity already exists: " + e.getMessage());
		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(PlayerControlledTransformableNotFound e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}

	}

	private void loadSystem(GameServerState state) throws IOException {
		Vector3i sys = new Vector3i((Integer) commandParams[0], (Integer) commandParams[1], (Integer) commandParams[2]);
		try {

			ObjectArrayFIFOQueue<Vector3i> queue = new ObjectArrayFIFOQueue<Vector3i>(VoidSystem.SYSTEM_SIZE * VoidSystem.SYSTEM_SIZE * VoidSystem.SYSTEM_SIZE);
			for(int z = sys.z * VoidSystem.SYSTEM_SIZE; z < sys.z * VoidSystem.SYSTEM_SIZE + VoidSystem.SYSTEM_SIZE; z++) {
				for(int y = sys.y * VoidSystem.SYSTEM_SIZE; y < sys.y * VoidSystem.SYSTEM_SIZE + VoidSystem.SYSTEM_SIZE; y++) {
					for(int x = sys.x * VoidSystem.SYSTEM_SIZE; x < sys.x * VoidSystem.SYSTEM_SIZE + VoidSystem.SYSTEM_SIZE; x++) {
						Vector3i pos = new Vector3i(x, y, z);

						queue.enqueue(pos);

					}
				}
			}
			state.toLoadSectorsQueue = queue;
		} catch(Exception e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] critical: " + e.getClass().getSimpleName() + ": " + e.getMessage());
		}

	}

	private void populateSector(GameServerState state) throws IOException {
		Vector3i sec = new Vector3i((Integer) commandParams[0], (Integer) commandParams[1], (Integer) commandParams[2]);
		try {
			Sector sector = state.getUniverse().getSector(sec);

			if(sector != null) {
				sector.populate(state);
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] sector not found: " + sec + ": " + state.getUniverse().getSectorSet());
			}
		} catch(Exception e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] critical: " + e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}

	private void refreshServerMessage(GameServerState state) throws IOException {
		state.getGameState().readServerMessage();
	}

	private void removeAdmin(GameServerState state) throws IOException {
		String name = (String) commandParams[0];
		boolean removeAdmin = state.getController().removeAdmin(client.getClientName(), name);

		if(!removeAdmin) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] '" + name + "' not found in admin list (use /list_admins to check the name)");
		}

	}

	private void simulationInfo(GameServerState state) throws IOException {
		state.getSimulationManager().print(client);
	}

	private void simulationClearAll(GameServerState state) throws IOException {
		state.getSimulationManager().clearAll();
	}

	private void repairSector(GameServerState state) throws IOException {
		PlayerState playerFromName;
		try {
			playerFromName = state.getPlayerFromStateId(client.getId());

			Vector3i sec = new Vector3i((Integer) commandParams[0], (Integer) commandParams[1], (Integer) commandParams[2]);
			Sector sector = state.getUniverse().getSector(sec);
			if(sector != null) {
				sector.queueRepairRequest();
				client.serverMessage("[ADMIN COMMAND] [SUCCESS] sector repair queued: " + sec);
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] sector not found: " + sec + ": " + state.getUniverse().getSectorSet());
			}
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] player not found for your client");
		} catch(Exception e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] server could not load sector");
		}

	}

	private void resAABB(GameServerState state) {
		synchronized(state.getLocalAndRemoteObjectContainer().getLocalObjects()) {
			for(Sendable s : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
				if(s instanceof SegmentController) {
					((SegmentController) s).getSegmentBuffer().restructBB();
				}
			}
		}
	}

	private void restockUid(GameServerState state, boolean full) throws IOException {

		String uid = (String) commandParams[0];
		ShopInterface shop = null;

		synchronized(state.getLocalAndRemoteObjectContainer().getLocalObjects()) {
			Sendable sendable = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(uid);
			if(sendable != null) {
				if(sendable instanceof ShopSpaceStation) {
					shop = (ShopInterface) sendable;
				} else if(sendable instanceof SpaceStation) {
					shop = ((SpaceStation) sendable).getManagerContainer();
				}
			}
		}
		if(shop == null) {
			System.err.println("[ADMIN] Shop not online: " + uid + "; checking logged off");
			File playerFile = new FileExt(GameServerState.ENTITY_DATABASE_PATH + File.separator + uid + ".ent");

			Tag tag;
			try {
				BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(playerFile));
				tag = Tag.readFrom(bufferedInputStream, true, false);
				Tag tagStructure = null;

				if("ShopSpaceStation3".equals(tag.getName())) {
					shop = new ManagedShop(state);
					((ManagedShop) shop).initialize();
					((ManagedShop) shop).fromTagStructure(tag);
					shop.fillInventory(false, full);
					tagStructure = ((ManagedShop) shop).toTagStructure();
				} else if("ShopSpaceStation2".equals(tag.getName())) {
					shop = new ShopSpaceStation(state);
					((ShopSpaceStation) shop).initialize();
					((ShopSpaceStation) shop).fromTagStructure(tag);
					shop.fillInventory(false, full);
					tagStructure = ((ShopSpaceStation) shop).toTagStructure();
				} else if("SpaceStation".equals(tag.getName())) {
					SpaceStation station = new SpaceStation(state);
					station.initialize();
					station.fromTagStructure(tag);
					shop = station.getManagerContainer();
					shop.fillInventory(false, full);
					tagStructure = ((SpaceStationManagerContainer) shop).getSegmentController().toTagStructure();
				} else {
					System.err.println("[ADMIN] Tag type not found " + tag.getName());
				}

				if(tagStructure != null) {
					BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(playerFile));
					tagStructure.writeTo(bufferedOutputStream, true);

					client.serverMessage("[ADMIN COMMAND] [SUCCESS] Restocked: " + shop);
				}
			} catch(FileNotFoundException e1) {
				client.serverMessage("[ADMIN COMMAND] [ERROR] shop " + uid + " not online, and no offline save state found");
				e1.printStackTrace();
			} catch(IOException e1) {
				client.serverMessage("[ADMIN COMMAND] [ERROR] shop " + uid + " not online, and no offline save state reading failed");
				e1.printStackTrace();
			} catch(Exception e1) {
				client.serverMessage("[ADMIN COMMAND] [ERROR] shop " + uid + ", " + e1.getClass() + ": " + e1.getMessage());
				e1.printStackTrace();
			}
		} else {
			try {
				shop.fillInventory(true, full);
			} catch(NoSlotFreeException e) {
				e.printStackTrace();
				client.serverMessage("[ADMIN COMMAND] [ERROR] No more slots free " + e.getMessage());
			}
			client.serverMessage("[ADMIN COMMAND] [SUCCESS] Restocked: " + shop);
		}

	}

	private void restock(GameServerState state, boolean full) throws IOException {
		PlayerState playerFromName;
		try {

			playerFromName = state.getPlayerFromStateId(client.getId());

			Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get((int) playerFromName.getNetworkObject().selectedEntityId.get());
			ShopInterface shop = null;
			if(sendable != null) {
				if(sendable instanceof ShopInterface) {
					shop = (ShopInterface) sendable;
				} else if(sendable instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) sendable).getManagerContainer() instanceof ShopInterface) {
					shop = (ShopInterface) ((ManagedSegmentController<?>) sendable).getManagerContainer();
				}
			}
			if(shop != null) {
				shop.fillInventory(true, full);
				client.serverMessage("[ADMIN COMMAND] [SUCCESS] Restocked: " + sendable);
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] No Shop Selected: " + playerFromName.getNetworkObject().selectedEntityId.get() + "->(" + sendable + ")");
			}

		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(NoSlotFreeException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] No more slots free " + e.getMessage());
		}

	}

	private void shopSetInfinite(GameServerState state) throws IOException {
		PlayerState playerFromName;
		try {

			playerFromName = state.getPlayerFromStateId(client.getId());

			Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get((int) playerFromName.getNetworkObject().selectedEntityId.get());
			ShopInterface shop = null;
			if(sendable != null) {
				if(sendable instanceof ShopInterface) {
					shop = (ShopInterface) sendable;
				} else if(sendable instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) sendable).getManagerContainer() instanceof ShopInterface) {
					shop = (ShopInterface) ((ManagedSegmentController<?>) sendable).getManagerContainer();
				}
			}
			if(shop != null) {
				shop.getShoppingAddOn().setInfiniteSupply(!shop.getShoppingAddOn().isInfiniteSupply());
				client.serverMessage("[ADMIN COMMAND] [SUCCESS] Shop infinite flag set to: " + shop.getShoppingAddOn().isInfiniteSupply() + " on " + sendable);
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] No Shop Selected: " + playerFromName.getNetworkObject().selectedEntityId.get() + "->(" + sendable + ")");
			}

		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}

	}

	private void save(GameServerState state) {
		state.getController().triggerForcedSave();
	}

	private void executeGraphicsEffect(GameServerState state) throws IOException {
		byte effect = ((Integer) commandParams[0]).byteValue();
		PlayerState playerFromName;
		try {

			playerFromName = state.getPlayerFromStateId(client.getId());
			SimpleTransformableSendableObject firstControlledTransformable = playerFromName.getFirstControlledTransformable();

			if(firstControlledTransformable == null || !(firstControlledTransformable instanceof SegmentController)) {
				System.err.println("[ADMIN COMMAND]checking selected");
				Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(playerFromName.getNetworkObject().selectedEntityId.get());
				if(sendable != null && sendable instanceof SimpleTransformableSendableObject) {
					firstControlledTransformable = (SimpleTransformableSendableObject) sendable;
				}
			}
			firstControlledTransformable.executeGraphicalEffectServer(effect);

		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(PlayerControlledTransformableNotFound e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}

	}

	public SimpleTransformableSendableObject getSelfOrSelectedObject(GameServerState state) throws PlayerNotFountException {
		PlayerState playerFromName;
		playerFromName = state.getPlayerFromStateId(client.getId());
		SimpleTransformableSendableObject firstControlledTransformable = playerFromName.getFirstControlledTransformableWOExc();

		if(firstControlledTransformable == null || !(firstControlledTransformable instanceof SegmentController)) {
			System.err.println("[ADMIN COMMAND]checking selected");
			Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(playerFromName.getNetworkObject().selectedEntityId.get());
			if(sendable != null && sendable instanceof SimpleTransformableSendableObject) {
				firstControlledTransformable = (SimpleTransformableSendableObject) sendable;
			}
		}
		return firstControlledTransformable;
	}

	public SimpleTransformableSendableObject getSelectedObject(GameServerState state) throws IOException {
		PlayerState playerFromName;
		try {
			playerFromName = state.getPlayerFromStateId(client.getId());

			System.err.println("[ADMIN COMMAND]checking selected");
			Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(playerFromName.getNetworkObject().selectedEntityId.get());
			if(sendable != null && sendable instanceof SimpleTransformableSendableObject) {
				return (SimpleTransformableSendableObject) sendable;
			}
		} catch(PlayerNotFountException e) {
			e.printStackTrace();
		}
		client.serverMessage("[ADMIN COMMAND] [ERROR] Nothing selected");
		return null;
	}

	private void getEntityInfo(GameServerState state) throws IOException {
		SimpleTransformableSendableObject selectedObject = getSelectedObject(state);
		if(selectedObject != null) {
			client.serverMessage(selectedObject.getInfo());
			printInfo(state, selectedObject);
		}
	}

	private void saveShipAs(GameServerState state) throws IOException {
		String name = (String) commandParams[0];
		if(!EntityRequest.isShipNameValid(name) || name.length() > 48) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] Invalid Ship name (Only Characters And numbers and -_ allowed) (max 48)");
			return;
		}
		String cat = (String) commandParams[1];
		BlueprintClassification v = BlueprintClassification.valueOf(cat.trim().toUpperCase(Locale.ENGLISH));
		if(v == null) {

			client.serverMessage("[ADMIN COMMAND] [ERROR] Invalid classification (list in the catalog save dialog)");
			return;
		}
		saveShip(state, name, v);
	}


	private void blueprintInfo(GameServerState state) throws IOException {
		String name = (String) commandParams[0];

		String info = state.getCatalogManager().serverGetInfo(name);

		if(info != null) {
			client.serverMessage("[ADMIN COMMAND] [SUCCESS] Blueprint info on: " + name + "\n" + info);
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] blueprint not found (name is case sensitive): " + name);
		}
	}

	private void blueprintDelete(GameServerState state) throws IOException {
		String name = (String) commandParams[0];

		boolean serverDeletEntry = state.getCatalogManager().serverDeletEntry(name);

		if(serverDeletEntry) {
			client.serverMessage("[ADMIN COMMAND] [SUCCESS] Removing blueprint: " + name);
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] blueprint not found (name is case sensitive): " + name);
		}
	}

	private void blueprintSetOwner(GameServerState state) throws IOException {
		String name = (String) commandParams[0];
		String owner = (String) commandParams[1];

		boolean serverDeletEntry = state.getCatalogManager().serverChangeOwner(name, owner);

		if(serverDeletEntry) {
			client.serverMessage("[ADMIN COMMAND] [SUCCESS] Changing blueprint owner for " + name + " to " + owner);
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] blueprint not found (name is case sensitive): " + name);
		}
	}

	private void saveShip(GameServerState state) throws IOException {
		String name = (String) commandParams[0];
		if(!EntityRequest.isShipNameValid(name) || name.length() > 48) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] Invalid Ship name (Only Characters And numbers and -_ allowed) (max 48)");
			return;
		}
		saveShip(state, name, null);
	}

	private void saveShip(GameServerState state, String name, BlueprintClassification cat) throws IOException {

		PlayerState playerFromName;
		try {

			playerFromName = state.getPlayerFromStateId(client.getId());
			SimpleTransformableSendableObject firstControlledTransformable = playerFromName.getFirstControlledTransformable();

			if(firstControlledTransformable == null || !(firstControlledTransformable instanceof SegmentController)) {
				System.err.println("[ADMIN COMMAND]checking selected");
				Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(playerFromName.getNetworkObject().selectedEntityId.get());
				if(sendable != null && sendable instanceof SimpleTransformableSendableObject) {
					firstControlledTransformable = (SimpleTransformableSendableObject) sendable;
				}
			}

			if(firstControlledTransformable instanceof SegmentController) {
				SegmentController s = (SegmentController) firstControlledTransformable;
				s.writeAllBufferedSegmentsToDatabase(true, false, false);

				BluePrintController.active.writeBluePrint(s, name, false, cat != null ? cat : s.getType().getDefaultClassification());

				boolean writeEntryAdmin = state.getCatalogManager().writeEntryAdmin(s, name, playerFromName.getName(), s.getType().getDefaultClassification(), true);
				if(writeEntryAdmin) {
					client.serverMessage("[ADMIN COMMAND] successfully saved ship in catalog as \"" + name + "\"\n");
				} else {
					client.serverMessage("[ADMIN COMMAND] [ERROR] FAILED saving ship in catalog as \"" + name + "\"\n");
				}
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR]  not inside or selected any entity");
			}
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(PlayerControlledTransformableNotFound e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}

	}

	public SegmentController getSelectedOrEnteredStructure(GameServerState state) {
		return getSelectedOrEnteredStructure(state, client);
	}

	public static SegmentController getSelectedOrEnteredStructure(GameServerState state, RegisteredClientInterface client) {
		PlayerState playerFromName;
		try {
			playerFromName = state.getPlayerFromStateId(client.getId());
			SimpleTransformableSendableObject ent = null;

			Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(playerFromName.getNetworkObject().selectedEntityId.get());

			if(sendable != null && sendable instanceof SimpleTransformableSendableObject) {
				ent = (SimpleTransformableSendableObject) sendable;
			}
			if(ent == null || !(ent instanceof SegmentController)) {

				ent = playerFromName.getFirstControlledTransformable();
			}
			if(ent instanceof SegmentController) {
				SegmentController s = (SegmentController) ent;
				return s;
			} else {
			}
		} catch(PlayerNotFountException e) {
		} catch(PlayerControlledTransformableNotFound e) {
		}
		return null;
	}

	private void resetDock(GameServerState state, boolean all) throws IOException {

		PlayerState playerFromName;
		try {

			playerFromName = state.getPlayerFromStateId(client.getId());
			SimpleTransformableSendableObject c = playerFromName.getFirstControlledTransformable();

			if(c == null || !(c instanceof SegmentController)) {
				System.err.println("[ADMIN COMMAND]checking selected");
				Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(playerFromName.getNetworkObject().selectedEntityId.get());
				if(sendable != null && sendable instanceof SimpleTransformableSendableObject) {
					c = (SimpleTransformableSendableObject) sendable;
				}
			}

			if(c instanceof SegmentController) {
				if(all) {
					((SegmentController) c).railController.resetRailAll();
				} else {
					((SegmentController) c).railController.resetRail();
				}
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] not inside or selected any entity");
			}
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(PlayerControlledTransformableNotFound e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}

	}

	private void saveShipUid(GameServerState state) throws IOException {
		String uid = (String) commandParams[0];
		String name = (String) commandParams[1];
		if(!EntityRequest.isShipNameValid(name) || name.length() > 48) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] Invalid Ship name (Only Characters And numbers and -_ allowed) (max 48)");
			return;
		}
		Sendable firstControlledTransformable = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(uid);

		if(firstControlledTransformable != null && firstControlledTransformable instanceof SegmentController) {
			SegmentController s = (SegmentController) firstControlledTransformable;
			s.writeAllBufferedSegmentsToDatabase(true, false, false);

			BluePrintController.active.writeBluePrint(s, name, false, s.getType().getDefaultClassification());

			boolean writeEntryAdmin = state.getCatalogManager().writeEntryAdmin(s, name, "ADMIN", s.getType().getDefaultClassification(), true);
			if(writeEntryAdmin) {
				client.serverMessage("[ADMIN COMMAND] successfully saved entity in catalog as \"" + name + "\"\n");
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] FAILED saving ship in catalog as \"" + name + "\"\n");
			}
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] uid " + uid + " not found or loaded");
		}

	}

	private void search(GameServerState state) throws IOException {
		String entity = ((String) commandParams[0]);
		List<SearchResult> results = Lists.newArrayList();
		for(Map.Entry<String, SegmentController> e : state.getSegmentControllersByName().entrySet()) {
			if(e.getValue().getRealName().toUpperCase(Locale.ENGLISH).contains(entity.toUpperCase(Locale.ENGLISH))) {
				if(e.getValue() instanceof Ship || e.getValue() instanceof SpaceStation) {
					results.add(new SearchResult(e.getValue().getRealName(), e.getValue().getUniqueIdentifier(), state.getUniverse().getSector((e.getValue()).getSectorId()).pos));
				}
			}
		}

		List<DatabaseEntry> byName;
		try {
			byName = state.getDatabaseIndex().getTableManager().getEntityTable().getByName("%" + DatabaseIndex.escape(entity) + "%", 20);

			FOUND_ENTRY:
			for(DatabaseEntry a : byName) {
				for(Map.Entry<String, SegmentController> e : state.getSegmentControllersByName().entrySet()) {
					if(e.getValue() instanceof Ship || e.getValue() instanceof SpaceStation) {
						if(e.getValue().getUniqueIdentifier().equals(a.uid)) {
							break FOUND_ENTRY;
						}
					}
				}
				// If no matching uid was found, code will reach here
				results.add(new SearchResult(a.realName, a.uid, a.sectorPos));
			}

			if(results.isEmpty()) {
				client.serverMessage("[ADMIN COMMAND] No matches found for '" + entity + "'");
			} else {
				for(SearchResult result : results) {
					client.serverMessage("FOUND: " + result.realName + " -> " + result.position);
				}
			}
		} catch(SQLException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] SQL EXCEPTION");
		}
	}

	private void setAllRelations(GameServerState state) {
		String mode = (String) commandParams[0];
		if(mode.toLowerCase(Locale.ENGLISH).trim().equals("ally")) {
			state.getFactionManager().setAllRelations(RType.FRIEND.code);
		} else if(mode.toLowerCase(Locale.ENGLISH).trim().equals("neutral")) {
			state.getFactionManager().setAllRelations(RType.NEUTRAL.code);
		} else if(mode.toLowerCase(Locale.ENGLISH).trim().equals("enemy")) {
			state.getFactionManager().setAllRelations(RType.ENEMY.code);
		}

	}

	private void setDebugMode(GameServerState state) throws IOException {
		PlayerState playerFromName;
		int mode = (Integer) commandParams[0];
		try {

			playerFromName = state.getPlayerFromStateId(client.getId());

			Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get((int) playerFromName.getNetworkObject().selectedEntityId.get());

			if(sendable != null && sendable instanceof SimpleTransformableSendableObject) {
				SimpleTransformableSendableObject s = (SimpleTransformableSendableObject) sendable;
				s.setDebugMode((byte) mode);
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] No Entity Selected: " + playerFromName.getNetworkObject().selectedEntityId.get() + "->(" + sendable + ")");
			}

		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}
	}

	private void unsuspendFaction(GameServerState state) throws IOException {
		String name = (String) commandParams[0];
		try {
			PlayerState playerFromName = state.getPlayerFromName(name);

			playerFromName.getFactionController().unsuspendFaction(state, playerFromName);
			client.serverMessage("[ADMIN COMMAND] unsuspended faction for " + playerFromName);
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] player not found for your client");
		}
	}

	private void suspendFaction(GameServerState state) throws IOException {
		String name = (String) commandParams[0];

		try {
			PlayerState playerFromName = state.getPlayerFromName(name);
			playerFromName.getFactionController().suspendFaction(state, playerFromName);
			client.serverMessage("[ADMIN COMMAND] suspended faction for " + playerFromName);

		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] player not found for your client");
		}
	}

	private void setFactionId(GameServerState state) throws IOException {
		String name = (String) commandParams[0];
		int fid = (Integer) commandParams[1];

		try {
			PlayerState playerFromName = state.getPlayerFromName(name);
			client.serverMessage("[ADMIN COMMAND] set factionID of " + name + " to " + fid);
			playerFromName.getFactionController().setFactionId(fid);
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] player not found for your client");
		}
	}

	private void setFactionIdEntity(GameServerState state, boolean uid) throws IOException {
		int faction;

		String uidStr = null;
		Sendable sendable;
		if(uid) {
			uidStr = (String) commandParams[0];
			faction = (Integer) commandParams[1];

			sendable = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(uid);

		} else {
			faction = (Integer) commandParams[0];

			sendable = getSelectedOrEnteredStructure(state);
		}

		if(sendable instanceof SimpleTransformableSendableObject) {
			((SimpleTransformableSendableObject) sendable).setFactionId(faction);

			client.serverMessage("[ADMIN COMMAND] SET FACTION ID: " + sendable + " -> " + faction);

		} else {
			client.serverMessage("[ERROR][ADMIN COMMAND] OBJECT TO SET FACTION ID FOR NOT FOUND");
		}
	}

	private void setGlobalSpawn(GameServerState state) throws IOException {
		Transform t = new Transform();
		t.setIdentity();

		PlayerState playerFromName;

		try {
			playerFromName = state.getPlayerFromStateId(client.getId());
			SimpleTransformableSendableObject firstControlledTransformable = playerFromName.getFirstControlledTransformable();

			t.set(firstControlledTransformable.getWorldTransform());

			ServerConfig.DEFAULT_SPAWN_SECTOR_X.setInt(playerFromName.getCurrentSector().x);
			ServerConfig.DEFAULT_SPAWN_SECTOR_Y.setInt(playerFromName.getCurrentSector().y);
			ServerConfig.DEFAULT_SPAWN_SECTOR_Z.setInt(playerFromName.getCurrentSector().z);

			ServerConfig.DEFAULT_SPAWN_POINT_X_1.setFloat(t.origin.x);
			ServerConfig.DEFAULT_SPAWN_POINT_Y_1.setFloat(t.origin.y);
			ServerConfig.DEFAULT_SPAWN_POINT_Z_1.setFloat(t.origin.z);

			ServerConfig.DEFAULT_SPAWN_POINT_X_2.setFloat(t.origin.x);
			ServerConfig.DEFAULT_SPAWN_POINT_Y_2.setFloat(t.origin.y);
			ServerConfig.DEFAULT_SPAWN_POINT_Z_2.setFloat(t.origin.z);

			ServerConfig.DEFAULT_SPAWN_POINT_X_3.setFloat(t.origin.x);
			ServerConfig.DEFAULT_SPAWN_POINT_Y_3.setFloat(t.origin.y);
			ServerConfig.DEFAULT_SPAWN_POINT_Z_3.setFloat(t.origin.z);

			ServerConfig.DEFAULT_SPAWN_POINT_X_4.setFloat(t.origin.x);
			ServerConfig.DEFAULT_SPAWN_POINT_Y_4.setFloat(t.origin.y);
			ServerConfig.DEFAULT_SPAWN_POINT_Z_4.setFloat(t.origin.z);

			ServerConfig.write();
			client.serverMessage("[ADMIN COMMAND] SET DEFAULT SPAWN TO Sector" + playerFromName.getCurrentSector() + " LocalPos" + t.origin);
		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(PlayerControlledTransformableNotFound e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}

	}

	private void setSimulationDelay(GameServerState state) throws IOException {
		int delay = (Integer) commandParams[0];
		if (delay >= 0) {
			ServerConfig.SIMULATION_SPAWN_DELAY.setInt(delay);
			client.serverMessage("[ADMIN COMMAND] [SUCCESS] simulation delay is now "
					+ delay + " secs");
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] time must be >= 0");
		}
	}

	private void setSpawn(GameServerState state) throws IOException {
		PlayerState playerFromName;
		try {
			playerFromName = state.getPlayerFromStateId(client.getId());
			playerFromName.spawnData.setDeathSpawnToPlayerPos();
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] player not found for your client");
			e.printStackTrace();
		}
	}

	private void setSpawnPlayer(GameServerState state) throws IOException {
		String name = (String) commandParams[0];

		try {
			PlayerState playerFromName = state.getPlayerFromName(name);
			playerFromName.spawnData.setDeathSpawnToPlayerPos();
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] player not found for your client");
			e.printStackTrace();
		}
	}

	private void showModifierAndSpawner(GameServerState state) throws IOException {
		PlayerState playerFromName;
		try {
			playerFromName = state.getPlayerFromStateId(client.getId());

			SimpleTransformableSendableObject firstControlledTransformable = null;

			Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(playerFromName.getNetworkObject().selectedEntityId.get());

			if(sendable != null && sendable instanceof SegmentController) {
				firstControlledTransformable = (SimpleTransformableSendableObject) sendable;
			} else {
				firstControlledTransformable = playerFromName.getFirstControlledTransformable();
			}

			if(firstControlledTransformable instanceof SegmentController) {
				String s = ((SegmentController) firstControlledTransformable).getSpawner();
				String l = ((SegmentController) firstControlledTransformable).getLastModifier();
				s = s != null ? (s.length() > 0 ? s : "unknown") : "unknown";
				l = l != null ? (l.length() > 0 ? l : "unknown") : "unknown";
				client.serverMessage("[ADMIN COMMAND] " + firstControlledTransformable.toNiceString() + " spawned by " + s + "; last modified by " + l);
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] you are not inside or selected a ship");
				return;
			}

		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(PlayerControlledTransformableNotFound e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}
	}

	private void shutdown(GameServerState state) {
		int seconds = (Integer) commandParams[0];

		state.addTimedShutdown(seconds);

	}

	private void spawnEntity(GameServerState state) throws IOException {
		String catalogname = (String) commandParams[0];
		String name = (String) commandParams[1];
		int sX = (Integer) commandParams[2];
		int sY = (Integer) commandParams[3];
		int sZ = (Integer) commandParams[4];
		int faction = (Integer) commandParams[5];
		boolean ai = (Boolean) commandParams[6];

		Transform t = new Transform();
		t.setIdentity();

		try {
			PlayerState playerFromName;
			playerFromName = state.getPlayerFromStateId(client.getId());
			SimpleTransformableSendableObject firstControlledTransformable = playerFromName.getFirstControlledTransformable();

			t.set(firstControlledTransformable.getWorldTransform());
		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [WARNING] " + e.getMessage() + " Assuming 0,0,0 as local position within sector");
			t.setIdentity();
		} catch(PlayerControlledTransformableNotFound e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [WARNING] " + e.getMessage() + " Assuming 0,0,0 as local position within sector");
			t.setIdentity();
		}
		try {
			SegmentPiece toDockOn = null;
			ByteBuffer buffer = ByteBuffer.allocate(10 * 1024);
			SegmentControllerOutline loadBluePrint = BluePrintController.active.loadBluePrint(state, catalogname, name, t, -1, faction, new Vector3i(sX, sY, sZ), "<admin>", buffer, toDockOn, ai, new ChildStats(false));
			synchronized(state.getBluePrintsToSpawn()) {
				state.getBluePrintsToSpawn().add(loadBluePrint);
			}

		} catch(EntityNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(EntityAlreadyExistsException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}
	}

	private void spawnEntityPos(GameServerState state) throws IOException {
		String catalogname = (String) commandParams[0];
		String name = (String) commandParams[1];
		int sX = (Integer) commandParams[2];
		int sY = (Integer) commandParams[3];
		int sZ = (Integer) commandParams[4];

		float x = (Float) commandParams[5];
		float y = (Float) commandParams[6];
		float z = (Float) commandParams[7];

		int faction = (Integer) commandParams[8];
		boolean ai = (Boolean) commandParams[9];

		Transform t = new Transform();
		t.setIdentity();

		t.origin.set(x, y, z);

		try {
			SegmentPiece toDockOn = null;
			ByteBuffer buffer = ByteBuffer.allocate(10 * 1024);
			SegmentControllerOutline loadBluePrint = BluePrintController.active.loadBluePrint(state, catalogname, name, t, -1, faction, new Vector3i(sX, sY, sZ), "<admin>", buffer, toDockOn, ai, new ChildStats(false));
			synchronized(state.getBluePrintsToSpawn()) {
				state.getBluePrintsToSpawn().add(loadBluePrint);
			}

		} catch(EntityNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(EntityAlreadyExistsException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}
	}

	private void spawnItem(GameServerState state) throws IOException {

		StringBuilder s = new StringBuilder();
		for(int i = 0; i < commandParams.length - 1; i++) {
			s.append((String) commandParams[i]);
			if(i < commandParams.length - 2) {
				s.append(" ");
			}
		}
		String type = s.toString();

		Integer count = (Integer) commandParams[commandParams.length - 1];

		try {

			ArrayList<Short> names = new ArrayList<Short>();
			for(Short c : ElementKeyMap.typeList()) {
				String elementName = ElementKeyMap.getInfo(c).getName().toLowerCase(Locale.ENGLISH);
				if(elementName.startsWith(type.trim().toLowerCase(Locale.ENGLISH))) {
					names.add(c);
					if(elementName.toLowerCase(Locale.ENGLISH).equals(type.toLowerCase(Locale.ENGLISH).trim())) {
						names.clear();
						names.add(c);
						break;
					}
				}

			}

			if(names.size() == 1) {

				PlayerState playerFromName = state.getPlayerFromStateId(client.getId());

				short itemType = names.get(0);

				// for(int i = 0; i < 10000; i++){
				Sector sector = state.getUniverse().getSector(playerFromName.getCurrentSectorId());
				Transform worldTransform = playerFromName.getFirstControlledTransformable().getWorldTransform();
				Vector3f pos = new Vector3f(worldTransform.origin);
				Vector3f forwardVector = playerFromName.getForward(new Vector3f());
				forwardVector.scale(2);
				pos.add(forwardVector);
				// pos.x += 10d*Math.random();
				// pos.y += 10*Math.random();
				// pos.z += 10*Math.random();

				sector.getRemoteSector().addItem(pos, itemType, -1, count);
				// }
				client.serverMessage("[ADMIN COMMAND] sucessfully spawned item at " + pos);
			} else if(names.isEmpty()) {
				client.serverMessage("[ADMIN COMMAND] [ERROR] no element starts with the string: \"" + type + "\"");
			} else {
				Iterator<Short> iterator = names.iterator();

				while(iterator.hasNext()) {
					short next = iterator.next();
					client.serverMessage("[ADMIN COMMAND] [ERROR] ambigous string: \"" + type + "\": " + "(" + ElementKeyMap.getInfo(next).getName() + " [" + next + "])" + (iterator.hasNext() ? ", " : ""));
				}
				client.serverMessage("[ADMIN COMMAND] [ERROR] use either the classified name or the one in the parenthesis");
			}

		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(IndexOutOfBoundsException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] Too many arguments");
		} catch(ElementClassNotFoundException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] Unknown Element" + type);
		} catch(PlayerControlledTransformableNotFound e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}

	}

	public void spawnMobs(GameServerState state) throws IOException {
		String catalogname = (String) commandParams[0];
		int faction = (Integer) commandParams[1];
		int count = (Integer) commandParams[2];
		System.err.println("Spawning " + count + " mobs of type: " + catalogname);
		Transform t = new Transform();
		t.setIdentity();

		PlayerState playerFromName;
		try {

			playerFromName = state.getPlayerFromStateId(client.getId());
			SimpleTransformableSendableObject firstControlledTransformable = playerFromName.getFirstControlledTransformable();

			t.set(firstControlledTransformable.getWorldTransform());

			state.spawnMobs(count, catalogname, playerFromName.getCurrentSector(), t, faction, BluePrintController.active);

		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(PlayerControlledTransformableNotFound e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(EntityNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(EntityAlreadyExistsException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}
	}

	private void spawnMobsLine(GameServerState state) throws IOException {
		String catalogname = (String) commandParams[0];
		int factionId = (Integer) commandParams[1];
		int count = (Integer) commandParams[2];
		PlayerState playerFromName;
		try {

			playerFromName = state.getPlayerFromStateId(client.getId());
			SimpleTransformableSendableObject firstControlledTransformable = playerFromName.getFirstControlledTransformable();
			Vector3f pos = new Vector3f(firstControlledTransformable.getWorldTransform().origin);
			Vector3f to = new Vector3f(pos);
			Vector3f forw = new Vector3f(playerFromName.getForward(new Vector3f()));
			forw.scale(5000 * Element.BLOCK_SIZE);
			to.add(forw);

			Transform t = new Transform();
			t.setIdentity();
			Sector sector = state.getUniverse().getSector(firstControlledTransformable.getSectorId());
			ClosestRayResultCallback testRayCollisionPoint = ((PhysicsExt) sector.getPhysics()).testRayCollisionPoint(pos, to, false, null, null, false, true, false);
			if(testRayCollisionPoint.hasHit()) {

				t.origin.set(testRayCollisionPoint.hitPointWorld);

				System.err.println("Spawning " + count + " mobs of type: " + catalogname + " at " + testRayCollisionPoint.hitPointWorld);

				state.spawnMobs(count, catalogname, playerFromName.getCurrentSector(), t, factionId, BluePrintController.active);
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] no object in line of sight");
			}
		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(PlayerControlledTransformableNotFound e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(EntityNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(EntityAlreadyExistsException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}
	}

	private void startShipAI(GameServerState state) throws IOException {
		PlayerState playerFromName;
		try {
			int factionId = (Integer) commandParams[0];
			playerFromName = state.getPlayerFromStateId(client.getId());

			SimpleTransformableSendableObject firstControlledTransformable = null;

			Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(playerFromName.getNetworkObject().selectedEntityId.get());

			if(sendable != null && sendable instanceof Ship) {
				firstControlledTransformable = (SimpleTransformableSendableObject) sendable;
			} else {
				firstControlledTransformable = playerFromName.getFirstControlledTransformable();
			}
			if(firstControlledTransformable instanceof SegmentController) {
				((SegmentController) firstControlledTransformable).railController.activateAllAIServer(true, true, true, true);
			}
			if(firstControlledTransformable instanceof Ship) {
				((Ship) firstControlledTransformable).setFactionId(factionId);
				((AIConfiguationElements<String>) ((Ship) firstControlledTransformable).getAiConfiguration().get(Types.TYPE)).setCurrentState("Ship", true);
				((AIConfiguationElements<Boolean>) ((Ship) firstControlledTransformable).getAiConfiguration().get(Types.ACTIVE)).setCurrentState(true, true);
				((Ship) firstControlledTransformable).getAiConfiguration().applyServerSettings();


				client.serverMessage("[ADMIN COMMAND] activated " + firstControlledTransformable + " with faction " + factionId);
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] you are not inside or selected a ship");
				return;
			}

		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(PlayerControlledTransformableNotFound e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}

	}

	private void status(GameServerState state) throws IOException {

		long totalMemory = (Runtime.getRuntime().totalMemory() / 1024) / 1024;
		long freeMemory = (Runtime.getRuntime().freeMemory() / 1024) / 1024;
		long takenMemory = totalMemory - freeMemory;

		client.serverMessage("PhysicsInMem: " + GameServerState.axisSweepsInMemory + "; Rep: " + state.getUniverse().getPhysicsRepository().size());
		client.serverMessage("Total queued NT Packages: " + ServerProcessor.totalPackagesQueued);
		client.serverMessage("Loaded !empty Segs / free: " + GameServerState.lastAllocatedSegmentData + " / " + GameServerState.lastFreeSegmentData);
		client.serverMessage("Loaded Objects: " + state.getLocalAndRemoteObjectContainer().getLocalObjects().size());
		// client.serverMessage("[SERVER] [STATUS] PlayerList: "+state.getClients());
		client.serverMessage("Players: " + state.getClients().size() + " / " + state.getMaxClients());
		client.serverMessage("Mem (MB)[free, taken, total]: [" + freeMemory + ", " + takenMemory + ", " + totalMemory + "]");
		client.serverMessage("---------SERVER STATUS---------");
	}

	private void stopShipAI(GameServerState state) throws IOException {
		PlayerState playerFromName;
		try {

			playerFromName = state.getPlayerFromStateId(client.getId());

			SimpleTransformableSendableObject firstControlledTransformable = null;

			Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(playerFromName.getNetworkObject().selectedEntityId.get());

			if(sendable != null && sendable instanceof Ship) {
				firstControlledTransformable = (SimpleTransformableSendableObject) sendable;
			} else {
				firstControlledTransformable = playerFromName.getFirstControlledTransformable();
			}
			if(firstControlledTransformable instanceof Ship) {
				((AIConfiguationElements<Boolean>) ((Ship) firstControlledTransformable).getAiConfiguration().get(Types.ACTIVE)).setCurrentState(false, true);
				((Ship) firstControlledTransformable).getAiConfiguration().applyServerSettings();
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] you are not inside or selected a ship");
				return;
			}

		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(PlayerControlledTransformableNotFound e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}

	}

	private void teleportSelfHome(GameServerState state) throws IOException {
		try {
			PlayerState playerFromName = state.getPlayerFromStateId(client.getId());
			warpTransformable(playerFromName.getFirstControlledTransformable(), 0, 0, 0);
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(PlayerControlledTransformableNotFound e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}

	}

	private void teleportSelfTo(GameServerState state) throws IOException {
		try {
			PlayerState playerFromName = state.getPlayerFromStateId(client.getId());

			warpTransformable(playerFromName.getFirstControlledTransformable(), (Float) commandParams[0], (Float) commandParams[1], (Float) commandParams[2]);
		} catch(PlayerControlledTransformableNotFound e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}

	}

	private void teleportUidTo(GameServerState state, Int2ObjectOpenHashMap<Sendable> localObjects) throws IOException {

		String uid = (String) commandParams[0];

		float x = (Float) commandParams[1];
		float y = (Float) commandParams[2];
		float z = (Float) commandParams[3];

		Sendable sendable = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(uid);

		if(sendable != null && sendable instanceof SimpleTransformableSendableObject) {

			SimpleTransformableSendableObject firstControlledTransformable = (SimpleTransformableSendableObject) sendable;

			warpTransformable(firstControlledTransformable, x, y, z);
			client.serverMessage("[ADMIN COMMAND] teleported " + uid + " to ");
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] UID not found or not loaded");
		}

	}

	private void teleportTo(GameServerState state, Int2ObjectOpenHashMap<Sendable> localObjects) throws IOException {
		String name = (String) commandParams[0];
		float x = (Float) commandParams[1];
		float y = (Float) commandParams[2];
		float z = (Float) commandParams[3];

		try {

			PlayerState playerFromName = state.getPlayerFromName(name);

			SimpleTransformableSendableObject firstControlledTransformable = playerFromName.getFirstControlledTransformable();

			warpTransformable(firstControlledTransformable, x, y, z);

			client.serverMessage("[ADMIN COMMAND] teleported " + name + " to ");
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] player not found for your client");
		} catch(PlayerControlledTransformableNotFound e) {
			e.printStackTrace();
		}

	}

	private void teleportSelectedTo(GameServerState state, Int2ObjectOpenHashMap<Sendable> localObjects) throws IOException {
		float x = (Float) commandParams[0];
		float y = (Float) commandParams[1];
		float z = (Float) commandParams[2];

		try {

			PlayerState playerFromName = state.getPlayerFromStateId(client.getId());

			SimpleTransformableSendableObject firstControlledTransformable = null;

			Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(playerFromName.getNetworkObject().selectedEntityId.get());

			if(sendable != null && sendable instanceof SimpleTransformableSendableObject) {

				warpTransformable((SimpleTransformableSendableObject) sendable, x, y, z);

			} else {
				client.serverMessage("[ERROR][ADMIN COMMAND] OBJECT TO WARP NOT FOUND");
			}

			client.serverMessage("[ADMIN COMMAND] teleported " + sendable + " to ");
		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] player not found for your client");
		}

	}

	private void tp(GameServerState state) throws IOException {
		PlayerState self;
		String name = (String) commandParams[0];

		try {
			PlayerState playerFromName = state.getPlayerFromName(name);
			self = state.getPlayerFromStateId(client.getId());

			SimpleTransformableSendableObject ft = self.getFirstControlledTransformable();
			Vector3f pos = new Vector3f(ft.getWorldTransform().origin);
			pos.x += 1;
			Vector3i sec = new Vector3i(self.getCurrentSector());
			Sector sector = state.getUniverse().getSector(sec);
			if(sector != null) {
				if(playerFromName.getCurrentSectorId() != sector.getId()) {
					for(ControllerStateUnit u : playerFromName.getControllerState().getUnits()) {
						if(u.playerControllable instanceof SimpleTransformableSendableObject) {
							state.getController().queueSectorSwitch(getControllerRoot((SimpleTransformableSendableObject) u.playerControllable), sector.pos, SectorSwitch.TRANS_JUMP, false, true, true);
						}
					}
				} else {
					client.serverMessage("[ADMIN COMMAND] not changing sector for object " + ft.getSectorId() + "/" + sector.getId());
				}
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] sector not found: " + sec + ": " + state.getUniverse().getSectorSet());
			}

		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] player not found for your client");
		} catch(Exception e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] server could not load sector");
		}

	}

	private void tpTo(GameServerState state) throws IOException {
		PlayerState self;
		String name = (String) commandParams[0];

		try {
			PlayerState playerFromName = state.getPlayerFromName(name);
			self = state.getPlayerFromStateId(client.getId());

			SimpleTransformableSendableObject ft = playerFromName.getFirstControlledTransformable();
			Vector3f pos = new Vector3f(ft.getWorldTransform().origin);
			pos.x += 1;
			Vector3i sec = new Vector3i(self.getCurrentSector());
			Sector sector = state.getUniverse().getSector(sec);
			if(sector != null) {
				if(playerFromName.getCurrentSectorId() != sector.getId()) {
					for(ControllerStateUnit u : self.getControllerState().getUnits()) {
						if(u.playerControllable instanceof SimpleTransformableSendableObject) {
							state.getController().queueSectorSwitch(getControllerRoot((SimpleTransformableSendableObject) u.playerControllable), playerFromName.getCurrentSector(), SectorSwitch.TRANS_JUMP, false, true, true);
						}
					}
				} else {
					client.serverMessage("[ADMIN COMMAND] not changing sector for object " + ft.getSectorId() + "/" + sector.getId());
				}
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] sector not found: " + sec + ": " + state.getUniverse().getSectorSet());
			}

		} catch(PlayerNotFountException e) {
			client.serverMessage("[ADMIN COMMAND] [ERROR] player not found for your client");
		} catch(Exception e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] server could not load sector");
		}

	}

	private void triggerSimulationPlanning(GameServerState state) {
		try {
			state.getSimulationManager().getPlanner().interrupt();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void unBanIp(GameServerState state) throws IOException {
		String ip = (String) commandParams[0];
		boolean removeBannedIp = state.getController().removeBannedIp(client.getClientName(), ip);
		if (removeBannedIp) {
			client.serverMessage("[ADMIN COMMAND] successfully unbanned: " + ip);
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] ip not found in blacklist: " + ip + " -> " + state.getBlackListedIps());
		}

	}

	private void unBanName(GameServerState state) throws IOException {
		String name = (String) commandParams[0];
		boolean removeBannedIp = state.getController().removeBannedName(client.getClientName(), name);
		if (removeBannedIp) {
			client.serverMessage("[ADMIN COMMAND] successfully unbanned: "
					+ name);
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] name not found in blacklist: " + name + " -> " + state.getBlackListedNames());
		}

	}

	private void updateAllShopPrices(GameServerState state) {
		GameServerState.updateAllShopPricesFlag = true;
	}

	private void warpTransformable(SimpleTransformableSendableObject obj, float x, float y, float z) {
		obj.warpTransformable(x, y, z, true, null);
	}

	private void whitelistIp(GameServerState state, boolean timed) throws IOException {
		String ip = (String) commandParams[0];
		long time = -1;
		if(timed) {
			time = System.currentTimeMillis() + ((Integer) commandParams[1]).longValue() * 60000L;
		}
		try {
			state.getController().addWitelistedIp(ip, time);
			client.serverMessage("[ADMIN COMMAND] successfully whitelisted: " + ip);
		} catch(NoIPException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] not an IP: " + ip);
		}

	}

	private void whitelistName(GameServerState state, boolean timed) throws IOException {
		String name = (String) commandParams[0];
		long time = -1;
		if(timed) {
			time = System.currentTimeMillis() + ((Integer) commandParams[1]).longValue() * 60000L;
		}
		state.getController().addWitelistedName(name, time);
		client.serverMessage("[ADMIN COMMAND] successfully whitelisted: " + name);
	}

	private void whitelistAccount(GameServerState state, boolean timed) throws IOException {
		long time = -1;
		if(timed) {
			time = System.currentTimeMillis() + ((Integer) commandParams[1]).longValue() * 60000L;
		}
		String name = (String) commandParams[0];
		state.getController().addWitelistedAccount(name, time);
		client.serverMessage("[ADMIN COMMAND] successfully whitelisted account: " + name);
	}

	private void entitySetShieldRegen(GameServerState state) throws IOException {
		PlayerState playerFromName;
		try {

			playerFromName = state.getPlayerFromStateId(client.getId());

			Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get((int) playerFromName.getNetworkObject().selectedEntityId.get());
			ShieldAddOn shieldAddon = null;
			if(sendable != null) {
				if(sendable instanceof ShieldAddOn) {
					shieldAddon = (ShieldAddOn) sendable;
				} else if(sendable instanceof ManagedSegmentController<?>) {
					if(((ManagedSegmentController<?>) sendable).getManagerContainer() instanceof ShipManagerContainer) {
						shieldAddon = ((ShipManagerContainer) ((ManagedSegmentController<?>) sendable).getManagerContainer()).getShieldAddOn();
					} else if(((ManagedSegmentController<?>) sendable).getManagerContainer() instanceof StationaryManagerContainer) {
						shieldAddon = ((StationaryManagerContainer) ((ManagedSegmentController<?>) sendable).getManagerContainer()).getShieldAddOn();
					}
				}
			}
			Boolean enabled = (Boolean) commandParams[0];
			if(shieldAddon != null) {
				shieldAddon.setRegenEnabled(enabled);
				client.serverMessage("[ADMIN COMMAND] [SUCCESS] Shield addon regen set to: " + shieldAddon.isRegenEnabled() + " on " + sendable);
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] No entity selected: " + playerFromName.getNetworkObject().selectedEntityId.get() + "->(" + sendable + ")");
			}

		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}
	}

	private void entitySetPowerRegen(GameServerState state) throws IOException {
		PlayerState playerFromName;
		try {

			playerFromName = state.getPlayerFromStateId(client.getId());

			Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get((int) playerFromName.getNetworkObject().selectedEntityId.get());
			PowerAddOn powerAddon = null;
			if(sendable != null) {
				if(sendable instanceof PowerAddOn) {
					powerAddon = (PowerAddOn) sendable;
				} else if(sendable instanceof ManagedSegmentController<?>) {
					if(((ManagedSegmentController<?>) sendable).getManagerContainer() instanceof ShipManagerContainer) {
						powerAddon = ((ShipManagerContainer) ((ManagedSegmentController<?>) sendable).getManagerContainer()).getPowerAddOn();
					} else if(((ManagedSegmentController<?>) sendable).getManagerContainer() instanceof StationaryManagerContainer) {
						powerAddon = ((StationaryManagerContainer) ((ManagedSegmentController<?>) sendable).getManagerContainer()).getPowerAddOn();
					}
				}
			}
			Boolean enabled = (Boolean) commandParams[0];
			if(powerAddon != null) {
				powerAddon.setRechargeEnabled(enabled);
				client.serverMessage("[ADMIN COMMAND] [SUCCESS] Power addon regen set to: " + powerAddon.isRechargeEnabled() + " on " + sendable);
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] No entity selected: " + playerFromName.getNetworkObject().selectedEntityId.get() + "->(" + sendable + ")");
			}

		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}
	}

	private void powerDrain(GameServerState state) throws IOException {
		int drain = (Integer) commandParams[0];
		PlayerState playerFromName;
		try {

			playerFromName = state.getPlayerFromStateId(client.getId());
			SimpleTransformableSendableObject firstControlledTransformable = playerFromName.getFirstControlledTransformable();

			if(firstControlledTransformable == null || !(firstControlledTransformable instanceof SegmentController)) {
				System.err.println("[ADMIN COMMAND]checking selected");
				Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(playerFromName.getNetworkObject().selectedEntityId.get());
				if(sendable != null && sendable instanceof SimpleTransformableSendableObject) {
					firstControlledTransformable = (SimpleTransformableSendableObject) sendable;
				}

			}
			boolean ok = false;
			if(firstControlledTransformable != null && firstControlledTransformable instanceof ManagedSegmentController<?>) {
				if(((ManagedSegmentController<?>) firstControlledTransformable).getManagerContainer() instanceof PowerManagerInterface) {
					PowerAddOn powerAddOn = ((PowerManagerInterface) ((ManagedSegmentController<?>) firstControlledTransformable).getManagerContainer()).getPowerAddOn();
					ok = true;
					powerAddOn.setPower(powerAddOn.getPower() - drain);
					powerAddOn.sendPowerUpdate();
					client.serverMessage("[ADMIN COMMAND] [SUCCESS] drained " + drain + " power from " + firstControlledTransformable);
				}
			}

			if(!ok) {
				client.serverMessage("[ADMIN COMMAND] [ERROR] object " + firstControlledTransformable + " has no power capability");
			}
		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		} catch(PlayerControlledTransformableNotFound e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}
	}

	private void spawnParticle(GameServerState state) throws IOException {
		String particleName = (String) commandParams[0];
		PlayerState playerFromName;
		try {

			playerFromName = state.getPlayerFromStateId(client.getId());

			Transform t = new Transform();
			t.origin.set(playerFromName.getAssingedPlayerCharacter().getWorldTransform().origin);
			ParticleUtil.sendToAllPlayers(state, new ParticleEntry(particleName, t.origin));

		} catch(PlayerNotFountException e) {
			e.printStackTrace();
			client.serverMessage("[ADMIN COMMAND] [ERROR] " + e.getMessage());
		}
	}

	private static class SearchResult {
		String realName;
		Vector3i position;

		public SearchResult(String realName, String uid, Vector3i position) {
			this.realName = realName;
			this.position = position;
		}
	}
}
