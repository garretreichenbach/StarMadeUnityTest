/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>Sprite</H2>
 * <H3>org.schema.schine.graphicsengine.forms</H3>
 * Sprite.java
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
package org.schema.schine.graphicsengine.forms;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.texture.Material;
import org.schema.schine.graphicsengine.texture.Texture;
import org.schema.schine.input.Mouse;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.List;

/**
 * The Class Sprite.
 */
public class Sprite extends SceneNode {

	public static final int BLEND_SEPERATE_NORMAL = 1;
	public static final int BLEND_ADDITIVE = 2;
	public static int lastTexId = -1;
	static int sSizeVerts = 4 * 3;
	static int sSizeTex = 4 * 2;
	static int sSizeNorm = 4 * 3;
	private static FloatBuffer vertices; // Vertex Data
	private static FloatBuffer texCoords; // TextureNew Coordinates
//	private static FloatBuffer normals; // TextureNew Coordinates
	private static FloatBuffer tempModelviewBuffer = MemoryUtil.memAllocFloat(16);
	private static IntBuffer viewportTemp = MemoryUtil.memAllocInt(16);
	private static FloatBuffer projectionTemp = MemoryUtil.memAllocFloat(16);
	private static FloatBuffer modelviewTemp = MemoryUtil.memAllocFloat(16);
	private static FloatBuffer depth = MemoryUtil.memAllocFloat(1);

	static {
		
		Matrix4f m = new Matrix4f();
		m.setIdentity();
		Matrix4fTools.scale(m, new Vector3f(0.01f, -0.01f, 0.01f));
		Matrix4fTools.store(m, tempModelviewBuffer);

		tempModelviewBuffer.rewind();
	}

	public int blendFunc;
	public boolean useShader = true;
	/**
	 * The animation number.
	 */
	protected int animationNumber;
	private int multiSpriteMax = 1;
	private int selectedMultiSprite = 0;
	/**
	 * The position center.
	 */
	private boolean positionCenter = false;
	/**
	 * The depth test.
	 */
	private boolean depthTest = true;
	/**
	 * The blend.
	 */
	private boolean blend = true;
	private Vector4f tint;
	private int width, height;
	private boolean flip = false;
	/**
	 * The billboard.
	 */
	private boolean billboard = false;
	private int VBOverts;// = MemoryUtil.memAllocInt(1);
	private IntBuffer VBOTexCoords;
//	private int VBONormals;
	private boolean orthogonal = false;
	private boolean VBOActive = true;
	private boolean firstDraw = true;
	private int multiSpriteMaxX;
	private int multiSpriteMaxY;
	private float selectionAreaLength;

	/**
	 * Instantiates a new sprite.
	 *
	 * @param height
	 * @param width
	 */
	public Sprite(int width, int height) {
		this.material = new Material();
		this.width = width;
		this.height = height;
	}

	/**
	 * Instantiates a new sprite.
	 *
	 * @param tex the tex
	 */
	public Sprite(Texture tex) {
		this(tex.getWidth(), tex.getHeight());
		this.getMaterial().setTexture(tex);
	}

