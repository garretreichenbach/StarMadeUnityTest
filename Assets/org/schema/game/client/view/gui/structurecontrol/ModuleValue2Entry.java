package org.schema.game.client.view.gui.structurecontrol;

import org.schema.game.client.data.GameClientState;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;

public class ModuleValue2Entry implements GUIKeyValueEntry {
	public final String name;
	public final Object value;
	private final Object value2;

	public ModuleValue2Entry(String name, Object value, Object value2) {
		super();
		this.name = name;
		this.value = value;
		this.value2 = value2;
	}

	@Override
	public GUIAnchor get(GameClientState state) {
		GUIAnchor c = new GUIAnchor(state, UIScale.getUIScale().scale(30), UIScale.getUIScale().scale(24));
		GUITextOverlay o = new GUITextOverlay(FontSize.TINY_12, state);
		GUITextOverlay o1 = new GUITextOverlay(FontSize.TINY_12, state);
		GUITextOverlay o2 = new GUITextOverlay(FontSize.TINY_12, state);
		o.setTextSimple(name);
		o1.setTextSimple(value);
		o2.setTextSimple(value2);

		o1.setPos(UIScale.getUIScale().scale(160), 0, 0);
		o2.setPos(UIScale.getUIScale().scale(200), 0, 0);

		c.attach(o);
		c.attach(o1);
		c.attach(o2);
		return c;
	}
}
