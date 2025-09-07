package org.schema.game.client.view.gui.shiphud;

import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUITiledTextureRectangle;
import org.schema.schine.graphicsengine.texture.Texture;
import org.schema.schine.input.InputState;

public class HudRadar extends GUIElement {

	private Texture texture;
	private GUIOverlay background;
	private GUITiledTextureRectangle radarBackground;
	private int height;
	private int width;
	private RadarOverlay radar;
	private Gyroscope gyroscope;

	public HudRadar(InputState state, int width, int height) {
		super(state);
		this.width = width;
		this.height = height;
	}

	@Override
	public void cleanUp() {

	}

	@Override
	public void draw() {

		GlUtil.glPushMatrix();
		transform();

		background.draw();

		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
		texture = Controller.getResLoader().getSprite("radarBackground").getMaterial().getTexture();
		radarBackground = new GUITiledTextureRectangle(getState(), width, height, texture, 32);
		radarBackground.onInit();

		background = new GUIOverlay(Controller.getResLoader().getSprite("radarGUIBackground"), getState());

		background.attach(radarBackground);
		radarBackground.setPos(8, 8, 0);

		radar = new RadarOverlay(getState(), width);
		radar.onInit();
		radarBackground.attach(radar);

		gyroscope = new Gyroscope(getState());
		gyroscope.onInit();
		background.attach(gyroscope);
		//		gyroscope.getPos().y += (-(128+16));
	}

	@Override
	public float getHeight() {
		return background.getHeight();
	}

	@Override
	public float getWidth() {
		return background.getWidth();
	}

	@Override
	public boolean isPositionCenter() {
				return false;
	}

}
