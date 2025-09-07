package org.schema.schine.graphicsengine.texture.textureImp;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.vecmath.Color4f;

import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.system.MemoryUtil;
import org.schema.schine.graphicsengine.core.GlUtil;

/**
 * An OpenGL texture object.
 */
public abstract class TextureNew {

	/**
	 * Formats corresponding to one, two, three, or four color components.
	 */
	protected static final int[] FORMATS = {
			GL11.GL_LUMINANCE, GL11.GL_LUMINANCE_ALPHA, GL11.GL_RGB, GL11.GL_RGBA};
	/**
	 * Compressed internal formats for one, two, three, or four color components.
	 */
	protected static final int[] COMPRESSED_FORMATS = {
			GL13.GL_COMPRESSED_LUMINANCE,
			GL13.GL_COMPRESSED_LUMINANCE_ALPHA,
			GL13.GL_COMPRESSED_RGB,
			GL13.GL_COMPRESSED_RGBA};
	/**
	 * A buffer for floating point values.
	 */
	protected static FloatBuffer _vbuf = MemoryUtil.memAllocFloat(16);
	/**
	 * The OpenGL identifier for this texture.
	 */
	protected int _id;
	/**
	 * The texture target.
	 */
	protected int _target;
	/**
	 * The format of the texture.
	 */
	protected int _format;
	/**
	 * The current minification filter.
	 */
	protected int _minFilter = GL11.GL_NEAREST_MIPMAP_LINEAR;
	/**
	 * The current magnification filter.
	 */
	protected int _magFilter = GL11.GL_LINEAR;
	/**
	 * The maximum degree of anisotropy.
	 */
	protected float _maxAnisotropy = 1f;
	/**
	 * The s texture wrap mode.
	 */
	protected int _wrapS = GL11.GL_REPEAT;
	/**
	 * The t texture wrap mode.
	 */
	protected int _wrapT = GL11.GL_REPEAT;
	/**
	 * The r texture wrap mode.
	 */
	protected int _wrapR = GL11.GL_REPEAT;
	/**
	 * The border color.
	 */
	protected Color4f _borderColor = new Color4f(0f, 0f, 0f, 0f);
	/**
	 * Whether or not mipmaps should be automatically generated.
	 */
	protected boolean _generateMipmaps;
	/**
	 * The texture compare mode.
	 */
	protected int _compareMode = GL11.GL_NONE;
	/**
	 * The texture compare function.
	 */
	protected int _compareFunc = GL11.GL_LEQUAL;
	/**
	 * The depth texture mode.
	 */
	protected int _depthMode = GL11.GL_LUMINANCE;
	/**
	 * The number of bytes occupied by each mipmap level.
	 */
	protected int[] _bytes = new int[0];
	private IntBuffer idbuf;

	/**
	 * Creates an invalid texture (used by the renderer to force reapplication).
	 */
	protected TextureNew() {
	}

	/**
	 * Creates a new texture for the specified renderer.
	 */
	public TextureNew(int target) {
		idbuf = MemoryUtil.memAllocInt(1);
		GL11.glGenTextures(idbuf);
		_id = idbuf.get(0);
		_target = target;
	}

	/**
	 * Converts (and resizes) an image into a buffer of data to be passed to OpenGL11.
	 */
	protected static ByteBuffer getData(
			BufferedImage image, boolean premultiply, int width, int height, boolean rescale) {
		int iwidth = image.getWidth(), iheight = image.getHeight();
		int ncomps = image.getColorModel().getNumComponents();
		// create a compatible color model
		boolean hasAlpha = (ncomps == 2 || ncomps == 4);
		ComponentColorModel cmodel = new ComponentColorModel(
				ColorSpace.getInstance(ncomps >= 3 ? ColorSpace.CS_sRGB : ColorSpace.CS_GRAY),
				hasAlpha,
				hasAlpha && premultiply,
				hasAlpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE,
				DataBuffer.TYPE_BYTE);

		// create the target image
		BufferedImage dest = new BufferedImage(
				cmodel,
				Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, ncomps, null),
				cmodel.isAlphaPremultiplied(), null);

