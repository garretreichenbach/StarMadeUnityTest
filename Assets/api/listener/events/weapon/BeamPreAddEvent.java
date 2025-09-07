package api.listener.events.weapon;

import api.listener.events.Event;
import org.schema.game.common.controller.elements.beam.BeamCommand;
import org.schema.game.common.data.element.beam.AbstractBeamHandler;

import javax.vecmath.Vector3f;

public class BeamPreAddEvent extends Event {
    private final AbstractBeamHandler<?> handlerf;
    private BeamCommand commandf;

    /**
     * Called whenever a BeamUnit shoots a beam (via a BeamCommand)
     */
    public BeamPreAddEvent(AbstractBeamHandler<?> beamHandler, BeamCommand beamCommand) {

        handlerf = beamHandler;
        commandf = beamCommand;
    }

    public BeamCommand getCommand() {
        return commandf;
    }

    /**
     * Calculates and returns the direction of the shot beam
     */
    public Vector3f getBeamDirection(){
        Vector3f to = new Vector3f(commandf.to);
        to.sub(commandf.from);
        to.normalize();
        return to;
    }
    /**
     * Returns if a ray intersects a sphere
     * Algorithm Credits:
     *     https://www.ccs.neu.edu/home/fell/CS4300/Lectures/Ray-TracingFormulas.pdf
     *     https://viclw17.github.io/2018/07/16/raytracing-ray-sphere-intersection/
     */
    public static boolean beamIntersectsSphere(Vector3f center, float radius, Vector3f beamFrom, Vector3f beamTo){
        Vector3f ray_dir = new Vector3f(beamTo);
        ray_dir.sub(beamFrom);
        ray_dir.normalize();

        Vector3f oc = new Vector3f(beamFrom);
        oc.sub(center);
        float a = ray_dir.dot(ray_dir);
        float b = 2F * oc.dot(ray_dir);
        float c = oc.dot(oc) - radius*radius;
        float discriminant = b*b - 4*a*c;
        return discriminant > 0;
    }
}
