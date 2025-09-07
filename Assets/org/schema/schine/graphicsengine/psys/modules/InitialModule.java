package org.schema.schine.graphicsengine.psys.modules;

import java.awt.Color;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

import org.schema.common.util.linAlg.Quat4fTools;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.psys.ParticleContainer;
import org.schema.schine.graphicsengine.psys.ParticleSystemConfiguration;
import org.schema.schine.graphicsengine.psys.modules.iface.ParticleStartInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.BooleanInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.DropDownInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.PSCurveVariable;
import org.schema.schine.graphicsengine.psys.modules.variable.PSGradientVariable;
import org.schema.schine.graphicsengine.psys.modules.variable.StringPair;
import org.schema.schine.graphicsengine.psys.modules.variable.VarInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.XMLSerializable;

import com.bulletphysics.linearmath.Transform;

public class InitialModule extends ParticleSystemModule implements ParticleStartInterface {
	@XMLSerializable(name = "duration", type = "float")
	private float duration = 5.0f;            // 	The duration of the particle system in seconds (Read Only).
	private float div = 1.0f / duration; //readOnly
	@XMLSerializable(name = "emissionBurst", type = "int")
	private int emissionBurst = 0;            // 	The rate of emission.

	@XMLSerializable(name = "gravityModifier", type = "float")
	private float gravityModifier;        // 	Scale being applied to the gravity defined by Physics.gravity.

	@XMLSerializable(name = "loop", type = "boolean")
	private boolean loop = true;                // 	Is the particle system looping?

	@XMLSerializable(name = "maxParticles", type = "int")
	private int maxParticles = 1000;            // 	The maximum number of particles to emit.

	private int particleCount;            // 	The current number of particles (Read Only).

	@XMLSerializable(name = "playbackSpeed", type = "float")
	private float playbackSpeed = 1.0f;        // 	The playback speed of the particle system. 1 is normal playback speed.

	@XMLSerializable(name = "playOnAwake", type = "boolean")
	private boolean playOnAwake;            // 	If set to true, the particle system will automatically start playing on startup.

	@XMLSerializable(name = "randomSeed", type = "long")
	private long randomSeed;            // 	Random seed used for the particle system emission. If set to 0, it will be assigned a random value on awake.

	@XMLSerializable(name = "safeCollisionEventSize", type = "int")
	private int safeCollisionEventSize;    // 	Safe array size for use with ParticleSystem.GetCollisionEvents.

	@XMLSerializable(name = "simulationSpace", type = "int")
	private int simulationSpace;        // 	This selects the space in which to simulate particles. It can be either world or local space.

	@XMLSerializable(name = "startColor", type = "gradient")
	private PSGradientVariable startColor = new PSGradientVariable() {
		@Override
		public void init() {
			color.put(0, new Color(255, 255, 255, 200));
			color.put(1, new Color(255, 255, 255, 200));
		}

		@Override
		public String getName() {
			return "startColor";
		}
	};        // 	The initial color of particles when emitted.

	@XMLSerializable(name = "startDelay", type = "float")
	private float startDelay;            // 	Start delay in seconds.

	@XMLSerializable(name = "startLifetime", type = "float")
	private float startLifetime = 5.0f;            //	The total lifetime in seconds that particles will have when emitted. When using curves, this values acts as a scale on the curve. This value is set in the particle when it is create by the particle system.

	@XMLSerializable(name = "startRotationX", type = "curve")
	private PSCurveVariable startRotationX = new PSCurveVariable() {
		@Override
		public String getName() {
			return "startRotationX";
		}

		@Override
		public Color getColor() {
			return Color.RED;
		}
	};        // 	The initial rotation of particles when emitted. When using curves, this values acts as a scale on the curve.
	@XMLSerializable(name = "startRotationY", type = "curve")
	private PSCurveVariable startRotationY = new PSCurveVariable() {
		@Override
		public String getName() {
			return "startRotationY";
		}

		@Override
		public Color getColor() {
			return Color.GREEN;
		}
	};        // 	The initial rotation of particles when emitted. When using curves, this values acts as a scale on the curve.
	@XMLSerializable(name = "startRotationZ", type = "curve")
	private PSCurveVariable startRotationZ = new PSCurveVariable() {
		@Override
		public String getName() {
			return "startRotationZ";
		}

		@Override
		public Color getColor() {
			return Color.BLUE;
		}
	};        // 	The initial rotation of particles when emitted. When using curves, this values acts as a scale on the curve.

