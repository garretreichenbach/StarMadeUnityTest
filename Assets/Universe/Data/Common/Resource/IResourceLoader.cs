using System;

namespace Universe.Data.Common.Resource {
	public interface IResourceLoader {
		void SetUp();

		void OnStart();

		void OnFinish();

		float GetLoadProgress();

		void UpdateLoad();
	}

	public class ResourceManager {

		GameState _gameState;

		public ResourceManager(GameState gameState) {
			_gameState = gameState;
		}
	}

	public struct LoadableResource {
		public string Path;
		public Type Type;
	}
}