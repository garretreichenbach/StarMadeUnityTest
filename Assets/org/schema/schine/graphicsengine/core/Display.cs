using UnityEngine;

namespace Org.Schema.Schine.GraphicsEngine.Core
{
    /// <summary>
    /// This class is a wrapper for the UnityEngine.Screen class.
    /// It is used to provide a similar API to the LWJGL Display class.
    /// </summary>
    public static class Display
    {
        public static int GetWidth()
        {
            return Screen.width;
        }

        public static int GetHeight()
        {
            return Screen.height;
        }

        public static bool IsFullscreen()
        {
            return Screen.fullScreen;
        }

        public static void SetFullscreen(bool fullscreen)
        {
            Screen.fullScreen = fullscreen;
        }

        public static void SetResolution(int width, int height, bool fullscreen)
        {
            Screen.SetResolution(width, height, fullscreen);
        }

        public static string GetTitle()
        {
            return Application.productName;
        }

        public static bool IsActive()
        {
            return Application.isFocused;
        }

        public static void Destroy()
        {
            Application.Quit();
        }
    }
}
