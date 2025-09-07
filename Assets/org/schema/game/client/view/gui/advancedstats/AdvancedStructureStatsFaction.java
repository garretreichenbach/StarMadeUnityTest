package org.schema.game.client.view.gui.advancedstats;

import java.util.Locale;

import javax.vecmath.Vector4f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.tools.ButtonCallback;
import org.schema.game.client.view.gui.advanced.tools.ButtonResult;
import org.schema.game.client.view.gui.advanced.tools.LabelResult;
import org.schema.game.client.view.gui.advanced.tools.StatLabelResult;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.StationaryManagerContainer;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.SimplePlayerCommands;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableDirtyInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonColor;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.sound.controller.AudioController;

public class AdvancedStructureStatsFaction extends AdvancedStructureStatsGUISGroup {

	private Vector3i tmp = new Vector3i();

	private Vector4f selected = new Vector4f(0.5f, 0.9f, 0.4f, 1.0f);

	private Vector4f uselected = new Vector4f(0.8f, 0.2f, 0.4f, 1.0f);

	private Vector4f blue = new Vector4f(0.2f, 0.4f, 0.8f, 1.0f);

	private Vector4f mouse = new Vector4f(0.5f, 0.4f, 0.2f, 1.0f);

	public AdvancedStructureStatsFaction(AdvancedGUIElement e) {
		super(e);
	}

