using System;
using UnityEngine;

namespace Dev {
	public class MemoryReporter : MonoBehaviour, StatsDisplay.IStatsDisplayReporter {
		void Start() {
			StatsDisplay statsDisplay = FindFirstObjectByType<StatsDisplay>();
			if(statsDisplay != null) {
				statsDisplay.Reporters.Add(this);
			} else {
				Debug.LogWarning("No StatsDisplay found in scene. FPSReporter will not report stats.");
			}
		}

		public string Report(StatsDisplay.DisplayMode displayMode, float deltaTime) {
			long memory = GC.GetTotalMemory(false) / (1024 * 1024);
			return displayMode.HasFlag(StatsDisplay.DisplayMode.MemoryStats) ? $"Memory: {memory} MB" : "";
		}

		public void ClearLastReport() { }
	}
}