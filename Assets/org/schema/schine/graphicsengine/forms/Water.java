/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>Water</H2>
 * <H3>org.schema.schine.graphicsengine.forms</H3>
 * Water.java
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
 * @copyright Copyright © 2004-2010 Robin Promesberger (schema)
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

import java.io.IOException;
import java.nio.DoubleBuffer;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;
import org.schema.common.util.data.DataUtil;
import org.schema.schine.graphicsengine.core.AbstractScene;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.FrameBufferObjects;
import org.schema.schine.graphicsengine.core.GLException;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;
import org.schema.schine.graphicsengine.texture.Texture;

/**
 * The Class Water.
 */
public class Water extends SceneNode implements Shaderable {

	public static boolean drawingRefraction = false;
	private static double plane[] = new double[]{0.0, -1.0, 0.0, 0.0};
	private static DoubleBuffer dBuffer = MemoryUtil.memAllocDouble(4);
	/**
	 * The time.
	 */
	float time;
	/**
	 * The time2.
	 */
	float time2;
	/**
	 * The watercolor.
	 */
	Vector3f watercolor;
	double plusplane[] = new double[]{0.0, 1.0, 0.0, 0.0}; // water at y=0
	/**
	 * The dudvmap.
	 */
	private int reflection, refraction, depth, normalmap, dudvmap;
	/**
	 * The size y.
	 */
	private float sizeX, sizeY;
	/**
	 * The firstdraw.
	 */
	private boolean firstdraw = true;
	/**
	 * The water reflect fbo.
	 */
	private FrameBufferObjects waterReflectFBO;
	/**
	 * The water refract fbo.
	 */
	private FrameBufferObjects waterRefractFBO;
	/**
	 * The size1.
	 */
	private float size1 = -1;
	/**
	 * The size2.
	 */
	private float size2 = -1;

	/**
	 * The water tesselate x.
	 */
	private float waterTesselateX = 1;

	/**
	 * The water tesselate y.
	 */
	private float waterTesselateY = 1;

	/**
	 * The surface.
	 */
	private int surface;

	/**
	 * The scene.
	 */
	private DrawableScene scene;

	/**
	 * The noisemap.
	 */
	private int noisemap;

	/**
	 * The seabed.
	 */
	private int seabed;

