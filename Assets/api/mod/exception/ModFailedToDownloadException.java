package api.mod.exception;

import api.mod.ModIdentifier;

/**
 * Created by Jake on 3/19/2021.
 * <insert description here>
 */
public class ModFailedToDownloadException extends RuntimeException{
    public ModFailedToDownloadException(ModIdentifier identifier, Throwable exc) {
        super("Mod "+identifier+" failed to download. This is usually a problem with the mod author not setting version info correctly.", exc);
    }
}
