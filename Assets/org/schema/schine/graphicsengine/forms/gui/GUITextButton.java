package org.schema.schine.graphicsengine.forms.gui;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUINewButtonBackground;
import org.schema.schine.input.InputState;
import org.schema.schine.input.Mouse;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.List;

public class GUITextButton extends GUIAnchor implements GUIButtonInterface, TooltipProviderCallback {

	public static ColorPalletteInterface cp;

	private final Vector4f foregroundColorText;

	private final Vector4f backgroundColor;

	private final Vector4f inactiveColor = new Vector4f(0.3f, 0.3f, 0.3f, 0.9f);

	private final Vector4f mouseOverlayBackgroundColor;

	private final List<Object> texts = new ObjectArrayList<Object>();

	private final List<GUIElement> beforeIcons = new ObjectArrayList<GUIElement>();

	private final List<GUIElement> afterIcons = new ObjectArrayList<GUIElement>();

	public int centeredOffsetY = -1;

	protected GUIColoredAnchor background;

	private GUITextOverlay text;

	private FontInterface font;

	private Vector4f selectColorText;

	private Vector4f pressedColorText;

	private boolean init;

	private Vector3f textPos = new Vector3f();

	private Suspendable suspendable;

	private boolean active = true;

	private boolean centered = true;

	private boolean updateTextPos = true;

	private Vector4f mouseOverlayPressedBackgroundColor;

	private GUIToolTip toolTip;

	private Object2IntOpenHashMap<GUIElement> befAftPosesX = new Object2IntOpenHashMap<GUIElement>();

	private Object2IntOpenHashMap<GUIElement> befAftPosesY = new Object2IntOpenHashMap<GUIElement>();

	private boolean hovering;

	private ColorPalette paletteUsed;

	public GUITextButton(InputState state, int width, int height, Object text, GUICallback callback) {
		this(state, width, height, new Vector4f(0.3f, 0.3f, 0.6f, 0.9f), new Vector4f(0.99f, 0.99f, 0.99f, 1.0f), FontSize.SMALL_14, text, callback);
	}

	public GUITextButton(InputState state, int width, int height, Object text, GUICallback callback, Suspendable s) {
		this(state, width, height, new Vector4f(0.3f, 0.3f, 0.6f, 0.9f), new Vector4f(0.99f, 0.99f, 0.99f, 1.0f), FontSize.SMALL_14, text, callback, s);
	}

	public GUITextButton(InputState state, int width, int height, FontInterface font, Object text, GUICallback callback) {
		this(state, width, height, new Vector4f(0.3f, 0.3f, 0.6f, 0.9f), new Vector4f(0.99f, 0.99f, 0.99f, 1.0f), font, text, callback);
	}

	public GUITextButton(InputState state, int width, int height, Vector4f backgroundColor, Vector4f foregroundColor, FontInterface font, Object text, GUICallback callback) {
		this(state, width, height, backgroundColor, foregroundColor, font, text, callback, null);
	}

	public GUITextButton(InputState state, int width, int height, Vector4f backgroundColor, Vector4f foregroundColor, FontInterface font, Object text, GUICallback callback, Suspendable s) {
		super(state, width, height);
		this.backgroundColor = backgroundColor;
		this.mouseOverlayBackgroundColor = new Vector4f();
		this.mouseOverlayBackgroundColor.set(backgroundColor);
		this.mouseOverlayPressedBackgroundColor = new Vector4f();
		this.mouseOverlayPressedBackgroundColor.set(backgroundColor);
		this.foregroundColorText = foregroundColor;
		this.selectColorText = new Vector4f(0.8f, 0.8f, 1, 1);
		this.pressedColorText = new Vector4f(1, 0.8f, 0.8f, 1);
		this.font = font;
		this.setCallback(callback);
		this.setMouseUpdateEnabled(true);
		texts.add(text);
		this.suspendable = s;
	}

	public GUITextButton(InputState state, int width, int height, ColorPalette color, FontInterface font, Object text, GUICallback callback) {
		this(state, width, height, color, font, text, callback, null);
	}

