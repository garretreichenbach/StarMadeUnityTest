package org.schema.game.client.view.gui.manualtrade;

import org.schema.game.client.controller.PlayerManualTradeOverviewInput;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

public class GUIPlayerTradePlayerOverviewPanel extends GUIInputPanel implements GUIActiveInterface {

	private PlayerManualTradeOverviewInput dialog;

	public GUIPlayerTradePlayerOverviewPanel(InputState state, int width, int height,
	                               PlayerManualTradeOverviewInput guiCallback) {
		super("GUIPlayerTradePlayerOverviewPanel", state, width, height, guiCallback, "Players in Range", "");
		setOkButton(false);
		this.dialog = guiCallback;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.gui.GUIInputPanel#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();
		GUIContentPane mc = ((GUIDialogWindow) background).getMainContentPane();
		
		mc.setTextBoxHeightLast(UIScale.getUIScale().scale(30));
		
		GUIPlayerInRangeScrollableList l = new GUIPlayerInRangeScrollableList(getState(), mc.getContent(0), dialog);
		l.onInit();
		mc.getContent(0).attach(l);
	}

	



	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#isActive()
	 */
	@Override
	public boolean isActive() {
		return (getState().getController().getPlayerInputs().isEmpty() || getState().getController().getPlayerInputs().get(getState().getController().getPlayerInputs().size() - 1).getInputPanel() == this) && super.isActive();
	}

	

}
