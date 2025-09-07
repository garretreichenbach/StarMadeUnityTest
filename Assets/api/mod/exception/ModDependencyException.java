package api.mod.exception;

import api.mod.ModSkeleton;

/**
 * Created by Jake on 12/29/2020.
 * <insert description here>
 */
public class ModDependencyException extends NullPointerException {
    public ModDependencyException(ModSkeleton parent, int mod) {
        super("Mod: " + parent.getDebugName() + " required mod: " + mod + ", but was not found.");
    }
}
