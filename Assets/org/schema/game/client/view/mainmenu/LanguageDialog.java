package org.schema.game.client.view.mainmenu;

import java.util.Locale;

import org.schema.game.client.controller.GameMainMenuController;
import org.schema.game.client.controller.PlayerButtonTilesInput;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonColor;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

public class LanguageDialog extends PlayerButtonTilesInput implements MainMenuInputDialogInterface{


	public LanguageDialog(GameMainMenuController state) {
		super(null, state, UIScale.getUIScale().scale(650), UIScale.getUIScale().scale(510), Lng.str("Languagess"), UIScale.getUIScale().scale(200), UIScale.getUIScale().scale(100));
	}
	@Override
	public void onDeactivate() {
		
	}

	private void addLanguage(final String name, String desc){
		addTile(name, desc, HButtonColor.BLUE, 
				new GUICallback() {
					@Override
					public boolean isOccluded() {
						return !LanguageDialog.this.isActive();
					}
					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if(event.pressedLeftMouse()){
							getState().loadLanguage(name);
							deactivate();
						}
					}
				}, new GUIActivationCallback() {
					@Override
					public boolean isVisible(InputState state) {
						return true;
					}
					
					@Override
					public boolean isActive(InputState state) {
						return LanguageDialog.this.isActive() && EngineSettings.LANGUAGE_PACK.getString().toLowerCase(Locale.ENGLISH).equals(name.toLowerCase(Locale.ENGLISH));
					}
				});
	}
	@Override
	public boolean isActive() {
		return !MainMenuGUI.runningSwingDialog && (getState().getController().getPlayerInputs().isEmpty() 
				|| getState().getController().getPlayerInputs().get(getState().getController().getPlayerInputs().size() - 1) == this);
	}
	public void addToolsAndModsButtons() {
		
		addLanguage("English", "English");
		addLanguage("Polish", "jezyk polski");
		addLanguage("German", "Deutsch");
		addLanguage("Spanish", "Espaniol");
		addLanguage("French", "Francais");
		addLanguage("Russian", "Russkij Jazyk");
		addLanguage("Portuguese Brazilian", "portugues brasileiro");
		addLanguage("Japanese", "Nihon Go");
		addLanguage("Chinese Traditional", "Chinese Traditional\n");
		addLanguage("Chinese Simplified", "Chinese Simplified\n");
		addLanguage("Czech", "Cestina\n");
		
		
		
		
	}

	@Override
	public GameMainMenuController getState() {
		return (GameMainMenuController) super.getState();
	}


}
