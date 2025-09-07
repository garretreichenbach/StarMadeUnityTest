package api.mod.gui;

import api.ModPlayground;
import api.SMModLoader;
import api.mod.ModIdentifier;
import api.mod.ModSkeleton;
import api.mod.SinglePlayerModData;
import api.mod.StarLoader;
import api.utils.other.LangUtil;
import org.schema.common.util.CompareTools;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Set;

/**
 * Scrollable GUI list for installed mods.
 *
 * @author TheDerpGamer
 * @version 1.2 - [09/27/2021]
 */
public class InstalledModEntryScrollableTableList extends ScrollableTableList<ModSkeleton> implements GUIActiveInterface {

	private static InstalledModEntryScrollableTableList inst;
	private static final BufferedImage defaultImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

	public InstalledModEntryScrollableTableList(InputState state, float width, float height, GUIElement element) {
		super(state, width, height, element);
		inst = this;
	}

	public static InstalledModEntryScrollableTableList getInst() {
		if(inst != null) return inst;
		return null;
	}

	@Override
	public void initColumns() {
		setColumnsHeight(50);
		addColumn("Icon", 0.5f, (o1, o2) -> LangUtil.stringsCompareTo(o1.getName(), o2.getName()));

		addColumn("Name", 3.5f, (o1, o2) -> LangUtil.stringsCompareTo(o1.getName(), o2.getName()));

		addColumn("Description", 8.0f, (o1, o2) -> LangUtil.stringsCompareTo(o1.getModDescription(), o2.getModDescription()));

		addColumn("Enabled", 1.0f, (o1, o2) -> CompareTools.compare(SinglePlayerModData.getInstance().isClientEnabled(ModIdentifier.fromMod(o1)), SinglePlayerModData.getInstance().isClientEnabled(ModIdentifier.fromMod(o2))));
		addColumn("Mod Type", 1.5f, (o1, o2) -> Boolean.compare(o1.isCoreMod(), o2.isCoreMod()));

		addTextFilter(new GUIListFilterText<ModSkeleton>() {
			public boolean isOk(String s, ModSkeleton blueprint) {
				return LangUtil.stringsContain(blueprint.getName(), s);
			}
		}, ControllerElement.FilterRowStyle.FULL);

		activeSortColumnIndex = 0;
	}

	@Override
	protected Collection<ModSkeleton> getElementList() {
		return StarLoader.starMods;
	}

	public void redrawList() {
		clear();
		handleDirty();
	}

	@Override
	public void updateListEntries(GUIElementList list, Set<ModSkeleton> set) {
		//setColumnHeight(60);
		for(ModSkeleton mod : set) {
			BufferedImage iconImage = mod.getIconImage();
			if(iconImage == null) iconImage = defaultImage;
			GUIIcon iconElement = new GUIIcon(getState(), iconImage);
			iconElement.setTextSimple("");
			GUIClippedRow iconRowElement;
			(iconRowElement = new GUIClippedRow(getState())).attach(iconElement);
			iconElement.autoWrapOn = iconRowElement;
			iconElement.autoHeight = true;

			GUITextOverlayTable nameTextElement = new GUITextOverlayTable(getState());
			String nameText = "  " + mod.getName() + " v" + mod.getModVersion();
			if(mod.isOutOfDate()) {
				nameText += "\n     [Out of Date]";
				nameTextElement.setAWTColor(Color.red);
			}
			nameTextElement.setTextSimple(nameText);
			GUIClippedRow nameRowElement;
			(nameRowElement = new GUIClippedRow(getState())).attach(nameTextElement);
			nameTextElement.autoWrapOn = nameRowElement;
			nameTextElement.autoHeight = true;
			nameTextElement.setLimitTextDraw(3);

			GUITextOverlayTable descriptionTextElement = new GUITextOverlayTable(getState());
			descriptionTextElement.setTextSimple(mod.getModDescription());
			GUIClippedRow descriptionRowElement = new GUIClippedRow(getState());
			descriptionRowElement.attach(descriptionTextElement);
			descriptionTextElement.autoWrapOn = descriptionRowElement;
			descriptionTextElement.autoHeight = true;
			descriptionTextElement.setLimitTextDraw(3);

			GUITextOverlayTable enabledTextElement = new GUITextOverlayTable(getState());
			enabledTextElement.setTextSimple(SinglePlayerModData.getInstance().isClientEnabled(ModIdentifier.fromMod(mod)));
			GUIClippedRow enabledRowElement = new GUIClippedRow(getState());
			enabledRowElement.attach(enabledTextElement);
			enabledTextElement.autoWrapOn = enabledRowElement;
			enabledTextElement.autoHeight = true;
			enabledTextElement.setLimitTextDraw(3);

			GUITextOverlayTable modTypeTextElement = new GUITextOverlayTable(getState());
			StringBuilder text = new StringBuilder();
			if(mod.isLocal()) text.append("[Local] ");
			if(mod.isCoreMod()) text.append("[Core] ");
			if(mod.getSmdResourceId() == -1) text.append("[Virtual]"); //Mods that are not .jar's. StarLoader and possibly one day a "Vanilla" mod.
			else if(mod.isClientMod()) text.append("[Client]");
			else if(mod.isServerMod()) text.append("[Server]");
			else text.append("[Normal]");

			modTypeTextElement.setTextSimple(text.toString());
			GUIClippedRow modTypeRowElement = new GUIClippedRow(getState());
			modTypeRowElement.attach(modTypeTextElement);
			modTypeTextElement.autoWrapOn = modTypeRowElement;
			modTypeTextElement.autoHeight = true;
			modTypeTextElement.setLimitTextDraw(3);

			ModEntryListRow blueprintListRow = new ModEntryListRow(getState(), mod, iconRowElement, nameRowElement, descriptionRowElement, enabledTextElement, modTypeRowElement);
			blueprintListRow.expanded = new GUIElementList(getState());
			GUIAnchor anchor = new GUIAnchor(getState(), getWidth() - 28.0f, 24.0f);
			anchor.attach(redrawButtonPane(mod, anchor));
			blueprintListRow.expanded.add(new GUIListElement(anchor, getState()));
			blueprintListRow.expanded.attach(anchor);
			blueprintListRow.onInit();
			list.addWithoutUpdate(blueprintListRow);
		}
		list.updateDim();
	}

