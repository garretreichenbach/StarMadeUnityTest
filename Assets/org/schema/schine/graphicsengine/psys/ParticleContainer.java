package org.schema.schine.graphicsengine.psys;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

public class ParticleContainer {
	public final Vector3f angularVelocity = new Vector3f(); // 	The angular velocity of the particle.
	public final Vector4f color = new Vector4f(1, 1, 1, 1); // 	The initial color of the particle. The current color of the particle is calculated procedurally based on this value and the active color modules.
	public final Vector3f position = new Vector3f();        // 	The position of the particle.
	public final Quat4f rotation = new Quat4f(0, 0, 0, 1);  // 	The rotation of the particle.
	public final Vector3f size = new Vector3f(1, 1, 1);     // 	The initial size of the particle. The current size of the particle is calculated procedurally based on this value and the active size modules.
	public final Vector3f velocity = new Vector3f();        // 	The velocity of the particle.
	public float lifetime;                                  // 	The lifetime of the particle.
	public float lifetimeTotal;                             // 	The total life length of tha particle.
	public float randomSeed;                                // 	The random seed of the particle.
	public float camDist = 0;                               // 	The velocity of the particle.
	float startLifetime;                                    // 	The starting lifetime of the particle.

	public void reset() {
		angularVelocity.set(0, 0, 0);
		color.set(1, 1, 1, 1);
		lifetime = 0;
		lifetimeTotal = 0;
		position.set(0, 0, 0);
		randomSeed = (0);
		rotation.set(0, 0, 0, 1);
		size.set(1, 1, 1);
		startLifetime = 0;
		velocity.set(0, 0, 0);
		camDist = 0;
	}
}
