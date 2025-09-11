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

		public struct PlayerControlState {
			// The entity the player is currently controlling (ship, station, etc), -1 if on foot
			public int ControlledEntityID;

			// The entity the player is currently aligned with, only applicable if on foot, -1 if not aligned or currently controlling an entity
			// When a player is aligned with an entity their position and rotation is always updated relative to that entity
			public int CurrentAlignedEntityID;

			//The last block index the player entered to control an entity, -1 if none
			public int LastEnteredBlockIndex;
		}

		public PlayerController(int playerStateID, String playerName) {
			_playerStateID = playerStateID;
			_playerName = playerName;
			_controlState = new PlayerControlState {
				ControlledEntityID = -1,
				CurrentAlignedEntityID = -1,
				LastEnteredBlockIndex = -1
			};
		}

		void Start() {
			
		}

		void Update() { }
	}
}