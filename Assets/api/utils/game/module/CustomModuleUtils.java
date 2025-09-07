package api.utils.game.module;

import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.UsableControllableElementManager;

import java.lang.reflect.Field;

/**
 * Created by Jake on 11/11/2020.
 * Utility class for ManagerModules
 */
public class CustomModuleUtils {
    public static void setElementManager(UsableControllableElementManager<?,?,?> cm, short computerId, short moduleId){
        setFinalShort(UsableControllableElementManager.class, cm, "controllerId", computerId);
        setFinalShort(UsableControllableElementManager.class, cm, "controllingId", moduleId);
    }

    public static void setCollectionManager(ElementCollectionManager<?,?,?> cm, short moduleId){
        setFinalShort(ElementCollectionManager.class, cm, "enhancerClazz", moduleId);
    }

    private static void setFinalShort(Class<?> clazz, Object obj, String field, short s){
        Field f = null;
        try {
            f = clazz.getDeclaredField(field);
            f.setAccessible(true);
//            f.setInt(obj, f.getModifiers() & ~Modifier.FINAL);
            f.setShort(obj, s);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

    }
}
