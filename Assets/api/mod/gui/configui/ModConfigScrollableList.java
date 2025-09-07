package api.mod.gui.configui;

import api.mod.ModSkeleton;
import api.mod.StarLoader;
import api.mod.config.ModConfigData;
import api.utils.other.LangUtil;
import com.bulletphysics.util.ObjectArrayList;
import org.schema.game.client.controller.GameMainMenuController;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class ModConfigScrollableList extends ScrollableTableList<ModConfigData> {

	private final GUIElement panel;
	private ModConfigEditWindow window;

	public ModConfigScrollableList(InputState state, float width, float height, GUIElement panel) {
		super(state, width, height, panel);
		this.panel = panel;
	}

	@Override
	protected Collection<ModConfigData> getElementList() {
		List<ModConfigData> list = new ObjectArrayList<>();
		for(ModSkeleton skeleton : StarLoader.starMods) list.addAll(skeleton.getConfigs().values());
		return list;
	}

	@Override
	public void initColumns() {
		addColumn("Name", 1.0f, (o1, o2) -> LangUtil.stringsCompareTo(o1.getName(), o2.getName()));
		addColumn("Mod", 1.0f, (o1, o2) -> LangUtil.stringsCompareTo(o1.getMod().getName(), o2.getMod().getName()));
		addTextFilter(new GUIListFilterText<>() {
			public boolean isOk(String s, ModConfigData configData) {
				return LangUtil.stringsContain(configData.getMod().getName(), s);
			}
		}, ControllerElement.FilterRowStyle.LEFT);
		addDropdownFilter(new GUIListFilterDropdown<>(getModNames()) {
			@Override
			public boolean isOk(String input, ModConfigData data) {
				return LangUtil.stringsContain(data.getMod().getName(), input);
			}
		}, new CreateGUIElementInterface<>() {
			@Override
			public GUIElement create(String o) {
				GUIAnchor c = new GUIAnchor(getState(), 10, UIScale.getUIScale().h);
				GUITextOverlayTableDropDown a = new GUITextOverlayTableDropDown(10, 10, getState());
				a.setTextSimple(o);
				a.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
				c.setUserPointer(o);
				c.attach(a);
				return c;
			}

			@Override
			public GUIElement createNeutral() {
				GUIAnchor c = new GUIAnchor(getState(), 10, UIScale.getUIScale().h);
				GUITextOverlayTableDropDown a = new GUITextOverlayTableDropDown(10, 10, getState());
				a.setTextSimple(Lng.str("ALL"));
				a.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
				c.attach(a);
				return c;
			}
		}, ControllerElement.FilterRowStyle.RIGHT);
		activeSortColumnIndex = 0;
	}

	private String[] getModNames() {
		ArrayList<String> names = new ArrayList<>();
		for(ModSkeleton skeleton : StarLoader.starMods) names.add(skeleton.getName());
		return names.toArray(new String[0]);
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<ModConfigData> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		int count = 0;
		for(ModSkeleton skeleton : StarLoader.starMods) {
			ScrollableTableList<ModConfigData>.Seperator seperator = getSeperator(skeleton.getName(), count);
			count++;
			for(ModConfigData configData : skeleton.getConfigs().values()) {
				GUIClippedRow nameRow = getSimpleRow(configData.getMod().getName());
				ModConfigScrollableListRow listRow = new ModConfigScrollableListRow(getState(), configData, nameRow);
				GUIAnchor anchor = new GUIAnchor(getState(), panel.getWidth() - 28.0f, 28.0F) {
					@Override
					public void draw() {
						setWidth(panel.getWidth() - 28.0f);
						super.draw();
					}
				};

				anchor.attach(createButtonPane(configData, anchor));
				listRow.expanded = new GUIElementList(getState());
				listRow.expanded.add(new GUIListElement(anchor, getState()));
				listRow.expanded.attach(anchor);
				listRow.seperator = seperator;
				listRow.onInit();
				mainList.addWithoutUpdate(listRow);
			}
		}
		mainList.updateDim();
	}

	private GUIHorizontalButtonTablePane createButtonPane(ModConfigData configData, GUIAnchor anchor) {
		GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 1, 1, anchor);
		buttonPane.onInit();
		buttonPane.addButton(0, 0, Lng.str("EDIT"), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					(window = new ModConfigEditWindow((GameMainMenuController) getState(), configData)).activate();
				}
			}

			@Override
			public boolean isOccluded() {
				return window != null && window.isActive();
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return window == null || !window.isActive();
			}
		});
		return buttonPane;
	}

	public class ModConfigScrollableListRow extends ScrollableTableList<ModConfigData>.Row {
		public ModConfigScrollableListRow(InputState state, ModConfigData configData, GUIElement... elements) {
			super(state, configData, elements);
			highlightSelect = true;
			highlightSelectSimple = true;
			setAllwaysOneSelected(true);
		}
	}
}