	public static void doDraw(Sprite sprite) {

		if (sprite.tint != null) {
			GlUtil.glColor4f(sprite.tint.x, sprite.tint.y, sprite.tint.z, sprite.tint.w);
		} else {
			GlUtil.glColor4f(1, 1, 1, 1);
		}
		if (sprite.getScale() != null) {
			GL11.glScalef(sprite.getScale().x, sprite.getScale().y, sprite.getScale().z);
		} else {
			GL11.glScalef(1f, 1f, 1f);
		}
		if (lastTexId != sprite.selectedMultiSprite) {
			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, sprite.VBOTexCoords.get(sprite.selectedMultiSprite));
			GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);
			lastTexId = sprite.selectedMultiSprite;
		}
		GL11.glDrawArrays(GL11.GL_QUADS, 0, 4);
	}

	public static void draw(Sprite sprite, int arrayLength, Positionable... poses) {

		if (sprite.firstDraw) {
			sprite.onInit();

		}
		if (sprite.isInvisible()) {
			return;
		}

		if (EngineSettings.G_CULLING_ACTIVE.isOn()) {
			sprite.activateCulling();
		}
		GlUtil.glPushMatrix();

		GlUtil.glDisable(GL11.GL_LIGHTING);
		if (sprite.depthTest) {
			GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		} else {
			GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		}

		if (sprite.blend) {
			GlUtil.glEnable(GL11.GL_BLEND);
			GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); // default
			// : one
			// minus
			// source
			// alpha
		} else {
			GlUtil.glDisable(GL11.GL_BLEND);
		}
		// GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		sprite.material.getTexture().attach(0);
		// draw Sprite
		if (sprite.VBOActive && EngineSettings.G_USE_SPRITE_VBO.isOn()) {
			// Enable Pointers
			GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY); // Enable Vertex  Arrays
			// Enable TextureNew Coord Arrays
			GlUtil.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

			// Bind Buffer To the Vertex Array
			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, sprite.VBOverts);
			// Set The Vertex Pointer To The Vertex Buffer
			GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);

			GlUtil.glActiveTexture(GL13.GL_TEXTURE0);

			if (sprite.tint != null) {
				GlUtil.glColor4f(sprite.tint.x, sprite.tint.y, sprite.tint.z, sprite.tint.w);
			} else {
				GlUtil.glColor4f(1, 1, 1, 1);
			}
			// Bind Buffer to the Tex Coord Array
			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, sprite.VBOTexCoords.get(sprite.selectedMultiSprite));
			// Set The TexCoord Pointer To The TexCoord Buffer
			GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);

			//GlUtil.glActiveTexture(GL13.GL_TEXTURE0);


			// Render
			// Draw All Of The Triangles At Once
			int i = 0;

			if (arrayLength > 0) {
				GlUtil.glPushMatrix();

				for (int c = 0; c < arrayLength; c++) {
					GlUtil.glPushMatrix();
					Positionable p = poses[c];

					GL11.glTranslatef(p.getPos().x, p.getPos().y, p.getPos().z);

					if (sprite.billboard) {
						FloatBuffer billboardSphericalBeginMatrix = sprite.getBillboardSphericalBeginMatrix();
						GL11.glLoadMatrixf(billboardSphericalBeginMatrix);
					}
					if (sprite.flip) {
						GL11.glScalef(sprite.getScale().x, -sprite.getScale().y, sprite.getScale().z);
					} else {
						GL11.glScalef(sprite.getScale().x, sprite.getScale().y, sprite.getScale().z);
					}

					GL11.glDrawArrays(GL11.GL_QUADS, 0, 4);
					GlUtil.glPopMatrix();
					i++;
				}
				GlUtil.glPopMatrix();
			} else {

				GlUtil.glPushMatrix();
				if (sprite.billboard) {
					GlUtil.translateModelview(sprite.getPos());
					tempModelviewBuffer.put(0, sprite.getScale().x);
					tempModelviewBuffer.put(5, -sprite.getScale().y);
					tempModelviewBuffer.put(10, sprite.getScale().z);

					tempModelviewBuffer.put(12, Controller.modelviewMatrix.m30);
					tempModelviewBuffer.put(13, Controller.modelviewMatrix.m31);
					tempModelviewBuffer.put(14, Controller.modelviewMatrix.m32);
					tempModelviewBuffer.rewind();
					GlUtil.glLoadMatrix(tempModelviewBuffer);
				}
				GL11.glDrawArrays(GL11.GL_QUADS, 0, 4);
				GlUtil.glPopMatrix();
			}

			// Disable Pointers
			// Disable Vertex Arrays
			GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
			// Disable TextureNew Coord Arrays
			GlUtil.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

			GlUtil.glDisableClientState(GL11.GL_NORMAL_ARRAY);
		} else {
			float wMin = 0;
			float wMax = 1;
			float hMin = 0;
			float hMax = 1;

			float width = sprite.material.getTexture().getWidth() * sprite.getScale().x;
			float height = sprite.material.getTexture().getHeight() * sprite.getScale().y;
			//			System.err.println(sprite+" has "+width+", "+height);
			GL11.glBegin(GL11.GL_QUADS);
			{
				if (sprite.positionCenter) {
					GL11.glTexCoord2f(wMin, hMin);
					GL11.glVertex3f(-width / 2, -height / 2, 0);
					GL11.glTexCoord2f(wMin, hMax);
					GL11.glVertex3f(-width / 2, height / 2, 0);
					GL11.glTexCoord2f(wMax, hMax);
					GL11.glVertex3f(width / 2, height / 2, 0);
					GL11.glTexCoord2f(wMax, hMin);
					GL11.glVertex3f(width / 2, -height / 2, 0);
				} else {
					GL11.glTexCoord2f(wMin, hMin);
					GL11.glVertex3f(0, 0, 0);
					GL11.glTexCoord2f(wMin, hMax);
					GL11.glVertex3f(0, height, 0);
					GL11.glTexCoord2f(wMax, hMax);
					GL11.glVertex3f(width, height, 0);
					GL11.glTexCoord2f(wMax, hMin);
					GL11.glVertex3f(width, 0, 0);
				}
			}
			GL11.glEnd();
		}
		sprite.material.getTexture().detach();

		GlUtil.glPopMatrix();

		//set color back to full
		GlUtil.glColor4f(1, 1, 1, 1);

		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		if (EngineSettings.G_CULLING_ACTIVE.isOn()) {
			sprite.activateCulling();
		}

	}

	public static void drawRaw(Sprite sprite) {
		
		GlUtil.glDisable(GL11.GL_LIGHTING);
		if (sprite.depthTest) {
			GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		} else {
			GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		}

		if (sprite.blend) {
			GlUtil.glEnable(GL11.GL_BLEND);
			GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); // default
		} else {
			GlUtil.glDisable(GL11.GL_BLEND);
		}
		
		// Enable Pointers
		GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY); // Enable Vertex  Arrays
		// Enable TextureNew Coord Arrays
		GlUtil.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

		// Bind Buffer To the Vertex Array
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, sprite.VBOverts);
		// Set The Vertex Pointer To The Vertex Buffer
		GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);

		sprite.material.getTexture().attach(0);
		
		if (sprite.tint != null) {
			GlUtil.glColor4f(sprite.tint.x, sprite.tint.y, sprite.tint.z, sprite.tint.w);
		} else {
			GlUtil.glColor4f(1, 1, 1, 1);
		}
		// Bind Buffer to the Tex Coord Array
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, sprite.VBOTexCoords.get(sprite.selectedMultiSprite));
		// Set The TexCoord Pointer To The TexCoord Buffer
		GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);

		//GlUtil.glActiveTexture(GL13.GL_TEXTURE0);


		// Render
		// Draw All Of The Triangles At Once
		int i = 0;
		int lastMultiSprite = -1;
		GL11.glDrawArrays(GL11.GL_QUADS, 0, 4);
		// Disable Pointers
		// Disable Vertex Arrays
		GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		// Disable TextureNew Coord Arrays
		GlUtil.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
	}
	public static void draw(Sprite sprite, int arrayLength, PositionableSubSprite... poses) {

		if (sprite.firstDraw) {
			sprite.onInit();

		}
		if (sprite.isInvisible()) {
			return;
		}

		if (EngineSettings.G_CULLING_ACTIVE.isOn()) {
			sprite.activateCulling();
		}
		boolean colored = poses.length > 0 && poses[0] instanceof PositionableSubColorSprite;
		boolean selectable = poses.length > 0 && poses[0] instanceof SelectableSprite;
		GlUtil.glPushMatrix();

		GlUtil.glDisable(GL11.GL_LIGHTING);
		if (sprite.depthTest) {
			GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		} else {
			GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		}

		if (sprite.blend) {
			GlUtil.glEnable(GL11.GL_BLEND);
			GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); // default
		} else {
			GlUtil.glDisable(GL11.GL_BLEND);
		}
		// GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		sprite.material.getTexture().attach(0);
		// draw Sprite
		if (sprite.VBOActive && EngineSettings.G_USE_SPRITE_VBO.isOn()) {
			// Enable Pointers
			GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY); // Enable Vertex  Arrays
			// Enable TextureNew Coord Arrays
			GlUtil.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

			// Bind Buffer To the Vertex Array
			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, sprite.VBOverts);
			// Set The Vertex Pointer To The Vertex Buffer
			GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);

			GlUtil.glActiveTexture(GL13.GL_TEXTURE0);

			if (sprite.tint != null) {
				GlUtil.glColor4f(sprite.tint.x, sprite.tint.y, sprite.tint.z, sprite.tint.w);
			} else {
				GlUtil.glColor4f(1, 1, 1, 1);
			}
			// Bind Buffer to the Tex Coord Array
			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, sprite.VBOTexCoords.get(sprite.selectedMultiSprite));
			// Set The TexCoord Pointer To The TexCoord Buffer
			GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);

			//GlUtil.glActiveTexture(GL13.GL_TEXTURE0);


			// Render
			// Draw All Of The Triangles At Once
			int i = 0;
			int lastMultiSprite = -1;
			if (arrayLength > 0) {
				GlUtil.glPushMatrix();

				for (int c = 0; c < arrayLength; c++) {
					GlUtil.glPushMatrix();
					PositionableSubSprite p = poses[c];

					if (colored) {
						PositionableSubColorSprite coloredSprite = (PositionableSubColorSprite) p;

						if (GlUtil.loadedShader == ShaderLibrary.selectionShader) {
							GlUtil.updateShaderFloat(ShaderLibrary.selectionShader, "texMult", 1);
							GlUtil.updateShaderVector4f(ShaderLibrary.selectionShader, "selectionColor", coloredSprite.getColor().x, coloredSprite.getColor().y, coloredSprite.getColor().z, coloredSprite.getColor().w);
						}

						GlUtil.glColor4f(coloredSprite.getColor().x, coloredSprite.getColor().y, coloredSprite.getColor().z, coloredSprite.getColor().w);

					}

					GL11.glTranslatef(p.getPos().x, p.getPos().y, p.getPos().z);

					sprite.setSelectedMultiSprite(p.getSubSprite(sprite));

					if (sprite.selectedMultiSprite != lastMultiSprite) {
						GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, sprite.VBOTexCoords.get(sprite.selectedMultiSprite));
						// Set The TexCoord Pointer To The TexCoord Buffer
						GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);
						lastMultiSprite = sprite.selectedMultiSprite;
					}
					if (sprite.billboard) {
						FloatBuffer billboardSphericalBeginMatrix = sprite.getBillboardSphericalBeginMatrix();
						GL11.glLoadMatrixf(billboardSphericalBeginMatrix);
					}

					if (sprite.flip) {
						GL11.glScalef(sprite.getScale().x, -sprite.getScale().y, sprite.getScale().z);
					} else {
						GL11.glScalef(sprite.getScale().x, sprite.getScale().y, sprite.getScale().z);
					}

					GL11.glDrawArrays(GL11.GL_QUADS, 0, 4);

					GlUtil.glPopMatrix();
					i++;
				}
				GlUtil.glPopMatrix();
			} else {

				GlUtil.glPushMatrix();
				if (sprite.billboard) {
					GlUtil.translateModelview(sprite.getPos());
					tempModelviewBuffer.put(0, sprite.getScale().x);
					tempModelviewBuffer.put(5, -sprite.getScale().y);
					tempModelviewBuffer.put(10, sprite.getScale().z);

					tempModelviewBuffer.put(12, Controller.modelviewMatrix.m30);
					tempModelviewBuffer.put(13, Controller.modelviewMatrix.m31);
					tempModelviewBuffer.put(14, Controller.modelviewMatrix.m32);
					tempModelviewBuffer.rewind();
					GlUtil.glLoadMatrix(tempModelviewBuffer);
				}
				GL11.glDrawArrays(GL11.GL_QUADS, 0, 4);
				GlUtil.glPopMatrix();
			}

			// Disable Pointers
			// Disable Vertex Arrays
			GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
			// Disable TextureNew Coord Arrays
			GlUtil.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

		} else {
			float wMin = 0;
			float wMax = 1;
			float hMin = 0;
			float hMax = 1;

			float width = sprite.material.getTexture().getWidth() * sprite.getScale().x;
			float height = sprite.material.getTexture().getHeight() * sprite.getScale().y;
			//			System.err.println(sprite+" has "+width+", "+height);
			GL11.glBegin(GL11.GL_QUADS);
			{
				if (sprite.positionCenter) {
					GL11.glTexCoord2f(wMin, hMin);
					GL11.glVertex3f(-width / 2, -height / 2, 0);
					GL11.glTexCoord2f(wMin, hMax);
					GL11.glVertex3f(-width / 2, height / 2, 0);
					GL11.glTexCoord2f(wMax, hMax);
					GL11.glVertex3f(width / 2, height / 2, 0);
					GL11.glTexCoord2f(wMax, hMin);
					GL11.glVertex3f(width / 2, -height / 2, 0);
				} else {
					GL11.glTexCoord2f(wMin, hMin);
					GL11.glVertex3f(0, 0, 0);
					GL11.glTexCoord2f(wMin, hMax);
					GL11.glVertex3f(0, height, 0);
					GL11.glTexCoord2f(wMax, hMax);
					GL11.glVertex3f(width, height, 0);
					GL11.glTexCoord2f(wMax, hMin);
					GL11.glVertex3f(width, 0, 0);
				}
			}
			GL11.glEnd();
		}
		sprite.material.getTexture().detach();

		GlUtil.glPopMatrix();

		//set color back to full
		GlUtil.glColor4f(1, 1, 1, 1);

		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		if (EngineSettings.G_CULLING_ACTIVE.isOn() && sprite.isFlipCulling()) {
			//reset culling
			GlUtil.glEnable(GL11.GL_CULL_FACE);
			GL11.glCullFace(GL11.GL_BACK);
		}

	}

	public static void endDraw(Sprite sprite) {
		// Disable Pointers
		// Disable Vertex Arrays
		GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		// Disable TextureNew Coord Arrays
		GlUtil.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

		GlUtil.glDisableClientState(GL11.GL_NORMAL_ARRAY);

		sprite.material.getTexture().detach();

		//set color/scale back to full
		GlUtil.glColor4f(1, 1, 1, 1);
		GL11.glScalef(1f, 1f, 1f);

		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		if (EngineSettings.G_CULLING_ACTIVE.isOn() && sprite.isFlipCulling()) {
			//reset culling
			GlUtil.glEnable(GL11.GL_CULL_FACE);
			GL11.glCullFace(GL11.GL_BACK);
		}
	}

	public static void startDraw(Sprite sprite) {
		lastTexId = -1;
		if (sprite.firstDraw) {
			sprite.onInit();
		}

		if (EngineSettings.G_CULLING_ACTIVE.isOn()) {
			sprite.activateCulling();
		}

		GlUtil.glDisable(GL11.GL_LIGHTING);
		if (sprite.depthTest) {
			GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		} else {
			GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		}

		if (sprite.blend) {
			GlUtil.glEnable(GL11.GL_BLEND);
			GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); // default
			// : one
			// minus
			// source
			// alpha
		} else {
			GlUtil.glDisable(GL11.GL_BLEND);
		}
		// GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		sprite.material.getTexture().attach(0);
		// draw Sprite
		if (sprite.VBOActive && EngineSettings.G_USE_SPRITE_VBO.isOn()) {
			// Enable Pointers
			GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY); // Enable Vertex  Arrays
			// Enable TextureNew Coord Arrays
			GlUtil.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);


			// Bind Buffer To the Vertex Array
			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, sprite.VBOverts);
			// Set The Vertex Pointer To The Vertex Buffer
			GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);

			GlUtil.glActiveTexture(GL13.GL_TEXTURE0);

			if (sprite.tint != null) {
				GlUtil.glColor4f(sprite.tint.x, sprite.tint.y, sprite.tint.z, sprite.tint.w);
			} else {
				GlUtil.glColor4f(1, 1, 1, 1);
			}
			// Bind Buffer to the Tex Coord Array
			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, sprite.VBOTexCoords.get(sprite.selectedMultiSprite));
			// Set The TexCoord Pointer To The TexCoord Buffer
			GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);
		} else {
			assert (false);
		}
	}
	private static Vector3f rightTmp = new Vector3f();
	private static Vector3f upTmp = new Vector3f();
	
	private static Vector4f outTmp = new Vector4f();
	
	private static Vector4f ss = new Vector4f(0, 0, 0, 1);
	private static Vector4f outTmpCp = new Vector4f();
	public static void draw3D(Sprite sprite, Collection<? extends PositionableSubSprite> poses, Camera camera) {

		if (sprite.firstDraw) {
			sprite.onInit();

		}
		if (sprite.isInvisible()) {
			return;
		}
		SelectableSprite lastSelected = null;
		boolean colored;
		boolean selectable;

		if (poses instanceof List) {
			colored = poses.size() > 0 && ((List<? extends PositionableSubSprite>) poses).get(0) instanceof PositionableSubColorSprite;
			selectable = poses.size() > 0 && ((List<? extends PositionableSubSprite>) poses).get(0) instanceof SelectableSprite;
		} else {
			colored = poses.size() > 0 && poses.iterator().next() instanceof PositionableSubColorSprite;
			selectable = poses.size() > 0 && poses.iterator().next() instanceof SelectableSprite;
		}

		if (EngineSettings.G_CULLING_ACTIVE.isOn()) {
			sprite.activateCulling();
		}
		GlUtil.glPushMatrix();

		GlUtil.glDisable(GL11.GL_LIGHTING);
		if (sprite.depthTest) {
			GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		} else {
			GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		}

		if (sprite.blend) {
			GlUtil.glEnable(GL11.GL_BLEND);
//			GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			if (sprite.blendFunc == 0) {
				GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); // default
			} else if (sprite.blendFunc == BLEND_ADDITIVE) {
				GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
//				GlUtil.glBlendFuncSeparate(GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ONE);
			} else {
				assert (sprite.blendFunc == BLEND_SEPERATE_NORMAL);
				GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
//				GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE); // default
			}
			// : one
			// minus
			// source
			// alpha
