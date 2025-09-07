package api.listener.fastevents;

import org.lwjgl.opengl.GL11;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.gamemap.GameMapDrawer;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.PositionableSubColorSprite;
import org.schema.schine.graphicsengine.forms.Sprite;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

/**
 * Created by Jake on 11/12/2020.
 * <insert description here>
 */
public interface GameMapDrawListener {
    /**
     * Called when the selected system is about to be drawn
     */
    void system_PreDraw(GameMapDrawer drawer, Vector3i system, boolean explored);

    /**
     * Called when the selected system is done drawing
     */
    void system_PostDraw(GameMapDrawer drawer, Vector3i system, boolean explored);

    /**
     * Called when the map is about to be drawn
     */
    void galaxy_PreDraw(GameMapDrawer drawer);

    /**
     * Called when the map is done drawing.
     */
    void galaxy_PostDraw(GameMapDrawer drawer);

    /**
     * Called when FTL Lines are draw, OpenGL is in the GL_LINES state
     */
    void galaxy_DrawLines(GameMapDrawer drawer);

    /**
     * Called when OpenGL is in a state to draw sprites
     */
    void galaxy_DrawSprites(GameMapDrawer drawer);


    /**
     * Called when OpenGL is in a state to draw quads
     */
    void galaxy_DrawQuads(GameMapDrawer drawer);

    class DrawUtils {

        /**
         * Draws a cube
         */
        public static void drawCube(Vector3f pos, float size, Vector4f color) {
//            GlUtil.translateModelview(pos.x, pos.y, pos.z);
            GlUtil.glBegin(GL11.GL_QUADS);

            GlUtil.glColor4f(color.x, color.y, color.z, color.w);

            GL11.glNormal3f(0, 0, 1);
            GL11.glVertex3f(size, size, -size);
            GL11.glVertex3f(size, -size, -size);
            GL11.glVertex3f(-size, -size, -size);
            GL11.glVertex3f(-size, size, -size);

            GL11.glNormal3f(0, 0, -1);
            GL11.glVertex3f(size, -size, size);
            GL11.glVertex3f(size, size, size);
            GL11.glVertex3f(-size, size, size);
            GL11.glVertex3f(-size, -size, size);

            GL11.glNormal3f(-1, 0, 0);
            GL11.glVertex3f(size, -size, -size);
            GL11.glVertex3f(size, size, -size);
            GL11.glVertex3f(size, size, size);
            GL11.glVertex3f(size, -size, size);

            GL11.glNormal3f(1, 0, 0);
            GL11.glVertex3f(-size, -size, size);
            GL11.glVertex3f(-size, size, size);
            GL11.glVertex3f(-size, size, -size);
            GL11.glVertex3f(-size, -size, -size);

            GL11.glNormal3f(0, -1, 0);
            GL11.glVertex3f(size, size, size);
            GL11.glVertex3f(size, size, -size);
            GL11.glVertex3f(-size, size, -size);
            GL11.glVertex3f(-size, size, size);

            GL11.glNormal3f(0, 1, 0);
            GL11.glVertex3f(size, -size, -size);
            GL11.glVertex3f(size, -size, size);
            GL11.glVertex3f(-size, -size, size);
            GL11.glVertex3f(-size, -size, -size);
            GlUtil.glEnd();

            GlUtil.glColor4f(1, 1, 1, 1);
        }

        /**
         * Tints a system by drawing a cube over it
         */
        public static void tintSystem(Vector3i system, Vector4f color) {
            Vector3f systemPos = new Vector3f(system.x-1, system.y-1, system.z-1);
            systemPos.scale(GameMapDrawer.sectorSize*16F);
            //Center in the middle of the system
            drawCube(systemPos, GameMapDrawer.halfsize-0.1F,  color);
        }

        /**
         * Draws a sub-sprite to the screen
         */
        public static void drawSprite(GameMapDrawer drawer, Sprite sprite, PositionableSubColorSprite[] entries) {
            GlUtil.glPushMatrix();

            GlUtil.glDisable(GL11.GL_LIGHTING);
            GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
            GlUtil.glEnable(GL11.GL_BLEND);
            GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            sprite.setBillboard(true);
            sprite.setBlend(true);
//            sprite.setFlip(true);

            drawer.getCamera().updateFrustum();
            Sprite.draw3D(sprite, entries, drawer.getCamera());

            sprite.setBillboard(false);
            sprite.setFlip(false);

            GlUtil.glDisable(GL11.GL_TEXTURE_2D);
            GlUtil.glEnable(GL11.GL_LIGHTING);
            GlUtil.glEnable(GL11.GL_CULL_FACE);
            GlUtil.glEnable(GL11.GL_BLEND);
            GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
            GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlUtil.glPopMatrix();
        }

        /**
         * Draws a line when GL is in GL_LINES state
         */
        public static void drawFTLLine(Vector3f start, Vector3f end, Vector4f startColor, Vector4f endColor) {
            //GL_LINES
            GL11.glColor4f(startColor.x, startColor.y, startColor.z, startColor.w);
            GL11.glVertex3f(start.x, start.y, start.z);
            GL11.glColor4f(endColor.x, endColor.y, endColor.z, endColor.w);
            GL11.glVertex3f(end.x, end.y, end.z);
        }

        /**
         * Draws a line when out of GL State
         */

        public static void drawLine(Vector3f start, Vector3f end, Vector4f startColor, Vector4f endColor, float thickness) {
            //GL_LINES
            GL11.glLineWidth(thickness);
            GlUtil.glBegin(GL11.GL_LINES);

            drawFTLLine(start, end, startColor, endColor);

            GlUtil.glEnd();
            GL11.glLineWidth(1);
        }
    }
}
