package org.schema.game.client.view.meshlod;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.texture.Texture;

public abstract class LODMesh<E extends LODCapable> {
	
	public final float maxDistance;
	public final int lodIndex;
	
	public LODMesh(int lodIndex, float maxDistance) {
		this.lodIndex = lodIndex;
		this.maxDistance = maxDistance;
	}
	
	public abstract void loadResourcesA();
	public abstract void loadResourcesB();

	public abstract void unloadResourcesA();
	public abstract void unloadResourcesB();
	
	public final void update(Timer timer, LODDrawerInterface<E> d, Vector3f camPos) {
		d.update(timer, camPos);
	}
	public abstract void drawA(LODDrawerInterface<E> d);
	public abstract void drawB(LODDrawerInterface<E> d);

	public abstract boolean isDeferred() ;


	public abstract Sprite getDefferredSprite();


	public void beforeDraw(LODDrawerCollection<E> lc) {
//		System.err.println("OBJECTS IN LOD<"+lodIndex+" "+getClass().getSimpleName()+">: "+lc.drawable.size());
//		Iterator<E> iterator = lc.drawable.iterator();
//		while(iterator.hasNext()) {
//			if(!iterator.next().isAlive()) {
//				iterator.remove();
//			}
//		}
	}
	protected void loadShader(Texture texture, Shader shader) {
		shader.loadWithoutUpdate();
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, texture.getTextureId());
		GlUtil.updateShaderInt(shader, "diffuseMap", 0);
		GlUtil.updateShaderVector4f(shader, "tint", 1,1,1,1);
		
//		GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
//		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
//		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, emissiveId);
//		GlUtil.updateShaderInt(shader, "emissiveMap", 1);
	}
	protected void unloadShader(Texture texture, Shader shader) {
		shader.unloadWithoutExit();
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}
	public void afterDraw(LODDrawerCollection<E> lc) {
		
	}

	public abstract boolean isBlending();

	public abstract boolean isDeferredA();
	public abstract boolean isDeferredB();

	public abstract boolean isDrawA();
	public abstract boolean isDrawB();
}
