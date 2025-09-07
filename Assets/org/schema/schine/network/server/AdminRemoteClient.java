package org.schema.schine.network.server;

import java.io.IOException;
import java.util.List;

import org.schema.schine.network.RegisteredClientInterface;
import org.schema.schine.network.commands.MessageCommandPackage;

import com.bulletphysics.util.ObjectArrayList;

public class AdminRemoteClient implements RegisteredClientInterface {

	public final List<String> msgs = new ObjectArrayList<String>();
	private ServerProcessorInterface p;
	private boolean blocked;

	public AdminRemoteClient(ServerProcessorInterface p) {
		this.p = p;
	}

	@Override
	public void executedAdminCommand() {
		(new Thread(() -> {
			while(blocked){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			synchronized(p.getState()){
				p.getState().setSynched();
				try{
					for (String msg : msgs) {
						try {
							ServerMessage m = new ServerMessage(new Object[] {"[SERVER]: "+msg}, ServerMessage.MESSAGE_TYPE_SIMPLE);
							MessageCommandPackage pack = new MessageCommandPackage();
							pack.message = m;
							pack.send(p);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					try {
						ServerMessage m = new ServerMessage(new Object[] {"[SERVER]: END; Admin command execution ended"}, ServerMessage.MESSAGE_TYPE_SIMPLE);
						MessageCommandPackage pack = new MessageCommandPackage();
						pack.message = m;
						pack.send(p);
					} catch (IOException e) {
						e.printStackTrace();
					}
					p.disconnectAfterSent();
				}finally{
					p.getState().setUnsynched();
				}
			}

		})).start();;
		
	}

	@Override
	public int getId() {
		return -1337;
	}

	@Override
	public String getClientName() {
		return "#REMOTE#" + (p.getClient() == null ? "#unknownName#" : p.getClient().getClientName()) + " (" + p.getIp() + ")";
	}

	@Override
	public void serverMessage(String msg) throws IOException {
		msgs.add(msg);
	}

	@Override
	public void blockFromLogout() {
		blocked = true;
	}

	@Override
	public void disconnect() {
		blocked = false;
	}
}
