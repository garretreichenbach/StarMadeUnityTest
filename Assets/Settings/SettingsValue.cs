using System;
using System.Collections.Generic;
using UnityEngine;

namespace Settings {
	public interface ISettingsValue<T> {
		public string Name { get; }

		public string Description { get; }

		public T Value { get; }

		public T DefaultValue { get; }

		public bool DebugOnly { get; }

		public List<ISettingsChangeListener> ChangeListeners { get; }
	}

	[Serializable]
	public struct IntSettingsValue : ISettingsValue<int> {
		public string Name { get; }

		public string Description { get; }

		public int Value { get; set; }

		public int DefaultValue { get; }

		public int Min { get; }

		public int Max { get; }

		public bool DebugOnly { get; }

		public List<ISettingsChangeListener> ChangeListeners { get; }

		public IntSettingsValue(string name, string description, int defaultValue, int min, int max, bool debugOnly = false, ISettingsChangeListener[] listeners = null) {
			Name = name;
			Description = description;
			DefaultValue = defaultValue;
			Value = defaultValue;
			Min = min;
			Max = max;
			DebugOnly = debugOnly;
			ChangeListeners = new List<ISettingsChangeListener>();
			if(listeners != null) {
				ChangeListeners.AddRange(listeners);
			}
		}

		public void SetValue(int newValue) {
			if(newValue < Min || newValue > Max) {
				Debug.LogWarning($"Attempted to set {Name} to {newValue}, which is outside the range of {Min} to {Max}.");
				return;
			}

			Value = newValue;
			foreach(ISettingsChangeListener listener in ChangeListeners) {
				listener.OnSettingChanged(Name, newValue);
			}
		}
	}

	[Serializable]
	public struct IntOptionsSettingsValue : ISettingsValue<int> {
		public string Name { get; }

		public string Description { get; }

		public int Value { get; set; }

		public int DefaultValue { get; }

		public int[] AllowedValues { get; }

		public bool DebugOnly { get; }

		public List<ISettingsChangeListener> ChangeListeners { get; }

		public IntOptionsSettingsValue(string name, string description, int defaultValue, int[] allowedValues, bool debugOnly = false, ISettingsChangeListener[] listeners = null) {
			Name = name;
			Description = description;
			DefaultValue = defaultValue;
			Value = defaultValue;
			AllowedValues = allowedValues;
			DebugOnly = debugOnly;
			ChangeListeners = new List<ISettingsChangeListener>();
			if(listeners != null) {
				ChangeListeners.AddRange(listeners);
			}
		}

		public void SetValue(int newValue) {
			if(Array.IndexOf(AllowedValues, newValue) < 0) {
				Debug.LogWarning($"Attempted to set {Name} to {newValue}, which is not in the list of allowed values.");
				return;
			}

			Value = newValue;
			foreach(ISettingsChangeListener listener in ChangeListeners) {
				listener.OnSettingChanged(Name, newValue);
			}
		}
	}

	[Serializable]
	public struct FloatSettingsValue : ISettingsValue<float> {
		public string Name { get; }

		public string Description { get; }

		public float Value { get; set; }

		public float DefaultValue { get; }

		public float Min { get; }

		public float Max { get; }

		public bool DebugOnly { get; }

		public List<ISettingsChangeListener> ChangeListeners { get; }

		public FloatSettingsValue(string name, string description, float defaultValue, float min, float max, bool debugOnly = false, ISettingsChangeListener[] listeners = null) {
			Name = name;
			Description = description;
			DefaultValue = defaultValue;
			Value = defaultValue;
			Min = min;
			Max = max;
			DebugOnly = debugOnly;
			ChangeListeners = new List<ISettingsChangeListener>();
			if(listeners != null) {
				ChangeListeners.AddRange(listeners);
			}
		}

		public void SetValue(float newValue) {
			if(newValue < Min || newValue > Max) {
				Debug.LogWarning($"Attempted to set {Name} to {newValue}, which is outside the range of {Min} to {Max}.");
				return;
			}

			Value = newValue;
			foreach(ISettingsChangeListener listener in ChangeListeners) {
				listener.OnSettingChanged(Name, newValue);
			}
		}
	}

	[Serializable]
	public struct FloatOptionsSettingsValue : ISettingsValue<float> {
		public string Name { get; }

		public string Description { get; }

		public float Value { get; set; }

		public float DefaultValue { get; }

		public float[] AllowedValues { get; }

		public bool DebugOnly { get; }

		public List<ISettingsChangeListener> ChangeListeners { get; }

		public FloatOptionsSettingsValue(string name, string description, float defaultValue, float[] allowedValues, bool debugOnly = false, ISettingsChangeListener[] listeners = null) {
			Name = name;
			Description = description;
			DefaultValue = defaultValue;
			Value = defaultValue;
			AllowedValues = allowedValues;
			DebugOnly = debugOnly;
			ChangeListeners = new List<ISettingsChangeListener>();
			if(listeners != null) {
				ChangeListeners.AddRange(listeners);
			}
		}

