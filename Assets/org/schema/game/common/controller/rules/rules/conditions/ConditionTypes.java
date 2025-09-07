package org.schema.game.common.controller.rules.rules.conditions;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.common.controller.rules.rules.conditions.faction.*;
import org.schema.game.common.controller.rules.rules.conditions.player.*;
import org.schema.game.common.controller.rules.rules.conditions.sector.SectorChmodCondition;
import org.schema.game.common.controller.rules.rules.conditions.sector.SectorCondition;
import org.schema.game.common.controller.rules.rules.conditions.sector.SectorConditionFactory;
import org.schema.game.common.controller.rules.rules.conditions.sector.SectorRangeCondition;
import org.schema.game.common.controller.rules.rules.conditions.seg.*;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.language.Translatable;
import org.schema.schine.network.TopLevelType;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public enum ConditionTypes{
	SEG_CONDITION_GROUP(0, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerConditionGroup();
		}
	}, en -> Lng.str("Condition Group"), en -> Lng.str("A set of other conditions that becomes true if 'any is true' or 'all are true' (depending on selection)")),
	SEG_MASS_CONDITION(1, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerMassCondition();
		}
	}, en -> Lng.str("Mass"), en -> Lng.str("Condition on mass of an entity being higher or lower than a set value")), SEG_TYPE_COUNT_CONDITION(2, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerTypeCountCondition();
		}
	}, en -> Lng.str("Type Count"), en -> Lng.str("Condition on the amount of a specific block count of an entity being higher or lower than a set value")), SEG_TOTAL_COUNT_CONDITION(3, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerTotalCountCondition();
		}
	}, en -> Lng.str("Total Count"), en -> Lng.str("Condition on the total block count of an entity being higher or lower than a set value")), SEG_SHIELD_CAPACITY_CONDITION(4, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerShieldCapacityCondition();
		}
	}, en -> Lng.str("Shiled Capacity"), en -> Lng.str("Condition on the shield capacity of an entity being higher or lower than a set value")), SEG_REACTOR_LEVEL_CONDITION(5, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerReactorLevelCondition();
		}
	}, en -> Lng.str("Reactor Level"), en -> Lng.str("Condition on the reactor level of an entity being higher or lower than a set value")), SEG_INTEGRITY_STABILIZER_CONDITION(6, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerIntegrityStabilizerCondition();
		}
	}, en -> Lng.str("Stabilizer Integrity"), en -> Lng.str("Condition on the stabilizer integrity of an entity being higher or lower than a set value")), SEG_INTEGRITY_CANNON_CONDITION(7, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerIntegrityCannonCondition();
		}
	}, en -> Lng.str("Cannon Integrity"), en -> Lng.str("Condition on the cannon integrity of an entity being higher or lower than a set value")), SEG_INTEGRITY_MISSILE_CONDITION(8, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerIntegrityMissileCondition();
		}
	}, en -> Lng.str("Missile Integrity"), en -> Lng.str("Condition on the missile integrity of an entity being higher or lower than a set value")), SEG_INTEGRITY_BEAM_CONDITION(9, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerIntegrityBeamCondition();
		}
	}, en -> Lng.str("Beam Integrity"), en -> Lng.str("Condition on the beam integrity of an entity being higher or lower than a set value")), SEG_INTEGRITY_THRUSTER_CONDITION(10, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerIntegrityThrusterCondition();
		}
	}, en -> Lng.str("Thruster Integrity"), en -> Lng.str("Condition on the Thruster Integrity of an entity being higher or lower than a set value")), SEG_INTEGRITY_SHIELD_RECHARGER_CONDITION(11, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerIntegrityShieldRechargeCondition();
		}
	}, en -> Lng.str("Shield Recharge Integrity"), en -> Lng.str("Condition on the Shield Recharge Integrity of an entity being higher or lower than a set value")), SEG_INTEGRITY_SHIELD_CAPACITY_CONDITION(12, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerIntegrityShieldCapacityCondition();
		}
	}, en -> Lng.str("Shield Capacity Integrity"), en -> Lng.str("Condition on the Shield Capacity Integrity of an entity being higher or lower than a set value")), SEG_INTEGRITY_MAIN_REACTOR_CONDITION(13, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerIntegrityMainReactorCondition();
		}
	}, en -> Lng.str("Main Reactor Integrity"), en -> Lng.str("Condition on the main reactor integrity of an entity being higher or lower than a set value")), SEG_INTEGRITY_CHAMBER_CONDITION(14, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerIntegrityReactorChamberCondition();
		}
	}, en -> Lng.str("Reactor Chamber Integrity"), en -> Lng.str("Condition on the reactor chamber integrity of an entity being higher or lower than a set value")),SEG_DOCKED_ENTITES_TOTAL(15, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerDockedEntityTotalCondition();
		}
	}, en -> Lng.str("Docked Entity Count"), en -> Lng.str("Condition on the amount of docked entities on an entity being higher or lower than a set value")),SEG_DOCKED_TURRETS(16, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerDockedTurretsCondition();
		}
	}, en -> Lng.str("Docked Turrets Count"), en -> Lng.str("Condition on the amount of docked turrets on an entity being higher or lower than a set value")),SEG_DOCKED_SHIPS(17, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerDockedShipsCondition();
		}
	}, en -> Lng.str("Docked Ship Count"), en -> Lng.str("Condition on the amount of docked entities (non turrets) on an entity being higher or lower than a set value")),SEG_CHAIN_DOCK_DEPTH(18, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerChainDockDepthCondition();
		}
	}, en -> Lng.str("Docking Chain Depth"), en -> Lng.str("Condition on the Docking Chain Depth of an entity being higher or lower than a set value")),SEG_THRUST_TO_MASS_RATIO(19, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerThrustToMassRatioCondition();
		}
	}, en -> Lng.str("Thrust to Mass Ratio"), en -> Lng.str("Condition on the Thrust to Mass Ratio of an entity being higher or lower than a set value")),SEG_OUTPUTS_PER_CANNON(20, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerCannonOutputCountCondition();
		}
	}, en -> Lng.str("Outputs per Cannon"), en -> Lng.str("Condition on the amount of Outputs on a Cannon of an entity being higher or lower than a set value")),SEG_OUTPUTS_PER_MISSILE(21, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerMissileOutputCountCondition();
		}
	}, en -> Lng.str("Outputs per Missile"), en -> Lng.str("Condition on the amount of Outputs on a Missile Computer of an entity being higher or lower than a set value")),SEG_OUTPUTS_PER_BEAM(22, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerBeamOutputCountCondition();
		}
	}, en -> Lng.str("Outputs per Damage Beam"), en -> Lng.str("Condition on the amount of Outputs on a Damage Beam Computer of an entity being higher or lower than a set value")),SEG_OUTPUTS_PER_SALVAGE(23, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerSalvageOutputCountCondition();
		}
	}, en -> Lng.str("Outputs per Salvager"), en -> Lng.str("Condition on the amount of Outputs on a Salvage Computer of an entity being higher or lower than a set value")),SEG_ALWAYS_TRUE_CONDITION(24, new SegmentControllerConditionFactory() {
			@Override
			public SegmentControllerCondition instantiateCondition() {
				return new SegmentControllerAlwaysTrueCondition();
			}
		}, en -> Lng.str("Always true/false"), en -> Lng.str("This condition is either always true or always false")),
	SEG_LAST_CHECKED(25, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerLastCheckedCondition();
		}
	}, en -> Lng.str("Last checked by admin"), en -> Lng.str("When the last check by an admin was compared to the last change of the entity")),
	SEG_IS_TYPE(26, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerIsTypeCondition();
		}
	}, en -> Lng.str("Is entity type"), en -> Lng.str("Condition on the type of the entity")),
	SEG_TYPE_PERCENT_CONDITION(27, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerTypePercentageCondition();
		}
	}, en -> Lng.str("Type Percentage"), en -> Lng.str("Condition on a specific percentage of a block type of an entity being higher or lower than a set value")),
	SEG_DATE_TIME(29, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerDateTimeCondition();
		}
	}, en -> Lng.str("Date/Time"), en -> Lng.str("Condition triggers at a certain time (server timezone)")),
	SEG_DURATION_ACTIVE(30, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerDurationCondition();
		}
	}, en -> Lng.str("Duration Triggered"), en -> Lng.str("Condition on how all other conditions have been triggered. Always true when untriggered")),
	SEG_UNDER_ATTACK(31, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerUnderAttackCondition();
		}
	}, en -> Lng.str("Under Attack"), en -> Lng.str("Condition on entity under attack (taken damage)")),
	SEG_SAME_SECTOR_CONTAINS(32, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerSameSectorContainsCondition();
		}
	}, en -> Lng.str("Same Sector Contains"), en -> Lng.str("Condition on the same sector of the entity containing entity type with specific relationship")),
	SEG_ADJACENT_SECTOR_CONTAINS(33, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerAdjacentSectorContainsCondition();
		}
	}, en -> Lng.str("Adj. Sector Contains"), en -> Lng.str("Condition on adjacent sector of the entity containing entity type with specific relationship")),
	SEG_SAME_SYSTEM_CONTAINS(34, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerSameSystemContainsCondition();
		}
	}, en -> Lng.str("Same System Contains"), en -> Lng.str("Condition on the same system of the entity containing entity type with specific relationship")),
	SEG_IS_IN_FLEET(35, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerInFleetCondition();
		}
	}, en -> Lng.str("Is in a fleet"), en -> Lng.str("Condition on if the entity is in any fleet")),
	SEG_IS_FACTION(36, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerFactionCondition();
		}
	}, en -> Lng.str("Is in a faction"), en -> Lng.str("Condition on the faction on an entity")),
	SEG_IS_AI_ACTIVE(37, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerAIActiveCondition();
		}
	}, en -> Lng.str("Is AI active"), en -> Lng.str("Condition on if the AI of a ship is active")),
	SEG_IS_HOMEBASE(38, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerIsHomebaseCondition();
		}
	}, en -> Lng.str("Is Homebase"), en -> Lng.str("Condition on if the entity is a homebase")),
	SECTOR_CHMOD(39, new SectorConditionFactory() {
		@Override
		public SectorCondition instantiateCondition() {

			return new SectorChmodCondition();
		}
	}, en -> Lng.str("Is Homebase"), en -> Lng.str("Condition on if the entity is a homebase")),
	SECTOR_RANGE(40, new SectorConditionFactory() {
		@Override
		public SectorCondition instantiateCondition() {

			return new SectorRangeCondition();
		}
		}, en -> Lng.str("Sector Range"), en -> Lng.str("Condition on if the sector is in a specific range")),
	SEG_IS_IN_SECTOR(41, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerInSectorCondition();
		}
	}, en -> Lng.str("Is in a sector range"), en -> Lng.str("Condition on if the entity is in a specific range of sectors")),
	PLAYER_IN_SECTOR(42, new PlayerConditionFactory() {
		@Override
		public PlayerCondition instantiateCondition() {
			return new PlayerInSectorCondition();
		}
	}, en -> Lng.str("Player is in a sector range"), en -> Lng.str("Condition on if the player is in a specific range of sectors")),
	PLAYER_SAY(43, new PlayerConditionFactory() {
		@Override
		public PlayerCondition instantiateCondition() {
			return new PlayerSayCondition();
		}
	}, en -> Lng.str("Player says something in chat"), en -> Lng.str("Condition on if the player is saying something in chat")),
	PLAYER_JOINED_AFTER_SECS(44, new PlayerConditionFactory() {
		@Override
		public PlayerCondition instantiateCondition() {
			return new PlayerSecondsSinceJoinedCondition();
		}
	}, en -> Lng.str("Seconds since player joined"), en -> Lng.str("Condition will be true after x seconds after player joined")),
	PLAYER_HAS_CREDITS(45, new PlayerConditionFactory() {
		@Override
		public PlayerCondition instantiateCondition() {
			return new PlayerHasCreditsCondition();
		}
	}, en -> Lng.str("Player Has Credits"), en -> Lng.str("Condition on if player has more or less credits than a specified value")),
	SEG_WEEKLY_DURATION(46, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerRepeatingWeeklyDurationCondition();
		}
	}, en -> Lng.str("Weekly Duration"), en -> Lng.str("Condition that is true for a weekly duration")),
	FACTION_MEMBER_COUNT(47, new FactionConditionFactory() {
		@Override
		public FactionCondition instantiateCondition() {
			return new FactionMemberCountCondition();
		}
	}, en -> Lng.str("Member Count"), en -> Lng.str("Condition on the amount of members in a faction")),
	FACTION_FP_COUNT(48, new FactionConditionFactory() {
		@Override
		public FactionCondition instantiateCondition() {
			return new FactionFPCountCondition();
		}
	}, en -> Lng.str("Faction Point Count"), en -> Lng.str("Condition on the amount of faction points of a faction")),
	FACTION_RELATIONSHIP_COUNT(49, new FactionConditionFactory() {
		@Override
		public FactionCondition instantiateCondition() {
			return new FactionRelationshipCountCondition();
		}
	}, en -> Lng.str("Relationship Count"), en -> Lng.str("Condition on the amount of specific relationships to other factions")),
	FACTION_IS_RANGE(50, new FactionConditionFactory() {
		@Override
		public FactionCondition instantiateCondition() {
			return new FactionInRangeCondition();
		}
	}, en -> Lng.str("Faction in id range"), en -> Lng.str("Condition on if the faction is in a specific range of ids")),
	SEG_SYSTEM_RELATIONSHIP(51, new SegmentControllerConditionFactory() {
		@Override
		public SegmentControllerCondition instantiateCondition() {
			return new SegmentControllerSystemOwnedByCondition();
		}
	}, en -> Lng.str("Entity system relation"), en -> Lng.str("Condition on the relation to the system ownership")),
	PLAYER_ALWAYS_TRUE(52, new PlayerConditionFactory() {
		@Override
		public PlayerCondition instantiateCondition() {
			return new PlayerAlwaysTrueCondition();
		}
	}, en -> Lng.str("Always True"), en -> Lng.str("Always True")),
	;
	public final ConditionFactory<?, ?> fac;
	public final int UID;
	private final Translatable name;
	private final Translatable description;
	public String getName() {
		return name.getName(this);
	}
	public String getDesc() {
		return description.getName(this);
	}
	
	private final static Int2ObjectOpenHashMap<ConditionTypes> t = new Int2ObjectOpenHashMap<ConditionTypes>();
	public static ConditionTypes getByUID(int UID) {
		return t.get(UID);
	}
	private ConditionTypes(final int UID, ConditionFactory<?, ?> fac, Translatable name, Translatable description) {
		this.fac = fac;
		this.UID = UID;
		this.name = name;
		this.description = description;
	}
	public static List<ConditionTypes> getSortedByName(TopLevelType filter){
		List<ConditionTypes> v = new ObjectArrayList<ConditionTypes>();
		for(ConditionTypes c : ConditionTypes.values()){
			if(c.getType() == filter) {
				v.add(c);
			}
		}
		Collections.sort(v, (o1, o2) -> o1.getName().compareTo(o2.getName()));
		return v;
	}
	public TopLevelType getType(){
		return fac.getType();
	}
	static {
		
		for(ConditionTypes e : values()) {
			if(t.containsKey(e.UID)) {
				throw new RuntimeException("ERROR INITIALIZING org.schema.game.common.controller.rules.rules.conditions: duplicate UID");
			}
			if(e.fac.instantiateCondition() == null) {
				throw new RuntimeException("ERROR INITIALIZING org.schema.game.common.controller.rules.rules.conditions: Factory returned null "+e.name());
			}
			if(e.fac.instantiateCondition().getType() != e) {
				throw new RuntimeException("ERROR INITIALIZING org.schema.game.common.controller.rules.rules.conditions: Factory type mismatch: "+e.name()+"; "+e.fac.instantiateCondition().getType().name());
			}
			t.put(e.UID, e);
		}
	}
	
}
