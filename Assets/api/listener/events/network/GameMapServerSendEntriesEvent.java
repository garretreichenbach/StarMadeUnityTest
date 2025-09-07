package api.listener.events.network;

import api.listener.events.Event;
import org.schema.game.client.data.gamemap.entry.MapEntryInterface;
import org.schema.game.client.data.gamemap.requests.GameMapAnswer;
import org.schema.game.server.controller.GameMapProvider;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Jake on 10/3/2020.
 * <insert description here>
 */
public class GameMapServerSendEntriesEvent extends Event {
    private GameMapProvider.Answer answer;
    private GameMapAnswer gameMapAnswer;
    private ArrayList<MapEntryInterface> dataArray;
    public GameMapServerSendEntriesEvent(GameMapProvider.Answer answer) {
        this.answer = answer;
        this.gameMapAnswer = answer.answer.get();
        dataArray = new ArrayList<>(Arrays.asList(gameMapAnswer.data));
    }

    public GameMapProvider.Answer getAnswer() {
        return answer;
    }

    public GameMapAnswer getGameMapAnswer() {
        return gameMapAnswer;
    }

    public ArrayList<MapEntryInterface> getDataArray() {
        return dataArray;
    }
    public void addEntry(MapEntryInterface entry){
        dataArray.add(entry);
    }
}
