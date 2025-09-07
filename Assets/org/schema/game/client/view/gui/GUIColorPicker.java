package org.schema.game.client.view.gui;

import api.utils.other.ObjectArrayStack;
import org.schema.game.client.controller.UserPreferencesManager;
import org.schema.game.client.view.gui.advanced.tools.GUIAdvSlider;
import org.schema.game.client.view.gui.advanced.tools.SliderCallback;
import org.schema.game.client.view.gui.advanced.tools.SliderResult;
import org.schema.schine.common.TextCallback;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActivatableTextBar;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector4f;

/**
 * Color picker GUI element.
 * <br/>Has hex input, recent colors, and RGB sliders.
 *
 * @author Garret Reichenbach
 */
public class GUIColorPicker extends GUIElement {

	private final Vector4f color;
	private final ColorChangeCallback colorChangeCallback;
	private boolean initialized;

	private GUIAdvSlider redSlider;
	private GUIAdvSlider greenSlider;
	private GUIAdvSlider blueSlider;
	private GUIColoredRectangle colorPreview;
	private GUIActivatableTextBar hexInput;
	private final ObjectArrayStack<Integer> recentColors = new ObjectArrayStack<>(10, false);
	private final GUIColoredRectangle[] recentColorRectangles = new GUIColoredRectangle[10];
	
	public GUIColorPicker(InputState state, Vector4f color, ColorChangeCallback colorChangeCallback) {
		super(state);
		this.color = color;
		this.colorChangeCallback = colorChangeCallback;
		loadColorPalette();
	}

	private void addColor(Vector4f color) {
		recentColors.push((int) (color.x * 255) << 16 | (int) (color.y * 255) << 8 | (int) (color.z * 255));
		if(recentColors.size() > 10) recentColors.pop();
		saveColorPalette();
	}
	
	private void removeColor(int index) {
		recentColors.remove(index);
		saveColorPalette();
	}
	
	private Vector4f getColor(int index) {
		if(index >= recentColors.size()) return new Vector4f(0, 0, 0, 1);
		int color = recentColors.toArray()[index];
		return new Vector4f((color >> 16 & 0xFF) / 255.0f, (color >> 8 & 0xFF) / 255.0f, (color & 0xFF) / 255.0f, 1);
	}
	
	private void loadColorPalette() {
		int[] recentColors = UserPreferencesManager.getColorPalette();
		for(int recentColor : recentColors) this.recentColors.push(recentColor);
	}
	
	private void saveColorPalette() {
		int[] recentColors = new int[this.recentColors.size()];
		for(int i = 0; i < recentColors.length; i ++) recentColors[i] = this.recentColors.toArray()[i];
		UserPreferencesManager.setColorPalette(recentColors);
	}
	
