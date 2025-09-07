package org.schema.game.client.view.gui.advancedEntity;

import java.util.Collection;

import org.schema.common.util.StringTools;
import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.tools.ButtonCallback;
import org.schema.game.client.view.gui.advanced.tools.ButtonResult;
import org.schema.game.client.view.gui.advanced.tools.DropdownCallback;
import org.schema.game.client.view.gui.advanced.tools.DropdownResult;
import org.schema.game.client.view.gui.advanced.tools.LabelResult;
import org.schema.game.client.view.gui.advanced.tools.StatLabelResult;
import org.schema.game.client.view.gui.reactor.ReactorTreeDialog;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorSet;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorTree;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorTreeChangeListener;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableDirtyInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonColor;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class AdvancedEntityReactor extends AdvancedEntityGUIGroup {

	protected ReactorTree selectedReactor;

	public AdvancedEntityReactor(AdvancedGUIElement e) {
		super(e);
	}

	@Override
	public String getId() {
		return "AEREACTOR";
	}

	@Override
	public String getTitle() {
		return Lng.str("Power & Reactor");
	}

	public int addReactorBlockIcons(GUIContentPane pane, int x, int y) {
		addStatLabel(pane.getContent(0), x, y++, new StatLabelResult() {

			@Override
			public String getName() {
				return Lng.str("Reactor:");
			}

			@Override
			public String getValue() {
				return ElementKeyMap.getInfo(ElementKeyMap.REACTOR_MAIN).getName();
			}

			@Override
			public int getStatDistance() {
				return 100;
			}
		});
		addStatLabel(pane.getContent(0), x, y++, new StatLabelResult() {

			@Override
			public String getName() {
				return Lng.str("Stabilizer:");
			}

			@Override
			public String getValue() {
				return ElementKeyMap.getInfo(ElementKeyMap.REACTOR_STABILIZER).getName();
			}

			@Override
			public int getStatDistance() {
				return 100;
			}
		});
		addWeaponBlockIcon(pane, x, y, new Object() {

			public String toString() {
				if (getMan() != null) {
					return Lng.str("Reactor (%s)", ElementKeyMap.getInfo(ElementKeyMap.REACTOR_MAIN).getName());
				}
				return "";
			}
		}, new InitInterface() {

			public short getType() {
				return ElementKeyMap.REACTOR_MAIN;
			}

			@Override
			public boolean isInit() {
				return getMan() != null;
			}
		});
		addWeaponBlockIcon(pane, x + 1, y++, new Object() {

			public String toString() {
				if (getMan() != null) {
					return Lng.str("Stabilizer (%s)", ElementKeyMap.getInfo(ElementKeyMap.REACTOR_STABILIZER).getName());
				}
				return "";
			}
		}, new InitInterface() {

			public short getType() {
				return ElementKeyMap.REACTOR_STABILIZER;
			}

			@Override
			public boolean isInit() {
				return getMan() != null;
			}
		});
		return y;
	}

	@Override
	public void build(GUIContentPane pane, GUIDockableDirtyInterface dInt) {
		pane.setTextBoxHeightLast(UIScale.getUIScale().scale(30));
		int indexY = 0;
		addButton(pane.getContent(0), 0, indexY++, new ButtonResult() {

			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {

					@Override
					public void pressedRightMouse() {
					}

					@Override
					public void pressedLeftMouse() {
						if (getState().getCurrentPlayerObject() != null && getState().getCurrentPlayerObject() instanceof ManagedSegmentController<?>) {
							if (getMan().getPowerInterface().getReactorSet().getTrees().size() > 0) {
								ReactorTreeDialog d = new ReactorTreeDialog(getState(), (ManagedSegmentController<?>) getState().getCurrentPlayerObject());
								d.activate();
								/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
								AudioController.fireAudioEventID(310);
							}
						}
					}
				};
			}

			@Override
			public String getName() {
				return Lng.str("Open Reactor Panel");
			}

			@Override
			public boolean isActive() {
				return getMan().getPowerInterface().getReactorSet().getTrees().size() > 0;
			}

			@Override
			public HButtonColor getColor() {
				return HButtonColor.BLUE;
			}
		});
		indexY = addReactorBlockIcons(pane, 0, indexY);
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
						promptBuild(ElementKeyMap.REACTOR_MAIN, 1, Lng.str("Build a reactor or add to an existing one"));
					}
				};
			}

			@Override
			public boolean isActive() {
				return super.isActive() && canQueue(ElementKeyMap.REACTOR_MAIN, 1);
			}

			@Override
			public String getName() {
				return Lng.str("Build Reactor");
			}

			@Override
			public HButtonColor getColor() {
				return HButtonColor.BLUE;
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
						resetQueue();
						promptBuild(ElementKeyMap.REACTOR_STABILIZER, 1, Lng.str("Stabilize your reactor by putting down stabilizers.\nThe closer they are to the reactor, the less effective they are."));
					}
				};
			}

			@Override
			public boolean isActive() {
				return super.isActive() && canQueue(ElementKeyMap.REACTOR_STABILIZER, 1);
			}

			@Override
			public String getName() {
				return Lng.str("Build Stabilization");
			}

			@Override
			public HButtonColor getColor() {
				return HButtonColor.BLUE;
			}
		});
		addLabel(pane.getContent(0), 0, indexY++, new LabelResult() {

			@Override
			public String getName() {
				return Lng.str("Reactor");
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.BIG_20;
			}
		});
		addDropdown(pane.getContent(0), 0, indexY++, new ReactorSelectDropdownResult());
		addStatLabel(pane.getContent(0), 0, indexY++, new StatLabelResult() {

			@Override
			public String getName() {
				return Lng.str("Size:");
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}

			@Override
			public String getValue() {
				if (selectedReactor != null) {
					return String.valueOf(selectedReactor.getActualSize());
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
				return Lng.str("Level:");
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}

			@Override
			public String getValue() {
				if (selectedReactor != null) {
					return String.valueOf(selectedReactor.getLevelReadable());
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
				return Lng.str("Next Level");
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}

			@Override
			public String getValue() {
				if (selectedReactor != null) {
					return String.valueOf(selectedReactor.getMaxLvlSize() + 1);
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
				return Lng.str("HP:");
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}

			@Override
			public String getValue() {
				if (selectedReactor != null) {
					return String.valueOf(selectedReactor.getHp()) + " / " + String.valueOf(selectedReactor.getMaxHp());
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
				return Lng.str("HP(%):");
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}

			@Override
			public String getValue() {
				if (selectedReactor != null) {
					return StringTools.formatPointZero(selectedReactor.getHpPercent() * 100d) + "%";
				} else {
					return Lng.str("n/a");
				}
			}

			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
		if (hasIntegrity()) {
			addStatLabel(pane.getContent(0), 0, indexY++, new StatLabelResult() {

				@Override
				public String getName() {
					return Lng.str("Integrity");
				}

				@Override
				public FontInterface getFontSize() {
					return FontSize.MEDIUM_15;
				}

				@Override
				public String getValue() {
					if (selectedReactor != null) {
						return StringTools.formatPointZero(selectedReactor.getIntegrity());
					} else {
						return Lng.str("n/a");
					}
				}

				@Override
				public int getStatDistance() {
					return getTextDist();
				}
			});
		}
		addLabel(pane.getContent(0), 0, indexY++, new LabelResult() {

			@Override
			public String getName() {
				return Lng.str("Stabilization (for act.)");
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.BIG_20;
			}
		});
		addStatLabel(pane.getContent(0), 0, indexY++, new StatLabelResult() {

			@Override
			public String getName() {
				return Lng.str("Stabilizers");
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}

			@Override
			public String getValue() {
				if (getMan() != null) {
					return String.valueOf(getMan().getStabilizer().getTotalSize());
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
				return Lng.str("Efficiency");
			}

			@Override
			public FontInterface getFontSize() {
				return FontSize.MEDIUM_15;
			}

			@Override
			public String getValue() {
				if (getMan() != null) {
					return StringTools.formatPointZero(getMan().getPowerInterface().getStabilizerEfficiencyTotal() * 100d) + "%";
				} else {
					return Lng.str("n/a");
				}
			}

			@Override
			public int getStatDistance() {
				return getTextDist();
			}
		});
	}

	private class ReactorSelectDropdownResult extends DropdownResult implements ReactorTreeChangeListener {

		private ObjectArrayList<GUIElement> list;

		private boolean dirty;

		private ManagerContainer<?> lastMan;

		@Override
		public DropdownCallback initCallback() {
			return value -> {
				if (value instanceof ReactorTree) {
					AdvancedEntityReactor.this.selectedReactor = ((ReactorTree) (value));
				}
			// AdvancedEntityReactor.this.selectedReactor = ((ReactorTree)((GUIElement)value).getUserPointer());
			};
		}

		@Override
		public void update(Timer timer) {
			if (getMan() != null && getMan() != lastMan) {
				dirty = true;
				if (lastMan != null) {
					getMan().getPowerInterface().getReactorSet().removeReactorTreeListener(this);
				}
				getMan().getPowerInterface().getReactorSet().addReactorTreeListener(this);
				lastMan = getMan();
			}
			super.update(timer);
		}

		@Override
		public String getToolTipText() {
			return Lng.str("Available Reactors");
		}

		@Override
		public String getName() {
			return Lng.str("Reactor");
		}

		@Override
		public Collection<? extends GUIElement> getDropdownElements(final GUIElement dep) {
			if (getMan() == null) {
				return new ObjectArrayList<GUIElement>();
			}
			list = new ObjectArrayList<GUIElement>();
			ReactorSet reactorSet = getMan().getPowerInterface().getReactorSet();
			for (ReactorTree t : reactorSet.getTrees()) {
				String nm = Lng.str("Reactor (lvl: %d)", t.getLevelReadable());
				if (t.isActiveTree()) {
					nm += " " + Lng.str("(active)");
				}
				GUIAnchor guiAnchor = new GUIAnchor(getState(), 200, 20);
				guiAnchor.setUserPointer(t);
				list.add(guiAnchor);
				final GUITextOverlay td = new GUITextOverlay(FontSize.MEDIUM_15, getState()) {

					@Override
					public void draw() {
						if (dep != null) {
							limitTextWidth = (int) (dep.getWidth());
						}
						super.draw();
					}
				};
				td.setTextSimple(nm);
				td.getPos().x = 3;
				td.getPos().y = 2;
				guiAnchor.attach(td);
				if (selectedReactor != null && selectedReactor.getId() == t.getId()) {
					// reselect selected reactor to make sure the data is current
					selectedReactor = t;
				} else if (selectedReactor == null) {
					if (t.isActiveTree()) {
						selectedReactor = t;
					}
				}
			}
			dirty = false;
			return list;
		}

		@Override
		public Object getDefault() {
			if (list == null) {
				return null;
			}
			for (GUIElement l : list) {
				if (((ReactorTree) l.getUserPointer()).isActiveTree()) {
					return l;
				}
			}
			return list.size() > 0 ? list.get(0) : null;
		}

		@Override
		public boolean needsListUpdate() {
			return dirty;
		}

		@Override
		public void flagListNeedsUpdate(boolean b) {
			dirty = b;
		}

		@Override
		public void onReceivedTree() {
			dirty = true;
		}

		@Override
		public void onReactorSizeChanged(ReactorTree t, boolean damaged) {
		}
	}
}
