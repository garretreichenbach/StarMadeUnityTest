using System;
using System.Collections.Generic;
using TMPro;
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

		public interface IStatsDisplayReporter {
			string Report(DisplayMode displayMode, float deltaTime);

			void ClearLastReport();
		}

		
		[InspectorLabel("Current Stats")]
		public string Stats = "";

		TextMeshProUGUI  _statsText;
		public DisplayMode Mode = DisplayMode.FPS | DisplayMode.Ping;

		public readonly List<IStatsDisplayReporter> Reporters = new List<IStatsDisplayReporter>();
		
		void Start() {
			_statsText = GetComponent<Canvas>().GetComponentInChildren<TextMeshProUGUI>();
			if(_statsText == null) {
				Debug.LogError("StatsDisplay requires a Text component.");
				enabled = false;
				return;
			}
			if(Mode == DisplayMode.None) {
				enabled = false;
			}
		}

		void Update() {
			if(Mode == DisplayMode.None) {
				_statsText.text = "";
				return;
			}

			var sb = new System.Text.StringBuilder();
			for(int i = 0; i < Reporters.Count; i++) {
				var reporter = Reporters[i];
				sb.AppendLine(reporter.Report(Mode, Time.deltaTime));
			}

			_statsText.text = sb.ToString().TrimEnd('\n');
			Stats = _statsText.text;
		}
	}
}