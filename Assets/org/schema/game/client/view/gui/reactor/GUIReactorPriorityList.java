package org.schema.game.client.view.gui.reactor;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.lwjgl.glfw.GLFW;
import org.schema.common.util.CompareTools;
import org.schema.common.util.StringTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.power.reactor.PowerConsumer.PowerConsumerCategory;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorSet;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.server.data.FactionState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;
import org.schema.schine.input.Keyboard;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GUIReactorPriorityList extends ScrollableTableList<PowerConsumerCategory> implements ReactorTreeListener {

	private ManagerContainer<?> c;

	private boolean blockDrag;

	public GUIReactorPriorityList(InputState state, ManagerContainer<?> c, GUIElement p) {
		super(state, 100, 100, p);
		this.c = c;
		c.getPowerInterface().addObserver(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		c.getPowerInterface().deleteObserver(this);
		super.cleanUp();
	}

	@Override
	public void onTreeChanged(ReactorSet t) {
		onChange(false);
	}

	@Override
	public void initColumns() {
		addFixedWidthColumnScaledUI(Lng.str("Index"), 45, (o1, o2) -> {
			int p1 = c.getPowerInterface().getPowerConsumerPriorityQueue().getPriority(o1);
			int p2 = c.getPowerInterface().getPowerConsumerPriorityQueue().getPriority(o2);
			return p1 - p2;
		}, true);
		addFixedWidthColumnScaledUI(Lng.str("Name"), 180, (o1, o2) -> o1.getName().toLowerCase(Locale.ENGLISH).compareTo(o2.getName().toLowerCase(Locale.ENGLISH)));
		addFixedWidthColumnScaledUI(Lng.str("Powered"), 68, (o1, o2) -> CompareTools.compare(c.getPowerInterface().getPowerConsumerPriorityQueue().getPercent(o1), c.getPowerInterface().getPowerConsumerPriorityQueue().getPercent(o2)));
		addFixedWidthColumnScaledUI(Lng.str("Amount"), 60, (o1, o2) -> CompareTools.compare(c.getPowerInterface().getPowerConsumerPriorityQueue().getAmount(o1), c.getPowerInterface().getPowerConsumerPriorityQueue().getAmount(o2)));
		addColumn(Lng.str("Consumption"), 1, (o1, o2) -> CompareTools.compare(c.getPowerInterface().getPowerConsumerPriorityQueue().getConsumption(o1), c.getPowerInterface().getPowerConsumerPriorityQueue().getConsumption(o2)));
		addColumn(Lng.str("% of Recharge"), 1, (o1, o2) -> CompareTools.compare(c.getPowerInterface().getPowerConsumerPriorityQueue().getConsumptionPercent(o1), c.getPowerInterface().getPowerConsumerPriorityQueue().getConsumptionPercent(o2)));
		addFixedWidthColumnScaledUI(Lng.str("Move"), 60, (o1, o2) -> 0);
		addTextFilter(new GUIListFilterText<PowerConsumerCategory>() {

			@Override
			public boolean isOk(String input, PowerConsumerCategory listElement) {
				return listElement.getName().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, FilterRowStyle.FULL);
		activeSortColumnIndex = 0;
		continousSortColumn = 0;
	}

	@Override
	protected Collection<PowerConsumerCategory> getElementList() {
		List<PowerConsumerCategory> p = new ObjectArrayList<PowerConsumerCategory>();
		p.addAll(c.getPowerInterface().getPowerConsumerPriorityQueue().getQueue());
		assert (p != null);
		return p;
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<PowerConsumerCategory> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final FactionManager m = ((FactionState) getState()).getFactionManager();
		final PlayerState player = ((GameClientState) getState()).getPlayer();
		int i = 0;
		for (final PowerConsumerCategory f : collection) {
			GUITextOverlayTable indexText = new GUITextOverlayTable(getState());
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			GUITextOverlayTable amountText = new GUITextOverlayTable(getState());
			GUITextOverlayTable poweredText = new GUITextOverlayTable(getState());
			GUITextOverlayTable consumptionText = new GUITextOverlayTable(getState());
			GUITextOverlayTable consumptionPercText = new GUITextOverlayTable(getState());
			GUIClippedRow indexAnchorP = new GUIClippedRow(getState());
			indexAnchorP.attach(indexText);
			indexText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return String.valueOf(c.getPowerInterface().getPowerConsumerPriorityQueue().getPriority(f));
				}
			});
			GUIClippedRow nameAnchorP = new GUIClippedRow(getState());
			nameAnchorP.attach(nameText);
			nameText.setTextSimple(f.getName());
			amountText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return String.valueOf(c.getPowerInterface().getPowerConsumerPriorityQueue().getAmount(f));
				}
			});
			GUIClippedRow poweredAnchorP = new GUIClippedRow(getState());
			poweredAnchorP.attach(poweredText);
			poweredText.setTextSimple(new Object() {

				@Override
				public String toString() {
					if (c.getPowerInterface().getPowerConsumerPriorityQueue().getConsumption(f) <= 0d) {
						return "-";
					} else {
						return StringTools.formatPointZero(c.getPowerInterface().getPowerConsumerPriorityQueue().getPercent(f) * 100d) + "%";
					}
				}
			});
			consumptionText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return StringTools.formatSmallAndBig(c.getPowerInterface().getPowerConsumerPriorityQueue().getConsumption(f));
				}
			});
			consumptionPercText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return StringTools.formatPointZero(c.getPowerInterface().getPowerConsumerPriorityQueue().getConsumptionPercent(f) * 100d) + "%";
				}
			});
			GUITextButton upButton = new GUITextButton(getState(), 25, this.getDefaultColumnsHeight(), Lng.str("^"), new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !GUIReactorPriorityList.this.isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
						AudioController.fireAudioEventID(674);
						int dir = -1;
						if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) || Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
							dir = -100000;
						}
						c.getPowerInterface().getPowerConsumerPriorityQueue().move(c.getPowerInterface(), f, dir);
					}
				}
			}) {

				@Override
				public void draw() {
					super.draw();
					if (isInside()) {
						blockDrag = true;
					}
				}
			};
			GUITextButton downButton = new GUITextButton(getState(), 25, this.getDefaultColumnsHeight(), Lng.str("v"), new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !GUIReactorPriorityList.this.isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
						AudioController.fireAudioEventID(675);
						int dir = 1;
						if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) || Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
							dir = Integer.MAX_VALUE;
						}
						c.getPowerInterface().getPowerConsumerPriorityQueue().move(c.getPowerInterface(), f, dir);
					}
				}
			}) {

				@Override
				public void draw() {
					super.draw();
					if (isInside()) {
						blockDrag = true;
					}
				}
			};
			GUIAnchor opt = new GUIAnchor(getState(), 50, this.getDefaultColumnsHeight());
			opt.attach(upButton);
			downButton.getPos().x = upButton.getWidth();
			opt.attach(downButton);
			nameText.getPos().y = 4;
			amountText.getPos().y = 4;
			consumptionText.getPos().y = 4;
			consumptionPercText.getPos().y = 4;
			PowerConsumerCategoryRow r = new PowerConsumerCategoryRow(getState(), f, indexAnchorP, nameAnchorP, poweredAnchorP, amountText, consumptionText, consumptionPercText, opt);
			r.expanded = null;
			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}

	@Override
	public void draw() {
		blockDrag = false;
		super.draw();
	}

	private class PowerConsumerCategoryRow extends Row {

		public PowerConsumerCategoryRow(InputState state, PowerConsumerCategory f, GUIElement... elements) {
			super(state, f, elements);
			this.draggable = true;
		}

		@Override
		public void onDrop(ScrollableTableList<PowerConsumerCategory>.Row draggable) {
			c.getPowerInterface().getPowerConsumerPriorityQueue().moveAbs(c.getPowerInterface(), draggable.f, c.getPowerInterface().getPowerConsumerPriorityQueue().getPriority(f));
		}

		@Override
		public boolean isDragStartOk() {
			return !blockDrag;
		}
	}
}
