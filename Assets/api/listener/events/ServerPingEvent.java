package api.listener.events;

import api.listener.type.ServerEvent;

@ServerEvent
public class ServerPingEvent extends Event{
    private  String version;
    private  String name;
    private  String description;
    private  int players;
    private  int maxPlayers;

    public ServerPingEvent(String version, String name, String description, int players, int maxPlayers){
        //Id is used for where it is in the list.
        this.version = version;
        this.name = name;
        this.description = description;
        this.players = players;
        this.maxPlayers = maxPlayers;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPlayers(int players) {
        this.players = players;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public String getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getPlayers() {
        return players;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public static int id = 1;

    @Override
    public String toString() {
        return "ServerPingEvent{" +
                "version='" + version + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", players=" + players +
                ", maxPlayers=" + maxPlayers +
                '}';
    }
}
