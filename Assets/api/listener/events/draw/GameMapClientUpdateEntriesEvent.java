package api.listener.events.draw;

import api.listener.events.Event;
import org.schema.game.client.data.gamemap.GameMap;
import org.schema.game.client.data.gamemap.entry.MapEntryInterface;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Called whenever the client receives an answer from the server about what entries are in what sector on the Game Map
 * (M)
 */
public class GameMapClientUpdateEntriesEvent extends Event {

    private GameMap gameMap;
    private ArrayList<MapEntryInterface> entryArray;

    public GameMapClientUpdateEntriesEvent(GameMap gameMap, MapEntryInterface[] entryArray) {
        this.gameMap = gameMap;
        this.entryArray = new ArrayList<>(Arrays.asList(entryArray));
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    public ArrayList<MapEntryInterface> getEntryArray() {
        return entryArray;
    }

    public void addEntry(MapEntryInterface entry){
        entryArray.add(entry);
    }
}
