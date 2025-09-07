package org.schema.game.common.controller.rules.rules.actions;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.common.controller.rules.rules.actions.faction.*;
import org.schema.game.common.controller.rules.rules.actions.player.*;
import org.schema.game.common.controller.rules.rules.actions.sector.*;
import org.schema.game.common.controller.rules.rules.actions.seg.*;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.language.Translatable;
import org.schema.schine.network.TopLevelType;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public enum ActionTypes {
		DO_NOTHING(1, 
			new SegmentControllerActionFactory() {
				@Override
				public SegmentControllerAction instantiateAction() {
					return new SegmentControllerVoidAction();
				}
		
			}, en -> Lng.str("Do Nothing"), en -> Lng.str("A void action that will not do anything")),
	
			SEG_APPLY_EFFECT(2, 
			new SegmentControllerActionFactory() {
				@Override
				public SegmentControllerAction instantiateAction() {
					return new SegmentControllerApplyEffectAction();
				}
		
			}, en -> Lng.str("Apply Effect"), en -> Lng.str("Applies an effect to the entity")),
	
			SEG_CONSOLE_MESSAGE(3, 
			new SegmentControllerActionFactory() {
				@Override
				public SegmentControllerAction instantiateAction() {
					return new SegmentControllerConsoleMessageAction();
				}
		
			}, en -> Lng.str("Console Message"), en -> Lng.str("Prints a message to console")),
	
			SEG_POPUP_MESSAGE(4, 
			new SegmentControllerActionFactory() {
				@Override
				public SegmentControllerAction instantiateAction() {
					return new SegmentControllerPopupMessageAction();
				}
		
			}, en -> Lng.str("Popup Message"), en -> Lng.str("Pops up a message for the pilots of the ship")),
	
			SEG_MODIFY_THRUSTER_ACTION(5, 
			new SegmentControllerActionFactory() {
				@Override
				public SegmentControllerAction instantiateAction() {
					return new SegmentControllerModifyThrusterAction();
				}
		
			}, en -> Lng.str("Modify Thruster"), en -> Lng.str("Modifies thruster of the ship")),
			
			SEG_POPUP_MESSAGE_IN_BUILD_MODE(6, 
					new SegmentControllerActionFactory() {
				@Override
				public SegmentControllerAction instantiateAction() {
					return new SegmentControllerPopupMessageInBuildModeAction();
				}
				
			}, en -> Lng.str("Build Mode Message"), en -> Lng.str("Permanent Message in Build Mode of the entity")),

			SEG_TRACK(7, 
					new SegmentControllerActionFactory() {
				@Override
				public SegmentControllerAction instantiateAction() {
					return new SegmentControllerTrackAction();
				}
				
			}, en -> Lng.str("Admin Track"), en -> Lng.str("Tracks entity for admins")),
			
			SEG_ENABLE_ENERGY_STREAM(8, 
					new SegmentControllerActionFactory() {
					@Override
					public SegmentControllerAction instantiateAction() {
						return new SegmentControllerEnableEnergyStreamAction();
					}
				
				}, en -> Lng.str("Enable Energy Stream"), en -> Lng.str("Enables reactor energy stream for ships and stations")),
			
			SEG_BEAM_EXPLOSIVE(9, 
					new SegmentControllerActionFactory() {
				@Override
				public SegmentControllerAction instantiateAction() {
					return new SegmentControllerBeamExplosiveAction();
				}
				
			}, en -> Lng.str("Volatile Beam Weapons"), en -> Lng.str("Beam block structures explode extra when hit")),
			SEG_MISSILE_EXPLOSIVE(10, 
					new SegmentControllerActionFactory() {
				@Override
				public SegmentControllerAction instantiateAction() {
					return new SegmentControllerMissileExplosiveAction();
				}
				
			}, en -> Lng.str("Volatile Missile Weapons"), en -> Lng.str("Missile block structures explode extra when hit")),
			SEG_CANNON_EXPLOSIVE(11, 
					new SegmentControllerActionFactory() {
				@Override
				public SegmentControllerAction instantiateAction() {
					return new SegmentControllerCannonExplosiveAction();
				}
				
			}, en -> Lng.str("Volatile Cannon Weapons"), en -> Lng.str("Cannon block structures explode extra when hit")),
			SEG_REACTOR_EXPLOSIVE(12, 
					new SegmentControllerActionFactory() {
				@Override
				public SegmentControllerAction instantiateAction() {
					return new SegmentControllerReactorExplosiveAction();
				}
				
			}, en -> Lng.str("Volatile Reactors"), en -> Lng.str("Reactor block structures explode extra when hit")),
			SEG_REACTOR_CHAMBER_EXPLOSIVE(13, 
					new SegmentControllerActionFactory() {
				@Override
				public SegmentControllerAction instantiateAction() {
					return new SegmentControllerChamberExplosiveAction();
				}
				
			}, en -> Lng.str("Volatile Reactor Chambers"), en -> Lng.str("Reactor chamber block structures explode extra when hit")),
			SEG_SHIELD_CAPACITY_EXPLOSIVE(14, 
					new SegmentControllerActionFactory() {
				@Override
				public SegmentControllerAction instantiateAction() {
					return new SegmentControllerShieldCapacityExplosiveAction();
				}
				
			}, en -> Lng.str("Volatile Shield Capacity"), en -> Lng.str("Shield capacity block structures explode extra when hit")),
			SEG_SHIELD_REGEN_EXPLOSIVE(15, 
					new SegmentControllerActionFactory() {
				@Override
				public SegmentControllerAction instantiateAction() {
					return new SegmentControllerShieldRegenExplosiveAction();
				}
				
			}, en -> Lng.str("Volatile Shield Rechargers"), en -> Lng.str("Shield recharger block structures explode extra when hit")),
			SEG_STABILIZER_EXPLOSIVE(16, 
					new SegmentControllerActionFactory() {
				@Override
				public SegmentControllerAction instantiateAction() {
					return new SegmentControllerStabilizerExplosiveAction();
				}
				
			}, en -> Lng.str("Volatile Shield Stabilizers"), en -> Lng.str("Stabilizers block structures explode extra when hit")),
			SEG_THRUSTER_EXPLOSIVE(17, 
					new SegmentControllerActionFactory() {
				@Override
				public SegmentControllerAction instantiateAction() {
					return new SegmentControllerThrusterExplosiveAction();
				}
				
			}, en -> Lng.str("Volatile Thrusters"), en -> Lng.str("Thruster block structures explode extra when hit")),
			SEG_SET_FACTION(18,
					new SegmentControllerActionFactory() {
				@Override
				public SegmentControllerAction instantiateAction() {
					return new SegmentControllerSetFactionAction();
				}

			}, en -> Lng.str("Set Faction"), en -> Lng.str("Sets faction of entity")),
			SEG_SET_AI(19,
					new SegmentControllerActionFactory() {
				@Override
				public SegmentControllerAction instantiateAction() {
					return new SegmentControllerSetAIAction();
				}

			}, en -> Lng.str("Turn AI on/off"), en -> Lng.str("Turns AI of an entity on/off")),
			SEG_RUN_ADMIN_COMMAND(20,
					new SegmentControllerActionFactory() {
				@Override
				public SegmentControllerAction instantiateAction() {
					return new SegmentControllerRunAdminCommandAction();
				}

			}, en -> Lng.str("Run Admin Command"), en -> Lng.str("Runs admin command where <uid> is replaced with entity uid")),
			SECTOR_CHMOD(21,
					new SectorActionFactory() {
				@Override
				public SectorAction instantiateAction() {
					return new SectorChmodAction();
				}

			}, en -> Lng.str("Sector Chmod"), en -> Lng.str("Changes the sector flags")),
			SECTOR_RUN_ADMIN_COMMAND(22,
					new SectorActionFactory() {
				@Override
				public SectorAction instantiateAction() {
					return new SectorRunAdminCommandAction();
				}

			}, en -> Lng.str("Run Admin Command"), en -> Lng.str("Runs admin command where <pos> is replaced with sector pos")),
			PLAYER_KILL(23,
					new PlayerActionFactory() {
				@Override
				public PlayerAction instantiateAction() {
					return new PlayerKillAction();
				}

			}, en -> Lng.str("Kills Player"), en -> Lng.str("Kills Player")),
			PLAYER_KICK(24,
					new PlayerActionFactory() {
				@Override
				public PlayerAction instantiateAction() {
					return new PlayerKickAction();
				}

			}, en -> Lng.str("Kicks Player"), en -> Lng.str("Kicks Player")),
			PLAYER_BAN(25,
					new PlayerActionFactory() {
				@Override
				public PlayerAction instantiateAction() {
					return new PlayerBanAction();
				}

			}, en -> Lng.str("Bans Player"), en -> Lng.str("Bans Player")),
			PLAYER_WARP(26,
					new PlayerActionFactory() {
				@Override
				public PlayerAction instantiateAction() {
					return new PlayerWarpAction();
				}

			}, en -> Lng.str("Warp Player"), en -> Lng.str("Warps Player to a sector/pos")),
			PLAYER_SET_CREDITS(27,
					new PlayerActionFactory() {
				@Override
				public PlayerAction instantiateAction() {
					return new PlayerSetCreditsAction();
				}

			}, en -> Lng.str("Set Player Credits"), en -> Lng.str("Sets Player Credits")),

			PLAYER_KICK_OUT_OF_ENTITY(28,
					new PlayerActionFactory() {
				@Override
				public PlayerAction instantiateAction() {
					return new PlayerKickOutOfEntityAction();
				}

			}, en -> Lng.str("Kicks Player out of entity"), en -> Lng.str("Kicks Player out of entity")),
			PLAYER_SEND_MESSAGE(29,
					new PlayerActionFactory() {
				@Override
				public PlayerAction instantiateAction() {
					return new PlayerSendMessageAction();
				}

			}, en -> Lng.str("Send Player Message"), en -> Lng.str("Sends a message to a player")),
			PLAYER_MOD_CREDITS(30,
					new PlayerActionFactory() {
				@Override
				public PlayerAction instantiateAction() {
					return new PlayerModCreditsAction();
				}

			}, en -> Lng.str("Sends Player Message"), en -> Lng.str("Senda a message to the player")),
			PLAYER_RUN_ADMIN_COMMAND(31,
					new PlayerActionFactory() {
				@Override
				public PlayerAction instantiateAction() {
					return new PlayerRunAdminCommandAction();
				}

			}, en -> Lng.str("Player Admin Command"), en -> Lng.str("Runs admin command for player")),
			PLAYER_SET_FACTION_POINTS(32,
					new PlayerActionFactory() {
				@Override
				public PlayerAction instantiateAction() {
					return new PlayerSetFactionPointsAction();
				}

			}, en -> Lng.str("Set Player Faction Points"), en -> Lng.str("Sets faction points for a player")),
			PLAYER_MOD_FACTION_POINTS(33,
					new PlayerActionFactory() {
				@Override
				public PlayerAction instantiateAction() {
					return new PlayerModFactionPointsAction();
				}

			}, en -> Lng.str("Add Player Faction Points"), en -> Lng.str("Adds faction points for a player")),
			SEG_SET_FACTION_POINTS(34,
					new SegmentControllerActionFactory() {
				@Override
				public SegmentControllerAction instantiateAction() {
					return new SegmentControllerSetFactionPointsAction();
				}

			}, en -> Lng.str("Set Entity Owner Faction Points"), en -> Lng.str("Sets faction points for an Entity Owner")),
			SEG_MOD_FACTION_POINTS(35,
					new SegmentControllerActionFactory() {
				@Override
				public SegmentControllerAction instantiateAction() {
					return new SegmentControllerModFactionPointsAction();
				}

			}, en -> Lng.str("Add Entity Owner Faction Points"), en -> Lng.str("Adds faction points for an Entity Owner")),
			FACTION_SET_FACTION_POINTS(36,
					new FactionActionFactory() {
				@Override
				public FactionAction instantiateAction() {
					return new FactionSetFactionPointsAction();
				}

			}, en -> Lng.str("Set Faction Points"), en -> Lng.str("Sets faction points")),
			FACTION_MOD_FACTION_POINTS(37,
					new FactionActionFactory() {
				@Override
				public FactionAction instantiateAction() {
					return new FactionModFactionPointsAction();
				}

			}, en -> Lng.str("Add Faction Points"), en -> Lng.str("Adds faction points")),
			FACTION_2_PLAYERS(38,
					new FactionActionFactory() {
				@Override
				public FactionAction instantiateAction() {
					return new Faction2PlayerAction();
				}

			}, en -> Lng.str("Faction->Player"), en -> Lng.str("Faction->Player")),
			PLAYER_2_SEG(39,
					new PlayerActionFactory() {
				@Override
				public PlayerAction instantiateAction() {
					return new Player2EnteredSegmentControllerAction();
				}

			}, en -> Lng.str("Player->Entity"), en -> Lng.str("Player->Entity")),
			PLAYER_2_SECTOR(40,
					new PlayerActionFactory() {
				@Override
				public PlayerAction instantiateAction() {
					return new Player2SectorAction();
				}

			}, en -> Lng.str("Player->Sector"), en -> Lng.str("Player->Sector")),
			PLAYER_2_FACTION(41,
					new PlayerActionFactory() {
				@Override
				public PlayerAction instantiateAction() {
					return new Player2FactionAction();
				}

			}, en -> Lng.str("Player->Faction"), en -> Lng.str("Player->Faction")),
			SECTOR_2_PLAYER(42,
					new SectorActionFactory() {
				@Override
				public SectorAction instantiateAction() {
					return new Sector2PlayerAction();
				}

			}, en -> Lng.str("Sector->Player"), en -> Lng.str("Sector->Player")),
			SECTOR_2_SEG(43,
					new SectorActionFactory() {
				@Override
				public SectorAction instantiateAction() {
					return new Sector2EntityAction();
				}

			}, en -> Lng.str("Sector->Player"), en -> Lng.str("Sector->Player")),
			SECTOR_2_FACTION(44,
					new SectorActionFactory() {
				@Override
				public SectorAction instantiateAction() {
					return new Sector2FactionAction();
				}

			}, en -> Lng.str("Sector->Faction"), en -> Lng.str("Sector->Faction")),
			SEG_2_FACTION(45,
					new SegmentControllerActionFactory() {
				@Override
				public SegmentControllerAction instantiateAction() {
					return new SegmentController2FactionAction();
				}

			}, en -> Lng.str("Entity->Faction"), en -> Lng.str("Entity->Faction")),
			SEG_2_PLAYER(46,
					new SegmentControllerActionFactory() {
				@Override
				public SegmentControllerAction instantiateAction() {
					return new SegmentController2PlayerAction();
				}

			}, en -> Lng.str("Entity->Player"), en -> Lng.str("Entity->Player")),
			SEG_2_SECTOR(47,
					new SegmentControllerActionFactory() {
				@Override
				public SegmentControllerAction instantiateAction() {
					return new SegmentController2SectorAction();
				}

			}, en -> Lng.str("Entity->Sector"), en -> Lng.str("Entity->Sector")),
			PLAYER_MOD_CREDITS_RECURRING(48,
					new PlayerActionFactory() {
				@Override
				public PlayerAction instantiateAction() {
					return new PlayerModCreditsRecurringAction();
				}

			}, en -> Lng.str("Add credits (recurring)"), en -> Lng.str("This adds credits in every specific time interval as long as action active")),
			PLAYER_MOD_BLOCK_TYPE(49,
					new PlayerActionFactory() {
				@Override
				public PlayerAction instantiateAction() {
					return new PlayerModBlockTypeAction();
				}

			}, en -> Lng.str("Add block type"), en -> Lng.str("This adds and amount of blocks to a player")),
			PLAYER_MOD_BLOCK_TYPE_RECURRING(50,
					new PlayerActionFactory() {
				@Override
				public PlayerAction instantiateAction() {
					return new PlayerModBlockTypeRecurringAction();
				}

			}, en -> Lng.str("Add block type (recurring)"), en -> Lng.str("This adds and amount of blocks to a player every a specific time interval as long as action active")),
	;
	public final int UID;
	private final Translatable name;
	private final Translatable desc;
	public final ActionFactory<?, ?> fac;
	private final static Int2ObjectOpenHashMap<ActionTypes> t = new Int2ObjectOpenHashMap<ActionTypes>();
	public static ActionTypes getByUID(int UID) {
		return t.get(UID);
	}
	public String getName() {
		return name.getName(this);
	}
	public String getDesc() {
		return desc.getName(this);
	}
	static {
		
		for(ActionTypes e : values()) {
			if(t.containsKey(e.UID)) {
				throw new RuntimeException("ERROR INITIALIZING org.schema.game.common.controller.rules.rules.actions: duplicate UID");
			}
			if(e.fac.instantiateAction() == null) {
				throw new RuntimeException("ERROR INITIALIZING org.schema.game.common.controller.rules.rules.actions: Factory returned null "+e.name());
			}
			if(e.fac.instantiateAction().getType() != e) {
				throw new RuntimeException("ERROR INITIALIZING org.schema.game.common.controller.rules.rules.actions: Factory type mismatch: "+e.name()+"; "+e.fac.instantiateAction().getType().name());
			}
			t.put(e.UID, e);
		}
	}
	private ActionTypes(int UID, ActionFactory<?, ?> fac, Translatable name, Translatable desc) {
		this.UID = UID;
		this.fac = fac;
		this.name = name;
		this.desc = desc;
	}
	public TopLevelType getType() {
		return fac.getType();
	}
	public static List<ActionTypes> getSortedByName(TopLevelType filter) {
		List<ActionTypes> v = new ObjectArrayList<ActionTypes>();
		for(ActionTypes c : ActionTypes.values()){
			if(c.getType() == filter) {
				v.add(c);
			}
		}
		Collections.sort(v, (o1, o2) -> o1.getName().compareTo(o2.getName()));
		return v;
	}
}
