package org.schema.game.client.view.gui.shiphud.newhud;

import java.lang.reflect.Field;
import java.util.Locale;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.schema.common.config.ConfigParserException;
import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.FactionState;
import org.schema.schine.input.InputState;
import org.w3c.dom.Document;

public class ColorPalette extends HudConfig {

	@ConfigurationElement(name = "EnemyLifeform")
	public static Vector4f enemyLifeform;

	@ConfigurationElement(name = "AllyLifeform")
	public static Vector4f allyLifeform;

	@ConfigurationElement(name = "FactionLifeform")
	public static Vector4f factionLifeform;

	@ConfigurationElement(name = "NeutralLifeform")
	public static Vector4f neutralLifeform;

	@ConfigurationElement(name = "EnemyOther")
	public static Vector4f enemyOther;

	@ConfigurationElement(name = "AllyOther")
	public static Vector4f allyOther;

	@ConfigurationElement(name = "FactionOther")
	public static Vector4f factionOther;

	@ConfigurationElement(name = "NeutralOther")
	public static Vector4f neutralOther;

	@ConfigurationElement(name = "EnemyShip")
	public static Vector4f enemyShip;

	@ConfigurationElement(name = "AllyShip")
	public static Vector4f allyShip;

	@ConfigurationElement(name = "FactionShip")
	public static Vector4f factionShip;

	@ConfigurationElement(name = "NeutralShip")
	public static Vector4f neutralShip;
	
	
	@ConfigurationElement(name = "EnemyOverheating")
	public static Vector4f enemyOverheating;
	
	@ConfigurationElement(name = "AllyOverheating")
	public static Vector4f allyOverheating;
	
	@ConfigurationElement(name = "FactionOverheating")
	public static Vector4f factionOverheating;
	
	@ConfigurationElement(name = "NeutralOverheating")
	public static Vector4f neutralOverheating;

	@ConfigurationElement(name = "EnemyStation")
	public static Vector4f enemyStation;

	@ConfigurationElement(name = "AllyStation")
	public static Vector4f allyStation;

	@ConfigurationElement(name = "FactionStation")
	public static Vector4f factionStation;

	@ConfigurationElement(name = "NeutralStation")
	public static Vector4f neutralStation;

	@ConfigurationElement(name = "EnemyShop")
	public static Vector4f enemyShop;

	@ConfigurationElement(name = "AllyShop")
	public static Vector4f allyShop;

	@ConfigurationElement(name = "FactionShop")
	public static Vector4f factionShop;

	@ConfigurationElement(name = "NeutralShop")
	public static Vector4f neutralShop;

	@ConfigurationElement(name = "EnemyTurret")
	public static Vector4f enemyTurret;

	@ConfigurationElement(name = "AllyTurret")
	public static Vector4f allyTurret;

	@ConfigurationElement(name = "FactionTurret")
	public static Vector4f factionTurret;

	@ConfigurationElement(name = "NeutralTurret")
	public static Vector4f neutralTurret;
	
	@ConfigurationElement(name = "EnemyUnpoweredDock")
	public static Vector4f enemyUnpoweredDock;
	
	@ConfigurationElement(name = "AllyUnpoweredDock")
	public static Vector4f allyUnpoweredDock;
	
	@ConfigurationElement(name = "FactionUnpoweredDock")
	public static Vector4f factionUnpoweredDock;
	
	@ConfigurationElement(name = "NeutralUnpoweredDock")
	public static Vector4f neutralUnpoweredDock;

	@ConfigurationElement(name = "EnemyDock")
	public static Vector4f enemyDock;

	@ConfigurationElement(name = "AllyDock")
	public static Vector4f allyDock;

	@ConfigurationElement(name = "FactionDock")
	public static Vector4f factionDock;

	@ConfigurationElement(name = "NeutralDock")
	public static Vector4f neutralDock;

	@ConfigurationElement(name = "EnemyAsteroid")
	public static Vector4f enemyAsteroid;
	
	@ConfigurationElement(name = "AllyAsteroid")
	public static Vector4f allyAsteroid;
	
	@ConfigurationElement(name = "FactionAsteroid")
	public static Vector4f factionAsteroid;
	
	@ConfigurationElement(name = "NeutralAsteroid")
	public static Vector4f neutralAsteroid;
	
	@ConfigurationElement(name = "EnemyPlanet")
	public static Vector4f enemyPlanet;
	
	@ConfigurationElement(name = "AllyPlanet")
	public static Vector4f allyPlanet;
	
	@ConfigurationElement(name = "FactionPlanet")
	public static Vector4f factionPlanet;
	
	@ConfigurationElement(name = "NeutralPlanet")
	public static Vector4f neutralPlanet;

