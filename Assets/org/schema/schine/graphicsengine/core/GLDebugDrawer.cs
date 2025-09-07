using System;
using UnityEngine;
using System.Collections.Generic;
using com.bulletphysics.collision.broadphase; // For List

// Placeholder for JBullet types
// You will need to replace these with actual Unity Physics or custom implementations
namespace com.bulletphysics.linearmath
{
    public class IDebugDraw
    {
        public virtual void drawLine(Vector3 from, Vector3 to, Vector3 color) { }
        public virtual void drawContactPoint(Vector3 pointOnB, Vector3 normalOnB, float distance, int lifeTime, Vector3 color) { }
        public virtual void reportErrorWarning(string warningString) { Debug.LogWarning(warningString); }
        public virtual void draw3dText(Vector3 location, string textString) { }
        public virtual int getDebugMode() { return 0; }
        public virtual void setDebugMode(int debugMode) { }
    }

    public class Transform
    {
        public Matrix4x4 basis = Matrix4x4.identity;
        public Vector3 origin = Vector3.zero;

        public void setIdentity()
        {
            basis = Matrix4x4.identity;
            origin = Vector3.zero;
        }

        public void getOpenGLMatrix(float[] m)
        {
            // Convert Unity Matrix4x4 to OpenGL float array
            // Unity's Matrix4x4 is column-major, OpenGL is column-major
            // So direct copy should work if the order is correct.
            // m[0] = basis.m00; m[1] = basis.m10; m[2] = basis.m20; m[3] = basis.m30;
            // ...
            // m[12] = origin.x; m[13] = origin.y; m[14] = origin.z; m[15] = 1.0f;
            for (int i = 0; i < 4; i++)
            {
                for (int j = 0; j < 4; j++)
                {
                    m[i * 4 + j] = basis[j, i]; // Transpose for column-major to row-major if needed, but Unity is column-major
                }
            }
            m[12] = origin.x;
            m[13] = origin.y;
            m[14] = origin.z;
            m[15] = 1.0f;
        }

        public void mul(Transform other)
        {
            // This is a simplified multiplication for debug purposes
            // For full physics, you'd need proper matrix multiplication
            basis *= other.basis;
            origin += other.origin; // Simplified
        }
    }

    public static class DebugDrawModes
    {
        public const int NO_DEBUG = 0;
        public const int DRAW_WIREFRAME = 1;
        public const int DRAW_AABB = 2;
        public const int DRAW_FEATURES_TEXT = 4;
        public const int DRAW_CONTACT_POINTS = 8;
        public const int NO_DEACTIVATION = 16;
        public const int NO_HELP_TEXT = 32;
        public const int DRAW_TEXT = 64;
        public const int PROFILE_TIMINGS = 128;
        public const int ENABLE_SAT_COMPARISON = 256;
        public const int DISABLE_BULLET_LCP = 512;
        public const int ENABLE_CCD = 1024;
        public const int MAX_DEBUG_DRAW_MODE = 2047;
    }

    public static class TransformUtil
    {
        public static void planeSpace1(Vector3 n, out Vector3 p, out Vector3 q)
        {
            if (Mathf.Abs(n.z) > Mathf.Abs(n.x))
            {
                if (Mathf.Abs(n.z) > Mathf.Abs(n.y))
                {
                    // n.z is largest component
                    p = new Vector3(1.0f, 1.0f, -(n.x + n.y) / n.z);
                }
                else
                {
                    // n.y is largest component
                    p = new Vector3(1.0f, -(n.x + n.z) / n.y, 1.0f);
                }
            }
            else
            {
                if (Mathf.Abs(n.x) > Mathf.Abs(n.y))
                {
                    // n.x is largest component
                    p = new Vector3(-(n.y + n.z) / n.x, 1.0f, 1.0f);
                }
                else
                {
                    // n.y is largest component
                    p = new Vector3(1.0f, -(n.x + n.z) / n.y, 1.0f);
                }
            }
            p.Normalize();
            q = Vector3.Cross(n, p);
        }
    }

    public static class VectorUtil
    {
        public static float getCoord(Vector3 vec, int axis)
        {
            switch (axis)
            {
                case 0: return vec.x;
                case 1: return vec.y;
                case 2: return vec.z;
                default: throw new ArgumentOutOfRangeException(nameof(axis));
            }
        }
    }
}

namespace com.bulletphysics.collision.broadphase
{
    public enum BroadphaseNativeType
    {
        BOX_SHAPE_PROXYTYPE,
        SPHERE_SHAPE_PROXYTYPE,
        STATIC_PLANE_PROXYTYPE,
        CYLINDER_SHAPE_PROXYTYPE,
        COMPOUND_SHAPE_PROXYTYPE,
        // Add other types as needed
        TERRAIN_SHAPE_PROXYTYPE,
    }
}

