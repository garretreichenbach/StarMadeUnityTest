using System;
using UnityEngine;
using System.Collections.Generic;
using System.IO; // For File
using System.Text; // For Encoding
using System.Linq; // For Linq extensions

// Using aliases for clarity and to avoid conflicts with UnityEngine types
using Matrix4x4 = UnityEngine.Matrix4x4;
using Vector3 = UnityEngine.Vector3;
using Vector4 = UnityEngine.Vector4;
using Color = UnityEngine.Color;

// Placeholders for custom types and external libraries
using Org.Schema.Common;
using Org.Schema.Common.Util.LinAlg;
using Org.Schema.Game.Common.Data.Element;
using Org.Schema.Schine.GraphicsEngine.Core.Settings;
using Org.Schema.Schine.GraphicsEngine.Forms;
using Org.Schema.Schine.GraphicsEngine.Shader;
using Org.Schema.Schine.Input;
using Org.Schema.Schine.Resource;
using com.bulletphysics.linearmath; // JBullet Transform and AabbUtil2
using Org.Schema.Schine.GraphicsEngine.Core; // For AbstractScene, Controller, GLException, GLFrame

namespace Org.Schema.Common
{
    public static class FastMath
    {
        public const float PI = Mathf.PI;
        public const float DEG_TO_RAD = Mathf.Deg2Rad;

        // Simplified versions of original FastMath methods
        public static float sin(float f) { return Mathf.Sin(f); }
        public static float cos(float f) { return Mathf.Cos(f); }
        public static float atan2(float y, float x) { return Mathf.Atan2(y, x); }
        public static float sqrt(float f) { return Mathf.Sqrt(f); }
        public static void normalizeCarmack(Vector3 v) { v.Normalize(); } // Simplified
    }
}

namespace Org.Schema.Common.Util.LinAlg
{
    // These will be absorbed into GlUtil or replaced by Unity's Matrix4x4/Vector3 methods
    // Keeping them as empty placeholders for now to avoid compilation errors during conversion
    public static class Matrix4fTools
    {
        public static void store(Matrix4x4 mat, float[] buffer) { /* Implementation in GlUtil */ }
        public static void load(float[] buffer, Matrix4x4 mat) { /* Implementation in GlUtil */ }
        public static void mul(Matrix4x4 left, Matrix4x4 right, Matrix4x4 result) { result = left * right; }
        public static void rotate(Matrix4x4 mat, float angle, Vector3 axis) { /* Implementation in GlUtil */ }
        public static void scale(Matrix4x4 mat, Vector3 scale) { /* Implementation in GlUtil */ }
        public static void unproject(Matrix4x4 projection, Matrix4x4 modelview, float winX, float winY, float winZ, int[] viewport, out Vector4 coord) { /* Implementation in GlUtil */ }
    }

    public static class Vector3fTools
    {
        public static Vector3 crossProduct(Vector3 v1, Vector3 v2, Vector3 result) { result = Vector3.Cross(v1, v2); return result; }
        public static void addScaled(Vector3 target, float scale, Vector3 source) { target += source * scale; }
    }
}

namespace Org.Schema.Game.Common.Data.Element
{
    public static class Element
    {
        public const int FRONT = 0;
        public const int BACK = 1;
        public const int LEFT = 2;
        public const int RIGHT = 3;
        public const int TOP = 4;
        public const int BOTTOM = 5;
    }
}

namespace Org.Schema.Schine.GraphicsEngine.Core.Settings
{
    public class EngineSettings
    {
        public static DebugInfoShaderErrors D_INFO_SHADER_ERRORS = new DebugInfoShaderErrors();
    }

    public class DebugInfoShaderErrors
    {
        public bool isOn() { return false; }
    }
}

namespace Org.Schema.Schine.GraphicsEngine.Forms
{
    public class Mesh
    {
        public BoundingBox BoundingBox = new BoundingBox();
    }

    public class BoundingBox
    {
        public Vector3 min = Vector3.zero;
        public Vector3 max = Vector3.zero;
    }
}

namespace Org.Schema.Schine.GraphicsEngine.Shader
{
    public class Shader
    {
        public string getVertexshaderPath() { return "path/to/shader"; }
    }
}

namespace Org.Schema.Schine.Input
{
    public class Mouse
    {
        // Placeholder
    }
}

namespace Org.Schema.Schine.Resource
{
    // FileExt is already defined in GLFrame.cs
}

namespace com.bulletphysics.linearmath
{
    // Transform is already defined in GLDebugDrawer.cs
    public class AabbUtil2
    {
        public static void transformAabb(Vector3 aabbMin, Vector3 aabbMax, float margin, Transform trans, Vector3 aabbMinOut, Vector3 aabbMaxOut)
        {
            // Simplified placeholder for AABB transformation
            // In a real scenario, this would involve transforming the 8 corners of the AABB
            // and finding the new min/max.
            aabbMinOut.Set(aabbMin.x, aabbMin.y, aabbMin.z);
            aabbMaxOut.Set(aabbMax.x, aabbMax.y, aabbMax.z);
        }
    }
}

namespace Org.Schema.Schine.GraphicsEngine.Core
{
    // AbstractScene is used for infoList
    public class AbstractScene
    {
        public static List<string> infoList = new List<string>();
    }

    // Controller is used for modelviewMatrix, projectionMatrix, getCamera
    public partial class Controller
    {
        public static Matrix4x4 modelviewMatrix = Matrix4x4.identity;
        public static Matrix4x4 projectionMatrix = Matrix4x4.identity;
        public static Camera getCamera() { return Camera.main; } // Placeholder
    }

    // GLException
    public class GLException : Exception
    {
        public GLException(string message) : base(message) { }
    }
}

// Custom Matrix3x3 struct for javax.vecmath.Matrix3f conversion
public struct Matrix3x3
{
    public float m00, m01, m02;
    public float m10, m11, m12;
    public float m20, m21, m22;

    public Matrix3x3(float m00, float m01, float m02,
                     float m10, float m11, float m12,
                     float m20, float m21, float m22)
    {
        this.m00 = m00; this.m01 = m01; this.m02 = m02;
        this.m10 = m10; this.m11 = m11; this.m12 = m12;
        this.m20 = m20; this.m21 = m21; this.m22 = m22;
    }

    public void set(Matrix3x3 other)
    {
        m00 = other.m00; m01 = other.m01; m02 = other.m02;
        m10 = other.m10; m11 = other.m11; m12 = other.m12;
        m20 = other.m20; m21 = other.m21; m22 = other.m22;
    }

    public void setColumn(int column, Vector3 v)
    {
        switch (column)
        {
            case 0: m00 = v.x; m10 = v.y; m20 = v.z; break;
            case 1: m01 = v.x; m11 = v.y; m21 = v.z; break;
            case 2: m02 = v.x; m12 = v.y; m22 = v.z; break;
            default: throw new IndexOutOfRangeException("Column index out of range.");
        }
    }

    public void getColumn(int column, Vector3 v)
    {
        switch (column)
        {
            case 0: v.Set(m00, m10, m20); break;
            case 1: v.Set(m01, m11, m21); break;
            case 2: v.Set(m02, m12, m22); break;
            default: throw new IndexOutOfRangeException("Column index out of range.");
        }
    }

    public void setRow(int row, Vector3 v)
    {
        switch (row)
        {
            case 0: m00 = v.x; m01 = v.y; m02 = v.z; break;
            case 1: m10 = v.x; m11 = v.y; m12 = v.z; break;
            case 2: m20 = v.x; m21 = v.y; m22 = v.z; break;
            default: throw new IndexOutOfRangeException("Row index out of range.");
        }
    }

    public void invert()
    {
        // Simplified inversion for 3x3 matrix.
        // For a proper implementation, refer to linear algebra libraries.
        // This is a placeholder.
        Debug.LogWarning("Matrix3x3.invert() is a placeholder and might not be accurate.");
        float det = m00 * (m11 * m22 - m12 * m21) -
                    m01 * (m10 * m22 - m12 * m20) +
                    m02 * (m10 * m21 - m11 * m20);

        if (det == 0)
        {
            Debug.LogError("Matrix3x3 cannot be inverted: Determinant is zero.");
            return;
        }

        float invDet = 1.0f / det;

        float nm00 = (m11 * m22 - m12 * m21) * invDet;
        float nm01 = (m02 * m21 - m01 * m22) * invDet;
        float nm02 = (m01 * m12 - m02 * m11) * invDet;

        float nm10 = (m12 * m20 - m10 * m22) * invDet;
        float nm11 = (m00 * m22 - m02 * m20) * invDet;
        float nm12 = (m02 * m10 - m00 * m12) * invDet;

        float nm20 = (m10 * m21 - m11 * m20) * invDet;
        float nm21 = (m01 * m20 - m00 * m21) * invDet;
        float nm22 = (m00 * m11 - m01 * m10) * invDet;

        m00 = nm00; m01 = nm01; m02 = nm02;
        m10 = nm10; m11 = nm11; m12 = nm12;
        m20 = nm20; m21 = nm21; m22 = nm22;
    }
}

