package org.schema.game.client.view.gui.inventory;

import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.input.InputState;

public class SlotsOverlay extends GUIOverlay {

	private GUIScrollablePanel scroller;
	public SlotsOverlay(InputState state, GUIScrollablePanel scroller) {
		super(Controller.getResLoader().getSprite("inventory-slots-gui-"), state);
		this.scroller = scroller;
		setMouseUpdateEnabled(true);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIOverlay#draw()
	 */
	@Override
	public void draw() {
		//		System.err.println("INVENTORY DRAWN: :: "+this+": "+invetory);
//		if (invetory != null && invetory.getInventorySlotsMax() == 2) {
//			setSprite(Controller.getResLoader().getSprite("inventory-slots-simple-gui-"));
//		} else {
			setSprite(Controller.getResLoader().getSprite("inventory-slots-gui-"));
//		}
		super.draw();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIOverlay#getHeight()
	 */
	@Override
	public float getHeight() {
//		if (invetory != null && invetory.getInventorySlotsMax() == 2) {
//			return 128;
//		} else {
			return 360;
//		}

	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIOverlay#getWidth()
	 */
	@Override
	public float getWidth() {
		return 503;
	}

	@Override
	public boolean isInside() {
		boolean i = super.isInside() && scroller.isInside();
		//		System.err.println("Slots Overlay: "+this+" -> inside "+i);

		return i;
	}

	public void setInventory(Inventory inventory) {
	}
}
