using Dev;
using UnityEngine;
using static Universe.Data.Chunk.Chunk;

namespace Universe.Data.Chunk {
	public class ChunkBuilder : MonoBehaviour, StatsDisplay.IStatsDisplayReporter {

		static int chunkCount;
		static int vertexCount;
		static int triangleCount;

		public static void BuildChunk(IChunkData chunk, Mesh mesh) {
			var vertices = new System.Collections.Generic.List<Vector3>();
			var triangles = new System.Collections.Generic.List<int>();
			
			for (var x = 0; x < ChunkSize; x++) {
				for (var y = 0; y < ChunkSize; y++) {
					for (var z = 0; z < ChunkSize; z++) {
						var index = chunk.GetBlockIndex(new Vector3(x, y, z));
						if (chunk.GetBlockType(index) == 0) continue;

						// Check for exposed faces
						if (x == 0 || (x > 0 && chunk.GetBlockType(chunk.GetBlockIndex(new Vector3(x - 1, y, z))) == 0)) {
							// Left face
							vertices.Add(new Vector3(x, y, z));
							vertices.Add(new Vector3(x, y + 1, z));
							vertices.Add(new Vector3(x, y, z + 1));
							vertices.Add(new Vector3(x, y + 1, z + 1));
							AddTriangles(vertices.Count - 4);
						}
						if (x == ChunkSize - 1 || (x < ChunkSize - 1 && chunk.GetBlockType(chunk.GetBlockIndex(new Vector3(x + 1, y, z))) == 0)) {
							// Right face
							vertices.Add(new Vector3(x + 1, y, z));
							vertices.Add(new Vector3(x + 1, y + 1, z));
							vertices.Add(new Vector3(x + 1, y, z + 1));
							vertices.Add(new Vector3(x + 1, y + 1, z + 1));
							AddTriangles(vertices.Count - 4, false);
						}
						if (y == 0 || (y > 0 && chunk.GetBlockType(chunk.GetBlockIndex(new Vector3(x, y - 1, z))) == 0)) {
							// Bottom face
							vertices.Add(new Vector3(x, y, z));
							vertices.Add(new Vector3(x + 1, y, z));
							vertices.Add(new Vector3(x, y, z + 1));
							vertices.Add(new Vector3(x + 1, y, z + 1));
							AddTriangles(vertices.Count - 4, false);
						}
						if (y == ChunkSize - 1 || (y < ChunkSize - 1 && chunk.GetBlockType(chunk.GetBlockIndex(new Vector3(x, y + 1, z))) == 0)) {
							// Top face
							vertices.Add(new Vector3(x, y + 1, z));
							vertices.Add(new Vector3(x + 1, y + 1, z));
							vertices.Add(new Vector3(x, y + 1, z + 1));
							vertices.Add(new Vector3(x + 1, y + 1, z + 1));
							AddTriangles(vertices.Count - 4);
						}
						if (z == 0 || (z > 0 && chunk.GetBlockType(chunk.GetBlockIndex(new Vector3(x, y, z - 1))) == 0)) {
							// Back face
							vertices.Add(new Vector3(x, y, z));
							vertices.Add(new Vector3(x, y + 1, z));
							vertices.Add(new Vector3(x + 1, y, z));
							vertices.Add(new Vector3(x + 1, y + 1, z));
							AddTriangles(vertices.Count - 4, false);
						}
						if (z == ChunkSize - 1 || (z < ChunkSize - 1 && chunk.GetBlockType(chunk.GetBlockIndex(new Vector3(x, y, z + 1))) == 0)) {
							// Front face
							vertices.Add(new Vector3(x, y, z + 1));
							vertices.Add(new Vector3(x, y + 1, z + 1));
							vertices.Add(new Vector3(x + 1, y, z + 1));
							vertices.Add(new Vector3(x + 1, y + 1, z + 1));
							AddTriangles(vertices.Count - 4);
						}
					}
				}
			}

			mesh.Clear();
			mesh.vertices = vertices.ToArray();
			mesh.triangles = triangles.ToArray();
			mesh.RecalculateNormals();
			mesh.RecalculateBounds();
			mesh.Optimize();
            mesh.UploadMeshData(true);
			chunkCount++;
			vertexCount += vertices.Count;
			triangleCount += triangles.Count / 3;

			void AddTriangles(int offset, bool reverse = false) {
				if (reverse) {
					triangles.Add(offset + 0);
					triangles.Add(offset + 1);
					triangles.Add(offset + 2);
					triangles.Add(offset + 2);
					triangles.Add(offset + 1);
					triangles.Add(offset + 3);
				} else {
					triangles.Add(offset + 2);
					triangles.Add(offset + 1);
					triangles.Add(offset + 0);
					triangles.Add(offset + 3);
					triangles.Add(offset + 1);
					triangles.Add(offset + 2);
				}
			}
		}

		public string Report(StatsDisplay.DisplayMode displayMode, float deltaTime) {
			return !displayMode.HasFlag(StatsDisplay.DisplayMode.RenderStats) ? "" : $"ChunkBuilder: {chunkCount} chunks, {vertexCount} vertices, {triangleCount} triangles";
		}

		public void ClearLastReport() {
			chunkCount = 0;
			vertexCount = 0;
			triangleCount = 0;
		}

		void Start() {
			FindFirstObjectByType<StatsDisplay>()?.Reporters.Add(this);
		}
	}
}