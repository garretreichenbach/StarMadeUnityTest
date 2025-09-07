package org.schema.game.client.view.textbox;

import java.util.List;
import java.util.Locale;

import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.ShieldContainerInterface;
import org.schema.game.common.controller.elements.ShieldLocal;
import org.schema.game.common.controller.elements.ShieldLocalAddOn;
import org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager;
import org.schema.game.common.controller.elements.armorhp.ArmorHPCollection;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;
import org.schema.game.common.controller.elements.power.reactor.PowerInterface;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorTree;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.schine.common.language.Lng;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import static org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager.WeaponType.*;

public class Replacements {
	private static final Vector3i tmp = new Vector3i();

	interface RFactory {
		public String getValue(SegmentController c, int index);

		public boolean ok(SegmentController c);
	}

	private static final int REPLACE_AVAILABILITY_NEW_POWER = 1;
	private static final int REPLACE_AVAILABILITY_OLD_POWER = 2;
	private static final int REPLACE_AVAILABILITY_ALL = REPLACE_AVAILABILITY_NEW_POWER | REPLACE_AVAILABILITY_OLD_POWER;

	public enum Type {
		SHIELD("shield", REPLACE_AVAILABILITY_OLD_POWER, new RFactory() {
			@Override
			public String getValue(SegmentController c, int index) {
				return StringTools.formatPointZero(((ShieldContainerInterface) ((ManagedSegmentController<?>) c).getManagerContainer()).getShieldAddOn().getShields());
			}

			@Override
			public boolean ok(SegmentController c) {
				return (c instanceof ManagedSegmentController<?>) && ((ManagedSegmentController<?>) c).getManagerContainer() instanceof ShieldContainerInterface;
			}
		}),
		SHIELD_CAP("shieldCap", REPLACE_AVAILABILITY_OLD_POWER, new RFactory() {
			@Override
			public String getValue(SegmentController c, int index) {
				return StringTools.formatPointZero(((ShieldContainerInterface) ((ManagedSegmentController<?>) c).getManagerContainer()).getShieldAddOn().getShieldCapacity());
			}

			@Override
			public boolean ok(SegmentController c) {
				return (c instanceof ManagedSegmentController<?>) && ((ManagedSegmentController<?>) c).getManagerContainer() instanceof ShieldContainerInterface;
			}
		}),
		SHIELD_PERCENT("shieldPercent", REPLACE_AVAILABILITY_OLD_POWER, new RFactory() {
			@Override
			public String getValue(SegmentController c, int index) {
				double max = ((ShieldContainerInterface) ((ManagedSegmentController<?>) c).getManagerContainer()).getShieldAddOn().getShieldCapacity();
				double p = ((ShieldContainerInterface) ((ManagedSegmentController<?>) c).getManagerContainer()).getShieldAddOn().getShields();
				return StringTools.formatPointZero((p / Math.max(0.00001, max)) * 100.0);
			}

			@Override
			public boolean ok(SegmentController c) {
				return (c instanceof ManagedSegmentController<?>) && ((ManagedSegmentController<?>) c).getManagerContainer() instanceof ShieldContainerInterface;
			}
		}),
		POWER("power", REPLACE_AVAILABILITY_OLD_POWER, new RFactory() {
			@Override
			public String getValue(SegmentController c, int index) {
				return StringTools.formatPointZero(((PowerManagerInterface) ((ManagedSegmentController<?>) c).getManagerContainer()).getPowerAddOn().getPower());
			}

			@Override
			public boolean ok(SegmentController c) {
				return (c instanceof ManagedSegmentController<?>) && ((ManagedSegmentController<?>) c).getManagerContainer() instanceof PowerManagerInterface;
			}
		}),
		POWER_CAP("powerCap", REPLACE_AVAILABILITY_OLD_POWER, new RFactory() {
			@Override
			public String getValue(SegmentController c, int index) {
				return StringTools.formatPointZero(((PowerManagerInterface) ((ManagedSegmentController<?>) c).getManagerContainer()).getPowerAddOn().getMaxPower());
			}

			@Override
			public boolean ok(SegmentController c) {
				return (c instanceof ManagedSegmentController<?>) && ((ManagedSegmentController<?>) c).getManagerContainer() instanceof PowerManagerInterface;
			}
		}),
		POWER_PERCENT("powerPercent", REPLACE_AVAILABILITY_OLD_POWER, new RFactory() {
			@Override
			public String getValue(SegmentController c, int index) {
				double max = ((PowerManagerInterface) ((ManagedSegmentController<?>) c).getManagerContainer()).getPowerAddOn().getMaxPower();
				double p = ((PowerManagerInterface) ((ManagedSegmentController<?>) c).getManagerContainer()).getPowerAddOn().getPower();
				return StringTools.formatPointZero((p / Math.max(0.00001, max)) * 100.0);
			}

			@Override
			public boolean ok(SegmentController c) {
				return (c instanceof ManagedSegmentController<?>) && ((ManagedSegmentController<?>) c).getManagerContainer() instanceof PowerManagerInterface;
			}
		}),
		POWER_BATTERY("auxPower", REPLACE_AVAILABILITY_OLD_POWER, new RFactory() {
			@Override
			public String getValue(SegmentController c, int index) {
				return StringTools.formatPointZero(((PowerManagerInterface) ((ManagedSegmentController<?>) c).getManagerContainer()).getPowerAddOn().getBatteryPower());
			}

			@Override
			public boolean ok(SegmentController c) {
				return (c instanceof ManagedSegmentController<?>) && ((ManagedSegmentController<?>) c).getManagerContainer() instanceof PowerManagerInterface;
			}
		}),
		POWER_BATTERY_CAP("auxPowerCap", REPLACE_AVAILABILITY_OLD_POWER, new RFactory() {
			@Override
			public String getValue(SegmentController c, int index) {
				return StringTools.formatPointZero(((PowerManagerInterface) ((ManagedSegmentController<?>) c).getManagerContainer()).getPowerAddOn().getBatteryMaxPower());
			}

			@Override
			public boolean ok(SegmentController c) {
				return (c instanceof ManagedSegmentController<?>) && ((ManagedSegmentController<?>) c).getManagerContainer() instanceof PowerManagerInterface;
			}
		}),
		POWER_BATTERY_PERCENT("auxPowerPercent", REPLACE_AVAILABILITY_OLD_POWER, new RFactory() {
			@Override
			public String getValue(SegmentController c, int index) {
				double max = ((PowerManagerInterface) ((ManagedSegmentController<?>) c).getManagerContainer()).getPowerAddOn().getBatteryMaxPower();
				double p = ((PowerManagerInterface) ((ManagedSegmentController<?>) c).getManagerContainer()).getPowerAddOn().getBatteryPower();
				return StringTools.formatPointZero((p / Math.max(0.00001, max)) * 100.0);
			}

			@Override
			public boolean ok(SegmentController c) {
				return (c instanceof ManagedSegmentController<?>) && ((ManagedSegmentController<?>) c).getManagerContainer() instanceof PowerManagerInterface;
			}
		}),
		STRUCTURE_HP("structureHp", REPLACE_AVAILABILITY_OLD_POWER, new RFactory() {
			@Override
			public String getValue(SegmentController c, int index) {
				return StringTools.formatSmallAndBig(c.getHpController().getHp());
			}

			@Override
			public boolean ok(SegmentController c) {
				return true;
			}
		}),
		STRUCTURE_HP_CAPACITY("structureHpCap", REPLACE_AVAILABILITY_OLD_POWER, new RFactory() {
			@Override
			public String getValue(SegmentController c, int index) {
				return StringTools.formatSmallAndBig(c.getHpController().getMaxHp());
			}

			@Override
			public boolean ok(SegmentController c) {
				return true;
			}
		}),
		STRUCTURE_HP_PERCENT("structureHpPercent", REPLACE_AVAILABILITY_OLD_POWER, new RFactory() {
			@Override
			public String getValue(SegmentController c, int index) {
				return StringTools.formatPointZero((c.getHpController().getHp() / (Math.max(0.00001, c.getHpController().getMaxHp()))) * 100);
			}

			@Override
			public boolean ok(SegmentController c) {
				return true;
			}
		}),
		REACTOR_HP("activeReactorHp", REPLACE_AVAILABILITY_NEW_POWER, new RFactory() {
			@Override
			public String getValue(SegmentController c, int index) {
				return StringTools.formatSmallAndBig(c.getHpController().getHp());
			}

			@Override
			public boolean ok(SegmentController c) {
				return true;
			}
		}),
		REACTOR_HP_CAPACITY("activeReactorMaxHp", REPLACE_AVAILABILITY_NEW_POWER, new RFactory() {
			@Override
			public String getValue(SegmentController c, int index) {
				return StringTools.formatSmallAndBig(c.getHpController().getMaxHp());
			}

			@Override
			public boolean ok(SegmentController c) {
				return true;
			}
		}),
		REACTOR_HP_PERCENT("activeReactorHpPercent", REPLACE_AVAILABILITY_NEW_POWER, new RFactory() {
			@Override
			public String getValue(SegmentController c, int index) {
				return StringTools.formatPointZero((c.getHpController().getHp() / (Math.max(0.00001, c.getHpController().getMaxHp()))) * 100);
			}

			@Override
			public boolean ok(SegmentController c) {
				return true;
			}
		}),
		ARMOR_HP("armorHp", REPLACE_AVAILABILITY_ALL, new RFactory() {
			@Override
			public String getValue(SegmentController c, int index) {
				return StringTools.formatSmallAndBig(ArmorHPCollection.getCollection(c).getCurrentHP());
			}

			@Override
			public boolean ok(SegmentController c) {
				return true;
			}
		}),
		ARMOR_HP_CAPACITY("armorHpCap", REPLACE_AVAILABILITY_ALL, new RFactory() {
			@Override
			public String getValue(SegmentController c, int index) {
				return StringTools.formatSmallAndBig(ArmorHPCollection.getCollection(c).getMaxHP());
			}

			@Override
			public boolean ok(SegmentController c) {
				return true;
			}
		}),
		ARMOR_PERCENT("armorHpPercent", REPLACE_AVAILABILITY_ALL, new RFactory() {
			@Override
			public String getValue(SegmentController c, int index) {
				return StringTools.formatPointZero((ArmorHPCollection.getCollection(c).getHPPercent() * 100));
			}

			@Override
			public boolean ok(SegmentController c) {
				return true;
			}
		}),
		MASS("mass", REPLACE_AVAILABILITY_ALL, new RFactory() {
			@Override
			public String getValue(SegmentController c, int index) {
				return StringTools.formatPointZero(c.getTotalPhysicalMass());
			}

			@Override
			public boolean ok(SegmentController c) {
				return true;
			}
		}),
		BLOCK_COUNT("blockCount", REPLACE_AVAILABILITY_ALL, new RFactory() {
			@Override
			public String getValue(SegmentController c, int index) {
				return StringTools.formatPointZero(c.getTotalElements());
			}

			@Override
			public boolean ok(SegmentController c) {
				return true;
			}
		}),
		SECTOR("sector", REPLACE_AVAILABILITY_ALL, new RFactory() {
			@Override
			public String getValue(SegmentController c, int index) {
				return c.getClientSector().toStringPure();
			}

			@Override
			public boolean ok(SegmentController c) {
				return true;
			}
		}),
		SYSTEM("system", REPLACE_AVAILABILITY_ALL, new RFactory() {

			@Override
			public String getValue(SegmentController c, int index) {
				return VoidSystem.getContainingSystem(c.getClientSector(), tmp).toStringPure();
			}

			@Override
			public boolean ok(SegmentController c) {
				return true;
			}
		}),
		NAME("name", REPLACE_AVAILABILITY_ALL, new RFactory() {

			@Override
			public String getValue(SegmentController c, int index) {
				return c.getRealName();
			}

			@Override
			public boolean ok(SegmentController c) {
				return true;
			}
		}),
		DOCKED("docked", REPLACE_AVAILABILITY_ALL, new RFactory() {

			@Override
			public String getValue(SegmentController c, int index) {
				return c.railController.isDockedAndExecuted() ? Lng.str("docked") : Lng.str("undocked");
			}

			@Override
			public boolean ok(SegmentController c) {
				return true;
			}
		}),
		CLOAKED("cloaked", REPLACE_AVAILABILITY_ALL, new RFactory() {

			@Override
			public String getValue(SegmentController c, int index) {
				return ((Ship) c).isCloakedFor(null) ? Lng.str("on") : Lng.str("off");
			}

			@Override
			public boolean ok(SegmentController c) {
				return c instanceof Ship;
			}
		}),
		JAMMING("jamming", REPLACE_AVAILABILITY_ALL, new RFactory() {

			@Override
			public String getValue(SegmentController c, int index) {
				return ((Ship) c).isJammingFor(null) ? Lng.str("on") : Lng.str("off");
			}

			@Override
			public boolean ok(SegmentController c) {
				return c instanceof Ship;
			}
		}),
		SPEED("speed", REPLACE_AVAILABILITY_ALL, new RFactory() {

			@Override
			public String getValue(SegmentController c, int index) {
				return StringTools.formatPointZero(((Ship) c).getSpeedCurrent());
			}

			@Override
			public boolean ok(SegmentController c) {
				return c instanceof Ship;
			}
		}),
		REACTORIDACTIVE("activeReactorId", REPLACE_AVAILABILITY_NEW_POWER, new RFactory() {

			@Override
			public String getValue(SegmentController c, int index) {
				if(c instanceof ManagedSegmentController<?>) {
					PowerInterface pw = ((ManagedSegmentController<?>) c).getManagerContainer().getPowerInterface();
					return String.valueOf(pw.getActiveReactorId());
				}
				return Lng.str("invalid reactor index");
			}

			@Override
			public boolean ok(SegmentController c) {
				return !c.isUsingOldPower() && c instanceof ManagedSegmentController<?> && c.hasActiveReactors();
			}
		}),
		REACTORRECHARGEACTIVE("activeReactorRecharge", REPLACE_AVAILABILITY_NEW_POWER, new RFactory() {

			@Override
			public String getValue(SegmentController c, int index) {
				if(c instanceof ManagedSegmentController<?>) {
					PowerInterface pw = ((ManagedSegmentController<?>) c).getManagerContainer().getPowerInterface();
					return StringTools.formatSmallAndBig(pw.getRechargeRatePowerPerSec());
				}
				return Lng.str("invalid reactor index");
			}

			@Override
			public boolean ok(SegmentController c) {
				return !c.isUsingOldPower() && c instanceof ManagedSegmentController<?> && c.hasActiveReactors();
			}
		}),
		REACTORCONSUMPIONACTIVE("activeReactorConsumption", REPLACE_AVAILABILITY_NEW_POWER, new RFactory() {

			@Override
			public String getValue(SegmentController c, int index) {
				if(c instanceof ManagedSegmentController<?>) {
					PowerInterface pw = ((ManagedSegmentController<?>) c).getManagerContainer().getPowerInterface();
					return StringTools.formatSmallAndBig(pw.getCurrentConsumptionPerSec());
				}
				return Lng.str("invalid reactor index");
			}

			@Override
			public boolean ok(SegmentController c) {
				return !c.isUsingOldPower() && c instanceof ManagedSegmentController<?> && c.hasActiveReactors();
			}
		}),
		REACTORCONSUMPIONPERCENTACTIVE("activeReactorConsumptionPercent", REPLACE_AVAILABILITY_NEW_POWER, new RFactory() {

			@Override
			public String getValue(SegmentController c, int index) {
				if(c instanceof ManagedSegmentController<?>) {
					PowerInterface pw = ((ManagedSegmentController<?>) c).getManagerContainer().getPowerInterface();
					return StringTools.formatPointZero(pw.getPowerConsumptionAsPercent() * 100f);
				}
				return Lng.str("invalid reactor index");
			}

			@Override
			public boolean ok(SegmentController c) {
				return !c.isUsingOldPower() && c instanceof ManagedSegmentController<?> && c.hasActiveReactors();
			}
		}),

