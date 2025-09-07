//package org.schema.schine.input;
//
//import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
//import it.unimi.dsi.fastutil.objects.ObjectArrayList;
//
//import java.nio.ByteBuffer;
//import java.util.List;
//
//import org.lwjgl.glfw.GLFW;
////import org.schema.schine.graphicsengine.core.GLFWCharCallbackI;
////import org.schema.schine.graphicsengine.core.GLFWCursorPosCallbackI;
////import org.schema.schine.graphicsengine.core.GLFWJoystickCallbackI;
////import org.schema.schine.graphicsengine.core.GLFWKeyCallbackI;
////import org.schema.schine.graphicsengine.core.GLFWMonitorCallbackI;
////import org.schema.schine.graphicsengine.core.GLFWMouseButtonCallbackI;
////import org.schema.schine.graphicsengine.core.GLFWScrollCallbackI;
//import org.schema.schine.common.TextAreaInput;
//import org.schema.schine.graphicsengine.core.Controller;
//import org.schema.schine.graphicsengine.core.Display;
//import org.schema.schine.graphicsengine.core.GLFrame;
//import org.schema.schine.graphicsengine.core.MouseEvent;
//import org.schema.schine.graphicsengine.core.Timer;
//import org.schema.schine.graphicsengine.forms.gui.Draggable;
//import org.schema.schine.graphicsengine.forms.gui.GUIElement;
//import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
//import org.schema.schine.graphicsengine.forms.gui.GUIUniqueExpandableInterface;
//import org.schema.schine.graphicsengine.forms.gui.NoKeyboardInput;
//import org.schema.schine.graphicsengine.forms.gui.newgui.DialogInterface;
//import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContextPane;
//import org.schema.schine.network.client.DelayedDropDownSelectedChanged;
//import org.schema.schine.network.client.GUICallbackController;
//
//
//public class BasicInputControllerGLFW{
//	private final List<DialogInterface> playerInputs = new ObjectArrayList<DialogInterface>();
//	private final JoystickMappingFile joystick = new JoystickMappingFile();
//	private final ObjectArrayFIFOQueue<DelayedDropDownSelectedChanged> delayedDropDowns = new ObjectArrayFIFOQueue<DelayedDropDownSelectedChanged>();
//	private boolean grabbigStoppedFlag;
//	private boolean wasDisplayActive;
//	private TextAreaInput lastSelectedInput;
//	private final List<MouseEvent> mouseEvents = new ObjectArrayList<MouseEvent>();
//	private final List<KeyboardEvent> keyboardEvents = new ObjectArrayList<KeyboardEvent>();
//	private final List<JoystickEvent> joystickEvents = new ObjectArrayList<JoystickEvent>();
//	private final List<DialogInterface> deactivatedPlayerInputs = new ObjectArrayList<DialogInterface>();
//	public static Object grabbedObjectLeftMouse;
//	
//	private TextAreaInput currentActiveField;
//	private GUIUniqueExpandableInterface currentActiveDropdown;
//	
//	private final GUICallbackController guiCallbackController = new GUICallbackController();
//	private Draggable draggable;
//	private GUIContextPane currentContextPane;
//	private GUIContextPane currentContextPaneDrawing;
//	private long lastDeactivatedMenu;
//	
//	private List<DialogInterface> tmpInputs = new ObjectArrayList<DialogInterface>();
//	public long windowId;
//	public void updateInput(InputController c, Timer timer) {
//
//		Display.attachInputController(this);
//	
//		while (!getDelayedDropDowns().isEmpty()) {
//			getDelayedDropDowns().dequeue().execute();
//		}
//		
//		boolean beforeInputUpdate = c.beforeInputUpdate();
//		
//		if(!beforeInputUpdate){
//			return;
//		}
//		
//
//		joystick.updateInput();
//
//		//		joystick.printAxisString();
//
//		//when player dialogs available -> enable repeated events
//		Keyboard.enableRepeatEvents(getCurrentActiveField() != null || !c.getPlayerInputs().isEmpty() || c.isChatActive());
//		//		if(!state.getPlayerInputs().isEmpty() && state.getPlayer() == null){
//		//		}
//		
//		//use tmp list because a decativate in a update() function of a dialog will remove it from the main list
//		tmpInputs.addAll(c.getPlayerInputs());
//		for (int i = 0; i < tmpInputs.size(); i++) {
//			tmpInputs.get(i).update(timer);
//		}
//		tmpInputs.clear();
//		for (int i = 0; i < c.getPlayerInputs().size(); i++) {
//			
//			if (c.getPlayerInputs().get(i).checkDeactivated()) {
//				c.getPlayerInputs().get(i).deactivate();
//				i--;
//			}
//			
//		}
//
//		
//
//		if (GraphicsContext.isFocused()) {
//			if (wasDisplayActive) {
//				for (KeyboardEvent e : keyboardEvents) {
//					if (getCurrentActiveField() != null) {
//						if (KeyboardMappings.getEventKey(e) != GLFW.GLFW_KEY_ESCAPE) {
//							Keyboard.enableRepeatEvents(true);
////							System.err.println("HANDLING KEY EVENT FOR CUEENT");
//							getCurrentActiveField().handleKeyEvent(e);
//						} else {
//							Keyboard.enableRepeatEvents(false);
//							setCurrentActiveField(null);
//						}
//					} else {
//						c.handleKeyEvent(e);
//						
//					}
//				}
//				
//				ByteBuffer buttons = JoystickMappingFile.getButtons();
//				
//				for(int i = 0; i < buttons.limit(); i++){
//					MouseEvent e = new MouseEvent();
//					e.button = -1;
//
//					e.state = false;
//					if (i == 0 && buttons.get(i) == GLFW.GLFW_PRESS) {
//						e.button = 0;
//					} else if (i == 1 && buttons.get(i) == GLFW.GLFW_PRESS) {
//						e.button = 1;
//					}
//					if (e.button >= 0) {
//						if (GLFrame.activeForInput) {
//							c.handleMouseEvent(e);
//						}
//					}
//					if(buttons.get(i) == GLFW.GLFW_PRESS){
//						JoystickEvent je = new JoystickEvent();
//						je.button = i;
//						joystickEvents.add(je);
//					}
//				}
//				
//				
//				for (JoystickEvent j : joystickEvents) {
////					if (Controllers.isEventAxis()) {
////						joystick.axisEvent = true;
////					}
//
//						
//					c.handleJoystickEventButton(j);
//					
//
//					Controller.checkJoystick = true;
//					getCurrentActiveField().handleKeyEvent(j);
//					Controller.checkJoystick = false;
//					//interpret joystick input as mouse clicks for joystick config
//
//					MouseEvent e = new MouseEvent();
//					e.button = -1;
//
//					e.state = false;
//					if (joystick.getLeftMouse().isDown()) {
//						e.button = 0;
//					} else if (joystick.getRightMouse().isDown()) {
//						e.button = 1;
//					}
//					if (e.button >= 0) {
//						if (GLFrame.activeForInput) {
//							c.handleMouseEvent(e);
//						}
//					}
//				}
//				joystickEvents.clear();
//				c.handleLocalMouseInput();
//
//				
//
//				if (grabbigStoppedFlag) {
//					if (getDragging() != null && !getDragging().isStickyDrag()) {
//						getDragging().reset();
//						//						System.err.println("Grabbing of "+getState().getController().getInputController().getDragging() +" stopped with no result");
//						setDragging(null);
//					}
//					grabbigStoppedFlag = false;
//				}
//				boolean grabbed = (grabbedObjectLeftMouse != null);
//				for (MouseEvent e : getMouseEvents()) {
//					
//					if (grabbed && !Mouse.isButtonDown(0)) {
//						grabbedObjectLeftMouse = null;
//					} else {
//						
//					}
//					if (grabbed && !Mouse.isButtonDown(0)) {
//						grabbedObjectLeftMouse = null;
//					} else {
//						c.onMouseEvent(e);
//					}
//					if (e.pressedLeftMouse() || e.pressedRightMouse()) {
//						setCurrentActiveField(null);
//						Keyboard.enableRepeatEvents(false);
//					}
//					if (GLFrame.activeForInput) {
//						getGuiCallbackController().execute(e, c.getState());
//					}	
//
//					if (!e.state) {
//						grabbigStoppedFlag = e.button == 0;
//					}
//				}
//				getMouseEvents().clear();
//				
//			}
//		} else {
//			//consume
//			getMouseEvents().clear();
//		}
//		wasDisplayActive = GraphicsContext.isFocused();
//
//		setCurrentActiveDropdown(null);
//		
//		getGuiCallbackController().reset();
//	}
//
//	/**
//	 * @return the joystick
//	 */
//	public JoystickMappingFile getJoystick() {
//		return joystick;
//	}
//
//	public void initialize() {
//		try {
//			joystick.init();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}		
//	}
//
//	public ObjectArrayFIFOQueue<DelayedDropDownSelectedChanged> getDelayedDropDowns() {
//		return delayedDropDowns;
//	}
//
//	public TextAreaInput getLastSelectedInput() {
//		return lastSelectedInput;
//	}
//
//	public void setLastSelectedInput(TextAreaInput lastSelectedInput) {
//		this.lastSelectedInput = lastSelectedInput;
//	}
//
//	public TextAreaInput getCurrentActiveField() {
//		return currentActiveField;
//	}
//
//	public void setCurrentActiveField(TextAreaInput currentActiveField) {
//		this.currentActiveField = currentActiveField;
//	}
//	public void setCurrentActiveDropdown(GUIUniqueExpandableInterface object) {
//		currentActiveDropdown = object;
//	}
//
//	public GUIUniqueExpandableInterface getCurrentActiveDropdown() {
//		return currentActiveDropdown;
//	}
//	public GUICallbackController getGuiCallbackController() {
//		return guiCallbackController;
//	}
//	public List<MouseEvent> getMouseEvents() {
//		return mouseEvents;
//	}
//	public Draggable getDragging(){
//		return draggable;
//	}
//	public void setDragging(Draggable dragging){
//		if (this.draggable != null) {
//			this.draggable.reset();
//		}
//		assert (dragging == null || dragging.getType() != 0);
//		this.draggable = dragging;
//	}
//	
//	public void handleGUIMouseEvent(GUIElement guiElement, int mouseButton) {
//		System.err.println("MOUSE EVENT ON GUI: " + mouseButton + ", " + guiElement.getName());
//		if (guiElement instanceof GUIListElement) {
//			((GUIListElement) guiElement).setSelected(!((GUIListElement) guiElement).isSelected());
//		}
//	}
//	
//	public GUIContextPane getCurrentContextPane() {
//		return currentContextPane;
//	}
//	public void setCurrentContextPane(GUIContextPane currentContextPane) {
//		this.currentContextPane = currentContextPane;
//	}
//	public GUIContextPane getCurrentContextPaneDrawing() {
//		return currentContextPaneDrawing;
//	}
//	public void setCurrentContextPaneDrawing(GUIContextPane currentContextPaneDrawing) {
//		this.currentContextPaneDrawing = currentContextPaneDrawing;
//	}
//	/**
//	 * @return the lastDeactivatedMenu
//	 */
//	public long getLastDeactivatedMenu() {
//		return lastDeactivatedMenu;
//	}
//
//	public void setLastDeactivatedMenu(long currentTimeMillis) {
//		this.lastDeactivatedMenu = currentTimeMillis;
//	}
//	public List<DialogInterface> getPlayerInputs() {
//		return playerInputs;
//	}
//	public List<DialogInterface> getDeactivatedPlayerInputs() {
//		return deactivatedPlayerInputs;
//	}
//	public boolean handleKeyEventInputPanels(KeyEventInterface e) {
//		final int size = getPlayerInputs().size();
//		if (size > 0) {
//			//only the last in list is active
//			DialogInterface playerInput = getPlayerInputs().get(size - 1);
//			if (!(playerInput instanceof NoKeyboardInput)) {
//				playerInput.handleKeyEvent(e);
//				return true;
//			}
//		}
//		return false;
//
//	}
//
//	public void drawDropdownAndContext() {
//		if (getCurrentActiveDropdown() != null) {
//			getCurrentActiveDropdown().drawExpanded();
//		}
//		if (getCurrentContextPane() != null) {
//			setCurrentContextPaneDrawing(getCurrentContextPane());
//			getCurrentContextPane().draw();
//			setCurrentContextPaneDrawing(null);
//		}		
//	}
////	public GLFWScrollCallbackI scrollCallback = new GLFWScrollCallbackI() {
////		
////		@Override
////		public void invoke(long window, double xoffset, double yoffset) {
////			Mouse.scrollX = xoffset;
////			Mouse.scrollY = yoffset;
////			
////			MouseEvent e = new MouseEvent();
////			e.dWheel = (int) xoffset;
////			mouseEvents.add(e);		
////		}
////	}; 
////	public GLFWKeyCallbackI keyCallback = new GLFWKeyCallbackI() {
////		
////		@Override
////		public void invoke(long window, int key, int scancode, int action, int mods) {
////			KeyboardEvent k = new KeyboardEvent();
////			k.charEvent = false;		
////			k.key = key;
////			k.scancode = key;
////			k.state = action == GLFW.GLFW_PRESS;
////			k.mods = mods;
////			keyboardEvents.add(k);
////		}
////	}; 
////	public GLFWMouseButtonCallbackI mouseButtonCallback = new GLFWMouseButtonCallbackI() {
////		
////		@Override
////		public void invoke(long window, int button, int action, int mods) {
////			MouseEvent e = new MouseEvent();
////			e.button = button;
////			e.x = Mouse.getX();
////			e.y = Mouse.getY();
////			e.button = button;
////			e.state = action == GLFW.GLFW_PRESS;
////			e.dx = Mouse.getDX();
////			e.dy = Mouse.getDY();
////			mouseEvents.add(e);			
////		}
////	}; 
////	public GLFWCharCallbackI charCallback = new GLFWCharCallbackI() {
////		
////		@Override
////		public void invoke(long window, int codepoint) {
////			KeyboardEvent k = new KeyboardEvent();
////			k.charEvent = true;		
////			System.err.println("CHAR EVENT: "+codepoint);
////			
////			final char keyChar = (char) codepoint;
////			k.charac = keyChar;
////			
////			keyboardEvents.add(k);
////		}
////	};
////	public GLFWCursorPosCallbackI mouseCursorCallback = new GLFWCursorPosCallbackI() {
////		
////		@Override
////		public void invoke(long window, double posX, double posY) {
////			Mouse.cursorPosCallback(window, posX, posY);			
////		}
////	};
////	
////
////	public GLFWJoystickCallbackI joystickCallback = new GLFWJoystickCallbackI() {
////		
////		@Override
////		public void invoke(int joystick, int event) {
////			if (event == GLFW.GLFW_CONNECTED) {
////				// The joystick was connected
////			} else if (event == GLFW.GLFW_DISCONNECTED) {
////				// The joystick was disconnected
////			}
////		}
////	};
//}
