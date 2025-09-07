package org.schema.game.common.facedit;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.schema.game.client.view.GameResourceLoader;
import org.schema.game.common.util.GuiErrorHandler;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.resource.FileExt;

public class EditorTextureManager {

	private static final int resolution = EngineSettings.M_TEXTURE_PACK_CONFIG_TOOL.getString().contains("256") ? 256 :
			(EngineSettings.M_TEXTURE_PACK_CONFIG_TOOL.getString().contains("128") ? 128 : 64);
	private static ImageIcon[] textures;

	public static ImageIcon getImage(int textureId) {
		if (textures == null) {
			try {
				load();
			} catch (IOException e) {
				e.printStackTrace();
				GuiErrorHandler.processErrorDialogException(e);
			}
		}
		return textures[textureId];
	}

	private static void load() throws IOException {
		textures = new ImageIcon[8 * 256];
		BufferedImage img[] = new BufferedImage[8];
		img[0] = ImageIO.read(new FileExt(EngineSettings.M_TEXTURE_PACK_CONFIG_TOOL.getString() + "t000.png"));
		img[1] = ImageIO.read(new FileExt(EngineSettings.M_TEXTURE_PACK_CONFIG_TOOL.getString() + "t001.png"));
		img[2] = ImageIO.read(new FileExt(EngineSettings.M_TEXTURE_PACK_CONFIG_TOOL.getString() + "t002.png"));
		img[3] = ImageIO.read(new FileExt(EngineSettings.M_TEXTURE_PACK_CONFIG_TOOL.getString() + "t003.png"));
		img[7] = ImageIO.read(new FileExt(GameResourceLoader.CUSTOM_TEXTURE_PATH + File.separator + resolution + File.separator + "custom.png"));

		for (int i = 0; i < textures.length; i++) {
			int x = (i % 256) % 16;
			int y = (i % 256) / 16;
			if (img[i / 256] != null) {
				BufferedImage image = img[i / 256];

				BufferedImage subimage = image.getSubimage(x * resolution, y * resolution, resolution, resolution);
				textures[i] = new ImageIcon(subimage);
			}
		}
		//		for(int i = 0; i < img.length; i++){
		//		}
	}
}