		REACTORIDX("reactorId", REPLACE_AVAILABILITY_NEW_POWER, 1000, new RFactory() {

			@Override
			public String getValue(SegmentController c, int index) {
				if(c instanceof ManagedSegmentController<?>) {
					PowerInterface pw = ((ManagedSegmentController<?>) c).getManagerContainer().getPowerInterface();
					if(index >= 0 && index <= pw.getReactorSet().getTrees().size()) {
						ReactorTree t = pw.getReactorSet().getTrees().get(index);
						return String.valueOf(t.getId());
					}
				}
				return Lng.str("invalid reactor index");
			}

			@Override
			public boolean ok(SegmentController c) {
				return !c.isUsingOldPower() && c instanceof ManagedSegmentController<?> && c.hasAnyReactors();
			}
		}),
		REACTORSIZEX("reactorSize", REPLACE_AVAILABILITY_NEW_POWER, 1000, new RFactory() {

			@Override
			public String getValue(SegmentController c, int index) {
				if(c instanceof ManagedSegmentController<?>) {
					PowerInterface pw = ((ManagedSegmentController<?>) c).getManagerContainer().getPowerInterface();
					if(index >= 0 && index <= pw.getReactorSet().getTrees().size()) {
						ReactorTree t = pw.getReactorSet().getTrees().get(index);
						return StringTools.formatSmallAndBig(t.getActualSize());
					}
				}
				return Lng.str("invalid reactor index");
			}

			@Override
			public boolean ok(SegmentController c) {
				return !c.isUsingOldPower() && c instanceof ManagedSegmentController<?> && c.hasAnyReactors();
			}
		}),
		REACTORHPX("reactorHp", REPLACE_AVAILABILITY_NEW_POWER, 1000, new RFactory() {

			@Override
			public String getValue(SegmentController c, int index) {
				if(c instanceof ManagedSegmentController<?>) {
					PowerInterface pw = ((ManagedSegmentController<?>) c).getManagerContainer().getPowerInterface();
					if(index >= 0 && index <= pw.getReactorSet().getTrees().size()) {
						ReactorTree t = pw.getReactorSet().getTrees().get(index);
						return StringTools.formatSmallAndBig(t.getHp());
					}
				}
				return Lng.str("invalid reactor index");
			}

			@Override
			public boolean ok(SegmentController c) {
				return !c.isUsingOldPower() && c instanceof ManagedSegmentController<?> && c.hasAnyReactors();
			}
		}),
		REACTORMAXHPX("reactorHp", REPLACE_AVAILABILITY_NEW_POWER, 1000, new RFactory() {

			@Override
			public String getValue(SegmentController c, int index) {
				if(c instanceof ManagedSegmentController<?>) {
					PowerInterface pw = ((ManagedSegmentController<?>) c).getManagerContainer().getPowerInterface();
					if(index >= 0 && index <= pw.getReactorSet().getTrees().size()) {
						ReactorTree t = pw.getReactorSet().getTrees().get(index);
						return StringTools.formatSmallAndBig(t.getMaxHp());
					}
				}
				return Lng.str("invalid reactor index");
			}

			@Override
			public boolean ok(SegmentController c) {
				return !c.isUsingOldPower() && c instanceof ManagedSegmentController<?> && c.hasAnyReactors();
			}
		}),

