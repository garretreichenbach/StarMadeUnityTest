package org.schema.schine.resource;

import api.listener.events.draw.SpriteLoadEvent;
import api.mod.StarLoader;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.ImageProbs;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.texture.Texture;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageLoader {

	static Pattern multiTexturePathPattern = Pattern.compile("(.*)-([0-9]+x[0-9]+)(.*)");

	/**
	 * The image map.
	 */
	private final Object2ObjectOpenHashMap<String, ImageProbs> imageMap;

	/**
	 * The sprite map.
	 */
	private final Object2ObjectOpenHashMap<String, Sprite> spriteMap;

	public ImageLoader() {
		imageMap = new Object2ObjectOpenHashMap<String, ImageProbs>();
		spriteMap = new Object2ObjectOpenHashMap<String, Sprite>();
	}

	/**
	 * @return the imageMap
	 */
	public Object2ObjectOpenHashMap<String, ImageProbs> getImageMap() {
		return imageMap;
	}

	//	/**
	//	 * Gets the sprite file.
	//	 *
	//	 * @param path the path
	//	 * @param filename the filename
	//	 * @return the sprite file
	//	 * @throws FileNotFoundException the file not found exception
	//	 */
	//	private File getSpriteFile(String path, String filename)
	//			throws FileNotFoundException {
	//
	//		File dir = null;
	//		try {
	//			dir = new FileExt(resourceUtil.getResourceURL(
	//					DataUtil.dataPath + path).getFile());
	//		} catch (ResourceException e) {
	//
	//			e.printStackTrace();
	//		}
	//		if (!dir.exists() || !dir.isDirectory()) {
	//			throw new FileNotFoundException(path
	//					+ " is not a Directory \n" + dir.getAbsolutePath());
	//		}
	//		File files[] = dir.listFiles();
	//		for (File g : files) {
	//			if (g.getName().contains(filename)
	//					&& g.getName().matches(
	//							filename + "-[0-9]+-[0-9]+x[0-9]+.png")
	//							&& g.getName().endsWith(".png")) {
	//				return g;
	//			}
	//		}
	//		throw new FileNotFoundException(filename
	//				+ "-count-widthxheight.png Sprite-description-format not found in "
	//				+ dir.getAbsolutePath());
	//	}

	/**
	 * @return the spriteMap
	 */
	public Object2ObjectOpenHashMap<String, Sprite> getSpriteMap() {
		return spriteMap;
	}

	public void loadImage(String path, String name) throws IOException {
		loadImage(path, name, false);
	}

	/**
	 * Adds the image.
	 *
	 * @param path        the path
	 * @param description the description
	 * @param gl          the gl
	 * @param glu         the glu
	 * @throws IOException
	 */
	public void loadImage(String path, String name, boolean forceNonompressed) throws IOException {

		Texture tex;
		long t = System.currentTimeMillis();
		tex = Controller.getTexLoader().getTexture2D(path, !name.contains("-gui-"), !forceNonompressed && !name.contains("-gui-"));
		long took = System.currentTimeMillis()-t;
		Sprite spr = new Sprite(tex);
		spr.setName(name);
		//			System.err.println("SPRITE: "+spr.getMaterial().getTexture().getTextureId());
		spr.setPositionCenter(name.contains("-c-"));
		Matcher m = multiTexturePathPattern.matcher(path);
		boolean b = m.matches();
		if (b) {
			// part, and group 3 the rest
			String[] split = m.group(2).split("x");
			int x = Integer.parseInt(split[0]);
			int y = Integer.parseInt(split[1]);
			int size = x * y;
			spr.setMultiSpriteMax(x, y);
			spr.setWidth(tex.getWidth() / x);
			spr.setHeight(tex.getHeight() / y);
		}
		spr.onInit();
		//INSERTED CODE
		SpriteLoadEvent event = new SpriteLoadEvent(path, name, tex, spr);
		StarLoader.fireEvent(event, false);
		///
		spriteMap.put(name, spr);
		long time = (System.currentTimeMillis() - t);
		if (time > 300) {
			System.err.println("[WARNING] initializing Texture " + path + " took " + time + " ms");
		}

	}

	//	/**
	//	 * Adds the image.
	//	 *
	//	 * @param type the type
	//	 * @param description the description
	//	 * @param filename the filename
	//	 * @param path the path
	//	 * @param gl the gl
	//	 * @param glu the glu
	//	 */
	//	@SuppressWarnings("unused")
	//	private void loadImage(String type, String name, String filename,
	//			String path ) {
	//		try {
	//			File im = getSpriteFile(path, filename);
	//
	//			Texture tex = Controller.getTexLoader().getTexture2D( im
	//					.getCanonicalPath(), true);
	//			Sprite spr = new Sprite(tex);
	//			spriteMap.put(name, spr);
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		}
	//	}
}
