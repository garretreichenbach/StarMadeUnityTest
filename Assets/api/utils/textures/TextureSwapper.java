package api.utils.textures;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.lwjgl.opengl.GL11;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.texture.Texture;
import org.schema.schine.graphicsengine.texture.TextureLoader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Internal utilities class to swap out textures
 * Public incase anyone wants to play around with it, however it is preferred to use StarLoaderTexture
 * as it can batch changes more effectively.
 */
public class TextureSwapper {

	public static final int TEXTURES_STORED_PER_ROW = 16;
	public static final int TEXTURES_PER_SHEET = 16 * 16;

	public static void addSpriteToMap(String name, Sprite sprite) {
		Object2ObjectOpenHashMap<String, Sprite> spriteMap = Controller.getResLoader().getImageLoader().getSpriteMap();
		spriteMap.put(name, sprite);
	}

	public static BufferedImage getImageFromTexture(Texture texture) {
		String textureFile = texture.getName();
		if(textureFile.endsWith(".tga")) {
			try {
				BufferedImage image = TargaReader.getImage(textureFile);
				return image;
			} catch(IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				return ImageIO.read(new File(textureFile));
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	//FIXME: Currently just reading from disk, would be nice to read from opengl
	public static BufferedImage getImageFromSprite(Sprite sprite) throws IOException {
		String textureFile = sprite.getMaterial().getTexture().getName();
		return ImageIO.read(new File(textureFile));
		//Get sprite
		//get texture
		//get path
		//load as buffered image
		//allow user to modify buffered image
		//create texture from bufferedimage
		//update sprite with new texture
	}

	//FIXME: Bind texture to same id
	public static void swapSpriteTexture(Sprite spr, BufferedImage newImage) {
		String textureURL = spr.getMaterial().getTexture().getName();
		Texture texture = TextureLoader.getTexture(newImage, textureURL, GL11.GL_TEXTURE_2D, GL11.GL_RGBA, GL11.GL_LINEAR, GL11.GL_LINEAR, false, false);
		spr.getMaterial().getTexture().detach();
		spr.getMaterial().setTexture(texture);
	}

	public static Texture getTextureFromImage(BufferedImage img, String textureURL, boolean mipmap, boolean compress) {
		return TextureLoader.getTexture(img, textureURL, GL11.GL_TEXTURE_2D, GL11.GL_RGBA, GL11.GL_LINEAR, GL11.GL_LINEAR, mipmap, compress);
	}

	public static Sprite getSpriteFromName(String name) {
		return Controller.getResLoader().getSprite(name);
	}

	public static void modGraphics(String sprite, GraphicsOperator operator) {
		try {
			Sprite spriteFromName = getSpriteFromName(sprite);
			BufferedImage img = getImageFromSprite(spriteFromName);
			operator.apply(img, img.getGraphics());
			swapSpriteTexture(spriteFromName, img);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public static void setBlockTexture(int current_scale, BufferedImage[] textures, int textureAtlasId, StarLoaderTexture texture, boolean normal, boolean[] modificationArray) {
//        int current_scale = textures[0].getWidth() / TEXTURES_STORED_PER_ROW;
//        DebugFile.log("Scale:" + current_scale);
		int sheet = textureAtlasId / TEXTURES_PER_SHEET;
		int sheet_id = textureAtlasId % TEXTURES_PER_SHEET;
		int sheet_x = sheet_id % TEXTURES_STORED_PER_ROW;
		int sheet_y = sheet_id / TEXTURES_STORED_PER_ROW;

		//FIXME: Temporary fix, texture sheet 7 is the only one that works
		if(sheet == 3) {
			sheet = 7;
		} else if(sheet == 7) {
			sheet = 3;
		}
		modificationArray[sheet] = true;
//        DebugFile.log("X, Y:" + sheet_x + ", " + sheet_y);
		BufferedImage img = textures[sheet];
		System.err.println("Sheet: " + sheet + ", sheet id:" + sheet_id + ", sheet_x: " + sheet_x + ", sheet_y: " + sheet_y + ", c_scale: " + current_scale + " normal: " + normal);
		Image textureside;
		if(normal) {
			textureside = texture.getNormalTexture(current_scale);
		} else {
			textureside = texture.getTexture(current_scale);
		}
		// Images may have no normal texture
		if(textureside != null) {
			img.getGraphics().drawImage(textureside, sheet_x * current_scale, sheet_y * current_scale, null);
		}
	}

	public static void setOverlayTexture(int current_scale, BufferedImage overlayImg, int textureAtlasId, StarLoaderTexture texture) {
		int sheet_x = textureAtlasId % TEXTURES_STORED_PER_ROW;
		int sheet_y = textureAtlasId / TEXTURES_STORED_PER_ROW;
		overlayImg.getGraphics().drawImage(texture.getTexture(current_scale), sheet_x * current_scale, sheet_y * current_scale, null);
	}

	public static void setIconTexture(BufferedImage image, int textureAtlasId, StarLoaderTexture texture) {
//        int current_scale = textures[0].getWidth() / TEXTURES_STORED_PER_ROW;
//        DebugFile.log("Scale:" + current_scale);
		int sheet = textureAtlasId / TEXTURES_PER_SHEET;
		int sheet_id = textureAtlasId % TEXTURES_PER_SHEET;
		int sheet_x = sheet_id % TEXTURES_STORED_PER_ROW;
		int sheet_y = sheet_id / TEXTURES_STORED_PER_ROW;

		int current_scale = 64;
		System.err.println("Sheet: " + sheet + ", sheet id:" + sheet_id + ", sheet_x: " + sheet_x + ", sheet_y: " + sheet_y);
		image.getGraphics().drawImage(texture.getTexture(current_scale), sheet_x * current_scale, sheet_y * current_scale, null);
	}

}
