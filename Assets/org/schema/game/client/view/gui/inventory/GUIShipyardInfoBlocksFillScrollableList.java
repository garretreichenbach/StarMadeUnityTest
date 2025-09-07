package org.schema.game.client.view.gui.inventory;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.client.controller.PlayerShipyardInfoDialog;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIBlockSprite;
import org.schema.game.common.controller.elements.shipyard.ShipyardCollectionManager;
import org.schema.game.common.controller.observer.DrawerObservable;
import org.schema.game.common.controller.observer.DrawerObserver;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalProgressBar;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;

import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.Set;

public class GUIShipyardInfoBlocksFillScrollableList extends ScrollableTableList<ShipyardTypeRowItem> implements DrawerObserver {

    ObjectArrayList<ShipyardTypeRowItem> items = new ObjectArrayList<ShipyardTypeRowItem>();
    private ShipyardCollectionManager item;

    public GUIShipyardInfoBlocksFillScrollableList(InputState state, GUIElement p, PlayerShipyardInfoDialog dialog, ShipyardCollectionManager item) {
        super(state, 100, 100, p);
        this.item = item;
        item.drawObserver = this;
    }

    public void updateTypes() {
        items.clear();
        for(int j = 0; j < ElementKeyMap.highestType + 1; j++) {
            final short type = (short) j;
            if(item.clientGoalFrom.get(type) > 0 && ElementKeyMap.isValidType(type)) {
                ElementKeyMap.getInfo(type);
                final ElementInformation info = ElementKeyMap.getInfo(type);
                items.add(new ShipyardTypeRowItem(info, item, (GameClientState) getState()));
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
        addFixedWidthColumnScaledUI(Lng.str("Provided"), 64, Comparator.comparingInt(ShipyardTypeRowItem::getProgress));

        addFixedWidthColumnScaledUI(Lng.str("Goal"), 64, Comparator.comparingInt(ShipyardTypeRowItem::getGoal));
        addFixedWidthColumnScaledUI(Lng.str("Progress"), 100, (o1, o2) -> Float.compare(o1.getPercent(), o2.getPercent()));


        addTextFilter(new GUIListFilterText<ShipyardTypeRowItem>() {

            @Override
            public boolean isOk(String input, ShipyardTypeRowItem listElement) {
                return listElement.info.getName().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
            }
        }, Lng.str("SEARCH BY TYPE"), FilterRowStyle.FULL);
    }

    @Override
    protected Collection<ShipyardTypeRowItem> getElementList() {
        updateTypes();
        return items;
    }

    @Override
    public void updateListEntries(GUIElementList mainList,
                                  Set<ShipyardTypeRowItem> collection) {
        mainList.deleteObservers();
        mainList.addObserver(this);

        final PlayerState player = ((GameClientState) getState()).getPlayer();
        int i = 0;
        for(final ShipyardTypeRowItem f : collection) {

            GUITextOverlayTable nameText = new GUITextOverlayTable(getState());

            GUIBlockSprite iconSprite = new GUIBlockSprite(getState(), f.info.getId());
            iconSprite.setScale(0.35f, 0.35f, 0);
            nameText.setTextSimple(f.info.getName());

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

            PlayerMessageRow r = new PlayerMessageRow(getState(), f, nameAnchorP, processText, goalText, progressAncor);

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


        public PlayerMessageRow(InputState state, ShipyardTypeRowItem f, GUIElement... elements) {
            super(state, f, elements);
            this.highlightSelect = true;
        }


    }

}
