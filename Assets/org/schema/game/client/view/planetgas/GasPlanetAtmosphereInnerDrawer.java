package org.schema.game.client.view.planetgas;


import api.utils.draw.ModWorldDrawer;
import com.bulletphysics.linearmath.Transform;
import org.lwjgl.opengl.GL11;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.world.planet.gasgiant.GasPlanetInformation;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.schema.game.client.view.GameResourceLoader.gasGiantTextures;
import static org.schema.game.client.view.planetgas.GasPlanetSurfaceDrawer.FORCED_PERSPECTIVE_HORIZON;
import static org.schema.game.common.data.world.planet.gasgiant.GasPlanetInformation.BASE_GIANT_SIZE;
import static org.schema.schine.graphicsengine.core.Controller.getCamera;
import static org.schema.game.client.view.planetgas.GasPlanetSurfaceDrawer.*;

public class GasPlanetAtmosphereInnerDrawer implements Drawable, Shaderable {
    private static final float ATMOSPHERE_SIZE_FACTOR_INNER = 1.06f;
    private final GameClientState state;
    float time = 0;
    float scale = 0;
    float depth = 0;
    Vector3i relSectorPos = new Vector3i();
    Vector3f trueOffset = new Vector3f();
    Vector3f modelScale = new Vector3f();
    static Shader innerAtmosphereShader; //TODO: tinted solar specular & sunset color band?
    public static float FULL_INNER_OPAQUE_HORIZON = 0.95f;

    Mesh highMesh;
    Mesh lowMesh; //honestly we might be able to get away with using just the lowmesh... scale is off though
    Mesh currentMesh;
    Transform tr = new Transform();
    GasPlanetInformation currentPlanet = null; //failsafe first-pass value

    public GasPlanetAtmosphereInnerDrawer(GameClientState state) {
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
        if (currentPlanet != null) {
            GlUtil.updateShaderColor4f(innerAtmosphereShader, "color1", currentPlanet.getColor1());
            GlUtil.updateShaderColor4f(innerAtmosphereShader, "color2", currentPlanet.getColor2());
            GlUtil.updateShaderFloat(innerAtmosphereShader, "depth", depth);
            GlUtil.updateShaderVector4f(innerAtmosphereShader, "viewPos", new Vector4f(trueOffset.x, trueOffset.y, trueOffset.z, 1));
            GlUtil.updateShaderFloat(innerAtmosphereShader, "ambientLevel", 0.15f); // should be pretty low, but we don't want to wash the colour out totally, not to mention nightside scattering, starlight, moonlight, lightning, etc. would be factors here
        }
    }

    @Override
    public void cleanUp() {
        if (highMesh != null) {
            highMesh.cleanUp();
            lowMesh.cleanUp();
        }
    }

    @Override
    public void draw() {
        float sectorSize = GameClientState.instance.getSectorSize();
        FORCED_PERSPECTIVE_HORIZON = sectorSize * 2; //hmmmmmmm
        FORCED_PERSPECTIVE_HORIZON_SQ = FORCED_PERSPECTIVE_HORIZON * FORCED_PERSPECTIVE_HORIZON;
        if (lowMesh != null) {
            Transform tr = new Transform();
            for (Map.Entry<Vector3i, GasPlanetInformation> entry : gasGiantsToDraw.entrySet()) {
                tr.setIdentity();
                currentPlanet = entry.getValue();
                Vector3i sector = entry.getKey();
                relSectorPos.set(sector);
                Vector3i currentSec = GameClientState.instance.getPlayer().getCurrentSector();
                relSectorPos.sub(currentSec);

                float secLength = relSectorPos.lengthSquared();
                if(secLength > 3){
                    continue; //literally no way we could need to draw this, so don't waste time in cycle
                }

                currentMesh = highMesh; //Mesh mesh = isOutOfSector? lowMesh : highMesh; //once the lowres mesh actually works

                GasPlanetInformation planet = entry.getValue();
                Vector3f drawPosition = relSectorPos.toVector3f();
                drawPosition.scale(sectorSize);

                Vector3f camPos = getCamera().getOffsetPos(new Vector3f());
                trueOffset.set(drawPosition);
                trueOffset.sub(camPos);

                scale = planet.getRadius();
                //float bulge = scale * currentPlanet.rotationRate * EQUATOR_BULGE_FACTOR; //irl this probably isn't linear but whatever
                //Vector3f modelScale = new Vector3f(scale + bulge, scale - bulge, scale + bulge); //are these the right axes? TODO: no, they aren't. need to rotate the bulge vector to the actual planetary axis

                //trans.basis.rotX((FastMath.PI * 2) * year);
                tr.origin.set(drawPosition);

                float nearHorizonSize = NEAR_FORCED_PERSPECTIVE_HORIZON*scale;
                if(trueOffset.lengthSquared() < ((scale + ((nearHorizonSize) + 50))*(scale + (nearHorizonSize) + 50))) {
                    //draw inner atmo bubble
                    depth = Math.min(1.0f,Math.max(0.0f, 1.0f - Math.min(1.0f, ((Math.min(1.0f, (trueOffset.length() / (scale + nearHorizonSize + 50)))) - FULL_INNER_OPAQUE_HORIZON) / (1 - FULL_INNER_OPAQUE_HORIZON)))); //By Sea and Sky that is a *mouthful*! I should likely make some variables but w/e

                    modelScale.set(scale, scale, scale);
                    modelScale.scale(MODEL_SCALE_FACTOR);
                    modelScale.scale(ATMOSPHERE_SIZE_FACTOR_INNER);

                    GlUtil.glPushMatrix();
                    GlUtil.glMultMatrix(tr);

                    currentMesh.loadVBO(true);

                    //GL11.glDepthRange(0.9999998807907104D, 1.0D);
                    GlUtil.glEnable(GL_BLEND);
                    //GlUtil.glDisable(GL_DEPTH_TEST); //not sure why...
                    GlUtil.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                    GlUtil.glDepthMask(false);
                    //glClearColor(0,0,0,0);

                    innerAtmosphereShader.setShaderInterface(this);
                    innerAtmosphereShader.load();

                    GlUtil.glEnable(GL_CULL_FACE);
                    GL11.glCullFace(GL_BACK);
                    //GL11.glCullFace(GL_FRONT);

                    GlUtil.scaleModelview(-modelScale.x, -modelScale.y, -modelScale.z);
                    GlUtil.rotateModelview(90f + currentPlanet.getAxialTiltX(), 1f, 0f, 0f);
                    GlUtil.rotateModelview(currentPlanet.getAxialTiltY(), 0f, 1f, 0f);
                    GlUtil.rotateModelview(time * currentPlanet.getRotationRate() * GasPlanetInformation.BASE_ROTATION_RATE, 0f, 0f, 1f);

                    //GL11.glCullFace(GL_BACK);

                    currentMesh.renderVBO();
                    innerAtmosphereShader.unload();
                    GlUtil.glEnable(GL_DEPTH_TEST);
                    GlUtil.glDisable(GL_BLEND);
                    GlUtil.glEnable(GL_CULL_FACE);
                    GlUtil.glPopMatrix();
                    GL11.glDepthRange(0.0D, 1.0D);
                    GlUtil.glDepthMask(true);
                    currentMesh.unloadVBO(true);
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

