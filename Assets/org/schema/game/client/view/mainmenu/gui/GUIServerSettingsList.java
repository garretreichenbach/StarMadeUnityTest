package org.schema.game.client.view.mainmenu.gui;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.Set;

import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.ServerConfig.ServerConfigCategory;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIToolTip;
import org.schema.schine.graphicsengine.forms.gui.TooltipProviderCallback;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GUIServerSettingsList extends ScrollableTableList<ServerConfig>  {

	private ServerConfigCategory gameSettingCat;
	private GUIActiveInterface active;

	public GUIServerSettingsList(InputState state, GUIElement p, ServerConfigCategory gameSetting, GUIActiveInterface active) {
		super(state, 100, 100, p);
		this.active = active;
		this.gameSettingCat = gameSetting;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
//		messageController.deleteObserver(this);
		super.cleanUp();

	}

	@Override
	public void initColumns() {


		addColumn(Lng.str("Name"), 4f, Comparator.comparing(o -> (o.getName().toLowerCase(Locale.ENGLISH))));
		addColumn(Lng.str("Setting"), 3f, Comparator.comparing(o -> (o.getAsString().toLowerCase(Locale.ENGLISH))));


		addTextFilter(new GUIListFilterText<ServerConfig>() {

			@Override
			public boolean isOk(String input, ServerConfig listElement) {
				return listElement.getDescription().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH BY NAME"), FilterRowStyle.FULL);
		
	}

	@Override
	protected Collection<ServerConfig> getElementList() {
		Collection<ServerConfig> c = new ObjectArrayList<ServerConfig>();
		for(ServerConfig s : ServerConfig.values()){
			try {
				if(s.getCategory() == gameSettingCat) {
					c.add(s);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return c;
	}
	boolean first = true;
	@Override
	public void updateListEntries(GUIElementList mainList,
	                              Set<ServerConfig> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final DateFormat dateFormatter;

		dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault());
		int i = 0;
		for (final ServerConfig f : collection) {

			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());

			//nameText.setTextSimple(f.name().substring(0, 1) + f.name().substring(1, f.name().length()).replaceAll("_", " ").toLowerCase(Locale.ENGLISH));
			nameText.setTextSimple(f.getName());

			GUIClippedRow settingP = new GUIClippedRow(getState());
			settingP.activationInterface = active;

			GUIElement setting = f.getGUIElement(getState(), settingP, Lng.str("ENTER SETTING"));

			GUIClippedRow nameP = new GUIClippedRow(getState());
			nameP.attach(nameText);


			settingP.attach(setting);

			nameText.getPos().y = 5;

			ServerConfigRow r = new ServerConfigRow(getState(), f, nameP, settingP);
			r.setToolTip(new GUIToolTip(getState(), f.getDescription(), r));

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
	protected boolean isFiltered(ServerConfig e) {
		return super.isFiltered(e);
	}

	
	private class ServerConfigRow extends Row implements TooltipProviderCallback {
		
		private GUIToolTip toolTip;
		public ServerConfigRow(InputState state, ServerConfig f, GUIElement... elements) {
			super(state, f, elements);
		}

		@Override
		public GUIToolTip getToolTip() {
			return toolTip;
		}

		@Override
		public void setToolTip(GUIToolTip toolTip) {
			this.toolTip = toolTip;
		}

	}



	

}
