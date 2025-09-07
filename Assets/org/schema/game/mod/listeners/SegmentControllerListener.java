package org.schema.game.mod.listeners;

import org.schema.game.common.controller.SegmentController;

/**
 * SegmentControllers are block structures (ships, stations, planets, etc)
 *
 * @author schema
 */
public interface SegmentControllerListener extends ModListener {

	public void onSegmentControllerUpdate(SegmentController c);

	public void onSegmentControllerSpawn(SegmentController c);

	public void onSegmentControllerDelete(SegmentController c);

	public void onSegmentControllerDamageTaken(SegmentController c);

	public void onSegmentControllerHitByLaser(SegmentController c);

	public void onSegmentControllerHitByBeam(SegmentController c);

	public void onSegmentControllerHitByPulse(SegmentController c);

	public void onSegmentControllerHitByMissile(SegmentController c);

	public void onSegmentControllerPlayerAttached(SegmentController c);

	public void onSegmentControllerPlayerDetached(SegmentController c);

	public void onSegmentControllerDocking(SegmentController c);

	public void onSegmentControllerUndocking(SegmentController c);

	public void onSegmentControllerDestroyedPermanently(SegmentController s);
}
