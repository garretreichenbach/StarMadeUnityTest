package org.schema.game.client.view.gui.shiphud.newhud;

import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.elements.armorhp.ArmorHPCollection;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector2f;
import java.awt.*;

/**
 * Target Ship Armor HP Bar.
 *
 * @author TheDerpGamer
 */
public class TargetShipArmorHPBar extends FillableHorizontalBar {

	@ConfigurationElement(name = "Color")
	public static Vector4i COLOR;

	@ConfigurationElement(name = "Offset")
	public static Vector2f OFFSET;

	@ConfigurationElement(name = "FlipX")
	public static boolean FLIPX;
	@ConfigurationElement(name = "FlipY")
	public static boolean FLIPY;

	@ConfigurationElement(name = "FillStatusTextOnTop")
	public static boolean FILL_ON_TOP;

	@ConfigurationElement(name = "TextPos")
	public static Vector2f TEXT_POS;

	@ConfigurationElement(name = "TextDescPos")
	public static Vector2f TEXT_DESC_POS;

	public TargetShipArmorHPBar(InputState inputState) {
		super(inputState);
	}

	@Override
	public boolean isBarFlippedX() {
		return FLIPX;
	}

	@Override
	public boolean isBarFlippedY() {
		return FLIPY;
	}

	@Override
	public boolean isFillStatusTextOnTop() {
		return FILL_ON_TOP;
	}

	@Override
	public Vector2f getTextPos() {
		return new Vector2f(TEXT_POS.x, TEXT_POS.y - 7);
	}

	@Override
	public Vector2f getTextDescPos() {
		return new Vector2f(TEXT_DESC_POS.x, TEXT_DESC_POS.y - 7);
	}

	@Override
	public float getFilled() {
		SimpleTransformableSendableObject<?> targetObject = ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getSelectedEntity();
		if(targetObject instanceof ManagedUsableSegmentController<?> managedUsableSegmentController) {
			ArmorHPCollection collection  = managedUsableSegmentController.getManagerContainer().getArmorHP().getCollectionManager();
			double hp = collection.getCurrentHP();
			double maxHP = collection.getMaxHP();
			if(hp == 0 && maxHP == 0) return 0.0f;
			else return (float) (hp / maxHP);
		} else return 0;
	}

	@Override
	public String getText() {
		SimpleTransformableSendableObject<?> targetObject = ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getSelectedEntity();
		if(targetObject instanceof ManagedUsableSegmentController<?> managedUsableSegmentController) {
			ArmorHPCollection collection  = managedUsableSegmentController.getManagerContainer().getArmorHP().getCollectionManager();
			double hp = collection.getCurrentHP();
			double maxHP = collection.getMaxHP();
			if(hp == 0 && maxHP == 0) return Lng.str("No Armor");
			return Lng.str("Armor HP") + " " + StringTools.massFormat(hp) + " / " + StringTools.massFormat(maxHP);
		} else return Lng.str("Armor n/a");
	}

	@Override
	public Vector4i getConfigColor() {
		Color color = Color.decode("#7c97a3");
		return new Vector4i(color.getRed(), color.getGreen(), color.getBlue(), 255);
	}

	@Override
	public GUIPosition getConfigPosition() {
		return null;
	}

	@Override
	public Vector2f getConfigOffset() {
		OFFSET.y = 136;
		return OFFSET;
	}

	@Override
	protected String getTag() {
		return "TargetArmorHPBar";
	}
}
