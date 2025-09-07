using UnityEngine; // For MouseButton enum values if using Unity's Input system

namespace Org.Schema.Schine.GraphicsEngine.Core
{
    public enum MouseButton
    {
        MouseLeft = 0,
        MouseRight = 1,
        MouseMiddle = 2,
        Mouse3 = 3,
        Mouse4 = 4,
        Mouse5 = 5,
        Mouse6 = 6,
        Mouse7 = 7,
        Mouse8 = 8,
        Mouse9 = 9,
        Mouse10 = 10,
        Mouse11 = 11,
        Mouse12 = 12,
        Mouse13 = 13,
        Mouse14 = 14,
        Mouse15 = 15,
    }

    public static class MouseButtonExtensions
    {
        public static string GetName(this MouseButton button)
        {
            switch ((int)button)
            {
                case 0: return "Mouse Left";
                case 1: return "Mouse Right";
                case 2: return "Mouse Middle";
                default: return $"Mouse Button {(int)button}";
            }
        }
    }
}