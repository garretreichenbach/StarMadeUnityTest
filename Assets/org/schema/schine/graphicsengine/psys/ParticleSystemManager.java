package org.schema.schine.graphicsengine.psys;

import java.awt.EventQueue;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.schine.graphicsengine.camera.CameraMouseState;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Transformable;
import org.schema.schine.network.client.ClientState;
import org.schema.schine.network.objects.container.TransformTimed;
import org.schema.schine.physics.Physics;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

public class ParticleSystemManager implements ISortableParticles {
	static Vector4f res = new Vector4f();
	public ObjectList<ParticleSystem> systems = new ObjectArrayList<ParticleSystem>();

	public void startParticleSystemWorld(ParticleSystemConfiguration config, Transform position) {

//		systems.clear();

		assert (position != null);
		ParticleSystem system = new ParticleSystem(config, new WorldTransformable(position));

		systems.add(system);

		system.start();
	}

	public void stopParticleSystemsWorld() {
		for (ParticleSystem system : systems) {
			system.stop();
		}

	}

	public void pauseParticleSystemsWorld() {
		for (ParticleSystem system : systems) {
			system.pause();
		}
	}

	public void update(Physics physics, Timer timer) {
		Iterator<ParticleSystem> it = systems.iterator();

		while (it.hasNext()) {
			ParticleSystem next = it.next();
			next.update(physics, timer);
			if (next.isCompletelyDead()) {
				it.remove();
			}
		}
	}

	public void draw() {

		Particle.quickSort(this);

		for (ParticleSystem s : systems) {
			s.draw();
		}
	}

	public void openGUI(final ClientState state) {
		EventQueue.invokeLater(() -> {
			try {
				ParticleSystemGUI frame = new ParticleSystemGUI(state);
//					frame.setLocationRelativeTo(null);
				CameraMouseState.ungrabForced = !CameraMouseState.ungrabForced;
				frame.setAlwaysOnTop(true);
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public float get(int index) {
		Vector3f position = systems.get(index).getWorldTransform().origin;
		Matrix4fTools.transform(Controller.modelviewMatrix, new Vector4f(position.x, position.y, position.z, 1.0F), res);
		return res.z;
	}

	@Override
	public void switchVal(int a, int b) {
		Collections.swap(systems, a, b);
	}

	@Override
	public int getSize() {
		return systems.size();
	}

	private class WorldTransformable implements Transformable {

		TransformTimed transform = new TransformTimed();

		public WorldTransformable(Transform transform) {
			super();
			this.transform.set(transform);
		}

		@Override
		public TransformTimed getWorldTransform() {
			return transform;
		}

	}
}
