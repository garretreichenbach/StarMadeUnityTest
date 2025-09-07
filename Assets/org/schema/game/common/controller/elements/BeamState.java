package org.schema.game.common.controller.elements;

import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.ArmorValue;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.HitType;
import org.schema.game.common.controller.elements.armorhp.ArmorHPCollection;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.beam.AbstractBeamHandler;
import org.schema.game.common.data.element.beam.BeamReloadCallback;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.player.PersonalBeamHandler;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.input.KeyboardMappings;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

public class BeamState {

	public final Vector3f hitPointCache = new Vector3f(); 
	public final SegmentPiece p1 = new SegmentPiece();
	public final SegmentPiece p2 = new SegmentPiece();
	
	public final Vector3f relativePos = new Vector3f();
	public final long identifyerSig;
	public final Vector3f from = new Vector3f();
	public final Vector3f to = new Vector3f();
	public final Vector4f color = new Vector4f();
	private final AbstractBeamHandler<?> handler;
	public Vector3f hitPoint;
	public float timeRunningSinceLastUpdate = 0;
	public long lastCheck = -1;
	public float timeSpent;
	public SegmentPiece segmentHit;
	public SegmentPiece currentHit;
	public float hitOneSegment;
	public float hitBlockTime;
	public SegmentController cachedLastSegment;
	public Vector3b cachedLastPos = new Vector3b(-1, -1, -1);
	public float camDistStart;
	public float camDistEnd;
	public float size = 1;


	//two states of firing
	public final ObjectOpenHashSet<KeyboardMappings> beamButton = new ObjectOpenHashSet<KeyboardMappings>(2);

	public long weaponId;
	public float timeOutInSecs;
	public float burstTime;
	public float initialTicks;
	public float timeRunning;
	public BeamReloadCallback reloadCallback;
	public float powerConsumptionPerTick;
	public float powerConsumptionExtraPerTick;
	public float totalConsumedPower;
	public int beamType;
	public boolean markDeath;
	public MetaObject originMetaObject;
	public Vector3i controllerPos;
	public int ticksToDo;
	public long fireStart;
	public boolean dontFade;
	public int ticksDone;
	private float tickRate;
	private float power;
	public double railParent;
	public double railChild;
	public boolean handheld;
	public Vector3f drawVarsCamPos = new Vector3f();
	public Vector3f drawVarsStart = new Vector3f();
	public Vector3f drawVarsEnd = new Vector3f();
	public float drawVarsAxisAngle;
	public float drawVarsDist;
	public float drawVarsLenDiff;
	public Transform drawVarsDrawTransform = new Transform();
	public Transform lastHitTrans = new Transform();
	public Transform lastSegConTrans = new Transform();
	public Vector3i lastHitPos = new Vector3i();
	public boolean oldPower;
	public boolean latchOn;
	public long firstLatch;
	public HitType hitType;
	public Transform initalRelativeTranform = new Transform();
	public boolean friendlyFire;
	public boolean penetrating;
	public float acidDamagePercent;
	public Vector3f hitNormalWorld = new Vector3f();
	public Vector3f hitNormalRelative = new Vector3f();
	public boolean checkLatchConnection = true;
	public Vector3f dirTmp = new Vector3f();
	public Vector3f fromInset = new Vector3f();
	public Vector3f toInset = new Vector3f();
	public float beamLength;
	public float minEffectiveRange;
	public float minEffectiveValue;
	public float maxEffectiveRange;
	public float maxEffectiveValue;
	public float capacityPerTick;
	public int hitSectorId = -1;
	public BeamState(long elementPos, Vector3f relativePos, Vector3f from,
	                 Vector3f to, PlayerState playerState, float speed, float power, 
	                 long weaponId, int beamType, MetaObject originMetaObject,
	                 Vector3i controllerPos, boolean handheld, boolean latchOn, boolean checkLatchConnection, float capacityPerTick, HitType hitType, AbstractBeamHandler<?> handler) {
		super();
		this.identifyerSig = elementPos;
		this.from.set(from);
		this.to.set(to);
		this.relativePos.set(relativePos);
		this.tickRate = speed;
		this.power = power;
		this.handler = handler;
		this.weaponId = weaponId;
		this.beamType = beamType;
		this.originMetaObject = originMetaObject;
		this.color.set(handler.getColor(this));
		this.controllerPos = controllerPos;
		this.handheld = handheld;
		this.oldPower = handler.isUsingOldPower();
		this.latchOn = latchOn;
		this.checkLatchConnection = checkLatchConnection;
		this.capacityPerTick = capacityPerTick;
		this.firstLatch = Long.MIN_VALUE;
		this.hitType = hitType;
	}


	/**
	 * @return the power
	 */
	public float getTickRate() {
		return tickRate;
	}

	/**
	 * @param speed the power to set
	 */
	public void setTickRate(float speed) {
		this.tickRate = speed;
	}

	@Override
	public int hashCode() {
		return (int) identifyerSig * handler.getBeamShooter().hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return ((BeamState) o).handler.getBeamShooter() == handler.getBeamShooter() && ((BeamState) o).identifyerSig == identifyerSig;
	}

