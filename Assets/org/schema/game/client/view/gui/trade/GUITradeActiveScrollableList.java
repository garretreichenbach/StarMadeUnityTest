package org.schema.game.client.view.gui.trade;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.schema.common.util.CompareTools;
import org.schema.common.util.StringTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.ShopInterface;
import org.schema.game.common.controller.trade.TradeActive;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;

public class GUITradeActiveScrollableList extends ScrollableTableList<TradeActive>  {

	public GUITradeActiveScrollableList(InputState state, ShopInterface currentClosestShop, GUIElement p) {
		super(state, 100, 100, p);
		((GameClientState)getState()).getGameState().getTradeManager().getTradeActiveMap().addObserver(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		super.cleanUp();
		((GameClientState)getState()).getGameState().getTradeManager().getTradeActiveMap().deleteObserver(this);
	}

	@Override
	public void initColumns() {


		addColumn(Lng.str("From"), 3f, (o1, o2) -> (o1.getFromStation()).compareTo(o2.getFromStation()));
		addColumn(Lng.str("To"), 3f, (o1, o2) -> o1.getToStation().compareTo(o2.getToStation()));
		addColumn(Lng.str("Current Sector"), 3f, (o1, o2) -> o1.getCurrentSector().compareTo(o2.getCurrentSector()));
		
		addFixedWidthColumnScaledUI(Lng.str("Volume"), 80, (o1, o2) -> CompareTools.compare(o1.getVolume(), o2.getVolume()));
		addFixedWidthColumnScaledUI(Lng.str("Time till Arrival"), 130, (o1, o2) -> 0);

		addTextFilter(new GUIListFilterText<TradeActive>() {

			@Override
			public boolean isOk(String input, TradeActive listElement) {
				return listElement.getFromStation().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH)) ||
						listElement.getToStation().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH BY STATION"), FilterRowStyle.FULL);
		
		activeSortColumnIndex = 4;
	}

	@Override
	protected Collection<TradeActive> getElementList() {
		List<TradeActive> tradeNodes = ((GameClientState)getState()).getGameState().getTradeManager().getTradeActiveMap().getTradeList();
		
		return tradeNodes;
	}

//	@Override
//	public void draw() {
//		super.draw();
//		
//		long t = System.currentTimeMillis();
//		if(t - lastRefresh > 1000){
//			flagDirty();
//			lastRefresh = t;
//		}
//	}

	@Override
	protected boolean isFiltered(TradeActive e) {
		return super.isFiltered(e) || !e.canView(((GameClientState)getState()).getPlayer());
	}

	@Override
	public void updateListEntries(GUIElementList mainList,
	                              Set<TradeActive> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final PlayerState player = ((GameClientState) getState()).getPlayer();
		int i = 0;
		for (final TradeActive f : collection) {

			GUITextOverlayTable fromText = new GUITextOverlayTable(getState());
			GUITextOverlayTable toText = new GUITextOverlayTable(getState());
			GUITextOverlayTable currentSectorText = new GUITextOverlayTable(getState());
			GUITextOverlayTable volumeText = new GUITextOverlayTable(getState());
			GUITextOverlayTable timeText = new GUITextOverlayTable(getState());

			fromText.setTextSimple(new Object(){
				@Override
				public String toString() {
					return f.getFromStation();
				}
				
			});
			toText.setTextSimple(new Object(){
				@Override
				public String toString() {
					return f.getToStation();
				}
				
			});
			currentSectorText.setTextSimple(new Object(){
				@Override
				public String toString() {
					return f.getCurrentSector().toStringPure();
				}
			});
			volumeText.setTextSimple(new Object(){
				@Override
				public String toString() {
					return StringTools.formatPointZero(f.getVolume());
				}
			});
			timeText.setTextSimple(new Object(){
				@Override
				public String toString() {
					return "~"+StringTools.formatTimeFromMS(f.getEstimatedDuration());
				}
			});
			

			GUIClippedRow fromAnchorP = new GUIClippedRow(getState());
			fromAnchorP.attach(fromText);
			
			GUIClippedRow toAnchorP = new GUIClippedRow(getState());
			toAnchorP.attach(toText);

			GUIClippedRow currentSectorAnchorP = new GUIClippedRow(getState());
			currentSectorAnchorP.attach(currentSectorText);
			
			GUIClippedRow volumeAnchorP = new GUIClippedRow(getState());
			volumeAnchorP.attach(volumeText);

			GUIClippedRow timeAnchorP = new GUIClippedRow(getState());
			timeAnchorP.attach(timeText);

			
			
			
			
			fromText.getPos().y = 5;
			toText.getPos().y = 5;
			currentSectorText.getPos().y = 5;
			volumeText.getPos().y = 5;
			timeText.getPos().y = 5;

			TradeActiveRow r = new TradeActiveRow(getState(), f, fromAnchorP, toAnchorP, currentSectorAnchorP, volumeAnchorP, timeAnchorP);

			r.onInit();
			mainList.addWithoutUpdate(r);

			i++;
		}
		mainList.updateDim();
	}
	

	private class TradeActiveRow extends Row {


		public TradeActiveRow(InputState state, TradeActive f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}




	}

}
