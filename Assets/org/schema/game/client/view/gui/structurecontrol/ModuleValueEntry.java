package org.schema.game.client.view.gui.structurecontrol;

import org.schema.game.client.data.GameClientState;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;

public class ModuleValueEntry implements GUIKeyValueEntry {
	public final String name;
	public final Object value;
	private int lines = 1;

	public ModuleValueEntry(String name, Object value, int lines) {
		this.name = name;
		this.value = value;
		this.lines = lines;
	}

	public ModuleValueEntry(String name, Object value) {
		this(name, value, 1);
	}

	@Override
	public GUIAnchor get(GameClientState state) {
		GUIAnchor c = new GUIAnchor(state, 380, 24 * lines);
		GUITextOverlay o = new GUITextOverlay(FontSize.TINY_12, state);
		GUITextOverlay o1 = new GUITextOverlay(FontSize.TINY_12, state);
		o.setTextSimple(name);
		o1.setTextSimple(value);
		c.attach(o);
		o1.setPos(UIScale.getUIScale().scale(220), 0);
		c.attach(o1);
		return c;
	}
}
