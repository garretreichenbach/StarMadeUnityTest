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
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;

import javax.vecmath.Vector3f;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.schema.game.client.view.GameResourceLoader.gasGiantTextures;
import static org.schema.game.client.view.planetgas.GasPlanetSurfaceDrawer.*;

public class GasPlanetAtmosphereOuterDrawer implements Drawable, Shaderable {
    public static float ATMOSPHERE_SIZE_FACTOR_OUTER = 0.56f; //idk, 559?
    static Shader outerAtmosphereShader;
    static Shader innerAtmosphereShader; //TODO: basic diffuse shader, but inside out and has transparency decreasing with depth (also tinted solar specular & sunset color band?)
    private final GameClientState state;
    float time = 0;
    float scale = 0;
    float depth = 0;
    Vector3i relSectorPos = new Vector3i();
    Vector3f offset = new Vector3f();
    Vector3f modelScale = new Vector3f();
    //    Dodecahedron h;
    Mesh highMesh;
    Mesh lowMesh;
    Mesh currentMesh;
    Transform tr = new Transform();
    GasPlanetInformation currentPlanet = null;

    public GasPlanetAtmosphereOuterDrawer(GameClientState state) {
        this.state = state;
    }

    @Override
    public void onExit() {
    }

    @Override
    public void updateShader(DrawableScene scene) {
        GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    @Override
    public void updateShaderParameters(Shader s) {
        GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, gasGiantTextures[currentPlanet.getTextureID()].getTextureId()); //needed for inner sky
        if(currentPlanet != null) {
            GlUtil.updateShaderFloat(s, "density", 1.5F);
            GlUtil.updateShaderColor4f(s, "fvDiffuse", currentPlanet.getColor2());
            GlUtil.updateShaderColor4f(s, "fvAtmoColor", currentPlanet.getColor2());
            GlUtil.updateShaderColor4f(s, "diffuse", currentPlanet.getColor2()); //idk lol
            GlUtil.updateShaderFloat(s, "fGlowPower", 15.0f);
            GlUtil.updateShaderFloat(s, "fAbsPower", 0.4f);
            GlUtil.updateShaderFloat(s, "fCloudHeight", 0.08F);
            GlUtil.updateShaderFloat(s, "dist", offset.length());
        }
    }

    @Override
    public void cleanUp() {
        if(highMesh != null) {
            highMesh.cleanUp();
            lowMesh.cleanUp();
        }
    }

    @Override
    public void draw() {
        /*
         * Special case; we don't call this method because there's a close and far version of the render procedure
         * PRE-CAMERA-PREPARE: drawAtmospheres(false);
         * POST: drawAtmospheres(true);
         */
    }


    public void preCameraPrepare() {
        drawAtmospheres(false);
    } //TODO: probably doing both under precam is fine...

    public void postWorldDraw() {
        drawAtmospheres(true);
    }


