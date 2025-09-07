package org.schema.game.client.view.gui.advancedstats;

import javax.vecmath.Vector3f;

import org.schema.common.util.StringTools;
import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.tools.StatLabelResult;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.ManagerThrustInterface;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableDirtyInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;

public class AdvancedStructureStatsThruster extends AdvancedStructureStatsGUISGroup{


	

	public AdvancedStructureStatsThruster(AdvancedGUIElement e) {
		super(e);
	}
	
	@Override
	public void build(GUIContentPane pane, GUIDockableDirtyInterface dInt) {
		pane.setTextBoxHeightLast(UIScale.getUIScale().scale(30));
		
//		addLabel(pane.getContent(0), 0, 0, new LabelResult() {
//			@Override
//			public String getName() {
//				return Lng.str("Thrust");
//			}
//			@Override
//			public FontInterface getFontSize() {
//				return FontSize.BIG;
//			}
//			
//		});
		addStatLabel(pane.getContent(0), 0, 0, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Blocks:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				if (getMan() instanceof ManagerThrustInterface) {
					String blocks = StringTools.formatSmallAndBig((((ManagerThrustInterface) getMan()).getThrust().getCollectionManager().getTotalSize()));
					return blocks;
				}
				return "";
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
			
			
			
		});
		addStatLabel(pane.getContent(0), 0, 1, new StatLabelResult() {
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
				assert(getMan() != null);
				if (getMan() instanceof ManagerThrustInterface) {
					String tStr = StringTools.formatSmallAndBig(((ManagerThrustInterface) getMan()).getThrusterElementManager().getActualThrust());
					return tStr;
				}
				return "";
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
			
		});
		addStatLabel(pane.getContent(0), 0, 2, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Max Speed:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				if (getMan() instanceof ManagerThrustInterface) {
					String maxSpeed = StringTools.formatPointZero(((Ship) getSegCon()).getCurrentMaxVelocity());
					return maxSpeed;
				}
				return "";
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
			
		});
		addStatLabel(pane.getContent(0), 0, 3, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Consumption (e/sec):");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				if (getMan() instanceof ManagerThrustInterface) {
					return StringTools.formatPointZero(((ManagerThrustInterface) getMan()).getThrusterElementManager().getPowerConsumedPerSecondCharging());			
				}
				return "";
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		addStatLabel(pane.getContent(0), 0, 4, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Thrust/Mass Ratio:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				if (getMan() instanceof ManagerThrustInterface) {
					float r = ((ManagerThrustInterface) getMan()).getThrusterElementManager().getThrustMassRatio();
					String ratio = StringTools.formatPointZero(r);
					return ratio;
				}
				return "";
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		
		
		addStatLabel(pane.getContent(0), 0, 5, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Turning Speed:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				if (getMan() instanceof ManagerThrustInterface) {
					
					Vector3f ii = ((Ship)getSegCon()).getOrientationForce();
					return StringTools.formatPointZero(ii.x)+", "+
							StringTools.formatPointZero(ii.y)+", "+
							StringTools.formatPointZero(ii.z);
				}
				return "";
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		if(hasIntegrity()) {
			addStatLabel(pane.getContent(0), 0, 6, new StatLabelResult() {
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
					if (getMan() instanceof ManagerThrustInterface) {
						double integrity = ((ManagerThrustInterface)getMan()).getThrusterElementManager().lowestIntegrity;
						if(integrity == Double.POSITIVE_INFINITY){
							return Lng.str("n/a");
						}
						return String.valueOf((int)integrity);				
					}
					return "";
				}
				@Override
				public int getStatDistance() {
					return getTextDist();
				}
			});
		}
		
	}
	private int getTextDist() {
		return 150;
	}
	
	@Override
	public String getId() {
		return "ASTHRUSTER";
	}

	@Override
	public String getTitle() {
		return Lng.str("Thrusters");
	}




}
