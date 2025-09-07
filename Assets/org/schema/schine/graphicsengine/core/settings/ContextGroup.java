package org.schema.schine.graphicsengine.core.settings;

import org.schema.schine.common.language.Lng;
import org.schema.schine.common.language.Translatable;

public enum ContextGroup {
	ALL(en -> Lng.str("All"),ContextFilter.CRUCIAL, ContextFilter.IMPORTANT, ContextFilter.NORMAL, ContextFilter.TRIVIAL),
	MOST(en -> Lng.str("Most"),ContextFilter.CRUCIAL, ContextFilter.IMPORTANT, ContextFilter.NORMAL),
	SOME(en -> Lng.str("Some"), ContextFilter.CRUCIAL, ContextFilter.IMPORTANT),
	CRUCIAL_ONLY(en -> Lng.str("None"), ContextFilter.CRUCIAL),
	NONE(en -> Lng.str("None"));
	
	private final ContextFilter[] filters;
	private Translatable translation;
	
	private ContextGroup (Translatable translation, final ContextFilter... filters){
		this.filters = filters;
		this.translation = translation;
	}
	
	@Override
	public String toString() {
		return name();
	}	
	
	public String getDisplayName(){
		return translation.getName(this);
	}
	
	public boolean containsFilter(ContextFilter filter) {
		for (int i = 0; i < filters.length; i++) {
			if(filter.equals(filters[i])){
				return true;
			}
		}
		return false;
	}

	
}


