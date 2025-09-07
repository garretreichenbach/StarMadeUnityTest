package org.schema.game.common.controller.elements.power.reactor;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.schema.common.FastMath;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.CompareTools;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.EditableSendableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.elements.BlockKillInterface;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.ProximityCollectionInterface;
import org.schema.game.common.controller.elements.StabBonusCalcStyle;
import org.schema.game.common.controller.elements.UsableElementManager;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorTree;
import org.schema.game.common.controller.elements.ProximityCollectionInterface;
import org.schema.game.common.controller.elements.StabBonusCalcStyle;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;

import com.bulletphysics.util.ObjectArrayList;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleComparator;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongSet;

public class StabilizerCollectionManager extends ElementCollectionManager<StabilizerUnit, StabilizerCollectionManager, StabilizerElementManager> implements BlockKillInterface, ProximityCollectionInterface {

	private double stabilization;
	private int currentMeshID;
	private int meshVertexCount;
	private boolean needsMeshUpdate;
	private int meshSegController;
	private double integrity;
	private static double curOptimalDist;
	private static FloatList mc = new FloatArrayList();
	private static FloatList meshDone = new FloatArrayList() ;
	

	public StabilizerCollectionManager(
			SegmentController segController, StabilizerElementManager em) {
		super(ElementKeyMap.REACTOR_STABILIZER, segController, em);
	}

	@Override
	public int getMargin() {
		return 0;
	}

	@Override
	protected Class<StabilizerUnit> getType() {
		return StabilizerUnit.class;
	}
	

	@Override
	public boolean needsUpdate() {
		return !getSegmentController().isOnServer();
	}
	public void drawMesh(){
		
		if(currentMeshID == 0 || rawCollection.size() == 0){
			return;
		}
		
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		
		
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		GlUtil.glPushMatrix();
		
		GlUtil.glMultMatrix(getSegmentController().getWorldTransform());
		
		ShaderLibrary.colorBoxShader.loadWithoutUpdate();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, currentMeshID);
		GL11.glVertexPointer(4, GL11.GL_FLOAT, 0, 0);	
		GL11.glDrawArrays(GL11.GL_QUADS, 0, meshVertexCount);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		ShaderLibrary.colorBoxShader.unloadWithoutExit();
		
