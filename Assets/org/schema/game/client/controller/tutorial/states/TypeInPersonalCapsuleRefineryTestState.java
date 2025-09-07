package org.schema.game.client.controller.tutorial.states;

import java.util.Set;

import org.schema.game.client.controller.manager.ingame.InventoryControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.common.language.Lng;
import org.schema.schine.input.KeyboardMappings;

public class TypeInPersonalCapsuleRefineryTestState extends SatisfyingCondition {

	/**
	 *
	 */
	
	private short type;
	private int count;
	public TypeInPersonalCapsuleRefineryTestState(AiEntityStateInterface gObj, String message, GameClientState state, short type, int count) {
		super(gObj, message, state);
		skipIfSatisfiedAtEnter = true;
		this.type = type;
		this.count = count;
	}

	@Override
	protected boolean checkSatisfyingCondition() {

		return getGameState().getPlayer().getPersonalFactoryInventoryCapsule().getOverallQuantity(type) >= count;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.tutorial.states.SatisfyingCondition#onEnter()
	 */
	@Override
	public boolean onEnter() {
		setMessage("Open up your inventory (" + KeyboardMappings.INVENTORY_PANEL.getKeyChar() + ")");
		getGameState().getController().getTutorialMode().highlightType = type;
		return super.onEnter();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.tutorial.states.SatisfyingCondition#onUpdate()
	 */
	@Override
	public boolean onUpdate() throws FSMException {

		Inventory target = getGameState().getPlayer().getPersonalFactoryInventoryCapsule();
		int placed = target.getOverallQuantity(type);
		if (placed < count && getGameState().getPlayer().getInventory().getOverallQuantity(type) < count - placed) {
			int inc = getGameState().getPlayer().getInventory().incExistingOrNextFreeSlot(type, count - placed);
			getGameState().getPlayer().getInventory().sendInventoryModification(inc);
		}

		String targetName = Lng.str("Refine Raw Materials");

		InventoryControllerManager iv = getGameState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getInventoryControlManager();
		if (!iv.isTreeActive()) {
			setMessage(Lng.str("Open up your inventory (%s)", KeyboardMappings.INVENTORY_PANEL.getKeyChar()));
			return super.onUpdate();
		}

		if (!getGameState().getWorldDrawer().getGuiDrawer().getPlayerPanel().getInventoryPanel().isCapsuleRefineryOpen()) {
			setMessage(Lng.str("Please click on the Craft and then on the \n'%s' button", targetName));
			return super.onUpdate();
		}

		Set<Integer> slots = target.getSlots();
		for (int i : slots) {
			short in = target.getType(i);
			boolean compatible = (ElementKeyMap.isShard(type) && in == ElementKeyMap.CRYSTAL_CRIRCUITS) || (ElementKeyMap.isOre(type) && in == ElementKeyMap.METAL_MESH);
			if (in != 0 && in != type && !compatible) {
				if (in > 0) {
					setMessage(Lng.str("Please drag \n%s from the\n'%s' inventory\nback to your own inventory", ElementKeyMap.getInfo(in), targetName));
				} else {
					setMessage(Lng.str("Please remove \n the item from the\n'%s' inventory", targetName));
				}
				return super.onUpdate();
			}
		}

		setMessage(Lng.str("Drag at least %s more\n%s into\n"
				+ "one of the two slots\nbelow your inventory", (count - placed), ElementKeyMap.getInfo(type)));
		return super.onUpdate();
	}

}