// Custom Quat4f struct for javax.vecmath.Quat4f conversion
public struct Quat4f
{
    public float x, y, z, w;

    public Quat4f(float x, float y, float z, float w)
    {
        this.x = x; this.y = y; this.z = z; this.w = w;
    }

    public void set(Matrix3x3 m)
    {
        // Simplified conversion from 3x3 matrix to quaternion.
        // For a proper implementation, refer to linear algebra libraries.
        // This is a placeholder.
        Debug.LogWarning("Quat4f.set(Matrix3x3) is a placeholder and might not be accurate.");
        float trace = m.m00 + m.m11 + m.m22;
        if (trace > 0)
        {
            float s = Mathf.Sqrt(trace + 1.0f) * 2.0f;
            w = 0.25f * s;
            x = (m.m21 - m.m12) / s;
            y = (m.m02 - m.m20) / s;
            z = (m.m10 - m.m01) / s;
        }
        else if (m.m00 > m.m11 && m.m00 > m.m22)
        {
            float s = Mathf.Sqrt(1.0f + m.m00 - m.m11 - m.m22) * 2.0f;
            w = (m.m21 - m.m12) / s;
            x = 0.25f * s;
            y = (m.m01 + m.m10) / s;
            z = (m.m02 + m.m20) / s;
        }
        else if (m.m11 > m.m22)
        {
            float s = Mathf.Sqrt(1.0f + m.m11 - m.m00 - m.m22) * 2.0f;
            w = (m.m02 - m.m20) / s;
            x = (m.m01 + m.m10) / s;
            y = 0.25f * s;
            z = (m.m12 + m.m21) / s;
        }
        else
        {
            float s = Mathf.Sqrt(1.0f + m.m22 - m.m00 - m.m11) * 2.0f;
            w = (m.m10 - m.m01) / s;
            x = (m.m02 + m.m20) / s;
            y = (m.m12 + m.m21) / s;
            z = 0.25f * s;
        }
    }
}

namespace Org.Schema.Schine.GraphicsEngine.Core
{
    /// <summary>
    /// The Class GlUtil.
    /// </summary>
    public static class GlUtil
    {
        public static readonly HashSet<int> locked = new HashSet<int>();
        public static readonly float PI_OVER_360 = FastMath.PI / 360.0f;

        // Unity's GL class uses its own internal matrix stacks.
        // These fields represent the custom matrix stacks from the original Java code.
        // Their direct manipulation might be redundant if only Unity's GL functions are used.
        // However, for direct porting, we'll keep them and update them alongside GL calls.
        public static Matrix4x4 ProjMatrix = Matrix4x4.identity; // Replaces projBuffer
        public static Matrix4x4 ModelMatrix = Matrix4x4.identity; // Replaces modelBuffer
        public static Matrix4x4 PModelMatrix = Matrix4x4.identity; // Replaces pModelBuffer
        public static Matrix4x4 MvpMatrix = Matrix4x4.identity; // Replaces mvpBuffer
        public static Matrix4x4 NormalMatrix = Matrix4x4.identity; // Replaces normalBuffer

        public static int[] NameBuffer = new int[1]; // Replaces nameBuffer
        public static Matrix4x4 TmpMat = Matrix4x4.identity; // Replaces tmpMat
        public static int matrixMode; // GL.PROJECTION or GL.MODELVIEW

        public static Shader LoadedShader; // Replaces loadedShader
        public static bool LOCKDYN;
        public static int CurrentBoundBuffer; // Replaces currentBoundBuffer
        public static int CurrentBoundBufferTarget; // Replaces currentBoundBufferTarget
        public static bool ColorMask = true; // Replaces colorMask

        static Matrix3x3 VecMathMat3fTmp = new Matrix3x3(); // Replaces vecMathMat3fTmp

        static Vector3 MinBB = Vector3.zero; // Replaces minBB
        static Vector3 MaxBB = Vector3.zero; // Replaces maxBB
        static Vector3 TransformHelper = Vector3.zero; // Replaces transformHelper
        static Transform T = new Transform(); // Replaces t (JBullet Transform)

        private static Vector3 XFac = Vector3.zero; // Replaces xFac
        private static Vector3 YFac = Vector3.zero; // Replaces yFac
        private static readonly Vector3 ZFac = Vector3.zero; // Replaces zFac
        private static readonly Transform LookT = new Transform(); // Replaces lookT (JBullet Transform)
        private static readonly int[] IBuff = new int[1]; // Replaces iBuff

        // dynamicByteBuffer is a custom memory management for ByteBuffers.
        // In C#, we'll use byte arrays and let GC handle memory.
        // private static readonly byte[][] DynamicByteBuffer = new byte[256][]; // Replaces dynamicByteBuffer

        private static readonly Vector3 ATemp = Vector3.zero; // Replaces aTemp
        private static readonly Vector3 BTemp = Vector3.zero; // Replaces bTemp
        private static readonly Vector3 CTemp = Vector3.zero; // Replaces cTemp
        private static readonly Vector3 DTemp = Vector3.zero; // Replaces dTemp
        private static readonly Vector4 A4Temp = Vector4.zero; // Replaces a4Temp
        private static readonly Vector4 B4Temp = Vector4.zero; // Replaces b4Temp
        private static readonly Vector4 C4Temp = Vector4.zero; // Replaces c4Temp
        private static readonly Vector4 D4Temp = Vector4.zero; // Replaces d4Temp
        private static readonly Vector3 ETemp = Vector3.zero; // Replaces eTemp
        private static readonly Vector3 FTemp = Vector3.zero; // Replaces fTemp
        private static readonly Vector3 GTemp = Vector3.zero; // Replaces gTemp
        private static readonly Vector3 HTemp = Vector3.zero; // Replaces hTemp
        private static readonly Vector3 MinBBOut = Vector3.zero; // Replaces minBBOut
        private static readonly Vector3 MaxBBOut = Vector3.zero; // Replaces maxBBOut
        private static readonly Vector4 PosTmp = Vector4.zero; // Replaces posTmp
        private static readonly Vector4 PosTmp2 = Vector4.zero; // Replaces posTmp2
        private static readonly Vector3 CamPosTest = Vector3.zero; // Replaces camPosTest
        private static readonly Vector3 Look = Vector3.zero; // Replaces look
        private static readonly Vector3 Up = Vector3.zero; // Replaces up
        private static readonly Vector3 Right = Vector3.zero; // Replaces right

        // Custom matrix stacks
        private static readonly Matrix4x4[] ModelStack = new Matrix4x4[64]; // Replaces modelstack
        private static int ModelStackPointer; // Replaces modelstackPointer
        private static readonly Vector4[] GuiClipStack = new Vector4[64]; // Replaces guiClipStack
        private static readonly Matrix4x4[] GuiClipStackTransformsM = new Matrix4x4[64]; // Replaces guiClipStackTransformsM
        private static readonly Matrix4x4[] GuiClipStackTransformsP = new Matrix4x4[64]; // Replaces guiClipStackTransformsP
        private static int GuiClipStackPointer = -1; // Replaces guiClipStackPointer
        private static readonly Matrix4x4[] ProjectStack = new Matrix4x4[64]; // Replaces projectstack
        private static int ProjectStackPointer; // Replaces projectstackPointer

        private static readonly float[] FloatArrayBuffer = new float[16]; // Replaces floatArrayBuffer
        private static readonly Vector4 CurrentColor = Vector4.zero; // Replaces currentColor

        // DoubleBuffer fields are removed as they are not idiomatic in C# Unity
        // private static readonly DoubleBuffer leftL;
        // private static readonly DoubleBuffer rightL;
        // private static readonly DoubleBuffer topL;
        // private static readonly DoubleBuffer bottomL;

        private static bool Blended; // Replaces blended
        private static int CurrentMode; // Replaces currentMode
        public static float ScrollX; // Replaces scrollX
        public static float ScrollY; // Replaces scrollY
        private static bool StartedTextureCaching; // Replaces startedTextureCaching

        static GlUtil()
        {
            // Initialize custom matrix stacks
            for (int i = 0; i < ModelStack.Length; i++)
            {
                ModelStack[i] = Matrix4x4.identity;
            }
            for (int i = 0; i < ProjectStack.Length; i++)
            {
                ProjectStack[i] = Matrix4x4.identity;
            }
            for (int i = 0; i < GuiClipStack.Length; i++)
            {
                GuiClipStack[i] = Vector4.zero;
            }
            for (int i = 0; i < GuiClipStackTransformsM.Length; i++)
            {
                GuiClipStackTransformsM[i] = Matrix4x4.identity;
                GuiClipStackTransformsP[i] = Matrix4x4.identity;
            }
        }

