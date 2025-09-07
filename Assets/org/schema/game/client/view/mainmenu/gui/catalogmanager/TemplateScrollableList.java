package org.schema.game.client.view.mainmenu.gui.catalogmanager;

import org.schema.game.client.view.GameResourceLoader;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;

import java.io.File;
import java.util.*;

public class TemplateScrollableList extends ScrollableTableList<File> implements GUIActiveInterface {

    private List<File> templates;
    public boolean updated;

    public TemplateScrollableList(InputState state, float width, float height, GUIElement element) {
        super(state, width, height, element);
        updateTemplates();
    }

    @Override
    public void initColumns() {
        this.addColumn("Name", 30.0F, Comparator.comparing(o -> o.getName().toLowerCase(Locale.ROOT)));

        this.addTextFilter(new GUIListFilterText<File>() {
            public boolean isOk(String s, File template) {
                return template.getName().toLowerCase(Locale.ROOT).contains(s.toLowerCase(Locale.ROOT));
            }
        }, ControllerElement.FilterRowStyle.FULL);

        this.activeSortColumnIndex = 0;
    }

    @Override
    protected Collection<File> getElementList() {
        if(!updated) updateTemplates();
        return templates;
    }

    public void updateTemplates() {
        ElementKeyMap.initializeData(GameResourceLoader.getConfigInputFile());
        File templatesFolder = new File("./templates");
        if(!templatesFolder.exists()) templatesFolder.mkdirs();
        templates = new ArrayList<>();
        for(File f : templatesFolder.listFiles()) {
            if(f.getName().endsWith(".smtpl")) templates.add(f);
        }
        flagDirty();
        updated = true;
    }

    @Override
    public void updateListEntries(GUIElementList list, Set<File> set) {
        if(!updated) updateTemplates();
        setColumnsHeight(30);
        for(final File template : set) {
            String name = template.getName().substring(0, template.getName().indexOf(".smtpl"));
            GUITextOverlayTable nameTextElement = new GUITextOverlayTable(this.getState());
            nameTextElement.setTextSimple(name);
            GUIClippedRow nameRowElement;
            (nameRowElement = new GUIClippedRow(this.getState())).attach(nameTextElement);
            nameTextElement.autoHeight = true;
            nameTextElement.autoWrapOn = nameRowElement;
            nameTextElement.setLimitTextDraw(3);

            TemplateListRow templateListRow = new TemplateListRow(this.getState(), template, nameRowElement);
            templateListRow.onInit();
            list.addWithoutUpdate(templateListRow);
        }

        list.updateDim();
    }

    public class TemplateListRow extends ScrollableTableList<File>.Row {
        public TemplateListRow(InputState inputState, File template, GUIElement... guiElements) {
            super(inputState, template, guiElements);
            this.highlightSelect = true;
            this.highlightSelectSimple = true;
            this.setAllwaysOneSelected(true);
        }
    }
}
