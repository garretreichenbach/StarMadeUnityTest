using System.Collections.Generic;
using System.Threading.Tasks;
using UnityEngine;

namespace Universe.Tools {
    public class RecompressAllUncompressed : MonoBehaviour {
        [ContextMenu("Recompress All Uncompressed Chunks")]
        public async void RecompressAll() {
            var mgr = Universe.Data.Chunk.ChunkMemoryManager.Instance;
            if(mgr == null) {
                Debug.LogError("ChunkMemoryManager.Instance is null");
                return;
            }

            Debug.Log("RecompressAll: Starting recompression of all uncompressed chunks...");
            // Use public API provided by ChunkMemoryManager to get IDs and headers
            var keys = new List<long>(mgr.GetAllChunkIDs());

            int success = 0;
            int failed = 0;

            foreach(var chunkID in keys) {
                try {
                    if(mgr.TryGetHeader(chunkID, out var header)) {
                        if(header.State == Universe.Data.Chunk.ChunkMemoryManager.ChunkState.Uncompressed) {
                            bool ok = await mgr.CompressChunk(chunkID);
                            if(ok) success++; else failed++;
                            await Task.Delay(1); // yield
                        }
                    }
                 } catch(System.Exception ex) {
                     failed++;
                     Debug.LogError($"RecompressAll: failed for chunk {chunkID}: {ex.Message}");
                 }
             }

             Debug.Log($"RecompressAll: completed. success={success}, failed={failed}");
         }
     }
 }
