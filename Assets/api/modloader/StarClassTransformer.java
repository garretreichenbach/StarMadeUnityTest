package api.modloader;

import api.mod.ModSkeleton;
import api.mod.StarLoader;
import me.jakev.starloader.IClassTransformer;


/**
 * Created by Jake on 10/8/2020.
 * Class transformer that passes class transforms to mods
 */
public class StarClassTransformer implements IClassTransformer {
    @Override
    public byte[] transform( String className, byte[] byteCode) {
        String classFilePath = className + ".class";
        ClassLoader loader = getClass().getClassLoader();
        for (ModSkeleton coreMod : StarLoader.getCoreMods()) {
            byteCode = coreMod.getRealMod().onClassTransform(className, byteCode);
        }



//        byteCode = AccessTransformerHandler.handleTransform(loader, className, byteCode);
        return byteCode;
    }
    
}