namespace com.bulletphysics.collision.shapes
{
    public class CollisionShape
    {
        public virtual BroadphaseNativeType getShapeType() { return BroadphaseNativeType.BOX_SHAPE_PROXYTYPE; } // Placeholder
        public virtual bool isConvex() { return false; }
        public virtual bool isPolyhedral() { return false; }
        public virtual bool isConcave() { return false; }
        public virtual object getUserPointer() { return null; }
        public virtual void setUserPointer(object obj) { }
        public virtual float getMargin() { return 0f; }
    }

    public class BoxShape : PolyhedralConvexShape
    {
        private Vector3 halfExtents;
        public BoxShape(Vector3 halfExtents) { this.halfExtents = halfExtents; }
        public override BroadphaseNativeType getShapeType() { return BroadphaseNativeType.BOX_SHAPE_PROXYTYPE; }
        public Vector3 getHalfExtentsWithMargin(Vector3 outVec) { outVec.Set(halfExtents.x, halfExtents.y, halfExtents.z); return outVec; }
    }

    public class SphereShape : PolyhedralConvexShape
    {
        private float radius;
        public SphereShape(float radius) { this.radius = radius; }
        public override BroadphaseNativeType getShapeType() { return BroadphaseNativeType.SPHERE_SHAPE_PROXYTYPE; }
        public override float getMargin() { return radius; }
    }

    public class StaticPlaneShape : CollisionShape
    {
        private Vector3 planeNormal;
        private float planeConstant;
        public StaticPlaneShape(Vector3 planeNormal, float planeConstant) { this.planeNormal = planeNormal; this.planeConstant = planeConstant; }
        public override BroadphaseNativeType getShapeType() { return BroadphaseNativeType.STATIC_PLANE_PROXYTYPE; }
        public Vector3 getPlaneNormal(Vector3 outVec) { outVec.Set(planeNormal.x, planeNormal.y, planeNormal.z); return outVec; }
        public float getPlaneConstant() { return planeConstant; }
    }

    public class CylinderShape : PolyhedralConvexShape
    {
        private Vector3 halfExtents;
        private int upAxis;
        public CylinderShape(Vector3 halfExtents, int upAxis) { this.halfExtents = halfExtents; this.upAxis = upAxis; }
        public override BroadphaseNativeType getShapeType() { return BroadphaseNativeType.CYLINDER_SHAPE_PROXYTYPE; }
        public int getUpAxis() { return upAxis; }
        public float getRadius() { return Mathf.Max(halfExtents.x, halfExtents.z); } // Simplified
        public Vector3 getHalfExtentsWithMargin(Vector3 outVec) { outVec.Set(halfExtents.x, halfExtents.y, halfExtents.z); return outVec; }
    }

    public class CompoundShape : CollisionShape
    {
        private List<(Transform, CollisionShape)> childShapes = new List<(Transform, CollisionShape)>();
        public override BroadphaseNativeType getShapeType() { return BroadphaseNativeType.COMPOUND_SHAPE_PROXYTYPE; }
        public int getNumChildShapes() { return childShapes.Count; }
        public void getChildTransform(int index, Transform outTrans) { outTrans.basis = childShapes[index].Item1.basis; outTrans.origin = childShapes[index].Item1.origin; }
        public CollisionShape getChildShape(int index) { return childShapes[index].Item2; }
    }

    public class ConvexShape : PolyhedralConvexShape { }
    public class PolyhedralConvexShape : ConvexShape
    {
        public override bool isPolyhedral() { return true; }
        public virtual int getNumEdges() { return 0; }
        public virtual void getEdge(int i, Vector3 a, Vector3 b) { }
    }
    public class ConcaveShape : CollisionShape
    {
        public override bool isConcave() { return true; }
        public virtual void processAllTriangles(TriangleCallback callback, Vector3 aabbMin, Vector3 aabbMax) { }
    }
    public class ShapeHull {
        public ShapeHull(ConvexShape shape) {}
        public void buildHull(float margin) {}
        public int numTriangles() { return 0; }
        public int numIndices() { return 0; }
        public int numVertices() { return 0; }
    }

    public class TriangleCallback
    {
        public virtual void processTriangle(Vector3[] triangle, int partId, int triangleIndex) { }
    }
}

namespace com.bulletphysics.dynamics.constraintsolver
{
    public enum TypedConstraintType
    {
        POINT2POINT_CONSTRAINT_TYPE,
        HINGE_CONSTRAINT_TYPE,
        CONETWIST_CONSTRAINT_TYPE,
        D6_CONSTRAINT_TYPE,
        SLIDER_CONSTRAINT_TYPE,
        VEHICLE_CONSTRAINT_TYPE,
        // Add others as needed
    }

