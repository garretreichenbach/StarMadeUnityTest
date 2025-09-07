package api.mod.gui.configui;

import api.mod.ModSkeleton;
import api.mod.StarLoader;
import api.mod.config.ModControlData;
import api.utils.other.LangUtil;
import com.bulletphysics.util.ObjectArrayList;
import org.schema.game.client.controller.GameMainMenuController;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Scrollable list for mod controls.
 *
 * @author TheDerpGamer
 */
public class ModControlScrollableList extends ScrollableTableList<ModControlData> {

	private final GUIElement panel;
	private ModControlEditWindow window;

	public ModControlScrollableList(InputState state, float width, float height, GUIElement panel) {
		super(state, width, height, panel);
		this.panel = panel;
	}

	@Override
	protected Collection<ModControlData> getElementList() {
		List<ModControlData> list = new ObjectArrayList<>();
		for(ModSkeleton skeleton : StarLoader.starMods) list.add(skeleton.getControls());
		return list;
	}

	@Override
	public void initColumns() {
		addColumn("Mod", 1.0f, (o1, o2) -> LangUtil.stringsCompareTo(o1.getMod().getName(), o2.getMod().getName()));
		addTextFilter(new GUIListFilterText<>() {
			public boolean isOk(String s, ModControlData modControlData) {
				return LangUtil.stringsContain(modControlData.getMod().getName(), s);
			}
		}, ControllerElement.FilterRowStyle.FULL);
		activeSortColumnIndex = 0;
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<ModControlData> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		for(ModControlData controlData : collection) {
			GUIClippedRow nameRow = getSimpleRow(controlData.getMod().getName());
			ModControlScrollableListRow listRow = new ModControlScrollableListRow(getState(), controlData, nameRow);
			GUIAnchor anchor = new GUIAnchor(getState(), panel.getWidth() - 28.0f, 24.0F) {
				@Override
				public void draw() {
					setWidth(panel.getWidth() - 28.0f);
					super.draw();
				}
			};
			anchor.attach(createButtonPane(controlData, anchor));
			listRow.expanded = new GUIElementList(getState());
			listRow.expanded.add(new GUIListElement(anchor, getState()));
			listRow.expanded.attach(anchor);
			listRow.onInit();
			mainList.addWithoutUpdate(listRow);
		}
		mainList.updateDim();
	}

	private GUIHorizontalButtonTablePane createButtonPane(ModControlData controlData, GUIAnchor anchor) {
		GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 1, 1, anchor);
		buttonPane.onInit();
		buttonPane.addButton(0, 0, Lng.str("EDIT"), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					(window = new ModControlEditWindow((GameMainMenuController) getState(), controlData)).activate();
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

	public class ModControlScrollableListRow extends ScrollableTableList<ModControlData>.Row {
		public ModControlScrollableListRow(InputState state, ModControlData controlData, GUIElement... elements) {
			super(state, controlData, elements);
			highlightSelect = true;
			highlightSelectSimple = true;
			setAllwaysOneSelected(true);
		}
	}
}
