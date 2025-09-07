package org.schema.game.client.view.mainmenu.gui;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.settings.*;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.language.Translatable;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.core.settings.EngineSettingsChangeListener;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActivatableTextBar;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.graphicsengine.forms.gui.newgui.settingsnew.GUIScrollSettingSelector;
import org.schema.schine.input.InputState;

import java.io.IOException;
import java.util.List;

public enum SimpleSettings implements SettingsInterface {

	PLAYER_NAME(() -> new SettingStateString(EngineSettings.OFFLINE_PLAYER_NAME.getString()), (Enum en) -> Lng.str("Ingame Player Name"), (Enum en) -> Lng.str("PLEASE ENTER PLAYERNAME")),
	TUTORIAL(() -> new SettingStateBoolean(EngineSettings.TUTORIAL_NEW.isOn()), (Enum en) -> Lng.str("Tutorial Enabled")),
	DIFFICULTY(() -> new SettingStateEnum<>(Difficulty.valueOf(new Object() {
		@Override
		public String toString() {
			return switch(ServerConfig.AI_WEAPON_AIMING_ACCURACY.getInt()) {
				case 1 -> "VERY_EASY";
				case 10 -> "EASY";
				case 2000 -> "HARD";
				case 5000 -> "MEAN";
				case 10000 -> "CRUSHING";
				default -> "NORMAL";
			};
		}
	}.toString()), Difficulty.values()), (Enum en) -> Lng.str("Game Difficulty")), //Todo: This setting should affect more than ai aiming accuracy. It should also change things like enemy spawning rates, ship sizes, etc.
	CREATIVE_MODE(() -> new SettingStateBoolean(EngineSettings.G_SINGLEPLAYER_CREATIVE_MODE.isOn()), (Enum en) -> Lng.str("Creative Mode Inventory"));

	public final List<EngineSettingsChangeListener> changeListeners = new ObjectArrayList<>();
	private final Translatable description;
	private final Translatable emptyFieldText;
	private final SettingState s;

	SimpleSettings(SettingState.SettingStateValueFac fac, Translatable description, Translatable emptyFieldText) {
		this.description = description;
		s = fac.inst();
		this.emptyFieldText = emptyFieldText;
		addChangeListener(SimpleSettings::onSettingChanged);
	}

	SimpleSettings(SettingState.SettingStateValueFac fac, Translatable description) {
		this(fac, description, Translatable.DEFAULT);
	}

	private static void onSettingChanged(SettingsInterface setting) {
		switch((SimpleSettings) setting) {
			case PLAYER_NAME:
				PLAYER_NAME.setString(((SettingStateString) setting).getString());
				break;
			case TUTORIAL:
				TUTORIAL.setOn(((SettingStateBoolean) setting).isOn());
				break;
			case DIFFICULTY:
				DIFFICULTY.setInt(((SettingStateEnum) setting).getInt());
				break;
			case CREATIVE_MODE:
				CREATIVE_MODE.setOn(((SettingStateBoolean) setting).isOn());
				break;
			default:
				break;
		}
	}

	public String getDescription() {
		return description.getName(this);
	}

	public String getEmptyFieldText() {
		return emptyFieldText.getName(this);
	}

	private void onStateChanged(Object newState) {
		switch(this) {
			case DIFFICULTY:
				Difficulty d = (Difficulty) newState;
				switch(d) {
					case VERY_EASY:
						ServerConfig.AI_WEAPON_AIMING_ACCURACY.setInt(1);
						break;
					case EASY:
						ServerConfig.AI_WEAPON_AIMING_ACCURACY.setInt(10);
						break;
					case NORMAL:
						ServerConfig.AI_WEAPON_AIMING_ACCURACY.setInt(200);
						break;
					case HARD:
						ServerConfig.AI_WEAPON_AIMING_ACCURACY.setInt(2000);
						break;
					case MEAN:
						ServerConfig.AI_WEAPON_AIMING_ACCURACY.setInt(5000);
						break;
					case CRUSHING:
						ServerConfig.AI_WEAPON_AIMING_ACCURACY.setInt(10000);
						break;
					default:
						throw new IllegalArgumentException();
				}
				try {
					ServerConfig.write();
				} catch(IOException e) {
					e.printStackTrace();
				}
				break;
			case PLAYER_NAME:
				EngineSettings.OFFLINE_PLAYER_NAME.setString(newState.toString());
				System.err.println("[GUI] Offline Player Name Changed to " + newState);
				try {
					EngineSettings.write();
				} catch(IOException e) {
					e.printStackTrace();
				}
				break;
			case TUTORIAL:
				EngineSettings.TUTORIAL_NEW.setValueByObject(newState);
				try {
					EngineSettings.write();
				} catch(IOException e) {
					e.printStackTrace();
				}
				break;
			case CREATIVE_MODE:
				EngineSettings.G_SINGLEPLAYER_CREATIVE_MODE.setValueByObject(newState);
				try {
					EngineSettings.write();
				} catch(IOException e) {
					e.printStackTrace();
				}
				break;
			default:
				throw new IllegalArgumentException();
		}
	}

