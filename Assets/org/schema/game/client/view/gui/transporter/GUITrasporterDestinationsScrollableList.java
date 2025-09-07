package org.schema.game.client.view.gui.transporter;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import org.schema.game.client.controller.GameClientController;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.elements.transporter.TransporterCollectionManager;
import org.schema.game.common.controller.observer.DrawerObservable;
import org.schema.game.common.controller.observer.DrawerObserver;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class GUITrasporterDestinationsScrollableList extends ScrollableTableList<TransporterDestinations> implements DrawerObserver {

	private TransporterCollectionManager transporter;

	private long lastRefresh;

	public GUITrasporterDestinationsScrollableList(InputState state, GUIElement p, TransporterCollectionManager transporter) {
		super(state, 100, 100, p);
		this.transporter = transporter;
		((GameClientController) getState().getController()).sectorEntitiesChangeObservable.addObserver(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		((GameClientController) getState().getController()).sectorEntitiesChangeObservable.deleteObserver(this);
		super.cleanUp();
	}

	@Override
	public void initColumns() {
		addColumn(Lng.str("Structure"), 3f, (o1, o2) -> (o1.target.getName()).compareTo(o2.target.getName()));
		addColumn(Lng.str("Name"), 3, (o1, o2) -> o1.name.compareTo(o2.name));
		addFixedWidthColumnScaledUI(Lng.str("Block"), 80, (o1, o2) -> o1.pos.compareTo(o2.pos));
		addTextFilter(new GUIListFilterText<TransporterDestinations>() {

			@Override
			public boolean isOk(String input, TransporterDestinations listElement) {
				return listElement.name.toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH BY NAME"), FilterRowStyle.FULL);
		addTextFilter(new GUIListFilterText<TransporterDestinations>() {

			@Override
			public boolean isOk(String input, TransporterDestinations listElement) {
				return listElement.target.getName().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH BY STRUCTURE"), FilterRowStyle.FULL);
		activeSortColumnIndex = 1;
	}

	@Override
	protected Collection<TransporterDestinations> getElementList() {
		return transporter.getActiveTransporterDestinations(transporter.getSegmentController());
	}

	@Override
	public void draw() {
		super.draw();
		long t = System.currentTimeMillis();
		if (t - lastRefresh > 1000) {
			flagDirty();
			lastRefresh = t;
		}
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<TransporterDestinations> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final PlayerState player = ((GameClientState) getState()).getPlayer();
		int i = 0;
		for (final TransporterDestinations f : collection) {
			GUITextOverlayTable structureText = new GUITextOverlayTable(getState());
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			GUITextOverlayTable posText = new GUITextOverlayTable(getState());
			assert (f.name != null);
			structureText.setTextSimple(f.target.getName());
			nameText.setTextSimple(f.name);
			posText.setTextSimple(f.pos.toStringPure());
			GUIClippedRow senderAnchorP = new GUIClippedRow(getState());
			senderAnchorP.attach(nameText);
			GUIClippedRow topicAnchorP = new GUIClippedRow(getState());
			topicAnchorP.attach(posText);
			structureText.getPos().y = 5;
			nameText.getPos().y = 5;
			posText.getPos().y = 5;
			TransporterDestinationsRow r = new TransporterDestinationsRow(getState(), f, structureText, senderAnchorP, topicAnchorP);
			r.expanded = new GUIElementList(getState());
			GUIAnchor c = new GUIAnchor(getState(), 100, 30);
			GUITextButton setButton = new GUITextButton(getState(), 140, 24, ColorPalette.OK, Lng.str("SET DESTINATION"), new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						transporter.setDestination(f.target.getUniqueIdentifier(), ElementCollection.getIndex(f.pos));
						transporter.sendDestinationUpdate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(725);
					}
				}

				@Override
				public boolean isOccluded() {
					return !isActive();
				}
			});
			setButton.onInit();
			c.attach(setButton);
			c.onInit();
			setButton.setPos(UIScale.getUIScale().smallinset, UIScale.getUIScale().smallinset, 0);
			r.expanded.add(new GUIListElement(c, c, getState()));
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
	protected boolean isFiltered(TransporterDestinations e) {
		return super.isFiltered(e) || (e.target == transporter.getSegmentController() && e.pos.equals(transporter.getControllerPos()));
	}

	@Override
	public void update(DrawerObservable observer, Object userdata, Object message) {
		flagDirty();
	}

	private class TransporterDestinationsRow extends Row {

		public TransporterDestinationsRow(InputState state, TransporterDestinations f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}
	}
}
