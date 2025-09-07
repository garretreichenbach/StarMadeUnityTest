package org.schema.game.client.view.gui.leaderboard;

import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map.Entry;

import javax.vecmath.Vector4f;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.gamemode.battle.KillerEntity;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIChangeListener;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangle;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GUIScrollableLoaderboardList extends GUIElement implements GUIChangeListener {

	private static boolean needsUpdate;

	int settingsWidth = 300;

	private GUIScrollablePanel leaderboardScrollPanel;

	private GUIElementList leaderboardList;

	private int width;

	private int height;

	private GUICallback onSelectCallBack;

	private boolean displayJoinOption;

	private boolean displayRelationShipOption;

	private GUITextButton nameSort;

	private GUITextButton kills;

	private boolean init;

	private GUITextButton deaths;

	public GUIScrollableLoaderboardList(InputState state, int width, int height, GUICallback onSelectCallBack) {
		super(state);
		this.width = width;
		this.height = height;
		this.onSelectCallBack = onSelectCallBack;
		getState().getGameState().leaderboardGUI = this;
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public void draw() {
		if (!init) {
			onInit();
			needsUpdate = false;
			assert (init);
		}
		if (needsUpdate) {
			updateLeaderboardList();
			needsUpdate = false;
		}
		drawAttached();
	}

	@Override
	public void onInit() {
		leaderboardScrollPanel = new GUIScrollablePanel(width - settingsWidth, height, getState());
		nameSort = new GUITextButton(getState(), GUILoaderboardElement.nameWidth, 20, new Vector4f(0.4f, 0.4f, 0.4f, 0.5f), new Vector4f(1, 1, 1, 1), FontSize.SMALL_15, "Name", new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					order(Order.NAME);
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
		kills = new GUITextButton(getState(), 50, 20, new Vector4f(0.4f, 0.4f, 0.4f, 0.5f), new Vector4f(1, 1, 1, 1), FontSize.SMALL_15, "Kills", new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					order(Order.KILLS);
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
		deaths = new GUITextButton(getState(), 50, 20, new Vector4f(0.4f, 0.4f, 0.4f, 0.5f), new Vector4f(1, 1, 1, 1), FontSize.SMALL_15, "Deaths", new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					order(Order.DEATHS);
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
		leaderboardList = new GUIElementList(getState());
		leaderboardList.setCallback(onSelectCallBack);
		leaderboardList.setMouseUpdateEnabled(true);
		leaderboardScrollPanel.setContent(leaderboardList);
		leaderboardScrollPanel.getPos().y = 20;
		updateLeaderboardList();
		kills.getPos().x = GUILoaderboardElement.nameWidth;
		deaths.getPos().x = GUILoaderboardElement.nameWidth + kills.getWidth();
		attach(nameSort);
		attach(kills);
		attach(deaths);
		// attach(npcSort);
		GUIColoredRectangle settingsPanel = new GUIColoredRectangle(getState(), settingsWidth - 32, 400, new Vector4f(0, 0, 0, 1));
		GUITextOverlay settings = new GUITextOverlay(getState());
		settings.setTextSimple(new Object() {

			/* (non-Javadoc)
			 * @see java.lang.Object#toString()
			 */
			@Override
			public String toString() {
				return getState().getGameState().getClientBattlemodeSettings();
			}
		});
		settingsPanel.attach(settings);
		settingsPanel.getPos().x = (leaderboardScrollPanel.getWidth() + 32);
		attach(leaderboardScrollPanel);
		attach(settingsPanel);
		init = true;
	}

	@Override
	public float getHeight() {
		return width;
	}

	@Override
	public GameClientState getState() {
		return (GameClientState) super.getState();
	}

	@Override
	public float getWidth() {
		return height;
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	public boolean isDisplayJoinOption() {
		return displayJoinOption;
	}

	public void setDisplayJoinOption(boolean displayJoinOption) {
		this.displayJoinOption = displayJoinOption;
	}

	/**
	 * @return the displayRelationShipOption
	 */
	public boolean isDisplayRelationShipOption() {
		return displayRelationShipOption;
	}

	/**
	 * @param displayRelationShipOption the displayRelationShipOption to set
	 */
	public void setDisplayRelationShipOption(boolean displayRelationShipOption) {
		this.displayRelationShipOption = displayRelationShipOption;
	}

	public void order(Order order) {
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
		AudioController.fireAudioEventID(539);
		order.comp = Collections.reverseOrder(order.comp);
		orderFixed(order);
	}

	public void orderFixed(Order order) {
		Collections.sort(leaderboardList, order.comp);
		nameSort.getColorText().set(0.7f, 0.7f, 0.7f, 0.7f);
		kills.getColorText().set(0.7f, 0.7f, 0.7f, 0.7f);
		switch(order) {
			case NAME:
				nameSort.getColorText().set(1, 1, 1, 1);
				break;
			case KILLS:
				kills.getColorText().set(1, 1, 1, 1);
				break;
			case DEATHS:
				break;
			default:
				break;
		}
		for (int i = 0; i < leaderboardList.size(); i++) {
			GUILeaderboardListElement il = (GUILeaderboardListElement) leaderboardList.get(i);
			((GUILeaderboardEnterableList) il.getContent()).updateIndex(i);
		}
	}

	private void updateLeaderboardList() {
		leaderboardList.clear();
		FactionManager factionManager = getState().getGameState().getFactionManager();
		int i = 0;
		for (final Entry<String, ObjectArrayList<KillerEntity>> f : getState().getGameState().getClientLeaderboard().entrySet()) {
			GUILearderboardElementList sub = new GUILearderboardElementList(getState(), f);
			GUIColoredRectangle p = new GUIColoredRectangle(getState(), 510, 80, GUILoaderboardElement.getRowColor(i, f, getState()));
			GUILeaderboardExtendedPanel pt = new GUILeaderboardExtendedPanel(getState(), f);
			pt.onInit();
			p.attach(pt);
			sub.add(new GUIListElement(p, p, getState()));
			GUILeaderboardEnterableList enterList = new GUILeaderboardEnterableList(getState(), sub, new GUILoaderboardElement(getState(), f, "+", i, onSelectCallBack), new GUILoaderboardElement(getState(), f, "-", i, onSelectCallBack), p);
			enterList.addObserver(this);
			leaderboardList.add(new GUILeaderboardListElement(enterList, enterList, getState(), f));
			i++;
		}
	}

	public void updateEntries() {
		GUIScrollableLoaderboardList.needsUpdate = true;
	}

	enum Order {

		NAME((o1, o2) -> {
			return ((GUILeaderboardListElement) o1).getEntry().getKey().toLowerCase(Locale.ENGLISH).compareTo(((GUILeaderboardListElement) o2).getEntry().getKey().toLowerCase(Locale.ENGLISH));
		}), KILLS((o1, o2) -> ((GUILeaderboardListElement) o1).getEntry().getValue().size() - (((GUILeaderboardListElement) o2).getEntry().getValue().size())), DEATHS((o1, o2) -> ((GUILeaderboardListElement) o1).getDeaths() - (((GUILeaderboardListElement) o2).getDeaths()));

		Comparator<GUIListElement> comp;

		private Order(Comparator<GUIListElement> comp) {
			this.comp = comp;
		}
	}

	@Override
	public void onChange(boolean updateListDim) {
		if (updateListDim) {
			leaderboardList.updateDim();
		}
	}
}
