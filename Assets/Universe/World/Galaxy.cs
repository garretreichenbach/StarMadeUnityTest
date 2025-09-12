using System.Collections.Generic;
using UnityEngine;

namespace Universe.World {
	public class Galaxy : MonoBehaviour {

		public long seed;
		public Vector3 systemPosition;
		public HashSet<int> LoadedSectors;

		public Galaxy(long seed, Vector3 systemPosition) {
			this.seed = seed;
			this.systemPosition = systemPosition;
		}

		public static Galaxy Instance { get; set; }

		void Start() {
			Instance = this;
			LoadedSectors = new HashSet<int>();
		}

		public void LoadSector(int sectorID, bool loadSurrounding) {
			// Todo: Implement sector loading logic from db, for now just create new sector
			Debug.Log("Loading sector: " + sectorID + " with surrounding: " + loadSurrounding);
			GameObject sectorObject = new GameObject("Sector_" + sectorID);
			sectorObject.transform.parent = transform;
			sectorObject.transform.position = new Vector3(sectorID % 16 * Sector.Sector.SectorSize, sectorID / 16 % 16 * Sector.Sector.SectorSize, sectorID / 256 * Sector.Sector.SectorSize);
			sectorObject.AddComponent<Sector.Sector>();
			LoadedSectors.Add(sectorID);
			// If loadSurrounding is true, load adjacent sectors as well (not implemented yet)
		}
	}
}