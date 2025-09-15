using UnityEngine;
using UnityEngine.Rendering;

namespace Universe.Data.Client.Graphics {
	public class BlockOutline : MonoBehaviour {
		//Todo: This should be a drawable as part of some drawable manager, not a game object.
		static readonly Vector3[] Corners = {
			new Vector3(0, 0, 0), new Vector3(1, 0, 0), new Vector3(1, 1, 0), new Vector3(0, 1, 0),
			new Vector3(0, 0, 1), new Vector3(1, 0, 1), new Vector3(1, 1, 1), new Vector3(0, 1, 1),
		};
		static readonly int[,] Edges = {
			{ 0, 1 }, { 1, 2 }, { 2, 3 }, { 3, 0 }, // bottom
			{ 4, 5 }, { 5, 6 }, { 6, 7 }, { 7, 4 }, // top
			{ 0, 4 }, { 1, 5 }, { 2, 6 }, { 3, 7 }, // sides
		};
		static Material _lineMat;
		static readonly int SrcBlend = Shader.PropertyToID("_SrcBlend");
		static readonly int DstBlend = Shader.PropertyToID("_DstBlend");
		static readonly int Cull = Shader.PropertyToID("_Cull");
		static readonly int ZWrite = Shader.PropertyToID("_ZWrite");
		public Color outlineColor = Color.black;
		public float lineWidth = 2f;
		Vector3 _blockWorldPos;
		bool _visible;
		void OnRenderObject() {
			if(!_visible) return;
			Material mat = GetLineMaterial();
			mat.SetPass(0);
			GL.PushMatrix();
			GL.MultMatrix(Matrix4x4.TRS(_blockWorldPos, Quaternion.identity, Vector3.one));
			GL.Begin(GL.LINES);
			GL.Color(outlineColor);
			for(int i = 0; i < 12; i++) {
				GL.Vertex(Corners[Edges[i, 0]]);
				GL.Vertex(Corners[Edges[i, 1]]);
			}
			GL.End();
			GL.PopMatrix();
		}

		public void Show(Vector3 worldPos) {
			_blockWorldPos = worldPos;
			_visible = true;
		}
		public void Hide() {
			_visible = false;
		}
		static Material GetLineMaterial() {
			if(_lineMat == null) {
				Shader shader = Shader.Find("Hidden/Internal-Colored");
				_lineMat = new Material(shader) {
					hideFlags = HideFlags.HideAndDontSave,
				};
				_lineMat.SetInt(SrcBlend, (int)BlendMode.SrcAlpha);
				_lineMat.SetInt(DstBlend, (int)BlendMode.OneMinusSrcAlpha);
				_lineMat.SetInt(Cull, (int)CullMode.Off);
				_lineMat.SetInt(ZWrite, 0);
			}
			return _lineMat;
		}
	}
}