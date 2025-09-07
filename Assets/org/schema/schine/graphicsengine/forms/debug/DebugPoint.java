package org.schema.schine.graphicsengine.forms.debug;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.schema.schine.graphicsengine.core.GlUtil;

public class DebugPoint extends DebugGeometry {

	private Vector3f point;

	public DebugPoint(Vector3f point) {
		this.point = point;

	}

	public DebugPoint(Vector3f point, Vector4f color) {
		this(point);
		this.color = color;
	}

	public DebugPoint(Vector3f point, Vector4f color, float size) {
		super(size);
		this.point = point;
		this.color = color;
	}

	public void draw() {
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		if (color != null) {
			GlUtil.glColor4f(color.x, color.y, color.z, color.w * getAlpha());
		} else {
			GlUtil.glColor4f(1, 1, 1, getAlpha());
		}
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex3f(point.x - size, point.y, point.z);
		GL11.glVertex3f(point.x + size, point.y, point.z);

		GL11.glVertex3f(point.x, point.y - size, point.z);
		GL11.glVertex3f(point.x, point.y + size, point.z);

		GL11.glVertex3f(point.x, point.y, point.z - size);
		GL11.glVertex3f(point.x, point.y, point.z + size);
		GL11.glEnd();

		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
	}
}
