package org.schema.game.common.controller.elements;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.schema.common.FastMath;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.SegNotifyType;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.SlotAssignment;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.config.ConfigEntityManager;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.ShipConfigurationNotFoundException;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.util.FastCopyLongOpenHashSet;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

public abstract class 
	UsableControllableElementManager<E extends ElementCollection<E, CM, EM>, 
	CM extends ControlBlockElementCollectionManager<E, CM, EM>, 
	EM extends UsableControllableElementManager<E, CM, EM>> extends UsableElementManager<E, CM, EM> implements TargetableSystemInterface {

	private static final boolean DEBUG = false;
	
	private final ObjectArrayList<CM> collectionManagers = new ObjectArrayList<CM>();
	private final Long2ObjectOpenHashMap<CM> collectionManagerMap = new Long2ObjectOpenHashMap<CM>();
	public final short controllerId;
	public final short controllingId;
	private long lastSendLimitWarning;
	private boolean integrityChecked;
	private long startCheck;
	private boolean needsDeepCheck;
	private int deepCheckPointer;
	private DeepStructureChecker deepChecker;
	public LongOpenHashSet uniqueConnections;


	/**
	 * if this returns true,
	 * a check will be performed to make sure, a blocked is only
	 * controlled by one, and only one controller
	 * @return true, if check should be performed
	 */
	public boolean isCheckForUniqueConnections(){
		return false;
	}
	public ConfigEntityManager getConfigManager(){
		return getSegmentController().getConfigManager();
	}
	@Override
	public void onElementCollectionsChanged(){
		this.lowestIntegrity = Float.POSITIVE_INFINITY;
		final int size = collectionManagers.size();
		for(int i = 0; i < size; i++){
			this.lowestIntegrity = Math.min(this.lowestIntegrity, collectionManagers.get(i).getLowestIntegrity());
		}
		
	}
	public UsableControllableElementManager(short controller, short controlling, SegmentController segmentController) {
		super(segmentController);
		/*
		 * cache all info
		 */

		this.controllerId = controller;
		this.controllingId = controlling;

		if (controlling != Element.TYPE_ALL && controlling != Element.TYPE_RAIL_INV && controlling != Element.TYPE_RAIL_TRACK && controlling != Element.TYPE_SIGNAL) {
			ElementInformation controllerInfo = ElementKeyMap.getInfo(controller);
			ElementInformation controllingInfo = ElementKeyMap.getInfo(controlling);

			assert (controllerInfo.getControlling().contains(controlling)) : this.getClass() + "; " + ElementKeyMap.toString(controllerInfo.getControlling()) + " : " + ElementKeyMap.toString(controller) + " -> " + ElementKeyMap.toString(controlling);
			assert (controllingInfo.getControlledBy().contains(controller)) : this.getClass() + "; " + controllingInfo.getName() + ": controlled by set " + controllingInfo.getControlledBy() + " does not contain " + ElementKeyMap.getInfo(controller) + "(" + controller + ")";
		}
	}
	@Override
	public boolean canHandle(ControllerStateInterface unit) {
		
		if(getSegmentController().isOnServer() && ((GameServerState)getState()).getGameConfig().hasControllerLimit(controllerId, getCollectionManagers().size()) ){
			if(System.currentTimeMillis() - lastSendLimitWarning > 5000){
				getSegmentController().sendControllingPlayersServerMessage(
						Lng.astr("WARNING!\nWeapon/Tool will not not work on server!\nServer doesn't allow more than %s computers of this type.",  ((GameServerState)getState()).getGameConfig().getControllerLimit(controllerId)),ServerMessage.MESSAGE_TYPE_ERROR);	
				lastSendLimitWarning = System.currentTimeMillis();
			}
			return false;
		}
		
		if(!getSegmentController().checkBlockMassServerLimitOk()){
			if(!getSegmentController().isOnServer() && System.currentTimeMillis() - lastSendLimitWarning > 5000){
				int blockLimit = ((GameStateInterface)getState()).getGameState().getBlockLimit(getSegmentController());
				double massLimit = FastMath.round(((GameStateInterface)getState()).getGameState().getMassLimit(getSegmentController()) * 100.0) / 100.0;
				String limit = blockLimit > 0 ? blockLimit + " " + Lng.str("blocks") : "";
				limit += limit.length() > 0 && massLimit > 0 ? " " + Lng.str("and") + " " : "";
				limit += massLimit > 0 ? massLimit + " " + Lng.str("mass") : "";
				
				getSegmentController().popupOwnClientMessage(
						Lng.str("WARNING! SERVER MASS/BLOCK REACHED!\nWeapons/Tools of this entity will not have any effect on server!\nServer doesn't allow more than %s", limit), ServerMessage.MESSAGE_TYPE_ERROR);	
				lastSendLimitWarning = System.currentTimeMillis();
			}
			return false;
		}
		return super.canHandle(unit);
	}
	public static Vector3f getShootingDir(SegmentController c, Vector3f defaultDir, Vector3f defaultUp, Vector3f defaultRight, Vector3f forwardTmp, Vector3f shootingUoTemp, Vector3f shootingRightTemp, boolean useBlockOrientation, Vector3i collectionControllerPos, final SegmentPiece tmpPiece) {
		forwardTmp.set(defaultDir);
		shootingUoTemp.set(defaultUp);
		shootingRightTemp.set(defaultRight);
		if (useBlockOrientation) {
			SegmentPiece pointUnsave;
			pointUnsave = c.getSegmentBuffer().getPointUnsave(collectionControllerPos, tmpPiece);
//			System.err.println("USING BLOCK ORIENT: "+pointUnsave+": "+pointUnsave.getOrientation());
			if (pointUnsave != null && pointUnsave.getOrientation() != Element.FRONT) {
				byte orient = pointUnsave.getOrientation();
				switch(orient) {
					case (Element.BACK) -> {
						GlUtil.getBackVector(forwardTmp, c.getWorldTransform());
						GlUtil.getUpVector(shootingUoTemp, c.getWorldTransform());
						GlUtil.getRightVector(shootingRightTemp, c.getWorldTransform());
					}
					case (Element.LEFT) -> {
						GlUtil.getRightVector(forwardTmp, c.getWorldTransform());
						GlUtil.getUpVector(shootingUoTemp, c.getWorldTransform());
						GlUtil.getForwardVector(shootingRightTemp, c.getWorldTransform());
					}
					case (Element.RIGHT) -> {
						GlUtil.getLeftVector(forwardTmp, c.getWorldTransform());
						GlUtil.getUpVector(shootingUoTemp, c.getWorldTransform());
						GlUtil.getBackVector(shootingRightTemp, c.getWorldTransform());
					}
					case (Element.TOP) -> {
						GlUtil.getUpVector(forwardTmp, c.getWorldTransform());
						GlUtil.getForwardVector(shootingUoTemp, c.getWorldTransform());
						GlUtil.getRightVector(shootingRightTemp, c.getWorldTransform());
					}
					case (Element.BOTTOM) -> {
						GlUtil.getBottomVector(forwardTmp, c.getWorldTransform());
						GlUtil.getBackVector(shootingUoTemp, c.getWorldTransform());
						GlUtil.getRightVector(shootingRightTemp, c.getWorldTransform());
					}
				}

			}
		}

		return forwardTmp;
	}

	public static final void drawReload(InputState state, Vector3i iconPos, Vector3i iconSize, Vector4f reloadColor, boolean backwards, float percent) {
		drawReload(state, iconPos, iconSize, reloadColor, backwards, percent, false, 0, 0, -1, null);
	}
	
	public static final void drawReloadText(InputState state, Vector3i iconPos, Vector3i iconSize, GUITextOverlay textCharge) {
		GlUtil.glPushMatrix();
		GlUtil.glTranslatef(iconPos.x + iconSize.x / 2.0F,
				iconPos.y + iconSize.y / 2.0F, 0.0F);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); // default
		
		textCharge.draw();
		
		GlUtil.glPopMatrix();
		GlUtil.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_BLEND);
	}
	public static final void drawReload(InputState state, Vector3i iconPos, Vector3i iconSize, Vector4f reloadColor, boolean backwards, float percent, 
			boolean drawOneCharge, float charges, int maxCharges, long timeLeftMS, GUITextOverlay textCharge) {
		if(percent > 10000){
			throw new RuntimeException("Too much charge? "+percent);
		}
		percent = Math.min(1, percent);
		GlUtil.glPushMatrix();
		GlUtil.glTranslatef(iconPos.x + iconSize.x / 2.0F,
				iconPos.y + iconSize.y / 2.0F, 0.0F);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); // default
		GlUtil.glColor4f(reloadColor.x, reloadColor.y, reloadColor.z, reloadColor.w);

		float offsetAmount = 0.125F;
		float percentOffset = percent + offsetAmount;

		
		if(!drawOneCharge || percent < 1f) {
			GL11.glBegin(GL11.GL_TRIANGLE_FAN);
			GL11.glVertex3f(0, 0, 0);
			float size = iconSize.x / 2.0F;
			int i = 0;
			for (float offset0 = percentOffset; offset0 >= offsetAmount - 0.005F; offset0 -= 0.005F) {
				float offset0Copy = offset0;
				if (offset0Copy < offsetAmount) {
					offset0Copy = offsetAmount;
				}
				if (offset0Copy > 1.0F) {
					offset0Copy -= 1.0F;
				}
				if (offset0Copy < 0.25F) {
					GL11.glVertex2f(iconSize.x * offset0Copy * 4.0F - size, -size);
				} else if (offset0Copy < 0.5F) {
					offset0Copy -= 0.25F;
					GL11.glVertex2f(size, iconSize.y * offset0Copy * 4.0F - size);
				} else if (offset0Copy < 0.75F) {
					offset0Copy -= 0.5F;
					GL11.glVertex2f(-iconSize.x * offset0Copy * 4.0F + size, size);
				} else {
					offset0Copy -= 0.75F;
					GL11.glVertex2f(-size, -iconSize.y * offset0Copy * 4.0F + size);
				}
				i++;
			}
			GL11.glEnd();
		}

		
		
		if(timeLeftMS > 0){
			textCharge.setTextSimple(Lng.str("%s sec",StringTools.formatPointZero(timeLeftMS/1000d)));
			
			textCharge.setPos(-8, 7, 0);
			if(drawOneCharge) {
				textCharge.getPos().x = -20;
			}
			textCharge.draw();
		}else if(drawOneCharge || maxCharges > 1){
			//note: position starts at middle
			String chargeStr;
			if(!drawOneCharge) {
				chargeStr = StringTools.massFormat((int)charges);
			}else {
				if(Math.abs((float)Math.round(charges) - charges) < 0.05f){
					chargeStr = StringTools.massFormat(Math.floor(charges));
				}else {
					chargeStr = StringTools.massFormat(charges);
				}
			}
			textCharge.setTextSimple(chargeStr+" / "+StringTools.massFormat(maxCharges));
			textCharge.setPos(-8, 7, 0);
			if(drawOneCharge) {
				//textCharge.getPos().x = -20;
			}
			textCharge.draw();
		}
		GlUtil.glPopMatrix();
		GlUtil.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_BLEND);
		
	}

	@Override
	public int getPriority() {
		return 1;
	}

	@Override
	public E getRandomCollection(Random r) {
		if (hasAnyBlock()) {
			CM cm = getCollectionManagers().get(r.nextInt(getCollectionManagers().size()));
			if (cm.getElementCollections().size() > 0 && cm.getTotalSize() > 0) {
				return cm.getElementCollections().get(r.nextInt(cm.getElementCollections().size()));
			} else {
				for (int i = 0; i < getCollectionManagers().size(); i++) {
					CM cc = getCollectionManagers().get(i);
					if (cc.getElementCollections().size() > 0 && cc.getTotalSize() > 0) {
						return cc.getElementCollections().get(r.nextInt(cc.getElementCollections().size()));
					}
				}
			}
		}
		return null;
	}

	public void beforeUpdate() {
	}
	public void checkIntegrityServer() {
		if( !integrityChecked && ElementKeyMap.isValidType(controllerId) && ElementKeyMap.isValidType(controllingId)){
			if(getSegmentController().isFullyLoaded()){
				
				if(startCheck == 0L){
					startCheck = System.currentTimeMillis();
					//do real check after 30 seconds
				}else if(System.currentTimeMillis() - startCheck > 30000){
					int connected = 0;
					for(CM cm : collectionManagers){
						Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> pp = 
							getSegmentController().getControlElementMap().getControllingMap().get(ElementCollection.getIndex(cm.getControllerPos()));
						if(pp != null){
							FastCopyLongOpenHashSet m = pp.get(controllingId);
							if(m != null){
								connected += m.size();
							}
						}
					}
				
					if(connected > getSegmentController().getElementClassCountMap().get(controllingId)){
						needsDeepCheck = true;
					}
					
					integrityChecked = true;
				}
			}
		}
		
		
		if(needsDeepCheck){
			if(deepCheckPointer >= collectionManagers.size() && (deepChecker == null || deepChecker.isDone())){
				needsDeepCheck = false;
				deepChecker = null;
				System.err.println("[SERVER][CHECKER] "+getSegmentController()+" PERFORMING DEEP STRUCTURE CHECK DONE! Total Comps: "+deepCheckPointer);
			}else{
				if(deepChecker == null || deepChecker.isDone()){
					CM cm = collectionManagers.get(deepCheckPointer);
					if(deepChecker == null){
						deepChecker = new DeepStructureChecker();
					}
					deepChecker.set(getSegmentController(), cm.getControllerIndex(), controllingId, cm.getTotalSize());
					for(E e : cm.getElementCollections()){
						deepChecker.init(e.getNeighboringCollection());
					}
					
					deepChecker.update();
					deepCheckPointer++;
				}else{
					deepChecker.update();
				}
			}
		}
	}

	public void afterUpdate() {
	}

	
	
	
	
	

	
	public void addConnectionIfNecessary(Vector3i controller, short fromType, Vector3i controlled, short controlledType) {
		if (controlledType == controllingId || controllingId == Element.TYPE_ALL) {
//			System.err.println(getSegmentController().isOnServer()+" ADDDDDDDDDDD: "+controller+" -> "+controlled);
			
			//normal adding
			CM m = collectionManagerMap.get(ElementCollection.getIndex(controller));
			if (m != null) {
				m.addModded(controlled, controlledType);
				this.notifyObservers(SegNotifyType.SHIP_ELEMENT_CHANGED);
				return;
			}
		} else {
			if (ElementKeyMap.isValidType(controllerId) && ElementKeyMap.getInfo(controllerId).isCombiConnectAny(controlledType)) {
				//both elements are weapons controllers -> ok to connect and put as extra connection
				CM m = collectionManagerMap.get(ElementCollection.getIndex(controller));
				if (m != null) {
					if (ElementKeyMap.getInfo(controllerId).isCombiConnectSupport(controlledType)) {
						long blockIndex = ElementCollection.getIndex4((short) controlled.x, (short) controlled.y, (short) controlled.z, controlledType);
						if (blockIndex != m.getSlaveConnectedElement()) {

							if (m.getSlaveConnectedElement() != Long.MIN_VALUE) {
								if (DEBUG) {
									System.err.println("SLAVE SUPPORT ALREADY SET. DISCONNECTING " + m.getSlaveConnectedElement() + " (replacing with " + blockIndex + ")");
								}

								long supPos = ElementCollection.getPosIndexFrom4(m.getSlaveConnectedElement());
								short supType = (short) ElementCollection.getType(m.getSlaveConnectedElement());

								getSegmentController().getControlElementMap().removeControllerForElement(ElementCollection.getIndex(controller),
										supPos, supType);

								if (getSegmentController() instanceof Ship) {
									if (DEBUG) {
										System.err.println("ADDING SUPPORT BACK TO DEFAULT (CORE): " + ElementKeyMap.toString(supType));
									}
									getSegmentController().getControlElementMap().addControllerForElement(ElementCollection
											.getIndex(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF), supPos, supType);

									//remove slave's connection to core
									getSegmentController().getControlElementMap()
									.removeControllerForElement(ElementCollection.getIndex(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF), ElementCollection.getIndex(controlled), controlledType);
								}
							}
							if (DEBUG) {
								System.err.println(getState() + "; " + getSegmentController() + "[UsableControllableElementManager] ADDING SUPPORT COMBO " + controller + "->" + controlled);
							}
							m.setSlaveConnectedElement(blockIndex);

							ManagerModuleCollection<?, ?, ?> managerModuleCollection = getManagerContainer().getModulesControllerMap().get(controlledType);
							if (managerModuleCollection != null) {
								ControlBlockElementCollectionManager<?, ?, ?> controlledCM = managerModuleCollection.getCollectionManagersMap().get(ElementCollection.getIndex(controlled));

								if (controlledCM != null) {
									disconnectAllSupportAndEffectAndLight(controlledCM, controlled);
								}
							}
							this.notifyObservers(SegNotifyType.SHIP_ELEMENT_CHANGED);

							if (getState() instanceof GameClientState) {
								((GameClientState) getState()).getWorldDrawer().getGuiDrawer().getPlayerPanel().getWeaponManagerPanel().setReconstructionRequested(true);
							}
						}
						return;
					}
					if (ElementKeyMap.getInfo(controllerId).isCombiConnectEffect(controlledType)) {

//						System.err.println("::::::: ADD ONNN "+getState()+"; "+controller+" -> "+controlled+"; "+fromType+"; "+controlledType);
						
						long blockIndex = ElementCollection.getIndex4((short) controlled.x, (short) controlled.y, (short) controlled.z, controlledType);
						if (blockIndex != m.getEffectConnectedElement()) {
							if (m.getEffectConnectedElement() != Long.MIN_VALUE) {
								if (DEBUG) {
									System.err.println("SLAVE EFFECT ALREADY SET. DISCONNECTING " + m.getEffectConnectedElement() + " (replacing with " + blockIndex + ")");
								}

								long supPos = ElementCollection.getPosIndexFrom4(m.getEffectConnectedElement());
								short supType = (short) ElementCollection.getType(m.getEffectConnectedElement());

//								getSegmentController().get

								getSegmentController().getControlElementMap().removeControllerForElement(ElementCollection.getIndex(controller),
										supPos, supType);

								if (getSegmentController() instanceof Ship) {
									if (DEBUG) {
										System.err.println("ADDING EFFECT BACK TO DEFAULT (CORE): " + ElementKeyMap.toString(supType));
									}
									getSegmentController().getControlElementMap().addControllerForElement(
											ElementCollection.getIndex(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF), supPos, supType);

									//remove slave's connection to core
									getSegmentController().getControlElementMap().removeControllerForElement(
											ElementCollection.getIndex(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF), ElementCollection.getIndex(controlled), controlledType);
								}
							}
							if (DEBUG) {
								System.err.println(getState() + "; " + getSegmentController() + "[UsableControllableElementManager] ADDING EFFECT COMBO " + controller + "->" + controlled);
							}
							m.setEffectConnectedElement(blockIndex);
							this.notifyObservers(SegNotifyType.SHIP_ELEMENT_CHANGED);

							if (getState() instanceof GameClientState) {
								((GameClientState) getState()).getWorldDrawer().getGuiDrawer().getPlayerPanel().getWeaponManagerPanel().setReconstructionRequested(true);
							}
							ManagerModuleCollection<?, ?, ?> managerModuleCollection = getManagerContainer().getModulesControllerMap().get(controlledType);
							if (managerModuleCollection != null) {
								ControlBlockElementCollectionManager<?, ?, ?> controlledCM = managerModuleCollection.getCollectionManagersMap().get(ElementCollection.getIndex(controlled));

								if (controlledCM != null) {
									disconnectAllSupportAndEffectAndLight(controlledCM, controlled);
								}
							}
						}
						return;
					}
					if (ElementKeyMap.getInfo(controllerId).isLightConnect(controlledType)) {

						long blockIndex = ElementCollection.getIndex4((short) controlled.x, (short) controlled.y, (short) controlled.z, controlledType);
						if (blockIndex != m.getLightConnectedElement()) {
							if (m.getLightConnectedElement() != Long.MIN_VALUE) {
								if (DEBUG) {
									System.err.println("LIGHT EFFECT ALREADY SET. DISCONNECTING " + m.getLightConnectedElement() + " (replacing with " + blockIndex + ")");
								}

								long supPos = ElementCollection.getPosIndexFrom4(m.getLightConnectedElement());
								short supType = (short) ElementCollection.getType(m.getLightConnectedElement());

								getSegmentController().getControlElementMap().removeControllerForElement(
										ElementCollection.getIndex(controller),
										supPos, supType);

								//remove slave's connection to core
								getSegmentController().getControlElementMap().removeControllerForElement(
										ElementCollection.getIndex(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF), ElementCollection.getIndex(controlled), controlledType);

							}
							m.setLightConnectedElement(blockIndex);
							this.notifyObservers(SegNotifyType.SHIP_ELEMENT_CHANGED);

							if (getState() instanceof GameClientState) {
								((GameClientState) getState()).getWorldDrawer().getGuiDrawer().getPlayerPanel().getWeaponManagerPanel().setReconstructionRequested(true);
							}

							ManagerModuleCollection<?, ?, ?> managerModuleCollection = getManagerContainer().getModulesControllerMap().get(controlledType);
							if (managerModuleCollection != null) {
								ControlBlockElementCollectionManager<?, ?, ?> controlledCM = managerModuleCollection.getCollectionManagersMap().get(ElementCollection.getIndex(controlled));

								if (controlledCM != null) {
									disconnectAllSupportAndEffectAndLight(controlledCM, controlled);
								}
							}
						}
						return;
					}
				}

				if (DEBUG) {
					System.err.println(this.getClass().getSimpleName() + "; " + getState() + "; " + getSegmentController() + "[UsableControllableElementManager] NOT ADDING WEAPON CONROLELR COMBO: " + getCollectionManagers() + "; " + controller + "; " + controller + "->" + controlled);
				}
			} else {
				if (DEBUG) {
					System.err.println(this.getClass().getSimpleName() + "; " + getState() + "; " + getSegmentController() + "[UsableControllableElementManager] NOT ADDING BECAUSE INVALID TYPE " + ElementKeyMap.toString(controlledType) + "; type differs: " + controlledType + "; " + ElementKeyMap.toString(controllingId) + "; " + "; attempted to add " + ElementKeyMap.toString(controlledType) + " to " + ElementKeyMap.toString(controllerId));
				}
			}
		}
		if (DEBUG) {
			System.err.println(this.getClass().getSimpleName() + "; " + getState() + "; " + getSegmentController() + "[UsableControllableElementManager] NOT ADDING " + ElementKeyMap.toString(controlledType) + "; type differs: " + ElementKeyMap.toString(controlledType) + "; " + ElementKeyMap.toString(controllingId) + ", or position not found: " + getCollectionManagers() + "; controller: " + ElementKeyMap.toString(controllerId));
		}
	}

	private void disconnectAllSupportAndEffectAndLight(ControlBlockElementCollectionManager<?, ?, ?> controlledCM, Vector3i controlled) {
		if (controlledCM.getSlaveConnectedElement() != Long.MIN_VALUE) {
			long supPos = ElementCollection.getPosIndexFrom4(controlledCM.getSlaveConnectedElement());
			short supType = (short) ElementCollection.getType(controlledCM.getSlaveConnectedElement());
			getSegmentController().getControlElementMap().removeControllerForElement(ElementCollection.getIndex(controlled),
					supPos, supType);
			if (getSegmentController() instanceof Ship) {
				getSegmentController().getControlElementMap().addControllerForElement(
						ElementCollection.getIndex(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF),
						supPos, supType);
			}
		}
		if (controlledCM.getEffectConnectedElement() != Long.MIN_VALUE) {
			long supPos = ElementCollection.getPosIndexFrom4(controlledCM.getEffectConnectedElement());
			short supType = (short) ElementCollection.getType(controlledCM.getEffectConnectedElement());
			getSegmentController().getControlElementMap().removeControllerForElement(ElementCollection.getIndex(controlled),
					supPos, supType);
			if (getSegmentController() instanceof Ship) {

				getSegmentController().getControlElementMap().addControllerForElement(
						ElementCollection.getIndex(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF),
						supPos, supType);
			}
		}
		if (controlledCM.getLightConnectedElement() != Long.MIN_VALUE) {
			long supPos = ElementCollection.getPosIndexFrom4(controlledCM.getLightConnectedElement());
			short supType = (short) ElementCollection.getType(controlledCM.getLightConnectedElement());
			getSegmentController().getControlElementMap().removeControllerForElement(ElementCollection.getIndex(controlled),
					supPos, supType);

//			if(getSegmentController() instanceof Ship){
//				getSegmentController().getControlElementMap().addControllerForElement(ElementCollection.getIndex(SegmentData.SEG_HALF,SegmentData.SEG_HALF,SegmentData.SEG_HALF),
//						supPos, supType);
//			}
		}

		controlledCM.setSlaveConnectedElement(Long.MIN_VALUE);
		controlledCM.setEffectConnectedElement(Long.MIN_VALUE);
		controlledCM.setLightConnectedElement(Long.MIN_VALUE);
	}

	public void removeConnectionIfNecessary(Vector3i controller, Vector3i controlled, short controlledType) {
		if (DEBUG) {
			System.err.println("REMOVE IF NECESSARY CON: " + ElementKeyMap.toString(controllerId) + " -> " + ElementKeyMap.toString(controlledType) + "; " + controller + " -> " + controlled);
		}
		if (controlledType == controllingId || controllingId == Element.TYPE_ALL) {
			CM m = collectionManagerMap.get(ElementCollection.getIndex(controller));
			if (m != null) {
				if (DEBUG) {
					System.err.println("REMOVING NORMAL");
				}
				m.remove(controlled);
				this.notifyObservers(SegNotifyType.SHIP_ELEMENT_CHANGED);
				return;
			}
		} else {
			if (ElementKeyMap.isValidType(controllerId) && ElementKeyMap.getInfo(controllerId).isCombiConnectAny(controlledType)) {
				if (DEBUG) {
					System.err.println("CHECKING CON: " + ElementKeyMap.toString(controllerId) + " -> " + ElementKeyMap.toString(controlledType));
				}
				//both elements are weapons controllers -> ok to connect and put as extra connection
				CM m = collectionManagerMap.get(ElementCollection.getIndex(controller));
				if (m != null) {

					if (ElementKeyMap.getInfo(controllerId).isCombiConnectSupport(controlledType)) {
						if (DEBUG) {
							System.err.println("REMOVING COMBI SUPPORT CONNECTION");
						}

						if (getSegmentController() instanceof Ship && m.getSlaveConnectedElementRaw() != Long.MIN_VALUE) {
							if (DEBUG) {
								System.err.println("ADDING SUPPORT BACK TO DEFAULT (CORE): " + ElementKeyMap.toString(controlledType));
							}
							FastCopyLongOpenHashSet coreCons = getSegmentController()
									.getControlElementMap().getControllingMap()
									.getAll().get(ElementCollection.getIndex(Ship.core));
							if(coreCons == null || !coreCons.contains(m.getSlaveConnectedElementRaw())){
								getSegmentController().getControlElementMap().addControllerForElement(
										ElementCollection.getIndex(Ship.core),
										ElementCollection.getPosIndexFrom4(m.getSlaveConnectedElementRaw()),
										(short) ElementCollection.getType(m.getSlaveConnectedElementRaw()));
							}
						}
						m.setSlaveConnectedElement(Long.MIN_VALUE);
						this.notifyObservers(SegNotifyType.SHIP_ELEMENT_CHANGED);

						if (getState() instanceof GameClientState) {
							((GameClientState) getState()).getWorldDrawer().getGuiDrawer().getPlayerPanel().getWeaponManagerPanel().setReconstructionRequested(true);
						}
						return;
					}
					if (ElementKeyMap.getInfo(controllerId).isCombiConnectEffect(controlledType)) {
//						System.err.println("::::::: REMOVE ONNN "+getState()+"; "+controller+" -> "+controlled+";; "+controlledType);
						if (DEBUG) {
							System.err.println("REMOVING COMBI EFFECT CONNECTION");
						}

						if (getSegmentController() instanceof Ship && m.getEffectConnectedElementRaw() != Long.MIN_VALUE) {
							if (DEBUG) {
								System.err.println("ADDING EFFECT BACK TO DEFAULT (CORE): " + ElementKeyMap.toString(controlledType));
							}
							FastCopyLongOpenHashSet coreCons = getSegmentController()
									.getControlElementMap().getControllingMap()
									.getAll().get(ElementCollection.getIndex(Ship.core));
							if(coreCons == null || !coreCons.contains(m.getEffectConnectedElementRaw())){
								getSegmentController().getControlElementMap().addControllerForElement(
										ElementCollection.getIndex(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF),
										ElementCollection.getPosIndexFrom4(m.getEffectConnectedElementRaw()), (short) ElementCollection.getType(m.getEffectConnectedElementRaw()));
							}
						}

						m.setEffectConnectedElement(Long.MIN_VALUE);
						this.notifyObservers(SegNotifyType.SHIP_ELEMENT_CHANGED);

						if (getState() instanceof GameClientState) {
							((GameClientState) getState()).getWorldDrawer().getGuiDrawer().getPlayerPanel().getWeaponManagerPanel().setReconstructionRequested(true);
						}
						return;
					}
					if (ElementKeyMap.getInfo(controllerId).isLightConnect(controlledType)) {
						if (DEBUG) {
							System.err.println("REMOVING COMBI LIGHT CONNECTION");
						}

						//							if(getSegmentController() instanceof Ship && m.getLightConnectedElement() != Long.MIN_VALUE){
						//								System.err.println("ADDING LIGHT BACK TO DEFAULT (CORE): "+ElementKeyMap.toString(controlledType));
						//							}

						m.setLightConnectedElement(Long.MIN_VALUE);
						this.notifyObservers(SegNotifyType.SHIP_ELEMENT_CHANGED);

						if (getState() instanceof GameClientState) {
							((GameClientState) getState()).getWorldDrawer().getGuiDrawer().getPlayerPanel().getWeaponManagerPanel().setReconstructionRequested(true);
						}
						return;
					}

				} else {
					if (DEBUG) {
						System.err.println("NOT VALID: " + ElementKeyMap.toString(controllerId) + " -> " + ElementKeyMap.toString(controlledType) + "; not found CM: " + ElementCollection.getIndex(controller) + "; " + collectionManagerMap);
					}

				}

				return;
			} else {
				if (DEBUG) {
					System.err.println("NO CON SU CON: " + ElementKeyMap.toString(controllerId) + " -> " + ElementKeyMap.toString(controlledType));
				}
			}
		}
	}

	protected boolean convertDeligateControls(ControllerStateInterface unit, Vector3i controlledFromOrig, Vector3i controlledFrom) throws IOException {
		
		unit.getParameter(controlledFromOrig);
		unit.getParameter(controlledFrom);

		if (unit.getPlayerState() == null) {
			return true;
		}
		SlotAssignment shipConfiguration = null;
			SegmentPiece fromPiece = getSegmentBuffer().getPointUnsave(controlledFrom);//autorequest true previously
			if(fromPiece == null){
				return false;
			}
			if (fromPiece.getType() == ElementKeyMap.CORE_ID) {
				try {
					shipConfiguration = checkShipConfig(unit);
					int currentlySelectedSlot = unit.getPlayerState().getCurrentShipControllerSlot();
					if (unit.getPlayerState() != null && !shipConfiguration.hasConfigForSlot(currentlySelectedSlot)) {
						return false;
					} else {
						controlledFrom.set(shipConfiguration.get(currentlySelectedSlot));
					}
				} catch (ShipConfigurationNotFoundException e) {
					return false;
				}
			}
		return true;
	}

	public List<CM> getCollectionManagers() {
		return collectionManagers;
	}

	public Long2ObjectOpenHashMap<CM> getCollectionManagersMap() {
		return collectionManagerMap;
	}

	public final boolean hasMetaData() {
		return this instanceof TagModuleUsableInterface;
	}
	public void onAddedCollection(long absPos, CM newInstance) {
	}

	public void onConnectionRemoved(long absPos, CM collection) {
	}

	//	protected boolean receiveDistribution(ReceivedDistribution d){
