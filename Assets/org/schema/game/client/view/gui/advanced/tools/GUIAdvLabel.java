package org.schema.game.client.view.gui.advanced.tools;

import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalProgressBar;
import org.schema.schine.input.InputState;

public class GUIAdvLabel extends GUIAdvTool<LabelResult>{
	private final GUITextOverlay l;
	public GUIAdvLabel(InputState state, GUIElement dependent, final LabelResult r) {
		super(state, dependent, r);
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
		attach(l);
	}

	@Override
	public int getElementHeight() {
		return l.getTextHeight();
	}
	@Override
	protected int getElementWidth() {
		return l.getMaxLineWidth();
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
