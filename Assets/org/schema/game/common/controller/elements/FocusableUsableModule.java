package org.schema.game.common.controller.elements;

import org.schema.game.common.controller.elements.beam.harvest.SalvageBeamCollectionManager;
import org.schema.game.common.controller.elements.beam.repair.RepairElementManager;
import org.schema.game.common.controller.elements.beam.tractorbeam.TractorBeamCollectionManager;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.language.Translatable;

public interface FocusableUsableModule {
	public enum FireMode{
		UNFOCUSED(en -> {
			return Lng.str("Unfocused");
		}),
		FOCUSED(en -> {
			return Lng.str("Focused");
		}),
		VOLLEY(en -> {
			return Lng.str("Volley");
		}),
		
		;

		private final Translatable nm;

		private FireMode(Translatable nm) {
			this.nm = nm;
		}
		public static FireMode getDefault(Class<?> c) {
			if(c == SalvageBeamCollectionManager.class || c == TractorBeamCollectionManager.class || c == RepairElementManager.class) {
				return UNFOCUSED;
			}
			return FOCUSED;
		}

		public String getName() {
			return nm.getName(this);
		}
	}
	public FireMode getFireMode();
	public boolean isInFocusMode();
	public void setFireMode(FireMode mode);
	public void sendFireMode();
}
