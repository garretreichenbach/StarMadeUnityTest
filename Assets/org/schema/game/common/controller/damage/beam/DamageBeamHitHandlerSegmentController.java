package org.schema.game.common.controller.damage.beam;

import api.listener.events.block.SegmentPieceDamageEvent;
import api.listener.fastevents.DamageBeamHitListener;
import api.listener.fastevents.FastListenerCommon;
import api.listener.fastevents.segmentpiece.SegmentPieceDamageListener;
import api.mod.StarLoader;
import com.bulletphysics.collision.dispatch.CollisionObject;
import org.schema.common.FastMath;
import org.schema.game.common.Starter;
import org.schema.game.common.controller.BeamHandlerContainer;
import org.schema.game.common.controller.EditableSendableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.TransientSegmentController;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.HitReceiverType;
import org.schema.game.common.controller.damage.HitType;
import org.schema.game.common.controller.damage.effects.InterEffectHandler;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.elements.BeamState;
import org.schema.game.common.controller.elements.ShieldAddOn;
import org.schema.game.common.controller.elements.ShieldContainerInterface;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.PersonalBeamHandler;
import org.schema.game.common.data.world.SectorNotFoundException;
import org.schema.game.common.data.world.Segment;
import org.schema.schine.graphicsengine.core.Timer;

import javax.vecmath.Vector3f;
import java.util.Collection;

public class DamageBeamHitHandlerSegmentController implements DamageBeamHitHandler {
	private SegmentPiece segmentPiece = new SegmentPiece();
	private final InterEffectSet defenseShield = new InterEffectSet();
	private final InterEffectSet defenseBlock = new InterEffectSet();
	private final InterEffectSet defenseArmor = new InterEffectSet();
	private Damager damager;
	private final DamageDealerType damageDealerType = DamageDealerType.BEAM;
	private long weaponId = Long.MIN_VALUE;
	private float dam;
	private HitType hitType;
	private SegmentController hitController;
	private final InterEffectSet defense = new InterEffectSet();

	public void reset() {
		segmentPiece.reset();
		defenseShield.reset();
		defenseBlock.reset();
		defenseArmor.reset();
		this.damager = null;
		this.hitType = null;
		this.hitController = null;
		weaponId = Long.MIN_VALUE;
		this.dam = 0;
	}

	public int onBeamDamage(BeamState hittingBeam, int hits, BeamHandlerContainer<?> container,
	                        SegmentPiece sp, Vector3f from, Vector3f to, Timer timer, Collection<Segment> updatedSegments) {
		segmentPiece.setByReference(sp);

		if(!segmentPiece.isValid()) {
			System.err.println(segmentPiece.getSegmentController().getState() + " HITTTING INVALID PIECE");
			return 0;
		}
		this.hitController = sp.getSegmentController();
		if(hitController instanceof TransientSegmentController) {
			((TransientSegmentController) hitController).setTouched(true, true);
		}

		Starter.modManager.onSegmentControllerHitByBeam(hitController);
		//INSERTED CODE
		for(DamageBeamHitListener listener : FastListenerCommon.damageBeamHitListeners) {
			listener.handle(hittingBeam, hits, container, segmentPiece, from, to, timer, updatedSegments, this);
		}
		///

		short oldType = segmentPiece.getType();
		final ElementInformation info = segmentPiece.getInfo();
		this.damager = hittingBeam.getHandler().getBeamShooter();

		if(!hitController.checkAttack(damager, true, true)) {
			return 0;
		}

		defenseShield.setEffect(hitController.getEffectContainer().get(HitReceiverType.SHIELD));
		defenseShield.add(VoidElementManager.shieldEffectConfiguration);
		if(VoidElementManager.individualBlockEffectArmorOnShieldHit) {
			defenseShield.add(info.effectArmor);
		}

		//individual armor effect defense is added on beam damage
		defenseArmor.setEffect(hitController.getEffectContainer().get(HitReceiverType.ARMOR));
		defenseArmor.add(VoidElementManager.armorEffectConfiguration);

		//individual block effect defense is added on beam damage
		defenseBlock.setEffect(hitController.getEffectContainer().get(HitReceiverType.BLOCK));
		defenseBlock.add(VoidElementManager.basicEffectConfiguration);

		this.weaponId = hittingBeam.weaponId;
		float originalDamage = (hits * hittingBeam.getPowerByBeamLength());
		this.dam = originalDamage;

		if(hittingBeam.beamType == PersonalBeamHandler.TORCH) {
			this.dam = FastMath.ceil((float) info.getMaxHitPointsFull() / (float) info.getMaxHitPointsByte()) * (int) (hits * hittingBeam.getPowerByBeamLength());  //always do damage
		}
		hitType = hittingBeam.hitType;

		dam *= hitController.getDamageTakenMultiplier(damageDealerType);

		if(damager != null) {
			dam *= damager.getDamageGivenMultiplier();
		}

		boolean shieldHit = false;
		if(!hittingBeam.ignoreShield && hitController instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) hitController).getManagerContainer() instanceof ShieldContainerInterface) {

			ShieldContainerInterface sh = ((ShieldContainerInterface) ((ManagedSegmentController<?>) hitController).getManagerContainer());
			ShieldAddOn shieldAddOn = sh.getShieldAddOn();
			if(hitController.isUsingLocalShields()) {
				if(shieldAddOn.isUsingLocalShieldsAtLeastOneActive() || hitController.railController.isDockedAndExecuted()) {
					try {
						float damBef = dam;
						dam = (float) shieldAddOn.handleShieldHit(damager, defenseShield, hittingBeam.hitPoint, hittingBeam.hitSectorId, damageDealerType, hitType, dam, weaponId);
					} catch(SectorNotFoundException e) {
						e.printStackTrace();
						dam = 0;
					}
					if(dam <= 0) {
						hitController.sendHitConfirmToDamager(damager, true);
						return 0;
					}
				}
			} else {
				//check if shields are present so we dont return on 0 damage (to apply possible effects)
				if(shieldAddOn.getShields() > 0 || hitController.railController.isDockedAndExecuted()) {
					try {
						dam = (float) shieldAddOn.handleShieldHit(damager, defenseShield, hittingBeam.hitPoint, hittingBeam.hitSectorId, damageDealerType, hitType, dam, weaponId);
					} catch(SectorNotFoundException e) {
						e.printStackTrace();
						dam = 0;
					}
					if(dam <= 0) {
						hitController.sendHitConfirmToDamager(damager, true);
						return 0;
					}
				}

				shieldHit = sh.getShieldAddOn().getShields() > 0;
			}
		}
		hitController.sendHitConfirmToDamager(damager, shieldHit);
		this.dam = (int) hittingBeam.calcPreviousArmorDamageReduction(this.dam, hitController);
