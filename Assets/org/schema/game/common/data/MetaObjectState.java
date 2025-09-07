package org.schema.game.common.data;

import org.schema.game.common.data.element.meta.MetaObjectManager;

public interface MetaObjectState {

	public MetaObjectManager getMetaObjectManager();

	public void requestMetaObject(int id);
}
