using System;
using System.Collections.Generic;
using System.IO; // For File, Directory, Path, IOException
using UnityEngine; // For Texture2D, Debug, Application

// Placeholders for custom utility classes
namespace Org.Schema.Common.Util.Data
{
    public static class DataUtil
    {
        public static string dataPath = Application.dataPath + "/"; // Simplified
    }
}

namespace Org.Schema.Common.Util.Settings
{
    public interface SettingsXMLValue
    {
        string GetStringID();
    }
}

namespace Org.Schema.Schine.Resource
{
    public class FileExt : FileInfo
    {
        public FileExt(string path) : base(path) { }
    }
}

namespace Org.Schema.Schine.GraphicsEngine.Core
{
    public class TexturePack : Org.Schema.Common.Util.Settings.SettingsXMLValue
    {
        public static readonly string Path = DataUtil.dataPath + "textures" + System.IO.Path.DirectorySeparatorChar + "block" + System.IO.Path.DirectorySeparatorChar;

        public readonly string Name;
        public readonly int[] Resolutions;

        public TexturePack(string name, int[] resolutions)
        {
            this.Name = name;
            this.Resolutions = resolutions;
        }

        public static TexturePack[] CreateTexturePacks()
        {
            DirectoryInfo textureDir = new DirectoryInfo(Path);
            if (!textureDir.Exists)
            {
                Debug.LogError($"[TEXTURE-PACK] Error: Texture directory not found: {Path}");
                return new TexturePack[0];
            }

            List<TexturePack> texturePacksList = new List<TexturePack>();

            foreach (DirectoryInfo packDir in texturePackDirs)
            {
                if (packDir.Exists)
                {
                    List<int> res = new List<int>();

                    foreach (DirectoryInfo resolutionDir in packDir.GetDirectories())
                    {
                        try
                        {
                            int resolutionValue;
                            if (int.TryParse(resolutionDir.Name, out resolutionValue))
                            {
                                if (Check(resolutionDir))
                                {
                                    res.Add(resolutionValue);
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            Debug.LogException(e);
                            GLFrame.ProcessErrorDialogException(e, null); // Assuming GLFrame is converted
                        }
                    }

                    if (res.Count > 0)
                    {
                        texturePacksList.Add(new TexturePack(packDir.Name, res.ToArray()));
                    }
                }
                else
                {
                    Debug.LogWarning($"[TEXTURE-PACK] WARNING: ignoring {packDir.Name}: not a directory");
                }
            }

            return texturePacksList.ToArray();
        }

        public static Vector2 GetDimension(FileInfo resourceFile)
        {
            // In Unity, getting image dimensions usually involves loading the image.
            // This is a simplified implementation.
            try
            {
                byte[] fileData = File.ReadAllBytes(resourceFile.FullName);
                Texture2D tex = new Texture2D(2, 2); // Create a dummy texture
                if (tex.LoadImage(fileData)) // Load image data into the texture
                {
                    return new Vector2(tex.width, tex.height);
                }
            }
            catch (Exception e)
            {
                Debug.LogError($"[TEXTURE-PACK] Error getting dimension for {resourceFile.FullName}: {e.Message}");
            }
            return new Vector2(-1, -1);
        }

        private static bool Contains(FileInfo[] files, string[] allowedExtensions, string name)
        {
            foreach (FileInfo f in files)
            {
                foreach (string ext in allowedExtensions)
                {
                    string n = name + (ext.StartsWith(".") ? "" : ".") + ext;
                    if (string.Equals(n, f.Name, StringComparison.OrdinalIgnoreCase) ||
                        string.Equals(n + ".zip", f.Name, StringComparison.OrdinalIgnoreCase))
                    {
                        return true;
                    }
                }
            }
            return false;
        }

        private static bool Check(DirectoryInfo parent, string[] allowedExtensions, FileInfo[] files, params string[] containing)
        {
            bool c = true;
            // int res = parent.Name.Equals("256") ? 4096 : (parent.Name.Equals("128") ? 2048 : 1024); // Original logic
            // Vector2 d = new Vector2(res, res); // Original logic

            foreach (string requiredFile in containing)
            {
                bool contains = Contains(files, allowedExtensions, requiredFile);
                c = c && contains;

                if (!contains)
                {
                    // If its not the default one dont throw an exception
                    if (parent.Parent.Name.Equals("Default", StringComparison.OrdinalIgnoreCase))
                    {
                        throw new ResourceException($"The directory {parent.FullName} must contain {requiredFile} to be a valid Texture pack");
                    }
                    else
                    {
                        Debug.LogWarning($"[TEXTURE-PACK] WARNING: The directory {parent.FullName} must contain {requiredFile} to be a valid Texture pack");
                    }
                }
            }
            return c;
        }

        private static bool Check(DirectoryInfo f)
        {
            try
            {
                return Check(f, new string[] { "png", "tga" }, f.GetFiles(), "overlays", "t000", "t001", "t002", "t003");
            }
            catch (Exception e)
            {
                Debug.LogException(e);
                GLFrame.ProcessErrorDialogException(e, null); // Assuming GLFrame is converted
            }
            return false;
        }

        /// <summary>
        /// @return the name
        /// </summary>
        public string GetName()
        {
            return Name;
        }

        /// <summary>
        /// @return the resolutions
        /// </summary>
        public int[] GetResolutions()
        {
            return Resolutions;
        }

        public override string ToString()
        {
            return Name;
        }

        public string GetStringID()
        {
            return Name;
        }
    }
}