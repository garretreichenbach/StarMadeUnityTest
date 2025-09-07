package org.schema.game.common.controller.elements.power.reactor.tree.graph;

import javax.vecmath.Vector4f;

import org.schema.common.util.StringTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.elements.power.reactor.tree.graph.ReactorGraphBackground.ColorEnum;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.graph.GUIGraphConnection;
import org.schema.schine.graphicsengine.forms.gui.graph.GUIGraphElementBackground;
import org.schema.schine.input.Mouse;
import org.schema.schine.sound.controller.AudioController;

public class ReactorGraphContainerElementInformation extends ReactorGraphContainer implements GUICallback {

	public static short selected;

	private static long selectTime;

	private ElementInformation element;

	private String desc;

	private boolean availableToBeSelected;

	public ReactorGraphContainerElementInformation(ReactorGraphGlobal global, ElementInformation element) {
		super(global);
		this.element = element;
		desc = element.getChamberEffectInfo(((GameClientState) getGlobal().getState()).getConfigPool());
		if (element.isChamberUpgraded()) {
			desc = Lng.str("[CHAMBER UPGRADE]\n") + desc;
		}
		String rc = Lng.str("RC: %s%%; Total branch up to this: %s%%", StringTools.formatPointZero(element.chamberCapacity * 100f), StringTools.formatPointZero(element.getChamberCapacityBranch() * 100f));
		String description = element.getDescriptionIncludingChamberUpgraded();
		if (description.equals(Lng.str("undefined description"))) {
			description = "";
		}
		desc = element.getName() + "\n" + (description.length() > 0 ? description + "\n" : "") + desc + "\n" + rc;
		availableToBeSelected = !global.tree.containsElementAnyChild(element.getId()) && global.onNode != null && global.onNode.getPossibleSpecifications().contains(element.getId());
		availableToBeSelected = availableToBeSelected & element.isChamberPermitted(global.getTree().pw.getSegmentController().getType());
		if (availableToBeSelected && selected == 0) {
			ReactorGraphContainerElementInformation.selected = element.getId();
		}
	}

	@Override
	public String getText() {
		return element.getName();
	}

	@Override
	public ReactorGraphGlobal getGlobal() {
		return (ReactorGraphGlobal) super.getGlobal();
	}

	@Override
	public GUICallback getSelectionCallback() {
		return this;
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (event.pressedLeftMouse() && availableToBeSelected) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
			AudioController.fireAudioEventID(899);
			if (ReactorGraphContainerElementInformation.selected == element.getId() && System.currentTimeMillis() - ReactorGraphContainerElementInformation.selectTime < Mouse.DOUBLE_CLICK_MS) {
				if (getGlobal().onNode != null && ElementKeyMap.isValidType(ReactorGraphContainerElementInformation.selected)) {
					getGlobal().tree.pw.convertRequest(getGlobal().onNode.getId(), ReactorGraphContainerElementInformation.selected);
					if (getGlobal().ip != null) {
						getGlobal().ip.deactivate();
					}
				}
			} else {
				ReactorGraphContainerElementInformation.selected = element.getId();
				ReactorGraphContainerElementInformation.selectTime = System.currentTimeMillis();
			}
		}
	}

	@Override
	public boolean isOccluded() {
		return !getGlobal().isActive();
	}

	@Override
	public boolean isSelected() {
		return ReactorGraphContainerElementInformation.selected == element.getId();
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
		if (element.isChamberUpgraded()) {
			index = CONNECTION_GREEN;
		} else {
			index = CONNECTION_BLUE;
		}
		int maxIndex = 4;
		c.setTextured(s.getMaterial().getTexture(), index, maxIndex);
		c.getLineColor().set(neutral);
	}

	@Override
	public String getToolTipText() {
		if (getGlobal().tree.existsMutuallyExclusiveFor(element.id)) {
			return Lng.str("This chamber cannot exist in same tree as %s\n%s", ElementKeyMap.toString(element.chamberMutuallyExclusive), desc);
		}
		if (!element.isChamberPermitted(getGlobal().tree.pw.getSegmentController().getType())) {
			return Lng.str("Chamber not permitted on this structure type\n%s", desc);
		}
		return desc;
	}

	@Override
	public GUIGraphElementBackground initiateBackground() {
		if (element.isChamberUpgraded()) {
			return new ReactorGraphBackgroundSmall(getGlobal().getState());
		} else {
			return new ReactorGraphBackgroundBig(getGlobal().getState());
		}
	}

	@Override
	public void setBackgroundColor(GUIGraphElementBackground background) {
		ReactorGraphBackground b = (ReactorGraphBackground) background;
		if (getGlobal().tree.existsMutuallyExclusiveFor(element.id)) {
			b.setColorEnum(ColorEnum.RED);
			return;
		}
		if (!element.isChamberPermitted(getGlobal().tree.pw.getSegmentController().getType())) {
			b.setColorEnum(ColorEnum.RED);
			return;
		}
		if (getGlobal().tree.containsElementAnyChild(element.getId())) {
			b.setColorEnum(ColorEnum.GREEN);
			if (element.isChamberUpgraded()) {
				b.setColorEnum(ColorEnum.CYAN);
			}
			return;
		} else if (getGlobal().onNode != null && getGlobal().onNode.getPossibleSpecifications().contains(element.getId())) {
			b.setColorEnum(ColorEnum.BLUE);
			if (element.isChamberUpgraded()) {
				b.setColorEnum(ColorEnum.MAGENTA);
			}
			return;
		}
		b.setColorEnum(ColorEnum.GREY);
	// b.setColor(getBackgroundColor());
	}
}
