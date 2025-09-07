package org.schema.schine.graphicsengine.psys.modules;

import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.psys.ParticleContainer;
import org.schema.schine.graphicsengine.psys.ParticleSystemConfiguration;
import org.schema.schine.graphicsengine.psys.modules.iface.ParticleColorInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.PSGradientVariable;
import org.schema.schine.graphicsengine.psys.modules.variable.VarInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.XMLSerializable;

public class ColorBySpeedModule extends ParticleSystemModule implements ParticleColorInterface {

	@XMLSerializable(name = "color", type = "gradient")
	PSGradientVariable color = new PSGradientVariable() {

		@Override
		public String getName() {
			return "color";
		}

	};
	@XMLSerializable(name = "minSpeed", type = "float")
	float minSpeed = 0;
	@XMLSerializable(name = "maxSpeed", type = "float")
	float maxSpeed = 1;

	public ColorBySpeedModule(ParticleSystemConfiguration sys) {
		super(sys);
	}

	@Override
	protected JPanel getConfigPanel() {
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		int i = 0;

		addRow(p, i++, color);
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
		return "Color By Speed";
	}

	@Override
	public void onParticleColor(Vector4f color, ParticleContainer p) {
		if (maxSpeed - minSpeed != 0.0F) {
			Vector4f c = this.color.get(Math.min(maxSpeed, Math.max(minSpeed, p.velocity.length())) / (maxSpeed - minSpeed));
			color.x *= c.x;
			color.y *= c.y;
			color.z *= c.z;
			color.w *= c.w;
		}
	}
}
