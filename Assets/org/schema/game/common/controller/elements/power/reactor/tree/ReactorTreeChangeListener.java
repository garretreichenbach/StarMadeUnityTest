package org.schema.game.common.controller.elements.power.reactor.tree;

public interface ReactorTreeChangeListener {

	public void onReceivedTree();

	public void onReactorSizeChanged(ReactorTree t, boolean damaged);

}
