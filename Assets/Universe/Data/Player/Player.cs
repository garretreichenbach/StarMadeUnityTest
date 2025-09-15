using Settings;
using UnityEngine;
using Universe.Data.Inventory;
using Universe.World;

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
		Inventory.PlayerInventory _inventory;

		void Start() {
			Cursor.lockState = CursorLockMode.Locked;
			Cursor.visible = false;
			_inventory = new PlayerInventory();
		}

		void Update() {
			if(!_initialized) {
				return;
			}
			if(!_controlState.InventoryActive) {
				HandleMovement();
				HandleLook();
				HandleBlockInput();
				HandleBlockSelectionInput();
			}
			HandleInventoryInput();
			CheckSectorTransition(); //Todo: Move this to somewhere else, maybe a GameManager or similar
		}

		public void Initialize(int playerStateID, string playerName) {
			_playerStateID = playerStateID;
			_playerName = playerName;
			_controlState = new PlayerControlState {
				ControlledEntityID = -1,
				CurrentAlignedEntityID = -1,
				LastEnteredBlockIndex = -1,
			};
			_initialized = true;
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
			slot.count--;
			if(slot.count == 0) {
				slot.id = 0;
				if(slot.count < 0) {
					slot.count = 0;
					Debug.LogWarning("Inventory slot count went below 0!");
				}
			}

			Ray ray = playerCamera.ScreenPointToRay(new Vector3(Screen.width / 2, Screen.height / 2));
			if(Physics.Raycast(ray, out RaycastHit hit, 5f)) {
				Vector3 placePos = hit.point + hit.normal * 0.5f;
				placePos = new Vector3(Mathf.Round(placePos.x), Mathf.Round(placePos.y), Mathf.Round(placePos.z));

			}
		}

		public void RemoveBlock() {
			Ray ray = playerCamera.ScreenPointToRay(new Vector3(Screen.width / 2, Screen.height / 2));
			if(Physics.Raycast(ray, out RaycastHit hit, 5f)) {
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