package org.schema.schine.network.objects.remote;

public interface NetworkChangeObservable {
	public boolean hasChanged();

	public boolean initialSynchUpdateOnly();

	public boolean keepChanged();

	public void setChanged(boolean changed);

	void setObserver(NetworkChangeObserver arg0);
}
