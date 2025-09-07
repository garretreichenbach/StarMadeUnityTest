package org.schema.game.client.view.mines;

import java.util.Collections;
import java.util.List;

import org.schema.game.client.controller.ClientSectorChangeListener;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.meshlod.LODDeferredSpriteCollection;
import org.schema.game.common.controller.elements.mines.ClientMineListener;
import org.schema.game.common.data.mines.Mine;
import org.schema.game.common.data.mines.Mine.MineType;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Sprite;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class MineDrawer implements Drawable, ClientMineListener, ClientSectorChangeListener{
	public final GameClientState state;
	
	
	private MineDrawerSystem[] systemsInactive;
	private MineDrawerSystem[] systemsActive;


	private final List<Mine> minesToAdd = new ObjectArrayList<Mine>();
	private final List<Mine> minesToRemove = new ObjectArrayList<Mine>();
	
	private final LODDeferredSpriteCollection<MineDrawableData> deferred = new LODDeferredSpriteCollection<MineDrawableData>();
	
	public MineDrawer(GameClientState state) {
		this.state = state;
	}

	@Override
	public void cleanUp() {
		if(systemsInactive != null) {
			for(int i = 0; i < systemsInactive.length; i++) {
				systemsInactive[i].cleanUp();
			}
		}
		if(systemsActive != null) {
			for(int i = 0; i < systemsActive.length; i++) {
				systemsActive[i].cleanUp();
			}
		}
	}

	@Override
	public void draw() {
		for(MineDrawerSystem s : systemsInactive) {
//			if(s.getEntries().size() > 0) {
//				System.err.println("DRAW INACTIVE "+s.getEntries().size());
//			}
			s.draw(deferred);
		}
		for(MineDrawerSystem s : systemsActive) {
//			if(s.getEntries().size() > 0) {
//				System.err.println("DRAW ACTIVE "+s.getEntries().size());
//			}
			s.draw(deferred);
			
		}
		if(deferred.size() > 0) {
			Collections.sort(deferred);
			deferred.deferredSprite.setBillboard(true);
			Sprite.draw3D(deferred.deferredSprite, deferred, Controller.getCamera());
			deferred.clear();
		}

	}
		
	

	@Override
	public boolean isInvisible() {
		return false;
	}

	@Override
	public void onInit() {
		this.state.getController().getMineController().addClientMineListener(this);
		this.state.getController().addSectorChangeListener(this);
		
		
		MineType[] v = MineType.values();
		systemsInactive = new MineDrawerSystem[MineType.values().length];
		for(int i = 0; i < v.length; i++) {
			systemsInactive[i] = new MineDrawerSystem(v[i], false);
		}
		systemsActive = new MineDrawerSystem[MineType.values().length];
		for(int i = 0; i < v.length; i++) {
			systemsActive[i] = new MineDrawerSystem(v[i], true);
		}
	}

	@Override
	public void onRemovedMine(Mine m) {
		minesToRemove.add(m);
	}

	@Override
	public void onAddMine(Mine m) {
		minesToAdd.add(m);
	}
	public void update(Timer timer) {
		if(systemsInactive == null || systemsActive == null) {
			return;
		}
		for(Mine m : minesToAdd) {
			assert(m != null);
			assert(m.getType() != null);
			if(m.isActive()) {
				systemsActive[m.getType().ordinal()].addEntry(m);
			}else {
				systemsInactive[m.getType().ordinal()].addEntry(m);
			}
		}
		minesToAdd.clear();
		for(Mine m : minesToRemove) {
			boolean removedInAct = systemsInactive[m.getType().ordinal()].removeEntry(m);
			boolean removedAct = systemsActive[m.getType().ordinal()].removeEntry(m);
			assert(removedInAct || removedAct):"Not removed mine";
		}
		minesToRemove.clear();

		
		for(MineDrawerSystem s : systemsInactive) {
			s.update(timer);
		}
		for(MineDrawerSystem s : systemsActive) {
			s.update(timer);
		}
	}
	@Override
	public void onChangedMine(Mine m) {
	}

	@Override
	public void onSectorChangeSelf(int newSector, int oldSector) {
	}

	@Override
	public void onBecomingInactive(final Mine mine) {
		for(int i = 0; i < systemsActive.length; i++) {
			//move inactive mines from active
			MineDrawableData dt = systemsActive[i].mines.get(mine.getId());
			if(dt != null) {
				boolean rm = systemsActive[i].removeEntry(mine);
				assert(rm):mine;
				systemsInactive[i].addEntry(mine);	
			}
		}	
	}

	@Override
	public void onBecomingActive(final Mine mine) {
		for(int i = 0; i < systemsInactive.length; i++) {
			//move active mines from inactive
			MineDrawableData dt = systemsInactive[i].mines.get(mine.getId());
			if(dt != null) {
				boolean rm = systemsInactive[i].removeEntry(mine);
				assert(rm):mine;
				systemsActive[i].addEntry(mine);	
			}
		}		
	}
}
