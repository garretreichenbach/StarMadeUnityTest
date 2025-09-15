using UnityEngine;
using Universe.Data.Player;

namespace Dev.Testing.Galaxy {
	public class GalaxySceneTest : MonoBehaviour {
		public long seed = 123456789;
		public Vector3 systemPosition = Vector3.zero;

		void Start() {
			Universe.World.Galaxy.Instance = gameObject.AddComponent<Universe.World.Galaxy>();
			Debug.Log("Galaxy initialized with seed: " + seed + " at position: " + systemPosition);

			Player player = gameObject.AddComponent<Player>();
			player.Initialize(1, "TestPlayer");
			Debug.Log("PlayerController initialized for player: TestPlayer");
		}
	}
}