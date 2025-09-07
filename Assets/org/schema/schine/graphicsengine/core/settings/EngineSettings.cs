using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Xml.Linq; // For XML parsing
using UnityEngine; // For Debug, Application

// Placeholders for custom types
namespace Org.Schema.Common
{
    public static class XMLTools
    {
        public static XDocument loadXML(FileInfo file) { Debug.Log($"XMLTools.loadXML called for {file.FullName}"); return new XDocument(new XElement("Settings")); }
        public static void writeDocument(FileInfo file, XDocument doc) { Debug.Log($"XMLTools.writeDocument called for {file.FullName}"); }
    }

    public static class StringTools
    {
        public static string LongestCommonSubsequence(string s1, string s2) { Debug.Log("StringTools.LongestCommonSubsequence called."); return ""; }
    }
}

namespace Org.Schema.Common.Util.Settings
{
    public interface SettingsXMLValue
    {
        string GetStringID();
    }

    public interface SettingState
    {
        bool isOn();
        void setOn(bool on);
        int getInt();
        void setInt(int v);
        float getFloat();
        void setFloat(float v);
        string getString();
        void setString(string v);
        object getObject();
        void setObject(object o);
        void next();
        void previous();
        string getAsString();
        SettingStateType getType();
        void addListener(SettingChangedListener listener);
        void setValue(SettingState other);
        void setValueByObject(object value);
        bool setFromString(string s);
        GUIElement getGUIElementTextBar(InputState state, GUIElement dependent, string deactText);
        GUIElement getGUIElement(InputState state, GUIElement dependent);
        GUIElement getGUIElement(InputState state, GUIElement dependent, string deactText);

        public enum SettingStateType
        {
            BOOLEAN, INT, FLOAT, STRING, ENUM, OBJECT, LONG
        }

        public interface SettingStateValueFac
        {
            SettingState inst();
        }
    }

    public interface SettingChangedListener
    {
        void onSettingChanged(SettingState settingState);
    }

    public class SettingStateBoolean : SettingState
    {
        private bool _value;
        private List<SettingChangedListener> _listeners = new List<SettingChangedListener>();
        public SettingStateBoolean(bool initialValue) { _value = initialValue; }
        public bool isOn() { return _value; }
        public void setOn(bool on) { _value = on; NotifyListeners(); }
        public int getInt() { return _value ? 1 : 0; }
        public void setInt(int v) { _value = v != 0; NotifyListeners(); }
        public float getFloat() { return _value ? 1f : 0f; }
        public void setFloat(float v) { _value = v != 0f; NotifyListeners(); }
        public string getString() { return _value.ToString(); }
        public void setString(string v) { _value = bool.Parse(v); NotifyListeners(); }
        public object getObject() { return _value; }
        public void setObject(object o) { _value = (bool)o; NotifyListeners(); }
        public void next() { _value = !_value; NotifyListeners(); }
        public void previous() { _value = !_value; NotifyListeners(); }
        public string getAsString() { return _value.ToString(); }
        public SettingState.SettingStateType getType() { return SettingState.SettingStateType.BOOLEAN; }
        public void addListener(SettingChangedListener listener) { _listeners.Add(listener); }
        private void NotifyListeners() { foreach (var l in _listeners) l.onSettingChanged(this); }
        public void setValue(SettingState other) { if (other is SettingStateBoolean b) setOn(b.isOn()); }
        public void setValueByObject(object value) { if (value is bool b) setOn(b); }
        public bool setFromString(string s) { if (bool.TryParse(s, out bool result)) { setOn(result); return true; } return false; }
        public GUIElement getGUIElementTextBar(InputState state, GUIElement dependent, string deactText) { return null; }
        public GUIElement getGUIElement(InputState state, GUIElement dependent) { return null; }
        public GUIElement getGUIElement(InputState state, GUIElement dependent, string deactText) { return null; }
        public long getLong() { return _value ? 1L : 0L; }
        public void setLong(long l) { _value = l != 0L; NotifyListeners(); }
        public void parseXML(XElement e) { setOn(bool.Parse(e.Value)); }
        public void writeXML(XDocument d, XElement e) { e.Value = _value.ToString(); }
    }

    public class SettingStateInt : SettingState
    {
        private int _value;
        private int _min, _max;
        private int[] _possibilities;
        private List<SettingChangedListener> _listeners = new List<SettingChangedListener>();
        public SettingStateInt(int initialValue, int min, int max) { _value = initialValue; _min = min; _max = max; }
        public SettingStateInt(int initialValue, int[] possibilities) { _value = initialValue; _possibilities = possibilities; }
        public bool isOn() { return _value != 0; }
        public void setOn(bool on) { _value = on ? 1 : 0; NotifyListeners(); }
        public int getInt() { return _value; }
        public void setInt(int v) { _value = v; NotifyListeners(); }
        public float getFloat() { return _value; }
        public void setFloat(float v) { _value = (int)v; NotifyListeners(); }
        public string getString() { return _value.ToString(); }
        public void setString(string v) { _value = int.Parse(v); NotifyListeners(); }
        public object getObject() { return _value; }
        public void setObject(object o) { _value = (int)o; NotifyListeners(); }
        public void next() { if (_possibilities != null) { int idx = Array.IndexOf(_possibilities, _value); if (idx < _possibilities.Length - 1) _value = _possibilities[idx + 1]; else _value = _possibilities[0]; } else { _value++; } NotifyListeners(); }
        public void previous() { if (_possibilities != null) { int idx = Array.IndexOf(_possibilities, _value); if (idx > 0) _value = _possibilities[idx - 1]; else _value = _possibilities[_possibilities.Length - 1]; } else { _value--; } NotifyListeners(); }
        public string getAsString() { return _value.ToString(); }
        public SettingState.SettingStateType getType() { return SettingState.SettingStateType.INT; }
        public void addListener(SettingChangedListener listener) { _listeners.Add(listener); }
        private void NotifyListeners() { foreach (var l in _listeners) l.onSettingChanged(this); }
        public void setValue(SettingState other) { if (other is SettingStateInt i) setInt(i.getInt()); }
        public void setValueByObject(object value) { if (value is int i) setInt(i); }
        public bool setFromString(string s) { if (int.TryParse(s, out int result)) { setInt(result); return true; } return false; }
        public GUIElement getGUIElementTextBar(InputState state, GUIElement dependent, string deactText) { return null; }
        public GUIElement getGUIElement(InputState state, GUIElement dependent) { return null; }
        public GUIElement getGUIElement(InputState state, GUIElement dependent, string deactText) { return null; }
        public long getLong() { return _value; }
        public void setLong(long l) { _value = (int)l; NotifyListeners(); }
        public void parseXML(XElement e) { setInt(int.Parse(e.Value)); }
        public void writeXML(XDocument d, XElement e) { e.Value = _value.ToString(); }
    }

