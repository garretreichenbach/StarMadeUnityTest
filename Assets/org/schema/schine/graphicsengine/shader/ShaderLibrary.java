/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>ShaderLibrary</H2>
 * <H3>org.schema.schine.graphicsengine.shader</H3>
 * ShaderLibrary.java
 * <HR>
 * Description goes here. If you see this message, please contact me and the
 * description will be filled.<BR>
 * <BR>
 *
 * @author Robin Promesberger (schema)
 * @mail <A HREF="mailto:schemaxx@gmail.com">schemaxx@gmail.com</A>
 * @site <A
 * HREF="http://www.the-schema.com/">http://www.the-schema.com/</A>
 * @project JnJ / VIR / Project R
 * @homepage <A
 * HREF="http://www.the-schema.com/JnJ">
 * http://www.the-schema.com/JnJ</A>
 * @copyright Copyright � 2004-2010 Robin Promesberger (schema)
 * @licence Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.schema.schine.graphicsengine.shader;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import org.lwjgl.opengl.GL20;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.data.DataUtil;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;

import java.util.HashSet;
import java.util.Set;

/**
 * The Class ShaderLibrary.
 */
public class ShaderLibrary {

	/**
	 * The shaders.
	 */
	public static final Set<Shader> shaders = new HashSet<Shader>();
	public static int CUBE_VERTEX_COMPONENTS = EngineSettings.G_USE_TWO_COMPONENT_SHADER.isOn() ? 2 : 4;
	/**
	 * The smoke shader.
	 */
//	public static Shader smokeShader;
//	public static Shader volumeSmokeShader;
	public static Shader projectileShader;
	public static Shader projectileQuadShader;
	public static Shader projectileQuadBloomShader;
	public static Shader projectileBeamQuadShader;
	public static Shader starShader;
	public static Shader starFlareShader;
	public static Shader plasmaShader;
	public static Shader cubeShieldShader;
	public static Shader scanlineShader;
	public static Shader shardShader;
	public static Shader guiTextureWrapperShader;
	public static Shader jumpOverlayShader;
	public static Shader ocDistortion;
	public static Shader powerBarShader;
	public static Shader powerBarShaderHor;
	public static Shader colorBoxShader;
	public static Shader gamma;

	/**
	 * The heightField shader.
	 */
	public static Shader terrainShader;

	/**
	 * The water shader.
	 */
	public static Shader waterShader;

	/** The sky shader. */
	//	public static Shader skyShader;

	/**
	 * The explosion shader.
	 */
	public static Shader explosionShader;
	public static Shader simpleColorShader;
	public static Shader spacedustShader;

	/**
	 * The fog shader.
	 */
	public static Shader fogShader;

	/** The point light shader. */
	//	public static Shader pointLightShader;

	/** The sky shader2. */
	//	public static Shader skyShader2;

	/**
	 * The bump shader.
	 */
	public static Shader bumpShader;

	public static Shader planetShader;
	public static Shader atmosphereShader;
	public static Shader bloomShader;
	public static Shader sunShader;
	public static Shader lavaShader;
	public static Shader silhouetteShader;
	public static Shader blackHoleShader;

	public static Shader hoffmanSkyShader;

	public static Shader hoffmanTerrainShader;

	//	public static Shader oceanAtmosphereShader;
	//	public static Shader oceanCloundsShader;
	//	public static Shader oceanFFTXShader;
	//	public static Shader oceanFFTYShader;
	//	public static Shader oceanInitShader;
	//	public static Shader oceanOceanShader;
	//	public static Shader oceanSkyShader;
	//	public static Shader oceanSkyMapShader;
	//	public static Shader oceanVariancesShader;
	public static Shader cubemapShader;

	public static Shader bloomShaderPass1;
	public static Shader bloomShaderPass2;
	public static Shader bloomShaderPass3;
	public static Shader bloomShaderPass4;
	public static Shader bloomShader1Vert;
	public static Shader bloomShader2Hor;
	public static Shader downsamplerFirst;
	public static Shader downsampler;

	public static Shader silhouetteShader2D;
	public static Shader silhouetteAlpha;
	public static Shader shadowShaderCubes;
	public static Shader depthCubeShader;
	public static Shader lodCubeShaderD4;
	public static Shader lodCubeShaderD8;
	public static Shader shadowShaderCubesBlend;
	public static Shader graphConnectionShader;

	public static Shader bloomSimpleShader;
	public static Shader transporterShader;
	public static Shader mineShader;

	//	public static Shader cubeShader;
//	public static Shader cubeShader13;
//	public static Shader cubeShader13Passthrough;
//	public static Shader cubeShader13Blended;
//	public static Shader cubeShader13VertexLight;
//	public static Shader cubeShader13VertexLightBlend;
	public static Shader cubeShader13Simple;

//	public static Shader cubeShader13LightAll;
//	public static Shader cubeShader13LightAllBlended;
//	public static Shader cubeShader13VertexLightLightAll;
//	public static Shader cubeShader13VertexLightBlendLightAll;
//	public static Shader cubeShader13Single;

	public static Shader exaustShader;

	//	public static Shader shieldShader;
	public static Shader selectionShader;
	public static Shader solidSelectionShader;
	public static Shader beamBoxShader;
	public static Shader simpleBeamShader;
	public static Shader godRayShader;
	public static Shader projectileTrailShader;
	public static Shader lensFlareShader;
	public static Shader perpixelShader;
//	public static Shader nebulaShader;
	public static Shader skyFromAtmo;
	public static Shader skyFromSpace;
	public static Shader gasPlanetSurfaceShader;
	public static Shader gasGiantInnerAtmoShader;
	public static Shader gasGiantOuterAtmoShader;
	public static Shader tubesShader;
	public static Shader tubesStreamShader;
	public static Shader pulseShader;
	public static Shader shad_single_prog;
	public static Shader shad_single_hl_prog;

	//	shad_single_prog = createShaders( "../../src/cascaded_shadow_maps/shadow_vertex.glsl", "../../src/cascaded_shadow_maps/shadow_single_fragment.glsl");
//	shad_single_hl_prog = createShaders( "../../src/cascaded_shadow_maps/shadow_vertex.glsl", "../../src/cascaded_shadow_maps/shadow_single_hl_fragment.glsl");
//	shad_multi_prog = createShaders( "../../src/cascaded_shadow_maps/shadow_vertex.glsl", "../../src/cascaded_shadow_maps/shadow_multi_leak_fragment.glsl");
//	shad_multi_noleak_prog = createShaders( "../../src/cascaded_shadow_maps/shadow_vertex.glsl", "../../src/cascaded_shadow_maps/shadow_multi_noleak_fragment.glsl");
//    shad_pcf_prog = createShaders( "../../src/cascaded_shadow_maps/shadow_vertex.glsl", "../../src/cascaded_shadow_maps/shadow_pcf_fragment.glsl");
//    shad_pcf_trilin_prog = createShaders( "../../src/cascaded_shadow_maps/shadow_vertex.glsl", "../../src/cascaded_shadow_maps/shadow_pcf_trilinear_fragment.glsl");
//    shad_pcf_4tap_prog = createShaders( "../../src/cascaded_shadow_maps/shadow_vertex.glsl", "../../src/cascaded_shadow_maps/shadow_pcf_4tap_fragment.glsl");
//    shad_pcf_8tap_prog = createShaders( "../../src/cascaded_shadow_maps/shadow_vertex.glsl", "../../src/cascaded_shadow_maps/shadow_pcf_8tap_random_fragment.glsl");
//    shad_pcf_gaussian_prog = createShaders( "../../src/cascaded_shadow_maps/shadow_vertex.glsl", "../../src/cascaded_shadow_maps/shadow_pcf_gaussian_fragment.glsl");
	public static Shader shad_multi_prog;
	public static Shader shad_multi_noleak_prog;
	public static Shader shad_pcf_prog;
	public static Shader shad_pcf_trilin_prog;
	public static Shader shad_pcf_4tap_prog;
	public static Shader shad_pcf_8tap_prog;
	public static Shader shad_pcf_gaussian_prog;
	public static Shader shad_view;
	public static Shader fieldShader;
	public static Shader bgShader;
	public static Shader tunnelShader;
    private static Int2ObjectOpenHashMap<Shader> skinningShader = new Int2ObjectOpenHashMap<Shader>();
	private static Int2ObjectOpenHashMap<Shader> skinningShaderSpot = new Int2ObjectOpenHashMap<Shader>();
	private static Int2ObjectOpenHashMap<Shader> cubeShaders = new Int2ObjectOpenHashMap<Shader>();
	public static boolean USE_CUBE_TEXTURE_EMISSION;
	public static Shader lodCubeShader;
	public static Shader lodCubeShaderTangent;
	public static Shader lodCubeShaderNormalOff;
	public static Shader lodCubeShaderShadow;
	public static Shader outlineShader;
	public static Shader cubeGroupShader;
	public static Shader cubeShader13SimpleWhite;
	public static Shader shieldBubbleShader;
	public static Shader lodSimpleMeshCubeShader;

