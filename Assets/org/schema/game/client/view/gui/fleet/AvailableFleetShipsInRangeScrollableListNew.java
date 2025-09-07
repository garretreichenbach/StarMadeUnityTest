package org.schema.game.client.view.gui.fleet;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.GameClientController;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.server.data.FactionState;
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

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class AvailableFleetShipsInRangeScrollableListNew extends ScrollableTableList<SegmentController> {

	public AvailableFleetShipsInRangeScrollableListNew(InputState state, GUIElement p) {
		super(state, UIScale.getUIScale().scale(100), UIScale.getUIScale().scale(100), p);
		((GameClientController)getState().getController()).sectorEntitiesChangeObservable.addObserver(this);
	}
	private final Set<SegmentController> selectedSegmentController = new ObjectOpenHashSet<SegmentController>();
	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		((GameClientController)getState().getController()).sectorEntitiesChangeObservable.deleteObserver(this);
		super.cleanUp();

	}

	@Override
	public void initColumns() {


		addColumn(Lng.str("Name"), 5, (o1, o2) -> o1.getRealName().compareToIgnoreCase(o2.getRealName()), true);

		addColumn(Lng.str("Faction"), 3, (o1, o2) -> {
			FactionManager m = ((FactionState)o1.getState()).getFactionManager();
			return  m.getFactionName(o1.getFactionId()).compareTo(m.getFactionName(o2.getFactionId()));
		});
		addColumn(Lng.str("Sector"), 1, (o1, o2) -> {

			Vector3i a = o1.getClientSector();
			Vector3i b = o2.getClientSector();

			if(a == null && b == null){
				return 0;
			}
			if(a == null){
				return 1;
			}
			if(b == null){
				return -1;
			}

			return a.compareTo(b);
		});
		
		addFixedWidthColumnScaledUI(Lng.str("Docked"), 80, (o1, o2) -> Boolean.compare(o1.railController.isDockedAndExecuted(), o2.railController.isDockedAndExecuted()));
		
		addDropdownFilter(new GUIListFilterDropdown<SegmentController, Integer>(new Integer[]{0, 1, 3}) {

			@Override
			public boolean isOk(Integer input, SegmentController f) {
				return switch(input) {
					case 0 -> !f.railController.isDockedAndExecuted();
					case 1 -> f.railController.isDockedAndExecuted();
					case 2 -> true;
					default -> true;
				};
			}
		}, new CreateGUIElementInterface<Integer>() {
			@Override
			public GUIElement create(Integer o) {
				GUIAnchor c = new GUIAnchor(getState(), 10, 24);
				GUITextOverlayTableDropDown a = new GUITextOverlayTableDropDown(10, 10, getState());
				switch(o) {
					case 0 -> a.setTextSimple(Lng.str("MOTHER SHIPS"));
					case 1 -> a.setTextSimple(Lng.str("DOCKED SHIPS"));
					case 2 -> a.setTextSimple(Lng.str("ALL"));
				}
				
				a.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
				c.setUserPointer(o);
				c.attach(a);

				return c;
			}

			@Override
			public GUIElement createNeutral() {
				return null; // default is mother ships only
			}
		}, FilterRowStyle.LEFT);
		addTextFilter(new GUIListFilterText<SegmentController>() {

			@Override
			public boolean isOk(String input, SegmentController listElement) {
				return listElement.getName().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, FilterRowStyle.RIGHT);

	}

	@Override
	protected Collection<SegmentController> getElementList() {
		List<SegmentController> p = ((GameClientState)getState()).getController().getPossibleFleetAdd();
		assert(p != null);
		return p;
	}

	@Override
	public void updateListEntries(GUIElementList mainList,
	                              Set<SegmentController> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);

		final FactionManager m = ((FactionState)getState()).getFactionManager();
		final PlayerState player = ((GameClientState) getState()).getPlayer();
		int i = 0;
		for (final SegmentController f : collection) {

			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			GUITextOverlayTable factionText = new GUITextOverlayTable(getState());
			
			GUITextOverlayTable sectorText = new GUITextOverlayTable(getState());
			GUITextOverlayTable dockedText = new GUITextOverlayTable(getState());

			GUIClippedRow nameAnchorP = new GUIClippedRow(getState());
			nameAnchorP.attach(nameText);
			nameText.setTextSimple(new Object() {
				@Override
				public String toString() {
					return f.getRealName();
				}
			});
			factionText.setTextSimple(new Object() {
				@Override
				public String toString() {
					return m.getFactionName(f.getFactionId());
				}
			});
			
			sectorText.setTextSimple(new Object() {
				@Override
				public String toString() {
					Vector3i clientSector = f.getClientSector();
					return clientSector == null ? "-" : clientSector.toStringPure();
				}
			});
			dockedText.setTextSimple(new Object() {
				@Override
				public String toString() {
					return f.railController.isDockedAndExecuted() ? "X" : "";
				}
			});

			nameText.getPos().y = UIScale.getUIScale().inset;
			factionText.getPos().y = UIScale.getUIScale().inset;
			sectorText.getPos().y = UIScale.getUIScale().inset;
			dockedText.getPos().y = UIScale.getUIScale().inset;

			SegmentControllerRow r = new SegmentControllerRow(getState(), f, nameAnchorP, factionText, sectorText, dockedText);

			r.expanded = null;
			

			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}
	public Set<SegmentController> getSelectedSegmentController() {
		return selectedSegmentController;
	}
	private class SegmentControllerRow extends Row {


		public SegmentControllerRow(InputState state, SegmentController f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}
		/* (non-Javadoc)
		 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList.Row#isSimpleSelected()
		 */
		@Override
		protected boolean isSimpleSelected() {
			return getSelectedSegmentController().contains(f);
		}
		/* (non-Javadoc)
		 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList.Row#clickedOnRow()
		 */
		@Override
		protected void clickedOnRow() {
			if (isSimpleSelected()) {
				getSelectedSegmentController().remove(f);
			} else {
				getSelectedSegmentController().add(f);
			}
		}

	}




}
