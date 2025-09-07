package org.schema.game.client.view.mainmenu.gui.effectconfig;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import org.schema.game.common.data.blockeffects.config.ConfigEntityManager;
import org.schema.game.common.data.blockeffects.config.ConfigGroup;
import org.schema.game.common.data.blockeffects.config.ConfigManagerInterface;
import org.schema.game.common.data.blockeffects.config.EffectConfigNetworkObjectInterface;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICheckBox;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.network.objects.Sendable;

public class GUIEffectConfigGroupTestList extends ScrollableTableList<ConfigGroup>   {

	private GUIActiveInterface active;
	private ConfigEntityManager man;
	private GUIEffectStat stat;
	private ConfigManagerInterface specifiedMan;

	public GUIEffectConfigGroupTestList(InputState state, GUIElement p, GUIActiveInterface active, GUIEffectStat stat, ConfigEntityManager man, ConfigManagerInterface specifiedMan) {
		super(state, 100, 100, p);
		this.active = active;
		this.stat = stat;
		this.specifiedMan = specifiedMan;
		this.man = man;
		man.addObserver(this);
		stat.addObserver(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		man.deleteObserver(this);
		stat.deleteObserver(this);
		super.cleanUp();
	}

	@Override
	public void initColumns() {


		addFixedWidthColumnScaledUI(Lng.str("Enable"), 50, (o1, o2) -> o1.id.toLowerCase(Locale.ENGLISH).compareTo(o2.id.toLowerCase(Locale.ENGLISH)), true);
		addColumn(Lng.str("Name"), 1, (o1, o2) -> o1.id.toLowerCase(Locale.ENGLISH).compareTo(o2.id.toLowerCase(Locale.ENGLISH)), true);
		addFixedWidthColumnScaledUI(Lng.str("Size"), 40, (o1, o2) -> (o1.elements.size() - o2.elements.size()));
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

			
			GUIAnchor con = new GUIAnchor(getState(), UIScale.getUIScale().h, UIScale.getUIScale().h);
			GUICheckBox on = new GUICheckBox(getState()) {
				
				@Override
				protected boolean isActivated() {
					return man.isActive(f);
				}
				
				@Override
				protected void deactivate() throws StateParameterNotFoundException {
					if(stat.testManager == man){
						man.removeEffect(f.ntId, true);
					}else{
						Sendable s = ((Sendable)specifiedMan);
						man.removeEffectAndSend(f, true, (EffectConfigNetworkObjectInterface) s.getNetworkObject());			
					}
				}
				
				@Override
				protected void activate() throws StateParameterNotFoundException {
					if(stat.testManager == man || specifiedMan == null){
						man.addEffect(f.ntId, true);					
					}else{
						Sendable s = ((Sendable)specifiedMan);
						
						man.addEffectAndSend(f, true, (EffectConfigNetworkObjectInterface) s.getNetworkObject());
					}
				}
			};
			con.attach(on);
			on.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
			
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());

			nameText.setTextSimple(f.id);
			
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

			nameText.getPos().y = UIScale.getUIScale().scale(5);

			ConfigGroupRow r = new ConfigGroupRow(getState(), f, on, nameP, settingP);
			
			GUIAnchor c = new GUIAnchor(getState(), UIScale.getUIScale().scale(100), UIScale.getUIScale().scale(100));


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