		// draw the image into the target buffer, scaling and flipping it in the process
		double xscale, yscale;
		if (rescale && (width != iwidth || height != iheight)) {
			xscale = (double) width / iwidth;
			yscale = -(double) height / iheight;
		} else {
			xscale = +1.0;
			yscale = -1.0;
		}
		AffineTransform xform = AffineTransform.getScaleInstance(xscale, yscale);
		xform.translate(0.0, -iheight);
		Graphics2D graphics = dest.createGraphics();
		try {
			graphics.setComposite(AlphaComposite.Src);
			graphics.setRenderingHint(
					RenderingHints.KEY_INTERPOLATION,
					rescale ? RenderingHints.VALUE_INTERPOLATION_BILINEAR :
							RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			graphics.drawRenderedImage(image, xform);
		} finally {
			graphics.dispose();
		}

		// get the pixel data and copy it to a byte buffer
		byte[] rgba = ((DataBufferByte) dest.getRaster().getDataBuffer()).getData();
		ByteBuffer data = GlUtil.getDynamicByteBuffer(width * height * ncomps, 0);
		data.put(rgba).rewind();
		return data;
	}

	/**
	 * Returns the format of the data to be returned by {@link #getData} for the specified image.
	 */
	protected static int getFormat(BufferedImage image) {
		return FORMATS[image.getColorModel().getNumComponents() - 1];
	}

	/**
	 * Returns the internal format to use for the given image with optional compression.
	 */
	protected static int getInternalFormat(BufferedImage image, boolean compress) {
		//        int[] formats = (cap..GL_texture_compression && compress) ?
		//            COMPRESSED_FORMATS : FORMATS;
		int[] formats = COMPRESSED_FORMATS;
		return formats[image.getColorModel().getNumComponents() - 1];
	}

	/**
	 * Scales the provided image in half by each dimension for use as a mipmap.
	 */
	protected static BufferedImage halveImage(BufferedImage image) {
		int width = Math.max(1, image.getWidth() / 2);
		int height = Math.max(1, image.getHeight() / 2);
		BufferedImage dest = new BufferedImage(width, height, image.getType());
		Graphics2D graphics = dest.createGraphics();
		try {
			graphics.drawImage(image, 0, 0, width, height, null);
		} finally {
			graphics.dispose();
		}
		return dest;
	}

	/**
	 * Deletes this texture, rendering it unusable.
	 */
	public void delete() {
		GL11.glDeleteTextures(idbuf);
		_id = 0;
	}

	@Override // documentation inherited
	protected void finalize()
			throws Throwable {
		super.finalize();
		if (_id > 0) {
			//            _renderer.textureFinalized(_id, getTotalBytes());
		}
	}

	/**
	 * Generates a set of mipmaps for this texture.  This relies on the GL_EXT_framebuffer_object
	 * extension, so it's really only useful in conjunction with FBOs.
	 */
	public void generateMipmap() {
//		GL30.glGenerateMipmap(_target);
	}

	/**
	 * Returns the depth of the texture.
	 */
	public int getDepth() {
		return 1;
	}

	/**
	 * Returns the format of this texture.
	 */
	public int getFormat() {
		return _format;
	}

	/**
	 * Returns the height of the texture.
	 */
	public abstract int getHeight();

	/**
	 * Returns this texture's OpenGL identifier.
	 */
	public final int getId() {
		return _id;
	}

	/**
	 * Returns the texture's target.
	 */
	public final int getTarget() {
		return _target;
	}

	/**
	 * Returns the total number of bytes in the texture.
	 */
	protected int getTotalBytes() {
		return _bytes.length;
	}

	/**
	 * Returns the width of the texture.
	 */
	public abstract int getWidth();

	/**
	 * Checks whether this texture has an alpha channel.
	 */
	public boolean hasAlpha() {
		// these aren't all the alpha formats; just the ones in TextureConfig
		return _format == GL11.GL_ALPHA ||
				_format == GL13.GL_COMPRESSED_ALPHA ||
				_format == GL11.GL_LUMINANCE_ALPHA ||
				_format == GL13.GL_COMPRESSED_LUMINANCE_ALPHA ||
				_format == GL11.GL_RGBA ||
				_format == GL13.GL_COMPRESSED_RGBA;
	}

	/**
	 * Determines whether this is a depth texture.
	 */
	public boolean isDepth() {
		return _format == GL11.GL_DEPTH_COMPONENT ||
				_format == GL14.GL_DEPTH_COMPONENT16 ||
				_format == GL14.GL_DEPTH_COMPONENT24 ||
				_format == GL14.GL_DEPTH_COMPONENT32;
	}

	/**
	 * Sets the border color.
	 */
	public void setBorderColor(Color4f borderColor) {
		if (!_borderColor.equals(borderColor)) {
			_borderColor.set(borderColor);
			//            GL11.glTexParameter(_target, GL11.GL_TEXTURE_BORDER_COLOR, _vbuf);
		}
	}

