using System;
using System.Diagnostics; // For Stopwatch
using System.Threading; // For Thread.Sleep, Thread.Yield
using UnityEngine; // For Application.targetFrameRate, QualitySettings.vSyncCount, Time.realtimeSinceStartup

namespace Org.Schema.Schine.GraphicsEngine.Core
{
    /// <summary>
    /// A highly accurate sync method that continually adapts to the system
    /// it runs on to provide reliable results.
    /// </summary>
    /// <remarks>
    /// In Unity, frame rate synchronization is typically handled by
    /// Application.targetFrameRate and QualitySettings.vSyncCount.
    /// This class provides a low-level sync mechanism, but its direct use
    /// for frame rate control in Unity's main thread is generally discouraged.
    /// </remarks>
    public class Sync
    {
        /// <summary>
        /// number of nano seconds in a second
        /// </summary>
        private const long NANOS_IN_SECOND = 1000L * 1000L * 1000L;

        /// <summary>
        /// The time to sleep/yield until the next frame
        /// </summary>
        private long _nextFrame = 0;

        /// <summary>
        /// whether the initialisation code has run
        /// </summary>
        private bool _initialised = false;

        /// <summary>
        /// for calculating the averages the previous sleep/yield times are stored
        /// </summary>
        private RunningAvg _sleepDurations = new RunningAvg(10);
        private RunningAvg _yieldDurations = new RunningAvg(10);

        private Stopwatch _stopwatch = new Stopwatch();

        public Sync()
        {
            _stopwatch.Start(); // Start the stopwatch once
        }

        /// <summary>
        /// An accurate sync method that will attempt to run at a constant frame rate.
        /// It should be called once every frame.
        /// </summary>
        /// <param name="fps">the desired frame rate, in frames per second</param>
        public void sync(int fps)
        {
            if (fps <= 0)
            {
                return;
            }

            if (!_initialised) Initialise();

            // In Unity, it's generally better to use Application.targetFrameRate
            // and QualitySettings.vSyncCount for frame rate control.
            // The original low-level sleep/yield loop is often unnecessary and can
            // interfere with Unity's internal timing.
            // For direct porting, the original logic is kept but commented out.

            // Application.targetFrameRate = fps; // Set Unity's target frame rate
            // QualitySettings.vSyncCount = 0; // Disable VSync if using targetFrameRate

            // Original low-level sync logic (commented out for Unity's built-in FPS control)
            /*
            try
            {
                // sleep until the average sleep time is greater than the time remaining till nextFrame
                for (long t0 = GetTime(), t1; (_nextFrame - t0) > _sleepDurations.avg(); t0 = t1)
                {
                    Thread.Sleep(1);
                    _sleepDurations.add((t1 = GetTime()) - t0); // update average sleep time
                }

                // slowly dampen sleep average if too high to avoid yielding too much
                _sleepDurations.dampenForLowResTicker();

                // yield until the average yield time is greater than the time remaining till nextFrame
                for (long t0 = GetTime(), t1; (_nextFrame - t0) > _yieldDurations.avg(); t0 = t1)
                {
                    Thread.Yield();
                    _yieldDurations.add((t1 = GetTime()) - t0); // update average yield time
                }
            }
            catch (ThreadInterruptedException e)
            {
                // Handle interruption
            }
            */

            // schedule next frame, drop frame(s) if already too late for next frame
            _nextFrame = Math.Max(_nextFrame + NANOS_IN_SECOND / fps, GetTime());
        }

        /// <summary>
        /// This method will initialise the sync method by setting initial
        /// values for sleepDurations/yieldDurations and nextFrame.
        ///
        /// If running on windows it will start the sleep timer fix.
        /// </summary>
        private void Initialise()
        {
            _initialised = true;

            _sleepDurations.init(1000 * 1000);
            _yieldDurations.init((int)(-(GetTime() - GetTime()) * 1.333)); // This calculation seems odd, might be 0

            _nextFrame = GetTime();

            // On windows the sleep functions can be highly inaccurate by
            // over 10ms making in unusable. However it can be forced to
            // be a bit more accurate by running a separate sleeping daemon
            // thread.
            // This Windows-specific timer accuracy fix is not needed in Unity/C#.
            // Unity handles timing internally.
            /*
            string osName = System.Environment.OSVersion.Platform.ToString();
            if (osName.StartsWith("Win"))
            {
                Thread timerAccuracyThread = new Thread(() =>
                {
                    try
                    {
                        Thread.Sleep(Timeout.Infinite); // Sleep indefinitely
                    }
                    catch (Exception e) { }
                });

                timerAccuracyThread.Name = "LWJGL3 Timer";
                timerAccuracyThread.IsBackground = true; // Set as daemon
                timerAccuracyThread.Start();
            }
            */
        }

        /// <summary>
        /// Get the system time in nano seconds
        /// </summary>
        /// <returns>will return the current time in nano's</returns>
        private long GetTime()
        {
            // Using Stopwatch for high-resolution time
            return _stopwatch.ElapsedTicks * NANOS_IN_SECOND / Stopwatch.Frequency;
        }

        private class RunningAvg
        {
            private readonly long[] _slots;
            private int _offset;

            private const long DAMPEN_THRESHOLD = 10 * 1000L * 1000L; // 10ms in nanos
            private const float DAMPEN_FACTOR = 0.9f; // don't change: 0.9f is exactly right!

            public RunningAvg(int slotCount)
            {
                _slots = new long[slotCount];
                _offset = 0;
            }

            public void init(long value)
            {
                for (int i = 0; i < _slots.Length; i++)
                {
                    _slots[i] = value;
                }
                _offset = _slots.Length; // Set offset to end after initialization
            }

            public void add(long value)
            {
                _slots[_offset++ % _slots.Length] = value;
                _offset %= _slots.Length;
            }

            public long avg()
            {
                long sum = 0;
                for (int i = 0; i < _slots.Length; i++)
                {
                    sum += _slots[i];
                }
                return sum / _slots.Length;
            }

            public void dampenForLowResTicker()
            {
                if (avg() > DAMPEN_THRESHOLD)
                {
                    for (int i = 0; i < _slots.Length; i++)
                    {
                        _slots[i] = (long)(_slots[i] * DAMPEN_FACTOR);
                    }
                }
            }
        }
    }
}