package org.schema.game.client.controller.tutorial.states;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class PlaceElementOnLastSpawnedTestState extends SatisfyingCondition {

	/**
	 *
	 */
	
	private short type;
	private boolean mustBuyNew;
	private Vector3i where;
	private int count;
	private String txt;

	public PlaceElementOnLastSpawnedTestState(AiEntityStateInterface gObj, GameClientState state, String txt, short clazz, Vector3i where, boolean mustBuyNew, int count) {
		super(gObj, txt, state);
		this.type = clazz;
		skipIfSatisfiedAtEnter = true;
		this.txt = txt;
		this.where = where;
		this.mustBuyNew = mustBuyNew;
		this.count = count;
		if (where != null) {
			setMarkers(new ObjectArrayList<TutorialMarker>(1));
			TutorialMarker tutorialMarker = new TutorialMarker(where, "Place " + ElementKeyMap.getInfo(type).getName() + " here");
			getMarkers().add(tutorialMarker);
		}

	}

	@Override
	protected boolean checkSatisfyingCondition() throws FSMException {
		if (getGameState().getController().getTutorialMode().lastSpawnedShip != null) {
			if (where == null) {
//				System.err.println("PLACE BLOCKS: "+type+": "+getGameState().getController().getTutorialMode().lastSpawnedShip.getElementClassCountMap().get(type)+" of "+count);
				return getGameState().getController().getTutorialMode().lastSpawnedShip.getElementClassCountMap().get(type) >= count;
			} else {
				getMarkers().get(0).context = getGameState().getController().getTutorialMode().lastSpawnedShip;
				SegmentPiece pointUnsave = getGameState().getController().getTutorialMode().lastSpawnedShip.getSegmentBuffer().getPointUnsave(where);
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
			if (getGameState().getController().getTutorialMode().lastSpawnedShip != null) {
				placed = getGameState().getController().getTutorialMode().lastSpawnedShip.getElementClassCountMap().get(type);
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
				int slot = getGameState().getPlayer().getInventory().incExistingOrNextFreeSlot(type, 1);
				getGameState().getPlayer().sendInventoryModification(slot, Long.MIN_VALUE);
			}
		} else if (firstSlot > 10) {
			setMessage("Please drag \n" +
					"" + ElementKeyMap.getInfo(type).getName() + "\n" +
					"to the bottom bar from\n" +
					"the inventory (Press I)");
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

}
