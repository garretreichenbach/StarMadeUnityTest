package org.schema.game.client.view.gui.catalog.newcatalog;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import org.schema.game.client.controller.PlayerBlockStorageMetaDialog;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIBlockSprite;
import org.schema.game.common.controller.observer.DrawerObservable;
import org.schema.game.common.controller.observer.DrawerObserver;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.BlockStorageMetaItem;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;
import org.schema.schine.network.client.ClientState;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GUIBlockStorageMetaItemFillScrollableList extends ScrollableTableList<TypeRowStorageItem> implements DrawerObserver {

	ObjectArrayList<TypeRowStorageItem> items = new ObjectArrayList<TypeRowStorageItem>();

	private BlockStorageMetaItem item;

	private PlayerBlockStorageMetaDialog dialog;

	public GUIBlockStorageMetaItemFillScrollableList(InputState state, GUIElement p, PlayerBlockStorageMetaDialog dialog, BlockStorageMetaItem item) {
		super(state, 100, 100, p);
		this.item = item;
		this.dialog = dialog;
	}

	public void updateTypes() {
		items.clear();
		for (int j = 0; j < ElementKeyMap.highestType + 1; j++) {
			final short type = (short) j;
			if (ElementKeyMap.isValidType(type)) {
				ElementKeyMap.getInfo(type);
				final ElementInformation info = ElementKeyMap.getInfo(type);
				items.add(new TypeRowStorageItem(info, item.getId(), (GameClientState) getState()));
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		super.cleanUp();
	}

	@Override
	public void initColumns() {
		addColumn(Lng.str("Type"), 3, (o1, o2) -> o1.info.getName().compareToIgnoreCase(o2.info.getName()));
		ScrollableTableList<TypeRowStorageItem>.Column ss = addFixedWidthColumnScaledUI(Lng.str("Count"), 64, (o1, o2) -> o1.getCount() - o2.getCount(), true);
		ss.reverseOrders();
		addFixedWidthColumnScaledUI(Lng.str("Options"), 150, (o1, o2) -> 0);
		addTextFilter(new GUIListFilterText<TypeRowStorageItem>() {

			@Override
			public boolean isOk(String input, TypeRowStorageItem listElement) {
				return listElement.info.getName().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH BY TYPE"), FilterRowStyle.FULL);
	}

	@Override
	protected Collection<TypeRowStorageItem> getElementList() {
		updateTypes();
		return items;
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<TypeRowStorageItem> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final PlayerState player = ((GameClientState) getState()).getPlayer();
		int i = 0;
		for (final TypeRowStorageItem f : collection) {
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			GUIBlockSprite iconSprite = new GUIBlockSprite(getState(), f.info.getId());
			iconSprite.setScale(0.35f, 0.35f, 0);
			nameText.setTextSimple(f.info.getName());
			GUIClippedRow nameAnchorP = new GUIClippedRow(getState());
			nameAnchorP.attach(iconSprite);
			nameText.setPos(24, 5, 0);
			nameAnchorP.attach(nameText);
			GUITextOverlayTable processText = new GUITextOverlayTable(getState());
			processText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return String.valueOf(f.getCount());
				}
			});
			processText.getPos().y = 5;
			GUIAnchor buttonAncor = new GUIAnchor(getState(), 10, 10);
			GUITextButton getButton = new GUITextButton(getState(), 60, 24, ColorPalette.OK, Lng.str("GET"), new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(404);
						dialog.pressedGet(f.info.getId());
					}
				}

				@Override
				public boolean isOccluded() {
					return !isActive();
				}
			});
			GUITextButton addButton = new GUITextButton(getState(), 60, 24, ColorPalette.OK, Lng.str("ADD"), new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(405);
						dialog.pressedAdd(f.info.getId());
					}
				}

				@Override
				public boolean isOccluded() {
					return !isActive();
				}
			}) {

				@Override
				public void draw() {
					if (((ClientState) getState()).isAdmin()) {
						super.draw();
					}
				}
			};
			buttonAncor.attach(getButton);
			addButton.getPos().set(getButton.getWidth(), 0, 0);
			buttonAncor.attach(addButton);
			PlayerMessageRow r = new PlayerMessageRow(getState(), f, nameAnchorP, processText, buttonAncor);
			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}

	@Override
	public void update(DrawerObservable observer, Object userdata, Object message) {
		flagDirty();
	}

	private class PlayerMessageRow extends Row {

		public PlayerMessageRow(InputState state, TypeRowStorageItem f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}
	}
}