	/**
	 * Checks if is on.
	 *
	 * @return true, if is on
	 */
	public boolean isOn() {
		return s.isOn();
	}

	@Override
	public void setOn(boolean on) {
		s.setOn(on);
		onStateChanged(on);
	}

	@Override
	public void addChangeListener(EngineSettingsChangeListener c) {
		if(!changeListeners.contains(c)) {
			changeListeners.add(c);
		}
	}

	@Override
	public void removeChangeListener(EngineSettingsChangeListener c) {
		changeListeners.remove(c);
	}

	@Override
	public int getInt() {
		return s.getInt();
	}

	@Override
	public void setInt(int v) {
		s.setInt(v);
		onStateChanged(v);
	}

	@Override
	public float getFloat() {
		return s.getFloat();
	}

	@Override
	public void setFloat(float v) {
		s.setFloat(v);
		onStateChanged(v);
	}

	@Override
	public String getString() {
		return s.getString();
	}

	@Override
	public void setString(String v) {
		s.setString(v);
		onStateChanged(v);
	}

	public Object getObject() {
		return s.getObject();
	}

	public void setObject(Object o) {
		s.setValueByObject(o);
		onStateChanged(o);
	}

	public GUIElement getGUIElementTextBar(InputState state, GUIElement dependent, String deactText) {
		GUIActivatableTextBar element = new GUIActivatableTextBar(state, FontLibrary.FontSize.MEDIUM_15, deactText, dependent, new TextCallback() {
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
		}, t1 -> {
			try {
				setString(t1);
			} catch(NumberFormatException e) {
				e.printStackTrace();
			}
			return t1;
		}) {
			@Override
			protected void onBecomingInactive() {
				setText(getAsString());
			}

		};
		element.setDeleteOnEnter(false);
		element.setText(getAsString());
		return element;
	}

	public GUIElement getGUIElement(InputState state, GUIElement dependent) {
		return getGUIElement(state, dependent, s.getAsString());
	}

	public GUIElement getGUIElement(InputState state, GUIElement dependent, String deactText) {
		switch(s.getType()) {
			case BOOLEAN:
				GUICheckBox checkBox = new GUICheckBox(state) {

					@Override
					protected boolean isActivated() {
						return isOn();
					}

					@Override
					protected void deactivate() {
						setOn(false);
					}

					@Override
					protected void activate() {
						setOn(true);
					}
				};
				checkBox.activeInterface = dependent::isActive;
				checkBox.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
				return checkBox;
			case ENUM:
				SettingStateEnum e = (SettingStateEnum) s;
				int width = UIScale.getUIScale().scale(100);
				int heigth = UIScale.getUIScale().h;
				int i = 0;
				GUIElement[] elements = new GUIElement[e.getValues().length];
				int selIndex = -1;
				for(Object obj : e.getValues()) {
					GUITextOverlay o = new GUITextOverlay(FontLibrary.FontSize.MEDIUM_15, state);
					o.setTextSimple(new Object() {
						@Override
						public String toString() {
							return obj.toString().replaceAll("_", " ");
						}
					});
					if(obj == e.getValue()) selIndex = i;
					GUIAnchor a = new GUIAnchor(state, width, heigth) {
						@Override
						public void draw() {
							setWidth(dependent.getWidth());
							super.draw();
						}
					};
					o.getPos().x = UIScale.getUIScale().inset;
					o.getPos().y = UIScale.getUIScale().inset;
					a.attach(o);
					a.setUserPointer(obj);
					elements[i] = a;
					i++;
				}
				GUIDropDownList dropDown = new GUIDropDownList(state, width, heigth, 400, new DropDownCallback() {
					private boolean first = true;

					@Override
					public void onSelectionChanged(GUIListElement element) {
						if(first) {
							first = false;
							return;
						}
						Object inv = element.getContent().getUserPointer();
						setObject(inv);
					}
				}, elements);
				if(selIndex >= 0) dropDown.setSelectedIndex(selIndex);
				dropDown.dependend = dependent;
				return dropDown;
			case FLOAT:
			case INT:
			case LONG:
				return getTextBar(state, dependent, deactText);
			case STRING:
				return getGUIElementTextBar(state, dependent, getEmptyFieldText());
			default:
				throw new IllegalArgumentException();
		}
	}

