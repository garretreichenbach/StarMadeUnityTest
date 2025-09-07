namespace Org.Schema.Schine.GraphicsEngine.Core
{
    public enum ShadowQuality
    {
        Off,
        Barebone,
        Simple,
        Best,
        Ultra
    }

    // If the sName field is needed, an extension method can be used:
    public static class ShadowQualityExtensions
    {
        public static string GetName(this ShadowQuality quality)
        {
            switch (quality)
            {
                case ShadowQuality.Off: return "OFF";
                case ShadowQuality.Barebone: return "BAREBONE";
                case ShadowQuality.Simple: return "SIMPLE";
                case ShadowQuality.Best: return "BEST";
                case ShadowQuality.Ultra: return "ULTRA";
                default: return quality.ToString().ToUpper(); // Fallback
            }
        }
    }
}