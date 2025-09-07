package org.schema.game.client.view.gui;

import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.AbstractSceneNode;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.font.FontStyle;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIResizableGrabbableWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

import java.util.ArrayList;

public class GUIInputPanel extends GUIElement implements GUIInputInterface {

	public static final int TEXT_PANEL = 0;
	public static final int SMALL_PANEL = 1;
	public static final int BIG_PANEL = 2;
	public GUIResizableGrabbableWindow background;
	protected GUITextOverlay infoText;
	protected GUITextOverlay errorText;
	protected boolean autoOrientate = true;
	private GUITextButton buttonOK;
	private GUIElement buttonCancel;
	private final GUITextOverlay descriptionText;
	private boolean okButton = true;
	private boolean cancelButton = true;
	private String okButtonText = Lng.str("OK");
	private String secondOptionButtonText = "";
	private String cancelButtonText = Lng.str("CANCEL");
	private long timeError;
	private long timeErrorShowed;
	private boolean firstDraw = true;
	private int infoTextSize;
	private boolean titleOnTop;
	protected GUIScrollablePanel scrollableContent;
	private GUITextButton buttonSecondOption;
	private int secondOptionWidth = 85;
	public GUIInputPanel(String windowId, InputState state, int width, int height, GUICallback guiCallback, GUIResizableGrabbableWindow background) {
		super(state);
		this.setCallback(guiCallback);
		infoText = new GUITextOverlay(FontSize.BIG_20, state);
		descriptionText = new GUITextOverlay(FontStyle.def.fontSize, state){

			@Override
			public void draw() {
				super.draw();
//				setWidth(getMaxLineWidth());
//				setHeight(getTextHeight());
			}
			
		};
		errorText = new GUITextOverlay(state);


		init(guiCallback, "", "", background);
	}
	public GUIInputPanel(String windowId, InputState state, int width, int height, GUICallback guiCallback, Object info, Object description, GUIResizableGrabbableWindow background, FontStyle style) {
		super(state);
		this.setCallback(guiCallback);
		infoText = new GUITextOverlay(FontSize.BIG_20, state);
		descriptionText = new GUITextOverlay(style.fontSize, state){

			@Override
			public void draw() {
				super.draw();
//				setWidth(getMaxLineWidth());
//				setHeight(getTextHeight());
			}
			
		};
		errorText = new GUITextOverlay(state);


		init(guiCallback, info, description, background);
	}

	public GUIInputPanel(String windowId, InputState state, GUICallback guiCallback, Object info, Object description, FontStyle style) {
		this(windowId, state, UIScale.getUIScale().scale(420), UIScale.getUIScale().scale(180), guiCallback, info, description, style);
	}

	public GUIInputPanel(String windowId, InputState state, int width, int height, GUICallback guiCallback, Object info, Object description, FontStyle style) {
		this(windowId, state, width, height, guiCallback, info, description, new GUIDialogWindow(state, width, height, windowId), style);
	}

	public GUIInputPanel(String windowId, InputState state, GUICallback guiCallback, Object info, Object description) {
		this(windowId, state, UIScale.getUIScale().scale(420), UIScale.getUIScale().scale(180), guiCallback, info, description);
	}

	public GUIInputPanel(String windowId, InputState state, int width, int height, GUICallback guiCallback, Object info, Object description) {
		this(windowId, state, width, height, guiCallback, info, description, new GUIDialogWindow(state, width, height, windowId), FontStyle.def);
	}

	public void setOkButtonText(String string) {
		this.okButtonText = string;
	}
	public void setSecondOptionButtonWidth(int width) {
		this.secondOptionWidth = width;
		buttonSecondOption.setWidth(width);
	}
	public void setSecondOptionButtonText(String string) {
		this.secondOptionButtonText = string;
	}

