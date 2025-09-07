package org.schema.game.common.data.world.nat.terra.server.data;


import org.schema.game.common.data.world.nat.terra.server.Pointer;

/**
 * An object which somehow requires offheap data to be in place.
 *
 */
public interface OffheapObject {
    
    @Pointer
    long memoryAddress();
    
    int memoryLength();
}
