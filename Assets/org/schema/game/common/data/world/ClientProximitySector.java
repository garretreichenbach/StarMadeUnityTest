package org.schema.game.common.data.world;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import api.config.BlockConfig;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.io.SegmentSerializationBuffersGZIP;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SectorInformation.SectorType;
import org.schema.game.common.data.world.planet.gasgiant.GasPlanetInformation;
import org.schema.game.common.data.world.planet.terrestrial.TerrestrialBodyInformation;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.resource.tag.Tag;

public class ClientProximitySector {
	public static final int PROXIMITY_SIZE = 16;
	public static final int LEN = (PROXIMITY_SIZE * 2);
	public static final int LENxLEN = LEN * LEN;
	public static final int ALEN = (LEN * LEN * LEN);

	private int sectorId;
	private Vector3i basePosition = new Vector3i();
	private byte[] sectorType = new byte[ALEN + ALEN];
	private PlayerState player;
	private Int2ObjectOpenHashMap<TerrestrialBodyInformation> terrestrialsDetails = new Int2ObjectOpenHashMap<>();
	private Int2ObjectOpenHashMap<GasPlanetInformation> gasPlanetsDetails = new Int2ObjectOpenHashMap<>();

	public ClientProximitySector(PlayerState state) {
		this.player = state;
	}

	public void deserialize(DataInputStream stream) throws IOException {

		long t = System.currentTimeMillis();

		sectorId = stream.readInt();

		int x = stream.readInt();
		int y = stream.readInt();
		int z = stream.readInt();

		basePosition.set(x, y, z);
		final SegmentSerializationBuffersGZIP bm = SegmentSerializationBuffersGZIP.get();
		Inflater inflater = bm.inflater;
		byte[] buffer = bm.SEGMENT_BUFFER;
		try {
			int inflatedSize = stream.readInt();

			int read = stream.read(buffer, 0, inflatedSize);

			assert (read == inflatedSize);
			inflater.reset();
			inflater.setInput(buffer, 0, inflatedSize);
			try {
				int inflate = inflater.inflate(sectorType);

				if (inflate == 0) {
					System.err.println("WARNING: INFLATED BYTES 0: " + inflater.needsInput() + " " + inflater.needsDictionary());
				}

				if (inflate != sectorType.length) {
					System.err.println("[INFLATER] Exception: " + sectorType.length + " size received: " + inflatedSize + ": " + inflate + "/" + sectorType.length + " for " + player + " pos " + basePosition);
				}
			} catch (DataFormatException e) {
				e.printStackTrace();
			}
		}finally {
			SegmentSerializationBuffersGZIP.free(bm);
		}

		int index;
		Tag tag;

		short trrs = stream.readShort();
		for(short i = 0; i < trrs; i++){
			index = stream.readInt();
			tag = Tag.deserializeNT(stream);
			terrestrialsDetails.put(index,new TerrestrialBodyInformation(tag));
		}

		short ggs = stream.readShort(); //(TODO merge these with short object type headers and a custom unpacker built in VoidSystemObject rather than tags)
		for(short i = 0; i < ggs; i++){
			index = stream.readInt();
			tag = Tag.deserializeNT(stream);
			gasPlanetsDetails.put(index,new GasPlanetInformation(tag));
		}

		long took = System.currentTimeMillis() - t;
		if (took > 5) {
			System.err.println("[CLIENT] WARNING: deserialized PROXIMITY " + basePosition + " took " + took + "ms");
		}
		//		for(int i = 0; i<sectorType.length; i++){
		//			sectorType[i] = stream.readByte();
		//		}

	}

	public Vector3i getBasePosition() {
		return basePosition;
	}

	public byte getPlanetType(int i) {
		return sectorType[ALEN + i];
	}

	public GasPlanetInformation getGasPlanetInfo(int secIndex) {
		return gasPlanetsDetails.get(secIndex);
	}

	public TerrestrialBodyInformation getTerrestrialInfo(int secIndex){
		return terrestrialsDetails.get(secIndex);
	}

	public Vector3i getPosFromIndex(int i, Vector3i out) {
		out.set(
				basePosition.x - PROXIMITY_SIZE,
				basePosition.y - PROXIMITY_SIZE,
				basePosition.z - PROXIMITY_SIZE);

		int relZ = i / (LENxLEN);

		int relY = (i - (relZ * (LENxLEN))) / LEN;

		int relX = i - ((relZ * (LENxLEN)) + relY * LEN);

		out.add(relX, relY, relZ);

		return out;
	}

	public int getSectorId() {
		return sectorId;
	}

	public byte getSectorType(int i) {
		return sectorType[i];
	}

