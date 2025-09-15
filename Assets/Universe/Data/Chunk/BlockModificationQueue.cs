using System;
using System.Collections.Generic;
using Settings;
using UnityEngine;
using Universe.World;

namespace Universe.Data.Chunk {
    public struct BlockModification {
        public GameEntity.GameEntity Entity;
        public int ChunkIndex;
        public int BlockIndex;
        public short NewType;
    }

    /// <summary>
    /// Collects block modifications and applies them in batches on the main thread to avoid per-block stalls.
    /// Multiple modifications to the same block will be coalesced to the last write.
    /// After a batch is applied, the owning entities are enqueued for async mesh rebuild.
    /// </summary>
    public class BlockModificationQueue : MonoBehaviour {
        public static BlockModificationQueue Instance { get; private set; }

        readonly object _lock = new object();
        // Key: entityUID:chunk:blk
        readonly Dictionary<string, BlockModification> _pending = new Dictionary<string, BlockModification>();

        int MaxModsPerFrame {
            get => EngineSettings.Instance == null ? 128 : EngineSettings.Instance.MaxBlockModificationsPerFrame.Value;
        }

        void Awake() {
            Instance = this;
        }

        public void EnqueueModification(GameEntity.GameEntity entity, int chunkIndex, int blockIndex, int newType) {
            if(entity == null) return;
            string key = BuildKey(entity.UID, chunkIndex, blockIndex);
            var mod = new BlockModification { Entity = entity, ChunkIndex = chunkIndex, BlockIndex = blockIndex, NewType = (short)newType };
            lock(_lock) {
                // overwrite if exists -> last write wins
                _pending[key] = mod;
            }
        }

        static string BuildKey(string uid, int chunkIndex, int blockIndex) {
            return uid + ":" + chunkIndex + ":" + blockIndex;
        }

        void Update() {
            // Drain up to MaxModsPerFrame modifications and apply them on main thread
            List<BlockModification> batch = new List<BlockModification>();
            List<string> keysToRemove = new List<string>();
            lock(_lock) {
                int taken = 0;
                foreach(var kv in _pending) {
                    if(taken >= MaxModsPerFrame) break;
                    batch.Add(kv.Value);
                    keysToRemove.Add(kv.Key);
                    taken++;
                }
                // remove taken
                foreach(var k in keysToRemove) _pending.Remove(k);
            }

            if(batch.Count == 0) return;

            HashSet<string> modifiedEntities = new HashSet<string>();
            foreach(var mod in batch) {
                try {
                    if(mod.Entity == null) continue;
                    var chunk = mod.Entity.GetChunkData(mod.ChunkIndex);
                    if(chunk == null) continue;
                    // Only write if value actually changes (reduces churn)
                    if(chunk.GetBlockType(mod.BlockIndex) == mod.NewType) continue;
                    chunk.SetBlockType(mod.BlockIndex, mod.NewType);
                    modifiedEntities.Add(mod.Entity.UID);
                } catch(Exception ex) {
                    Debug.LogError($"BlockModificationQueue: Failed to apply modification: {ex}");
                }
            }

            // Request mesh rebuilds for modified entities (one request per entity)
            var chunkGen = FindFirstObjectByType<ChunkGenerationQueue>();
            if(chunkGen != null) {
                foreach(var uid in modifiedEntities) {
                    GameEntity.GameEntity entity = EntityDatabaseManager.Instance.GetLoadedEntityFromUID(uid);
                    if(entity != null) chunkGen.RequestMeshRebuild(entity);
                }
            }
        }
    }
}

