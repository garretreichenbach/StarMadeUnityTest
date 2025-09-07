package api.mod.gui.configui;

import api.mod.config.ModControlData;
import org.schema.game.client.controller.GameMainMenuController;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.client.view.gui.options.GUIAbstractJoystickElement;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.*;

import java.util.Map;

import static org.schema.game.common.data.element.Element.TOP;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class ModControlEditWindow extends DialogInput {

	private final ModControlEditPanel inputPanel;

	public ModControlEditWindow(GameMainMenuController state, ModControlData controlData) {
		super(state);
		(inputPanel = new ModControlEditPanel(getState(), controlData, this)).onInit();
	}

	@Override
	public ModControlEditPanel getInputPanel() {
		return inputPanel;
	}

	@Override
	public void onDeactivate() {

	}

	@Override
	public void callback(GUIElement callingElement, MouseEvent mouseEvent) {
	}

	public static class ModControlEditPanel extends GUIInputPanel implements GUIChangeListener {

		private final ModControlData controlData;
		private GUIElementList list;
		private boolean reconstructNeeded;
		private boolean initialized;

		public ModControlEditPanel(InputState inputState, ModControlData controlData, GUICallback guiCallback) {
			super(controlData.getMod().getName().replaceAll(" ", "_"), inputState, 500, 300, guiCallback, controlData.getMod().getName(), "");
			this.controlData = controlData;
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

			BasicInputController inputController = getState().getController().getInputController();
			for(Map.Entry<String, String[]> entry : controlData.values.entrySet()) {
				list.add(new ModControlMappingInputPanel(getState(), entry.getKey(), new GUIAbstractJoystickElement(getState()) {
					@Override
					public boolean hasDuplicate() {
						if(!inputController.getJoystick().getRightMouse().isSet()) return false;
						if(inputController.getJoystick().getLeftMouse().equals(inputController.getJoystick().getRightMouse())) return true;
						for(Map.Entry<String, String[]> entry : controlData.values.entrySet()) {
							//I don't know how this works lol
							if(entry.getValue()[0].equals(String.valueOf(inputController.getJoystick().getRightMouse().buttonId))) return true;
						}
						return false;
					}

					@Override
					public String getDesc() {
						return entry.getValue()[1];
					}

					@Override
					public void mapJoystickPressed(JoystickEvent e) {
						inputController.getJoystick().setLeftMouse(JoystickMappingFile.getPressedButton());
					}

					@Override
					public void mapJoystickPressedNothing() {
						inputController.getJoystick().setLeftMouse(new JoystickButtonMapping());
					}

					@Override
					public String getCurrentSettingString() {
						return inputController.getJoystick().getLeftMouse().toString();
					}

					@Override
					public boolean isHighlighted() {
						return hasDuplicate();
					}
				}, list.size() % 2 == 0));
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
}