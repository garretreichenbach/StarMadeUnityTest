package api.utils.textures;

import api.utils.other.HashList;
import org.schema.game.client.view.WorldDrawer;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.forms.Sprite;

import javax.annotation.Nullable;
import javax.vecmath.Vector4f;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 * Created by Jake on 9/28/2020.
 * <insert description here>
 */
public class StarLoaderTexture {
	private static final HashList<Sprite, GraphicsOperator> textureOpMap = new HashList<>();
	public static HashMap<Integer, StarLoaderTexture> textures = new HashMap<>();
	public static HashMap<Integer, StarLoaderTexture> overlayTextures = new HashMap<>();
	public static HashMap<Integer, StarLoaderTexture> iconTextures = new HashMap<>();
	private static int customTextureCount = 16 * 16 * 3; //Start at the 4th texture sheet
	private static int iconTextureLog = 16 * 16 * 6; //Start of sheet 05 Todo: Every time we add a new icon sheet in vanilla, we need to update this
	public Image res64;
	public Image res128;
	public Image res256;
	public Image nrmRes64;
	public Image nrmRes128;
	public Image nrmRes256;
	private int textureId;

	private StarLoaderTexture() {

	}

	/**
	 * Creates a new sprite with the given image and name.
	 * @param img The image to create the sprite from
	 * @param name The name of the sprite
	 * @return The sprite
	 */
	@Deprecated
	public static Sprite newSprite(BufferedImage img, String name) {
		return newSprite(img, name, true, true);
	}

	/**
	 * Creates a new sprite with the given image and name.
	 * @param img The image to create the sprite from
	 * @param name The name of the sprite
	 * @param mipmap Whether or not to use mipmapping
	 * @param compress Whether or not to compress the texture
	 * @return The sprite
	 */
	@Deprecated
	public static Sprite newSprite(BufferedImage img, String name, boolean mipmap, boolean compress) {
		Sprite sprite = new Sprite(TextureSwapper.getTextureFromImage(img, name, mipmap, compress));
		//Texture defaults: No tint, centered
		sprite.setPositionCenter(true);
		sprite.setTint(new Vector4f(1, 1, 1, 1));
		TextureSwapper.addSpriteToMap(name, sprite);
		return sprite;
	}

	/**
	 * Gets a sprite from the resloader, and if it doesn't exist, creates a new one. This should be used instead of {@link #newSprite(BufferedImage, String)}
	 * in order to avoid the excess overhead of creating a new one every time.
	 * @param image The image to create the sprite from
	 * @param name The name of the sprite
	 * @return The sprite
	 */
	public static Sprite fetchSprite(BufferedImage image, String name) {
		return fetchSprite(image, name, true, true);
	}

	/**
	 * Gets a sprite from the resloader, and if it doesn't exist, creates a new one. This should be used instead of {@link #newSprite(BufferedImage, String)}
	 * in order to avoid the excess overhead of creating a new one every time.
	 * @param image The image to create the sprite from
	 * @param name The name of the sprite
	 * @param mipmap Whether or not to use mipmapping
	 * @param compress Whether or not to compress the texture
	 * @return The sprite
	 */
	public static Sprite fetchSprite(BufferedImage image, String name, boolean mipmap, boolean compress) {
		Sprite sprite = Controller.getResLoader().getSprite(name);
		if(sprite == null) sprite = newSprite(image, name, mipmap, compress);
		return sprite;
	}
	
	public static boolean isCached(String imageName) {
		return Controller.getResLoader().getSprite(imageName) != null;
	}

	public static Sprite fetchCachedSprite(String imageName) {
		return Controller.getResLoader().getSprite(imageName);
	}
	
	/**
	 * Creates a new StarLoader texture with a new id
	 *
	 * @param img The buffered image to create the texture from
	 * @return The StarLoaderTexture
	 */
	public static StarLoaderTexture newBlockTexture(BufferedImage img) {
		return newBlockTexture(img, customTextureCount++);
	}

	/**
	 * Creates an overlay texture, this is used for ores.  You will need to register a URV for ores.
	 */
	public static StarLoaderTexture newOverlayTexture(BufferedImage img, int urvId) {
		StarLoaderTexture texture = new StarLoaderTexture();
		texture.res64 = img.getScaledInstance(64, 64, Image.SCALE_SMOOTH);
		texture.res128 = img.getScaledInstance(128, 128, Image.SCALE_SMOOTH);
		texture.res256 = img.getScaledInstance(256, 256, Image.SCALE_SMOOTH);
		int startOfCustomOverlayTextures = 16;
		texture.textureId = startOfCustomOverlayTextures + urvId;
		overlayTextures.put(texture.textureId, texture);
		return texture;
	}

	public static StarLoaderTexture newBlockTexture(BufferedImage img, int texId) {
		return newBlockTexture(img, null, texId);
	}

	/**
	 * Creates a new StarLoader texture with a custom ID, will overwrite any existing textures of that id
	 *
	 * @param img   The buffered image to create the texture from
	 * @param texId Texture ID to overwrite, if any.
	 * @return The StarLoaderTexture
	 */
	public static StarLoaderTexture newBlockTexture(BufferedImage img, @Nullable BufferedImage normalMap, int texId) {
		StarLoaderTexture texture = new StarLoaderTexture();
		texture.res64 = img.getScaledInstance(64, 64, Image.SCALE_FAST);
		texture.res128 = img.getScaledInstance(128, 128, Image.SCALE_SMOOTH);
		texture.res256 = img.getScaledInstance(256, 256, Image.SCALE_SMOOTH);
		if(normalMap != null) {
			texture.nrmRes64 = normalMap.getScaledInstance(64, 64, Image.SCALE_FAST);
			texture.nrmRes128 = normalMap.getScaledInstance(128, 128, Image.SCALE_SMOOTH);
			texture.nrmRes256 = normalMap.getScaledInstance(256, 256, Image.SCALE_SMOOTH);
		}

		texture.textureId = texId;
		textures.put(texture.textureId, texture);
		return texture;
	}

	/**
	 * Block icon textures
	 */
	public static StarLoaderTexture newIconTexture(BufferedImage img) {
		StarLoaderTexture slTexture = new StarLoaderTexture();
		slTexture.res64 = img;
		if(img.getWidth() != 64 || img.getHeight() != 64) {
			System.err.println("!! WARNING !! Provided icon texture was not 64x64");
		}
		slTexture.textureId = iconTextureLog++;
		iconTextures.put(slTexture.textureId, slTexture);
		return slTexture;
	}

	public static void addSpriteChange(String spriteName, GraphicsOperator operator) {
		Sprite sprite = Controller.getResLoader().getSprite(spriteName);
		assert sprite != null : "Sprite is null! not good!";
		textureOpMap.add(sprite, operator);
	}

	public static HashList<Sprite, GraphicsOperator> getTextureOperationMap() {
		return textureOpMap;
	}

	/**
	 * Runs a runnable on the texture thread
	 */
	public static void runOnGraphicsThread(Runnable runnable) {
		WorldDrawer.runQueue.add(runnable);
	}

	public int getTextureId() {
		return textureId;
	}

	public Image getTexture(int res) {
		return switch(res) {
			case 64 -> res64;
			case 128 -> res128;
			case 256 -> res256;
			default -> null;
		};
	}

	@Nullable
	public Image getNormalTexture(int res) {
		return switch(res) {
			case 64 -> nrmRes64;
			case 128 -> nrmRes128;
			case 256 -> nrmRes256;
			default -> null;
		};
	}
}
