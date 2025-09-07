package org.schema.game.common.controller.elements.transporter;

import org.schema.common.config.ConfigurationElement;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.BlockMetaDataDummy;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.NTReceiveInterface;
import org.schema.game.common.controller.elements.NTSenderInterface;
import org.schema.game.common.controller.elements.TagModuleUsableInterface;
import org.schema.game.common.controller.elements.UsableControllableElementManager;
import org.schema.game.common.controller.rails.RailRelation;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.objects.NetworkObject;

public class TransporterElementManager extends UsableControllableElementManager<TransporterUnit, TransporterCollectionManager, TransporterElementManager> implements
		NTSenderInterface, TagModuleUsableInterface, NTReceiveInterface {

	@ConfigurationElement(name = "PowerNeededPerGateBlock")
	public static float POWER_CONST_NEEDED_PER_BLOCK = 50;

	public final static String TAG_ID = "TR";

	protected static final long SHIELD_DOWN_TIME_MS = 5000;

	public TransporterElementManager(final SegmentController segmentController) {
		super(ElementKeyMap.TRANSPORTER_CONTROLLER, ElementKeyMap.TRANSPORTER_MODULE, segmentController);
	}

	@Override
	public void updateFromNT(NetworkObject o) {
	}
	@Override
	public String getTagId() {
		return TAG_ID;
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
	public ControllerManagerGUI getGUIUnitValues(TransporterUnit firingUnit,
	                                             TransporterCollectionManager col, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {

		return ControllerManagerGUI.create((GameClientState) getState(), Lng.str("Transporter Unit"), firingUnit);
	}

	@Override
	public boolean canHandle(ControllerStateInterface unit) {
		return false;
	}

	@Override
	protected String getTag() {
		return "transporter";
	}

	@Override
	public TransporterCollectionManager getNewCollectionManager(
			SegmentPiece position, Class<TransporterCollectionManager> clazz) {

		return new TransporterCollectionManager(position, getSegmentController(), this);
	}

	@Override
	public String getManagerName() {
		return Lng.str("Transporter System Collective");
	}


	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {

	}

	public void handleCopyUIDTranslation() {
		for (int i = 0; i < getCollectionManagers().size(); i++) {
			TransporterCollectionManager cm = getCollectionManagers().get(i);
			if(cm.getDestinationUID() != null && !cm.getDestinationUID().equals("none")){
				replaceRecursive(cm, getSegmentController().railController.getRoot());
			}
		}
	}

	private void replaceRecursive(TransporterCollectionManager cm,
			SegmentController s) {
		if(s instanceof Ship && ((Ship)s).getCopiedFromUID().equals(cm.getDestinationUID())){
			cm.setDestinationUID(s.getUniqueIdentifier());
			return;
		}
		for(RailRelation n : s.railController.next){
			replaceRecursive(cm, n.docked.getSegmentController());
		}
	}

	@Override
	public BlockMetaDataDummy getDummyInstance() {
		return new TransporterMetaDataDummy();
	}
}
