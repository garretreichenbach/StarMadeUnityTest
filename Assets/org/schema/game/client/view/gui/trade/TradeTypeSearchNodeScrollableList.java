package org.schema.game.client.view.gui.trade;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import org.schema.common.util.CompareTools;
import org.schema.common.util.StringTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.ShopInterface;
import org.schema.game.common.controller.trade.TradeTypeRequestAwnser;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.FactionState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIChangeListener;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class TradeTypeSearchNodeScrollableList extends ScrollableTableList<TradeTypeRequestAwnser> implements GUIChangeListener {

	private ShopInterface currentClosestShop;

	public TradeTypeSearchNodeScrollableList(InputState state, ShopInterface currentClosestShop, GUIElement p) {
		super(state, 100, 100, p);
		this.currentClosestShop = currentClosestShop;
		((GameClientState) getState()).getController().getClientChannel().getReceivedTradeSearchResults().addObserver(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		super.cleanUp();
		((GameClientState) getState()).getController().getClientChannel().getReceivedTradeSearchResults().deleteObserver(this);
	}

	@Override
	protected boolean isFiltered(TradeTypeRequestAwnser e) {
		return super.isFiltered(e) || e.nodeClient == null || e.nodeClient.getEntityDBId() == currentClosestShop.getSegmentController().getDbId();
	}

	@Override
	public void initColumns() {
		addColumn(Lng.str("System"), 3f, (o1, o2) -> (o1.nodeClient.getSystem()).compareTo(o2.nodeClient.getSystem()));
		addColumn(Lng.str("Name"), 3f, (o1, o2) -> o1.nodeClient.getStationName().compareTo(o2.nodeClient.getStationName()));
		addColumn(Lng.str("Faction"), 3f, (o1, o2) -> {
			String a = ((FactionState) getState()).getFactionManager().getFactionName(o1.nodeClient.getFactionId());
			String b = ((FactionState) getState()).getFactionManager().getFactionName(o2.nodeClient.getFactionId());
			return a.compareTo(b);
		});
		// addColumn(Lng.str("Owner"), 3f, new Comparator<TradeTypeRequestAwnser>() {
		// @Override
		// public int compare(TradeTypeRequestAwnser o1, TradeTypeRequestAwnser o2) {
		// String a = o1.nodeClient.getOwners().isEmpty() ? Lng.str("Nobody") : o1.nodeClient.getOwnerString();
		// String b = o2.nodeClient.getOwners().isEmpty() ? Lng.str("Nobody") : o2.nodeClient.getOwnerString();
		// return a.compareTo(b);
		// }
		// });
		addFixedWidthColumnScaledUI(Lng.str("Sells for"), 85, (o1, o2) -> CompareTools.compare(o1.buyPrice, o2.buyPrice));
		addFixedWidthColumnScaledUI(Lng.str("Available"), 90, (o1, o2) -> CompareTools.compare(o1.availableBuy, o2.availableBuy));
		addFixedWidthColumnScaledUI(Lng.str("Buys for"), 85, (o1, o2) -> CompareTools.compare(o1.sellPrice, o2.sellPrice));
		addFixedWidthColumnScaledUI(Lng.str("Buys how many"), 110, (o1, o2) -> CompareTools.compare(o1.availableSell, o2.availableSell));
		addFixedWidthColumnScaledUI(Lng.str("Order"), 75, (o1, o2) -> 0);
		addTextFilter(new GUIListFilterText<TradeTypeRequestAwnser>() {

			@Override
			public boolean isOk(String input, TradeTypeRequestAwnser listElement) {
				return listElement.nodeClient.getStationName().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH BY NAME"), FilterRowStyle.LEFT);
		addTextFilter(new GUIListFilterText<TradeTypeRequestAwnser>() {

			@Override
			public boolean isOk(String input, TradeTypeRequestAwnser listElement) {
				String a = ((FactionState) getState()).getFactionManager().getFactionName(listElement.nodeClient.getFactionId());
				return a.toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH BY FACTION"), FilterRowStyle.RIGHT);
		activeSortColumnIndex = 1;
	}

	@Override
	protected Collection<TradeTypeRequestAwnser> getElementList() {
		ObjectArrayList<TradeTypeRequestAwnser> tradeNodes = ((GameClientState) getState()).getController().getClientChannel().getReceivedTradeSearchResults().o;
		return tradeNodes;
	}

	// @Override
	// public void draw() {
	// super.draw();
	// 
	// long t = System.currentTimeMillis();
	// if(t - lastRefresh > 1000){
	// flagDirty();
	// lastRefresh = t;
	// }
	// }
	@Override
	public void updateListEntries(GUIElementList mainList, Set<TradeTypeRequestAwnser> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final PlayerState player = ((GameClientState) getState()).getPlayer();
		int i = 0;
		for (final TradeTypeRequestAwnser f : collection) {
			GUITextOverlayTable systemText = new GUITextOverlayTable(getState());
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			GUITextOverlayTable factionText = new GUITextOverlayTable(getState());
			GUITextOverlayTable ownerText = new GUITextOverlayTable(getState());
			GUITextOverlayTable buy = new GUITextOverlayTable(getState());
			GUITextOverlayTable available = new GUITextOverlayTable(getState());
			GUITextOverlayTable buysFor = new GUITextOverlayTable(getState());
			GUITextOverlayTable buysHowMany = new GUITextOverlayTable(getState());
			systemText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f.nodeClient.getSystem().toStringPure() + " sec[" + f.nodeClient.getSector().toStringPure() + "]";
				}
			});
			nameText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f.nodeClient.getStationName();
				}
			});
			factionText.setTextSimple(new Object() {

				@Override
				public String toString() {
					String a = ((FactionState) getState()).getFactionManager().getFactionName(f.nodeClient.getFactionId());
					return a;
				}
			});
			ownerText.setTextSimple(new Object() {

				@Override
				public String toString() {
					String a = f.nodeClient.getOwnerString();
					return a;
				}
			});
			buy.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f.buyPrice <= 0 ? Lng.str("N/A") : StringTools.formatSeperated(f.buyPrice);
				}
			});
			available.setTextSimple(new Object() {

				@Override
				public String toString() {
					return (f.buyPrice <= 0 || f.availableBuy <= 0) ? Lng.str("N/A") : StringTools.formatSeperated(f.availableBuy);
				}
			});
			buysFor.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f.sellPrice <= 0 ? Lng.str("N/A") : StringTools.formatSeperated(f.sellPrice);
				}
			});
			buysHowMany.setTextSimple(new Object() {

				@Override
				public String toString() {
					return (f.sellPrice <= 0 || f.availableSell <= 0) ? Lng.str("N/A") : StringTools.formatSeperated(f.availableSell);
				}
			});
			GUIClippedRow sysAnchorP = new GUIClippedRow(getState());
			sysAnchorP.attach(systemText);
			GUIClippedRow nameAnchorP = new GUIClippedRow(getState());
			nameAnchorP.attach(nameText);
			GUIClippedRow factionAnchorP = new GUIClippedRow(getState());
			factionAnchorP.attach(factionText);
			GUIClippedRow ownerAnchorP = new GUIClippedRow(getState());
			ownerAnchorP.attach(ownerText);
			GUIClippedRow buyAnchorP = new GUIClippedRow(getState());
			buyAnchorP.attach(buy);
			GUIClippedRow availableAnchorP = new GUIClippedRow(getState());
			availableAnchorP.attach(available);
			GUIClippedRow buysForAnchorP = new GUIClippedRow(getState());
			buysForAnchorP.attach(buysFor);
			GUIClippedRow buysHowManyAnchorP = new GUIClippedRow(getState());
			buysHowManyAnchorP.attach(buysHowMany);
			GUITextButton b = new GUITextButton(getState(), 50, this.getDefaultColumnsHeight(), Lng.str("Order"), new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !TradeTypeSearchNodeScrollableList.this.isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						openOrderDialog(f);
					}
				}
			});
			GUIClippedRow orderAnchorP = new GUIClippedRow(getState());
			orderAnchorP.attach(b);
			systemText.getPos().y = 5;
			nameText.getPos().y = 5;
			factionText.getPos().y = 5;
			ownerText.getPos().y = 5;
			buy.getPos().y = 5;
			available.getPos().y = 5;
			buysFor.getPos().y = 5;
			buysHowMany.getPos().y = 5;
			TradeTypeRequestAwnserRow r = new TradeTypeRequestAwnserRow(getState(), f, sysAnchorP, nameAnchorP, factionAnchorP, buyAnchorP, availableAnchorP, buysForAnchorP, buysHowManyAnchorP, orderAnchorP);
			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}

	private void openOrderDialog(TradeTypeRequestAwnser f) {
		OrderDialog d;
		try {
			d = new OrderDialog((GameClientState) getState(), currentClosestShop, f.nodeClient);
			d.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(718);
		} catch (ShopNotFoundException e) {
			e.printStackTrace();
			((GameClientState) getState()).getController().popupAlertTextMessage(e.getMessage(), 0);
		}
	}

	private class TradeTypeRequestAwnserRow extends Row {

		public TradeTypeRequestAwnserRow(InputState state, TradeTypeRequestAwnser f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}
	}
}
