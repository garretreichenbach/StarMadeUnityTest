package org.schema.game.common.controller.damage.projectile;

import api.listener.events.block.SegmentPieceDamageEvent;
import api.listener.fastevents.CannonProjectileHitListener;
import api.listener.fastevents.FastListenerCommon;
import api.listener.fastevents.segmentpiece.SegmentPieceDamageListener;
import api.mod.StarLoader;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.effects.ExplosionDrawer;
import org.schema.game.common.controller.EditableSendableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.TransientSegmentController;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.HitReceiverType;
import org.schema.game.common.controller.damage.HitType;
import org.schema.game.common.controller.damage.acid.AcidDamageFormula;
import org.schema.game.common.controller.damage.acid.AcidSetting;
import org.schema.game.common.controller.damage.effects.InterEffectHandler;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;
import org.schema.game.common.controller.elements.ArmorDamageCalcStyle;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ShieldContainerInterface;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.controller.elements.armorhp.ArmorHPCollection;
import org.schema.game.common.controller.elements.cargo.CargoCollectionManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.physics.CubeRayCastResult;
import org.schema.game.common.data.physics.InnerSegmentIterator;
import org.schema.game.common.data.physics.RayTraceGridTraverser;
import org.schema.game.common.data.physics.RigidBodyExt;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.world.SectorNotFoundException;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;

import javax.vecmath.Vector3f;
import java.util.List;

public class ProjectileHandlerSegmentController extends ProjectileHandler {
	public class ShotHandler {
		public SegmentController hitSegController;
		public long weaponId;
		public Damager damager;
		public Vector3f posBeforeUpdate = new Vector3f();
		public Vector3f posAfterUpdate = new Vector3f();
		public Vector3f shootingDir = new Vector3f();
		public Vector3f shootingDirRelative = new Vector3f();
		public HitType hitType = HitType.WEAPON;
		public DamageDealerType damageDealerType = DamageDealerType.PROJECTILE;
		private float dmg;
		private float totalArmorValue;
		//LOGGING ONLY
		private float totalDmg;

		private final LongArrayList positionsHit = new LongArrayList();
		private final ObjectArrayList<ElementInformation> typesHit = new ObjectArrayList<ElementInformation>();
		private final ObjectArrayList<Segment> segmentsHit = new ObjectArrayList<Segment>();
		private final ObjectOpenHashSet<Segment> segmentsHitSet = new ObjectOpenHashSet<Segment>();
		private final IntArrayList infoIndexHit = new IntArrayList();
		public InterEffectSet defenseArmor = new InterEffectSet();
		public InterEffectSet defenseShield = new InterEffectSet();
		public InterEffectSet defenseBlock = new InterEffectSet();
		public MetaWeaponEffectInterface meta;
		public ShotStatus shotStatus = ShotStatus.OVER_PENETRATION;
		public final AcidSetting acidSetting = new AcidSetting();
		public AcidDamageFormula acidFormula;
		public int blockIndex;
		public int penetrationDepth;
		public float projectileWidth;
		public float initialDamage;
		public InterEffectSet defenseEffectSetTmp = new InterEffectSet();
		public ProjectileController.ProjectileHandleState forcedResult;
		public boolean wasFirst;
		public float damageToAcidPercent;
		public int projectileId;

		//		public boolean ignoreArmor;
		public void reset() {
			//			ignoreArmor = false;
			meta = null;
			weaponId = Long.MIN_VALUE;
			damager = null;
			hitSegController = null;
			dmg = 0;
			totalDmg = 0.0f;
			totalArmorValue = 0.0f;
			initialDamage = 0;
			acidFormula = null;
			forcedResult = null;
			shotStatus = ShotStatus.OVER_PENETRATION;
			forcedResult = null;
			resetHitBuffer();
		}

		public void resetHitBuffer() {
			positionsHit.clear();
			typesHit.clear();
			segmentsHit.clear();
			segmentsHitSet.clear();
			infoIndexHit.clear();
		}

		public ProjectileController.ProjectileHandleState getResult() {
			if(forcedResult != null) {
				return forcedResult;
			}

			dmg = totalDmg;
			if(dmg > 0) {
				return ProjectileController.ProjectileHandleState.PROJECTILE_HIT_CONTINUE;
			} else {
				return ProjectileController.ProjectileHandleState.PROJECTILE_HIT_STOP;
			}
		}
	}

	private final SegmentPiece pTmp = new SegmentPiece();
	private final Vector3f hTmp = new Vector3f();
	private final Vector3f dirTmp = new Vector3f();
	private final ShotHandler shotHandler = new ShotHandler();
	private final CubeRayCastResult rayCallbackTraverse = new CubeRayCastResult(new Vector3f(), new Vector3f(), null) {

		@Override
		public InnerSegmentIterator newInnerSegmentIterator() {
			return new ProjectileTraverseHandler();
		}

	};

	private class ProjectileTraverseHandler extends InnerSegmentIterator {

		@Override
		public boolean onOuterSegmentHitTest(Segment sOuter, boolean hadHit) {
			if(debug) {
				System.err.println("OUTER HIT::: " + sOuter);
			}
			boolean continueShot = true;
			if(!hadHit && shotHandler.typesHit.size() > 0) {

				continueShot = processHitsUnshielded(shotHandler);
				//				System.err.println(shotHandler.hitSegController.getState()+" PROCESS OUTER SEGMENT ACCUMULATED SHOT: count "+shotHandler.typesHit.size()+"; "+shotHandler.hitSegController+"; continue: "+continueShot);
				//we hit "air". process rest of accumulated
			}
			return continueShot;
		}

