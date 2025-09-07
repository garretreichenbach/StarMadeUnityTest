package org.schema.game.common.controller.elements.activation;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.SignalTrace;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerMessage;

import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;

public class AbstractUnit extends ElementCollection<AbstractUnit, ActivationCollectionManager, ActivationElementManager> {

	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return elementCollectionManager.getElementManager().getGUIUnitValues(this, elementCollectionManager, supportCol, effectCol);
	}

	public void onActivate(ActivationCollectionManager currentCollection, ActivationElementManager man, SegmentPiece piece, boolean active) {

		for (long index : getNeighboringCollection()) {
			long d;
			boolean delegate = false;
			if (ElementKeyMap.isValidType(piece.getType())) {
				if (ElementKeyMap.getInfo(piece.getType()).controlsAll()) {
					delegate = true;
				}
			}
			if (active) {
				d = ElementCollection.getActivation(index, delegate, true);
				assert (ElementCollection.getType(d) > 10);
			} else {
				d = ElementCollection.getDeactivation(index, delegate, true);
				assert (ElementCollection.getType(d) > 10);
			}
			ObjectArrayFIFOQueue<SignalTrace> signalQueue = ((SendableSegmentController) getSegmentController()).signalQueue;
			SignalTrace st = ((SendableSegmentController)getSegmentController()).getSignalPool().get();
			
			st.set(ElementCollection.getPosIndexFrom4(d), d, ((SendableSegmentController) getSegmentController()).currentTrace);
			signalQueue.enqueue(st);
			
			if(this.getSegmentController().isOnServer() && signalQueue.size() > ServerConfig.MAX_LOGIC_SIGNAL_QUEUE_PER_OBJECT.getInt()){
				((GameServerState)this.getSegmentController().getState()).getController().broadcastMessage(
						Lng.astr("WARNING: Too much logic lag caused by\n%s; in sector: %s", this.getSegmentController().getRealName(), this.getSegmentController().getSector(new Vector3i())), ServerMessage.MESSAGE_TYPE_ERROR);
				signalQueue.clear();
				return;
			}
		}
	}

}