	public GUIHorizontalButtonTablePane redrawButtonPane(ModSkeleton mod, GUIAnchor anchor) {
		GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 3, 1, anchor);
		buttonPane.onInit();

		buttonPane.addButton(0, 0, "ENABLE", GUIHorizontalArea.HButtonColor.GREEN, new GUICallback() {
			@Override
			public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
				if(mouseEvent.pressedLeftMouse()) {
					//getState().getController().queueUIAudio("0022_menu_ui - select 2");
					SinglePlayerModData.getInstance().setClientEnabled(ModIdentifier.fromMod(mod), true);
					redrawList();
				}
			}

			@Override
			public boolean isOccluded() {
				return !inputsEmpty() || SinglePlayerModData.getInstance().isClientEnabled(ModIdentifier.fromMod(mod)) || mod.getRealMod() instanceof ModPlayground;
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState inputState) {
				return true;
			}

			@Override
			public boolean isActive(InputState inputState) {
				return inputsEmpty() && !(mod.getRealMod() instanceof ModPlayground) && !SinglePlayerModData.getInstance().isClientEnabled(ModIdentifier.fromMod(mod));
			}
		});

		buttonPane.addButton(1, 0, "DISABLE", GUIHorizontalArea.HButtonColor.ORANGE, new GUICallback() {
			@Override
			public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
				if(mouseEvent.pressedLeftMouse()) {
					// if(mod.getRealMod() instanceof ModPlayground) getState().getController().queueUIAudio("0022_menu_ui - error 1");
					//else {
					//getState().getController().queueUIAudio("0022_menu_ui - select 3");
					SinglePlayerModData.getInstance().setClientEnabled(ModIdentifier.fromMod(mod), false);
					redrawList();
					//}
				}
			}

			@Override
			public boolean isOccluded() {
				return !inputsEmpty() || !SinglePlayerModData.getInstance().isClientEnabled(ModIdentifier.fromMod(mod)) || mod.getRealMod() instanceof ModPlayground;
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState inputState) {
				return true;
			}

			@Override
			public boolean isActive(InputState inputState) {
				return inputsEmpty() && SinglePlayerModData.getInstance().isClientEnabled(ModIdentifier.fromMod(mod)) && !(mod.getRealMod() instanceof ModPlayground);
			}
		});

		buttonPane.addButton(2, 0, "DELETE", GUIHorizontalArea.HButtonColor.RED, new GUICallback() {
			@Override
			public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
				if(mouseEvent.pressedLeftMouse()) {
					//if(mod.getRealMod() instanceof ModPlayground) getState().getController().queueUIAudio("0022_menu_ui - error 2");
					//else {
					//    getState().getController().queueUIAudio("0022_menu_ui - select 4");
					SMModLoader.deleteMod(mod);
					redrawList();
					//}
				}
			}

			@Override
			public boolean isOccluded() {
				return !inputsEmpty() || mod.getRealMod() instanceof ModPlayground;
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState inputState) {
				return true;
			}

			@Override
			public boolean isActive(InputState inputState) {
				return inputsEmpty() && !(mod.getRealMod() instanceof ModPlayground);
			}
		});

		return buttonPane;
	}

	private boolean inputsEmpty() {
		return getState().getController().getPlayerInputs().isEmpty() || getState().getController().getPlayerInputs().get(getState().getController().getPlayerInputs().size() - 1).getInputPanel() != this;
	}

	public class ModEntryListRow extends ScrollableTableList<ModSkeleton>.Row {
		public ModEntryListRow(InputState inputState, ModSkeleton blueprint, GUIElement... guiElements) {
			super(inputState, blueprint, guiElements);
			highlightSelect = true;
			highlightSelectSimple = true;
			setAllwaysOneSelected(true);
		}
	}
}