	public GUITextButton(InputState state, int width, int height, ColorPalette color, FontInterface font, Object text, GUICallback callback, Suspendable s) {
		super(state, width, height);
		this.backgroundColor = new Vector4f();
		this.mouseOverlayBackgroundColor = new Vector4f();
		this.mouseOverlayPressedBackgroundColor = new Vector4f();
		this.foregroundColorText = new Vector4f();
		this.selectColorText = new Vector4f();
		this.pressedColorText = new Vector4f();
		this.font = font;
		this.setCallback(callback);
		this.setMouseUpdateEnabled(true);
		texts.add(text);
		this.suspendable = s;
		setColorPalette(color);
		this.paletteUsed = color;
	}

	public GUITextButton(InputState state, int width, int height, ColorPalette color, Object text, GUICallback callback, Suspendable s) {
		this(state, width, height, color, FontSize.SMALL_14, text, callback, s);
	}

	public GUITextButton(InputState state, int width, int height, ColorPalette color, Object text, GUICallback callback) {
		this(state, width, height, color, FontSize.SMALL_14, text, callback, null);
	}

	public void setColorPalette(ColorPalette c) {
		assert (cp != null);
		cp.setColorPallete(c, this);
	}

	public void removeBeforeIcon(GUIElement e) {
		beforeIcons.remove(e);
		detach(e);
		removeBeforOrAfterIconPos(e);
	}

	public void setBeforOrAfterIconPos(GUIElement e, int x, int y) {
		befAftPosesX.put(e, x);
		befAftPosesY.put(e, y);
	}

	public void removeBeforOrAfterIconPos(GUIElement e) {
		befAftPosesX.removeInt(e);
		befAftPosesY.removeInt(e);
	}

	public void addBeforeIcon(GUIElement e) {
		beforeIcons.add(e);
		attach(e);
	}

	public void addAfterIcon(GUIElement e) {
		afterIcons.add(e);
		attach(e);
	}

	public void removeAfterIcon(GUIElement e) {
		afterIcons.remove(e);
		detach(e);
		removeBeforOrAfterIconPos(e);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#draw()
	 */
	@Override
	public void draw() {
		if (!init) {
			onInit();
		}
		if(isRenderable()) {
			if (!isActive()) {
				text.setColor(foregroundColorText);
				text.getColor().a /= 2f;
				setMouseUpdateEnabled(false);
			} else if ((suspendable != null && (suspendable.isSuspended() || !suspendable.isActive() || suspendable.isHinderedInteraction()))) {
				text.setColor(foregroundColorText);
				setMouseUpdateEnabled(false);
			} else {
				
				if (!isActive()) {
					background.setColor(inactiveColor);
					text.setColor(foregroundColorText);
				} else {
					if (isInside() && (getCallback() == null || !getCallback().isOccluded())) {
						if (!hovering) {
							hovering = true;
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.HOVER)*/
							//AudioController.fireAudioEventID(962); This is annoying, don't do this
						}
						if (Mouse.isPrimaryMouseDownUtility()) {
							background.setColor(mouseOverlayPressedBackgroundColor);
							text.setColor(pressedColorText);
						} else {
							background.setColor(mouseOverlayBackgroundColor);
							text.setColor(selectColorText);
							
						}
					} else {
						background.setColor(getBackgroundColor());
						text.setColor(foregroundColorText);
					}
					
				}
			}
		}
		
		
		if (updateTextPos) {
			updateTextPos = false;
			int yPosIcon = 0;
			int xPosIcon = 0;
			for (int i = 0; i < beforeIcons.size(); i++) {
				final GUIElement guiElement = beforeIcons.get(i);
				xPosIcon += guiElement.getWidth();
				yPosIcon = (int) Math.max(yPosIcon, guiElement.getHeight());
			}
			yPosIcon /= 2;
			if (centered) {
				int xPosIconAft = 0;
				for (int i = 0; i < afterIcons.size(); i++) {
					final GUIElement guiElement = afterIcons.get(i);
					xPosIconAft += afterIcons.get(i).getWidth();
				}
				int totalWidth = xPosIcon + text.getFont().getWidth(texts.get(0).toString()) + xPosIconAft;
				int textOnlyWidth = text.getFont().getWidth(texts.get(0).toString());
				int textHeight = text.getFont().getHeight(texts.get(0).toString());
				int start = (int) (width / 2 - totalWidth / 2);
				int yCent = (int) (height / 2 - textHeight / 2) + centeredOffsetY;
				for (int i = 0; i < beforeIcons.size(); i++) {
					final GUIElement guiElement = beforeIcons.get(i);
					int x = befAftPosesX.getInt(guiElement);
					int y = befAftPosesY.getInt(guiElement);
					guiElement.setPos(start + x, yCent + y, 0);
					start += guiElement.getWidth();
				}
				text.setPos(start, yCent, 0);
				start += textOnlyWidth;
				for (int i = 0; i < afterIcons.size(); i++) {
					final GUIElement guiElement = afterIcons.get(i);
					int x = befAftPosesX.getInt(guiElement);
					int y = befAftPosesY.getInt(guiElement);
					guiElement.setPos(start + x, yCent + y, 0);
					start += guiElement.getWidth();
				}
			} else {
				int startX = (int) textPos.x;
				int startY = (int) textPos.y;
				for (int i = 0; i < beforeIcons.size(); i++) {
					final GUIElement guiElement = beforeIcons.get(i);
					int x = befAftPosesX.getInt(guiElement);
					int y = befAftPosesY.getInt(guiElement);
					guiElement.setPos(startX + x, startY + y, 0);
					startX += guiElement.getWidth();
				}
				text.setPos(startX, startY, 0);
				int textOnlyWidth = text.getFont().getWidth(texts.get(0).toString());
				startX += textOnlyWidth;
				for (int i = 0; i < afterIcons.size(); i++) {
					final GUIElement guiElement = afterIcons.get(i);
					int x = befAftPosesX.getInt(guiElement);
					int y = befAftPosesY.getInt(guiElement);
					guiElement.setPos(startX + x, startY + y, 0);
					startX += guiElement.getWidth();
				}
			}
		}
		
		super.draw();
		if(isMouseUpdateEnabled() && !isInside() && hovering) {
			hovering = false;
		}
			
		
		setMouseUpdateEnabled(true);
		
		
	}

