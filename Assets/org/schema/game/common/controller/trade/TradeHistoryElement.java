package org.schema.game.common.controller.trade;

public class TradeHistoryElement {
	public long id;
	public long from;
	public long to; 
	public String fromOwner; 
	public String toOwner; 
	public int fromFactionId; 
	public int toFactionId; 
	public long totalCost; 
	public long deliveryCost; 
	public long sent; 
	public long received; 
	public double volume;
	public boolean success;
	
}
