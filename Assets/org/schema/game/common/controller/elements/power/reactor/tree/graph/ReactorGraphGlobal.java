package org.schema.game.common.controller.elements.power.reactor.tree.graph;

import java.util.List;

import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorElement;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorTree;
import org.schema.game.common.controller.elements.power.reactor.tree.graph.ReactorGraphTree.TreeLayout;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.graph.GUIGraph;
import org.schema.schine.graphicsengine.forms.gui.graph.GUIGraphElementGraphicsGlobal;
import org.schema.schine.input.InputState;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class ReactorGraphGlobal extends GUIGraphElementGraphicsGlobal{

	public static final int EXISTING = 1;
	public static final int NON_EXISTING = 2;
	final ReactorTree tree;
	private GUIGraph graph;
	public int nodeDistanceX = 10;
	public int nodeDistanceY = 20;

	private TreeLayout layout = TreeLayout.VERTICAL;
	private final List<ReactorGraphTree> current = new ObjectArrayList<ReactorGraphTree>();
	public ReactorElement onNode;
	private GUIElement dependent;
	public DialogInput ip;
	
	public ReactorGraphGlobal(ReactorTree tree, GUIElement dependent) {
		super((InputState) tree.pw.getSegmentController().getState());
		this.tree = tree;
		this.dependent = dependent;
	}

	public ReactorTree getTree() {
		return tree;
	}

	public void updateGraph(boolean existing, ReactorElement onNode, ElementInformation ... elems) {
		current.clear();
		this.onNode = onNode;
		ReactorGraphTreeLevelMap map = new ReactorGraphTreeLevelMap();
		if(existing){
//			tree.print();
			for (ReactorElement c : tree.children) {
				for(ElementInformation root : elems){
					if(c.isOrIsChildOfGeneral(root.getId())){
						addRecursively(map, elems, c, null);
					}
				}
			}
		}else{
			ReactorGraphContainerElementInformation.selected = 0;
			for(ElementInformation root : elems){
				for (short c : root.chamberChildren) {
					ElementInformation childInfo = ElementKeyMap.getInfo(c);
					addRecursively(map, root, c, null);
				}
			}
		}
		if(graph != null){
			graph.cleanUp();
		}
		graph = constructGraph(current);
		graph.arrowSize = 8;
	}

	private GUIGraph constructGraph(List<ReactorGraphTree> l) {
		GUIGraph g = new GUIGraph(getState());
		
		int i = 0;
		
		ReactorGraphTreeLevel rootLvl = null;
		for(ReactorGraphTree t : l){

			t.buildNodes();
			t.setDimensions(layout, nodeDistanceX, nodeDistanceY);
			
			if(rootLvl == null){
				rootLvl = t.level;
			}
			i++;
		}
		if(rootLvl != null){
			rootLvl.calculateStructure();
			rootLvl.setDimensions(layout, nodeDistanceX, nodeDistanceY);
			rootLvl.setTreePosition(layout, nodeDistanceX, nodeDistanceY, l, i);
			rootLvl.buildNodeLayout(layout, nodeDistanceX, nodeDistanceY);
			for(ReactorGraphTree t : l){
				t.addVerticesAndConnectionsRec(g);
			}
		}
		return g;
	}

	private void addRecursively(ReactorGraphTreeLevelMap map, ElementInformation root, short r, ReactorGraphTree parent) {
		ReactorGraphTree t = new ReactorGraphTree(map, parent);
		ElementInformation info = ElementKeyMap.getInfo(r);
		t.node = new GUIReactorGraphElement(getState(), new ReactorGraphContainerElementInformation(this, info)) ;
		if(parent == null){
			current.add(t);
		}else{
			parent.childs.add(t);
		}
		for (short c : info.chamberChildren) {
			addRecursively(map, root, c, t);
		}
	}

	private void addRecursively(ReactorGraphTreeLevelMap map, ElementInformation[] elems, ReactorElement r, ReactorGraphTree parent) {
		ReactorGraphTree t = new ReactorGraphTree(map, parent);
		t.node = new GUIReactorGraphElement(getState(), new ReactorGraphContainerReactorElement(this, r)) ;
		
		if(parent == null){
			current.add(t);
		}else{
			parent.childs.add(t);
		}
		
		for (ReactorElement c : r.children) {
			addRecursively(map, elems, c, t);
		}
		
		
		
	}

	public GUIGraph getGraph() {
		return graph;
	}

	@Override
	public boolean isActive() {
		return dependent == null || dependent.isActive();
	}

}