    public class TypedConstraint
    {
        public virtual TypedConstraintType getConstraintType() { return TypedConstraintType.POINT2POINT_CONSTRAINT_TYPE; }
    }

    public class HingeConstraint : TypedConstraint
    {
        public override TypedConstraintType getConstraintType() { return TypedConstraintType.HINGE_CONSTRAINT_TYPE; }
        public Rigidbody getRigidBodyA() { return null; } // Placeholder
        public Rigidbody getRigidBodyB() { return null; } // Placeholder
        public void getAFrame(Transform outTrans) { outTrans.setIdentity(); } // Placeholder
        public void getBFrame(Transform outTrans) { outTrans.setIdentity(); } // Placeholder
    }

    public class SliderConstraint : TypedConstraint
    {
        public override TypedConstraintType getConstraintType() { return TypedConstraintType.SLIDER_CONSTRAINT_TYPE; }
        public Rigidbody getRigidBodyA() { return null; } // Placeholder
        public Rigidbody getRigidBodyB() { return null; } // Placeholder
        public void getFrameOffsetA(Transform outTrans) { outTrans.setIdentity(); } // Placeholder
        public void getFrameOffsetB(Transform outTrans) { outTrans.setIdentity(); } // Placeholder
    }
}

namespace com.bulletphysics.dynamics
{
    public class Rigidbody {
        public void getWorldTransform(Transform outTrans) { outTrans.setIdentity(); } // Placeholder
    }
}

// Placeholder for custom types
namespace Org.Schema.Schine.GraphicsEngine.Core.Settings
{
    public static class EngineSettings
    {
        public static class P_PHYSICS_DEBUG_MODE
        {
            public static int getInt() { return 0; } // Placeholder
        }
    }
}

namespace Org.Schema.Schine.GraphicsEngine.Forms
{
    public static class Cube
    {
        public static void wireCube(float size)
        {
            // Placeholder for drawing a wireframe cube
            // In Unity, you'd typically use Gizmos.DrawWireCube in OnDrawGizmos()
            // Or generate a mesh and draw it with GL.LINES
            Debug.Log($"Drawing wire cube of size {size}");
        }
    }
}


namespace Org.Schema.Schine.GraphicsEngine.Core
{
    /// <summary>
    /// The Class GLDebugDrawer.
    /// </summary>
    public class GLDebugDrawer : com.bulletphysics.linearmath.IDebugDraw
    {
        /// <summary>
        /// The Constant DEBUG_NORMALS.
        /// </summary>
        private static readonly bool DEBUG_NORMALS = false;

        /// <summary>
        /// The gl matTmp.
        /// </summary>
        private static float[] glMat = new float[16];

        // JAVA NOTE: added
        /// <summary>
        /// The tmp vec.
        /// </summary>
        private readonly Vector3 tmpVec = new Vector3();

        /// <summary>
        /// The debug mode.
        /// </summary>
        private int debugMode = com.bulletphysics.linearmath.DebugDrawModes.NO_DEBUG;

        /// <summary>
        /// Instantiates a new gL debug drawer.
        /// </summary>
        public GLDebugDrawer() { }

