package api.utils.draw.debugging;

import api.utils.draw.ModWorldDrawer;
import com.bulletphysics.util.ObjectArrayList;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.schine.graphicsengine.core.Timer;

/**
 * Class used for drawing debug stuff.
 *
 * @author TheDerpGamer
 */
public class DebugDrawer extends ModWorldDrawer {

	public static boolean doDraw = true;
	private static final ObjectArrayList<DebugDrawerData> drawMap = new ObjectArrayList<>();

	@Override
	public void onInit() {

	}

	@Override
	public void draw() {
		for(DebugDrawerData drawerData : drawMap) drawerData.draw();
	}

	@Override
	public void update(Timer timer) {

	}

	@Override
	public void cleanUp() {
		for(DebugDrawerData drawerData : drawMap) drawerData.cleanUp();
	}

	@Override
	public boolean isInvisible() {
		return !doDraw;
	}

	public static void addDraw(DebugDrawerData drawerData) {
		drawMap.add(drawerData);
	}

	public static void removeDraw(ElementCollection<?, ?, ?> elementCollection) {
		ObjectArrayList<DebugDrawerData> toRemove = new ObjectArrayList<>();
		for(DebugDrawerData drawerData : drawMap) {
			if(drawerData instanceof SoundDrawerData soundDrawerData) {
				if(soundDrawerData.getSegmentPiece().equals(elementCollection.getElementCollectionId())) toRemove.add(drawerData);
			}
		}
		for(DebugDrawerData drawerData : toRemove) drawMap.remove(drawerData);
	}
}
