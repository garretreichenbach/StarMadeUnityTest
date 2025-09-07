package api.mod.gui.configui;

import api.mod.config.ModConfigData;
import org.json.JSONObject;
import org.schema.game.client.controller.GameMainMenuController;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.schine.common.TextAreaInput;
import org.schema.schine.common.TextCallback;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import static org.schema.game.common.data.element.Element.TOP;

/**
 * ConfigEditWindow is a GUI for editing mod configuration files.
 */
public class ModConfigEditWindow extends DialogInput {

	private final ModConfigEditWindow.ModConfigEditPanel inputPanel;

	public ModConfigEditWindow(GameMainMenuController state, ModConfigData configData) {
		super(state);
		(inputPanel = new ModConfigEditWindow.ModConfigEditPanel(getState(), configData, this)).onInit();
	}

	@Override
	public ModConfigEditWindow.ModConfigEditPanel getInputPanel() {
		return inputPanel;
	}

	@Override
	public void onDeactivate() {

	}

	@Override
	public void callback(GUIElement callingElement, MouseEvent mouseEvent) {
		if(mouseEvent.pressedLeftMouse()) {
			if(callingElement.getUserPointer().equals("CANCEL") || callingElement.getUserPointer().equals("X")) {
				AudioController.fireAudioEventID(225);
				cancel();
			} else if(callingElement.getUserPointer().equals("OK")) {
				for(GUIListElement element : inputPanel.list.getElements()) {
					if(element instanceof ModConfigStringInputPanel stringInput) {
						inputPanel.configData.config.put(stringInput.getName(), stringInput.getValue());
					} else if(element instanceof ModConfigBooleanInputPanel booleanInput) {
						inputPanel.configData.config.put(booleanInput.getName(), booleanInput.getValue());
					} else if(element instanceof ModConfigNumberInputPanel numberInput) {
						inputPanel.configData.config.put(numberInput.getName(), numberInput.getValue());
					}
				}
			}
		}
	}

	public static class ModConfigEditPanel extends GUIInputPanel implements GUIChangeListener {

		public final ModConfigData configData;
		private GUIElementList list;
		private boolean reconstructNeeded;
		private boolean initialized;

		public ModConfigEditPanel(InputState inputState, ModConfigData configData, GUICallback guiCallback) {
			super(configData.getMod().getName().replaceAll(" ", "_"), inputState, 500, 300, guiCallback, configData.getMod().getName(), "");
			this.configData = configData;
		}

		@Override
		public void onInit() {
			super.onInit();
			GUIAnchor anchor = new GUIAnchor(getState(), 0.0F, 0.0F);
			GUIScrollablePanel scrollPanel = new GUIScrollablePanel(10, 10, getContent(), getState());
			scrollPanel.setContent(anchor);
			getContent().attach(scrollPanel);
			list = new GUIElementList(getState());
			list.setScrollPane(scrollPanel);
			list.setPos(0, UIScale.getUIScale().scale(TOP), 0);
			anchor.attach(list);
			reconstructNeeded = true;
			JSONObject jsonObject = configData.config;
			for(Object key : jsonObject.keySet()) {
				Object value = jsonObject.get((String) key);
				if(!key.toString().endsWith("_description")) {
					String description = jsonObject.get(key + "_description") != null ? jsonObject.get(key + "_description").toString() : "";
					if(value instanceof String) {
						list.add(new ModConfigStringInputPanel(getState(), (String) key, description, (String) value));
					} else if(value instanceof Boolean) {
						list.add(new ModConfigBooleanInputPanel(getState(), (String) key, description, (Boolean) value));
					} else if(value instanceof Number) {
						list.add(new ModConfigNumberInputPanel(getState(), (String) key, description, ((Number) value).floatValue()));
					}
				}
			}
			initialized = true;
			reconstructNeeded = false;
		}

		@Override
		public void draw() {
			if(!initialized || reconstructNeeded) onInit();
			super.draw();
		}

		@Override
		public void onChange(boolean updateListDim) {
			list.updateDim();
			reconstructNeeded = true;
		}
	}

	public static class ModConfigStringInputPanel extends GUIListElement implements TooltipProviderCallback {

		private final GUITextInput settingElement;
		private boolean init;
		private GUIToolTip toolTip;

		public ModConfigStringInputPanel(InputState state, String name, String description, String value) {
			super(state);
			toolTip = new GUIToolTip(state, description, this);
			settingElement = new GUITextInput(10, 10, getState());
			settingElement.setText(value);
			setName(name);
			setContent(this);
			GUIAnchor anchor = new GUIAnchor(getState(), 0.0F, 0.0F) {
				@Override
				public void draw() {
					setWidth(settingElement.getWidth());
					setHeight(settingElement.getHeight());
					super.draw();
				}
			};
			anchor.attach(settingElement);
			setContent(anchor);
		}

		@Override
		public void draw() {
			if(!init) onInit();
			GlUtil.glPushMatrix();
			transform();
			settingElement.draw();
			GlUtil.glPopMatrix();
		}

