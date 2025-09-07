package org.schema.game.client.view.gui.catalog.newcatalog;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIBlockSprite;
import org.schema.game.common.controller.ElementCountMap;
import org.schema.game.common.controller.observer.DrawerObservable;
import org.schema.game.common.controller.observer.DrawerObserver;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GUIBlueprintConsistenceScrollableList extends ScrollableTableList<TypeRowConsistanceItem> implements DrawerObserver {

	public static ElementCountMap currentRequestedBlockMap;
	ObjectArrayList<TypeRowConsistanceItem> items = new ObjectArrayList<TypeRowConsistanceItem>();
	private boolean resources;

	public GUIBlueprintConsistenceScrollableList(InputState state, GUIElement p) {
		super(state, 100, 100, p);
	}

	public boolean isResources() {
		return resources;
	}

	public void setResources(boolean resources) {
		this.resources = resources;
	}

	public void updateTypes(ElementCountMap currentRequestedBlockMap) {
		assert (currentRequestedBlockMap != null);
		items.clear();
		
		ElementCountMap resourceBlocks;
		
		if (resources) {
			resourceBlocks = new ElementCountMap();
			for (short type : ElementKeyMap.keySet) {
				int count = currentRequestedBlockMap.get(type);
				if (count > 0 && ElementKeyMap.isValidType(type)) {
					ElementCountMap infoRawBlocks = new ElementCountMap(ElementKeyMap.getInfo(type).getRawBlocks());
					infoRawBlocks.mult(count);
					resourceBlocks.add(infoRawBlocks);
				}
			}
		} else {
			resourceBlocks = currentRequestedBlockMap;
		}

		
		for (short type : ElementKeyMap.keySet) {
			int count = resourceBlocks.get(type);
			if (count > 0 && ElementKeyMap.isValidType(type)) {

				ElementKeyMap.getInfo(type);
				items.add(new TypeRowConsistanceItem(ElementKeyMap.getInfo(type), count));

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
	public void draw() {
		if (GUIBlueprintConsistenceScrollableList.currentRequestedBlockMap != null) {
			updateTypes(GUIBlueprintConsistenceScrollableList.currentRequestedBlockMap);
			flagDirty();
			GUIBlueprintConsistenceScrollableList.currentRequestedBlockMap = null;
		}
		super.draw();
	}

	@Override
	public void initColumns() {


		addColumn(Lng.str("Type"), 3, (o1, o2) -> o1.info.getName().compareToIgnoreCase(o2.info.getName()), true);
		addFixedWidthColumnScaledUI(Lng.str("Amount"), 110, (o1, o2) -> o1.getAmount() - o2.getAmount());

		addTextFilter(new GUIListFilterText<TypeRowConsistanceItem>() {

			@Override
			public boolean isOk(String input, TypeRowConsistanceItem listElement) {
				return listElement.info.getName().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH BY TYPE"),FilterRowStyle.FULL);
	}

	@Override
	protected Collection<TypeRowConsistanceItem> getElementList() {
		return items;
	}

	@Override
	public void updateListEntries(GUIElementList mainList,
	                              Set<TypeRowConsistanceItem> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final PlayerState player = ((GameClientState) getState()).getPlayer();
		int i = 0;
		for (final TypeRowConsistanceItem f : collection) {

			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			GUITextOverlayTable amountText = new GUITextOverlayTable(getState());
			amountText.setTextSimple(f.getAmount());

			GUIBlockSprite iconSprite = new GUIBlockSprite(getState(), f.info.getId());
			iconSprite.setScale(0.35f, 0.35f, 0);
			nameText.setTextSimple(f.info.getName());

			GUIClippedRow nameAnchorP = new GUIClippedRow(getState());

			nameAnchorP.attach(iconSprite);
			nameText.setPos(24, 5, 0);
			nameAnchorP.attach(nameText);

			amountText.setPos(4, 5, 0);

			PlayerMessageRow r = new PlayerMessageRow(getState(), f, nameAnchorP, amountText);

			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}

	@Override
	public void update(DrawerObservable observer, Object userdata,
	                   Object message) {
		flagDirty();
	}

	private class PlayerMessageRow extends Row {


		public PlayerMessageRow(InputState state, TypeRowConsistanceItem f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}


	}

}
