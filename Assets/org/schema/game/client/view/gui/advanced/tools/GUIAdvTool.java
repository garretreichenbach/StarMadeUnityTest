package org.schema.game.client.view.gui.advanced.tools;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIBlockSprite;
import org.schema.game.client.view.gui.advanced.tools.AdvResult.HorizontalAlignment;
import org.schema.game.client.view.gui.advanced.tools.AdvResult.VerticalAlignment;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

import java.util.List;
import java.util.Locale;

public abstract class GUIAdvTool<E extends AdvResult<?>> extends GUIElement{
	

	protected final GUIElement dependent;

	public boolean adaptWidth = true;
	public boolean adaptHeight = false;
	public int width = UIScale.getUIScale().scale(10);
	private boolean init;

	private final E res;
	private GUIToolTip toolTip;

	private long insideTime;

	public GUIActiveInterface mainElementActiveInterface;
	
	public GUIAdvTool(InputState state, GUIElement dependent, E res) {
		super(state);
		this.dependent = dependent;
		this.res = res; 
		res.init();
		if(EngineSettings.DRAW_TOOL_TIPS.isOn()){
			toolTip = new GUIToolTip(state, "testToolTip", this);
		}
		setMouseUpdateEnabled(true);
	}
	public boolean isInsideForTooltip() {
		return isInside();
	}
	public void adapt(float curWeight, float weight){
		
		if(dependent != null){
			if(adaptWidth){
				setWidth(dependent.getWidth() * weight);
				float start = curWeight * dependent.getWidth();
				
				if(res.getHorizontalAlignment() == HorizontalAlignment.LEFT){
					getPos().x = res.getInsetLeft()+(int)(start);
				}else if(res.getHorizontalAlignment() == HorizontalAlignment.RIGHT){
					getPos().x = (int)(start)+ (getWidth() - (getElementWidth() + res.getInsetRight()));
				}else{
					assert(res.getHorizontalAlignment() == HorizontalAlignment.MID);
					getPos().x = (int)(start)+ (getWidth()/2 - (getElementWidth()/2));
				}
			}
		}
	}
	public void adaptY(int yPos, int height) {
		if(res.getVerticalAlignment() == VerticalAlignment.TOP){
			getPos().y = yPos + res.getInsetTop();
		}else if(res.getVerticalAlignment() == VerticalAlignment.BOTTOM){
			getPos().y = yPos +(height - (getElementHeight() + res.getInsetBottom()));
		}else{
			assert(res.getVerticalAlignment() == VerticalAlignment.MID);
			getPos().y = yPos + height/2 - getElementHeight()/2;
		}
		
	}
	protected int getElementWidth() {
		throw new RuntimeException("Width function needs to be overwritten if this alignment is used");
	}

	@Override
	public void draw(){
		if(!init){
			onInit();
		}
		if(!isVisible()){
			return;
		}
		drawAttached();
	}
	public boolean isVisible(){
		return (getActCallback() == null || getActCallback().isVisible(getState()));
	}

	public GUIActivationCallback getActCallback() {
		return res.getActCallback();
	}

	public void setWidth(float w){
		this.width = (int)w;
	}

	@Override
	public void cleanUp() {
		
	}

	@Override
	public void setCallback(GUICallback callback) {
		assert(getCallback() == null);
		super.setCallback(callback);
		
	}
	@Override
	public void onInit() {
		init = true;
	}


	@Override
	public float getWidth() {
		return width;
	}

	public void setWidth(int w){
		this.width = w;
	}
	
	@Override
	public boolean isActive(){
		return super.isActive() && 
				(mainElementActiveInterface == null || mainElementActiveInterface.isActive()) && 
				(getActCallback() == null || getActCallback().isActive(getState()));
	}

	public abstract int getElementHeight();
	@Override
	public float getHeight(){
		return res.getInsetTop()+getElementHeight()+res.getInsetBottom();
	}

	public void refresh(){
		res.refresh();
	}

	public E getRes() {
		return res;
	}

	public void drawToolTip(long time) {
		
		if(toolTip != null && isInsideForTooltip() && res.getToolTipText() != null && isActive()){
			if(insideTime == 0L){
				insideTime = time;
			}
			if(time - insideTime > res.getToolTipDelayMs()){
				toolTip.setText(res.getToolTipText());
				toolTip.draw();
			}
		}else{
			insideTime = 0L;
		}
	}
	public static List<GUIElement> getBlockElements(GameClientState state, String contain, final GUIElement dep, ObjectArrayList<GUIElement> additionalElements) {

		ObjectArrayList<GUIElement> g = new ObjectArrayList<GUIElement>();

		if (additionalElements != null) {
			g.addAll(additionalElements);
		}

		int i = 0;
		for (final ElementInformation info : ElementKeyMap.sortedByName) {
			if (contain.trim().length() == 0 || info.getName().toLowerCase(Locale.ENGLISH).contains(contain.trim().toLowerCase(Locale.ENGLISH))) {
				GUIAnchor guiAnchor = new GUIAnchor(state, UIScale.getUIScale().scale(300), UIScale.getUIScale().scale(26));
				g.add(guiAnchor);

				final GUITextOverlay t = new GUITextOverlay(FontSize.TINY_12, state){

					@Override
					public void draw() {
						if(dep != null){
							limitTextWidth = (int) (dep.getWidth()-UIScale.getUIScale().scale(26));
						}
						super.draw();
					}
					
				};
				if(dep != null){
					t.limitTextWidth = (int) (dep.getWidth()-UIScale.getUIScale().scale(10));
				}
				t.setTextSimple(info.getName());
				guiAnchor.setUserPointer(info);

				GUIBlockSprite b = new GUIBlockSprite(state, info.getId());
				b.getScale().set(0.4f, 0.4f, 0.0f);

				guiAnchor.attach(b);

				t.getPos().x = UIScale.getUIScale().scale(50);
				t.getPos().y = UIScale.getUIScale().scale(7);

				guiAnchor.attach(t);

				i++;
			}
		}

		return g;
	}
	
}
