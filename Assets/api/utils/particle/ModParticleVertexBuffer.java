package api.utils.particle;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Quat4Util;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.psys.modules.RendererModule;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.Collection;

/**
 * Original Schine class
 */
public class ModParticleVertexBuffer {
    //(3floatPos, 2FloatTex, 3FloatNormal, 4FloatColor)
    public static final int vertexDataSize = (3 + 2 + 3 + 4);
    private static final Vector2f[] squareVertices = new Vector2f[]{
            new Vector2f(-0.5f, -0.5f),
            new Vector2f(0.5f, -0.5f),
            new Vector2f(0.5f, 0.5f),
            new Vector2f(-0.5f, 0.5f)
    };
    public static void postRegisterParticles() {
        ModParticleUtil.postRegisterParticles();
    }
    static int maxParticleCount = 10240;
    protected static FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer((maxParticleCount * vertexDataSize) * 4);
    static int currentVBOId;
    private static boolean initialized;
    Vector3f cameraRight_worldspace = new Vector3f();
    Vector3f cameraBack_worldspace = new Vector3f();
    Vector3f cameraUp_worldspace = new Vector3f();
    private Vector4f colorTmp = new Vector4f();
    private Vector3f posTmp = new Vector3f();
    Vector3f resultTmp = new Vector3f();
    float wack = 0;
    protected int addQuadNormalBillboard(ModParticle particle, RendererModule.FrustumCullingMethod frustumCulling) {
        float[] verts = new float[squareVertices.length * 3];

        for (int i = 0; i < squareVertices.length; i++) {

//            float sizeX = squareVertices[i].x * BillboardSize.x * -particle.sizeX;
//            float sizeY = squareVertices[i].y * BillboardSize.y * -particle.sizeY;
            float sizeX = squareVertices[i].x * -particle.sizeX;
            float sizeY = squareVertices[i].y * -particle.sizeY;
            Vector3f result = Quat4Util.mult(particle.rotation, new Vector3f(sizeX, sizeY, 0.0F), resultTmp);
            float vertX;
            float vertY;
            float vertZ;
            if(particle.normalOverride != null){
                // Calculate 2 vectors to use as direction

                Vector3f upDir = new Vector3f();
                particle.normalOverride.getColumn(1, upDir);

                Vector3f rightDir = new Vector3f();
                particle.normalOverride.getColumn(2, rightDir);

                 vertX =
                        particle.position.x
                                + rightDir.x * result.x
                                + upDir.x * result.y;
                 vertY =
                        particle.position.y
                                + rightDir.y * result.x
                                + upDir.y * result.y;
                 vertZ =
                        particle.position.z
                                + rightDir.z * result.x
                                + upDir.z * result.y;
            }else{
                 vertX =
                        particle.position.x
                                + cameraRight_worldspace.x * result.x
                                + cameraUp_worldspace.x * result.y;
                 vertY =
                        particle.position.y
                                + cameraRight_worldspace.y * result.x
                                + cameraUp_worldspace.y * result.y;
                 vertZ =
                        particle.position.z
                                + cameraRight_worldspace.z * result.x
                                + cameraUp_worldspace.z * result.y;
            }

            verts[i * 3] = vertX;
            verts[i * 3 + 1] = vertY;
            verts[i * 3 + 2] = vertZ;
        }

        switch (frustumCulling) {
            case NONE:
                break;
            case SINGLE:
                if (!GlUtil.isPointInView(particle.position, Controller.vis.getVisLen())) {
                    return 0;
                }
                break;
            case ACCURATE:
                boolean anyVisible = false;
                for (int i = 0; i < squareVertices.length; i++) {
                    posTmp.set(verts[i * 3], verts[i * 3 + 1], verts[i * 3 + 2]);
                    if (GlUtil.isPointInView(posTmp, Controller.vis.getVisLen())) {
                        anyVisible = true;
                        break;
                    }
                }
                if (!anyVisible) {
                    return 0;
                }
                break;
        }
        Vector2f[] texCoords = ModParticleUtil.pointMap.get(particle.particleSpriteId);
        Vector3f normalTmp = new Vector3f();
        for (int i = 0; i < squareVertices.length; i++) {

            vertexBuffer.put(verts[i * 3]);
            vertexBuffer.put(verts[i * 3 + 1]);
            vertexBuffer.put(verts[i * 3 + 2]);

//            vertexBuffer.put(squareTexCoords[i].x);
//            vertexBuffer.put(squareTexCoords[i].y);
            vertexBuffer.put(texCoords[i].x);
            vertexBuffer.put(texCoords[i].y);

//            vertexBuffer.put(cameraBack_worldspace.x);
//            vertexBuffer.put(cameraBack_worldspace.y);
//            vertexBuffer.put(cameraBack_worldspace.z + (wack+=0.01F));
            if(particle.normalOverride != null){
                particle.normalOverride.getColumn(2, normalTmp);
            }else{
                normalTmp.set(cameraBack_worldspace);
            }
            vertexBuffer.put(normalTmp.x);
            vertexBuffer.put(normalTmp.x);
            vertexBuffer.put(normalTmp.x);

            vertexBuffer.put(particle.colorR/127F);
            vertexBuffer.put(particle.colorG/127F);
            vertexBuffer.put(particle.colorB/127F);
            vertexBuffer.put(particle.colorA/127F);

        }

        return squareVertices.length; //4
    }

