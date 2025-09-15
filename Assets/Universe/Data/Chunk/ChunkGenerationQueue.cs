using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Settings;
using UnityEngine;
using UnityEngine.Rendering;

namespace Universe.Data.Chunk {
	public class ChunkGenerationQueue : MonoBehaviour {
		readonly Queue<GameEntity.GameEntity> _queue = new Queue<GameEntity.GameEntity>();

		// Track ongoing rebuilds per entity
		struct RebuildState {
			public GameEntity.GameEntity Entity;
			public Task<ChunkBuilder.MeshDataRaw>[] Tasks;
			public int ChunksTotal;
		}

		readonly Dictionary<string, RebuildState> _ongoing = new Dictionary<string, RebuildState>();

		int MaxRebuildsPerFrame => EngineSettings.Instance.MaxEntityRebuildsPerFrame.Value;

		void Update() {
			int finalized = 0;
			// Start up to MaxRebuildsPerFrame rebuilds per frame
			int toStart = Mathf.Min(MaxRebuildsPerFrame, _queue.Count);
			for(int i = 0; i < toStart; i++) {
				GameEntity.GameEntity entity = _queue.Dequeue();
				if(entity == null) continue;
				if(_ongoing.ContainsKey(entity.Uid)) continue; // already building
				StartAsyncRebuild(entity);
			}

			// Finalize completed rebuilds (also limited per frame to avoid hitches)
			var keys = new List<string>(_ongoing.Keys);
			foreach(var key in keys) {
				if(finalized >= MaxRebuildsPerFrame) break;
				RebuildState state = _ongoing[key];
				bool allDone = true;
				for(int t = 0; t < state.Tasks.Length; t++) {
					if(state.Tasks[t] == null) continue; // empty chunk
					if(!state.Tasks[t].IsCompleted) { allDone = false; break; }
				}
				if(!allDone) continue;

				// All tasks completed - finalize on main thread
				try {
					FinalizeRebuild(state);
				} catch(Exception ex) {
					Debug.LogError($"ChunkGenerationQueue: error finalizing rebuild for {state.Entity.Uid}: {ex}");
				}
				_ongoing.Remove(key);
				finalized++;
			}
		}

		void StartAsyncRebuild(GameEntity.GameEntity entity) {
			int chunksTotal = Math.Max(0, entity.ChunkCount);
			if(chunksTotal == 0) {
				Debug.LogWarning($"ChunkGenerationQueue: Entity {entity.Uid} has no chunks - skipping rebuild");
				return;
			}
			var tasks = new Task<ChunkBuilder.MeshDataRaw>[chunksTotal];
			for(int i = 0; i < chunksTotal; i++) {
				var chunk = entity.GetChunkData(i);
				if(chunk == null) {
					tasks[i] = null; // nothing to build
					continue;
				}
				try {
					tasks[i] = ChunkBuilder.BuildChunkAsync(chunk);
				} catch(Exception ex) {
					Debug.LogError($"ChunkGenerationQueue: failed to start async build for {entity.Uid} chunk {i}: {ex}");
					tasks[i] = null;
				}
			}
			var state = new RebuildState { Entity = entity, Tasks = tasks, ChunksTotal = chunksTotal};
			_ongoing[entity.Uid] = state;
		}