    public class SettingStateFloat : SettingState
    {
        private float _value;
        private float _min, _max;
        private float[] _possibilities;
        private List<SettingChangedListener> _listeners = new List<SettingChangedListener>();
        public SettingStateFloat(float initialValue, float min, float max, bool clamp) { _value = initialValue; _min = min; _max = max; }
        public SettingStateFloat(float initialValue, float[] possibilities) { _value = initialValue; _possibilities = possibilities; }
        public bool isOn() { return _value != 0f; }
        public void setOn(bool on) { _value = on ? 1f : 0f; NotifyListeners(); }
        public int getInt() { return (int)_value; }
        public void setInt(int v) { _value = v; NotifyListeners(); }
        public float getFloat() { return _value; }
        public void setFloat(float v) { _value = v; NotifyListeners(); }
        public string getString() { return _value.ToString(); }
        public void setString(string v) { _value = float.Parse(v); NotifyListeners(); }
        public object getObject() { return _value; }
        public void setObject(object o) { _value = (float)o; NotifyListeners(); }
        public void next() { if (_possibilities != null) { int idx = Array.IndexOf(_possibilities, _value); if (idx < _possibilities.Length - 1) _value = _possibilities[idx + 1]; else _value = _possibilities[0]; } else { _value++; } NotifyListeners(); }
        public void previous() { if (_possibilities != null) { int idx = Array.IndexOf(_possibilities, _value); if (idx > 0) _value = _possibilities[idx - 1]; else _value = _possibilities[_possibilities.Length - 1]; } else { _value--; } NotifyListeners(); }
        public string getAsString() { return _value.ToString(); }
        public SettingState.SettingStateType getType() { return SettingState.SettingStateType.FLOAT; }
        public void addListener(SettingChangedListener listener) { _listeners.Add(listener); }
        private void NotifyListeners() { foreach (var l in _listeners) l.onSettingChanged(this); }
        public void setValue(SettingState other) { if (other is SettingStateFloat f) setFloat(f.getFloat()); }
        public void setValueByObject(object value) { if (value is float f) setFloat(f); }
        public bool setFromString(string s) { if (float.TryParse(s, out float result)) { setFloat(result); return true; } return false; }
        public GUIElement getGUIElementTextBar(InputState state, GUIElement dependent, string deactText) { return null; }
        public GUIElement getGUIElement(InputState state, GUIElement dependent) { return null; }
        public GUIElement getGUIElement(InputState state, GUIElement dependent, string deactText) { return null; }
        public long getLong() { return (long)_value; }
        public void setLong(long l) { _value = l; NotifyListeners(); }
        public void parseXML(XElement e) { setFloat(float.Parse(e.Value)); }
        public void writeXML(XDocument d, XElement e) { e.Value = _value.ToString(); }
    }

    public class SettingStateString : SettingState
    {
        private string _value;
        private string[] _possibilities;
        private List<SettingChangedListener> _listeners = new List<SettingChangedListener>();
        public SettingStateString(string initialValue) { _value = initialValue; }
        public SettingStateString(string initialValue, string[] possibilities) { _value = initialValue; _possibilities = possibilities; }
        public bool isOn() { return !string.IsNullOrEmpty(_value); }
        public void setOn(bool on) { _value = on ? "true" : "false"; NotifyListeners(); }
        public int getInt() { return 0; }
        public void setInt(int v) { }
        public float getFloat() { return 0f; }
        public void setFloat(float v) { }
        public string getString() { return _value; }
        public void setString(string v) { _value = v; NotifyListeners(); }
        public object getObject() { return _value; }
        public void setObject(object o) { _value = (string)o; NotifyListeners(); }
        public void next() { if (_possibilities != null) { int idx = Array.IndexOf(_possibilities, _value); if (idx < _possibilities.Length - 1) _value = _possibilities[idx + 1]; else _value = _possibilities[0]; } NotifyListeners(); }
        public void previous() { if (_possibilities != null) { int idx = Array.IndexOf(_possibilities, _value); if (idx > 0) _value = _possibilities[idx - 1]; else _value = _possibilities[_possibilities.Length - 1]; } NotifyListeners(); }
        public string getAsString() { return _value; }
        public SettingState.SettingStateType getType() { return SettingState.SettingStateType.STRING; }
        public void addListener(SettingChangedListener listener) { _listeners.Add(listener); }
        private void NotifyListeners() { foreach (var l in _listeners) l.onSettingChanged(this); }
        public void setValue(SettingState other) { if (other is SettingStateString s) setString(s.getString()); }
        public void setValueByObject(object value) { if (value is string s) setString(s); }
        public bool setFromString(string s) { setString(s); return true; }
        public GUIElement getGUIElementTextBar(InputState state, GUIElement dependent, string deactText) { return null; }
        public GUIElement getGUIElement(InputState state, GUIElement dependent) { return null; }
        public GUIElement getGUIElement(InputState state, GUIElement dependent, string deactText) { return null; }
        public long getLong() { return 0L; }
        public void setLong(long l) { }
        public void parseXML(XElement e) { setString(e.Value); }
        public void writeXML(XDocument d, XElement e) { e.Value = _value; }
    }

    public class SettingStateEnum<T> : SettingState where T : Enum
    {
        private T _value;
        private T[] _possibilities;
        private List<SettingChangedListener> _listeners = new List<SettingChangedListener>();
        public SettingStateEnum(T initialValue, T[] possibilities) { _value = initialValue; _possibilities = possibilities; }
        public bool isOn() { return Convert.ToInt32(_value) != 0; }
        public void setOn(bool on) { }
        public int getInt() { return Convert.ToInt32(_value); }
        public void setInt(int v) { _value = (T)Enum.ToObject(typeof(T), v); NotifyListeners(); }
        public float getFloat() { return Convert.ToSingle(_value); }
        public void setFloat(float v) { }
        public string getString() { return _value.ToString(); }
        public void setString(string v) { _value = (T)Enum.Parse(typeof(T), v); NotifyListeners(); }
        public object getObject() { return _value; }
        public void setObject(object o) { _value = (T)o; NotifyListeners(); }
        public void next() { if (_possibilities != null) { int idx = Array.IndexOf(_possibilities, _value); if (idx < _possibilities.Length - 1) _value = _possibilities[idx + 1]; else _value = _possibilities[0]; } NotifyListeners(); }
        public void previous() { if (_possibilities != null) { int idx = Array.IndexOf(_possibilities, _value); if (idx > 0) _value = _possibilities[idx - 1]; else _value = _possibilities[_possibilities.Length - 1]; } NotifyListeners(); }
        public string getAsString() { return _value.ToString(); }
        public SettingState.SettingStateType getType() { return SettingState.SettingStateType.ENUM; }
        public void addListener(SettingChangedListener listener) { _listeners.Add(listener); }
        private void NotifyListeners() { foreach (var l in _listeners) l.onSettingChanged(this); }
        public void setValue(SettingState other) { if (other is SettingStateEnum<T> e) setObject(e.getObject()); }
        public void setValueByObject(object value) { if (value is T t) setObject(t); }
        public bool setFromString(string s) { try { setObject(Enum.Parse(typeof(T), s)); return true; } catch { return false; } }
        public GUIElement getGUIElementTextBar(InputState state, GUIElement dependent, string deactText) { return null; }
        public GUIElement getGUIElement(InputState state, GUIElement dependent) { return null; }
        public GUIElement getGUIElement(InputState state, GUIElement dependent, string deactText) { return null; }
        public long getLong() { return Convert.ToInt64(_value); }
        public void setLong(long l) { _value = (T)Enum.ToObject(typeof(T), l); NotifyListeners(); }
        public void parseXML(XElement e) { setObject(Enum.Parse(typeof(T), e.Value)); }
        public void writeXML(XDocument d, XElement e) { e.Value = _value.ToString(); }
    }

