package org.schema.game.client.view.gui;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.schema.common.TimeStatistics;
import org.schema.game.client.controller.GUIController;
import org.schema.game.client.controller.PlayerInput;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.controller.manager.HelpManager;
import org.schema.game.client.controller.manager.ingame.ChatControlManager;
import org.schema.game.client.controller.manager.ingame.InventoryControllerManager;
import org.schema.game.client.controller.manager.ingame.PlayerGameControlManager;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.controller.manager.ingame.ship.ShipExternalFlightController;
import org.schema.game.client.controller.manager.ingame.shop.ShopControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.chat.ChatPanel;
import org.schema.game.client.view.gui.fleet.FleetQuickMenuDialog;
import org.schema.game.client.view.gui.newgui.GUIBlockNamePanel;
import org.schema.game.client.view.gui.playerstats.PlayerStatisticsPanelNew;
import org.schema.game.client.view.gui.shiphud.newhud.Hud;
import org.schema.game.client.view.gui.shiphud.newhud.PopupMessageNew;
import org.schema.game.client.view.gui.tutorial.GUITutorialControls;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.ShieldContainerInterface;
import org.schema.game.common.controller.elements.effectblock.EffectElementManager.OffensiveEffects;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.camera.CameraMouseState;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUITintScreenElement;
import org.schema.schine.graphicsengine.forms.gui.GUIToolTip;
import org.schema.schine.graphicsengine.forms.gui.newgui.DialogInterface;
import org.schema.schine.graphicsengine.movie.subtitles.Subtitle;
import org.schema.schine.input.BasicInputController;
import org.schema.schine.input.Keyboard;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.input.Mouse;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.sound.controller.AudioController;

import javax.vecmath.Vector3f;
import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.List;

public class GuiDrawer implements Drawable, ZSortedDrawable {

	public static final int GUI_MODE_NONE = 0;

	public static final int GUI_MODE_INGAME = 1;

	public static final int GUI_MODE_INVENTORY = 2;

	public static final int GUI_MODE_SHOP = 4;

	public static final int GUI_MODE_CHAT = 8;

	public static final int GUI_MODE_CONTROLLER_MANAGER = 16;

	public static final int GUI_HELP_MANAGER = 32;

	public static final int GUI_MODE_SHIP_FLIGHT = 64;

	public final LinkedList<GUIPopupInterface> popupMessages = new LinkedList<GUIPopupInterface>();

	public final LinkedList<BigTitleMessage> titleMessages = new LinkedList<BigTitleMessage>();

	public final LinkedList<BigMessage> bigMessages = new LinkedList<BigMessage>();

	public GUIBlockNamePanel inViewPopup;

	private PlayerPanel playerPanel;

	private GameClientState state;

	private GUIController guiController;

	private PlayerStatisticsPanelNew playerStatisticsPanel;

	private GUITextOverlay detachedInfo;

	private GUITintScreenElement screenTint;

	private float hurtDir = 1;

	private float hurtDuration = 0.2f;

	private float hurtIntensity = 0.3f;

	private float hurtTime = -1;

	private ShipOrientationElement shipOrientationElement;

	private int guiMode = GUI_MODE_INGAME;

	private Hud hud;

	private final List<GUITextOverlay> subtitleOverlays = new ObjectArrayList<GUITextOverlay>();

	private GUITutorialControls tutorialControls;

	private boolean drawHud;

	private RadialMenuDialog currentRadialMenu;
	private int beforeRadialMouseX;

	private int beforeRadialMouseY;

	private boolean wasDownRadialButton;
	private boolean wasFleetQuickRadialDownButton;

	public GuiDrawer(GUIController guiController) {
		this.state = guiController.getState();
		this.guiController = guiController;
		this.guiController.deleteObservers();
		// reset gui drawing to true...
		EngineSettings.G_DRAW_GUI_ACTIVE.setOn(true);
	}

	private boolean aiActive() {
		return state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getAiConfigurationManager().isTreeActive();
	}

