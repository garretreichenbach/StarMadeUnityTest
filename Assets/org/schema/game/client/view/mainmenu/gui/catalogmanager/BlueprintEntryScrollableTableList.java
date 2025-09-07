package org.schema.game.client.view.mainmenu.gui.catalogmanager;

import org.schema.common.util.CompareTools;
import org.schema.common.util.StringTools;
import org.schema.game.client.view.GameResourceLoader;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.data.blueprintnw.BlueprintEntry;
import org.schema.game.server.data.blueprintnw.BlueprintType;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;

import java.util.*;

public class BlueprintEntryScrollableTableList extends ScrollableTableList<BlueprintEntry> implements GUIActiveInterface {

    private List<BlueprintEntry> blueprints;
    public boolean updated;

    public BlueprintEntryScrollableTableList(InputState state, float width, float height, GUIElement element) {
        super(state, width, height, element);
        updateBlueprints();
    }

    @Override
    public void initColumns() {
        this.addColumn("Name", 18.0F, Comparator.comparing(o -> o.getName().toLowerCase(Locale.ROOT)));

        this.addColumn("Mass", 7.5F, (o1, o2) -> CompareTools.compare(o1.getMass(), o2.getMass()));

        this.addColumn("Price", 7.5F, (o1, o2) -> CompareTools.compare(o1.getPrice(), o2.getPrice()));

        this.addColumn("Type", 5.0F, Comparator.comparing(o -> o.getType().name().toLowerCase(Locale.ROOT)));

        this.addTextFilter(new GUIListFilterText<BlueprintEntry>() {
            public boolean isOk(String s, BlueprintEntry blueprint) {
                return blueprint.getName().toLowerCase(Locale.ROOT).contains(s.toLowerCase(Locale.ROOT));
            }
        }, ControllerElement.FilterRowStyle.LEFT);

        this.addDropdownFilter(new GUIListFilterDropdown<>(BlueprintTypeFilter.values()) {
            public boolean isOk(BlueprintTypeFilter type, BlueprintEntry blueprint) {
                return switch(type) {
                    case ALL -> true;
                    case SHIP -> blueprint.getType() == BlueprintType.SHIP;
                    case STATION -> blueprint.getType() == BlueprintType.SPACE_STATION;
                };
            }
        }, new CreateGUIElementInterface<>() {
            @Override
            public GUIElement create(BlueprintTypeFilter blueprintType) {
                GUIAnchor anchor = new GUIAnchor(getState(), 10.0F, 24.0F);
                GUITextOverlayTableDropDown dropDown;
                (dropDown = new GUITextOverlayTableDropDown(10, 10, getState())).setTextSimple(blueprintType.name());
                dropDown.setPos(4.0F, 4.0F, 0.0F);
                anchor.setUserPointer(blueprintType);
                anchor.attach(dropDown);
                return anchor;
            }

            @Override
            public GUIElement createNeutral() {
                return null;
            }
        }, ControllerElement.FilterRowStyle.RIGHT);

        this.activeSortColumnIndex = 0;
    }

    @Override
    protected Collection<BlueprintEntry> getElementList() {
        if(!updated) updateBlueprints();
        return blueprints;
    }

    public void updateBlueprints() {
        ElementKeyMap.initializeData(GameResourceLoader.getConfigInputFile());
        blueprints = BluePrintController.active.readBluePrints();
        flagDirty();
        updated = true;
    }

    @Override
    public void updateListEntries(GUIElementList list, Set<BlueprintEntry> set) {
        if(!updated) updateBlueprints();
        //setColumnHeight(30);
        for(final BlueprintEntry blueprint : set) {
            GUITextOverlayTable nameTextElement = new GUITextOverlayTable(this.getState());
            nameTextElement.setTextSimple(blueprint.getName());
            GUIClippedRow nameRowElement;
            (nameRowElement = new GUIClippedRow(this.getState())).attach(nameTextElement);
            nameTextElement.autoHeight = true;
            nameTextElement.autoWrapOn = nameRowElement;
            nameTextElement.setLimitTextDraw(3);

            GUITextOverlayTable massTextElement;
            (massTextElement = new GUITextOverlayTable(this.getState())).setTextSimple(StringTools.formatPointZero(blueprint.getMass()));
            GUIClippedRow massRowElement;
            (massRowElement = new GUIClippedRow(this.getState())).attach(massTextElement);
            massTextElement.autoHeight = true;
            massTextElement.autoWrapOn = massRowElement;
            massTextElement.setLimitTextDraw(3);

            GUITextOverlayTable priceTextElement;
            (priceTextElement = new GUITextOverlayTable(this.getState())).setTextSimple(StringTools.formatPointZero(blueprint.getPrice()));
            GUIClippedRow priceRowElement;
            (priceRowElement = new GUIClippedRow(this.getState())).attach(priceTextElement);
            priceTextElement.autoHeight = true;
            priceTextElement.autoWrapOn = priceRowElement;
            priceTextElement.setLimitTextDraw(3);

            GUITextOverlayTable typeTextElement;
            (typeTextElement = new GUITextOverlayTable(this.getState())).setTextSimple(blueprint.getType().name());
            GUIClippedRow typeRowElement;
            (typeRowElement = new GUIClippedRow(this.getState())).attach(typeTextElement);
            typeTextElement.autoHeight = true;
            typeTextElement.autoWrapOn = typeRowElement;
            typeTextElement.setLimitTextDraw(3);

            BlueprintEntryListRow blueprintListRow;
            (blueprintListRow = new BlueprintEntryListRow(this.getState(), blueprint, nameRowElement, massRowElement, priceRowElement, typeRowElement)).onInit();
            list.addWithoutUpdate(blueprintListRow);
        }
        list.updateDim();
    }

    public class BlueprintEntryListRow extends ScrollableTableList<BlueprintEntry>.Row {
        public BlueprintEntryListRow(InputState inputState, BlueprintEntry blueprint, GUIElement... guiElements) {
            super(inputState, blueprint, guiElements);
            this.highlightSelect = true;
            this.highlightSelectSimple = true;
            this.setAllwaysOneSelected(true);
        }
    }

    public enum BlueprintTypeFilter {
        ALL,
        SHIP,
        STATION
    }
}
