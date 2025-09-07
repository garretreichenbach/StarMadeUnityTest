package org.schema.game.common.controller.elements.dockingBlock;

import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.ingame.PlayerGameControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ActivateValueEntry;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.CollectionNotLoadedException;
import org.schema.game.common.controller.DockingController;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.ai.AIConfiguationElements;
import org.schema.game.common.controller.ai.AIGameConfiguration;
import org.schema.game.common.controller.ai.SegmentControllerAIInterface;
import org.schema.game.common.controller.ai.Types;
import org.schema.game.common.controller.elements.BlockMetaDataDummy;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.dockingBlock.fixed.FixedDockingBlockCollectionManager;
import org.schema.game.common.controller.elements.dockingBlock.turret.TurretDockingBlockCollectionManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementDocking;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.ai.stateMachines.AIConfigurationInterface;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.network.objects.remote.RemoteBoolean;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.sound.controller.AudioController;

import com.bulletphysics.linearmath.Transform;

public abstract class DockingBlockCollectionManager<E extends DockingBlockUnit<E, CM, EM>, CM extends DockingBlockCollectionManager<E, CM, EM>, EM extends DockingBlockElementManager<E, CM, EM>> extends ControlBlockElementCollectionManager<E, CM, EM> implements PlayerUsableInterface {

	public static final int defaultDockingHalfSize = 3;

	private static String dockingOnlineMsg = Lng.str("Docking Module online!");

	private static String dockingOfflineMsg = Lng.str("Docking Module offline!\nArea is blocked!");

	private final Vector3i min = new Vector3i();

	private final Vector3i max = new Vector3i();

	public byte orientation = 0;

	protected int enhancers;

	private boolean collision;

	private Vector3i minArea = new Vector3i(0, 0, 0);

	private Vector3i maxArea = new Vector3i(0, 0, 0);

