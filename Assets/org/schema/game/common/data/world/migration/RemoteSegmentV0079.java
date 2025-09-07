package org.schema.game.common.data.world.migration;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.DeserializationException;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataWriteException;

public class RemoteSegmentV0079 extends Segment {

	public static final byte DATA_AVAILABLE_BYTE = 1;
	private static final byte DATA_EMPTY_BYTE = 2;
	public Object writingToDiskLock = new Object();
	private long lastChanged;
	private boolean dirty;
	private long requestTime = -1;
	private boolean revalidating;
	private boolean deserializing;

	public RemoteSegmentV0079(SegmentController segmentController) {
		super(segmentController);
	}

	@Override
	public void dataChanged(boolean addedDeleted) {
		dirty = addedDeleted;
	}

	@Override
	public String toString() {
		return getSegmentController() + "(" + pos + ")[Hash" + this.hashCode() + "; " + (getSegmentData() != null ? getSegmentData().getSize() : "e") + "]";
	}

	public void deserialize(DataInputStream dataInputStream, int size, boolean ignoreName, long time) throws IOException, DeserializationException {
		DataInputStream zip;
		if (size < 0) {
			zip = new DataInputStream(new GZIPInputStream(dataInputStream));
		} else {
			zip = new DataInputStream(new GZIPInputStream(dataInputStream, size));
		}
		//		System.err.println("READING "+getSegmentController()+"; "+getSegmentController().getUniqueIdentifier()+"; "+pos);
		long lastChanged = zip.readLong();

		int x = zip.readInt();
		int y = zip.readInt();
		int z = zip.readInt();

		assert (ignoreName || pos.x == x && pos.y == y && pos.z == z) : " deserialized " + x + ", " + y + ", " + z + "; toSerialize " + pos;

		byte dataByte = zip.readByte();

		boolean needsReset = false;
		if (dataByte == DATA_AVAILABLE_BYTE) {
			if (getSegmentData() == null) {
				SegmentData freeSegmentData = getSegmentController().getSegmentProvider().getFreeSegmentData();
				freeSegmentData.assignData(this);
			} else {

				needsReset = true;
			}
			this.deserializing = true;
			try{
				synchronized (getSegmentData()) {
					if (needsReset) {
						getSegmentData().reset(time);
					}
					getSegmentData().deserialize(zip, time);
				}
			} catch (SegmentDataWriteException e) {
				e.printStackTrace();
				throw new RuntimeException("this should be never be thrown as migration should always be to"
						+ "a normal segment data", e);
			}

		} else {
			assert (dataByte == DATA_EMPTY_BYTE);
			//Received empty segment
		}
		this.lastChanged = lastChanged;
		this.deserializing = false;

		try {
			int read = zip.read();
			if (read != -1) {
				throw new DeserializationException("EoF not reached: " + read + " - size given: " + size);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new DeserializationException("[WARNING][DESERIALIZE] " + getSegmentController().getState() + ": " + getSegmentController() + ": " + pos + ": " + e.getMessage());
		}

	}

	public long getLastChanged() {
		return lastChanged;
	}

	/**
	 * set to -1 when a segment timestamp was received from server
	 * that is newer than the clients version.
	 * All segments with lastChangedState == -1 will be re-requested from server
	 *
	 * @param lastChangedState percentage of last change
	 */
	public void setLastChanged(long lastChanged) {
		this.lastChanged = lastChanged;
	}

	/**
	 * @return the requestTime
	 */
	public long getRequestTime() {
		return requestTime;
	}

	/**
	 * @param requestTime the requestTime to set
	 */
	public void setRequestTime(long requestTime) {
		this.requestTime = requestTime;
	}

	/**
	 * @return the deserializing
	 */
	public boolean isDeserializing() {
		return deserializing;
	}

	/**
	 * @param deserializing the deserializing to set
	 */
	public void setDeserializing(boolean deserializing) {
		this.deserializing = deserializing;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	/**
	 * @return the revalidating
	 */
	public boolean isRevalidating() {
		return revalidating;
	}

	/**
	 * @param revalidating the revalidating to set
	 */
	public void setRevalidating(boolean revalidating) {
		this.revalidating = revalidating;
	}

//	public int serialize(DataOutputStream outputStream, boolean active) throws IOException {
//		DataOutputStream wrap = new DataOutputStream(outputStream);
//		GZIPOutputStream zz;
//		DataOutputStream zip = new DataOutputStream(zz = new GZIPOutputStream(wrap));
//
//		zip.writeLong(getLastChanged());
//
//		zip.writeInt(pos.x);
//		zip.writeInt(pos.y);
//		zip.writeInt(pos.z);
//
//		if (active) {
//			if (getSegmentData() != null && !isEmpty()) {
//				//				 System.err.println(getSegmentController().getState()+" data AVAILABLE for "+pos);
//				zip.writeByte(DATA_AVAILABLE_BYTE);
//				synchronized (getSegmentData()) {
//					getSegmentData().serialize(zip);
//				}
//			} else {
//				assert (isEmpty());
//				// segData == null -> memorySaveMode must be on
//				zip.writeByte(DATA_EMPTY_BYTE);
//				//				 System.err.println(getSegmentController().getState()+" data EMPTY for "+pos);
//
//			}
//		} else {
//			zip.writeByte(DATA_EMPTY_BYTE);
//		}
//		zz.finish();
//		zz.flush();
//
//
//		/*
//		 * the size of the actual (comrpressed) written bytes.
//		 * zip.size wouldnt give the right size,
//		 * since it counts all incoming bytes and not that written to
//		 * the underlying zip stream
//		 */
//		return wrap.size();
//	}

}
