package org.schema.schine.sound.manager.engine.openal;

import static org.schema.schine.sound.manager.engine.openal.AL.AL_INVALID_ENUM;
import static org.schema.schine.sound.manager.engine.openal.AL.AL_INVALID_NAME;
import static org.schema.schine.sound.manager.engine.openal.AL.AL_INVALID_OPERATION;
import static org.schema.schine.sound.manager.engine.openal.AL.AL_INVALID_VALUE;
import static org.schema.schine.sound.manager.engine.openal.AL.AL_NO_ERROR;
import static org.schema.schine.sound.manager.engine.openal.AL.AL_OUT_OF_MEMORY;

public final class ALUtil {

    private ALUtil() {
    }
    
    public static String getALErrorMessage(int errorCode) {
        String errorText = switch(errorCode) {
            case AL_NO_ERROR -> "No Error";
            case AL_INVALID_NAME -> "Invalid Name";
            case AL_INVALID_ENUM -> "Invalid Enum";
            case AL_INVALID_VALUE -> "Invalid Value";
            case AL_INVALID_OPERATION -> "Invalid Operation";
            case AL_OUT_OF_MEMORY -> "Out of Memory";
            default -> "Unknown Error Code: " + String.valueOf(errorCode);
        };
        return errorText;
    }
    
    public static void checkALError(AL al) {
        int err = al.alGetError();
        if (err != AL_NO_ERROR) {
            throw new RuntimeException("OpenAL Error: " + getALErrorMessage(err));
        }
    }
}
