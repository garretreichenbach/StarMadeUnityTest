package api.listener.fastevents;

import org.schema.common.util.linAlg.Vector3i;

/**
 * Created by Jake on 11/13/2020.
 * <insert description here>
 */
public interface SystemDrawListener {
    void preSystemDraw(Vector3i systemPos);
    void postSystemDraw(Vector3i systemPos);

}