		@Override
		public void onInit() {
			settingElement.onInit();
			int centerYTrans = UIScale.getUIScale().scale(9);
			settingElement.getPos().y += centerYTrans;
			init = true;
		}

		@Override
		public float getHeight() {
			return settingElement.getHeight() + UIScale.getUIScale().scale(5);
		}

		@Override
		public float getWidth() {
			return settingElement.getWidth() + UIScale.getUIScale().scale(5);
		}

		@Override
		public GUIToolTip getToolTip() {
			return toolTip;
		}

		@Override
		public void setToolTip(GUIToolTip toolTip) {
			this.toolTip = toolTip;
		}

		public String getValue() {
			return settingElement.getText();
		}
	}

	public static class ModConfigBooleanInputPanel extends GUIListElement implements TooltipProviderCallback {

		private GUIToolTip toolTip;
		private final GUICheckBox settingElement;
		private boolean init;
		private boolean value;

		public ModConfigBooleanInputPanel(InputState state, String name, String description, boolean value) {
			super(state);
			this.value = value;
			toolTip = new GUIToolTip(state, description, this);
			settingElement = new GUICheckBox(state) {

				@Override
				protected void activate() throws StateParameterNotFoundException {
					ModConfigBooleanInputPanel.this.value = true;
				}

				@Override
				protected void deactivate() throws StateParameterNotFoundException {
					ModConfigBooleanInputPanel.this.value = false;
				}

				@Override
				protected boolean isActivated() {
					return ModConfigBooleanInputPanel.this.value;
				}
			};
			setName(name);
			setContent(this);
			GUIAnchor anchor = new GUIAnchor(getState(), 0.0F, 0.0F) {
				@Override
				public void draw() {
					setWidth(settingElement.getWidth());
					setHeight(settingElement.getHeight());
					super.draw();
				}
			};
			anchor.attach(settingElement);
			setContent(anchor);
		}

		@Override
		public void draw() {
			if(!init) onInit();
			GlUtil.glPushMatrix();
			transform();
			settingElement.draw();
			GlUtil.glPopMatrix();
		}

		@Override
		public void onInit() {
			settingElement.onInit();
			int centerYTrans = UIScale.getUIScale().scale(9);
			settingElement.getPos().y += centerYTrans;
			init = true;
		}

		@Override
		public float getHeight() {
			return settingElement.getHeight() + UIScale.getUIScale().scale(5);
		}

		@Override
		public float getWidth() {
			return settingElement.getWidth() + UIScale.getUIScale().scale(5);
		}

		@Override
		public GUIToolTip getToolTip() {
			return toolTip;
		}

		@Override
		public void setToolTip(GUIToolTip toolTip) {
			this.toolTip = toolTip;
		}

		public boolean getValue() {
			return value;
		}
	}

	public static class ModConfigNumberInputPanel extends GUIListElement implements TooltipProviderCallback {

		private GUIToolTip toolTip;
		private final GUITextInput settingElement;
		private boolean init;
		private final float[] value;

		public ModConfigNumberInputPanel(InputState state, String name, String description, float value) {
			super(state);
			this.value = new float[] {value};
			settingElement = new GUITextInput(10, 10, getState());
			settingElement.setTextInput(new TextAreaInput(16, 1, new TextCallback() {
				@Override
				public String[] getCommandPrefixes() {
					return new String[0];
				}

				@Override
				public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
					return "";
				}

				@Override
				public void onFailedTextCheck(String msg) {

				}

				@Override
				public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {
					try {
						ModConfigNumberInputPanel.this.value[0] = Float.parseFloat(entry);
					} catch(NumberFormatException e) {
						ModConfigNumberInputPanel.this.value[0] = 0;
					}
				}

				@Override
				public void newLine() {

				}
			}));
			settingElement.setText(value + "");
			setName(name);
			setContent(this);
			GUIAnchor anchor = new GUIAnchor(getState(), 0.0F, 0.0F) {
				@Override
				public void draw() {
					setWidth(settingElement.getWidth());
					setHeight(settingElement.getHeight());
					super.draw();
				}
			};
			anchor.attach(settingElement);
			setContent(anchor);
		}

		@Override
		public void draw() {
			if(!init) onInit();
			GlUtil.glPushMatrix();
			transform();
			settingElement.draw();
			GlUtil.glPopMatrix();
		}

		@Override
		public void onInit() {
			settingElement.onInit();
			int centerYTrans = UIScale.getUIScale().scale(9);
			settingElement.getPos().y += centerYTrans;
			init = true;
		}

		@Override
		public float getHeight() {
			return settingElement.getHeight() + UIScale.getUIScale().scale(5);
		}

		@Override
		public float getWidth() {
			return settingElement.getWidth() + UIScale.getUIScale().scale(5);
		}

		@Override
		public GUIToolTip getToolTip() {
			return toolTip;
		}

		@Override
		public void setToolTip(GUIToolTip toolTip) {
			this.toolTip = toolTip;
		}

		public float getValue() {
			return value[0];
		}
	}
}
