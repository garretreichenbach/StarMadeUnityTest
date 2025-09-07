package org.schema.game.client.view.gui.shiphud.newhud;

import java.util.List;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.lwjgl.glfw.GLFW;
import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.game.client.controller.manager.ingame.BlockSyleSubSlotController;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.controller.manager.ingame.SegmentBuildController;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.HotbarInterface;
import org.schema.game.client.view.gui.inventory.InventoryIconsNew;
import org.schema.game.client.view.gui.inventory.InventorySlotOverlayElement;
import org.schema.game.client.view.gui.inventory.inventorynew.InventoryPanelNew;
import org.schema.game.client.view.gui.shiphud.HudIndicatorOverlay;
import org.schema.game.client.view.gui.weapon.WeaponSlotOverlayElement;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.player.inventory.InventorySlot;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.font.unicode.Color;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.input.Keyboard;
import org.schema.schine.input.Mouse;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class BottomBarBuild extends HudConfig implements HotbarInterface {

	@ConfigurationElement(name = "Color")
	public static Vector4i COLOR;

	@ConfigurationElement(name = "Position")
	public static GUIPosition POSITION;

	@ConfigurationElement(name = "Offset")
	public static Vector2f OFFSET;

	@ConfigurationElement(name = "StartPosIconsX")
	public static int START_ICON_X;

	@ConfigurationElement(name = "StartPosIconsY")
	public static int START_ICON_Y;

	@ConfigurationElement(name = "IconSpacing")
	public static int ICON_SPACING = 70;

	private InventorySlotOverlayElement[] icons;
	private InventorySlotOverlayElement multiIconHelper;
	private GUITextOverlay[] iconsTexts;
	private GUIOverlay selectIcon;
	private GUIOverlay background;
	private int lastSelected;
	private long selectTime;
	private boolean mouseGlobalCheck = true;
	private PlayerHealthBar playerHealth;
	private InventoryPanelNew inventoryPanelNew;
	private GUIOverlay reload;
	private int lastSelectedSub;
	private Vector4f iconTint = new Vector4f();
	
	public BottomBarBuild(InputState state, InventoryPanelNew inventoryPanelNew) {
		super(state);
		icons = new InventorySlotOverlayElement[256];
		this.inventoryPanelNew = inventoryPanelNew;
		iconsTexts = new GUITextOverlay[256];

		for (int i = 0; i < icons.length; i++) {
			icons[i] = new InventorySlotOverlayElement(true, state, true, this);
			multiIconHelper = new InventorySlotOverlayElement(true, state, true, this);

			
			
			getIconsTexts()[i] = new GUITextOverlay(FontSize.MEDIUM_15, state);
			getIconsTexts()[i].setColor(Color.white);
			getIconsTexts()[i].setText(new ObjectArrayList<>());
			getIconsTexts()[i].getText().add("i " + i);
			getIconsTexts()[i].setPos(UIScale.getUIScale().smallinset, UIScale.getUIScale().smallinset, 0);
			icons[i].attach(getIconsTexts()[i]);
		}
		reload = new GUIOverlay(Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath()+"HUD_Hotbar-8x2-gui-"), state);//new GUIOverlay(Controller.getResLoader().getSprite(getState().getGUIPath()+"tools-16x16-gui-"), state);

		background = new GUIOverlay(Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath()+"HUD_Hotbar-gui-"), state);
		selectIcon = new GUIOverlay(Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath()+"HUD_Hotbar-8x2-gui-"), state);//new GUIOverlay(getState(), 64f, 64f, new Vector4f(1,1,1,0.18f));
		selectIcon.setSpriteSubIndex(1);

		playerHealth = new PlayerHealthBar(state);
	}

	public int getStartIconX() {
		return UIScale.getUIScale().scale(START_ICON_X);
	}

	public int getStartIconY() {
		return UIScale.getUIScale().scale(START_ICON_Y);
	}

	public int getIconSpacing() {
		return UIScale.getUIScale().scale(ICON_SPACING);
	}

	@Override
	public void activateDragging(boolean active) {
		for (int i = 0; i < 10; i++) {
			icons[i].setMouseUpdateEnabled(active);
		}
	}

	@Override
	public void drawDragging(WeaponSlotOverlayElement e) {
		//nothing to do here
	}

	@Override
	public void cleanUp() {

	}

	@Override
	public void draw() {
		//		if(!state.getGlobalGameControlManager().getIngameControlManager().getShipControlManager().isActive()){
		//			return;
		//		}

		GlUtil.glPushMatrix();
		SegmentBuildController buildController =
				((GameClientState) getState()).
						getGlobalGameControlManager().
						getIngameControlManager().
						getPlayerGameControlManager().
						getPlayerIntercationManager().
						getInShipControlManager().
						getShipControlManager().
						getSegmentBuildController();

		PlayerInteractionControlManager interaction =
				((GameClientState) getState()).
						getGlobalGameControlManager().
						getIngameControlManager().
						getPlayerGameControlManager().
						getPlayerIntercationManager();

		if (buildController == null) {
			return;
		}
		int selectedSlot = interaction.getSelectedSlot();
		int subSlot = interaction.getSelectedSubSlot();
		Inventory inventory = ((GameClientState) getState()).getPlayer().getInventory(null);
		transform();
		if (mouseGlobalCheck) {
			checkMouseInside();
		}

		float startPointX = getStartIconX();
		float startPointY = getStartIconY();

		background.draw();
		BlockSyleSubSlotController bs = ((GameClientState)getState()).getBlockSyleSubSlotController();
		for (int i = 0; i < Keyboard.slotKeys.length; i++) {
			icons[i].forcedType = 0;
			short type = 0;
			int buildIconNum = 511;

			int slot = Keyboard.getSlotKey(Keyboard.slotKeys[i]);
			int secondBuildIcon = -1;
			short secondType = -1;
			MetaObject object = null;
			short countType = 0;
			short drawType = 0;
			boolean useMultiSelectIcon = false;
			if (inventory.isSlotEmpty(slot)) {
				iconsTexts[i].getText().set(0, "");
			} else {
				type = inventory.getType(slot);
				
				ElementInformation info;
				
				if(slot == selectedSlot && ElementKeyMap.isValidType(type) && ((info = ElementKeyMap.getInfoFast(type)).blocktypeIds != null)){
					
					short[] selectedStack = bs.getSelectedStack(type);
					if(subSlot < selectedStack.length && subSlot == selectedStack.length-1){
						useMultiSelectIcon = true;
					}
					countType = type;
					
					
					if(interaction.getForcedSelect() != 0){
						useMultiSelectIcon = false;
						buildIconNum = ElementKeyMap.getInfo(interaction.getForcedSelect()).getBuildIconNum();
						icons[i].forcedType = interaction.getForcedSelect();
					}else{
						if(interaction.getSelectedSubSlot() >= bs.getSelectedStack(type).length){
							interaction.resetSubSlot();
						}
						short t = bs.getBlockType(type, interaction.getSelectedSubSlot());
						if(t > 0){
							useMultiSelectIcon = false;
							buildIconNum = ElementKeyMap.getInfo(t).getBuildIconNum();
							icons[i].forcedType = t;
						}
					}
					
				}else if (type < 0) {
					
					if (type == InventorySlot.MULTI_SLOT) {
						
					}else if (type == InventorySlot.MULTI_SLOT) {
					} else {
						//meta item
						buildIconNum = Math.abs(type);
						
						if(type < 0){
							object = ((GameClientState) getState()).getMetaObjectManager().getObject(inventory.getMeta(slot));
						}
						
						object = ((GameClientState) getState()).getMetaObjectManager().getObject(inventory.getMeta(slot));
						if (object != null && object.drawUsingReloadIcon()) {
							buildIconNum += object.getExtraBuildIconIndex();
						}
					}
				} else {
					buildIconNum = ElementKeyMap.getInfo(type).getBuildIconNum();
				}
			}

			int elementsInInv;
			
			if(countType <= 0){
				elementsInInv = inventory.getCount(slot, type);
			}else{
				elementsInInv = inventory.getCount(slot, countType);
			}
			
			if (type == InventorySlot.MULTI_SLOT) {
				List<InventorySlot> subSlots = new ObjectArrayList<InventorySlot>(inventory.getSubSlots(slot));

				for (int j = 1; j < subSlots.size(); j++) {
					InventorySlot s = subSlots.get(j);
					if (j == subSlot
							&& slot == selectedSlot) {
						//put selected first
						subSlots.set(j, subSlots.get(0));
						subSlots.set(0, s);
					}
				}

				for (int t = 1; t < subSlots.size() && t < 5; t++) {
					float x = (t - 1) % 2;
					float y = (t - 1) / 2;
					InventorySlot inventorySlot = subSlots.get(t);
					doDraw(ElementKeyMap.getInfo(inventorySlot.getType()).getBuildIconNum(), slot, inventorySlot.getType(), object, inventorySlot.count(), inventorySlot.count(), startPointX + x * 32, startPointY + y * 32, x * 44, y * 42, 0.5f, true, true, false, i, useMultiSelectIcon);
				}
				InventorySlot inventorySlot = subSlots.get(0);
				//draw master block
				doDraw(ElementKeyMap.getInfo(inventorySlot.getType()).getBuildIconNum(), slot, inventorySlot.getType(), object, inventorySlot.count(), inventorySlot.count(), startPointX + 8, startPointY + 8, 0.75f, true, true, false, i, useMultiSelectIcon);
				//draw transparent for mouse grab
				doDraw(ElementKeyMap.getInfo(inventorySlot.getType()).getBuildIconNum(), slot, type, object, inventorySlot.count(), inventorySlot.count(), startPointX, startPointY, 1f, false, false, true, i, useMultiSelectIcon);
			} else if (secondBuildIcon >= 0) {
				doDraw(buildIconNum, slot, type, object, 0, elementsInInv, startPointX, startPointY, 1.0f, true, true, true, i, useMultiSelectIcon);
				doDraw(secondBuildIcon, slot, secondType, object, 0, elementsInInv, startPointX + 32, startPointY + 32, 0.5f, false, true, false, i, useMultiSelectIcon);
				//just set the values for mouse updates
				doDraw(buildIconNum, slot, type, object, 0, elementsInInv, startPointX, startPointY, 1.0f, true, false, true, i, useMultiSelectIcon);
			} else {
				doDraw(buildIconNum, slot, type, object, 0, elementsInInv, startPointX, startPointY, 1.0f, true, true, true, i, useMultiSelectIcon);
			}
			if (slot == selectedSlot) {
				selectIcon.getPos().set(icons[i].getPos());
				selectIcon.getScale().set(icons[i].getScale());
				selectIcon.draw();
			}

			if (slot == selectedSlot) {
				if (lastSelected != slot || lastSelectedSub != subSlot) {
					selectTime = System.currentTimeMillis();
					lastSelected = slot;
					lastSelectedSub = subSlot;
				}

			}

		}
		
		
		for (int i = 0; i < 10; i++) {
			
			int slot = Keyboard.getSlotKey(i + GLFW.GLFW_KEY_1);
			if (slot == selectedSlot) {
				long t = System.currentTimeMillis() - selectTime;
				if (t < 1200) {
					
					ElementInformation info;
					if(ElementKeyMap.isValidType(icons[i].getType()) && ((info = ElementKeyMap.getInfoFast(icons[i].getType())).blocktypeIds != null)){
						
						short[] selectedStack = bs.getSelectedStack(icons[i].getType());
						
						for(int j = 0; j < selectedStack.length; j++){
							short refType = selectedStack[j];
							
							
							
							multiIconHelper.setPos(icons[i].getPos());
							multiIconHelper.setScale(icons[i].getScale());
							multiIconHelper.getPos().y -= (j + 1) * UIScale.getUIScale().scale(64);

							if(inventory.isInfinite()){
								iconsTexts[i].getText().set(0, Inventory.INFINITE_TEXT);
							}
							iconsTexts[i].setPos(multiIconHelper.getPos());
							iconsTexts[i].setTextSimple("");
							if(refType < 0 ){
								multiIconHelper.setType((short)0);
								multiIconHelper.setLayer(-1);
								multiIconHelper.setSpriteSubIndex(8);
							}else{
								ElementInformation refInfo = ElementKeyMap.getInfo(refType);
								multiIconHelper.setType(refType);
								multiIconHelper.setLayer(refInfo.getBuildIconNum() / 256);
								multiIconHelper.setSpriteSubIndex(refInfo.getBuildIconNum() % 256);

							}

							multiIconHelper.draw();
							iconsTexts[i].draw();

							if (subSlot == j) {

								selectIcon.getPos().set(multiIconHelper.getPos());
								selectIcon.getScale().set(multiIconHelper.getScale());
								selectIcon.draw();
							}

						}
					}else if (icons[i].getType() == InventorySlot.MULTI_SLOT) {
						GlUtil.glPushMatrix();
						InventorySlot multiSlot = inventory.getSlot(icons[i].getSlot());
						if (multiSlot.isMultiSlot()) {
							for (int j = 0; j < multiSlot.getSubSlots().size(); j++) {
								InventorySlot inventorySlot = multiSlot.getSubSlots().get(j);

								multiIconHelper.setPos(icons[i].getPos());
								multiIconHelper.setScale(icons[i].getScale());
								multiIconHelper.getPos().y -= (j + 1) * UIScale.getUIScale().scale(64);

								iconsTexts[i].getText().set(0, String.valueOf(inventorySlot.count()));
								if(inventory.isInfinite()){
									iconsTexts[i].getText().set(0, Inventory.INFINITE_TEXT);
								}
								iconsTexts[i].setPos(multiIconHelper.getPos());
								multiIconHelper.setType(inventorySlot.getType());
								multiIconHelper.setLayer(ElementKeyMap.getInfo(inventorySlot.getType()).getBuildIconNum() / 256);
								multiIconHelper.setSpriteSubIndex(ElementKeyMap.getInfo(inventorySlot.getType()).getBuildIconNum() % 256);

								multiIconHelper.draw();
								iconsTexts[i].draw();

								if (subSlot == j) {

									selectIcon.getPos().set(multiIconHelper.getPos());
									selectIcon.getScale().set(multiIconHelper.getScale());
									selectIcon.draw();
								}

								iconsTexts[i].getText().set(0, "");
								iconsTexts[i].getPos().set(0, 0, 0);
							}
						}
						GlUtil.glPopMatrix();
					}
					// Draw for both multi-slots and non-multi-slots
					icons[i].drawToolTipFixed(t / 1200f);
				}
			}
		}

		playerHealth.setPos(UIScale.getUIScale().scale((int)PlayerHealthBar.OFFSET.x), UIScale.getUIScale().scale((int)PlayerHealthBar.OFFSET.y), 0);
		playerHealth.draw();

		icons[0].getSprite().setSelectedMultiSprite(0);
		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
		background.onInit();
		playerHealth.onInit();

		for (int i = 0; i < icons.length; i++) {
			icons[i].onInit();
		}
		selectIcon.onInit();
		reload.onInit();
	}

	@Override
	protected void doOrientation() {
		
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

	private void doDraw(int buildIconNum, int slot, short type, MetaObject object, int subCount, int elementsInInv, float startPointX, float startPointY, float scale, boolean drawCount, boolean draw, boolean checkMouse, int i, boolean useMultiSelectIcon) {
		doDraw(buildIconNum, slot, type, object, subCount, elementsInInv, startPointX, startPointY, 0, 0, scale, drawCount, draw, checkMouse, i, useMultiSelectIcon);
	}

	private void doDraw(int buildIconNum, int slot, short type, MetaObject object, int subCount, int elementsInInv, float startPointX, float startPointY, float countPosX, float countPosY, float scale, boolean drawCount, boolean draw, boolean checkMouse, int i, boolean useMultiSelectIcon) {
		Inventory inventory = ((GameClientState) getState()).getPlayer().getInventory(null);

		if (mouseGlobalCheck) {
			icons[slot].setMouseUpdateEnabled(checkMouse);
			checkMouseInside();
		} else {
			icons[slot].setMouseUpdateEnabled(false);
		}
		iconTint.set(1, 1, 1, 1);
		if (icons[slot].getSprite().getTint() == null) {
			icons[slot].getSprite().setTint(new Vector4f());
		}
		if (((GameClientState) getState()).getController().getTutorialMode() != null && ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager()
				.getPlayerGameControlManager().getInventoryControlManager().isTreeActive() && inventory == ((GameClientState) getState()).getPlayer().getInventory() &&
				((GameClientState) getState()).getController().getTutorialMode().highlightType == type) {
			icons[slot].getSprite().getTint().set(0.4f + HudIndicatorOverlay.selectColorValue, 0.9f + HudIndicatorOverlay.selectColorValue, 0.1f + HudIndicatorOverlay.selectColorValue, 0.1f + HudIndicatorOverlay.selectColorValue);
			iconsTexts[slot].setColor(new Vector4f(icons[slot].getSprite().getTint()));
		} else {
			icons[slot].getSprite().getTint().set(iconTint);
			iconsTexts[slot].setColor(iconTint);
		}

		GlUtil.glPushMatrix();

		icons[slot].setSlot(slot);
		icons[slot].setType(type);
		icons[slot].setMeta(inventory.getMeta(slot));
		icons[slot].setCount(elementsInInv);

		icons[slot].setScale(scale, scale, scale);

		boolean invisible = false;

		//get total count including possible drag
		int elemTotalCount = icons[slot].getCount(false);
		if (elemTotalCount > 0 || subCount > 0) {
			if (subCount > 0) {
				if (icons[slot].getSubSlotType() == type) {
					subCount -= icons[slot].getDraggingCount();
				}
				iconsTexts[slot].getText().set(0, String.valueOf(subCount));
			} else {
				iconsTexts[slot].getText().set(0, String.valueOf(elemTotalCount));
			}
			if(inventory.isInfinite()){
				iconsTexts[slot].getText().set(0, Inventory.INFINITE_TEXT);
			}
		} else {
			buildIconNum = 511; //invisible
			iconsTexts[slot].getText().set(0, "");
			//				invisible = true;
		}
		if (!drawCount) {
			iconsTexts[slot].getText().set(0, "");
		}
		iconsTexts[slot].setPos(countPosX, countPosY, 0);
		icons[slot].setInvisible(invisible);
		icons[slot].getPos().y = ((startPointY));
		icons[slot].getPos().x = ((startPointX) + ((i * getIconSpacing())));

		int layer = buildIconNum / 256;
		if (type > 0 && !useMultiSelectIcon) {
			icons[slot].setLayer(layer);
		} else {
			icons[slot].setLayer(-1);
		}
		buildIconNum = (short) (buildIconNum % 256);

		if(useMultiSelectIcon){
			buildIconNum = 8;
		}
		
		icons[slot].getSprite().setSelectedMultiSprite(buildIconNum);
		//			if(slot == Inventory.ACTIVE_SLOTS_MAX){
		//				System.err.println("SLOT 0 is empty "+inventory.isSlotEmpty(slot)+": "+absType);
		////				System.err.println("INVENTORY: "+inventory);
		//			}

		if (draw) {
			icons[slot].draw();
			if (object != null) {
				reload.setPos(icons[slot].getPos());
				if(object.isDrawnOverlayInHotbar()){
					object.drawPossibleOverlay(reload, ((GameClientState)getState()).getPlayer().getInventory());
				}
			}
		} else {
			if (checkMouse && mouseGlobalCheck) {
				icons[slot].checkMouseInsideWithTransform();
			}
		}

		//		if(slot == selectedSlot){
		//			//					System.err.println(" Class: "+shipElementClass+", i "+i+": "+selectIcon.getPos());
		//			selectIcon.getPos().set(icons[slot].getPos());
		//			selectIcon.getScale().set(icons[slot].getScale());
		//			selectIcon.getSprite().setSelectedMultiSprite(254);
		//			selectIcon.draw( );
		//		}
		GlUtil.glPopMatrix();
		icons[slot].setScale(1, 1, 1);

		iconsTexts[slot].setPos(0, 0, 0);

		if (icons[slot].getSprite().getTint() != null) {
			icons[slot].getSprite().getTint().set(1, 1, 1, 1);
		}
		iconsTexts[slot].setColor(1, 1, 1, 1);
	}

	@Override
	public void drawToolTip() {
		if (Mouse.isGrabbed()) {
			return;
		}
		GlUtil.glPushMatrix();
		for (int i = 0; i < Math.min(11, icons.length); i++) {
			icons[i].drawToolTip();
		}
		GlUtil.glPopMatrix();
	}

	/**
	 * @return the iconsTexts
	 */
	public GUITextOverlay[] getIconsTexts() {
		return iconsTexts;
	}

	/**
	 * @param iconsTexts the iconsTexts to set
	 */
	public void setIconsTexts(GUITextOverlay[] iconsTexts) {
		this.iconsTexts = iconsTexts;
	}

	@Override
	public void update(Timer timer) {
		super.update(timer);
	}

	public boolean placeInChestOrInv(InventorySlotOverlayElement element) {

		return inventoryPanelNew.switchFromBottomBar(element);

//		InventoryControllerManager inventoryControlManager = ((GameClientState)getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getInventoryControlManager();
//		Inventory ownInv = ((GameClientState)getState()).getPlayer().getInventory();
//
//
//		if(inventoryControlManager.getSecondInventory() != null){
//			System.err.println("[CLIENT] HOTBAR FAST SWITCH TO MULTI");
//			return switchInto(ownInv, inventoryControlManager.getSecondInventory(), element);
//		}else{
//			System.err.println("[CLIENT] HOTBAR FAST SWITCH TO SINGLE");
//			return switchToInv(ownInv, element);
//		}
	}

	public void displaySplitDialog(
			InventorySlotOverlayElement se) {
		InventoryIconsNew.displaySplitDialog(se, (GameClientState) getState(), ((GameClientState) getState()).getPlayer().getInventory());
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
		return "BottomBarBuild";
	}

}