	public static Vector4f[] lifeform = new Vector4f[4];
	public static Vector4f[] other = new Vector4f[4];
	public static Vector4f[] ship = new Vector4f[4];
	public static Vector4f[] station = new Vector4f[4];
	public static Vector4f[] turret = new Vector4f[4];
	public static Vector4f[] dock = new Vector4f[4];

	@ConfigurationElement(name = "SimpleNoteColor")
	public static Vector4f simpleNoteColor;

	@ConfigurationElement(name = "InfoNoteColor")
	public static Vector4f infoNoteColor;

	@ConfigurationElement(name = "ErrorNoteColor")
	public static Vector4f errorNoteColor;

	@ConfigurationElement(name = "GameNoteColor")
	public static Vector4f gameNoteColor;

	@ConfigurationElement(name = "Buff")
	public static Vector4f buff;

	@ConfigurationElement(name = "Debuff")
	public static Vector4f debuff;

	public ColorPalette(InputState state) {
		super(state);
	}

	public static Vector4f[] getCIndex(SimpleTransformableSendableObject b) {
		if (b instanceof AbstractCharacter<?>) {
			return lifeform;
		} else if (b instanceof Ship) {
			if (((Ship) b).getDockingController().isDocked()) {
				if (((Ship) b).getDockingController().isTurretDocking()) {
					return turret;
				} else {
					return dock;
				}
			} else {
				return ship;
			}
		} else if (b instanceof SpaceStation) {
			return station;
		} else {
			return other;
		}
	}

	public static Vector4f getColorDefault(RType relation, boolean sameFaction) {
		return getColor(relation, sameFaction, other);
	}

	public static Vector4f getColor(RType relation, boolean sameFaction, Vector4f[] cindex) {
		switch (relation) {
			case ENEMY:
				return cindex[0];
			case FRIEND:
				if (sameFaction) {
					return cindex[2];
				} else {
					return cindex[1];
				}
			case NEUTRAL:
				return cindex[3];
			default:
				break;
		}

		throw new IllegalArgumentException("Cannot find color");
	}

	public static Vector4f getColor(SimpleTransformableSendableObject a, SimpleTransformableSendableObject b) {

		RType relation = ((FactionState) a.getState()).getFactionManager().getRelation(a, b);
		try {
			return getColor(relation, a.getFactionId() == b.getFactionId(), getCIndex(b));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Cannot find color for: " + a + " --- " + b);
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
		return "ColorPalette";
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.gui.shiphud.newhud.HudConfig#parse(org.w3c.dom.Document)
	 */
	@Override
	public void parse(Document config) throws IllegalArgumentException,
			IllegalAccessException, ConfigParserException {
		super.parse(config);

		Field[] declaredFields = this.getClass().getDeclaredFields();

		for (Field f : declaredFields) {
			putF(f);
		}
	}

	private void putIn(Vector4f[] in, Field f) throws IllegalArgumentException, IllegalAccessException {
		ConfigurationElement annotation = f.getAnnotation(ConfigurationElement.class);
		if (annotation != null) {
			if (annotation.name().toLowerCase(Locale.ENGLISH).startsWith("enemy")) {
				in[0] = (Vector4f) f.get(this);
			} else if (annotation.name().toLowerCase(Locale.ENGLISH).startsWith("ally")) {
				in[1] = (Vector4f) f.get(this);
			} else if (annotation.name().toLowerCase(Locale.ENGLISH).startsWith("faction")) {
				in[2] = (Vector4f) f.get(this);
			} else if (annotation.name().toLowerCase(Locale.ENGLISH).startsWith("neutral")) {
				in[3] = (Vector4f) f.get(this);
			} else {
				assert (false);
			}
		}
	}

	private void putF(Field f) throws IllegalArgumentException, IllegalAccessException {
		ConfigurationElement annotation = f.getAnnotation(ConfigurationElement.class);
		if (annotation != null) {
			if (annotation.name().toLowerCase(Locale.ENGLISH).endsWith("lifeform")) {
				putIn(lifeform, f);
			} else if (annotation.name().toLowerCase(Locale.ENGLISH).endsWith("other")) {
				putIn(other, f);
			} else if (annotation.name().toLowerCase(Locale.ENGLISH).endsWith("ship")) {
				putIn(ship, f);
			} else if (annotation.name().toLowerCase(Locale.ENGLISH).endsWith("station")) {
				putIn(station, f);
			} else if (annotation.name().toLowerCase(Locale.ENGLISH).endsWith("turret")) {
				putIn(turret, f);
			} else if (annotation.name().toLowerCase(Locale.ENGLISH).endsWith("dock")) {
				putIn(dock, f);
			} else {
			}
		}
	}

}
