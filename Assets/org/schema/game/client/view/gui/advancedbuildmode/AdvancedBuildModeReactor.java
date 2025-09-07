package org.schema.game.client.view.gui.advancedbuildmode;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.game.client.controller.manager.ingame.SymmetryPlanes;
import org.schema.game.client.view.BuildModeDrawer;
import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.tools.ButtonCallback;
import org.schema.game.client.view.gui.advanced.tools.ButtonResult;
import org.schema.game.client.view.gui.advanced.tools.GUIAdvSlider;
import org.schema.game.client.view.gui.advanced.tools.SliderCallback;
import org.schema.game.client.view.gui.advanced.tools.SliderResult;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.controller.elements.power.reactor.PowerInterface;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorTree;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableDirtyInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonColor;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.KeyboardMappings;

public class AdvancedBuildModeReactor extends AdvancedBuildModeGUISGroup{




	public AdvancedBuildModeReactor(AdvancedGUIElement e) {
		super(e);
	}
	
	private void apply() {
		Matrix3f mX = new Matrix3f();
		mX.setIdentity();
		mX.rotX(FastMath.DEG_TO_RAD*defX);
		
		Matrix3f mY = new Matrix3f();
		mY.setIdentity();
		mY.rotY(FastMath.DEG_TO_RAD*defY);
		
		Matrix3f mZ = new Matrix3f();
		mZ.setIdentity();
		mZ.rotZ(FastMath.DEG_TO_RAD*defZ);
		
		if(isActive()){
			PowerInterface pw = ((ManagedSegmentController<?>)getState().getCurrentPlayerObject()).getManagerContainer().getPowerInterface();
			if(pw.getActiveReactor() != null) {
				mX.mul(mY);
				mX.mul(mZ);
				pw.getActiveReactor().getBonusMatrix().set(mX);
			}
		}
	}
	 
