package org.schema.game.common.controller;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.elements.BeamState;
import org.schema.game.common.controller.elements.beam.repair.RepairBeamHandler;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.forms.Transformable;
import org.schema.schine.physics.Physical;

public interface Salvage extends Physical, Transformable {

	public void handleBeingSalvaged(BeamState hittingBeam, BeamHandlerContainer<? extends SimpleTransformableSendableObject> container, Vector3f to, SegmentPiece hitPiece, int beamHitsForReal);

	public boolean isRepariableFor(RepairBeamHandler repairBeamHandler,
	                               String[] cannotHitReason, Vector3i position);

	public boolean isSalvagableFor(Salvager harvester, String[] cannotHitReason, Vector3i position);
}
