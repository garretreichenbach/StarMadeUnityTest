package org.schema.game.client.view.gui.advancedstats;

import org.lwjgl.glfw.GLFW;
import org.schema.common.FastMath;
import org.schema.common.util.StringTools;
import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.tools.ButtonCallback;
import org.schema.game.client.view.gui.advanced.tools.ButtonResult;
import org.schema.game.client.view.gui.advanced.tools.LabelResult;
import org.schema.game.client.view.gui.advanced.tools.StatLabelResult;
import org.schema.game.common.controller.elements.ShieldContainerInterface;
import org.schema.game.common.controller.elements.ShieldLocal;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableDirtyInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonColor;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.Keyboard;

public class AdvancedStructureStatsShield extends AdvancedStructureStatsGUISGroup{


	

	public AdvancedStructureStatsShield(AdvancedGUIElement e) {
		super(e);
	}
	private int pressedNext;
	
	public ShieldLocal getSelectedShield(){
		if(getMan() != null && getMan() instanceof ShieldContainerInterface && getMan().isUsingPowerReactors() && ((ShieldContainerInterface) getMan()).getShieldAddOn().getShieldLocalAddOn().getActiveShields().size() > 0){
			return ((ShieldContainerInterface) getMan()).getShieldAddOn().getShieldLocalAddOn().getActiveShields().get(
					pressedNext%((ShieldContainerInterface) getMan()).getShieldAddOn().getShieldLocalAddOn().getActiveShields().size());
		}
		return null;
	}
	@Override
	public void build(GUIContentPane pane, GUIDockableDirtyInterface dInt) {
		pane.setTextBoxHeightLast(UIScale.getUIScale().scale(30));
		
//		addLabel(pane.getContent(0), 0, 0, new LabelResult() {
//			@Override
//			public String getName() {
//				return Lng.str("Sizes");
//			}
//			@Override
//			public FontInterface getFontSize() {
//				return FontSize.BIG;
//			}
//			
//		});
		int y = 0;
		addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Active Shields:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				assert(getMan() != null);
				if (getMan() instanceof ShieldContainerInterface) {
					if(getMan().isUsingPowerReactors()){
						return String.valueOf(((ShieldContainerInterface) getMan()).getShieldAddOn().getShieldLocalAddOn().getActiveAvailableShields());
					}else{
						if(Keyboard.isKeyDown(GLFW.GLFW_KEY_0)){
							System.err.println("LEGACY::: "+getMan()+"; "+getMan().getSegmentController()+"; MAN_NEW "+getMan().isUsingPowerReactors()+"; OLDPOWER "+getMan().getSegmentController().isUsingOldPower()+"; fully loaded "+getMan().getSegmentController().isFullyLoaded());
						}
						return Lng.str("(Legacy)");
					}
				}
				return getNoneString();
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
			
			
		});
		addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Inactive Shields:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				assert(getMan() != null);
				if (getMan() instanceof ShieldContainerInterface) {
					if(getMan().isUsingPowerReactors()){
						return String.valueOf(((ShieldContainerInterface) getMan()).getShieldAddOn().getShieldLocalAddOn().getInactiveShields().size());
					}
				}
				return getNoneString();
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
			
			
		});
		
		addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Charge Blocks:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				if (getMan() instanceof ShieldContainerInterface) {
					return StringTools.formatSmallAndBig(((ShieldContainerInterface) getMan()).getShieldRegenManager().getTotalSize());
				}
				
				return getNoneString();
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Capacity Blocks:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				if (getMan() instanceof ShieldContainerInterface) {
					return StringTools.formatSmallAndBig(((ShieldContainerInterface) getMan()).getShieldCapacityManager().getTotalSize());
				}
				return getNoneString();
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		
		addButton(pane.getContent(0), 0, y, new ButtonResult() {
			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {
					

					@Override
					public void pressedRightMouse() {
					}
					
					@Override
					public void pressedLeftMouse() {
						pressedNext = (pressedNext+1)%100000;
					}
				};
			}
			
			@Override
			public String getName() {
				return Lng.str("Previous");
			}
			
			@Override
			public HButtonColor getColor() {
				return HButtonColor.BLUE;
			}
		});
		addButton(pane.getContent(0), 1, y++, new ButtonResult() {
			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {
					@Override
					public void pressedRightMouse() {
					}
					
					@Override
					public void pressedLeftMouse() {
						pressedNext = (pressedNext == 0 ? pressedNext = 100000 : pressedNext-1);
					}
				};
			}
			
			@Override
			public String getName() {
				return Lng.str("Next");
			}
			
			@Override
			public HButtonColor getColor() {
				return HButtonColor.BLUE;
			}
		});
		
		addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Pos:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				ShieldLocal s = getSelectedShield();
				if(s != null){
					return s.getPosString();
				}
				return "";
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Capacity (HP):");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				if(getMan() == null){
					return "";
				}
				if(getMan().isUsingPowerReactors()){
					ShieldLocal s = getSelectedShield();
					if(s != null){
						return StringTools.formatSmallAndBig(s.getShields());
					}
					return "";
				}else{
					return StringTools.formatSmallAndBig(((ShieldContainerInterface) getMan()).getShieldAddOn().getShields());
				}
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Capacity (max HP):");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				if(getMan() == null){
					return "";
				}
				
				if(getMan().isUsingPowerReactors()){
					ShieldLocal s = getSelectedShield();
					if(s != null){
						return StringTools.formatSmallAndBig(s.getShieldCapacity());
					}
				}else{
					return StringTools.formatSmallAndBig(((ShieldContainerInterface) getMan()).getShieldAddOn().getShieldCapacity());
				}
				return "";
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Recharge (HP/sec):");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				if(getMan() == null){
					return "";
				}
				if(getMan().isUsingPowerReactors()){
					ShieldLocal s = getSelectedShield();
					if(s != null){
						return StringTools.formatPointZero(s.rechargePerSecond);
					}
					return "";
				}else{
					return StringTools.formatSmallAndBig(((ShieldContainerInterface) getMan()).getShieldAddOn().getShieldRechargeRate());
				}
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Upkeep (HP/sec):");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				ShieldLocal s = getSelectedShield();
				if(s != null){
					return StringTools.formatSmallAndBig(s.getShieldUpkeep());
				}
				return "";
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Radius:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				ShieldLocal s = getSelectedShield();
				if(s != null){
					return Lng.str("%sm", FastMath.round(s.radius));
				}
				return "";
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Capacity Banks:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				ShieldLocal s = getSelectedShield();
				if(s != null){
					return String.valueOf(s.supportIds.size())+" / "+String.valueOf(VoidElementManager.SHIELD_LOCAL_MAX_CAPACITY_GROUPS_PER_LOCAL_SHIELD);
				}
				return "";
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		if(hasIntegrity()) {
			addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
				@Override
				public String getName() {
					return Lng.str("Struc. Integrity:");
				}
				@Override
				public FontInterface getFontSize() {
					return FontSize.MEDIUM_15;
				}
				@Override
				public String getValue() {
					ShieldLocal s = getSelectedShield();
					if(s != null){
						return String.valueOf(Math.round(s.getIntegrity()));
					}
					return "";
				}
				
				@Override
				public String getToolTipText() {
					return super.getToolTipText()+"\n"+Lng.str("WARNING: A integrity lower than %s is dangerous and can lead to system implosion on shield damage!",VoidElementManager.INTEGRITY_MARGIN);
				}
				@Override
				public int getStatDistance() {
					return getTextDist();
				}
			});
		}
		addLabel(pane.getContent(0), 0, y++, new LabelResult() {
			@Override
			public String getName() {
				return Lng.str("Consumption");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.BIG_20;
			}
			
		});
		addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Resting (e/sec):");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				ShieldLocal s = getSelectedShield();
				if(s != null){
					return StringTools.formatSmallAndBig(s.getPowerConsumedPerSecondResting());
				}
				return "";
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Charging (e/sec):");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				if(getMan() == null){
					return "";
				}
				if(getMan().isUsingPowerReactors()){
					ShieldLocal s = getSelectedShield();
					if(s != null){
						return StringTools.formatSmallAndBig(s.getPowerConsumedPerSecondCharging());
					}
					return "";
				}else{
					return StringTools.formatSmallAndBig(((ShieldContainerInterface) getMan()).getShieldAddOn().getLastPowerConsumption());
				}
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		
		
		
		
		
		
	}
	private int getTextDist() {
		return 150;
	}
	
	@Override
	public String getId() {
		return "ASSHIELDS";
	}

	@Override
	public String getTitle() {
		return Lng.str("Shields");
	}




}
