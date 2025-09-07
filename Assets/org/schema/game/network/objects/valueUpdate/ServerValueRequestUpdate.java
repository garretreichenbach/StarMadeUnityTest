package org.schema.game.network.objects.valueUpdate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ShieldContainerInterface;
import org.schema.game.common.controller.elements.ShipManagerContainer;
import org.schema.game.common.controller.elements.SingleElementCollectionContainerInterface;
import org.schema.game.common.controller.elements.jumpdrive.JumpDriveCollectionManager;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;

public class ServerValueRequestUpdate extends BooleanValueUpdate {

	private static interface Exec{
		public void execute(ServerValueRequestUpdate u, ManagerContainer<?> o);
	}
	
	public enum Type{
		ALL(new Exec(){
			@Override
			public void execute(ServerValueRequestUpdate u, ManagerContainer<?> o) {
				for(Type t : Type.values()){
					if(t != ALL){
						t.e.execute(u, o);
					}
				}
			}
		}),
		SHIELD((u, o) -> {
			((ShieldContainerInterface) o).getShieldAddOn().sendShieldUpdate();
		}),
		POWER((u, o) -> {
			((PowerManagerInterface) o).getPowerAddOn().sendPowerUpdate();
			((PowerManagerInterface) o).getPowerAddOn().sendPowerExpectedUpdate();
			if(!o.isUsingPowerReactors()){
				((PowerManagerInterface) o).getPowerAddOn().sendBatteryPowerUpdate();
				((PowerManagerInterface) o).getPowerAddOn().sendBatteryPowerExpectedUpdate();
			}
		}),
		JUMP((u, o) -> {
			if(o instanceof ShipManagerContainer smc){
				if(smc.getJumpDrive().getElementManager().hasCollection()) {
					((SingleElementCollectionContainerInterface<JumpDriveCollectionManager>) (smc.getJumpDrive().getElementManager())).getCollection().sendChargeUpdate();
				}
			}
		}),
		STEALTH((u, o) -> {
			//o.getStealthAddOn().sendChargeUpdate();
		}),
		REACTOR_BOOST((u, o) -> {
			o.getReactorBoostAddOn().sendChargeUpdate();
		}),
		JUMP_INTERDICTION((u, o) -> {
			o.getInterdictionAddOn().sendChargeUpdate();
		}),
		SCAN((u, o) -> {
			//o.getScanAddOn().sendChargeUpdate();
		}),
		EFFECT((u, o) -> {
			o.getEffectAddOnManager().sendChargeUpdate();
		}),
		WEAPON_AMMO((u, o) -> {
			o.flagSendAllAmmo(); //TODO split
		}),
		FIRE_MODES((u, o) -> {
			o.sendAllFireModes();
		}),
		;
		
		private final Exec e;

		private Type(Exec e){
			this.e = e;
		}
	}

	public ServerValueRequestUpdate(Type requestType) {
		super();
		this.requestType = requestType;
	}
	public ServerValueRequestUpdate() {
		super();
	}


	public Type requestType;

	
	
	@Override
	public void serialize(DataOutput buffer, boolean onServer) throws IOException {
		super.serialize(buffer, onServer);
		buffer.writeByte((byte)requestType.ordinal());
	}



	@Override
	public void deserialize(DataInput stream, int updateSenderStateId, boolean onServer) throws IOException {
		super.deserialize(stream, updateSenderStateId, onServer);
		requestType = Type.values()[stream.readByte()];
	}



	@Override
	public void setServer(ManagerContainer<?> o, long parameter) {
		//nothing to do, but this happens on client
		assert (!o.getSegmentController().isOnServer());
		
	}

	@Override
	public ValTypes getType() {
		return ValTypes.SERVER_UPDATE_REQUEST;
	}



	@Override
	public boolean checkOnAdd() {
		return requestType != null;
	}



	@Override
	public boolean applyClient(ManagerContainer<?> o) {
		//this update happens on server
		assert (o.getSegmentController().isOnServer());
		requestType.e.execute(this, o);
		return true;
	}

}
