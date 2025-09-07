package org.schema.schine.network.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.schema.schine.resource.FileExt;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;


public class ServerEntry {
	
	public String host;
	public int port;
	public boolean favorite;
	public boolean custom;

	public ServerEntry(String line) throws Exception {
		String[] split = line.split(",", 3);
		host = split[1];
		port = Integer.parseInt(split[2]);
	}

	public ServerEntry(String host, int port) {
		super();
		this.host = host;
		this.port = port;
	}

	public String getLine() {
		return host + "," + port;
	}
	public void serialize(DataOutput b) throws IOException{
		b.writeUTF(host);
		b.writeInt(port);
		b.writeBoolean(favorite);
	}
	
	public static ServerEntry deserialize(DataInputStream b) throws IOException{
		ServerEntry serverEntry = new ServerEntry(b.readUTF(), b.readInt());
		serverEntry.favorite = b.readBoolean();
		return serverEntry;
	}
	
	public static void write(Collection<ServerEntry> infos, String path) throws IOException{
		DataOutputStream b = null;
		try{
			File f = new FileExt(path);
			if(f.exists()){
				f.delete();
			}
			
			b = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
			
			b.writeInt(infos.size());
			
			for(ServerEntry m : infos){
				m.serialize(b);
			}
		}finally{
			if(b != null){
				b.close();
			}
		}
		
	}
	public static List<ServerEntry> read(String path) throws IOException{
		
		DataInputStream d = null;
		try{
			File f = new FileExt(path);
			
			d = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
			int size = d.readInt();
			List<ServerEntry> m = new ObjectArrayList<ServerEntry>(size);
			
			for(int i = 0; i < size; i++){
				m.add(deserialize(d));
			}
			return m;
		}finally{
			if(d != null){
				d.close();
			}
		}
	}

	@Override
	public int hashCode() {
		return host.hashCode() * port;
	}

	@Override
	public boolean equals(Object obj) {
		return host.equals(((ServerEntry)obj).host) && port ==  (((ServerEntry)obj).port);
	}
	
	
	
}
