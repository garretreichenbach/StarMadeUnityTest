package api.listener.events.player;

import api.listener.events.Event;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.trade.TradeActive;
import org.schema.game.common.controller.trade.TradeNodeStub;
import org.schema.game.common.controller.trade.TradeOrder;

import java.util.List;

public class BuyTradeEvent extends Event {

    private List<TradeOrder.TradeOrderElement> items;
    private int deliveryCost;
    private int totalCost;
    private TradeOrder order;
    private Vector3i from;
    private Vector3i to;
    private TradeNodeStub buyer;
    private TradeNodeStub seller;
    private TradeActive tradeActive;

    public BuyTradeEvent(TradeOrder order, TradeNodeStub buyer, TradeNodeStub seller, TradeActive tradeActive) {
        this.order = order;
        this.deliveryCost = (int) order.getDeliveryCost();
        this.totalCost = (int) order.getTotalPrice();
        this.from = seller.getSector();
        this.to = buyer.getSector();
        this.items = order.getElements();
        this.tradeActive = tradeActive;
        this.buyer = buyer;
        this.seller = seller;
    }

    public Vector3i getFrom() {
        return from;
    }

    public Vector3i getTo() {
        return to;
    }

    public int getDeliveryCost() {
        return deliveryCost;
    }

    public int getTotalCost() {
        return totalCost;
    }

    public List<TradeOrder.TradeOrderElement> getItems() {
        return items;
    }

    public TradeNodeStub getBuyer() {
        return buyer;
    }

    public TradeNodeStub getSeller() {
        return seller;
    }

    public TradeOrder getOrder() {
        return order;
    }

    @Override
    public void setCanceled(boolean canceled) {
        tradeActive.cancelTrade = canceled;
    }
}