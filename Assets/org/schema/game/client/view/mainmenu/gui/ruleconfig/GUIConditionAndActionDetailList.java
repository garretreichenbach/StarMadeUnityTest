package org.schema.game.client.view.mainmenu.gui.ruleconfig;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.schema.game.common.controller.rules.rules.conditions.RuleFieldValue;
import org.schema.game.common.controller.rules.rules.conditions.RuleFieldValueInterface;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GUIConditionAndActionDetailList extends ScrollableTableList<RuleFieldValue>   {

	private GUIActiveInterface active;
	private GUIRuleStat stat;
	private final RuleFieldValueInterface rfv;
	private GUIElement p;
	public GUIConditionAndActionDetailList(InputState state, GUIElement p, GUIActiveInterface active, GUIRuleStat stat, RuleFieldValueInterface rfv) {
		super(state, 100, 100, p);
		this.active = active;
		this.stat = stat;
		this.rfv = rfv;
		this.p = p;
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


		addColumn(Lng.str("Name"), 0.5f, (o1, o2) -> o1.a.tag().toLowerCase(Locale.ENGLISH).compareTo(o2.a.tag().toLowerCase(Locale.ENGLISH)), true);
		
		addColumn(Lng.str("Value"), 1f, (o1, o2) -> o1.getValueAsString().toLowerCase(Locale.ENGLISH).compareTo(o2.getValueAsString().toLowerCase(Locale.ENGLISH)));
	}

	@Override
	protected List<RuleFieldValue> getElementList() {
		if(rfv == null) {
			List<RuleFieldValue> c = new ObjectArrayList<RuleFieldValue>();
			System.err.println("NO RULE FIELD VALUE");
			return c;
		}
		List<RuleFieldValue> c = rfv.createFieldValues();
		return c;
	}
	boolean first = true;
	@Override
	public void updateListEntries(GUIElementList mainList,
	                              Set<RuleFieldValue> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		int i = 0;
		for (final RuleFieldValue f : collection) {

			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());

			nameText.setTextSimple(new Object(){
				@Override
				public String toString(){
					return f.a.tag();
				}
			});
			
			
			GUIClippedRow nameP = new GUIClippedRow(getState());
			nameP.attach(nameText);

			
			
			
			GUIClippedRow contP = new GUIClippedRow(getState());
			contP.activationInterface = active;
			
			contP.attach(f.createGUIEditElement(getState(), stat, contP));
			
			

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
	protected boolean isFiltered(RuleFieldValue e) {
		return super.isFiltered(e);
	}

	
	
	private class RuleRow extends Row {


		public RuleRow(InputState state, RuleFieldValue f, GUIElement... elements) {
			super(state, f, elements);
//			this.highlightSelect = true;
//			this.highlightSelectSimple = true;
//			this.setAllwaysOneSelected(true);
		}

//		@Override
//		protected void clickedOnRow() {
//			stat.selectedCondition = f;
//			stat.selectedAction = null;
//			stat.change();
//			super.clickedOnRow();
//		}

	}
}