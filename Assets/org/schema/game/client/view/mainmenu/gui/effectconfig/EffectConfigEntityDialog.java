package org.schema.game.client.view.mainmenu.gui.effectconfig;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.common.data.blockeffects.config.ConfigManagerInterface;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;

public class EffectConfigEntityDialog extends DialogInput{

	private final GUIEffectConfigPanel p;
	private GUIEffectStat stat;
	public EffectConfigEntityDialog(GameClientState state, ConfigManagerInterface man, GUIEffectStat stat) {
		super(state);
		p = new GUIEffectConfigPanel(state, man, stat, this);
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
	
}