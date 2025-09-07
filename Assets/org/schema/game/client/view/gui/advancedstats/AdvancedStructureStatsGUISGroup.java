package org.schema.game.client.view.gui.advancedstats;

import javax.vecmath.Vector4f;

import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.AdvancedGUIGroup;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.power.reactor.PowerInterface;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;

public abstract class AdvancedStructureStatsGUISGroup extends AdvancedGUIGroup{

	public AdvancedStructureStatsGUISGroup(AdvancedGUIElement e) {
		super(e);
	}
	public PlayerInteractionControlManager getPlayerInteractionControlManager() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
	}
	@Override
	public GameClientState getState(){
		return (GameClientState)super.getState();
	}
	public ManagerContainer<?> getMan(){
		SimpleTransformableSendableObject<?> s = getState().getCurrentPlayerObject();
		if(s instanceof ManagedSegmentController<?>){
			return ((ManagedSegmentController<?>)s).getManagerContainer();
		}
		return null;
	}
	public PowerInterface getPI(){
		return getMan().getPowerInterface();
	}
	public boolean hasIntegrity() {
		return getSegCon() != null && getSegCon().hasIntegrityStructures();
	}
	public SegmentController getSegCon(){
		if(getMan() == null){
			return null;
		}
		return getMan().getSegmentController();
	}
	public String getNoneString(){
		return Lng.str("none");
	}
	@Override
	public void setInitialBackgroundColor(Vector4f bgColor) {
		bgColor.set(1,1,1,0.65f);
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