        //*****************************************************************************
        public static Transform BillboardAbitraryAxis(Vector3 pos, Vector3 axis, Vector3 camPos, Transform @out)
        {
            // create the look vector: pos -> camPos
            Look.Set(camPos.x - pos.x, camPos.y - pos.y, camPos.z - pos.z);
            FastMath.normalizeCarmack(Look);

            // billboard about the direction vector
            Up.Set(axis.x, axis.y, axis.z);
            Right = Vector3.Cross(Up, Look);

            // watch out when the look vector is almost equal to the up vector the right
            // vector gets close to zeroed, normalize it
            FastMath.normalizeCarmack(Right);

            // the billboard won't actually face the direction of the look vector we
            // created earlier, that was just used as a tempory vector to create the
            // right vector so we could calculate the correct look vector from that.
            Look = Vector3.Cross(Right, Up);

            SetUpVector(Up, @out);
            SetForwardVector(Look, @out);
            SetRightVector(Right, @out);
            @out.origin.Set(pos.x, pos.y, pos.z);

            return @out;
        }

        public static void DrawSphere(float radius, float gradation)
        {
            // In Unity, direct GL drawing of complex primitives like spheres is usually done
            // by generating a mesh or using Gizmos.DrawWireSphere in editor.
            // This re-implementation uses GL.TRIANGLE_STRIP for direct drawing.
            // A material must be set up for GL drawing to work.
            // Example:
            // Material lineMaterial = new Material(Shader.Find("Hidden/Internal-Colored"));
            // lineMaterial.hideFlags = HideFlags.HideAndDontSave;
            // lineMaterial.SetInt("_SrcBlend", (int)UnityEngine.Rendering.BlendMode.SrcAlpha);
            // lineMaterial.SetInt("_DstBlend", (int)UnityEngine.Rendering.BlendMode.OneMinusSrcAlpha);
            // lineMaterial.SetInt("_Cull", (int)UnityEngine.Rendering.CullMode.Off);
            // lineMaterial.SetInt("_ZWrite", 0);
            // lineMaterial.SetPass(0);

            float PI = FastMath.PI;
            float x, y, z, alpha, beta; // Storage for coordinates and angles

            for (alpha = 0.0f; alpha < PI; alpha += PI / gradation)
            {
                GL.Begin(GL.TRIANGLE_STRIP);
                for (beta = 0.0f; beta < 2.01 * PI; beta += PI / gradation)
                {
                    x = radius * FastMath.cos(beta) * FastMath.sin(alpha);
                    y = radius * FastMath.sin(beta) * FastMath.sin(alpha);
                    z = radius * FastMath.cos(alpha);
                    GL.Vertex3(x, y, z);
                    x = radius * FastMath.cos(beta) * FastMath.sin(alpha + PI / gradation);
                    y = radius * FastMath.sin(beta) * FastMath.sin(alpha + PI / gradation);
                    z = radius * FastMath.cos(alpha + PI / gradation);
                    GL.Vertex3(x, y, z);
                }
                GL.End();
            }
        }

        public static void BuildAxisAlignedBBMatrix(float x, float y, float z, float[] m)
        {
            // This method calculates a rotation matrix based on an axis.
            // In Unity, you'd typically use Quaternion.AngleAxis or Quaternion.LookRotation.
            // Re-implementing the original math for direct porting.

            float theta = -180 * FastMath.atan2(m[8], m[10]) / FastMath.PI; // This line seems to use m before it's set.
                                                                           // Assuming m[8] and m[10] are from an existing matrix.
                                                                           // If m is meant to be an output, this is a bug in original.
                                                                           // For now, I'll assume m is an input and output.

            float d = x * x + y * y + z * z;
            float ct = FastMath.cos(FastMath.DEG_TO_RAD * (theta));
            float st = FastMath.sin(FastMath.DEG_TO_RAD * (theta));

            // Normalize
            if (d > 0)
            {
                d = 1 / d;
                x *= d;
                y *= d;
                z *= d;
            }

            // Clear out the view matrix passed in
            // This part is problematic if m is meant to be an output.
            // Assuming it's clearing a pre-existing matrix.
            for (int i = 0; i < 16; i++) m[i] = 0;
            m[0] = 1; m[5] = 1; m[10] = 1; m[15] = 1; // Identity

            // R = uu' + cos(theta)*(I-uu') + sin(theta)*S
            // S =  0  -z   y    u' = (x, y, z)
            //	    z   0  -x
            //	   -y   x   0

            m[0] = x * x + ct * (1 - x * x);
            m[4] = x * y + ct * (-x * y) + st * -z;
            m[8] = x * z + ct * (-x * z) + st * y;

            m[1] = y * x + ct * (-y * x) + st * z;
            m[5] = y * y + ct * (1 - y * y);
            m[9] = y * z + ct * (-y * z) + st * -x;

            m[2] = z * x + ct * (-z * x) + st * -y;
            m[6] = z * y + ct * (-z * y) + st * x;
            m[10] = z * z + ct * (1 - z * z);
        }

        public static void BuildAxisAlignedBBMatrix(float x, float y, float z, Transform @out)
        {
            BuildAxisAlignedBBMatrix(x, y, z, FloatArrayBuffer);
            @out.setFromOpenGLMatrix(FloatArrayBuffer);
        }

        public static bool CheckAABB(Vector3 min, Vector3 max)
        {
            return min.x <= max.x && min.y <= max.y && min.z <= max.z;
        }

        public static void CreateStack()
        {
            // In Unity, GL.PushMatrix/PopMatrix manage their own internal stacks.
            // These custom stacks are for the game's internal matrix management.
            // They are initialized in the static constructor.
        }

        public static Matrix4x4 CreateViewMatrix(float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ)
        {
            return CreateViewMatrix(new Vector3(eyeX, eyeY, eyeZ), new Vector3(centerX, centerY, centerZ), new Vector3(upX, upY, upZ));
        }

        public static Matrix4x4 CreateViewMatrix(Vector3 eye, Vector3 center, Vector3 up)
        {
            // This is equivalent to Matrix4x4.LookAt, but re-implemented based on original Java logic.
            // Unity's Matrix4x4 is column-major, so the indices might need adjustment if the original was row-major.
            // javax.vecmath.Matrix4f is column-major.

            Matrix4x4 m = Matrix4x4.identity;

            Vector3 f = (center - eye).normalized;
            Vector3 u = up.normalized;

            Vector3 s = Vector3.Cross(f, u).normalized;
            u = Vector3.Cross(s, f).normalized; // Recompute u to be orthogonal to s and f

            // Populate the matrix. Note: Unity's Matrix4x4 is column-major.
            // The original Java code's m.mXY corresponds to Unity's m[X, Y] if it's column-major.
            // However, the original code then transposes. Let's follow the original logic.

            // Original Java:
            // m.m00 = s.x; m.m10 = s.y; m.m20 = s.z;
            // m.m01 = u.x; m.m11 = u.y; m.m21 = u.z;
            // m.m02 = -f.x; m.m12 = -f.y; m.m22 = -f.z;
            // m.transpose();

            // This means the original matrix was effectively row-major before transpose,
            // or it was column-major and then transposed to become row-major for OpenGL.
            // Let's construct it as if it's column-major for Unity's Matrix4x4.

            m.SetColumn(0, new Vector4(s.x, s.y, s.z, 0));
            m.SetColumn(1, new Vector4(u.x, u.y, u.z, 0));
            m.SetColumn(2, new Vector4(-f.x, -f.y, -f.z, 0));
            m.SetColumn(3, new Vector4(0, 0, 0, 1));

            // Apply translation for eye position
            Matrix4x4 translation = Matrix4x4.Translate(-eye);
            return m * translation; // View matrix is inverse of camera transform
        }

        public static Matrix4x4 CreateOrthogonalProjectionMatrix(float left, float right, float top, float bottom, float near, float far)
        {
            // Unity has Matrix4x4.Ortho. Re-implementing based on original Java logic.
            Matrix4x4 m = Matrix4x4.identity;

            float lateral = right - left;
            float vertical = top - bottom;
            float forward = far - near;
            float tx = -(right + left) / (right - left);
            float ty = -(top + bottom) / (top - bottom);
            float tz = -(far + near) / (far - near);

            // Original Java:
            // m.m00 = 2.0f / lateral;
            // m.m30 = tx;
            // m.m11 = 2.0f / vertical;
            // m.m31 = ty;
            // m.m22 = -2.0f / forward;
            // m.m32 = tz;
            // m.transpose();

            // This implies the original matrix was effectively row-major before transpose.
            // Let's construct it as if it's column-major for Unity's Matrix4x4.

            m[0, 0] = 2.0f / lateral;
            m[1, 1] = 2.0f / vertical;
            m[2, 2] = -2.0f / forward;
            m[0, 3] = tx; // Translation in column 3
            m[1, 3] = ty;
            m[2, 3] = tz;

            return m;
        }

        public static Matrix4x4 CreatePerspectiveProjectionMatrix(float fovY, float aspectRatio, float zNear, float zFar)
        {
            // Unity has Matrix4x4.Perspective. Re-implementing based on original Java logic.
            Matrix4x4 m = Matrix4x4.identity;

            float f = 1.0f / Mathf.Tan(fovY * 0.5f * Mathf.Deg2Rad);

            // Original Java:
            // m.m00 = f / aspectRatio;
            // m.m11 = f;
            // m.m22 = (zFar + zNear) / (zNear - zFar);
            // m.m32 = (2 * zFar * zNear) / (zNear - zFar);
            // m.m23 = -1;
            // m.transpose();

            // This implies the original matrix was effectively row-major before transpose.
            // Let's construct it as if it's column-major for Unity's Matrix4x4.

            m[0, 0] = f / aspectRatio;
            m[1, 1] = f;
            m[2, 2] = (zFar + zNear) / (zNear - zFar);
            m[3, 2] = -1; // This is the perspective divide row
            m[2, 3] = (2 * zFar * zNear) / (zFar - zFar); // Translation in Z

            return m;
        }