//			GlUtil.glDisable(GL11.GL_BLEND);
		} else {
			GlUtil.glDisable(GL11.GL_BLEND);
		}
		float winX = 0;
		float winY = 0;
		if (selectable) {
			winX = Mouse.getX();
			winY = Mouse.getY();
		}
		// GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		sprite.material.getTexture().attach(0);

		// draw Sprite
		if (sprite.VBOActive && EngineSettings.G_USE_SPRITE_VBO.isOn()) {

			if (sprite.useShader) {
				ShaderLibrary.selectionShader.loadWithoutUpdate();

				GlUtil.updateShaderFloat(ShaderLibrary.selectionShader, "texMult", 1);
				GlUtil.updateShaderVector4f(ShaderLibrary.selectionShader, "selectionColor", 1, 1, 1, 1);
				GlUtil.updateShaderInt(ShaderLibrary.selectionShader, "mainTexA", 0);
			}
			// Enable Pointers
			GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY); // Enable Vertex  Arrays
			// Enable TextureNew Coord Arrays
			GlUtil.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);


			// Bind Buffer To the Vertex Array
			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, sprite.VBOverts);
			// Set The Vertex Pointer To The Vertex Buffer
			GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);

			GlUtil.glActiveTexture(GL13.GL_TEXTURE0);

			if (sprite.tint != null) {
				GlUtil.glColor4f(sprite.tint.x, sprite.tint.y, sprite.tint.z, sprite.tint.w);
			} else {
				GlUtil.glColor4f(1, 1, 1, 1);
			}
			int i = 0;

			GlUtil.glPushMatrix();
			int lastMultiSprite = -1;

			
			Vector3f up = camera.getUp(upTmp);
			Vector3f right = camera.getRight(rightTmp);
			up.scale(0.30f);
			right.scale(0.30f);
			long time = System.currentTimeMillis();

			if (poses instanceof List) {
				List<? extends PositionableSubSprite> l = (List<? extends PositionableSubSprite>) poses;
				final int size = l.size();
				for (int j = 0; j < size; j++) {
					PositionableSubSprite p = l.get(j);
					i++;
					if (!camera.isPointInFrustrum(p.getPos()) || p.getSubSprite(sprite) < 0 || !p.canDraw()) {
						continue;
					}
					sprite.setSelectedMultiSprite(p.getSubSprite(sprite));

					if (sprite.selectedMultiSprite != lastMultiSprite) {
						GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, sprite.VBOTexCoords.get(sprite.selectedMultiSprite));
						// Set The TexCoord Pointer To The TexCoord Buffer
						GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);
						lastMultiSprite = sprite.selectedMultiSprite;
					}
					if (colored) {
						PositionableSubColorSprite c = (PositionableSubColorSprite) p;
						if (sprite.useShader) {
							GlUtil.updateShaderVector4f(ShaderLibrary.selectionShader, "selectionColor", c.getColor().x, c.getColor().y, c.getColor().z, c.getColor().w);
						}
						GlUtil.glColor4f(c.getColor().x, c.getColor().y, c.getColor().z, c.getColor().w);
					}

					GlUtil.glPushMatrix();
					if (!sprite.positionCenter) {
						GlUtil.translateModelview(p.getPos().x + up.x + right.x, p.getPos().y + up.y + right.y, p.getPos().z + up.z + right.z);
					} else {
						GlUtil.translateModelview(p.getPos().x, p.getPos().y, p.getPos().z);
					}

					float iconscale = p.getScale(time);
					tempModelviewBuffer.put(0, iconscale);
					tempModelviewBuffer.put(5, -iconscale);
					tempModelviewBuffer.put(10, iconscale);

					tempModelviewBuffer.put(12, Controller.modelviewMatrix.m30);
					tempModelviewBuffer.put(13, Controller.modelviewMatrix.m31);
					tempModelviewBuffer.put(14, Controller.modelviewMatrix.m32);
					tempModelviewBuffer.rewind();
					GlUtil.glLoadMatrix(tempModelviewBuffer);
					if (selectable && ((SelectableSprite) p).isSelectable()) {

						Matrix4fTools.transform(Controller.modelviewMatrix, ss, outTmp);
						outTmpCp.set(outTmp);
						Matrix4fTools.transform(Controller.projectionMatrix, outTmpCp, outTmp);
						
						float winZ = outTmp.z / outTmp.w * 0.5f + 0.5f;

						if (getMousePosition(sprite, winX, winY, winZ)) {
							if (lastSelected != null) {
								if (lastSelected.getSelectionDepth() > ((SelectableSprite) p).getSelectionDepth()) {
									lastSelected.onUnSelect();
									((SelectableSprite) p).onSelect(winZ);
									lastSelected = ((SelectableSprite) p);
								} else {
									((SelectableSprite) p).onUnSelect();
								}
							} else {
								((SelectableSprite) p).onSelect(winZ);
								lastSelected = ((SelectableSprite) p);
							}
						} else {
							((SelectableSprite) p).onUnSelect();
						}
					}

					GL11.glDrawArrays(GL11.GL_QUADS, 0, 4);
					GlUtil.glPopMatrix();

				}
			} else {
				for (PositionableSubSprite p : poses) {
					i++;
					if (!camera.isPointInFrustrum(p.getPos()) || p.getSubSprite(sprite) < 0 || !p.canDraw()) {
						continue;
					}
					sprite.setSelectedMultiSprite(p.getSubSprite(sprite));

					if (sprite.selectedMultiSprite != lastMultiSprite) {
						GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, sprite.VBOTexCoords.get(sprite.selectedMultiSprite));
						// Set The TexCoord Pointer To The TexCoord Buffer
						GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);
						lastMultiSprite = sprite.selectedMultiSprite;
					}
					if (colored) {
						PositionableSubColorSprite c = (PositionableSubColorSprite) p;
						if (sprite.useShader) {
							GlUtil.updateShaderVector4f(ShaderLibrary.selectionShader, "selectionColor", c.getColor().x, c.getColor().y, c.getColor().z, c.getColor().w);
						}
						GlUtil.glColor4f(c.getColor().x, c.getColor().y, c.getColor().z, c.getColor().w);
					}

					GlUtil.glPushMatrix();
					if (!sprite.positionCenter) {
						GlUtil.translateModelview(p.getPos().x + up.x + right.x, p.getPos().y + up.y + right.y, p.getPos().z + up.z + right.z);
					} else {
						GlUtil.translateModelview(p.getPos().x, p.getPos().y, p.getPos().z);
					}

					float iconscale = p.getScale(time);
					tempModelviewBuffer.put(0, iconscale);
					tempModelviewBuffer.put(5, -iconscale);
					tempModelviewBuffer.put(10, iconscale);

					tempModelviewBuffer.put(12, Controller.modelviewMatrix.m30);
					tempModelviewBuffer.put(13, Controller.modelviewMatrix.m31);
					tempModelviewBuffer.put(14, Controller.modelviewMatrix.m32);
					tempModelviewBuffer.rewind();
					GlUtil.glLoadMatrix(tempModelviewBuffer);
					if (selectable && ((SelectableSprite) p).isSelectable()) {
						
						Matrix4fTools.transform(Controller.modelviewMatrix, ss, outTmp);
						outTmpCp.set(outTmp);
						Matrix4fTools.transform(Controller.projectionMatrix, outTmpCp, outTmp);

						float winZ = outTmp.z / outTmp.w * 0.5f + 0.5f;

						if (getMousePosition(sprite, winX, winY, winZ)) {
							if (lastSelected != null) {
								if (lastSelected.getSelectionDepth() > ((SelectableSprite) p).getSelectionDepth()) {
									lastSelected.onUnSelect();
									((SelectableSprite) p).onSelect(winZ);
									lastSelected = ((SelectableSprite) p);
								} else {
									((SelectableSprite) p).onUnSelect();
								}
							} else {
								((SelectableSprite) p).onSelect(winZ);
								lastSelected = ((SelectableSprite) p);
							}
						} else {
							((SelectableSprite) p).onUnSelect();
						}
					}

					GL11.glDrawArrays(GL11.GL_QUADS, 0, 4);
					GlUtil.glPopMatrix();

				}
			}

			GlUtil.glPopMatrix();
			if (sprite.useShader) {
				ShaderLibrary.selectionShader.unloadWithoutExit();
			}

			// Disable Pointers
			// Disable Vertex Arrays
			GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
			// Disable TextureNew Coord Arrays
			GlUtil.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

			GlUtil.glDisableClientState(GL11.GL_NORMAL_ARRAY);
		} else {
			float wMin = 0;
			float wMax = 1;
			float hMin = 0;
			float hMax = 1;

			float width = sprite.material.getTexture().getWidth() * sprite.getScale().x;
			float height = sprite.material.getTexture().getHeight() * sprite.getScale().y;
			//			System.err.println(sprite+" has "+width+", "+height);
			GL11.glBegin(GL11.GL_QUADS);
			{
				if (sprite.positionCenter) {
					GL11.glTexCoord2f(wMin, hMin);
					GL11.glVertex3f(-width / 2, -height / 2, 0);
					GL11.glTexCoord2f(wMin, hMax);
					GL11.glVertex3f(-width / 2, height / 2, 0);
					GL11.glTexCoord2f(wMax, hMax);
					GL11.glVertex3f(width / 2, height / 2, 0);
					GL11.glTexCoord2f(wMax, hMin);
					GL11.glVertex3f(width / 2, -height / 2, 0);
				} else {
					GL11.glTexCoord2f(wMin, hMin);
					GL11.glVertex3f(0, 0, 0);
					GL11.glTexCoord2f(wMin, hMax);
					GL11.glVertex3f(0, height, 0);
					GL11.glTexCoord2f(wMax, hMax);
					GL11.glVertex3f(width, height, 0);
					GL11.glTexCoord2f(wMax, hMin);
					GL11.glVertex3f(width, 0, 0);
				}
			}
			GL11.glEnd();
		}
		sprite.material.getTexture().detach();

		GlUtil.glPopMatrix();

		//set color back to full
		GlUtil.glColor4f(1, 1, 1, 1);

		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		if (EngineSettings.G_CULLING_ACTIVE.isOn()) {
			sprite.activateCulling();
		}

	}

	public static void draw3D(Sprite sprite, PositionableSubSprite[] poses, Camera camera) {

		if (sprite.firstDraw) {
			sprite.onInit();

		}
		if (sprite.isInvisible()) {
			return;
		}

		boolean colored = poses.length > 0 && poses[0] instanceof PositionableSubColorSprite;
		boolean selectable = poses.length > 0 && poses[0] instanceof SelectableSprite;

		if (EngineSettings.G_CULLING_ACTIVE.isOn()) {
			sprite.activateCulling();
		}
//		GlUtil.glPushMatrix();

		SelectableSprite lastSelected = null;

		GlUtil.glDisable(GL11.GL_LIGHTING);
		if (sprite.depthTest) {
			GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		} else {
			GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		}

		if (sprite.blend) {
			GlUtil.glEnable(GL11.GL_BLEND);
			if (sprite.blendFunc == 0) {
				GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); // default
			} else {
//				GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE); // default
				GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
			}
			// : one
			// minus
			// source
			// alpha
		} else {
			GlUtil.glDisable(GL11.GL_BLEND);
		}
		float winX = 0;
		float winY = 0;
		if (selectable) {
			winX = Mouse.getX();
			winY = Mouse.getY();
		}
		// GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		sprite.material.getTexture().attach(0);

		// draw Sprite
		if (sprite.VBOActive && EngineSettings.G_USE_SPRITE_VBO.isOn()) {
			ShaderLibrary.selectionShader.loadWithoutUpdate();
			GlUtil.updateShaderFloat(ShaderLibrary.selectionShader, "texMult", 1);
			GlUtil.updateShaderVector4f(ShaderLibrary.selectionShader, "selectionColor", 1, 1, 1, 1);
			GlUtil.updateShaderInt(ShaderLibrary.selectionShader, "mainTexA", 0);
			// Enable Pointers
			GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY); // Enable Vertex  Arrays
			// Enable TextureNew Coord Arrays
			GlUtil.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

			// Bind Buffer To the Vertex Array
			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, sprite.VBOverts);
			// Set The Vertex Pointer To The Vertex Buffer
			GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);

			GlUtil.glActiveTexture(GL13.GL_TEXTURE0);

			if (sprite.tint != null) {
				GlUtil.glColor4f(sprite.tint.x, sprite.tint.y, sprite.tint.z, sprite.tint.w);
			} else {
				GlUtil.glColor4f(1, 1, 1, 1);
			}

			GlUtil.glPushMatrix();
			int lastMultiSprite = -1;

			Vector3f up = camera.getUp(upTmp);
			Vector3f right = camera.getRight(rightTmp);
			up.scale(0.30f);
			right.scale(0.30f);
			long time = System.currentTimeMillis();
			for (int i = 0; i < poses.length; i++) {
				PositionableSubSprite p = poses[i];

				if (!camera.isPointInFrustrum(p.getPos()) || !p.canDraw()) {
					continue;
				}
				sprite.setSelectedMultiSprite(p.getSubSprite(sprite));

				if (sprite.selectedMultiSprite != lastMultiSprite) {
					GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, sprite.VBOTexCoords.get(sprite.selectedMultiSprite));
					// Set The TexCoord Pointer To The TexCoord Buffer
					GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);
					lastMultiSprite = sprite.selectedMultiSprite;
				}
				if (colored) {
					PositionableSubColorSprite c = (PositionableSubColorSprite) p;
					GlUtil.updateShaderVector4f(ShaderLibrary.selectionShader, "selectionColor", c.getColor().x, c.getColor().y, c.getColor().z, c.getColor().w);
					GlUtil.glColor4f(c.getColor().x, c.getColor().y, c.getColor().z, c.getColor().w);
				}
