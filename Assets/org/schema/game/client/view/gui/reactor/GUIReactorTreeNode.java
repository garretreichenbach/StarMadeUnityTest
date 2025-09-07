package org.schema.game.client.view.gui.reactor;

import java.util.List;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.schema.common.util.StringTools;
import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.AdvancedGUIGroup;
import org.schema.game.client.view.gui.advanced.tools.GUIAdvLabel;
import org.schema.game.client.view.gui.advanced.tools.LabelResult;
import org.schema.game.client.view.gui.advanced.tools.ProgressBarInterface;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorElement;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableDirtyInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class GUIReactorTreeNode extends AdvancedGUIElement {

	private Vector2f initalPos;

	public ReactorElement element;

	ManagedSegmentController<?> m;

	private GUIActiveInterface actIface;

	private GUIReactorTree guiReactorTree;

	public static int NODE_WIDTH = 230;

	public static int NODE_HEIGHT = 94;

	public static final Vector4f GENERAL_NODE_COLOR = new Vector4f(0, 0, 1, 1);

	public static final Vector4f SPECIFIED_NODE_COLOR = new Vector4f(0, 1, 0, 1);

	public static final Vector4f INVALID_NODE_COLOR = new Vector4f(1, 0.3f, 0.3f, 1);

	public GUIReactorTreeNode(InputState state, GUIReactorTree guiReactorTree, GUIActiveInterface actIface, ReactorElement element, ReactorElement parentR, ManagedSegmentController<?> m, Vector2f pos) {
		super(state);
		this.actIface = actIface;
		this.initalPos = pos;
		this.m = m;
		this.element = element;
		this.guiReactorTree = guiReactorTree;
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (event.pressedLeftMouse()) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
			AudioController.fireAudioEventID(677);
			guiReactorTree.setSelected(this);
		}
	}

	@Override
	protected int getScrollerWidth() {
		return NODE_WIDTH + 1;
	}

	@Override
	protected Vector2f getInitialPos() {
		return initalPos;
	}

	@Override
	public boolean isActive() {
		return super.isActive() && (actIface == null || actIface.isActive());
	}

	@Override
	protected int getScrollerHeight() {
		return NODE_HEIGHT * 128;
	}

	public static Vector4f getColor(ReactorElement element) {
		if (element.isGeneral() && element.isParentValidTreeNode() && element.isValidTreeNodeBySize()) {
			return (GENERAL_NODE_COLOR);
		} else if (element.isValidTreeNode()) {
			return (SPECIFIED_NODE_COLOR);
		} else {
			return (INVALID_NODE_COLOR);
		}
	}

	@Override
	protected void addGroups(List<AdvancedGUIGroup> g) {
		if (element != null) {
			g.add(new AdvancedGUIGroup(this) {

				@Override
				public boolean isClosable() {
					return false;
				}

				@Override
				public boolean isExpandable() {
					return false;
				}

				@Override
				public void build(GUIContentPane pane, GUIDockableDirtyInterface dInt) {
					pane.setTextBoxHeightLast(UIScale.getUIScale().scale(30));
					GUIAdvLabel lbl = addLabel(pane.getContent(0), 0, 0, new LabelResult() {

						@Override
						public String getName() {
							if (element == null || getColor(element) == INVALID_NODE_COLOR) {
								return ElementKeyMap.getInfo(element.type).getName() + Lng.str("(INVALID)");
							}
							return (element.isBooted() ? "" : "[" + StringTools.formatPointZero(element.getBootStatus()) + " sec] ") + ElementKeyMap.getInfo(element.type).getName();
						}
					});
					if (!element.isGeneral()) {
						lbl.setBackgroundProgressBar(new ProgressBarInterface() {

							@Override
							public float getProgressPercent() {
								return element == null ? 0 : element.getBootStatusPercent();
							}

							@Override
							public void getColor(Vector4f color) {
								color.set(0.3f, 0.01f, 0.01f, 1.0f);
							}
						});
					}
				}

				@Override
				public String getId() {
					return "REACT" + element.type;
				}

				@Override
				public String getTitle() {
					return ElementKeyMap.getInfo(element.type).getName() + " (" + element.getSize() + ")";
				}

				@Override
				public void setInitialBackgroundColor(Vector4f bgColor) {
					bgColor.set(getColor(element));
				}
			});
		}
	}

	@Override
	public boolean isSelected() {
		return element != null && GUIReactorTree.selected == element.getId();
	}
}
