package org.schema.game.client.view.gui.advancedstats;

import org.schema.common.util.StringTools;
import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.tools.LabelResult;
import org.schema.game.client.view.gui.advanced.tools.StatLabelResult;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableDirtyInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;

public class AdvancedStructureStatsStructure extends AdvancedStructureStatsGUISGroup{


	

	public AdvancedStructureStatsStructure(AdvancedGUIElement e) {
		super(e);
	}
	
	@Override
	public void build(GUIContentPane pane, GUIDockableDirtyInterface dInt) {
		pane.setTextBoxHeightLast(UIScale.getUIScale().scale(30));
		
		int y = 0;
		addLabel(pane.getContent(0), 0, y++, new LabelResult() {
			@Override
			public String getName() {
				return Lng.str("Structure");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.BIG_20;
			}
			
		});
		addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Reactor (HP):");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				assert(getMan() != null);
				return StringTools.formatSmallAndBig(getSegCon().getHpController().getHp());
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
			
			
		});
		addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
			@Override
			public String getName() {
				return Lng.str("Reactor (max HP):");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
				assert(getMan() != null);
				return StringTools.formatSmallAndBig(getSegCon().getHpController().getMaxHp());
			}
			@Override
			public int getStatDistance() {
				return getTextDist();
			}
			
			
		});
//		addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
//			@Override
//			public String getName() {
//				return Lng.str("Armor (HP):");
//			}
//			@Override
//			public FontInterface getFontSize() {
//				return FontSize.MEDIUM;
//			}
//			@Override
//			public String getValue() {
//				return StringTools.formatSmallAndBig(getSegCon().getHpController().getArmorHp());
//			}
//			@Override
//			public int getStatDistance() {
//				return getTextDist();
//			}
//		});		
//		addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
//			@Override
//			public String getName() {
//				return Lng.str("Armor (max HP):");
//			}
//			@Override
//			public FontInterface getFontSize() {
//				return FontSize.MEDIUM;
//			}
//			@Override
//			public String getValue() {
//				return StringTools.formatSmallAndBig(getSegCon().getHpController().getMaxArmorHp());
//			}
//			@Override
//			public int getStatDistance() {
//				return getTextDist();
//			}
//		});	
		addLabel(pane.getContent(0), 0, y++, new LabelResult() {
			@Override
			public String getName() {
				return Lng.str("System Sanity");
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.BIG_20;
			}
			
		});
		addStatLabel(pane.getContent(0), 0, y++, new StatLabelResult() {
			@Override
			public String getName() {
				String systemSanity = getSegCon().getHpController().getDebuffString();
				return systemSanity;
			}
			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}
			@Override
			public String getValue() {
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
					return Lng.str("Integrity Updates");
				}
				@Override
				public FontInterface getFontSize() {
					return FontSize.MEDIUM_15;
				}
				@Override
				public String getValue() {
					return getMan() != null ? Lng.str("n/a") : (getMan().getIntegrityUpdateDelay() > 0 ? Lng.str("in stasis (%s sec)", String.valueOf(Math.ceil(getMan().getIntegrityUpdateDelay()))) : Lng.str("no delay"));
				}
				@Override
				public int getStatDistance() {
					return getTextDist();
				}
				@Override
				public String getToolTipText() {
					return Lng.str("When a system block gets taken out, the integrity of all systems will be temporarily protected and stay at the value they had before damage\nto prevent rapid integrity decline in a battle, when the inital system was built with good integrity.\nProtection cancels when a block gets added.");
				}
				
			});
		}
		
		
	}
	private int getTextDist() {
		return 150;
	}
	
	@Override
	public String getId() {
		return "ASSTRUCTURE";
	}

	@Override
	public String getTitle() {
		return Lng.str("Structure");
	}




}
