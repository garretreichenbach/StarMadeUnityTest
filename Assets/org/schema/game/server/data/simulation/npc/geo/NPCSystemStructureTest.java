package org.schema.game.server.data.simulation.npc.geo;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.schema.common.util.linAlg.Vector3i;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class NPCSystemStructureTest {
	public static void main(String[] sadsad){
		speedTestIterator(new Vector3i(0,0,0));
		testIterator(new Vector3i(23,-234,2));
	}
	private static void speedTestIterator(Vector3i sSys){
		List<Vector3i> bruteList = new ObjectArrayList<Vector3i>();
		List<Vector3i> directList = new ObjectArrayList<Vector3i>();
		
		long totalBrute = 0;
		long totalDirect = 0;
		int tries = 30;
		int dist = 15;
		for(int i = 0; i < tries; i++){
		
			
			
			bruteList.clear();
			directList.clear();
			long s = System.nanoTime();
			getTestBruteIterator(dist, bruteList, sSys);
			totalBrute += (System.nanoTime()-s);
			
			s = System.nanoTime();
			getTestDirectIterator(dist, directList, sSys);
			totalDirect += (System.nanoTime()-s);
		
		}
		
		double avgBrute = (double)totalBrute / (double)tries;
		double avgDirect = (double)totalDirect / (double)tries;
		
		System.err.println("BRUTE TOT: "+(avgBrute/1000d)+"ms");
		System.err.println("DIREC TOT: "+(avgDirect/1000d)+"ms");
	}
	private static void testIterator(Vector3i sSys){
		Set<Vector3i> bruteSet = new ObjectOpenHashSet<Vector3i>();
		Set<Vector3i> directSet = new ObjectOpenHashSet<Vector3i>();
		
		List<Vector3i> bruteList = new ObjectArrayList<Vector3i>();
		List<Vector3i> directList = new ObjectArrayList<Vector3i>();
		
		
		for(int i = 0; i < 1000; i++){
			bruteSet.clear();
			directSet.clear();
			bruteList.clear();
			directList.clear();
			
			getTestBruteIterator(i, bruteList, sSys);
			getTestDirectIterator(i, directList, sSys);
			
			bruteSet.addAll(bruteList);
			directSet.addAll(directList);
			assert(bruteSet.equals(directSet));
			
			assert(directList.size() == directSet.size());
			assert(bruteList.size() == directList.size());
			
			
			for(int l = 0; l < directList.size(); l++){
				assert(bruteSet.contains(directList.get(l))):directList.get(l)+"; ";
			}
			System.err.println("ALL TESTS OK FOR "+i+"; Sys: "+sSys);
		}
	}
	
	private static void getTestBruteIterator(int lvl, Collection<Vector3i> col, Vector3i sSys){
		if(lvl == 0){
			col.add(new Vector3i(sSys));
			return;
		}
		for(int x = -lvl; x <= lvl; x++){
			for(int y = -lvl; y <= lvl; y++){
				for(int z = -lvl; z <= lvl; z++){
					col.add(new Vector3i(sSys.x+x, sSys.y+y, sSys.z+z));
				}
			}
		}
		for(int x = -lvl+1; x <= lvl-1; x++){
			for(int y = -lvl+1; y <= lvl-1; y++){
				for(int z = -lvl+1; z <= lvl-1; z++){
					col.remove(new Vector3i(sSys.x+x, sSys.y+y, sSys.z+z));
				}
			}
		}
	}
	private static void getTestDirectIterator(int lvl, Collection<Vector3i> col, Vector3i sSys){
		if(lvl == 0){
			col.add(new Vector3i(sSys));
			return;
		}
		int c = 0;
		Vector3i tmp = new Vector3i();
		for(int x = -lvl; x <= lvl; x++){
			for(int z = -lvl; z <= lvl; z++){
				int y = lvl;
				tmp.set(x,y,z);
				tmp.add(sSys);
				col.add(new Vector3i(tmp));
				c++;
				y = -lvl;
				tmp.set(x,y,z);
				tmp.add(sSys);
				col.add(new Vector3i(tmp));
				c++;
			}
		}
		
		for(int y = -lvl+1; y <= lvl-1; y++){
			for(int x = -lvl; x <= lvl; x++){
				int z = lvl;
				tmp.set(x,y,z);
				tmp.add(sSys);
				col.add(new Vector3i(tmp));
				c++;
				z = -lvl;
				tmp.set(x,y,z);
				tmp.add(sSys);
				col.add(new Vector3i(tmp));
				c++;
			}
			//don't include first and last one on Z, because that is already covered by the X iteration
			for(int z = -lvl+1; z <= lvl-1; z++){
				int x = lvl;
				tmp.set(x,y,z);
				tmp.add(sSys);
				c++;
				col.add(new Vector3i(tmp));
				x = -lvl;
				tmp.set(x,y,z);
				tmp.add(sSys);
				col.add(new Vector3i(tmp));
				c++;
			}
		}
	}
}
