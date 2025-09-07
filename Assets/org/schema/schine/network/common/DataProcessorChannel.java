package org.schema.schine.network.common;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

import org.schema.schine.graphicsengine.core.GlUtil;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class DataProcessorChannel implements DataProcessor{

	
	private SocketChannel chan;
	private BufferCon buf;
	
	private static class BufferCon{
		public int reused;
		private ByteBuffer bufIn;
		private ByteBuffer bufOut;

		public BufferCon(NetworkSettings settings) {
			this.bufIn = ByteBuffer.allocateDirect (settings.getSocketReceiveBufferSize().getInt());
			this.bufOut = ByteBuffer.allocateDirect (settings.getSocketSendBufferSize().getInt());
		}
		
		public void clear() {
			bufIn.clear();
			bufOut.clear();
		}

		public void destroy() {
			GlUtil.destroyDirectByteBuffer(this.bufIn);
			GlUtil.destroyDirectByteBuffer(this.bufOut);
		}
	}
	
	private static final List<BufferCon> pool = new ObjectArrayList<>();

	private static BufferCon getBuffer(NetworkSettings settings) {
		final BufferCon c;
		synchronized (pool) {
			if (!pool.isEmpty()) {
				c = pool.remove(pool.size() - 1);
				c.reused++;
			} else {
				c = new BufferCon(settings);
			}
		}
		c.clear();
		return c;
	}
	private static void freeBuffer(BufferCon buf) {
		buf.clear();
		buf.destroy();
//		synchronized(pool) {
//			pool.add(buf);
//		}
	}
	
	
	public DataProcessorChannel(SocketChannel chan, NetworkSettings settings) {
		this.chan = chan;
		assert(chan.isBlocking());
		this.buf = getBuffer(settings);
		this.buf.clear();
	}
	
	@Override
	public int readPackage(InputPacket packet) throws IOException {
		ensure (buf.bufIn, 4, chan);
        int len = buf.bufIn.getInt ();
        assert(len > 0):"no data "+len+"; reused: "+buf.reused;
       
        packet.readFully(chan, buf.bufIn, len);
		
		return len;
	}
	@Override
	public void sendPacket(OutputPacket s) throws IOException {
		s.writeTo(chan, buf.bufOut);
	}
	public static void ensure (final ByteBuffer buf, final int len, final SocketChannel chan) throws IOException
    {
		assert(len > 0):len+"; ";
		buf.clear();
		buf.limit(len);
		int read = 0;
		int totalRead = 0;
		while (buf.hasRemaining()) {
			assert(buf.position() < buf.limit()):buf.position() +" < "+ buf.limit()+"; read "+read+"/"+totalRead+"; rem "+buf.remaining();
			read = chan.read(buf);
			totalRead += read;
			if (read == -1 || read == 0) {
				throw new EOFException("SOCKET WRONG Read "+read);
			}
			
		}
		buf.flip();
//        if (buf.position() > buf.capacity () - len) {
//            buf.compact ();
//            buf.flip ();
//        }
//        while(buf.remaining () < len){
//            int oldpos = buf.position ();
//            buf.position (buf.limit ());
//            buf.limit (buf.capacity ());
//            int read = chan.read (buf);
//            if(read == -1) {
//            	throw new EOFException();
//            }
//            buf.limit (buf.position ());
//            buf.position (oldpos);
//        }
    }

	@Override
	public void close(NetworkProcessor proc) throws IOException {
		try {
			try {
				chan.shutdownInput();
			} catch (IOException e) {
			}
			try {
				chan.shutdownOutput();
			} catch (IOException e) {
			}
			try {
				chan.close();
			} catch (IOException e) {
			}
		} finally {
			proc.getThreadPool().execute(() -> {
				int tries = 0;
				while(!proc.isFullyFinishedDisconnect()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if(tries > 3) {
						System.err.println("WARNING: waiting for connection processor finish");
					}
					tries++;
				}
				freeBuffer(buf);
			});
			
		}
		
		
	}

	@Override
	public boolean isConnected() {
		return chan.isOpen() && this.chan.isConnected() && !this.chan.socket().isClosed();
	}

	@Override
	public String getInetAddress() {
		try {
			return chan.isOpen() ? chan.getLocalAddress().toString() : "n/a";
		} catch (IOException e) {
			return "n/a";
		}
	}

	@Override
	public String getRemoteIp() {
		try {
			return chan.isOpen() ? this.chan.getRemoteAddress().toString() : "n/a";
		} catch (IOException e) {
			return "n/a";
		}
	}

	@Override
	public int getLocalPort() {
		return 0;
	}

	@Override
	public void flushOut() throws IOException {
		
	}
	@Override
	public Socket getSocket() {
		return chan.socket();
	}

    @Override
    public boolean hasData() {
        return true;
    }


}
