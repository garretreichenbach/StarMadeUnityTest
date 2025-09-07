package api.mod.config;

import api.listener.events.Event;

/**
 * Created by Jake on 12/5/2020.
 * <insert description here>
 */
public class SyncedConfigReceiveEvent extends Event {
    private FileConfiguration config;

    public SyncedConfigReceiveEvent(FileConfiguration config) {

        this.config = config;
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