        public static void DisplayMemory(string what)
        {
            // NVXGPUMemoryInfo is NVIDIA specific and LWJGL.
            // Unity doesn't expose this directly in a cross-platform way.
            Debug.LogWarning($"[GL_MEMORY] DisplayMemory called for: {what}. GPU memory info not available cross-platform in Unity.");
        }

        /// <summary>
        /// Frees a byte buffer if its direct
        /// </summary>
        public static void DestroyDirectByteBuffer(object toBeDestroyed)
        {
            // In C#, memory is managed by the garbage collector.
            // Direct byte buffers (like java.nio.ByteBuffer.allocateDirect) are not a concept in C# in the same way.
            // This method is effectively a no-op in C#.
            Debug.LogWarning("DestroyDirectByteBuffer is a no-op in Unity C# as memory is managed by GC.");
        }

        public static Quat4f EulerToQuaternion(Vector3 euler, Quat4f @out)
        {
            // Original Java used javax.vecmath.Matrix3f as an intermediate.
            // Unity's Quaternion.Euler is the direct equivalent.
            Quaternion q = Quaternion.Euler(euler.x, euler.y, euler.z);
            @out.x = q.x;
            @out.y = q.y;
            @out.z = q.z;
            @out.w = q.w;
            return @out;
        }

        public static void FillBuffer(Transform t, float[] buff)
        {
            // This method fills a float array with the OpenGL matrix representation of a JBullet Transform.
            // In Unity, you'd typically use Matrix4x4.TRS or directly access the Transform's matrix.
            // Assuming 'buff' is a float[16] for OpenGL matrix.
            Matrix4x4 m = Matrix4x4.TRS(t.origin, t.basis.rotation, t.basis.lossyScale); // Simplified
            for (int i = 0; i < 16; i++)
            {
                buff[i] = m[i]; // Matrix4x4[index] accesses elements in column-major order
            }
        }

        public static Vector3 GetBackVector(Vector3 forward, Transform from)
        {
            // Assuming 'from' is a JBullet Transform.
            // In Unity, Transform.forward is the Z-axis. Back is -Z.
            // from.basis.getColumn(2, forward); // Original Java
            forward = -from.basis.GetColumn(2); // Simplified
            return forward;
        }

        /// <summary>
        /// gets the bottom vector of this transform
        /// </summary>
        /// <param name="bottom">out</param>
        /// <param name="from"></param>
        /// <returns></returns>
        public static Vector3 GetBottomVector(Vector3 bottom, Transform from)
        {
            GetUpVector(bottom, from);
            bottom = -bottom;
            return bottom;
        }

        public static byte[] GetDynamicByteBuffer(int size, int index)
        {
            // In C#, memory is managed by the garbage collector.
            // This method's purpose of managing direct byte buffers is not idiomatic.
            // Simply return a new byte array.
            Debug.LogWarning("GetDynamicByteBuffer is simplified to return a new byte[] in Unity C#.");
            return new byte[size];
        }

        public static Vector3 GetForwardVector(Vector3 forward)
        {
            // Original Java: modelBuffer.rewind(); Matrix4fTools.store(Controller.modelviewMatrix, modelBuffer);
            // forward.x = modelBuffer.get(2); forward.y = modelBuffer.get(6); forward.z = modelBuffer.get(10);
            // This gets the Z-axis (forward) from the modelview matrix.
            forward = Controller.modelviewMatrix.GetColumn(2); // Z-axis is column 2
            return forward;
        }

        /// <summary>
        /// forward is column 2
        /// </summary>
        /// <param name="forward">out</param>
        /// <param name="from"></param>
        /// <returns></returns>
        public static Vector3 GetForwardVector(Vector3 forward, Transform from)
        {
            // from.basis.getColumn(2, forward); // Original Java
            forward = from.basis.GetColumn(2);
            return forward;
        }

        public static void GetVectors(int orientation, Vector3 tForwardVector, Vector3 tUpVector, Vector3 tRightVector, Transform from)
        {
            switch (orientation)
            {
                case Element.FRONT:
                    GetForwardVector(tForwardVector, from);
                    GetUpVector(tUpVector, from);
                    GetRightVector(tRightVector, from);
                    break;
                case Element.BACK:
                    GetForwardVector(tForwardVector, from);
                    GetUpVector(tUpVector, from);
                    GetRightVector(tRightVector, from);
                    tForwardVector = -tForwardVector;
                    tRightVector = -tRightVector;
                    break;
                case Element.LEFT:
                    GetRightVector(tForwardVector, from); // Forward is Right
                    GetUpVector(tUpVector, from);
                    GetForwardVector(tRightVector, from); // Right is original Forward
                    break;
                case Element.RIGHT:
                    GetLeftVector(tForwardVector, from); // Forward is Left
                    GetUpVector(tUpVector, from);
                    GetForwardVector(tRightVector, from); // Right is original Forward
                    break;
                case Element.TOP:
                    GetUpVector(tForwardVector, from); // Forward is Up
                    GetRightVector(tUpVector, from); // Up is original Right
                    GetForwardVector(tRightVector, from); // Right is original Forward
                    break;
                case Element.BOTTOM:
                    GetBottomVector(tForwardVector, from); // Forward is Bottom
                    GetLeftVector(tUpVector, from); // Up is original Left
                    GetForwardVector(tRightVector, from); // Right is original Forward
                    break;
            }
        }

        public static Vector3 GetForwardVector(Vector3 forward, Matrix3x3 from)
        {
            from.getColumn(2, forward);
            return forward;
        }

        /// <summary>
        /// Gets the gl error.
        /// </summary>
        /// <returns>the gl error</returns>
        public static string GetGlError()
        {
            // Unity's GL class doesn't expose glGetError directly.
            // Error handling in Unity is typically done via Debug.LogError, exceptions, or specific API return values.
            Debug.LogWarning("GetGlError is a placeholder. Unity's GL class does not expose glGetError directly.");
            return null; // Always return null as we can't get GL errors directly
        }

        public static int[] GetIntBuffer1()
        {
            IBuff[0] = 0; // Clear the buffer
            return IBuff;
        }

        /// <summary>
        /// gets the left vector of this transform
        /// </summary>
        /// <param name="left">out</param>
        /// <param name="from"></param>
        /// <returns></returns>
        public static Vector3 GetLeftVector(Vector3 left, Transform from)
        {
            GetRightVector(left, from);
            left = -left;
            return left;
        }

        public static void GetRibbon(Transform axisAlignmentTransform, float dist, Vector3[] c)
        {
            float s = dist;

            c[0].Set(-s, -s, 0);
            c[1].Set(s, -s, 0);
            c[2].Set(s, s, 0);
            c[3].Set(-s, s, 0);

            // Transform points by the JBullet Transform
            // Assuming Transform.transform applies the transformation to the Vector3
            c[0] = axisAlignmentTransform.TransformPoint(c[0]);
            c[1] = axisAlignmentTransform.TransformPoint(c[1]);
            c[2] = axisAlignmentTransform.TransformPoint(c[2]);
            c[3] = axisAlignmentTransform.TransformPoint(c[3]);
        }

        public static Vector3 GetRightVector(Vector3 right)
        {
            // Original Java: modelBuffer.rewind(); Matrix4fTools.store(Controller.modelviewMatrix, modelBuffer);
            // right.x = modelBuffer.get(0); right.y = modelBuffer.get(4); right.z = modelBuffer.get(8);
            // This gets the X-axis (right) from the modelview matrix.
            right = Controller.modelviewMatrix.GetColumn(0); // X-axis is column 0
            return right;
        }

        /// <summary>
        /// right is column 0
        /// </summary>
        /// <param name="right"></param>
        /// <param name="from"></param>
        /// <returns></returns>
        public static Vector3 GetRightVector(Vector3 right, Transform from)
        {
            // from.basis.getColumn(0, right); // Original Java
            right = from.basis.GetColumn(0);
            return right;
        }

        public static Vector3 GetRightVector(Vector3 right, Matrix3x3 from)
        {
            from.getColumn(0, right);
            return right;
        }

        public static Vector3 GetUpVector(Vector3 up)
        {
            // Original Java: modelBuffer.rewind(); Matrix4fTools.store(Controller.modelviewMatrix, modelBuffer);
            // up.x = modelBuffer.get(1); up.y = modelBuffer.get(5); up.z = modelBuffer.get(9);
            // This gets the Y-axis (up) from the modelview matrix.
            up = Controller.modelviewMatrix.GetColumn(1); // Y-axis is column 1
            return up;
        }

