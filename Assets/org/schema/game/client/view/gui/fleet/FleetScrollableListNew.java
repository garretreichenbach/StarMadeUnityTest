package org.schema.game.client.view.gui.fleet;

import api.listener.events.systems.GetAvailableFleetsEvent;
import api.mod.StarLoader;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetManager;
import org.schema.game.common.data.fleet.FleetStateInterface;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

public class FleetScrollableListNew extends ScrollableTableList<Fleet>  {

	public FleetScrollableListNew(InputState state, GUIElement p) {
		super(state, 100, 100, p);
		((GameClientState) getState()).getFleetManager().obs.addObserver(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		((GameClientState) getState()).getFleetManager().obs.deleteObserver(this);
		super.cleanUp();

	}

	@Override
	public void initColumns() {


		addColumn(Lng.str("Name"), 3, (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()), true);

		addFixedWidthColumnScaledUI(Lng.str("Ships"), 39, (o1, o2) -> o1.getMembers().size() - o2.getMembers().size());
		addColumn(Lng.str("Flagship"), 3, (o1, o2) -> o1.getFlagShipName().compareToIgnoreCase(o2.getFlagShipName()));
		
		addColumn(Lng.str("Sector"), 1, (o1, o2) -> o1.getFlagShipSector().compareToIgnoreCase(o2.getFlagShipSector()));
		
		addColumn(Lng.str("Mission"), 1.5f, (o1, o2) -> o1.getMissionName().compareToIgnoreCase(o2.getMissionName()));

		
		addTextFilter(new GUIListFilterText<Fleet>() {

			@Override
			public boolean isOk(String input, Fleet listElement) {
				return listElement.getName().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, FilterRowStyle.FULL);

	}

	@Override
	protected Collection<Fleet> getElementList() {
		final FleetManager fleetMan = ((GameClientState) getState()).getFleetManager();
		Collection<Fleet> availableFleetsClient = fleetMan.getAvailableFleetsClient();
		//INSERTED CODE @97
		GetAvailableFleetsEvent ev = new GetAvailableFleetsEvent(this, fleetMan, availableFleetsClient);
		StarLoader.fireEvent(ev, false);
		///
		return availableFleetsClient;
	}

	@Override
	public void updateListEntries(GUIElementList mainList,
	                              Set<Fleet> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final FleetManager factionManager = ((GameClientState) getState()).getFleetManager();
		final PlayerState player = ((GameClientState) getState()).getPlayer();
		int i = 0;
		for (final Fleet f : collection) {
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			GUITextOverlayTable sizeText = new GUITextOverlayTable(getState());
			GUITextOverlayTable flagshipText = new GUITextOverlayTable(getState()) {
				/* (non-Javadoc)
				 * @see org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable#draw()
				 */
				@Override
				public void draw() {
//					RType relation = factionManager.getRelation(player.getName(), player.getFleetId(), f.getId());
//					setColor(org.schema.game.client.view.gui.shiphud.newhud.ColorPalette.getColorDefault(relation, f.getId() == player.getFleetId()));
					super.draw();
				}

			};
			GUITextOverlayTable sectorText = new GUITextOverlayTable(getState());
			GUITextOverlayTable missionText = new GUITextOverlayTable(getState());

			GUIClippedRow nameAnchorP = new GUIClippedRow(getState());
			nameAnchorP.attach(nameText);
			nameText.setTextSimple(new Object() {
				@Override
				public String toString() {
					return f.getName();
				}
			});
			sizeText.setTextSimple(new Object() {
				@Override
				public String toString() {
					return String.valueOf(f.getMembers().size());
				}
			});
			flagshipText.setTextSimple(new Object() {
				@Override
				public String toString() {
					return f.getFlagShipName();
				}
			});
			flagshipText.setTextSimple(new Object() {
				@Override
				public String toString() {
					return f.getFlagShipName();
				}
			});
			sectorText.setTextSimple(new Object() {
				@Override
				public String toString() {
					return f.getFlagShipSector();
				}
			});
			missionText.setTextSimple(new Object() {
				@Override
				public String toString() {
					return f.getMissionName();
				}
			});

			nameText.getPos().y = 4;
			sizeText.getPos().y = 4;
			flagshipText.getPos().y = 4;
			sectorText.getPos().y = 4;
			missionText.getPos().y = 4;

			FleetRow r = new FleetRow(getState(), f, nameAnchorP, sizeText, flagshipText, sectorText, missionText);

//			r.expanded = new GUIElementList(getState());
//
//			GUIAncor c = new GUIAncor(getState(), 100, 10);
//
//			GUITextButton addMemberButton = new GUITextButton(getState(), 80, 24, ColorPalette.OK, Lng.str("Add Ship"), new GUICallback() {
//				@Override
//				public void callback(GUIElement callingGuiElement, MouseEvent event) {
//					if (event.pressedLeftMouse()) {
//						AddShipToFleetPlayerInput a = new AddShipToFleetPlayerInput((GameClientState) getState(), f);
//						a.activate(); AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE);
//					}
//				}				@Override
//				public boolean isOccluded() {
//					return !isActive();
//				}
//
//
//			});
//			
//			GUITextButton deleteButton = new GUITextButton(getState(), 80, 24, ColorPalette.CANCEL, Lng.str("Delete Fleet"), new GUICallback() {
//				@Override
//				public void callback(GUIElement callingGuiElement, MouseEvent event) {
//					if (event.pressedLeftMouse()) {
//						new PlayerOkCancelInput("CONFIRM", 
//								(GameClientState) getState(), Lng.str("Confirm"), Lng.str("Do you really want to delete this Fleet?")) {
//							
//							@Override
//							public void pressedOK() {
//								((FleetStateInterface)getState()).getFleetManager().requestFleetRemoveClient(f);
//							}
//							
//							@Override
//							public void onDeactivate() {
//							}
//						}.activate(); AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE);
//					}
//				}				@Override
//				public boolean isOccluded() {
//					return !isActive();
//				}
//
//
//			});
//			
//
//			c.attach(addMemberButton);
//			c.attach(deleteButton);
//
//
////			idText.setPos(4, c.getHeight() - 16, 0);
//
//			addMemberButton.setPos(0, c.getHeight(), 0);
//
//			deleteButton.setPos(90, c.getHeight(), 0);
////
////			relationButton.setPos(mailButton.getWidth() + 10 + viewRelation.getWidth() + 10, c.getHeight(), 0);
////
////			joinButton.setPos(mailButton.getWidth() + 10 + viewRelation.getWidth() + 10 + relationButton.getWidth() + 10, c.getHeight(), 0);
//
//
//			r.expanded.add(new GUIListElement(c, c, getState()));

			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}

	private class FleetRow extends Row {


		public FleetRow(InputState state, Fleet f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}
		/* (non-Javadoc)
		 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList.Row#isSimpleSelected()
		 */
		@Override
		protected boolean isSimpleSelected() {
			return ((FleetStateInterface)getState()).getFleetManager().getSelected() == (f);
		}
		@Override
		protected void clickedOnRow() {
			Fleet sel = ((FleetStateInterface)getState()).getFleetManager().getSelected();
			if(sel != f){
				((FleetStateInterface)getState()).getFleetManager().setSelected(f);
			}else{
				((FleetStateInterface)getState()).getFleetManager().setSelected(null);
			}
			super.clickedOnRow();
		}


	}




}
