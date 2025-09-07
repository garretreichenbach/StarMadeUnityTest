package org.schema.schine.graphicsengine.psys.modules;

import java.awt.Color;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.psys.ParticleContainer;
import org.schema.schine.graphicsengine.psys.ParticleSystemConfiguration;
import org.schema.schine.graphicsengine.psys.modules.iface.ParticleUpdateInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.PSCurveVariable;
import org.schema.schine.graphicsengine.psys.modules.variable.XMLSerializable;

public class SizeOverLifetimeModule extends ParticleSystemModule implements ParticleUpdateInterface {

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

	public SizeOverLifetimeModule(ParticleSystemConfiguration sys) {
		super(sys);
	}

	@Override
	protected JPanel getConfigPanel() {
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		int i = 0;
		addRow(p, i++, size);
		return p;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.psys.modules.ParticleSystemModule#getName()
	 */
	@Override
	public String getName() {
		return "Size over Lifetime";
	}

	@Override
	public void onParticleUpdate(Timer timer, ParticleContainer p) {
		p.size.x = size.get(1.0F - p.lifetime / p.lifetimeTotal);
		p.size.y = size.get(1.0F - p.lifetime / p.lifetimeTotal);
		p.size.z = size.get(1.0F - p.lifetime / p.lifetimeTotal);
	}
}
