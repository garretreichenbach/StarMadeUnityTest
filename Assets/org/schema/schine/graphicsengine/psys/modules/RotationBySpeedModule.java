package org.schema.schine.graphicsengine.psys.modules;

import java.awt.Color;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Quat4Util;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.psys.ParticleContainer;
import org.schema.schine.graphicsengine.psys.ParticleSystemConfiguration;
import org.schema.schine.graphicsengine.psys.modules.iface.ParticleUpdateInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.PSCurveVariable;
import org.schema.schine.graphicsengine.psys.modules.variable.VarInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.XMLSerializable;

public class RotationBySpeedModule extends ParticleSystemModule implements ParticleUpdateInterface {

	@XMLSerializable(name = "rotation", type = "curve")
	PSCurveVariable rotation = new PSCurveVariable() {
		@Override
		public String getName() {
			return "rotation";
		}

		@Override
		public Color getColor() {
			return Color.RED;
		}
	};
	@XMLSerializable(name = "minSpeed", type = "float")
	float minSpeed = 0;
	@XMLSerializable(name = "maxSpeed", type = "float")
	float maxSpeed = 1;
	@XMLSerializable(name = "rotMult", type = "float")
	float multiplier = 0.1F;

	public RotationBySpeedModule(ParticleSystemConfiguration sys) {
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
				return "multiplier";
			}

			@Override
			public Float get() {
				return multiplier;
			}

			@Override
			public void set(String f) {
				multiplier = Float.parseFloat(f);
			}

			@Override
			public Float getDefault() {
				return 0.1F;
			}

		});

		addRow(p, i++, rotation);

		addRow(p, i++, new VarInterface<Float>() {

			@Override
			public String getName() {
				return "min speed";
			}

			@Override
			public Float get() {
				return minSpeed;
			}

			@Override
			public void set(String f) {
				minSpeed = Math.max(0, Math.min(1.0f, Float.parseFloat(f)));
			}

			@Override
			public Float getDefault() {
				return 1.0f;
			}

		});
		addRow(p, i++, new VarInterface<Float>() {

			@Override
			public String getName() {
				return "max speed";
			}

			@Override
			public Float get() {
				return maxSpeed;
			}

			@Override
			public void set(String f) {
				maxSpeed = Math.max(0, Math.min(1.0f, Float.parseFloat(f)));
			}

			@Override
			public Float getDefault() {
				return 1.0f;
			}

		});
		return p;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.psys.modules.ParticleSystemModule#getName()
	 */
	@Override
	public String getName() {
		return "Rotation by Speed";
	}

	@Override
	public void onParticleUpdate(Timer timer, ParticleContainer p) {
		float vel = Math.min(maxSpeed, Math.max(minSpeed, p.velocity.length())) / (maxSpeed - minSpeed);
		Quat4f q = Quat4Util.fromAngleAxis(rotation.get(vel) * multiplier, new Vector3f(0, 0, 1), new Quat4f());
		Quat4Util.mult(q, p.rotation, p.rotation);
	}
}
