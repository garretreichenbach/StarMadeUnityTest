/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>ImageUtil</H2>
 * <H3>org.schema.common.util.image</H3>
 * ImageUtil.java
 * <HR>
 * Description goes here. If you see this message, please contact me and the
 * description will be filled.<BR>
 * <BR>
 *
 * @author Robin Promesberger (schema)
 * @mail <A HREF="mailto:schemaxx@gmail.com">schemaxx@gmail.com</A>
 * @site <A
 * HREF="http://www.the-schema.com/">http://www.the-schema.com/</A>
 * @project JnJ / VIR / Project R
 * @homepage <A
 * HREF="http://www.the-schema.com/JnJ">
 * http://www.the-schema.com/JnJ</A>
 * @copyright Copyright � 2004-2010 Robin Promesberger (schema)
 * @licence Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.schema.common.util.image;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;

import org.schema.common.FastMath;
import org.schema.common.util.data.DataUtil;
import org.schema.schine.resource.FileExt;

/**
 * The Class ImageUtil.
 */
public class ImageUtil {

	/**
	 * Creates the buffered image.
	 *
	 * @param path the path
	 * @return the buffered image
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static BufferedImage createBufferedImage(String path) throws IOException {
		File imageFile = new FileExt(DataUtil.dataPath + path);
		BufferedImage bi;
		if (!imageFile.exists()) {
			throw new FileNotFoundException("-- ERROR(ImageUtil): Picture not found: " + imageFile.getAbsolutePath());
		}
		try {
			bi = ImageIO.read(imageFile);
		} catch (Exception e) {
			ImageIcon ii = createImageIcon(path);
			bi = new BufferedImage(ii.getIconWidth(), ii.getIconHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = (Graphics2D) bi.getGraphics();
			g2.drawImage(ii.getImage(), 0, 0, null);
			g2.dispose();
			throw new FileNotFoundException("-- Warning: ImageIO threw exception while reading " + path + ", using alterantive version succeeded");
		}

		return bi;
	}

	/**
	 * Creates the image icon.
	 *
	 * @param path the path
	 * @return the image icon
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static ImageIcon createImageIcon(String path) throws IOException {
		File imageFile = new FileExt(DataUtil.dataPath + path);
		if (!imageFile.exists()) {
			throw new FileNotFoundException("-- ERROR(ImageUtil): Picture not found: " + imageFile.getAbsolutePath());
		}
		return new ImageIcon(imageFile.getAbsolutePath());
	}

	/**
	 * Gets the buffered images from dir.
	 *
	 * @param description the description
	 * @param source      the source
	 * @param picnumber   the picnumber
	 * @param format      the format
	 * @return the buffered images from dir
	 */
	public static BufferedImage[] getBufferedImagesFromDir(String name, String source, int picnumber, String format) {
		BufferedImage[] pic = new BufferedImage[picnumber + 1];
		//        MainMenu.println(this,"-- Blast pic number: "+picnumber);
		DecimalFormat df = new DecimalFormat("000");

		for (int i = 0; i <= picnumber; i++) {
			try {
				pic[i] = createBufferedImage(source + name + " " + df.format(i) + "." + format);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (pic[i] == null) {
				throw new IndexOutOfBoundsException("!! ERROR(ImageUtil) while requesting picture #" + i + " of maximal " + picnumber);
			}
		}

		return pic;
	}

	public static Dimension getImageDimension(File file) throws IOException {
		return ImageUtil.getImageDimension(new FileInputStream(file));
	}

	public static Dimension getImageDimension(InputStream in) throws IOException {
		DataInputStream dis = new DataInputStream(in);

		try {
			int header = dis.readUnsignedShort();

			if (header == 0x8950) {
				// PNG
				dis.readFully(new byte[(8 - 2) + 4 + 4]); // thanks Abuse

				return new Dimension(dis.readInt(), dis.readInt());
			}

			if (header == 0xffd8) {
				// JPG (see below)
			} else if (header == 0x424D) {
				// BMP
				dis.readFully(new byte[16]);

				int w = dis.read() | (dis.read() << 8) | (dis.read() << 16) | (dis.read() << 24);
				int h = dis.read() | (dis.read() << 8) | (dis.read() << 16) | (dis.read() << 24);
				return new Dimension(w, h);
			} else if (header == (('G' << 8) | ('I' << 0))) // GIF
			{
				// GIF
				dis.readFully(new byte[4]);
				int w = dis.read() | (dis.read() << 8);
				int h = dis.read() | (dis.read() << 8);
				return new Dimension(w, h);
			} else {
				throw new IllegalStateException("unexpected header: " + Integer.toHexString(header));
			}

			while (true) // JPG is not so straight forward
			{
				int marker = dis.readUnsignedShort();

				switch (marker) {
					case 0xffd8: // SOI
					case 0xffd0: // RST0
					case 0xffd1: // RST1
					case 0xffd2: // RST2
					case 0xffd3: // RST3
					case 0xffd4: // RST4
					case 0xffd5: // RST5
					case 0xffd6: // RST6
					case 0xffd7: // RST7
					case 0xffd9: // EOI
						break;

					case 0xffdd: // DRI
						dis.readUnsignedShort();
						break;

					case 0xffe0: // APP0
					case 0xffe1: // APP1
					case 0xffe2: // APP2
					case 0xffe3: // APP3
					case 0xffe4: // APP4
					case 0xffe5: // APP5
					case 0xffe6: // APP6
					case 0xffe7: // APP7
					case 0xffe8: // APP8
					case 0xffe9: // APP9
					case 0xffea: // APPa
					case 0xffeb: // APPb
					case 0xffec: // APPc
					case 0xffed: // APPd
					case 0xffee: // APPe
					case 0xffef: // APPf
					case 0xfffe: // COM
					case 0xffdb: // DQT
					case 0xffc4: // DHT
					case 0xffda: // SOS
						dis.readFully(new byte[dis.readUnsignedShort() - 2]);
						break;

					case 0xffc0: // SOF0
					case 0xffc2: // SOF2
						dis.readUnsignedShort();
						dis.readByte();
						int height = dis.readUnsignedShort();
						int width = dis.readUnsignedShort();
						return new Dimension(width, height);

					default:
						throw new IllegalStateException("invalid jpg marker: " + Integer.toHexString(marker));
				}
			}
		} finally {
			dis.close();
		}
	}

	/**
	 * retrieves the appropriate pictures from the data directory.
	 *
	 * @param description Name of the unit
	 * @param source      directory in data where the images are
	 * @param picnumber   number of pictures in the file
	 * @param format      file format of picture
	 * @return the image icons from dir
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static ImageIcon[] getImageIconsFromDir(String name, String source, int picnumber, String format) throws IOException {
		ImageIcon[] pic = new ImageIcon[picnumber + 1];
		//        MainMenu.println(this,"-- Blast pic number: "+picnumber);
		DecimalFormat df = new DecimalFormat("000");

		for (int i = 0; i <= picnumber; i++) {
			pic[i] = createImageIcon(source + name + " " + df.format(i) + "." + format);
			if (pic[i] == null) {
				throw new IndexOutOfBoundsException("!! ERROR(ImageUtil) while requesting picture #" + i + " of maximal " + picnumber);
			}
		}

		return pic;
	}

	public static ImageAnalysisResult analyzeImage(final File file)
			throws NoSuchAlgorithmException, IOException {
		final ImageAnalysisResult result = new ImageAnalysisResult();

		final InputStream is = new BufferedInputStream(new FileInputStream(file));
		try {
			final ImageInputStream imageInputStream = ImageIO
					.createImageInputStream(is);
			final Iterator<ImageReader> imageReaders = ImageIO
					.getImageReaders(imageInputStream);
			if (!imageReaders.hasNext()) {
				System.err.println("ERROR: NO IMAGE READERS");
				assert (false);
				result.setImage(false);
				return result;
			}
			final ImageReader imageReader = imageReaders.next();
			imageReader.setInput(imageInputStream);
			final BufferedImage image = imageReader.read(0);
			if (image == null) {
				assert (false) : imageReader + "; " + file.getAbsolutePath();
				return result;
			}
			image.flush();

			if (imageReader.getFormatName().equals("JPEG")) {
				imageInputStream.seek(imageInputStream.getStreamPosition() - 2);
				final byte[] lastTwoBytes = new byte[2];
				imageInputStream.read(lastTwoBytes);
				if (lastTwoBytes[0] != 0xff && lastTwoBytes[1] != 0xd9) {
					result.setTruncated(true);
				} else {
					result.setTruncated(false);
					assert (false);
				}
			}
			boolean isPowerOfTwo = FastMath.isPowerOfTwo(image.getWidth())
					&& FastMath.isPowerOfTwo(image.getHeight());

			result.setPowerOfTwo(isPowerOfTwo);

			result.setImage(true);
		} catch (final IndexOutOfBoundsException e) {
			result.setTruncated(true);
		} catch (final IOException e) {
			if (e.getCause() instanceof EOFException) {
				result.setTruncated(true);
			}
		} finally {
			is.close();
		}
		return result;
	}
}
