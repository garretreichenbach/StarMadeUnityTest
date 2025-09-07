package org.schema.game.common.data.explosion;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.HitReceiverType;
import org.schema.game.common.controller.damage.HitType;
import org.schema.game.common.controller.damage.effects.InterEffectContainer;
import org.schema.game.common.controller.damage.effects.InterEffectHandler;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.elements.ShieldAddOn;
import org.schema.game.common.controller.elements.ShieldContainerInterface;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.blockeffects.config.ConfigManagerInterface;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.world.SectorNotFoundException;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.network.objects.Sendable;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;

public class ExplosionCollisionSegmentCallback implements ExplosionDamageInterface {

	public static enum ArmorMode{
		ON_ALL_BLOCKS,
		PREEMTIVE,
		NONE
	}
	public ArmorMode armorMode = ArmorMode.PREEMTIVE;
	public final Vector3f centerOfExplosion = new Vector3f();
	public final ExplosionDataHandler explosionDataStructure;
	public final IntOpenHashSet sentDamage = new IntOpenHashSet();
	public final ObjectOpenHashSet<Segment> hitSegments = new ObjectOpenHashSet<Segment>();
	public final Object2ObjectOpenHashMap<SegmentController, Long2ObjectOpenHashMap<ByteArrayList>> hitSendSegments = new Object2ObjectOpenHashMap<SegmentController, Long2ObjectOpenHashMap<ByteArrayList>>();
	public final Int2IntOpenHashMap railMap = new Int2IntOpenHashMap();
	public final Int2IntOpenHashMap railRootMap = new Int2IntOpenHashMap();
	public final Int2ObjectOpenHashMap<ExplosionCubeConvexBlockCallback> shieldHitPosMap = new Int2ObjectOpenHashMap<ExplosionCubeConvexBlockCallback>();
	public final Int2ObjectOpenHashMap<ExplosionCubeConvexBlockCallback> hitPosMap = new Int2ObjectOpenHashMap<ExplosionCubeConvexBlockCallback>();
	public final Int2DoubleOpenHashMap shieldMap = new Int2DoubleOpenHashMap();
	public final Int2LongOpenHashMap shieldLocalMap = new Int2LongOpenHashMap();
	public final Int2DoubleOpenHashMap shieldMapPercent = new Int2DoubleOpenHashMap();
	public final Int2FloatOpenHashMap appliedDamageMap = new Int2FloatOpenHashMap();
	public final Int2DoubleOpenHashMap shieldMapBef = new Int2DoubleOpenHashMap();
	public final Int2ObjectOpenHashMap<InterEffectContainer> interEffectMap = new Int2ObjectOpenHashMap<InterEffectContainer>();
	public final Int2FloatOpenHashMap environmentalProtectMultMap = new Int2FloatOpenHashMap();
	public final Int2FloatOpenHashMap extraDamageTakenMap = new Int2FloatOpenHashMap();
	private final InterEffectSet defenseTmp = new InterEffectSet();
	private final ShortOpenHashSet closedList;
	private final ShortOpenHashSet cpy;
	public float explosionRadius;
	public DamageDealerType damageType;
	public long weaponId;
	public float shieldDamageBonus = VoidElementManager.EXPLOSION_SHIELD_DAMAGE_BONUS;
	public float[] explosionDamageBuffer;
	public ExplosionCubeConvexBlockCallback[] callbackCache = new ExplosionCubeConvexBlockCallback[4096];
	public int cubeCallbackPointer;
	public boolean ignoreShieldsGlobal;
	public ObjectOpenHashSet<Segment> ownLockedSegments = new ObjectOpenHashSet<Segment>(128);
	LongArrayList collidingSegments = new LongArrayList();
	private Long2IntOpenHashMap explosionBlockMapA = new Long2IntOpenHashMap(4096);
	private Long2IntOpenHashMap explosionBlockMapB = new Long2IntOpenHashMap(4096);
	private Long2IntOpenHashMap explosionBlockMapC = new Long2IntOpenHashMap(4096);
	private Long2IntOpenHashMap explosionBlockMapD = new Long2IntOpenHashMap(4096);
	private Vector3f tmp = new Vector3f();
	public final IntOpenHashSet entitiesToIgnoreShieldsOn = new IntOpenHashSet();
	public boolean useLocalShields;
	private boolean ignoreShield;
	
