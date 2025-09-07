package org.schema.game.common.data.world;

import org.schema.common.util.ByteUtil;
import org.schema.game.client.controller.element.world.ClientSegmentProvider;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SegmentDataMetaData;
import org.schema.game.common.controller.io.SegmentDataIONew;
import org.schema.game.common.controller.io.SegmentDummyProvider;
import org.schema.game.common.controller.io.SegmentSerializationBuffers;
import org.schema.game.common.controller.io.SegmentSerializationBuffersGZIP;
import org.schema.game.server.data.GameServerState;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class RemoteSegment extends Segment {

	public static final byte NEW_REQUEST = 0;
	public static final byte LOCAL_SIGNATURE_REQUESTED = 1;
	public static final byte LOCAL_SIGNATURE_RECEIVED = 2;
	public static final byte SEGMENT_REQUESTED = 3;
	public static final byte SEGMENT_RECEIVED = 4;
	public static final byte SEGMENT_READY_TO_ADD = 5;
	public static final byte SEGMENT_ADDED = 10;
	public static final byte DATA_AVAILABLE_BYTE = 1;
	public static final byte DATA_EMPTY_BYTE = 2;
	public Object writingToDiskLock = new Object();
	public long lastLocalTimeStamp = -1;
	public byte requestStatus = NEW_REQUEST;
	public short lastSize;
	public long timestampTmp;
	public long lastChangedDeserialized;
	public SegmentDataMetaData buildMetaData;
	public boolean needsGeneration;
	public boolean compressionCheck = true;
	//	private long lastChanged;
	private boolean dirty;
	private boolean revalidating;
	private boolean deserializing;


	public RemoteSegment(SegmentController segmentController) {
		super(segmentController);
	}

	@Override
	public void dataChanged(boolean addedDeleted) {
		dirty = addedDeleted;
	}


	@Override
	public String toString() {
		return getSegmentController() + "(" + pos + ")[" + (getSegmentData() != null ? "s" + getSegmentData().getSize() : "e") + "; Hash" + this.hashCode() + "; id " + id + "]";
	}


	/**
	 * @param dataInputStream
	 * @param size
	 * @param ignoreName
	 * @param reset
	 * @param compression
	 * @return true, if segment need resave (loaded from old version, etc...)
	 * @throws IOException
	 * @throws DeserializationException
	 */
	public boolean deserialize(DataInputStream dataInputStream, int size, boolean ignoreName, boolean reset, long time) throws IOException, DeserializationException {
		int version = dataInputStream.readByte();

		long lastChanged = dataInputStream.readLong();

		if(lastChanged > getSegmentController().getState().getUpdateTime() + 20) {
			System.err.println("[IO] WARNING Deserialize Read last changed from the future future " + getSegmentController() + "; " + System.currentTimeMillis() + "; " + lastChanged);
			lastChanged = System.currentTimeMillis() - 1000;
		}

		int x = dataInputStream.readInt();
		int y = dataInputStream.readInt();
		int z = dataInputStream.readInt();

		if(ignoreName) {
			setPos(x, y, z);
		}

		assert (ignoreName || pos.x == x && pos.y == y && pos.z == z) : " deserialized " + x + ", " + y + ", " + z + "; toSerialize " + pos + " on " + getSegmentController();

		byte dataByte = dataInputStream.readByte();


		if(dataByte != DATA_EMPTY_BYTE) {


			SegmentData.SegmentDataType dataType = SegmentData.SegmentDataType.getByNetworkId(dataByte);
			final int oldVersion = version;
			version = SegmentDataIONew.checkVersionSkip(version, dataType);

			this.deserializing = true;

			if(dataType != SegmentData.SegmentDataType.FOUR_BYTE) {
				//special segment
				final SegmentSerializationBuffersGZIP bm = SegmentSerializationBuffersGZIP.get();
				try {
					final boolean onServer = getSegmentController() == null || getSegmentController().isOnServer();
					SegmentData data = dataType.instantiate(!getSegmentController().isOnServer());
					data.deserializeRemoteSegment(dataInputStream);
					dataType.received++;

					byte[] buffer;
					buffer = bm.SEGMENT_BUFFER;
					if(version < SegmentDataIONew.VERSION) {
						SegmentDataInterface dummy;
						dummy = bm.dummies.get(version);
						assert (version == 4) : version + "; " + SegmentDataIONew.VERSION + "; " + data.getClass().getSimpleName() + "; " + dummy.getClass().getSimpleName();
						data.copyTo(dummy);
						lastChanged = getSegmentController().getState().getUpdateTime();
						while(version < SegmentDataIONew.VERSION) {

							//migrate to new format. dummies are synched under buffer
							if(version < SegmentDataIONew.VERSION - 1) {
								SegmentDataInterface dummyNext;
								if(getSegmentController() == null || getSegmentController().isOnServer()) {
									dummyNext = bm.dummies.get(version + 1);
								} else {
									dummyNext = bm.dummies.get(version + 1);
								}
								System.err.println("MIGRATING: " + version + " -> " + (version + 1));
								dummy.migrateTo(version, dummyNext);
								try {
									dummy.resetFast();
								} catch(SegmentDataWriteException e) {
									throw new RuntimeException("Should not happen here", e);
								}
								dummy = dummyNext;
							} else {

								data = new SegmentData4Byte(!onServer);
								dummy.migrateTo(version, data);
								System.err.println("::::::::::: DATA MIGRATED FROM BITMAP SEGMENT DATA VERSION " + oldVersion + " -> " + version + ": " + data);
								try {
									dummy.resetFast();
								} catch(SegmentDataWriteException e) {
									throw new RuntimeException("Should not happen here", e);
								}

							}

							version++;
						}

						//mark changed so it is saved in the new format
						lastLocalTimeStamp = System.currentTimeMillis();

						if(data instanceof SegmentData4Byte) {
							((SegmentData4Byte) data).needsBitmapCompressionCheck = true;
						}
					}
					if(getSegmentData() == null) {
						data.assignData(this);
					} else {
						synchronized(getSegmentController().getState()) {
							System.err.println(getSegmentController() + " USING ALREADY ASSIGNED SEGMENT DATA " + pos);
							//unvalidate data and set new
							getSegmentController().getSegmentProvider().purgeSegmentData(this, getSegmentData(), false);
							setSegmentData(null);
							data.assignData(this);
						}
					}
				} finally {
					SegmentSerializationBuffersGZIP.free(bm);
				}
//				assert(getSegmentData().getClass() == dataType.associatedClass);
			} else {
				boolean preExistingSegData = false;
				if(this != ClientSegmentProvider.dummySegment) {
					if(getSegmentData() == null) {
						SegmentData freeSegmentData = getSegmentController().getSegmentProvider().getFreeSegmentData();
						freeSegmentData.assignData(this);
						assert ((getSegmentData() instanceof SegmentData4Byte)) : getSegmentData().getClass().getSimpleName() + "; " + getSegmentData();
						//				assert(freeSegmentData.checkEmpty());
						//				System.err.println("FREE SEGMENT DATA AQUIRED");
					} else {
						preExistingSegData = true;
						try {
							getSegmentData().checkWritable();
						} catch(SegmentDataWriteException e) {
							SegmentDataWriteException.replaceData(this);
						}
						assert ((getSegmentData() instanceof SegmentData4Byte)) : getSegmentData().getClass().getSimpleName() + "; " + getSegmentData();
					}
				}

				if(version < SegmentData.VERSION) {
					lastChanged = deserializeOldGZip(version, preExistingSegData, reset, lastChanged, dataInputStream);
				} else {
					lastChanged = deserializeLZ4(version, preExistingSegData, reset, lastChanged, dataInputStream);
				}

				if(lastChanged < 0) {
					return false;
				}

			}
			if(compressionCheck && getSegmentData() != null && getSegmentData() instanceof SegmentData4Byte && getSegmentData().onServer) {
				SegmentData newData = getSegmentData().doBitmapCompressionCheck(this);
				if(newData != getSegmentData()) {
					newData.setNeedsRevalidate(getSegmentData().needsRevalidate());
					newData.revalidatedOnce = getSegmentData().revalidatedOnce;

					//free and replace
					System.err.println("[SEGMENT] REPLACING SEGMENT DATA FOR OPTIMIZED SEGMENT " + newData);
					synchronized(getSegmentController().getSegmentProvider().getSegmentDataManager()) {
						getSegmentController().getSegmentProvider().getSegmentDataManager().addToFreeSegmentData(getSegmentData(), true, true);
					}
					newData.assignData(this);

				}
			}

			getSegmentData().setNeedsRevalidate(true);
		} else {
			assert (dataByte == DATA_EMPTY_BYTE) : dataByte + "/" + DATA_EMPTY_BYTE + ": " + x + ", " + y + ", " + z + "; byte size: " + size;
			//Received empty segment
		}
		assert (isEmpty() || (getSegmentData() != null && getSegmentData().needsRevalidate())) : getSegmentData().getClass() + "; " + getSegmentData();


//		lastChanged = System.currentTimeMillis();
//		getSegmentData().repairAll();

		lastChangedDeserialized = lastChanged;
		if(getSegmentController() != null) {

			setLastChanged(lastChanged);
		}
		this.deserializing = false;

		return version < SegmentDataIONew.VERSION;

	}


	private long deserializeLZ4(int version, boolean preExistingSegData, boolean reset, long lastChanged, DataInputStream dataInputStream) throws IOException {
		SegmentDataInterface dummy;
		final int compressedSize = dataInputStream.readInt();
		if(getSegmentController() == null || getSegmentController().isOnServer()) {
			GameServerState.dataReceived += compressedSize;
		} else {
			GameClientState.dataReceived += compressedSize;
		}
		if(this == ClientSegmentProvider.dummySegment) {
			System.err.println("[CLIENT] USED DUMMY SEGMENT TO INTERCEPT STREAM");
			return -1L;
		}
		final SegmentSerializationBuffers bm = SegmentSerializationBuffers.get();
		try {
			ReadableByteChannel channel = Channels.newChannel(dataInputStream);
			bm.clear(); //clear both byte buffers

			channel.read(bm.compressed);
			bm.compressed.rewind();
			bm.compressed.limit(SegmentData.TOTAL_SIZE_BYTES);

			bm.uncompressed.limit(SegmentData.TOTAL_SIZE_BYTES);
			bm.uncompress();

//			System.err.println("decompressed: "+compressedSize+" -> "+decompressed);

			//flip doesn't work here as it is not sufficient
			bm.uncompressed.rewind();

			try {
				if(preExistingSegData) {
					getSegmentData().rwl.writeLock().lock();
				}
				resetIfPreexising(preExistingSegData, reset);

				int inflate = 0;
				if(version < SegmentDataIONew.VERSION) {
					dummy = bm.dummies.get(version);
					inflate = dummy.readFrom(bm.uncompressed);

					lastChanged = getSegmentController().getState().getUpdateTime();
					migrate(version, dummy, bm);

					//mark changed so it is saved in the new format
					lastLocalTimeStamp = System.currentTimeMillis();
				} else {
					//normal: right version
					inflate = getSegmentData().readFrom(bm.uncompressed);

				}

				if(inflate == 0) {
					System.err.println("WARNING: LZ4 INFLATED BYTES 0: ");
				}

			} catch(SegmentDataWriteException e) {
				e.printStackTrace();
				assert (false) : "This should never happen";
			} finally {
				if(preExistingSegData) {
					getSegmentData().rwl.writeLock().unlock();
				}
			}

		} finally {
			SegmentSerializationBuffers.free(bm);
		}

		return lastChanged;
	}

	private long deserializeOldGZip(int version, boolean preExistingSegData, boolean reset, long lastChanged, DataInputStream dataInputStream) throws IOException {
		final SegmentSerializationBuffersGZIP bm = SegmentSerializationBuffersGZIP.get();
		try {
			Inflater inflater;
			byte[] buffer;
			SegmentDataInterface dummy;
			Long dataReceived;
			byte[] byteFormatBuffer;
			buffer = bm.SEGMENT_BUFFER;


			int inflatedSize = dataInputStream.readInt();
			if(getSegmentController() == null || getSegmentController().isOnServer()) {
				GameServerState.dataReceived += inflatedSize;
			} else {
				GameClientState.dataReceived += inflatedSize;
			}
			assert (inflatedSize <= buffer.length) : inflatedSize + "/" + buffer.length;

			byteFormatBuffer = bm.SEGMENT_BYTE_FORMAT_BUFFER;
			inflater = bm.inflater;

			int read = dataInputStream.read(buffer, 0, inflatedSize);

			if(read != inflatedSize) {
				throw new DeserializationException(read + "; " + inflatedSize + "; " + buffer.length + "; " + this + "; " + getSegmentController() + "; " + getSegmentController().isOnServer());
			}
			if(this == ClientSegmentProvider.dummySegment) {
				System.err.println("[CLIENT] USED DUMMY SEGMENT TO INTERCEPT STREAM");
				return -1L;
			}
			inflater.reset();

			inflater.setInput(buffer, 0, inflatedSize);


			try {
				if(preExistingSegData) {
					getSegmentData().rwl.writeLock().lock();
				}
				resetIfPreexising(preExistingSegData, reset);

				int inflate = 0;
				if(version < SegmentDataIONew.VERSION) {
					dummy = bm.dummies.get(version);
					inflate = inflate(dummy, inflater, inflatedSize, byteFormatBuffer.clone());

					lastChanged = getSegmentController().getState().getUpdateTime();
					migrate(version, dummy, bm);

					//mark changed so it is saved in the new format
					lastLocalTimeStamp = System.currentTimeMillis();
				} else {
					//normal: right version
					inflate = inflate(getSegmentData(), inflater, inflatedSize, byteFormatBuffer);
				}

				if(inflate == 0) {
					System.err.println("WARNING: INFLATED BYTES 0: " + inflater.needsInput() + " " + inflater.needsDictionary());
				}

			} catch(DataFormatException e) {
				e.printStackTrace();
			} finally {
				if(preExistingSegData) {
					getSegmentData().rwl.writeLock().unlock();
				}
			}
		} finally {
			SegmentSerializationBuffersGZIP.free(bm);
		}
		return lastChanged;
	}

	private int inflate(SegmentDataInterface s, Inflater inflater, int inflatedSize, byte[] byteFormatBuffer) throws DataFormatException {
		try {
			return s.inflate(inflater, byteFormatBuffer);
		} catch(SegmentInflaterException e) {
			System.err.println("[INFLATER] Exception: " + getSegmentController().getState() + " size received: " + inflatedSize + ": " + e.inflate + "/" + e.shouldBeInflate + " for " + getSegmentController() + " pos " + pos);
			e.printStackTrace();
		} catch(SegmentDataWriteException e) {
			if(this != ClientSegmentProvider.dummySegment) {
				throw new RuntimeException(e);
			} else {
				e.printStackTrace();
			}

		}
		return 0;
	}

	private void resetIfPreexising(boolean preExistingSegData, boolean reset) {
		if(preExistingSegData && reset) {
			try {
				if(getSegmentData().getSize() > 0) {
					System.err.println("[SEGMENT][DESERIALIZE] WARN: RECEIVING OVER NON-EMPTY SEGMENTDATA " + pos);
					getSegmentData().resetFast();
				}

			} catch(SegmentDataWriteException e) {
				if(this != ClientSegmentProvider.dummySegment) {
					throw new RuntimeException(e);
				} else {
					e.printStackTrace();
				}
			}
		}
	}

	private void migrate(int version, SegmentDataInterface dummy, SegmentDummyProvider bm) {
		while(version < SegmentDataIONew.VERSION) {

			//migrate to new format. dummies are synched under buffer
			if(version < SegmentData.VERSION - 1) {
				SegmentDataInterface dummyNext;
				dummyNext = bm.getDummies().get(version + 1);
				System.err.println("MIGRATING: " + version + " -> " + (version + 1));
				dummy.migrateTo(version, dummyNext);
				try {
					dummy.resetFast();
				} catch(SegmentDataWriteException e) {
					throw new RuntimeException("Should not happen here", e);
				}
				dummy = dummyNext;
			} else {
				System.err.println("LAST MIGRATE: " + dummy.getClass().getSimpleName() + " -> " + getSegmentData().getClass().getSimpleName());
				dummy.migrateTo(version, getSegmentData());
				try {
					dummy.resetFast();
				} catch(SegmentDataWriteException e) {
					throw new RuntimeException("Should not happen here", e);
				}

			}
			version++;
		}
	}

	public long getLastChanged() {
		return getSegmentController().getSegmentBuffer().getLastChanged(pos);
	}

	/**
	 * set to -1 when a segment timestamp was received from server
	 * that is newer than the clients version.
	 * All segments with lastChangedState == -1 will be re-requested from server
	 *
	 * @param lastChangedState percentage of last change
	 */
	public void setLastChanged(long lastChanged) {
//		CHECK DEAKLOCK HERE FROM LOADING THREADS FROM POOL
		getSegmentController().getSegmentBuffer().setLastChanged(pos, lastChanged);
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


	public int serialize(DataOutputStream oStream, boolean active, long lastChanged, byte version) throws IOException {
		DataOutputStream wrap = new DataOutputStream(oStream);
		assert (wrap.size() == 0);
		int totalSize = 0;

		/*
		 * write negative of version so we can later determine
		 * if a version was written by checking if the first byte is
		 * negative (it will never be negative otherwise as
		 * timestamps never have any negative bytes)
		 */
		assert (version > 0);
		wrap.writeByte(version);

		wrap.writeLong(lastChanged);

		wrap.writeInt(pos.x);
		wrap.writeInt(pos.y);
		wrap.writeInt(pos.z);


		totalSize += 8 + 4 + 4 + 4 + 1;
		assert (wrap.size() == totalSize) : wrap.size() + "/" + totalSize;
		int zipSize = 0;

		if(active) {
			if(getSegmentData() != null && !isEmpty()) {

				totalSize += 1;
				wrap.writeByte(getSegmentData().getDataType().networkTypeId);

				if(getSegmentData().getDataType() != SegmentData.SegmentDataType.FOUR_BYTE) {
//					System.err.println("SENDING SPECIAL: "+pos+" "+getSegmentData().getClass().getSimpleName()+"; "+getSize()+"; "+getSegmentData().getSize()+"; NTID: "+getSegmentData().getDataType().networkTypeId);
					//special serialization
					totalSize += getSegmentData().serializeRemoteSegment(wrap);
				} else {
					if(version < 7) {
						totalSize = serializeOldGZIP(wrap, totalSize);
					} else {
						totalSize = serializeLZ4(wrap, totalSize);
					}
					assert (wrap.size() == totalSize) : wrap.size() + "/" + totalSize;
				}
			} else {
				assert (isEmpty());
				// segData == null -> memorySaveMode must be on
				totalSize += 1;
				//								System.err.println("WRITING EMPTY (empty) "+pos+" "+getSegmentController()+"; "+getSegmentData()+"; empty: "+isEmpty());
				wrap.writeByte(DATA_EMPTY_BYTE);
				//				 System.err.println(getSegmentController().getState()+" data EMPTY for "+pos);

			}
		} else {
			totalSize += 1;
			//						System.err.println("WRITING EMPTY (inactive) "+pos+" "+getSegmentController()+"; "+getSegmentData()+"; empty: "+isEmpty());
			wrap.writeByte(DATA_EMPTY_BYTE);
		}
		assert (wrap.size() == totalSize) : wrap.size() + "/" + totalSize;
		/*
		 * the size of the actual (comrpressed) written bytes.
		 * zip.size wouldnt give the right size,
		 * since it counts all incoming bytes and not that written to
		 * the underlying zip stream
		 */
		return wrap.size(); //timestamp, x, y, z, dataByte
	}

	private int serializeLZ4(DataOutputStream wrap, int totalSize) throws IOException {

		final SegmentSerializationBuffers bm = SegmentSerializationBuffers.get();
		try {
			//should be a given
			bm.ensureSegmentBufferSize(SegmentData.TOTAL_SIZE * ByteUtil.SIZEOF_INT, SegmentData.TOTAL_SIZE * ByteUtil.SIZEOF_INT);

			bm.clear(); //clear both byte buffers
			try {
				getSegmentData().rwl.readLock().lock();
				bm.uncompressed.rewind();
				bm.uncompressed.limit(SegmentData.TOTAL_SIZE_BYTES);
				getSegmentData().copyTo(bm.uncompressed);
				bm.uncompressed.rewind();
			} finally {
				getSegmentData().rwl.readLock().unlock();
			}
			bm.compress();
			int zippedSize = bm.compressed.position();

			//flip() is doesn't work here since compression works on memory only and doesn't touch the byteBuffer position

			bm.compressed.rewind();
			bm.compressed.limit(zippedSize);

//			System.err.println("CURRENT ZIPPED: "+zippedSize);

			wrap.writeInt(totalSize);
			totalSize += ByteUtil.SIZEOF_INT;

			WritableByteChannel channel = Channels.newChannel(wrap);

			channel.write(bm.compressed);
			totalSize += zippedSize;


		} finally {
			SegmentSerializationBuffers.free(bm);
		}

		return totalSize;
	}


	private int serializeOldGZIP(DataOutputStream wrap, int totalSize) throws IOException {

		final SegmentSerializationBuffersGZIP bm = SegmentSerializationBuffersGZIP.get();
		try {
			//normal array serialization
			byte[] byteFormatBuffer = bm.SEGMENT_BYTE_FORMAT_BUFFER;
			byte[] buffer = bm.SEGMENT_BUFFER;
			Deflater deflater = bm.deflater;
			assert (wrap.size() == totalSize) : wrap.size() + "/" + totalSize;

			//lock segment for reading
			int zipSize = 0;
			try {
				getSegmentData().rwl.readLock().lock();
				deflater.reset();
				deflater.setInput(getSegmentData().getAsByteBuffer(byteFormatBuffer));
				deflater.finish();
				zipSize = deflater.deflate(buffer);

			} finally {
				getSegmentData().rwl.readLock().unlock();
			}

			assert (zipSize > 0) : "[DEFLATER] DELFLATED SIZE: " + zipSize + " for: " + pos + " " + getSegmentController() + ": SData Size: " + getSegmentData().getSize();
			//					System.err.println("[DEFLATER] DELFLATED SIZE: "+zipSize+" for: "+pos+" "+getSegmentController()+": SData Size: "+getSegmentData().getSize());

			wrap.writeInt(zipSize);
			totalSize += ByteUtil.SIZEOF_INT;
			assert (wrap.size() == totalSize) : wrap.size() + "/" + totalSize;

			wrap.write(buffer, 0, zipSize);

			totalSize += zipSize;
		} finally {
			SegmentSerializationBuffersGZIP.free(bm);
		}
		return totalSize;
	}

	public void unvalidate(long time) {
		revalidating = true;
		SegmentData segmentData = getSegmentData();
		if(segmentData != null) {
			segmentData.unvalidateData(time);

		}
		revalidating = false;
	}


}
