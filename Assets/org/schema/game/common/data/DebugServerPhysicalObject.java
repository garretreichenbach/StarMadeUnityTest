package org.schema.game.common.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

import org.lwjgl.glfw.GLFW;
import org.schema.common.util.linAlg.TransformTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.effects.TimedIndication;
import org.schema.game.client.view.gui.shiphud.HudIndicatorOverlay;
import org.schema.game.common.data.physics.RigidBodySegmentController;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.world.GameTransformable;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.game.common.data.world.SectorInformation.SectorType;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.TransformaleObjectTmpVars;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.graphicsengine.forms.DebugBox;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;
import org.schema.schine.input.Keyboard;
import org.schema.schine.network.objects.Sendable;

import com.bulletphysics.linearmath.Transform;

public class DebugServerPhysicalObject extends DebugServerObject {

	private static final TransformaleObjectTmpVars v = new TransformaleObjectTmpVars();
	public final BoundingBox bb = new BoundingBox();
	public String name;
	public Transform serverTransform = new Transform();
	public SimpleTransformableSendableObject objOnServer;
	int sector = -1;

	public void setObject(GameTransformable objOnServer) {
		serverTransform.set(objOnServer.getWorldTransform());
		if (objOnServer instanceof AbstractCharacter<?>) {
			bb.min.set(-1, -1, -1);
			bb.max.set(1, 1, 1);
		} else {
			objOnServer.getGravityAABB(serverTransform, bb.min, bb.max);
		}
		name = "[ByObj]" + objOnServer.toString();
		sector = (objOnServer.getSectorId());
	}

	public void setObject(RigidBodySegmentController objOnServer, int sector) {
		objOnServer.getWorldTransform(serverTransform);
		objOnServer.getCollisionShape().getAabb(TransformTools.ident, bb.min, bb.max);
		name = "[ByPhy]" + objOnServer.toString();
		this.sector = (sector);
	}

	@Override
	public byte getType() {
		return PHYSICAL;
	}

	@Override
	public void draw(GameClientState state) {
		if (EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn() && EngineSettings.N_TRANSMIT_RAW_DEBUG_POSITIONS.isOn()) {

			Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(sector);

			if (sendable != null && sendable instanceof RemoteSector) {
				RemoteSector o = (RemoteSector) sendable;
				boolean fromPlanet = state.getCurrentRemoteSector().getType() == SectorType.PLANET &&
						o.getType() != SectorType.PLANET;

				boolean toPlanet = state.getCurrentRemoteSector().getType() != SectorType.PLANET &&
						o.getType() == SectorType.PLANET;

				Transform t = new Transform();
				SimpleTransformableSendableObject.calcWorldTransformRelative(state, state.getCurrentRemoteSector().clientPos(), o.clientPos(),
						fromPlanet, toPlanet,
						serverTransform, t, v);

				DebugBox dbb = null;

				if (!name.contains("Asteroid") || Keyboard.isKeyDown(GLFW.GLFW_KEY_3)) {
					if (name.toLowerCase(Locale.ENGLISH).contains("virt")) {
						if (!Keyboard.isKeyDown(GLFW.GLFW_KEY_1)) {
							dbb = new DebugBox(bb.min, bb.max, t, 1.0f, 0.1f, 0.3f, 1.0f);
						}
					} else {
						if (!Keyboard.isKeyDown(GLFW.GLFW_KEY_2)) {
							bb.min.x -= 1;
							bb.min.y -= 1;
							bb.min.z -= 1;
							bb.max.x += 1;
							bb.max.y += 1;
							bb.max.z += 1;
							dbb = new DebugBox(bb.min, bb.max, t, 0.8f, 0.4f, 0.7f, 1.0f);

						}
					}
				}
				if (dbb != null) {
					DebugDrawer.boxes.add(dbb);
					if (name.length() == 0) {
						name = "zeroLength";
					}
					TimedIndication timedIndication = new TimedIndication(t, o.clientPos() + name, 0.2f, 10000);
					HudIndicatorOverlay.toDrawTexts.add(timedIndication);
				}

			}
		}
	}

	@Override
	public void serialize(DataOutputStream stream) throws IOException {
		stream.writeUTF(name);
		bb.serialize(stream);
		stream.writeInt(sector);
		TransformTools.serializeFully(stream, serverTransform);
	}

	@Override
	public void deserialize(DataInputStream stream) throws IOException {
		name = stream.readUTF();
		bb.deserialize(stream);
		sector = stream.readInt();
		TransformTools.deserializeFully(stream, serverTransform);
	}

}
