package org.schema.game.client.view.gui.reactor;

import org.schema.common.util.StringTools;
import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorElement;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorTree;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.font.FontStyle;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.graph.GUIGraph;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonColor;
import org.schema.schine.graphicsengine.util.timer.LinearTimerUtil;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import javax.vecmath.Vector4f;

public class GUIReactorTabs extends GUIElement {

	private final GUIElement dependent;

	private GUIScrollablePanel scrollPane;

	private GUIAnchor ancor;

	private boolean init;

	private GUIReactorTree tree;

	private LinearTimerUtil timeTool = new LinearTimerUtil(1.9f);

	private LinearTimerUtil timeToolTest = new LinearTimerUtil(0.2f);

	private GUIReactorManagerInterface reactorPanel;

	private final GUIColoredRectangleOutline rectangle;

	private GUIProgressBarDynamic progressBar;

	public GUIReactorTabs(InputState state, GUIElement dependent, GUIReactorManagerInterface panel) {
		super(state);
		this.dependent = dependent;
		this.reactorPanel = panel;
		ancor = new GUIAnchor(state) {

			@Override
			public float getHeight() {
				return GUIReactorTabs.this.dependent.getHeight();
			}

			@Override
			public float getWidth() {
				return GUIReactorTabs.this.dependent.getWidth();
			}
		};
		scrollPane = new GUIScrollablePanel(10, 10, this.dependent, state);
		scrollPane.setContent(ancor);
		progressBar = new GUIProgressBarDynamic(getState(), 10, 10) {

			@Override
			public float getValue() {
				if (tree != null && tree.getTree() != null) {
					ReactorTree t = tree.getTree();
					return t.getChamberCapacity();
				} else {
					return 0;
				}
			}

			@Override
			public String getLabelText() {
				if (tree != null && tree.getTree() != null) {
					ReactorTree t = tree.getTree();
					if (isAnimated() && Math.abs(this.getValue() - this.getAnimValue()) > 0.01f) {
						return Lng.str("%s%% RC used", StringTools.formatPointZero(this.getAnimValue() * 100f));
					} else {
						return Lng.str("%s%% RC used", StringTools.formatPointZero(this.getValue() * 100f));
					}
				} else {
					return "";
				}
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.BIG_20;
			}

			@Override
			public boolean isActive() {
				return GUIReactorTabs.this.isActive();
			}

			@Override
			public boolean isAnimated() {
				return true;
			}
		};
		this.rectangle = new GUIColoredRectangleOutline(state, 64, 64, 3, new Vector4f(1, 1, 1, 1));
		attach(scrollPane);
	}

	@Override
	public void cleanUp() {
		scrollPane.cleanUp();
		ancor.cleanUp();
	}

	@Override
	public void draw() {
		if (!init) {
			onInit();
		}
		progressBar.setHeight((int) ancor.getHeight() - 8);
		progressBar.setWidth((int) (ancor.getWidth() - progressBar.getPos().x) - 4);
		drawAttached();
	}

	@Override
	public void onInit() {
		if (init) {
			return;
		}
		progressBar.onInit();
		progressBar.setBackgroundColor(new Vector4f(1, 1, 1, 1));
		progressBar.setEmptyColor(new Vector4f(0.5f, 0.5f, 1f, 1f));
		progressBar.setFilledColor(new Vector4f(0f, 1f, 0f, 1f), new Vector4f(1f, 0f, 0f, 1f));
		int c = 0;
		int startX = 2;
		int startY = 2;
		for (short t : ElementKeyMap.typeList()) {
			ElementInformation info = ElementKeyMap.getInfoFast(t);
			if (info.isReactorChamberGeneral()) {
				Icon icon = new Icon(getState(), info, c);
				icon.onInit();
				icon.setPos(startX + (c * 66), startY, 0);
				ancor.attach(icon);
				c++;
			}
		}
		progressBar.setPos(startX + (c * 66) + 4, startY + 2, 0);
		if (EngineSettings.DRAW_TOOL_TIPS.isOn()) {
			progressBar.setToolTip(new GUIToolTip(getState(), new Object() {

				@Override
				public String toString() {
					return Lng.str("REACTOR CAPACITY");
				}
			}, progressBar) {

				@Override
				public void draw() {
					if (progressBar.isActive()) {
						super.draw();
					}
				}
			});
		}
		ancor.attach(progressBar);
		init = true;
	}

	@Override
	public void update(Timer timer) {
		timeTool.update(timer);
		timeToolTest.update(timer);
	}

	private class Icon extends GUIElement implements GUICallback, TooltipProviderCallback {

		GUIOverlay backl = new GUIOverlay(Controller.getResLoader().getSprite(getState().getGUIPath() + "UI 64px ChamberTabs-4x4-gui-"), getState());

		GUIOverlay l = new GUIOverlay(Controller.getResLoader().getSprite(getState().getGUIPath() + "UI 64px ChamberTabs-4x4-gui-"), getState());

		private ElementInformation info;

		private final Vector4f color = new Vector4f(1, 1, 1, 1);

		private GUIToolTip toolTip;

		private long lastLeftClick;

		public Icon(InputState state, final ElementInformation info, int index) {
			super(state);
			this.info = info;
			l.onInit();
			backl.onInit();
			backl.setSpriteSubIndex(8);
			l.setSpriteSubIndex(info.reactorGeneralIconIndex);
			setMouseUpdateEnabled(true);
			setCallback(Icon.this);
			if (EngineSettings.DRAW_TOOL_TIPS.isOn()) {
				toolTip = new GUIToolTip(state, new Object() {

					@Override
					public String toString() {
						return Icon.this.info.getName() + "\n" + Lng.str("RC used: %s%%", StringTools.formatPointZero(tree.getReactorCapacityOf(info) * 100f)) + "\n" + Lng.str("(Right click for more info)");
					}
				}, this);
			}
		}

