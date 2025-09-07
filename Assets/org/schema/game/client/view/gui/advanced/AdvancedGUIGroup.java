package org.schema.game.client.view.gui.advanced;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.schema.common.util.CompareTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.advanced.tools.*;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIInnerTextbox;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIResizableGrabbableWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.InnerWindowBuilderInterface;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import javax.vecmath.Vector4f;

public abstract class AdvancedGUIGroup implements InnerWindowBuilderInterface {

	protected Vector4f bgcolor = new Vector4f(1, 1, 1, 1);

	private final InputState state;

	public abstract String getId();

	public String getWindowId() {
		return "EXPN_" + getId();
	}

	public abstract String getTitle();

	public abstract void setInitialBackgroundColor(Vector4f bgColor);

	private final Object2ObjectOpenHashMap<GUIElement, Container> elems = new Object2ObjectOpenHashMap<GUIElement, Container>();

	private final AdvancedGUIElement mainElement;

	public AdvancedGUIGroup(AdvancedGUIElement e) {
		setInitialBackgroundColor(bgcolor);
		this.state = e.getState();
		this.mainElement = e;
	}

	public Vector4f getBackgroundColor() {
		return bgcolor;
	}

	public boolean isActive() {
		return this.mainElement.isActive() && ((GameClientState) getState()).getCurrentPlayerObject() instanceof ManagedSegmentController<?>;
	}

	public InputState getState() {
		return state;
	}

	public GUIAdvSlider addSlider(GUIElement container, int x, int y, SliderResult s) {
		GUIAdvSlider slider = new GUIAdvSlider(getState(), container, s);
		addElement(slider, container, x, y);
		return slider;
	}

	public GUIAdvCheckbox addCheckbox(GUIElement container, int x, int y, CheckboxResult s) {
		GUIAdvCheckbox d = new GUIAdvCheckbox(getState(), container, s);
		addElement(d, container, x, y);
		return d;
	}

	public GUIAdvBlock3DDisplay addBlockDisplay3D(GUIElement container, int x, int y, Block3DResult s) {
		GUIAdvBlock3DDisplay d = new GUIAdvBlock3DDisplay(getState(), container, s);
		addElement(d, container, x, y);
		return d;
	}

	public GUIAdvBlockDisplay addBlockSelector(GUIElement container, int x, int y, BlockDisplayResult s) {
		GUIAdvBlockDisplay d = new GUIAdvBlockDisplay(getState(), container, s);
		addElement(d, container, x, y);
		return d;
	}

	public GUIAdvBlockOrientationDisplay addBlockOrientation(GUIElement container, int x, int y, BlockOrientationResult s) {
		GUIAdvBlockOrientationDisplay d = new GUIAdvBlockOrientationDisplay(getState(), container, s);
		addElement(d, container, x, y);
		return d;
	}

	public GUIAdvButton addButton(GUIElement container, int x, int y, ButtonResult s) {
		GUIAdvButton d = new GUIAdvButton(getState(), container, s);
		addElement(d, container, x, y);
		return d;
	}

	public GUIAdvTextBar addTextBar(GUIElement container, int x, int y, TextBarResult s) {
		GUIAdvTextBar d = new GUIAdvTextBar(getState(), container, s);
		addElement(d, container, x, y);
		return d;
	}

	public GUIAdvLabel addLabel(GUIElement container, int x, int y, LabelResult s) {
		GUIAdvLabel d = new GUIAdvLabel(getState(), container, s);
		addElement(d, container, x, y);
		return d;
	}

	public GUIAdvStatLabel addStatLabel(GUIElement container, int x, int y, StatLabelResult s) {
		GUIAdvStatLabel d = new GUIAdvStatLabel(getState(), container, s);
		addElement(d, container, x, y);
		return d;
	}

	public GUIAdvDropdown addDropdown(GUIElement container, int x, int y, DropdownResult s) {
		GUIAdvDropdown d = new GUIAdvDropdown(getState(), container, s);
		addElement(d, container, x, y);
		return d;
	}

	public GUIAdvBlockDisplay addBlockDisplay(GUIElement container, int x, int y, BlockDisplayResult s) {
		GUIAdvBlockDisplay d = new GUIAdvBlockDisplay(getState(), container, s);
		addElement(d, container, x, y);
		return d;
	}

	protected void addElement(GUIAdvTool<?> tool, GUIElement container, int x, int y) {
		Container map = elems.get(container);
		if (map == null) {
			map = new Container(container);
			elems.put(container, map);
		}
		GUIAdvTool<?> put = map.add(x, y, tool);
		tool.mainElementActiveInterface = AdvancedGUIGroup.this::isActive;
		assert (!container.getChilds().contains(tool));
		container.attach(tool);
		assert (put == null) : "Already used spot: " + put + "; " + x + ", " + y;
	}

	protected GUIAdvTool removeElement(GUIElement container, int x, int y) {
		Container map = elems.get(container);
		if (map != null) {
			return map.remove(x, y);
		}
		return null;
	}

	public void removeAllFrom(GUIAnchor container) {
		Container map = elems.remove(container);
		if (map != null) {
			map.cleanUp();
			container.detachAll();
		}
	}

