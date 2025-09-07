package org.schema.game.client.view.gui.advancedbuildmode;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.manager.ingame.BuildSelectionLineHelper;
import org.schema.game.client.view.buildhelper.BuildHelper;
import org.schema.game.client.view.buildhelper.BuildHelperClass;
import org.schema.game.client.view.buildhelper.BuildHelperFactory;
import org.schema.game.client.view.buildhelper.BuildHelperVar;
import org.schema.game.client.view.buildhelper.LineBuildHelper;
import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.tools.ButtonCallback;
import org.schema.game.client.view.gui.advanced.tools.ButtonResult;
import org.schema.game.client.view.gui.advanced.tools.CheckboxCallback;
import org.schema.game.client.view.gui.advanced.tools.CheckboxResult;
import org.schema.game.client.view.gui.advanced.tools.DropdownCallback;
import org.schema.game.client.view.gui.advanced.tools.DropdownResult;
import org.schema.game.client.view.gui.advanced.tools.LabelResult;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUISettingsListElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableDirtyInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonColor;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.graphicsengine.forms.gui.newgui.settingsnew.GUIScrollSettingSelector;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class AdvancedBuildModeShape extends AdvancedBuildModeGUISGroup{


	private Int2ObjectOpenHashMap<Object2ObjectOpenHashMap<BuildHelperFactory, BuildHelper>> mm = new Int2ObjectOpenHashMap<>();
	private BuildHelper currentBuildHelperInstance;
	private BuildHelperFactory currentBuildHelperClass;
	
	public AdvancedBuildModeShape(AdvancedGUIElement e) {
		super(e);
	}
	
	@Override
	public void build(final GUIContentPane pane, GUIDockableDirtyInterface dInt) {
		pane.setTextBoxHeightLast(UIScale.getUIScale().scale(30));
		pane.addNewTextBox(UIScale.getUIScale().scale(30));
//		GUITextOverlay l2 = new GUITextOverlay(10, 10, FontSize.MEDIUM.getFont(), getState());
		addLabel(pane.getContent(0), 0, 0, new LabelResult() {
			@Override
			public String getName() {
				return Lng.str("Build Helpers");
			}

			@Override
			public HorizontalAlignment getHorizontalAlignment() {
				return HorizontalAlignment.MID;
			}
			
			
		});
		addDropdown(pane.getContent(0), 0, 1, new DropdownResult() {
			
			@Override
			public DropdownCallback initCallback() {
				return this::setFromValue;
			}
			private void setFromValue(Object value){
				currentBuildHelperClass = (BuildHelperFactory)value;
				if(getState().getCurrentPlayerObject() != null){
					BuildHelperFactory b = (BuildHelperFactory)value;
					BuildHelper bHelperInstance;
					bHelperInstance = getBuildHelperCached(b, getState().getCurrentPlayerObject());
					bHelperInstance.getPanel(getState(), pane, AdvancedBuildModeShape.this);
					currentBuildHelperInstance = bHelperInstance;
				}
				if(getBuildToolsManager().getBuildHelper() != null && getBuildToolsManager().getBuildHelper().getType() != currentBuildHelperClass){
					getBuildToolsManager().getBuildHelper().clean();
					getBuildToolsManager().setBuildHelper(null);
				}
			}
			@Override
			public String getToolTipText() {
				return Lng.str("Select Build Helper");
			}
			
			@Override
			public String getName() {
				return Lng.str("Build Helpers");
			}
			
			@Override
			public Collection<? extends GUIElement> getDropdownElements(GUIElement dep) {
				return getHelperList();
			}
			
			@Override
			public Object getDefault() {
				return getBuildToolsManager().getBuildHelperClasses().get(0);
			}

			@Override
			public void update(Timer timer) {
				super.update(timer);
				if(currentBuildHelperInstance != null && currentBuildHelperInstance.transformable != getState().getCurrentPlayerObject()){
					System.err.println("[CLIENT] RESET BHELPER INSTANCE");
					currentBuildHelperInstance = null;
				}
				if(currentBuildHelperInstance == null){
					if(currentBuildHelperClass == null){
						currentBuildHelperClass = (BuildHelperFactory) getDefault();
					}
					setFromValue(currentBuildHelperClass);
				}
			}
			@Override
			public boolean needsListUpdate() {
				return false;
			}
			@Override
			public void flagListNeedsUpdate(boolean b) {
			}
			
			
		});
		
		addCheckbox(pane.getContent(0), 0, 2, new CheckboxResult() {
			
			@Override
			public CheckboxCallback initCallback() {
				return null;
			}
			
			@Override
			public String getName() {
				return Lng.str("Restrict Building to Helper");
			}
			
			@Override
			public void setCurrentValue(boolean b) {
				getBuildToolsManager().buildHelperReplace = b;				
			}
			
			@Override
			public boolean getDefault() {
				return getBuildToolsManager().buildHelperReplace;
			}
			
			@Override
			public boolean getCurrentValue() {
				return getBuildToolsManager().buildHelperReplace;
			}
			@Override
			public String getToolTipText() {
				return Lng.str("Only place blocks inside build helper area");
			}
		});
		
		addButton(pane.getContent(0), 0, 3, new ButtonResult() {

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
						
						
						
						BuildHelper buildHelper = getBuildToolsManager().getBuildHelper();
						
						if(buildHelper != null){
							if(!buildHelper.placed){
								//currently inplace mode. untoggle place mode
								getBuildToolsManager().setBuildHelper(null);
								buildHelper.clean();
								if(buildHelper instanceof LineBuildHelper){
									((LineBuildHelper)buildHelper).line = null;
								}
								if(getBuildToolsManager().getSelectMode() != null){
									
									getBuildToolsManager().setSelectMode(null);
								}
								buildHelper.placed = false;
							}else{
								//set with existing
								buildHelper.placed = false;
								if(buildHelper instanceof LineBuildHelper){
									((LineBuildHelper)buildHelper).clean();
									((LineBuildHelper)buildHelper).line = null;
									((LineBuildHelper)buildHelper).placed = false;
									buildHelper.onPressedOk(getBuildToolsManager());
								}
							}
						}else if(currentBuildHelperInstance != null){
							if(getBuildToolsManager().getSelectMode() != null){
								getBuildToolsManager().setSelectMode(null);
							}else{
								currentBuildHelperInstance.onPressedOk(getBuildToolsManager());
							}
						}
					}
				};
			}

			
			@Override
			public boolean isActive() {
				return currentBuildHelperInstance != null;
			}

			@Override
			public String getName() {
				return Lng.str("Set Position");
			}

			@Override
			public boolean isHighlighted() {
				BuildHelper buildHelper = getBuildToolsManager().getBuildHelper();
				if(buildHelper != null){
					return !buildHelper.placed;
				}else{
					return getBuildToolsManager().getSelectMode() instanceof BuildSelectionLineHelper;
				}
			}
			
		});
		addButton(pane.getContent(0), 1, 3, new ButtonResult() {
			
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
						BuildHelper buildHelper = getBuildToolsManager().getBuildHelper();
						if(buildHelper instanceof LineBuildHelper){
							buildHelper.clean();
							buildHelper.create();
							buildHelper.showProcessingDialog(getState(),  getBuildToolsManager(), false);
						}else{
							buildHelper.onPressedOk(getBuildToolsManager());
						}
					}
				};
			}
			
			
			@Override
			public boolean isActive() {
				return getBuildToolsManager().getBuildHelper() != null && getBuildToolsManager().getBuildHelper().placed;
			}
			@Override
			public String getName() {
				return Lng.str("Recalc");
			}
			
		});
		
		addButton(pane.getContent(0), 2, 3, new ButtonResult() {
			
			@Override
			public HButtonColor getColor() {
				return HButtonColor.ORANGE;
			}
			
			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {
					
					@Override
					public void pressedRightMouse() {
					}
					
					@Override
					public void pressedLeftMouse() {
						BuildHelper buildHelper = getBuildToolsManager().getBuildHelper();
						getBuildToolsManager().setBuildHelper(null);
						buildHelper.clean();
						buildHelper.placed = false;
					}
				};
			}
			
			
			@Override
			public boolean isActive() {
				return getBuildToolsManager().getBuildHelper() != null;
			}
			@Override
			public String getName() {
				return Lng.str("Clear");
			}
			
		});
	}

	
	@Override
	public String getId() {
		return "BSHAPE";
	}

	@Override
	public String getTitle() {
		return Lng.str("Build Helpers");
	}
	private List<GUIElement> getHelperList() {
		List<GUIElement> helperList = new ObjectArrayList<GUIElement>();

		
		
		
		for (BuildHelperFactory a : getBuildToolsManager().getBuildHelperClasses()) {

			GUIAnchor c = new GUIAnchor(getState(), UIScale.getUIScale().scale(200), UIScale.getUIScale().h);

			GUITextOverlay o = new GUITextOverlay(FontSize.MEDIUM_15, getState());
			o.setTextSimple(a.getBuildHelperClass().getAnnotation(BuildHelperClass.class).name());
			o.setPos(3, 2, 0);
			c.attach(o);

			helperList.add(c);

			c.setUserPointer(a);
		}

		return helperList;
	}

	public GUIElementList getHelperFieldList(final BuildHelper c, final PlayerGameOkCancelInput input) {
		Field[] fields = c.getClass().getFields();
		GUIElementList list = new GUIElementList(getState());
		for (int i = 0; i < fields.length; i++) {

			final Field f = fields[i];

			final BuildHelperVar annotation = f.getAnnotation(BuildHelperVar.class);
			if (annotation != null) {
				
				if (annotation.type().equals("line")) {
					
					GUITextOverlay l = new GUITextOverlay(getState());
					l.setTextSimple(Lng.str("With this tool, you can create a line helper.\n"
							+ "Press 'Ok' to go into camera pick-mode.\n"
							+ "In this mode you can click once to select the starting point\n"
							+ "of the line.\nThen click again to select the second point."));
					
					
					list.add(new GUIListElement(l, l, getState()));
				}else{
					GUIScrollSettingSelector g = new GUIScrollSettingSelector(getState(), 
								GUIScrollablePanel.SCROLLABLE_HORIZONTAL, 200, FontSize.MEDIUM_18) {
		
					@Override
					protected void decSetting() {
						try {
							f.setFloat(c, f.getFloat(c) - 1);
							settingChanged(null);
						} catch (IllegalArgumentException e) {
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					}
		
					@Override
					protected void incSetting() {
						try {
							f.setFloat(c, f.getFloat(c) + 1);
							settingChanged(null);
						} catch (IllegalArgumentException e) {
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
		
					}
		
					@Override
					protected float getSettingX() {
						try {
							return f.getFloat(c);
		
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
						return 0;
					}
		
					@Override
					protected void setSettingX(float value) {
						try {
							f.setFloat(c, (int) value);
							settingChanged(null);
						} catch (IllegalArgumentException e) {
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					}
		
					@Override
					protected float getSettingY() {
						try {
							return f.getFloat(c);
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
						return 0;
					}
		
					@Override
					protected void setSettingY(float value) {
						try {
							f.setFloat(c, (int) value);
							settingChanged(null);
						} catch (IllegalArgumentException e) {
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
		
					}
		
					@Override
					public void settingChanged(Object setting) {
						super.settingChanged(setting);
					}
		
					@Override
					public float getMaxX() {
						return annotation.max();
					}
		
					@Override
					public float getMaxY() {
						return annotation.max();
					}
		
					@Override
					public float getMinX() {
						return annotation.min();
					}
		
					@Override
					public float getMinY() {
						return annotation.min();
					}
		
					@Override
					public boolean isVerticalActive() {
						return false;
					}
					};
					g.setNameLabel(annotation.name());
						
					list.add(new GUISettingsListElement(getState(), UIScale.getUIScale().scale(100), UIScale.getUIScale().scale(53), "", g, false, false));
				}
			}
		}
		
		
		return list;
	}

	private BuildHelper getBuildHelperCached(BuildHelperFactory buildHelperClass, SimpleTransformableSendableObject obj){
		assert(obj != null);
		Object2ObjectOpenHashMap<BuildHelperFactory, BuildHelper> map = mm.get(obj.getId());
		BuildHelper buildHelper = null;
		if(map != null){
			buildHelper = map.get(buildHelperClass);
		}
		if(buildHelper == null){
			buildHelper = buildHelperClass.getInstance(obj);
			if(map == null){
				map = new Object2ObjectOpenHashMap<>();
				mm.put(obj.getId(), map);
			}
			map.put(buildHelperClass, buildHelper);
		} else {
			buildHelper.reset();			
		}
		return buildHelper;
	}
}