	public void setCancelButtonText(String string) {
		this.cancelButtonText = string;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#doOrientation()
	 */
	@Override
	protected void doOrientation() {
		if (isNewHud()) {
			background.doOrientation();
		} else {
			super.doOrientation();
		}
	}

	@Override
	public float getHeight() {
		return 256;
	}

	@Override
	public float getWidth() {
		return 256;
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	@Override
	public void orientate(int orientation) {
		background.orientate(orientation);
	}

	private void init(GUICallback guiCallback, Object info, Object description, GUIResizableGrabbableWindow bg) {

		buttonOK = new GUITextButton(getState(), UIScale.getUIScale().scale(85), UIScale.getUIScale().scale(20), ColorPalette.OK, new Object() {
			@Override
			public String toString() {
				return okButtonText;
			}
		}, guiCallback);
		buttonSecondOption = new GUITextButton(getState(), UIScale.getUIScale().scale(secondOptionWidth), UIScale.getUIScale().scale(20), ColorPalette.OK, new Object() {
			@Override
			public String toString() {
				return secondOptionButtonText;
			}
		}, guiCallback);
		buttonCancel = new GUITextButton(getState(), UIScale.getUIScale().scale(85), UIScale.getUIScale().scale(20), ColorPalette.CANCEL, new Object() {
			@Override
			public String toString() {
				return cancelButtonText;
			}
		}, guiCallback);

//		background = new GUIMainWindow(getState(), 600, 300);
		background = bg;
		background.activeInterface = () -> getState().getController().getPlayerInputs().isEmpty() || (getState().getController().getPlayerInputs().get(getState().getController().getPlayerInputs().size() - 1).getInputPanel() == GUIInputPanel.this);

		buttonOK.setCallback(guiCallback);
		buttonOK.setUserPointer("OK");
		buttonOK.setMouseUpdateEnabled(true);
		
		buttonSecondOption.setCallback(guiCallback);
		buttonSecondOption.setUserPointer("SECOND_OPTION");
		buttonSecondOption.setMouseUpdateEnabled(true);

		buttonCancel.setCallback(guiCallback);
		buttonCancel.setUserPointer("CANCEL");
		buttonCancel.setMouseUpdateEnabled(true);

		background.setCloseCallback(guiCallback);

		infoText.setTextSimple(info);
		descriptionText.setTextSimple(description);

		ArrayList<Object> te = new ArrayList<Object>();
		errorText.setText(te);

	}

	@Override
	public void setCallback(GUICallback callback) {
		if(buttonCancel != null){
			buttonCancel.setCallback(callback);
		}
		if(buttonOK != null){
			buttonOK.setCallback(callback);
		}
		if(buttonSecondOption != null){
			buttonSecondOption.setCallback(callback);
		}
		
		if(background != null){
			background.setCloseCallback(callback);
		}
		super.setCallback(callback);
	}
	@Override
	public void cleanUp() {
		background.cleanUp();
		infoText.cleanUp();
		buttonOK.cleanUp();
		buttonSecondOption.cleanUp();
		buttonCancel.cleanUp();
	}
	public void setContentInScrollable(GUIElement e){
		scrollableContent.setContent(e);
	}
	public GUIAnchor getContent(){
		return ((GUIDialogWindow) background).getMainContentPane().getContent(0);
	}
	protected void adaptSizes(){
		int wPlus = 8;
		int hPlus = 12;
		scrollableContent.setWidth(((GUIDialogWindow) background).getInnerWidth() + wPlus);
		scrollableContent.setHeight(((GUIDialogWindow) background).getMainContentPane().getContent(0).getHeight());
		
		getContent().setWidth(Math.max(((GUIDialogWindow) background).getInnerWidth(), contentInterface.getWidth() ));
		getContent().setHeight(Math.max(((GUIDialogWindow) background).getInnerHeigth()-((GUIDialogWindow) background).getInset(), contentInterface.getHeight()));
	}
	@Override
	public void draw() {
		if (firstDraw) {
			onInit();
		}
		if (needsReOrientation()) {
			background.orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_VERTICAL_MIDDLE);
		}

		buttonOK.setPos(UIScale.getUIScale().scale(8), (int) (background.getHeight() - (UIScale.getUIScale().scale(42) + buttonOK.getHeight())), 0);
		
		if(secondOptionButton){
			buttonSecondOption.setPos((int) (buttonOK.getPos().x + (buttonOK.getWidth() + UIScale.getUIScale().scale(5))), (int) (buttonOK.getPos().y), 0);
			buttonCancel.setPos((int) (buttonSecondOption.getPos().x + (buttonSecondOption.getWidth() + UIScale.getUIScale().scale(5))), (int) (buttonSecondOption.getPos().y), 0);
		}else{
			buttonCancel.setPos((int) (buttonOK.getPos().x + (buttonOK.getWidth() + UIScale.getUIScale().scale(5))), (int) (buttonOK.getPos().y), 0);
		}
		if (titleOnTop) {
			System.err.println("INFO::: " + infoText.getText());
			infoText.setPos((int) (background.getWidth() / 2 - infoText.getMaxLineWidth() / 2), UIScale.getUIScale().scale(-124), 0);
		} else {
			infoText.setPos((int) (background.getWidth() / 2 - infoText.getMaxLineWidth() / 2), UIScale.getUIScale().scale(8), 0);

		}
		
		adaptSizes();
		GlUtil.glPushMatrix();
		transform();
		if (timeError < System.currentTimeMillis() - timeErrorShowed) {
			errorText.getText().clear();
		}
		
		for (AbstractSceneNode a : getChilds()) {
			a.draw();
		}

		GlUtil.glPopMatrix();
	}
	
	private final GUIInputContentSizeInterface defaultContentInterface = new GUIInputContentSizeInterface() {
		
		@Override
		public int getWidth() {
			return descriptionText.getMaxLineWidth() - UIScale.getUIScale().scale(12);
		}
		
		@Override
		public int getHeight() {
			return descriptionText.getTextHeight() + UIScale.getUIScale().scale(12);
		}
	};
	
	public GUIInputContentSizeInterface contentInterface = defaultContentInterface;
	private boolean secondOptionButton;
	

	
	@Override
	public void onInit() {
		if (!firstDraw) {
			return;
		}
		background.onInit();
		infoText.onInit();
		buttonOK.onInit();
		buttonCancel.onInit();
		buttonSecondOption.onInit();
		descriptionText.onInit();

		if (autoOrientate) {
			
			background.orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_VERTICAL_MIDDLE);
		}

		this.attach(background);
		try {
			infoTextSize = infoText.getFont().getWidth(infoText.getText().get(0).toString());
		} catch(Exception exception) {
			exception.printStackTrace();
		}
		if (background instanceof GUIDialogWindow) {
			((GUIDialogWindow) background).attachSuper(infoText);
		}
		background.attach(errorText);

		if (okButton) {
			background.attach(buttonOK);
		}
		if (secondOptionButton) {
			background.attach(buttonSecondOption);
		}
		if (cancelButton) {
			background.attach(buttonCancel);
		}

		infoText.setPos(UIScale.getUIScale().scale(100), UIScale.getUIScale().scale(8), 0);
		
		scrollableContent = new GUIScrollablePanel(100, 100, ((GUIDialogWindow) background).getMainContentPane().getContent(0), getState());
		
		
		scrollableContent.setContent(descriptionText);
		
		descriptionText.wrapSimple = false;
		descriptionText.autoWrapOn = scrollableContent;
		
		getContent().attach(scrollableContent);

		errorText.setPos(UIScale.getUIScale().scale(16), background.getHeight() - UIScale.getUIScale().scale(32), 0);

		descriptionText.setPos(UIScale.getUIScale().smallinset, UIScale.getUIScale().smallinset, 0);
		firstDraw = false;
		
	}

