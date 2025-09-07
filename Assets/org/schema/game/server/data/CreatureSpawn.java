package org.schema.game.server.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.ai.AIGameCreatureConfiguration;
import org.schema.game.common.data.creature.AICharacter;
import org.schema.game.common.data.creature.AICreature;
import org.schema.game.common.data.creature.AIRandomCompositeCreature;
import org.schema.game.common.data.creature.CreaturePartNode;
import org.schema.game.common.data.world.Sector;
import org.schema.schine.resource.CreatureStructure.PartType;

import com.bulletphysics.linearmath.Transform;

public class CreatureSpawn {

	private static int count;
	private Vector3i spawnSectorPos = new Vector3i();
	private Transform localPos = new Transform();
	private String realName = "noName";
	private CreatureType type = CreatureType.CHARACTER;
	private CreaturePartNode node;
	private float speed = -1;
	private float scale = 1;
	private float height = 0.2f;
	private float width = 0.3f;
	private Vector3i blockDim = new Vector3i(1, 1, 1);

	public CreatureSpawn() {
		localPos.setIdentity();
	}

	public CreatureSpawn(Vector3i spawnSectorPos, Transform localPos, String realName, CreatureType type) {
		this();
		assert (type != null);
		this.spawnSectorPos = new Vector3i(spawnSectorPos);
		this.localPos = new Transform(localPos);
		this.realName = realName;
		this.type = type;

	}

	public void serialize(DataOutput buffer) throws IOException {
		buffer.writeInt(type.ordinal());

		buffer.writeUTF(realName);

		buffer.writeInt(spawnSectorPos.x);
		buffer.writeInt(spawnSectorPos.y);
		buffer.writeInt(spawnSectorPos.z);

		buffer.writeFloat(localPos.origin.x);
		buffer.writeFloat(localPos.origin.y);
		buffer.writeFloat(localPos.origin.z);

		buffer.writeInt(blockDim.x);
		buffer.writeInt(blockDim.y);
		buffer.writeInt(blockDim.z);

		buffer.writeFloat(speed);
		buffer.writeFloat(scale);
		buffer.writeFloat(width);
		buffer.writeFloat(height);

		if (type == CreatureType.CREATURE_SPECIFIC) {
			node.serialize(buffer);
		}

	}

	public void deserialize(DataInput buffer) throws IOException {
		type = CreatureType.values()[buffer.readInt()];

		realName = buffer.readUTF();

		spawnSectorPos.x = buffer.readInt();
		spawnSectorPos.y = buffer.readInt();
		spawnSectorPos.z = buffer.readInt();

		localPos.origin.x = buffer.readFloat();
		localPos.origin.y = buffer.readFloat();
		localPos.origin.z = buffer.readFloat();

		blockDim.x = buffer.readInt();
		blockDim.y = buffer.readInt();
		blockDim.z = buffer.readInt();

		speed = buffer.readFloat();
		scale = buffer.readFloat();
		width = buffer.readFloat();
		height = buffer.readFloat();

		if (type == CreatureType.CREATURE_SPECIFIC) {
			node = new CreaturePartNode(PartType.BOTTOM);
			node.deserialize(buffer);
		}
	}

	public void execute(GameServerState state) throws IOException {
		String uName = "ENTITY_CREATURE_" + System.currentTimeMillis() + "_" + (count++);
		//		CreaturePartNode pBot = new CreaturePartNode(state, "LegsArag", null);
		//		CreaturePartNode pMid = new CreaturePartNode(state, "TorsoShell", null);
		//		pBot.attach(state, pMid, AttachmentType.MAIN);
		//		AIRandomCompositeCreature c = AIRandomCompositeCreature.isntantiate(state, 4, 1, 0.5f, 0.25f, new Vector3i(1,1,1), pBot);

		AICreature<?> c;

		assert (type != null);
		switch(type) {
			case CHARACTER -> c = new AICharacter(state);
			case CREATURE_RANDOM -> c = AIRandomCompositeCreature.random(state);
			case CREATURE_SPECIFIC -> {
				assert (node != null);
				c = AIRandomCompositeCreature.instantiate(state, 4, scale, width, height, blockDim, node);
			}
			default -> throw new IllegalArgumentException();
		}

		Sector spawnSector = state.getUniverse().getSector(spawnSectorPos);
		c.setSectorId(spawnSector.getId());
		c.initialize();
		c.setRealName(realName);
		c.getInitialTransform().setIdentity();

		c.getInitialTransform().set(localPos);

		c.initialFillInventory();
		c.setId(state.getNextFreeObjectId());
		c.setUniqueIdentifier(uName);
		if (speed >= 0) {
			c.setSpeed(speed);
		}

		AIGameCreatureConfiguration<?, ?> aiConfiguration = c.getAiConfiguration();
		initAI(aiConfiguration);
		aiConfiguration.applyServerSettings();
		state.getController().getSynchController().addNewSynchronizedObjectQueued(c);
	}

	public void initAI(AIGameCreatureConfiguration<?, ?> aiConfiguration) {
		//can be overwritten
	}

	/**
	 * @return the node
	 */
	public CreaturePartNode getNode() {
		return node;
	}

	/**
	 * @param node the node to set
	 */
	public void setNode(CreaturePartNode node) {
		this.node = node;
	}

	/**
	 * @return the speed
	 */
	public float getSpeed() {
		return speed;
	}

	/**
	 * @param speed the speed to set
	 */
	public void setSpeed(float speed) {
		this.speed = speed;
	}
}