		void FinalizeRebuild(RebuildState state) {
			var entity = state.Entity;
			int chunksTotal = state.ChunksTotal;
			var combineList = new List<CombineInstance>(chunksTotal);
			int totalVertex = 0; int totalTriangles = 0; int totalBlocks = 0;
			for(int i = 0; i < chunksTotal; i++) {
				var task = state.Tasks[i];
				if(task == null) continue;
				if(task.IsFaulted) {
					Debug.LogError($"ChunkGenerationQueue: task faulted for {entity.Uid} chunk {i}: {task.Exception}");
					continue;
				}
				ChunkBuilder.MeshDataRaw data = task.Result;
				if(data.VertexCount == 0 || data.Triangles == null || data.Triangles.Length == 0) continue;
				Mesh m = new Mesh();
				// If vertex counts exceed 65535, ensure 32-bit indices are used
				if(data.VertexCount > 65535) m.indexFormat = UnityEngine.Rendering.IndexFormat.UInt32;
				m.SetVertices(new List<Vector3>(data.Vertices));
				m.SetNormals(new List<Vector3>(data.Normals));
				m.SetUVs(0, new List<Vector2>(data.Uvs));
				m.SetTriangles(data.Triangles, 0);
				m.RecalculateBounds();
				CombineInstance ci = new CombineInstance { mesh = m, transform = Matrix4x4.TRS(entity.GetChunkPosition(i), Quaternion.identity, Vector3.one) };
				combineList.Add(ci);
				totalVertex += data.VertexCount;
				totalTriangles += data.TriangleCount;
				totalBlocks += data.BlockCount;
			}

			if(combineList.Count == 0) {
				Debug.LogWarning($"ChunkGenerationQueue: FinalizeRebuild produced no geometry for {entity.Uid}");
				return;
			}

			// Create combined mesh
			Mesh finalMesh = new Mesh();
			// If combined vertex count will exceed 65535, use 32-bit indices
			if(totalVertex > 65535) finalMesh.indexFormat = UnityEngine.Rendering.IndexFormat.UInt32;
			finalMesh.name = $"EntityMesh_{entity.Uid}";
			// Manually concatenate meshes to avoid CombineMeshes issues
			var finalVerts = new List<Vector3>(totalVertex);
			var finalNormals = new List<Vector3>(totalVertex);
			var finalUVs = new List<Vector2>(totalVertex);
			var finalTris = new List<int>(totalTriangles * 3);
			int vOffset = 0;
			foreach(var ci in combineList) {
				Mesh src = ci.mesh;
				Matrix4x4 xf = ci.transform;
				Matrix4x4 normalXf = xf.inverse.transpose;
				var srcVerts = src.vertices;
				var srcNormals = src.normals;
				var srcUvs = src.uv;
				var srcTris = src.GetTriangles(0);
				// transform verts and normals
				for(int vi = 0; vi < srcVerts.Length; vi++) {
					finalVerts.Add(xf.MultiplyPoint3x4(srcVerts[vi]));
					if(srcNormals != null && srcNormals.Length == srcVerts.Length) {
						Vector3 n = normalXf.MultiplyVector(srcNormals[vi]);
						n.Normalize();
						finalNormals.Add(n);
					} else {
						finalNormals.Add(Vector3.up);
					}
					if(srcUvs != null && srcUvs.Length == srcVerts.Length) finalUVs.Add(srcUvs[vi]); else finalUVs.Add(Vector2.zero);
				}
				// append triangles with offset
				for(int ti = 0; ti < srcTris.Length; ti++) {
					finalTris.Add(srcTris[ti] + vOffset);
				}
				vOffset += srcVerts.Length;
			}
			finalMesh.SetVertices(finalVerts);
			finalMesh.SetNormals(finalNormals);
			finalMesh.SetUVs(0, finalUVs);
			finalMesh.SetTriangles(finalTris, 0);
			// Ensure bounds are correct and upload data for GPU
			finalMesh.RecalculateBounds();
			try {
				finalMesh.UploadMeshData(false); // keep readable in case of further edits
			} catch(Exception) {
				// UploadMeshData can throw in editor in some cases; ignore safely
			}

			// Diagnostics
			// Debug.Log($"ChunkGenerationQueue: Final mesh for {entity.UID}: vertices={finalMesh.vertexCount} subMeshCount={finalMesh.subMeshCount} bounds={finalMesh.bounds}");

			// Ensure renderer and filter exist and assign the mesh (use sharedMesh so inspector shows it)
			MeshFilter mf = entity.gameObject.GetComponent<MeshFilter>();
			if(mf == null) mf = entity.gameObject.AddComponent<MeshFilter>();
			// assign mesh first
			mf.sharedMesh = finalMesh;

			MeshRenderer mr = entity.gameObject.GetComponent<MeshRenderer>();
			if(mr == null) mr = entity.gameObject.AddComponent<MeshRenderer>();
			mr.enabled = true; // ensure renderer is enabled
			// assign default material if none
			if(mr.sharedMaterial == null) {
				var mat = Resources.Load<Material>("ChunkMaterial");
				if(mat == null) {
					mat = new Material(Shader.Find("Standard"));
					mat.name = "ChunkMaterial_Fallback";
					Debug.LogWarning("ChunkGenerationQueue: ChunkMaterial not found in Resources, assigning fallback Standard material.");
				}
				mr.sharedMaterial = mat;
			}
			// Ensure proper rendering flags
			mr.shadowCastingMode = ShadowCastingMode.On;
			mr.receiveShadows = true;

			MeshCollider mc = entity.gameObject.GetComponent<MeshCollider>();
			if(mc == null) mc = entity.gameObject.AddComponent<MeshCollider>();
			mc.sharedMesh = finalMesh;

			// Update counts
			entity.blockCount = totalBlocks;
			entity.vertexCount = totalVertex;
			entity.triangleCount = totalTriangles;

			// Debug.Log($"Finalized async rebuild for {entity.UID}: Verts={totalVertex} Tris={totalTriangles} Blocks={totalBlocks}");
		}

		public void RequestMeshRebuild(GameEntity.GameEntity entity) {
			if(!_queue.Contains(entity) && !_ongoing.ContainsKey(entity.Uid)) {
				_queue.Enqueue(entity);
			}
		}
	}
}