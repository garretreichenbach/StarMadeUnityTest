package org.schema.game.client.view.gui.trade;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.common.controller.ShopInterface;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIChangeListener;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

public class GUITradeNodeSearchInputPanel extends GUIInputPanel implements GUIChangeListener{

	private boolean init;
	final int rowHeight = 24;
	private GUIElementList mainList;
	private GUIActiveInterface dialog;
	
	
	
	
	@Override
	public boolean isActive() {
		return super.isActive() && dialog.isActive();
	}

	public GUITradeNodeSearchInputPanel(InputState state, GUIActiveInterface dialog, ElementInformation searchInfo, ShopInterface currentClosestShop, TradeNodeTypeSearchDialog tradeNodeTypeSearchDialog) throws ShopNotFoundException {
		super("ORDER_PNLMA", state, UIScale.getUIScale().scale(800), UIScale.getUIScale().scale(600), tradeNodeTypeSearchDialog, Lng.str("SEARCH RESULT FOR %s",searchInfo.getName()), "");
		this.dialog = dialog;
		onInit();
		
		GUIDialogWindow w = (GUIDialogWindow)getBackground();
		w.getMainContentPane().setTextBoxHeightLast(UIScale.getUIScale().scale(148));
		
		((GameClientState)state).getController().getClientChannel().requestTradeNodesFor(searchInfo.id);
		TradeTypeSearchNodeScrollableList orderList = new TradeTypeSearchNodeScrollableList(getState(), currentClosestShop, w.getMainContentPane().getContent(0));
		orderList.onInit();
		w.getMainContentPane().getContent(0).attach(orderList);
		
		
	}

	
	@Override
	public void onInit() {
		if(init){
			return;
		}
		super.onInit();
		
		this.init = true;
		
	}
	
	@Override
	public void draw() {
		assert(init);
		super.draw();
	}


	@Override
	public void onChange(boolean updateListDim) {
		if(updateListDim) {
			mainList.updateDim();
		}
	}
	
	
	

}
