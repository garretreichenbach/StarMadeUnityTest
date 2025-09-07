using System;
using UnityEngine;
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Threading; // For Thread.Sleep

namespace Org.Schema.Schine.GraphicsEngine.Core {
	/// <summary>
	/// Mediator for commands that can be issued by the JoglEventMediator. The event
	/// mediator listens for various GUI events and determines the policy for
	/// deciding what user actions constitute what event. The job of this class is to
	/// perform the requested actions based on those events.
	/// </summary>
	public class GLFrame : MonoBehaviour, GraphicsFrame {
		public ClientState State; // Changed to public property for Unity Inspector
		public static bool ActiveForInput = true;

		private static int _dialogResult = -1; // Renamed n to _dialogResult for C# conventions

		private Controller _controller; // Renamed to _controller
		private bool _disposing;
		private DrawableScene _drawable;

		// JFrame related fields are removed as Unity handles the main window
		// private JFrame mFrame;

		private long _lastInactive;
		public bool Existing = false; // Renamed to Existing
		private bool _waitingForServerShutdown;
		private bool _waitingForLocalServerUp;
		public static string CurrentMemString = ""; // Renamed to CurrentMemString
		private long _lastDrawInact;
		public static StateChangeRequest StateChangeRequest; // Renamed to StateChangeRequest

		/// <summary>
		/// Instantiates a new gL frame.
		/// </summary>
		public GLFrame() { }

		public static int GetHeight() {
			// In Unity, screen height is typically accessed via Screen.height
			return Screen.height; // Controller.viewport.get(3) equivalent
		}

		public static int GetWidth() {
			// In Unity, screen width is typically accessed via Screen.width
			return Screen.width; // Controller.viewport.get(2) equivalent
		}

		public static bool IsFinished() { return GraphicsContext.isFinished(); }

		public void setFinishedFrame(bool finished) { SetFinished(finished); }

		public static void SetFinished(bool finished) {
			if(!GraphicsContext.isFinished() && finished) {
				try {
					throw new FinishedFrameException();
				}
				catch(FinishedFrameException e) {
					Debug.LogError("!!!! THIS DISPLAYS THE STACKTRACE OF A REGULAR GL FRAME EXIT");
					Debug.LogException(e);
				}

				GraphicsContext.SetFinished(finished);

				ServerState.setFlagShutdown(true);
			}
		}