//				GlUtil.glPushMatrix();
				if (!sprite.positionCenter) {
					GlUtil.translateModelview(p.getPos().x + up.x + right.x, p.getPos().y + up.y + right.y, p.getPos().z + up.z + right.z);
				} else {
					GlUtil.translateModelview(p.getPos().x, p.getPos().y, p.getPos().z);
				}

				float iconscale = p.getScale(time);
				tempModelviewBuffer.put(0, iconscale);
				tempModelviewBuffer.put(5, -iconscale);
				tempModelviewBuffer.put(10, iconscale);

				tempModelviewBuffer.put(12, Controller.modelviewMatrix.m30);
				tempModelviewBuffer.put(13, Controller.modelviewMatrix.m31);
				tempModelviewBuffer.put(14, Controller.modelviewMatrix.m32);
				tempModelviewBuffer.rewind();
				GlUtil.glLoadMatrix(tempModelviewBuffer);
				if (selectable && ((SelectableSprite) p).isSelectable()) {

					Matrix4fTools.transform(Controller.modelviewMatrix, ss, outTmp);
					outTmpCp.set(outTmp);
					Matrix4fTools.transform(Controller.projectionMatrix, outTmpCp, outTmp);
					
					
					float winZ = outTmp.z / outTmp.w * 0.5f + 0.5f;

					if (getMousePosition(sprite, winX, winY, winZ)) {
						if (lastSelected != null) {
							if (lastSelected.getSelectionDepth() > ((SelectableSprite) p).getSelectionDepth()) {
								lastSelected.onUnSelect();
								((SelectableSprite) p).onSelect(winZ);
								lastSelected = ((SelectableSprite) p);
							} else {
								((SelectableSprite) p).onUnSelect();
							}
						} else {
							((SelectableSprite) p).onSelect(winZ);
							lastSelected = ((SelectableSprite) p);
						}
					} else {
						((SelectableSprite) p).onUnSelect();
					}
				}

				GL11.glDrawArrays(GL11.GL_QUADS, 0, 4);

