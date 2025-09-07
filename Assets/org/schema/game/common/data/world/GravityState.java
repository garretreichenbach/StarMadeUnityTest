package org.schema.game.common.data.world;

import com.bulletphysics.linearmath.AabbUtil2;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.PlanetIco;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.meta.weapon.GrappleBeam;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.DebugBoundingBox;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

public class GravityState {
	public static final float G = 9.81f;
	private final Vector3f acceleration = new Vector3f();
	private final Vector3f minTmp = new Vector3f();
	private final Vector3f maxTmp = new Vector3f();
	private final Vector3f minTmpOwn = new Vector3f();
	private final Vector3f maxTmpOwn = new Vector3f();
	public SimpleTransformableSendableObject source;
	//if physics is not yet initialized, this will be used
	public SegmentController pendingUpdate;
	public boolean forcedFromServer;
	public boolean withBlockBelow;
	public long grappleStart;
	SegmentPiece ppTmp = new SegmentPiece();
	private boolean changed;
	private long outOfSectorTime;
	private long outOfBoundsTime;
	public boolean differentObjectTouched;
	public boolean central;

	public boolean isAligedOnly() {
		return source != null && acceleration.lengthSquared() == 0 && !central;
	}

	/**
	 * @return the changed
	 */
	public boolean isChanged() {
		return changed;
	}

	/**
	 * @param changed the changed to set
	 */
	public void setChanged(boolean changed) {
		if(this.changed != changed){
			differentObjectTouched = false;
		}
		this.changed = changed;
	}

	public boolean isGravityOrAlignedOn() {
		return isGravityOn() || isAligedOnly();
	}

	public boolean isGravityOn() {
		return source != null && (acceleration.lengthSquared() > 0 || central);
	}

	public boolean isValid(SimpleTransformableSendableObject<?> self) {
		if(self.isHidden()) {
			System.err.println("[GRAVITY] GRAVITY STOP " + self.getState() + " Gravity/Attach for " + self + " is no longer valid because object is hidden");
			return false;
		}

		if (isGravityOrAlignedOn()) {
			if (source instanceof SegmentController) {
				//request the block so AABB is right
				Vector3i blockPositionRelativeTo = SimpleTransformableSendableObject.getBlockPositionRelativeTo(self.getWorldTransform().origin, source, new Vector3i());
				SegmentPiece pointUnsave = ((SegmentController) source).getSegmentBuffer().getPointUnsave(blockPositionRelativeTo, ppTmp); //autorequest true previously
			}
			if(central){
				acceleration.sub(source.getWorldTransform().origin, self.getWorldTransform().origin);
				acceleration.normalize();
				acceleration.scale(((SegmentController)source).getConfigManager().apply(StatusEffectType.GRAVITY_OVERRIDE_ENTITY_CENTRAL, 1f));
			}

			self.getGravityAABB(minTmpOwn, maxTmpOwn);
			source.getGravityAABB(self.getWorldTransform(), minTmp, maxTmp);

			if (self.getState().getUpdateTime() - self.sectorChangedTimeOwnClient > 1000) {

				boolean fullyLoaded = !(source instanceof SegmentController) || ((SegmentController)source).isFullyLoadedWithDock();
				if (source.getSectorId() != self.getSectorId()) {
					System.err.println("[GRAVITY][" + self.getState() + "] GRAVITY STOP gravity reset for " + self + ": sector test failed: gravitySource: " + source.getSectorId() + ", self: " + self.getSectorId());
					if (outOfSectorTime == 0) {
						outOfSectorTime = System.currentTimeMillis();
					}
					return self.getState().getUpdateTime() - outOfSectorTime <= 500;
				} else if (fullyLoaded && !AabbUtil2.testAabbAgainstAabb2(minTmpOwn, maxTmpOwn, minTmp, maxTmp)) {

					if (System.currentTimeMillis() - grappleStart > GrappleBeam.TIME_BEFORE_OUT_OF_BOUNDS) {
						System.err.println("[GRAVITY][" + self.getState() + "] GRAVITY STOP gravity reset for " + self + " -> " + source + ": AABB test failed: Own: " + minTmpOwn + "; " + maxTmpOwn + "  ----  Source " + minTmp + "; " + maxTmp + " of " + source);

						if (outOfBoundsTime == 0) {
							outOfBoundsTime = self.getState().getUpdateTime();
						}
						if(!self.isOnServer() && EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()){
							DebugDrawer.boundingBoxes.add(
									new DebugBoundingBox(minTmpOwn, maxTmpOwn, 1, 0, 0, 1));
							DebugDrawer.boundingBoxes.add(
									new DebugBoundingBox(minTmp, maxTmp, 0, 1, 0, 1));
							DebugDrawer.addArrowFromTransform(source.getWorldTransform(), new Vector4f(0.0f, 1.0f, 0.35f, 1.0f));
						}
						return self.getState().getUpdateTime() - outOfBoundsTime <= 500 || source instanceof PlanetIco;
					} else {
						if (!self.isOnServer()) {
							System.err.println("[GRAVITY][" + self.getState() + "] GRAVITY STOP gravity reset for " + self + " -> " + source + ": AABB test failed: Own: " + minTmpOwn + "; " + maxTmpOwn + "  ----  Source " + minTmp + "; " + maxTmp + " of " + source);

							if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()){
								DebugDrawer.boundingBoxes.add(
										new DebugBoundingBox(minTmpOwn, maxTmpOwn, 1, 0, 0, 1));
								DebugDrawer.boundingBoxes.add(
										new DebugBoundingBox(minTmp, maxTmp, 0, 1, 0, 1));
								DebugDrawer.addArrowFromTransform(source.getWorldTransform(), new Vector4f(0.0f, 1.0f, 0.35f, 1.0f));
							}

							long tLeft = GrappleBeam.TIME_BEFORE_OUT_OF_BOUNDS - (self.getState().getUpdateTime() - grappleStart);
							((GameClientState) (self.getState())).getController()
									.popupAlertTextMessage("Grappled but not\nnear grappled object.\nTime until detach: " + StringTools.formatTimeFromMS(tLeft), "G", 0);
						}
					}
				} else {
					outOfSectorTime = 0;
					outOfBoundsTime = 0;
				}
			} else {
				outOfSectorTime = 0;
				outOfBoundsTime = 0;
			}

			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "GRAV[" + (source != null ? source.getUniqueIdentifier() + "(" + source.getId() + ")" : "NULL") + "; " + acceleration + "; central: "+central+"]";
	}

	public String accelToString(){
		return acceleration.toString();
	}

	public float magnitude(){
		return acceleration.length();
	}

	public float magnitudeSquared(){
		return acceleration.lengthSquared();
	}

	public Vector3f copyAccelerationTo(Vector3f out) { //in lieu of getAcceleration. Lack of encapsulation was making things difficult to debug with multiple contributors. ~Ithirahad
		out.set(acceleration);
		return out;
	}

	private Vector3f tmpIn = new Vector3f();
	public boolean accelerationEquals(float x, float y, float z){
		synchronized(tmpIn) {
			tmpIn.set(x, y, z);
			return acceleration.equals(tmpIn);
		}
	}

	public boolean accelerationEquals(Vector3f compare){
		return acceleration.equals(compare);
	}

	public void setAcceleration(Vector3f val){
		setAcceleration(val.x,val.y,val.z);
	}

	public void setAcceleration(float x, float y, float z){
		acceleration.set(x,y,z);
		if(magnitude() > G){
			System.out.println("[GRAVITY] !!! Set high gravity from " + source + ": " + magnitude());
		}
	}
}