	GUIAdvSlider sx;
	GUIAdvSlider sy;
	GUIAdvSlider sz;
	private float defX;
	private float defY;
	private float defZ;
	private SegmentController init;
	private SegmentController initX;
	private SegmentController initY;
	private SegmentController initZ;
	@Override
	public void build(final GUIContentPane pane, GUIDockableDirtyInterface dInt) {
		
		
		pane.setTextBoxHeightLast(UIScale.getUIScale().scale(30));
		
		int yPos = 0;
		if(VoidElementManager.isUsingReactorDistance()) {
		sx = addSlider(pane.getContent(0), 0, yPos++, new SliderResult() {
			
			boolean started = false;
			@Override
			public SliderCallback initCallback() {
				return value -> {
					defX = value;
					started = true;
					apply();

				};
			}
			@Override
			public float getResetValue() {
				return 0;
			}
			@Override
			public String getToolTipText() {
				return Lng.str("X-Axis Alignment");
			}
			@Override
			public String getName() {
				return Lng.str("X-Axis Angle");
			}
			@Override
			public void update(Timer timer) {
				super.update(timer);
				if(sx.isInside()){
					BuildModeDrawer.inReactorAlignSlider = true;
					BuildModeDrawer.inReactorAlignSliderSelectedAxis = SymmetryPlanes.MODE_YZ;
				}
				if(started && !KeyboardMappings.BUILD_BLOCK_BUILD_MODE.isDown()){
					started = false;
					if(isActive()){
						PowerInterface pw = ((ManagedSegmentController<?>)getState().getCurrentPlayerObject()).getManagerContainer().getPowerInterface();
						pw.sendBonusMatrixUpdate(pw.getActiveReactor(), pw.getActiveReactor().getBonusMatrix());
					}
				}
				initCC();
				if(getState().getCurrentPlayerObject() instanceof ManagedSegmentController<?> && initX != getState().getCurrentPlayerObject() && ((ManagedSegmentController<?>)getState().getCurrentPlayerObject()).getManagerContainer().hasActiveReactors()){
					initX = (SegmentController) getState().getCurrentPlayerObject();
					change(defX);
					started = false;
				}
			}

			@Override
			public float getMin() {
				return -45;
			}
			@Override
			public float getMax() {
				return 45;
			}
			@Override
			public float getDefault() {
				
				return defX;
			}
			@Override
			public boolean isActive() {
				return super.isActive() && getState().getCurrentPlayerObject() instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>)getState().getCurrentPlayerObject()).getManagerContainer().hasActiveReactors();
			}
			@Override
			public boolean isHighlighted() {
				return isActive();
			}
		});
		sy = addSlider(pane.getContent(0), 0, yPos++, new SliderResult() {
			
			boolean started = false;
			@Override
			public SliderCallback initCallback() {
				return value -> {
					defY = value;
					started = true;
					apply();

				};
			}
			@Override
			public float getResetValue() {
				return 0;
			}
			@Override
			public String getToolTipText() {
				return Lng.str("Y-Axis Alignment");
			}
			@Override
			public String getName() {
				return Lng.str("Y-Axis Angle");
			}
			@Override
			public void update(Timer timer) {
				super.update(timer);
				if(sy.isInside()){
					BuildModeDrawer.inReactorAlignSlider = true;
					BuildModeDrawer.inReactorAlignSliderSelectedAxis = SymmetryPlanes.MODE_XZ;
				}
				if(started && !KeyboardMappings.BUILD_BLOCK_BUILD_MODE.isDown()){
					started = false;
					if(isActive()){
						PowerInterface pw = ((ManagedSegmentController<?>)getState().getCurrentPlayerObject()).getManagerContainer().getPowerInterface();
						pw.sendBonusMatrixUpdate(pw.getActiveReactor(), pw.getActiveReactor().getBonusMatrix());
					}
				}
				initCC();
				if(getState().getCurrentPlayerObject() instanceof ManagedSegmentController<?> && initY != getState().getCurrentPlayerObject() && ((ManagedSegmentController<?>)getState().getCurrentPlayerObject()).getManagerContainer().hasActiveReactors()){
					initY = (SegmentController) getState().getCurrentPlayerObject();
					change(defY);
					started = false;
				}
			}
			
			@Override
			public float getMin() {
				return -45;
			}
			@Override
			public float getMax() {
				return 45;
			}
			@Override
			public float getDefault() {
				
				return defY;
			}
			@Override
			public boolean isActive() {
				return super.isActive() && getState().getCurrentPlayerObject() instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>)getState().getCurrentPlayerObject()).getManagerContainer().hasActiveReactors();
			}
			@Override
			public boolean isHighlighted() {
				return isActive();
			}
		});
		sz = addSlider(pane.getContent(0), 0, yPos++, new SliderResult() {
			
			boolean started = false;
			@Override
			public SliderCallback initCallback() {
				return value -> {
					defZ = value;
					started = true;
					apply();

				};
			}
			@Override
			public float getResetValue() {
				return 0;
			}
			@Override
			public String getToolTipText() {
				return Lng.str("Z-Axis Alignment");
			}
			@Override
			public String getName() {
				return Lng.str("Z-Axis Angle");
			}
			@Override
			public void update(Timer timer) {
				super.update(timer);
				if(sz.isInside()){
					BuildModeDrawer.inReactorAlignSlider = true;
					BuildModeDrawer.inReactorAlignSliderSelectedAxis = SymmetryPlanes.MODE_XY;
				}
				if(started && !KeyboardMappings.BUILD_BLOCK_BUILD_MODE.isDown()){
					started = false;
					if(isActive()){
						PowerInterface pw = ((ManagedSegmentController<?>)getState().getCurrentPlayerObject()).getManagerContainer().getPowerInterface();
						pw.sendBonusMatrixUpdate(pw.getActiveReactor(), pw.getActiveReactor().getBonusMatrix());
					}
				}
				initCC();
				if(getState().getCurrentPlayerObject() instanceof ManagedSegmentController<?> && initZ != getState().getCurrentPlayerObject() && ((ManagedSegmentController<?>)getState().getCurrentPlayerObject()).getManagerContainer().hasActiveReactors()){
					initZ = (SegmentController) getState().getCurrentPlayerObject();
					change(defZ);
					started = false;
				}
			}
			
			@Override
			public float getMin() {
				return -45;
			}
			@Override
			public float getMax() {
				return 45;
			}
			@Override
			public float getDefault() {
				
				return defZ;
			}
			@Override
			public boolean isActive() {
				return super.isActive() && getState().getCurrentPlayerObject() instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>)getState().getCurrentPlayerObject()).getManagerContainer().hasActiveReactors();
			}
			@Override
			public boolean isHighlighted() {
				return isActive();
			}
		});
		}
		addButton(pane.getContent(0), 0, yPos++, new ButtonResult() {
			
			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {
					@Override
					public void pressedRightMouse() {
					}
					
					@Override
					public void pressedLeftMouse() {
						BuildModeDrawer.inReactorAlignAlwaysVisible = !BuildModeDrawer.inReactorAlignAlwaysVisible; 
					}
				};
			}
			
			@Override
			public String getName() {
				return Lng.str("Toggle always visible");
			}
			@Override
			public boolean isActive() {
				return super.isActive() && getState().getCurrentPlayerObject() instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>)getState().getCurrentPlayerObject()).getManagerContainer().hasActiveReactors();
			}
			@Override
			public HButtonColor getColor() {
				return HButtonColor.BLUE;
			}

			@Override
			public boolean isHighlighted() {
				return BuildModeDrawer.inReactorAlignAlwaysVisible;
			}
			
		});
		
	}

	public void initCC(){
		if(getState().getCurrentPlayerObject() instanceof ManagedSegmentController<?> && init != getState().getCurrentPlayerObject() && ((ManagedSegmentController<?>)getState().getCurrentPlayerObject()).getManagerContainer().hasActiveReactors()){
			PowerInterface pw = ((ManagedSegmentController<?>)getState().getCurrentPlayerObject()).getManagerContainer().getPowerInterface();
			ReactorTree r = pw.getActiveReactor();
			
			
			if(r.hasModifiedBonusMatrix()){
				Vector3f out = new Vector3f();
				Matrix4fTools.ToEulerAnglesXYZ(r.getBonusMatrix(), out);
				defX = Math.round (out.x * FastMath.RAD_TO_DEG);
				defY = Math.round (out.y * FastMath.RAD_TO_DEG);
				defZ = Math.round (out.z * FastMath.RAD_TO_DEG);
//				assert(false):defX+", "+defY+", "+defZ;
			}
			init = (SegmentController) getState().getCurrentPlayerObject();
			initX = null;
			initY = null;
			initZ = null;
		}
	}
	@Override
	public String getId() {
		return "BREACT";
	}

	@Override
	public String getTitle() {
		return Lng.str("Reactor Align");
	}
	
}