		@Override
		public boolean handle(int absX, int absY, int absZ, RayTraceGridTraverser traverser) {

			//			if(rayResult.isDebug()) {
			//				System.err.println("HANDLE "+absX+", "+absY+", "+absZ);
			//			}
			SegmentController controller = getContextObj();


			int x = (absX - currentSeg.pos.x) + SegmentData.SEG_HALF;
			int y = (absY - currentSeg.pos.y) + SegmentData.SEG_HALF;
			int z = (absZ - currentSeg.pos.z) + SegmentData.SEG_HALF;
			if(debug) {
				traverser.drawDebug(absX + SegmentData.SEG_HALF, absY + SegmentData.SEG_HALF, absZ + SegmentData.SEG_HALF, tests, controller.getWorldTransform());
			}
			tests++;

			SegmentData data0 = currentSeg.getSegmentData();
			short type;
			int infoIndex;
			if(x >= 0 && x < SegmentData.SEG && y >= 0 && y < SegmentData.SEG && z >= 0 && z < SegmentData.SEG) {

				v.elemA.set((byte) x, (byte) y, (byte) z);
				v.elemPosA.set(v.elemA.x - SegmentData.SEG_HALF, v.elemA.y - SegmentData.SEG_HALF, v.elemA.z - SegmentData.SEG_HALF);

				v.elemPosA.x += currentSeg.pos.x;
				v.elemPosA.y += currentSeg.pos.y;
				v.elemPosA.z += currentSeg.pos.z;

				v.nA.set(v.elemPosA);
				v.tmpTrans3.set(testCubes);
				v.tmpTrans3.basis.transform(v.nA);
				v.tmpTrans3.origin.add(v.nA);

				//do not set hitsignal, or the cast will stop at the end of the segment
				//				this.hitSignal = true;
				float fraction = Vector3fTools.length(fromA.origin, v.tmpTrans3.origin) / Vector3fTools.length(fromA.origin, toA.origin);
				rayResult.closestHitFraction = fraction;
				rayResult.setSegment(data0.getSegment());
				rayResult.getCubePos().set(v.elemA);
				rayResult.hitPointWorld.set(v.tmpTrans3.origin);
				rayResult.hitNormalWorld.sub(fromA.origin, toA.origin);
				FastMath.normalizeCarmack(rayResult.hitNormalWorld);

				if((type = data0.getType((infoIndex = SegmentData.getInfoIndex((byte) x, (byte) y, (byte) z)))) > 0 && ElementInformation.isPhysicalRayTests(type, data0, infoIndex) && isZeroHpPhysical(data0, infoIndex)) {

					if(rayResult.isDebug()) {
						System.err.println("HIT BLOCK: " + x + ", " + y + "; " + z + "; BLOCK: " + (absX - SegmentData.SEG_HALF) + ", " + (absY - SegmentData.SEG_HALF) + ", " + (absZ - SegmentData.SEG_HALF));
					}
					rayResult.collisionObject = collisionObject;
					//the test of the parameters may be set here but not necessary

					boolean continueShot = processRawHitUnshielded(currentSeg, infoIndex, type, v.elemA, v.elemPosA, testCubes);

					if(rayResult.isDebug()) {
						System.err.println("HIT BLOCK: " + x + ", " + y + "; " + z + "; BLOCK: " + (absX - SegmentData.SEG_HALF) + ", " + (absY - SegmentData.SEG_HALF) + ", " + (absZ - SegmentData.SEG_HALF) + " -CONTINUE: " + continueShot);
					}
					if(!continueShot) {
						//dont continue with next segment in outer handler only if we didn't hit any armor
						hitSignal = true;
					}
					return continueShot;
				} else {

					if(shotHandler.typesHit.size() > 0) {
						//						if(isOnServer()) {
						//							System.err.println(shotHandler.hitSegController.getState()+" PROCESS ACCUMULATED SHOT: "+shotHandler.typesHit.size()+"; "+x+", "+y+", "+z+" ;; "+shotHandler.hitSegController+"; onType: "+ElementKeyMap.toString(type));
						//						}

						//hitsignal set here for outer handler, so cast stops with this segment
						hitSignal = true;
						boolean continueShot = processHitsUnshielded(shotHandler);
						//we hit "air". process rest of accumulated

						if(rayResult.isDebug()) {
							System.err.println("*AIR* HIT BLOCK ACCUMULATED (air block): " + x + ", " + y + "; " + z + "; BLOCK: " + (absX - SegmentData.SEG_HALF) + ", " + (absY - SegmentData.SEG_HALF) + ", " + (absZ - SegmentData.SEG_HALF) + "; type: " + type + "; continue: " + continueShot);
						}
						return continueShot;
					}
				}
			}
			return true;
		}
	}

	private boolean processRawHitUnshielded(Segment currentSeg, int infoIndex, short type, Vector3b segmentPos, Vector3f absolutePos, Transform segmentControllerWorldTransform) {

		ElementInformation info = ElementKeyMap.getInfoFast(type);

		shotHandler.positionsHit.add(currentSeg.getAbsoluteIndex(infoIndex));
		shotHandler.typesHit.add(info);
		shotHandler.segmentsHit.add(currentSeg);
		shotHandler.segmentsHitSet.add(currentSeg);
		shotHandler.infoIndexHit.add(infoIndex);
		boolean continueShot = true;
		if(isAccumulateShot(info)) {
			//shot has been added to process later
		} else {
			continueShot = processHitsUnshielded(shotHandler);
		}
		//preliminary test has already confirmed hit
		return continueShot;
	}

