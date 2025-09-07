package org.schema.game.client.view.mainmenu.gui.ruleconfig;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import org.schema.common.util.CompareTools;
import org.schema.game.common.controller.rules.rules.Rule;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;

public class GUIRuleList extends ScrollableTableList<Rule>   {

	private GUIActiveInterface active;
	private GUIRuleCollection stat;

	
	public GUIRuleList(InputState state, GUIElement p, GUIActiveInterface active, GUIRuleCollection stat) {
		super(state, 100, 100, p);
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


		addColumn(Lng.str("Name"), 1, (o1, o2) -> o1.getUniqueIdentifier().toLowerCase(Locale.ENGLISH).compareTo(o2.getUniqueIdentifier().toLowerCase(Locale.ENGLISH)), true);
		addColumn(Lng.str("Type"), 1, (o1, o2) -> (o1.ruleType.getName().toLowerCase(Locale.ENGLISH).compareTo(o2.ruleType.getName().toLowerCase(Locale.ENGLISH))));
		addFixedWidthColumnScaledUI(Lng.str("Cond."), 45, (o1, o2) -> (o1.getConditionCount() - o2.getConditionCount()));
		addFixedWidthColumnScaledUI(Lng.str("Act."), 45, (o1, o2) -> (o1.getActionCount() - o2.getActionCount()));
		
		if(stat.canRulesBeIgnored()) {
			addFixedWidthColumnScaledUI(Lng.str("Ign."), 40, (o1, o2) -> CompareTools.compare(o1.ignoreRule, o2.ignoreRule));
		}
	}

	@Override
	protected Collection<Rule> getElementList() {
		return stat.getRules();
	}
	boolean first = true;
	@Override
	public void updateListEntries(GUIElementList mainList,
	                              Set<Rule> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final DateFormat dateFormatter;

		dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault());
		int i = 0;
		for (final Rule f : collection) {

			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());

			nameText.setTextSimple(new Object(){
				@Override
				public String toString(){
					return f.getUniqueIdentifier();
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
					return String.valueOf(f.getConditionCount());
				}
				
			});
			contP.attach(contCont);
			
			
			GUIClippedRow actP = new GUIClippedRow(getState());
			actP.activationInterface = active;
			GUITextOverlayTable actCont = new GUITextOverlayTable(getState());
			actCont.setTextSimple(new Object(){
				@Override
				public String toString() {
					return String.valueOf(f.getActionCount());
				}
				
			});
			actP.attach(actCont);
			
			
			GUIClippedRow rsP = new GUIClippedRow(getState());
			rsP.activationInterface = active;
			GUITextOverlayTable rsCont = new GUITextOverlayTable(getState());
			rsCont.setTextSimple(new Object(){
				@Override
				public String toString() {
					return f.ruleType.getName();
				}
				
			});
			rsP.attach(rsCont);

			nameText.getPos().y = 5;

			RuleRow r;
			if(stat.canRulesBeIgnored()) {
				GUIClippedRow actIR = new GUIClippedRow(getState());
				actIR.activationInterface = active;
				GUITextOverlayTable actI = new GUITextOverlayTable(getState());
				actI.setTextSimple(new Object(){
					@Override
					public String toString() {
						return f.ignoreRule ? "X" : "";
					}
					
				});
				actIR.attach(actI);
				
				r = new RuleRow(getState(), f, nameP, rsP, contP, actP, actIR);
			}else {
			
				r = new RuleRow(getState(), f, nameP, rsP, contP, actP);
			}
			
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
	protected boolean isFiltered(Rule e) {
		return super.isFiltered(e);
	}

	
	
	private class RuleRow extends Row {


		public RuleRow(InputState state, Rule f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
			this.highlightSelectSimple = true;
			this.setAllwaysOneSelected(true);
		}

		@Override
		protected void clickedOnRow() {
			stat.setSelectedRule(f);
			stat.setSelectedAction(null);
			stat.setSelectedCondition(null);
			stat.change();
			super.clickedOnRow();
		}

	}
}