	public HitType hitType;
	public InterEffectSet attack;
	
	
	public ExplosionCollisionSegmentCallback(ExplosionDataHandler s) {

		explosionBlockMapA.defaultReturnValue(-1);
		explosionBlockMapB.defaultReturnValue(-1);
		explosionBlockMapC.defaultReturnValue(-1);
		explosionBlockMapD.defaultReturnValue(-1);
		extraDamageTakenMap.defaultReturnValue(1f);
		environmentalProtectMultMap.defaultReturnValue(1f);
		this.explosionDataStructure = s;

		explosionDamageBuffer = new float[s.bigLength * s.bigLength * s.bigLength];
		closedList = new ShortOpenHashSet(1024);
		cpy = new ShortOpenHashSet(1024);

		for (int i = 0; i < callbackCache.length; i++) {
			callbackCache[i] = new ExplosionCubeConvexBlockCallback();
		}
	}
	public void updateCallbacks() {
		ReentrantReadWriteLock currentLock = null;
		for (int i = 0; i < cubeCallbackPointer; i++) {
			currentLock = callbackCache[i].update(currentLock);
		}
		if (currentLock != null) {
			currentLock.readLock().unlock();
		}
	}

	public void addCharacterHittable(AbstractCharacter<?> s) {

		addControllerSub(s, -0.3f);
		addControllerSub(s, 0.0f);
		addControllerSub(s, 0.3f);

	}

