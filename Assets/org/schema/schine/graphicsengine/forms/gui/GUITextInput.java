package org.schema.schine.graphicsengine.forms.gui;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.schema.schine.common.TextAreaInput;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.unicode.UnicodeFont;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.input.InputState;
import org.schema.schine.input.Keyboard;

import javax.vecmath.Vector4f;
import java.util.ArrayList;

public class GUITextInput extends GUIElement implements GUICallback {
	public GUIAnchor dependend;
	public int dependendWidthOffset;
	private GUITextOverlay carrier;
	private GUITextOverlay inputBox;
	private TextAreaInput system;
	private boolean firstDraw = true;
	private String preText = "";
	private float time;
	private String lastText;
	private boolean mouseActive = false;
	private boolean drawCarrier = true;
	private GUIHorizontalArea box;
	private boolean textBox;
	private Vector4f colorNormal = new Vector4f(0.98f, 0.98f, 0.98f, 1.0f);
	private GUIScrollablePanel boxScroll;
	private boolean displayAsPassword;

	public GUITextInput(int width, int height, InputState state) {
		this(width, height, state, false);
	}

	public GUITextInput(int width, int height, InputState state, boolean mouseActive) {
		super(state);
		carrier = new GUITextOverlay(state);
		inputBox = new GUITextOverlay(state);

		if (isNewHud()) {
			this.box = new GUIHorizontalArea(state, HButtonType.TEXT_FIELD, width);

		}

		this.mouseActive = mouseActive;

	}

	public GUITextInput(int width, int height, FontInterface font, InputState state) {
		this(width, height, font, state, false);
	}

	public GUITextInput(int width, int height, FontInterface font, InputState state, boolean mouseActive) {
		super(state);
		carrier = new GUITextOverlay(font, state);
		inputBox = new GUITextOverlay(font, state);
		this.mouseActive = mouseActive;

	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (event.pressedLeftMouse()) {

			/*
	         * this is pretty fucked up
			 * but somewhat works
			 */
			int cx = 0;
			int cy = 0;
			StringBuffer sb = new StringBuffer(system.getCache());
			int i = 0;

			i = 0;
			int height = inputBox.getFont().getLineHeight();
			while (cy < getRelMousePos().y - 3) {
				cy = height * i;
				i++;
			}
			i -= 2;
			cy = Math.max(0, Math.min(system.getLineIndex(), i));
			; //number of lines

			int carrierY = 0;
			int se = 0;
			i = 0;
			while (i < cy) {
				int index = sb.indexOf("\n", carrierY + 1);
				if (index >= 0) {
					carrierY = index;
				}
				i++;
			}
			if (carrierY > 0) {
				carrierY = Math.max(0, Math.min(system.getCache().length(), carrierY + 1));
			} else {
				carrierY = 0;
			}

			i = 0;
			while (cx < getRelMousePos().x - 5 && i < sb.length() && carrierY + i < sb.length()) {

				cx = carrier.getFont().getWidth(preText + sb.substring(carrierY, carrierY + i));
				i++;

			}
			cx = i;

			int carrier = carrierY + (Math.max(0, cx));

			carrier = Math.max(0, Math.min(system.getCache().length(), carrier));

			system.setChatCarrier(carrier);
			system.setBufferChanged();
			system.resetSelection();
		}
	}

	@Override
	public void cleanUp() {

	}

