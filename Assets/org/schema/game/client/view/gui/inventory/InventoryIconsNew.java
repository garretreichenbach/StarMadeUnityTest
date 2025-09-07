package org.schema.game.client.view.gui.inventory;

import java.util.List;
import java.util.Locale;

import javax.vecmath.Vector4f;

import org.schema.game.client.controller.manager.ingame.InventorySlotSplitDialog;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.shiphud.HudIndicatorOverlay;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.common.data.MetaObjectState;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.element.meta.MetaObjectManager.MetaObjectType;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.player.inventory.InventorySlot;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.AbstractSceneNode;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.font.unicode.Color;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.TooltipProvider;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITexDrawableAreaCustomWrappable;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.Mouse;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class InventoryIconsNew extends GUIElement implements TooltipProvider {

	public static final int ICON_SCALE() {
		return UIScale.getUIScale().ICON_SIZE * 2;
	}

	public static final int ICON_SIZE() {
		return UIScale.getUIScale().ICON_SIZE;
	}

	public static final int ICON_SPACING() {
		return UIScale.getUIScale().inset;
	}

	public static final int ICON_TOTAL() {
		return ICON_SIZE() + 2 * ICON_SPACING();
	}

	private final GUIScrollablePanel scrolPane;

	private final InventoryProvider inventoryProvider;

	private GUITexDrawableAreaCustomWrappable slotBg;

	private int height;

	private int width;

	private boolean init;

	private GameClientState state;

	private int maxRows;

	private int iconsPerRow;

	private final Vector4f iconTint = new Vector4f(1, 1, 1, 1);

	private GUITextOverlay[] iconsTexts;

	private GUIOverlay reload;

	private InventorySlotOverlayElement[] icons;

	private InventorySlotOverlayElement draggingIcon;

	private GUITextOverlay draggingIconText;

	private int startRow;

	private int firstIcon;

	private int lastRow;

	private int lastIcon;

	private int lastIconCount;

	private InventorySlot lastSelected;

	public InventoryCallBack invCallback;

	private InventoryToolInterface tools;

	private boolean filtering;

	public InventoryIconsNew(GameClientState state, GUIScrollablePanel scrolPane, InventoryProvider inventory, InventoryCallBack callback, InventoryToolInterface tools) {
		super(state);
		this.scrolPane = scrolPane;
		this.inventoryProvider = inventory;
		this.invCallback = callback;
		this.state = state;
		this.tools = tools;
		setMouseUpdateEnabled(true);
	}

	public static void displaySplitDialog(InventorySlotOverlayElement se, GameClientState state, Inventory inventory) {
		int slot = se.getSlot();
		short type = se.getType();
		int metaId = se.getMetaId();
		if (type == InventorySlot.MULTI_SLOT) {
			List<InventorySlot> subSlots = inventory.getSubSlots(slot);
			InventorySlotSplitDialog inventorySlotSplitDialog = new InventorySlotSplitDialog((state), type, slot, Lng.str("Split Inventory Slot"), Lng.str("Select the item you want to take."), 1, se, subSlots, inventory);
			inventorySlotSplitDialog.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(509);
		// 
		} else if (type > Element.TYPE_NONE) {
			InventorySlotSplitDialog inventorySlotSplitDialog = new InventorySlotSplitDialog((state), type, slot, Lng.str("Split Inventory Slot"), Lng.str("How many %s do you want to take?", ElementKeyMap.getInfo(type).getName()), 1, se, null, inventory);
			inventorySlotSplitDialog.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(508);
		} else {
			System.err.println("Cannot split meta items (shouldnt stack yet)");
			MetaObject object = ((MetaObjectState) state).getMetaObjectManager().getObject(metaId);
			if (object != null) {
				(state).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().suspend(true);
				DialogInput editDialog = object.getEditDialog(state, (state).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager(), inventory);
				if (editDialog == null) {
					(state.getController()).popupAlertTextMessage(Lng.str("Object has no editing option (yet)..."), 0);
				} else {
					editDialog.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(507);
				}
			} else {
				(state.getController()).popupAlertTextMessage(Lng.str("Object is unknown\non server...\nre-requesting..."), 0);
				((MetaObjectState) state).getMetaObjectManager().checkAvailable(metaId, (state));
			}
		}
	}

	private class IconSlot {

		InventorySlotOverlayElement elem;

		int slotPosToDrawM;

		public int slot;

		public int startPointY;

		public int elementsInInv;

		private short type;

		public int secondBuildIcon;

		private int buildIconNum;

		public short secondType;

		public int startPointX;

		public float scale;

		public boolean drawCount;

		public float countPosX;

		public float countPosY;

		public int subCount;

		public boolean checkMouse;

		public boolean draw;

		public MetaObject object;

		public boolean second;

		public IconSlot() {
		}

		public IconSlot(IconSlot ic) {
			super();
			this.elem = ic.elem;
			this.slotPosToDrawM = ic.slotPosToDrawM;
			this.slot = ic.slot;
			this.startPointY = ic.startPointY;
			this.elementsInInv = ic.elementsInInv;
			this.type = ic.getType();
			this.secondBuildIcon = ic.secondBuildIcon;
			this.setBuildIconNum(ic.getBuildIconNum());
			this.secondType = ic.secondType;
			this.startPointX = ic.startPointX;
			this.scale = ic.scale;
			this.drawCount = ic.drawCount;
			this.countPosX = ic.countPosX;
			this.countPosY = ic.countPosY;
			this.subCount = ic.subCount;
			this.checkMouse = ic.checkMouse;
			this.draw = ic.draw;
			this.object = ic.object;
			this.second = ic.second;
		}

		public int getRow() {
			int row = (slotPosToDrawM - getInventory().getActiveSlotsMax()) / iconsPerRow;
			return row;
		}

		public int getColumn() {
			return (slotPosToDrawM - getInventory().getActiveSlotsMax()) % iconsPerRow;
		}

		public int getColumnPos() {
			return startPointX + getColumn() * (ICON_TOTAL());
		}

		public int getRowPos() {
			int rowY = startPointY + getRow() * (ICON_TOTAL());
			return rowY;
		}

		public short getType() {
			if (second) {
				return secondType;
			} else {
				return type;
			}
		}

		public void setType(short type) {
			this.type = type;
		}

		public int getBuildIconNum() {
			if (second) {
				return secondBuildIcon;
			} else {
				return buildIconNum;
			}
		}

		public void setBuildIconNum(int buildIconNum) {
			if (second) {
				secondBuildIcon = buildIconNum;
			} else {
				this.buildIconNum = buildIconNum;
			}
		}

		public InventorySlotOverlayElement getIcon() {
			return icons[slotPosToDrawM - firstIcon];
		}

		public GUITextOverlay getTextIcon() {
			return iconsTexts[slotPosToDrawM - firstIcon];
		}
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public boolean isInside() {
		return super.isInside() && scrolPane.isInside();
	}

	@Override
	public void setInside(boolean inside) {
		super.setInside(inside);
	}

	private boolean isValid(InventorySlot s) {
		if (ElementKeyMap.isValidType(s.getType()) && ElementKeyMap.getInfoFast(s.getType()).getName().toLowerCase(Locale.ENGLISH).contains(tools.getText().toLowerCase(Locale.ENGLISH))) {
			// System.err.println("CHECK "+ElementKeyMap.getInfoFast(s.getType()).getName()+"; "+tools.getText());
			return true;
		}
		if ((s.metaId != -1 && state.getMetaObjectManager().getObject(s.metaId) != null && state.getMetaObjectManager().getObject(s.metaId).getName().toLowerCase(Locale.ENGLISH).contains(tools.getText().toLowerCase(Locale.ENGLISH)))) {
			return true;
		}
		if (s.isMultiSlot()) {
			for (int i = 0; i < s.getSubSlots().size(); i++) {
				if (isValid(s.getSubSlots().get(i))) {
					return true;
				}
			}
		}
		return false;
	}

	private final static Vector4f cFiltered = new Vector4f(0.6f, 0.6f, 1f, 1f);

	private final static Vector4f cUnFiltered = new Vector4f(1f, 1f, 1f, 1f);

	@Override
	public void draw() {
		if (inventoryProvider.getInventory() != null && !inventoryProvider.getInventory().clientRequestedDetails) {
			inventoryProvider.getInventory().requestClient(state);
		}
		calculateDim();
		GlUtil.glPushMatrix();
		transform();
		checkMouseInside();
		if (tools.getText() != null && tools.getText().trim().length() > 0) {
			this.filtering = true;
			slotBg.setColor(cFiltered);
		} else {
			this.filtering = false;
			slotBg.setColor(cUnFiltered);
		}
		slotBg.draw();
		assert (init);
		int filterSlot = getInventory().getActiveSlotsMax() + 1;
		synchronized (state) {
			state.setSynched();
			if (tools.getText() != null && tools.getText().trim().length() > 0) {
				for (InventorySlot s : getInventory().getMap().values()) {
					if (s.slot >= getInventory().getActiveSlotsMax() && isValid(s)) {
						if (filterSlot >= firstIcon && filterSlot < lastIcon - 1 && ((filterSlot - 1) - firstIcon) >= 0 && ((filterSlot - 1) - firstIcon) < icons.length) {
							drawSlot(s.slot, filterSlot - 1, s, iconsPerRow);
						}
						filterSlot++;
					}
				}
			} else {
				for (int slot = firstIcon; slot < lastIcon; slot++) {
					drawSlot(slot, slot, getInventory().getMap().get(slot), iconsPerRow);
				}
			}
			state.setUnsynched();
		}
		for (AbstractSceneNode f : getChilds()) {
			f.draw();
		}
		GlUtil.glPopMatrix();
	}

	private void drawSlot(int slot, int slotToDrawAs, InventorySlot inventorySlot, int iconsPerRow) {
		{
			IconSlot ic = new IconSlot();
			ic.startPointY = ICON_SPACING();
			ic.startPointX = ICON_SPACING();
			ic.slot = slot;
			ic.slotPosToDrawM = slotToDrawAs;
			ic.countPosX = 0;
			ic.countPosY = 0;
			ic.second = false;
			assert (slot >= getInventory().getActiveSlotsMax());
			// invisible
			ic.setType((short) 0);
			// invisible
			ic.setBuildIconNum(511);
			ic.elementsInInv = 0;
			ic.secondBuildIcon = -1;
			ic.secondType = (short) -1;
			MetaObject object = null;
			if (!getInventory().isSlotEmpty(slot)) {
				ic.setType(inventorySlot.getType());
				ic.elementsInInv = inventorySlot.count();
				if (ic.getType() > 0) {
					ic.setBuildIconNum(ElementKeyMap.getInfo(ic.getType()).getBuildIconNum());
				} else {
					if (ic.getType() == InventorySlot.MULTI_SLOT) {
					} else {
						ic.setBuildIconNum(Math.abs(ic.getType()));
						if (ic.getType() < 0) {
							object = state.getMetaObjectManager().getObject(inventorySlot.metaId);
						}
						if (ic.getType() == MetaObjectType.WEAPON.type) {
							object = state.getMetaObjectManager().getObject(inventorySlot.metaId);
							if (object != null) {
								ic.setBuildIconNum(ic.getBuildIconNum() + object.getExtraBuildIconIndex());
							}
						}
					}
				}
			}
			ic.scale = 1.0f;
			ic.object = object;
			// String filter = inventoryIconTools.getText().trim();
			// if(inventory.isInfinite() && filter.length() > 0){
			// if (getName(ic.slot, ic.getType(), ic.object != null ? ic.object.getId() : -1).toLowerCase(Locale.ENGLISH).contains(filter.toLowerCase(Locale.ENGLISH))) {
			// //displayed
			// ic.slotPosToDrawM = next;
			// next++;
			// }else{
			// continue;
			// }
			// } else {
			// ic.slotPosToDrawM = slot;
			// }
			// if(scrollPanel != null && (scrollPanel.getScrollY() > ic.getRowPos()+spacing)){
			// continue;
			// }
			// if(scrollPanel != null && scrollPanel.getScrollY()+scrollPanel.getHeight() < ic.getRowPos()){
			// break;
			// }
			if (ic.getType() == InventorySlot.MULTI_SLOT) {
				List<InventorySlot> subSlots = new ObjectArrayList<InventorySlot>(inventorySlot.getSubSlots());
				for (int i = 1; i < subSlots.size() && i < 5; i++) {
					float x = (i - 1) % 2;
					float y = (i - 1) / 2;
					IconSlot sub = new IconSlot();
					sub.object = object;
					sub.setBuildIconNum(ElementKeyMap.getInfo(subSlots.get(i).getType()).getBuildIconNum());
					sub.slot = slot;
					sub.slotPosToDrawM = ic.slotPosToDrawM;
					sub.setType(subSlots.get(i).getType());
					sub.elementsInInv = subSlots.get(i).count();
					sub.subCount = subSlots.get(i).count();
					sub.startPointX = (int) (ic.startPointX + x * 32);
					sub.startPointY = (int) (ic.startPointY + y * 32);
					sub.countPosX = x * 44;
					sub.countPosY = y * 42;
					sub.scale = 0.5f;
					sub.drawCount = true;
					sub.draw = true;
					sub.checkMouse = false;
					doDraw(sub);
				}
				IconSlot master = new IconSlot(ic);
				master.setBuildIconNum(ElementKeyMap.getInfo(subSlots.get(0).getType()).getBuildIconNum());
				master.elementsInInv = subSlots.get(0).count();
				master.subCount = subSlots.get(0).count();
				master.setType(subSlots.get(0).getType());
				master.startPointX = ic.startPointX + UIScale.getUIScale().ICON_START_POINT;
				master.startPointY = ic.startPointY + UIScale.getUIScale().ICON_START_POINT;
				master.drawCount = true;
				master.draw = true;
				master.checkMouse = false;
				master.scale = 0.75f;
				// draw master block
				doDraw(master);
				ic.buildIconNum = ElementKeyMap.getInfo(subSlots.get(0).getType()).getBuildIconNum();
				ic.elementsInInv = subSlots.get(0).count();
				ic.subCount = subSlots.get(0).count();
				ic.drawCount = false;
				ic.draw = false;
				ic.checkMouse = true;
				// draw transparent for mouse grab
				doDraw(ic);
			} else if (ic.secondBuildIcon >= 0) {
				ic.drawCount = true;
				ic.draw = true;
				ic.checkMouse = true;
				doDraw(ic);
				IconSlot snd = new IconSlot(ic);
				snd.second = true;
				doDraw(snd);
				ic.drawCount = true;
				ic.draw = false;
				ic.checkMouse = true;
				// just set the values for mouse updates
				doDraw(ic);
			} else {
				ic.drawCount = true;
				ic.draw = true;
				ic.checkMouse = true;
				doDraw(ic);
			}
		}
	}

	private void doDraw(IconSlot ic) {
		InventorySlotOverlayElement icons = ic.getIcon();
		GUITextOverlay iconsTexts = ic.getTextIcon();
		// String filter = inventoryIconTools.getText().trim();
		iconTint.set(1, 1, 1, 1);
		// if(!inventory.isInfinite()){
		// if (filter.length() > 0 && !icons.getName().toLowerCase(Locale.ENGLISH).contains(filter.toLowerCase(Locale.ENGLISH))) {
		// iconTint.set(1, 1, 1, 0.2f);
		// } else {
		// iconTint.set(1, 1, 1, 1);
		// }
		// }
		if (icons.getSprite().getTint() == null) {
			icons.getSprite().setTint(new Vector4f());
		}
		if (state.getController().getTutorialMode() != null && state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getInventoryControlManager().isTreeActive() && inventoryProvider == state.getPlayer().getInventory() && state.getController().getTutorialMode().highlightType == ic.getType()) {
			icons.getSprite().getTint().set(0.4f + HudIndicatorOverlay.selectColorValue, 0.9f + HudIndicatorOverlay.selectColorValue, 0.1f + HudIndicatorOverlay.selectColorValue, 0.1f + HudIndicatorOverlay.selectColorValue);
			iconsTexts.setColor(new Vector4f(icons.getSprite().getTint()));
		} else {
			icons.getSprite().getTint().set(iconTint);
			iconsTexts.setColor(iconTint);
		}
		iconsTexts.setPos(ic.countPosX, ic.countPosY, 0);
		icons.setMouseUpdateEnabled(ic.checkMouse);
		GlUtil.glPushMatrix();
		icons.setSlot(ic.slot);
		icons.setType(ic.getType());
		icons.setMeta(getInventory().getMeta(ic.slot));
		icons.setCount(ic.elementsInInv);
		icons.setScale(ic.scale, ic.scale, ic.scale);
		boolean invisible = false;
		// get total count including possible drag
		int elemTotalCount = icons.getCount(false);
		if (elemTotalCount > 0 || ic.subCount > 0) {
			if (ic.subCount > 0) {
				if (icons.getSubSlotType() == ic.getType()) {
					ic.subCount -= icons.getDraggingCount();
				}
				iconsTexts.getText().set(0, String.valueOf(ic.subCount));
			} else {
				iconsTexts.getText().set(0, String.valueOf(elemTotalCount));
			}
			if (getInventory().isInfinite()) {
				iconsTexts.getText().set(0, Inventory.INFINITE_TEXT);
			}
		} else {
			// invisible
			ic.setBuildIconNum(511);
			iconsTexts.getText().set(0, "");
		// invisible = true;
		}
		if (!ic.drawCount) {
			iconsTexts.getText().set(0, "");
		}
		icons.setInvisible(invisible);
		icons.getPos().y = ic.getRowPos();
		icons.getPos().x = ic.getColumnPos();
		int layer = ic.getBuildIconNum() / 256;
		if (ic.getType() > 0) {
			icons.setLayer(layer);
		} else {
			icons.setLayer(-1);
		}
		ic.setBuildIconNum((short) (ic.getBuildIconNum() % 256));
		icons.getSprite().setSelectedMultiSprite(ic.getBuildIconNum());
		// if(slot == Inventory.ACTIVE_SLOTS_MAX){
		// System.err.println("SLOT 0 is empty "+inventory.isSlotEmpty(slot)+": "+absType);
		// //				System.err.println("INVENTORY: "+inventory);
		// }
		if (ic.draw) {
			icons.draw();
			if (ic.object != null && ic.object.isDrawnOverlayInInventory()) {
				reload.setPos(icons.getPos());
				ic.object.drawPossibleOverlay(reload, getInventory());
			}
		} else {
			if (ic.checkMouse) {
				icons.checkMouseInsideWithTransform();
			}
		}
		// if(slot == selectedSlot){
		// //					System.err.println(" Class: "+shipElementClass+", i "+i+": "+selectIcon.getPos());
		// selectIcon.getPos().set(icons[slot].getPos());
		// selectIcon.getScale().set(icons[slot].getScale());
		// selectIcon.getSprite().setSelectedMultiSprite(254);
		// selectIcon.draw( );
		// }
		GlUtil.glPopMatrix();
		icons.setScale(1, 1, 1);
		if (icons.getSprite().getTint() != null) {
			icons.getSprite().getTint().set(1, 1, 1, 1);
		}
		iconsTexts.setColor(1, 1, 1, 1);
		iconsTexts.setPos(0, 0, 0);
	}

	@Override
	public void onInit() {
		slotBg = new GUITexDrawableAreaCustomWrappable(state, Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath() + "inventory-slot-gui-").getMaterial().getTexture(), 0.0f, 0.0f);
		// 0.5625f + ((float)ICON_SPACING * 2.0f) * 0.0078125f;
		slotBg.wrapX = ICON_TOTAL() / (float) ICON_SCALE();
		// 0.5625f + ((float)ICON_SPACING * 2.0f) * 0.0078125f;//( 0.0078125f == 1px (1/128))
		slotBg.wrapY = ICON_TOTAL() / (float) ICON_SCALE();
		draggingIconText = getNewTextIcon();
		draggingIcon = new InventorySlotOverlayElement(false, state, false, this);
		draggingIcon.attach(draggingIconText);
		reload = new GUIOverlay(Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath() + "HUD_Hotbar-8x2-gui-"), state);
		calculateDim();
		init = true;
	}

	private GUITextOverlay getNewTextIcon() {
		GUITextOverlay iconsTexts = new GUITextOverlay(FontSize.MEDIUM_15, state);
		iconsTexts.setColor(Color.white);
		iconsTexts.setText(new ObjectArrayList(1));
		iconsTexts.getText().add("undefined");
		iconsTexts.setPos(UIScale.getUIScale().smallinset, UIScale.getUIScale().smallinset, 0);
		return iconsTexts;
	}

	private void reinitIcon(int i) {
		icons[i] = new InventorySlotOverlayElement(false, state, true, this);
		iconsTexts[i] = getNewTextIcon();
		icons[i].attach(iconsTexts[i]);
	}

	private void reinitIcons(final int size) {
		icons = new InventorySlotOverlayElement[size];
		iconsTexts = new GUITextOverlay[size];
		for (int i = 0; i < size; i++) {
			reinitIcon(i);
		}
	}

	public void calculateDim() {
		int sX = (int) scrolPane.getScrollX();
		int wid = (int) scrolPane.getWidth();
		int sY = (int) scrolPane.getScrollY();
		int hei = (int) scrolPane.getHeight();
		iconsPerRow = wid / ICON_TOTAL();
		if (!getInventory().isEmpty()) {
			assert (inventoryProvider != null);
			assert (getInventory().getMap() != null);
			// System.err.println("LAST INT KEY: "+getInventory().getMap().lastIntKey());
			maxRows = (int) Math.ceil((double) Math.max(0, getInventory().getMap().lastIntKey() - getInventory().getActiveSlotsMax()) / (double) iconsPerRow) + 1;
			width = (iconsPerRow * ICON_TOTAL());
			height = (maxRows * ICON_TOTAL());
			slotBg.setWidth(width);
			slotBg.setHeight(height);
		} else {
			maxRows = 1;
			width = (iconsPerRow * ICON_TOTAL());
			height = (maxRows * ICON_TOTAL());
			slotBg.setWidth(width);
			slotBg.setHeight(height);
		}
		startRow = (int) Math.floor((double) sY / (double) ICON_TOTAL());
		firstIcon = startRow * iconsPerRow + getInventory().getActiveSlotsMax();
		lastRow = (int) Math.ceil(((double) sY + hei) / ICON_TOTAL());
		lastIcon = (lastRow + 1) * iconsPerRow + getInventory().getActiveSlotsMax();
		int iconCount = (lastIcon - firstIcon);
		if (iconCount > lastIconCount) {
			reinitIcons(iconCount);
			lastIconCount = iconCount;
		}
	}

	@Override
	public void drawToolTip() {
		GlUtil.glPushMatrix();
		if (icons != null) {
			for (int i = 0; i < icons.length; i++) {
				icons[i].drawToolTip();
			}
		}
		GlUtil.glPopMatrix();
	}

	@Override
	public float getHeight() {
		return height;
	}

	@Override
	public float getWidth() {
		return width;
	}

	@Override
	public GameClientState getState() {
		return state;
	}

	public Inventory getInventory() {
		return inventoryProvider.getInventory();
	}

	public void drawDragging(InventorySlotOverlayElement e) {
		GUIElement.enableOrthogonal();
		draggingIcon.setPos(Mouse.getX() - e.getDragPosX(), Mouse.getY() - e.getDragPosY(), 0);
		draggingIcon.setScale(1, 1, 1);
		draggingIconText.setScale(1, 1, 1);
		short type = e.getType();
		if (type == InventorySlot.MULTI_SLOT && e.getSubSlotType() == Element.TYPE_NONE) {
			assert (e.getDraggingInventory() != null);
			List<InventorySlot> subSlots = new ObjectArrayList<InventorySlot>(e.getDraggingInventory().getSubSlots(e.getSlot()));
			int buildIconNum = 0;
			int layer = 0;
			for (int i = 1; i < subSlots.size() && i < 5; i++) {
				draggingIcon.setPos(Mouse.getX() - e.getDragPosX(), Mouse.getY() - e.getDragPosY(), 0);
				float x = (i - 1) % 2;
				float y = (i - 1) / 2;
				InventorySlot inventorySlot = subSlots.get(i);
				buildIconNum = ElementKeyMap.getInfo(inventorySlot.getType()).getBuildIconNum();
				layer = buildIconNum / 256;
				buildIconNum = (buildIconNum % 256);
				draggingIcon.setLayer(layer);
				draggingIcon.setSpriteSubIndex(buildIconNum);
				draggingIconText.getText().set(0, String.valueOf(inventorySlot.count()));
				if (inventorySlot.isInfinite()) {
					draggingIconText.getText().set(0, Inventory.INFINITE_TEXT);
				}
				draggingIconText.setPos(x * 44, y * 42, 0);
				draggingIcon.setScale(0.5f, 0.5f, 0.5f);
				// draggingIconText.setScale(0.5f, 0.5f, 0.5f);
				draggingIcon.getPos().x += x * 32;
				draggingIcon.getPos().y += y * 32;
				draggingIcon.draw();
				draggingIconText.setPos(0, 0, 0);
			}
			draggingIcon.setPos(Mouse.getX() - e.getDragPosX(), Mouse.getY() - e.getDragPosY(), 0);
			InventorySlot inventorySlot = subSlots.get(0);
			// draw master block
			buildIconNum = ElementKeyMap.getInfo(inventorySlot.getType()).getBuildIconNum();
			layer = buildIconNum / 256;
			buildIconNum = (buildIconNum % 256);
			draggingIcon.setLayer(layer);
			draggingIcon.setSpriteSubIndex(buildIconNum);
			draggingIconText.getText().set(0, String.valueOf(inventorySlot.count()));
			if (inventorySlot.isInfinite()) {
				draggingIconText.getText().set(0, Inventory.INFINITE_TEXT);
			}
			draggingIcon.setScale(0.75f, 0.75f, 0.75f);
			// draggingIconText.setScale(0.75f, 0.75f, 0.75f);
			draggingIcon.getPos().x += 8;
			draggingIcon.getPos().y += 8;
			draggingIcon.draw();
			GUIElement.disableOrthogonal();
			return;
		} else if (type == InventorySlot.MULTI_SLOT && e.getSubSlotType() != Element.TYPE_NONE) {
			type = e.getSubSlotType();
		}
		int buildIconNum = 0;
		if (type > 0) {
			buildIconNum = ElementKeyMap.getInfo(type).getBuildIconNum();
			int layer = buildIconNum / 256;
			draggingIcon.setLayer(layer);
		} else if (type < 0) {
			buildIconNum = Math.abs(type);
			draggingIcon.setLayer(-1);
		}
		if (type == MetaObjectType.WEAPON.type) {
			int meta = getInventory().getMeta(e.getSlot());
			MetaObject object = state.getMetaObjectManager().getObject(meta);
			if (object != null) {
				buildIconNum += object.getExtraBuildIconIndex();
			} else {
				GUIElement.disableOrthogonal();
				return;
			}
		}
		buildIconNum = (buildIconNum % 256);
		draggingIcon.setSpriteSubIndex(buildIconNum);
		draggingIconText.getText().set(0, String.valueOf(e.getCount(true)));
		if (e.getDraggingInventory().isInfinite()) {
			draggingIconText.getText().set(0, Inventory.INFINITE_TEXT);
		}
		draggingIcon.draw();
		GUIElement.disableOrthogonal();
	}

	public InventorySlot getLastSelected() {
		return lastSelected;
	}

	public void setLastSelected(final InventorySlot invSlot) {
		if (invSlot != null) {
			this.lastSelected = new InventorySlot(invSlot, 0);
		} else {
			this.lastSelected = null;
		}
	}

	public boolean isCurrentlyFilteringInventory() {
		return filtering;
	}

	public void clearFilter() {
		tools.clearFilter();
	}
}
