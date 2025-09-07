package org.schema.game.client.view.gui.advancedbuildmode;

import javax.vecmath.Vector4f;

import org.schema.game.client.controller.manager.ingame.AbstractSizeSetting;
import org.schema.game.client.controller.manager.ingame.BuildToolsManager;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.controller.manager.ingame.SymmetryPlanes;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.AdvancedGUIGroup;
import org.schema.game.client.view.gui.advanced.tools.SliderCallback;
import org.schema.game.client.view.gui.advanced.tools.SliderResult;
import org.schema.schine.graphicsengine.forms.gui.newgui.settingsnew.GUIScrollSettingSelector;

public abstract class AdvancedBuildModeGUISGroup extends AdvancedGUIGroup{

	public AdvancedBuildModeGUISGroup(AdvancedGUIElement e) {
		super(e);
	}
	public BuildToolsManager getBuildToolsManager() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager();
	}
	public PlayerInteractionControlManager getPlayerInteractionControlManager() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
	}
	public SymmetryPlanes getSymmetryPlanes() {
		PlayerInteractionControlManager pp = getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
		if (pp.getInShipControlManager().getShipControlManager().getSegmentBuildController().isTreeActive()) {
			return pp.getInShipControlManager().getShipControlManager().getSegmentBuildController().getSymmetryPlanes();
		} else {
			return pp.getSegmentControlManager().getSegmentBuildController().getSymmetryPlanes();
		}
	}
	@Override
	public GameClientState getState(){
		return (GameClientState)super.getState();
	}
	protected abstract class SizeSliderResult extends SliderResult{
		private AbstractSizeSetting size;

		
		public SizeSliderResult(AbstractSizeSetting e){
			this.size = e;
		}
		@Override
		public float getDefault() {
			return size.setting;
		}
		@Override
		public void onInitializeScrollSetting(GUIScrollSettingSelector scrollSetting) {
			size.guiCallBack = scrollSetting;
		}
		@Override
		public float getMax() {
			return size.getMax();
		}

		@Override
		public float getMin() {
			return size.getMin();
		}
		@Override
		public boolean showLabel(){
			return false;
		}
		@Override
		public String getName() {
			return "SizeLabel";
		}
		@Override
		public SliderCallback initCallback() {
			return value -> size.set(value);
		}
		
	}
	@Override
	public void setInitialBackgroundColor(Vector4f bgColor) {
		bgColor.set(1,1,1,0.65f);
	}
	@Override
	public int getSubListIndex() {
		return 1;
	}
	@Override
	public boolean isExpandable() {
		return true;
	}
	@Override
	public boolean isClosable() {
		return false;
	}
}