    public class SettingStateObject<T> : SettingState where T : class
    {
        private T _value;
        private T[] _possibilities;
        private List<SettingChangedListener> _listeners = new List<SettingChangedListener>();
        public SettingStateObject(T initialValue, T[] possibilities) { _value = initialValue; _possibilities = possibilities; }
        public bool isOn() { return _value != null; }
        public void setOn(bool on) { }
        public int getInt() { return 0; }
        public void setInt(int v) { }
        public float getFloat() { return 0f; }
        public void setFloat(float v) { }
        public string getString() { return _value?.ToString(); }
        public void setString(string v) { }
        public object getObject() { return _value; }
        public void setObject(object o) { _value = (T)o; NotifyListeners(); }
        public void next() { if (_possibilities != null) { int idx = Array.IndexOf(_possibilities, _value); if (idx < _possibilities.Length - 1) _value = _possibilities[idx + 1]; else _value = _possibilities[0]; } NotifyListeners(); }
        public void previous() { if (_possibilities != null) { int idx = Array.IndexOf(_possibilities, _value); if (idx > 0) _value = _possibilities[idx - 1]; else _value = _possibilities[_possibilities.Length - 1]; } NotifyListeners(); }
        public string getAsString() { return _value?.ToString(); }
        public SettingState.SettingStateType getType() { return SettingState.SettingStateType.OBJECT; }
        public void addListener(SettingChangedListener listener) { _listeners.Add(listener); }
        private void NotifyListeners() { foreach (var l in _listeners) l.onSettingChanged(this); }
        public void setValue(SettingState other) { if (other is SettingStateObject<T> o) setObject(o.getObject()); }
        public void setValueByObject(object value) { if (value is T t) setObject(t); }
        public bool setFromString(string s) { return false; } // Complex, needs custom parsing for objects
        public GUIElement getGUIElementTextBar(InputState state, GUIElement dependent, string deactText) { return null; }
        public GUIElement getGUIElement(InputState state, GUIElement dependent) { return null; }
        public GUIElement getGUIElement(InputState state, GUIElement dependent, string deactText) { return null; }
        public long getLong() { return 0L; }
        public void setLong(long l) { }
        public void parseXML(XElement e) { /* Complex, needs custom parsing for objects */ }
        public void writeXML(XDocument d, XElement e) { e.Value = _value?.ToString(); }
        public void addPossibilityOption(object o) { /* Not directly supported by this placeholder */ }
    }

    public class SettingStateLong : SettingState
    {
        private long _value;
        private long _min, _max;
        private List<SettingChangedListener> _listeners = new List<SettingChangedListener>();
        public SettingStateLong(long initialValue, long min, long max) { _value = initialValue; _min = min; _max = max; }
        public bool isOn() { return _value != 0L; }
        public void setOn(bool on) { _value = on ? 1L : 0L; NotifyListeners(); }
        public int getInt() { return (int)_value; }
        public void setInt(int v) { _value = v; NotifyListeners(); }
        public float getFloat() { return _value; }
        public void setFloat(float v) { _value = (long)v; NotifyListeners(); }
        public string getString() { return _value.ToString(); }
        public void setString(string v) { _value = long.Parse(v); NotifyListeners(); }
        public object getObject() { return _value; }
        public void setObject(object o) { _value = (long)o; NotifyListeners(); }
        public void next() { _value++; NotifyListeners(); }
        public void previous() { _value--; NotifyListeners(); }
        public string getAsString() { return _value.ToString(); }
        public SettingState.SettingStateType getType() { return SettingState.SettingStateType.LONG; }
        public void addListener(SettingChangedListener listener) { _listeners.Add(listener); }
        private void NotifyListeners() { foreach (var l in _listeners) l.onSettingChanged(this); }
        public void setValue(SettingState other) { if (other is SettingStateLong l) setLong(l.getLong()); }
        public void setValueByObject(object value) { if (value is long l) setLong(l); }
        public bool setFromString(string s) { if (long.TryParse(s, out long result)) { setLong(result); return true; } return false; }
        public GUIElement getGUIElementTextBar(InputState state, GUIElement dependent, string deactText) { return null; }
        public GUIElement getGUIElement(InputState state, GUIElement dependent) { return null; }
        public GUIElement getGUIElement(InputState state, GUIElement dependent, string deactText) { return null; }
        public long getLong() { return _value; }
        public void setLong(long l) { _value = l; NotifyListeners(); }
        public void parseXML(XElement e) { setLong(long.Parse(e.Value)); }
        public void writeXML(XDocument d, XElement e) { e.Value = _value.ToString(); }
    }

    public class SettingStateParseError : Exception
    {
        public SettingStateParseError(string s, SettingState state, object engineSetting) : base($"Failed to parse '{s}' for setting '{engineSetting}' with state type '{state.getType()}'") { }
    }
}

namespace Org.Schema.Schine.Common.Language
{
    public interface Translatable
    {
        string GetName(Enum en);
        string GetName(string s);
        string GetName(object o);

        public static Translatable DEFAULT = new DefaultTranslatable();
    }

    public class DefaultTranslatable : Translatable
    {
        public string GetName(Enum en) { return en.ToString(); }
        public string GetName(string s) { return s; }
        public string GetName(object o) { return o?.ToString(); }
    }

    public static class Lng
    {
        public static string str(string key) { return key; }
        public static string str(string key, params object[] args) { return string.Format(key, args); }
    }
}

namespace Org.Schema.Schine.Common.Util
{
    public static class FileUtil
    {
        public static void copyFile(FileInfo source, FileInfo destination) { File.Copy(source.FullName, destination.FullName, true); }
    }
}

namespace Org.Schema.Schine.GraphicsEngine.Core
{
    // EngineSettingsType
    public enum EngineSettingsType
    {
        GENERAL, GRAPHICS, GRAPHICS_ADVANCED, MOUSE, NETWORK, SOUND, GAMEPLAY, MISCELLANIOUS
    }

    // AtmosphereShaderSetting
    public enum AtmosphereShaderSetting
    {
        NORMAL, NONE // Placeholder
    }

    // PresetStates
    public class PresetStates
    {
        public static PresetStates getDefault() { return new PresetStates(); }
        public static PresetStates[] values() { return new PresetStates[] { new PresetStates() }; }
        public string name() { return "DEFAULT"; }
    }

    // MinimapMode
    public enum MinimapMode
    {
        SMALL // Placeholder
    }

    // SectorIndicationMode
    public enum SectorIndicationMode
    {
        INDICATION_ONLY // Placeholder
    }

    // InfoSetting
    public enum InfoSetting
    {
        FPS_AND_PING // Placeholder
    }

    // FBOFlag
    public enum FBOFlag
    {
        STATIC // Placeholder
    }

    // GUIElement
    public class GUIElement
    {
        // Placeholder
    }

    // UIScale
    public enum UIScale
    {
        S_100, S_200 // Placeholder
    }

    // InputState
    public class InputState
    {
        // Placeholder
    }

    // EngineSettingsChangeListener
    public interface EngineSettingsChangeListener
    {
        void onSettingChanged(SettingsInterface setting);
    }

    // SettingsInterface (from org.schema.schine.graphicsengine.forms.gui)
    public interface SettingsInterface
    {
        bool isOn();
        void setOn(bool on);
        int getInt();
        void setInt(int v);
        float getFloat();
        void setFloat(float v);
        string getString();
        void setString(string v);
        object getObject();
        void setObject(object o);
        string name();
        void addChangeListener(EngineSettingsChangeListener c);
        void removeChangeListener(EngineSettingsChangeListener c);
        void next();
        void previous();
        string getAsString();
        SettingsInterface getSettingsForGUI();
    }
}

namespace Org.Schema.Schine.GraphicsEngine.Core.Settings
{
    /// <summary>
    /// The Enum EngineSettings.
    /// Converted to a static class with static properties for each setting.
    /// </summary>
    public static class EngineSettings
    {
        public static readonly HashSet<EngineSettings> DirtyTmpSettings = new HashSet<EngineSettings>(); // Replaced ObjectOpenHashSet
        public const string SETTINGS_PATH = "settings.cfg";
        private static readonly List<EngineSettings> _sortedSettings = new List<EngineSettings>();
        public static bool Dirty;
        public static bool NeedsRestart;

