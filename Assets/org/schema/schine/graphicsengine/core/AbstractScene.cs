using System;
using System.Collections.Generic;
using UnityEngine;

namespace Org.Schema.Schine.GraphicsEngine.Core
{
    public abstract class AbstractScene : IDrawableScene
    {
        public static readonly List<Light> SpotLights = new List<Light>();
        private static readonly float[] projMatBuffer = new float[16];
        public static List<string> InfoList = new List<string>();
        public static Light MainLight;
        public static float FarPlane = 500000.0f; //one astronomical unit
        public static float NearPlane = 1.0f;
        public static SortedList<ZSortedDrawable> ZSortedMap = new SortedList<ZSortedDrawable>();
        static float[] f = new float[1];
        static float[] coord = new float[3];
        static bool test = true;
        private static float zoomFactor = 1;
        protected bool firstDraw = true;
        protected FrameBufferObjects fbo;
        protected Exception requestedThrowError;

        Vector3 rayFrom = new Vector3();
        Vector3 rayForward = new Vector3();
        Vector3 rightOffset = new Vector3();
        Vector3 vertical = new Vector3();

        //TODO: Replace with Unity UI
        //GUITextOverlay infoTextOverlay;
        //GUITextOverlay pingTextOverlay;
        private Camera sceneCamera;
        private bool textInit;
        private StateInterface state;

        public AbstractScene(StateInterface state)
        {
            Initialize();
            this.state = state;
        }

        public static Vector3 GetAbsoluteMousePosition(Vector3 absMousePos)
        {
            //TODO: Implement with Unity input system
            return Vector3.zero;
        }

        protected static float GetDepthOfPixel(int x, int y)
        {
            //TODO: Implement with Unity render textures
            return 0;
        }

        public static void SetZoomFactorForRender(bool mainWorldrender, float value)
        {
            if (mainWorldrender)
            {
                zoomFactor = value;
            }
        }

        public static float GetZoomFactorForRender(bool mainWorldRender)
        {
            return mainWorldRender ? zoomFactor : 1.0F;
        }

        public static float GetZoomFactorUnchecked()
        {
            return zoomFactor;
        }

        public static float GetFarPlane()
        {
            return FarPlane;
        }

        public void SetFarPlane(float farPlane)
        {
            FarPlane = farPlane;
        }

        public static float GetNearPlane()
        {
            return NearPlane;
        }

        public void SetNearPlane(float nearPlane)
        {
            NearPlane = nearPlane;
        }

        public static void InitProjection()
        {
        }

        public abstract void AddPhysicsDebugDrawer();

        public abstract void ApplyEngineSettings();

        protected void AttachCamera()
        {
            Controller.Viewer = new PositionableViewer();
            sceneCamera = new Camera(state, Controller.Viewer);
            Controller.SetCamera(sceneCamera);
        }

        public abstract void CleanUp();

        public void InitProjection(int ocMode)
        {
            //TODO: Set up projection matrix
        }

        protected void DrawInfo()
        {
            //TODO: Implement with Unity UI
        }

        public void DrawLine(Vector3 from, Vector3 to, Color color)
        {
            Debug.DrawLine(from, to, color);
        }

        public Light GetMainLight()
        {
            return MainLight;
        }

        public void SetMainLight(Light mainLight)
        {
            MainLight = mainLight;
        }

        public string GetPlayerName()
        {
            return "unknownScene";
        }

        public Vector3 GetRayTo(int x, int y)
        {
            //TODO: Implement with Unity camera
            return Vector3.zero;
        }

        public Camera GetSceneCamera()
        {
            return sceneCamera;
        }

        public void SetSceneCamera(Camera sceneCamera)
        {
            this.sceneCamera = sceneCamera;
        }

        protected void Initialize()
        {
            Light.ResetLightAssignment();
            MainLight = new Light();
            MainLight.SetPos(450f, 900f, 0f);

            //TODO: Replace with Unity UI
            //infoTextOverlay = new GUITextOverlay(null);
            //infoTextOverlay.SetText(new List<string>());
            //infoTextOverlay.UseUncachedDefaultFont(true);

            //pingTextOverlay = new GUITextOverlay(null);
            //pingTextOverlay.UseUncachedDefaultFont(true);

            //List<object> objectArrayList = new List<object>(1);
            //objectArrayList.Add("");
            //pingTextOverlay.SetText(objectArrayList);
        }

        public void RequestThrowException(Exception e)
        {
            this.requestedThrowError = e;
        }
    }
}