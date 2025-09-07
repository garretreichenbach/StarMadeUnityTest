package org.schema.game.client.view.mainmenu.gui.effectconfig;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import org.schema.game.common.data.blockeffects.config.EffectConfigElement;
import org.schema.game.common.data.blockeffects.config.elements.ModifierStackType;
import org.schema.game.common.data.blockeffects.config.parameter.StatusEffectParameter;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIDropDownList;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GUIEffectConfigElementList extends ScrollableTableList<EffectConfigElement>   {

	private GUIActiveInterface active;
	private GUIEffectStat stat;
	private Collection<EffectConfigElement> empty = new ObjectArrayList<EffectConfigElement>();

	public GUIEffectConfigElementList(InputState state, GUIElement p, GUIActiveInterface active, GUIEffectStat stat) {
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


		addColumn(Lng.str("Category"), 1, (o1, o2) -> o1.getType().getCategory().getName().toLowerCase(Locale.ENGLISH).compareTo(o2.getType().getCategory().getName().toLowerCase(Locale.ENGLISH)), true);
		addColumn(Lng.str("Type"), 2, (o1, o2) -> o1.getType().getName().toLowerCase(Locale.ENGLISH).compareTo(o2.getType().getName().toLowerCase(Locale.ENGLISH)), true);
		addColumn(Lng.str("Value"), 2, (o1, o2) -> o1.getParamString(o1.value).compareTo(o2.getParamString(o2.value)));
		addColumn(Lng.str("Wpn"), 1, (o1, o2) -> o1.getParamString(o1.weaponType).compareTo(o2.getParamString(o2.weaponType)));
		addFixedWidthColumnScaledUI(Lng.str("Modifier"), 110, (o1, o2) -> o1.stackType.name().toLowerCase(Locale.ENGLISH).compareTo(o2.stackType.name().toLowerCase(Locale.ENGLISH)), true);
		addFixedWidthColumnScaledUI(Lng.str("Priority"), 60, (o1, o2) -> o1.priority - o2.priority, true);
	}

	@Override
	protected Collection<EffectConfigElement> getElementList() {
		if(stat.selectedGroup != null){
			return stat.selectedGroup.elements;
		}else{
			return empty;
		}
	}
	boolean first = true;
	
	@Override
	public void updateListEntries(GUIElementList mainList,
	                              Set<EffectConfigElement> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final DateFormat dateFormatter;

		dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault());
		int i = 0;
		for (final EffectConfigElement f : collection) {

			GUITextOverlayTable catText = new GUITextOverlayTable(getState());
			catText.setTextSimple(f.getType().getCategory().getName());
			GUIClippedRow catP = new GUIClippedRow(getState());
			catP.attach(catText);
			catText.getPos().y = 5;
			
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			nameText.setTextSimple(f.getType().getName());
			GUIClippedRow nameP = new GUIClippedRow(getState());
			nameP.attach(nameText);

			nameText.getPos().y = 5;

			
			GUIClippedRow prio = new GUIClippedRow(getState());
			prio.attach(f.createPriorityBar(getState(), prio));
			
			EffectConfigElementRow r = new EffectConfigElementRow(getState(), f, catP, nameP, getParamCol(f, f.value), getParamColWep(f, f.weaponType), getStackMod(f), prio);
			
			GUIAnchor c = new GUIAnchor(getState(), 100, 100);


			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
		first = false;
	}
	private GUIElement getStackMod(final EffectConfigElement f) {
		ModifierStackType[] values = ModifierStackType.values();
		GUIElement[] sts = new GUIElement[values.length];
		int selected = 0;
		for(int i = 0; i < values.length; i++){
			ModifierStackType modifierStackType = values[i];
			GUIAnchor c = new GUIAnchor(getState(), 100, 24);
			GUITextOverlayTable t = new GUITextOverlayTable(getState());
			t.setTextSimple(modifierStackType.name());
			t.setPos(5, 5, 0);
			c.attach(t);
			sts[i] = c;
			c.setUserPointer(modifierStackType);
			if(f.stackType == modifierStackType){
				selected = i;
			}
		}
		
		GUIDropDownList stack = new GUIDropDownList(getState(), 100, 24, 80, element -> {
			ModifierStackType s = (ModifierStackType)element.getContent().getUserPointer();
			f.stackType = s;
		},sts);
		
		stack.setSelectedIndex(selected);
		
		GUIClippedRow nameP = new GUIClippedRow(getState());
		nameP.attach(stack);
		return nameP;
	}

	private GUIElement getParamColWep(EffectConfigElement f, StatusEffectParameter value) {
		GUIClippedRow pp0 = new GUIClippedRow(getState());
		pp0.activationInterface = active;
		
		if(value == null){
			GUITextOverlayTable p0 = new GUITextOverlayTable(getState());
			p0.setTextSimple(f.getParamString(value));
			p0.getPos().y = 5;
			pp0.attach(p0);
			return pp0;
		}
		GUIAnchor total = new GUIAnchor(getState(), 250, 24);
		total.onInit();
		GUIElement weaponDropdown = value.getWeaponDropdown(getState());
		total.attach(weaponDropdown);
		return total;
		
	}
	private GUIElement getParamCol(EffectConfigElement f, StatusEffectParameter value) {
		GUIClippedRow pp0 = new GUIClippedRow(getState());
		pp0.activationInterface = active;
		
		if(value == null){
			GUITextOverlayTable p0 = new GUITextOverlayTable(getState());
			p0.setTextSimple(f.getParamString(value));
			p0.getPos().y = 5;
			pp0.attach(p0);
			return pp0;
		}
		
		GUIAnchor total = new GUIAnchor(getState(), 250, 24);
		
		GUIAnchor cName = new GUIAnchor(getState(), 80, 24);
		GUITextOverlayTable p0 = new GUITextOverlayTable(getState());
		p0.setTextSimple(f.getParamString(value));
		p0.getPos().y = 5;
		cName.attach(p0);
		
		GUIAnchor cText = new GUIAnchor(getState(), 100, 24);
		
		cText.attach(value.createEditBar(getState(), cText));
		
		total.attach(cName);
		total.attach(cText);
		cText.getPos().x = cName.getWidth();
		
		pp0.attach(total);
		return pp0;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#isFiltered(java.lang.Object)
	 */
	@Override
	protected boolean isFiltered(EffectConfigElement e) {
		return super.isFiltered(e);
	}

	
	
	private class EffectConfigElementRow extends Row {


		public EffectConfigElementRow(InputState state, EffectConfigElement f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
			this.highlightSelectSimple = true;
			this.setAllwaysOneSelected(true);
		}

		@Override
		protected void clickedOnRow() {
			stat.selectedElement = f;
			stat.change();
			super.clickedOnRow();
		}

	}



	

}
