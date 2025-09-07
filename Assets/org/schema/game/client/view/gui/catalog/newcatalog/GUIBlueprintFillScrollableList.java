package org.schema.game.client.view.gui.catalog.newcatalog;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.client.controller.PlayerBlueprintMetaDialog;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIBlockSprite;
import org.schema.game.common.controller.observer.DrawerObservable;
import org.schema.game.common.controller.observer.DrawerObserver;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.BlueprintMetaItem;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalProgressBar;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.Set;

public class GUIBlueprintFillScrollableList extends ScrollableTableList<TypeRowItem> implements DrawerObserver {

	ObjectArrayList<TypeRowItem> items = new ObjectArrayList<TypeRowItem>();

	private BlueprintMetaItem item;

	private PlayerBlueprintMetaDialog dialog;

	public GUIBlueprintFillScrollableList(InputState state, GUIElement p, PlayerBlueprintMetaDialog dialog, BlueprintMetaItem item) {
		super(state, 100, 100, p);
		this.item = item;
		this.dialog = dialog;
	}

	public void updateTypes() {
		items.clear();
		for (int j = 0; j < ElementKeyMap.highestType + 1; j++) {
			final short type = (short) j;
			if (item.goal.get(type) > 0 && ElementKeyMap.isValidType(type)) {
				ElementKeyMap.getInfo(type);
				final ElementInformation info = ElementKeyMap.getInfo(type);
				items.add(new TypeRowItem(info, item.getId(), (GameClientState) getState()));
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
		addColumn(Lng.str("Type"), 3, (o1, o2) -> o1.info.getName().compareToIgnoreCase(o2.info.getName()), true);
		addFixedWidthColumnScaledUI(Lng.str("Provided"), 64, Comparator.comparingInt(TypeRowItem::getProgress));
		addFixedWidthColumnScaledUI(Lng.str("Goal"), 64, Comparator.comparingInt(TypeRowItem::getGoal));
		addFixedWidthColumnScaledUI(Lng.str("Progress"), 100, (o1, o2) -> Float.compare(o1.getPercent(), o2.getPercent()));
		addFixedWidthColumnScaledUI(Lng.str("Options"), 100, (o1, o2) -> 0);
		addTextFilter(new GUIListFilterText<TypeRowItem>() {

			@Override
			public boolean isOk(String input, TypeRowItem listElement) {
				return listElement.info.getName().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH BY TYPE"), FilterRowStyle.FULL);
	}

	@Override
	protected Collection<TypeRowItem> getElementList() {
		updateTypes();
		return items;
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<TypeRowItem> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final PlayerState player = ((GameClientState) getState()).getPlayer();
		int i = 0;
		for (final TypeRowItem f : collection) {
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			GUIBlockSprite iconSprite = new GUIBlockSprite(getState(), f.info.getId());
			iconSprite.setScale(0.35f, 0.35f, 0);
			nameText.setTextSimple(f.info.getName() + (f.info.getSourceReference() != 0 ? Lng.str(" (needs %s)", ElementKeyMap.getInfoFast(f.info.getSourceReference()).getName()) : ""));
			GUIClippedRow nameAnchorP = new GUIClippedRow(getState());
			nameAnchorP.attach(iconSprite);
			nameText.setPos(24, 5, 0);
			nameAnchorP.attach(nameText);
			GUITextOverlayTable processText = new GUITextOverlayTable(getState());
			GUITextOverlayTable goalText = new GUITextOverlayTable(getState());
			processText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return String.valueOf(f.getProgress());
				}
			});
			goalText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return String.valueOf(f.getGoal());
				}
			});
			processText.getPos().y = 5;
			goalText.getPos().y = 5;
			GUIAnchor buttonAncor = new GUIAnchor(getState(), 10, 10);
			GUITextButton addButton = new GUITextButton(getState(), 80, 24, ColorPalette.OK, Lng.str("ADD"), new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(406);
						dialog.pressedAdd(f.info.getId());
					}
				}

				@Override
				public boolean isOccluded() {
					return !isActive();
				}
			});
			GUIClippedRow progressAncor = new GUIClippedRow(getState());
			GUIHorizontalProgressBar progress = new GUIHorizontalProgressBar(getState(), progressAncor) {

				@Override
				public float getValue() {
					return f.getPercent();
				}
			};
			progress.getColor().set(0.3f, 1, 0, 1);
			progress.setDisplayPercent(true);
			progressAncor.attach(progress);
			buttonAncor.attach(addButton);
			PlayerMessageRow r = new PlayerMessageRow(getState(), f, nameAnchorP, processText, goalText, progressAncor, buttonAncor);
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

		public PlayerMessageRow(InputState state, TypeRowItem f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}
	}
}
