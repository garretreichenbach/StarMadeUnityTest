package api.listener.events;

public abstract class Event {
    private boolean canceled = false;
    public boolean server = true;
    protected Condition condition = Condition.NONE;

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public boolean isServer() {
        return server;
    }

    public enum Condition {
        PRE,
        NONE,//For events that dont have pre/post conditions
        POST,
    }

    public Condition getCondition() {
        return condition;
    }


}