	/**
	 * @return the background
	 */
	public GUIResizableGrabbableWindow getBackground() {
		return background;
	}

	/**
	 * @return the buttonCancel
	 */
	public GUIElement getButtonCancel() {
		return buttonCancel;
	}

	/**
	 * @return the buttonOK
	 */
	public GUITextButton getButtonOK() {
		return buttonOK;
	}

	/**
	 * @return the descriptionText
	 */
	public GUITextOverlay getDescriptionText() {
		return descriptionText;
	}

	public boolean isCancelButton() {
		return cancelButton;
	}

	public void setCancelButton(boolean cancelButton) {
		this.cancelButton = cancelButton;
	}

	public boolean isOkButton() {
		return okButton;
	}

	public void setOkButton(boolean okButton) {
		boolean changed = this.okButton != okButton;
		this.okButton = okButton;
		
		if (changed && !firstDraw) {
			if(okButton) {
				background.attach(buttonOK);
			}else {
				background.detach(buttonOK);
			}
		}
	}

	public void setErrorMessage(String msg, long timeShowed) {
		if (errorText.getText().isEmpty()) {
			errorText.getText().add(msg);
		} else {
			errorText.getText().set(0, msg);
		}
		timeError = System.currentTimeMillis();
		timeErrorShowed = timeShowed;
	}

	public void newLine() {
	}

	/**
	 * @return the firstDraw
	 */
	public boolean isFirstDraw() {
		return firstDraw;
	}

	/**
	 * @param firstDraw the firstDraw to set
	 */
	public void setFirstDraw(boolean firstDraw) {
		this.firstDraw = firstDraw;
	}

	/**
	 * @return the titleOnTop
	 */
	public boolean isTitleOnTop() {
		return titleOnTop;
	}

	/**
	 * @param titleOnTop the titleOnTop to set
	 */
	public void setTitleOnTop(boolean titleOnTop) {
		this.titleOnTop = titleOnTop;
		if (titleOnTop) {
			((GUIDialogWindow) background).setTopDist(0);
		} else {
			((GUIDialogWindow) background).setTopDist(20);
		}
	}
	public boolean isSecondOptionButton() {
		return secondOptionButton;
	}
	public void setSecondOptionButton(boolean secondOptionButton) {
		this.secondOptionButton = secondOptionButton;
	}
	public boolean isMouseInAnyButton() {
		return (okButton && buttonOK.isInside()) || (cancelButton && buttonCancel.isInside()) || (secondOptionButton && buttonSecondOption.isInside());
	}
	

}
