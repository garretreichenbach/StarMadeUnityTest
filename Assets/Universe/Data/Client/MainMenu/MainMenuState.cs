using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using UnityEngine;
using UnityEngine.UI;
using Universe.Data.Common;
using Universe.Data.Common.Resource;
using Universe.Data.Inventory;
using Debug = UnityEngine.Debug;
using Random = UnityEngine.Random;

namespace Universe.Data.Client.MainMenu {
	public class MainMenuState : GameState {

		enum MainMenuLoadState {
			None,
			LoadingAssets,
			PlayingIntroVideo,
			Active,
			LoadingGame,
		}

		readonly float _bgFadeDuration = 2000f;
		readonly float _changeBgTimer = 10000f;
		float _bgFadeTimer;
		bool _bgFadingOut;
		Image _bgUI; // Placeholder for actual UI component
		Texture2D _currentBg;

		MainMenuLoadState _currentLoadState = MainMenuLoadState.None;
		MainMenuLoader _loader;
		float _updateTimer;
		AudioSource _musicSource;

		public override InventoryController InventoryController {
			get => throw new NotImplementedException("MainMenuState does not have an InventoryController");
			protected set => throw new NotImplementedException("MainMenuState does not have an InventoryController");
		}

		public override ResourceManager ResourceManager { get; set; }

		void Start() {
			ResourceManager = new ResourceManager(this);
			_loader = new MainMenuLoader {
				State = this,
			};
			_loader.SetUp();
			_bgUI = GameObject.Find("BackgroundImage").GetComponent<Image>();
			_currentBg = GetRandomBackground();
			_musicSource = gameObject.AddComponent<AudioSource>();
			_musicSource.loop = true;
			_musicSource.volume = 0.5f;
			_musicSource.playOnAwake = false;
			AudioClip mainTheme = (AudioClip)ResourceManager.GetResourceAtPath("Audio/Music/Main Theme");
			if(mainTheme != null) {
				_musicSource.clip = mainTheme;
				_musicSource.Play();
			} else {
				throw new Exception("Main Theme audio clip not found!");
			}
		}

		void Update() {
			switch(_currentLoadState) {
				case MainMenuLoadState.None:
					//Start loading assets
					_currentLoadState = MainMenuLoadState.LoadingAssets;
					UpdateLoad();
					break;

				case MainMenuLoadState.LoadingAssets:
					//Check if assets are loaded
					//If loaded, start intro video
					if(_loader.GetLoadProgress() >= 1f) {
						// _currentLoadState = MainMenuLoadState.PlayingIntroVideo; We can skip this for now tbh
						_currentLoadState = MainMenuLoadState.Active;
					} else {
						UpdateLoad();
					}
					break;

				case MainMenuLoadState.PlayingIntroVideo:
					//Check if video is done
					//If done, show main menu UI
					_currentLoadState = MainMenuLoadState.Active;
					break;

				case MainMenuLoadState.Active:
					//Wait for user input to start game or exit
					UpdateActive();
					break;

				case MainMenuLoadState.LoadingGame:
					//Show loading screen while game loads
					break;

				default:
					throw new ArgumentOutOfRangeException();
			}
		}

		void UpdateActive() {
			//Handle background image fading and changing
			_updateTimer += Time.deltaTime * 1000f;
			if(!(_updateTimer >= 100f)) return;
			_updateTimer = 0f;
			if(_bgFadingOut) {
				_bgFadeTimer += 100f;
				if(!(_bgFadeTimer >= _bgFadeDuration)) return;
				_bgFadeTimer = _bgFadeDuration;
				_bgFadingOut = false;
				_currentBg = GetRandomBackground();
			} else {
				_bgFadeTimer -= 100f;
				if(!(_bgFadeTimer <= 0f)) return;
				_bgFadeTimer = 0f;
				_bgFadingOut = true;
				//Change alpha
				if(_currentBg == null) return;
				float alpha = 1f - _bgFadeTimer / _bgFadeDuration;
				if(_bgUI != null) {
					_bgUI.color = new Color(1f, 1f, 1f, alpha);
				}
			}
		}

		Texture2D GetRandomBackground() {
			object[] backgrounds = ResourceManager.GetResourcesOfType<Texture2D>("Image/Loading Screens");
			if(backgrounds.Length == 0) {
				Debug.LogWarning("No loading screen backgrounds found!");
				return null;
			}
			int index = Random.Range(0, backgrounds.Length);
			return (Texture2D)backgrounds[index];
		}

		public override void Shutdown(bool restart = false) {
			//Todo: Handle restart?
			Application.Quit();
		}

		void UpdateLoad() {
			_loader.UpdateLoad();
		}
	}

	public struct MainMenuLoader : IResourceLoader {

		public MainMenuState State { get; set; }

		static Queue<LoadableResource> ToLoadResources {
			get => new Queue<LoadableResource>();
		}

		static Queue<LoadableResource> LoadedResources {
			get => new Queue<LoadableResource>();
		}

		Stopwatch LoadTimer { get; set; }

		float LoadProgress { get; set; }

		public void SetUp() {
			LoadProgress = 0f;
			var resourcesToLoad = new Dictionary<string, Type> {
				{ "Image/StarMade Logo.png", typeof(Texture2D) },
				{ "Image/Schine Logo.png", typeof(Texture2D) },
				{ "Image/UI/*.png", typeof(Texture2D[]) },
				{ "Image/Loading Screens/*.png", typeof(Texture2D[]) },
				{ "Audio/Music/Main Theme.ogg", typeof(AudioClip) },
				{ "Font/*.ttf", typeof(Font[]) },
			};
			CreateResourcesList(resourcesToLoad);
			OnStart();
		}