        /// <summary>
        /// gets the up vector of this transform
        /// </summary>
        /// <param name="up">out</param>
        /// <param name="from"></param>
        /// <returns></returns>
        public static Vector3 GetUpVector(Vector3 up, Transform from)
        {
            // from.basis.getColumn(1, up); // Original Java
            up = from.basis.GetColumn(1);
            return up;
        }

        public static Vector3 GetUpVector(Vector3 up, Matrix3x3 from)
        {
            from.getColumn(1, up);
            return up;
        }

        public static void GlColor4f(Vector4 color)
        {
            GlColor4f(color.x, color.y, color.z, color.w);
        }

        public static void GlColor4f(Color color)
        {
            GlColor4f(color.r, color.g, color.b, color.a);
        }

        public static void GlColor4f(float r, float g, float b, float a)
        {
            if (CurrentColor.x != r ||
                CurrentColor.y != g ||
                CurrentColor.z != b ||
                CurrentColor.w != a
            )
            {
                CurrentColor.Set(r, g, b, a);
                GL.Color(new Color(r, g, b, a));
            }
        }

        public static void GlColor4fForced(float r, float g, float b, float a)
        {
            CurrentColor.Set(r, g, b, a);
            GL.Color(new Color(r, g, b, a));
        }

        public static void GlLoadIdentity()
        {
            if (matrixMode == (int)GL.PROJECTION)
            {
                Controller.projectionMatrix = Matrix4x4.identity;
            }
            else
            {
                Controller.modelviewMatrix = Matrix4x4.identity;
            }
            GL.LoadIdentity();
        }

        public static void GlLoadMatrix(float[] buff)
        {
            // Assuming buff is a float[16] representing a column-major OpenGL matrix.
            Matrix4x4 m = new Matrix4x4();
            for (int i = 0; i < 16; i++)
            {
                m[i] = buff[i];
            }

            if (matrixMode == (int)GL.PROJECTION)
            {
                Controller.projectionMatrix = m;
            }
            else
            {
                Controller.modelviewMatrix = m;
            }
            GL.LoadMatrix(m);
        }

        public static void GlLoadMatrix(Matrix4x4 mat)
        {
            if (matrixMode == (int)GL.PROJECTION)
            {
                Controller.projectionMatrix = mat;
            }
            else
            {
                Controller.modelviewMatrix = mat;
            }
            GL.LoadMatrix(mat);
        }

        public static void GlLoadMatrix(Transform t)
        {
            // Assuming t is a JBullet Transform.
            // Convert JBullet Transform to Unity Matrix4x4.
            Matrix4x4 m = Matrix4x4.TRS(t.origin, t.basis.rotation, t.basis.lossyScale); // Simplified

            if (matrixMode == (int)GL.PROJECTION)
            {
                Controller.projectionMatrix = m;
            }
            else
            {
                Controller.modelviewMatrix = m;
            }
            GL.LoadMatrix(m);
        }

        public static void GlMatrixMode(int mode)
        {
            // GL.MatrixMode takes a GL.MatrixMode enum, which has int values.
            // Assuming the original 'mode' values map correctly (e.g., GL11.GL_PROJECTION -> (int)GL.PROJECTION).
            GL.MatrixMode((UnityEngine.GL.MatrixMode)mode);
            matrixMode = mode;
        }

        public static void GlMultMatrix(float[] ff)
        {
            // Assuming ff is a float[16] representing a column-major OpenGL matrix.
            Matrix4x4 m = new Matrix4x4();
            for (int i = 0; i < 16; i++)
            {
                m[i] = ff[i];
            }

            if (matrixMode == (int)GL.PROJECTION)
            {
                Controller.projectionMatrix *= m;
            }
            else
            {
                Controller.modelviewMatrix *= m;
            }
            GL.MultMatrix(m);
        }

        public static void GlMultMatrix(Matrix4x4 f)
        {
            if (matrixMode == (int)GL.PROJECTION)
            {
                Controller.projectionMatrix *= f;
            }
            else
            {
                Controller.modelviewMatrix *= f;
            }
            GL.MultMatrix(f);
        }

        public static void GlMultMatrix(Transform t)
        {
            // Assuming t is a JBullet Transform.
            // Convert JBullet Transform to Unity Matrix4x4.
            Matrix4x4 m = Matrix4x4.TRS(t.origin, t.basis.rotation, t.basis.lossyScale); // Simplified

            if (matrixMode == (int)GL.PROJECTION)
            {
                Controller.projectionMatrix *= m;
            }
            else
            {
                Controller.modelviewMatrix *= m;
            }
            GL.MultMatrix(m);
        }

        /// <summary>
        /// Generates and returns parallel projection matrix.
        /// </summary>
        /// <param name="left">- left surface position,</param>
        /// <param name="right">- right surface position,</param>
        /// <param name="bottom">- bottom surface position,</param>
        /// <param name="top">- top surface position,</param>
        /// <param name="near">- distance to near clipping plane,</param>
        /// <param name="far">- distance to far clipping plane.</param>
        /// <returns>4x4 parallel projection matrix.</returns>
        public static void GlOrtho(float left, float right, float bottom,
                                   float top, float near, float far)
        {
            Matrix4x4 m = CreateOrthogonalProjectionMatrix(left, right, top, bottom, near, far);

            if (matrixMode == (int)GL.PROJECTION)
            {
                Controller.projectionMatrix = m;
            }
            else if (matrixMode == (int)GL.MODELVIEW)
            {
                Controller.modelviewMatrix = m;
            }
            GL.LoadMatrix(m); // Load the new matrix
        }

        public static void GlOrtho(float left, float right, float bottom,
                                   float top, float near, float far, float[] floatArrayBuffer)
        {
            // This method populates a float array with the orthogonal projection matrix.
            // The actual GL operation is done by the other GlOrtho overload.
            Matrix4x4 m = CreateOrthogonalProjectionMatrix(left, right, top, bottom, near, far);
            for (int i = 0; i < 16; i++)
            {
                floatArrayBuffer[i] = m[i];
            }
        }

        public static void GlPopMatrix()
        {
            if (matrixMode == (int)GL.PROJECTION)
            {
                GlPopProjectionMatrix();
            }
            else
            {
                GlPopModelviewMatrix();
            }
        }

        private static void GlPopModelviewMatrix()
        {
            Debug.Assert(ModelStackPointer > 0, "ModelStack underflow!");
            ModelStackPointer--;
            Controller.modelviewMatrix = ModelStack[ModelStackPointer];
            GL.PopMatrix();
        }

        private static void GlPopProjectionMatrix()
        {
            Debug.Assert(ProjectStackPointer > 0, "ProjectStack underflow!");
            ProjectStackPointer--;
            Controller.projectionMatrix = ProjectStack[ProjectStackPointer];
            GL.PopMatrix();
        }

        public static void GlPushMatrix()
        {
            if (matrixMode == (int)GL.PROJECTION)
            {
                GlPushProjectionMatrix();
            }
            else
            {
                GlPushModelviewMatrix();
            }
        }

        private static void GlPushModelviewMatrix()
        {
            Debug.Assert(ModelStackPointer < ModelStack.Length, $"ModelStack overflow! Max size: {ModelStack.Length}");
            Debug.Assert(ModelStackPointer >= 0, "ModelStack pointer negative!");
            ModelStack[ModelStackPointer] = Controller.modelviewMatrix;
            ModelStackPointer++;
            GL.PushMatrix();
        }

        private static void GlPushProjectionMatrix()
        {
            Debug.Assert(ProjectStackPointer < ProjectStack.Length, $"ProjectStack overflow! Max size: {ProjectStack.Length}");
            ProjectStack[ProjectStackPointer] = Controller.projectionMatrix;
            ProjectStackPointer++;
            GL.PushMatrix();
        }

        public static void GluOrtho2D(float left, float right, float bottom, float top)
        {
            GlOrtho(left, right, bottom, top, -1.0f, 1.0f);
        }

        public static void GluPerspective(float fov, float aspect,
                                          float znear, float zfar)
        {
            Matrix4x4 m = CreatePerspectiveProjectionMatrix(fov, aspect, znear, zfar);
            Controller.projectionMatrix = m;
            GL.LoadMatrix(m);
        }

        public static Matrix4x4 GluPerspective(Matrix4x4 @out, float fovx, float aspect, float zNear, float zFar, bool load)
        {
            // This code is based off the MESA source for gluPerspective
            // *NOTE* This assumes GL_PROJECTION is the current matrix

            @out = Matrix4x4.identity; // Clear out the matrix

            float xmin, xmax, ymin, ymax;

            xmax = zNear * Mathf.Tan(fovx / 360.0f * FastMath.PI);
            xmin = -xmax;

            ymin = xmin / aspect;
            ymax = xmax / aspect;

            // Set up the projection matrix
            @out[0, 0] = (2.0f * zNear) / (xmax - xmin);
            @out[1, 1] = (2.0f * zNear) / (ymax - ymin);
            @out[2, 2] = -(zFar + zNear) / (zFar - zNear);

            @out[0, 2] = (xmax + xmin) / (xmax - xmin); // Original Java m.m20
            @out[1, 2] = (ymax + ymin) / (ymax - ymin); // Original Java m.m21
            @out[3, 2] = -1.0f; // Original Java m.m23

            @out[2, 3] = -(2.0f * zFar * zNear) / (zFar - zFar); // Original Java m.m32

            if (load)
            {
                // Add to current matrix
                GlLoadMatrix(@out);
            }

            return @out;
        }

