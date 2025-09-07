using System; // For IEquatable

namespace Org.Schema.Schine.GraphicsEngine.Core.Settings
{
    // Assuming ResolutionInterface and SettingsXMLValue are defined elsewhere or will be.
    // For now, I'll include a minimal definition for ResolutionInterface.
    // SettingsXMLValue is already defined in EngineSettings.cs.

    public interface ResolutionInterface
    {
        int getWidth();
        int getHeight();
    }

    public class Resolution : ResolutionInterface, Org.Schema.Common.Util.Settings.SettingsXMLValue, IEquatable<Resolution>
    {
        public readonly int Width; // Renamed to PascalCase
        public readonly int Height; // Renamed to PascalCase
        private readonly string _name; // Renamed to PascalCase
        private readonly float _aspect; // Renamed to PascalCase

        public Resolution(string name, int width, int height)
        {
            this.Width = width;
            this.Height = height;
            this._name = name;
            this._aspect = (float)width / (float)height;
        }

        /// <summary>
        /// @return the aspect
        /// </summary>
        public float GetAspect() // Renamed to PascalCase
        {
            return _aspect;
        }

        /// <summary>
        /// @return the name
        /// </summary>
        public string GetName() // Renamed to PascalCase
        {
            return $"{Width}x{Height}";
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public override string ToString()
        {
            return $"{Width} x {Height}";
        }

        public override int GetHashCode()
        {
            unchecked
            {
                int hash = 17;
                hash = hash * 23 + Height.GetHashCode();
                hash = hash * 23 + Width.GetHashCode();
                return hash;
            }
        }

        public override bool Equals(object obj)
        {
            return Equals(obj as Resolution);
        }

        public bool Equals(Resolution other)
        {
            if (ReferenceEquals(other, null)) return false;
            if (ReferenceEquals(this, other)) return true;
            return Width == other.Width && Height == other.Height;
        }

        public int getWidth()
        {
            return Width;
        }

        public int getHeight()
        {
            return Height;
        }

        public string GetStringID() // From SettingsXMLValue
        {
            return GetName();
        }
    }
}