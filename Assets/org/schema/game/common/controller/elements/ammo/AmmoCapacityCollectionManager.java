package org.schema.game.common.controller.elements.ammo;

import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.elements.BlockKillInterface;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.data.SegmentPiece;

import static org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager.*;

public abstract class AmmoCapacityCollectionManager
        <
            E extends AmmoCapacityUnit<E,CM,EM>,
            CM extends AmmoCapacityCollectionManager<E,CM,EM>,
            EM extends AmmoCapacityElementManager<E,CM,EM>
        >
        extends ElementCollectionManager<E,CM,EM> implements BlockKillInterface {

    public AmmoCapacityCollectionManager(final short blockID, final SegmentController segController, final EM elementManager) {
        super(blockID, segController, elementManager);
    }

    @Override
    public int getMargin() {
        return 0;
    }

    @Override
    public boolean needsUpdate() {
        return false;
    }

    @Override
    protected void onFinishedCollection() {
        super.onFinishedCollection();
        float maxBef = getElementManager().getCapacityMax();

        final float size = (float)getTotalSize();
        final float maxCap = getMaxCapacity(getElementManager(), size);

        getElementManager().setCapacityMax(maxCap);

        if(getSegmentController().isOnServer() && getElementManager().getCapacityMax() < maxBef) {
            //send if max capacity got smaller. as it only gets bigger upon loading
            float cBef = getElementManager().getCapacityFilled();
            getElementManager().setAmmoCapacity(Math.min(getElementManager().getCapacityFilled(), getElementManager().getCapacityMax()));
            if(cBef != getElementManager().getCapacityFilled()) {
                getElementManager().sendAmmoCapacity();
            }
        }

    }

    protected float getMaxCapacity(AmmoCapacityElementManager<?, ?, ?> em, float size) {
        final float capacityBasic = em.getBasicCapacity();

        return switch(em.getCapacityCalcStyle()) {
            case LINEAR -> capacityBasic + size * getElementManager().getCapacityPerBlockLinear();
            case EXP -> capacityBasic + Math.max(0, (float) Math.pow(size, em.getCapacityExp()) * em.getCapacityExpMult());
            case DOUBLE_EXP -> {
                float threshold = em.getCapacityExponentThreshold();
                float h1 = em.getCapacityDoubleExpFirstHalf();
                float h1m = em.getCapacityDoubleExpMultFirstHalf();
                float h2 = em.getCapacityDoubleExpSecondHalf();
                float h2m = em.getCapacityDoubleExpMultSecondHalf();

                float exp1 = threshold * (float) Math.pow(Math.min((size / threshold), 1f), h1) * h1m;
                float exp2 = (float) Math.pow(Math.max(size - threshold, 0f), h2) * h2m;

                yield em.getBasicCapacity() + exp1 + exp2;
            }
            case LOG -> em.getBasicCapacity() + Math.max(0, ((float) Math.log10(size) + em.getCapacityLogOffset()) * em.getCapacityLogFactor());
            default -> throw new RuntimeException("Illegal calc style " + em.getCapacityCalcStyle());
        };
    }

    @Override
    public boolean isDetailedElementCollections() {
        return false;
    }
    @Override
    public GUIKeyValueEntry[] getGUICollectionStats() {
        getElementManager();
        return new GUIKeyValueEntry[]{};
    }


    @Override
    public float getSensorValue(SegmentPiece connected){
        WeaponType w = getElementManager().getWeaponType();
        return Math.min(1f, (getSegmentController().getAmmoCapacity(w) / getSegmentController().getAmmoCapacityMax(w)));
    }
    @Override
    public org.schema.game.common.controller.elements.ElementCollectionManager.CollectionShape requiredNeigborsPerBlock() {
        return CollectionShape.ALL_IN_ONE;
    }

    @Override
    public void onKilledBlock(long pos, short type, Damager from) {
        checkIntegrity(pos, type, from);
    }

    @Override
    protected void onChangedCollection() {
    }

    @Override
    protected abstract Class<E> getType();

    @Override
    public abstract E getInstance();

    @Override
    public abstract String getModuleName();
}
