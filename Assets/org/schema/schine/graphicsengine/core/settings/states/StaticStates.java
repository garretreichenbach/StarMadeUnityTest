package org.schema.schine.graphicsengine.core.settings.states;

import java.util.Arrays;

import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.DropDownCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICheckBox;
import org.schema.schine.graphicsengine.forms.gui.GUIDropDownList;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.SettingsInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class StaticStates<E extends Object> extends States<E> {

	public E[] states;
	private int pointer;

	public StaticStates(E... states) {
		assert (states.length > 0);
		this.states = states;
	}

	@Override
	public boolean contains(E state) {
		for (int i = 0; i < states.length; i++) {
			if (state.equals(states[i])) {
				return true;
			}
		}
		return false;
	}

	@Override
	public E getFromString(String arg) throws StateParameterNotFoundException {
		for (int i = 0; i < states.length; i++) {
			if (arg.equals(states[i].toString())) {
				pointer = i;
				return states[pointer];
			}
		}
		throw new StateParameterNotFoundException(arg, states);
	}

	@Override
	public String getType() {
		return states[0].getClass().getSimpleName();
	}

	@Override
	public E next() throws StateParameterNotFoundException {
		if (states.length <= 1) {
			return states[pointer];
		}
		pointer = FastMath.cyclicModulo(pointer + 1, states.length);
		return states[pointer];
	}

	@Override
	public E previous() throws StateParameterNotFoundException {
		if (states.length <= 1) {
			return states[pointer];
		}
		pointer = FastMath.cyclicModulo(pointer - 1, states.length);
		return states[pointer];
	}

	@Override
	public Tag toTag() {
		return new Tag(Type.STRING, null, states[pointer].toString());
	}

	@Override
	public E readTag(Tag tag) {
		try {
			return getFromString((String) tag.getValue());
		} catch (StateParameterNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public E getCurrentState() {
		return states[pointer];
	}

	@Override
	public void setCurrentState(E state) {
		for (int i = 0; i < states.length; i++) {
			if (state.equals(states[i])) {
				pointer = i;
				break;
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Arrays.toString(states) + "->[" + states[pointer] + "]";
	}
	public GUIElement getGUIElement(InputState state, GUIElement dependent, SettingsInterface s) {
		return getGUIElement(state, dependent, "SETTING", s);
	}
	@Override
	public GUIElement getGUIElement(InputState state, final GUIElement dependent, String deactText, final SettingsInterface s) {
		
		if(getCurrentState() instanceof Boolean){
			GUICheckBox c = new GUICheckBox(state) {
				
				@Override
				protected boolean isActivated() {
					return s.isOn();
				}
				
				@Override
				protected void deactivate() throws StateParameterNotFoundException {
					setCurrentState((E) Boolean.FALSE);
					s.setOn((Boolean)getCurrentState());
				}
				
				@Override
				protected void activate() throws StateParameterNotFoundException {
					setCurrentState((E) Boolean.TRUE);	
					s.setOn((Boolean)getCurrentState());
				}
			};
			c.activeInterface = dependent::isActive;
			c.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
			return c;
		}
		
		
		int width = UIScale.getUIScale().scale(100);
		int heigth = UIScale.getUIScale().h;
		
		int i = 0;
		GUIElement[] elements = new GUIElement[states.length];
		int selIndex = -1;
		for (final E e : states) {
			GUITextOverlay o = new GUITextOverlay(FontSize.MEDIUM_15, state);
			final Vector3i pos = new Vector3i();
			o.setTextSimple(new Object() {
				@Override
				public String toString() {
					return e.toString();
				}
			});
			if(e == s.getObject()){
				selIndex = i;
			}
			GUIAnchor a = new GUIAnchor(state, width, heigth);
			o.getPos().x = UIScale.getUIScale().scale(3);
			o.getPos().y = UIScale.getUIScale().scale(3);
			a.attach(o);
			a.setUserPointer(e);
			elements[i] = a;
			i++;
		}

		GUIDropDownList t = new GUIDropDownList(state, width, heigth, UIScale.getUIScale().scale(400), new DropDownCallback() {
			private boolean first = true;

			@Override
			public void onSelectionChanged(GUIListElement element) {
				if (first) {
					first = false;
					return;
				}
				E inv = (E) element.getContent().getUserPointer();
				setCurrentState(inv);
				s.setObject(getCurrentState());
			}
		}, elements); 
		if(selIndex >= 0){
			t.setSelectedIndex(selIndex);
		}
		
		t.dependend = dependent;
		
		
		
		return t;
	}
}