        public static int GluProject(float objx, float objy, float objz,
                                     float[] modelview, float[] projection, int[] viewport,
                                     float[] windowCoordinate)
        {
            // Convert float[] to Matrix4x4 for easier Unity math operations
            Matrix4x4 modelMatrix = new Matrix4x4();
            for (int i = 0; i < 16; i++) modelMatrix[i] = modelview[i];
            Matrix4x4 projMatrix = new Matrix4x4();
            for (int i = 0; i < 16; i++) projMatrix[i] = projection[i];

            Vector3 winPos;
            bool success = GLU.GluProject(objx, objy, objz, modelMatrix, projMatrix, viewport, out winPos);

            if (success)
            {
                windowCoordinate[0] = winPos.x;
                windowCoordinate[1] = winPos.y;
                windowCoordinate[2] = winPos.z;
                return 1;
            }
            return 0;
        }

        public static int GluProject(float objx, float objy, float objz,
                                     Matrix4x4 modelview, Matrix4x4 projection, int[] viewport,
                                     float[] windowCoordinate)
        {
            Vector3 winPos;
            bool success = GLU.GluProject(objx, objy, objz, modelview, projection, viewport, out winPos);

            if (success)
            {
                windowCoordinate[0] = winPos.x;
                windowCoordinate[1] = winPos.y;
                windowCoordinate[2] = winPos.z;
                return 1;
            }
            return 0;
        }

        private static void HandleShaderUniformError(Shader shader, string param)
        {
            if (EngineSettings.D_INFO_SHADER_ERRORS.isOn())
            {
                string s = $"[ERROR][SHADER] {shader.getVertexshaderPath()} - {param} HANDLE -1 ";
                if (!AbstractScene.infoList.Contains(s))
                {
                    AbstractScene.infoList.Add(s);
                }
            }
        }

        public static bool IsInViewFrustum(Transform t, Mesh m, float margin)
        {
            MinBB.Set(m.BoundingBox.min.x, m.BoundingBox.min.y, m.BoundingBox.min.z);
            MaxBB.Set(m.BoundingBox.max.x, m.BoundingBox.max.y, m.BoundingBox.max.z);
            AabbUtil2.transformAabb(MinBB, MaxBB, margin, t, MinBBOut, MaxBBOut);
            return Controller.getCamera().IsAABBInFrustum(MinBBOut, MaxBBOut); // Assuming IsAABBInFrustum exists on Camera
        }

        public static bool IsLineInView(Vector3 start, Vector3 end, float visLen)
        {
            return IsPointInCamRange(start, visLen) && IsPointInCamRange(end, visLen) && IsLineInViewFrustum(start, end);
        }

        public static bool IsLineInViewFrustum(Vector3 start, Vector3 end)
        {
            MaxBB.x = Mathf.Min(start.x, end.x);
            MaxBB.y = Mathf.Min(start.y, end.y);
            MaxBB.z = Mathf.Min(start.z, end.z);

            MinBB.x = Mathf.Max(start.x, end.x);
            MinBB.y = Mathf.Max(start.y, end.y);
            MinBB.z = Mathf.Max(start.z, end.z);
            bool aabbInFrustum = Controller.getCamera().IsAABBInFrustum(MinBB, MaxBB); // Assuming IsAABBInFrustum exists on Camera
            return aabbInFrustum;
        }

        public static bool IsPointInCamRange(Vector3 start, float visLen)
        {
            CamPosTest = start - Controller.getCamera().transform.position; // Assuming Controller.getCamera().getPos() is Camera.main.transform.position
            return CamPosTest.magnitude <= visLen;
        }

        public static bool IsPointInView(Vector3 start, float visLen)
        {
            return IsPointInCamRange(start, visLen) && Controller.getCamera().IsPointInFrustrum(start); // Assuming IsPointInFrustrum exists on Camera
        }

        public static Transform LookAtWithoutLoad(float eyex, float eyey, float eyez, float centerx, float centery, float centerz, float upx, float upy, float upz)
        {
            // view direction
            ZFac.Set(eyex - centerx, eyey - centery, eyez - centerz);

            // default y to up
            YFac.Set(upx, upy, upz);

            // force x, y and z to be all perpendicular to each other
            ZFac.Normalize();

            XFac = Vector3.Cross(YFac, ZFac);
            YFac = Vector3.Cross(ZFac, XFac);

            // normalise
            XFac.Normalize();
            YFac.Normalize();

            LookT.basis.SetColumn(0, XFac); // Assuming basis is a Matrix3x3 or similar
            LookT.basis.SetColumn(1, YFac);
            LookT.basis.SetColumn(2, ZFac);
            LookT.origin.Set(-eyex, -eyey, -eyez);
            LookT.basis.TransformPoint(LookT.origin); // Assuming TransformPoint exists on Matrix3x3

            return LookT;
        }

        public static Transform LookAt(float eyex, float eyey, float eyez, float centerx, float centery, float centerz, float upx, float upy, float upz)
        {
            // This method also loads the matrix onto the GL stack.
            Transform result = LookAtWithoutLoad(eyex, eyey, eyez, centerx, centery, centerz, upx, upy, upz);
            GlLoadMatrix(result); // Load the JBullet Transform's matrix onto GL stack
            return result;
        }

        public static Vector3 MatrixToEuler(Matrix3x3 m, Vector3 @out)
        {
            // This converts a 3x3 rotation matrix to Euler angles.
            // Unity's Quaternion.LookRotation or Quaternion.Euler can be used for conversion.
            // Re-implementing the original math for direct porting.

            float y = FastMath.atan2(-m.m20, FastMath.sqrt(m.m00 * m.m00 + m.m10 * m.m10));
            float cosy = FastMath.cos(y);

            if (cosy != 0)
            {
                float x = FastMath.atan2(m.m21 / cosy, m.m22 / cosy);
                float z = FastMath.atan2(m.m10 / cosy, m.m00 / cosy);
                @out.Set(x, y, z);
                return @out;
            }

            float z = 0;
            float x = FastMath.atan2(m.m01, m.m11);

            if (y < 0)
            {
                x = -x;
            }

            @out.Set(x, y, z);
            return @out;
        }

        /// <summary>
        /// Prints the gl error.
        /// </summary>
        public static void PrintGlError()
        {
            string r = GetGlError();
            if (r != null)
            {
                Debug.LogError($"JUST STACKTRACE (no exception thrown further): GL_ERROR: {r}");
                // No direct stack trace print here, Debug.LogError provides it.
            }
        }

        /// <summary>
        /// Prints the gl error critical.
        /// </summary>
        public static void PrintGlErrorCritical()
        {
            string r = GetGlError();
            if (r != null)
            {
                GLException ex = new GLException(r);
                Debug.LogError($"GL_ERROR: {r}");
                Debug.LogException(ex);
                GLFrame.ProcessErrorDialogException(ex, null); // Call the converted GLFrame method
            }
        }

        public static void PrintGlErrorCritical(string msg)
        {
            string r = GetGlError();
            if (r != null)
            {
                GLException ex = new GLException($"{r}: {msg}");
                Debug.LogError($"GL_ERROR: {r}");
                Debug.LogException(ex);
                GLFrame.ProcessErrorDialogException(ex, null); // Call the converted GLFrame method
            }
        }

        public static void Project(Vector3 planeNormal, Vector3 toProject, Vector3 projected)
        {
            // This projects a vector onto a plane passing through the origin.
            Vector3 N = planeNormal.normalized;
            projected = toProject - Vector3.Dot(toProject, N) * N;
        }

        /// <summary>
        /// </summary>
        /// <param name="vProj">- projection of v to the plane through the origin</param>
        /// <param name="v">- given vector</param>
        /// <param name="normal">- unit normal of the plane</param>
        /// <returns>vProj</returns>
        public static Vector3 ProjectOntoPlane(Vector3 vProj, Vector3 v,
                                               Vector3 normal)
        {
            float d = Vector3.Dot(v, normal);

            vProj.Set(v.x, v.y, v.z);
            vProj.x -= d * normal.x;
            vProj.y -= d * normal.y;
            vProj.z -= d * normal.z;
            return vProj;
        }