		GlUtil.glPopMatrix();
	}
	@Override
	public void update(Timer timer) {
		
		
		
		if(!getSegmentController().isOnServer() && getSegmentController().isFullyLoaded()){
		
			GameClientState state = ((GameClientState)getSegmentController().getState());
			if(state.isInAnyStructureBuildMode() && state.getCurrentPlayerObject() == getSegmentController()){
				if(needsMeshUpdate || meshSegController != getSegmentController().getId()){
					meshSegController = getSegmentController().getId();
					createStabilizationMesh();
					needsMeshUpdate = false;
				}
				if(meshDoneFlag){
					synchronized(meshDone){
						if(meshDoneFlag){
							registerMesh(meshDone);
							meshDoneFlag = false;
							meshDone.clear();
						}
					}
				}
			}
		}
		
	}

	private void registerMesh(FloatList m) {
		
		int byteSize = m.size() * ByteUtil.SIZEOF_FLOAT;
		meshVertexCount = m.size() / 4;
		System.err.println("[CLIENT] registering stabilizer mesh. Vertices: "+meshVertexCount);
		FloatBuffer byteBuffer = GlUtil.getDynamicByteBuffer(byteSize, 0).asFloatBuffer();
		
		final int size = m.size();
		for(int i = 0; i < size; i++){
			byteBuffer.put(m.getFloat(i));
		}
		byteBuffer.flip();
		if(currentMeshID == 0){
			currentMeshID = GL15.glGenBuffers();
		}
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, currentMeshID);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, byteBuffer, GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}

	@Override
	public StabilizerUnit getInstance() {
		return new StabilizerUnit();
	}
	@Override
	public CollectionShape requiredNeigborsPerBlock() {
		//all stabilizers are considered the same group
		return CollectionShape.PROXIMITY;
	}
	
	@Override
	public float getGroupProximity(){
		return VoidElementManager.REACTOR_STABILIZER_GROUPING_PROXIMITY;
	}
	@Override
	protected void onFinishedCollection() {
		super.onFinishedCollection();
		
		//sort by size (biggest first)
		Collections.sort(getElementCollections(), (o1, o2) -> {
			//sizes will be well within range
			return o2.size() - o1.size();
		});
		this.integrity = Double.POSITIVE_INFINITY;
		for(int i = 0; i < getElementCollections().size(); i++){
			StabilizerUnit u = getElementCollections().get(i);
			u.setBonusSlot(false);
			this.integrity = Math.min(this.integrity, u.getIntegrity());
		}
		
		PowerInterface p = getPowerInterface();
		p.onFinishedStabilizerChange();
		
		if(!getSegmentController().isOnServer()){
			needsMeshUpdate = true;
		}
	}
	private int[] maxSizesPerSide = new int[6];
	private StabilizerUnit[] maxStabPerSide = new StabilizerUnit[6];
	private double stabilizationWithoutBonus;
	private double stabilizationBonus;
	private int bonusSlotsUsed;
	
	
	public static void main(String[] asd){
		UsableElementManager.parseTest();
		
		stabilizationTest(10);
		System.err.println("-------------------");
		stabilizationTest(100);
		System.err.println("-------------------");
		stabilizationTest(500);
		System.err.println("-------------------");
		stabilizationTest(1000);
		System.err.println("-------------------");
		stabilizationTest(5000);
		System.err.println("-------------------");
		stabilizationTest(10000);
		System.err.println("-------------------");
		stabilizationTest(50000);
		System.err.println("-------------------");
		stabilizationTest(100000);
		System.err.println("-------------------");
		stabilizationTest(200000);
		
		System.exit(0);
	}
	
	public static void stabilizationTest(int reactorBlocks){
		
		//calculate minimum amount of stabilizers at optimal distance
		double stabilizationPercent = 0; 
		int stabilizers = 1;
		while(stabilizationPercent < 1){
			
			stabilizationPercent = PowerImplementation.getStabilizerEfficiency(PowerImplementation.getStabilization(PowerImplementation.calcStabilizationStatic(1d, 1f) * stabilizers, true), reactorBlocks);
			if(stabilizationPercent < 1){
				stabilizers++;
			}
		}
		
		
		System.err.println("Reactor blocks: "+reactorBlocks+"; Lvl: "+PowerImplementation.getReactorLevel(reactorBlocks));
		System.err.println("Stabilizers needed at optimal distance: "+stabilizers);
		
		
		for(int i = 0; i < 6; i++){
			final int bonusSides = i+1;
			float distance = 0;
			stabilizationPercent = 0; 
			while(stabilizationPercent < 1){
				//all stabilizers, no matter what group at the same distance
				double raw = PowerImplementation.getStabilization(PowerImplementation.calcStabilizationStatic(1d, distance) * stabilizers, true);
				double rawWithoutFree = PowerImplementation.getStabilization(PowerImplementation.calcStabilizationStatic(1d, distance) * stabilizers, false);
				
				DoubleList tmpList = new DoubleArrayList();
				for(int b = 1; b <= bonusSides; b++){
					double stabPart = PowerImplementation.getStabilization(PowerImplementation.calcStabilizationStatic(1d, distance) * ((double)stabilizers / (double) bonusSides), false);
					tmpList.add(stabPart);
				}
				//sort from biggest to smallest stabilization
				Collections.sort(tmpList, new DoubleComparator() {
					@Override
					public int compare(Double o1, Double o2) {
												return CompareTools.compare(o2, o1);
					}
					
					@Override
					public int compare(double o1, double o2) {
						return CompareTools.compare(o2, o1);
					}
				});
				
				double biggestStabilization = tmpList.get(0);
				
				
				double stabilizationBonusEfficiency = 0;
				for(int j = 0; j < tmpList.size(); j++){
					double bonusEfficiency = biggestStabilization == 0 ? 0 : (tmpList.get(j) / biggestStabilization);
					stabilizationBonusEfficiency += bonusEfficiency;
				}
				stabilizationBonusEfficiency /= tmpList.size();
				
				double stabilizationBonus = 0d;
				switch(tmpList.size()) {
					case (2) -> stabilizationBonus = (biggestStabilization == 0 ? 0 : (tmpList.get(1) / biggestStabilization)) * VoidElementManager.STABILIZATION_DIMENSION_BONUS_2;
					case (3) ->
						stabilizationBonus = (biggestStabilization == 0 ? 0 : (tmpList.get(1) / biggestStabilization)) * VoidElementManager.STABILIZATION_DIMENSION_BONUS_2 + (biggestStabilization == 0 ? 0 : (tmpList.get(2) / biggestStabilization)) * VoidElementManager.STABILIZATION_DIMENSION_BONUS_3;
					case (4) ->
						stabilizationBonus = (biggestStabilization == 0 ? 0 : (tmpList.get(1) / biggestStabilization)) * VoidElementManager.STABILIZATION_DIMENSION_BONUS_2 + (biggestStabilization == 0 ? 0 : (tmpList.get(2) / biggestStabilization)) * VoidElementManager.STABILIZATION_DIMENSION_BONUS_3 + (biggestStabilization == 0 ? 0 : (tmpList.get(3) / biggestStabilization)) * VoidElementManager.STABILIZATION_DIMENSION_BONUS_4;
					case (5) ->
						stabilizationBonus = (biggestStabilization == 0 ? 0 : (tmpList.get(1) / biggestStabilization)) * VoidElementManager.STABILIZATION_DIMENSION_BONUS_2 + (biggestStabilization == 0 ? 0 : (tmpList.get(2) / biggestStabilization)) * VoidElementManager.STABILIZATION_DIMENSION_BONUS_3 + (biggestStabilization == 0 ? 0 : (tmpList.get(3) / biggestStabilization)) * VoidElementManager.STABILIZATION_DIMENSION_BONUS_4 + (biggestStabilization == 0 ? 0 : (tmpList.get(4) / biggestStabilization)) * VoidElementManager.STABILIZATION_DIMENSION_BONUS_5;
					case (6) ->
						stabilizationBonus = (biggestStabilization == 0 ? 0 : (tmpList.get(1) / biggestStabilization)) * VoidElementManager.STABILIZATION_DIMENSION_BONUS_2 + (biggestStabilization == 0 ? 0 : (tmpList.get(2) / biggestStabilization)) * VoidElementManager.STABILIZATION_DIMENSION_BONUS_3 + (biggestStabilization == 0 ? 0 : (tmpList.get(3) / biggestStabilization)) * VoidElementManager.STABILIZATION_DIMENSION_BONUS_4 + (biggestStabilization == 0 ? 0 : (tmpList.get(4) / biggestStabilization)) * VoidElementManager.STABILIZATION_DIMENSION_BONUS_5 + (biggestStabilization == 0 ? 0 : (tmpList.get(5) / biggestStabilization)) * VoidElementManager.STABILIZATION_DIMENSION_BONUS_6;
					default -> {
					} //no bonus
				}
				
				//one single group is 1/x of that
//				double smallestStabForBonus = PowerImplementation.getStabilization(PowerImplementation.calcStabilizationStatic(1d, distance) * ((double)stabilizers / (double) bonusSides), false);
//				
//				//calculate bonus for collection
//				double stabilizationBonus = 0d;
//				switch(bonusSides){
//					case(0): break;
//					case(1): break; //no bonus for one dimension
//					case(2): stabilizationBonus = VoidElementManager.STABILIZATION_DIMENSION_BONUS_2 * smallestStabForBonus * (double)bonusSides; break;
//					case(3): stabilizationBonus = VoidElementManager.STABILIZATION_DIMENSION_BONUS_3 * smallestStabForBonus * (double)bonusSides; break;
//					case(4): stabilizationBonus = VoidElementManager.STABILIZATION_DIMENSION_BONUS_4 * smallestStabForBonus * (double)bonusSides; break;
//					case(5): stabilizationBonus = VoidElementManager.STABILIZATION_DIMENSION_BONUS_5 * smallestStabForBonus * (double)bonusSides; break;
//					case(6): stabilizationBonus = VoidElementManager.STABILIZATION_DIMENSION_BONUS_6 * smallestStabForBonus * (double)bonusSides; break;
//					default: throw new RuntimeException("Invalid amount of dimesnions: "+bonusSides);
//					
//				}
				
				
			
//			System.err.println("STAB BONUS: "+stabilizationBonus+"; biggest: "+biggestStabilization);
				//result is the sum of base stabilization and bonus
//				double stabilization = raw + stabilizationBonus;
				double stabilization = raw + rawWithoutFree * stabilizationBonus;
				stabilizationPercent = PowerImplementation.getStabilizerEfficiency(stabilization, reactorBlocks);
				
//				System.err.println("RAW:: "+raw+" PPP "+stabilizationPercent+"; "+distance+"; Sides: "+bonusSides+" Bonus: "+stabilizationBonus);
				if(stabilizationPercent < 1){
					distance += 0.001f;
				}
			}
			System.err.println("Percentage of optimal distance with same amount of stabilizers in "+bonusSides+" dimensions: "+StringTools.formatPointZero(distance*100d)+"%");
		}
		
		
	}
	
	
	private static class StabComp implements Comparator<StabilizerUnit>{
		@Override
		public int compare(StabilizerUnit o1, StabilizerUnit o2) {
			return CompareTools.compare(o2.getStabilization(), o1.getStabilization());
		}
	}
	private static class AngleComp implements Comparator<StabilizerUnit>{
		@Override
		public int compare(StabilizerUnit o1, StabilizerUnit o2) {
			int compare = CompareTools.compare(o2.smallestAngle, o1.smallestAngle);
			return compare != 0 ? compare : CompareTools.compare(o2.getStabilization(), o1.getStabilization());
		}
	}
	private static final StabComp stabComp = new StabComp();
	private static final AngleComp angleComp = new AngleComp();
	private List<StabilizerUnit> stabListBiggestFirst = new ObjectArrayList<StabilizerUnit>();
	private List<StabilizerUnit> biggestAngleFirst = new ObjectArrayList<StabilizerUnit>();
	private double stabilizationBonusEfficiency;
	private final FloatArrayList angleUsedList = new FloatArrayList();
	private final FloatArrayList angleBonusList = new FloatArrayList();
	private final FloatArrayList angleBonusTotalList = new FloatArrayList();
	public void calculateStabilization(final long reactorId, final long reactorCoM){
//		System.err.println(getSegmentController().getState()+"; "+getSegmentController()+" TOTAL STABILIZATION CAL START; ");
		stabListBiggestFirst.clear();
		biggestAngleFirst.clear();
		Arrays.fill(maxSizesPerSide, Integer.MIN_VALUE);
		Arrays.fill(maxStabPerSide, null);
		this.stabilization = 0;
		bonusSlotsUsed = 0;
		this.stabilizationWithoutBonus = 0;
		this.stabilizationBonusEfficiency = 0;
		this.stabilizationBonus = 0d;
		
		final int size = Math.min(PowerImplementation.getMaxStabilizerCount(), getElementCollections().size());
		//determine side of group and find biggest per side
		for(int i = 0; i < size; i++){
			StabilizerUnit u = getElementCollections().get(i);

			u.smallestAngle = FastMath.PI * 2;
			u.smallestAngleTo = null;

			int side = u.determineSide(reactorId, reactorCoM);
			u.setBonusSlot(false);
			u.setBonusEfficiency(0);
			
			if(u.size() > maxSizesPerSide[side]){
				maxSizesPerSide[side] = u.size();
				maxStabPerSide[side] = u;
			}

			for(int o = 0; o < size; o++){
				StabilizerUnit oth = getElementCollections().get(o);
				if(oth != u) {
					float angle = Math.abs(u.calcAngle(reactorId, oth, reactorCoM));

					if(angle < u.smallestAngle) {
						u.smallestAngle = angle;
						u.smallestAngleTo = oth;
					}
				}
			}
			biggestAngleFirst.add(u);
		}

		if(VoidElementManager.STABILIZER_BONUS_CALC == StabBonusCalcStyle.BY_ANGLE) {
			Collections.sort(biggestAngleFirst, angleComp);
		}

		bonusSlotsUsed = 0;


		//identify biggest groups per side and flag them
		for(int i = 0; i < 6; i++){
			if(maxStabPerSide[i] != null){
				maxStabPerSide[i].setBonusSlot(true);
				bonusSlotsUsed++;
				stabListBiggestFirst.add(maxStabPerSide[i]);
			}
		}
	
		angleUsedList.clear();
		angleBonusList.clear();
		angleBonusTotalList.clear();

//		System.err.println("AAD::: "+biggestAngleFirst.size());
		for(int i = 0; i < size; i++){
			StabilizerUnit u = getElementCollections().get(i);
			this.stabilizationWithoutBonus += u.getStabilization();

			if(VoidElementManager.STABILIZER_BONUS_CALC == StabBonusCalcStyle.BY_ANGLE) {
				u.setBonusSlot(i < 6);
			}
		}
		if(VoidElementManager.STABILIZER_BONUS_CALC == StabBonusCalcStyle.BY_ANGLE) {

			for(int i = 0; i < Math.min(5, biggestAngleFirst.size()); i++) {
				angleUsedList.add(biggestAngleFirst.get(i).smallestAngle);
			}
			stabilizationBonus = 0;
			//calculate bonus based on angle
			if(biggestAngleFirst.size() > 1) {


				float bonus2 = (biggestAngleFirst.get(0).smallestAngle / FastMath.PI) * VoidElementManager.STABILIZATION_ANGLE_BONUS_2_GROUPS;
				double totalBonus2 = bonus2 * biggestAngleFirst.get(1).getStabilization();
				biggestAngleFirst.get(0).setBonusEfficiency(totalBonus2);
				biggestAngleFirst.get(1).setBonusEfficiency(totalBonus2);

				stabilizationBonus += totalBonus2;

//				System.err.println("SMALLEST ANGLE: "+biggestAngleFirst.get(0).smallestAngle+"; bon "+bonus2+";  totbon "+totalBonus2);

				angleBonusList.add(bonus2);
				angleBonusTotalList.add((float)totalBonus2);

				if(biggestAngleFirst.size() > 2) {
					float bonus3 = (biggestAngleFirst.get(1).smallestAngle / FastMath.PI) * VoidElementManager.STABILIZATION_ANGLE_BONUS_3_GROUPS;
					double totalBonus3 = bonus3 * biggestAngleFirst.get(2).getStabilization();

					stabilizationBonus += totalBonus3;
					biggestAngleFirst.get(2).setBonusEfficiency(totalBonus3);
					angleBonusList.add(bonus3);
					angleBonusTotalList.add((float)totalBonus3);
				}
				if(biggestAngleFirst.size() > 3) {
					float bonus4 = (biggestAngleFirst.get(2).smallestAngle / FastMath.PI) * VoidElementManager.STABILIZATION_ANGLE_BONUS_4_GROUPS;
					double totalBonus4 =  bonus4 * biggestAngleFirst.get(3).getStabilization();
					stabilizationBonus += totalBonus4;
					biggestAngleFirst.get(3).setBonusEfficiency(totalBonus4);
					angleBonusList.add(bonus4);
					angleBonusTotalList.add((float)totalBonus4);
				}
				if(biggestAngleFirst.size() > 4) {
					float bonus5 = (biggestAngleFirst.get(3).smallestAngle / FastMath.PI) * VoidElementManager.STABILIZATION_ANGLE_BONUS_5_GROUPS;
					double totalBonus5 = bonus5 * biggestAngleFirst.get(4).getStabilization();
					stabilizationBonus += totalBonus5;
					biggestAngleFirst.get(4).setBonusEfficiency(totalBonus5);

					angleBonusList.add(bonus5);
					angleBonusTotalList.add((float)totalBonus5);
				}
				if(biggestAngleFirst.size() > 5) {
					float bonus6 = (biggestAngleFirst.get(4).smallestAngle / FastMath.PI) * VoidElementManager.STABILIZATION_ANGLE_BONUS_6_GROUPS;
					double totalBonus6 = bonus6 * biggestAngleFirst.get(5).getStabilization();
					stabilizationBonus += totalBonus6;
					biggestAngleFirst.get(5).setBonusEfficiency(totalBonus6);
					angleBonusList.add(bonus6);
					angleBonusTotalList.add((float)totalBonus6);
				}
			}
			this.stabilization = this.stabilizationWithoutBonus + this.stabilizationBonus;
		}else {


			if(stabListBiggestFirst.isEmpty()){
				//no stabilizers here. Nothing to do
				return;
			}

			//sort from biggest to smallest stabilization
			Collections.sort(stabListBiggestFirst, stabComp);

			double biggestStabilization = stabListBiggestFirst.get(0).getStabilization();

			for(int i = 0; i < stabListBiggestFirst.size(); i++){
				if(biggestStabilization > 0){
					stabListBiggestFirst.get(i).setBonusEfficiency(stabListBiggestFirst.get(i).getStabilization() / biggestStabilization);
				}
				stabilizationBonusEfficiency += stabListBiggestFirst.get(i).getBonusEfficiency();
			}
			stabilizationBonusEfficiency /= stabListBiggestFirst.size();


			if(biggestStabilization > 0){
				switch(stabListBiggestFirst.size()) {
					case (2) -> stabilizationBonus = (stabListBiggestFirst.get(1).getStabilization() / biggestStabilization) * VoidElementManager.STABILIZATION_DIMENSION_BONUS_2;
					case (3) ->
						stabilizationBonus = (stabListBiggestFirst.get(1).getStabilization() / biggestStabilization) * VoidElementManager.STABILIZATION_DIMENSION_BONUS_2 + (stabListBiggestFirst.get(2).getStabilization() / biggestStabilization) * VoidElementManager.STABILIZATION_DIMENSION_BONUS_3;
					case (4) ->
						stabilizationBonus = (stabListBiggestFirst.get(1).getStabilization() / biggestStabilization) * VoidElementManager.STABILIZATION_DIMENSION_BONUS_2 + (stabListBiggestFirst.get(2).getStabilization() / biggestStabilization) * VoidElementManager.STABILIZATION_DIMENSION_BONUS_3 + (stabListBiggestFirst.get(3).getStabilization() / biggestStabilization) * VoidElementManager.STABILIZATION_DIMENSION_BONUS_4;
					case (5) ->
						stabilizationBonus = (stabListBiggestFirst.get(1).getStabilization() / biggestStabilization) * VoidElementManager.STABILIZATION_DIMENSION_BONUS_2 + (stabListBiggestFirst.get(2).getStabilization() / biggestStabilization) * VoidElementManager.STABILIZATION_DIMENSION_BONUS_3 + (stabListBiggestFirst.get(3).getStabilization() / biggestStabilization) * VoidElementManager.STABILIZATION_DIMENSION_BONUS_4 + (stabListBiggestFirst.get(4).getStabilization() / biggestStabilization) * VoidElementManager.STABILIZATION_DIMENSION_BONUS_5;
					case (6) ->
						stabilizationBonus = (stabListBiggestFirst.get(1).getStabilization() / biggestStabilization) * VoidElementManager.STABILIZATION_DIMENSION_BONUS_2 + (stabListBiggestFirst.get(2).getStabilization() / biggestStabilization) * VoidElementManager.STABILIZATION_DIMENSION_BONUS_3 + (stabListBiggestFirst.get(3).getStabilization() / biggestStabilization) * VoidElementManager.STABILIZATION_DIMENSION_BONUS_4 + (stabListBiggestFirst.get(4).getStabilization() / biggestStabilization) * VoidElementManager.STABILIZATION_DIMENSION_BONUS_5 + (stabListBiggestFirst.get(5).getStabilization() / biggestStabilization) * VoidElementManager.STABILIZATION_DIMENSION_BONUS_6;
					default -> {
					} //no bonus
				}
			}

			this.stabilization = this.stabilizationWithoutBonus + this.stabilizationWithoutBonus * this.stabilizationBonus;
		}
//		this.stabilizationBonus = 0d;
//		switch(bonusSlotsUsed){
//			case(0): break;
//			case(1): break; //no bonus for one dimension
//			case(2): stabilizationBonus = VoidElementManager.STABILIZATION_DIMENSION_BONUS_2 * smallestStabForBonus * (double)bonusSlotsUsed; break;
//			case(3): stabilizationBonus = VoidElementManager.STABILIZATION_DIMENSION_BONUS_3 * smallestStabForBonus * (double)bonusSlotsUsed; break;
//			case(4): stabilizationBonus = VoidElementManager.STABILIZATION_DIMENSION_BONUS_4 * smallestStabForBonus * (double)bonusSlotsUsed; break;
//			case(5): stabilizationBonus = VoidElementManager.STABILIZATION_DIMENSION_BONUS_5 * smallestStabForBonus * (double)bonusSlotsUsed; break;
//			case(6): stabilizationBonus = VoidElementManager.STABILIZATION_DIMENSION_BONUS_6 * smallestStabForBonus * (double)bonusSlotsUsed; break;
//			default: throw new RuntimeException("Invalid amount of dimesnions: "+bonusSlotsUsed);
//		}
		
		

		
//		this.stabilization = this.stabilizationWithoutBonus + this.stabilizationBonus;

//		System.err.println(getSegmentController().getState()+"; "+getSegmentController()+" TOTAL STABILIZATION CAL END: "+stabilization+" = "+this.stabilizationWithoutBonus+" + "+this.stabilizationWithoutBonus+" * "+this.stabilizationBonus);
		
		Arrays.fill(maxSizesPerSide, Integer.MIN_VALUE);
		Arrays.fill(maxStabPerSide, null);
		stabListBiggestFirst.clear();
		biggestAngleFirst.clear();
	}
	private static Long2FloatMap mpc = new Long2FloatOpenHashMap();
	private static Long2FloatMap handle = new Long2FloatOpenHashMap();
	private static boolean dirty = false;
	private void createStabilizationMesh() {
		
		
		if(!getElementCollections().isEmpty() && getElementCollections().get(0).distances != null){
			synchronized(mpc){
				mpc.clear();
//				mpc.deepApplianceCopy(rawCollection);
				for(StabilizerUnit e : getElementCollections()){
					mpc.putAll(e.distances);
				}
				curOptimalDist = getPowerInterface().getReactorOptimalDistance();
//				assert(rawCollection.size() == mpc.size()):getElementCollections().get(0).distances.size()+"; "+mpc.size();
				dirty = true;
				mpc.notifyAll();
			}
			for(StabilizerUnit u : getElementCollections()) {
				u.distances = null;
			}
		}else{
//			System.err.println("SKIPPED CREATING MESH::: "+getElementCollections().isEmpty()+"; "+(!getElementCollections().isEmpty() && getElementCollections().get(0).distances != null));
		}
	}
	
	
	public static void startStaticThread() {
		Thread thread = new Thread(new MeshCreator(), "StabilizerMeshCreator");
		thread.setPriority(2);
		thread.setDaemon(true);
		thread.start();		
	}
	private static boolean meshDoneFlag;
	private static class MeshCreator implements Runnable{
		

		@Override
		public void run() {
			while(true){
				synchronized(mpc){
					while(!dirty){
						try {
							mpc.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					handle.clear();
//					handle.deepApplianceCopy(mpc);
					handle.putAll(mpc);
					assert(mpc.size() == handle.size());
					dirty = false;
				}
				createMesh(handle);
			}
		}

		private void createMesh(Long2FloatMap handle) {
			Vector3f pos = new Vector3f();
			LongSet keySet = handle.keySet();
			for(long l : keySet){
				
				for(int side = 0; side < 6; side++){
					Vector3i d = Element.DIRECTIONSi[side];
					int x = ElementCollection.getPosX(l);
					int y = ElementCollection.getPosY(l);
					int z = ElementCollection.getPosZ(l);
					long neigh = ElementCollection.getIndex(x + d.x, y + d.y, z + d.z);
					if(!keySet.contains(neigh)){
					
						pos.set(x,y,z);
						float distance = handle.get(l);
						float distanceEff = (float) PowerImplementation.calcStabilizationStatic(curOptimalDist, distance);
						switch(side) {
							case (Element.FRONT) -> {
								addVert(mc, pos.x - 0.5f, pos.y + 0.5f, pos.z + 0.5f, distanceEff);
								addVert(mc, pos.x - 0.5f, pos.y - 0.5f, pos.z + 0.5f, distanceEff);
								addVert(mc, pos.x + 0.5f, pos.y - 0.5f, pos.z + 0.5f, distanceEff);
								addVert(mc, pos.x + 0.5f, pos.y + 0.5f, pos.z + 0.5f, distanceEff);
							}
							case (Element.BACK) -> {
								addVert(mc, pos.x - 0.5f, pos.y - 0.5f, pos.z - 0.5f, distanceEff);
								addVert(mc, pos.x - 0.5f, pos.y + 0.5f, pos.z - 0.5f, distanceEff);
								addVert(mc, pos.x + 0.5f, pos.y + 0.5f, pos.z - 0.5f, distanceEff);
								addVert(mc, pos.x + 0.5f, pos.y - 0.5f, pos.z - 0.5f, distanceEff);
							}
							case (Element.TOP) -> {
								addVert(mc, pos.x + 0.5f, pos.y + 0.5f, pos.z + 0.5f, distanceEff);
								addVert(mc, pos.x + 0.5f, pos.y + 0.5f, pos.z - 0.5f, distanceEff);
								addVert(mc, pos.x - 0.5f, pos.y + 0.5f, pos.z - 0.5f, distanceEff);
								addVert(mc, pos.x - 0.5f, pos.y + 0.5f, pos.z + 0.5f, distanceEff);
							}
							case (Element.BOTTOM) -> {
								addVert(mc, pos.x - 0.5f, pos.y - 0.5f, pos.z + 0.5f, distanceEff);
								addVert(mc, pos.x - 0.5f, pos.y - 0.5f, pos.z - 0.5f, distanceEff);
								addVert(mc, pos.x + 0.5f, pos.y - 0.5f, pos.z - 0.5f, distanceEff);
								addVert(mc, pos.x + 0.5f, pos.y - 0.5f, pos.z + 0.5f, distanceEff);
							}
							case (Element.RIGHT) -> {
								addVert(mc, pos.x + 0.5f, pos.y - 0.5f, pos.z + 0.5f, distanceEff);
								addVert(mc, pos.x + 0.5f, pos.y - 0.5f, pos.z - 0.5f, distanceEff);
								addVert(mc, pos.x + 0.5f, pos.y + 0.5f, pos.z - 0.5f, distanceEff);
								addVert(mc, pos.x + 0.5f, pos.y + 0.5f, pos.z + 0.5f, distanceEff);
							}
							case (Element.LEFT) -> {
								addVert(mc, pos.x - 0.5f, pos.y + 0.5f, pos.z + 0.5f, distanceEff);
								addVert(mc, pos.x - 0.5f, pos.y + 0.5f, pos.z - 0.5f, distanceEff);
								addVert(mc, pos.x - 0.5f, pos.y - 0.5f, pos.z - 0.5f, distanceEff);
								addVert(mc, pos.x - 0.5f, pos.y - 0.5f, pos.z + 0.5f, distanceEff);
							}
						}
					}
				}
			}
			
			synchronized(meshDone){
				meshDone.clear();
				meshDone.addAll(mc);
				meshDoneFlag = true;
			}
			
			mc.clear();
		}

		private void addVert(FloatList mc, float x, float y, float z, float d) {
			mc.add(x-Segment.HALF_DIM);
			mc.add(y-Segment.HALF_DIM);
			mc.add(z-Segment.HALF_DIM);
			mc.add(d);
		}
	}
	@Override
	protected void onChangedCollection() {
		
	}

	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {
		return new GUIKeyValueEntry[]{
				new ModuleValueEntry(Lng.str("Placeholder"), "")};
	}

	@Override
	public String getModuleName() {
		return Lng.str("Reactor Stabilizer");
	}

	@Override
	public float getSensorValue(SegmentPiece connected){
		return  0;
	}


	public double getStabilization() {
		return stabilization;
	}

	@Override
	public void onKilledBlock(long pos, short type, Damager from) {
		if(getSegmentController().isOnServer()){
			checkIntegrity(pos, type, from);
		}
	}

	public double getIntegrity() {
		return this.integrity;
	}
	private static class SizeSorter implements Comparator<StabilizerUnit>{

		@Override
		public int compare(StabilizerUnit o1, StabilizerUnit o2) {
			return o2.size() - o1.size();
		}
		
	}
	private static SizeSorter sizeSorter = new SizeSorter();
	public void calculatePaths(ReactorTree activeReactor, List<StabilizerPath> stabilizerPaths) {
		Collections.sort(getElementCollections(), sizeSorter);
		final int size = Math.min(PowerImplementation.getMaxStabilizerCount(), getElementCollections().size());
		
		double d = 0;
		for(int i = 0; i < size; i++){
			StabilizerUnit u = getElementCollections().get(i);
			d += u.size();
		}
		
		
		for(int i = 0; i < size; i++){
			StabilizerUnit u = getElementCollections().get(i);
			double weight = u.size() / d;
			StabilizerPath calculatePath = u.calculatePath(weight, activeReactor, getElementManager().getManagerContainer().stabilizerNodePath);
			if(calculatePath != null){
				stabilizerPaths.add(calculatePath);
			}
		}
	}

	public void killRandomBlocks(int blocks, Damager damager) {
		if(getSegmentController().isOnServer()){
			LongArrayList s = new LongArrayList();
			int i = 0;
			for(long l : rawCollection){
				s.add(l);
				i++;
				if(i >= blocks){
					
					break;
				}
			}
			SegmentPiece p = new SegmentPiece();
			
			for(long l : s){
				SegmentPiece segmentPiece = getSegmentController().getSegmentBuffer().getPointUnsave(l, p);
				if(segmentPiece != null){
					((EditableSendableSegmentController)getSegmentController()).killBlock(segmentPiece);
				}
			}
			
		}
	}

	public int getGetDimsUsed() {
		return bonusSlotsUsed;
	}

	public double getStabilizationWithoutBonus() {
		return stabilizationWithoutBonus;
	}


	public double getStabilizationBonus() {
		return stabilizationBonus;
	}

	public double getBonusEfficiency() {
		return stabilizationBonusEfficiency;
	}

	public FloatArrayList getAngleUsedList() {
		return angleUsedList;
	}

	public FloatArrayList getAngleBonusList() {
		return angleBonusList;
	}

	public FloatArrayList getAngleBonusTotalList() {
		return angleBonusTotalList;
	}



	
	
}