    public void draw(Collection<ModParticle> particle, RendererModule.FrustumCullingMethod frustumCulling) {
        if (!initialized) {
            init();
        }

        int vertexCount = updateBillboards(particle, frustumCulling);
        drawVBO(vertexCount);
    }

    public void init() {

        currentVBOId = GL15.glGenBuffers();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, currentVBOId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STREAM_DRAW);
        initialized = true;
    }

    private int updateBillboards(Collection<ModParticle> particles, RendererModule.FrustumCullingMethod frustumCulling) {
        wack = 0;
        int particleCount = particles.size();
        if (particleCount > maxParticleCount) {
            maxParticleCount = particleCount;
            vertexBuffer = BufferUtils.createFloatBuffer((maxParticleCount * vertexDataSize) * 4);
            init();
        }
        ((Buffer) vertexBuffer).rewind();
        ((Buffer) vertexBuffer).limit(vertexBuffer.capacity());
        cameraRight_worldspace.set(Controller.modelviewMatrix.m00, Controller.modelviewMatrix.m10, Controller.modelviewMatrix.m20);
        cameraUp_worldspace.set(Controller.modelviewMatrix.m01, Controller.modelviewMatrix.m11, Controller.modelviewMatrix.m21);
        cameraBack_worldspace.set(-Controller.modelviewMatrix.m02, -Controller.modelviewMatrix.m12, -Controller.modelviewMatrix.m22);

        int vertexCount = 0;
        for (ModParticle particle : particles) {
            vertexCount += addQuadNormalBillboard(particle, frustumCulling);
        }

        if (vertexCount == 0) {
            return 0;
        }
        ((Buffer) vertexBuffer).flip();

        GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, currentVBOId);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, vertexBuffer);

        return vertexCount;
    }

    private void drawVBO(int vertexCount) {


        GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GlUtil.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GlUtil.glEnableClientState(GL11.GL_NORMAL_ARRAY);
        GlUtil.glEnableClientState(GL11.GL_COLOR_ARRAY);
        GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, currentVBOId);

        GL11.glVertexPointer(3, GL11.GL_FLOAT, vertexDataSize * ByteUtil.SIZEOF_FLOAT, 0);
        GL11.glTexCoordPointer(2, GL11.GL_FLOAT, vertexDataSize * ByteUtil.SIZEOF_FLOAT, 3 * ByteUtil.SIZEOF_FLOAT);
        GL11.glNormalPointer(GL11.GL_FLOAT, vertexDataSize * ByteUtil.SIZEOF_FLOAT, (3 + 2) * ByteUtil.SIZEOF_FLOAT);
        GL11.glColorPointer(4, GL11.GL_FLOAT, vertexDataSize * ByteUtil.SIZEOF_FLOAT, (3 + 2 + 3) * ByteUtil.SIZEOF_FLOAT);

        GL11.glDrawArrays(GL11.GL_QUADS, 0, vertexCount);

        GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        GlUtil.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GlUtil.glDisableClientState(GL11.GL_NORMAL_ARRAY);
        GlUtil.glDisableClientState(GL11.GL_COLOR_ARRAY);


    }
}
