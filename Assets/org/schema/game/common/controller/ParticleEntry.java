package org.schema.game.common.controller;

import java.io.File;

import javax.vecmath.Vector3f;

import org.schema.game.client.data.GameClientState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.psys.ParticleSystemConfiguration;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.FileExt;

import com.bulletphysics.linearmath.Transform;

public class ParticleEntry {

	private String particleName;
	private Vector3f origin;

	public ParticleEntry() {
		super();
	}

	public ParticleEntry(String name, Vector3f origin) {
		super();
		this.particleName = name;
		this.origin = origin;
	}

	public void handleClient(GameClientState state) {
		try {
			Transform t = new Transform();
			t.origin.set(this.origin);
			File file = new FileExt("./data/effects/particles/" + particleName + ".xml");
			if (!file.exists()) {
				state.getController().popupAlertTextMessage(Lng.str("Particle effect\n\"%s\"\nmissing!",  particleName), ServerMessage.MESSAGE_TYPE_ERROR);
			} else {
				state.getParticleSystemManager().startParticleSystemWorld(ParticleSystemConfiguration.fromFile(file, false), t);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Vector3f getOrigin() {
		return origin;
	}

	public void setOrigin(Vector3f origin) {
		this.origin = origin;
	}

	public String getParticleName() {
		return particleName;
	}

	public void setParticleName(String particleName) {
		this.particleName = particleName;
	}
}
