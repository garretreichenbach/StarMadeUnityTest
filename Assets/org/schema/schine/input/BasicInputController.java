package org.schema.schine.input;

import api.listener.events.input.KeyPressEvent;
import api.mod.StarLoader;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL11;
import org.schema.schine.common.InputCharHandler;
import org.schema.schine.common.TextAreaInput;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.DialogInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContextPane;
import org.schema.schine.input.Mouse.MouseWheelDir;
import org.schema.schine.network.client.DelayedDropDownSelectedChanged;
import org.schema.schine.network.client.GUICallbackController;

import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

public class BasicInputController implements GLFWInputCallbackHandler{
	private final List<DialogInterface> playerInputs = new ObjectArrayList<DialogInterface>();
	private final JoystickMappingFile joystick = new JoystickMappingFile();
	private final ObjectArrayFIFOQueue<DelayedDropDownSelectedChanged> delayedDropDowns = new ObjectArrayFIFOQueue<DelayedDropDownSelectedChanged>();
	private boolean grabbigStoppedFlag;
	private boolean wasDisplayActive;
	private TextAreaInput lastSelectedInput;

	private final List<MouseEvent> mouseEvents = new ObjectArrayList<MouseEvent>();
	private final List<KeyEventInterface> events = new ObjectArrayList<KeyEventInterface>();
	private final List<KeyboardEvent> keyboardEvents = new ObjectArrayList<KeyboardEvent>();
	private final List<JoystickEvent> joystickEvents = new ObjectArrayList<JoystickEvent>();

	private final List<MouseEvent> mouseEventsToAdd = new ObjectArrayList<MouseEvent>();
	private final List<KeyEventInterface> eventsToAdd = new ObjectArrayList<KeyEventInterface>();
	private final List<KeyboardEvent> keyboardEventsToAdd = new ObjectArrayList<KeyboardEvent>();
	private final List<JoystickEvent> joystickEventsToAdd = new ObjectArrayList<JoystickEvent>();


	private final List<DialogInterface> deactivatedPlayerInputs = new ObjectArrayList<DialogInterface>();
	public static Object grabbedObjectLeftMouse;

	private TextAreaInput currentActiveField;
	private GUIUniqueExpandableInterface currentActiveDropdown;

	private final GUICallbackController guiCallbackController = new GUICallbackController();
	private Draggable draggable;
	private GUIContextPane currentContextPane;
	private GUIContextPane currentContextPaneDrawing;
	private long lastDeactivatedMenu;

	private List<DialogInterface> tmpInputs = new ObjectArrayList<DialogInterface>();
	private GUIElement lockedOnScrollBar;
	private short lockedOnScrollBarUpdateNum;
	private final ByteArrayList eventQueue = new ByteArrayList();
	private int pressedMap;
	private double callbackCursorX;
	private double callbackCursorY;

