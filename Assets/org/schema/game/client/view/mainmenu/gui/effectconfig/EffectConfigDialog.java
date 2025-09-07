package org.schema.game.client.view.mainmenu.gui.effectconfig;

import org.schema.game.client.controller.GameMainMenuController;
import org.schema.game.client.view.mainmenu.MainMenuInputDialog;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;

public class EffectConfigDialog extends MainMenuInputDialog{

	private final GUIEffectConfigPanel p;
	private GUIEffectStat stat;
	public EffectConfigDialog(GameMainMenuController state, GUIEffectStat stat) {
		super(state);
		p = new GUIEffectConfigPanel(state, null, stat, this);
		p.onInit();
		this.stat = stat;
		
	}


	@Override
	public GUIElement getInputPanel() {
		return p;
	}

	@Override
	public void onDeactivate() {
		p.cleanUp();
	}


	@Override
	public void update(Timer timer) {
		super.update(timer);
		stat.updateLocal(timer);
	}

	@Override
	public boolean isInside() {
		return p.isInside();
	}
	
}