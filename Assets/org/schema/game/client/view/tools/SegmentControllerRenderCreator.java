package org.schema.game.client.view.tools;

import com.bulletphysics.linearmath.Transform;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;
import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.common.controller.SegmentBufferInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.VoidSegmentPiece;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.FrameBufferObjects;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * Used to create a rendering of a Segment Controller and output it to a framebuffer, which can then be used to create a texture, image, icon, etc.
 * <p>Similar to SingleBlockDrawer, but this draws more than a single block (obviously).</p>
 *
 * @author TheDerpGamer
 */
public class SegmentControllerRenderCreator {

	private static final int WIDTH = 1280;
	private static final int HEIGHT = 720;
	private static final int BBP = 4;
	private FloatBuffer fb = MemoryUtil.memAllocFloat(16);
	private float[] ff = new float[16];
	private Transform mView = new Transform();
	private Transform orientation = new Transform();
	private Transform orientationTmp = new Transform();
	private Matrix3f rot = new Matrix3f();

	public BufferedImage bake(SegmentController segmentController) throws Exception {
		SegmentBufferInterface segmentBuffer = segmentController.getSegmentBuffer();

		FrameBufferObjects frameBuffer = new FrameBufferObjects("ShipRenderer", WIDTH, HEIGHT);
		frameBuffer.initialize();
		frameBuffer.enable();

		Vector3i minSegment = segmentController.getMinPos();
		Vector3i maxSegment = segmentController.getMaxPos();
		Transform transform = new Transform();
		transform.set(segmentController.getWorldTransform());

		Matrix4f modelviewMatrix = Controller.modelviewMatrix;
		fb.rewind();
		Matrix4fTools.store(modelviewMatrix, fb);
		fb.rewind();
		fb.get(ff);
		mView.setFromOpenGLMatrix(ff);
		mView.origin.set(0, 0, 0);

		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		GUIElement.enableOrthogonal3d(WIDTH, HEIGHT);
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		Vector3f scaleOut = new Vector3f(1, 1, 1);

		for(int x = minSegment.x; x < maxSegment.x; x++) {
			for(int y = minSegment.y; y < maxSegment.y; y++) {
				for(int z = minSegment.z; z < maxSegment.z; z++) {
					if(segmentBuffer.get(x, y, z) != null && !segmentBuffer.get(x, y, z).isEmpty()) {
						Segment segment = segmentBuffer.get(x, y, z);
						renderSegment(segment, scaleOut);
					}
				}
			}
		}

		GUIElement.disableOrthogonal();
		ByteBuffer buffer = GlUtil.getDynamicByteBuffer(WIDTH * HEIGHT * BBP, 0);
		GL11.glReadPixels(0, 0, WIDTH, HEIGHT, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
		BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		for(int x = 0; x < WIDTH; x++) {
			for(int y = 0; y < HEIGHT; y++) {
				int i = (x + (WIDTH * y)) * BBP;
				int r = buffer.get(i) & 0xFF;
				int g = buffer.get(i + 1) & 0xFF;
				int b = buffer.get(i + 2) & 0xFF;
				int a = buffer.get(i + 3) & 0xFF;
				image.setRGB(x, HEIGHT - (y + 1), (a << 24) | (r << 16) | (g << 8) | b);
			}
		}
		frameBuffer.disable();
		frameBuffer.cleanUp();
		return image;
	}

	private void renderSegment(Segment segment, Vector3f scaleOut) {
		SegmentData segmentData = segment.getSegmentData();
		VoidSegmentPiece piece = new VoidSegmentPiece();
		Vector3f scale = new Vector3f(scaleOut);
		scale.x *= 32.0f;
		scale.y *= -32.0f;
		scale.z *= 32.0f;
		for(byte x = 0; x < Segment.DIM; x++) {
			for(byte y = 0; y < Segment.DIM; y++) {
				for(byte z = 0; z < Segment.DIM; z++) {
					piece.setByReference(segment, x, y, z);
					if(ElementKeyMap.isValidType(segmentData.getType(x, y, z))) {
						ElementInformation info = ElementKeyMap.getInfoFast(piece.getType());
						GlUtil.glPushMatrix();
						GlUtil.translateModelview(x * 32.0f, y * 32.0f, z * 32.0f);
						GlUtil.scaleModelview(scale.x, scale.y, scale.z);
						rot.set(orientation.basis);
						mView.basis.mul(rot);
						mView.basis.setIdentity();
						GlUtil.glMultMatrix(mView);
						SingleBlockDrawer drawer = new SingleBlockDrawer();
						if(info.getBlockStyle() != BlockStyle.NORMAL) {
							drawer.setSidedOrientation((byte) 0);
							drawer.setShapeOrientation24(BlockShapeAlgorithm.getLocalAlgoIndex(info.getBlockStyle(), piece.getOrientation()));
						} else if(info.getIndividualSides() > 3) {
							drawer.setShapeOrientation24((byte) 0);
							drawer.setSidedOrientation(piece.getOrientation());
						} else if(info.orientatable) {
							drawer.setShapeOrientation24((byte) 0);
							drawer.setSidedOrientation(piece.getOrientation());
						} else {
							drawer.setShapeOrientation24((byte) 0);
							drawer.setSidedOrientation((byte) 0);
						}
						drawer.setActive(piece.isActive());
//						drawer.useSpriteIcons = false;
//						drawer.setLightAll(false);
						drawer.drawType(piece.getType());
//						drawer.useSpriteIcons = true;
						GlUtil.glPopMatrix();
					}
				}
			}
		}
	}
}
