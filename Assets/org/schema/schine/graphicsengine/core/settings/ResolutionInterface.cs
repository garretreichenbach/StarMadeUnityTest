namespace Org.Schema.Schine.GraphicsEngine.Core.Settings
{
    public interface ResolutionInterface : Org.Schema.Common.Util.Settings.SettingsXMLValue
    {
        float GetAspect(); // Renamed to PascalCase

        string GetName(); // Renamed to PascalCase

        int GetWidth(); // Renamed to PascalCase

        int GetHeight(); // Renamed to PascalCase
    }
}