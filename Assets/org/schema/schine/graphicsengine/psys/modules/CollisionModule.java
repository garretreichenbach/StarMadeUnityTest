package org.schema.schine.graphicsengine.psys.modules;

import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.vecmath.Vector3f;

import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.psys.ParticleContainer;
import org.schema.schine.graphicsengine.psys.ParticleSystemConfiguration;
import org.schema.schine.graphicsengine.psys.modules.iface.ParticleDeathInterface;
import org.schema.schine.graphicsengine.psys.modules.iface.ParticleMoveInterface;
import org.schema.schine.graphicsengine.psys.modules.iface.ParticleStartInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.DropDownInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.StringPair;
import org.schema.schine.graphicsengine.psys.modules.variable.VarInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.XMLSerializable;
import org.schema.schine.physics.ClosestRayCastResultExt;
import org.schema.schine.physics.Physics;

import com.bulletphysics.linearmath.Transform;

public class CollisionModule extends ParticleSystemModule implements ParticleMoveInterface, ParticleDeathInterface, ParticleStartInterface {

	private final Vector3f velocityTmp = new Vector3f();
	@XMLSerializable(name = "dampen", type = "float")
	float dampen;
	@XMLSerializable(name = "bounce", type = "float")
	float bounce;
	@XMLSerializable(name = "lifetimeLoss", type = "float")
	float lifetimeLoss;
	@XMLSerializable(name = "minKillSpeed", type = "float")
	float minKillSpeed;
	@XMLSerializable(name = "particleRadius", type = "float")
	float particleRadius;
	@XMLSerializable(name = "quality", type = "int")
	int quality = QUALITY_MID;

	public CollisionModule(ParticleSystemConfiguration sys) {
		super(sys);
	}

	@Override
	protected JPanel getConfigPanel() {
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		int i = 0;
		addRow(p, i++, new VarInterface<Float>() {

			@Override
			public String getName() {
				return "dampen";
			}

			@Override
			public Float get() {
				return dampen;
			}

			@Override
			public void set(String f) {
				dampen = Math.max(0, Math.min(1.0f, Float.parseFloat(f)));
			}

			@Override
			public Float getDefault() {
				return 1.0f;
			}

		});

		addRow(p, i++, new VarInterface<Float>() {

			@Override
			public String getName() {
				return "bounce";
			}

			@Override
			public Float get() {
				return bounce;
			}

			@Override
			public void set(String f) {
				bounce = Math.max(0, Math.min(1.0f, Float.parseFloat(f)));
			}

			@Override
			public Float getDefault() {
				return 1.0f;
			}

		});

		addRow(p, i++, new VarInterface<Float>() {

			@Override
			public String getName() {
				return "lifetime loss";
			}

			@Override
			public Float get() {
				return lifetimeLoss;
			}

			@Override
			public void set(String f) {
				lifetimeLoss = Math.max(0, Math.min(1.0f, Float.parseFloat(f)));
			}

			@Override
			public Float getDefault() {
				return 1.0f;
			}

		});

		addRow(p, i++, new VarInterface<Float>() {

			@Override
			public String getName() {
				return "Min kill speed";
			}

			@Override
			public Float get() {
				return minKillSpeed;
			}

			@Override
			public void set(String f) {
				minKillSpeed = Math.max(0, Math.min(1000.0f, Float.parseFloat(f)));
			}

			@Override
			public Float getDefault() {
				return 0.0f;
			}

		});

		addRow(p, i++, new VarInterface<Float>() {

			@Override
			public String getName() {
				return "Particle Radius";
			}

			@Override
			public Float get() {
				return particleRadius;
			}

			@Override
			public void set(String f) {
				particleRadius = Math.max(0, Math.min(1000.0f, Float.parseFloat(f)));
			}

			@Override
			public Float getDefault() {
				return 0.0f;
			}

		});

		addRow(p, i++, new DropDownInterface(new StringPair("low", QUALITY_LOW), new StringPair("middle", QUALITY_MID), new StringPair("high", QUALITY_HIGH)) {

			@Override
			public String getName() {
				return "Quality";
			}

			@Override
			public int getCurrentIndex() {
				return quality;
			}

			@Override
			public void set(StringPair selectedItem) {
				quality = selectedItem.val;
			}
		});
		return p;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.psys.modules.ParticleSystemModule#getName()
	 */
	@Override
	public String getName() {
		return "Collision";
	}

	@Override
	public void onParticleSpawn(ParticleContainer newParticle, Transform systemTransform) {

	}

	@Override
	public void onParticleMove(Physics physics, Timer timer, ParticleContainer particle) {
		Vector3f velocityDir = new Vector3f(particle.velocity);
		velocityDir.normalize();
		velocityTmp.set(particle.velocity);
		velocityTmp.scale(timer.getDelta());
		velocityTmp.add(particle.position);
		if (velocityTmp.lengthSquared() == particle.position.lengthSquared()) {
			return;
		}
		velocityDir.scale(particleRadius);
		velocityTmp.add(velocityDir);
		ClosestRayCastResultExt callback = new ClosestRayCastResultExt(particle.position, velocityTmp) {

			@Override
			public Object newInnerSegmentIterator() {
				return null;
			}
		};
		physics.getDynamicsWorld().rayTest(particle.position, velocityTmp, callback);
		if (callback.hasHit()) {
			Vector3f hitPointWorld = callback.hitPointWorld;
			hitPointWorld.sub(particle.position);
			float hitTime = hitPointWorld.length() / velocityTmp.length();
			//hitPointWorld.sub(velocityDir);
			hitPointWorld.scale(timer.getDelta());
			particle.position.add(hitPointWorld);

			Vector3f hitNormal = callback.hitNormalWorld;
			hitNormal.normalize();
			Vector3f leftoverVelocity = new Vector3f();
			leftoverVelocity.set(particle.velocity.x * (1.0F - hitTime), particle.velocity.y * (1.0F - hitTime), particle.velocity.z * (1.0F - hitTime));
			leftoverVelocity.scale(timer.getDelta());

			if (bounce > 0.0F)
			{
				// Calculate reflection vector
				float dDotN2 = leftoverVelocity.dot(hitNormal) * 2.0F;
				hitNormal.scale(dDotN2);
				leftoverVelocity.sub(hitNormal);
				leftoverVelocity.scale(1.0F / timer.getDelta());
				leftoverVelocity.scale(bounce);
				particle.velocity.set(leftoverVelocity);
			}
			else
			{
				// Project velocity on to normal
				hitNormal.scale(-1.0F);
				hitNormal.scale(leftoverVelocity.dot(hitNormal)); // Change normal to direction's length and normal's axis
				leftoverVelocity.sub(hitNormal);
				leftoverVelocity.scale(1.0F / timer.getDelta());
				leftoverVelocity.scale(dampen);
				particle.velocity.set(leftoverVelocity);
			}
			velocityTmp.set(particle.velocity);
			velocityTmp.scale(timer.getDelta());
			particle.position.add(velocityTmp);
		} else {
			velocityTmp.set(particle.velocity);
			velocityTmp.scale(timer.getDelta());
			particle.position.add(velocityTmp);
		}
	}

	@Override
	public void onParticleDeath(ParticleContainer particle) {

	}
}