	@Override
	public void onInit() {
		//Hex Input
		GUIAnchor textInputAnchor = new GUIAnchor(getState(), getWidth() - 484, getHeight());
		(hexInput = new GUIActivatableTextBar(getState(), FontLibrary.FontSize.MEDIUM_15, 6, 1, getHexColorString(), textInputAnchor, new TextCallback() {
			@Override
			public String[] getCommandPrefixes() {
				return new String[0];
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) {
				return "";
			}

			@Override
			public void onFailedTextCheck(String msg) {

			}

			@Override
			public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {
				if(!entry.matches("[0-9A-Fa-f]{6}")) setHexColorString(entry);
			}

			@Override
			public void newLine() {

			}
		}, t -> t)).onInit();
		textInputAnchor.attach(hexInput);
		attach(textInputAnchor);
		hexInput.setPos(324, 180, 0);

		//Recent Colors
		int yIndex = 0;
		for(int i = 0; i < 10; i ++) {
			GUIColoredRectangle recentColor = new GUIColoredRectangle(getState(), 30, 30, getColor(i));
			recentColor.onInit();
			//Two rows of 5 colors positioned underneath the hex input
			recentColor.setPos(324 + (i % 5) * 35, 215 + (yIndex * 35), 0);
			recentColor.setCallback(new GUICallback() {
				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse()) {
						Vector4f oldColor = new Vector4f(color);
						color.set(recentColor.getColor());
						addColor(color);
						colorPreview.setColor(color);
						hexInput.setText(getHexColorString());
						if(colorChangeCallback != null) colorChangeCallback.colorChanged(oldColor, color);
					}
				}

				@Override
				public boolean isOccluded() {
					return false;
				}
			});
			if(i == 4) yIndex ++;
			recentColorRectangles[i] = recentColor;
			textInputAnchor.attach(recentColor);
		}

		//Color Preview
		(colorPreview = new GUIColoredRectangle(getState(), 300, 100, color)).onInit();
		colorPreview.setPos(15, 180, 0);
		attach(colorPreview);

		//Red Slider
		redSlider = new GUIAdvSlider(getState(), this, new SliderResult() {
			@Override
			public float getDefault() {
				return 255;
			}

			@Override
			public float getMax() {
				return 255;
			}

			@Override
			public float getMin() {
				return 0;
			}

			@Override
			public SliderCallback initCallback() {
				return value -> {
					Vector4f oldColor = new Vector4f(color);
					color.x = value / 255;
					addColor(color);
					colorPreview.setColor(color);
					hexInput.setText(getHexColorString());
					if(colorChangeCallback != null) colorChangeCallback.colorChanged(oldColor, color);
				};
			}

			@Override
			public String getName() {
				return "Red [" + (int) (color.x * 255) + "]";
			}

			@Override
			public String getToolTipText() {
				return "Red [" + (int) (color.x * 255) + "]";
			}
		});
		redSlider.onInit();
		redSlider.setPos(0, 0, 0);
		redSlider.setWidth(getWidth() - 157);
		attach(redSlider);

		//Green Slider
		greenSlider = new GUIAdvSlider(getState(), this, new SliderResult() {
			@Override
			public float getDefault() {
				return 255;
			}

			@Override
			public float getMax() {
				return 255;
			}

			@Override
			public float getMin() {
				return 0;
			}

			@Override
			public SliderCallback initCallback() {
				return value -> {
					Vector4f oldColor = new Vector4f(color);
					color.y = value / 255;
					addColor(color);
					colorPreview.setColor(color);
					hexInput.setText(getHexColorString());
					if(colorChangeCallback != null) colorChangeCallback.colorChanged(oldColor, color);
				};
			}

			@Override
			public String getName() {
				return "Green [" + (int) (color.y * 255) + "]";
			}

			@Override
			public String getToolTipText() {
				return "Green [" + (int) (color.y * 255) + "]";
			}
		});
		greenSlider.onInit();
		greenSlider.setPos(0, 55, 0);
		greenSlider.setWidth(getWidth() - 157);
		attach(greenSlider);

		//Blue Slider
		blueSlider = new GUIAdvSlider(getState(), this, new SliderResult() {
			@Override
			public float getDefault() {
				return 255;
			}

			@Override
			public float getMax() {
				return 255;
			}

			@Override
			public float getMin() {
				return 0;
			}

			@Override
			public SliderCallback initCallback() {
				return value -> {
					Vector4f oldColor = new Vector4f(color);
					color.z = value / 255;
					addColor(color);
					colorPreview.setColor(color);
					hexInput.setText(getHexColorString());
					if(colorChangeCallback != null) colorChangeCallback.colorChanged(oldColor, color);
				};
			}

			@Override
			public String getName() {
				return "Blue [" + (int) (color.z * 255) + "]";
			}

			@Override
			public String getToolTipText() {
				return "Blue [" + (int) (color.z * 255) + "]";
			}
		});
		blueSlider.onInit();
		blueSlider.setPos(0, 110, 0);
		blueSlider.setWidth(getWidth() - 157);
		attach(blueSlider);

		initialized = true;
	}

	private static Vector4f getColorAt(int i) {
		int color = UserPreferencesManager.getColorPalette()[i];
		return new Vector4f((color >> 16 & 0xFF) / 255.0f, (color >> 8 & 0xFF) / 255.0f, (color & 0xFF) / 255.0f, 1);
	}

	@Override
	public void draw() {
		if(!initialized) onInit();
		redSlider.draw();
		greenSlider.draw();
		blueSlider.draw();
		colorPreview.draw();
		hexInput.draw();
		for(GUIColoredRectangle recentColor : recentColorRectangles) recentColor.draw();
	}

	@Override
	public void cleanUp() {
		if(initialized) {
			redSlider.cleanUp();
			greenSlider.cleanUp();
			blueSlider.cleanUp();
			colorPreview.cleanUp();
			hexInput.cleanUp();
			for(GUIColoredRectangle recentColor : recentColorRectangles) recentColor.cleanUp();
			initialized = false;
		}
	}

	@Override
	public float getWidth() {
		return 700;
	}

	@Override
	public float getHeight() {
		return 250;
	}

	public Vector4f getColor() {
		return color;
	}

	public void setColor(Vector4f color) {
		this.color.set(color);
	}

	private String getHexColorString() {
		return String.format("%02X%02X%02X", (int) (color.x * 255), (int) (color.y * 255), (int) (color.z * 255));
	}

	private void setHexColorString(String hex) {
		Vector4f oldColor = new Vector4f(color);
		color.x = Integer.valueOf(hex.substring(0, 2), 16) / 255.0f;
		color.y = Integer.valueOf(hex.substring(2, 4), 16) / 255.0f;
		color.z = Integer.valueOf(hex.substring(4, 6), 16) / 255.0f;
		addColor(color);
		colorPreview.setColor(color);
		redSlider.scrollSetting.settingChanged(color.x * 255);
		greenSlider.scrollSetting.settingChanged(color.y * 255);
		blueSlider.scrollSetting.settingChanged(color.z * 255);
		if(colorChangeCallback != null) colorChangeCallback.colorChanged(oldColor, color);
	}

	public interface ColorChangeCallback {
		void colorChanged(Vector4f oldColor, Vector4f newColor);
	}
}
