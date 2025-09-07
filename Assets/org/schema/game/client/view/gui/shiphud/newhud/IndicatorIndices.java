package org.schema.game.client.view.gui.shiphud.newhud;

import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.game.common.controller.*;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector2f;

public class IndicatorIndices extends HudConfig {
	@ConfigurationElement(name = "Lifeform")
	public static int lifeform;
	@ConfigurationElement(name = "Asteroid")
	public static int asteroid;
	@ConfigurationElement(name = "Other")
	public static int other;
	@ConfigurationElement(name = "Shop")
	public static int shop;
	@ConfigurationElement(name = "Planet")
	public static int planet;
	@ConfigurationElement(name = "Ship")
	public static int ship;
	@ConfigurationElement(name = "Station")
	public static int station;

	public IndicatorIndices(InputState state) {
		super(state);
	}

	public static int getCIndex(SimpleTransformableSendableObject<?> b) {
		if(b instanceof AbstractCharacter<?>) {
			return lifeform;
		} else if(b instanceof Ship) {
			return ship;
		} else if(b instanceof SpaceStation) {
			return station;
		} else if(b instanceof FloatingRock) {
			return asteroid;
		} else if(b instanceof Planet || b instanceof PlanetIco) {
			return planet;
		} else if(b instanceof ShopSpaceStation) {
			return shop;
		} else {
			return other;
		}
	}

	@Override
	public Vector4i getConfigColor() {
		return null;
	}

	@Override
	public GUIPosition getConfigPosition() {
		return null;
	}

	@Override
	public Vector2f getConfigOffset() {
		return null;
	}

	@Override
	protected String getTag() {
		return "IndicatorIndices";
	}
}
