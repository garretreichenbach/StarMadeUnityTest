package org.schema.schine.graphicsengine.forms.gui.newgui;

import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.vbogui.VBOAccess;
import org.schema.schine.graphicsengine.texture.Texture;
import org.schema.schine.input.InputState;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class GUITexDrawableArea extends GUIElement {

	private static final Int2ObjectOpenHashMap<Mappings> textureToMapping = new Int2ObjectOpenHashMap<Mappings>(1024);
	private static final int MAX_TEXTMAPPINGS_PER_TEXTURE = 256;
	protected static boolean USE_DISPLAYLIST = EngineSettings.GUI_USE_DISPLAY_LISTS.isOn();
	public float xOffset;
	public float yOffset;
	protected Vector4f color;
	protected Texture texture;
	private int height;
	private int width;
	protected boolean sizeChanged = true;
	protected int diaplayListIndex;
	private VBOAccess currentTestAccess;

	public GUITexDrawableArea(InputState state, Texture texture, float xOffset, float yOffset) {
		super(state);
		this.texture = texture;
		this.width = texture.getWidth();
		this.height = texture.getHeight();
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		
		assert(getState() != null);
	}
	public void setOffset(float xOffset, float yOffset){
		this.xOffset = xOffset;
		this.yOffset = yOffset;
	}
	@Override
	public void cleanUp() {
		if (diaplayListIndex != 0) {
			GL11.glDeleteLists(this.diaplayListIndex, 1);
		}
	}
	public void drawRaw() {
		if(getWidth() <= 0 || getHeight() <= 0){
			return;
		}
		if (sizeChanged) {
			if (USE_DISPLAYLIST) {
				genTListOld();
			}
		}
		VBOAccess gen = null;
		if (!USE_DISPLAYLIST) {
			gen = gen();
		}
//		GlUtil.glPushMatrix();
		GlUtil.glTranslatef(getPos().x, getPos().y, getPos().z);
		if(GlUtil.isTextureChaching()){
			GlUtil.loadTextureCached(texture.getTextureId());
		}else{
			texture.attach(0);
		}
		if (color != null) {
			GlUtil.glColor4f(color);
		}
		if (isRenderable()) {
			if (USE_DISPLAYLIST) {
				GL11.glCallList(diaplayListIndex);
			} else {
				gen.render();
			}
		}
		if (isMouseUpdateEnabled()) {
			checkMouseInside();
		}
		GlUtil.glTranslatef(-getPos().x, -getPos().y, -getPos().z);
//		GlUtil.glPopMatrix();
	}
	@Override
	public void draw() {
		if(getWidth() <= 0 || getHeight() <= 0){
			return;
		}
//		USE_DISPLAYLIST = Keyboard.isKeyDown(org.lwjgl.input.GLFW.GLFW_KEY_RIGHT_SHIFT);
//		System.err.println("USE: "+USE_DISPLAYLIST);
		if (sizeChanged) {
			if (USE_DISPLAYLIST) {
				genTListOld();
			}
		}
		VBOAccess gen = null;
		if (!USE_DISPLAYLIST) {
			gen = gen();
		}

		GlUtil.glPushMatrix();

		transform();

		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		GlUtil.glDisable(GL11.GL_LIGHTING);

		texture.attach(0);
//		assert(diaplayListIndex != 0);
		if (color != null) {
			GlUtil.glColor4f(color);
		}
		if (isRenderable()) {
			if (USE_DISPLAYLIST) {
				GL11.glCallList(diaplayListIndex);
			} else {
				gen.render();
			}
		}
		texture.detach();

		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);

		GlUtil.glColor4f(1, 1, 1, 1);
		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);

		if (isMouseUpdateEnabled()) {
			checkMouseInside();
		}

		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
		if(getWidth() <= 0 || getHeight() <= 0){
			return;
		}
		gen();
	}

	protected void genTListOld() {
		if (diaplayListIndex != 0) {
			GL11.glDeleteLists(this.diaplayListIndex, 1);
		}

		this.diaplayListIndex = GL11.glGenLists(1);
		// compile the display list, store a triangle in it
		GL11.glNewList(diaplayListIndex, GL11.GL_COMPILE);

		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(xOffset, yOffset);
		GL11.glVertex2f(0, 0);

		GL11.glTexCoord2f(xOffset, yOffset + (getHeight() / texture.getHeight()));
		GL11.glVertex2f(0, getHeight());

		GL11.glTexCoord2f(xOffset + (getWidth() / texture.getWidth()), yOffset + (getHeight() / texture.getHeight()));
		GL11.glVertex2f(getWidth(), getHeight());

		GL11.glTexCoord2f(xOffset + (getWidth() / texture.getWidth()), yOffset);
		GL11.glVertex2f(getWidth(), 0);

		GL11.glEnd();

		GL11.glEndList();

		sizeChanged = false;
	}

	protected VBOAccess gen() {
		Mappings mappings = textureToMapping.get(texture.getTextureId());
		if (mappings == null) {
			mappings = new Mappings();
			textureToMapping.put(texture.getTextureId(), mappings);
		} else {
		}

		if (currentTestAccess == null) {
			currentTestAccess = new VBOAccess(texture.getWidth(), texture.getHeight());
		}
		currentTestAccess.setFromThis(this);

//		System.err.println("TEXMAP: "+texture.getName()+" "+mappings.texMappings.size());

		VBOAccess vboAccess = mappings.accessMap.get(currentTestAccess);
		if (vboAccess != null) {
//			assert(false);
			vboAccess.lastTouched = getState().getNumberOfUpdate();
			return vboAccess;
		} else {
//			for(VBOAccess a : mappings.texMappings){
//				if(a.tAccess.xOffset == xOffset && a.tAccess.yOffset == yOffset){
//					System.err.println("EQUAL TT");
//					if(a.vAccess.width == width && a.vAccess.height == height){
//						System.err.println("EQUAL V");
//						assert(currentTestAccess.tAccess.xOffset == xOffset && currentTestAccess.tAccess.yOffset == yOffset):currentTestAccess;
//						assert(currentTestAccess.vAccess.width == width && currentTestAccess.vAccess.height == height):currentTestAccess;
//						assert(false);
//					}
//				}
//			}
			VBOAccess newAccess = null;
			if (mappings.accessMap.size() > MAX_TEXTMAPPINGS_PER_TEXTURE) {
				ObjectIterator<VBOAccess> iterator = mappings.accessMap.iterator();

				while (iterator.hasNext()) {
					VBOAccess next = iterator.next();
					if (next.lastTouched < getState().getNumberOfUpdate() - 2) {
						newAccess = next;
						iterator.remove();
						break;
					}
				}
			}
			boolean newAc = false;
			if (newAccess == null) {
				//clean up old if needed
				newAccess = new VBOAccess(texture.getWidth(), texture.getHeight());
				newAc = true;
			}
			newAccess.setFromThis(this);

			assert (newAccess.tAccess.xOffset == GUITexDrawableArea.this.xOffset):newAccess.tAccess.xOffset+"; "+GUITexDrawableArea.this.xOffset;
			assert (newAccess.tAccess.yOffset == GUITexDrawableArea.this.yOffset);
			assert (newAccess.vAccess.width == GUITexDrawableArea.this.width);
			assert (newAccess.vAccess.height == GUITexDrawableArea.this.height);

			assert (newAccess.tAccess.xOffset == xOffset):newAccess.tAccess.xOffset+"; "+xOffset;
			assert (newAccess.tAccess.yOffset == yOffset);
			assert (newAccess.vAccess.width == width);
			assert (newAccess.vAccess.height == height);

			assert (currentTestAccess.equals(newAccess)) : "\n" + currentTestAccess + "; \n" + newAccess + "\n" + width + ", " + height + "; " + xOffset + ", " + yOffset + "; newAc: " + newAc;

			newAccess.generate(this);
			boolean add = mappings.accessMap.add(newAccess);
			assert (add) : newAccess;
			newAccess.lastTouched = getState().getNumberOfUpdate();
			return newAccess;
		}
	}

	@Override
	public float getHeight() {
		return height;
	}

	@Override
	public float getWidth() {
		return width;
	}

	public void setWidth(int width) {
		if (this.width != width) {

			sizeChanged = true;
			this.width = width;
		}
	}

	public void setHeight(int height) {
		if (this.height != height) {
			sizeChanged = true;
			this.height = height;
		}
	}

	public void setSpriteSubIndex(int index, int xMax, int yMax) {
		int x = index % xMax;
		int y = index / xMax;

		float tileX = 1f / xMax;
		float tileY = 1f / yMax;
		xOffset = x * tileX;
		yOffset = y * tileY;
		sizeChanged = true;
	}

	/**
	 * @return the color
	 */
	public Vector4f getColor() {
		return color;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(Vector4f color) {
		this.color = color;
	}

	/**
	 * @param sizeChanged the sizeChanged to set
	 */
	public void setSizeChanged(boolean sizeChanged) {
		this.sizeChanged = sizeChanged;
	}

	private class Mappings {
		private ObjectOpenHashSet<VBOAccess> accessMap = new ObjectOpenHashSet<VBOAccess>();
	}

}
