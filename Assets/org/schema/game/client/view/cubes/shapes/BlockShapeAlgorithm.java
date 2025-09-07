package org.schema.game.client.view.cubes.shapes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.MemoryManager.MemFloatArray;
import org.schema.common.util.MemoryManager.MemIntArray;
import org.schema.common.util.StringTools;
import org.schema.common.util.data.DataUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.cubes.CubeBufferFloat;
import org.schema.game.client.view.cubes.CubeBufferInt;
import org.schema.game.client.view.cubes.CubeInfo;
import org.schema.game.client.view.cubes.CubeMeshBufferContainer;
import org.schema.game.client.view.cubes.occlusion.Occlusion;
import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;
import org.schema.game.client.view.cubes.shapes.orientcube.back.OriencubeBackBottom;
import org.schema.game.client.view.cubes.shapes.orientcube.back.OriencubeBackLeft;
import org.schema.game.client.view.cubes.shapes.orientcube.back.OriencubeBackRight;
import org.schema.game.client.view.cubes.shapes.orientcube.back.OriencubeBackTop;
import org.schema.game.client.view.cubes.shapes.orientcube.bottom.OriencubeBottomBack;
import org.schema.game.client.view.cubes.shapes.orientcube.bottom.OriencubeBottomFront;
import org.schema.game.client.view.cubes.shapes.orientcube.bottom.OriencubeBottomLeft;
import org.schema.game.client.view.cubes.shapes.orientcube.bottom.OriencubeBottomRight;
import org.schema.game.client.view.cubes.shapes.orientcube.front.OriencubeFrontBottom;
import org.schema.game.client.view.cubes.shapes.orientcube.front.OriencubeFrontLeft;
import org.schema.game.client.view.cubes.shapes.orientcube.front.OriencubeFrontRight;
import org.schema.game.client.view.cubes.shapes.orientcube.front.OriencubeFrontTop;
import org.schema.game.client.view.cubes.shapes.orientcube.left.OriencubeLeftBack;
import org.schema.game.client.view.cubes.shapes.orientcube.left.OriencubeLeftBottom;
import org.schema.game.client.view.cubes.shapes.orientcube.left.OriencubeLeftFront;
import org.schema.game.client.view.cubes.shapes.orientcube.left.OriencubeLeftTop;
import org.schema.game.client.view.cubes.shapes.orientcube.right.OriencubeRightBack;
import org.schema.game.client.view.cubes.shapes.orientcube.right.OriencubeRightBottom;
import org.schema.game.client.view.cubes.shapes.orientcube.right.OriencubeRightFront;
import org.schema.game.client.view.cubes.shapes.orientcube.right.OriencubeRightTop;
import org.schema.game.client.view.cubes.shapes.orientcube.top.OriencubeTopBack;
import org.schema.game.client.view.cubes.shapes.orientcube.top.OriencubeTopFront;
import org.schema.game.client.view.cubes.shapes.orientcube.top.OriencubeTopLeft;
import org.schema.game.client.view.cubes.shapes.orientcube.top.OriencubeTopRight;
import org.schema.game.client.view.cubes.shapes.pentahedron.topbottom.PentaBottomBackLeft;
import org.schema.game.client.view.cubes.shapes.pentahedron.topbottom.PentaBottomBackRight;
import org.schema.game.client.view.cubes.shapes.pentahedron.topbottom.PentaBottomFrontLeft;
import org.schema.game.client.view.cubes.shapes.pentahedron.topbottom.PentaBottomFrontRight;
import org.schema.game.client.view.cubes.shapes.pentahedron.topbottom.PentaTopBackLeft;
import org.schema.game.client.view.cubes.shapes.pentahedron.topbottom.PentaTopBackRight;
import org.schema.game.client.view.cubes.shapes.pentahedron.topbottom.PentaTopFrontLeft;
import org.schema.game.client.view.cubes.shapes.pentahedron.topbottom.PentaTopFrontRight;
import org.schema.game.client.view.cubes.shapes.spike.SpikeIcon;
import org.schema.game.client.view.cubes.shapes.spike.frontback.SpikeBackBackLeft;
import org.schema.game.client.view.cubes.shapes.spike.frontback.SpikeBackBackRight;
import org.schema.game.client.view.cubes.shapes.spike.frontback.SpikeBackFrontLeft;
import org.schema.game.client.view.cubes.shapes.spike.frontback.SpikeBackFrontRight;
import org.schema.game.client.view.cubes.shapes.spike.frontback.SpikeFrontBackLeft;
import org.schema.game.client.view.cubes.shapes.spike.frontback.SpikeFrontBackRight;
import org.schema.game.client.view.cubes.shapes.spike.frontback.SpikeFrontFrontLeft;
import org.schema.game.client.view.cubes.shapes.spike.frontback.SpikeFrontFrontRight;
import org.schema.game.client.view.cubes.shapes.spike.sideways.SpikeLeftBackLeft;
import org.schema.game.client.view.cubes.shapes.spike.sideways.SpikeLeftBackRight;
import org.schema.game.client.view.cubes.shapes.spike.sideways.SpikeLeftFrontLeft;
import org.schema.game.client.view.cubes.shapes.spike.sideways.SpikeLeftFrontRight;
import org.schema.game.client.view.cubes.shapes.spike.sideways.SpikeRightBackLeft;
import org.schema.game.client.view.cubes.shapes.spike.sideways.SpikeRightBackRight;
import org.schema.game.client.view.cubes.shapes.spike.sideways.SpikeRightFrontLeft;
import org.schema.game.client.view.cubes.shapes.spike.sideways.SpikeRightFrontRight;
import org.schema.game.client.view.cubes.shapes.spike.topbottom.SpikeBottomBackLeft;
import org.schema.game.client.view.cubes.shapes.spike.topbottom.SpikeBottomBackRight;
import org.schema.game.client.view.cubes.shapes.spike.topbottom.SpikeBottomFrontLeft;
import org.schema.game.client.view.cubes.shapes.spike.topbottom.SpikeBottomFrontRight;
import org.schema.game.client.view.cubes.shapes.spike.topbottom.SpikeTopBackLeft;
import org.schema.game.client.view.cubes.shapes.spike.topbottom.SpikeTopBackRight;
import org.schema.game.client.view.cubes.shapes.spike.topbottom.SpikeTopFrontLeft;
import org.schema.game.client.view.cubes.shapes.spike.topbottom.SpikeTopFrontRight;
import org.schema.game.client.view.cubes.shapes.sprite.SpriteBack;
import org.schema.game.client.view.cubes.shapes.sprite.SpriteBottom;
import org.schema.game.client.view.cubes.shapes.sprite.SpriteFront;
import org.schema.game.client.view.cubes.shapes.sprite.SpriteLeft;
import org.schema.game.client.view.cubes.shapes.sprite.SpriteRight;
import org.schema.game.client.view.cubes.shapes.sprite.SpriteTop;
import org.schema.game.client.view.cubes.shapes.tetrahedron.TetrahedronBottomBackLeft;
import org.schema.game.client.view.cubes.shapes.tetrahedron.TetrahedronBottomBackRight;
import org.schema.game.client.view.cubes.shapes.tetrahedron.TetrahedronBottomFrontLeft;
import org.schema.game.client.view.cubes.shapes.tetrahedron.TetrahedronBottomFrontRight;
import org.schema.game.client.view.cubes.shapes.tetrahedron.TetrahedronTopBackLeft;
import org.schema.game.client.view.cubes.shapes.tetrahedron.TetrahedronTopBackRight;
import org.schema.game.client.view.cubes.shapes.tetrahedron.TetrahedronTopFrontLeft;
import org.schema.game.client.view.cubes.shapes.tetrahedron.TetrahedronTopFrontRight;
import org.schema.game.client.view.cubes.shapes.wedge.WedgeBottomBack;
import org.schema.game.client.view.cubes.shapes.wedge.WedgeBottomFront;
import org.schema.game.client.view.cubes.shapes.wedge.WedgeBottomLeft;
import org.schema.game.client.view.cubes.shapes.wedge.WedgeBottomRight;
import org.schema.game.client.view.cubes.shapes.wedge.WedgeIcon;
import org.schema.game.client.view.cubes.shapes.wedge.WedgeLeftBack;
import org.schema.game.client.view.cubes.shapes.wedge.WedgeLeftFront;
import org.schema.game.client.view.cubes.shapes.wedge.WedgeLeftLeft;
import org.schema.game.client.view.cubes.shapes.wedge.WedgeLeftRight;
import org.schema.game.client.view.cubes.shapes.wedge.WedgeTopBack;
import org.schema.game.client.view.cubes.shapes.wedge.WedgeTopFront;
import org.schema.game.client.view.cubes.shapes.wedge.WedgeTopLeft;
import org.schema.game.client.view.cubes.shapes.wedge.WedgeTopRight;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GraphicsContext;
import org.schema.schine.resource.FileExt;

