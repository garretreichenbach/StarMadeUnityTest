package api.listener.fastevents;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.planetdrawer.PlanetInformations;
import org.schema.game.common.data.Dodecahedron;
import org.schema.game.common.data.world.SectorInformation;
import org.schema.schine.graphicsengine.forms.Mesh;

/**
 * PlanetDrawListener.java
 * Planet draw fast listener
 * ==================================================
 * Created 02/14/2021
 * @author TheDerpGamer
 */
public interface PlanetDrawListener {

    /**
     * @param sector The sector coordinates
     * @param planetInfo The planet's info stats
     * @param planetType The planet type
     * @param atmosphere The atmosphere
     * @param core The planet's core
     */
    void onPlanetDraw(Vector3i sector, PlanetInformations planetInfo, SectorInformation.PlanetType planetType, Mesh atmosphere, Dodecahedron core);
}