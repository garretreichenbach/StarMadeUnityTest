package api.listener.fastevents;

import api.mod.StarMod;

/**
 * Created by Jake on 4/4/2021.
 */
public interface BlockConfigLoadListener {
    /**
     * Called before a mod registers its blocks
     */
    void onModLoadBlockConfig_PRE(StarMod mod);

    /**
     * Called after a mod registers its blocks
     */
    void onModLoadBlockConfig_POST(StarMod mod);

    /**
     * Called before any mods registers their BlockConfig entries
     */
    void preBlockConfigLoad();

    /**
     * Called after all mods registers their BlockConfig entries
     */
    void postBlockConfigLoad();
}
