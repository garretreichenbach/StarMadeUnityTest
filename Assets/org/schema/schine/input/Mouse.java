package org.schema.schine.input;

import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwGetMouseButton;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;

import org.lwjgl.glfw.GLFW;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GraphicsContext;

public class Mouse {

	public static final long DOUBLE_CLICK_MS = 300;
//	public static int getX() {
//		return org.lwjgl.input.Mouse.getX();
//	}
//	public static int getY() {
//		return org.lwjgl.input.Mouse.getY();
//	}
//	public static boolean isGrabbed() {
//		return org.lwjgl.input.Mouse.isGrabbed();
//	}
//	public static void setGrabbed(boolean grabbed) {
//		org.lwjgl.input.Mouse.setGrabbed(grabbed);
//	}
//	public static boolean isButtonDown(int i) {
//		return org.lwjgl.input.Mouse.isButtonDown(i);
//	}
//	public static boolean isCreated() {
//		return org.lwjgl.input.Mouse.isCreated();
//	}
//	public static void setClipMouseCoordinatesToWindow(boolean b) {
//		org.lwjgl.input.Mouse.setClipMouseCoordinatesToWindow(b);
//	}

	public enum MouseWheelDir{
		MOUSE_WHEEL_DOWN(-1),
		MOUSE_WHEEL_UP(1),
		;
		public final int dir;

		private MouseWheelDir(int dir) {
			this.dir = dir;
		}

		public String getName() {
			return this == MouseWheelDir.MOUSE_WHEEL_DOWN ? Lng.str("Mouse Wheel Down") : Lng.str("Mouse Wheel Up");
		}
	}
	
	private static boolean grabbed;
	private static boolean CREATED;
	private static double x;
	private static double y;
	private static double dx;
	private static double dy;
	protected static double scrollY;
	protected static double scrollX;
	public static boolean isDown(int i) {
		return glfwGetMouseButton(GraphicsContext.getWindowId(), i) == GLFW_PRESS;
	}
	public static boolean isGrabbed() {
		return grabbed;
	}
	public static void setGrabbed(boolean grabbed) {
		GLFW.glfwSetInputMode(GraphicsContext.getWindowId(), GLFW_CURSOR, 
				grabbed ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
		Mouse.grabbed = grabbed;
	}
	public static int getX() {
		return (int)x;
	}
	public static int getY() {
		return (int)y;
	}
	public static int getDX() {
		return (int)dx;
	}
	public static int getDY() {
		return (int)dy;
	}
	public static boolean isCreated() {
		return CREATED;
	}
	public static void create() {
		CREATED = true;
	}
	public static void cursorPosCallback(double posX, double posY) {
		double xLast = Mouse.x;
		double yLast = Mouse.y;
		
		Mouse.x = posX;
		Mouse.y = posY;
		
		Mouse.dx = (posX - xLast);
		Mouse.dy = (posY - yLast);

	}
	public static void setClipMouseCoordinatesToWindow(boolean b) {
		
	}
	public static void setCursorPosition(int x, int y) {
		glfwSetCursorPos(GraphicsContext.getWindowId(), x, y);
	}
	/**
	 * used for general actions such as drag&drop, GUI operations
	 * @return if right mouse button is down
	 */
	public static boolean isSecondaryMouseDownUtility() {
		return Mouse.isDown(1);
	}
	/**
	 * used for general actions such as drag&drop, GUI operations
	 * @return if left mouse button is down
	 */
	public static boolean isPrimaryMouseDownUtility() {
		return Mouse.isDown(0);
	}
	

}