	private void addControllerSub(AbstractCharacter<?> s, float upOffset) {
		ExplosionCubeConvexBlockCallback cb = callbackCache[cubeCallbackPointer];

		cb.type = ExplosionCubeConvexBlockCallback.CHARACTER;

		cb.segEntityId = s.getId();
		cb.blockHpOrig = (short) s.getOwnerState().getHealth();
		cb.blockHp = (short) s.getOwnerState().getHealth();
		cb.boxTransform.set(s.getWorldTransform());

		GlUtil.getUpVector(tmp, cb.boxTransform);
		tmp.scale(upOffset);

		cb.boxTransform.origin.add(tmp);

		tmp.sub(cb.boxTransform.origin, centerOfExplosion);
		cb.boxPosToCenterOfExplosion.set(tmp);
		cubeCallbackPointer++;
	}
	public void sortInsertShieldAndArmorValues(Vector3f hitWorld, int projectileSectorId, List<Sendable> hitting) throws SectorNotFoundException {

		shieldMap.clear();
		shieldLocalMap.clear();
		shieldMapBef.clear();
		shieldHitPosMap.clear();
		hitPosMap.clear();
		interEffectMap.clear();
		environmentalProtectMultMap.clear();
		railMap.clear();
		railRootMap.clear();
		appliedDamageMap.clear();
		extraDamageTakenMap.clear();

		for (Sendable s : hitting) {
			if(s instanceof SimpleTransformableSendableObject<?>){
				interEffectMap.put(s.getId(), ((SimpleTransformableSendableObject<?>)s).getEffectContainer());
			}
			if(s instanceof ConfigManagerInterface) {
				float envMult = ((ConfigManagerInterface)s).getConfigManager().apply(StatusEffectType.ARMOR_DEFENSE_ENVIRONMENTAL, 1f);
				environmentalProtectMultMap.put(s.getId(), envMult);
			}
			if (s instanceof SegmentController) {
				SegmentController c = (SegmentController) s;

				if(!ignoreShield){
					if (c instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) c).getManagerContainer() instanceof ShieldContainerInterface) {
						ShieldAddOn shieldAddOn = ((ShieldContainerInterface) ((ManagedSegmentController<?>) c).getManagerContainer()).getShieldAddOn();
						shieldAddOn.fillShieldsMapRecursive(hitWorld, projectileSectorId, shieldMap, shieldMapBef, shieldMapPercent, railMap, railRootMap, shieldLocalMap);
					}
				}

//				armorHPMap.put(c.getId(), c.getHpController().getArmorHp());
//				armorHPMapBef.put(c.getId(), c.getHpController().getArmorHp());

//				EffectContainer cc = new EffectContainer();
//				cc.status = new FastSegmentControllerStatus(((SendableSegmentController) c).getBlockEffectManager().status);
//				cc.chamberArmorAbsobtion = (c.getConfigManager().apply(StatusEffectType.ARMOR_HP_ABSORPTION, 1f))-1.0f;
//				statusMap.put(c.getId(), cc);
//				
//				c.getEffectContainer()
				
				
				extraDamageTakenMap.put(c.getId(), c.getDamageTakenMultiplier(damageType));
			}
		}
	}

	public void sortAndInsertCallbackCache() {
		explosionBlockMapA.clear();
		explosionBlockMapB.clear();
		explosionBlockMapC.clear();
		explosionBlockMapD.clear();
		for (int i = 0; i < cubeCallbackPointer; i++) {

			ExplosionCubeConvexBlockCallback explosionCubeConvexBlockCallback = callbackCache[i];

			//put in position relative to explosion center
			//in fixed axis aligned grid

			int x = FastMath.round(explosionCubeConvexBlockCallback.boxPosToCenterOfExplosion.x);
			int y = FastMath.round(explosionCubeConvexBlockCallback.boxPosToCenterOfExplosion.y);
			int z = FastMath.round(explosionCubeConvexBlockCallback.boxPosToCenterOfExplosion.z);

			long index = ElementCollection.getIndex(x, y, z);
			if (!explosionBlockMapA.containsKey(index)) {
				explosionBlockMapA.put(index, i);
			} else if (!explosionBlockMapB.containsKey(index)) {
				explosionBlockMapB.put(index, i);
			} else if (!explosionBlockMapC.containsKey(index)) {
				explosionBlockMapC.put(index, i);
			} else if (!explosionBlockMapD.containsKey(index)) {
				explosionBlockMapD.put(index, i);
			} else {
				assert (false);
			}
		}
	}

	public void reset() {
		cubeCallbackPointer = 0;
		ownLockedSegments.clear();
		hitSegments.clear();
		hitSendSegments.clear();
		entitiesToIgnoreShieldsOn.clear();
		armorMode = ArmorMode.PREEMTIVE;
	}

	public void growCache(int index) {
		if (index >= this.callbackCache.length) {
			ExplosionCubeConvexBlockCallback[] ncallbackCache = new ExplosionCubeConvexBlockCallback[this.callbackCache.length * 2];
			for (int i = 0; i < this.callbackCache.length; i++) {
				ncallbackCache[i] = this.callbackCache[i];
			}
			for (int i = this.callbackCache.length; i < ncallbackCache.length; i++) {
				ncallbackCache[i] = new ExplosionCubeConvexBlockCallback();
			}
			this.callbackCache = ncallbackCache;
		}
	}

	private float damageBlock(int x, int y, int z, float damage, ExplosionCubeConvexBlockCallback explosionCubeConvexBlockCallback) {

		if (explosionCubeConvexBlockCallback.blockHp == 0) {
			//this block is air. can happen due to synchronization update
			//return full damage, so it can travel on
			return damage;
		}
		float damageLim = 0;

		appliedDamageMap.put(explosionCubeConvexBlockCallback.segEntityId, 
		appliedDamageMap.get(explosionCubeConvexBlockCallback.segEntityId) + damage);

		if (damageType == DamageDealerType.PULSE) {

			//does x % of damage to a block, but
			//preserve the other part of the damage

			//this creates a piercing effect without
			//losing any damage

			damageLim = damage * 0.5f;
			damage = damageLim;
		}

		if (hitShield(damage, explosionCubeConvexBlockCallback)) {
			return damageLim;
		}
		InterEffectContainer interEffectContainer = interEffectMap.get(explosionCubeConvexBlockCallback.segEntityId);
		ElementInformation info = null;
		
		
		if (armorMode == ArmorMode.ON_ALL_BLOCKS && explosionCubeConvexBlockCallback.blockId != Element.TYPE_NONE) {
			info = ElementKeyMap.getInfoFast(explosionCubeConvexBlockCallback.blockId);
			HitReceiverType rType = info.isArmor() ? HitReceiverType.ARMOR : HitReceiverType.BLOCK;
			InterEffectSet globalDefense = interEffectContainer.get(rType);
			
			InterEffectSet defense = this.defenseTmp;
			
			
			defense.setDefenseFromInfo(info);
			
			
			if(hitType == HitType.ENVIROMENTAL) {
				defense.scaleAdd(globalDefense, environmentalProtectMultMap.get(explosionCubeConvexBlockCallback.segEntityId));
			}else {
				defense.add(globalDefense); //add 'chamber' effect
			}
			
			damage = InterEffectHandler.handleEffects(damage, this.attack, defense, this.hitType, this.damageType, rType, explosionCubeConvexBlockCallback.blockId);
			
		}
		
		

		int hitpointsEffective = (int) (explosionCubeConvexBlockCallback.blockHp );
		int hitpointsEffectiveAfter = (int) Math.max(0, (hitpointsEffective - FastMath.round(damage* extraDamageTakenMap.get(explosionCubeConvexBlockCallback.segEntityId))));
		int hitpointsAfter = (int) Math.max(0, hitpointsEffectiveAfter);

		ExplosionCubeConvexBlockCallback sh = hitPosMap.get(explosionCubeConvexBlockCallback.segEntityId);

		if (sh == null) {
			sh = explosionCubeConvexBlockCallback;

			//put in as a reference to send client a display of shields getting taken out
			//this is the closest, the shields are hit to the object
			hitPosMap.put(explosionCubeConvexBlockCallback.segEntityId, sh);
		}

		if (damage < hitpointsEffective) {
			//damage just damages the block
			int actualDamage = (int) Math.max(0, damage);
			explosionCubeConvexBlockCallback.blockHp = hitpointsAfter;

			//rays dies here
			return damageLim;
		} else {
			//damage enough to destroy the block

			damage = Math.max(0, damage - hitpointsEffective);
			explosionCubeConvexBlockCallback.blockHp = 0;
			return damage + damageLim;
		}
	}

	@Deprecated
	private int findShieldShare(float damage, int orig, int startId) {
		double shields = shieldMap.get(startId);
		if (shields > 0 && orig == startId) {
			return orig;
		}

		if (orig != startId) {
			double pc = shieldMapPercent.get(startId);

			if (pc > VoidElementManager.SHIELD_DOCK_TRANSFER_LIMIT && damage < shields) {
				//damage can be taken by a parent

				return startId;
			}
		}
		if (railMap.containsKey(startId)) {
			return findShieldShare(damage, orig, railMap.get(startId));
		} else {
			//no shields are able to take this damage
			return -1;
		}

	}

	private boolean hitShield(float damage,
	                          ExplosionCubeConvexBlockCallback c) {

		
		if(ignoreShieldsGlobal || entitiesToIgnoreShieldsOn.contains(c.segEntityId)){
			return false;
		}
		int findShieldShare;
		if(useLocalShields){
			
			int ent = railRootMap.get(c.segEntityId);
			double shields;
			if(ent == 0){
				ent = c.segEntityId;
			}
			shields = shieldMap.get(ent);
			if(shields < damage){
				findShieldShare = -1;
			}else{
				findShieldShare = ent;
			}
			
		}else{
			findShieldShare = findShieldShare(damage, c.segEntityId, c.segEntityId);
		}

		if (findShieldShare > -1) {
			double shields = shieldMap.get(findShieldShare);

			if (shields > 0) {

				float dmg = damage;
				
				
				
				//handle shield
				HitReceiverType rType = HitReceiverType.SHIELD;
				ElementInformation info = ElementKeyMap.getInfoFast(c.blockId);
				
				InterEffectContainer eCon = interEffectMap.get(c.segEntityId);
				InterEffectSet defense = this.defenseTmp;
				
				defense.setEffect(VoidElementManager.shieldEffectConfiguration);
				defense.add(eCon.get(rType));
				if(VoidElementManager.individualBlockEffectArmorOnShieldHit) {
					defense.add(info.effectArmor);
				}
				
				
				dmg = InterEffectHandler.handleEffects(dmg, attack, defense, hitType, damageType, rType, c.blockId);
				
				dmg += dmg * shieldDamageBonus;
				shields = Math.max(0, shields - dmg);

				//put in -1 to signal the afterExplosion function to take out all shields
				//as it can be that while running this thread, there has been a recharge
				//causing the shields to never actually reach 0
				shieldMap.put(findShieldShare, shields == 0 ? -1 : shields);

				ExplosionCubeConvexBlockCallback sh = shieldHitPosMap.get(findShieldShare);

				if (sh == null) {
					sh = c;

					//put in as a reference to send client a display of shields getting taken out
					//this is the closest, the shields are hit to the object
					shieldHitPosMap.put(findShieldShare, sh);
				}

				return true;
			}
		}

		return false;
	}
	@Override
	public float modifyDamageBasedOnBlockArmor(int x, int y, int z, float damage) {
		int aIndex = explosionBlockMapA.get(ElementCollection.getIndex(x, y, z));
		if (aIndex >= 0 && ElementKeyMap.isValidType(callbackCache[aIndex].blockId)) {
			
			ExplosionCubeConvexBlockCallback c = callbackCache[aIndex];
			
			InterEffectContainer eCon = interEffectMap.get(c.segEntityId);
			
			
			ElementInformation info = ElementKeyMap.getInfoFast(c.blockId);
			HitReceiverType rType = info.isArmor() ? HitReceiverType.ARMOR : HitReceiverType.BLOCK;
			
			
			InterEffectSet defense = this.defenseTmp;
			
			defense.setDefenseFromInfo(info);
			
			if(hitType == HitType.ENVIROMENTAL) {
				defense.scaleAdd(eCon.get(rType), environmentalProtectMultMap.get(c.segEntityId));
			}else {
				defense.add(eCon.get(rType)); //add 'chamber' effect
			}
			
			damage = InterEffectHandler.handleEffects(damage, attack, defense, hitType, damageType, rType, c.blockId);
		}
		return damage;
	}
	@Override
	public float damageBlock(int x, int y, int z, float damage) {

		//returns -1 as default if not set
		int aIndex = explosionBlockMapA.get(ElementCollection.getIndex(x, y, z));
		if (aIndex >= 0) {
			damage = damageBlock(x, y, z, damage, callbackCache[aIndex]);
			int bIndex = explosionBlockMapB.get(ElementCollection.getIndex(x, y, z));
			if (bIndex >= 0) {
				damage = damageBlock(x, y, z, damage, callbackCache[bIndex]);
				int cIndex = explosionBlockMapC.get(ElementCollection.getIndex(x, y, z));
				if (cIndex >= 0) {
					damage = damageBlock(x, y, z, damage, callbackCache[cIndex]);
					int dIndex = explosionBlockMapD.get(ElementCollection.getIndex(x, y, z));
					if (dIndex >= 0) {
						damage = damageBlock(x, y, z, damage, callbackCache[dIndex]);
					}
				}
			}
		}

		return damage;
	}

	@Override
	public void setDamage(int x, int y, int z, float damage) {

		int xx = x + explosionDataStructure.bigLengthHalf;
		int yy = y + explosionDataStructure.bigLengthHalf;
		int zz = z + explosionDataStructure.bigLengthHalf;

		int i = zz * (explosionDataStructure.bigLengthQuad) +
				yy * explosionDataStructure.bigLength +
				xx;

		explosionDamageBuffer[i] = damage;
	}

	@Override
	public float getDamage(int x, int y, int z) {

		int xx = x + explosionDataStructure.bigLengthHalf;
		int yy = y + explosionDataStructure.bigLengthHalf;
		int zz = z + explosionDataStructure.bigLengthHalf;

		int i = zz * (explosionDataStructure.bigLengthQuad) +
				yy * explosionDataStructure.bigLength +
				xx;

		assert (i < explosionDamageBuffer.length) : xx + ", " + yy + ", " + zz;

		return explosionDamageBuffer[i];
	}

	@Override
	public void resetDamage() {
		Arrays.fill(explosionDamageBuffer, 0);
		closedList.clear();
		cpy.clear();
	}

	@Override
	public ShortOpenHashSet getClosedList() {
		return closedList;
	}

	@Override
	public ShortOpenHashSet getCpy() {
		return cpy;
	}

	@Override
	public Vector3f getExplosionCenter() {
		return centerOfExplosion;
	}
	



}
