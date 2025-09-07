package org.schema.game.client.view.gui.advancedEntity;

import org.schema.common.util.StringTools;
import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.tools.ButtonCallback;
import org.schema.game.client.view.gui.advanced.tools.ButtonResult;
import org.schema.game.client.view.gui.advanced.tools.LabelResult;
import org.schema.game.client.view.gui.advanced.tools.StatLabelResult;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.WeaponManagerInterface;
import org.schema.game.common.controller.elements.beam.damageBeam.DamageBeamCollectionManager;
import org.schema.game.common.controller.elements.beam.damageBeam.DamageBeamElementManager;
import org.schema.game.common.controller.elements.beam.damageBeam.DamageBeamUnit;
import org.schema.game.common.controller.elements.combination.BeamCombiSettings;
import org.schema.game.common.controller.elements.combination.modifier.BeamUnitModifier;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableDirtyInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;

import static org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager.WeaponType.BEAM;
import static org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager.WeaponType.MISSILE;

public class AdvancedEntityBeams extends AdvancedEntityWeaponGUIGroup<DamageBeamUnit, DamageBeamCollectionManager, DamageBeamElementManager, BeamUnitModifier, BeamCombiSettings>{

	
	

	public AdvancedEntityBeams(AdvancedGUIElement e) {
		super(e);
	}

	
	
	
	
	
	@Override
	public String getId() {
		return "AEBEAM";
	}

	@Override
	public String getTitle() {
		return Lng.str("Beams");
	}

	public int addCapacityBlockIcons(GUIContentPane pane, int x, int y) {
		addStatLabel(pane.getContent(0), x, y++, new StatLabelResult() {

			@Override
			public String getName() {
				return Lng.str("Capacity:");
			}

			@Override
			public String getValue() {
				return ElementKeyMap.getInfo(ElementKeyMap.BEAM_CAPACITY_MODULE).getName();
			}

			@Override
			public int getStatDistance() {
				return 100;
			}
		});



		addWeaponBlockIcon(pane, x, y++, new Object() {
			public String toString() {
				if(getMan() != null && getEm() != null) {
					return Lng.str("Capacity (%s)", ElementKeyMap.getInfo(ElementKeyMap.BEAM_CAPACITY_MODULE).getName());
				}
				return "";
			}
		}, new InitInterface() {

			public short getType() {
				return ElementKeyMap.BEAM_CAPACITY_MODULE;
			}
			@Override
			public boolean isInit() {
				return getMan() != null && getEm() != null;
			}
		});


		return y;
	}

