package api.utils.simpleconfig;

import api.listener.Listener;
import api.listener.events.controller.ClientInitializeEvent;
import api.listener.events.controller.ServerInitializeEvent;
import api.listener.events.network.ClientLoginEvent;
import api.mod.StarLoader;
import api.mod.StarMod;
import api.mod.config.FileConfiguration;
import api.mod.config.SyncedConfigReceiveEvent;
import api.mod.config.SyncedConfigUtil;

import java.util.ArrayList;

/** by lupoCani on 2022-06-16
 *
 * Contains a Starloader config file, together with all boilerplate code to handle setup and syncing in the simple
 * use case.
 */
public class SimpleConfigContainer {
    private FileConfiguration config;
    private final StarMod mod;
    private boolean remote; //If true, the config is a client-side remote config to be set by the server.


    protected final ArrayList<SimpleConfigEntry<?>> entries = new ArrayList<>();
    public final String configName;
    public final boolean local;

    /** by lupoCani on 2022-06-16
     * Creates a simple config container and hooks it up to Starloader.
     *
     * @param configName the name of the config. (Must match filename.)
     * @param mod the mod handling the config.
     */

    @Deprecated
    public SimpleConfigContainer(String configName, StarMod mod) {
        this(mod, configName, false);
    }
    public SimpleConfigContainer(StarMod mod, final String configName, boolean local) {
        final SimpleConfigContainer thisContainer = this;
        this.configName = configName;
        this.local = local;
        this.remote = true;             //Assume the config is client-side until a server is created.
        this.mod = mod;
        this.config = null;             //Await server creation or remote config.

        println("Config created, provisionally in client mode with defaults.");

        //A local config is not remote
        StarLoader.registerListener(ClientInitializeEvent.class, new Listener<ClientInitializeEvent>() {
            @Override
            public void onEvent(ClientInitializeEvent event) {
                if (thisContainer.local)
                    thisContainer.setIsNotRemote();
            }
        }, mod);

        //Note when the server is created
        StarLoader.registerListener(ServerInitializeEvent.class, new Listener<ServerInitializeEvent>() {
            @Override
            public void onEvent(ServerInitializeEvent event) {
                thisContainer.setIsNotRemote();
            }
        }, mod);

        //Handle receiving a config from the server.
        StarLoader.registerListener(SyncedConfigReceiveEvent.class, new Listener<SyncedConfigReceiveEvent>() {
            @Override
            public void onEvent(SyncedConfigReceiveEvent event) {
                if (configName.equals(event.getConfig().getName()))
                    setConfig(event.getConfig());
            }
        }, mod);

        //Handle sending configs from the server to new players.
        StarLoader.registerListener(ClientLoginEvent.class, new Listener<ClientLoginEvent>() {
            @Override
            public void onEvent(ClientLoginEvent event) {
                if (thisContainer.isServer())
                    SyncedConfigUtil.sendConfigToClient(event.getServerProcessor(), thisContainer.getConfig());
            }
        }, mod);
    }

    /** by lupoCani on 2022-06-16
     *
     * Mark the config as server-side upon server creation. Can only run once, and is irreversible.
     * Also loads the config from disk. Defaults will be used if for whatever reason it cannot be loaded.
     * Entries not found on disk will be taken from default values and written to disk.
     */
    private void setIsNotRemote() {
        if (!remote) return;

        remote = false;
        config = this.mod.getConfig(this.configName);

        if (config != null) {
            readWriteFields();
            printFields();
            println("Config is server-side, loaded config values from disk.");
        } else {
            println("Loading config from disk failed, using defaults.");
        }
    }



    /** by lupoCani on 2022-06-16
     * Receives a config from the server. Can only be invoked if the config is in client mode.
     *
     * @param config the received config file.
     */
    private void setConfig(FileConfiguration config) {
        if (remote) {
            this.config = config;
            readFields();
            printFields();
            println("Received config from server.");
        }
        else {
            println( "Server-side or local config cannot be replaced over network.");
        }
    }
    public FileConfiguration getConfig() {
        return config;
    }

    /** by lupoCani on 2022-06-16
     * Reloads the config file and loads all values into their respective entry objects.
     */
    public void readFields() {
        if (config == null) {
            println("No config values to read.");
            return;
        }
        if (!remote)
            config.reloadConfig();

        for (SimpleConfigEntry<?> entry : entries)
            entry.readValue();

        forceSync();
    }

    public void readWriteFields() {
        readFields();
        writeAllFields();
    }

    public void resetFields() {
        if (!remote)
            for (SimpleConfigEntry<?> entry : entries)
                entry.resetValue();
    }

    /** by lupoCani on 2022-06-16
     * Writes all default values (ie. values originating from the default-value parameter passed on config setup)
     * to disk.
     * Can only be invoked if the config is in server mode.
     */
    public void fillEmptyFields() {
        writeFields(false);
    }

    /** by lupoCani on 2022-06-16
     * Writes all values to disk.
     * Can only be invoked if the config is in server mode.
     */
    public void writeAllFields() {
        writeFields(true);
    }

    private void writeFields(boolean all) {
        if (remote) {
            println("Will not save client-side config to disk.");
            return;
        } else if (config == null) {
            println("No config to write to.");
            return;
        }
        for (SimpleConfigEntry<?> entry : entries)
            if (entry.isDefault || all) {
                entry.writeValue();
            }
        config.saveConfig();

        forceSync();
    }

    public boolean isServer() {
        return !(local || remote);
    }

    public void forceSync() {
        if (isServer())
            SyncedConfigUtil.sendConfigToClients(config);
    }

    public void printFields() {
        println( (!isServer() ? "Client":"Server")+"-side config '" + configName + "' entries are:");
        for (SimpleConfigEntry<?> entry : entries) {
            println( "    "+entry.toString());
        }
    }

    protected void println(String str) {
        System.err.println("[config '"+mod.getName()+"': "+this.configName+"]" + str);
    }
}