	public enum ShotStatus {
		OVER_PENETRATION, STOPPED, STOPPED_ACID, NORMAL,
	}

	private boolean processHitsUnshielded(ShotHandler shotHandler) {
		int accumulated = shotHandler.positionsHit.size();
		boolean hitArmor = false;
		if(accumulated > 1) {
			HitReceiverType hitReceiverType = HitReceiverType.ARMOR;
			InterEffectSet defense = shotHandler.hitSegController.getEffectContainer().get(hitReceiverType);
			assert (shotHandler.damager != null);
			assert (shotHandler.damageDealerType != null);
			InterEffectSet attack = shotHandler.damager.getAttackEffectSet(shotHandler.weaponId, shotHandler.damageDealerType);

			short typeParam = -1; //no type for a general check on damage
			float damageOnBlock;
			if(attack == null) {
				System.err.println(shotHandler.hitSegController.getState() + " WARNING: hit effect set on " + shotHandler.hitSegController + " by " + shotHandler.damager + " is null for weapon " + shotHandler.weaponId);
				damageOnBlock = shotHandler.dmg;
			} else {
				damageOnBlock = InterEffectHandler.handleEffects(shotHandler.dmg, attack, defense, shotHandler.hitType, shotHandler.damageDealerType, hitReceiverType, typeParam);
			}

			short firstArmorId = 0;
			int armorBlockCount = 0; //total armor blocks in a row
			float armorValueTotal = 0; //total armor value
			float armorIntegrity = 0; //percentage of damaged armor blocks
			for(int i = 0; i < accumulated; i++) {
				ElementInformation info = shotHandler.typesHit.get(i);
				Segment currentSeg = shotHandler.segmentsHit.get(i);
				int infoIndex = shotHandler.infoIndexHit.get(i);
				short type = info.id;

				if(info.isArmor()) {
					if(armorBlockCount == 0) firstArmorId = type;
					armorBlockCount++;
					armorValueTotal += info.getArmorValue() + (info.getArmorValue() * (armorBlockCount * VoidElementManager.ARMOR_THICKNESS_BONUS));
					armorIntegrity += currentSeg.getSegmentData().getHitpointsByte(infoIndex) * ElementKeyMap.MAX_HITPOINTS_INV; // div by 127
					//					if(isOnServer()) {
					//						System.out.println("[ARMOR] ARMOR VALUE TOTAL: " + armorValueTotal + " and currentArmorBlockCount: " + armorBlockCount);
					//					}
				} else {
					//					if(isOnServer()) {
					//						System.out.println("[ARMOR] ARMOR VALUE TOTAL: " + armorValueTotal + " and currentArmorBlockCount: " + armorBlockCount+"; NO ARMOR BLOCK: "+info);
					//					}
					break;
				}
			}

			if(armorBlockCount > 0) {

				//at least armor block hit. (cancels reboot)
				shotHandler.dmg = shotHandler.hitSegController.getHpController().onHullDamage(shotHandler.damager, damageOnBlock, firstArmorId, shotHandler.damageDealerType);

				hitArmor = true;
				armorIntegrity = armorIntegrity / armorBlockCount;

				//check if we can penetrate the armor
				//calculate damage vs armor

				shotHandler.totalArmorValue = armorValueTotal * armorIntegrity;
				//				if(isOnServer()) {
				//					System.out.println("[ARMOR] Damage on block " + damageOnBlock);
				//				}
				shotHandler.damageToAcidPercent = 0;
				if(damageOnBlock < shotHandler.totalArmorValue * VoidElementManager.ACID_DAMAGE_ARMOR_STOPPED_MARGIN) {
					//check failed. only hit first armor block
					shotHandler.shotStatus = ShotStatus.STOPPED;
				} else if(damageOnBlock < shotHandler.totalArmorValue) {
					//check failed. only hit first armor block
					shotHandler.shotStatus = ShotStatus.STOPPED_ACID;

					shotHandler.damageToAcidPercent = (damageOnBlock - shotHandler.totalArmorValue * VoidElementManager.ACID_DAMAGE_ARMOR_STOPPED_MARGIN) / (1.0f - VoidElementManager.ACID_DAMAGE_ARMOR_STOPPED_MARGIN);
					doDamageReduction(shotHandler, armorBlockCount);

				} else if(shotHandler.shotStatus == ShotStatus.OVER_PENETRATION) {
					//only switch from OP to normal, but never back
					if(damageOnBlock > shotHandler.totalArmorValue * VoidElementManager.ARMOR_OVER_PENETRATION_MARGIN_MULTIPLICATOR) {
						shotHandler.shotStatus = ShotStatus.OVER_PENETRATION;
					} else {
						shotHandler.shotStatus = ShotStatus.NORMAL;
					}
				}

				if(shotHandler.shotStatus == ShotStatus.NORMAL || shotHandler.shotStatus == ShotStatus.OVER_PENETRATION) {
					doDamageReduction(shotHandler, armorBlockCount);

				}
			} else {
				if(shotHandler.blockIndex == 0 && shotHandler.shotStatus == ShotStatus.OVER_PENETRATION) {
					//only switch on first block or when armor is encountered
					//only switch from OP to normal, but never back
					if(shotHandler.dmg > VoidElementManager.NON_ARMOR_OVER_PENETRATION_MARGIN) {
						shotHandler.shotStatus = ShotStatus.OVER_PENETRATION;
					} else {
						shotHandler.shotStatus = ShotStatus.NORMAL;
					}
				}
			}
		}

		for(int i = 0; i < accumulated && shotHandler.dmg > 0; i++) {
			ElementInformation info = shotHandler.typesHit.get(i);
			Segment currentSeg = shotHandler.segmentsHit.get(i);
			int infoIndex = shotHandler.infoIndexHit.get(i);
			short type = info.id;

			if(rayCallbackTraverse.isDebug()) System.err.println("HANDLING DAMAGE ON BLOCK: " + info + "; " + new SegmentPiece(currentSeg, infoIndex));
			shotHandler.dmg = doDamageOnBlock(type, info, currentSeg, infoIndex);
			shotHandler.blockIndex++;

			//			if(shotHandler.blockIndex > shotHandler.penetrationDepth) {
			//				shotHandler.dmg = 0;
			//				break;
			//			}
			if(hitArmor) {
				if(shotHandler.shotStatus == ShotStatus.STOPPED) {
					//					if(isOnServer()) {
					//						System.err.println("[ARMOR] ARMOR STOPPED SHOT");
					//					}
					shotHandler.dmg = 0;
					break;
				} else {
					//					if(isOnServer()) {
					//						System.err.println("[ARMOR] BROKEN "+shotHandler.shotStatus.name());
					//					}
				}
			}
		}
		shotHandler.resetHitBuffer();
		return shotHandler.dmg > 0;
	}

