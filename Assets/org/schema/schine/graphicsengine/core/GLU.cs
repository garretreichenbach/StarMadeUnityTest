using UnityEngine;

namespace Org.Schema.Common.Util.LinAlg {
	
	public static class Matrix4fTools {
		
		public static void unproject(Matrix4x4 projection, Matrix4x4 modelview, float winX, float winY, float winZ,
			int[] viewport, out Vector4 coord) {
			// This is a re-implementation of gluUnProject using Unity's Matrix4x4
			// Combined matrix
			Matrix4x4 finalMatrix = projection * modelview;

			// Invert the combined matrix
			Matrix4x4 inverseFinalMatrix = finalMatrix.inverse;

			// Map window coordinates to normalized device coordinates (-1 to 1)
			Vector4 inVec = new Vector4();
			inVec.x = (winX - viewport[0]) / viewport[2] * 2.0f - 1.0f;
			inVec.y = (winY - viewport[1]) / viewport[3] * 2.0f - 1.0f;
			inVec.z = winZ * 2.0f - 1.0f;
			inVec.w = 1.0f;

			// Transform to world coordinates
			coord = inverseFinalMatrix * inVec;

			if(coord.w == 0.0f) {
				// This indicates an error or a point at infinity
				Debug.LogError("Unproject failed: w component is zero.");
				coord = Vector4.zero; // Or throw an exception
				return;
			}

			coord.x /= coord.w;
			coord.y /= coord.w;
			coord.z /= coord.w;
		}
	}

	public static class Vector3fTools {
		// This was a helper for javax.vecmath.Vector3f.
		// In Unity, Vector3 has built-in operations.
		public static Vector3 sub(Vector3 a, Vector3 b) { return a - b; }

		public static void normalize(float[] v) {
			Vector3 vec = new Vector3(v[0], v[1], v[2]);
			vec.Normalize();
			v[0] = vec.x;
			v[1] = vec.y;
			v[2] = vec.z;
		}

		public static void cross(float[] v1, float[] v2, float[] result) {
			Vector3 vec1 = new Vector3(v1[0], v1[1], v1[2]);
			Vector3 vec2 = new Vector3(v2[0], v2[1], v2[2]);
			Vector3 crossProduct = Vector3.Cross(vec1, vec2);
			result[0] = crossProduct.x;
			result[1] = crossProduct.y;
			result[2] = crossProduct.z;
		}
	}
}


namespace Org.Schema.Schine.GraphicsEngine.Core {
	
	public static class GLU {

		public static Matrix4x4 Ortho(float left, float right,
			float bottom, float top,
			float zNear, float zFar) {
			return Matrix4x4.Ortho(left, right, bottom, top, zNear, zFar);
		}

		public static Matrix4x4 Frustum(float left, float right,
			float bottom, float top,
			float zNear, float zFar) {
			// Unity's Matrix4x4.Frustum is a bit different in parameters.
			// Re-implementing based on the original formula for consistency.
			Matrix4x4 m = new Matrix4x4();

			m[0, 0] = (2 * zNear) / (right - left);
			m[1, 1] = (2 * zNear) / (top - bottom);
			m[0, 2] = (right + left) / (right - left);
			m[1, 2] = (top + bottom) / (top - bottom);
			m[2, 2] = -(zFar + zNear) / (zFar - zNear);
			m[3, 2] = -1;
			m[2, 3] = -(2 * zFar * zNear) / (zFar - zNear);

			return m;
		}

		public static Matrix4x4 Perspective(float fovy, float aspect, float zNear, float zFar) {
			// Unity's Matrix4x4.Perspective is available.
			// Re-implementing based on the original formula for consistency.
			float range = Mathf.Tan(fovy / 2 * Mathf.Deg2Rad) * zNear;
			float left = -range * aspect;
			float right = range * aspect;
			float bottom = -range;
			float top = range;

			Matrix4x4 m = new Matrix4x4();

			m[0, 0] = (2 * zNear) / (right - left);
			m[1, 1] = (2 * zNear) / (top - bottom);
			m[2, 2] = -(zFar + zNear) / (zFar - zNear);
			m[3, 2] = -1;
			m[2, 3] = -(2 * zFar * zNear) / (zFar - zNear);

			return m;
		}