import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.collision.shapes.ConvexShape;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class BlockShapeAlgorithm implements IconInterface {

	public static final int[][] vertexOrderMap = new int[][]{
			{3, 1, 0, 2},
			{2, 0, 1, 3},

			{1, 0, 2, 3},
			{3, 2, 0, 1},//{2,3,1,0}

			{2, 0, 1, 3},
			{3, 1, 0, 2},
	};
	public final int[][] vertexPermutations3 = new int[][]{
			{1,2,3}, 
			{1,3,2},
			{2,1,3}, 
			{2,3,1}, 
			{3,2,1}, 
			{3,1,2}, 
	};
	public final int[][] vertexPermutations4 = new int[][]{
			{1,2,3,4},       {2,1,3,4},       {3,2,1,4},       {4,2,3,1},
			{1,2,4,3},       {2,1,4,3},       {3,2,4,1},       {4,2,1,3},
			{1,3,2,4},       {2,3,1,4},       {3,1,2,4},       {4,3,2,1},
			{1,3,4,2},       {2,3,4,1},       {3,1,4,2},       {4,3,1,2},
			{1,4,2,3},       {2,4,1,3},       {3,4,2,1},       {4,1,2,3},
			{1,4,3,2},       {2,4,3,1},       {3,4,1,2},       {4,1,3,2},
	};
	public static final byte[][] vertexPermutationsIndex3 = new byte[][]{
			{0,1,2,3},       {1,0,2,3},       {2,1,0,3},       {3,1,2,0},
			{0,1,3,2},       {1,0,3,2},       {2,1,3,0},       {3,1,0,2},
			{0,2,1,3},       {1,2,0,3},       {2,0,1,3},       {3,2,1,0},
			{0,2,3,1},       {1,2,3,0},       {2,0,3,1},       {3,2,0,1},
			{0,3,1,2},       {1,3,0,2},       {2,3,1,0},       {3,0,1,2},
			{0,3,2,1},       {1,3,2,0},       {2,3,0,1},       {3,0,2,1},
	};
	public static final byte[][] extOrderPerm = new byte[24][4];
	public static final byte[][][] texOrderMapNormal = new byte[][][]{
			//FRONT
			{{2, 0, 1, 3},
					{3, 1, 0, 2},

					{3, 1, 0, 2},
					{2, 0, 1, 3},

					{3, 1, 0, 2},
					{2, 0, 1, 3}},

			//BACK
			{
					{2, 0, 1, 3},
					{3, 1, 0, 2},

					{3, 1, 0, 2},
					{2, 0, 1, 3},

					{3, 1, 0, 2},
					{2, 0, 1, 3}},

			//TOP
			{
					{2, 0, 1, 3},
					{3, 1, 0, 2},

					{3, 1, 0, 2},
					{2, 0, 1, 3},

					{3, 1, 0, 2},
					{2, 0, 1, 3}},

			//BOTTOM
			{
					{2, 0, 1, 3},
					{3, 1, 0, 2},

					{3, 1, 0, 2},
					{2, 0, 1, 3},

					{3, 1, 0, 2},
					{2, 0, 1, 3}},

			//RIGHT
			{
					{2, 0, 1, 3},
					{3, 1, 0, 2},

					{3, 1, 0, 2},
					{2, 0, 1, 3},

					{3, 1, 0, 2},
					{2, 0, 1, 3},
			},

			//LEFT
			{
					{2, 0, 1, 3},
					{3, 1, 0, 2},

					{3, 1, 0, 2},
					{2, 0, 1, 3},

					{3, 1, 0, 2},
					{2, 0, 1, 3},
			}
	};
	public static final byte[][][] texOrderMap4x4 = new byte[][][]{
		//FRONT
		{{2, 0, 1, 3},
			{3, 1, 0, 2},
			
			{3, 1, 0, 2},
			{2, 0, 1, 3},
			
			{3, 1, 0, 2},
			{2, 0, 1, 3}},
			
			//BACK
			{
				{2, 0, 1, 3},
				{3, 1, 0, 2},
				
				{3, 1, 0, 2},
				{2, 0, 1, 3},
				
				{3, 1, 0, 2},
				{2, 0, 1, 3}},
				
				//TOP
				{
					{2, 0, 1, 3},
					{3, 1, 0, 2},
					
					{3, 1, 0, 2},
					{2, 0, 1, 3},
					
					{3, 1, 0, 2},
					{2, 0, 1, 3}},
					
					//BOTTOM
					{
						{2, 0, 1, 3},
						{3, 1, 0, 2},
						
						{3, 1, 0, 2},
						{2, 0, 1, 3},
						
						{3, 1, 0, 2},
						{2, 0, 1, 3}},
						
						//RIGHT
						{
							{2, 0, 1, 3},
							{3, 1, 0, 2},
							
							{3, 1, 0, 2},
							{2, 0, 1, 3},
							
							{3, 1, 0, 2},
							{2, 0, 1, 3},
						},
						
						//LEFT
						{
							{2, 0, 1, 3},
							{3, 1, 0, 2},
							
							{3, 1, 0, 2},
							{2, 0, 1, 3},
							
							{3, 1, 0, 2},
							{2, 0, 1, 3},
						}
	};
	public static final byte[][][] texOrderMapPointToOrient = new byte[][][]{
			//FRONT
			{{2, 0, 1, 3},
					{3, 1, 0, 2},

					{3, 1, 0, 2},
					{2, 0, 1, 3},

					{3, 1, 0, 2},
					{2, 0, 1, 3}},

			//BACK
			{
					{2, 0, 1, 3},
					{3, 1, 0, 2},

					{3, 1, 0, 2},
					{2, 0, 1, 3},

					{3, 1, 0, 2},
					{2, 0, 1, 3}},

			//TOP
			{
					{2, 0, 1, 3},
					{3, 1, 0, 2},

					{3, 1, 0, 2},
					{2, 0, 1, 3},

					{3, 1, 0, 2},
					{2, 0, 1, 3}},

			//BOTTOM
			{
					{2, 0, 1, 3},
					{3, 1, 0, 2},

					{3, 1, 0, 2},
					{2, 0, 1, 3},

					{3, 1, 0, 2},
					{2, 0, 1, 3}},

			//RIGHT
			{
					{2, 0, 1, 3},
					{3, 1, 0, 2},

					{3, 1, 0, 2},
					{2, 0, 1, 3},

					{3, 1, 0, 2},
					{2, 0, 1, 3},
			},

			//LEFT
			{
					{2, 0, 1, 3},
					{3, 1, 0, 2},

					{3, 1, 0, 2},
					{2, 0, 1, 3},

					{3, 1, 0, 2},
					{2, 0, 1, 3},
			}
	};
	public static final short[] vertexTriangleOrder = new short[]{0, 1, 2, 2, 3, 0};
	protected static final float SMALL_SCALE = 0.98f;
	protected static final Vector3f originalScaling = new Vector3f(1, 1, 1);
	protected static final Vector3f smallerScaling = new Vector3f(0.48f, 0.48f, 0.48f);
	private static final float SMALL_SCALE_CALC = 0.99f;
	public static Properties prop;
	public static String configPath = DataUtil.dataPath + "config/vertexInfo.properties";
	public static BlockShapeAlgorithm[][] algorithms;
	public static int[][] xyMappings;
	public static int[][] xzMappings;
	public static int[][] yzMappings;
	private static IntOpenHashSet normalBlockAlgorithmIndices;
	private static Oriencube orientcubes[][] = new Oriencube[6][6];
	private static boolean initialized;
	public final byte[][] extOrderMap = new byte[][]{
			extOrderPerm[0],
			extOrderPerm[0],

			extOrderPerm[0],
			extOrderPerm[0],//{2,3,1,0}

			extOrderPerm[0],
			extOrderPerm[0],

			extOrderPerm[0],
	};
	
	
	
	
	public final int[] extOrderMapPointer = new int[]{
			0,
			0,

			0,
			0,//{2,3,1,0}

			0,
			0,

			0,
	};
	public org.schema.game.common.data.physics.ConvexHullShapeExt smallerShape;
	public Object2IntOpenHashMap<Matrix3f> rotBuffer = new Object2IntOpenHashMap<Matrix3f>();

	public BlockShapeAlgorithm() {
		super();
		initTexOrder();
		rememberTexOrder();

	}

	;

	public static void initialize() throws IOException {
		if(initialized){
			return;
		}
		prop = new Properties();
		read();

		ArrayList<byte[]> result = new ArrayList<byte[]>();
		permute(new byte[]{0, 1, 2, 3}, 0, result);

		for (int i = 0; i < result.size(); i++) {
			extOrderPerm[i] = result.get(i);
		}

		/*
		 * indices of algorithms that are normal blocks
		 */
		normalBlockAlgorithmIndices = new IntOpenHashSet();
		normalBlockAlgorithmIndices.add(5);

		algorithms = new
				BlockShapeAlgorithm[][]{
				{

						new WedgeTopFront(), new WedgeTopRight(), new WedgeTopBack(), new WedgeTopLeft(),
						new WedgeBottomFront(), new WedgeBottomRight(), new WedgeBottomBack(), new WedgeBottomLeft(),
						new WedgeLeftFront(), new WedgeLeftRight(), new WedgeLeftBack(), new WedgeLeftLeft(),

						new WedgeTopFront(), new WedgeTopRight(), new WedgeTopBack(), new WedgeTopLeft(),
						new WedgeBottomFront(), new WedgeBottomRight(), new WedgeBottomBack(), new WedgeBottomLeft(),
						new WedgeLeftFront(), new WedgeLeftRight(), new WedgeLeftBack(), new WedgeLeftLeft(),

						new WedgeIcon()
				},
				{
						new SpikeTopFrontRight(), new SpikeTopBackRight(), new SpikeTopBackLeft(), new SpikeTopFrontLeft(),
						new SpikeBottomFrontRight(), new SpikeBottomBackRight(), new SpikeBottomBackLeft(), new SpikeBottomFrontLeft(),

						new SpikeFrontFrontRight(), new SpikeFrontBackRight(), new SpikeFrontBackLeft(), new SpikeFrontFrontLeft(),
						new SpikeBackFrontRight(), new SpikeBackBackRight(), new SpikeBackBackLeft(), new SpikeBackFrontLeft(),

						new SpikeRightFrontRight(), new SpikeRightBackRight(), new SpikeRightBackLeft(), new SpikeRightFrontLeft(),
						new SpikeLeftFrontRight(), new SpikeLeftBackRight(), new SpikeLeftBackLeft(), new SpikeLeftFrontLeft(),

						//			new SpikeRightFrontRight(), new SpikeRightBackRight(), new SpikeRightBackLeft(), new SpikeRightFrontLeft(),
						//			new SpikeLeftFrontRight(), new SpikeLeftBackRight(), new SpikeLeftBackLeft(), new SpikeLeftFrontLeft(),

						new SpikeIcon(),
				},

				{
						new SpriteFront(), new SpriteBack(), new SpriteTop(), new SpriteBottom(), new SpriteRight(), new SpriteLeft(),
						new SpriteFront(), new SpriteBack(), new SpriteTop(), new SpriteBottom(), new SpriteRight(), new SpriteLeft(),

						new SpriteFront(), new SpriteBack(), new SpriteTop(), new SpriteBottom(), new SpriteRight(), new SpriteLeft(),
						new SpriteFront(), new SpriteBack(), new SpriteTop(), new SpriteBottom(), new SpriteRight(), new SpriteLeft(),

						new SpriteFront(), new SpriteBack(), new SpriteTop(), new SpriteBottom(), new SpriteRight(), new SpriteLeft(),
						new SpriteFront(), new SpriteBack(), new SpriteTop(), new SpriteBottom(), new SpriteRight(), new SpriteLeft(),

						new SpriteBottom()
				},
				{
						new TetrahedronTopFrontRight(), new TetrahedronTopBackRight(), new TetrahedronTopBackLeft(), new TetrahedronTopFrontLeft(),
						new TetrahedronBottomFrontRight(), new TetrahedronBottomBackRight(), new TetrahedronBottomBackLeft(), new TetrahedronBottomFrontLeft(),

						new TetrahedronTopFrontRight(), new TetrahedronTopBackRight(), new TetrahedronTopBackLeft(), new TetrahedronTopFrontLeft(),
						new TetrahedronBottomFrontRight(), new TetrahedronBottomBackRight(), new TetrahedronBottomBackLeft(), new TetrahedronBottomFrontLeft(),

						new TetrahedronTopFrontRight(), new TetrahedronTopBackRight(), new TetrahedronTopBackLeft(), new TetrahedronTopFrontLeft(),
						new TetrahedronBottomFrontRight(), new TetrahedronBottomBackRight(), new TetrahedronBottomBackLeft(), new TetrahedronBottomFrontLeft(),

						new SpikeIcon(),
				},
				{
						new PentaTopFrontRight(), new PentaTopBackRight(), new PentaTopBackLeft(), new PentaTopFrontLeft(),
						new PentaBottomFrontRight(), new PentaBottomBackRight(), new PentaBottomBackLeft(), new PentaBottomFrontLeft(),

						new PentaTopFrontRight(), new PentaTopBackRight(), new PentaTopBackLeft(), new PentaTopFrontLeft(),
						new PentaBottomFrontRight(), new PentaBottomBackRight(), new PentaBottomBackLeft(), new PentaBottomFrontLeft(),

						new PentaTopFrontRight(), new PentaTopBackRight(), new PentaTopBackLeft(), new PentaTopFrontLeft(),
						new PentaBottomFrontRight(), new PentaBottomBackRight(), new PentaBottomBackLeft(), new PentaBottomFrontLeft(),

						new SpikeIcon(),
				},
				{
						new OriencubeFrontBottom(), new OriencubeFrontLeft(), new OriencubeFrontTop(), new OriencubeFrontRight(),
						new OriencubeBackBottom(), new OriencubeBackLeft(), new OriencubeBackTop(), new OriencubeBackRight(),

						new OriencubeBottomBack(), new OriencubeBottomLeft(), new OriencubeBottomFront(), new OriencubeBottomRight(),
						new OriencubeTopBack(), new OriencubeTopLeft(), new OriencubeTopFront(), new OriencubeTopRight(),

						new OriencubeRightFront(), new OriencubeRightTop(), new OriencubeRightBack(), new OriencubeRightBottom(),
						new OriencubeLeftFront(), new OriencubeLeftTop(), new OriencubeLeftBack(), new OriencubeLeftBottom(),

						new SpriteBottom()
				},
		};

		for (int s = 0; s < algorithms.length; s++) {
			for (int i = 0; i < algorithms[s].length - 1; i++) {
				/*
				 * either normal block or sides to vis have to be set
				 */
				assert (normalBlockAlgorithmIndices.contains(s) || algorithms[s][i].getSidesToCheckForVis() != null) : algorithms[s][i].getClass().getSimpleName();

				algorithms[s][i].createSmallShape();
				algorithms[s][i].onInit();
			}
		}

		for (int prim = 0; prim < 6; prim++) {
			for (int sec = 0; sec < 6; sec++) {

				for (int i = 0; i < algorithms[5].length - 1; i++) {
					if (((Oriencube) algorithms[5][i]).getOrientCubePrimaryOrientation() == prim &&
							((Oriencube) algorithms[5][i]).getOrientCubeSecondaryOrientation() == sec) {
						orientcubes[prim][sec] = (Oriencube) algorithms[5][i];
						orientcubes[prim][sec].orientationArrayIndex = i;
						break;
					}
				}

			}

		}

		xyMappings = new int[algorithms.length][algorithms[0].length];
		xzMappings = new int[algorithms.length][algorithms[0].length];
		yzMappings = new int[algorithms.length][algorithms[0].length];

		for (int s = 0; s < xyMappings.length; s++) {
//			if(normalBlockAlgorithmIndices.contains(s)){
//				continue;
//			}
			for (int i = 0; i < xyMappings[s].length - 1; i++) {
				xyMappings[s][i] = algorithms[s][i].findXY(s);
				xzMappings[s][i] = algorithms[s][i].findXZ(s);
				yzMappings[s][i] = algorithms[s][i].findYZ(s);

			}
		}

		try {
			for(TexOrderStyle s : TexOrderStyle.values()){
				readTexOrder(s);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		initialized = true;
	}

	protected void onInit() {
		
	}

	public static Oriencube getOrientcube(int primary, int secondary) {
		assert (primary != secondary) : "can't request that algorithm. illogical: " + Element.getSideString(primary) + "; " + Element.getSideString(secondary);
		return orientcubes[primary][secondary];
	}
	
	public int getDoubleVertex(){
		return 0;
	};

	public static void read() {

		InputStream input = null;

		try {

			input = new FileInputStream(configPath);

			// load a properties file
			prop.load(input);

			// get the property value and print it out
			//			System.out.println(prop.getProperty("database"));
			//			System.out.println(prop.getProperty("dbuser"));
			//			System.out.println(prop.getProperty("dbpassword"));

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void permute(byte[] input2, int startindex, ArrayList<byte[]> result) {

		boolean contains = false;
		for (int i = 0; i < result.size() && !contains; i++) {
			contains = contains || Arrays.equals(result.get(i), input2);
		}
		if (!contains) {
			result.add(input2);
		}

		if (input2.length == startindex) {
			return;
		} else {
			for (int i = startindex; i < input2.length; i++) {
				byte[] input = Arrays.copyOf(input2, input2.length);
				byte temp = input[i];
				input[i] = input[startindex];
				input[startindex] = temp;
				permute(input, startindex + 1, result);
			}
		}
	}
	public static int[][][][][] halfBlockConfig;
	
	
	public static void rearrageStat(int side, boolean mirror) {
		//		rearrageLin(side, texOrderMap,mirror);
	}
	public static void main(String[] args) {
//		for (int a = 0; a < algorithms.length; a++) {
//			for (int b = 0; b < algorithms[a].length; b++) {
//				BlockShapeAlgorithm blockShapeAlgorithm = algorithms[a][b];
//
//				printOrderProperty(blockShapeAlgorithm);
//
//			}
//			System.out.println();
//		}
		
		
		HalfBlockArray.printHalfBlockArray();
	}
	public static boolean isValid(int blockStyle, byte orientationA) {
		return blockStyle > 0 && getLocalAlgoIndex(blockStyle, orientationA) < algorithms[blockStyle - 1].length;
	}
	public static boolean isValid(BlockStyle blockStyle, byte orientationA) {
		return isValid(blockStyle.id, orientationA);
	}

	public static byte getLocalAlgoIndex(BlockStyle blockStyle, byte orientation) {
		return getLocalAlgoIndex(blockStyle.id, orientation);
	}
	public static byte getLocalAlgoIndex(int blockStyle, byte orientation) {
		return (byte) ((orientation ) % (algorithms[blockStyle - 1].length - 1));
	}

	public static BlockShapeAlgorithm getAlgo(BlockStyle blockStyle, byte orientationA) {
		return BlockShapeAlgorithm.getAlgo(blockStyle.id, orientationA);
	}
	public static BlockShapeAlgorithm getAlgo(int blockStyle, byte orientationA) {
		BlockShapeAlgorithm blockShapeAlgorithm = algorithms[blockStyle - 1][getLocalAlgoIndex(blockStyle, orientationA)];
		if (blockShapeAlgorithm == null) {
			throw new NullPointerException("ERROR: algorithm does not exitst " + blockStyle + "; " + orientationA );
		}
		return blockShapeAlgorithm;
	}
	private static int[] noSides = new int[0];
	private static int[] oneSide = new int[1];
	public static int[] getSidesToCheckForVis(ElementInformation info, byte orientationA) {
		assert(info.getBlockStyle().solidBlockStyle == (!info.isNormalBlockStyle() && info.getBlockStyle() != BlockStyle.SPRITE));
		if(info.blockStyle == BlockStyle.NORMAL24) {
			orientationA /= 4;
		}
		if(info.slab > 0){
			oneSide[0] = Element.switchLeftRight(Element.getOpposite(orientationA));
			return oneSide;
		}else if( info.getBlockStyle().solidBlockStyle && !(info.hasLod() && info.lodShapeStyle == 1)){
			BlockShapeAlgorithm algo = getAlgo(info.blockStyle.id, orientationA);
			return algo.getSidesToCheckForVis();
		}else{
			return noSides;
		}
	}

	public static ConvexShape getShape(int blockStyle, byte orientationA) {
		return getAlgo(blockStyle, orientationA).getShape();
	}
	public static ConvexShape getShape(BlockStyle blockStyle, byte orientationA) {
		return getAlgo(blockStyle.id, orientationA).getShape();
	}

	public static ConvexShape getSmallShape(BlockStyle blockStyle, byte orientationA) {
		assert (getAlgo(blockStyle, orientationA).smallerShape != null) : getAlgo(blockStyle, orientationA);
		return getAlgo(blockStyle, orientationA).smallerShape;
	}

	;


	public static void readTexOrder() throws IOException {
		for(TexOrderStyle s : TexOrderStyle.values()){
			readTexOrder(s);
			readTexOrder(s);
			readTexOrder(s);
		}
	}

	public enum TexOrderStyle{
		NORMAL("./data/textures/texOrderNormal.config", texOrderMapNormal),
		ORIENT("./data/textures/texOrderPointToOrientation.config", texOrderMapPointToOrient),
		AREA4x4("./data/textures/texOrder4x4.config", texOrderMap4x4)
		;
		public final String path;
		public final byte[][][] map;

		private TexOrderStyle(String path, byte[][][] map){
			this.path = path;
			this.map = map;
		}
	}
	
	public static void readTexOrder(TexOrderStyle pointToOrient) throws IOException {
		BufferedReader b = new BufferedReader(new FileReader(new FileExt(pointToOrient.path)));
		byte[][][] or = pointToOrient.map;

		String line = null;
		int orientation = 0;
		int side = 0;
		while ((line = b.readLine()) != null) {
			if (!line.startsWith("//") && line.contains(",")) {
				int indexOf = line.indexOf("//");
				String[] split = line.substring(0, indexOf < 0 ? line.length() : indexOf).split(",");
				for (int i = 0; i < split.length; i++) {
					or[orientation][side][i] = (byte) Integer.parseInt(split[i].trim());
				}
				side++;
				if (side == 6) {
					side = 0;
					orientation++;
				}
			}
		}
		b.close();

	}
	public static void writeTexOrder(TexOrderStyle pointToOrient) throws IOException {
		BufferedWriter b =  new BufferedWriter(new FileWriter(new FileExt(pointToOrient.path)));
		byte[][][] or = pointToOrient.map;
		
		
		for(int orientation = 0; orientation < or.length; orientation++){
			b.write("//"+Element.getSideString(orientation).toUpperCase(Locale.ENGLISH)+"\n");
			for(int side = 0; side < or[orientation].length; side++){
				for(int i = 0; i < or[orientation][side].length; i++){
					b.write(String.valueOf(or[orientation][side][i]));
					if(i < or[orientation][side].length-1){
						b.write(",");
					}
				}
				b.write(" //"+Element.getSideString(side).toUpperCase(Locale.ENGLISH)+"\n");
			}
			
		}
		b.close();
	}

	public static void normalBlock(BlockRenderInfo r) {
		if (CubeMeshBufferContainer.isTriangle()) {
			for (short j = 0; j < CubeMeshBufferContainer.VERTS_PER_FACE; j++) {

				short i = vertexTriangleOrder[j];

				short dex = i;
				int sid = r.sideId;
				byte tex;
				if (r.threeSided) {
					//texcoord encoding
					tex = TexOrderStyle.NORMAL.map[0][r.sideId][i];
				} else {
						//texcoord encoding
					tex = r.pointToOrientation.map[r.orientation % 6][r.sideId][i];
				}

				int normalMode = 0;
				byte insideMode = 0;
				put(r, sid, dex, tex, i, normalMode, insideMode, false);
			}
		} else {
			for (short i = 0; i < CubeMeshBufferContainer.VERTS_PER_FACE; i++) {
				short dex = i;
				int sid = r.sideId;
				byte tex;
				if (r.threeSided) {
					//texcoord encoding
					tex = TexOrderStyle.NORMAL.map[0][r.sideId][i];
				} else {
					//texcoord encoding
					tex = r.pointToOrientation.map[r.orientation % 6][r.sideId][i];
				}

				int normalMode = 0;
				byte insideMode = 0;
				put(r, sid, dex, tex, i, normalMode, insideMode, false);
			}
		}
	}

	protected static void put(BlockRenderInfo ri, int sid, short dex, byte tex, short i, int normalMode, byte insideMode, boolean angled) {

		byte r = ri.container.getFinalLight(ri.lightIndex, ri.sideOccId + i, 0); //colorR
		byte g = ri.container.getFinalLight(ri.lightIndex, ri.sideOccId + i, 1); //colorG
		byte b = ri.container.getFinalLight(ri.lightIndex, ri.sideOccId + i, 2); //colorB
		byte o = ri.container.getFinalLight(ri.lightIndex, ri.sideOccId + i, 3); //occlusion

		byte dX = ri.container.getFinalLight(ri.lightIndex, ri.sideOccId + i, 4);
		byte dY = ri.container.getFinalLight(ri.lightIndex, ri.sideOccId + i, 5);
		byte dZ = ri.container.getFinalLight(ri.lightIndex, ri.sideOccId + i, 6);
		
		if(GraphicsContext.INTEGER_VERTICES){
			MemIntArray buffer = angled ? 
					((CubeBufferInt)ri.container.dataBuffer).buffers[6] : 
						((CubeBufferInt)ri.container.dataBuffer).buffers[sid];
			
			put(ri, sid, dex, tex, i, r, g, b, o, dX, dY, dZ, normalMode, insideMode, buffer);
		}else{
			MemFloatArray buffer = angled ? 
					((CubeBufferFloat)ri.container.dataBuffer).buffers[6] : 
						((CubeBufferFloat)ri.container.dataBuffer).buffers[sid];
			
			put(ri, sid, dex, tex, i, r, g, b, o, normalMode, insideMode, buffer);
		}
		
	}

	
	
	
	
	
	protected static void put(BlockRenderInfo ri, int sid, short dex, byte tex, short i, byte r, byte g, byte b, byte o, byte dX, byte dY, byte dZ, 
			int normalMode, byte insideMode, MemIntArray dataBuffer) {
		
		
		int[] halfBlockConfig = HalfBlockArray.getHalfBlockConfig(ri.blockStyle, i, sid, ri.orientation, ri.halvedFactor);
		
		int code = ByteUtil.getCodeI(
				(byte) sid,
				ri.layer,
				ri.typeCode,
				ri.hitPointsCode,
				ri.animatedCode,
				tex, (byte)halfBlockConfig[2], ri.onlyInBuildMode, ri.extendedBlockTexture);
		
		int indexCode = ByteUtil.getCodeIndexI(ri.index, r, g, b);
		
		int codeS = ByteUtil.getCodeSI(normalMode, ri.resOverlay, o, dX, dY, dZ, halfBlockConfig[0], halfBlockConfig[1]);
		
		int sendCode = (byte) vertexOrderMap[sid][dex] + code;
		
		dataBuffer.put(indexCode);
		dataBuffer.put(sendCode);
		dataBuffer.put((int)ri.segIndex);
		dataBuffer.put(codeS);
		assert (ri.index + i < CubeInfo.CUBE_SIDE_STRIDE) : "vert index is bigger: " + (ri.index + i) + "/" + CubeInfo.CUBE_SIDE_STRIDE;
	}
	protected static void put(BlockRenderInfo ri, int sid, short dex, byte tex, short i, byte r, byte g, byte b, byte o, 
			int normalMode, byte insideMode, MemFloatArray dataBuffer) {

		
		int[] halfBlockConfig = HalfBlockArray.getHalfBlockConfig(ri.blockStyle, i, sid, ri.orientation, ri.halvedFactor);
		
		float code = ByteUtil.getCodeF(
				(byte) sid,
				ri.layer,
				ri.typeCode,
				ri.hitPointsCode,
				ri.animatedCode,
				tex, (byte)halfBlockConfig[2], ri.onlyInBuildMode);

		float indexCode = ByteUtil.getCodeIndexF(ri.index, r, g, b);
		
		float codeS = ByteUtil.getCodeS(normalMode, ri.resOverlay, o, halfBlockConfig[0], halfBlockConfig[1]);
		
		float sendCode = (byte) vertexOrderMap[sid][dex] + code;

		dataBuffer.put(indexCode);
		dataBuffer.put(sendCode);
		if (CubeMeshBufferContainer.vertexComponents > 2) {
			dataBuffer.put(ri.segIndex);
			dataBuffer.put(codeS);
		}
		assert (ri.index + i < CubeInfo.CUBE_SIDE_STRIDE) : "vert index is bigger: " + (ri.index + i) + "/" + CubeInfo.CUBE_SIDE_STRIDE;
	}

	public byte[] getWedgeOrientation() {
		return null;
	}

	public byte getWedgeGravityValidDir(byte gravityDir) {
		return -1;
	}

	public int[] getProperty() {
		BlockShape annotation = getClass().getAnnotation(BlockShape.class);
		assert (prop != null);
		String property = prop.getProperty(annotation.name());
//		if(property == null){
//			System.err.println(annotation.name()+"=3,0,19,0,20,1,0");
//			return new int[7];
//		}
		assert (property != null) : "not found in file: " + annotation.name();
		int[] v = new int[7];

		String[] split = property.split(",");

		for (int i = 0; i < 7; i++) {
			v[i] = Integer.parseInt(split[i]);
		}
		return v;
	}

	public String propertyToString(int[] p) {
		StringBuffer b = new StringBuffer();

		for (int i = 0; i < 7; i++) {
			b.append(p[i]);
			if (i < 6) {
				b.append(",");
			}
		}
		return b.toString();
	}
	public static void changeTexCoordOrder(SegmentPiece currentPiece,
			int sideId, int dir) throws IOException {
		
		ElementInformation info = ElementKeyMap.getInfo(currentPiece.getType());
		
		if (info.individualSides == 3) {
			//texcoord encoding
			//BlockShapeAlgorithm.texOrderMapNormal[0][sideId][i];
			TexOrderStyle t = TexOrderStyle.NORMAL;
			permute(t.map[0][sideId], dir);
			writeTexOrder(t);
		} else {
			byte orientation = currentPiece.getOrientation();
			TexOrderStyle t;
			if (info.extendedTexture) {
				t = TexOrderStyle.AREA4x4;
			}else if (info.sideTexturesPointToOrientation) {
				t = TexOrderStyle.ORIENT;
				
			} else {
				t = TexOrderStyle.NORMAL;
			}
			
			permute(t.map[orientation % 6][sideId], dir);
			writeTexOrder(t);
		}
	}
	public static void permute(byte[] or, int dir){
		int current = -1;
		for(int i = 0; i < vertexPermutationsIndex3.length; i++){
			if(Arrays.equals(vertexPermutationsIndex3[i], or)){
				current = i;
				break;
			}
		}
		if(current == -1){
			throw new NullPointerException("No permutation found");
		}
		current = (current+dir);
		if(current < 0){
			current = vertexPermutationsIndex3.length-1;
		}else if(current >= vertexPermutationsIndex3.length){
			current = 0;
		}
		
		for(int i = 0; i < vertexPermutationsIndex3[current].length; i++){
			or[i] = vertexPermutationsIndex3[current][i];
		}
		
	}
	public void modProperty(int side, int m) {

		System.err.println("MOD PROPERTY: SIDE: " + side + "; dir " + m);

		int[] property = getProperty();
		assert(side >= 0):side+"; "+m;
		property[side] = FastMath.cyclicModulo(property[side] + m, 24);
		BlockShape annotation = getClass().getAnnotation(BlockShape.class);
		prop.setProperty(annotation.name(), propertyToString(property));

		OutputStream output = null;

		try {

			output = new FileOutputStream(configPath);

			DateFormat dateFormat = StringTools.getSimpleDateFormat(Lng.str("yyyy/MM/dd HH:mm:ss"), "yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			// save properties to project root folder
			prop.store(output, "Generated " + dateFormat.format(date));

		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		initTexOrder();
	}

	private void initTexOrder() {
		int[] property = getProperty();

		for (int i = 0; i < 7; i++) {
			extOrderMap[i] = extOrderPerm[property[i]];
		}
	}

	protected int findIndex(Class<? extends BlockShapeAlgorithm> classToFind, int type) {
		for (int i = 0; i < algorithms[type].length; i++) {
			if (classToFind.equals(algorithms[type][i].getClass())) {
				return i;
			}
		}
		throw new IllegalArgumentException("NOT FOUND " + classToFind + "; in type " + type + ": " + Arrays.toString(algorithms[type]));
	}

	public void rearrage(int side, int dir) {

		extOrderMapPointer[side] = (FastMath.cyclicModulo(extOrderMapPointer[side] + dir, extOrderPerm.length));
		extOrderMap[side] = extOrderPerm[extOrderMapPointer[side]];

		System.err.println("REARRAGED: " + this + ": " + Element.getSideString(side) + "(" + side + "): " + Arrays.toString(extOrderMap[side]) + "-->" + extOrderMapPointer[side]);

	}

	private void rememberTexOrder() {
		for (int side = 0; side < 7; side++) {
			byte[] bs = extOrderMap[side];
			boolean found = false;
			for (int i = 0; i < extOrderPerm.length; i++) {
				if (Arrays.equals(extOrderPerm[i], bs)) {
					found = true;
					extOrderMapPointer[side] = i;
					break;
				}
			}
			assert (found);
		}

	}

	public void output() {
		System.err.println(this);
		System.err.println("	");
		System.err.println("		assert(this instanceof " + this.getClass().getSimpleName() + "):this+\" but " + this.getClass().getSimpleName() + "\";");
		for (int side = 0; side < 6; side++) {
			System.err.println("		extOrderMap[Element." + Element.getSideString(side) + "] = extOrderPerm[" + extOrderMapPointer[side] + "];");
		}
		System.err.println("	}");
	}

	protected int findYZ(int s) {
		//flip X
		for (int i = 0; i < algorithms[s].length; i++) {
			BlockShapeAlgorithm blockShapeAlgorithm = algorithms[s][i];
			if (!blockShapeAlgorithm.isPhysical()) {
				continue;
			}
			org.schema.game.common.data.physics.ConvexHullShapeExt h = (org.schema.game.common.data.physics.ConvexHullShapeExt) blockShapeAlgorithm.getShape();
			org.schema.game.common.data.physics.ConvexHullShapeExt own = (org.schema.game.common.data.physics.ConvexHullShapeExt) getShape();

			boolean matches = true;
			for (int a = 0; a < own.getNumPoints(); a++) {
				Vector3f ownV = new Vector3f();
				own.getVertex(a, ownV);
				//searching for a flipped x
				ownV.x = -ownV.x;
				boolean found = false;
				for (int b = 0; b < h.getNumPoints(); b++) {
					Vector3f otherV = new Vector3f();
					h.getVertex(b, otherV);
					if (otherV.equals(ownV)) {
						found = true;
						break;
					}
				}
				if (!found) {
					matches = false;
					break;
				}
			}
			if (matches) {
				//					System.err.println("YZ MEEM: "+this+" -> "+algorithms[s][i]);
				if (h.getNumPoints() < 6 && algorithms[s][i] == this) {
					for (int a = 0; a < own.getNumPoints(); a++) {
						Vector3f ownV = new Vector3f();
						own.getVertex(a, ownV);
						//searching for a flipped x
						ownV.x = -ownV.x;
						boolean found = false;

						for (int b = 0; b < h.getNumPoints(); b++) {
							Vector3f otherV = new Vector3f();
							h.getVertex(b, otherV);

							//								System.err.println(a+" CHECKING "+ownV+" -> "+otherV+" "+otherV.equals(ownV));
							if (otherV.equals(ownV)) {
								found = true;
								break;
							}
						}
						if (!found) {
							matches = false;
							break;
						}
					}
				}
				assert (h.getNumPoints() == 6 || algorithms[s][i] != this) : algorithms[s][i] + "; " + this;
				return i;
			}
		}
		throw new IllegalArgumentException(this.toString());
	}

	protected int findXZ(int s) {
		//flip X
		for (int i = 0; i < algorithms[s].length - 1; i++) {
			BlockShapeAlgorithm blockShapeAlgorithm = algorithms[s][i];
			if (!blockShapeAlgorithm.isPhysical()) {
				continue;
			}
			org.schema.game.common.data.physics.ConvexHullShapeExt h = (org.schema.game.common.data.physics.ConvexHullShapeExt) blockShapeAlgorithm.getShape();
			org.schema.game.common.data.physics.ConvexHullShapeExt own = (org.schema.game.common.data.physics.ConvexHullShapeExt) getShape();
			boolean matches = true;
			for (int a = 0; a < own.getNumPoints(); a++) {
				Vector3f ownV = new Vector3f();
				own.getVertex(a, ownV);
				//searching for a flipped x
				ownV.y = -ownV.y;
				boolean found = false;
				for (int b = 0; b < h.getNumPoints(); b++) {
					Vector3f otherV = new Vector3f();
					h.getVertex(b, otherV);
					if (otherV.equals(ownV)) {
						found = true;
						break;
					}
				}
				if (!found) {
					matches = false;
					break;
				}
			}
			if (matches) {
				return i;
			}
		}
		throw new IllegalArgumentException(this.toString());
	}

	protected int findXY(int s) {
		//flip X
		for (int i = 0; i < algorithms[s].length; i++) {
			BlockShapeAlgorithm blockShapeAlgorithm = algorithms[s][i];
			if (!blockShapeAlgorithm.isPhysical()) {
				continue;
			}
			org.schema.game.common.data.physics.ConvexHullShapeExt h = (org.schema.game.common.data.physics.ConvexHullShapeExt) blockShapeAlgorithm.getShape();
			org.schema.game.common.data.physics.ConvexHullShapeExt own = (org.schema.game.common.data.physics.ConvexHullShapeExt) getShape();
			boolean matches = true;
			//						System.err.println("CHECKING "+own+" -> "+h);
			for (int a = 0; a < own.getNumPoints(); a++) {
				Vector3f ownV = new Vector3f();
				own.getVertex(a, ownV);
				//searching for a flipped x
				ownV.z = -ownV.z;
				boolean found = false;
				for (int b = 0; b < h.getNumPoints(); b++) {
					Vector3f otherV = new Vector3f();
					h.getVertex(b, otherV);
					if (otherV.equals(ownV)) {
						found = true;
						break;
					}
				}
				if (!found) {
					matches = false;
					break;
				}
			}
			if (matches) {
				return i;
			}
		}
		throw new IllegalArgumentException(this.toString());
	}

	public void createSmallShape() {
		if (isPhysical() && !(this instanceof Oriencube) && getShape() != null) {

			List<Vector3f> sigPoints = getSigPoints();

			org.schema.game.common.data.physics.ConvexHullShapeExt cv = (org.schema.game.common.data.physics.ConvexHullShapeExt) getShape();
			com.bulletphysics.util.ObjectArrayList<Vector3f> points = cv.getPoints();

			com.bulletphysics.util.ObjectArrayList<Vector3f> smallPoints = new com.bulletphysics.util.ObjectArrayList<Vector3f>();

			for (Vector3f p : points) {
				if (sigPoints.contains(p)) {
					//dont change significant points
					Vector3f toAdd = new Vector3f(p);
					toAdd.scale(SMALL_SCALE);
					smallPoints.add(toAdd);
				} else {
					//find closest significant points

					float minDist = 100000;
					ObjectArrayList<Vector3f> closestSigPoints = new ObjectArrayList<Vector3f>();
					for (Vector3f v : sigPoints) {

						Vector3f d = new Vector3f();
						d.sub(v, p);

						float len = d.length();
						if (len <= minDist) {
							closestSigPoints.add(v);
							//remove all closest points that are no longer closest
							for (int i = 0; i < closestSigPoints.size(); i++) {
								Vector3f d2 = new Vector3f();
								d2.sub(closestSigPoints.get(i), p);

								if (d2.length() - 0.001f > minDist) {
									closestSigPoints.remove(i);
									i--;
								}
							}
							minDist = len;
						}

					}
					assert (closestSigPoints.size() > 0) : closestSigPoints.size() + " :::: " + sigPoints.size();

					Vector3f toAdd = new Vector3f(p);
					//move point in direction of closest significant points
					for (int i = 0; i < closestSigPoints.size(); i++) {
						Vector3f m = new Vector3f();
						m.sub(closestSigPoints.get(i), p);
						m.normalize();
						m.scale(1f - SMALL_SCALE_CALC);

						toAdd.add(m);

					}
					toAdd.scale(SMALL_SCALE);
					smallPoints.add(toAdd);
				}
			}
//			System.err.println("[ALGO] Creating smaller shape for: "+this);
			smallerShape = new org.schema.game.common.data.physics.ConvexHullShapeExt(smallPoints);
			smallerShape.setMargin(-0.03f);
		} else {
//			System.err.println("[ALGO] NOT Creating smaller shape for: "+this);
			if (isPhysical() && !(this instanceof Oriencube)) {
				assert (getShape() != null) : this;
			}
		}
	}

	private List<Vector3f> getSigPoints() {

		ObjectArrayList<Vector3f> sigPoints = new ObjectArrayList<Vector3f>();

		com.bulletphysics.util.ObjectArrayList<Vector3f> points = ((org.schema.game.common.data.physics.ConvexHullShapeExt) getShape()).getPoints();

		for (Vector3f p : points) {
			ObjectArrayList<Vector3f> neighbors = new ObjectArrayList<Vector3f>();
			for (Vector3f v : points) {
				if (p != v) {
					if (
							(p.x == v.x && p.y == v.y && p.z != v.z) ||
									(p.x == v.x && p.y != v.y && p.z == v.z) ||
									(p.x != v.x && p.y == v.y && p.z == v.z)
							) {
						neighbors.add(v);
					}
				}

			}
			if (neighbors.size() == 3) {
				sigPoints.add(p);
			}
		}
		assert (sigPoints.size() > 0) : this;
		return sigPoints;
	}

	public int findRot(BlockShapeAlgorithm[] row, Matrix3f rot) {

		if (rotBuffer.containsKey(rot)) {
			return rotBuffer.get(rot);
		}
		for (int i = 0; i < row.length; i++) {
			BlockShapeAlgorithm blockShapeAlgorithm = row[i];
			if (!blockShapeAlgorithm.isPhysical() || this == blockShapeAlgorithm) {
				continue;
			}

			org.schema.game.common.data.physics.ConvexHullShapeExt h = (org.schema.game.common.data.physics.ConvexHullShapeExt) blockShapeAlgorithm.getShape();
			org.schema.game.common.data.physics.ConvexHullShapeExt own = (org.schema.game.common.data.physics.ConvexHullShapeExt) getShape();
			boolean matches = true;
			//						System.err.println("CHECKING "+own+" -> "+h);
			for (int a = 0; a < own.getNumPoints(); a++) {
				Vector3f ownV = new Vector3f();
				own.getVertex(a, ownV);
				//searching for a flipped x
//				ownV.z = -ownV.z;
				rot.transform(ownV);

				//correct rounding errors from rotation

				if (ownV.x > 0.4f) {
					ownV.x = 0.5f;
				} else if (ownV.x < -0.4f) {
					ownV.x = -0.5f;
				}

				if (ownV.y > 0.4f) {
					ownV.y = 0.5f;
				} else if (ownV.y < -0.4f) {
					ownV.y = -0.5f;
				}

				if (ownV.z > 0.4f) {
					ownV.z = 0.5f;
				} else if (ownV.z < -0.4f) {
					ownV.z = -0.5f;
				}
				boolean found = false;
				for (int b = 0; b < h.getNumPoints(); b++) {
					Vector3f otherV = new Vector3f();
					h.getVertex(b, otherV);

					if (otherV.equals(ownV)) {
						found = true;
						break;
					}
				}
				if (!found) {
					matches = false;
					break;
				}
			}
			if (matches) {
				rotBuffer.put(new Matrix3f(rot), i);
				return i;
			}
		}
		throw new IllegalArgumentException(this.toString());
	}

	public boolean isPhysical() {
		return true;
	}

	public abstract void createSide(int sideId, short i, AlgorithmParameters p);

	public int getSixthSideOrientation() {
		return -1;
	}
	public abstract boolean isAngled(int sideId); 
	//int sideId, byte layer, short typeCode, byte hitPointsCode, byte animatedCode, int lightIndex, int sideOccId, int index, float segIndex, byte orientation, int halvedFactor, CubeMeshBufferContainer container, int resOverlay, boolean onlyInBuildMode, boolean extendedBlockTexture
	public void create(BlockRenderInfo r) {
		AlgorithmParameters p = r.container.p;

		if (CubeMeshBufferContainer.isTriangle()) {
			for (short j = 0; j < CubeMeshBufferContainer.VERTS_PER_FACE; j++) {

				//				p.ext = 0;
				//				p.normalMode = 0;
				//				p.insideMode = 0;
				//				p.sid = 0;
				//				p.vID = 0;

				short i = vertexTriangleOrder[j];
				createSide(r.sideId, i, p);
				put(r, p.sid, p.vID, p.ext, i, p.normalMode, p.insideMode, isAngled(r.sideId));

			}
			if (r.sideId == getSixthSideOrientation() && hasExtraSide() && p.fresh) {
				for (short j = 0; j < CubeMeshBufferContainer.VERTS_PER_FACE; j++) {
					short i = vertexTriangleOrder[j];

					createSide(6, i, p);
					assert (p.sid < 6);
					if(Occlusion.isNormnew()){
						r.sideOccId = (6 * CubeMeshBufferContainer.VERTICES_PER_SIDE);
					}
					put(r, p.sid, p.vID, p.ext, i, p.normalMode, p.insideMode, true);

				}
				p.fresh = false;
			}

		} else {
			for (short i = 0; i < CubeMeshBufferContainer.VERTS_PER_FACE; i++) {
				createSide(r.sideId, i, p);
				put(r, p.sid, p.vID, p.ext, i, p.normalMode, p.insideMode, isAngled(r.sideId));
			}
			if (r.sideId == getSixthSideOrientation() && hasExtraSide() && p.fresh) {
				for (short i = 0; i < CubeMeshBufferContainer.VERTS_PER_FACE; i++) {
					createSide(6, i, p);
					assert (p.sid < 6);
					if(Occlusion.isNormnew()){
						r.sideOccId = (6 * CubeMeshBufferContainer.VERTICES_PER_SIDE);
					}
					put(r, p.sid, p.vID, p.ext, i, p.normalMode, p.insideMode, true);

				}
				p.fresh = false;
			}
		}
	}

	public boolean hasExtraSide() {
		return false;
	}

	@Override
	public void single(BlockRenderInfo ri, byte r, byte g, byte b, byte o, MemFloatArray dataBuffer, AlgorithmParameters p) {

		if (CubeMeshBufferContainer.isTriangle()) {

			for (short j = 0; j < CubeMeshBufferContainer.VERTS_PER_FACE; j++) {
				short i = vertexTriangleOrder[j];
				createSide(ri.sideId, i, p);
				put(ri, p.sid, p.vID, p.ext, i, r, g, b, o, p.normalMode, p.insideMode, dataBuffer);
			}
			if (hasExtraSide() && p.fresh) {
				for (short j = 0; j < CubeMeshBufferContainer.VERTS_PER_FACE; j++) {

					short i = vertexTriangleOrder[j];
					createSide(6, i, p);
					assert (p.sid < 6);
					put(ri, p.sid, p.vID, p.ext, i, r, g, b, o, p.normalMode, p.insideMode, dataBuffer);
				}
				p.fresh = false;
			}
		} else {

			for (short i = 0; i < CubeMeshBufferContainer.VERTS_PER_FACE; i++) {
				createSide(ri.sideId, i, p);
				put(ri, p.sid, p.vID, p.ext, i, r, g, b, o, p.normalMode, p.insideMode, dataBuffer);
			}
			if (hasExtraSide() && p.fresh) {
				for (short i = 0; i < CubeMeshBufferContainer.VERTS_PER_FACE; i++) {
					createSide(6, i, p);
					assert (p.sid < 6);
					put(ri, p.sid, p.vID, p.ext, i, r, g, b, o, p.normalMode, p.insideMode, dataBuffer);
				}
				p.fresh = false;
			}
		}
	}
	@Override
	public void single(BlockRenderInfo ri, byte r, byte g, byte b, byte o, MemIntArray dataBuffer, AlgorithmParameters p) {
		byte lDirX = 0;
		byte lDirY = 0;
		byte lDirZ = 0;
		if (CubeMeshBufferContainer.isTriangle()) {
			
			for (short j = 0; j < CubeMeshBufferContainer.VERTS_PER_FACE; j++) {
				short i = vertexTriangleOrder[j];
				createSide(ri.sideId, i, p);
				put(ri, p.sid, p.vID, p.ext, i, r, g, b, o, lDirX, lDirY, lDirZ, p.normalMode, p.insideMode, dataBuffer);
			}
			if (hasExtraSide() && p.fresh) {
				for (short j = 0; j < CubeMeshBufferContainer.VERTS_PER_FACE; j++) {
					
					short i = vertexTriangleOrder[j];
					createSide(6, i, p);
					assert (p.sid < 6);
					put(ri, p.sid, p.vID, p.ext, i, r, g, b, o, lDirX, lDirY, lDirZ, p.normalMode, p.insideMode, dataBuffer);
				}
				p.fresh = false;
			}
		} else {
			
			for (short i = 0; i < CubeMeshBufferContainer.VERTS_PER_FACE; i++) {
				createSide(ri.sideId, i, p);
				put(ri, p.sid, p.vID, p.ext, i, r, g, b, o, lDirX, lDirY, lDirZ, p.normalMode, p.insideMode, dataBuffer);
			}
			if (hasExtraSide() && p.fresh) {
				for (short i = 0; i < CubeMeshBufferContainer.VERTS_PER_FACE; i++) {
					createSide(6, i, p);
					assert (p.sid < 6);
					put(ri, p.sid, p.vID, p.ext, i, r, g, b, o, lDirX, lDirY, lDirZ, p.normalMode, p.insideMode, dataBuffer);
				}
				p.fresh = false;
			}
		}
	}

	//	public abstract void create(int sideId, byte layer, short typeCode, byte hitPointsCode, byte animatedCode, int lightIndex, int sideOccId, int index, float segIndex, CubeMeshBufferContainer container);
	protected abstract ConvexShape getShape();

	public abstract int[] getSidesToCheckForVis();

	public abstract int[] getSidesAngled();

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

	public Vector3f getShapeCenter(Vector3f sum) {
		sum.set(0.0F, 0.0F, 0.0F);
		if (getShape() instanceof ConvexHullShape) {
			for (Vector3f v : ((ConvexHullShape) getShape()).getPoints()) {
				sum.add(v);
			}
			sum.scale(1.0F / ((ConvexHullShape) getShape()).getNumPoints());
		}
		return sum;
	}

	public boolean hasValidShape() {
		return true;
	}

	private static Vector3i[][] relativeBySide = new Vector3i[][]{
		{ //FRONT
			new Vector3i( 1, 	 1, 	 1),
			new Vector3i(-1, 	 1, 	 1),
			new Vector3i(-1, 	-1, 	 1),
			new Vector3i( 1, 	-1, 	 1),
		},
		{ //BACK
			new Vector3i( 1, 	-1, 	-1),
			new Vector3i(-1, 	-1, 	-1),
			new Vector3i(-1, 	 1, 	-1),
			new Vector3i( 1, 	 1, 	-1),
		},
		{ //TOP
			new Vector3i(1, 	1, -1),
			new Vector3i(-1, 	1, -1),
			new Vector3i(-1, 	1, 1),
			new Vector3i(1, 	1, 1),
		},
		{ //BOTTOM
			new Vector3i(1, 	-1, 1),
			new Vector3i(-1, 	-1, 1),
			new Vector3i(-1, 	-1, -1),
			new Vector3i(1, 	-1, -1),
		},
		{ //LEFT (real left)
			new Vector3i(-1, 	-1, 	 1),
			new Vector3i(-1, 	-1, 	-1),
			new Vector3i(-1, 	 1, 	-1),
			new Vector3i(-1, 	 1, 	 1),
		},
		{ //RIGHT (real right)
			new Vector3i( 1, 	 1, 	 1),
			new Vector3i( 1, 	 1, 	-1),
			new Vector3i( 1, 	-1, 	-1),
			new Vector3i( 1, 	-1, 	 1),
		}
	}; 
	
	public static Vector3i[] getSideVerticesByNormal(int sideId, int normal, BlockShapeAlgorithm bs) {
		
		if(bs == null){
			if(normal < 6){
				//we are serching for a normal block 
				return relativeBySide[normal];
			}else{
				return null;
			}
		}else{
			return bs.getSideByNormal(sideId, normal);
		}
		
			
	}
	public static int getRepresentitiveNormal(int sideId, int normal,
			BlockShapeAlgorithm bs) {
		if(bs == null){
			if(normal < 6){
				//we are serching for a normal block 
				return normal;
			}else{
				return -1;
			}
		}else{
			return bs.getRepresentitiveNormal(sideId, normal);
		}
	}
	public static int getAngledSideLightRepresentitive(int sideId, int normal,
			BlockShapeAlgorithm bs) {
		if(bs == null){
			if(normal < 6){
				//we are serching for a normal block 
				return sideId;
			}else{
				return -1;
			}
		}else{
			return bs.getAngledSideLightRepresentitive(sideId, normal);
		}
	}
	protected int getAngledSideLightRepresentitive(int sideId, int normal) {
		return sideId;
	}
	public static Vector3i[] none = new Vector3i[0];
	/**
	 * overwritten by non cubic shapes to return abnormal sides
	 * @param normal
	 * @return vertices that correspond to the side by normal, or null if no side with that normal exists
	 */
	protected Vector3i[] getSideByNormal(int sideId, int normal) {
		return relativeBySide[normal];
	}
	protected int getRepresentitiveNormal(int sideId, int normal) {
		return normal;
	}
	public static int getNormalBySide(int sideId, BlockShapeAlgorithm algo) {
		if(algo == null){
			return sideId;
		}
		return algo.getNormalBySide(sideId);
	}

	protected int getNormalBySide(int sideId) {
		return sideId;
	}

	public void modAngledVertex(int i) {
	}

	private static int[] openToAirNone = new int[0];
	public int[] getSidesOpenToAir() {
		return openToAirNone;
	}

	public int[] getSidesToTestSpecial(){
		return openToAirNone;
	}

	public BlockShapeAlgorithm getSpriteAlgoRepresentitive() {
		return this;
	}

	

	

}
