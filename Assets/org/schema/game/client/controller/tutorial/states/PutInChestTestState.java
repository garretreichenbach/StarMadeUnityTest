package org.schema.game.client.controller.tutorial.states;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.ingame.InventoryControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.common.language.Lng;
import org.schema.schine.input.KeyboardMappings;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class PutInChestTestState extends SatisfyingCondition {

	/**
	 *
	 */
	
	private short type;
	private boolean mustBuyNew;
	private Vector3i where;
	private int count;
	private String txt;

	public PutInChestTestState(AiEntityStateInterface gObj, GameClientState state, String txt, short clazz, Vector3i where, boolean mustBuyNew, int count) {
		super(gObj, txt, state);
		this.type = clazz;
		skipIfSatisfiedAtEnter = true;
		this.txt = txt;
		this.where = where;
		this.mustBuyNew = mustBuyNew;
		this.count = count;

		if (where != null) {
			setMarkers(new ObjectArrayList<TutorialMarker>(1));
			getMarkers().add(new TutorialMarker(where, Lng.str("Place %s here", ElementKeyMap.getInfo(type).getName())));
		}

	}

	@Override
	protected boolean checkSatisfyingCondition() throws FSMException {
		if (getGameState().getController().getTutorialMode().currentContext != null) {
			InventoryControllerManager iv = getGameState().getGlobalGameControlManager().getIngameControlManager()
					.getPlayerGameControlManager().getInventoryControlManager();
			if (iv.getSecondInventory() == null || iv.getSecondInventory().getParameter()!= ElementCollection.getIndex(where)) {
				return false;
			}
			return iv.getSecondInventory().getOverallQuantity(type) >= count;

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
		getGameState().getController().getTutorialMode().highlightType = type;
		setMessage(txt);
		return false;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.tutorial.states.SatisfyingCondition#onUpdate()
	 */
	@Override
	public boolean onUpdate() throws FSMException {

		InventoryControllerManager iv = getGameState().getGlobalGameControlManager().getIngameControlManager()
				.getPlayerGameControlManager().getInventoryControlManager();

		if (iv.getSecondInventory() == null || iv.getSecondInventory().getParameter()!= ElementCollection.getIndex(where)) {
			setMessage(
					Lng.str("Open the chest at the marker\nposition ('%s')", KeyboardMappings.ACTIVATE.getKeyChar()));
			return super.onUpdate();
		}
		int placed = iv.getSecondInventory().getOverallQuantity(type);
		int quant = getGameState().getPlayer().getInventory().getOverallQuantity(type);
		if (quant < count - placed) {
			if (mustBuyNew) {
				setMessage(
						Lng.str("You don't have enough (%s total)\n" +
								"%s\n" +
								"Please buy one in\n" +
								"the shop (Press %s)\n", count, ElementKeyMap.getInfo(type).getName(), KeyboardMappings.SHOP_PANEL.getKeyChar()));
			} else {
				int slot = getGameState().getPlayer().getInventory().incExistingOrNextFreeSlot(type, count - (placed + quant));
				getGameState().getPlayer().sendInventoryModification(slot, Long.MIN_VALUE);
			}
		} else {

			setMessage(Lng.str("Drag at least %s more \n%s from\n"
					+ "your inventory into the block\n"
					+ "inventoy below", (count - placed), ElementKeyMap.getInfo(type)));

		}

		return super.onUpdate();
	}

}
