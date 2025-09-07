package api.listener;

import api.mod.StarMod;

public abstract class Listener<T> {
    private StarMod mod;
    private EventPriority priority = EventPriority.NORMAL;
    public Listener(){

    }
    public Listener(EventPriority priority){
        this.priority = priority;
    }
    public abstract void onEvent(T event);
    public void setMod(StarMod mod) {
        this.mod = mod;
    }
    public StarMod getMod() {
        return mod;
    }
    public EventPriority getPriority() {
        return priority;
    }
}
