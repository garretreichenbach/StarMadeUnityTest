package org.schema.game.common.data.world.nat.terra.server.node;

/**
 * Chunk is a node which contains many blocks.
 *
 */
public interface Chunk extends Node {
    
    Object getRef(int blockId);
    
    void setRef(int blockId, Object ref);
}
