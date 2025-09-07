package api.utils.particle;

import org.schema.common.util.linAlg.Quat4Util;
import org.schema.schine.graphicsengine.core.Controller;

import javax.validation.constraints.NotNull;
import javax.vecmath.*;

/**
 * Created by Jake on 12/4/2020.
 * Uses structures like Vector4f for convenience
 *  Approximate max memory/particle: 125 bytes
 *
 * Size could be significantly reduced by using primitives only
 */
public class ModParticle implements Comparable<ModParticle> {
    /**
     * The id of the particle sprite, not to be confused with a sprite id.
     */
    public int particleSpriteId;

    public byte colorR = 127;
    public byte colorG = 127;
    public byte colorB = 127;
    public byte colorA = 127;

    public int lifetimeMs = 1000;
    public int ticksLived;
    public float cameraDistance;
    public long startTime;

    public Vector3f position = new Vector3f();
    public Quat4f rotation = new Quat4f(0,0,0,1);
    public Matrix3f normalOverride = null;

    /**
     * Calculates a view matrix based on a single vector, basically random
     * @param v
     */
    public void setArbitraryNormalOverride(Vector3f v){
        //Take non-parellel vector
        Vector3f forwardDir = new Vector3f(v.y, -v.z, -v.x);
        Vector3f rightDir = new Vector3f();
        forwardDir.cross(forwardDir, v);
        rightDir.cross(v, forwardDir);

        v.normalize();
        forwardDir.normalize();
        rightDir.normalize();

        normalOverride = new Matrix3f();
        normalOverride.setColumn(0, v); // Forward (Unused)
        normalOverride.setColumn(1, forwardDir); // Upward
        normalOverride.setColumn(2, rightDir); // Right
    }

    //What sector the particle is in. Useful for spawning sub-particles.
    public int sectorId;


    public float sizeX = 1;
    public float sizeY = 1;

    public Vector3f velocity = new Vector3f();

    public void updateCameraDistance(){
//        posHelper.set(position.x, position.y, position.z, 0.0F);
//        Matrix4f.transform(Controller.modelviewMatrix, posHelper, resHelper);
//        cameraDistance = -resHelper.z;
        Matrix4f left = Controller.modelviewMatrix;
        cameraDistance = left.m02 * position.x + left.m12 * position.y + left.m22 * position.z;
    }

    //Methods to override
    public void update(long currentTime){

    }
    public void spawn(){

    }
    public void die(){

    }
    //Helpers
    public void markForDelete(){
        lifetimeMs = 0;
    }
    public float getLifetimePercent(long currentTime){
        float offset = (float) (currentTime-startTime);
        return offset / lifetimeMs;
    }
    //Utility functions
    public static void fadeOverTime(ModParticle p, long time){
        p.colorA = (byte) ((1F-p.getLifetimePercent(time))*127F);
    }
    public static void sizeOverTime(ModParticle p, long time, float start, float end){
        float size = start + p.getLifetimePercent(time)*(end-start);
        p.sizeX = size;
        p.sizeY = size;
    }
    public static void sizeByPercent(ModParticle p, float pct, float start, float end){
        float size = start + pct*(end-start);
        p.sizeX = size;
        p.sizeY = size;
    }
    public static void colorOverTime(ModParticle p, long time, Vector4f start, Vector4f end){
        float endPercent = p.getLifetimePercent(time);
        float startPercent = 1F-endPercent;
        p.colorR = (byte) ((start.x*startPercent + end.x*endPercent) * 127F);
        p.colorG = (byte) ((start.y*startPercent + end.y*endPercent) * 127F);
        p.colorB = (byte) ((start.z*startPercent + end.z*endPercent) * 127F);
        p.colorA = (byte) ((start.w*startPercent + end.w*endPercent) * 127F);
    }
    public static void setColorF(ModParticle p, Vector4f vec){
        p.colorR = (byte) (vec.x * 127);
        p.colorG = (byte) (vec.y * 127);
        p.colorB = (byte) (vec.z * 127);
    }
    public static void rotate(ModParticle p, float amt){
        Quat4Util.mult(Quat4Util.fromAngleAxis(amt, new Vector3f(0.0F, 0.0F, 1.0F), new Quat4f()), p.rotation, p.rotation);
    }

    @Override
    public int compareTo(@NotNull ModParticle o) {
        return Float.compare(this.cameraDistance, o.cameraDistance);
    }
}
