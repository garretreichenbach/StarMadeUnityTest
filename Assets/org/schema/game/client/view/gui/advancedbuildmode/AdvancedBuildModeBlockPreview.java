package org.schema.game.client.view.gui.advancedbuildmode;

import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.tools.Block3DCallback;
import org.schema.game.client.view.gui.advanced.tools.BlockOrientationResult;
import org.schema.game.client.view.gui.advanced.tools.ButtonCallback;
import org.schema.game.client.view.gui.advanced.tools.ButtonResult;
import org.schema.game.client.view.gui.advanced.tools.LabelResult;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableDirtyInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonColor;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;

public class AdvancedBuildModeBlockPreview extends AdvancedBuildModeGUISGroup{


	

	public AdvancedBuildModeBlockPreview(AdvancedGUIElement e) {
		super(e);
	}
	
	
	
	@Override
	public void build(GUIContentPane pane, GUIDockableDirtyInterface dInt) {
		pane.setTextBoxHeightLast(UIScale.getUIScale().scale(30));
		GUITextOverlay l2 = new GUITextOverlay(getState());
		addLabel(pane.getContent(0), 0, 0, new LabelResult() {
			@Override
			public String getName() {
				return Lng.str("Orientation");
			}

			@Override
			public VerticalAlignment getVerticalAlignment() {
				return VerticalAlignment.MID;
			}

			@Override
			public HorizontalAlignment getHorizontalAlignment() {
				return HorizontalAlignment.RIGHT;
			}
			
		});
		
		
		addBlockOrientation(pane.getContent(0), 1, 0, new BlockOrientationResult() {
			
			@Override
			public Block3DCallback initCallback() {
				return null;
			}
			
			@Override
			public short getDefaultType() {
				short type = getPlayerInteractionControlManager().getSelectedTypeWithSub();
				return type;
			}
			
			@Override
			public int getDefaultOrientation() {
				int blockOrientation = getPlayerInteractionControlManager().getBlockOrientation();
				return blockOrientation;
			}

			@Override
			public String getToolTipText() {
				return Lng.str("Rotate Blocks\n(also with mousewheel)\nClick for shape select!");
			}
		});
		addLabel(pane.getContent(0), 0, 1, new LabelResult() {
			@Override
			public String getName() {
				PlayerInteractionControlManager c = getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
				short type = c.getSelectedTypeWithSub();
				
				ElementInformation info = ElementKeyMap.getMultiBaseType(type);
				return info != null ? Lng.str("(click & hold on block to select shape)") : "";
			}

			@Override
			public VerticalAlignment getVerticalAlignment() {
				return VerticalAlignment.TOP;
			}

			@Override
			public HorizontalAlignment getHorizontalAlignment() {
				return HorizontalAlignment.MID;
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.SMALL_14;
			}
			
		});
		addButton(pane.getContent(0), 0, 2, new ButtonResult() {
			
			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {
					@Override
					public void pressedRightMouse() {
					}
					@Override
					public void pressedLeftMouse() {
						getBuildToolsManager().undo();
					}
				};
			}
			@Override
			public String getName() {
				return Lng.str("UNDO");
			}

			@Override
			public boolean isActive() {
				return getBuildToolsManager().canUndo();
			}

			
			@Override
			public HButtonColor getColor() {
				return HButtonColor.ORANGE;
			}
		});
		addButton(pane.getContent(0), 1, 2, new ButtonResult() {
			
			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {
					@Override
					public void pressedRightMouse() {
					}
					@Override
					public void pressedLeftMouse() {
						getBuildToolsManager().redo();
					}
				};
			}
			@Override
			public String getName() {
				return Lng.str("REDO");
			}
			
			@Override
			public boolean isActive() {
				return getBuildToolsManager().canRedo();
			}
			
			@Override
			public HButtonColor getColor() {
				return HButtonColor.BLUE;
			}
		});
		
		
	}

	
	@Override
	public String getId() {
		return "BPREVIEW";
	}

	@Override
	public String getTitle() {
		return Lng.str("General");
	}

	@Override
	public boolean isDefaultExpanded(){
		return true;
	}

	@Override
	public int getSubListIndex() {
		return 0;
	}
	@Override
	public boolean isExpandable() {
		return false;
	}

}
