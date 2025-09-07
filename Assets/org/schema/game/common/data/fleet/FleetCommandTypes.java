package org.schema.game.common.data.fleet;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.PlayerButtonTilesInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.PlayerMultipleSectorInput;
import org.schema.game.client.view.gui.PlayerSectorInput;
import org.schema.game.client.view.gui.PlayerShipRemoteSelect;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.language.Translatable;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.input.InputState;
import org.schema.schine.network.client.ClientState;
import org.schema.schine.sound.controller.AudioController;

import java.util.Arrays;

public enum FleetCommandTypes {

	IDLE(en -> Lng.str("Idle"), en -> Lng.str("Fleet will idle. It will not attack enemies."), new FleetCommandDialog() {
		@Override
		public void clientSend(Fleet fleet) {
			fleet.sendFleetCommand(IDLE);
		}
	}, Transition.RESTART),
	MOVE_FLEET(en -> Lng.str("Move Fleet"), en -> Lng.str("Moves fleet to a specific location."), new FleetCommandDialog() {
		@Override
		public void clientSend(final Fleet fleet) {
			PlayerSectorInput p = new PlayerSectorInput((GameClientState) fleet.getState(), Lng.str("Enter destination sector!"), Lng.str("Enter destination sector! (e.g. 10, 20, 111 [or 10 20 111][or 10.20.100])"), "") {

				@Override
				public void handleEnteredEmpty() {
					deactivate();
				}

				@Override
				public void handleEntered(Vector3i p) {
					System.err.println("[CLIENT][FLEET] Sending fleet move command: " + p + "; on fleet " + fleet);
					fleet.sendFleetCommand(MOVE_FLEET, p);
				}

				@Override
				public Object getSelectCoordinateButtonText() {
					return Lng.str("USE");
				}
			};
			p.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(950);
		}
	}, Transition.MOVE_TO_SECTOR, Vector3i.class),
	PATROL_FLEET(en -> Lng.str("Patrol Fleet"), en -> Lng.str("Patrols Fleet between current sector and target sectors."), new FleetCommandDialog() {
		@Override
		public void clientSend(final Fleet fleet) {
			new PlayerMultipleSectorInput((GameClientState) fleet.getState(), Lng.str("Enter destination sector!"), Lng.str("Enter destination sector! (e.g. 10, 20, 111 [or 10 20 111][or 10.20.100]) Separate multiple points using ;"), "") {

				@Override
				public void handleEnteredEmpty() {
					deactivate();
				}

				@Override
				public void handleEntered(Vector3i... p) {
					System.err.println("[CLIENT][FLEET] Sending fleet patrol command: " + Arrays.toString(p) + "; on fleet " + fleet);
					fleet.sendFleetCommand(PATROL_FLEET, (Object[]) p);
				}

				@Override
				public void handleEntered(Vector3i p) {
				}

				@Override
				public Object getSelectCoordinateButtonText() {
					return Lng.str("USE");
				}

			}.activate();
					/*
					(new PlayerSectorInput((GameClientState) fleet.getState(),
							Lng.str("Enter destination sectors!"),
							Lng.str("Enter destination sectors! (e.g. 10, 20, 111 [or 10 20 111][or 10.20.100]"), "") {

						@Override
						public void handleEnteredEmpty() {
							deactivate();
						}

						@Override
						public void handleEntered(Vector3i sector) {
							System.err.println("[CLIENT][FLEET] Sending fleet PATROL command: " + sector + "; on fleet " + fleet);
							fleet.sendFleetCommand(PATROL_FLEET, sector);
						}

						@Override
						public Object getSelectCoordinateButtonText() {
							return Lng.str("USE");
						}
					}).activate();

					 */
		}
	}, Transition.FLEET_PATROL, Vector3i[].class),
	TRADE_FLEET(en -> Lng.str("Trading Fleet"), en -> Lng.str("Use fleet to trade between sectors."), null, Transition.FLEET_TRADE, Vector3i.class), /* TODO: Finish this later
	REPAIR_FLEET(new Translatable() {
		@Override
		public String getName(Enum en) {
			return Lng.str("Repair Fleet");
		}
	},
			new Translatable() {
				@Override
				public String getName(Enum en) {
					return Lng.str("Order fleet to repair at nearest shipyard.");
				}
			},
			null,
			Transition.FLEET_REPAIR),
	 */
	FLEET_ATTACK(en -> Lng.str("Attack Sector"), en -> Lng.str("Tries to move to target sector and attack enemy ships. For the time being, only loaded sectors can be attacked. The Fleet will wait outside the target sector otherwise."), new FleetCommandDialog() {
		@Override
		public void clientSend(final Fleet fleet) {
			PlayerSectorInput p = new PlayerSectorInput((GameClientState) fleet.getState(), Lng.str("Enter destination sector!"), Lng.str("Enter destination sector! (e.g. 10, 20, 111 [or 10 20 111][or 10.20.100])"), "") {

				@Override
				public void handleEnteredEmpty() {
					deactivate();
				}

				@Override
				public void handleEntered(Vector3i p) {

					System.err.println("[CLIENT][FLEET] Sending fleet ATTACK command: " + p + "; on fleet " + fleet);
					fleet.sendFleetCommand(FLEET_ATTACK, p);
				}

				@Override
				public Object getSelectCoordinateButtonText() {
					return Lng.str("USE");
				}
			};
			p.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(951);
		}
	}, Transition.FLEET_ATTACK, Vector3i.class),
	FLEET_DEFEND(en -> Lng.str("Defend Sector"), en -> Lng.str("Moves fleet to a sector. The fleet will attack any enemy in proximity. A ship will stop chasing if it is more than 2 sectors away from the target sector."), new FleetCommandDialog() {
		@Override
		public void clientSend(final Fleet fleet) {
			PlayerSectorInput p = new PlayerSectorInput((GameClientState) fleet.getState(), Lng.str("Enter destination sector!"), Lng.str("Enter destination sector! (e.g. 10, 20, 111 [or 10 20 111][or 10.20.100])"), "") {

				@Override
				public void handleEnteredEmpty() {
					deactivate();
				}

				@Override
				public void handleEntered(Vector3i p) {
					System.err.println("[CLIENT][FLEET] Sending fleet DEFEND command: " + p + "; on fleet " + fleet);
					fleet.sendFleetCommand(FLEET_DEFEND, p);
				}

				@Override
				public Object getSelectCoordinateButtonText() {
					return Lng.str("USE");
				}
			};
			p.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(952);
		}
	}, Transition.FLEET_DEFEND, Vector3i.class),
	ESCORT(en -> Lng.str("Fleet Escort"), en -> Lng.str("Assign the fleet to escort it's flagship. The fleet will follow the flagship and attack any enemy in proximity. A ship will stop chasing if it is more than 1 sectors away from the flagship."), new FleetCommandDialog() {
		@Override
		public void clientSend(Fleet fleet) {
			fleet.sendFleetCommand(ESCORT);
		}
	}, Transition.FLEET_ESCORT),
	REPAIR(en -> Lng.str("Fleet Repair"), en -> Lng.str("Order fleet to disengage any current targets and repair, prioritizing player-controlled ships. Requires at least one ship in the fleet to be capable of repairing."), new FleetCommandDialog() {
		@Override
		public void clientSend(Fleet fleet) {
			fleet.sendFleetCommand(REPAIR);
		}
	}, Transition.FLEET_REPAIR),
	ARTILLERY(en -> Lng.str("Fleet Artillery"), en -> Lng.str("Order fleet to engage enemies at a distance and avoid direct combat."), new FleetCommandDialog() {
		@Override
		public void clientSend(Fleet fleet) {
			fleet.sendFleetCommand(ARTILLERY);
		}
	}, Transition.FLEET_STANDOFF),
	SENTRY_FORMATION(en -> Lng.str("Formation Sentry Mode"), en -> Lng.str("WARNING: FORMATION IS EXPERIMENTAL AND CAN CAUSE GLITCHES. Fleet will keep formation. If an enemy is nearby, they will break formation and attack."), new FleetCommandDialog() {
		@Override
		public void clientSend(Fleet fleet) {
			fleet.sendFleetCommand(SENTRY_FORMATION);
		}
	}, Transition.FLEET_SENTRY_FORMATION),
	SENTRY(en -> Lng.str("Sentry Mode"), en -> Lng.str("Fleet will not keep formation. Any nearby enemy will be attacked."), new FleetCommandDialog() {
		@Override
		public void clientSend(Fleet fleet) {
			fleet.sendFleetCommand(SENTRY);
		}
	}, Transition.FLEET_SENTRY),
	FLEET_IDLE_FORMATION(en -> Lng.str("Idle in Formation"), en -> Lng.str("WARNING: FORMATION IS EXPERIMENTAL AND CAN CAUSE GLITCHES. Fleet will keep formation and not attack nearby enemies."), new FleetCommandDialog() {
		@Override
		public void clientSend(Fleet fleet) {
			fleet.sendFleetCommand(FLEET_IDLE_FORMATION);
		}
	}, Transition.FLEET_IDLE_FORMATION),
	CALL_TO_CARRIER(en -> Lng.str("Carrier Recall"), en -> Lng.str("Fleet ships will return to the pickup area of the flagship they last used to dock."), new FleetCommandDialog() {
		@Override
		public void clientSend(Fleet fleet) {
			fleet.sendFleetCommand(CALL_TO_CARRIER);
		}
	}, Transition.FLEET_RECALL_CARRIER),
	MINE_IN_SECTOR(en -> Lng.str("Mine this Sector"), en -> Lng.str("All fleet ships except the flag ship will try to mine astroids in this sector."), new FleetCommandDialog() {
		@Override
		public void clientSend(Fleet fleet) {
			fleet.sendFleetCommand(MINE_IN_SECTOR);
		}
	}, Transition.FLEET_MINE),
	CLOAK(en -> Lng.str("Enable Fleet Cloak"), en -> Lng.str("All fleet ships cloak if they can."), new FleetCommandDialog() {
		@Override
		public void clientSend(Fleet fleet) {
			fleet.sendFleetCommand(CLOAK);
		}
	}, Transition.FLEET_CLOAK),
	UNCLOAK(en -> Lng.str("Disable Fleet Cloak"), en -> Lng.str("All fleet ships uncloak if they can."), new FleetCommandDialog() {
		@Override
		public void clientSend(Fleet fleet) {
			fleet.sendFleetCommand(UNCLOAK);
		}
	}, Transition.FLEET_UNCLOAK),
	JAM(en -> Lng.str("Enable Fleet Jamming"), en -> Lng.str("All fleet ships radar jam if they can."), new FleetCommandDialog() {
		@Override
		public void clientSend(Fleet fleet) {
			fleet.sendFleetCommand(JAM);
		}
	}, Transition.FLEET_JAM),
	UNJAM(en -> Lng.str("Disable Fleet Jamming"), en -> Lng.str("All fleet ships stop radar jamming if they can."), new FleetCommandDialog() {
		@Override
		public void clientSend(Fleet fleet) {
			fleet.sendFleetCommand(UNJAM);
		}
	}, Transition.FLEET_UNJAM),

