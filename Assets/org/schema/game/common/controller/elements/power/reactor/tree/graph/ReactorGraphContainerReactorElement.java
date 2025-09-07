package org.schema.game.common.controller.elements.power.reactor.tree.graph;

import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.schema.common.util.StringTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.reactor.GUIReactorTree;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorElement;
import org.schema.game.common.controller.elements.power.reactor.tree.graph.ReactorGraphBackground.ColorEnum;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.graph.GUIGraphConnection;
import org.schema.schine.graphicsengine.forms.gui.graph.GUIGraphElementBackground;
import org.schema.schine.graphicsengine.forms.gui.graph.GUIGraphElementGraphicsGlobal;
import org.schema.schine.sound.controller.AudioController;

public class ReactorGraphContainerReactorElement extends ReactorGraphContainer implements GUICallback {

	private ReactorElement element;

	public ReactorGraphContainerReactorElement(GUIGraphElementGraphicsGlobal global, ReactorElement element) {
		super(global);
		this.element = element;
	}

	@Override
	public String getText() {
		return element.getInfo().getName();
	}

	@Override
	public GUICallback getSelectionCallback() {
		return this;
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (event.pressedLeftMouse()) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
			AudioController.fireAudioEventID(900);
			GUIReactorTree.selected = element.getId();
		}
	}

	@Override
	public boolean isOccluded() {
		return !getGlobal().isActive();
	}

	@Override
	public boolean isSelected() {
		return GUIReactorTree.selected == element.getId();
	}

	@Override
	public Vector4f getBackgroundColor() {
		return neutral;
	}

	@Override
	public Vector4f getConnectionColorTo() {
		return neutral;
	}

	@Override
	public void setConnectionColor(GUIGraphConnection c) {
		Sprite s = Controller.getResLoader().getSprite(getState().getGUIPath() + "UI 32px Conduit-4x1-gui-");
		int index = 0;
		if (element.isGeneral()) {
			if (!element.isParentValidTreeNode()) {
				index = CONNECTION_RED;
			} else {
				index = CONNECTION_ORANGE;
			}
		} else if (element.isValidTreeNode()) {
			index = CONNECTION_BLUE;
		}
		int maxIndex = 4;
		c.setTextured(s.getMaterial().getTexture(), index, maxIndex);
		c.getLineColor().set(neutral);
	}

	@Override
	public void setBackgroundColor(GUIGraphElementBackground background) {
		ReactorGraphBackground b = (ReactorGraphBackground) background;
		b.setColorEnum(ColorEnum.GREEN);
		if (element.isGeneral()) {
			if (!element.isParentValidTreeNode()) {
				if (element.isGeneralChain()) {
					b.setColorEnum(ColorEnum.YELLOW_OFF);
				} else {
					b.setColorEnum(ColorEnum.RED_OFF);
				}
			} else {
				if (element.isGeneral() && element.parent != null && !element.parent.isGeneral() && element.parent.getInfo().chamberRoot != element.type) {
					// parent specified but has different root than this general chamber
					b.setColorEnum(ColorEnum.RED_OFF);
				} else {
					b.setColorEnum(ColorEnum.BLUE_OFF);
				}
			}
		} else if (!element.isValidTreeNode()) {
			b.setColorEnum(ColorEnum.RED_OFF);
		} else if (!element.isBooted()) {
			b.setColorEnum(ColorEnum.ORANGE_OFF);
		}
		b.setColor(neutral);
	}

	@Override
	public String getToolTipText() {
		if (!element.root.isWithinCapacity()) {
			return Lng.str("Reactor Tree is over capacity!");
		}
		if (!element.validConduit) {
			return Lng.str("Invalid Conduit Connection!\nReactor Conduits must physically connect:\n 'reactor power' block <-> 'reactor chamber' block");
		}
		if (element.isGeneral()) {
			if (!element.isParentValidTreeNode()) {
				if (element.isGeneralChain()) {
					return Lng.str("This chamber can't be specified until its previous chamber is specified!");
				}
				return Lng.str("This chamber is connected to an incompatible chamber." + "\nCheck your conduit connections and chamber block types!");
			} else if (element.isGeneral() && element.parent != null && !element.parent.isGeneral() && element.parent.getInfo().chamberRoot != element.type) {
				// parent specified but has different root than this general chamber
				return Lng.str("This chamber is connected to an incompatible chamber block type." + "\nCheck your conduit connections and chamber block types!");
			}
			return element.getInfo().getName() + "\n" + Lng.str(" [GENERAL CHAMBER]\nClick to select and specify!");
		}
		if (!element.isValidTreeNode()) {
			return Lng.str("This specified chamber is not valid anymore." + "\nCheck your conduit connections and chamber block types!");
		}
		String desc = element.getInfo().getChamberEffectInfo(((GameClientState) getGlobal().getState()).getConfigPool());
		if (element.getInfo().isChamberUpgraded()) {
			desc = Lng.str("[CHAMBER UPGRADE]\n") + desc;
		}
		String rc = Lng.str("RC: %s%%; Total branch up to this: %s%%", StringTools.formatPointZero(element.getChamberCapacity() * 100f), StringTools.formatPointZero(element.getCapacityRecursivelyUpwards() * 100f));
		desc = element.getInfo().getName() + "\n" + desc + "\n" + rc + "\n" + Lng.str("Click to select for options");
		;
		return desc;
	}

	@Override
	public GUIGraphElementBackground initiateBackground() {
		// if(element.getInfo().isChamberUpgraded()){
		// return new ReactorGraphBackgroundSmall(getGlobal().getState());
		// }else{
		return new ReactorGraphBackgroundBig(getGlobal().getState());
	// }
	}

	@Override
	public void drawExtra(int width, int height) {
		if (!element.isBooted()) {
			// if(element.getBootStatusPercent() < 1f){
			float p = 1.0f - element.getBootStatusPercent();
			if (p < 0.25) {
				GlUtil.glColor4f(1, 0, 0, 0.5f);
			} else if (p < 0.50) {
				GlUtil.glColor4f(0, 1, 1, 0.5f);
			} else if (p < 0.75) {
				GlUtil.glColor4f(1, 1, 0, 0.5f);
			} else {
				GlUtil.glColor4f(0, 1, 0, 0.5f);
			}
			int osetX = 24;
			int osetY = 24;
			float w = (width - osetX * 2) * (element.getBootStatusPercent());
			// System.err.println("MMM "+w+":: height "+osetY+"-"+(height-osetY));
			GlUtil.glEnable(GL11.GL_BLEND);
			GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlUtil.glDisable(GL11.GL_TEXTURE_2D);
			GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex2f(osetX, osetY);
			GL11.glVertex2f(osetX, height - osetY);
			GL11.glVertex2f(osetX + w, height - osetY);
			GL11.glVertex2f(osetX + w, osetY);
			GL11.glEnd();
			GlUtil.glDisable(GL11.GL_BLEND);
		// }
		}
	}
}
