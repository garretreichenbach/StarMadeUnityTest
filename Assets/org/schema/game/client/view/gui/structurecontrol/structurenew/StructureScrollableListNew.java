package org.schema.game.client.view.gui.structurecontrol.structurenew;

import api.common.GameClient;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.elements.*;
import org.schema.game.common.controller.elements.power.reactor.MainReactorElementManager;
import org.schema.game.common.controller.elements.power.reactor.PowerConsumer;
import org.schema.game.common.controller.elements.rail.inv.RailConnectionElementManager;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;

public class StructureScrollableListNew extends ScrollableTableList<ManagerModule<?, ?, ?>> {

	private final ManagerContainer<?> container;
	private long lastControllerCheck;

	public StructureScrollableListNew(InputState state, float width, float height, GUIElement p, ManagerContainer<?> container) {
		super(state, width, height, p);
		this.container = container;
	}

	@Override
	protected Collection<ManagerModule<?, ?, ?>> getElementList() {
		return container.getModules();
	}

	@Override
	public void initColumns() {
		addColumn(Lng.str("Name"), 10.0f, (o1, o2) -> {
			String o1Name = o1.getElementManager().getManagerName();
			String o2Name = o2.getElementManager().getManagerName();
			return o1Name.compareTo(o2Name);
		});

		addColumn(Lng.str("Category"), 3.0f, (o1, o2) -> {
			String o1Name = Lng.str("Others");
			String o2Name = Lng.str("Others");
			if(o1.getElementManager() instanceof PowerConsumer powerConsumer) o1Name = powerConsumer.getPowerConsumerCategory().getName();
			else if(o1.getElementManager() instanceof MainReactorElementManager) o1Name = Lng.str("REACTOR");
			if(o2.getElementManager() instanceof PowerConsumer powerConsumer) o2Name = powerConsumer.getPowerConsumerCategory().getName();
			else if(o2.getElementManager() instanceof MainReactorElementManager) o2Name = Lng.str("REACTOR");
			return o1Name.compareTo(o2Name);
		});

		addColumn(Lng.str("Total Size"), 5.0f, (o1, o2) -> {
			int size1 = o1.getElementManager().totalSize;
			int size2 = o2.getElementManager().totalSize;
			return Integer.compare(size1, size2);
		});

		addTextFilter(new GUIListFilterText<>() {
			@Override
			public boolean isOk(String input, ManagerModule<?, ?, ?> listElement) {
				return listElement.getElementManager().getManagerName().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, ControllerElement.FilterRowStyle.LEFT);

		addDropdownFilter(new GUIListFilterDropdown<>(getCategories()) {

			@Override
			public boolean isOk(String input, ManagerModule<?, ?, ?> module) {
				if(input.equals(Lng.str("ALL"))) return true;
				if(module.getElementManager() instanceof PowerConsumer powerConsumer) return powerConsumer.getPowerConsumerCategory().name().toUpperCase(Locale.ENGLISH).equals(input);
				if(module.getElementManager() instanceof MainReactorElementManager) return Lng.str("REACTOR").equals(input);
				return input.equals(Lng.str("OTHERS"));
			}
		}, new CreateGUIElementInterface<>() {

			@Override
			public GUIElement create(String input) {
				GUIAnchor c = new GUIAnchor(getState(), 10, 20);
				GUITextOverlayTableDropDown a = new GUITextOverlayTableDropDown(10, 10, getState());
				a.setTextSimple(input);
				a.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
				c.setUserPointer(input);
				c.attach(a);
				return c;
			}

			@Override
			public GUIElement createNeutral() {
				return null;
			}
		}, ControllerElement.FilterRowStyle.RIGHT);

		activeSortColumnIndex = 1;
	}

	private String[] getCategories() {
		ArrayList<String> categories = new ArrayList<>();
		categories.add(Lng.str("ALL"));
		categories.add(Lng.str("REACTOR"));
		for(PowerConsumer.PowerConsumerCategory category : PowerConsumer.PowerConsumerCategory.values()) categories.add(category.getName().toUpperCase(Locale.ENGLISH));
		return categories.toArray(new String[0]);
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<ManagerModule<?, ?, ?>> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		for(ManagerModule<?, ?, ?> module : collection) {
			if(module.getElementManager().totalSize == 0 && !(module instanceof RailManagerInterface) && !(module.getElementManager() instanceof RailConnectionElementManager)) continue;
			GUIClippedRow nameRow = getSimpleRow(module.getElementManager().getManagerName());
			GUIClippedRow categoryRow = getSimpleRow(Lng.str("OTHER"));
			if(module.getElementManager() instanceof PowerConsumer powerConsumer) categoryRow = getSimpleRow(powerConsumer.getPowerConsumerCategory().getName().toUpperCase(Locale.ENGLISH));
			if(module.getElementManager() instanceof MainReactorElementManager reactor) categoryRow = getSimpleRow(Lng.str("REACTOR"));
			GUIClippedRow sizeRow = getSimpleRow(getSizeText(module));

			StructureScrollableListNewRow row = new StructureScrollableListNewRow(getState(), module, nameRow, categoryRow, sizeRow);
			createExpanded(row, module);
			row.onInit();
			mainList.addWithoutUpdate(row);
		}
		mainList.updateDim();
	}

	private String getSizeText(ManagerModule<?, ?, ?> module) {
		if(module.getElementManager() instanceof UsableElementManager<?, ?, ?> usableElementManager) {
			switch(usableElementManager) {
				case RailConnectionElementManager railConnectionElementManager -> {
					return Lng.str("%s entities", railConnectionElementManager.getCollectionManagers().size());
				}
				case UsableControllableSingleElementManager<?, ?, ?> singleElementManager -> {
					return Lng.str("%s blocks in %s groups", module.getElementManager().totalSize, singleElementManager.getCollection().getElementCollections().size());
				}
				case UsableControllableElementManager<?, ?, ?> multiElementManager -> {
					return Lng.str("%s blocks in %s groups", module.getElementManager().totalSize, multiElementManager.getCollectionManagers().size());
				}
				default -> {
				}
			}
		}
		return Lng.str("%s blocks", module.getElementManager().totalSize);
	}

	private void createExpanded(StructureScrollableListNewRow row, ManagerModule<?, ?, ?> module) {
		GameClientState state = (GameClientState) getState();
		GUIElementList list = new GUIElementList(state);
		ControllerManagerGUI createGUI = module.createGUI(state);
		GUIListElement listEntry = createGUI.getListEntry(state, list);
		list.addWithoutUpdate(listEntry);
		list.updateDim();
		row.expanded = list;
	}

	@Override
	public void draw() {
		if(doControllerCheck()) super.draw();
	}

	private boolean doControllerCheck() {
		if(System.currentTimeMillis() - lastControllerCheck > 1000) {
			lastControllerCheck = System.currentTimeMillis();
			SimpleTransformableSendableObject<?> controller = getCurrentController();
			return controller == container.getSegmentController();
		}
		return true;
	}

	private SimpleTransformableSendableObject<?> getCurrentController() {
		return GameClient.getCurrentControl();
	}

	public void notifyFinishedChangingCollection(ElementCollectionManager<?, ?, ?> col) {
		flagDirty();
		handleDirty();
	}

	private class StructureScrollableListNewRow extends ScrollableTableList<ManagerModule<?, ?, ?>>.Row {

		public StructureScrollableListNewRow(InputState state, ManagerModule<?, ?, ?> module, GUIElement... elements) {
			super(state, module, elements);
			highlightSelect = true;
			highlightSelectSimple = true;
		}
	}
}
