/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.schema.game.client.view.gui.advancedbuildmode;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.tools.*;
import org.schema.game.common.data.player.inventory.InventorySlot;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableDirtyInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.sound.controller.AudioController;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author brent
 */
public class AdvancedBuildModeHotbar extends AdvancedBuildModeGUISGroup {

	public static String selected;

	private boolean dirty = true;

	private ObjectArrayList<GUIElement> list;

	public AdvancedBuildModeHotbar(AdvancedGUIElement e) {
		super(e);
	}

	@Override
	public void build(GUIContentPane pane, GUIDockableDirtyInterface dInt) {
		pane.setTextBoxHeightLast(UIScale.getUIScale().scale(30));
		// GUITextOverlay l2 = new GUITextOverlay(10, 10, FontSize.MEDIUM.getFont(), getState());
		addLabel(pane.getContent(0), 0, 0, new LabelResult() {

			@Override
			public String getName() {
				return Lng.str("Hotbar Layout");
			}

			@Override
			public AdvResult.HorizontalAlignment getHorizontalAlignment() {
				return AdvResult.HorizontalAlignment.MID;
			}
		});
		addDropdown(pane.getContent(0), 0, 1, new DropdownResult() {

			@Override
			public DropdownCallback initCallback() {
				return value -> {
					if(value instanceof String) {
						selected = (String) value;
						System.out.println("HOTBAR: selected " + value);
					}
				};
			}

			@Override
			public String getToolTipText() {
				if(selected != null) {
					return selected;
				} else {
					return Lng.str("Select Hotbar layout to load");
				}
			}

			@Override
			public String getName() {
				return Lng.str("Hotbar Layouts");
			}

			@Override
			public Collection<? extends GUIElement> getDropdownElements(GUIElement dep) {
				return getGUIList();
			}

			@Override
			public Object getDefault() {
				if(selected != null && list != null) return indexOf(selected);
				else if(list == null || list.isEmpty()) return "";
				else return list.getFirst();
			}

			private Object indexOf(String selected) {
				for(int i = 0; i < list.size(); i++) {
					if(list.get(i).getUserPointer().equals(selected)) return i;
				}
				return "";
			}

			@Override
			public void update(Timer timer) {
				super.update(timer);
			}

			@Override
			public boolean needsListUpdate() {
				return dirty;
			}

			@Override
			public void flagListNeedsUpdate(boolean b) {
				dirty = b;
			}
		});
		addButton(pane.getContent(0), 0, 2, new ButtonResult() {

			@Override
			public GUIHorizontalArea.HButtonColor getColor() {
				return GUIHorizontalArea.HButtonColor.BLUE;
			}

			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {

					@Override
					public void pressedLeftMouse() {
						if(selected != null && !selected.isEmpty()) {
							getPlayerInteractionControlManager().getHotbarLayout().setCurrentHotbar(selected);
						}
					}

					@Override
					public void pressedRightMouse() {
					}
				};
			}

			@Override
			public boolean isActive() {
				return list.size() > 0 && selected != null;
			}

			@Override
			public String getName() {
				return Lng.str("Load Layout");
			}
		});
		addButton(pane.getContent(0), 1, 2, new ButtonResult() {

			@Override
			public GUIHorizontalArea.HButtonColor getColor() {
				return GUIHorizontalArea.HButtonColor.BLUE;
			}

			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {

					@Override
					public void pressedLeftMouse() {
						PlayerGameTextInput txtInput = new PlayerGameTextInput("HotbarPanel_SAVE", getState(), 50, Lng.str("Save hotbar layout"), Lng.str("Layout name:")) {

							@Override
							public void onDeactivate() {
							}

							@Override
							public boolean onInput(String entry) {
								if(!entry.trim().isEmpty() && !getPlayerInteractionControlManager().getHotbarLayout().containsHotbar(entry)) {
									getPlayerInteractionControlManager().getHotbarLayout().addHotbarLayout(entry);
									getPlayerInteractionControlManager().getHotbarLayout().writeHotbars();
									getState().getController().popupInfoTextMessage(Lng.str("Hotbar layout added."), 0);
									dirty = true;
									return true;
								}
								// TO-DO: Error handling
								return false;
							}

							@Override
							public String[] getCommandPrefixes() {
								return null;
							}

							@Override
							public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
								return s;
							}

							@Override
							public void onFailedTextCheck(String msg) {
							}
						};
						txtInput.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(307);
					}

					@Override
					public void pressedRightMouse() {
					}
				};
			}

			@Override
			public String getName() {
				return Lng.str("Save Layout");
			}
		});
		addButton(pane.getContent(0), 2, 2, new ButtonResult() {

			@Override
			public GUIHorizontalArea.HButtonColor getColor() {
				return GUIHorizontalArea.HButtonColor.ORANGE;
			}

			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {

					@Override
					public void pressedLeftMouse() {
						PlayerGameOkCancelInput check = new PlayerGameOkCancelInput("CONFIRM", getState(), Lng.str("Remove hotbar layout?"), Lng.str("Are you sure you want to remove this hotbar layout?")) {

							@Override
							public void onDeactivate() {
								getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().hinderInteraction(400);
							}

							@Override
							public void pressedOK() {
								getPlayerInteractionControlManager().getHotbarLayout().removeHotbarLayout(selected);
								getState().getController().popupInfoTextMessage(Lng.str("Hotbar layout removed%n%s", selected), 0);
								getPlayerInteractionControlManager().getHotbarLayout().writeHotbars();
								dirty = true;
								deactivate();
							}
						};
						check.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(308);
					}

					@Override
					public void pressedRightMouse() {
					}
				};
			}

			@Override
			public boolean isActive() {
				return list.size() > 0 && selected != null;
			}

			@Override
			public String getName() {
				return Lng.str("Remove");
			}
		});
	}

	@Override
	public String getId() {
		return "ABMHOTBARLAYOUT";
	}

	@Override
	public String getTitle() {
		return Lng.str("Hotbar");
	}

	private List<GUIElement> getGUIList() {
		if(dirty) {
			list = new ObjectArrayList<>();
			for(Map.Entry<String, InventorySlot[]> e : getPlayerInteractionControlManager().getHotbarLayout().getLayouts().entrySet()) {
				GUIAnchor c = new GUIAnchor(getState(), UIScale.getUIScale().scale(200), UIScale.getUIScale().h);
				GUITextOverlay o = new GUITextOverlay(FontLibrary.FontSize.MEDIUM_15, getState());
				o.setTextSimple(e.getKey());
				o.setPos(UIScale.getUIScale().smallinset, UIScale.getUIScale().smallinset);
				c.attach(o);
				list.add(c);
				c.setUserPointer(e.getKey());
			}
			dirty = false;
		}
		return list;
	}
}
