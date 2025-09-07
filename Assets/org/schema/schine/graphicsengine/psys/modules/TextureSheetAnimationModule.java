package org.schema.schine.graphicsengine.psys.modules;

import java.awt.Color;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

import org.schema.schine.graphicsengine.psys.ParticleSystemConfiguration;
import org.schema.schine.graphicsengine.psys.modules.variable.BooleanInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.PSCurveVariable;
import org.schema.schine.graphicsengine.psys.modules.variable.VarInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.XMLSerializable;

public class TextureSheetAnimationModule extends ParticleSystemModule {

	@XMLSerializable(name = "singleRow", type = "boolean")
	boolean singleRow;
	@XMLSerializable(name = "randomRow", type = "boolean")
	boolean randomRow;
	@XMLSerializable(name = "frameOverTime", type = "curve")
	PSCurveVariable frameOverTime = new PSCurveVariable() {
		@Override
		public String getName() {
			return "frame over time";
		}

		@Override
		public Color getColor() {
			return Color.RED;
		}
	};
	@XMLSerializable(name = "cycles", type = "float")
	float cycles;

	public TextureSheetAnimationModule(ParticleSystemConfiguration sys) {
		super(sys);
	}

	@Override
	protected JPanel getConfigPanel() {
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		int i = 0;
		addRow(p, i++, new BooleanInterface() {

			@Override
			public boolean get() {
				return singleRow;
			}

			@Override
			public void set(boolean selected) {
				singleRow = selected;
			}

			@Override
			public String getName() {
				return "use single row";
			}
		});

		addRow(p, i++, frameOverTime);

		addRow(p, i++, new VarInterface<Float>() {

			@Override
			public String getName() {
				return "cycles";
			}

			@Override
			public Float get() {
				return cycles;
			}

			@Override
			public void set(String f) {
				cycles = Math.max(0, Float.parseFloat(f));
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
		return "Texture Sheet Animation";
	}

}
