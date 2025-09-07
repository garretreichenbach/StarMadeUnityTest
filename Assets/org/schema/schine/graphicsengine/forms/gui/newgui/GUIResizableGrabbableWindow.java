package org.schema.schine.graphicsengine.forms.gui.newgui;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map.Entry;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.schema.schine.graphicsengine.camera.CameraMouseState;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.config.WindowPaletteInterface;
import org.schema.schine.input.InputState;
import org.schema.schine.input.Mouse;
import org.schema.schine.resource.FileExt;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public abstract class GUIResizableGrabbableWindow extends GUIElement implements GUIWindowInterface {

	public static final int LEFT = 1;
	public static final int RIGHT = 2;
	public static final int TOP = 4;
	public static final int BOTTOM = 8;
	public static final int DISTCHECK = 6;
	public static final String windowPositionAndSizeConfig = "windowPositionAndSizeConfig.tag";
	private static final float RIM = 4;
	private static final float MOVE_RIM = 30;
	protected static final Object2ObjectOpenHashMap<String, SaveSizeAndPosition> windowMap = new Object2ObjectOpenHashMap<String, SaveSizeAndPosition>();
	public static int topHeightSubtract;
	public final SaveSizeAndPosition savedSizeAndPosition;
	private final String windowId;
	public GUIActiveInterface activeInterface;
	private GUIOverlay cross;
	private boolean resizable = true;
	protected float width;
	protected float height;
	private int grabbedSize = 0;
	private boolean grabbedMove = false;
	private int curMouseX;
	private int curMouseY;
	private float grabWidth;
	private float grabHeight;
	private float grabPosX;
	private float grabPosY;
	private boolean movable = true;
	private boolean wasMouseDown;
	private boolean mouseOnResizeOrMoveIndicator;
	public GUIResizableGrabbableWindow(InputState state, int initialWidth, int initialHeight, String windowId) {
		this(state, initialWidth, initialHeight, 0, 0, windowId);
	}
	public GUIResizableGrabbableWindow(InputState state, int initialWidth, int initialHeight, int initialPosX, int initalPosY, String windowId) {
		super(state);
		this.windowId = windowId;
		this.width = initialWidth;
		this.height = initialHeight;

		if(windowId != null){
			if (!windowMap.containsKey(windowId)) {
				savedSizeAndPosition = new SaveSizeAndPosition(windowId);
				getPos().set(initialPosX, initalPosY, 0);
				setSavedSizeAndPosFrom();
				windowMap.put(windowId, savedSizeAndPosition);
			} else {
				savedSizeAndPosition = windowMap.get(windowId);
				savedSizeAndPosition.applyTo(this);
			}
		}else{
			savedSizeAndPosition = new SaveSizeAndPosition("");
		}
		cross = new GUIOverlay(Controller.getResLoader().getSprite(getState().getGUIPath()+"UI 16px-8x8-gui-"), getState());
	}
	public static boolean isHidden(String windowId) {
		return windowMap.containsKey(windowId) && windowMap.get(windowId).hidden;
	}
	public static void setHidden(String windowId, boolean hidden) {
		if (windowMap.containsKey(windowId)) {
			SaveSizeAndPosition s = windowMap.get(windowId);
			s.hidden = hidden;
		} 		
	}

	public static Tag savePositions() {
		Tag[] l = new Tag[windowMap.size() + 1];
		l[l.length - 1] = FinishTag.INST;
		int i = 0;
		for (Entry<String, SaveSizeAndPosition> e : windowMap.entrySet()) {
			l[i] = e.getValue().toTagStructure();
			i++;
		}
		Tag mainStruct = new Tag(Type.STRUCT, null, l);

		return new Tag(Type.STRUCT, null, new Tag[]{mainStruct, FinishTag.INST});
	}
	
	public abstract WindowPaletteInterface getWindowPalette();
	
	public static void write() {
		File windowPosFile = new FileExt(windowPositionAndSizeConfig);

		Tag posTag = savePositions();

		try {
			posTag.writeTo(new BufferedOutputStream(new FileOutputStream(windowPosFile)), true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void read() {
		File windowPosFile = new FileExt(windowPositionAndSizeConfig);

		try {
			Tag readFrom = Tag.readFrom(new BufferedInputStream(new FileInputStream(windowPosFile)), true, false);
			loadPositions(readFrom);
		} catch (FileNotFoundException e) {
			System.err.println("[GUI] NO STARMADE-WINDOW CONFIG FOUND. Using defaults");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void loadPositions(Tag readFrom) {
		try {
			Tag[] vGlobal = (Tag[]) readFrom.getValue();
			Tag[] saved = (Tag[]) vGlobal[0].getValue();
			for (int i = 0; i < saved.length && saved[i].getType() != Type.FINISH; i++) {
				SaveSizeAndPosition o = new SaveSizeAndPosition();
				o.fromTagStructure(saved[i]);
				windowMap.put(o.id, o);
			}
		} catch (NullPointerException e) {
			e.printStackTrace();

			GLFrame.processErrorDialogException(
					new IOException("Your window position/size save file somehow got corrupted. Continue to reset the positions. \nPlease send in a report describing how you exited the last game (did it crash?)."), null);
		}
	}

	@Override
	public void onInit() {
		cross.onInit();
		cross.setUserPointer("X");
		cross.getSprite().setTint(new Vector4f(1, 1, 1, 1));
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#doOrientation()
	 */
	@Override
	public void doOrientation() {
		if(savedSizeAndPosition.id != null && savedSizeAndPosition.id.length() > 0){
			if (savedSizeAndPosition.newPanel) {
				super.doOrientation();
				setSavedSizeAndPosFrom();
				savedSizeAndPosition.newPanel = false;
			} else {
				savedSizeAndPosition.applyTo(this);
			}
		}
	}

	@Override
	public float getHeight() {
		return height;
	}

	@Override
	public float getWidth() {
		return width;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#orientate(int)
	 */
	@Override
	public void orientate(int orientation) {
		if (savedSizeAndPosition.newPanel) {
			super.orientate(orientation);
			setSavedSizeAndPosFrom();
			savedSizeAndPosition.newPanel = false;
		} else {
			savedSizeAndPosition.applyTo(this);
		}

	}
	protected void setSavedSizeAndPosFrom(){
		savedSizeAndPosition.setFrom(width, height, getPos(), false);
	}
	@Override
	public boolean isActive() {
		return activeInterface == null || activeInterface.isActive();
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(float width) {
		this.width = width;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(float height) {
		this.height = height;
	}

	protected void drawCross(int xFromRight, int yFromTop) {
		if (cross.isInside() && (cross.getCallback() == null || !cross.getCallback().isOccluded()) && isActive()) {
			cross.getSprite().getTint().set(1.0f, 1.0f, 1.0f, 1.0f);
		} else {
			cross.getSprite().getTint().set(0.8f, 0.8f, 0.8f, 1.0f);
		}
		cross.setMouseUpdateEnabled(true);
		cross.setSpriteSubIndex(0);
		cross.setPos(width + xFromRight, yFromTop, 0);

		cross.draw();
		cross.getSprite().getTint().set(1.0f, 1.0f, 1.0f, 1.0f);

		GlUtil.glColor4fForced(1, 1, 1, 1);
	}

	public GUIOverlay getCloseCross() {
		return cross;
	}

	public void reset() {
		if (!GlUtil.isColorMask()) {
			return;
		}
		wasMouseDown = false;
		grabbedSize = 0;
		grabbedMove = false;
	}

	protected void checkGrabbedResize() {
		if (!GlUtil.isColorMask()) {
			return;
		}
		int inset = getInset();
		
		if (isResizable() && Mouse.isPrimaryMouseDownUtility()) {
			if (grabbedSize == 0 && !wasMouseDown) {
				WindowPaletteInterface w = getWindowPalette();
				//check rims
				if(isRelMouseInside()){
					if (Math.abs(getRelMousePos().x - w.getLeftModifierOffset().x) < RIM) {
						grabbedSize |= LEFT;
						if (Math.abs(getRelMousePos().y - w.getTopModifierOffset().y) < inset) {
							grabbedSize |= TOP;
						}
						if (Math.abs(getRelMousePos().y - height - w.getBottomModifierOffset().y) < inset) {
							grabbedSize |= BOTTOM;
						}
					}
					if (Math.abs(getRelMousePos().y - w.getTopModifierOffset().y) < RIM) {
						grabbedSize |= TOP;
						if (Math.abs(getRelMousePos().x - w.getLeftModifierOffset().x) < inset) {
							grabbedSize |= LEFT;
						}
						if (Math.abs(getRelMousePos().x - width - w.getRightModifierOffset().x) < inset) {
							grabbedSize |= RIGHT;
						}
					}
					if (Math.abs(getRelMousePos().x - width - w.getRightModifierOffset().x) < RIM) {
						grabbedSize |= RIGHT;
						if (Math.abs(getRelMousePos().y - w.getTopModifierOffset().y) < inset) {
							grabbedSize |= TOP;
						}
						if (Math.abs(getRelMousePos().y - height - w.getBottomModifierOffset().y) < inset) {
							grabbedSize |= BOTTOM;
						}
					}
					if (Math.abs(getRelMousePos().y - height - w.getBottomModifierOffset().y) < RIM) {
						grabbedSize |= BOTTOM;
						if (Math.abs(getRelMousePos().x - w.getLeftModifierOffset().x) < inset) {
							grabbedSize |= LEFT;
						}
						if (Math.abs(getRelMousePos().x - width - w.getRightModifierOffset().x) < inset) {
							grabbedSize |= RIGHT;
						}
					}
				}
				this.curMouseX = Mouse.getX();
				this.curMouseY = Mouse.getY();

				this.grabWidth = width;
				this.grabHeight = height;
				this.grabPosX = getPos().x;
				this.grabPosY = getPos().y;
			} else {
				if (grabbedSize != 0) {
					doResize();
				}
			}

		} else {
			//released
			grabbedSize = 0;

		}
		if (isMovable() && Mouse.isPrimaryMouseDownUtility()) {
			if (!grabbedMove && !wasMouseDown) {
				if (grabbedSize == 0) {
					WindowPaletteInterface w = getWindowPalette();
					
//					System.err.println("SDSD "+getRelMousePos().y);
					
					
					if (isRelMouseInside() && getRelMousePos().y-w.getMoveModifierOffset().y >= RIM && getRelMousePos().y-w.getMoveModifierOffset().y <= MOVE_RIM) {
						grabbedMove = true;

					}

					this.curMouseX = Mouse.getX();
					this.curMouseY = Mouse.getY();

					this.grabWidth = width;
					this.grabHeight = height;
					this.grabPosX = getPos().x;
					this.grabPosY = getPos().y;
				}
			} else {
				if (grabbedMove) {
					doMove();
				}
			}
		} else {
			//released
			grabbedMove = false;
		}

		wasMouseDown = Mouse.isPrimaryMouseDownUtility();

		//make sure the window is not going outside

		if (width > GLFrame.getWidth()) {
			width = GLFrame.getWidth();
		}
		if (getHeight() > GLFrame.getHeight() - getTopHeightSubtract()) {
			height = GLFrame.getHeight() - getTopHeightSubtract();
		}
		if (getPos().x < 0) {
			getPos().x = 0;
		}
		if (getPos().y < getTopHeightSubtract()) {
			getPos().y = getTopHeightSubtract();
		}

		if (getPos().x + width > GLFrame.getWidth()) {
			getPos().x = GLFrame.getWidth() - width;
		}

		if (getPos().y + getHeight() > GLFrame.getHeight()) {
			getPos().y = GLFrame.getHeight() - getHeight();
		}

		setSavedSizeAndPosFrom();
	}
	public boolean isRelMouseInside(){
		return getRelMousePos().x >= -DISTCHECK && getRelMousePos().x <= width+DISTCHECK && getRelMousePos().y >= -DISTCHECK && getRelMousePos().y <= height+DISTCHECK;
	}
	public int getTopHeightSubtract(){
		return topHeightSubtract;
	}
	public boolean isMouseOnAnyDragElement() {
		return mouseOnResizeOrMoveIndicator;
	}
	protected void drawMouseResizeIndicators() {
		mouseOnResizeOrMoveIndicator = false;
		if (!GlUtil.isColorMask()) {
			return;
		}
		if(!isRelMouseInside()){
			return;
		}
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		if (isResizable() || isMovable()) {
			
			int inset = getInset();
			int over = 0;
			//mouse draw
			
			if (isResizable() && !cross.isInside()) {
				WindowPaletteInterface w = getWindowPalette();
				if (Math.abs(getRelMousePos().x - w.getLeftModifierOffset().x) < RIM) {
					over |= LEFT;
					if (Math.abs(getRelMousePos().y - w.getTopModifierOffset().y) < inset) {
						over |= TOP;
					}
					if (Math.abs(getRelMousePos().y - height - w.getBottomModifierOffset().y) < inset) {
						over |= BOTTOM;
					}
				}
				if (Math.abs(getRelMousePos().y - w.getTopModifierOffset().y) < RIM) {
					over |= TOP;
					if (Math.abs(getRelMousePos().x - w.getLeftModifierOffset().x) < inset) {
						over |= LEFT;
					}
					if (Math.abs(getRelMousePos().x - width - w.getRightModifierOffset().x) < inset) {
						over |= RIGHT;
					}
				}
				if (Math.abs(getRelMousePos().x - width - w.getRightModifierOffset().x) < RIM) {
					over |= RIGHT;
					if (Math.abs(getRelMousePos().y - w.getTopModifierOffset().y) < inset) {
						over |= TOP;
					}
					if (Math.abs(getRelMousePos().y - height - w.getBottomModifierOffset().y) < inset) {
						over |= BOTTOM;
					}
				}
				if (Math.abs(getRelMousePos().y - height - w.getBottomModifierOffset().y) < RIM) {
					over |= BOTTOM;
					if (Math.abs(getRelMousePos().x - w.getLeftModifierOffset().x) < inset) {
						over |= LEFT;
					}
					if (Math.abs(getRelMousePos().x - width - w.getRightModifierOffset().x) < inset) {
						over |= RIGHT;
					}
				}
			}
			boolean grabMove = isMovable() && over == 0 && getRelMousePos().y >= RIM && getRelMousePos().y <= MOVE_RIM;
			GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			GlUtil.glDisable(GL11.GL_DEPTH_TEST);
			GlUtil.glDisable(GL11.GL_TEXTURE_2D);

			if (grabMove) {
				mouseOnResizeOrMoveIndicator = true;
				Vector2f o = getWindowPalette().getMoveModifierOffset();
				GL11.glBegin(GL11.GL_QUADS);
				GL11.glVertex2f((int)(o.x+width / 2 - inset), (int)(o.y+0 + MOVE_RIM / 2));
				GL11.glVertex2f((int)(o.x+width / 2 - inset), (int)(o.y+0 + RIM + MOVE_RIM / 2));
				GL11.glVertex2f((int)(o.x+width / 2 + inset), (int)(o.y+0 + RIM + MOVE_RIM / 2));
				GL11.glVertex2f((int)(o.x+width / 2 + inset), (int)(o.y+0 + MOVE_RIM / 2));
				GL11.glEnd();
			}

			if (over > 0) {
				mouseOnResizeOrMoveIndicator = true;
				int wm = ((int) (width / 2) - inset / 2);
				int wh = ((int) (getHeight() / 2) - inset / 2);

				GL11.glBegin(GL11.GL_QUADS);
				GlUtil.glColor4fForced(1, 1, 1, 1);
				if ((over & LEFT) == LEFT && (over & TOP) == TOP) {
					
					Vector2f o = new Vector2f(getWindowPalette().getLeftModifierOffset());
					o.add(getWindowPalette().getTopModifierOffset());
					
					GL11.glVertex2f(o.x+0, o.y+0);
					GL11.glVertex2f(o.x+0, o.y+0 + inset);
					GL11.glVertex2f(o.x+0 + RIM, o.y+0 + inset);
					GL11.glVertex2f(o.x+0 + RIM, o.y+0);

					GL11.glVertex2f(o.x+0, o.y+0);
					GL11.glVertex2f(o.x+0, o.y+0 + RIM);
					GL11.glVertex2f(o.x+0 + inset, o.y+0 + RIM);
					GL11.glVertex2f(o.x+0 + inset, o.y+0);
				} else if ((over & RIGHT) == RIGHT && (over & TOP) == TOP) {
					
					Vector2f o = new Vector2f(getWindowPalette().getRightModifierOffset());
					o.add(getWindowPalette().getTopModifierOffset());
					
					GL11.glVertex2f(o.x+width, o.y+0);
					GL11.glVertex2f(o.x+width, o.y+0 + inset);
					GL11.glVertex2f(o.x+width + RIM, o.y+0 + inset);
					GL11.glVertex2f(o.x+width + RIM, o.y+0);

					GL11.glVertex2f(o.x+width - inset, o.y+0);
					GL11.glVertex2f(o.x+width - inset, o.y+0 + RIM);
					GL11.glVertex2f(o.x+width, o.y+0 + RIM);
					GL11.glVertex2f(o.x+width, o.y+0);

				} else if ((over & RIGHT) == RIGHT && (over & BOTTOM) == BOTTOM) {
					Vector2f o = new Vector2f(getWindowPalette().getRightModifierOffset());
					o.add(getWindowPalette().getBottomModifierOffset());
					
					
					GL11.glVertex2f(o.x+width, o.y+height - inset);
					GL11.glVertex2f(o.x+width, o.y+height);
					GL11.glVertex2f(o.x+width + RIM, o.y+height);
					GL11.glVertex2f(o.x+width + RIM, o.y+height - inset);

					GL11.glVertex2f(o.x+width - inset, o.y+height - RIM);
					GL11.glVertex2f(o.x+width - inset, o.y+height);
					GL11.glVertex2f(o.x+width, o.y+height);
					GL11.glVertex2f(o.x+width, o.y+height - RIM);
				} else if ((over & LEFT) == LEFT && (over & BOTTOM) == BOTTOM) {
					Vector2f o = new Vector2f(getWindowPalette().getLeftModifierOffset());
					o.add(getWindowPalette().getBottomModifierOffset());
					
					GL11.glVertex2f(o.x+0, o.y+height - inset);
					GL11.glVertex2f(o.x+0, o.y+height);
					GL11.glVertex2f(o.x+0 + RIM, o.y+height);
					GL11.glVertex2f(o.x+0 + RIM, o.y+height - inset);

					GL11.glVertex2f(o.x+0, o.y+height - RIM);
					GL11.glVertex2f(o.x+0, o.y+height);
					GL11.glVertex2f(o.x+0 + inset, o.y+height);
					GL11.glVertex2f(o.x+0 + inset, o.y+height - RIM);

				} else if ((over & TOP) == TOP) {
					Vector2f o = getWindowPalette().getTopModifierOffset();
					GL11.glVertex2f(o.x+wm, o.y+0);
					GL11.glVertex2f(o.x+wm, o.y+0 + RIM);
					GL11.glVertex2f(o.x+wm + inset, o.y+0 + RIM);
					GL11.glVertex2f(o.x+wm + inset, o.y+0);
				} else if ((over & BOTTOM) == BOTTOM) {
					Vector2f o = getWindowPalette().getBottomModifierOffset();
					GL11.glVertex2f(o.x+wm, o.y+height);
					GL11.glVertex2f(o.x+wm, o.y+height + RIM);
					GL11.glVertex2f(o.x+wm + inset, o.y+height + RIM);
					GL11.glVertex2f(o.x+wm + inset, o.y+height);
				} else if ((over & LEFT) == LEFT) {
					Vector2f o = getWindowPalette().getLeftModifierOffset();
					GL11.glVertex2f(o.x+0, o.y+wh);
					GL11.glVertex2f(o.x+0, o.y+wh + inset);
					GL11.glVertex2f(o.x+0 + RIM, o.y+wh + inset);
					GL11.glVertex2f(o.x+0 + RIM, o.y+wh);
				} else if ((over & RIGHT) == RIGHT) {
					Vector2f o = getWindowPalette().getRightModifierOffset();
					GL11.glVertex2f(o.x+width, o.y+wh);
					GL11.glVertex2f(o.x+width, o.y+wh + inset);
					GL11.glVertex2f(o.x+width + RIM, o.y+wh + inset);
					GL11.glVertex2f(o.x+width + RIM, o.y+wh);
				}

				GL11.glEnd();
			}
		}
	}

	protected abstract int getMinWidth();

	protected abstract int getMinHeight();

	private void doMove() {
		int xDist = curMouseX - Mouse.getX();
		int yDist = curMouseY - Mouse.getY();
		int maxX = (int) width;
		int maxY = (int) height;
		if (isMovable()) {
			getPos().x = (int) grabPosX - xDist;
			getPos().y = (int) grabPosY - yDist;

			getPos().x = Math.max(getPos().x, 0);
			getPos().y = Math.max(getPos().y, getTopHeightSubtract());

			getPos().x = Math.min(getPos().x, GLFrame.getWidth() - maxX);
			getPos().y = Math.min(getPos().y, (GLFrame.getHeight()) - maxY);
		}
	}

	private void doResize() {

		int xDist = curMouseX - Mouse.getX();
		int yDist = curMouseY - Mouse.getY();

		int minX = getMinWidth();
		int minY = getMinHeight();
		if ((grabbedSize & LEFT) == LEFT) {

			if (grabWidth + xDist < minX) {
				int dist = (int) (minX - (grabWidth + xDist));
				xDist += dist;
			}

			getPos().x = (int) (grabPosX - xDist);
			width = (int) (grabWidth + xDist);
		}
		if ((grabbedSize & RIGHT) == RIGHT) {

			if (grabWidth - xDist < minX) {
				int dist = (int) (minX - (grabWidth - xDist));
				xDist -= dist;
			}

			width = (int) (grabWidth - xDist);
		}
		if ((grabbedSize & TOP) == TOP) {

			if (grabHeight + yDist < minY) {
				int dist = (int) (minY - (grabHeight + yDist));
				System.err.println("DIS "+dist);
				yDist += dist;
			}
			
			if ((int) (grabPosY - yDist) < getTopHeightSubtract()) {
				yDist += ((int) (grabPosY - yDist) + getTopHeightSubtract());
			}
			getPos().y = (int) (grabPosY - yDist);
			height = (int) (grabHeight + yDist);

		}
		if ((grabbedSize & BOTTOM) == BOTTOM) {

			if (grabHeight - yDist < minY) {
				int dist = (int) (minY - (grabHeight - yDist));
				yDist -= dist;
			}

			height = (int) (grabHeight - yDist);
		}
	}

	@Override
	public abstract int getTopDist();

	/**
	 * @return the resizable
	 */
	public boolean isResizable() {
		if (!isActive()) {
			return false;
		}
		return resizable && (!CameraMouseState.isGrabbed() || canMoveAndResizeWhenMouseGrabbed());
	}

	public boolean canMoveAndResizeWhenMouseGrabbed() {
		//can be overwritten (e.g. window that comes up with tab)
		return false;
	}

	/**
	 * @param resizable the resizable to set
	 */
	public void setResizable(boolean resizable) {
		this.resizable = resizable;
	}

	/**
	 * @return the movable
	 */
	public boolean isMovable() {
		if (!isActive()) {
			return false;
		}
		return movable && (!CameraMouseState.isGrabbed() || canMoveAndResizeWhenMouseGrabbed());
	}

	/**
	 * @param movable the movable to set
	 */
	public void setMovable(boolean movable) {
		this.movable = movable;
	}

	public void setCloseCallback(GUICallback guiCallback) {
		cross.setCallback(guiCallback);
	}
	public String getWindowId() {
		return windowId;
	}
	
	
}
