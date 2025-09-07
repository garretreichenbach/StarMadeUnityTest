package org.schema.game.client.view.gui.crew.quarters;

import api.common.GameClient;
import api.utils.game.PlayerUtils;
import org.schema.game.client.view.gui.crew.CrewPanelNew;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.element.quarters.QuarterManager;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIWindowInterface;
import org.schema.schine.input.InputState;

/**
 * [Description]
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class QuarterTab extends GUIContentPane implements GUIActiveInterface {

	private CrewQuarterGraph crewQuarterGraph;
	private QuarterScrollableList quarterScrollableNew;
	private final CrewPanelNew crewPanel;
	public QuarterManager manager;

	public QuarterTab(InputState state, GUIWindowInterface panel, CrewPanelNew crewPanel) {
		super(state, panel, Lng.str("ENTITY MANAGEMENT"));
		this.crewPanel = crewPanel;
	}

	@Override
	public void onInit() {
		super.onInit();
		if(!(PlayerUtils.getCurrentControl(GameClient.getClientPlayerState()) instanceof SegmentController)) return;
		setTextBoxHeightLast(300);
		SegmentController segmentController = (SegmentController) PlayerUtils.getCurrentControl(GameClient.getClientPlayerState());
		manager = segmentController.getQuarterManager();

//      Debug code
//		BridgeQuarter bridgeQuarter = (BridgeQuarter) Quarter.QuarterType.BRIDGE.getInstance(segmentController);
//		bridgeQuarter.getArea().min.set(segmentController.getMinPos());
//		bridgeQuarter.getArea().max.set(segmentController.getMaxPos());
//		manager.getQuartersById().put(0, bridgeQuarter);
//
//		try {
//			ManagedUsableSegmentController<?> managedUsableSegmentController = (ManagedUsableSegmentController<?>) segmentController;
//			TechnicalQuarter technicalQuarter = (TechnicalQuarter) Quarter.QuarterType.TECHNICAL.getInstance(segmentController);
//			ReactorTree reactorTree = managedUsableSegmentController.getManagerContainer().getPowerInterface().getActiveReactor();
//			int dim = reactorTree.getSize() / 3;
//			technicalQuarter.getArea().min.set(segmentController.getMinPos().x - dim, segmentController.getMinPos().y - dim, segmentController.getMinPos().z - dim);
//			technicalQuarter.getArea().max.set(segmentController.getMaxPos().x + dim, segmentController.getMaxPos().y + dim, segmentController.getMaxPos().z + dim);
//			manager.getQuartersById().put(1, technicalQuarter);
//			technicalQuarter.setPriority(1);
//		} catch(Exception ignored) { }

		crewQuarterGraph = new CrewQuarterGraph(getState(), manager);
		crewQuarterGraph.onInit();
		getContent(0).attach(crewQuarterGraph);

		addNewTextBox(300);
		quarterScrollableNew = new QuarterScrollableList(getState(), getContent(1), this);
		quarterScrollableNew.onInit();
		getContent(1).attach(quarterScrollableNew);
	}

	public void redraw() {
		crewPanel.recreateTabs();
	}
}
