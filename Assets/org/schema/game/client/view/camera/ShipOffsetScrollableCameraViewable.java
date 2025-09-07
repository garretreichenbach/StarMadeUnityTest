package org.schema.game.client.view.camera;

import java.util.List;

import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.ingame.EditSegmentInterface;
import org.schema.schine.common.InputHandler;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;

public class ShipOffsetScrollableCameraViewable extends ShipOffsetCameraViewable implements InputHandler {

	Vector3i segIndex = new Vector3i();
	Vector3b elemIndex = new Vector3b();

	public ShipOffsetScrollableCameraViewable(EditSegmentInterface controller) {
		super(controller);
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		Vector3i t = new Vector3i(posMod);
		t.add(edit.getCore()); //sub start
		if (!controller.getSegmentBuffer().existsPointUnsave(t)) {
			while (posMod.length() > 0 && !controller.getSegmentBuffer().existsPointUnsave(t)) {
				posMod.x /= 2;
				posMod.y /= 2;
				posMod.z /= 2;
				t.set(posMod);
				t.add(edit.getCore());
				System.err.println("[CAM] SEARCHING anchor");
			}
		} else {
			boolean hit = true;
			Vector3f f = new Vector3f(t.x, t.y, t.z);
			
			final List<KeyboardMappings> mappings = e.getTriggeredMappings();
			for(KeyboardMappings m : mappings) {
				switch(m) {
					case STRAFE_LEFT -> f.sub(controller.getCamLeftLocal());
					case STRAFE_RIGHT -> f.add(controller.getCamLeftLocal());
					case FORWARD -> f.add(controller.getCamForwLocal());
					case BACKWARDS -> f.sub(controller.getCamForwLocal());
					case UP -> f.add(controller.getCamUpLocal());
					case DOWN -> f.sub(controller.getCamUpLocal());
					default -> hit = false;
				}
					
				if(e.isLeftShift()) {
					hit = false;
				}
				
				t.set(Math.round(f.x), FastMath.round(f.y), FastMath.round(f.z));
				if (hit) {
					if (controller.getSegmentBuffer().existsPointUnsave(t)) {
						t.sub(edit.getCore());
						System.err.println("EXISTS!. pos mod set to " + t);
						posMod.set(t);
					} else {
						System.err.println("NOT EXISTS!. pos mod NOT set to " + t);
					}
				}
			}
		}

	}


}
