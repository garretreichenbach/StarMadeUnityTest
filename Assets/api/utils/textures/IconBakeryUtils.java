package api.utils.textures;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;
import org.schema.schine.graphicsengine.core.FrameBufferObjects;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

/**
 * Created by Jake on 10/13/2021.
 * TODO Unfinished
 */
public class IconBakeryUtils {

    public static BufferedImage writeFBOToBufferedImage(int width, int height, int bpp, final FrameBufferObjects fbo) {

        GlUtil.printGlErrorCritical();

        ByteBuffer buffer = GlUtil.getDynamicByteBuffer(width * height * bpp, 0);

        int aa = (Integer) EngineSettings.G_MULTI_SAMPLE.getInt();
        if (fbo == null) {
            assert(false):"Must provide 1024x1024 frame buffer, or else stuff is getting cut off";
            System.err.println("FBO "+fbo+"; aa "+aa+"; "+ GL11.glGetInteger(GL21.GL_PIXEL_PACK_BUFFER_BINDING)+"; "+GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING)+"; ");
            GlUtil.printGlErrorCritical();
            GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
            GlUtil.printGlErrorCritical();
        } else {
            GlUtil.printGlErrorCritical();
            GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
            GlUtil.printGlErrorCritical();
        }

        GlUtil.printGlErrorCritical();
        // The file to save to.
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int i = (x + (width * y)) * bpp;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                int a = buffer.get(i + 3) & 0xFF;

                image.setRGB(x, height - (y + 1), (a << 24) | (r << 16) | (g << 8) | b);
            }
        }

        GlUtil.printGlErrorCritical();
        return image;
    }
}
