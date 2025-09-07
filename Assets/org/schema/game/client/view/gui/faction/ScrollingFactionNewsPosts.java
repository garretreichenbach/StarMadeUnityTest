package org.schema.game.client.view.gui.faction;

import java.util.TreeSet;

import org.schema.game.client.controller.ClientChannel.FactionNewsPostListener;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.PlayerFactionController;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionNewsPost;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIChangeListener;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class ScrollingFactionNewsPosts extends GUIElement implements GUIChangeListener, FactionNewsPostListener {

	private int width;

	private int height;

	private GUIScrollablePanel newsPostScrollPanel;

	private GUIElementList newsPostList;

	private boolean updateNeeded;

	public ScrollingFactionNewsPosts(InputState state, int width, int height) {
		super(state);
		((GameClientState) state).getFactionManager().obs.addObserver(this);
		this.width = width;
		this.height = height;
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public void draw() {
		if (((GameClientState) getState()).getController().getClientChannel() == null) {
			return;
		} else {
			((GameClientState) getState()).getController().getClientChannel().factionNewsListeners.add(this);
		}
		if (updateNeeded) {
			System.err.println("[FACTIONNEWS] UPDATE NEWS");
			updateNewsPosts();
			updateNeeded = false;
		}
		drawAttached();
	}

	@Override
	public void onInit() {
		newsPostScrollPanel = new GUIScrollablePanel(width, height, getState());
		newsPostList = new GUIElementList(getState());
		newsPostScrollPanel.setContent(newsPostList);
		updateNeeded = true;
		attach(newsPostScrollPanel);
	}

	@Override
	public float getHeight() {
		return height;
	}

	@Override
	public float getWidth() {
		return width;
	}

	private void updateNewsPosts() {
		Faction faction = ((GameClientState) getState()).getFactionManager().getFaction(((GameClientState) getState()).getPlayer().getFactionId());
		newsPostList.clear();
		if (faction != null) {
			TreeSet<FactionNewsPost> treeSet = ((GameClientState) getState()).getFactionManager().getNews().get(faction.getIdFaction());
			assert (((GameClientState) getState()).getController().getClientChannel() != null);
			assert (((GameClientState) getState()).getController().getClientChannel().getFactionNews() != null);
			if (treeSet != null) {
				treeSet.addAll(((GameClientState) getState()).getController().getClientChannel().getFactionNews());
			} else {
				treeSet = ((GameClientState) getState()).getController().getClientChannel().getFactionNews();
			}
			if (treeSet != null) {
				int index = 0;
				for (FactionNewsPost p : treeSet.descendingSet()) {
					FactionNewsPostGUIListEntry e = new FactionNewsPostGUIListEntry(getState(), p, index);
					index++;
					newsPostList.add(e);
				}
			}
			GUIAnchor bg = new GUIAnchor(getState(), 540, 50);
			GUITextButton next = new GUITextButton(getState(), 200, 20, "Request next " + PlayerFactionController.MAX_NEWS_REQUEST_BATCH, new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(488);
						((GameClientState) getState()).getController().getClientChannel().requestNextNews();
					}
				}

				@Override
				public boolean isOccluded() {
					return false;
				}
			});
			bg.attach(next);
			next.getPos().x = 140;
			next.getPos().y = 12;
			next.setTextPos(40, 2);
			newsPostList.add(new GUIListElement(bg, bg, getState()));
		}
	}

	@Override
	public void onNewsReceived() {
		updateNeeded = true;
	}

	@Override
	public void onChange(boolean updateListDim) {
		updateNeeded = true;
	}
}
