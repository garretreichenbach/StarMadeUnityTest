package org.schema.game.client.view.mainmenu.gui.effectconfig;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.Set;

import org.schema.game.common.data.blockeffects.config.ConfigGroup;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;

public class GUIEffectConfigGroupList extends ScrollableTableList<ConfigGroup>   {

	private GUIActiveInterface active;
	private GUIEffectStat stat;

	public GUIEffectConfigGroupList(InputState state, GUIElement p, GUIActiveInterface active, GUIEffectStat stat) {
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


		addColumn(Lng.str("Name"), 1, Comparator.comparing(o -> o.id.toLowerCase(Locale.ENGLISH)), true);
		addFixedWidthColumnScaledUI(Lng.str("Size"), 100, Comparator.comparingInt(o -> o.elements.size()));
	}

	@Override
	protected Collection<ConfigGroup> getElementList() {
		return stat.configPool.pool;
	}
	boolean first = true;
	@Override
	public void updateListEntries(GUIElementList mainList,
	                              Set<ConfigGroup> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final DateFormat dateFormatter;

		dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault());
		int i = 0;
		for (final ConfigGroup f : collection) {

			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());

			nameText.setTextSimple(new Object(){
				@Override
				public String toString(){
					return f.id;
				}
			});
			
			GUIClippedRow settingP = new GUIClippedRow(getState());
			
			settingP.activationInterface = active;
			GUIClippedRow nameP = new GUIClippedRow(getState());
			nameP.attach(nameText);

			GUITextOverlayTable setting = new GUITextOverlayTable(getState());
			setting.setTextSimple(new Object(){
				@Override
				public String toString() {
					return String.valueOf(f.elements.size());
				}
				
			});
			settingP.attach(setting);

			nameText.getPos().y = 5;

			ConfigGroupRow r = new ConfigGroupRow(getState(), f, nameP, settingP);
			
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
	protected boolean isFiltered(ConfigGroup e) {
		return super.isFiltered(e);
	}

	
	
	private class ConfigGroupRow extends Row {


		public ConfigGroupRow(InputState state, ConfigGroup f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
			this.highlightSelectSimple = true;
			this.setAllwaysOneSelected(true);
		}

		@Override
		protected void clickedOnRow() {
			stat.selectedGroup = f;
			stat.selectedElement = null;
			stat.change();
			super.clickedOnRow();
		}

	}
}