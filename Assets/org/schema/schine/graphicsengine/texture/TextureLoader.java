package org.schema.schine.graphicsengine.texture;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.ARBTextureCompression;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;
import org.schema.common.FastMath;
import org.schema.common.util.ByteUtil;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.GraphicsContext;
import org.schema.schine.graphicsengine.core.ResourceException;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.texture.image.LoadableImageData;
import org.schema.schine.graphicsengine.texture.image.PNGImageData;
import org.schema.schine.resource.FileExt;
import org.schema.schine.resource.ResourceLoader;

/**
 * A utility class to load textures for JOGL. This source is based
 * on a texture that can be found in the Java Gaming (www.javagaming.org)
 * Wiki. It has been simplified slightly for explicit 2D graphics use.
 * <p/>
 * OpenGL uses a particular image format. Since the images that are
 * loaded from disk may not match this format this loader introduces
 * a intermediate image which the source image is copied into. In turn,
 * this image is used as source for the OpenGL texture.
 *
 * @author Kevin Glass
 * @author Brian Matzon
 */
public class TextureLoader {
	public static final boolean MIPMAP_NORMAL = EngineSettings.G_TEXTURE_MIPMAP.isOn();
	public static final boolean MIPMAP_ARRAY = EngineSettings.G_TEXTURE_ARRAY_MIPMAP.isOn();
	public static final int MIPMAP_LVLS = EngineSettings.G_MIPMAP_LEVEL_MAX.getInt();
	public static final boolean COMPRESSED_ARRAYS = EngineSettings.G_TEXTURE_ARRAY_COMPRESSION.isOn();
	public static final boolean COMPRESSED_REGULAR = EngineSettings.G_TEXTURE_COMPRESSION_BLOCKS.isOn();
	public static final boolean COMPRESSED_SHADOW = EngineSettings.G_TEXTURE_COMPRESSION_BLOCKS.isOn();
	public static final boolean COMPRESSED_FBO = EngineSettings.G_TEXTURE_COMPRESSION_BLOCKS.isOn();
	/**
	 * The table of textures that have been loaded in this loader
	 */
	private HashMap<String, Texture> table = new HashMap<String, Texture>();
	/**
	 * The colour model including alpha for the GL image
	 */
	private static ColorModel glAlphaColorModel= new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
			new int[]{8, 8, 8, 8},
			true,
			false,
			Transparency.TRANSLUCENT,
			DataBuffer.TYPE_BYTE);
	/**
	 * The colour model for the GL image
	 */
	private static ColorModel glColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
			new int[]{8, 8, 8, 0},
			false,
			false,
			Transparency.OPAQUE,
			DataBuffer.TYPE_BYTE);

	/**
	 * Create a new texture loader based on the game panel
	 *
	 * @param gl The GL content in which the textures should be loaded
	 */
	public TextureLoader() {
	}

	public static final int createFringeMap() {
		ByteBuffer data = GlUtil.getDynamicByteBuffer(256 * 3, 0);

		// these lambdas are in 100's of nm,
		//  they represent the wavelengths of light for each respective
		//  color channel.  They are only approximate so that the texture
		//  can repeat.

		float lamR = 6;  // 600 nm
		float lamG = 5;  // 500 nm, should be more like 550
		float lamB = 4;  // 400 nm. should be more like 440

		// these offsets are used to perturb the phase of the interference
		//   if you are using very thick "thin films" you will want to
		//   modify these offests to avoid complete contructive interference
		//   at a particular depth.. Just a tweak able.
		float offsetR = 0;
		float offsetG = 0;
		float offsetB = 0;

		// p is the period of the texture, it is the LCM of the wavelengths,
		//  this is the depth in nm when the pattern will repeat.  I was too
		//  lazy to write up a LCM function, so you have to provide it.
		float p = 60;   //lcm(6,5,4)

		// vd is the depth of the thin film relative to the texture index
		float vd = 1f / 256.0f * p;

		// now compute the color values using this formula:
		//  1/2 ( Sin( 2Pi * d/lam* + Pi/2 + O) + 1 )
		//   where d is the current depth, or "i*vd" and O is some offset* so that
		//   we avoid complete constructive interference in all wavelenths at some depth.
		float pi = 3.1415926535f;
		for (int i = 0; i < 256; ++i) {
			data.put(i * 3 + 0, (byte) ((.5f * (FastMath.sin(2 * pi * (i * vd) / lamR + pi / 2.0f + offsetR) + 1)) * 255));
			data.put(i * 3 + 1, (byte) ((.5f * (FastMath.sin(2 * pi * (i * vd) / lamG + pi / 2.0f + offsetG) + 1)) * 255));
			data.put(i * 3 + 2, (byte) ((.5f * (FastMath.sin(2 * pi * (i * vd) / lamB + pi / 2.0f + offsetB) + 1)) * 255));
		}

		// Now just load the texture, I use mipmapping since the depth may change very
		// fast in places.

		IntBuffer idBuffer = MemoryUtil.memAllocInt(1);
		idBuffer.rewind();
		GL11.glGenTextures(idBuffer);

		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, idBuffer.get(0));
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
		if (MIPMAP_NORMAL) {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_GENERATE_MIPMAP, GL11.GL_TRUE);
		}

		GL11.glTexImage2D(GL11.GL_TEXTURE_2D,
				0,
				GL11.GL_RGB8,
				256,
				1,
				0,
				GL11.GL_RGB,
				GL11.GL_UNSIGNED_BYTE,
				data);

		return idBuffer.get(0);
	}

	public static Texture get1DTexture(int width, ByteBuffer data) {
		data.rewind();
		GlUtil.glEnable(GL11.GL_TEXTURE_1D);
		// Create An Empty TextureNew
		// Create Storage Space For TextureNew Data (128x128x4)

		IntBuffer iBuffer = MemoryUtil.memAllocInt(1);
		GL11.glGenTextures(iBuffer); // Create 1 TextureNew
		GlUtil.glBindTexture(GL11.GL_TEXTURE_1D, iBuffer.get(0)); // Bind The TextureNew

		// Build TextureNew Using Information In data
		GL11.glTexParameteri(GL11.GL_TEXTURE_1D, GL11.GL_TEXTURE_MIN_FILTER,
				GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_1D, GL11.GL_TEXTURE_MAG_FILTER,
				GL11.GL_LINEAR);
		GL11.glTexImage1D(GL11.GL_TEXTURE_1D, 0, GL11.GL_RGBA, width, 0, GL11.GL_RGBA,
				GL11.GL_UNSIGNED_BYTE, data);

		GlUtil.glDisable(GL11.GL_TEXTURE_1D);
		return new Texture(GL11.GL_TEXTURE_1D, iBuffer.get(0), "1dTexture"); // Return The TextureNew ID
	}

	/**
	 * Gets the empty texture.
	 *
	 * @param gl     the gl
	 * @param width  the width
	 * @param height the height
	 * @return the empty texture
	 */
	public static Texture getEmptyTexture(int width, int height) {
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		// Create An Empty TextureNew
		// Create Storage Space For TextureNew Data (128x128x4)
		ByteBuffer data = GlUtil.getDynamicByteBuffer(width * height * ByteUtil.SIZEOF_INT, 0);
		data.limit(data.capacity());

		IntBuffer iBuffer = MemoryUtil.memAllocInt(1);
		GL11.glGenTextures(iBuffer); // Create 1 TextureNew
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, iBuffer.get(0)); // Bind The TextureNew
		Controller.loadedTextures.add(iBuffer.get(0));
		// Build TextureNew Using Information In data
		if (!MIPMAP_NORMAL) {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
					GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
					GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		} else {
//			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, MIPMAP_LVLS);
			GlUtil.printGlErrorCritical();
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
			GlUtil.printGlErrorCritical();
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GlUtil.printGlErrorCritical();
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_GENERATE_MIPMAP, GL11.GL_TRUE);
		}
		//		if(!MIPMAP){
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA,
				GL11.GL_UNSIGNED_BYTE, data);
		//		}else{
		//			GLU.gluBuild2DMipmaps(GL11.GL_TEXTURE_2D, 3, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data);
		//		}
		GlUtil.printGlErrorCritical();

		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		return new Texture(GL11.GL_TEXTURE_2D, iBuffer.get(0), "emptyTexture"); // Return The TextureNew ID
	}

	/**
	 * Convert the buffered image to a texture
	 *
	 * @param bufferedImage The image to convert to a texture
	 * @return A buffer containing the data
	 */
	private static ByteBuffer getImageData(BufferedImage bufferedImage) {
		ByteBuffer imageBuffer = null;

		// build a byte buffer from the temporary image

		// that be used by OpenGL to produce a texture.

		byte[] data = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
		imageBuffer = GlUtil.getDynamicByteBuffer(data.length, 0);
		imageBuffer.order(ByteOrder.nativeOrder());

		imageBuffer.put(data, 0, data.length);
		imageBuffer.flip();

		return imageBuffer;
	}

	/**
	 * Checks if is power of two.
	 *
	 * @param num the num
	 * @return true, if is power of two
	 */
	public static boolean isPowerOfTwo(int num) {
		int i = 1;
		while (i < num) {
			i *= 2;
		}
		return i == num;
	}

	/**
	 * Create a new texture ID
	 *
	 * @return A new texture ID
	 */
	private static int createTextureID() {

		GL11.glGenTextures(GlUtil.getIntBuffer1());
		Controller.loadedTextures.add(GlUtil.getIntBuffer1().get(0));
		return GlUtil.getIntBuffer1().get(0);
	}

	public static int getTextureArray(
			String resourceName[],
			int dstPixelFormat,
			int minFilter,
			int magFilter, boolean mipMap) throws IOException, ResourceException {
		int textureID = createTextureID();
		int target = GL30.GL_TEXTURE_2D_ARRAY;
		GlUtil.glBindTexture(target, textureID);
		PNGImageData data[] = new PNGImageData[resourceName.length];
		//			System.out.println("Loading PNG: " + resourceName);
		int size = 0;
		for (int i = 0; i < data.length; i++) {
			data[i] = new PNGImageData();
			InputStream resourceAsInputStream = ResourceLoader.resourceUtil
					.getResourceAsInputStream(resourceName[i]);

			size += data[i].getSizeBef(resourceAsInputStream);

			resourceAsInputStream.close();
		}

		ByteBuffer buffer = GlUtil.getDynamicByteBuffer(size, 0);
		buffer.rewind();
		int lastDepth = -1;
		String before = "";
		for (int i = 0; i < data.length; i++) {
			InputStream resourceAsInputStream = ResourceLoader.resourceUtil
					.getResourceAsInputStream(resourceName[i]);

			data[i].loadImage(resourceAsInputStream);
			if (lastDepth > 0 && lastDepth != data[i].getDepth()) {
				throw new ResourceException(resourceName[i] + " has a different color depth as another file " + before + " in the same array: \nother: " + lastDepth + "; this: " + data[i].getDepth() + "\n\nAll textures of this series must either be all RGB or all RGBA, and have the same resolution");
			}
			before = resourceName[i];
			lastDepth = data[i].getDepth();
			buffer.put(data[i].getImageBufferData());

			resourceAsInputStream.close();
		}
		buffer.flip();

		if (!MIPMAP_ARRAY || !mipMap) {
			GL11.glTexParameteri(target, GL11.GL_TEXTURE_MIN_FILTER,
					minFilter);
			GL11.glTexParameteri(target, GL11.GL_TEXTURE_MAG_FILTER,
					EngineSettings.G_MAG_FILTER_LINEAR_GUI.isOn() ? GL11.GL_LINEAR : GL11.GL_NEAREST);

			GL11.glTexParameteri(GL12.GL_TEXTURE_3D,
					GL12.GL_TEXTURE_MAX_LEVEL, 0);
			GL11.glTexParameteri(GL12.GL_TEXTURE_3D,
					GL14.GL_GENERATE_MIPMAP, GL11.GL_FALSE);
			
		} else {
			// HERE
			GL11.glTexParameteri(target,
					GL12.GL_TEXTURE_MAX_LEVEL, MIPMAP_LVLS);

			GL11.glTexParameteri(target,
					GL11.GL_TEXTURE_MIN_FILTER,
					GL11.GL_LINEAR_MIPMAP_LINEAR);

			/*
	         * The magnifaction filter GL_TEXTURE_MAG_FILTER doesn't
			 * support mip-mapping, as this just has no meaning for
			 * texture magnification. It only supports GL_NEAREST and
			 * GL_LINEAR.
			 */

			GL11.glTexParameteri(target,
					GL11.GL_TEXTURE_MAG_FILTER,
					hasLinMagForBlock(null) ? GL11.GL_LINEAR : GL11.GL_NEAREST);

//			if(GraphicsContext.current.getCapabilities().OpenGL30){
//				GL30.glGenerateMipmap(target);
//			}else{

			GL11.glTexParameteri(target,
					GL14.GL_GENERATE_MIPMAP, GL11.GL_TRUE);
//			}

		}
		int srcPixelFormat;
		if (data[0].getDepth() == 32) {
			srcPixelFormat = GL11.GL_RGBA;
			dstPixelFormat = GL11.GL_RGBA;
			if (COMPRESSED_ARRAYS && GraphicsContext.current.getCapabilities().GL_ARB_texture_compression) {
				System.err.println("Using texture array compression");
				dstPixelFormat = ARBTextureCompression.GL_COMPRESSED_RGBA_ARB;
			} else if (COMPRESSED_ARRAYS) {
				System.err.println("ERROR: Texture compression not supported by hardware");
			}
		} else {
			//24
			srcPixelFormat = GL11.GL_RGB;
			dstPixelFormat = GL11.GL_RGB;
			if (COMPRESSED_ARRAYS && GraphicsContext.current.getCapabilities().GL_ARB_texture_compression) {
				System.err.println("Using texture array compression");
				dstPixelFormat = ARBTextureCompression.GL_COMPRESSED_RGB_ARB;
			} else if (COMPRESSED_ARRAYS) {
				System.err.println("ERROR: Texture compression not supported by hardware");
			}
		}

		GlUtil.printGlErrorCritical();
		// produce a texture from the byte buffer
		//			long t = System.currentTimeMillis();
		try {
			GL12.glTexImage3D(target, 0, dstPixelFormat, data[0].getWidth(),
					data[0].getHeight(), data.length, 0,
					srcPixelFormat, GL11.GL_UNSIGNED_BYTE, buffer);
		} catch (RuntimeException e) {
			System.err.println("EXCEPTION HAPPENED WITH RESOURCE: " + Arrays.toString(resourceName));
			throw e;
		}
		if (COMPRESSED_ARRAYS) {
			int compressed = GL11.glGetTexLevelParameteri(target, 0, ARBTextureCompression.GL_TEXTURE_COMPRESSED_ARB);
            /* if the compression has been successful */
			if (compressed == GL11.GL_TRUE) {

			} else {
				System.err.println("Exception: Texture Compression failed: " + Arrays.toString(resourceName));
				assert (false);
			}
		}
		if (!MIPMAP_ARRAY || !mipMap) {

		} else {
//			GL30.glGenerateMipmap(target);
		}
		//			System.err.println("[TEXTURE] loaded: "+path+": "+(System.currentTimeMillis()-t)+" ms; mipmap: "+mipMap);

		GlUtil.printGlErrorCritical();

		return textureID;

	}

	public static int getTextureArrayAdv(
			String resourceName[],
			int dstPixelFormat,
			int minFilter,
			int magFilter, boolean mipMap) throws IOException, ResourceException {
		int textureID = createTextureID();
		int target = GL30.GL_TEXTURE_2D_ARRAY;
		GlUtil.glBindTexture(target, textureID);

		if (!MIPMAP_ARRAY || !mipMap) {
			GL11.glTexParameteri(target, GL11.GL_TEXTURE_MIN_FILTER,
					minFilter);
			GL11.glTexParameteri(target, GL11.GL_TEXTURE_MAG_FILTER,
					EngineSettings.G_MAG_FILTER_LINEAR_GUI.isOn() ? GL11.GL_LINEAR : GL11.GL_NEAREST);

			GL11.glTexParameteri(GL12.GL_TEXTURE_3D,
					GL12.GL_TEXTURE_MAX_LEVEL, 0);
			GL11.glTexParameteri(GL12.GL_TEXTURE_3D,
					GL14.GL_GENERATE_MIPMAP, GL11.GL_FALSE);

		} else {
			// HERE
			GL11.glTexParameteri(target,
					GL12.GL_TEXTURE_MAX_LEVEL, MIPMAP_LVLS);

			GL11.glTexParameteri(target,
					GL11.GL_TEXTURE_MIN_FILTER,
					GL11.GL_LINEAR_MIPMAP_LINEAR);

			GL11.glTexParameteri(target,
					GL11.GL_TEXTURE_MAG_FILTER, hasLinMagForBlock(null) ? GL11.GL_LINEAR : GL11.GL_NEAREST);

			GL11.glTexParameteri(target,
					GL14.GL_GENERATE_MIPMAP, GL11.GL_FALSE);

		}

		PNGImageData data[] = new PNGImageData[resourceName.length];
		//			System.out.println("Loading PNG: " + resourceName);
		int size = 0;
		int lastDepth = -1;
		String before = "";
		boolean created = false;
		for (int i = 0; i < data.length; i++) {
			InputStream resourceAsInputStream = ResourceLoader.resourceUtil
					.getResourceAsInputStream(resourceName[i]);
			data[i] = new PNGImageData();
			data[i].loadImage(resourceAsInputStream);
			if (lastDepth > 0 && lastDepth != data[i].getDepth()) {
				throw new ResourceException(resourceName[i] + " has a different color depth as another file " + before + " in the same array: \nother: " + lastDepth + "; this: " + data[i].getDepth() + "\n\nAll textures of this series must either be all RGB or all RGBA, and have the same resolution");
			}
			before = resourceName[i];
			lastDepth = data[i].getDepth();

			boolean compressed = COMPRESSED_ARRAYS && GraphicsContext.current.getCapabilities().GL_ARB_texture_compression;

			int srcPixelFormat;
			if (data[0].getDepth() == 32) {
				srcPixelFormat = GL11.GL_RGBA;
				dstPixelFormat = GL11.GL_RGBA;
				if (compressed) {
					System.err.println("Using texture array compression");
					dstPixelFormat = ARBTextureCompression.GL_COMPRESSED_RGBA_ARB;
				} else if (COMPRESSED_ARRAYS) {
					System.err.println("ERROR: Texture compression not supported by hardware");
				}
			} else {
				//24
				srcPixelFormat = GL11.GL_RGB;
				dstPixelFormat = GL11.GL_RGB;
				if (compressed) {
					System.err.println("Using texture array compression");
					dstPixelFormat = ARBTextureCompression.GL_COMPRESSED_RGB_ARB;
				} else if (COMPRESSED_ARRAYS) {
					System.err.println("ERROR: Texture compression not supported by hardware");
				}
			}

			GlUtil.printGlErrorCritical();
			if (!created) {
				if (compressed) {

					GL13.glCompressedTexImage3D(target, 0, dstPixelFormat, data[0].getWidth(),
							data[0].getHeight(), data.length, 0, (ByteBuffer) null);
				} else {
					GL12.glTexImage3D(target, 0, dstPixelFormat, data[0].getWidth(),
							data[0].getHeight(), data.length, 0,
							srcPixelFormat, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
				}
				created = true;
			}
			try {
				int xOffest = 0;
				int yOffest = 0;
				int zOffest = i;
				if (compressed) {
					GL12.glTexSubImage3D(target, 0, xOffest, yOffest, zOffest, data[i].getWidth(), data[i].getHeight(), 1, dstPixelFormat, GL11.GL_UNSIGNED_BYTE, data[i].getImageBufferData());
				} else {
					GL13.glCompressedTexSubImage3D(target, 0, xOffest, yOffest, zOffest, data[i].getWidth(), data[i].getHeight(), 1, GL11.GL_UNSIGNED_BYTE, data[i].getImageBufferData());
//				Image3D(target, 0, dstPixelFormat, data[0].getWidth(),
//						data[0].getHeight(), data.length, 0,
//						srcPixelFormat, GL11.GL_UNSIGNED_BYTE, buffer);
				}
			} catch (RuntimeException e) {
				System.err.println("EXCEPTION HAPPENED WITH RESOURCE: " + Arrays.toString(resourceName));
				throw e;
			}
			GlUtil.printGlErrorCritical();

//			buffer.put(data[i].getImageBufferData());

			resourceAsInputStream.close();
		}
//		buffer.flip();

		GlUtil.printGlErrorCritical();
		// produce a texture from the byte buffer
		//			long t = System.currentTimeMillis();

		if (COMPRESSED_ARRAYS) {
			int compressed = GL11.glGetTexLevelParameteri(target, 0, ARBTextureCompression.GL_TEXTURE_COMPRESSED_ARB);
            /* if the compression has been successful */
			if (compressed == GL11.GL_TRUE) {

			} else {
				System.err.println("Exception: Texture Compression failed: " + Arrays.toString(resourceName));
				assert (false);
			}
		}
		if (!MIPMAP_ARRAY || !mipMap) {

		} else {
//			GL30.glGenerateMipmap(target);
		}
		//			System.err.println("[TEXTURE] loaded: "+path+": "+(System.currentTimeMillis()-t)+" ms; mipmap: "+mipMap);

		GlUtil.printGlErrorCritical();

		return textureID;

	}

	private static BufferedImage convertBufferedImage(BufferedImage bufferedImage) {
		int texWidth = 2;
		int texHeight = 2;
		WritableRaster raster;
		BufferedImage texImage;
		// find the closest power of 2 for the width and height

		// of the produced texture

		while (texWidth < bufferedImage.getWidth()) {
			texWidth *= 2;
		}
		while (texHeight < bufferedImage.getHeight()) {
			texHeight *= 2;
		}

		//	       System.err.println("setting texture size: "+texWidth+", "+texHeight);

		// create a raster that can be used by OpenGL as a source

		// for a texture

		if (bufferedImage.getColorModel().hasAlpha()) {
			raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, texWidth, texHeight, 4, null);
			texImage = new BufferedImage(glAlphaColorModel, raster, false, new Hashtable());
		} else {
			raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, texWidth, texHeight, 3, null);
			texImage = new BufferedImage(glColorModel, raster, false, new Hashtable());
		}

		// copy the source image into the produced image

		Graphics g = texImage.getGraphics();
		g.setColor(new Color(0f, 0f, 0f, 0f));
		g.fillRect(0, 0, texWidth, texHeight);
		g.drawImage(bufferedImage, 0, 0, null);

		return texImage;
	}

	/**
	 * Creates an integer buffer to hold specified ints
	 * - strictly a utility method
	 *
	 * @param size how many int to contain
	 * @return created IntBuffer
	 */
	protected IntBuffer createIntBuffer(int size) {
		ByteBuffer temp = MemoryUtil.memAlloc(4 * size);
		temp.order(ByteOrder.nativeOrder());

		return temp.asIntBuffer();
	}

	/**
	 * Get the closest greater power of 2 to the fold number
	 *
	 * @param fold The target number
	 * @return The power of 2
	 */
	private static int get2Fold(int fold) {
		int ret = 2;
		while (ret < fold) {
			ret *= 2;
		}
		return ret;
	}

	public Texture getCubeMap(String resourceName, String imageExtension) throws IOException, ResourceException {
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glGenTextures(GlUtil.getIntBuffer1());
		int texId = GlUtil.getIntBuffer1().get(0);
		GlUtil.glEnable(GL13.GL_TEXTURE_CUBE_MAP);
		GlUtil.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texId);

		final String suffixes[] = new String[]{"posx", "negx", "posy",
				"negy", "posz", "negz"};

		int targets[] = new int[]{
				GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X,
				GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_X,
				GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Y,
				GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y,
				GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Z,
				GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z
		};

		Texture texture = new Texture(GL13.GL_TEXTURE_CUBE_MAP, texId, resourceName);
		for (int i = 0; i < 6; i++) {
			PNGImageData data = new PNGImageData();
			//			System.out.println("Loading PNG: " + resourceName);
			InputStream resourceAsInputStream;
			resourceAsInputStream = ResourceLoader.resourceUtil
					.getResourceAsInputStream(resourceName + "_" + suffixes[i] + "." + imageExtension);
			data.loadImage(resourceAsInputStream);
			resourceAsInputStream.close();

			texture.setOriginalWidth(data.getWidth());
			texture.setOriginalHeight(data.getHeight());

			ByteBuffer imageData = data.getImageBufferData();
			imageData.rewind();
			texture.setWidth(data.getWidth());
			texture.setHeight(data.getHeight());
			int mode;
			if (data.getDepth() == 32) {
				mode = GL11.GL_RGBA;
			} else {
				//24
				mode = GL11.GL_RGB;
			}
			int dstMode = mode;
			if (COMPRESSED_REGULAR) {
				if (dstMode == GL11.GL_RGB) {
//					System.err.println("Using texture compression on "+path);
					dstMode = ARBTextureCompression.GL_COMPRESSED_RGB_ARB;
				}
				if (dstMode == GL11.GL_RGBA) {
//					System.err.println("Using texture compression on "+path);
					dstMode = ARBTextureCompression.GL_COMPRESSED_RGBA_ARB;
				}
			}
			GL11.glTexImage2D(targets[i], 0, dstMode,
					data.getWidth(), data.getHeight(),
					0, mode, GL11.GL_UNSIGNED_BYTE, imageData);
		}
		// Typical cube map settings
		GL11.glTexParameterf(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER,
				GL11.GL_LINEAR);
		GL11.glTexParameterf(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER,
				GL11.GL_LINEAR);
		GL11.glTexParameterf(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S,
				GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameterf(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T,
				GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameterf(GL13.GL_TEXTURE_CUBE_MAP, GL12.GL_TEXTURE_WRAP_R,
				GL12.GL_CLAMP_TO_EDGE);

		GlUtil.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, 0);
		GlUtil.glDisable(GL13.GL_TEXTURE_CUBE_MAP);
		return texture;
	}

	public static Texture getTexture(BufferedImage bufferedImage, String resourceName,
	                          int target, int dstPixelFormat, int minFilter, int magFilter, boolean mipMap, boolean compress) {

		int srcPixelFormat = 0;
		int textureID = createTextureID();
		Texture texture = new Texture(target, textureID, resourceName);

//		boolean isPowerOfTwo = FastMath.isPowerOfTwo(bufferedImage.getWidth())
//				&& FastMath.isPowerOfTwo(bufferedImage.getHeight());
//		if (!isPowerOfTwo) {
//			// System.err.println("[TEXTURELOADER] WARNING: texture size was NOT power of two "+bufferedImage.getWidth()+", "+bufferedImage.getHeight()+
//			// ": "+resourceName);
//		}
		// convert that image into a byte buffer of texture data

		texture.setOriginalWidth((bufferedImage.getWidth()));
		texture.setOriginalHeight((bufferedImage.getHeight()));

		long t = System.currentTimeMillis();
		bufferedImage = convertBufferedImage(bufferedImage);
		long takenBuffConv = System.currentTimeMillis() - t;
		// bind this texture
		
		t = System.currentTimeMillis();
		ByteBuffer textureBuffer = getImageData(bufferedImage);
		textureBuffer.rewind();
		GlUtil.glBindTexture(target, textureID);
		assert (bufferedImage != null);
		assert (texture != null);
		long takenBuffData = System.currentTimeMillis() - t;
		texture.setWidth(bufferedImage.getWidth());
		texture.setHeight(bufferedImage.getHeight());
		// System.err.println("[TEXTURELOADER] set image sizes: "+bufferedImage.getWidth()+", "+bufferedImage.getHeight()+
		// " of "+resourceName);

		if (bufferedImage.getColorModel().hasAlpha()) {
			srcPixelFormat = GL11.GL_RGBA;
			dstPixelFormat = GL11.GL_RGBA;
		} else {
			srcPixelFormat = GL11.GL_RGB;
			dstPixelFormat = GL11.GL_RGB;
		}
		t = System.currentTimeMillis();
		make2DTexture(target, minFilter, magFilter, dstPixelFormat,
				bufferedImage.getWidth(), bufferedImage.getHeight(),
				srcPixelFormat, textureBuffer, mipMap, resourceName, compress);
		long mkTexture = System.currentTimeMillis() - t;
		GlUtil.printGlErrorCritical();
		
//		System.err.println("[TEXTURE] creation took: convert: "+takenBuffConv+"; data: "+takenBuffData+"; make: "+mkTexture);
		return texture;
	}

	public static Texture getTexture(LoadableImageData imageData, ByteBuffer bb, String resourceName,
	                          int target, int dstPixelFormat, int minFilter, int magFilter, boolean mipMap, boolean compressed) {

		int srcPixelFormat = 0;
		int textureID = createTextureID();

		//		System.err.println("CREATED TEX ID FOR "+resourceName+" -> "+textureID);
		Texture texture = new Texture(target, textureID, resourceName);
		boolean isPowerOfTwo = FastMath.isPowerOfTwo(imageData.getWidth())
				&& FastMath.isPowerOfTwo(imageData.getHeight());
		if (!isPowerOfTwo) {
			// System.err.println("[TEXTURELOADER] WARNING: texture size was NOT power of two "+bufferedImage.getWidth()+", "+bufferedImage.getHeight()+
			// ": "+resourceName);
			assert (false) : resourceName + ": " + imageData.getWidth() + "; " + imageData.getHeight();
		}
		// convert that image into a byte buffer of texture data

		texture.setOriginalWidth((imageData.getWidth()));
		texture.setOriginalHeight((imageData.getHeight()));

		// bind this texture
		ByteBuffer textureBuffer = bb;//imageData.getImageBufferData();
		textureBuffer.rewind();
		GlUtil.glBindTexture(target, textureID);
		assert (texture != null);

		texture.setWidth(imageData.getWidth());
		texture.setHeight(imageData.getHeight());
		// System.err.println("[TEXTURELOADER] set image sizes: "+bufferedImage.getWidth()+", "+bufferedImage.getHeight()+
		// " of "+resourceName);
		if (imageData.getDepth() == 32) {
			srcPixelFormat = GL11.GL_RGBA;
		} else {
			//24
			srcPixelFormat = GL11.GL_RGB;
		}

		make2DTexture(target, minFilter, magFilter, dstPixelFormat,
				imageData.getWidth(), imageData.getHeight(),
				srcPixelFormat, textureBuffer, mipMap, resourceName, compressed);

		GlUtil.printGlErrorCritical();
		return texture;
	}

	/**
	 * Load a texture
	 *
	 * @param resourceName The location of the resource to load
	 * @return The loaded texture
	 * @throws IOException Indicates a failure to access the resource
	 */
	public Texture getTexture2D(String resourceName, boolean mipMap) throws IOException {
		return getTexture2D(resourceName, mipMap, true);
	}

	/**
	 * Load a texture
	 *
	 * @param resourceName The location of the resource to load
	 * @return The loaded texture
	 * @throws IOException Indicates a failure to access the resource
	 */
	public Texture getTexture2D(String resourceName, boolean mipMap, boolean compress) throws IOException {
		Texture tex = table.get(resourceName);

		if (tex != null) {
			return tex;
		}

		try {
			tex = getTexture2D(resourceName,
					GL11.GL_TEXTURE_2D, // target

					GL11.GL_RGBA,     // dst pixel format

					GL11.GL_LINEAR, // min filter (unused)

					GL11.GL_LINEAR, false, false);
		} catch (ResourceException e) {

			e.printStackTrace();
		}

		return tex;
	}

	public Texture getTexture2DAnyFormat(String resourceName, int filter, boolean mipMap, boolean compress) throws IOException {
		File png = new FileExt(resourceName+".png");
		File tga = new FileExt(resourceName+".tga");
		
		
		if(tga.exists() && EngineSettings.USE_TGA_NORMAL_MAPS.isOn()){
			return getTexture2D(resourceName+".tga", filter, mipMap, compress);
		}else if(png.exists()){
			return getTexture2D(resourceName+".png", filter, mipMap, compress);
		}else{
			throw new FileNotFoundException("Neither .png or .tga found for resource "+resourceName);
		}
		
		
	}
	
	public Texture getTexture2D(String resourceName, int filter, boolean mipMap, boolean compress) throws IOException {
		Texture tex = table.get(resourceName);

		if (tex != null) {
			return tex;
		}

		try {
			tex = getTexture2D(resourceName,
					GL11.GL_TEXTURE_2D, // target

					GL11.GL_RGBA,     // dst pixel format

					filter, // min filter (unused)

					filter, mipMap, compress);
		} catch (ResourceException e) {

			e.printStackTrace();
		}

		return tex;
	}

	/**
	 * Load a texture into OpenGL from a image reference on
	 * disk.
	 *
	 * @param resourceName   The location of the resource to load
	 * @param target         The GL target to load the texture against
	 * @param dstPixelFormat The pixel format of the screen
	 * @param minFilter      The minimising filter
	 * @param magFilter      The magnification filter
	 * @return The loaded texture
	 * @throws IOException       Indicates a failure to access the resource
	 * @throws ResourceException
	 */
	public static Texture getTexture2D(String resourceName,
	                            int target,
	                            int dstPixelFormat,
	                            int minFilter,
	                            int magFilter, boolean mipMap, boolean compress) throws IOException, ResourceException {
//		System.err.println("CREATING TEX: "+resourceName+"; "+mipMap);
		// create the texture ID for this texture
		long takenByteBufferLoad = 0;
		long takenStreamLoad = 0;
		long takenTexCreate = 0;
		try{
		if (resourceName.toLowerCase(Locale.ENGLISH).endsWith(".tga")) {
//			TGAImageData data = new TGAImageData();
			//			System.out.println("Loading PNG: " + resourceName);
			long t = System.currentTimeMillis();
			InputStream resourceAsInputStream = ResourceLoader.resourceUtil
					.getResourceAsInputStream(resourceName);
			takenStreamLoad = System.currentTimeMillis() - t;
			t = System.currentTimeMillis();
			ByteBuffer bb = TGALoader.loadImage(resourceAsInputStream);
			resourceAsInputStream.close();
			takenByteBufferLoad = System.currentTimeMillis() - t;
			;
			LoadableImageData data = new LoadableImageData() {
				
				@Override
				public int getWidth() {
					return TGALoader.getLastWidth();
				}
				
				@Override
				public int getTexWidth() {
					return TGALoader.getLastTexWidth();
				}
				
				@Override
				public int getTexHeight() {
					return TGALoader.getLastTexHeight();
				}
				
				@Override
				public ByteBuffer getImageBufferData() {
					assert(false);
					return null;
				}
				
				@Override
				public int getHeight() {
					return TGALoader.getLastHeight();
				}
				
				@Override
				public int getDepth() {
					return TGALoader.getLastDepth();
				}
				
				@Override
				public ByteBuffer loadImage(InputStream arg0, boolean arg1, boolean arg2,
						int[] arg3) throws IOException {
					assert(false);
					return null;
				}
				
				@Override
				public ByteBuffer loadImage(InputStream arg0, boolean arg1, int[] arg2)
						throws IOException {
					assert(false);
					return null;
				}
				
				@Override
				public ByteBuffer loadImage(InputStream arg0) throws IOException {
					assert(false);
					return null;
				}
				
				@Override
				public void configureEdging(boolean arg0) {
					assert(false);
				}
			};
			
			
			t = System.currentTimeMillis();
			
			Texture tex = getTexture(data, bb, resourceName, target, dstPixelFormat,
					minFilter, magFilter, mipMap, compress);
			takenTexCreate = System.currentTimeMillis() - t;
			return tex;
		}
		
		if (resourceName.toLowerCase(Locale.ENGLISH).endsWith(".png")) {
			PNGImageData data = new PNGImageData();
			//			System.out.println("Loading PNG: " + resourceName);
			long t = System.currentTimeMillis();
			InputStream resourceAsInputStream = ResourceLoader.resourceUtil
					.getResourceAsInputStream(resourceName);
			takenStreamLoad = System.currentTimeMillis() - t;
			t = System.currentTimeMillis();
			
			ByteBuffer bb = data.loadImage(resourceAsInputStream);
			resourceAsInputStream.close();

			takenByteBufferLoad  = System.currentTimeMillis() - t;
			t = System.currentTimeMillis();
			Texture tex = getTexture(data, bb, resourceName, target, dstPixelFormat,
					minFilter, magFilter, mipMap, compress);
			
			
			takenTexCreate = System.currentTimeMillis()- t;
			return tex;
		}
		}finally{
//			System.err.println("[TEXTURE] "+resourceName+" LOAD TAKEN: stream: "+takenStreamLoad+" ms; data: "+takenByteBufferLoad+" ms; create: "+takenTexCreate+" ms");
		}
		BufferedImage bufferedImage = loadImage(resourceName);

		assert (bufferedImage != null) : "cannot load " + resourceName;

		Texture texture = getTexture(bufferedImage, resourceName, target, dstPixelFormat, minFilter, magFilter, mipMap, compress);

		return texture;

	}

	/**
	 * Load a given resource as a buffered image
	 *
	 * @param ref The location of the resource to load
	 * @return The loaded buffered image
	 * @throws IOException       Indicates a failure to find a resource
	 * @throws ResourceException
	 */
	private static BufferedImage loadImage(String ref) throws IOException, ResourceException {
		InputStream resourceAsInputStream = ResourceLoader.resourceUtil
				.getResourceAsInputStream(ref);

		BufferedInputStream bufferedInputStream = new BufferedInputStream(resourceAsInputStream);

		BufferedImage bufferedImage = ImageIO.read(bufferedInputStream);

		resourceAsInputStream.close();
		bufferedInputStream.close();
		return bufferedImage;
	}
	public static boolean hasLinMagForBlock(String path){
		return EngineSettings.G_MAG_FILTER_LINEAR_BLOCKS.isOn() || (path != null && path.contains("textures") && path.contains("block") && path.contains("256"));
	}
	private static void make2DTexture(int target, int minFilter, int magFilter,
	                           int dstPixelFormat, int width, int height, int srcPixelFormat,
	                           ByteBuffer textureBuffer, boolean mipMap, String path, boolean compress) {
		GlUtil.printGlErrorCritical();

		if (target == GL11.GL_TEXTURE_2D) {

			//			GL11.glTexParameterf(target, GL11.GL_TEXTURE_WRAP_S,
			//					GL12.GL_CLAMP_TO_EDGE);
			//			GL11.glTexParameterf(target, GL11.GL_TEXTURE_WRAP_T,
			//					GL12.GL_CLAMP_TO_EDGE);

			if (!MIPMAP_NORMAL || !mipMap) {

				GL11.glTexParameteri(target, GL11.GL_TEXTURE_MIN_FILTER,
						minFilter);
				GlUtil.printGlErrorCritical();

				GL11.glTexParameteri(target, GL11.GL_TEXTURE_MAG_FILTER,
						EngineSettings.G_MAG_FILTER_LINEAR_GUI.isOn() ? GL11.GL_LINEAR : GL11.GL_NEAREST);
				GlUtil.printGlErrorCritical();


				GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
						GL14.GL_GENERATE_MIPMAP, GL11.GL_FALSE);
				GlUtil.printGlErrorCritical();

			} else {
				// HERE
//				assert(!path.contains("NRM")):path;
				
				GL11.glTexParameteri(target,
						GL11.GL_TEXTURE_MAG_FILTER,  GL11.GL_LINEAR);
				
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
						GL11.GL_TEXTURE_MIN_FILTER,
						GL11.GL_LINEAR_MIPMAP_LINEAR);
				
				GlUtil.printGlErrorCritical();
				
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
						GL12.GL_TEXTURE_MAX_LEVEL, 5);
				
				GlUtil.printGlErrorCritical();
				
				
				
				if (path.contains("textures") && path.contains("block")) {

					
					
					GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
							GL11.GL_TEXTURE_MIN_FILTER,
							GL11.GL_LINEAR_MIPMAP_LINEAR);
					/*
					 * The magnifaction filter GL_TEXTURE_MAG_FILTER doesn't
					 * support mip-mapping, as this just has no meaning for
					 * texture magnification. It only supports GL_NEAREST and
					 * GL_LINEAR. 
					 */

					GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
							GL11.GL_TEXTURE_MAG_FILTER, hasLinMagForBlock(path) ? GL11.GL_LINEAR : GL11.GL_NEAREST);
					GlUtil.printGlErrorCritical();

					if(EngineSettings.BLOCK_TEXTURE_ANISOTROPY.isOn()){
						if(GraphicsContext.current.getCapabilities().GL_EXT_texture_filter_anisotropic){
							float largest = GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
							GL11.glTexParameterf(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, largest);
						}
					}
					
					
