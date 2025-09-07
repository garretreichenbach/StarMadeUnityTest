package api.mod.exception;

import java.io.File;

/**
 * Created by Jake on 1/21/2021.
 * <insert description here>
 */
public class ModInvalidMetadataException extends RuntimeException{
    public ModInvalidMetadataException(String rawJson, File file) {
        super("Invalid mod.json, make sure all fields are properly specified. file=" + file.getName() + ", rawJson=\n" + rawJson);
    }
}