        public static void PutOrthogonalBeam(float[] buffer,
                                             Vector3 rightPlusUp,
                                             Vector3 rightMinusUp, Vector4 pos, Vector3 endPos)
        {
            // This method populates a float buffer with vertex data for an orthogonal beam.
            // In Unity, this would typically involve creating a Mesh or using GL.QUADS/GL.TRIANGLES.
            // Assuming 'buffer' is a float array to be filled.

            PosTmp.Set(pos.x, pos.y, pos.z, pos.w);
            PosTmp2.Set(endPos.x, endPos.y, endPos.z, pos.w); // w component from pos

            ATemp.Set(PosTmp.x, PosTmp.y, PosTmp.z);
            ATemp -= rightPlusUp;

            BTemp.Set(PosTmp.x, PosTmp.y, PosTmp.z);
            BTemp += rightMinusUp;

            CTemp.Set(PosTmp.x, PosTmp.y, PosTmp.z);
            CTemp += rightPlusUp;

            DTemp.Set(PosTmp.x, PosTmp.y, PosTmp.z);
            DTemp -= rightMinusUp;

            A4Temp.Set(ATemp.x, ATemp.y, ATemp.z, PosTmp.w);
            B4Temp.Set(BTemp.x, BTemp.y, BTemp.z, PosTmp.w);
            C4Temp.Set(CTemp.x, CTemp.y, CTemp.z, PosTmp.w);
            D4Temp.Set(DTemp.x, DTemp.y, DTemp.z, PosTmp.w);

            // Put first quad points
            PutPoint4(buffer, 0, A4Temp);
            PutPoint4(buffer, 4, B4Temp);
            PutPoint4(buffer, 8, C4Temp);
            PutPoint4(buffer, 12, D4Temp);

            ETemp.Set(PosTmp2.x, PosTmp2.y, PosTmp2.z);
            ETemp -= rightPlusUp;

            FTemp.Set(PosTmp2.x, PosTmp2.y, PosTmp2.z);
            FTemp += rightMinusUp;

            GTemp.Set(PosTmp2.x, PosTmp2.y, PosTmp2.z);
            GTemp += rightPlusUp;

            HTemp.Set(PosTmp2.x, PosTmp2.y, PosTmp2.z);
            HTemp -= rightMinusUp;

            A4Temp.Set(ETemp.x, ETemp.y, ETemp.z, PosTmp.w);
            B4Temp.Set(FTemp.x, FTemp.y, FTemp.z, PosTmp.w);
            C4Temp.Set(GTemp.x, GTemp.y, GTemp.z, PosTmp.w);
            D4Temp.Set(HTemp.x, HTemp.y, HTemp.z, PosTmp.w);

            // Put second quad points
            PutPoint4(buffer, 16, A4Temp);
            PutPoint4(buffer, 20, B4Temp);
            PutPoint4(buffer, 24, C4Temp);
            PutPoint4(buffer, 28, D4Temp);

            // Middle part (connecting the two quads)
            A4Temp.Set(BTemp.x, BTemp.y, BTemp.z, PosTmp.w);
            B4Temp.Set(CTemp.x, CTemp.y, CTemp.z, PosTmp.w);
            C4Temp.Set(HTemp.x, HTemp.y, HTemp.z, PosTmp.w);
            D4Temp.Set(ETemp.x, ETemp.y, ETemp.z, PosTmp.w);

            PutPoint4(buffer, 32, A4Temp);
            PutPoint4(buffer, 36, B4Temp);
            PutPoint4(buffer, 40, C4Temp);
            PutPoint4(buffer, 44, D4Temp);
        }

        public static void PutOrthogonalLineCross(float[] buffer,
                                                  Vector3 right, Vector3 up, Vector4 p)
        {
            // VERTICAL
            ATemp.Set(p.x, p.y, p.z);
            ATemp -= up;

            DTemp.Set(p.x, p.y, p.z);
            DTemp += up;

            // HORIZONTAL
            BTemp.Set(p.x, p.y, p.z);
            BTemp += right;

            CTemp.Set(p.x, p.y, p.z);
            CTemp -= right;

            A4Temp.Set(ATemp.x, ATemp.y, ATemp.z, p.w);
            D4Temp.Set(DTemp.x, DTemp.y, DTemp.z, p.w);

            B4Temp.Set(BTemp.x, BTemp.y, BTemp.z, p.w);
            C4Temp.Set(CTemp.x, CTemp.y, CTemp.z, p.w);

            // Interlance points
            PutPoint4(buffer, 0, A4Temp);
            PutPoint4(buffer, 4, B4Temp);
            PutPoint4(buffer, 8, D4Temp);
            PutPoint4(buffer, 12, C4Temp);
        }

        public static void PutOrthogonalLineHorizontal(float[] buffer, Vector3 rightPlusUp,
                                                       Vector3 rightMinusUp, Vector4 p)
        {
            BTemp.Set(p.x, p.y, p.z);
            BTemp += rightMinusUp;

            CTemp.Set(p.x, p.y, p.z);
            CTemp += rightPlusUp;

            B4Temp.Set(BTemp.x, BTemp.y, BTemp.z, p.w);
            C4Temp.Set(CTemp.x, CTemp.y, CTemp.z, p.w);

            PutPoint4(buffer, 0, B4Temp);
            PutPoint4(buffer, 4, C4Temp);
        }

        public static void PutOrthogonalLineVertical(float[] buffer, Vector3 rightPlusUp,
                                                     Vector3 rightMinusUp, Vector4 p)
        {
            ATemp.Set(p.x, p.y, p.z);
            ATemp -= rightPlusUp;

            DTemp.Set(p.x, p.y, p.z);
            DTemp -= rightMinusUp;

            A4Temp.Set(ATemp.x, ATemp.y, ATemp.z, p.w);
            D4Temp.Set(DTemp.x, DTemp.y, DTemp.z, p.w);

            PutPoint4(buffer, 0, A4Temp);
            PutPoint4(buffer, 4, D4Temp);
        }

        public static void PutOrthogonalQuad(float[] buffer, Vector3 rightPlusUp,
                                             Vector3 rightMinusUp, Vector3 p)
        {
            ATemp.Set(p.x, p.y, p.z);
            ATemp -= rightPlusUp;

            BTemp.Set(p.x, p.y, p.z);
            BTemp += rightMinusUp;

            CTemp.Set(p.x, p.y, p.z);
            CTemp += rightPlusUp;

            DTemp.Set(p.x, p.y, p.z);
            DTemp -= rightMinusUp;

            PutPoint3(buffer, 0, ATemp);
            PutPoint3(buffer, 3, BTemp);
            PutPoint3(buffer, 6, CTemp);
            PutPoint3(buffer, 9, DTemp);
        }

        public static void PutBillboardQuad(float[] buffer, Vector3 up,
                                            Vector3 right, float size, Vector4 p)
        {
            ATemp.Set(p.x, p.y, p.z);
            Vector3fTools.addScaled(ATemp, -size * 0.5f, up);
            Vector3fTools.addScaled(ATemp, -size * 0.5f, right);

            BTemp.Set(p.x, p.y, p.z);
            Vector3fTools.addScaled(BTemp, -size * 0.5f, up);
            Vector3fTools.addScaled(BTemp, size * 0.5f, right);

            CTemp.Set(p.x, p.y, p.z);
            Vector3fTools.addScaled(CTemp, size * 0.5f, up);
            Vector3fTools.addScaled(CTemp, size * 0.5f, right);

            DTemp.Set(p.x, p.y, p.z);
            Vector3fTools.addScaled(DTemp, size * 0.5f, up);
            Vector3fTools.addScaled(DTemp, -size * 0.5f, right);

            A4Temp.Set(ATemp.x, ATemp.y, ATemp.z, p.w);
            B4Temp.Set(BTemp.x, BTemp.y, BTemp.z, p.w);
            C4Temp.Set(CTemp.x, CTemp.y, CTemp.z, p.w);
            D4Temp.Set(DTemp.x, DTemp.y, DTemp.z, p.w);

            PutPoint4(buffer, 0, A4Temp);
            PutPoint4(buffer, 4, B4Temp);
            PutPoint4(buffer, 8, C4Temp);
            PutPoint4(buffer, 12, D4Temp);
        }

        public static void PutOrthogonalQuad(float[] buffer, Vector3 rightPlusUp,
                                             Vector3 rightMinusUp, Vector4 p)
        {
            ATemp.Set(p.x, p.y, p.z);
            ATemp -= rightPlusUp;

            BTemp.Set(p.x, p.y, p.z);
            BTemp += rightMinusUp;

            CTemp.Set(p.x, p.y, p.z);
            CTemp += rightPlusUp;

            DTemp.Set(p.x, p.y, p.z);
            DTemp -= rightMinusUp;

            A4Temp.Set(ATemp.x, ATemp.y, ATemp.z, p.w);
            B4Temp.Set(BTemp.x, BTemp.y, BTemp.z, p.w);
            C4Temp.Set(CTemp.x, CTemp.y, CTemp.z, p.w);
            D4Temp.Set(DTemp.x, DTemp.y, DTemp.z, p.w);

            PutPoint4(buffer, 0, A4Temp);
            PutPoint4(buffer, 4, B4Temp);
            PutPoint4(buffer, 8, C4Temp);
            PutPoint4(buffer, 12, D4Temp);
        }

        public static void PutPoint3(float[] buffer, float x, float y, float z)
        {
            buffer[0] = x;
            buffer[1] = y;
            buffer[2] = z;
        }

        public static void PutPoint3(float[] buffer, int index, Vector3 p)
        {
            buffer[index] = p.x;
            buffer[index + 1] = p.y;
            buffer[index + 2] = p.z;
        }