		SHIELDIDX("shieldId", REPLACE_AVAILABILITY_NEW_POWER, 1000, new RFactory() {

			@Override
			public String getValue(SegmentController c, int index) {
				ShieldContainerInterface sci = (ShieldContainerInterface) ((ManagedSegmentController<?>) c).getManagerContainer();
				ShieldLocalAddOn shieldLocalAddOn = sci.getShieldAddOn().getShieldLocalAddOn();
				if(index >= 0 && index < shieldLocalAddOn.getActiveShields().size()) {
					ShieldLocal l = shieldLocalAddOn.getActiveShields().get(index);
					return String.valueOf(l.mainId);
				}
				return Lng.str("invalid shield index");
			}

			@Override
			public boolean ok(SegmentController c) {
				return c.isUsingLocalShields();
			}
		}),
		SHIELDPERCENTX("shieldPercent", REPLACE_AVAILABILITY_NEW_POWER, 1000, new RFactory() {

			@Override
			public String getValue(SegmentController c, int index) {
				ShieldContainerInterface sci = (ShieldContainerInterface) ((ManagedSegmentController<?>) c).getManagerContainer();
				ShieldLocalAddOn shieldLocalAddOn = sci.getShieldAddOn().getShieldLocalAddOn();
				if(index >= 0 && index < shieldLocalAddOn.getActiveShields().size()) {
					ShieldLocal l = shieldLocalAddOn.getActiveShields().get(index);
					return StringTools.formatPointZero(l.getPercentOne() * 100f);
				}
				return Lng.str("invalid shield index");
			}

			@Override
			public boolean ok(SegmentController c) {
				return c.isUsingLocalShields();
			}
		}),
		SHIELDHPX("shieldHp", REPLACE_AVAILABILITY_NEW_POWER, 1000, new RFactory() {

			@Override
			public String getValue(SegmentController c, int index) {
				ShieldContainerInterface sci = (ShieldContainerInterface) ((ManagedSegmentController<?>) c).getManagerContainer();
				ShieldLocalAddOn shieldLocalAddOn = sci.getShieldAddOn().getShieldLocalAddOn();
				if(index >= 0 && index < shieldLocalAddOn.getActiveShields().size()) {
					ShieldLocal l = shieldLocalAddOn.getActiveShields().get(index);
					return StringTools.formatPointZero(l.getShields());
				}
				return Lng.str("invalid shield index");
			}

			@Override
			public boolean ok(SegmentController c) {
				return c.isUsingLocalShields();
			}
		}),
		SHIELDCAPX("shieldMaxHp", REPLACE_AVAILABILITY_NEW_POWER, 1000, new RFactory() {

			@Override
			public String getValue(SegmentController c, int index) {
				ShieldContainerInterface sci = (ShieldContainerInterface) ((ManagedSegmentController<?>) c).getManagerContainer();
				ShieldLocalAddOn shieldLocalAddOn = sci.getShieldAddOn().getShieldLocalAddOn();
				if(index >= 0 && index < shieldLocalAddOn.getActiveShields().size()) {
					ShieldLocal l = shieldLocalAddOn.getActiveShields().get(index);
					return StringTools.formatPointZero(l.getShieldCapacity());
				}
				return Lng.str("invalid shield index");
			}

			@Override
			public boolean ok(SegmentController c) {
				return c.isUsingLocalShields();
			}
		}),
		SHIELDRADIUSX("shieldRadius", REPLACE_AVAILABILITY_NEW_POWER, 1000, new RFactory() {

			@Override
			public String getValue(SegmentController c, int index) {
				ShieldContainerInterface sci = (ShieldContainerInterface) ((ManagedSegmentController<?>) c).getManagerContainer();
				ShieldLocalAddOn shieldLocalAddOn = sci.getShieldAddOn().getShieldLocalAddOn();
				if(index >= 0 && index < shieldLocalAddOn.getActiveShields().size()) {
					ShieldLocal l = shieldLocalAddOn.getActiveShields().get(index);
					return StringTools.formatPointZero(l.radius);
				}
				return Lng.str("invalid shield index");
			}

			@Override
			public boolean ok(SegmentController c) {
				return c.isUsingLocalShields();
			}
		}),
		MISSILE_CAPACITY("missileCapacity", REPLACE_AVAILABILITY_ALL, new RFactory() {

			@Override
			public String getValue(SegmentController c, int index) {
				return StringTools.formatPointZero(c.getAmmoCapacity(MISSILE));
			}

			@Override
			public boolean ok(SegmentController c) {
				return c.isUsingLocalShields();
			}
		}),
		MISSILE_CAPACITY_MAX("missileCapacityMax", REPLACE_AVAILABILITY_ALL, new RFactory() {

			@Override
			public String getValue(SegmentController c, int index) {
				return StringTools.formatPointZero(c.getAmmoCapacityMax(MISSILE));
			}

			@Override
			public boolean ok(SegmentController c) {
				return c.isUsingLocalShields();
			}
		}),
		CANNON_CAPACITY("cannonCapacity", REPLACE_AVAILABILITY_ALL, new RFactory() {

			@Override
			public String getValue(SegmentController c, int index) {
				return StringTools.formatPointZero(c.getAmmoCapacity(CANNON));
			}

			@Override
			public boolean ok(SegmentController c) {
				return c.isUsingLocalShields();
			}
		}),
		CANNON_CAPACITY_MAX("cannonCapacityMax", REPLACE_AVAILABILITY_ALL, new RFactory() {

			@Override
			public String getValue(SegmentController c, int index) {
				return StringTools.formatPointZero(c.getAmmoCapacityMax(CANNON));
			}

			@Override
			public boolean ok(SegmentController c) {
				return c.isUsingLocalShields();
			}
		}),
		BEAM_CAPACITY("beamCapacity", REPLACE_AVAILABILITY_ALL, new RFactory() {

			@Override
			public String getValue(SegmentController c, int index) {
				return StringTools.formatPointZero(c.getAmmoCapacity(BEAM));
			}

			@Override
			public boolean ok(SegmentController c) {
				return c.isUsingLocalShields();
			}
		}),
		BEAM_CAPACITY_MAX("beamCapacityMax", REPLACE_AVAILABILITY_ALL, new RFactory() {

			@Override
			public String getValue(SegmentController c, int index) {
				return StringTools.formatPointZero(c.getAmmoCapacityMax(BEAM));
			}

			@Override
			public boolean ok(SegmentController c) {
				return c.isUsingLocalShields();
			}
		}),

