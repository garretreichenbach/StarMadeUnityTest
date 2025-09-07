package org.schema.game.client.view.gui.shiphud;

import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;
import org.schema.common.FastMath;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.common.util.linAlg.TransformTools;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.controller.manager.ingame.ship.ShipExternalFlightController;
import org.schema.game.client.controller.tutorial.TutorialMode;
import org.schema.game.client.controller.tutorial.states.TutorialMarker;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.client.data.gamemap.GameMap;
import org.schema.game.client.data.gamemap.entry.MapEntryInterface;
import org.schema.game.client.view.effects.Indication;
import org.schema.game.client.view.gamemap.GameMapDrawer;
import org.schema.game.client.view.gamemap.StarPosition;
import org.schema.game.client.view.gui.shiphud.newhud.IndicatorIndices;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.SlotAssignment;
import org.schema.game.common.controller.activities.Race;
import org.schema.game.common.controller.activities.Race.RaceState;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.rail.pickup.RailPickupUnit;
import org.schema.game.common.controller.trade.TradeNodeClient.TradeNodeMapIndication;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.creature.AICreature;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.fleet.FleetMember.FleetMemberMapIndication;
import org.schema.game.common.data.player.*;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.game.common.data.world.*;
import org.schema.game.common.data.world.SectorInformation.SectorType;
import org.schema.game.server.data.Galaxy;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.camera.viewer.FixedViewer;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.core.settings.SectorIndicationMode;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.Transformable;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.font.unicode.Color;
import org.schema.schine.graphicsengine.forms.gui.ColoredInterface;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.graphicsengine.util.WorldToScreenConverter;
import org.schema.schine.graphicsengine.util.timer.SinusTimerUtil;
import org.schema.schine.graphicsengine.util.timer.TimerUtil;
import org.schema.schine.input.InputState;
import org.schema.schine.network.objects.Sendable;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class HudIndicatorOverlay extends GUIElement {

	public static List<Indication> toDrawTexts = new ObjectArrayList<Indication>();
	public static List<SavedCoordinate> toDrawCustomWaypoints = new ObjectArrayList<>();
	public static List<Indication> toDrawMapTexts = new ObjectArrayList<Indication>();
	public static ObjectOpenHashSet<GameMap> toDrawMapInterfaces = new ObjectOpenHashSet<GameMap>();
	public static ObjectOpenHashSet<StarPosition> toDrawStars = new ObjectOpenHashSet<StarPosition>();
	public static ObjectOpenHashSet<FleetMemberMapIndication> toDrawFleet = new ObjectOpenHashSet<FleetMemberMapIndication>();
	public static ObjectOpenHashSet<TradeNodeMapIndication> toDrawTradeNodes = new ObjectOpenHashSet<TradeNodeMapIndication>();
	public static boolean DRAW_ALL_INFO = false;
	public static float selectColorValue;
	private static ObjectOpenHashSet<Vector3f> toDrawTextsSet = new ObjectOpenHashSet<Vector3f>();
	private static FloatBuffer modelview = MemoryUtil.memAllocFloat(16);
	private static FloatBuffer modelviewTmp = MemoryUtil.memAllocFloat(16);
	//INSERTED CODE
	public boolean drawSectorIndicators = true;
	public boolean drawWaypoints = true;
	public final String[] neighborSectorsNames;
	public final Transform[] neighborSectors;
	public Vector3i[] neighborSectorsPos;
	//
	private final ObjectArrayFIFOQueue<IndicationText> texts = new ObjectArrayFIFOQueue<IndicationText>();
	private final Vector3f pTmp = new Vector3f();
	private final Vector3f middleTmp = new Vector3f();
	private final Vector3f defaultTextOnScrenIndication = new Vector3f();
	Vector3f dir = new Vector3f();
	Vector3f cross = new Vector3f();
	Vector4f tint = new Vector4f(0, 1, 0, 1);
	Vector3f lastDir = new Vector3f();
	Transform localSec = new Transform();
	private GUIOverlay indicator;
	private GUIOverlay targetOverlay;
	private GUITextOverlay textOverlay;
	private Int2IntArrayMap animap = new Int2IntArrayMap();
	private WorldToScreenConverter worldToScreenConverter;
	private long lastTimeUpdate;
	private boolean updateAnim;
	private Transformable cameraTransformable;
	private TimerUtil timerUtil;
	private SimpleTransformableSendableObject selectedEntity;
	private int screenCap;

	//	private void calcNeighborSectors(Vector3i sector){
	//		int size = Universe.getSectorSizeWithMargin();
	//
	//		Vector3i toSec = new Vector3i();
	//		Vector3f toSecPos = new Vector3f();
	//		int i = 0;
	//		for(int x = -1;x < 2; x++){
	//			for(int y = -1;y < 2; y++){
	//				for(int z = -1;z < 2; z++){
	//					if(x != 0 || y != 0 || z != 0){ //dont't do 0,0,0 -> 9 + 8 + 9 = 26
	//						toSec.set(sector);
	//						toSec.add(x, y, z);
	//						neighborSectorsNames[i] = "Sector "+toSec;
	//
	//						toSecPos.set(x * size, y * size, z * size);
	//
	//						neighborSectors[i].origin.set(toSecPos);
	//
	//						i++;
	//					}
	//				}
	//			}
	//		}
	//	}
	private Vector3i tmpSysPos = new Vector3i();
	private Transform transR = new Transform();
	private Transform neighborT = new Transform();
	private TransformaleObjectTmpVars v = new TransformaleObjectTmpVars();
	private GUIOverlay arrow;
	private ObjectArrayFIFOQueue<IndicationText> pool = new ObjectArrayFIFOQueue<IndicationText>();
	private boolean drawDockingAreas;
	Transform posTmp = new Transform();
	private GUIOverlay leadIndicator;

	//INSERTED CODE
	//

	public HudIndicatorOverlay(InputState state) {
		super(state);
		posTmp.setIdentity();
		Sprite indicatorSprite = Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath()+"hud-target-c-4x4-gui-");
		indicator = new GUIOverlay(indicatorSprite, state);
		targetOverlay = new GUIOverlay(Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath()+"hud-target-c-4x4-gui-"), state);

		this.arrow = new GUIOverlay(Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath()+"hud_pointers-c-8x8"), state);
		
		this.leadIndicator = new GUIOverlay(Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath()+"hud_pointers-c-8x8"), state);
		this.leadIndicator.setSpriteSubIndex(33);

		textOverlay = new GUITextOverlay(state);

		timerUtil = new SinusTimerUtil(8);

		worldToScreenConverter = ((GameClientState) getState()).getScene().getWorldToScreenConverter();

		neighborSectorsPos = new Vector3i[6];
		neighborSectors = new Transform[6];
		neighborSectorsNames = new String[6];

		for (int i = 0; i < neighborSectors.length; i++) {
			neighborSectorsPos[i] = new Vector3i();
			neighborSectors[i] = new Transform();
			neighborSectors[i].setIdentity();
		}
		calcNeighborSectors(((GameClientState) state).getInitialSectorPos());
	}

	public static void getColor(SimpleTransformableSendableObject to, Vector4f tint, boolean selectVal, GameClientState state) {

		if (selectVal) {
			tint.x = 0.3f + selectColorValue;
			tint.y = 0.3f + selectColorValue;
			tint.z = 0.3f + selectColorValue;
			return;
		}
		RType relation = null;
		List<PlayerState> attachedPlayers = null;
		float select = selectVal ? selectColorValue : 0;

		if (to instanceof PlayerCharacter) {
			PlayerCharacter p = (PlayerCharacter) to;
			attachedPlayers = p.getAttachedPlayers();
			if (!attachedPlayers.isEmpty()) {
				relation = attachedPlayers.get(0).getRelation(state.getPlayer());
			}
		}

		if (to instanceof PlayerControllable) {
			PlayerControllable p = (PlayerControllable) to;
			attachedPlayers = p.getAttachedPlayers();
			if (!attachedPlayers.isEmpty()) {
				relation = attachedPlayers.get(0).getRelation(state.getPlayer());
			}
		}
		if (relation == null) {
			relation = state.getPlayer().getRelation(to.getFactionId());
		}
		to.getRelationColor(relation, state.getPlayer().getFactionId() != 0 && state.getPlayer().getFactionId() == to.getFactionId(), tint, select, selectColorValue);

		if (to instanceof SegmentController && ((SegmentController) to).railController.isDockedAndExecuted()) {
			if (state.getCurrentPlayerObject() != null && ((SegmentController) to).railController.previous.rail.getSegmentController()
					== state.getCurrentPlayerObject() && !state.getGlobalGameControlManager().getIngameControlManager().isInBuildMode()) {
				tint.set(0, 0, 0, 0);
			} else {
				float d = 0.2f;
				tint.x = Math.max(tint.x - d, 0.01f);
				tint.y = Math.max(tint.y - d, 0.01f);
				tint.z = Math.max(tint.z + d, 0.01f);
			}
		}

		if (to instanceof SegmentController && ((SegmentController) to).getDockingController().isDocked()) {
			if (state.getCurrentPlayerObject() != null && ((SegmentController) to).getDockingController().getDockedOn().to.getSegment().getSegmentController()
					== state.getCurrentPlayerObject() && !state.getGlobalGameControlManager().getIngameControlManager().isInBuildMode()) {
				tint.set(0, 0, 0, 0);
			} else {
				float d = 0.2f;
				tint.x = Math.max(tint.x - d, 0.01f);
				tint.y = Math.max(tint.y - d, 0.01f);
				tint.z = Math.max(tint.z + d, 0.01f);
			}
		}
	}
	public static float getRotation(SimpleTransformableSendableObject to, boolean selectVal, GameClientState state) {
		if (to instanceof SegmentController && ((SegmentController) to).railController.isDockedAndExecuted()) {
			return FastMath.HALF_PI*0.5f;
		}
		return 0;
	}
	public static float getScale(SimpleTransformableSendableObject to, boolean selectVal, GameClientState state) {
		
		if (to instanceof SegmentController && ((SegmentController) to).railController.isDockedAndExecuted()) {
			return 0.5f;
		}
		return 1.0f;
		
	}

	private void calcNeighborSectors(Vector3i sector) {
		int size = (int) ((GameClientState) getState()).getSectorSize();

		Vector3i toSec = new Vector3i();
		Vector3f toSecPos = new Vector3f();
		toSec.set(sector);

		for (int i = 0; i < 6; i++) {

			neighborSectorsPos[i].set(sector);
			toSec.set(sector);
			Vector3i d = Element.DIRECTIONSi[i];
			toSec.add(d);
			neighborSectorsPos[i].set(toSec);
			toSecPos.set(d.x * size, d.y * size, d.z * size);
			try {
				neighborSectorsNames[i] = Lng.str("Sector %s", toSec.toString());
			} catch(Exception ignored) {
				neighborSectorsNames[i] = "Sector " + toSec.toString();
			}
			neighborSectors[i].origin.set(toSecPos);
		}

	}

	private boolean calcSecPos(int index, Transform out) {
		Vector3i absSec = neighborSectorsPos[index];
		calcWaypointSecPos(absSec, out);
		if (((GameClientState) getState()).getController().getClientGameData().getNearestToWayPoint() != null) {
			return absSec.equals(((GameClientState) getState()).getController().getClientGameData().getNearestToWayPoint());
		}
		return false;
	}
	private final Vector3f otherSecCenterTmp = new Vector3f();
	private final Matrix3f rotTmp = new Matrix3f();
	private final Vector3i pPosTmp = new Vector3i();
	private Vector3f bbTmp = new Vector3f();
	private Transform tmpTrsns = new Transform();
	private SimpleTransformableSendableObject selectedAIEntity;
	private long lastExp;
	private void calcWaypointSecPos(Vector3i absSec, Transform out) {

		Vector3i sysPos = StellarSystem.getPosFromSector(absSec, tmpSysPos);

		pPosTmp.set(absSec);
		GameClientState state = ((GameClientState) getState());
		pPosTmp.sub(state.getPlayer().getCurrentSector());

		out.setIdentity();
		float year = state.getGameState().getRotationProgession();

		otherSecCenterTmp.set(
				pPosTmp.x * state.getSectorSize(),
				pPosTmp.y * state.getSectorSize(),
				pPosTmp.z * state.getSectorSize());

		rotTmp.rotX((FastMath.PI * 2) * year);

		Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(state.getCurrentSectorId());
		if (sendable != null && sendable instanceof RemoteSector) {
			RemoteSector sec = (RemoteSector) sendable;

			if (sec.getType() == SectorType.PLANET) {
				//we are next to a planet sector
				//-> rotate planet sector
				rotTmp.invert();
				Vector3f bb = new Vector3f();
				bb.add(otherSecCenterTmp);
				TransformTools.rotateAroundPoint(bbTmp, rotTmp, out, new Transform());
				out.origin.add(otherSecCenterTmp);

				return;
			}
		}
		out.origin.set(otherSecCenterTmp);

	}
	
	private void drawStarMarker() {
		GameClientState state = ((GameClientState) getState());
		if (state.getCurrentClientSystem() != null) {
			Galaxy galaxy = state.getCurrentGalaxy();
			VoidSystem system = state.getCurrentClientSystem();
			
			Vector3i currentSystem = system.getPos();
			Vector3i currentSector = state.getPlayer().getCurrentSector();
			Vector3i starPos = VoidSystem.getSunSectorPosAbs(galaxy, currentSystem, new Vector3i());			
			
			float intensity = galaxy.getSystemSunIntensity(currentSystem);
			float sunDistance = galaxy.getSunDistance(currentSector);
			float heatRange = state.getGameState().sunMinIntensityDamageRange;
			
			SectorType systemType = galaxy.getSystemTypeAt(state.getPlayer().getCurrentSystem());
			if (systemType == SectorType.SUN && 
				(system.isHeatDamage(currentSector, intensity, sunDistance, heatRange) 	|| system.getDistanceIntensity(intensity, sunDistance) < heatRange * 2)){
				
				Transform t = new Transform();
				t.setIdentity();
				calcWaypointSecPos(starPos, t);
				
				tint.set(0.25f + selectColorValue, 0.0f, 0.0f, selectColorValue);
				textOverlay.setColor(0.25f + selectColorValue, 0.0f, 0.0f, 0.25f + selectColorValue);
				drawFor(t, Lng.str("WARNING HEAT"), -50, true, true);
				textOverlay.setColor(Color.white);
				
			}			
		}	
	}
	
	public float cap(float in, int min, int max) {
		return Math.min(max, Math.max(min, in));
	}

	@Override
	public void cleanUp() {

	}
	@Override
	public void draw() {
		if (lastTimeUpdate + 50 < System.currentTimeMillis()) {
			updateAnim = true;
			lastTimeUpdate = System.currentTimeMillis();
		} else {
			updateAnim = false;
		}

		RemoteSector currentRemoteSector = ((GameClientState) getState()).getCurrentRemoteSector();
		if(currentRemoteSector != null && currentRemoteSector.isNoIndicationsClient()) return;

		this.drawDockingAreas = false;
		if(((GameClientState)getState()).getShip() != null){
			Ship ship = ((GameClientState)getState()).getShip();
			SlotAssignment shipConfiguration = ship.getSlotAssignment();
	
			Vector3i absPos = shipConfiguration.get(((GameClientState)getState()).getPlayer()
					.getCurrentShipControllerSlot());
			if (absPos != null) {
				SegmentPiece pointUnsave = ship.getSegmentBuffer()
						.getPointUnsave(absPos);
	
				if (pointUnsave != null ) {
					if(pointUnsave.getType() == ElementKeyMap.RAIL_BLOCK_DOCKER){
						this.drawDockingAreas = true;
					}
				}
			}
		}
		PlayerInteractionControlManager playerIntercationManager = ((GameClientState) getState()).
				getGlobalGameControlManager().
				getIngameControlManager().
				getPlayerGameControlManager().
				getPlayerIntercationManager();

		selectedEntity = playerIntercationManager.getSelectedEntity();
		selectedAIEntity = playerIntercationManager.getSelectedAITarget();

		cameraTransformable = null;
		if (Controller.getCamera().getViewable() instanceof FixedViewer) {
			FixedViewer v = (FixedViewer) Controller.getCamera().getViewable();
			cameraTransformable = v.getEntity();
		}

		TutorialMode tutorialMode = ((GameClientState) getState()).getController().getTutorialMode();
		if (tutorialMode != null) {

			if (tutorialMode.markers != null) {
				Transform t = new Transform();
				t.setIdentity();
				for (int i = 0; i < tutorialMode.markers.size(); i++) {

					TutorialMarker tutorialMarker = tutorialMode.markers.get(i);

					if (tutorialMarker.absolute != null) {
						t.origin.set(tutorialMarker.absolute);
					} else if (tutorialMarker.context != null) {
						tutorialMarker.context.getAbsoluteElementWorldPositionShifted(tutorialMarker.where, t.origin);
					} else if (tutorialMode.currentContext != null) {
						tutorialMode.currentContext.getAbsoluteElementWorldPositionShifted(tutorialMarker.where, t.origin);
					} else {
						t.origin.set(tutorialMarker.where.x, tutorialMarker.where.y, tutorialMarker.where.z);
					}
					tint.set(0.0f + selectColorValue, 1f - selectColorValue, selectColorValue, 0.99f);
					drawFor(t, tutorialMarker.markerText, -1000 - i, true, true);
				}

				return;
			}

		}
		
		PlayerState player = ((GameClientState) getState()).getPlayer();
		if (player != null && !player.isInTutorial()
				&& !player.isInPersonalSector() && !player.isInTestSector()) {
			if (EngineSettings.SECTOR_INDICATION_MODE.getObject() != SectorIndicationMode.OFF && drawSectorIndicators) {
				for (int i = 0; i < neighborSectors.length; i++) {
					assert (neighborSectorsNames[i] != null);

					neighborT.set(neighborSectors[i]);

					boolean sel = calcSecPos(i, neighborT);
					//			if(sel){
					//				tint.set(0.1f+selectColorValue, 0.8f+selectColorValue, 0.6f+selectColorValue, 0.4f+selectColorValue);
					//			}else{
					tint.set(0.4f, 0.4f, 0.4f, 0.4f);
					//			}
					drawFor(neighborT, neighborSectorsNames[i], -100 - i, EngineSettings.SECTOR_INDICATION_MODE.getObject() == SectorIndicationMode.INDICATION_AND_ARROW, true);
				}
			}
		}

		Vector3i wayPoint = ((GameClientState) getState()).getController().getClientGameData().getWaypoint();
		//INSERTED CODE [Added drawWayPoints to if statement
		if (wayPoint != null & drawWaypoints) {
		///
			tint.set(0.1f + selectColorValue, 0.8f + selectColorValue, 0.6f + selectColorValue, 0.4f + selectColorValue);
			Transform t = new Transform();
			t.setIdentity();
			calcWaypointSecPos(new Vector3i(wayPoint), t);
			drawFor(t, Lng.str("Waypoint", wayPoint.toString()), -300, true, true);

		}
		drawStarMarker();
		if(((GameClientState)getState()).getRaceManager().isInRunningRace(((GameClientState)getState()).getPlayer())) {
			Race race = ((GameClientState)getState()).getRaceManager().getRace(((GameClientState)getState()).getPlayer());
			RaceState rs = race.getRaceState(((GameClientState)getState()).getPlayer());
			if(!race.isFinished()){
				if(rs.isActive()){
					tint.set(0.9f + selectColorValue, 0.3f + selectColorValue, 0.1f + selectColorValue, 1.0f);
					drawFor(rs.totalDistance, "[Race] Gate #" + (rs.currentGate+1)+"/"+(race.getTotalGates()-1), -300, true, true);
					
					
					long raceTime = System.currentTimeMillis() - race.raceStart;
//					System.err.println("KDKKK "+raceTime+" :: "+race.raceStart);
					String raceTimeFormatted = StringTools.formatRaceTime(raceTime);
					((GameClientState)getState()).getController().showBigTitleMessage("RACE_RANK", Lng.str("Rank %s\n%s", rs.currentRank,  raceTimeFormatted), 0);
				}else{
					if(rs.getFinishedTime() > 0 && System.currentTimeMillis() - rs.getFinishedTime() > 30000){
						long raceTime = rs.getFinishedTime() - race.raceStart;
						String raceTimeFormatted = StringTools.formatRaceTime(raceTime);
						((GameClientState)getState()).getController().showBigTitleMessage("RACE_RANK", Lng.str("Rank %s\n (FINISHED)%s", rs.currentRank,  raceTimeFormatted), 0);
					}
				}
			}
		}

		GlUtil.glPushMatrix();
		for (SimpleTransformableSendableObject s : ((GameClientState) getState()).getCurrentSectorEntities().values()) {
			
			if (s != cameraTransformable) {
				SimpleTransformableSendableObject to = s;
				if(to instanceof ManagedSegmentController<?> && drawDockingAreas){
					
					ManagerContainer<?> managerContainer = ((ManagedSegmentController<?>)to).getManagerContainer();
					if(!(managerContainer.getSegmentController() instanceof Ship) || 
							!((Ship)managerContainer.getSegmentController()).isJammingFor(((GameClientState)getState()).getCurrentPlayerObject())){
						GlUtil.glPushMatrix();
						tint.set(0.89f, 0.64f, 0.77f, 0.7f);
						List<RailPickupUnit> elem = managerContainer.getRailPickup().getCollectionManager().getElementCollections();
						if(!elem.isEmpty()){
							RailPickupUnit railPickupUnit = elem.get(0);
							LongArrayList neighboringCollection = railPickupUnit.getNeighboringCollection();
							final int size = neighboringCollection.size();
							for(int i = 0; i < size; i++){
								long l = neighboringCollection.getLong(i);
								if(railPickupUnit.isActive(l)){
									ElementCollection.getPosFromIndex(l, posTmp.origin);
									
									posTmp.origin.x -= SegmentData.SEG_HALF;
									posTmp.origin.y -= SegmentData.SEG_HALF;
									posTmp.origin.z -= SegmentData.SEG_HALF;
									to.getWorldTransformOnClient().transform(posTmp.origin);
									String dec;
									if(to == selectedEntity){
										dec =  to.getRealName()+Lng.str("<PickupArea>");
									}else{
										dec = "";
									}
									
									drawFor(posTmp, dec, -2000-to.getId()-(i * 100000), false, to == selectedEntity || to == selectedAIEntity);
								}
								
							}
						}
						GlUtil.glPopMatrix();
					}
				}
			}
		}
		GlUtil.glPopMatrix();
		
		
		
		GlUtil.glPushMatrix();
		Sprite.startDraw(arrow.getSprite());
		for (SimpleTransformableSendableObject s : ((GameClientState) getState()).getCurrentSectorEntities().values()) {

			if (s != cameraTransformable) {
				drawFor(s);
			}
		}
		Sprite.endDraw(arrow.getSprite());
		GlUtil.glPopMatrix();

		while (!texts.isEmpty()) {
			IndicationText text = texts.dequeue();
			textOverlay.getPos().set(text.v);
			textOverlay.getText().set(0, text.s0);
			textOverlay.getText().set(1, text.s1);
			textOverlay.draw();
			pool.enqueue(text);
		}
		try {
//			System.err.println("DAW: "+toDrawTexts.size());
			toDrawTextsSet.clear();
			for (int i = 0; i < toDrawTexts.size(); i++) {
//				System.err.println("DRAW::: "+toDrawTexts.get(i).getText()+"; "+toDrawTexts.get(i).getDist());
				drawString(toDrawTexts.get(i), Controller.getCamera(), true, toDrawTexts.get(i).getDist(), worldToScreenConverter);
			}

		} catch (IndexOutOfBoundsException e) {
			//nevermind: we can ignore that one frame
			long ut = ((GameClientState)getState()).getUpdateTime();
			if(lastExp - ut > 20000) {
				System.err.println("[CLIENT][HUD] OOB INDEX");
				lastExp = ut;
			}
		}

	}

	@Override
	public void onInit() {
		indicator.onInit();

		textOverlay = new GUITextOverlay(FontSize.TINY_12, getState());
		textOverlay.setColor(Color.white);
		textOverlay.setText(new ArrayList());
		textOverlay.getText().add("");
		textOverlay.getText().add("");
		textOverlay.setPos(UIScale.getUIScale().smallinset, UIScale.getUIScale().smallinset, 0);

		textOverlay.onInit();

	}

	@Override
	protected void doOrientation() {

	}

	@Override
	public float getHeight() {
		return GLFrame.getHeight();
	}

	@Override
	public float getWidth() {
		return GLFrame.getWidth();
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}
	private final Vector3f dirTmp = new Vector3f();
	private float currentDelta;
	public void drawFor(SimpleTransformableSendableObject to) {
		
		
		
		
		GameClientState state = (GameClientState) getState();
		Transformable from = Controller.getCamera();
		if (from == null) {
			return;
		}
		if (to instanceof Ship) {
			if (((Ship) to).isJammingFor(((GameClientState)getState()).getCurrentPlayerObject())) {
				return;
			}
		}
		if (((GameClientState) getState()).getPlayer() != null && ((GameClientState) getState()).getPlayer().isInTutorial() && !(to instanceof AICreature<?>)) {
			//only do creature indications in tutorial
			return;
		}
//		if(to instanceof AbstractCharacter<?> ){
//			System.err.println("TOOO: "+to+" ;;; "+((AbstractCharacter<?>)to).getAttachedPlayers()+"; IN CORE: "+NavigationControllerManager.isPlayerInCore(to)+"; "+((PlayerState)to.getOwnerState()).getControllerState().getUnits());
//		}
		if (to instanceof AbstractCharacter<?> && !((AbstractCharacter<?>) to).getAttachedPlayers().isEmpty()) {
			if (((AbstractCharacter<?>) to).getAttachedPlayers().get(0).isInvisibilityMode()) {
				return;
			}
		}

		if (!state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getNavigationControlManager().isDisplayed(to)) {
			return;
		}
		boolean isAquireringTarget = getExternalShipMan().isTreeActive() && getExternalShipMan().getAquire().isTargetMode() && getExternalShipMan().getAquire().getTarget() == to;

		

		//		if(to.toString().contains("BasisTurret1_1378583946368")){
		//			System.err.println("4DJHDKJDH: "+to+": "+(camToPosLen > INDICATORS_MAX_DISTANCE)+"; "+camToPosLen +" / "+ INDICATORS_MAX_DISTANCE+": "+to.isHidden());
		//		}
		RType relation = state.getPlayer().getRelation(to.getFactionId());
		//		boolean deathStarIndication = (to instanceof TeamDeathStar && team != null && !(team instanceof NeutralTeam));
		//		boolean ownDeathStarIndication = deathStarIndication && ((TeamDeathStar)to).getTeam() == team;
	
		//		if(to.toString().contains("BasisTurret1_1378583946368")){
		//			System.err.println("5DJHDKJDH: "+to);
		//		}
		//		System.err.println("SELECTED KSDJHFKSHKJFHSKJHF: "+selectedEntity);

		//		if(to.toString().contains("BasisTurret1_1378583946368")){
		//			System.err.println("DDKJDHDJKH "+to);
		//		}
		//
		getColor(to, tint, to == selectedEntity, state);
		if(to == selectedAIEntity) {
			tint.set(0.71f, 0.32f, 0.53f, 1.0f);
		}
		float scale = getScale(to, to == selectedEntity, state);
		float rot = getRotation(to, to == selectedEntity, state);
		if (tint.lengthSquared() == 0) {
			return;
		}
		//		if(to.toString().contains("BasisTurret1_1378583946368")){
		//			System.err.println("6DJHDKJDH: "+to);
		//		}
		//		if(to.toString().contains("BasisTurret1_1378583946368")){
		//			System.err.println("DDKJDHDJKH "+to+" "+tint+"; "+to.getWorldTransformClient().origin);
		//		}

		
		Vector3f middle = worldToScreenConverter.getMiddleOfScreen(middleTmp);
		
		PlayerCharacter character = ((GameClientState)getState()).getCharacter();
		
		Vector3f w;
		if(character != null && character.getGravity() != null && character.getGravity().source != null && (character.getGravity().source == to || (to instanceof SegmentController && ((SegmentController)to).railController.isInAnyRailRelationWith(character.getGravity().source)))) {
			//set indicator to core if we are inside or aligned to a cube structure
			w = to.getWorldTransformOnClient().origin;
		}else {
			w = to.getWorldTransformOnClientCenterOfMass(tmpTrsns).origin;
		}
		
		dir.set(w);

		if (state.getCurrentPlayerObject() != null) {
			dir.sub(state.getCurrentPlayerObject().getWorldTransform().origin);
		} else {
			dir.sub(Controller.getCamera().getPos());
		}
		float camToPosLen = dir.length();
		if (!isAquireringTarget && (to != selectedEntity && to != selectedAIEntity && camToPosLen > to.getIndicatorMaxDistance(relation)) || camToPosLen < Element.BLOCK_SIZE || to.isHidden()) {
			return;
		}
		
		if (isAquireringTarget) {
			tint.set(1, 1, 0.2f, 1);
		}
		if (isAquireringTarget || to == selectedEntity || to == selectedAIEntity) {
			tint.w = 1;
		} else {
			tint.w = Math.min(1, to.getIndicatorMaxDistance(relation) / (camToPosLen * 10));
		}

		boolean lead = false;
		if(to == selectedEntity && to instanceof SegmentController && (((GameClientState)getState()).getPlayer() != null && ((GameClientState)getState()).getPlayer().getFirstControlledTransformableWOExc() instanceof ManagedUsableSegmentController<?>)) {
			ManagedUsableSegmentController<?> m = (ManagedUsableSegmentController<?>)((GameClientState)getState()).getPlayer().getFirstControlledTransformableWOExc();
			float weaponRange = 0;
			float weaponSpeed = 0;
			for (ControllerStateUnit u : ((GameClientState)getState()).getPlayer().getControllerState().getUnits()) {
				if (u.playerControllable == m) {
					weaponRange = m.getManagerContainer().getSelectedWeaponRange(u);
					weaponSpeed = m.getManagerContainer().getSelectedWeaponSpeed(u);
					break;
				}
			}
			
			
			
			SegmentController c = (SegmentController)to;
			dirTmp.sub(c.getPhysicsDataContainer().getCurrentPhysicsTransform().origin, c.getPhysicsDataContainer().lastTransform.origin);
			
			
			if(c.railController.getRoot() instanceof Ship && c.railController.getRoot().getPhysicsDataContainer().getObject() instanceof RigidBody && weaponSpeed > 0) {
				RigidBody b = (RigidBody) c.railController.getRoot().getPhysicsDataContainer().getObject();
				Vector3f velo = b.getLinearVelocity(new Vector3f());
				Vector3f targetAngularVelocity = new Vector3f();
				if(c.getPhysicsObject() != null) c.getPhysicsObject().getAngularVelocity(targetAngularVelocity);
				if(velo.lengthSquared() > 0) {
	//				float fps = 1f / currentDelta;
	//				dirTmp.scale(fps);
					Vector3f tt = new Vector3f();
					tt = Vector3fTools.predictPoint(w, velo, targetAngularVelocity, weaponSpeed, Controller.getCamera().getPos());

					tt.add(Controller.getCamera().getPos());
					dir.set(tt);

					if (state.getCurrentPlayerObject() != null) {
						dir.sub(state.getCurrentPlayerObject().getWorldTransform().origin);
					} else {
						dir.sub(Controller.getCamera().getPos());
					}
					camToPosLen = dir.length();

					tint.x += 0.5f;
					tint.y -= 0.3f;
					tint.z -= 0.2f;

					putOnScreen(tt, to, to.getId()-11111111, middle, rot, scale, camToPosLen, false, true);

					lead = true;
				}
			}
		}
		if(lead) {
			tint.x -= 0.2f;
			tint.y -= 0.3f;
			tint.z += 0.4f;
			tint.w *= 0.5f;
		}
		putOnScreen(w, to, to.getId(), middle, rot, scale, camToPosLen, true, false);
		
		

	}

	private void putOnScreen(Vector3f w, SimpleTransformableSendableObject to, int id, Vector3f middle, float rot, float scale, float camToPosLen, boolean withText, boolean lead) {
		Vector3f posOnScreen = worldToScreenConverter.convert(w, pTmp, true);
		Vector3f posOnScreenCapped = new Vector3f(posOnScreen);
		posOnScreenCapped.sub(middle);

		if (posOnScreenCapped.length() > screenCap) {
			posOnScreenCapped.normalize();
			posOnScreenCapped.scale(screenCap);
			posOnScreenCapped.add(middle);

			arrow.getSprite().setTint(tint);
			int index = IndicatorIndices.getCIndex(to);
			arrow.setSpriteSubIndex(index);
			arrow.getSprite().setSelectedMultiSprite(index);
			arrow.getPos().set((int) posOnScreenCapped.x, (int) posOnScreenCapped.y, (int) posOnScreenCapped.z);

			Vector3f p = new Vector3f(arrow.getPos());
			p.sub(middle);
			p.scale(-1);
			p.normalize();
			Vector3f upLocal = new Vector3f(0, 1, 0);
			float a = Vector3fTools.getFullRange2DAngleFast(upLocal, p);
			//			indicator.setRot(0, 0, FastMath.RAD_TO_DEG * a);

			modelview.rewind();
			Matrix4fTools.store(Controller.modelviewMatrix, modelview);

			//			GlUtil.glPushMatrix();

			float sinAngle, cosAngle;

			sinAngle = FastMath.sinFast(a);
			cosAngle = FastMath.cosFast(a);

			modelview.put(0, cosAngle);
			modelview.put(4, -sinAngle);
			modelview.put(8, 0f);

			modelview.put(1, sinAngle);
			modelview.put(5, cosAngle);
			modelview.put(9, 0);
			modelview.put(2, 0);
			modelview.put(6, 0);
			modelview.put(10, 1);

			modelview.put(12, (int) (Controller.modelviewMatrix.m30 + posOnScreenCapped.x));
			modelview.put(13, (int) (Controller.modelviewMatrix.m31 + posOnScreenCapped.y));
			modelview.put(14, (int) (Controller.modelviewMatrix.m32 + posOnScreenCapped.z));

			modelview.rewind();
			//dont use GlUtil or the internal modelview matrix
			//gets overwritten, which we want to reuse to be stored
			//into the FloatBuffer "modelview"
			GL11.glLoadMatrixf(modelview);
			arrow.getSprite().setScale(scale, scale, scale);
			Sprite.doDraw(arrow.getSprite());
			arrow.getSprite().getTransform().basis.setIdentity();
			if (withText && (DRAW_ALL_INFO || to == selectedEntity)) {
				textOverlay.getPos().set(posOnScreenCapped);
				textOverlay.getText().set(0, to.toNiceString());
				textOverlay.getText().set(1, (int) camToPosLen + "m");
				//				textOverlay.draw();
			}else if(withText && to == selectedAIEntity) {
				textOverlay.getPos().set(posOnScreenCapped);
				textOverlay.getText().set(0, to.toNiceString());
				textOverlay.getText().set(1, Lng.str("[AI TARGET]"));
			}

			if (((GameClientState) getState()).isInWarp() && animap.containsKey(id)) {
				//do not reset when in warp
			} else {
				animap.put(id, 1);
			}
		} else {
			
			arrow.getSprite().setTint(tint);
			posOnScreenCapped.add(middle);

			if (!animap.containsKey(id)) {
				//					System.err.println("RESETTING IN FRESH BECAUSE OF LOOSING: "+to+": "+((RemoteSector)state.getLocalAndRemoteObjectContainer().getLocalObjects().get(to.getSectorId())).clientPos());

				animap.put(id, 1);
			}
			if(lead) {
				arrow.setSpriteSubIndex(25);
				arrow.getSprite().setSelectedMultiSprite(25);
			}else {
				int frame = animap.get(id);
				arrow.setSpriteSubIndex(frame + 8);
				arrow.getSprite().setSelectedMultiSprite(frame + 8);
				if (updateAnim) {
					if (frame < 15) {
						animap.put(id, Math.min(15, frame + 1));
					}
				}
			}
			

			//			GlUtil.glPushMatrix();
			//			GlUtil.translateModelview(posOnScreenCapped);

			modelview.rewind();
			Matrix4fTools.store(Controller.modelviewMatrix, modelview);

			float sinAngle, cosAngle;

			sinAngle = FastMath.sinFast(rot);
			cosAngle = FastMath.cosFast(rot);

			modelview.put(0, cosAngle);
			modelview.put(4, -sinAngle);
			modelview.put(8, 0f);

			modelview.put(1, sinAngle);
			modelview.put(5, cosAngle);
			modelview.put(9, 0);
			modelview.put(2, 0);
			modelview.put(6, 0);
			modelview.put(10, 1);
			
			//			GlUtil.glPushMatrix();
			modelview.put(12, Controller.modelviewMatrix.m30 + posOnScreenCapped.x);
			modelview.put(13, Controller.modelviewMatrix.m31 + posOnScreenCapped.y);
			modelview.put(14, Controller.modelviewMatrix.m32 + posOnScreenCapped.z);
			modelview.rewind();
			//dont use GlUtil or the internal modelview matrix
			//gets overwritten, which we want to reuse to be stored
			//into the FloatBuffer "modelview"
			GL11.glLoadMatrixf(modelview);

			arrow.getSprite().setScale(scale, scale, scale);
			Sprite.doDraw(arrow.getSprite());
			//			GlUtil.glPopMatrix();

			arrow.getPos().set((int) posOnScreenCapped.x, (int) posOnScreenCapped.y, (int) posOnScreenCapped.z);

			//			targetOverlay.draw();
			if (withText && (DRAW_ALL_INFO || to == selectedEntity)) {
				//				textOverlay.getPos().set(posOnScreenCapped);
				//				textOverlay.getText().set(0, to.toNiceString());
				//				textOverlay.getText().set(1, (int)camToPosLen+"m");
				//				textOverlay.draw();
				IndicationText indicationText;
				if (pool.isEmpty()) {
					indicationText = new IndicationText(to.toNiceString(), (int) camToPosLen + "m", posOnScreenCapped);
				} else {
					indicationText = pool.dequeue();
					indicationText.s0 = to.toNiceString();
					indicationText.s1 = (int) camToPosLen + "m";
					indicationText.v.set(posOnScreenCapped);
					if (selectedEntity.getSectorId() != ((GameClientState)getState()).getPlayer().getCurrentSectorId() &&
							selectedEntity.getWorldTransform().origin.equals(new Vector3f(0, 0, 0))) {
						// If the entity is covering the sector label, offset slightly.
						indicationText.v.add(new Vector3f(0.0F, 25.0F, 0.0F));
					}
				}
				texts.enqueue(indicationText);
			}else if(withText && to == selectedAIEntity) {
				
				IndicationText indicationText;
				if (pool.isEmpty()) {
					indicationText = new IndicationText(to.toNiceString(), (int) camToPosLen + "m", posOnScreenCapped);
				} else {
					indicationText = pool.dequeue();
					indicationText.s0 = to.toNiceString();
					indicationText.s1 = Lng.str("[AI TARGET]");
					indicationText.v.set(posOnScreenCapped);
					if (selectedAIEntity.getSectorId() != ((GameClientState)getState()).getPlayer().getCurrentSectorId() &&
							selectedAIEntity.getWorldTransform().origin.equals(new Vector3f(0, 0, 0))) {
						// If the entity is covering the sector label, offset slightly.
						indicationText.v.add(new Vector3f(0.0F, 25.0F, 0.0F));
					}
				}
				texts.enqueue(indicationText);
				
			}

			if (withText && to instanceof SegmentController && ((SegmentController) to).getCoreTimerStarted() > 0) {

				long l = ((SegmentController) to).getCoreTimerDuration() - (System.currentTimeMillis() - ((SegmentController) to).getCoreTimerStarted());
				String sec = Lng.str("sec");
				texts.enqueue(new IndicationText(Lng.str("OVERHEATING\n%s %s\nReactor needs manual reboot!",  l / 1000, sec) , "", posOnScreenCapped));
			}
		}		
	}

	public void drawFor(Transform to, String desc, int animId, boolean useArrow, boolean displayDistanceOnNear) {
		Transformable from = Controller.getCamera();
		if (from == null) {
			return;
		}
		dir.set(to.origin);
		dir.sub(Controller.getCamera().getPos());

		float camToPosLen = dir.length();
		Vector3f middle = worldToScreenConverter.getMiddleOfScreen(new Vector3f());
		Vector3f posOnScreen = worldToScreenConverter.convert(to.origin, new Vector3f(), true);
		Vector3f posOnScreenCapped = new Vector3f(posOnScreen);
		posOnScreenCapped.sub(middle);

		indicator.getSprite().setTint(tint);
		targetOverlay.getSprite().setTint(tint);
		arrow.getSprite().setTint(tint);
		if (useArrow && posOnScreenCapped.length() > screenCap) {
			posOnScreenCapped.normalize();
			posOnScreenCapped.scale(screenCap);
			posOnScreenCapped.add(middle);

			arrow.setSpriteSubIndex(0);
			arrow.getPos().set((int) posOnScreenCapped.x, (int) posOnScreenCapped.y, (int) posOnScreenCapped.z);

			Vector3f p = new Vector3f(arrow.getPos());
			p.sub(middle);
			p.scale(-1);
			p.normalize();
			Vector3f upLocal = new Vector3f(0, 1, 0);
			float a = Vector3fTools.getFullRange2DAngleFast(upLocal, p);
			arrow.setRot(0, 0, FastMath.RAD_TO_DEG * a);
			arrow.draw();
			if (((GameClientState) getState()).isInWarp() && animap.containsKey(animId)) {
				//do not reset when in warp
			} else {
				animap.put(animId, 1);
			}

		} else {
			arrow.setRot(0, 0, 0);
			posOnScreenCapped.add(middle);
			if (!animap.containsKey(animId)) {
				animap.put(animId, 1);
			}
			int frame = animap.get(animId);

				arrow.setSpriteSubIndex(frame + 8);
				arrow.getSprite().setSelectedMultiSprite(frame + 8);
			if (updateAnim) {
				animap.put(animId, Math.min(15, frame + 1));
			}
			arrow.getPos().set((int) posOnScreenCapped.x, (int) posOnScreenCapped.y, (int) posOnScreenCapped.z);
			arrow.draw();

			textOverlay.getPos().set((int) posOnScreenCapped.x, (int) posOnScreenCapped.y, (int) posOnScreenCapped.z);
			textOverlay.getText().set(0, desc);
			if(displayDistanceOnNear){
				if (camToPosLen > 1000f) {
					textOverlay.getText().set(1, StringTools.formatPointZero(camToPosLen / 1000f) + "km");
				} else {
					textOverlay.getText().set(1, (int) camToPosLen + "m");
				}
			}else{
				textOverlay.getText().set(1, "");
			}
			textOverlay.draw();
		}

	}

	public void drawMapIndications() {
		if (((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getMapControlManager().isTreeActive()) {
			GameMapDrawer gameMapDrawer = ((GameClientState) getState()).getWorldDrawer().getGameMapDrawer();

			for(SavedCoordinate mapEntryInterface : toDrawCustomWaypoints) {
				if(mapEntryInterface.isDrawIndication() && mapEntryInterface.canDraw()) {
					drawString(mapEntryInterface.getIndication(gameMapDrawer.getGameMapPosition().getCurrentSysPos()), gameMapDrawer.getCamera(), false, 4000, gameMapDrawer.getWorldToScreenConverter());
				}
			}

			for (int i = 0; i < toDrawMapTexts.size(); i++) {
				drawString(toDrawMapTexts.get(i), gameMapDrawer.getCamera(), false, 4000, gameMapDrawer.getWorldToScreenConverter());
			}
			toDrawMapTexts.clear();

			for (GameMap gameMap : toDrawMapInterfaces) {
				if(gameMap == null) continue;
				Vector3i secPos = gameMapDrawer.getGameMapPosition().get(new Vector3i());
				for (int j = 0; j < gameMap.getEntries().length; j++) {
					MapEntryInterface mapEntryInterface = gameMap.getEntries()[j];
					if (
							mapEntryInterface.isDrawIndication() &&
									mapEntryInterface.canDraw() &&
									gameMapDrawer.getGameMapPosition().getCurrentSysPos().equals(gameMap.getPos()) &&
									mapEntryInterface.include(gameMapDrawer.getFilter(), secPos)) {

						drawString(mapEntryInterface.getIndication(gameMap.getPos()), gameMapDrawer.getCamera(), false, 4000,
								gameMapDrawer.getWorldToScreenConverter());
					}
				}

			}
			Vector3f screenPosMod = new Vector3f(0, -20, 0);
			for (StarPosition star : toDrawStars) {
				drawString(star.getIndication(((GameClientState) getState()).getCurrentGalaxy()), gameMapDrawer.getCamera(), false, 4000,
						gameMapDrawer.getWorldToScreenConverter(), screenPosMod);
			}
			toDrawStars.clear();
			
			
			Vector3i systemNull = new Vector3i();
			for (FleetMemberMapIndication mapEntryInterface : toDrawFleet) {
				if (
						mapEntryInterface.isDrawIndication() &&
								mapEntryInterface.canDraw()) {
					drawString(mapEntryInterface.getIndication(systemNull), gameMapDrawer.getCamera(), false, 4000,
							gameMapDrawer.getWorldToScreenConverter());
				}
			}
			toDrawFleet.clear();
			for (TradeNodeMapIndication mapEntryInterface : toDrawTradeNodes) {
				if (
						mapEntryInterface.isDrawIndication() &&
						mapEntryInterface.canDraw()) {
					drawString(mapEntryInterface.getIndication(systemNull), gameMapDrawer.getCamera(), false, 4000,
							gameMapDrawer.getWorldToScreenConverter());
				}
			}
			toDrawTradeNodes.clear();
			
			
		}

	}

	public void drawString(Indication indication, Camera camera, boolean fromPlayer, float maxDist, WorldToScreenConverter worldToScreenConverter) {
		drawString(indication, camera, fromPlayer, maxDist, worldToScreenConverter, defaultTextOnScrenIndication);
	}

	public void drawString(Indication indication, Camera camera, boolean fromPlayer, float maxDist, WorldToScreenConverter worldToScreenConverter, Vector3f screenPositionMod) {
		GameClientState state = (GameClientState) getState();
		dir.set(indication.getCurrentTransform().origin);

		if (fromPlayer && state.getCurrentPlayerObject() != null) {
			if (state.getCurrentPlayerObject() instanceof SegmentController) {
				dir.sub(camera.getPos());
			} else {
				dir.sub(state.getCurrentPlayerObject().getWorldTransform().origin);
			}

		} else {
			dir.sub(camera.getPos());
		}
		float camToPosLen = dir.length();
		if (camToPosLen > maxDist) {
			return;
		}
		Vector3f middle = worldToScreenConverter.getMiddleOfScreen(new Vector3f());

		Vector3f posOnScreen = worldToScreenConverter.convert(indication.getCurrentTransform().origin, new Vector3f(), true, camera);
//				System.err.println("DRAW INDICATION "+indication.getText()+": "+indication.getCurrentTransform().origin+" -> "+posOnScreen);
		Vector3f posOnScreenCapped = new Vector3f(posOnScreen);
		posOnScreenCapped.sub(middle);

		targetOverlay.getSprite().setTint(tint);
		posOnScreenCapped.add(middle);
		posOnScreenCapped.add(screenPositionMod);

		textOverlay.getPos().set((int) posOnScreenCapped.x, (int) posOnScreenCapped.y, (int) posOnScreenCapped.z);

		textOverlay.getText().set(0, indication.getText());
		textOverlay.getText().set(1, "");

		textOverlay.setScale(indication.scaleIndication(), indication.scaleIndication(), indication.scaleIndication());

		if (indication.getText() instanceof ColoredInterface) {
			textOverlay.setColor(new Vector4f(((ColoredInterface) indication.getText()).getColor()));
		}
		
		textOverlay.draw();
		textOverlay.getColor().r = 1;
		textOverlay.getColor().g = 1;
		textOverlay.getColor().b = 1;
		textOverlay.getColor().a = 1;

		textOverlay.setScale(1, 1, 1);
	}

	public ShipExternalFlightController getExternalShipMan() {
		return ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager()
				.getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getShipExternalFlightController();
	}

	public void onSectorChange() {

		/*
		 * remove all animations that are no longer in scope
		 */
		GameClientState s = ((GameClientState) getState());
		IntIterator iterator = animap.keySet().iterator();
		IntArrayList a = new IntArrayList();
		while (iterator.hasNext()) {
			int animId = iterator.next().intValue();
			if (!s.getCurrentSectorEntities().containsKey(animId)) {
				a.add(animId);
			}
		}
		for (int i : a) {
			animap.remove(i);
		}

		calcNeighborSectors(s.getPlayer().getCurrentSector());
	}

	@Override
	public void update(Timer timer) {
		this.currentDelta = timer.getDelta();
		selectColorValue = 0.5f + timerUtil.getTime() * 0.5f;
		screenCap = (int) Math.min(GLFrame.getHeight() / 2.4f, GLFrame.getWidth() / 2.4f);
		timerUtil.update(timer);
		try {
			for (int i = 0; i < toDrawTexts.size(); i++) {
				toDrawTexts.get(i).update(timer);
				if (!toDrawTexts.get(i).isAlive()) {
//					System.err.println("REMOVED: "+toDrawTexts.get(i).getText());
					toDrawTexts.remove(i);
					i--;
				}
			}
		} catch (IndexOutOfBoundsException e) {
		}
	}

	private class IndicationText {
		public final Vector3f v = new Vector3f();
		public String s0;
		public String s1;

		public IndicationText(String s0, String s1, Vector3f where) {
			super();
			this.s0 = s0;
			this.s1 = s1;
			v.set(where);
		}

	}

}
