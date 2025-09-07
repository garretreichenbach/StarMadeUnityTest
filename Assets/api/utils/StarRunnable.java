package api.utils;

import api.DebugFile;
import api.mod.ModSkeleton;
import api.mod.StarMod;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Runs a task later, and on the main game loop
 *
 * TODO: Probably should make this thread safe.
 */
public abstract class StarRunnable {
    private long ticks;
    private long frequency;
    private StarMod mod;

    public abstract void run();
    //Runnable without mod, to be removed
    @Deprecated
    public void runLater(long ticks){
        this.ticks = ticks;
        this.delay = true;
    }

    @Deprecated
    public void runTimer(long frequency){
        this.frequency = frequency;
        this.timer = true;
    }

    public void runLater(StarMod mod, long ticks){
        this.mod = mod;
        this.ticks = ticks;
        this.delay = true;
    }
    public void runTimer(StarMod mod, long frequency){
        this.mod = mod;
        this.frequency = frequency;
        this.timer = true;
    }
    public long ticksRan = 0;
    private long time = 0;
    private boolean delay = false;
    private boolean timer = false;
    public StarRunnable(){
        register();
    }
    public StarRunnable(boolean graphics){
        register();
        this.graphics = graphics;
    }
    private boolean queuedForDelete = false;
    public void cancel(){
        queuedForDelete = true;
    }
    private void tick(){
        time++;
        ticksRan++;
        if(this.delay) {
            if (time > ticks){
                cancel();
                run();
            }
        }
        if(this.timer){
            if(time > frequency){
                time = 0;
                run();
            }
        }
    }
    private int errorCount = 0;
    private boolean graphics;
    private void register() {
        registerQueue.add(this);
    }
    private final static ConcurrentLinkedQueue<StarRunnable> runnables = new ConcurrentLinkedQueue<StarRunnable>();
    private final static ConcurrentLinkedQueue<StarRunnable> registerQueue = new ConcurrentLinkedQueue<StarRunnable>();
    public static void tickAll(boolean graphicsOnly){
        ArrayList<StarRunnable> list = new ArrayList<StarRunnable>();
        for(StarRunnable runnable : runnables){
            if(runnable.graphics == graphicsOnly) {
                try {
                    runnable.tick();
                } catch (Exception e) {
                    DebugFile.err("A StarRunnable (" + runnable.getClass().getSimpleName() + ") threw an error");
                    DebugFile.logError(e, null);
                    if (runnable.errorCount++ >= 10) {
                        DebugFile.err("=== !!! THIS RUNNABLE WILL BE TERMINATED (threw 10 errors) !!! ===");
                        runnable.queuedForDelete = true;
                    }
                }
            }
            if(!graphicsOnly && runnable.queuedForDelete){
                list.add(runnable);
            }
        }
        if(!graphicsOnly) {
            for(StarRunnable runnable : list){
                runnables.remove(runnable);
            }
            runnables.addAll(registerQueue);
            registerQueue.clear();
        }
    }
    public static void deleteAll() {
        registerQueue.clear();
        runnables.clear();
    }

    public static void deleteAllFromMod(ModSkeleton mod){
        ArrayList<StarRunnable> remQueue = new ArrayList<>();
        for (StarRunnable runnable : registerQueue) {
            if(runnable.mod == mod.getRealMod()){
                remQueue.add(runnable);
            }
        }
        for (StarRunnable runnable : runnables) {
            if(runnable.mod == mod.getRealMod()){
                remQueue.add(runnable);
            }
        }
        for (StarRunnable starRunnable : remQueue) {
            registerQueue.remove(starRunnable);
            runnables.remove(starRunnable);
        }
    }

    public StarMod getMod() {
        return mod;
    }
}