	public String getText() {
		return getTextList().get(0).toString();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#onInit()
	 */
	@Override
	public void onInit() {
		if (init) {
			return;
		}
		text = new GUITextOverlay(font, getState());
		text.setText(texts);
		background = new GUINewButtonBackground(getState(), (int) width, (int) height);
		background.setColor(getBackgroundColor());
		background.attach(text);
		this.attach(background);
		super.onInit();
		init = true;
	}

	public void onResize() {
		background.setWidth(width);
		background.setHeight(height);
	}

	@Override
	public void setHeight(float height) {
		super.setHeight(height);
		if (background != null) {
			background.setHeight(height);
		}
	}

	@Override
	public void setWidth(float width) {
		super.setWidth(width);
		if (background != null) {
			background.setWidth(width);
		}
	}

	/**
	 * @return the active
	 */
	@Override
	public boolean isActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * @return the backgroundColor
	 */
	public Vector4f getBackgroundColor() {
		return backgroundColor;
	}

	/**
	 * @return the foregroundColor
	 */
	public Vector4f getColorText() {
		return foregroundColorText;
	}

	public void setText(Object text) {
		if (!texts.get(0).equals(text)) {
			texts.set(0, text);
			updateTextPos = true;
		}
	}

	public void setTextPos(int x, int y) {
		if (centered || textPos.x != x || textPos.y != y) {
			centered = false;
			textPos.set(x, y, 0);
			updateTextPos = true;
		}
	}

	public void setTextCentered() {
		if (!centered) {
			centered = true;
			updateTextPos = true;
		}
	}

	/**
	 * @return the text
	 */
	public List<Object> getTextList() {
		return text.getText();
	}

	/**
	 * @return the selectedBackgroundColor
	 */
	public Vector4f getBackgroundColorMouseOverlay() {
		return mouseOverlayBackgroundColor;
	}

	@Override
	public void setInvisible(boolean expanded) {
	}

	/**
	 * @return the mouseOverlayPressedColor
	 */
	public Vector4f getBackgroundColorMouseOverlayPressed() {
		return mouseOverlayPressedBackgroundColor;
	}

	/**
	 * @return the pressedColorText
	 */
	public Vector4f getColorTextPressed() {
		return pressedColorText;
	}

	/**
	 * @return the selectColorText
	 */
	public Vector4f getColorTextMouseOver() {
		return selectColorText;
	}

	public enum ColorPalette {

		OK,
		CANCEL,
		TUTORIAL,
		FRIENDLY,
		HOSTILE,
		NEUTRAL,
		TRANSPARENT
	}

	@Override
	public GUIToolTip getToolTip() {
		return toolTip;
	}

	@Override
	public void setToolTip(GUIToolTip toolTip) {
		this.toolTip = toolTip;
	}
}
