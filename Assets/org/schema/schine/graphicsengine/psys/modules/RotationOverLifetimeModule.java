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

public class RotationOverLifetimeModule extends ParticleSystemModule implements ParticleUpdateInterface {

	@XMLSerializable(name = "rotation", type = "float")
	float rotation = 0.1F;
	@XMLSerializable(name = "rotCurve", type = "curve")
	PSCurveVariable rotCurve = new PSCurveVariable() {
		@Override
		public String getName() {
			return "rotationCurve";
		}

		@Override
		public Color getColor() {
			return Color.RED;
		}
	};

	public RotationOverLifetimeModule(ParticleSystemConfiguration sys) {
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
				return rotation;
			}

			@Override
			public void set(String f) {
				rotation = Float.parseFloat(f);
			}

			@Override
			public Float getDefault() {
				return 0.1F;
			}

		});
		addRow(p, i++, rotCurve);

		return p;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.psys.modules.ParticleSystemModule#getName()
	 */
	@Override
	public String getName() {
		return "Rotation over Lifetime";
	}

	@Override
	public void onParticleUpdate(Timer timer, ParticleContainer p) {
		Quat4f q = Quat4Util.fromAngleAxis(rotCurve.get(1.0F - p.lifetime / p.lifetimeTotal) * rotation, new Vector3f(0, 0, 1), new Quat4f());
		Quat4Util.mult(q, p.rotation, p.rotation);
	}
}
