package org.schema.schine.network.objects.remote;

public interface RemoteSerializableFactory<E extends RemoteSerializable> {

	public E instantiate();

}
