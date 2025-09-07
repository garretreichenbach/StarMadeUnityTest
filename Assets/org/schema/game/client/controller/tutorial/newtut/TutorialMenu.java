package org.schema.game.client.controller.tutorial.newtut;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.config.ButtonColorPalette;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

/**
 * [Description]
 *
 * @author Garret Reichenbach
 */
public class TutorialMenu extends DialogInput  {

	private final TutorialDialogWindow dialogWindow;

	public TutorialMenu(InputState state) {
		super(state);
		dialogWindow = new TutorialDialogWindow(state);
		dialogWindow.onInit();
		dialogWindow.setCloseCallback(new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					AudioController.fireAudioEventID(291);
					deactivate();
				}
			}

			@Override
			public boolean isOccluded() {
				return !isActive();
			}
		});
	}

	@Override
	public void onDeactivate() {
		dialogWindow.cleanUp();
	}

	@Override
	public TutorialDialogWindow getInputPanel() {
		return dialogWindow;
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		dialogWindow.getCallback().callback(callingGuiElement, event);
	}

	@Override
	public boolean isOccluded() {
		return false;
	}

	private TutorialController getController() {
		return ((GameClientState) getState()).getController().getTutorialController();
	}

	public static class TutorialDialogWindow extends GUIDialogWindow {

		private GUIElementList topicsList;
		private GUIElementList documentPane;
		private GUIScrollablePanel documentPanel;

		public TutorialDialogWindow(InputState state) {
			super(state, UIScale.getUIScale().scale(800), UIScale.getUIScale().scale(650), "TUTORIAL_DIALOG_WINDOW");
		}

		@Override
		public void onInit() {
			super.onInit();
			getMainContentPane().setTextBoxHeightLast(UIScale.getUIScale().scale(650));
			getMainContentPane().addDivider(UIScale.getUIScale().scale(180));
			getMainContentPane().setTextBoxHeight(1, 0, UIScale.getUIScale().scale(650));

			topicsList = new GUIElementList(getState());
			topicsList.onInit();
			for(TutorialConfig config : TutorialConfig.getTutorials()) {
				GUITextOverlayTable overlay = new GUITextOverlayTable(getState());
				overlay.setTextSimple(config.displayName);
				overlay.onInit();
				overlay.setUserPointer(config);
				overlay.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
				GUIColoredRectangleLeftRightShadow anchor = new GUIColoredRectangleLeftRightShadow(getState(), UIScale.getUIScale().scale(170), UIScale.getUIScale().scale(30), ButtonColorPalette.ok);
				anchor.onInit();
				anchor.setUserPointer(config);
				anchor.attach(overlay);
				GUIListElement listElement = new GUIListElement(anchor, getState());
				listElement.heightDiff = UIScale.getUIScale().inset;
				listElement.onInit();
				listElement.setUserPointer(config);
				topicsList.addWithoutUpdate(listElement);
			}
			topicsList.updateDim();
			GUIScrollablePanel scrollablePanel = new GUIScrollablePanel(topicsList.getWidth(), topicsList.getHeight(), getMainContentPane().getContent(0, 0), getState()) {
				@Override
				public void draw() {
					topicsList.updateDim();
					super.draw();
				}
			};
			scrollablePanel.setContent(topicsList);
			topicsList.setScrollPane(scrollablePanel);
			scrollablePanel.setScrollable(GUIScrollablePanel.SCROLLABLE_VERTICAL);
			scrollablePanel.onInit();
			getMainContentPane().getContent(0, 0).attach(scrollablePanel);

			documentPane = new GUIElementList(getState());
			documentPane.onInit();
			documentPanel = new GUIScrollablePanel(documentPane.getWidth(), documentPane.getHeight(), getMainContentPane().getContent(1, 0), getState()) {
				@Override
				public void draw() {
					documentPane.updateDim();
					super.draw();
				}
			};
			documentPanel.setContent(documentPane);
			documentPane.setScrollPane(documentPanel);
			documentPanel.setScrollable(GUIScrollablePanel.SCROLLABLE_VERTICAL);
			documentPanel.onInit();
			getMainContentPane().getContent(1, 0).attach(documentPanel);

			topicsList.setCallback(new GUICallback() {
				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(callingGuiElement.getUserPointer() instanceof TutorialConfig config) {
						if(callingGuiElement instanceof GUIListElement element) {
							if(element.getContent() instanceof GUIColoredRectangleLeftRightShadow anchor) anchor.setColor(ButtonColorPalette.okMouseOver);
						}
						if(event.pressedLeftMouse()) {
							AudioController.fireAudioEventID(291);
							setConfig(config);
							//It has to be called thrice to force it to display the whole page... don't ask why
							if(callingGuiElement instanceof GUIListElement element) {
								if(element.getContent() instanceof GUIColoredRectangleLeftRightShadow anchor) anchor.setColor(ButtonColorPalette.okPressed);
							}
						}
					}
				}

				@Override
				public boolean isOccluded() {
					return false;
				}
			});
		}

		public void setConfig(TutorialConfig config) {
			if(config == null) return;
			if(!documentPane.isEmpty()) documentPane.clear();
			for(GUIElement element : config.getElements()) {
				if(element instanceof GUITextOverlay overlay) overlay.autoWrapOn = documentPanel;
				else if(element instanceof GUIAnchor background) {
					if(!background.getChilds().isEmpty() && background.getChilds().get(0) instanceof GUIScrollableStringTableList list) {
						list._getScrollPanel().setWidth(getMainContentPane().getContent(1, 0).getWidth() - UIScale.getUIScale().inset * 2);
						background.setWidth(getMainContentPane().getContent(1, 0).getWidth() - UIScale.getUIScale().inset * 2);
						background.setHeight((list.getElementList().size() * 24) + 34);
						list.flagDirty();
					}
				}
				GUIListElement listElement = new GUIListElement(element, getState());
				listElement.heightDiff = UIScale.getUIScale().inset * 4;
				listElement.onInit();
				documentPane.addWithoutUpdate(listElement);
			}
			documentPane.updateDim();
			for(GUIListElement element : topicsList.getElements()) {
				if(element.getContent() instanceof GUIColoredRectangle anchor) {
					if(anchor.getUserPointer() == config) anchor.setColor(ButtonColorPalette.okPressed);
					else anchor.setColor(ButtonColorPalette.ok);
				}
			}
		}
	}
}

