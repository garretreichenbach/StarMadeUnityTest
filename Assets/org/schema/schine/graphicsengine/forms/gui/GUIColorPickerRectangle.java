package org.schema.schine.graphicsengine.forms.gui;

import org.lwjgl.opengl.GL11;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.input.InputState;
import org.schema.schine.input.Mouse;

import javax.vecmath.Vector4f;

/**
 * GUI Element that allows players to select a color by clicking on it.
 *
 * @author TheDerpGamer
 */
public abstract class GUIColorPickerRectangle extends GUIAnchor implements GUICallback, TooltipProviderCallback {

	private final Vector4f selectedColor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f); // Default to white
	public float roundCorners;
	private GUIToolTip toolTip;
	private float saturation = 1.0f; // Default saturation
	private float brightness = 1.0f; // Default brightness

	protected GUIColorPickerRectangle(InputState state, float width, float height) {
		super(state, width, height);
		setCallback(this);
		toolTip = new GUIToolTip(state, "", this);
	}

	@Override
	public void draw() {
		setMouseUpdateEnabled(true);
		if(isActive() && isInside()) { // Shit ass hack cus the mouse callback doesnt work properly
			Vector4f color = calculateColorAtMousePos(Mouse.getX());
			if(toolTip != null) toolTip.setText("Color: " + toHex(color) + "\nR=" + (int) (color.x * 255) + " G=" + (int) (color.y * 255) + " B=" + (int) (color.z * 255));
			if(Mouse.isDown(0)) {
				if(!selectedColor.equals(color)) {
					selectedColor.set(color);
					onPickedColor(color);
				}
			}
		}

		GlUtil.glPushMatrix();
		transform();
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);

		if(roundCorners == 0) {
			GL11.glBegin(GL11.GL_QUAD_STRIP);
			for(int i = 0; i <= getWidth(); i++) {
				float hue = i / getWidth();
				float[] rgb = HSVtoRGB(hue, saturation, brightness);
				GL11.glColor3f(rgb[0], rgb[1], rgb[2]);
				GL11.glVertex2f(i, 0);
				GL11.glVertex2f(i, getHeight());
			}
			GL11.glEnd();
		} else {
			GL11.glBegin(GL11.GL_POLYGON);
			GL11.glVertex2f(0, roundCorners);
			GL11.glVertex2f(0, getHeight() - roundCorners);
			GL11.glVertex2f(roundCorners, getHeight());
			GL11.glVertex2f(getWidth() - roundCorners, getHeight());
			GL11.glVertex2f(getWidth(), getHeight() - roundCorners);
			for(int i = 0; i < getWidth(); i++) {
				float hue = i / getWidth();
				float[] rgb = HSVtoRGB(hue, saturation, brightness);
				GL11.glColor3f(rgb[0], rgb[1], rgb[2]);
				GL11.glVertex2f(i, roundCorners);
			}
			GL11.glVertex2f(getWidth(), roundCorners);
			GL11.glVertex2f(getWidth() - roundCorners, 0);
			GL11.glVertex2f(roundCorners, 0);
			GL11.glVertex2f(roundCorners, roundCorners);
			GL11.glEnd();
		}

		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glColor4f(1, 1, 1, 1);
		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glPopMatrix();
		super.draw();
		GlUtil.glColor4f(0.3f, 0.3f, 0.3f, 0.3f);
	}

	@Override
	public GUIToolTip getToolTip() {
		return toolTip;
	}

	@Override
	public void setToolTip(GUIToolTip toolTip) {
		this.toolTip = toolTip;
	}

	public abstract void onPickedColor(Vector4f color);

	public Vector4f getSelectedColor() {
		return selectedColor;
	}

	/**
	 * Calculates the color at the mouse position based on the hue.
	 */
	private Vector4f calculateColorAtMousePos(float mouseX) {
		//Todo: This doesnt work correctly
		float x = (mouseX - getWorldTranslation().x) + 200; // Get the relative X position within the color picker
		float hue = x / getWidth(); // Normalize to [0, 1]
		float[] rgb = HSVtoRGB(hue, saturation, brightness);
		return new Vector4f(rgb[0], rgb[1], rgb[2], 1.0f); // Return the color as a Vector4f
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {

	}

	@Override
	public boolean isOccluded() {
		return !isActive();
	}

	public String toHex(Vector4f color) {
		int r = (int) (color.x * 255);
		int g = (int) (color.y * 255);
		int b = (int) (color.z * 255);
		return String.format("#%02X%02X%02X", r, g, b);
	}

	public void fromHex(String hex) {
		if(hex.startsWith("#")) hex = hex.substring(1);
		if(hex.length() != 6) throw new IllegalArgumentException("Hex color must be in the format #RRGGBB");
		int r = Integer.parseInt(hex.substring(0, 2), 16);
		int g = Integer.parseInt(hex.substring(2, 4), 16);
		int b = Integer.parseInt(hex.substring(4, 6), 16);
		selectedColor.set(r / 255.0f, g / 255.0f, b / 255.0f, 1.0f);
	}

	private float[] HSVtoRGB(float h, float s, float v) {
		float r = 0, g = 0, b = 0;
		int i = (int) (h * 6);
		float f = h * 6 - i;
		float p = v * (1 - s);
		float q = v * (1 - f * s);
		float t = v * (1 - (1 - f) * s);
		switch(i % 6) {
			case 0:
				r = v;
				g = t;
				b = p;
				break;
			case 1:
				r = q;
				g = v;
				b = p;
				break;
			case 2:
				r = p;
				g = v;
				b = t;
				break;
			case 3:
				r = p;
				g = q;
				b = v;
				break;
			case 4:
				r = t;
				g = p;
				b = v;
				break;
			case 5:
				r = v;
				g = p;
				b = q;
				break;
		}
		return new float[]{r, g, b};
	}

	public void calcRandom() {
		float hue = (float) Math.random();
		float saturation = (float) Math.random();
		float brightness = (float) Math.random();
		this.saturation = saturation;
		this.brightness = brightness;
		float[] rgb = HSVtoRGB(hue, saturation, brightness);
		selectedColor.set(rgb[0], rgb[1], rgb[2], 1.0f);
		if(toolTip != null) toolTip.setText("Selected Color: " + toHex(selectedColor) + "\nR=" + (int) (selectedColor.x * 255) + " G=" + (int) (selectedColor.y * 255) + " B=" + (int) (selectedColor.z * 255));
		onPickedColor(selectedColor);
	}

	public float getSaturation() {
		return saturation * 100.0f; // Convert back to range [0, 100]
	}

	public void setSaturation(float v) {
		saturation = v / 100.0f; // Convert to range [0, 1]
		modSaturation(selectedColor);
		onPickedColor(selectedColor);
	}

	public float getBrightness() {
		return brightness * 100.0f; // Convert back to range [0, 100]
	}

	public void setBrightness(float v) {
		brightness = v / 100.0f; // Convert to range [0, 1]
		modBrightness(selectedColor);
		onPickedColor(selectedColor);
	}

	private void modSaturation(Vector4f color) {
		float[] hsv = RGBtoHSV(color.x, color.y, color.z);
		hsv[1] = saturation; // Set saturation
		float[] rgb = HSVtoRGB(hsv[0], hsv[1], hsv[2]);
		color.set(rgb[0], rgb[1], rgb[2], 1.0f);
	}

	private void modBrightness(Vector4f color) {
		float[] hsv = RGBtoHSV(color.x, color.y, color.z);
		hsv[2] = brightness; // Set brightness
		float[] rgb = HSVtoRGB(hsv[0], hsv[1], hsv[2]);
		color.set(rgb[0], rgb[1], rgb[2], 1.0f);
	}

	private float[] RGBtoHSV(float x, float y, float z) {
		float max = Math.max(x, Math.max(y, z));
		float min = Math.min(x, Math.min(y, z));
		float delta = max - min;
		float h = 0, s = 0;
		if(max != 0) s = delta / max;
		if(delta != 0) {
			if(max == x) h = (y - z) / delta + (y < z ? 6 : 0);
			else if(max == y) h = (z - x) / delta + 2;
			else h = (x - y) / delta + 4;
			h /= 6;
		}
		return new float[]{h, s, max};
	}
}
