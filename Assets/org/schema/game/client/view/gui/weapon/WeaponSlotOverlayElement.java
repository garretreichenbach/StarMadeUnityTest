package org.schema.game.client.view.gui.weapon;

import java.util.Locale;

import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.element.world.ClientSegmentProvider;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.SlotAssignment;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.ManagerModuleCollection;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.Draggable;
import org.schema.schine.graphicsengine.forms.gui.DropTarget;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangle;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUIToolTip;
import org.schema.schine.graphicsengine.forms.gui.TooltipProvider;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHelperIcon;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHelperTextureType;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.input.Mouse;

public class WeaponSlotOverlayElement extends GUIOverlay implements Draggable, DropTarget<WeaponSlotOverlayElement>, TooltipProvider {

	public static final int SLOT_NORMAL = 0;
	public static final int SLOT_SUPPORT = 1;
	public static final int SLOT_EFFECT = 2;
	public static boolean USE_DELAY_GRAB = false;
	private GUIToolTip toolTip;
	private short type;
	private long timeDraggingStart = -1;
	private int dragPosX;
	private int dragPosY;
	private boolean stickyDrag;
	private short slaveEffectType = -1;
	private short slaveSupportType = -1;
	private long posIndex = Long.MIN_VALUE;
	private long tiedToPosIndex = Long.MIN_VALUE;
	private int slot = -1;
	private int slotStyle = SLOT_NORMAL;
	private GUIColoredRectangle selectIcon;
	private boolean unloadedHighlightSupport;
	private boolean unloadedHighlightEffect;
	private short tiedToType;
	private boolean init;
	private GUITextOverlayTable test;
	private short lastType;
	private final GUIHelperIcon middleClick;

	public WeaponSlotOverlayElement(InputState state) {
		super(Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath()+"build-icons-00-16x16-gui-"), state);
		setMouseUpdateEnabled(true);
		this.setCallback(this);

		selectIcon = new GUIColoredRectangle(getState(), UIScale.getUIScale().ICON_SIZE, UIScale.getUIScale().ICON_SIZE, new Vector4f(1, 1, 1, 0.18f));
		if(EngineSettings.DRAW_TOOL_TIPS.isOn()){
		toolTip = new GUIToolTip(state, Lng.str("unknown module"), this);
		}
		test = new GUITextOverlayTable(getState());
		test.setTextSimple("UNDEF");
		middleClick = new GUIHelperIcon(getState(), GUIHelperTextureType.SINGLE, FontSize.SMALL_15, FontSize.SMALL_15);
		middleClick.setTextOn(KeyboardMappings.WEAPON_PANEL.getKeyChar().toUpperCase(Locale.ENGLISH));
//		middleClick.setScale(2, 2, 0);
		middleClick.setTextAfter(Lng.str("Assign"));
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.AbstractSceneNode#toString()
	 */
	@Override
	public String toString() {
		return "WeaponSlot(" + ElementKeyMap.toString(type) + "[" + posIndex + "])";
	}

	public boolean isDrawing() {
		return ((GameClientState) getState()).getWorldDrawer().getGuiDrawer().getPlayerPanel().isDrawShipSideBar();
	}
	
