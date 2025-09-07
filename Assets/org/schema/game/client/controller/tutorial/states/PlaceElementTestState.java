package org.schema.game.client.controller.tutorial.states;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.input.KeyboardMappings;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class PlaceElementTestState extends SatisfyingCondition {

	/**
	 *
	 */
	
	private short type;
	private boolean mustBuyNew;
	private Vector3i where;
	private int count;
	private String txt;

	public PlaceElementTestState(AiEntityStateInterface gObj, GameClientState state, String txt, short clazz, Vector3i where, boolean mustBuyNew, int count) {
		super(gObj, txt, state);
		this.type = clazz;
		skipIfSatisfiedAtEnter = true;
		this.txt = txt;
		this.where = where;
		this.mustBuyNew = mustBuyNew;
		this.count = count;
		if (where != null) {
			setMarkers(new ObjectArrayList<TutorialMarker>(1));
			getMarkers().add(new TutorialMarker(where, "Place " + ElementKeyMap.getInfo(type).getName() + " here"));
		}

	}

	@Override
	protected boolean checkSatisfyingCondition() throws FSMException {
		if (getGameState().getController().getTutorialMode().currentContext != null) {
			if (where == null) {
				return getGameState().getController().getTutorialMode().currentContext.getElementClassCountMap().get(type) >= count;
			} else {
				SegmentPiece pointUnsave = getGameState().getController().getTutorialMode().currentContext.getSegmentBuffer().getPointUnsave(where);
				return pointUnsave != null && pointUnsave.getType() == type;
			}
		} else {
			getEntityState().getMachine().getFsm().stateTransition(Transition.TUTORIAL_FAILED);
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.tutorial.states.SatisfyingCondition#onEnter()
	 */
	@Override
	public boolean onEnter() {
		super.onEnter();
		setMessage(txt);
		return false;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.tutorial.states.SatisfyingCondition#onUpdate()
	 */
	@Override
	public boolean onUpdate() throws FSMException {
		int placed = 0;
		if (where == null) {
			if (getGameState().getController().getTutorialMode().currentContext != null) {
				placed = getGameState().getController().getTutorialMode().currentContext.getElementClassCountMap().get(type);
			}
		}

		int firstSlot = getGameState().getPlayer().getInventory().getFirstSlot(type, true);
		if (firstSlot < 0) {
			if (mustBuyNew) {
				setMessage(
						"You don't have any \n" +
								"" + ElementKeyMap.getInfo(type).getName() + "\n" +
								"Modules.\n" +
								"Please buy one in\n" +
								"the shop (Press B)\n");
			} else {
				int slot = getGameState().getPlayer().getInventory().incExistingOrNextFreeSlot(type, count);
				getGameState().getPlayer().sendInventoryModification(slot, Long.MIN_VALUE);
			}
		} else if (firstSlot > 10) {
			setMessage("Please drag \n" +
					"" + ElementKeyMap.getInfo(type).getName() + "\n" +
					"to the bottom bar from\n" +
					"the inventory (Press " + KeyboardMappings.INVENTORY_PANEL.getKeyChar() + ")");
		} else {
			if (where == null) {
				setMessage("Please select \n" +
						"" + ElementKeyMap.getInfo(type).getName() + " (press " + ((firstSlot + 1) % 10) + ",\n" +
						"or use the mouse wheel to select it)\n" +
						"from the bottom bar and place\n" +
						+(count - placed) + " anywhere on the ship (left click)");
			} else {
				setMessage("Please select \n" +
						"" + ElementKeyMap.getInfo(type).getName() + " (press " + ((firstSlot + 1) % 10) + ",\n" +
						"or use the mouse wheel to select it)\n" +
						"from the bottom bar and place it on the\n" +
						"marker (left click)");
			}
		}

		return super.onUpdate();
	}

	/**
	 * @return the where
	 */
	public Vector3i getWhere() {
		return where;
	}

	/**
	 * @param where the where to set
	 */
	public void setWhere(Vector3i where) {
		this.where = where;
	}

}
