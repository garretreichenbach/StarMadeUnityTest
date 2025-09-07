package org.schema.game.client.view.mainmenu.gui.screenshotviewer;

import api.utils.textures.StarLoaderTexture;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;

import java.awt.image.BufferedImage;

public class ScreenshotFullViewDialog extends DialogInput {

	private final ScreenshotViewPanel panel;

	public ScreenshotFullViewDialog(InputState state, ScreenshotManager.ScreenshotData screenshotData, Sprite sprite) {
		super(state);
		(panel = new ScreenshotViewPanel(state, this, screenshotData, sprite)).onInit();
	}

	@Override
	public void onDeactivate() {
		if(panel != null) panel.cleanUp();
	}

	@Override
	public ScreenshotViewPanel getInputPanel() {
		return panel;
	}

	public static class ScreenshotViewPanel extends GUIInputPanel {

		private final ScreenshotManager.ScreenshotData screenshotData;
		private Sprite sprite;
		private GUIOverlay spriteOverlay;
		private BufferedImage image;
		private GUIContentPane contentPane;
		
		public ScreenshotViewPanel(InputState state, GUICallback guiCallback, ScreenshotManager.ScreenshotData screenshotData, Sprite sprite) {
			super("Screenshot_View_Full_Panel", state, 900, 780, guiCallback, screenshotData.getName(), "");
			this.screenshotData = screenshotData;
			this.sprite = sprite;
			image = screenshotData.getImage();
		}

		@Override
		public void onInit() {
			super.onInit();
			background.setResizable(false);
			contentPane = ((GUIDialogWindow) background).getMainContentPane();
			contentPane.setTextBoxHeightLast(28);
			
			GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 4, 1, contentPane.getContent(0));
			buttonPane.onInit();
			if(!screenshotData.isFavorite()) {
				buttonPane.addButton(0, 0, Lng.str("Mark as Favorite"), GUIHorizontalArea.HButtonColor.GREEN, new GUICallback() {
					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if(event.pressedLeftMouse()) {
							screenshotData.setFavorite(!screenshotData.isFavorite());
							cleanUp();
							onInit();
						}
					}

					@Override
					public boolean isOccluded() {
						return false;
					}
				}, new GUIActivationHighlightCallback() {
					@Override
					public boolean isHighlighted(InputState state) {
						return screenshotData.isFavorite();
					}

					@Override
					public boolean isVisible(InputState state) {
						return true;
					}

					@Override
					public boolean isActive(InputState state) {
						return true;
					}
				});
			} else {
				buttonPane.addButton(0, 0, Lng.str("Unmark as Favorite"), GUIHorizontalArea.HButtonColor.ORANGE, new GUICallback() {
					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if(event.pressedLeftMouse()) {
							screenshotData.setFavorite(!screenshotData.isFavorite());
							cleanUp();
							onInit();
						}
					}

					@Override
					public boolean isOccluded() {
						return false;
					}
				}, new GUIActivationHighlightCallback() {
					@Override
					public boolean isHighlighted(InputState state) {
						return !screenshotData.isFavorite();
					}

					@Override
					public boolean isVisible(InputState state) {
						return true;
					}

					@Override
					public boolean isActive(InputState state) {
						return true;
					}
				});
			}
			buttonPane.addButton(1, 0, Lng.str("Copy to Clipboard"), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse()) screenshotData.copyToClipboard();
				}

				@Override
				public boolean isOccluded() {
					return false;
				}
			}, new GUIActivationCallback() {
				@Override
				public boolean isVisible(InputState state) {
					return true;
				}

				@Override
				public boolean isActive(InputState state) {
					return true;
				}
			});
			buttonPane.addButton(2, 0, Lng.str("Open in File Explorer"), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse()) screenshotData.openInFileExplorer();
				}

				@Override
				public boolean isOccluded() {
					return false;
				}
			}, new GUIActivationCallback() {
				@Override
				public boolean isVisible(InputState state) {
					return true;
				}

				@Override
				public boolean isActive(InputState state) {
					return true;
				}
			});
			buttonPane.addButton(3, 0, Lng.str("Delete Screenshot"), GUIHorizontalArea.HButtonColor.RED, new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse()) {
						ScreenshotManager.removeData(screenshotData);
						onInit();
					}
				}

				@Override
				public boolean isOccluded() {
					return false;
				}
			}, new GUIActivationCallback() {
				@Override
				public boolean isVisible(InputState state) {
					return true;
				}

				@Override
				public boolean isActive(InputState state) {
					return true;
				}
			});
			contentPane.getContent(0).attach(buttonPane);

			image = ScreenshotViewerDialog.ScreenshotViewerPanel.scaleImage(screenshotData.getImage(), (int) contentPane.getWidth() - 24, (int) contentPane.getHeight() - 116);
			sprite = StarLoaderTexture.newSprite(image, screenshotData.getName(), false, false);
			sprite.setPositionCenter(true);

			contentPane.addNewTextBox(100);
			spriteOverlay = new GUIOverlay(sprite, getState());
			spriteOverlay.onInit();
			contentPane.getContent(1).attach(spriteOverlay);
		}
		
		@Override
		public void draw() {
			spriteOverlay.setPos(sprite.getWidth() / 2.0f, sprite.getHeight() / 2.0f, 0);
			super.draw();
		}
	}
}