package org.schema.game.client.view.gui.advancedEntity;

import javax.vecmath.Vector4f;

import org.schema.game.client.controller.manager.ingame.PlayerGameControlManager;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.AdvancedGUIGroup;
import org.schema.game.client.view.gui.advanced.tools.BlockDisplayResult;
import org.schema.game.client.view.gui.advanced.tools.BlockSelectCallback;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.power.reactor.PowerInterface;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;

public abstract class AdvancedEntityGUIGroup extends AdvancedGUIGroup{

	public AdvancedEntityGUIGroup(AdvancedGUIElement e) {
		super(e);
	}
	public PlayerGameControlManager getPlayerGameControlManager() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager();
	}
	public PlayerInteractionControlManager getPlayerInteractionControlManager() {
		return getPlayerGameControlManager().getPlayerIntercationManager();
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
	public int getTypeCount(short type) {
		if( getState().getPlayer() != null && getState().getPlayer().getInventory().existsInInventory(type)) {
			return getState().getPlayer().getInventory().getOverallQuantity(type);
		}else {
			return 0;
		}
	}
	public int getTextDist() {
		return 150;
	}
	public void addWeaponBlockIcon(GUIContentPane pane, int x, int y, final Object tooltip, final InitInterface init) {
		addBlockDisplay(pane.getContent(0), x, y, new BlockDisplayResult() {
			
			@Override
			public BlockSelectCallback initCallback() {
				return value -> {
				};
			}
			
			@Override
			public String getToolTipText() {
				return tooltip.toString();
			}
			
			@Override
			public Vector4f getBackgroundColor() {
				if(getTypeCount(init.getType()) > 0) {
					return GREEN;
				}else {
					return RED;
				}
				
			}

			public void afterInit(GUIOverlay blockOverlay) {
				GUITextOverlay availability = new GUITextOverlay(FontSize.MEDIUM_15, getState()) {

					@Override
					public void draw() {
						if(getTypeCount(init.getType()) > 0) {
							setColor(GREEN);
						}else {
							setColor(RED);
						}
						super.draw();
					}
					
				};
				availability.setTextSimple(new Object() {

					@Override
					public String toString() {
						if(getTypeCount(init.getType()) > 0) {
							return Lng.str("%s\navailable", getTypeCount(init.getType()));
						}else {
							return Lng.str("not\navailable");
						}
					}
				});
				
				availability.setPos(3, 3, 0);
				blockOverlay.attach(availability);
			}
			@Override
			public short getDefault() {
				return init.getType();
			}
			
			@Override
			public short getCurrentValue() {
				return init.getType();
			}
		});
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
	
	public boolean canQueue(short type, int amount) {
		return getPlayerInteractionControlManager().canQueue(type, amount);
	}
	public void resetQueue() {
		getPlayerInteractionControlManager().resetQueue();
	}
	public void promptBuild(short type, int amount, String info) {
		getPlayerInteractionControlManager().promptBuild(type, amount, info);
	}
	public boolean isCommandQueued() {
		return getPlayerInteractionControlManager().getBuildCommandManager().isCommandQueued();
	}
}
