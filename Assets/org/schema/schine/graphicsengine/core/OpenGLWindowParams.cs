using System;
using UnityEngine; // For Screen, Debug
using System.IO; // For IOException

// Placeholders for custom settings classes
namespace Org.Schema.Schine.GraphicsEngine.Core.Settings
{
    public interface ResolutionInterface
    {
        int getWidth();
        int getHeight();
    }

    public class Resolution : ResolutionInterface
    {
        public string Name;
        public int width;
        public int height;

        public Resolution(string name, int width, int height)
        {
            Name = name;
            this.width = width;
            this.height = height;
        }

        public int getWidth() { return width; }
        public int getHeight() { return height; }
    }

    public class EngineSettings
    {
        public static G_RESOLUTION_Setting G_RESOLUTION = new G_RESOLUTION_Setting();
        public static G_FULLSCREEN_Setting G_FULLSCREEN = new G_FULLSCREEN_Setting();
        public static AUTOSET_RESOLUTION_Setting AUTOSET_RESOLUTION = new AUTOSET_RESOLUTION_Setting();

        public static void write() { Debug.Log("EngineSettings.write called."); } // Placeholder
    }

    public class G_RESOLUTION_Setting
    {
        public object getObject() { return null; } // Placeholder
        public void addPossibilityObject(Resolution res) { Debug.Log($"Added resolution possibility: {res.width}x{res.height}"); }
        public void setObject(Resolution res) { Debug.Log($"Set resolution object: {res.width}x{res.height}"); }
    }

    public class G_FULLSCREEN_Setting
    {
        public bool isOn() { return false; } // Placeholder
        public void setOn(bool on) { Debug.Log($"Fullscreen set to: {on}"); }
    }

    public class AUTOSET_RESOLUTION_Setting
    {
        public bool isOn() { return false; } // Placeholder
        public void setOn(bool on) { Debug.Log($"Autoset resolution set to: {on}"); }
    }
}

namespace Org.Schema.Schine.GraphicsEngine.Core
{
    public class OpenGLWindowParams
    {
        public string Title = "StarMade"; // Renamed to PascalCase
        public Org.Schema.Schine.GraphicsEngine.Core.Settings.ResolutionInterface Res; // Renamed to PascalCase
        public bool Resizable = true; // Renamed to PascalCase

        public bool Changed; // Renamed to PascalCase

        public long Monitor = 0; // Replaced NULL with 0 (long)

        public long SelectedMonitor = 0; // Replaced NULL with 0 (long)

        public long Share = 0; // Replaced NULL with 0 (long)

        public bool Fullscreen; // Renamed to PascalCase
        public int RefreshRate; // Renamed to PascalCase
        private Org.Schema.Schine.GraphicsEngine.Core.Settings.Resolution _nativeRes; // Renamed to PascalCase
        public bool Vsync = false; // Renamed to PascalCase

        public OpenGLWindowParams()
        {
            // Initialize Res from EngineSettings.G_RESOLUTION
            Res = (Org.Schema.Schine.GraphicsEngine.Core.Settings.ResolutionInterface)Org.Schema.Schine.GraphicsEngine.Core.Settings.EngineSettings.G_RESOLUTION.getObject();
        }

        public void UpdateMonitor() // Renamed to PascalCase
        {
            // In Unity, monitor selection is not directly exposed like GLFW.
            // Screen.fullScreenMode handles fullscreen.
            if (SelectedMonitor == 0) SelectedMonitor = 1; // Placeholder for primary monitor
            if (Fullscreen) Monitor = SelectedMonitor;
            else Monitor = 0;
        }

        public void LoadDefaults() // Renamed to PascalCase
        {
            // get the native resolution of the display
            // GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            // In Unity, use Screen.currentResolution
            if (RefreshRate == 0) RefreshRate = Screen.currentResolution.refreshRate;
            Fullscreen = Org.Schema.Schine.GraphicsEngine.Core.Settings.EngineSettings.G_FULLSCREEN.isOn();
            Debug.LogError("[CLIENT][GRAPHCIS] display received");
            int width = Screen.currentResolution.width;
            int height = Screen.currentResolution.height;
            _nativeRes = new Org.Schema.Schine.GraphicsEngine.Core.Settings.Resolution("native", width, height);
            Org.Schema.Schine.GraphicsEngine.Core.Settings.EngineSettings.G_RESOLUTION.addPossibilityObject(_nativeRes);
            Res = (Org.Schema.Schine.GraphicsEngine.Core.Settings.ResolutionInterface)Org.Schema.Schine.GraphicsEngine.Core.Settings.EngineSettings.G_RESOLUTION.getObject();
            if (Res == null)
            {
                //set the resolution to the native resoultion if not yet set
                Debug.LogError("[CLIENT][GRAPHCIS] RETRIEVING DEFAULT SCREEN DEVICE");
                Res = _nativeRes;
            }
            if (Org.Schema.Schine.GraphicsEngine.Core.Settings.EngineSettings.AUTOSET_RESOLUTION.isOn())
            {
                Org.Schema.Schine.GraphicsEngine.Core.Settings.EngineSettings.G_FULLSCREEN.setOn(true);
                Org.Schema.Schine.GraphicsEngine.Core.Settings.EngineSettings.G_RESOLUTION.setObject(_nativeRes);
                Org.Schema.Schine.GraphicsEngine.Core.Settings.EngineSettings.AUTOSET_RESOLUTION.setOn(false);
                try
                {
                    Org.Schema.Schine.GraphicsEngine.Core.Settings.EngineSettings.write();
                }
                catch (IOException e)
                {
                    Debug.LogException(e);
                }
                UpdateMonitor();
            }
        }
    }
}