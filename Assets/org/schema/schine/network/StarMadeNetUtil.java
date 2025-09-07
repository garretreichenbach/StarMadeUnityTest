package org.schema.schine.network;

import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.rudp.ReliableSocket;
import org.schema.game.network.commands.ExecuteAdminCommandCommandPackage;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.network.commands.MessageCommandPackage;
import org.schema.schine.network.commands.ServerInfoAnswerCommandPackage;
import org.schema.schine.network.commands.ServerInfoRequestCommandPackage;
import org.schema.schine.network.common.OutputPacket;
import org.schema.schine.network.common.commands.CommandPackage;
import org.schema.schine.network.server.ServerMessage;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

public class StarMadeNetUtil {

	public interface InfoListener {
		void onInfoRetrieved(ServerInfo info);

		void onAdminCommandAnswer(ServerMessage message);
	}

	public List<InfoListener> listeners = new ObjectArrayList<>();

	public static void main(String[] args) {
		try {
			StarMadeNetUtil u = new StarMadeNetUtil();
			while(true) {
				ServerInfo serverInfo = u.getServerInfo("play.star-made.org", 4242, 5000);
				System.err.println(serverInfo.toString());
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This send a package directly. Note that this is inefficient as fuck currently, from the instantiations used, but as long as it isn't used in a per frame operation it's fine.
	 * 
	 * 
	 * Other side expects:
	 * 
	 * Size package, followed by package
	 * The first byte in a package is always the package ID, the rest is the package payload (or nothing if there is none)
	 * 
	 * @param pack
	 * @param out
	 * @param onServer
	 * @throws IOException
	 */
	public void sendPackageToSocketDirectly(CommandPackage pack, DataOutputStream out, boolean onServer) throws IOException {
		//this is usually done with reusable streams (from the wrapping packages)
		//size is determined by writing to the stream and then just checking how much we wrote
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream s = new DataOutputStream(b);
		s.writeByte(pack.getType().getId()); //write type
		pack.serialize(s, onServer); //write rest
		int size = s.size();
		out.writeInt(size);
		out.write(b.toByteArray());
		out.flush();
	}

	public ServerInfo getServerInfo(String host, int port, int timeout) throws IOException {

		DataOutputStream out;
		DataInputStream in;
		Socket socket = null;

		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(host, port), timeout);
			socket.setTcpNoDelay(true);
			socket.setSoTimeout(timeout);
			out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			in = new DataInputStream(socket.getInputStream());
		} catch(Exception e) {
			if(socket != null) {
				try {
					socket.close();
				} catch(Exception ee) {
					ee.printStackTrace();
				}
			}
			socket = new ReliableSocket();
			if(EngineSettings.CLIENT_TRAFFIC_CLASS.isOn()) {
				socket.setTrafficClass(24);
			}

			socket.connect(new InetSocketAddress(host, port), timeout);
			out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			in = new DataInputStream(socket.getInputStream());
		}

		// Start timing for ping calculation
		long started = System.currentTimeMillis();
		
		// Send the server info request
		ServerInfoRequestCommandPackage spack = new ServerInfoRequestCommandPackage();
		
		//out.writeByte((byte) spack.getType().getId());
		//spack.serialize(out, true);
		//out.flush();

		sendPackageToSocketDirectly(spack, out, true);
		
		try {
			// Read the size of the incoming packet
			int size = in.readInt(); //TODO: This times out!!!
			
			// Read the timestamp
//			long ts = in.readLong();
			
			// Read the full data packet
			byte[] receive = new byte[size];
			in.readFully(receive);
			
			// Calculate round trip time
			long ended = System.currentTimeMillis();
			long roundTripTime = ended - started;
			
			// Create a new input stream for the received data
			DataInputStream bin = new DataInputStream(new ByteArrayInputStream(receive));
			
			// Read the command type
			byte commandByte = bin.readByte(); // Use bin instead of in
			
			// Create and deserialize the server info answer
			ServerInfoAnswerCommandPackage pack = new ServerInfoAnswerCommandPackage();
			pack.deserialize(bin, 0, false); // Use bin instead of in
			
			// Create and populate the server info object
			ServerInfo info = pack.info;
			info.host = host;
			info.port = port;
			info.ping = roundTripTime;
			info.connType = (socket instanceof ReliableSocket ? "UDP" : "TCP");
			info.ip = socket.getInetAddress().getHostAddress().replace("/", "");
			info.reachable = true;
			
			// Notify listeners
			for(InfoListener l : listeners) {
				l.onInfoRetrieved(info);
			}
			
			socket.close();
			return info;
		} catch(Exception e) {
			System.err.println("Error retrieving server info from " + host + ":" + port);
			e.printStackTrace();
			
			// Create default server info on error
			ServerInfo info = new ServerInfo(host, port);
			info.connType = (socket instanceof ReliableSocket ? "UDP" : "TCP");
			if(socket.getInetAddress() != null) {
				info.ip = socket.getInetAddress().getHostAddress().replace("/", "");
			}
			
			try {
				socket.close();
			} catch(Exception ee) {
				// Ignore
				ee.printStackTrace();
			}
			
			return info;
		}
	}

	public void executeAdminCommand(String host, int port, String serverPassword, String command) throws IOException {

		Socket s = new Socket(host, port);

		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
		DataInputStream in = new DataInputStream(s.getInputStream());
		ByteArrayOutputStream bb = new ByteArrayOutputStream();
		DataOutputStream dI = new DataOutputStream(bb);
		dI.writeUTF(serverPassword);
		dI.writeUTF(command);
		/*
		 * All packets have an size integer first
		 */
		OutputPacket o = new OutputPacket(new FastByteArrayOutputStream(new byte[1024 * 20]), 0);

		ExecuteAdminCommandCommandPackage spack = new ExecuteAdminCommandCommandPackage();
		spack.command = command;
		spack.serverPassword = serverPassword;
		out.writeByte(spack.getType().getId());
		spack.serialize(out, true);

		long started = System.currentTimeMillis();

		/*
		 * flush the buffer
		 */
		out.flush();


		/*
		 * wait for an awnser
		 * (Again, size is first)
		 */

		while(true) {
			int size = in.readInt();
			long ts = in.readLong();
			/*
			 * read the received packet
			 */
			byte[] receive = new byte[size];
			in.readFully(receive);

			DataInputStream bin = new DataInputStream(new ByteArrayInputStream(receive));


			/*
			 * The check ID (only relevant ingame)
			 */
			byte c = bin.readByte();

			MessageCommandPackage msg = new MessageCommandPackage();
			assert (c == msg.getType().getId());

			msg.deserialize(in, 0, false);
			for(InfoListener l : listeners) {
				l.onAdminCommandAnswer(msg.message);
			}

			long ended = System.currentTimeMillis();
			long roundTripTime = ended - started;

			if(checkDisconnect(msg.message)) {
				break;
			}

		}
		s.close();

	}

	private boolean checkDisconnect(ServerMessage msg) {
		for(int i = 0; i < msg.getMessage().length; i++) {
			if(msg.getMessage()[i].toString().startsWith("END;")) {
				//soft disconnect
				return true;
			}
		}
		return false;
	}
}
