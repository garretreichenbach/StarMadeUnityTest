package org.schema.game.client.view.gui.npc;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.schema.game.client.controller.manager.ingame.shop.ShopControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.faction.newfaction.FactionPanelNew;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.game.server.data.simulation.npc.NPCFaction;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.CreateGUIElementInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterDropdown;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTableDropDown;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GUINPCFactionsScrollableList extends ScrollableTableList<NPCFaction> {

	private FactionPanelNew panel;
	
	public GUINPCFactionsScrollableList(InputState state, GUIElement p, FactionPanelNew factionPanelNew) {
		super(state, 100, 100, p);
		this.panel = factionPanelNew;
		((GameClientState)state).getFactionManager().obs.addObserver(this);
		
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		super.cleanUp();
		((GameClientState)getState()).getFactionManager().obs.deleteObserver(this);
	}

	@Override
	protected boolean isFiltered(NPCFaction e) {
		return super.isFiltered(e) ;
	}



	
	public ShopControllerManager getShopControlManager() {
		return ((GameClientState)getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getShopControlManager();
	}
	
	public RType getOwnRelationTo(NPCFaction f){
		return ((GameClientState)getState()).getFactionManager().getRelation
				(((GameClientState)getState()).getPlayerName(),
						((GameClientState)getState()).getPlayer().getFactionId(), f.getIdFaction());
	}

	@Override
	public void initColumns() {


		addColumn(Lng.str("Name"), 3f, (o1, o2) -> (o1.getName()).compareTo(o2.getName()));
		addFixedWidthColumnScaledUI(Lng.str("Home"), 140, (o1, o2) -> o1.getHomeSector().compareTo(o2.getHomeSector()));
		
		addTextFilter(new GUIListFilterText<NPCFaction>() {

			@Override
			public boolean isOk(String input, NPCFaction listElement) {
				return listElement.getName().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH"), FilterRowStyle.LEFT);
		
		addDropdownFilter(new GUIListFilterDropdown<NPCFaction, Integer>(new Integer[]{0, 1, 2, 3}) {

			@Override
			public boolean isOk(Integer input, NPCFaction f) {
				return switch(input) {
					case 0 -> true;
					case 1 -> getOwnRelationTo(f) == RType.NEUTRAL;
					case 2 -> getOwnRelationTo(f) == RType.ENEMY;
					case 3 -> getOwnRelationTo(f) == RType.FRIEND;
					default -> true;
				};
			}
		}, new CreateGUIElementInterface<Integer>() {
			@Override
			public GUIElement create(Integer o) {
				GUIAnchor c = new GUIAnchor(getState(), 10, UIScale.getUIScale().h);
				GUITextOverlayTableDropDown a = new GUITextOverlayTableDropDown(10, 10, getState());
				switch(o) {
					case 0 -> a.setTextSimple(Lng.str("ALL"));
					case 1 -> a.setTextSimple(Lng.str("NEUTRAL"));
					case 2 -> a.setTextSimple(Lng.str("WAR"));
					case 3 -> a.setTextSimple(Lng.str("ALLIES"));
				}
				
				a.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
				c.setUserPointer(o);
				c.attach(a);

				return c;
			}

			@Override
			public GUIElement createNeutral() {
				return null; // default is all
			}
		}, FilterRowStyle.RIGHT);
		
		activeSortColumnIndex = 0;
	}
	
	@Override
	protected Collection<NPCFaction> getElementList() {
		List<NPCFaction> d = new ObjectArrayList<NPCFaction>();
		for(Faction f : ((GameClientState)getState()).getFactionManager().getFactionCollection()){
			if(f.isNPC()){
				d.add((NPCFaction)f);
			}
		}
		
		return d;
	}

	
	@Override
	public void updateListEntries(GUIElementList mainList,
	                              Set<NPCFaction> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final PlayerState player = ((GameClientState) getState()).getPlayer();
		int i = 0;
		for (final NPCFaction f : collection) {

			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			GUITextOverlayTable systemText = new GUITextOverlayTable(getState());

			nameText.setTextSimple(new Object(){
				@Override
				public String toString() {
					return f.getName();
				}
				
			});
			systemText.setTextSimple(new Object(){
				@Override
				public String toString() {
					return f.getHomeSector().toStringPure();
				}
			});
			
			
			
			
			GUIClippedRow nameAnchorP = new GUIClippedRow(getState());
			nameAnchorP.attach(nameText);
			
			GUIClippedRow sysAnchorP = new GUIClippedRow(getState());
			
			sysAnchorP.attach(systemText);

			
			

			
			
			nameText.getPos().y = UIScale.getUIScale().scale(5);
			systemText.getPos().y = UIScale.getUIScale().scale(5);

			
			
			
			
			final NPCFactionRow r = new NPCFactionRow(getState(), f, nameAnchorP, sysAnchorP);


			
			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}
	


	private class NPCFactionRow extends Row {


		public NPCFactionRow(InputState state, NPCFaction f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
			this.highlightSelectSimple = true;
			this.setAllwaysOneSelected(true);
		}

		@Override
		protected void clickedOnRow() {
			panel.onSelectFaction(f);
			super.clickedOnRow();
		}

		




	}

}
