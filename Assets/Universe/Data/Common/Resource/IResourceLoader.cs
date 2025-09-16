using System;
using System.Collections.Generic;
using System.Linq;
using UnityEngine;

namespace Universe.Data.Common.Resource {
	public interface IResourceLoader {
		void SetUp();

		void OnStart();

		void OnFinish();

		float GetLoadProgress();

		void UpdateLoad();
	}

	public class ResourceManager {

		readonly Dictionary<string, object> _loadedResources = new Dictionary<string, object>();

		GameState _gameState;

		public ResourceManager(GameState gameState) {
			_gameState = gameState;
		}

		public object[] GetResourcesOfType<T>(string path) {
			return (from res in _loadedResources where res.Key.StartsWith(path) && res.Value is T select res.Value).ToArray();
		}

		public object GetResourceAtPath(string path) {
			if(_loadedResources.TryGetValue(path, out object res)) {
				return res;
			}
			Debug.LogError($"Tried to get resource at path {path} but it does not exist in the loaded resources.");
			return null;
		}

		public void AddLoadedResources(Queue<LoadableResource> loadedResources) {
			while(loadedResources.Count > 0) {
				LoadableResource res = loadedResources.Dequeue();
				if(!_loadedResources.TryAdd(res.Path, res)) {
					Debug.LogWarning($"Tried to add resource at path {res.Path} but it already exists in the loaded resources.");
				}
			}
		}
	}

	public struct LoadableResource {
		public string Path;
		public Type Type;
	}
}