	ACTIVATE_REMOTE(en -> "Activate Inner Ship Remote", en -> "Searches each fleet member for an inner-ship remote matching the specified name and toggles it.", fleet -> (new PlayerShipRemoteSelect((GameClientState) fleet.getState(), fleet)).activate(), Transition.RESTART, String.class, Boolean.class),
	INTERDICT(en -> Lng.str("Enable Fleet FTL Interdiction"), en -> Lng.str("All fleet ships start FTL interdiction if they can."), new FleetCommandDialog() {
		@Override
		public void clientSend(Fleet fleet) {
			fleet.sendFleetCommand(INTERDICT);
		}
	}, Transition.FLEET_INTERDICT),
	STOP_INTERDICT(en -> Lng.str("Disable Fleet FTL Interdiction"), en -> Lng.str("All fleet ships stop FTL interdiction if they can."), new FleetCommandDialog() {
		@Override
		public void clientSend(Fleet fleet) {
			fleet.sendFleetCommand(STOP_INTERDICT);
		}
	}, Transition.FLEET_STOP_INTERDICT);

	public interface FleetCommandDialog {
		void clientSend(Fleet fleet);
	}

	private final Translatable name;

	private final Translatable description;

	public final Class<?>[] args;

	public final FleetCommandDialog c;

