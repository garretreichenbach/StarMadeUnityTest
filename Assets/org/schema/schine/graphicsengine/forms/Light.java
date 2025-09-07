/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>Light</H2>
 * <H3>org.schema.schine.graphicsengine.forms</H3>
 * Light.java
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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.nio.FloatBuffer;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;
import org.schema.schine.graphicsengine.core.GlUtil;

/**
 * The Class Light.
 */
public class Light extends SceneNode implements KeyListener {
	/**
	 * The light num gen.
	 */
	private static int lightNumGen;
	/**
	 * Attach.
	 *
	 * @param gl the gl
	 * @param glu the glu
	 */
	private static float[] temp = new float[4];
	public static final FloatBuffer tempBuffer = MemoryUtil.memAllocFloat(4);
	private static Vector3f spotDirDefault;
	private static boolean defaultValues;
	private static float spotCutOffDefault;
	private static float spotExponentDefault;
	private static float constAttenuationDefault;
	private static float linearAttenuationDefault;
	private static float quadAttenuationDefault;
	public Vector3f spotDirection = new Vector3f();
	public float constAttenuation;
	public float linearAttenuation;
	public float quadAttenuation;
	public Vector3f spotUp = new Vector3f();
	float lightDir[] = new float[]{0.0f, 1.0f, 0.0f, 0.0f};
	/**
	 * The light num.
	 */
	private int lightNum;
	private Vector4f ambience;
	private Vector4f diffuse;
	private Vector4f specular;
	private float spotCutoff = 45;
	private float spotExponent = 4;
	/**
	 * The shininess.
	 */
	private float[] shininess;
	private boolean init;

	/**
	 * Instantiates a new light.
	 */
	public Light() {
		reassign();
		
		init();
	}
	private void init(){
		ambience = new Vector4f(.2f, .2f, .2f, 1f);
		diffuse = new Vector4f(.6f, .6f, .6f, 1f);
		specular = new Vector4f(.9f, .9f, .9f, 1f);
		getPos().set(0.0f, 0.0f, 0f);

		shininess = new float[] {32.0f};
	}
	public Light(int assign) {
		this.lightNum = assign;
		init();
	}
	public void setExtraValuesToDefault(){
		spotDirection.set(spotDirDefault);
		spotCutoff = spotCutOffDefault;
		spotExponent = spotExponentDefault;
		constAttenuation = constAttenuationDefault;
		linearAttenuation = linearAttenuationDefault;
		quadAttenuation = quadAttenuationDefault;
	}

	public void reassign() {
		if (lightNumGen > 7) {
			throw new IllegalArgumentException("too many Lights in scene");
		}

		lightNum = lightNumGen++;
	}

	@Override
	public void cleanUp() {
		GlUtil.glDisable(getLightID());
		lightNumGen--;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.SceneNode#draw(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void draw() {
		if(!init){
			onInit();
		}
		GlUtil.glPushMatrix();
		attach();
		GlUtil.glPopMatrix();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.SceneNode#onInit(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void onInit() {
		if(!Light.defaultValues){
			GL11.glGetLightfv(GL11.GL_LIGHT0, GL11.GL_SPOT_DIRECTION, tempBuffer);
			tempBuffer.rewind();
			Light.spotDirDefault = new Vector3f(tempBuffer.get(0), tempBuffer.get(1), tempBuffer.get(2));
//			Light.spotCutOffDefault = GL11.glGetLight(GL11.GL_LIGHT0, GL11.GL_SPOT_CUTOFF);
//			Light.spotExponentDefault = GL11.glGetLight(GL11.GL_LIGHT0, GL11.GL_SPOT_EXPONENT);
//			Light.constAttenuationDefault = GL11.glGetLight(GL11.GL_LIGHT0, GL11.GL_CONSTANT_ATTENUATION);
//			Light.linearAttenuationDefault = GL11.glGetLight(GL11.GL_LIGHT0, GL11.GL_LINEAR_ATTENUATION);
//			Light.quadAttenuationDefault = GL11.glGetLightf(GL11.GL_LIGHT0, GL11.GL_QUADRATIC_ATTENUATION);
			
			tempBuffer.rewind(); GL11.glGetLightfv(GL11.GL_LIGHT0, GL11.GL_SPOT_CUTOFF, tempBuffer); Light.spotCutOffDefault = tempBuffer.get(0);
			tempBuffer.rewind(); GL11.glGetLightfv(GL11.GL_LIGHT0, GL11.GL_SPOT_EXPONENT, tempBuffer); Light.spotExponentDefault = tempBuffer.get(0);
			tempBuffer.rewind(); GL11.glGetLightfv(GL11.GL_LIGHT0, GL11.GL_CONSTANT_ATTENUATION, tempBuffer); Light.constAttenuationDefault = tempBuffer.get(0);
			tempBuffer.rewind(); GL11.glGetLightfv(GL11.GL_LIGHT0, GL11.GL_LINEAR_ATTENUATION, tempBuffer); Light.linearAttenuationDefault = tempBuffer.get(0);
			tempBuffer.rewind(); GL11.glGetLightfv(GL11.GL_LIGHT0, GL11.GL_QUADRATIC_ATTENUATION, tempBuffer); Light.quadAttenuationDefault = tempBuffer.get(0);
			Light.defaultValues = true;
		}
		
		setExtraValuesToDefault();
		init = true;
	}

