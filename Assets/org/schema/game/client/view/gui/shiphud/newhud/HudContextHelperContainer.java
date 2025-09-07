package org.schema.game.client.view.gui.shiphud.newhud;

import org.schema.game.client.view.effects.TimedIndication;
import org.schema.game.client.view.gui.shiphud.HudIndicatorOverlay;
import org.schema.game.common.data.SegmentPiece;
import org.schema.schine.graphicsengine.core.settings.ContextFilter;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHelperIcon;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHelperIconManager;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.input.InputType;

import com.bulletphysics.linearmath.Transform;

public class HudContextHelperContainer {
	public enum Hos {
		MOUSE, LEFT, BLOCK
	}


	InputType type;
	int key;
	Object text;
	TimedIndication indication;
	Hos position;
	ContextFilter filter;
	GUIHelperIcon icon;
	SegmentPiece p;

	public HudContextHelperContainer(InputType type, int key, Object text, Hos position, ContextFilter filter) {
		super();
		set(type, key, text, position, filter);
	}

	public HudContextHelperContainer() {
	}

	public void set(InputType type, int key, Object text, Hos position, ContextFilter filter) {
		this.type = type;
		this.key = key;
		this.text = text;
		this.position = position;
		this.filter = filter;
	}

	public void create(InputState state) {
		this.icon = GUIHelperIconManager.get(state, type, key);
		icon.setTextAfter(text);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + key;
		result = prime * result + ((position == null) ? 0 : position.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(!(obj instanceof HudContextHelperContainer)) {
			return false;
		}
		HudContextHelperContainer other = (HudContextHelperContainer) obj;
		if(key != other.key) {
			return false;
		}
		if(position != other.position) {
			return false;
		}
		if(text == null) {
			if(other.text != null) {
				return false;
			}
		} else if(!text.equals(other.text)) {
			return false;
		}
		if(type != other.type) {
			return false;
		}
		return true;
	}

	public void drawOnBlock() {
		assert (p != null);
		indication = new TimedIndication(new Transform(), "", 0.1f, 100f);
		indication.setText(text);
		p.getTransform(indication.getCurrentTransform());
		indication.getCurrentTransform().origin.y += key * UIScale.getUIScale().scale(16);
		HudIndicatorOverlay.toDrawTexts.add(indication);
	}
}
