package org.schema.schine.graphicsengine.forms.gui.newgui.vbogui;

import java.nio.FloatBuffer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.schema.common.util.ByteUtil;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITexDrawableArea;
import org.schema.schine.input.Keyboard;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class VBOAccess {

	private static final ObjectArrayList<VBO> buffers = new ObjectArrayList<VBO>();
	//	GL11.glVertexPointer(2, GL11.GL_FLOAT, 0, 0);
	private final int textureHeight;
	private final int textureWidth;
	public VectexAccess vAccess = new VectexAccess();
	public TextureAccess tAccess = new TextureAccess();
	public long lastTouched = 0;

	public VBOAccess(int textureWidth, int textureHeight) {
		super();
		this.textureHeight = textureHeight;
		this.textureWidth = textureWidth;
	}

	public void render() {
		if (Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
			GlUtil.printGlErrorCritical();
		}
		GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GlUtil.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		if (Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
			GlUtil.printGlErrorCritical();
		}

//		System.err.println("CURRENT::: VBO: "+buffers.size());

		assert (vAccess.vertexIndex != 0);
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, vAccess.vertexIndex);
		// Set The Vertex Pointer To The Vertex Buffer
		GL11.glVertexPointer(2, GL11.GL_FLOAT, 0, vAccess.byteOffset);

		if (Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
			GlUtil.printGlErrorCritical();
		}

		assert (tAccess.texIndex != 0);
		// Bind Buffer to the Tex Coord Array
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, tAccess.texIndex);
		// Set The TexCoord Pointer To The TexCoord Buffer
		GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, tAccess.byteOffset);
		if (Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
			GlUtil.printGlErrorCritical();
		}
		GL11.glDrawArrays(GL11.GL_QUADS, 0, 4);
		if (Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
			GlUtil.printGlErrorCritical();
		}

		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		GlUtil.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		if (Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
			GlUtil.printGlErrorCritical();
		}

	}

	public void generate(GUITexDrawableArea g) {
		vAccess.generate(g);
		tAccess.generate(g);
	}

	public void setFromThis(GUITexDrawableArea guiTexDrawableArea) {
		vAccess.width = (int) guiTexDrawableArea.getWidth();
		vAccess.height = (int) guiTexDrawableArea.getHeight();

		tAccess.xOffset = guiTexDrawableArea.xOffset;
		tAccess.yOffset = guiTexDrawableArea.yOffset;

	}    /* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
     */

	@Override
	public int hashCode() {
		return vAccess.hashCode() + 10000000 * tAccess.hashCode();
	}

	public class VectexAccess {
		public final static int BYTE_SIZE = 4 * 2 * ByteUtil.SIZEOF_FLOAT;
		public int width;
		public int height;
		public int vertexIndex;
		private long byteOffset;

		private void generate(GUITexDrawableArea g) {
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
				GlUtil.printGlErrorCritical();
			}
			if (vertexIndex == 0) {
				if (buffers.isEmpty() || VBO.MAX_FILLED_BYTES - buffers.get(buffers.size() - 1).filledBytes < BYTE_SIZE) {
					int newVBOId = GL15.glGenBuffers();
					GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, newVBOId);
					GL15.glBufferData(GL15.GL_ARRAY_BUFFER, VBO.MAX_FILLED_BYTES, GL15.GL_STATIC_DRAW);

					buffers.add(new VBO(newVBOId));
				}
				vertexIndex = buffers.get(buffers.size() - 1).id;
				this.byteOffset = buffers.get(buffers.size() - 1).filledBytes;
				buffers.get(buffers.size() - 1).filledBytes += BYTE_SIZE;
			}
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
				GlUtil.printGlErrorCritical();
			}
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexIndex);
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
				GlUtil.printGlErrorCritical();
			}
			FloatBuffer fb = GlUtil.getDynamicByteBuffer(BYTE_SIZE, 0).asFloatBuffer();
			fb.put(0);
			fb.put(0);
			fb.put(0);
			fb.put(g.getHeight());
			fb.put(g.getWidth());
			fb.put(g.getHeight());
			fb.put(g.getWidth());
			fb.put(0);
			fb.flip();

			GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, byteOffset, fb);
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
				GlUtil.printGlErrorCritical();
			}
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
				GlUtil.printGlErrorCritical();
			}
		}		@Override
		public int hashCode() {
			return width + 100000 * height;
		}


