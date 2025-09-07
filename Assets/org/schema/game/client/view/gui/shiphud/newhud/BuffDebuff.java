package org.schema.game.client.view.gui.shiphud.newhud;

import java.lang.reflect.Field;
import java.util.Map.Entry;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.effectblock.EffectElementManager.OffensiveEffects;
import org.schema.game.common.data.blockeffects.BlockEffectTypes;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.AbstractSceneNode;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

public class BuffDebuff extends HudConfig {

	@ConfigurationElement(name = "BlendOutInSec")
	public static float blendOutTime;
	@ConfigurationElement(name = "StayTime")
	public static float stayTime;

	@ConfigurationElement(name = "EMPIndex")
	public static HitIconIndex EMP_INDEX;

	@ConfigurationElement(name = "ExplosiveIndex")
	public static HitIconIndex EXPLOSIVE_INDEX;

	@ConfigurationElement(name = "IonIndex")
	public static HitIconIndex ION_INDEX;

	@ConfigurationElement(name = "OverdriveIndex")
	public static HitIconIndex OVERDRIVE_INDEX;

	@ConfigurationElement(name = "PiercingIndex")
	public static HitIconIndex PIERCING_INDEX;

	@ConfigurationElement(name = "PunchthroughIndex")
	public static HitIconIndex PUNCHTHROUGH_INDEX;

	@ConfigurationElement(name = "RepairIndex")
	public static HitIconIndex REPAIR_INDEX;

	@ConfigurationElement(name = "ShieldSupplyIndex")
	public static HitIconIndex SHIELD_SUPPLY_INDEX;

	@ConfigurationElement(name = "ShieldDrainIndex")
	public static HitIconIndex SHIELD_DRAIN_INDEX;

	@ConfigurationElement(name = "PowerSupplyIndex")
	public static HitIconIndex POWER_SUPPLY_INDEX;

	@ConfigurationElement(name = "PowerDrainIndex")
	public static HitIconIndex POWER_DRAIN_INDEX;

	@ConfigurationElement(name = "NoThrust")
	public static HitIconIndex NO_THRUST;

	@ConfigurationElement(name = "NoPower")
	public static HitIconIndex NO_POWER;

	@ConfigurationElement(name = "ShieldsDown")
	public static HitIconIndex SHIELD_DOWN;

	@ConfigurationElement(name = "ThrusterOutage")
	public static HitIconIndex THRUSTER_OUTAGE;

	@ConfigurationElement(name = "NoPowerRecharge")
	public static HitIconIndex NO_POWER_RECHARGE;

	@ConfigurationElement(name = "NoShieldRecharge")
	public static HitIconIndex NO_SHIELD_RECHARGE;

	@ConfigurationElement(name = "ThrusterOutageEff")
	public static EffectIconIndex THRUSTER_OUTAGE_EFF;

	@ConfigurationElement(name = "NoPowerRechargeEff")
	public static EffectIconIndex NO_POWER_RECHARGE_EFF;

	@ConfigurationElement(name = "NoShieldRechargeEff")
	public static EffectIconIndex NO_SHIELD_RECHARGE_EFF;

	@ConfigurationElement(name = "Controlless")
	public static EffectIconIndex CONTROLLESS;

	@ConfigurationElement(name = "Push")
	public static EffectIconIndex PUSH;

	@ConfigurationElement(name = "Pull")
	public static EffectIconIndex PULL;

	@ConfigurationElement(name = "Stop")
	public static EffectIconIndex STOP;

	@ConfigurationElement(name = "StatusArmorHarden")
	public static EffectIconIndex STATUS_ARMOR_HARDEN;

	@ConfigurationElement(name = "StatusPiercingProtection")
	public static EffectIconIndex STATUS_PIERCING_PROTECTION;

	@ConfigurationElement(name = "StatusArmorHpAbsorptionBonus")
	public static EffectIconIndex STATUS_ARMOR_HP_ABSORPTION_BONUS;

	@ConfigurationElement(name = "StatusArmorHpRegenBonus")
	public static EffectIconIndex STATUS_ARMOR_HP_REGEN_BONUS;

	@ConfigurationElement(name = "StatusArmorHpHardeningBonus")
	public static EffectIconIndex STATUS_ARMOR_HP_HARDENING_BONUS;

	@ConfigurationElement(name = "StatusPowerShield")
	public static EffectIconIndex STATUS_POWER_SHIELD;

	@ConfigurationElement(name = "StatusShieldHarden")
	public static EffectIconIndex STATUS_SHIELD_HARDEN;

	@ConfigurationElement(name = "StatusTopSpeed")
	public static EffectIconIndex STATUS_TOP_SPEED;

	@ConfigurationElement(name = "StatusAntiGravity")
	public static EffectIconIndex STATUS_ANTI_GRAVITY;

	@ConfigurationElement(name = "StatusGravityEffectIgnorance")
	public static EffectIconIndex STATUS_GRAVITY_EFFECT_IGNORANCE;

	@ConfigurationElement(name = "TakeOff")
	public static EffectIconIndex TAKE_OFF;

	@ConfigurationElement(name = "Evade")
	public static EffectIconIndex EVADE;

	@ConfigurationElement(name = "Position")
	public static GUIPosition POSITION;

	@ConfigurationElement(name = "Offset")
	public static Vector2f OFFSET;
	public static Object2ObjectOpenHashMap<BlockEffectTypes, EffectIconIndex> map = new Object2ObjectOpenHashMap<BlockEffectTypes, EffectIconIndex>();
	public static Object2ObjectOpenHashMap<OffensiveEffects, HitIconIndex> mapOffensive = new Object2ObjectOpenHashMap<OffensiveEffects, HitIconIndex>();
	public Int2FloatOpenHashMap activeIcons = new Int2FloatOpenHashMap();
	private GUIOverlay icons;

