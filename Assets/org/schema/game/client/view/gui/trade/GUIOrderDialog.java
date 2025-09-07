package org.schema.game.client.view.gui.trade;

import javax.vecmath.Vector4f;

import org.schema.common.util.StringTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.common.controller.ShopInterface;
import org.schema.game.common.controller.elements.shop.ShopElementManager;
import org.schema.game.common.controller.trade.TradeNodeClient;
import org.schema.game.common.controller.trade.TradeOrder;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIChangeListener;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangle;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIEnterableList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITabbedContent;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.graphicsengine.forms.gui.newgui.config.ListColorPalette;
import org.schema.schine.input.InputState;

public class GUIOrderDialog extends GUIInputPanel implements GUIChangeListener{

	private boolean init;
	private final TradeOrder tradeOrder;
	private final TradeNodeClient tradeNode;
	final int rowHeight = 24;
	private GUIScrollablePanel p;
	private GUIElementList mainList;
	private Vector4f red = new Vector4f(1f,0.2f,0.2f,1.0f);
	private Vector4f green = new Vector4f(0.2f,1.0f,0.2f,1.0f);
	private Vector4f white = new Vector4f(0.9f,0.9f,0.9f,1.0f);
	private OrderDialog dialog;
	
	
	
	
	@Override
	public boolean isActive() {
		return super.isActive() && dialog.isActive();
	}

	public GUIOrderDialog(InputState state, OrderDialog dialog, TradeNodeClient f, ShopInterface currentClosestShop, OrderDialog orderDialog, TradeOrder tradeOrder) throws ShopNotFoundException {
		super("ORDER_PNL", state, UIScale.getUIScale().scale(800), UIScale.getUIScale().scale(600), orderDialog, Lng.str("PLACE ORDER"), "");
		this.tradeOrder = tradeOrder;
		this.tradeNode = f;
		this.dialog = dialog;
		onInit();
		
		GUIDialogWindow w = (GUIDialogWindow)getBackground();
		w.getMainContentPane().setTextBoxHeightLast(UIScale.getUIScale().scale(148));
		
		createTradeStats(w.getMainContentPane().getContent(0));
		
		w.getMainContentPane().addNewTextBox(UIScale.getUIScale().scale(140));
		
		
		
		GUITradeOrderScrollableList orderList = new GUITradeOrderScrollableList(getState(), currentClosestShop, tradeOrder, tradeNode, w.getMainContentPane().getContent(1));
		orderList.onInit();
		w.getMainContentPane().getContent(1).attach(orderList);
		
		w.getMainContentPane().addNewTextBox(UIScale.getUIScale().scale(150));
		
		
		GUITabbedContent buySellTabs = new GUITabbedContent(getState(), w.getMainContentPane().getContent(2));
		buySellTabs.onInit();
		GUIContentPane buyTab = buySellTabs.addTab(Lng.str("BUY"));
		GUIContentPane sellTab = buySellTabs.addTab(Lng.str("SELL"));
		
		buySellTabs.activationInterface = dialog;
		
		buyTab.setTextBoxHeightLast(UIScale.getUIScale().scale(10));
		sellTab.setTextBoxHeightLast(UIScale.getUIScale().scale(10));
		
		GUIPricesOfferScrollableList pricesBuy = new GUIPricesOfferScrollableList(getState(), 
				buyTab.getContent(0), tradeOrder, tradeNode, true);
		pricesBuy.onInit();
		buyTab.getContent(0).attach(pricesBuy);
		
		GUIPricesOfferScrollableList pricesSell = new GUIPricesOfferScrollableList(getState(), 
				sellTab.getContent(0), tradeOrder, tradeNode, false);
		pricesSell.onInit();
		sellTab.getContent(0).attach(pricesSell);
		
		w.getMainContentPane().getContent(2).attach(buySellTabs);
	}

