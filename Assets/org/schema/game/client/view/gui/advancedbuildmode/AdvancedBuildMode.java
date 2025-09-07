package org.schema.game.client.view.gui.advancedbuildmode;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.AdvancedGUIGroup;
import org.schema.game.client.view.gui.advanced.AdvancedGUIMinimizeCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;

import javax.vecmath.Vector2f;
import java.io.IOException;
import java.util.List;


public class AdvancedBuildMode extends AdvancedGUIElement{


	public AdvancedBuildMode(GameClientState state) {
		super(state);
		
		final AdvancedGUIMinimizeCallback minimizeCallback = new AdvancedGUIMinimizeCallback(state, true){

			@Override
			public boolean isActive() {
				return true;
			}

			@Override
			public void initialMinimized() {
				setMinimizedInitial(EngineSettings.ADVBUILDMODE_MINIMIZED.isOn());
			}
			protected int closeLashButtonOffsetX(){
				return 0;
			}
			@Override
			public void onMinimized(boolean minimized) {
				EngineSettings.ADVBUILDMODE_MINIMIZED.setOn(minimized);
				try {
					EngineSettings.write();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			protected boolean isCloseLashOnRight() {
				return false;
			}

			@Override
			public String getMinimizedText() {
				return Lng.str("\\/ Adv. Build Mode \\/");
			}

			@Override
			public String getMaximizedText() {
				return Lng.str("/\\");
			}
		};
		setMinimizeCallback(minimizeCallback);
	}

	
	@Override
	public GameClientState getState(){
		return (GameClientState)super.getState();
	}
	
	@Override
	protected Vector2f getInitialPos() {
		return new Vector2f((int)(GLFrame.getWidth()-getWidth()), 32);
	}
	

	@Override
	public void draw() {
		setPos((int)(GLFrame.getWidth()-getWidth()), 32);
		super.draw();
	}
	@Override
	public boolean isActive() {
		return super.isActive() && getState().getPlayerInputs().isEmpty();
	}
	@Override
	protected int getScrollerHeight() {
		return GLFrame.getHeight()-128;
	}
	@Override
	protected int getScrollerWidth() {
		return 128*2+64;
	}
	@Override
	protected void addGroups(List<AdvancedGUIGroup> g) {
		g.add(new AdvancedBuildModeHelpTop(this));
		g.add(new AdvancedBuildModeBlockPreview(this));
		g.add(new AdvancedBuildModeBrushSize(this));
		g.add(new AdvancedBuildModeSymmetry(this));
		g.add(new AdvancedBuildModeSelection(this));
		g.add(new AdvancedBuildModeHotbar(this));
		g.add(new AdvancedBuildModeColorFinder(this));
		g.add(new AdvancedBuildModeFill(this));
		g.add(new AdvancedBuildModeShape(this));
		g.add(new AdvancedBuildModeDocking(this));
//		g.add(new AdvancedBuildModeReactor(this));
		g.add(new AdvancedBuildModeDisplay(this));
		g.add(new AdvancedBuildModeHelp(this));	
	}

	@Override
	public void update(Timer timer) {
		super.update(timer);
	}


	@Override
	public boolean isSelected() {
		return false;
	}


	



	


}