	private GUIElement getTextBar(InputState state, GUIElement dependent, String deactText) {
		SettingStateFloat f = (SettingStateFloat) s;
		if(f.getPossibilities() == null) {
			if(f.getMin() > Float.NEGATIVE_INFINITY && f.getMax() < Float.POSITIVE_INFINITY) {
				GUIScrollSettingSelector scrollSetting = new GUIScrollSettingSelector(state, GUIScrollablePanel.SCROLLABLE_HORIZONTAL, 50, FontLibrary.FontSize.MEDIUM_15) {
					@Override
					public boolean isVerticalActive() {
						return false;
					}

					@Override
					public void settingChanged(Object setting) {
//								setFloat((Float) setting);
//								onStateChanged(setting);
					}

					@Override
					protected void setSettingY(float value) {
					}

					@Override
					public boolean showLabel() {
						return true;
					}
					@Override
					public void resetScrollValue(){
						setSettingX(f.getFloat());
					}
					@Override
					protected void setSettingX(float value) {
						f.setFloat(value);
						onStateChanged(value);
					}

					@Override
					protected void incSetting() {
						next();
						settingChanged(f.getFloat());
					}

					@Override
					protected float getSettingY() {
						return 0;
					}

					@Override
					protected float getSettingX() {
						return f.getFloat();
					}

					@Override
					public float getMaxY() {
						return 0;
					}

					@Override
					public float getMaxX() {
						return f.getMax();
					}

					@Override
					protected void decSetting() {
						previous();
						settingChanged(null);
					}

					@Override
					public float getMinX() {
						return f.getMin();
					}

					@Override
					public float getMinY() {
						return 0;
					}
				};
				scrollSetting.setNameLabel(new Object(){
					@Override
					public String toString() {
						return String.format("%.2f", f.getFloat());
					}

				});
				scrollSetting.labelPosition = GUIScrollSettingSelector.LabelPosition.RIGHT;
				scrollSetting.dep = dependent;
				scrollSetting.widthMod = -10;
				scrollSetting.posMoxX = 5;
				return scrollSetting;
			}
			return getGUIElementTextBar(state, dependent, deactText);
		}
		int w = UIScale.getUIScale().scale(100);
		int h = UIScale.getUIScale().h;
		int index = 0;
		GUIElement[] elements1 = new GUIElement[f.getPossibilities().length];
		int selIndex1 = -1;
		for(float el : f.getPossibilities()) {
			GUITextOverlay o = new GUITextOverlay(FontLibrary.FontSize.MEDIUM_15, state);
			o.setTextSimple(new Object() {
				@Override
				public String toString() {
					return String.valueOf(el);
				}
			});
			if(el == f.getFloat()) selIndex1 = index;
			GUIAnchor a = new GUIAnchor(state, w, h) {
				@Override
				public void draw() {
					setWidth(dependent.getWidth());
					super.draw();
				}
			};
			o.getPos().x = UIScale.getUIScale().inset;
			o.getPos().y = UIScale.getUIScale().inset;
			a.attach(o);
			a.setUserPointer(el);
			elements1[index] = a;
			index ++;
		}

		GUIDropDownList t = new GUIDropDownList(state, w, h, 400, new DropDownCallback() {
			private boolean first = true;

			@Override
			public void onSelectionChanged(GUIListElement element) {
				if(first) {
					first = false;
					return;
				}
				float inv = (float) element.getContent().getUserPointer();
				setFloat(inv);
			}

		}, elements1);
		if(selIndex1 >= 0)t.setSelectedIndex(selIndex1);
		t.dependend = dependent;
		return t;
	}

	@Override
	public void next() {
		s.next();
		onStateChanged(s.getObject());
	}

	@Override
	public void previous() {
		s.previous();
		onStateChanged(s.getObject());
	}

	@Override
	public String getAsString() {
		return s.getAsString();
	}

	public SettingsInterface getSettingsForGUI() {
		return this;
	}

	private enum Difficulty {
		VERY_EASY(en -> Lng.str("Very Easy")), EASY(en -> Lng.str("Easy")), NORMAL(en -> Lng.str("Normal")), HARD(en -> Lng.str("Hard")), MEAN(en -> Lng.str("Mean")), CRUSHING(en -> Lng.str("Crushing"));

		private final Translatable description;

		Difficulty(Translatable description) {
			this.description = description;
		}

		@Override
		public String toString() {
			return description.getName(this);
		}
	}
}