	private void createTradeStats(GUIAnchor c) {
		p = new GUIScrollablePanel(10, 10, c, getState());
		mainList = new GUIElementList(getState());
		p.setContent(mainList);
		mainList.setScrollPane(p);
		int i = 0;
		addRow(Lng.str("Raw Block Cost / Profit:"), new Object(){
			@Override
			public String toString() {
				long p = (tradeOrder.getBuyPrice() - tradeOrder.getSellPrice());
				return StringTools.formatSeperated(tradeOrder.getBuyPrice())+" / "+StringTools.formatSeperated(tradeOrder.getSellPrice()) + 
						Lng.str(" (Total: %s)", StringTools.formatSeperated(Math.abs(p)))+(p < 0 ? Lng.str(" (Profit[+])") : Lng.str(" (Cost[-])"));
			}
			
		}, c, () -> {
			long p =(tradeOrder.getBuyPrice() - tradeOrder.getSellPrice());
			return p == 0 ? white : ( p < 0 ? green : red);
		},mainList, (i++) % 2 == 0);
		
		
		
		GUIElementList subList = new GUIElementList(getState());
		
		int j = 0;
		addRow(Lng.str("     Trading Ships:"), new Object(){
			@Override
			public String toString() {
				return StringTools.formatSeperated(tradeOrder.getUsedTradeShips())+ 
						Lng.str(" (Cost per ship: %s, Total: %s)", StringTools.formatSeperated(ShopElementManager.TRADING_GUILD_COST_PER_CARGO_SHIP), StringTools.formatSeperated(tradeOrder.getTradeShipCost()));
			}
			
		}, c, null, subList, (j++) % 2 == 0);
		addRow(Lng.str("     System Distance:"), new Object(){
			@Override
			public String toString() {
				return StringTools.formatSeperated(tradeOrder.getTradeDistance())+ Lng.str(" (Per System: %s, Total: %s)", StringTools.formatSeperated(ShopElementManager.TRADING_GUILD_COST_PER_SYSTEM), StringTools.formatSeperated(tradeOrder.getPricePerDistance()));
			}
			
		}, c, null, subList, (j++) % 2 == 0);
		addRow(Lng.str("     Trading Guild value Share:"), new Object(){
			@Override
			public String toString() {
				return StringTools.formatSeperated(tradeOrder.getTradeingGuildProfit())+ Lng.str(" (Share: %s%%)", StringTools.formatPointZero(ShopElementManager.TRADING_GUILD_PROFIT_OF_VALUE*100));
			}
			
		}, c, null,subList, (j++) % 2 == 0);
		addRow(Lng.str("     Trading Guild distance Share:"), new Object(){
			@Override
			public String toString() {
				return StringTools.formatSeperated(tradeOrder.getTradeingGuildProfitPerSystem())+ Lng.str(" (Share: %s%% per system)", StringTools.formatPointZero(ShopElementManager.TRADING_GUILD_PROFIT_OF_VALUE_PER_SYSTEM*100));
			}
			
		}, c, null,subList, (j++) % 2 == 0);
		
		addRow(mainList, Lng.str("Delivery Cost:"), new Object(){
			@Override
			public String toString() {
				return StringTools.formatSeperated(tradeOrder.getTradingGuildPrice());
			}
			
		}, c, null,(i++) % 2 == 0, subList);
		
		
		
		
		addRow(Lng.str("Total Cost/Profit:"), new Object(){
			@Override
			public String toString() {
				long p = tradeOrder.getTotalPrice();
				return StringTools.formatSeperated(Math.abs(p))+(p < 0 ? Lng.str(" (Profit[+])") : Lng.str(" (Cost[-])"));
			}
			
		}, c, () -> {
			long p = tradeOrder.getTotalPrice();
			return p == 0 ? white : ( p < 0 ? green : red);
		},  mainList, (i++) % 2 == 0);
		
		
		mainList.onInit();
		mainList.updateDim();
		c.attach(p);
	}
	private void addRow(GUIElementList mainList, String name, Object val, final GUIAnchor c, final ColorInterface colorInt, boolean even, GUIElementList subList){
		Vector4f color =  even ? ListColorPalette.buyListBackgroundColor : ListColorPalette.buyListBackgroundColorAlternate;
		GUIColoredRectangle bg = new GUIColoredRectangle(getState(), 10, rowHeight,color){

			@Override
			public void draw() {
				setWidth((int)c.getWidth());
				setHeight(rowHeight);
				super.draw();
			}
			
		};
		bg.onInit();
		bg.setName("BG");
		GUITextOverlayTable nameOv = new GUITextOverlayTable(getState()){

			@Override
			public void draw() {
				if(colorInt != null){
					setColor(colorInt.getColor());
				}
				super.draw();
			}
			
		};;
		nameOv.setTextSimple("[+] "+name);
		nameOv.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
		
		bg.attach(nameOv);
		
		GUITextOverlayTable valOv = new GUITextOverlayTable(getState()){

			@Override
			public void draw() {
				if(colorInt != null){
					setColor(colorInt.getColor());
				}
				super.draw();
			}
			
		};;
		valOv.setTextSimple(val);
		valOv.setPos(UIScale.getUIScale().scale(4 + 200), UIScale.getUIScale().scale(4), 0);
		bg.attach(valOv);
		
		GUIColoredRectangle bgExt = new GUIColoredRectangle(getState(), 10, rowHeight, color){
			
			@Override
			public void draw() {
				setWidth((int)c.getWidth());
				setHeight(rowHeight);
				super.draw();
			}
			
		};
		bgExt.onInit();
		GUITextOverlayTable nameOvExt = new GUITextOverlayTable(getState()){

			@Override
			public void draw() {
				if(colorInt != null){
					setColor(colorInt.getColor());
				}
				super.draw();
			}
			
		};;
		nameOvExt.setTextSimple("[-] "+name);
		nameOvExt.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
		bgExt.setName("BGEXT");
		bgExt.attach(nameOv);
		
		GUITextOverlayTable valOvExt = new GUITextOverlayTable(getState()){

			@Override
			public void draw() {
				if(colorInt != null){
					setColor(colorInt.getColor());
				}
				super.draw();
			}
			
		};;
		valOvExt.setTextSimple(val);
		valOvExt.setPos(UIScale.getUIScale().scale(4 + 200), UIScale.getUIScale().scale(4), 0);
		bgExt.attach(valOv);
		
		
		GUIEnterableList le = new GUIEnterableList(getState(), subList, bg, bgExt);
		le.extendedHighlightBottomDistSet(4);
		le.scrollPanel = p;
		assert(le.isCollapsed());
		le.expandedBackgroundColor = color;
//		le.extendedCallback = onExpanded;
//		le.extendedCallbackSelector = this;
//		le.extendableBlockedInterface = extendableBlockedInterface;
		le.addObserver(this);
		assert(le.isCollapsed());
//		this.content = le;
//		this.selectContent = le;
		le.onInit();
		
		assert(le.isCollapsed());
		GUIListElement leA = new GUIListElement(le, getState());
		mainList.add(leA);
	}
	