	private void doDamageReduction(ShotHandler shotHandler, int armorValue) {
		if(shotHandler.dmg > 0){
			final float dmgBefore = shotHandler.dmg;

			if(VoidElementManager.ARMOR_CALC_STYLE == ArmorDamageCalcStyle.EXPONENTIAL) {
				//  Damage Dealt = (Damage Incoming^3)/((Armour Value In Line of Shot)^3+Damage Incoming^2)

				shotHandler.dmg = Math.max(0, FastMath.pow(shotHandler.dmg, VoidElementManager.CANNON_ARMOR_EXPONENTIAL_INCOMING_EXPONENT) / (FastMath.pow(shotHandler.totalArmorValue, VoidElementManager.CANNON_ARMOR_EXPONENTIAL_ARMOR_VALUE_TOTAL_EXPONENT) + FastMath.pow(shotHandler.dmg, VoidElementManager.CANNON_ARMOR_EXPONENTIAL_INCOMING_DAMAGE_ADDED_EXPONENT)));
			} else {
				shotHandler.dmg = Math.max(0, shotHandler.dmg - (VoidElementManager.CANNON_ARMOR_FLAT_DAMAGE_REDUCTION * shotHandler.dmg));
				shotHandler.dmg = Math.max(0, shotHandler.dmg - Math.min(VoidElementManager.CANNON_ARMOR_THICKNESS_DAMAGE_REDUCTION_MAX, ((VoidElementManager.CANNON_ARMOR_THICKNESS_DAMAGE_REDUCTION * armorValue)) * shotHandler.dmg));
			}

			if(shotHandler.hitSegController instanceof ManagedSegmentController<?> msc) {
				final ArmorHPCollection c = msc.getManagerContainer().getArmorHP().getCollectionManager();
				shotHandler.dmg = c.processDamageToArmor(dmgBefore, shotHandler.dmg);
			}
		}
	}

