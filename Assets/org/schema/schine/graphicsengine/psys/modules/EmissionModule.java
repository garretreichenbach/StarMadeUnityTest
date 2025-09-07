package org.schema.schine.graphicsengine.psys.modules;

import java.awt.Color;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.psys.ParticleSystemConfiguration;
import org.schema.schine.graphicsengine.psys.modules.variable.BooleanInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.DropDownInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.PSCurveVariable;
import org.schema.schine.graphicsengine.psys.modules.variable.StringPair;
import org.schema.schine.graphicsengine.psys.modules.variable.VarInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.XMLSerializable;

import it.unimi.dsi.fastutil.floats.Float2IntOpenHashMap;

public class EmissionModule extends ParticleSystemModule {

	public static int RATE_BY_SECOND = 0;
	public static int RATE_BY_METER = 1;
	//	Rate	Amount of particles emitted over Time (per second) or Distance (per meter) (see MinMaxCurve).
	@XMLSerializable(name = "useCurve", type = "boolean")
	boolean useCurve;
	@XMLSerializable(name = "type", type = "int")

	int type;
	//	Bursts (Time option only)	Add bursts of particles that occur within the duration of the Particle System.
	//and Number of Particles	Specify time (in seconds within duration) that a specified amount of particles should be emitted. Use the + and - for adjusting number of bursts.
	Float2IntOpenHashMap bursts = new Float2IntOpenHashMap();
	@XMLSerializable(name = "rate", type = "int")
	private int rate = 10;
	@XMLSerializable(name = "rateCurve", type = "curve")
	private PSCurveVariable rateCurve = new PSCurveVariable() {
		@Override
		public String getName() {
			return "rate curve";
		}

		@Override
		public Color getColor() {
			return Color.BLUE;
		}
	};
	private long nextSpawn = 0;

	public EmissionModule(ParticleSystemConfiguration sys) {
		super(sys);
	}

	@Override
	protected JPanel getConfigPanel() {
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		int i = 0;
		addRow(p, i++, new VarInterface<Integer>() {

			@Override
			public String getName() {
				return "rate";
			}

			@Override
			public Integer get() {
				return rate;
			}

			@Override
			public void set(String f) {
				rate = Math.max(0, Integer.parseInt(f));
			}

			@Override
			public Integer getDefault() {
				return 10;
			}

		});
		addRow(p, i++, new DropDownInterface(new StringPair("per second", RATE_BY_SECOND), new StringPair("per meter", RATE_BY_METER)) {

			@Override
			public String getName() {
				return "Type";
			}

			@Override
			public int getCurrentIndex() {
				return type;
			}

			@Override
			public void set(StringPair selectedItem) {
				type = selectedItem.val;
			}
		});
		addRow(p, i++, new BooleanInterface() {

			@Override
			public boolean get() {
				return useCurve;
			}

			@Override
			public void set(boolean selected) {
				useCurve = selected;
			}

			@Override
			public String getName() {
				return "use curve";
			}
		});

		addRow(p, i++, rateCurve);
		return p;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.psys.modules.ParticleSystemModule#getName()
	 */
	@Override
	public String getName() {
		return "Emission";
	}

	public int getParticlesToSpawn(Timer timer) {
		if (rate <= 0) {
			return 0;
		}

		if (!useCurve) {
			int ret = 0;
			if (timer.currentTime >= nextSpawn) {
				ret = Math.min(sys.getMaxParticles() - sys.getParticleCount(), 1);
				nextSpawn = timer.currentTime + (1000L / rate);
			}
			return ret;
		} else {
			int ret = 0;
			if (timer.currentTime >= nextSpawn) {
				ret = Math.min(sys.getMaxParticles() - sys.getParticleCount(), 1);
				float val = (rate * rateCurve.get(sys.getSystemTime() / sys.getParticleSystemDuration()));
				if (val != 0) {
					nextSpawn = timer.currentTime + (int) Math.floor(1000L / val);
				} else {
					nextSpawn = -1;
				}
			}
			return ret;
		}
	}
}