		public static void ProcessErrorDialogException(Exception e, StateInterface state) {
			if(IsFinished()) {
				return;
			}

			Debug.LogException(e);
			Debug.LogError("GLFRAME ProcessErrorDialogException ()");
			if(e is SocketException socketEx && socketEx.Message != null &&
			   socketEx.Message.Contains("Connection reset by peer: socket write error")) {
				ProcessErrorDialogExceptionWithoutReport(e, state);
				return;
			}

			Debug.LogException(e);
			_dialogResult = -1;

			// In Unity, we don't use SwingUtilities.invokeAndWait for UI.
			// This would typically be handled by a Unity UI Canvas or a native dialog plugin.
			// For now, we'll simulate the dialog choice and log the error.
			Debug.LogError($"Critical Error: {e.GetType().Name}: {e.Message}");

			string extraMessage = "";

			if((e.Message != null && e.Message.Contains("Database lock acquisition"))) {
				extraMessage +=
					"\n\nIMPORTANT NOTE: this crash happens when there is still an instance of the game running\n" +
					"check your process manager for \"javaw.exe\" and terminate it, or restart your computer to solve this problem.";
			}

			if((e.Message != null && e.Message.Contains("shieldhit"))) {
				extraMessage += "\n\nIMPORTANT NOTE: this crash happens on some ATI cards\n" +
				                "please try to update your drivers, and if it still doesn't work,\n" +
				                "set \"Shieldhit drawing\" to \"off\" in the advanced graphics settings on start-up";
			}

			if((e.Message != null && e.Message.Contains("SkyFromSpace"))) {
				extraMessage += "\n\nIMPORTANT NOTE: this crash happens on some ATI cards\n" +
				                "please try to update your drivers, and if it still doesn't work,\n" +
				                "set \"Atmosphere Shader\" to \"none\" in the advanced graphics settings on start-up";
			}

			bool isIntelCard = GraphicsContext.current != null && GraphicsContext.current.isIntel();
			if(isIntelCard || (e.Message != null && e.Message.Contains("perpixel"))) {
				extraMessage +=
					"\n\nIMPORTANT NOTE: The game has detected that you are using an Intel graphics device.\n" +
					"Please update your graphics card driver to the latest version.\n" +
					"Many issues arise from outdated Intel drivers. If you have an Intel card, please click:\n" +
					"the button on the bottom left. If unsuccessful, another option is to manually download the\n" +
					"driver from intel.com";
				// Simulate "Download Intel Drivers" option being chosen if it were available
				// For now, we'll just log the URL
				Debug.LogWarning(
					"Simulating 'Download Intel Drivers' option. URL: http://www.intel.com/p/en_US/support/detect");
				// Application.OpenURL("http://www.intel.com/p/en_US/support/detect");
			}

			Debug.LogError($"Error Details: {e.GetType().Name}: {e.Message}{extraMessage}");

			// Simulate user choice: 0 for Retry, 1 for Exit
			// For now, we'll default to Exit in a critical error scenario in Unity
			_dialogResult = 1; // Default to Exit

			switch(_dialogResult) {
				case 0: // Retry - not fully implemented, as it depends on the context of the error
					Debug.LogWarning(
						"Simulating 'Retry' option. This might not work without proper error recovery logic.");
					break;
				case 1: // Exit
					Debug.LogError("AGAIN PRINTING STACK TRACE");
					Debug.LogException(e);
					Debug.LogError($"[GLFrame] (ErrorDialog Chose Exit) Error Message: {e.Message}");
					state?.exit();
					Application.Quit(-1); // Unity's way to exit
					break;
			}
		}

		public static void ProcessErrorDialogExceptionWithoutReport(Exception e, StateInterface state) {
			if(IsFinished()) {
				return;
			}

			Debug.LogException(e);
			_dialogResult = -1;

			Debug.LogError($"Disconnected: {e.GetType().Name}: {e.Message}");

			// Simulate user choice: 0 for Exit
			_dialogResult = 0; // Default to Exit

			switch(_dialogResult) {
				default: // Exit
					Debug.LogError($"[GLFrame] (ErrorDialog Chose Exit) Error Message: {e.Message}");
					state?.exit();
					Application.Quit(-1);
					break;
			}
		}

		public static void ProcessErrorDialogExceptionWithoutReportWithContinue(Exception e, StateInterface state) {
			if(IsFinished()) {
				return;
			}

			Debug.LogException(e);
			_dialogResult = -1;

			Debug.LogError($"Disconnected (with continue option): {e.GetType().Name}: {e.Message}");

			// Simulate user choice: 0 for Continue, 1 for Exit
			// For now, we'll default to Exit in a critical error scenario in Unity
			_dialogResult = 1; // Default to Exit

			switch(_dialogResult) {
				case 0: // Continue
					Debug.LogWarning(
						"Simulating 'Continue' option. This might not work without proper error recovery logic.");
					break;
				case 1: // Exit
					Debug.LogError($"[GLFrame] (ErrorDialog Chose Exit) Error Message: {e.Message}");
					state?.exit();
					Application.Quit(-1);
					break;
			}
		}

		private static void StartCrashReporterInstance(string[] args, StateInterface state) {
			// In Unity, launching external Java processes for crash reporting is not idiomatic.
			// This functionality would typically be replaced by Unity's built-in crash reporting
			// or a custom C# solution that logs errors and potentially sends them to a server.
			Debug.LogError("[CRASHREPORTER] Original Java crash reporter functionality removed.");
			Debug.LogError($"[CRASHREPORTER] Arguments: {string.Join(" ", args)}");

			// Simulate exiting the application as the original code did
			state?.exit();
			Application.Quit(-1);
		}

