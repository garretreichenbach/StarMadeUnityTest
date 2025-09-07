package org.schema.game.common.controller.rules.rules.conditions;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.TranslatableEnum;
import org.schema.game.client.view.mainmenu.gui.ruleconfig.ActionGroupConfigDialog;
import org.schema.game.client.view.mainmenu.gui.ruleconfig.ConditionGroupConfigDialog;
import org.schema.game.client.view.mainmenu.gui.ruleconfig.GUIRuleStat;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.controller.rules.rules.actions.ActionList;
import org.schema.game.common.controller.rules.rules.conditions.seg.ConditionGroup;
import org.schema.game.common.controller.rules.rules.conditions.seg.SegmentControllerMoreLessCondition;
import org.schema.game.common.util.FieldUtils;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActivatableTextBar;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.settingsnew.GUICheckBoxTextPairNew;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class RuleFieldValue {

	public final Object obj;

	public final Field f;

	public final RuleValue a;

	public RuleFieldValue(Object obj, Field f, RuleValue a) {
		super();
		this.obj = obj;
		this.f = f;
		this.a = a;
		f.setAccessible(true);
	}

	public static List<RuleFieldValue> create(Object o) {
		List<RuleFieldValue> c = new ObjectArrayList<RuleFieldValue>();
		List<Field> fs = FieldUtils.getAllFields(new ObjectArrayList<Field>(), o.getClass());
		for (Field f : fs) {
			RuleValue a = f.getAnnotation(RuleValue.class);
			if (a != null) {
				RuleFieldValue v = new RuleFieldValue(o, f, a);
				c.add(v);
			}
		}
		return c;
	}

	public Class<?> getType() {
		return f.getType();
	}

	public String getName() {
		return a.tag();
	}

	private GUIElement createTextBarFromTo(InputState state, final GUIElement dep)  {
		GUIAnchor t = new GUIAnchor(state) {

			@Override
			public float getHeight() {
				return dep.getHeight();
			}

			@Override
			public float getWidth() {
				return dep.getWidth();
			}

		};
		assert( f.getType() == FactionRange.class);
		final FactionRange r;
		try {
			r = (FactionRange) f.get(obj);
		} catch (Exception e1) {
			e1.printStackTrace();
			return new GUIAnchor(state);
		}
		GUIActivatableTextBar tFrom = new GUIActivatableTextBar(state, FontSize.MEDIUM_15, 12, 1, "from", t, new TextCallback() {
			@Override
			public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {}
			@Override
			public void onFailedTextCheck(String msg) {}
			@Override
			public void newLine() {}
			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {return null;}
			@Override
			public String[] getCommandPrefixes() {return null;}
		}, t12 -> t12){
			@Override
			protected void onBecomingInactive() {
				String t = getText();
				long v = 0;
				while(t.length() > 0){
					try{
						r.from = Integer.parseInt(t.trim());
						return;
					}catch(Exception e){
						t = t.substring(0, t.length()-1);
					}
				}
				setTextWithoutCallback(getValueAsString());
			}
		};
		tFrom.setDeleteOnEnter(false);
		tFrom.setTextWithoutCallback(String.valueOf(r.from));
		tFrom.leftDependentHalf = true;

		GUIActivatableTextBar tTo = new GUIActivatableTextBar(state, FontSize.MEDIUM_15, 12, 1, "from", t, new TextCallback() {
			@Override
			public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {}
			@Override
			public void onFailedTextCheck(String msg) {}
			@Override
			public void newLine() {}
			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {return null;}
			@Override
			public String[] getCommandPrefixes() {return null;}
		}, t1 -> t1){
			@Override
			protected void onBecomingInactive() {
				String t = getText();
				long v = 0;
				while(t.length() > 0){
					try{
						r.to = Integer.parseInt(t.trim());
						return;
					}catch(Exception e){
						t = t.substring(0, t.length()-1);
					}
				}
				setTextWithoutCallback(getValueAsString());
			}
		};
		tTo.setDeleteOnEnter(false);
		tTo.setTextWithoutCallback(String.valueOf(r.to));
		tTo.rightDependentHalf = true;


		t.attach(tFrom);
		t.attach(tTo);


		return t;
	}
	private GUIElement createTextBar(InputState state, GUIElement dep) {
		int len = f.getType().isPrimitive() ? 10 : 500;
		GUIActivatableTextBar t = new GUIActivatableTextBar(state, FontSize.MEDIUM_15, len, 1, "val", dep, new TextCallback() {

			@Override
			public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {
			}

			@Override
			public void onFailedTextCheck(String msg) {
			}

			@Override
			public void newLine() {
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
				return null;
			}

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}
		}, t1 -> t1) {

			@Override
			protected void onBecomingInactive() {
				String t = getText();
				long v = 0;
				while (t.length() > 0) {
					try {
						applyValueFromString(t.trim());
						return;
					} catch (Exception e) {
						t = t.substring(0, t.length() - 1);
					}
				}
				setTextWithoutCallback(getValueAsString());
			}
		};
		t.setDeleteOnEnter(false);
		t.setTextWithoutCallback(getValueAsString());
		return t;
	}

	public GUIElement createGUIEditElement(InputState state, final GUIRuleStat stat, GUIElement dep) {

		if(Condition.isEnum(f)){
			Enum[] possile;
			try {
				possile = (Enum[]) f.getType().getMethod("values").invoke(null);

			return createDropDown(state, dep, possile, (DropDownCallback) element -> {
				try {
					f.set(obj, element.getContent().getUserPointer());
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			});
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
			} catch (NoSuchMethodException e1) {
				e1.printStackTrace();
			} catch (SecurityException e1) {
				e1.printStackTrace();
			}
			GUITextOverlay o = new GUITextOverlay(FontSize.MEDIUM_15, state);
			o.setTextSimple(Lng.str("editing of type not implemented"));
			return o;
		}else if(f.getType() == Integer.TYPE && a.intMap().length > 0){
			return createDropDown(state, dep, a.intMap(), a.int2StringMap(), element -> {
				try {
					f.setInt(obj, ((Integer) element.getContent().getUserPointer()).intValue());
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			});
		} else if (f.getType() == Boolean.TYPE) {
			return createCheckBox(state, dep, SegmentControllerMoreLessCondition.class.isAssignableFrom(obj.getClass()));
		}else if(ActionList.class.isAssignableFrom(f.getType())) {//ActionList.class.isAssignableFrom(obj.getClass())) {
			try {
				return createActionGroupEditButton(state, stat, dep, f.get(obj));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			GUITextOverlay o = new GUITextOverlay(FontSize.MEDIUM_15, state);
			o.setTextSimple(Lng.str("editing of type failed"));
			return o;
		}else if(f.getType().isPrimitive()) {
			return createTextBar(state, dep);
		} else if (f.getType() == ConditionList.class) {
			return createGroupEditButton(state, stat, dep);
		} else if (f.getType() == FactionRange.class) {
			return createTextBarFromTo(state, dep);
		} else if (f.getType() == String.class) {
			return createTextBar(state, dep);
		} else {
			GUITextOverlay o = new GUITextOverlay(FontSize.MEDIUM_15, state);
			o.setTextSimple(Lng.str("editing of type not implemented"));
			return o;
		}
	}

	private GUIElement createDropDown(InputState state, GUIElement dep, Object[] intMap, String[] strings, DropDownCallback d) {
		List<GUIElement> f = new ObjectArrayList<GUIElement>();
		int selected = 0;
		for (int i = 0; i < strings.length; i++) {
			f.add(generateDropDownElement(state, strings[i], intMap[i]));
			try {
				if (intMap[i].equals(this.f.get(obj))) {
					selected = i;
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		GUIDropDownList dd = new GUIDropDownList(state, 24, 24, 200, d, f);
		dd.dependend = dep;
		dd.setSelectedIndex(selected);
		return dd;
	}

	private GUIElement createDropDown(InputState state, GUIElement dep, int[] intMap, String[] strings, DropDownCallback d) {
		List<GUIElement> f = new ObjectArrayList<GUIElement>();
		int selected = 0;
		for (int i = 0; i < strings.length; i++) {
			f.add(generateDropDownElement(state, strings[i], intMap[i]));
			try {
				if (intMap[i] == this.f.getInt(obj)) {
					selected = i;
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		GUIDropDownList dd = new GUIDropDownList(state, 24, 24, 200, d, f);
		dd.dependend = dep;
		dd.setSelectedIndex(selected);
		return dd;
	}
	private GUIElement createDropDown(InputState state, GUIElement dep, Enum[] enums,
			DropDownCallback d) {
		List<GUIElement> f = new ObjectArrayList<GUIElement>();
		int selected = 0;
		for(int i = 0; i < enums.length; i++) {
			f.add(generateDropDownElement(state, enums[i]));
			try {
				if(enums[i] == this.f.get(obj)) {
					selected = i;
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		GUIDropDownList dd = new GUIDropDownList(state, 24, 24, 200, d, f);
		dd.dependend = dep;
		dd.setSelectedIndex(selected);
		return dd;
	}

	private GUIElement generateDropDownElement(InputState state, Enum e) {
		GUIAnchor c = new GUIAnchor(state, 100, 24);
		GUITextOverlayTable t = new GUITextOverlayTable(state);
		if(e instanceof TranslatableEnum) {
			t.setTextSimple(((TranslatableEnum)e).getName());
		}else {
			t.setTextSimple(e.name());
		}

		t.setPos(5, 5, 0);
		c.attach(t);
		c.setUserPointer(e);
		return c;
	}
	private GUIElement generateDropDownElement(InputState state, String name, Object data) {
		GUIAnchor c = new GUIAnchor(state, 100, 24);
		GUITextOverlayTable t = new GUITextOverlayTable(state);
		t.setTextSimple(name);
		t.setPos(5, 5, 0);
		c.attach(t);
		c.setUserPointer(data);
		return c;
	}

	private GUIElement createCheckBox(InputState state, GUIElement dep, final boolean biggerSmaller) {
		Object text = new Object() {

			public String toString() {
				if (biggerSmaller) {
					try {
						return f.getBoolean(obj) ? "bigger than" : "smaller or exactly";
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
				return "";
			}
		};
		GUICheckBoxTextPairNew r = new GUICheckBoxTextPairNew(state, text, FontSize.MEDIUM_15) {

			@Override
			public boolean isChecked() {
				try {
					return f.getBoolean(obj);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				return false;
			}

			@Override
			public void deactivate() {
				try {
					f.setBoolean(obj, false);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void activate() {
				try {
					f.setBoolean(obj, true);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		};
		return r;
	}

	private GUIElement createGroupEditButton(final InputState state, final GUIRuleStat stat, final GUIElement dep) {
		GUITextButton upButton = new GUITextButton(state, 25, 24, Lng.str("Edit"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !dep.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					ConditionGroupConfigDialog d = new ConditionGroupConfigDialog(state, stat, (ConditionGroup) obj);
					d.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(936);
				}
			}

		});
		return upButton;
	}
	private GUIElement createActionGroupEditButton(final InputState state, final GUIRuleStat stat, final GUIElement dep, final Object actionList) {
		GUITextButton upButton = new GUITextButton(state, 25, 24, Lng.str("Edit"), new GUICallback() {
			@Override
			public boolean isOccluded() {
				return !dep.isActive();
			}
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()){
					ActionGroupConfigDialog d = new ActionGroupConfigDialog(state, stat, stat.selectedAction, (ActionList<?,?>)actionList);
					d.activate();
				}
			}

		});
		return upButton;
	}

	public String getValueAsString() {
		try {
			if (f.getType() == Boolean.TYPE) {
				return String.valueOf(f.getBoolean(obj));
			} else if (f.getType() == Float.TYPE) {
				return String.valueOf(String.valueOf(f.getFloat(obj)));
			} else if (f.getType() == Long.TYPE) {
				return String.valueOf(String.valueOf(f.getLong(obj)));
			} else if (f.getType() == Short.TYPE) {
				return String.valueOf(String.valueOf(f.getShort(obj)));
			} else if (f.getType() == Integer.TYPE) {
				return String.valueOf(String.valueOf(f.getInt(obj)));
			} else if (f.getType() == Byte.TYPE) {
				return String.valueOf(String.valueOf(f.getByte(obj)));
			} else if (f.getType() == Double.TYPE) {
				return String.valueOf(String.valueOf(f.getDouble(obj)));
			} else if (f.getType() == String.class) {
				return f.get(obj).toString();
			} else if (f.getType() == ConditionList.class) {
				return Lng.str("Group");
			} else {
				return String.valueOf(f.get(obj).toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return e.getClass().getSimpleName();
		}
	}

	public void applyValueFromString(String trim) {
		try {
			if (f.getType() == Boolean.TYPE) {
				f.setBoolean(obj, Boolean.parseBoolean(trim));
			} else if (f.getType() == Float.TYPE) {
				f.setFloat(obj, Float.parseFloat(trim));
			} else if (f.getType() == Long.TYPE) {
				f.setLong(obj, Long.parseLong(trim));
			} else if (f.getType() == Short.TYPE) {
				f.setShort(obj, Short.parseShort(trim));
			} else if (f.getType() == Integer.TYPE) {
				f.setInt(obj, Integer.parseInt(trim));
			} else if (f.getType() == Byte.TYPE) {
				f.setByte(obj, Byte.parseByte(trim));
			} else if (f.getType() == Double.TYPE) {
				f.setDouble(obj, Double.parseDouble(trim));
			} else if (f.getType() == ConditionList.class) {
				throw new Exception("Can't be applied with string");
			} else if (f.getType() == String.class) {
				f.set(obj, trim);
			} else {
				throw new Exception("Can't be applied with string");
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			// catch everything but number format
			e.printStackTrace();
		}
	}
}
