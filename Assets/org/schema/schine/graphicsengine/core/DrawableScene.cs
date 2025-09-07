using UnityEngine;

namespace Org.Schema.Schine.GraphicsEngine.Core {
	/// <summary>
	/// The Interface DrawableScene.
	/// </summary>
	public interface IDrawableScene : IDrawable {
		new void CleanUp();

		void DrawScene();

		FrameBufferObjects GetFbo();

		Light GetLight();

		void InitProjection(int ocMode);

		void Reshape();

		void Update(Timer timer);
	}
}