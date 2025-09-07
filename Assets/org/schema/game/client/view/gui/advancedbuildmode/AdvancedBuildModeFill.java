package org.schema.game.client.view.gui.advancedbuildmode;

import org.schema.game.client.controller.manager.ingame.BuildInstruction;
import org.schema.game.client.controller.manager.ingame.BuildSelectionCopy;
import org.schema.game.client.controller.manager.ingame.BuildSelectionFillHelper;
import org.schema.game.client.controller.manager.ingame.FillTool;
import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.tools.*;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableDirtyInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonColor;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;

public class AdvancedBuildModeFill extends AdvancedBuildModeGUISGroup {

	public AdvancedBuildModeFill(AdvancedGUIElement e) {
		super(e);
	}

	@Override
	public void build(final GUIContentPane pane, GUIDockableDirtyInterface dInt) {
		pane.setTextBoxHeightLast(UIScale.getUIScale().scale(30));

		addButton(pane.getContent(0), 0, 0, new ButtonResult() {

			@Override
			public HButtonColor getColor() {
				if(getBuildToolsManager().getFillTool() == null && !(getBuildToolsManager().getSelectMode() instanceof BuildSelectionFillHelper)) {
					return HButtonColor.BLUE;
				} else {
					return HButtonColor.ORANGE;
				}

			}

			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {

					@Override
					public void pressedRightMouse() {
					}

					@Override
					public void pressedLeftMouse() {
						if(!getBuildToolsManager().isSelectMode() && getBuildToolsManager().getFillTool() == null) {
							getBuildToolsManager().setSelectMode(new BuildSelectionFillHelper());
						} else {
							getBuildToolsManager().setSelectMode(null);
							getBuildToolsManager().setFillTool(null);
						}
					}
				};
			}

			@Override
			public boolean isActive() {
				return !getBuildToolsManager().isPasteMode() && !getBuildToolsManager().isCopyMode() && !(getBuildToolsManager().getSelectMode() instanceof BuildSelectionCopy);
			}

			@Override
			public boolean isHighlighted() {
				if(getBuildToolsManager().getFillTool() == null && !(getBuildToolsManager().getSelectMode() instanceof BuildSelectionFillHelper)) {
					return false;
				} else {
					return true;
				}
			}

			@Override
			public String getName() {
				if(getBuildToolsManager().getFillTool() == null && !(getBuildToolsManager().getSelectMode() instanceof BuildSelectionFillHelper)) {
					return Lng.str("Place Fill Origin");
				} else {
					return Lng.str("Reset Tool");
				}

			}

		});
		addButton(pane.getContent(0), 1, 0, new ButtonResult() {

			@Override
			public HButtonColor getColor() {
				return HButtonColor.BLUE;
			}

			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {

					@Override
					public void pressedRightMouse() {
					}

					@Override
					public void pressedLeftMouse() {
						if(getBuildToolsManager().getFillTool() != null) {
							short type = getPlayerInteractionControlManager().getSelectedTypeWithSub();
							getBuildToolsManager().getFillTool().doFill(getBuildToolsManager(), type, getBuildToolsManager().fill.setting);

							if(ElementKeyMap.isValidType(type)) {
								int blockOrientation = getPlayerInteractionControlManager().getBlockOrientation();

								boolean act = ElementKeyMap.getInfoFast(type).activateOnPlacement();
								BuildInstruction buildInstruction = new BuildInstruction(getPlayerInteractionControlManager().getSegmentControlManager().getSegmentController());

								getBuildToolsManager().getFillTool().place(getPlayerInteractionControlManager(),
										type, blockOrientation, act, buildInstruction);
							}
						}
					}
				};
			}

			@Override
			public boolean isActive() {
				return super.isActive() && getBuildToolsManager().getFillTool() != null;
			}

			@Override
			public String getName() {
				return Lng.str("Fill");
			}

			@Override
			public String getToolTipText() {
				return Lng.str("Fill with selected block");
			}

		});
		addCheckbox(pane.getContent(0), 0, 1, new CheckboxResult() {
			@Override
			public boolean getCurrentValue() {
				return FillTool.useSymmetry;
			}

			@Override
			public void setCurrentValue(boolean b) {
				FillTool.useSymmetry = b;
			}

			@Override
			public boolean getDefault() {
				return true;
			}

			@Override
			public CheckboxCallback initCallback() {
				return null;
			}

			@Override
			public String getName() {
				return Lng.str("Use Symmetry");
			}

			@Override
			public String getToolTipText() {
				return Lng.str("Use symmetry when filling");
			}
		});
		addLabel(pane.getContent(0), 0, 2, new LabelResult() {
			@Override
			public String getName() {
				return Lng.str("Blocks added per click");
			}
		});
		addSlider(pane.getContent(0), 0, 3, new AdvancedBuildModeGUISGroup.SizeSliderResult(getBuildToolsManager().fill) {
			@Override
			public String getToolTipText() {
				return Lng.str("How many blocks per click will be added");
			}
		});
	}

	@Override
	public String getId() {
		return "BFILL";
	}

	@Override
	public String getTitle() {
		return Lng.str("Fill Tool");
	}

}
