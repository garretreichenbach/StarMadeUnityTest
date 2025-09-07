package org.schema.game.client.view.gui.advancedstats;

import org.schema.common.FastMath;
import org.schema.common.util.StringTools;
import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.tools.LabelResult;
import org.schema.game.client.view.gui.advanced.tools.StatLabelResult;
import org.schema.game.common.controller.elements.ManagerModuleSingle;
import org.schema.game.common.controller.elements.StabBonusCalcStyle;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.controller.elements.power.PowerAddOn;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;
import org.schema.game.common.controller.elements.power.reactor.PowerImplementation;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberCollectionManager;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberElementManager;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberUnit;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableDirtyInterface;

import javax.vecmath.Vector4f;

public class AdvancedStructureStatsPower extends AdvancedStructureStatsGUISGroup{


	

	public AdvancedStructureStatsPower(AdvancedGUIElement e) {
		super(e);
	}
	
	@Override
	public void build(GUIContentPane pane, GUIDockableDirtyInterface dInt) {
		pane.setTextBoxHeightLast(30);
		int y = 0;
		addLabel(pane.getContent(0), 0, y++, new LabelResult() {
			@Override
			public String getName() {
				return Lng.str("Sizes");
			}
			@Override
			public FontSize getFontSize() {
				return FontSize.BIG_20;
			}
			
		});
		addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
			@Override
			public String getName() {
				if(getMan().isUsingPowerReactors()){
					return Lng.str("Reactor Blocks:");
				}else{
					return Lng.str("Power Blocks:");
				}
			}
			@Override
			public FontSize getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				assert(getMan() != null);
				if(getMan().isUsingPowerReactors()){
					return StringTools.formatSmallAndBig(getMan().getMainReactor().getTotalSize());
				}else{
					return StringTools.formatSmallAndBig(getMan().getSegmentController().getElementClassCountMap().get(ElementKeyMap.POWER_ID_OLD));
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
				return Lng.str("Stabilizers:");
			}
			@Override
			public FontSize getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				if(getMan().isUsingPowerReactors()){
					return StringTools.formatSmallAndBig(getMan().getStabilizer().getTotalSize());
				}else{
					return Lng.str("n/a (legacy power)");
				}
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		if(VoidElementManager.isUsingReactorDistance()){
		addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Stabilizer Groups:");
			}
			@Override
			public FontSize getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				if(getMan().isUsingPowerReactors()){
					if(PowerImplementation.getMaxStabilizerCount() < Integer.MAX_VALUE){
						return getMan().getStabilizer().getElementCollections().size() +" / "+PowerImplementation.getMaxStabilizerCount();
					}else{
						return String.valueOf(getMan().getStabilizer().getElementCollections().size());
					}
				}else{
					return Lng.str("n/a (legacy power)");
				}
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		}
		addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Chamber Blocks:");
			}
			@Override
			public FontSize getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				if(getMan().isUsingPowerReactors()){
					int i = 0;
					for(ManagerModuleSingle<ReactorChamberUnit, ReactorChamberCollectionManager, ReactorChamberElementManager> e : getMan().getChambers()){
						i += e.getElementManager().totalSize;
					}
					return StringTools.formatSmallAndBig(i);
				}else{
					return Lng.str("n/a (legacy power)");
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
				return Lng.str("Conduits:");
			}
			@Override
			public FontSize getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				if(getMan().isUsingPowerReactors()){
					return StringTools.formatSmallAndBig(getMan().getConduit().getTotalSize());
				}else{
					return Lng.str("n/a (legacy power)");
				}
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		
		addLabel(pane.getContent(0), 0, y++, new LabelResult() {
			@Override
			public String getName() {
				return Lng.str("Active Reactor Stats");
			}
			@Override
			public FontSize getFontSize() {
				return FontSize.BIG_20;
			}
			
		});
		if(hasIntegrity()) {
			addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
				@Override
				public String getName() {
					return Lng.str("Integrity:");
				}
				@Override
				public FontSize getFontSize() {
					return FontSize.MEDIUM_15;
				}
				@Override
				public String getValue() {
					if(getMan().isUsingPowerReactors()){
						double activeReactorIntegrity = getPI().getActiveReactorIntegrity();					
						if(activeReactorIntegrity >= VoidElementManager.INTEGRITY_MARGIN){
							return Lng.str("%s (OK)", (int)activeReactorIntegrity);
						}else{
							return Lng.str("%s (WARNING)", (int)activeReactorIntegrity);
						}
					}else{
						return Lng.str("n/a");
					}
				}
				@Override
				public String getToolTipText() {
					return super.getToolTipText()+"\n"+Lng.str("WARNING: A integrity lower than %s is dangerous and can lead to system implosion on damage!",(int)VoidElementManager.INTEGRITY_MARGIN);
				}
				@Override
				public int getStatDistance() {
					return getTextDist();
				}
				
			});
		}
		addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Recharge (e/sec):");
			}
			@Override
			public FontSize getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				if(getMan().isUsingPowerReactors()){
					return StringTools.formatSmallAndBig(getPI().getRechargeRatePowerPerSec());
				}else{
					if(getMan() instanceof PowerManagerInterface){
						PowerAddOn powerAddOn = ((PowerManagerInterface)getMan()).getPowerAddOn();
						return StringTools.formatSmallAndBig(powerAddOn.getRecharge());
					}else{
						return "";
					}
				}
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
			
		});
		
		addLabel(pane.getContent(0), 0, y++, new LabelResult() {
			@Override
			public String getName() {
				return Lng.str("Consumption");
			}
			@Override
			public FontSize getFontSize() {
				return FontSize.BIG_20;
			}
			
		});
		addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
			private Vector4f color = new Vector4f(WHITE);
			@Override
			public String getName() {
				return Lng.str("Usage (incl docks):");
			}
			@Override
			public FontSize getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				if(getMan().isUsingPowerReactors()){
					double perc = getPI().getPowerConsumptionAsPercent();
					if(perc > 1d) {
						color.set(1, 0, 0, 1);
					}else {
						color.set(0, 1, 0, 1);
					}
					return StringTools.formatPointZero(perc * 100f)+"%";
				}else{
					if(getMan() instanceof PowerManagerInterface){
						PowerAddOn powerAddOn = ((PowerManagerInterface)getMan()).getPowerAddOn();
						return StringTools.formatSmallAndBig(powerAddOn.getPowerConsumedPerSecond());
					}else{
						return "";
					}
				}
			}
			public Vector4f getFontColor(){
				return color;
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("with docks (e/sec):");
			}
			@Override
			public FontSize getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				if(getMan().isUsingPowerReactors()){
					return StringTools.formatSmallAndBig(getPI().getCurrentConsumptionPerSec());
				}else{
					if(getMan() instanceof PowerManagerInterface){
						PowerAddOn powerAddOn = ((PowerManagerInterface)getMan()).getPowerAddOn();
						return StringTools.formatSmallAndBig(powerAddOn.getPowerConsumedPerSecond());
					}else{
						return "";
					}
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
				return Lng.str("w/o docks:");
			}
			@Override
			public FontSize getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				if(getMan().isUsingPowerReactors()){
					return Lng.str("%s e/sec", StringTools.formatSmallAndBig(getPI().getCurrentLocalConsumptionPerSec()));
				}else{
					return "n/a";
				}
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		addLabel(pane.getContent(0), 0, y++, new LabelResult() {
			@Override
			public String getName() {
				return Lng.str("Stabilization");
			}
			@Override
			public FontSize getFontSize() {
				return FontSize.BIG_20;
			}
			
		});
		
		if(VoidElementManager.hasAngleOrSideStabBonus()) {
			if(VoidElementManager.STABILIZER_BONUS_CALC == StabBonusCalcStyle.BY_ANGLE) {

				addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
					@Override
					public String getName() {
						return Lng.str("Angle stabs(5): ");
					}
					@Override
					public FontSize getFontSize() {
						return FontSize.MEDIUM_15;
					}
					@Override
					public String getValue() {
						if(getMan().isUsingPowerReactors()){
							StringBuffer b = new StringBuffer();

							if(getPI().getStabilizerCollectionManager().getAngleUsedList().size() == 0) {
								b.append("n/a");
							}else {

								for(int i = 0; i < getPI().getStabilizerCollectionManager().getAngleUsedList().size(); i++){
									b.append(Math.round(FastMath.RAD_TO_DEG* getPI().getStabilizerCollectionManager().getAngleUsedList().get(i))+"°");
									if(i < getPI().getStabilizerCollectionManager().getAngleUsedList().size() -1) {
										b.append(", ");
									}
								}
							}

							return Lng.str("%s", b.toString());
						}else{
							return Lng.str("n/a (legacy power)");
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
						return Lng.str("+Angle mults: ");
					}
					@Override
					public FontSize getFontSize() {
						return FontSize.MEDIUM_15;
					}
					@Override
					public String getValue() {
						if(getMan().isUsingPowerReactors()){
							StringBuffer b = new StringBuffer();
							if(getPI().getStabilizerCollectionManager().getAngleBonusList().size() == 0) {
								b.append("n/a");
							}else {
								for(int i = 0; i < getPI().getStabilizerCollectionManager().getAngleBonusList().size(); i++){
									b.append(StringTools.formatPointZero(getPI().getStabilizerCollectionManager().getAngleBonusList().get(i)));
									if(i < getPI().getStabilizerCollectionManager().getAngleBonusList().size() -1) {
										b.append(", ");
									}
								}
							}


							return Lng.str("%s", b.toString());
						}else{
							return Lng.str("n/a (legacy power)");
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
						return Lng.str("+Angle Stabilization: ");
					}
					@Override
					public FontSize getFontSize() {
						return FontSize.MEDIUM_15;
					}
					@Override
					public String getValue() {
						if(getMan().isUsingPowerReactors()){
							StringBuffer b = new StringBuffer();

							if(getPI().getStabilizerCollectionManager().getAngleBonusTotalList().size() == 0) {
								b.append("n/a");
							}else {
								for(int i = 0; i < getPI().getStabilizerCollectionManager().getAngleBonusTotalList().size(); i++){
									b.append(Math.round(getPI().getStabilizerCollectionManager().getAngleBonusTotalList().get(i)));
									if(i < getPI().getStabilizerCollectionManager().getAngleBonusTotalList().size() -1) {
										b.append(" + ");
									}
								}
							}

							return Lng.str("%s", b.toString());
						}else{
							return Lng.str("n/a (legacy power)");
						}
					}
					@Override
					public int getStatDistance() {
						return getTextDist();
					}
				});

			}else {

				addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
					@Override
					public String getName() {
						return Lng.str("Dimensions Used: ");
					}
					@Override
					public FontSize getFontSize() {
						return FontSize.MEDIUM_15;
					}
					@Override
					public String getValue() {
						if(getMan().isUsingPowerReactors()){
							return Lng.str("%s of 6", String.valueOf(getPI().getStabilizerCollectionManager().getGetDimsUsed()));
						}else{
							return Lng.str("n/a (legacy power)");
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
						return Lng.str("Bonus efficiency for\nequally big groups: ");
					}
					@Override
					public FontSize getFontSize() {
						return FontSize.MEDIUM_15;
					}
					@Override
					public String getValue() {
						if(getMan().isUsingPowerReactors()){
							return "\n"+StringTools.formatPointZero(getPI().getStabilizerCollectionManager().getBonusEfficiency());
						}else{
							return Lng.str("\nn/a (legacy power)");
						}
					}

					@Override
					public String getToolTipText() {
						return super.getToolTipText()+Lng.str(" bonus efficiency goes down if there are smaller groups compared to the others");
					}
					@Override
					public int getStatDistance() {
						return getTextDist();
					}
				});
			}
			addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
				@Override
				public String getName() {
					return Lng.str("Without Bonus: ");
				}
				@Override
				public FontSize getFontSize() {
					return FontSize.MEDIUM_15;
				}
				@Override
				public String getValue() {
					if(getMan().isUsingPowerReactors()){
						return StringTools.formatPointZero(getPI().getStabilizerCollectionManager().getStabilizationWithoutBonus());
					}else{
						return Lng.str("n/a (legacy power)");
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
					return VoidElementManager.STABILIZER_BONUS_CALC == StabBonusCalcStyle.BY_ANGLE ? Lng.str("Angle Bonus Sum: ") : Lng.str("Dimension Bonus Mult: ");
				}
				@Override
				public FontSize getFontSize() {
					return FontSize.MEDIUM_15;
				}
				@Override
				public String getValue() {
					if(getMan().isUsingPowerReactors()){
						return StringTools.formatPointZero(getPI().getStabilizerCollectionManager().getStabilizationBonus());
					}else{
						return Lng.str("n/a (legacy power)");
					}
				}
				@Override
				public int getStatDistance() {
					return getTextDist();
				}
			});
		}
		addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Total: ");
			}
			@Override
			public FontSize getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				if(getMan().isUsingPowerReactors()){
					return StringTools.formatPointZero(getPI().getStabilizerCollectionManager().getStabilization());
				}else{
					return Lng.str("n/a (legacy power)");
				}
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
//		addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
//			@Override
//			public String getName() {
//				return Lng.str("Stabilization:\n(with buffer)");
//			}
//			@Override
//			public FontSize getFontSize() {
//				return FontSize.MEDIUM;
//			}
//			@Override
//			public String getValue() {
//				if(getMan().isUsingPowerReactors()){
//					String s = StringTools.formatPointZero(getPI().getStabilizerEfficiencyTotal() * 100.0) + "%";
//					if(getPI().getStabilizerEfficiencyExtra() > 0d){
//						s += "\n" + StringTools.formatPointZero(getPI().getStabilizerEfficiencyExtra() * 100.0) + "%";
//					}
//					return s;
//				}else{
//					return Lng.str("n/a (legacy power)");
//				}
//			}
//			@Override
//			public int getStatDistance() {
//				return getTextDist();
//			}
//		});
		if(hasIntegrity()) {
			addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
				@Override
				public String getName() {
					return Lng.str("Stab. Sys Integrity:");
				}
				@Override
				public FontSize getFontSize() {
					return FontSize.MEDIUM_15;
				}
				@Override
				public String getValue() {
					if(getMan().isUsingPowerReactors()){
						if(getPI().getStabilizerIntegrity() < Double.POSITIVE_INFINITY){
							long integrity = FastMath.round(getPI().getStabilizerIntegrity());
							return integrity >= VoidElementManager.INTEGRITY_MARGIN ? Lng.str("%s (OK)", integrity) : Lng.str("%s (WARNING)", integrity);
						}else{
							return Lng.str("No stabilizers");
						}
					}else{
						return Lng.str("n/a (legacy power)");
					}
				}
				@Override
				public String getToolTipText() {
					return super.getToolTipText()+"\n"+Lng.str("WARNING: A integrity lower than %s is dangerous and can lead to system implosion on damage!",(int)VoidElementManager.INTEGRITY_MARGIN);
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
		return "ASPOWER";
	}

	@Override
	public String getTitle() {
		return Lng.str("Power");
	}




}
