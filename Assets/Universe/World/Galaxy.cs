using UnityEngine;

namespace Universe.World {
	public class Galaxy : MonoBehaviour {
		
		public long seed;
		public Vector3 systemPosition;
		public static Galaxy Instance { get; private set; }

		public Galaxy(long seed, Vector3 systemPosition) {
			this.seed = seed;
			this.systemPosition = systemPosition;
		}
	}
}