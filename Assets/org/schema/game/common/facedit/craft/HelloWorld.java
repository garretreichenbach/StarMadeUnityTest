package org.schema.game.common.facedit.craft;

import javax.swing.JFrame;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

public class HelloWorld extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	

	public HelloWorld() {
		super("Hello, World!");

		mxGraph graph = new mxGraph();
		Object parent = graph.getDefaultParent();
		graph.setCellsEditable(false);
		graph.setConnectableEdges(false);
		graph.getModel().beginUpdate();
		try {
			Object v1 = graph.insertVertex(parent, null, "Hello,", 20, 20, 80,
					60);
			Object v2 = graph.insertVertex(parent, null, "World!", 200, 150,
					80, 60);
			Object v3 = graph.insertVertex(parent, null, "Hello,", 200, 20, 80,
					30);
			Object e1 = graph
					.insertEdge(
							parent,
							null,
							"",
							v1,
							v2,
							"edgeStyle=elbowEdgeStyle;elbow=horizontal;"
									+ "exitX=0.5;exitY=1;exitPerimeter=1;entryX=0;entryY=0;entryPerimeter=1;");
			Object e2 = graph.insertEdge(parent, null, "", v3, v2,
					"edgeStyle=elbowEdgeStyle;elbow=horizontal;orthogonal=0;"
							+ "entryX=0;entryY=0;entryPerimeter=1;");
		} finally {
			graph.getModel().endUpdate();
		}

		mxGraphComponent graphComponent = new mxGraphComponent(graph);
		getContentPane().add(graphComponent);
	}

	public static void main(String[] args) {
		HelloWorld frame = new HelloWorld();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 320);
		frame.setVisible(true);
	}

}