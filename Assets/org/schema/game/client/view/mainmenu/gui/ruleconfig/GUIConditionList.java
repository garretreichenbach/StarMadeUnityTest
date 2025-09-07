package org.schema.game.client.view.mainmenu.gui.ruleconfig;

import java.text.DateFormat;
import java.util.Locale;
import java.util.Set;

import org.schema.game.common.controller.rules.rules.conditions.Condition;
import org.schema.game.common.controller.rules.rules.conditions.ConditionList;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;

public class GUIConditionList extends ScrollableTableList<Condition<?>>   {

	private GUIActiveInterface active;
	private GUIRuleStat stat;
	private ConditionProvider condProvider;

	public GUIConditionList(InputState state, GUIElement p, GUIActiveInterface active, GUIRuleStat stat, ConditionProvider condProvider) {
		super(state, 100, 100, p);
		this.condProvider = condProvider;
		this.active = active;
		this.stat = stat;
		stat.addObserver(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		stat.deleteObserver(this);
		super.cleanUp();
	}

	@Override
	public void initColumns() {


		addColumn(Lng.str("Name"), 0.5f, (o1, o2) -> o1.getType().getName().toLowerCase(Locale.ENGLISH).compareTo(o2.getType().getName().toLowerCase(Locale.ENGLISH)), true);
		addColumn(Lng.str("Desc"), 1f, (o1, o2) -> o1.getDescriptionShort().toLowerCase(Locale.ENGLISH).compareTo(o2.getDescriptionShort().toLowerCase(Locale.ENGLISH)));
	}

	@Override
	protected ConditionList getElementList() {
		return condProvider.isConditionsAvailable() ? condProvider.getConditions() : new ConditionList();
	}
	boolean first = true;
	@Override
	public void updateListEntries(GUIElementList mainList,
	                              Set<Condition<?>> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final DateFormat dateFormatter;

		dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault());
		int i = 0;
		for (final Condition<?> f : collection) {

			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());

			nameText.setTextSimple(new Object(){
				@Override
				public String toString(){
					return f.getType().getName();
				}
			});
			
			
			GUIClippedRow nameP = new GUIClippedRow(getState());
			nameP.attach(nameText);

			
			
			
			GUIClippedRow contP = new GUIClippedRow(getState());
			contP.activationInterface = active;
			GUITextOverlayTable contCont = new GUITextOverlayTable(getState());
			contCont.setTextSimple(new Object(){
				@Override
				public String toString() {
					return String.valueOf(f.getDescriptionShort());
				}
				
			});
			contP.attach(contCont);
			
			

			nameText.getPos().y = 5;

			RuleRow r = new RuleRow(getState(), f, nameP, contP);
			
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
	protected boolean isFiltered(Condition<?> e) {
		return super.isFiltered(e);
	}

	
	
	private class RuleRow extends Row {


		public RuleRow(InputState state, Condition<?> f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
			this.highlightSelectSimple = true;
			this.setAllwaysOneSelected(true);
		}

		@Override
		protected void clickedOnRow() {
			stat.selectedCondition = f;
			stat.selectedAction = null;
			stat.change();
			super.clickedOnRow();
		}

	}
}