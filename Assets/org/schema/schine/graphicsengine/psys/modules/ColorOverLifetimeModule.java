package org.schema.schine.graphicsengine.psys.modules;

import java.awt.Color;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.psys.ParticleContainer;
import org.schema.schine.graphicsengine.psys.ParticleSystemConfiguration;
import org.schema.schine.graphicsengine.psys.modules.iface.ParticleColorInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.PSGradientVariable;
import org.schema.schine.graphicsengine.psys.modules.variable.XMLSerializable;

public class ColorOverLifetimeModule extends ParticleSystemModule implements ParticleColorInterface {

	@XMLSerializable(name = "color", type = "gradient")
	PSGradientVariable color = new PSGradientVariable() {

		@Override
		public void init() {
			color.put(0, new Color(255, 255, 255, 254));
			color.put(1, new Color(255, 255, 255, 200));
		}

		@Override
		public String getName() {
			return "color";

		}
	};

	public ColorOverLifetimeModule(ParticleSystemConfiguration sys) {
		super(sys);
		setEnabled(true);
	}

	@Override
	protected JPanel getConfigPanel() {
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		int i = 0;

		addRow(p, i++, color);

		return p;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.psys.modules.ParticleSystemModule#getName()
	 */
	@Override
	public String getName() {
		return "Color over Lifetime";
	}

	@Override
	public void onParticleColor(Vector4f color, ParticleContainer p) {
		Vector4f c = this.color.get(1.0f - p.lifetime / p.lifetimeTotal);
		color.x *= c.x;
		color.y *= c.y;
		color.z *= c.z;
		color.w *= c.w;
	}
}
