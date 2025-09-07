package org.schema.game.client.view.cubes.shapes;

import org.schema.game.common.data.element.ElementKeyMap;

public class GeneralBlockStyle implements Comparable<GeneralBlockStyle>{
	
	public BlockStyle blockStyle;
	
	public int slab;
	
	public int wildcard;
	
	public int index;
	
	public boolean permanent;
	
	public static int getId(short type){
		return getId(ElementKeyMap.getInfo(type).blockStyle, ElementKeyMap.getInfo(type).slab, ElementKeyMap.getInfo(type).wildcardIndex);
	}
	public static int getId(BlockStyle blockStyle, int slab, int wildcard){
		return blockStyle.id + slab * 100 + wildcard*10000;
	}
	public int getId(){
		return getId(blockStyle, slab, wildcard);
	}

	@Override
	public int hashCode() {
		return getId();
	}

	@Override
	public boolean equals(Object obj) {
		//hashcode is unique
		return hashCode() == obj.hashCode();
	}

	@Override
	public int compareTo(GeneralBlockStyle o) {
		return index - o.index;
	}

	@Override
	public String toString() {
		return "GeneralBlockStyle [ID="+getId()+", blockStyle=" + blockStyle + ", slab=" + slab
				+ ", index=" + index + ", permanent=" + permanent + "]";
	}
	
	
}