	public static void cleanUp() {
		for (Shader s : shaders) {
			if (s != null) {
				s.cleanUp();
			}
		}
		skinningShader.clear();
		skinningShaderSpot.clear();
		cubeShaders.clear();
	}
	
	/**
	 * Load shaders.
	 *
	 * @param gl  the gl
	 * @param glu the glu
	 * @ the error diolog exception
	 */
	public static void loadShaders() {
		//		System.out.println("[SHADER] now loading shader library");
		try {
			
			if (EngineSettings.G_ATMOSPHERE_SHADER.getObject() == AtmosphereShaderSetting.NORMAL) {
				skyFromSpace = new Shader(DataUtil.dataPath
						+ "/shader/atmosphere/SkyFromSpace.vert", DataUtil.dataPath
						+ "/shader/atmosphere/SkyFromSpace.frag");

				skyFromAtmo = new Shader(DataUtil.dataPath
						+ "/shader/atmosphere/SkyFromAtmosphere.vert", DataUtil.dataPath
						+ "/shader/atmosphere/SkyFromAtmosphere.frag");
			}

			perpixelShader = new Shader(DataUtil.dataPath
					+ "/shader/perpixel/perpixel.vert.glsl", DataUtil.dataPath
					+ "/shader/perpixel/perpixel.frag.glsl");

//			nebulaShader = new Shader(DataUtil.dataPath
//					+ "/shader/nebula/nebula.vert.glsl", DataUtil.dataPath
//					+ "/shader/nebula/nebula.frag.glsl");

			blackHoleShader = new Shader(DataUtil.dataPath
					+ "/shader/blackhole/blackhole.vert.glsl", DataUtil.dataPath
					+ "/shader/blackhole/blackhole.frag.glsl");
			selectionShader = new Shader(DataUtil.dataPath
					+ "/shader/cube/selectionSingle.vert.glsl", DataUtil.dataPath
					+ "/shader/cube/selectionSingle.frag.glsl");
			solidSelectionShader = new Shader(DataUtil.dataPath
					+ "/shader/cube/selectionSolid.vert.glsl", DataUtil.dataPath
					+ "/shader/cube/selectionSolid.frag.glsl");
			beamBoxShader = new Shader(DataUtil.dataPath
					+ "/shader/cube/beamHitSingle.vert.glsl", DataUtil.dataPath
					+ "/shader/cube/beamHitSingle.frag.glsl");

			plasmaShader = new Shader(DataUtil.dataPath
					+ "/shader/plasma/plasma.vert.glsl", DataUtil.dataPath
					+ "/shader/plasma/plasma.frag.glsl");

			godRayShader = new Shader(DataUtil.dataPath
					+ "/shader/bloom/godrays.vert.glsl", DataUtil.dataPath
					+ "/shader/bloom/godrays.frag.glsl");

			tubesShader = new Shader(DataUtil.dataPath
					+ "/shader/tubes/tube.vert.glsl", DataUtil.dataPath
					+ "/shader/tubes/tube.frag.glsl");

			tubesStreamShader = new Shader(DataUtil.dataPath
					+ "/shader/tubes/stream.vert.glsl", DataUtil.dataPath
					+ "/shader/tubes/stream.frag.glsl");

			ocDistortion = new Shader(DataUtil.dataPath
					+ "/shader/ocDistortion/ocDistortion_vert.glsl", DataUtil.dataPath
					+ "/shader/ocDistortion/ocDistortion_frag.glsl");
			
			gamma = new Shader(DataUtil.dataPath
					+ "/shader/gamma/gamma.vert.glsl", DataUtil.dataPath
					+ "/shader/gamma/gamma.frag.glsl");
			graphConnectionShader = new Shader(DataUtil.dataPath
					+ "/shader/hud/graph/graphconnection.vert.glsl", DataUtil.dataPath
					+ "/shader/hud/graph/graphconnection.frag.glsl");

			;

			simpleBeamShader = new Shader(
					DataUtil.dataPath+ "/shader/simplebeam/simplebeam.vert.glsl", 
					DataUtil.dataPath+ "/shader/simplebeam/simplebeam.frag.glsl");
			
			transporterShader = new Shader(DataUtil.dataPath
					+ "/shader/transporter/transporter.vert.glsl", DataUtil.dataPath
					+ "/shader/transporter/transporter.frag.glsl");
			
			mineShader = new Shader(DataUtil.dataPath
					+ "/shader/mine/mine.vert.glsl", DataUtil.dataPath
					+ "/shader/mine/mine.frag.glsl", 
					new ShaderPreprocessor(EngineSettings.isShadowOn() ? "shadow" : null,
							EngineSettings.isShadowOn() ? ((ShadowQuality)EngineSettings.G_SHADOW_QUALITY.getObject()).sName : null,
									EngineSettings.isShadowOn() && EngineSettings.G_SHADOWS_VSM.isOn() ? "VSM" : null));

			//			if(EngineSettings.G_DRAW_SHIELDS.isOn()){
			//				shieldShader = new Shader(DataUtil.dataPath
			//						+ "/shader/shieldhit/shieldhit.vert.glsl", DataUtil.dataPath
			//						+ "/shader/shieldhit/shieldhit.frag.glsl");
			//			}
			shieldBubbleShader = new Shader(DataUtil.dataPath
											+ "/shader/shieldhit/shieldhit.vert.glsl", DataUtil.dataPath
											+ "/shader/shieldhit/shieldhit.frag.glsl");
			pulseShader = new Shader(DataUtil.dataPath
					+ "/shader/pulse/pulse.vert.glsl", DataUtil.dataPath
					+ "/shader/pulse/pulse.frag.glsl");

//			smokeShader = new Shader(DataUtil.dataPath
//					+ "/shader/smoke.vert", DataUtil.dataPath
//					+ "/shader/smoke.frag");
            /*
            terrainShader = new Shader(DataUtil.dataPath
					+ "/shader/terrain.vs", DataUtil.dataPath
					+ "/shader/terrain.fs");

			waterShader = new Shader(DataUtil.dataPath
					+ "/shader/water.vert", DataUtil.dataPath
					+ "/shader/water.frag");
			 */
			//			skyShader = new Shader(DataUtil.dataPath
			//					+ "/shader/sky.vs", DataUtil.dataPath + "/shader/sky.fs");

			//			explosionShader = new Shader(DataUtil.dataPath
			//					+ "/shader/explosion.vert", DataUtil.dataPath + "/shader/explosion.frag");

			//			pointLightShader = new Shader(DataUtil.dataPath
			//					+ "/shader/pointlight.vert", DataUtil.dataPath + "/shader/pointlight.frag");

			bumpShader = new Shader(DataUtil.dataPath
					+ "/shader/bump.vert", DataUtil.dataPath + "/shader/bump.frag");

//			volumeSmokeShader = new Shader(DataUtil.dataPath
//					+ "/shader/volumesmoke.vsh", DataUtil.dataPath + "/shader/volumesmoke.fsh");

			projectileShader = new Shader(DataUtil.dataPath
					+ "/shader/projectiles/standard/projectile.vsh", DataUtil.dataPath + "/shader/projectiles/standard/projectile.fsh");
			
			guiTextureWrapperShader = new Shader(DataUtil.dataPath
					+ "/shader/texture/wrap.vert.glsl", DataUtil.dataPath + "/shader/texture/wrap.frag.glsl");
			
			scanlineShader = new Shader(DataUtil.dataPath
					+ "/shader/scanline/scanline.vert.glsl", DataUtil.dataPath + "/shader/scanline/scanline.frag.glsl");

			projectileTrailShader = new Shader(DataUtil.dataPath
					+ "/shader/projectiles/trail/projectileTrail.vsh", DataUtil.dataPath + "/shader/projectiles/trail/projectileTrail.fsh");

			projectileQuadShader = new Shader(DataUtil.dataPath
					+ "/shader/projectiles/standard/projectileQuad.vsh", DataUtil.dataPath + "/shader/projectiles/standard/projectileQuad.fsh");
			
			projectileQuadBloomShader = new Shader(DataUtil.dataPath
					+ "/shader/projectiles/standard/projectileQuad.vsh", DataUtil.dataPath + "/shader/projectiles/standard/projectileQuad.fsh");

			projectileBeamQuadShader = new Shader(DataUtil.dataPath
					+ "/shader/projectiles/beam/laserProjectile.vert.glsl", DataUtil.dataPath + "/shader/projectiles/beam/laserProjectile.frag.glsl");

			explosionShader = new Shader(DataUtil.dataPath
					+ "/shader/explosion/explosion.vert.glsl", DataUtil.dataPath + "/shader/explosion/explosion.frag.glsl");
			simpleColorShader = new Shader(DataUtil.dataPath
					+ "/shader/simple/color.vert.glsl", DataUtil.dataPath + "/shader/simple/color.frag.glsl");
			
			lodSimpleMeshCubeShader = new Shader(DataUtil.dataPath
					+ "/shader/simple/diffuse.vert.glsl", DataUtil.dataPath + "/shader/simple/diffuse.frag.glsl",
					new ShaderPreprocessor(EngineSettings.isShadowOn() ? "shadow" : null,
							EngineSettings.isShadowOn() ? ((ShadowQuality)EngineSettings.G_SHADOW_QUALITY.getObject()).sName : null,
									EngineSettings.isShadowOn() && EngineSettings.G_SHADOWS_VSM.isOn() ? "VSM" : null));

			if(!EngineSettings.G_SPACE_PARTICLE.isOn()) {
				createSpaceDustShader();
			}

			planetShader = new Shader(DataUtil.dataPath
					+ "/shader/planet.vert.glsl", DataUtil.dataPath + "/shader/planet.frag.glsl");

			atmosphereShader = new Shader(DataUtil.dataPath
					+ "/shader/atmosphere.vert.glsl", DataUtil.dataPath + "/shader/atmosphere.frag.glsl");

			gasPlanetSurfaceShader = new Shader(DataUtil.dataPath
					+ "/shader/gasgiant.vert.glsl", DataUtil.dataPath + "/shader/gasgiant.frag.glsl");

			gasGiantOuterAtmoShader = atmosphereShader; //TODO temp

			gasGiantInnerAtmoShader = new Shader(DataUtil.dataPath
					+ "/shader/atmosphere/gasgiant_interior.vert", DataUtil.dataPath + "/shader/atmosphere/gasgiant_interior.frag");

			bloomShader = new Shader(DataUtil.dataPath
					+ "/shader/bloom.vert.glsl", DataUtil.dataPath + "/shader/bloom2D.frag.glsl");

			sunShader = new Shader(DataUtil.dataPath
					+ "/shader/sun.vert.glsl", DataUtil.dataPath + "/shader/sun.frag.glsl");

			lavaShader = new Shader(DataUtil.dataPath
					+ "/shader/lava/lava.vert.glsl", DataUtil.dataPath + "/shader/lava/lava.frag.glsl");

			silhouetteShader = new Shader(DataUtil.dataPath
					+ "/shader/silhouette.vert.glsl", DataUtil.dataPath + "/shader/silhouette.frag.glsl");
			silhouetteShader.setShaderInterface(new SilhouetteShader()); //silhouette doesn't need parameters

			silhouetteShader2D = new Shader(DataUtil.dataPath
					+ "/shader/silhouette.vert.glsl", DataUtil.dataPath + "/shader/silhouette2DAlpha.frag.glsl");

			silhouetteAlpha = new Shader(DataUtil.dataPath
					+ "/shader/silhouette.vert.glsl", DataUtil.dataPath + "/shader/silhouetteAlpha.frag.glsl");

			//			hoffmanSkyShader = new Shader(DataUtil.dataPath
			//					+ "/shader/scatter/sky.vert.glsl", DataUtil.dataPath + "/shader/scatter/sky.frag.glsl");
			//			hoffmanTerrainShader = new Shader(DataUtil.dataPath
			//					+ "/shader/scatter/terrain.vert.glsl", DataUtil.dataPath + "/shader/scatter/terrain.frag.glsl");

			exaustShader = new Shader(DataUtil.dataPath
					+ "/shader/thruster/thruster.vert.glsl", DataUtil.dataPath + "/shader/thruster/thruster.frag.glsl");
			outlineShader = new Shader(DataUtil.dataPath
					+ "/shader/outline/outline.vert.glsl", DataUtil.dataPath + "/shader/outline/outline.frag.glsl");

			if (EngineSettings.isShadowOn()) {
//			shad_single_prog = createShaders( "../../src/cascaded_shadow_maps/shadow_vertex.glsl", "../../src/cascaded_shadow_maps/shadow_single_fragment.glsl");
//			shad_single_hl_prog = createShaders( "../../src/cascaded_shadow_maps/shadow_vertex.glsl", "../../src/cascaded_shadow_maps/shadow_single_hl_fragment.glsl");
//			shad_multi_prog = createShaders( "../../src/cascaded_shadow_maps/shadow_vertex.glsl", "../../src/cascaded_shadow_maps/shadow_multi_leak_fragment.glsl");
//			shad_multi_noleak_prog = createShaders( "../../src/cascaded_shadow_maps/shadow_vertex.glsl", "../../src/cascaded_shadow_maps/shadow_multi_noleak_fragment.glsl");
//		    shad_pcf_prog = createShaders( "../../src/cascaded_shadow_maps/shadow_vertex.glsl", "../../src/cascaded_shadow_maps/shadow_pcf_fragment.glsl");
//		    shad_pcf_trilin_prog = createShaders( "../../src/cascaded_shadow_maps/shadow_vertex.glsl", "../../src/cascaded_shadow_maps/shadow_pcf_trilinear_fragment.glsl");
//		    shad_pcf_4tap_prog = createShaders( "../../src/cascaded_shadow_maps/shadow_vertex.glsl", "../../src/cascaded_shadow_maps/shadow_pcf_4tap_fragment.glsl");
//		    shad_pcf_8tap_prog = createShaders( "../../src/cascaded_shadow_maps/shadow_vertex.glsl", "../../src/cascaded_shadow_maps/shadow_pcf_8tap_random_fragment.glsl");
//		    shad_pcf_gaussian_prog = createShaders( "../../src/cascaded_shadow_maps/shadow_vertex.glsl", "../../src/cascaded_shadow_maps/shadow_pcf_gaussian_fragment.glsl");

//				shad_single_prog = new Shader(DataUtil.dataPath
//						+ "/shader/shadow/shadow_vertex.glsl", DataUtil.dataPath + "/shader/shadow/shadow_single_fragment.glsl");;
//				shad_single_hl_prog = new Shader(DataUtil.dataPath
//						+ "/shader/shadow/shadow_vertex.glsl", DataUtil.dataPath + "/shader/shadow/shadow_single_hl_fragment.glsl");
//				shad_multi_prog = new Shader(DataUtil.dataPath
//						+ "/shader/shadow/shadow_vertex.glsl", DataUtil.dataPath + "/shader/shadow/shadow_multi_leak_fragment.glsl");
//				shad_multi_noleak_prog = new Shader(DataUtil.dataPath
//						+ "/shader/shadow/shadow_vertex.glsl", DataUtil.dataPath + "/shader/shadow/shadow_multi_noleak_fragment.glsl");
//				shad_pcf_prog = new Shader(DataUtil.dataPath
//						+ "/shader/shadow/shadow_vertex.glsl", DataUtil.dataPath + "/shader/shadow/shadow_pcf_fragment.glsl");
//				shad_pcf_trilin_prog = new Shader(DataUtil.dataPath
//						+ "/shader/shadow/shadow_vertex.glsl", DataUtil.dataPath + "/shader/shadow/shadow_pcf_trilinear_fragment.glsl");
//				shad_pcf_4tap_prog = new Shader(DataUtil.dataPath
//						+ "/shader/shadow/shadow_vertex.glsl", DataUtil.dataPath + "/shader/shadow/shadow_pcf_4tap_fragment.glsl");
//				shad_pcf_8tap_prog = new Shader(DataUtil.dataPath
//						+ "/shader/shadow/shadow_vertex.glsl", DataUtil.dataPath + "/shader/shadow/shadow_pcf_8tap_random_fragment.glsl");
//				shad_pcf_gaussian_prog = new Shader(DataUtil.dataPath
//						+ "/shader/shadow/shadow_vertex.glsl", DataUtil.dataPath + "/shader/shadow/shadow_pcf_gaussian_fragment.glsl");
//
				shad_view = new Shader(DataUtil.dataPath
						+ "/shader/shadow/view_vertex.glsl", DataUtil.dataPath + "/shader/shadow/view_fragment.glsl");
			}

			starShader = new Shader(
					DataUtil.dataPath + "/shader/starsExt/stars.vsh",
					DataUtil.dataPath + "/shader/starsExt/stars.fsh");
			fieldShader = new Shader(
					DataUtil.dataPath + "/shader/starsExt/field.vert.glsl",
					DataUtil.dataPath + "/shader/starsExt/field.frag.glsl");
			bgShader = new Shader(
					DataUtil.dataPath + "/shader/bg.vert.glsl",
					DataUtil.dataPath + "/shader/bg.frag.glsl");
			tunnelShader = new Shader(
					DataUtil.dataPath + "/shader/hyperspace/tunnel.vert.glsl",
					DataUtil.dataPath + "/shader/hyperspace/tunnel.frag.glsl");

			starFlareShader = new Shader(
					DataUtil.dataPath + "/shader/starsExt/starsflare.vsh",
					DataUtil.dataPath + "/shader/starsExt/starsflare.fsh");
			powerBarShader = new Shader(
					DataUtil.dataPath + "/shader/hud/powerbar/powerbar.vert.glsl",
					DataUtil.dataPath + "/shader/hud/powerbar/powerbar.frag.glsl");
			powerBarShaderHor = new Shader(
					DataUtil.dataPath + "/shader/hud/powerbar/powerbarHorizontal.vert.glsl",
					DataUtil.dataPath + "/shader/hud/powerbar/powerbarHorizontal.frag.glsl");

			
			cubeGroupShader = new Shader(
					DataUtil.dataPath + "/shader/cube/groups/cubegrp.vert.glsl",
					DataUtil.dataPath + "/shader/cube/groups/cubegrp.frag.glsl",
					new ShaderPreprocessor(
							(EngineSettings.G_ELEMENT_COLLECTION_INT_ATT.isOn() && GraphicsContext.INTEGER_VERTICES) ? "INTATT" : null,
							GraphicsContext.current.EXT_GPU_SHADER4() && EngineSettings.G_USE_SHADER4.isOn() ? "shader4" : null,
							GraphicsContext.current.forceOpenGl30() && EngineSettings.G_USE_SHADER4.isOn() ? "force130" : null
					)){
				@Override
				public void bindAttributes(int shaderProgram) {
					if(GraphicsContext.INTEGER_VERTICES){
						GL20.glBindAttribLocation(shaderProgram, ElementCollectionMesh_VERT_ATTRIB_INDEX, "ivert");
					}
				}
			};;
			
			createCubeShaders();

			colorBoxShader= new Shader(
					DataUtil.dataPath + "/shader/cube/colorbox.vert.glsl",
					DataUtil.dataPath + "/shader/cube/colorbox.frag.glsl");
			
			lodCubeShader = new Shader(
					DataUtil.dataPath + "/shader/cube/lodCube/lodcube.vert.glsl",
					DataUtil.dataPath + "/shader/cube/lodCube/lodcube.frag.glsl",
					new ShaderPreprocessor(
							EngineSettings.G_NORMAL_MAPPING.isOn() ? "normalmap" : null,
							EngineSettings.isShadowOn() && EngineSettings.G_SHADOWS_VSM.isOn() ? "VSM" : null,
							EngineSettings.isShadowOn() ? "shadow" : null,
							"owntangent",
							EngineSettings.isShadowOn() ? ((ShadowQuality)EngineSettings.G_SHADOW_QUALITY.getObject()).sName : null,
							GraphicsContext.current.EXT_GPU_SHADER4() && EngineSettings.G_USE_SHADER4.isOn() ? "shader4" : null,
							GraphicsContext.current.forceOpenGl30() && EngineSettings.G_USE_SHADER4.isOn() ? "force130" : null
					));
			lodCubeShaderShadow = new Shader(
					DataUtil.dataPath + "/shader/cube/lodCube/lodcube.vert.glsl",
					DataUtil.dataPath + "/shader/cube/lodCube/lodcube-shadow.frag.glsl",
					new ShaderPreprocessor(
						EngineSettings.isShadowOn() && EngineSettings.G_SHADOWS_VSM.isOn() ? "VSM" : null,
						EngineSettings.isShadowOn() ? "shadow" : null,
						EngineSettings.isShadowOn() ? ((ShadowQuality)EngineSettings.G_SHADOW_QUALITY.getObject()).sName : null,
						EngineSettings.G_NORMAL_MAPPING.isOn()  ? "normalmap" : null,
								"owntangent",
								GraphicsContext.current.EXT_GPU_SHADER4() && EngineSettings.G_USE_SHADER4.isOn() ? "shader4" : null,
										GraphicsContext.current.forceOpenGl30() && EngineSettings.G_USE_SHADER4.isOn() ? "force130" : null
							));
			lodCubeShaderTangent = new Shader(
					DataUtil.dataPath + "/shader/cube/lodCube/lodcube.vert.glsl",
					DataUtil.dataPath + "/shader/cube/lodCube/lodcube.frag.glsl",
					new ShaderPreprocessor(
					EngineSettings.isShadowOn() && EngineSettings.G_SHADOWS_VSM.isOn() ? "VSM" : null,
					EngineSettings.isShadowOn() ? "shadow" : null,
					EngineSettings.isShadowOn() ? ((ShadowQuality)EngineSettings.G_SHADOW_QUALITY.getObject()).sName : null,
					EngineSettings.G_NORMAL_MAPPING.isOn()  ? "normalmap" : null,
					"owntangent",
					GraphicsContext.current.EXT_GPU_SHADER4() && EngineSettings.G_USE_SHADER4.isOn() ? "shader4" : null,
							GraphicsContext.current.forceOpenGl30() && EngineSettings.G_USE_SHADER4.isOn() ? "force130" : null
							));
			lodCubeShaderNormalOff = new Shader(
					DataUtil.dataPath + "/shader/cube/lodCube/lodcube.vert.glsl",
					DataUtil.dataPath + "/shader/cube/lodCube/lodcube.frag.glsl",
					new ShaderPreprocessor(
							EngineSettings.isShadowOn() && EngineSettings.G_SHADOWS_VSM.isOn() ? "VSM" : null,
							EngineSettings.isShadowOn() ? "shadow" : null,
							EngineSettings.isShadowOn() ? ((ShadowQuality)EngineSettings.G_SHADOW_QUALITY.getObject()).sName : null,
									GraphicsContext.current.EXT_GPU_SHADER4() && EngineSettings.G_USE_SHADER4.isOn() ? "shader4" : null,
											GraphicsContext.current.forceOpenGl30() && EngineSettings.G_USE_SHADER4.isOn() ? "force130" : null
							));
			
			shardShader = new Shader(
					DataUtil.dataPath + "/shader/cube/shard/shard.vert.glsl",
					DataUtil.dataPath + "/shader/cube/shard/shard.frag.glsl",
					new ShaderPreprocessor(
							EngineSettings.isShadowOn() && EngineSettings.G_SHADOWS_VSM.isOn() ? "VSM" : null,
							CUBE_VERTEX_COMPONENTS > 2 ? "threeComp" : null,
							
							EngineSettings.isShadowOn() ? "shadow" : null,
							EngineSettings.isShadowOn() ? ((ShadowQuality)EngineSettings.G_SHADOW_QUALITY.getObject()).sName : null,
							
							(EngineSettings.G_NORMAL_MAPPING.isOn()) ? "normalmap" : null,
									GraphicsContext.current.EXT_GPU_SHADER4() && EngineSettings.G_USE_SHADER4.isOn() ? "shader4" : null,
											GraphicsContext.current.forceOpenGl30() && EngineSettings.G_USE_SHADER4.isOn() ? "force130" : null
					));

			if (EngineSettings.G_DRAW_SHIELDS.isOn()) {
				cubeShieldShader = new Shader(
						DataUtil.dataPath + "/shader/cube/shieldCube/shieldcube-3rd.vsh",
						DataUtil.dataPath + "/shader/cube/shieldCube/shieldcube.fsh",
						new ShaderPreprocessor(
								GraphicsContext.current.EXT_GPU_SHADER4() && EngineSettings.G_USE_SHADER4.isOn() ? "shader4" : null,
								GraphicsContext.INTEGER_VERTICES ? "INTATT" : null,
								GraphicsContext.current.forceOpenGl30() && EngineSettings.G_USE_SHADER4.isOn() ? "force130" : null
						)
						
						) {
					@Override
					public void bindAttributes(int shaderProgram) {
						if(GraphicsContext.INTEGER_VERTICES){
							GL20.glBindAttribLocation(shaderProgram, CUBE_SHADER_VERT_INDEX, "ivert");
						}
					}
				};
			}

			if (EngineSettings.G_DRAW_JUMP_OVERLAY.isOn()) {
				jumpOverlayShader = new Shader(
						DataUtil.dataPath + "/shader/cube/jumpCube/jumpcube.vsh",
						DataUtil.dataPath + "/shader/cube/jumpCube/jumpcube.fsh",
						new ShaderPreprocessor(
								GraphicsContext.current.EXT_GPU_SHADER4() && EngineSettings.G_USE_SHADER4.isOn() ? "shader4" : null,
								GraphicsContext.INTEGER_VERTICES ? "INTATT" : null,
								GraphicsContext.current.forceOpenGl30() && EngineSettings.G_USE_SHADER4.isOn() ? "force130" : null
										
								)
						
						) {
					@Override
					public void bindAttributes(int shaderProgram) {
						if(GraphicsContext.INTEGER_VERTICES){
							GL20.glBindAttribLocation(shaderProgram, CUBE_SHADER_VERT_INDEX, "ivert");
						}
					}
				};

			}

			cubemapShader = new Shader(DataUtil.dataPath
					+ "/shader/cubemap.vsh", DataUtil.dataPath + "/shader/cubemap.fsh");
//			addBoneShader(28, false);
//			addBoneShader(29, false);
//			addBoneShader(14, false);

			bloomShaderPass1 = new Shader(DataUtil.dataPath
					+ "/shader/bloom/bloom.vert.glsl", DataUtil.dataPath + "/shader/bloom/bloom1.frag.glsl");
			bloomShaderPass2 = new Shader(DataUtil.dataPath
					+ "/shader/bloom/bloom.vert.glsl", DataUtil.dataPath + "/shader/bloom/bloom2.frag.glsl");
			bloomShaderPass3 = new Shader(DataUtil.dataPath
					+ "/shader/bloom/bloom.vert.glsl", DataUtil.dataPath + "/shader/bloom/bloom3.frag.glsl");
			bloomShaderPass4 = new Shader(DataUtil.dataPath
					+ "/shader/bloom/bloom.vert.glsl", DataUtil.dataPath + "/shader/bloom/bloom4.frag.glsl");
			
			
			bloomShader1Vert = new Shader(DataUtil.dataPath
					+ "/shader/pointBloom/default.vert.glsl", DataUtil.dataPath + "/shader/pointBloom/blur1Vert.frag.glsl");
			bloomShader2Hor = new Shader(DataUtil.dataPath
					+ "/shader/pointBloom/default.vert.glsl", DataUtil.dataPath + "/shader/pointBloom/blur2Hor.frag.glsl");
			downsampler = new Shader(DataUtil.dataPath
					+ "/shader/pointBloom/default.vert.glsl", DataUtil.dataPath + "/shader/pointBloom/downsampler.frag.glsl",
					new ShaderPreprocessor( "nothing"));
			
			downsamplerFirst = new Shader(DataUtil.dataPath
					+ "/shader/pointBloom/default.vert.glsl", DataUtil.dataPath + "/shader/pointBloom/downsampler.frag.glsl",
					new ShaderPreprocessor( "withDepth"));

			
			depthCubeShader = new Shader(
					DataUtil.dataPath + "/shader/cube/quads13/depthcube.vsh",
					DataUtil.dataPath + "/shader/cube/quads13/depthcube.fsh", new ShaderPreprocessor(
					EngineSettings.isShadowOn() && EngineSettings.G_SHADOWS_VSM.isOn() ? "VSM" : null,
							
					EngineSettings.G_USE_VERTEX_LIGHTING_ONLY.isOn() ? "vertexLighting" : null,
							"owntangent",
							GraphicsContext.INTEGER_VERTICES ? "INTATT" : null,
							GraphicsContext.current.EXT_GPU_SHADER4() && EngineSettings.G_USE_SHADER4.isOn() ? "shader4" : null,
							GraphicsContext.current.forceOpenGl30() && EngineSettings.G_USE_SHADER4.isOn() ? "force130" : null,		
							GraphicsContext.current.isIntel() && GraphicsContext.current.forceOpenGl30() && EngineSettings.G_USE_SHADER4.isOn() ? "intel130" : null		
							)){
				@Override
				public void bindAttributes(int shaderProgram) {
					if(GraphicsContext.INTEGER_VERTICES){
						GL20.glBindAttribLocation(shaderProgram, CUBE_SHADER_VERT_INDEX, "ivert");
					}
				}
			};
			lodCubeShaderD4 = new Shader(
					DataUtil.dataPath + "/shader/cube/quads13/LOD4/lod4.vert.glsl",
					DataUtil.dataPath + "/shader/cube/quads13/LOD4/lod4.frag.glsl", new ShaderPreprocessor(
					EngineSettings.isShadowOn() && EngineSettings.G_SHADOWS_VSM.isOn() ? "VSM" : null,
							
					EngineSettings.G_USE_VERTEX_LIGHTING_ONLY.isOn() ? "vertexLighting" : null,
							"owntangent",
							
							"RES_4",
							GraphicsContext.INTEGER_VERTICES ? "INTATT" : null,
							GraphicsContext.current.EXT_GPU_SHADER4() && EngineSettings.G_USE_SHADER4.isOn() ? "shader4" : null,
							GraphicsContext.current.forceOpenGl30() && EngineSettings.G_USE_SHADER4.isOn() ? "force130" : null,		
							GraphicsContext.current.isIntel() && GraphicsContext.current.forceOpenGl30() && EngineSettings.G_USE_SHADER4.isOn() ? "intel130" : null		
							)){
				@Override
				public void bindAttributes(int shaderProgram) {
					if(GraphicsContext.INTEGER_VERTICES){
						GL20.glBindAttribLocation(shaderProgram, CUBE_SHADER_VERT_INDEX, "ivert");
					}
				}
			};
			lodCubeShaderD8 = new Shader(
					DataUtil.dataPath + "/shader/cube/quads13/LOD4/lod4.vert.glsl",
					DataUtil.dataPath + "/shader/cube/quads13/LOD4/lod4.frag.glsl", new ShaderPreprocessor(
							EngineSettings.isShadowOn() && EngineSettings.G_SHADOWS_VSM.isOn() ? "VSM" : null,
									
							EngineSettings.G_USE_VERTEX_LIGHTING_ONLY.isOn() ? "vertexLighting" : null,
											"owntangent",
											
							GraphicsContext.INTEGER_VERTICES ? "INTATT" : null,
							GraphicsContext.current.EXT_GPU_SHADER4() && EngineSettings.G_USE_SHADER4.isOn() ? "shader4" : null,
							GraphicsContext.current.forceOpenGl30() && EngineSettings.G_USE_SHADER4.isOn() ? "force130" : null,		
							GraphicsContext.current.isIntel() && GraphicsContext.current.forceOpenGl30() && EngineSettings.G_USE_SHADER4.isOn() ? "intel130" : null		
							)){
				@Override
				public void bindAttributes(int shaderProgram) {
					if(GraphicsContext.INTEGER_VERTICES){
						GL20.glBindAttribLocation(shaderProgram, CUBE_SHADER_VERT_INDEX, "ivert");
					}
				}
			};
			GlUtil.printGlErrorCritical();

			
			shaders.add(lodCubeShaderD4);
			shaders.add(lodCubeShaderD8);
			shaders.add(lodSimpleMeshCubeShader);
			shaders.add(depthCubeShader);
			
			//			shaders.add(oceanAtmosphereShader);
			shaders.add(blackHoleShader);
			shaders.add(tubesShader);
			shaders.add(tubesStreamShader);
			shaders.add(pulseShader);

			if (EngineSettings.G_ATMOSPHERE_SHADER.getObject() != AtmosphereShaderSetting.NONE) {
				shaders.add(skyFromSpace);
				shaders.add(skyFromAtmo);
			}

			shaders.add(downsampler);
			shaders.add(downsamplerFirst);
			shaders.add(perpixelShader);
//			shaders.add(nebulaShader);
			shaders.add(projectileTrailShader);
			shaders.add(godRayShader);
			shaders.add(plasmaShader);
			shaders.add(silhouetteAlpha);
			
			shaders.add(bloomShader1Vert);
			shaders.add(bloomShader2Hor);

			shaders.add(shieldBubbleShader);
			
			
			shaders.add(silhouetteAlpha);

			shaders.add(fieldShader);
			shaders.add(simpleBeamShader);
			shaders.add(transporterShader);
			shaders.add(selectionShader);
			shaders.add(beamBoxShader);
			//			shaders.add(shieldShader);
			shaders.add(exaustShader);
			shaders.add(starFlareShader);
			shaders.add(cubeShieldShader);
			shaders.add(bgShader);
			shaders.add(solidSelectionShader);
			shaders.add(outlineShader);
			shaders.add(graphConnectionShader);

			shaders.add(lodCubeShader);
			shaders.add(lodCubeShaderTangent);
			shaders.add(lodCubeShaderNormalOff);
			shaders.add(lodCubeShaderShadow);
			shaders.add(colorBoxShader);
			
			shaders.add(cubeGroupShader);
			shaders.add(mineShader);

			//			shaders.add(oceanCloundsShader);
			//			shaders.add(oceanFFTXShader);
			//			shaders.add(oceanFFTYShader);
			//			shaders.add(oceanInitShader);
			//			shaders.add(oceanOceanShader);
			//			shaders.add(oceanSkyShader);
			//			shaders.add(oceanSkyMapShader);
			//			shaders.add(oceanVariancesShader);

			//			shaders.add(hoffmanTerrainShader);
			//			shaders.add(hoffmanSkyShader);
			shaders.add(silhouetteShader);
//			shaders.add(smokeShader);
			//			shaders.add(terrainShader);
			//			shaders.add(waterShader);
			//			shaders.add(skyShader2);
			shaders.add(bumpShader);
			shaders.add(scanlineShader);
//			shaders.add(volumeSmokeShader);
			shaders.add(planetShader);
			shaders.add(atmosphereShader);
			shaders.add(bloomShader);
			shaders.add(sunShader);
			shaders.add(tunnelShader);
			shaders.add(spacedustShader);
			shaders.add(ocDistortion);

			shaders.add(lavaShader);
			shaders.add(projectileShader);
			shaders.add(guiTextureWrapperShader);
			shaders.add(projectileQuadShader);
			shaders.add(projectileQuadBloomShader);
			shaders.add(projectileBeamQuadShader);
			shaders.add(simpleColorShader);
			shaders.add(explosionShader);
			shaders.add(starShader);
			shaders.add(cubemapShader);
			shaders.add(bloomShaderPass1);
			shaders.add(bloomShaderPass2);
			shaders.add(bloomShaderPass3);
			shaders.add(bloomShaderPass4);
			shaders.add(bloomSimpleShader);
			shaders.add(shardShader);
			shaders.add(jumpOverlayShader);
			shaders.add(cubeShader13Simple);
			shaders.add(cubeShader13SimpleWhite);
			shaders.add(powerBarShader);
			shaders.add(powerBarShaderHor);
			shaders.add(depthCubeShader);

			if (EngineSettings.isShadowOn()) {

				shaders.add(shadowShaderCubes);
				shaders.add(shadowShaderCubesBlend);
//				shaders.add(shad_single_prog);
//				shaders.add(shad_single_hl_prog);
//				shaders.add(shad_multi_prog);
//				shaders.add(shad_multi_noleak_prog);
//				shaders.add(shad_pcf_prog);
//				shaders.add(shad_pcf_trilin_prog);
//				shaders.add(shad_pcf_4tap_prog);
//				shaders.add(shad_pcf_8tap_prog);
//				shaders.add(shad_pcf_gaussian_prog);
				shaders.add(shad_view);
			}
			//			shaders.add(cubeShader);
//			shaders.add(cubeShader13);

			for (Shader s : cubeShaders.values()) {
				shaders.add(s);
			}
//			shaders.add(cubeShader13Passthrough);
//			shaders.add(cubeShader13Blended);
//			shaders.add(cubeShader13VertexLight);
//			shaders.add(cubeShader13VertexLightBlend);
//			shaders.add(cubeShader13LightAll);
//			shaders.add(cubeShader13LightAllBlended);
//			shaders.add(cubeShader13Single);
		} catch (ResourceException e) {
			e.printStackTrace();
		}
	}

