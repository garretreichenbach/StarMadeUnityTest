package org.schema.game.network.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.SerializationInterface;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.PlayerControllable;

public class ControllerUnitRequest implements SerializationInterface{

	public int fromId = -1;
	public int toId = -1;
	public boolean hideExitedObject;
	public Vector3i fromParam;
	public Vector3i toParam;

	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		
		b.writeInt(fromId);
		b.writeInt(toId);
		
		if(fromParam != null){
			fromParam.serialize(b);
		}else{
			b.writeInt(0);
			b.writeInt(0);
			b.writeInt(0);
		}
		
		if(toParam != null){
			toParam.serialize(b);
		}else{
			b.writeInt(0);
			b.writeInt(0);
			b.writeInt(0);
		}
		
		b.writeBoolean(hideExitedObject);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
			boolean isOnServer) throws IOException {
		
		fromId = b.readInt();
		toId = b.readInt();
		
		fromParam = new Vector3i();
		fromParam.deserialize(b);
		
		toParam = new Vector3i();
		toParam.deserialize(b);
		
		hideExitedObject = b.readBoolean();
		
		
		assert(fromParam != null);
		assert(toParam != null);
	}

	public void setFrom(PlayerControllable from) {
		if(from != null){
			this.fromId = from.getId();
		}else{
			this.fromId = -1;
		}
	}

	public void setTo(int to) {
		this.toId = to;
	}
	public void setTo(PlayerControllable to) {
		if(to != null){
			this.toId = to.getId();
		}else{
			this.toId = -1;
		}		
	}

	public boolean isHideExitedObject() {
		return hideExitedObject;
	}

	public void setHideExitedObject(boolean hideExitedObject) {
		this.hideExitedObject = hideExitedObject;
	}

	public void setFromParam(Vector3i fromParam) {
		this.fromParam = fromParam;
	}
	public void setToParam(Vector3i toParam) {
		this.toParam = toParam;
	}

	@Override
	public String toString() {
		return "ControllerUnitRequest [fromId=" + fromId + ", toId=" + toId
				+ ", hideExitedObject=" + hideExitedObject + ", fromParam="
				+ fromParam + ", toParam=" + toParam + "]";
	}

	
	
	
}
