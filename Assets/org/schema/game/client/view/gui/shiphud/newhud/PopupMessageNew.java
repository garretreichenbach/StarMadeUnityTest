package org.schema.game.client.view.gui.shiphud.newhud;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.controller.manager.ingame.SegmentBuildController;
import org.schema.game.client.controller.manager.ingame.SegmentControlManager;
import org.schema.game.client.controller.manager.ingame.ship.ShipControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIPopupInterface;
import org.schema.game.client.view.gui.buildtools.BuildToolsPanel;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.font.unicode.Color;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUINewButtonBackground;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUINormalBackground;
import org.schema.schine.graphicsengine.util.timer.SinusTimerUtil;
import org.schema.schine.input.InputState;

public class PopupMessageNew extends HudConfig implements GUIPopupInterface {

	@ConfigurationElement(name = "PopoutTimeInSec")
	public static float popoutTimeInSec;
	@ConfigurationElement(name = "AncorOnRadarAndAdvBuildMode")
	public static boolean ancorOnRadarAndAdvBuildMode;

	@ConfigurationElement(name = "DistFromTop")
	public static int distFromTop;
	@ConfigurationElement(name = "DistFromRight")
	public static int distFromRight;
	@ConfigurationElement(name = "StackedDistanceX")
	public static int stackedDistanceX;
	public static Vector2f targetPanel = new Vector2f();
	public float index = 0;
	public boolean flashing;
	SinusTimerUtil tu = new SinusTimerUtil(5);
	private GUINormalBackground background;
	private float timeOutInSeconds = 5;
	private float timeDrawn;
	private float timeDelayed;
	private GUITextOverlay text;
	private boolean firstDraw = true;
	private float timeDelayInSecs;
	private float currentIndex = 0;
	private float currentIndexX = 0;
	private String message;
	private Color color = new Color(1, 1, 1, 1);
	private float popupTime;
	private GUINewButtonBackground overlayA;
	private String id;
	private GUINewButtonBackground overlayB;
	private int currentHeight;
	public float distFromLeft = 256;

	public PopupMessageNew(InputState state, String id, String message, Color color) {
		super(state);
		this.id = id;
		if (message != null) {
			text = new GUITextOverlay(state);
			
			this.message = message;

			this.color.r = color.r;
			this.color.g = color.g;
			this.color.b = color.b;
			this.color.a = color.a;

		}
	}

	@Override
	public void cleanUp() {
		
	}

	@Override
	public void draw() {
		if (firstDraw) {
			onInit();
		}
		if (timeDelayed < timeDelayInSecs) {
			return;
		}
		int textWidth = text.getMaxLineWidth();
		int textHeight = text.getTextHeight();

		background.setWidth(textWidth + 26 * 2);
		background.setHeight(textHeight + 8 * 2);

//		System.err.println("CCCX: "+currentIndexX);
		if(isLeft()){
			getPos().x = (int) (distFromLeft + currentIndexX);
		}else{
			getPos().x = (int) (((GLFrame.getWidth()) - (getWidth() + distFromRight)) + currentIndexX);
		}
		getPos().y = (int) (currentIndex);
		if (!isOnScreen()) {
			return;
		}
		//		text.setColor(color);
		float a = timeOutInSeconds - timeDrawn;
		float f = flashing ? tu.getTime() * 0.5f : 0;
		overlayA.getPos().x = 8;
		overlayB.getPos().x = background.getWidth() - (8 + overlayB.getWidth());

		overlayA.getPos().y = 2;
		overlayB.getPos().y = 2;

		overlayA.setHeight(background.getHeight() - 4);
		overlayB.setHeight(background.getHeight() - 4);

		overlayA.getColor().set(color.r - f, color.g - f, color.b - f, 1);
		overlayB.getColor().set(color.r - f, color.g - f, color.b - f, 1);
		if (a < 1) {
			background.getColor().set(1, 1, 1, a);
			overlayA.getColor().set(color.r - f, color.g - f, color.b - f, a);
			overlayB.getColor().set(color.r - f, color.g - f, color.b - f, a);
			text.getColor().a = a;
		}
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GlUtil.glPushMatrix();
		transform();
		background.draw();
		GlUtil.glPopMatrix();

		GlUtil.glDisable(GL11.GL_BLEND);

		background.getColor().set(1, 1, 1, 1);
		text.getColor().a = 1;
		text.getColor().r = 1;
		text.getColor().g = 1;
		text.getColor().b = 1;
	}

	private boolean isLeft() {
		return ((GameClientState)getState()).getGlobalGameControlManager().getIngameControlManager()
				.getPlayerGameControlManager().getPlayerIntercationManager().isInAnyStructureBuildMode() || 
				((GameClientState)getState()).getGlobalGameControlManager().getIngameControlManager()
				.getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getShipExternalFlightController().isActive();
	}

	@Override
	public void onInit() {

		overlayA = new GUINewButtonBackground(getState(), 13, 128 - 20);
		overlayB = new GUINewButtonBackground(getState(), 13, 128 - 20);

		background = new GUINormalBackground(getState(), 300, 128);

		text.setPos(26, 8, 0);
		text.setColor(Color.white);
		text.setFont(FontSize.MEDIUM_15);

//		text.autoWrapOn = background;

		text.setTextSimple(new Object() {

			@Override
			public String toString() {
				return getMessage() == null ? "" : getMessage();
			}

		});
		text.onInit();

		text.draw();
		background.setColor(new Vector4f(1, 1, 1, 1));
		overlayA.setColor(new Vector4f(1, 1, 1, 1));
		overlayB.setColor(new Vector4f(1, 1, 1, 1));
		background.onInit();
		overlayB.onInit();
		overlayA.onInit();

		background.attach(overlayA);
		background.attach(overlayB);
		background.attach(text);
		currentIndex = this.index * ((getHeight()) + 5) + getDistFromTop();
		currentIndexX = (1.0f - popupTime / popoutTimeInSec) * (getWidth() - stackedDistanceX);

		firstDraw = false;
	}