	public void serialize(DataOutput buffer) throws IOException {
		buffer.writeInt(sectorId);
		buffer.writeInt(basePosition.x);
		buffer.writeInt(basePosition.y);
		buffer.writeInt(basePosition.z);

		final SegmentSerializationBuffersGZIP bm = SegmentSerializationBuffersGZIP.get();
		Deflater deflater = bm.deflater;
		byte[] bb = bm.SEGMENT_BUFFER;
		try {
			deflater.reset();
			deflater.setInput(sectorType);
			deflater.finish();
			int zipSize = deflater.deflate(bb);
			buffer.writeInt(zipSize);
			buffer.write(bb, 0, zipSize);
		}finally {
			SegmentSerializationBuffersGZIP.free(bm);
		}

		buffer.writeShort(terrestrialsDetails.size()); //there physically cannot be any more celestial objects than a short can represent; realistically there should never be more than approx. 15 per system at worst (and usually no more than 6-7, often less). Honestly a byte would be fine here
		for(int i : terrestrialsDetails.keySet()){
			buffer.writeInt(i);
			terrestrialsDetails.get(i).toTagStructure(true).serializeNT(buffer); //(TODO merge these with short object type headers and a custom packer built in VoidSystemObject rather than tags)
		}

		buffer.writeShort(gasPlanetsDetails.size()); //likewise
		for(int i : gasPlanetsDetails.keySet()){
			buffer.writeInt(i);
			gasPlanetsDetails.get(i).toTagStructure(true).serializeNT(buffer);
		}
		//		for(int i = 0; i<sectorType.length; i++){
		//
		//			buffer.writeByte(sectorType[i]);
		//		}

	}

	public void updateServer() throws IOException {

		long t = System.currentTimeMillis();

		gasPlanetsDetails.clear();
		terrestrialsDetails.clear();

		GameServerState state = (GameServerState) player.getState();
		Vector3i plrSec = new Vector3i(player.getCurrentSector());

		basePosition.set(plrSec);
		sectorId = player.getCurrentSectorId();

		state.getUniverse().updateProximitySectorInformation(plrSec);
		int i = 0;
		Vector3i pos = new Vector3i();
		long minZ = Math.min((long) plrSec.z - (long) PROXIMITY_SIZE, (long) plrSec.z + (long) PROXIMITY_SIZE);
		long minY = Math.min((long) plrSec.y - (long) PROXIMITY_SIZE, (long) plrSec.y + (long) PROXIMITY_SIZE);
		long minX = Math.min((long) plrSec.x - (long) PROXIMITY_SIZE, (long) plrSec.x + (long) PROXIMITY_SIZE);

		long maxZ = Math.max((long) plrSec.z - (long) PROXIMITY_SIZE, (long) plrSec.z + (long) PROXIMITY_SIZE);
		long maxY = Math.max((long) plrSec.y - (long) PROXIMITY_SIZE, (long) plrSec.y + (long) PROXIMITY_SIZE);
		long maxX = Math.max((long) plrSec.x - (long) PROXIMITY_SIZE, (long) plrSec.x + (long) PROXIMITY_SIZE);

		for (long z = minZ; z < maxZ; z++) {
			for (long y = minY; y < maxY; y++) {
				for (long x = minX; x < maxX; x++) {
					pos.set((int) x, (int) y, (int) z);
					VoidSystem sys = (VoidSystem)state.getUniverse().getStellarSystemFromSecPos(pos);

					sectorType[i] = (byte) sys.getSectorType(pos).ordinal();
					if (sys.getSectorType(pos) == SectorType.PLANET) {
						sectorType[ALEN + i] = (byte) sys.getPlanetType(pos).ordinal();
						terrestrialsDetails.put(i,sys.getPlanetInfo(pos)); //TODO: instead of tag structure, consider a custom byte squish/desquish in void system objects, complete with an object type ID registry (mod expandable)
					}
					else if(sys.getSectorType(pos) == SectorType.GAS_PLANET) {
						sectorType[ALEN + i] = (byte) sys.getGasPlanetType(pos).ordinal();
						gasPlanetsDetails.put(i,sys.getGasPlanetInfo(pos));
					}
					else {
						sectorType[ALEN + i] = (byte) sys.getSpaceStationTypeType(pos).ordinal();
					}
					i++;
				}
			}
		}
		assert (i == ALEN) : i + "/" + ALEN;
		player.getNetworkObject().proximitySector.setChanged(true); //TODO: these proximity changes could reasonably have their own API event on server
		long took = System.currentTimeMillis() - t;

		if (took > 10) {
			System.err.println("[SERVER] WARNING: UPDATING SERVER SECTORPROXMITY TOOK " + took);
		}
	}
}
