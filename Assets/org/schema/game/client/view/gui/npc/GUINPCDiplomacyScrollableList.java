package org.schema.game.client.view.gui.npc;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.schema.game.client.controller.manager.ingame.shop.ShopControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.simulation.npc.NPCFaction;
import org.schema.game.server.data.simulation.npc.diplomacy.NPCDiplModifier;
import org.schema.game.server.data.simulation.npc.diplomacy.NPCDiplomacyEntity;
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

public class GUINPCDiplomacyScrollableList extends ScrollableTableList<NPCDiplModifier> {

	private long diplEntityId;
	private NPCFaction toFaction;
	
	public GUINPCDiplomacyScrollableList(InputState state, long diplEntityId, NPCFaction toFaction, GUIElement p) {
		super(state, 100, 100, p);
		this.toFaction = toFaction;
		this.diplEntityId = diplEntityId;
		((GameClientState)state).getFactionManager().obs.addObserver(this);
		toFaction.getDiplomacy().addObserver(this);
		
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		super.cleanUp();
		((GameClientState)getState()).getFactionManager().obs.deleteObserver(this);
		toFaction.getDiplomacy().deleteObserver(this);
	}

	@Override
	protected boolean isFiltered(NPCDiplModifier e) {
		return super.isFiltered(e) ;
	}



	
	public ShopControllerManager getShopControlManager() {
		return ((GameClientState)getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getShopControlManager();
	}
	


	@Override
	public void initColumns() {


		addColumn(Lng.str("Modifier"), 3f, (o1, o2) -> (o1.getName()).compareTo(o2.getName()));
		addFixedWidthColumnScaledUI(Lng.str("Value"), 140, (o1, o2) -> o1.getValue() - o2.getValue());
		
		addTextFilter(new GUIListFilterText<NPCDiplModifier>() {

			@Override
			public boolean isOk(String input, NPCDiplModifier listElement) {
				return listElement.getName().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH"), FilterRowStyle.LEFT);
		
		addDropdownFilter(new GUIListFilterDropdown<NPCDiplModifier, Integer>(new Integer[]{0, 1, 2}) {

			@Override
			public boolean isOk(Integer input, NPCDiplModifier f) {
				return switch(input) {
					case 0 -> true;
					case 1 -> !f.isStatic();
					case 2 -> f.isStatic();
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
					case 1 -> a.setTextSimple(Lng.str("TURN MODIFIERS"));
					case 2 -> a.setTextSimple(Lng.str("STATIC MODIFIERS"));
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
	protected Collection<NPCDiplModifier> getElementList() {
		
		List<NPCDiplModifier> d = new ObjectArrayList<NPCDiplModifier>();
		
		NPCDiplomacyEntity ent = toFaction.getDiplomacy().entities.get(diplEntityId);
		if(ent != null){
			
			d.addAll(ent.getDynamicMap().values());
			d.addAll(ent.getStaticMap().values());
		}
		
		return d;
	}

	
	@Override
	public void updateListEntries(GUIElementList mainList,
	                              Set<NPCDiplModifier> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final PlayerState player = ((GameClientState) getState()).getPlayer();
		int i = 0;
		for (final NPCDiplModifier f : collection) {

			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			GUITextOverlayTable valueText = new GUITextOverlayTable(getState());

			nameText.setTextSimple(new Object(){
				@Override
				public String toString() {
					return f.getName();
				}
				
			});
			valueText.setTextSimple(new Object(){
				@Override
				public String toString() {
					return f.getValue()+(f.isStatic() ? "" : " / turn");
				}
			});
			
			
			
			
			GUIClippedRow nameAnchorP = new GUIClippedRow(getState());
			nameAnchorP.attach(nameText);
			
			GUIClippedRow sysAnchorP = new GUIClippedRow(getState());
			
			sysAnchorP.attach(valueText);

			
			

			
			
			nameText.getPos().y = UIScale.getUIScale().scale(5);
			valueText.getPos().y = UIScale.getUIScale().scale(5);

			
			
			
			
			final NPCDiplModifierRow r = new NPCDiplModifierRow(getState(), f, nameAnchorP, sysAnchorP);


			
			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}
	


	private class NPCDiplModifierRow extends Row {


		public NPCDiplModifierRow(InputState state, NPCDiplModifier f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}




	}

}
