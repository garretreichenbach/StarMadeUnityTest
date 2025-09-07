package org.schema.schine.graphicsengine.animation.structure.classes;

public class AnimationIndex {

	public static final AnimationIndexElement ATTACKING_MEELEE_FLOATING = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.attacking.attackingMelee.attackingMeleeFloating;
		}

		@Override
		public String toString() {
			return "ATTACKING_MEELEE_FLOATING";
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(AttackingMeleeFloating.class)) {
				return true;
			}
			if (clazz.equals(AttackingMelee.class)) {
				return true;
			}
			if (clazz.equals(Attacking.class)) {
				return true;
			}

			return false;
		}

	};
	public static final AnimationIndexElement ATTACKING_MEELEE_GRAVITY = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.attacking.attackingMelee.attackingMeleeGravity;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(AttackingMeleeGravity.class)) {
				return true;
			}
			if (clazz.equals(AttackingMelee.class)) {
				return true;
			}
			if (clazz.equals(Attacking.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "ATTACKING_MEELEE_GRAVITY";
		}
	};
	public static final AnimationIndexElement DEATH_FLOATING = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.death.deathFloating;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(DeathFloating.class)) {
				return true;
			}
			if (clazz.equals(Death.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "DEATH_FLOATING";
		}
	};
	public static final AnimationIndexElement DEATH_GRAVITY = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.death.deathGravity;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(DeathGravity.class)) {
				return true;
			}
			if (clazz.equals(Death.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "DEATH_GRAVITY";
		}
	};
	public static final AnimationIndexElement SITTING_BLOCK_NOFLOOR_IDLE = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.sitting.sittingBlock.sittingBlockNoFloor.sittingBlockNoFloorIdle;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(SittingBlockNoFloorIdle.class)) {
				return true;
			}
			if (clazz.equals(SittingBlockNoFloor.class)) {
				return true;
			}
			if (clazz.equals(SittingBlock.class)) {
				return true;
			}
			if (clazz.equals(Sitting.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "SITTING_BLOCK_NOFLOOR_IDLE";
		}
	};
	public static final AnimationIndexElement SITTING_BLOCK_FLOOR_IDLE = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.sitting.sittingBlock.sittingBlockFloor.sittingBlockFloorIdle;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(SittingBlockFloorIdle.class)) {
				return true;
			}
			if (clazz.equals(SittingBlockFloor.class)) {
				return true;
			}
			if (clazz.equals(SittingBlock.class)) {
				return true;
			}
			if (clazz.equals(Sitting.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "SITTING_BLOCK_FLOOR_IDLE";
		}
	};
	public static final AnimationIndexElement SITTING_WEDGE_NOFLOOR_IDLE = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.sitting.sittingWedge.sittingWedgeNoFloor.sittingWedgeNoFloorIdle;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(SittingWedgeNoFloorIdle.class)) {
				return true;
			}
			if (clazz.equals(SittingWedgeNoFloor.class)) {
				return true;
			}
			if (clazz.equals(SittingWedge.class)) {
				return true;
			}
			if (clazz.equals(Sitting.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "SITTING_WEDGE_NOFLOOR_IDLE";
		}
	};
	public static final AnimationIndexElement SITTING_WEDGE_FLOOR_IDLE = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.sitting.sittingWedge.sittingWedgeFloor.sittingWedgeFloorIdle;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(SittingWedgeFloorIdle.class)) {
				return true;
			}
			if (clazz.equals(SittingWedgeFloor.class)) {
				return true;
			}
			if (clazz.equals(SittingWedge.class)) {
				return true;
			}
			if (clazz.equals(Sitting.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "SITTING_WEDGE_FLOOR_IDLE";
		}
	};
	public static final AnimationIndexElement HIT_SMALL_FLOATING = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.hit.hitSmall.hitSmallFloating;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(HitSmallFloating.class)) {
				return true;
			}
			if (clazz.equals(HitSmall.class)) {
				return true;
			}
			if (clazz.equals(Hit.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "HIT_SMALL_FLOATING";
		}
	};
	public static final AnimationIndexElement HIT_SMALL_GRAVITY = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.hit.hitSmall.hitSmallGravity;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(HitSmallGravity.class)) {
				return true;
			}
			if (clazz.equals(HitSmall.class)) {
				return true;
			}
			if (clazz.equals(Hit.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "HIT_SMALL_GRAVITY";
		}
	};
	public static final AnimationIndexElement DANCING_GRAVITY = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.dancing.dancingGravity;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(DancingGravity.class)) {
				return true;
			}
			if (clazz.equals(Dancing.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "DANCING_GRAVITY";
		}
	};
	public static final AnimationIndexElement IDLING_FLOATING = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.idling.idlingFloating;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(IdlingFloating.class)) {
				return true;
			}
			if (clazz.equals(Idling.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "IDLING_FLOATING";
		}
	};
	public static final AnimationIndexElement IDLING_GRAVITY = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.idling.idlingGravity;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(IdlingGravity.class)) {
				return true;
			}
			if (clazz.equals(Idling.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "IDLING_GRAVITY";
		}
	};
	public static final AnimationIndexElement SALUTES_SALUTE = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.salutes.salutesSalute;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(SalutesSalute.class)) {
				return true;
			}
			if (clazz.equals(Salutes.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "SALUTES_SALUTE";
		}
	};
	public static final AnimationIndexElement TALK_SALUTE = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.talk.talkSalute;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(TalkSalute.class)) {
				return true;
			}
			if (clazz.equals(Talk.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "TALK_SALUTE";
		}
	};
	public static final AnimationIndexElement MOVING_JUMPING_JUMPUP = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingJumping.movingJumpingJumpUp;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingJumpingJumpUp.class)) {
				return true;
			}
			if (clazz.equals(MovingJumping.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_JUMPING_JUMPUP";
		}
	};
	public static final AnimationIndexElement MOVING_JUMPING_JUMPDOWN = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingJumping.movingJumpingJumpDown;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingJumpingJumpDown.class)) {
				return true;
			}
			if (clazz.equals(MovingJumping.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_JUMPING_JUMPDOWN";
		}
	};
	public static final AnimationIndexElement MOVING_FALLING_STANDARD = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingFalling.movingFallingStandard;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingFallingStandard.class)) {
				return true;
			}
			if (clazz.equals(MovingFalling.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_FALLING_STANDARD";
		}
	};
	public static final AnimationIndexElement MOVING_FALLING_LEDGE = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingFalling.movingFallingLedge;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingFallingLedge.class)) {
				return true;
			}
			if (clazz.equals(MovingFalling.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_FALLING_LEDGE";
		}
	};
	public static final AnimationIndexElement MOVING_LANDING_SHORTFALL = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingLanding.movingLandingShortFall;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingLandingShortFall.class)) {
				return true;
			}
			if (clazz.equals(MovingLanding.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_LANDING_SHORTFALL";
		}
	};
	public static final AnimationIndexElement MOVING_LANDING_MIDDLEFALL = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingLanding.movingLandingMiddleFall;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingLandingMiddleFall.class)) {
				return true;
			}
			if (clazz.equals(MovingLanding.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_LANDING_MIDDLEFALL";
		}
	};
	public static final AnimationIndexElement MOVING_LANDING_LONGFALL = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingLanding.movingLandingLongFall;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingLandingLongFall.class)) {
				return true;
			}
			if (clazz.equals(MovingLanding.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_LANDING_LONGFALL";
		}
	};
	public static final AnimationIndexElement MOVING_NOGRAVITY_GRAVTONOGRAV = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingNoGravity.movingNoGravityGravToNoGrav;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingNoGravityGravToNoGrav.class)) {
				return true;
			}
			if (clazz.equals(MovingNoGravity.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_NOGRAVITY_GRAVTONOGRAV";
		}
	};
	public static final AnimationIndexElement MOVING_NOGRAVITY_NOGRAVTOGRAV = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingNoGravity.movingNoGravityNoGravToGrav;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingNoGravityNoGravToGrav.class)) {
				return true;
			}
			if (clazz.equals(MovingNoGravity.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_NOGRAVITY_NOGRAVTOGRAV";
		}
	};
	public static final AnimationIndexElement MOVING_NOGRAVITY_FLOATMOVEN = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingNoGravity.movingNoGravityFloatMoveN;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingNoGravityFloatMoveN.class)) {
				return true;
			}
			if (clazz.equals(MovingNoGravity.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_NOGRAVITY_FLOATMOVEN";
		}
	};
	public static final AnimationIndexElement MOVING_NOGRAVITY_FLOATMOVES = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingNoGravity.movingNoGravityFloatMoveS;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingNoGravityFloatMoveS.class)) {
				return true;
			}
			if (clazz.equals(MovingNoGravity.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_NOGRAVITY_FLOATMOVES";
		}
	};
	public static final AnimationIndexElement MOVING_NOGRAVITY_FLOATMOVEUP = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingNoGravity.movingNoGravityFloatMoveUp;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingNoGravityFloatMoveUp.class)) {
				return true;
			}
			if (clazz.equals(MovingNoGravity.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_NOGRAVITY_FLOATMOVEUP";
		}
	};
	public static final AnimationIndexElement MOVING_NOGRAVITY_FLOATMOVEDOWN = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingNoGravity.movingNoGravityFloatMoveDown;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingNoGravityFloatMoveDown.class)) {
				return true;
			}
			if (clazz.equals(MovingNoGravity.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_NOGRAVITY_FLOATMOVEDOWN";
		}
	};
	public static final AnimationIndexElement MOVING_NOGRAVITY_FLOATROT = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingNoGravity.movingNoGravityFloatRot;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingNoGravityFloatRot.class)) {
				return true;
			}
			if (clazz.equals(MovingNoGravity.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_NOGRAVITY_FLOATROT";
		}
	};
	public static final AnimationIndexElement MOVING_NOGRAVITY_FLOATHIT = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingNoGravity.movingNoGravityFloatHit;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingNoGravityFloatHit.class)) {
				return true;
			}
			if (clazz.equals(MovingNoGravity.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_NOGRAVITY_FLOATHIT";
		}
	};
	public static final AnimationIndexElement MOVING_NOGRAVITY_FLOATDEATH = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingNoGravity.movingNoGravityFloatDeath;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingNoGravityFloatDeath.class)) {
				return true;
			}
			if (clazz.equals(MovingNoGravity.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_NOGRAVITY_FLOATDEATH";
		}
	};
	public static final AnimationIndexElement MOVING_BYFOOT_CRAWLING_NORTH = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingByFoot.movingByFootCrawling.movingByFootCrawlingNorth;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingByFootCrawlingNorth.class)) {
				return true;
			}
			if (clazz.equals(MovingByFootCrawling.class)) {
				return true;
			}
			if (clazz.equals(MovingByFoot.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_BYFOOT_CRAWLING_NORTH";
		}
	};
	public static final AnimationIndexElement MOVING_BYFOOT_CRAWLING_SOUTH = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingByFoot.movingByFootCrawling.movingByFootCrawlingSouth;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingByFootCrawlingSouth.class)) {
				return true;
			}
			if (clazz.equals(MovingByFootCrawling.class)) {
				return true;
			}
			if (clazz.equals(MovingByFoot.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_BYFOOT_CRAWLING_SOUTH";
		}
	};
	public static final AnimationIndexElement MOVING_BYFOOT_CRAWLING_WEST = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingByFoot.movingByFootCrawling.movingByFootCrawlingWest;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingByFootCrawlingWest.class)) {
				return true;
			}
			if (clazz.equals(MovingByFootCrawling.class)) {
				return true;
			}
			if (clazz.equals(MovingByFoot.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_BYFOOT_CRAWLING_WEST";
		}
	};
	public static final AnimationIndexElement MOVING_BYFOOT_CRAWLING_EAST = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingByFoot.movingByFootCrawling.movingByFootCrawlingEast;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingByFootCrawlingEast.class)) {
				return true;
			}
			if (clazz.equals(MovingByFootCrawling.class)) {
				return true;
			}
			if (clazz.equals(MovingByFoot.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_BYFOOT_CRAWLING_EAST";
		}
	};
	public static final AnimationIndexElement MOVING_BYFOOT_CRAWLING_NORTHEAST = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingByFoot.movingByFootCrawling.movingByFootCrawlingNorthEast;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingByFootCrawlingNorthEast.class)) {
				return true;
			}
			if (clazz.equals(MovingByFootCrawling.class)) {
				return true;
			}
			if (clazz.equals(MovingByFoot.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_BYFOOT_CRAWLING_NORTHEAST";
		}
	};
	public static final AnimationIndexElement MOVING_BYFOOT_CRAWLING_NORTHWEST = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingByFoot.movingByFootCrawling.movingByFootCrawlingNorthWest;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingByFootCrawlingNorthWest.class)) {
				return true;
			}
			if (clazz.equals(MovingByFootCrawling.class)) {
				return true;
			}
			if (clazz.equals(MovingByFoot.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_BYFOOT_CRAWLING_NORTHWEST";
		}
	};
	public static final AnimationIndexElement MOVING_BYFOOT_CRAWLING_SOUTHWEST = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingByFoot.movingByFootCrawling.movingByFootCrawlingSouthWest;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingByFootCrawlingSouthWest.class)) {
				return true;
			}
			if (clazz.equals(MovingByFootCrawling.class)) {
				return true;
			}
			if (clazz.equals(MovingByFoot.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_BYFOOT_CRAWLING_SOUTHWEST";
		}
	};
	public static final AnimationIndexElement MOVING_BYFOOT_CRAWLING_SOUTHEAST = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingByFoot.movingByFootCrawling.movingByFootCrawlingSouthEast;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingByFootCrawlingSouthEast.class)) {
				return true;
			}
			if (clazz.equals(MovingByFootCrawling.class)) {
				return true;
			}
			if (clazz.equals(MovingByFoot.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_BYFOOT_CRAWLING_SOUTHEAST";
		}
	};
	public static final AnimationIndexElement MOVING_BYFOOT_SLOWWALKING_NORTH = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingByFoot.movingByFootSlowWalking.movingByFootSlowWalkingNorth;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingByFootSlowWalkingNorth.class)) {
				return true;
			}
			if (clazz.equals(MovingByFootSlowWalking.class)) {
				return true;
			}
			if (clazz.equals(MovingByFoot.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_BYFOOT_SLOWWALKING_NORTH";
		}
	};
	public static final AnimationIndexElement MOVING_BYFOOT_SLOWWALKING_SOUTH = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingByFoot.movingByFootSlowWalking.movingByFootSlowWalkingSouth;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingByFootSlowWalkingSouth.class)) {
				return true;
			}
			if (clazz.equals(MovingByFootSlowWalking.class)) {
				return true;
			}
			if (clazz.equals(MovingByFoot.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_BYFOOT_SLOWWALKING_SOUTH";
		}
	};
	public static final AnimationIndexElement MOVING_BYFOOT_SLOWWALKING_WEST = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingByFoot.movingByFootSlowWalking.movingByFootSlowWalkingWest;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingByFootSlowWalkingWest.class)) {
				return true;
			}
			if (clazz.equals(MovingByFootSlowWalking.class)) {
				return true;
			}
			if (clazz.equals(MovingByFoot.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_BYFOOT_SLOWWALKING_WEST";
		}
	};
	public static final AnimationIndexElement MOVING_BYFOOT_SLOWWALKING_EAST = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingByFoot.movingByFootSlowWalking.movingByFootSlowWalkingEast;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingByFootSlowWalkingEast.class)) {
				return true;
			}
			if (clazz.equals(MovingByFootSlowWalking.class)) {
				return true;
			}
			if (clazz.equals(MovingByFoot.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_BYFOOT_SLOWWALKING_EAST";
		}
	};
	public static final AnimationIndexElement MOVING_BYFOOT_SLOWWALKING_NORTHEAST = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingByFoot.movingByFootSlowWalking.movingByFootSlowWalkingNorthEast;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingByFootSlowWalkingNorthEast.class)) {
				return true;
			}
			if (clazz.equals(MovingByFootSlowWalking.class)) {
				return true;
			}
			if (clazz.equals(MovingByFoot.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_BYFOOT_SLOWWALKING_NORTHEAST";
		}
	};
	public static final AnimationIndexElement MOVING_BYFOOT_SLOWWALKING_NORTHWEST = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingByFoot.movingByFootSlowWalking.movingByFootSlowWalkingNorthWest;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingByFootSlowWalkingNorthWest.class)) {
				return true;
			}
			if (clazz.equals(MovingByFootSlowWalking.class)) {
				return true;
			}
			if (clazz.equals(MovingByFoot.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_BYFOOT_SLOWWALKING_NORTHWEST";
		}
	};
	public static final AnimationIndexElement MOVING_BYFOOT_SLOWWALKING_SOUTHWEST = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingByFoot.movingByFootSlowWalking.movingByFootSlowWalkingSouthWest;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingByFootSlowWalkingSouthWest.class)) {
				return true;
			}
			if (clazz.equals(MovingByFootSlowWalking.class)) {
				return true;
			}
			if (clazz.equals(MovingByFoot.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_BYFOOT_SLOWWALKING_SOUTHWEST";
		}
	};
	public static final AnimationIndexElement MOVING_BYFOOT_SLOWWALKING_SOUTHEAST = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingByFoot.movingByFootSlowWalking.movingByFootSlowWalkingSouthEast;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingByFootSlowWalkingSouthEast.class)) {
				return true;
			}
			if (clazz.equals(MovingByFootSlowWalking.class)) {
				return true;
			}
			if (clazz.equals(MovingByFoot.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_BYFOOT_SLOWWALKING_SOUTHEAST";
		}
	};
	public static final AnimationIndexElement MOVING_BYFOOT_WALKING_NORTH = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingByFoot.movingByFootWalking.movingByFootWalkingNorth;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingByFootWalkingNorth.class)) {
				return true;
			}
			if (clazz.equals(MovingByFootWalking.class)) {
				return true;
			}
			if (clazz.equals(MovingByFoot.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_BYFOOT_WALKING_NORTH";
		}
	};
	public static final AnimationIndexElement MOVING_BYFOOT_WALKING_SOUTH = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingByFoot.movingByFootWalking.movingByFootWalkingSouth;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingByFootWalkingSouth.class)) {
				return true;
			}
			if (clazz.equals(MovingByFootWalking.class)) {
				return true;
			}
			if (clazz.equals(MovingByFoot.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_BYFOOT_WALKING_SOUTH";
		}
	};
	public static final AnimationIndexElement MOVING_BYFOOT_WALKING_WEST = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingByFoot.movingByFootWalking.movingByFootWalkingWest;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingByFootWalkingWest.class)) {
				return true;
			}
			if (clazz.equals(MovingByFootWalking.class)) {
				return true;
			}
			if (clazz.equals(MovingByFoot.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_BYFOOT_WALKING_WEST";
		}
	};
	public static final AnimationIndexElement MOVING_BYFOOT_WALKING_EAST = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingByFoot.movingByFootWalking.movingByFootWalkingEast;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingByFootWalkingEast.class)) {
				return true;
			}
			if (clazz.equals(MovingByFootWalking.class)) {
				return true;
			}
			if (clazz.equals(MovingByFoot.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_BYFOOT_WALKING_EAST";
		}
	};
	public static final AnimationIndexElement MOVING_BYFOOT_WALKING_NORTHEAST = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingByFoot.movingByFootWalking.movingByFootWalkingNorthEast;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingByFootWalkingNorthEast.class)) {
				return true;
			}
			if (clazz.equals(MovingByFootWalking.class)) {
				return true;
			}
			if (clazz.equals(MovingByFoot.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_BYFOOT_WALKING_NORTHEAST";
		}
	};
	public static final AnimationIndexElement MOVING_BYFOOT_WALKING_NORTHWEST = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingByFoot.movingByFootWalking.movingByFootWalkingNorthWest;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingByFootWalkingNorthWest.class)) {
				return true;
			}
			if (clazz.equals(MovingByFootWalking.class)) {
				return true;
			}
			if (clazz.equals(MovingByFoot.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_BYFOOT_WALKING_NORTHWEST";
		}
	};
	public static final AnimationIndexElement MOVING_BYFOOT_WALKING_SOUTHWEST = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingByFoot.movingByFootWalking.movingByFootWalkingSouthWest;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingByFootWalkingSouthWest.class)) {
				return true;
			}
			if (clazz.equals(MovingByFootWalking.class)) {
				return true;
			}
			if (clazz.equals(MovingByFoot.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_BYFOOT_WALKING_SOUTHWEST";
		}
	};
	public static final AnimationIndexElement MOVING_BYFOOT_WALKING_SOUTHEAST = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingByFoot.movingByFootWalking.movingByFootWalkingSouthEast;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingByFootWalkingSouthEast.class)) {
				return true;
			}
			if (clazz.equals(MovingByFootWalking.class)) {
				return true;
			}
			if (clazz.equals(MovingByFoot.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_BYFOOT_WALKING_SOUTHEAST";
		}
	};
	public static final AnimationIndexElement MOVING_BYFOOT_RUNNING_NORTH = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingByFoot.movingByFootRunning.movingByFootRunningNorth;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingByFootRunningNorth.class)) {
				return true;
			}
			if (clazz.equals(MovingByFootRunning.class)) {
				return true;
			}
			if (clazz.equals(MovingByFoot.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_BYFOOT_RUNNING_NORTH";
		}
	};
	public static final AnimationIndexElement MOVING_BYFOOT_RUNNING_SOUTH = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingByFoot.movingByFootRunning.movingByFootRunningSouth;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingByFootRunningSouth.class)) {
				return true;
			}
			if (clazz.equals(MovingByFootRunning.class)) {
				return true;
			}
			if (clazz.equals(MovingByFoot.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_BYFOOT_RUNNING_SOUTH";
		}
	};
	public static final AnimationIndexElement MOVING_BYFOOT_RUNNING_WEST = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingByFoot.movingByFootRunning.movingByFootRunningWest;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingByFootRunningWest.class)) {
				return true;
			}
			if (clazz.equals(MovingByFootRunning.class)) {
				return true;
			}
			if (clazz.equals(MovingByFoot.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_BYFOOT_RUNNING_WEST";
		}
	};
	public static final AnimationIndexElement MOVING_BYFOOT_RUNNING_EAST = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingByFoot.movingByFootRunning.movingByFootRunningEast;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingByFootRunningEast.class)) {
				return true;
			}
			if (clazz.equals(MovingByFootRunning.class)) {
				return true;
			}
			if (clazz.equals(MovingByFoot.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_BYFOOT_RUNNING_EAST";
		}
	};
	public static final AnimationIndexElement MOVING_BYFOOT_RUNNING_NORTHEAST = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingByFoot.movingByFootRunning.movingByFootRunningNorthEast;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingByFootRunningNorthEast.class)) {
				return true;
			}
			if (clazz.equals(MovingByFootRunning.class)) {
				return true;
			}
			if (clazz.equals(MovingByFoot.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_BYFOOT_RUNNING_NORTHEAST";
		}
	};
	public static final AnimationIndexElement MOVING_BYFOOT_RUNNING_NORTHWEST = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingByFoot.movingByFootRunning.movingByFootRunningNorthWest;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingByFootRunningNorthWest.class)) {
				return true;
			}
			if (clazz.equals(MovingByFootRunning.class)) {
				return true;
			}
			if (clazz.equals(MovingByFoot.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_BYFOOT_RUNNING_NORTHWEST";
		}
	};
	public static final AnimationIndexElement MOVING_BYFOOT_RUNNING_SOUTHWEST = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingByFoot.movingByFootRunning.movingByFootRunningSouthWest;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingByFootRunningSouthWest.class)) {
				return true;
			}
			if (clazz.equals(MovingByFootRunning.class)) {
				return true;
			}
			if (clazz.equals(MovingByFoot.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_BYFOOT_RUNNING_SOUTHWEST";
		}
	};
	public static final AnimationIndexElement MOVING_BYFOOT_RUNNING_SOUTHEAST = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.moving.movingByFoot.movingByFootRunning.movingByFootRunningSouthEast;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(MovingByFootRunningSouthEast.class)) {
				return true;
			}
			if (clazz.equals(MovingByFootRunning.class)) {
				return true;
			}
			if (clazz.equals(MovingByFoot.class)) {
				return true;
			}
			if (clazz.equals(Moving.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "MOVING_BYFOOT_RUNNING_SOUTHEAST";
		}
	};
	public static final AnimationIndexElement HELMET_ON = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.helmet.helmetOn;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(HelmetOn.class)) {
				return true;
			}
			if (clazz.equals(Helmet.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "HELMET_ON";
		}
	};
	public static final AnimationIndexElement HELMET_OFF = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.helmet.helmetOff;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(HelmetOff.class)) {
				return true;
			}
			if (clazz.equals(Helmet.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "HELMET_OFF";
		}
	};
	public static final AnimationIndexElement UPPERBODY_GUN_IDLE = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.upperBody.upperBodyGun.upperBodyGunIdle;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(UpperBodyGunIdle.class)) {
				return true;
			}
			if (clazz.equals(UpperBodyGun.class)) {
				return true;
			}
			if (clazz.equals(UpperBody.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "UPPERBODY_GUN_IDLE";
		}
	};
	public static final AnimationIndexElement UPPERBODY_GUN_IDLEIN = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.upperBody.upperBodyGun.upperBodyGunIdleIn;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(UpperBodyGunIdleIn.class)) {
				return true;
			}
			if (clazz.equals(UpperBodyGun.class)) {
				return true;
			}
			if (clazz.equals(UpperBody.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "UPPERBODY_GUN_IDLEIN";
		}
	};
	public static final AnimationIndexElement UPPERBODY_GUN_DRAW = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.upperBody.upperBodyGun.upperBodyGunDraw;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(UpperBodyGunDraw.class)) {
				return true;
			}
			if (clazz.equals(UpperBodyGun.class)) {
				return true;
			}
			if (clazz.equals(UpperBody.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "UPPERBODY_GUN_DRAW";
		}
	};
	public static final AnimationIndexElement UPPERBODY_GUN_AWAY = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.upperBody.upperBodyGun.upperBodyGunAway;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(UpperBodyGunAway.class)) {
				return true;
			}
			if (clazz.equals(UpperBodyGun.class)) {
				return true;
			}
			if (clazz.equals(UpperBody.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "UPPERBODY_GUN_AWAY";
		}
	};
	public static final AnimationIndexElement UPPERBODY_GUN_FIRE = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.upperBody.upperBodyGun.upperBodyGunFire;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(UpperBodyGunFire.class)) {
				return true;
			}
			if (clazz.equals(UpperBodyGun.class)) {
				return true;
			}
			if (clazz.equals(UpperBody.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "UPPERBODY_GUN_FIRE";
		}
	};
	public static final AnimationIndexElement UPPERBODY_GUN_FIREHEAVY = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.upperBody.upperBodyGun.upperBodyGunFireHeavy;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(UpperBodyGunFireHeavy.class)) {
				return true;
			}
			if (clazz.equals(UpperBodyGun.class)) {
				return true;
			}
			if (clazz.equals(UpperBody.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "UPPERBODY_GUN_FIREHEAVY";
		}
	};
	public static final AnimationIndexElement UPPERBODY_GUN_MEELEE = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.upperBody.upperBodyGun.upperBodyGunMelee;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(UpperBodyGunMelee.class)) {
				return true;
			}
			if (clazz.equals(UpperBodyGun.class)) {
				return true;
			}
			if (clazz.equals(UpperBody.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "UPPERBODY_GUN_MEELEE";
		}
	};
	public static final AnimationIndexElement UPPERBODY_FABRICATOR_IDLE = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.upperBody.upperBodyFabricator.upperBodyFabricatorIdle;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(UpperBodyFabricatorIdle.class)) {
				return true;
			}
			if (clazz.equals(UpperBodyFabricator.class)) {
				return true;
			}
			if (clazz.equals(UpperBody.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "UPPERBODY_FABRICATOR_IDLE";
		}
	};
	public static final AnimationIndexElement UPPERBODY_FABRICATOR_DRAW = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.upperBody.upperBodyFabricator.upperBodyFabricatorDraw;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(UpperBodyFabricatorDraw.class)) {
				return true;
			}
			if (clazz.equals(UpperBodyFabricator.class)) {
				return true;
			}
			if (clazz.equals(UpperBody.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "UPPERBODY_FABRICATOR_DRAW";
		}
	};
	public static final AnimationIndexElement UPPERBODY_FABRICATOR_AWAY = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.upperBody.upperBodyFabricator.upperBodyFabricatorAway;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(UpperBodyFabricatorAway.class)) {
				return true;
			}
			if (clazz.equals(UpperBodyFabricator.class)) {
				return true;
			}
			if (clazz.equals(UpperBody.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "UPPERBODY_FABRICATOR_AWAY";
		}
	};
	public static final AnimationIndexElement UPPERBODY_FABRICATOR_FIRE = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.upperBody.upperBodyFabricator.upperBodyFabricatorFire;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(UpperBodyFabricatorFire.class)) {
				return true;
			}
			if (clazz.equals(UpperBodyFabricator.class)) {
				return true;
			}
			if (clazz.equals(UpperBody.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "UPPERBODY_FABRICATOR_FIRE";
		}
	};
	public static final AnimationIndexElement UPPERBODY_FABRICATOR_PUMPACTION = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.upperBody.upperBodyFabricator.upperBodyFabricatorPumpAction;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(UpperBodyFabricatorPumpAction.class)) {
				return true;
			}
			if (clazz.equals(UpperBodyFabricator.class)) {
				return true;
			}
			if (clazz.equals(UpperBody.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "UPPERBODY_FABRICATOR_PUMPACTION";
		}
	};
	public static final AnimationIndexElement UPPERBODY_BAREHAND_MEELEE = new AnimationIndexElement() {

		@Override
		public AnimationStructEndPoint get(AnimationStructure root) {
			return root.upperBody.upperBodyBareHand.upperBodyBareHandMelee;
		}

		@Override
		public boolean isType(Class<? extends AnimationStructSet> clazz) {
			if (clazz.equals(UpperBodyBareHandMelee.class)) {
				return true;
			}
			if (clazz.equals(UpperBodyBareHand.class)) {
				return true;
			}
			if (clazz.equals(UpperBody.class)) {
				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "UPPERBODY_BAREHAND_MEELEE";
		}
	};
	public static AnimationIndexElement[] animations = new AnimationIndexElement[78];

	static {
		animations[0] = ATTACKING_MEELEE_FLOATING;
		animations[1] = ATTACKING_MEELEE_GRAVITY;
		animations[2] = DEATH_FLOATING;
		animations[3] = DEATH_GRAVITY;
		animations[4] = SITTING_BLOCK_NOFLOOR_IDLE;
		animations[5] = SITTING_BLOCK_FLOOR_IDLE;
		animations[6] = SITTING_WEDGE_NOFLOOR_IDLE;
		animations[7] = SITTING_WEDGE_FLOOR_IDLE;
		animations[8] = HIT_SMALL_FLOATING;
		animations[9] = HIT_SMALL_GRAVITY;
		animations[10] = DANCING_GRAVITY;
		animations[11] = IDLING_FLOATING;
		animations[12] = IDLING_GRAVITY;
		animations[13] = SALUTES_SALUTE;
		animations[14] = TALK_SALUTE;
		animations[15] = MOVING_JUMPING_JUMPUP;
		animations[16] = MOVING_JUMPING_JUMPDOWN;
		animations[17] = MOVING_FALLING_STANDARD;
		animations[18] = MOVING_FALLING_LEDGE;
		animations[19] = MOVING_LANDING_SHORTFALL;
		animations[20] = MOVING_LANDING_MIDDLEFALL;
		animations[21] = MOVING_LANDING_LONGFALL;
		animations[22] = MOVING_NOGRAVITY_GRAVTONOGRAV;
		animations[23] = MOVING_NOGRAVITY_NOGRAVTOGRAV;
		animations[24] = MOVING_NOGRAVITY_FLOATMOVEN;
		animations[25] = MOVING_NOGRAVITY_FLOATMOVES;
		animations[26] = MOVING_NOGRAVITY_FLOATMOVEUP;
		animations[27] = MOVING_NOGRAVITY_FLOATMOVEDOWN;
		animations[28] = MOVING_NOGRAVITY_FLOATROT;
		animations[29] = MOVING_NOGRAVITY_FLOATHIT;
		animations[30] = MOVING_NOGRAVITY_FLOATDEATH;
		animations[31] = MOVING_BYFOOT_CRAWLING_NORTH;
		animations[32] = MOVING_BYFOOT_CRAWLING_SOUTH;
		animations[33] = MOVING_BYFOOT_CRAWLING_WEST;
		animations[34] = MOVING_BYFOOT_CRAWLING_EAST;
		animations[35] = MOVING_BYFOOT_CRAWLING_NORTHEAST;
		animations[36] = MOVING_BYFOOT_CRAWLING_NORTHWEST;
		animations[37] = MOVING_BYFOOT_CRAWLING_SOUTHWEST;
		animations[38] = MOVING_BYFOOT_CRAWLING_SOUTHEAST;
		animations[39] = MOVING_BYFOOT_SLOWWALKING_NORTH;
		animations[40] = MOVING_BYFOOT_SLOWWALKING_SOUTH;
		animations[41] = MOVING_BYFOOT_SLOWWALKING_WEST;
		animations[42] = MOVING_BYFOOT_SLOWWALKING_EAST;
		animations[43] = MOVING_BYFOOT_SLOWWALKING_NORTHEAST;
		animations[44] = MOVING_BYFOOT_SLOWWALKING_NORTHWEST;
		animations[45] = MOVING_BYFOOT_SLOWWALKING_SOUTHWEST;
		animations[46] = MOVING_BYFOOT_SLOWWALKING_SOUTHEAST;
		animations[47] = MOVING_BYFOOT_WALKING_NORTH;
		animations[48] = MOVING_BYFOOT_WALKING_SOUTH;
		animations[49] = MOVING_BYFOOT_WALKING_WEST;
		animations[50] = MOVING_BYFOOT_WALKING_EAST;
		animations[51] = MOVING_BYFOOT_WALKING_NORTHEAST;
		animations[52] = MOVING_BYFOOT_WALKING_NORTHWEST;
		animations[53] = MOVING_BYFOOT_WALKING_SOUTHWEST;
		animations[54] = MOVING_BYFOOT_WALKING_SOUTHEAST;
		animations[55] = MOVING_BYFOOT_RUNNING_NORTH;
		animations[56] = MOVING_BYFOOT_RUNNING_SOUTH;
		animations[57] = MOVING_BYFOOT_RUNNING_WEST;
		animations[58] = MOVING_BYFOOT_RUNNING_EAST;
		animations[59] = MOVING_BYFOOT_RUNNING_NORTHEAST;
		animations[60] = MOVING_BYFOOT_RUNNING_NORTHWEST;
		animations[61] = MOVING_BYFOOT_RUNNING_SOUTHWEST;
		animations[62] = MOVING_BYFOOT_RUNNING_SOUTHEAST;
		animations[63] = HELMET_ON;
		animations[64] = HELMET_OFF;
		animations[65] = UPPERBODY_GUN_IDLE;
		animations[66] = UPPERBODY_GUN_IDLEIN;
		animations[67] = UPPERBODY_GUN_DRAW;
		animations[68] = UPPERBODY_GUN_AWAY;
		animations[69] = UPPERBODY_GUN_FIRE;
		animations[70] = UPPERBODY_GUN_FIREHEAVY;
		animations[71] = UPPERBODY_GUN_MEELEE;
		animations[72] = UPPERBODY_FABRICATOR_IDLE;
		animations[73] = UPPERBODY_FABRICATOR_DRAW;
		animations[74] = UPPERBODY_FABRICATOR_AWAY;
		animations[75] = UPPERBODY_FABRICATOR_FIRE;
		animations[76] = UPPERBODY_FABRICATOR_PUMPACTION;
		animations[77] = UPPERBODY_BAREHAND_MEELEE;
	}
}

