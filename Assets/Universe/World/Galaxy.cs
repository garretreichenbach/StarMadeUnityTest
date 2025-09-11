using System;
using UnityEngine;

namespace Universe.World {
	public class Galaxy : MonoBehaviour {
		
		public long seed;
		public Vector3 systemPosition;
		public static Galaxy Instance { get; set; }
		public System.Collections.Generic.HashSet<int> LoadedSectors;

		public Galaxy(long seed, Vector3 systemPosition) {
			this.seed = seed;
			this.systemPosition = systemPosition;
		}

		void Start() {
			Instance = this;
			LoadedSectors = new System.Collections.Generic.HashSet<int>();
			int initialSectorID = (int)(systemPosition.x / Sector.Sector.SectorSize) + 
			                      ((int)(systemPosition.y / Sector.Sector.SectorSize) * 16) + 
			                      ((int)(systemPosition.z / Sector.Sector.SectorSize) * 256);
			LoadSector(initialSectorID, true);
			Debug.Log("Galaxy started with seed: " + seed + " at position: " + systemPosition);
		}

		public void LoadSector(int sectorID, bool loadSurrounding) {
			// Todo: Implement sector loading logic from db, for now just create new sector
			Debug.Log("Loading sector: " + sectorID + " with surrounding: " + loadSurrounding);
			GameObject sectorObject = new GameObject("Sector_" + sectorID);
			sectorObject.transform.parent = this.transform;
			sectorObject.transform.position = new Vector3((sectorID % 16) * Sector.Sector.SectorSize, ((sectorID / 16) % 16) * Sector.Sector.SectorSize, (sectorID / 256) * Sector.Sector.SectorSize);
			sectorObject.AddComponent<Sector.Sector>();
			LoadedSectors.Add(sectorID);
			// If loadSurrounding is true, load adjacent sectors as well (not implemented yet)
		}
	}
}