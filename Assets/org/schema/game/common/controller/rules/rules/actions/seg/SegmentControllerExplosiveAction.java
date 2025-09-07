package org.schema.game.common.controller.rules.rules.actions.seg;

import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.UsableElementManager;
import org.schema.schine.common.language.Lng;

public abstract class SegmentControllerExplosiveAction extends SegmentControllerAction {
	
	

	public SegmentControllerExplosiveAction() {
		super();
	}



	@Override
	public String getDescriptionShort() {
		return Lng.str("Makes %s explode if it gets hit (block destroyed)", getExplName());
	}

	public abstract String getExplName();


	public abstract UsableElementManager<?, ?, ?> getElementManager(ManagedUsableSegmentController<?> se);

	@Override
	public void onTrigger(SegmentController se) {
		if(se instanceof ManagedUsableSegmentController<?>) {
			ManagedUsableSegmentController<?> s = (ManagedUsableSegmentController<?>)se;
			UsableElementManager<?, ?, ?> elementManager = getElementManager(s);
			if(elementManager != null) {
				elementManager.setExplosiveStructure(true);
			}
		}
	}

	@Override
	public void onUntrigger(SegmentController se) {
		if(se instanceof ManagedUsableSegmentController<?>) {
			ManagedUsableSegmentController<?> s = (ManagedUsableSegmentController<?>)se;
			UsableElementManager<?, ?, ?> elementManager = getElementManager(s);
			if(elementManager != null) {
				elementManager.setExplosiveStructure(false);
			}
		}
	}

	
	
}