		void CreateResourcesList(Dictionary<string, Type> resourcesToLoad) {
			string resourcesPath = Application.dataPath;
			if(!Directory.Exists(resourcesPath)) {
				throw new DirectoryNotFoundException($"Resources directory not found at path: {resourcesPath}");
			}
			foreach((string path, Type type) in resourcesToLoad) {
				string fullPath = Path.Join(resourcesPath, path);
				if(type == typeof(Texture2D)) {
					if(path.Contains("*")) {
						string dirPath = Path.GetDirectoryName(fullPath);
						if(Directory.Exists(dirPath)) {
							string[] files = Directory.GetFiles(dirPath, "*.png");
							foreach(string file in files) {
								string relativePath = Path.Join(Path.GetDirectoryName(path), Path.GetFileNameWithoutExtension(file));
								ToLoadResources.Enqueue(new LoadableResource { Type = type, Path = relativePath });
							}
						} else {
							Debug.LogWarning($"Directory not found: {dirPath}");
						}
					} else {
						string relativePath = path.Replace("Resources/", "").Replace(".png", "");
						ToLoadResources.Enqueue(new LoadableResource { Type = type, Path = relativePath });
					}
				} else if(type == typeof(Texture2D[])) {
					string dirPath = Path.GetDirectoryName(fullPath);
					if(Directory.Exists(dirPath)) {
						string[] files = Directory.GetFiles(dirPath, "*.png");
						foreach(string file in files) {
							string relativePath = Path.Join(Path.GetDirectoryName(path), Path.GetFileNameWithoutExtension(file));
							ToLoadResources.Enqueue(new LoadableResource { Type = typeof(Texture2D), Path = relativePath });
						}
					} else {
						Debug.LogWarning($"Directory not found: {dirPath}");
					}
				} else if(type == typeof(AudioClip)) {
					string relativePath = path.Replace(".ogg", "");
					ToLoadResources.Enqueue(new LoadableResource { Type = type, Path = relativePath });
				} else if(type == typeof(AudioClip[])) {
					string dirPath = Path.GetDirectoryName(fullPath);
					if(Directory.Exists(dirPath)) {
						string[] files = Directory.GetFiles(dirPath, "*.ogg");
						foreach(string file in files) {
							string relativePath = Path.Join(Path.GetDirectoryName(path), Path.GetFileNameWithoutExtension(file));
							ToLoadResources.Enqueue(new LoadableResource { Type = typeof(AudioClip), Path = relativePath });
						}
					} else {
						Debug.LogWarning($"Directory not found: {dirPath}");
					}
				} else if(type == typeof(Font[])) {
					string dirPath = Path.GetDirectoryName(fullPath);
					if(Directory.Exists(dirPath)) {
						string[] files = Directory.GetFiles(dirPath, "*.ttf");
						foreach(string file in files) {
							string relativePath = Path.Join(Path.GetDirectoryName(path), Path.GetFileNameWithoutExtension(file));
							ToLoadResources.Enqueue(new LoadableResource { Type = typeof(Font), Path = relativePath });
						}
					} else {
						Debug.LogWarning($"Directory not found: {dirPath}");
					}
				} else if(type == typeof(Font)) {
					if(path.Contains("*")) {
						string dirPath = Path.GetDirectoryName(fullPath);
						if(Directory.Exists(dirPath)) {
							string[] files = Directory.GetFiles(dirPath, "*.ttf");
							foreach(string file in files) {
								string relativePath = Path.Join(Path.GetDirectoryName(path), Path.GetFileNameWithoutExtension(file));
								ToLoadResources.Enqueue(new LoadableResource { Type = type, Path = relativePath });
							}
						} else {
							Debug.LogWarning($"Directory not found: {dirPath}");
						}
					} else {
						string relativePath = path.Replace("Resources/", "").Replace(".ttf", "");
						ToLoadResources.Enqueue(new LoadableResource { Type = type, Path = relativePath });
					}
				} else {
					Debug.LogWarning($"Unsupported resource type: {type}");
				}
			}
		}

		public void OnStart() {
			LoadTimer = new Stopwatch();
			LoadTimer.Start();
		}

		public void OnFinish() {
			State.ResourceManager.AddLoadedResources(LoadedResources);
			LoadTimer.Stop();
			Debug.Log($"MainMenuLoader finished loading in {LoadTimer.ElapsedMilliseconds} ms");
		}

		public float GetLoadProgress() {
			return LoadProgress;
		}

		public void UpdateLoad() {
			if(ToLoadResources.Count > 0) {
				//Load a resource
				LoadableResource resourceToLoad = ToLoadResources.Dequeue();
				object loadedResource = null;
				if(resourceToLoad.Type == typeof(Texture2D)) {
					loadedResource = Resources.Load<Texture2D>(resourceToLoad.Path);
				} else if(resourceToLoad.Type == typeof(AudioClip)) {
					loadedResource = Resources.Load<AudioClip>(resourceToLoad.Path);
				} else if(resourceToLoad.Type == typeof(Font)) {
					loadedResource = Resources.Load<Font>(resourceToLoad.Path);
				} else {
					Debug.LogWarning($"Unsupported resource type: {resourceToLoad.Type}");
				}
				if(loadedResource != null) {
					LoadedResources.Enqueue(resourceToLoad);
				} else {
					Debug.LogWarning($"Failed to load resource at path: {resourceToLoad.Path}");
				}
				//Update progress
				int totalResources = ToLoadResources.Count + LoadedResources.Count;
				LoadProgress = (float)LoadedResources.Count / totalResources;
			} else {
				//All resources loaded
				LoadProgress = 1f;
				OnFinish();
			}
		}
	}
}