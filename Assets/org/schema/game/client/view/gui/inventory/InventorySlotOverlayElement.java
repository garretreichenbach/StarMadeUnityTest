package org.schema.game.client.view.gui.inventory;

import java.util.List;

import javax.vecmath.Matrix4f;

import org.schema.common.util.StringTools;
import org.schema.game.client.controller.PlayerInventoryInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.shiphud.newhud.BottomBarBuild;
import org.schema.game.common.data.MetaObjectState;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.player.inventory.InventorySlot;
import org.schema.game.common.data.player.inventory.StashInventory;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.gui.Draggable;
import org.schema.schine.graphicsengine.forms.gui.DropTarget;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUIToolTip;
import org.schema.schine.graphicsengine.forms.gui.TooltipProvider;
import org.schema.schine.graphicsengine.forms.gui.newgui.DialogInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class InventorySlotOverlayElement extends GUIOverlay implements Draggable, DropTarget<InventorySlotOverlayElement>, TooltipProvider {

	// private Sprite sprite2;
	public static final boolean USE_DELAY_GRAB = false;

	private static ObjectArrayList<FixedToolTip> fixedToolTips = new ObjectArrayList<FixedToolTip>();

	// private final Sprite sprite1;
	// private final Sprite sprite0;
	public GUIElement superior;

	private GUIToolTip toolTip;

	private GUIToolTip toolTipDropToSpace;

	private int slot = -1;

	private short type;

	private long timeDraggingStart = -1;

	private int count;

	private int dragPosX;

	private int dragPosY;

	private boolean stickyDrag;

	private int draggingCount;

	private boolean hoverDropZone;

	private int metaId;

	private int subSlot = -1;

	private short subSlotType;

	private Inventory draggingInventory;

	public short drawType;

	public short forcedType;

	public InventorySlotOverlayElement(boolean simple, InputState state, boolean dropTarget, GUIElement superior) {
		super(Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath() + "build-icons-00-16x16-gui-"), state);
		setMouseUpdateEnabled(true);
		if (dropTarget) {
			this.setCallback(this);
		}
		this.superior = superior;
		if (EngineSettings.DRAW_TOOL_TIPS.isOn()) {
			toolTip = new GUIToolTip(state, "", this);
			toolTipDropToSpace = new GUIToolTip(state, "Drop", this);
			toolTipDropToSpace.getColor().set(0.8f, 0, 0);
		}
	}

	public static void drawFixedToolTips() {
		GlUtil.glPushMatrix();
		for (int i = 0; i < fixedToolTips.size(); i++) {
			FixedToolTip fixedToolTip = fixedToolTips.get(i);
			GlUtil.glLoadMatrix(fixedToolTip.modelview);
			float completed = fixedToolTip.completed;
			fixedToolTip.elem.drawToolTipFixedDelayed(completed);
		}
		fixedToolTips.clear();
		GlUtil.glPopMatrix();
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent e) {
		if ((e.pressedLeftMouse() || e.pressedRightMouse()) && getInventory() == ((GameClientState) getState()).getPlayer().getInventory()) {
			if (superior instanceof BottomBarBuild) {
				// clear filters when bottom bar is clicked to avoid confusion when dragging something from there
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(537);
				((GameClientState) getState()).getWorldDrawer().getGuiDrawer().getPlayerPanel().getInventoryPanel().clearFilter(true);
			}
		}
		if (e.pressedLeftMouse()) {
			if (superior instanceof InventoryIconsNew) {
				InventorySlot invSlot = ((InventoryIconsNew) this.superior).getInventory().getSlot(slot);
				((InventoryIconsNew) this.superior).setLastSelected(invSlot);
			}
			if (KeyboardMappings.INVENTORY_SWITCH_ITEM.isDown()) {
				System.err.println("[BUILDBAR] Fast Switch " + superior + "; " + superior.getClass());
				if (superior instanceof InventoryIconsNew) {
					InventoryCallBack invCallback = ((InventoryIconsNew) superior).invCallback;
					if (invCallback != null) {
						invCallback.onFastSwitch(this, (InventoryIconsNew) superior);
					} else {
						assert (false) : "invcallback is null";
					}
				} else if (superior instanceof BottomBarBuild) {
					((BottomBarBuild) superior).placeInChestOrInv(this);
				}
			} else {
				// System.err.println("CALLCACKKK "+super.isInside()+"; sup: "+superior.isInside()+" -> "+isInside()+"; "+getCount(false));
				if (getState().getController().getInputController().getDragging() != null && getState().getController().getInputController().getDragging().isStickyDrag()) {
				// no pick up since we are dragging a sticky object
				} else {
					if (isInside() && getCount(false) > 0) {
						// e.state will already confirm, that the mouse was pressed inside of this
						this.timeDraggingStart = System.currentTimeMillis();
						if (!USE_DELAY_GRAB) {
							System.err.println("NOW GRABBING: " + this + "; " + this.subSlotType + "; " + this.subSlot);
							getState().getController().getInputController().setDragging(this);
							this.draggingInventory = getInventory();
							dragPosX = (int) getRelMousePos().x;
							dragPosY = (int) getRelMousePos().y;
						}
					}
				}
			}
		} else if (e.pressedRightMouse()) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
			AudioController.fireAudioEventID(538);
			boolean openSplit = false;
			Inventory inv = null;
			GameClientState state = (GameClientState) getState();
			if (superior != null && (superior instanceof InventoryIconsNew || superior instanceof BottomBarBuild)) {
				if (superior instanceof BottomBarBuild) {
					inv = state.getPlayer().getInventory();
				} else {
					inv = ((InventoryIconsNew) superior).getInventory();
				}
			}
			if (type < Element.TYPE_NONE) {
				if (inv != null && metaId >= 0) {
					MetaObject object = state.getMetaObjectManager().getObject(metaId);
					if (object != null) {
						getState().getController().getInputController().setCurrentContextPane(object.createContextPane(state, inv, superior));
					} else {
						(state.getController()).popupAlertTextMessage(Lng.str("Object is unknown\non server...\nre-requesting..."), 0);
						state.getMetaObjectManager().checkAvailable(metaId, state);
					}
				} else {
					openSplit = true;
				}
			} else if (type != Element.TYPE_NONE) {
				openSplit = true;
			}
			if (inv.isLockedInventory()) {
				(state.getController()).popupAlertTextMessage(Lng.str("Cannot split stacks in creative mode."), 0);
			} else if (openSplit) {
				if (superior instanceof InventoryIconsNew) {
					InventoryIconsNew.displaySplitDialog(this, (GameClientState) getState(), ((InventoryIconsNew) superior).getInventory());
				} else if (superior instanceof BottomBarBuild) {
					((BottomBarBuild) superior).displaySplitDialog(this);
				}
			}
		} else {
			this.timeDraggingStart = -1;
		}
		checkTarget(e);
	}

	@Override
	public boolean isOccluded() {
		if (isNewHud()) {
			if (superior != null && superior instanceof InventoryIconsNew) {
				return !((InventoryIconsNew) superior).isActive();
			}
			List<DialogInterface> p = getState().getController().getPlayerInputs();
			return !(p.isEmpty() || p.get(p.size() - 1) instanceof PlayerInventoryInput);
		} else {
			return !((GameClientState) getState()).getPlayerInputs().isEmpty();
		}
	}

	@Override
	public boolean checkDragReleasedMouseEvent(MouseEvent e) {
		return (e.releasedLeftMouse()) || (e.pressedLeftMouse() && getState().getController().getInputController().getDragging().isStickyDrag());
	}

	@Override
	public int getDragPosX() {
		return dragPosX;
	}

	/**
	 * @param dragPosX the dragPosX to set
	 */
	@Override
	public void setDragPosX(int dragPosX) {
		this.dragPosX = dragPosX;
	}

	@Override
	public int getDragPosY() {
		return dragPosY;
	}

	/**
	 * @param dragPosY the dragPosY to set
	 */
	@Override
	public void setDragPosY(int dragPosY) {
		this.dragPosY = dragPosY;
	}

	@Override
	public Integer getPlayload() {
		return slot;
	}

	@Override
	public long getTimeDragStarted() {
		return timeDraggingStart;
	}

	@Override
	public boolean isStickyDrag() {
		return stickyDrag;
	}

	@Override
	public void setStickyDrag(boolean b) {
		this.stickyDrag = b;
	}

	@Override
	public void checkTarget(MouseEvent e) {
		// System.err.println("CHECKING "+this+" "+this.hashCode()+" INSIDE");
		if (getState().getController().getInputController().getDragging() != null && getState().getController().getInputController().getDragging().checkDragReleasedMouseEvent(e)) {
			if (isTarget(getState().getController().getInputController().getDragging()) && (getState().getController().getInputController().getDragging() != this)) {
				if ((System.currentTimeMillis() - getState().getController().getInputController().getDragging().getTimeDragStarted()) > Draggable.MIN_DRAG_TIME) {
					onDrop((InventorySlotOverlayElement) getState().getController().getInputController().getDragging());
				}
				getState().getController().getInputController().getDragging().reset();
				getState().getController().getInputController().setDragging(null);
			}
			if ((getState().getController().getInputController().getDragging() == this)) {
				if (getState().getController().getInputController().getDragging().isStickyDrag()) {
					getState().getController().getInputController().getDragging().setStickyDrag(false);
					getState().getController().getInputController().getDragging().reset();
					getState().getController().getInputController().setDragging(null);
				}
			}
		}
	}

	@Override
	public boolean isTarget(Draggable draggable) {
		return this.getClass().isInstance(draggable);
	}

	@Override
	public void onDrop(InventorySlotOverlayElement draggable) {
		System.err.println("[ONDROP] SWITCHING SLOTS: " + slot + ", " + draggable.slot);
		Inventory targetInventory = draggable.getInventory();
		if (targetInventory == getInventory()) {
			assert (slot != draggable.slot);
			getInventory().switchSlotsOrCombineClient(slot, draggable.slot, draggable.subSlot, draggable.getCount(true));
		} else {
			if ((getInventory().isSlotLocked(slot) || targetInventory.isSlotLocked(draggable.slot))) {
				((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Cannot move active Item!"), 0);
			} else if ((getInventory().isLockedInventory() || targetInventory.isLockedInventory())) {
				((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Cannot move item from this inventory!"), 0);
			} else {
				System.err.println("Inventory Switched: " + getInventory() + " -> " + targetInventory);
				if (((GameClientState) getState()).getPlayer().isInTestSector() && (targetInventory instanceof StashInventory || getInventory() instanceof StashInventory)) {
					((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Cannot move item from this inventory in test sector!"), 0);
				} else {
					getInventory().switchSlotsOrCombineClient(slot, draggable.slot, draggable.subSlot, targetInventory, draggable.getCount(true));
				}
			}
		}
		draggable.stickyDrag = false;
		draggable.draggingCount = 0;
		draggable.reset();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIOverlay#draw()
	 */
	@Override
	public void draw() {
		super.draw();
	}

	@Override
	public String getName() {
		if (type > 0) {
			ElementInformation elementInformation = ElementKeyMap.getInfo(type);
			return elementInformation.getName();
		} else if (type < 0) {
			MetaObject object = ((MetaObjectState) getState()).getMetaObjectManager().getObject(metaId);
			String text;
			if (object == null) {
				text = "";
			} else {
				text = object.toString();
			}
			return text;
		}
		return "";
	}

	public short getFirstSubType() {
		if (getInventory() == null || getInventory().getSlot(slot) == null || getInventory().getSlot(slot).getSubSlots().isEmpty()) {
			return (short) -1;
		} else {
			return getInventory().getSlot(slot).getSubSlots().get(0).getType();
		}
	}

	@Override
	public void drawToolTip() {
		if (!hoverDropZone && superior != null && superior instanceof InventoryIconsNew && !((InventoryIconsNew) superior).isInside()) {
			return;
		}
		if (!toolTip.isDrawableTooltip()) {
			toolTip.onNotDrawTooltip();
			return;
		}
		boolean draw = false;
		if (ElementKeyMap.isValidType(type)) {
			draw = true;
			ElementInformation elementInformation = ElementKeyMap.getInfo(getSelectedType());
			String toolTipText = elementInformation.getName();
			toolTip.setText(toolTipText);
			if (getInventory() != null && getInventory().getSlot(slot) != null) {
				toolTip.setText(toolTip.getText() + "\n" + Lng.str("Vol: %s", StringTools.formatPointZero(getInventory().getSlot(slot).getVolume())));
			}
		// System.err.println("TT TEXT::: "+toolTip.getText()+"; "+getType()+"; "+lastType);
		}
		if (type == InventorySlot.MULTI_SLOT) {
			draw = true;
			String text = Lng.str("Multi-Slot(unknown)");
			if (getInventory() != null && getInventory().getSlot(slot) != null && getInventory().getSlot(slot).isMultiSlot()) {
				getInventory().getSlot(slot).getSubSlots().get(0).getType();
				short subType = getInventory().getSlot(slot).getSubSlots().get(0).getType();
				text = Lng.str("Multi-Slot: %s", ElementKeyMap.getInfo(subType).getName());
			}
			toolTip.setText(text);
			if (getInventory() != null && getInventory().getSlot(slot) != null) {
				toolTip.setText(toolTip.getText() + "\n" + Lng.str("Vol: %s", StringTools.formatPointZero(getInventory().getSlot(slot).getVolume())));
			}
		} else if (type < 0) {
			draw = true;
			MetaObject object = ((MetaObjectState) getState()).getMetaObjectManager().getObject(metaId);
			String text;
			if (object == null) {
				text = Lng.str("unknown");
			} else {
				text = object.toString();
			}
			toolTip.setText(text);
			// only cache if loaded
			if (getInventory() != null && getInventory().getSlot(slot) != null) {
				toolTip.setText(toolTip.getText() + "\n" + Lng.str("Vol: %s", StringTools.formatSmallAndBig(getInventory().getSlot(slot).getVolume())));
			}
		}
		if (toolTip.getText().toString().length() > 0) {
			if (slot >= 10 && getInventory().getInventoryHolder() == ((GameClientState) getState()).getPlayer()) {
				String qs = "";
				qs = "\n" + Lng.str("[Shift+Click] Quick-Switch");
				if (!toolTip.getText().toString().contains(qs)) {
					toolTip.setText(toolTip.getText() + qs);
				}
			} else if (getInventory().isInfinite() && slot >= 0 && getInventory().getInventoryHolder() == ((GameClientState) getState()).getPlayer()) {
				String qs = "";
				qs = "\n" + Lng.str("[Shift+Click] Remove");
				if (!toolTip.getText().toString().contains(qs)) {
					toolTip.setText(toolTip.getText() + qs);
				}
			}
		}
		if (draw) {
			GlUtil.glPushMatrix();
			if (!hoverDropZone) {
				toolTip.draw();
			}
			GlUtil.glPopMatrix();
			if (hoverDropZone) {
				// System.err.println("HOVERING");
				GlUtil.glPushMatrix();
				toolTipDropToSpace.drawForced();
				GlUtil.glPopMatrix();
				hoverDropZone = false;
			}
		}
	}

	private void drawToolTipFixedDelayed(float completed) {
		boolean draw = false;
		if (getSelectedType() > 0) {
			draw = true;
			ElementInformation elementInformation = ElementKeyMap.getInfo(getSelectedType());
			String toolTipText = elementInformation.getName();
			toolTip.setText(toolTipText);
		} else if (type == InventorySlot.MULTI_SLOT) {
			draw = true;
			String text = Lng.str("Multi-Slot(unknown)");
			int subSlot = ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getSelectedSubSlot();
			if (getInventory() != null && getInventory().getSlot(slot) != null && getInventory().getSlot(slot).isMultiSlot() && getInventory().getSlot(slot).getSubSlots().size() > subSlot) {
				text = ElementKeyMap.getInfo(getInventory().getSlot(slot).getSubSlots().get(subSlot).getType()).getName();
			}
			toolTip.setText(text);
		} else if (type < 0) {
			draw = true;
			MetaObject object = ((MetaObjectState) getState()).getMetaObjectManager().getObject(metaId);
			String text;
			if (object == null) {
				text = Lng.str("unknown");
			} else {
				text = object.toString();
			}
			toolTip.setText(text);
		}
		if (draw) {
			GlUtil.glPushMatrix();
			this.transform();
			toolTip.setMouseOver(false);
			toolTip.getPos().y = -13;
			toolTip.getPos().x = 0;
			if (completed < 0.15f) {
				toolTip.setAlpha(completed / 0.15f);
			} else if (completed > 0.8f) {
				toolTip.setAlpha(((1.0f - completed)) / 0.2f);
			}
			toolTip.draw();
			toolTip.setMouseOver(true);
			GlUtil.glPopMatrix();
			toolTip.setAlpha(-1);
		}
	}

	private short getSelectedType() {
		if (forcedType > 0) {
			return forcedType;
		} else {
			return type;
		}
	}

	public void drawToolTipFixed(float completed) {
		fixedToolTips.add(new FixedToolTip(this, completed));
	}

	public void flagHoverDropeZone() {
		this.hoverDropZone = true;
	}

	/**
	 * @return the count
	 */
	public int getCount(boolean dragging) {
		if (dragging && stickyDrag) {
			if (subSlotType > 0) {
				draggingCount = Math.min(draggingCount, draggingInventory.getCount(slot, subSlotType));
			}
			return draggingCount;
		} else {
			if (getState().getController().getInputController().getDragging() == this) {
				if (subSlotType > 0) {
					count = Math.min(count, draggingInventory.getCount(slot, subSlotType));
				}
				return count - draggingCount;
			}
			return count;
		}
	}

	public int getDraggingCount() {
		return draggingCount;
	}

	/**
	 * @param draggingCount the draggingCount to set
	 */
	public void setDraggingCount(int draggingCount) {
		this.draggingCount = draggingCount;
	}

	private Inventory getInventory() {
		return superior instanceof InventoryIconsNew ? ((InventoryIconsNew) superior).getInventory() : ((GameClientState) getState()).getPlayer().getInventory(null);
	}

	/**
	 * @return the metaId
	 */
	public int getMetaId() {
		return metaId;
	}

	/**
	 * @param metaId the metaId to set
	 */
	public void setMetaId(int metaId) {
		this.metaId = metaId;
	}

	/**
	 * @return the slot
	 */
	public int getSlot() {
		return slot;
	}

	public void setSlot(int slot) {
		this.slot = slot;
	}

	/**
	 * @return the timeDraggingStart
	 */
	public long getTimeDraggingStart() {
		return timeDraggingStart;
	}

	/**
	 * @param timeDraggingStart the timeDraggingStart to set
	 */
	@Override
	public void setTimeDraggingStart(long timeDraggingStart) {
		this.timeDraggingStart = timeDraggingStart;
	}

	@Override
	public void reset() {
		this.subSlotType = Element.TYPE_NONE;
		this.subSlot = -1;
		this.draggingCount = 0;
		this.draggingInventory = null;
	}

	/**
	 * @return the getType()
	 */
	@Override
	public short getType() {
		return type;
	}

	/**
	 * @param getType() the getType() to set
	 */
	public void setType(short type) {
		if (getState().getController().getInputController().getDragging() == this && type == Element.TYPE_NONE) {
			try {
				throw new NullPointerException();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		this.type = type;
	}

	@Override
	public boolean isInside() {
		// if(slot == 10){
		// System.err.println("IN "+super.isInside()+"; superior "+(superior == null || (superior).isInside()));
		// }
		boolean b = super.isInside() && (superior == null || (superior).isInside());
		;
		// if(b){
		// System.err.println("INSIDE::: "+ElementKeyMap.toString(getType())+"; sot: "+slot);
		// }
		return b;
	}

	/**
	 * @param count the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}

	public void setLayer(int layer) {
		if (layer == -1) {
			setSprite(Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath() + "meta-icons-00-16x16-gui-"));
		} else {
			setSprite(Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath() + "build-icons-" + StringTools.formatTwoZero(layer) + "-16x16-gui-"));
		}
	}

	public void setMeta(int meta) {
		this.metaId = meta;
	}

	public void setDraggingSubSlot(int subSlot) {
		this.subSlot = subSlot;
	}

	public void setDraggingSubSlotType(short selected) {
		this.subSlotType = selected;
	}

	/**
	 * @return the subSlotType
	 */
	public short getSubSlotType() {
		return subSlotType;
	}

	/**
	 * @param subSlotType the subSlotType to set
	 */
	public void setSubSlotType(short subSlotType) {
		this.subSlotType = subSlotType;
	}

	/**
	 * @return the draggingInventory
	 */
	public Inventory getDraggingInventory() {
		return draggingInventory;
	}

	/**
	 * @param draggingInventory the draggingInventory to set
	 */
	public void setDraggingInventory(Inventory draggingInventory) {
		this.draggingInventory = draggingInventory;
	}

	private class FixedToolTip {

		Matrix4f modelview = new Matrix4f();

		float completed;

		InventorySlotOverlayElement elem;

		public FixedToolTip(InventorySlotOverlayElement elem, float completed) {
			super();
			this.modelview = new Matrix4f();
			this.modelview.set(Controller.modelviewMatrix);
			this.completed = completed;
			this.elem = elem;
		}
	}
}
