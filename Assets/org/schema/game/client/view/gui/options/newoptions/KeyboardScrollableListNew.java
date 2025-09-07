/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.schema.game.client.view.gui.options.newoptions;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.schema.game.client.view.gui.options.GUIKeyboardDialog;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIToolTip;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.CreateGUIElementInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButton;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterDropdown;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTableDropDown;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.graphicsengine.forms.gui.newgui.settingsnew.GUISettingsElementPanel;
import org.schema.schine.input.InputAction;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyboardContext;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * @author brent
 */
public class KeyboardScrollableListNew extends ScrollableTableList<KBMappingContainer> {

	private GUIActiveInterface a;

	public KeyboardScrollableListNew(InputState state, GUIActiveInterface a, GUIElement p) {
		super(state, 100, 100, p);
		this.a = a;
	}

	@Override
	public void initColumns() {
		addColumn(Lng.str("Name"), 4, (o1, o2) -> o1.m.getDescription().compareToIgnoreCase(o2.m.getDescription()));
		addColumn(Lng.str("Setting"), 1, (o1, o2) -> 0);
		addTextFilter(new GUIListFilterText<KBMappingContainer>() {

			@Override
			public boolean isOk(String input, KBMappingContainer listElement) {
				return listElement.m.getDescription().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH BY NAME"), FilterRowStyle.LEFT);
		addDropdownFilter(new GUIListFilterDropdown<KBMappingContainer, KeyboardContext>(KeyboardContext.values()) {

			@Override
			public boolean isOk(KeyboardContext input, KBMappingContainer listElement) {
				return listElement.m.getContext() == input;
			}
		}, new CreateGUIElementInterface<KeyboardContext>() {

			@Override
			public GUIElement create(KeyboardContext o) {
				GUIAnchor c = new GUIAnchor(getState(), 10, 20);
				GUITextOverlayTableDropDown a = new GUITextOverlayTableDropDown(10, 10, getState());
				a.setTextSimple(o.name());
				a.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
				c.setUserPointer(o);
				c.attach(a);
				return c;
			}

			@Override
			public GUIElement createNeutral() {
				GUIAnchor c = new GUIAnchor(getState(), 10, 20);
				GUITextOverlayTableDropDown a = new GUITextOverlayTableDropDown(10, 10, getState());
				a.setTextSimple(Lng.str("Filter By Type (off)"));
				a.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
				c.attach(a);
				return c;
			}
		}, FilterRowStyle.RIGHT);
	}

	/**
	 * use a static container representation to mark the enum "changed" manually when an input action was added/removed
	 */
	private static Object2ObjectOpenHashMap<KeyboardMappings, KBMappingContainer> gmap = new Object2ObjectOpenHashMap<>();

	static {
		for (KeyboardMappings m : KeyboardMappings.values()) {
			setChangedSetting(m);
		}
	}

	public static void setChangedSetting(KeyboardMappings m) {
		gmap.put(m, new KBMappingContainer(m));
	}

	@Override
	protected Collection<KBMappingContainer> getElementList() {
		return gmap.values();
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<KBMappingContainer> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		int cnt = 0;
		for (final KeyboardContext c : KeyboardContext.values()) {
			final ScrollableTableList<KBMappingContainer>.Seperator cv;
			cv = getSeperator(c.getDesc(), cnt);
			cnt++;
			for (final KBMappingContainer kg : collection) {
				KeyboardMappings k = kg.m;
				if (k.getContext().equals(c)) {
					GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
					List<GUIHorizontalButton> btns = new ObjectArrayList<>();
					System.err.println("CREATING " + k + " -> " + k.getMappings());
					for (InputAction action : k.getMappings()) {
						GUIHorizontalButton settingBtn = getInputButton(k, action);
						btns.add(settingBtn);
					}
					boolean nothingAssigned = btns.isEmpty();
					// if(nothingAssigned) {
					// nothing assigned
					btns.add(0, getAssignButton(k, false));
					// }
					GUIAnchor elements = new GUIAnchor(getState(), 10, UIScale.getUIScale().h * btns.size()) {

						@Override
						public void draw() {
							setWidth(columns.get(1).bg.getWidth() - 25);
							super.draw();
						}
					};
					for (int y = 0; y < btns.size(); y++) {
						GUIHorizontalButton btn = btns.get(y);
						btn.setPos(0, y * UIScale.getUIScale().h, 0);
						elements.attach(btn);
					}
					GUISettingsElementPanel set = new GUISettingsElementPanel(getState(), elements, false, false);
					nameText.setTextSimple(k.getDescription());
					int heightInset = 3;
					nameText.getPos().y = heightInset;
					SettingRow r = new SettingRow(getState(), kg, nameText, set);
					r.rowHeight = (int) elements.getHeight();
					r.seperator = cv;
					r.onInit();
					mainList.addWithoutUpdate(r);
				}
			}
			mainList.updateDim();
		}
	}

	private GUIHorizontalButton getInputButton(KeyboardMappings k, InputAction action) {
		GUIHorizontalButton settingBtn = new GUIHorizontalButton(getState(), HButtonType.BUTTON_RED_MEDIUM, new Object() {

			@Override
			public String toString() {
				return action.getName();
			}
		}, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(601);
					GUIKeyboardDialog dialog = new GUIKeyboardDialog(getState(), k, KeyboardScrollableListNew.this);
					dialog.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(602);
				} else if (event.pressedRightMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.DELETE)*/
					AudioController.fireAudioEventID(600);
					boolean removeMapping = k.removeMapping(action);
					assert (removeMapping);
					setChangedSetting(k);
					flagDirty();
				}
			}

			@Override
			public boolean isOccluded() {
				return !a.isActive();
			}
		}, a, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return a.isActive();
			}
		}) {

			@Override
			public void draw() {
				setWidth(columns.get(1).bg.getWidth() - 25);
				HButtonType.getType(HButtonType.TEXT_FILED_LIGHT, isInside(), isActive(), false);
				setMouseUpdateEnabled(isActive());
				super.draw();
			}
		};
		settingBtn.onInit();
		settingBtn.setFont(FontSize.BIG_20);
		settingBtn.setToolTip(new GUIToolTip(getState(), Lng.str("Right Click to remove"), settingBtn));
		return settingBtn;
	}

	private GUIHorizontalButton getRemoveButton(KeyboardMappings k, InputAction action) {
		GUIHorizontalButton settingBtn = new GUIHorizontalButton(getState(), HButtonType.BUTTON_RED_MEDIUM, new Object() {

			@Override
			public String toString() {
				return Lng.str("<+>");
			}
		}, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.DELETE)*/
					AudioController.fireAudioEventID(603);
					k.removeMapping(action);
				}
			}

			@Override
			public boolean isOccluded() {
				return !a.isActive();
			}
		}, a, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return a.isActive();
			}
		}) {

			@Override
			public void draw() {
				setWidth(22);
				HButtonType.getType(HButtonType.TEXT_FILED_LIGHT, isInside(), isActive(), false);
				setMouseUpdateEnabled(isActive());
				super.draw();
			}
		};
		settingBtn.onInit();
		settingBtn.setFont(FontSize.BIG_20);
		return settingBtn;
	}

	private GUIHorizontalButton getAssignButton(KeyboardMappings k, boolean small) {
		GUIHorizontalButton settingBtn = new GUIHorizontalButton(getState(), HButtonType.BUTTON_BLUE_MEDIUM, new Object() {

			@Override
			public String toString() {
				return Lng.str("<Assign>");
			}
		}, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					GUIKeyboardDialog dialog = new GUIKeyboardDialog(getState(), k, KeyboardScrollableListNew.this);
					dialog.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(604);
				}
			}

			@Override
			public boolean isOccluded() {
				return !a.isActive();
			}
		}, a, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return a.isActive();
			}
		}) {

			@Override
			public void draw() {
				if (small) {
					setWidth(22);
				} else {
					setWidth(columns.get(1).bg.getWidth() - 25);
				}
				HButtonType.getType(HButtonType.TEXT_FILED_LIGHT, isInside(), isActive(), false);
				setMouseUpdateEnabled(isActive());
				super.draw();
			}
		};
		settingBtn.onInit();
		settingBtn.setFont(FontSize.BIG_20);
		return settingBtn;
	}

	private class SettingRow extends Row {

		public int rowHeight;

		public SettingRow(InputState state, KBMappingContainer k, GUIElement... elements) {
			super(state, k, elements);
			this.highlightSelect = true;
		}

		@Override
		protected int getColumnsHeight() {
			return rowHeight;
		}
	}
}
