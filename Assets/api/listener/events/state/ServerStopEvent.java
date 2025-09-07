package api.listener.events.state;

import api.listener.events.Event;

/**
 * Event fired when the server is about to shut down.
 * This is fired just before the server shutdown, not when the count-down is initiated, see {@link api.listener.events.state.ServerShutdownCounterEvent} for that.
 */
public class ServerStopEvent extends Event {

}