	public BuffDebuff(InputState state) {
		super(state);
	}

	@Override
	public Vector4i getConfigColor() {
		return null;
	}

	@Override
	public GUIPosition getConfigPosition() {
		return POSITION;
	}

	@Override
	public Vector2f getConfigOffset() {
		return OFFSET;
	}

	@Override
	protected String getTag() {
		return "BuffDebuff";
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#draw()
	 */
	@Override
	public void draw() {
		GlUtil.glPushMatrix();
		transform();

		int buffIndex = 0;
		int debuffIndex = -1;
		Ship ship = ((GameClientState) getState()).getShip();
		if (ship != null) {

			ObjectIterator<Entry<Integer, Float>> iterator = activeIcons.entrySet().iterator();

			while (iterator.hasNext()) {
				Entry<Integer, Float> next = iterator.next();
				icons.setSpriteSubIndex(Math.abs(next.getKey()));
				if (next.getKey() >= 0) {
					icons.getSprite().getTint().set(ColorPalette.buff);
					icons.getPos().x = buffIndex * 32;
					buffIndex++;
				} else {
					icons.getSprite().getTint().set(ColorPalette.debuff);
					icons.getPos().x = debuffIndex * 32 - 10;
					debuffIndex--;
				}

				if (next.getValue() > stayTime) {
					icons.getSprite().getTint().w = 1.0f - (next.getValue() - stayTime) / blendOutTime;
				}

				icons.draw();

			}

		}
		for (AbstractSceneNode e : getChilds()) {
			e.draw();
		}
		GlUtil.glPopMatrix();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#onInit()
	 */
	@Override
	public void onInit() {

		icons = new GUIOverlay(Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath()+"HUD_Sprites-8x8-gui-"), getState());

		map.clear();
		mapOffensive.clear();

		mapOffensive.put(OffensiveEffects.EMP, EMP_INDEX);
		mapOffensive.put(OffensiveEffects.EXPLOSIVE, EXPLOSIVE_INDEX);
		mapOffensive.put(OffensiveEffects.ION, ION_INDEX);
		mapOffensive.put(OffensiveEffects.OVERDRIVE, OVERDRIVE_INDEX);
		mapOffensive.put(OffensiveEffects.PIERCING, PIERCING_INDEX);
		mapOffensive.put(OffensiveEffects.PUNCHTHROUGH, PUNCHTHROUGH_INDEX);

		mapOffensive.put(OffensiveEffects.THRUSTER_OUTAGE, THRUSTER_OUTAGE);
		mapOffensive.put(OffensiveEffects.NO_POWER_RECHARGE, NO_POWER_RECHARGE);
		mapOffensive.put(OffensiveEffects.NO_SHIELD_RECHARGE, NO_SHIELD_RECHARGE);

		Field[] declaredFields = this.getClass().getFields();
		for (Field f : declaredFields) {

			if (f.getType().equals(HitIconIndex.class)) {
				HitIconIndex object;
				try {
					object = (HitIconIndex) f.get(this);
//					System.err.println("FOUND: "+f.getName());
					assert (object.type != null);
					mapOffensive.put(object.type, object);
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}

			if (f.getType().equals(EffectIconIndex.class)) {
				EffectIconIndex object;
				try {
					object = (EffectIconIndex) f.get(this);
//					System.err.println("FOUND: "+f.getName());
					assert (object.type != null);
					map.put(object.type, object);
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			} else {
//				System.err.println("NOT FOUND: "+f.getName());
			}
		}

		for (BlockEffectTypes b : BlockEffectTypes.values()) {
			assert (b == BlockEffectTypes.NULL_EFFECT || map.containsKey(b)) : b.name() + "; " + map;
		}
		icons.getSprite().setTint(new Vector4f(1, 1, 1, 1));
		super.onInit();
	}

	@Override
	public void update(Timer timer) {
		Ship ship = ((GameClientState) getState()).getShip();
		if (ship != null) {

			for (BlockEffectTypes e : ship.getBlockEffectManager().getActiveEffectTypes()) {

//				System.err.println("ACTIVE:::: "+e);

				EffectIconIndex effectIconIndex = map.get(e);
				assert (effectIconIndex != null) : e.name();

				activeIcons.put(effectIconIndex.getIndex() * (effectIconIndex.isBuff() ? 1 : -1), 0f);

			}

			for (int i : activeIcons.keySet()) {

				activeIcons.put(i, activeIcons.get(i) + timer.getDelta());

			}

			ObjectIterator<it.unimi.dsi.fastutil.ints.Int2FloatMap.Entry> iterator = activeIcons.int2FloatEntrySet().fastIterator();

			while (iterator.hasNext()) {
				it.unimi.dsi.fastutil.ints.Int2FloatMap.Entry next = iterator.next();

				if (next.getFloatValue() >= blendOutTime + stayTime) {
					iterator.remove();
				}
			}
		}
	}

	public void notifyEffectHit(SimpleTransformableSendableObject obj,
	                            OffensiveEffects offensiveEffects) {
		HitIconIndex hitIconIndex = mapOffensive.get(offensiveEffects);
		if (hitIconIndex != null) {
			assert (hitIconIndex != null) : offensiveEffects.name() + "::: " + mapOffensive;
			activeIcons.put(hitIconIndex.getIndex() * (hitIconIndex.isBuff() ? 1 : -1), 0f);
		} else {
			EffectIconIndex eee = map.get(offensiveEffects.getEffect());
			activeIcons.put(eee.getIndex() * (eee.isBuff() ? 1 : -1), 0f);
		}

	}

}
