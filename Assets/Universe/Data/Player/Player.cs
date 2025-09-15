using Settings;
using UnityEngine;
using Universe.Data.Inventory;
using Universe.World;
using Universe.Data.World;
using Universe.Data.Chunk;

namespace Universe.Data.Player {
	/**
	* Controls player state information and handles loading of sectors near the player.
	*/
	public class Player : MonoBehaviour {
		PlayerControlState _controlState;
		int _currentSectorID = -1;
		int _factionID = -1;
		bool _initialized;
		string _playerName;
		int _playerStateID = -1;

		[SerializeField]
		float moveSpeed = 5f;
		[SerializeField]
		float lookSensitivity = 2f;
		[SerializeField]
		Camera playerCamera;

		[SerializeField]
		PlayerInventory _inventory;

		void Start() {
			Cursor.lockState = CursorLockMode.Locked;
			Cursor.visible = false;
			playerCamera = GetComponentInChildren<Camera>();
			_controlState = new PlayerControlState {
				InventoryActive = false,
				ControlledEntityID = -1,
				CurrentAlignedEntityID = -1,
				LastEnteredBlockIndex = -1,
			};
			_inventory = new PlayerInventory();
		}

		void Update() {
			if(!_initialized) {
				return;
			}
			if(!_controlState.InventoryActive) {
				//Todo: Proper player input controller
				HandleMovement();
				HandleLook();
				HandleBlockInput();
				HandleBlockSelectionInput();
			}
			HandleInventoryInput();
			CheckSectorTransition(); //Todo: Move this to somewhere else, maybe a GameManager or similar
		}

		void CheckSectorTransition() {
			int sectorSize = ServerSettings.Instance.SectorSize.Value;
			Vector3 playerPosition = transform.position;
			int newSectorX = Mathf.FloorToInt(playerPosition.x / sectorSize);
			int newSectorY = Mathf.FloorToInt(playerPosition.y / sectorSize);
			int newSectorZ = Mathf.FloorToInt(playerPosition.z / sectorSize);
			int newSectorID = newSectorX + newSectorY * 1000 + newSectorZ * 1000000;
			if(newSectorID != _currentSectorID) {
				// Player has moved to a new sector
				_currentSectorID = newSectorID;
				Galaxy.Instance.LoadSector(_currentSectorID, true);
				Debug.Log("Player moved to sector: " + _currentSectorID);
			}
		}

		void HandleMovement() {
			float h = Input.GetAxis("Horizontal");
			float v = Input.GetAxis("Vertical");
			Vector3 move = transform.right * h + transform.forward * v;
			transform.position += move * (moveSpeed * Time.deltaTime);
		}

		void HandleLook() {
			float mouseX = Input.GetAxis("Mouse X") * lookSensitivity;
			float mouseY = Input.GetAxis("Mouse Y") * lookSensitivity;
			_controlState.Pitch -= mouseY;
			_controlState.Pitch = Mathf.Clamp(_controlState.Pitch, -90f, 90f);
			playerCamera.transform.localEulerAngles = new Vector3(_controlState.Pitch, 0f, 0f);
			transform.Rotate(Vector3.up * mouseX);
		}

		void HandleBlockInput() {
			if(Input.GetMouseButtonDown(0)) {
				RemoveBlock();
			}
			if(Input.GetMouseButtonDown(1)) {
				PlaceBlock();
			}
		}

		void HandleBlockSelectionInput() {
			float scroll = Input.GetAxis("Mouse ScrollWheel");
			if(scroll > 0f) {
				_inventory.SelectNext();
			} else if(scroll < 0f) {
				_inventory.SelectPrevious();
			}
		}

		void HandleInventoryInput() {
			if(Input.GetKeyDown(KeyCode.I)) {
				_controlState.InventoryActive = !_controlState.InventoryActive;
				Cursor.lockState = _controlState.InventoryActive ? CursorLockMode.None : CursorLockMode.Locked;
				Cursor.visible = _controlState.InventoryActive;
			}
		}

