package org.schema.game.common.data.mines;

import java.util.Collection;
import java.util.List;

import org.schema.game.common.controller.elements.mines.MineController;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.core.Timer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class MineActivityLevelContainer {
	public static class MineHashSet extends ObjectOpenHashSet<Mine>{
		/**
		 * 
		 */
		private static final long serialVersionUID = 753118082558116039L;
	}
	
	public enum ActiveLevel{
		INACTIVE(Float.POSITIVE_INFINITY, 5000),
		STANDBY(2200, 2000),
		WAKING(1000, 1000),
		AWAKE(700, 700),
		ACTIVE(500, 120),
		ALERT(250, 60),
		COLLISION_CHECK(Float.NEGATIVE_INFINITY, 30),
		;
		public final float distance;
		public final int updateMilliSecs;

		private ActiveLevel(float distance, int updateMilliSecs) {
			this.distance = distance;
			this.updateMilliSecs = updateMilliSecs;
		}
		
	}
	private final MineHashSet[] actLevelMines = new MineHashSet[ActiveLevel.values().length];
	private final long[] lastUpdateActLevel = new long[ActiveLevel.values().length];
	private final List<Mine> tmpMineUpdate = new ObjectArrayList<Mine>();
	private int lastSecEntities;
	public MineActivityLevelContainer(){
		ActiveLevel[] as = ActiveLevel.values();
		for(ActiveLevel a : as) {
			actLevelMines[a.ordinal()] = new MineHashSet();
		}
	}
	
	public void updateActiveLevel(Mine mine, ActiveLevel oldLevel, ActiveLevel newLevel) {
		if(oldLevel != newLevel) {
			actLevelMines[oldLevel.ordinal()].remove(mine);
			actLevelMines[newLevel.ordinal()].add(mine);
			
//			if(!mine.isOnServer()) {
//				System.err.println(" AAAA "+mine+" "+oldLevel.name()+" -> "+newLevel.name());
//			}
		}
	}

	public void updateLocal(Timer timer, MineController mineController) {
		ActiveLevel[] av = ActiveLevel.values();
		for(int i = 0; i < av.length; i++) {
			ActiveLevel a = av[i];
			
			
			if(a.updateMilliSecs > -1 && (timer.currentTime - lastUpdateActLevel[i]) > a.updateMilliSecs) {
				
				
				tmpMineUpdate.addAll(actLevelMines[i]);
				mineController.handleMineUpdate(timer, tmpMineUpdate);
				
				if(a == ActiveLevel.COLLISION_CHECK) {
					//only do collision check in highest update level
					mineController.handleMineCollisions(timer, tmpMineUpdate);
				}
				
				
				lastUpdateActLevel[i] = timer.currentTime;
			}
			tmpMineUpdate.clear();
		}
		
	}

	public void onSectorChange(Collection<SimpleTransformableSendableObject<?>> entities, Collection<Mine> mines) {
		if(entities.isEmpty()) {
			//no entities in sector -> set all mines incative
			
			for(Mine m : mines) {
				if(m.getActiveLevel() != ActiveLevel.INACTIVE) {
					m.setActiveLevel(ActiveLevel.INACTIVE);
				}
			}
		}else if(lastSecEntities == 0){
			for(Mine m : mines) {
				if(m.getActiveLevel() == ActiveLevel.INACTIVE) {
					//set mine active if necessary
					m.updateActivityLevelOnly(entities);
				}
			}
		}		
		lastSecEntities = entities.size();
	}

	public void add(Mine mine) {
		actLevelMines[mine.getActiveLevel().ordinal()].add(mine);		
	}

	public void clearActivity() {
		lastSecEntities = 0;		
	}

	public void remove(Mine mine) {
		actLevelMines[mine.getActiveLevel().ordinal()].remove(mine);		
	}

	public void clear() {
		clearActivity();
		for(MineHashSet s : actLevelMines) {
			s.clear();
		}		
	}
	
}
