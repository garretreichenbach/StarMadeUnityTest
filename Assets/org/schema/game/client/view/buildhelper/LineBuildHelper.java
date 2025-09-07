package org.schema.game.client.view.buildhelper;

import java.nio.FloatBuffer;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL15;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.manager.ingame.BuildSelectionLineHelper;
import org.schema.game.client.controller.manager.ingame.BuildToolsManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.Transformable;
import org.schema.schine.graphicsengine.forms.simple.Box;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

/**
 * Classes with the annotation BuildHelperClass are automatically
 * listed as helper algorithms
 *
 * @author schema
 */
@BuildHelperClass(name = "Line")
public class LineBuildHelper extends BuildHelper {

	public static LongOpenHashSet poses = new LongOpenHashSet();
	private static Vector3f[][] verts = Box.init();
	@BuildHelperVar(type = "line", name = BuildHelperVarName.LINE_SEGMENT, min = 0, max = 0)
	public Line line;
	@BuildHelperVar(type = "float", name = BuildHelperVarName.LINE_THICKNESS, min = 1, max = 4)
	public float thickness;
	
	private int vertCount;
	private FloatBuffer fBuffer;
	public LineBuildHelper(Transformable transformable) {
		super(transformable);
	}
	@Override
	public void showProcessingDialog(final GameClientState state, final BuildToolsManager buildToolsManager, boolean placeMode) {
		onFinished();
		setFinished(true);
		buildToolsManager.setBuildHelper(this);
	}

	void createLine(Line line, final float step) {
		poses.clear();

		
		Vector3f start = new Vector3f(line.A.x, line.A.y, line.A.z);
		Vector3f end = new Vector3f(line.B.x, line.B.y, line.B.z);
		
		Vector3f d = new Vector3f();
		d.sub(end, start);
		
		float len = d.length();
		d.normalize();
		
		System.err.println("LINE THICKNESS "+thickness);
		
		for(float f = 0; f < len; f += step){
			Vector3f p = new Vector3f();
			p.x += FastMath.round(f * d.x);
			p.y += FastMath.round(f * d.y);
			p.z += FastMath.round(f * d.z);
			poses.add(ElementCollection.getIndex((int)(p.x), (int)(p.y), (int)(p.z)));
			if(thickness > 1){
				for(int s = 0; s < Element.DIRECTIONSi.length; s++){
					Vector3i dir = Element.DIRECTIONSi[s];
					for(int i = 1; i < thickness; i++){
						poses.add(ElementCollection.getIndex((int)(p.x+dir.x*i), (int)(p.y+dir.y*i), (int)(p.z+dir.z*i)));
					}
				}
			}
		}

		vertCount = poses.size() * 24;
		fBuffer = GlUtil.getDynamicByteBuffer(vertCount * 3 * 4, 8).asFloatBuffer();
		Vector3f[][] box = Box.getVertices(new Vector3f(-0.5f, -0.5f, -0.5f), new Vector3f(0.5f, 0.5f, 0.5f), verts);

		Vector3f pos = new Vector3f();
		for (long p : poses) {

			ElementCollection.getPosFromIndex(p, pos);
			for (int i = 0; i < box.length; i++) {
				for (int k = 0; k < box[i].length; k++) {
					fBuffer.put(pos.x + box[i][k].x);
					fBuffer.put(pos.y + box[i][k].y);
					fBuffer.put(pos.z + box[i][k].z);
				}
			}
		}
		fBuffer.flip();
		percent = 1;
	}

	@Override
	public void create() {

		createLine(line, 0.2f);
		setInitialized(true);
	}

	@Override
	public void drawLocal() {
		GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GlUtil.glEnable(GL11.GL_CULL_FACE);
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_CULL_FACE);
		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glDisable(GL12.GL_TEXTURE_3D);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glDisable(GL11.GL_TEXTURE_1D);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glColor4f(1, 1, 1, 1);

		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.buffer);
		GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);
		GL11.glDrawArrays(GL11.GL_QUADS, 0, vertCount);

		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glEnable(GL11.GL_CULL_FACE);
		GlUtil.glDisable(GL11.GL_BLEND);
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
	}

	@Override
	public void clean() {
		if (this.buffer != 0) {
			GL15.glDeleteBuffers(this.buffer);
			buffer = 0;
		}
		setFinished(false);
	}

	@Override
	public LongOpenHashSet getPoses() {
		return poses;
	}

	@Override
	public void onFinished() {
		if (this.buffer != 0) {
			GL15.glDeleteBuffers(this.buffer);
		}
		this.buffer = GL15.glGenBuffers();

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.buffer);

		System.err.println("[CLIENT] elipsoid: Blocks: " + poses.size() + "; ByteBufferNeeded: " + ((vertCount * 3 * 4) / 1024f) / 1024f + "MB");

		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, fBuffer, GL15.GL_STATIC_DRAW);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}
	@Override
	public void onPressedOk(BuildToolsManager buildToolsManager) {
		buildToolsManager.setSelectMode(new BuildSelectionLineHelper(this));
	}
	@Override
	public void onPressedOk(PlayerGameOkCancelInput playerGameOkCancelInput, BuildToolsManager buildToolsManager) {
		
		playerGameOkCancelInput.deactivate();
	}
	@Override
	public BuildHelperFactory getType() {
		return BuildHelpers.LINE;
	}
}
