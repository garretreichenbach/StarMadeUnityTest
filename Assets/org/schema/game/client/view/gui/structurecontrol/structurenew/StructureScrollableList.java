package org.schema.game.client.view.gui.structurecontrol.structurenew;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.structurecontrol.GUIManagerEnterableList;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ManagerModule;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.input.InputState;

import java.util.List;

public class StructureScrollableList extends GUIScrollablePanel implements GUIChangeListener {

	private final GameClientState state;
	private SegmentController oldController;
	private GUIElementList list;
	private GUITextOverlay noController;
	private boolean updateNeeded;
	private long lastUpate;

	public StructureScrollableList(InputState state, GUIAnchor dependend) {
		super(10, 20, dependend, state);
		this.state = (GameClientState) state;
		noController = new GUITextOverlay(getState());
		noController.setTextSimple("You are currently not in control of any structure");

	}

	public void notifyFinishedChangingCollection(ElementCollectionManager<?, ?, ?> col) {
		if (oldController == col.getSegmentController()) {
			updateNeeded = true;
		}
	}

	public void check(SegmentController controller) {
		if (this.oldController != controller || (updateNeeded && (System.currentTimeMillis() - lastUpate) > 1000)) {
			create(controller);
			this.oldController = controller;
			updateNeeded = false;
			lastUpate = System.currentTimeMillis();
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#draw()
	 */
	@Override
	public void draw() {
		if (state.getShip() != null) {
			check(state.getShip());
		} else if (state.getCurrentPlayerObject() != null && state.getCurrentPlayerObject() instanceof SegmentController) {
			check((SegmentController) state.getCurrentPlayerObject());
		} else {
			check(null);
		}
		super.draw();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#onInit()
	 */
	@Override
	public void onInit() {
		create(null);
		super.onInit();
	}

	private void create(SegmentController controller) {
		if (controller != null) {
			this.list = new GUIElementList(state);
			list.setScrollPane(this);
			if (controller instanceof ManagedSegmentController<?>) {
				ManagedSegmentController<?> m = (ManagedSegmentController<?>) controller;
				ManagerContainer<?> managerContainer = m.getManagerContainer();
				createManager(managerContainer);
			}
			setContent(list);
		} else {
			setContent(noController);
		}
	}

	private void createManager(ManagerContainer<?> managerContainer) {
		List<ManagerModule<?, ?, ?>> modules = managerContainer.getModules();
		for (int i = 0; i < modules.size(); i++) {
			createModule(modules.get(i));
		}
	}

	private void createModule(ManagerModule<?, ?, ?> managerModule) {
		ControllerManagerGUI cGUI = managerModule.createGUI(state);
		GUIManagerEnterableList enterList = new GUIManagerEnterableList(getState(), cGUI.sub,
				cGUI.collapsedButton,
				cGUI.backButton, cGUI.backGround);
		enterList.setIndention(10);
		enterList.addObserver(this);
		enterList.getList().addObserverRecusive(this);
		//
		//		GUIEnterableList(managerModule.getGUIList());

		//		list.add(cGUI.getListEntry(state));
		list.add(new GUIListElement(enterList, enterList, state));

	}


	@Override
	public void onChange(boolean updateListDim) {
		if(updateListDim) {
			list.updateDim();
		}
	}
}