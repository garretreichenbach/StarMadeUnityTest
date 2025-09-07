package org.schema.game.client.view.tools;

import api.utils.textures.StarLoaderTexture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class ImageUtils {

	protected static final long TIMEOUT = 1500;
	protected static final long WARNING = 1000;

	protected static final LinkedList<String> imageQueue = new LinkedList<>();
	protected static boolean isBusy;

	public static boolean isValidImageURL(String url) {
		return url != null && !url.isEmpty() && url.trim().startsWith("http") && url.trim().endsWith(".png");
	}

	public static void getImageFromURL(String image, ImageDownloadCallback imageDownloadCallback) {
		if(isValidImageURL(image)) {
			if(isBusy) imageQueue.add(image);
			else {
				isBusy = true;
				StarLoaderTexture.runOnGraphicsThread(new ImageDownloadRunnable(image, imageDownloadCallback));
			}
		}
	}

	public static boolean isBusy() {
		return isBusy;
	}

	public interface ImageDownloadCallback {

		void onFinished(BufferedImage downloaded);
	}

	public static class ImageDownloadRunnable implements Runnable {

		private final String image;
		private final ImageDownloadCallback imageDownloadCallback;

		public ImageDownloadRunnable(String image, ImageDownloadCallback imageDownloadCallback) {
			this.image = image;
			this.imageDownloadCallback = imageDownloadCallback;
		}

		@Override
		public void run() {
			try {
				BufferedImage image = fromURL(this.image);
				imageDownloadCallback.onFinished(image);
			} catch(Exception exception) {
				exception.printStackTrace();
				System.err.println("Failed to download image from " + image);
				imageDownloadCallback.onFinished(null);
			}
			if(!imageQueue.isEmpty()) {
				String nextImage = imageQueue.removeFirst();
				getImageFromURL(nextImage, imageDownloadCallback);
			} else isBusy = false;
		}

		private static BufferedImage fromURL(String u) {
			BufferedImage image;
			long started = System.currentTimeMillis();
			try {
				URL url = new URL(u);
				URLConnection urlConnection = url.openConnection();
				urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
				InputStream stream = urlConnection.getInputStream();
				image = ImageIO.read(stream);
				stream.close();
				long time = System.currentTimeMillis() - started;
				if(time > WARNING) System.out.println("Image download took " + time + "ms");
				return image;
			} catch(Exception exception) {
				exception.printStackTrace();
			}
			return null;
		}
	}
}
