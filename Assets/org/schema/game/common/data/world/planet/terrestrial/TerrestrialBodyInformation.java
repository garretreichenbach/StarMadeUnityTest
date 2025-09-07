package org.schema.game.common.data.world.planet.terrestrial;

import org.apache.poi.util.NotImplemented;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.SectorInformation;
import org.schema.game.common.data.world.planet.PlanetaryInformation;
import org.schema.game.server.controller.world.factory.terrain.TerrainDeco;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.resource.tag.Tag;

import javax.vecmath.Vector4f;
import java.util.Random;

/**
 * Information for a planet, moon, etc. with a surface.
 */

public class TerrestrialBodyInformation extends PlanetaryInformation {
    
    private SectorInformation.PlanetType mainType; //provides terrain/etc. for now; mostly for backwards compatibility later on
    @NotImplemented
    private Vector4f atmosphereColor = new Vector4f();
    @NotImplemented
    public TerrestrialPlanetMaterialsProvider materialsSource;
    //TODO planet generation properties, surface features etc

    public TerrestrialBodyInformation(Vector3i location, int seed, int radius, SectorInformation.PlanetType oldType, TerrestrialPlanetMaterialsProvider materials) {
        super(location, "Planet", seed, radius);
        this.mainType = oldType;
        this.materialsSource = materials;
        init(getRand());
    }

    public TerrestrialBodyInformation(Vector3i location, int seed, SectorInformation.PlanetType type) {
        super(location, "Planet", seed);
        this.mainType = type;
        this.materialsSource = getFromOldType();
        init(getRand());
    }

    public TerrestrialBodyInformation(Tag source){
        super(source);
        init(getRand());
    }

    @Override
    protected void init(Random rand) {
        super.init(rand);
        setCategoricalName(mainType.name());
        setDescription("[PLACEHOLDER TEXT] A terrestrial planet made of blocks. Build what you want here; the sky isn't the limit!");
        if(isServer()) {
            if(getAtmosphereResources() == null) {
                //Todo: Implement planet atmosphere resources
            }
            if(getCoreResources() == null) {
                //Todo: Implement planet core resources
            }
        }
    }

    private TerrestrialPlanetMaterialsProvider getFromOldType() {
        return null; //TODO
    }

    public SectorInformation.PlanetType getMainType() {
        return mainType;
    }

    @Override
    public boolean claimOnSystemClaim() {
        return false; //can actually place faction blocks on these; yey.
    }

    @Override
    protected int createRadius(Random rand) {
        float expected = ServerConfig.PLANET_SIZE_MEAN_VALUE.getFloat();
        float deviation = ServerConfig.PLANET_SIZE_DEVIATION_VALUE.getFloat();
        return (int) (expected + rand.nextGaussian() * (deviation / 3.0f));
    }

    @Override
    protected Tag[] toPlanetSubTagStructure() {
        return new Tag[]{
                new Tag(Tag.Type.INT,"PlanetArchetype", mainType.ordinal()),
                new Tag(Tag.Type.FINISH, "[END]", null)
        };
    }

    @Override
    protected void fromPlanetSubTagStructure(Tag[] tags) {
        mainType = SectorInformation.PlanetType.values()[tags[0].getInt()];
    }

    public void setTypeAndRefresh(SectorInformation.PlanetType type) {
        mainType = type;
        init(getRand());
    }

    public interface TerrestrialPlanetMaterialsProvider {
        public String getDescription();

        public short getSurface();

        public short getSoil();

        public short getSubstrate();

        public short getOcean();

        public short getCaveBottomLiquid();

        public SectorInformation.PlanetType getOldEnumType();

        public TerrainDeco[] getDecos();
    }
}