	private static void createCubeShaders() throws ResourceException {
		
		if (EngineSettings.isShadowOn()) {

			shadowShaderCubes = new Shader(
					DataUtil.dataPath + "/shader/cube/quads13/shadowcube.vsh",
					DataUtil.dataPath + "/shader/cube/quads13/shadowcube.fsh", new ShaderPreprocessor(
					EngineSettings.isShadowOn() && EngineSettings.G_SHADOWS_VSM.isOn() ? "VSM" : null,
							
					EngineSettings.G_USE_VERTEX_LIGHTING_ONLY.isOn() ? "vertexLighting" : null,
							"owntangent",
							GraphicsContext.INTEGER_VERTICES ? "INTATT" : null,
							GraphicsContext.current.EXT_GPU_SHADER4() && EngineSettings.G_USE_SHADER4.isOn() ? "shader4" : null,
							GraphicsContext.current.forceOpenGl30() && EngineSettings.G_USE_SHADER4.isOn() ? "force130" : null,
							GraphicsContext.current.isIntel() && GraphicsContext.current.forceOpenGl30() && EngineSettings.G_USE_SHADER4.isOn() ? "intel130" : null
							)){
				@Override
				public void bindAttributes(int shaderProgram) {
					if(GraphicsContext.INTEGER_VERTICES){
						GL20.glBindAttribLocation(shaderProgram, CUBE_SHADER_VERT_INDEX, "ivert");
					}
				}
			};
			shadowShaderCubesBlend = new Shader(
					DataUtil.dataPath + "/shader/cube/quads13/shadowcube.vsh",
					DataUtil.dataPath + "/shader/cube/quads13/shadowcube.fsh", new ShaderPreprocessor(
					EngineSettings.isShadowOn() && EngineSettings.G_SHADOWS_VSM.isOn() ? "VSM" : null,
					 "blended",
					GraphicsContext.INTEGER_VERTICES ? "INTATT" : null,
							"owntangent",
					EngineSettings.G_USE_VERTEX_LIGHTING_ONLY.isOn() ? "vertexLighting" : null,
					GraphicsContext.current.EXT_GPU_SHADER4() && EngineSettings.G_USE_SHADER4.isOn() ? "shader4" : null,
					GraphicsContext.current.forceOpenGl30() && EngineSettings.G_USE_SHADER4.isOn() ? "force130" : null,
					GraphicsContext.current.isIntel() && GraphicsContext.current.forceOpenGl30() && EngineSettings.G_USE_SHADER4.isOn() ? "intel130" : null
							)){
					
				@Override
				public void bindAttributes(int shaderProgram) {
					if(GraphicsContext.INTEGER_VERTICES){
						GL20.glBindAttribLocation(shaderProgram, CUBE_SHADER_VERT_INDEX, "ivert");
					}
				}
			};
		}
		cubeShader13Simple = new Shader(
				DataUtil.dataPath + "/shader/cube/quads13/simplecube.vsh",
				DataUtil.dataPath + "/shader/cube/quads13/simplecube.fsh",
				new ShaderPreprocessor(
						EngineSettings.isShadowOn() && EngineSettings.G_SHADOWS_VSM.isOn() ? "VSM" : null,
						CUBE_VERTEX_COMPONENTS > 2 ? "threeComp" : null,
								
						EngineSettings.isShadowOn() ? "shadow" : null,
								"owntangent",
						EngineSettings.G_USE_VERTEX_LIGHTING_ONLY.isOn() ? "vertexLighting" : null,
						EngineSettings.isShadowOn() ? ((ShadowQuality)EngineSettings.G_SHADOW_QUALITY.getObject()).sName : null,
						GraphicsContext.INTEGER_VERTICES ? "INTATT" : null,
						
						(EngineSettings.G_NORMAL_MAPPING.isOn() ) ? "normalmap" : null,
							GraphicsContext.current.EXT_GPU_SHADER4() && EngineSettings.G_USE_SHADER4.isOn() ? "shader4" : null,
						GraphicsContext.current.forceOpenGl30() && EngineSettings.G_USE_SHADER4.isOn() ? "force130" : null,
						GraphicsContext.current.isIntel() && GraphicsContext.current.forceOpenGl30() && EngineSettings.G_USE_SHADER4.isOn() ? "intel130" : null
				)){
			@Override
			public void bindAttributes(int shaderProgram) {
				if(GraphicsContext.INTEGER_VERTICES){
					GL20.glBindAttribLocation(shaderProgram, CUBE_SHADER_VERT_INDEX, "ivert");
				}
			}
		};
		cubeShader13SimpleWhite = new Shader(
				DataUtil.dataPath + "/shader/cube/quads13/shadowcube.vsh",
				DataUtil.dataPath + "/shader/cube/quads13/whitecube.fsh",
				new ShaderPreprocessor(
						EngineSettings.isShadowOn() && EngineSettings.G_SHADOWS_VSM.isOn() ? "VSM" : null,
						CUBE_VERTEX_COMPONENTS > 2 ? "threeComp" : null,
								"owntangent",
						
						EngineSettings.isShadowOn() ? "shadow" : null,
						EngineSettings.G_USE_VERTEX_LIGHTING_ONLY.isOn() ? "vertexLighting" : null,
						EngineSettings.isShadowOn() ? ((ShadowQuality)EngineSettings.G_SHADOW_QUALITY.getObject()).sName : null,
						GraphicsContext.INTEGER_VERTICES ? "INTATT" : null,
						
						(EngineSettings.G_NORMAL_MAPPING.isOn() ) ? "normalmap" : null,
						GraphicsContext.current.EXT_GPU_SHADER4() && EngineSettings.G_USE_SHADER4.isOn() ? "shader4" : null,
						GraphicsContext.current.forceOpenGl30() && EngineSettings.G_USE_SHADER4.isOn() ? "force130" : null,
						GraphicsContext.current.isIntel() && GraphicsContext.current.forceOpenGl30() && EngineSettings.G_USE_SHADER4.isOn() ? "intel130" : null
						)){
			@Override
			public void bindAttributes(int shaderProgram) {
				if(GraphicsContext.INTEGER_VERTICES){
					GL20.glBindAttribLocation(shaderProgram, CUBE_SHADER_VERT_INDEX, "ivert");
				}
			}
		};

		createCubeShader();
		createCubeShader(CubeShaderType.BLENDED);
		
		createCubeShader(CubeShaderType.NO_SPOT_LIGHTS);
		createCubeShader(CubeShaderType.NO_SPOT_LIGHTS, CubeShaderType.BLENDED);

		createCubeShader(CubeShaderType.VERTEX_LIGHTING);
		createCubeShader(CubeShaderType.VERTEX_LIGHTING, CubeShaderType.BLENDED);

		createCubeShader(CubeShaderType.LIGHT_ALL);
		createCubeShader(CubeShaderType.LIGHT_ALL, CubeShaderType.BLENDED);
		
		createCubeShader(CubeShaderType.LIGHT_ALL, CubeShaderType.VERTEX_LIGHTING);
		createCubeShader(CubeShaderType.LIGHT_ALL, CubeShaderType.VERTEX_LIGHTING, CubeShaderType.BLENDED);

		createCubeShader(CubeShaderType.NO_SPOT_LIGHTS, CubeShaderType.LIGHT_ALL);
		createCubeShader(CubeShaderType.NO_SPOT_LIGHTS, CubeShaderType.LIGHT_ALL, CubeShaderType.BLENDED);
		
		
		createCubeShader(CubeShaderType.VIRTUAL);
		createCubeShader(CubeShaderType.BLENDED, CubeShaderType.VIRTUAL);
		
		createCubeShader(CubeShaderType.NO_SPOT_LIGHTS, CubeShaderType.VIRTUAL);
		createCubeShader(CubeShaderType.NO_SPOT_LIGHTS, CubeShaderType.BLENDED, CubeShaderType.VIRTUAL);

		createCubeShader(CubeShaderType.VERTEX_LIGHTING, CubeShaderType.VIRTUAL);
		createCubeShader(CubeShaderType.VERTEX_LIGHTING, CubeShaderType.BLENDED, CubeShaderType.VIRTUAL);

		createCubeShader(CubeShaderType.LIGHT_ALL, CubeShaderType.VIRTUAL);
		createCubeShader(CubeShaderType.LIGHT_ALL, CubeShaderType.BLENDED, CubeShaderType.VIRTUAL);
		
		createCubeShader(CubeShaderType.LIGHT_ALL, CubeShaderType.VERTEX_LIGHTING, CubeShaderType.VIRTUAL);
		createCubeShader(CubeShaderType.LIGHT_ALL, CubeShaderType.VERTEX_LIGHTING, CubeShaderType.BLENDED, CubeShaderType.VIRTUAL);

		createCubeShader(CubeShaderType.NO_SPOT_LIGHTS, CubeShaderType.LIGHT_ALL, CubeShaderType.VIRTUAL);
		createCubeShader(CubeShaderType.NO_SPOT_LIGHTS, CubeShaderType.LIGHT_ALL, CubeShaderType.BLENDED, CubeShaderType.VIRTUAL);

		

		createCubeShader(CubeShaderType.SINGLE_DRAW, CubeShaderType.BLENDED, CubeShaderType.LIGHT_ALL);
		createCubeShader(CubeShaderType.SINGLE_DRAW, CubeShaderType.BLENDED);


	}

