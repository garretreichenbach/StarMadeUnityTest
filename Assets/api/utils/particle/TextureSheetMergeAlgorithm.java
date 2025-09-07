package api.utils.particle;

import javax.vecmath.Vector2f;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Created by Jake on 12/31/2020.
 * Packs multiple images into a singular texture sheet
 * <p>
 * The algorithm:
 * <p>
 * Set image height to the greatest
 * <p>
 * Sort images by width
 * <p>
 * pointer = [x, y] = [0, 0]
 * <p>
 * For each texture:
 * Attempt to put texture below pointer
 * If out of bounds in y:
 * shift pointer x by last image width
 * Set pointer y to zero
 * Blit texture to main image
 * Shift Y down by image width
 * <p>
 * Works very fast, however does not give an optimal packing ratio.
 */
public class TextureSheetMergeAlgorithm {
    /**
     * Merges a texture id map (List of BufferedImages), into a single image and fills pointMap with texture points
     * PointMap Vector4f[] format (Each pair is a coord on the texture):
     * {
     * [Left, Up]
     * [Right, Up]
     * [Left, Down]
     * [Right, Down]
     * }
     * Points are all percentage, ie 0.5 = halfway. Origin is top left.
     */
    static BufferedImage mergeTextures(ArrayList<BufferedImage> baseImages, ArrayList<Vector2f[]> pointMap) {
        //Copy images into new array, so we do not modify the old one.
        //Done because the position of the texture in the array is its id.
        ArrayList<BufferedImage> images = new ArrayList<>(baseImages);

        int maxHeight = getMaxHeight(images);
        int pointerX = 0;
        int pointerY = 0;
        int totalMax = getTotalMaxWidth(images);
        BufferedImage mainImage = new BufferedImage(totalMax, maxHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = mainImage.getGraphics();
        Collections.sort(images, (o1, o2) -> Integer.compare(o2.getWidth(), o1.getWidth()));
        //Draw images of similar size downward, then shift to the right whenever a width change occurs
        HashMap<BufferedImage, Point2i> renderInfo = new HashMap<>();
        BufferedImage previousImage = images.get(0);
        for (BufferedImage image : images) {
            int imgHeight = image.getHeight();
            if (previousImage.getWidth() != image.getWidth()) {
                pointerX += previousImage.getWidth();
                pointerY = 0;
            }
            previousImage = image;
            if (pointerY + imgHeight > maxHeight) {
                pointerX += image.getWidth();
                pointerY = 0;
            }
            graphics.drawImage(image, pointerX, pointerY, null);
            renderInfo.put(image, new Point2i(pointerX, pointerY));
            pointerY += imgHeight;
        }
        graphics.dispose();

        //Resize image to remove leftover pixels
        int usedMaxWidth = pointerX + previousImage.getWidth();
        mainImage = mainImage.getSubimage(0, 0, usedMaxWidth, mainImage.getHeight());

        //Round to power of 2 (OpenGL texture coords need a power of 2 texture to work properly)
        mainImage = roundToPower2Image(mainImage);

        //Flip vertically since OpenGL uses bottom left as origin
//        AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
//        tx.translate(0, -mainImage.getHeight(null));
//        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
//        mainImage = op.filter(mainImage, null);

        double width = mainImage.getWidth();
        double height = mainImage.getHeight();
        for (BufferedImage baseImage : baseImages) {
            Point2i renderPoint = renderInfo.get(baseImage);
            // Offset all textures by 0.5F for accuracy [https://jvm-gaming.org/t/texture-bleeding/46089/3]
            float x = (float) (((float) renderPoint.x + 0.5F) / width);
            float y = (float) (((float) renderPoint.y + 0.5F) / height);
            float w = (float) (((float) (renderPoint.x  + 0.5F + baseImage.getWidth())) / width);
            float h = (float) (((float) (renderPoint.y  + 0.5F + baseImage.getHeight())) / height);
            Vector2f[] pointData = new Vector2f[]{
                    new Vector2f(x, y),
                    new Vector2f(w, y),
                    new Vector2f(w, h),
                    new Vector2f(x, h)
            };
            pointMap.add(pointData);
        }


        return mainImage;
    }

    /**
     * Makes width and height of an image a power of 2
     */
    private static BufferedImage roundToPower2Image(BufferedImage image) {
        int newWidth = roundToNearest2(image.getWidth());
        int newHeight = roundToNearest2(image.getHeight());
        BufferedImage n = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        n.getGraphics().drawImage(image,0,0,null);
        return n;
    }

    private static int roundToNearest2(int i) {
        int power = 1;
        while (power < i) {
            power *= 2;
        }
        return power;
    }

    public static void main(String[] args) {
        BufferedImage red = createImage(64, 64, Color.red);
        BufferedImage green = createImage(32, 32, Color.green);
        BufferedImage blue = createImage(8, 8, Color.blue);
        BufferedImage cyan = createImage(4, 4, Color.cyan);
        BufferedImage orange = createImage(128, 128, Color.orange);
        ArrayList<BufferedImage> images = new ArrayList<>();
        images.add(red);
        images.add(green);
        images.add(blue);
        images.add(cyan);
        images.add(orange);
        ArrayList<Vector2f[]> pointMap = new ArrayList<>();
        BufferedImage img = mergeTextures(images, pointMap);
        // Breakpoint line below
        System.err.println(img);
    }

    private static BufferedImage createImage(int w, int h, Color c) {
        BufferedImage blue = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = blue.getGraphics();
        graphics.setColor(c);
        graphics.fillRect(0, 0, w, h);
        graphics.setColor(Color.black);
        graphics.drawRect(0, 0, w - 1, h - 1);
        graphics.dispose();
        return blue;
    }

    private static int getMaxHeight(ArrayList<BufferedImage> images) {
        int max = 0;
        for (BufferedImage image : images) {
            int height = image.getHeight();
            if (height > max) {
                max = height;
            }
        }
        return max;
    }

    private static int getTotalMaxWidth(ArrayList<BufferedImage> images) {
        int max = 0;
        for (BufferedImage image : images) {
            max += image.getWidth();
        }
        return max;
    }

    /**
     * Created by Jake on 1/3/2021.
     * Fast (not) random number generators for particles.
     *
     * More of a fun experiment, To be replaced by SplittableRandom or some other fast random in java8+
     */
    private static int r = 0;
    public static int randInt(int magnitude){
        return (r++ % (magnitude*2)) - magnitude;
    }
    public static float randFloat(float magnitude){
        return floats[r++%length] * magnitude;
    }
    public static float nextGaussian(){
        return gaussian[r++%length];
    }

    private static final float[] floats;
    private static final float[] gaussian;
    private static final int length = 500;
    static {
        Random tempRand = new Random(System.currentTimeMillis());
        floats = new float[length];
        gaussian = new float[length];
        for (int i = 0; i < length; i++) {
            //Random float between [-1, 1]
            floats[i] = 1-(tempRand.nextFloat()*2);
            gaussian[i] = (float) tempRand.nextGaussian();
        }
    }
}
