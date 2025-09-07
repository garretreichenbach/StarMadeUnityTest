using System;
using UnityEngine;

namespace Org.Schema.Schine.GraphicsEngine.Core
{
    /// <summary>
    /// A wrapper class for Unity's RenderTexture, providing similar functionality to the original FrameBufferObjects.
    /// </summary>
    public class FrameBufferObjects
    {
        public readonly string Name;
        private RenderTexture _renderTexture;
        private RenderTexture _blitTargetRenderTexture; // Used for multisampling blit
        private int _width;
        private int _height;
        private bool _isMultisampled;
        private bool _withDepthTexture; // Indicates if the main render texture has a depth buffer
        private bool _drawWithDepthTest; // This might be handled by shaders/materials in Unity

        /// <summary>
        /// Gets the main RenderTexture managed by this FrameBufferObjects instance.
        /// </summary>
        public RenderTexture RenderTexture => _renderTexture;

        /// <summary>
        /// Gets the width of the framebuffer.
        /// </summary>
        public int Width => _width;

        /// <summary>
        /// Gets the height of the framebuffer.
        /// </summary>
        public int Height => _height;

        /// <summary>
        /// Indicates if the framebuffer is multisampled.
        /// </summary>
        public bool IsMultisampled => _isMultisampled;

        /// <summary>
        /// Indicates if the framebuffer has a depth texture/buffer.
        /// </summary>
        public bool WithDepthTexture => _withDepthTexture;

        /// <summary>
        /// Gets or sets whether drawing should occur with depth testing.
        /// In Unity, this is typically controlled by materials and shaders.
        /// </summary>
        public bool DrawWithDepthTest
        {
            get => _drawWithDepthTest;
            set => _drawWithDepthTest = value;
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="FrameBufferObjects"/> class.
        /// </summary>
        /// <param name="name">The name of the framebuffer.</param>
        /// <param name="width">The width of the framebuffer.</param>
        /// <param name="height">The height of the framebuffer.</param>
        /// <param name="isMultisampled">True if the framebuffer should be multisampled.</param>
        /// <param name="withDepthTexture">True if the framebuffer should have a depth buffer.</param>
        /// <param name="format">The format of the color buffer.</param>
        /// <param name="depthBits">The depth buffer precision in bits. Use 0 for no depth buffer.</param>
        public FrameBufferObjects(string name, int width, int height, bool isMultisampled = false, bool withDepthTexture = true, RenderTextureFormat format = RenderTextureFormat.ARGB32, int depthBits = 24)
        {
            Name = name;
            _width = width;
            _height = height;
            _isMultisampled = isMultisampled;
            _withDepthTexture = withDepthTexture;

            Initialize(format, depthBits);
        }

        private void Initialize(RenderTextureFormat format, int depthBits)
        {
            Debug.Log($"[FrameBuffer] Creating frame buffer '{Name}' {Width}x{Height}; Multisampled: {IsMultisampled}; WithDepth: {WithDepthTexture}");

            // Determine depth buffer setting based on _withDepthTexture
            int actualDepthBits = _withDepthTexture ? depthBits : 0;

            _renderTexture = new RenderTexture(Width, Height, actualDepthBits, format);
            _renderTexture.name = Name;
            _renderTexture.enableRandomWrite = true; // Often useful for compute shaders
            _renderTexture.antiAliasing = _isMultisampled ? QualitySettings.antiAliasing : 1; // Use project quality settings for AA
            _renderTexture.Create();

            if (_isMultisampled)
            {
                // Create a non-multisampled render texture for blitting
                // This blit target should also have a depth buffer if the source does.
                _blitTargetRenderTexture = new RenderTexture(Width, Height, actualDepthBits, format);
                _blitTargetRenderTexture.name = $"{Name}_BlitTarget";
                _blitTargetRenderTexture.enableRandomWrite = true;
                _blitTargetRenderTexture.Create();
            }
        }

        /// <summary>
        /// Cleans up the RenderTextures.
        /// </summary>
        public void CleanUp()
        {
            Debug.Log($"[FBO] Cleaning up FBO '{Name}'");
            if (_renderTexture != null)
            {
                _renderTexture.Release();
                UnityEngine.Object.Destroy(_renderTexture); // Destroy the asset
                _renderTexture = null;
            }
            if (_blitTargetRenderTexture != null)
            {
                _blitTargetRenderTexture.Release();
                UnityEngine.Object.Destroy(_blitTargetRenderTexture); // Destroy the asset
                _blitTargetRenderTexture = null;
            }
        }

        /// <summary>
        /// Enables this framebuffer, making it the active render target.
        /// </summary>
        public void Enable()
        {
            RenderTexture.active = _renderTexture;
        }

        /// <summary>
        /// Disables this framebuffer, restoring the default render target.
        /// If multisampled, blits the content to a non-multisampled target.
        /// </summary>
        public void Disable()
        {
            if (_isMultisampled && _blitTargetRenderTexture != null)
            {
                Graphics.Blit(_renderTexture, _blitTargetRenderTexture);
            }
            RenderTexture.active = null;
        }

        /// <summary>
        /// Gets the ID of the main color texture.
        /// If multisampled, returns the ID of the blit target's texture.
        /// </summary>
        /// <returns>The instance ID of the main RenderTexture, or 0 if not created.</returns>
        public int GetTextureId()
        {
            // If multisampled, return the blit target's texture ID for external use
            return (_isMultisampled ? _blitTargetRenderTexture : _renderTexture)?.GetInstanceID() ?? 0;
        }

        /// <summary>
        /// Gets the ID of the depth texture.
        /// If multisampled, returns the ID of the blit target's depth buffer.
        /// </summary>
        /// <returns>The instance ID of the depth buffer of the main RenderTexture, or 0 if not available.</returns>
        public int GetDepthTextureId()
        {
            if (!_withDepthTexture)
            {
                throw new InvalidOperationException("No depth texture initialized for this framebuffer.");
            }
            // If multisampled, return the blit target's depth buffer ID for external use
            // Otherwise, return the main render texture's depth buffer ID.
            return (_isMultisampled ? _blitTargetRenderTexture : _renderTexture)?.depthBuffer.GetInstanceID() ?? 0;
        }

        /// <summary>
        /// Renders a fullscreen quad using the specified shader.
        /// This method assumes the shader handles the rendering logic.
        /// The x, y, viewportWidth, viewportHeight parameters from the original method are not directly
        /// supported by Graphics.Blit for sub-rectangle blitting. If this functionality is critical,
        /// a custom rendering solution (e.g., a mesh with specific UVs) would be required.
        /// </summary>
        /// <param name="shader">The shader to use for rendering.</param>
        public void RenderFullscreenQuad(Shader shader)
        {
            if (shader == null)
            {
                Debug.LogError("Shader is null for RenderFullscreenQuad.");
                return;
            }
            Graphics.Blit(_renderTexture, (RenderTexture)null, new Material(shader)); // Blit to screen using the shader
        }

        /// <summary>
        /// Renders a fullscreen quad using a default blit shader.
        /// The x, y, viewportWidth, viewportHeight parameters from the original method are not directly
        /// supported by Graphics.Blit for sub-rectangle blitting. If this functionality is critical,
        /// a custom rendering solution (e.g., a mesh with specific UVs) would be required.
        /// </summary>
        public void RenderFullscreenQuad()
        {
            Graphics.Blit(_renderTexture, (RenderTexture)null); // Blit to screen using default blit shader
        }
    }
}