	public static Shader getCubeShader(int code) {
		Shader shader = cubeShaders.get(code);
		
		if (shader == null) {
			String s = "";
			for (CubeShaderType a : CubeShaderType.values()) {
				if ((code & a.bit) == a.bit) {
					s += a.name() + ":" + a.shaderVar;
				}
			}

			throw new IllegalArgumentException("CubeShader not found: " + s);
		}
		shader.optionBits = code;
		return shader;
	}

	private static void createCubeShader(CubeShaderType... variables) throws ResourceException {
		String[] config = new String[13 + variables.length];

		int i = 0;
		config[i++] = EngineSettings.isShadowOn() && EngineSettings.G_SHADOWS_VSM.isOn() ? "VSM" : null;
		config[i++] = CUBE_VERTEX_COMPONENTS > 2 ? "threeComp" : null;
		config[i++] = EngineSettings.isShadowOn() ? "shadow" : null;
		config[i++] = "owntangent";
		config[i++] = EngineSettings.G_USE_VERTEX_LIGHTING_ONLY.isOn() ? "vertexLighting" : null;
		config[i++] = EngineSettings.isShadowOn() ? ((ShadowQuality)EngineSettings.G_SHADOW_QUALITY.getObject()).sName : null;
		config[i++] = (EngineSettings.G_NORMAL_MAPPING.isOn() ) ? "normalmap" : null;
		config[i++] = GraphicsContext.current.EXT_GPU_SHADER4() && EngineSettings.G_USE_SHADER4.isOn() ? "shader4" : null;
		config[i++] = GraphicsContext.current.forceOpenGl30() && EngineSettings.G_USE_SHADER4.isOn() ? "force130" : null;
		config[i++] = ByteUtil.Chunk32 ? null : "chunk16";
		config[i++] = GraphicsContext.INTEGER_VERTICES ? "INTATT" : null;
		config[i++] = USE_CUBE_TEXTURE_EMISSION ? null : "noemission";
		config[i++] = GraphicsContext.current.isIntel() && GraphicsContext.current.forceOpenGl30() && EngineSettings.G_USE_SHADER4.isOn() ? "intel130" : null;

		int id = 0;
		for (int j = 0; j < variables.length; j++) {
			config[i + j] = variables[j].shaderVar;
			id |= variables[j].bit;
		}
		Shader shader = new Shader(
				DataUtil.dataPath + "/shader/cube/quads13/cube-3rd.vsh",
				DataUtil.dataPath + "/shader/cube/quads13/cubeTArray.fsh",
				new ShaderPreprocessor(config)){
			@Override
			public void bindAttributes(int shaderProgram) {
				if(GraphicsContext.INTEGER_VERTICES){
					GL20.glBindAttribLocation(shaderProgram, CUBE_SHADER_VERT_INDEX, "ivert");
				}
			}
		};

		cubeShaders.put(id, shader);
	}
	public static final int CUBE_SHADER_VERT_INDEX = 0;
	public static final int ElementCollectionMesh_VERT_ATTRIB_INDEX = 0;
	public static Shader getBoneShader(int numBones, boolean spot) throws ResourceException {
		Shader s = spot ? skinningShaderSpot.get(numBones) : skinningShader.get(numBones);
		if (s == null) {
			s = addBoneShader(numBones, spot);
			s.recompiled = true;
		}
		return s;
	}

