using System;
using UnityEngine;

namespace Universe.Data.Player {
	/**
	 * Controls player state information and handles loading of sectors near the player.
	 */
	public class PlayerController : MonoBehaviour {
		private int _playerStateID = -1;
		private String _playerName;
		private int _currentSectorID = -1;
		private int _factionID = -1;
		private PlayerControlState _controlState;
		private bool _initialized;

		public struct PlayerControlState {
			// The entity the player is currently controlling (ship, station, etc), -1 if on foot
			public int ControlledEntityID;

			// The entity the player is currently aligned with, only applicable if on foot, -1 if not aligned or currently controlling an entity
			// When a player is aligned with an entity their position and rotation is always updated relative to that entity
			public int CurrentAlignedEntityID;

			//The last block index the player entered to control an entity, -1 if none
			public int LastEnteredBlockIndex;
		}

		public void initialize(int playerStateID, String playerName) {
			_playerStateID = playerStateID;
			_playerName = playerName;
			_controlState = new PlayerControlState {
				ControlledEntityID = -1,
				CurrentAlignedEntityID = -1,
				LastEnteredBlockIndex = -1
			};
			_initialized = true;
		}

		void Start() {
			
		}

		void Update() {
			if(!_initialized) {
				return;
			}
			
			// Update player state, load/unload sectors as needed based on player position
			// For now, we only have to ask the galaxy to load sectors around the player
			CheckSectorTransition();
		}

		private void CheckSectorTransition() {
			int sectorSize = World.Sector.Sector.SectorSize;
			Vector3 playerPosition = transform.position;
			int newSectorX = Mathf.FloorToInt(playerPosition.x / sectorSize);
			int newSectorY = Mathf.FloorToInt(playerPosition.y / sectorSize);
			int newSectorZ = Mathf.FloorToInt(playerPosition.z / sectorSize);
			int newSectorID = newSectorX + newSectorY * 1000 + newSectorZ * 1000000;
			if(newSectorID != _currentSectorID) {
				// Player has moved to a new sector
				_currentSectorID = newSectorID;
				World.Galaxy.Instance.LoadSector(_currentSectorID, true);
				Debug.Log("Player moved to sector: " + _currentSectorID);
			}
		}
	}
}