using System; // For IComparable
using UnityEngine; // For Vector3

namespace Org.Schema.Schine.GraphicsEngine.Core
{
    public interface ZSortedDrawable : IComparable<ZSortedDrawable>, Drawable
    {
        void DrawZSorted(); // Renamed to PascalCase

        float[] GetBufferedTransformation(); // Renamed to PascalCase, FloatBuffer to float[]

        Vector3 GetBufferedTransformationPosition(); // Renamed to PascalCase, Vector3f to Vector3
    }
}