	@XMLSerializable(name = "startSizeX", type = "curve")
	private PSCurveVariable startSizeX = new PSCurveVariable() {
		@Override
		public String getName() {
			return "startSizeX";
		}

		@Override
		public Color getColor() {
			return Color.RED;
		}
	    /* (non-Javadoc)
         * @see org.schema.schine.graphicsengine.psys.modules.variable.PSCurveVariable#initPoints()
		 */

	};    // 	The initial size of particles when emitted. When using curves, this values acts as a scale on the curve.

	@XMLSerializable(name = "startSizeY", type = "curve")
	private PSCurveVariable startSizeY = new PSCurveVariable() {
		@Override
		public String getName() {
			return "startSizeY";
		}

		@Override
		public Color getColor() {
			return Color.GREEN;
		}
	};    // 	The initial size of particles when emitted. When using curves, this values acts as a scale on the curve.

	@XMLSerializable(name = "startSizeZ", type = "curve")
	private PSCurveVariable startSizeZ = new PSCurveVariable() {
		@Override
		public String getName() {
			return "startSizeZ";
		}

		@Override
		public Color getColor() {
			return Color.BLUE;
		}
	};    // 	The initial size of particles when emitted. When using curves, this values acts as a scale on the curve.

	@XMLSerializable(name = "startSpeedMin", type = "float")
	private float startSpeedMin = 1.0F; // Minimum initial speed when emitted

	@XMLSerializable(name = "startSpeedMax", type = "float")
	private float startSpeedMax = 1.0F; // Maximum initial speed when emitted

	@XMLSerializable(name = "initialSpeedType", type = "int")
	private int initialSpeedType;        // 	This determines whether initial velocity is per-axis or absolute

	private float time;                    // 	Playback position in seconds.
	private float dist = 0;
	private Transform transTmp = new Transform();
	public static final int INITIAL_SPEED_PER_AXIS = 0;
	public static final int INITIAL_SPEED_ABSOLUTE = 1;

	public InitialModule(ParticleSystemConfiguration sys) {
		super(sys);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.psys.modules.ParticleSystemModule#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		//always enabled
		return true;
	}

