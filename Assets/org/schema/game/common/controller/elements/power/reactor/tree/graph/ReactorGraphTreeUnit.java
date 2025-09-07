package org.schema.game.common.controller.elements.power.reactor.tree.graph;

public class ReactorGraphTreeUnit {
	public ReactorGraphTree member;
	public boolean emptyReference;
	public boolean placed;
	public boolean isOnThisLevel(ReactorGraphTreeLevel lvl) {
		return !emptyReference || member.level == lvl;
	}
	
	@Override
	public String toString(){
		return (emptyReference ? "*" : "")+member.toString();
	}

}
