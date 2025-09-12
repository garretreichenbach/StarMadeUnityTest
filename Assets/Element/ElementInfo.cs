using UnityEngine;

namespace Element {
	public class ElementInfo : MonoBehaviour {

		public static Mesh GetMesh(short blockType) {
			// Return a unit cube (size 1) centered at origin with unique vertices per face
			// so normals are correct and each face renders independently.
			Mesh mesh = new Mesh();
			var v = new[] {
				// +X (right)
				new Vector3(0.5f, -0.5f, -0.5f), new Vector3(0.5f, -0.5f, 0.5f), new Vector3(0.5f, 0.5f, 0.5f), new Vector3(0.5f, 0.5f, -0.5f),
				// -X (left)
				new Vector3(-0.5f, -0.5f, 0.5f), new Vector3(-0.5f, -0.5f, -0.5f), new Vector3(-0.5f, 0.5f, -0.5f), new Vector3(-0.5f, 0.5f, 0.5f),
				// +Y (top)
				new Vector3(-0.5f, 0.5f, -0.5f), new Vector3(0.5f, 0.5f, -0.5f), new Vector3(0.5f, 0.5f, 0.5f), new Vector3(-0.5f, 0.5f, 0.5f),
				// -Y (bottom)
				new Vector3(-0.5f, -0.5f, 0.5f), new Vector3(0.5f, -0.5f, 0.5f), new Vector3(0.5f, -0.5f, -0.5f), new Vector3(-0.5f, -0.5f, -0.5f),
				// +Z (front)
				new Vector3(-0.5f, -0.5f, 0.5f), new Vector3(-0.5f, 0.5f, 0.5f), new Vector3(0.5f, 0.5f, 0.5f), new Vector3(0.5f, -0.5f, 0.5f),
				// -Z (back)
				new Vector3(0.5f, -0.5f, -0.5f), new Vector3(0.5f, 0.5f, -0.5f), new Vector3(-0.5f, 0.5f, -0.5f), new Vector3(-0.5f, -0.5f, -0.5f),
			};
			var n = new[] {
				Vector3.right, Vector3.right, Vector3.right, Vector3.right,
				Vector3.left, Vector3.left, Vector3.left, Vector3.left,
				Vector3.up, Vector3.up, Vector3.up, Vector3.up,
				Vector3.down, Vector3.down, Vector3.down, Vector3.down,
				Vector3.forward, Vector3.forward, Vector3.forward, Vector3.forward,
				Vector3.back, Vector3.back, Vector3.back, Vector3.back,
			};
			var uv = new[] {
				new Vector2(0, 0), new Vector2(1, 0), new Vector2(1, 1), new Vector2(0, 1),
				new Vector2(0, 0), new Vector2(1, 0), new Vector2(1, 1), new Vector2(0, 1),
				new Vector2(0, 0), new Vector2(1, 0), new Vector2(1, 1), new Vector2(0, 1),
				new Vector2(0, 0), new Vector2(1, 0), new Vector2(1, 1), new Vector2(0, 1),
				new Vector2(0, 0), new Vector2(1, 0), new Vector2(1, 1), new Vector2(0, 1),
				new Vector2(0, 0), new Vector2(1, 0), new Vector2(1, 1), new Vector2(0, 1),
			};
			int[] t = {
				// +X (reverse winding to face outward)
				0, 2, 1, 0, 3, 2,
				// -X
				4, 6, 5, 4, 7, 6,
				// +Y
				8, 10, 9, 8, 11, 10,
				// -Y
				12, 14, 13, 12, 15, 14,
				// +Z
				16, 18, 17, 16, 19, 18,
				// -Z
				20, 22, 21, 20, 23, 22,
			};
			mesh.vertices = v;
			mesh.normals = n;
			mesh.uv = uv;
			mesh.triangles = t;
			mesh.RecalculateBounds();
			return mesh;
		}
	}
}