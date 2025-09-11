using UnityEngine;

namespace Dev.Testing.Galaxy {
	/** 
	 * Debug drawer for visualizing loaded sector grid in the galaxy.
	 */
	public class SectorDebugDrawer : MonoBehaviour {
		
		bool showGizmos = true;
		Color gizmoColor = Color.green;
		float sectorSize = 1000f; // Assuming each sector is 1000 units in size
		int gridSize = 16; // 16x16 grid of sectors
		
		
		void Start() {
			
		}

		void Update() {
			Universe.World.Galaxy galaxy = Universe.World.Galaxy.Instance;
			if(galaxy != null) {
				var loadedSectorMap = galaxy.LoadedSectors;
				for(int i = 0; i < loadedSectorMap.Count; i++) {
					int sectorID = -1;
					foreach(var id in loadedSectorMap) {
						sectorID = id;
						break;
					}
					if(sectorID == -1) continue;
					
					int x = sectorID % gridSize;
					int y = (sectorID / gridSize) % gridSize;
					int z = sectorID / (gridSize * gridSize);
					
					Vector3 sectorPosition = new Vector3(x * sectorSize, 0, z * sectorSize);
					
					if(showGizmos) {
						Debug.DrawLine(sectorPosition, sectorPosition + new Vector3(sectorSize, 0, 0), gizmoColor);
						Debug.DrawLine(sectorPosition, sectorPosition + new Vector3(0, 0, sectorSize), gizmoColor);
						Debug.DrawLine(sectorPosition + new Vector3(sectorSize, 0, 0), sectorPosition + new Vector3(sectorSize, 0, sectorSize), gizmoColor);
						Debug.DrawLine(sectorPosition + new Vector3(0, 0, sectorSize), sectorPosition + new Vector3(sectorSize, 0, sectorSize), gizmoColor);
					}
				}
			}
		}
	}
}