	private float doDamageOnBlock(short type, ElementInformation info, Segment currentSeg, int infoIndex) {
		int orientationOrig = currentSeg.getSegmentData().getOrientation(infoIndex);
		int orientation = orientationOrig;

		//		SegmentPiece pCheck = new SegmentPiece(currentSeg, infoIndex);
		//		Vector3i hitPosCheck = pCheck.getAbsolutePos(new Vector3i());
		//		if(isOnServer() && hitPosCheck.z > 75) {
		//			assert(false):("WARNING::::::::::: "+shotHandler.projectileId+" PHASE THROUGH: "+pCheck+"; "+pCheck.getWorldPos(new Vector3f(), pCheck.getSegmentController().getSectorId()));
		//		}

		float damageOnBlock;

		if(shotHandler.blockIndex == 0) {

			//reduce damage according to armor hit on first hit
			InterEffectSet defense = shotHandler.defenseEffectSetTmp;
			InterEffectSet globalDefense = info.isArmor() ? shotHandler.defenseArmor : shotHandler.defenseBlock;
			assert (shotHandler.damager != null);
			assert (shotHandler.damageDealerType != null);

			defense.setEffect(globalDefense); //already contains 'chamber' effects and (VoidElementManaged) global effects
			defense.add(info.effectArmor);

			InterEffectSet attack = shotHandler.damager.getAttackEffectSet(shotHandler.weaponId, shotHandler.damageDealerType);
			HitReceiverType hitReceiverType = info.isArmor() ? HitReceiverType.ARMOR : HitReceiverType.BLOCK;

			if(attack != null) {
				damageOnBlock = InterEffectHandler.handleEffects(shotHandler.dmg, attack, defense, shotHandler.hitType, shotHandler.damageDealerType, hitReceiverType, type);
			} else {
				System.err.println(shotHandler.hitSegController.getState() + " WARNING: block hit effect set on " + shotHandler.hitSegController + " by " + shotHandler.damager + " is null for weapon " + shotHandler.weaponId);
				damageOnBlock = shotHandler.dmg;
			}
		} else {
			damageOnBlock = shotHandler.dmg;
		}

		float restDamage = damageOnBlock;

		int damage = FastMath.round(damageOnBlock);

		if(damage == 0) {
			return 0;
		}
		if(currentSeg.getSegmentController() instanceof ManagedSegmentController<?> && (info.isInventory() || type == ElementKeyMap.CARGO_SPACE)) {
			if(type == ElementKeyMap.CARGO_SPACE) {
				//cargo may be hit
				orientation = Element.TOP;
			}

			long absIndex = currentSeg.getAbsoluteIndex(infoIndex);
			ManagerContainer<?> managerContainer = ((ManagedSegmentController<?>) currentSeg.getSegmentController()).getManagerContainer();
			Inventory inv = null;
			if(info.isInventory()) {
				inv = managerContainer.getInventory(absIndex);
			} else {
				List<CargoCollectionManager> cm = managerContainer.getCargo().getCollectionManagers();
				for(CargoCollectionManager m : cm) {
					if(m.rawCollection.contains(absIndex)) {
						inv = managerContainer.getInventory(m.getControllerIndex());
						break;
					}
				}
			}
			if(inv != null) {
				if(!inv.isEmpty()) {

					double vol = inv.getVolume();
					double volumeLost = 0;
					while(volumeLost < damage && !inv.isEmpty()) {
						if(isOnServer()) {
							inv.spawnVolumeInSpace(currentSeg.getSegmentController(), damage);
						}

						restDamage = Math.max(0, restDamage - damage);
						volumeLost += damage;

					}

					//continue fully
					return damage;
				}
			} else {
				System.err.println("[SERVER][PROJECTILE] Warning: no connected inventory found when hitting " + currentSeg.getAbsoluteElemPos(infoIndex, new Vector3i()) + " -> " + info);
			}

		}

		short hitpointsBefByte = currentSeg.getSegmentData().getHitpointsByte(infoIndex);

		EditableSendableSegmentController eSeg = (EditableSendableSegmentController) shotHandler.hitSegController;

		//INSERTED CODE
		if (StarLoader.hasListeners(SegmentPieceDamageEvent.class))
		{
			SegmentPieceDamageEvent ev = new SegmentPieceDamageEvent(currentSeg.getSegmentController(), currentSeg.getAbsoluteIndex(infoIndex), type, damage, shotHandler.damageDealerType, shotHandler.damager);
			StarLoader.fireEvent(ev, isOnServer());
			if (ev.isCanceled()) return 0;
			damage = ev.getDamage();
		}
		for (SegmentPieceDamageListener listener : FastListenerCommon.segmentPieceDamageListeners) {
			damage = listener.onBlockDamage(currentSeg.getSegmentController(), currentSeg.getAbsoluteIndex(infoIndex), type, damage, shotHandler.damageDealerType, shotHandler.damager, isOnServer());
		}
		if (damage <= 0)
			return 0;
		///

		//damage = currentSeg.getSegmentController().handleArmorHPDamage(damage);

		if(eSeg.isExtraAcidDamageOnDecoBlocks() && info.isDecorative()) {
			if(isOnServer()) {
				//kill decorative blocks outright, and do bonus acid
				SegmentPiece p = new SegmentPiece(currentSeg, infoIndex);
				eSeg.killBlock(p);

				int extraDamageFromDecoHit = 40;
				shotHandler.acidFormula.getAcidDamageSetting(type, damage, extraDamageFromDecoHit, extraDamageFromDecoHit, shotHandler.totalArmorValue, shotHandler.blockIndex, shotHandler.projectileWidth, shotHandler.penetrationDepth, shotHandler.shotStatus, shotHandler.acidSetting);

				boolean propagateOnInitallyKilled = true; //we want acid damage to originate from blocks that are already dead
				shotHandler.totalDmg += shotHandler.acidSetting.damage;
				boolean decoOnly = true;
				eSeg.getAcidDamageManagerServer().inputDamage(currentSeg.getAbsoluteIndex(infoIndex), shotHandler.shootingDirRelative, shotHandler.acidSetting.damage, shotHandler.acidSetting.maxPropagation, shotHandler.damager, shotHandler.weaponId, propagateOnInitallyKilled, decoOnly);
			} else {
				ExplosionDrawer explosionDrawer = ((GameClientState) shotHandler.hitSegController.getState()).getWorldDrawer().getExplosionDrawer();
				pTmp.setByReference(currentSeg, infoIndex);
				pTmp.getAbsolutePos(hTmp);
				hTmp.x -= 8;
				hTmp.y -= 8;
				hTmp.z -= 8;
				explosionDrawer.addExplosion(hTmp);
			}

			return restDamage;
		} else {

			float actualDamage = eSeg.damageElement(type, infoIndex, currentSeg.getSegmentData(), damage, shotHandler.damager, DamageDealerType.PROJECTILE, shotHandler.weaponId);
			shotHandler.totalDmg += actualDamage;
			//check if empty to avoid nullpointer if we just killed the last block of that segment
			short hitPointsRemainByte = currentSeg.isEmpty() ? 0 : currentSeg.getSegmentData().getHitpointsByte(infoIndex);

			restDamage = Math.max(0.0f, restDamage - actualDamage);
			if(hitPointsRemainByte > 0) {
				//block was not killed
				//projectile stopping on block
				if(isOnServer()) {
					eSeg.sendBlockHpByte(currentSeg.getAbsoluteIndex(infoIndex), hitPointsRemainByte);
					//called delayed for client
					eSeg.onBlockDamage(currentSeg.getAbsoluteIndex(infoIndex), type, damage, shotHandler.damageDealerType, shotHandler.damager);
				}
				restDamage = 0;
			} else {
				//block was killed
				if(isOnServer()) {
					eSeg.sendBlockKill(currentSeg.getAbsoluteIndex(infoIndex));
					//called delayed for client
					SegmentPiece p = new SegmentPiece(currentSeg, infoIndex);
					p.setType(type);
					eSeg.onBlockKill(p, shotHandler.damager);
				} else {
					ExplosionDrawer explosionDrawer = ((GameClientState) shotHandler.hitSegController.getState()).getWorldDrawer().getExplosionDrawer();
					pTmp.setByReference(currentSeg, infoIndex);
					pTmp.getAbsolutePos(hTmp);
					hTmp.x -= 8;
					hTmp.y -= 8;
					hTmp.z -= 8;
					explosionDrawer.addExplosion(hTmp);
				}

				//if there is rest damage, apply par of it as acid damage
				if(restDamage > 0 && shotHandler.shotStatus != ShotStatus.STOPPED) {
					if(shotHandler.shotStatus == ShotStatus.STOPPED_ACID) {
						restDamage *= shotHandler.damageToAcidPercent;
					}
					shotHandler.acidFormula.getAcidDamageSetting(type, damage, (int) restDamage, (int) shotHandler.initialDamage, shotHandler.totalArmorValue, shotHandler.blockIndex, shotHandler.projectileWidth, shotHandler.penetrationDepth, shotHandler.shotStatus, shotHandler.acidSetting);

					if(isOnServer()) {

						boolean propagateOnInitallyKilled = true; //we want acid damage to originate from blocks that are already dead
						shotHandler.totalDmg += shotHandler.acidSetting.damage;
						boolean decoOnly = false;
						eSeg.getAcidDamageManagerServer().inputDamage(currentSeg.getAbsoluteIndex(infoIndex), shotHandler.shootingDirRelative, shotHandler.acidSetting.damage, shotHandler.acidSetting.maxPropagation, shotHandler.damager, shotHandler.weaponId, propagateOnInitallyKilled, decoOnly);
					}
					restDamage = Math.max(restDamage - shotHandler.acidSetting.damage, 0);

				}
			}

			return restDamage;
		}
	}