	@Override
	public void draw() {
		if (firstDraw) {
			onInit();
		}
		GlUtil.glPushMatrix();
		transform();
		String cache;
		if (system.getLinewrap() > 0) {
			cache = system.getCache();//StringTools.wrap(system.getCache(), system.getLinewrap());
		} else {
			cache = system.getCache();
		}
		if (isNewHud() && textBox && dependend != null) {
			this.box.setWidth((int) (dependend.getWidth() + dependendWidthOffset));
		}
		String sayString;
		if (displayAsPassword) {
			StringBuffer sb = new StringBuffer(cache.length());
			for (int i = 0; i < cache.length(); i++) {
				sb.append("*");
			}
			sayString = preText + sb.toString();
		} else {
			sayString = preText + cache;
		}

		if (isNewHud() && textBox) {

			this.box.draw();
			GlUtil.glPushMatrix();
			GlUtil.translateModelview(4, 4, 0);
		}
		if (system.getCacheSelect().length() > 0) {
			int ys = 0;
			int ye = ys + carrier.getFont().getHeight(preText + system.getCacheSelectStart() + system.getCacheSelect());
			int xs = carrier.getFont().getWidth(preText + system.getCacheSelectStart());
			int xe = carrier.getFont().getWidth(preText + system.getCacheSelectStart() + system.getCacheSelect());

			drawOrthogonalQuad(
					xs - 1,
					ys - 1,
					xe + 1,
					ye + 1);

			inputBox.getText().set(0, sayString);
			inputBox.setColor(colorNormal);
			if (boxScroll != null) {
				boxScroll.setContent(inputBox);
				boxScroll.draw();
			} else {
				inputBox.draw();
			}
			
			String selection = system.getCacheSelect();
			if(displayAsPassword){
				StringBuilder sb = new StringBuilder(selection.length());
				for (int i = 0; i < sb.length(); i++) {
					sb.append("*");
				}
				selection = sb.toString();
			}

			int xTemp = (int) inputBox.getPos().x;
			inputBox.getPos().x = xs;
			inputBox.getText().set(0, selection);
			//inputBox.setColor(0.5f, 0.5f, 0, 1f);
			inputBox.draw();
			inputBox.getPos().x = xTemp;

		} else {
			inputBox.getText().set(0, sayString);
			inputBox.setColor(colorNormal);
			if (boxScroll != null) {
				boxScroll.setContent(inputBox);
				boxScroll.draw();
			} else {
				inputBox.draw();
			}
		}

		float before = carrier.getPos().x;

		if (!system.getCacheCarrier().equals(lastText)) {

			int lastNewline = system.getCacheCarrier().lastIndexOf("\n");

			String substring = lastNewline >= 0 ? system.getCacheCarrier().substring(lastNewline + 1) : system.getCacheCarrier();

			carrier.getPos().x = carrier.getFont().getWidth(preText + substring) - 2;

			carrier.getPos().y = inputBox.getCurrentLineHeight(system.getCarrierLineIndex());//system.getCarrierLineIndex()*carrier.getFont().getLineHeight();

			lastText = new String(system.getCacheCarrier());

		}
//		System.err.println("CARRIERPOS "+carrier.getPos()+"; "+drawCarrier);

		if (before != carrier.getPos().x) {
			time = 0;
			carrier.getColor().a = 1;
		}
		
		if (drawCarrier) {
			getState().setInTextBox(true);
			if (boxScroll != null) {
				boxScroll.setContent(carrier);
				boxScroll.draw();
			} else {
				
				if(Keyboard.isKeyDown(GLFW.GLFW_KEY_TAB)){
					carrier.setColor(1.0f, 0.0f, 0.1f, 1f);
				}else{
					carrier.setColor(colorNormal);
				}
				carrier.draw();
			}
		}
		if (isNewHud() && textBox) {
			GlUtil.glPopMatrix();
		}
		if (isMouseUpdateEnabled()) {
			checkMouseInside();
		}

		GlUtil.glPopMatrix();

	}

	@Override
	public void onInit() {
		if (!firstDraw) {
			return;
		}
		if (textBox) {
			boxScroll = new GUIScrollablePanel(10, 10, this, getState());
			boxScroll.setContent(inputBox);
			boxScroll.setLeftRightClipOnly = true;
			boxScroll.setScrollable(0);
		}
		carrier.setLimitTextDraw(1);
		carrier.setText(new ArrayList(1));
		carrier.getText().add("|");
		carrier.onInit();
		this.attach(carrier);

		inputBox.setLimitTextDraw(1);
		inputBox.setText(new ArrayList(1));
		inputBox.getText().add("");
		inputBox.onInit();
		this.attach(inputBox);

		if (mouseActive) {
			setMouseUpdateEnabled(true);
			setCallback(this);
		}

		firstDraw = false;

	}

