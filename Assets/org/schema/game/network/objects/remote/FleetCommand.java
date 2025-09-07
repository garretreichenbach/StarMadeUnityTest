package org.schema.game.network.objects.remote;

import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetCommandTypes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class FleetCommand extends SimpleCommand<FleetCommandTypes>{

	public static final int BYTE_SIZE = 1024;
	public long fleetDbId;
	public FleetCommand(FleetCommandTypes command, Fleet fleet, Object... args) {
		this(command, fleet.dbid, args);
	}
	public FleetCommand(FleetCommandTypes command, long fleet, Object... args) {
		super(command, args);
		this.fleetDbId = fleet;
	}

	public FleetCommand() {
	}

	@Override
	protected void checkMatches(FleetCommandTypes command, Object[] args) {
		command.checkMatches(args);		
	}
	
	@Override
	public void serialize(DataOutput buffer) throws IOException {
		assert(fleetDbId > -1);
		buffer.writeLong(fleetDbId);
		super.serialize(buffer);
	}

	@Override
	public void deserialize(DataInput buffer, int updateSenderStateId) throws IOException {
		fleetDbId = buffer.readLong();
		super.deserialize(buffer, updateSenderStateId);
	}
	public byte[] serializeBytes() throws IOException {
		byte[] b = new byte[BYTE_SIZE];
		
		DataOutputStream out = new DataOutputStream(new FastByteArrayOutputStream(b));
		serialize(out);
		out.close();
		
		return b;
	}
	@Override
	public String toString() {
		return "[FLEETCOMMAND: "+fleetDbId+"; "+getCommand()+"; "+Arrays.toString(getArgs())+"]";
	}
	
}
