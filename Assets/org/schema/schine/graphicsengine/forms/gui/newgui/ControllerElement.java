package org.schema.schine.graphicsengine.forms.gui.newgui;

import java.util.List;

import org.schema.schine.graphicsengine.forms.gui.GUIDropDownList;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;

public  class ControllerElement {
	public enum FilterPos{
		TOP, BOTTOM;
	}
	
	public enum FilterRowStyle{
		FULL,
		LEFT,
		RIGHT;
	}
	
	private FilterRowStyle mode = FilterRowStyle.FULL;
	private FilterPos pos = FilterPos.BOTTOM;
	public GUIElement gui;

	public ControllerElement(GUIElement gui) {
		super();
		this.gui = gui;
	}

	public FilterRowStyle getMode() {
		return mode;
	}

	public void setMode(FilterRowStyle mode) {
		this.mode = mode;
	}

	public FilterPos getPos() {
		return pos;
	}

	public void setPos(FilterPos pos) {
		this.pos = pos;
	}
	
	public static void drawFilterElements(boolean second, int filterPosY, List<ControllerElement> elements) {
		if (second) {
			//draw bottom to top. positions have been set
			for (int i = elements.size() - 1; i >= 0; i--) {
				ControllerElement filter = elements.get(i);
				if (!second && !(filter.gui instanceof GUIDropDownList)) {
					filter.gui.draw();
				} else if (second && (filter.gui instanceof GUIDropDownList)) {
					filter.gui.draw();
				}
			}
		} else {
			
			for (int i = 0; i < elements.size(); i++) {
				ControllerElement filter = elements.get(i);
				filter.gui.getPos().y = filterPosY;
				if (!second && !(filter.gui instanceof GUIDropDownList)) {
					filter.gui.draw();
				} else if (second && (filter.gui instanceof GUIDropDownList)) {
					filter.gui.draw();
				}
				//left may not be the last
				assert (!(filter.mode == FilterRowStyle.LEFT) || i != elements.size() - 1);

				//before left there was a right or a full
				assert (!(filter.mode == FilterRowStyle.LEFT && i > 0) || (elements.get(i - 1).mode == FilterRowStyle.FULL || elements.get(i - 1).mode == FilterRowStyle.RIGHT));

				//before right was a left
				assert (!(filter.mode == FilterRowStyle.RIGHT && i > 0) || (elements.get(i - 1).mode == FilterRowStyle.LEFT));

				if (filter.mode == FilterRowStyle.FULL || filter.mode == FilterRowStyle.RIGHT) {
					filterPosY += filter.gui.getHeight();
				}
			}
		}
	}
}
