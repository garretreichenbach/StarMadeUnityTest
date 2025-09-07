package org.schema.game.client.view.gui;

import javax.vecmath.Vector4f;

import org.schema.common.util.StringTools;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.gui.ColoredInterface;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

public class GUIBlockSprite extends GUIOverlay implements ColoredInterface{

	protected short type;
	//	private Sprite sprite0;
//	private Sprite sprite1;
	private Sprite spriteMeta0;

	public GUIBlockSprite(InputState state, short type) {
		super(Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath()+"build-icons-00-16x16-gui-"), state);

//		sprite0 = Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath()+"build-icons-00-16x16-gui-");
//		sprite1 = Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath()+"build-icons-01-16x16-gui-");

		spriteMeta0 = Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath()+"meta-icons-00-16x16-gui-");
		this.type = type;
	}

	@Override
	public void draw() {
		ElementInformation info = ElementKeyMap.getInfo(type);
		int buildIconNum = info.getBuildIconNum();
		int layer = buildIconNum / 256;
		setLayer(layer);
		buildIconNum = (short) (buildIconNum % 256);
		getSprite().setSelectedMultiSprite(buildIconNum);

		super.draw();
	}

	public void setLayer(int layer) {
		if (layer == -1) {
			setSprite(spriteMeta0);
		} else {
			setSprite(Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath()+"build-icons-" + StringTools.formatTwoZero(layer) + "-16x16-gui-"));
		}
	}

	@Override
	public Vector4f getColor() {
		if(getSprite().getTint() == null){
			getSprite().setTint(new Vector4f(1,1,1,1));
		}
		return getSprite().getTint();
	}

}