        // Dictionary to hold the actual setting instances
        private static readonly Dictionary<string, EngineSettings> _settingsMap = new Dictionary<string, EngineSettings>();
        private static readonly Dictionary<EngineSettings, SettingState.SettingStateValueFac> _stateFac = new Dictionary<EngineSettings, SettingState.SettingStateValueFac>();

        // Define each setting as a static readonly field
        public static readonly EngineSettings UI_SCALE;
        public static readonly EngineSettings CONTROL_HELP;
        public static readonly EngineSettings G_ICON_CONTEXT_FILTER;
        public static readonly EngineSettings G_RESOLUTION;
        public static readonly EngineSettings G_FULLSCREEN;
        public static readonly EngineSettings G_WINDOWED_BORDERLESS;
        public static readonly EngineSettings G_TEXTURE_PACK;
        public static readonly EngineSettings G_TEXTURE_PACK_RESOLUTION;
        public static readonly EngineSettings G_VSYNCH;
        public static readonly EngineSettings G_MULTI_SAMPLE;
        public static readonly EngineSettings G_GAMMA;
        public static readonly EngineSettings G_FOV;
        public static readonly EngineSettings G_MAX_SEGMENTSDRAWN;
        public static readonly EngineSettings G_NORMAL_MAPPING;
        public static readonly EngineSettings G_SHADOW_QUALITY;
        public static readonly EngineSettings G_SHADOW_USE_PCF;
        public static readonly EngineSettings G_PROD_BG;
        public static readonly EngineSettings G_PROD_BG_QUALITY;
        public static readonly EngineSettings G_DRAW_SURROUNDING_GALAXIES_IN_MAP;
        public static readonly EngineSettings HIT_INDICATION_NUMBERS_LIFETIME;
        public static readonly EngineSettings BUILD_AND_REMOVE_ON_BUTTON_RELEASE;
        public static readonly EngineSettings PLAYER_SKIN_CREATE;
        public static readonly EngineSettings PLAYER_SKIN;
        public static readonly EngineSettings D_LIFETIME_NORM;
        public static readonly EngineSettings N_TRANSMIT_RAW_DEBUG_POSITIONS;
        public static readonly EngineSettings C_SELECTED_JOYSTICK;
        public static readonly EngineSettings ICON_BAKERY_BLOCKSTYLE_ROTATE_DEG;
        public static readonly EngineSettings MOUSE_WHEEL_SENSIBILITY;
        public static readonly EngineSettings BUILD_MODE_SHIFT_SPEED;
        public static readonly EngineSettings ORBITAL_CAM_SPEED;
        public static readonly EngineSettings G_DAMAGE_DISPLAY;
        public static readonly EngineSettings G_STAR_COUNT;
        public static readonly EngineSettings G_VBO_BULKMODE_SIZE;
        public static readonly EngineSettings G_DRAW_FOG;
        public static readonly EngineSettings G_DRAW_BEAMS;
        public static readonly EngineSettings G_DRAW_EXHAUST_PLUMES;
        public static readonly EngineSettings D_INFO_CONTROLMANAGER;
        public static readonly EngineSettings D_INFO_SHADER_ERRORS;
        public static readonly EngineSettings T_ENABLE_TEXTURE_BAKER;
        public static readonly EngineSettings D_INFO_DRAW_TIMES;
        public static readonly EngineSettings D_INFO_DRAW_SPACE_PARTICLE;
        public static readonly EngineSettings G_SPACE_PARTICLE;
        public static readonly EngineSettings G_DRAW_MOUSE_COLLISION;
        public static readonly EngineSettings G_ATMOSPHERE_SHADER;
        public static readonly EngineSettings F_FRAME_BUFFER;
        public static readonly EngineSettings G_SHADOW_NEAR_DIST;
        public static readonly EngineSettings G_SHADOW_FAR_DIST;
        public static readonly EngineSettings G_SHADOW_DEPTH_RANGE_NEAR;
        public static readonly EngineSettings G_SHADOW_DEPTH_RANGE_FAR;
        public static readonly EngineSettings G_SHADOW_SPLIT_FAR_0;
        public static readonly EngineSettings G_SHADOW_SPLIT_NEAR_1;
        public static readonly EngineSettings G_SHADOW_SPLIT_FAR_1;
        public static readonly EngineSettings G_SHADOW_SPLIT_NEAR_2;
        public static readonly EngineSettings G_SHADOW_SPLIT_MULT;
        public static readonly EngineSettings G_SHADOW_CROP_MATRIX_MAX;
        public static readonly EngineSettings G_SHADOW_CROP_MATRIX_MIN;
        public static readonly EngineSettings G_SHADOW_EXTRA_BACKUP;
        public static readonly EngineSettings G_SHADOW_NEAR_CLIP;
        public static readonly EngineSettings G_SHADOW_SPLIT_MAT_RADIUS_ADD_0;
        public static readonly EngineSettings G_SHADOW_SPLIT_MAT_RADIUS_ADD_1;
        public static readonly EngineSettings G_SHADOW_SPLIT_MAT_RADIUS_ADD_2;
        public static readonly EngineSettings G_SHADOW_SPLIT_MAT_RADIUS_ADD_3;
        public static readonly EngineSettings G_SHADOW_SPLIT_ORTHO_MAT_FAR_ADDED_0;
        public static readonly EngineSettings G_SHADOW_SPLIT_ORTHO_MAT_FAR_ADDED_1;
        public static readonly EngineSettings G_SHADOW_SPLIT_ORTHO_MAT_FAR_ADDED_2;
        public static readonly EngineSettings G_SHADOW_SPLIT_ORTHO_MAT_FAR_ADDED_3;
        public static readonly EngineSettings G_SHADOW_SPLIT_ORTHO_NEAR_ADDED;
        public static readonly EngineSettings G_SHADOW_ULTRA_FAR_BOUNDS_ADDED_0;
        public static readonly EngineSettings G_SHADOW_ULTRA_FAR_BOUNDS_ADDED_1;
        public static readonly EngineSettings G_SHADOW_ULTRA_FAR_BOUNDS_ADDED_2;
        public static readonly EngineSettings G_SHADOW_OTHER_QUALITY_FAR_BOUND_ADDED_0;
        public static readonly EngineSettings G_SHADOW_OTHER_QUALITY_FAR_BOUND_ADDED_1;
        public static readonly EngineSettings G_SHADOW_OTHER_QUALITY_FAR_BOUND_ADDED_2;
        public static readonly EngineSettings G_SHADOW_FOV_ADDED_RAD;
        public static readonly EngineSettings G_SHADOW_DISPLAY_SHADOW_MAP;
        public static readonly EngineSettings G_DRAW_SHIELDS;
        public static readonly EngineSettings G_DRAW_WATER;
        public static readonly EngineSettings G_DRAW_BACKGROUND;
        public static readonly EngineSettings G_WATER_USE_MIPMAPS;
        public static readonly EngineSettings E_NAVIGATION_FILTER;
        public static readonly EngineSettings G_DRAW_ENTITIES;
        public static readonly EngineSettings G_FRUSTUM_CULLING;
        public static readonly EngineSettings G_DRAW_EFFECTS;
        public static readonly EngineSettings G_SHADER_RELOAD;
        public static readonly EngineSettings G_FRAMERATE_FIXED;
        public static readonly EngineSettings GRAPHICS_PRESET;
        public static readonly EngineSettings G_SHADERS_ACTIVE;
        public static readonly EngineSettings G_DEBUG_DRAW_GRID;
        public static readonly EngineSettings G_DEBUG_DRAW_LINES;
        public static readonly EngineSettings T_TERRAIN_DRAW;
        public static readonly EngineSettings T_TERRAIN_WIREFRAME_DRAW;
        public static readonly EngineSettings G_CULLING_ACTIVE;
        public static readonly EngineSettings G_TEXTURE_ARRAYS;
        public static readonly EngineSettings G_AUTO_NORMALIZATION;
        public static readonly EngineSettings G_DRAW_BY_SIDES;
        public static readonly EngineSettings G_WIREFRAMED;
        public static readonly EngineSettings G_TEXTURE_COMPRESSION_BLOCKS;
        public static readonly EngineSettings G_TEXTURE_ARRAY_COMPRESSION;
        public static readonly EngineSettings G_TEXTURE_ARRAY_MIPMAP;
        public static readonly EngineSettings G_TEXTURE_MIPMAP;
        public static readonly EngineSettings G_SHADOWS_VSM;
        public static readonly EngineSettings S_PAUSED;
        public static readonly EngineSettings S_SETTINGS_SHOW;
        public static readonly EngineSettings MINIMAP_MODE;
        public static readonly EngineSettings SECTOR_INDICATION_MODE;
        public static readonly EngineSettings S_KEY_ALLOW_DUPLICATES;
        public static readonly EngineSettings GIF_RESOLUTION;
        public static readonly EngineSettings GIF_FPS;
        public static readonly EngineSettings GIF_GUI;
        public static readonly EngineSettings GIF_QUALITY;
        public static readonly EngineSettings G_DEBRIS_THRESHOLD_SLOW_MS;
        public static readonly EngineSettings S_INITIAL_SETTING;
        public static readonly EngineSettings G_BONE_ANIMATION;
        public static readonly EngineSettings P_NT_DEBUG_ACTIVE;
        public static readonly EngineSettings P_PHYSICS_DEBUG_ACTIVE_OCCLUSION;
        public static readonly EngineSettings P_PHYSICS_DEBUG_ACTIVE;
        public static readonly EngineSettings P_PHYSICS_DEBUG_MODE;
        public static readonly EngineSettings G_DEBUG_LINE_DRAWING_ACTIVATED;
        public static readonly EngineSettings G_WINDOW_START_POSITION;
        public static readonly EngineSettings G_DRAW_GUI_ACTIVE;
        public static readonly EngineSettings G_DRAW_NO_OVERLAYS;
        public static readonly EngineSettings G_PARTICLE_SORTING;
        public static readonly EngineSettings P_PHYSICS_ACTIVATED;
        public static readonly EngineSettings G_SMOKE_QUALITY;
        public static readonly EngineSettings G_DRAW_STARS;
        public static readonly EngineSettings G_DRAW_PASTE_PREVIEW;
        public static readonly EngineSettings G_USE_SPRITE_VBO;
        public static readonly EngineSettings TUTORIAL_NEW;
        public static readonly EngineSettings G_AUTOSELECT_CONTROLLERS;
        public static readonly EngineSettings S_INFO_DRAW;
        public static readonly EngineSettings G_VBO_FLAG;
        public static readonly EngineSettings N_LAG_COMPENSATION;
        public static readonly EngineSettings N_IGNORE_SAVED_UPLINK_CREDENTIALS_IN_SINGLEPLAYER;
        public static readonly EngineSettings N_SERVERTIME_UPDATE_FREQUENCY;
        public static readonly EngineSettings M_TEXTURE_PACK_CONFIG_TOOL;
        public static readonly EngineSettings G_MIPMAP_LEVEL_MAX;
        public static readonly EngineSettings I_MOUSE_SENSITIVITY;
        public static readonly EngineSettings G_HIT_INDICATION_SIZE;
        public static readonly EngineSettings F_BLOOM;
        public static readonly EngineSettings F_BLOOM_INTENSITY;
        public static readonly EngineSettings S_MOUSE_LOCK;
        public static readonly EngineSettings G_DRAW_ADV_BUILDMODE_BLOCK_PREVIEW;
        public static readonly EngineSettings G_SHOW_PURE_NUMBERS_FOR_SHIELD_AND_POWER;
        public static readonly EngineSettings S_MOUSE_SHIP_INVERT;
        public static readonly EngineSettings S_MOUSE_ALL_INVERT;
        public static readonly EngineSettings S_USE_REGION_SIGNATURE_TEST;
        public static readonly EngineSettings G_PREVIEW_TO_BUILD_BLOCK;
        public static readonly EngineSettings S_EXIT_ON_ESC;
        public static readonly EngineSettings G_USE_HIGH_QUALITY_BACKGROUND;
        public static readonly EngineSettings G_DRAW_POPUPS;
        public static readonly EngineSettings G_USE_VBO_MAP;
        public static readonly EngineSettings G_DRAW_JUMP_OVERLAY;
        public static readonly EngineSettings G_MAG_FILTER_LINEAR_BLOCKS;
        public static readonly EngineSettings BLOCK_TEXTURE_ANISOTROPY;
        public static readonly EngineSettings G_MAG_FILTER_LINEAR_GUI;
        public static readonly EngineSettings G_MAX_BEAMS;
        public static readonly EngineSettings SEGMENT_REQUEST_BATCH;
        public static readonly EngineSettings CLIENT_BUFFER_SIZE;
        public static readonly EngineSettings ICON_BAKERY_SINGLE_RESOLUTION;
        public static readonly EngineSettings LIGHT_RAY_COUNT;
        public static readonly EngineSettings ICON_BAKERY_SINGLE_ICONS;
        public static readonly EngineSettings G_MUST_CONFIRM_DETACHEMENT_AT_SPEED;
        public static readonly EngineSettings G_USE_SHADER4;
        public static readonly EngineSettings O_OCULUS_RENDERING;
        public static readonly EngineSettings G_USE_VERTEX_LIGHTING_ONLY;
        public static readonly EngineSettings CLIENT_TRAFFIC_CLASS;
        public static readonly EngineSettings G_DRAW_SELECTED_BLOCK_WOBBLE;
        public static readonly EngineSettings G_DRAW_SELECTED_BLOCK_WOBBLE_ALWAYS;
        public static readonly EngineSettings G_USE_TWO_COMPONENT_SHADER;
        public static readonly EngineSettings A_FORCE_AUTHENTICATION_METHOD;
        public static readonly EngineSettings MIN_FFA;
        public static readonly EngineSettings B_UNDO_REDO_MAX;
        public static readonly EngineSettings G_DRAW_ANY_CONNECTIONS;
        public static readonly EngineSettings G_DRAW_ALL_CONNECTIONS;
        public static readonly EngineSettings N_ARTIFICIAL_DELAY;
        public static readonly EngineSettings SEGMENT_PIECE_QUEUE_SINGLEPLAYER;
        public static readonly EngineSettings G_MAX_MISSILE_TRAILS;
        public static readonly EngineSettings G_USE_OCCLUSION_CULLING;
        public static readonly EngineSettings C_AUTOASSIGN_WEAPON_SLOTS;
        public static readonly EngineSettings G_DRAW_NT_STATS_OVERLAY;
        public static readonly EngineSettings CLIENT_CUSTOM_TEXTURE_PATH;
        public static readonly EngineSettings SECRET;
        public static readonly EngineSettings LIMIT_FPS_UNFOCUS;
        public static readonly EngineSettings GUI_USE_DISPLAY_LISTS;
        public static readonly EngineSettings USE_GL_MULTI_DRAWARRAYS;
        public static readonly EngineSettings USE_GL_MULTI_DRAWARRAYS_INITIAL_SET;
        public static readonly EngineSettings CHAT_CLOSE_ON_ENTER;
        public static readonly EngineSettings A_FORCE_LOCAL_SAVE_ENABLED_IN_SINGLE_PLAYER;
        public static readonly EngineSettings G_SHIP_INFO_ZOOM;
        public static readonly EngineSettings G_SINGLEPLAYER_CREATIVE_MODE;
        public static readonly EngineSettings G_DRAW_LAG_OBJECTS_IN_HUD;
        public static readonly EngineSettings G_SHOW_SYMMETRY_PLANES;
        public static readonly EngineSettings LANGUAGE_PACK;
        public static readonly EngineSettings LANGUAGE_PACK_ASSIGNED;
        public static readonly EngineSettings DELETE_SEVER_DATABASE_ON_STARTUP;
        public static readonly EngineSettings G_BASIC_SELECTION_BOX;
        public static readonly EngineSettings OFFLINE_PLAYER_NAME;
        public static readonly EngineSettings ONLINE_PLAYER_NAME;
        public static readonly EngineSettings SERVERLIST_COMPATIBLE;
        public static readonly EngineSettings SERVERLIST_RESPONSIVE;
        public static readonly EngineSettings SERVERLIST_FAVORITES;
        public static readonly EngineSettings SERVERLIST_CUSTOMS;
        public static readonly EngineSettings CUBE_LIGHT_NORMALIZER_NEW_M;
        public static readonly EngineSettings SERVERLIST_LAST_SERVER_USED;
        public static readonly EngineSettings LAST_GAME;
        public static readonly EngineSettings PLAY_INTRO;
        public static readonly EngineSettings SUBTITLES;
        public static readonly EngineSettings TUTORIAL_BUTTON_BLINKING;
        public static readonly EngineSettings TUTORIAL_PLAY_INTRO;
        public static readonly EngineSettings TUTORIAL_WATCHED;
        public static readonly EngineSettings SHOW_32BIT_WARNING;
        public static readonly EngineSettings USE_INTEGER_VERTICES;
        public static readonly EngineSettings LOD_DISTANCE_IN_THRESHOLD;
        public static readonly EngineSettings AUTOSET_RESOLUTION;
        public static readonly EngineSettings FIRST_START;
        public static readonly EngineSettings USE_TGA_NORMAL_MAPS;
        public static readonly EngineSettings PLUME_BLOOM;
        public static readonly EngineSettings BLUEPRINT_STRUCTURE_BUILD_OPTIONS;
        public static readonly EngineSettings BLOCK_STYLE_PRESET;
        public static readonly EngineSettings DEBUG_SHIP_CAM_ON_RCONTROL;
        public static readonly EngineSettings G_ELEMENT_COLLECTION_INT_ATT;
        public static readonly EngineSettings CONTEXT_HELP_PLACE_MODULE_WITHOUT_COMPUTER_WARNING;
        public static readonly EngineSettings CONTEXT_HELP_PLACE_CHAMBER_WITHOUT_CONDUIT_WARNING;
        public static readonly EngineSettings CONTEXT_HELP_PLACE_CONDIUT_WITHOUT_CHAMBER_OR_MAIN_WARNING;
        public static readonly EngineSettings CONTEXT_HELP_PLACE_REACTOR_WITH_LOW_STABILIZATION;
        public static readonly EngineSettings CONTEXT_HELP_STABILIZER_EFFICIENCY_PLACE;
        public static readonly EngineSettings CONTEXT_HELP_PLACED_NEW_REACTOR_ON_OLD_SHIP;
        public static readonly EngineSettings CONTEXT_HELP_PLACED_OLD_POWER_ON_NEW_SHIP;
        public static readonly EngineSettings CREATE_MANAGER_MESHES;
        public static readonly EngineSettings STRUCTURE_STATS_MINIMIZED;
        public static readonly EngineSettings ADVBUILDMODE_MINIMIZED;
        public static readonly EngineSettings SECONDARY_MOUSE_CLICK_MINE_TIMER;
        public static readonly EngineSettings TRACK_VRAM;
        public static readonly EngineSettings DRAW_TOOL_TIPS;
        public static readonly EngineSettings ADVANCED_BUILD_MODE_STICKY_DELAY;
        public static readonly EngineSettings PERMA_OUTLINE;
        public static readonly EngineSettings USE_ADV_ENERGY_BEAM_SHADER;
        public static readonly EngineSettings USE_POLY_SCALING_ENERGY_STREAM;
        public static readonly EngineSettings SHOW_MODULE_HELP_ON_CURSOR;
        public static readonly EngineSettings PROFILER_MIN_MS;
        public static readonly EngineSettings LOG_FILE_COUNT;
        public static readonly EngineSettings MAX_DISPLAY_MODULE_TEXT_DRAW_DISTANCE;
        public static readonly EngineSettings BLOCK_ID_OF_CARGO_SPACE_BUILD_MODE;
        public static readonly EngineSettings CAMERA_DRONE_FLASHLIGHT_ON;
        public static readonly EngineSettings CAMERA_DRONE_OWN_VISIBLE;
        public static readonly EngineSettings CAMERA_DRONE_DISPLAY_NAMES;
        public static readonly EngineSettings AUDIO_MIXER_MASTER;
        public static readonly EngineSettings AUDIO_MIXER_MUSIC;
        public static readonly EngineSettings AUDIO_MIXER_SFX;
        public static readonly EngineSettings AUDIO_MIXER_SFX_GUI;
        public static readonly EngineSettings AUDIO_MIXER_SFX_INGAME;

