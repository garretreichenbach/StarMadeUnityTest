package org.schema.game.client.view.gui.reactor;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;

public class ReactorTreeDialog extends DialogInput{

	private final GUIReactorPanel p;
	public ReactorTreeDialog(GameClientState state, ManagedSegmentController<?> man) {
		super(state);
		p = new GUIReactorPanel(state, man, this);
		p.onInit();
		assert(man.getManagerContainer().getPowerInterface().getReactorSet().getTrees().size() > 0):"This Dialog should not be accessible";
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
		p.update(timer);
	}
	
}