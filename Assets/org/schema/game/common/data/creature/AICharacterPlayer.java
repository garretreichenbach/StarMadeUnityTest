package org.schema.game.common.data.creature;

import javax.vecmath.Vector3f;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.element.meta.weapon.Weapon;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.player.inventory.PlayerInventory;
import org.schema.schine.input.KeyboardMappings;

import com.bulletphysics.linearmath.Transform;

public class AICharacterPlayer extends AIPlayer {

	private long targetMoveCommand;
	private long moveTimeout;

	public AICharacterPlayer(AICreature<? extends AIPlayer> creature) {
		super(creature);

	}

	@Override
	protected boolean isFlying() {
		return !getCreature().getGravity().isGravityOn();
	}

	@Override
	protected Inventory initInventory() {
		return new PlayerInventory(this, AbstractOwnerState.NORM_INV);
	}

	@Override
	public void resetTargetToMove() {
		setTarget(null);
		getNetworkObject().hasTarget.set(false);
	}

	@Override
	public boolean isTargetReachTimeout() {
		return getTarget() != null && moveTimeout > 0 && System.currentTimeMillis() - targetMoveCommand > moveTimeout;
	}

	@Override
	public boolean isTargetReachLocalTimeout() {
		return getTarget() != null && moveTimeout > 0 && System.currentTimeMillis() - lastTargetSet > 1000;
	}

	@Override
	public void setTargetToMove(Vector3f pos, long timeout) {
		targetMoveCommand = System.currentTimeMillis();
		moveTimeout = timeout;
		Transform t = new Transform();
		t.setIdentity();
		t.origin.set(pos);
		setTarget(t);
		getNetworkObject().target.set(t.origin);
		getNetworkObject().hasTarget.set(true);
		
	}

	@Override
	public float getMaxHealth() {
		return 300;
	}

	@Override
	protected void updateServer() {
		if (getTarget() == null) {

		}
	}

	@Override
	protected void updateClient() {

	}

	@Override
	public void interactClient(final AbstractOwnerState from) {
		GameClientState state = (GameClientState) getState();
		state.getPlayer().getPlayerConversationManager().clientInteracted(this);
	}

	@Override
	public boolean isFactoryInUse() {
		return false;
	}


	@Override
	protected void onNoSlotFree(short type, int amount) {
		
	}

	@Override
	public void onFiredWeapon(Weapon object) {
		
	}

	@Override
	public String getConversationScript() {
		if (conversationScript == null) {
			if (getFactionId() == FactionManager.TRAIDING_GUILD_ID) {
				conversationScript = "npc-trading-guild.lua";
			} else {
				conversationScript = "npc-general.lua";
			}
		}
		return conversationScript;
	}

	@Override
	public long getDbId() {
		return Long.MIN_VALUE;
	}

	@Override
	public boolean isDown(KeyboardMappings mapping) {
		return false;
	}

	

}
