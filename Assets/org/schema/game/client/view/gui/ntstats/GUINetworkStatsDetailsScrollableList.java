package org.schema.game.client.view.gui.ntstats;

import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.StringTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.observer.DrawerObservable;
import org.schema.game.common.controller.observer.DrawerObserver;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;
import org.schema.schine.network.DataStatsEntry;
import org.schema.schine.network.objects.NetworkObject;

import java.util.*;
import java.util.Map.Entry;

public class GUINetworkStatsDetailsScrollableList extends ScrollableTableList<StatsDetailData> implements DrawerObserver {

	private List<StatsDetailData> list = new ObjectArrayList<StatsDetailData>();

	public GUINetworkStatsDetailsScrollableList(InputState state, GUIElement p, DataStatsEntry entry) {
		super(state, 100, 100, p);
		for (Entry<Class<? extends NetworkObject>, Int2LongOpenHashMap> a : entry.entry.entrySet()) {
			list.add(new StatsDetailData(a.getKey(), a.getValue()));
		}
		Comparator<StatsDetailData> comparator = (o1, o2) -> o1.volume > o2.volume ? -1 : (o1.volume < o2.volume ? 1 : 0);

		Collections.sort(list, comparator);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		super.cleanUp();

	}

	@Override
	public void initColumns() {


		addColumn("Class", 4f, (o1, o2) -> o1.ntObjClass.getSimpleName().compareTo(o2.ntObjClass.getSimpleName()));
		addColumn("Volume", 1f, (o1, o2) -> o1.volume > o2.volume ? 1 : (o1.volume < o2.volume ? -1 : 0));
		addFixedWidthColumnScaledUI("Fields", 80, (o1, o2) -> o1.data.size() - o2.data.size());

	}

	@Override
	protected Collection<StatsDetailData> getElementList() {
		return list;
	}

	@Override
	public void updateListEntries(GUIElementList mainList,
	                              Set<StatsDetailData> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);

		final PlayerState player = ((GameClientState) getState()).getPlayer();
		int i = 0;
		for (final StatsDetailData f : collection) {

			GUITextOverlayTable classText = new GUITextOverlayTable(getState());
			GUITextOverlayTable dateText = new GUITextOverlayTable(getState());
			GUITextOverlayTable volumeText = new GUITextOverlayTable(getState());

			classText.setTextSimple(f.ntObjClass.getSimpleName());
			volumeText.setTextSimple(StringTools.readableFileSize(f.volume));
			dateText.setTextSimple(f.data.size());

			GUIClippedRow classSizeAnchorP = new GUIClippedRow(getState());
			classSizeAnchorP.attach(classText);

			GUIClippedRow fieldSizeAnchorP = new GUIClippedRow(getState());
			fieldSizeAnchorP.attach(dateText);

			GUIClippedRow volumeAnchorP = new GUIClippedRow(getState());
			volumeAnchorP.attach(volumeText);

			classText.getPos().y = 5;
			volumeText.getPos().y = 5;
			dateText.getPos().y = 5;

			StatRow r = new StatRow(getState(), f, classSizeAnchorP, volumeAnchorP, fieldSizeAnchorP);

			r.expanded = new GUIElementList(getState());

			ObjectArrayList<FieldEntry> entries = new ObjectArrayList<FieldEntry>();
			String[] name = NetworkObject.getFieldNames(f.ntObjClass);
			for (it.unimi.dsi.fastutil.ints.Int2LongMap.Entry e : f.data.int2LongEntrySet()) {
				FieldEntry ef = new FieldEntry(name[e.getIntKey()], e.getLongValue());
				entries.add(ef);
			}
			Collections.sort(entries);
			for (FieldEntry ef : entries) {
				if (ef.volume > 0) {
					r.expanded.addWithoutUpdate(ef.toGUIListElement());
				}
			}
			r.expanded.updateDim();

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
	protected boolean isFiltered(StatsDetailData e) {
		return e.volume <= 0 || super.isFiltered(e);
	}

	@Override
	public void update(DrawerObservable observer, Object userdata,
	                   Object message) {
		flagDirty();
	}

	private class StatRow extends Row {


		public StatRow(InputState state, StatsDetailData f, GUIElement... elements) {
			super(state, f, elements);
			this.f = f;
			this.highlightSelect = true;
		}


	}

	private class FieldEntry implements Comparator<FieldEntry>, Comparable<FieldEntry> {
		public String name;
		public long volume;

		public FieldEntry(String name, long volume) {
			super();
			this.name = name;
			this.volume = volume;
		}

		public GUIListElement toGUIListElement() {
			GUIAnchor e = new GUIAnchor(getState(), 200, 25);
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			nameText.setTextSimple(name);

			GUITextOverlayTable vol = new GUITextOverlayTable(getState());
			vol.setTextSimple(StringTools.readableFileSize(volume));

			nameText.setPos(4, 5, 0);
			vol.setPos(330, 5, 0);

			e.attach(nameText);
			e.attach(vol);

			return new GUIListElement(e, e, getState());
		}

		@Override
		public int compareTo(FieldEntry o) {
			return compare(this, o);
		}		@Override
		public int compare(FieldEntry o1, FieldEntry o2) {
			return o1.volume > o2.volume ? -1 : (o1.volume < o2.volume ? 1 : 0);
		}



	}

}