	@Override
	protected JPanel getConfigPanel() {
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		int i = 0;

		addRow(p, i++, new VarInterface<Float>() {

			@Override
			public String getName() {
				return "duration";
			}

			@Override
			public Float get() {
				return duration;
			}

			@Override
			public void set(String f) {
				duration = Math.max(0, Float.parseFloat(f));
				div = duration > 0 ? 1.0f / duration : 0.00001f;
			}

			@Override
			public Float getDefault() {
				return 1.0f;
			}
		});

		addRow(p, i++, new VarInterface<Integer>() {
			@Override
			public String getName() {
				return "initial emission burst";
			}

			@Override
			public Integer get() {
				return emissionBurst;
			}

			@Override
			public void set(String f) {
				emissionBurst = Math.max(0, Integer.parseInt(f));
			}

			@Override
			public Integer getDefault() {
				return 0;
			}
		});

		addRow(p, i++, new VarInterface<Float>() {
			@Override
			public String getName() {
				return "gravity modifier";
			}

			@Override
			public Float get() {
				return gravityModifier;
			}

			@Override
			public void set(String f) {
				gravityModifier = Math.max(0, Float.parseFloat(f));
			}

			@Override
			public Float getDefault() {
				return 1.0f;
			}
		});

		addRow(p, i++, new BooleanInterface() {
			@Override
			public boolean get() {
				return loop;
			}

			@Override
			public void set(boolean selected) {
				loop = selected;
			}

			@Override
			public String getName() {
				return "loop";
			}

		});

		addRow(p, i++, new VarInterface<Integer>() {
			@Override
			public String getName() {
				return "max particles";
			}

			@Override
			public Integer get() {
				return maxParticles;
			}

			@Override
			public void set(String f) {
				maxParticles = Math.max(0, Integer.parseInt(f));
			}

			@Override
			public Integer getDefault() {
				return 100;
			}
		});

		addRow(p, i++, new VarInterface<Float>() {
			@Override
			public String getName() {
				return "playback speed";
			}

			@Override
			public Float get() {
				return playbackSpeed;
			}

			@Override
			public void set(String f) {
				playbackSpeed = Math.max(0, Float.parseFloat(f));
			}

			@Override
			public Float getDefault() {
				return 1.0f;
			}
		});

		addRow(p, i++, new BooleanInterface() {
			@Override
			public void set(boolean selected) {
				playOnAwake = selected;
			}

			@Override
			public String getName() {
				return "play on awake";
			}

			@Override
			public boolean get() {
				return playOnAwake;
			}
		});

		addRow(p, i++, new VarInterface<Long>() {
			@Override
			public String getName() {
				return "random seed";
			}

			@Override
			public Long get() {
				return randomSeed;
			}

			@Override
			public void set(String f) {
				randomSeed = Math.max(0, Long.parseLong(f));
			}

			@Override
			public Long getDefault() {
				return 1L;
			}
		});

		addRow(p, i++, new VarInterface<Integer>() {
			@Override
			public String getName() {
				return "safe collision event size";
			}

			@Override
			public Integer get() {
				return safeCollisionEventSize;
			}

			@Override
			public void set(String f) {
				safeCollisionEventSize = Math.max(1, Integer.parseInt(f));
			}

			@Override
			public Integer getDefault() {
				return 100;
			}
		});

		addRow(p, i++, new DropDownInterface(new StringPair("world", SPACE_WORLD), new StringPair("local", SPACE_LOCAL)) {
			@Override
			public String getName() {
				return "Simulation Space";
			}

			@Override
			public int getCurrentIndex() {
				return simulationSpace;
			}

			@Override
			public void set(StringPair selectedItem) {
				simulationSpace = selectedItem.val;
			}
		});

		addRow(p, i++, startColor);

		addRow(p, i++, new VarInterface<Float>() {
			@Override
			public String getName() {
				return "start delay";
			}

			@Override
			public Float get() {
				return startDelay;
			}

			@Override
			public void set(String f) {
				startDelay = Math.max(0, Float.parseFloat(f));
			}

			@Override
			public Float getDefault() {
				return 0.0f;
			}
		});

		addRow(p, i++, new VarInterface<Float>() {
			@Override
			public String getName() {
				return "start lifetime";
			}

			@Override
			public Float get() {
				return startLifetime;
			}

			@Override
			public void set(String f) {
				startLifetime = Math.max(1, Integer.parseInt(f));
			}

			@Override
			public Float getDefault() {
				return 100f;
			}
		});

		//startRotation
		addRow(p, i++, startRotationX, startRotationY, startRotationZ);

		//startSize
		addRow(p, i++, startSizeX, startSizeY, startSizeZ);

		//startSpeedMin
		addRow(p, i++, new VarInterface<Float>() {
			@Override
			public String getName() {
				return "Start Speed Minimum";
			}

			@Override
			public Float get() {
				return startSpeedMin;
			}

			@Override
			public void set(String f) {
				startSpeedMin = Math.abs(Math.min(startSpeedMax, Float.parseFloat(f)));
			}

			@Override
			public Float getDefault() {
				return 1.0f;
			}
		});

		//startSpeedMax
		addRow(p, i++, new VarInterface<Float>() {
			@Override
			public String getName() {
				return "Start Speed Maximum";
			}

			@Override
			public Float get() {
				return startSpeedMax;
			}

			@Override
			public void set(String f) {
				startSpeedMax = Math.abs(Math.max(startSpeedMin, Float.parseFloat(f)));
			}

			@Override
			public Float getDefault() {
				return 1.0f;
			}
		});

		addRow(p, i++, new DropDownInterface(new StringPair("per_axis", INITIAL_SPEED_PER_AXIS), new StringPair("absolute", INITIAL_SPEED_ABSOLUTE)) {
			@Override
			public String getName() {
				return "Initial Speed Behaviour";
			}

			@Override
			public int getCurrentIndex() {
				return initialSpeedType;
			}

			@Override
			public void set(StringPair selectedItem) {
				initialSpeedType = selectedItem.val;
			}
		});

		return p;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.psys.modules.ParticleSystemModule#getName()
	 */
	@Override
	public String getName() {
		return "Initial";
	}

	@Override
	public boolean canDisable() {
		return false;
	}

	public int getMaxParticles() {
		return maxParticles;
	}

	public int getParticleCount() {
		return particleCount;
	}

	public void setParticleCount(int i) {
		particleCount = i;
	}

	public void updateTimes(Timer timer) {
		if (time + timer.getDelta() * playbackSpeed > duration) {
			if (loop) {
				time = -(int) (time / duration);
			} else {
				return;
			}
		} else {
			time += timer.getDelta() * playbackSpeed;
		}
		dist += timer.getDelta() * playbackSpeed;
	}

	public int getParticlesToSpawn() {

		if (this.time <= 0.0F) {
			return emissionBurst;
		}

		return 0;
	}

	public void start() {
		this.time = 0.0F;
	}

	@Override
	public void onParticleSpawn(ParticleContainer newParticle, Transform systemTransform) {

		newParticle.lifetime = startLifetime;
		newParticle.lifetimeTotal = startLifetime;

		float time = this.time * div;

		newParticle.position.set(systemTransform.origin);

//		transTmp.set(systemTransform);
//		rotTmp.rotX(startRotationX.get(time));
//		transTmp.basis.mul(rotTmp);
//		rotTmp.rotY(startRotationY.get(time));
//		transTmp.basis.mul(rotTmp);
//		rotTmp.rotZ(startRotationZ.get(time));
//		transTmp.basis.mul(rotTmp);

		Quat4fTools.set(transTmp.basis, newParticle.rotation);
		newParticle.rotation.x = 0.0F;
		newParticle.rotation.y = 0.0F;
		newParticle.rotation.z = 0.0F;
		newParticle.rotation.w = 1.0F;

		newParticle.size.set(startSizeX.get(time), startSizeY.get(time), startSizeZ.get(time));

		float minSpeed = Math.min(startSpeedMin, startSpeedMax);
		float maxSpeed = Math.max(startSpeedMin, startSpeedMax);
		float speed = minSpeed + sys.getRandom().nextFloat() * (maxSpeed - minSpeed);
		float perAxis = initialSpeedType == INITIAL_SPEED_PER_AXIS ? speed * 2.0F : 1.0F;
		newParticle.velocity.set(
				(sys.getRandom().nextFloat() - 0.5f) * perAxis,
				(sys.getRandom().nextFloat() - 0.5f) * perAxis,
				(sys.getRandom().nextFloat() - 0.5f) * perAxis);
		if (initialSpeedType == INITIAL_SPEED_ABSOLUTE)
		{
			newParticle.velocity.normalize();
			newParticle.velocity.scale(speed);
		}

		newParticle.color.set(startColor.get(time));

//		System.err.println("PARTICLE VELOCITY IS NOW: "+newParticle.velocity);
	}

	public float getParticleLifetimeDiv() {
		return div;
	}

	public float getParticleSystemDuration() {
		return duration;
	}

	/**
	 * @return the duration
	 */
	public float getDuration() {
		return duration;
	}

	/**
	 * @param duration the duration to set
	 */
	public void setDuration(float duration) {
		this.duration = duration;
	}

	/**
	 * @return the randomSeed
	 */
	public long getRandomSeed() {
		return randomSeed;
	}

	public float getTime() {
		return time;
	}

}
