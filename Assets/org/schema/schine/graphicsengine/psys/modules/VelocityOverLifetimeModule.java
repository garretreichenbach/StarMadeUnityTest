package org.schema.schine.graphicsengine.psys.modules;

import java.awt.Color;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.psys.ParticleContainer;
import org.schema.schine.graphicsengine.psys.ParticleSystemConfiguration;
import org.schema.schine.graphicsengine.psys.modules.iface.ParticleUpdateInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.DropDownInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.PSCurveVariable;
import org.schema.schine.graphicsengine.psys.modules.variable.StringPair;
import org.schema.schine.graphicsengine.psys.modules.variable.XMLSerializable;

public class VelocityOverLifetimeModule extends ParticleSystemModule implements ParticleUpdateInterface {

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
	@XMLSerializable(name = "space", type = "int")
	private int space = SPACE_WORLD;

	public VelocityOverLifetimeModule(ParticleSystemConfiguration sys) {
		super(sys);
	}

	@Override
	protected JPanel getConfigPanel() {
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		int i = 0;
		addRow(p, i++, x, y, z);

		addRow(p, i++, new DropDownInterface(new StringPair("world", SPACE_WORLD), new StringPair("local", SPACE_LOCAL)) {
			@Override
			public String getName() {
				return "Space";
			}

			@Override
			public int getCurrentIndex() {
				return space;
			}

			@Override
			public void set(StringPair selectedItem) {
				space = selectedItem.val;
			}
		});
		return p;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.psys.modules.ParticleSystemModule#getName()
	 */
	@Override
	public String getName() {
		return "Velocity over Lifetime (Multiplier)";
	}

	@Override
	public void onParticleUpdate(Timer timer, ParticleContainer p) {
		p.velocity.x *= x.get(p.lifetime / p.lifetimeTotal);
		p.velocity.y *= y.get(p.lifetime / p.lifetimeTotal);
		p.velocity.z *= z.get(p.lifetime / p.lifetimeTotal);
	}
}