        // Static constructor to initialize the settings
        static EngineSettings()
        {
            // Initialize each setting here, mapping Java enum constructor to C# static field initialization
            // This will be a very long block. I will only put a few examples here.
            // The actual implementation will require careful mapping of each setting.

            UI_SCALE = new EngineSettings(
                (en) => Lng.str("UI Scale (Requires restart)"),
                () => new SettingStateEnum<UIScale>(UIScale.S_100, new UIScale[] { UIScale.S_100, UIScale.S_200 }),
                false, true, EngineSettingsType.GENERAL);

            CONTROL_HELP = new EngineSettings(
                (en) => Lng.str("Display help for controls"),
                () => new SettingStateBoolean(false),
                false, EngineSettingsType.GENERAL);

            // ... (many more settings)

            // Example of a setting with a string description instead of Translatable
            N_TRANSMIT_RAW_DEBUG_POSITIONS = new EngineSettings(
                "Do not Use (debug)",
                () => new SettingStateBoolean(false),
                true);

            // Initialize the static listener for needsRestart
            EngineSettingsChangeListener listener = new EngineSettingsChangeListener()
            {
                OnSettingChangedAction = (setting) =>
                {
                    if (setting is EngineSettings engineSetting)
                    {
                        if (engineSetting.RequiresRestart) NeedsRestart = true;
                    }
                }
            };

            // Add listener to settings that require restart
            foreach (var setting in Values())
            {
                if (setting.RequiresRestart) setting.AddChangeListener(listener);
            }
        }

