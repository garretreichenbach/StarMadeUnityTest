using UnityEngine;

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
			//Todo: Track memory usage specific to Chunk Generation, not just total memory
			long memory = UnityEngine.Profiling.Profiler.GetTotalAllocatedMemoryLong() / 1048576;
			return displayMode.HasFlag(StatsDisplay.DisplayMode.MemoryStats) ? $"Memory Usage: {memory} MB" : "";
		}

		public void ClearLastReport() { }
	}
}