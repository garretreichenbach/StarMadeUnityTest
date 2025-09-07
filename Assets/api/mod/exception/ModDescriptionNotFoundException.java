package api.mod.exception;

import java.io.File;

/**
 * Created by Jake on 1/21/2021.
 * <insert description here>
 */
public class ModDescriptionNotFoundException extends RuntimeException{
    public ModDescriptionNotFoundException(File file) {
        super("File: " + file.getName());
    }
}
