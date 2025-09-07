package org.schema.game.client.view.gui.trade;

import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

public class GUITradePanelIndependent extends GUIInputPanel {

	public GUITradePanelIndependent(InputState state, int width, int height, 
			GUICallback guiCallback) {
		super("TradeNodePanelIndependent", state, width, height, guiCallback, Lng.str("Trade Nodes"), "");
		setOkButton(false);
		
		onInit();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.gui.GUIInputPanel#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();
		
		((GUIDialogWindow) background).getMainContentPane().setTextBoxHeightLast(UIScale.getUIScale().scale(10));
		GUITradeNodeScrollableList l = new GUITradeNodeScrollableList(getState(), null, ((GUIDialogWindow) background).getMainContentPane().getContent(0));
		l.onInit();
		((GUIDialogWindow) background).getMainContentPane().getContent(0).attach(l);
		
		
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#isActive()
	 */
	@Override
	public boolean isActive() {
		return (getState().getController().getPlayerInputs().isEmpty() || getState().getController().getPlayerInputs().get(getState().getController().getPlayerInputs().size() - 1).getInputPanel() == this) && super.isActive();
	}


}
