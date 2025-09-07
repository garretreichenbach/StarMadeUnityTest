package api.listener.events.world;

import api.listener.events.Event;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.SectorInformation;

import javax.vecmath.Vector4f;

/**
 * STARMADE MOD
 * CREATOR: Ithirahad Ivrar'kiim
 * DATE: 8/27/2020
 * TIME: Too damn late.
 */
public class ProceduralSkyboxColorEvent extends Event {

    /**
     * Fires when the skybox chooses colors. Allows you to override those color choices.
     * Status: Should work.
     */
    private Vector3i systemCoordinates;
    private SectorInformation.SectorType sectorType;
    private Vector4f skyboxColor1; //grba
    private Vector4f skyboxColor2; //grba?

    public ProceduralSkyboxColorEvent(Vector3i systemCoordinates, SectorInformation.SectorType sectorType, Vector4f skyboxColor1, Vector4f skyboxColor2){
        this.systemCoordinates = systemCoordinates;
        this.sectorType = sectorType;
        this.skyboxColor1 = skyboxColor1;
        this.skyboxColor2 = skyboxColor2;
    }

    public void setColor1(float r, float g, float b, float a){
	    skyboxColor1 = new Vector4f(g, r, b, a);}

    public void setColor2(float r, float g, float b, float a){
	    skyboxColor2 = new Vector4f(g, r, b, a);}

    public void setColor1(Vector4f skyboxColor1) {
        this.skyboxColor1 = skyboxColor1;
    }

    public void setColor2(Vector4f skyboxColor2) {
        this.skyboxColor2 = skyboxColor2;
    }

    public Vector3i getSystemCoordinates(){
        return systemCoordinates;
    }

    public SectorInformation.SectorType getSectorType(){
        return sectorType;
    }

    public Vector4f getColor1(){
        return skyboxColor1;
    }

    public Vector4f getColor2(){
        return skyboxColor2;
    }
}