//				GlUtil.glPopMatrix();
			}

//			GlUtil.glPopMatrix();
			ShaderLibrary.selectionShader.unloadWithoutExit();

			// Disable Pointers
			// Disable Vertex Arrays
			GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
			// Disable TextureNew Coord Arrays
			GlUtil.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

		} else {
			float wMin = 0;
			float wMax = 1;
			float hMin = 0;
			float hMax = 1;

			float width = sprite.material.getTexture().getWidth() * sprite.getScale().x;
			float height = sprite.material.getTexture().getHeight() * sprite.getScale().y;
			//			System.err.println(sprite+" has "+width+", "+height);
			GL11.glBegin(GL11.GL_QUADS);
			{
				if (sprite.positionCenter) {
					GL11.glTexCoord2f(wMin, hMin);
					GL11.glVertex3f(-width / 2, -height / 2, 0);
					GL11.glTexCoord2f(wMin, hMax);
					GL11.glVertex3f(-width / 2, height / 2, 0);
					GL11.glTexCoord2f(wMax, hMax);
					GL11.glVertex3f(width / 2, height / 2, 0);
					GL11.glTexCoord2f(wMax, hMin);
					GL11.glVertex3f(width / 2, -height / 2, 0);
				} else {
					GL11.glTexCoord2f(wMin, hMin);
					GL11.glVertex3f(0, 0, 0);
					GL11.glTexCoord2f(wMin, hMax);
					GL11.glVertex3f(0, height, 0);
					GL11.glTexCoord2f(wMax, hMax);
					GL11.glVertex3f(width, height, 0);
					GL11.glTexCoord2f(wMax, hMin);
					GL11.glVertex3f(width, 0, 0);
				}
			}
			GL11.glEnd();
		}
		sprite.material.getTexture().detach();

		GlUtil.glPopMatrix();

		//set color back to full
		GlUtil.glColor4f(1, 1, 1, 1);

		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		if (EngineSettings.G_CULLING_ACTIVE.isOn()) {
			sprite.activateCulling();
		}

	}
	public static void draw3D(Sprite sprite, TransformableSubSprite[] poses, int arrayLength, Camera camera) {
		if (sprite.firstDraw) {
			sprite.onInit();

		}
		if (sprite.isInvisible()) {
			return;
		}

		if (EngineSettings.G_CULLING_ACTIVE.isOn()) {
			sprite.activateCulling();
		}
		boolean colored = poses.length > 0 && poses[0] instanceof PositionableSubColorSprite;
		boolean selectable = poses.length > 0 && poses[0] instanceof SelectableSprite;
		GlUtil.glPushMatrix();

		GlUtil.glDisable(GL11.GL_LIGHTING);
		if (sprite.depthTest) {
			GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		} else {
			GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		}

		if (sprite.blend) {
			GlUtil.glEnable(GL11.GL_BLEND);
			GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); // default
			// : one
			// minus
			// source
			// alpha
		} else {
			GlUtil.glDisable(GL11.GL_BLEND);
		}
		// GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		sprite.material.getTexture().attach(0);
		// draw Sprite
		if (sprite.VBOActive && EngineSettings.G_USE_SPRITE_VBO.isOn()) {
			// Enable Pointers
			GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY); // Enable Vertex  Arrays
			// Enable TextureNew Coord Arrays
			GlUtil.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

			// Bind Buffer To the Vertex Array
			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, sprite.VBOverts);
			// Set The Vertex Pointer To The Vertex Buffer
			GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);

			GlUtil.glActiveTexture(GL13.GL_TEXTURE0);

			if (sprite.tint != null) {
				GlUtil.glColor4f(sprite.tint.x, sprite.tint.y, sprite.tint.z, sprite.tint.w);
			} else {
				GlUtil.glColor4f(1, 1, 1, 1);
			}
			// Bind Buffer to the Tex Coord Array
			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, sprite.VBOTexCoords.get(sprite.selectedMultiSprite));
			// Set The TexCoord Pointer To The TexCoord Buffer
			GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);


			// Render
			// Draw All Of The Triangles At Once
			int i = 0;
			int lastMultiSprite = -1;
			if (arrayLength > 0) {
				GlUtil.glPushMatrix();

				for (int c = 0; c < arrayLength; c++) {
					GlUtil.glPushMatrix();
					TransformableSubSprite p = poses[c];

					if (colored) {
						PositionableSubColorSprite coloredSprite = (PositionableSubColorSprite) p;

						if (GlUtil.loadedShader == ShaderLibrary.selectionShader) {
							GlUtil.updateShaderFloat(ShaderLibrary.selectionShader, "texMult", 1);
							GlUtil.updateShaderVector4f(ShaderLibrary.selectionShader, "selectionColor", coloredSprite.getColor().x, coloredSprite.getColor().y, coloredSprite.getColor().z, coloredSprite.getColor().w);
						}

						GlUtil.glColor4f(coloredSprite.getColor().x, coloredSprite.getColor().y, coloredSprite.getColor().z, coloredSprite.getColor().w);

					}

					GlUtil.glMultMatrix(p.getWorldTransform());

					sprite.setSelectedMultiSprite(p.getSubSprite(sprite));

					if (sprite.selectedMultiSprite != lastMultiSprite) {
						GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, sprite.VBOTexCoords.get(sprite.selectedMultiSprite));
						// Set The TexCoord Pointer To The TexCoord Buffer
						GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);
						lastMultiSprite = sprite.selectedMultiSprite;
					}
					if (sprite.billboard) {
						FloatBuffer billboardSphericalBeginMatrix = sprite.getBillboardSphericalBeginMatrix();
						GlUtil.glLoadMatrix(billboardSphericalBeginMatrix);
					}

					GlUtil.scaleModelview(p.getScale(0), p.getScale(0), p.getScale(0));

					GL11.glDrawArrays(GL11.GL_QUADS, 0, 4);

					GlUtil.glPopMatrix();
					i++;
				}
				GlUtil.glPopMatrix();
			} else {

				GlUtil.glPushMatrix();
				if (sprite.billboard) {
					GlUtil.translateModelview(sprite.getPos());
					tempModelviewBuffer.put(0, sprite.getScale().x);
					tempModelviewBuffer.put(5, -sprite.getScale().y);
					tempModelviewBuffer.put(10, sprite.getScale().z);

					tempModelviewBuffer.put(12, Controller.modelviewMatrix.m30);
					tempModelviewBuffer.put(13, Controller.modelviewMatrix.m31);
					tempModelviewBuffer.put(14, Controller.modelviewMatrix.m32);
					tempModelviewBuffer.rewind();
					GlUtil.glLoadMatrix(tempModelviewBuffer);
				}
				GL11.glDrawArrays(GL11.GL_QUADS, 0, 4);
				GlUtil.glPopMatrix();
			}

			// Disable Pointers
			// Disable Vertex Arrays
			GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
			// Disable TextureNew Coord Arrays
			GlUtil.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

		} else {
			float wMin = 0;
			float wMax = 1;
			float hMin = 0;
			float hMax = 1;

			float width = sprite.material.getTexture().getWidth() * sprite.getScale().x;
			float height = sprite.material.getTexture().getHeight() * sprite.getScale().y;
			//			System.err.println(sprite+" has "+width+", "+height);
			GL11.glBegin(GL11.GL_QUADS);
			{
				if (sprite.positionCenter) {
					GL11.glTexCoord2f(wMin, hMin);
					GL11.glVertex3f(-width / 2, -height / 2, 0);
					GL11.glTexCoord2f(wMin, hMax);
					GL11.glVertex3f(-width / 2, height / 2, 0);
					GL11.glTexCoord2f(wMax, hMax);
					GL11.glVertex3f(width / 2, height / 2, 0);
					GL11.glTexCoord2f(wMax, hMin);
					GL11.glVertex3f(width / 2, -height / 2, 0);
				} else {
					GL11.glTexCoord2f(wMin, hMin);
					GL11.glVertex3f(0, 0, 0);
					GL11.glTexCoord2f(wMin, hMax);
					GL11.glVertex3f(0, height, 0);
					GL11.glTexCoord2f(wMax, hMax);
					GL11.glVertex3f(width, height, 0);
					GL11.glTexCoord2f(wMax, hMin);
					GL11.glVertex3f(width, 0, 0);
				}
			}
			GL11.glEnd();
		}
		sprite.material.getTexture().detach();

		GlUtil.glPopMatrix();

		//set color back to full
		GlUtil.glColor4f(1, 1, 1, 1);

		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		if (EngineSettings.G_CULLING_ACTIVE.isOn() && sprite.isFlipCulling()) {
			//reset culling
			GlUtil.glEnable(GL11.GL_CULL_FACE);
			GL11.glCullFace(GL11.GL_BACK);
		}
	}

	public static boolean getMousePosition(Sprite sprite, float winX, float winY, float winZ) {
		Matrix4f modelMatrix = Controller.modelviewMatrix;
		Matrix4f projectionMatrix = Controller.projectionMatrix;

		FloatBuffer mousePos = BufferUtils.createFloatBuffer(3);
		FloatBuffer modelViewBuffer = BufferUtils.createFloatBuffer(16);
		FloatBuffer projectionBuffer = BufferUtils.createFloatBuffer(16);

		Matrix4fTools.store(modelMatrix, modelViewBuffer);
		Matrix4fTools.store(projectionMatrix, projectionBuffer);

		modelViewBuffer.rewind(); //Rewind buffer
		projectionBuffer.rewind(); //Rewind buffer

		winY = Controller.viewport.get(3) - winY;  //Adjust Y Coordinates
		GLU.gluUnProject(winX, winY, winZ, modelViewBuffer, projectionBuffer, Controller.viewport, mousePos);
		float relX = mousePos.get(0);
		float relY = mousePos.get(1);

		if(sprite.selectionAreaLength > 0) {
			return FastMath.carmackSqrt(relX * relX + relY * relY) < sprite.selectionAreaLength;
		}

		relX += sprite.width / 2;
		relY += sprite.height / 2;
		boolean xIn = relX < sprite.width && relX > 0;
		boolean yIn = relY < sprite.height && relY > 0;
		return xIn && yIn;
	}

	public float getSelectionAreaLength() {
		return selectionAreaLength;
	}

	public void setSelectionAreaLength(float l) {
		selectionAreaLength = l;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.SceneNode#draw(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void draw() {
		GlUtil.glPushMatrix();
		if (flip) {
			GL11.glScalef(1, -1, 1);
		}
		transform();
		draw(this, 0);
		GlUtil.glPopMatrix();
	}
	public void drawRaw() {
		if(GUIElement.translateOnlyMode){
			translate();
		}else{
			GlUtil.glPushMatrix();
			if (flip) {
				GL11.glScalef(1, -1, 1);
			}
			transform();
		}
		drawRaw(this);
		if(GUIElement.translateOnlyMode){
			translateBack();
		}else{
			GlUtil.glPopMatrix();
		}
	}
	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.SceneNode#onInit(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void onInit() {
		if (!firstDraw) {
			return;
		}
		if (VBOActive) {
			//			System.err.println("initializing sprite "+this.getName()+" "+width+"x"+height);
			int sqrtX = 1;
			int sqrtY = 1;
			if (multiSpriteMax > 1) {
				//				if(multiSpriteMaxX != multiSpriteMaxY){
				//					System.err.println("UNEVEN PARTS OF SPRITE "+multiSpriteMaxX+" / "+multiSpriteMaxY);
				//				}
				sqrtX = multiSpriteMaxX;
				sqrtY = multiSpriteMaxY;
			}

			float wPart = 1f / sqrtX;
			float hPart = 1f / sqrtY;

			if (vertices == null) {
				vertices = MemoryUtil.memAllocFloat(sSizeVerts);
				texCoords = MemoryUtil.memAllocFloat(sSizeTex);
//				normals = MemoryUtil.memAllocFloat(sSizeNorm);
			} else {
				vertices.rewind();
				texCoords.rewind();
//				normals.rewind();
			}

			try {
				// Vertices
				Vector3f[] verts = new Vector3f[4];
				Vector3f[] norms = new Vector3f[4];
				if (positionCenter) {
					verts[3] = new Vector3f(-width / 2, -height / 2, 0);
					norms[3] = new Vector3f(0, 0, 1);

					verts[2] = new Vector3f(width / 2, -height / 2, 0);
					norms[2] = new Vector3f(0, 0, 1);

					verts[1] = new Vector3f(width / 2, height / 2, 0);
					norms[1] = new Vector3f(0, 0, 1);

					verts[0] = new Vector3f(-width / 2, height / 2, 0);
					norms[0] = new Vector3f(0, 0, 1);
				} else {
					verts[3] = new Vector3f(0, 0, 0);
					norms[3] = new Vector3f(0, 0, 1);

					verts[2] = new Vector3f(width, 0, 0);
					norms[2] = new Vector3f(0, 0, 1);

					verts[1] = new Vector3f(width, height, 0);
					norms[1] = new Vector3f(0, 0, 1);

					verts[0] = new Vector3f(0, height, 0);
					norms[0] = new Vector3f(0, 0, 1);
				}

				for (int i = 0; i < verts.length; i++) {
					Vector3f vert = verts[i];
					Vector3f norm = norms[i];

					vertices.put(vert.x);
					vertices.put(vert.y);
					vertices.put(vert.z);
					// Normals
//					normals.put(norm.getX());
//					normals.put(norm.getY());
//					normals.put(norm.getZ());

				}
			} catch (java.nio.BufferOverflowException e) {
				// Logger.println("BufferSize Verts "+sSizeVerts);
				// Logger.println("BufferSize TextureNew Coords "+sSizeTex);
				// Logger.println("BufferSize Norms "+sSizeNorm);
				e.printStackTrace();
			}
			// Logger.println("read "+cordCount+"/"+sSizeVerts+" Coordinates of "+mesh
			// .getFaceCount()*3+" Vertices of "+mesh.getFaceCount()+" faces");
			vertices.rewind();

//			normals.rewind();

			// mesh.vertices = hVert;

			// Generate And Bind The Vertex Buffer
			VBOverts = GL15.glGenBuffers(); // Get A Valid Name
			Controller.loadedVBOBuffers.add(VBOverts);
			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBOverts); // Bind
			// Load The Data
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);

			VBOTexCoords = MemoryUtil.memAllocInt(multiSpriteMax);
			GL15.glGenBuffers(VBOTexCoords); // Get A Valid Name
			Controller.loadedVBOBuffers.add(VBOTexCoords.get(0));
			// 3 Coords
			// Generate And Bind The TextureNew Coordinate Buffer

			int i = 0;

			for (int y = 0; y < sqrtY; y++) {
				for (int x = 0; x < sqrtX; x++) {

					Vector3f[] tex = getTexcoords(x * wPart, (x + 1) * wPart, y * hPart, (y + 1) * hPart);

					texCoords.rewind();
					// TextureNew Coordinates (only U,V needed)
					for (int t = 0; t < tex.length; t++) {
						texCoords.put(tex[t].x);
						texCoords.put(tex[t].y);
					}
					texCoords.rewind();

					GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBOTexCoords.get(i)); // Bind
					// Load The Data
					GL15.glBufferData(GL15.GL_ARRAY_BUFFER, texCoords, GL15.GL_STATIC_DRAW);
					i++;
				}

			}
			// Generate And Bind The Normal Buffer
