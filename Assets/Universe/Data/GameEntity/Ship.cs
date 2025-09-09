using UnityEngine;

namespace Universe.Data.GameEntity {
	public class Ship : GameEntity {

		public Ship() : base(GameEntityType.Ship) {
			
		}

		protected override void Initialize(GameEntityData data) {
			if(data.Type != GameEntityType.Ship) {
				throw new System.Exception("Invalid entity type for Ship: " + data.Type);
			}
		}
	}
}