using System;
using System.Collections.Generic;
using System.Text;
using Unity.VisualScripting;
using UnityEngine;

namespace Dev {

	/**
	 * Simple stats display for showing FPS, ping, network stats, render stats, entity stats, and sector stats.
	 */
	public class StatsDisplay : MonoBehaviour {
		[Flags]
		public enum DisplayMode {
			None = 0,
			FPS = 1,
			Ping = 2,
			PacketStats = 4,
			RenderStats = 8,
			EntityStats = 16,
			MemoryStats = 32,
			All = FPS | Ping | PacketStats | RenderStats | EntityStats | MemoryStats,
		}


		[InspectorLabel("Stats")]
		public string stats = "";

		public DisplayMode Mode = DisplayMode.FPS | DisplayMode.Ping;

		public readonly List<IStatsDisplayReporter> Reporters = new List<IStatsDisplayReporter>();

		void Start() {
			if(Mode == DisplayMode.None) {
				enabled = false;
			}
		}

		void Update() {
			if(Mode == DisplayMode.None) {
				return;
			}

			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < Reporters.Count; i++) {
				IStatsDisplayReporter reporter = Reporters[i];
				sb.AppendLine(reporter.Report(Mode, Time.deltaTime));
			}

			stats = sb.ToString().TrimEnd('\n');
		}

		//Todo: Add some way to visualize chunk borders better, like a grid overlay or colored edges

		public interface IStatsDisplayReporter {
			string Report(DisplayMode displayMode, float deltaTime);

			void ClearLastReport();
		}
	}
}