	private boolean isOnServer() {
		return shotHandler.hitSegController.isOnServer();
	}

	private boolean isAccumulateShot(ElementInformation info) {
		return info.isArmor();
	}

	@Override
	public ProjectileController.ProjectileHandleState handle(Damager damager, ProjectileController projectileController, Vector3f posBeforeUpdate, Vector3f posAfterUpdate, ProjectileParticleContainer particles, int particleIndex, CubeRayCastResult rayCallbackInitial) {
		Segment seg = rayCallbackInitial.getSegment();
		if(seg == null) {
			System.err.println(damager + " ERROR: SEGMENT NULL: " + rayCallbackInitial);
			return ProjectileController.ProjectileHandleState.PROJECTILE_NO_HIT;
		}

		//hit on segmentcontroller
		SegmentController c = seg.getSegmentController();
		if(c instanceof ProjectileHittable) {

			if(!c.canBeDamagedBy(damager, DamageDealerType.PROJECTILE)) {
				return ProjectileController.ProjectileHandleState.PROJECTILE_NO_HIT_STOP;
			}

			shotHandler.hitSegController = c;
			shotHandler.posBeforeUpdate.set(posBeforeUpdate);
			shotHandler.posAfterUpdate.set(posBeforeUpdate);
			shotHandler.shootingDir.sub(posAfterUpdate, posBeforeUpdate);
			FastMath.normalizeCarmack(shotHandler.shootingDir);
			shotHandler.shootingDirRelative.set(shotHandler.shootingDir);
			c.getWorldTransformInverse().basis.transform(shotHandler.shootingDirRelative);

			//scale shot to penetrate
			dirTmp.sub(posAfterUpdate, posBeforeUpdate);
			FastMath.normalizeCarmack(dirTmp);
			dirTmp.scale(400);
			posAfterUpdate.add(posBeforeUpdate, dirTmp);

			rayCallbackTraverse.closestHitFraction = 1.0f;
			rayCallbackTraverse.collisionObject = null;
			rayCallbackTraverse.setSegment(null);

			rayCallbackTraverse.rayFromWorld.set(posBeforeUpdate);
			rayCallbackTraverse.rayToWorld.set(posAfterUpdate);

			rayCallbackTraverse.setFilter(c); //filter for performance since inital check already succeeded
			rayCallbackTraverse.setOwner(damager);
			rayCallbackTraverse.setIgnoereNotPhysical(false);
			rayCallbackTraverse.setIgnoreDebris(false);
			rayCallbackTraverse.setRecordAllBlocks(false);
			rayCallbackTraverse.setZeroHpPhysical(false); //dont hit 0 hp blocks
			rayCallbackTraverse.setDamageTest(true);
			rayCallbackTraverse.setCheckStabilizerPaths(true); //hit stablizer paths
			rayCallbackTraverse.setSimpleRayTest(true);

			//			rayCallbackTraverse.setDebug(isOnServer());

			projectileController.getCurrentPhysics().getDynamicsWorld().rayTest(posBeforeUpdate, posAfterUpdate, rayCallbackTraverse);

			//INSERTED CODE
			for(CannonProjectileHitListener listener : FastListenerCommon.cannonProjectileHitListeners) {
				listener.handle(damager, projectileController, posBeforeUpdate, posAfterUpdate, particles, particleIndex, rayCallbackInitial, this);
			}
			//

			if(shotHandler.typesHit.size() > 0) {
				//process rest
				processHitsUnshielded(shotHandler);
			}

			assert (shotHandler.typesHit.size() == 0) : "not all hits consumed " + shotHandler.typesHit.size();
			if(shotHandler.getResult() == ProjectileController.ProjectileHandleState.PROJECTILE_HIT_STOP || shotHandler.getResult() == ProjectileController.ProjectileHandleState.PROJECTILE_HIT_CONTINUE || shotHandler.getResult() == ProjectileController.ProjectileHandleState.PROJECTILE_HIT_STOP_INVULNERABLE) {
				shotHandler.hitSegController.sendHitConfirmToDamager(damager, false);
			}

			if(rayCallbackTraverse.hasHit()) {
				if(rayCallbackTraverse.isDebug()) {
					System.err.println("UPDATE POSAFTERHIT::: -> " + rayCallbackTraverse.hitPointWorld);
				}
				posAfterUpdate.set(rayCallbackTraverse.hitPointWorld);
			}

			return shotHandler.getResult();
		}

		return ProjectileController.ProjectileHandleState.PROJECTILE_NO_HIT;

	}

