package org.schema.game.client.view.gui.faction.newfaction;

import java.util.Collection;
import java.util.HashSet;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import org.schema.game.client.controller.ClientChannel.FactionNewsPostListener;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.player.faction.FactionNewsPost;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIChangeListener;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterPos;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTableInnerDescription;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.graphicsengine.forms.gui.newgui.config.GuiDateFormats;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class FactionNewsScrollableListNew extends ScrollableTableList<FactionNewsPost> implements GUIChangeListener, FactionNewsPostListener {

	private Faction faction;

	private FactionManager factionManager;

	public FactionNewsScrollableListNew(InputState state, GUIElement p, Faction f) {
		super(state, 100, 100, p);
		this.faction = f;
		this.factionManager = getState().getFactionManager();
		this.setColumnsHeight(UIScale.getUIScale().scale(36));
		factionManager.obs.addObserver(this);
		if (getState().getController().getClientChannel() == null) {
		} else {
			getState().getController().getClientChannel().factionNewsListeners.add(this);
		}
		getState().getPlayer().getFactionController().addObserver(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		factionManager.obs.deleteObserver(this);
		if (getState().getController().getClientChannel() == null) {
		} else {
			getState().getController().getClientChannel().factionNewsListeners.remove(this);
		}
		getState().getPlayer().getFactionController().deleteObserver(this);
		super.cleanUp();
	}

	@Override
	public void initColumns() {
		addColumn(Lng.str("News"), 3, (o1, o2) -> o1.getDate() > o2.getDate() ? 1 : (o1.getDate() < o2.getDate() ? -1 : 0));
		addButton(new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(464);
					getState().getController().getClientChannel().requestNextNews();
				}
			}

			@Override
			public boolean isOccluded() {
				return !isActive();
			}
		}, Lng.str("LOAD MORE"), FilterRowStyle.FULL, FilterPos.BOTTOM);
	}

	@Override
	protected Collection<FactionNewsPost> getElementList() {
		TreeSet<FactionNewsPost> treeSet = getState().getFactionManager().getNews().get(faction.getIdFaction());
		assert (getState().getController().getClientChannel() != null);
		assert (getState().getController().getClientChannel().getFactionNews() != null);
		if (treeSet != null) {
			treeSet.addAll(getState().getController().getClientChannel().getFactionNews());
		} else {
			treeSet = getState().getController().getClientChannel().getFactionNews();
		}
		if (treeSet == null) {
			return new HashSet();
		}
		NavigableSet<FactionNewsPost> descendingSet = treeSet.descendingSet();
		return descendingSet;
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<FactionNewsPost> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final FactionManager factionManager = getState().getGameState().getFactionManager();
		final PlayerState player = getState().getPlayer();
		int i = 0;
		for (final FactionNewsPost f : collection) {
			final GUITextOverlayTable topicText = new GUITextOverlayTable(getState());
			topicText.setTextSimple(f.getTopic());
			final GUITextOverlayTable prevText = new GUITextOverlayTable(getState());
			int maxSize = 50;
			boolean nl = f.getMessage().indexOf("\n") > 0;
			String msgPrev = nl ? f.getMessage().substring(0, f.getMessage().indexOf("\n") - 1) : f.getMessage();
			prevText.setTextSimple((msgPrev.length() < maxSize ? msgPrev + (nl ? Lng.str("... (click to load)") : "") : (msgPrev.subSequence(0, maxSize - 1) + Lng.str("... (click to load)"))));
			final GUITextOverlayTable dateText = new GUITextOverlayTable(getState());
			dateText.setTextSimple(GuiDateFormats.factionNewsTime.format(f.getDate()));
			final int textWidth = dateText.getFont().getWidth(dateText.getText().get(0).toString());
			GUIAnchor tag = new GUIAnchor(getState(), 100, 32) {

				@Override
				public void draw() {
					setWidth(FactionNewsScrollableListNew.this.columns.get(0).bg.getWidth());
					topicText.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
					prevText.setPos(4, 20, 0);
					dateText.setPos(getWidth() - textWidth - 18, 4, 0);
					super.draw();
				}
			};
			tag.attach(topicText);
			tag.attach(prevText);
			tag.attach(dateText);
			FactionRow r = new FactionRow(getState(), f, tag) {

				/* (non-Javadoc)
				 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList.Row#draw()
				 */
				@Override
				public void draw() {
					if (l.isExpended()) {
						prevText.setColor(0, 0, 0, 0);
					} else {
						prevText.setColor(0.8f, 0.8f, 0.8f, 1);
					}
					super.draw();
				}
			};
			r.expanded = new GUIElementList(getState());
			GUITextOverlayTableInnerDescription post = new GUITextOverlayTableInnerDescription(10, 10, getState());
			post.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f.getMessage();
				}
			});
			GUIAnchor c = new GUIAnchor(getState(), 100, 100);
			GUITextButton mailButton = new GUITextButton(getState(), 80, 24, ColorPalette.CANCEL, Lng.str("DELETE"), new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.DELETE)*/
						AudioController.fireAudioEventID(465);
						getState().getPlayer().getFactionController().removeNewsClient(f);
					}
				}
			}) {

				/* (non-Javadoc)
				 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
				 */
				@Override
				public void draw() {
					if (player.getFactionController().hasDescriptionAndNewsPostPermission()) {
						super.draw();
					}
				}
			};
			mailButton.setPos(0, c.getHeight(), 0);
			c.attach(post);
			c.attach(mailButton);
			r.expanded.add(new GUIListElement(c, c, getState()));
			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#getState()
	 */
	@Override
	public GameClientState getState() {
		return (GameClientState) super.getState();
	}

	private class FactionRow extends Row {

		public FactionRow(InputState state, FactionNewsPost f, GUIElement... elements) {
			super(state, f, elements);
			customColumnHeightExpanded = 16;
			this.highlightSelect = true;
		}
	}

	@Override
	public void onNewsReceived() {
		onChange(true);
	}
}
