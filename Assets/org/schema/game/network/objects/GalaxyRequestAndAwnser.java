package org.schema.game.network.objects;
import java.io.*;

public class GalaxyRequestAndAwnser {

	public String ownerUID;
	public int factionUID;
	public int secX;
	public int secY;
	public int secZ;

	public NetworkClientChannel networkObjectOnServer;

	public void deserialize(DataInput b, boolean onServer) throws IOException {
		if (onServer) {
			secX = b.readInt();
			secY = b.readInt();
			secZ = b.readInt();
		} else {
			secX = b.readInt();
			secY = b.readInt();
			secZ = b.readInt();

			if (b.readByte() != 0) {
				ownerUID = b.readUTF();
				factionUID = b.readInt();
			}

		}
	}

	public void serialize(DataOutput b, boolean onServer) throws IOException {
		if (onServer) {
			b.writeInt(secX);
			b.writeInt(secY);
			b.writeInt(secZ);

			if (ownerUID != null) {
				b.writeByte(1);
				b.writeUTF(ownerUID);
				b.writeInt(factionUID);
			} else {
				b.writeByte(0);
			}
		} else {
			b.writeInt(secX);
			b.writeInt(secY);
			b.writeInt(secZ);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "GalaxyRequestAndAwnser [ownerUID=" + ownerUID + ", factionUID="
				+ factionUID + ", secX=" + secX + ", secY=" + secY + ", secZ="
				+ secZ + ", networkObjectOnServer=" + networkObjectOnServer
				+ "]";
	}

}
