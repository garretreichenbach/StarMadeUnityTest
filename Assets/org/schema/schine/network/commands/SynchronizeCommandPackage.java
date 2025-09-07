package org.schema.schine.network.commands;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

import org.schema.schine.network.DataInputStreamPositional;
import org.schema.schine.network.DataOutputStreamPositional;
import org.schema.schine.network.common.commands.CommandPackage;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;




public abstract class SynchronizeCommandPackage extends CommandPackage{

	private FastByteArrayOutputStream fb;
	public DataOutputStreamPositional out;
	
	private FastByteArrayInputStream fi;
	public DataInputStreamPositional in;

	private static List<FastByteArrayOutputStream> outPool = new ObjectArrayList<>();
	private static List<DataOutputStreamPositional> outPoolFB = new ObjectArrayList<>();
	
	private static List<FastByteArrayInputStream> inPool = new ObjectArrayList<>();
	private static List<DataInputStreamPositional> inPoolFB = new ObjectArrayList<>();
	
	public boolean manualClear = false;
	public static boolean poolingBuffersSend = true;
	public static boolean poolingBuffersReceive = true;
	public void prepareSending() {
		prepareSending(false);
	}
	public void prepareSending(boolean manualClear) {
		synchronized(outPool) {
			if(poolingBuffersSend && !outPool.isEmpty()) {
				fb = outPool.remove(outPool.size()-1);
				out = outPoolFB.remove(outPoolFB.size()-1);
			}else {
				fb = new FastByteArrayOutputStream(1024);
				out = new DataOutputStreamPositional(fb);
			}
		}
		this.manualClear = manualClear;
	}
	public void freeSending() {
		fb.reset(); //from git: length = 0; position = 0;
		
		synchronized(outPool) {
			outPool.add(fb);
			outPoolFB.add(out);
		}		
		
	}
	
	private void prepareReceiving(int wantedSize) {
		synchronized(inPool) {
			if(poolingBuffersReceive && !inPool.isEmpty()) {
				fi = inPool.remove(inPool.size()-1);
				in = inPoolFB.remove(inPoolFB.size()-1);
				int size = fi.array.length;
				while(size < wantedSize) {
					size *= 2;
				}
				if(size > fi.array.length) {
					fi = new FastByteArrayInputStream(new byte[size]);
					in = new DataInputStreamPositional(fi);
				}
			}else {
				int size = 1024;
				while(size < wantedSize) {
					size *= 2;
				}
				fi = new FastByteArrayInputStream(new byte[size]);
				in = new DataInputStreamPositional(fi);
			}
		}
	}
	

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeInt((int)fb.length);
		b.write(fb.array, 0, fb.length);
		if(poolingBuffersSend && !manualClear) {
			freeSending();
		}
		
	}

	
	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		int size = b.readInt();
		prepareReceiving(size);
		b.readFully(fi.array, 0, size);
		fi.length = size;
		fi.offset = 0;
		
		assert(in.available() == size);
	}

	@Override
	public void reset() {
		
			
		fi.length = 0;
		fi.position(0);
		if(poolingBuffersReceive) {	
			synchronized(inPool) {
				inPool.add(fi);
				inPoolFB.add(in);
			}
		}
		out = null;
		fb = null;
		fi = null;
		in = null;
		manualClear = false;
		
	}


}
