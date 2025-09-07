package org.schema.game.common.controller.elements;

import api.listener.events.calculate.ShieldCapacityCalculateEvent;
import api.listener.events.calculate.ShieldRegenCalculateEvent;
import api.listener.events.systems.ShieldHitEvent;
import api.mod.StarLoader;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import org.schema.common.SerializationInterface;
import org.schema.common.util.CompareTools;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.elements.power.reactor.PowerConsumer;
import org.schema.game.common.controller.elements.shield.capacity.ShieldCapacityUnit;
import org.schema.game.common.controller.elements.shield.regen.ShieldRegenUnit;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.world.Segment;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.resource.tag.TagSerializable;
import org.schema.schine.sound.controller.AudioController;
import org.schema.schine.sound.controller.AudioEmitter;

import javax.vecmath.Vector3f;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ShieldLocal implements SerializationInterface, TagSerializable, Comparable<ShieldLocal>, PowerConsumer, AudioEmitter {
	public static byte VERSION = 0;
	public double shields;
	public double shieldCapacity;
	public double rechargePerSecond;
	public long outputPos;
	public long mainId;
	public final LongArrayList supportIds = new LongArrayList();
	public final LongArrayList supportCoMIds = new LongArrayList();
	public float radius;
	public boolean active = true;
	private float powered;
	private float preventRecharge;
	public ShieldLocalAddOn shieldLocalAddOn;
	private double regenIntegrity;
	private double capacityIntegrity;
	private double preventRechargeNerf;
	private long lastSentUpdate;
	private long lastMgs;

	//@ConfigurationElement(name="EnableLowDamage", description = "Enables Low Damage Chamber, which is disabled by default.")
	public static boolean ENABLE_LOW_DAMAGE = false;

	//@ConfigurationElement(name="EnableHighDamage", description = "Enables High Damage Chamber, which is disabled by default.")
	public static boolean ENABLE_HIGH_DAMAGE = false;

	public ShieldLocal(ShieldLocalAddOn shieldLocalAddOn) {
		super();
		this.shieldLocalAddOn = shieldLocalAddOn;
	}

	public double getIntegrity() {
		return Math.min(regenIntegrity, capacityIntegrity);
	}

	public ShieldLocal() {
	}

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] struct = tag.getStruct();
		byte version = struct[0].getByte();
		mainId = struct[1].getLong();
		shields = struct[2].getDouble();
		shieldCapacity = struct[3].getDouble();
		outputPos = struct[4].getLong();
		rechargePerSecond = struct[5].getDouble();
		active = struct[6].getBoolean();
		radius = struct[7].getFloat();
	}

	@Override
	public Tag toTagStructure() {
		return new Tag(Type.STRUCT, null, new Tag[]{new Tag(Type.BYTE, null, VERSION), new Tag(Type.LONG, null, mainId), new Tag(Type.DOUBLE, null, shields), new Tag(Type.DOUBLE, null, getShieldCapacity()), new Tag(Type.LONG, null, outputPos), new Tag(Type.DOUBLE, null, getRechargeRate()), new Tag(Type.BYTE, null, active ? (byte) 1 : (byte) 0), new Tag(Type.FLOAT, null, radius), FinishTag.INST,});
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeLong(mainId);
		b.writeDouble(shields);
		b.writeDouble(getShieldCapacity());
		b.writeLong(outputPos);
		b.writeDouble(getRechargeRate());
		b.writeBoolean(active);
		b.writeFloat(radius);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		mainId = b.readLong();
		shields = b.readDouble();
		shieldCapacity = b.readDouble();
		outputPos = b.readLong();
		rechargePerSecond = b.readDouble();
		active = b.readBoolean();
		radius = b.readFloat();
	}

	public void updateFrom(ShieldRegenUnit u) {
		switch(VoidElementManager.SHIELD_LOCAL_RADIUS_CALC_STYLE) {
			case LINEAR -> radius = VoidElementManager.SHIELD_LOCAL_DEFAULT_RADIUS + u.size() * VoidElementManager.SHIELD_LOCAL_RADIUS_PER_RECHARGE_BLOCK;
			case EXP -> radius = VoidElementManager.SHIELD_LOCAL_DEFAULT_RADIUS + Math.max(0, (float) Math.pow(u.size(), VoidElementManager.SHIELD_LOCAL_RADIUS_EXP) * VoidElementManager.SHIELD_LOCAL_RADIUS_EXP_MULT);
			case LOG -> radius = VoidElementManager.SHIELD_LOCAL_DEFAULT_RADIUS + Math.max(0, ((float) Math.log10(u.size()) + VoidElementManager.SHIELD_LOCAL_RADIUS_LOG_OFFSET) * VoidElementManager.SHIELD_LOCAL_RADIUS_LOG_FACTOR);
			default -> throw new RuntimeException("Illegal calc style " + VoidElementManager.SHIELD_LOCAL_RADIUS_CALC_STYLE);
		}

		outputPos = ElementCollection.getIndex(u.getCoMOrigin());
		rechargePerSecond = (u.size() * VoidElementManager.SHIELD_LOCAL_RECHARGE_PER_BLOCK);

		// INSERTED CODE
		ShieldRegenCalculateEvent event = new ShieldRegenCalculateEvent(this, shieldLocalAddOn.getSegmentController(), shieldLocalAddOn, rechargePerSecond, u.size());
		StarLoader.fireEvent(event, shieldLocalAddOn.getSegmentController().isOnServer());
		rechargePerSecond = event.shieldRegen;
		///
	}

	public void createFrom(ShieldRegenUnit u) {
		mainId = u.idPos;
		regenIntegrity = u.getIntegrity();
		updateFrom(u);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (mainId ^ (mainId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj instanceof ShieldLocal && mainId == ((ShieldLocal) obj).mainId;
	}

	@Override
	public int compareTo(ShieldLocal o) {
		//biggest first
		return CompareTools.compare(o.radius, radius);
	}

	public boolean containsInRadius(float ox, float oy, float oz) {
		float x = ElementCollection.getPosX(outputPos);
		float y = ElementCollection.getPosY(outputPos);
		float z = ElementCollection.getPosZ(outputPos);
		float dist = Vector3fTools.distance(x, y, z, ox, oy, oz);
		return dist <= radius;
	}


	public boolean containsInRadius(ShieldLocal other) {
		float ox = ElementCollection.getPosX(other.outputPos);
		float oy = ElementCollection.getPosY(other.outputPos);
		float oz = ElementCollection.getPosZ(other.outputPos);

		return containsInRadius(ox, oy, oz);
	}

	public boolean addCapacityUnitIfContains(ShieldCapacityUnit cap) {
		boolean containsInRadius = containsInRadius(cap.getCoMOrigin().x, cap.getCoMOrigin().y, cap.getCoMOrigin().z);

		if(containsInRadius) {
			supportIds.add(cap.idPos);
			supportCoMIds.add(ElementCollection.getIndex(cap.getCoMOrigin()));
			shieldCapacity += (cap.size() * VoidElementManager.SHIELD_LOCAL_CAPACITY_PER_BLOCK);
			capacityIntegrity = Math.min(capacityIntegrity, cap.getIntegrity());
			//INSERTED CODE @178
			ShieldCapacityCalculateEvent event = new ShieldCapacityCalculateEvent(cap, this, this.shieldCapacity);
			StarLoader.fireEvent(ShieldCapacityCalculateEvent.class, event, this.shieldLocalAddOn.getSegmentController().isOnServer());
			this.shieldCapacity = event.getCapacity();
			///
		}
		return containsInRadius;
	}


	public void process(ShieldHitCallback hit) {
		//		if(shieldLocalAddOn.getSegmentController().isOnServer()) {
		//			try {
		//				System.err.println("###########SHIELD CONTAINS: shields: "+(int)shields+"; world "+hit.xWorld+", "+hit.yWorld+", "+hit.zWorld+"; L;; "+hit.xLocalBlock+", "+hit.yLocalBlock+", "+hit.zLocalBlock+"; CONTIANS:: "+containsInRadius(hit)+"; "+shieldLocalAddOn.getSegmentController().getState()+" "+shieldLocalAddOn.getSegmentController()+"; ShieldSector: "+shieldLocalAddOn.getSegmentController().getSector(new Vector3i()));
		//			} catch (Exception e) {
		//				e.printStackTrace();
		//			}
		//		}
		if(shields > 0 && containsInRadius(hit)) {

			boolean shieldDPS = shieldLocalAddOn.getSegmentController().getConfigManager().apply(StatusEffectType.SHIELD_HOTSPOT_DPS, false);
			boolean shieldAlpha = shieldLocalAddOn.getSegmentController().getConfigManager().apply(StatusEffectType.SHIELD_HOTSPOT_ALPHA, false);

			float hotspotPerc = shieldLocalAddOn.getSegmentController().getConfigManager().apply(StatusEffectType.SHIELD_HOTSPOT_PERCENTAGE, 1.0f);
			float hotspotZone = shieldLocalAddOn.getSegmentController().getConfigManager().apply(StatusEffectType.SHIELD_HOTSPOT_RANGE, 1.0f);
			boolean hotspotMode = shieldLocalAddOn.getSegmentController().getConfigManager().apply(StatusEffectType.SHIELD_HOTSPOT_RECHARGE_MODE, false);

			double orig = hit.getDamage();

			if(!ENABLE_LOW_DAMAGE) shieldDPS = false;
			if(!ENABLE_HIGH_DAMAGE) shieldAlpha = false;

			//INSERTED CODE @205
			ShieldHitEvent event = new ShieldHitEvent(this, hit, shieldDPS, shieldAlpha, this.shieldLocalAddOn.getSegmentController());
			StarLoader.fireEvent(ShieldHitEvent.class, event, this.shieldLocalAddOn.getSegmentController().isOnServer());
			shieldDPS = event.isLowDamage();
			shieldAlpha = event.isHighDamage();
			if(event.isCanceled()) {
				hit.hasHit = false;
				return;
			}
			///


			double inverted = 0.0;


			if(shieldAlpha || shieldDPS) {
				if(shieldDPS) {
					inverted = 1.0f;
				} else if(shieldAlpha) {
					inverted = -1.0;
				}
				if(!hit.hasHit) {

					hit.setDamage((hit.getDamage() - inverted * hotspotZone * hit.getDamage()) + inverted * hotspotZone * hit.getDamage() * Math.min(hit.getDamage() / (hotspotPerc * (hotspotMode ? getRechargeRate() : getShieldCapacity())), 2));
				}
			}
			//			System.out.println("SHIELD HOTSPOT Perc: " + hotspotPerc + " Range: " + hotspotZone + " origDamage " + orig + " => modDamage " + hit.damage);


			if(shields <= hit.getDamage()) {
				if(!hit.hasHit) {
					hit.setDamage(hit.getDamage() - shields);
				}
				shields = 0;
				hit.onShieldOutage(this);
			} else {
				shields -= hit.getDamage();
				if(!hit.hasHit) {
					hit.setDamage(0);
				}
				hit.onShieldDamage(this);
			}

			hit.hasHit = true;
		}
	}

	public void onDamage(double damage) {
		preventRecharge = Math.max(preventRecharge, shieldLocalAddOn.getSegmentController().getConfigManager().apply(StatusEffectType.SHIELD_UNDER_FIRE_TIMEOUT, VoidElementManager.SHIELD_LOCAL_RECHARGE_UNDER_FIRE_MODE_SEC));
		double p = shields / getShieldCapacity();

		preventRechargeNerf = 1.0;

		if(p <= 0.0000001) {
			preventRechargeNerf = 0;
		} else if(p < VoidElementManager.SHIELD_LOCAL_RECHARGE_UNDER_FIRE_START_AT_CHARGED) {
			if(p > VoidElementManager.SHIELD_LOCAL_RECHARGE_UNDER_FIRE_END_AT_CHARGED) {

				double pBottom = p - VoidElementManager.SHIELD_LOCAL_RECHARGE_UNDER_FIRE_END_AT_CHARGED;
				double tBottom = VoidElementManager.SHIELD_LOCAL_RECHARGE_UNDER_FIRE_START_AT_CHARGED - VoidElementManager.SHIELD_LOCAL_RECHARGE_UNDER_FIRE_END_AT_CHARGED;

				assert (tBottom != 0.0);
				if(tBottom == 0.0) {
					throw new RuntimeException("Invalid config: shield nerf range zero");
				}
				preventRechargeNerf = pBottom / tBottom;

			} else {
				preventRechargeNerf = VoidElementManager.SHIELD_LOCAL_RECHARGE_UNDER_FIRE_MIN_PERCENT;
			}
		}

		if(shieldLocalAddOn.getSegmentController().isOnServer()) {
			shieldLocalAddOn.getManagerContainer().getPowerInterface().onShieldDamageServer(damage);
			AudioController.fireAudioEvent("0022_spaceship user - ricochet laser hits metal", AudioController.ent(shieldLocalAddOn.getSegmentController(), shieldLocalAddOn.getSegmentController().getSegmentBuffer().getPointUnsave(mainId), mainId, 30));
		}
	}

	public void onOutage(boolean hadShields) {
		//		if(shieldLocalAddOn.getSegmentController().isOnServer()){
		//			System.err.println("ON OUTAGE ON SERVER");
		//		}


		preventRecharge = Math.max(preventRecharge, shieldLocalAddOn.getSegmentController().getConfigManager().apply(StatusEffectType.SHIELD_ZERO_SHIELDS_TIMEOUT, VoidElementManager.SHIELD_LOCAL_ON_ZERO_SHIELDS_RECHARGE_PREVENTION_SEC));
		preventRechargeNerf = 0;
		if(hadShields && shieldLocalAddOn.getSegmentController().isOnServer()) {
			shieldLocalAddOn.sendShieldUpdate(this);
			stopAudio();
		}
	}

	@Override
	public String toString() {
		return "ShieldLocal[<" + mainId + "> (R: " + radius + ") " + StringTools.formatPointZero(shields) + "/" + StringTools.formatPointZero(getShieldCapacity()) + " -> " + ElementCollection.getPosFromIndex(outputPos, new Vector3i()).toStringPure() + "; charge/sec: " + StringTools.formatPointZero(getRechargeRate()) + "]";
	}

	public boolean containsInRadius(ShieldHitCallback hit) {
		return containsInRadius(hit.xLocalBlock, hit.yLocalBlock, hit.zLocalBlock);
	}

	public boolean containsLocalBlockInRadius(long absoluteIndexOnThisEntity) {
		return containsInRadius(ElementCollection.getPosX(absoluteIndexOnThisEntity), ElementCollection.getPosY(absoluteIndexOnThisEntity), ElementCollection.getPosZ(absoluteIndexOnThisEntity));
	}

	@Override
	public double getPowerConsumedPerSecondResting() {
		return getRechargeRate() * VoidElementManager.SHIELD_LOCAL_CONSUMPTION_PER_CURRENT_RECHARGE_PER_SECOND_RESTING;
	}

	@Override
	public double getPowerConsumedPerSecondCharging() {
		return getRechargeRate() * VoidElementManager.SHIELD_LOCAL_CONSUMPTION_PER_CURRENT_RECHARGE_PER_SECOND_CHARGING;
	}

	public double getShieldUpkeep() {
		float upkeep = shieldLocalAddOn.getSegmentController().getConfigManager().apply(StatusEffectType.SHIELD_CAPACITY_UPKEEP, VoidElementManager.SHIELD_LOCAL_UPKEEP_PER_SECOND_OF_TOTAL_CAPACITY);
		return getShieldCapacity() * upkeep;
	}

	@Override
	public boolean isPowerCharging(long curTime) {
		return shields < getShieldCapacity() - 0.000001;
	}

	@Override
	public void setPowered(float powered) {
		this.powered = powered;
	}

	@Override
	public float getPowered() {
		return powered;
	}

	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.SHIELDS;
	}

	@Override
	public void reloadFromReactor(double secTime, Timer timer, float tickTime, boolean powerCharging, float poweredResting) {
		double shieldsBef = shields;
		if(preventRecharge > 0) {
			preventRecharge = Math.max(0f, preventRecharge - (float) secTime);
		} else {
			preventRechargeNerf = 1.0;
		}
		double nerf = getRechargePrevented();
		double recharge = secTime * getRechargeRate();
		double upkeep = secTime * getShieldUpkeep();

		double t = recharge - upkeep;
		if(t >= 0) {
			t *= preventRechargeNerf;
		} else {
			if(recharge > 0 && shieldLocalAddOn.getSegmentController().railController.isRoot() && shieldLocalAddOn.getSegmentController().isClientOwnObject()) {
				if(shieldLocalAddOn.getState().getUpdateTime() - lastMgs > 10000) {
					Vector3i posFromIndex = ElementCollection.getPosFromIndex(mainId, new Vector3i());
					shieldLocalAddOn.getSegmentController().sendClientMessage(Lng.str("Warning! A shield %s is discharging because it's upkeep is bigger than the recharge.\nConsider reducing upkeep by either reducing the shield capacity or by adding more shield recharge modules", posFromIndex), ServerMessage.MESSAGE_TYPE_WARNING);
					lastMgs = shieldLocalAddOn.getState().getUpdateTime();
				}
			}
		}
		shields = Math.max(0, Math.min(getShieldCapacity(), (shields) + t));
		if(shieldLocalAddOn.getSegmentController().isOnServer()) {

			boolean full = shieldsBef < shields && shields == getShieldCapacity();
			boolean margin = false;
			for(int i = 1; i < 10 && !margin; i++) {
				margin = shieldsBef < getShieldCapacity() * (float) i && shields >= getShieldCapacity() * (float) i;
			}
			if(margin || full) {
				if(full || shieldLocalAddOn.getState().getUpdateTime() - lastSentUpdate > 1000) {
					shieldLocalAddOn.sendShieldUpdate(this);
					lastSentUpdate = shieldLocalAddOn.getState().getUpdateTime();
				}
				startAudio();
			}
		}
	}

	@Override
	public boolean isPowerConsumerActive() {
		return active;
	}

	public void resetCapacity() {
		supportIds.clear();
		supportCoMIds.clear();
		this.capacityIntegrity = Double.POSITIVE_INFINITY;
		this.shieldCapacity = VoidElementManager.SHIELD_LOCAL_DEFAULT_CAPACITY;
	}

	public boolean markDrawCollectionByBlock(final ShieldContainerInterface sc, final long absoluteIndex) {
		for(ShieldRegenUnit c : sc.getShieldRegenManager().getElementCollections()) {
			if(mainId == c.idPos && c.getNeighboringCollection().contains(absoluteIndex)) {
				markDraw(sc);
				return true;
			}
		}
		for(ShieldCapacityUnit c : sc.getShieldCapacityManager().getElementCollections()) {
			if(supportIds.contains(c.idPos) && c.getNeighboringCollection().contains(absoluteIndex)) {
				markDraw(sc);
				return true;
			}
		}
		return false;
	}

	private void markDraw(ShieldContainerInterface sc) {
		for(ShieldRegenUnit c : sc.getShieldRegenManager().getElementCollections()) {
			if(mainId == c.idPos) {
				if(active) {
					c.setDrawColor(0, 1, 1, 1);
				} else {
					c.setDrawColor(1, 0, 0, 1);
				}
				c.markDraw();
			}
		}
		for(ShieldCapacityUnit c : sc.getShieldCapacityManager().getElementCollections()) {
			if(supportIds.contains(c.idPos)) {
				c.markDraw();
				c.setDrawColor(0, 1, 0, 1);
			}
		}
	}

	public boolean containsBlock(final ShieldContainerInterface sc, final long absoluteIndex) {
		for(ShieldRegenUnit c : sc.getShieldRegenManager().getElementCollections()) {
			if(mainId == c.idPos && c.getNeighboringCollection().contains(absoluteIndex)) {
				return true;
			}
		}
		for(ShieldCapacityUnit c : sc.getShieldCapacityManager().getElementCollections()) {
			if(supportIds.contains(c.idPos) && c.getNeighboringCollection().contains(absoluteIndex)) {
				return true;
			}
		}
		return false;
	}

	public float getPercentOne() {
		return (float) (shields / getShieldCapacity());
	}

	public double getShieldCapacity() {
		return shieldLocalAddOn.getSegmentController().getConfigManager().apply(StatusEffectType.SHIELD_CAPACITY, shieldCapacity);
	}

	public double getShields() {
		return shields;
	}

	public String getPosString() {
		return ElementCollection.getPosX(mainId) + ", " + ElementCollection.getPosY(mainId) + ", " + ElementCollection.getPosZ(mainId);
	}

	@Override
	public String getName() {
		return "ShieldLocal[" + getPosString() + "]";
	}

	public double getRechargeRate() {
		double reg = rechargePerSecond * ((SendableSegmentController) shieldLocalAddOn.getSegmentController()).getBlockEffectManager().status.shieldRegenPercent;
		reg = shieldLocalAddOn.getSegmentController().getConfigManager().apply(StatusEffectType.SHIELD_RECHARGE_RATE, reg);

		return reg;
	}

	public double getRechargeRateIncludingPrevent() {
		return getRechargeRate() * getRechargePrevented();
	}

	public boolean isPositionInRadiusWorld(Transform entityTrans, Vector3f pos) {
		float ox = ElementCollection.getPosX(outputPos) - Segment.HALF_DIM;
		float oy = ElementCollection.getPosY(outputPos) - Segment.HALF_DIM;
		float oz = ElementCollection.getPosZ(outputPos) - Segment.HALF_DIM;
		Vector3f m = new Vector3f(ox, oy, oz);
		entityTrans.transform(m);

		float dist = Vector3fTools.distance(pos.x, pos.y, pos.z, m.x, m.y, m.z);
		return dist <= radius;
	}

	public void receivedShields(double val) {
		setShieldsAsAction(val);
	}

	public void setShieldsAsAction(double val) {
		final double oldShields = shields;
		boolean damage = false;
		double dmgDone = 0;
		if(val > 0 && val < shields) {
			damage = true;
			dmgDone = shields - val;
		}
		shields = val;
		if(damage) {
			onDamage(dmgDone);
		}
		if(val == 0d) {
			onOutage(oldShields > 0);
		}
		if(shieldLocalAddOn.getSegmentController().isOnServer()) {
			boolean margin = false;
			for(int i = 1; i < 10 && !margin; i++) {
				margin = oldShields > getShieldCapacity() * (float) i && shields <= getShieldCapacity() * (float) i;
			}
			if(margin && shieldLocalAddOn.getState().getUpdateTime() - lastSentUpdate > 1000) {
				shieldLocalAddOn.sendShieldUpdate(this);
				lastSentUpdate = shieldLocalAddOn.getState().getUpdateTime();
			}
		}
	}

	public double getRechargePrevented() {
		return preventRecharge > 0 ? preventRechargeNerf : 1.0;
	}

	@Override
	public void dischargeFully() {
	}

	@Override
	public void startAudio() {
		AudioController.fireAudioEvent("0022_item - shield activate", AudioController.ent(shieldLocalAddOn.getSegmentController(), shieldLocalAddOn.getSegmentController().getSegmentBuffer().getPointUnsave(mainId), mainId, 5));
		AudioController.firedAudioLoopStart("0022_item - shield loop", AudioController.ent(shieldLocalAddOn.getSegmentController(), shieldLocalAddOn.getSegmentController().getSegmentBuffer().getPointUnsave(mainId), mainId, 5));
	}

	@Override
	public void stopAudio() {
		AudioController.fireAudioEvent("0022_item - shield powerdown", AudioController.ent(shieldLocalAddOn.getSegmentController(), shieldLocalAddOn.getSegmentController().getSegmentBuffer().getPointUnsave(mainId), mainId, 5));
		AudioController.firedAudioLoopStop("0022_item - shield loop");
	}
}
