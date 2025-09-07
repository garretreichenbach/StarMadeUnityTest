package api.listener.fastevents;

import api.listener.fastevents.segmentpiece.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Locale;

/**
 * For events that are called so many times it's worth it to not have to create an object.
 * Much closer to the vanilla mod listeners than the event system.
 *
 *  Note that these names should be exactly the class name with `s` appended to the end.
 */
public class FastListenerCommon {
    public final static ArrayList<StatusEffectApplyListener> statusEffectApplyListeners = new ArrayList<>();
    public final static ArrayList<GameMapDrawListener> gameMapListeners = new ArrayList<>();
    public final static ArrayList<SystemDrawListener> systemDrawListeners = new ArrayList<>();
    public final static ArrayList<SegmentDrawListener> segmentDrawListeners = new ArrayList<>();
    public final static ArrayList<FactoryManufactureListener> factoryManufactureListeners = new ArrayList<>();
    public final static ArrayList<StorageItemPullListener> storageItemPullListeners = new ArrayList<>();
    public final static ArrayList<TextBoxDrawListener> textBoxListeners = new ArrayList<>();
    public final static ArrayList<RailMoveListener> railMoveListeners = new ArrayList<>();
    public final static ArrayList<ProductionItemPullListener> productionItemPullListeners = new ArrayList<>();
    public final static ArrayList<MissileUpdateListener> missileUpdateListeners = new ArrayList<>();
    public final static ArrayList<PlanetDrawListener> planetDrawListeners = new ArrayList<>();
    public final static ArrayList<SectorUpdateListener> sectorUpdateListeners = new ArrayList<>();

    public final static ArrayList<SalvageBeamHitListener> salvageBeamHitListeners = new ArrayList<>();
    public final static ArrayList<CannonProjectileHitListener> cannonProjectileHitListeners = new ArrayList<>();
    public final static ArrayList<DamageBeamHitListener> damageBeamHitListeners = new ArrayList<>();
    public final static ArrayList<BlockConfigLoadListener> blockConfigLoadListeners = new ArrayList<>();
    public final static ArrayList<ShipAIEntityAttemptToShootListener> shipAIEntityAttemptToShootListeners = new ArrayList<>();
    public final static ArrayList<ApplyAddConfigEventListener> applyAddEffectConfigEventListeners = new ArrayList<>();
    public final static ArrayList<ThrusterElementManagerListener> thrusterElementManagerListeners = new ArrayList<>();

    public final static ArrayList<RepairBeamHitListener> repairBeamHitListeners = new ArrayList<>();

    public final static ArrayList<HealingBeamHitListener> healingBeamHitListeners = new ArrayList<>();
    public final static ArrayList<CustomAddOnUseListener> customAddOnUseListeners = new ArrayList<>();


    public final static ArrayList<SegmentPieceAddListener> segmentPieceAddListeners = new ArrayList<>();
    public final static ArrayList<SegmentPieceDamageListener> segmentPieceDamageListeners = new ArrayList<>();
    public final static ArrayList<SegmentPieceKilledListener> segmentPieceKilledListeners = new ArrayList<>();
    public final static ArrayList<SegmentPieceRemoveListener> segmentPieceRemoveListeners = new ArrayList<>();

    public final static ArrayList<FrameBufferDrawListener> frameBufferDrawListeners = new ArrayList<>();

    /* Funny reflective addListener, the cost of arraylists is basically zero so this is pretty much useless.
    public static <T> void addListener(Class<T> clazz, T listener) {
        String fieldName = fieldNameFromClass(clazz);
        try {
            Field df = FastListenerCommon.class.getDeclaredField(fieldName);
            df.setAccessible(true);
            T[] arr = (T[]) df.get(null);
            T[] newArr = Arrays.copyOf(arr, arr.length + 1);
            newArr[newArr.length - 1] = listener;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }
    */
    private static String fieldNameFromClass(Class<?> clazz){
        return clazz.getSimpleName().substring(0, 1).toLowerCase(Locale.ENGLISH) + clazz.getSimpleName().substring(1) + "s";
    }

    public static void clearAllListeners(){
        try {
            for (Field field : FastListenerCommon.class.getFields()) {
                ArrayList<?> list = (ArrayList<?>) field.get(null);
                list.clear();
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
