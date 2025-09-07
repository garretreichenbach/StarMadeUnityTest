package api.mod.gui;

/**
 * Created by Jake on 9/20/2020.
 * <insert description here>
 */

import api.mod.ModIdentifier;
import api.mod.ModUpdater;
import api.mod.SMDModData;
import api.mod.SMDModInfo;
import api.utils.gui.SimplePopup;
import api.utils.other.LangUtil;
import org.schema.common.util.CompareTools;
import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.game.common.util.DesktopUtils;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class SMDModEntryScrollableTableList extends ScrollableTableList<SMDModInfo> implements GUIActiveInterface {

    private Collection<SMDModInfo> mods = new ArrayList<>();
    public boolean updated;

    private boolean boxOpen = false;

    public SMDModEntryScrollableTableList(InputState state, float width, float height,  GUIElement element) {
        super(state, width, height, element);
        updateMods();
    }

    @Override
    public void initColumns() {
        setColumnsHeight(50);
        this.addColumn("Icon", 1.35F, (o1, o2) -> LangUtil.stringsCompareTo(o1.getName(), o2.getName()));

        this.addColumn("Name", 3.75F, (o1, o2) -> LangUtil.stringsCompareTo(o1.getName(), o2.getName()));

        this.addColumn("Description", 7.5F, (o1, o2) -> LangUtil.stringsCompareTo(o1.getTagLine(), o2.getTagLine()));

        this.addColumn("Rating", 1.0F, (o1, o2) -> CompareTools.compare(o1.getRatingAverage(), o2.getRatingAverage()));

        this.addColumn("Author", 3.15F, (o1, o2) -> o1.getUsername().compareToIgnoreCase(o2.getUsername()));

        this.addColumn("Downloads", 3.15F, (o1, o2) -> CompareTools.compare(o1.getDownloadCount(), o2.getDownloadCount()));

        this.addTextFilter(new GUIListFilterText<SMDModInfo>() {
            public boolean isOk(String s, SMDModInfo blueprint) {
                return LangUtil.stringsContain(blueprint.getName(), s);
            }
        }, ControllerElement.FilterRowStyle.FULL);

        this.activeSortColumnIndex = 0;
    }

    @Override
    protected Collection<SMDModInfo> getElementList() {
        if(!updated) updateMods();
        return mods;
    }

    public void updateMods() {
        mods.clear();
        for (SMDModInfo modDataValue : SMDModData.getInstance().getModDataValues()) {
            if(modDataValue.getTags().contains("starloader")) {
                mods.add(modDataValue);
            }
        }
        flagDirty();
        updated = true;
    }


    @Override
    public void updateListEntries(GUIElementList list, Set<SMDModInfo> set) {
        if(!updated) updateMods();
        //setColumnHeight(70);
        for(final SMDModInfo mod : set) {
            GUIModIcon icon = new GUIModIcon(this.getState(), mod.getIconURL());
            GUIClippedRow iconRowElement = new GUIClippedRow(this.getState());
            iconRowElement.attach(icon);
            icon.autoWrapOn = iconRowElement;
            icon.autoHeight = true;

            GUITextOverlayTable nameTextElement = new GUITextOverlayTable(this.getState());
            nameTextElement.setTextSimple(mod.getName());
            GUIClippedRow nameRowElement;
            (nameRowElement = new GUIClippedRow(this.getState())).attach(nameTextElement);
            nameTextElement.autoWrapOn = nameRowElement;
            nameTextElement.autoHeight = true;
            nameTextElement.setLimitTextDraw(3);

            GUITextOverlayTable descriptionTextElement = new GUITextOverlayTable(this.getState());
            descriptionTextElement.setTextSimple(mod.getTagLine());
            GUIClippedRow descriptionRowElement = new GUIClippedRow(this.getState());
            descriptionRowElement.attach(descriptionTextElement);
            descriptionTextElement.autoWrapOn = descriptionRowElement;
            descriptionTextElement.autoHeight = true;
            descriptionTextElement.setLimitTextDraw(3);

            GUITextOverlayTable ratingTextElement = new GUITextOverlayTable(this.getState());
            ratingTextElement.setTextSimple(mod.getRatingAverage());
            GUIClippedRow ratingRowElement = new GUIClippedRow(this.getState());
            ratingRowElement.attach(ratingTextElement);
            ratingTextElement.autoWrapOn = ratingRowElement;
            ratingTextElement.autoHeight = true;
            ratingTextElement.setLimitTextDraw(3);

            GUITextOverlayTable authorTextElement = new GUITextOverlayTable(this.getState());
            authorTextElement.setTextSimple(mod.getUsername());
            GUIClippedRow authorRowElement = new GUIClippedRow(this.getState());
            authorRowElement.attach(authorTextElement);
            authorTextElement.autoWrapOn = authorRowElement;
            authorTextElement.autoHeight = true;
            authorTextElement.setLimitTextDraw(3);

            GUITextOverlayTable downloadsTextElement = new GUITextOverlayTable(this.getState());
            downloadsTextElement.setTextSimple(mod.getDownloadCount() + " downloads");
            GUIClippedRow downloadsRowElement = new GUIClippedRow(this.getState());
            downloadsRowElement.attach(downloadsTextElement);
            downloadsTextElement.autoWrapOn = downloadsRowElement;
            downloadsTextElement.autoHeight = true;
            downloadsTextElement.setLimitTextDraw(3);

            final ModEntryListRow modListRow = new ModEntryListRow(getState(), mod, iconRowElement, nameRowElement, descriptionRowElement, ratingTextElement, authorTextElement, downloadsTextElement);
            modListRow.expanded = new GUIElementList(getState());
            GUIAnchor anchor = new GUIAnchor(getState(), getWidth() - 107.0f, 24.0f) {
                @Override
                public void draw() {
                    super.draw();
                    setWidth(modListRow.getWidth());
                }
            };
            anchor.attach(redrawButtonPane(mod, anchor));
            modListRow.expanded.add(new GUIListElement(anchor, getState()));
            modListRow.expanded.attach(anchor);
            modListRow.onInit();
            list.addWithoutUpdate(modListRow);
        }
        list.updateDim();
    }

    public GUIHorizontalButtonTablePane redrawButtonPane(final SMDModInfo modInfo, GUIAnchor anchor) {
        GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 2, 1, anchor);
        buttonPane.onInit();
        buttonPane.addButton(0, 0, "DOWNLOAD", GUIHorizontalArea.HButtonColor.GREEN, new GUICallback() {
            @Override
            public void callback(GUIElement callingGuiElement, MouseEvent mouseEvent) {
                if(mouseEvent.pressedLeftMouse()) {
                    //getState().getController().queueUIAudio("0022_menu_ui - enter");
                    PlayerOkCancelInput confirmBox = new PlayerOkCancelInput("CONFIRM", getState(), 500, 300, "Confirm Download", "Do you wish to download \"" + modInfo.getName() + "\"?") {
                        @Override
                        public void onDeactivate() {
                            boxOpen = false;
                        }

                        @Override
                        public void pressedOK() {
                            try {
                                String latestDL = modInfo.getLatestDownloadVersion();
                                System.err.println("Latest DL: " + latestDL);
                                ModUpdater.downloadAndLoadMod(new ModIdentifier(modInfo.getResourceId(), latestDL), null);
                                this.deactivate();
                                new SimplePopup(getState(), "Success", "Successfully downloaded mod \"" + modInfo.getName() + "\".");
                                //getState().getController().queueUIAudio("0022_menu_ui - enter");
                                if(InstalledModEntryScrollableTableList.getInst() != null) {
                                    InstalledModEntryScrollableTableList.getInst().clear();
                                    InstalledModEntryScrollableTableList.getInst().handleDirty();
                                }
                            } catch(IOException e) {
                                this.deactivate();
                                e.printStackTrace();
                                new SimplePopup(getState(), "Error", "Could not download mod \"" + modInfo.getName() + "\" due to an unexpected error.");
                                //getState().getController().queueUIAudio("0022_menu_ui - error 2");
                            }
                        }
                    };
                    confirmBox.getInputPanel().onInit();
                    confirmBox.getInputPanel().background.setPos(470.0F, 35.0F, 0.0F);
                    confirmBox.getInputPanel().background.setWidth((float) (GLFrame.getWidth() - 435));
                    confirmBox.getInputPanel().background.setHeight((float) (GLFrame.getHeight() - 70));
                    confirmBox.activate();
                    boxOpen = true;
                }
            }

            @Override
            public boolean isOccluded() {
                return boxOpen;
            }
        }, new GUIActivationCallback() {
            @Override
            public boolean isVisible(InputState state) {
                return true;
            }

            @Override
            public boolean isActive(InputState state) {
                return !boxOpen;
            }
        });

        buttonPane.addButton(1, 0, "VIEW ON SMD", GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
            @Override
            public void callback(GUIElement callingGuiElement, MouseEvent mouseEvent) {
                if(mouseEvent.pressedLeftMouse()) {
                    String url = "http://starmadedock.net/content/" + modInfo.getResourceId() + "/";
                    try {
//                        Desktop.getDesktop().browse(new URL(url).toURI());
                        DesktopUtils.browseURL(new URI(url));
                    } catch(Exception exception) {
                        exception.printStackTrace();
                        new SimplePopup(getState(), "Error", "Could not open link \"" + url + "\" due to an unexpected error.");
                        //getState().getController().queueUIAudio("0022_menu_ui - error 2");
                    }
                }
            }

            @Override
            public boolean isOccluded() {
                return boxOpen;
            }
        }, new GUIActivationCallback() {
            @Override
            public boolean isVisible(InputState state) {
                return true;
            }

            @Override
            public boolean isActive(InputState state) {
                return !boxOpen;
            }
        });

        return buttonPane;
    }

    public class ModEntryListRow extends ScrollableTableList<SMDModInfo>.Row {
        public ModEntryListRow(InputState inputState, SMDModInfo blueprint, GUIElement... guiElements) {
            super(inputState, blueprint, guiElements);
            this.highlightSelect = true;
            this.highlightSelectSimple = true;
            this.setAllwaysOneSelected(true);
        }
    }
}

