package org.schema.game.client.controller.manager.ingame;

import java.util.List;

import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIBlockIconButton;
import org.schema.game.client.view.gui.inventory.InventorySlotOverlayElement;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.player.inventory.InventorySlot;
import org.schema.schine.common.InputChecker;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.Draggable;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.sound.controller.AudioController;

public class InventorySlotSplitDialog extends PlayerGameTextInput {

	protected final Object descriptionObject;

	protected short element;

	private Draggable guiSlot;

	private int subSlot;

	private short selected;

	private List<InventorySlot> subSlots;

	private Inventory inventory;

	private int slot;

	public InventorySlotSplitDialog(GameClientState state, final short element, int slot, String title, Object sellDesc, int lastQuantity, final Draggable guiSlot, List<InventorySlot> subSlots, final Inventory inventory) {
		super("InventorySlotSplitDialog", state, 450, 250, 10, title, sellDesc, String.valueOf(lastQuantity));
		this.descriptionObject = sellDesc;
		this.element = element;
		this.guiSlot = guiSlot;
		this.subSlots = subSlots;
		this.inventory = inventory;
		this.slot = slot;
		setInputChecker((entry, callback) -> {
			try {
				if (entry.length() > 0) {
					if (element != InventorySlot.MULTI_SLOT) {
						ElementInformation elementInformation = ElementKeyMap.getInfo(element);
					}
					int quantity = (int) Math.min(Integer.MAX_VALUE, Long.parseLong(entry));
					return true;
				}
			} catch (NumberFormatException e) {
			}
			callback.onFailedTextCheck("Amount must be a number!");
			return false;
		});
		getInputPanel().onInit();
		if (subSlots != null) {
			for (int i = 0; i < subSlots.size(); i++) {
				final short type = subSlots.get(i).getType();
				if (i == 0) {
					selected = type;
				}
				final int subSlot = i;
				GUITextOverlay t = new GUITextOverlay(FontSize.MEDIUM_18, getState());
				GUIBlockIconButton c = new GUIBlockIconButton(state, type, new GUICallback() {

					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if (event.pressedLeftMouse()) {
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
							AudioController.fireAudioEventID(144);
							selected = type;
							InventorySlotSplitDialog.this.subSlot = subSlot;
						}
					}

					@Override
					public boolean isOccluded() {
						return false;
					}
				}) {

					/* (non-Javadoc)
					 * @see org.schema.schine.graphicsengine.forms.gui.GUIIconButton#draw()
					 */
					@Override
					public void draw() {
						if (selected == this.type) {
							getBackgroundColor().set(0.6f, 0.6f, 1.0f, 1f);
							getForegroundColor().set(1f, 1.0f, 1f, 1f);
						} else {
							getBackgroundColor().set(0.3f, 0.3f, 0.3f, 0.6f);
							getForegroundColor().set(1f, 1.0f, 1f, 0.6f);
						}
						super.draw();
					}
				};
				t.setTextSimple(String.valueOf(subSlots.get(i).count()));
				t.onInit();
				c.onInit();
				c.attach(t);
				c.setPos(i * 68 + 4, 20, 0);
				getInputPanel().getContent().attach(c);
			}
			GUITextButton splitAll = new GUITextButton(state, 100, 20, Lng.str("Split All Up"), new GUICallback() {

				@Override
				public boolean isOccluded() {
					return false;
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(145);
						inventory.splitUpMulti(((InventorySlotOverlayElement) guiSlot).getSlot());
						deactivate();
					}
				}
			});
			splitAll.setPos(4, 96, 0);
			getInputPanel().getContent().attach(splitAll);
		}
		;
	}

	@Override
	public String[] getCommandPrefixes() {
		return null;
	}

	@Override
	public String handleAutoComplete(String s, TextCallback callback, String prefix) {
		return s;
	}

	@Override
	public void onFailedTextCheck(String msg) {
		setErrorMessage(msg);
	}

	/**
	 * @return the descriptionObject
	 */
	public Object getDescriptionObject() {
		return descriptionObject;
	}

	@Override
	public boolean isOccluded() {
		return getState().getController().getPlayerInputs().indexOf(this) != getState().getController().getPlayerInputs().size() - 1;
	}

	@Override
	public void onDeactivate() {
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getShopControlManager().suspend(false);
	}

	@Override
	public boolean onInput(String entry) {
		int dragCount = (int) Math.min(Integer.MAX_VALUE, Long.parseLong(entry));
		if (dragCount > 0) {
			if (subSlots != null) {
				dragCount = Math.min(subSlots.get(subSlot).count(), dragCount);
			} else {
				dragCount = Math.min(((InventorySlotOverlayElement) guiSlot).getCount(false), dragCount);
			}
			guiSlot.setStickyDrag(true);
			getState().getController().getInputController().setDragging(guiSlot);
			guiSlot.setDragPosX((int) ((GUIElement) getInputPanel()).getRelMousePos().x + 32);
			guiSlot.setDragPosY((int) ((GUIElement) getInputPanel()).getRelMousePos().y + 32);
			guiSlot.setTimeDraggingStart(System.currentTimeMillis());
			((InventorySlotOverlayElement) guiSlot).setSlot(slot);
			if (selected != Element.TYPE_NONE) {
				((InventorySlotOverlayElement) guiSlot).setDraggingSubSlot(subSlot);
				((InventorySlotOverlayElement) guiSlot).setDraggingSubSlotType(selected);
			} else {
				((InventorySlotOverlayElement) guiSlot).setDraggingSubSlot(-1);
				((InventorySlotOverlayElement) guiSlot).setDraggingSubSlotType((short) 0);
			}
			((InventorySlotOverlayElement) guiSlot).setDraggingCount(dragCount);
			((InventorySlotOverlayElement) guiSlot).setDraggingInventory(inventory);
			return true;
		} else {
			return false;
		}
	}
}