		public GUIContextPane createContextPane() {
			int bLen = 1;
			GUIContextPane p = new GUIContextPane(getState(), 108, bLen * 25);
			p.onInit();
			GUIHorizontalButtonTablePane buttonTable = new GUIHorizontalButtonTablePane(getState(), 1, 1, p);
			buttonTable.onInit();
			buttonTable.activeInterface = GUIReactorTabs.this::isActive;
			GUIHorizontalArea button = buttonTable.addButton(0, 0, Lng.str("Show full tree"), HButtonColor.BLUE, new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !GUIReactorTabs.this.isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						openFullTreeOfCurrentlySelectedChamber();
					}
				}
			}, new GUIActivationCallback() {

				@Override
				public boolean isVisible(InputState state) {
					return true;
				}

				@Override
				public boolean isActive(InputState state) {
					return true;
				}
			});
			button.activeInterface = buttonTable.activeInterface;
			p.attach(buttonTable);
			System.err.println("[CLIENT][GUI] contect pane for reactor tab " + info.getName());
			return p;
		}

		@Override
		public void cleanUp() {
		}

		@Override
		public void draw() {
			updateColor();
			if (isInside()) {
				color.x = Math.min(color.x + 0.2f, 1);
				color.y = Math.min(color.y + 0.2f, 1);
				color.z = Math.min(color.z + 0.2f, 1);
			}
			l.getSprite().setTint(color);
			backl.getSprite().setTint(color);
			GlUtil.glPushMatrix();
			transform();
			checkMouseInside();
			backl.draw();
			l.draw();
			if (isSelected()) {
				rectangle.draw();
			}
			GlUtil.glPopMatrix();
			l.getSprite().setTint(null);
			backl.getSprite().setTint(null);
		}

		public boolean isSelected() {
			return info == reactorPanel.getSelectedTab();
		}

		private void updateColor() {
			ReactorTree t = tree.getTree();
			if (t != null) {
				for (ReactorElement e : t.children) {
					if (e.getTypeGeneral() == info) {
						if (e.isAllValid()) {
							color.set(0.34f, 0.85f, 0.34f, 0.85f);
						} else if (e.isAllValidOrUnspecified()) {
							color.set(0.2f + (0.3f - timeTool.getTime() * 0.3f), 0.2f + (0.3f - timeTool.getTime() * 0.3f), Math.min(1.0f, 0.5f + (0.5f - (timeTool.getTime() * 0.5f))), 0.85f);
						} else {
							color.set(Math.min(1.0f, 0.5f + timeTool.getTime() * 0.5f), 0.2f + (timeTool.getTime() * 0.3f), 0.2f + (timeTool.getTime() * 0.3f), 0.85f);
						}
						return;
					}
				}
			}
			color.set(0.5f, 0.5f, 0.5f, 0.7f);
		}

		@Override
		public void onInit() {
		}

		@Override
		public float getWidth() {
			return backl.getWidth();
		}

		@Override
		public float getHeight() {
			return backl.getHeight();
		}

		private void openFullTreeOfCurrentlySelectedChamber() {
			final PlayerOkCancelInput ip = new PlayerOkCancelInput("REACTOR_OVERVIEW_TREE", getState(), 900, 600, Lng.str("%s Tree View", info.getName()), "", FontStyle.big) {

				@Override
				public void pressedOK() {
					deactivate();
				}

				@Override
				public void onDeactivate() {
				}
			};
			ip.getInputPanel().setCancelButton(false);
			ip.getInputPanel().onInit();
			final GUIDialogWindow w = (GUIDialogWindow) ip.getInputPanel().getBackground();
			w.getMainContentPane().setTextBoxHeightLast(UIScale.getUIScale().P_SMALL_PANE_HEIGHT);
			GUIGraph treeGraph = tree.getTree().getTreeGraph(info, null, null, w.getMainContentPane().getContent(0));
			treeGraph.onInit();
			GUIScrollablePanel pTree = new GUIScrollablePanel(10, 10, w.getMainContentPane().getContent(0), getState());
			pTree.setScrollable(GUIScrollablePanel.SCROLLABLE_HORIZONTAL | GUIScrollablePanel.SCROLLABLE_VERTICAL);
			pTree.setContent(treeGraph);
			w.getMainContentPane().getContent(0).attach(pTree);
			ip.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(676);
		}

		@Override
		public void callback(GUIElement callingGuiElement, MouseEvent event) {
			if (event.pressedLeftMouse()) {
				if (reactorPanel.getSelectedTab() == info) {
					if (System.currentTimeMillis() - lastLeftClick < 200) {
						openFullTreeOfCurrentlySelectedChamber();
					}
				} else {
					reactorPanel.setSelectedTab(info);
				}
				lastLeftClick = System.currentTimeMillis();
			} else if (event.pressedRightMouse()) {
				getState().getController().getInputController().setCurrentContextPane(createContextPane());
			}
		}

		@Override
		public boolean isOccluded() {
			return !GUIReactorTabs.this.isActive();
		}

		@Override
		public GUIToolTip getToolTip() {
			return toolTip;
		}

		@Override
		public void setToolTip(GUIToolTip toolTip) {
			this.toolTip = toolTip;
		}
	}

	@Override
	public boolean isActive() {
		return super.isActive() && reactorPanel.isActive();
	}

	@Override
	public float getWidth() {
		return this.dependent.getWidth();
	}

	@Override
	public float getHeight() {
		return this.dependent.getHeight();
	}

	public void setTree(GUIReactorTree tree) {
		this.tree = tree;
	}
}