	private static Shader addBoneShader(int numBones, boolean spot) throws ResourceException {

		Shader sSpot = new Shader(DataUtil.dataPath
				+ "/shader/skin/skin-tex.vert.glsl", DataUtil.dataPath
				+ "/shader/skin/skin-tex.frag.glsl", new ShaderReplaceDynamic("#NUM_BONES", String.valueOf(numBones)),
				new ShaderPreprocessor(EngineSettings.isShadowOn() ? "shadow" : null,
						EngineSettings.isShadowOn() ? ((ShadowQuality)EngineSettings.G_SHADOW_QUALITY.getObject()).sName : null,
						EngineSettings.isShadowOn() && EngineSettings.G_SHADOWS_VSM.isOn() ? "VSM" : null)) {
			@Override
			public void bindAttributes(int shaderProgram) {
				GL20.glBindAttribLocation(shaderProgram, 3, "indices");
				GL20.glBindAttribLocation(shaderProgram, 4, "weights");
			}
		};

		skinningShader.put(numBones, sSpot);

		Shader s = new Shader(DataUtil.dataPath
				+ "/shader/skin/skin-tex.vert.glsl", DataUtil.dataPath
				+ "/shader/skin/skin-tex.frag.glsl", new ShaderReplaceDynamic("#NUM_BONES", String.valueOf(numBones)),
				new ShaderPreprocessor(EngineSettings.isShadowOn() ? "shadow" : null,
						EngineSettings.isShadowOn() ? ((ShadowQuality)EngineSettings.G_SHADOW_QUALITY.getObject()).sName : null,
						EngineSettings.isShadowOn() && EngineSettings.G_SHADOWS_VSM.isOn() ? "VSM" : null,
						"nospotlight")) {
			@Override
			public void bindAttributes(int shaderProgram) {
				GL20.glBindAttribLocation(shaderProgram, 3, "indices");
				GL20.glBindAttribLocation(shaderProgram, 4, "weights");
			}
		};

		skinningShaderSpot.put(numBones, sSpot);
		skinningShader.put(numBones, s);

		shaders.add(s);
		shaders.add(sSpot);

		return spot ? sSpot : s;
	}

