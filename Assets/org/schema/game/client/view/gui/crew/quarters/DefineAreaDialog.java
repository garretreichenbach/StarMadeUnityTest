package org.schema.game.client.view.gui.crew.quarters;

import api.common.GameClient;
import org.schema.game.client.view.AreaDefineDrawer;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.client.view.gui.GUISizeSettingSelectorScroll;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUISettingsListElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButton;
import org.schema.schine.input.InputState;

/**
 * [Description]
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class DefineAreaDialog extends DialogInput {
	private final DefineAreaPane inputPanel;

	public DefineAreaDialog(InputState state) {
		super(state);
		inputPanel = new DefineAreaPane(getState(), this);
		inputPanel.setCallback(this);
		inputPanel.setOkButtonText("SET AREA");
		inputPanel.onInit();
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		boolean set = false;
		if(event.pressedLeftMouse()) {
			if(callingGuiElement != null && callingGuiElement.getUserPointer() instanceof GUIHorizontalButton button) {
				if("SET AREA".equals(button.getTextToString())) {
					set = true;
				}
			}
		}
		AreaDefineDrawer.endAreaDefine(set);
		onDeactivate();
	}

	@Override
	public DefineAreaPane getInputPanel() {
		return inputPanel;
	}

	@Override
	public void onDeactivate() {
		inputPanel.cleanUp();
		GameClient.getClientState().getWorldDrawer().getAreaDefineDrawer().cleanUp();
	}

	public static class DefineAreaPane extends GUIInputPanel {
		public DefineAreaPane(InputState state, GUICallback callback) {
			super("DEFINE_AREA", state, 350, 116, callback, Lng.str("DEFINE AREA"), "");
		}

		public void onInit() {
			super.onInit();
			//Todo: Type specific size limits
			QuarterAreaSetting xMin = new QuarterAreaSetting(setting -> AreaDefineDrawer.min.x = setting.get(), -15, 15);
			QuarterAreaSetting xMax = new QuarterAreaSetting(setting -> AreaDefineDrawer.max.x = setting.get(), -15, 15);
			QuarterAreaSetting yMin = new QuarterAreaSetting(setting -> AreaDefineDrawer.min.y = setting.get(), -15, 15);
			QuarterAreaSetting yMax = new QuarterAreaSetting(setting -> AreaDefineDrawer.max.y = setting.get(), -15, 15);
			QuarterAreaSetting zMin = new QuarterAreaSetting(setting -> AreaDefineDrawer.min.z = setting.get(), -15, 15);
			QuarterAreaSetting zMax = new QuarterAreaSetting(setting -> AreaDefineDrawer.max.z = setting.get(), -15, 15);

			GUIElementList topList = new GUIElementList(getState());
			GUIElementList bottomList = new GUIElementList(getState());

			topList.add(new GUISettingsListElement(getState(), "X MIN", new GUISizeSettingSelectorScroll(getState(), xMin), true, false));
			topList.add(new GUISettingsListElement(getState(), "Y MIN", new GUISizeSettingSelectorScroll(getState(), yMin), true, false));
			topList.add(new GUISettingsListElement(getState(), "Z MIN", new GUISizeSettingSelectorScroll(getState(), zMin), true, false));

			bottomList.add(new GUISettingsListElement(getState(), "X MAX", new GUISizeSettingSelectorScroll(getState(), xMax), true, false));
			bottomList.add(new GUISettingsListElement(getState(), "Y MAX", new GUISizeSettingSelectorScroll(getState(), yMax), true, false));
			bottomList.add(new GUISettingsListElement(getState(), "Z MAX", new GUISizeSettingSelectorScroll(getState(), zMax), true, false));

			topList.setPos(0, 0, 0);
			bottomList.setPos(0, topList.getHeight() + 4, 0);

			getContent().attach(topList);
			getContent().attach(bottomList);
		}
	}
}