	@Override
	public void updateOnDraw() {
		for (Container map : elems.values()) {
			map.updateOnDraw();
		}
	}

	@Override
	public void adaptTextBox(int t, GUIInnerTextbox tb) {
		GUIAnchor container = tb.getContent();
		Container map = elems.get(container);
		if (map != null) {
			tb.tbHeight = map.totalHeight + GUIInnerTextbox.INSET * 2;
		// System.err.println("TAB ON "+t+": "+tb.tbHeight+"; "+map+"; "+container);
		}
	}

	private class Container {

		public int maxY;

		private class Row implements Comparable<Row> {

			int maxX;

			int indexY;

			@Override
			public int compareTo(Row o) {
				return CompareTools.compare(indexY, o.indexY);
			}

			private Int2ObjectSortedMap<GUIAdvTool> rowList = new Int2ObjectAVLTreeMap<GUIAdvTool>();

			public GUIAdvTool add(int x, GUIAdvTool tool) {
				maxX = Math.max(x + 1, maxX);
				return rowList.put(x, tool);
			}

			public void remove(int x) {
				rowList.remove(x);
			}

			public int updateOnDraw(int yPos) {
				int height = 0;
				float totalWeight = 0;
				for (Entry<GUIAdvTool> e : rowList.int2ObjectEntrySet()) {
					GUIAdvTool v = e.getValue();
					totalWeight += v.getRes().getWeight();
					height = Math.max(height, (int) v.getHeight());
				}
				float curWeight = 0;
				for (Entry<GUIAdvTool> e : rowList.int2ObjectEntrySet()) {
					int indexX = e.getIntKey();
					GUIAdvTool v = e.getValue();
					float weight = v.getRes().getWeight() / totalWeight;
					v.adapt(curWeight, weight);
					v.adaptY(yPos, height);
					curWeight += weight;
				}
				return height;
			}

			public void refresh() {
				for (GUIAdvTool e : rowList.values()) {
					e.refresh();
				}
			}

			public void update(Timer timer) {
				for (GUIAdvTool e : rowList.values()) {
					e.getRes().update(timer);
				}
			}

			public void drawToolTip(long time) {
				for (GUIAdvTool e : rowList.values()) {
					e.drawToolTip(time);
				}
			}

			public void cleanUp() {
				for (GUIAdvTool e : rowList.values()) {
					e.cleanUp();
				}
			}
		}

		final GUIElement elem;

		final Int2ObjectOpenHashMap<Row> row = new Int2ObjectOpenHashMap<Row>();

		private int emptyRowHeight = 5;

		private int totalHeight;

		public Container(GUIElement elem) {
			super();
			this.elem = elem;
		}

		public void updateOnDraw() {
			totalHeight = 0;
			for (int i = 0; i < maxY; i++) {
				Row r = row.get(i);
				if (r != null) {
					totalHeight += r.updateOnDraw(totalHeight);
				} else {
					totalHeight += emptyRowHeight;
				}
			}
		}

		public GUIAdvTool remove(int x, int y) {
			Row r = row.get(y);
			if (r != null) {
				r.remove(x);
			}
			return null;
		}

		public GUIAdvTool add(int x, int y, GUIAdvTool tool) {
			maxY = Math.max(maxY, y + 1);
			Row r = row.get(y);
			if (r == null) {
				r = new Row();
				row.put(y, r);
			}
			r.indexY = y;
			return r.add(x, tool);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((elem == null) ? 0 : elem.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof Container)) {
				return false;
			}
			Container other = (Container) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (elem == null) {
				if (other.elem != null) {
					return false;
				}
			} else if (!elem.equals(other.elem)) {
				return false;
			}
			return true;
		}

		private AdvancedGUIGroup getOuterType() {
			return AdvancedGUIGroup.this;
		}

		public void refresh() {
			for (Row r : row.values()) {
				r.refresh();
			}
		}

		public void cleanUp() {
			for (Row r : row.values()) {
				r.cleanUp();
			}
		}

		public void update(Timer timer) {
			for (Row r : row.values()) {
				r.update(timer);
			}
		}

		public void drawToolTip(long time) {
			for (Row r : row.values()) {
				r.drawToolTip(time);
			}
		}
	}

	public void refresh() {
		for (Container c : elems.values()) {
			c.refresh();
		}
	}

	public void update(Timer timer) {
		for (Container c : elems.values()) {
			c.update(timer);
		}
	}

	public void drawToolTip(long time) {
		for (Container c : elems.values()) {
			c.drawToolTip(time);
		}
	}

	public boolean isDefaultExpanded() {
		return false;
	}

	public int getSubListIndex() {
		return 0;
	}

	public boolean isExpandable() {
		return true;
	}

	public boolean isClosable() {
		return false;
	}

	public GUICallback getCloseCallback() {
		return new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CLOSE)*/
					AudioController.fireAudioEventID(302);
					onClosed();
					mainElement.removeGroup(AdvancedGUIGroup.this);
				}
			}
		};
	}

	public void onClosed() {
	}

	public boolean isHidden() {
		return GUIResizableGrabbableWindow.isHidden(getWindowId());
	}
}
