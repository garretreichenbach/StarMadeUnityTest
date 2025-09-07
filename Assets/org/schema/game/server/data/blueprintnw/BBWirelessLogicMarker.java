package org.schema.game.server.data.blueprintnw;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.common.data.element.ElementCollection;

public class BBWirelessLogicMarker {
	public String marking;
	public long markerLocation;
	public long fromLocation;

	public void serialize(DataOutputStream stream) throws IOException {
		stream.writeUTF(marking);
		stream.writeLong(markerLocation);
		stream.writeLong(fromLocation);
	}

	public void deserialize(DataInputStream stream, int shift) throws IOException {
		marking = stream.readUTF();
		markerLocation = stream.readLong();
		fromLocation = stream.readLong();
		
		if(shift != 0){
			markerLocation = ElementCollection.shiftIndex(markerLocation, shift, shift, shift);
			fromLocation = ElementCollection.shiftIndex(fromLocation, shift, shift, shift);
		}
	}
}