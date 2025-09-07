package org.schema.game.common.data;

import com.bulletphysics.linearmath.Transform;
import org.lwjgl.opengl.GL11;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.GlUtil;

import javax.vecmath.Vector3f;

public class Sphere implements Drawable {

	private static final int SIDES = 20;
	private static final float[][] tc = {{0, 0}, {0, 1}, {1, 1}, {1, 0}, {0.5f, 0.5f}};

	private final Vector3f[][] poly = new Vector3f[SIDES][3];
	private final float radius;
	private int dList;

	public Sphere(float radius) {
		this.radius = radius;
	}

	@Override
	public void onInit() {
		for(int i = 0; i < SIDES; i++) {
			for(int j = 0; j < 3; j++) {
				poly[i][j] = new Vector3f();
			}
		}
	}

	@Override
	public void draw() {
		if(dList == 0) {
			dList = GL11.glGenLists(1);
			GL11.glNewList(dList, GL11.GL_COMPILE);
			for(int i = 0; i < SIDES; i++) polygonDraw(i);
			GL11.glEndList();
		}
		GL11.glCallList(dList);
		GlUtil.glColor4f(1, 1, 1, 1);
	}

	@Override
	public boolean isInvisible() {
		return false;
	}

	@Override
	public void cleanUp() {
		if(dList != 0) GL11.glDeleteLists(dList, 1);
	}

	public void polygonDraw(int polyV) {
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GL11.glBegin(GL11.GL_POLYGON);
		for(int i = 0; i < 3; i++) {
			GL11.glTexCoord2f(tc[i][0], tc[i][1]);
			GL11.glVertex3f(poly[polyV][i].x, poly[polyV][i].y, poly[polyV][i].z);
		}
		GL11.glEnd();
		GlUtil.glColor4f(1, 1, 1, 1.0f);
	}
}