	public void attachSimple() {
		getPos().get(temp);
		temp[3] = 1;
		tempBuffer.rewind();
		tempBuffer.put(temp);
		tempBuffer.rewind();
		GL11.glLightfv(getLightID(), GL11.GL_POSITION, tempBuffer);
		
		ambience.get(temp);
		tempBuffer.rewind();
		tempBuffer.put(temp);
		tempBuffer.rewind();
		GL11.glLightfv(getLightID(), GL11.GL_AMBIENT, tempBuffer);

		diffuse.get(temp);
		tempBuffer.rewind();
		tempBuffer.put(temp);
		tempBuffer.rewind();
		GL11.glLightfv(getLightID(), GL11.GL_DIFFUSE, tempBuffer);
		
		specular.get(temp);
		tempBuffer.rewind();
		tempBuffer.put(temp);
		tempBuffer.rewind();
		GL11.glLightfv(getLightID(), GL11.GL_SPECULAR, tempBuffer);
		//		GL11.glLightfv(getLightID(), GL11.GL_SHININESS, shininess, 0);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glEnable(getLightID());
	}
	private void attach() {

		ambience.get(temp);
		tempBuffer.rewind();
		tempBuffer.put(temp);
		tempBuffer.rewind();
		GL11.glLightfv(getLightID(), GL11.GL_AMBIENT, tempBuffer);

		diffuse.get(temp);
		tempBuffer.rewind();
		tempBuffer.put(temp);
		tempBuffer.rewind();
		GL11.glLightfv(getLightID(), GL11.GL_DIFFUSE, tempBuffer);

		getPos().get(temp);
		temp[3] = 1;
		tempBuffer.rewind();
		tempBuffer.put(temp);
		tempBuffer.rewind();
		GL11.glLightfv(getLightID(), GL11.GL_POSITION, tempBuffer);


		specular.get(temp);
		tempBuffer.rewind();
		tempBuffer.put(temp);
		tempBuffer.rewind();
		GL11.glLightfv(getLightID(), GL11.GL_SPECULAR, tempBuffer);
		
		spotDirection.get(temp);
		temp[3] = 1;
		tempBuffer.rewind();
		tempBuffer.put(temp);
		tempBuffer.rewind();
		GL11.glLightfv(getLightID(), GL11.GL_SPOT_DIRECTION, tempBuffer);

		GL11.glLightf(getLightID(), GL11.GL_SPOT_CUTOFF, spotCutoff);

		GL11.glLightf(getLightID(), GL11.GL_SPOT_EXPONENT, spotExponent);

		//attenuation simulates fading with distance
		GL11.glLightf(getLightID(), GL11.GL_CONSTANT_ATTENUATION, constAttenuation);
		GL11.glLightf(getLightID(), GL11.GL_LINEAR_ATTENUATION, linearAttenuation);
		GL11.glLightf(getLightID(), GL11.GL_QUADRATIC_ATTENUATION, quadAttenuation);

		//		GL11.glLightfv(getLightID(), GL11.GL_SHININESS, shininess, 0);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glEnable(getLightID());
	}

	/**
	 * @return the ambience
	 */
	public Vector4f getAmbience() {
		return ambience;
	}

	/**
	 * @param ambience the ambience to set
	 */
	public void setAmbience(Vector4f ambience) {
		this.ambience = ambience;
	}

	/**
	 * @return the diffuse
	 */
	public Vector4f getDiffuse() {
		return diffuse;
	}

	/**
	 * @param diffuse the diffuse to set
	 */
	public void setDiffuse(Vector4f diffuse) {
		this.diffuse = diffuse;
	}

	/**
	 * Gets the light id.
	 *
	 * @return the light id
	 */
	private int getLightID() {
		return switch(lightNum) {
			case (0) -> GL11.GL_LIGHT0;
			case (1) -> GL11.GL_LIGHT1;
			case (2) -> GL11.GL_LIGHT2;
			case (3) -> GL11.GL_LIGHT3;
			case (4) -> GL11.GL_LIGHT4;
			case (5) -> GL11.GL_LIGHT5;
			case (6) -> GL11.GL_LIGHT6;
			case (7) -> GL11.GL_LIGHT7;
			default -> 0;
		};
	}

	/**
	 * Gets the shininess.
	 *
	 * @return the shininess
	 */
	public float[] getShininess() {
		return shininess;
	}

	/**
	 * Sets the shininess.
	 *
	 * @param shininess the new shininess
	 */
	public void setShininess(float[] shininess) {
		this.shininess = shininess;
	}

	/**
	 * @return the specular
	 */
	public Vector4f getSpecular() {
		return specular;
	}

	/**
	 * @param specular the specular to set
	 */
	public void setSpecular(Vector4f specular) {
		this.specular = specular;
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		float mult = 100;
		if (e.isShiftDown()) {
			mult = 1000;
		}
		if (e.isControlDown()) {
			mult = 0.5f;
		}
		switch(e.getKeyCode()) {
			case (KeyEvent.VK_UP) -> getPos().z += mult;
			case (KeyEvent.VK_DOWN) -> getPos().z -= mult;
			case (KeyEvent.VK_LEFT) -> getPos().x += mult;
			case (KeyEvent.VK_RIGHT) -> getPos().x -= mult;
			case (KeyEvent.VK_PAGE_UP) -> getPos().y += mult;
			case (KeyEvent.VK_PAGE_DOWN) -> getPos().y -= mult;
		}

	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	public void deactivate() {
		GlUtil.glDisable(getLightID());
	}
	public static void resetLightAssignment() {
		lightNumGen = 0;
	}

}
