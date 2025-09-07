package org.schema.game.common.controller.elements.activationgate;

import org.schema.common.config.ConfigurationElement;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.NTReceiveInterface;
import org.schema.game.common.controller.elements.NTSenderInterface;
import org.schema.game.common.controller.elements.UsableControllableElementManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.objects.NetworkObject;

public class ActivationGateElementManager extends UsableControllableElementManager<ActivationGateUnit, ActivationGateCollectionManager, ActivationGateElementManager> implements
		NTSenderInterface, NTReceiveInterface {

	@ConfigurationElement(name = "PowerNeededPerGateBlock")
	public static float POWER_CONST_NEEDED_PER_BLOCK = 50;

	@ConfigurationElement(name = "PowerNeededPerMass")
	public static float POWER_NEEDED_PER_MASS = 50;


	public ActivationGateElementManager(final SegmentController segmentController) {
		super(ElementKeyMap.ACTIVATION_GATE_CONTROLLER, ElementKeyMap.ACTIVATION_GATE_MODULE, segmentController);
	}

	@Override
	public void updateFromNT(NetworkObject o) {
	}

	@Override
	public void updateToFullNT(NetworkObject networkObject) {
		if (getSegmentController().isOnServer()) {
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.missile.MissileElementManager#getGUIUnitValues(org.schema.game.common.controller.elements.missile.MissileUnit, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager)
	 */
	@Override
	public ControllerManagerGUI getGUIUnitValues(ActivationGateUnit firingUnit,
	                                             ActivationGateCollectionManager col, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {

		return ControllerManagerGUI.create((GameClientState) getState(), Lng.str("Activation Gate Unit"), firingUnit);
	}

	@Override
	public boolean canHandle(ControllerStateInterface unit) {
		return false;
	}

	@Override
	protected String getTag() {
		return "activationgate";
	}

	@Override
	public ActivationGateCollectionManager getNewCollectionManager(
			SegmentPiece position, Class<ActivationGateCollectionManager> clazz) {

		return new ActivationGateCollectionManager(position, getSegmentController(), this);
	}

	@Override
	public String getManagerName() {
		return Lng.str("Activation Gate System Collective");
	}


	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {

	}
}
