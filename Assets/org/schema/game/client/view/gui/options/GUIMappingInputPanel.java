package org.schema.game.client.view.gui.options;

import java.util.ArrayList;

import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangle;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

public class GUIMappingInputPanel extends GUIListElement {

	private GUIControlConfigElementInterface settingElement;
	private GUITextOverlay nameOverlay;
	private boolean init;
	private boolean backgroundShade;
	private GUIColoredRectangle rec;

	public GUIMappingInputPanel(InputState state, String name, GUIControlConfigElementInterface settingsElement, boolean backgroundShade) {
		super(state);
		this.backgroundShade = backgroundShade;
		this.setName(name);
		this.settingElement = settingsElement;
		this.nameOverlay = new GUITextOverlay(FontSize.SMALL_14, state);
		this.nameOverlay.setText(new ArrayList());
		this.nameOverlay.getText().add(name);
		setContent(this);
		nameOverlay.setCallback(settingsElement);
		this.settingElement.setCallback(settingsElement);
	}

	@Override
	public void cleanUp() {

	}
	

		
	@Override
	public void draw() {
		if (!init) {
			onInit();
		}
		GlUtil.glPushMatrix();
		transform();
		if (backgroundShade || settingElement.isHighlighted()) {
			if(rec == null){
				rec = new GUIColoredRectangle(getState(), (int)getWidth(), (int)getHeight(), new Vector4f(0.06f, 0.06f, 0.06f, 0.3f));
			}
			if(settingElement.isHighlighted()){
				rec.setColor(new Vector4f(0.90f, 0.03f, 0.03f, 0.3f));
			}else{
				rec.setColor(new Vector4f(0.06f, 0.06f, 0.06f, 0.3f));
			}
			rec.draw();
		}
		nameOverlay.draw();

		settingElement.getPos().x = nameOverlay.getWidth();

		settingElement.draw();
		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
		nameOverlay.onInit();
		settingElement.onInit();

		int centerYTrans =UIScale.getUIScale().scale(9);
		nameOverlay.getPos().x += UIScale.getUIScale().inset;
		nameOverlay.getPos().y += centerYTrans;
		settingElement.getPos().y += centerYTrans;

		nameOverlay.setMouseUpdateEnabled(true);

		init = true;
	}

	@Override
	public float getHeight() {
		return settingElement.getHeight() + UIScale.getUIScale().scale(5);
	}

	@Override
	public float getWidth() {
		return nameOverlay.getWidth() + settingElement.getWidth();
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}
}
