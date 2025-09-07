package org.schema.game.client.view.mainmenu.gui.effectconfig;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.common.data.blockeffects.config.ConfigEntityManager;
import org.schema.game.common.data.blockeffects.config.EffectModule;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;

import java.text.DateFormat;
import java.util.*;

public class GUIEffectConfigTestList extends ScrollableTableList<EffectModule>   {

	private ConfigEntityManager stat;
	private Collection<EffectModule> empty = new ObjectArrayList<EffectModule>();

	public GUIEffectConfigTestList(InputState state, GUIElement p, GUIActiveInterface active, ConfigEntityManager stat) {
		super(state, 100, 100, p);
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
		addColumn(Lng.str("Type"), 1, (o1, o2) -> o1.getType().getName().toLowerCase(Locale.ENGLISH).compareTo(o2.getType().getName().toLowerCase(Locale.ENGLISH)), true);
		addColumn(Lng.str("Value"), 2, (o1, o2) -> o1.getValueString().compareTo(o2.getValueString()));
	}
	private List<EffectModule> modList = new ObjectArrayList<EffectModule>();
	@Override
	protected Collection<EffectModule> getElementList() {
		
		modList.clear();
		for(EffectModule m : stat.getModulesList()){
			if(!m.getWeaponType().isEmpty()){
				modList.addAll(m.getWeaponType().values());
			}else{
				modList.add(m);
			}
		}
//		System.err.println("MOD LIST: "+modList+"; ");
		return modList;
	}
	boolean first = true;
	
	@Override
	public void updateListEntries(GUIElementList mainList,
	                              Set<EffectModule> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final DateFormat dateFormatter;

		dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault());
		int i = 0;
		for (final EffectModule f : collection) {

			GUITextOverlayTable catText = new GUITextOverlayTable(getState());
			assert(f != null):f;

			assert(f.getType() != null);
			assert(f.getType().getCategory() != null);
			assert(f.getType().getCategory().getName() != null);
			catText.setTextSimple(f.getType().getCategory().getName());

			GUIClippedRow catP = new GUIClippedRow(getState());
			catP.attach(catText);
			catText.getPos().y = 5;
			
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			nameText.setTextSimple(f.getType().getName()+(f.getParent() != null ? f.getOwnWeaponTypeString() : ""));
			GUIClippedRow nameP = new GUIClippedRow(getState());
			nameP.attach(nameText);

			nameText.getPos().y = 5;
			
			GUITextOverlayTable valueText = new GUITextOverlayTable(getState());
			valueText.setTextSimple(f.getValueString());
			GUIClippedRow valueP = new GUIClippedRow(getState());
			valueP.attach(valueText);
			
			valueText.getPos().y = 5;

			
			
			EffectModuleRow r = new EffectModuleRow(getState(), f, catP, nameP, valueP);
			
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
	protected boolean isFiltered(EffectModule e) {
		return super.isFiltered(e);
	}

	
	
	private class EffectModuleRow extends Row {


		public EffectModuleRow(InputState state, EffectModule f, GUIElement... elements) {
			super(state, f, elements);
		}


	}



	

}
