using System;
using System.Diagnostics; // For Stopwatch
using UnityEngine; // For Time.deltaTime, Time.realtimeSinceStartup

namespace Org.Schema.Schine.GraphicsEngine.Core
{
    public class Timer
    {
        // Original Java: public static double TIMER_RESULUTION = 1000000000; // Nanoseconds
        // In C#, Stopwatch.Frequency gives ticks per second.
        // For delta time, Unity's Time.deltaTime is preferred.

        public long lastUpdate;
        public long currentTime;
        private long _lastFrame = 0;
        public long fps;
        private long _lastFPS;
        private int _frames;
        private long _thisFrame = 0;
        private double _delta; // This will be updated by Time.deltaTime
        public double lastDrawMilli;
        public double lastDrawMilliCount;
        public int counter;

        private Stopwatch _stopwatch = new Stopwatch();

        public Timer()
        {
            _stopwatch.Start(); // Start the stopwatch
        }

        /// <summary>
        /// Returns the time elapsed since the last frame in seconds.
        /// In Unity, this is typically Time.deltaTime.
        /// </summary>
        public float getDelta()
        {
            return Time.deltaTime;
        }

        /// <summary>
        /// @return the fps
        /// </summary>
        public long getFps()
        {
            return fps;
        }

        public void initialize(bool server)
        {
            _lastFPS = _stopwatch.ElapsedMilliseconds; // Use Stopwatch for milliseconds
            _lastFrame = _stopwatch.ElapsedTicks; // Use Stopwatch for high-res ticks
        }

        public void updateFPS(bool server)
        {
            long time = _stopwatch.ElapsedMilliseconds;
            this.currentTime = time;
            if (time - _lastFPS > 1000)
            {
                fps = _frames;
                _frames = 0; // reset the FPS counter
                _lastFPS = time; // add one second
            }
            _frames++;

            // Update delta time using Unity's Time.deltaTime
            _delta = Time.deltaTime;

            _lastFrame = _thisFrame;
            _thisFrame = _stopwatch.ElapsedTicks; // Update high-res ticks for internal use

            lastUpdate++;
        }
    }
}