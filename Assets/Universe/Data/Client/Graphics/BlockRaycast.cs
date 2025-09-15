using UnityEngine;
using Universe.Data.Chunk;

namespace Universe.Data.Client.Graphics {
	public struct BlockRaycastHit {
		public GameEntity.GameEntity entity;
		public int chunkIndex;
		public int blockIndex;
		public Vector3Int blockPosition;
		public Vector3 hitPoint;
		public Vector3 hitNormal;
		public bool valid;
	}

	public static class BlockRaycast {
		// Returns info about the first block hit in the entity, or null if none
		public static BlockRaycastHit Raycast(GameEntity.GameEntity entity, Ray ray, float maxDistance) {
			BlockRaycastHit result = new BlockRaycastHit { entity = entity, valid = false };
			if(entity == null || !entity.Loaded || entity.Chunks == null) return result;

			// Step along the ray in small increments
			const float step = 0.1f;
			Vector3 pos = ray.origin;
			for(float d = 0; d < maxDistance; d += step) {
				pos = ray.origin + ray.direction * d;
				// Find which chunk this position is in
				Vector3 localPos = pos - entity.transform.position;
				Vector3Int chunkDims = entity.ChunkDimensions;
				int chunkSize = IChunkData.ChunkSize;
				int cx = Mathf.FloorToInt(localPos.x / chunkSize);
				int cy = Mathf.FloorToInt(localPos.y / chunkSize);
				int cz = Mathf.FloorToInt(localPos.z / chunkSize);
				if(cx < 0 || cy < 0 || cz < 0 || cx >= chunkDims.x || cy >= chunkDims.y || cz >= chunkDims.z) continue;
				int chunkIndex = cx + cy * chunkDims.x + cz * chunkDims.x * chunkDims.y;
				var chunk = entity.GetChunkData(chunkIndex);
				if(chunk == null) continue;
				// Find block index in chunk
				int bx = Mathf.FloorToInt(localPos.x - cx * chunkSize);
				int by = Mathf.FloorToInt(localPos.y - cy * chunkSize);
				int bz = Mathf.FloorToInt(localPos.z - cz * chunkSize);
				if(bx < 0 || by < 0 || bz < 0 || bx >= chunkSize || by >= chunkSize || bz >= chunkSize) continue;
				int blockIndex = bx + by * chunkSize + bz * chunkSize * chunkSize;
				short type = chunk.GetBlockType(blockIndex);
				if(type != 0) {
					result.chunkIndex = chunkIndex;
					result.blockIndex = blockIndex;
					result.blockPosition = new Vector3Int(bx, by, bz);
					result.hitPoint = pos;
					result.hitNormal = -ray.direction; // Approximate
					result.valid = true;
					return result;
				}
			}
			return result;
		}
	}
}