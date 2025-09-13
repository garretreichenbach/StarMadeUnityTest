using UnityEngine;

namespace Dev.Testing.Galaxy {
	/**
	 * Debug drawer for visualizing loaded sector grid in the galaxy.
	 */
	public class SectorDebugDrawer : MonoBehaviour {
		readonly Color gizmoColor = Color.green;
		readonly int gridSize = 16; // 16x16x16 grid of sectors
		readonly float sectorSize = 1000f; // Assuming each sector is 1000 units in size

		void Update() {
			Universe.World.Galaxy galaxy = Universe.World.Galaxy.Instance;
			if(galaxy != null) {
				for(int i = 0; i < galaxy.LoadedSectors.Count; i++) {
					int sectorID = -1;
					foreach(int id in galaxy.LoadedSectors) {
						sectorID = id;
						break;
					}
					if(sectorID == -1) continue;

					int x = sectorID % gridSize;
					int y = sectorID / gridSize % gridSize;
					int z = sectorID / (gridSize * gridSize);
					Vector3 sectorPosition = new Vector3(x * sectorSize, y * sectorSize, z * sectorSize);
					Debug.DrawLine(sectorPosition, sectorPosition + new Vector3(sectorSize, 0, 0), gizmoColor);
					Debug.DrawLine(sectorPosition, sectorPosition + new Vector3(0, sectorSize, 0), gizmoColor);
					Debug.DrawLine(sectorPosition, sectorPosition + new Vector3(0, 0, sectorSize), gizmoColor);
					Debug.DrawLine(sectorPosition + new Vector3(sectorSize, sectorSize, 0), sectorPosition + new Vector3(sectorSize, 0, 0), gizmoColor);
					Debug.DrawLine(sectorPosition + new Vector3(sectorSize, sectorSize, 0), sectorPosition + new Vector3(0, sectorSize, 0), gizmoColor);
					Debug.DrawLine(sectorPosition + new Vector3(sectorSize, 0, sectorSize), sectorPosition + new Vector3(sectorSize, 0, 0), gizmoColor);
					Debug.DrawLine(sectorPosition + new Vector3(sectorSize, 0, sectorSize), sectorPosition + new Vector3(0, 0, sectorSize), gizmoColor);
					Debug.DrawLine(sectorPosition + new Vector3(0, sectorSize, sectorSize), sectorPosition + new Vector3(0, sectorSize, 0), gizmoColor);
					Debug.DrawLine(sectorPosition + new Vector3(0, sectorSize, sectorSize), sectorPosition + new Vector3(0, 0, sectorSize), gizmoColor);
					Debug.DrawLine(sectorPosition + new Vector3(sectorSize, sectorSize, sectorSize), sectorPosition + new Vector3(sectorSize, sectorSize, 0), gizmoColor);
					Debug.DrawLine(sectorPosition + new Vector3(sectorSize, sectorSize, sectorSize), sectorPosition + new Vector3(sectorSize, 0, sectorSize), gizmoColor);
					Debug.DrawLine(sectorPosition + new Vector3(sectorSize, sectorSize, sectorSize), sectorPosition + new Vector3(0, sectorSize, sectorSize), gizmoColor);
					Debug.DrawLine(sectorPosition + new Vector3(sectorSize, sectorSize, sectorSize), sectorPosition + new Vector3(0, 0, sectorSize), gizmoColor);
				}
			}
		}
	}
}