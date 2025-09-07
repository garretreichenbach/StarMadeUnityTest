package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.input.InputState;

public class GUIHorizontalButtonTablePane extends GUIElement {

//	private static final int buttonHeight = 25;
//	private static final float titleHeight = 32;
	public GUIElement dependend;
	public GUIActiveInterface activeInterface;
	public int totalButtonWidthOffset;
	private int columns;
	private int rows;
	private GUIAbstractHorizontalArea[][] buttons;
	private String title;
	private GUITextOverlay tOverlay;
	private int titleWidth;
	

	public GUIHorizontalButtonTablePane(InputState state, int columns, int rows, GUIElement p) {
		this(state, columns, rows, null, p);
	}

	public GUIHorizontalButtonTablePane(InputState state, int columns, int rows, String title, GUIElement p) {
		super(state);
		this.columns = columns;
		this.rows = rows;
		this.dependend = p;
		this.title = title;
	}

	@Override
	public void cleanUp() {
		if(buttons != null){
			for(GUIAbstractHorizontalArea[] r : buttons){
				if(r != null){
					for(GUIAbstractHorizontalArea b : r){
						if(b != null){
							b.cleanUp();
						}
					}
				}
			}
		}
	}

	@Override
	public void draw() {
		GlUtil.glPushMatrix();
		transform();
		if (title != null) {
			tOverlay.setPos((int) (getWidth() / 2 - titleWidth / 2), UIScale.getUIScale().inset, 0);
			tOverlay.draw();
			GlUtil.translateModelview(0, UIScale.getUIScale().scale(32), 0);
		}

		int wPart = (int) (getWidth() / columns);
		for (int x = 0; x < columns; x++) {
			for (int y = 0; y < rows; y++) {
				GUIAbstractHorizontalArea button = buttons[y][x];
				if (button != null) {

					button.setPos(x * wPart, y * UIScale.getUIScale().scale(25), 0);

					if (x == columns - 1 && (columns * wPart + ((button.spacingButtonIndexX - 1) * wPart)) != (int) getWidth()) {
						button.setWidth(wPart + ((int) getWidth() - (columns * wPart + ((button.spacingButtonIndexX - 1) * wPart))));
					} else {
						button.setWidth(wPart*button.spacingButtonIndexX);
					}
					button.draw();
				}
			}
		}

		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
		buttons = new GUIAbstractHorizontalArea[rows][columns];

		if (title != null) {
			this.tOverlay = new GUITextOverlay(FontSize.BIG_20, getState());
			tOverlay.setTextSimple(title);
			tOverlay.onInit();
			this.titleWidth = tOverlay.getFont().getWidth(title);
		}
	}

	public GUIHorizontalArea addButton(int x, int y, Object text, final GUIHorizontalArea.HButtonColor type, GUICallback callback, final GUIActivationCallback actCallback) {
		buttons[y][x] = new GUIHorizontalButton(getState(), type, text, callback, activeInterface, actCallback);
		return (GUIHorizontalButton)buttons[y][x];
	}
	public GUIHorizontalArea addButton(int x, int y, Object text, final GUIHorizontalArea.HButtonType type, GUICallback callback, final GUIActivationCallback actCallback) {
		//INSERTED CODE @103
		if(rows <= y) addRow();
		if(columns <= x) addColumn();
		//
		buttons[y][x] = new GUIHorizontalButton(getState(), type, text, callback, activeInterface, actCallback);
		return (GUIHorizontalButton)buttons[y][x];
	}
	public GUIHorizontalText addText(int x, int y, Object text, FontInterface size, int orientation) {
		return addText(x, y, text, size, null, orientation);
	}
	public GUIHorizontalText addText(int x, int y, Object text, int orientation) {
		return addText(x, y, text, FontSize.MEDIUM_15, null, orientation);
	}
	public GUIHorizontalText addText(int x, int y, Object text) {
		return addText(x, y, text, FontSize.MEDIUM_15, null, ORIENTATION_HORIZONTAL_MIDDLE);
	}
	public GUIHorizontalText addText(int x, int y, Object text, FontInterface size, ColoredInterface colorIface, int orientation) {
		buttons[y][x] = new GUIHorizontalText(getState(), text, size, colorIface);
		((GUIHorizontalText)buttons[y][x]).setAlign(orientation);
		return (GUIHorizontalText)buttons[y][x];
	}

	public void addButton(GUIAbstractHorizontalArea guiHorizontalButton, int x, int y) {
		guiHorizontalButton.activeInterface = activeInterface;
		buttons[y][x] = guiHorizontalButton;

	}

	@Override
	public float getHeight() {
		return rows * UIScale.getUIScale().scale(25) + (title != null ? UIScale.getUIScale().scale(32) : 0);
	}

	@Override
	public float getWidth() {
		return dependend.getWidth() + totalButtonWidthOffset;
	}

	/**
	 * @return the buttons
	 */
	public GUIAbstractHorizontalArea[][] getButtons() {
		return buttons;
	}

	public void setButtonSpacing(int x, int y, int spacing) {
		buttons[y][x].spacingButtonIndexX = spacing;
	}

	//INSERTED CODE @152
	public void addColumn() {
		GUIAbstractHorizontalArea[][] newButtons = new GUIAbstractHorizontalArea[rows][columns + 1];
		System.arraycopy(buttons[0], 0, newButtons[0], 0, buttons[0].length);
		this.buttons = newButtons;
		this.columns ++;
	}

	public void addRow() {
		GUIAbstractHorizontalArea[][] newButtons = new GUIAbstractHorizontalArea[rows + 1][columns];
		System.arraycopy(buttons, 0, newButtons, 0, buttons.length);
		this.buttons = newButtons;
		this.rows ++;
	}
	//
}
