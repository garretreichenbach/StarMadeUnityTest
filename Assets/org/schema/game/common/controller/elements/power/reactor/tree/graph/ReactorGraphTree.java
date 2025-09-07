package org.schema.game.common.controller.elements.power.reactor.tree.graph;

import java.util.List;

import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.gui.graph.GUIGraph;
import org.schema.schine.graphicsengine.forms.gui.graph.GUIGraphConnection;
import org.schema.schine.graphicsengine.forms.gui.graph.GUIGraphElementGraphic;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class ReactorGraphTree{
	
	public enum TreeLayout{
		VERTICAL,
		HORIZONTAL
	}
	public ReactorGraphTreeLevel level;
	public GUIReactorGraphElement node;
	public final ReactorGraphTree parent;
	public GUIGraphConnection connectionToParent;
	public final List<ReactorGraphTree> childs = new ObjectArrayList<ReactorGraphTree>();
	GUIGraphElementGraphic graphics;
	public int childSizeWidth;
	public int childSizeWithDistWidth;
	public int childSizeHeight;
	public int childSizeWithDistHeight;
	
	public int maxTreeWidth;
	public int maxTreeHeight;

	public int getMaxWidthColumn() {
		return getMaxWidthColumnRec((int)graphics.getWidth());
	}
	private int getMaxWidthColumnRec(int cur) {
		if(parent != null && parent.childs.size() == 1 ){
			return parent.getMaxWidthColumnRec(Math.max(cur, (int)graphics.getWidth()));
		}
		return Math.max(cur, (int)graphics.getWidth());
	}
	
	@Override
	public String toString() {
		return node.container.getText();
	}
	public ReactorGraphTree(ReactorGraphTreeLevelMap map, ReactorGraphTree parent) {
		this.parent = parent;
		if(parent == null){
			this.level = map.map.get(0);
			if(this.level == null){
				this.level = new ReactorGraphTreeLevel(0);
				map.map.put(this.level.level, this.level);
			}
		}else{
			this.level = map.map.get(parent.level.level+1);
			if(this.level == null){
				this.level = new ReactorGraphTreeLevel(parent.level.level+1);
				parent.level.nextLevel = this.level; 
				this.level.previousLevel = parent.level;
				map.map.put(this.level.level, this.level);
			}
		}
		this.level.levelMembers.add(this);
	}
	public void setDimensions(TreeLayout l, int distanceX, int distanceY) {
		maxTreeWidth = calculateMaxWidth(0, distanceX, l);
		maxTreeHeight = calculateMaxHeight(0, distanceY, l);
		for(ReactorGraphTree c : childs){
			c.setDimensions(maxTreeWidth, maxTreeHeight);
		}
	}
	private void setDimensions(int width, int height) {
		maxTreeWidth = width;
		maxTreeHeight = height;
		for(ReactorGraphTree c : childs){
			c.setDimensions(width, height);
		}
	}
	public void addVerticesAndConnectionsRec(GUIGraph g) {
		g.addVertex(node);
		if(parent != null){
			assert(parent.node != null);
			assert(node != null);
			connectionToParent = g.addConnection(parent.node, node);
			
			Sprite s = Controller.getResLoader().getSprite(g.getState().getGUIPath()+"UI 32px Conduit-4x1-gui-");
			int index = 0; //0 = blue, 1 = orange, 2 = red, 3 = green
			int maxIndex = 4;
			connectionToParent.setTextured(s.getMaterial().getTexture(), index, maxIndex);
			connectionToParent.getLineColor().set(1,1,1,1);
			connectionToParent.correctVertical = 0.4f;
			
			graphics.connectionToParent = connectionToParent;
		}
		for(ReactorGraphTree c : childs){
			c.addVerticesAndConnectionsRec(g);
		}
	}
	public void buildNodes() {
		graphics = new GUIGraphElementGraphic(node.getState(), node.container);
		graphics.onInit();
		graphics.doFormating();
		node.setContent(graphics);
		
		assert(level.levelMembers.contains(this));
		for(ReactorGraphTree c : childs){
			c.buildNodes();
		}
	}
	
	int calculateMaxWidth(int curWidth, int distance, final TreeLayout l){
		if(l == TreeLayout.VERTICAL){
			childSizeWidth = 0;
			for(ReactorGraphTree c : childs){
				childSizeWidth += (c.graphics.getWidth());
			}
			childSizeWithDistWidth = childSizeWidth + Math.max(0, (childs.size()-1)) * distance;
			int max = (int) Math.max(childSizeWithDistWidth, graphics.getWidth());
			for(ReactorGraphTree c : childs){
				max = Math.max(max, c.calculateMaxWidth(max, distance, l));
			}
			return max;
		}else{
			int width = (int) graphics.getWidth();
			childSizeWidth = width;
			if(childs.size() > 0){
				width += distance;
			}
			childSizeWithDistWidth = width;
			for(ReactorGraphTree c : childs){
				width += c.calculateMaxWidth(width, distance, l);
			}
			return width;
		}
	}
	
	private int calculateMaxHeight(int curWidth, int distance, final TreeLayout l){
		if(l == TreeLayout.VERTICAL){
			int height = (int) graphics.getHeight();
			childSizeHeight = height;
			if(childs.size() > 0){
				height += distance;
			}
			childSizeWithDistHeight = height;
			for(ReactorGraphTree c : childs){
				height += c.calculateMaxHeight(height, distance, l);
			}
			return height;
			
		}else{
			childSizeHeight = 0;
			for(ReactorGraphTree c : childs){
				childSizeHeight += (c.graphics.getHeight());
			}
			childSizeWithDistHeight = childSizeHeight + Math.max(0, (childs.size()-1)) * distance;
			int max = (int) Math.max(childSizeWithDistHeight, graphics.getHeight());
			for(ReactorGraphTree c : childs){
				max = Math.max(max, c.calculateMaxHeight(max, distance, l));
			}
			return max;
		}
	}
	
	public void setLocalPositions(final TreeLayout layout, int nodeDistanceX, int nodeDistanceY){
		
	}
	
}
