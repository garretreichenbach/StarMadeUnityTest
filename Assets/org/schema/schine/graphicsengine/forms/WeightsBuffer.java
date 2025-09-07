package org.schema.schine.graphicsengine.forms;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;

/**
 * WeightBuffer contains associations of vertexes to bones and their weights.
 * The WeightBuffer can be sent to a shader or processed on the CPU
 * to do skinning.
 */
public final class WeightsBuffer {

	
	final int bufferCount;
	public IntBuffer indexIdBuffer = MemoryUtil.memAllocInt(1);
	public IntBuffer weightIdBuffer = MemoryUtil.memAllocInt(1);
	/**
	 * The maximum number of weighted bones used by the vertices
	 * Can be 1-4. The indices and weights still have 4 components per vertex,
	 * regardless of this value.
	 */
	int maxWeightsPerVert = 0;
	/**
	 * Each 4 bytes in the boneIndex buffer are assigned to a vertex.
	 */
	private FloatBuffer indices;
	/**
	 * The weight of each bone specified in the index buffer
	 */
	private FloatBuffer weights;

	public WeightsBuffer(FloatBuffer indexes, FloatBuffer weights) {
		this.bufferCount = indices.capacity() / 4;
		this.indices = indexes;
		this.weights = weights;
	}

	public WeightsBuffer(int vertexCount) {
		this.bufferCount = vertexCount;
		indices = MemoryUtil.memAllocFloat(vertexCount * 4);
		weights = MemoryUtil.memAllocFloat(vertexCount * 4);
	}

	public FloatBuffer getIndexes() {
		return indices;
	}

	public FloatBuffer getWeights() {
		return weights;
	}

	public void initialize(HashMap<Integer, List<VertexBoneWeight>> vertexWeightMap, Skeleton skeleton) {
		indices.rewind();
		weights.rewind();
		int ex = 0;
		for (Entry<Integer, List<VertexBoneWeight>> entry : vertexWeightMap.entrySet()) {

			List<VertexBoneWeight> vertexWeightList = entry.getValue();
			if (vertexWeightList.size() > 4) {
				assert (false);
				System.err.println("[BONE] WARNING: vertex is influened by more then 4 bones: vertex index: " + entry.getKey() + "; influened by count: " + vertexWeightList.size());
				for (int i = 0; i < vertexWeightList.size(); i++) {
					int max = Math.min(vertexWeightList.size() - 1, i);
					VertexBoneWeight vertexBoneWeight = vertexWeightList.get(max);
					System.err.println("[BONE] WARNING: vertex bone weight influence exceeded " + entry.getKey() + ": " + skeleton.getBones().get(vertexBoneWeight.boneIndex) + " -> " + entry.getKey());
				}
				ex++;
			}
			for (int i = 0; i < 4; i++) {
				int max = Math.min(vertexWeightList.size() - 1, i);
				VertexBoneWeight vertexBoneWeight = vertexWeightList.get(max);
//				    			System.err.println("VERTEXBONEWEIGHT: "+vertexBoneWeight+": "+i+"/"+vertexWeightList.size() );

				indices.put(vertexBoneWeight.vertexIndex * 4 + i, vertexBoneWeight.boneIndex);

				weights.put(vertexBoneWeight.vertexIndex * 4 + i, i < vertexWeightList.size() ? vertexBoneWeight.weight : 0);
			}
		}
		//		System.err.println("TOTAL WEIGHT EXCEEDING: "+ex);
		normalizeWeights();

		indices.rewind();
		weights.rewind();
	}

	public void initVBO() {
		indices.rewind();
		weights.rewind();

		GL15.glGenBuffers(indexIdBuffer); // Get A Valid Name
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, indexIdBuffer.get(0)); // Bind
		Controller.loadedVBOBuffers.add(indexIdBuffer.get(0));
		// Load The Data
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, indices, GL15.GL_STATIC_DRAW);
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Bind

		GL15.glGenBuffers(weightIdBuffer); // Get A Valid Name
		Controller.loadedVBOBuffers.add(weightIdBuffer.get(0));
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, weightIdBuffer.get(0)); // Bind
		// Load The Data
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, weights, GL15.GL_STATIC_DRAW);
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Bind
	}

	public void loadShaderVBO() {
		GL20.glEnableVertexAttribArray(3);
		GL20.glEnableVertexAttribArray(4);

		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, indexIdBuffer.get(0));
		GL20.glVertexAttribPointer(3, 4, GL11.GL_FLOAT, false, 0, 0); // Set The Vertex Pointer To The Vertex Buffer

		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, weightIdBuffer.get(0));
		GL20.glVertexAttribPointer(4, 4, GL11.GL_FLOAT, false, 0, 0); // Set The Vertex Pointer To The Vertex Buffer
	}

	public void normalizeWeights() {
		int nVerts = weights.capacity() / 4;
		weights.rewind();
		for (int v = 0; v < nVerts; v++) {
			float w0 = weights.get(),
					w1 = weights.get(),
					w2 = weights.get(),
					w3 = weights.get();

			if (w3 > 0.01f) {
				maxWeightsPerVert = Math.max(maxWeightsPerVert, 4);
			} else if (w2 > 0.01f) {
				maxWeightsPerVert = Math.max(maxWeightsPerVert, 3);
			} else if (w1 > 0.01f) {
				maxWeightsPerVert = Math.max(maxWeightsPerVert, 2);
			} else if (w0 > 0.01f) {
				maxWeightsPerVert = Math.max(maxWeightsPerVert, 1);
			}

			float sum = w0 + w1 + w2 + w3;
			if (sum != 1f) {
				weights.position(weights.position() - 4);
				weights.put(w0 / sum);
				weights.put(w1 / sum);
				weights.put(w2 / sum);
				weights.put(w3 / sum);
			}
		}
		weights.rewind();
	}

	public void unloadShaderVBO() {
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL20.glDisableVertexAttribArray(3);
		GL20.glDisableVertexAttribArray(4);
	}

}