	public DockingBlockCollectionManager(SegmentPiece element, SegmentController segController, short enhancerId, EM em) {
		super(element, enhancerId, segController, em);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.ControlBlockElementCollectionManager#applyMetaData(org.schema.game.common.controller.elements.BlockMetaDataDummy)
	 */
	@Override
	protected void applyMetaData(BlockMetaDataDummy dummy) {
		orientation = ((DockingMetaDataDummy) dummy).orientation;
	}

	@Override
	protected Tag toTagStructurePriv() {
		return new Tag(Type.BYTE, null, orientation);
	}

	public void getDockingArea(Vector3i min, Vector3i max) {
		max.sub(maxArea, minArea);
		max.absolute();
		max.add(defaultDockingHalfSize, defaultDockingHalfSize, defaultDockingHalfSize);
		min.set(max);
		min.negate();
	// min.scale(2);
	// max.scale(2);
	}

	public void getDockingAreaAbsolute(Vector3i min, Vector3i max, boolean withDockingPos) {
		getDockingArea(min, max);
		// if(Element.orientationBackMapping[getControllerElement().getOrientation()] == Element.TOP){
		// System.err.println("DOCKING AREA BEF: "+min+"; "+max);
		// }
		// Vector3f hSize = new Vector3f(Math.abs(min.x - max.x)/2f, Math.abs(min.y - max.y)/2f, Math.abs(min.z - max.z)/2f);
		Vector3i dockPos = new Vector3i();
		if (withDockingPos) {
			dockPos.set(getControllerPos());
		}
		int xSize = max.x - min.x;
		int ySize = max.y - min.y;
		int zSize = max.z - min.z;
		switch(getControllerElement().getOrientation()) {
			case (Element.LEFT) -> {
				min.x = dockPos.x + 1;
				max.x = dockPos.x + (xSize + 1);
				min.y = dockPos.y - ySize / 2;
				max.y = dockPos.y + ySize / 2;
				min.z = dockPos.z - zSize / 2;
				max.z = dockPos.z + zSize / 2;
			}
			case (Element.RIGHT) -> {
				max.x = dockPos.x - 1;
				min.x = dockPos.x - (xSize + 1);
				min.y = dockPos.y - ySize / 2;
				max.y = dockPos.y + ySize / 2;
				min.z = dockPos.z - zSize / 2;
				max.z = dockPos.z + zSize / 2;
			}
			case (Element.TOP) -> {
				min.x = dockPos.x - xSize / 2;
				max.x = dockPos.x + xSize / 2;
				min.y = dockPos.y + 1;
				max.y = dockPos.y + (ySize + 1);
				min.z = dockPos.z - zSize / 2;
				max.z = dockPos.z + zSize / 2;
			}
			case (Element.BOTTOM) -> {
				min.x = dockPos.x - xSize / 2;
				max.x = dockPos.x + xSize / 2;
				max.y = dockPos.y - 1;
				min.y = dockPos.y - (ySize + 1);
				min.z = dockPos.z - zSize / 2;
				max.z = dockPos.z + zSize / 2;
			}
			case (Element.FRONT) -> {
				min.x = dockPos.x - xSize / 2;
				max.x = dockPos.x + xSize / 2;
				min.y = dockPos.y - ySize / 2;
				max.y = dockPos.y + ySize / 2;
				min.z = dockPos.z + 1;
				max.z = dockPos.z + (zSize + 1);
			}
			case (Element.BACK) -> {
				min.x = dockPos.x - xSize / 2;
				max.x = dockPos.x + xSize / 2;
				min.y = dockPos.y - ySize / 2;
				max.y = dockPos.y + ySize / 2;
				max.z = dockPos.z - 1;
				min.z = dockPos.z - (zSize + 1);
			}
		}
	// min.div(2);
	// max.div(2);
	// 
	// min.add(dockPos);
	// max.add(dockPos);
	// if(Element.orientationBackMapping[getControllerElement().getOrientation()] == Element.TOP){
	// System.err.println("DOCKING AREA: "+min+"; "+max);
	// }
	}

	public void getDockingDimensionFor(SegmentController segmentController, byte dockingOrientation, Vector3i min, Vector3i max) {
		Transform t = new Transform();
		t.setIdentity();
		DockingController.getDockingTransformation(dockingOrientation, t);
		Vector3f sfMin = new Vector3f(segmentController.getSegmentBuffer().getBoundingBox().min.x + 1.0f, segmentController.getSegmentBuffer().getBoundingBox().min.y + 1.0f, segmentController.getSegmentBuffer().getBoundingBox().min.z + 1.0f);
		Vector3f sfMax = new Vector3f(segmentController.getSegmentBuffer().getBoundingBox().max.x - 1.0f, segmentController.getSegmentBuffer().getBoundingBox().max.y - 1.0f, segmentController.getSegmentBuffer().getBoundingBox().max.z - 1.0f);
		// System.err.println("BOUNDING BOX::: "+segmentController.getSegmentBuffer().getBoundingBox()+" ---> "+sfMin+"; "+sfMax);
		// #RM1958 avoid java 6 compiler bug: http://bugs.java.com/view_bug.do?bug_id=6932571
		Object o = this;
		if (o instanceof TurretDockingBlockCollectionManager) {
			sfMin.y = 0;
			sfMax.y = ((segmentController.getSegmentBuffer().getBoundingBox().max.y - 1.0f) - (segmentController.getSegmentBuffer().getBoundingBox().min.y + 1.0f));
		}
		t.basis.transform(sfMin);
		t.basis.transform(sfMax);
		min.set(Math.round(Math.min(sfMax.x, sfMin.x)), FastMath.round(Math.min(sfMax.y, sfMin.y)), FastMath.round(Math.min(sfMax.z, sfMin.z)));
		max.set(Math.round(Math.max(sfMax.x, sfMin.x)), FastMath.round(Math.max(sfMax.y, sfMin.y)), FastMath.round(Math.max(sfMax.z, sfMin.z)));
		switch(dockingOrientation) {
			case (Element.LEFT) -> {
				min.add(0, 1, 0);
				max.add(0, 1, 0);
			}
			case (Element.RIGHT) -> {
				min.add(1, 0, 0);
				max.add(1, 0, 0);
			}
			case (Element.TOP) -> {
				min.add(0, 0, 0);
				max.add(0, 0, 0);
			}
			case (Element.BOTTOM) -> {
				min.add(0, 1, 1);
				max.add(0, 1, 1);
			}
			case (Element.FRONT) -> {
				min.add(0, 1, 0);
				max.add(0, 1, 0);
			}
			case (Element.BACK) -> {
				min.add(1, 1, 1);
				max.add(1, 1, 1);
			}
		}
	}

	public abstract void getDockingMoved(Vector3i min, Vector3i max, byte dockingOrientation);

	@Override
	public int getMargin() {
		return 0;
	}

	@Override
	protected void onChangedCollection() {
		int maxEncArea = -1;
		enhancers = 0;
		if (getElementCollections().isEmpty()) {
			minArea.set(0, 0, 0);
			maxArea.set(0, 0, 0);
		} else {
			for (DockingBlockUnit<?, ?, ?> w : getElementCollections()) {
				assert (!w.getNeighboringCollection().isEmpty());
				int area = w.getAbsBBMult();
				assert (area != -1);
				if (area > maxEncArea) {
					w.getMin(this.minArea);
					w.getMax(this.maxArea);
					maxEncArea = area;
				}
				enhancers += w.size();
			}
		}
	// System.err.println(getSegmentController().getState()+"; "+getSegmentController()+" DOCKING AREA RECALCULATED: "+minArea+"; "+maxArea+"; AREA: "+maxEncArea+": CONTROLLER: "+getControllerPos());
	}

	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {
		Vector3i min = new Vector3i();
		Vector3i max = new Vector3i();
		getDockingArea(min, max);
		return new GUIKeyValueEntry[] { new ModuleValueEntry(Lng.str("Docking Area"), min + " - " + max), new ActivateValueEntry(new Object() {

			private boolean check;

			private long lastCheck;

			@Override
			public String toString() {
				if (System.currentTimeMillis() - lastCheck > 1000) {
					check = isDockedHere();
					lastCheck = System.currentTimeMillis();
				}
				return check ? Lng.str("undock") : "-";
			}
		}) {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(882);
					clientUndock();
				}
			}
		} };
	}

	public boolean hasAreaCollision() {
		Vector3i min = new Vector3i();
		Vector3i max = new Vector3i();
		getDockingAreaAbsolute(min, max, true);
		Vector3f tmpMinB = new Vector3f();
		Vector3f tmpMaxB = new Vector3f();
		tmpMinB.set(min.x - SegmentData.SEG_HALF, min.y - SegmentData.SEG_HALF, min.z - SegmentData.SEG_HALF);
		tmpMaxB.set(max.x - SegmentData.SEG_HALF, max.y - SegmentData.SEG_HALF, max.z - SegmentData.SEG_HALF);
		return getSegmentController().getCollisionChecker().existsBlockInAABB(min, max);
	}

	public boolean hasCollision() {
		return collision;
	}

	public boolean isObjectDockable(SegmentController segmentController, byte dockingOrientation, boolean debug) throws CollectionNotLoadedException {
		if (enhancers != getSegmentController().getControlElementMap().getControlledElements(getEnhancerClazz(), getControllerPos()).getControlMap().size()) {
			throw new CollectionNotLoadedException();
		}
		if (!segmentController.getBoundingBox().isInitialized()) {
			System.err.println("Exception Catched (OK): SegmentController tested to dock " + segmentController + " to " + getSegmentController() + ": BB is not yet loaded");
			throw new CollectionNotLoadedException();
		}
		// boolean debug = ((GameStateInterface)getSegmentController().getState()).getGameState().isIgnoreDockingArea();
		getDockingMoved(min, max, dockingOrientation);
		// getDockingAreaAbsolute(min, max, false);
		// compensate for segment bounding box +1
		max.x++;
		max.y++;
		max.z++;
		Vector3i sMax = new Vector3i();
		Vector3i sMin = new Vector3i();
		// if(debug){
		// System.err.println("[DOCK] "+segmentController+" valid check orientation: "+Element.getSideString(dockingOrientation)+": "+segmentController.getBoundingBox());
		// }
		// #RM1958 avoid java 6 compiler bug: http://bugs.java.com/view_bug.do?bug_id=6932571
		Object o = this;
		if (o instanceof TurretDockingBlockCollectionManager) {
			if (segmentController.getBoundingBox().min.y < -1) {
				if (debug) {
					System.err.println("[DOCKING FAILED] TurretBB not bottom " + segmentController.getBoundingBox());
				}
				return false;
			}
		}
		getDockingDimensionFor(segmentController, dockingOrientation, sMin, sMax);
		boolean ok = false;
		// #RM1958 avoid java 6 compiler bug: http://bugs.java.com/view_bug.do?bug_id=6932571
		if (o instanceof FixedDockingBlockCollectionManager) {
			switch(dockingOrientation) {
				case (Element.LEFT) -> ok = sMin.y < min.y || sMin.z < min.z || sMax.y > max.y || sMax.z > max.z || max.x - min.x < sMax.x - sMin.x;
				// if(debug){
				// System.err.println("DOCK DENIED BY RIGHT: "+(max.x- min.x)+" < "+(sMax.x - sMin.x));
				// }
				case (Element.RIGHT) -> ok = sMin.y < min.y || sMin.z < min.z || sMax.y > max.y || sMax.z > max.z || max.x - min.x < sMax.x - sMin.x;
				// if(debug){
				// System.err.println("DOCK DENIED BY RIGHT: "+(max.x- min.x)+" < "+(sMax.x - sMin.x));
				// }
				case (Element.FRONT) -> ok = sMin.x < min.x || sMin.y < min.y || sMax.x > max.x || sMax.y > max.y || max.z - min.z < sMax.z - sMin.z;
				// if(debug){
				// System.err.println("DOCK DENIED BY FRONT: "+(max.z- min.z)+" < "+(sMax.z - sMin.z));
				// }
				case (Element.BACK) -> ok = sMin.x < min.x || sMin.y < min.y || sMax.x > max.x || sMax.y > max.y || max.z - min.z < sMax.z - sMin.z;
				// if(debug){
				// System.err.println("DOCK DENIED BY BACK: "+(max.z- min.z)+" < "+(sMax.z - sMin.z));
				// }
				case (Element.TOP) -> ok = sMin.x < min.x || sMin.z < min.z || sMax.x > max.x || sMax.z > max.z || max.y - min.y < sMax.y - sMin.y;
				// if(debug){
				// System.err.println("DOCK DENIED BY TOP: "+(max.y- min.y)+" < "+(sMax.y - sMin.y));
				// }
				case (Element.BOTTOM) -> ok = sMin.x < min.x || sMin.z < min.z || sMax.x > max.x || sMax.z > max.z || max.y - min.y < sMax.y - sMin.y;
				// if(debug){
				// System.err.println("DOCK DENIED BY BOTTOM: "+(max.y- min.y)+" < "+(sMax.y - sMin.y));
				// }
			}
		} else {
			ok = sMin.x < min.x || sMin.y < min.y || sMin.z < min.z || sMax.x > max.x || sMax.y > max.y || sMax.z > max.z;
		}
		if (ok) {
			if (debug) {
				System.err.println("[DOCKINGBLOCK] !NOT! DOCKABLE: Docking[" + min + "; " + max + "] / Segment[" + sMin + "; " + sMax + "]; enhancers: " + enhancers + " / " + getSegmentController().getControlElementMap().getControlledElements(getEnhancerClazz(), getControllerPos()).getControlMap().size());
			}
			return false;
		}
		if (debug) {
			System.err.println("[DOCKINGBLOCK] IS DOCKABLE: DOCK [" + min + "; " + max + "] / SHIP [" + sMin + "; " + sMax + "]");
		}
		return true;
	}

	public boolean isValidPositionToBuild(Vector3i toBuild) {
		Vector3i min = new Vector3i();
		Vector3i max = new Vector3i();
		getDockingAreaAbsolute(min, max, true);
		System.err.println("[DOCKING] CHECKING TO BUILD POSITION " + toBuild + " ----> " + min + "; " + max);
		return toBuild.x > max.x || toBuild.y > max.y || toBuild.z > max.z || toBuild.x < min.x || toBuild.y < min.y || toBuild.z < min.z;
	}

	public void refreshActive() {
		// System.err.println("[DOCKING] REFRESHING ACTIVE AREA OF "+getControllerElement());
		boolean collisionBefore = collision;
		collision = hasAreaCollision();
		if (!getSegmentController().isOnServer() && collisionBefore != collision) {
			GameClientState s = ((GameClientState) getSegmentController().getState());
			PlayerGameControlManager playerGameControlManager = s.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager();
			if (playerGameControlManager.getPlayerIntercationManager().getSegmentControlManager().isActive() || playerGameControlManager.getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getSegmentBuildController().isActive()) {
				if (collision) {
					s.getController().endPopupMessage(dockingOnlineMsg);
					s.getController().popupAlertTextMessage(dockingOfflineMsg, 0);
				} else {
					s.getController().endPopupMessage(dockingOfflineMsg);
					s.getController().popupInfoTextMessage(dockingOnlineMsg, 0);
				}
			}
		}
	}

	public boolean isDockedHere() {
		for (ElementDocking g : getSegmentController().getDockingController().getDockedOnThis()) {
			if (g.to.equalsPos(getControllerPos())) {
				return true;
			}
		}
		return false;
	}

	public void clientUndock() {
		for (ElementDocking g : getSegmentController().getDockingController().getDockedOnThis()) {
			if (g.to.equalsPos(getControllerPos())) {
				g.from.getSegment().getSegmentController().getNetworkObject().dockClientUndockRequests.add(new RemoteBoolean(false));
			}
		}
	}

	public void clientActivateAI(boolean active) {
		for (ElementDocking g : getSegmentController().getDockingController().getDockedOnThis()) {
			if (g.to.equalsPos(getControllerPos())) {
				SegmentController docked = g.from.getSegment().getSegmentController();
				if (docked instanceof SegmentControllerAIInterface && docked.getElementClassCountMap().get(ElementKeyMap.AI_ELEMENT) >= 1) {
					AIConfigurationInterface aiConfiguration = ((SegmentControllerAIInterface) docked).getAiConfiguration();
					if (aiConfiguration instanceof AIGameConfiguration<?, ?>) {
						AIGameConfiguration<?, ?> c = (AIGameConfiguration<?, ?>) aiConfiguration;
						((AIConfiguationElements<Boolean>) c.get(Types.ACTIVE)).setCurrentState(active, true);
					}
				}
			}
		}
	}
}
