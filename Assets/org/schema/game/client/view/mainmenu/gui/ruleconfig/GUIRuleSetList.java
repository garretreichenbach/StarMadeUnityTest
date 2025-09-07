package org.schema.game.client.view.mainmenu.gui.ruleconfig;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import org.schema.game.common.controller.rules.RuleSet;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICheckBox;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;

public class GUIRuleSetList extends ScrollableTableList<RuleSet>   {

	private GUIActiveInterface active;
	private GUIRuleSetStat stat;

	public GUIRuleSetList(InputState state, GUIElement p, GUIActiveInterface active, GUIRuleSetStat stat) {
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


		addColumn(Lng.str("Name"), 4, (o1, o2) -> o1.uniqueIdentifier.toLowerCase(Locale.ENGLISH).compareTo(o2.uniqueIdentifier.toLowerCase(Locale.ENGLISH)), true);
		if(stat.getProperties() != null) {
			addFixedWidthColumnScaledUI(Lng.str("Global"), 40, (o1, o2) -> (stat.getProperties().isGlobal(o1) ? 1 : -1) - (stat.getProperties().isGlobal(o2) ? 1 : -1));
//			addFixedWidthColumn(Lng.str("Apply For"), 200, new Comparator<RuleSet>() {
//				@Override
//				public int compare(RuleSet o1, RuleSet o2) {
//					return stat.getProperties().getSubType(o1) - stat.getProperties().getSubType(o2);
//				}
//			}, false);
		}
		addFixedWidthColumnScaledUI(Lng.str("Size"), 40, (o1, o2) -> (o1.size() - o2.size()));
	}

	@Override
	protected Collection<RuleSet> getElementList() {
		return stat.manager.getRuleSets();
	}
	boolean first = true;
	@Override
	public void updateListEntries(GUIElementList mainList,
	                              Set<RuleSet> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		
//		System.err.println("UPDATE RULE SET LIST FOR "+collection.size());
		final DateFormat dateFormatter;

		dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault());
		int i = 0;
		for (final RuleSet f : collection) {

			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());

			nameText.setTextSimple(new Object(){
				@Override
				public String toString(){
					return f.uniqueIdentifier;
				}
			});
			
			GUIClippedRow sizeP = new GUIClippedRow(getState());
			
			sizeP.activationInterface = active;
			GUIClippedRow nameP = new GUIClippedRow(getState());
			nameP.attach(nameText);

			GUITextOverlayTable size = new GUITextOverlayTable(getState());
			size.setTextSimple(new Object(){
				@Override
				public String toString() {
					return String.valueOf(f.size());
				}
				
			});
			sizeP.attach(size);

			nameText.getPos().y = 5;

			RuleSetRow r;
			if(stat.getProperties() != null) {
				GUICheckBox global = new GUICheckBox(getState()) {
					
					@Override
					protected boolean isActivated() {
						return stat.getProperties().isGlobal(f);
					}
					
					@Override
					protected void deactivate() throws StateParameterNotFoundException {
						stat.getProperties().setGlobal(f, false, false);
					}
					
					@Override
					protected void activate() throws StateParameterNotFoundException {
						stat.getProperties().setGlobal(f, true, false);
					}
				};
				GUIClippedRow globalP = new GUIClippedRow(getState());
				globalP.attach(global);
				
				
//				GUIDropDownList dropDown = new GUIDropDownList(getState(), 200, 24, 300, new DropDownCallback() {
//					
//					@Override
//					public void onSelectionChanged(GUIListElement element) {
//						
//					}
//				});
//				GUIClippedRow dopDownP = new GUIClippedRow(getState());
//				dopDownP.attach(dropDown);
				
				r = new RuleSetRow(getState(), f, nameP, globalP, sizeP);
			}else {
				r = new RuleSetRow(getState(), f, nameP, sizeP);
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
	protected boolean isFiltered(RuleSet e) {
		return super.isFiltered(e);
	}

	
	
	private class RuleSetRow extends Row {


		public RuleSetRow(InputState state, RuleSet f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
			this.highlightSelectSimple = true;
			this.setAllwaysOneSelected(true);
		}

		@Override
		protected void clickedOnRow() {
			stat.selectedRuleset = f;
			stat.change();
			super.clickedOnRow();
		}

	}
}