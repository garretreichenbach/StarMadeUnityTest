package org.schema.game.client.view.gui.advanced;

import api.listener.events.gui.AdvancedBuildModeGUICreateEvent;
import api.mod.StarLoader;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableList;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableList.DockerElementExpandable;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUISelectable;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector2f;
import java.util.List;

public abstract class AdvancedGUIElement extends GUIElement implements GUICallback, GUISelectable{

	public static int elemDrawCountDebug;
	protected final GUIDockableList main;
	protected boolean init;

	private List<AdvancedGUIGroup> group = new ObjectArrayList<AdvancedGUIGroup>();
	private AdvancedGUIMinimizeCallback minimizeCallback;


	public AdvancedGUIElement(InputState state){
		super(state);
		main = new GUIDockableList(state, this);
		setMouseUpdateEnabled(true);
		setCallback(this);
		
		//use default
		this.minimizeCallback = new AdvancedGUIMinimizeCallback(getState(), false) {

			@Override
			public boolean isActive() {
				return isActive() && main.isActive();
			}

			@Override
			public void initialMinimized() {
			}

			@Override
			public void onMinimized(boolean minimized) {
			}
			@Override
			public String getMinimizedText() {
				return Lng.str("^ Structure ^");
			}

			@Override
			public String getMaximizedText() {
				return Lng.str("\\/");
			}
		};
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
	}

	@Override
	public boolean isActive() {
		//Overwritten if necessary
		return super.isActive();
	}
	@Override
	public boolean isInside() {
		//include close button and include period when its moving (else the click when minimizing is gonna be outside)
		return main.isInside() || minimizeCallback.isInside();
	}
	
	
	@Override
	public void draw(){
		if(!init){
			onInit();
		}
		elemDrawCountDebug = 0;
		
		minimizeCallback.setButtonPosition(this);
		if(minimizeCallback.minimized && minimizeCallback.minimizeStatus >= 1f){
			if(minimizeCallback.isCloseLash()){
				GlUtil.glPushMatrix();
				transform();
				minimizeCallback.draw();
				GlUtil.glPopMatrix();
			}
		}else{
			if(minimizeCallback.isCloseLash()){
				GlUtil.glPushMatrix();
				transform();
				minimizeCallback.draw();
				GlUtil.glPopMatrix();
			}
			GlUtil.glPushMatrix();
			GUIElement.translateOnlyMode = true;
			drawAttached();
			GUIElement.translateOnlyMode = false;
			GlUtil.glPopMatrix();
			
		}
	}
	
	public void refresh(){
		for(AdvancedGUIGroup g : group){
			g.refresh();
		}
	}
	@Override
	public void update(Timer timer) {
		super.update(timer);
		minimizeCallback.update(timer);
		
		for(AdvancedGUIGroup g : group){
			g.update(timer);
		}
	}
	public void drawToolTip(long time) {
		if(!isInside()){
			return;
		}
		for(AdvancedGUIGroup g : group){
			g.drawToolTip(time);
		}
	}
	
	public int getMinimizeOffset(){
		if(minimizeCallback.isCloseLashOnRight()) {
			return -(int)((getWidth()+(minimizeCallback.closeLashButtonOffsetX()))*minimizeCallback.minimizeStatus);
		}else {
			return -(int)((getWidth()+(minimizeCallback.closeLashButtonOffsetX()))*-minimizeCallback.minimizeStatus);
		}
	}
	
	@Override
	public void onInit() {
		if(init){
			return;
		}
		addGroups(group);
		main.onInit();

		main.setHeightScroller(getScrollerHeight());
		main.setWidthScroller(getScrollerWidth());
		Vector2f initialPos = getInitialPos();
		setPos((int)initialPos.x, (int)initialPos.y);
		
		for(int i = 0; i < group.size(); i++){
			
			AdvancedGUIGroup b = group.get(i);
			if(!b.isHidden()){
				final DockerElementExpandable el;
				if(b.isExpandable()){
					el = main.addElementExpanded(b.getWindowId(), b.getTitle(), b.isClosable(), b.getCloseCallback(), b.getSubListIndex(), b.getBackgroundColor(), b.isDefaultExpanded());
				}else{
					el = main.addElementFixed(b.getWindowId(), b.getTitle(), b.isClosable(), b.getCloseCallback(), b.getSubListIndex(), b.getBackgroundColor()); 
				}
				el.build(b);
			}
			
		}
		
		minimizeCallback.onInit();

		init = true;

		//INSERTED CODE
		AdvancedBuildModeGUICreateEvent advancedBuildModeGUICreateEvent = new AdvancedBuildModeGUICreateEvent(main, group);
		StarLoader.fireEvent(advancedBuildModeGUICreateEvent, false);
		if(advancedBuildModeGUICreateEvent.isCanceled()) return;
		//

		attach(main);
//		attach(closeLashButton);
	}


	
	protected abstract int getScrollerWidth();
	public void setPos(int x, int y){
		setPos(getMinimizeOffset()+x,y,0);
	}
	protected abstract Vector2f getInitialPos();
	protected abstract int getScrollerHeight();
	
	
	@Override
	public void cleanUp() {
		main.cleanUp();
	}


	@Override
	public float getHeight() {
		return main.getHeight();
	}


	@Override
	public float getWidth() {
		return main.getWidth();
	}
	

	protected abstract void addGroups(List<AdvancedGUIGroup> g);

	public void removeGroup(AdvancedGUIGroup advancedGUIGroup) {
		main.removeElement(advancedGUIGroup.getWindowId());
	}
	
	@Override
	public abstract boolean isSelected();

	public AdvancedGUIMinimizeCallback getMinimizeCallback() {
		return minimizeCallback;
	}

	public void setMinimizeCallback(AdvancedGUIMinimizeCallback minimizeCallback) {
		this.minimizeCallback = minimizeCallback;
	}
}

