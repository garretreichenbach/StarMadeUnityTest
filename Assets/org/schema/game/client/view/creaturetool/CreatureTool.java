package org.schema.game.client.view.creaturetool;

import com.bulletphysics.linearmath.Transform;
import org.lwjgl.opengl.GL11;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.WorldDrawer;
import org.schema.game.client.view.character.AbstractDrawableCreature;
import org.schema.game.client.view.character.AnimationNotSetException;
import org.schema.game.client.view.character.CharactersDrawer;
import org.schema.game.client.view.character.DrawableCharacterInterface;
import org.schema.game.client.view.creaturetool.swing.CreatureToolFrame;
import org.schema.game.common.data.creature.AIRandomCompositeCreature;
import org.schema.game.network.objects.remote.RemoteCreatureSpawnRequest;
import org.schema.game.server.data.CreatureSpawn;
import org.schema.game.server.data.CreatureType;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationIndexElement;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.resource.CreatureStructure.PartType;

import javax.swing.*;

public class CreatureTool implements Drawable, GUICallback {

	private DrawableCharacterInterface creatureDrawable;
	private AIRandomCompositeCreature creature;
	private GameClientState state;
	private Timer timer;
	private CreatureToolFrame f;
	private boolean disposed;
	private AIRandomCompositeCreature toSpawn;

	public CreatureTool(GameClientState state, Timer timer) {
		super();
		this.state = state;
		this.timer = timer;

		f = new CreatureToolFrame(state, this);
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		f.setVisible(true);
	}

	@Override
	public void cleanUp() {

	}

	public boolean isOccluded() {
		return false;
	}

	@Override
	public void draw() {
		synchronized(state) {
			if(creatureDrawable != null && state.getCurrentPlayerObject() != null) {
				creature.getWorldTransformOnClient().setIdentity();
				creature.getWorldTransformOnClient().origin.set(state.getCurrentPlayerObject().getWorldTransformOnClient().origin);
				GlUtil.glEnable(GL11.GL_BLEND);
				GlUtil.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GlUtil.glBlendFuncSeparate(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY);
				GlUtil.glEnableClientState(GL11.GL_NORMAL_ARRAY);
				GlUtil.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

				creatureDrawable.draw();

				GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
				GlUtil.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
				GlUtil.glDisableClientState(GL11.GL_NORMAL_ARRAY);
				GlUtil.glDisable(GL11.GL_BLEND);
			}

			if(toSpawn != null) {
				//CreatureSpawn p = new CreatureSpawn(new Vector3i(state.getCurrentRemoteSector().clientPos()), new Transform(creature.getWorldTransformOnClient()), "CreatureTestSpawn", CreatureType.CREATURE_SPECIFIC);
				CreatureSpawn p = new CreatureSpawn(new Vector3i(state.getCurrentRemoteSector().clientPos()), new Transform(creature.getWorldTransformOnClient()), "CreatureTestSpawn", CreatureType.CREATURE_RANDOM);
				p.setNode(creature.getCreatureNode());
				p.setSpeed(creature.getSpeed());
				state.getPlayer().getNetworkObject().creatureSpawnBuffer.add(new RemoteCreatureSpawnRequest(p, false));
				toSpawn = null;
			}
		}
	}

	@Override
	public boolean isInvisible() {
		return false;
	}

	@Override
	public void onInit() {

	}

	public void make(Timer timer, AIRandomCompositeCreature c) {
		this.creature = c;
		c.initialize();
		c.initPhysics();
		creatureDrawable = CharactersDrawer.getDrawer(c, timer, state);

	}

	public void openGUI() {
		CreatureToolFrame f = new CreatureToolFrame(state, this);
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {

	}

	public void updateFromGUI(AIRandomCompositeCreature creature) {
		synchronized(state) {
			if(creature != null) {
				make(timer, creature);
			} else {
				this.creature = null;
				this.creatureDrawable = null;
			}
		}
	}

	public void onDiasble() {
		this.disposed = true;
		f.dispose();
	}

	public void onGUIDispose() {
		if(!disposed) {
			WorldDrawer.flagCreatureTool = true;
		}
	}

	public void updateForcedAnimation(PartType type, Object selectedItem) throws AnimationNotSetException {
		if(creature != null) {
			((AbstractDrawableCreature<?>) creatureDrawable).setForcedAnimation(type, (AnimationIndexElement) selectedItem);
		}
	}

	public AIRandomCompositeCreature getCreature() {
		return creature;
	}

	public void updateAnimSpeed(PartType type, float speed) {
		((AbstractDrawableCreature<?>) creatureDrawable).setAnimSpeedForced(type, speed);
	}

	public void spawn() {
		synchronized(state) {
			if(toSpawn != null) toSpawn.destroyPersistent();
			toSpawn = creature;
		}
	}

	public void despawn() {
		synchronized(state) {
			if(toSpawn != null) {
				toSpawn.destroyPersistent();
				toSpawn = null;
			}
			if(creature != null) {
				creature.destroyPersistent();
				creature = null;
			}
		}
	}
}