//		CM m = getCollectionManagersMap().get(ElementCollection.getIndex(d.controllerPos));
//		if(m != null){
//			if(m.receiveDistribution(d)){
//				return true;
//			}
//		}
//
//		return false;
//	}
	public Tag toTagStructure() {

		List<Tag> tags = new ObjectArrayList<Tag>(getCollectionManagers().size());
		for (int i = 0; i < getCollectionManagers().size(); i++) {
			if (getCollectionManagers().get(i).hasTag()) {
				tags.add(getCollectionManagers().get(i).toTagStructure());
			}
		}

		Tag[] t = new Tag[tags.size() + 1];
		for (int i = 0; i < tags.size(); i++) {
			t[i] = tags.get(i);
		}
		assert (((TagModuleUsableInterface)this).getTagId() != null) : this.getClass();
		t[tags.size()] = FinishTag.INST;

		Tag str = new Tag(Type.STRUCT, ((TagModuleUsableInterface)this).getTagId(), t);
		return str;
	}
	@Override
	public void flagCheckUpdatable() {
		final int size = collectionManagers.size();
		if(size > 10000) {
			System.err.println("Exception: Entity "+getSegmentController()+" has abnormal amount of collectionManagers: "+size);
		}
		if(size > 1000000) {
			System.err.println("Exception: FATAL! Entity "+getSegmentController()+" has abnormal amount of collectionManagers: "+size+" Entity is being removed now!");
			setUpdatable(false);
			getSegmentController().destroyPersistent();
			return;
		}
		for(int i = 0; i < size; i++) {
			CM c = collectionManagers.get(i);
			if(c.isStructureUpdateNeeded()) {
				setUpdatable(true);
				return;
			}
		}
		
		setUpdatable(false);
	}
	public boolean isUsingRegisteredActivation() {
		return false;
	}
}


