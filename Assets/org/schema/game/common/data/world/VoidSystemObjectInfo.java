package org.schema.game.common.data.world;

import api.common.GameCommon;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.schine.resource.tag.Tag;
import theleo.jstruct.Struct;

import java.io.Serializable;
import java.util.Observable;

/**
    Information regarding a world object in a star system (planet, moon, anomaly, etc) which is relevant outside its own sector.<br/>
    It should be possible to construct the (unmodified) physical form of the object from any full implementation instance of this abstract class.
 */

public abstract class VoidSystemObjectInfo extends Observable implements Serializable {
    /**
     * Faction ID of the owning faction.
     */
    private int owner = 0;
    /**
     * Current name of the object, may be changeable by owning faction or by circumstance.
     */
    private String name;
    /**
     * Name given upon generation. The object name will revert to this when a faction claim is lost.
     */
    private String originalName;
    private String description;
    /**
    Name of the type of object. e.g. "Lava Moon" or "Dark Matter Nebula"
     */
    private String cataName;
    protected int seed;
    private Vector3i location;

    protected VoidSystemObjectInfo(Vector3i location, String originalName, int seed) {
        this.location = location;
        this.seed = seed;
        this.originalName = originalName;
        this.name = originalName;
        //TODO acquire observers
    }

    protected VoidSystemObjectInfo(Vector3i location, Tag source) {
        fromTagStructure(source);
        this.location = location;
        //TODO acquire observers
    }

    protected VoidSystemObjectInfo(Tag source){
        fromTagStructure(source);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyObservers();
    }

    public void resetName(){
        setName(originalName);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategoricalName() {
        return cataName;
    }

    protected void setCategoricalName(String v) {
        this.cataName = v;
    }

    public final int getSeed() {
        return seed;
    }

    public String getOriginalName() {
        return originalName;
    }

    public int getOwner() {
        return owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
        if(owner == FactionManager.ID_NEUTRAL) resetName();
        notifyObservers();
    }

    /**
     * @return if this celestial object automatically changes faction ownership when its containing system does.<br/>
     * This is useful for something which cannot have a faction block placed on it, such as a gas-giant planet or a space anomaly.
     */
    public abstract boolean claimOnSystemClaim();

    public void resetOwner(){
        setOwner(FactionManager.ID_NEUTRAL);
    }

    public final Tag toTagStructure(boolean forClient){
        Tag[] subtags = toSubTagStructure(forClient);
        if(subtags == null){
            throw new RuntimeException("[WORLD] VoidSystem celestial object failed to create subtag information!!!");
        }
        return new Tag(Tag.Type.STRUCT,"VoidSystemObject[" + getCategoricalName() + "]",new Tag[]{
                new Tag(Tag.Type.STRING,"OriginalName",originalName),
                new Tag(Tag.Type.STRING,"CurrentName",name),
                new Tag(Tag.Type.INT,"OwnerFaction",owner),
                new Tag(Tag.Type.STRING, "Category",getCategoricalName()),
                new Tag(Tag.Type.INT,"GenSeed",seed),
                new Tag(Tag.Type.VECTOR3i,"LocationSector",getLocation()),
                new Tag(Tag.Type.STRUCT,"ObjectDetails",subtags),
                new Tag(Tag.Type.FINISH,"[END]",null),
        });
    }

    public final void fromTagStructure(Tag tag){
        Tag[] info = tag.getStruct();
        originalName = info[0].getString();
        name = info[1].getString();
        owner = info[2].getInt();
        setCategoricalName(info[3].getString());
        seed = info[4].getInt();
        location = info[5].getVector3i();
        fromSubTagStructure(info[6].getStruct()); //populate child info
    }

    public Vector3i getLocation() {
        return new Vector3i(location);
    }

    protected abstract Tag[] toSubTagStructure(boolean forClient);

    protected abstract void fromSubTagStructure(Tag[] info);

    protected final boolean isServer(){
        return !GameCommon.isClientConnectedToServer(); //TODO temp
    }
}
