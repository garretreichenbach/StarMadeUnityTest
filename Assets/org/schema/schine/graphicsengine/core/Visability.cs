using UnityEngine; // For Vector3

namespace Org.Schema.Schine.GraphicsEngine.Core
{
    public abstract class Visability
    {
        public abstract Vector3 GetVisability(); // Renamed to PascalCase

        public int GetVisibleDistance() // Renamed to PascalCase
        {
            return 300;
        }

        public abstract float GetVisLen(); // Renamed to PascalCase

        public abstract void RecalculateVisibility(); // Renamed to PascalCase
    }
}