//			VBONormals = GL15.glGenBuffers(); // Get A Valid Name
//			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBONormals); // Bind
//			Controller.loadedVBOBuffers.add(VBONormals);
			// Load The Data
//			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, normals, GL15.GL_STATIC_DRAW);

			// Our Copy Of The Data Is No Longer Necessary, It Is Safe In The
			// Graphics Card

		}
		firstDraw = false;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * @return the multiSpriteMax
	 */
	public int getMultiSpriteMax() {
		return multiSpriteMax;
	}

	/**
	 * @return the selectedMultiSprite
	 */
	public int getSelectedMultiSprite() {
		return selectedMultiSprite;
	}

	/**
	 * @param selectedMultiSprite the selectedMultiSprite to set
	 */
	public void setSelectedMultiSprite(int selectedMultiSprite) {

		assert (selectedMultiSprite < multiSpriteMax && selectedMultiSprite >= 0) : "tried to set " + selectedMultiSprite + " / " + multiSpriteMax;
		this.selectedMultiSprite = selectedMultiSprite;
	}

	private Vector3f[] getTexcoords(float wMin, float wMax, float hMin, float hMax) {

		Vector3f[] tCoords = new Vector3f[4];

		tCoords[3] = new Vector3f(wMin, hMin, 0);
		tCoords[2] = new Vector3f(wMax, hMin, 0);
		tCoords[1] = new Vector3f(wMax, hMax, 0);
		tCoords[0] = new Vector3f(wMin, hMax, 0);

		return tCoords;
	}

	public Vector4f getTint() {
		return tint;
	}

	public void setTint(Vector4f tint) {
		this.tint = tint;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * Checks if is billboard.
	 *
	 * @return true, if is billboard
	 */
	public boolean isBillboard() {
		return billboard;
	}

	/**
	 * Sets the billboard.
	 *
	 * @param billboard the new billboard
	 */
	public void setBillboard(boolean billboard) {
		this.billboard = billboard;
	}

	/**
	 * Checks if is blend.
	 *
	 * @return true, if is blend
	 */
	public boolean isBlend() {
		return blend;
	}

	/**
	 * Sets the blend.
	 *
	 * @param blend the new blend
	 */
	public void setBlend(boolean blend) {
		this.blend = blend;
	}

	/**
	 * Checks if is depth test.
	 *
	 * @return true, if is depth test
	 */
	public boolean isDepthTest() {
		return depthTest;
	}

	/**
	 * Sets the depth test.
	 *
	 * @param depthTest the new depth test
	 */
	public void setDepthTest(boolean depthTest) {
		this.depthTest = depthTest;
	}

	/**
	 * @return the flip
	 */
	public boolean isFlip() {
		return flip;
	}

	/**
	 * @param flip the flip to set
	 */
	public void setFlip(boolean flip) {
		this.flip = flip;
	}

	/**
	 * @return the orthogonal
	 */
	public boolean isOrthogonal() {
		return orthogonal;
	}

	/**
	 * @param orthogonal the orthogonal to set
	 */
	public void setOrthogonal(boolean orthogonal) {
		this.orthogonal = orthogonal;
	}

	/**
	 * Checks if is position center.
	 *
	 * @return true, if is position center
	 */
	public boolean isPositionCenter() {
		return positionCenter;
	}

	/**
	 * Sets the position center.
	 *
	 * @param positionCenter the new position center
	 */
	public void setPositionCenter(boolean positionCenter) {
		this.positionCenter = positionCenter;
	}

	/**
	 * @param multiSpriteMax the multiSpriteMax to set
	 * @param y
	 */
	public void setMultiSpriteMax(int x, int y) {
		this.multiSpriteMax = x * y;
		this.multiSpriteMaxX = x;
		this.multiSpriteMaxY = y;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.AbstractSceneNode#update(float)
	 */
	@Override
	public void update(Timer timer) {
		System.err.println("Cannot update static Sprite");

	}

	/**
	 * @return the multiSpriteMaxX
	 */
	public int getMultiSpriteMaxX() {
		return multiSpriteMaxX;
	}

	/**
	 * @return the multiSpriteMaxY
	 */
	public int getMultiSpriteMaxY() {
		return multiSpriteMaxY;
	}

}
