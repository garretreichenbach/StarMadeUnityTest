package org.schema.game.client.view.mainmenu.gui.screenshotviewer;

import api.utils.textures.StarLoaderTexture;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.client.view.gui.GUIScrollableOverlayList;
import org.schema.game.client.view.gui.LoadingScreenDetailed;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.LoadingScreen;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.input.InputState;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class ScreenshotViewerDialog extends DialogInput {

	private static final int TILE_WIDTH = 230;
	private static final int TILE_HEIGHT = 130;
	private final ScreenshotViewerPanel panel;
	private static ScreenshotFullViewDialog dialog;

	public ScreenshotViewerDialog(InputState state) {
		super(state);
		(panel = new ScreenshotViewerPanel(state, this)).onInit();
	}

	@Override
	public ScreenshotViewerPanel getInputPanel() {
		return panel;
	}

	@Override
	public void onDeactivate() {
		if(dialog != null) {
			dialog.deactivate();
			dialog = null;
		}
		if(panel != null) panel.cleanUp();
	}

	public static class ScreenshotViewerPanel extends GUIInputPanel {

		public ScreenshotViewerPanel(InputState state, GUICallback guiCallback) {
			super("Screenshot_Viewer_Panel", state, (TILE_WIDTH * 3) + 50, 850, guiCallback, Lng.str("Screenshot Viewer"), "");
		}

		@Override
		public void onInit() {
			super.onInit();
			GUIContentPane contentPane = ((GUIDialogWindow) background).getMainContentPane();
			contentPane.setTextBoxHeightLast(600);
			GUIScrollableOverlayList scrollableOverlayList = new GUIScrollableOverlayList(getState(), contentPane.getContent(0), GUIScrollablePanel.SCROLLABLE_VERTICAL | GUIScrollablePanel.SCROLLABLE_HORIZONTAL, TILE_WIDTH, TILE_HEIGHT);
			scrollableOverlayList.onInit();
			ArrayList<GUIOverlay> overlays = createPreviewOverlays();
			for(GUIOverlay overlay : overlays) {
				String name = ((ScreenshotManager.ScreenshotData) overlay.getUserPointer()).getFile().getName().trim();
				name = name.substring(0, name.lastIndexOf('.'));
				String finalName = name;
				scrollableOverlayList.addOverlay(overlay, name, new GUICallback() {
					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if(event.pressedLeftMouse()) { //View full image
							ScreenshotManager.ScreenshotData screenshot = (ScreenshotManager.ScreenshotData) overlay.getUserPointer();
							try {
								BufferedImage image = scaleImage(ImageIO.read(screenshot.getFile()), 800, 600);
								Sprite sprite = StarLoaderTexture.newSprite(image, finalName, false, false);
								sprite.setPositionCenter(true);
								if(dialog != null) dialog.deactivate();
								(dialog = new ScreenshotFullViewDialog(getState(), screenshot, sprite)).activate();
								scrollableOverlayList.setSelected(overlay);
							} catch(IOException exception) {
								exception.printStackTrace();
							}
						} else if(event.pressedRightMouse()) { //Right-click options for image
							//Todo: Implement right-click options
						} else if(event.pressedMiddleMouse()) { //Select for multi-action
							//Todo: Implement multi-action selection
						}
					}

					@Override
					public boolean isOccluded() {
						return dialog != null && dialog.isActive();
					}
				});
			}
			contentPane.getContent(0).attach(scrollableOverlayList);
		}

		private ArrayList<GUIOverlay> createPreviewOverlays() {
			ArrayList<GUIOverlay> overlays = new ArrayList<>();
			HashSet<ScreenshotManager.ScreenshotData> screenshots = ScreenshotManager.getScreenshots();
			for(ScreenshotManager.ScreenshotData data : screenshots) {
				BufferedImage image = data.getImage();
				//Scale to preview size, we will only load the full image when the user clicks on it
				image = scaleImage(image, TILE_WIDTH - 6, TILE_HEIGHT - 31);
				String name = data.getFile().getName().substring(0, data.getFile().getName().lastIndexOf('.'));
				Sprite sprite = StarLoaderTexture.newSprite(image, name, false, false);
				sprite.setPositionCenter(true);
				sprite.setPos(14, 17);
				GUIOverlay overlay = new GUIOverlay(sprite, getState());
				overlay.onInit();
				overlay.setUserPointer(data);
				overlays.add(overlay);
			}
			GlUtil.printGlError();
			return overlays;
		}

		public static BufferedImage scaleImage(BufferedImage image, int scaleWidth, int scaleHeight) {
			BufferedImage scaledImage = new BufferedImage(scaleWidth, scaleHeight, BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics2D = scaledImage.createGraphics();
			graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			graphics2D.drawImage(image, 0, 0, scaleWidth, scaleHeight, null);
			graphics2D.dispose();
			return scaledImage;
		}
	}
}
