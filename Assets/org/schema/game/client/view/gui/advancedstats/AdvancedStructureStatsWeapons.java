package org.schema.game.client.view.gui.advancedstats;

import org.schema.common.util.StringTools;
import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.tools.LabelResult;
import org.schema.game.client.view.gui.advanced.tools.StatLabelResult;
import org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableDirtyInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;

import static org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager.WeaponType.MISSILE;

public class AdvancedStructureStatsWeapons extends AdvancedStructureStatsGUISGroup{


	

	public AdvancedStructureStatsWeapons(AdvancedGUIElement e) {
		super(e);
	}
	
	@Override
	public void build(GUIContentPane pane, GUIDockableDirtyInterface dInt) {
		pane.setTextBoxHeightLast(UIScale.getUIScale().scale(30));
		
		int y = 0;
		addLabel(pane.getContent(0), 0, y++, new LabelResult() {
			@Override
			public String getName() {
				return Lng.str("Missiles");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.BIG_20;
			}
			
		});
		addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Loaded:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				assert(getMan() != null);
				return StringTools.formatPointZeroZero(getMan().getAmmoCapacity(MISSILE));
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Capacity:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				assert(getMan() != null);
				return StringTools.formatPointZeroZero(getMan().getAmmoCapacityMax(MISSILE));
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Reload:");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				assert(getMan() != null);
				return Lng.str("%s / %s sec", 
						StringTools.formatPointZero(getMan().getAmmoCapacityReloadTime(MISSILE)-getMan().getAmmoCapacityTimer(MISSILE)),
						StringTools.formatPointZero(getMan().getAmmoCapacityReloadTime(MISSILE)));
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
		return "ASWEAPONS";
	}

	@Override
	public String getTitle() {
		return Lng.str("Weapons");
	}




}
