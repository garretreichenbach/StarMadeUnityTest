using System.Threading.Tasks;
using LiteDB;
using Unity.VisualScripting;
using UnityEngine;
using Universe.Data.GameEntity;

namespace Universe.World.Database {

	/**
	 * Manages the saving and loading of game entities to and from the database using LiteDB.
	 */
	public class EntityDatabaseManager : MonoBehaviour {

		[Header("Database Stats")]
		[InspectorLabel("Current Total Entities")]
		[Tooltip("The total number of entities active in the scene.")]
		public int CurrentActiveEntityCount { get; private set; }

		[InspectorLabel("Current Loaded Entities")]
		[Tooltip("The number of entities currently chunk loaded in memory.")]
		public int CurrentLoadedEntityCount { get; private set; }

		[InspectorLabel("Current Unloaded Entities")]
		[Tooltip("The number of entities that are active in the scene, but not chunk loaded].")]
		public int CurrentUnloadedEntityCount { get; private set; }

		//Todo: More stats like amount of entities currently being processed for saving/loading, etc.

		public static EntityDatabaseManager Instance { get; private set; }

		const string UniverseName = "world0"; //Todo: Make this based on the current universe being played

		LiteDatabase _db;

		void Awake() {
			if(Instance != null && Instance != this) {
				Destroy(this);
			} else {
				Instance = this;
				InitDB();
			}
		}

		/**
		 * Initializes the LiteDB database connection and sets up necessary collections and indexes.
		 */
		void InitDB() {
			var folderPath = System.IO.Path.Combine(Application.persistentDataPath, "Database");
			if(!System.IO.Directory.Exists(folderPath)) {
				Debug.Log($"Creating database folder at {folderPath}");
				System.IO.Directory.CreateDirectory(folderPath);
			}
			var filePath = System.IO.Path.Combine(folderPath, $"{UniverseName}.db");
			if(System.IO.File.Exists(filePath)) {
				LoadDB(filePath);
			} else {
				CreateDB(filePath);
			}
		}

		void CreateDB(string filePath) {
			Debug.Log($"Creating new database at {filePath}");
			_db = new LiteDatabase($"Filename={filePath};Connection=shared;");

		}

		void LoadDB(string filePath) {
			Debug.Log($"Loading existing database at {filePath}");
			_db = new LiteDatabase($"Filename={filePath};Connection=shared;");
		}

		/**
		* Loads the entity data from the database, and creates an empty game object in the scene.
		* Note: This does not load the entity's block data, just the entity data itself
		*/
		public Task<GameEntity> LoadEntityAsync(long entityDBID) {

		}

		/**
		 * Saves the entity data to the database, including all block data (if the entity is loaded)
		 */
		public Task SaveEntityAsync(GameEntity entity) {

		}

		/**
		 * Returns the latest available database ID for a new entity.
		 */
		public long GetNewDBID() {

		}

	}
}