package api.listener.events.register;

import api.listener.events.Event;
import org.schema.schine.graphicsengine.forms.MeshGroup;
import org.schema.schine.resource.MeshLoader;

/**
 * Created by Jake on 4/12/2021.
 * <insert description here>
 */
public class MeshLoadEvent extends Event {

    private final String name;
    private final String fileName;
    private final String path;
    private final String physicsMesh;
    private final MeshGroup mesh;
    private MeshLoader meshLoader;

    public MeshLoadEvent(String name, String fileName, String path, String physicsMesh, MeshGroup mesh, MeshLoader meshLoader) {

        this.name = name;
        this.fileName = fileName;
        this.path = path;
        this.physicsMesh = physicsMesh;
        this.mesh = mesh;
        this.meshLoader = meshLoader;
    }

    public MeshLoader getMeshLoader() {
        return meshLoader;
    }

    public String getName() {
        return name;
    }

    public String getFileName() {
        return fileName;
    }

    public String getPath() {
        return path;
    }

    public String getPhysicsMesh() {
        return physicsMesh;
    }

    public MeshGroup getMesh() {
        return mesh;
    }
}