//		private void generate(GUITexDrawableArea g){
//			if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
//				GlUtil.printGlErrorCritical();
//			}
//			if(vertexIndex == 0){
//				vertexIndex = GL15.glGenBuffers();
//			}
//			if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
//				GlUtil.printGlErrorCritical();
//			}
//			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexIndex);
//			if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
//				GlUtil.printGlErrorCritical();
//			}
//			FloatBuffer fb = GlUtil.getDynamicByteBuffer(4*2*ByteUtil.SIZEOF_FLOAT, 0).asFloatBuffer();
//			fb.put(0); 				fb.put(0);
//			fb.put(0); 				fb.put(g.getHeight());
//			fb.put(g.getWidth()); 	fb.put(g.getHeight());
//			fb.put(g.getWidth()); 	fb.put(0);
//			fb.flip();
//			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, fb, GL15.GL_STATIC_DRAW);
//			if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
//				GlUtil.printGlErrorCritical();
//			}
//			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
//			if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
//				GlUtil.printGlErrorCritical();
//			}
//		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			return width == ((VectexAccess) obj).width && height == ((VectexAccess) obj).height;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "VectexAccess [width=" + width + ", height=" + height
					+ ", vertexIndex=" + vertexIndex + "]";
		}

	}

	public class TextureAccess {

		public final static int BYTE_SIZE = 4 * 2 * ByteUtil.SIZEOF_FLOAT;
		public float xOffset;
		public float yOffset;
		public int texIndex;
		private long byteOffset;

		private void generate(GUITexDrawableArea g) {
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
				GlUtil.printGlErrorCritical();
			}
			if (texIndex == 0) {
				if (buffers.isEmpty() || VBO.MAX_FILLED_BYTES - buffers.get(buffers.size() - 1).filledBytes < BYTE_SIZE) {
					int newVBOId = GL15.glGenBuffers();
					GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, newVBOId);
					GL15.glBufferData(GL15.GL_ARRAY_BUFFER, VBO.MAX_FILLED_BYTES, GL15.GL_STATIC_DRAW);

					buffers.add(new VBO(newVBOId));
				}
				texIndex = buffers.get(buffers.size() - 1).id;
				this.byteOffset = buffers.get(buffers.size() - 1).filledBytes;
				buffers.get(buffers.size() - 1).filledBytes += BYTE_SIZE;
			}
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
				GlUtil.printGlErrorCritical();
			}
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, texIndex);
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
				GlUtil.printGlErrorCritical();
			}
			FloatBuffer fb = GlUtil.getDynamicByteBuffer(BYTE_SIZE, 0).asFloatBuffer();
			fb.put(g.xOffset);
			fb.put(g.yOffset);
			fb.put(g.xOffset);
			fb.put(g.yOffset + (g.getHeight() / textureHeight));
			fb.put(g.xOffset + (g.getWidth() / textureWidth));
			fb.put(g.yOffset + (g.getHeight() / textureHeight));
			fb.put(g.xOffset + (g.getWidth() / textureWidth));
			fb.put(g.yOffset);
			fb.flip();

			GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, byteOffset, fb);
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
				GlUtil.printGlErrorCritical();
			}
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
				GlUtil.printGlErrorCritical();
			}
		}		@Override
		public int hashCode() {
			return (int) (xOffset * textureWidth) + 100000 * (int) (yOffset * textureHeight);
		}



//		private void generate(GUITexDrawableArea g){
//			if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
//				GlUtil.printGlErrorCritical();
//			}
//			if(texIndex == 0){
//				texIndex = GL15.glGenBuffers();
//			}
//			if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
//				GlUtil.printGlErrorCritical();
//			}
//			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, texIndex);
//			if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
//				GlUtil.printGlErrorCritical();
//			}
//			FloatBuffer fb = GlUtil.getDynamicByteBuffer(4*2*ByteUtil.SIZEOF_FLOAT, 0).asFloatBuffer();
//			fb.put(g.xOffset); 												fb.put(g.yOffset);
//			fb.put(g.xOffset); 												fb.put(g.yOffset+((float)g.getHeight()/(float)textureHeight));
//			fb.put(g.xOffset+((float)g.getWidth()/(float)textureWidth)); 	fb.put(g.yOffset+((float)g.getHeight()/(float)textureHeight));
//			fb.put(g.xOffset+((float)g.getWidth()/(float)textureWidth)); 	fb.put(g.yOffset);
//			fb.flip();
//			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, fb, GL15.GL_STATIC_DRAW);
//			if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
//				GlUtil.printGlErrorCritical();
//			}
//			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
//			if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
//				GlUtil.printGlErrorCritical();
//			}
//		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "TextureAccess [xOffset=" + xOffset + ", yOffset=" + yOffset
					+ ", texIndex=" + texIndex + "]";
		}

	}    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */

	private class VBO {
		public static final int MAX_FILLED_BYTES = 1024 * 1024 * 4;
		private int id;
		private long filledBytes;

		public VBO(int id) {
			super();
			this.id = id;
		}

	}	@Override
	public boolean equals(Object obj) {
		VBOAccess o = ((VBOAccess) obj);
		return vAccess.width == o.vAccess.width && vAccess.height == o.vAccess.height &&
				tAccess.xOffset == o.tAccess.xOffset && tAccess.yOffset == o.tAccess.yOffset;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "VBOAccess [vAccess=" + vAccess + ", tAccess=" + tAccess
				+ ", lastTouched=" + lastTouched + "]";
	}

}
