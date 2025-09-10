using UnityEngine;
using Universe.Data.Chunk;

namespace Dev {
	public class MemoryReporter : MonoBehaviour, StatsDisplay.IStatsDisplayReporter {
		void Start() {
			var statsDisplay = FindObjectOfType<StatsDisplay>();
			if(statsDisplay != null) {
				statsDisplay.Reporters.Add(this);
			} else {
				Debug.LogWarning("No StatsDisplay found in scene. FPSReporter will not report stats.");
			}
		}

		public string Report(StatsDisplay.DisplayMode displayMode, float deltaTime) {
			long memory = ChunkAllocator.TotalAllocatedMemory / 1048576;
			return displayMode.HasFlag(StatsDisplay.DisplayMode.MemoryStats) ? $"Chunk Memory: {memory} MB" : "";
		}

		public void ClearLastReport() { }
	}
}