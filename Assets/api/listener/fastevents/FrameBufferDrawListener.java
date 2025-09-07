package api.listener.fastevents;

import org.schema.game.client.view.MainGameGraphics;

public class FrameBufferDrawListener {
    // TODO More draw controls
    public static boolean drawSphereForGodRays = true;

    // TODO more lifecycle hooks
    public void preDrawStarSky(MainGameGraphics g){}
    public void preProjectileDraw(MainGameGraphics g){}
    public void preGodRaysDraw(MainGameGraphics g){}

    public static void firePreGodRaysDraw(MainGameGraphics g){
        if(FastListenerCommon.frameBufferDrawListeners.isEmpty()) return;
        drawSphereForGodRays = true; // Reset disable draw flag

        for (FrameBufferDrawListener fbdl : FastListenerCommon.frameBufferDrawListeners) {
            fbdl.preGodRaysDraw(g);
        }
    }
}
