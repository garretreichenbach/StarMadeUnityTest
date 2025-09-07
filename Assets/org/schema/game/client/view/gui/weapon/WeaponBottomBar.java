package org.schema.game.client.view.gui.weapon;

import java.io.IOException;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.game.client.controller.manager.ingame.ship.ShipExternalFlightController;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.HotbarInterface;
import org.schema.game.client.view.gui.shiphud.newhud.BottomBar;
import org.schema.game.client.view.gui.shiphud.newhud.GUIPosition;
import org.schema.game.client.view.gui.shiphud.newhud.PlayerHealthBar;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.SlotAssignment;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.ManagerActivityInterface;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ManagerModuleCollection;
import org.schema.game.common.controller.elements.ManagerReloadInterface;
import org.schema.game.common.controller.elements.ShipManagerContainer;
import org.schema.game.common.controller.elements.UsableControllableElementManager;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.BlockEffect;
import org.schema.game.common.data.blockeffects.StatusBlockEffect;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.font.unicode.Color;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIInnerBackground;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.input.Mouse;
import org.schema.schine.network.client.ClientState;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class WeaponBottomBar extends BottomBar implements HotbarInterface {

	@ConfigurationElement(name = "StartPosIconsX")
	public static int START_ICON_X;

	@ConfigurationElement(name = "StartPosIconsY")
	public static int START_ICON_Y;

	@ConfigurationElement(name = "IconSpacing")
	public static int ICON_SPACING = 70;

	@ConfigurationElement(name = "Color")
	public static Vector4i COLOR;

	@ConfigurationElement(name = "Position")
	public static GUIPosition POSITION;

	@ConfigurationElement(name = "Offset")
	public static Vector2f OFFSET;

	private final int barIndex;

	Vector3i absPosTmp = new Vector3i();

	int repairIconNum = -1;

	int jammingIconNum = -1;

	int cloakIconNum = -1;

	private WeaponSlotOverlayElement[] icons;

	private GUIOverlay selectIcon;

	private GUIOverlay background;

	private Vector3i tmpPos = new Vector3i();

	private SegmentPiece tmp = new SegmentPiece();

	private GUITextOverlay draggingIconText;

	private WeaponSlotOverlayElement draggingIcon;

	private GUITextOverlay barIndexText;

	private long selectTime;

	private int lastSelected = 0;

	private PlayerHealthBar playerHealth;

	private GUIAnchor bgIndexAnchor;

	private GUIInnerBackground bgIndex;

	private GUIOverlay upButton;

	private GUIOverlay downButton;

	private GUIOverlay reload;

	public WeaponBottomBar(InputState state, int barIndex) {
		super(state);
		this.barIndex = barIndex;
	}

	@Override
	public int getStartIconX() {
		return UIScale.getUIScale().scale(START_ICON_X);
	}

	@Override
	public int getStartIconY() {
		return UIScale.getUIScale().scale(START_ICON_Y);
	}

	@Override
	public int getIconSpacing() {
		return UIScale.getUIScale().scale(ICON_SPACING);
	}

	private GUITextOverlay initializeTextIcon(FontInterface font, InputState state) {
		GUITextOverlay icon = new GUITextOverlay(font, state);
		icon.setColor(Color.white);
		icon.setTextSimple(Lng.str("undefined"));
		icon.setPos(UIScale.getUIScale().smallinset, UIScale.getUIScale().smallinset, 0);
		return icon;
	}

	@Override
	public void activateDragging(boolean active) {
		for (int i = 0; i < 10; i++) {
			icons[i].setMouseUpdateEnabled(active);
		}
	}

	@Override
	public void drawDragging(WeaponSlotOverlayElement e) {
		GUIElement.enableOrthogonal();
		draggingIcon.setPos(Mouse.getX() - e.getDragPosX(), Mouse.getY() - e.getDragPosY(), 0);
		draggingIcon.setType(e.getType(), e.getPosIndex());
		draggingIcon.setSpriteSubIndex(ElementKeyMap.getInfo(e.getType()).getBuildIconNum());
		draggingIconText.getText().set(0, ElementKeyMap.toString(e.getType()));
		draggingIcon.draw();
		GUIElement.disableOrthogonal();
	}

	private Vector3i tmp1 = new Vector3i();

	private Vector3i tmp2 = new Vector3i();

	private long lastErrorMsg;

	private static Vector4f fadedTint = new Vector4f(1, 1, 1, 0.3f);

	private void checkController(SegmentController s, long pos, float startPointX, final int slot, float spacing, float yModifier, boolean inCore) {
		if (Ship.core.equals(pos)) {
			return;
		}
		short x = (short) ElementCollection.getPosX(pos);
		short y = (short) ElementCollection.getPosY(pos);
		short z = (short) ElementCollection.getPosZ(pos);
		int arrayIndex = slot % 10;
		SegmentPiece point = null;
		PlayerUsableInterface playerUsable = null;
		ManagerContainer<?> managerContainer = ((ManagedSegmentController<?>) s).getManagerContainer();
		if (pos > PlayerUsableInterface.MIN_USABLE) {
			point = s.getSegmentBuffer().getPointUnsave(pos, tmp);
		} else {
			playerUsable = managerContainer.getPlayerUsable(pos);
			if (playerUsable == null) {
				if (((ClientState) getState()).getUpdateTime() - lastErrorMsg > 5000) {
					System.err.println("[CLIENT] ERROR: MISSING PLAYER USABLE ON " + managerContainer.getSegmentController() + "; POS: " + pos);
					lastErrorMsg = ((ClientState) getState()).getUpdateTime();
				}
				return;
			}
			if (!playerUsable.isPlayerUsable()) {
				// disabled usable (e.g. from secondary reactor). draw faded out;
				short type = PlayerUsableInterface.ICONS.get(pos);
				if (!ElementKeyMap.isValidType(type)) {
					System.err.println("NO disabled ICON FOR " + pos + "; Check PlayerUsableInterface.ICONS map");
				} else {
					ElementInformation info = ElementKeyMap.getInfo(type);
					int icon = info.getBuildIconNum();
					icons[arrayIndex].getSprite().setTint(fadedTint);
					icons[arrayIndex].setType(type, pos);
					icons[arrayIndex].getPos().x = ((startPointX) + ((arrayIndex * spacing)));
					icons[arrayIndex].getPos().y = yModifier;
					icons[arrayIndex].setSpriteSubIndex(icon);
					icons[arrayIndex].draw();
					icons[arrayIndex].getSprite().setTint(null);
				}
				return;
			}
		}
		LongOpenHashSet coreCon = s.getControlElementMap().getControllingMap().getAll().get(ElementCollection.getIndex(Ship.core));
		if (point != null && ElementKeyMap.isValidType(point.getType()) && coreCon != null && ElementKeyMap.getInfo(point.getType()).needsCoreConnectionToWorkOnHotbar() && !coreCon.contains(ElementCollection.getIndex4(x, y, z, point.getType()))) {
			s.getSlotAssignment().removeBySlotAndSend((byte) slot);
			return;
		}
		final short type;
		if (point != null) {
			type = point.getType();
		} else {
			type = PlayerUsableInterface.ICONS.get(pos);
			if (!ElementKeyMap.isValidType(type)) {
				System.err.println("NO ICON FOR " + pos + "; Check PlayerUsableInterface.ICONS map");
			}
		}
		// System.err.println("USED ::: "+slot+" -> "+pos+": "+type+" -> "+ElementKeyMap.toString(type) );
		// System.err.println("PUSE "+playerUsable+"; "+point)
		;
		if (ElementKeyMap.isValidType(type)) {
			ElementInformation info = ElementKeyMap.getInfo(type);
			int icon = info.getBuildIconNum();
			icons[arrayIndex].setType(type, pos);
			icons[arrayIndex].getPos().x = ((startPointX) + ((arrayIndex * spacing)));
			icons[arrayIndex].getPos().y = yModifier;
			icons[arrayIndex].setSpriteSubIndex(icon);
			icons[arrayIndex].draw();
			if (point != null && point.getType() == ElementKeyMap.POWER_BATTERY && ((PowerManagerInterface) managerContainer).getPowerAddOn().isBatteryActive()) {
				int base = 2;
				int sprite = base;
				reload.setSpriteSubIndex(sprite);
				reload.getPos().y = yModifier;
				reload.getPos().x = startPointX + (arrayIndex * spacing);
				reload.draw();
			}
			if (point != null && point.getType() == ElementKeyMap.STEALTH_COMPUTER) {
				if (managerContainer.getSegmentController().isCloakedFor(null)) {
					int base = 2;
					int sprite = base;
					reload.setSpriteSubIndex(sprite);
					reload.getPos().y = yModifier;
					reload.getPos().x = startPointX + (arrayIndex * spacing);
					reload.draw();
				} else if (managerContainer instanceof ShipManagerContainer) {
					tmp1.set((int) (((startPointX) + ((arrayIndex * spacing)))), (int) yModifier, 0);
					tmp2.set(64, 64, 0);
					((ShipManagerContainer) managerContainer).getStealthElementManager().drawReloads(tmp1, tmp2, pos);
				}
			}
			ManagerModuleCollection<?, ?, ?> managerModuleCollection = managerContainer.getModulesControllerMap().get(type);
			if (managerModuleCollection != null && !managerModuleCollection.getCollectionManagers().isEmpty()) {
				if (managerModuleCollection.getElementManager() instanceof ManagerReloadInterface) {
					tmp1.set((int) (((startPointX) + ((arrayIndex * spacing)))), (int) yModifier, 0);
					tmp2.set(UIScale.getUIScale().scale(64), UIScale.getUIScale().scale(64), 0);
					((ManagerReloadInterface) managerModuleCollection.getElementManager()).drawReloads(tmp1, tmp2, pos);
				}
				/*
				 *display on/off state if needed (cloaker)
				 */
				if (managerModuleCollection.getElementManager() instanceof ManagerActivityInterface) {
					if (((ManagerActivityInterface) managerModuleCollection.getElementManager()).isActive()) {
						int base = 2;
						int sprite = base;
						reload.setSpriteSubIndex(sprite);
						reload.getPos().y = yModifier;
						reload.getPos().x = startPointX + (arrayIndex * spacing);
						reload.draw();
					}
				}
				ControlBlockElementCollectionManager<?, ?, ?> collection = ((UsableControllableElementManager<?, ?, ?>) managerModuleCollection.getElementManager()).getCollectionManagersMap().get(pos);
				if (collection != null && collection instanceof ManagerActivityInterface) {
					if (((ManagerActivityInterface) collection).isActive()) {
						int base = 2;
						int sprite = base;
						reload.setSpriteSubIndex(sprite);
						reload.getPos().y = yModifier;
						reload.getPos().x = startPointX + (arrayIndex * spacing);
						reload.draw();
					}
				}
				if (collection != null && collection.getSlaveConnectedElement() != Long.MIN_VALUE) {
					icons[arrayIndex].setSlaveType((short) ElementCollection.getType(collection.getSlaveConnectedElement()));
				} else {
					icons[arrayIndex].setSlaveType((short) 0);
				}
				if (collection != null && collection.getEffectConnectedElement() != Long.MIN_VALUE) {
					icons[arrayIndex].setEffectType((short) ElementCollection.getType(collection.getEffectConnectedElement()));
				} else {
					icons[arrayIndex].setEffectType((short) 0);
				}
			} else {
				icons[arrayIndex].setEffectType((short) 0);
				icons[arrayIndex].setSlaveType((short) 0);
			}
			if (playerUsable != null) {
				ManagerReloadInterface reloadInterface = playerUsable.getReloadInterface();
				if (reloadInterface != null) {
					reloadInterface.drawReloads(new Vector3i(((startPointX) + ((arrayIndex * spacing))), yModifier, 0.0F), new Vector3i(64.0F, 64.0F, 0.0F), pos);
				}
				ManagerActivityInterface activityInterface = playerUsable.getActivityInterface();
				if (activityInterface != null && activityInterface.isActive()) {
					int base = 2;
					int sprite = base;
					reload.setSpriteSubIndex(sprite);
					reload.getPos().y = yModifier;
					reload.getPos().x = startPointX + (arrayIndex * spacing);
					reload.draw();
				}
			}
			/*
			 *display on/off state of inner remote
			 */
			if (point != null && type == ElementKeyMap.LOGIC_REMOTE_INNER) {
				if (point.isActive()) {
					int base = 2;
					int sprite = base;
					reload.setSpriteSubIndex(sprite);
					reload.getPos().y = yModifier;
					reload.getPos().x = startPointX + (arrayIndex * spacing);
					reload.draw();
				}
			}
		} else {
			if (getState().getController().getInputController().getDragging() != null && getState().getController().getInputController().getDragging() instanceof WeaponSlotOverlayElement && ((WeaponSlotOverlayElement) getState().getController().getInputController().getDragging()).getSlotStyle() == WeaponSlotOverlayElement.SLOT_NORMAL) {
				icons[arrayIndex].drawHighlight();
			}
			GlUtil.glPushMatrix();
			icons[arrayIndex].setInside(false);
			icons[arrayIndex].transform();
			icons[arrayIndex].setType((short) 0, Long.MIN_VALUE);
			icons[arrayIndex].setEffectType((short) 0);
			icons[arrayIndex].setSlaveType((short) 0);
			icons[arrayIndex].checkMouseInside();
			GlUtil.glPopMatrix();
		}
	}

	@Override
	public void cleanUp() {
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIOverlay#draw(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void draw() {
		ShipExternalFlightController flightController = ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getShipExternalFlightController();
		GameClientState state = (GameClientState) getState();
		if (flightController == null || flightController.getShip() == null) {
			System.err.println("[WEAPON-SIDE-BAR] NO CONTROLLER DRAW");
			return;
		}
		if (flightController.getEntered() == null) {
			System.err.println("[WEAPON-SIDE-BAR] NO CONTROLLER DRAW. nothing entered");
			return;
		}
		GlUtil.glPushMatrix();
		Ship ship = flightController.getShip();
		transform();
		float startPointX = getStartIconX();
		float startPointY = getStartIconY();
		float spacing = getIconSpacing();
		background.draw();
		if (flightController.getEntered(tmpPos).equals(Ship.core)) {
			try {
				drawFromCore(ship, startPointX, spacing, startPointY);
				drawFixedTooltipsFromCore(ship, startPointX, spacing, startPointY);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		} else {
			try {
				drawFromEntered(ship, startPointX, spacing, flightController.getEntered().getAbsoluteIndex(), startPointY);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		drawEffects(flightController.getShip());
		playerHealth.setPos(UIScale.getUIScale().scale((int) PlayerHealthBar.OFFSET.x), UIScale.getUIScale().scale((int) PlayerHealthBar.OFFSET.y));
		playerHealth.draw();
		icons[0].getSprite().setSelectedMultiSprite(0);
		bgIndexAnchor.getPos().x = background.getWidth() - UIScale.getUIScale().scale(157);
		bgIndexAnchor.getPos().y = UIScale.getUIScale().scale(50);
		bgIndexAnchor.draw();
		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
		icons = new WeaponSlotOverlayElement[10];
		float startPointX = getStartIconX();
		float startPointY = getStartIconY();
		float spacing = getIconSpacing();
		barIndexText = new GUITextOverlay(FontSize.BIG_20, getState());
		barIndexText.setTextSimple(String.valueOf((barIndex + 1)));
		barIndexText.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().scale(28), 0);
		draggingIconText = initializeTextIcon(FontSize.MEDIUM_18, getState());
		draggingIcon = new WeaponSlotOverlayElement(getState());
		draggingIcon.attach(draggingIconText);
		draggingIcon.setScale(1, 1, 1);
		bgIndexAnchor = new GUIAnchor(getState(), UIScale.getUIScale().scale(18) + (((int) Math.log10(barIndex + 1)) * UIScale.getUIScale().scale(11)), UIScale.getUIScale().scale(77));
		bgIndex = new GUIInnerBackground(getState(), bgIndexAnchor, 0);
		bgIndexAnchor.attach(bgIndex);
		bgIndex.attach(barIndexText);
		String p = getState().getGUIPath();
		this.upButton = new GUIOverlay(Controller.getResLoader().getSprite(p + "UI 16px-8x8-gui-"), getState());
		this.downButton = new GUIOverlay(Controller.getResLoader().getSprite(p + "UI 16px-8x8-gui-"), getState());
		this.upButton.setSpriteSubIndex(4);
		this.downButton.setSpriteSubIndex(5);
		bgIndex.attach(upButton);
		bgIndex.attach(downButton);
		upButton.setPos(UIScale.getUIScale().scale(1), 0, 0);
		downButton.setPos(UIScale.getUIScale().scale(1), bgIndexAnchor.getHeight() - downButton.getHeight(), 0);
		for (int i = 0; i < icons.length; i++) {
			icons[i] = new WeaponSlotOverlayElement(getState());
			icons[i].setSlot(barIndex * 10 + i);
			icons[i].getPos().x = startPointX + (i * spacing);
			icons[i].getPos().y = startPointY;
		}
		// new GUIOverlay(Controller.getResLoader().getSprite(getState().getGUIPath()+"tools-16x16-gui-"), getState());
		reload = new GUIOverlay(Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath() + "HUD_Hotbar-8x2-gui-"), getState());
		background = new GUIOverlay(Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath() + "HUD_Hotbar-gui-"), getState());
		// GUIColoredRectangle(getState(), 64f, 64f, new Vector4f(1,1,1,0.18f));
		selectIcon = new GUIOverlay(Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath() + "HUD_Hotbar-8x2-gui-"), getState());
		selectIcon.setSpriteSubIndex(1);
		playerHealth = new PlayerHealthBar(getState());
		background.onInit();
		for (int i = 0; i < icons.length; i++) {
			icons[i].onInit();
		}
		upButton.setMouseUpdateEnabled(true);
		downButton.setMouseUpdateEnabled(true);
		upButton.setCallback(new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(737);
					((GameClientState) getState()).getWorldDrawer().getGuiDrawer().getPlayerPanel().modSelectedWeaponBottomBar(1);
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
		downButton.setCallback(new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(738);
					((GameClientState) getState()).getWorldDrawer().getGuiDrawer().getPlayerPanel().modSelectedWeaponBottomBar(-1);
				}
			}
		});
		upButton.onInit();
		downButton.onInit();
		playerHealth.onInit();
		barIndexText.onInit();
		selectIcon.onInit();
	// reload.onInit();
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

	public void drawEffects(SendableSegmentController c) {
		GameClientState state = (GameClientState) getState();
		ObjectOpenHashSet<BlockEffect> activeEffects = c.getBlockEffectManager().getActiveEffectsSet();
		for (BlockEffect b : activeEffects) {
			// System.err.println("ACTIVE: "+b);
			if (b.getBlockAndTypeId4() != Long.MIN_VALUE) {
				if (b instanceof StatusBlockEffect) {
					state.getController().popupGameTextMessage(Lng.str("Something is affecting you:\n<%s>\nEffect Efficiency: %s%%\n<to be replaced by icon>", b.getType().getName(), StringTools.formatPointZero(((StatusBlockEffect) b).getRatio(c.getBlockEffectManager().status) * 100f)), 0);
				} else {
					state.getController().popupGameTextMessage(Lng.str("Something is affecting you:\n<%s>\n", b.getType().getName()), 0);
				}
				/*
				 * display state of on/off effect
				 */
				for (int i = 0; i < 10; i++) {
					if (icons[i].getPosIndex() == ElementCollection.getPosIndexFrom4(b.getBlockAndTypeId4())) {
						int base = 2;
						int sprite = base;
						reload.setSpriteSubIndex(sprite);
						reload.setPos(icons[i].getPos());
						reload.draw();
					}
				}
			} else {
				if (b.getMessage() != null && !b.isMessageDisplayed()) {
					if (c.isClientOwnObject() && ((GameClientState) c.getState()).getWorldDrawer() != null) {
						((GameClientState) c.getState()).getWorldDrawer().getGuiDrawer().notifyEffectHit(c, b.getMessage());
						b.setMessageDisplayed(System.currentTimeMillis());
					}
				}
			}
		}
	}

	private void drawFixedTooltipsFromCore(Ship ship, float startPointX, float spacing, float yModifier) throws IOException {
		PlayerState player = ((GameClientState) getState()).getPlayer();
		for (int slotPos = 0; slotPos < 10; slotPos++) {
			if (slotPos == player.getCurrentShipControllerSlot() % 10) {
				long t = System.currentTimeMillis() - selectTime;
				GlUtil.glPushMatrix();
				icons[slotPos].drawToolTipFixed(t, 1200);
				GlUtil.glPopMatrix();
			}
		}
	}

	private void drawFromCore(Ship ship, float startPointX, float spacing, float yModifier) throws IOException {
		PlayerState player = ((GameClientState) getState()).getPlayer();
		SlotAssignment shipConfiguration = ship.getSlotAssignment();
		for (int slotPos = barIndex * 10; slotPos < barIndex * 10 + 10; slotPos++) {
			int arrayIndex = slotPos % 10;
			long shipConfigSlot = shipConfiguration.getAsIndex(slotPos);
			GlUtil.glPushMatrix();
			if (slotPos == player.getCurrentShipControllerSlot()) {
				GlUtil.glPushMatrix();
				if (lastSelected != slotPos) {
					selectTime = System.currentTimeMillis();
					lastSelected = slotPos;
				}
				selectIcon.getPos().set(icons[arrayIndex].getPos());
				selectIcon.getScale().set(icons[arrayIndex].getScale());
				selectIcon.draw();
				GlUtil.glPopMatrix();
			}
			GlUtil.glPopMatrix();
			if (shipConfigSlot == Long.MIN_VALUE) {
				if (getState().getController().getInputController().getDragging() != null && getState().getController().getInputController().getDragging() instanceof WeaponSlotOverlayElement && ((WeaponSlotOverlayElement) getState().getController().getInputController().getDragging()).getSlotStyle() == WeaponSlotOverlayElement.SLOT_NORMAL) {
					GlUtil.glPushMatrix();
					icons[arrayIndex].drawHighlight();
					GlUtil.glPopMatrix();
				}
				GlUtil.glPushMatrix();
				icons[arrayIndex].setInside(false);
				icons[arrayIndex].transform();
				icons[arrayIndex].setType((short) 0, Long.MIN_VALUE);
				icons[arrayIndex].setEffectType((short) 0);
				icons[arrayIndex].setSlaveType((short) 0);
				icons[arrayIndex].checkMouseInside();
				GlUtil.glPopMatrix();
			} else {
				GlUtil.glPushMatrix();
				// checkCore(ship, shipConfigSlot, startPointX, slotPos, spacing, yModifier);
				checkController(ship, shipConfigSlot, startPointX, slotPos, spacing, yModifier, true);
				GlUtil.glPopMatrix();
			}
		}
	}

	private void drawFromEntered(Ship ship, float startPointX, float spacing, long entered, float yModifier) throws IOException {
		checkController(ship, entered, startPointX, 0, spacing, yModifier, false);
	// checkWeapons(ship, entered, startPointX, 0, spacing, yModifier);
	// checkSalvageControllers(ship, entered, startPointX, 0, spacing, yModifier);
	// checkRepairControllers(ship, entered, startPointX, 0, spacing, yModifier);
	// checkPowerDrainControllers(ship, entered, startPointX, 0, spacing, yModifier);
	// checkPowerSupplyControllers(ship, entered, startPointX, 0, spacing, yModifier);
	// checkMissiles(ship, entered, startPointX, 0, spacing, yModifier, ElementKeyMap.MISSILE_DUMB_CONTROLLER_ID, ElementKeyMap.MISSILE_DUMB_ID, ship.getManagerContainer().getDumbMissile().getCollectionManagers());
	// checkMissiles(ship, entered, startPointX, 0, spacing, yModifier, ElementKeyMap.MISSILE_HEAT_CONTROLLER_ID, ElementKeyMap.MISSILE_HEAT_ID, ship.getManagerContainer().getHeatMissile().getCollectionManagers());
	// checkMissiles(ship, entered, startPointX, 0, spacing, yModifier, ElementKeyMap.MISSILE_FAFO_CONTROLLER_ID, ElementKeyMap.MISSILE_FAFO_ID, ship.getManagerContainer().getFafoMissile().getCollectionManagers());
	}

	@Override
	public void drawToolTip() {
		GlUtil.glPushMatrix();
		for (int i = 0; i < icons.length; i++) {
			icons[i].drawToolTip();
		}
		GlUtil.glPopMatrix();
	}

	@Override
	public void update(Timer timer) {
		super.update(timer);
	}

	@Override
	public Vector4i getConfigColor() {
		return COLOR;
	}

	@Override
	public GUIPosition getConfigPosition() {
		return POSITION;
	}

	@Override
	public Vector2f getConfigOffset() {
		return OFFSET;
	}

	@Override
	protected String getTag() {
		return "BottomBarWeapon";
	}
}
