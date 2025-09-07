package org.schema.game.client.view.planetgas;

import com.bulletphysics.linearmath.Transform;
import org.lwjgl.opengl.GL11;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.world.planet.gasgiant.GasPlanetInformation;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.Shaderable;

import javax.vecmath.Vector3f;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.schema.game.client.view.GameResourceLoader.gasGiantTextures;
import static org.schema.game.common.data.world.planet.old.texgen.MathUtil.clamp;
import static org.schema.schine.graphicsengine.core.Controller.getCamera;
import static org.schema.schine.graphicsengine.shader.ShaderLibrary.gasPlanetSurfaceShader;

public class GasPlanetSurfaceDrawer implements Drawable, Shaderable {
    public static float FORCED_PERSPECTIVE_FACTOR = 0.85f;
    public static float MODEL_SCALE_FACTOR = 0.00334f; //model-dependent; has to be tuned every time :(

    public static HashMap<Vector3i, GasPlanetInformation> gasGiantsToDraw = new HashMap<>();
    public static float FORCED_PERSPECTIVE_HORIZON = 10000f; //TODO: add commands and calibrate.
    public static float NEAR_FORCED_PERSPECTIVE_HORIZON = 0.025f; //giant begins scaling down beneath the camera at this fraction of the overall giant radius above the surface
    public static float GIANT_LOWER_SURFACE = 0.8f; //you sink into the model (and get repelled) here
    static float FORCED_PERSPECTIVE_HORIZON_SQ;
    static GasPlanetInformation currentPlanet;
    static Vector3f drawPosition = new Vector3f();
    static Vector3f offset = new Vector3f();
    static Vector3f trueOffset = new Vector3f();
    private final GameClientState state;
    float alpha;
    float time = 0;

    //    Dodecahedron h;
    Mesh highMesh;
    Mesh lowMesh;

    public GasPlanetSurfaceDrawer(GameClientState state) {
        this.state = state;
    }

    public static void clearGasGiants(){
        gasGiantsToDraw.clear();
    }
    public static void addGasGiant(Vector3i sector, GasPlanetInformation info){
        gasGiantsToDraw.put(new Vector3i(sector), info);
    }

    @Override
    public void onExit() {
    }

    @Override
    public void updateShader(DrawableScene scene) {
        GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    @Override
    public void updateShaderParameters(Shader shader) {
        GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, gasGiantTextures[currentPlanet.getTextureID()].getTextureId()); //TODO texcoord y *= 0.5f in shader?
        //GlUtil.updateShaderVector4f(shader, "lightDiffuse", currentPlanet.tint.x, currentPlanet.tint.y, currentPlanet.tint.z, currentPlanet.tint.w);
        GlUtil.updateShaderFloat(shader, "time", time / 50f); //speed of lava wobblies. The effect is really just there to break up the staticness of the texture, so this should be very slow, else it's just disturbing.
        GlUtil.updateShaderFloat(shader, "ambientLevel", 0.03f); // should be pretty low. 0.0 gives the stark, realistic contrast but SM space backgrounds tend to be bright and it looks incongruous to have zero ambient lighting when the sky is so bright
        //GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, GameResourceLoader.lavaTexture.getTextureId());
        GlUtil.updateShaderInt(shader, "lavaTex", 0); //TODO: make a custom version of lavatex with some tweaked params
        if (currentPlanet != null) {
            GlUtil.updateShaderColor4f(shader, "color1", currentPlanet.getColor1());
            GlUtil.updateShaderColor4f(shader, "color2", currentPlanet.getColor2());
            GlUtil.updateShaderFloat(shader, "alpha", alpha);
        }
    }


    @Override
    public void cleanUp() {
        if(highMesh != null){
            highMesh.cleanUp();
            lowMesh.cleanUp();
        }
    }

    @Override
    public void draw() {
        /*
        * Special case; we don't call this method because there's a close and far version of the render procedure
        * PRE-CAMERA-PREPARE: drawGasGiants(false);
        * POST: drawGasGiants(true);
        */
    }