        /// <summary>
        /// Draw open GL.
        /// </summary>
        /// <param name="trans">The transform.</param>
        /// <param name="shape">The collision shape.</param>
        /// <param name="color">The color.</param>
        /// <param name="debugMode">The debug mode.</param>
        public static void DrawOpenGL(com.bulletphysics.linearmath.Transform trans, com.bulletphysics.collision.shapes.CollisionShape shape, Vector3 color, int debugMode)
        {
            // ObjectPools are removed for simplicity in Unity C#
            // You might consider using Unity's built-in physics debug visualization or Gizmos.

            GL.PushMatrix();
            // Convert JBullet Transform to Unity Matrix4x4
            Matrix4x4 unityMatrix = Matrix4x4.identity;
            if (trans != null)
            {
                // This conversion is simplified. A proper conversion would involve
                // converting the basis (rotation) and origin (position) to a Unity Matrix4x4.
                // For now, we'll use a placeholder.
                // trans.getOpenGLMatrix(glMat); // This populates glMat
                // unityMatrix.SetColumn(0, new Vector4(glMat[0], glMat[1], glMat[2], glMat[3]));
                // ...
                // For debug drawing, we can directly use the origin and basis if Transform is properly converted.
                unityMatrix.SetTRS(trans.origin, trans.basis.rotation, trans.basis.lossyScale); // Simplified
            }
            GL.MultMatrix(unityMatrix);

            GL.Enable(GL.COLOR_MATERIAL); // Placeholder for GL_COLOR_MATERIAL
            GL.Color(new Color(color.x, color.y, color.z, 1f));
            GL.Disable(GL.LIGHTING); // Placeholder for GL_LIGHTING
            GL.Disable(GL.TEXTURE_2D); // Placeholder for GL_TEXTURE_2D

            if (shape.getShapeType() == com.bulletphysics.collision.broadphase.BroadphaseNativeType.COMPOUND_SHAPE_PROXYTYPE)
            {
                com.bulletphysics.collision.shapes.CompoundShape compoundShape = (com.bulletphysics.collision.shapes.CompoundShape)shape;
                com.bulletphysics.linearmath.Transform childTrans = new com.bulletphysics.linearmath.Transform(); // No pooling
                for (int i = compoundShape.getNumChildShapes() - 1; i >= 0; i--)
                {
                    compoundShape.getChildTransform(i, childTrans);
                    com.bulletphysics.collision.shapes.CollisionShape colShape = compoundShape.getChildShape(i);
                    DrawOpenGL(childTrans, colShape, color, debugMode);
                }
            }
            else
            {
                bool useWireframeFallback = true;

                if ((debugMode & com.bulletphysics.linearmath.DebugDrawModes.DRAW_WIREFRAME) == 0)
                {
                    switch (shape.getShapeType())
                    {
                        case com.bulletphysics.collision.broadphase.BroadphaseNativeType.BOX_SHAPE_PROXYTYPE:
                            if (shape is com.bulletphysics.collision.shapes.BoxShape boxShape)
                            {
                                Vector3 halfExtent = Vector3.zero; // Placeholder
                                boxShape.getHalfExtentsWithMargin(halfExtent);
                                // In Unity, you'd typically use Gizmos.DrawWireCube or generate a mesh
                                // For now, a simple GL.LINES cube
                                GL.Begin(GL.LINES);
                                // Simplified cube drawing
                                GL.Vertex3(-halfExtent.x, -halfExtent.y, -halfExtent.z); GL.Vertex3(halfExtent.x, -halfExtent.y, -halfExtent.z);
                                GL.Vertex3(halfExtent.x, -halfExtent.y, -halfExtent.z); GL.Vertex3(halfExtent.x, halfExtent.y, -halfExtent.z);
                                GL.Vertex3(halfExtent.x, halfExtent.y, -halfExtent.z); GL.Vertex3(-halfExtent.x, halfExtent.y, -halfExtent.z);
                                GL.Vertex3(-halfExtent.x, halfExtent.y, -halfExtent.z); GL.Vertex3(-halfExtent.x, -halfExtent.y, -halfExtent.z);

                                GL.Vertex3(-halfExtent.x, -halfExtent.y, halfExtent.z); GL.Vertex3(halfExtent.x, -halfExtent.y, halfExtent.z);
                                GL.Vertex3(halfExtent.x, -halfExtent.y, halfExtent.z); GL.Vertex3(halfExtent.x, halfExtent.y, halfExtent.z);
                                GL.Vertex3(halfExtent.x, halfExtent.y, halfExtent.z); GL.Vertex3(-halfExtent.x, halfExtent.y, halfExtent.z);
                                GL.Vertex3(-halfExtent.x, halfExtent.y, halfExtent.z); GL.Vertex3(-halfExtent.x, -halfExtent.y, halfExtent.z);

                                GL.Vertex3(-halfExtent.x, -halfExtent.y, -halfExtent.z); GL.Vertex3(-halfExtent.x, -halfExtent.y, halfExtent.z);
                                GL.Vertex3(halfExtent.x, -halfExtent.y, -halfExtent.z); GL.Vertex3(halfExtent.x, -halfExtent.y, halfExtent.z);
                                GL.Vertex3(halfExtent.x, halfExtent.y, -halfExtent.z); GL.Vertex3(halfExtent.x, halfExtent.y, halfExtent.z);
                                GL.Vertex3(-halfExtent.x, halfExtent.y, -halfExtent.z); GL.Vertex3(-halfExtent.x, halfExtent.y, halfExtent.z);
                                GL.End();

                                useWireframeFallback = false;
                            }
                            break;
                        case com.bulletphysics.collision.broadphase.BroadphaseNativeType.SPHERE_SHAPE_PROXYTYPE:
                            com.bulletphysics.collision.shapes.SphereShape sphereShape = (com.bulletphysics.collision.shapes.SphereShape)shape;
                            float radius = sphereShape.getMargin();
                            // Unity doesn't have a direct GL.DrawSphere. Use Gizmos or custom mesh.
                            // For now, just a debug log.
                            Debug.LogError("Cannot draw debug sphere directly with GL. Use Gizmos.DrawWireSphere or custom mesh.");
                            useWireframeFallback = false;
                            break;
                        case com.bulletphysics.collision.broadphase.BroadphaseNativeType.STATIC_PLANE_PROXYTYPE:
                            com.bulletphysics.collision.shapes.StaticPlaneShape staticPlaneShape = (com.bulletphysics.collision.shapes.StaticPlaneShape)shape;
                            float planeConst = staticPlaneShape.getPlaneConstant();
                            Vector3 planeNormal = Vector3.zero;
                            staticPlaneShape.getPlaneNormal(planeNormal);
                            Vector3 planeOrigin = planeNormal * planeConst;
                            Vector3 vec0, vec1;
                            com.bulletphysics.linearmath.TransformUtil.planeSpace1(planeNormal, out vec0, out vec1);
                            float vecLen = 100f;
                            Vector3 pt0 = planeOrigin + vec0 * vecLen;
                            Vector3 pt1 = planeOrigin - vec0 * vecLen;
                            Vector3 pt2 = planeOrigin + vec1 * vecLen;
                            Vector3 pt3 = planeOrigin - vec1 * vecLen;

                            GL.Begin(GL.LINES);
                            GL.Vertex3(pt0.x, pt0.y, pt0.z);
                            GL.Vertex3(pt1.x, pt1.y, pt1.z);
                            GL.Vertex3(pt2.x, pt2.y, pt2.z);
                            GL.Vertex3(pt3.x, pt3.y, pt3.z);
                            GL.End();
                            break;
                        case com.bulletphysics.collision.broadphase.BroadphaseNativeType.CYLINDER_SHAPE_PROXYTYPE:
                            com.bulletphysics.collision.shapes.CylinderShape cylinder = (com.bulletphysics.collision.shapes.CylinderShape)shape;
                            int upAxis = cylinder.getUpAxis();
                            float cylinderRadius = cylinder.getRadius();
                            Vector3 halfVec = Vector3.zero;
                            float halfHeight = com.bulletphysics.linearmath.VectorUtil.getCoord(cylinder.getHalfExtentsWithMargin(halfVec), upAxis);

                            GL.PushMatrix();
                            // Apply rotation based on upAxis
                            switch (upAxis)
                            {
                                case 2: GL.Rotate(new Vector3(90, 0, 0)); break; // X Axis
                                case 3: GL.Rotate(new Vector3(0, 0, 90)); break; // Z Axis
                                default: GL.Translate(new Vector3(-halfHeight, 0, 0)); GL.Rotate(new Vector3(0, 90, 0)); break; // Y Axis
                            }

                            Debug.LogError("Cannot draw debug cylinder directly with GL. Use Gizmos.DrawWireCylinder or custom mesh.");
                            GL.Begin(GL.LINES);
                            GL.Vertex3(0, 0, 0);
                            GL.Vertex3(0, halfHeight * 2, 0);
                            GL.End();
                            GL.PopMatrix();
                            break;
                        default:
                            if (shape.isConvex())
                            {
                                Org.Schema.Schine.GraphicsEngine.Forms.Cube.wireCube(100f); // Placeholder
                                com.bulletphysics.collision.shapes.ConvexShape convexShape = (com.bulletphysics.collision.shapes.ConvexShape)shape;
                                if (shape.getUserPointer() == null)
                                {
                                    com.bulletphysics.collision.shapes.ShapeHull hull = new com.bulletphysics.collision.shapes.ShapeHull(convexShape);
                                    float margin = shape.getMargin();
                                    hull.buildHull(margin);
                                    convexShape.setUserPointer(hull);
                                }
                                // Drawing hull is commented out in original, so keep it commented.
                            }
                            break;
                    }
                }

                if (useWireframeFallback)
                {
                    if (shape.isPolyhedral() && (shape is com.bulletphysics.collision.shapes.PolyhedralConvexShape polyshape))
                    {
                        GL.Begin(GL.LINES);
                        Vector3 a = Vector3.zero, b = Vector3.zero;
                        for (int i = 0; i < polyshape.getNumEdges(); i++)
                        {
                            polyshape.getEdge(i, a, b);
                            GL.Vertex3(a.x, a.y, a.z);
                            GL.Vertex3(b.x, b.y, b.z);
                        }
                        GL.End();
                    }
                }

                if (shape.isConcave() && shape.getShapeType() != com.bulletphysics.collision.broadphase.BroadphaseNativeType.TERRAIN_SHAPE_PROXYTYPE)
                {
                    com.bulletphysics.collision.shapes.ConcaveShape concaveMesh = (com.bulletphysics.collision.shapes.ConcaveShape)shape;
                    Vector3 aabbMax = new Vector3(1e30f, 1e30f, 1e30f);
                    Vector3 aabbMin = new Vector3(-1e30f, -1e30f, -1e30f);

                    GlDrawcallback drawCallback = new GlDrawcallback();
                    drawCallback.wireframe = (debugMode & com.bulletphysics.linearmath.DebugDrawModes.DRAW_WIREFRAME) != 0;

                    concaveMesh.processAllTriangles(drawCallback, aabbMin, aabbMax);
                }

                if ((debugMode & com.bulletphysics.linearmath.DebugDrawModes.DRAW_TEXT) != 0)
                {
                    // TODO: Implement 3D text drawing in Unity (e.g., TextMeshPro or custom GUI)
                }

                if ((debugMode & com.bulletphysics.linearmath.DebugDrawModes.DRAW_FEATURES_TEXT) != 0)
                {
                    // TODO: Implement 3D text drawing in Unity
                }
            }
            GL.PopMatrix();
            GL.Enable(GL.LIGHTING); // Placeholder
        }

