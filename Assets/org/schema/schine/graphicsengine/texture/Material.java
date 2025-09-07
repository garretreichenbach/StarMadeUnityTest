/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>Material</H2>
 * <H3>org.schema.schine.graphicsengine.texture</H3>
 * Material.java
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
package org.schema.schine.graphicsengine.texture;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.system.MemoryUtil;
import org.schema.schine.graphicsengine.core.AbstractScene;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.Shaderable;

/**
 * The Class Material.
 */
public class Material implements Shaderable {

	/**
	 * The ambient.
	 */
	private static FloatBuffer ambientBuffer = MemoryUtil.memAllocFloat(4);
	/**
	 * The diffuse.
	 */
	private static FloatBuffer diffuseBuffer = MemoryUtil.memAllocFloat(4);
	/**
	 * The specular.
	 */
	private static FloatBuffer specularBuffer = MemoryUtil.memAllocFloat(4);
	private static FloatBuffer shineBuffer = MemoryUtil.memAllocFloat(1);
	/**
	 * The ambient.
	 */
	private float[] ambient = {0.3f, 0.3f, 0.3f, 1.0f};
	/**
	 * The diffuse.
	 */
	private float[] diffuse = {0.6f, 0.6f, 0.6f, 1.0f};
	/**
	 * The specular.
	 */
	private float[] specular = {0.8f, 0.8f, 0.8f, 1.0f};
	/**
	 * The shine.
	 */
	private float[] shine = {10};
	/**
	 * The material textured.
	 */
	private boolean materialTextured = false;

	/**
	 * The material bump mapped.
	 */
	private boolean materialBumpMapped = false;

	/**
	 * The texture.
	 */
	private Texture texture;

	/**
	 * The description.
	 */
	private String name;

	/**
	 * The texture file.
	 */
	private String textureFile;

	/**
	 * The normal map.
	 */
	private Texture normalMap;

	/**
	 * The specular map.
	 */
	private Texture specularMap;

	/**
	 * The specular mapped.
	 */
	private boolean specularMapped;

	private int texturesUsed;

	private String emissiveTextureFile;

	private Texture emissiveTexture;
	private String normalTextureFile;
	public String texturePathFull;
	public String normalTexturePathFull;
	public String specularTexturePathFull;
	public String emissiveTexturePathFull;

	/**
	 * Instantiates a new material.
	 */
	public Material() {

	}

	/**
	 * Attach.
	 *
	 * @param gl     the gl
	 * @param glu    the glu
	 * @param filter the filter
	 */
	public void attach(int filter) {

		ambientBuffer.rewind();
		diffuseBuffer.rewind();
		specularBuffer.rewind();
		shineBuffer.rewind();

		ambientBuffer.put(ambient);
		diffuseBuffer.put(diffuse);
		specularBuffer.put(specular);
		shineBuffer.put(shine);

		ambientBuffer.rewind();
		diffuseBuffer.rewind();
		specularBuffer.rewind();
		shineBuffer.rewind();

		GL11.glMaterialfv(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT, ambientBuffer);
		GL11.glMaterialfv(GL11.GL_FRONT_AND_BACK, GL11.GL_DIFFUSE, diffuseBuffer);
		GL11.glMaterialfv(GL11.GL_FRONT_AND_BACK, GL11.GL_SPECULAR, specularBuffer);
		GL11.glMaterialf(GL11.GL_FRONT_AND_BACK, GL11.GL_SHININESS, shineBuffer.get(0));

//		if(EngineSettings.G_NORMAL_MAPS_ACTIVE.isOn() && !AbstractSceneNode.isMirrorMode() && materialTextured && isMaterialBumpMapped() && isSpecularMapped()){
//			//			if(Water.isMirrorMode()){
//			//				System.err.println("activated bump in mirrror mode "+getName()+" "+normalMap.getPath()[0]);
//			//			}
//			ShaderLibrary.bumpShader.setShaderInterface(this);
//			try {
//				ShaderLibrary.bumpShader.load();
//			} catch (ErrorDialogException e) {
//				GLFrame.processErrorDialogException(e);
//				e.printStackTrace();
//			}
//		}
//		else 
		if (materialTextured) {
			// System.err.println("attaching with tex "+description);
			if (texture == null) {
				throw new IllegalArgumentException("no texture loaded for "
						+ name + " but sould be " + textureFile);
			}
			texture.attach(filter);
		}

	}

	/**
	 * Clean up.
	 *
	 * @param gl the gl
	 */
	public void cleanUp() {
		if (texture != null) {
			texture.cleanUp();
		}
		if (materialBumpMapped) {
			normalMap.cleanUp();
		}
		if (specularMapped) {
			specularMap.cleanUp();
		}
	}

	/**
	 * Detach.
	 *
	 * @param gl  the gl
	 * @param glu the glu
	 */
	public void detach() {
//		if(EngineSettings.G_NORMAL_MAPS_ACTIVE.isOn() && !AbstractSceneNode.isMirrorMode() && materialTextured && isMaterialBumpMapped() && isSpecularMapped()){
//			Shader bumpShader = ShaderLibrary.bumpShader;
//			bumpShader.unload();
//		}
	}

	/**
	 * Gets the ambient.
	 *
	 * @return the ambient
	 */
	public float[] getAmbient() {
		return ambient;
	}

	/**
	 * Sets the ambient.
	 *
	 * @param lightAmbient the new ambient
	 */
	public void setAmbient(float[] lightAmbient) {
		this.ambient = lightAmbient;
	}

