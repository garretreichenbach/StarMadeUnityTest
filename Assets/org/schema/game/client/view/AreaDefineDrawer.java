package org.schema.game.client.view;

import api.common.GameClient;
import com.bulletphysics.linearmath.Transform;
import org.lwjgl.opengl.GL11;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.quarters.Quarter;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;

import javax.vecmath.Vector3f;

/**
 * Draws the crew area selection box.
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class AreaDefineDrawer implements Drawable, Shaderable {
	private static Quarter q;
	public static Vector3f min;
	public static Vector3f max;
	private static SegmentPiece piece;
	private Mesh mesh;
	private Transform transform;
	private SelectionShader shader;
	private static boolean initialized;

	@Override
	public void onInit() {
		mesh = ((Mesh) Controller.getResLoader().getMesh("Box").getChilds().get(0));
		transform = new Transform();
		transform.setIdentity();
		transform.set(piece.getSegmentController().getWorldTransform());
		transform.origin.sub(min);
		shader = new SelectionShader(-1);
		initialized = true;
	}

	@Override
	public void draw() {
		if(q == null || piece == null) return;
		if(!piece.getSegmentController().isInClientRange()) return;
		if(!initialized) onInit();
		GlUtil.glPushMatrix();
		GlUtil.glEnable(GL11.GL_CULL_FACE);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		mesh.loadVBO(true);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		ShaderLibrary.selectionShader.setShaderInterface(shader);
		ShaderLibrary.selectionShader.load();
		//Todo: Make it not draw over UI, also make it be colored
//		GlUtil.glColor4f(1, 1, 0.0f, 0.5f);
		GlUtil.updateShaderVector4f(ShaderLibrary.selectionShader, "selectionColor", 1.0F, 1.0F, 0.0F, 0.5F);
		GlUtil.translateModelview(transform.origin);
		GlUtil.scaleModelview(max.x - min.x, max.y - min.y, max.z - min.z);
		mesh.renderVBO();
		ShaderLibrary.selectionShader.unload();
		mesh.unloadVBO(true);
		GlUtil.glColor4f(1, 1, 1, 1.0f);
//		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_BLEND);
		GL11.glCullFace(GL11.GL_BACK);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glDisable(GL11.GL_CULL_FACE);
		GlUtil.glPopMatrix();
	}

	@Override
	public void cleanUp() {
		if(mesh != null) mesh.cleanUp();
	}

	@Override
	public boolean isInvisible() {
		return false;
	}

	public static void startAreaDefine(Quarter quarter, SegmentPiece segmentPiece) {
		assert GameClient.getClientState() != null;
		q = quarter;
		Vector3i blockPos = new Vector3i(segmentPiece.getAbsolutePos(new Vector3i()));
		min = new Vector3i(quarter.getArea().min).toVector3f();
		max = new Vector3i(quarter.getArea().max).toVector3f();
		min.x += 0.5f;
		min.y += 0.5f;
		min.z += 0.5f;
		max.x -= 0.5f;
		max.y -= 0.5f;
		max.z -= 0.5f;
		WorldDrawer.flagAreaDefineDrawer = true;
		piece = segmentPiece;
		initialized = false;
	}

	public static void endAreaDefine(boolean set) {
		if(set) {
			Vector3i minConvert = new Vector3i(min.x - 0.5f, min.y - 0.5f, min.z - 0.5f);
			Vector3i maxConvert = new Vector3i(max.x + 0.5f, max.y + 0.5f, max.z + 0.5f);
			q.getArea().min.set(minConvert);
			q.getArea().max.set(maxConvert);
		}
		WorldDrawer.flagAreaDefineDrawer = false;
		piece = null;
		initialized = false;
	}

	@Override
	public void onExit() {
	}

	@Override
	public void updateShader(DrawableScene scene) {
	}

	@Override
	public void updateShaderParameters(Shader shader) {
	}
}
