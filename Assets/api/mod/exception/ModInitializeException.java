package api.mod.exception;

import api.mod.ModSkeleton;
import api.mod.ModStarter;

/**
 * Created by Jake on 2/16/2021.
 * <insert description here>
 */
public class ModInitializeException extends RuntimeException {
    public ModInitializeException(ModSkeleton mod, ModStarter.LoadStage stage, Throwable cause) {
        super("Mod: " + mod.getDebugName() + " Failed on load stage: " + stage + ", Cause of exception: ", cause);
    }
}