	/**
	 * Gets the diffuse.
	 *
	 * @return the diffuse
	 */
	public float[] getDiffuse() {
		return diffuse;
	}

	/**
	 * Sets the diffuse.
	 *
	 * @param lightDiffuse the new diffuse
	 */
	public void setDiffuse(float[] lightDiffuse) {
		this.diffuse = lightDiffuse;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the normal map.
	 *
	 * @return the normal map
	 */
	public Texture getNormalMap() {
		return normalMap;
	}

	/**
	 * Sets the normal map.
	 *
	 * @param normalMap the new normal map
	 */
	public void setNormalMap(Texture normalMap) {
		this.normalMap = normalMap;

		if (normalMap != null) {
			texturesUsed++;
			materialBumpMapped = true;
		}
	}

	/**
	 * Gets the specular.
	 *
	 * @return the specular
	 */
	public float[] getSpecular() {
		return specular;
	}

	/**
	 * Sets the specular.
	 *
	 * @param lightPosition the new specular
	 */
	public void setSpecular(float[] lightPosition) {
		this.specular = lightPosition;
	}

	/**
	 * Gets the specular map.
	 *
	 * @return the specular map
	 */
	public Texture getSpecularMap() {
		return specularMap;
	}

	/**
	 * Sets the specular map.
	 *
	 * @param specularMap the new specular map
	 */
	public void setSpecularMap(Texture specularMap) {
		this.specularMap = specularMap;

		if (specularMap != null) {
			texturesUsed++;
			specularMapped = true;
		}
	}

	/**
	 * Gets the texture.
	 *
	 * @return the texture
	 */
	public Texture getTexture() {
		return texture;
	}

	/**
	 * Sets the texture.
	 *
	 * @param texture the new texture
	 */
	public void setTexture(Texture texture) {
		if (texture == null) {
			setMaterialTextured(false);
		} else {
			setMaterialTextured(true);
		}
		this.texture = texture;

	}

	/**
	 * Gets the texture file.
	 *
	 * @return the textureFile
	 */
	public String getTextureFile() {
		return textureFile;
	}

	/**
	 * Sets the texture file.
	 *
	 * @param trim the new texture file
	 */
	public void setTextureFile(String trim) {
		this.textureFile = trim;

	}

	/**
	 * @return the texturesUsed
	 */
	public int getTexturesUsed() {
		return texturesUsed;
	}

	/**
	 * @param texturesUsed the texturesUsed to set
	 */
	public void setTexturesUsed(int texturesUsed) {
		this.texturesUsed = texturesUsed;
	}

	/**
	 * Checks if is material bump mapped.
	 *
	 * @return true, if is material bump mapped
	 */
	public boolean isMaterialBumpMapped() {
		return materialBumpMapped;
	}

	/**
	 * Sets the material bump mapped.
	 *
	 * @param materialBumpMapped the new material bump mapped
	 */
	public void setMaterialBumpMapped(boolean materialBumpMapped) {
		this.materialBumpMapped = materialBumpMapped;
	}

	/**
	 * Checks if is material textured.
	 *
	 * @return true, if is material textured
	 */
	public boolean isMaterialTextured() {
		return materialTextured;
	}

	/**
	 * Sets the material textured.
	 *
	 * @param materialTextured the new material textured
	 */
	public void setMaterialTextured(boolean materialTextured) {
		if (materialTextured) {
			texturesUsed++;
		}
		this.materialTextured = materialTextured;
	}

	/**
	 * Checks if is specular mapped.
	 *
	 * @return true, if is specular mapped
	 */
	public boolean isSpecularMapped() {
		return specularMapped;
	}

	/**
	 * Sets the specular mapped.
	 *
	 * @param specularMapped the new specular mapped
	 */
	public void setSpecularMapped(boolean specularMapped) {
		this.specularMapped = specularMapped;
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

		if (materialBumpMapped) {
			GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
			GlUtil.glDisable(GL11.GL_TEXTURE_2D);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		}
		if (specularMapped) {
			GlUtil.glActiveTexture(GL13.GL_TEXTURE2);
			GlUtil.glDisable(GL11.GL_TEXTURE_2D);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		}

		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
	}

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

		GlUtil.updateShaderFloat(shader, "shininess", 2);
		;

		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, texture.getTextureId());
		GlUtil.updateShaderInt(shader, "diffuseTexture", 0);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, normalMap.getTextureId());
		GlUtil.updateShaderInt(shader, "specularMap", 1);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE2);
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, specularMap.getTextureId());
		GlUtil.updateShaderInt(shader, "normalMap", 2);
		//		;
		GlUtil.glActiveTexture(GL13.GL_TEXTURE3);
		;

	}

	/**
	 * Sets the shininess.
	 *
	 * @param shine the new shininess
	 */
	public void setShininess(float shine) {
		this.shine[0] = shine;

	}

	/**
	 * @return the emissiveTextureFile
	 */
	public String getEmissiveTextureFile() {
		return emissiveTextureFile;
	}

	public void setEmissiveTextureFile(String textureName) {
		emissiveTextureFile = textureName;
	}

	/**
	 * @return the emissiveTexture
	 */
	public Texture getEmissiveTexture() {
		return emissiveTexture;
	}

	public void setEmissiveTexture(Texture texture2d) {
		emissiveTexture = texture2d;
	}

	public void setNormalTextureFile(String textureName) {
		normalTextureFile = textureName;
	}

	public String getNormalTextureFile() {
		return normalTextureFile;
	}

}
