package org.schema.game.common.data.player;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.character.AbstractAnimatedObject;
import org.schema.game.client.view.character.AnimationNotSetException;
import org.schema.game.client.view.character.DrawableCharacterInterface;
import org.schema.schine.graphicsengine.animation.LoopMode;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationIndexElement;
import org.schema.schine.resource.CreatureStructure.PartType;

public class ForcedAnimation {
	public PartType type;
	public AnimationIndexElement animation;
	public LoopMode loopMode;
	public float speed;
	public boolean fullBody;
	public long time;
	public boolean received;
	private boolean applied;

	public ForcedAnimation(PartType type,
	                       AnimationIndexElement animation, LoopMode loopMode,
	                       float speed, boolean fullBody) {

		this.fullBody = fullBody;
		this.type = type;
		this.animation = animation;
		this.loopMode = loopMode;
		this.speed = speed;
	}

	public void apply(AbstractCharacterInterface c) {
		if (!applied) {

			assert (!c.isOnServer());
			GameClientState state = ((GameClientState) c.getState());

			DrawableCharacterInterface<?> drawableCharacterInterface = state.getWorldDrawer().getCharacterDrawer().getPlayerCharacters().get(c.getId());
			if (drawableCharacterInterface != null && drawableCharacterInterface instanceof AbstractAnimatedObject<?>) {
				try {
					((AbstractAnimatedObject<?>) drawableCharacterInterface).setForcedAnimation(type, animation, fullBody);
					((AbstractAnimatedObject<?>) drawableCharacterInterface).setAnimSpeedForced(type, speed, fullBody);
					((AbstractAnimatedObject<?>) drawableCharacterInterface).setForcedLoopMode(type, loopMode, fullBody);
					applied = true;
					System.err.println("[CLIENT] Forced Animation applied: " + this);
					return;
				} catch (AnimationNotSetException e) {
					e.printStackTrace();
				}
			}
			assert (false) : c + "; " + state.getWorldDrawer().getCharacterDrawer().getPlayerCharacters();
		}

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return ((Long) time).hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return time == ((ForcedAnimation) obj).time;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ForcedAnimation [type=" + type + ", animation="
				+ animation + ", loopMode=" + loopMode + ", speed=" + speed
				+ ", fullBody=" + fullBody + "]";
	}

	public void set(ForcedAnimation forcedAnimation) {
		type = forcedAnimation.type;
		animation = forcedAnimation.animation;
		loopMode = forcedAnimation.loopMode;
		speed = forcedAnimation.speed;
		fullBody = forcedAnimation.fullBody;
		time = forcedAnimation.time;
	}

	public void resetApplied() {
		applied = false;
	}

}