	public BasicInputController() {

	}
	public void updateInput(InputController c, Timer timer) {
		eventQueue.clear();
		while (!delayedDropDowns.isEmpty()) {
			delayedDropDowns.dequeue().execute();
		}

		boolean beforeInputUpdate = c.beforeInputUpdate();
		if(!beforeInputUpdate){
			return;
		}

		registerCallbacks();

		Mouse.cursorPosCallback(callbackCursorX, callbackCursorY);

		updateMouseCursorState();

		joystick.updateInput();

		consume();


		//		joystick.printAxisString();

		//when player dialogs available -> enable repeated events
		Keyboard.enableRepeatEvents(currentActiveField != null || !c.getPlayerInputs().isEmpty() || c.isChatActive());

		//		if(!state.getPlayerInputs().isEmpty() && state.getPlayer() == null){
		//		}

		//use tmp list because a decativate in a update() function of a dialog will remove it from the main list
		tmpInputs.addAll(c.getPlayerInputs());
		for (int i = 0; i < tmpInputs.size(); i++) {
			tmpInputs.get(i).update(timer);
		}
		tmpInputs.clear();
		for (int i = 0; i < c.getPlayerInputs().size(); i++) {

			if (c.getPlayerInputs().get(i).checkDeactivated()) {
				c.getPlayerInputs().get(i).deactivate();
				i--;
			}

		}

		List<MouseEvent> joystickMouseEvents = new ObjectArrayList<MouseEvent>();



		final int playerInputCountBefore = playerInputs.size(); //amount of dialogs up before processing input
		if (currentActiveField != null) {
			//PRIO 1: handle active field
			for(KeyEventInterface e : events){
				if (!e.isEscapeKeyRaw()) {
					Keyboard.enableRepeatEvents(true);
					if(currentActiveField == null) continue;
					if(e.isCharacterEvent()) {
						currentActiveField.handleCharEvent(e);
					}else {
						currentActiveField.handleKeyEvent(e);
					}
				} else {
					Keyboard.enableRepeatEvents(false);
					currentActiveField = null;
				}
			}
		} else if(playerInputCountBefore > 0) {
			//PRIO 2: handle callbacks of dialogs
			for(KeyEventInterface e : events){
				DialogInterface d = playerInputs.get(playerInputCountBefore - 1);//only the last in list is active
				d.handleKeyEvent(e);
				if(d instanceof InputCharHandler) {
					((InputCharHandler)d).handleCharEvent(e);
				}
				if(playerInputs.size() < playerInputCountBefore) {
					//don't handle other events if a dialog was deactivated
					break;
				}
			}
		}else {
			//PRIO 3: handle other input
			/*
			 * only process input for other systems if
			 * there is no more dialog open before this update.
			 * Don't process the input that was used to deactivate a dialog
			 * (e.g. escape would close dialog and open up main menu at the same update)
			 */

			for(KeyEventInterface e : events){
				if(!Keyboard.isRepeatEvent() && e.isRepeat()) {
					//ignore repeat keys when repeat not active
					continue;
				}

				if(e.isCharacterEvent()) {
					c.handleCharEvent(e);
				}else {

					for(KeyboardMappings m : e.getTriggeredMappings()) {
						if(m.NT) {
							assert(m.ntOrdinal < 127 && m.ntOrdinal >= 0):m;
							if(e.isPressed() || e.isRepeat()) {
								assert(!e.isRepeat()):"Sent Repeat "+m;
								eventQueue.add((byte)m.ntOrdinal);
							}else {
								eventQueue.add((byte)(-m.ntOrdinal));
							}
						}
					}
					c.handleKeyEvent(e, timer);
					c.handleLocalMouseInput();
				}

				//INSERTED CODE
				StarLoader.fireEvent(new KeyPressEvent((KeyboardEvent) e), false);
				///

			}
			pressedMap = 0;
			for(KeyboardMappings m : KeyboardMappings.remoteMappings) {
				if (m.isDown()) {
					pressedMap = (int) (pressedMap | m.ntKey);
				} else {
					pressedMap = (int) (pressedMap & ~m.ntKey);
				}
			}
		}


		if (grabbigStoppedFlag) {
			if (draggable != null && !draggable.isStickyDrag()) {
				draggable.reset();
				setDragging(null);
			}
			grabbigStoppedFlag = false;
		}
		boolean grabbed = (grabbedObjectLeftMouse != null);
		for(MouseEvent e : mouseEvents){

			if (grabbed && !Mouse.isPrimaryMouseDownUtility()) {
				grabbedObjectLeftMouse = null;
			} else {

			}
			if (grabbed && !Mouse.isPrimaryMouseDownUtility()) {
				grabbedObjectLeftMouse = null;
			}
			if (e.pressedLeftMouse() || e.pressedRightMouse()) {
				currentActiveField = null;
				Keyboard.enableRepeatEvents(false);
			}
			if (GLFrame.activeForInput) {
				guiCallbackController.execute(e, c.getState());
			}

			if (e.releasedLeftMouse()) {
				grabbigStoppedFlag = true;
			}

		}


		wasDisplayActive = GraphicsContext.isCurrentFocused();

		currentActiveDropdown = null;

		guiCallbackController.reset();



		if(lockedOnScrollBar != null && Math.abs(lockedOnScrollBar.getState().getNumberOfUpdate() - lockedOnScrollBarUpdateNum) > 2){
			lockedOnScrollBar = null;
		}


	}
	public ByteArrayList getEventKeyNTQueue() {
		return eventQueue;
	}
	public int getPressedKeyNTMap() {
		return pressedMap;
	}
	public void updateMouseCursorState() {
		Camera.mouseState.updateMouseState(this);
	}
	public void consume() {
		events.clear();
		keyboardEvents.clear();
		mouseEvents.clear();
		joystickEvents.clear();
		events.addAll(eventsToAdd);
		keyboardEvents.addAll(keyboardEventsToAdd);
		mouseEvents.addAll(mouseEventsToAdd);
		joystickEvents.addAll(joystickEventsToAdd);
		eventsToAdd.clear();
		keyboardEventsToAdd.clear();
		mouseEventsToAdd.clear();
		joystickEventsToAdd.clear();
	}
	/**
	 * @return the joystick
	 */
	public JoystickMappingFile getJoystick() {
		return joystick;
	}

