using System;
using Settings;
using TMPro;
using Unity.VisualScripting;
using UnityEngine;
using UnityEngine.UI;

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
			All = FPS | Ping | PacketStats | RenderStats | EntityStats
		}
		
		[InspectorLabel("Current Stats")]
		public string Stats = "";
		
		private TextMeshProUGUI  _statsText;
		public DisplayMode Mode = DisplayMode.FPS | DisplayMode.Ping;
		
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

		private void Update() {
			if(Mode == DisplayMode.None) {
				_statsText.text = "";
				return;
			}
			
			var sb = new System.Text.StringBuilder();
			if((Mode & DisplayMode.FPS) != 0) {
				var fps = 1.0f / Time.unscaledDeltaTime;
				var cap = EngineSettings.Instance.FPSLimit.Value;
				sb.AppendLine(cap == -1 ? $"FPS: {fps:F1}\n" : $"FPS: {fps:F1}/{cap}\n");
			}

			if((Mode & DisplayMode.Ping) != 0) {
				//Todo: Networking
				sb.AppendLine($"Ping: -1ms\n");
			}

			if((Mode & DisplayMode.PacketStats) != 0) {
				//Todo: Networking
				sb.AppendLine("Packet Stats: N/A\n");
			}
			
			if((Mode & DisplayMode.RenderStats) != 0) {
				//Todo: Segment drawing
				sb.AppendLine("Segment Render Stats: N/A\n");
			}

			if((Mode & DisplayMode.EntityStats) != 0) { 
				//Todo: Entity stats
				sb.AppendLine("Entity Stats: N/A\n");
			}
			
			_statsText.text = sb.ToString().TrimEnd('\n');
			Stats = _statsText.text;
		}
	}
}