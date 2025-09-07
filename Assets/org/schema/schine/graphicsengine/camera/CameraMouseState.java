package org.schema.schine.graphicsengine.camera;

import org.lwjgl.glfw.GLFW;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.input.BasicInputController;
import org.schema.schine.input.Keyboard;
import org.schema.schine.input.Mouse;

public class CameraMouseState {

	private static boolean grabbed = false; // Don't grab mouse in windowed mode
	public static boolean ungrabForced = false;
	public int dWheel;
	public int dx;
	public int dy;
	public int x;
	public int y;
	public CameraMouseState() {
	}

	public static boolean isInMouseControl() {
		if (Keyboard.isDebugKeyDown()) {
			return false;
		}

		if (ungrabForced && EngineSettings.S_MOUSE_LOCK.isOn()) {
			return false;
		}
		return (grabbed && EngineSettings.S_MOUSE_LOCK.isOn());
	}


	public void updateMouseState(BasicInputController state) {

		

		boolean grabbed = isInMouseControl();

		if (Mouse.isGrabbed() != grabbed) {
			Mouse.setGrabbed(grabbed);
		}
		//unfortunately doesnt work grabbed
		Mouse.setClipMouseCoordinatesToWindow(!(Mouse.isGrabbed() || Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_ALT)));
		reset();

		for (MouseEvent e : state.getMouseEvents()) {
			dWheel += e.dWheel;
		}
		
		dx += Mouse.getDX();
		dy += Mouse.getDY();
		x = Mouse.getX();
		y = Mouse.getY();
		
//		dx = Mouse.getDX();
//		dy = Mouse.getDY();

//		System.err.println("MOUSE....  "+dx+", "+dy+"; :::: "+x+", "+y);


//		if(Mouse.isGrabbed() || Keyboard.isKeyDown(GLFW.GLFW_KEY_LMENU)){
//			Mouse.setCursorPosition(GLFrame.getWidth()/2, GLFrame.getHeight()/2);
//		}

	}

	public static boolean isGrabbed() {
		return grabbed;
	}

	public static void setGrabbed(boolean grabbed) {
		CameraMouseState.grabbed = grabbed;
	}

	public void reset() {
		dx = 0;
		dy = 0;
		dWheel = 0;		
	}

}
