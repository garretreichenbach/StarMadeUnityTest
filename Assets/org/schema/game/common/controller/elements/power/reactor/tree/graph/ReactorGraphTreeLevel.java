package org.schema.game.common.controller.elements.power.reactor.tree.graph;

import java.util.List;

import org.schema.game.common.controller.elements.power.reactor.tree.graph.ReactorGraphTree.TreeLayout;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class ReactorGraphTreeLevel {
	public ReactorGraphTreeLevel(int lvl) {
		this.level = lvl;
	}
	public final int level;
	public List<ReactorGraphTree> levelMembers = new ObjectArrayList<ReactorGraphTree>();
	public List<ReactorGraphTreeUnit> units = new ObjectArrayList<ReactorGraphTreeUnit>();
	private int childSizeWidth;
	private int childSizeWithDistWidth;
	private int childSizeHeight;
	private int childSizeWithDistHeight;
	public ReactorGraphTreeLevel nextLevel;
	public ReactorGraphTreeLevel previousLevel;
	public int maxTreeWidth;
	public int maxTreeHeight;
	
	public int treePosX;
	public int treePosY;
	private boolean formated;
	
	
	private int calculateMaxWidth(int curWidth, int distance, final TreeLayout l){
		if(l == TreeLayout.VERTICAL){
			childSizeWidth = 0;
			for(ReactorGraphTree c : levelMembers){
				childSizeWidth += c.graphics.getWidth();
			}
			childSizeWithDistWidth = childSizeWidth + Math.max(0, (levelMembers.size()-1)) * distance;
			int max = childSizeWithDistWidth;
			if(nextLevel != null){
				max = Math.max(max, nextLevel.calculateMaxWidth(max, distance, l));
			}
			return max;
		}else{
			int width = (int) levelMembers.get(0).graphics.getWidth();
			childSizeWidth = width;
			childSizeWithDistWidth = width + (levelMembers.size() > 0 ? distance : 0);
			if(nextLevel != null){
				width += nextLevel.calculateMaxWidth(width, distance, l);
			}
			return width;
		}
	}
	private int calculateMaxHeight(int curWidth, int distance, final TreeLayout l){
		if(l == TreeLayout.VERTICAL){
			int height = (int) levelMembers.get(0).graphics.getHeight();
			assert(height > 0);
			
			childSizeHeight = height;
			childSizeWithDistHeight = height + (levelMembers.size() > 0 ? distance : 0);
			if(nextLevel != null){
				height += nextLevel.calculateMaxHeight(height, distance, l);
			}
			return height;
			
		}else{
			childSizeHeight = 0;
			for(ReactorGraphTree c : levelMembers){
				childSizeHeight += c.graphics.getHeight();
			}
			childSizeWithDistHeight = childSizeHeight + Math.max(0, (levelMembers.size()-1)) * distance;
			int max = childSizeWithDistHeight;
			if(nextLevel != null){
				max = Math.max(max, nextLevel.calculateMaxHeight(max, distance, l));
			}
			return max;
		}
	}
	
	public void setDimensions(TreeLayout l, int distanceX, int distanceY) {
		maxTreeWidth = calculateMaxWidth(0, distanceX, l);
		maxTreeHeight = calculateMaxHeight(0, distanceY, l);
		if(nextLevel != null){
			nextLevel.setDimensions(maxTreeWidth, maxTreeHeight);
		}
	}
	private void setDimensions(int width, int height) {
		maxTreeWidth = width;
		maxTreeHeight = height;
		if(nextLevel != null){
			nextLevel.setDimensions(width, height);
		}
	}
	public void setTreePosition(final TreeLayout layout, int nodeDistanceX, int nodeDistanceY, List<ReactorGraphTree> l,
			int index) {
		treePosX = 0;
		treePosY = 0;
		if(layout == TreeLayout.VERTICAL){
			for(int i = 0; i < index; i++){
				treePosX += l.get(i).level.maxTreeWidth;
				treePosX += nodeDistanceX;
			}
		}else{
			for(int i = 0; i < index; i++){
				treePosY += l.get(i).level.maxTreeHeight;
				treePosY += nodeDistanceY;
			}
		}
	}
	public void formatLevel(int distX){
		if(formated){
			return;
		}
		formated = true;
		
		if(nextLevel != null){
			
			assert(nextLevel.formated);
			int posX = 0;
			for(int i = 0; i < units.size(); i++){
				final ReactorGraphTreeUnit unit = units.get(i);
				if(unit.placed){
					continue;
				}
				if(!unit.member.childs.isEmpty()){
					
					//has parent. place in middle
					if(unit.member.childs.size() == 1){
						posX = (int) unit.member.childs.get(0).node.getPos().x;
					}else{
						int min = Integer.MAX_VALUE;
						int max = Integer.MIN_VALUE;
						
						for(ReactorGraphTree c : unit.member.childs){
							min = Math.min(min, (int)c.node.getPos().x);
							max = Math.max(max, (int)c.node.getPos().x + (int)c.graphics.getWidth()+distX);
						}
						posX = min + ((max - min)/2 - (int)(unit.member.graphics.getWidth()/2f));
					}
					unit.member.node.setPos(posX, unit.member.level.getPosY(), 0);
				}else{
					assert(unit.placed);
				}
			}
		}else{
			//max level
			int posX = 0;
			for(int i = 0; i < units.size(); i++){
				final ReactorGraphTreeUnit unit = units.get(i);
				
				unit.member.node.setPos(posX, unit.member.level.getPosY(), 0);
				
				posX += (unit.member.getMaxWidthColumn()+distX);
				unit.placed = true;
				
			}
		}
		
		if(previousLevel != null){
			previousLevel.formatLevel(distX);
		}
	}
	public void buildNodeLayout(TreeLayout layout, int nodeDistanceX, int nodeDistanceY) {
		ReactorGraphTreeLevel lvl = this;
		ReactorGraphTreeLevel maxLvl = null;
		while(lvl != null){
			maxLvl = lvl;
			lvl = lvl.nextLevel;
		}
		//start at last level
		maxLvl.formatLevel(nodeDistanceX);
		

	}
	
	public int getPosY(){
		int h = 0;
		if(previousLevel != null){
			h += previousLevel.childSizeWithDistHeight + previousLevel.getPosY();
		}
		return h;//level * 30;
	}
	public int getPosX(){
		int h = 0;
		if(previousLevel != null){
			h += previousLevel.childSizeWithDistWidth + previousLevel.getPosX();
		}
		return h;
	}
	/**
	 * this creates "slots" for leaves that are not on this level to later
	 * be able to make space on the higher levels for lower level nodes
	 */
	public void calculateStructure() {
		
		if(previousLevel != null){
			for(ReactorGraphTreeUnit unit : previousLevel.units){
				if(!unit.emptyReference){
					for(ReactorGraphTree e : levelMembers){
						if(unit.member.childs.contains(e)){
							ReactorGraphTreeUnit u = new ReactorGraphTreeUnit();
							u.emptyReference = e.childs.size() == 0;
							u.member = e;
							units.add(u);
						}
					}
				}else{
					//add empty references as is
					units.add(unit);
				}
			}
		}else{
			//root
			for(ReactorGraphTree e : levelMembers){
				ReactorGraphTreeUnit u = new ReactorGraphTreeUnit();
				u.emptyReference = e.childs.size() == 0;
				u.member = e;
				units.add(u);
			}
		}
//		System.err.println("UNITS: "+level+": "+units);
		if(nextLevel != null){
			nextLevel.calculateStructure();
		}
	}
}
