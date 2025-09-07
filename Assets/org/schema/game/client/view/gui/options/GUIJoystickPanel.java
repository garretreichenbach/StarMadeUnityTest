package org.schema.game.client.view.gui.options;

import org.schema.schine.common.JoystickAxisMapping;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.*;

import javax.vecmath.Vector4f;
import java.util.ArrayList;
import java.util.Map.Entry;

public class GUIJoystickPanel extends GUIAnchor implements GUIChangeListener {

	private static final int TOP = 40;
	private GUIElementList list;
	private boolean reconstructNeeded;
	private boolean init;
	private GUITextOverlay overlayNoJoy;

	public GUIJoystickPanel(GUIScrollablePanel scrollPanel, InputState state) {
		super(state);
		list = new GUIElementList(state);
		list.setScrollPane(scrollPanel);
		reconstructNeeded = true;
		list.setPos(0, UIScale.getUIScale().scale(TOP), 0);
		attach(list);

		overlayNoJoy = new GUITextOverlay(getState());
		overlayNoJoy.setTextSimple(Lng.str("No joystick connected to system."));

	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#draw()
	 */
	@Override
	public void draw() {
		if(Controller.getControllerInput().getSize() == 0) {

			GlUtil.glPushMatrix();
			transform();
			overlayNoJoy.draw();
			GlUtil.glPopMatrix();

			return;
		}
		if(!init) {
			onInit();
			init = true;
		}
		if(reconstructNeeded) {
			reconstructList();
			reconstructNeeded = false;
		}
		super.draw();
	}

	@Override
	public void onInit() {
		GUITextOverlay[] joys = new GUITextOverlay[Controller.getControllerInput().getSize()];
		for(int i = 0; i < Controller.getControllerInput().getSize(); i++) {
			joys[i] = new GUITextOverlay(getState());
			String n = Controller.getControllerInput().getName(i);
			joys[i].setTextSimple(n.length() == 0 || n.equals("?") ? "unknown device" : n);
			joys[i].setUserPointer(i);
		}
		Integer currentState = EngineSettings.C_SELECTED_JOYSTICK.getInt();
		Controller.getControllerInput().select(currentState.intValue());

		GUIDropDownList l = new GUIDropDownList(getState(), UIScale.getUIScale().scale(300), UIScale.getUIScale().scale(32), UIScale.getUIScale().scale(300), element -> {
			if((EngineSettings.C_SELECTED_JOYSTICK.getInt()) != ((Integer) element.getContent().getUserPointer()).intValue()) {
				EngineSettings.C_SELECTED_JOYSTICK.setInt((Integer) element.getContent().getUserPointer());
				Controller.getControllerInput().select((EngineSettings.C_SELECTED_JOYSTICK.getInt()));
				System.err.println("JOYSTICK SET TO " + element.getContent().getUserPointer());
				reconstructNeeded = true;
			}
		}, joys);
		l.setSelectedIndex(currentState.intValue());

		l.onInit();
		attach(l);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#getHeight()
	 */
	@Override
	public float getHeight() {
		return list.getHeight() + UIScale.getUIScale().scale(TOP);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#getWidth()
	 */
	@Override
	public float getWidth() {
		return list.getWidth();
	}

	public void reconstructList() {

		list.clear();

		GUIJoystickVirt jVirt = new GUIJoystickVirt(getState());
		jVirt.onInit();

		list.add(new GUIListElement(jVirt, jVirt, getState()));

		final BasicInputController c = getState().getController().getInputController();

		GUIAnchor tab = new GUIAnchor(getState(), UIScale.getUIScale().scale(500), UIScale.getUIScale().scale(20));

		GUITextOverlay gameText = new GUITextOverlay(getState());
		gameText.setTextSimple("in-game function");
		gameText.getPos().x = UIScale.getUIScale().inset;

		GUITextOverlay joyText = new GUITextOverlay(getState());
		joyText.setTextSimple("assigned joystick axis");
		joyText.getPos().x = UIScale.getUIScale().scale(200);

		GUITextOverlay invText = new GUITextOverlay(getState());
		invText.setTextSimple("invert");
		invText.getPos().x = UIScale.getUIScale().scale(357);

		GUITextOverlay sensText = new GUITextOverlay(getState());
		sensText.setTextSimple("sensivity");
		sensText.getPos().x = UIScale.getUIScale().scale(412);

		tab.attach(gameText);
		tab.attach(joyText);
		tab.attach(invText);
		tab.attach(sensText);

		list.add(new GUIListElement(tab, tab, getState()));

		for(final Entry<JoystickAxisMapping, JoystickAxisSingleMap> m : c.getJoystick().getAxis().entrySet()) {

			GUISettingSelector s = new GUISettingSelector(getState(), UIScale.getUIScale().scale(100), UIScale.getUIScale().scale(30), null, new JoystickSettingInterface(c.getJoystick(), m.getKey(), m.getValue().mapping, getState()));
			GUITextOverlay name = new GUITextOverlay(getState());

			GUICheckBox invert = new GUICheckBox(getState()) {

				@Override
				protected void activate() throws StateParameterNotFoundException {
					c.getJoystick().invertedAxis(m.getKey(), true);
				}

				@Override
				protected void deactivate() throws StateParameterNotFoundException {
					c.getJoystick().invertedAxis(m.getKey(), false);
				}

				@Override
				protected boolean isActivated() {
					return c.getJoystick().isAxisInverted(m.getKey());
				}
			};

			GUISettingSelector sensivity = new GUISettingSelector(getState(), UIScale.getUIScale().scale(30), UIScale.getUIScale().scale(30), null, new IndexChoiceSetting(100) {

				@Override
				public void onSelectedValue(int v) {
					c.getJoystick().modSensivity(m.getKey(), (float) v * 0.1f);
				}

				@Override
				public String getNameAt(int i) {
					return String.valueOf(i);
				}

				@Override
				public int getCurrentValue() {
					return (int) (c.getJoystick().getSensivity(m.getKey()) / 0.1f);
				}

			});

			//					new SettingsInterface() {
			//
			//				@Override
			//				public void switchSetting() throws StateParameterNotFoundException {
			//					if (c.getJoystick().getSensivity(m.getKey()) < 1000.0f) {
			//						c.getJoystick().modSensivity(m.getKey(), 0.1f);
			//					}
			//				}
			//
			//				@Override
			//				public Object getCurrentState() {
			//					return c.getJoystick().getSensivity(m.getKey());
			//				}
			//
			//				@Override
			//				public void switchSettingBack() throws StateParameterNotFoundException {
			//					if (c.getJoystick().getSensivity(m.getKey()) > 0.1f) {
			//						c.getJoystick().modSensivity(m.getKey(), -0.1f);
			//					}
			//				}
			//
			//
			//				@Override
			//				public void addChangeListener(EngineSettingsChangeListener c) {
			//				}
			//
			//				@Override
			//				public void removeChangeListener(EngineSettingsChangeListener c) {
			//				}
			//			});
			sensivity.onInit();

			name.setTextSimple(m.getKey().desc);

			GUIAnchor a = new GUIAnchor(getState(), UIScale.getUIScale().scale(500), UIScale.getUIScale().scale(32));
			s.getPos().x = UIScale.getUIScale().scale(180);

			invert.getPos().x = s.getPos().x + s.getWidth() + UIScale.getUIScale().scale(10);
			sensivity.getPos().x = invert.getPos().x + UIScale.getUIScale().scale(35);

			a.attach(name);
			a.attach(s);
			a.attach(invert);
			a.attach(sensivity);

			list.add(new GUIListElement(a, a, getState()));
		}

		list.add(new GUIMappingInputPanel(getState(), "Primary Action", new GUIAbstractJoystickElement(getState()) {

			@Override
			public boolean hasDuplicate() {
				if(!c.getJoystick().getLeftMouse().isSet()) {
					return false;
				}
				if(c.getJoystick().getLeftMouse().equals(c.getJoystick().getRightMouse())) {
					return true;
				}
				for(KeyboardMappings m : KeyboardMappings.values()) {
					if(c.getJoystick().getLeftMouse().equals(c.getJoystick().getButtonFor(m))) {
						// duplicate key
						return true;
					}

				}
				return false;
			}

			@Override
			public void mapJoystickPressedNothing() {
				c.getJoystick().setLeftMouse(new JoystickButtonMapping());
			}

			@Override
			public boolean isHighlighted() {
				return hasDuplicate();
			}

			@Override
			public void mapJoystickPressed(JoystickEvent e) {
				c.getJoystick().setLeftMouse(JoystickMappingFile.getPressedButton());
			}

			@Override
			public String getDesc() {
				return "Primary Action";
			}

			@Override
			public String getCurrentSettingString() {
				return c.getJoystick().getLeftMouse().toString();
			}

		}, list.size() % 2 == 0));
		list.add(new GUIMappingInputPanel(getState(), "Secondary Action", new GUIAbstractJoystickElement(getState()) {
			@Override
			public boolean isHighlighted() {
				return hasDuplicate();
			}

			@Override
			public void mapJoystickPressedNothing() {
				c.getJoystick().setRightMouse(new JoystickButtonMapping());
			}

			@Override
			public void mapJoystickPressed(JoystickEvent e) {
				c.getJoystick().setRightMouse(JoystickMappingFile.getPressedButton());
			}

			@Override
			public String getDesc() {
				return "Secondary Action";
			}

			@Override
			public String getCurrentSettingString() {
				return c.getJoystick().getRightMouse().toString();
			}

			@Override
			public boolean hasDuplicate() {
				if(!c.getJoystick().getRightMouse().isSet()) {
					return false;
				}
				if(c.getJoystick().getLeftMouse().equals(c.getJoystick().getRightMouse())) {
					return true;
				}
				for(KeyboardMappings m : KeyboardMappings.values()) {
					if(c.getJoystick().getRightMouse().equals(c.getJoystick().getButtonFor(m))) {
						// duplicate key
						return true;
					}

				}
				return false;
			}
		}, list.size() % 2 == 0));
		createButtons();

		list.onInit();

	}

	private void createButtons() {

		BasicInputController con = getState().getController().getInputController();

		ArrayList<GUIListElement> ls = new ArrayList<GUIListElement>();
		for(KeyboardContext c : KeyboardContext.values()) {

			GUITextOverlay col = new GUITextOverlay(FontSize.SMALL_15, getState());
			col.setText(new ArrayList());
			col.getText().add("+ " + c.getDesc());
			col.getPos().y += UIScale.getUIScale().scale(8);

			GUIColoredRectangle rBack = new GUIColoredRectangle(getState(), 468, 30, new Vector4f(0, 0, 0, 0.8f));
			GUITextOverlay back = new GUITextOverlay(FontSize.SMALL_15, getState());
			back.setText(new ArrayList());
			back.getText().add(c.getDesc());
			back.setMouseUpdateEnabled(true);
			back.getPos().y += UIScale.getUIScale().scale(8);
			rBack.attach(back);

			GUIEnterableList category = new GUIEnterableList(getState(), col, rBack);

			category.getPos().x = c.getLvl() * UIScale.getUIScale().scale(5);

			category.addObserver(this);
			category.setUserPointer("CATEGORY");
			category.onInit();
			category.setMouseUpdateEnabled(true);

			category.setExpanded(true);

			GUIListElement listElement = new GUIListElement(category, category, getState());
			category.setParent(this);
			list.add(listElement);
			ls.add(listElement);

		}
		for(KeyboardMappings s : KeyboardMappings.values()) {
			GUIEnterableList guiListElement = (GUIEnterableList) ls.get(s.getContext().ordinal()).getContent();
			guiListElement.getList().add(new GUIMappingInputPanel(getState(), s.getDescription(), new GUIJoystickElement(getState(), s, con.getJoystick()), guiListElement.getList().size() % 2 == 0));
		}
		list.updateDim();
	}

	@Override
	public void onChange(boolean updateListDim) {
		list.updateDim();
	}
}
