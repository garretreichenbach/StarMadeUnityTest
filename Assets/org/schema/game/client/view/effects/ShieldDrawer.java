package org.schema.game.client.view.effects;

import java.util.Observable;

import javax.vecmath.Vector3f;

import org.schema.game.client.controller.manager.ingame.BuildToolsManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.shader.ShieldShader;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ShieldAddOn;
import org.schema.game.common.controller.elements.ShieldContainerInterface;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.world.DrawableRemoteSegment;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;

import com.bulletphysics.linearmath.Transform;

public class ShieldDrawer extends Observable implements Drawable {

	SegmentController controller;
	Transform tt = new Transform();
	Transform inv = new Transform();
	private ShieldAddOn manager;
	private Vector3f center = new Vector3f();
	private ShieldShader shieldShader;
	private boolean firstDraw = true;
	private double shieldPercent;

	public ShieldDrawer(final ManagedSegmentController<?> controller) {
		super();
		set(controller);
		shieldShader = new ShieldShader();
	}

	public void addHit(float worldX, float worldY, float worldZ, float damage, float percent) {
		BuildToolsManager buildToolsManager = ((GameClientState)controller.getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager();
		
		center.set(worldX, worldY, worldZ);
		controller.getWorldTransformInverse().transform(center);
		shieldShader.addCollision(center, damage, percent);

		if (shieldShader.getCollisionNum() > 0) {
			setChanged();
			notifyObservers(true);

		}
	}
	public void addHitOld(Vector3f hitPoint, float damage) {
		addHit(hitPoint.x, hitPoint.y, hitPoint.z, damage, (float)shieldPercent);
	}

	@Override
	public void cleanUp() {
		
	}

	@Override
	public void draw() {
		if (!EngineSettings.G_DRAW_SHIELDS.isOn()) {
			return;
		}
		//		if(getShieldShader().getCollisionNum() > 0){
		//			drawShields();
		//		}

	}

	@Override
	public boolean isInvisible() {
		return false;
	}

	@Override
	public void onInit() {
		if (!EngineSettings.G_DRAW_SHIELDS.isOn()) {
			return;
		}

		firstDraw = false;
	}

	public void drawShields() {
		if (firstDraw) {
			onInit();
		}
		SegmentController sc = controller;
		if (sc instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) sc).getManagerContainer() instanceof ShieldContainerInterface) {
			//			ShieldRegenCollectionManager shieldManager = ((ShieldContainerInterface)((ManagedSegmentController<?>)sc).getManagerContainer()).getShieldManager();

			shieldShader.updateShaderParameters(shieldShader.s);
			//			drawElementCollectionWired(controller.getWorldTransform(), shieldManager);

		}
	}

	/**
	 * @return the shieldPercent
	 */
	public double getShieldPercent() {
		return shieldPercent;
	}

	/**
	 * @return the shieldShader
	 */
	public ShieldShader getShieldShader() {
		return shieldShader;
	}

	/**
	 * @param shieldShader the shieldShader to set
	 */
	public void setShieldShader(ShieldShader shieldShader) {
		this.shieldShader = shieldShader;
	}

	public boolean hasHit(DrawableRemoteSegment e) {
		BuildToolsManager buildToolsManager = ((GameClientState)controller.getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager();
		
		if(shieldShader.getCollisionNum() == 0 || !EngineSettings.G_DRAW_SHIELDS.isOn() || (controller.isClientOwnObject() && ((GameClientState)controller.getState()).isInAnyStructureBuildMode() && buildToolsManager.lighten)){
			return false;
		}

		return shieldShader.hasCollisionInRange(e.pos.x, e.pos.y, e.pos.z);
	}

	public void reset() {
		controller = null;
		manager = null;
		center.set(0, 0, 0);
		tt.setIdentity();
		inv.setIdentity();
		shieldShader.reset();
	}

	public void set(final ManagedSegmentController<?> controller) {
		this.controller = controller.getSegmentController();
		this.manager = ((ShieldContainerInterface) controller.getManagerContainer()).getShieldAddOn();
	}

	public void update(Timer timer) {
		if (!EngineSettings.G_DRAW_SHIELDS.isOn()) {
			return;
		}

		shieldShader.update(timer);
		if (shieldShader.getCollisionNum() <= 0) {
			setChanged();
			notifyObservers(false);
		}
		shieldPercent = manager.getShields() / manager.getShieldCapacity();

	}

}
