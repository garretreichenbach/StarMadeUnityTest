package org.schema.game.client.view.gui.lagStats;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.observer.DrawerObservable;
import org.schema.game.common.controller.observer.DrawerObserver;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;
import org.schema.schine.network.objects.Sendable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class GUILagObjectsScrollableList extends ScrollableTableList<LagObject> implements DrawerObserver {

	private List<LagObject> list = new ObjectArrayList<LagObject>();
	public final LagDataStatsList stats;

	public GUILagObjectsScrollableList(GameClientState state, GUIElement p, LagDataStatsList stats) {
		super(state, 100, 100, p);
		this.stats = stats;

	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		super.cleanUp();

	}

	public void notifyGUIFromDataStatistics() {
		flagDirty();
	}

	@Override
	public void initColumns() {
		addColumn("Name", 4f, (o1, o2) -> o1.getName().compareTo(o2.getName()));
		addColumn("Sector", 1f, (o1, o2) -> o1.getSector().compareTo(o2.getSector()));
		addColumn("Type", 1f, (o1, o2) -> o1.getType().compareTo(o2.getType()));
		addFixedWidthColumnScaledUI("Lag (ms/update)", 130, (o1, o2) -> o1.getLagTime() > o2.getLagTime() ? 1 : (o1.getLagTime() < o2.getLagTime() ? -1 : 0));

	}

	@Override
	protected Collection<LagObject> getElementList() {
		list.clear();

		if(!stats.isAnySelected()) {

			ObjectArrayList<Sendable> laggyList = ((GameClientState) getState()).laggyList;
			for(Sendable s : laggyList) {
				LagObject lagObject = new LagObject(s);
				list.add(lagObject);
			}
		} else {
			LagDataStatsEntry l = stats.getSelected();

			for(LagObject a : l.entry) {
				list.add(a);
			}
		}
		return list;
	}

	@Override
	public void updateListEntries(GUIElementList mainList,
	                              Set<LagObject> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);

		final PlayerState player = ((GameClientState) getState()).getPlayer();
		int i = 0;
		for(final LagObject f : collection) {

			GUITextOverlayTable classText = new GUITextOverlayTable(getState());
			GUITextOverlayTable sectorText = new GUITextOverlayTable(getState());
			GUITextOverlayTable dateText = new GUITextOverlayTable(getState());
			GUITextOverlayTable volumeText = new GUITextOverlayTable(getState());

			classText.setTextSimple(new Object() {
				@Override
				public String toString() {
					return f.getName();
				}

			});
			volumeText.setTextSimple(new Object() {
				@Override
				public String toString() {
					return String.valueOf(f.getLagTime());
				}

			});
			dateText.setTextSimple(new Object() {
				@Override
				public String toString() {
					return f.getType();
				}

			});
			sectorText.setTextSimple(new Object() {
				@Override
				public String toString() {
					return f.getSector();
				}

			});

			GUIClippedRow classSizeAnchorP = new GUIClippedRow(getState());
			classSizeAnchorP.attach(classText);

			GUIClippedRow fieldSizeAnchorP = new GUIClippedRow(getState());
			fieldSizeAnchorP.attach(dateText);

			GUIClippedRow sectorTextAnchorP = new GUIClippedRow(getState());
			sectorTextAnchorP.attach(sectorText);

			GUIClippedRow volumeAnchorP = new GUIClippedRow(getState());
			volumeAnchorP.attach(volumeText);

			classText.getPos().y = 5;
			volumeText.getPos().y = 5;
			dateText.getPos().y = 5;

			StatRow r = new StatRow(getState(), f, classSizeAnchorP, sectorTextAnchorP, fieldSizeAnchorP, volumeAnchorP);

			r.expanded = null;

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
	protected boolean isFiltered(LagObject e) {
		return super.isFiltered(e);
	}

	@Override
	public void update(DrawerObservable observer, Object userdata,
	                   Object message) {
		flagDirty();
	}

	private class StatRow extends Row {

		public StatRow(InputState state, LagObject f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}

	}

}
