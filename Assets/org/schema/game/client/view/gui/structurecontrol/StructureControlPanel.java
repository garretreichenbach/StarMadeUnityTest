package org.schema.game.client.view.gui.structurecontrol;

import api.common.GameClient;
import org.schema.game.client.data.CollectionManagerChangeListener;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.structurenew.StructureScrollableListNew;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.input.InputState;

public class StructureControlPanel extends GUIAnchor implements GUIChangeListener, CollectionManagerChangeListener {

	private final GameClientState state;
	private final GUIOverlay background;
	private GUIScrollablePanel scrollPanel;
	private SegmentController oldController;
	private StructureScrollableListNew list;
	private GUITextOverlay noController;
	private boolean updateNeeded;
	private long lastUpate;

	public StructureControlPanel(InputState state) {
		super(state);
		this.state = (GameClientState) state;
		background = new GUIOverlay(Controller.getResLoader().getSprite("structure-panel-gui-"), state);
		noController = new GUITextOverlay(getState());
		noController.setTextSimple("You are currently not in control of any structure");

		height = background.getHeight();
		width = background.getWidth();

		((GameClientState) getState()).getController().addCollectionManagerChangeListener(this);
	}

	@Override
	public void cleanUp() {
		super.cleanUp();
		((GameClientState) getState()).getController().removeCollectionManagerChangeListener(this);
	}

	public void check(SegmentController controller) {
		if(oldController != controller || (updateNeeded && (System.currentTimeMillis() - lastUpate) > 1000)) {
			create(controller);
			oldController = controller;
			updateNeeded = false;
			lastUpate = System.currentTimeMillis();
		}
	}
	
	@Override
	public void draw() {
		if(state.getShip() != null) {
			check(state.getShip());
		} else if(state.getCurrentPlayerObject() != null && state.getCurrentPlayerObject() instanceof SegmentController) {
			check((SegmentController) state.getCurrentPlayerObject());
		} else {
			check(null);
		}
		super.draw();
	}
	
	@Override
	public void onInit() {
		super.onInit();
		scrollPanel = new GUIScrollablePanel(533, 315, state);
		create(null);

		scrollPanel.onInit();
		scrollPanel.getPos().set(252, 107, 0);

		background.onInit();
		background.attach(scrollPanel);

		attach(background);
	}

	private void create(SegmentController controller) {
		if(controller != null) {
			if(controller instanceof ManagedSegmentController<?>) {
				SimpleTransformableSendableObject<?> currentPlayerObject = GameClient.getClientState().getCurrentPlayerObject();
				if(currentPlayerObject instanceof ManagedSegmentController<?> managedSegmentController) {
					list = new StructureScrollableListNew(getState(), 10, 10, scrollPanel, managedSegmentController.getManagerContainer());
					list.onInit();
					scrollPanel.setContent(list);
					list.addObserver(this);
				}
			}
		} else {
			scrollPanel.setContent(noController);
		}
	}

	@Override
	public void onChange(ElementCollectionManager<?, ?, ?> col) {
		if(oldController == col.getSegmentController()) updateNeeded = true;
	}

	@Override
	public void onChange(boolean updateListDim) {
		if(updateListDim && list != null) {
			list.flagDirty();
			list.handleDirty();
		}
	}
}
