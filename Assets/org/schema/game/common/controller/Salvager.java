package org.schema.game.common.controller;

import java.util.Collection;

import javax.vecmath.Vector3f;

import org.schema.game.common.controller.elements.BeamState;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.world.Segment;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Transformable;
import org.schema.schine.physics.Physical;

public interface Salvager extends Physical, Transformable {

	/**
	 * @param hittingBeam
	 * @param updatedSegments
	 * @return the harvesterController
	 */
	public int handleSalvage(BeamState hittingBeam, int hits, BeamHandlerContainer<?> container, Vector3f from, SegmentPiece hitPiece, Timer timer, Collection<Segment> updatedSegments);

	public int getFactionId();

	public byte getFactionRights();

	public byte getOwnerFactionRights();

	public AbstractOwnerState getOwnerState();
}
