using UnityEngine;

namespace Universe.Tools {
	public class CompressionTestRunner : MonoBehaviour {
		[Tooltip("Chunk ID to test")]
		public long ChunkID;

		[ContextMenu("Run Compression Round-Trip Test")]
		public async void RunCompressionRoundTripTest() {
			if(ChunkID < 0) {
				Debug.LogError("CompressionTestRunner: Set ChunkID before running test.");
				return;
			}
			var mgr = Universe.Data.Chunk.ChunkMemoryManager.Instance;
			if(mgr == null) {
				Debug.LogError("CompressionTestRunner: ChunkMemoryManager.Instance is null");
				return;
			}

			Debug.Log($"CompressionTestRunner: Starting round-trip test for chunk {ChunkID}...");
			bool ok = false;
			try {
				ok = await mgr.TestCompressionRoundTrip(ChunkID);
			} catch(System.Exception ex) {
				Debug.LogError($"CompressionTestRunner: Exception during test: {ex.Message}");
			}
			Debug.Log($"CompressionTestRunner: Round-trip test for chunk {ChunkID} result: {ok}");
		}
	}
}

