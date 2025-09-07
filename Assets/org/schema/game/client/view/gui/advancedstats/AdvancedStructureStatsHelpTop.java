package org.schema.game.client.view.gui.advancedstats;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIQuickReferencePanel;
import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.tools.ButtonCallback;
import org.schema.game.client.view.gui.advanced.tools.ButtonResult;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableDirtyInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonColor;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIResizableGrabbableWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.sound.controller.AudioController;

public class AdvancedStructureStatsHelpTop extends AdvancedStructureStatsGUISGroup {

	public AdvancedStructureStatsHelpTop(AdvancedGUIElement e) {
		super(e);
	}

	private static class QuickReferenceDialog extends DialogInput {

		private final GUIQuickReferencePanel p;

		public QuickReferenceDialog(GameClientState state) {
			super(state);
			p = new GUIQuickReferencePanel(state, this);
			p.onInit();
		}

		@Override
		public GUIElement getInputPanel() {
			return p;
		}

		@Override
		public void onDeactivate() {
			p.cleanUp();
		}

		@Override
		public void update(Timer timer) {
			super.update(timer);
			p.update(timer);
		}
	}

	@Override
	public void build(GUIContentPane pane, GUIDockableDirtyInterface dInt) {
		pane.setTextBoxHeightLast(UIScale.getUIScale().scale(30));
		GUITextOverlay l2 = new GUITextOverlay(getState());
		addButton(pane.getContent(0), 0, 0, new ButtonResult() {

			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {

					@Override
					public void pressedRightMouse() {
					}

					@Override
					public void pressedLeftMouse() {
						if (getState().getPlayerInputs().isEmpty()) {
							QuickReferenceDialog d = new QuickReferenceDialog(getState());
							d.activate();
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
							AudioController.fireAudioEventID(318);
						}
					}
				};
			}

			@Override
			public String getName() {
				return Lng.str("Building Quick-Reference");
			}

			@Override
			public HButtonColor getColor() {
				return HButtonColor.PINK;
			}
		});
	}

	@Override
	public String getId() {
		return "ASTPHELP";
	}

	@Override
	public String getTitle() {
		return Lng.str("Help");
	}

	@Override
	public boolean isDefaultExpanded() {
		return true;
	}

	@Override
	public int getSubListIndex() {
		return 0;
	}

	@Override
	public boolean isExpandable() {
		return false;
	}

	@Override
	public boolean isClosable() {
		return true;
	}

	@Override
	public void onClosed() {
		GUIResizableGrabbableWindow.setHidden(getWindowId(), true);
	}
}
