package org.schema.schine.graphicsengine.psys.modules;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.geom.Point2D;

import javax.swing.JPanel;

import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.psys.ParticleContainer;
import org.schema.schine.graphicsengine.psys.ParticleSystemConfiguration;
import org.schema.schine.graphicsengine.psys.modules.iface.ParticleUpdateInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.BooleanInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.DropDownInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.PSCurveVariable;
import org.schema.schine.graphicsengine.psys.modules.variable.StringPair;
import org.schema.schine.graphicsengine.psys.modules.variable.XMLSerializable;

public class ForceOverLifetimeModule extends ParticleSystemModule implements ParticleUpdateInterface {

	@XMLSerializable(name = "x", type = "curve")
	PSCurveVariable x = new PSCurveVariable() {
		@Override
		public String getName() {
			return "X";
		}

		@Override
		public void initPoints() {
			getPoints().add(new Point2D.Double(0, 0));
			getPoints().add(new Point2D.Double(1, 0));
			getPointsSecond().add(new Point2D.Double(0, 0));
			getPointsSecond().add(new Point2D.Double(1, 0));
			revalidate();
		}

		@Override
		public Color getColor() {
			return Color.RED;
		}
	};
	@XMLSerializable(name = "y", type = "curve")
	PSCurveVariable y = new PSCurveVariable() {
		@Override
		public void initPoints() {
			getPoints().add(new Point2D.Double(0, 0));
			getPoints().add(new Point2D.Double(1, 0));
			getPointsSecond().add(new Point2D.Double(0, 0));
			getPointsSecond().add(new Point2D.Double(1, 0));
			revalidate();
		}

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
		public void initPoints() {
			getPoints().add(new Point2D.Double(0, 0));
			getPoints().add(new Point2D.Double(1, 0));
			getPointsSecond().add(new Point2D.Double(0, 0));
			getPointsSecond().add(new Point2D.Double(1, 0));
			revalidate();
		}

		@Override
		public String getName() {
			return "Z";
		}

		@Override
		public Color getColor() {
			return Color.BLUE;
		}
	};
	@XMLSerializable(name = "randomize", type = "boolean")
	boolean randomize = true;
	@XMLSerializable(name = "space", type = "int")
	private int space = SPACE_WORLD;

	public ForceOverLifetimeModule(ParticleSystemConfiguration sys) {
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

		addRow(p, i++, new BooleanInterface() {

			@Override
			public boolean get() {
				return randomize;
			}

			@Override
			public void set(boolean selected) {
				randomize = selected;
			}

			@Override
			public String getName() {
				return "randomize";
			}
		});
		return p;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.psys.modules.ParticleSystemModule#getName()
	 */
	@Override
	public String getName() {
		return "Velocity over Lifetime (Additive)";
	}

	@Override
	public void onParticleUpdate(Timer timer, ParticleContainer p) {
		p.velocity.x += timer.getDelta() * x.get(p.lifetime / p.lifetimeTotal);
		p.velocity.y += timer.getDelta() * y.get(p.lifetime / p.lifetimeTotal);
		p.velocity.z += timer.getDelta() * z.get(p.lifetime / p.lifetimeTotal);
	}
}