	@Override
	public void build(GUIContentPane pane, GUIDockableDirtyInterface dInt) {
		pane.setTextBoxHeightLast(UIScale.getUIScale().scale(30));
		int indexY = 0;
		
		
		
		
		
		indexY = addWeaponBlockIcons(pane, 0, indexY);
		
		addBuildButton(pane, dInt, 0, indexY);
		addWeaponPanel(pane, 1, indexY++);

		addSelectButton(pane, 0, indexY);
		addAddButton(pane, 1, indexY++);

		indexY = addCapacityBlockIcons(pane, 0, indexY);
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
						promptBuild(ElementKeyMap.BEAM_CAPACITY_MODULE, 1, Lng.str("Add %s for more beam firing capacity",ElementKeyMap.getInfo(getComputerType()).getName()));
					}


				};
			}

			@Override
			public String getName() {
				return Lng.str("Add Beam Firing Capacity");
			}

			@Override
			public GUIHorizontalArea.HButtonColor getColor() {
				return GUIHorizontalArea.HButtonColor.BLUE;
			}

			@Override
			public boolean isActive() {
				return super.isActive() && !isCommandQueued();
			}
		});
		addStatLabel(pane.getContent(0), 0, indexY++, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Max Capacity:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {

				if(getMan() != null) {
					return StringTools.formatPointZero(getSegCon().getAmmoCapacityMax(BEAM));
				}else {
					return Lng.str("n/a");
				}
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		addLabel(pane.getContent(0), 0, indexY++, new LabelResult() {
			@Override
			public String getName() {
				return Lng.str("Beam");
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.BIG_20;
			}
		});

		addDropdown(pane.getContent(0), 0, indexY++, new WeaponSelectDropdownResult());
		
		addStatLabel(pane.getContent(0), 0, indexY++, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Position:");
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}

			@Override
			public String getValue() {

				if (selectedElement != null && selectedCollectionManager != null) {
					return "[" + ElementCollection.getPosString(selectedCollectionManager.getControllerIndex()) + "]";
				} else {
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
				return Lng.str("Blocks:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				
				if(selectedCollectionManager != null) {
					return StringTools.formatSeperated(selectedCollectionManager.getTotalSize());
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
				return Lng.str("Groups:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				
				if(selectedCollectionManager != null) {
					return String.valueOf(selectedCollectionManager.getElementCollections().size());
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
				return Lng.str("Combination:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				ControlBlockElementCollectionManager<?, ?, ?> s;
				if(selectedCollectionManager != null && (s = selectedCollectionManager.getSupportCollectionManager()) != null) {
					
					return String.valueOf(s.getModuleName());
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
				return Lng.str("Range:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				if(selectedCollectionManager != null) {
					return StringTools.formatDistance(selectedCollectionManager.getWeaponDistance());
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
				return Lng.str("Zoom:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				if(selectedCollectionManager != null && getWeaponCombiSettings().possibleZoom > 0) {
					return StringTools.formatPointZero(1 / getWeaponCombiSettings().possibleZoom * 100f) + "%";
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
				return Lng.str("Reload");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				if(selectedCollectionManager != null && selectedCollectionManager.getElementCollections().size() > 0) {
					return Lng.str("%s sec", StringTools.formatPointZeroZero(getEm().calculateReload(selectedCollectionManager.getElementCollections().get(0)) / 1000f));
				}else {
					return Lng.str("n/a");
				}
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});

		addLabel(pane.getContent(0), 0, indexY++, new LabelResult() {
			@Override
			public String getName() {
				return Lng.str("Group");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.BIG_20;
			}
		});		
		
		addDropdown(pane.getContent(0), 0, indexY++, new ModuleSelectDropdown());
		
		addStatLabel(pane.getContent(0), 0, indexY++, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Position:");
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}

			@Override
			public String getValue() {

				if (selectedElement != null && selectedCollectionManager != null) {
					return "[" + ElementCollection.getPosString(selectedElement.idPos) + "]";
				} else {
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
				return Lng.str("Blocks:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				if(selectedElement != null) {
					return StringTools.formatSeperated(selectedElement.size());
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
				return Lng.str("Dmg/sec:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				
				if(selectedElement != null && selectedCollectionManager != null) {
					BeamUnitModifier gui = getCombiValue();
					if(gui != null) {
						return StringTools.formatPointZero(gui.outputDamagePerHit * gui.outputTickRate);
					}else {
						return StringTools.formatPointZero(selectedElement.getBeamPower() * selectedElement.getTickRate());
					}
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
				return Lng.str("Burst Time:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				
				if(selectedElement != null && selectedCollectionManager != null) {
					BeamUnitModifier gui = getCombiValue();
					if(gui != null) {
						return StringTools.formatPointZero(gui.outputBurstTime);
					}else {
						return StringTools.formatPointZero(selectedElement.getBurstTime());
					}
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
				return Lng.str("Burst Damage:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				
				if(selectedElement != null && selectedCollectionManager != null) {
					BeamUnitModifier gui = getCombiValue();
					if(gui != null) {
						return gui.outputBurstTime <= 0 ? Lng.str("const.") : StringTools.formatPointZero(gui.outputDamagePerHit * ((gui.outputTickRate*gui.outputBurstTime) + gui.outputInitialTicks));
					}else {
						return selectedElement.getBurstTime() <= 0 ? Lng.str("const.") : StringTools.formatPointZero(selectedElement.getBeamPower() * ((selectedElement.getTickRate()*selectedElement.getBurstTime())+selectedElement.getInitialTicks()));
					}
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
				return Lng.str("Dmg/Tick:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				
				if(selectedElement != null && selectedCollectionManager != null) {
					BeamUnitModifier gui = getCombiValue();
					if(gui != null) {
						return StringTools.formatPointZero(gui.outputDamagePerHit);
					}else {
						return StringTools.formatPointZero(selectedElement.getBeamPower());
					}
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
				return Lng.str("Tickrate:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				
				if(selectedElement != null && selectedCollectionManager != null) {
					BeamUnitModifier gui = getCombiValue();
					if(gui != null) {
						return Lng.str("%s / sec",StringTools.formatPointZero(gui.outputTickRate));
					}else {
						return Lng.str("%s / sec",StringTools.formatPointZero(selectedElement.getTickRate()));
					}
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
				return Lng.str("Initial Ticks:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				
				if(selectedElement != null && selectedCollectionManager != null) {
						BeamUnitModifier gui = getCombiValue();
						if(gui != null) {
							return Lng.str("%s",StringTools.formatPointZero(gui.outputInitialTicks));
						}else {
							return Lng.str("%s",StringTools.formatPointZero(selectedElement.getInitialTicks()));
						}
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
				return Lng.str("Falloff start:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				
				if(selectedElement != null && selectedCollectionManager != null) {
					BeamUnitModifier gui = getCombiValue();
					if(gui != null) {
						return Lng.str("%s%% at %s", StringTools.formatPointZero(gui.outputMinEffectiveValue*100f), StringTools.formatDistance(gui.outputMinEffectiveRange*gui.outputDistance));
					}else {
						return Lng.str("%s%% at %s", StringTools.formatPointZero(selectedElement.getMinEffectiveValue()*100f), StringTools.formatDistance(selectedElement.getMinEffectiveRange()*selectedElement.getDistance()));
					}
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
				return Lng.str("Falloff end:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				
				if(selectedElement != null && selectedCollectionManager != null) {
					BeamUnitModifier gui = getCombiValue();
					if(gui != null) {
						return Lng.str("%s%% at %s", StringTools.formatPointZero(gui.outputMaxEffectiveValue*100f), StringTools.formatDistance(gui.outputMaxEffectiveRange*gui.outputDistance));
					}else {
						return Lng.str("%s%% at %s", StringTools.formatPointZero(selectedElement.getMaxEffectiveValue()*100f), StringTools.formatDistance(selectedElement.getMaxEffectiveRange()*selectedElement.getDistance()));
					}
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
				return Lng.str("Pw. Cons. Idle:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				if(selectedElement != null) {
					return StringTools.formatPointZero(selectedElement.getPowerConsumedPerSecondResting());
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
				return Lng.str("Pw. Cons. Reload:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				if(selectedElement != null) {
					return StringTools.formatPointZero(selectedElement.getPowerConsumedPerSecondCharging());
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
				return Lng.str("Blast Damage:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				if(selectedElement != null) {
					return StringTools.formatPointZero(selectedElement.getAcidDamagePercentage()*100f)+"%";
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



	@Override
	public DamageBeamElementManager getEm() {
		if(getMan() instanceof WeaponManagerInterface) {
			return (DamageBeamElementManager) ((WeaponManagerInterface)getMan()).getBeam().getElementManager();
		}
		return null;
	}




	@Override
	protected BeamCombiSettings getWeaponCombiSettingsRaw() {
		return selectedCollectionManager.getWeaponChargeParams();
	}

	@Override
	public String getSystemNameShort() {
		return Lng.str("Beam");
	}

	@Override
	public String getOutputNameShort() {
		return Lng.str("Output");
	}




	





	
	
}