	@Override
	public void cleanUp() {
		super.cleanUp();
		middleClick.cleanUp();
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (isDrawing()) {
			if (slotStyle == SLOT_NORMAL) {
				for (MouseEvent e : getState().getController().getInputController().getMouseEvents()) {
					if (e.pressedLeftMouse()) {
						if (isInside()) {
							//e.state will already confirm, that the mouse was pressed inside of this
							this.timeDraggingStart = System.currentTimeMillis();
							if (!USE_DELAY_GRAB && type != 0) {
								getState().getController().getInputController().setDragging(this);
								dragPosX = (int) getRelMousePos().x;
								dragPosY = (int) getRelMousePos().y;
							}
						}
					} else {
						this.timeDraggingStart = -1;
					}
				}
				if (USE_DELAY_GRAB && Mouse.isPrimaryMouseDownUtility()) {
					if (getState().getController().getInputController().getDragging() != this && this.getTimeDragStarted() > 0 && (System.currentTimeMillis() - this.getTimeDragStarted()) > Draggable.MIN_DRAG_TIME) {
						getState().getController().getInputController().setDragging(this);
						dragPosX = (int) getRelMousePos().x;
						dragPosY = (int) getRelMousePos().y;
					}
				}
				checkTarget(event);
			}
			if (slotStyle == SLOT_SUPPORT) {
				if (type != 0) {
					for (MouseEvent e : getState().getController().getInputController().getMouseEvents()) {
						if (e.pressedLeftMouse()) {
							if (isInside()) {
								//e.state will already confirm, that the mouse was pressed inside of this
								this.timeDraggingStart = System.currentTimeMillis();
								if (!USE_DELAY_GRAB) {
									getState().getController().getInputController().setDragging(this);
									dragPosX = (int) getRelMousePos().x;
									dragPosY = (int) getRelMousePos().y;
								}
							}
						} else {
							this.timeDraggingStart = -1;
						}
					}
					if (USE_DELAY_GRAB && Mouse.isPrimaryMouseDownUtility()) {
						if (getState().getController().getInputController().getDragging() != this && this.getTimeDragStarted() > 0 && (System.currentTimeMillis() - this.getTimeDragStarted()) > Draggable.MIN_DRAG_TIME) {
							getState().getController().getInputController().setDragging(this);
							dragPosX = (int) getRelMousePos().x;
							dragPosY = (int) getRelMousePos().y;
						}
					}
				}

				checkTarget(event);
			}
			if (slotStyle == SLOT_EFFECT) {
				if (type != 0) {
					for (MouseEvent e : getState().getController().getInputController().getMouseEvents()) {
						if (e.pressedLeftMouse()) {
							if (isInside()) {
								//e.state will already confirm, that the mouse was pressed inside of this
								this.timeDraggingStart = System.currentTimeMillis();
								if (!USE_DELAY_GRAB) {
									getState().getController().getInputController().setDragging(this);
									dragPosX = (int) getRelMousePos().x;
									dragPosY = (int) getRelMousePos().y;
								}
							}
						} else {
							this.timeDraggingStart = -1;
						}
					}
					if (USE_DELAY_GRAB && Mouse.isPrimaryMouseDownUtility()) {
						if (getState().getController().getInputController().getDragging() != this && this.getTimeDragStarted() > 0 && (System.currentTimeMillis() - this.getTimeDragStarted()) > Draggable.MIN_DRAG_TIME) {
							getState().getController().getInputController().setDragging(this);
							dragPosX = (int) getRelMousePos().x;
							dragPosY = (int) getRelMousePos().y;
						}
					}
				}
				checkTarget(event);
			}

		}

	}

	@Override
	public boolean isOccluded() {
		return !((GameClientState) getState()).getPlayerInputs().isEmpty();
	}

	@Override
	public boolean checkDragReleasedMouseEvent(MouseEvent e) {
		return (e.releasedLeftMouse()) || (e.pressedLeftMouse() && getState().getController().getInputController().getDragging().isStickyDrag());
	}

	@Override
	public int getDragPosX() {
		return dragPosX;
	}

	/**
	 * @param dragPosX the dragPosX to set
	 */
	@Override
	public void setDragPosX(int dragPosX) {
		this.dragPosX = dragPosX;
	}

	@Override
	public int getDragPosY() {
		return dragPosY;
	}

	/**
	 * @param dragPosY the dragPosY to set
	 */
	@Override
	public void setDragPosY(int dragPosY) {
		this.dragPosY = dragPosY;
	}

	@Override
	public Object getPlayload() {
		return null;
	}

	@Override
	public long getTimeDragStarted() {
		return timeDraggingStart;
	}

	@Override
	public boolean isStickyDrag() {
		return stickyDrag;
	}

	/**
	 * @param stickyDrag the stickyDrag to set
	 */
	@Override
	public void setStickyDrag(boolean stickyDrag) {
		this.stickyDrag = stickyDrag;
	}

	@Override
	public void checkTarget(MouseEvent e) {
		if (isInside() && (isDrawing() || unloadedHighlightEffect || unloadedHighlightSupport)) {
			Draggable dragging = getState().getController().getInputController().getDragging();
			if (dragging != null) {
				if (dragging instanceof WeaponSlotOverlayElement && posIndex == ((WeaponSlotOverlayElement) dragging).posIndex) {
					//do not handle self
					return;
				}

				if (dragging.checkDragReleasedMouseEvent(e)) {
					System.err.println("[DRAG RELEASE] CHECKING " + this + " MOUSE NO MORE GRABBED for dragging: " + getState().getController().getInputController().getDragging());
					if (isTarget(dragging) && (dragging != this)) {
						if ((System.currentTimeMillis() - dragging.getTimeDragStarted()) > Draggable.MIN_DRAG_TIME) {
							System.err.println("NOW DROPPING " + dragging + "; " + dragging.hashCode() + " on " + this + "; " + hashCode());
							onDrop((WeaponSlotOverlayElement) dragging);
						} else {
							System.err.println("NO DROP: time dragged to short");
						}
						getState().getController().getInputController().setDragging(null);
						return;
					} else {
						if ((dragging == this)) {
							System.err.println("NO DROP: dragging and target are the same");
						}
						if ((!isTarget(dragging))) {
							System.err.println("NO DROP: not a target: " + dragging + " for " + this);
						}
					}
				}
			}
		}
	}

