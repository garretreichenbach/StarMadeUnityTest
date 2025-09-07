package api.element.usable;

import api.element.block.Blocks;
import it.unimi.dsi.fastutil.shorts.Short2LongOpenHashMap;
import org.schema.game.common.controller.PlayerUsableInterface;

public class PlayerUsableHelper {
    private static Short2LongOpenHashMap reverseUsableMap = new Short2LongOpenHashMap();
    static{
        //Set default return value for the reverse map
        reverseUsableMap.defaultReturnValue(0);
    }

    //Player Usable Generator:
    //   Maybe re-write the original system to just use block ids.
    public static long getPlayerUsableId(Blocks block){
        short id = block.getId();
        if(PlayerUsableInterface.ICONS.containsValue(id)){
            //If the icon exists, get its value from the usable map
            long l = reverseUsableMap.get(id);
            if(l == 0){
                //If it doesnt exist in the reverse map, but DOES exist in the normal map, get its id
                long v = 0;
                for (Long longId : PlayerUsableInterface.ICONS.keySet()) {
                    if(PlayerUsableInterface.ICONS.get(longId) == id){
                        v = longId;
                        break;
                    }
                }
                assert v != 0 : "[Blocks] Value existed in ICONS map, did not exist in reverse map, but was not found.";
                reverseUsableMap.put(id, v);
                return v;
            }else{
                return l;
            }
        }else{
            //If there is no registered id, assign it one.
            long nextId = nextUsableId();
            PlayerUsableInterface.ICONS.put(nextId, id);
            reverseUsableMap.put(id, nextId);
            return nextId;
        }
    }
    //Value of the last usable defined in PlayerUsableInterface
    private static long usableIdLog = -9223372036854775775L;
    private static long nextUsableId(){
        return usableIdLog++;
    }
}