		public static Matrix4x4 LookAt(Vector3 eye, Vector3 center, Vector3 up) {
			// Unity's Matrix4x4.LookAt is not directly available, but Camera.LookAt is.
			// We can construct the view matrix manually.
			Vector3 forward = (center - eye).normalized;
			Vector3 u = up.normalized;

			Vector3 side = Vector3.Cross(forward, u).normalized;
			u = Vector3.Cross(side, forward); // Recompute up

			Matrix4x4 m = new Matrix4x4();

			m[0, 0] = side.x;
			m[1, 0] = side.y;
			m[2, 0] = side.z;
			m[3, 0] = 0;

			m[0, 1] = u.x;
			m[1, 1] = u.y;
			m[2, 1] = u.z;
			m[3, 1] = 0;

			m[0, 2] = -forward.x;
			m[1, 2] = -forward.y;
			m[2, 2] = -forward.z;
			m[3, 2] = 0;

			m[0, 3] = 0;
			m[1, 3] = 0;
			m[2, 3] = 0;
			m[3, 3] = 1;

			// Apply translation for eye position
			Matrix4x4 translation = Matrix4x4.Translate(-eye);
			return m * translation;
		}

		public static void GluUnProject(float winX, float winY, float winZ, Matrix4x4 modelviewTemp,
			Matrix4x4 projectionTemp, int[] viewportTemp, out Vector4 coord) {
			Matrix4fTools.unproject(projectionTemp, modelviewTemp, winX, winY, winZ, viewportTemp, out coord);
		}

		public static void GluOrtho2D(float left, float right, float bottom, float top) {
			// In Unity, this would typically set the Camera's projection matrix
			// For direct GL drawing, we can load it onto the GL matrix stack.
			GL.LoadProjectionMatrix(Ortho(left, right, bottom, top, -1, 1));
		}

		// The following methods (__gluMakeIdentityf, __gluMultMatrixVecf, __gluInvertMatrixf, __gluMultMatricesf)
		// are internal helper functions for raw float arrays in the original Java code.
		// In C# with Unity's Matrix4x4, these are replaced by direct Matrix4x4 operations.
		// Therefore, they are removed from the public API of this class.

		// The FloatBuffer and float[] fields are also removed as Matrix4x4 handles matrix data directly.
		// private static final float[] IDENTITY_MATRIX;
		// private static final FloatBuffer matrix;
		// private static final FloatBuffer finalMatrix;
		// private static final FloatBuffer tempMatrix;
		// private static final float[] in;
		// private static final float[] out;
		// private static final float[] forward;
		// private static final float[] side;
		// private static final float[] up;


		/// <summary>
		/// Method gluPerspective.
		/// </summary>
		/// <param name="fovy">Field of view in Y direction.</param>
		/// <param name="aspect">Aspect ratio.</param>
		/// <param name="zNear">Near clipping plane.</param>
		/// <param name="zFar">Far clipping plane.</param>
		public static void GluPerspective(float fovy, float aspect, float zNear, float zFar) {
			// This method modifies the current GL matrix stack.
			// In Unity, you would typically set Camera.projectionMatrix.
			// For direct GL drawing, we can use GL.LoadProjectionMatrix.
			GL.LoadProjectionMatrix(Perspective(fovy, aspect, zNear, zFar));
		}

		/// <summary>
		/// Method gluLookAt
		/// </summary>
		/// <param name="eyex">Eye X position.</param>
		/// <param name="eyey">Eye Y position.</param>
		/// <param name="eyez">Eye Z position.</param>
		/// <param name="centerx">Center X position.</param>
		/// <param name="centery">Center Y position.</param>
		/// <param name="centerz">Center Z position.</param>
		/// <param name="upx">Up vector X component.</param>
		/// <param name="upy">Up vector Y component.</param>
		/// <param name="upz">Up vector Z component.</param>
		public static void GluLookAt(
			float eyex,
			float eyey,
			float eyez,
			float centerx,
			float centery,
			float centerz,
			float upx,
			float upy,
			float upz) {
			// This method modifies the current GL matrix stack.
			// In Unity, you would typically set Camera.worldToCameraMatrix.
			// For direct GL drawing, we can use GL.LoadMatrix.
			GL.LoadMatrix(LookAt(new Vector3(eyex, eyey, eyez), new Vector3(centerx, centery, centerz),
				new Vector3(upx, upy, upz)));
		}