	@Override
	public float getHeight() {

		return background != null ? background.getHeight() + 1 : 0;
	}

	@Override
	public float getWidth() {
		return background.getWidth();
	}

	@Override
	public boolean isPositionCenter() {
				return false;
	}

	private float getDistFromTop() {
		if(isLeft()){
			
			return distFromTop+targetPanel.y;
		}
		if (!ancorOnRadarAndAdvBuildMode) {
			return distFromTop;
		} else {
			if (!shopActive() && !inventoryActive() && buildModeActive()) {

//				BuildToolsPanel buildToolsShip = ((GameClientState) getState()).getWorldDrawer().getGuiDrawer().getPlayerPanel().buildToolsShip;

				return (PlayerInteractionControlManager.isAdvancedBuildMode((GameClientState) getState()) ? BuildToolsPanel.HEIGHT : BuildToolsPanel.HEIGHT_UNEXP);
			} else {
				return ((GameClientState) getState()).getWorldDrawer().getGuiDrawer().getHud().getRadar().getPos().y +
						((GameClientState) getState()).getWorldDrawer().getGuiDrawer().getHud().getRadar().getHeight() + distFromTop;
			}

		}
	}

	public ShipControllerManager getShipControllerManager() {
		return ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager();
	}

	public SegmentControlManager getSegmentControlManager() {
		return ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getSegmentControlManager();
	}

	public SegmentBuildController getActiveBuildController() {
		if (getSegmentControlManager().getSegmentBuildController().isTreeActive()) {
			return getSegmentControlManager().getSegmentBuildController();
		} else if (getShipControllerManager().getSegmentBuildController().isTreeActive()) {
			return getShipControllerManager().getSegmentBuildController();
		}
		return null;
	}

	private boolean buildModeActive() {
		return getActiveBuildController() != null;
	}

	private boolean inventoryActive() {
		return ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getInventoryControlManager().isTreeActive();
	}

	private boolean shopActive() {
		return ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getShopControlManager().isTreeActive();
	}

	private PlayerInteractionControlManager getInteractionManager() {
		return ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager()
				.getPlayerIntercationManager();
	}

	public boolean isExternalActive() {
		return getInteractionManager().getInShipControlManager().getShipControlManager().getShipExternalFlightController().isTreeActive()
				|| getInteractionManager().getSegmentControlManager().getSegmentExternalController().isTreeActive();
	}

	@Override
	public void update(Timer timer) {
		if (firstDraw) {
			return;
		}
		tu.update(timer);

		if (timeDelayed < timeDelayInSecs) {
			timeDelayed += timer.getDelta();
			return;
		}
		timeDrawn += timer.getDelta();

//		float targetYPos = this.index * ((getHeight()  ) + 5) + distFromTop;
		float targetYPos = currentHeight + getDistFromTop();

		if (popupTime == 0) {
			currentIndex = targetYPos;
			currentIndexX = (1.0f - popupTime / popoutTimeInSec) * (stackedDistanceX);
		} else {

			if (popupTime < popoutTimeInSec) {
				currentIndexX = (1.0f - popupTime / popoutTimeInSec) * (stackedDistanceX);
			} else {
				currentIndexX = 0;
			}

			float distSpeed = Math.min(1.0f, (Math.max(0.01f, Math.abs(currentIndex - targetYPos))) / (getHeight()));

			if (currentIndex > targetYPos) {
				currentIndex -= timer.getDelta() * 1000 * distSpeed;
				if (currentIndex <= targetYPos) {
					currentIndex = targetYPos;
				}
			} else if (currentIndex < targetYPos) {
				currentIndex += timer.getDelta() * 1000 * distSpeed;
				if (currentIndex >= targetYPos) {
					currentIndex = targetYPos;
				}
			}
		}
		popupTime += timer.getDelta();

	}

	/**
	 * @return the flashing
	 */
	public boolean isFlashing() {
		return flashing;
	}

	/**
	 * @param flashing the flashing to set
	 */
	@Override
	public void setFlashing(boolean flashing) {
		this.flashing = flashing;
	}

	/**
	 * starts a popup message with a delay
	 *
	 * @param timeDelayInSecs
	 */
	@Override
	public void startPopupMessage(float timeDelayInSecs) {
		this.timeDelayInSecs = timeDelayInSecs;
		timeDelayed = 0;
		timeDrawn = 0;
		popupTime = 0;
	}

	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public void setCurrentHeight(int height) {
		currentHeight = height;
	}

	/**
	 * @return the index
	 */
	public float getIndex() {
		return index;
	}

	/**
	 * @param index the index to set
	 */
	@Override
	public void setIndex(float index) {
		this.index = index;
	}

	@Override
	public boolean isAlive() {
		return timeDrawn < timeOutInSeconds;
	}

	/**
	 * @return the message
	 */
	@Override
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	@Override
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * restarts the popup message
	 * without delaying
	 * the restart
	 */
	@Override
	public void restartPopupMessage() {
		timeDrawn = 0;
	}

	@Override
	public void timeOut() {
		if (timeDrawn < timeOutInSeconds - 1) {
			timeDrawn = timeOutInSeconds - 1;
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
		return "PopupMessage";
	}

}
