package org.schema.game.client.view;

import java.nio.FloatBuffer;
import java.util.Map.Entry;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.SegmentDrawer.SAABB;
import org.schema.game.client.view.cubes.CubeMeshNormal;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.core.GlUtil;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class SegAABBDrawer {

	private static final int VERT_COMPO = 4; //(xFloat,yFloat,zFloat, param)*24
	private static final int VERTS_PER_AABB = 24; //(xFloat,yFloat,zFloat, param)*24
	private static final int FLOAT_SIZE_PER_AABB = VERTS_PER_AABB * VERT_COMPO; //(xFloat,yFloat,zFloat, param)*24
	private static final int BYTE_SIZE_PER_AABB = FLOAT_SIZE_PER_AABB * ByteUtil.SIZEOF_FLOAT; //(xFloat,yFloat,zFloat, param)*24
	private static final boolean DEBUG = false;
	FloatBuffer helpBuffer;
	private int bufferId;
	private IntArrayList segConIds = new IntArrayList();
	private LongArrayList endPointers = new LongArrayList();
	private boolean updated;
	public static void addBox(Vector3i p, Vector3f min, Vector3f max, int id, FloatBuffer buffer) {

		float scale = 1;

		// White side - FRONT
		GlUtil.putPoint4(buffer, min.x * scale + p.x, min.y * scale + p.y, min.z * scale + p.z, id);
		GlUtil.putPoint4(buffer, min.x * scale + p.x, max.y * scale + p.y, min.z * scale + p.z, id);
		GlUtil.putPoint4(buffer, max.x * scale + p.x, max.y * scale + p.y, min.z * scale + p.z, id);
		GlUtil.putPoint4(buffer, max.x * scale + p.x, min.y * scale + p.y, min.z * scale + p.z, id);

		// White side - BACK
		GlUtil.putPoint4(buffer, max.x * scale + p.x, min.y * scale + p.y, max.z * scale + p.z, id);
		GlUtil.putPoint4(buffer, max.x * scale + p.x, max.y * scale + p.y, max.z * scale + p.z, id);
		GlUtil.putPoint4(buffer, min.x * scale + p.x, max.y * scale + p.y, max.z * scale + p.z, id);
		GlUtil.putPoint4(buffer, min.x * scale + p.x, min.y * scale + p.y, max.z * scale + p.z, id);

		// Purple side - RIGHT
		GlUtil.putPoint4(buffer, max.x * scale + p.x, min.y * scale + p.y, min.z * scale + p.z, id);
		GlUtil.putPoint4(buffer, max.x * scale + p.x, max.y * scale + p.y, min.z * scale + p.z, id);
		GlUtil.putPoint4(buffer, max.x * scale + p.x, max.y * scale + p.y, max.z * scale + p.z, id);
		GlUtil.putPoint4(buffer, max.x * scale + p.x, min.y * scale + p.y, max.z * scale + p.z, id);

		// Green side - LEFT
		GlUtil.putPoint4(buffer, min.x * scale + p.x, min.y * scale + p.y, max.z * scale + p.z, id);
		GlUtil.putPoint4(buffer, min.x * scale + p.x, max.y * scale + p.y, max.z * scale + p.z, id);
		GlUtil.putPoint4(buffer, min.x * scale + p.x, max.y * scale + p.y, min.z * scale + p.z, id);
		GlUtil.putPoint4(buffer, min.x * scale + p.x, min.y * scale + p.y, min.z * scale + p.z, id);

		// Blue side - TOP
		GlUtil.putPoint4(buffer, max.x * scale + p.x, max.y * scale + p.y, max.z * scale + p.z, id);
		GlUtil.putPoint4(buffer, max.x * scale + p.x, max.y * scale + p.y, min.z * scale + p.z, id);
		GlUtil.putPoint4(buffer, min.x * scale + p.x, max.y * scale + p.y, min.z * scale + p.z, id);
		GlUtil.putPoint4(buffer, min.x * scale + p.x, max.y * scale + p.y, max.z * scale + p.z, id);

		// Red side - BOTTOM
		GlUtil.putPoint4(buffer, max.x * scale + p.x, min.y * scale + p.y, min.z * scale + p.z, id);
		GlUtil.putPoint4(buffer, max.x * scale + p.x, min.y * scale + p.y, max.z * scale + p.z, id);
		GlUtil.putPoint4(buffer, min.x * scale + p.x, min.y * scale + p.y, max.z * scale + p.z, id);
		GlUtil.putPoint4(buffer, min.x * scale + p.x, min.y * scale + p.y, min.z * scale + p.z, id);
	}

	public void generate(int maxDrawnSegs) {
		bufferId = GL15.glGenBuffers();
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferId); // Bind
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, BYTE_SIZE_PER_AABB * maxDrawnSegs, CubeMeshNormal.BUFFER_FLAG); // Load The Data

			if (helpBuffer != null) {
				GlUtil.destroyDirectByteBuffer(helpBuffer);
			}
		helpBuffer = MemoryUtil.memAllocFloat(FLOAT_SIZE_PER_AABB * maxDrawnSegs);
	}

	public void draw(GameClientState state) {
		if (updated) {
			if (DEBUG) {
				GlUtil.printGlErrorCritical();
			}
			GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY);
			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferId);

			GL20.glUseProgram(0);
			GlUtil.glPushMatrix();

			long cur = 0;
			for (int i = 0; i < segConIds.size(); i++) {
				if (DEBUG) {
					GlUtil.printGlErrorCritical();
				}

				long next = endPointers.get(i);

				SimpleTransformableSendableObject segmentController = state.getCurrentSectorEntities().get(segConIds.get(i));
				GlUtil.glPushMatrix();
//				GlUtil.glLoadIdentity();
				GlUtil.glMultMatrix(segmentController.getWorldTransformOnClient());
//				GlUtil.scaleModelview(3f, 3f, 3f);

				if (DEBUG) {
					GlUtil.printGlErrorCritical();
				}
				GL11.glVertexPointer(VERT_COMPO, GL11.GL_FLOAT, 0, cur);

				if (DEBUG) {
					GlUtil.printGlErrorCritical();
				}

				long verticesCount = (next - cur) / (VERT_COMPO * ByteUtil.SIZEOF_FLOAT);

//				if(segmentController instanceof ShopSpaceStation){
//					System.err.println("DRAW: "+cur+"; Vertices: "+verticesCount);
//				}

				GL11.glDrawArrays(GL11.GL_QUADS, 0, (int) verticesCount);
				if (DEBUG) {
					GlUtil.printGlErrorCritical();
				}

				GlUtil.glPopMatrix();

				if (DEBUG) {
					GlUtil.printGlErrorCritical();
				}
				cur = next;

			}
			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			if (DEBUG) {
				GlUtil.printGlErrorCritical();
			}
			GlUtil.glPopMatrix();
			GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		}
	}

	public void update(Int2ObjectOpenHashMap<ObjectArrayList<SAABB>> map, int size) {

		helpBuffer.clear();

		segConIds.clear();
		endPointers.clear();

		long end = 0;
		for (Entry<Integer, ObjectArrayList<SAABB>> a : map.entrySet()) {
			int segConId = a.getKey().intValue();
			ObjectArrayList<SAABB> saabs = a.getValue();

			for (int i = 0; i < saabs.size(); i++) {

				SAABB saabb = saabs.get(i);

//				System.err.println("ADDING: "+saabb.min+" - "+saabb.max);

				addBox(saabb.position, saabb.min, saabb.max, segConId, helpBuffer);

				end += BYTE_SIZE_PER_AABB;
			}

			segConIds.add(segConId);
			endPointers.add(end);
		}

		helpBuffer.flip();
		if (DEBUG) {
			GlUtil.printGlErrorCritical();
		}
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferId);
		if (helpBuffer.limit() != 0) {
			//DO BUFFER SUB DATA UPDATE
			GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, helpBuffer);// Load The Data
		}
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		if (DEBUG) {
			GlUtil.printGlErrorCritical();
		}
		updated = true;
	}

}
