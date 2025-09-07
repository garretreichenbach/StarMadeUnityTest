package api.utils.draw.debugging;

import com.bulletphysics.linearmath.Transform;
import org.lwjgl.opengl.GL11;
import org.schema.game.client.view.SelectionShader;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;

import javax.vecmath.Vector3f;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class SoundDrawerData implements DebugDrawerData {

	private final Vector3f min = new Vector3f();
	private final Vector3f max = new Vector3f();
	private final SegmentPiece segmentPiece;

	private boolean initialized;
	private Mesh mesh;
	private Transform transform;
	private SelectionShader shader;

	public SoundDrawerData(ElementCollection<?, ?, ?> ec) {
		ec.getMin(min);
		ec.getMax(max);
//		min.sub(new Vector3f(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF));
//		max.sub(new Vector3f(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF));
		transform = new Transform();
		transform.setIdentity();
		segmentPiece = ec.getElementCollectionId();
		segmentPiece.getTransform(transform);
		transform.transform(min);
		transform.transform(max);
//		transform.origin.add(new Vector3f(min.x / 2, min.y / 2, min.z / 2));
	}

	@Override
	public void onInit() {
		mesh = ((Mesh) Controller.getResLoader().getMesh("Box").getChilds().get(0));
		shader = new SelectionShader(-1);
		initialized = true;
	}

	@Override
	public void draw() {
		if(!initialized) onInit();
		GlUtil.glPushMatrix();
		GlUtil.glEnable(GL11.GL_CULL_FACE);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		mesh.loadVBO(true);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		ShaderLibrary.selectionShader.setShaderInterface(shader);
		ShaderLibrary.selectionShader.load();
		GlUtil.glColor4f(1, 1, 0.0f, 0.5f);
		GlUtil.updateShaderVector4f(ShaderLibrary.selectionShader, "selectionColor", 1.0F, 1.0F, 0.0F, 0.5F);
		GlUtil.translateModelview(transform.origin);
		GlUtil.scaleModelview(max.x - min.x, max.y - min.y, max.z - min.z);
		mesh.renderVBO();
		ShaderLibrary.selectionShader.unload();
		mesh.unloadVBO(true);
		GlUtil.glColor4f(1, 1, 1, 1.0f);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
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

	@Override
	public void onExit() {

	}

	@Override
	public void updateShader(DrawableScene scene) {

	}

	@Override
	public void updateShaderParameters(Shader shader) {

	}

	public SegmentPiece getSegmentPiece() {
		return segmentPiece;
	}
}
