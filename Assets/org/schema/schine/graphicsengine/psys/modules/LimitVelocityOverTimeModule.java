package org.schema.schine.graphicsengine.psys.modules;

import java.awt.Color;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.psys.ParticleContainer;
import org.schema.schine.graphicsengine.psys.ParticleSystemConfiguration;
import org.schema.schine.graphicsengine.psys.modules.iface.ParticleUpdateInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.PSCurveVariable;
import org.schema.schine.graphicsengine.psys.modules.variable.VarInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.XMLSerializable;

public class LimitVelocityOverTimeModule extends ParticleSystemModule implements ParticleUpdateInterface {

	@XMLSerializable(name = "multiplierX", type = "float")
	float multiplierX = 1.0F;
	@XMLSerializable(name = "multiplierY", type = "float")
	float multiplierY = 1.0F;
	@XMLSerializable(name = "multiplierZ", type = "float")
	float multiplierZ = 1.0F;
	@XMLSerializable(name = "x", type = "curve")
	PSCurveVariable x = new PSCurveVariable() {
		@Override
		public String getName() {
			return "X";
		}

		@Override
		public Color getColor() {
			return Color.RED;
		}
	};
	@XMLSerializable(name = "y", type = "curve")
	PSCurveVariable y = new PSCurveVariable() {
		@Override
		public String getName() {
			return "Y";
		}

		@Override
		public Color getColor() {
			return Color.GREEN;
		}
	};
	@XMLSerializable(name = "z", type = "curve")
	PSCurveVariable z = new PSCurveVariable() {
		@Override
		public String getName() {
			return "Z";
		}

		@Override
		public Color getColor() {
			return Color.BLUE;
		}
	};
	@XMLSerializable(name = "dampen", type = "float")
	float dampen = 1;

	public LimitVelocityOverTimeModule(ParticleSystemConfiguration sys) {
		super(sys);
	}

	@Override
	protected JPanel getConfigPanel() {
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		int i = 0;
		addRow(p, i++, x, y, z);

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
				return "multiplierX";
			}

			@Override
			public Float get() {
				return multiplierX;
			}

			@Override
			public void set(String f) {
				multiplierX = Float.parseFloat(f);
			}

			@Override
			public Float getDefault() {
				return 1.0f;
			}

		});
		addRow(p, i++, new VarInterface<Float>() {

			@Override
			public String getName() {
				return "multiplierY";
			}

			@Override
			public Float get() {
				return multiplierY;
			}

			@Override
			public void set(String f) {
				multiplierY = Float.parseFloat(f);
			}

			@Override
			public Float getDefault() {
				return 1.0f;
			}

		});
		addRow(p, i++, new VarInterface<Float>() {

			@Override
			public String getName() {
				return "multiplierZ";
			}

			@Override
			public Float get() {
				return multiplierZ;
			}

			@Override
			public void set(String f) {
				multiplierZ = Float.parseFloat(f);
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
		return "Limit Velocity over Lifetime";
	}

	@Override
	public void onParticleUpdate(Timer timer, ParticleContainer p) {
		float maxVelX = multiplierX * Math.abs(x.get(1.0f - p.lifetime / p.lifetimeTotal));
		float maxVelY = multiplierY * Math.abs(y.get(1.0f - p.lifetime / p.lifetimeTotal));
		float maxVelZ = multiplierZ * Math.abs(z.get(1.0f - p.lifetime / p.lifetimeTotal));
		if (Math.abs(p.velocity.x) > maxVelX)
		{
			p.velocity.x = maxVelX;
		}
		if (Math.abs(p.velocity.y) > maxVelY)
		{
			p.velocity.y = maxVelY;
		}
		if (Math.abs(p.velocity.z) > maxVelZ)
		{
			p.velocity.z = maxVelZ;
		}
	}
}