		/// <summary>
		/// Clean up.
		/// </summary>
		public void cleanUp() { _drawable?.cleanUp(); }

		/*
		 * (non-Javadoc)
		 *
		 * @see javax.media.openGL11.GLEventListener#dispose(javax.media.openGL11.
		 * GLAutoDrawable)
		 */
		public void dispose() {
			Debug.Log("[GLFrame] disposing GLFrame");
			_disposing = true;
			GLFrame.SetFinished(true);
		}

		private void Draw() {
			if(_drawable != null) {
				try {
					_drawable.draw();
					TimeStatistics.reset("betweenDraw");
					// Unity handles clear color via Camera component or Graphics.SetRenderTarget
					// GL.ClearColor(0.004f, 0.004f, 0.02f, 0.0f); // This would be set on the camera
				}
				catch(RuntimeException e) {
					ProcessErrorDialogException(e, State);
				}
			}
			else {
				// Unity handles clear via Camera.clearFlags
				// GL.Clear(GL.COLOR_BUFFER_BIT | GL.DEPTH_BUFFER_BIT);
				// GL.Color(new Color(0.2f, 1.0f, 0.3f, 1f); // For direct GL drawing
			}
		}

		/// <summary>
		/// Gets the controller.
		/// </summary>
		/// <returns>The controller</returns>
		public Controller GetController() { return _controller; }

		/// <summary>
		/// Sets the controller.
		/// </summary>
		/// <param name="controller">The controller to set</param>
		public void SetController(Controller controller) { _controller = controller; }

		private void Render() {
			if(_disposing) {
				return;
			}
			// Unity handles GL.Clear via Camera.clearFlags
			// GL.Clear(GL.COLOR_BUFFER_BIT | GL.DEPTH_BUFFER_BIT);

			TimeStatistics.reset("DRAW");
			GL.PushMatrix(); // For custom GL drawing
			Draw();
			GL.PopMatrix(); // For custom GL drawing
			TimeStatistics.set("DRAW");

			// GPU Memory Info - NVXGPUMemoryInfo is NVIDIA specific and LWJGL. 
			// Unity doesn't expose this directly in a cross-platform way.
			// This code block is removed.
			// if (Keyboard.isCreated() && KeyboardMappings.PLAYER_LIST.isDown() && !Keyboard.isKeyDown(GLFW.GLFW_KEY_CAPS_LOCK) && Keyboard.isKeyDown(GLFW.GLFW_KEY_DOWN) && GraphicsContext.current.getCapabilities().GL_NVX_gpu_memory_info) { ... }
		}

		public void onEndLoop(GraphicsContext context) {
			try {
				Controller.cleanUpAllBuffers();
				// mFrame.dispose() is removed as JFrame is not used
				context.destroy();
			}
			catch(Exception e) {
				Debug.LogException(e);
			}

			if(State != null) {
				SetFinished(true);
				try {
					State.getController().onShutDown(); // Assuming onShutDown exists on Controller
				}
				catch(IOException e) {
					Debug.LogException(e);
				}
				catch(Exception e) {
					Debug.LogException(e);
				}
			}

			Debug.Log($"[GLFrame] terminated frame: exiting program; finished: {IsFinished()}");
			Debug.LogError("Exiting because of terminated frame");

			try {
				State?.disconnect();
			}
			catch(IOException e) {
				Debug.LogException(e);
			}

			State?.exit();

			while(ServerState.isCreated() && !ServerState.serverIsOkToShutdown) {
				try {
					Thread.Sleep(30);
				}
				catch(ThreadInterruptedException e) {
					Debug.LogException(e);
				}
			}

			Debug.LogError("[GLFrame] Client Application.Quit()");
			Application.Quit(1); // Unity's way to exit
		}

