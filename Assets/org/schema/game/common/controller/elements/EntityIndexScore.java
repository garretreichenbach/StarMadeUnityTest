package org.schema.game.common.controller.elements;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.schema.common.SerializationInterface;
import org.schema.common.util.StringTools;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.newgui.PolygonStatsInterface;

import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

public class EntityIndexScore implements PolygonStatsInterface, SerializationInterface {

	public final static Short2ObjectOpenHashMap<EntityIndexScoreSerializationInterface> serializationMap = new Short2ObjectOpenHashMap<>();
	

	static {
		serializationMap.put((short) 0, new EntityIndexScoreSerializationInterface() {

			@Override
			public void serialize(EntityIndexScore s, DataOutput b,
			                      boolean isOnServer) throws IOException {
				b.writeDouble(s.offensiveIndex);
				b.writeDouble(s.defensiveIndex);
				b.writeDouble(s.powerIndex);
				b.writeDouble(s.mobilityIndex);
				b.writeDouble(s.dangerIndex);
				b.writeDouble(s.survivabilityIndex);
				b.writeDouble(s.offensiveIndex);
				b.writeDouble(s.supportIndex);
			}

			@Override
			public void deserialize(EntityIndexScore s, DataInput b,
			                        int updateSenderStateId, boolean isOnServer)
					throws IOException {
				s.offensiveIndex = b.readDouble();
				s.defensiveIndex = b.readDouble();
				s.powerIndex = b.readDouble();
				s.mobilityIndex = b.readDouble();
				s.dangerIndex = b.readDouble();
				s.survivabilityIndex = b.readDouble();
				s.offensiveIndex = b.readDouble();
				s.supportIndex = b.readDouble();
			}

		});
		serializationMap.put((short) 1, new EntityIndexScoreSerializationInterface() {
			
			@Override
			public void serialize(EntityIndexScore s, DataOutput b,
					boolean isOnServer) throws IOException {
				b.writeDouble(s.offensiveIndex);
				b.writeDouble(s.defensiveIndex);
				b.writeDouble(s.powerIndex);
				b.writeDouble(s.mobilityIndex);
				b.writeDouble(s.dangerIndex);
				b.writeDouble(s.survivabilityIndex);
				b.writeDouble(s.offensiveIndex);
				b.writeDouble(s.supportIndex);
				b.writeDouble(s.miningIndex);
			}
			
			@Override
			public void deserialize(EntityIndexScore s, DataInput b,
					int updateSenderStateId, boolean isOnServer)
							throws IOException {
				s.offensiveIndex = b.readDouble();
				s.defensiveIndex = b.readDouble();
				s.powerIndex = b.readDouble();
				s.mobilityIndex = b.readDouble();
				s.dangerIndex = b.readDouble();
				s.survivabilityIndex = b.readDouble();
				s.offensiveIndex = b.readDouble();
				s.supportIndex = b.readDouble();
				s.miningIndex = b.readDouble();
			}
			
		});
	}

