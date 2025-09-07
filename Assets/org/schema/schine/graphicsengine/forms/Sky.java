/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>Sky</H2>
 * <H3>org.schema.schine.graphicsengine.forms</H3>
 * Sky.java
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

import java.io.IOException;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.schema.common.util.data.DataUtil;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.Shaderable;
import org.schema.schine.graphicsengine.texture.Texture;

/**
 * The Class Sky.
 */
public class Sky extends SceneNode implements Shaderable {

	/**
	 * The first draw.
	 */
	boolean firstDraw = true;

	/**
	 * The time.
	 */
	private float time;

	/**
	 * The sky tex.
	 */
	private int skyTex;

	/**
	 * The sky normal.
	 */
	private int skyNormal;

	/**
	 * The lightpos.
	 */
	private Vector3f lightpos = new Vector3f();

	/**
	 * The sharp.
	 */
	private float sharp;

	/**
	 * The cover.
	 */
	private float cover;

	/**
	 * Instantiates a new sky.
	 */
	public Sky() {
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.SceneNode#draw(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void draw() {
		if (isInvisible()) {
			return;
		}
		//		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		if (firstDraw) {
			onInit();
			firstDraw = false;
		}
		GlUtil.glPushMatrix();
		;
		// GLUT glut = new GLUT();
		transform();
		;
		//		ShaderLibrary.skyShader2.load();
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		Controller.getResLoader().getMesh("Sky").getMaterial().setTexture(null);
		Controller.getResLoader().getMesh("Sky").draw();
		;
		//		ShaderLibrary.skyShader2.unload();
		GlUtil.glPopMatrix();
		;
		//		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.SceneNode#onInit(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void onInit() {
		sharp = 0.0000005f;
		cover = 0.7f;
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		try {
			Texture tdist;
			tdist = Controller.getTexLoader().getTexture2D(DataUtil.dataPath
					+ "/image-resource/detail.png", true);
			Texture tnormal = Controller.getTexLoader().getTexture2D(DataUtil.dataPath
					+ "/image-resource/detail_normal.png", true);//
			skyTex = tdist.getTextureId();
			skyNormal = tnormal.getTextureId();
		} catch (IOException e) {

			e.printStackTrace();
		}
		//				ShaderLibrary.skyShader2.setShaderInterface(this);

	}

	/**
	 * Gets the cover.
	 *
	 * @return the cover
	 */
	public float getCover() {
		return cover;
	}

	/**
	 * Sets the cover.
	 *
	 * @param cover the cover to set
	 */
	public void setCover(float cover) {
		this.cover = cover;
	}

	/**
	 * Gets the sharp.
	 *
	 * @return the sharp
	 */
	public float getSharp() {
		return sharp;
	}

	/**
	 * Sets the sharp.
	 *
	 * @param sharp the sharp to set
	 */
	public void setSharp(float sharp) {
		this.sharp = sharp;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.shader.Shaderable#onExit(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void onExit() {
		;
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		//		GlUtil.glActiveTexture(GL13.GL_TEXTURE2);
		//		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		//		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		;
	}

	/**
	 * Update shader.
	 *
	 * @param scene the scene
	 */
	@Override
	public void updateShader(DrawableScene scene) {
		lightpos.x = (scene.getLight().getPos().x);
		lightpos.y = (scene.getLight().getPos().y);
		lightpos.z = (scene.getLight().getPos().z);
		time += 0.0001f;
		if (!AbstractSceneNode.isMirrorMode()) {
			setPos(Controller.getCamera().getPos().x, Controller.getCamera().getPos().y, Controller.getCamera().getPos().z);
		} else {
			setPos(getInitionPos());
		}

	}

	/**
	 * uniform sampler2D tex;
	 * uniform sampler2D nmap;
	 * uniform float cSharp;
	 * uniform float cCover;
	 * uniform float cMove;
	 * uniform vec4 lightPos;.
	 *
	 * @param gl            the gl
	 * @param glu           the glu
	 * @param shaderprogram the shaderprogram
	 */
	@Override
	public void updateShaderParameters(Shader shader) {
		;
		GlUtil.updateShaderFloat(shader, "cSharp", sharp);
		GlUtil.updateShaderFloat(shader, "cCover", cover);
		GlUtil.updateShaderFloat(shader, "cMove", time);
		GlUtil.updateShaderVector3f(shader, "lightPos", lightpos, 1);
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);

		int old = 0;

		GlUtil.updateShaderTexture2D(shader, "tex", skyTex, 0);

		GlUtil.updateShaderTexture2D(shader, "nmap", skyNormal, 0);
		//		old = GL20.glGetUniformLocation(shader, "nmap");
		//		if (old >= 0) {
		//			GlUtil.glActiveTexture(GL13.GL_TEXTURE2);
		//			GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		//			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, skyNormal);
		//			GL20.glUniform1i(old, 2);
		//		} else {
		//			System.err.println("tex " + old + " " + skyNormal);
		//		}

		;
	}

}
