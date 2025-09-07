package org.schema.schine.network.objects.remote;

public interface NetworkChangeObserver {
	void update(Streamable<?> streamable);

	boolean isSynched();
}
