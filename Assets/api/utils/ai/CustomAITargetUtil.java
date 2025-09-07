package api.utils.ai;

import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.ai.program.turret.states.SeachingForTurretTarget;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.HashMap;

/**
 * Created by Jake on 7/26/2021.
 * Allows for adding custom AI targeting types, like "Selected Entity", "Any", "Missiles", etc
 */
public class CustomAITargetUtil {
    private static final HashMap<String, FindTargetProgram> customPrograms = new HashMap<>();
    public static void registerNewTargetProgram(String name, FindTargetProgram targetProgram){
        customPrograms.put(name, targetProgram);
        System.err.println("[CustomAITargetUtil] Registering new program: " + name);
    }

    public static HashMap<String, FindTargetProgram> getCustomPrograms() {
        return customPrograms;
    }

    public interface FindTargetProgram{
        @Nullable
        SimpleTransformableSendableObject<?> findTarget(@NotNull SeachingForTurretTarget state);
    }
}