//					GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, -0.2f);
					GlUtil.printGlErrorCritical();
				} else {
					GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
							GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
					GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
							GL11.GL_TEXTURE_MIN_FILTER,
							GL11.GL_LINEAR_MIPMAP_LINEAR);
					GlUtil.printGlErrorCritical();
				}
				
				if(GraphicsContext.current.openGL30()){
				}else{
					GL11.glTexParameteri(target,
							GL14.GL_GENERATE_MIPMAP, GL11.GL_TRUE);
				}
				GlUtil.printGlErrorCritical();

				

			}

		}
		if (COMPRESSED_REGULAR && compress) {
			if (dstPixelFormat == GL11.GL_RGB) {
//				System.err.println("Using texture compression on "+path);
				dstPixelFormat = ARBTextureCompression.GL_COMPRESSED_RGB_ARB;
			}
			if (dstPixelFormat == GL11.GL_RGBA) {
//				System.err.println("Using texture compression on "+path);
				dstPixelFormat = ARBTextureCompression.GL_COMPRESSED_RGBA_ARB;
			}
		}

		GlUtil.printGlErrorCritical();
		// produce a texture from the byte buffer
		//		long t = System.currentTimeMillis();
		GL11.glTexImage2D(target, 0, dstPixelFormat, get2Fold(width),
				get2Fold(height), 0, srcPixelFormat, GL11.GL_UNSIGNED_BYTE,
				textureBuffer);
		
		if (MIPMAP_NORMAL && mipMap) {
			if(GraphicsContext.current.getCapabilities().OpenGL30){
				try{
					GL30.glGenerateMipmap(target);
					GlUtil.printGlErrorCritical();
				}catch(Exception e){
					e.printStackTrace();
				}
				
				if (path.contains("textures") && path.contains("block")) {
					/*
					 * Filtering is the process of accessing a particular sample
					 * from a texture. There are two cases for filtering:
					 * minification and magnification. Magnification means that the
					 * area of the fragment in texture space is smaller than a
					 * texel, and minification means that the area of the fragment
					 * in texture space is larger than a texel. Filtering for these
					 * two cases can be set independently.
					 */
					GL11.glTexParameteri(target,
							GL11.GL_TEXTURE_MAG_FILTER,  hasLinMagForBlock(path) ? GL11.GL_LINEAR : GL11.GL_NEAREST);
					
					GL11.glTexParameteri(target,
							GL11.GL_TEXTURE_MIN_FILTER,
							GL11.GL_LINEAR_MIPMAP_LINEAR );
					
					if(EngineSettings.BLOCK_TEXTURE_ANISOTROPY.isOn()){
						if(GraphicsContext.current.getCapabilities().GL_EXT_texture_filter_anisotropic){
							float largest = GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
							GL11.glTexParameterf(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, largest);
						}
					}
				}else{
					GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
							GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
					GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
							GL11.GL_TEXTURE_MIN_FILTER,
							GL11.GL_LINEAR_MIPMAP_LINEAR);
				}
			}
		}
		//		System.err.println("[TEXTURE] loaded: "+path+": "+(System.currentTimeMillis()-t)+" ms; mipmap: "+mipMap);

		GlUtil.printGlErrorCritical();
	}

}