	/**
	 * @return the power
	 */
	public float getPower() {
		return power;
	}

	/**
	 * @param power the power to set
	 */
	public void setPower(float power) {
		this.power = power;
	}

	/**
	 * @return the handler
	 */
	public AbstractBeamHandler<?> getHandler() {
		return handler;
	}

	public boolean isAlive() {
		if (markDeath) {
			return false;
		}
		if (!dontFade && timeRunningSinceLastUpdate > timeOutInSecs) {
			return false;
		}

		if (burstTime <= 0 && timeRunningSinceLastUpdate < timeOutInSecs) {
			//constant beam
			return true;
		}

		//wither normal time ran out, and burstTime exist implicates time has to be smaller than that burst time
		return initialTicks > 0 || ticksToDo > 0 || timeRunning < timeOutInSecs;
	}
	public boolean isOnServer() {
		return handler.isOnServer();
	}

	public void reset() {
		ignoreArmor = false;
		ignoreShield = false;
		currentHit = null;
		hitPoint = null;
		segmentHit = null;
		hitSectorId = -1;
		hitOneSegment = 0;
		hitBlockTime = 0;
		timeSpent = 0;	
		firstLatch = Long.MIN_VALUE;
		initalRelativeTranform.setIdentity();
		armorValue.reset();
	}

	private final Vector3f dd = new Vector3f();
	public boolean ignoreShield;
	public ArmorValue armorValue = new ArmorValue();
	public boolean ignoreArmor;

	public float getPowerByBeamLength() {
		return power * getBeamLengthModifier();
	}
	public float getBeamLengthModifier() {
		if(beamType == PersonalBeamHandler.TORCH) {
			return 1;
		}
		if(!penetrating && beamLength > 0 && maxEffectiveValue != minEffectiveValue) {
			float p = Vector3fTools.diffLength(to, from) / beamLength;
			
//			if(isOnServer()) {
//				System.err.println("LEN: beam "+Vector3fTools.diffLength(to, from)+"; max "+beamLength+": p "+p+"; RANGE: "+minEffectiveRange+" / "+maxEffectiveRange);
//			}
			float ret;
			if(p >= maxEffectiveRange) {
				ret = maxEffectiveValue;
			}else if(p <= minEffectiveRange) {
				ret = minEffectiveValue;
			}else {
				float percP = (p - minEffectiveRange) / Math.abs(maxEffectiveRange - minEffectiveRange);
				
//				System.err.println("PERP: "+percP);
				
				
				if(maxEffectiveValue < minEffectiveValue) {
					float diffVal = minEffectiveValue - maxEffectiveValue;
					ret =  minEffectiveValue - (diffVal * percP);
				}else {
					float diffVal = maxEffectiveValue - minEffectiveValue;
					ret =  minEffectiveValue + (diffVal * percP);
				}
			}
//			System.err.println("::: "+ret);
			return ret;
		}else {
			return 1f;
		}
	}


//	public int calcPreviousArmorDamageReduction(float beamPower) {
//		beamPower = Math.max(0, beamPower -armorValue.totalArmorValue * DamageBeamElementManager.DAMAGE_REDUCTION_PER_ARMOR_VALUE_MULT);
//		return (int)beamPower;
//	}
	public int calcPreviousArmorDamageReduction(final float dmgOriginal, SegmentController target) {
		float dmgOut = dmgOriginal;
		if(VoidElementManager.ARMOR_CALC_STYLE == ArmorDamageCalcStyle.EXPONENTIAL) {
			//  Damage Dealt = (Damage Incoming^3)/((Armour Value In Line of Shot)^3+Damage Incoming^2)

			dmgOut = Math.max(0,
					FastMath.pow(dmgOriginal, VoidElementManager.MISSILE_ARMOR_EXPONENTIAL_INCOMING_EXPONENT) /
							(FastMath.pow(armorValue.totalArmorValue, VoidElementManager.MISSILE_ARMOR_EXPONENTIAL_ARMOR_VALUE_TOTAL_EXPONENT) +
									FastMath.pow(dmgOriginal, VoidElementManager.MISSILE_ARMOR_EXPONENTIAL_INCOMING_DAMAGE_ADDED_EXPONENT)));
		}else {
			dmgOut = Math.max(0, dmgOut - (VoidElementManager.MISSILE_ARMOR_FLAT_DAMAGE_REDUCTION *  dmgOut));
			dmgOut = Math.max(0, dmgOut - Math.min(VoidElementManager.MISSILE_ARMOR_THICKNESS_DAMAGE_REDUCTION_MAX, ((VoidElementManager.BEAM_ARMOR_THICKNESS_DAMAGE_REDUCTION * (float)armorValue.typesHit.size())) *  dmgOut));
		}

		if(target instanceof ManagedSegmentController<?> msc) {
			final ArmorHPCollection c = msc.getManagerContainer().getArmorHP().getCollectionManager();
			dmgOut = c.processDamageToArmor(dmgOriginal, dmgOut);
		}
		return (int)dmgOut;
	}


}
