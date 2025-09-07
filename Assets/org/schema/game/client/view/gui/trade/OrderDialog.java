package org.schema.game.client.view.gui.trade;

import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.PlayerInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.ShopInterface;
import org.schema.game.common.controller.trade.TradeNode;
import org.schema.game.common.controller.trade.TradeNodeClient;
import org.schema.game.common.controller.trade.TradeNodeStub;
import org.schema.game.common.controller.trade.TradeOrder;
import org.schema.game.common.controller.trade.TradingGuildTradeOrderConfig;
import org.schema.game.network.objects.TradePriceInterface;
import org.schema.game.network.objects.remote.RemoteTradeOrder;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.sound.controller.AudioController;

public class OrderDialog extends PlayerInput implements GUICallback {

	private final GUIOrderDialog orderDialog;

	private final TradeOrder tradeOrder;

	private TradeNodeClient tradeNodeTo;

	private ShopInterface currentClosestShop;

	public OrderDialog(GameClientState state, ShopInterface currentClosestShop, TradeNodeClient f) throws ShopNotFoundException {
		super(state);
		this.currentClosestShop = currentClosestShop;
		this.tradeNodeTo = f;
		this.tradeOrder = new TradeOrder(state, new TradingGuildTradeOrderConfig(), state.getCurrentClosestShop().getSegmentController().getDbId(), state.getCurrentClosestShop().getSegmentController().getSystem(new Vector3i()), f.getSystem(), f);
		this.orderDialog = new GUIOrderDialog(state, this, f, currentClosestShop, this, tradeOrder);
		this.orderDialog.onInit();
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		super.handleKeyEvent(e);
		// debug
		if (e.getKey() == GLFW.GLFW_KEY_PAGE_DOWN) {
			this.tradeNodeTo.dirty = true;
			System.err.println("[CLIENT] FORCING RE REQUEST " + tradeNodeTo.getEntityDBId());
			List<TradePriceInterface> tradePricesClient = this.tradeNodeTo.getTradePricesClient();
			System.err.println("[CLIENT] PRICES BEFORE REQUEST: " + tradePricesClient);
		}
	}

	@Override
	public GUIElement getInputPanel() {
		return orderDialog;
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (!isOccluded()) {
			if (event.pressedLeftMouse()) {
				if (callingGuiElement.getUserPointer().equals("OK")) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(714);
					pressedOK();
				}
				if (callingGuiElement.getUserPointer().equals("CANCEL") || callingGuiElement.getUserPointer().equals("X")) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(715);
					cancel();
				}
			}
		}
	}

	private void pressedOK() {
		TradeNode tradeNode = currentClosestShop.getTradeNode();
		TradeNodeStub tradeNodeStub = getState().getController().getClientChannel().getGalaxyManagerClient().getTradeNodeDataById().get(currentClosestShop.getSegmentController().getDbId());
		if (tradeNodeStub == null) {
			getState().getController().popupAlertTextMessage(Lng.str("Own shop not found!"), 0);
			deactivate();
		}
		// TradeNodeClient tradeNode = (TradeNodeClient)tradeNodeStub;
		List<TradePriceInterface> pricesA = currentClosestShop.getShoppingAddOn().getPricesRep();
		List<TradePriceInterface> pricesB = tradeNodeTo.getTradePricesClient();
		if (getState().getGameState().getTradeManager().checkTrade(tradeOrder, tradeNode, tradeNodeTo, pricesA, pricesB)) {
			getState().getController().getClientChannel().getNetworkObject().tradeOrderRequests.add(new RemoteTradeOrder(tradeOrder, false));
			// getState().getController().popupGameTextMessage(Lng.str("Trade Order Sent!\nActive trades can also be checked in the Galaxy Map (press '%s')", KeyboardMappings.MAP_PANEL.getKeyChar()), 0);
			deactivate();
		}
	}

	@Override
	public void onDeactivate() {
	}
}
