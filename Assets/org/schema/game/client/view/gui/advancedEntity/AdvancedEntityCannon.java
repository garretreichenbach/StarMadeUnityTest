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
import org.schema.game.common.controller.elements.combination.CannonCombiSettings;
import org.schema.game.common.controller.elements.combination.modifier.CannonUnitModifier;
import org.schema.game.common.controller.elements.cannon.CannonCollectionManager;
import org.schema.game.common.controller.elements.cannon.CannonElementManager;
import org.schema.game.common.controller.elements.cannon.CannonUnit;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableDirtyInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;

import static org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager.WeaponType.CANNON;

public class AdvancedEntityCannon extends AdvancedEntityWeaponGUIGroup<CannonUnit, CannonCollectionManager, CannonElementManager, CannonUnitModifier, CannonCombiSettings>{

	
	

	public AdvancedEntityCannon(AdvancedGUIElement e) {
		super(e);
	}

	
	
	
	
	
	@Override
	public String getId() {
		return "AECANNON";
	}

	@Override
	public String getTitle() {
		return Lng.str("Cannons");
	}

	public int addCapacityBlockIcons(GUIContentPane pane, int x, int y) {
		addStatLabel(pane.getContent(0), x, y++, new StatLabelResult() {

			@Override
			public String getName() {
				return Lng.str("Capacity:");
			}

			@Override
			public String getValue() {
				return ElementKeyMap.getInfo(ElementKeyMap.CANNON_CAPACITY_MODULE).getName();
			}

			@Override
			public int getStatDistance() {
				return 100;
			}
		});



		addWeaponBlockIcon(pane, x, y++, new Object() {
			public String toString() {
				if(getMan() != null && getEm() != null) {
					return Lng.str("Capacity (%s)", ElementKeyMap.getInfo(ElementKeyMap.CANNON_CAPACITY_MODULE).getName());
				}
				return "";
			}
		}, new InitInterface() {

			public short getType() {
				return ElementKeyMap.CANNON_CAPACITY_MODULE;
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
						promptBuild(ElementKeyMap.CANNON_CAPACITY_MODULE, 1, Lng.str("Add %s for more ammo capacity",ElementKeyMap.getInfo(getComputerType()).getName()));
					}


				};
			}

			@Override
			public String getName() {
				return Lng.str("Add Cannon Ammo Capacity");
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
					return StringTools.formatPointZero(getSegCon().getAmmoCapacityMax(CANNON));
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
				return Lng.str("Cannon");
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
		addStatLabel(pane.getContent(0), 0, indexY++, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Blast Type");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				if(selectedCollectionManager != null && selectedCollectionManager.getElementCollections().size() > 0) {
					return getWeaponCombiSettings().acidType.toString();
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
				
				if(selectedElement != null && selectedCollectionManager != null) {
					return "[" + ElementCollection.getPosString(selectedElement.idPos) + "]";
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
						CannonUnitModifier gui = getCombiValue();
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
				return Lng.str("Phys. Recoil:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				
				if(selectedElement != null && selectedCollectionManager != null) {
					CannonUnitModifier gui = getCombiValue();
					if(gui != null) {
						return StringTools.formatPointZero(gui.outputRecoil);
					}else {
						return StringTools.formatPointZero(selectedElement.getRecoil());
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
				return Lng.str("AImpact Force:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				
				if(selectedElement != null && selectedCollectionManager != null) {
					CannonUnitModifier gui = getCombiValue();
					if(gui != null) {
						return StringTools.formatPointZero(gui.outputImpactForce);
					}else {
						return StringTools.formatPointZero(selectedElement.getImpactForce());
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
				return Lng.str("Proj. Width:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				if(selectedElement != null) {
					return StringTools.formatPointZero(selectedElement.getProjectileWidth());
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
	public CannonElementManager getEm() {
		if(getMan() instanceof StationaryManagerContainer) {
			return (CannonElementManager) ((StationaryManagerContainer)getMan()).getWeapon().getElementManager();
		}else {
			return (CannonElementManager) ((ShipManagerContainer)getMan()).getWeapon().getElementManager();
		}
	}




	@Override
	protected CannonCombiSettings getWeaponCombiSettingsRaw() {
		return selectedCollectionManager.getWeaponChargeParams();
	}

	@Override
	public String getSystemNameShort() {
		return Lng.str("Cannon");
	}

	@Override
	public String getOutputNameShort() {
		return Lng.str("Barrel");
	}
	





	
	
}
