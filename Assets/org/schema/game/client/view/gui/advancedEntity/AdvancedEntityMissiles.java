package org.schema.game.client.view.gui.advancedEntity;

import org.schema.common.util.StringTools;
import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.tools.ButtonCallback;
import org.schema.game.client.view.gui.advanced.tools.ButtonResult;
import org.schema.game.client.view.gui.advanced.tools.LabelResult;
import org.schema.game.client.view.gui.advanced.tools.StatLabelResult;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.ShipManagerContainer;
import org.schema.game.common.controller.elements.StationaryManagerContainer;
import org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager;
import org.schema.game.common.controller.elements.combination.MissileCombiSettings;
import org.schema.game.common.controller.elements.combination.modifier.MissileUnitModifier;
import org.schema.game.common.controller.elements.missile.dumb.DumbMissileCollectionManager;
import org.schema.game.common.controller.elements.missile.dumb.DumbMissileElementManager;
import org.schema.game.common.controller.elements.missile.dumb.DumbMissileUnit;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableDirtyInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonColor;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;

import static org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager.WeaponType.MISSILE;

public class AdvancedEntityMissiles extends AdvancedEntityWeaponGUIGroup<DumbMissileUnit, DumbMissileCollectionManager, DumbMissileElementManager, MissileUnitModifier, MissileCombiSettings>{

	
	

	public AdvancedEntityMissiles(AdvancedGUIElement e) {
		super(e);
	}

	
	
	
	
	
	@Override
	public String getId() {
		return "AEMISSILE";
	}

	@Override
	public String getTitle() {
		return Lng.str("Missiles");
	}

	
	public int addCapacityBlockIcons(GUIContentPane pane, int x, int y) {
		addStatLabel(pane.getContent(0), x, y++, new StatLabelResult() {
			
			@Override
			public String getName() {
				return Lng.str("Capacity:");
			}
			
			@Override
			public String getValue() {
				return ElementKeyMap.getInfo(ElementKeyMap.MISSILE_CAPACITY_MODULE).getName();
			}
			
			@Override
			public int getStatDistance() {
				return 100;
			}
		});
		
		
		
		addWeaponBlockIcon(pane, x, y++, new Object() {
			public String toString() {
				if(getMan() != null && getEm() != null) {
					return Lng.str("Capacity (%s)", ElementKeyMap.getInfo(ElementKeyMap.MISSILE_CAPACITY_MODULE).getName());
				}
				return "";
			}
		}, new InitInterface() {
		
			public short getType() {
				return ElementKeyMap.MISSILE_CAPACITY_MODULE;
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
						promptBuild(ElementKeyMap.MISSILE_CAPACITY_MODULE, 1, Lng.str("Add %s for more missile capacity",ElementKeyMap.getInfo(getComputerType()).getName()));
					}

					
				};
			}
			
			@Override
			public String getName() {
				return Lng.str("Add Missile Capacity");
			}
			
			@Override
			public HButtonColor getColor() {
				return HButtonColor.BLUE;
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
					return StringTools.formatPointZero(getSegCon().getAmmoCapacityMax(MISSILE));
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
				return Lng.str("Missile");
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
				return Lng.str("Speed:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				if(selectedCollectionManager != null) {
					return Lng.str("%s m/s", StringTools.formatPointZero(selectedCollectionManager.getWeaponSpeed()));
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
				return Lng.str("Damage:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				
				if(selectedElement != null && selectedCollectionManager != null) {
					MissileUnitModifier gui = getCombiValue();
					if(gui != null) {
						return StringTools.formatPointZero(gui.outputDamage);
					}else {
						return StringTools.formatPointZero(selectedElement.getDamage());
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
	}



	@Override
	public DumbMissileElementManager getEm() {
		if(getMan() instanceof StationaryManagerContainer) {
			return (DumbMissileElementManager) ((StationaryManagerContainer)getMan()).getMissile().getElementManager();
		}else {
			return (DumbMissileElementManager) ((ShipManagerContainer)getMan()).getMissile().getElementManager();
		}
	}




	@Override
	protected MissileCombiSettings getWeaponCombiSettingsRaw() {
		return selectedCollectionManager.getWeaponChargeParams();
	}

	@Override
	public String getSystemNameShort() {
		return Lng.str("Missile");
	}

	@Override
	public String getOutputNameShort() {
		return Lng.str("Tube");
	}

	





	
	
}
