package org.schema.schine.graphicsengine.core.settings.states;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.FastMath;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.input.InputState;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import java.util.List;

public abstract class DynamicStates<E extends Object> extends States<E> {

	public List<E> states = new ObjectArrayList<E>();
	public E state;
	private int pointer;

	public DynamicStates(E... states) {
		
		
		for(E s : states){
			this.states.add(s);
		}
		assert (this.states.size() > 0);
	}

	@Override
	public boolean contains(E state) {
		for (int i = 0; i < states.size(); i++) {
			if (state.equals(states.get(i))) {
				return true;
			}
		}
		return false;
	}

	

	@Override
	public String getType() {
		return states.get(0).getClass().getSimpleName();
	}

	@Override
	public E next() throws StateParameterNotFoundException {
		if (states.size() <= 1) {
			return states.get(pointer);
		}
		pointer = (pointer + 1) % states.size();
		state = states.get(pointer);
		return state;
	}

	@Override
	public E previous() throws StateParameterNotFoundException {
		if (states.size() <= 1) {
			return states.get(pointer);
		}
		pointer = FastMath.cyclicBWModulo(pointer - 1, states.size());
		state = states.get(pointer);
		return state;
	}

	@Override
	public Tag toTag() {
		return new Tag(Type.STRING, null, state.toString());
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
		return state;
	}

	@Override
	public void setCurrentState(E state) {
		this.state = state; 
		this.pointer = states.indexOf(state);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return states + "->[" + state + "]";
	}
	public GUIElement getGUIElement(InputState state, GUIElement dependent, SettingsInterface s) {
		return getGUIElement(state, dependent, "SETTING", s);
	}
	@Override
	public GUIElement getGUIElement(InputState state, final GUIElement dependent, String deactText, final SettingsInterface s) {
		throw new RuntimeException();
//		if(getCurrentState() instanceof Boolean){
//			GUICheckBox c = new GUICheckBox(state) {
//
//				@Override
//				protected boolean isActivated() {
//					return (Boolean)s.getCurrentState();
//				}
//
//				@Override
//				protected void deactivate() throws StateParameterNotFoundException {
//					setCurrentState((E) Boolean.FALSE);
//					s.setCurrentState(getCurrentState());
//				}
//
//				@Override
//				protected void activate() throws StateParameterNotFoundException {
//					setCurrentState((E) Boolean.TRUE);
//					s.setCurrentState(getCurrentState());
//				}
//			};
//			c.activeInterface = new GUIActiveInterface() {
//
//				@Override
//				public boolean isActive() {
//					return dependent.isActive();
//				}
//			};
//			c.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
//			return c;
//		}
//
//
//		int width = UIScale.getUIScale().scale(100);
//		int heigth = UIScale.getUIScale().h;
//
//		int i = 0;
//		GUIElement[] elements = new GUIElement[states.size()];
//		int selIndex = -1;
//		for (final E e : states) {
//			GUITextOverlay o = new GUITextOverlay(FontSize.MEDIUM_15, state);
//			final Vector3i pos = new Vector3i();
//			o.setTextSimple(new Object() {
//				@Override
//				public String toString() {
//					return e.toString();
//				}
//			});
//			if(e == s.getCurrentState()){
//				selIndex = i;
//			}
//			GUIAncor a = new GUIAncor(state, width, heigth);
//			o.getPos().x = UIScale.getUIScale().inset;
//			o.getPos().y = UIScale.getUIScale().inset;
//			a.attach(o);
//			a.setUserPointer(e);
//			elements[i] = a;
//			i++;
//		}
//
//		GUIDropDownList t = new GUIDropDownList(state, width, heigth, 400, new DropDownCallback() {
//			private boolean first = true;
//
//			@Override
//			public void onSelectionChanged(GUIListElement element) {
//				if (first) {
//					first = false;
//					return;
//				}
//				E inv = (E) element.getContent().getUserPointer();
//				setCurrentState(inv);
//				s.setCurrentState(getCurrentState());
//			}
//		}, elements);
//		if(selIndex >= 0){
//			t.setSelectedIndex(selIndex);
//		}
//
//		t.dependend = dependent;
//
//
//
//		return t;
	}

	public void addState(E currentState) {
		if(!states.contains(currentState)){
			states.add(currentState);
		}
	}
}