//		if(info.isArmor()) {
//			this.dam = Math.max(1, this.dam - info.getArmorValue() * VoidElementManager.ARMOR_BEAM_DAMAGE_SCALING);
//		}

		dam = hitController.getHpController().onHullDamage(damager, dam, segmentPiece.getType(), damageDealerType);

//		System.err.println("DAMAGE BLOCK BY BEAM: "+segmentPiece+"; "+segmentPiece.getSegmentController()+"; "+segmentPiece.getSegmentController().getState());
		boolean killed = doDamageOnBlock(segmentPiece, hittingBeam);
		if(killed && info.isArmor()) {

			hittingBeam.getHandler().onArmorBlockKilled(hittingBeam, info.getArmorValue());
		}

		CollisionObject pObject = hitController.getPhysicsDataContainer().getObject();
		if(pObject != null) {
			pObject.activate(true);
		}

		Starter.modManager.onSegmentControllerDamageTaken(hitController);
		return hits;
	}

	private boolean doDamageOnBlock(SegmentPiece hitPiece, BeamState hittingBeam) {
		int orientationOrig = hitPiece.getOrientation();
		int orientation = orientationOrig;
		final short type = hitPiece.getType();
		final ElementInformation info = hitPiece.getInfo();
		if(type == ElementKeyMap.CARGO_SPACE) {
			//cargo may be hit
			orientation = Element.TOP;
		}
		HitReceiverType hitReceiverType = info.isArmor() ? HitReceiverType.ARMOR : HitReceiverType.BLOCK;
		InterEffectSet globalDefense = info.isArmor() ? defenseArmor : defenseBlock;
		assert (damager != null);
		assert (damageDealerType != null);
		InterEffectSet attack = damager.getAttackEffectSet(weaponId, damageDealerType);

		this.defense.setEffect(globalDefense);
		this.defense.scaleAdd(info.effectArmor, 1);

		final float damageOnBlock = InterEffectHandler.handleEffects(dam, attack, this.defense, hitType, damageDealerType, hitReceiverType, type);
		float restDamage = damageOnBlock;

		int damage = FastMath.round(damageOnBlock);
		final int hitpointsBef = hitPiece.getHitpointsFull();

		EditableSendableSegmentController eSeg = (EditableSendableSegmentController) hitController;

		//INSERTED CODE
		if(StarLoader.hasListeners(SegmentPieceDamageEvent.class)) {
			SegmentPieceDamageEvent ev = new SegmentPieceDamageEvent(segmentPiece.getSegmentController(), segmentPiece.getAbsoluteIndex(), type, damage, damageDealerType, damager);
			StarLoader.fireEvent(ev, isOnServer());
			if(ev.isCanceled()) return false;
			damage = ev.getDamage();
		}
		for(SegmentPieceDamageListener listener : FastListenerCommon.segmentPieceDamageListeners)
			damage = listener.onBlockDamage(segmentPiece.getSegmentController(), segmentPiece.getAbsoluteIndex(), type, damage, damageDealerType, damager, isOnServer());
		if(damage <= 0) return false;
		///

		float actualDamage = eSeg.damageElement(type, hitPiece.getInfoIndex(), hitPiece.getSegment().getSegmentData(), damage, damager, damageDealerType, weaponId);
		int hitPointsRemain = (int) (hitpointsBef - actualDamage);

		restDamage = Math.max(0f, restDamage - actualDamage);

		if(hitPointsRemain > 0) {
			//block was not killed
			if(isOnServer()) {
				eSeg.sendBlockHpByte(hitPiece.getAbsoluteIndex(), ElementKeyMap.convertToByteHP(type, hitPointsRemain));
				//called delayed for client
				eSeg.onBlockDamage(hitPiece.getAbsoluteIndex(), type, damage, damageDealerType, damager);
			}
			restDamage = 0;
		} else {
			//block was killed
			if(isOnServer()) {
				eSeg.sendBlockKill(hitPiece.getAbsoluteIndex());
				//called delayed for client
				eSeg.onBlockKill(hitPiece, damager);
			}

			if(isOnServer() && hittingBeam.acidDamagePercent > 0) {
				int acidDamage = (int) (hittingBeam.acidDamagePercent * restDamage);
				boolean decoOnly = false;
				eSeg.getAcidDamageManagerServer().inputDamage(
						hitPiece.getAbsoluteIndex(),
						hittingBeam.hitNormalRelative,
						acidDamage,
						16,
						hittingBeam.getHandler().getBeamShooter(),
						hittingBeam.weaponId,
						true, decoOnly);

				restDamage = Math.max(restDamage - acidDamage, 0);
			}
		}
		return hitPointsRemain <= 0;
	}

	private boolean isOnServer() {
		return hitController.isOnServer();
	}
}