		public void SetValue(float newValue) {
			if(Array.IndexOf(AllowedValues, newValue) < 0) {
				Debug.LogWarning($"Attempted to set {Name} to {newValue}, which is not in the list of allowed values.");
				return;
			}

			Value = newValue;
			foreach(ISettingsChangeListener listener in ChangeListeners) {
				listener.OnSettingChanged(Name, newValue);
			}
		}
	}

	[Serializable]
	public struct BoolSettingsValue : ISettingsValue<bool> {
		public string Name { get; }

		public string Description { get; }

		public bool Value { get; set; }

		public bool DefaultValue { get; }

		public bool DebugOnly { get; }

		public List<ISettingsChangeListener> ChangeListeners { get; }

		public BoolSettingsValue(string name, string description, bool defaultValue, bool debugOnly = false, ISettingsChangeListener[] listeners = null) {
			Name = name;
			Description = description;
			DefaultValue = defaultValue;
			Value = defaultValue;
			DebugOnly = debugOnly;
			ChangeListeners = new List<ISettingsChangeListener>();
			if(listeners != null) {
				ChangeListeners.AddRange(listeners);
			}
		}

		public void SetValue(bool newValue) {
			Value = newValue;
			foreach(ISettingsChangeListener listener in ChangeListeners) {
				listener.OnSettingChanged(Name, newValue);
			}
		}
	}

	[Serializable]
	public struct StringSettingsValue : ISettingsValue<string> {
		public string Name { get; }

		public string Description { get; }

		public string Value { get; set; }

		public string DefaultValue { get; }

		public bool DebugOnly { get; }

		public List<ISettingsChangeListener> ChangeListeners { get; }

		public StringSettingsValue(string name, string description, string defaultValue, bool debugOnly = false, ISettingsChangeListener[] listeners = null) {
			Name = name;
			Description = description;
			DefaultValue = defaultValue;
			Value = defaultValue;
			DebugOnly = debugOnly;
			ChangeListeners = new List<ISettingsChangeListener>();
			if(listeners != null) {
				ChangeListeners.AddRange(listeners);
			}
		}

		public void SetValue(string newValue) {
			Value = newValue;
			foreach(ISettingsChangeListener listener in ChangeListeners) {
				listener.OnSettingChanged(Name, newValue);
			}
		}
	}

	[Serializable]
	public struct StringOptionsSettingsValue : ISettingsValue<string> {
		public string Name { get; }

		public string Description { get; }

		public string Value { get; set; }

		public string DefaultValue { get; }

		public string[] AllowedValues { get; }

		public bool DebugOnly { get; }

		public List<ISettingsChangeListener> ChangeListeners { get; }

		public StringOptionsSettingsValue(string name, string description, string defaultValue, string[] allowedValues, bool debugOnly = false, ISettingsChangeListener[] listeners = null) {
			Name = name;
			Description = description;
			DefaultValue = defaultValue;
			Value = defaultValue;
			AllowedValues = allowedValues;
			DebugOnly = debugOnly;
			ChangeListeners = new List<ISettingsChangeListener>();
			if(listeners != null) {
				ChangeListeners.AddRange(listeners);
			}
		}

		public void SetValue(string newValue) {
			if(Array.IndexOf(AllowedValues, newValue) < 0) {
				Debug.LogWarning($"Attempted to set {Name} to {newValue}, which is not in the list of allowed values.");
				return;
			}

			Value = newValue;
			foreach(ISettingsChangeListener listener in ChangeListeners) {
				listener.OnSettingChanged(Name, newValue);
			}
		}
	}

	[Serializable]
	public struct EnumSettingsValue<T> : ISettingsValue<T> where T : Enum {
		public string Name { get; }

		public string Description { get; }

		public T Value { get; set; }

		public T DefaultValue { get; }

		public bool DebugOnly { get; }

		public List<ISettingsChangeListener> ChangeListeners { get; }

		public EnumSettingsValue(string name, string description, T defaultValue, bool debugOnly = false, ISettingsChangeListener[] listeners = null) {
			Name = name;
			Description = description;
			DefaultValue = defaultValue;
			Value = defaultValue;
			DebugOnly = debugOnly;
			ChangeListeners = new List<ISettingsChangeListener>();
			if(listeners != null) {
				ChangeListeners.AddRange(listeners);
			}
		}

		public void SetValue(T newValue) {
			if(!Enum.IsDefined(typeof(T), newValue)) {
				Debug.LogWarning($"Attempted to set {Name} to {newValue}, which is not a valid value for enum {typeof(T).Name}.");
				return;
			}

			Value = newValue;
			foreach(ISettingsChangeListener listener in ChangeListeners) {
				listener.OnSettingChanged(Name, newValue);
			}
		}
	}

	public interface ISettingsChangeListener {
		void OnSettingChanged(string settingName, object newValue);
	}

	public class SettingsChangeListener<T> : ISettingsChangeListener {
		readonly Action<T> _onChange;

		public SettingsChangeListener(Action<T> onChange) {
			_onChange = onChange;
		}

		public void OnSettingChanged(string settingName, object newValue) {
			if(newValue is T typedValue) {
				_onChange(typedValue);
			} else {
				Debug.LogWarning($"Setting {settingName} changed to value of type {newValue.GetType().Name}, expected type {typeof(T).Name}.");
			}
		}
	}
}