package org.schema.game.client.controller.manager.ingame;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.FastEntrySet;
import it.unimi.dsi.fastutil.shorts.Short2IntOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import org.schema.common.SerializationInterface;
import org.schema.common.util.ByteUtil;
import org.schema.game.client.controller.manager.ingame.BuildInstruction.Remove;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.SegmentRetrieveCallback;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.DrawableRemoteSegment;
import org.schema.game.common.data.world.RemoteSegment;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData4Byte;
import org.schema.game.common.util.FastCopyLongOpenHashSet;
import org.schema.schine.resource.tag.SerializableTagElement;
import org.schema.schine.resource.tag.SerializableTagFactory;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class BlockBuffer implements SerializationInterface, SerializableTagElement{
	public final ShortArrayList positions = new ShortArrayList();
	public final IntArrayList data = new IntArrayList();
	public final BooleanArrayList meta = new BooleanArrayList();
	public final LongArrayList controller = new LongArrayList();
	public final LongArrayList connectedFromThis = new LongArrayList();
	private final SegmentRetrieveCallback tmpCb = new SegmentRetrieveCallback();
	private float volumeAdded;
	private boolean serializing;
	
	public void record(SegmentPiece where, long controller, LongCollection connectedFromThis) {
		assert(!serializing);
		assert(!where.getSegmentController().isMarkedForPermanentDelete());
		assert(!where.getSegmentController().isMarkedForDeleteVolatile());
		positions.add((short)where.getAbsolutePosX());
		positions.add((short)where.getAbsolutePosY());
		positions.add((short)where.getAbsolutePosZ());
		
		data.add(where.getData());
		if(controller != Long.MIN_VALUE || (connectedFromThis != null && !connectedFromThis.isEmpty())) {
			this.meta.add(true);
			this.controller.add(controller);
			if((connectedFromThis != null && !connectedFromThis.isEmpty())) {
				this.connectedFromThis.add(connectedFromThis.size());
				this.connectedFromThis.addAll(connectedFromThis);
			}else {
				this.connectedFromThis.add(0);
			}
		}else {
			meta.add(false);
		}
		
		assert(checkIntegrity());
	}
	
	private boolean checkIntegrity() {
		int c = 0;
		for(int i = 0; i < this.meta.size(); i++) {
			if(this.meta.getBoolean(i)) {
				c++;
			}
		}
		if(c > this.controller.size()) {
			System.err.println("FAILED: "+c+"; "+this.controller.size());
			return false;
		}
		return true;
	}
	public void peak(int amount, Short2IntOpenHashMap consTmp) {
		for(int i = 0; i < amount; i++) {
			int data = this.data.getInt((this.data.size()-1)-i);
			consTmp.addTo(SegmentPiece.getType(data), 1);
		}
	}
	public short peakNextType(){
		int data = this.data.getInt(this.data.size()-1);
		return SegmentPiece.getType(data);
	}
	public Remove createInstruction(SegmentController c, BuildInstruction.Remove out){
		int z = positions.removeShort(positions.size()-1);
		int y = positions.removeShort(positions.size()-1);
		int x = positions.removeShort(positions.size()-1);
		
		int data = this.data.removeInt(this.data.size()-1);
		
		boolean meta = this.meta.removeBoolean(this.meta.size()-1);
		long controller = Long.MIN_VALUE;
		if(meta) {
			
			controller = this.controller.removeLong(this.controller.size()-1);
			
			
			int toAddAmount = (int) this.connectedFromThis.removeLong(this.connectedFromThis.size());
			int toAddIndex = this.connectedFromThis.size()-1;
			for(int i = 0; i < toAddAmount; i++) {
				out.connectedFromThis.add(this.connectedFromThis.removeLong(toAddIndex--));
			}
			out.controller = controller;
		}
		
		
		c.getSegmentBuffer().get(
				ByteUtil.divUSeg(x)*Segment.DIM, 
				ByteUtil.divUSeg(y)*Segment.DIM, 
				ByteUtil.divUSeg(z)*Segment.DIM, tmpCb);
		
		boolean needsAdd = false;
		if(tmpCb.state <= 0) {

			tmpCb.segment = c.isOnServer() ? new RemoteSegment(c) : new DrawableRemoteSegment(c);
			tmpCb.segment.setPos(tmpCb.pos);
			SegmentData4Byte sData = new SegmentData4Byte(!c.isOnServer());
			sData.assignData(tmpCb.segment);
			needsAdd = true;
		}
		
		
		if(needsAdd) {
			c.getSegmentBuffer().addImmediate(tmpCb.segment);
			c.getSegmentBuffer().updateBB(tmpCb.segment);
		}
		
		out.where = new SegmentPiece(tmpCb.segment, (byte)ByteUtil.modUSeg(x), 
				(byte)ByteUtil.modUSeg(y), 
				(byte)ByteUtil.modUSeg(z));
		out.where.setDataByReference(data);

		return out;
	}
	public void recordRemove(SegmentPiece piece) {
		long index = piece.getAbsoluteIndex();
		short type = piece.getType();
		long controller = Long.MIN_VALUE;
		long index4 = ElementCollection.getIndex4(index, type);
		Long2ObjectOpenHashMap<FastCopyLongOpenHashSet> all = piece.getSegmentController().getControlElementMap().getControllingMap().getAll();
		if(ElementInformation.canBeControlledByAny(type)) {
			
			FastEntrySet<FastCopyLongOpenHashSet> long2ObjectEntrySet = all.long2ObjectEntrySet();
			for ( Long2ObjectMap.Entry<FastCopyLongOpenHashSet> e : long2ObjectEntrySet) {
				if (e.getValue().contains(index4)) {
					controller = e.getLongKey();
					break;
				}
			}
		}
		LongOpenHashSet connectedFromThis = all.get(index);
        if (type != 0) {
            this.volumeAdded -= ElementKeyMap.getInfo(type).getVolume();
        }
        
        record(piece, controller, connectedFromThis);
	}
	public void clear() {
		assert(!serializing);
		positions.clear();
		data.clear();
		meta.clear();
		controller.clear();
		connectedFromThis.clear();
		volumeAdded = 0;		
	}

	public int size() {
		return meta.size();
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
//		GZIPOutputStream gzipOutputStream = new GZIPOutputStream(b);
//		DataOutputStream m = new DataOutputStream(new BufferedOutputStream(gzipOutputStream));
		this.serializing = true;
		serialize(b);
		this.serializing = false;
//		m.flush();
//		gzipOutputStream.finish();
//		gzipOutputStream.flush();
	}
	private void serialize(DataOutput m) throws IOException {
		final int size = meta.size();
		final int controllerSize = this.controller.size();
		final int conSize = this.connectedFromThis.size();
		
		
		m.writeInt(size);
		m.writeInt(controllerSize);
		m.writeInt(conSize);
		int c = 0;
		int conC = 0;
		for(int i = 0; i < size; i++) {
			m.writeShort(positions.getShort(i*3+0));
			m.writeShort(positions.getShort(i*3+1));
			m.writeShort(positions.getShort(i*3+2));
			m.writeInt(data.getInt(i));
			boolean meta = this.meta.getBoolean(i);
			m.writeBoolean(meta);
			if(meta) {
				m.writeLong(controller.getLong(c));
				int mSize = (int)connectedFromThis.getLong(conC);
				conC++;
				m.writeInt(mSize);
				for(int j = 0; j < mSize; j++) {
					m.writeLong(connectedFromThis.getLong(conC));
					conC++;
				}
				c++;
			}
		}
//		System.err.println("SERIALIZED:::::::::::: : "+positions.size());
	}
	private void deserialize(DataInput in) throws IOException {
		final int size = in.readInt();
		final int controllerSize = in.readInt();
		final int conSize = in.readInt();
		
		//pre-allocate lists to not waste cpu/memory
		positions.ensureCapacity(size*3);
		data.ensureCapacity(size);
		meta.ensureCapacity(size);
		controller.ensureCapacity(controllerSize);
		connectedFromThis.ensureCapacity(conSize);
		
		for(int i = 0; i < size; i++) {
			positions.add(in.readShort());
			positions.add(in.readShort());
			positions.add(in.readShort());
			data.add(in.readInt());
			boolean meta = in.readBoolean();
			this.meta.add(meta);
			if(meta) {
				controller.add(in.readLong());
				int mSize = in.readInt();
				connectedFromThis.add(mSize);
				for(int j = 0; j < mSize; j++) {
					connectedFromThis.add(in.readLong());
				}
			}
		}
	}
	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
//		GZIPInputStream gzipInputStream = new GZIPInputStream(b);
//		DataInputStream in = new DataInputStream(new BufferedInputStream(gzipInputStream));
		deserialize(b);
	}
	
	public static BlockBuffer fromTag(Tag t) {
		return (BlockBuffer)t.getValue();
	}
	public Tag getTag() {
		return new Tag(Type.SERIALIZABLE, null, this);
	}

	@Override
	public byte getFactoryId() {
		return SerializableTagElement.BLOCK_BUFFER;
	}

	@Override
	public void writeToTag(DataOutput dos) throws IOException {
		serialize(dos, false);
	}
	
	public static class BlockBufferFactory implements SerializableTagFactory{

		@Override
		public Object create(DataInput dis) throws IOException {
			BlockBuffer b = new BlockBuffer();
			b.deserialize((DataInput)dis, 0, false);
			return b;
		}
		
	}

	
}