	private boolean catalogActive() {
		return state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getCatalogControlManager().isTreeActive();
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public void draw() {
		// if(GLFrame.isScreenSettingChanging()){
		// doOrientation();
		// }
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		/*
		 * WARNING: GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT) is a dangerous call.
		 * But it's needed to draw the gui on top of everything. Make sure that
		 * nothing is drawn afterwards that needs to reference the cleared depth
		 * buffer
		 */
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		if (EngineSettings.G_DRAW_NO_OVERLAYS.isOn()) {
			hud.resetDrawnHUDAtAll();
			return;
		}
		if (EngineSettings.G_DRAW_GUI_ACTIVE.isOn()) {
			drawScreenTint();
			if (shipBuildActive()) {
				shipOrientationElement.draw();
			}
			drawHud = !(shopActive() || inventoryActive() || chatManagerActive() || aiActive() || shipControllerManagerActive() || navigationActive() || factionActive() || catalogActive() || mapActive() || structureControllerActive());
			GUIElement.enableOrthogonal();
			if (drawHud) {
				TimeStatistics.reset("HUD");
				hud.draw();
				TimeStatistics.set("HUD");
			} else {
				hud.resetDrawnHUDAtAll();
				if (mapActive()) {
					hud.getIndicator().drawMapIndications();
				}
			}
//			state.getController().getTutorialController().drawBackgroundPlayer();
			if (!state.getGlobalGameControlManager().getIngameControlManager().getAutoRoamController().isActive()) {
				playerPanel.draw();
			}
			GUIElement.disableOrthogonal();
		} else {
			hud.resetDrawnHUDAtAll();
		}
		GUIElement.enableOrthogonal();
		BasicInputController ic = state.getController().getInputController();
		synchronized (state.getPlayerInputs()) {
			for (int i = 0; i < state.getPlayerInputs().size(); i++) {
				state.getPlayerInputs().get(i).getInputPanel().draw();
				if (ic.getCurrentActiveDropdown() != null) {
					ic.getCurrentActiveDropdown().drawExpanded();
				}
			}
		}
		{
			GUIElement.deactivateCallbacks = true;
			for (int i = 0; i < ic.getDeactivatedPlayerInputs().size(); i++) {
				DialogInterface inputPanel = ic.getDeactivatedPlayerInputs().get(i);
				;
				inputPanel.updateDeacivated();
				inputPanel.getInputPanel().draw();
				if (System.currentTimeMillis() - inputPanel.getDeactivationTime() > PlayerInput.drawDeactivatedTime) {
					ic.getDeactivatedPlayerInputs().get(i).getInputPanel().cleanUp();
					ic.getDeactivatedPlayerInputs().remove(i);
					i--;
				}
			}
			GUIElement.deactivateCallbacks = false;
		}
		if (!state.getGlobalGameControlManager().getIngameControlManager().getChatControlManager().isActive() && KeyboardMappings.PLAYER_LIST.isDown()) {
			if (state.getPlayerInputs().isEmpty()) {
				playerStatisticsPanel.draw();
			}
		}
		if (!wasDownRadialButton&& !Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
			if (KeyboardMappings.RADIAL_MENU.isDown() && !state.isInTextBox()) {
				if (!(currentRadialMenu instanceof RadialMenuDialogMain) ) {
					beforeRadialMouseX = Mouse.getX();
					beforeRadialMouseY = Mouse.getY();
					Mouse.setCursorPosition(GLFrame.getWidth() / 2, GLFrame.getHeight() / 2);
					currentRadialMenu = new RadialMenuDialogMain(state);
					currentRadialMenu.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(503);
				}
			} else {
				state.setInTextBox(false);
				if (currentRadialMenu instanceof RadialMenuDialogMain) {
					if (state.getPlayerInputs().contains(currentRadialMenu)) {
						currentRadialMenu.activateSelected();
						currentRadialMenu.deactivate();
					}
					currentRadialMenu = null;
					if (isInAdvBuildMode()) Mouse.setCursorPosition(beforeRadialMouseX, beforeRadialMouseY);
				}
			}
		}
		wasDownRadialButton = KeyboardMappings.RADIAL_MENU.isDown() && !Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL);

		if(!wasFleetQuickRadialDownButton && Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
			if(KeyboardMappings.RADIAL_MENU.isDown() && !state.isInTextBox()) {
				if(!(currentRadialMenu instanceof FleetQuickMenuDialog)) {
					beforeRadialMouseX = Mouse.getX();
					beforeRadialMouseY = Mouse.getY();
					Mouse.setCursorPosition(GLFrame.getWidth() / 2, GLFrame.getHeight() / 2);
					currentRadialMenu = new FleetQuickMenuDialog();
					currentRadialMenu.activate();
				}
			} else {
				state.setInTextBox(false);
				if(currentRadialMenu instanceof FleetQuickMenuDialog) {
					if(state.getPlayerInputs().contains(currentRadialMenu)) {
						currentRadialMenu.activateSelected();
						currentRadialMenu.deactivate();
					}
					currentRadialMenu = null;
					if(isInAdvBuildMode()) Mouse.setCursorPosition(beforeRadialMouseX, beforeRadialMouseY);
				}
			}
		}
		wasFleetQuickRadialDownButton = KeyboardMappings.RADIAL_MENU.isDown() && Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL);
		//draw chat always
//		chat.draw();