	@Override
	public ProjectileController.ProjectileHandleState handleBefore(Damager damager, ProjectileController projectileController, Vector3f posBeforeUpdate, Vector3f posAfterUpdate, ProjectileParticleContainer particles, int particleIndex, CubeRayCastResult rayCallbackInitial) {

		if(rayCallbackInitial.getSegment() == null) {
			System.err.println("[PROJECTILE][WARNING] Segment null but collided " + rayCallbackInitial.collisionObject);
			return ProjectileController.ProjectileHandleState.PROJECTILE_NO_HIT_STOP;
		}

		//check for shield or other pre shot things
		SegmentController c = rayCallbackInitial.getSegment().getSegmentController();

		shotHandler.reset();
		shotHandler.dmg = particles.getDamage(particleIndex);
		shotHandler.penetrationDepth = particles.getPenetrationDepth(particleIndex);
		shotHandler.initialDamage = particles.getDamageInitial(particleIndex);
		shotHandler.damager = damager;
		shotHandler.hitSegController = c;
		shotHandler.weaponId = particles.getWeaponId(particleIndex);
		shotHandler.blockIndex = particles.getBlockHitIndex(particleIndex);
		shotHandler.shotStatus = ShotStatus.values()[particles.getShotStatus(particleIndex)];
		shotHandler.projectileWidth = particles.getWidth(particleIndex);
		shotHandler.projectileId = particles.getId(particleIndex);

		shotHandler.wasFirst = shotHandler.blockIndex == 0;

		shotHandler.defenseArmor.setEffect(shotHandler.hitSegController.getEffectContainer().get(HitReceiverType.ARMOR));
		shotHandler.defenseArmor.add(VoidElementManager.armorEffectConfiguration);

		shotHandler.defenseBlock.setEffect(shotHandler.hitSegController.getEffectContainer().get(HitReceiverType.BLOCK));
		shotHandler.defenseBlock.add(VoidElementManager.basicEffectConfiguration);

		shotHandler.defenseShield.setEffect(shotHandler.hitSegController.getEffectContainer().get(HitReceiverType.SHIELD));
		shotHandler.defenseShield.add(VoidElementManager.shieldEffectConfiguration);

		shotHandler.acidFormula = AcidDamageFormula.AcidFormulaType.values()[particles.getAcidFormulaIndex(particleIndex)].formula;

		shotHandler.meta = shotHandler.damager.getMetaWeaponEffect(shotHandler.weaponId, shotHandler.damageDealerType);

		//INSERTED CODE
		for(CannonProjectileHitListener listener : FastListenerCommon.cannonProjectileHitListeners) {
			listener.handleBefore(damager, projectileController, posBeforeUpdate, posAfterUpdate, particles, particleIndex, rayCallbackInitial, this);
		}
		//

		//		if(isOnServer()) {
		//			System.err.println("HITSEC "+c);
		//		}

		if(shotHandler.meta != null && particles.getBlockHitIndex(particleIndex) == 0) {
			shotHandler.meta.onHit(c);
		}

		if(!isOnServer()) {
			ExplosionDrawer explosionDrawer = ((GameClientState) shotHandler.hitSegController.getState()).getWorldDrawer().getExplosionDrawer();
			explosionDrawer.addExplosion(rayCallbackInitial.hitPointWorld, 10, 10, shotHandler.dmg);
		}

		if(!shotHandler.hitSegController.checkAttack(damager, true, true) && !(!isOnServer() && ((GameClientState) shotHandler.hitSegController.getState()).getPlayer().isInTutorial())) {
			if(!isOnServer()) {
				//do an explosion still for feedback (so the shooter knows that the shot was stopped. usefull when a turret is for example shooting its parents)
				ExplosionDrawer explosionDrawer = ((GameClientState) shotHandler.hitSegController.getState()).getWorldDrawer().getExplosionDrawer();
				pTmp.setByReference(rayCallbackInitial.getSegment(), rayCallbackInitial.getCubePos());
				pTmp.getAbsolutePos(hTmp);
				hTmp.x -= 8;
				hTmp.y -= 8;
				hTmp.z -= 8;
				explosionDrawer.addExplosion(hTmp);
			}
			//don't continue
			return ProjectileController.ProjectileHandleState.PROJECTILE_HIT_STOP_INVULNERABLE;
		}
		if(c instanceof TransientSegmentController) {
			((TransientSegmentController) c).setTouched(true, true);
		}

		if(c instanceof ManagedSegmentController<?>) {
			ManagerContainer<SegmentController> man = ((ManagedSegmentController<SegmentController>) c).getManagerContainer();
			if(man instanceof ShieldContainerInterface sh) {

				boolean localShieldOn = (c.isUsingLocalShields() && (sh.getShieldAddOn().isUsingLocalShieldsAtLeastOneActive() || c.railController.isDockedAndExecuted()));
				boolean oldShieldsOn = (sh.getShieldAddOn().getShields() > 0 || c.railController.isDockedAndExecuted());
				if(localShieldOn || oldShieldsOn) {

					float bef = shotHandler.dmg;
					InterEffectSet shieldDefense = shotHandler.defenseEffectSetTmp;

					shieldDefense.setEffect(shotHandler.defenseShield); //contains 'chamber' effect and (VoidElementManager) effect

					if(VoidElementManager.individualBlockEffectArmorOnShieldHit) {
						pTmp.setByReference(rayCallbackInitial.getSegment(), rayCallbackInitial.getCubePos());
						if(pTmp.isValid()) {
							shieldDefense.add(pTmp.getInfo().effectArmor);
						}
					}

					try {
						shotHandler.dmg = (float) sh.getShieldAddOn().handleShieldHit(damager, shieldDefense, rayCallbackInitial.hitPointWorld, projectileController.getSectorId(), DamageDealerType.PROJECTILE, HitType.WEAPON, shotHandler.dmg, shotHandler.weaponId);
					} catch(SectorNotFoundException e) {
						e.printStackTrace();
						shotHandler.dmg = 0;
					}
					if(shotHandler.dmg <= 0) {
						c.sendHitConfirmToDamager(damager, true);
						return ProjectileController.ProjectileHandleState.PROJECTILE_HIT_STOP;
					} else {
						return ProjectileController.ProjectileHandleState.PROJECTILE_HIT_CONTINUE;
					}
				}

			}
		}

		return ProjectileController.ProjectileHandleState.PROJECTILE_NO_HIT;
	}

