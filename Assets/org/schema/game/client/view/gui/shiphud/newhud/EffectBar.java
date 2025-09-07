package org.schema.game.client.view.gui.shiphud.newhud;

import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

public abstract class EffectBar extends HudConfig {

	protected GUIOverlay notification;
	protected GUITextOverlay text;
	private IconInterface activeIcon;
	private float iconStarted = 10000;
	private Vector4f color;

	public EffectBar(InputState state) {
		super(state);
	}

	public abstract float getBlendOutTime();

	public abstract float getStayTime();

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#draw()
	 */
	@Override
	public void draw() {
		if (iconStarted < getStayTime() + getBlendOutTime()) {

			text.getText().set(0, activeIcon.getText());
			float alpha = 1;
			if (iconStarted > getStayTime()) {
				alpha = 1.0f - ((iconStarted - getStayTime()) / getBlendOutTime());
			}

			text.setColor(getTextColor().x, getTextColor().y, getTextColor().z, getTextColor().w * alpha);
			notification.getSprite().getTint().set(color.x, color.y, color.z, color.w * alpha);

			text.getPos().x = (getWidth() / 2 - text.getMaxLineWidth() / 2);
			int befY = (int) notification.getPos().y;
			int befYText = (int) text.getPos().y;

			notification.getSprite().setFlip(isFlipped());
			notification.getSprite().setFlipCulling(isFlipped());
			if (isFlipped()) {
				text.getPos().y -= notification.getHeight();
				notification.getPos().y += notification.getHeight();
			}

			super.draw();
			notification.getPos().y = befY;
			text.getPos().y = befYText;
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();

		this.notification = new GUIOverlay(Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath()+"HUD_CombatNote-gui-"), getState());
		notification.getSprite().setTint(new Vector4f(1, 1, 1, 1));
		this.text = new GUITextOverlay(FontSize.MEDIUM_15, getState());
		this.text.setTextSimple("n/a");

		this.width = notification.getWidth();
		this.height = notification.getHeight();
		this.color = new Vector4f(getConfigColor().x / 255f, getConfigColor().y / 255f, getConfigColor().z / 255f, getConfigColor().w / 255f);

//		notification.setPos(-notification.getWidth()/2, -notification.getHeight()/2, 0);
		this.attach(notification);
		notification.attach(text);

		text.setPos(30, 22, 0);
	}

	public abstract Vector4f getTextColor();

	public abstract boolean isFlipped();

	public void activate(IconInterface hitIconIndex) {
		if (iconStarted > getStayTime() || this.activeIcon == hitIconIndex) {
			this.activeIcon = hitIconIndex;
			this.iconStarted = 0f;
		}
	}

	@Override
	public void update(Timer timer) {
		iconStarted += timer.getDelta();
	}
}
