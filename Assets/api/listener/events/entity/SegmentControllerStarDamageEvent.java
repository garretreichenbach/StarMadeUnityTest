package api.listener.events.entity;

import api.listener.events.Event;
import com.bulletphysics.linearmath.Transform;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.EditableSendableSegmentController;

/**
 * [Description]
 *
 * @author TheDerpGamer (MrGoose#0027)
 */
public class SegmentControllerStarDamageEvent extends Event {

	private final EditableSendableSegmentController segmentController;
	private final Vector3i sector;
	private float damage;
	private Transform where;
	private float radius;

	public SegmentControllerStarDamageEvent(EditableSendableSegmentController segmentController, Vector3i sector, float damage, Transform where, float radius) {
		this.segmentController = segmentController;
		this.sector = sector;
		this.damage = damage;
		this.where = where;
		this.radius = radius;
	}

	public EditableSendableSegmentController getSegmentController() {
		return segmentController;
	}

	public Vector3i getSector() {
		return sector;
	}

	public float getDamage() {
		return damage;
	}

	public void setDamage(float damage) {
		this.damage = damage;
	}

	public Transform getWhere() {
		return where;
	}

	public void setWhere(Transform where) {
		this.where = where;
	}

	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}
}
