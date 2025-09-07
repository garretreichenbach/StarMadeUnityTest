package org.schema.game.common.data.missile.updates;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.game.client.controller.ClientChannel;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.missile.ActivationMissileInterface;
import org.schema.game.common.data.missile.BombMissile;
import org.schema.game.common.data.missile.DumbMissile;
import org.schema.game.common.data.missile.FafoMissile;
import org.schema.game.common.data.missile.HeatMissile;
import org.schema.game.common.data.missile.Missile;
import org.schema.game.common.data.missile.TargetChasingMissile;
import org.schema.schine.network.StateInterface;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

public class MissileSpawnUpdate extends MissileUpdate {

	private static interface MissileFactory{
		public Missile instance(StateInterface state);
	}
	public static enum MissileType{
		DUMB((byte)1, 0, false, false, DumbMissile::new),
		HEAT((byte)2, 1, true, false, HeatMissile::new),
		FAFO((byte)3, 2, true, false, FafoMissile::new),
		BOMB((byte)4, 3, false, true, BombMissile::new),
		;
		
		public final byte id;
		public final boolean targetChasing;
		public final boolean activationTime;
		public final MissileFactory fac;
		public final int configIndex;
		
		private MissileType(byte id, int configIndex, boolean targetChasing, boolean activationTime, MissileFactory fac){
			this.id = id;
			this.targetChasing = targetChasing;
			this.activationTime = activationTime;
			this.fac = fac;
			this.configIndex = configIndex;
		}
	}
	
	public static final Byte2ObjectOpenHashMap<MissileType> typeMap = new Byte2ObjectOpenHashMap<MissileType>();
	static{
		for(MissileType t : MissileType.values()){
			assert(!typeMap.containsKey(t.id)):"DOUBLE ID: "+t+"; "+typeMap.get(t.id).name();
			
			typeMap.put(t.id, t);
		}
	}
	
	public Vector3f position = new Vector3f();
	public Vector3f dir = new Vector3f();
	public float speed;
	public int target;
	public MissileType missileType;
	public short colorType;
	public int sectorId = -666;
	public long spawnTime;
	public Vector3f targetPos;
	public int startTicks;
	public Vector3f relativePos;
	public float bombActivationTime;
	public long weaponId;

	public MissileSpawnUpdate(byte type, short id) {
		super(type, id);
		//		System.err.println("[MISSILE] DECODING MISSILE SPAWN : "+this.id);
		assert (type == SPAWN);
	}

	public MissileSpawnUpdate(short id) {
		super(SPAWN, id);
	}

	public MissileSpawnUpdate(short id, int damage, Vector3f position, Vector3f dir, float speed, int target, 
			MissileType missileType, int sectorId, long weaponId, short colorType, 
			long spawnTime, Vector3f targetPos, int startTicks, Vector3f relativePos, float bombActivationTime) {
		super(SPAWN, id);
		this.missileType = missileType;
		this.position.set(position);
		this.dir.set(dir);
		this.target = target;
		this.sectorId = sectorId;
		this.speed = speed;
		this.weaponId = weaponId;
		this.colorType = colorType;
		this.spawnTime = spawnTime;
		this.targetPos = targetPos;
		this.startTicks = startTicks;
		this.relativePos = relativePos;
		this.bombActivationTime = bombActivationTime;
	}

	@Override
	protected void decode(DataInputStream stream) throws IOException {
		missileType = typeMap.get(stream.readByte());
		position.set(stream.readFloat(), stream.readFloat(), stream.readFloat());
		dir.set(stream.readFloat(), stream.readFloat(), stream.readFloat());
		target = stream.readInt();
		sectorId = stream.readInt();
		colorType = stream.readShort();
		weaponId = stream.readLong();
		speed = stream.readFloat();
		spawnTime = stream.readLong();
		startTicks = stream.readInt();
		if(missileType.activationTime){
			bombActivationTime = stream.readFloat();
		}
		if(missileType.targetChasing){
			targetPos = Vector3fTools.deserialize(stream);
			relativePos = Vector3fTools.deserialize(stream);
		}
	}

	@Override
	protected void encode(DataOutputStream buffer) throws IOException {
		buffer.writeByte(missileType.id);
		buffer.writeFloat(position.x);
		buffer.writeFloat(position.y);
		buffer.writeFloat(position.z);
		buffer.writeFloat(dir.x);
		buffer.writeFloat(dir.y);
		buffer.writeFloat(dir.z);
		buffer.writeInt(target);
		buffer.writeInt(sectorId);
		buffer.writeShort(colorType);
		buffer.writeLong(weaponId);
		buffer.writeFloat(speed);
		buffer.writeLong(spawnTime);
		buffer.writeInt(startTicks);
		if(missileType.activationTime){
			buffer.writeFloat(bombActivationTime);
		}
		if(missileType.targetChasing){
			Vector3fTools.serialize(targetPos, buffer);
			Vector3fTools.serialize(relativePos, buffer);
		}
	}

	@Override
	public void handleClientUpdate(GameClientState state,
			Short2ObjectOpenHashMap<Missile> missiles, ClientChannel channel) {
		if (!missiles.containsKey(id)) {
			Missile m = getClientMissile(state);
			missiles.put(m.getId(), m);
			m.startTrail();
		} 
	}

	public Missile getClientMissile(StateInterface state) {
		Missile m = missileType.fac.instance(state);
		assert(!m.getType().activationTime || m instanceof ActivationMissileInterface );
		assert(!m.getType().targetChasing || m instanceof TargetChasingMissile );
		m.setFromSpawnUpdate(this);
		return m;
	}

}