	private final Vector3f tmpDir = new Vector3f();
	private final Vector3f tmpWorldPos = new Vector3f();

	@Override
	public ProjectileController.ProjectileHandleState handleAfterIfNotStopped(Damager damager, ProjectileController projectileController, Vector3f posBeforeUpdate, Vector3f posAfterUpdate, ProjectileParticleContainer particles, int particleIndex, CubeRayCastResult rayCallbackInitial) {

		particles.setBlockHitIndex(particleIndex, shotHandler.blockIndex); //increment to mark shot has had a hit
		particles.setShotStatus(particleIndex, shotHandler.shotStatus.ordinal());

		//INSERTED CODE
		for(CannonProjectileHitListener listener : FastListenerCommon.cannonProjectileHitListeners) {
			listener.handleAfterIfNotStopped(damager, projectileController, posBeforeUpdate, posAfterUpdate, particles, particleIndex, rayCallbackInitial, this);
		}
		//

		return ProjectileController.ProjectileHandleState.PROJECTILE_IGNORE;
	}

	@Override
	public void afterHandleAlways(Damager damager, ProjectileController projectileController, Vector3f posBeforeUpdate, Vector3f posAfterUpdate, ProjectileParticleContainer particles, int particleIndex, CubeRayCastResult rayCallbackInitial) {

		//		System.err.println("BODY::: "+shotHandler.wasFirst+": "+(c.railController.getRoot().getPhysicsDataContainer().getObject() instanceof RigidBodyExt));
		boolean wasFirstHit = shotHandler.wasFirst && shotHandler.blockIndex > 0;
		if(wasFirstHit && shotHandler.hitSegController != null && shotHandler.hitSegController.railController.getRoot().getPhysicsDataContainer().getObject() instanceof RigidBodyExt) {
			tmpDir.sub(posAfterUpdate, posBeforeUpdate);

			//INSERTED CODE
			for(CannonProjectileHitListener listener : FastListenerCommon.cannonProjectileHitListeners) {
				listener.handleAfterAlways(damager, projectileController, posBeforeUpdate, posAfterUpdate, particles, particleIndex, rayCallbackInitial, this);
			}
			//

			if(tmpDir.lengthSquared() > 0) {
				tmpWorldPos.set(rayCallbackInitial.hitPointWorld);
				applyRecoil(tmpWorldPos, tmpDir, shotHandler.initialDamage);
			}
		}

	}

	private void applyRecoil(Vector3f worldPos, Vector3f dir, float force) {
		//		System.err.println("[PROJECTILE] IMPACT RECOIL: "+shotHandler.hitSegController+": "+force);
		SegmentController root = shotHandler.hitSegController.railController.getRoot();

		//		if(isOnServer()) {
		//			root.getWorldTransformInverse().basis.transform(dir);
		//		}else {
		//			root.getWorldTransformInverse().basis.transform(dir);
		//		}
		boolean negateTorque = false;
		shotHandler.hitSegController.railController.getRoot().hitWithPhysicalRecoil(worldPos, dir, force * 0.1f, negateTorque);
	}
}
