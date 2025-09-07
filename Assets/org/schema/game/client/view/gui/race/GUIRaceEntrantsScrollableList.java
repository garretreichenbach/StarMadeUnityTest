package org.schema.game.client.view.gui.race;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.activities.Race;
import org.schema.game.common.controller.activities.RaceManager;
import org.schema.game.common.controller.observer.DrawerObservable;
import org.schema.game.common.controller.observer.DrawerObserver;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GUIRaceEntrantsScrollableList extends ScrollableTableList<Race.RaceState> implements DrawerObserver {

	private RaceManager raceManager;
	

	public GUIRaceEntrantsScrollableList(InputState state, GUIElement p) {
		super(state, 100, 100, p);
		raceManager = ((GameClientState) getState()).getRaceManager();
		raceManager.addObserver(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
//		messageController.deleteObserver(this);
		raceManager.deleteObserver(this);
		super.cleanUp();

	}

	@Override
	public void initColumns() {


		addFixedWidthColumnScaledUI(Lng.str("Rank"), 35, (o1, o2) -> (o1.currentRank) - (o1.currentRank));
		addFixedWidthColumnScaledUI(Lng.str("Gate"), 35, (o1, o2) -> (o1.name).compareTo(o2.name));
		addColumn(Lng.str("Name"), 5f, (o1, o2) -> (o1.name).compareTo(o2.name));
		addFixedWidthColumnScaledUI(Lng.str("Status"), 80, (o1, o2) -> Boolean.compare(o1.forefeit, o2.forefeit));
		
		
		addTextFilter(new GUIListFilterText<Race.RaceState>() {

			@Override
			public boolean isOk(String input, Race.RaceState listElement) {
				return listElement.name.toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH BY NAME"), FilterRowStyle.FULL);
		
		activeSortColumnIndex = 0;
		continousSortColumn = 0;
		
	}
	List<Race.RaceState> emptyList = new ObjectArrayList();
	@Override
	protected Collection<Race.RaceState> getElementList() {
		if(raceManager.getSelectedRaceClient() != null){
			return raceManager.getSelectedRaceClient().getEntrants();
		}else{
			return emptyList;
		}
	}

	@Override
	public void updateListEntries(GUIElementList mainList,
	                              Set<Race.RaceState> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);

		final PlayerState player = ((GameClientState) getState()).getPlayer();
		int i = 0;
		for (final Race.RaceState f : collection) {

			GUITextOverlayTable rankText = new GUITextOverlayTable(getState());
			GUITextOverlayTable gateText = new GUITextOverlayTable(getState());
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			GUITextOverlayTable statusText = new GUITextOverlayTable(getState());
			

			rankText.setTextSimple(new Object(){
				@Override
				public String toString() {
					return String.valueOf(f.currentRank);
				}
				});
			gateText.setTextSimple(new Object(){
				@Override
				public String toString() {
					String s = (f.currentGate == -1 ? "-" : String.valueOf(f.currentGate));
					assert(s != null);
					return s;
				}
				
			});
			assert(f.name != null);
			nameText.setTextSimple(f.name);
			statusText.setTextSimple(new Object(){
				@Override
				public String toString() {
					if(f.forefeit){
						return Lng.str("FOREFEIT");
					}
					String s = f.currentGate == -1 ? Lng.str("START") : (f.getFinishedTime() > 0 ? Lng.str("FINISHED") : (Lng.str("RACING")));
					assert(s != null);
					return s;
				}
				
			});

			GUIClippedRow senderAnchorP = new GUIClippedRow(getState());
			senderAnchorP.attach(nameText);

			GUIClippedRow gateAnchorP = new GUIClippedRow(getState());
			gateAnchorP.attach(gateText);

			GUIClippedRow topicAnchorP = new GUIClippedRow(getState());
			topicAnchorP.attach(statusText);

			rankText.getPos().y = 5;
			nameText.getPos().y = 5;
			statusText.getPos().y = 5;
			gateText.getPos().y = 5;

			RaceRow r = new RaceRow(getState(), f, rankText, gateAnchorP, senderAnchorP, topicAnchorP);

			
			GUIAnchor c = new GUIAnchor(getState(), 100, 100);


			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#isFiltered(java.lang.Object)
	 */
	@Override
	protected boolean isFiltered(Race.RaceState e) {
		return super.isFiltered(e);
	}

	@Override
	public void update(DrawerObservable observer, Object userdata,
	                   Object message) {
		flagDirty();
	}

	private class RaceRow extends Row {


		public RaceRow(InputState state, Race.RaceState f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}


	}

}