    public void drawAtmospheres(boolean cull) {

        float sectorSize = GameClientState.instance.getSectorSize();
        FORCED_PERSPECTIVE_HORIZON = sectorSize * 2; //hmmmmmmm
        FORCED_PERSPECTIVE_HORIZON_SQ = FORCED_PERSPECTIVE_HORIZON * FORCED_PERSPECTIVE_HORIZON;
        if(lowMesh != null) {
            Transform tr = new Transform();
            for(Map.Entry<Vector3i, GasPlanetInformation> entry : gasGiantsToDraw.entrySet()) {
                tr.setIdentity();
                currentPlanet = entry.getValue();
                Vector3i sector = entry.getKey();
                relSectorPos.set(sector);
                Vector3i currentSec = GameClientState.instance.getPlayer().getCurrentSector();
                relSectorPos.sub(currentSec);

                float secLength = relSectorPos.lengthSquared();
                if(!cull && secLength <= 2) {
                    continue;
                }
                if(cull && secLength > 3) {
                    continue;
                }

                //boolean isOutOfSector = relativeSector.lengthSquared() != 0;
                currentMesh = highMesh; //Mesh mesh = isOutOfSector? lowMesh : highMesh; //once the lowres mesh actually works

                GasPlanetInformation planet = entry.getValue();
                Vector3f drawPosition = relSectorPos.toVector3f();
                drawPosition.scale(sectorSize);

                Vector3f camPos = Controller.getCamera().getWorldTransform().origin;
                offset.set(drawPosition);
                offset.sub(camPos); //we don't really care about camera zoom here as this is not hiding anything

                float scaleFactor = 1.0f;
                if(offset.lengthSquared() > FORCED_PERSPECTIVE_HORIZON_SQ) {
                    //mesh = lowMesh; //borked
                    float length = FastMath.sqrt(offset.lengthSquared());  //Vector3f.length() doesn't use fastmath.
                    float distFromHorizon = length - FORCED_PERSPECTIVE_HORIZON; //TODO: smoothstep
                    scaleFactor = 1 / (1 + ((distFromHorizon / sectorSize) * FORCED_PERSPECTIVE_FACTOR));
                }

                scale = planet.getRadius();
                //float bulge = scale * currentPlanet.rotationRate * EQUATOR_BULGE_FACTOR; //irl this probably isn't linear but whatever
                //Vector3f modelScale = new Vector3f(scale + bulge, scale - bulge, scale + bulge); //are these the right axes? TODO: no, they aren't. need to rotate the bulge vector to the actual planetary axis
                modelScale.set(scale * ATMOSPHERE_SIZE_FACTOR_OUTER, scale * ATMOSPHERE_SIZE_FACTOR_OUTER, scale * ATMOSPHERE_SIZE_FACTOR_OUTER);
                modelScale.scale(MODEL_SCALE_FACTOR);
                modelScale.scale(scaleFactor); //shrink model proportionally if beyond the false size horizon

                //trans.basis.rotX((FastMath.PI * 2) * year);
                tr.origin.set(drawPosition);
                if(offset.length() >= (NEAR_FORCED_PERSPECTIVE_HORIZON * scale) + 50) {
                    GlUtil.glPushMatrix();
                    GlUtil.glMultMatrix(tr);
                    currentMesh.loadVBO(true);

                    GL11.glDepthRange(0.9999998807907104D, 1.0D);
                    GlUtil.glEnable(GL_BLEND);
                    GlUtil.glDepthMask(false);
                    GlUtil.glBlendFunc(GL_SRC_ALPHA, 1);


                    outerAtmosphereShader.setShaderInterface(this);
                    outerAtmosphereShader.load();

                    GlUtil.glEnable(GL_CULL_FACE);
                    GL11.glCullFace(GL_BACK);

                    GlUtil.scaleModelview(modelScale.x, modelScale.y, modelScale.z); //lol, why is there no scaleModelview(vector3f)
                    GlUtil.rotateModelview(90f + planet.getAxialTiltX(), 1f, 0f, 0f);
                    GlUtil.rotateModelview(planet.getAxialTiltY(), 0f, 1f, 0f);
                    GlUtil.rotateModelview(time * planet.getRotationRate(), 0f, 0f, 1f);

                    //GL11.glCullFace(GL_BACK);

                    currentMesh.renderVBO();
                    GL11.glCullFace(GL_BACK);
                    outerAtmosphereShader.unload();
                    GlUtil.glEnable(GL_DEPTH_TEST);
                    GlUtil.glDisable(GL_BLEND);
                    GlUtil.glEnable(GL_CULL_FACE);
                    GlUtil.glPopMatrix();
                    GL11.glDepthRange(0.0D, 1.0D);
                    GlUtil.glDepthMask(true);
                    currentMesh.unloadVBO(true);
                    GlUtil.glBlendFunc(770, 1); //shrug
                }
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
        outerAtmosphereShader = ShaderLibrary.atmosphereShader;
        innerAtmosphereShader = ShaderLibrary.gasGiantInnerAtmoShader;
//        lowMesh = (Mesh) Controller.getResLoader().getMesh("Sphere").getChilds().iterator().next();
//        mesh = (Mesh) Controller.getResLoader().getMeshLoader().getModMesh(StarExtractorNew.inst, "planet_sphere").getChilds().iterator().next();
//        mesh = (Mesh) Controller.getResLoader().getMesh("GeoSphere").getChilds().iterator().next();
//        h = new Dodecahedron(500);
//        h.create();
    }

    public void update(Timer timer) {
        time += timer.getDelta() * 10.1F;
    }
}