	/**
	 * Instantiates a new water.
	 *
	 * @param sizeX the size x
	 * @param sizeY the size y
	 */
	public Water(float sizeX, float sizeY) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		waterReflectFBO = new FrameBufferObjects("Water0", 128, 128);
		waterRefractFBO = new FrameBufferObjects("Water1", 128, 128);
		waterRefractFBO.setWithDepthTexture(true);
	}

	@Override
	public void cleanUp() {
		if (waterReflectFBO != null) {
			System.out.println("[CLEANUP] cleaning up Water Reflect FBO");
			waterReflectFBO.cleanUp();
		}
		if (waterRefractFBO != null) {
			System.out.println("[CLEANUP] cleaning up Water Refract FBO");
			waterRefractFBO.cleanUp();
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.SceneNode#draw(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void draw() {
		if (isInvisible()) {
			return;
		}
		GlUtil.glPushMatrix();
		GL11.glCallList(surface);
		GlUtil.glPopMatrix();
		//		GlUtil.glDisable(GL11.GL_CULL_FACE);

	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.SceneNode#onInit(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void onInit() {
		//		System.err.println("loading Water Textures");

		try {
			Texture tbottom = Controller.getTexLoader().getTexture2D(DataUtil.dataPath
					+ "/watermaps/SeaBed.jpg", true);

			seabed = tbottom.getTextureId();

			Texture tdist = Controller.getTexLoader().getTexture2D(DataUtil.dataPath
					+ "/watermaps/dudvmap.jpg", true);

			Texture tnormal = Controller.getTexLoader().getTexture2D(DataUtil.dataPath
					+ "/watermaps/perlin_noise.jpg", true); //   "/watermaps/normalmap.jpg"

			Texture tspec = Controller.getTexLoader().getTexture2D(DataUtil.dataPath
					+ "/watermaps/normalmap.jpg", true);

			noisemap = tspec.getTextureId();
			dudvmap = tdist.getTextureId();
			normalmap = tnormal.getTextureId();
		} catch (IOException e1) {

			e1.printStackTrace();
		}
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);

		// TextureNew.getEmptyTexture( texSize, texSize);

		watercolor = new Vector3f(.9f, .9f, .9f);

		surface = GL11.glGenLists(1);
		if (surface == 0) {
			System.err.println("GenList Error");
		} else {
			// System.out.println("GenList planet1:"+sphere);//ddd
			GL11.glNewList(surface, GL11.GL_COMPILE);
			{
				// GL11.glTranslatef ( 0.0f, -500.0f, 0.0f);
				GlUtil.glColor4f(1, 1, 1, 1);
				//				GL11.glCullFace(GL11.GL_FRONT);
				//				GlUtil.glEnable(GL11.GL_CULL_FACE);
				if (size1 == -1) {
					size1 = 0.0f;
					size2 = 1.0f;
				}
				GL11.glBegin(GL11.GL_QUADS);
				float leapX = sizeX / waterTesselateX;
				float leapY = sizeY / waterTesselateY;
				for (int x = 0; x < waterTesselateX; x++) {
					for (int y = 0; y < waterTesselateY; y++) {
						float fak = ((size2) / waterTesselateX * 40);

						float size1X = x * fak;
						float size1Y = y * fak;
						GL11.glNormal3f(0, 1, 0);
						GL13.glMultiTexCoord2f(GL13.GL_TEXTURE0, size1X, size1Y);

						GL13.glMultiTexCoord2f(GL13.GL_TEXTURE1, 0.0f, size2);
						GL13.glMultiTexCoord2f(GL13.GL_TEXTURE2, 0.0f, size2);
						GL13.glMultiTexCoord2f(GL13.GL_TEXTURE3, 0.0f, size2);
						GL13.glMultiTexCoord2f(GL13.GL_TEXTURE4, 0.0f, size2);
						GL13.glMultiTexCoord2f(GL13.GL_TEXTURE5, 0.0f, 1);
						GL11.glVertex3f(getPos().x + x * leapX, getPos().y, getPos().z + y
								* leapY);

						GL11.glNormal3f(0, 1, 0);
						GL13.glMultiTexCoord2f(GL13.GL_TEXTURE0, size1X + fak, size1Y);
						GL13.glMultiTexCoord2f(GL13.GL_TEXTURE1, 0.0f, size2);
						GL13.glMultiTexCoord2f(GL13.GL_TEXTURE2, 0.0f, size2);
						GL13.glMultiTexCoord2f(GL13.GL_TEXTURE3, 0.0f, size2);
						GL13.glMultiTexCoord2f(GL13.GL_TEXTURE4, 0.0f, size2);
						GL13.glMultiTexCoord2f(GL13.GL_TEXTURE5, 0.0f, 1);
						GL11.glVertex3f(getPos().x + x * leapX + leapX, getPos().y, getPos().z
								+ y * leapY);

						GL11.glNormal3f(0, 1, 0);
						GL13.glMultiTexCoord2f(GL13.GL_TEXTURE0, size1X + fak, size1Y + fak);
						GL13.glMultiTexCoord2f(GL13.GL_TEXTURE1, 0.0f, size2);
						GL13.glMultiTexCoord2f(GL13.GL_TEXTURE2, 0.0f, size2);
						GL13.glMultiTexCoord2f(GL13.GL_TEXTURE3, 0.0f, size2);
						GL13.glMultiTexCoord2f(GL13.GL_TEXTURE4, 0.0f, size2);
						GL13.glMultiTexCoord2f(GL13.GL_TEXTURE5, 0.0f, 1);
						GL11.glVertex3f(getPos().x + x * leapX + leapX, getPos().y, getPos().z
								+ y * leapY + leapY);

						GL11.glNormal3f(0, 1, 0);
						GL13.glMultiTexCoord2f(GL13.GL_TEXTURE0, size1X, size1Y + fak);
						GL13.glMultiTexCoord2f(GL13.GL_TEXTURE1, 0.0f, size2);
						GL13.glMultiTexCoord2f(GL13.GL_TEXTURE2, 0.0f, size2);
						GL13.glMultiTexCoord2f(GL13.GL_TEXTURE3, 0.0f, size2);
						GL13.glMultiTexCoord2f(GL13.GL_TEXTURE4, 0.0f, size2);
						GL13.glMultiTexCoord2f(GL13.GL_TEXTURE5, 0.0f, 1);
						GL11.glVertex3f(getPos().x + x * leapX, getPos().y, getPos().z + y
								* leapY + leapY);
					}
				}
				GL11.glEnd();
			}
			GL11.glEndList();
			//			GlUtil.glDisable(GL11.GL_CULL_FACE);
			//			GL11.glCullFace(GL11.GL_BACK);
		}
		if (scene == null) {
			throw new NullPointerException("scene null");
		}
		try {
			waterReflectFBO.initialize();
			waterRefractFBO.initialize();
		} catch (GLException e) {

			e.printStackTrace();
		}
	}

	/**
	 * Draw water.
	 *
	 * @param gl    the gl
	 * @param glu   the glu
	 * @param scene the scene
	 * @ the error diolog exception
	 */
	public void drawWater(DrawableScene scene) {
		if (!isMirrorMode()) {
			setMirrorMode(true);
			textureUpdate(scene);
			setMirrorMode(false);
			ShaderLibrary.waterShader.load();
			draw();
			ShaderLibrary.waterShader.unload();

			// System.err.println("drawing water");
		} else {

		}

	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.shader.Shaderable#onExit(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void onExit() {
		// unbind all textures
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE2);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE3);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE4);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE5);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);

	}

	// Render Scene scaled by(1.0, -1.0, 1.0) to texture

	@Override
	public void updateShader(DrawableScene scene) {

	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.shader.Shaderable#updateShaderParameters(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU, int)
	 */
	@Override
	public void updateShaderParameters(Shader shader) {

		;
		GlUtil.updateShaderVector4f(shader, "light.ambient", AbstractScene.mainLight.getAmbience());
		GlUtil.updateShaderVector4f(shader, "light.diffuse", AbstractScene.mainLight.getDiffuse());
		GlUtil.updateShaderVector4f(shader, "light.specular", AbstractScene.mainLight.getSpecular());
		GlUtil.updateShaderVector4f(shader, "light.position",
				AbstractScene.mainLight.getPos().x,
				AbstractScene.mainLight.getPos().y,
				AbstractScene.mainLight.getPos().z,
				1.0f);

		GlUtil.updateShaderFloat(shader, "shininess", AbstractScene.mainLight.getShininess()[0]);
		;

		GlUtil.updateShaderFloat(shader, "time", time);

		GlUtil.updateShaderFloat(shader, "time2", time2);

		GlUtil.updateShaderVector3f(shader, "waterColor", watercolor, 1.0f);

		// bind all textures

		GL11.glGetError();

		GlUtil.updateShaderTexture2D(shader, "water_reflection", reflection, 0);
		GlUtil.updateShaderTexture2D(shader, "water_refraction", refraction, 1);
		GlUtil.updateShaderTexture2D(shader, "water_normalmap", normalmap, 2);
		GlUtil.updateShaderTexture2D(shader, "water_dudvmap", dudvmap, 3);
		GlUtil.updateShaderTexture2D(shader, "water_depthmap", depth, 4);
		GlUtil.updateShaderTexture2D(shader, "noiseMap", noisemap, 5);

		time += 0.001f;
		time2 -= 0.002f;
	}

	/**
	 * Render reflection.
	 *
	 * @param gl    the gl
	 * @param glu   the glu
	 * @param scene the scene
	 * @ the error diolog exception
	 */
	void renderReflection(DrawableScene scene) {
		waterReflectFBO.enable();
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 1);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		//		;
		//		waterReflectFBO.checkFrameBuffer();
		GlUtil.glPushMatrix();

		GlUtil.glEnable(GL11.GL_DEPTH_TEST); // Enable Depth Testing
		//		GL11.glColorMask(true, true, true, true); // Set Color Mask to TRUE, TRUE,
		// TRUE, TRUE
		//		GL11.glStencilFunc(GL11.GL_EQUAL, 1, 1); // We Draw Only Where The Stencil
		// Is 1
		// (I.E. Where The Floor Was Drawn)
		//		GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP); // Don'transformationArray Change The
		// Stencil Buffer
		GL11.glScalef(1, -1, 1);
		dBuffer.rewind();
		dBuffer.put(plusplane);
		GL11.glClipPlane(GL11.GL_CLIP_PLANE0, dBuffer);
		GlUtil.glEnable(GL11.GL_CLIP_PLANE0);
		scene.drawScene();
		GlUtil.glDisable(GL11.GL_CLIP_PLANE0);
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		GlUtil.glPopMatrix();
		;
		waterReflectFBO.disable();

		reflection = waterReflectFBO.getTexture();

		if (EngineSettings.G_WATER_USE_MIPMAPS.isOn()) {
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, reflection);
			GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		}
	}

	/**
	 * Render refraction and depth.
	 *
	 * @param gl    the gl
	 * @param glu   the glu
	 * @param scene the scene
	 * @ the error diolog exception
	 */
	void renderRefractionAndDepth(DrawableScene scene) {
		waterRefractFBO.enable();

		GL11.glClearColor(0.6f, 0.6f, 1.0f, 1);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glClearColor(0.4f, 0.4f, 0.4f, 1);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		;

		FrameBufferObjects.checkFrameBuffer();
		// normal pointing along negative y

		GlUtil.glEnable(GL11.GL_CLIP_PLANE0);
		;
		dBuffer.rewind();
		dBuffer.put(plane);
		GL11.glClipPlane(GL11.GL_CLIP_PLANE0, dBuffer);
		;
		FrameBufferObjects.checkFrameBuffer();
		GlUtil.glPushMatrix();
		drawingRefraction = true;
		scene.drawScene();
		drawingRefraction = false;

		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, seabed);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glPushMatrix();
		GL11.glTranslatef(0.0f, -41, 0.0f);
		GL11.glCallList(surface);
		GlUtil.glPopMatrix();

		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);

		GlUtil.glPopMatrix();
		FrameBufferObjects.checkFrameBuffer();
		GlUtil.glDisable(GL11.GL_CLIP_PLANE0);

		;
		waterRefractFBO.disable();

		refraction = waterRefractFBO.getTexture();
		depth = waterRefractFBO.getDepthTextureID();

		if (EngineSettings.G_WATER_USE_MIPMAPS.isOn()) {
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, depth);
			GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, refraction);
			GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		}

	}

	/**
	 * TextureNew update.
	 *
	 * @param gl    the gl
	 * @param glu   the glu
	 * @param scene the scene
	 * @ the error diolog exception
	 */
	public void textureUpdate(DrawableScene scene) {
		GlUtil.glPushMatrix();
		if (EngineSettings.F_FRAME_BUFFER.isOn()) {
			scene.getFbo().disable();
		}
		if (firstdraw) {
			this.scene = scene;
			onInit();
			firstdraw = false;
		}

		renderReflection(scene);
		renderRefractionAndDepth(scene);
		if (EngineSettings.F_FRAME_BUFFER.isOn()) {
			scene.getFbo().enable();
		}
		GlUtil.glPopMatrix();
	}

}
