package org.schema.game.common.data.world.nat.terra.server.data;

/**
 * Decides best storage option for given block data.
 *
 */
public class TypeSelector {
    
    // Chunk data formats
    private WorldDataFormat uncompressedChunk;
    private WorldDataFormat palette16Chunk;
    private WorldDataFormat rle22Chunk;
    
    // Octree data formats
    private WorldDataFormat octreeNode;
    
    public TypeSelector() { }
    
    /**
     * Gets (probably) best data format for data with given values.
     * @param matCount Material count. Required.
     * @return More or less suitable data provider for given data.
     */
    public WorldDataFormat getDataFormat(int matCount) {
        if (matCount < 17) {
            return palette16Chunk;
        } else {
            return uncompressedChunk;
        }
    }
    
    public WorldDataFormat nextFormat(WorldDataFormat previous) {
        assert previous != null;
        
        if (previous == palette16Chunk) {
            return uncompressedChunk;
        }
        
        throw new IllegalArgumentException("next format not available");
    }
}