		/// <summary>
		/// Method gluProject
		/// </summary>
		/// <param name="objx">Object X coordinate.</param>
		/// <param name="objy">Object Y coordinate.</param>
		/// <param name="objz">Object Z coordinate.</param>
		/// <param name="modelMatrix">Modelview matrix.</param>
		/// <param name="projMatrix">Projection matrix.</param>
		/// <param name="viewport">Viewport array [x, y, width, height].</param>
		/// <param name="win_pos">Output window coordinates (x, y, z).</param>
		/// <returns>True if successful, false otherwise.</returns>
		public static bool GluProject(
			float objx,
			float objy,
			float objz,
			Matrix4x4 modelMatrix,
			Matrix4x4 projMatrix,
			int[] viewport,
			out Vector3 win_pos) {
			// In Unity, Camera.WorldToScreenPoint is the equivalent.
			// Re-implementing based on the original GLU logic for consistency with the original code's intent.

			Vector4 inVec = new Vector4(objx, objy, objz, 1.0f);
			Vector4 outVec;

			// Multiply by modelview matrix
			outVec = modelMatrix * inVec;
			// Multiply by projection matrix
			inVec = projMatrix * outVec;

			if(inVec.w == 0.0f) {
				win_pos = Vector3.zero;
				return false;
			}

			// Normalize to clip coordinates
			inVec.x /= inVec.w;
			inVec.y /= inVec.w;
			inVec.z /= inVec.w;

			// Map to normalized device coordinates (0 to 1)
			inVec.x = inVec.x * 0.5f + 0.5f;
			inVec.y = inVec.y * 0.5f + 0.5f;
			inVec.z = inVec.z * 0.5f + 0.5f;

			// Map to window coordinates
			win_pos = new Vector3(
				inVec.x * viewport[2] + viewport[0],
				inVec.y * viewport[3] + viewport[1],
				inVec.z // Z is already in [0,1] range, representing depth
			);

			return true;
		}

		/// <summary>
		/// Method gluUnProject
		/// </summary>
		/// <param name="winx">Window X coordinate.</param>
		/// <param name="winy">Window Y coordinate.</param>
		/// <param name="winz">Window Z coordinate.</param>
		/// <param name="modelMatrix">Modelview matrix.</param>
		/// <param name="projMatrix">Projection matrix.</param>
		/// <param name="viewport">Viewport array [x, y, width, height].</param>
		/// <param name="obj_pos">Output object coordinates (x, y, z).</param>
		/// <returns>True if successful, false otherwise.</returns>
		public static bool GluUnProject(
			float winx,
			float winy,
			float winz,
			Matrix4x4 modelMatrix,
			Matrix4x4 projMatrix,
			int[] viewport,
			out Vector3 obj_pos) {
			// In Unity, Camera.ScreenToWorldPoint is the equivalent.
			// Re-implementing based on the original GLU logic for consistency with the original code's intent.

			Vector4 inVec = new Vector4();
			Vector4 outVec;

			// Combine matrices and invert
			Matrix4x4 finalMatrix = projMatrix * modelMatrix;
			Matrix4x4 inverseFinalMatrix = finalMatrix.inverse;

			if(!inverseFinalMatrix.ValidTRS()) // Check if inverse is valid
			{
				obj_pos = Vector3.zero;
				return false;
			}

			// Map window coordinates to normalized device coordinates (-1 to 1)
			inVec.x = (winx - viewport[0]) / viewport[2] * 2.0f - 1.0f;
			inVec.y = (winy - viewport[1]) / viewport[3] * 2.0f - 1.0f;
			inVec.z = winz * 2.0f - 1.0f;
			inVec.w = 1.0f;

			// Transform to world coordinates
			outVec = inverseFinalMatrix * inVec;

			if(outVec.w == 0.0f) {
				obj_pos = Vector3.zero;
				return false;
			}

			obj_pos = new Vector3(outVec.x / outVec.w, outVec.y / outVec.w, outVec.z / outVec.w);
			return true;
		}

		/// <summary>
		/// Method gluPickMatrix
		/// </summary>
		/// <param name="x">Picking region center X coordinate.</param>
		/// <param name="y">Picking region center Y coordinate.</param>
		/// <param name="deltaX">Picking region width.</param>
		/// <param name="deltaY">Picking region height.</param>
		/// <param name="viewport">Viewport array [x, y, width, height].</param>
		public static void GluPickMatrix(
			float x,
			float y,
			float deltaX,
			float deltaY,
			int[] viewport) {
			if(deltaX <= 0 || deltaY <= 0) {
				return;
			}

			// This function modifies the current GL matrix stack.
			// In Unity, this would typically be used to create a custom projection matrix for picking.
			// For direct GL drawing, we can use GL.Translate and GL.Scale.
			GL.Translate(new Vector3(
				(viewport[2] - 2 * (x - viewport[0])) / deltaX,
				(viewport[3] - 2 * (y - viewport[1])) / deltaY,
				0));
			GL.Scale(new Vector3(viewport[2] / deltaX, viewport[3] / deltaY, 1.0f));
		}
	}
}