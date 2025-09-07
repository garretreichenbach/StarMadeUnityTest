package org.schema.schine.graphicsengine.forms.particle.simple;

import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.forms.particle.ParticleController;

public class ParticleSimpleDrawerFlare extends ParticleSimpleDrawer {

	private static final float SPRITE_SIZE = 3.5f;

	public ParticleSimpleDrawerFlare(ParticleController controller) {
		this(controller, SPRITE_SIZE);
	}

	public ParticleSimpleDrawerFlare(ParticleController controller, float size) {
		super(controller, size);
	}

	@Override
	public void onInit() {
		if (getTexture() == null) {
			setTexture(Controller.getResLoader().getSprite("starSprite").getMaterial().getTexture());
		}
		super.onInit();
	}

	
}
