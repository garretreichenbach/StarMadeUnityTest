package org.schema.game.server.data.blueprintnw;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.TranslatableEnum;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;

import java.util.List;

@SuppressWarnings("unchecked")
public enum BlueprintClassification implements TranslatableEnum{
	NONE(EntityType.SHIP),
	MINING(EntityType.SHIP),
	SUPPORT(EntityType.SHIP),
	CARGO(EntityType.SHIP),
	ATTACK(EntityType.SHIP),
	DEFENSE(EntityType.SHIP),
	CARRIER(EntityType.SHIP),
	SCOUT(EntityType.SHIP),
	SCAVENGER(EntityType.SHIP),
	NONE_STATION(EntityType.SPACE_STATION),
	SHIPYARD_STATION(EntityType.SPACE_STATION),
	OUTPOST_STATION(EntityType.SPACE_STATION),
	DEFENSE_STATION(EntityType.SPACE_STATION),
	MINING_STATION(EntityType.SPACE_STATION),
	FACTORY_STATION(EntityType.SPACE_STATION),
	TRADE_STATION(EntityType.SPACE_STATION),
	WAYPOINT_STATION(EntityType.SPACE_STATION),
	SHOPPING_STATION(EntityType.SPACE_STATION),
	
	NONE_ASTEROID(EntityType.ASTEROID),
	NONE_ASTEROID_MANAGED(EntityType.ASTEROID_MANAGED),
	NONE_PLANET(EntityType.PLANET_SEGMENT),
	NONE_SHOP(EntityType.SHOP),
	NONE_ICO(EntityType.PLANET_ICO),
	ALL_SHIPS(EntityType.SHIP);

	public final EntityType type;
	private BlueprintClassification(EntityType type){
		this.type = type;
	}
	private static List<BlueprintClassification>[] byType;

	public String getName(){
		return switch(this) {
			case ATTACK -> Lng.str("Attacker");
			case CARGO -> Lng.str("Cargo Transport");
			case CARRIER -> Lng.str("Carrier");
			case DEFENSE -> Lng.str("Defender");
			case DEFENSE_STATION -> Lng.str("Defense Station");
			case FACTORY_STATION -> Lng.str("Factory");
			case MINING -> Lng.str("Miner");
			case MINING_STATION -> Lng.str("Mining Station");
			case NONE -> Lng.str("Ship");
			case NONE_STATION -> Lng.str("Space Station");
			case OUTPOST_STATION -> Lng.str("Outpost");
			case SCAVENGER -> Lng.str("Scavenger");
			case SCOUT -> Lng.str("Scout");
			case SHIPYARD_STATION -> Lng.str("Shipyard");
			case SHOPPING_STATION -> Lng.str("Shopping");
			case SUPPORT -> Lng.str("Support");
			case TRADE_STATION -> Lng.str("Trade Station");
			case WAYPOINT_STATION -> Lng.str("Warp Gate");
			case NONE_ASTEROID -> Lng.str("");
			case NONE_ASTEROID_MANAGED -> Lng.str("");
			case NONE_PLANET -> Lng.str("");
			case NONE_SHOP -> Lng.str("");
			case ALL_SHIPS -> Lng.str("All");
			default -> Lng.str("Unknown");
		};
	}
	
	static{
		byType = new ObjectArrayList[EntityType.values().length];
		for(int i = 0; i < byType.length; i++){
			byType[i] = new ObjectArrayList<BlueprintClassification>(); 
		}
		for(BlueprintClassification b : values()){
			if(b != ALL_SHIPS) byType[b.type.ordinal()].add(b);
		}
		for(int i = 0; i < byType.length; i++){
			((ObjectArrayList<BlueprintClassification>)byType[i]).trim(); 
		}
	}
	
	public static List<BlueprintClassification> shipValues() {
		return byType[EntityType.SHIP.ordinal()];
	}
	public static List<BlueprintClassification> stationValues() {
		return byType[EntityType.SPACE_STATION.ordinal()];
	}
	public static GUIElement[] getGUIElements(InputState state, EntityType entity) {
		int s = 0;
		for(int i = 0; i < values().length - 1; i++){
			if(values()[i].type == entity && values()[i] != ALL_SHIPS){
				s++;
			}
		}
		GUIElement[] elems = new GUIElement[s];
		s = 0;
		for(int i = 0; i < values().length - 1; i++){
			if(values()[i].type == entity && values()[i] != ALL_SHIPS){
				GUIAnchor c = new GUIAnchor(state, 300, 24);
				c.setUserPointer(values()[i]);
				GUITextOverlay t = new GUITextOverlay(state);
				t.setTextSimple(values()[i].getName());
				t.setUserPointer(values()[i]);
				t.setPos(4, 6, 0);
				c.attach(t);
				elems[s] = c;
				s++;
			}
		}
		return elems;
	}
}
