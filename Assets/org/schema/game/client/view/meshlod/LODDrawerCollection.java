package org.schema.game.client.view.meshlod;

import java.util.Set;

import javax.vecmath.Vector3f;

import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.AbstractSceneNode;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.forms.Sprite;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class LODDrawerCollection<E extends LODCapable> implements LODDrawerInterface<E>{

	
	public final Set<E> drawable = new ObjectOpenHashSet<E>();
	
	@Override
	public void drawInstances(Mesh m, boolean blendA) {
		
		for(LODCapable dr : drawable) {
			if(dr.canDraw()) {
				GlUtil.glPushMatrix();
	//			GlUtil.glMultMatrix(dr.getWorldTransform());
				m.getTransform().set(dr.getWorldTransform());
				AbstractSceneNode.transform(m);
				dr.getColor().w = blendA ? dr.getBlending() : (1f-dr.getBlending()); 
				GlUtil.glColor4f(dr.getColor());
				m.drawVBO();
				GlUtil.glPopMatrix();
			}
		}
	}

	@Override
	public void update(Timer timer, Vector3f camPos) {
		for(E dr : drawable) {
			dr.update(timer, camPos);
		}
	}

	@Override
	public void drawSprites(Sprite sprite, int subSpriteIndex) {
		sprite.setBillboard(true);
		Sprite.draw3D(sprite, drawable, Controller.getCamera());
	}

	public void addEntry(E c) {
		drawable.add(c);
	}
	public void removeEntry(E c) {
		drawable.remove(c);
	}

	public boolean isEmpty() {
		return drawable.isEmpty();
	}


}