	@Override
	protected void doOrientation() {

	}

	@Override
	public float getHeight() {
		if (textBox && isNewHud()) {
			return this.box.getHeight();
		}
		return inputBox.getHeight();
	}

	public void setHeight(int height) {
		if (textBox && isNewHud()) {
			box.setHeight(height);
		} else {
//			inputBox.setHeight(height);
		}
	}

	@Override
	public float getWidth() {
		if (textBox && isNewHud()) {
			return this.box.getWidth();
		}
		return inputBox.getWidth();
	}

	public void setWidth(int width) {
		if (textBox && isNewHud()) {
			box.setWidth(width);
		} else {
//			inputBox.setWidth(width);
		}
	}

	@Override
	public boolean isPositionCenter() {
				return false;
	}

	public void drawOrthogonalQuad(int xs, int ys, int xe, int ye) {

		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		// save the current modelview matrix
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glColor4f(0.2784f, 0.2784f, 0.2784f, 0.9f);
		GL11.glVertex2i(xs, ys);
		GL11.glVertex2i(xs, ye);
		GL11.glVertex2i(xe, ye);
		GL11.glVertex2i(xe, ys);
		GL11.glColor4f(1f, 1f, 1f, 1f);
		GL11.glEnd();

		GlUtil.glDisable(GL11.GL_BLEND);

	}

	@Override
	public String getName() {
		return "textInput";
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.AbstractSceneNode#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void update(Timer timer) {
		time += timer.getDelta();
		if (time > 0.3) {
			if (carrier.getColor().a > 0) {
				carrier.getColor().a = 0;
			} else {
				carrier.getColor().a = 1;
			}
			time = 0;
		}
	}

	public String getPreText() {
		return preText;
	}

	public void setPreText(String preText) {
		this.preText = preText;
	}

	public void setDrawCarrier(boolean b) {
		
		drawCarrier = b;
	}

	/**
	 * @return the inputBox
	 */
	public GUITextOverlay getInputBox() {
		return inputBox;
	}

	/**
	 * @param inputBox the inputBox to set
	 */
	public void setInputBox(GUITextOverlay inputBox) {
		this.inputBox = inputBox;
	}

	/**
	 * @return the carrier
	 */
	public GUITextOverlay getCarrier() {
		return carrier;
	}

	/**
	 * @param carrier the carrier to set
	 */
	public void setCarrier(GUITextOverlay carrier) {
		this.carrier = carrier;
	}

	/**
	 * @return the system
	 */
	public TextAreaInput getTextInput() {
		return system;
	}

	/**
	 * @param system the system to set
	 */
	public void setTextInput(TextAreaInput system) {
		this.system = system;
	}

	public boolean isTextBox() {
		return textBox;
	}

	public void setTextBox(boolean textBox) {
		this.textBox = textBox;
	}

	public UnicodeFont getFont() {
		return inputBox.getFont();
	}

	public void setColor(float r, float g, float b, float a) {
		colorNormal.set(r, g, b, a);
	}

	/**
	 * @return the maxLineWidth
	 */
	public int getMaxLineWidth() {
		return inputBox.getMaxLineWidth();
	}
	public int getTextHeight() {
		return inputBox.getTextHeight();
	}

	public void setDisplayAsPassword(boolean displayAsPassword) {
		this.displayAsPassword = displayAsPassword;
	}

	/**
	 * @return the box
	 */
	public GUIHorizontalArea getBox() {
		return box;
	}

	/**
	 * @param box the box to set
	 */
	public void setBox(GUIHorizontalArea box) {
		this.box = box;
	}

	public FontInterface getFontSize() {
		return inputBox.getFontSize();
	}

	public String getText() {
		return system.getCache();
	}

	public void setText(String text) {
		system.clear();
		system.append(text);
	}
}
