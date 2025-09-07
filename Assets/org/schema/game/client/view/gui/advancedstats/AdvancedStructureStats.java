package org.schema.game.client.view.gui.advancedstats;

import java.util.List;

import javax.vecmath.Vector2f;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.advanced.AdvancedGUIBuildModeLeftElement;
import org.schema.game.client.view.gui.advanced.AdvancedGUIGroup;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.Timer;


public class AdvancedStructureStats extends AdvancedGUIBuildModeLeftElement{

	private int topOffsetX = 32;
	private int offsetLeft = 16;
	public AdvancedStructureStats(GameClientState state) {
		super(state);
	}

	


	@Override
	public GameClientState getState(){
		return (GameClientState)super.getState();
	}
	


	@Override
	protected Vector2f getInitialPos() {
		return new Vector2f(offsetLeft, topOffsetX);
	}
	public ManagerContainer<?> getMan(){
		SimpleTransformableSendableObject<?> s = getState().getCurrentPlayerObject();
		if(s instanceof ManagedSegmentController<?>){
			return ((ManagedSegmentController<?>)s).getManagerContainer();
		}
		return null;
	}
	@Override
	public boolean isActive() {
		return super.isActive() && getState().getPlayerInputs().isEmpty();
	}
	@Override
	public void draw() {
		if(getMan() == null){
			return;
		}
		setPos(offsetLeft, topOffsetX);
		super.draw();
	}
	@Override
	protected int getScrollerHeight() {

		return Math.min(
			GLFrame.getHeight() - 128,
			GLFrame.getHeight() 
				- (GLFrame.getHeight() - getState().getWorldDrawer().getGuiDrawer().getHud().getHelpManager().getLeftEndPosY())
				- 16
		
		);
		
	}
	@Override
	protected int getScrollerWidth() {
		return 128*2;
	}
	@Override
	protected void addGroups(List<AdvancedGUIGroup> g) {
		g.add(new AdvancedStructureStatsHelpTop(this));
		g.add(new AdvancedStructureStatsGeneral(this));
		g.add(new AdvancedStructureStatsStructure(this));
		g.add(new AdvancedStructureStatsPower(this));
		g.add(new AdvancedStructureStatsThruster(this));
		g.add(new AdvancedStructureStatsShield(this));
		g.add(new AdvancedStructureStatsWeapons(this));
		g.add(new AdvancedStructureStatsDocks(this));
		g.add(new AdvancedStructureStatsFaction(this));
	}

	@Override
	public void update(Timer timer) {
		super.update(timer);
		main.setHeightScroller(getScrollerHeight());
		main.setWidthScroller(getScrollerWidth());
	}


	@Override
	public boolean isSelected() {
		return false;
	}


	@Override
	public String getPanelName() {
		return Lng.str("Stats");
	}


	



	


}
