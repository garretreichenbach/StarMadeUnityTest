package org.schema.game.client.view.gui.advanced.tools;

import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationHighlightCallback;
import org.schema.schine.input.InputState;

public abstract class AdvResult<E extends AdvCallback> {
	private static final long TOOLTIP_DEFAULT_DELAY_MS = 2000;
	public E callback;
	
	public abstract E initCallback();
	
	public abstract String getName();
	public FontInterface getFontSize(){
		return FontSize.MEDIUM_15;
	}
	public static final Vector4f WHITE = new Vector4f(1,1,1,1);
	public static final Vector4f BLACK = new Vector4f(0,0,0,1);
	public static final Vector4f RED = new Vector4f(1.0f,0.3f,0.3f,1);
	public static final Vector4f GREEN = new Vector4f(0.3f,1.0f,0.3f,1);
	public static final Vector4f HALF_TRANS = new Vector4f(0,0,0,0.4f);
	public static final Vector4f FULL_TRANS = new Vector4f(0,0,0,0);
	
	public boolean isActive(){
		return true;
	}
	public boolean isVisible(){
		return true;
	}
	public boolean isHighlighted(){
		return false;
	}
	public final GUIActivationCallback getActCallback() {
		return actCallback;
	}

	private GUIActivationHighlightCallback actCallback;
	public AdvResult(){
		this.actCallback = new GUIActivationHighlightCallback() {
			
			@Override
			public boolean isVisible(InputState state) {
				return AdvResult.this.isVisible();
			}
			
			@Override
			public boolean isActive(InputState state) {
				return AdvResult.this.isActive();
			}
			
			@Override
			public boolean isHighlighted(InputState state) {
				return AdvResult.this.isHighlighted();
			}
		};
	}
	public final void init(){
		callback = initCallback();
		initDefault();
	}
	protected abstract void initDefault();
	public Vector4f getFontColor(){
		return WHITE;
	}
	
	public int getInsetTop(){
		return 0;
	}
	public int getInsetBottom(){
		return 0;
	}
	public int getInsetRight(){
		return 0;
	}
	public int getInsetLeft(){
		return 0;
	}
	public VerticalAlignment getVerticalAlignment(){
		return VerticalAlignment.TOP;
	}
	public float getWeight(){
		return 1f;
	}
	public HorizontalAlignment getHorizontalAlignment(){
		return HorizontalAlignment.LEFT;
	}
	public enum VerticalAlignment{
		MID,
		TOP,
		BOTTOM,
	}
	public enum HorizontalAlignment{
		MID,
		RIGHT,
		LEFT,
	}
	public void refresh(){
	}

	public void update(Timer timer) {
	}
	
	public abstract String getToolTipText(); 
	public long getToolTipDelayMs(){
		return TOOLTIP_DEFAULT_DELAY_MS;
	}
}