	private void addRow(Object name, Object val, final GUIAnchor c, final ColorInterface colorInt, GUIElementList l, boolean even){
		
		GUIColoredRectangle bg = new GUIColoredRectangle(getState(), 10, rowHeight, even ? ListColorPalette.buyListBackgroundColor : ListColorPalette.buyListBackgroundColorAlternate){

			@Override
			public void draw() {
				setWidth((int)c.getWidth());
				setHeight(rowHeight);
				super.draw();
			}
			
		};
		bg.onInit();
		
		GUITextOverlayTable nameOv = new GUITextOverlayTable(getState()){

			@Override
			public void draw() {
				if(colorInt != null){
					setColor(colorInt.getColor());
				}
				super.draw();
			}
			
		};
		nameOv.setTextSimple(name);
		nameOv.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
		
		bg.attach(nameOv);
		
		GUITextOverlayTable valOv = new GUITextOverlayTable(getState()){

			@Override
			public void draw() {
				if(colorInt != null){
					setColor(colorInt.getColor());
				}
				super.draw();
			}
			
		};
		valOv.setTextSimple(val);
		valOv.setPos(4 + 200, 4, 0);
		
		
		bg.attach(valOv);
		
		GUIListElement le = new GUIListElement(bg, getState());
		
		l.add(le);
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
		
		TradeNodeClient n = (TradeNodeClient)((GameClientState)getState())
				.getController().getClientChannel().getGalaxyManagerClient()
				.getTradeNodeDataById().get(tradeNode.getEntityDBId());
		
		if(n != tradeNode){
			
				dialog.queueDeactivate = Lng.str("Trade Node removed from Trade Network!");
		}
		
		super.draw();
	}

	@Override
	public void onChange(boolean updateListDim) {
		if(updateListDim) {
			mainList.updateDim();
		}
	}
	
	
	

}
