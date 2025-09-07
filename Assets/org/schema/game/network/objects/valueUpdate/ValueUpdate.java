package org.schema.game.network.objects.valueUpdate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.common.controller.elements.FireModeValueUpdate;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.beam.tractorbeam.TractorModeValueUpdate;
import org.schema.game.common.controller.elements.stealth.StealthAddOnChargeValueUpdate;
import org.schema.game.common.controller.elements.effectblock.EffectAddOnChargeValueUpdate;
import org.schema.game.common.controller.elements.jumpdrive.JumpAddOnChargeValueUpdate;
import org.schema.game.common.controller.elements.jumpprohibiter.InterdictionAddOnChargeValueUpdate;
import org.schema.game.common.controller.elements.power.reactor.ReactorBoostAddOnChargeValueUpdate;
import org.schema.game.common.controller.elements.spacescanner.ScanAddOnChargeValueUpdate;

public abstract class ValueUpdate {

	public int failedCount = 0;
	public long lastTry = -1;
	public boolean deligateToClient;
	public static ValueUpdate getInstance(byte type) {
		ValTypes v = ValTypes.values()[type];
		ValueUpdate instance = v.fab.getInstance();

		assert (instance.getType() == v):instance.getType()+"; "+v;

		return instance;

	}

	public abstract void serialize(DataOutput buffer, boolean onServer) throws IOException;

	public abstract void deserialize(DataInput stream, int updateSenderStateId, boolean onServer) throws IOException;

	public abstract boolean applyClient(ManagerContainer<?> o);

	public abstract void setServer(ManagerContainer<?> o, long parameter);

	public void setServer(ManagerContainer<?> o) {
		setServer(o, Long.MIN_VALUE);
	}

	public abstract ValTypes getType();

	public enum ValTypes {
		SHIELD(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new ShieldValueUpdate();
			}
		}),
		SHIELD_LOCAL(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new ShieldLocalSingleValueUpdate();
			}
		}),
		SHIELD_LOCAL_FULL(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new ShieldLocalFullValueUpdate();
			}
		}),
		POWER(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new PowerValueUpdate();
			}
		}),
		POWER_BATTERY(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new BatteryPowerValueUpdate();
			}
		}),
		POWER_BATTERY_ACTIVE(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new BatteryActiveValueClientToServerUpdate();
			}
		}),
		POWER_BATTERY_EXPECTED(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new BatteryPowerExpectedValueUpdate();
			}
		}),
		POWER_EXPECTED(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new PowerExpectedValueUpdate();
			}
		}),
		SHIELD_EXPECTED(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new ShieldExpectedValueUpdate();
			}
		}),
		JUMP_CHARGE(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new JumpChargeValueUpdate();
			}
		}),
		JUMP_INHIBITOR(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new JumpInhibitorValueUpdate();
			}
		}),
		SCAN_CHARGE(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new LongRangeScanChargeValueUpdate();
			}
		}),
		DESTINATION(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new DestinationValueUpdate();
			}
		}),
		SERVER_UPDATE_REQUEST(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new ServerValueRequestUpdate();
			}
		}),
		SHIELD_REGEN_ENABLED(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new ShieldRechargeValueUpdate();
			}
		}),
		POWER_REGEN_ENABLED(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new PowerRechargeValueUpdate();
			}
		}),
		STEALTH_RECHARGE(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new StealthChargeValueUpdate();
			}
		}),
		STEALTH_ACTIVE(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new StealthActivationValueUpdate();
			}
		}),
		@Deprecated
		JAM_DELAY(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new JamDelayValueUpdate();
			}
		}),
		SHIPYARD_STATE_UPDATE(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new ShipyardCurrentStateValueUpdate();
			}
		}),
		SHIPYARD_ERROR_UPDATE(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new ShipyardErrorValueUpdate();
			}
		}),
		SHIPYARD_CLIENT_COMMAND(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new ShipyardClientCommandValueUpdate();
			}
		}),
		SHIPYARD_CLIENT_STATE_REQUEST(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new ShipyardClientStateRequestValueUpdate();
			}
		}),
		SHIPYARD_BLOCK_GOAL(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new ShipyardBlockGoalValueUpdate();
			}
		}),
		TRANSPORTER_DESTINATION_UPDATE(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new TransporterDestinationUpdate();
			}
		}),
		TRANSPORTER_SETTINGS_UPDATE(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new TransporterSettingsUpdate();
			}
		}),
		TRANSPORTER_CLIENT_STATE_REQUEST(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new TransporterClientStateRequestUpdate();
			}
		}),
		TRANSPORTER_USAGE_UPDATE(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new TransporterUsageUpdate();
			}
		}),
		TRANSPORTER_BEACON_ACTIVATED(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new TransporterBeaconActivated();
			}
		}),
		@Deprecated
		JUMP_CHARGE_REACTOR(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new JumpAddOnChargeValueUpdate();
			}
		}),
		EFFECT_ADD_ON_CHARGE(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new EffectAddOnChargeValueUpdate();
			}
		}),
		@Deprecated
		SCAN_CHARGE_REACTOR(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new ScanAddOnChargeValueUpdate();
			}
		}),
		@Deprecated
		STEALTH_CHARGE_REACTOR(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new StealthAddOnChargeValueUpdate();
			}
		}),
		REACTOR_BOOST_CHARGE(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new ReactorBoostAddOnChargeValueUpdate();
			}
		}), 
		INTERDICTION_CHARGE(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new InterdictionAddOnChargeValueUpdate();
			}
		}),
		WEAPON_CAPACITY_UPDATE(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new WeaponCapacityValueUpdate();
			}
		}),
		TRACTOR_MODE(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new TractorModeValueUpdate();
			}
		}), 
		FIRE_MODE(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new FireModeValueUpdate();
			}
		}),
		STRUCTURE_SCANNER_ACTIVATION(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new StructureScanActivationValueUpdate();
			}
		}),
		STRUCTURE_SCANNER_CHARGE(new ValueUpdateFab() {
			@Override
			public ValueUpdate getInstance() {
				return new StructureScanChargeValueUpdate();
			}}),
		;
		private ValueUpdateFab fab;

		private ValTypes(ValueUpdateFab fab) {
			this.fab = fab;
		}
	}

	public boolean checkOnAdd() {
		return true;
	}

}