    public void drawGasGiants(boolean cull){
        float sectorSize = GameClientState.instance.getSectorSize();
        FORCED_PERSPECTIVE_HORIZON = sectorSize * 2;
        FORCED_PERSPECTIVE_HORIZON_SQ = FORCED_PERSPECTIVE_HORIZON * FORCED_PERSPECTIVE_HORIZON;
        if (lowMesh != null) {
            Transform tr = new Transform();
            for (Map.Entry<Vector3i, GasPlanetInformation> entry : gasGiantsToDraw.entrySet()) {
                alpha = 1.0f;
                tr.setIdentity();
                Vector3i sector = entry.getKey();
                Vector3i relativeSector = new Vector3i(sector);
                Vector3i currentSec = GameClientState.instance.getPlayer().getCurrentSector();
                relativeSector.sub(currentSec);

                float secLength = relativeSector.lengthSquared();
                if(!cull && secLength <= 2){
                    continue;
                }
                if(cull && secLength > 3){
                    continue;
                }

                //boolean isOutOfSector = relativeSector.lengthSquared() != 0;
                Mesh mesh = highMesh; //Mesh mesh = isOutOfSector? lowMesh : highMesh; //once the lowres mesh actually works

                currentPlanet = entry.getValue();
                drawPosition.set(relativeSector.toVector3f());
                drawPosition.scale(sectorSize);
                //drawPosition.add(entry.getValue().inSectorOffset); //TODO assume centre for now

                Vector3f playerPos = getCamera().getWorldTransform().origin;
                Vector3f camPos = getCamera().getOffsetPos(new Vector3f()); //getCamera().getWorldTransform().origin;
                offset.set(drawPosition);
                trueOffset.set(drawPosition);
                offset.sub(playerPos);
                trueOffset.sub(camPos);

                float scale = currentPlanet.getRadius();
                float scaleFactor = 1.0f;

                GlUtil.glEnable(GL_DEPTH_TEST); //??????????????????
                GlUtil.glDepthMask(true); //???????

                if(offset.lengthSquared() > FORCED_PERSPECTIVE_HORIZON_SQ){
                    //mesh = lowMesh; //borked
                    float length = FastMath.sqrt(offset.lengthSquared());  //Vector3f.length() doesn't use fastmath.
                    float distFromHorizon = length - FORCED_PERSPECTIVE_HORIZON;
                    scaleFactor = 1/(1 + ((distFromHorizon/sectorSize)*FORCED_PERSPECTIVE_FACTOR));
                } else if(trueOffset.lengthSquared() < (scale + (NEAR_FORCED_PERSPECTIVE_HORIZON * scale)) * (scale + (NEAR_FORCED_PERSPECTIVE_HORIZON * scale))){
                    //hard horizon should be r=1 (scale) itself; ramp to there from configured horizon
                    float length = FastMath.sqrt(trueOffset.lengthSquared());  //Vector3f.length() doesn't use fastmath.
                    float distFromFloor = Math.max(0.00001f,((length/(scale + (NEAR_FORCED_PERSPECTIVE_HORIZON * scale)))-GIANT_LOWER_SURFACE)/(1-GIANT_LOWER_SURFACE));
                    //alpha = smoothstep(1f, 0.00000001f, distFromFloor);
                    float s = smoothstep(1/distFromFloor, 1f, distFromFloor);
                    scaleFactor = length / (scale + ((NEAR_FORCED_PERSPECTIVE_HORIZON * scale) * (distFromFloor) * s)); //lerp with length/scale based on distFromFloor
                    //TODO: Surface shader should fade to a darkened average of the two giant colours the deeper you go
                    //TODO: Surface below camera should emit particles
                    //TODO: Generally unjank this
                }
                scale *= scaleFactor;

                //float bulge = scale * currentPlanet.rotationRate * EQUATOR_BULGE_FACTOR; //irl this probably isn't linear but whatever
                //Vector3f modelScale = new Vector3f(scale + bulge, scale - bulge, scale + bulge); //are these the right axes? TODO: no, they aren't. need to rotate the bulge vector to the actual planetary axis
                Vector3f modelScale = new Vector3f(scale,scale,scale);
                modelScale.scale(MODEL_SCALE_FACTOR);

                gasPlanetSurfaceShader.setShaderInterface(this);
                gasPlanetSurfaceShader.load();

                //trans.basis.rotX((FastMath.PI * 2) * year); //orbits??
                tr.origin.set(drawPosition);
                mesh.loadVBO(true);

                GlUtil.glPushMatrix();
                GlUtil.glMultMatrix(tr);

                GlUtil.scaleModelview(modelScale.x, modelScale.y, modelScale.z); //lol, why is there no scaleModelview(vector3f)
                GlUtil.rotateModelview(90f + currentPlanet.getAxialTiltX(),1f,0f,0f);
                GlUtil.rotateModelview(currentPlanet.getAxialTiltY(), 0f,0f,1f);
                GlUtil.rotateModelview(time * currentPlanet.getRotationRate(),0f,1f,0f);

                mesh.renderVBO();
                GlUtil.glPopMatrix();
                mesh.unloadVBO(true);
                gasPlanetSurfaceShader.unload();
            }

        }
    }

    @Override
    public boolean isInvisible() {
        return false;
    }

    @Override
    public void onInit() {
        highMesh = (Mesh) (Controller.getResLoader().getMesh("GeoSphere").getChilds().get(0)); //should it be GeoSphere?
        lowMesh = (Mesh) (Controller.getResLoader().getMesh("GeoSphere").getChilds().get(0));
    }

    public void update(Timer timer) {
        time += timer.getDelta()*10.1F;
    }

    public static float smoothstep(float from, float to, float x) {
        x = clamp((x - from) / (to - from), 0.0f, 1.0f);
        return x * x * (3 - 2 * x);
    }
}
