namespace Universe.Data.GameEntity {
	public class Asteroid : GameEntity {
		public Asteroid(GameEntityType type) : base(type) { }

		protected override void Initialize(GameEntityData data) {
			if(data.Type != GameEntityType.Asteroid) {
				throw new System.Exception("Invalid entity type for Asteroid: " + data.Type);
			}
		}
	}
}