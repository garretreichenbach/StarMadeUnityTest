package org.schema.game.client.view.mainmenu.gui;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActivatableTextBar;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GUIFileChooserList extends ScrollableTableList<GUIFileEntry>  {

	private GUIActiveInterface active;
	private final FileChooserStats stats;

	public GUIFileChooserList(InputState state, GUIElement p, GUIActiveInterface active, FileChooserStats stats) {
		super(state, 100, 100, p);
		this.active = active;
		stats.addObserver(this);
		this.stats = stats;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
//		messageController.deleteObserver(this);
		super.cleanUp();
		stats.deleteObserver(this);
	}

	@Override
	public void initColumns() {


		addFixedWidthColumnScaledUI("Type", 60, GUIFileEntry::compareTo, true);
		addColumn("Name", 2, (o1, o2) -> (o1.getName().compareTo(o2.getName())), false);
		
		
	}

	@Override
	protected Collection<GUIFileEntry> getElementList() {
		List<GUIFileEntry> c = new ObjectArrayList<GUIFileEntry>();
		stats.getFiles(c);
		return c;
	}
	boolean first = true;
	private GUIActivatableTextBar playerNameBar;
	
	@Override
	public void updateListEntries(GUIElementList mainList,
	                              Set<GUIFileEntry> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final DateFormat dateFormatter;

		dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault());
		int i = 0;
		for (final GUIFileEntry f : collection) {

			ScrollableTableList<GUIFileEntry>.GUIClippedRow tp = getSimpleRow(new Object(){
				@Override
				public String toString() {
					return f.isUpDir() ?  "DIR" : (f.isDirectory() ? "DIR" : "FILE");
				}
				
			}, active);
			ScrollableTableList<GUIFileEntry>.GUIClippedRow nm = getSimpleRow(new Object(){
				@Override
				public String toString() {
					return f.getName();
				}
				
			}, active);
			
			
			

			GUIFileEntryRow r = new GUIFileEntryRow(getState(), f, tp, nm);
			

			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
		first = false;
	}
	
	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#isFiltered(java.lang.Object)
	 */
	@Override
	protected boolean isFiltered(GUIFileEntry e) {
		return stats.isFilteredOut(e) || super.isFiltered(e);
	}

	
	
	private class GUIFileEntryRow extends Row {


		public GUIFileEntryRow(InputState state, GUIFileEntry f, GUIElement... elements) {
			super(state, f, elements);
			
			this.highlightSelect = true;
			this.highlightSelectSimple = true;
			setAllwaysOneSelected(true);
			this.rightClickSelectsToo = true;
		}

		@Override
		public void onDoubleClick() {
			super.onDoubleClick();
			stats.onDoubleClick(f);
		}

		@Override
		protected void clickedOnRow() {
			super.clickedOnRow();
			stats.onSingleClick(f);
		}
		



	}



	

}
