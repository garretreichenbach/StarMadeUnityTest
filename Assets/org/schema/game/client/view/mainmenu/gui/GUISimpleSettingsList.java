package org.schema.game.client.view.mainmenu.gui;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.settings.SettingStateBoolean;
import org.schema.common.util.settings.SettingStateEnum;
import org.schema.common.util.settings.SettingStateString;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.core.settings.EngineSettingsChangeListener;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.SettingsInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActivatableTextBar;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;

import java.text.DateFormat;
import java.util.*;

public class GUISimpleSettingsList extends ScrollableTableList<SimpleSettings> implements EngineSettingsChangeListener {

	boolean first = true;
	private GUIActiveInterface active;
	private GUIActivatableTextBar playerNameBar;

	public GUISimpleSettingsList(InputState state, GUIElement p, GUIActiveInterface active) {
		super(state, 100, 100, p);
		this.active = active;
		EngineSettings.OFFLINE_PLAYER_NAME.addChangeListener(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
//		messageController.deleteObserver(this);
		super.cleanUp();
		EngineSettings.OFFLINE_PLAYER_NAME.removeChangeListener(this);
	}

	@Override
	public void initColumns() {


		addFixedWidthColumnScaledUI("#", 0, (o1, o2) -> (o1.ordinal() - o2.ordinal()), true);
		addColumn(Lng.str("Setting"), 4f, (o1, o2) -> (o1.name().toLowerCase(Locale.ENGLISH)).compareTo(o2.name().toLowerCase(Locale.ENGLISH)));
		addColumn(Lng.str("Value"), 3f, (o1, o2) -> (o1.getString().toLowerCase(Locale.ENGLISH)).compareTo(o2.getString().toLowerCase(Locale.ENGLISH)));

	}

	@Override
	protected Collection<SimpleSettings> getElementList() {
		List<SimpleSettings> c = new ObjectArrayList<SimpleSettings>();
		for(SimpleSettings s : SimpleSettings.values()) {
			c.add(s);
		}
		Collections.sort(c, (o1, o2) -> o1.ordinal() - o2.ordinal());
		return c;
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<SimpleSettings> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final DateFormat dateFormatter;
		dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault());
		int i = 0;
		for(SimpleSettings f : collection) {

			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());

			nameText.setTextSimple(f.getDescription());

			GUIClippedRow settingP = new GUIClippedRow(getState());

			GUIAnchor indexSpoofer = new GUIAnchor(getState(), 0, 0);
			settingP.activationInterface = active;
			GUIElement setting = f.getGUIElement(getState(), settingP, f.getEmptyFieldText());

			if(f == SimpleSettings.PLAYER_NAME) {
				playerNameBar = (GUIActivatableTextBar) setting;
			}
			f.addChangeListener(this);

			GUIClippedRow nameP = new GUIClippedRow(getState());
			nameP.attach(nameText);


			settingP.attach(setting);

			nameText.getPos().y = 5;

			SimpleSettingsRow r = new SimpleSettingsRow(getState(), f, indexSpoofer, nameP, settingP);

			GUIAnchor c = new GUIAnchor(getState(), 100, 100);


			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
		first = false;
	}

	@Override
	public void onSettingChanged(SettingsInterface setting) {
		if(!(setting instanceof SimpleSettings)) return;
		if(setting == EngineSettings.OFFLINE_PLAYER_NAME && playerNameBar != null) {
			playerNameBar.setText(EngineSettings.OFFLINE_PLAYER_NAME.getString().trim());
			EngineSettings.OFFLINE_PLAYER_NAME.setString(playerNameBar.getText());
		}
		switch((SimpleSettings) setting) {
			case PLAYER_NAME:
				SimpleSettings.PLAYER_NAME.setString(((SettingStateString) setting).getString());
				break;
			case TUTORIAL:
				SimpleSettings.TUTORIAL.setOn(((SettingStateBoolean) setting).isOn());
				break;
			case DIFFICULTY:
				SimpleSettings.DIFFICULTY.setInt(((SettingStateEnum) setting).getInt());
				break;
			case CREATIVE_MODE:
				SimpleSettings.CREATIVE_MODE.setOn(((SettingStateBoolean) setting).isOn());
				break;
			default:
				break;
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#isFiltered(java.lang.Object)
	 */
	@Override
	protected boolean isFiltered(SimpleSettings e) {
		return super.isFiltered(e);
	}

	private class SimpleSettingsRow extends Row {


		public SimpleSettingsRow(InputState state, SimpleSettings f, GUIElement... elements) {
			super(state, f, elements);
		}
	}
}
