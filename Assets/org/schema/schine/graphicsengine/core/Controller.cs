using System.Collections.Generic;
using UnityEngine;

namespace Org.Schema.Schine.GraphicsEngine.Core
{
    public class Controller
    {
        private static readonly GameControllerInput controllerInput = new GameControllerInput();
        private static readonly TextureLoader texLoader = new TextureLoader();
        public static Visability Vis;
        public static bool FreeCamSticky = false;
        public static int[] Viewport = new int[16];
        public static List<MouseEvent> MouseEvents = new List<MouseEvent>();
        public static AbstractViewer Viewer;
        public static Matrix4x4 ModelviewMatrix = new Matrix4x4();
        public static Matrix4x4 ProjectionMatrix = new Matrix4x4();
        public static List<int> LoadedVBOBuffers = new List<int>();
        public static List<int> LoadedFrameBuffers = new List<int>();
        public static List<int> LoadedRenderBuffers = new List<int>();
        public static List<int> LoadedTextures = new List<int>();
        public static bool CheckJoystick;
        private static ResourceLoader resLoader;
        private static Camera camera;
        private static float[] tmp = new float[16];
        private static float[] tmpFl = new float[16];
        private IDrawableScene drawable;

        public Controller(float width, float height, IDrawableScene drawable, GLFrame glFrame)
        {
            this.drawable = drawable;
        }

        public static Camera GetCamera()
        {
            return camera;
        }

        public static void SetCamera(Camera camera)
        {
            Controller.camera = camera;
        }

        public static ResourceLoader GetResLoader()
        {
            return resLoader;
        }

        public static TextureLoader GetTexLoader()
        {
            return texLoader;
        }

        public static void InitResLoader(ResourceLoader resLoader)
        {
            if (Controller.resLoader == null)
            {
                Controller.resLoader = resLoader;
            }
        }

        public static void OnViewportChange()
        {
            //TODO: Implement with Unity
            //GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
            Debug.Log("[CLIENT][GRAPHICS] VIEWPORT CHANGED: " + Viewport[0] + "; " + Viewport[1] + "; " + Viewport[2] + "; " + Viewport[3]);
        }

        public static void CleanUpAllBuffers()
        {
            //TODO: Implement with Unity
            //foreach (int i in loadedFrameBuffers) {
            //    GL30.glDeleteFramebuffers(i);
            //}
            //foreach (int i in loadedVBOBuffers) {
            //    GL15.glDeleteBuffers(i);
            //}
            //foreach (int i in loadedRenderBuffers) {
            //    GL30.glDeleteRenderbuffers(i);
            //}
            //foreach (int i in loadedTextures) {
            //    GL11.glDeleteTextures(i);
            //}
            LoadedVBOBuffers.Clear();
            LoadedFrameBuffers.Clear();
            LoadedRenderBuffers.Clear();
            LoadedTextures.Clear();
        }

        public static void GetMat(Matrix4x4 mat, float[] output)
        {
            //TODO: Implement with Unity
        }

        public static void GetMat(Matrix4x4 mat, Transform output)
        {
            //TODO: Implement with Unity
        }

        public static void GetMat(Transform mat, Matrix4x4 output)
        {
            //TODO: Implement with Unity
        }

        public static GameControllerInput GetControllerInput()
        {
            return controllerInput;
        }

        public IDrawableScene GetDrawable()
        {
            return drawable;
        }

        public void SetDrawable(IDrawableScene drawable)
        {
            this.drawable = drawable;
        }

        public void SwitchCamera(Camera camera)
        {
            Controller.SetCamera(camera);
        }

        public static void SetLoadMessage(string str)
        {
            if (resLoader != null)
            {
                resLoader.SetLoadString(str);
            }
        }
    }
}