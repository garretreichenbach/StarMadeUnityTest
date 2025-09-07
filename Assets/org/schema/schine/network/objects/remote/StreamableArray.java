package org.schema.schine.network.objects.remote;

public interface StreamableArray<E> {

	int arrayLength();

	void cleanAtRelease();

}
