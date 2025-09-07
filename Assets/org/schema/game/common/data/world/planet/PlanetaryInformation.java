package org.schema.game.common.data.world.planet;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.VoidSystemObjectInfo;
import org.schema.game.server.data.simulation.resource.PassiveResourceProvider;
import org.schema.schine.resource.tag.Tag;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.Random;

/**
    Generic information needed by any planet subtype.
 */
public abstract class PlanetaryInformation extends VoidSystemObjectInfo {
    /**
    Radius data for the planet. May be used differently by subclasses (e.g. relative vs. absolute)
     */
    protected Integer radius = null;
    private PassiveResourceProvider atmosphereResources; //preferably null on MP clients for FoW
    private PassiveResourceProvider coreResources; //preferably null on MP clients for FoW

    protected PlanetaryInformation(Vector3i location, String name, int seed, int radius) {
        super(location, name, seed);
        this.radius = radius;
    }

    protected PlanetaryInformation(Vector3i location, String name, int seed) {
        super(location, name, seed);
    }

    protected PlanetaryInformation(Vector3i location, Tag source){
        super(location,source);
    }

    public PlanetaryInformation(Tag source) {
        super(source);
    }

    protected abstract int createRadius(Random rand);

    public int getRadius() {
        return radius;
    }

    public PassiveResourceProvider getAtmosphereResources() {
        return atmosphereResources;
    }

    public void setAtmosphereResources(PassiveResourceProvider atmosphereResources) {
        this.atmosphereResources = atmosphereResources;
    }

    public PassiveResourceProvider getCoreResources() {
        return coreResources;
    }

    public void setCoreResources(PassiveResourceProvider coreResources) {
        this.coreResources = coreResources;
    }

    /**
        * Initializes any procedurally-generated properties. Should always be called after first instantiation or when loaded from tag structure.
     */
    @OverridingMethodsMustInvokeSuper
    protected void init(Random rand) {
        if(radius == null) this.radius = createRadius(rand);
    }

    @Override
    protected final Tag[] toSubTagStructure(boolean excludeServerOnlyInfo){
        //Todo: Even if we aren't storing these in the tag, we should still at least store the resource rates because those may have 
        //to be alterable and sync to clients on their own
        
        /* These are stored globally instead, see PassiveResourceManager
        Tag atmo;
        if (excludeServerOnlyInfo || atmosphereResources == null)
            atmo = new Tag("AtmoResPlaceholder", Tag.Type.NOTHING);
        else
            atmo = new Tag(Tag.Type.STRUCT, "AtmoResources", atmosphereResources.toTagStructure());

        Tag core;
        if (excludeServerOnlyInfo || coreResources == null)
            core = new Tag("CoreResPlaceholder", Tag.Type.NOTHING);
        else
            core = new Tag(Tag.Type.STRUCT, "CoreResources", coreResources.toTagStructure());
         */
        return new Tag[]{
            new Tag(Tag.Type.INT, "radius", radius),
            new Tag(Tag.Type.STRUCT, "planetCategoryUniqueInfo", toPlanetSubTagStructure()),
//            atmo,
//            core,
            new Tag(Tag.Type.FINISH, "[END]", null)
        };
    }

    @Override
    protected final void fromSubTagStructure(Tag[] info){
        radius = info[0].getInt();
        fromPlanetSubTagStructure(info[1].getStruct());

        /* These are stored globally instead, see PassiveResourceManager
        Tag atmo = info[2];
        if(atmo.getType() == Tag.Type.STRUCT) atmosphereResources = new PassiveResourceProvider(info[3].getStruct());

        Tag core = info[3];
        if(core.getType() == Tag.Type.STRUCT) coreResources = new PassiveResourceProvider(info[5].getStruct());
         */
    }

    /**
        @return information specific to the planet subclass (e.g. terrestrial world or gas giant) to a tag array.<br/>
        This can include info such as colours/materials of atmosphere or ground layers, noise parameters, etc.<br/>
        (Try to keep this brief. Anything that can be re-generated locally from the planet seed generally should be, in order to avoid bandwidth spikes.)
     */
    protected abstract Tag[] toPlanetSubTagStructure();

    /**
         Reads information specific to the planet subclass (e.g. terrestrial world or gas giant) from tags, assigning it to variables.
         This method should not do any initialization of procedurally generated properties, as PlanetaryInformation will call init() when necessary after calling this method.
     */
    protected abstract void fromPlanetSubTagStructure(Tag[] tags);

    protected Random getRand(){
        return new Random(seed);
    }
}