		;
		public final String var;
		public final RFactory fac;
		private final int availability;
		/**
		 * for elements that require an index (e.g. multiple reactors or multiple local shields)
		 **/
		public final int takesIndex;

		private Type(String name, int availability, RFactory fac) {
			this(name, availability, 0, fac);
		}

		private Type(String name, int availability, int takesIndex, RFactory fac) {
			this.var = name;
			this.fac = fac;
			this.availability = availability;
			this.takesIndex = takesIndex;

		}

	}

	public static String getVariables(boolean oldPower) {
		StringBuffer b = new StringBuffer();
		List<Replacements.Type> r = new ObjectArrayList();
		for(int i = 0; i < Replacements.Type.values().length; i++) {
			Replacements.Type t = Replacements.Type.values()[i];
			if((oldPower && (t.availability & REPLACE_AVAILABILITY_OLD_POWER) == REPLACE_AVAILABILITY_OLD_POWER) ||
					(!oldPower && (t.availability & REPLACE_AVAILABILITY_NEW_POWER) == REPLACE_AVAILABILITY_NEW_POWER)) {
				r.add(t);
			}
		}
		for(int i = 0; i < r.size(); i++) {
			Replacements.Type t = r.get(i);
			b.append("[");
			b.append(t.var.toLowerCase(Locale.ENGLISH));
			if(t.takesIndex > 0) {
				b.append("#");
			}
			b.append("]");
			if(i < Replacements.Type.values().length - 1) {
				b.append(", ");
			}
		}
		b.append(Lng.str("Replace # with index (0, 1, 2, 3, ...) corresponding to the id order"));
		return b.toString();
	}

	public static void main(String[] sdf) {
		System.out.println(getVariables(false));
	}

}