        /// <summary>
        /// Draw open GL.
        /// </summary>
        /// <param name="constraint">The typed constraint.</param>
        /// <param name="color">The color.</param>
        /// <param name="debugMode">The debug mode.</param>
        public static void DrawOpenGL(com.bulletphysics.dynamics.constraintsolver.TypedConstraint constraint, Vector3 color, int debugMode)
        {
            GL.PushMatrix();
            GL.Enable(GL.COLOR_MATERIAL); // Placeholder
            GL.Disable(GL.LIGHTING); // Placeholder
            GL.Color(new Color(color.x, color.y, color.z, 1f));
            GL.Disable(GL.TEXTURE_2D); // Placeholder

            com.bulletphysics.dynamics.constraintsolver.TypedConstraintType type = constraint.getConstraintType();

            //FIXME doesent work yet
            switch (type)
            {
                case com.bulletphysics.dynamics.constraintsolver.TypedConstraintType.CONETWIST_CONSTRAINT_TYPE:
                    Debug.LogWarning("[GLDebugDrawer] [WARNING]: conetwist constraint drawing type not implemented yet");
                    break;
                case com.bulletphysics.dynamics.constraintsolver.TypedConstraintType.D6_CONSTRAINT_TYPE:
                    Debug.LogWarning("[GLDebugDrawer] [WARNING]: D6 constraint drawing type not implemented yet");
                    break;
                case com.bulletphysics.dynamics.constraintsolver.TypedConstraintType.HINGE_CONSTRAINT_TYPE:
                    com.bulletphysics.dynamics.constraintsolver.HingeConstraint hc = (com.bulletphysics.dynamics.constraintsolver.HingeConstraint)constraint;
                    com.bulletphysics.linearmath.Transform th = new com.bulletphysics.linearmath.Transform();
                    com.bulletphysics.linearmath.Transform th2 = new com.bulletphysics.linearmath.Transform();
                    th2.setIdentity();
                    hc.getRigidBodyA().getWorldTransform(th);
                    hc.getAFrame(th2);
                    th.mul(th2);
                    DrawPoint(th, 25, color);

                    th.setIdentity();
                    th2.setIdentity();
                    hc.getRigidBodyB().getWorldTransform(th);
                    hc.getBFrame(th2);
                    th.mul(th2);
                    DrawPoint(th, 25, new Vector3(0, 1, 1));
                    break;
                case com.bulletphysics.dynamics.constraintsolver.TypedConstraintType.POINT2POINT_CONSTRAINT_TYPE:
                    Debug.LogWarning("[GLDebugDrawer] [WARNING]: point 2 point constraint drawing type not implemented yet");
                    break;
                case com.bulletphysics.dynamics.constraintsolver.TypedConstraintType.SLIDER_CONSTRAINT_TYPE:
                    com.bulletphysics.dynamics.constraintsolver.SliderConstraint sc = (com.bulletphysics.dynamics.constraintsolver.SliderConstraint)constraint;
                    com.bulletphysics.linearmath.Transform t = new com.bulletphysics.linearmath.Transform();
                    com.bulletphysics.linearmath.Transform t2 = new com.bulletphysics.linearmath.Transform();
                    t2.setIdentity();
                    sc.getRigidBodyA().getWorldTransform(t);
                    sc.getFrameOffsetA(t2);
                    t.mul(t2);
                    DrawPoint(t, 25, color);

                    t.setIdentity();
                    t2.setIdentity();
                    sc.getRigidBodyB().getWorldTransform(t);
                    sc.getFrameOffsetB(t2);
                    t.mul(t2);
                    DrawPoint(t, 25, new Vector3(0, 1, 1));
                    break;
                case com.bulletphysics.dynamics.constraintsolver.TypedConstraintType.VEHICLE_CONSTRAINT_TYPE:
                    Debug.LogWarning("[GLDebugDrawer] [WARNING]: vehicle constraint drawing type not implemented yet");
                    break;
            }

            GL.PopMatrix();
            GL.Enable(GL.LIGHTING); // Placeholder
        }

