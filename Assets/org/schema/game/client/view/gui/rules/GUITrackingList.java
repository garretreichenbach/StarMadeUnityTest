package org.schema.game.client.view.gui.rules;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.schema.game.client.controller.EntityTrackingChangedListener;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;
import org.schema.schine.network.objects.Sendable;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GUITrackingList extends ScrollableTableList<SimpleTransformableSendableObject<?>> implements EntityTrackingChangedListener   {

	private GUIActiveInterface active;

	public GUITrackingList(GameClientState state, GUIElement p, GUIActiveInterface active) {
		super(state, 100, 100, p);
		this.active = active;
		state.getController().addEntityTrackingListener(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		((GameClientState)getState()).getController().removeEntityTrackingListener(this);
		super.cleanUp();
	}

	@Override
	public void initColumns() {


		addColumn(Lng.str("Name"), 1, (o1, o2) -> o1.getName().toLowerCase(Locale.ENGLISH).compareTo(o2.getName().toLowerCase(Locale.ENGLISH)), true);
		addColumn(Lng.str("Type"), 0.5f, (o1, o2) -> o1.getType().getName().toLowerCase(Locale.ENGLISH).compareTo(o2.getType().getName().toLowerCase(Locale.ENGLISH)), true);
		addColumn(Lng.str("Spawner"), 1f, (o1, o2) -> getSpwner(o1).compareTo(getSpwner(o2)));
		addColumn(Lng.str("LastMod"), 1f, (o1, o2) -> getLastMod(o1).compareTo(getLastMod(o2)));
		addColumn(Lng.str("Sector"), 1f, (o1, o2) -> o1.getClientSector().compareTo(o2.getClientSector()));
	}
	List<SimpleTransformableSendableObject<?>> ents = new ObjectArrayList<SimpleTransformableSendableObject<?>>();
	@Override
	protected List<SimpleTransformableSendableObject<?>> getElementList() {
		ents.clear();
		for(Sendable s : ((GameClientState)getState()).getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
			if(s instanceof SimpleTransformableSendableObject<?> && ((SimpleTransformableSendableObject<?>)s).isTracked()) {
				ents.add((SimpleTransformableSendableObject<?>)s);
			}
		}
		return ents;
	}
	boolean first = true;
	
	public String getSpwner(SimpleTransformableSendableObject<?> f) {
		return f instanceof SegmentController ? ((SegmentController)f).getSpawner() : Lng.str("n/a");
	}
	public String getLastMod(SimpleTransformableSendableObject<?> f) {
		return f instanceof SegmentController ? ((SegmentController)f).getLastModifier() : Lng.str("n/a");
	}
	@Override
	public void updateListEntries(GUIElementList mainList,
	                              Set<SimpleTransformableSendableObject<?>> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final DateFormat dateFormatter;

		dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault());
		int i = 0;
		for (final SimpleTransformableSendableObject<?> f : collection) {

			
			
			GUIClippedRow nameP = getSimpleRow(new Object(){
				@Override
				public String toString() {
					return f.getName();
				}
				
			}, active);
			GUIClippedRow contP = getSimpleRow(new Object(){
				@Override
				public String toString() {
					return f.getType().getName();
				}
				
			}, active);
			GUIClippedRow spawnerP = getSimpleRow(new Object(){
				@Override
				public String toString() {
					return getSpwner(f);
				}
				
			}, active);
			GUIClippedRow lastMod = getSimpleRow(new Object(){
				@Override
				public String toString() {
					return getLastMod(f);
				}
				
			}, active);
			GUIClippedRow sectorP = getSimpleRow(new Object(){
				@Override
				public String toString() {
					return f.getClientSector().toStringPure();
				}
				
			}, active);


			RuleRow r = new RuleRow(getState(), f, nameP, contP, spawnerP, lastMod, sectorP);
			
			GUIAnchor c = new GUIAnchor(getState(), 100, 100);


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
	protected boolean isFiltered(SimpleTransformableSendableObject<?> e) {
		return super.isFiltered(e);
	}

	
	
	private class RuleRow extends Row {


		public RuleRow(InputState state, SimpleTransformableSendableObject<?> f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
			this.highlightSelectSimple = true;
			this.setAllwaysOneSelected(true);
		}

		@Override
		protected void clickedOnRow() {
			super.clickedOnRow();
		}

	}



	@Override
	public void onTrackingChanged() {
		flagDirty();
	}
}