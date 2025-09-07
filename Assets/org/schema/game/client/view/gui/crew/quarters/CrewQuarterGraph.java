package org.schema.game.client.view.gui.crew.quarters;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.schema.game.common.data.element.quarters.Quarter;
import org.schema.game.common.data.element.quarters.QuarterManager;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangleOutline;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.graph.GUIGraph;
import org.schema.schine.graphicsengine.forms.gui.graph.GUIGraphElement;
import org.schema.schine.input.InputState;

/**
 * [Description]
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class CrewQuarterGraph extends GUIGraph {

	private final QuarterManager manager;
	private final Long2ObjectOpenHashMap<GUIGraphElement> elementsMap = new Long2ObjectOpenHashMap<>();

	public CrewQuarterGraph(InputState state, QuarterManager manager) {
		super(state);
		this.manager = manager;
	}

	@Override
	public void onInit() {
		for(Quarter quarter : manager.getQuartersById().values()) {
			float width = Math.max(quarter.getArea().max.x - quarter.getArea().min.x, FontLibrary.FontSize.MEDIUM_15.getFont().getWidth(quarter.getType().name().replaceAll("_", " ") + " - " + (quarter.getIntegrity() * 100) + "%") + 20);
			float height = Math.max(quarter.getArea().max.z - quarter.getArea().min.z, FontLibrary.FontSize.MEDIUM_15.getFont().getHeight(quarter.getType().name().replaceAll("_", " ") + " - " + (quarter.getIntegrity() * 100) + "%") + 20);
			GUIAnchor anchor = new GUIAnchor(getState(), width, height);
			anchor.onInit();

			GUIColoredRectangleOutline rectangle = new GUIColoredRectangleOutline(getState(), width, height, 3, quarter.getStatus().color);
			rectangle.onInit();
			anchor.attach(rectangle);

			GUITextOverlay textOverlay = new GUITextOverlay(getState());
			textOverlay.onInit();
			textOverlay.setTextSimple(quarter.getType().name().replaceAll("_", " ") + " - " + (quarter.getIntegrity() * 100) + "%");
			textOverlay.setFont(FontLibrary.FontSize.MEDIUM_15);
			anchor.attach(textOverlay);

			GUIGraphElement element = new GUIGraphElement(getState(), anchor);
			element.onInit();
			float x = quarter.getArea().max.x - quarter.getArea().min.x;
			float y = quarter.getArea().max.z - quarter.getArea().min.z;

			element.getPos().x = x;
			element.getPos().y = y;

			textOverlay.getPos().x += width / 20;
			textOverlay.getPos().y += height / 4;
			addGraphElement(quarter.getIndex(), element);
		}

		for(Quarter parent : manager.getQuartersById().values()) {
			for(Quarter child : parent.getChildConnections().values()) {
				if(child != null) addConnection(getGraphElement(parent.getIndex()), getGraphElement(child.getIndex()));
			}
		}
	}

	public GUIGraphElement getGraphElement(long index) {
		return elementsMap.get(index);
	}

	public void addGraphElement(long index, GUIGraphElement element) {
		elementsMap.put(index, element);
		super.addVertex(element);
	}

	@Override
	public GUIGraphElement addVertex(GUIGraphElement g) {
		throw new UnsupportedOperationException("Use addGraphElement() instead");
	}
}