        /// <summary>
        /// Draw point.
        /// </summary>
        /// <param name="trans">The transform.</param>
        /// <param name="size">The size.</param>
        /// <param name="color">The color.</param>
        public static void DrawPoint(com.bulletphysics.linearmath.Transform trans, float size, Vector3 color)
        {
            GL.PushMatrix();
            GL.Disable(GL.TEXTURE_2D); // Placeholder
            GL.Enable(GL.COLOR_MATERIAL); // Placeholder
            GL.Disable(GL.LIGHTING); // Placeholder
            GL.Color(new Color(color.x, color.y, color.z, 1f));

            Matrix4x4 unityMatrix = Matrix4x4.identity;
            if (trans != null)
            {
                unityMatrix.SetTRS(trans.origin, trans.basis.rotation, trans.basis.lossyScale); // Simplified
            }
            GL.MultMatrix(unityMatrix);

            GL.Begin(GL.LINES);
            GL.Vertex3(-size, 0, 0);
            GL.Vertex3(+size, 0, 0);
            GL.Vertex3(0, 0, -size);
            GL.Vertex3(0, 0, +size);
            GL.Vertex3(0, -size, 0);
            GL.Vertex3(0, +size, 0);
            GL.End();

            GL.Disable(GL.COLOR_MATERIAL); // Placeholder
            GL.Enable(GL.LIGHTING); // Placeholder
            GL.PopMatrix();
        }

