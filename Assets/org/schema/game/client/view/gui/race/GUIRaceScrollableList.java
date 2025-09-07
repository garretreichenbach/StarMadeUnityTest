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

public class GUIRaceScrollableList extends ScrollableTableList<Race> implements DrawerObserver {

	private RaceManager raceMan;

	public GUIRaceScrollableList(InputState state, GUIElement p) {
		super(state, 100, 100, p);
		raceMan = ((GameClientState) getState()).getRaceManager();
		raceMan.addObserver(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		raceMan.deleteObserver(this);
		super.cleanUp();
	}

	@Override
	public void initColumns() {


		addColumn(Lng.str("Name"), 5f, (o1, o2) -> (o1.name).compareTo(o2.name));
		addFixedWidthColumnScaledUI(Lng.str("Sector"), 100, (o1, o2) -> o1.startSector.compareTo(o2.startSector));

		addFixedWidthColumnScaledUI(Lng.str("Racers"), 60, (o1, o2) -> o1.getRacerCount() - (o2.getRacerCount()));
		addFixedWidthColumnScaledUI(Lng.str("Buy-In"), 95, (o1, o2) -> o1.getBuyIn() - (o1.getBuyIn()));
		addFixedWidthColumnScaledUI(Lng.str("Started"), 60, (o1, o2) -> Boolean.compare(o1.isStarted(), o2.isStarted()));

		addTextFilter(new GUIListFilterText<Race>() {

			@Override
			public boolean isOk(String input, Race listElement) {
				return listElement.name.toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH BY NAME"), FilterRowStyle.FULL);
		
	}

	@Override
	protected Collection<Race> getElementList() {
		List<Race> activeRaces = raceMan.getActiveRaces();
//		try {
//			throw new Exception(activeRaces.toString());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		return activeRaces;
	}

	@Override
	public void updateListEntries(GUIElementList mainList,
	                              Set<Race> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final PlayerState player = ((GameClientState) getState()).getPlayer();
		int i = 0;
		for (final Race f : collection) {

			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			GUITextOverlayTable sectorText = new GUITextOverlayTable(getState());
			GUITextOverlayTable buyInText = new GUITextOverlayTable(getState());
			GUITextOverlayTable racerCountText = new GUITextOverlayTable(getState());
			GUITextOverlayTable startedText = new GUITextOverlayTable(getState());

			assert(f.name != null);
			assert(f.startSector.toStringPure() != null);
			nameText.setTextSimple(f.name);
			sectorText.setTextSimple(f.startSector.toStringPure());
			racerCountText.setTextSimple(new Object(){
				@Override
				public String toString() {
					String r = String.valueOf(f.getRacerCount());
					assert(r != null);
					return r;
				}
				
			});
			startedText.setTextSimple(new Object() {
				@Override
				public String toString() {
					String r = !f.isStarted() ? Lng.str("OPEN") : Lng.str("STARTED");
					assert(r != null);
					return r;
				}
			});
			buyInText.setTextSimple(f.getBuyIn());

			GUIClippedRow senderAnchorP = new GUIClippedRow(getState());
			senderAnchorP.attach(sectorText);

			GUIClippedRow topicAnchorP = new GUIClippedRow(getState());
			topicAnchorP.attach(racerCountText);

			GUIClippedRow buyInTextP = new GUIClippedRow(getState());
			buyInTextP.attach(buyInText);

			nameText.getPos().y = 5;
			sectorText.getPos().y = 5;
			racerCountText.getPos().y = 5;
			startedText.getPos().y = 5;

			RaceRow r = new RaceRow(getState(), f, nameText, senderAnchorP, topicAnchorP, buyInTextP, startedText);
			
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
	protected boolean isFiltered(Race e) {
		return super.isFiltered(e);
	}

	@Override
	public void update(DrawerObservable observer, Object userdata,
	                   Object message) {
		flagDirty();
	}

	private class RaceRow extends Row {


		public RaceRow(InputState state, Race f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelectSimple = true;
		}

		@Override
		protected void clickedOnRow() {
			super.clickedOnRow();
			if(getSelectedRow() != null){
				raceMan.setSelectedRaceClient(getSelectedRow().f);
			}else{
				raceMan.setSelectedRaceClient(null);
			}
		}

	}

}
