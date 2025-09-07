package api.listener;

/**
 * Priority controls when your listener is called in relation to others
 * monitor is for mods who want to be called very last, so they can "monitor" changes from other mods
 * PRE/POST are just here for good measure, preferably don't use them.
 * For testing, its fine though
 *
 * LOW or lower get fired earlier, before those in normal/high
 */
public enum EventPriority {
    PRE,
    LOWEST,
    LOW,
    NORMAL,
    HIGH,
    HIGHER,
    HIGHEST,
    MONITOR,
    POST,
}
