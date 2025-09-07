namespace Org.Schema.Schine.GraphicsEngine.Core
{
    /// <summary>
    /// The Interface Drawable.
    /// </summary>
    public interface IDrawable
    {
        void CleanUp();

        void Draw();

        bool IsInvisible();

        void OnInit();
    }
}
