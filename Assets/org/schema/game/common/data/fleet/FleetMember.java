package org.schema.game.common.data.fleet;

import api.common.GameClient;
import api.common.GameServer;
import com.bulletphysics.linearmath.Transform;
import org.lwjgl.opengl.GL11;
import org.schema.common.SerializationInterface;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.ingame.map.MapControllerManager;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.data.gamemap.entry.AbstractMapEntry;
import org.schema.game.client.view.effects.ConstantIndication;
import org.schema.game.client.view.effects.Indication;
import org.schema.game.client.view.gamemap.GameMapDrawer;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.database.DatabaseEntry;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionNewsPost;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.game.common.data.world.*;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.SelectableSprite;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.network.server.ServerStateInterface;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.*;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public class FleetMember implements SerializationInterface {

	public final FleetMemberMapIndication mapEntry;
	private final StateInterface state;
	public String UID;
	public String name;
	public String command = "";
	public long entityDbId;
	public Vector3i moveTarget;
	public int tmpListIndex;
	private Vector3i sector = new Vector3i();
	private long dockedToId;
	private double lastMass;
	private boolean changed;
	private long dockedToDbRootId = -1;
	private int factionId;
	private FleetUnloadedAction currentAction;
	private float lastShipPercent;
	private boolean attackedTriggered;
	private int dockedToFaction;

	public FleetMember(StateInterface state) {
		super();
		this.state = state;

		mapEntry = isOnServer() ? null : new FleetMemberMapIndication();
	}


	public FleetMember(SegmentController c) {
		this(c.getState());
		assert (isOnServer());
		UID = c.getUniqueIdentifier();
		name = c.getRealName();
		entityDbId = c.dbId;
		Sector sec = ((GameServerState) state).getUniverse().getSector(c.getSectorId());
		sector.set(sec.pos);
		this.dockedToDbRootId = c.railController.isDocked() ? c.railController.getRoot().dbId : -1L;
		this.dockedToId = c.railController.isDocked() ? c.railController.previous.rail.getSegmentController().dbId : -1L;
		lastMass = c.getTotalPhysicalMass();
	}

	public FleetMember(GameServerState state, long dbId, String fullUid, String realName, Vector3i entitySector, long dockedTo, long dockedRoot) {
		this(state);
		assert (isOnServer());
		UID = fullUid;
		name = realName;
		entityDbId = dbId;
		this.dockedToDbRootId = dockedRoot;
		this.dockedToId = dockedTo;
		sector.set(entitySector);
	}

	public Fleet getFleet() {
		if(isOnServer()) return GameServer.getServerState().getFleetManager().getByEntityDbId(entityDbId);
		else return GameClient.getClientState().getFleetManager().getByFleetDbId(entityDbId);
	}

	public Fleet getFleetByOwner(String owner) {
		Collection<Fleet> fleets = GameClient.getClientState().getFleetManager().fleetCache.values();
		if(fleets != null) {
			for(Fleet fleet : fleets) {
				if(fleet.canAccess(owner) && fleet.getMembers().contains(this)) {
					return fleet;
				}
			}
		}
		return null;
	}

	public boolean isLoaded() {
		return state.getLocalAndRemoteObjectContainer().getUidObjectMap().containsKey(UID);
	}

	public void updateServerData() {
		assert (isOnServer());

		if(isOnServer()) {
			if(isLoaded()) {
				SimpleTransformableSendableObject s = (SimpleTransformableSendableObject) state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(UID);

				RemoteSector sec = (RemoteSector) state.getLocalAndRemoteObjectContainer().getLocalObjects().get(s.getSectorId());

				if(!sector.equals(sec.getServerSector().pos)) {
					sector.set(sec.getServerSector().pos);
					changed = true;
				}
				if(!name.equals(s.getRealName())) {
					name = s.getRealName();
					changed = true;
				}
			} else {
				try {
//					((GameServerState)state).getDatabaseIndex().getSector(DatabaseEntry.removePrefixWOException(UID), sector);
					List<DatabaseEntry> entries = ((GameServerState) state).getDatabaseIndex().getTableManager().getEntityTable().getByUIDExact(DatabaseEntry.removePrefixWOException(UID), 1);
					if(entries.size() > 0) {
						DatabaseEntry databaseEntry = entries.get(0);
						if(!name.equals(databaseEntry.realName)) {
							name = databaseEntry.realName;
							changed = true;
						}
						if(!name.equals(databaseEntry.sectorPos)) {
							sector.set(databaseEntry.sectorPos);
							changed = true;
						}
					}
				} catch(SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public Vector3i getSector() {
		return sector;
	}

	public int getFactionId() {
		SegmentController loaded = getLoaded();
		if(loaded != null) {
			factionId = loaded.getFactionId();
		}
		return factionId;
	}

	public void setFactionId(int faction) {
		factionId = faction;
	}

	public boolean isOnServer() {
		return state instanceof ServerStateInterface;
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeBoolean(true);
		b.writeUTF(name);
		b.writeUTF(UID);
		b.writeLong(entityDbId);
		b.writeUTF(command);
		sector.serialize(b);
		b.writeDouble(lastMass);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		if(b.readBoolean()) {
			name = b.readUTF();
			UID = b.readUTF();
			entityDbId = b.readLong();
			command = b.readUTF();
			sector.deserialize(b);
			try {
				lastMass = b.readDouble();
			} catch(EOFException e) {
				lastMass = 0;
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (entityDbId ^ (entityDbId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		FleetMember other = (FleetMember) obj;
		return entityDbId == other.entityDbId;
	}

	@Override
	public String toString() {
		return "FleetMember [state=" + state + ", sector=" + sector + ", UID=" + UID + ", name=" + name + ", command=" + command + ", changed=" + changed + ", entityDbId=" + entityDbId + "]";
	}

	public void apply(FleetMember m) {
		assert (UID.equals(m.UID));
		sector.set(m.sector);
		name = m.name;
		command = m.command;
		lastMass = m.lastMass;
	}

	public SegmentController getLoaded() {
		Sendable sendable = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(UID);
		return sendable != null && sendable instanceof SegmentController ? (SegmentController) sendable : null;
	}

	public String getName() {
		return name;
	}

	public void moveUnloadedTowardsGoal(Fleet f, Vector3i to) {
		assert (f.isCached());
		if(!isLoaded() && !isDocked()) {
//			System.err.println("FLEET MOVE: "+name+"; DOCKED "+isDocked()+"; DOCKED_ROOT_ID: "+dockedToDbRootId);
			Vector3i from = new Vector3i();
			Vector3i toTmp = new Vector3i();
			Vector3i toDestTmp = new Vector3i();

			from.set(getSector());
			double shortest = -1;
			int shortestDirIndex = -1;
			for(int i = 0; i < 6; i++) {
				toTmp.set(from);
				toTmp.add(Element.DIRECTIONSi[i]);
				toTmp.sub(to);
				double len = toTmp.lengthSquaredDouble();
				if(shortest < 0 || len < shortest) {
					toDestTmp.set(from);
					toDestTmp.add(Element.DIRECTIONSi[i]);
					shortest = len;
					shortestDirIndex = i;
				}
			}
			if(shortest >= 0) {
				boolean updateDB = true;
				sectorMoveUnsave(f, toDestTmp, shortestDirIndex);
			}
		}

	}

	private void sectorMoveUnsave(Fleet f, Vector3i to, int shortestDir) {
		assert (f.isCached());
		try {

			Vector3f local = new Vector3f();
			local.set(Element.DIRECTIONSf[shortestDir]);
			local.negate();
			local.scale((((GameStateInterface) state).getSectorSize() * 0.5f) * 0.97f);
			((GameServerState) state).getDatabaseIndex().getTableManager().getEntityTable().changeSectorForEntity(entityDbId, to, local, true);

		} catch(SQLException e) {
			e.printStackTrace();
		}
		if(!sector.equals(to)) {
			onSectorChange(sector, to);
			sector.set(to);
			((GameServerState) state).getFleetManager().submitSectorChangeToClients(this);
		}

		//recursively move all docked eneties with the mothership
		for(int i = 0; i < f.getMembers().size(); i++) {
			FleetMember fleetMember = f.getMembers().get(i);
			if(fleetMember != this && fleetMember.getDockedToRootDbId() == entityDbId) {
				fleetMember.sector.set(sector);
				((GameServerState) state).getFleetManager().submitSectorChangeToClients(fleetMember);
			}
		}


		checkLoadPhysicalWithDocks();
	}

	private void checkLoadPhysicalWithDocks() {
		Sector sec = ((GameServerState) state).getUniverse().getSectorWithoutLoading(getSector());
		if(sec != null) {
			System.err.println("[SERVER][FLEET_MEMBER] NOW SECTOR != NULL: NOW LOADING PHYSICALLY: " + UID);
			loadEntityInSectorWithDocks(sec);

		}
	}

	private boolean isDocked() {
		return dockedToDbRootId > 0;
	}

	private void loadEntityInSectorWithDocks(Sector sec) {
		assert (sec != null);
		sec.setActive(true);

		try {
			EntityUID mm = new EntityUID(UID, DatabaseEntry.getEntityType(UID), entityDbId);
			mm.spawnedOnlyInDb = true; //since the fleet moved at least one sector we have to avoid
			Sendable loadEntitiy = sec.loadEntitiy((GameServerState) state, mm);

			System.err.println("[SERVER][FLEET_MEMBER] LOADED PHYSICALLY ROOT: " + loadEntitiy);

			List<EntityUID> loadByDockedEntity = ((GameServerState) state).getDatabaseIndex().getTableManager().getEntityTable().loadByDockedEntity(entityDbId);
			int c = 0;
			for(EntityUID docked : loadByDockedEntity) {
				Sendable dock = sec.loadEntitiy((GameServerState) state, docked);
				System.err.println("[SERVER][FLEET_MEMBER] LOADED PHYSICALLY DOCK #" + c + ": " + dock);
				c++;
			}
			if(loadEntitiy instanceof SegmentController) {
				lastMass = ((SegmentController) loadEntitiy).getTotalPhysicalMass();
				assert (((SegmentController) loadEntitiy).needsPositionCheckOnLoad) : loadEntitiy;
			}
		} catch(Exception e) {
			System.err.println("FAILED TO LOAD ENTITY: " + UID);
			e.printStackTrace();
		}
	}

	public void moveRequestUnloaded(Fleet fleet, Vector3i to) {
		assert (fleet.isCached());
		if(this.currentAction == null || !(this.currentAction instanceof FleetUnloadedActionMoveTo)) {
			this.currentAction = new FleetUnloadedActionMoveTo(this, fleet, to);
		} else {
			((FleetUnloadedActionMoveTo) this.currentAction).setTarget(to);
		}
		this.currentAction.fleet = fleet;

		((FleetStateInterface) state).getFleetManager().addUpdateAction(this.currentAction);
	}

	public long getDockedToRootDbId() {
		SegmentController s;
		if((s = getLoaded()) != null) {
			dockedToDbRootId = s.railController.isDockedAndExecuted() ? s.railController.getRoot().dbId : -1;
			dockedToId = s.railController.isDocked() ? s.railController.previous.rail.getSegmentController().dbId : -1L;
			dockedToFaction = s.railController.getRoot().getFactionId();
		}
		return dockedToDbRootId;
	}

	public String getPickupPoint() {
		return isLoaded() ? (((Ship) getLoaded()).lastPickupAreaUsed != Long.MIN_VALUE ? ElementCollection.getPosFromIndex(((Ship) getLoaded()).lastPickupAreaUsed, new Vector3i()).toStringPure() : "NONE") : "N/A (unloaded)";
	}

	public double getMass() {
		return lastMass;
	}

	public float getEngagementRange() {
		SegmentController loaded = getLoaded();
		if(loaded != null) {
			return ((Ship) loaded).getAiConfiguration().getAiEntityState().getShootingRange();
		}
		return 0;
	}

	public float getShipPercent() {
		SegmentController loaded = getLoaded();
		if(loaded != null) {
			return (float) ((Ship) loaded).getHpController().getHpPercent();
		}
		return 0;
	}

	public boolean checkAttacked() {
		SegmentController loaded = getLoaded();
		if(loaded != null) {
			if(lastShipPercent <= 0) {
				lastShipPercent = getShipPercent();
			} else if(getShipPercent() < lastShipPercent && !attackedTriggered) {
				attackedTriggered = true;
				return true;
			}
		}
		return false;
	}

	public boolean isAttackedTriggered() {
		return attackedTriggered;
	}

	public void setAttackedTriggered(boolean attackedTriggered) {
		this.attackedTriggered = attackedTriggered;
	}

	public void onSectorChange(Vector3i sector, Vector3i to) {
		Vector3i beforeSystem = new Vector3i();
		Vector3i newSystem = new Vector3i();
		StellarSystem.getPosFromSector(sector, beforeSystem);
		StellarSystem.getPosFromSector(to, newSystem);
		if(!beforeSystem.equals(newSystem)) {
			StellarSystem sSys;
			try {
				sSys = ((GameServerState) state).getUniverse().getStellarSystemFromStellarPos(newSystem);
				if(sSys.getOwnerFaction() != 0 && sSys.getOwnerFaction() != getFactionId()) {
					Faction faction = ((GameServerState) state).getFactionManager().getFaction(sSys.getOwnerFaction());
					if(faction != null) {
						//how is the owner's relation to this player
						RType relation = ((GameServerState) state).getFactionManager().getRelation(faction.getIdFaction(), getFactionId());
						switch(relation) {
							case ENEMY -> {
								String msgs = Lng.str("Our scanners picked up a HOSTILE\nsignature in our territory.\n\nOrigin System: %s %s.\nSend Scanner there to get exact\npositions!", sSys.getName(), sSys.getPos());
								Object[] msg = Lng.astr("Our scanners picked up a HOSTILE\nsignature in our territory.\n\nOrigin System: %s %s.\nSend Scanner there to get exact\npositions!", sSys.getName(), sSys.getPos());
								faction.broadcastMessage(msg, ServerMessage.MESSAGE_TYPE_WARNING, ((GameServerState) state));
								FactionNewsPost o = new FactionNewsPost();
								o.set(faction.getIdFaction(), Lng.str("Faction Auto Scanner"), System.currentTimeMillis(), Lng.str("Hostile signature in territory"), msgs, 0);
								((GameServerState) state).getFactionManager().addNewsPostServer(o);
							}
							case FRIEND -> faction.broadcastMessage(Lng.astr("Our scanners picked up a FRIENDLY\nsignature in our territory.\n\nOrigin System: %s %s.\nSend Scanner there to get exact\npositions!", sSys.getName(), sSys.getPos()), ServerMessage.MESSAGE_TYPE_INFO, ((GameServerState) state));
							case NEUTRAL -> faction.broadcastMessage(Lng.astr("Our scanners picked up a NEUTRAL\nsignature in our territory.\n\nOrigin System: %s %s.\nSend Scanner there to get exact\npositions!", sSys.getName(), sSys.getPos()), ServerMessage.MESSAGE_TYPE_INFO, ((GameServerState) state));
							default -> {
							}
						}

					}
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void onRemovedFromFleet() {
		if(isLoaded()) {
			getLoaded().onRevealingAction();
		}
	}

	public void setDockedToDbRootId(long dockedToDbRootId) {
		this.dockedToDbRootId = dockedToDbRootId;
	}

	public void setCombinedTargetId(int targetId) {
		ManagedUsableSegmentController<?> musc = (ManagedUsableSegmentController<?>) getLoaded();
		if(musc.isAIControlled() && musc.getAiConfiguration().getAiEntityState().getCurrentProgram() instanceof TargetProgram<?>) {
			TargetProgram<?> targetProgram = (TargetProgram<?>) musc.getAiConfiguration().getAiEntityState().getCurrentProgram();
			targetProgram.setSpecificTargetId(targetId);
		}
	}

	public class FleetMemberMapIndication extends AbstractMapEntry implements SelectableSprite {
		private final Vector4f DEFAULT_COLOR = new Vector4f(0.3f, 0.8f, 0.2f, 0.8f);
		public boolean s;
		private Indication indication;
		private Vector4f color = new Vector4f(DEFAULT_COLOR);

		private Vector3f posf = new Vector3f();

		private boolean drawIndication;

		private float selectDepth;
		public Vector3i getSector() {
			return getMember().getSector();
		}

		public String getName() {
			return name;
		}

		public FleetMember getMember() {
			return FleetMember.this;
		}

		@Override
		public void drawPoint(boolean colored, int filter, Vector3i selectedSector) {
			if(colored) {
				float alpha = 1f;
				if(!include(filter, selectedSector)) {
					alpha = 0.1f;
				}
				GlUtil.glColor4f(0.9f, 0.1f, 0.1f, alpha);
			}
			GL11.glBegin(GL11.GL_POINTS);
			GL11.glVertex3f(getPos().x, getPos().y, getPos().z);
			GL11.glEnd();
		}

		public void resetColor() {
			color = new Vector4f(DEFAULT_COLOR);
		}

		@Override
		public Indication getIndication(Vector3i system) {
			Vector3f pos = getPos();
			if(indication == null) {
				Transform t = new Transform();
				t.setIdentity();
				indication = new ConstantIndication(t, name + " " + getSector());
			}
			indication.setText(name + " " + getSector() + "\n(Right-Click for options)");
			indication.getCurrentTransform().origin.set(pos.x - GameMapDrawer.halfsize, pos.y - GameMapDrawer.halfsize, pos.z - GameMapDrawer.halfsize);
			if(s) indication.setText(name + " " + getSector() + "\n(Selected)");
			else indication.setText(name + " " + getSector() + "\n(Right-Click for options)");
			return indication;
		}

		@Override
		public int getType() {
			return EntityType.SHIP.ordinal();
		}

		@Override
		public void setType(byte type) {
		}

		@Override
		public boolean include(int filter, Vector3i selectedSector) {
			return true;
		}

		@Override
		public Vector4f getColor() {
//			if(s) return new Vector4f(((1.0f - GameMapDrawer.sinus.getTime()) / 2f + 0.5f), 1, ((1.0f - GameMapDrawer.sinus.getTime()) / 2f + 0.5f), 1);
//			else return color;
			return color;
		}

		public void setColor(Vector4f color) {
			this.color = color;
		}

		@Override
		public float getScale(long time) {
			if(s) return 0.1f + (0.07f * GameMapDrawer.sinus.getTime());
			else return 0.1f;
		}

		@Override
		public int getSubSprite(Sprite sprite) {
			return EntityType.SHIP.mapSprite;
		}

		@Override
		public boolean canDraw() {
			return true;
		}

		@Override
		public Vector3f getPos() {
			posf.set((getSector().x / VoidSystem.SYSTEM_SIZEf) * 100f, (getSector().y / VoidSystem.SYSTEM_SIZEf) * 100f, (getSector().z / VoidSystem.SYSTEM_SIZEf) * 100f);
			return posf;
		}

		/**
		 * @return the drawIndication
		 */
		@Override
		public boolean isDrawIndication() {
			return drawIndication;
		}

		/**
		 * @param drawIndication the drawIndication to set
		 */
		@Override
		public void setDrawIndication(boolean drawIndication) {
			this.drawIndication = drawIndication;
		}

		@Override
		protected void decodeEntryImpl(DataInputStream stream) throws IOException {
		}

		@Override
		public void encodeEntryImpl(DataOutputStream buffer) throws IOException {
		}


		@Override
		public float getSelectionDepth() {
			return selectDepth;
		}

		@Override
		public void onSelect(float depth) {
			drawIndication = true;
			this.selectDepth = depth;
			MapControllerManager.selected.add(this);

		}

		@Override
		public void onUnSelect() {
			drawIndication = false;
			MapControllerManager.selected.remove(this);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return (int) entityDbId;
		}

		@Override
		public boolean isSelectable() {
			return true;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof FleetMemberMapIndication) {
				return ((FleetMemberMapIndication) obj).getEntityId() == getEntityId();
			}

			return false;
		}

		private long getEntityId() {
			return entityDbId;
		}
	}
}