	/**
	 * Re compile.
	 *
	 * @param gl  the gl
	 * @param glu the glu
	 * @ the error diolog exception
	 */
	public static void reCompile(boolean forceReload) {
		GlUtil.printGlErrorCritical();
		if (forceReload) {
			for (Shader s : shaders) {
				if (s != null) {
					s.cleanUp();
				}
			}
			skinningShader.clear();
			skinningShaderSpot.clear();
			shaders.clear();
			loadShaders();
		}

		for (Shader s : shaders) {
			try {
				if (s != null) {
					s.reset();
					s.compile();
				}
			} catch (ResourceException e) {
				e.printStackTrace();
			}
		}

		GlUtil.printGlErrorCritical();
	}

	public enum CubeShaderType {
		BLENDED(1, "blended"),
		VERTEX_LIGHTING(2, "vertexLighting"),
		LIGHT_ALL(4, "lightall"),
		SINGLE_DRAW(8, "singledraw"),
		NO_SPOT_LIGHTS(16, "nospotlights"),
		VIRTUAL(32, "virtual"),;

		public final int bit;
		private final String shaderVar;

		private CubeShaderType(int b, String shaderVar) {
			this.bit = b;
			this.shaderVar = shaderVar;
		}

	}

	public static ObjectCollection<Shader> getCubeShaders() {
		return cubeShaders.values();
	}

	public static void createSpaceDustShader() throws ResourceException {
		if(spacedustShader != null) {
			spacedustShader = new Shader(DataUtil.dataPath
					+ "/shader/spacedustExt/spacedust.vert.glsl", DataUtil.dataPath + "/shader/spacedustExt/spacedust.frag.glsl");
		}
	}

}
