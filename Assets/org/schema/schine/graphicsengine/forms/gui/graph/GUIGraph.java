package org.schema.schine.graphicsengine.forms.gui.graph;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.lwjgl.opengl.GL11;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.AbstractSceneNode;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector4f;
import java.util.List;

public class GUIGraph extends GUIElement {

	public float arrowSize = 7;
	protected ObjectArrayList<GUIGraphElement> elements = new ObjectArrayList<>();
	protected ObjectArrayList<GUIGraphConnection> connections = new ObjectArrayList<>();
	float width, height;

	public GUIGraph(InputState state) {
		super(state);
	}

	public List<GUIGraphElement> getConnections(GUIGraphElement v1) {
		ObjectArrayList<GUIGraphElement> con = new ObjectArrayList<>();
		for(GUIGraphConnection c : connections) {
			if(c.a == v1) {
				con.add(c.b);
			}
		}
		return con;
	}

	public GUIGraphElement addVertex(GUIGraphElement g) {
		elements.add(g);

		width = Math.max(width, g.getPos().x + g.getWidth());
		height = Math.max(height, g.getPos().y + g.getHeight());
		return g;
	}

	public GUIGraphConnection addConnection(GUIGraphElement a, GUIGraphElement b) {
		GUIGraphConnection guiGraphConnection = new GUIGraphConnection(a, b);
		connections.add(guiGraphConnection);
		return guiGraphConnection;
	}

	public GUIGraphConnection addConnection(GUIGraphElement a, GUIGraphElement b, Vector4f color) {
		GUIGraphConnection guiGraphConnection = new GUIGraphConnection(a, b, color);
		connections.add(guiGraphConnection);
		return guiGraphConnection;
	}

	@Override
	public void cleanUp() {
		for(GUIGraphElement e : elements) {
			e.cleanUp();
		}
		for(GUIGraphConnection e : connections) {
			e.cleanUp();
		}
	}

	@Override
	public void draw() {
		GlUtil.glPushMatrix();

		transform();

		drawGraph();

		for(AbstractSceneNode e : getChilds()) {
			e.draw();
		}

		if(isMouseUpdateEnabled()) {
			checkMouseInside();
		} else {

		}

		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {

	}

	private void drawGraph() {
		drawConnections();
		drawNodes();
	}

	private void drawNodes() {
		for(GUIElement g : elements) {
			g.draw();
		}
	}

	private void drawConnections() {
		GUIGraphConnection.err();
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		for(GUIGraphConnection g : connections) {
			assert (g.a != null);
			assert (g.b != null);
			g.draw(arrowSize);
		}
		GUIGraphConnection.afterDraw();

		GlUtil.glDisable(GL11.GL_BLEND);
		GUIGraphConnection.err();
	}


	@Override
	public float getHeight() {
		return height;
	}

	@Override
	public float getWidth() {
		return width;
	}

	public boolean isEmptyGraph() {
		return elements.isEmpty();
	}


}
