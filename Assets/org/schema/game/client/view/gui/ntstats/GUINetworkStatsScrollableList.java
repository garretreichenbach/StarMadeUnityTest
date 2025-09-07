package org.schema.game.client.view.gui.ntstats;

import java.util.Collection;
import java.util.Set;

import org.schema.common.util.StringTools;
import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.observer.DrawerObservable;
import org.schema.game.common.controller.observer.DrawerObserver;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIStatisticsGraph;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.graphicsengine.forms.gui.newgui.config.GuiDateFormats;
import org.schema.schine.input.InputState;
import org.schema.schine.network.DataStatsEntry;
import org.schema.schine.network.DataStatsManager;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GUINetworkStatsScrollableList extends ScrollableTableList<DataStatsEntry> implements DrawerObserver {

	private final DataDisplayMode mode;

	private final ObjectArrayList<DataStatsEntry> data;

	private final DataStatsManager man;

	private GUIStatisticsGraph statPanel;

	public GUINetworkStatsScrollableList(InputState state, GUIElement p, DataDisplayMode mode, DataStatsManager man, GUIStatisticsGraph statPanel) {
		super(state, 100, 100, p);
		this.mode = mode;
		this.man = man;
		this.statPanel = statPanel;
		if (mode == DataDisplayMode.RECEIVED) {
			data = man.getReceivedData();
		} else {
			data = man.getSentData();
		}
		man.addObserver(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		man.deleteObserver(this);
		super.cleanUp();
	}

	@Override
	public void initColumns() {
		addColumn("Time", 1f, (o1, o2) -> o1.time > o2.time ? 1 : (o1.time < o2.time ? -1 : 0));
		addFixedWidthColumnScaledUI("Volume", 100, (o1, o2) -> o1.volume > o2.volume ? 1 : (o1.volume < o2.volume ? -1 : 0));
		addFixedWidthColumnScaledUI("Details", 100, (o1, o2) -> 0);
	}

	@Override
	protected Collection<DataStatsEntry> getElementList() {
		return data;
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<DataStatsEntry> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final PlayerState player = ((GameClientState) getState()).getPlayer();
		int i = 0;
		for (final DataStatsEntry f : collection) {
			GUITextOverlayTable dateText = new GUITextOverlayTable(getState());
			GUITextOverlayTable volumeText = new GUITextOverlayTable(getState());
			volumeText.setTextSimple(StringTools.readableFileSize(f.volume));
			dateText.setTextSimple(GuiDateFormats.ntStatTime.format(f.time));
			GUIClippedRow dateAnchorP = new GUIClippedRow(getState());
			dateAnchorP.attach(dateText);
			GUIClippedRow volumeAnchorP = new GUIClippedRow(getState());
			volumeAnchorP.attach(volumeText);
			volumeText.getPos().y = 5;
			dateText.getPos().y = 5;
			GUITextButton detailsButton = new GUITextButton(getState(), 76, 20, ColorPalette.OK, "DETAILS", new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						openDetailDialog(f);
					}
				}

				@Override
				public boolean isOccluded() {
					return !GUINetworkStatsScrollableList.this.isActive();
				}
			});
			StatRow r = new StatRow(getState(), f, dateAnchorP, volumeAnchorP, detailsButton);
			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#isFiltered(java.lang.Object)
	 */
	@Override
	protected boolean isFiltered(DataStatsEntry e) {
		if (!super.isFiltered(e)) {
			if (statPanel.isSelected()[mode.ordinal()]) {
				return !e.selected;
			}
		}
		return super.isFiltered(e);
	}

	@Override
	public void update(DrawerObservable observer, Object userdata, Object message) {
		flagDirty();
	}

	private void openDetailDialog(final DataStatsEntry f) {
		PlayerGameOkCancelInput c = new PlayerGameOkCancelInput("NETSTATSDETAILDIAG", (GameClientState) getState(), 800, 450, Lng.str("Details"), "") {

			@Override
			public void pressedOK() {
				String savePath = f.save(mode == DataDisplayMode.SENT);
				PlayerGameOkCancelInput cs = new PlayerGameOkCancelInput("CONFIRM", getState(), 300, 200, Lng.str("Saved"), Lng.str("Packet data has been successfully saved\nto %s", savePath)) {

					@Override
					public void onDeactivate() {
					}

					@Override
					public void pressedOK() {
						deactivate();
					}
				};
				cs.getInputPanel().setCancelButton(false);
				cs.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(591);
			}

			@Override
			public void onDeactivate() {
			}
		};
		c.getInputPanel().setOkButtonText(Lng.str("SAVE"));
		c.getInputPanel().onInit();
		GUINetworkStatsDetailsScrollableList g = new GUINetworkStatsDetailsScrollableList(getState(), ((GUIDialogWindow) c.getInputPanel().background).getMainContentPane().getContent(0), f);
		g.onInit();
		((GUIDialogWindow) c.getInputPanel().background).getMainContentPane().getContent(0).attach(g);
		c.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(592);
	}

	public enum DataDisplayMode {

		RECEIVED, SENT
	}

	private class StatRow extends Row {

		public StatRow(InputState state, DataStatsEntry f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}
	}
}