	public void initialize() {
		try {
			joystick.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ObjectArrayFIFOQueue<DelayedDropDownSelectedChanged> getDelayedDropDowns() {
		return delayedDropDowns;
	}

	public TextAreaInput getLastSelectedInput() {
		return lastSelectedInput;
	}

	public void setLastSelectedInput(TextAreaInput lastSelectedInput) {
		this.lastSelectedInput = lastSelectedInput;
	}

	public TextAreaInput getCurrentActiveField() {
		return currentActiveField;
	}

	public void setCurrentActiveField(TextAreaInput currentActiveField) {
		this.currentActiveField = currentActiveField;
	}
	public void setCurrentActiveDropdown(GUIUniqueExpandableInterface object) {
		currentActiveDropdown = object;
	}

	public GUIUniqueExpandableInterface getCurrentActiveDropdown() {
		return currentActiveDropdown;
	}
	public GUICallbackController getGuiCallbackController() {
		return guiCallbackController;
	}
	public List<MouseEvent> getMouseEvents() {
		return mouseEvents;
	}


	public Draggable getDragging(){
		return draggable;
	}
	public void setDragging(Draggable dragging){
		if (this.draggable != null) {
			this.draggable.reset();
		}
		assert (dragging == null || dragging.getType() != 0);
		this.draggable = dragging;
	}

	public void handleGUIMouseEvent(GUIElement guiElement, int mouseButton) {
//		System.err.println("MOUSE EVENT ON GUI: " + mouseButton + ", " + guiElement.getName());
		if (guiElement instanceof GUIListElement) {
			((GUIListElement) guiElement).setSelected(!((GUIListElement) guiElement).isSelected());
		}
	}

	public GUIContextPane getCurrentContextPane() {
		return currentContextPane;
	}
	public void setCurrentContextPane(GUIContextPane currentContextPane) {
		this.currentContextPane = currentContextPane;
	}
	public GUIContextPane getCurrentContextPaneDrawing() {
		return currentContextPaneDrawing;
	}
	public void setCurrentContextPaneDrawing(GUIContextPane currentContextPaneDrawing) {
		this.currentContextPaneDrawing = currentContextPaneDrawing;
	}
	/**
	 * @return the lastDeactivatedMenu
	 */
	public long getLastDeactivatedMenu() {
		return lastDeactivatedMenu;
	}

	public void setLastDeactivatedMenu(long currentTimeMillis) {
		this.lastDeactivatedMenu = currentTimeMillis;
	}
	public List<DialogInterface> getPlayerInputs() {
		return playerInputs;
	}
	public List<DialogInterface> getDeactivatedPlayerInputs() {
		return deactivatedPlayerInputs;
	}


	public void drawDropdownAndContext() {
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		if (currentActiveDropdown != null) {
			currentActiveDropdown.drawExpanded();
		}
		if (currentContextPane != null) {

			currentContextPaneDrawing = currentContextPane;
			currentContextPane.draw();
			currentContextPaneDrawing = null;
		}
	}

	public boolean isScrollLockOn(GUIScrollablePanel self) {
		return lockedOnScrollBar != null && lockedOnScrollBar != self;
	}
	public void scrollLockOn(GUIElement scrollBar) {
		lockedOnScrollBar = scrollBar;
		lockedOnScrollBarUpdateNum = scrollBar.getState().getNumberOfUpdate();
	}
	public void registerCallbacks() {
		GraphicsContext.current.registerInputController(this);
	}
	public GLFWScrollCallbackI scrollCallback = (window, xoffset, yoffset) -> {
		if(window != GraphicsContext.getWindowId()) {
			return;
		}
		Mouse.scrollX = xoffset;
		Mouse.scrollY = yoffset;

		MouseEvent e = new MouseEvent(InputType.MOUSE_WHEEL);
		e.dWheel = (int) yoffset;
		e.key = yoffset > 0 ? MouseWheelDir.MOUSE_WHEEL_UP.dir : MouseWheelDir.MOUSE_WHEEL_DOWN.dir;
		e.actionState = GLFW_PRESS;
		e.checkSpecialKeysDown();
		e.checkTriggeredMappings(InputType.MOUSE_WHEEL);

		mouseEventsToAdd.add(e);
		eventsToAdd.add(e);
	};
	public GLFWKeyCallbackI keyCallback = (window, key, scancode, action, mods) -> {
		if(window != GraphicsContext.getWindowId()) {
			return;
		}
		KeyboardEvent k = new KeyboardEvent();
		k.charEvent = false; //for special events (cursors, enter, etc)
		k.key = key;
		k.scancode = key;
		k.actionState = action;
		k.mods = mods;
		k.checkSpecialKeysDown();
		k.checkTriggeredMappings(InputType.KEYBOARD);
		keyboardEventsToAdd.add(k);
		eventsToAdd.add(k);
	};
	public GLFWMouseButtonCallbackI mouseButtonCallback = (window, button, action, mods) -> {
		if(window != GraphicsContext.getWindowId()) {
			return;
		}
		MouseEvent e = new MouseEvent(InputType.MOUSE);
		e.key = button;
		e.x = Mouse.getX();
		e.y = Mouse.getY();
		e.actionState = action;
		e.dx = Mouse.getDX();
		e.dy = Mouse.getDY();
		e.checkSpecialKeysDownMod(mods);

		e.checkTriggeredMappings(InputType.MOUSE);

		mouseEventsToAdd.add(e);
		eventsToAdd.add(e);
	};
	public GLFWCharCallbackI charCallback = (window, codepoint) -> {
		if(window != GraphicsContext.getWindowId()) {
			return;
		}
		KeyboardEvent k = new KeyboardEvent();
		k.charEvent = true;
		k.key = codepoint;
		k.actionState = GLFW_PRESS;
		final String keyChar = String.valueOf(Character.toChars(codepoint));
		k.charac = keyChar;

		//character events don't trigger any normal event

		eventsToAdd.add(k);
	};


	public GLFWCursorPosCallbackI mouseCursorCallback = new GLFWCursorPosCallbackI() {



		@Override
		public void invoke(long window, double posX, double posY) {
			if(window == GraphicsContext.getWindowId()) {
				callbackCursorX = posX;
				callbackCursorY = posY;
			}

		}
	};
	public GLFWJoystickCallbackI joystickCallback = (joystick, event) -> {
		if (event == GLFW.GLFW_CONNECTED) {
			// The joystick was connected
		} else if (event == GLFW.GLFW_DISCONNECTED) {
			// The joystick was disconnected
		}
	};
	@Override
	public GLFWCharCallbackI getCharCallback() {
		return charCallback;
	}


	@Override
	public GLFWKeyCallbackI getKeyCallback() {
		return keyCallback;
	}

	@Override
	public GLFWScrollCallbackI getScrollCallback() {
		return scrollCallback;
	}

	@Override
	public GLFWMouseButtonCallbackI getMouseButtonCallback() {
		return mouseButtonCallback;
	}

	@Override
	public GLFWCursorPosCallbackI getMouseCursorCallback() {
		return mouseCursorCallback;
	}

	@Override
	public GLFWJoystickCallbackI getJoystickCallback() {
		return joystickCallback;
	}
}