	@Override
	public boolean isTarget(Draggable draggable) {
		if (draggable != null && draggable instanceof WeaponSlotOverlayElement) {
			return posIndex != ((WeaponSlotOverlayElement) draggable).posIndex;
		}
		return false;
	}

	@Override
	public void onDrop(WeaponSlotOverlayElement draggable) {
		System.err.println("[CLIENT][WeaponSlotOverlay] dropped " + draggable + " on: " + this);
		if (draggable.posIndex == this.posIndex) {
			return;
		}
		GameClientState gs = (GameClientState) getState();
		SlotAssignment shipConfiguration = gs.getShip().getSlotAssignment();

		int slot = this.slot;
		if (slot >= 0) {
			System.err.println("[CLIENT][WeaponSlotOverlay] DRAGGED INTO WEAPON BAR");
			int remove = -1;

			if (shipConfiguration.hasConfigForPos(draggable.posIndex)) {
				System.err.println("[CLIENT][WeaponSlotOverlay] REMOVING FROM HOTBAR AND REASSIIGNING (dropped at slot): draggable " + draggable + " -> " + this);
				remove = shipConfiguration.removeByPosAndSend(draggable.posIndex);
			}

			if (remove != slot) {
				System.err.println("[CLIENT][WeaponSlotOverlay] PUT INTO SLOT " + slot + ": REMOVE: " + remove + "; pos: " + ElementCollection.getPosFromIndex(draggable.posIndex, new Vector3i()) + " "+ draggable.posIndex +": draggable " + draggable + " -> " + this);
				shipConfiguration.modAndSend((byte) slot, draggable.posIndex);
			} else {
				System.err.println("[CLIENT][WeaponSlotOverlay] NOT PUT INTO SLOT " + slot + ": REMOVE: " + remove + "; pos: " + ElementCollection.getPosFromIndex(draggable.posIndex, new Vector3i()) + ": draggable " + draggable + " -> " + this);
			}
			SegmentPiece pointUnsave = gs.getShip().getSegmentBuffer().getSegmentController().getSegmentBuffer().getPointUnsave(draggable.posIndex);
			if (pointUnsave != null) {
				notifyObservers();
				draggable.notifyObservers();
				((GameClientState) getState()).getWorldDrawer().getGuiDrawer().getPlayerPanel().getWeaponManagerPanel().setReconstructionRequested(true);
			}
		} else {

			if (draggable.slot >= 0) {
				System.err.println("[CLIENT][WeaponSlotOverlay] REMOVING FROM HOTBAR (dropped at noslot)" + ": draggable " + draggable + " -> " + this);
				//dropped from sidebar
				if (shipConfiguration.hasConfigForPos(draggable.posIndex)) {
					int remove = shipConfiguration.removeByPosAndSend(draggable.posIndex);
					((GameClientState) getState()).getWorldDrawer().getGuiDrawer().getPlayerPanel().getWeaponManagerPanel().setReconstructionRequested(true);
				}
			} else {
				assert (draggable.slot < 0);
				assert (this.slot < 0);

				if (tiedToType > 0) {
					ElementInformation dragInfo = ElementKeyMap.getInfo(draggable.type);
					ElementInformation info = ElementKeyMap.getInfo(tiedToType);

					if (draggable.slotStyle == SLOT_NORMAL) {
						if (draggable.posIndex == tiedToPosIndex) {
							((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Cannot connect to itself"), 0);
						} else {
							System.err.println("[CLIENT][WeaponSlotOverlay] CHECKING EFFECT COMBINATION drag: " + dragInfo + " -on> " + info + "; combi: " + info.isCombiConnectSupport(draggable.type) + "; effect: " + info.isCombiConnectEffect(draggable.type));

							if (slotStyle == SLOT_SUPPORT && info.isCombiConnectSupport(draggable.type)) {
								System.err.println("SUPPORT COMBINATION WITH DRAG: ");
								//dropping into support slot
								ManagerModuleCollection<?, ?, ?> mmc = gs.getShip().getManagerContainer().getModulesControllerMap().get(tiedToType);
								if (mmc != null) {
									ControlBlockElementCollectionManager<?, ?, ?> ecm = mmc.getCollectionManagersMap().get(tiedToPosIndex);
									long e = ecm.getSlaveConnectedElement();
									if (e != Long.MIN_VALUE) {
										long supPos = ElementCollection.getPosIndexFrom4(e);
										short supType = (short) ElementCollection.getType(e);

										gs.getShip().getControlElementMap().removeControllerForElement(tiedToPosIndex,
												supPos, supType);
									}
									gs.getShip().getControlElementMap().switchControllerForElement(tiedToPosIndex, draggable.posIndex, draggable.type);
								}
							}
							if (slotStyle == SLOT_EFFECT && info.isCombiConnectEffect(draggable.type)) {
								System.err.println("EFFECT COMBINATION WITH DRAG: ");
								//dropping into effect slot

								ManagerModuleCollection<?, ?, ?> mmc = gs.getShip().getManagerContainer().getModulesControllerMap().get(tiedToType);
								if (mmc != null) {
									ControlBlockElementCollectionManager<?, ?, ?> ecm = mmc.getCollectionManagersMap().get(tiedToPosIndex);
									long e = ecm.getEffectConnectedElement();
									if (e != Long.MIN_VALUE) {
										long supPos = ElementCollection.getPosIndexFrom4(e);
										short supType = (short) ElementCollection.getType(e);

										gs.getShip().getControlElementMap().removeControllerForElement(tiedToPosIndex,
												supPos, supType);
									}

									gs.getShip().getControlElementMap().switchControllerForElement(tiedToPosIndex, draggable.posIndex, draggable.type);

								}
							}
						}
					}
				} else {
					if (draggable.slotStyle == SLOT_NORMAL && draggable.type > 0 && slotStyle == SLOT_NORMAL && type <= 0) {
						if (shipConfiguration.hasConfigForPos(draggable.posIndex)) {
							int remove = shipConfiguration.removeByPosAndSend(draggable.posIndex);
						}
					}

					if (draggable.slotStyle == SLOT_SUPPORT) {
						draggable.getTiedToType();
						ManagerModuleCollection<?, ?, ?> mmc = gs.getShip().getManagerContainer().getModulesControllerMap().get(draggable.tiedToType);
						if (mmc != null) {
							ControlBlockElementCollectionManager<?, ?, ?> ecm = mmc.getCollectionManagersMap().get(draggable.tiedToPosIndex);
							long e = ecm.getSlaveConnectedElement();
							if (e != Long.MIN_VALUE) {
								long supPos = ElementCollection.getPosIndexFrom4(e);
								short supType = (short) ElementCollection.getType(e);

								gs.getShip().getControlElementMap().removeControllerForElement(draggable.tiedToPosIndex,
										supPos, supType);

								((GameClientState) getState()).getWorldDrawer().getGuiDrawer().getPlayerPanel().getWeaponManagerPanel().setReconstructionRequested(true);
							}
						}

					} else if (draggable.slotStyle == SLOT_EFFECT) {
						draggable.getTiedToType();
						ManagerModuleCollection<?, ?, ?> mmc = gs.getShip().getManagerContainer().getModulesControllerMap().get(draggable.tiedToType);
						if (mmc != null) {
							ControlBlockElementCollectionManager<?, ?, ?> ecm = mmc.getCollectionManagersMap().get(draggable.tiedToPosIndex);
							long e = ecm.getEffectConnectedElement();
							if (e != Long.MIN_VALUE) {
								long supPos = ElementCollection.getPosIndexFrom4(e);
								short supType = (short) ElementCollection.getType(e);

								gs.getShip().getControlElementMap().removeControllerForElement(draggable.tiedToPosIndex,
										supPos, supType);

								((GameClientState) getState()).getWorldDrawer().getGuiDrawer().getPlayerPanel().getWeaponManagerPanel().setReconstructionRequested(true);
							}
						}

					}
				}
			}
		}

		stickyDrag = false;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIOverlay#draw()
	 */
	@Override
	public void draw() {

		if (!init) {
			onInit();
		}
		GlUtil.glPushMatrix();

		GlUtil.glColor4f(1,1,1,1);
		if (ElementKeyMap.isValidType(type) || getState().getController().getInputController().getDragging() == this) {
			super.draw();

			drawLittle(slaveSupportType, 0, 29, 0.5f);
			drawLittle(slaveEffectType, 32, 29, 0.5f);
		} else if (unloadedHighlightSupport || unloadedHighlightEffect) {

			if (getState().getController().getInputController().getDragging() != null && getState().getController().getInputController().getDragging() instanceof WeaponSlotOverlayElement && ElementKeyMap.isValidType(((WeaponSlotOverlayElement) getState().getController().getInputController().getDragging()).type)) {
				if (getState().getController().getInputController().getDragging() != null && ((WeaponSlotOverlayElement) getState().getController().getInputController().getDragging()).posIndex == tiedToPosIndex) {
					selectIcon.getColor().set(.8f, .0f, .0f, 0.18f);
				} else {
					if (unloadedHighlightSupport && ElementKeyMap.getInfo(((WeaponSlotOverlayElement) getState().getController().getInputController().getDragging()).type).isMainCombinationControllerB()) {
						selectIcon.getColor().set(0f, 1f, 0f, 0.18f);
					} else if (unloadedHighlightEffect && ElementKeyMap.getInfo(((WeaponSlotOverlayElement) getState().getController().getInputController().getDragging()).type).isEffectCombinationController()) {
						selectIcon.getColor().set(0f, 1f, 0f, 0.18f);
					} else {
						selectIcon.getColor().set(1, 1, 1, 0.18f);
					}
				}
			} else {
				selectIcon.getColor().set(1, 1, 1, 0.18f);
			}
			selectIcon.setScale(getScale());
			selectIcon.setPos(getPos());
			selectIcon.draw();

			checkMouseInsideWithTransform();

		}
		GlUtil.glPopMatrix();

	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIOverlay#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();
		init = true;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIOverlay#setSpriteSubIndex(int)
	 */
	@Override
	public void setSpriteSubIndex(int spriteSubIndex) {
		if (spriteSubIndex != getSpriteSubIndex()) {
			int layer = spriteSubIndex / 256;
			setSprite(Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath()+"build-icons-" + StringTools.formatTwoZero(layer) + "-16x16-gui-"));
		}
		super.setSpriteSubIndex(spriteSubIndex % 256);
	}

	@Override
	public void drawToolTip() {
		if(!toolTip.isDrawableTooltip()){
			toolTip.onNotDrawTooltip();
			return;
		}
		if (ElementKeyMap.isValidType(type)) {
			String toolTipText = getTooltipText();

			GlUtil.glPushMatrix();
			//			transformTranslation();
			toolTip.setScale(1, 1, 1);
			toolTip.setText(toolTipText);
			toolTip.draw();

			GlUtil.glPopMatrix();
		}
	}

	public void drawHighlight() {

		selectIcon.getColor().set(1, 1, 1, 0.18f);
		selectIcon.setScale(getScale());
		selectIcon.setPos(getPos());
		selectIcon.draw();

	}

	private void drawLittle(short type, int x, int y, float scale) {
		if (ElementKeyMap.isValidType(type)) {
			GlUtil.glPushMatrix();
			transform();
			GlUtil.translateModelview(x, y, 0);
			GlUtil.scaleModelview(scale, scale, scale);

			int bef = getSpriteSubIndex();

			setSpriteSubIndex(ElementKeyMap.getInfo(type).getBuildIconNum());
			sprite.setSelectedMultiSprite(getSpriteSubIndex());
			sprite.drawRaw();

			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			GlUtil.glDisable(GL11.GL_TEXTURE_2D);
			GlUtil.glPopMatrix();
		}
	}

	private String getTooltipText() {
		
		String toolTipText = "";
		if (type == ElementKeyMap.LOGIC_REMOTE_INNER) {
			GameClientState gs = (GameClientState) getState();
			Ship s = gs.getShip();
			if(s == null){
				return "";
			}
			SlotAssignment shipConfiguration = s.getSlotAssignment();

			int slot = this.slot;
			if (slot >= 0) {
				Vector3i pos = shipConfiguration.get(slot);
//					System.err.println("POS: "+pos);
				if (pos != null) {
					SegmentPiece currentPiece = s.getSegmentBuffer().getPointUnsave(pos);

					if (currentPiece != null) {

						long index = currentPiece.getAbsoluteIndexWithType4();
						String tx = currentPiece.getSegmentController().getTextMap().get(index);
						if (tx == null) {
							((ClientSegmentProvider) currentPiece.getSegmentController().getSegmentProvider()).getSendableSegmentProvider().clientTextBlockRequest(index);
							tx = "";
						}
						return tx;
					}
				}
			}

		} else if (ElementKeyMap.isValidType(type)) {
			ElementInformation info = ElementKeyMap.getInfo(type);
			toolTipText = info.getName();

			if (ElementKeyMap.isValidType(slaveSupportType)) {
				ElementInformation supportInfo = ElementKeyMap.getInfo(slaveSupportType);
				toolTipText += Lng.str("\n+S: %s",  supportInfo.getName());
			}
			if (ElementKeyMap.isValidType(slaveEffectType)) {
				ElementInformation supportInfo = ElementKeyMap.getInfo(slaveEffectType);
				toolTipText += Lng.str("\n+E: %s",  supportInfo.getName());
			}
		}
		return toolTipText;
	}

	public void drawToolTipFixed(long t, long duration) {
		
		float completed = (float)t / (float)1200;
		
		if (!ElementKeyMap.isValidType(type)) {
			GlUtil.glPushMatrix();
			this.transform();
			middleClick.draw();
			GlUtil.glPopMatrix();
		}
		
		if(t > 1200){
			return;
		}
		
		boolean draw = false;
		if (ElementKeyMap.isValidType(type)) {
			draw = true;

			if (type != lastType || type == ElementKeyMap.LOGIC_REMOTE_INNER) {
				String toolTipText = getTooltipText();
				toolTip.setText(toolTipText);
				lastType = type;
			}

		}

		if (draw) {
			GlUtil.glPushMatrix();
			this.transform();
			toolTip.setMouseOver(false);
			toolTip.setScale(1, 1, 1);
			toolTip.getPos().y = -13;
			toolTip.getPos().x = 0;
			if (completed < 0.15f) {

				toolTip.setAlpha(completed / 0.15f);
			} else if (completed > 0.8f) {
				toolTip.setAlpha(((1.0f - completed)) / 0.2f);
			}
			toolTip.draw();
			toolTip.setMouseOver(true);
			GlUtil.glPopMatrix();
			toolTip.setAlpha(-1);
		}
	}

	/**
	 * @return the timeDraggingStart
	 */
	public long getTimeDraggingStart() {
		return timeDraggingStart;
	}

	/**
	 * @param timeDraggingStart the timeDraggingStart to set
	 */
	@Override
	public void setTimeDraggingStart(long timeDraggingStart) {
		this.timeDraggingStart = timeDraggingStart;
	}

	@Override
	public void reset() {
		
	}

	/**
	 * @return the type
	 */
	@Override
	public short getType() {
		return type;
	}

	private short getTiedToType() {
		return tiedToType;
	}

	/**
	 * @param tiedToType the tiedToType to set
	 */
	public void setTiedToType(short tiedToType) {
		this.tiedToType = tiedToType;
	}

	public int getSlot() {
		return slot;
	}

	/**
	 * @param slot the slot to set
	 */
	public void setSlot(int slot) {
		this.slot = slot;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(short type, long pos) {
		this.type = type;
		this.posIndex = pos;
	}

	public void setSlaveType(short t) {
		this.slaveSupportType = t;
	}
	//	/**
	//	 * @param posIndex the posIndex to set
	//	 */
	//	public void setPosIndex(long posIndex) {
	//		this.posIndex = posIndex;
	//	}

	public void setEffectType(short t) {
		this.slaveEffectType = t;
	}

	/**
	 * @return the posIndex
	 */
	public long getPosIndex() {
		return posIndex;
	}

	/**
	 * @return the slotStyle
	 */
	public int getSlotStyle() {
		return slotStyle;
	}

	/**
	 * @param slotStyle the slotStyle to set
	 */
	public void setSlotStyle(int slotStyle) {
		this.slotStyle = slotStyle;
	}

	public void setUnloadedHighlightSupport(boolean b) {
		unloadedHighlightSupport = b;
	}

	public void setUnloadedHighlightEffect(boolean b) {
		unloadedHighlightEffect = b;
	}

	/**
	 * @return the tiedToPosIndex
	 */
	public long getTiedToPosIndex() {
		return tiedToPosIndex;
	}

	/**
	 * @param tiedToPosIndex the tiedToPosIndex to set
	 */
	public void setTiedToPosIndex(long tiedToPosIndex) {
		this.tiedToPosIndex = tiedToPosIndex;
	}

}
