package org.schema.game.client.view.gui;

import org.schema.game.client.view.gui.weapon.WeaponSlotOverlayElement;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.TooltipProvider;

public interface HotbarInterface extends TooltipProvider {

	public void orientate(int i);

	public float getHeight();

	public void activateDragging(boolean b);

	public void draw();

	public void drawDragging(WeaponSlotOverlayElement e);

	public void update(Timer timer);

	public void onInit();

	public boolean isInside();

}
