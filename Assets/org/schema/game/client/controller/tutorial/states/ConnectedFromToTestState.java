package org.schema.game.client.controller.tutorial.states;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.EditableSendableSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.common.language.Lng;
import org.schema.schine.input.KeyboardMappings;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class ConnectedFromToTestState extends SatisfyingCondition {

	/**
	 *
	 */
	
	private Vector3i connectFrom;
	private Vector3i connectTo;

	public ConnectedFromToTestState(AiEntityStateInterface gObj, String message, GameClientState state,
	                                Vector3i connectFrom,
	                                Vector3i connectTo) {
		super(gObj, message, state);
		this.connectFrom = connectFrom;
		this.connectTo = connectTo;
		skipIfSatisfiedAtEnter = true;

		setMarkers(new ObjectArrayList<TutorialMarker>());
		getMarkers().add(new TutorialMarker(connectFrom, "First, press '" + KeyboardMappings.SELECT_MODULE.getKeyChar() + "' here to select this block"));

	}

	@Override
	protected boolean checkSatisfyingCondition() throws FSMException {
		return isConnected();
	}

	private boolean isConnected() throws FSMException {
		EditableSendableSegmentController s = getGameState().getController().getTutorialMode().currentContext;
		getMarkers().set(0, new TutorialMarker(connectFrom, "First, press '" + KeyboardMappings.SELECT_MODULE.getKeyChar() + "' here to select this block"));
		if (s != null) {
			SegmentPiece cc = s.getSegmentBuffer().getPointUnsave(connectFrom);
			SegmentPiece tt = s.getSegmentBuffer().getPointUnsave(connectTo);
			SegmentPiece selectedBlock = getGameState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getPlayerCharacterManager().getSelectedBlock();
			if (cc != null && selectedBlock != null) {
				if (!cc.getAbsolutePos(new Vector3i()).equals(selectedBlock.getAbsolutePos(new Vector3i()))) {
					getMarkers().set(0, new TutorialMarker(connectFrom, "First, press '" + KeyboardMappings.SELECT_MODULE.getKeyChar() + "' here to select this block"));
					return false;
				}
			}

			if (tt != null && cc != null && ElementKeyMap.isValidType(cc.getType()) && ElementKeyMap.isValidType(tt.getType()) &&
					(ElementInformation.canBeControlled(cc.getType(), tt.getType()) || ElementKeyMap.getInfo(cc.getType()).getControlling().contains(tt.getType()))) {
				short toType = tt.getType();
				LongOpenHashSet longOpenHashSet = s.getControlElementMap().getControllingMap().getAll().get(ElementCollection.getIndex(connectFrom));
				SegmentPiece selected = getGameState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getSelectedBlockByActiveController();

				if (selected != null && selected.equalsPos(connectFrom)) {
					getMarkers().set(0, new TutorialMarker(connectTo, "Then, press '" + KeyboardMappings.CONNECT_MODULE.getKeyChar() + "' here to connect\nthis block to the one with the orange wobble"));
				}

				return longOpenHashSet != null && longOpenHashSet.contains(ElementCollection.getIndex4((short) connectTo.x, (short) connectTo.y, (short) connectTo.z, toType));
			} else {
				if (tt != null && cc != null) {
					if (ElementKeyMap.isValidType(cc.getType()) && ElementKeyMap.isValidType(tt.getType())) {
						System.err.println("[ERROR] from to: " + ElementInformation.canBeControlled(cc.getType(), tt.getType()) + "; " + ElementKeyMap.getInfo(cc.getType()).getControlling().contains(tt.getType()));
					}
					System.err.println("[ERROR] Blocks to connect dont exist or are incompatible: " + cc + " -> " + tt);
					getEntityState().getMachine().getFsm().stateTransition(Transition.TUTORIAL_FAILED);
					return false;
				} else {
					return false;
				}
			}

		} else {
			getEntityState().getMachine().getFsm().stateTransition(Transition.TUTORIAL_FAILED);
			return false;
		}
	}

	public boolean checkConnectBlock(SegmentPiece selectedBlock, GameClientState state) {

		if (selectedBlock == null || !selectedBlock.equalsPos(connectFrom)) {
			state.getController().popupAlertTextMessage(Lng.str("Please select the block\nthe marker is pointing to!"), 0);

			return false;
		}

		return true;
	}

	public boolean checkConnectToBlock(Vector3i c,
	                                   GameClientState state) {
		if (c == null || !c.equals(connectTo)) {
			state.getController().popupAlertTextMessage(Lng.str("Please connect the block\nthe marker is pointing to!"), 0);
			return false;
		}
		return true;
	}

}
