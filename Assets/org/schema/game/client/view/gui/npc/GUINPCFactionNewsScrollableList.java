package org.schema.game.client.view.gui.npc;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import org.schema.game.client.controller.manager.ingame.shop.ShopControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.faction.newfaction.FactionPanelNew;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.simulation.npc.news.NPCFactionNewsEvent;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;

public class GUINPCFactionNewsScrollableList extends ScrollableTableList<NPCFactionNewsEvent> {

	public GUINPCFactionNewsScrollableList(InputState state, GUIElement p, FactionPanelNew factionPanelNew) {
		super(state, 100, 100, p);
		((GameClientState)getState()).getFactionManager().getNpcFactionNews().obs.addObserver(this);
		
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		super.cleanUp();
		((GameClientState)getState()).getFactionManager().getNpcFactionNews().obs.deleteObserver(this);
	}

	@Override
	protected boolean isFiltered(NPCFactionNewsEvent e) {
		return super.isFiltered(e) ;
	}



	
	public ShopControllerManager getShopControlManager() {
		return ((GameClientState)getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getShopControlManager();
	}
	


	@Override
	public void initColumns() {


		addColumn(Lng.str("Event"), 3f, (o1, o2) -> o1.getMessage((FactionState) getState()).compareTo(o2.getMessage((FactionState) getState())));
		
		addTextFilter(new GUIListFilterText<NPCFactionNewsEvent>() {

			@Override
			public boolean isOk(String input, NPCFactionNewsEvent listElement) {
				return listElement.getMessage((FactionState) getState()).toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH"), FilterRowStyle.FULL);
		
		
		
		activeSortColumnIndex = 0;
	}
	
	@Override
	protected Collection<NPCFactionNewsEvent> getElementList() {
		return ((GameClientState)getState()).getFactionManager().getNpcFactionNews().events;
	}

	
	@Override
	public void updateListEntries(GUIElementList mainList,
	                              Set<NPCFactionNewsEvent> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final PlayerState player = ((GameClientState) getState()).getPlayer();
		int i = 0;
		for (final NPCFactionNewsEvent f : collection) {

			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());

			nameText.setTextSimple(new Object(){
				@Override
				public String toString() {
					return f.getMessage((FactionState)getState());
				}
				
			});
			
			
			
			
			GUIClippedRow nameAnchorP = new GUIClippedRow(getState());
			nameAnchorP.attach(nameText);
			

			
			

			
			
			nameText.getPos().y = 5;

			
			
			
			
			final NPCFactionNewsEventRow r = new NPCFactionNewsEventRow(getState(), f, nameAnchorP);


			
			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}
	


	private class NPCFactionNewsEventRow extends Row {


		public NPCFactionNewsEventRow(InputState state, NPCFactionNewsEvent f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}


		




	}

}