		public void PlaceBlock() {
			InventorySlot slot = _inventory.GetSelectedSelectedSlot();
			if(slot.id == 0 || !slot.GetElementInfo().IsPlacable) {
				return; // No block selected
			}
			Ray ray = playerCamera.ScreenPointToRay(new Vector3(Screen.width / 2, Screen.height / 2));
			// Raycast to find entity and block
			GameEntity.GameEntity entity = null;
			if(Physics.Raycast(ray, out RaycastHit entityHit, 5f)) {
				entity = entityHit.collider.GetComponent<GameEntity.GameEntity>();
			}
			if(entity == null || !entity.Loaded) return;
			var hit = BlockRaycast.Raycast(entity, ray, 5f);
			if(hit.valid) {
				// Place block at adjacent position
				Vector3Int placePos = hit.blockPosition + Vector3Int.RoundToInt(hit.hitNormal);
				int chunkSize = IChunkData.ChunkSize;
				Vector3Int chunkDims = entity.ChunkDimensions;
				int cx = placePos.x / chunkSize;
				int cy = placePos.y / chunkSize;
				int cz = placePos.z / chunkSize;
				if(cx < 0 || cy < 0 || cz < 0 || cx >= chunkDims.x || cy >= chunkDims.y || cz >= chunkDims.z) return;
				int chunkIndex = cx + cy * chunkDims.x + cz * chunkDims.x * chunkDims.y;
				var chunk = entity.GetChunkData(chunkIndex);
				if(chunk == null) return;
				int bx = placePos.x - cx * chunkSize;
				int by = placePos.y - cy * chunkSize;
				int bz = placePos.z - cz * chunkSize;
				if(bx < 0 || by < 0 || bz < 0 || bx >= chunkSize || by >= chunkSize || bz >= chunkSize) return;
				int blockIndex = bx + by * chunkSize + bz * chunkSize * chunkSize;
				if(chunk.GetBlockType(blockIndex) != 0) return; // Only place if empty
				chunk.SetBlockType(blockIndex, slot.id);
				// Remove from inventory
				slot.count--;
				if(slot.count == 0) slot.id = 0;
				// Update slot in inventory
				_inventory.slots[_inventory.GetSelectedSelectedSlotIndex()] = slot;
				entity.RequestMeshRebuild();
			}
		}

		public void RemoveBlock() {
			// Raycast to find entity and block
			GameEntity.GameEntity entity = null;
			Ray ray = playerCamera.ScreenPointToRay(new Vector3(Screen.width / 2, Screen.height / 2));
			if(Physics.Raycast(ray, out RaycastHit entityHit, 5f)) {
				entity = entityHit.collider.GetComponent<GameEntity.GameEntity>();
			}
			if(entity == null || !entity.Loaded) return;
			ray = playerCamera.ScreenPointToRay(new Vector3(Screen.width / 2, Screen.height / 2));
			var hit = BlockRaycast.Raycast(entity, ray, 5f);
			if(hit.valid) {
				var chunk = entity.GetChunkData(hit.chunkIndex);
				if(chunk == null) return;
				short removedType = chunk.GetBlockType(hit.blockIndex);
				if(removedType == 0) return;
				chunk.SetBlockType(hit.blockIndex, 0);
				// Add to inventory
				_inventory.AddToAnySlot(removedType, 1);
				entity.RequestMeshRebuild();
			}
		}

		public struct PlayerControlState {
			// The entity the player is currently controlling (ship, station, etc), -1 if on foot
			public int ControlledEntityID;

			// The entity the player is currently aligned with, only applicable if on foot, -1 if not aligned or currently controlling an entity
			// When a player is aligned with an entity their position and rotation is always updated relative to that entity
			public int CurrentAlignedEntityID;

			//The last block index the player entered to control an entity, -1 if none
			public int LastEnteredBlockIndex;

			public bool InventoryActive;

			public float Pitch;
		}
	}
}