		public void handleException(Exception e) {
			Debug.LogError($"[GLFRAME] THROWN: {e.GetType().Name} Now Printing StackTrace");
			Debug.LogException(e);
			Debug.LogError($"[GLFRAME] THROWN: {e.GetType().Name} Printing StackTrace DONE");
			if(e is SocketException) {
				if(State != null && State.getGraphicsContext() != null) {
					State.getGraphicsContext().getLoadingScreen().handleException(
						new DisconnectException(
							$"You have been disconnected from the Server\n\n(Actual Exception: {e.GetType().Name})"));
					try {
						State.stopClient();
					}
					catch(Exception ex) {
						Debug.LogException(ex);
					}

					if(ServerState.isCreated()) {
						ServerState.setFlagShutdown(true);
						// In Unity, avoid creating new Threads for UI updates or blocking the main thread.
						// This would typically be handled by a Coroutine or an async operation.
						// For now, we'll simulate the server shutdown message.
						Debug.LogWarning("Simulating server shutdown wait. This should be a Coroutine in Unity.");
						// (new Thread(() => { ... })).start(); // Removed Java Thread
						Controller.getResLoader().loadAll(); // Placeholder for Controller.setLoadMessage
						Controller.getResLoader().loadClient(); // Placeholder for Controller.setLoadMessage
						ServerState.clearStatic();
					}
				}
				else {
					GLFrame.ProcessErrorDialogExceptionWithoutReport(
						new DisconnectException(
							$"You have been disconnected from the Server\nThis is caused either connection problem or a server crash.\n\nActualException: {e.GetType().Name}"),
						State);
				}
			}
			else {
				if(State != null && State.getGraphicsContext() != null) {
					State.getGraphicsContext().getLoadingScreen().handleException(e);
				}
				else {
					GLFrame.ProcessErrorDialogException(e, State);
				}
			}
		}

		// Unity's Update method will replace doFrameAndUpdate
		void Update() {
			if(State == null) return; // Ensure state is initialized

			// Check for close requests (Unity handles this via Application.Quit or ESC key)
			// if (context.isFocused()) { ... }
			// Unity's Application.isFocused handles window focus.

			if(Application.isFocused) {
				State.getController().updateStateInput(GraphicsContext.current.timer);

				if(!ActiveForInput) {
					if(Time.realtimeSinceStartup - _lastInactive > 0.3f) // 300ms
					{
						ActiveForInput = true;
					}
				}

				TimeStatistics.set("betweenDraw");
				TimeStatistics.reset("RENDERLOOP");
				Render(); // Call our custom Render method
				TimeStatistics.set("RENDERLOOP");

				// DEBUG: to force certain fps
				// if(Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL) && Keyboard.isKeyDown(GLFW.GLFW_KEY_LMENU)){
				//     Display.sync(10);
				// }

				try {
					UpdateLogic(GraphicsContext.current.timer); // Renamed to avoid conflict with MonoBehaviour.Update
				}
				catch(IOException e) {
					Debug.LogException(e);
				}
			}
			else {
				State.getController().consumeIntputs();
				ActiveForInput = false;
				_lastInactive = (long)(Time.realtimeSinceStartup * 1000);

				if(GraphicsContext.current.isContentVisible() || (State.getUpdateTime() - _lastDrawInact) > 1000) {
					Render();
					_lastDrawInact = (long)(Time.realtimeSinceStartup * 1000);
				}

				try {
					UpdateLogic(GraphicsContext.current.timer);
				}
				catch(IOException e) {
					Debug.LogException(e);
				}
			}

			afterFrame(); // Call afterFrame after each Unity Update
		}

		// Renamed from update to UpdateLogic to avoid conflict with MonoBehaviour.Update
		float _updateMem = 2;

		public void UpdateLogic(Timer timer) {
			if(!Controller.getResLoader().isLoaded()) {
				return;
			}

			if(EngineSettings.TRACK_VRAM.isOn()) {
				_updateMem -= timer.getDelta();
				if(_updateMem < 0) {
					_updateMem = 3;
					CurrentMemString = GraphicsContext.current.getUsedMemory();
				}
			}

			lock(State.updateLock) {
				State.getController().update(timer);
				_drawable?.update(timer);
			}
		}

