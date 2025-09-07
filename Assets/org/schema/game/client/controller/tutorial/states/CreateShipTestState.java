package org.schema.game.client.controller.tutorial.states;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.admin.AdminCommandIllegalArgument;
import org.schema.game.server.data.admin.AdminCommands;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIChangeListener;
import org.schema.schine.network.objects.Sendable;

public class CreateShipTestState extends SatisfyingCondition implements GUIChangeListener {

	/**
	 *
	 */
	

	boolean satisfied;

	private String lastShipName;

	private GameClientState state;

	private boolean limitedBlockSupply;

	private boolean takeAsCurrentContext;

	public CreateShipTestState(AiEntityStateInterface gObj, String message, GameClientState state, boolean limitedBlockSupply, boolean takeAsCurrentContext) {
		super(gObj, message, state);

		this.state = state;
		this.limitedBlockSupply = limitedBlockSupply;
		skipIfSatisfiedAtEnter = true;
		this.takeAsCurrentContext = takeAsCurrentContext;
	}

	@Override
	protected boolean checkSatisfyingCondition() {
		if (satisfied) {
			satisfied = false;
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.tutorial.states.SatisfyingCondition#onEnter()
	 */
	@Override
	public boolean onEnter() {
		state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().
				getPlayerIntercationManager().getPlayerCharacterManager().addObserver(this);

		getGameState().getController().getTutorialMode().lastSpawnedShip = null;

		return super.onEnter();

	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.tutorial.states.SatisfyingCondition#onExit()
	 */
	@Override
	public boolean onExit() {
		state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().
				getPlayerIntercationManager().getPlayerCharacterManager().deleteObserver(this);
		return super.onExit();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.tutorial.states.SatisfyingCondition#onUpdate()
	 */
	@Override
	public boolean onUpdate() throws FSMException {

		if (getGameState().getController().getTutorialMode().lastSpawnedShip != null) {
			if (takeAsCurrentContext) {
				getGameState().getController().getTutorialMode().currentContext = getGameState().getController().getTutorialMode().lastSpawnedShip;
			}
			satisfied = true;
		}

		if (lastShipName != null) {
			synchronized (getGameState().getLocalAndRemoteObjectContainer().getLocalObjects()) {
				for (Sendable s : getGameState().getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
					if (s instanceof Ship && ((Ship) s).getUniqueIdentifier().equals(lastShipName)) {
						getGameState().getController().getTutorialMode().lastSpawnedShip = (Ship) s;
						getGameState().getGlobalGameControlManager().getIngameControlManager()
								.getPlayerGameControlManager().getPlayerIntercationManager()
								.setSelectedEntity((SimpleTransformableSendableObject) s);
						lastShipName = null;
					}
				}
			}
		}
		if (!satisfied) {
			Inventory inventory = getGameState().getPlayer().getInventory(null);
			if (!inventory.existsInInventory(ElementKeyMap.CORE_ID)) {

				if (limitedBlockSupply) {
					System.err.println("[TUTORIAL] ship core does NOT exist");
					getGameState().getController().popupAlertTextMessage("" +
							Lng.str("WARNING\nYou already used your core!\nYou will be given one more.\n\n(this works if you're an admin)"), 0);

					try {
						getGameState().getController().sendAdminCommand(AdminCommands.GIVEID, AdminCommands.packParameters(AdminCommands.GIVEID, getGameState().getPlayer().getName(), String.valueOf(ElementKeyMap.CORE_ID), "1"));
					} catch (AdminCommandIllegalArgument e) {
						e.printStackTrace();
					}
				} else {
					int slot;
					slot = getGameState().getPlayer().getInventory().incExistingOrNextFreeSlot(ElementKeyMap.CORE_ID, 1);
					getGameState().getPlayer().sendInventoryModification(slot, Long.MIN_VALUE);
				}
			} else {
				//			System.err.println("[TUTORIAL] ship core exists");
			}
		}

		return super.onUpdate();
	}


	@Override
	public void onChange(boolean updateListDim) {
		assert(false):"not implemented";
	}

}
