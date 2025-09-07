package org.schema.game.client.view.gui.advanced.tools;

import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalProgressBar;
import org.schema.schine.input.InputState;

public class GUIAdvStatLabel extends GUIAdvTool<StatLabelResult>{
	private final GUITextOverlay l;
	private final GUITextOverlay ls;
	private GUIScrollablePanel scroll;
	public GUIAdvStatLabel(InputState state, GUIElement dependent, final StatLabelResult r) {
		super(state, dependent, r);
		scroll = new GUIScrollablePanel(10, 10, dependent, state);
		scroll.setScrollable(0);
		GUIAnchor anc = new GUIAnchor(state, 10, 10);
		l = new GUITextOverlay(getRes().getFontSize(), getState()){
			@Override
			public void draw() {
				setColor(getRes().getFontColor());
				super.draw();
			}
		};
		l.setTextSimple(new Object(){
			@Override
			public String toString() {
				return getRes().getName();
			}
			
		});
		ls = new GUITextOverlay(getRes().getFontSize(), getState()){
			@Override
			public void draw() {
				setColor(getRes().getFontColor());
				super.draw();
			}
		};
		ls.setTextSimple(new Object(){
			@Override
			public String toString() {
				return getRes().getValue();
			}
			
		});
		anc.attach(l);
		anc.attach(ls);
		anc.setMouseUpdateEnabled(true);
		scroll.setContent(anc);
		scroll.onInit();
		attach(scroll);
	}
	public int calcTextWidth(){
		l.updateTextSize();
		return l.getMaxLineWidth();
	}
	public int getTextWidth(){
		return l.getMaxLineWidth();
	}
	@Override
	public void draw(){
		ls.setPos(l.getPos().x + getRes().getStatDistance(), l.getPos().y, 0);
		super.draw();
	}
	@Override
	public int getElementHeight() {
		return l.getTextHeight();
	}
	@Override
	protected int getElementWidth() {
		return Math.max((int)(l.getPos().x + getRes().getStatDistance()+ls.getMaxLineWidth()), l.getMaxLineWidth());
	}
	
	public void setBackgroundProgressBar(final ProgressBarInterface progressBar) {
		GUIHorizontalProgressBar p = new GUIHorizontalProgressBar(getState(), this) {
			@Override
			public float getValue() {
				return progressBar.getProgressPercent();
			}

			@Override
			public void draw() {
				progressBar.getColor(getColor());
				super.draw();
			}
			
		};
		p.setPos(0, -2, 0);
		p.onInit();
		attach(p, 0);
	}
}