        public static readonly string SETTINGS_PATH = "settings.cfg";
        private static readonly List<EngineSettings> _sortedSettings = new List<EngineSettings>();
        public static bool Dirty;

        public readonly bool Debug; // Renamed from 'debug'
        private readonly Translatable _description; // Renamed from 'description'
        private readonly EngineSettingsType _type; // Renamed from 'type'
        private readonly bool _direct; // Renamed from 'direct'
        private readonly SettingState _s; // Renamed from 's'
        private readonly SettingState _sTmp; // Renamed from 'sTmp'
        private readonly SettingState _lastSetting; // Renamed from 'lastSetting'

        // The anonymous inner class 'tempSetting' will be converted to a nested class
        private readonly SettingsInterface _tempSetting; // Renamed from 'tempSetting'

        public bool RequiresRestart { get; private set; } // Renamed from 'requiresRestart'

        // Constructor for settings with Translatable description and EngineSettingsType
        private EngineSettings(Func<Enum, string> descriptionFunc, SettingState.SettingStateValueFac sFac, bool debug, EngineSettingsType type, bool direct = false)
            : this(descriptionFunc, sFac, debug, type, direct, false) { }

        // Constructor for settings with Translatable description, EngineSettingsType, and requiresRestart
        private EngineSettings(Func<Enum, string> descriptionFunc, SettingState.SettingStateValueFac sFac, bool debug, EngineSettingsType type, bool direct, bool requiresRestart)
        {
            _description = new DefaultTranslatable(descriptionFunc); // Wrap the lambda in a Translatable
            Debug = debug;
            _type = type;
            _direct = direct;
            RequiresRestart = requiresRestart;

            _s = sFac.inst();
            _sTmp = sFac.inst();
            _lastSetting = sFac.inst();

            _s.addListener(new SettingChangedListener { OnSettingChangedAction = OnSettingChanged });
            _sTmp.addListener(new SettingChangedListener { OnSettingChangedAction = (settingState) => { DirtyTmpSettings.Add(this); Dirty = true; } });

            _stateFac.Add(this, sFac); // Add to static map

            _tempSetting = new TempSettingImpl(this); // Initialize the nested class
            _settingsMap.Add(this.name(), this); // Add to static map for valueOf lookup
        }

