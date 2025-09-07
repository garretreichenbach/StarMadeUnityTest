package org.schema.game.client.view.gui.reactor;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorSet;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.server.data.FactionState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyboardMappings;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GUIReactorFunctionalityList extends ScrollableTableList<PlayerUsableInterface> implements ReactorTreeListener {

	private ManagerContainer<?> c;
	public GUIReactorFunctionalityList(InputState state, ManagerContainer<?> c, GUIElement p) {
		super(state, 100, 100, p);
		this.c = c;
		c.getPowerInterface().addObserver(this);
	}
	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	
	@Override
	public void onTreeChanged(ReactorSet t) {
		onChange(false);
	}
	
	@Override
	public void cleanUp() {
		c.getPowerInterface().deleteObserver(this);
		super.cleanUp();
	}

	@Override
	public void initColumns() {


		addColumn(Lng.str("Name"), 3, (o1, o2) -> o1.getName().compareTo(o2.getName()), true);
		addColumn(Lng.str("Status"), 2, (o1, o2) -> {
			String a = Lng.str("STANDBY");
			String b = Lng.str("STANDBY");
			if(o1.getReloadInterface() != null){
				a = o1.getReloadInterface().getReloadStatus(o1.getUsableId());
			}
			if(o2.getReloadInterface() != null){
				b = o2.getReloadInterface().getReloadStatus(o2.getUsableId());
			}

			return o1.getName().toLowerCase(Locale.ENGLISH).compareTo(o2.getName().toLowerCase(Locale.ENGLISH));
		});

		
		
		addFixedWidthColumnScaledUI(Lng.str("Options"), 128, (o1, o2) -> 0);
		
		
		addTextFilter(new GUIListFilterText<PlayerUsableInterface>() {

			@Override
			public boolean isOk(String input, PlayerUsableInterface listElement) {
				return listElement.getName().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, FilterRowStyle.FULL);
		
		
		activeSortColumnIndex = 0;
		continousSortColumn = 0;
	}


	@Override
	protected Collection<PlayerUsableInterface> getElementList() {
		List<PlayerUsableInterface> p = new ObjectArrayList<PlayerUsableInterface>();
		for(PlayerUsableInterface pu : c.getPlayerUsable()){
			if(pu.isPlayerUsable() && pu.getUsableId() < PlayerUsableInterface.MIN_USABLE && pu.getUsableId() != PlayerUsableInterface.USABLE_ID_THRUSTER){
				p.add(pu);
			}
		}
		assert(p != null);
		return p;
	}

	@Override
	public void updateListEntries(GUIElementList mainList,
	                              Set<PlayerUsableInterface> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);

		final FactionManager m = ((FactionState)getState()).getFactionManager();
		final PlayerState player = ((GameClientState) getState()).getPlayer();
		int i = 0;
		
		for (final PlayerUsableInterface f : collection) {
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			GUITextOverlayTable statusText = new GUITextOverlayTable(getState());

			
			GUIClippedRow nameAnchorP = new GUIClippedRow(getState());
			nameAnchorP.attach(nameText);
			nameText.setTextSimple(f.getName());
			
			
			GUIClippedRow statusAnchorP = new GUIClippedRow(getState());
			statusAnchorP.attach(statusText);
			statusText.setTextSimple(new Object() {
				@Override
				public String toString() {
					if(f.getReloadInterface() != null){
						return f.getReloadInterface().getReloadStatus(f.getUsableId());
					}else{
						return Lng.str("STANDBY");
					}
				}
			});
			
			GUITextButton upButton = new GUITextButton(getState(), 120, this.getDefaultColumnsHeight(), Lng.str("TRIGGER"), new GUICallback() {
				@Override
				public boolean isOccluded() {
					return !GUIReactorFunctionalityList.this.isActive();
				}
				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					for(KeyboardMappings m : event.getTriggeredMappings()) {
						c.triggerEventFromMouseCallback(((GameClientState)getState()).getPlayer().getId(), f, m);
					}
				}
				
			});
			
			GUIAnchor opt = new GUIAnchor(getState(), 50, this.getDefaultColumnsHeight());
			opt.attach(upButton);
			nameText.getPos().y = 4;
			statusText.getPos().y = 4;

			PlayerUsableInterfaceRow r = new PlayerUsableInterfaceRow(getState(), f, nameAnchorP, statusAnchorP, opt);
			r.expanded = null;
			

			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}
	

	private class PlayerUsableInterfaceRow extends Row {

		public PlayerUsableInterfaceRow(InputState state, PlayerUsableInterface f, GUIElement... elements) {
			super(state, f, elements);
		}
	}




}