	public final Transition transition;

	FleetCommandTypes(Translatable name, Translatable description, FleetCommandDialog c, Transition transition, Class<?>... args) {
		this.name = name;
		this.args = args;
		this.description = description;
		this.c = c;
		this.transition = transition;
	}

	public String getName() {
		return name.getName(this);
	}

	public String getDescription() {
		return description.getName(this);
	}

	public boolean isAvailableOnClient() {
		return c != null;
	}

	public void addTile(final PlayerButtonTilesInput a, final Fleet fleet) {
		if(!isAvailableOnClient()) return;

		final GUIActivationCallback ac = new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return fleet.isCommandUsable(FleetCommandTypes.this);
			}
		};
		a.addTile(getName(), getDescription(), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
			@Override
			public boolean isOccluded() {
				return !(a.isActive() && ac.isActive((ClientState) fleet.getState()));
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(953);
					FleetCommandTypes.this.c.clientSend(fleet);
					a.deactivate();
				}
			}
		}, ac);
	}

	public void checkMatches(Object[] to) {
		/*
		if(to instanceof Vector3i[]) return; //Temp fix
		if (args.length != to.length) {
			throw new IllegalArgumentException("Invalid argument count: Provided: " + Arrays.toString(to) + ", but needs: " + Arrays.toString(args));
		}
		for (int i = 0; i < args.length; i++) {
			if (!to[i].getClass().equals(args[i])) {
				System.err.println("Not Equal: " + to[i] + " and " + args[i]);
				throw new IllegalArgumentException("Invalid argument on index " + i + ": Provided: " + Arrays.toString(to) + "; cannot take " + to[i] + ":" + to[i].getClass() + ", it has to be type: " + args[i].getClass());
			}
		}
				 */
	}
}
