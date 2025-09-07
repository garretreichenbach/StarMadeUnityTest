package org.schema.game.client.view.gui.advancedEntity;

import org.schema.common.util.StringTools;
import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.tools.ButtonCallback;
import org.schema.game.client.view.gui.advanced.tools.ButtonResult;
import org.schema.game.client.view.gui.advanced.tools.LabelResult;
import org.schema.game.client.view.gui.advanced.tools.StatLabelResult;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.ShipManagerContainer;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableDirtyInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonColor;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;

public class AdvancedEntityThruster extends AdvancedEntityGUIGroup{

	
	

	public AdvancedEntityThruster(AdvancedGUIElement e) {
		super(e);
	}

	
	@Override
	public String getId() {
		return "AETHRUSTER";
	}

	@Override
	public String getTitle() {
		return Lng.str("Thrusters");
	}
	@Override
	public boolean isActive() {
		return super.isActive() && getMan() != null && getSegCon() instanceof Ship;
	}
	public ShipManagerContainer getMan() {
		return super.getMan() instanceof ShipManagerContainer ? (ShipManagerContainer)super.getMan() : null;
	}
	public int addThrusterBlockIcons(GUIContentPane pane, int x, int y) {
		addStatLabel(pane.getContent(0), x, y++, new StatLabelResult() {
			
			@Override
			public String getName() {
				return Lng.str("Thruster:");
			}
			
			@Override
			public String getValue() {
				return ElementKeyMap.getInfo(ElementKeyMap.THRUSTER_ID).getName();
			}
			
			@Override
			public int getStatDistance() {
				return 100;
			}
		});
		
		
		
		addWeaponBlockIcon(pane, x, y++, new Object() {
			public String toString() {
				if(getMan() != null ) {
					return Lng.str("Thruster (%s)", ElementKeyMap.getInfo(ElementKeyMap.THRUSTER_ID).getName());
				}
				return "";
			}
		}, new InitInterface() {
		
			public short getType() {
				return ElementKeyMap.THRUSTER_ID;
			}
			@Override
			public boolean isInit() {
				return getMan() != null;
			}
		});
		
		
		return y;
	}
	@Override
	public void build(GUIContentPane pane, GUIDockableDirtyInterface dInt) {
		pane.setTextBoxHeightLast(UIScale.getUIScale().scale(30));
		int indexY = 0;
		
		addButton(pane.getContent(0), 0, indexY++, new ButtonResult() {
			
			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {
					@Override
					public void pressedRightMouse() {
					}
					
					@Override
					public void pressedLeftMouse() {
						if(AdvancedEntityThruster.this.isActive()){
							getPlayerGameControlManager().activateThrustManager((Ship) getSegCon());
						}
					}
				};
			}
			
			@Override
			public String getName() {
				return Lng.str("Open Thruster Settings");
			}
			
			@Override
			public HButtonColor getColor() {
				return HButtonColor.BLUE;
			}
		});
		
		indexY = addThrusterBlockIcons(pane, 0, indexY);
		
		addButton(pane.getContent(0), 0, indexY++, new ButtonResult() {
			
			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {
					@Override
					public void pressedRightMouse() {
					}
					
					@Override
					public void pressedLeftMouse() {
						resetQueue();
						promptBuild(ElementKeyMap.THRUSTER_ID, 1, Lng.str("Build thrusters. The more mass a ship has, the more are needed for the same acceleration."));
						
					}

					
				};
			}
			@Override
			public boolean isActive() {
				return super.isActive() && canQueue(ElementKeyMap.THRUSTER_ID, 1);
			}
			@Override
			public String getName() {
				return Lng.str("Build");
			}
			
			@Override
			public HButtonColor getColor() {
				return HButtonColor.BLUE;
			}
		});
		addLabel(pane.getContent(0), 0, indexY++, new LabelResult() {
			@Override
			public String getName() {
				return Lng.str("Thrusters");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.BIG_20;
			}
		});
		addStatLabel(pane.getContent(0), 0, indexY++, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Size:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				
				if(AdvancedEntityThruster.this.isActive()){
					return StringTools.formatSeperated(getMan().getThrusterElementManager().totalSize);
				}else {
					return Lng.str("n/a");
				}
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		addStatLabel(pane.getContent(0), 0, indexY++, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Thrust:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				
				if(AdvancedEntityThruster.this.isActive()){
					return StringTools.formatPointZero(getMan().getThrusterElementManager().getActualThrust());
				}else {
					return Lng.str("n/a");
				}
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		if(hasIntegrity()) {
			addStatLabel(pane.getContent(0), 0, indexY++, new StatLabelResult() {
				@Override
				public String getName() {
					return Lng.str("Integrity:");
				}
				@Override
				public FontInterface getFontSize() {
					return FontSize.MEDIUM_15;
				}
				@Override
				public String getValue() {
					if(AdvancedEntityThruster.this.isActive()){
						return StringTools.formatPointZero(getMan().getThrusterElementManager().lowestIntegrity);
					}else {
						return Lng.str("n/a");
					}
				}
				@Override
				public int getStatDistance() {
					return getTextDist();
				}
			});
		}
		
	}
	
	
}