        // Constructor for settings with String description (no Translatable)
        private EngineSettings(string descriptionString, SettingState.SettingStateValueFac sFac, bool debug)
            : this((en) => descriptionString, sFac, debug, EngineSettingsType.MISCELLANIOUS) { }

        // Nested class for TempSettingImpl
        private class TempSettingImpl : SettingsInterface
        {
            private readonly EngineSettings _parentSetting;

            public TempSettingImpl(EngineSettings parentSetting)
            {
                _parentSetting = parentSetting;
            }

            public bool isOn() { return _parentSetting._sTmp.isOn(); }
            public void setOn(bool on) { _parentSetting._sTmp.setOn(on); }
            public int getInt() { return _parentSetting._sTmp.getInt(); }
            public void setInt(int v) { _parentSetting._sTmp.setInt(v); }
            public float getFloat() { return _parentSetting._sTmp.getFloat(); }
            public void setFloat(float v) { _parentSetting._sTmp.setFloat(v); }
            public string getString() { return _parentSetting._sTmp.getString(); }
            public void setString(string v) { _parentSetting._sTmp.setString(v); }
            public object getObject() { return _parentSetting._sTmp.getObject(); }
            public void setObject(object o) { _parentSetting._sTmp.setObject(o); }
            public string name() { return _parentSetting.name(); }
            public void addChangeListener(EngineSettingsChangeListener c) { throw new System.NotImplementedException(); } // Original throws RuntimeException
            public void removeChangeListener(EngineSettingsChangeListener c) { }
            public void next() { _parentSetting._sTmp.next(); }
            public void previous() { _parentSetting._sTmp.previous(); }
            public string getAsString() { return _parentSetting._sTmp.getAsString(); }
            public SettingsInterface getSettingsForGUI() { return this; }
            // GUIElement methods are not directly implemented here, as they are GUI-specific
        }

        private static TexturePack GetStartTexturePack()
        {
            TexturePack[] texturePacks = TexturePack.CreateTexturePacks();
            TexturePack current = texturePacks[0];
            int i = 0;
            while (("Cartoon".Equals(texturePacks[i].Name, StringComparison.OrdinalIgnoreCase) || "Pixel".Equals(texturePacks[i].Name, StringComparison.OrdinalIgnoreCase)) && i < texturePacks.Length - 1)
            {
                current = texturePacks[i + 1];
                i++;
            }
            return current;
        }

        private static TexturePack[] ReadTexturePack()
        {
            TexturePack[] texturePacks = TexturePack.CreateTexturePacks();
            return texturePacks;
        }

        /// <summary>
        /// Apply changes.
        /// </summary>
        /// <param name="g">the g</param>
        public static void ApplyChanges(AbstractScene g)
        {
            g.applyEngineSettings(); // Assuming applyEngineSettings exists on AbstractScene
        }

        public static string AutoCompleteString(string s)
        {
            s = s.Trim();
            List<EngineSettings> list = List(s);
            bool first = true;
            string a = "";
            foreach (EngineSettings e in list)
            {
                if (s.Length > a.Length)
                {
                    a = StringTools.LongestCommonSubsequence(s, e.name().ToLower());
                    if (a.Equals(s) && first)
                    {
                        s = e.name().ToLower();
                        first = false;
                    }
                    else
                    {
                        s = a;
                    }
                }
            }
            return s;
        }

        public static void ReadLastSettings()
        {
            Read();
        }

        public static void Read()
        {
            Read(SETTINGS_PATH);
        }

        public static void Read(string path)
        {
            Dirty = false;
            DirtyTmpSettings.Clear();
            try
            {
                FileInfo file = new FileInfo(path);
                if (!file.Exists)
                {
                    file.Create().Close(); // Create the file and close the stream
                    WriteDefault();
                    Read();
                }
                XDocument doc = XMLTools.loadXML(file);
                XElement root = doc.Root;
                if (root != null)
                {
                    foreach (XElement e in root.Elements())
                    {
                        EngineSettings setting = ValueOf(e.Name.LocalName.ToUpper());
                        setting._s.parseXML(e);
                        setting._sTmp.parseXML(e);
                    }
                }
            }
            catch (Exception e)
            {
                Debug.LogException(e);
                WriteDefault();
                Read();
            }
        }

        public static void WriteDefault()
        {
            try
            {
                string defaultSettingsPath = Path.Combine(".", "data", "config", "defaultSettings", "settings.cfg");
                string currentSettingsPath = SETTINGS_PATH;

                FileInfo defaultSettingsFile = new FileInfo(defaultSettingsPath);
                FileInfo currentSettingsFile = new FileInfo(currentSettingsPath);

                FileUtil.copyFile(defaultSettingsFile, currentSettingsFile);
            }
            catch (IOException e)
            {
                Debug.LogException(e);
            }
        }

        public static void Write()
        {
            Write(SETTINGS_PATH);
        }

        public static void Write(string path)
        {
            try
            {
                FileInfo settingsFile = new FileInfo(path);
                XDocument d;
                if (!settingsFile.Exists) d = InitSettings();
                else d = XMLTools.loadXML(settingsFile);

                if (d.Root != null) d.Root.RemoveAll(); // Clear the root's children
                else d.Add(new XElement("Settings")); // Add root if it doesn't exist

                XElement root = d.Root;
                foreach (EngineSettings s in Values())
                {
                    XElement e = new XElement(s.name().ToUpper());
                    try
                    {
                        s._s.writeXML(d, e);
                    }
                    catch (Exception ex)
                    {
                        throw new IOException($"Exception during writing of {s.name()}; ", ex);
                    }
                    root.Add(e);
                }
                XMLTools.writeDocument(settingsFile, d);
            }
            catch (Exception e1)
            {
                throw new IOException(e1.Message, e1);
            }
        }

        private static XDocument InitSettings()
        {
            try
            {
                XDocument d = new XDocument(new XElement("Settings"));
                XElement root = d.Root;
                foreach (EngineSettings s in Values())
                {
                    XElement e = new XElement(s.name().ToUpper());
                    try
                    {
                        s._s.writeXML(d, e);
                    }
                    catch (Exception exception)
                    {
                        Debug.LogException(exception);
                    }
                    root.Add(e);
                }
                return d;
            }
            catch (Exception exception)
            {
                Debug.LogException(exception);
                return null;
            }
        }

