package org.schema.game.client.view.buildhelper;

import org.schema.schine.graphicsengine.forms.Transformable;

public interface BuildHelperFactory {
	public BuildHelper getInstance(Transformable trans);
	public Class<? extends BuildHelper> getBuildHelperClass();
}
