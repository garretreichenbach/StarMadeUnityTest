using System;
using System.IO; // For IOException

namespace Org.Schema.Schine.GraphicsEngine.Core
{
    public abstract class LoadingScreen
    {
        public static string ServerMessage = ""; // Renamed to PascalCase
        public abstract void DrawLoadingScreen(); // Renamed to PascalCase
        public abstract void LoadInitialResources(); // Renamed to PascalCase, removed throws IOException, ResourceException
        public abstract void Update(Timer timer); // Renamed to PascalCase
        public abstract void HandleException(Exception e); // Renamed to PascalCase
    }
}