		public void doFrameAndUpdate(GraphicsContext context) {
			// This method is replaced by Unity's Update()
			// The logic is moved to the Update() method of this MonoBehaviour.
		}

		public void afterFrame() {
			if(StateChangeRequest != null) {
				ExecuteStateChange(StateChangeRequest);
			}
		}

		private void ExecuteStateChange(StateChangeRequest c) {
			bool checkServer = true;
			if(_waitingForLocalServerUp) {
				if(!ServerState.isCreated()) {
					State.getController().alertMessage("Waiting for local server to start");
					return;
				}
				else {
					_waitingForLocalServerUp = false;
					checkServer = false;
				}
			}

			if(_waitingForServerShutdown) {
				if(ServerState.isCreated()) {
					State.getController().alertMessage("Waiting for local server to shut down");
					return;
				}
				else {
					_waitingForServerShutdown = false;
					checkServer = false;
				}
			}

			if(checkServer) {
				IPAddress ip;
				try {
					ip = IPAddress.Parse(c.hostPortLogin.host); // Assuming host is an IP address string
				}
				catch(FormatException) {
					// If it's not a direct IP, try to resolve it.
					// For simplicity, we'll just assume it's an IP or handle as unknown host.
					State.getController().alertMessage($"Unknown Host: {c.hostPortLogin.host}");
					return;
				}
				catch(Exception e) {
					Debug.LogException(e);
					State.getController().alertMessage($"Unknown Host: {c.hostPortLogin.host}");
					return;
				}

				bool local = IPAddress.IsLoopback(ip); // Simplified check for local address

				if(!local && ServerState.isCreated()) {
					State.setDoNotDisplayIOException(true);
					State.setExitApplicationOnDisconnect(false);

					ClientState.setFinishedFrameAfterLocalServerShutdown = false;

					ServerState.setFlagShutdown(true);

					_waitingForServerShutdown = true;
					if(ServerState.isCreated()) {
						State.getController().alertMessage("Waiting for local server to shut down");
						return;
					}

					ServerState.clearStatic();

					ClientState.setFinishedFrameAfterLocalServerShutdown = true;
				}
				else if(local && !ServerState.isCreated()) {
					if(!_waitingForLocalServerUp) {
						State.startLocalServer();
						_waitingForLocalServerUp = true;
						return;
					}
				}
			}

			State.stopClient();
			State.startClient(c.hostPortLogin, false);

			GLFrame.StateChangeRequest = null;
		}

		public void SetState(ClientState state, DrawableScene drawable) {
			_drawable = drawable;
			State = state;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * javax.media.openGL11.GLEventListener#init(javax.media.openGL11.GLAutoDrawable
		 * )
		 */
		public void StartGraphicsWithState(string title, ClientState state,
			DrawableScene drawable, GraphicsContext context) {
			context.params.title = title;

			SetState(state, drawable);
			TimeStatistics.reset("betweenDraw");

			Existing = true;

			if(!context.started) {
				Debug.Assert(false, "GraphicsContext not started. This should be handled by Unity's lifecycle.");
				context.startMainLoop(); // This would typically be Unity's own game loop
			}
			else {
				EnqueueFrameResources();
			}
		}

		public void enqueueFrameResources() {
			Controller.getResLoader().loadAll();
			Controller.getResLoader().loadClient();
		}

		public static void ProcessErrorDialogException(StateParameterNotFoundException e, InputState s) {
			if(s != null && s is StateInterface stateInterface) {
				ProcessErrorDialogException(e, stateInterface);
			}
			else {
				ProcessErrorDialogException(e, (StateInterface)null);
			}
		}

		public bool synchByFrame() { return false; }

		public void queueException(Exception e) { handleException(e); }
	}
}