	public final List<EntityIndexScore> children = new ArrayList<EntityIndexScore>();
	public static final short version = 1;
	public double weaponDamageIndex;
	public double weaponRangeIndex;
	public double weaponhitPropabilityIndex;
	public double weaponSpecialIndex;
	public double weaponPowerConsumptionPerSecondIndex;
	public double hitpoints;
	public double armor;
	public double thrust;
	public double shields;
	public double jumpDriveIndex;
	public double mass;
	public double powerRecharge;
	public double maxPower;
	public double support;
	public double mining;
	public boolean docked;
	public boolean turret;
	/**
	 * relevant result
	 **/
	public double offensiveIndex;
	/**
	 * relevant result
	 **/
	public double defensiveIndex;
	/**
	 * relevant result
	 **/
	public double powerIndex;
	/**
	 * relevant result
	 **/
	public double mobilityIndex;
	/**
	 * relevant result
	 **/
	public double dangerIndex;
	/**
	 * relevant result
	 **/
	public double survivabilityIndex;
	/**
	 * relevant result
	 **/
	public double supportIndex;
	
	
	public double miningIndex;
	@Override
	public String toString() {
		return "EntityIndexScore [version=" + version + ", weaponDamageIndex="
				+ weaponDamageIndex + ", weaponRangeIndex=" + weaponRangeIndex
				+ ", weaponhitPropabilityIndex=" + weaponhitPropabilityIndex
				+ ", weaponSpecialIndex=" + weaponSpecialIndex
				+ ", weaponPowerConsumptionPerSecondIndex="
				+ weaponPowerConsumptionPerSecondIndex + ", hitpoints="
				+ hitpoints + ", armor=" + armor + ", thrust=" + thrust
				+ ", shields=" + shields + ", jumpDriveIndex=" + jumpDriveIndex
				+ ", mass=" + mass + ", powerRecharge=" + powerRecharge
				+ ", maxPower=" + maxPower + ", support=" + support
				+ ", docked=" + docked + ", turret=" + turret + ", children="
				+ children + ", offensiveIndex=" + offensiveIndex
				+ ", defensiveIndex=" + defensiveIndex + ", powerIndex="
				+ powerIndex + ", mobilityIndex=" + mobilityIndex
				+ ", dangerIndex=" + dangerIndex + ", survivabilityIndex="
				+ survivabilityIndex + ", supportIndex=" + supportIndex + "]";
	}

	public String toShortString() {
		return "EntityIndexScore [version=" + version +
				",\n offensiveIndex=" + (long) offensiveIndex
				+ ",\n defensiveIndex=" + (long) defensiveIndex +
				",\n powerIndex=" + (long) powerIndex +
				",\n mobilityIndex=" + (long) mobilityIndex
				+ ",\n dangerIndex=" + (long) dangerIndex +
				",\n survivabilityIndex=" + (long) survivabilityIndex +
				",\n miningIndex=" + (long) miningIndex +
				",\n supportIndex=" + (long) supportIndex + "]";
	}

	public void addScores(EntityIndexScore sc) {
		Field[] fields = this.getClass().getFields();
		for (Field f : fields) {
			f.setAccessible(true);
			if (f.getType() == Double.TYPE) {
				try {
					f.setDouble(this, f.getDouble(this) + f.getDouble(sc));
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public int getDataPointsNum() {
		return 5;
	}

	@Override
	public double getPercent(int j) {
		double max = 0;
		for (int i = 0; i < getDataPointsNum(); i++) {
			max = Math.max(getValue(i), max);
		}

		return max > 0 ? getValue(j) / max : 0;
	}

	@Override
	public double getValue(int i) {
		return switch(i) {
			case (0) -> offensiveIndex;
			case (1) -> defensiveIndex;
			case (2) -> mobilityIndex;
			case (3) -> supportIndex;
			case (4) -> miningIndex;
			default -> 0;
		};
	}

	@Override
	public String getValueName(int i) {
		return switch(i) {
			case (0) -> Lng.str("offensive");
			case (1) -> Lng.str("defensive");
			case (2) -> Lng.str("mobility");
			case (3) -> Lng.str("support");
			case (4) -> Lng.str("mining");
			default -> "unknown";
		};
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		b.writeShort(version);
		serializationMap.get(version).serialize(this, b, isOnServer);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
	                        boolean isOnServer) throws IOException {
		short version = b.readShort();
		if(!serializationMap.containsKey(version)) return;
		serializationMap.get(version).deserialize(this, b, updateSenderStateId, isOnServer);
	}

	public void addStrings(Collection<Object> a) {
		for (int i = 0; i < getDataPointsNum(); i++) {
			a.add(Lng.str("%s: %s Points", getValueName(i), StringTools.formatSeperated((long) getValue(i))));
		}
	}

}
