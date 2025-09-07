package org.schema.game.client.view.character;

import org.lwjgl.opengl.GL11;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.creature.AICharacter;
import org.schema.game.common.data.creature.AICompositeCreature;
import org.schema.game.common.data.creature.AIRandomCompositeCreature;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.player.AbstractCharacterInterface;
import org.schema.game.common.data.player.PlayerCharacter;
import org.schema.game.common.data.player.simplified.SimplifiedCharacter;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.objects.Sendable;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

public class CharactersDrawer implements Drawable {

	private final Int2ObjectOpenHashMap<DrawableCharacterInterface> playerCharacters = new Int2ObjectOpenHashMap<DrawableCharacterInterface>();
	public boolean shadow;
	boolean lastState;
	private GameClientState state;
	private boolean firstDraw = true;

	public CharactersDrawer(GameClientState state) {
		this.state = state;
	}

	public static DrawableCharacterInterface<?> getDrawer(AbstractCharacterInterface c, Timer timer, GameClientState state) {
		if (c instanceof PlayerCharacter) {
			DrawableCharacterInterface<?> playerCharacter = new DrawableHumanCharacterNew((PlayerCharacter) c, timer, state);
			return playerCharacter;

		} else if (c instanceof AICharacter) {

			DrawableCharacterInterface<?> playerCharacter;
			playerCharacter = new DrawableAIHumanCharacterNew((AICharacter) c, timer, state);
			//			playerCharacter = new AbstractDrawableCreature<AICharacterPlayer>((AICharacter) c);
			//			playerCharacter = new AbstractDrawableHumanCharacter<AICharacterPlayer, AICharacter>((AICharacter) c);

			return playerCharacter;
		} else if (c instanceof SimplifiedCharacter) {

			DrawableCharacterInterface<?> playerCharacter;
			playerCharacter = new DrawableSimplifiedHumanCharacter((SimplifiedCharacter) c, timer, state);
			//			playerCharacter = new AbstractDrawableCreature<AICharacterPlayer>((AICharacter) c);
			//			playerCharacter = new AbstractDrawableHumanCharacter<AICharacterPlayer, AICharacter>((AICharacter) c);

			return playerCharacter;
		} else if (c instanceof AICompositeCreature) {

			DrawableCharacterInterface<?> playerCharacter;
			//			playerCharacter = new DrawableAIHumanCharacterNew((AICharacter) c);
			playerCharacter = new AbstractDrawableCreature((AIRandomCompositeCreature) c, timer, state);

			return playerCharacter;
		} else {
			throw new IllegalArgumentException("No drawer found for " + c);
		}
	}

	@Override
	public void cleanUp() {
		
	}

	@Override
	public void draw() {
		if (firstDraw) {
			onInit();
		}
		boolean drawSelf = true;
		if (state.getWorldDrawer().getCreatureTool() != null) {
			drawSelf = false;
		}
		GlUtil.glEnable(GL11.GL_BLEND);

		GlUtil.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glBlendFuncSeparate(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		//		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GlUtil.glEnableClientState(GL11.GL_NORMAL_ARRAY);
		GlUtil.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		for (DrawableCharacterInterface<?> p : playerCharacters.values()) {
			p.setShadowMode(shadow);
			if (p.isInFrustum() && (drawSelf || p.getEntity() != state.getCharacter())) {
				p.draw();
			}
			p.setShadowMode(false);
		}
		GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		GlUtil.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GlUtil.glDisableClientState(GL11.GL_NORMAL_ARRAY);
		GlUtil.glDisable(GL11.GL_BLEND);
	}

	@Override
	public boolean isInvisible() {
				return false;
	}

	@Override
	public void onInit() {

		//		Skin skin = ((Mesh)Controller.getResLoader().getMesh("Konata").getChilds().get(0)).getSkin();
		//		skin.setDiffuseTexId(GameResourceLoader.marpleTexture.getTextureId());

		firstDraw = false;

	}

	public void clear() {
		for (DrawableCharacterInterface remove : playerCharacters.values()) {
			remove.onRemove();
		}

		playerCharacters.clear();

	}

	public void update(Timer timer) {

		for (DrawableCharacterInterface p : playerCharacters.values()) {

			p.update(timer);
		}
	}

	public void updateCharacterSet(Timer timer) {

		ObjectIterator<DrawableCharacterInterface> iterator = playerCharacters.values().iterator();
		while (iterator.hasNext()) {
			DrawableCharacterInterface c = iterator.next();
			if (!c.isInClientRange()) {
				iterator.remove();
				c.onRemove();
			}
		}
		for (Sendable s : state.getCurrentSectorEntities().values()) {
			if (s instanceof AbstractCharacter) {
				if (!playerCharacters.containsKey(s.getId())) {
					playerCharacters.put(s.getId(), getDrawer((AbstractCharacter<?>) s, timer, state));
				}
			}
		}
	}

	/**
	 * @return the playerCharacters
	 */
	public Int2ObjectOpenHashMap<DrawableCharacterInterface> getPlayerCharacters() {
		return playerCharacters;
	}

}
