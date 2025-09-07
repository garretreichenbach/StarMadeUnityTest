package org.schema.common.util;

import java.io.IOException;
import java.io.InputStream;

import org.schema.schine.resource.ResourceLoadEntry;

public abstract class AssetInfo<E> extends ResourceLoadEntry{

	public AssetInfo(String name) {
		super(name);
	}

	public abstract AssetId<E> getId();

    /**
     * Implementations of this method should return an {@link InputStream}
     * allowing access to the data represented by the {@link AssetId}.
     * <p>
     * Each invocation of this method should return a new stream to the
     * asset data, starting at the beginning of the file.
     * 
     * @return The asset data.
     * @throws IOException 
     */
    public abstract InputStream openStream() throws IOException;

}