        /// <summary>
        /// Draw line.
        /// </summary>
        /// <param name="from">The start point.</param>
        /// <param name="to">The end point.</param>
        /// <param name="color">The color.</param>
        public override void drawLine(Vector3 from, Vector3 to, Vector3 color)
        {
            // In Unity, for runtime debug drawing, Debug.DrawLine is often preferred.
            // For GL drawing, you need to set up a material and use GL.Begin/End.
            // This method is part of the IDebugDraw interface, so it's expected to draw.
            // For simplicity, we'll use GL.LINES here.
            GL.Begin(GL.LINES);
            GL.Color(new Color(color.x, color.y, color.z, 1f));
            GL.Vertex3(from.x, from.y, from.z);
            GL.Vertex3(to.x, to.y, to.z);
            GL.End();
        }

        /// <summary>
        /// Draw contact point.
        /// </summary>
        /// <param name="pointOnB">The contact point on B.</param>
        /// <param name="normalOnB">The normal on B.</param>
        /// <param name="distance">The distance.</param>
        /// <param name="lifeTime">The lifetime.</param>
        /// <param name="color">The color.</param>
        public override void drawContactPoint(Vector3 pointOnB, Vector3 normalOnB, float distance, int lifeTime, Vector3 color)
        {
            if ((debugMode & com.bulletphysics.linearmath.DebugDrawModes.DRAW_CONTACT_POINTS) != 0)
            {
                Vector3 to = tmpVec;
                to = pointOnB + normalOnB * (distance * 100f); // Simplified scaleAdd

                Vector3 from = pointOnB;

                if (DEBUG_NORMALS)
                {
                    to = normalOnB.normalized * 10f + pointOnB; // Simplified scale and add
                    // GL.LineWidth and GL.PointSize are not directly supported in Unity's GL class.
                    // These would typically be handled by shaders.
                    // GL.Begin(GL.POINTS); // Not directly supported for size control
                    // GL.Color(new Color(color.x, color.y, color.z, 1f));
                    // GL.Vertex3(from.x, from.y, from.z);
                    // GL.End();
                }

                GL.Begin(GL.LINES);
                GL.Color(new Color(color.x, color.y, color.z, 1f));
                GL.Vertex3(from.x, from.y, from.z);
                GL.Vertex3(to.x, to.y, to.z);
                GL.End();
            }
        }

        /// <summary>
        /// Report error warning.
        /// </summary>
        /// <param name="warningString">The warning string.</param>
        public override void reportErrorWarning(string warningString)
        {
            Debug.LogError(warningString);
        }

        /// <summary>
        /// Draw 3d text.
        /// </summary>
        /// <param name="location">The location.</param>
        /// <param name="textString">The text string.</param>
        public override void draw3dText(Vector3 location, string textString)
        {
            // In Unity, 3D text is typically rendered using TextMeshPro or custom GUI.
            // This method will not directly draw text using GL.
            Debug.LogWarning($"[GLDebugDrawer] 3D Text drawing not implemented: {textString} at {location}");
        }

