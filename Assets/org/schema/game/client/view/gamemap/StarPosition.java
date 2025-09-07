package org.schema.game.client.view.gamemap;

import api.common.GameClient;
import com.bulletphysics.linearmath.Transform;
import org.schema.common.util.linAlg.Vector;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.ingame.map.MapControllerManager;
import org.schema.game.client.data.gamemap.entry.SelectableMapEntry;
import org.schema.game.client.view.effects.ConstantIndication;
import org.schema.game.client.view.effects.Indication;
import org.schema.game.client.view.gui.shiphud.HudIndicatorOverlay;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.server.data.Galaxy;
import org.schema.schine.graphicsengine.forms.PositionableSubColorSprite;
import org.schema.schine.graphicsengine.forms.SelectableSprite;
import org.schema.schine.graphicsengine.forms.Sprite;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

public class StarPosition implements PositionableSubColorSprite, SelectableSprite, SelectableMapEntry {

	public static float posAdd = 0;
	public static int posMult = 1;
	public static float spriteScale = 1;
	public static Indication indication;
	public int starSubSprite;
	public Vector3f pos = new Vector3f();
	public Vector3f posRet = new Vector3f();
	public Vector4f color = new Vector4f(1, 1, 1, 1);
	public Vector4f colorRet = new Vector4f(1, 1, 1, 1);
	public Vector3i relPosInGalaxy = new Vector3i();
	public float scale = 1;
	private float selectDepth;
	private boolean drawIndication;
	private float distanceToCam;

	@Override
	public float getScale(long time) {
		return spriteScale;
	}

	@Override
	public int getSubSprite(Sprite sprite) {
		return starSubSprite;
	}

	@Override
	public boolean canDraw() {
		return true;
	}

	@Override
	public Vector3f getPos() {
		posRet.set(pos);

		posRet.scale(posMult);
		posRet.x += (posAdd);
		posRet.y += (posAdd);
		posRet.z += (posAdd);
		return posRet;
	}

	@Override
	public Vector4f getColor() {
		colorRet.set(color);

		colorRet.w *= (Math.min(1, distanceToCam / 150f));

		return colorRet;
	}

	@Override
	public float getSelectionDepth() {
		return selectDepth;
	}

	@Override
	public boolean isSelectable() {
		return true;
	}

	@Override
	public void onSelect(float depth) {
		drawIndication = true;
		this.selectDepth = depth;
		MapControllerManager.selected.add(this);
		HudIndicatorOverlay.toDrawStars.add(this);
	}

	@Override
	public void onUnSelect() {
		drawIndication = false;
		MapControllerManager.selected.remove(this);
		HudIndicatorOverlay.toDrawStars.remove(this);
	}

	@Override
	public boolean isDrawIndication() {
		return drawIndication;
	}

	@Override
	public void setDrawIndication(boolean b) {
		drawIndication = b;
	}

	/**
	 * @return the distanceToCam
	 */
	public float getDistanceToCam() {
		return distanceToCam;
	}

	/**
	 * @param distanceToCam the distanceToCam to set
	 */
	public void setDistanceToCam(float distanceToCam) {
		this.distanceToCam = distanceToCam;
	}

	public Indication getIndication(Galaxy galaxy) {
		if (indication == null) {
			Transform t = new Transform();
			t.setIdentity();
			indication = new ConstantIndication(t, "STAR" + pos);
		}
		indication.getCurrentTransform().origin.set(pos.x * GameMapDrawer.size, pos.y * GameMapDrawer.size, pos.z * GameMapDrawer.size);
		indication.setText(galaxy.getName(relPosInGalaxy) + " " + getSector().toString());
//		indication.setText(galaxy.getName(relPosInGalaxy) + " (" + (relPosInGalaxy.x + galaxy.galaxyPos.x * Galaxy.size) + ", " + (relPosInGalaxy.y + galaxy.galaxyPos.y * Galaxy.size) + ", " + (relPosInGalaxy.z + galaxy.galaxyPos.z * Galaxy.size) + ")");
		return indication;

	}

	public String getName() {
		String name = GameClient.getClientState().getCurrentGalaxy().getName(relPosInGalaxy);
		if(name == null) name = "Star";
		return name;
	}

	public Vector3i getSector() {
		return GameClient.getClientState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getMapControlManager().getEntryPos(this);
	}
}