	/**
	 * Sets the number of bytes occupied by the specified mipmap level.
	 */
	protected void setBytes(int level, int bytes) {
		if (level >= _bytes.length) {
			int[] obytes = _bytes;
			_bytes = new int[level + 1];
			System.arraycopy(obytes, 0, _bytes, 0, obytes.length);
		}
		_bytes[level] = bytes;
	}

	/**
	 * Convenience method to set both compare parameters at once.
	 */
	public void setCompare(int mode, int func) {
		setCompareMode(mode);
		setCompareFunc(func);
	}

	/**
	 * Sets the texture compare function.
	 */
	public void setCompareFunc(int compareFunc) {
		if (_compareFunc != compareFunc) {
			GL11.glTexParameteri(
					_target, GL14.GL_TEXTURE_COMPARE_FUNC,
					_compareFunc = compareFunc);
		}
	}

	/**
	 * Sets the texture compare mode.
	 */
	public void setCompareMode(int compareMode) {
		if (_compareMode != compareMode) {
			GL11.glTexParameteri(
					_target, GL14.GL_TEXTURE_COMPARE_MODE,
					_compareMode = compareMode);
		}
	}

	/**
	 * Sets the depth texture mode.
	 */
	public void setDepthMode(int depthMode) {
		if (_depthMode != depthMode) {
			GL11.glTexParameteri(
					_target, GL14.GL_DEPTH_TEXTURE_MODE,
					_depthMode = depthMode);
		}
	}

	/**
	 * Convenience method to set both the filters at once.
	 */
	public void setFilters(int min, int mag) {
		setMinFilter(min);
		setMagFilter(mag);
	}

	/**
	 * Sets whether or not to generate mipmaps automatically.
	 */
	public void setGenerateMipmaps(boolean generate) {
		if (_generateMipmaps != generate) {
			GL11.glTexParameteri(
					_target, GL14.GL_GENERATE_MIPMAP,
					(_generateMipmaps = generate) ? GL11.GL_TRUE : GL11.GL_FALSE);
		}
	}

	/**
	 * Sets the texture magnification filter.
	 */
	public void setMagFilter(int magFilter) {
		if (_magFilter != magFilter) {
			GL11.glTexParameteri(_target, GL11.GL_TEXTURE_MAG_FILTER, _magFilter = magFilter);
		}
	}

	/**
	 * Sets the texture maximum anisotropy.
	 */
	public void setMaxAnisotropy(float maxAnisotropy) {
		if (_maxAnisotropy != maxAnisotropy) {
			GL11.glTexParameterf(
					_target, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT,
					_maxAnisotropy = maxAnisotropy);
		}
	}

	/**
	 * Sets the texture minification filter.
	 */
	public void setMinFilter(int minFilter) {
		if (_minFilter != minFilter) {
			GL11.glTexParameteri(_target, GL11.GL_TEXTURE_MIN_FILTER, _minFilter = minFilter);
		}
	}

	/**
	 * Sets the number of bytes occupied by all mipmap levels.
	 */
	protected void setMipmapBytes(int bytes, int... dimensions) {
		int size = Integer.MIN_VALUE;
		for (int i = 0; i < dimensions.length; i++) {
			if (size < dimensions[i]) {
				size = dimensions[i];
			}
		}

		for (int ll = 0; size > 0; ll++, bytes /= 4, size /= 2) {
			setBytes(ll, bytes);
		}
	}

	/**
	 * Convenience method to set the s and t wrap modes at once.
	 */
	public void setWrap(int s, int t) {
		setWrapS(s);
		setWrapT(t);
	}

	/**
	 * Convenience method to set all the wrap modes at once.
	 */
	public void setWrap(int s, int t, int r) {
		setWrapS(s);
		setWrapT(t);
		setWrapR(r);
	}

	/**
	 * Sets the r texture wrap mode.
	 */
	public void setWrapR(int wrap) {
		if (_wrapR != wrap) {
			GL11.glTexParameteri(_target, GL12.GL_TEXTURE_WRAP_R, _wrapR = wrap);
		}
	}

	/**
	 * Sets the s texture wrap mode.
	 */
	public void setWrapS(int wrap) {
		if (_wrapS != wrap) {
			GL11.glTexParameteri(_target, GL11.GL_TEXTURE_WRAP_S, _wrapS = wrap);
		}
	}

	/**
	 * Sets the t texture wrap mode.
	 */
	public void setWrapT(int wrap) {
		if (_wrapT != wrap) {
			GL11.glTexParameteri(_target, GL11.GL_TEXTURE_WRAP_T, _wrapT = wrap);
		}
	}
}
