package org.schema.game.common.controller.elements.power.reactor;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import org.schema.common.util.CompareTools;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.PolygonToolsVars;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.elements.BlockConnectionPath;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorTree;
import org.schema.game.common.controller.elements.shield.CenterOfMassUnit;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.schine.common.language.Lng;

import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class StabilizerUnit extends CenterOfMassUnit<StabilizerUnit, StabilizerCollectionManager, StabilizerElementManager> {

	public final PolygonToolsVars v = new PolygonToolsVars();

	public Long2FloatOpenHashMap distances;
	
	private double stabilization;
	
	public float distMargin = 5;

	private int reactorSide = -1;

	private long reactorIdSide = Long.MIN_VALUE;

	private boolean bonusSlot;

	private String reactorSideString = "";

	private double bonusEfficiency;
	
	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		Vector3i dim = new Vector3i();
		dim.sub(getMax(new Vector3i()), getMin(new Vector3i()));
		return ControllerManagerGUI.create(state, Lng.str("StabilizerUnit Module"), this,
				new ModuleValueEntry(Lng.str("Dimension"), dim));
	}
	
	@Override
	public void onClear() {
		super.onClear();
		//probably not gonna do much, but possibly helps garbo col to mark it quicker for cleanup
		distances = null;
	}

	@Override
	public void calculateExtraDataAfterCreationThreaded(long updateSignture, LongOpenHashSet totalCollectionSet){
//		System.err.println(getSegmentController().getState()+" "+getSegmentController()+" ID POS "+getIdPos(new Vector3i())+" +++ CALCULATE STABILIZATION DISTANCES START");
		final List<MainReactorUnit> reactors;
		final double reactorOptimalDist;
		synchronized(getSegmentController().getState()){
			try{
				getSegmentController().getState().setSynched();
				//get all current main reactors
				
				reactors = new ObjectArrayList<MainReactorUnit>(getPowerInterface().getMainReactors());
				reactorOptimalDist = getPowerInterface().getReactorOptimalDistance(); 
				
			}finally{
				getSegmentController().getState().setUnsynched();
			}
		}
		
		distances = new Long2FloatOpenHashMap(getNeighboringCollection().size());
		distances.defaultReturnValue(-1);
		stabilization = 0;
		long totalTaken = 0;
		long time = System.currentTimeMillis();
		for(int i = 0; i < getNeighboringCollection().size(); i++){
			
			long index = getNeighboringCollection().getLong(i);
			float minDist = Float.POSITIVE_INFINITY;
			
			for(MainReactorUnit u : reactors){
				minDist = Math.min(minDist, u.distanceToThis(index, v));
			}
			if(minDist != Float.POSITIVE_INFINITY){
				distances.put(index, minDist);
				stabilization += getPowerInterface().calcStabilization(reactorOptimalDist, minDist);
			}
//			if(elementCollectionManager.cancelUpdateStatus == updateSignture){
//				System.err.println(this+" DISTANCE FUNC CANCELED");
//				break;
//			}
			
			
			if(i > 0 && i % 10000 == 0){
				long taken = System.currentTimeMillis() - time;
				totalTaken += taken;
				double avg = (double)totalTaken / (double)(i+1);
				if(!this.getSegmentController().isOnServer()){
					System.err.println(this+" DONE: "+i+" / "+getNeighboringCollection().size()+"; avg processing: "+StringTools.formatPointZeroZero(avg)+" ms");
				}
				time = System.currentTimeMillis();
			}
		}
		
		super.calculateExtraDataAfterCreationThreaded(updateSignture, totalCollectionSet);
		
//		System.err.println(getSegmentController().getState()+" "+getSegmentController()+" ID POS "+getIdPos(new Vector3i())+" SIZE: "+getNeighboringCollection().size()+" DSIZE: "+distances.size()+"; ### CALCULATE STABILIZATION DISTANCES FINISHED (stab: "+stabilization+")");
		
		if(getSegmentController().isOnServer()) {
			//distances is not needed on server. on client its needed to create the mesh. after that it is set to null
			distances = null;
		}
	}
	public double getStabilization() {
		return stabilization;
	}
	public StabilizerPath calculatePath(double weight, ReactorTree r, BlockConnectionPath nodesOrig) {
		
		ObjectArrayList<LongArrayList> paths = new ObjectArrayList<LongArrayList>(nodesOrig.getPaths());
		

		Vector3i com = getCoMOrigin();
		long stabPos = ElementCollection.getIndex(com);
		long reactorPos = r.getCenterOfMass();
		
		
		final long start = reactorPos;
		final long end = stabPos;
		
		int rx = ElementCollection.getPosX(reactorPos);
		int ry = ElementCollection.getPosY(reactorPos);
		int rz = ElementCollection.getPosZ(reactorPos);
		
		
		final int sx = ElementCollection.getPosX(stabPos);
		final int sy = ElementCollection.getPosY(stabPos);
		final int sz = ElementCollection.getPosZ(stabPos);
		
		final float minDist = VoidElementManager.REACTOR_STABILIZATION_ENERGY_STREAM_DISTANCE;
		if(minDist >= 0){
			float dx = rx - sx;
			float dy = ry - sy;
			float dz = rz - sz;
			
			float d = Vector3fTools.length(dx, dy, dz);
			if(d <= minDist){
				return null;
			}
			
			
		}
		
		
		float rxD = rx-com.x;
		float ryD = ry-com.y;
		float rzD = rz-com.z;
		float reactorDistSquared = rxD*rxD + ryD*ryD + rzD*rzD;
		
		float sxD = sx-com.x;
		float syD = sy-com.y;
		float szD = sz-com.z;
		
		
		float shortest = reactorDistSquared;
		LongArrayList path = null;
		//find path that has its end closest to the stabilizer
		for(LongArrayList p : paths){
			long l = p.getLong(p.size()-1); //end of the path
			
			int x = ElementCollection.getPosX(l);
			int y = ElementCollection.getPosY(l);
			int z = ElementCollection.getPosZ(l);
			
			float xD = x-sx;
			float yD = y-sy;
			float zD = z-sz;
			float distSquared = xD*xD + yD*yD + zD*zD;
			
			while(distSquared < shortest){
				path = p;
				shortest = distSquared;
			}
		}
		
		Collections.sort(paths, (o1, o2) -> {
			float distSquaredo1;
			float distSquaredo2;
			{
				long l = o1.getLong(o1.size()-1); //end of the path

				int x = ElementCollection.getPosX(l);
				int y = ElementCollection.getPosY(l);
				int z = ElementCollection.getPosZ(l);

				float xD = x-sx;
				float yD = y-sy;
				float zD = z-sz;
				distSquaredo1 = xD*xD + yD*yD + zD*zD;
			}
			{
				long l = o2.getLong(o2.size()-1); //end of the path

				int x = ElementCollection.getPosX(l);
				int y = ElementCollection.getPosY(l);
				int z = ElementCollection.getPosZ(l);

				float xD = x-sx;
				float yD = y-sy;
				float zD = z-sz;
				distSquaredo2 = xD*xD + yD*yD + zD*zD;
			}

			return CompareTools.compare(distSquaredo1, distSquaredo2);
		});
		
		if(path != null){
			
			//remove all that are too far from stab
			for(int i = 0; i < paths.size(); i++){
				LongArrayList o1 = paths.get(i);
			
				long l = o1.getLong(o1.size()-1); //end of the path
				
				int x = ElementCollection.getPosX(l);
				int y = ElementCollection.getPosY(l);
				int z = ElementCollection.getPosZ(l);
				
				float xD = x-sx;
				float yD = y-sy;
				float zD = z-sz;
				float d = xD*xD + yD*yD + zD*zD;
				
				if( d > shortest+distMargin*distMargin){
					paths.remove(i);
					i--;
				}
			}
			//sort rest by total length to reactor
			Collections.sort(paths, (o1, o2) -> {
				float len1 = calcPathLength(o1, start, end);
				float len2 = calcPathLength(o2, start, end);
				return CompareTools.compare(len1, len2);
			});
			//select shortest path that is still suffitiently close to stabilizer
			path = paths.get(0);
		}
		
		
		StabilizerPath p = new StabilizerPath(weight, getPowerInterface(), this);
		p.start = start;
		
		
		long cur = p.start;
		if(path != null){
			for(long e : path){
				p.nodes.put(cur, e);
				cur = e;
			}
		}
		p.nodes.put(cur, end);
		
		return p;
	}
	private float calcPathLength(LongArrayList path, long start, long end) {
		float len = 0;
		long cur = start;
		for(int i = 0; i < path.size(); i++){
			long next = path.getLong(i);
			int x = ElementCollection.getPosX(cur);
			int y = ElementCollection.getPosY(cur);
			int z = ElementCollection.getPosZ(cur);
			
			int xn = ElementCollection.getPosX(next);
			int yn = ElementCollection.getPosY(next);
			int zn = ElementCollection.getPosZ(next);
			
			len += Vector3fTools.length(xn-x, yn-y, zn-z);
			cur = next;
		}
		int x = ElementCollection.getPosX(cur);
		int y = ElementCollection.getPosY(cur);
		int z = ElementCollection.getPosZ(cur);
		int xn = ElementCollection.getPosX(end);
		int yn = ElementCollection.getPosY(end);
		int zn = ElementCollection.getPosZ(end);
		len += Vector3fTools.length(xn-x, yn-y, zn-z);
		return len;
	}
	private Vector3f up = new Vector3f(0, 1, 0);
	private Vector3f hlp = new Vector3f();
	private Vector3f dist = new Vector3f();
	private Vector3f distOther = new Vector3f();
	private Matrix3f m = new Matrix3f();

	public float smallestAngle;

	public StabilizerUnit smallestAngleTo;
	public int determineSide(long reactorId, long reactorCoM) {
		
		
		
		dist.x = getCoMOrigin().x - ElementCollection.getPosX(reactorCoM);
		dist.y = getCoMOrigin().y - ElementCollection.getPosY(reactorCoM);
		dist.z = getCoMOrigin().z - ElementCollection.getPosZ(reactorCoM);
		
		ReactorTree t = getPowerInterface().getReactorSet().getTreeMap().get(reactorId);
		m.set(t.getBonusMatrix());
		m.invert();
		m.transform(dist);
		
		if(Math.abs(dist.x) > Math.abs(dist.y) && Math.abs(dist.x) >= Math.abs(dist.z)){
			this.reactorSide = dist.x >= 0 ? Element.RIGHT : Element.LEFT;
			this.reactorSideString = "*"+dist.x+"*, "+dist.y+", "+dist.z;
		}else if(Math.abs(dist.y) >= Math.abs(dist.x) && Math.abs(dist.y) > Math.abs(dist.z)){
			this.reactorSide = dist.y >= 0 ? Element.TOP : Element.BOTTOM;
			this.reactorSideString = dist.x+", *"+dist.y+"*, "+dist.z;
		}else{
			this.reactorSide = dist.z >= 0 ? Element.FRONT : Element.BACK;
			this.reactorSideString = dist.x+", "+dist.y+", *"+dist.z+"*";
		}
		this.reactorIdSide = reactorId;
		return this.reactorSide;
	}
	public float calcAngle(long reactorId, StabilizerUnit other, long reactorCoM) {
		
		
		
		dist.x = getCoMOrigin().x - ElementCollection.getPosX(reactorCoM);
		dist.y = getCoMOrigin().y - ElementCollection.getPosY(reactorCoM);
		dist.z = getCoMOrigin().z - ElementCollection.getPosZ(reactorCoM);
		
		distOther.x = other.getCoMOrigin().x - ElementCollection.getPosX(reactorCoM);
		distOther.y = other.getCoMOrigin().y - ElementCollection.getPosY(reactorCoM);
		distOther.z = other.getCoMOrigin().z - ElementCollection.getPosZ(reactorCoM);
		
		float angle = Vector3fTools.getAngleSigned(dist, distOther, up, hlp);
		//both vecs are normalized after
		
		
		return angle;
	}
	public long getReactorIdSide() {
		return reactorIdSide;
	}
	public int getReactorSide() {
		return reactorSide;
	}
	public void setBonusSlot(boolean bs) {
		this.bonusSlot = bs;
	}
	public boolean isBonusSlot() {
		return bonusSlot;
	}
	public String getReactorSideString() {
		return reactorSideString;
	}
	public double getBonusEfficiency() {
		return bonusEfficiency;
	}
	public void setBonusEfficiency(double bonusEfficiency) {
		this.bonusEfficiency = bonusEfficiency;
	}
	
}
