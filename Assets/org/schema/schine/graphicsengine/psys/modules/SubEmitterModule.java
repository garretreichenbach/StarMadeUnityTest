package org.schema.schine.graphicsengine.psys.modules;

import javax.swing.JPanel;

import org.schema.schine.graphicsengine.psys.ParticleSystemConfiguration;

public class SubEmitterModule extends ParticleSystemModule {

	public SubEmitterModule(ParticleSystemConfiguration sys) {
		super(sys);
	}

	@Override
	protected JPanel getConfigPanel() {
		JPanel p = new JPanel();
		return p;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.psys.modules.ParticleSystemModule#getName()
	 */
	@Override
	public String getName() {
		return "Sub-Emitter";
	}
}
