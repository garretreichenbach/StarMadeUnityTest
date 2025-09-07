package org.schema.game.client.view.gui.buildtools;

import javax.vecmath.Vector4f;

import org.schema.common.FastMath;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.controller.manager.ingame.SegmentBuildController;
import org.schema.game.client.controller.manager.ingame.SegmentControlManager;
import org.schema.game.client.controller.manager.ingame.ship.ShipControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.client.view.gui.GUI3DBlockElement;
import org.schema.game.client.view.gui.RadialMenuDialogShapesMini;
import org.schema.game.client.view.gui.advanced.tools.BlockOrientationResult;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredGradientRectangle;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUISettingsElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class GUIOrientationSettingElementNew extends GUISettingsElement {

	public final GUI3DBlockElement blockPreview;

	private final GUITextOverlay settingNotAvailable;

	private final GUIOverlay leftArrow;

	private final GUIOverlay rightArrow;

	boolean checkError = true;

	boolean checkError2 = true;

	private boolean init;

	private int lastSlot = -1;

	private int lastOrientation;

	public final GUIColoredGradientRectangle blockBg;

	private int lastSubSlot;

	private int lastSlab;

	private short lastForced;

	private BlockOrientationResult res;

	private boolean mouseInsideBlock;

	public GUIOrientationSettingElementNew(InputState state, BlockOrientationResult r) {
		super(state);
		this.res = r;
		this.setMouseUpdateEnabled(true);
		leftArrow = new GUIOverlay(Controller.getResLoader().getSprite(getState().getGUIPath() + "tools-16x16-gui-"), getState());
		rightArrow = new GUIOverlay(Controller.getResLoader().getSprite(getState().getGUIPath() + "tools-16x16-gui-"), getState());
		blockPreview = new GUI3DBlockElement(state);
		blockPreview.setMouseUpdateEnabled(true);
		settingNotAvailable = new GUITextOverlay(getState());
		settingNotAvailable.setTextSimple("N/A for this block");
		blockBg = new GUIColoredGradientRectangle(state, 52, 52, new Vector4f(0.7f, 0.7f, 0.7f, 0.7f));
		blockBg.gradient.set(0.1f, 0.1f, 0.1f, 0.9f);
		blockBg.setCallback(new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					PlayerInteractionControlManager c = ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
					short type = c.getSelectedTypeWithSub();
					ElementInformation info = ElementKeyMap.getMultiBaseType(type);
					if (info != null) {
						RadialMenuDialogShapesMini m = new RadialMenuDialogShapesMini((GameClientState) getState(), info);
						m.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(357);
					}
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
	}

	public static int getMaxRotation(PlayerInteractionControlManager c) {
		short type = c.getSelectedTypeWithSub();
		if (ElementKeyMap.isValidType(type)) {
			ElementInformation info = ElementKeyMap.getInfo(type);
			return info.getBlockStyle().orientations;
		}
		return 6;
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public void draw() {
		if (checkError2) {
			GlUtil.printGlErrorCritical();
		}
		if (!init) {
			onInit();
		}
		if (checkError2) {
			GlUtil.printGlErrorCritical();
		}
		if (lastSlot != getPlayerInteractionControlManager().getSelectedSlot() || (lastSubSlot != getPlayerInteractionControlManager().getSelectedSubSlot()) || lastOrientation != getPlayerInteractionControlManager().getBlockOrientation() || lastForced != getPlayerInteractionControlManager().getForcedSelect() || lastSlab != getPlayerInteractionControlManager().getBuildToolsManager().slabSize.setting) {
			updateText();
			lastForced = getPlayerInteractionControlManager().getForcedSelect();
			lastSlot = getPlayerInteractionControlManager().getSelectedSlot();
			lastOrientation = getPlayerInteractionControlManager().getBlockOrientation();
			lastSubSlot = getPlayerInteractionControlManager().getSelectedSubSlot();
			lastSlab = getPlayerInteractionControlManager().getBuildToolsManager().slabSize.setting;
		}
		if (checkError2) {
			GlUtil.printGlErrorCritical();
		}
		checkError2 = false;
		short type = getPlayerInteractionControlManager().getSelectedTypeWithSub();
		boolean orien = true;
		if (type <= Element.TYPE_NONE || (ElementKeyMap.getInfo(type).getBlockStyle() == BlockStyle.NORMAL && ElementKeyMap.getInfo(type).individualSides < 4 && !ElementKeyMap.getInfo(type).isOrientatable())) {
			orien = false;
		}
		if (checkError) {
			GlUtil.printGlErrorCritical();
		}
		if (checkError) {
			GlUtil.printGlErrorCritical();
		}
		GlUtil.glPushMatrix();
		transform();
		if (checkError) {
			GlUtil.printGlErrorCritical();
		}
		// blockBg.draw();
		if (checkError) {
			GlUtil.printGlErrorCritical();
		}
		blockBg.checkMouseInsideWithTransform();
		if (blockBg.isInside()) {
			mouseInsideBlock = true;
			blockBg.draw();
		} else {
			mouseInsideBlock = false;
		}
		blockPreview.draw();
		if (checkError) {
			GlUtil.printGlErrorCritical();
		}
		if (orien) {
			leftArrow.draw();
		}
		if (checkError) {
			GlUtil.printGlErrorCritical();
		}
		if (orien) {
			rightArrow.draw();
		}
		if (checkError) {
			GlUtil.printGlErrorCritical();
		}
		GlUtil.glPopMatrix();
		if (checkError) {
			GlUtil.printGlErrorCritical();
		}
		checkError = false;
		GlUtil.glColor4f(1, 1, 1, 1);
	}

	@Override
	public void onInit() {
		blockPreview.onInit();
		leftArrow.setMouseUpdateEnabled(true);
		rightArrow.setMouseUpdateEnabled(true);
		blockBg.onInit();
		leftArrow.setCallback(new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(358);
					int blockOrientation = getPlayerInteractionControlManager().getBlockOrientation();
					short type = getPlayerInteractionControlManager().getSelectedTypeWithSub();
					blockOrientation = FastMath.cyclicModulo(blockOrientation - 1, getMaxRotation(getPlayerInteractionControlManager()));
					getPlayerInteractionControlManager().setBlockOrientation(blockOrientation);
					updateText();
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
		rightArrow.setCallback(new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(359);
					int blockOrientation = getPlayerInteractionControlManager().getBlockOrientation();
					short type = getPlayerInteractionControlManager().getSelectedTypeWithSub();
					blockOrientation = (blockOrientation + 1) % getMaxRotation(getPlayerInteractionControlManager());
					getPlayerInteractionControlManager().setBlockOrientation(blockOrientation);
					updateText();
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
		leftArrow.setSpriteSubIndex(21);
		rightArrow.setSpriteSubIndex(20);
		settingNotAvailable.getPos().x = 6;
		settingNotAvailable.getPos().y = 9;
		blockPreview.getPos().x = leftArrow.getWidth() + blockPreview.getWidth() / 2 - 6;
		blockPreview.getPos().y = blockPreview.getHeight() / 2 - 4;
		rightArrow.getPos().x = leftArrow.getWidth() + blockPreview.getWidth();
		blockBg.setPos(leftArrow.getWidth(), 0, 0);
		leftArrow.getPos().y = (int) (blockPreview.getWidth() / 2 - leftArrow.getWidth() / 2);
		rightArrow.getPos().y = (int) (blockPreview.getWidth() / 2 - rightArrow.getWidth() / 2);
		init = true;
	}

	public SegmentBuildController getActiveBuildController() {
		if (getSegmentControlManager().getSegmentBuildController().isTreeActive()) {
			return getSegmentControlManager().getSegmentBuildController();
		} else if (getShipControllerManager().getSegmentBuildController().isTreeActive()) {
			return getShipControllerManager().getSegmentBuildController();
		}
		return null;
	}

	@Override
	public float getHeight() {
		return blockPreview.getHeight();
	}

	@Override
	public float getWidth() {
		return blockPreview.getWidth() + leftArrow.getWidth() + rightArrow.getWidth();
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	public PlayerInteractionControlManager getPlayerInteractionControlManager() {
		return ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
	}

	public SegmentControlManager getSegmentControlManager() {
		return ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getSegmentControlManager();
	}

	public ShipControllerManager getShipControllerManager() {
		return ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager();
	}

	private void updateText() {
		int blockOrientation = getPlayerInteractionControlManager().getBlockOrientation();
		short type = getPlayerInteractionControlManager().getSelectedTypeWithSub();
		String text;
		if (type > Element.TYPE_NONE) {
			ElementInformation info = ElementKeyMap.getInfo(type);
			if (info.getBlockStyle() != BlockStyle.NORMAL) {
				// BlockShapeAlgorithm blockShapeAlgorithm = BlockShapeAlgorithm.algorithms[blockStyle-1][Element.orientationMapping[blockOrientation]];
				blockPreview.setBlockType(type);
				blockPreview.setSidedOrientation(0);
				blockPreview.setShapeOrientation(blockOrientation);
			} else if (ElementKeyMap.getInfo(type).getIndividualSides() > 3) {
				blockPreview.setBlockType(type);
				blockPreview.setShapeOrientation(0);
				blockPreview.setSidedOrientation(blockOrientation);
			} else if (ElementKeyMap.getInfo(type).orientatable) {
				blockPreview.setBlockType(type);
				blockPreview.setShapeOrientation(0);
				blockPreview.setSidedOrientation(blockOrientation);
			} else {
				getPlayerInteractionControlManager().setBlockOrientation(ElementKeyMap.getInfo(type).getDefaultOrientation());
				blockPreview.setBlockType(type);
				blockPreview.setShapeOrientation(0);
				blockPreview.setSidedOrientation(0);
				blockOrientation = 0;
			}
		} else {
			blockPreview.setBlockType((short) 0);
			blockPreview.setShapeOrientation(0);
			blockPreview.setSidedOrientation(0);
			blockOrientation = 0;
		}
		res.change(blockPreview.getBlockType());
		res.changeOrientation(blockOrientation);
	}

	public boolean isMouseInsideBlock() {
		return mouseInsideBlock;
	}
}