		GUIElement.disableOrthogonal();
		if (EngineSettings.G_DRAW_POPUPS.isOn()) {
			if (shipBuildActive()) {
				GUIElement.enableOrthogonal();
				inViewPopup.draw();
				GUIElement.disableOrthogonal();
			}
			synchronized (popupMessages) {
				for (int i = 0; i < popupMessages.size(); i++) {
					GUIElement.enableOrthogonal();
					popupMessages.get(i).draw();
					GUIElement.disableOrthogonal();
				}
				PopupMessageNew.targetPanel.set(0, 0);
			}
			synchronized (titleMessages) {
				for (int i = 0; i < titleMessages.size(); i++) {
					GUIElement.enableOrthogonal();
					titleMessages.get(i).draw();
					GUIElement.disableOrthogonal();
				}
			}
			synchronized (bigMessages) {
				for (int i = 0; i < bigMessages.size(); i++) {
					GUIElement.enableOrthogonal();
					bigMessages.get(i).draw();
					GUIElement.disableOrthogonal();
				}
			}
		}
		// boolean tut = EngineSettings.S_TUTORIAL.isOn() && state.getGlobalGameControlManager().getMainMenuManager().isActive();
		// if(tut && state.getController().getTutorialMode() != null && state.getController().isTutorialStarted()){
		// GUIElement.enableOrthogonal();
		// tutorialControls.draw( );
		// GUIElement.disableOrthogonal();
		// }
		if (CameraMouseState.ungrabForced) {
			GUIElement.enableOrthogonal();
			detachedInfo.draw();
			GUIElement.disableOrthogonal();
		}
		GUIElement.enableOrthogonal();
		state.getController().getInputController().drawDropdownAndContext();
		GUIElement.disableOrthogonal();
		drawSubtitles();
		playerPanel.afterGUIDraw();
	}

	@Override
	public boolean isInvisible() {
		return false;
	}

	@Override
	public void onInit() {
		screenTint = new GUITintScreenElement(state);
		screenTint.getColor().set(1, 0, 0, 0);
		screenTint.setBorder(35);
		screenTint.onInit();
		inViewPopup = new GUIBlockNamePanel(state);
		inViewPopup.onInit();
		// state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getShipControllerManagerManager().addObserver(controllerManagerPanel);
		playerStatisticsPanel = new PlayerStatisticsPanelNew(state);
		playerStatisticsPanel.onInit();
		hud = new Hud(state);
		hud.onInit();
		playerPanel = new PlayerPanel(state);
		playerPanel.onInit();
		tutorialControls = new GUITutorialControls(state);
		tutorialControls.orientate(GUIElement.ORIENTATION_BOTTOM | GUIElement.ORIENTATION_RIGHT);
		shipOrientationElement = new ShipOrientationElement(state);
		shipOrientationElement.onInit();
		detachedInfo = new GUITextOverlay(FontSize.BIG_24, state);
		detachedInfo.setTextSimple(Lng.str("Detached Mouse From Game. Click to reattach."));
		doOrientation();
	}

	@Override
	public int compareTo(ZSortedDrawable arg0) {
		// allways in front
		return Integer.MAX_VALUE;
	}

	public void doOrientation() {
		playerStatisticsPanel.orientate(GUIElement.ORIENTATION_HORIZONTAL_MIDDLE | GUIElement.ORIENTATION_VERTICAL_MIDDLE);
		detachedInfo.orientate(GUIElement.ORIENTATION_HORIZONTAL_MIDDLE | GUIElement.ORIENTATION_VERTICAL_MIDDLE);
		hud.orientate(GUIElement.ORIENTATION_HORIZONTAL_MIDDLE | GUIElement.ORIENTATION_VERTICAL_MIDDLE);
	}

	private void drawScreenTint() {
		if (hurtTime < hurtDuration) {
			screenTint.getColor().w = (hurtTime / hurtDuration) * hurtIntensity;
			GUIElement.enableOrthogonal();
			screenTint.draw();
			GUIElement.disableOrthogonal();
		}
	}

	@Override
	public void drawZSorted() {
		AbstractScene.zSortedMap.add(this);
	}

	@Override
	public FloatBuffer getBufferedTransformation() {
		return null;
	}

	@Override
	public Vector3f getBufferedTransformationPosition() {
		return null;
	}

	private boolean factionActive() {
		return state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getFactionControlManager().isTreeActive();
	}

	public PlayerGameControlManager getPlayerInteractionManager() {
		return state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager();
	}

	private boolean structureControllerActive() {
		return getPlayerInteractionManager().getStructureControlManager().isTreeActive();
	}

	// private boolean inGameActive(){
	// return state.getGlobalGameControlManager().getIngameControlManager().isTreeActive();
	// }
	/**
	 * @return the playerPanel
	 */
	public PlayerPanel getPlayerPanel() {
		return playerPanel;
	}

	public void handleMode(int mode, AbstractControlManager o) {
		int add = mode;
		if ((guiMode & add) == add) {
			add = 0;
		}
		// System.err.println("CONTROLLRT GUI: "+o.getClass().getSimpleName()+": "+o.isTreeActive());
		guiMode += o.isTreeActive() ? add : -mode;
	}

	private boolean inventoryActive() {
		return state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getInventoryControlManager().isTreeActive();
	}

	public boolean isMouseOnPanel() {
		return playerPanel.isMouseOnPanel();
	}

	public void managerChanged(ElementCollectionManager<?, ?, ?> man) {
		if (playerPanel != null) {
			playerPanel.managerChanged(man);
		}
	}

	private boolean mapActive() {
		return state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getMapControlManager().isTreeActive();
	}

	private boolean navigationActive() {
		return state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getNavigationControlManager().isTreeActive();
	}

	public void onSectorChange() {
		hud.onSectorChange();
	}

	private boolean shipBuildActive() {
		return state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getSegmentBuildController().isTreeActive();
	}

	private boolean isInAdvBuildMode() {
		return PlayerInteractionControlManager.isAdvancedBuildMode(state);
	}

	private boolean shipControllerManagerActive() {
		return state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getWeaponControlManager().isActive();
	}

	private boolean chatManagerActive() {
		return state.getGlobalGameControlManager().getIngameControlManager().getChatControlManager().isActive();
	}

	private boolean shopActive() {
		return state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getShopControlManager().isTreeActive();
	}

	public void drawSubtitles() {
		if (state.getActiveSubtitles() != null && EngineSettings.SUBTITLES.isOn()) {
			GUIElement.enableOrthogonal();
			GlUtil.glPushMatrix();
			List<Subtitle> activeSubtitles = state.getActiveSubtitles();
			int p = 0;
			int yDist = 120;
			for (int i = activeSubtitles.size() - 1; i >= 0; i--) {
				Subtitle subtitle = activeSubtitles.get(i);
				String[] spl = subtitle.text.split("\n");
				for (int j = spl.length - 1; j >= 0; j--) {
					String st = spl[j];
					if (subtitleOverlays.size() == p) {
						subtitleOverlays.add(new GUITextOverlay(FontSize.BIG_30, state));
					}
					GUITextOverlay o = subtitleOverlays.get(p);
					o.setTextSimple(st);
					o.updateTextSize();
					o.getPos().x = GLFrame.getWidth() / 2 - o.getMaxLineWidth() / 2;
					o.getPos().y = GLFrame.getHeight() - (yDist + o.getTextHeight());
					o.draw();
					yDist += o.getTextHeight() + 6;
					p++;
				}
			}
			GlUtil.glPopMatrix();
			GUIElement.disableOrthogonal();
		}
	}

	public void startHurtAnimation(Sendable vessel) {
		if (hurtTime < 0) {
			hurtTime = 0;
			hurtDir = 1;
			if (vessel != null && vessel instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) vessel) instanceof ShieldContainerInterface && ((ShieldContainerInterface) ((ManagedSegmentController<?>) vessel)).getShieldAddOn().getShields() > 0) {
				screenTint.getColor().x = 0;
				screenTint.getColor().y = 0;
				screenTint.getColor().z = 1;
			} else {
				screenTint.getColor().x = 1;
				screenTint.getColor().y = 0;
				screenTint.getColor().z = 0;
			}
		}
	}

	public void notifySwitch(AbstractControlManager o) {
		if (playerPanel != null) {
			playerPanel.notifySwitch(o);
		}
		if (o instanceof ChatControlManager) {
			handleMode(GUI_MODE_CHAT, o);
		// if(chat != null){
		// System.err.println("ON SWITCH TEXT MODE");
		// chat.setInTextMode(((ChatControlManager)o).isActive());
		// }
		}
		if (o instanceof InventoryControllerManager) {
			handleMode(GUI_MODE_INVENTORY, o);
		}
		if (o instanceof ShipExternalFlightController) {
			handleMode(GUI_MODE_SHIP_FLIGHT, o);
		}
		if (o instanceof ShopControllerManager) {
			handleMode(GUI_MODE_SHOP, o);
		}
		if (o instanceof HelpManager) {
			handleMode(GUI_HELP_MANAGER, o);
		}
	}

	public void update(Timer timer) {
		if (hurtTime >= 0) {
			hurtTime += hurtDir * (timer.getDelta() * 2);
			if (hurtTime > hurtDuration) {
				hurtDir = -1;
			}
		} else {
			hurtDir = 1;
		}
		if (GUIToolTip.tooltipGraphics != null) {
			GUIToolTip.tooltipGraphics.update(timer);
		}
		if (inViewPopup != null) {
			inViewPopup.update(timer);
		}
		// if(Keyboard.isKeyDown(GLFW.GLFW_KEY_N)){
		// startHurtAnimation();
		// }
		if (!state.getGlobalGameControlManager().getIngameControlManager().getChatControlManager().isActive() && KeyboardMappings.PLAYER_LIST.isDown()) {
			playerStatisticsPanel.update(timer);
		}
		playerPanel.update(timer);
		hud.update(timer);
		GUI3DBlockElement.linearTimer.update(timer);
		synchronized (popupMessages) {
			int height = 0;
			for (int i = 0; i < popupMessages.size(); i++) {
				popupMessages.get(i).update(timer);
				if (!popupMessages.get(i).isAlive()) {
					popupMessages.remove(i);
					i--;
					continue;
				}
				popupMessages.get(i).setIndex(i);
				popupMessages.get(i).setCurrentHeight(height);
				height += popupMessages.get(i).getHeight();
			}
		}
		synchronized (titleMessages) {
			for (int i = 0; i < titleMessages.size(); i++) {
				titleMessages.get(i).update(timer);
				if (!titleMessages.get(i).isAlive()) {
					titleMessages.remove(i);
					i--;
					continue;
				}
				titleMessages.get(i).index = i;
			}
		}
		synchronized (bigMessages) {
			for (int i = 0; i < bigMessages.size(); i++) {
				bigMessages.get(i).update(timer);
				if (!bigMessages.get(i).isAlive()) {
					bigMessages.remove(i);
					i--;
					continue;
				}
				bigMessages.get(i).index = i;
			}
		}
	}

	public void notifyEffectHit(SimpleTransformableSendableObject obj, OffensiveEffects offensiveEffects) {
		playerPanel.getTopBar().notifyEffectHit(obj, offensiveEffects);
		hud.notifyEffectHit(obj, offensiveEffects);
	}

	/**
	 * @return the playerStatisticsPanel
	 */
	public PlayerStatisticsPanelNew getPlayerStatisticsPanel() {
		return playerStatisticsPanel;
	}

	/**
	 * @param playerStatisticsPanel the playerStatisticsPanel to set
	 */
	public void setPlayerStatisticsPanel(PlayerStatisticsPanelNew playerStatisticsPanel) {
		this.playerStatisticsPanel = playerStatisticsPanel;
	}

	/**
	 * @return the chatNew
	 */
	public ChatPanel getChatNew() {
		return playerPanel.getChat();
	}

	public Hud getHud() {
		return hud;
	}

	public void setHud(Hud hud) {
		this.hud = hud;
	}
}
