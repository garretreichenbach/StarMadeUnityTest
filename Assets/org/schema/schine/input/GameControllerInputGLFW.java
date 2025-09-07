//package org.schema.schine.input;
//
//import it.unimi.dsi.fastutil.ints.IntArrayList;
//
//import org.lwjgl.glfw.GLFW;
//
//public class GameControllerInput {
//	private GameController[] joysticks;
//	private GameController activeController;
//	private boolean init;
//
//	public void init() {
//		addJoysticks();
//		init = true;
//	}
//	public void addJoysticks(){
//		IntArrayList l = new IntArrayList();
//		for(int joy = 0; joy < 16; joy++){
//			if(GLFW.glfwJoystickPresent(joy)){
//				l.add(joy);
//			}
//		}
//		if(l.size() > 0){
//			joysticks = new GameController[l.size()];
//			System.out.println("[CLIENT][GAMEINPUT] TOTAL INPUT DEVICES (joysticks/gamepads/others): "
//					+ joysticks.length);
//			for(int i = 0; i < l.size();i++){
//				int joy = l.getInt(i);
//				joysticks[i] = new GameController(joy);
//			}
//		}else{
//			joysticks = null;
//		}
//	}
//	public int getSize() {
//		if (joysticks != null) {
//			return joysticks.length;
//		} else {
//			return 0;
//		}
//	}
//
//	public String getName(int i) {
//		return GLFW.glfwGetJoystickName(joysticks[i].joystick);
//	}
//
//	public void delesect() {
//		setActiveController(null);
//	}
//
//	public void select(int i) {
//		if (joysticks != null && i >= 0 && i < joysticks.length) {
//			setActiveController(joysticks[i]);
//		} else {
//			System.err.println("[CONTROLLERINPUT] resetcontroller (tried " + i + ")");
//			setActiveController(null);
//		}
//	}
//
//	public void poll() {
//		if (getActiveController() != null) {
////			getActiveController().joystick.poll();
//		}
//	}
//
//	/**
//	 * @return the activeController
//	 */
//	public GameController getActiveController() {
//		return activeController;
//	}
//
//	/**
//	 * @param activeController the activeController to set
//	 */
//	public void setActiveController(GameController activeController) {
//		this.activeController = activeController;
//	}
//
//	public boolean initialized() {
//		return init;
//	}
//}
