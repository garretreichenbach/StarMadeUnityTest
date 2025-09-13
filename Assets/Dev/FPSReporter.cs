using UnityEngine;

namespace Dev {
	public class FPSReporter : MonoBehaviour, StatsDisplay.IStatsDisplayReporter {

		void Start() {
			StatsDisplay statsDisplay = FindFirstObjectByType<StatsDisplay>();
			if(statsDisplay != null) {
				statsDisplay.Reporters.Add(this);
			} else {
				Debug.LogWarning("No StatsDisplay found in scene. FPSReporter will not report stats.");
			}
		}

		public string Report(StatsDisplay.DisplayMode displayMode, float deltaTime) {
			return $"FPS: {1f / deltaTime}";
		}

		public void ClearLastReport() { }
	}
}