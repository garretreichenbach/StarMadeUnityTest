package api.mod.gui.configui;

import org.schema.game.client.controller.GameMainMenuController;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.client.view.mainmenu.MainMenuInputDialog;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIMainWindow;
import org.schema.schine.input.InputState;

/**
 * Menu for browsing mod configs.
 *
 * @author TheDerpGamer
 */
public class ModConfigBrowser extends MainMenuInputDialog {

	private final ModConfigBrowserPanel configPanel;

	public ModConfigBrowser(GameMainMenuController state) {
		super(state);
		(configPanel = new ModConfigBrowserPanel(state, this)).onInit();
	}

	@Override
	public GUIElement getInputPanel() {
		return configPanel;
	}

	@Override
	public void onDeactivate() {
		configPanel.cleanUp();
	}

	@Override
	public boolean isInside() {
		return configPanel.isInside();
	}

	public static class ModConfigBrowserPanel extends GUIElement implements GUIActiveInterface {

		public GUIMainWindow mainPanel;
		private boolean initialized;
		private final DialogInput dialogInput;
		private GUIContentPane settingsTab;
		private GUIContentPane controlsTab;

		public ModConfigBrowserPanel(InputState state, DialogInput dialogInput) {
			super(state);
			this.dialogInput = dialogInput;
		}

		@Override
		public void onInit() {
			mainPanel = new GUIMainWindow(getState(), GLFrame.getWidth() - 410, GLFrame.getHeight() - 20, 400, 10, "Mod Manager");
			mainPanel.onInit();
			mainPanel.setPos(435, 35, 0);
			mainPanel.setWidth(GLFrame.getWidth() - 470);
			mainPanel.setHeight(GLFrame.getHeight() - 70);
			mainPanel.clearTabs();

			(settingsTab = createSettingsTab()).onInit();
			(controlsTab = createControlsTab()).onInit();

			mainPanel.setSelectedTab(0);
			mainPanel.activeInterface = this;

			mainPanel.setCloseCallback(new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse()) dialogInput.deactivate();
				}
			});
			initialized = true;
		}

		@Override
		public void draw() {
			if(!initialized) onInit();
			mainPanel.draw();
		}

		@Override
		public void cleanUp() {
			mainPanel.cleanUp();
		}

		@Override
		public float getWidth() {
			return GLFrame.getWidth() - 470;
		}

		@Override
		public float getHeight() {
			return GLFrame.getHeight() - 70;
		}

		private GUIContentPane createSettingsTab() {
			GUIContentPane contentPane = mainPanel.addTab(Lng.str("SETTINGS"));
			ModConfigScrollableList scrollableList = new ModConfigScrollableList(getState(), getWidth(), getHeight(), contentPane);
			scrollableList.onInit();
			contentPane.getContent(0, 0).attach(scrollableList);
			return contentPane;
		}

		private GUIContentPane createControlsTab() {
			GUIContentPane contentPane = mainPanel.addTab(Lng.str("CONTROLS"));
			ModControlScrollableList scrollableList = new ModControlScrollableList(getState(), getWidth(), getHeight(), contentPane);
			scrollableList.onInit();
			contentPane.getContent(0, 0).attach(scrollableList);
			return contentPane;
		}
	}
}
