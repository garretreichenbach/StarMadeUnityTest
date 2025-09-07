package org.schema.game.client.view.gui;

import java.io.File;

import org.lwjgl.opengl.GL11;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.graphicsengine.core.FrameBufferObjects;
import org.schema.schine.graphicsengine.core.GLException;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.graph.GUIGraph;
import org.schema.schine.input.InputState;
import org.schema.schine.resource.FileExt;

public class GUIBlockConsistenceGraph extends GUIAnchor {

	private GUIScrollablePanel scrollPane;
	private GUIGraph graph;

	public GUIBlockConsistenceGraph(InputState state, ElementInformation info) {
		this(state, info, null);
	}

	public GUIBlockConsistenceGraph(InputState state, ElementInformation info, GUIAnchor guiAnchor) {
		super(state);

		if (guiAnchor != null) {
			scrollPane = new GUIScrollablePanel(824, 424, guiAnchor, state);
		} else {
			scrollPane = new GUIScrollablePanel(824, 424, state);
		}

		graph = info.getRecipeGraph(state);

		scrollPane.setScrollable(GUIScrollablePanel.SCROLLABLE_HORIZONTAL | GUIScrollablePanel.SCROLLABLE_VERTICAL);

		scrollPane.setContent(graph);

		this.attach(scrollPane);

	}

	public static void bake(FrameBufferObjects fbo, GameClientState state) throws GLException {

		FrameBufferObjects fb = new FrameBufferObjects("BlockConsistenceBake", 2048, 2048);
		fb.initialize();
		fb.enable();

		GL11.glClearColor(0, 0, 0, 0);

		File f = new FileExt("./recipeGraphs/");
		if (!f.exists()) {
			f.mkdir();
		}

		for (short infoId : ElementKeyMap.keySet) {
			GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);

			ElementInformation info = ElementKeyMap.getInfo(infoId);

			GUIGraph graph = info.getRecipeGraph(state);
			GL11.glViewport(0, 0, (int) graph.getWidth(), (int) graph.getHeight());

			GlUtil.glDisable(GL11.GL_LIGHTING);
			GlUtil.glDisable(GL11.GL_DEPTH_TEST);
			GlUtil.glPushMatrix();
			GlUtil.glMatrixMode(GL11.GL_PROJECTION);    // Select The Projection Matrix
			GlUtil.glPushMatrix();            // Store The Projection Matrix
			GlUtil.glLoadIdentity();
			GlUtil.gluOrtho2D(0, (int) graph.getWidth(), (int) graph.getHeight(), 0);
			GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
			GlUtil.glLoadIdentity();

			graph.draw();
			GlUtil.writeScreenToDisk("./recipeGraphs/" + info.getId() + "_" + info.getName(), "png", (int) graph.getWidth(), (int) graph.getHeight(), 4, fbo);

			GlUtil.glMatrixMode(GL11.GL_PROJECTION);    // Select The Projection Matrix
			GlUtil.glPopMatrix();    // Restore The Old Projection Matrix
			GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
			GlUtil.glPopMatrix();

			GlUtil.glEnable(GL11.GL_DEPTH_TEST);
			GlUtil.glEnable(GL11.GL_LIGHTING);
		}
		GL11.glViewport(0, 0, GLFrame.getWidth(), GLFrame.getHeight());
		fb.disable();
		fb.cleanUp();

	}
}
