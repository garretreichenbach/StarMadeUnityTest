package org.schema.game.client.view.gui.advancedstats;

import api.listener.events.StructureStatsCreateEvent;
import api.mod.StarLoader;
import org.schema.common.util.StringTools;
import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.tools.ButtonCallback;
import org.schema.game.client.view.gui.advanced.tools.ButtonResult;
import org.schema.game.client.view.gui.advanced.tools.LabelResult;
import org.schema.game.client.view.gui.advanced.tools.StatLabelResult;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.network.commands.gamerequests.EntityRequest;
import org.schema.schine.common.InputChecker;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableDirtyInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonColor;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.sound.controller.AudioController;

public class AdvancedStructureStatsGeneral extends AdvancedStructureStatsGUISGroup {

	public AdvancedStructureStatsGeneral(AdvancedGUIElement e) {
		super(e);
	}

	@Override
	public void build(GUIContentPane pane, GUIDockableDirtyInterface dInt) {
		pane.setTextBoxHeightLast(UIScale.getUIScale().scale(30));
		int indexY = 0;
		addLabel(pane.getContent(0), 0, indexY++, new LabelResult() {

			@Override
			public String getName() {
				return Lng.str("Entity");
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.BIG_20;
			}
		});
		addStatLabel(pane.getContent(0), 0, indexY++, new StatLabelResult() {

			@Override
			public String getName() {
				return Lng.str("Name:");
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}

			@Override
			public String getValue() {
				return getSegCon().getName();
			}

			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		addStatLabel(pane.getContent(0), 0, indexY++, new StatLabelResult() {

			@Override
			public String getName() {
				return Lng.str("Faction:");
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}

			@Override
			public String getValue() {
				return FactionManager.getFactionName(getSegCon());
			}

			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});

		addStatLabel(pane.getContent(0), 0, indexY++, new StatLabelResult() {

			@Override
			public String getName() {
				return Lng.str("UID:");
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}

			@Override
			public String getValue() {
				return getSegCon().getUniqueIdentifierFull();
			}

			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		addLabel(pane.getContent(0), 0, indexY++, new LabelResult() {

			@Override
			public String getName() {
				return Lng.str("Mass");
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.BIG_20;
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
				assert (getMan() != null);
				return StringTools.formatSmallAndBig(getSegCon().getTotalElements());
			}

			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		addStatLabel(pane.getContent(0), 0, indexY++, new StatLabelResult() {

			@Override
			public String getName() {
				return Lng.str("Mass:");
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}

			@Override
			public String getValue() {
				return StringTools.formatSmallAndBig(getSegCon().getTotalPhysicalMass());
			}

			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		addStatLabel(pane.getContent(0), 0, indexY++, new StatLabelResult() {

			@Override
			public String getName() {
				return Lng.str("Mass (Cargo):");
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}

			@Override
			public String getValue() {
				double cargo = getMan().getMassFromInventories();
				return StringTools.massFormat(cargo);
			}

			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		addStatLabel(pane.getContent(0), 0, indexY++, new StatLabelResult() {

			@Override
			public String getName() {
				return Lng.str("Mass (Total):");
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}

			@Override
			public String getValue() {
				float crm = getSegCon().railController.calculateRailMassIncludingSelf();
				return StringTools.formatSmallAndBig(crm);
			}

			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		addLabel(pane.getContent(0), 0, indexY++, new LabelResult() {

			@Override
			public String getName() {
				return Lng.str("Dimension");
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.BIG_20;
			}
		});
		addStatLabel(pane.getContent(0), 0, indexY++, new StatLabelResult() {

			@Override
			public String getName() {
				return Lng.str("Width(X):");
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}

			@Override
			public String getValue() {
				return Lng.str("%s m", StringTools.formatSmallAndBig(getSegCon().getBoundingBox().max.x - getSegCon().getBoundingBox().min.x - 2));
			}

			@Override
			public int getStatDistance() {
				return getTextDist();
			}

		});
		addStatLabel(pane.getContent(0), 0, indexY++, new StatLabelResult() {

			@Override
			public String getName() {
				return Lng.str("Height(Y):");
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}

			@Override
			public String getValue() {
				return Lng.str("%s m", StringTools.formatSmallAndBig(getSegCon().getBoundingBox().max.y - getSegCon().getBoundingBox().min.y - 2));
			}

			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		addStatLabel(pane.getContent(0), 0, indexY++, new StatLabelResult() {

			@Override
			public String getName() {
				return Lng.str("Length(Z):");
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}

			@Override
			public String getValue() {
				return Lng.str("%s m", StringTools.formatSmallAndBig(getSegCon().getBoundingBox().max.z - getSegCon().getBoundingBox().min.z - 2));
			}

			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		addButton(pane.getContent(0), 0, indexY, new ButtonResult() {

			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {

					@Override
					public void pressedRightMouse() {
					}

					@Override
					public void pressedLeftMouse() {
						final SegmentController c = getSegCon();
						if (getState().getPlayer().getFactionId() != 0 && getState().getPlayer().getFactionId() == c.getFactionId() && !c.isSufficientFactionRights(getState().getPlayer())) {
							getState().getController().popupAlertTextMessage(Lng.str("Permission denied!\n(faction rank)"), 0);
						} else {
							PlayerGameTextInput p = new PlayerGameTextInput("FactionBlockDialog_CHANGE_NAME", getState(), 50, Lng.str("Change Name"), Lng.str("Change the name of the object"), c.getRealName()) {

								@Override
								public String[] getCommandPrefixes() {
									return null;
								}

								@Override
								public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
									return null;
								}

								@Override
								public void onFailedTextCheck(String msg) {
								}

								@Override
								public void onDeactivate() {
									getPlayerInteractionControlManager().hinderInteraction(400);
									getPlayerInteractionControlManager().suspend(false);
								}

								@Override
								public boolean onInput(String entry) {
									if (!c.getRealName().equals(entry.trim())) {
										System.err.println("[CLIENT] sending name for object: " + c + ": " + entry.trim());
										c.getNetworkObject().realName.set(entry.trim(), true);
										assert (c.getNetworkObject().realName.hasChanged());
										assert (c.getNetworkObject().isChanged());
									}
									return true;
								}
							};
							p.setInputChecker((entry, callback) -> {
								if (EntityRequest.isShipNameValid(entry)) {
									return true;
								} else {
									callback.onFailedTextCheck(Lng.str("Must only contain letters or numbers or ( _-)!"));
									return false;
								}
							});
							p.activate();
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
							AudioController.fireAudioEventID(317);
						}
					}
				};
			}

			@Override
			public boolean isActive() {
				return super.isActive() && getState() != null && getState().getPlayer() != null && getMan() != null && !(getState().getPlayer().getFactionId() != 0 && getSegCon() != null && getState().getPlayer().getFactionId() == getSegCon().getFactionId() && !getSegCon().isSufficientFactionRights(getState().getPlayer()));
			}

			@Override
			public String getName() {
				return Lng.str("Rename %s", getSegCon().getType().getName());
			}
			@Override
			public String getToolTipText() {
				return isActive() ? null : Lng.str("Insufficient faction permission to rename");
			}

			@Override
			public HButtonColor getColor() {
				return HButtonColor.BLUE;
			}
		});
		//INSERTED CODE @360
		StarLoader.fireEvent(StructureStatsCreateEvent.class, new StructureStatsCreateEvent(this, pane), false);
		///
	}

	private int getTextDist() {
		return 150;
	}

	@Override
	public String getId() {
		return "ASGENERAL";
	}

	@Override
	public String getTitle() {
		return Lng.str("General");
	}
}
