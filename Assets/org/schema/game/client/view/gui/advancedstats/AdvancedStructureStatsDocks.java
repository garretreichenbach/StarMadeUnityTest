package org.schema.game.client.view.gui.advancedstats;

import org.schema.common.util.StringTools;
import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.tools.ButtonCallback;
import org.schema.game.client.view.gui.advanced.tools.ButtonResult;
import org.schema.game.client.view.gui.advanced.tools.LabelResult;
import org.schema.game.client.view.gui.advanced.tools.StatLabelResult;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableDirtyInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonColor;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.sound.controller.AudioController;

public class AdvancedStructureStatsDocks extends AdvancedStructureStatsGUISGroup {

	public AdvancedStructureStatsDocks(AdvancedGUIElement e) {
		super(e);
	}

	@Override
	public void build(GUIContentPane pane, GUIDockableDirtyInterface dInt) {
		pane.setTextBoxHeightLast(UIScale.getUIScale().scale(30));
		int yIndex = 0;
		addStatLabel(pane.getContent(0), 0, yIndex++, new StatLabelResult() {

			@Override
			public String getName() {
				return Lng.str("Normal:");
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}

			@Override
			public String getValue() {
				assert (getMan() != null);
				return StringTools.formatSmallAndBig(getSegCon().railController.getNormalDockCount());
			}

			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		addStatLabel(pane.getContent(0), 0, yIndex++, new StatLabelResult() {

			@Override
			public String getName() {
				return Lng.str("Turrets:");
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}

			@Override
			public String getValue() {
				assert (getMan() != null);
				return StringTools.formatSmallAndBig(getSegCon().railController.getTurretCount());
			}

			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		addStatLabel(pane.getContent(0), 0, yIndex++, new StatLabelResult() {

			@Override
			public String getName() {
				return Lng.str("Docked:");
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}

			@Override
			public String getValue() {
				assert (getMan() != null);
				return StringTools.formatSmallAndBig(getSegCon().railController.next.size());
			}

			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		addStatLabel(pane.getContent(0), 0, yIndex++, new StatLabelResult() {

			@Override
			public String getName() {
				return Lng.str("Docked (Total):");
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}

			@Override
			public String getValue() {
				assert (getMan() != null);
				return StringTools.formatSmallAndBig(getSegCon().railController.getTotalDockedFromHere());
			}

			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		addLabel(pane.getContent(0), 0, yIndex++, new LabelResult() {

			@Override
			public String getName() {
				return Lng.str("UNDOCK");
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.BIG_20;
			}

			@Override
			public HorizontalAlignment getHorizontalAlignment() {
				return HorizontalAlignment.MID;
			}
		});
		addButton(pane.getContent(0), 0, yIndex, new ButtonResult() {

			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {

					@Override
					public void pressedRightMouse() {
					}

					@Override
					public void pressedLeftMouse() {
						PlayerGameOkCancelInput check = new PlayerGameOkCancelInput("CONFIRM", getState(), Lng.str("Confirm"), Lng.str("Do you really want to do this?")) {

							@Override
							public void onDeactivate() {
							}

							@Override
							public void pressedOK() {
								deactivate();
								getSegCon().railController.undockAllClient();
							}
						};
						check.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(311);
					}
				};
			}

			@Override
			public String getName() {
				return Lng.str("All");
			}

			@Override
			public HButtonColor getColor() {
				return HButtonColor.ORANGE;
			}
		});
		addButton(pane.getContent(0), 1, yIndex, new ButtonResult() {

			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {

					@Override
					public void pressedRightMouse() {
					}

					@Override
					public void pressedLeftMouse() {
						PlayerGameOkCancelInput check = new PlayerGameOkCancelInput("CONFIRM", getState(), Lng.str("Confirm"), Lng.str("Do you really want to do this?")) {

							@Override
							public void onDeactivate() {
							}

							@Override
							public void pressedOK() {
								deactivate();
								getSegCon().railController.undockAllClientTurret();
							}
						};
						check.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(312);
					}
				};
			}

			@Override
			public String getName() {
				return Lng.str("Turrets");
			}

			@Override
			public HButtonColor getColor() {
				return HButtonColor.ORANGE;
			}
		});
		addButton(pane.getContent(0), 2, yIndex++, new ButtonResult() {

			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {

					@Override
					public void pressedRightMouse() {
					}

					@Override
					public void pressedLeftMouse() {
						PlayerGameOkCancelInput check = new PlayerGameOkCancelInput("CONFIRM", getState(), Lng.str("Confirm"), Lng.str("Do you really want to do this?")) {

							@Override
							public void onDeactivate() {
							}

							@Override
							public void pressedOK() {
								deactivate();
								getSegCon().railController.undockAllClientNormal();
							}
						};
						check.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(313);
					}
				};
			}

			@Override
			public String getName() {
				return Lng.str("Normal");
			}

			@Override
			public HButtonColor getColor() {
				return HButtonColor.ORANGE;
			}
		});
		addLabel(pane.getContent(0), 0, yIndex++, new LabelResult() {

			@Override
			public String getName() {
				return Lng.str("AI");
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.BIG_20;
			}

			@Override
			public HorizontalAlignment getHorizontalAlignment() {
				return HorizontalAlignment.MID;
			}
		});
		addButton(pane.getContent(0), 0, yIndex, new ButtonResult() {

			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {

					@Override
					public void pressedRightMouse() {
					}

					@Override
					public void pressedLeftMouse() {
						getSegCon().railController.getRoot().railController.activateAllAIClient(true, true, false);
					}
				};
			}

			@Override
			public String getName() {
				return Lng.str("Turrets On");
			}

			@Override
			public HButtonColor getColor() {
				return HButtonColor.GREEN;
			}
		});
		addButton(pane.getContent(0), 1, yIndex++, new ButtonResult() {

			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {

					@Override
					public void pressedRightMouse() {
					}

					@Override
					public void pressedLeftMouse() {
						getSegCon().railController.getRoot().railController.activateAllAIClient(false, true, false);
					}
				};
			}

			@Override
			public String getName() {
				return Lng.str("Turrets Off");
			}

			@Override
			public HButtonColor getColor() {
				return HButtonColor.RED;
			}
		});
		addButton(pane.getContent(0), 0, yIndex, new ButtonResult() {

			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {

					@Override
					public void pressedRightMouse() {
					}

					@Override
					public void pressedLeftMouse() {
						getSegCon().railController.getRoot().railController.activateAllAIClient(true, false, true);
					}
				};
			}

			@Override
			public String getName() {
				return Lng.str("Act. all");
			}

			@Override
			public HButtonColor getColor() {
				return HButtonColor.GREEN;
			}
		});
		addButton(pane.getContent(0), 1, yIndex++, new ButtonResult() {

			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {

					@Override
					public void pressedRightMouse() {
					}

					@Override
					public void pressedLeftMouse() {
						getSegCon().railController.getRoot().railController.activateAllAIClient(false, false, true);
					}
				};
			}

			@Override
			public String getName() {
				return Lng.str("Deact. all");
			}

			@Override
			public HButtonColor getColor() {
				return HButtonColor.RED;
			}
		});
	}

	private int getTextDist() {
		return 150;
	}

	@Override
	public String getId() {
		return "ASDOCKS";
	}

	@Override
	public String getTitle() {
		return Lng.str("Docks");
	}
}