	@Override
	public void build(GUIContentPane pane, GUIDockableDirtyInterface dInt) {
		pane.setTextBoxHeightLast(UIScale.getUIScale().scale(30));
		int indexY = 0;
		addLabel(pane.getContent(0), 0, indexY++, new LabelResult() {

			@Override
			public String getName() {
				if (hasFactionBlock()) {
					return Lng.str("Faction Block at %s", ElementCollection.getPosFromIndex(getMan().getFactionBlockPos(), tmp).toStringPure());
				} else {
					return Lng.str("No Faction Block");
				}
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
				return Lng.str("Owner:");
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}

			@Override
			public String getValue() {
				if (getSegCon().currentOwnerLowerCase.length() > 0) {
					return getSegCon().currentOwnerLowerCase;
				} else {
					return Lng.str("<No Owner>");
				}
			}

			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		addButton(pane.getContent(0), 0, indexY++, new ButtonResult() {

			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {

					@Override
					public void pressedRightMouse() {
					}

					@Override
					public void pressedLeftMouse() {
						if (canTakeOwnerShip()) {
							getState().getPlayer().getFactionController().sendEntityOwnerChangeRequest(getSegCon().currentOwnerLowerCase.length() > 0 ? "" : getState().getPlayer().getName().toLowerCase(Locale.ENGLISH), getSegCon());
							return;
						}
					}
				};
			}

			@Override
			public boolean isActive() {
				return super.isActive() && getSegCon() != null && canTakeOwnerShip();
			}

			@Override
			public String getName() {
				if (getSegCon().currentOwnerLowerCase.length() > 0) {
					return Lng.str("Reset Owner");
				} else {
					return Lng.str("Set Owner");
				}
			}

			@Override
			public HButtonColor getColor() {
				if (getSegCon() == null) {
					return HButtonColor.RED;
				} else {
					return HButtonColor.BLUE;
				}
			}
		});
		addButton(pane.getContent(0), 0, indexY++, new ButtonResult() {

			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {

					@Override
					public void pressedRightMouse() {
					}

					@Override
					public void pressedLeftMouse() {
						if (getSegCon().isHomeBaseFor(getState().getPlayer().getFactionId())) {
							getState().getController().popupAlertTextMessage(Lng.str("This is your homebase.\nYou have to revoke the homebase status first\nto reset the faction on this entity!"), 0);
							return;
						}
						if (getSegCon().getFactionId() != 0) {
							getState().getPlayer().getFactionController().sendEntityFactionIdChangeRequest(0, getSegCon());
						} else {
							if (getState().getPlayer().getFactionId() != 0) {
								getState().getPlayer().getFactionController().sendEntityFactionIdChangeRequest(getState().getPlayer().getFactionId(), getSegCon());
							}
						}
					}
				};
			}

			@Override
			public boolean isActive() {
				return super.isActive() && hasFactionBlock() && hasRights() && (getSegCon().getFactionId() != 0 || getState().getPlayer().getFactionId() != 0);
			}

			@Override
			public String getName() {
				if (!hasFactionBlock()) {
					return Lng.str("No Faction Block");
				}
				if (getSegCon().getFactionId() != 0) {
					return Lng.str("Reset Faction");
				} else {
					return Lng.str("Set Faction");
				}
			}

			@Override
			public HButtonColor getColor() {
				if (getSegCon() == null || getSegCon().getFactionId() != 0) {
					return HButtonColor.RED;
				} else {
					return HButtonColor.GREEN;
				}
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
						String e = Lng.str("you already have a homebase");
						boolean h = getState().getPlayer().getFactionController().hasHomebase();
						PlayerGameOkCancelInput check = new PlayerGameOkCancelInput("CONFIRM", getState(), Lng.str("WARN"), Lng.str("Do you really want to do this?") + "\n" + (h ? e : "")) {

							@Override
							public boolean isOccluded() {
								return false;
							}

							@Override
							public void onDeactivate() {
							}

							@Override
							public void pressedOK() {
								deactivate();
								getState().getFactionManager().sendClientHomeBaseChange(getState().getPlayer().getName(), getState().getPlayer().getFactionId(), getSegCon().getUniqueIdentifier());
							}
						};
						check.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(314);
					}
				};
			}

			@Override
			public boolean isActive() {
				return super.isActive() && getMan() != null && hasRights() && getMan() instanceof StationaryManagerContainer<?> && isSegConOwnFactionExists() && !getSegCon().isHomeBaseFor(getState().getPlayer().getFactionId());
			}

			@Override
			public String getName() {
				return Lng.str("Make Homebase");
			}

			@Override
			public HButtonColor getColor() {
				return HButtonColor.GREEN;
			}
		});
		addButton(pane.getContent(0), 1, indexY++, new ButtonResult() {

			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {

					@Override
					public void pressedRightMouse() {
					}

					@Override
					public void pressedLeftMouse() {
						PlayerGameOkCancelInput check = new PlayerGameOkCancelInput("CONFIRM", getState(), Lng.str("WARN"), Lng.str("Do you really want to do this?")) {

							@Override
							public boolean isOccluded() {
								return false;
							}

							@Override
							public void onDeactivate() {
							}

							@Override
							public void pressedOK() {
								deactivate();
								if (getState().getPlayer().getFactionId() != 0 && getState().getPlayer().getFactionId() == getSegCon().getFactionId() && getState().getPlayer().getFactionRights() < getSegCon().getFactionRights()) {
									getState().getController().popupAlertTextMessage(Lng.str("Permission denied!\n(faction rank)"), 0);
								} else {
									getState().getFactionManager().sendClientHomeBaseChange(getState().getPlayer().getName(), getState().getPlayer().getFactionId(), "");
									deactivate();
								}
							}
						};
						check.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(315);
					}
				};
			}

			@Override
			public boolean isActive() {
				return super.isActive() && getMan() != null && hasRights() && getSegCon().isHomeBaseFor(getState().getPlayer().getFactionId());
			}

			@Override
			public String getName() {
				return Lng.str("Revoke Homebase");
			}

			@Override
			public HButtonColor getColor() {
				return HButtonColor.RED;
			}
		});
		addLabel(pane.getContent(0), 0, indexY++, new LabelResult() {

			@Override
			public String getName() {
				return Lng.str("Permission Rank");
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.BIG_20;
			}
		});
		int perm = 0;
		for (byte i = -1; i < 5; i++) {
			addPermissionButton(pane.getContent(0), i, indexY);
		}
	}

	private void addPermissionButton(GUIAnchor a, final byte rank, int indexY) {
		addButton(a, rank, indexY, new ButtonResult() {

			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {

					@Override
					public void pressedRightMouse() {
					}

					@Override
					public void pressedLeftMouse() {
						final SegmentController sc = (getSegCon());
						final PlayerState p = getState().getPlayer();
						if ((sc.getFactionRights() != -1 && p.getFactionRights() == 4) || sc.isSufficientFactionRights(p)) {
							if (rank > p.getFactionRights()) {
								(new PlayerGameOkCancelInput("CONFIRM", getState(), Lng.str("Confirm"), Lng.str("WARNING!\nIf you set this rank, you will no longer\nbe able to edit or change it.\n\nDo you really want to do this?")) {

									@Override
									public boolean isOccluded() {
										return false;
									}

									@Override
									public void onDeactivate() {
									}

									@Override
									public void pressedOK() {
										p.sendSimpleCommand(SimplePlayerCommands.SET_FACTION_RANK_ON_OBJ, sc.getId(), rank);
										deactivate();
									}
								}).activate();
								/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
								AudioController.fireAudioEventID(316);
							} else {
								p.sendSimpleCommand(SimplePlayerCommands.SET_FACTION_RANK_ON_OBJ, sc.getId(), rank);
							}
						} else {
							getState().getController().popupAlertTextMessage(Lng.str("You don't have the right\nto set permission to that\nvalue"), 0);
							System.err.println("[CLIENT] Permission failed: " + p + ": Rank: " + p.getFactionRights() + "; Target " + sc + ": Rank: " + sc.getFactionRights() + "; toSet: " + rank);
						}
					}
				};
			}

			@Override
			public boolean isActive() {
				return super.isActive() && isSegConOwnFactionExists();
			}

			@Override
			public String getName() {
				return getRankString(rank) + (rank == getState().getPlayer().getFactionRights() ? "*" : "");
			}

			@Override
			public String getToolTipText() {
				return getRankStringLong(rank, rank == getState().getPlayer().getFactionRights());
			}

			@Override
			public long getToolTipDelayMs() {
				return 500;
			}

			@Override
			public HButtonColor getColor() {
				if (getSegCon() == null) {
					return HButtonColor.BLUE;
				}
				if (getSegCon().getFactionRights() == -1) {
					if (rank == -1) {
						return HButtonColor.GREEN;
					} else {
						return HButtonColor.RED;
					}
				} else if (getSegCon().getFactionRights() == -2) {
					if (rank == -1) {
						return HButtonColor.BLUE;
					} else {
						return HButtonColor.RED;
					}
				} else {
					if (rank >= getSegCon().getFactionRights()) {
						return HButtonColor.GREEN;
					} else {
						if (rank == -1) {
							return HButtonColor.BLUE;
						}
						return HButtonColor.RED;
					}
				}
			}
		});
	}

	private String getRankString(byte rank) {
		if (rank == -1) {
			return Lng.str("P");
		}
		if (rank == 4) {
			return Lng.str("F");
		}
		return String.valueOf(4 - rank);
	}

	private String getRankStringLong(byte rank, boolean current) {
		String o = "";
		if (current) {
			o = "\n" + Lng.str("(currently active rank)");
		}
		if (rank == -1) {
			return Lng.str("Personal Owner Rank\nMay only be accessed by the owner of this entity!") + o;
		}
		if (rank == 4) {
			return Lng.str("Faction Founder Rank\nMay only be accessed by the founder rank!") + o;
		}
		return Lng.str("Faction Rank %s\nMay only be accessed by this rank or better ranks!", String.valueOf(4 - rank)) + o;
	}

	public boolean hasRights() {
		return getSegCon().isSufficientFactionRights(getState().getPlayer());
	}

	public boolean hasFactionBlock() {
		return getSegCon().getElementClassCountMap().get(ElementKeyMap.FACTION_BLOCK) > 0;
	}

	private boolean isSegConOwnFactionExists() {
		return getState().getPlayer().getFactionId() != 0 && isSegConOwnFaction();
	}

	private boolean isSegConOwnFactionOrNeutral() {
		return getState().getPlayer().getFactionId() == 0 || isSegConOwnFaction();
	}

	private boolean isSegConOwnFaction() {
		return getSegCon() != null && getSegCon().getFactionId() == getState().getPlayer().getFactionId();
	}

	private int getTextDist() {
		return 150;
	}

	private boolean canTakeOwnerShip() {
		return isSegConOwnFactionOrNeutral() && hasRights();
	}

	@Override
	public String getId() {
		return "ASFACTION";
	}

	@Override
	public String getTitle() {
		return Lng.str("Faction & Owner");
	}
}
