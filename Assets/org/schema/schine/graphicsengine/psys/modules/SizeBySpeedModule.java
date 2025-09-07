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

public class SizeBySpeedModule extends ParticleSystemModule implements ParticleUpdateInterface {

	@XMLSerializable(name = "size", type = "curve")
	PSCurveVariable size = new PSCurveVariable() {
		@Override
		public String getName() {
			return "size";
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

	public SizeBySpeedModule(ParticleSystemConfiguration sys) {
		super(sys);
	}

	@Override
	protected JPanel getConfigPanel() {
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		int i = 0;
		addRow(p, i++, size);

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
		return "Size by Speed";
	}

	@Override
	public void onParticleUpdate(Timer timer, ParticleContainer p) {
		p.size.x = size.get(Math.abs(p.velocity.length()));
		p.size.y = size.get(Math.abs(p.velocity.length()));
		p.size.z = size.get(Math.abs(p.velocity.length()));
	}
}
