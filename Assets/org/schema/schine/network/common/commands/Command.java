package org.schema.schine.network.common.commands;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.SerializationInterface;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.network.commands.GameCommands;
import org.schema.schine.network.NetUtil;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.commands.BasicCommands;
import org.schema.schine.network.common.NetworkProcessor;
import org.schema.schine.network.common.OutputPacket;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The Class Command.
 */
public class Command {

	public static Command[] commandIdMap;
	private static List<Commandable> commands = new ObjectArrayList<>();

	public static void addCommands(Commandable... toAdd) {
		Collections.addAll(commands, toAdd);
		int highest = 0;
		IntOpenHashSet b = new IntOpenHashSet();
		for(Commandable c : commands) {
			if(c.getFac().hasTarget()) {
				CommandPackage pack = c.getFac().getNewPackage();
				assert (pack.getType() == c) : c.name() + " has different mode than instance " + pack.getClass().getSimpleName() + "; InstanceMode: " + pack.getType().name() + "; EnumMode: " + c.name();
				if(pack.getType() != c) {
					throw new RuntimeException(c.name() + " has different mode than instance " + pack.getClass().getSimpleName() + "; InstanceMode: " + pack.getType().name() + "; EnumMode: " + c.name());
				}
			}
			if(!b.add(c.getId())) {
				assert (false) : "Duplicate ID: " + c;
				throw new RuntimeException("Duplicate ID: " + c);
			}
			highest = Math.max(c.getId(), highest);
		}
		if(commandIdMap == null) commandIdMap = new Command[highest + 1];
		else if(commandIdMap.length < highest + 1) { //have to resize
			Command[] old = commandIdMap;
			commandIdMap = new Command[highest + 1];
			System.arraycopy(old, 0, commandIdMap, 0, old.length);
		}

		for(Commandable c : commands) {
			commandIdMap[c.getId()] = c.getCommand();
		}
	}

	public static Command get(int id) throws UnknownCommandException {
		if(commandIdMap != null) return commandIdMap[id];
		return null;
	}

	private final Commandable commandable;

	public Command(Commandable commandable) {
		this.commandable = commandable;
	}

	public static void initializeCommands() {
		BasicCommands.addAllCommands();
		GameCommands.addAllCommands();
	}

	public CommandPackage instantiate() {
		return commandable.getFac().getNewPackage();
	}

	protected OutputPacket createPackage(NetworkProcessor proc, SerializationInterface target) throws IOException {
		OutputPacket p = proc.getNewOutputPacket();

		proc.attachDebugIfNecessary(this, target, p);

		p.payload.writeByte((byte) commandable.getId());
		if(commandable.getFac().hasTarget()) {
			assert (target != null) : this;
			target.serialize(p.payload, proc.isOnServer());
		}
		return p;
	}

	public void receiverProcess(NetworkProcessor proc, DataInput in, StateInterface state) throws IOException {
		if(commandable.getFac().hasTarget()) {
			CommandPackage commandPackage = commandable.getNewPackage();
			commandPackage.deserialize(in, 0, proc.isOnServer());
			state.getNetworkManager().processReceivedPackage(proc, state, commandPackage);
			commandable.freePackage(commandPackage);
		} else {
			state.getNetworkManager().processReceivedCommandWithoutTarget(proc, state, commandable);
		}
	}

	public final void send(NetworkProcessor proc) throws IOException {
		send(proc, null);
	}

	public final void send(NetworkProcessor proc, CommandPackage target) throws IOException {
		OutputPacket packet = createPackage(proc, target);
		proc.enqueuePacket(packet);
	}
//	public void sendDirect(DataOutputStream out, NetworkProcessor proc) throws IOException {
//		sendDirect(out, proc, null);
//	}
//	public void sendDirect(DataOutputStream out, NetworkProcessor proc, SerializationInterface target) throws IOException {
//		OuputPacket packet = createPackage(proc, target);
//		try {
//			packet.writeTo(out);
//		}finally {
//			proc.freeOuptutPacket(packet);
//		}
//	}

	public int getId() {
		return commandable.getId();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return commandable.name() + "[" + getId() + "]";
	}

