using System.Collections.Generic;
using Settings;
using Unity.VisualScripting;
using UnityEngine;

namespace Universe.World {
	public class Galaxy : MonoBehaviour {

		public long seed;
		public Vector3 systemPosition;
		public HashSet<int> LoadedSectors;

		int GalaxyRadius => ServerSettings.Instance.GalaxyRadius.Value;
		int SystemSize => ServerSettings.Instance.SystemSize.Value;
		int SectorSize => ServerSettings.Instance.SectorSize.Value;

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
			// Debug.Log("Loading sector: " + sectorID + " with surrounding: " + loadSurrounding);
			// GameObject sectorObject = new GameObject("Sector_" + sectorID);
			// Sector sector = gameObject.AddComponent<Sector>();
			// sector.transform.parent = transform;
			// sector.transform.position = new Vector3(sectorID % 16 * Sector.Sector.SectorSize, sectorID / 16 % 16 * Sector.Sector.SectorSize, sectorID / 256 * Sector.Sector.SectorSize); LoadedSectors.Add(sectorID);
			// If loadSurrounding is true, load adjacent sectors as well (not implemented yet)
		}

		public Vector3Int GetSystemCoordsFromID(int systemID) {
			int x = systemID % (GalaxyRadius * 2);
			int y = (systemID / (GalaxyRadius * 2)) % (GalaxyRadius * 2);
			int z = systemID / ((GalaxyRadius * 2) * (GalaxyRadius * 2));
			return new Vector3Int(x - GalaxyRadius, y - GalaxyRadius, z - GalaxyRadius);
		}

		public int GetSystemIDFromCoords(Vector3Int value) {
			if(Mathf.Abs(value.x) > GalaxyRadius || Mathf.Abs(value.y) > GalaxyRadius || Mathf.Abs(value.z) > GalaxyRadius) {
				Debug.LogError("System coordinates out of bounds: " + value);
				return -1;
			}
			int x = value.x + GalaxyRadius;
			int y = value.y + GalaxyRadius;
			int z = value.z + GalaxyRadius;
			return x + y * (GalaxyRadius * 2) + z * (GalaxyRadius * 2) * (GalaxyRadius * 2);
		}

		public Vector3Int GetSectorCoordsFromID(int sectorID) {
			int x = sectorID % (SystemSize * 16);
			int y = (sectorID / (SystemSize * 16)) % (SystemSize * 16);
			int z = sectorID / ((SystemSize * 16) * (SystemSize * 16));
			return new Vector3Int(x - SystemSize * 8, y - SystemSize * 8, z - SystemSize * 8);
		}

		public int GetSectorIDFromCoords(Vector3Int value) {
			if(Mathf.Abs(value.x) > SystemSize * 8 || Mathf.Abs(value.y) > SystemSize * 8 || Mathf.Abs(value.z) > SystemSize * 8) {
				Debug.LogError("Sector coordinates out of bounds: " + value);
				return -1;
			}
			int x = value.x + SystemSize * 8;
			int y = value.y + SystemSize * 8;
			int z = value.z + SystemSize * 8;
			return x + y * (SystemSize * 16) + z * (SystemSize * 16) * (SystemSize * 16);
		}
	}
}