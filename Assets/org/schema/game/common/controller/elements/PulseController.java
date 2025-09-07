package org.schema.game.common.controller.elements;

import api.listener.events.weapon.PulseAddEvent;
import api.mod.StarLoader;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.schema.game.client.view.GameResourceLoader;
import org.schema.game.common.controller.SegmentController;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.server.ServerStateInterface;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class PulseController {

	private final StateInterface state;
	private final ObjectArrayList<Pulse> pulses = new ObjectArrayList<Pulse>();
	float time = 0;
	private int sectorId;

	public PulseController(StateInterface state, int sectorId) {
		this.state = state;
		this.sectorId = sectorId;
	}
	//INSERTED CODE @???
	public ObjectArrayList<Pulse> getPulses() {
		return pulses;
	}
	///
	public void addDamagePulse(Transform location, Vector3f dir, SegmentController owner, float force, float radius, long weaponId, Vector4f pulseColor) {
		addPulse(Pulse.TYPE_DAMAGE, location, dir, owner, force, radius, weaponId, pulseColor);
	}

	public void addPushPulse(Transform location, Vector3f dir, SegmentController owner, float force, float radius, long weaponId, Vector4f pulseColor) {
		addPulse(Pulse.TYPE_PUSH, location, dir, owner, force, radius, weaponId, pulseColor);
	}

	private void addPulse(byte pulseType, Transform location, Vector3f dir, SegmentController owner, float force, float radius,long weaponId, Vector4f pulseColor) {
		Pulse pulse = new Pulse(state, pulseType, location, owner, dir, owner, force, radius, sectorId, weaponId, pulseColor);
		//INSERTED CODE
		PulseAddEvent event = new PulseAddEvent(pulse);
		StarLoader.fireEvent(event, state instanceof ServerStateInterface);
		pulse = event.getPulse();
		///
		pulses.add(pulse);
	}

	public void draw() {
		if (!pulses.isEmpty()) {
			Mesh mesh = (Mesh) Controller.getResLoader().getMesh("Sphere").getChilds().iterator().next();
			mesh.loadVBO(true);
			GlUtil.glEnable(GL11.GL_BLEND);

			GlUtil.glDisable(GL11.GL_CULL_FACE);
			GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			ShaderLibrary.pulseShader.loadWithoutUpdate();

			GlUtil.updateShaderFloat(ShaderLibrary.pulseShader, "m_Time", time);
			GlUtil.updateShaderFloat(ShaderLibrary.pulseShader, "m_TexCoordMult", 5);

			GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, Controller.getResLoader().getSprite("energy_tex").getMaterial().getTexture().getTextureId());
			GlUtil.updateShaderInt(ShaderLibrary.pulseShader, "m_ShieldTex", 0);

			GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, GameResourceLoader.effectTextures[0].getTextureId());
			GlUtil.updateShaderInt(ShaderLibrary.pulseShader, "m_Distortion", 1);

			GlUtil.glActiveTexture(GL13.GL_TEXTURE2);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, GameResourceLoader.effectTextures[1].getTextureId());
			GlUtil.updateShaderInt(ShaderLibrary.pulseShader, "m_Noise", 2);
			for (int i = 0; i < pulses.size(); i++) {

				pulses.get(i).draw(mesh);
			}

			ShaderLibrary.pulseShader.unloadWithoutExit();
			mesh.unloadVBO(true);

			GlUtil.glActiveTexture(GL13.GL_TEXTURE2);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			GlUtil.glDisable(GL11.GL_BLEND);
			GlUtil.glEnable(GL11.GL_CULL_FACE);

		}
	}

	/**
	 * @return the sectorId
	 */
	public int getSectorId() {
		return sectorId;
	}

	/**
	 * @param sectorId the sectorId to set
	 */
	public void setSectorId(int sectorId) {
		this.sectorId = sectorId;
	}

	public void update(Timer timer) {
		time += timer.getDelta();
		for (int i = 0; i < pulses.size(); i++) {
			pulses.get(i).update(timer);
			if (!pulses.get(i).isActive()) {
				pulses.remove(i);
				i--;
			}
		}
	}

}