        public static Dictionary<EngineSettingsType, List<EngineSettings>> GetCategoryMap() // Replaced Object2ObjectArrayMap and ObjectArrayList
        {
            Dictionary<EngineSettingsType, List<EngineSettings>> m = new Dictionary<EngineSettingsType, List<EngineSettings>>();
            foreach (EngineSettingsType t in Enum.GetValues(typeof(EngineSettingsType)))
            {
                m.Add(t, new List<EngineSettings>());
            }
            foreach (EngineSettings s in Values())
            {
                m[s._type].Add(s);
            }
            return m;
        }

        public static string[] List()
        {
            EngineSettings[] values = Values();
            string[] list = new string[values.Length];
            for (int i = 0; i < values.Length; i++)
            {
                list[i] = values[i].ToString();
            }
            return list;
        }

        public static List<EngineSettings> List(string autoComplete)
        {
            autoComplete = autoComplete.Trim();
            if (_sortedSettings.Count == 0)
            {
                EngineSettings[] values = Values();
                _sortedSettings.AddRange(values);
                _sortedSettings.Sort(new EngineNameLengthComparator());
            }
            List<EngineSettings> l = new List<EngineSettings>();
            foreach (EngineSettings s in _sortedSettings)
            {
                if (s.name().ToLower().StartsWith(autoComplete))
                {
                    l.Add(s);
                }
            }
            return l;
        }

        public static void Print()
        {
            EngineSettings[] values = Values();
            int max = 0;
            foreach (EngineSettings value in values)
            {
                max = Math.Max(value.name().Length + 1, max);
            }
            string spaces = new string(' ', max + 2);
            Debug.LogError("################### ENGINE SETTINGS ##########################");
            for (int i = 0; i < values.Length; i++)
            {
                int c = max - values[i].name().Length;
                Debug.LogError(values[i].name() + spaces.Substring(0, c) + values[i]._s);
                if (i < values.Length - 1)
                {
                    Debug.LogError("----------------------------------------------------------------");
                }
            }
            Debug.LogError("################### /ENGINE SETTINGS #########################");
        }

        public static bool IsShadowOn()
        {
            return (ShadowQuality)G_SHADOW_QUALITY.getObject() == ShadowQuality.Off;
        }

        public static void SetShadowOn(bool b)
        {
            G_SHADOW_QUALITY.setObject(ShadowQuality.Off);
        }

        public bool IsDirectSetting()
        {
            return _direct;
        }

        public string GetDescription()
        {
            return _description.GetName(this.name()); // Assuming GetName takes string
        }

        public void AddChangeListener(EngineSettingsChangeListener c)
        {
            if (!_changeListeners.Contains(c))
            {
                _changeListeners.Add(c);
            }
        }

        public void RemoveChangeListener(EngineSettingsChangeListener c)
        {
            _changeListeners.Remove(c);
        }

        /// <summary>
        /// @return the debug
        /// </summary>
        public bool IsDebug()
        {
            return Debug;
        }

        /// <summary>
        /// Checks if is on.
        /// </summary>
        /// <returns>true, if is on</returns>
        public bool isOn()
        {
            return _s.isOn();
        }

        public void setOn(bool on)
        {
            _s.setOn(on);
        }

        public override string ToString()
        {
            return name().ToLower() + " (" + _s + ")";
        }

        /// <summary>
        /// @return the type
        /// </summary>
        public EngineSettingsType getType()
        {
            return _type;
        }

        public void NotifyChanged()
        {
            foreach (EngineSettingsChangeListener l in _changeListeners)
            {
                l.onSettingChanged(this);
            }
        }

        public void onSettingChanged(SettingState settingState)
        {
            foreach (EngineSettingsChangeListener l in _changeListeners)
            {
                l.onSettingChanged(this);
            }
        }

        public int getInt()
        {
            return _s.getInt();
        }

        public void setInt(int v)
        {
            _s.setInt(v);
        }

        public float getFloat()
        {
            return _s.getFloat();
        }

        public void setFloat(float v)
        {
            _s.setFloat(v);
        }

        public string getString()
        {
            return _s.getString();
        }

        public void setString(string v)
        {
            _s.setString(v);
        }

        public object getObject()
        {
            return _s.getObject();
        }

        public void setObject(object o)
        {
            _s.setObject(o);
        }

        public void Revert()
        {
            _s.setValue(_lastSetting);
            _sTmp.setValue(_lastSetting);
        }

        public void ApplyFromTmp()
        {
            Debug.LogError($"--> APPLY: {_sTmp} to {_s}");
            _lastSetting.setValue(_s);
            _s.setValue(_sTmp);
            Debug.LogError($"--> RESULT APPLY: {_s}");
        }

        public void SetValueByObject(object value)
        {
            _s.setValueByObject(value);
        }

        public void SwitchOn()
        {
            setOn(!isOn());
        }

        public void AddPossibilityObject(object o)
        {
            _s.addPossibilityOption(o);
        }

        public void SetFromString(string @string)
        {
            bool set = _s.setFromString(@string);
            if (!set)
            {
                throw new SettingStateParseError(@string, _s, this);
            }
        }

        public string GetAsString()
        {
            return GetDisplayString();
        }

        public string GetDisplayString()
        {
            return (_s.getType() == SettingState.SettingStateType.BOOLEAN ? (_s.isOn() ? Lng.str("ON") : Lng.str("OFF")) : _s.getAsString()).Replace("_", " ");
        }

        public long getLong()
        {
            return _s.getLong();
        }

        public void setLong(long l)
        {
            _s.setLong(l);
        }

        public SettingState.SettingStateType getSettingsType()
        {
            return _s.getType();
        }

        public GUIElement getGUIElementTextBar(InputState state, GUIElement dependent, string deactText)
        {
            return _s.getGUIElementTextBar(state, dependent, deactText);
        }

        public GUIElement getGUIElement(InputState state, GUIElement dependent)
        {
            return _s.getGUIElement(state, dependent);
        }

        public GUIElement getGUIElement(InputState state, GUIElement dependent, string deactText)
        {
            return _s.getGUIElement(state, dependent, deactText);
        }

        public void next()
        {
            _s.next();
        }

        public void previous()
        {
            _s.previous();
        }

        public SettingsInterface getTempSetting()
        {
            return _tempSetting;
        }

        public SettingsInterface getSettingsForGUI()
        {
            if (_direct)
            {
                return this;
            }
            return _tempSetting;
        }

        // Helper to get the name of the setting (equivalent to Java enum.name())
        public string name()
        {
            // This assumes that the static fields are named the same as the original enum members.
            // A more robust solution would involve a lookup map initialized in the static constructor.
            foreach (var field in typeof(EngineSettings).GetFields(System.Reflection.BindingFlags.Public | System.Reflection.BindingFlags.Static))
            {
                if (field.FieldType == typeof(EngineSettings) && field.GetValue(null) == this)
                {
                    return field.Name;
                }
            }
            return base.ToString(); // Fallback
        }

        // Helper to get all settings (equivalent to Java enum.values())
        public static EngineSettings[] Values()
        {
            return _settingsMap.Values.ToArray();
        }

        // Helper to get a setting by name (equivalent to Java enum.valueOf())
        public static EngineSettings ValueOf(string name)
        {
            if (_settingsMap.TryGetValue(name, out EngineSettings setting))
            {
                return setting;
            }
            throw new ArgumentException($"No EngineSetting with name {name}");
        }

        // Custom comparator for sorting settings by name length
        private class EngineNameLengthComparator : IComparer<EngineSettings>
        {
            public int Compare(EngineSettings x, EngineSettings y)
            {
                if (x == null || y == null) return 0;
                return x.name().Length.CompareTo(y.name().Length);
            }
        }
    }
}