        /// <summary>
        /// Switch debug mode.
        /// </summary>
        public void SwitchDebug()
        {
            debugMode = Org.Schema.Schine.GraphicsEngine.Core.Settings.EngineSettings.P_PHYSICS_DEBUG_MODE.getInt();
            if (debugMode == 0)
            {
                debugMode = 1;
            }
            else if (debugMode < 1024)
            {
                debugMode *= 2;
            }
            else
            {
                debugMode = 0;
            }

            string rep = "";
            switch (debugMode)
            {
                case com.bulletphysics.linearmath.DebugDrawModes.NO_DEBUG: rep = "no debug mode"; break;
                case com.bulletphysics.linearmath.DebugDrawModes.DRAW_WIREFRAME: rep = "wireframe"; break;
                case com.bulletphysics.linearmath.DebugDrawModes.DRAW_AABB: rep = "aabb"; break;
                case com.bulletphysics.linearmath.DebugDrawModes.DRAW_FEATURES_TEXT: rep = "feautures text"; break;
                case com.bulletphysics.linearmath.DebugDrawModes.DRAW_CONTACT_POINTS: rep = "contact points"; break;
                case com.bulletphysics.linearmath.DebugDrawModes.NO_DEACTIVATION: rep = "no deactivation"; break;
                case com.bulletphysics.linearmath.DebugDrawModes.NO_HELP_TEXT: rep = "no help text"; break;
                case com.bulletphysics.linearmath.DebugDrawModes.DRAW_TEXT: rep = "draw text"; break;
                case com.bulletphysics.linearmath.DebugDrawModes.PROFILE_TIMINGS: rep = "profile timings"; break;
                case com.bulletphysics.linearmath.DebugDrawModes.ENABLE_SAT_COMPARISON: rep = "sat comparison"; break;
                case com.bulletphysics.linearmath.DebugDrawModes.DISABLE_BULLET_LCP: rep = "disable bullet lcp"; break;
                case com.bulletphysics.linearmath.DebugDrawModes.ENABLE_CCD: rep = "enable ccd"; break;
                case com.bulletphysics.linearmath.DebugDrawModes.MAX_DEBUG_DRAW_MODE: rep = "max debug mode"; break;
                default: rep = ""; break;
            }
            Debug.LogError($"[GLDebugDrawer] DebugMode: {rep}"); // Using Error for now as original used System.err
        }

        /// <summary>
        /// The Class GlDrawcallback.
        /// </summary>
        private class GlDrawcallback : com.bulletphysics.collision.shapes.TriangleCallback
        {
            /// <summary>
            /// The wireframe.
            /// </summary>
            public bool wireframe = false;

            /// <summary>
            /// Instantiates a new gl drawcallback.
            /// </summary>
            public GlDrawcallback() { }

            /// <summary>
            /// Process triangle.
            /// </summary>
            /// <param name="triangle">The triangle vertices.</param>
            /// <param name="partId">The part ID.</param>
            /// <param name="triangleIndex">The triangle index.</param>
            public override void processTriangle(Vector3[] triangle, int partId, int triangleIndex)
            {
                GL.Disable(GL.LIGHTING); // Placeholder
                GL.Disable(GL.TEXTURE_2D); // Placeholder
                GL.Color(new Color(1, 1, 1, 1));
                if (wireframe)
                {
                    GL.Begin(GL.LINES);
                    GL.Color(new Color(1, 0, 0, 1));
                    GL.Vertex3(triangle[0].x, triangle[0].y, triangle[0].z);
                    GL.Vertex3(triangle[1].x, triangle[1].y, triangle[1].z);
                    GL.Color(new Color(0, 1, 0, 1));
                    GL.Vertex3(triangle[2].x, triangle[2].y, triangle[2].z);
                    GL.Vertex3(triangle[1].x, triangle[1].y, triangle[1].z);
                    GL.Color(new Color(0, 0, 1, 1));
                    GL.Vertex3(triangle[2].x, triangle[2].y, triangle[2].z);
                    GL.Vertex3(triangle[0].x, triangle[0].y, triangle[0].z);
                    GL.End();
                }
                else
                {
                    GL.Begin(GL.TRIANGLES);
                    GL.Color(new Color(1, 0, 0, 1));
                    GL.Vertex3(triangle[0].x, triangle[0].y, triangle[0].z);
                    GL.Color(new Color(0, 1, 0, 1));
                    GL.Vertex3(triangle[1].x, triangle[1].y, triangle[1].z);
                    GL.Color(new Color(0, 0, 1, 1));
                    GL.Vertex3(triangle[2].x, triangle[2].y, triangle[2].z);
                    GL.End();
                }
                GL.Enable(GL.LIGHTING); // Placeholder
            }
        }

        /// <summary>
        /// Get debug mode.
        /// </summary>
        /// <returns>The current debug mode.</returns>
        public override int getDebugMode()
        {
            debugMode = Mathf.Min(1025, Org.Schema.Schine.GraphicsEngine.Core.Settings.EngineSettings.P_PHYSICS_DEBUG_MODE.getInt());
            return debugMode;
        }

        /// <summary>
        /// Set debug mode.
        /// </summary>
        /// <param name="debugMode">The debug mode to set.</param>
        public override void setDebugMode(int debugMode)
        {
            this.debugMode = debugMode;
        }
    }
}