package org.schema.schine.resource;

public abstract class ResourceLoadEntryOther extends ResourceLoadEntry{


	public ResourceLoadEntryOther(String name) {
		super(name);
	}

	@Override
	public LoadEntryType getType() {
		return LoadEntryType.OTHER;
	}


	@Override
	public String getFilePath() {
		return null;
	}

}
