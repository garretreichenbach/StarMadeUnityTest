package org.schema.game.client.view.cubes;

import java.util.Collection;

import org.lwjgl.opengl.GL11;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.GlUtil;

public class CubeBruteCollectionDrawer implements Drawable {

	public Collection<Long> drawCollection;
	public SimpleTransformableSendableObject obj;

	private int diaplayListIndex;

	private boolean generated;

	@Override
	public void cleanUp() {
		
	}

	@Override
	public void draw() {
		if (drawCollection == null) {
			return;
		}

		if (!generated) {
			generateDisplayList();
		}
		GlUtil.glPushMatrix();
		GlUtil.glMultMatrix(obj.getWorldTransform());
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);

		GlUtil.glColor4f(1, 1, 1, 1);
		assert (generated);
		GL11.glCallList(diaplayListIndex);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);

		GlUtil.glColor4f(1, 1, 1, 1);
		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);

		GlUtil.glPopMatrix();
	}

	@Override
	public boolean isInvisible() {
				return false;
	}

	@Override
	public void onInit() {
		
	}

	protected void generateDisplayList() {
		// create one display list
		if (diaplayListIndex != 0) {
			GL11.glDeleteLists(this.diaplayListIndex, 1);
		}
		this.diaplayListIndex = GL11.glGenLists(1);
		// compile the display list, store a triangle in it
		GL11.glNewList(diaplayListIndex, GL11.GL_COMPILE);

		GL11.glBegin(GL11.GL_POINTS);
		Vector3i v = new Vector3i();
		float s = 0.5f;
		for (long l : drawCollection) {
			ElementCollection.getPosFromIndex(l, v);
			v.sub(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF);
			GL11.glVertex3f(v.x, v.y, v.z);
			//			GL11.glVertex3f(v.x-s, v.y+s, v.z-s);
			//			GL11.glVertex3f(v.x+s, v.y+s, v.z-s);
			//			GL11.glVertex3f(v.x+s, v.y+s, v.z+s);
			//			GL11.glVertex3f(v.x-s, v.y+s, v.z+s);
			//
			//			GL11.glVertex3f(v.x-s, v.y-s, v.z+s);
			//			GL11.glVertex3f(v.x+s, v.y-s, v.z+s);
			//			GL11.glVertex3f(v.x+s, v.y-s, v.z-s);
			//			GL11.glVertex3f(v.x-s, v.y-s, v.z-s);
			//
			//			GL11.glVertex3f(v.x+s, v.y-s, v.z-s);
			//			GL11.glVertex3f(v.x+s, v.y+s, v.z-s);
			//			GL11.glVertex3f(v.x+s, v.y+s, v.z+s);
			//			GL11.glVertex3f(v.x+s, v.y-s, v.z+s);
			//
			//			GL11.glVertex3f(v.x-s, v.y-s, v.z+s);
			//			GL11.glVertex3f(v.x-s, v.y+s, v.z+s);
			//			GL11.glVertex3f(v.x-s, v.y+s, v.z-s);
			//			GL11.glVertex3f(v.x-s, v.y-s, v.z-s);
			//
			//			GL11.glVertex3f(v.x-s, v.y-s, v.z+s);
			//			GL11.glVertex3f(v.x+s, v.y-s, v.z+s);
			//			GL11.glVertex3f(v.x+s, v.y+s, v.z+s);
			//			GL11.glVertex3f(v.x-s, v.y+s, v.z+s);
			//
			//			GL11.glVertex3f(v.x-s, v.y+s, v.z-s);
			//			GL11.glVertex3f(v.x+s, v.y+s, v.z-s);
			//			GL11.glVertex3f(v.x+s, v.y-s, v.z-s);
			//			GL11.glVertex3f(v.x-s, v.y-s, v.z-s);

		}

		GL11.glEnd();
		GL11.glEndList();

		generated = true;
	}

	public void updateCollection(Collection<Long> drawCollection, SimpleTransformableSendableObject obj) {
		this.drawCollection = drawCollection;
		this.obj = obj;
		generated = false;
	}
}