        public static void PutPoint3(float[] buffer, Vector3 p)
        {
            buffer[0] = p.x;
            buffer[1] = p.y;
            buffer[2] = p.z;
        }

        public static void PutPoint4(float[] buffer, Vector4 p)
        {
            buffer[0] = p.x;
            buffer[1] = p.y;
            buffer[2] = p.z;
            buffer[3] = p.w;
        }

        public static void PutPoint4(float[] buffer, Vector3 p, float w)
        {
            buffer[0] = p.x;
            buffer[1] = p.y;
            buffer[2] = p.z;
            buffer[3] = w;
        }

        public static void PutPoint4(float[] buffer, float x, float y, float z, float w)
        {
            buffer[0] = x;
            buffer[1] = y;
            buffer[2] = z;
            buffer[3] = w;
        }

        public static void PutPoint4(float[] buffer, int index, float x, float y, float z, float w)
        {
            buffer[index] = x;
            buffer[index + 1] = y;
            buffer[index + 2] = z;
            buffer[index + 3] = w;
        }

        public static void PutPoint4(float[] buffer, int index, Vector4 p)
        {
            buffer[index] = p.x;
            buffer[index + 1] = p.y;
            buffer[index + 2] = p.z;
            buffer[index + 3] = p.w;
        }

        public static void PutRibbon(float[] buffer,
                                     Vector4 p, Transform axisAlignmentTransform, float dist, Vector3[] c)
        {
            float s = dist;

            c[0].Set(-s, -s, 0);
            c[1].Set(s, -s, 0);
            c[2].Set(s, s, 0);
            c[3].Set(-s, s, 0);

            c[0] = axisAlignmentTransform.TransformPoint(c[0]);
            c[1] = axisAlignmentTransform.TransformPoint(c[1]);
            c[2] = axisAlignmentTransform.TransformPoint(c[2]);
            c[3] = axisAlignmentTransform.TransformPoint(c[3]);

            PutPoint4(buffer, 0, c[0].x, c[0].y, c[0].z, p.w);
            PutPoint4(buffer, 4, c[1].x, c[1].y, c[1].z, p.w);
            PutPoint4(buffer, 8, c[2].x, c[2].y, c[2].z, p.w);
            PutPoint4(buffer, 12, c[3].x, c[3].y, c[3].z, p.w);
        }

        public static void PutRibbon2(float[] buffer, float w, Vector3[] c, int i)
        {
            if (i < 0)
            {
                PutPoint4(buffer, 0, c[3].x, c[3].y, c[3].z, w);
                PutPoint4(buffer, 4, c[3].x, c[3].y, c[3].z, w);
            }
            else
            {
                int vPos = i;
                PutPoint4(buffer, vPos, c[2].x, c[2].y, c[2].z, w);
                PutPoint4(buffer, vPos + 4, c[3].x, c[3].y, c[3].z, w);
            }
        }

        public static void PutRibbon4(float[] buffer, float w, Vector3[] c)
        {
            A4Temp.Set(c[0].x, c[0].y, c[0].z, w);
            B4Temp.Set(c[1].x, c[1].y, c[1].z, w);
            C4Temp.Set(c[2].x, c[2].y, c[2].z, w);
            D4Temp.Set(c[3].x, c[3].y, c[3].z, w);

            PutPoint4(buffer, 0, A4Temp);
            PutPoint4(buffer, 4, B4Temp);
            PutPoint4(buffer, 8, C4Temp);
            PutPoint4(buffer, 12, D4Temp);
        }

        public static Matrix4x4 RetrieveModelviewMatrix(Matrix4x4 ret)
        {
            // GL.GetMatrix is the Unity equivalent of glGetFloatv for matrices.
            ret = GL.modelview;
            return ret;
        }

        public static Matrix4x4 RetrieveModelviewProjectionMatrix()
        {
            // In Unity, this is simply GL.GetGPUProjectionMatrix(GL.projection, false) * GL.modelview
            // Or Camera.main.projectionMatrix * Camera.main.worldToCameraMatrix
            // Re-implementing based on original Java logic.

            Matrix4x4 pmat = RetrieveProjectionMatrix(Matrix4x4.identity);
            Matrix4x4 mmat = RetrieveModelviewMatrix(Matrix4x4.identity);

            Matrix4x4 mvp = pmat * mmat; // Matrix multiplication

            // Store in mvpBuffer (now MvpMatrix)
            MvpMatrix = mvp;
            return mvp;
        }

        public static Matrix3x3 RetrieveNormalMatrix(Matrix4x4 modelView)
        {
            // Normal matrix is the inverse transpose of the modelview matrix.
            // For non-uniform scaling, it's the inverse transpose of the 3x3 part.
            // For uniform scaling, it's just the inverse of the 3x3 part.
            // Original Java used javax.vecmath.Matrix3f.invert().

            Matrix3x3 nMat = new Matrix3x3(
                modelView[0, 0], modelView[0, 1], modelView[0, 2],
                modelView[1, 0], modelView[1, 1], modelView[1, 2],
                modelView[2, 0], modelView[2, 1], modelView[2, 2]
            );
            nMat.invert(); // Invert the 3x3 matrix

            // Store in normalBuffer (now NormalMatrix)
            // This part is tricky as NormalMatrix is Matrix4x4.
            // We'll store the 3x3 part into the top-left of NormalMatrix.
            NormalMatrix[0, 0] = nMat.m00; NormalMatrix[0, 1] = nMat.m01; NormalMatrix[0, 2] = nMat.m02;
            NormalMatrix[1, 0] = nMat.m10; NormalMatrix[1, 1] = nMat.m11; NormalMatrix[1, 2] = nMat.m12;
            NormalMatrix[2, 0] = nMat.m20; NormalMatrix[2, 1] = nMat.m21; NormalMatrix[2, 2] = nMat.m22;

            return nMat;
        }

        public static Matrix4x4 RetrieveProjectionMatrix(Matrix4x4 ret)
        {
            // GL.GetMatrix is the Unity equivalent of glGetFloatv for matrices.
            ret = GL.projection;
            return ret;
        }

        public static void RotateModelview(float angle, float x, float y, float z)
        {
            // This applies a rotation to the current modelview matrix and GL stack.
            // In Unity, GL.Rotate applies a rotation to the current matrix.
            // We also need to update our internal Controller.modelviewMatrix.

            Vector3 axis = new Vector3(x, y, z);
            // Create a rotation matrix
            Quaternion rotation = Quaternion.AngleAxis(angle, axis);
            Matrix4x4 rotationMatrix = Matrix4x4.Rotate(rotation);

            Controller.modelviewMatrix *= rotationMatrix; // Apply rotation to internal matrix
            GL.Rotate(rotation); // Apply rotation to GL stack
        }

        public static void ScaleModelview(float x, float y, float z)
        {
            // This applies a scale to the current modelview matrix and GL stack.
            // In Unity, GL.Scale applies a scale to the current matrix.
            // We also need to update our internal Controller.modelviewMatrix.

            Vector3 scaleVec = new Vector3(x, y, z);
            Matrix4x4 scaleMatrix = Matrix4x4.Scale(scaleVec);

            Controller.modelviewMatrix *= scaleMatrix; // Apply scale to internal matrix
            GL.Scale(scaleVec); // Apply scale to GL stack
        }

        /// <summary>
        /// gets the bottom vector of this transform
        /// </summary>
        /// <param name="bottom">out</param>
        /// <param name="to"></param>
        public static void SetBottomVector(Vector3 bottom, Transform to)
        {
            bottom = -bottom;
            SetUpVector(bottom, to);
        }

        /// <summary>
        /// forward is column 2
        /// </summary>
        /// <param name="forward">out</param>
        /// <param name="to"></param>
        public static void SetForwardVector(Vector3 forward, Transform to)
        {
            to.basis.SetColumn(2, forward); // Assuming basis is a Matrix3x3 or similar
        }

        /// <summary>
        /// gets the left vector of this transform
        /// </summary>
        /// <param name="left">out</param>
        /// <param name="to"></param>
        public static void SetLeftVector(Vector3 left, Transform to)
        {
            left = -left;
            SetRightVector(left, to);
        }

        /// <summary>
        /// right is column 0
        /// </summary>
        /// <param name="right"></param>
        /// <param name="to"></param>
        public static void SetRightVector(Vector3 right, Transform to)
        {
            to.basis.SetColumn(0, right); // Assuming basis is a Matrix3x3 or similar
        }

        /// <summary>
        /// gets the up vector of this transform
        /// </summary>
        /// <param name="up">out</param>
        /// <param name="to"></param>
        public static void SetUpVector(Vector3 up, Transform to)
        {
            to.basis.SetColumn(1, up); // Assuming basis is a Matrix3x3 or similar
        }

        /// <summary>
        /// forward is column 2
        /// </summary>
        /// <param name="forward">out</param>
        /// <param name="to"></param>
        public static void SetForwardVector(Vector3 forward, Matrix3x3 to)
        {
            to.setColumn(2, forward);
        }
    }
}