	public static Object[] deserialize(DataInput inputStream) throws IOException {
		int parameterSize = inputStream.readByte() & 0xFF;
		Object[] parameters = new Object[parameterSize];

		for(int i = 0; i < parameterSize; i++) {
			byte type = inputStream.readByte();
			switch(type) {
				case (NetUtil.TYPE_LONG) -> parameters[i] = inputStream.readLong();
				case (NetUtil.TYPE_STRING) -> parameters[i] = inputStream.readUTF();
				case (NetUtil.TYPE_FLOAT) -> parameters[i] = inputStream.readFloat();
				case (NetUtil.TYPE_INT) -> parameters[i] = inputStream.readInt();
				case (NetUtil.TYPE_BOOLEAN) -> parameters[i] = inputStream.readBoolean();
				case (NetUtil.TYPE_BYTE) -> parameters[i] = inputStream.readByte();
				case (NetUtil.TYPE_SHORT) -> parameters[i] = inputStream.readShort();
				case (NetUtil.TYPE_VECTOR3i) -> parameters[i] = new Vector3i(inputStream.readInt(), inputStream.readInt(), inputStream.readInt());
				case (NetUtil.TYPE_VECTOR3f) -> parameters[i] = new Vector3f(inputStream.readFloat(), inputStream.readFloat(), inputStream.readFloat());
				case (NetUtil.TYPE_VECTOR4f) -> parameters[i] = new Vector4f(inputStream.readFloat(), inputStream.readFloat(), inputStream.readFloat(), inputStream.readFloat());
				case (NetUtil.TYPE_BYTE_ARRAY) -> {
					int l = inputStream.readInt();
					byte[] b = new byte[l];
					inputStream.readFully(b);
					parameters[i] = b;
					break;
				}
				case (NetUtil.TYPE_STRUCT) -> {
					Object[] val = deserialize(inputStream);
					parameters[i] = val;
					break;
				}
				default -> throw new IllegalArgumentException("Type: " + type + " unknown. parameter " + i + " of " + parameterSize + "; so far: " + Arrays.toString(parameters));
			}
		}

		return parameters;
	}

	public static void serialize(Object[] args, DataOutput buffer) throws IOException {
		if(args.length > 255) {
			throw new IOException("Invalid server message: " + Arrays.toString(args));
		}
		buffer.writeByte((byte) args.length);

		for(Object arg : args) { // 7 parameters
			switch(arg) {
				case Long l -> {
					buffer.writeByte(NetUtil.TYPE_LONG);
					buffer.writeLong(l);
				}
				case String s -> {
					buffer.writeByte(NetUtil.TYPE_STRING);
					buffer.writeUTF(s);
				}
				case Float v -> {
					buffer.writeByte(NetUtil.TYPE_FLOAT);
					buffer.writeFloat(v);
				}
				case Integer i -> {
					buffer.writeByte(NetUtil.TYPE_INT);
					buffer.writeInt(i);
				}
				case Boolean aBoolean -> {
					buffer.writeByte(NetUtil.TYPE_BOOLEAN);
					buffer.writeBoolean(aBoolean);
				}
				case Byte aByte -> {
					buffer.writeByte(NetUtil.TYPE_BYTE);
					buffer.writeByte(aByte);
				}
				case Short i -> {
					buffer.writeByte(NetUtil.TYPE_SHORT);
					buffer.writeShort(i);
				}
				case byte[] b -> {
					buffer.writeByte(NetUtil.TYPE_BYTE_ARRAY);
					buffer.writeInt(b.length);
					buffer.write(b);
				}
				case Vector3i vector3i -> {
					buffer.writeByte(NetUtil.TYPE_VECTOR3i);
					buffer.writeInt(vector3i.x);
					buffer.writeInt(vector3i.y);
					buffer.writeInt(vector3i.z);
				}
				case Vector3f vector3f -> {
					buffer.writeByte(NetUtil.TYPE_VECTOR3f);
					Vector3fTools.serialize(vector3f, buffer);
				}
				case Vector4f vector4f -> {
					buffer.writeByte(NetUtil.TYPE_VECTOR4f);
					buffer.writeFloat(vector4f.x);
					buffer.writeFloat(vector4f.y);
					buffer.writeFloat(vector4f.z);
					buffer.writeFloat(vector4f.w);
				}
				case Object[] b -> {
					buffer.writeByte(NetUtil.TYPE_STRUCT);
					serialize(b, buffer);
				}
				default -> {
					System.err.println("[COMMAND] DESERIALIZE: WARNING (object deserializing toString()): " + arg);
					buffer.writeByte(NetUtil.TYPE_STRING);
					buffer.writeUTF(arg.toString());

					assert (false) : "UNKNOWN OBJECT TYPE: " + arg + " : " + arg.getClass();
				}
			}
		}

	}

}
